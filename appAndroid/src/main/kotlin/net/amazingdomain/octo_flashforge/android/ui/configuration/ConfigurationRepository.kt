package net.amazingdomain.octo_flashforge.android.ui.configuration

import android.content.Context
import android.util.Log
import io.ktor.http.*
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber

// TODO extract all magic strings as private vals
// TODO convert [Log] to [Timber]
/**
 * Does persistence AND business logic for ip addresses and video streaming url for 3d printers.
 *
 * Tested only on FlashForge Adventurer 3
 */
class ConfigurationRepository {


    fun saveConfiguration(context: Context, label: String, ipAddress: String) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        // Save as current configuration
        prefs.edit()
            .putString("current_printer_label", label.trim())
            .putString("current_printer_ip", ipAddress.trim())
            .apply()

        // Save to configurations list
        val configurations = JSONArray(prefs.getString("saved_configurations", "[]"))
        val configMap = mutableMapOf<String, String>()

        // Convert existing configurations to map
        for (i in 0 until configurations.length()) {
            val config = configurations.getJSONObject(i)
            configMap[config.getString("label")] = config.getString("ip")
        }

        // Add or update current configuration
        configMap[label] = ipAddress

        // Convert back to JSONArray
        val newConfigurations = JSONArray()
        configMap.forEach { (savedLabel, savedIp) ->
            newConfigurations.put(JSONObject().apply {
                put("label", savedLabel)
                put("ip", savedIp)
            })
        }

        prefs.edit()
            .putString("saved_configurations", newConfigurations.toString())
            .apply()
    }

    fun loadActiveConfiguration(context: Context): Pair<String, String>? {

        val activeLabel = getActiveLabel(context)
        return loadAllConfigurations(context)
            .firstOrNull { it.first == activeLabel }
            ?.also { Log.i("ConfigurationRepository", "Loaded active configuration: $it") }


    }

    fun loadAllConfigurations(context: Context): List<Pair<String, String>> {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val configurationsJson = prefs.getString("saved_configurations", "[]")
        val configurations = JSONArray(configurationsJson)

        Log.i("ConfigurationRepository", "Loaded all configurations: $configurations")

        return List(configurations.length()) { i ->
            val config = configurations.getJSONObject(i)
            Pair(
                first = config.getString("label").trim(),
                second = config.getString("ip").trim()
            )
        }
    }

    fun saveActiveLabel(context: Context, savedLabel: String) {

        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("current_printer_label", savedLabel)
            .apply()

        Log.i("ConfigurationRepository", "Saved active label: $savedLabel")
    }

    fun getActiveLabel(context: Context): String {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val activeLabel = prefs.getString("current_printer_label", "") ?: ""

        Log.i("ConfigurationRepository", "GET Active label: $activeLabel")
        return activeLabel
    }

    fun getVideoUrl(context: Context): String? {

        return loadActiveConfiguration(context)
            ?.second
            ?.let { buildVideoUrl(it) }
            ?.also { Timber.i("GET Video URL: $it") }

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