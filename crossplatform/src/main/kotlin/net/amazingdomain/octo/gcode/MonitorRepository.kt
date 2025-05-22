package net.amazingdomain.octo.gcode

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import mu.KotlinLogging
import java.io.IOException
import java.lang.Thread.sleep

/**
 * Creates a socket that can send and receive data.
 *
 * Listen to received data on [sharedFlow]; note that if you don't consume in a timely fashion and the buffer is full
 * other clients won't be able to receive data at all, suspending all flows. Set buffer size in constructor.
 *
 * Send data using [sendTextOverTcp]
 *
 * @param host The hostname or IP address of the TCP server.
 * @param port The port number of the TCP server.
 * @param bufferSize Size of [sharedFlow] buffer
 */
class MonitorRepository(private val host: String, private val port: Int, private val bufferSize: Int = 100) {

    // TODO is this supposed to come from DI?
    private val logger by lazy { KotlinLogging.logger {} }

    private val mutableFlow = MutableSharedFlow<String?>(
        extraBufferCapacity = bufferSize,
        onBufferOverflow = BufferOverflow.SUSPEND
    )

    /**
     * Emits every line received by the TCP connection, suspending when the buffer is full.
     *
     * Buffer size is specified on constructor, default is 100.
     */
    val sharedFlow: SharedFlow<String?> = mutableFlow

    // TODO refactor this data into an internal class and make it single responsibility
    private var socket: Socket? = null

    //    private var socketInput: ByteReadChannel? = null
    private var socketOutput: ByteWriteChannel? = null
    private var job: Job? = null

    /**
     * Asynchronously start a connection, and returns immediately
     */
    private suspend fun connect() {

        assert(job == null) {
            "Job is active, can't have 2 jobs"
        }

        logger.trace("Connect started")
        withContext(Dispatchers.IO) {
            logger.trace("**1 ")
            job = launch(Dispatchers.IO) {
                logger.trace("**2 ")
                val localSocket = setupSocket()
                socket = localSocket
                logger.trace("**3  isActive ${localSocket?.isActive}")
                launch { readData(localSocket) }
                logger.trace("**4 ")
            }
        }

        logger.info("Connect Established and read in progress")
    }

    private fun readData(localSocket: Socket?) {

        if (localSocket?.isActive == true) {
            localSocket.async {

                val socketInput = localSocket.openReadChannel()

                logger.debug("-- Connection in progress end, starting to read")
                while (!socketInput.isClosedForRead) {
                    val r = socketInput.readUTF8Line().toString()
                    mutableFlow.emit(r)
                }
            }
        } else {
            logger.error("Socket is closed for some reason!")
        }
    }

    private suspend fun setupSocket(): Socket? {

        return try {

            val selectorManager = ActorSelectorManager(Dispatchers.IO)
            val localSocket = aSocket(selectorManager).tcp().connect(host, port)
            socketOutput = localSocket.openWriteChannel(autoFlush = true) // autoFlush = true is convenient

            logger.info { "*0 Opened socket to '$host:$port'  status = ${localSocket.isActive}" }

            localSocket

        } catch (e: IOException) {
            // Handle specific network I/O errors (e.g., connection refused, host not found)
            logger.error { "Network I/O error sending TCP message to $host:$port: ${e.message}" }
            null
            // e.printStackTrace() // For more detailed debugging
        } catch (e: Exception) {
            // Handle other potential exceptions
            logger.error { "Error sending TCP message to $host:$port: ${e.message}: $e" }
            e.printStackTrace() // For more detailed debugging
            null
        }

    }

    /**
     * Call this method when the MonitorRepository is no longer needed
     * to cancel all ongoing coroutines and release resources.
     * This is important to prevent leaks.
     *
     * Also, sockets may linger opened even after the application is killed depending on a number of factors.
     */
    suspend fun disconnect() {

        assert(job != null) {
            "Job is not active, cannot disconnect"
        }

        logger.info("Disconnect started")
//        socketInput?.cancel()
        socketOutput?.flushAndClose()
        socket?.close()
        job?.cancelAndJoin()
        logger.trace("Disconnect finished")
    }


    /**
     * Send messages to the connection
     */
    // TODO skip connect if it's active, add timer to start disconnect if inactive for like 10 minutes
    suspend fun sendTextOverTcp(message: String) {

        logger.trace { "Send data 1 '$message'" }
        connect()
        logger.trace("Send data 2")
        // Send the message (ensure it's UTF-8 encoded)
        // Adding a newline is common for text-based protocols
        assert(socketOutput != null) { "Socket should be initialized now " }
        if (socket?.isActive == true && socketOutput?.isClosedForWrite != null) {
            socketOutput?.writeStringUtf8(message + "\n")
            logger.debug("Send data 3 - write successfully")
        } else {
            logger.error("Cannot write to socket")
        }
        sleep(500) // this is necessary, so we have time for the read coroutine to execute, until a refactor comes
        disconnect()
        logger.trace("Send data 4")
    }
}
