package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(tableName = "tournaments")
data class Tournament(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val game: String, // "BGMI" or "Free Fire"
    val prizePool: Double,
    val entryFee: Double,
    val maxSlots: Int,
    var filledSlots: Int,
    val joined: Boolean = false,
    val dateTimeStr: String, // "Today at 8:00 PM"
    val mapType: String, // "Erangel", "Bermuda", "Miramar"
    val perspective: String, // "TPP" or "FPP"
    val bannerIdx: Int = 1 // 1, 2, 3
) {
    val isFull: Boolean
        get() = filledSlots >= maxSlots
}

@Serializable
@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // "ADD_FUNDS", "WITHDRAWAL", "ENTRY_FEE", "WINNINGS"
    val amount: Double,
    val detail: String, // e.g. "BGMI Solo Battle Royale Entry" or "Withdrawn to UPI"
    val timestamp: Long = 0, // Using default of 0 as System.currentTimeMillis() requires custom serializer, though we can just not rely on it in default
    val isPositive: Boolean
)

@Serializable
@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: Int = 0,
    val username: String = "ProGamer_X",
    val phoneOrEmail: String = "+91 9876543210",
    val balance: Double = 500.0,
    val avatarIdx: Int = 2,
    val passwordHash: String = "",
    val sessionToken: String = "",
    val bio: String = "Ready for battle",
    val socialLink: String = "@gamer",
    val dataExported: Boolean = false
)

@Serializable
@Entity(tableName = "leaderboard")
data class LeaderboardPlayer(
    @PrimaryKey val rank: Int,
    val username: String,
    val totalWinnings: Double,
    val kills: Int,
    val avatarIdx: Int
)

@Serializable
@Entity(tableName = "search_history")
data class SearchHistory(
    @PrimaryKey val query: String,
    val timestamp: Long
)
