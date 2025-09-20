package net.amazingdomain.octo.testapplication

import kotlinx.coroutines.*
import mu.KLogger
import mu.KotlinLogging.logger
import net.amazingdomain.octo.networking.ClientSocket
import net.amazingdomain.octo.networking.ServerSocket

fun main() {

    val logger = logger {}

    val listeningPort = 9999

    logger.debug { "Hello world" }
    prepTest(logger, listeningPort)

    sendData(logger, listeningPort)

    Thread.sleep(200000L)
}

fun sendData(logger: KLogger, forwardingPort: Int) {

    val mainScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    val repository = ClientSocket(host = "127.0.0.1", port = forwardingPort)

    mainScope
        .launch {
            repository
                .sharedFlow
                .collect {
                    logger.info("The client side of the test reads '$it'")
                }

        }

    mainScope
        .launch {
            repository
                .sendTextOverTcp("Hello from the client!")
        }
}

fun prepTest(logger: KLogger, listeningPort: Int) {

    val listeningSocket = ServerSocket(port = listeningPort)

    val mainScope = CoroutineScope(Dispatchers.IO + SupervisorJob())


    mainScope
        .launch {
            while (true) {

                logger.debug { "Received ${listeningSocket.getReceivedStatistics()}" }
                logger.debug { "Sent     ${listeningSocket.getSentStatistics()}" }

                delay(5000L) // Adjust the delay as needed
            }
        }

    logger.debug("Starting to read from listening socket")

    mainScope
        .launch {

            listeningSocket
                .sharedFlow
                .collect {
                    logger.info("The server side of the test reads '$it'")
                }
        }

    logger.debug("Starting to open listening socket")

    mainScope
        .launch {
            listeningSocket.connect()
        }

}
