package com.example.data.repository

import android.util.Log
import com.example.data.db.AppDatabase
import com.example.data.model.LeaderboardPlayer
import com.example.data.model.Tournament
import com.example.data.model.Transaction
import com.example.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class PlatformRepository(private val db: AppDatabase) {

    val user: Flow<User?> = db.userDao().getUser()
    val tournaments: Flow<List<Tournament>> = db.tournamentDao().getAll()
    val transactions: Flow<List<Transaction>> = db.transactionDao().getAll()
    val leaderboard: Flow<List<LeaderboardPlayer>> = db.leaderboardDao().getAll()
    val searchHistory: Flow<List<String>> = db.searchHistoryDao().getRecentSearches()

    suspend fun saveSearchQuery(query: String) {
        db.searchHistoryDao().insert(com.example.data.model.SearchHistory(query, System.currentTimeMillis()))
    }

    fun getTournamentById(id: String): Flow<Tournament?> {
        return tournaments.map { list -> list.find { it.id == id } }
    }

    suspend fun seedDatabase() {
        try {
            // Seed local database if empty
            if (db.tournamentDao().getAll().firstOrNull()?.isEmpty() != false) {
                db.tournamentDao().insertAll(
                    listOf(
                        Tournament(title = "BGMI Solo Battle Royale", game = "BGMI", prizePool = 10000.0, entryFee = 20.0, maxSlots = 100, filledSlots = 45, dateTimeStr = "Today at 8:00 PM", mapType = "Erangel", perspective = "TPP", bannerIdx = 1),
                        Tournament(title = "Free Fire Squad Rush", game = "Free Fire", prizePool = 25000.0, entryFee = 100.0, maxSlots = 48, filledSlots = 40, dateTimeStr = "Tomorrow at 5:00 PM", mapType = "Bermuda", perspective = "TPP", bannerIdx = 2),
                        Tournament(title = "BGMI TDM 4v4", game = "BGMI", prizePool = 5000.0, entryFee = 50.0, maxSlots = 8, filledSlots = 8, dateTimeStr = "Today at 10:00 PM", mapType = "Warehouse", perspective = "FPP", bannerIdx = 3)
                    )
                )
                db.leaderboardDao().insertAll(
                    listOf(
                        LeaderboardPlayer(1, "VeloWarrior_99", 125000.0, 534, 1),
                        LeaderboardPlayer(2, "SniperKingx", 89400.0, 412, 2),
                        LeaderboardPlayer(3, "ToxicMamba", 67000.0, 310, 3),
                        LeaderboardPlayer(4, "HeadHunter_Z", 54300.0, 276, 1),
                        LeaderboardPlayer(5, "UnknownPlayerX", 45000.0, 250, 2)
                    )
                )
            }
        } catch (e: Throwable) {
            Log.e("Room", "Failed to seed DB.", e)
        }
    }

    suspend fun joinTournament(tournamentId: String): JoinResult {
        val userItem = user.firstOrNull() ?: return JoinResult.Failure("User not found")
        val match = tournaments.firstOrNull()?.find { it.id == tournamentId } 
            ?: return JoinResult.Failure("Tournament not found")

        if (match.joined) return JoinResult.Failure("Already joined this tournament")
        if (match.isFull) return JoinResult.Failure("Tournament is full")
        if (userItem.balance < match.entryFee) return JoinResult.Failure("Insufficient balance. Please add funds!")

        val updatedUser = userItem.copy(balance = userItem.balance - match.entryFee)
        val updatedMatch = match.copy(joined = true, filledSlots = match.filledSlots + 1)
        val newTx = Transaction(type = "ENTRY_FEE", amount = match.entryFee, detail = "Joined ${match.game}: ${match.title}", isPositive = false, timestamp = System.currentTimeMillis())

        try {
            db.userDao().update(updatedUser)
            db.tournamentDao().update(updatedMatch)
            db.transactionDao().insert(newTx)
        } catch (e: Throwable) {
            Log.e("Room", "Sync failed.", e)
            return JoinResult.Failure("Database error. Try again.")
        }

        return JoinResult.Success("Successfully registered for ${match.title}! Room ID will be shared 15 mins before match.")
    }

    suspend fun addFunds(amount: Double) {
        val userItem = user.firstOrNull() ?: return
        val updatedUser = userItem.copy(balance = userItem.balance + amount)
        val newTx = Transaction(type = "ADD_FUNDS", amount = amount, detail = "Added funds to Wallet via UPI", isPositive = true, timestamp = System.currentTimeMillis())

        try {
            db.userDao().update(updatedUser)
            db.transactionDao().insert(newTx)
        } catch (e: Throwable) {
            Log.e("Room", "Failed to add funds", e)
        }
    }

    suspend fun withdrawFunds(amount: Double): WithdrawResult {
        val userItem = user.firstOrNull() ?: return WithdrawResult.Failure("User info missing")
        if (amount <= 0) return WithdrawResult.Failure("Amount must be greater than zero")
        if (userItem.balance < amount) return WithdrawResult.Failure("Insufficient balance")

        val updatedUser = userItem.copy(balance = userItem.balance - amount)
        val newTx = Transaction(type = "WITHDRAWAL", amount = amount, detail = "Withdrawn to linked UPI / Bank Account", isPositive = false, timestamp = System.currentTimeMillis())

        try {
            db.userDao().update(updatedUser)
            db.transactionDao().insert(newTx)
        } catch (e: Throwable) {
            Log.e("Room", "Withdraw failed", e)
        }
        return WithdrawResult.Success("Withdrawal of ₹$amount initiated successfully. Funds will reflect in 4 hours!")
    }

    suspend fun updateProfile(updatedUser: User) {
        try {
            db.userDao().update(updatedUser)
        } catch (e: Throwable) {
            Log.e("Room", "Failed to update profile", e)
        }
    }

    suspend fun saveUserProfile(username: String, phoneOrEmail: String, passwordHash: String = "") {
        val currentUser = user.firstOrNull()
        val newUser = currentUser?.copy(username = username, phoneOrEmail = phoneOrEmail, passwordHash = passwordHash.ifEmpty { currentUser.passwordHash }) 
            ?: User(id = 0, username = username, phoneOrEmail = phoneOrEmail, balance = 500.0, avatarIdx = 2, passwordHash = passwordHash)
            
        try {
            if (currentUser != null) {
                db.userDao().update(newUser)
            } else {
                db.userDao().insert(newUser)
            }
        } catch (e: Throwable) {
            Log.e("Room", "Failed to save profile", e)
        }
    }
}

sealed interface JoinResult {
    data class Success(val message: String) : JoinResult
    data class Failure(val message: String) : JoinResult
}

sealed interface WithdrawResult {
    data class Success(val message: String) : WithdrawResult
    data class Failure(val message: String) : WithdrawResult
}

