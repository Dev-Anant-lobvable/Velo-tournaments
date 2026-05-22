package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.DeepSpaceBlack
import com.example.ui.viewmodel.PlatformViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    viewModel: PlatformViewModel,
    onNavigateToAuth: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()

    // Pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "splash_pulsing")
    
    // Progress bar animation
    val progressAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing)
        ),
        label = "progress_bar"
    )

    LaunchedEffect(key1 = true) {
        delay(2600) // loading delay
        if (isLoggedIn) {
            onNavigateToHome()
        } else {
            onNavigateToAuth()
        }
    }

    val velorixRed = Color(0xFFC70039) // Deep crimson red for the theme
    val darkRed = Color(0xFF420012)
    val almostBlack = Color(0xFF070505)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        darkRed,
                        almostBlack
                    ),
                    radius = 1200f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Glowing Circle border containing the logo
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(110.dp)
                    .border(1.dp, velorixRed.copy(alpha = 0.5f), CircleShape)
                    .padding(8.dp)
                    .border(1.dp, velorixRed, CircleShape)
                    .clip(CircleShape)
                    .background(Color.Black)
            ) {
                // We use a placeholder image for the user's logo attachment (2nd image)
                // The user will replace this in the res/drawable folder
                Image(
                    painter = painterResource(id = R.drawable.velorix_logo), // The user generated app logo
                    contentDescription = "Velorix App Logo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Brand Text Header with paired typographic accents
            Text(
                text = "VELORIX",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 6.sp,
                    color = velorixRed
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "LOADING THE ARENA",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = velorixRed.copy(alpha = 0.7f),
                    letterSpacing = 4.sp,
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Small glowing loading bar
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(velorixRed.copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = progressAnim)
                        .fillMaxHeight()
                        .background(velorixRed)
                )
            }
        }
    }
}
