package com.example.data

import kotlinx.coroutines.flow.Flow

class LudoRepository(private val dao: LudoDao) {

    val matchHistory: Flow<List<MatchRecordEntity>> = dao.getAllMatchRecords()
    val playerProfile: Flow<PlayerProfileEntity?> = dao.getPlayerProfile()
    val cosmetics: Flow<List<CosmeticItemEntity>> = dao.getAllCosmetics()

    suspend fun recordMatch(record: MatchRecordEntity) {
        dao.insertMatchRecord(record)
    }

    suspend fun updateProfile(profile: PlayerProfileEntity) {
        dao.insertOrUpdateProfile(profile)
    }

    suspend fun unlockItem(itemId: String) {
        dao.unlockCosmetic(itemId)
    }

    suspend fun equipItem(itemId: String, itemType: String) {
        dao.equipCosmetic(itemId, itemType)
    }

    suspend fun addCoins(amount: Int) {
        dao.addCoins(amount)
    }

    suspend fun addDiamonds(amount: Int) {
        dao.addDiamonds(amount)
    }

    suspend fun setVipStatus(isVip: Boolean) {
        dao.setVipStatus(isVip)
    }
}
