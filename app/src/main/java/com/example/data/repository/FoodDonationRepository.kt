package com.example.data.repository

import com.example.data.local.FoodDonationDao
import com.example.data.local.UserDao
import com.example.data.model.DonationStatus
import com.example.data.model.FoodDonationEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

class FoodDonationRepository(
  private val foodDonationDao: FoodDonationDao,
  private val userDao: UserDao
) {
  val allDonations: Flow<List<FoodDonationEntity>> = foodDonationDao.getAllDonations()
  val currentUser: Flow<UserEntity?> = userDao.getCurrentUser()
  val allUsers: Flow<List<UserEntity>> = userDao.getAllUsers()

  fun getDonationsByDonor(donorId: String): Flow<List<FoodDonationEntity>> =
    foodDonationDao.getDonationsByDonor(donorId)

  fun getDonationsClaimedByNgo(ngoId: String): Flow<List<FoodDonationEntity>> =
    foodDonationDao.getDonationsClaimedByNgo(ngoId)

  fun getDonationsByVolunteer(volunteerId: String): Flow<List<FoodDonationEntity>> =
    foodDonationDao.getDonationsByVolunteer(volunteerId)

  fun getPendingVolunteerPickups(): Flow<List<FoodDonationEntity>> =
    foodDonationDao.getPendingVolunteerPickups()

  suspend fun createDonation(
    donor: UserEntity,
    title: String,
    description: String,
    quantityMeals: Int,
    quantityWeightKg: Double,
    foodType: String,
    cookedTimeMillis: Long,
    expiryHours: Int,
    pickupAddress: String,
    pickupInstructions: String,
    latitude: Double,
    longitude: Double
  ): String {
    val id = "don_" + UUID.randomUUID().toString().take(8)
    val now = System.currentTimeMillis()
    val expiryTimeMillis = now + (expiryHours * 3600 * 1000L)
    val randomOtp = (1000..9999).random().toString()

    val entity = FoodDonationEntity(
      id = id,
      donorId = donor.id,
      donorName = donor.name,
      donorPhone = donor.phone,
      donorOrg = donor.organization,
      title = title,
      description = description,
      quantityMeals = quantityMeals,
      quantityWeightKg = quantityWeightKg,
      foodType = foodType,
      cookedTimeMillis = cookedTimeMillis,
      expiryTimeMillis = expiryTimeMillis,
      pickupAddress = pickupAddress,
      pickupInstructions = pickupInstructions,
      latitude = latitude,
      longitude = longitude,
      status = DonationStatus.PENDING.name,
      verificationOtp = randomOtp,
      createdAtMillis = now
    )
    foodDonationDao.insertDonation(entity)
    return id
  }

  suspend fun claimDonation(
    donationId: String,
    ngo: UserEntity
  ): Boolean {
    val existing = foodDonationDao.getDonationById(donationId).firstOrNull() ?: return false
    if (existing.status != DonationStatus.PENDING.name) return false

    val updated = existing.copy(
      status = DonationStatus.CLAIMED.name,
      claimedByNgoId = ngo.id,
      claimedByNgoName = ngo.organization.ifBlank { ngo.name },
      claimedAtMillis = System.currentTimeMillis()
    )
    foodDonationDao.updateDonation(updated)
    return true
  }

  suspend fun assignVolunteer(
    donationId: String,
    volunteer: UserEntity
  ): Boolean {
    val existing = foodDonationDao.getDonationById(donationId).firstOrNull() ?: return false
    val updated = existing.copy(
      assignedVolunteerId = volunteer.id,
      assignedVolunteerName = volunteer.name
    )
    foodDonationDao.updateDonation(updated)
    return true
  }

  suspend fun updateDeliveryStatus(
    donationId: String,
    newStatus: DonationStatus,
    volunteer: UserEntity? = null,
    deliveryNotes: String? = null
  ): Boolean {
    val existing = foodDonationDao.getDonationById(donationId).firstOrNull() ?: return false
    val now = System.currentTimeMillis()

    val updated = when (newStatus) {
      DonationStatus.CLAIMED -> existing.copy(
        status = DonationStatus.CLAIMED.name
      )
      DonationStatus.OUT_FOR_DELIVERY -> existing.copy(
        status = DonationStatus.OUT_FOR_DELIVERY.name,
        assignedVolunteerId = volunteer?.id ?: existing.assignedVolunteerId,
        assignedVolunteerName = volunteer?.name ?: existing.assignedVolunteerName,
        outForDeliveryAtMillis = now
      )
      DonationStatus.DELIVERED -> existing.copy(
        status = DonationStatus.DELIVERED.name,
        deliveredAtMillis = now,
        deliveryNotes = deliveryNotes ?: existing.deliveryNotes
      )
      DonationStatus.CANCELLED -> existing.copy(
        status = DonationStatus.CANCELLED.name
      )
      DonationStatus.PENDING -> existing.copy(
        status = DonationStatus.PENDING.name,
        claimedByNgoId = null,
        claimedByNgoName = null,
        claimedAtMillis = null,
        assignedVolunteerId = null,
        assignedVolunteerName = null
      )
    }
    foodDonationDao.updateDonation(updated)
    return true
  }

  suspend fun switchUser(userId: String) {
    userDao.switchCurrentUser(userId)
  }

  suspend fun saveUser(user: UserEntity) {
    userDao.insertUser(user)
  }
}
