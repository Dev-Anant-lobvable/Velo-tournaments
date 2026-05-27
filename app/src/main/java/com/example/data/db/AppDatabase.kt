package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.LeaderboardPlayer
import com.example.data.model.Tournament
import com.example.data.model.Transaction
import com.example.data.model.User

@Database(
    entities = [Tournament::class, User::class, Transaction::class, LeaderboardPlayer::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tournamentDao(): TournamentDao
    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao
    abstract fun leaderboardDao(): LeaderboardDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "velorix_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
