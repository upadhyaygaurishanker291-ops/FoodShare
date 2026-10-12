package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.DonationStatus
import com.example.data.model.FoodDonationEntity
import com.example.data.model.FoodType
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
  entities = [UserEntity::class, FoodDonationEntity::class],
  version = 1,
  exportSchema = false
)
abstract class FoodDatabase : RoomDatabase() {
  abstract fun userDao(): UserDao
  abstract fun foodDonationDao(): FoodDonationDao

  companion object {
    @Volatile
    private var INSTANCE: FoodDatabase? = null

    fun getDatabase(context: Context, scope: CoroutineScope): FoodDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          FoodDatabase::class.java,
          "food_share_database"
        )
          .addCallback(FoodDatabaseCallback(scope))
          .build()
        INSTANCE = instance
        instance
      }
    }

    private class FoodDatabaseCallback(
      private val scope: CoroutineScope
    ) : Callback() {
      override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        INSTANCE?.let { database ->
          scope.launch(Dispatchers.IO) {
            populateInitialData(database)
          }
        }
      }
    }

    suspend fun populateInitialData(database: FoodDatabase) {
      val userDao = database.userDao()
      val donationDao = database.foodDonationDao()

      // Seed Users
      val users = listOf(
        UserEntity(
          id = "donor_hyatt",
          name = "Chef Marco - Grand Hyatt",
          role = UserRole.DONOR.name,
          organization = "Grand Hyatt Regency",
          email = "catering@grandhyatt.org",
          phone = "+1 (555) 234-5678",
          address = "120 Market St, Downtown",
          latitude = 37.7937,
          longitude = -122.3965,
          isCurrent = true // Active user by default
        ),
        UserEntity(
          id = "donor_bistro",
          name = "Elena Rostova",
          role = UserRole.DONOR.name,
          organization = "Green Leaf Artisan Bistro",
          email = "elena@greenleafbistro.com",
          phone = "+1 (555) 345-6789",
          address = "450 Valencia St, Mission",
          latitude = 37.7648,
          longitude = -122.4219,
          isCurrent = false
        ),
        UserEntity(
          id = "donor_bakery",
          name = "Pierre Boulanger",
          role = UserRole.DONOR.name,
          organization = "Golden Crust Organic Bakery",
          email = "pierre@goldencrust.co",
          phone = "+1 (555) 456-7890",
          address = "782 Columbus Ave, North Beach",
          latitude = 37.8012,
          longitude = -122.4111,
          isCurrent = false
        ),
        UserEntity(
          id = "ngo_hope",
          name = "Hope Community Food Bank",
          role = UserRole.NGO.name,
          organization = "Hope Food Rescue & Shelter",
          email = "director@hopefoodbank.org",
          phone = "+1 (555) 890-1234",
          address = "950 Howard St, SoMa",
          latitude = 37.7816,
          longitude = -122.4058,
          isCurrent = false
        ),
        UserEntity(
          id = "ngo_care",
          name = "Care & Nourish Youth Pantry",
          role = UserRole.NGO.name,
          organization = "Care & Nourish NGO",
          email = "contact@carenourish.org",
          phone = "+1 (555) 901-2345",
          address = "320 Turk St, Tenderloin",
          latitude = 37.7831,
          longitude = -122.4143,
          isCurrent = false
        ),
        UserEntity(
          id = "vol_sarah",
          name = "Sarah Jenkins",
          role = UserRole.VOLUNTEER.name,
          organization = "EcoBike Deliveries",
          email = "sarah.j@volunteers.org",
          phone = "+1 (555) 678-9012",
          address = "Mission District Hub",
          latitude = 37.7680,
          longitude = -122.4190,
          isCurrent = false
        ),
        UserEntity(
          id = "vol_marcus",
          name = "Marcus Chen",
          role = UserRole.VOLUNTEER.name,
          organization = "Community EV Express",
          email = "marcus.c@volunteerfleet.org",
          phone = "+1 (555) 789-0123",
          address = "Downtown Transit Hub",
          latitude = 37.7900,
          longitude = -122.4000,
          isCurrent = false
        )
      )
      userDao.insertUsers(users)

      val now = System.currentTimeMillis()
      val oneHour = 3600 * 1000L

      // Seed Initial Food Donations
      val initialDonations = listOf(
        // Pending 1 (Veg buffet)
        FoodDonationEntity(
          id = "don_001",
          donorId = "donor_hyatt",
          donorName = "Chef Marco",
          donorPhone = "+1 (555) 234-5678",
          donorOrg = "Grand Hyatt Regency",
          title = "Executive Banquet: Mediterranean Rice & Roasted Veggies",
          description = "Steam-table surplus sealed in insulated stainless gastro containers. Freshly prepared 1 hour ago.",
          quantityMeals = 45,
          quantityWeightKg = 18.0,
          foodType = FoodType.VEG.name,
          cookedTimeMillis = now - (1 * oneHour),
          expiryTimeMillis = now + (4 * oneHour),
          pickupAddress = "120 Market St, Service Loading Dock Bay 2",
          pickupInstructions = "Ring buzzer at Dock Bay 2; ask for kitchen steward.",
          latitude = 37.7937,
          longitude = -122.3965,
          status = DonationStatus.PENDING.name,
          verificationOtp = "6291",
          createdAtMillis = now - (45 * 60 * 1000L)
        ),
        // Pending 2 (Bakery)
        FoodDonationEntity(
          id = "don_002",
          donorId = "donor_bakery",
          donorName = "Pierre Boulanger",
          donorPhone = "+1 (555) 456-7890",
          donorOrg = "Golden Crust Organic Bakery",
          title = "Fresh Artisanal Sourdough & Brioche Loaves",
          description = "Unsold day-baked loaves and gourmet baguettes. Bagged cleanly in food-grade kraft bags.",
          quantityMeals = 30,
          quantityWeightKg = 12.5,
          foodType = FoodType.BAKERY.name,
          cookedTimeMillis = now - (3 * oneHour),
          expiryTimeMillis = now + (18 * oneHour),
          pickupAddress = "782 Columbus Ave, North Beach",
          pickupInstructions = "Come to the side entrance on Filbert St.",
          latitude = 37.8012,
          longitude = -122.4111,
          status = DonationStatus.PENDING.name,
          verificationOtp = "3814",
          createdAtMillis = now - (2 * oneHour)
        ),
        // Pending 3 (High protein Non-Veg)
        FoodDonationEntity(
          id = "don_003",
          donorId = "donor_bistro",
          donorName = "Elena Rostova",
          donorPhone = "+1 (555) 345-6789",
          donorOrg = "Green Leaf Artisan Bistro",
          title = "Herb Roast Chicken & Steamed Green Beans",
          description = "Chilled to 38°F immediately after lunchtime service. Packaged in aluminum trays with temperature tags.",
          quantityMeals = 28,
          quantityWeightKg = 14.0,
          foodType = FoodType.NON_VEG.name,
          cookedTimeMillis = now - (2 * oneHour),
          expiryTimeMillis = now + (3 * oneHour),
          pickupAddress = "450 Valencia St, Mission",
          pickupInstructions = "Kitchen door in back alley; ring buzzer labeled Bistro.",
          latitude = 37.7648,
          longitude = -122.4219,
          status = DonationStatus.PENDING.name,
          verificationOtp = "8402",
          createdAtMillis = now - (1 * oneHour)
        ),
        // Claimed (Waiting for Volunteer Pickup)
        FoodDonationEntity(
          id = "don_004",
          donorId = "donor_hyatt",
          donorName = "Chef Marco",
          donorPhone = "+1 (555) 234-5678",
          donorOrg = "Grand Hyatt Regency",
          title = "Hearty Minestrone Soup & Garlic Rolls",
          description = "Large commercial thermo-dispenser of fresh minestrone plus two boxes of wrapped garlic rolls.",
          quantityMeals = 50,
          quantityWeightKg = 22.0,
          foodType = FoodType.VEGAN.name,
          cookedTimeMillis = now - (2 * oneHour),
          expiryTimeMillis = now + (4 * oneHour),
          pickupAddress = "120 Market St, Service Loading Dock Bay 2",
          pickupInstructions = "Thermal transport containers recommended.",
          latitude = 37.7937,
          longitude = -122.3965,
          status = DonationStatus.CLAIMED.name,
          claimedByNgoId = "ngo_hope",
          claimedByNgoName = "Hope Community Food Bank",
          claimedAtMillis = now - (30 * 60 * 1000L),
          verificationOtp = "5190",
          createdAtMillis = now - (2 * oneHour)
        ),
        // Out For Delivery (Active courier tracking!)
        FoodDonationEntity(
          id = "don_005",
          donorId = "donor_bistro",
          donorName = "Elena Rostova",
          donorPhone = "+1 (555) 345-6789",
          donorOrg = "Green Leaf Artisan Bistro",
          title = "Organic Quinoa Bowls & Harvest Salads",
          description = "Individually boxed compostable bowls with dressing cups on the side.",
          quantityMeals = 35,
          quantityWeightKg = 15.0,
          foodType = FoodType.VEG.name,
          cookedTimeMillis = now - (3 * oneHour),
          expiryTimeMillis = now + (5 * oneHour),
          pickupAddress = "450 Valencia St, Mission",
          pickupInstructions = "Handed off to courier Sarah.",
          latitude = 37.7648,
          longitude = -122.4219,
          status = DonationStatus.OUT_FOR_DELIVERY.name,
          claimedByNgoId = "ngo_care",
          claimedByNgoName = "Care & Nourish Youth Pantry",
          claimedAtMillis = now - (45 * 60 * 1000L),
          assignedVolunteerId = "vol_sarah",
          assignedVolunteerName = "Sarah Jenkins",
          outForDeliveryAtMillis = now - (15 * 60 * 1000L),
          verificationOtp = "7721",
          createdAtMillis = now - (3 * oneHour)
        ),
        // Delivered 1 (Past success)
        FoodDonationEntity(
          id = "don_006",
          donorId = "donor_hyatt",
          donorName = "Chef Marco",
          donorPhone = "+1 (555) 234-5678",
          donorOrg = "Grand Hyatt Regency",
          title = "Breakfast Pastries, Frittata & Fresh Fruit",
          description = "Surplus breakfast buffet trays delivered to shelter morning breakfast program.",
          quantityMeals = 60,
          quantityWeightKg = 25.0,
          foodType = FoodType.VEG.name,
          cookedTimeMillis = now - (24 * oneHour),
          expiryTimeMillis = now - (18 * oneHour),
          pickupAddress = "120 Market St, Downtown",
          latitude = 37.7937,
          longitude = -122.3965,
          status = DonationStatus.DELIVERED.name,
          claimedByNgoId = "ngo_hope",
          claimedByNgoName = "Hope Community Food Bank",
          claimedAtMillis = now - (22 * oneHour),
          assignedVolunteerId = "vol_marcus",
          assignedVolunteerName = "Marcus Chen",
          outForDeliveryAtMillis = now - (21 * oneHour),
          deliveredAtMillis = now - (20 * oneHour),
          deliveryNotes = "Successfully delivered to shelter kitchen. Temperature verified at 40°F.",
          verificationOtp = "1940",
          createdAtMillis = now - (25 * oneHour)
        ),
        // Delivered 2
        FoodDonationEntity(
          id = "don_007",
          donorId = "donor_bakery",
          donorName = "Pierre Boulanger",
          donorPhone = "+1 (555) 456-7890",
          donorOrg = "Golden Crust Organic Bakery",
          title = "50 Whole Grain Loaves & Croissants",
          description = "End of week bake distributed to family pantries.",
          quantityMeals = 50,
          quantityWeightKg = 20.0,
          foodType = FoodType.BAKERY.name,
          cookedTimeMillis = now - (48 * oneHour),
          expiryTimeMillis = now - (30 * oneHour),
          pickupAddress = "782 Columbus Ave, North Beach",
          latitude = 37.8012,
          longitude = -122.4111,
          status = DonationStatus.DELIVERED.name,
          claimedByNgoId = "ngo_care",
          claimedByNgoName = "Care & Nourish Youth Pantry",
          claimedAtMillis = now - (46 * oneHour),
          assignedVolunteerId = "vol_sarah",
          assignedVolunteerName = "Sarah Jenkins",
          outForDeliveryAtMillis = now - (45 * oneHour),
          deliveredAtMillis = now - (44 * oneHour),
          deliveryNotes = "Pantry volunteers distributed loaves to 30 waiting families.",
          verificationOtp = "4401",
          createdAtMillis = now - (50 * oneHour)
        )
      )
      donationDao.insertDonations(initialDonations)
    }
  }
}
