package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.FoodDonationEntity
import com.example.data.model.UserEntity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreFoodRepository(private val context: Context) {
  private val auth = Firebase.auth
  val db: FirebaseFirestore = FirebaseFirestore.getInstance(
    context.getString(R.string.firestore_database_id)
  )

  fun currentUserId(): String? = auth.currentUser?.uid

  private fun requireUserId(): String {
    return auth.currentUser?.uid
      ?: throw IllegalStateException("User must be signed in with Google before accessing cloud Firestore.")
  }

  fun observeCloudDonations(): Flow<List<FoodDonationEntity>> {
    return db.collection("food_donations")
      .snapshots()
      .map { snapshot ->
        snapshot.documents.mapNotNull { doc ->
          try {
            FoodDonationEntity(
              id = doc.getString("id") ?: doc.id,
              donorId = doc.getString("donorId") ?: "",
              donorName = doc.getString("donorName") ?: "",
              donorPhone = doc.getString("donorPhone") ?: "",
              donorOrg = doc.getString("donorOrg") ?: "",
              title = doc.getString("title") ?: "",
              description = doc.getString("description") ?: "",
              quantityMeals = (doc.getLong("quantityMeals") ?: 0L).toInt(),
              quantityWeightKg = doc.getDouble("quantityWeightKg") ?: 0.0,
              foodType = doc.getString("foodType") ?: "VEG",
              cookedTimeMillis = doc.getLong("cookedTimeMillis") ?: System.currentTimeMillis(),
              expiryTimeMillis = doc.getLong("expiryTimeMillis") ?: (System.currentTimeMillis() + 14400000L),
              pickupAddress = doc.getString("pickupAddress") ?: "",
              pickupInstructions = doc.getString("pickupInstructions") ?: "",
              latitude = doc.getDouble("latitude") ?: 37.7816,
              longitude = doc.getDouble("longitude") ?: -122.4058,
              status = doc.getString("status") ?: "PENDING",
              claimedByNgoId = doc.getString("claimedByNgoId"),
              claimedByNgoName = doc.getString("claimedByNgoName"),
              claimedAtMillis = doc.getLong("claimedAtMillis"),
              assignedVolunteerId = doc.getString("assignedVolunteerId"),
              assignedVolunteerName = doc.getString("assignedVolunteerName"),
              outForDeliveryAtMillis = doc.getLong("outForDeliveryAtMillis"),
              deliveredAtMillis = doc.getLong("deliveredAtMillis"),
              deliveryNotes = doc.getString("deliveryNotes"),
              verificationOtp = doc.getString("verificationOtp") ?: "4829",
              createdAtMillis = doc.getLong("createdAtMillis") ?: System.currentTimeMillis()
            )
          } catch (e: Exception) {
            Log.w("FirestoreFoodRepository", "Error parsing donation ${doc.id}", e)
            null
          }
        }
      }
      .catch { e ->
        Log.e("FirestoreFoodRepository", "Error observing cloud donations", e)
        emit(emptyList())
      }
  }

  suspend fun syncDonationToCloud(donation: FoodDonationEntity) {
    try {
      val data = hashMapOf<String, Any?>(
        "id" to donation.id,
        "donorId" to (auth.currentUser?.uid ?: donation.donorId),
        "donorName" to donation.donorName,
        "donorPhone" to donation.donorPhone,
        "donorOrg" to donation.donorOrg,
        "title" to donation.title,
        "description" to donation.description,
        "quantityMeals" to donation.quantityMeals,
        "quantityWeightKg" to donation.quantityWeightKg,
        "foodType" to donation.foodType,
        "cookedTimeMillis" to donation.cookedTimeMillis,
        "expiryTimeMillis" to donation.expiryTimeMillis,
        "pickupAddress" to donation.pickupAddress,
        "pickupInstructions" to donation.pickupInstructions,
        "latitude" to donation.latitude,
        "longitude" to donation.longitude,
        "status" to donation.status,
        "claimedByNgoId" to donation.claimedByNgoId,
        "claimedByNgoName" to donation.claimedByNgoName,
        "claimedAtMillis" to donation.claimedAtMillis,
        "assignedVolunteerId" to donation.assignedVolunteerId,
        "assignedVolunteerName" to donation.assignedVolunteerName,
        "outForDeliveryAtMillis" to donation.outForDeliveryAtMillis,
        "deliveredAtMillis" to donation.deliveredAtMillis,
        "deliveryNotes" to donation.deliveryNotes,
        "verificationOtp" to donation.verificationOtp,
        "createdAtMillis" to donation.createdAtMillis
      )
      db.collection("food_donations").document(donation.id).set(data).await()
    } catch (e: Exception) {
      Log.e("FirestoreFoodRepository", "Failed to sync donation to cloud", e)
    }
  }

  suspend fun syncUserToCloud(user: UserEntity) {
    val uid = auth.currentUser?.uid ?: user.id
    try {
      val data = hashMapOf<String, Any>(
        "id" to uid,
        "name" to user.name,
        "role" to user.role,
        "organization" to user.organization,
        "email" to user.email,
        "phone" to user.phone,
        "address" to user.address,
        "latitude" to user.latitude,
        "longitude" to user.longitude,
        "createdAt" to System.currentTimeMillis()
      )
      db.collection("users").document(uid).set(data).await()
    } catch (e: Exception) {
      Log.e("FirestoreFoodRepository", "Failed to sync user to cloud", e)
    }
  }
}
