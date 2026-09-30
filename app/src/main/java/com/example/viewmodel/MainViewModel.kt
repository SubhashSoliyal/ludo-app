package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CosmeticItemEntity
import com.example.data.LudoRepository
import com.example.data.MatchRecordEntity
import com.example.data.PlayerProfileEntity
import com.example.model.GameMode
import com.example.model.Player
import com.example.model.PlayerColor
import com.example.model.RoomInfo
import com.example.model.TournamentBracket
import com.example.model.TournamentMatch
import com.example.network.MultiplayerSyncManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

data class LeaderboardEntry(
    val rank: Int,
    val username: String,
    val trophies: Int,
    val winRate: String,
    val tier: String,
    val avatar: String,
    val isCurrentUser: Boolean = false
)

enum class AuthProvider {
    GOOGLE,
    FACEBOOK,
    GUEST
}

class MainViewModel(
    private val repository: LudoRepository,
    val syncManager: MultiplayerSyncManager
) : ViewModel() {

    val profile: StateFlow<PlayerProfileEntity?> = repository.playerProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val matchHistory: StateFlow<List<MatchRecordEntity>> = repository.matchHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cosmetics: StateFlow<List<CosmeticItemEntity>> = repository.cosmetics
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active auth session
    private val _currentAuth = MutableStateFlow(AuthProvider.GOOGLE)
    val currentAuth: StateFlow<AuthProvider> = _currentAuth.asStateFlow()

    // Tournament system
    private val _activeTournament = MutableStateFlow<TournamentBracket?>(null)
    val activeTournament: StateFlow<TournamentBracket?> = _activeTournament.asStateFlow()

    // Leaderboards
    private val _leaderboardType = MutableStateFlow("WEEKLY") // "DAILY", "WEEKLY", "ALL_TIME"
    val leaderboardType: StateFlow<String> = _leaderboardType.asStateFlow()

    private val _leaderboardEntries = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val leaderboardEntries: StateFlow<List<LeaderboardEntry>> = _leaderboardEntries.asStateFlow()

    // Stripe checkout state
    private val _stripeSheetVisible = MutableStateFlow(false)
    val stripeSheetVisible: StateFlow<Boolean> = _stripeSheetVisible.asStateFlow()

    private val _stripeStatus = MutableStateFlow<String?>(null)
    val stripeStatus: StateFlow<String?> = _stripeStatus.asStateFlow()

    init {
        generateLeaderboards("WEEKLY")
        initializeTournamentBracket()
    }

    fun setLeaderboardFilter(type: String) {
        _leaderboardType.value = type
        generateLeaderboards(type)
    }

    private fun generateLeaderboards(type: String) {
        val multiplier = when (type) {
            "DAILY" -> 1
            "WEEKLY" -> 3
            else -> 10
        }
        val entries = mutableListOf<LeaderboardEntry>()
        val topUsers = listOf(
            Triple("Vikram_Rathore", 3890 * multiplier, "76%"),
            Triple("QueenElena", 3740 * multiplier, "74%"),
            Triple("LudoMaster_99", 3620 * multiplier, "71%"),
            Triple("DragonSlayer", 3480 * multiplier, "69%"),
            Triple("GoldenDice_Pro", 3350 * multiplier, "68%"),
            Triple("Subhash", 3120 * multiplier, "67%"), // Current user in top 6!
            Triple("ShadowWalker", 2990 * multiplier, "65%"),
            Triple("ThunderStrike", 2850 * multiplier, "63%"),
            Triple("LuckyStar_21", 2710 * multiplier, "61%"),
            Triple("CyberKing", 2600 * multiplier, "59%")
        )

        topUsers.forEachIndexed { index, triple ->
            val isUser = triple.first == "Subhash"
            val tier = when {
                index < 3 -> "Grandmaster"
                index < 6 -> "Master"
                index < 10 -> "Diamond"
                else -> "Platinum"
            }
            entries.add(
                LeaderboardEntry(
                    rank = index + 1,
                    username = triple.first,
                    trophies = triple.second,
                    winRate = triple.third,
                    tier = tier,
                    avatar = if (isUser) "crown_king" else "avatar_${(index % 5) + 1}",
                    isCurrentUser = isUser
                )
            )
        }
        _leaderboardEntries.value = entries
    }

    private fun initializeTournamentBracket() {
        val matches = listOf(
            TournamentMatch("m1", "Quarterfinals", "Subhash (You)", "Alex_99", isUserMatch = true),
            TournamentMatch("m2", "Quarterfinals", "Viper_X", "QueenElena"),
            TournamentMatch("m3", "Quarterfinals", "LudoLord", "ShadowNinja"),
            TournamentMatch("m4", "Quarterfinals", "DragonSlayer", "LuckyStar"),
            // Semifinals
            TournamentMatch("m5", "Semifinals", "TBD", "TBD"),
            TournamentMatch("m6", "Semifinals", "TBD", "TBD"),
            // Finals
            TournamentMatch("m7", "Grand Championship", "TBD", "TBD")
        )

        _activeTournament.value = TournamentBracket(
            id = "tb_royal_season_14",
            title = "Royal Dynasty Cup - 50,000 Coins",
            prizePool = 50000,
            currentRound = 1,
            matches = matches
        )
    }

    fun advanceTournamentRound() {
        val current = _activeTournament.value ?: return
        val updatedMatches = current.matches.map { m ->
            when (m.matchId) {
                "m1" -> m.copy(winnerName = "Subhash (You)", isCompleted = true)
                "m2" -> m.copy(winnerName = "QueenElena", isCompleted = true)
                "m3" -> m.copy(winnerName = "LudoLord", isCompleted = true)
                "m4" -> m.copy(winnerName = "DragonSlayer", isCompleted = true)
                "m5" -> m.copy(player1Name = "Subhash (You)", player2Name = "QueenElena", isUserMatch = true)
                "m6" -> m.copy(player1Name = "LudoLord", player2Name = "DragonSlayer")
                else -> m
            }
        }
        _activeTournament.value = current.copy(currentRound = 2, matches = updatedMatches)
    }

    fun completeTournamentChampionship() {
        val current = _activeTournament.value ?: return
        val updatedMatches = current.matches.map { m ->
            when (m.matchId) {
                "m5" -> m.copy(winnerName = "Subhash (You)", isCompleted = true)
                "m6" -> m.copy(winnerName = "DragonSlayer", isCompleted = true)
                "m7" -> m.copy(player1Name = "Subhash (You)", player2Name = "DragonSlayer", winnerName = "Subhash (You)", isCompleted = true, isUserMatch = true)
                else -> m
            }
        }
        _activeTournament.value = current.copy(currentRound = 3, matches = updatedMatches)

        viewModelScope.launch {
            repository.addCoins(50000)
            repository.addDiamonds(100)
        }
    }

    fun unlockCosmetic(item: CosmeticItemEntity) {
        val currentCoins = profile.value?.coins ?: 0
        val currentDiamonds = profile.value?.diamonds ?: 0

        if (currentCoins >= item.priceCoins && currentDiamonds >= item.priceGems) {
            viewModelScope.launch {
                repository.addCoins(-item.priceCoins)
                repository.addDiamonds(-item.priceGems)
                repository.unlockItem(item.itemId)
                repository.equipItem(item.itemId, item.itemType)
            }
        }
    }

    fun equipCosmetic(item: CosmeticItemEntity) {
        viewModelScope.launch {
            repository.equipItem(item.itemId, item.itemType)
        }
    }

    fun switchAuth(provider: AuthProvider) {
        _currentAuth.value = provider
        viewModelScope.launch {
            val name = when (provider) {
                AuthProvider.GOOGLE -> "Subhash (Google)"
                AuthProvider.FACEBOOK -> "Subhash (FB Gaming)"
                AuthProvider.GUEST -> "Guest_4892"
            }
            profile.value?.let { p ->
                repository.updateProfile(p.copy(username = name))
            }
        }
    }

    fun openStripeCheckout() {
        _stripeSheetVisible.value = true
        _stripeStatus.value = null
    }

    fun closeStripeCheckout() {
        _stripeSheetVisible.value = false
    }

    fun processStripePayment(
        cardNumber: String,
        isVipSubscription: Boolean,
        coinAmount: Int = 25000,
        gemAmount: Int = 250
    ) {
        viewModelScope.launch {
            _stripeStatus.value = "Processing with Stripe Secure Gateway..."
            kotlinx.coroutines.delay(1200)

            if (cardNumber.length >= 15) {
                if (isVipSubscription) {
                    repository.setVipStatus(true)
                    repository.addCoins(50000)
                    repository.addDiamonds(500)
                    _stripeStatus.value = "✅ Stripe Payment Successful! Royal VIP Pass Activated!"
                } else {
                    repository.addCoins(coinAmount)
                    repository.addDiamonds(gemAmount)
                    _stripeStatus.value = "✅ Stripe Payment Successful! $coinAmount Coins & $gemAmount Gems added!"
                }
            } else {
                _stripeStatus.value = "❌ Stripe Card Validation Failed. Enter a valid 16-digit card."
            }
        }
    }

    fun updateProfileAvatar(avatarId: String) {
        viewModelScope.launch {
            profile.value?.let {
                repository.updateProfile(it.copy(avatarId = avatarId))
            }
        }
    }
}
