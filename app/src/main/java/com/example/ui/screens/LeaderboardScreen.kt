package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LeaderboardPlayer
import com.example.ui.theme.Bronze
import com.example.ui.theme.CyberpunkYellow
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DeepSpaceBlack
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.Gold
import com.example.ui.theme.Silver
import com.example.ui.theme.TextGray
import com.example.ui.viewmodel.PlatformViewModel

@Composable
fun LeaderboardScreen(viewModel: PlatformViewModel) {
    val players by viewModel.leaderboard.collectAsState()

    // Separate Top 3 for the elegant podium
    val top1 = players.find { it.rank == 1 }
    val top2 = players.find { it.rank == 2 }
    val top3 = players.find { it.rank == 3 }

    // Ranks 4 to 100
    val restOfPlayers = players.filter { it.rank > 3 }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSpaceBlack)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // App Header Title
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = "VELORIX CHAMPIONS",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = ElectricBlue,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "LEADERBOARD",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Top 3 Podium component
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TOP 3 WARRIORS",
                        fontSize = 11.sp,
                        color = CyberpunkYellow,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // Rank 2 Podium (Left)
                        top2?.let {
                            PodiumColumn(
                                player = it,
                                podiumColor = Silver,
                                height = 110,
                                modifier = Modifier.weight(1f)
                            )
                        } ?: Spacer(modifier = Modifier.weight(1f))

                        // Rank 1 Podium (Center - Tallest)
                        top1?.let {
                            PodiumColumn(
                                player = it,
                                podiumColor = Gold,
                                height = 150,
                                modifier = Modifier.weight(1.2f)
                            )
                        } ?: Spacer(modifier = Modifier.weight(1.2f))

                        // Rank 3 Podium (Right)
                        top3?.let {
                            PodiumColumn(
                                player = it,
                                podiumColor = Bronze,
                                height = 95,
                                modifier = Modifier.weight(1f)
                            )
                        } ?: Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Leaderboard stats header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, top = 0.dp, end = 8.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "RANKING LIST", fontSize = 11.sp, color = TextGray, fontWeight = FontWeight.Bold)
                Text(text = "TOTAL EARNINGS", fontSize = 11.sp, color = TextGray, fontWeight = FontWeight.Bold)
            }
        }

        // Ranks 4 to 100 rows
        items(restOfPlayers) { player ->
            LeaderboardRow(player)
        }
    }
}

@Composable
fun PodiumColumn(
    player: LeaderboardPlayer,
    podiumColor: Color,
    height: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        // Player avatar with dynamic spot border
        Box(
            modifier = Modifier
                .size(if (player.rank == 1) 56.dp else 48.dp)
                .clip(CircleShape)
                .border(2.dp, podiumColor, CircleShape)
                .background(Color(0xFF242435)),
            contentAlignment = Alignment.Center
        ) {
            val emoji = when (player.avatarIdx) {
                1 -> "🦊"
                2 -> "🦁"
                3 -> "🐯"
                4 -> "🦖"
                else -> "🦅"
            }
            Text(text = emoji, fontSize = if (player.rank == 1) 28.sp else 22.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = player.username,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(90.dp)
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "₹${player.totalWinnings.toInt()}",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = CyberpunkYellow
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Physical Podium Base Block
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height.dp)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            podiumColor.copy(alpha = 0.85f),
                            podiumColor.copy(alpha = 0.25f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "#${player.rank}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = DeepSpaceBlack
                )
                Text(
                    text = "${player.kills} KILLS",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = DeepSpaceBlack.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun LeaderboardRow(player: LeaderboardPlayer) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .testTag("leaderboard_row_${player.rank}"),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Rank label
                Text(
                    text = "#${player.rank}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = ElectricBlue,
                    modifier = Modifier.width(36.dp)
                )

                // Avatar
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF242435)),
                    contentAlignment = Alignment.Center
                ) {
                    val emoji = when (player.avatarIdx) {
                        1 -> "⚡"
                        2 -> "💥"
                        3 -> "🐺"
                        4 -> "🐉"
                        else -> "⚔️"
                    }
                    Text(text = emoji, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Username details
                Column {
                    Text(
                        text = player.username,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${player.kills} TOTAL KILLS",
                        fontSize = 10.sp,
                        color = TextGray,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Total winnings
            Text(
                text = "₹${player.totalWinnings.toInt()}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = CyberpunkYellow
            )
        }
    }
}
