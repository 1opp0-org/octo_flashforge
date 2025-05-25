package net.amazingdomain.octo.networking

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import mu.KotlinLogging.logger
import net.amazingdomain.octo.gcode.Virtual3dPrinter

/**
 * Creates a listening socket over IP on port [port], and simulates responses that a 3d printer would do.
 */
class VirtualIp3dPrinter(private val port: Int, private val bufferSize: Int = 100) {

    private val logger = logger {}
    private val virtualPrinter = Virtual3dPrinter()
    private var _server: ServerSocket? = null

    // spins up socket; if you want to send and receive commands, you need to write your own IP reading/writing code
    fun turnOn() {

        val mainScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

        val server = ServerSocket(port = port, bufferSize = bufferSize)


        // Listens to the incoming data on IP (even before the socket is connected),
        // sends it for processing on virtual printer
        mainScope
            .launch {
                server
                    ?.sharedFlow
                    ?.collect {
                        logger.debug { "Printer got command $it" }
                        virtualPrinter
                            .writeGcode(it)
                    }
            }

        // Start socket
        mainScope
            .launch {
                server.connect()
            }

        // Reads data from virtual printer
        // sends it back to client through socket
        mainScope
            .launch {
                while (true) {
                    virtualPrinter
                        .readData()
                        .let { response ->
                            logger.debug {
                                "Printer replies command " + response.joinToString(
                                    separator = "\n",
                                    prefix = "\n"
                                )

                            }
                            server.reply(response.joinToString (separator = "\n"))
                        }
                }
            }



        _server = server

    }

    // turns off IP server
    fun turnOff() {

        CoroutineScope(Dispatchers.IO)
            .launch {
                _server?.disconnect()
                _server = null
            }
    }

}