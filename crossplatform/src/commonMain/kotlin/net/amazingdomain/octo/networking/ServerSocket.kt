package net.amazingdomain.octo.networking

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.network.sockets.ServerSocket
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/**
 * It's the abstraction opposite of [ClientSocket].
 *
 * This class opens a listening socket on localhost port [port],
 * and anything received there will be sent to the hot [sharedFlow].
 *
 * The meaning of hot is that it will send data whether someone is receiving data or not. See [bufferSize], which allows some leniency if the code receiving data takes a bit too long to process it and ask for more.
 *
 * Messages are not replayed, so if you have code that sends and receives data, start listening early.
 *
 */
open class ServerSocket(private val port: Int, private val bufferSize: Int = 100) {


    private val logger = mu.KotlinLogging.logger {}

    private var _socketBuilder: ServerSocket? = null
    private var writeChannel: ByteWriteChannel? = null

    private val mutableSharedFlow = MutableSharedFlow<String>(
        extraBufferCapacity = bufferSize,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val sharedFlow: SharedFlow<String> = mutableSharedFlow


    /**
     * Starts listening on the specified port and emits received messages to the shared flow.
     * Returns only once the connection is established.
     */
    open suspend fun connect() {

        logger.debug("Starting to listen on port $port with buffer size $bufferSize")
        openSocket()
        logger.info("Connection established:: listening on port $port")
    }

    open suspend fun disconnect() {
        logger.info("Disconnecting from listening port $port")
    }

    /**
     * Opens a socket to listen for incoming connections. Synchronous function.
     */
    private suspend fun openSocket() {
        logger.debug("Opening socket on port $port")
        // Implementation to open a socket and start listening for incoming connections.
        // This is where you would set up the server socket and start accepting connections.
        // For example, using Java's ServerSocket or Kotlin's Ktor server.
        // KTor server code below:

        val socketBuilder = aSocket(selector = ActorSelectorManager(Dispatchers.IO))
            .tcp()
            .bind(port = port)

        _socketBuilder = socketBuilder

        while (!socketBuilder.isClosed) {
            logger.debug { "Socket accepting connection on port $port" }
            val socket = socketBuilder
                .accept()

            readData(socket)

            writeChannel = socket.openWriteChannel(autoFlush = true)

            logger.debug { "Socket connection accepted on port $port" }
        }
    }

    suspend fun reply(payload: String) {

        writeChannel
            ?.writeStringUtf8(payload)
    }

    private suspend fun readData(socket: Socket) {

        logger.info("I am a server and a new client connected to me on port ${socket.localAddress} / ${socket.remoteAddress}")
        val readChannel = socket.openReadChannel()

        while (!readChannel.isClosedForRead) {
            val line = readChannel.readUTF8Line()

            logger.debug { "Received data: $line" }
            if (line != null) {
                recvData.incrementData(lineCount = 1, bytesCount = line.length.toLong())
                mutableSharedFlow.emit(line)
            } else {
                recvData.incrementData(lineCount = 0, bytesCount = 0) // at least, the timestamp gets updated
            }

        }

    }

    /**
     * Closes the socket.
     */
    private fun closeSocket() {
// TODO

    }

    // region stats
    val sentData = Statistics()
    val recvData = Statistics()

    fun getSentStatistics(): Statistics.StatData = sentData.statData
    fun getReceivedStatistics(): Statistics.StatData = recvData.statData
    // endregion

}

class Statistics {

    data class StatData(
        val lineCount: Long,
        val bytesCount: Long,
        val timestampLastData: Long
    )

    var statData = StatData(0, 0, 0)
        private set


    fun incrementData(lineCount: Long, bytesCount: Long) {
        // Increment the statistics with the provided line and byte counts.

        statData = statData.copy(
            lineCount = statData.lineCount + lineCount,
            bytesCount = statData.bytesCount + bytesCount,
            timestampLastData = System.currentTimeMillis()
        )
    }
}