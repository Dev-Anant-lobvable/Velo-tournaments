package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.LeaderboardPlayer
import com.example.data.model.Tournament
import com.example.data.model.Transaction
import com.example.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface TournamentDao {
    @Query("SELECT * FROM tournaments")
    fun getAll(): Flow<List<Tournament>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tournaments: List<Tournament>)
    
    @Update
    suspend fun update(tournament: Tournament)
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users LIMIT 1")
    fun getUser(): Flow<User?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)

    @Update
    suspend fun update(user: User)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY id DESC")
    fun getAll(): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<Transaction>)
}

@Dao
interface LeaderboardDao {
    @Query("SELECT * FROM leaderboard ORDER BY rank ASC")
    fun getAll(): Flow<List<LeaderboardPlayer>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(players: List<LeaderboardPlayer>)
}
