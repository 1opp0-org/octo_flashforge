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
        val info = ConfigurationRepository.ConfigurationInfo(
            label = "test",
            ipAddress = "192.168.1.100",
            gcodePort = 8899,
            videoPort = 9090,
        )
        val expectedUrl = "http://192.168.1.100:9090?action=stream"

        val actualUrl = info.buildVideoUrl()
        assertEquals(expectedUrl, actualUrl)
    }

    @Test
    fun `buildVideoUrl with localhost returns correct URL`() {
        val info = ConfigurationRepository.ConfigurationInfo(
            label = "test",
            ipAddress = "localhost",
            gcodePort = 8899,
            videoPort = 9090,
        )
        val expectedUrl = "http://localhost:9090?action=stream"
        val actualUrl = info.buildVideoUrl()
        assertEquals(expectedUrl, actualUrl)
    }

    @Test
    fun `buildVideoUrl with empty string returns null with empty host`() {

        val info = ConfigurationRepository.ConfigurationInfo(
            label = "test",
            ipAddress = "",
            gcodePort = 9090,
            videoPort = 8080
        )
        val expectedUrl = null
        val actualUrl = info.buildVideoUrl()
        assertEquals(expectedUrl, actualUrl)
    }

    @Test
    fun `buildVideoUrl with input that already has http scheme results correct scheme`() {
        // This test highlights a potential issue if the input is not just an IP/hostname
        val ipAddress = ""
        val info = ConfigurationRepository.ConfigurationInfo(
            label = "test",
            ipAddress = "http://192.168.1.50:",
            gcodePort = 8899,
            videoPort = 9091,
        )
        // The function blindly prepends "http://", leading to this:
        val expectedUrl = "http://192.168.1.50:9091?action=stream"
        val actualUrl = info.buildVideoUrl()
        assertEquals(expectedUrl, actualUrl)
    }


    // Optional: Test for potentially invalid characters (depends on Uri.parse leniency)
    // android.net.Uri.parse is often lenient and might encode invalid characters.
    @Test
    fun `buildVideoUrl with host containing space gets encoded`() {

        val info = ConfigurationRepository.ConfigurationInfo(
            label = "test",
            ipAddress = "invalid host",
            gcodePort = 8899,
            videoPort = 9090,
        )
        val expectedUrl = "http://invalid host:9090?action=stream" // %20 is URL encoding for space
        val actualUrl = info.buildVideoUrl()
        assertEquals(expectedUrl, actualUrl)
    }
}



