package com.example.data.local

import androidx.room.*
import com.example.data.model.LeaderboardPlayer
import com.example.data.model.Tournament
import com.example.data.model.Transaction
import com.example.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface TournamentDao {

    @Query("SELECT * FROM users WHERE id = 0")
    fun getUser(): Flow<User?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Query("SELECT * FROM tournaments")
    fun getAllTournaments(): Flow<List<Tournament>>

    @Query("SELECT * FROM tournaments WHERE id = :id")
    fun getTournamentById(id: String): Flow<Tournament?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTournaments(tournaments: List<Tournament>)

    @Update
    suspend fun updateTournament(tournament: Tournament)

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Query("SELECT * FROM leaderboard ORDER BY rank ASC")
    fun getLeaderboard(): Flow<List<LeaderboardPlayer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaderboard(players: List<LeaderboardPlayer>)

    @Query("DELETE FROM tournaments")
    suspend fun clearTournaments()

    @Query("DELETE FROM leaderboard")
    suspend fun clearLeaderboard()
}
