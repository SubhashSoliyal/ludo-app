package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PlayerProfileEntity
import com.example.model.GameMode
import com.example.model.Player
import com.example.model.PlayerColor
import com.example.ui.theme.CleanWhite
import com.example.ui.theme.DarkNavyBase
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.LudoBlue
import com.example.ui.theme.LudoGreen
import com.example.ui.theme.LudoRed
import com.example.ui.theme.LudoYellow
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalPurple
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.viewmodel.MainViewModel

@Composable
fun HomeScreen(
    mainViewModel: MainViewModel,
    profile: PlayerProfileEntity?,
    onCreatePrivateRoom: (GameMode, Int) -> Unit,
    onJoinRoom: (String) -> Unit,
    onQuickStart: (GameMode) -> Unit,
    onNavigateTournament: () -> Unit,
    onNavigateStore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showJoinDialog by remember { mutableStateOf(false) }
    var joinRoomCodeInput by remember { mutableStateOf("") }
    var showCreateRoomDialog by remember { mutableStateOf(false) }
    var selectedCreateMode by remember { mutableStateOf(GameMode.CLASSIC_4P) }
    var selectedEntryFee by remember { mutableStateOf(500) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavyBase)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Profile & Currency Top Bar
            HeaderProfileBar(
                profile = profile,
                onAddCoinsClick = onNavigateStore
            )
        }

        // Season Banner
        item {
            TournamentSeasonBanner(
                onJoinTournamentClick = onNavigateTournament
            )
        }

        // Multiplayer Action Buttons: Create Room & Join Room
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionTile(
                    title = "Create Room",
                    subtitle = "Host private match",
                    icon = Icons.Default.Add,
                    gradientColors = listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9)),
                    modifier = Modifier.weight(1f),
                    testTag = "create_room_card",
                    onClick = { showCreateRoomDialog = true }
                )

                ActionTile(
                    title = "Join Room",
                    subtitle = "Enter room code",
                    icon = Icons.Default.Login,
                    gradientColors = listOf(Color(0xFFEC4899), Color(0xFFBE185D)),
                    modifier = Modifier.weight(1f),
                    testTag = "join_room_card",
                    onClick = { showJoinDialog = true }
                )
            }
        }

        item {
            Text(
                text = "🎮 Game Modes",
                color = RoyalGold,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Game Modes Grid / List
        item {
            GameModeCard(
                title = "Team Battle 2v2",
                badge = "CO-OP LUDO",
                description = "Partner with an ally! Shared victory, no friendly captures.",
                icon = Icons.Default.Groups,
                accentColor = LudoBlue,
                testTag = "mode_2v2",
                onClick = { onQuickStart(GameMode.TEAM_2V2) }
            )
        }

        item {
            GameModeCard(
                title = "Classic 4-Player FFA",
                badge = "POPULAR",
                description = "Traditional 4-player competitive race to the finish.",
                icon = Icons.Default.Group,
                accentColor = LudoGreen,
                testTag = "mode_classic_4p",
                onClick = { onQuickStart(GameMode.CLASSIC_4P) }
            )
        }

        item {
            GameModeCard(
                title = "Quick Ludo",
                badge = "5 MINS",
                description = "Fast action! Race with 2 tokens straight into battle.",
                icon = Icons.Default.Speed,
                accentColor = LudoYellow,
                testTag = "mode_quick",
                onClick = { onQuickStart(GameMode.QUICK_LUDO) }
            )
        }

        item {
            GameModeCard(
                title = "Pass & Play (Offline)",
                badge = "LOCAL MULTIPLAYER",
                description = "Play with friends and family on a single device.",
                icon = Icons.Default.PlayArrow,
                accentColor = RoyalPurple,
                testTag = "mode_pass_play",
                onClick = { onQuickStart(GameMode.PASS_AND_PLAY) }
            )
        }

        item {
            GameModeCard(
                title = "Practice vs AI",
                badge = "OFFLINE",
                description = "Hone your tactical moves against smart computer bots.",
                icon = Icons.Default.Star,
                accentColor = LudoRed,
                testTag = "mode_practice",
                onClick = { onQuickStart(GameMode.PRACTICE_BOTS) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Join Room Dialog
    if (showJoinDialog) {
        AlertDialog(
            onDismissRequest = { showJoinDialog = false },
            title = {
                Text(
                    text = "🔑 Join Private Room",
                    color = RoyalGold,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter the 6-character room code shared by your friend:",
                        color = TextPrimaryDark,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = joinRoomCodeInput,
                        onValueChange = { joinRoomCodeInput = it.uppercase() },
                        placeholder = { Text("e.g. LUDO-4821", color = TextSecondaryDark) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("room_code_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark,
                            focusedBorderColor = RoyalGold,
                            unfocusedBorderColor = Color(0x66FFFFFF),
                            focusedContainerColor = DarkSurfaceElevated,
                            unfocusedContainerColor = DarkSurfaceElevated
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (joinRoomCodeInput.isNotBlank()) {
                            onJoinRoom(joinRoomCodeInput.trim())
                            showJoinDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalGold, contentColor = DarkNavyBase),
                    modifier = Modifier.testTag("join_room_confirm_button")
                ) {
                    Text("Join Match", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showJoinDialog = false }) {
                    Text("Cancel", color = TextSecondaryDark)
                }
            },
            containerColor = DarkSurfaceCard,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Create Room Dialog
    if (showCreateRoomDialog) {
        AlertDialog(
            onDismissRequest = { showCreateRoomDialog = false },
            title = {
                Text(
                    text = "🏰 Create Private Room",
                    color = RoyalGold,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Choose Game Mode:",
                        color = TextPrimaryDark,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(GameMode.CLASSIC_4P, GameMode.TEAM_2V2).forEach { mode ->
                            val isSelected = selectedCreateMode == mode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) RoyalPurple else DarkSurfaceElevated)
                                    .border(
                                        1.dp,
                                        if (isSelected) RoyalGold else Color(0x33FFFFFF),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedCreateMode = mode }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (mode == GameMode.CLASSIC_4P) "4-Player FFA" else "2v2 Team",
                                    color = if (isSelected) CleanWhite else TextSecondaryDark,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Entry Fee:",
                        color = TextPrimaryDark,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(500, 2000, 10000).forEach { fee ->
                            val isSelected = selectedEntryFee == fee
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) RoyalGold else DarkSurfaceElevated)
                                    .clickable { selectedEntryFee = fee }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "💰 $fee",
                                    color = if (isSelected) DarkNavyBase else TextPrimaryDark,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCreatePrivateRoom(selectedCreateMode, selectedEntryFee)
                        showCreateRoomDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalGold, contentColor = DarkNavyBase),
                    modifier = Modifier.testTag("create_room_confirm_button")
                ) {
                    Text("Create Room", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateRoomDialog = false }) {
                    Text("Cancel", color = TextSecondaryDark)
                }
            },
            containerColor = DarkSurfaceCard,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun HeaderProfileBar(
    profile: PlayerProfileEntity?,
    onAddCoinsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceCard)
            .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // User Avatar & Level
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2E1065))
                    .border(2.dp, RoyalGold, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "👑", fontSize = 22.sp)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = profile?.username ?: "Subhash",
                        color = TextPrimaryDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    if (profile?.isVip == true) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(RoyalGold)
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "VIP",
                                color = DarkNavyBase,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
                Text(
                    text = "Level ${profile?.level ?: 14} • 🏆 ${profile?.trophies ?: 2450}",
                    color = TextSecondaryDark,
                    fontSize = 11.sp
                )
            }
        }

        // Coins & Diamonds Badges
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Coins
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkSurfaceElevated)
                    .clickable { onAddCoinsClick() }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "💰 ${profile?.coins ?: 15500}",
                    color = RoyalGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            // Diamonds
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkSurfaceElevated)
                    .clickable { onAddCoinsClick() }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "💎 ${profile?.diamonds ?: 120}",
                    color = Color(0xFF38BDF8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun TournamentSeasonBanner(
    onJoinTournamentClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onJoinTournamentClick() }
            .testTag("tournament_banner"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFF7C3AED), Color(0xFF4338CA), Color(0xFFBE185D))
                    )
                )
                .border(1.5.dp, RoyalGold, RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(RoyalGold)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "SEASON 14 LIVE",
                            color = DarkNavyBase,
                            fontWeight = FontWeight.Black,
                            fontSize = 9.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "🏆 Royal Dynasty Cup",
                        color = CleanWhite,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Grand Prize: 50,000 Coins + Crown Dice",
                        color = Color(0xFFFDE68A),
                        fontSize = 12.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(CleanWhite)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ENTER",
                        color = Color(0xFF6D28D9),
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier,
    testTag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .shadow(6.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(gradientColors))
            .clickable { onClick() }
            .padding(14.dp)
            .testTag(testTag)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0x33FFFFFF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = CleanWhite,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                color = CleanWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Text(
                text = subtitle,
                color = Color(0xCCFFFFFF),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun GameModeCard(
    title: String,
    badge: String,
    description: String,
    icon: ImageVector,
    accentColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(14.dp))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.2f))
                    .border(1.dp, accentColor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        color = TextPrimaryDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(accentColor.copy(alpha = 0.2f))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = badge,
                            color = accentColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    color = TextSecondaryDark,
                    fontSize = 11.sp,
                    maxLines = 2
                )
            }

            Text(
                text = "▶",
                color = RoyalGold,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
