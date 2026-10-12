package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole(val label: String, val badge: String) {
  DONOR("Food Donor", "Hotel / Restaurant"),
  NGO("NGO / Food Bank", "Recipient"),
  VOLUNTEER("Volunteer / Courier", "Delivery Agent")
}

@Entity(tableName = "users")
data class UserEntity(
  @PrimaryKey val id: String,
  val name: String,
  val role: String, // DONOR, NGO, VOLUNTEER
  val organization: String,
  val email: String,
  val phone: String,
  val address: String,
  val latitude: Double,
  val longitude: Double,
  val isCurrent: Boolean = false
) {
  val userRole: UserRole
    get() = try {
      UserRole.valueOf(role)
    } catch (_: Exception) {
      UserRole.DONOR
    }
}
