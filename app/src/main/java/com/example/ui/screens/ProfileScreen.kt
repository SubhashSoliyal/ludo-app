package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MatchRecordEntity
import com.example.ui.theme.CleanWhite
import com.example.ui.theme.DarkNavyBase
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalPurple
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.viewmodel.AuthProvider
import com.example.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileScreen(
    mainViewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val profile by mainViewModel.profile.collectAsState()
    val matchHistory by mainViewModel.matchHistory.collectAsState()
    val currentAuth by mainViewModel.currentAuth.collectAsState()

    val totalGames = profile?.totalGames ?: 0
    val totalWins = profile?.totalWins ?: 0
    val winRate = if (totalGames > 0) ((totalWins * 100) / totalGames) else 0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavyBase)
            .padding(16.dp)
            .testTag("profile_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Text(
                text = "👤 Player Profile & History",
                color = RoyalGold,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black
            )
        }

        // Profile Identity Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2E1065))
                            .border(3.dp, RoyalGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "👑", fontSize = 32.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = profile?.username ?: "Subhash",
                            color = CleanWhite,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                        if (profile?.isVip == true) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(RoyalGold)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("VIP", color = DarkNavyBase, fontSize = 9.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }

                    Text(
                        text = "Level ${profile?.level ?: 14} Master • 🏆 ${profile?.trophies ?: 2450} Trophies",
                        color = TextSecondaryDark,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Social Auth Switcher
                    Text(
                        text = "Linked Social Authentication",
                        color = TextSecondaryDark,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple(AuthProvider.GOOGLE, "Google", "🌐"),
                            Triple(AuthProvider.FACEBOOK, "Facebook", "📘"),
                            Triple(AuthProvider.GUEST, "Guest", "👤")
                        ).forEach { (provider, label, icon) ->
                            val isSelected = currentAuth == provider
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
                                    .clickable { mainViewModel.switchAuth(provider) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$icon $label",
                                    color = if (isSelected) CleanWhite else TextSecondaryDark,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Career Statistics Grid
        item {
            Text(
                text = "📊 Career Statistics",
                color = RoyalGold,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatBox(label = "Matches", value = "$totalGames", modifier = Modifier.weight(1f))
                StatBox(label = "Wins", value = "$totalWins", modifier = Modifier.weight(1f))
                StatBox(label = "Win Rate", value = "$winRate%", modifier = Modifier.weight(1f))
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatBox(label = "Captures Made", value = "${profile?.capturesCount ?: 138}", modifier = Modifier.weight(1f))
                StatBox(label = "Sixes Rolled", value = "${profile?.sixesCount ?: 284}", modifier = Modifier.weight(1f))
                StatBox(label = "Fair Play Score", value = "100%", modifier = Modifier.weight(1f))
            }
        }

        // Match History Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📜 Recent Match History",
                    color = RoyalGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "${matchHistory.size} Records",
                    color = TextSecondaryDark,
                    fontSize = 11.sp
                )
            }
        }

        // Match History Items
        if (matchHistory.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceElevated)
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No matches played yet. Start a match to see history!", color = TextSecondaryDark, fontSize = 12.sp)
                }
            }
        } else {
            items(matchHistory) { record ->
                MatchHistoryCard(record = record)
            }
        }
    }
}

@Composable
fun StatBox(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(12.dp))
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, color = RoyalGold, fontWeight = FontWeight.Black, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = label, color = TextSecondaryDark, fontSize = 10.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun MatchHistoryCard(record: MatchRecordEntity) {
    val isWin = record.finishRank == 1
    val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(record.dateEpoch))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = if (isWin) Color(0xFF10B981) else Color(0x1AFFFFFF),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isWin) Color(0xFF065F46) else DarkSurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = if (isWin) "🥇" else "#${record.finishRank}", fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(text = record.mode, color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(text = "$formattedDate • ${record.durationSeconds}s", color = TextSecondaryDark, fontSize = 10.sp)
                    Text(text = "⚔️ ${record.capturesMade} captures • 🎲 ${record.sixesRolled} sixes", color = TextSecondaryDark, fontSize = 10.sp)
                }
            }

            Text(
                text = if (record.coinsDelta >= 0) "+${record.coinsDelta} 💰" else "${record.coinsDelta} 💰",
                color = if (record.coinsDelta >= 0) Color(0xFF34D399) else Color(0xFFF87171),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}
