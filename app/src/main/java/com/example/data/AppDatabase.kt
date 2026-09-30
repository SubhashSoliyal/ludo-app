package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        MatchRecordEntity::class,
        PlayerProfileEntity::class,
        CosmeticItemEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun ludoDao(): LudoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ludo_arena_db"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database.ludoDao())
                    }
                }
            }

            private suspend fun populateInitialData(dao: LudoDao) {
                dao.insertOrUpdateProfile(
                    PlayerProfileEntity(
                        playerId = "user_player_1",
                        username = "Subhash",
                        avatarId = "crown_king",
                        level = 16,
                        trophies = 2480,
                        coins = 18500,
                        diamonds = 140,
                        totalGames = 72,
                        totalWins = 46,
                        capturesCount = 138,
                        sixesCount = 284,
                        isVip = true
                    )
                )

                val cosmetics = listOf(
                    // Dices
                    CosmeticItemEntity("dice_classic", "DICE", "Classic Ivory", "The timeless traditional ivory dice", 0, 0, true, true),
                    CosmeticItemEntity("dice_gold", "DICE", "Golden Royal", "Pure 24k polished gold dice with ruby pips", 5000, 0, true, false),
                    CosmeticItemEntity("dice_magma", "DICE", "Inferno Magma", "Burning volcanic dice with molten glow", 12000, 50, false, false),
                    CosmeticItemEntity("dice_cyber", "DICE", "Neon Cyberpunk", "Holographic future dice with blue lasers", 20000, 100, false, false),

                    // Boards
                    CosmeticItemEntity("board_classic", "BOARD", "Royal Palace", "High-contrast royal court themed board", 0, 0, true, true),
                    CosmeticItemEntity("board_wood", "BOARD", "Antique Teakwood", "Handcrafted premium teak wood finish", 8000, 30, false, false),
                    CosmeticItemEntity("board_galaxy", "BOARD", "Cosmic Starlight", "Deep space galaxy board with twinkling stars", 15000, 80, false, false),
                    CosmeticItemEntity("board_emerald", "BOARD", "Emerald Oasis", "Lush emerald marble with gold star runes", 25000, 150, false, false),

                    // Pawns
                    CosmeticItemEntity("pawn_pawn", "PAWN", "Standard Peg", "Classic wooden pawn figurine", 0, 0, true, false),
                    CosmeticItemEntity("pawn_crown", "PAWN", "Royal Crown", "Imperial royal crown token with gemstone", 6000, 0, true, true),
                    CosmeticItemEntity("pawn_shield", "PAWN", "Viking Shield", "Inscribed runic shield token", 10000, 40, false, false),
                    CosmeticItemEntity("pawn_gem", "PAWN", "Diamond Gem", "Sparkling crystalline cut diamond pawn", 18000, 90, false, false)
                )
                dao.insertCosmetics(cosmetics)

                // Initial sample match record for instant stats display
                dao.insertMatchRecord(
                    MatchRecordEntity(
                        mode = "Classic 4-Player",
                        dateEpoch = System.currentTimeMillis() - 3600000 * 2,
                        finishRank = 1,
                        coinsDelta = 1000,
                        capturesMade = 4,
                        sixesRolled = 8,
                        durationSeconds = 480,
                        opponentsSummary = "vs Rahul, Alex, Maya"
                    )
                )
                dao.insertMatchRecord(
                    MatchRecordEntity(
                        mode = "Team Battle 2v2",
                        dateEpoch = System.currentTimeMillis() - 3600000 * 6,
                        finishRank = 1,
                        coinsDelta = 2000,
                        capturesMade = 3,
                        sixesRolled = 6,
                        durationSeconds = 620,
                        opponentsSummary = "Team Red+Yellow vs Team Green+Blue"
                    )
                )
            }
        }
    }
}
