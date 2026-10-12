package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldPrimary

@Composable
fun RoleSelectorDialog(
  users: List<UserEntity>,
  currentUser: UserEntity?,
  onSelectUser: (String) -> Unit,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Column {
        Text(
          text = "Switch Active Role",
          fontWeight = FontWeight.Bold,
          style = MaterialTheme.typography.titleMedium
        )
        Text(
          text = "Experience the app from Donor, NGO, or Volunteer perspectives",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    },
    text = {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        items(users) { user ->
          val isSelected = user.id == currentUser?.id
          val roleIcon = when (user.userRole) {
            UserRole.DONOR -> Icons.Default.Business
            UserRole.NGO -> Icons.Default.VolunteerActivism
            UserRole.VOLUNTEER -> Icons.Default.DirectionsBike
          }

          Card(
            modifier = Modifier
              .fillMaxWidth()
              .testTag("role_option_${user.id}")
              .clickable {
                onSelectUser(user.id)
                onDismiss()
              },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (isSelected) EmeraldContainer else MaterialTheme.colorScheme.surfaceVariant
            ),
            border = if (isSelected) {
              androidx.compose.foundation.BorderStroke(1.5.dp, EmeraldPrimary)
            } else null
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(40.dp)
                  .background(
                    if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.surface,
                    CircleShape
                  ),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = roleIcon,
                  contentDescription = null,
                  tint = if (isSelected) Color.White else MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = user.organization.ifBlank { user.name },
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                  )
                }
                Text(
                  text = "${user.userRole.label} • ${user.address}",
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
              if (isSelected) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = "Selected",
                  tint = EmeraldPrimary,
                  modifier = Modifier.size(20.dp)
                )
              }
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text("Close")
      }
    }
  )
}
