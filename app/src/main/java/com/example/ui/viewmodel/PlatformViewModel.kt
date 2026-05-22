package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.LeaderboardPlayer
import com.example.data.model.Tournament
import com.example.data.model.Transaction
import com.example.data.model.User
import com.example.data.repository.JoinResult
import com.example.data.repository.PlatformRepository
import com.example.data.repository.WithdrawResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PlatformViewModel(private val repository: PlatformRepository) : ViewModel() {

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

    // Current selected tournament for details screen
    private val _selectedTournamentId = MutableStateFlow<String?>(null)
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
            repository.seedDatabase()
        }
    }

    fun selectTournament(id: String?) {
        _selectedTournamentId.value = id
    }

    // Swipe Refresh simulation
    fun refreshHome() {
        viewModelScope.launch {
            _isRefreshingHome.value = true
            // Simulate networking delay
            delay(1500)
            _isRefreshingHome.value = false
            _toastMessage.emit("Tournaments compiled & matches updated!")
        }
    }

    fun refreshWallet() {
        viewModelScope.launch {
            _isRefreshingWallet.value = true
            // Simulate billing refresh delay
            delay(1500)
            _isRefreshingWallet.value = false
            _toastMessage.emit("Wallet transactions synchronized.")
        }
    }

    // Login operations
    fun login(phoneOrEmail: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            if (phoneOrEmail.isBlank()) {
                _toastMessage.emit("Please enter a valid Phone number or Email.")
                return@launch
            }
            // Update user state detail if user doesn't exist
            repository.saveUserProfile("VeloWarrior_99", phoneOrEmail)
            _isLoggedIn.value = true
            _toastMessage.emit("Welcome back, Warrior!")
            onComplete()
        }
    }

    fun register(username: String, phoneOrEmail: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            if (username.isBlank() || phoneOrEmail.isBlank()) {
                _toastMessage.emit("All fields are mandatory.")
                return@launch
            }
            repository.saveUserProfile(username, phoneOrEmail)
            _isLoggedIn.value = true
            _toastMessage.emit("Account verified! Let the games begin.")
            onComplete()
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

// ViewModelFactory for manual instantiation since we choose to execute cleanly without fragile Hilt plugin
@Suppress("UNCHECKED_CAST")
class PlatformViewModelFactory(private val repository: PlatformRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlatformViewModel::class.java)) {
            return PlatformViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class Exception")
    }
}
