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
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.*
import androidx.navigation.navDeepLink
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.repository.PlatformRepository
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.CyberpunkYellow
import com.example.ui.theme.DeepSpaceBlack
import com.example.ui.viewmodel.PlatformViewModel

class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val viewModel: PlatformViewModel by viewModels()

        setContent {
            MyApplicationTheme {
                val context = LocalContext.current
                val dbError by viewModel.dbErrorDialog.collectAsStateWithLifecycle()
                var currentTabState by remember { mutableStateOf("home") }

                if (dbError != null) {
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { viewModel.clearDbError() },
                        title = { androidx.compose.material3.Text("Supabase Backend Error") },
                        text = { androidx.compose.material3.Text(dbError!!) },
                        confirmButton = {
                            androidx.compose.material3.TextButton(onClick = { viewModel.clearDbError() }) {
                                androidx.compose.material3.Text("OK")
                            }
                        }
                    )
                }

                // Live Toast collector for instant actions feedback
                LaunchedEffect(Unit) {
                    viewModel.toastMessage.collect { message ->
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    }
                }

                val navController = rememberNavController()

                // Global Background with glowing liquid glass orbs
                Box(modifier = Modifier.fillMaxSize().background(com.example.ui.theme.DeepSpaceBlack)) {
                    Box(modifier = Modifier
                        .offset(x = (-80).dp, y = (-20).dp)
                        .size(350.dp)
                        .background(com.example.ui.theme.ElectricBlue.copy(alpha = 0.15f), shape = androidx.compose.foundation.shape.CircleShape)
                        .blur(100.dp)
                    )
                    Box(modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 100.dp, y = 50.dp)
                        .size(400.dp)
                        .background(com.example.ui.theme.CyberpunkYellow.copy(alpha = 0.12f), shape = androidx.compose.foundation.shape.CircleShape)
                        .blur(120.dp)
                    )

                    NavHost(
                        navController = navController,
                        startDestination = "splash",
                        modifier = Modifier.fillMaxSize(),
                        enterTransition = { androidx.compose.animation.fadeIn() },
                        exitTransition = { androidx.compose.animation.fadeOut() },
                        popEnterTransition = { androidx.compose.animation.fadeIn() },
                        popExitTransition = { androidx.compose.animation.fadeOut() }
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
                                        modifier = Modifier.height(72.dp)
                                    ) {
                                        // Home & Matches Tab
                                        NavigationBarItem(
                                            selected = currentTabState == "home",
                                            onClick = { currentTabState = "home" },
                                            icon = {
                                                Icon(
                                                    imageVector = Icons.Default.Home,
                                                    contentDescription = "Home Hub"
                                                )
                                            },
                                            label = { Text("HOME / MATCHES", fontSize = 10.sp) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = CyberpunkYellow,
                                                selectedTextColor = CyberpunkYellow,
                                                unselectedIconColor = Color.White.copy(alpha = 0.6f),
                                                unselectedTextColor = Color.White.copy(alpha = 0.6f),
                                                indicatorColor = Color.Transparent
                                            ),
                                            modifier = Modifier.testTag("nav_home")
                                        )

                                        // Leaderboard Tab
                                        NavigationBarItem(
                                            selected = currentTabState == "leaderboard",
                                            onClick = { currentTabState = "leaderboard" },
                                            icon = {
                                                Icon(
                                                    imageVector = Icons.Default.Leaderboard,
                                                    contentDescription = "Leaderboard"
                                                )
                                            },
                                            label = { Text("RANKS", fontSize = 10.sp) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = CyberpunkYellow,
                                                selectedTextColor = CyberpunkYellow,
                                                unselectedIconColor = Color.White.copy(alpha = 0.6f),
                                                unselectedTextColor = Color.White.copy(alpha = 0.6f),
                                                indicatorColor = Color.Transparent
                                            ),
                                            modifier = Modifier.testTag("nav_leaderboard")
                                        )

                                        // Wallet Tab
                                        NavigationBarItem(
                                            selected = currentTabState == "wallet",
                                            onClick = { currentTabState = "wallet" },
                                            icon = {
                                                Icon(
                                                    imageVector = Icons.Default.AccountBalanceWallet,
                                                    contentDescription = "Wallet Balance"
                                                )
                                            },
                                            label = { Text("WALLET", fontSize = 10.sp) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = CyberpunkYellow,
                                                selectedTextColor = CyberpunkYellow,
                                                unselectedIconColor = Color.White.copy(alpha = 0.6f),
                                                unselectedTextColor = Color.White.copy(alpha = 0.6f),
                                                indicatorColor = Color.Transparent
                                            ),
                                            modifier = Modifier.testTag("nav_wallet")
                                        )

                                        // Profile Tab
                                        NavigationBarItem(
                                            selected = currentTabState == "profile",
                                            onClick = { currentTabState = "profile" },
                                            icon = {
                                                Icon(
                                                    imageVector = Icons.Default.Person,
                                                    contentDescription = "User Profile"
                                                )
                                            },
                                            label = { Text("PROFILE", fontSize = 10.sp) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = CyberpunkYellow,
                                                selectedTextColor = CyberpunkYellow,
                                                unselectedIconColor = Color.White.copy(alpha = 0.6f),
                                                unselectedTextColor = Color.White.copy(alpha = 0.6f),
                                                indicatorColor = Color.Transparent
                                            ),
                                            modifier = Modifier.testTag("nav_profile")
                                        )

                                        // Customer Support Tab
                                        NavigationBarItem(
                                            selected = currentTabState == "support",
                                            onClick = { currentTabState = "support" },
                                            icon = {
                                                Icon(
                                                    imageVector = Icons.Default.SupportAgent,
                                                    contentDescription = "Customer Support"
                                                )
                                            },
                                            label = { Text("SUPPORT", fontSize = 10.sp) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = CyberpunkYellow,
                                                selectedTextColor = CyberpunkYellow,
                                                unselectedIconColor = Color.White.copy(alpha = 0.6f),
                                                unselectedTextColor = Color.White.copy(alpha = 0.6f),
                                                indicatorColor = Color.Transparent
                                            ),
                                            modifier = Modifier.testTag("nav_support")
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
                                    "leaderboard" -> {
                                        LeaderboardScreen(viewModel = viewModel)
                                    }
                                    "wallet" -> {
                                        WalletScreen(viewModel = viewModel)
                                    }
                                    "profile" -> {
                                        ProfileScreen(
                                            viewModel = viewModel,
                                            onLogout = {
                                                navController.navigate("auth") {
                                                    popUpTo(0)
                                                }
                                            }
                                        )
                                    }
                                    "support" -> {
                                        com.example.ui.screens.CustomerSupportScreen(viewModel = viewModel)
                                    }
                                }
                            }
                        }
                    }

                    // 4. TOURNAMENT COMPREHENSIVE DETAIL DISPLAY PANEL
                    composable(
                        "details/{tournamentId}",
                        deepLinks = listOf(navDeepLink { uriPattern = "velorixtournaments://details/{tournamentId}" })
                    ) { backStackEntry ->
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
                } // End of Global Box
            }
        }
    }
}
