package net.amazingdomain.octo.gcode

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.jetbrains.annotations.VisibleForTesting

class MonitorUseCase(private val monitorRepository: MonitorRepository) {

    private val logger = KotlinLogging.logger {}

    data class TemperatureQuery(
        val extruderCurrentTemp: Int, val baseCurrentTemp: Int,
        val extruderTargetTemp: Int, val baseTargetTemp: Int,
    )

    // TODO refactor and clean code, it can be much better
    suspend fun getExtruderTemperature(): Int? {

        var response: TemperatureQuery? = null

        withContext(Dispatchers.IO) {

            val receiveJob = launch {
                monitorRepository.sharedFlow
                    .map { parseResponse(it) }
                    .collect {
                        if (it != null) {
                            response = it
                            coroutineContext.cancel() // Cancel the coroutine when a valid response is received
                        }
                    }
            }

            val sendJob = launch { monitorRepository.sendTextOverTcp(GCode.readTemperature.code) }

            receiveJob.join() // Wait for the response to be received and processed
            sendJob.cancel()
        }

        logger.debug { "7*   response $response" }
        return response?.extruderCurrentTemp
    }

    /**
     * It will pay attention to the following message:
     * T0:39/0 B:40/0
     *
     * as TemperatureQuery(39,40,0,0)
     *
     */
    @VisibleForTesting
    fun parseResponse(gcodeResponse: String?): TemperatureQuery? {

        if (gcodeResponse == null) return null

        // Regex to capture the temperature values
        val regex = Regex("""T0:(\d+)/(\d+)\s+B:(\d+)/(\d+)""") // TODO make this constant
        val matchResult = regex.find(gcodeResponse)

        return matchResult
            ?.let {
                val (extruderCurrent, extruderTarget, baseCurrent, baseTarget) = it.destructured
                TemperatureQuery(
                    extruderCurrentTemp = extruderCurrent.toInt(),
                    baseCurrentTemp = baseCurrent.toInt(),
                    extruderTargetTemp = extruderTarget.toInt(),
                    baseTargetTemp = baseTarget.toInt()
                )
            }
    }

}