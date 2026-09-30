package com.example.model

enum class PlayerColor(val hex: Long, val displayName: String, val startOffset: Int) {
    RED(0xFFEF4444, "Red", 0),
    GREEN(0xFF10B981, "Green", 13),
    YELLOW(0xFFFBBF24, "Yellow", 26),
    BLUE(0xFF0EA5E9, "Blue", 39)
}

enum class GameMode(val title: String, val description: String) {
    CLASSIC_4P("Classic 4-Player", "Free-for-all race to get all 4 tokens home"),
    TEAM_2V2("Team Battle 2v2", "Red & Yellow vs Green & Blue coop battle"),
    QUICK_LUDO("Quick Ludo", "Fast 2-token match with instant action"),
    PASS_AND_PLAY("Pass & Play", "Local offline multiplayer on one screen"),
    PRACTICE_BOTS("Practice vs Bots", "Sharpen skills against smart AI")
}

enum class TokenState {
    IN_BASE,
    ON_BOARD,
    HOME_STRETCH,
    FINISHED
}

data class LudoToken(
    val id: Int,
    val color: PlayerColor,
    val state: TokenState = TokenState.IN_BASE,
    val stepCount: Int = -1 // -1 = in base; 0..50 = on common track; 51..55 = home stretch; 56 = finished
) {
    val isHome: Boolean get() = state == TokenState.FINISHED
    val isMovableFromBase: Boolean get() = state == TokenState.IN_BASE
}

data class Player(
    val id: String,
    val name: String,
    val color: PlayerColor,
    val isBot: Boolean = false,
    val isHost: Boolean = false,
    val avatarId: String = "king",
    val tokens: List<LudoToken> = listOf(
        LudoToken(0, color),
        LudoToken(1, color),
        LudoToken(2, color),
        LudoToken(3, color)
    ),
    val finishRank: Int = 0, // 0 = playing, 1 = 1st, 2 = 2nd, etc.
    val consecutiveSixes: Int = 0,
    val pingMs: Int = 24
) {
    val finishedTokensCount: Int get() = tokens.count { it.state == TokenState.FINISHED }
    val hasWon: Boolean get() = finishRank > 0
}

data class ChatMessage(
    val id: String,
    val senderName: String,
    val senderColor: PlayerColor,
    val text: String,
    val isEmoji: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class RoomInfo(
    val roomCode: String,
    val hostPlayerId: String,
    val gameMode: GameMode,
    val entryCoins: Int = 500,
    val maxPlayers: Int = 4,
    val isPrivate: Boolean = true,
    val isStarted: Boolean = false,
    val webInviteUrl: String = "https://ludoarena.app/join/$roomCode"
)

data class TournamentMatch(
    val matchId: String,
    val roundName: String, // Quarterfinals, Semifinals, Finals
    val player1Name: String,
    val player2Name: String,
    val winnerName: String? = null,
    val isUserMatch: Boolean = false,
    val isCompleted: Boolean = false
)

data class TournamentBracket(
    val id: String,
    val title: String,
    val prizePool: Int,
    val currentRound: Int,
    val matches: List<TournamentMatch>
)
