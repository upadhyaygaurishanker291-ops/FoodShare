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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Co2
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DonationStatus
import com.example.data.model.FoodDonationEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.ui.FoodShareViewModel
import com.example.ui.components.ImpactStatCard
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberTertiary
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.TealContainer
import com.example.ui.theme.TealSecondary

data class LeaderboardEntry(
  val name: String,
  val organization: String,
  val metricValue: String,
  val metricLabel: String
)

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ImpactLeaderboardScreen(
  viewModel: FoodShareViewModel,
  allDonations: List<FoodDonationEntity>,
  allUsers: List<UserEntity>,
  modifier: Modifier = Modifier
) {
  val impactSummary by viewModel.impactSummary.collectAsState()
  var leaderboardTab by remember { mutableIntStateOf(0) }

  // Compute Top Donors
  val donorGroups = allDonations
    .filter { it.donationStatus != DonationStatus.CANCELLED }
    .groupBy { it.donorOrg }
    .map { (org, list) ->
      LeaderboardEntry(
        name = list.firstOrNull()?.donorName ?: org,
        organization = org,
        metricValue = "${list.sumOf { it.quantityMeals }}",
        metricLabel = "Meals Shared"
      )
    }
    .sortedByDescending { it.metricValue.toIntOrNull() ?: 0 }

  // Compute Top Volunteers
  val volunteerEntries = listOf(
    LeaderboardEntry("Sarah Jenkins", "EcoBike Deliveries", "14", "Deliveries"),
    LeaderboardEntry("Marcus Chen", "Community EV Express", "9", "Deliveries"),
    LeaderboardEntry("Amina Yusuf", "Pedal Pantry Volunteer", "6", "Deliveries"),
    LeaderboardEntry("Carlos Gomez", "Cargo Bike Hero", "4", "Deliveries")
  )

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Header Banner
    item {
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = EmeraldPrimary),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Column {
              Text(
                text = "Community Impact",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "${impactSummary.totalMealsSaved} Meals Saved",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold
              )
            }
            Box(
              modifier = Modifier
                .size(48.dp)
                .background(Color.White.copy(alpha = 0.2f), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Eco,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Milestone Progress
          val targetMeals = 1000
          val progress = (impactSummary.totalMealsSaved.toFloat() / targetMeals).coerceIn(0f, 1f)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "Milestone: Road to $targetMeals Meals",
              color = Color.White.copy(alpha = 0.9f),
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold
            )
            Text(
              text = "${(progress * 100).toInt()}%",
              color = Color.White,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )
          }
          Spacer(modifier = Modifier.height(6.dp))
          LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
              .fillMaxWidth()
              .height(8.dp)
              .clip(RoundedCornerShape(4.dp)),
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.25f)
          )
        }
      }
    }

    // Impact KPI Grid
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        ImpactStatCard(
          title = "Food Diverted",
          value = "${impactSummary.totalKgDiverted.toInt()} kg",
          subtitle = "From landfills",
          icon = Icons.Default.Scale,
          iconTint = EmeraldPrimary,
          containerColor = EmeraldContainer,
          modifier = Modifier.weight(1f)
        )
        ImpactStatCard(
          title = "CO2e Prevented",
          value = "${impactSummary.co2AvoidedKg.toInt()} kg",
          subtitle = "Emissions offset",
          icon = Icons.Default.Co2,
          iconTint = TealSecondary,
          containerColor = TealContainer,
          modifier = Modifier.weight(1f)
        )
      }
    }

    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        ImpactStatCard(
          title = "Active Donors",
          value = "${impactSummary.activeDonorsCount}",
          subtitle = "Hotels & Caterers",
          icon = Icons.Default.Business,
          iconTint = AmberTertiary,
          containerColor = AmberContainer,
          modifier = Modifier.weight(1f)
        )
        ImpactStatCard(
          title = "Partner NGOs",
          value = "${impactSummary.partnerNgosCount}",
          subtitle = "Food banks & shelters",
          icon = Icons.Default.VolunteerActivism,
          iconTint = EmeraldPrimary,
          containerColor = EmeraldContainer,
          modifier = Modifier.weight(1f)
        )
      }
    }

    // Leaderboards section
    item {
      Spacer(modifier = Modifier.height(4.dp))
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.EmojiEvents,
            contentDescription = null,
            tint = AmberTertiary,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Community Leaderboard",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      PrimaryTabRow(
        selectedTabIndex = leaderboardTab,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = EmeraldPrimary,
        modifier = Modifier.clip(RoundedCornerShape(10.dp))
      ) {
        Tab(
          selected = leaderboardTab == 0,
          onClick = { leaderboardTab = 0 },
          text = { Text("Top Donors", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
          modifier = Modifier.testTag("tab_top_donors")
        )
        Tab(
          selected = leaderboardTab == 1,
          onClick = { leaderboardTab = 1 },
          text = { Text("Top Volunteers", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
          modifier = Modifier.testTag("tab_top_volunteers")
        )
      }
    }

    val currentList = if (leaderboardTab == 0) donorGroups else volunteerEntries

    itemsIndexed(currentList) { index, entry ->
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("leaderboard_item_$index"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Rank Badge
          val rankColor = when (index) {
            0 -> Color(0xFFEAB308) // Gold
            1 -> Color(0xFF94A3B8) // Silver
            2 -> Color(0xFFD97706) // Bronze
            else -> MaterialTheme.colorScheme.surfaceVariant
          }
          val textColor = if (index < 3) Color.White else MaterialTheme.colorScheme.onSurface

          Box(
            modifier = Modifier
              .size(32.dp)
              .background(rankColor, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "#${index + 1}",
              color = textColor,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = entry.organization,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
            Text(
              text = entry.name,
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Column(horizontalAlignment = Alignment.End) {
            Text(
              text = entry.metricValue,
              fontWeight = FontWeight.ExtraBold,
              fontSize = 16.sp,
              color = EmeraldPrimary
            )
            Text(
              text = entry.metricLabel,
              fontSize = 10.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}
