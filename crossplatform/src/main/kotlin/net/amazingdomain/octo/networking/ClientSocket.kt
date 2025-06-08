package net.amazingdomain.octo.networking

import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.readUTF8Line
import io.ktor.utils.io.writeStringUtf8
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import java.io.IOException

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
 * @param disconnectTimeoutMs max time the socket will remain open without write operations
 */
class ClientSocket(
    private val host: String,
    private val port: Int,
    private val bufferSize: Int = 100,
    private val disconnectTimeoutMs: Long = 1000L,
) {


    // TODO is this supposed to come from DI?
    private val logger by lazy { KotlinLogging.logger {} }

    private val mutex = Mutex()

    private val mutableFlow = MutableSharedFlow<String?>(
        extraBufferCapacity = bufferSize, onBufferOverflow = BufferOverflow.SUSPEND
    )

    /**
     * Emits every line received by the TCP connection, suspending when the buffer is full.
     *
     * Buffer size is specified on constructor.
     */
    val sharedFlow: SharedFlow<String?> = mutableFlow

    // TODO refactor this data into an internal class and make it single responsibility
    private var socket: Socket? = null

    private var socketOutput: ByteWriteChannel? = null
    private var readerJob: Job? = null
    private var disconnectWatchdogJob: Job? = null

    /**
     * Asynchronously start a connection, and returns immediately
     */
    private suspend fun connect() {

        withContext(Dispatchers.IO + SupervisorJob()) {
            readerJob = launch {
                val localSocket = setupSocket()
                socket = localSocket
                readData(localSocket)
            }
        }
    }

    /**
     * Keeps listening to [localSocket] and every time a newline is found, emit a [String] to [sharedFlow]
     */
    private fun readData(localSocket: Socket?) {

        if (localSocket?.isActive == true) {
            localSocket.async {

                val socketInput = localSocket.openReadChannel()

                while (!socketInput.isClosedForRead) {
                    socketInput.readUTF8Line().toString().let {
                        mutableFlow.emit(it)
                    }
                }
                logger.debug("Socket is closed naturally")
            }
        } else {
            logger.error("Socket is closed for some unexpected reason!")
        }
    }

    private suspend fun setupSocket(): Socket? {

        return try {

            val selectorManager = ActorSelectorManager(Dispatchers.IO)
            val localSocket = aSocket(selectorManager).tcp().connect(host, port)
            socketOutput =
                localSocket.openWriteChannel(autoFlush = true) // autoFlush = true is convenient

            logger.info { "Socket successful to '$host:$port'  status = ${localSocket.isActive}" }
            localSocket

        } catch (e: IOException) {
            // Handle specific network I/O errors (e.g., connection refused, host not found)
            logger.error { "Socket not successful: Network I/O error sending TCP message to $host:$port: ${e.message}" }
            e.printStackTrace() // For more detailed debugging
            null
        } catch (e: Exception) {
            // Handle other potential exceptions
            logger.error { "Socket not successful: Error sending TCP message to $host:$port: ${e.message}: $e" }
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

        logger.info("Disconnect started")
        mutex
            .withLock {

                socketOutput?.flushAndClose()
                socket?.close()
                readerJob?.cancelAndJoin()
                disconnectWatchdogJob?.cancelAndJoin()

                disconnectWatchdogJob = null
                readerJob = null
                socket = null
            }
        logger.trace("Disconnect finished")
    }

    /**
     * checks if socket is open
     *   opens a socket if necessary
     *   reuses socket if one is available
     *   resets timer for disconnection
     *
     *   thread safe method.
     */
    suspend fun ensureConnection() {
        mutex.withLock {
            if (readerJob == null || socket?.isActive != true) {
                connect()
                disconnectWatchdogJob = startDisconnectWatchdog()
            } else {
                logger.trace("Renew watchdog job")
                disconnectWatchdogJob?.cancelAndJoin()
                disconnectWatchdogJob = startDisconnectWatchdog()
            }
        }
    }

    /**
     * Kicks off asynchronously a watchdog that closes the socket by calling [disconnect] after a period of inactivity.
     *
     * This is useful to save resources when the connection is not actively used.
     *
     * The watchdog will be cancelled if [sendTextOverTcp] is called.

     *
     * @return job to be cancelled if data is written
     */
    private fun startDisconnectWatchdog(): Job {

        return CoroutineScope(Dispatchers.IO).launch {
            delay(timeMillis = disconnectTimeoutMs) // 10 minutes of inactivity
            logger.info { "Disconnect watchdog triggered after $disconnectTimeoutMs ms of inactivity" }
            disconnect()
        }

    }

    /**
     * Send messages to the connection
     */
    // TODO skip connect if it's active, add timer to start disconnect if inactive for like 10 minutes
    suspend fun sendTextOverTcp(message: String) {

        logger.trace { "Send data 1 '$message'" }

        ensureConnection()

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

        logger.trace("Send data 4")
    }
}