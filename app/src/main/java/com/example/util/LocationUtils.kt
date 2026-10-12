package com.example.util

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class LocationCoordinate(
  val latitude: Double,
  val longitude: Double,
  val name: String
)

object LocationUtils {
  // Common city location presets for easy geotagging and testing
  val CITY_PRESETS = listOf(
    LocationCoordinate(37.7937, -122.3965, "Downtown / Financial District"),
    LocationCoordinate(37.7816, -122.4058, "SoMa / Tech Corridor"),
    LocationCoordinate(37.7648, -122.4219, "Mission District"),
    LocationCoordinate(37.8012, -122.4111, "North Beach / Waterfront"),
    LocationCoordinate(37.7831, -122.4143, "Civic Center / Mid-Market"),
    LocationCoordinate(37.7680, -122.4467, "Buena Vista / Castro")
  )

  /**
   * Calculates distance between two coordinates in kilometers using Haversine formula
   */
  fun calculateDistanceKm(
    lat1: Double,
    lon1: Double,
    lat2: Double,
    lon2: Double
  ): Double {
    val earthRadiusKm = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
        cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
        sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return earthRadiusKm * c
  }

  fun formatDistance(distanceKm: Double): String {
    return if (distanceKm < 1.0) {
      "${(distanceKm * 1000).toInt()} m away"
    } else {
      String.format("%.1f km away", distanceKm)
    }
  }
}
