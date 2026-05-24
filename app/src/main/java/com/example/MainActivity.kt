package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.*
import com.example.data.local.AppDatabase
import com.example.data.repository.PlatformRepository
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.CyberpunkYellow
import com.example.ui.theme.DeepSpaceBlack
import com.example.ui.viewmodel.PlatformViewModel
import com.example.ui.viewmodel.PlatformViewModelFactory

class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Robust initialization bypassing fragile dependency injection
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = PlatformRepository(database.tournamentDao())
        
        val viewModel: PlatformViewModel by viewModels {
            PlatformViewModelFactory(repository)
        }

        setContent {
            MyApplicationTheme {
                val context = LocalContext.current
                var currentTabState by remember { mutableStateOf("home") }

                // Live Toast collector for instant actions feedback
                LaunchedEffect(Unit) {
                    viewModel.toastMessage.collect { message ->
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    }
                }

                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "splash",
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 1. SPLASH INTRO PORTAL
                    composable("splash") {
                        SplashScreen(
                            viewModel = viewModel,
                            onNavigateToAuth = {
                                navController.navigate("auth") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            },
                            onNavigateToHome = {
                                navController.navigate("main") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            }
                        )
                    }

                    // 2. AUTHENTICATION CONTROLLER PANEL
                    composable("auth") {
                        AuthScreen(
                            viewModel = viewModel,
                            onAuthSuccess = {
                                navController.navigate("main") {
                                    popUpTo("auth") { inclusive = true }
                                }
                            }
                        )
                    }

                    // 3. MAIN PORTAL HUB WITH TRANSLUCENT BOTTOM NAVIGATION
                    composable("main") {
                        Scaffold(
                            modifier = Modifier
                                .fillMaxSize()
                                .statusBarsPadding(),
                            containerColor = DeepSpaceBlack,
                            bottomBar = {
                                // High-fidelity frosted glass cyberpunk bottom bar
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .navigationBarsPadding()
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                        .clip(RoundedCornerShape(24.dp))
                                        .border(
                                            BorderStroke(0.8.dp, Color(0x3300E5FF)),
                                            RoundedCornerShape(24.dp)
                                        ),
                                    color = Color(0xD9101018), 
                                    tonalElevation = 8.dp
                                ) {
                                    NavigationBar(
                                        containerColor = Color.Transparent,
                                        modifier = Modifier.height(64.dp)
                                    ) {
                                        // Feed Tab
                                        NavigationBarItem(
                                            selected = currentTabState == "home",
                                            onClick = { currentTabState = "home" },
                                            icon = {
                                                Icon(
                                                    imageVector = Icons.Default.Home,
                                                    contentDescription = "Home Hub"
                                                )
                                            },
                                            label = { Text("HOME", fontSize = 10.sp) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = CyberpunkYellow,
                                                selectedTextColor = CyberpunkYellow,
                                                unselectedIconColor = Color(0xFF8E8E9F),
                                                unselectedTextColor = Color(0xFF8E8E9F),
                                                indicatorColor = Color(0x26E5FF00)
                                            ),
                                            modifier = Modifier.testTag("nav_home")
                                        )

                                        // Matches & Lobbies Tab
                                        NavigationBarItem(
                                            selected = currentTabState == "matches",
                                            onClick = { currentTabState = "matches" },
                                            icon = {
                                                Icon(
                                                    imageVector = Icons.Default.SportsEsports,
                                                    contentDescription = "My Registered Matches"
                                                )
                                            },
                                            label = { Text("MATCHES", fontSize = 10.sp) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = CyberpunkYellow,
                                                selectedTextColor = CyberpunkYellow,
                                                unselectedIconColor = Color(0xFF8E8E9F),
                                                unselectedTextColor = Color(0xFF8E8E9F),
                                                indicatorColor = Color(0x26E5FF00)
                                            ),
                                            modifier = Modifier.testTag("nav_matches")
                                        )

                                        // Ranks Leaderboard Tab
                                        NavigationBarItem(
                                            selected = currentTabState == "leaderboard",
                                            onClick = { currentTabState = "leaderboard" },
                                            icon = {
                                                Icon(
                                                    imageVector = Icons.Default.Leaderboard,
                                                    contentDescription = "Global Rankings"
                                                )
                                            },
                                            label = { Text("RANKS", fontSize = 10.sp) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = CyberpunkYellow,
                                                selectedTextColor = CyberpunkYellow,
                                                unselectedIconColor = Color(0xFF8E8E9F),
                                                unselectedTextColor = Color(0xFF8E8E9F),
                                                indicatorColor = Color(0x26E5FF00)
                                            ),
                                            modifier = Modifier.testTag("nav_leaderboard")
                                        )

                                        // Balance & Wallet Tab
                                        NavigationBarItem(
                                            selected = currentTabState == "wallet",
                                            onClick = { currentTabState = "wallet" },
                                            icon = {
                                                Icon(
                                                    imageVector = Icons.Default.AccountBalanceWallet,
                                                    contentDescription = "Dynamic Wallet Balance"
                                                )
                                            },
                                            label = { Text("WALLET", fontSize = 10.sp) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = CyberpunkYellow,
                                                selectedTextColor = CyberpunkYellow,
                                                unselectedIconColor = Color(0xFF8E8E9F),
                                                unselectedTextColor = Color(0xFF8E8E9F),
                                                indicatorColor = Color(0x26E5FF00)
                                            ),
                                            modifier = Modifier.testTag("nav_wallet")
                                        )
                                    }
                                }
                            }
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(
                                        top = innerPadding.calculateTopPadding(),
                                        bottom = innerPadding.calculateBottomPadding()
                                    )
                            ) {
                                when (currentTabState) {
                                    "home" -> {
                                        HomeScreen(
                                            viewModel = viewModel,
                                            onNavigateToTournament = { id ->
                                                navController.navigate("details/$id")
                                            },
                                            onNavigateToWallet = {
                                                currentTabState = "wallet"
                                            }
                                        )
                                    }
                                    "matches" -> {
                                        MatchesScreen(
                                            viewModel = viewModel,
                                            onNavigateToTournament = { id ->
                                                navController.navigate("details/$id")
                                            }
                                        )
                                    }
                                    "leaderboard" -> {
                                        LeaderboardScreen(viewModel = viewModel)
                                    }
                                    "wallet" -> {
                                        WalletScreen(viewModel = viewModel)
                                    }
                                }
                            }
                        }
                    }

                    // 4. TOURNAMENT COMPREHENSIVE DETAIL DISPLAY PANEL
                    composable("details/{tournamentId}") { backStackEntry ->
                        val tournamentId = backStackEntry.arguments?.getString("tournamentId") ?: ""
                        TournamentDetailsScreen(
                            viewModel = viewModel,
                            tournamentId = tournamentId,
                            onNavigateBack = {
                                navController.navigateUp()
                            }
                        )
                    }
                }
            }
        }
    }
}
