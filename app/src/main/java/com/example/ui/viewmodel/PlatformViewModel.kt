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

    // Screen Loading/Swipe Refresh variables
    private val _isRefreshingHome = MutableStateFlow(false)
    val isRefreshingHome: StateFlow<Boolean> = _isRefreshingHome.asStateFlow()

    private val _isRefreshingWallet = MutableStateFlow(false)
    val isRefreshingWallet: StateFlow<Boolean> = _isRefreshingWallet.asStateFlow()

    // Status notifications / Alerts
    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.seedDatabase()
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
                repository.seedDatabase()
            }
            _isRefreshingHome.value = false
            _toastMessage.emit("Tournaments & matches updated from database!")
        }
    }

    fun refreshWallet() {
        viewModelScope.launch {
            _isRefreshingWallet.value = true
            withContext(Dispatchers.IO) {
                repository.seedDatabase()
            }
            _isRefreshingWallet.value = false
            _toastMessage.emit("Wallet transactions synchronized.")
        }
    }

    // Login operations
    fun login(phoneOrEmail: String, passwordHash: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            if (phoneOrEmail.isBlank() || passwordHash.isBlank()) {
                _toastMessage.emit("Please enter valid Email/Phone and Password.")
                return@launch
            }
            withContext(Dispatchers.IO) {
               val user = repository.user.firstOrNull()
               if (user != null) {
                   if (user.passwordHash.isNotEmpty() && user.passwordHash != passwordHash) {
                       _toastMessage.emit("Incorrect password.")
                       return@withContext
                   }
                   repository.saveUserProfile(user.username, phoneOrEmail, passwordHash)
               } else {
                   repository.saveUserProfile("VeloWarrior_99", phoneOrEmail, passwordHash)
               }
               repository.seedDatabase()
            }
            
            // Only login if error was not emitted
            val user = repository.user.firstOrNull()
            if (user?.passwordHash?.isNotEmpty() == true && user.passwordHash != passwordHash) {
                return@launch
            }
            _isLoggedIn.value = true
            _toastMessage.emit("Welcome back, Warrior!")
            onComplete()
        }
    }

    fun register(username: String, phoneOrEmail: String, passwordHash: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            if (username.isBlank() || phoneOrEmail.isBlank() || passwordHash.isBlank()) {
                _toastMessage.emit("All fields are mandatory.")
                return@launch
            }
            repository.saveUserProfile(username, phoneOrEmail, passwordHash)
            _isLoggedIn.value = true
            _toastMessage.emit("Account verified! Let the games begin.")
            onComplete()
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

    fun logout() {
        _isLoggedIn.value = false
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
            repository.addFunds(amount)
            _toastMessage.emit("Added ₹$amount successfully! Start dominating matches.")
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
