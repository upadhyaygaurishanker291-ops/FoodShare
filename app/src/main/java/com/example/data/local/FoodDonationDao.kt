package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.FoodDonationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDonationDao {
  @Query("SELECT * FROM food_donations ORDER BY createdAtMillis DESC")
  fun getAllDonations(): Flow<List<FoodDonationEntity>>

  @Query("SELECT * FROM food_donations WHERE status = :status ORDER BY createdAtMillis DESC")
  fun getDonationsByStatus(status: String): Flow<List<FoodDonationEntity>>

  @Query("SELECT * FROM food_donations WHERE donorId = :donorId ORDER BY createdAtMillis DESC")
  fun getDonationsByDonor(donorId: String): Flow<List<FoodDonationEntity>>

  @Query("SELECT * FROM food_donations WHERE claimedByNgoId = :ngoId ORDER BY claimedAtMillis DESC")
  fun getDonationsClaimedByNgo(ngoId: String): Flow<List<FoodDonationEntity>>

  @Query("SELECT * FROM food_donations WHERE assignedVolunteerId = :volunteerId ORDER BY claimedAtMillis DESC")
  fun getDonationsByVolunteer(volunteerId: String): Flow<List<FoodDonationEntity>>

  @Query("SELECT * FROM food_donations WHERE status = 'CLAIMED' AND assignedVolunteerId IS NULL ORDER BY claimedAtMillis DESC")
  fun getPendingVolunteerPickups(): Flow<List<FoodDonationEntity>>

  @Query("SELECT * FROM food_donations WHERE id = :id LIMIT 1")
  fun getDonationById(id: String): Flow<FoodDonationEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertDonation(donation: FoodDonationEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertDonations(donations: List<FoodDonationEntity>)

  @Update
  suspend fun updateDonation(donation: FoodDonationEntity)

  @Delete
  suspend fun deleteDonation(donation: FoodDonationEntity)

  @Query("DELETE FROM food_donations WHERE id = :id")
  suspend fun deleteDonationById(id: String)
}
