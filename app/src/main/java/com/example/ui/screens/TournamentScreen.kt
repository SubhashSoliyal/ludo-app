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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TournamentMatch
import com.example.ui.theme.CleanWhite
import com.example.ui.theme.DarkNavyBase
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalPurple
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.viewmodel.MainViewModel

@Composable
fun TournamentScreen(
    mainViewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val tournament by mainViewModel.activeTournament.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavyBase)
            .padding(16.dp)
            .testTag("tournament_screen")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "🏆 Tournament Arena",
                    color = RoyalGold,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Automated brackets & seasonal rewards",
                    color = TextSecondaryDark,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tabs: Live Bracket vs Season Pass
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = DarkSurfaceElevated,
            contentColor = RoyalGold,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = RoyalGold
                )
            },
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Championship Bracket", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Season 14 Pass", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (selectedTab == 0) {
            // Bracket View
            tournament?.let { bracket ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        // Grand Banner
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFFB45309), Color(0xFF78350F), Color(0xFF1E1B4B))
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
                                    Column {
                                        Text(
                                            text = bracket.title,
                                            color = RoyalGold,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 16.sp
                                        )
                                        Text(
                                            text = "Prize Pool: 💰 50,000 Coins + 💎 100 Gems",
                                            color = CleanWhite,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Text(text = "👑", fontSize = 28.sp)
                                }
                            }
                        }
                    }

                    item {
                        // Action Button
                        if (bracket.currentRound == 1) {
                            Button(
                                onClick = { mainViewModel.advanceTournamentRound() },
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalGold, contentColor = DarkNavyBase),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("advance_round_button")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Play")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Play Quarterfinals Match", fontWeight = FontWeight.Bold)
                            }
                        } else if (bracket.currentRound == 2) {
                            Button(
                                onClick = { mainViewModel.completeTournamentChampionship() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981), contentColor = CleanWhite),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("championship_match_button")
                            ) {
                                Icon(Icons.Default.EmojiEvents, contentDescription = "Play Finals")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Play Grand Finals Match", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF065F46))
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "🎉 You Won the Championship! 50,000 Coins Claimed!",
                                    color = RoyalGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    // Bracket Matches
                    items(bracket.matches) { match ->
                        TournamentMatchNode(match = match)
                    }
                }
            }
        } else {
            // Season Pass Road
            SeasonPassRoad()
        }
    }
}

@Composable
fun TournamentMatchNode(match: TournamentMatch) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = if (match.isUserMatch) 1.5.dp else 1.dp,
                    color = if (match.isUserMatch) RoyalGold else Color(0x22FFFFFF),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = match.roundName,
                    color = if (match.isUserMatch) RoyalGold else TextSecondaryDark,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                if (match.isCompleted) {
                    Text(
                        text = "Winner: ${match.winnerName}",
                        color = Color(0xFF34D399),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = match.player1Name,
                    color = if (match.winnerName == match.player1Name) RoyalGold else TextPrimaryDark,
                    fontWeight = if (match.winnerName == match.player1Name) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 13.sp
                )
                Text(text = "VS", color = TextSecondaryDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = match.player2Name,
                    color = if (match.winnerName == match.player2Name) RoyalGold else TextPrimaryDark,
                    fontWeight = if (match.winnerName == match.player2Name) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun SeasonPassRoad() {
    val tiers = listOf(
        Triple(1, "💰 2,000 Coins", "Unlocked"),
        Triple(2, "💎 20 Diamonds", "Unlocked"),
        Triple(3, "👑 Golden Crown Avatar", "Claimed"),
        Triple(4, "💰 5,000 Coins", "Next Tier (120/200 XP)"),
        Triple(5, "🎲 Inferno Magma Dice", "Locked"),
        Triple(6, "💎 50 Diamonds", "Locked"),
        Triple(10, "🏰 Royal Palace 3D Board", "Locked Tier 10")
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(tiers) { (tier, reward, status) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceCard)
                    .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(RoyalPurple),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "T$tier", color = CleanWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = reward, color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = status, color = if (status.contains("Unlocked")) Color(0xFF34D399) else TextSecondaryDark, fontSize = 11.sp)
                    }
                }

                if (status == "Unlocked") {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(RoyalGold)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text("CLAIM", color = DarkNavyBase, fontWeight = FontWeight.Black, fontSize = 10.sp)
                    }
                } else if (status == "Claimed") {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Done", tint = Color(0xFF10B981))
                } else {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = "Locked", tint = TextSecondaryDark)
                }
            }
        }
    }
}
