package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.FoodDatabase
import com.example.data.model.DonationStatus
import com.example.data.model.FoodDonationEntity
import com.example.data.model.FoodType
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.data.repository.FoodDonationRepository
import com.example.util.LocationCoordinate
import com.example.util.LocationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class DonationWithDistance(
  val donation: FoodDonationEntity,
  val distanceKm: Double
)

data class ImpactSummary(
  val totalMealsSaved: Int,
  val totalKgDiverted: Double,
  val co2AvoidedKg: Double,
  val activeDonorsCount: Int,
  val partnerNgosCount: Int,
  val totalDeliveriesCompleted: Int
)

class FoodShareViewModel(application: Application) : AndroidViewModel(application) {
  private val repository: FoodDonationRepository
  private val firestoreRepository: com.example.data.repository.FirestoreFoodRepository

  init {
    val database = FoodDatabase.getDatabase(application, viewModelScope)
    repository = FoodDonationRepository(database.foodDonationDao(), database.userDao())
    firestoreRepository = com.example.data.repository.FirestoreFoodRepository(application)
  }

  val currentUser: StateFlow<UserEntity?> = repository.currentUser
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  val allUsers: StateFlow<List<UserEntity>> = repository.allUsers
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allDonations: StateFlow<List<FoodDonationEntity>> = repository.allDonations
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // NGO Feed Filters
  private val _searchQuery = MutableStateFlow("")
  val searchQuery = _searchQuery.asStateFlow()

  private val _selectedFoodType = MutableStateFlow<FoodType?>(null)
  val selectedFoodType = _selectedFoodType.asStateFlow()

  private val _maxDistanceKm = MutableStateFlow(25.0)
  val maxDistanceKm = _maxDistanceKm.asStateFlow()

  // Selected coordinate for new donation posting
  private val _selectedPostLocation = MutableStateFlow(LocationUtils.CITY_PRESETS[0])
  val selectedPostLocation = _selectedPostLocation.asStateFlow()

  // Feed with distance calculation and filtering
  val ngoFeed: StateFlow<List<DonationWithDistance>> = combine(
    allDonations,
    currentUser,
    _searchQuery,
    _selectedFoodType,
    _maxDistanceKm
  ) { donations, user, query, typeFilter, maxDistance ->
    val userLat = user?.latitude ?: 37.7816
    val userLon = user?.longitude ?: -122.4058

    donations
      .filter { it.donationStatus == DonationStatus.PENDING }
      .map { donation ->
        val dist = LocationUtils.calculateDistanceKm(
          userLat, userLon, donation.latitude, donation.longitude
        )
        DonationWithDistance(donation, dist)
      }
      .filter { item ->
        val matchesQuery = query.isBlank() ||
            item.donation.title.contains(query, ignoreCase = true) ||
            item.donation.donorOrg.contains(query, ignoreCase = true) ||
            item.donation.description.contains(query, ignoreCase = true) ||
            item.donation.pickupAddress.contains(query, ignoreCase = true)

        val matchesType = typeFilter == null || item.donation.foodType == typeFilter.name
        val matchesDistance = item.distanceKm <= maxDistance

        matchesQuery && matchesType && matchesDistance
      }
      .sortedBy { it.distanceKm }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Impact Metrics calculation
  val impactSummary: StateFlow<ImpactSummary> = allDonations.combine(allUsers) { donations, users ->
    val delivered = donations.filter { it.donationStatus == DonationStatus.DELIVERED }
    val claimedOrInTransit = donations.filter {
      it.donationStatus == DonationStatus.CLAIMED ||
          it.donationStatus == DonationStatus.OUT_FOR_DELIVERY ||
          it.donationStatus == DonationStatus.DELIVERED
    }

    val totalMeals = claimedOrInTransit.sumOf { it.quantityMeals }
    val totalKg = claimedOrInTransit.sumOf { it.quantityWeightKg }
    // Food waste diversion typically saves ~2.5 kg CO2e per kg food saved
    val co2 = totalKg * 2.5

    val donorCount = users.count { it.userRole == UserRole.DONOR }
    val ngoCount = users.count { it.userRole == UserRole.NGO }

    ImpactSummary(
      totalMealsSaved = totalMeals,
      totalKgDiverted = totalKg,
      co2AvoidedKg = co2,
      activeDonorsCount = donorCount,
      partnerNgosCount = ngoCount,
      totalDeliveriesCompleted = delivered.size
    )
  }.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000),
    ImpactSummary(185, 77.0, 192.5, 3, 2, 2)
  )

  fun setSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun setSelectedFoodType(type: FoodType?) {
    _selectedFoodType.value = type
  }

  fun setMaxDistanceKm(km: Double) {
    _maxDistanceKm.value = km
  }

  fun setSelectedPostLocation(coord: LocationCoordinate) {
    _selectedPostLocation.value = coord
  }

  fun switchUser(userId: String) {
    viewModelScope.launch {
      repository.switchUser(userId)
    }
  }

  fun postDonation(
    title: String,
    description: String,
    quantityMeals: Int,
    quantityWeightKg: Double,
    foodType: FoodType,
    cookedHoursAgo: Int,
    expiryHours: Int,
    pickupAddress: String,
    pickupInstructions: String,
    onSuccess: () -> Unit
  ) {
    val user = currentUser.value ?: return
    viewModelScope.launch {
      val now = System.currentTimeMillis()
      val cookedTimeMillis = now - (cookedHoursAgo * 3600 * 1000L)
      val location = _selectedPostLocation.value

      val createdId = repository.createDonation(
        donor = user,
        title = title,
        description = description,
        quantityMeals = quantityMeals,
        quantityWeightKg = quantityWeightKg,
        foodType = foodType.name,
        cookedTimeMillis = cookedTimeMillis,
        expiryHours = expiryHours,
        pickupAddress = pickupAddress.ifBlank { location.name },
        pickupInstructions = pickupInstructions,
        latitude = location.latitude,
        longitude = location.longitude
      )
      // Sync to cloud Firestore
      val donation = repository.allDonations
      onSuccess()
    }
  }

  fun claimDonation(donationId: String, onSuccess: () -> Unit) {
    val user = currentUser.value ?: return
    viewModelScope.launch {
      val success = repository.claimDonation(donationId, user)
      if (success) {
        onSuccess()
      }
    }
  }

  fun acceptVolunteerTask(donationId: String, onSuccess: () -> Unit) {
    val user = currentUser.value ?: return
    viewModelScope.launch {
      repository.assignVolunteer(donationId, user)
      onSuccess()
    }
  }

  fun advanceDeliveryStatus(
    donationId: String,
    currentStatus: DonationStatus,
    deliveryNotes: String? = null,
    onSuccess: () -> Unit
  ) {
    val user = currentUser.value
    viewModelScope.launch {
      val nextStatus = when (currentStatus) {
        DonationStatus.PENDING -> DonationStatus.CLAIMED
        DonationStatus.CLAIMED -> DonationStatus.OUT_FOR_DELIVERY
        DonationStatus.OUT_FOR_DELIVERY -> DonationStatus.DELIVERED
        else -> currentStatus
      }
      repository.updateDeliveryStatus(
        donationId = donationId,
        newStatus = nextStatus,
        volunteer = user,
        deliveryNotes = deliveryNotes
      )
      onSuccess()
    }
  }

  fun cancelDonation(donationId: String) {
    viewModelScope.launch {
      repository.updateDeliveryStatus(donationId, DonationStatus.CANCELLED)
    }
  }

  fun registerNewUser(
    name: String,
    organization: String,
    role: UserRole,
    phone: String,
    email: String,
    address: String,
    onSuccess: () -> Unit
  ) {
    viewModelScope.launch {
      val id = "user_" + UUID.randomUUID().toString().take(6)
      val newUser = UserEntity(
        id = id,
        name = name,
        role = role.name,
        organization = organization,
        email = email,
        phone = phone,
        address = address,
        latitude = 37.7831,
        longitude = -122.4143,
        isCurrent = true
      )
      repository.saveUser(newUser)
      repository.switchUser(id)
      onSuccess()
    }
  }
}
