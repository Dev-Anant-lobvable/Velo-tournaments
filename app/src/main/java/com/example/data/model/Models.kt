package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

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

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // "ADD_FUNDS", "WITHDRAWAL", "ENTRY_FEE", "WINNINGS"
    val amount: Double,
    val detail: String, // e.g. "BGMI Solo Battle Royale Entry" or "Withdrawn to UPI"
    val timestamp: Long = System.currentTimeMillis(),
    val isPositive: Boolean
)

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: Int = 0,
    val username: String = "ProGamer_X",
    val phoneOrEmail: String = "+91 9876543210",
    val balance: Double = 500.0,
    val avatarIdx: Int = 2
)

@Entity(tableName = "leaderboard")
data class LeaderboardPlayer(
    @PrimaryKey val rank: Int,
    val username: String,
    val totalWinnings: Double,
    val kills: Int,
    val avatarIdx: Int
)
