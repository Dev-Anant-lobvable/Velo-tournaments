package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Transaction
import com.example.ui.components.VeloRixButton
import com.example.ui.theme.CyberpunkYellow
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DeepSpaceBlack
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TextGray
import com.example.ui.viewmodel.PlatformViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(viewModel: PlatformViewModel) {
    val user by viewModel.userState.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val isRefreshing by viewModel.isRefreshingWallet.collectAsState()

    var showWithdrawDialog by remember { mutableStateOf(false) }
    var withdrawAmount by remember { mutableStateOf("") }
    var upiId by remember { mutableStateOf("") }
    var showReceiptDialog by remember { mutableStateOf(false) }
    var lastWithdrawnAmount by remember { mutableStateOf("") }

    val context = androidx.compose.ui.platform.LocalContext.current
    var pendingAmount by remember { mutableStateOf(0.0) }
    val upiLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val dataString = result.data?.getStringExtra("response") ?: ""
        val isSuccess = dataString.contains("Status=SUCCESS", ignoreCase = true)

        if (result.resultCode == android.app.Activity.RESULT_OK && isSuccess) {
            if (pendingAmount > 0) {
                viewModel.addWalletFunds(pendingAmount)
                android.widget.Toast.makeText(context, "Payment Successful: VT $pendingAmount added", android.widget.Toast.LENGTH_SHORT).show()
            }
        } else {
            android.widget.Toast.makeText(context, "Payment failed or cancelled.", android.widget.Toast.LENGTH_SHORT).show()
        }
        pendingAmount = 0.0
    }
    
    val launchUpiPayment = { amount: Double ->
        pendingAmount = amount
        val tr = System.currentTimeMillis().toString()
        val uri = android.net.Uri.parse("upi://pay?pa=velorix@ybl&pn=VeloRix+Esports&mc=5411&tr=$tr&tn=Wallet+Topup&am=$amount&cu=INR")
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
        val chooser = android.content.Intent.createChooser(intent, "Pay with UPI")
        try {
            upiLauncher.launch(chooser)
        } catch (e: Exception) {
            android.widget.Toast.makeText(context, "No UPI app found. Please install a UPI app to add funds.", android.widget.Toast.LENGTH_LONG).show()
            pendingAmount = 0.0
        }
    }

    val presetAmounts = listOf(50.0, 100.0, 200.0, 500.0)

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // App Header Title
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.velorix_logo_image),
                            contentDescription = "Logo",
                            modifier = Modifier.size(36.dp).padding(end = 6.dp)
                        )
                        Text(
                            text = "FUNDS MANAGER",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = ElectricBlue,
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Text(
                        text = "MY WALLET",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Big Header Wallet Balance card
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
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "TOTAL WALLET BALANCE",
                            fontSize = 11.sp,
                            color = TextGray,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = CyberpunkYellow,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "VT ${user?.balance?.toInt() ?: 0}",
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }

                            Button(
                                onClick = { showWithdrawDialog = true },
                                modifier = Modifier
                                    .height(44.dp)
                                    .testTag("withdraw_action_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = ElectricBlue
                                ),
                                border = BorderStroke(1.5.dp, ElectricBlue),
                                shape = RoundedCornerShape(50.dp)
                            ) {
                                Text(
                                    text = "WITHDRAW",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }
            }

            // SECTION 1: ADD FUNDS PRESET GRID
            item {
                Text(
                    text = "QUICK ADD FUNDS (UPI)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presetAmounts.forEach { amount ->
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { launchUpiPayment(amount) }
                                .testTag("add_funds_preset_${amount.toInt()}"),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF161622)),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color(0xFF2C2C3E))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = CyberpunkYellow,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "VT ${amount.toInt()}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
                
                // Custom Amount Input
                var customAmount by remember { mutableStateOf("") }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = customAmount,
                        onValueChange = { customAmount = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("custom_add_funds_input"),
                        label = { Text("Custom Amount (VT)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberpunkYellow,
                            unfocusedBorderColor = Color(0xFF333333),
                            focusedLabelColor = CyberpunkYellow,
                            unfocusedLabelColor = TextGray,
                            focusedContainerColor = Color(0xFF0D0D12),
                            unfocusedContainerColor = Color(0xFF0D0D12),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Button(
                        onClick = {
                            val amt = customAmount.toDoubleOrNull() ?: 0.0
                            if (amt > 0) {
                                launchUpiPayment(amt)
                                customAmount = ""
                            }
                        },
                        modifier = Modifier
                            .height(56.dp)
                            .testTag("custom_add_funds_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberpunkYellow)
                    ) {
                        Text("ADD", color = DeepSpaceBlack, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // SECTION 2: TRANSACTION HISTORY TITLE
            item {
                Text(
                    text = "TRANSACTION HISTORY",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            // Historic transaction records LazyList
            if (transactions.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = TextGray,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "NO RECENT TRANSACTIONS FOUND",
                            fontSize = 12.sp,
                            color = TextGray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                items(transactions) { tx ->
                    TransactionRow(tx)
                }
            }
        }

        // DYNAMIC WITHDRAW POPUP DIALOG
        if (showWithdrawDialog) {
            AlertDialog(
                onDismissRequest = { showWithdrawDialog = false },
                title = {
                    Text(
                        text = "WITHDRAW WINNINGS",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Funds will migrate immediately to your designated UPI handle or linked bank account.",
                            fontSize = 13.sp,
                            color = TextGray,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        OutlinedTextField(
                            value = upiId,
                            onValueChange = { upiId = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .testTag("upi_id_input"),
                            label = { Text("UPI ID (e.g. name@okhdfc)") },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberpunkYellow,
                                unfocusedBorderColor = Color(0xFF333333),
                                focusedLabelColor = CyberpunkYellow,
                                unfocusedLabelColor = TextGray,
                                focusedContainerColor = Color(0xFF0D0D12),
                                unfocusedContainerColor = Color(0xFF0D0D12),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = withdrawAmount,
                            onValueChange = { withdrawAmount = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("withdraw_amount_input"),
                            label = { Text("Amount (VT)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberpunkYellow,
                                unfocusedBorderColor = Color(0xFF333333),
                                focusedLabelColor = CyberpunkYellow,
                                unfocusedLabelColor = TextGray,
                                focusedContainerColor = Color(0xFF0D0D12),
                                unfocusedContainerColor = Color(0xFF0D0D12),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val amt = withdrawAmount.toDoubleOrNull() ?: 0.0
                            if (upiId.isNotBlank() && amt > 0) {
                                viewModel.withdrawFunds(amt)
                                lastWithdrawnAmount = amt.toString()
                                showWithdrawDialog = false
                                withdrawAmount = ""
                                upiId = ""
                                showReceiptDialog = true
                            }
                        },
                        modifier = Modifier.testTag("submit_withdraw_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberpunkYellow, contentColor = DeepSpaceBlack)
                    ) {
                        Text("CONFIRM", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showWithdrawDialog = false }) {
                        Text("CANCEL", color = ElectricBlue)
                    }
                },
                containerColor = DarkSurface,
                shape = RoundedCornerShape(20.dp)
            )
        }

        if (showReceiptDialog) {
            AlertDialog(
                onDismissRequest = { showReceiptDialog = false },
                title = {
                    Text(
                        text = "WITHDRAWAL SUCCESS",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = NeonGreen
                    )
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Your funds have been successfully withdrawn.",
                            fontSize = 13.sp,
                            color = TextGray,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Amount Withdrawn:", color = Color.Gray, fontSize = 14.sp)
                            Text("VT $lastWithdrawnAmount", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Platform Fee:", color = Color.Gray, fontSize = 14.sp)
                            Text("VT 0.00", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Color.DarkGray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Sent:", color = Color.Gray, fontSize = 14.sp)
                            Text("VT $lastWithdrawnAmount", color = NeonGreen, fontWeight = FontWeight.Black, fontSize = 14.sp)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { 
                            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(android.content.Intent.EXTRA_TEXT, "I just successfully withdrew VT $lastWithdrawnAmount from my Velorix Wallet!")
                            }
                            context.startActivity(android.content.Intent.createChooser(intent, "Share Receipt"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberpunkYellow, contentColor = DeepSpaceBlack)
                    ) {
                        Text("SHARE RECEIPT", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showReceiptDialog = false }) {
                        Text("CLOSE", color = ElectricBlue)
                    }
                },
                containerColor = DarkSurface,
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

@Composable
fun TransactionRow(tx: Transaction) {
    val dateStr = remember(tx.timestamp) {
        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        sdf.format(Date(tx.timestamp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .testTag("transaction_row_${tx.id}"),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Cash icon indicator
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (tx.isPositive) NeonGreen.copy(alpha = 0.15f) else NeonRed.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (tx.isPositive) Icons.AutoMirrored.Filled.CallReceived else Icons.AutoMirrored.Filled.CallMade,
                        contentDescription = null,
                        tint = if (tx.isPositive) NeonGreen else NeonRed,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = tx.detail,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = dateStr,
                        fontSize = 11.sp,
                        color = TextGray
                    )
                }
            }

            // Positive/negative green red amount display
            val prefix = if (tx.isPositive) "+" else "-"
            Text(
                text = "$prefix VT ${tx.amount.toInt()}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = if (tx.isPositive) NeonGreen else NeonRed
            )
        }
    }
}
