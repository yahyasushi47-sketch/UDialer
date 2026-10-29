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
    assertEquals("PulseDial", appName)
  }

  @Test
  fun `test T9 predictive search matches contact name and phone`() {
    val contacts = listOf(
        com.example.data.model.Contact(name = "Mom", phone = "555-1234"),
        com.example.data.model.Contact(name = "Sarah", phone = "555-5678"),
        com.example.data.model.Contact(name = "David", phone = "555-9012")
    )
    // 666 matches Mom
    val matchesMom = com.example.util.T9SearchEngine.match(contacts, "666")
    assertEquals(1, matchesMom.size)
    assertEquals("Mom", matchesMom.first().contact.name)

    // 727 matches Sarah
    val matchesSarah = com.example.util.T9SearchEngine.match(contacts, "727")
    assertEquals(1, matchesSarah.size)
    assertEquals("Sarah", matchesSarah.first().contact.name)
  }
}
