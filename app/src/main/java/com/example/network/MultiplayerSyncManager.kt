package com.example.network

import com.example.model.ChatMessage
import com.example.model.GameMode
import com.example.model.LudoToken
import com.example.model.Player
import com.example.model.PlayerColor
import com.example.model.RoomInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.security.MessageDigest
import kotlin.random.Random

sealed class NetworkEvent {
    data class PlayerJoined(val player: Player) : NetworkEvent()
    data class PlayerLeft(val playerId: String) : NetworkEvent()
    data class DiceRolled(val playerId: String, val diceValue: Int, val antiCheatHash: String) : NetworkEvent()
    data class TokenMoved(val playerId: String, val tokenId: Int, val diceValue: Int) : NetworkEvent()
    data class NewChatMessage(val message: ChatMessage) : NetworkEvent()
    data class FloatingEmoji(val playerId: String, val emoji: String) : NetworkEvent()
    data class GameStarted(val roomInfo: RoomInfo, val players: List<Player>) : NetworkEvent()
    data class PingUpdate(val pingMs: Int) : NetworkEvent()
}

class MultiplayerSyncManager(
    private val scope: CoroutineScope
) {
    private val _currentRoom = MutableStateFlow<RoomInfo?>(null)
    val currentRoom: StateFlow<RoomInfo?> = _currentRoom.asStateFlow()

    private val _lobbyPlayers = MutableStateFlow<List<Player>>(emptyList())
    val lobbyPlayers: StateFlow<List<Player>> = _lobbyPlayers.asStateFlow()

    private val _networkEvents = MutableSharedFlow<NetworkEvent>(extraBufferCapacity = 64)
    val networkEvents: SharedFlow<NetworkEvent> = _networkEvents.asSharedFlow()

    private val _isConnecting = MutableStateFlow(false)
    val isConnecting: StateFlow<Boolean> = _isConnecting.asStateFlow()

    private var heartbeatJob: Job? = null

    fun createRoom(
        hostPlayer: Player,
        gameMode: GameMode,
        entryFee: Int = 500
    ): RoomInfo {
        val code = "LUDO-${Random.nextInt(1000, 9999)}"
        val room = RoomInfo(
            roomCode = code,
            hostPlayerId = hostPlayer.id,
            gameMode = gameMode,
            entryCoins = entryFee,
            maxPlayers = if (gameMode == GameMode.TEAM_2V2 || gameMode == GameMode.CLASSIC_4P) 4 else 2,
            isPrivate = true,
            isStarted = false
        )
        _currentRoom.value = room
        _lobbyPlayers.value = listOf(hostPlayer.copy(isHost = true, pingMs = 18))
        startHeartbeat()
        return room
    }

    fun joinRoom(roomCode: String, joiningPlayer: Player, onResult: (Boolean, String) -> Unit) {
        val formattedCode = roomCode.trim().uppercase()
        if (formattedCode.length < 4) {
            onResult(false, "Invalid room code format")
            return
        }

        _isConnecting.value = true
        scope.launch {
            delay(400) // Realistic network handshake
            _isConnecting.value = false

            // Setup or connect to room
            val activeRoom = _currentRoom.value ?: RoomInfo(
                roomCode = formattedCode,
                hostPlayerId = "host_online_99",
                gameMode = GameMode.CLASSIC_4P,
                entryCoins = 500,
                maxPlayers = 4,
                isPrivate = true
            )
            _currentRoom.value = activeRoom

            val existing = _lobbyPlayers.value.toMutableList()
            if (existing.isEmpty()) {
                // Seed mock host if entering external code
                existing.add(
                    Player(
                        id = "host_online_99",
                        name = "ApexRuler",
                        color = PlayerColor.RED,
                        isHost = true,
                        avatarId = "crown_king",
                        pingMs = 32
                    )
                )
            }

            // Assign next free color
            val usedColors = existing.map { it.color }.toSet()
            val availableColor = PlayerColor.entries.firstOrNull { !usedColors.contains(it) } ?: PlayerColor.GREEN

            val newPlayer = joiningPlayer.copy(
                color = availableColor,
                isHost = false,
                pingMs = Random.nextInt(20, 55)
            )

            if (existing.none { it.id == newPlayer.id }) {
                existing.add(newPlayer)
            }
            _lobbyPlayers.value = existing
            _networkEvents.tryEmit(NetworkEvent.PlayerJoined(newPlayer))
            startHeartbeat()
            onResult(true, "Successfully joined room $formattedCode")
        }
    }

    fun addOnlineBotToLobby() {
        val current = _lobbyPlayers.value.toMutableList()
        val room = _currentRoom.value ?: return
        if (current.size >= room.maxPlayers) return

        val usedColors = current.map { it.color }.toSet()
        val nextColor = PlayerColor.entries.firstOrNull { !usedColors.contains(it) } ?: return

        val botNames = listOf("LudoMaster", "Viper", "RoyalPhoenix", "ShadowNinja", "QueenBella")
        val chosenName = botNames[Random.nextInt(botNames.size)]
        val bot = Player(
            id = "bot_${Random.nextInt(1000, 9999)}",
            name = chosenName,
            color = nextColor,
            isBot = true,
            isHost = false,
            avatarId = "wizard",
            pingMs = Random.nextInt(15, 35)
        )
        current.add(bot)
        _lobbyPlayers.value = current
        _networkEvents.tryEmit(NetworkEvent.PlayerJoined(bot))
    }

    fun broadcastDiceRoll(playerId: String, diceValue: Int) {
        val hash = generateAntiCheatProof(playerId, diceValue)
        _networkEvents.tryEmit(NetworkEvent.DiceRolled(playerId, diceValue, hash))
    }

    fun broadcastTokenMove(playerId: String, tokenId: Int, diceValue: Int) {
        _networkEvents.tryEmit(NetworkEvent.TokenMoved(playerId, tokenId, diceValue))
    }

    fun broadcastChatMessage(message: ChatMessage) {
        _networkEvents.tryEmit(NetworkEvent.NewChatMessage(message))
        if (message.isEmoji) {
            _networkEvents.tryEmit(NetworkEvent.FloatingEmoji(message.senderName, message.text))
        }
    }

    fun sendFloatingEmoji(playerId: String, emoji: String) {
        _networkEvents.tryEmit(NetworkEvent.FloatingEmoji(playerId, emoji))
    }

    fun startGame() {
        val room = _currentRoom.value ?: return
        val players = _lobbyPlayers.value
        _currentRoom.value = room.copy(isStarted = true)
        _networkEvents.tryEmit(NetworkEvent.GameStarted(room, players))
    }

    fun leaveRoom() {
        heartbeatJob?.cancel()
        _currentRoom.value = null
        _lobbyPlayers.value = emptyList()
    }

    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = scope.launch(Dispatchers.Default) {
            while (isActive) {
                delay(3000)
                val ping = Random.nextInt(18, 42)
                _networkEvents.tryEmit(NetworkEvent.PingUpdate(ping))
            }
        }
    }

    /**
     * Anti-cheat proof verification using SHA-256 seed hashing
     */
    private fun generateAntiCheatProof(playerId: String, diceValue: Int): String {
        val raw = "$playerId-$diceValue-${System.currentTimeMillis() / 15000}-SECRET_SALT_2026"
        val bytes = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }.take(12)
    }
}
