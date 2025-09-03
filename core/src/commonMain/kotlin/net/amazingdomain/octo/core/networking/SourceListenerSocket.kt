package net.amazingdomain.octo.core.networking

import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.ServerSocket
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.isClosed
import io.ktor.network.sockets.openReadChannel
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import mu.KotlinLogging

/**
 * An implementation of [SourceListener] that listens on a TCP socket for incoming data.
 * It can handle multiple client connections concurrently,
 * funneling all incoming data into calls to [sink].
 *
 *
 * @param port The port to listen on.
 * @param dispatcher
 * @param scope The [kotlinx.coroutines.CoroutineScope] to launch background jobs in.
 * @param sink
 */
class SourceListenerSocket(
    private val port: Int,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    scope: CoroutineScope = CoroutineScope(dispatcher),
    private val sink: suspend (data: String) -> Unit,
) : SourceListener {

    private val logger = KotlinLogging.logger {}

    private val selectorManager = ActorSelectorManager(dispatcher)

    private var serverSocket: ServerSocket? = null

    private val scope = CoroutineScope(scope.coroutineContext + SupervisorJob())

    private var listeningJob: Job? = null

    /**
     * Starts listening for incoming connections and data.
     * When it returns the server is either successfully listening for data, or already thrown an exception
     */
    override suspend fun start() {

        logger.info { "Starting server socket on port $port" }
        try {
            val ss = aSocket(selectorManager).tcp().bind(port = port)
            serverSocket = ss
            logger.info { "Server listening on ${serverSocket?.localAddress}" }

            // connections will happen asynchronously
            listeningJob = scope.launch {
                listenForConnections(ss)
            }

        } catch (e: Exception) {
            logger.error(e) { "Error in server socket on port $port" }
            throw e
        }
    }

    private suspend fun CoroutineScope.listenForConnections(serverSocket: ServerSocket) {
        while (isActive && !serverSocket.isClosed) {
            val clientSocket = serverSocket.accept()
            logger.info { "Accepted connection from ${clientSocket.remoteAddress}" }
            handleClient(clientSocket)
        }
    }

    /**
     * Reads one line at a time, and sends it for processing outside this class by calling [sink].
     */
    private fun CoroutineScope.handleClient(clientSocket: Socket) = launch {
        val readChannel = clientSocket.openReadChannel()
        logger.trace { "open socket channel"}
        try {
            while (isActive && !readChannel.isClosedForRead) {
                logger.trace { "waiting for line"}
                val line = readChannel.readUTF8Line() ?: break // Client disconnected
                logger.trace { "got a line      : $line"}
                sink(line)
            }
        } catch (e: Exception) {
            logger.warn(e) { "Error reading from client ${clientSocket.remoteAddress}" }
        } finally {
            clientSocket.close()
            logger.info { "Client ${clientSocket.remoteAddress} disconnected and socket closed." }
        }
    }

    override fun isOpen(): Boolean {
        return !(serverSocket?.isClosed ?: true)
    }

    override suspend fun close() {
        logger.info { "Closing server socket on port $port" }
        // 1. Cancel all coroutines to gracefully stop listening and handling clients.
        // This will cause `accept()` to throw a CancellationException.
        scope.cancel()
        // 2. Wait for the main listening job to complete its cancellation.
        listeningJob?.join()
        // 3. Now that all jobs are stopped, close the underlying resources.
        serverSocket?.close()
        selectorManager.close()
    }
}