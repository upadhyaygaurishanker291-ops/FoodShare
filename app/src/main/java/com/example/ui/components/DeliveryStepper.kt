package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DonationStatus
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.SlateOutlineLight

@Composable
fun DeliveryStepper(
  currentStatus: DonationStatus,
  modifier: Modifier = Modifier
) {
  val steps = listOf(
    DonationStatus.PENDING to "Listed",
    DonationStatus.CLAIMED to "Claimed",
    DonationStatus.OUT_FOR_DELIVERY to "In Transit",
    DonationStatus.DELIVERED to "Delivered"
  )

  val currentIndex = when (currentStatus) {
    DonationStatus.PENDING -> 0
    DonationStatus.CLAIMED -> 1
    DonationStatus.OUT_FOR_DELIVERY -> 2
    DonationStatus.DELIVERED -> 3
    DonationStatus.CANCELLED -> -1
  }

  Column(modifier = modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      steps.forEachIndexed { index, (status, label) ->
        val isCompleted = index <= currentIndex
        val isCurrent = index == currentIndex

        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.weight(1f)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
          ) {
            // Left connector line
            if (index > 0) {
              Box(
                modifier = Modifier
                  .weight(1f)
                  .height(3.dp)
                  .background(
                    if (index <= currentIndex) EmeraldPrimary else SlateOutlineLight
                  )
              )
            } else {
              Spacer(modifier = Modifier.weight(1f))
            }

            // Circle step indicator
            Box(
              modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(
                  when {
                    isCompleted -> EmeraldPrimary
                    else -> MaterialTheme.colorScheme.surfaceVariant
                  }
                ),
              contentAlignment = Alignment.Center
            ) {
              if (isCompleted) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = "Completed",
                  tint = Color.White,
                  modifier = Modifier.size(16.dp)
                )
              } else {
                Text(
                  text = "${index + 1}",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            // Right connector line
            if (index < steps.lastIndex) {
              Box(
                modifier = Modifier
                  .weight(1f)
                  .height(3.dp)
                  .background(
                    if (index < currentIndex) EmeraldPrimary else SlateOutlineLight
                  )
              )
            } else {
              Spacer(modifier = Modifier.weight(1f))
            }
          }

          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
            color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }
}
