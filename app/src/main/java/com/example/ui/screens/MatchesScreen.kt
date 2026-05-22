package com.example.ui.screens

import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Tournament
import com.example.ui.components.VeloRixButton
import com.example.ui.theme.CyberpunkYellow
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DeepSpaceBlack
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextGray
import com.example.ui.viewmodel.PlatformViewModel

@Composable
fun MatchesScreen(
    viewModel: PlatformViewModel,
    onNavigateToTournament: (String) -> Unit
) {
    val tournaments by viewModel.tournaments.collectAsState()
    val joinedMatches = tournaments.filter { it.joined }

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("UPCOMING", "COMPLETED")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSpaceBlack)
            .padding(horizontal = 16.dp)
    ) {
        // App Header Title
        Spacer(modifier = Modifier.height(16.dp))
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(
                text = "MY esports CAMPAIGN",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = ElectricBlue,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = "MY MATCHES",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Custom selector TabRow
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.Transparent,
            contentColor = CyberpunkYellow,
            modifier = Modifier.padding(bottom = 20.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    modifier = Modifier.testTag("match_tab_$index"),
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

        if (selectedTabIndex == 0) {
            // UPCOMING REGISTERED MATCHES LIST
            if (joinedMatches.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Gamepad,
                            contentDescription = null,
                            tint = TextGray,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "NO ENROLLED MATCHES YET",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Join hosted tournaments on your Home board!",
                            fontSize = 11.sp,
                            color = TextGray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    items(joinedMatches) { match ->
                        UpcomingJoinedRow(match = match, onClick = { onNavigateToTournament(match.id) })
                    }
                }
            }
        } else {
            // COMPLETED HISTORY
            val completedMatches = listOf(
                CompletedMatchItem(
                    title = "Solo Showdown Rush",
                    game = "BGMI",
                    date = "Yesterday",
                    rank = "#2 Out of 100",
                    reward = "₹250",
                    kills = 12
                ),
                CompletedMatchItem(
                    title = "Bermuda Survivors Cup",
                    game = "Free Fire",
                    date = "Last Sunday",
                    rank = "#45 Out of 50",
                    reward = "₹0",
                    kills = 1
                ),
                CompletedMatchItem(
                    title = "Elite Squad Scrims",
                    game = "BGMI",
                    date = "9 May 2026",
                    rank = "#1 Winner!",
                    reward = "₹500",
                    kills = 15
                )
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                items(completedMatches) { match ->
                    CompletedRow(match)
                }
            }
        }
    }
}

@Composable
fun UpcomingJoinedRow(match: Tournament, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .border(1.dp, ElectricBlue.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("upcoming_match_${match.id}"),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (match.game == "BGMI") ElectricBlue else CyberpunkYellow)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = match.game,
                            color = if (match.game == "BGMI") DeepSpaceBlack else Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ROOM SHARING ACTIVE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonGreen,
                        letterSpacing = 0.5.sp
                    )
                }

                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    tint = CyberpunkYellow,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = match.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${match.mapType} • ${match.perspective} • ${match.dateTimeStr}",
                fontSize = 12.sp,
                color = TextGray,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Room Credentials alert card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF161622))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.MeetingRoom,
                    contentDescription = null,
                    tint = CyberpunkYellow,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "ROOM DETAILS BROADCAST",
                        fontSize = 9.sp,
                        color = TextGray,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Available 15 mins before custom lobby starts",
                        fontSize = 12.sp,
                        color = Color.LightGray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun CompletedRow(match: CompletedMatchItem) {
    val isWin = !match.reward.contains("₹0")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (match.game == "BGMI") ElectricBlue.copy(alpha = 0.2f) else CyberpunkYellow.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = match.game,
                            color = if (match.game == "BGMI") ElectricBlue else CyberpunkYellow,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = match.date,
                        fontSize = 11.sp,
                        color = TextGray,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (isWin) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Victory",
                        tint = CyberpunkYellow,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = match.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Accomplishments and stats summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "YOUR STANDING",
                        fontSize = 9.sp,
                        color = TextGray,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = match.rank,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isWin) CyberpunkYellow else Color.White
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "KILLS",
                        fontSize = 9.sp,
                        color = TextGray,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "${match.kills} Kills",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "WON REWARD",
                        fontSize = 9.sp,
                        color = TextGray,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = match.reward,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isWin) NeonGreen else Color.White
                    )
                }
            }
        }
    }
}

data class CompletedMatchItem(
    val title: String,
    val game: String,
    val date: String,
    val rank: String,
    val reward: String,
    val kills: Int
)
