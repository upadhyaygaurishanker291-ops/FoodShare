package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("FoodShare", appName)
  }

  @Test
  fun `test haversine distance calculation`() {
    // San Francisco Downtown to Mission District ~3.8km
    val dist = com.example.util.LocationUtils.calculateDistanceKm(37.7937, -122.3965, 37.7648, -122.4219)
    assertTrue(dist > 3.0 && dist < 5.0)
  }
}
