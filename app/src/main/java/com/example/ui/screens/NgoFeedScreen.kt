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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DonationStatus
import com.example.data.model.FoodDonationEntity
import com.example.data.model.FoodType
import com.example.data.model.UserEntity
import com.example.ui.DonationWithDistance
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
fun NgoFeedScreen(
  viewModel: FoodShareViewModel,
  currentUser: UserEntity?,
  allDonations: List<FoodDonationEntity>,
  snackbarHostState: SnackbarHostState,
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableIntStateOf(0) }
  val scope = rememberCoroutineScope()

  val feedItems by viewModel.ngoFeed.collectAsState()
  val searchQuery by viewModel.searchQuery.collectAsState()
  val selectedType by viewModel.selectedFoodType.collectAsState()
  val maxDistance by viewModel.maxDistanceKm.collectAsState()

  var claimingItem by remember { mutableStateOf<FoodDonationEntity?>(null) }

  val myClaimedDonations = allDonations.filter {
    it.claimedByNgoId == currentUser?.id
  }

  Column(modifier = modifier.fillMaxSize()) {
    PrimaryTabRow(
      selectedTabIndex = selectedTab,
      containerColor = MaterialTheme.colorScheme.surface,
      contentColor = EmeraldPrimary
    ) {
      Tab(
        selected = selectedTab == 0,
        onClick = { selectedTab = 0 },
        text = { Text("Available Feed (${feedItems.size})", fontWeight = FontWeight.SemiBold) },
        modifier = Modifier.testTag("tab_available_feed")
      )
      Tab(
        selected = selectedTab == 1,
        onClick = { selectedTab = 1 },
        text = {
          Text("Our Claimed (${myClaimedDonations.size})", fontWeight = FontWeight.SemiBold)
        },
        modifier = Modifier.testTag("tab_our_claimed")
      )
    }

    if (selectedTab == 0) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 16.dp, vertical = 8.dp)
      ) {
        // Search bar
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { viewModel.setSearchQuery(it) },
          placeholder = { Text("Search by food, hotel, or location...") },
          leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null, tint = EmeraldPrimary)
          },
          trailingIcon = {
            if (searchQuery.isNotBlank()) {
              IconButton(onClick = { viewModel.setSearchQuery("") }) {
                Icon(Icons.Default.Clear, contentDescription = "Clear search")
              }
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("feed_search_input"),
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = EmeraldPrimary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
          )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Food Category Filters
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          item {
            FilterChip(
              selected = selectedType == null,
              onClick = { viewModel.setSelectedFoodType(null) },
              label = { Text("All Categories", fontSize = 11.sp) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = EmeraldPrimary,
                selectedLabelColor = Color.White
              ),
              modifier = Modifier.testTag("filter_all_types")
            )
          }
          items(FoodType.values()) { type ->
            FilterChip(
              selected = selectedType == type,
              onClick = { viewModel.setSelectedFoodType(if (selectedType == type) null else type) },
              label = { Text(type.label, fontSize = 11.sp) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = EmeraldPrimary,
                selectedLabelColor = Color.White
              ),
              modifier = Modifier.testTag("filter_type_${type.name}")
            )
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Proximity radius selector
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.NearMe,
            contentDescription = null,
            tint = EmeraldPrimary,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Within radius:",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.width(8.dp))
          listOf(5.0 to "5 km", 15.0 to "15 km", 25.0 to "25 km", 50.0 to "All").forEach { (dist, label) ->
            val isSelected = maxDistance == dist
            Box(
              modifier = Modifier
                .padding(end = 6.dp)
                .background(
                  if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                  RoundedCornerShape(8.dp)
                )
                .clickable { viewModel.setMaxDistanceKm(dist) }
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Feed list
        if (feedItems.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(32.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                imageVector = Icons.Default.VolunteerActivism,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(44.dp)
              )
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = "No matching donations found",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
              )
              Text(
                text = "Try expanding the radius or clearing filters to see more available listings.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
              )
            }
          }
        } else {
          LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
          ) {
            items(feedItems, key = { it.donation.id }) { item ->
              FeedDonationCard(
                item = item,
                onClaimClicked = { claimingItem = item.donation }
              )
            }
          }
        }
      }
    } else {
      // NGO Claimed list
      ClaimedDonationsList(claimedList = myClaimedDonations)
    }
  }

  // Claim Confirmation Dialog
  if (claimingItem != null) {
    val donation = claimingItem!!
    AlertDialog(
      onDismissRequest = { claimingItem = null },
      icon = {
        Icon(
          imageVector = Icons.Default.VolunteerActivism,
          contentDescription = null,
          tint = EmeraldPrimary,
          modifier = Modifier.size(32.dp)
        )
      },
      title = {
        Text("Claim Food Donation", fontWeight = FontWeight.Bold)
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "Are you sure ${currentUser?.organization ?: "your NGO"} wants to claim this listing?",
            fontSize = 13.sp
          )
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .background(EmeraldContainer, RoundedCornerShape(8.dp))
              .padding(10.dp)
          ) {
            Column {
              Text(
                text = donation.title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "${donation.quantityMeals} meals (${donation.quantityWeightKg} kg) from ${donation.donorOrg}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Pickup: ${donation.pickupAddress}",
                fontSize = 11.sp,
                color = EmeraldPrimary,
                fontWeight = FontWeight.Medium
              )
            }
          }
          Text(
            text = "Once claimed, an eco-courier volunteer will be assigned to transport the food to your shelter.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.claimDonation(donation.id) {
              claimingItem = null
              selectedTab = 1
              scope.launch {
                snackbarHostState.showSnackbar("Successfully claimed! Assigned for logistics dispatch.")
              }
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
          modifier = Modifier.testTag("confirm_claim_button")
        ) {
          Text("Confirm Claim")
        }
      },
      dismissButton = {
        TextButton(onClick = { claimingItem = null }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
fun FeedDonationCard(
  item: DonationWithDistance,
  onClaimClicked: () -> Unit
) {
  val donation = item.donation

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("feed_card_${donation.id}"),
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
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .background(EmeraldContainer, RoundedCornerShape(8.dp))
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = EmeraldPrimary,
                modifier = Modifier.size(13.dp)
              )
              Spacer(modifier = Modifier.width(3.dp))
              Text(
                text = LocationUtils.formatDistance(item.distanceKm),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = EmeraldPrimary
              )
            }
          }
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = donation.donorOrg,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.HourglassBottom,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = TimeUtils.formatExpiryWindow(donation.expiryTimeMillis),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.tertiary
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

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
        BadgePill(text = TimeUtils.formatCookedAgo(donation.cookedTimeMillis))
      }

      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Pickup: ${donation.pickupAddress}",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Button(
          onClick = onClaimClicked,
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.testTag("claim_button_${donation.id}")
        ) {
          Text("Claim / Accept", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
      }
    }
  }
}

@Composable
fun ClaimedDonationsList(
  claimedList: List<FoodDonationEntity>
) {
  if (claimedList.isEmpty()) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(32.dp),
      contentAlignment = Alignment.Center
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
          imageVector = Icons.Default.CheckCircle,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(44.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
          text = "No active claimed donations",
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp
        )
        Text(
          text = "Claim food from the 'Available Feed' tab to route it to your shelter.",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
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
      items(claimedList, key = { it.id }) { item ->
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("claimed_card_${item.id}"),
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
              StatusBadge(status = item.donationStatus)
              Text(
                text = "${item.quantityMeals} Meals • ${item.quantityWeightKg} kg",
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = EmeraldPrimary
              )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = item.title,
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp
            )
            Text(
              text = "Donor: ${item.donorOrg} (${item.donorPhone})",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))
            DeliveryStepper(currentStatus = item.donationStatus)

            Spacer(modifier = Modifier.height(12.dp))
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .background(EmeraldContainer, RoundedCornerShape(8.dp))
                .padding(10.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(
                    text = if (item.assignedVolunteerName != null)
                      "Courier: ${item.assignedVolunteerName}"
                    else
                      "Awaiting courier dispatch...",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Text(
                    text = "Verification OTP: ${item.verificationOtp}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}
