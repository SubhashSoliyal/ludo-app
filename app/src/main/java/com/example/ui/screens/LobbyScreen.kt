package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Player
import com.example.model.PlayerColor
import com.example.model.RoomInfo
import com.example.ui.theme.CleanWhite
import com.example.ui.theme.DarkNavyBase
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.LudoBlue
import com.example.ui.theme.LudoGreen
import com.example.ui.theme.LudoRed
import com.example.ui.theme.LudoYellow
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

@Composable
fun LobbyScreen(
    roomInfo: RoomInfo,
    players: List<Player>,
    isHost: Boolean,
    onAddBot: () -> Unit,
    onStartGame: () -> Unit,
    onLeaveLobby: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavyBase)
            .padding(16.dp)
            .testTag("lobby_screen")
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onLeaveLobby) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimaryDark
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "🏰 Private Game Lobby",
                    color = RoyalGold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${roomInfo.gameMode.title} • Entry: 💰 ${roomInfo.entryCoins}",
                    color = TextSecondaryDark,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Room Code & Invite Link Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, RoyalGold, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ROOM CODE",
                    color = TextSecondaryDark,
                    fontSize = 11.sp,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = roomInfo.roomCode,
                        color = RoyalGold,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 3.sp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Room Code", roomInfo.roomCode))
                            Toast.makeText(context, "Room Code Copied!", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Code",
                            tint = CleanWhite
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Share Invite Button
                Button(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "🎲 Join my Ludo Arena room! Code: ${roomInfo.roomCode} or click to play (Cross-platform Web & Mobile): ${roomInfo.webInviteUrl}"
                            )
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Room Invite"))
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RoyalGold,
                        contentColor = DarkNavyBase
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .testTag("share_invite_button")
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share Invite Link", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "🌐 Supports cross-platform mobile & web friends",
                    color = TextSecondaryDark,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Anti-Cheat Guarantee Tag
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF064E3B).copy(alpha = 0.5f))
                .border(1.dp, Color(0xFF10B981), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = "Anti Cheat",
                tint = Color(0xFF34D399),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Anti-Cheat Active: Server-side cryptographic seed validation",
                color = Color(0xFFA7F3D0),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Players List Slots (4 slots)
        Text(
            text = "👥 Players Joined (${players.size}/${roomInfo.maxPlayers})",
            color = TextPrimaryDark,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(players) { index, player ->
                LobbyPlayerCard(player = player)
            }

            // Empty slots
            val emptySlots = (roomInfo.maxPlayers - players.size).coerceAtLeast(0)
            items(emptySlots) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceElevated.copy(alpha = 0.5f))
                        .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⏳ Waiting for player...",
                        color = TextSecondaryDark,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Host Actions: Add Bot & Start Match
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (players.size < roomInfo.maxPlayers) {
                Button(
                    onClick = onAddBot,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkSurfaceElevated,
                        contentColor = TextPrimaryDark
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("add_bot_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Bot", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Bot")
                }
            }

            Button(
                onClick = onStartGame,
                enabled = players.size >= 2,
                colors = ButtonDefaults.buttonColors(
                    containerColor = RoyalGold,
                    contentColor = DarkNavyBase,
                    disabledContainerColor = Color(0x33FFD700),
                    disabledContentColor = Color(0x66000000)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(if (players.size < roomInfo.maxPlayers) 1.4f else 1f)
                    .testTag("start_match_button")
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Start")
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isHost) "Start Match" else "Waiting for Host...",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun LobbyPlayerCard(player: Player) {
    val pColor = when (player.color) {
        PlayerColor.RED -> LudoRed
        PlayerColor.GREEN -> LudoGreen
        PlayerColor.YELLOW -> LudoYellow
        PlayerColor.BLUE -> LudoBlue
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceCard)
            .border(1.dp, pColor.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(pColor.copy(alpha = 0.2f))
                    .border(2.dp, pColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (player.isBot) "🤖" else "🤴",
                    fontSize = 20.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = player.name,
                        color = TextPrimaryDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    if (player.isHost) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(RoyalGold)
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "HOST",
                                color = DarkNavyBase,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
                Text(
                    text = "${player.color.displayName} Team • Ping: ${player.pingMs}ms",
                    color = TextSecondaryDark,
                    fontSize = 11.sp
                )
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF10B981).copy(alpha = 0.2f))
                .border(1.dp, Color(0xFF10B981), RoundedCornerShape(12.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "READY",
                color = Color(0xFF34D399),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
