package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Tournament
import com.example.ui.components.VeloRixButton
import com.example.ui.components.TournamentCard
import com.example.ui.theme.CyberpunkYellow
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceGlass
import com.example.ui.theme.DeepSpaceBlack
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextGray
import com.example.ui.viewmodel.PlatformViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: PlatformViewModel,
    onNavigateToTournament: (String) -> Unit,
    onNavigateToWallet: () -> Unit
) {
    val user by viewModel.userState.collectAsState()
    val tournaments by viewModel.tournaments.collectAsState()
    val isRefreshing by viewModel.isRefreshingHome.collectAsState()

    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var searchFocused by remember { mutableStateOf(false) }
    val searchHistory by viewModel.searchHistory.collectAsState()
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val categories = listOf("All", "BGMI", "Free Fire")

    val speechRecognizerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val data = result.data
            val results = data?.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)
            val spokenText = results?.getOrNull(0) ?: ""
            searchQuery = spokenText
            viewModel.saveSearchQuery(spokenText)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSpaceBlack)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // Top User profile and Wallet details bar
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Dynamic themed avatar circle based on seed index
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            ElectricBlue,
                                            CyberpunkYellow
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.velorix_logo_image),
                                contentDescription = "Avatar Logo",
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = user?.username ?: "Warrior",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Verified",
                                    tint = ElectricBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "READY FOR BATTLE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberpunkYellow,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    // Balance container pill on the right
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFF222232))
                            .clickable { onNavigateToWallet() }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = "Wallet",
                            tint = CyberpunkYellow,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "₹${user?.balance?.toInt() ?: 0}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add funds icon",
                            tint = CyberpunkYellow,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Hero Promotion Banners (Horizontally scrollable)
            item {
                Text(
                    text = "HOT EVENTS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val banners = listOf(
                        BannerItem(
                            title = "Mega BGMI Clash at 8 PM",
                            prize = "₹10,000",
                            tag = "SPECIAL",
                            backgroundGradient = Brush.linearGradient(
                                colors = listOf(Color(0xFFEE2B4B), Color(0xFFA1102A))
                            ),
                            gameImg = "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=500&auto=format&fit=crop"
                        ),
                        BannerItem(
                            title = "Free Fire Pro Squad Battle",
                            prize = "₹5,000",
                            tag = "FREE ENTRY",
                            backgroundGradient = Brush.linearGradient(
                                colors = listOf(Color(0xFF333333), Color(0xFF111111))
                            ),
                            gameImg = "https://images.unsplash.com/photo-1552820728-8b83bb6b773f?w=500&auto=format&fit=crop"
                        ),
                        BannerItem(
                            title = "Weekend Sniper Only Arena",
                            prize = "₹2,500",
                            tag = "SOLO",
                            backgroundGradient = Brush.linearGradient(
                                colors = listOf(Color(0xFFEE2B4B), Color(0xFF111111))
                            ),
                            gameImg = "https://images.unsplash.com/photo-1511512578047-dfb367046420?w=500&auto=format&fit=crop"
                        )
                    )

                    items(banners) { banner ->
                        Column(
                            modifier = Modifier
                                .width(280.dp)
                                .height(140.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(banner.backgroundGradient)
                                .clickable { }
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50.dp))
                                        .background(Color.Black.copy(alpha = 0.5f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = banner.tag,
                                        fontSize = 10.sp,
                                        color = CyberpunkYellow,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = CyberpunkYellow,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = banner.title,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    lineHeight = 22.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "WIN PRIZE POOL ${banner.prize}",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Categories Filter chip row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "BROWSE MATCHES",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 2.sp
                    )

                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter",
                        tint = TextGray,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        val isSelected = selectedCategory == category
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(if (isSelected) CyberpunkYellow else Color(0xFF1E1E2A))
                                .clickable { selectedCategory = category }
                                .padding(horizontal = 22.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = category.toUpperCase(Locale.current),
                                color = if (isSelected) DeepSpaceBlack else Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by title...", color = TextGray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .onFocusChanged { searchFocused = it.isFocused },
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberpunkYellow,
                        unfocusedBorderColor = Color(0xFF1E1E2A),
                        focusedContainerColor = Color(0xFF1E1E2A),
                        unfocusedContainerColor = Color(0xFF1E1E2A),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            viewModel.saveSearchQuery(searchQuery)
                            focusManager.clearFocus()
                        }
                    ),
                    trailingIcon = {
                        IconButton(onClick = {
                            val intent = android.content.Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            }
                            speechRecognizerLauncher.launch(intent)
                        }) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Mic,
                                contentDescription = "Voice Search",
                                tint = CyberpunkYellow
                            )
                        }
                    }
                )

                if (searchFocused && searchHistory.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF161622)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Recent Searches", color = TextGray, fontSize = 11.sp, modifier = Modifier.padding(start = 8.dp, bottom = 4.dp))
                            searchHistory.forEach { historyQuery ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            searchQuery = historyQuery
                                            viewModel.saveSearchQuery(historyQuery)
                                            focusManager.clearFocus()
                                        }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, tint = TextGray, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(historyQuery, color = Color.White, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // Upcoming Matches list
            val filteredTournaments = tournaments.filter { t ->
                val matchesCategory = selectedCategory == "All" || t.game == selectedCategory
                val matchesSearch = searchQuery.isBlank() || t.title.contains(searchQuery, ignoreCase = true)
                matchesCategory && matchesSearch
            }

            if (filteredTournaments.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Gamepad,
                            contentDescription = "No matches",
                            tint = TextGray,
                            modifier = Modifier.size(50.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "NO HOSTED MATCHES AVAILABLE",
                            fontSize = 12.sp,
                            color = TextGray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                items(filteredTournaments) { match ->
                    TournamentCard(
                        match = match,
                        onClick = { onNavigateToTournament(match.id) },
                        onJoinClick = {
                            viewModel.registerForTournament(match.id)
                        }
                    )
                }
            }
        }
    }
}

// Simple Helper Model
data class BannerItem(
    val title: String,
    val prize: String,
    val tag: String,
    val backgroundGradient: Brush,
    val gameImg: String
)
