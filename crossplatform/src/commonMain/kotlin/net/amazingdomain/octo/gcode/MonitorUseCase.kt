package net.amazingdomain.octo.gcode

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.mapNotNull
import mu.KotlinLogging
import net.amazingdomain.octo.networking.ClientSocket
import org.jetbrains.annotations.VisibleForTesting
import kotlin.coroutines.cancellation.CancellationException

/**
 *
 */
class MonitorUseCase(private val clientSocket: ClientSocket,
    private val loopIntervalMs:Long = 500L) {

    private val logger = KotlinLogging.logger {}

    private val mutableFlow = MutableSharedFlow<GCodeResponse>(
        replay = 0,
        extraBufferCapacity = 100,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val sharedFlow = mutableFlow as Flow<GCodeResponse>

    private var listeningJob: Job? = null

    init {
        listenForFlow()
    }

    private fun listenForFlow() {


        listeningJob = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            .launch {

                async {
                    logger.info("Starting to listen to socket")
                    clientSocket
                        .sharedFlow
                        .mapNotNull { parseGcodeResponse(it) }
                        .collect(mutableFlow)

                    logger.info("End to listen to socket")
                }

                async {
                    logger.info("Starting to write gcode queries")
                    while (isActive) {
                        delay(loopIntervalMs)
                        send(GCode.readTemperature)
                        logger.trace { "I'm in a loop" }
                    }
                    logger.info("End loop writing queries")
                }
            }

    }

    private fun parseGcodeResponse(response: String?): GCodeResponse? {

        logger.trace { "response raw  = $response" }

        return try {
            parseResponse(response)
        } catch (t: Throwable) {
            logger.warn { t }
            null
        }
            .also {
                logger.trace { "response gcode = $it" }
            }
    }

    private suspend fun send(gCode: GCode) {
        try {
            clientSocket
                .sendTextOverTcp(gCode.code)
        } catch (t: Throwable) {
            logger.warn { t }
        }
    }

    suspend fun disconnect() {
        logger.info("Disconnect")
        listeningJob?.cancel(cause = CancellationException("Called from disconnect"))

        clientSocket.disconnect()
    }

    fun parseResponse(gcodeResponse: String?): GCodeResponse? {
        return parseTemperatureResponse(gcodeResponse)
    }

    /**
     * It will pay attention to the following message:
     * T0:39/0 B:40/0
     *
     * as TemperatureQuery(39,40,0,0)
     *
     */
    @VisibleForTesting
    internal fun parseTemperatureResponse(gcodeResponse: String?): GCodeResponse.Temperature? {

        if (gcodeResponse == null) return null

        // Regex to capture the temperature values
        val regex = Regex("""T0:(\d+)/(\d+)\s+B:(\d+)/(\d+)""") // TODO make this constant
        val matchResult = regex.find(gcodeResponse)

        return matchResult
            ?.let {
                val (extruderCurrent, extruderTarget, baseCurrent, baseTarget) = it.destructured

                GCodeResponse.Temperature(
                    extruderCurrentTemp = extruderCurrent.toInt(),
                    baseCurrentTemp = baseCurrent.toInt(),
                    extruderTargetTemp = extruderTarget.toInt(),
                    baseTargetTemp = baseTarget.toInt()
                )
            }
    }
}

sealed class GCodeResponse {


    data class Temperature(
        val extruderCurrentTemp: Int, val baseCurrentTemp: Int,
        val extruderTargetTemp: Int, val baseTargetTemp: Int,
    ) : GCodeResponse()
}
