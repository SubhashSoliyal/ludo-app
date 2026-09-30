package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.engine.LudoBoardCoordinates
import com.example.engine.LudoEngine
import com.example.model.GameMode
import com.example.model.LudoToken
import com.example.model.Player
import com.example.model.PlayerColor
import com.example.model.TokenState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Ludo Arena", appName)
    }

    @Test
    fun `token in base requires 6 to be movable`() {
        val player = Player(
            id = "p1",
            name = "TestPlayer",
            color = PlayerColor.RED
        )
        // Roll 5 -> No movable tokens
        val movableRoll5 = LudoEngine.getMovableTokens(player, 5)
        assertTrue(movableRoll5.isEmpty())

        // Roll 6 -> All 4 base tokens are eligible to be released
        val movableRoll6 = LudoEngine.getMovableTokens(player, 6)
        assertEquals(4, movableRoll6.size)
    }

    @Test
    fun `token capture sends opponent to base and grants extra turn`() {
        val playerRed = Player(
            id = "red_player",
            name = "Red",
            color = PlayerColor.RED,
            tokens = listOf(LudoToken(id = 0, color = PlayerColor.RED, state = TokenState.ON_BOARD, stepCount = 0))
        )
        // Red start is common index 0. Red moving 1 step lands on common index 1.
        // Place Green token on common index 1 (Green start is 13, so stepCount = 1 + 52 - 13 = 40)
        val greenToken = LudoToken(
            id = 10,
            color = PlayerColor.GREEN,
            state = TokenState.ON_BOARD,
            stepCount = 40 // (13 + 40) % 52 = 1
        )
        val playerGreen = Player(
            id = "green_player",
            name = "Green",
            color = PlayerColor.GREEN,
            tokens = listOf(greenToken)
        )

        val result = LudoEngine.executeMove(
            activePlayer = playerRed,
            token = playerRed.tokens.first(),
            diceRoll = 1,
            allPlayers = listOf(playerRed, playerGreen),
            gameMode = GameMode.CLASSIC_4P
        )

        assertNotNull(result.capturedToken)
        assertEquals(10, result.capturedToken?.id)
        assertTrue(result.grantsExtraTurn)
    }

    @Test
    fun `safe star tiles protect tokens from capture`() {
        // Common index 8 is a safe star
        assertTrue(LudoBoardCoordinates.isCommonIndexSafe(8))
        assertTrue(LudoBoardCoordinates.isCommonIndexSafe(0)) // Red start
        assertTrue(LudoBoardCoordinates.isCommonIndexSafe(13)) // Green start
        assertTrue(LudoBoardCoordinates.isCommonIndexSafe(21)) // Arm 2 star
        assertTrue(LudoBoardCoordinates.isCommonIndexSafe(26)) // Yellow start
        assertTrue(LudoBoardCoordinates.isCommonIndexSafe(34)) // Arm 3 star
        assertTrue(LudoBoardCoordinates.isCommonIndexSafe(39)) // Blue start
        assertTrue(LudoBoardCoordinates.isCommonIndexSafe(47)) // Arm 4 star
    }
}
