package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val PinkishRedAccent = Color(0xFFE91E63)
val DeepSpaceBlack = Color(0xFF0D0D12)
val CardSurfaceLight = Color(0xFF1E1E24)
val TextGray = Color(0xFFA0A0A5)

@Composable
fun TournamentCard(
    thumbnailUrl: String,
    title: String,
    entryFee: Double,
    prizePool: Double,
    filledSlots: Int,
    maxSlots: Int,
    isJoined: Boolean = false,
    onClick: () -> Unit,
    onJoinClick: () -> Unit
) {
    val isFull = filledSlots >= maxSlots
    val rawProgress = if (maxSlots > 0) filledSlots.toFloat() / maxSlots.toFloat() else 0f
    val progress = rawProgress.coerceIn(0f, 1f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .clickable { onClick() }
            .testTag("custom_tournament_card"),
        colors = CardDefaults.cardColors(containerColor = CardSurfaceLight),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Thumbnail Image Container (Using gradient to avoid emulator Coil crashes)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = if (title.contains("BGMI", ignoreCase = true) || title.contains("PUBG", ignoreCase = true)) {
                                listOf(Color(0xFF2E3192), Color(0xFF1BFFFF))
                            } else {
                                listOf(Color(0xFFFF416C), Color(0xFFFF4B2B))
                            }
                        )
                    )
            ) {
                
                // Status BADGE
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (isJoined) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF4CAF50).copy(alpha = 0.9f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "JOINED",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else if (isFull) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF9E9E9E).copy(alpha = 0.9f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "FULL",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Card Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Metadata Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "PRIZE POOL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextGray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "VT ${prizePool.toInt()}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = PinkishRedAccent
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "ENTRY FEE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextGray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (entryFee == 0.0) "FREE" else "VT ${entryFee.toInt()}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "SLOTS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextGray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$filledSlots/$maxSlots",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Slots Progress Bar
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = PinkishRedAccent,
                    trackColor = Color(0xFF2A2A35)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Join Button
                if (!isJoined) {
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    val buttonScale by animateFloatAsState(
                        targetValue = if (isPressed) 0.95f else 1f,
                        animationSpec = tween(durationMillis = 150)
                    )

                    Button(
                        onClick = onJoinClick,
                        enabled = !isFull,
                        interactionSource = interactionSource,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .graphicsLayer {
                                scaleX = buttonScale
                                scaleY = buttonScale
                            }
                            .shadow(
                                elevation = if (!isFull) 16.dp else 0.dp,
                                shape = CircleShape,
                                spotColor = PinkishRedAccent,
                                ambientColor = PinkishRedAccent
                            )
                            .testTag("join_now_action_button"),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PinkishRedAccent,
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFF3A3A45),
                            disabledContentColor = Color(0xFF888888)
                        )
                    ) {
                        Text(
                            text = if (isFull) "Slots Full" else "Join Now",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = onClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("view_details_button"),
                        border = BorderStroke(1.dp, PinkishRedAccent),
                        shape = CircleShape,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = PinkishRedAccent,
                            containerColor = Color.Transparent
                        )
                    ) {
                        Text(
                            text = "REGISTERED • VIEW DETAILS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}

