package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
  @Query("SELECT * FROM users ORDER BY name ASC")
  fun getAllUsers(): Flow<List<UserEntity>>

  @Query("SELECT * FROM users WHERE isCurrent = 1 LIMIT 1")
  fun getCurrentUser(): Flow<UserEntity?>

  @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
  fun getUserById(id: String): Flow<UserEntity?>

  @Query("SELECT * FROM users WHERE role = :role")
  fun getUsersByRole(role: String): Flow<List<UserEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertUser(user: UserEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertUsers(users: List<UserEntity>)

  @Update
  suspend fun updateUser(user: UserEntity)

  @Query("UPDATE users SET isCurrent = 0")
  suspend fun clearCurrentUser()

  @Query("UPDATE users SET isCurrent = 1 WHERE id = :id")
  suspend fun markUserAsCurrent(id: String)

  @Transaction
  suspend fun switchCurrentUser(id: String) {
    clearCurrentUser()
    markUserAsCurrent(id)
  }
}
