package com.example

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.LudoRepository
import com.example.model.GameMode
import com.example.model.Player
import com.example.model.PlayerColor
import com.example.model.RoomInfo
import com.example.network.MultiplayerSyncManager
import com.example.notification.NotificationHelper
import com.example.ui.screens.GameScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.LobbyScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.StoreScreen
import com.example.ui.screens.TournamentScreen
import com.example.ui.theme.DarkNavyBase
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.viewmodel.LudoGameViewModel
import com.example.viewmodel.MainViewModel

enum class AppScreen {
    HOME,
    TOURNAMENT,
    LEADERBOARD,
    STORE,
    PROFILE,
    LOBBY,
    GAME
}

class MainActivity : ComponentActivity() {

    private var initialDeepLinkRoomCode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create notification channels for turn alerts and tournaments
        NotificationHelper.createNotificationChannels(this)

        // Handle deep links for room invitations
        handleIncomingDeepLink(intent)

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = LudoRepository(database.ludoDao())

        setContent {
            MyApplicationTheme {
                LudoArenaApp(
                    repository = repository,
                    initialRoomCode = initialDeepLinkRoomCode
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingDeepLink(intent)
    }

    private fun handleIncomingDeepLink(intent: Intent?) {
        val data = intent?.data ?: return
        if (data.scheme == "ludo" && data.host == "join") {
            initialDeepLinkRoomCode = data.getQueryParameter("room")
        } else if (data.scheme == "https" && data.host == "ludoarena.app") {
            val segments = data.pathSegments
            if (segments.size >= 2 && segments[0] == "join") {
                initialDeepLinkRoomCode = segments[1]
            }
        }
    }
}

@Composable
fun LudoArenaApp(
    repository: LudoRepository,
    initialRoomCode: String? = null
) {
    val coroutineScope = rememberCoroutineScope()
    val syncManager = remember { MultiplayerSyncManager(coroutineScope) }
    val mainViewModel = remember { MainViewModel(repository, syncManager) }
    val gameViewModel = remember { LudoGameViewModel(repository, syncManager) }

    val userProfile by mainViewModel.profile.collectAsState()
    val currentRoom by syncManager.currentRoom.collectAsState()
    val lobbyPlayers by syncManager.lobbyPlayers.collectAsState()

    var currentScreen by remember { mutableStateOf(AppScreen.HOME) }

    val context = LocalContext.current

    // Auto join if launched with deep link
    LaunchedEffect(initialRoomCode) {
        if (!initialRoomCode.isNullOrBlank()) {
            val userPlayer = Player(
                id = "user",
                name = userProfile?.username ?: "Subhash",
                color = PlayerColor.RED,
                isBot = false,
                isHost = false
            )
            syncManager.joinRoom(initialRoomCode, userPlayer) { success, msg ->
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                if (success) {
                    currentScreen = AppScreen.LOBBY
                }
            }
        }
    }

    // Secondary screen back handler
    if (currentScreen == AppScreen.LOBBY) {
        BackHandler {
            syncManager.leaveRoom()
            currentScreen = AppScreen.HOME
        }
    } else if (currentScreen != AppScreen.HOME && currentScreen != AppScreen.GAME) {
        BackHandler {
            currentScreen = AppScreen.HOME
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkNavyBase,
        bottomBar = {
            // Only show bottom navigation on primary screens
            if (currentScreen != AppScreen.GAME && currentScreen != AppScreen.LOBBY) {
                NavigationBar(
                    containerColor = DarkSurfaceElevated,
                    contentColor = RoyalGold,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                ) {
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.HOME,
                        onClick = { currentScreen = AppScreen.HOME },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Play") },
                        label = { Text("Play", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkNavyBase,
                            indicatorColor = RoyalGold,
                            selectedTextColor = RoyalGold,
                            unselectedIconColor = TextSecondaryDark,
                            unselectedTextColor = TextSecondaryDark
                        ),
                        modifier = Modifier.testTag("nav_home")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.TOURNAMENT,
                        onClick = { currentScreen = AppScreen.TOURNAMENT },
                        icon = { Icon(Icons.Default.EmojiEvents, contentDescription = "Tournaments") },
                        label = { Text("Tournaments", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkNavyBase,
                            indicatorColor = RoyalGold,
                            selectedTextColor = RoyalGold,
                            unselectedIconColor = TextSecondaryDark,
                            unselectedTextColor = TextSecondaryDark
                        ),
                        modifier = Modifier.testTag("nav_tournaments")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.LEADERBOARD,
                        onClick = { currentScreen = AppScreen.LEADERBOARD },
                        icon = { Icon(Icons.Default.Leaderboard, contentDescription = "Leaderboard") },
                        label = { Text("Rankings", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkNavyBase,
                            indicatorColor = RoyalGold,
                            selectedTextColor = RoyalGold,
                            unselectedIconColor = TextSecondaryDark,
                            unselectedTextColor = TextSecondaryDark
                        ),
                        modifier = Modifier.testTag("nav_leaderboard")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.STORE,
                        onClick = { currentScreen = AppScreen.STORE },
                        icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Store") },
                        label = { Text("Store", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkNavyBase,
                            indicatorColor = RoyalGold,
                            selectedTextColor = RoyalGold,
                            unselectedIconColor = TextSecondaryDark,
                            unselectedTextColor = TextSecondaryDark
                        ),
                        modifier = Modifier.testTag("nav_store")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.PROFILE,
                        onClick = { currentScreen = AppScreen.PROFILE },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                        label = { Text("Profile", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkNavyBase,
                            indicatorColor = RoyalGold,
                            selectedTextColor = RoyalGold,
                            unselectedIconColor = TextSecondaryDark,
                            unselectedTextColor = TextSecondaryDark
                        ),
                        modifier = Modifier.testTag("nav_profile")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.HOME -> {
                    HomeScreen(
                        mainViewModel = mainViewModel,
                        profile = userProfile,
                        onCreatePrivateRoom = { mode, fee ->
                            val userPlayer = Player(
                                id = "user",
                                name = userProfile?.username ?: "Subhash",
                                color = PlayerColor.RED,
                                isBot = false,
                                isHost = true
                            )
                            syncManager.createRoom(userPlayer, mode, fee)
                            currentScreen = AppScreen.LOBBY
                        },
                        onJoinRoom = { code ->
                            val userPlayer = Player(
                                id = "user",
                                name = userProfile?.username ?: "Subhash",
                                color = PlayerColor.GREEN,
                                isBot = false,
                                isHost = false
                            )
                            syncManager.joinRoom(code, userPlayer) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                if (success) {
                                    currentScreen = AppScreen.LOBBY
                                }
                            }
                        },
                        onQuickStart = { mode ->
                            // Instantly initialize local/practice/quick match
                            gameViewModel.initializeGame(
                                players = emptyList(),
                                mode = mode,
                                room = null
                            )
                            currentScreen = AppScreen.GAME
                        },
                        onNavigateTournament = { currentScreen = AppScreen.TOURNAMENT },
                        onNavigateStore = { currentScreen = AppScreen.STORE }
                    )
                }

                AppScreen.LOBBY -> {
                    currentRoom?.let { room ->
                        LobbyScreen(
                            roomInfo = room,
                            players = lobbyPlayers,
                            isHost = lobbyPlayers.firstOrNull { it.id == "user" }?.isHost == true,
                            onAddBot = {
                                syncManager.addOnlineBotToLobby()
                            },
                            onStartGame = {
                                syncManager.startGame()
                                gameViewModel.initializeGame(
                                    players = lobbyPlayers,
                                    mode = room.gameMode,
                                    room = room
                                )
                                currentScreen = AppScreen.GAME
                            },
                            onLeaveLobby = {
                                syncManager.leaveRoom()
                                currentScreen = AppScreen.HOME
                            }
                        )
                    } ?: run {
                        currentScreen = AppScreen.HOME
                    }
                }

                AppScreen.GAME -> {
                    GameScreen(
                        gameViewModel = gameViewModel,
                        onExitGame = {
                            currentScreen = AppScreen.HOME
                        }
                    )
                }

                AppScreen.TOURNAMENT -> {
                    TournamentScreen(mainViewModel = mainViewModel)
                }

                AppScreen.LEADERBOARD -> {
                    LeaderboardScreen(mainViewModel = mainViewModel)
                }

                AppScreen.STORE -> {
                    StoreScreen(mainViewModel = mainViewModel)
                }

                AppScreen.PROFILE -> {
                    ProfileScreen(mainViewModel = mainViewModel)
                }
            }
        }
    }
}
