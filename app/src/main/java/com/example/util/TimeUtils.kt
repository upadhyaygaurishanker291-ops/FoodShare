package com.example.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TimeUtils {
  private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
  private val dateTimeFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())

  fun formatTimeOnly(timestamp: Long): String {
    return timeFormat.format(Date(timestamp))
  }

  fun formatDateTime(timestamp: Long): String {
    return dateTimeFormat.format(Date(timestamp))
  }

  fun formatCookedAgo(cookedTime: Long): String {
    val diff = System.currentTimeMillis() - cookedTime
    if (diff < 0) return "Just cooked"
    val minutes = diff / (60 * 1000)
    val hours = minutes / 60
    return when {
      minutes < 5 -> "Cooked just now"
      minutes < 60 -> "Cooked ${minutes}m ago"
      hours < 24 -> "Cooked ${hours}h ago"
      else -> "Cooked ${hours / 24}d ago"
    }
  }

  fun formatExpiryWindow(expiryTime: Long): String {
    val diff = expiryTime - System.currentTimeMillis()
    if (diff <= 0) return "Expired"
    val totalMinutes = diff / (60 * 1000)
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
      hours == 0L -> "Expires in ${minutes}m"
      minutes == 0L -> "Expires in ${hours}h"
      else -> "Expires in ${hours}h ${minutes}m"
    }
  }
}
