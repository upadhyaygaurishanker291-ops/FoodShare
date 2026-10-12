package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.example.data.model.UserEntity
import com.example.ui.FoodShareViewModel
import com.example.ui.components.DeliveryStepper
import com.example.ui.components.StatusBadge
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldPrimary
import com.example.util.TimeUtils
import kotlinx.coroutines.launch

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun VolunteerDeliveryScreen(
  viewModel: FoodShareViewModel,
  currentUser: UserEntity?,
  allDonations: List<FoodDonationEntity>,
  snackbarHostState: SnackbarHostState,
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableIntStateOf(0) }
  val scope = rememberCoroutineScope()

  // Available pickup tasks: Claimed by an NGO and either unassigned or assigned to current volunteer
  val availableTasks = allDonations.filter {
    it.donationStatus == DonationStatus.CLAIMED && it.assignedVolunteerId == null
  }

  // Active deliveries for current volunteer
  val myActiveDeliveries = allDonations.filter {
    (it.assignedVolunteerId == currentUser?.id || it.assignedVolunteerId == null) &&
        (it.donationStatus == DonationStatus.CLAIMED || it.donationStatus == DonationStatus.OUT_FOR_DELIVERY)
  }

  val myPastDeliveries = allDonations.filter {
    it.assignedVolunteerId == currentUser?.id && it.donationStatus == DonationStatus.DELIVERED
  }

  var completingDonation by remember { mutableStateOf<FoodDonationEntity?>(null) }
  var otpInput by remember { mutableStateOf("") }
  var notesInput by remember { mutableStateOf("") }
  var otpError by remember { mutableStateOf(false) }

  Column(modifier = modifier.fillMaxSize()) {
    PrimaryTabRow(
      selectedTabIndex = selectedTab,
      containerColor = MaterialTheme.colorScheme.surface,
      contentColor = EmeraldPrimary
    ) {
      Tab(
        selected = selectedTab == 0,
        onClick = { selectedTab = 0 },
        text = { Text("Available Tasks (${availableTasks.size})", fontWeight = FontWeight.SemiBold) },
        modifier = Modifier.testTag("tab_available_tasks")
      )
      Tab(
        selected = selectedTab == 1,
        onClick = { selectedTab = 1 },
        text = {
          Text("Active Deliveries (${myActiveDeliveries.size})", fontWeight = FontWeight.SemiBold)
        },
        modifier = Modifier.testTag("tab_active_deliveries")
      )
      Tab(
        selected = selectedTab == 2,
        onClick = { selectedTab = 2 },
        text = {
          Text("History (${myPastDeliveries.size})", fontWeight = FontWeight.SemiBold)
        },
        modifier = Modifier.testTag("tab_delivery_history")
      )
    }

    when (selectedTab) {
      0 -> {
        // Available Tasks Tab
        AvailableTasksList(
          tasks = availableTasks,
          onAcceptTask = { donationId ->
            viewModel.acceptVolunteerTask(donationId) {
              selectedTab = 1
              scope.launch {
                snackbarHostState.showSnackbar("Task accepted! Route ready for pickup.")
              }
            }
          }
        )
      }
      1 -> {
        // Active Deliveries Tab
        ActiveDeliveriesList(
          deliveries = myActiveDeliveries,
          onStartDelivery = { donation ->
            viewModel.advanceDeliveryStatus(donation.id, donation.donationStatus) {
              scope.launch {
                snackbarHostState.showSnackbar("Food picked up! Status updated to 'Out for Delivery'.")
              }
            }
          },
          onCompleteDeliveryClick = { donation ->
            completingDonation = donation
            otpInput = ""
            notesInput = "Safely delivered in temperature-controlled cooler. Verified fresh."
            otpError = false
          }
        )
      }
      2 -> {
        // Past history
        PastDeliveriesList(deliveries = myPastDeliveries)
      }
    }
  }

  // Complete Delivery Dialog with OTP verification
  if (completingDonation != null) {
    val donation = completingDonation!!
    AlertDialog(
      onDismissRequest = { completingDonation = null },
      icon = {
        Icon(
          imageVector = Icons.Default.CheckCircle,
          contentDescription = null,
          tint = EmeraldPrimary,
          modifier = Modifier.size(36.dp)
        )
      },
      title = {
        Text("Confirm Safe Delivery", fontWeight = FontWeight.Bold)
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "Handing over ${donation.quantityMeals} meals to ${donation.claimedByNgoName ?: "the recipient NGO"}.",
            fontSize = 13.sp
          )

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
              .padding(10.dp)
          ) {
            Text(
              text = "Expected Recipient OTP: ${donation.verificationOtp}",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = EmeraldPrimary
            )
          }

          OutlinedTextField(
            value = otpInput,
            onValueChange = {
              otpInput = it
              otpError = false
            },
            label = { Text("Enter Recipient Verification OTP") },
            placeholder = { Text("e.g. ${donation.verificationOtp}") },
            isError = otpError,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("input_delivery_otp"),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = EmeraldPrimary,
              focusedLabelColor = EmeraldPrimary
            )
          )

          if (otpError) {
            Text(
              text = "Invalid code. Please enter the 4-digit verification code from the recipient.",
              color = MaterialTheme.colorScheme.error,
              fontSize = 11.sp
            )
          }

          OutlinedTextField(
            value = notesInput,
            onValueChange = { notesInput = it },
            label = { Text("Delivery Notes") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 2,
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = EmeraldPrimary,
              focusedLabelColor = EmeraldPrimary
            )
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (otpInput.trim() != donation.verificationOtp && otpInput.isNotBlank()) {
              otpError = true
              return@Button
            }
            viewModel.advanceDeliveryStatus(
              donationId = donation.id,
              currentStatus = DonationStatus.OUT_FOR_DELIVERY,
              deliveryNotes = notesInput
            ) {
              completingDonation = null
              selectedTab = 2
              scope.launch {
                snackbarHostState.showSnackbar("Delivery completed! Impact stats updated.")
              }
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
          modifier = Modifier.testTag("confirm_delivery_complete_button")
        ) {
          Text("Confirm Delivered")
        }
      },
      dismissButton = {
        TextButton(onClick = { completingDonation = null }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
fun AvailableTasksList(
  tasks: List<FoodDonationEntity>,
  onAcceptTask: (String) -> Unit
) {
  if (tasks.isEmpty()) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(32.dp),
      contentAlignment = Alignment.Center
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
          imageVector = Icons.Default.DirectionsBike,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(44.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
          text = "No pending pickup tasks",
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp
        )
        Text(
          text = "New pickup tasks appear when an NGO claims a food listing. Check back soon!",
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
      items(tasks, key = { it.id }) { task ->
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("task_card_${task.id}"),
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
              StatusBadge(status = task.donationStatus)
              Text(
                text = "${task.quantityMeals} Meals • ${task.quantityWeightKg} kg",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = EmeraldPrimary
              )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = task.title,
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Route visual: Pickup -> Dropoff
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .background(EmeraldContainer, RoundedCornerShape(10.dp))
                .padding(12.dp)
            ) {
              Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .size(20.dp)
                      .background(EmeraldPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                  ) {
                    Text("A", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                  }
                  Spacer(modifier = Modifier.width(8.dp))
                  Column {
                    Text(
                      text = "PICKUP: ${task.donorOrg}",
                      fontWeight = FontWeight.Bold,
                      fontSize = 12.sp,
                      color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                      text = task.pickupAddress,
                      fontSize = 11.sp,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .size(20.dp)
                      .background(MaterialTheme.colorScheme.secondary, CircleShape),
                    contentAlignment = Alignment.Center
                  ) {
                    Text("B", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                  }
                  Spacer(modifier = Modifier.width(8.dp))
                  Column {
                    Text(
                      text = "DELIVER TO: ${task.claimedByNgoName ?: "Claimed NGO"}",
                      fontWeight = FontWeight.Bold,
                      fontSize = 12.sp,
                      color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                      text = "Community Food Pantry Facility",
                      fontSize = 11.sp,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
              onClick = { onAcceptTask(task.id) },
              colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("accept_task_button_${task.id}")
            ) {
              Icon(Icons.Default.DirectionsBike, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Accept Delivery Task", fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}

@Composable
fun ActiveDeliveriesList(
  deliveries: List<FoodDonationEntity>,
  onStartDelivery: (FoodDonationEntity) -> Unit,
  onCompleteDeliveryClick: (FoodDonationEntity) -> Unit
) {
  if (deliveries.isEmpty()) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(32.dp),
      contentAlignment = Alignment.Center
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
          imageVector = Icons.Default.DirectionsBike,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(44.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
          text = "No active deliveries",
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp
        )
        Text(
          text = "Accept a task from the 'Available Tasks' tab to start delivering.",
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
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      items(deliveries, key = { it.id }) { item ->
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("active_delivery_card_${item.id}"),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
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
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = EmeraldPrimary
              )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = item.title,
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))
            // Interactive Stepper
            DeliveryStepper(currentStatus = item.donationStatus)

            Spacer(modifier = Modifier.height(14.dp))

            // Details card
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                .padding(12.dp)
            ) {
              Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                  text = "Pickup Location: ${item.pickupAddress}",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Medium
                )
                if (item.pickupInstructions.isNotBlank()) {
                  Text(
                    text = "Instructions: ${item.pickupInstructions}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
                Text(
                  text = "Donor Contact: ${item.donorName} (${item.donorPhone})",
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = "Recipient NGO: ${item.claimedByNgoName}",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = EmeraldPrimary
                )
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Workflow action button based on current stage
            if (item.donationStatus == DonationStatus.CLAIMED) {
              Button(
                onClick = { onStartDelivery(item) },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("mark_out_for_delivery_button_${item.id}")
              ) {
                Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Picked Up Food • Start Route", fontWeight = FontWeight.Bold)
              }
            } else if (item.donationStatus == DonationStatus.OUT_FOR_DELIVERY) {
              Button(
                onClick = { onCompleteDeliveryClick(item) },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("complete_delivery_button_${item.id}")
              ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Arrived & Confirm Delivered", fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun PastDeliveriesList(
  deliveries: List<FoodDonationEntity>
) {
  if (deliveries.isEmpty()) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(32.dp),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "No completed deliveries yet.",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 14.sp
      )
    }
  } else {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      items(deliveries, key = { it.id }) { item ->
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .background(EmeraldContainer, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(text = item.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
              Text(
                text = "${item.quantityMeals} meals delivered to ${item.claimedByNgoName}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              if (item.deliveredAtMillis != null) {
                Text(
                  text = "Completed: ${TimeUtils.formatDateTime(item.deliveredAtMillis)}",
                  fontSize = 10.sp,
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
