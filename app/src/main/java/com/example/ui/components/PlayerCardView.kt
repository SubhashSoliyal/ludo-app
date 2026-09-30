package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Player
import com.example.model.PlayerColor
import com.example.ui.theme.DarkNavyBase
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.LudoBlue
import com.example.ui.theme.LudoGreen
import com.example.ui.theme.LudoRed
import com.example.ui.theme.LudoYellow
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

@Composable
fun PlayerCardView(
    player: Player,
    isActiveTurn: Boolean,
    floatingEmoji: String?,
    modifier: Modifier = Modifier
) {
    val playerColor = when (player.color) {
        PlayerColor.RED -> LudoRed
        PlayerColor.GREEN -> LudoGreen
        PlayerColor.YELLOW -> LudoYellow
        PlayerColor.BLUE -> LudoBlue
    }

    Box(modifier = modifier) {
        // Floating emoji bubble when triggered
        if (!floatingEmoji.isNullOrEmpty()) {
            val emojiOffsetY = remember { Animatable(0f) }
            val emojiAlpha = remember { Animatable(1f) }

            LaunchedEffect(floatingEmoji) {
                emojiOffsetY.animateTo(-40f, animationSpec = tween(1200))
                emojiAlpha.animateTo(0f, animationSpec = tween(500))
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = emojiOffsetY.value.dp)
                    .graphicsLayer { alpha = emojiAlpha.value }
                    .background(Color(0xCC000000), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(text = floatingEmoji, fontSize = 24.sp)
            }
        }

        Row(
            modifier = Modifier
                .shadow(if (isActiveTurn) 8.dp else 2.dp, RoundedCornerShape(14.dp), spotColor = if (isActiveTurn) RoyalGold else Color.Transparent)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (isActiveTurn) Brush.horizontalGradient(
                        listOf(DarkSurfaceElevated, playerColor.copy(alpha = 0.25f))
                    ) else Brush.horizontalGradient(
                        listOf(DarkSurfaceElevated, DarkNavyBase)
                    )
                )
                .border(
                    width = if (isActiveTurn) 2.dp else 1.dp,
                    color = if (isActiveTurn) RoyalGold else Color(0x33FFFFFF),
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with colored ring
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(playerColor.copy(alpha = 0.2f))
                    .border(2.dp, playerColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (player.avatarId) {
                        "crown_king" -> "👑"
                        "wizard" -> "🧙"
                        "queen" -> "👸"
                        else -> if (player.isBot) "🤖" else "🤴"
                    },
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Column(modifier = Modifier.width(76.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = player.name,
                        color = TextPrimaryDark,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Home count
                    Text(
                        text = if (player.finishRank > 0) "Rank #${player.finishRank} 🏆" else "${player.finishedTokensCount}/4 Home",
                        color = if (player.finishRank > 0) RoyalGold else TextSecondaryDark,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
