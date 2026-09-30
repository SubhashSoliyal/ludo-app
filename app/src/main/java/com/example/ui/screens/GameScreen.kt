package com.example.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Player
import com.example.model.PlayerColor
import com.example.ui.components.Dice3DView
import com.example.ui.components.InGameChatOverlay
import com.example.ui.components.LudoBoardView
import com.example.ui.components.PlayerCardView
import com.example.ui.theme.CleanWhite
import com.example.ui.theme.DarkNavyBase
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.viewmodel.LudoGameViewModel

@Composable
fun GameScreen(
    gameViewModel: LudoGameViewModel,
    onExitGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        gameViewModel.setContext(context)
    }

    val state by gameViewModel.uiState.collectAsState()
    var showChatOverlay by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }

    // Handle back button to confirm exit
    BackHandler {
        showExitDialog = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavyBase)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("game_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Bar: Match mode, Anti-cheat badge, ping, chat toggle, close
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { showExitDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Exit Match",
                        tint = TextSecondaryDark
                    )
                }

                // Anti-Cheat & Latency Chip
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, Color(0x33FFD700), RoundedCornerShape(16.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Anti Cheat",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = state.antiCheatHash.take(8),
                        color = Color(0xFFA7F3D0),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${state.pingMs}ms",
                        color = TextSecondaryDark,
                        fontSize = 10.sp
                    )
                }

                // Chat Toggle Button
                Box {
                    IconButton(
                        onClick = { showChatOverlay = !showChatOverlay },
                        modifier = Modifier.testTag("open_chat_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubble,
                            contentDescription = "Chat",
                            tint = RoyalGold
                        )
                    }
                    if (state.chatMessages.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444))
                        )
                    }
                }
            }

            // 2. Top Player Cards (Red on Left, Green on Right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val redPlayer = state.players.firstOrNull { it.color == PlayerColor.RED }
                val greenPlayer = state.players.firstOrNull { it.color == PlayerColor.GREEN }

                redPlayer?.let { p ->
                    val emoji = state.floatingEmojis.lastOrNull { it.senderName == p.name }?.emoji
                    PlayerCardView(
                        player = p,
                        isActiveTurn = state.activePlayer?.id == p.id,
                        floatingEmoji = emoji
                    )
                }

                greenPlayer?.let { p ->
                    val emoji = state.floatingEmojis.lastOrNull { it.senderName == p.name }?.emoji
                    PlayerCardView(
                        player = p,
                        isActiveTurn = state.activePlayer?.id == p.id,
                        floatingEmoji = emoji
                    )
                }
            }

            // 3. Center Ludo Board (15x15)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                LudoBoardView(
                    players = state.players,
                    movableTokenIds = state.movableTokenIds,
                    activePlayerColor = state.activePlayer?.color,
                    onTokenClick = { token ->
                        gameViewModel.onTokenSelected(token)
                    }
                )
            }

            // 4. Bottom Player Cards (Blue on Left, Yellow on Right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val bluePlayer = state.players.firstOrNull { it.color == PlayerColor.BLUE }
                val yellowPlayer = state.players.firstOrNull { it.color == PlayerColor.YELLOW }

                bluePlayer?.let { p ->
                    val emoji = state.floatingEmojis.lastOrNull { it.senderName == p.name }?.emoji
                    PlayerCardView(
                        player = p,
                        isActiveTurn = state.activePlayer?.id == p.id,
                        floatingEmoji = emoji
                    )
                }

                yellowPlayer?.let { p ->
                    val emoji = state.floatingEmojis.lastOrNull { it.senderName == p.name }?.emoji
                    PlayerCardView(
                        player = p,
                        isActiveTurn = state.activePlayer?.id == p.id,
                        floatingEmoji = emoji
                    )
                }
            }

            // 5. Status Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceElevated)
                    .border(1.dp, Color(0x33FFD700), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.statusBanner,
                    color = RoyalGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }

            // 6. Bottom Dice Control Area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Dice3DView(
                    diceValue = state.diceValue,
                    isRolling = state.isRolling,
                    isUserTurn = state.isUserTurn,
                    timeRemainingSeconds = state.turnSecondsLeft,
                    onRollClick = {
                        gameViewModel.onRollDiceClick()
                    }
                )
            }
        }

        // In-Game Chat Drawer (Slide in from bottom)
        AnimatedVisibility(
            visible = showChatOverlay,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            InGameChatOverlay(
                chatMessages = state.chatMessages,
                onSendMessage = { text, isEmoji ->
                    gameViewModel.sendChatMessage(text, isEmoji)
                },
                onSendEmoji = { emoji ->
                    gameViewModel.triggerFloatingEmoji(emoji)
                },
                onDismiss = { showChatOverlay = false }
            )
        }

        // Exit Match Confirmation Dialog
        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                title = { Text("Leave Match?", color = RoyalGold, fontWeight = FontWeight.Bold) },
                text = { Text("Leaving will forfeit your match and entry coins.", color = TextPrimaryDark) },
                confirmButton = {
                    Button(
                        onClick = {
                            showExitDialog = false
                            onExitGame()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Text("Forfeit & Leave")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExitDialog = false }) {
                        Text("Keep Playing", color = TextSecondaryDark)
                    }
                },
                containerColor = DarkSurfaceCard,
                shape = RoundedCornerShape(16.dp)
            )
        }

        // Victory / Game Over Dialog
        if (state.isGameOver) {
            GameOverVictoryDialog(
                winners = state.winners,
                gameMode = state.gameMode,
                userCaptures = state.userCaptures,
                userSixes = state.userSixes,
                onPlayAgain = {
                    gameViewModel.initializeGame(
                        state.players.map { it.copy(finishRank = 0, consecutiveSixes = 0) },
                        state.gameMode,
                        state.roomInfo
                    )
                },
                onExit = onExitGame
            )
        }
    }
}

@Composable
fun GameOverVictoryDialog(
    winners: List<Player>,
    gameMode: com.example.model.GameMode,
    userCaptures: Int,
    userSixes: Int,
    onPlayAgain: () -> Unit,
    onExit: () -> Unit
) {
    val firstWinner = winners.firstOrNull()
    val isUserWinner = firstWinner != null && !firstWinner.isBot

    AlertDialog(
        onDismissRequest = {},
        confirmButton = {
            Button(
                onClick = onPlayAgain,
                colors = ButtonDefaults.buttonColors(containerColor = RoyalGold, contentColor = DarkNavyBase),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("play_again_button")
            ) {
                Text("Play Again", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onExit) {
                Text("Return Home", color = TextSecondaryDark)
            }
        },
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isUserWinner) "🏆 VICTORY!" else "GAME OVER",
                    color = if (isUserWinner) RoyalGold else CleanWhite,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp
                )
                Text(
                    text = if (isUserWinner) "You won the match!" else "${firstWinner?.name ?: "Opponent"} took 1st Place!",
                    color = TextSecondaryDark,
                    fontSize = 13.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isUserWinner) Color(0xFF065F46) else DarkSurfaceElevated)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isUserWinner) "+1,500 COINS WON! 💰" else "+250 XP Gained",
                        color = if (isUserWinner) RoyalGold else CleanWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "⚔️ Captures", color = TextSecondaryDark, fontSize = 11.sp)
                        Text(text = "$userCaptures", color = CleanWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🎲 Sixes Rolled", color = TextSecondaryDark, fontSize = 11.sp)
                        Text(text = "$userSixes", color = CleanWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🛡️ Fair Play", color = TextSecondaryDark, fontSize = 11.sp)
                        Text(text = "Verified", color = Color(0xFF34D399), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        },
        containerColor = DarkSurfaceCard,
        shape = RoundedCornerShape(20.dp)
    )
}
