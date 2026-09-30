package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.BoardCoord
import com.example.engine.LudoBoardCoordinates
import com.example.model.LudoToken
import com.example.model.Player
import com.example.model.PlayerColor
import com.example.model.TokenState
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkNavyBase
import com.example.ui.theme.LudoBlue
import com.example.ui.theme.LudoBlueDark
import com.example.ui.theme.LudoGreen
import com.example.ui.theme.LudoGreenDark
import com.example.ui.theme.LudoRed
import com.example.ui.theme.LudoRedDark
import com.example.ui.theme.LudoYellow
import com.example.ui.theme.LudoYellowDark
import com.example.ui.theme.RoyalGold

@Composable
fun LudoBoardView(
    players: List<Player>,
    movableTokenIds: Set<Int>,
    activePlayerColor: PlayerColor?,
    onTokenClick: (LudoToken) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(1f)
            .shadow(16.dp, RoundedCornerShape(16.dp), spotColor = RoyalGold)
            .clip(RoundedCornerShape(16.dp))
            .background(DarkNavyBase)
            .border(3.dp, RoyalGold, RoundedCornerShape(16.dp))
            .testTag("ludo_board")
    ) {
        val boardSizePx = constraints.maxWidth.toFloat()
        val cellSize = maxWidth / 15

        // Canvas draws the grid, base yards, colored arms, and center triangles
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cellPx = boardSizePx / 15f

            // 1. Draw base backgrounds (6x6 cells each)
            // Red Base (Top-Left)
            drawRect(
                color = LudoRed,
                topLeft = Offset(0f, 0f),
                size = Size(cellPx * 6, cellPx * 6)
            )
            // Green Base (Top-Right)
            drawRect(
                color = LudoGreen,
                topLeft = Offset(cellPx * 9, 0f),
                size = Size(cellPx * 6, cellPx * 6)
            )
            // Yellow Base (Bottom-Right)
            drawRect(
                color = LudoYellow,
                topLeft = Offset(cellPx * 9, cellPx * 9),
                size = Size(cellPx * 6, cellPx * 6)
            )
            // Blue Base (Bottom-Left)
            drawRect(
                color = LudoBlue,
                topLeft = Offset(0f, cellPx * 9),
                size = Size(cellPx * 6, cellPx * 6)
            )

            // Inner white yard boxes for bases
            val yardMargin = cellPx * 0.9f
            val yardDim = cellPx * 4.2f
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(yardMargin, yardMargin),
                size = Size(yardDim, yardDim),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cellPx * 0.4f)
            )
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(cellPx * 9 + yardMargin, yardMargin),
                size = Size(yardDim, yardDim),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cellPx * 0.4f)
            )
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(cellPx * 9 + yardMargin, cellPx * 9 + yardMargin),
                size = Size(yardDim, yardDim),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cellPx * 0.4f)
            )
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(yardMargin, cellPx * 9 + yardMargin),
                size = Size(yardDim, yardDim),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cellPx * 0.4f)
            )

            // Inner base token circles
            fun drawBasePockets(color: Color, slots: List<BoardCoord>) {
                slots.forEach { slot ->
                    drawCircle(
                        color = color.copy(alpha = 0.85f),
                        radius = cellPx * 0.38f,
                        center = Offset((slot.col + 0.5f) * cellPx, (slot.row + 0.5f) * cellPx)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = cellPx * 0.4f,
                        center = Offset((slot.col + 0.5f) * cellPx, (slot.row + 0.5f) * cellPx),
                        style = Stroke(width = cellPx * 0.08f)
                    )
                }
            }

            drawBasePockets(LudoRed, LudoBoardCoordinates.BASE_SLOTS[PlayerColor.RED] ?: emptyList())
            drawBasePockets(LudoGreen, LudoBoardCoordinates.BASE_SLOTS[PlayerColor.GREEN] ?: emptyList())
            drawBasePockets(LudoYellow, LudoBoardCoordinates.BASE_SLOTS[PlayerColor.YELLOW] ?: emptyList())
            drawBasePockets(LudoBlue, LudoBoardCoordinates.BASE_SLOTS[PlayerColor.BLUE] ?: emptyList())

            // 2. Center Home Triangles (rows 6..8, cols 6..8)
            val centerLeft = cellPx * 6
            val centerTop = cellPx * 6
            val centerRight = cellPx * 9
            val centerBottom = cellPx * 9
            val midX = cellPx * 7.5f
            val midY = cellPx * 7.5f

            // Red Left Triangle
            val redTriangle = Path().apply {
                moveTo(centerLeft, centerTop)
                lineTo(midX, midY)
                lineTo(centerLeft, centerBottom)
                close()
            }
            drawPath(redTriangle, color = LudoRed)

            // Green Top Triangle
            val greenTriangle = Path().apply {
                moveTo(centerLeft, centerTop)
                lineTo(midX, midY)
                lineTo(centerRight, centerTop)
                close()
            }
            drawPath(greenTriangle, color = LudoGreen)

            // Yellow Right Triangle
            val yellowTriangle = Path().apply {
                moveTo(centerRight, centerTop)
                lineTo(midX, midY)
                lineTo(centerRight, centerBottom)
                close()
            }
            drawPath(yellowTriangle, color = LudoYellow)

            // Blue Bottom Triangle
            val blueTriangle = Path().apply {
                moveTo(centerLeft, centerBottom)
                lineTo(midX, midY)
                lineTo(centerRight, centerBottom)
                close()
            }
            drawPath(blueTriangle, color = LudoBlue)

            // Center Golden Ring
            drawCircle(
                color = RoyalGold,
                radius = cellPx * 0.6f,
                center = Offset(midX, midY)
            )
            drawCircle(
                color = DarkNavyBase,
                radius = cellPx * 0.5f,
                center = Offset(midX, midY)
            )

            // 3. Grid Cells & Home Columns
            for (c in 0 until 15) {
                for (r in 0 until 15) {
                    val inBase = (c < 6 && r < 6) || (c >= 9 && r < 6) || (c < 6 && r >= 9) || (c >= 9 && r >= 9)
                    val inCenter = c in 6..8 && r in 6..8
                    if (!inBase && !inCenter) {
                        val x = c * cellPx
                        val y = r * cellPx

                        // Cell background
                        var cellBg = Color(0xFFF8FAFC)

                        // Colored Home Columns
                        if (r == 7 && c in 1..5) cellBg = LudoRed
                        if (c == 7 && r in 1..5) cellBg = LudoGreen
                        if (r == 7 && c in 9..13) cellBg = LudoYellow
                        if (c == 7 && r in 9..13) cellBg = LudoBlue

                        // Colored Start Tiles
                        if (c == 1 && r == 6) cellBg = LudoRed.copy(alpha = 0.9f)
                        if (c == 8 && r == 1) cellBg = LudoGreen.copy(alpha = 0.9f)
                        if (c == 13 && r == 8) cellBg = LudoYellow.copy(alpha = 0.9f)
                        if (c == 6 && r == 13) cellBg = LudoBlue.copy(alpha = 0.9f)

                        drawRect(
                            color = cellBg,
                            topLeft = Offset(x, y),
                            size = Size(cellPx, cellPx)
                        )
                        // Cell grid border
                        drawRect(
                            color = Color(0xFF94A3B8),
                            topLeft = Offset(x, y),
                            size = Size(cellPx, cellPx),
                            style = Stroke(width = 1f)
                        )
                    }
                }
            }
        }

        // Render Star Safe-Zone Icons on Star Tiles
        LudoBoardCoordinates.SAFE_COMMON_INDICES.forEach { starCommonIdx ->
            val coord = LudoBoardCoordinates.COMMON_PATH[starCommonIdx]
            Box(
                modifier = Modifier
                    .offset(x = cellSize * coord.col, y = cellSize * coord.row)
                    .size(cellSize),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "★",
                    color = Color(0xFFD97706),
                    fontWeight = FontWeight.Black,
                    fontSize = (cellSize.value * 0.55f).sp
                )
            }
        }

        // Center Trophy in Golden Ring
        Box(
            modifier = Modifier
                .offset(x = cellSize * 7, y = cellSize * 7)
                .size(cellSize),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "👑",
                fontSize = (cellSize.value * 0.65f).sp
            )
        }

        // Group tokens by their board coordinate to handle token stacking cleanly
        val allTokens = players.flatMap { p -> p.tokens.map { t -> Pair(p, t) } }
        val tokensByCoord = allTokens.groupBy { (_, token) ->
            LudoBoardCoordinates.getTokenBoardPosition(token)
        }

        tokensByCoord.forEach { (coord, tokenPairs) ->
            Box(
                modifier = Modifier
                    .offset(x = cellSize * coord.col, y = cellSize * coord.row)
                    .size(cellSize),
                contentAlignment = Alignment.Center
            ) {
                // If multiple tokens are stacked on the same spot, render with slight offset and count badge
                tokenPairs.forEachIndexed { index, (player, token) ->
                    val isMovable = movableTokenIds.contains(token.id) && player.color == activePlayerColor
                    val stackOffset = if (tokenPairs.size > 1) (index * 3).dp else 0.dp

                    TokenPawnView(
                        token = token,
                        isMovable = isMovable,
                        tokenSize = cellSize * 0.72f,
                        modifier = Modifier.offset(x = stackOffset, y = stackOffset),
                        onClick = {
                            if (isMovable) {
                                onTokenClick(token)
                            }
                        }
                    )
                }

                if (tokenPairs.size > 1) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(RoyalGold)
                            .border(1.dp, Color.Black, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${tokenPairs.size}",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TokenPawnView(
    token: LudoToken,
    isMovable: Boolean,
    tokenSize: Dp,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bounceAnim = remember { Animatable(1f) }
    LaunchedEffect(isMovable) {
        if (isMovable) {
            bounceAnim.animateTo(
                targetValue = 1.25f,
                animationSpec = infiniteRepeatable(
                    animation = tween(450, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        } else {
            bounceAnim.snapTo(1f)
        }
    }

    val primaryColor = when (token.color) {
        PlayerColor.RED -> LudoRed
        PlayerColor.GREEN -> LudoGreen
        PlayerColor.YELLOW -> LudoYellow
        PlayerColor.BLUE -> LudoBlue
    }

    val darkRim = when (token.color) {
        PlayerColor.RED -> LudoRedDark
        PlayerColor.GREEN -> LudoGreenDark
        PlayerColor.YELLOW -> LudoYellowDark
        PlayerColor.BLUE -> LudoBlueDark
    }

    Box(
        modifier = modifier
            .size(tokenSize)
            .graphicsLayer {
                scaleX = bounceAnim.value
                scaleY = bounceAnim.value
            }
            .shadow(
                elevation = if (isMovable) 10.dp else 4.dp,
                shape = CircleShape,
                spotColor = if (isMovable) RoyalGold else Color.Black
            )
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.8f),
                        primaryColor,
                        darkRim
                    )
                )
            )
            .border(
                width = if (isMovable) 2.5.dp else 1.5.dp,
                color = if (isMovable) RoyalGold else Color.White,
                shape = CircleShape
            )
            .clickable(
                enabled = isMovable,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onClick()
            }
            .testTag("token_${token.color}_${token.id}"),
        contentAlignment = Alignment.Center
    ) {
        // Inner jewel / crown emblem
        Box(
            modifier = Modifier
                .size(tokenSize * 0.4f)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.9f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = when (token.color) {
                    PlayerColor.RED -> "♦"
                    PlayerColor.GREEN -> "♠"
                    PlayerColor.YELLOW -> "★"
                    PlayerColor.BLUE -> "♣"
                },
                color = primaryColor,
                fontSize = (tokenSize.value * 0.3f).sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}
