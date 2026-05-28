package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Tournament
import com.example.ui.theme.CyberpunkYellow
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DeepSpaceBlack
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.TextGray

@Composable
fun TournamentCard(
    match: Tournament,
    onClick: () -> Unit,
    onJoinClick: () -> Unit
) {
    val rawProgress = if (match.maxSlots > 0) match.filledSlots.toFloat() / match.maxSlots.toFloat() else 0f
    val progress = rawProgress.coerceIn(0f, 1f)
    
    val scale = remember { androidx.compose.animation.core.Animatable(0.9f) }
    val offsetX = remember { androidx.compose.animation.core.Animatable(10f) }
    
    LaunchedEffect(Unit) {
        scale.animateTo(1f, androidx.compose.animation.core.tween(300, easing = androidx.compose.animation.core.FastOutSlowInEasing))
        repeat(4) {
            offsetX.animateTo(((-3)..3).random().toFloat(), androidx.compose.animation.core.tween(40))
        }
        offsetX.animateTo(0f, androidx.compose.animation.core.tween(40))
    }

    // Determine stylized gradient cover representing the game type (cyberpunk feel)
    val coverGradient = if (match.game == "BGMI") {
        androidx.compose.ui.graphics.Brush.horizontalGradient(colors = listOf(Color(0xFF333333), Color(0xFF111111)))
    } else {
        androidx.compose.ui.graphics.Brush.horizontalGradient(colors = listOf(Color(0xFFEE2B4B), Color(0xFFA1102A)))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .graphicsLayer { 
                scaleX = scale.value
                scaleY = scale.value
                translationX = offsetX.value 
            }
            .border(
                1.dp,
                if (match.joined) ElectricBlue.copy(alpha = 0.5f) else Color.Transparent,
                RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .testTag("tournament_card_${match.id}"),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Card Cover Image / Gradient Thumbnail
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(coverGradient)
            ) {
                // Removed Coil Image to stop unrecoverable crashes

                // Gaming tag overlays
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(if (match.game == "BGMI") ElectricBlue else CyberpunkYellow)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = match.game,
                            color = if (match.game == "BGMI") DeepSpaceBlack else Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    if (match.joined) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(ElectricBlue)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "JOINED",
                                color = DeepSpaceBlack,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else if (match.isFull) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(Color.Red)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "FULL",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Match Metadata overlay on bottom
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomStart)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = match.dateTimeStr,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${match.mapType} • ${match.perspective}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CyberpunkYellow
                    )
                }
            }

            // Card Body Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = match.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Stats rows (Entry fee, Prize, Slots)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "PRIZE POOL",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextGray
                        )
                        Text(
                            text = "₹${match.prizePool.toInt()}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = ElectricBlue
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "ENTRY FEE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextGray
                        )
                        Text(
                            text = if (match.entryFee == 0.0) "FREE" else "₹${match.entryFee.toInt()}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = CyberpunkYellow
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "SLOTS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextGray
                        )
                        Text(
                            text = "${match.filledSlots}/${match.maxSlots}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Progress Bar showing slots status
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(50.dp)),
                    color = if (progress >= 0.9f) Color.Red else ElectricBlue,
                    trackColor = Color(0xFF242435)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Glowing Join button
                if (!match.joined) {
                    VeloRixButton(
                        text = if (match.isFull) "Slots Full" else "Join Now",
                        onClick = onJoinClick,
                        enabled = !match.isFull,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        accentColor = if (match.isFull) Color.Gray else CyberpunkYellow,
                        glowColor = CyberpunkYellow,
                        testTag = "join_now_button_${match.id}"
                    )
                } else {
                    OutlinedButton(
                        onClick = onClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("match_joined_indicator_${match.id}"),
                        border = BorderStroke(1.5.dp, ElectricBlue),
                        shape = RoundedCornerShape(50.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ElectricBlue
                        )
                    ) {
                        Text(
                            text = "REGISTERED • DETAILS",
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}
