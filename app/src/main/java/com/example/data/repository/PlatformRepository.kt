package com.example.data.repository

import com.example.data.local.TournamentDao
import com.example.data.model.LeaderboardPlayer
import com.example.data.model.Tournament
import com.example.data.model.Transaction
import com.example.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow

class PlatformRepository(private val dao: TournamentDao) {

    val user: Flow<User?> = dao.getUser()
    val tournaments: Flow<List<Tournament>> = dao.getAllTournaments()
    val transactions: Flow<List<Transaction>> = dao.getAllTransactions()
    val leaderboard: Flow<List<LeaderboardPlayer>> = dao.getLeaderboard()

    fun getTournamentById(id: String): Flow<Tournament?> = dao.getTournamentById(id)

    suspend fun seedDatabase() {
        // Only seed if user or tournaments don't exist yet
        val currentUser = dao.getUser().firstOrNull()
        if (currentUser == null) {
            // Seed User
            dao.insertUser(User(id = 0, username = "VeloWarrior_99", phoneOrEmail = "+91 9876543210", balance = 500.0, avatarIdx = 2))

            // Seed Tournaments
            val initialTournaments = listOf(
                Tournament(
                    id = "t1",
                    title = "Solo Battle Royale - Erangel",
                    game = "BGMI",
                    prizePool = 1000.0,
                    entryFee = 10.0,
                    maxSlots = 100,
                    filledSlots = 45,
                    dateTimeStr = "Today at 8:00 PM",
                    mapType = "Erangel",
                    perspective = "TPP",
                    bannerIdx = 1
                ),
                Tournament(
                    id = "t2",
                    title = "Ultimate Squad Clash",
                    game = "Free Fire",
                    prizePool = 5000.0,
                    entryFee = 50.0,
                    maxSlots = 100,
                    filledSlots = 82,
                    dateTimeStr = "Today at 9:30 PM",
                    mapType = "Bermuda",
                    perspective = "FPP",
                    bannerIdx = 2
                ),
                Tournament(
                    id = "t3",
                    title = "Mega Championship Elite",
                    game = "BGMI",
                    prizePool = 10000.0,
                    entryFee = 100.0,
                    maxSlots = 200,
                    filledSlots = 154,
                    dateTimeStr = "Tomorrow at 8:00 PM",
                    mapType = "Miramar",
                    perspective = "TPP",
                    bannerIdx = 3
                ),
                Tournament(
                    id = "t4",
                    title = "Sniper Showdown (Solos)",
                    game = "BGMI",
                    prizePool = 2500.0,
                    entryFee = 25.0,
                    maxSlots = 80,
                    filledSlots = 31,
                    dateTimeStr = "Tomorrow at 6:00 PM",
                    mapType = "Sanhok",
                    perspective = "FPP",
                    bannerIdx = 1
                ),
                Tournament(
                    id = "t5",
                    title = "Free Fire Masters Weekly",
                    game = "Free Fire",
                    prizePool = 1500.0,
                    entryFee = 0.0, // FREE
                    maxSlots = 50,
                    filledSlots = 48,
                    dateTimeStr = "May 24 at 5:00 PM",
                    mapType = "Kalahari",
                    perspective = "TPP",
                    bannerIdx = 2
                ),
                Tournament(
                    id = "t6",
                    title = "Midnight Rush Combat",
                    game = "Free Fire",
                    prizePool = 800.0,
                    entryFee = 10.0,
                    maxSlots = 48,
                    filledSlots = 12,
                    dateTimeStr = "May 25 at 11:30 PM",
                    mapType = "Purgatory",
                    perspective = "TPP",
                    bannerIdx = 3
                )
            )
            dao.insertTournaments(initialTournaments)

            // Seed Transactions
            val seedTransactions = listOf(
                Transaction(type = "ADD_FUNDS", amount = 300.0, detail = "Fund Deposited via GPay", isPositive = true),
                Transaction(type = "ENTRY_FEE", amount = 10.0, detail = "BGMI Solo Battle Royale - Joined", isPositive = false),
                Transaction(type = "WINNINGS", amount = 210.0, detail = "BGMI Solo Battle Royale - Rank #4 Prize", isPositive = true)
            )
            for (tx in seedTransactions) {
                dao.insertTransaction(tx)
            }

            // Seed Leaderboard
            val players = listOf(
                LeaderboardPlayer(rank = 1, username = "Deadshot_Viper", totalWinnings = 15400.0, kills = 482, avatarIdx = 1),
                LeaderboardPlayer(rank = 2, username = "Neon_Slayer", totalWinnings = 12100.0, kills = 389, avatarIdx = 2),
                LeaderboardPlayer(rank = 3, username = "AlphaDraken", totalWinnings = 9800.0, kills = 311, avatarIdx = 3),
                LeaderboardPlayer(rank = 4, username = "VeloRix_Pro", totalWinnings = 7500.0, kills = 254, avatarIdx = 4),
                LeaderboardPlayer(rank = 5, username = "SniperKing_47", totalWinnings = 6300.0, kills = 212, avatarIdx = 5),
                LeaderboardPlayer(rank = 6, username = "FreeFire_Legend", totalWinnings = 5100.0, kills = 182, avatarIdx = 1),
                LeaderboardPlayer(rank = 7, username = "BGMIBoy_OP", totalWinnings = 4200.0, kills = 165, avatarIdx = 3),
                LeaderboardPlayer(rank = 8, username = "Trigger_Happy", totalWinnings = 3500.0, kills = 143, avatarIdx = 2),
                LeaderboardPlayer(rank = 9, username = "Ghost_Reaper", totalWinnings = 2900.0, kills = 112, avatarIdx = 4),
                LeaderboardPlayer(rank = 10, username = "X_Rishabh_X", totalWinnings = 1800.0, kills = 98, avatarIdx = 5)
            )
            dao.insertLeaderboard(players)
        }
    }

    suspend fun joinTournament(tournamentId: String): JoinResult {
        val userItem = dao.getUser().firstOrNull() ?: return JoinResult.Failure("User not found")
        val match = dao.getAllTournaments().firstOrNull()?.find { it.id == tournamentId } 
            ?: return JoinResult.Failure("Tournament not found")

        if (match.joined) {
            return JoinResult.Failure("Already joined this tournament")
        }
        if (match.isFull) {
            return JoinResult.Failure("Tournament is full")
        }
        if (userItem.balance < match.entryFee) {
            return JoinResult.Failure("Insufficient wallet balance. Please add funds!")
        }

        // Deduct money & increase slots, mark joined
        val updatedUser = userItem.copy(balance = userItem.balance - match.entryFee)
        dao.updateUser(updatedUser)

        val updatedMatch = match.copy(joined = true, filledSlots = match.filledSlots + 1)
        dao.updateTournament(updatedMatch)

        // Add to transactions
        dao.insertTransaction(
            Transaction(
                type = "ENTRY_FEE",
                amount = match.entryFee,
                detail = "Joined ${match.game}: ${match.title}",
                isPositive = false
            )
        )

        return JoinResult.Success("Successfully registered for ${match.title}! Room ID will be shared 15 mins before match.")
    }

    suspend fun addFunds(amount: Double) {
        val userItem = dao.getUser().firstOrNull() ?: return
        val updatedUser = userItem.copy(balance = userItem.balance + amount)
        dao.updateUser(updatedUser)

        dao.insertTransaction(
            Transaction(
                type = "ADD_FUNDS",
                amount = amount,
                detail = "Added funds to Wallet via UPI",
                isPositive = true
            )
        )
    }

    suspend fun withdrawFunds(amount: Double): WithdrawResult {
        val userItem = dao.getUser().firstOrNull() ?: return WithdrawResult.Failure("User info missing")
        if (amount <= 0) {
            return WithdrawResult.Failure("Amount must be greater than zero")
        }
        if (userItem.balance < amount) {
            return WithdrawResult.Failure("Insufficient balance to execute withdrawal")
        }

        val updatedUser = userItem.copy(balance = userItem.balance - amount)
        dao.updateUser(updatedUser)

        dao.insertTransaction(
            Transaction(
                type = "WITHDRAWAL",
                amount = amount,
                detail = "Withdrawn to linked UPI / Bank Account",
                isPositive = false
            )
        )

        return WithdrawResult.Success("Withdrawal of ₹$amount initiated successfully. Funds will reflect in 4 hours!")
    }

    suspend fun saveUserProfile(username: String, phoneOrEmail: String) {
        val currentUser = dao.getUser().firstOrNull()
        if (currentUser != null) {
            dao.updateUser(currentUser.copy(username = username, phoneOrEmail = phoneOrEmail))
        } else {
            dao.insertUser(User(id = 0, username = username, phoneOrEmail = phoneOrEmail, balance = 500.0, avatarIdx = 2))
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
