package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "match_records")
data class MatchRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val mode: String,
    val dateEpoch: Long = System.currentTimeMillis(),
    val finishRank: Int, // 1 for 1st, 2 for 2nd, etc.
    val coinsDelta: Int,
    val capturesMade: Int,
    val sixesRolled: Int,
    val durationSeconds: Int,
    val opponentsSummary: String
)

@Entity(tableName = "player_profile")
data class PlayerProfileEntity(
    @PrimaryKey
    val playerId: String = "user_player_1",
    val username: String = "KingSubhash",
    val avatarId: String = "crown_king",
    val level: Int = 14,
    val trophies: Int = 2450,
    val coins: Int = 15500,
    val diamonds: Int = 120,
    val totalGames: Int = 86,
    val totalWins: Int = 54,
    val capturesCount: Int = 142,
    val sixesCount: Int = 310,
    val isVip: Boolean = false,
    val countryCode: String = "IN",
    val equippedDiceSkin: String = "classic_gold",
    val equippedBoardTheme: String = "royal_palace",
    val equippedPawnSkin: String = "royal_crown"
)

@Entity(tableName = "cosmetic_items")
data class CosmeticItemEntity(
    @PrimaryKey
    val itemId: String,
    val itemType: String, // "DICE", "BOARD", "PAWN"
    val name: String,
    val description: String,
    val priceCoins: Int,
    val priceGems: Int,
    val isUnlocked: Boolean = false,
    val isEquipped: Boolean = false
)
