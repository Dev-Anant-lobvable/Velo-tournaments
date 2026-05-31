package com.example.ui.screens

import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Tournament
import com.example.ui.components.VeloRixButton
import com.example.ui.theme.CyberpunkYellow
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DeepSpaceBlack
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.TextGray
import com.example.ui.viewmodel.PlatformViewModel
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.compose.ui.graphics.drawscope.Stroke
import android.widget.Toast

fun simulateLocalNotification(context: Context, matchTitle: String) {
    val channelId = "tournament_reminders"
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            "Tournament Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alerts 15 mins before tournament starts"
        }
        notificationManager.createNotificationChannel(channel)
    }

    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("Tournament Starts in 15 Min!")
        .setContentText("Room ID and Password are now available for $matchTitle")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .build()

    notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    Toast.makeText(context, "Notification Sent! (Check notification tray)", Toast.LENGTH_SHORT).show()
}

@Composable
fun TournamentDetailsScreen(
    viewModel: PlatformViewModel,
    tournamentId: String,
    onNavigateBack: () -> Unit
) {
    LaunchedEffect(key1 = tournamentId) {
        viewModel.selectTournament(tournamentId)
    }

    val match by viewModel.selectedTournament.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("DETAILS", "PRIZE POOL", "RULES")

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        match?.let { t ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(bottom = 90.dp) // space for sticky button
            ) {
                // Parallax-style Gradient Header Cover
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                ) {
                    val coverBg = when (t.bannerIdx) {
                        1 -> Brush.verticalGradient(colors = listOf(Color(0xFF333333), DeepSpaceBlack))
                        2 -> Brush.verticalGradient(colors = listOf(Color(0xFFEE2B4B), DeepSpaceBlack))
                        else -> Brush.verticalGradient(colors = listOf(CyberpunkYellow.copy(alpha=0.5f), DeepSpaceBlack))
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(coverBg)
                    )

                    // Overlay bottom shader
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        DeepSpaceBlack.copy(alpha = 0.5f),
                                        DeepSpaceBlack
                                    )
                                )
                            )
                    )

                    // Top Bar back button icon overlay
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .padding(top = 40.dp, start = 16.dp)
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50.dp))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go Back",
                            tint = Color.White
                        )
                    }

                    // Game badge overlays
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (t.game == "BGMI") ElectricBlue else CyberpunkYellow)
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = t.game,
                            color = if (t.game == "BGMI") DeepSpaceBlack else Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // Title Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = t.title,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF161622))
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = CyberpunkYellow,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "TOTAL TOURNAMENT PRIZE",
                                fontSize = 10.sp,
                                color = TextGray,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "VT ${t.prizePool.toInt()}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = CyberpunkYellow
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Tabbed Layout selectors
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.Transparent,
                    contentColor = CyberpunkYellow,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = if (selectedTabIndex == index) CyberpunkYellow else TextGray
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Tab Content Rendering
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    when (selectedTabIndex) {
                        0 -> DetailsTabContent(t)
                        1 -> PrizePoolTabContent(t)
                        2 -> RulesTabContent()
                    }
                }
            }

            // Sticky Bottom Join/Action Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, DeepSpaceBlack.copy(alpha = 0.95f), DeepSpaceBlack)
                        )
                    )
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 20.dp, top = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "ENTRY CHARGE",
                            fontSize = 10.sp,
                            color = TextGray,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (t.entryFee == 0.0) "FREE" else "VT ${t.entryFee.toInt()}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = ElectricBlue
                        )
                    }

                    if (!t.joined) {
                        VeloRixButton(
                            text = if (t.isFull) "Slots Full" else "Join Match",
                            onClick = { viewModel.registerForTournament(t.id) },
                            enabled = !t.isFull,
                            modifier = Modifier
                                .width(200.dp)
                                .height(50.dp),
                            accentColor = if (t.isFull) Color.Gray else CyberpunkYellow,
                            glowColor = CyberpunkYellow,
                            testTag = "sticky_join_button"
                        )
                    } else {
                        Button(
                            onClick = {},
                            modifier = Modifier
                                .width(200.dp)
                                .height(50.dp)
                                .testTag("registered_sticky_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1B4E3E),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(50.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("REGISTERED", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CyberpunkYellow)
            }
        }
    }
}

@Composable
fun DetailsTabContent(match: Tournament) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DetailItemCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Event,
                title = "DATE & TIME",
                value = match.dateTimeStr
            )
            DetailItemCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Map,
                title = "MAP",
                value = match.mapType
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DetailItemCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Visibility,
                title = "PERSPECTIVE",
                value = match.perspective
            )
            DetailItemCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.People,
                title = "TEAM TYPE",
                value = "SOLO ARENA"
            )
        }

        // Additional Match Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "TOURNAMENT INFO",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = ElectricBlue,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Welcome to the ultimate esports arena! Join top-tier players in ${match.game} and prove your skills. Room ID and custom passwords will be displayed in the matches section and sent dynamically via notifications 15 to 20 minutes before the official countdown starts.",
                    fontSize = 13.sp,
                    color = Color.LightGray,
                    lineHeight = 18.sp
                )
                
                if (match.joined) {
                    Spacer(modifier = Modifier.height(16.dp))
                    val context = LocalContext.current
                    OutlinedButton(
                        onClick = { simulateLocalNotification(context, match.title) },
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        shape = RoundedCornerShape(50.dp),
                        border = BorderStroke(1.5.dp, CyberpunkYellow),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberpunkYellow)
                    ) {
                        Icon(imageVector = Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "SIMULATE 15-MIN ALERT", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun DetailItemCard(
    modifier: Modifier = Modifier,
    modifierIcon: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ElectricBlue,
                modifier = modifierIcon.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 10.sp,
                color = TextGray,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun PrizePoolTabContent(match: Tournament) {
    val level1 = match.prizePool * 0.5
    val level2 = match.prizePool * 0.25
    val level3 = match.prizePool * 0.15
    val level4_10 = match.prizePool * 0.10 / 7

    val rankingPrizes = listOf(
        RankPrize("Rank 1 (Winner)", level1),
        RankPrize("Rank 2", level2),
        RankPrize("Rank 3", level3),
        RankPrize("Rank 4 - 10", level4_10),
        RankPrize("Per Kill Bounty", 10.0) // constant sample
    )
    
    val totalPrizeForChart = rankingPrizes.take(4).sumOf { it.amount } 

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "PRIZE SPLIT DISTRIBUTION",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = CyberpunkYellow
                )
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            // Pie Chart implementation
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                androidx.compose.foundation.Canvas(modifier = Modifier.size(160.dp)) {
                    var startAngle = 0f
                    val colors = listOf(Color(0xFFE5C07B), Color(0xFFC0C0C0), Color(0xFFCD7F32), ElectricBlue)
                    rankingPrizes.take(4).forEachIndexed { index, prize ->
                        var rawSweep = 0f
                        if (totalPrizeForChart > 0) {
                            rawSweep = (prize.amount.toFloat() / totalPrizeForChart.toFloat()) * 360f
                        } else if (index == 0) {
                            rawSweep = 360f // default edge case
                        }
                        val sweepAngle = rawSweep.takeIf { !it.isNaN() }?.coerceIn(0f, 360f) ?: 0f
                        drawArc(
                            color = colors.getOrElse(index) { Color.Gray },
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(width = 40.dp.toPx())
                        )
                        startAngle += sweepAngle
                    }
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "TOTAL", fontSize = 10.sp, color = TextGray, fontWeight = FontWeight.Bold)
                    Text(text = "VT ${match.prizePool.toInt()}", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Black)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            rankingPrizes.forEachIndexed { idx, p ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val iconColor = when (idx) {
                            0 -> Color(0xFFFFFFFF)
                            1 -> Color(0xFFC0C0C0)
                            2 -> Color(0xFFCD7F32)
                            else -> ElectricBlue
                        }
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = p.rank,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Text(
                        text = "VT ${p.amount.toInt()}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = CyberpunkYellow
                    )
                }

                if (idx < rankingPrizes.size - 1) {
                    HorizontalDivider(color = Color(0xFF242435))
                }
            }
        }
    }
}

@Composable
fun RulesTabContent() {
    val rules = listOf(
        "No Emulator play is allowed. Standard mobile touch interface only.",
        "Teaming up with rival players will lead to instant disqualification and wallet ban.",
        "Hackers, scripts, or exploit users will receive permanent account suspensions.",
        "The Room ID and Password will be broadcasted exactly 15 minutes prior to start time.",
        "Ensure your game app is pre-updated and you enter the lobby within the 10-minute grace window.",
        "All bounty/kill winnings will be synthesized and added instantly post Match Referee verification."
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "STRICT REGULATIONS",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = Color.Red
                )
            )
            Spacer(modifier = Modifier.height(14.dp))

            rules.forEach { rule ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "• ",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberpunkYellow
                    )
                    Text(
                        text = rule,
                        fontSize = 13.sp,
                        color = Color.LightGray,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

data class RankPrize(val rank: String, val amount: Double)
val White = Color.White
