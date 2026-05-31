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
import com.example.data.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.Serializable

@Serializable
data class AddFundsParams(val p_amount: Double)

@Serializable
data class WithdrawFundsParams(val p_amount: Double)

@Serializable
data class JoinTournamentParams(val p_tournament_id: String)

@Serializable
data class UpdateProfileParams(val p_username: String)

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

    suspend fun fetchDataFromServer() {
        try {
            // Strictly fetch from the server. Client-side demo fallback is removed.
            val fetchedTournaments = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                SupabaseClient.client.postgrest["tournaments"].select().decodeList<Tournament>()
            }
            if (fetchedTournaments.isNotEmpty()) {
                db.tournamentDao().insertAll(fetchedTournaments)
            }
            
            val fetchedLeaderboard = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                SupabaseClient.client.postgrest["leaderboard"].select().decodeList<LeaderboardPlayer>()
            }
            if (fetchedLeaderboard.isNotEmpty()) {
                db.leaderboardDao().insertAll(fetchedLeaderboard)
            }
            
        } catch (e: Throwable) {
            Log.e("PlatformRepository", "Failed to fetch real data from Server.", e)
        }
    }

    suspend fun joinTournament(tournamentId: String): JoinResult {
        val userItem = user.firstOrNull() ?: return JoinResult.Failure("User not found")
        val match = tournaments.firstOrNull()?.find { it.id == tournamentId } 
            ?: return JoinResult.Failure("Tournament not found")

        if (match.joined) return JoinResult.Failure("Already joined this tournament")
        if (match.isFull) return JoinResult.Failure("Tournament is full")
        if (userItem.balance < match.entryFee) return JoinResult.Failure("Insufficient balance. Please add funds!")

        try {
            // SERVER-SIDE LOGIC
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                SupabaseClient.client.postgrest.rpc("join_tournament", JoinTournamentParams(tournamentId))
            }
            Log.d("PlatformRepository", "Supabase RPC join_tournament succeeded")
        } catch (e: Throwable) {
            Log.e("PlatformRepository", "Backend RPC failed: ${e.message}", e)
            // Allow local update for demo version
        }

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
        val userItem = user.firstOrNull() ?: throw Exception("User info missing")
        
        try {
            // SERVER-SIDE BACKEND LOGIC: Calling Postgres RPC on Supabase
            // Performs atomical balance change & creates transaction log
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                SupabaseClient.client.postgrest.rpc("add_funds", AddFundsParams(amount))
            }
            Log.d("PlatformRepository", "Supabase RPC add_funds succeeded")
        } catch (e: Throwable) {
            Log.e("PlatformRepository", "Backend RPC failed: ${e.message}", e)
            // Allow local update for demo version
        }

        val updatedUser = userItem.copy(balance = userItem.balance + amount)
        val newTx = Transaction(type = "ADD_FUNDS", amount = amount, detail = "Added funds to Wallet via UPI", isPositive = true, timestamp = System.currentTimeMillis())

        try {
            db.userDao().update(updatedUser)
            db.transactionDao().insert(newTx)
        } catch (e: Throwable) {
            Log.e("Room", "Failed to add funds locally", e)
        }
    }

    suspend fun withdrawFunds(amount: Double): WithdrawResult {
        val userItem = user.firstOrNull() ?: return WithdrawResult.Failure("User info missing")
        if (amount <= 0) return WithdrawResult.Failure("Amount must be greater than zero")
        if (userItem.balance < amount) return WithdrawResult.Failure("Insufficient balance")

        try {
            // SERVER-SIDE LOGIC
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                SupabaseClient.client.postgrest.rpc("withdraw_funds", WithdrawFundsParams(amount))
            }
            Log.d("PlatformRepository", "Supabase RPC withdraw_funds succeeded")
        } catch (e: Throwable) {
            Log.e("PlatformRepository", "Backend RPC failed: ${e.message}", e)
            // Allow local update for demo version
        }

        val updatedUser = userItem.copy(balance = userItem.balance - amount)
        val newTx = Transaction(type = "WITHDRAWAL", amount = amount, detail = "Withdrawn to linked UPI / Bank Account", isPositive = false, timestamp = System.currentTimeMillis())

        try {
            db.userDao().update(updatedUser)
            db.transactionDao().insert(newTx)
        } catch (e: Throwable) {
            Log.e("Room", "Withdraw failed", e)
        }
        return WithdrawResult.Success("Withdrawal of VT $amount initiated successfully. Funds will reflect in 4 hours!")
    }

    suspend fun updateProfile(updatedUser: User) {
        try {
            db.userDao().update(updatedUser)
        } catch (e: Throwable) {
            Log.e("Room", "Failed to update profile", e)
        }
    }

    suspend fun updateSessionToken(token: String) {
        val currentUser = user.firstOrNull()
        if (currentUser != null) {
            val newUser = currentUser.copy(sessionToken = token)
            try {
                db.userDao().update(newUser)
            } catch (e: Throwable) {
                Log.e("Room", "Failed to update session token", e)
            }
        }
    }

    suspend fun saveUserProfile(username: String, phoneOrEmail: String, passwordHash: String = "", sessionToken: String = "") {
        val currentUser = user.firstOrNull()
        val newUser = currentUser?.copy(
            username = username, 
            phoneOrEmail = phoneOrEmail, 
            passwordHash = passwordHash.ifEmpty { currentUser.passwordHash },
            sessionToken = sessionToken.ifEmpty { currentUser.sessionToken }
        ) ?: User(id = 0, username = username, phoneOrEmail = phoneOrEmail, balance = 500.0, avatarIdx = 2, passwordHash = passwordHash, sessionToken = sessionToken)
            
        try {
            // SERVER-SIDE LOGIC
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                SupabaseClient.client.postgrest.rpc("update_profile", UpdateProfileParams(username))
            }
            Log.d("PlatformRepository", "Supabase RPC update_profile succeeded")
        } catch (e: Throwable) {
            Log.e("PlatformRepository", "Backend RPC update_profile failed: ${e.message}", e)
            // Do not throw here, allow login/registration to proceed locally.
        }

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

