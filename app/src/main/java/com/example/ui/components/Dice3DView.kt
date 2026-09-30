package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkNavyBase
import com.example.ui.theme.LudoRed
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalPurple

@Composable
fun Dice3DView(
    diceValue: Int?,
    isRolling: Boolean,
    isUserTurn: Boolean,
    timeRemainingSeconds: Int,
    onRollClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 72.dp
) {
    val rotationAnim = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(1f) }

    LaunchedEffect(isRolling) {
        if (isRolling) {
            rotationAnim.animateTo(
                targetValue = rotationAnim.value + 720f,
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
            )
        }
    }

    // Glow pulse when it's user turn and waiting for roll
    val pulseAnim = remember { Animatable(1f) }
    LaunchedEffect(isUserTurn, diceValue) {
        if (isUserTurn && diceValue == null && !isRolling) {
            pulseAnim.animateTo(
                targetValue = 1.12f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        } else {
            pulseAnim.snapTo(1f)
        }
    }

    Box(
        modifier = modifier
            .size(size + 24.dp)
            .testTag("dice_roll_container"),
        contentAlignment = Alignment.Center
    ) {
        // Turn timer ring
        val progress = (timeRemainingSeconds / 15f).coerceIn(0f, 1f)
        val timerColor = when {
            timeRemainingSeconds > 7 -> RoyalGold
            timeRemainingSeconds > 3 -> Color(0xFFFB923C)
            else -> LudoRed
        }

        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.size(size + 18.dp),
            color = timerColor,
            trackColor = Color(0x33FFFFFF),
            strokeWidth = 3.5.dp
        )

        // 3D Dice Box
        Box(
            modifier = Modifier
                .size(size)
                .graphicsLayer {
                    rotationZ = rotationAnim.value % 360f
                    scaleX = pulseAnim.value
                    scaleY = pulseAnim.value
                }
                .shadow(
                    elevation = if (isUserTurn && diceValue == null) 12.dp else 4.dp,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = RoyalGold,
                    spotColor = RoyalGold
                )
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFFFFF),
                            Color(0xFFF1F5F9),
                            Color(0xFFE2E8F0)
                        )
                    )
                )
                .border(
                    width = 2.dp,
                    color = if (isUserTurn && diceValue == null) RoyalGold else Color(0xFFCBD5E1),
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable(
                    enabled = isUserTurn && diceValue == null && !isRolling,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    onRollClick()
                }
                .testTag("dice_button"),
            contentAlignment = Alignment.Center
        ) {
            if (isRolling) {
                Text(
                    text = "🎲",
                    fontSize = (size.value * 0.45f).sp
                )
            } else if (diceValue != null) {
                DiceFacePips(value = diceValue, size = size)
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "ROLL",
                        color = Color(0xFF1E293B),
                        fontWeight = FontWeight.Black,
                        fontSize = (size.value * 0.22f).sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "TAP",
                        color = RoyalGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = (size.value * 0.16f).sp
                    )
                }
            }
        }
    }
}

@Composable
fun DiceFacePips(value: Int, size: Dp) {
    val pipSize = (size.value * 0.18f).dp
    val pipColor = if (value == 6) LudoRed else Color(0xFF0F172A)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding((size.value * 0.15f).dp)
    ) {
        when (value) {
            1 -> {
                Pip(modifier = Modifier.align(Alignment.Center), size = pipSize * 1.3f, color = pipColor)
            }
            2 -> {
                Pip(modifier = Modifier.align(Alignment.TopStart), size = pipSize, color = pipColor)
                Pip(modifier = Modifier.align(Alignment.BottomEnd), size = pipSize, color = pipColor)
            }
            3 -> {
                Pip(modifier = Modifier.align(Alignment.TopStart), size = pipSize, color = pipColor)
                Pip(modifier = Modifier.align(Alignment.Center), size = pipSize, color = pipColor)
                Pip(modifier = Modifier.align(Alignment.BottomEnd), size = pipSize, color = pipColor)
            }
            4 -> {
                Pip(modifier = Modifier.align(Alignment.TopStart), size = pipSize, color = pipColor)
                Pip(modifier = Modifier.align(Alignment.TopEnd), size = pipSize, color = pipColor)
                Pip(modifier = Modifier.align(Alignment.BottomStart), size = pipSize, color = pipColor)
                Pip(modifier = Modifier.align(Alignment.BottomEnd), size = pipSize, color = pipColor)
            }
            5 -> {
                Pip(modifier = Modifier.align(Alignment.TopStart), size = pipSize, color = pipColor)
                Pip(modifier = Modifier.align(Alignment.TopEnd), size = pipSize, color = pipColor)
                Pip(modifier = Modifier.align(Alignment.Center), size = pipSize, color = pipColor)
                Pip(modifier = Modifier.align(Alignment.BottomStart), size = pipSize, color = pipColor)
                Pip(modifier = Modifier.align(Alignment.BottomEnd), size = pipSize, color = pipColor)
            }
            6 -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().weight(1f),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Pip(size = pipSize, color = pipColor)
                        Pip(size = pipSize, color = pipColor)
                    }
                    Row(
                        modifier = Modifier.fillMaxSize().weight(1f),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Pip(size = pipSize, color = pipColor)
                        Pip(size = pipSize, color = pipColor)
                    }
                    Row(
                        modifier = Modifier.fillMaxSize().weight(1f),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Pip(size = pipSize, color = pipColor)
                        Pip(size = pipSize, color = pipColor)
                    }
                }
            }
        }
    }
}

@Composable
private fun Pip(modifier: Modifier = Modifier, size: Dp, color: Color) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
            .shadow(1.dp, CircleShape)
    )
}
