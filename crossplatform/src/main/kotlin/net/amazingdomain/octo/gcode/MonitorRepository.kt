package net.amazingdomain.octo.gcode

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import java.io.IOException

class MonitorRepository {

    // Scope for launching coroutines related to this repository
    // SupervisorJob ensures that if one child coroutine fails, others are not affected.
    // Dispatchers.IO is appropriate for network operations.
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun getTemperature(): Int { // Existing method


        return 201
    }


    fun sentTextOverTcpAsync(host: String, port: Int, message: String): Job {
        return repositoryScope.launch {
            sendTextOverTcp(host, port, message)
        }
    }

    // TODO convert this into an abstraction that uses one flow to receive commands and another to send them,
    // then stitches them together and has another abstraction where you send a command and has callback.
    // then another level where you have a suspend function to send a command and returns a data class with result
    /**
     * Launches a coroutine to send a text message over TCP.
     *
     * @param host The hostname or IP address of the TCP server.
     * @param port The port number of the TCP server.
     * @param message The text message to send.
     * @return A Job representing the launched coroutine. You can use this to cancel the operation
     *         or join it if you need to wait for its completion from another coroutine.
     */
    suspend fun sendTextOverTcp(host: String, port: Int, message: String): String? {


        println("Connecting to TCP $host:$port sending '$message'")

        val response = mutableListOf<String>()
        var socket: Socket? = null
        try {
            // ActorSelectorManager uses Dispatchers.IO by default if not specified
            val selectorManager = ActorSelectorManager(Dispatchers.IO)
            socket = aSocket(selectorManager).tcp().connect(host, port) {
                // You can configure socket options here if needed
                // e.g., socketTimeout = 10_000
            }

            println("Connected to socket")
            // Get the output channel to write data
            val output = socket.openWriteChannel(autoFlush = true) // autoFlush = true is convenient

            println("Opened socket")
            // Send the message (ensure it's UTF-8 encoded)
            // Adding a newline is common for text-based protocols
            output.writeStringUtf8(message + "\n")
//            output.flush() // Not needed if autoFlush = true

            println("Successfully sent TCP message: '$message' to $host:$port")

            // If you also need to read a response (optional, not requested but common):
            val input = socket.openReadChannel()

            while (!input.isClosedForRead) {
                val r = input.readUTF8Line().toString()
                println("r= $r")
                println("response $response")
                response += r
            }


            if (response != null) {
                println("Received response: $response")
            } else {
                println("No response received or connection closed.")
            }

        } catch (e: IOException) {
            // Handle specific network I/O errors (e.g., connection refused, host not found)
            println("Network I/O error sending TCP message to $host:$port: ${e.message}")
            // e.printStackTrace() // For more detailed debugging
        } catch (e: Exception) {
            // Handle other potential exceptions
            println("Error sending TCP message to $host:$port: ${e.message}: $e")
            e.printStackTrace() // For more detailed debugging
        } finally {
            // Ensure the socket is closed
            try {
                socket?.close()
            } catch (e: Exception) {
                println("Error closing socket: ${e.message}")
            }
        }
        return response
            .joinToString(separator = ":")
    }

    /**
     * Call this method when the MonitorRepository is no longer needed
     * to cancel all ongoing coroutines and release resources.
     * This is important to prevent leaks.
     */
    fun dispose() {
        println("Dispose coroutine")
        repositoryScope.cancel() // Cancels all coroutines launched in this scope
    }
}
