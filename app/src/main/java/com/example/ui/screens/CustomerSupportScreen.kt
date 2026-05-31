package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DeepSpaceBlack
import com.example.ui.theme.CyberpunkYellow

@Composable
fun CustomerSupportScreen(viewModel: com.example.ui.viewmodel.PlatformViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "CUSTOMER SUPPORT",
                color = CyberpunkYellow,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "How can we help you today?",
                color = Color.White,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { /* TODO */ },
                colors = ButtonDefaults.buttonColors(containerColor = CyberpunkYellow)
            ) {
                Text("Contact Support (Live Chat)", color = Color.Black)
            }
        }
    }
}
