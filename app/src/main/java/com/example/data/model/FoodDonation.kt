package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DonationStatus(val label: String) {
  PENDING("Available"),
  CLAIMED("Claimed"),
  OUT_FOR_DELIVERY("Out for Delivery"),
  DELIVERED("Delivered"),
  CANCELLED("Cancelled")
}

enum class FoodType(val label: String, val iconName: String) {
  VEG("Vegetarian", "veg"),
  NON_VEG("Non-Vegetarian", "meat"),
  VEGAN("Vegan", "plant"),
  BAKERY("Bakery / Bread", "bread"),
  PACKAGED("Packaged Goods", "box")
}

@Entity(tableName = "food_donations")
data class FoodDonationEntity(
  @PrimaryKey val id: String,
  val donorId: String,
  val donorName: String,
  val donorPhone: String,
  val donorOrg: String,
  val title: String,
  val description: String,
  val quantityMeals: Int,
  val quantityWeightKg: Double,
  val foodType: String,
  val cookedTimeMillis: Long,
  val expiryTimeMillis: Long,
  val pickupAddress: String,
  val pickupInstructions: String = "",
  val latitude: Double,
  val longitude: Double,
  val status: String, // PENDING, CLAIMED, OUT_FOR_DELIVERY, DELIVERED, CANCELLED
  val claimedByNgoId: String? = null,
  val claimedByNgoName: String? = null,
  val claimedAtMillis: Long? = null,
  val assignedVolunteerId: String? = null,
  val assignedVolunteerName: String? = null,
  val outForDeliveryAtMillis: Long? = null,
  val deliveredAtMillis: Long? = null,
  val deliveryNotes: String? = null,
  val verificationOtp: String = "4829",
  val createdAtMillis: Long = System.currentTimeMillis()
) {
  val donationStatus: DonationStatus
    get() = try {
      DonationStatus.valueOf(status)
    } catch (_: Exception) {
      DonationStatus.PENDING
    }

  val isExpired: Boolean
    get() = System.currentTimeMillis() > expiryTimeMillis && donationStatus == DonationStatus.PENDING
}
