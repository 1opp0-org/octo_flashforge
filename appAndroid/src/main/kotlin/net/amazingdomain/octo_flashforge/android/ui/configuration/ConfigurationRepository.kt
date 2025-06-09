package net.amazingdomain.octo_flashforge.android.ui.configuration

import android.content.Context
import androidx.core.content.edit
import io.ktor.http.*
import net.amazingdomain.octo_flashforge.android.ui.configuration.ConfigurationRepository.ConfigurationInfo.Companion.DEFAULT_GCODE_PORT
import net.amazingdomain.octo_flashforge.android.ui.configuration.ConfigurationRepository.ConfigurationInfo.Companion.DEFAULT_VIDEO_PORT
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber

// TODO extract all magic strings as private vals
// TODO convert [Log] to [Timber]
// TODO improve this class to support GCode IP address and port`
/**
 * Does persistence AND business logic for ip addresses and video streaming url for 3d printers.
 *
 * Tested only on FlashForge Adventurer 3
 */
class ConfigurationRepository(private val applicationContext: Context) {

    companion object Strings {
        private const val SHARED_PREFERENCES_FILE = "app_prefs"
        private const val KEY_SAVED_CONFIGURATIONS = "saved_configurations" // top level key

        private const val KEY_LABEL = "label"
        private const val KEY_IP = "ip"
        private const val KEY_CURRENT_PRINTER_LABEL = "current_printer_label"
        private const val KEY_CURRENT_PRINTER_IP =
            "current_printer_ip" // this seems wrong, scheduled to be removed
    }

    data class ConfigurationInfo(
        val label: String,
        internal val ipAddress: String,
        internal val gcodePort: Int,
        internal val videoPort: Int,
    ) {
        companion object {
            val DEFAULT_GCODE_PORT = 8899
            val DEFAULT_VIDEO_PORT = 9090
        }
    }


    private fun getSharedPreferences() =
        applicationContext.getSharedPreferences(SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE)

    fun saveConfiguration(label: String, ipAddress: String) {
        val prefs = getSharedPreferences()

        // Save as current configuration
        prefs.edit {
            putString(KEY_CURRENT_PRINTER_LABEL, label.trim())
                .putString(KEY_CURRENT_PRINTER_IP, ipAddress.trim())
        }

        // Save to configurations list
        val configurations = JSONArray(prefs.getString(KEY_SAVED_CONFIGURATIONS, "[]"))
        val configMap = mutableMapOf<String, String>()

        // Convert existing configurations to map
        for (i in 0 until configurations.length()) {
            val config = configurations.getJSONObject(i)
            configMap[config.getString(KEY_LABEL)] = config.getString(KEY_IP)
        }

        // Add or update current configuration
        configMap[label] = ipAddress

        // Convert back to JSONArray
        val newConfigurations = JSONArray()
        configMap.forEach { (savedLabel, savedIp) ->
            newConfigurations.put(JSONObject().apply {
                put(KEY_LABEL, savedLabel)
                put(KEY_IP, savedIp)
            })
        }

        prefs.edit {
            putString(KEY_SAVED_CONFIGURATIONS, newConfigurations.toString())
        }
    }


    /**
     * Loads the active configuration based on the saved label.
     *
     * @return Pair of <label, IP address:IP port>, or null if not found.
     */
    fun loadActiveConfiguration(): ConfigurationInfo? {

        val activeLabel = getActiveLabel()
        return loadAllConfigurations()
            .firstOrNull { it.label == activeLabel }
            ?.also { Timber.d("Loaded active configuration: $it") }


    }

    fun loadAllConfigurations(): List<ConfigurationInfo> {
        val prefs = getSharedPreferences()
        val configurationsJson = prefs.getString("saved_configurations", "[]")
        val configurations = JSONArray(configurationsJson)

        Timber.d("Loaded all configurations: $configurations")

        return List(configurations.length()) { i ->
            val config = configurations.getJSONObject(i)
            ConfigurationInfo(
                label = config.getString(KEY_LABEL).trim(),
                ipAddress = config.getString(KEY_IP).trim(),
                gcodePort = DEFAULT_GCODE_PORT,
                videoPort = DEFAULT_VIDEO_PORT
            )
        }
    }


    fun saveActiveLabel(savedLabel: String) {

        getSharedPreferences().edit {
            putString(KEY_CURRENT_PRINTER_LABEL, savedLabel)
        }

        Timber.d("Saved active label: $savedLabel")
    }

    private fun getActiveLabel(): String {
        val prefs = getSharedPreferences()
        val activeLabel = prefs.getString(KEY_CURRENT_PRINTER_LABEL, "") ?: ""

        Timber.d("GET Active label: $activeLabel")
        return activeLabel
    }


}

fun ConfigurationRepository.ConfigurationInfo.getGcodeIpAddress(): String {

    return ipAddress
        .let {
            // sample format of `it` is "192.168.0.10:9090"
            // our quick fix is strip the port and return the rest
            when {
                it.isEmpty() -> null
                it.startsWith("http") -> URLBuilder(it)
                else -> URLBuilder("http://$it")
            }
                ?.host
        }
        ?.also { Timber.i("GET Gcode IP address: $it") }
        ?: throw RuntimeException("Something went wrong on 'getGcodeIpAddress()")
}

fun ConfigurationRepository.ConfigurationInfo.getGcodeIpPort(): Int {

    return ConfigurationRepository.ConfigurationInfo.DEFAULT_GCODE_PORT

}

/**
 * Builds Url for accessing video live stream.
 *
 * Supports only FlashForge Adventurer 3: adds the action=stream query parameter to the given IP address.
 */
fun ConfigurationRepository.ConfigurationInfo.buildVideoUrl(): String? {


    val urlBuilder = when {
        ipAddress.isEmpty() -> null
        ipAddress.startsWith("http") -> URLBuilder(ipAddress)
        else -> URLBuilder("http://$ipAddress")
    }

    Timber.i("Parsed URLBuilder: '${urlBuilder?.build()}' .. input was $ipAddress")

    return urlBuilder
        ?.apply {
            encodedParameters = ParametersBuilder().apply {
                append("action", "stream")
            }
            appendPathSegments(listOf())
        }
        ?.build()
        ?.also { Timber.i("Built URL: $it") }
        ?.toString()
}