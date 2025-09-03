package net.amazingdomain.octo.core.networking

import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.writeStringUtf8
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import mu.KotlinLogging
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.DurationUnit
import kotlin.time.Duration
import kotlin.time.toDuration
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.assertFalse
import java.net.ServerSocket as JavaServerSocket

@OptIn(ExperimentalCoroutinesApi::class)
class SourceListenerSocketTest {
    private val logger = KotlinLogging.logger {}

    private val testTimeout: Duration = 2.toDuration(DurationUnit.SECONDS)
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    private lateinit var testServerScope: CoroutineScope

    private var port: Int = 0
    private lateinit var sourceListenerSocket: SourceListenerSocket
    private val receivedData = ConcurrentLinkedQueue<String>()
    private lateinit var dataReceivedLatch: CountDownLatch

    @BeforeTest
    fun setup() {
        // Find a free port to avoid conflicts during tests
        port = JavaServerSocket(0).use { it.localPort }
        receivedData.clear()
        testServerScope = CoroutineScope(SupervisorJob() + ioDispatcher)
        sourceListenerSocket = SourceListenerSocket(
            port = port,
            // The server must run on a real I/O dispatcher to handle network connections.
            // The scope must also be a "real" scope tied to the test lifecycle.
            scope = testServerScope,
            sink = { data ->
                receivedData.add(data)
                if (::dataReceivedLatch.isInitialized) {
                    dataReceivedLatch.countDown()
                }
            }
        )
    }

    @AfterTest
    fun tearDown() {
        // runTest is needed to properly close the socket which is a suspend fun
        // and to advance the dispatcher to execute the close logic.
        runTest {
            if (sourceListenerSocket.isOpen()) {
                sourceListenerSocket.close()
            }
        }
        testServerScope.cancel()
    }

    @Test
    fun `start and stop without any clients`() =
        runTest(timeout = testTimeout) {

            sourceListenerSocket.start()
            assertTrue(sourceListenerSocket.isOpen())
            sourceListenerSocket.close()
            assertFalse(sourceListenerSocket.isOpen())
        }

    @Test
    fun `port is occupied only while server is running`() =
        runTest(timeout = testTimeout) {
            // Start the server and confirm it's open
            sourceListenerSocket.start()

            assertTrue(sourceListenerSocket.isOpen(), "Server should be open after start()")
            assertFalse(isPortFree(port), "Port should be busy while server is running")

            // Stop the server and confirm it's closed
            sourceListenerSocket.close()
            assertFalse(sourceListenerSocket.isOpen(), "Server should be closed after close()")

            // Wait for the OS to release the port. This avoids flakiness.
            val portWasFreed = awaitPortFree(port, 200.toDuration(DurationUnit.MILLISECONDS))

            assertTrue(portWasFreed, "Port should be free after server is closed")
        }

    @Test
    fun `should receive all data sent by a client`() =
        runTest(timeout = testTimeout) {
            dataReceivedLatch = CountDownLatch(2)

            sourceListenerSocket.start()
            assertTrue(sourceListenerSocket.isOpen())

            val clientJob = launchClient("Hello", "World")

            // Wait for the server to receive the data, with a timeout to prevent hangs
            val receivedInTime = dataReceivedLatch.await(testTimeout.inWholeMilliseconds, TimeUnit.MILLISECONDS)

            assertTrue(receivedInTime, "Test timed out waiting for server to receive data.")
            assertEquals(listOf("Hello", "World"), receivedData.toList())
            clientJob.join()
        }

    /**
     * Verifies the server's ability to handle multiple clients connecting one after another.
     * - Starts the server.
     * - Connects a first client, sends data, and verifies its reception.
     * - Confirms the server remains open after the first client disconnects.
     * - Connects a second client, sends more data, and verifies the cumulative data is correct.
     */
    @Test
    fun `should handle sequential clients`() =
        runTest(timeout = testTimeout) {
            sourceListenerSocket.start()
            assertTrue(sourceListenerSocket.isOpen())

            // --- Client 1 ---
            dataReceivedLatch = CountDownLatch(2)
            val client1Job = launchClient("ClientA-1", "ClientA-2")

            var receivedInTime = dataReceivedLatch.await(testTimeout.inWholeMilliseconds, TimeUnit.MILLISECONDS)
            assertTrue(receivedInTime, "Test timed out waiting for data from client 1.")
            assertEquals(listOf("ClientA-1", "ClientA-2"), receivedData.toList())
            client1Job.join()

            // Server should still be running
            assertTrue(sourceListenerSocket.isOpen(), "Server should remain open after a client disconnects.")

            // --- Client 2 ---
            dataReceivedLatch = CountDownLatch(1)
            val client2Job = launchClient("ClientB-1")

            receivedInTime = dataReceivedLatch.await(testTimeout.inWholeMilliseconds, TimeUnit.MILLISECONDS)
            assertTrue(receivedInTime, "Test timed out waiting for data from client 2.")

            val expectedTotalData = listOf("ClientA-1", "ClientA-2", "ClientB-1")
            assertEquals(expectedTotalData, receivedData.toList())
            client2Job.join()
        }

    /**
     * Launches a temporary TCP client in a new coroutine.
     *
     * This client connects to the test server's port, writes each of the provided [linesToSend]
     * terminated by a newline character, and then closes the connection.
     */
    private fun TestScope.launchClient(vararg linesToSend: String): Job = launch(ioDispatcher) {
        val selectorManager = ActorSelectorManager(ioDispatcher)
        try {
            aSocket(selectorManager).tcp().connect("127.0.0.1", port).use { clientSocket ->
                val writeChannel = clientSocket.openWriteChannel(autoFlush = true)
                linesToSend.forEach { line ->
                    writeChannel.writeStringUtf8("$line\n")
                }
            }
        } catch (e: Exception) {
            // Exceptions in the client can be hard to debug, so we log them.
            e.printStackTrace()
        }
    }

    /**
     * Checks if a given TCP port is available by attempting to bind a server socket to it.
     * @return true if the port is free, false otherwise.
     */
    private fun isPortFree(port: Int): Boolean {
        return runCatching { JavaServerSocket(port).use {} }.isSuccess
    }

    /**
     * Waits for a given TCP port to become free by polling.
     * This is useful in tests to account for OS delays in releasing sockets.
     * @return true if the port becomes free within the [timeout], false otherwise.
     */
    private suspend fun awaitPortFree(port: Int, timeout: Duration): Boolean {
        return withTimeoutOrNull(timeout) {
            while (true) {
                if (isPortFree(port)) return@withTimeoutOrNull true
                delay(50) // Polling interval
            }
            @Suppress("UNREACHABLE_CODE") // Required by compiler for the while(true)
            false
        } ?: false
    }
}
