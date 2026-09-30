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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.example.ui.theme.CleanWhite
import com.example.ui.theme.DarkNavyBase
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.viewmodel.LeaderboardEntry
import com.example.viewmodel.MainViewModel

@Composable
fun LeaderboardScreen(
    mainViewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val entries by mainViewModel.leaderboardEntries.collectAsState()
    val filterType by mainViewModel.leaderboardType.collectAsState()

    val tabList = listOf("DAILY", "WEEKLY", "ALL_TIME")
    val selectedTabIndex = tabList.indexOf(filterType).coerceAtLeast(0)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavyBase)
            .padding(16.dp)
            .testTag("leaderboard_screen")
    ) {
        // Header
        Column {
            Text(
                text = "👑 Global Hall of Fame",
                color = RoyalGold,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Top ranked competitive Ludo masters worldwide",
                color = TextSecondaryDark,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Time Filters TabRow
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = DarkSurfaceElevated,
            contentColor = RoyalGold,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = RoyalGold
                )
            },
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            tabList.forEachIndexed { index, tab ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { mainViewModel.setLeaderboardFilter(tab) },
                    text = {
                        Text(
                            text = when (tab) {
                                "DAILY" -> "Daily"
                                "WEEKLY" -> "Weekly"
                                else -> "All-Time"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // List of Leaderboard Entries
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(entries) { entry ->
                LeaderboardRow(entry = entry)
            }
        }

        // Sticky User Rank at bottom
        val userEntry = entries.firstOrNull { it.isCurrentUser }
        userEntry?.let { entry ->
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, RoyalGold, RoundedCornerShape(14.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "#${entry.rank}",
                            color = RoyalGold,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Your Global Rank (${entry.tier})",
                            color = CleanWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Text(
                        text = "🏆 ${entry.trophies}",
                        color = RoyalGold,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun LeaderboardRow(entry: LeaderboardEntry) {
    val rankColor = when (entry.rank) {
        1 -> RoyalGold
        2 -> Color(0xFFE2E8F0)
        3 -> Color(0xFFCD7F32)
        else -> TextSecondaryDark
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (entry.isCurrentUser) Color(0xFF1E1B4B) else DarkSurfaceCard)
            .border(
                width = if (entry.isCurrentUser) 1.5.dp else 1.dp,
                color = if (entry.isCurrentUser) RoyalGold else Color(0x1AFFFFFF),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Rank Number
            Box(
                modifier = Modifier.width(28.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = when (entry.rank) {
                        1 -> "🥇"
                        2 -> "🥈"
                        3 -> "🥉"
                        else -> "#${entry.rank}"
                    },
                    color = rankColor,
                    fontWeight = FontWeight.Black,
                    fontSize = if (entry.rank <= 3) 16.sp else 13.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Avatar
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(DarkSurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Text(text = if (entry.isCurrentUser) "👑" else "🤴", fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = entry.username,
                    color = if (entry.isCurrentUser) RoyalGold else TextPrimaryDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = "${entry.tier} • Win Rate: ${entry.winRate}",
                    color = TextSecondaryDark,
                    fontSize = 10.sp
                )
            }
        }

        Text(
            text = "🏆 ${entry.trophies}",
            color = RoyalGold,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}
