package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.VeloRixButton
import com.example.ui.theme.CyberpunkYellow
import com.example.ui.theme.DeepSpaceBlack
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.TextGray
import com.example.ui.viewmodel.PlatformViewModel

@Composable
fun AuthScreen(
    viewModel: PlatformViewModel,
    onAuthSuccess: () -> Unit
) {
    var isSignUpMode by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var phoneOrEmail by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSpaceBlack)
            .systemBarsPadding()
            .verticalScroll(scrollState)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp)
        ) {
            // VeloRix Mini esports logo representation
            Image(
                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.velorix_logo_image),
                contentDescription = "VeloRix Logo",
                modifier = Modifier
                    .size(140.dp)
                    .padding(bottom = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "VELORIX TOURNAMENTS",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp,
                    color = Color.White
                )
            )

            Text(
                text = if (isSignUpMode) "JOIN THE ARENA" else "LOGIN TO PLAY",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = ElectricBlue,
                    letterSpacing = 4.sp,
                    fontWeight = FontWeight.Black
                )
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Text inputs
            AnimatedVisibility(
                visible = isSignUpMode,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .testTag("username_input"),
                        label = { Text("Gamertag / Username") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = ElectricBlue)
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberpunkYellow,
                            unfocusedBorderColor = Color(0xFF333333),
                            focusedLabelColor = CyberpunkYellow,
                            unfocusedLabelColor = TextGray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF111111),
                            unfocusedContainerColor = Color(0xFF111111)
                        ),
                        singleLine = true
                    )
                }
            }

            OutlinedTextField(
                value = phoneOrEmail,
                onValueChange = { phoneOrEmail = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .testTag("phone_email_input"),
                label = { Text("Phone Number or Email") },
                leadingIcon = {
                    Icon(
                        imageVector = if (phoneOrEmail.contains("@")) Icons.Default.Mail else Icons.Default.Phone,
                        contentDescription = null,
                        tint = ElectricBlue
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberpunkYellow,
                    unfocusedBorderColor = Color(0xFF333333),
                    focusedLabelColor = CyberpunkYellow,
                    unfocusedLabelColor = TextGray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF111111),
                    unfocusedContainerColor = Color(0xFF111111)
                ),
                singleLine = true
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .testTag("password_input"),
                label = { Text("Enter Password") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = ElectricBlue)
                },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberpunkYellow,
                    unfocusedBorderColor = Color(0xFF333333),
                    focusedLabelColor = CyberpunkYellow,
                    unfocusedLabelColor = TextGray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF111111),
                    unfocusedContainerColor = Color(0xFF111111)
                ),
                singleLine = true
            )

            // Submit Button
            val buttonText = if (isSignUpMode) "Register & Verify OTP" else "Login / Verify OTP"
            VeloRixButton(
                text = buttonText,
                onClick = {
                    if (isSignUpMode) {
                        viewModel.register(username, phoneOrEmail, password, onComplete = onAuthSuccess)
                    } else {
                        viewModel.login(phoneOrEmail, password, onComplete = onAuthSuccess)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                accentColor = CyberpunkYellow,
                glowColor = CyberpunkYellow,
                testTag = "submit_auth_button"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Switch layout
            val footerPrompt = if (isSignUpMode) {
                "Already have an account? Log In"
            } else {
                "Don't have an account? Sign Up"
            }
            Text(
                text = footerPrompt,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = ElectricBlue,
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier
                    .clickable { isSignUpMode = !isSignUpMode }
                    .padding(8.dp)
                    .testTag("toggle_auth_mode")
            )
        }
    }
}
