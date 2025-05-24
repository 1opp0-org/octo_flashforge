package net.amazingdomain.octo_flashforge.android.ui.configuration

import android.content.Context
import androidx.core.content.edit
import io.ktor.http.*
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
        private const val KEY_CURRENT_PRINTER_IP = "current_printer_ip" // this seems wrong, scheduled to be removed
    }


    private fun getSharedPreferences() = applicationContext.getSharedPreferences(SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE)

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
    fun loadActiveConfiguration(): Pair<String, String>? {

        val activeLabel = getActiveLabel()
        return loadAllConfigurations()
            .firstOrNull { it.first == activeLabel }
            ?.also { Timber.d("Loaded active configuration: $it") }


    }

    fun loadAllConfigurations(): List<Pair<String, String>> {
        val prefs = getSharedPreferences()
        val configurationsJson = prefs.getString("saved_configurations", "[]")
        val configurations = JSONArray(configurationsJson)

        Timber.d("Loaded all configurations: $configurations")

        return List(configurations.length()) { i ->
            val config = configurations.getJSONObject(i)
            Pair(
                first = config.getString(KEY_LABEL).trim(),
                second = config.getString(KEY_IP).trim()
            )
        }
    }


    fun saveActiveLabel(savedLabel: String) {

        getSharedPreferences().edit {
            putString(KEY_CURRENT_PRINTER_LABEL, savedLabel)
        }

        Timber.d("Saved active label: $savedLabel")
    }

    fun getActiveLabel(): String {
        val prefs = getSharedPreferences()
        val activeLabel = prefs.getString(KEY_CURRENT_PRINTER_LABEL, "") ?: ""

        Timber.d("GET Active label: $activeLabel")
        return activeLabel
    }

    fun getVideoUrl(): String? {

        return loadActiveConfiguration()
            ?.second
            ?.let { buildVideoUrl(it) }
            ?.also { Timber.i("GET Video URL: $it") }

    }

    fun getGcodeIpAddress(): String? {

        return loadActiveConfiguration()
            ?.second
            ?.let {
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

    }

    // TODO Store and retrieve from persistence
    fun getGcodeIpPort(): Int? {

        return 8899

    }


    /**
     * Builds Url for accessing video live stream.
     *
     * Supports only FlashForge Adventurer 3: adds the action=stream query parameter to the given IP address.
     */
    fun buildVideoUrl(ipAddress: String): String? {


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

}