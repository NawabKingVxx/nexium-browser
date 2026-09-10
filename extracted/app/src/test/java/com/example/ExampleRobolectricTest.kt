package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.browser.SearchEngine
import com.example.browser.UrlHelper
import com.example.vpn.VpnServerRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("NEXIUM Browser", appName)
  }

  @Test
  fun `url helper resolves plain search query`() {
    val url = UrlHelper.resolveInputToUrl("kotlin coroutines", SearchEngine.GOOGLE)
    assertTrue(url.startsWith("https://www.google.com/search?q="))
  }

  @Test
  fun `url helper resolves domain without scheme`() {
    val url = UrlHelper.resolveInputToUrl("wikipedia.org", SearchEngine.GOOGLE)
    assertEquals("https://wikipedia.org", url)
  }

  @Test
  fun `url helper preserves existing https scheme`() {
    val url = UrlHelper.resolveInputToUrl("https://github.com", SearchEngine.GOOGLE)
    assertEquals("https://github.com", url)
  }

  @Test
  fun `vpn repository has all twelve regions`() {
    assertEquals(12, VpnServerRepository.defaultServers.size)
    val uk = VpnServerRepository.getServerById("uk_london")
    assertNotNull(uk)
    assertEquals("United Kingdom", uk.country)
  }
}
