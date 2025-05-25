package net.amazingdomain.octo.testapplication

import kotlinx.coroutines.*
import mu.KotlinLogging
import net.amazingdomain.octo.networking.ClientSocket
import net.amazingdomain.octo.gcode.MonitorUseCase
import kotlin.time.DurationUnit
import kotlin.time.toDuration

fun main() {

    val logger = KotlinLogging.logger {}

    logger.debug("Hello network world")

    val socketWatchdogTimeoutMs = 300L
    val repository = ClientSocket(
        host = "127.0.0.1", port = 8899,
        disconnectTimeoutMs = socketWatchdogTimeoutMs,
    )

    val monitorUseCase = MonitorUseCase(repository)

    val mainScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    val job = mainScope.launch {
        monitorUseCase
            .getExtruderTemperature()
            .let {
                logger.info("1st Answer is '$it'C ")
            }


        (socketWatchdogTimeoutMs.toFloat() * 1.2f).toLong().toDuration(DurationUnit.MILLISECONDS)
            .let { delay(it) } // give enough time for disconnection to trigger auto timer

        monitorUseCase
            .getExtruderTemperature()
            .let {
                logger.info("2nd Answer is '$it'C ")
            }

        repository.disconnect()
    }

    runBlocking {
        job.join()
    }

}

// TODO refactor in its own file
data class GCode(val code: String) {

    companion object {
        val readTemperature = GCode("~M105")
    }


}