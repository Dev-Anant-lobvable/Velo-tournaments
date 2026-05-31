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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
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

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
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

            Spacer(modifier = Modifier.height(24.dp))

            if (isSignUpMode) {
                RegistrationScreen(
                    viewModel = viewModel,
                    onAuthSuccess = onAuthSuccess,
                    onSwitchToLogin = { isSignUpMode = false }
                )
            } else {
                LoginScreen(
                    viewModel = viewModel,
                    onAuthSuccess = onAuthSuccess,
                    onSwitchToSignUp = { isSignUpMode = true }
                )
            }
        }
    }
}

@Composable
fun LoginScreen(
    viewModel: PlatformViewModel,
    onAuthSuccess: () -> Unit,
    onSwitchToSignUp: () -> Unit
) {
    var phoneOrEmail by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loginMethod by remember { mutableStateOf("email") } // "email" or "phone"
    var showOtpField by remember { mutableStateOf(false) }
    var otpCode by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Method Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(Color(0xFF111111), RoundedCornerShape(12.dp))
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        if (loginMethod == "email") Color(0xFF222222) else Color.Transparent,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { loginMethod = "email"; phoneOrEmail = ""; showOtpField = false },
                contentAlignment = Alignment.Center
            ) {
                Text("Email", color = if (loginMethod == "email") CyberpunkYellow else TextGray, fontWeight = FontWeight.Bold)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        if (loginMethod == "phone") Color(0xFF222222) else Color.Transparent,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { loginMethod = "phone"; phoneOrEmail = ""; showOtpField = false },
                contentAlignment = Alignment.Center
            ) {
                Text("Phone", color = if (loginMethod == "phone") CyberpunkYellow else TextGray, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = phoneOrEmail,
            onValueChange = { phoneOrEmail = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .testTag("phone_email_input"),
            label = { Text(if (loginMethod == "phone") "Phone Number (e.g. +1234...)" else "Email Address") },
            leadingIcon = {
                Icon(
                    imageVector = if (loginMethod == "email") Icons.Default.Mail else Icons.Default.Phone,
                    contentDescription = null,
                    tint = ElectricBlue
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = if (loginMethod == "phone") KeyboardType.Phone else KeyboardType.Email
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

        AnimatedVisibility(visible = !showOtpField) {
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
        }

        AnimatedVisibility(visible = showOtpField) {
            OutlinedTextField(
                value = otpCode,
                onValueChange = { otpCode = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .testTag("otp_input"),
                label = { Text("Enter 6-digit OTP") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = ElectricBlue)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

        // Submit Button
        VeloRixButton(
            text = if (showOtpField) "Verify OTP & Login" else "Login",
            onClick = {
                if (showOtpField) {
                    viewModel.login(phoneOrEmail, password, loginMethod, onComplete = onAuthSuccess) // Submitting password as OTP or actual password depending on logic
                } else if (loginMethod == "phone" && password.isEmpty()) {
                    showOtpField = true // Simulate OTP step
                } else {
                    viewModel.login(phoneOrEmail, password, loginMethod, onComplete = onAuthSuccess)
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            accentColor = CyberpunkYellow,
            glowColor = CyberpunkYellow,
            testTag = "submit_login_button"
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        // OR Divider
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f).height(1.dp).background(Color(0xFF333333)))
            Text(" OR ", color = TextGray, style = MaterialTheme.typography.bodySmall)
            Box(modifier = Modifier.weight(1f).height(1.dp).background(Color(0xFF333333)))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Google Login Button
        AuthOptionButton(
            text = "Continue with Google",
            icon = Icons.Default.Email, // Placeholder
            onClick = { viewModel.loginWithGoogle(onComplete = onAuthSuccess) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Don't have an account? Sign Up",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = ElectricBlue,
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier
                .clickable { onSwitchToSignUp() }
                .padding(8.dp)
                .align(Alignment.CenterHorizontally)
                .testTag("toggle_auth_mode")
        )
    }
}

@Composable
fun RegistrationScreen(
    viewModel: PlatformViewModel,
    onAuthSuccess: () -> Unit,
    onSwitchToLogin: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var phoneOrEmail by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loginMethod by remember { mutableStateOf("email") }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Method Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(Color(0xFF111111), RoundedCornerShape(12.dp))
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        if (loginMethod == "email") Color(0xFF222222) else Color.Transparent,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { loginMethod = "email"; phoneOrEmail = "" },
                contentAlignment = Alignment.Center
            ) {
                Text("Email", color = if (loginMethod == "email") CyberpunkYellow else TextGray, fontWeight = FontWeight.Bold)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        if (loginMethod == "phone") Color(0xFF222222) else Color.Transparent,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { loginMethod = "phone"; phoneOrEmail = "" },
                contentAlignment = Alignment.Center
            ) {
                Text("Phone", color = if (loginMethod == "phone") CyberpunkYellow else TextGray, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

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

        OutlinedTextField(
            value = phoneOrEmail,
            onValueChange = { phoneOrEmail = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .testTag("phone_email_input"),
            label = { Text(if (loginMethod == "phone") "Phone Number (e.g. +1234...)" else "Email Address") },
            leadingIcon = {
                Icon(
                    imageVector = if (loginMethod == "email") Icons.Default.Mail else Icons.Default.Phone,
                    contentDescription = null,
                    tint = ElectricBlue
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = if (loginMethod == "phone") KeyboardType.Phone else KeyboardType.Email
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
            label = { Text("Create Password") },
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
        VeloRixButton(
            text = "Register",
            onClick = {
                viewModel.register(username, phoneOrEmail, password, loginMethod, onComplete = onAuthSuccess)
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            accentColor = CyberpunkYellow,
            glowColor = CyberpunkYellow,
            testTag = "submit_register_button"
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        // OR Divider
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f).height(1.dp).background(Color(0xFF333333)))
            Text(" OR ", color = TextGray, style = MaterialTheme.typography.bodySmall)
            Box(modifier = Modifier.weight(1f).height(1.dp).background(Color(0xFF333333)))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Google Login Button
        AuthOptionButton(
            text = "Continue with Google",
            icon = Icons.Default.Email, // Placeholder
            onClick = { viewModel.loginWithGoogle(onComplete = onAuthSuccess) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Already have an account? Log In",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = ElectricBlue,
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier
                .clickable { onSwitchToLogin() }
                .padding(8.dp)
                .align(Alignment.CenterHorizontally)
                .testTag("toggle_auth_mode")
        )
    }
}

@Composable
fun AuthOptionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    androidx.compose.material3.OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .testTag("google_login_button"),
        shape = RoundedCornerShape(16.dp),
        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
            contentColor = Color.White
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF333333))
    ) {
        Icon(icon, contentDescription = null, tint = Color.White)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold))
    }
}

