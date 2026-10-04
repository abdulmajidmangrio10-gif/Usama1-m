package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
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
    assertEquals("InShot", appName)
  }

  @Test
  fun `video clip trimmed duration computation is correct`() {
    val clip = com.example.model.VideoClip(
      id = "test_clip",
      title = "Intro",
      durationMs = 5000L,
      trimStartMs = 1000L,
      trimEndMs = 4000L,
      speed = 1.0f
    )
    assertEquals(3000L, clip.trimmedDurationMs)
  }

  @Test
  fun `canvas ratio aspect ratio calculation is correct`() {
    val ratio916 = com.example.model.CanvasRatio.RATIO_9_16
    assertEquals(9f / 16f, ratio916.aspectRatio, 0.001f)
    val ratio11 = com.example.model.CanvasRatio.RATIO_1_1
    assertEquals(1f, ratio11.aspectRatio, 0.001f)
  }
}
