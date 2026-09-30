package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatMessage
import com.example.ui.theme.DarkNavyBase
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InGameChatOverlay(
    chatMessages: List<ChatMessage>,
    onSendMessage: (String, Boolean) -> Unit,
    onSendEmoji: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var customText by remember { mutableStateOf("") }

    val quickPhrases = listOf(
        "Roll fast! ⏱️",
        "Good move! 👏",
        "Oops! 😅",
        "I'm coming for you! ⚔️",
        "Nice try! 😉",
        "Well played! 🏆",
        "Unlucky roll! 🎲",
        "GG! 🔥"
    )

    val emojis = listOf("👑", "🔥", "😂", "😭", "👏", "😡", "🎲", "🎯")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(DarkSurfaceCard)
            .border(1.dp, Color(0x33FFD700), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .padding(16.dp)
            .testTag("in_game_chat_overlay")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💬 Match Chat & Reactions",
                    color = RoyalGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Chat",
                        tint = TextSecondaryDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Emoji Reaction Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                emojis.forEach { emoji ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceElevated)
                            .clickable {
                                onSendEmoji(emoji)
                                onSendMessage(emoji, true)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = 20.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Recent Messages List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkNavyBase)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (chatMessages.isEmpty()) {
                    item {
                        Text(
                            text = "No messages yet. Send a quick phrase or emoji!",
                            color = TextSecondaryDark,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
                items(chatMessages.takeLast(10)) { msg ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${msg.senderName}: ",
                            color = RoyalGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = msg.text,
                            color = TextPrimaryDark,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Tactical Phrases
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                quickPhrases.forEach { phrase ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(DarkSurfaceElevated)
                            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp))
                            .clickable { onSendMessage(phrase, false) }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = phrase,
                            color = TextPrimaryDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Custom Text Input Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = customText,
                    onValueChange = { customText = it },
                    placeholder = { Text("Type custom message...", fontSize = 12.sp, color = TextSecondaryDark) },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("chat_input_field"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark,
                        focusedBorderColor = RoyalGold,
                        unfocusedBorderColor = Color(0x44FFFFFF),
                        focusedContainerColor = DarkSurfaceElevated,
                        unfocusedContainerColor = DarkSurfaceElevated
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(RoyalGold)
                        .clickable {
                            if (customText.isNotBlank()) {
                                onSendMessage(customText.trim(), false)
                                customText = ""
                            }
                        }
                        .testTag("send_chat_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = DarkNavyBase,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
