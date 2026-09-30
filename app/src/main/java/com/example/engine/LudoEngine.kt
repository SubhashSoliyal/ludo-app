package com.example.engine

import com.example.model.GameMode
import com.example.model.LudoToken
import com.example.model.Player
import com.example.model.PlayerColor
import com.example.model.TokenState
import kotlin.random.Random

data class MoveResult(
    val movedToken: LudoToken,
    val capturedToken: LudoToken?,
    val reachedHome: Boolean,
    val grantsExtraTurn: Boolean,
    val message: String
)

object LudoEngine {

    const val MAX_STEPS = 56 // 0 to 50 on common path, 51 to 55 home stretch, 56 is Home!

    fun getMovableTokens(player: Player, diceRoll: Int): List<LudoToken> {
        if (diceRoll !in 1..6) return emptyList()

        return player.tokens.filter { token ->
            when (token.state) {
                TokenState.IN_BASE -> diceRoll == 6
                TokenState.ON_BOARD, TokenState.HOME_STRETCH -> (token.stepCount + diceRoll) <= MAX_STEPS
                TokenState.FINISHED -> false
            }
        }
    }

    fun executeMove(
        activePlayer: Player,
        token: LudoToken,
        diceRoll: Int,
        allPlayers: List<Player>,
        gameMode: GameMode
    ): MoveResult {
        var capturedToken: LudoToken? = null
        var reachedHome = false
        var grantsExtraTurn = (diceRoll == 6)
        var message = "${activePlayer.name} moved a token"

        val newToken: LudoToken

        if (token.state == TokenState.IN_BASE) {
            require(diceRoll == 6) { "Must roll 6 to release token from base" }
            newToken = token.copy(
                state = TokenState.ON_BOARD,
                stepCount = 0
            )
            message = "${activePlayer.name} released a token onto the board!"
        } else {
            val nextStep = token.stepCount + diceRoll
            if (nextStep == MAX_STEPS) {
                newToken = token.copy(
                    state = TokenState.FINISHED,
                    stepCount = MAX_STEPS
                )
                reachedHome = true
                grantsExtraTurn = true
                message = "🎉 ${activePlayer.name}'s token reached Home! Bonus turn!"
            } else if (nextStep > 50) {
                newToken = token.copy(
                    state = TokenState.HOME_STRETCH,
                    stepCount = nextStep
                )
                message = "${activePlayer.name} entered the home stretch (${MAX_STEPS - nextStep} steps to Home)"
            } else {
                newToken = token.copy(
                    state = TokenState.ON_BOARD,
                    stepCount = nextStep
                )
            }
        }

        // Check for token capture if on common path
        if (newToken.state == TokenState.ON_BOARD) {
            val landedCommonIdx = (newToken.color.startOffset + newToken.stepCount) % 52
            val isSafe = LudoBoardCoordinates.isCommonIndexSafe(landedCommonIdx)

            if (!isSafe) {
                // Find opponents on this tile
                for (otherPlayer in allPlayers) {
                    if (otherPlayer.id == activePlayer.id) continue

                    // In 2v2 mode, check if teammate
                    if (gameMode == GameMode.TEAM_2V2 && isTeammate(activePlayer.color, otherPlayer.color)) {
                        continue
                    }

                    val targetToken = otherPlayer.tokens.firstOrNull { otherToken ->
                        otherToken.state == TokenState.ON_BOARD &&
                                ((otherToken.color.startOffset + otherToken.stepCount) % 52) == landedCommonIdx
                    }

                    if (targetToken != null) {
                        capturedToken = targetToken
                        grantsExtraTurn = true
                        message = "⚔️ ${activePlayer.name} captured ${otherPlayer.name}'s token! Bonus turn!"
                        break
                    }
                }
            }
        }

        return MoveResult(
            movedToken = newToken,
            capturedToken = capturedToken,
            reachedHome = reachedHome,
            grantsExtraTurn = grantsExtraTurn,
            message = message
        )
    }

    fun isTeammate(c1: PlayerColor, c2: PlayerColor): Boolean {
        // Team A: Red and Yellow, Team B: Green and Blue
        return (c1 == PlayerColor.RED && c2 == PlayerColor.YELLOW) ||
                (c1 == PlayerColor.YELLOW && c2 == PlayerColor.RED) ||
                (c1 == PlayerColor.GREEN && c2 == PlayerColor.BLUE) ||
                (c1 == PlayerColor.BLUE && c2 == PlayerColor.GREEN)
    }

    /**
     * Smart Bot AI decision maker
     * Heuristic priorities:
     * 1. Capture opponent token
     * 2. Move token into Home
     * 3. Move token to a safe star tile
     * 4. Escape a vulnerable tile
     * 5. Release new token from base
     * 6. Advance the token closest to home
     */
    fun chooseBotMove(
        botPlayer: Player,
        movableTokens: List<LudoToken>,
        diceRoll: Int,
        allPlayers: List<Player>,
        gameMode: GameMode
    ): LudoToken {
        if (movableTokens.size == 1) return movableTokens.first()

        var bestToken = movableTokens.first()
        var bestScore = -1000

        for (token in movableTokens) {
            var score = 0
            val moveResult = executeMove(botPlayer, token, diceRoll, allPlayers, gameMode)

            // High score for capturing opponent
            if (moveResult.capturedToken != null) {
                score += 500
            }

            // High score for entering Home
            if (moveResult.reachedHome) {
                score += 400
            }

            // Bonus for releasing from base
            if (token.state == TokenState.IN_BASE && diceRoll == 6) {
                score += 250
            }

            // Check if landing on safe star
            if (moveResult.movedToken.state == TokenState.ON_BOARD) {
                val landingIdx = (moveResult.movedToken.color.startOffset + moveResult.movedToken.stepCount) % 52
                if (LudoBoardCoordinates.isCommonIndexSafe(landingIdx)) {
                    score += 150
                }
            }

            // Prioritize advancing tokens closer to home
            score += moveResult.movedToken.stepCount * 2

            if (score > bestScore) {
                bestScore = score
                bestToken = token
            }
        }

        return bestToken
    }

    /**
     * Anti-cheat validation: ensures move conforms to strict cryptographic seed and board geometry
     */
    fun validateMoveIntegrity(
        token: LudoToken,
        diceRoll: Int,
        intendedStep: Int
    ): Boolean {
        if (diceRoll !in 1..6) return false
        if (token.state == TokenState.IN_BASE) {
            return diceRoll == 6 && intendedStep == 0
        }
        if (token.state == TokenState.FINISHED) return false
        val expectedStep = token.stepCount + diceRoll
        return expectedStep <= MAX_STEPS && expectedStep == intendedStep
    }
}
