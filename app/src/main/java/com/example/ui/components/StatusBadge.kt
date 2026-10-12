package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DonationStatus
import com.example.ui.theme.StatusClaimedBg
import com.example.ui.theme.StatusClaimedText
import com.example.ui.theme.StatusDeliveredBg
import com.example.ui.theme.StatusDeliveredText
import com.example.ui.theme.StatusOutForDeliveryBg
import com.example.ui.theme.StatusOutForDeliveryText
import com.example.ui.theme.StatusPendingBg
import com.example.ui.theme.StatusPendingText

@Composable
fun StatusBadge(
  status: DonationStatus,
  modifier: Modifier = Modifier
) {
  val (bgColor, textColor, label) = when (status) {
    DonationStatus.PENDING -> Triple(StatusPendingBg, StatusPendingText, "Available")
    DonationStatus.CLAIMED -> Triple(StatusClaimedBg, StatusClaimedText, "Claimed")
    DonationStatus.OUT_FOR_DELIVERY -> Triple(StatusOutForDeliveryBg, StatusOutForDeliveryText, "In Transit")
    DonationStatus.DELIVERED -> Triple(StatusDeliveredBg, StatusDeliveredText, "Delivered")
    DonationStatus.CANCELLED -> Triple(Color(0xFFFEE2E2), Color(0xFF991B1B), "Cancelled")
  }

  Box(
    modifier = modifier
      .background(color = bgColor, shape = RoundedCornerShape(12.dp))
      .padding(horizontal = 8.dp, vertical = 4.dp)
  ) {
    Text(
      text = label,
      color = textColor,
      fontSize = 11.sp,
      fontWeight = FontWeight.SemiBold,
      style = MaterialTheme.typography.labelSmall
    )
  }
}
