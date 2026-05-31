package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Info
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.LocalFireDepartment
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.User
import com.example.ui.components.VeloRixButton
import com.example.ui.theme.CyberpunkYellow
import com.example.ui.theme.DeepSpaceBlack
import com.example.ui.theme.ElectricBlue
import com.example.ui.viewmodel.PlatformViewModel

@Composable
fun AchievementBadge(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f))
                .border(2.dp, color.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(28.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: PlatformViewModel,
    onLogout: () -> Unit
) {
    val user by viewModel.userState.collectAsStateWithLifecycle()
    var isEditing by remember { mutableStateOf(false) }
    var editedUsername by remember { mutableStateOf(user?.username ?: "") }
    var editedPhone by remember { mutableStateOf(user?.phoneOrEmail ?: "") }
    var editedBio by remember { mutableStateOf(user?.bio ?: "") }
    var editedSocial by remember { mutableStateOf(user?.socialLink ?: "") }
    var selectedAvatarIdx by remember { mutableStateOf(user?.avatarIdx ?: 1) }

    LaunchedEffect(user) {
        val u = user
        if (!isEditing && u != null) {
            editedUsername = u.username
            editedPhone = u.phoneOrEmail
            editedBio = u.bio
            editedSocial = u.socialLink
            selectedAvatarIdx = u.avatarIdx
        }
    }

    Scaffold(
        containerColor = DeepSpaceBlack,
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.velorix_logo_image),
                            contentDescription = "Logo",
                            modifier = Modifier.size(32.dp).padding(end = 8.dp)
                        )
                        Text("COMMAND CENTER", color = CyberpunkYellow, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xD9101018),
                    titleContentColor = CyberpunkYellow
                ),
                actions = {
                    IconButton(onClick = { 
                        viewModel.logout()
                        onLogout() 
                    }) {
                        Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Logout", tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        val currentUser = user
        if (currentUser == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CyberpunkYellow)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar Selection
                val avatarColors = listOf(
                    Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF3F51B5), 
                    Color(0xFF00BCD4), Color(0xFF4CAF50), Color(0xFFFF9800)
                )

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(avatarColors[selectedAvatarIdx % avatarColors.size].copy(alpha = 0.2f))
                        .border(2.dp, if (isEditing) CyberpunkYellow else Color.Transparent, CircleShape)
                        .clickable(enabled = isEditing) {
                            selectedAvatarIdx = (selectedAvatarIdx + 1) % avatarColors.size
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Face,
                        contentDescription = "Avatar",
                        modifier = Modifier.size(80.dp),
                        tint = avatarColors[selectedAvatarIdx % avatarColors.size]
                    )
                    if (isEditing) {
                        Surface(
                            modifier = Modifier.align(Alignment.BottomEnd),
                            shape = CircleShape,
                            color = CyberpunkYellow
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Avatar",
                                modifier = Modifier.padding(4.dp).size(16.dp),
                                tint = Color.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (isEditing) {
                    OutlinedTextField(
                        value = editedUsername,
                        onValueChange = { editedUsername = it },
                        label = { Text("Gamertag", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CyberpunkYellow,
                            unfocusedBorderColor = Color.DarkGray
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = editedPhone,
                        onValueChange = { editedPhone = it },
                        label = { Text("Phone / Email", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CyberpunkYellow,
                            unfocusedBorderColor = Color.DarkGray
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = editedBio,
                        onValueChange = { editedBio = it },
                        label = { Text("Bio", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, tint = CyberpunkYellow) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CyberpunkYellow,
                            unfocusedBorderColor = Color.DarkGray
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = editedSocial,
                        onValueChange = { editedSocial = it },
                        label = { Text("Social Link", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, tint = CyberpunkYellow) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CyberpunkYellow,
                            unfocusedBorderColor = Color.DarkGray
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        OutlinedButton(
                            onClick = { 
                                isEditing = false 
                                editedUsername = currentUser.username
                                editedPhone = currentUser.phoneOrEmail
                                editedBio = currentUser.bio
                                editedSocial = currentUser.socialLink
                                selectedAvatarIdx = currentUser.avatarIdx
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text("Discard")
                        }
                        VeloRixButton(
                            text = "SAVE UPDATE",
                            onClick = {
                                val updatedUser = currentUser.copy(
                                    username = editedUsername,
                                    phoneOrEmail = editedPhone,
                                    bio = editedBio,
                                    socialLink = editedSocial,
                                    avatarIdx = selectedAvatarIdx
                                )
                                viewModel.updateProfile(updatedUser)
                                isEditing = false
                            },
                            testTag = "save_profile_btn"
                        )
                    }
                } else {
                    Text(
                        text = currentUser.username,
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentUser.phoneOrEmail,
                        color = Color.Gray,
                        fontSize = 16.sp,
                    )
                    if (currentUser.bio.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = currentUser.bio,
                            color = Color.LightGray,
                            fontSize = 14.sp,
                        )
                    }
                    if (currentUser.socialLink.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = currentUser.socialLink,
                            color = ElectricBlue,
                            fontSize = 14.sp,
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF1E1E28))
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("CURRENT RANK", color = Color.Gray, fontSize = 12.sp)
                            Text("Elite Lvl 42", color = CyberpunkYellow, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("WALLET", color = Color.Gray, fontSize = 12.sp)
                            Text("VT ${currentUser.balance}", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // ACHIEVEMENTS SECTION
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF1E1E28))
                            .padding(16.dp)
                    ) {
                        Text("MILESTONES & ACHIEVEMENTS", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            AchievementBadge(icon = androidx.compose.material.icons.Icons.Default.EmojiEvents, title = "10 Wins", color = CyberpunkYellow)
                            AchievementBadge(icon = androidx.compose.material.icons.Icons.Default.Stars, title = "Top 1%", color = ElectricBlue)
                            AchievementBadge(icon = androidx.compose.material.icons.Icons.Default.LocalFireDepartment, title = "3 Streak", color = Color(0xFFE91E63))
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    VeloRixButton(
                        text = "EDIT PROFILE",
                        onClick = { isEditing = true },
                        testTag = "edit_profile_btn"
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { viewModel.exportUserData() },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = CyberpunkYellow,
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (currentUser.dataExported) "SYNC CUSTOMER DATA AGAIN" else "EXPORT & SECURE DATA")
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { 
                            viewModel.deleteAccount()
                            onLogout()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFE91E63),
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("DELETE ACCOUNT")
                    }
                }
            }
        }
    }
}
