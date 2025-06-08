package net.amazingdomain.octo_flashforge.android.ui.configuration

import io.mockk.mockk
import net.amazingdomain.octo_flashforge.utils.TestTree
import timber.log.Timber
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

class ConfigurationRepositoryTest {

    init {
        Timber.plant(TestTree())
    }

    private val repository = ConfigurationRepository(applicationContext = mockk())

    @Test
    fun `buildVideoUrl with valid IPv4 address returns correct URL`() {
        val ipAddress = "192.168.1.100:9090"
        val expectedUrl = "http://192.168.1.100:9090?action=stream"
        val actualUrl = repository.buildVideoUrl(ipAddress)
        assertEquals(expectedUrl, actualUrl)
    }

    @Test
    fun `buildVideoUrl with localhost returns correct URL`() {
        val ipAddress = "localhost:9090"
        val expectedUrl = "http://localhost:9090?action=stream"
        val actualUrl = repository.buildVideoUrl(ipAddress)
        assertEquals(expectedUrl, actualUrl)
    }

    @Test
    fun `buildVideoUrl with empty string returns null with empty host`() {

        val ipAddress = ""
        val expectedUrl = null
        val actualUrl = repository.buildVideoUrl(ipAddress)
        assertEquals(expectedUrl, actualUrl)
    }

    @Test
    fun `buildVideoUrl with IP address containing port returns URL with port`() {
        val ipAddress = "192.168.1.100:8080"
        val expectedUrl = "http://192.168.1.100:8080?action=stream"
        val actualUrl = repository.buildVideoUrl(ipAddress)
        assertEquals(expectedUrl, actualUrl)
    }

    @Test
    fun `buildVideoUrl with input that already has http scheme results correct scheme`() {
        // This test highlights a potential issue if the input is not just an IP/hostname
        val ipAddress = "http://192.168.1.50:9090"
        // The function blindly prepends "http://", leading to this:
        val expectedUrl = "http://192.168.1.50:9090?action=stream"
        val actualUrl = repository.buildVideoUrl(ipAddress)
        assertEquals(expectedUrl, actualUrl)
    }


    // Optional: Test for potentially invalid characters (depends on Uri.parse leniency)
    // android.net.Uri.parse is often lenient and might encode invalid characters.
    @Ignore
    @Test
    fun `buildVideoUrl with host containing space gets encoded`() {
        val ipAddress = "invalid host"
        val expectedUrl = "http://invalid%20host?action=stream" // %20 is URL encoding for space
        val actualUrl = repository.buildVideoUrl(ipAddress)
        assertEquals(expectedUrl, actualUrl)
    }
}



