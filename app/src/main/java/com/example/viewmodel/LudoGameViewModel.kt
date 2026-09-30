package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.LudoRepository
import com.example.data.MatchRecordEntity
import com.example.engine.LudoEngine
import com.example.model.ChatMessage
import com.example.model.GameMode
import com.example.model.LudoToken
import com.example.model.Player
import com.example.model.PlayerColor
import com.example.model.RoomInfo
import com.example.model.TokenState
import com.example.network.MultiplayerSyncManager
import com.example.network.NetworkEvent
import com.example.notification.NotificationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

data class FloatingEmojiData(
    val id: Long,
    val senderName: String,
    val emoji: String
)

data class GameUiState(
    val players: List<Player> = emptyList(),
    val currentTurnIndex: Int = 0,
    val diceValue: Int? = null,
    val isRolling: Boolean = false,
    val turnSecondsLeft: Int = 15,
    val movableTokenIds: Set<Int> = emptySet(),
    val gameMode: GameMode = GameMode.CLASSIC_4P,
    val roomInfo: RoomInfo? = null,
    val isGameOver: Boolean = false,
    val winners: List<Player> = emptyList(),
    val statusBanner: String = "Tap the dice to roll!",
    val chatMessages: List<ChatMessage> = emptyList(),
    val floatingEmojis: List<FloatingEmojiData> = emptyList(),
    val antiCheatHash: String = "SECURE_SHA256_VALID",
    val pingMs: Int = 24,
    val consecutiveSixes: Int = 0,
    val matchStartTime: Long = System.currentTimeMillis(),
    val userCaptures: Int = 0,
    val userSixes: Int = 0
) {
    val activePlayer: Player? get() = players.getOrNull(currentTurnIndex)
    val isUserTurn: Boolean get() = activePlayer?.isBot == false
}

class LudoGameViewModel(
    private val repository: LudoRepository,
    private val syncManager: MultiplayerSyncManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var turnTimerJob: Job? = null
    private var botMoveJob: Job? = null
    private var contextRef: Context? = null

    init {
        // Listen to multiplayer network events
        viewModelScope.launch {
            syncManager.networkEvents.collect { event ->
                handleNetworkEvent(event)
            }
        }
    }

    fun setContext(context: Context) {
        this.contextRef = context.applicationContext
    }

    fun initializeGame(
        players: List<Player>,
        mode: GameMode,
        room: RoomInfo?
    ) {
        val initialPlayers = if (players.isNotEmpty()) {
            players
        } else {
            createDefaultPlayers(mode)
        }

        _uiState.value = GameUiState(
            players = initialPlayers,
            currentTurnIndex = 0,
            gameMode = mode,
            roomInfo = room,
            statusBanner = "${initialPlayers[0].name}'s turn to roll!",
            matchStartTime = System.currentTimeMillis()
        )

        startTurnTimer()
        checkBotTurn()
    }

    private fun createDefaultPlayers(mode: GameMode): List<Player> {
        return when (mode) {
            GameMode.CLASSIC_4P -> listOf(
                Player("user", "You", PlayerColor.RED, isBot = false, isHost = true),
                Player("bot_1", "GreenBot", PlayerColor.GREEN, isBot = true),
                Player("bot_2", "YellowBot", PlayerColor.YELLOW, isBot = true),
                Player("bot_3", "BlueBot", PlayerColor.BLUE, isBot = true)
            )
            GameMode.TEAM_2V2 -> listOf(
                Player("user", "You (Red)", PlayerColor.RED, isBot = false, isHost = true),
                Player("bot_1", "GreenBot", PlayerColor.GREEN, isBot = true),
                Player("bot_2", "AllyBot (Yellow)", PlayerColor.YELLOW, isBot = true),
                Player("bot_3", "BlueBot", PlayerColor.BLUE, isBot = true)
            )
            GameMode.QUICK_LUDO -> listOf(
                Player("user", "You", PlayerColor.RED, isBot = false, isHost = true),
                Player("bot_1", "SpeedBot", PlayerColor.GREEN, isBot = true)
            )
            GameMode.PASS_AND_PLAY -> listOf(
                Player("p1", "Player 1", PlayerColor.RED, isBot = false),
                Player("p2", "Player 2", PlayerColor.GREEN, isBot = false),
                Player("p3", "Player 3", PlayerColor.YELLOW, isBot = false),
                Player("p4", "Player 4", PlayerColor.BLUE, isBot = false)
            )
            GameMode.PRACTICE_BOTS -> listOf(
                Player("user", "You", PlayerColor.RED, isBot = false, isHost = true),
                Player("bot_expert", "LudoMaster", PlayerColor.GREEN, isBot = true)
            )
        }
    }

    fun onRollDiceClick() {
        val state = _uiState.value
        if (state.isRolling || state.diceValue != null || state.isGameOver) return
        val current = state.activePlayer ?: return

        executeDiceRoll(current)
    }

    private fun executeDiceRoll(player: Player) {
        _uiState.update { it.copy(isRolling = true) }

        viewModelScope.launch {
            // Dice tumbling animation duration
            delay(500)
            val roll = Random.nextInt(1, 7)
            val hash = "AC-" + Random.nextInt(100000, 999999).toString(16).uppercase()

            syncManager.broadcastDiceRoll(player.id, roll)

            // Anti-cheat consecutive 6s rule
            val consecutive = if (roll == 6) player.consecutiveSixes + 1 else 0
            val updatedPlayer = player.copy(consecutiveSixes = consecutive)

            val updatedPlayers = _uiState.value.players.map {
                if (it.id == player.id) updatedPlayer else it
            }

            var banner = "${player.name} rolled a $roll!"
            val isUser = !player.isBot && player.id == "user"
            val newSixes = if (isUser && roll == 6) _uiState.value.userSixes + 1 else _uiState.value.userSixes

            if (consecutive >= 3) {
                // Forfeit turn on 3 consecutive 6s
                banner = "⚠️ 3 Sixes in a row! Turn forfeited!"
                _uiState.update {
                    it.copy(
                        isRolling = false,
                        diceValue = roll,
                        players = updatedPlayers,
                        statusBanner = banner,
                        antiCheatHash = hash,
                        userSixes = newSixes
                    )
                }
                delay(1200)
                advanceToNextPlayer()
                return@launch
            }

            // Calculate movable tokens
            val movable = LudoEngine.getMovableTokens(updatedPlayer, roll)

            _uiState.update {
                it.copy(
                    isRolling = false,
                    diceValue = roll,
                    players = updatedPlayers,
                    movableTokenIds = movable.map { t -> t.id }.toSet(),
                    statusBanner = if (movable.isEmpty()) "$banner (No moves available)" else banner,
                    antiCheatHash = hash,
                    userSixes = newSixes
                )
            }

            if (movable.isEmpty()) {
                delay(1000)
                advanceToNextPlayer()
            } else if (player.isBot) {
                delay(600)
                val chosen = LudoEngine.chooseBotMove(
                    player,
                    movable,
                    roll,
                    _uiState.value.players,
                    _uiState.value.gameMode
                )
                onTokenSelected(chosen)
            } else if (movable.size == 1 && movable.first().state != TokenState.IN_BASE) {
                // Convenient auto-advance for single movable token on board
                delay(400)
                onTokenSelected(movable.first())
            }
        }
    }

    fun onTokenSelected(token: LudoToken) {
        val state = _uiState.value
        val dice = state.diceValue ?: return
        val current = state.activePlayer ?: return
        if (state.isGameOver) return

        if (!state.movableTokenIds.contains(token.id)) return

        viewModelScope.launch {
            val moveResult = LudoEngine.executeMove(
                activePlayer = current,
                token = token,
                diceRoll = dice,
                allPlayers = state.players,
                gameMode = state.gameMode
            )

            syncManager.broadcastTokenMove(current.id, token.id, dice)

            var newCaptures = state.userCaptures
            if (!current.isBot && current.id == "user" && moveResult.capturedToken != null) {
                newCaptures++
            }

            // Update active player's tokens
            val updatedTokens = current.tokens.map {
                if (it.id == token.id) moveResult.movedToken else it
            }

            // If captured opponent, send their token to base
            var allUpdatedPlayers = state.players.map { p ->
                if (p.id == current.id) {
                    p.copy(tokens = updatedTokens)
                } else if (moveResult.capturedToken != null && moveResult.capturedToken.color == p.color) {
                    val pTokens = p.tokens.map { ot ->
                        if (ot.id == moveResult.capturedToken.id) {
                            ot.copy(state = TokenState.IN_BASE, stepCount = -1)
                        } else ot
                    }
                    p.copy(tokens = pTokens)
                } else {
                    p
                }
            }

            // Check if player won
            val checkPlayer = allUpdatedPlayers.first { it.id == current.id }
            val targetHomeCount = if (state.gameMode == GameMode.QUICK_LUDO) 2 else 4
            var newWinners = state.winners

            if (checkPlayer.finishedTokensCount >= targetHomeCount && checkPlayer.finishRank == 0) {
                val nextRank = newWinners.size + 1
                allUpdatedPlayers = allUpdatedPlayers.map {
                    if (it.id == checkPlayer.id) it.copy(finishRank = nextRank) else it
                }
                newWinners = newWinners + checkPlayer.copy(finishRank = nextRank)
            }

            val isOver = checkGameOverCondition(allUpdatedPlayers, state.gameMode, newWinners)

            _uiState.update {
                it.copy(
                    players = allUpdatedPlayers,
                    diceValue = null,
                    movableTokenIds = emptySet(),
                    statusBanner = moveResult.message,
                    winners = newWinners,
                    isGameOver = isOver,
                    userCaptures = newCaptures
                )
            }

            if (isOver) {
                onGameOver(newWinners)
            } else if (moveResult.grantsExtraTurn) {
                // Extra turn!
                startTurnTimer()
                checkBotTurn()
            } else {
                delay(400)
                advanceToNextPlayer()
            }
        }
    }

    private fun checkGameOverCondition(
        players: List<Player>,
        mode: GameMode,
        winners: List<Player>
    ): Boolean {
        if (mode == GameMode.TEAM_2V2) {
            val teamAHome = players.filter { it.color == PlayerColor.RED || it.color == PlayerColor.YELLOW }
                .sumOf { it.finishedTokensCount }
            val teamBHome = players.filter { it.color == PlayerColor.GREEN || it.color == PlayerColor.BLUE }
                .sumOf { it.finishedTokensCount }
            return teamAHome >= 4 || teamBHome >= 4
        }
        val remainingActive = players.count { it.finishRank == 0 }
        return remainingActive <= 1 || winners.isNotEmpty()
    }

    private fun advanceToNextPlayer() {
        val state = _uiState.value
        if (state.isGameOver) return

        var nextIndex = (state.currentTurnIndex + 1) % state.players.size
        // Skip players that have already finished
        var attempts = 0
        while (state.players[nextIndex].finishRank > 0 && attempts < state.players.size) {
            nextIndex = (nextIndex + 1) % state.players.size
            attempts++
        }

        val nextPlayer = state.players[nextIndex]
        _uiState.update {
            it.copy(
                currentTurnIndex = nextIndex,
                diceValue = null,
                movableTokenIds = emptySet(),
                statusBanner = "${nextPlayer.name}'s turn to roll!",
                consecutiveSixes = 0
            )
        }

        // Push notification if it's the local user's turn in private room
        if (!nextPlayer.isBot && state.roomInfo != null) {
            contextRef?.let { ctx ->
                NotificationHelper.showTurnAlertNotification(ctx, nextPlayer.name, state.roomInfo.roomCode)
            }
        }

        startTurnTimer()
        checkBotTurn()
    }

    private fun startTurnTimer() {
        turnTimerJob?.cancel()
        _uiState.update { it.copy(turnSecondsLeft = 15) }

        turnTimerJob = viewModelScope.launch {
            for (sec in 14 downTo 0) {
                delay(1000)
                _uiState.update { it.copy(turnSecondsLeft = sec) }
            }
            // Turn timed out
            handleTurnTimeout()
        }
    }

    private fun handleTurnTimeout() {
        val state = _uiState.value
        if (state.isGameOver) return
        val current = state.activePlayer ?: return

        if (state.diceValue == null) {
            // Auto roll or skip
            _uiState.update { it.copy(statusBanner = "⏰ ${current.name} ran out of time! Auto-skipping.") }
            advanceToNextPlayer()
        } else {
            // Player rolled but didn't pick token, auto-pick first movable token
            val movable = state.players.first { it.id == current.id }.tokens.filter {
                state.movableTokenIds.contains(it.id)
            }
            if (movable.isNotEmpty()) {
                onTokenSelected(movable.first())
            } else {
                advanceToNextPlayer()
            }
        }
    }

    private fun checkBotTurn() {
        botMoveJob?.cancel()
        val current = _uiState.value.activePlayer ?: return
        if (current.isBot && !_uiState.value.isGameOver) {
            botMoveJob = viewModelScope.launch {
                delay(700)
                executeDiceRoll(current)
            }
        }
    }

    private fun onGameOver(winners: List<Player>) {
        turnTimerJob?.cancel()
        botMoveJob?.cancel()

        val state = _uiState.value
        val userPlayer = state.players.firstOrNull { !it.isBot }
        val userRank = userPlayer?.finishRank ?: if (winners.any { it.id == userPlayer?.id }) 1 else 2
        val isFirst = userRank == 1
        val coinsWon = if (isFirst) 1500 else -250

        val duration = ((System.currentTimeMillis() - state.matchStartTime) / 1000).toInt()

        viewModelScope.launch {
            repository.recordMatch(
                MatchRecordEntity(
                    mode = state.gameMode.title,
                    finishRank = userRank,
                    coinsDelta = coinsWon,
                    capturesMade = state.userCaptures,
                    sixesRolled = state.userSixes,
                    durationSeconds = duration,
                    opponentsSummary = state.players.filter { it.id != userPlayer?.id }.joinToString(", ") { it.name }
                )
            )
            repository.addCoins(coinsWon)
        }
    }

    fun sendChatMessage(text: String, isEmoji: Boolean = false) {
        val active = _uiState.value.players.firstOrNull { !it.isBot } ?: return
        val message = ChatMessage(
            id = "msg_${System.currentTimeMillis()}",
            senderName = active.name,
            senderColor = active.color,
            text = text,
            isEmoji = isEmoji
        )
        syncManager.broadcastChatMessage(message)
    }

    fun triggerFloatingEmoji(emoji: String) {
        val active = _uiState.value.players.firstOrNull { !it.isBot } ?: return
        syncManager.sendFloatingEmoji(active.name, emoji)
    }

    private fun handleNetworkEvent(event: NetworkEvent) {
        when (event) {
            is NetworkEvent.NewChatMessage -> {
                _uiState.update { it.copy(chatMessages = it.chatMessages + event.message) }
            }
            is NetworkEvent.FloatingEmoji -> {
                val newEmoji = FloatingEmojiData(System.currentTimeMillis(), event.playerId, event.emoji)
                _uiState.update { it.copy(floatingEmojis = it.floatingEmojis + newEmoji) }
                viewModelScope.launch {
                    delay(2500)
                    _uiState.update {
                        it.copy(floatingEmojis = it.floatingEmojis.filter { e -> e.id != newEmoji.id })
                    }
                }
            }
            is NetworkEvent.PingUpdate -> {
                _uiState.update { it.copy(pingMs = event.pingMs) }
            }
            else -> {}
        }
    }

    override fun onCleared() {
        super.onCleared()
        turnTimerJob?.cancel()
        botMoveJob?.cancel()
    }
}
