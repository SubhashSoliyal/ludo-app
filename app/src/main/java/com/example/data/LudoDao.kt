package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LudoDao {

    @Query("SELECT * FROM match_records ORDER BY dateEpoch DESC LIMIT 50")
    fun getAllMatchRecords(): Flow<List<MatchRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatchRecord(record: MatchRecordEntity)

    @Query("SELECT * FROM player_profile WHERE playerId = :playerId LIMIT 1")
    fun getPlayerProfile(playerId: String = "user_player_1"): Flow<PlayerProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: PlayerProfileEntity)

    @Query("SELECT * FROM cosmetic_items")
    fun getAllCosmetics(): Flow<List<CosmeticItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCosmetics(items: List<CosmeticItemEntity>)

    @Query("UPDATE cosmetic_items SET isUnlocked = 1 WHERE itemId = :itemId")
    suspend fun unlockCosmetic(itemId: String)

    @Query("UPDATE cosmetic_items SET isEquipped = CASE WHEN itemId = :itemId THEN 1 ELSE 0 END WHERE itemType = :itemType")
    suspend fun equipCosmetic(itemId: String, itemType: String)

    @Query("UPDATE player_profile SET coins = coins + :amount WHERE playerId = :playerId")
    suspend fun addCoins(amount: Int, playerId: String = "user_player_1")

    @Query("UPDATE player_profile SET diamonds = diamonds + :amount WHERE playerId = :playerId")
    suspend fun addDiamonds(amount: Int, playerId: String = "user_player_1")

    @Query("UPDATE player_profile SET isVip = :isVip WHERE playerId = :playerId")
    suspend fun setVipStatus(isVip: Boolean, playerId: String = "user_player_1")
}
