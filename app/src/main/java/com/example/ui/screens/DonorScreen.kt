package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DonationStatus
import com.example.data.model.FoodDonationEntity
import com.example.data.model.FoodType
import com.example.data.model.UserEntity
import com.example.ui.FoodShareViewModel
import com.example.ui.components.DeliveryStepper
import com.example.ui.components.StatusBadge
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldPrimary
import com.example.util.LocationUtils
import com.example.util.TimeUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun DonorScreen(
  viewModel: FoodShareViewModel,
  currentUser: UserEntity?,
  allDonations: List<FoodDonationEntity>,
  snackbarHostState: SnackbarHostState,
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableIntStateOf(0) }
  val scope = rememberCoroutineScope()

  val myDonations = allDonations.filter { it.donorId == currentUser?.id }

  Column(modifier = modifier.fillMaxSize()) {
    PrimaryTabRow(
      selectedTabIndex = selectedTab,
      containerColor = MaterialTheme.colorScheme.surface,
      contentColor = EmeraldPrimary
    ) {
      Tab(
        selected = selectedTab == 0,
        onClick = { selectedTab = 0 },
        text = { Text("Post Excess Food", fontWeight = FontWeight.SemiBold) },
        modifier = Modifier.testTag("tab_post_food")
      )
      Tab(
        selected = selectedTab == 1,
        onClick = { selectedTab = 1 },
        text = {
          Text("My Listings (${myDonations.size})", fontWeight = FontWeight.SemiBold)
        },
        modifier = Modifier.testTag("tab_my_listings")
      )
    }

    if (selectedTab == 0) {
      PostFoodForm(
        viewModel = viewModel,
        currentUser = currentUser,
        onSubmitted = {
          selectedTab = 1
          scope.launch {
            snackbarHostState.showSnackbar("Food donation posted! Nearby NGOs have been alerted.")
          }
        }
      )
    } else {
      MyListingsList(
        myDonations = myDonations,
        onCancel = { donationId ->
          viewModel.cancelDonation(donationId)
          scope.launch {
            snackbarHostState.showSnackbar("Listing cancelled")
          }
        }
      )
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PostFoodForm(
  viewModel: FoodShareViewModel,
  currentUser: UserEntity?,
  onSubmitted: () -> Unit
) {
  var title by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }
  var quantityMealsText by remember { mutableStateOf("30") }
  var quantityWeightText by remember { mutableStateOf("12.0") }
  var selectedFoodType by remember { mutableStateOf(FoodType.VEG) }
  var cookedHoursAgo by remember { mutableIntStateOf(1) }
  var expiryHours by remember { mutableIntStateOf(4) }
  var pickupAddress by remember { mutableStateOf(currentUser?.address ?: "120 Market St, Downtown") }
  var pickupInstructions by remember { mutableStateOf("Kitchen back entrance, ring loading dock bell") }
  var showLocationPicker by remember { mutableStateOf(false) }

  var errorMessage by remember { mutableStateOf<String?>(null) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = EmeraldContainer),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .background(EmeraldPrimary, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Fastfood,
              contentDescription = null,
              tint = Color.White
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = "Posting as ${currentUser?.organization ?: currentUser?.name}",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Surplus food is immediately matched with local hunger shelters",
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }

    item {
      OutlinedTextField(
        value = title,
        onValueChange = { title = it },
        label = { Text("Food Title / Description *") },
        placeholder = { Text("e.g. Banquet Surplus: Grilled Veggies & Jasmine Rice") },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("input_food_title"),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = EmeraldPrimary,
          focusedLabelColor = EmeraldPrimary
        )
      )
    }

    item {
      OutlinedTextField(
        value = description,
        onValueChange = { description = it },
        label = { Text("Detailed Food Details & Packaging *") },
        placeholder = { Text("e.g. Packed in food-grade aluminum trays, kept in hot-holding cabinet") },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("input_food_desc"),
        minLines = 2,
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = EmeraldPrimary,
          focusedLabelColor = EmeraldPrimary
        )
      )
    }

    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        OutlinedTextField(
          value = quantityMealsText,
          onValueChange = { quantityMealsText = it.filter { char -> char.isDigit() } },
          label = { Text("Est. Meals *") },
          modifier = Modifier
            .weight(1f)
            .testTag("input_quantity_meals"),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = EmeraldPrimary,
            focusedLabelColor = EmeraldPrimary
          )
        )

        OutlinedTextField(
          value = quantityWeightText,
          onValueChange = { quantityWeightText = it },
          label = { Text("Est. Weight (kg) *") },
          modifier = Modifier
            .weight(1f)
            .testTag("input_quantity_weight"),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = EmeraldPrimary,
            focusedLabelColor = EmeraldPrimary
          )
        )
      }
    }

    item {
      Text(
        text = "Food Type Category",
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onSurface
      )
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        FoodType.values().forEach { type ->
          FilterChip(
            selected = selectedFoodType == type,
            onClick = { selectedFoodType = type },
            label = { Text(type.label, fontSize = 12.sp) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = EmeraldPrimary,
              selectedLabelColor = Color.White
            ),
            modifier = Modifier.testTag("chip_food_type_${type.name}")
          )
        }
      }
    }

    item {
      Text(
        text = "Cooked Time & Expiry Window",
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onSurface
      )
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        listOf(1 to "1h ago", 2 to "2h ago", 3 to "3h ago", 0 to "Freshly made").forEach { (h, lbl) ->
          FilterChip(
            selected = cookedHoursAgo == h,
            onClick = { cookedHoursAgo = h },
            label = { Text(lbl, fontSize = 11.sp) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = EmeraldContainer,
              selectedLabelColor = EmeraldPrimary
            ),
            modifier = Modifier.weight(1f)
          )
        }
      }
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "Safe Expiry Window (Pickup within):",
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        listOf(2 to "2 hours", 4 to "4 hours", 8 to "8 hours", 24 to "24 hours").forEach { (exp, lbl) ->
          FilterChip(
            selected = expiryHours == exp,
            onClick = { expiryHours = exp },
            label = { Text(lbl, fontSize = 11.sp) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = EmeraldPrimary,
              selectedLabelColor = Color.White
            ),
            modifier = Modifier.weight(1f)
          )
        }
      }
    }

    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Pickup Location & Geotag",
          fontWeight = FontWeight.SemiBold,
          fontSize = 13.sp
        )
        TextButtonWithIcon(
          icon = Icons.Default.MyLocation,
          label = "Geotag GPS",
          onClick = {
            // Geotag location preset
            val preset = LocationUtils.CITY_PRESETS.first()
            viewModel.setSelectedPostLocation(preset)
            pickupAddress = preset.name
          }
        )
      }

      OutlinedTextField(
        value = pickupAddress,
        onValueChange = { pickupAddress = it },
        label = { Text("Exact Pickup Address *") },
        leadingIcon = {
          Icon(Icons.Default.LocationOn, contentDescription = null, tint = EmeraldPrimary)
        },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("input_pickup_address"),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = EmeraldPrimary,
          focusedLabelColor = EmeraldPrimary
        )
      )

      Spacer(modifier = Modifier.height(6.dp))
      OutlinedTextField(
        value = pickupInstructions,
        onValueChange = { pickupInstructions = it },
        label = { Text("Pickup Instructions / Access Notes") },
        placeholder = { Text("e.g. Loading bay entrance, ask for Shift Manager") },
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = EmeraldPrimary,
          focusedLabelColor = EmeraldPrimary
        )
      )
    }

    if (errorMessage != null) {
      item {
        Text(
          text = errorMessage ?: "",
          color = MaterialTheme.colorScheme.error,
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium
        )
      }
    }

    item {
      Button(
        onClick = {
          if (title.isBlank()) {
            errorMessage = "Please enter a title for the food donation"
            return@Button
          }
          val meals = quantityMealsText.toIntOrNull() ?: 10
          val weight = quantityWeightText.toDoubleOrNull() ?: 5.0
          errorMessage = null

          viewModel.postDonation(
            title = title,
            description = description,
            quantityMeals = meals,
            quantityWeightKg = weight,
            foodType = selectedFoodType,
            cookedHoursAgo = cookedHoursAgo,
            expiryHours = expiryHours,
            pickupAddress = pickupAddress,
            pickupInstructions = pickupInstructions,
            onSuccess = onSubmitted
          )
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("submit_donation_button"),
        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
        shape = RoundedCornerShape(12.dp)
      ) {
        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Publish Donation Listing", fontWeight = FontWeight.Bold, fontSize = 15.sp)
      }
      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
fun TextButtonWithIcon(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  label: String,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .clickable(onClick = onClick)
      .padding(4.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(icon, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
    Spacer(modifier = Modifier.width(4.dp))
    Text(
      text = label,
      color = EmeraldPrimary,
      fontSize = 12.sp,
      fontWeight = FontWeight.SemiBold
    )
  }
}

@Composable
fun MyListingsList(
  myDonations: List<FoodDonationEntity>,
  onCancel: (String) -> Unit
) {
  if (myDonations.isEmpty()) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(32.dp),
      contentAlignment = Alignment.Center
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
          imageVector = Icons.Default.Fastfood,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
          text = "No food listings yet",
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp
        )
        Text(
          text = "Switch to 'Post Excess Food' to donate your surplus banquet or kitchen food.",
          fontSize = 13.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(horizontal = 24.dp),
          textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
      }
    }
  } else {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      items(myDonations, key = { it.id }) { donation ->
        DonationCard(donation = donation, onCancel = onCancel)
      }
    }
  }
}

@Composable
fun DonationCard(
  donation: FoodDonationEntity,
  onCancel: (String) -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("donation_card_${donation.id}"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        StatusBadge(status = donation.donationStatus)
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Schedule,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = TimeUtils.formatExpiryWindow(donation.expiryTimeMillis),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = donation.title,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = MaterialTheme.colorScheme.onSurface
      )

      if (donation.description.isNotBlank()) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = donation.description,
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 2
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        BadgePill(text = "${donation.quantityMeals} Meals")
        BadgePill(text = "${donation.quantityWeightKg} kg")
        BadgePill(text = donation.foodType)
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Stepper showing lifecycle
      DeliveryStepper(currentStatus = donation.donationStatus)

      Spacer(modifier = Modifier.height(12.dp))

      // Pickup details & verification OTP
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
          .padding(10.dp)
      ) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.LocationOn,
              contentDescription = null,
              tint = EmeraldPrimary,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = donation.pickupAddress,
              fontSize = 11.sp,
              fontWeight = FontWeight.Medium
            )
          }

          if (donation.claimedByNgoName != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Claimed by: ${donation.claimedByNgoName}",
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              color = EmeraldPrimary
            )
          }

          if (donation.assignedVolunteerName != null) {
            Text(
              text = "Courier: ${donation.assignedVolunteerName}",
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Spacer(modifier = Modifier.height(4.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.VerifiedUser,
                contentDescription = null,
                tint = EmeraldPrimary,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Pickup Verification Code: ${donation.verificationOtp}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
            }

            if (donation.donationStatus == DonationStatus.PENDING) {
              IconButton(
                onClick = { onCancel(donation.id) },
                modifier = Modifier.size(24.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Delete,
                  contentDescription = "Cancel listing",
                  tint = MaterialTheme.colorScheme.error,
                  modifier = Modifier.size(16.dp)
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun BadgePill(text: String) {
  Box(
    modifier = Modifier
      .background(EmeraldContainer, RoundedCornerShape(6.dp))
      .padding(horizontal = 8.dp, vertical = 3.dp)
  ) {
    Text(
      text = text,
      color = EmeraldPrimary,
      fontSize = 11.sp,
      fontWeight = FontWeight.SemiBold
    )
  }
}
