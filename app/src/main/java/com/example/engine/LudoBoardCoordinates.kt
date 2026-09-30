package com.example.engine

import com.example.model.PlayerColor
import com.example.model.LudoToken
import com.example.model.TokenState

data class BoardCoord(val col: Int, val row: Int)

object LudoBoardCoordinates {
    // 52 Common Track tiles mapped to (col, row) on a 15x15 board
    val COMMON_PATH = listOf(
        BoardCoord(1, 6),   // 0: Red Start (Safe)
        BoardCoord(2, 6),   // 1
        BoardCoord(3, 6),   // 2
        BoardCoord(4, 6),   // 3
        BoardCoord(5, 6),   // 4
        BoardCoord(6, 5),   // 5
        BoardCoord(6, 4),   // 6
        BoardCoord(6, 3),   // 7
        BoardCoord(6, 2),   // 8: Safe Star
        BoardCoord(6, 1),   // 9
        BoardCoord(6, 0),   // 10
        BoardCoord(7, 0),   // 11
        BoardCoord(8, 0),   // 12
        BoardCoord(8, 1),   // 13: Green Start (Safe)
        BoardCoord(8, 2),   // 14
        BoardCoord(8, 3),   // 15
        BoardCoord(8, 4),   // 16
        BoardCoord(8, 5),   // 17
        BoardCoord(9, 6),   // 18
        BoardCoord(10, 6),  // 19
        BoardCoord(11, 6),  // 20
        BoardCoord(12, 6),  // 21: Safe Star
        BoardCoord(13, 6),  // 22
        BoardCoord(14, 6),  // 23
        BoardCoord(14, 7),  // 24
        BoardCoord(14, 8),  // 25
        BoardCoord(13, 8),  // 26: Yellow Start (Safe)
        BoardCoord(12, 8),  // 27
        BoardCoord(11, 8),  // 28
        BoardCoord(10, 8),  // 29
        BoardCoord(9, 8),   // 30
        BoardCoord(8, 9),   // 31
        BoardCoord(8, 10),  // 32
        BoardCoord(8, 11),  // 33
        BoardCoord(8, 12),  // 34: Safe Star
        BoardCoord(8, 13),  // 35
        BoardCoord(8, 14),  // 36
        BoardCoord(7, 14),  // 37
        BoardCoord(6, 14),  // 38
        BoardCoord(6, 13),  // 39: Blue Start (Safe)
        BoardCoord(6, 12),  // 40
        BoardCoord(6, 11),  // 41
        BoardCoord(6, 10),  // 42
        BoardCoord(6, 9),   // 43
        BoardCoord(5, 8),   // 44
        BoardCoord(4, 8),   // 45
        BoardCoord(3, 8),   // 46
        BoardCoord(2, 8),   // 47: Safe Star
        BoardCoord(1, 8),   // 48
        BoardCoord(0, 8),   // 49
        BoardCoord(0, 7),   // 50
        BoardCoord(0, 6)    // 51
    )

    val SAFE_COMMON_INDICES = setOf(0, 8, 13, 21, 26, 34, 39, 47)

    // Private Home Column for each color (steps 51 to 55)
    val RED_HOME_PATH = listOf(
        BoardCoord(1, 7),
        BoardCoord(2, 7),
        BoardCoord(3, 7),
        BoardCoord(4, 7),
        BoardCoord(5, 7)
    )

    val GREEN_HOME_PATH = listOf(
        BoardCoord(7, 1),
        BoardCoord(7, 2),
        BoardCoord(7, 3),
        BoardCoord(7, 4),
        BoardCoord(7, 5)
    )

    val YELLOW_HOME_PATH = listOf(
        BoardCoord(13, 7),
        BoardCoord(12, 7),
        BoardCoord(11, 7),
        BoardCoord(10, 7),
        BoardCoord(9, 7)
    )

    val BLUE_HOME_PATH = listOf(
        BoardCoord(7, 13),
        BoardCoord(7, 12),
        BoardCoord(7, 11),
        BoardCoord(7, 10),
        BoardCoord(7, 9)
    )

    // Base Yard token resting slots (col, row)
    val BASE_SLOTS = mapOf(
        PlayerColor.RED to listOf(
            BoardCoord(2, 2), BoardCoord(3, 2),
            BoardCoord(2, 3), BoardCoord(3, 3)
        ),
        PlayerColor.GREEN to listOf(
            BoardCoord(11, 2), BoardCoord(12, 2),
            BoardCoord(11, 3), BoardCoord(12, 3)
        ),
        PlayerColor.YELLOW to listOf(
            BoardCoord(11, 11), BoardCoord(12, 11),
            BoardCoord(11, 12), BoardCoord(12, 12)
        ),
        PlayerColor.BLUE to listOf(
            BoardCoord(2, 11), BoardCoord(3, 11),
            BoardCoord(2, 12), BoardCoord(3, 12)
        )
    )

    // Center Home Apex Coordinates for each color (step 56)
    val HOME_CENTER = mapOf(
        PlayerColor.RED to BoardCoord(6, 7),
        PlayerColor.GREEN to BoardCoord(7, 6),
        PlayerColor.YELLOW to BoardCoord(8, 7),
        PlayerColor.BLUE to BoardCoord(7, 8)
    )

    fun getTokenBoardPosition(token: LudoToken): BoardCoord {
        return when (token.state) {
            TokenState.IN_BASE -> {
                BASE_SLOTS[token.color]?.getOrNull(token.id % 4) ?: BoardCoord(0, 0)
            }
            TokenState.ON_BOARD -> {
                val commonIdx = (token.color.startOffset + token.stepCount) % 52
                COMMON_PATH[commonIdx]
            }
            TokenState.HOME_STRETCH -> {
                val stretchIdx = (token.stepCount - 51).coerceIn(0, 4)
                when (token.color) {
                    PlayerColor.RED -> RED_HOME_PATH[stretchIdx]
                    PlayerColor.GREEN -> GREEN_HOME_PATH[stretchIdx]
                    PlayerColor.YELLOW -> YELLOW_HOME_PATH[stretchIdx]
                    PlayerColor.BLUE -> BLUE_HOME_PATH[stretchIdx]
                }
            }
            TokenState.FINISHED -> {
                HOME_CENTER[token.color] ?: BoardCoord(7, 7)
            }
        }
    }

    fun isCommonIndexSafe(index: Int): Boolean = SAFE_COMMON_INDICES.contains(index)
}
