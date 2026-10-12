package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.ui.components.RoleSelectorDialog
import com.example.ui.screens.DonorScreen
import com.example.ui.screens.ImpactLeaderboardScreen
import com.example.ui.screens.NgoFeedScreen
import com.example.ui.screens.VolunteerDeliveryScreen
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodShareApp(
  viewModel: FoodShareViewModel = viewModel()
) {
  val currentUser by viewModel.currentUser.collectAsState()
  val allUsers by viewModel.allUsers.collectAsState()
  val allDonations by viewModel.allDonations.collectAsState()

  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  var selectedNavIndex by remember { mutableIntStateOf(0) }
  var isWebMode by remember { mutableStateOf(false) }
  var showRoleDialog by remember { mutableStateOf(false) }
  var showRegisterDialog by remember { mutableStateOf(false) }

  // Handle system back button to return to home tab if on secondary tab
  BackHandler(enabled = isWebMode || selectedNavIndex != 0) {
    if (isWebMode) {
      isWebMode = false
    } else {
      selectedNavIndex = 0
    }
  }

  Scaffold(
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = {
      CenterAlignedTopAppBar(
        title = {
          Row(
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(32.dp)
                .background(EmeraldContainer, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Fastfood,
                contentDescription = null,
                tint = EmeraldPrimary,
                modifier = Modifier.size(18.dp)
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = if (isWebMode) "FoodShare (Web)" else "FoodShare",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = if (isWebMode) "Netlify Ready Web Application" else "Smart Food Donation Network",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        },
        navigationIcon = {
          // Active Role Pill button
          currentUser?.let { user ->
            Card(
              modifier = Modifier
                .padding(start = 12.dp)
                .testTag("active_role_pill")
                .clickable { showRoleDialog = true },
              shape = RoundedCornerShape(20.dp),
              colors = CardDefaults.cardColors(containerColor = EmeraldContainer)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                val roleIcon = when (user.userRole) {
                  UserRole.DONOR -> Icons.Default.Business
                  UserRole.NGO -> Icons.Default.VolunteerActivism
                  UserRole.VOLUNTEER -> Icons.Default.DirectionsBike
                }
                Icon(
                  imageVector = roleIcon,
                  contentDescription = null,
                  tint = EmeraldPrimary,
                  modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = user.userRole.name,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = EmeraldPrimary
                )
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                  imageVector = Icons.Default.SwapHoriz,
                  contentDescription = "Switch Role",
                  tint = EmeraldPrimary,
                  modifier = Modifier.size(14.dp)
                )
              }
            }
          }
        },
        actions = {
          // Web Mode Toggle Button
          IconButton(
            onClick = {
              isWebMode = !isWebMode
              scope.launch {
                snackbarHostState.showSnackbar(
                  if (isWebMode) "Switched to Netlify Web Application" else "Switched to Native App"
                )
              }
            },
            modifier = Modifier.testTag("action_toggle_web_view")
          ) {
            Icon(
              imageVector = androidx.compose.material.icons.Icons.Default.AddBusiness,
              contentDescription = "Toggle Netlify Web View",
              tint = if (isWebMode) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          IconButton(
            onClick = { showRegisterDialog = true },
            modifier = Modifier.testTag("action_register_user")
          ) {
            Icon(
              imageVector = Icons.Default.PersonAdd,
              contentDescription = "Add Profile",
              tint = EmeraldPrimary
            )
          }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    bottomBar = {
      val userRole = currentUser?.userRole ?: UserRole.DONOR
      val primaryTabLabel = when (userRole) {
        UserRole.DONOR -> "Donor Hub"
        UserRole.NGO -> "NGO Feed"
        UserRole.VOLUNTEER -> "Logistics"
      }
      val primaryTabIcon = when (userRole) {
        UserRole.DONOR -> Icons.Default.Business
        UserRole.NGO -> Icons.Default.VolunteerActivism
        UserRole.VOLUNTEER -> Icons.Default.DirectionsBike
      }

      NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = EmeraldPrimary
      ) {
        NavigationBarItem(
          selected = selectedNavIndex == 0,
          onClick = { selectedNavIndex = 0 },
          icon = { Icon(primaryTabIcon, contentDescription = null) },
          label = { Text(primaryTabLabel, fontSize = 11.sp) },
          colors = NavigationBarItemDefaults.colors(
            indicatorColor = EmeraldContainer,
            selectedIconColor = EmeraldPrimary,
            selectedTextColor = EmeraldPrimary
          ),
          modifier = Modifier.testTag("nav_primary_tab")
        )

        // Quick shortcut to NGO Feed if not already NGO, or Donor Hub if NGO
        NavigationBarItem(
          selected = selectedNavIndex == 1,
          onClick = { selectedNavIndex = 1 },
          icon = {
            Icon(
              if (userRole == UserRole.NGO) Icons.Default.Business else Icons.Default.VolunteerActivism,
              contentDescription = null
            )
          },
          label = {
            Text(
              if (userRole == UserRole.NGO) "Donor Listings" else "Live NGO Feed",
              fontSize = 11.sp
            )
          },
          colors = NavigationBarItemDefaults.colors(
            indicatorColor = EmeraldContainer,
            selectedIconColor = EmeraldPrimary,
            selectedTextColor = EmeraldPrimary
          ),
          modifier = Modifier.testTag("nav_feed_tab")
        )

        NavigationBarItem(
          selected = selectedNavIndex == 2,
          onClick = { selectedNavIndex = 2 },
          icon = { Icon(Icons.Default.DirectionsBike, contentDescription = null) },
          label = { Text("Deliveries", fontSize = 11.sp) },
          colors = NavigationBarItemDefaults.colors(
            indicatorColor = EmeraldContainer,
            selectedIconColor = EmeraldPrimary,
            selectedTextColor = EmeraldPrimary
          ),
          modifier = Modifier.testTag("nav_deliveries_tab")
        )

        NavigationBarItem(
          selected = selectedNavIndex == 3,
          onClick = { selectedNavIndex = 3 },
          icon = { Icon(Icons.Default.Leaderboard, contentDescription = null) },
          label = { Text("Impact", fontSize = 11.sp) },
          colors = NavigationBarItemDefaults.colors(
            indicatorColor = EmeraldContainer,
            selectedIconColor = EmeraldPrimary,
            selectedTextColor = EmeraldPrimary
          ),
          modifier = Modifier.testTag("nav_impact_tab")
        )
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      if (isWebMode) {
        com.example.ui.screens.WebScreen()
      } else {
        val userRole = currentUser?.userRole ?: UserRole.DONOR

        when (selectedNavIndex) {
        0 -> {
          // Role-specific primary screen
          when (userRole) {
            UserRole.DONOR -> DonorScreen(
              viewModel = viewModel,
              currentUser = currentUser,
              allDonations = allDonations,
              snackbarHostState = snackbarHostState
            )
            UserRole.NGO -> NgoFeedScreen(
              viewModel = viewModel,
              currentUser = currentUser,
              allDonations = allDonations,
              snackbarHostState = snackbarHostState
            )
            UserRole.VOLUNTEER -> VolunteerDeliveryScreen(
              viewModel = viewModel,
              currentUser = currentUser,
              allDonations = allDonations,
              snackbarHostState = snackbarHostState
            )
          }
        }
        1 -> {
          // Live NGO Feed / Matching
          NgoFeedScreen(
            viewModel = viewModel,
            currentUser = currentUser,
            allDonations = allDonations,
            snackbarHostState = snackbarHostState
          )
        }
        2 -> {
          // Volunteer Logistics Tracker
          VolunteerDeliveryScreen(
            viewModel = viewModel,
            currentUser = currentUser,
            allDonations = allDonations,
            snackbarHostState = snackbarHostState
          )
        }
        3 -> {
          // Impact & Leaderboard
          ImpactLeaderboardScreen(
            viewModel = viewModel,
            allDonations = allDonations,
            allUsers = allUsers
          )
        }
      }
    }
  }
  }

  // Switch Role Dialog
  if (showRoleDialog) {
    RoleSelectorDialog(
      users = allUsers,
      currentUser = currentUser,
      onSelectUser = { userId ->
        viewModel.switchUser(userId)
        scope.launch {
          snackbarHostState.showSnackbar("Switched active user profile")
        }
      },
      onDismiss = { showRoleDialog = false }
    )
  }

  // Register New User Dialog
  if (showRegisterDialog) {
    RegisterUserDialog(
      onDismiss = { showRegisterDialog = false },
      onRegister = { name, org, role, phone, email, address ->
        viewModel.registerNewUser(name, org, role, phone, email, address) {
          showRegisterDialog = false
          scope.launch {
            snackbarHostState.showSnackbar("New profile created and active!")
          }
        }
      }
    )
  }
}

@Composable
fun RegisterUserDialog(
  onDismiss: () -> Unit,
  onRegister: (String, String, UserRole, String, String, String) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var organization by remember { mutableStateOf("") }
  var selectedRole by remember { mutableStateOf(UserRole.DONOR) }
  var phone by remember { mutableStateOf("") }
  var email by remember { mutableStateOf("") }
  var address by remember { mutableStateOf("") }
  var error by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text("Add Network Partner", fontWeight = FontWeight.Bold)
    },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Text("Select Organization Role:", fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          UserRole.values().forEach { role ->
            FilterChip(
              selected = selectedRole == role,
              onClick = { selectedRole = role },
              label = { Text(role.name, fontSize = 11.sp) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = EmeraldPrimary,
                selectedLabelColor = Color.White
              ),
              modifier = Modifier.weight(1f)
            )
          }
        }

        OutlinedTextField(
          value = organization,
          onValueChange = { organization = it },
          label = { Text("Organization / Business Name *") },
          placeholder = { Text("e.g. Hilton Banquet Services") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = EmeraldPrimary,
            focusedLabelColor = EmeraldPrimary
          )
        )

        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Contact Person Name *") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = EmeraldPrimary,
            focusedLabelColor = EmeraldPrimary
          )
        )

        OutlinedTextField(
          value = phone,
          onValueChange = { phone = it },
          label = { Text("Phone Number") },
          placeholder = { Text("+1 (555) 000-0000") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = EmeraldPrimary,
            focusedLabelColor = EmeraldPrimary
          )
        )

        OutlinedTextField(
          value = address,
          onValueChange = { address = it },
          label = { Text("Facility Address *") },
          placeholder = { Text("500 Market St, Downtown") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = EmeraldPrimary,
            focusedLabelColor = EmeraldPrimary
          )
        )

        if (error != null) {
          Text(text = error ?: "", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (organization.isBlank() || name.isBlank()) {
            error = "Please fill in organization and contact name"
            return@Button
          }
          onRegister(
            name,
            organization,
            selectedRole,
            phone.ifBlank { "+1 (555) 123-4567" },
            email.ifBlank { "partner@foodshare.org" },
            address.ifBlank { "San Francisco City Hub" }
          )
        },
        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
      ) {
        Text("Save & Switch")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}
