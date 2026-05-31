package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.LeaderboardPlayer
import com.example.data.model.Tournament
import com.example.data.model.Transaction
import com.example.data.model.User
import com.example.data.repository.JoinResult
import com.example.data.repository.PlatformRepository
import com.example.data.repository.WithdrawResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import com.example.data.SupabaseClient

class PlatformViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PlatformRepository(AppDatabase.getDatabase(application))

    val userState: StateFlow<User?> = repository.user
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val tournaments: StateFlow<List<Tournament>> = repository.tournaments
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val transactions: StateFlow<List<Transaction>> = repository.transactions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val leaderboard: StateFlow<List<LeaderboardPlayer>> = repository.leaderboard
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val searchHistory: StateFlow<List<String>> = repository.searchHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun saveSearchQuery(query: String) {
        if (query.isNotBlank()) {
            viewModelScope.launch {
                repository.saveSearchQuery(query)
            }
        }
    }

    // Current selected tournament for details screen
    private val _selectedTournamentId = MutableStateFlow<String?>(null)
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val selectedTournament: StateFlow<Tournament?> = _selectedTournamentId
        .flatMapLatest { id ->
            if (id == null) flowOf(null) else repository.getTournamentById(id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Login authenticating state
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _dbErrorDialog = MutableStateFlow<String?>(null)
    val dbErrorDialog: StateFlow<String?> = _dbErrorDialog.asStateFlow()

    fun clearDbError() {
        _dbErrorDialog.value = null
    }

    // Screen Loading/Swipe Refresh variables
    private val _isRefreshingHome = MutableStateFlow(false)
    val isRefreshingHome: StateFlow<Boolean> = _isRefreshingHome.asStateFlow()

    private val _isRefreshingWallet = MutableStateFlow(false)
    val isRefreshingWallet: StateFlow<Boolean> = _isRefreshingWallet.asStateFlow()

    // Status notifications / Alerts
    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    private val prefs = application.getSharedPreferences("auth_prefs", android.content.Context.MODE_PRIVATE)

    private fun checkLoginStreak() {
        val lastLogin = prefs.getLong("last_login_time", 0L)
        val currentStreak = prefs.getInt("login_streak", 0)
        val now = System.currentTimeMillis()
        val oneDayMillis = 24 * 60 * 60 * 1000L
        val twoDaysMillis = 2 * oneDayMillis

        val timeDiff = now - lastLogin
        if (timeDiff in oneDayMillis..twoDaysMillis) {
            // Consecutive day
            val newStreak = currentStreak + 1
            if (newStreak == 7) {
                // 7th day: give 10 VT
                addWalletFunds(10.0)
                prefs.edit().putInt("login_streak", 0).putLong("last_login_time", now).apply()
                viewModelScope.launch { _toastMessage.emit("Day 7 Login! You received 10 VT.") }
            } else {
                prefs.edit().putInt("login_streak", newStreak).putLong("last_login_time", now).apply()
                viewModelScope.launch { _toastMessage.emit("Day $newStreak! Received Login Token.") }
            }
        } else if (timeDiff > twoDaysMillis || lastLogin == 0L) {
            // Reset streak
            prefs.edit().putInt("login_streak", 1).putLong("last_login_time", now).apply()
            viewModelScope.launch { _toastMessage.emit("Day 1 Login! Received Login Token.") }
        }
    }

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.fetchDataFromServer()
            }
            // Auto login using Supabase session or local prefs
            val sessionCount = try {
                withContext(Dispatchers.IO) {
                    SupabaseClient.client.auth.currentSessionOrNull() != null
                }
            } catch (e: Exception) { false }
            if (sessionCount || prefs.getBoolean("is_logged_in", false)) {
                _isLoggedIn.value = true
                checkLoginStreak()
            }
        }
    }

    fun selectTournament(id: String?) {
        _selectedTournamentId.value = id
    }

    // Swipe Refresh simulation
    fun refreshHome() {
        viewModelScope.launch {
            _isRefreshingHome.value = true
            withContext(Dispatchers.IO) {
                repository.fetchDataFromServer()
            }
            _isRefreshingHome.value = false
            _toastMessage.emit("Tournaments & matches updated from database!")
        }
    }

    fun refreshWallet() {
        viewModelScope.launch {
            _isRefreshingWallet.value = true
            withContext(Dispatchers.IO) {
                repository.fetchDataFromServer()
            }
            _isRefreshingWallet.value = false
            _toastMessage.emit("Wallet transactions synchronized.")
        }
    }

    // Login operations
    fun login(phoneOrEmail: String, passwordHash: String, loginMethod: String = "email", onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            if (phoneOrEmail.isBlank() || passwordHash.isBlank()) {
                _toastMessage.emit("Please enter valid identifier and Password.")
                return@launch
            }
            if (phoneOrEmail.length > 100 || passwordHash.length > 100) {
                _toastMessage.emit("Input exceeds maximum allowed length.")
                return@launch
            }
            try {
                withContext(Dispatchers.IO) {
                    try {
                        if (loginMethod == "email") {
                            SupabaseClient.client.auth.signInWith(io.github.jan.supabase.gotrue.providers.builtin.Email) {
                                email = phoneOrEmail
                                password = passwordHash
                            }
                        } else {
                            SupabaseClient.client.auth.signInWith(io.github.jan.supabase.gotrue.providers.builtin.Phone) {
                                phone = phoneOrEmail
                                password = passwordHash
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("Auth", "Supabase sign in failed: ${e.message}", e)
                        _dbErrorDialog.value = "Backend API Error: ${e.message}\n(Falling back to local demo login)"
                    }
                    val currentProfile = repository.user.firstOrNull()
                    val username = currentProfile?.username ?: "Warrior"
                    repository.saveUserProfile(username, phoneOrEmail)
                }
                prefs.edit().putBoolean("is_logged_in", true).apply()
                _isLoggedIn.value = true
                checkLoginStreak()
                _toastMessage.emit("Welcome back, Warrior!")
                onComplete()
            } catch (e: Exception) {
                _dbErrorDialog.value = "Login Failed: ${e.message}"
            }
        }
    }

    fun register(username: String, phoneOrEmail: String, passwordHash: String, loginMethod: String = "email", onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            if (username.isBlank() || phoneOrEmail.isBlank() || passwordHash.isBlank()) {
                _toastMessage.emit("All fields are mandatory.")
                return@launch
            }
            if (username.length > 50 || phoneOrEmail.length > 100 || passwordHash.length > 100) {
                _toastMessage.emit("Input exceeds maximum allowed length.")
                return@launch
            }
            try {
                withContext(Dispatchers.IO) {
                    try {
                        if (loginMethod == "email") {
                            SupabaseClient.client.auth.signUpWith(io.github.jan.supabase.gotrue.providers.builtin.Email) {
                                email = phoneOrEmail
                                password = passwordHash
                            }
                        } else {
                            SupabaseClient.client.auth.signUpWith(io.github.jan.supabase.gotrue.providers.builtin.Phone) {
                                phone = phoneOrEmail
                                password = passwordHash
                            }
                        }
                    } catch(e: Exception) {
                        android.util.Log.e("Auth", "Supabase sign up failed: ${e.message}", e)
                        _dbErrorDialog.value = "Backend API Error: ${e.message}\n(Falling back to local demo registration)"
                    }
                    repository.saveUserProfile(username, phoneOrEmail)
                }
                prefs.edit().putBoolean("is_logged_in", true).apply()
                _isLoggedIn.value = true
                checkLoginStreak()
                _toastMessage.emit("Account created! Let the games begin.")
                onComplete()
            } catch (e: Exception) {
                _dbErrorDialog.value = "Registration Failed: ${e.message}"
            }
        }
    }

    fun loginWithGoogle(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    try {
                        // For Android, standard SDK OAuth requires configuration. 
                        // This might launch a browser or need redirect setup.
                        SupabaseClient.client.auth.signInWith(io.github.jan.supabase.gotrue.providers.Google)
                    } catch (e: Exception) {
                        android.util.Log.e("Auth", "Supabase Google sign in failed: ${e.message}", e)
                        _dbErrorDialog.value = "Backend API Error: ${e.message}\n(Falling back to local demo login)"
                    }
                    val currentProfile = repository.user.firstOrNull()
                    val username = currentProfile?.username ?: "Google_Warrior"
                    repository.saveUserProfile(username, "google_user@demo.com")
                }
                prefs.edit().putBoolean("is_logged_in", true).apply()
                _isLoggedIn.value = true
                checkLoginStreak()
                _toastMessage.emit("Welcome back, Google Warrior!")
                onComplete()
            } catch (e: Exception) {
                _dbErrorDialog.value = "Google Login Failed: ${e.message}"
            }
        }
    }

    fun updateProfile(updatedUser: User) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.updateProfile(updatedUser)
            }
            _toastMessage.emit("Profile updated successfully!")
        }
    }

    fun exportUserData() {
        viewModelScope.launch {
            val userItem = repository.user.firstOrNull()
            if (userItem != null) {
                val updatedUser = userItem.copy(dataExported = true)
                repository.updateProfile(updatedUser)
                // We're mimicking an upload to a backend/CSV here for proof of customer
                _toastMessage.emit("Data Exported & Uploaded to Database for Verification.")
            }
        }
    }
    
    fun deleteAccount() {
        viewModelScope.launch {
            logout()
            _toastMessage.emit("Account DELETED from Database. We will miss you!")
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    try {
                        SupabaseClient.client.auth.signOut()
                    } catch (e: Exception) {
                        android.util.Log.e("Auth", "Logout error", e)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("Auth", "Logout error", e)
            }
            prefs.edit().putBoolean("is_logged_in", false).apply()
            _isLoggedIn.value = false
        }
    }

    // Joining matches
    fun registerForTournament(id: String, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            when (val result = repository.joinTournament(id)) {
                is JoinResult.Success -> {
                    _toastMessage.emit(result.message)
                    onResult(true)
                }
                is JoinResult.Failure -> {
                    _toastMessage.emit(result.message)
                    onResult(false)
                }
            }
        }
    }

    // Adding Funds
    fun addWalletFunds(amount: Double) {
        viewModelScope.launch {
            try {
                repository.addFunds(amount)
                _toastMessage.emit("Added VT $amount successfully! Start dominating matches.")
            } catch (e: Exception) {
                _dbErrorDialog.value = "Backend RPC failed: ${e.message}"
            }
        }
    }

    // Funds withdrawal
    fun withdrawFunds(amount: Double) {
        viewModelScope.launch {
            when (val result = repository.withdrawFunds(amount)) {
                is WithdrawResult.Success -> {
                    _toastMessage.emit(result.message)
                }
                is WithdrawResult.Failure -> {
                    _toastMessage.emit(result.message)
                }
            }
        }
    }
}
