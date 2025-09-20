package net.amazingdomain.octo.testapplication

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import mu.KotlinLogging
import net.amazingdomain.octo.gcode.MonitorUseCase
import net.amazingdomain.octo.networking.ClientSocket
import kotlin.time.DurationUnit
import kotlin.time.toDuration

fun main() {

    val logger = KotlinLogging.logger {}

    logger.debug("Hello network world")

    val socketWatchdogTimeoutMs = 300L
    val clientSocket = ClientSocket(
//        host = "192.168.0.11", port = 8899,
        host = "127.0.0.1", port = 8899,
        disconnectTimeoutMs = socketWatchdogTimeoutMs,
    )

    val monitorUseCase = MonitorUseCase(clientSocket)

    val mainScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    val job = mainScope.launch {
        monitorUseCase
            .sharedFlow
            .first()
            .let {
                logger.info("1st Answer is '$it'C ")
            }


        (socketWatchdogTimeoutMs.toFloat() * 1.2f).toLong().toDuration(DurationUnit.MILLISECONDS)
            .let { delay(it) } // give enough time for disconnection to trigger auto timer

        monitorUseCase
            .sharedFlow
            .first()
            .let {
                logger.info("2nd Answer is '$it'C ")
            }

        monitorUseCase.disconnect()
    }

    runBlocking {
        job.join()
    }

}

