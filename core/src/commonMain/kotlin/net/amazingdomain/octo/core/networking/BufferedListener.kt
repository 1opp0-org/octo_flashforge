package net.amazingdomain.octo.core.networking

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.Flow
import mu.KotlinLogging
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean

/**
 * An abstract implementation of a [Listener] that processes data in a buffered and decoupled way.
 *
 * Internally,
 * - [write] puts data into a [Flow]
 * - A coroutine on [scope] collects from the flow,
 *   - it processes the data
 *   - then it calls [sink](data)
 *
 * The flow is ready to accumulate buffer data immediately.
 * Call [start] to get the coroutine and processing going.
 * Once [close] is called, the listener stops accepting new data via [write].
 *
 * TODO: currently calling [close] will cancel the coroutine, which may stop data in transit.
 *
 * @param scope The [CoroutineScope] to launch the processing coroutine in. Defaults to a scope on [Dispatchers.Default].
 * @param bufferSize The size of the buffer
 * @param sink the lambda that will
 */
open class BufferedListener(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    bufferSize: Int = 100,
    private val sink: suspend (String) -> Unit
) : SimpleListener(sink = sink) {

    private val logger = KotlinLogging.logger {}

    private var job: Job? = null
    private val isClosed = AtomicBoolean(false)

    /**
     * Implements queue for processing data
     */
    private val flow = MutableSharedFlow<String>(
        extraBufferCapacity = bufferSize,
        onBufferOverflow = BufferOverflow.SUSPEND
    )

    /**
     * Starts coroutine that will consume from internal buffer, then call [process] and [sink].
     */
    open fun start() {

        job = scope.launch(
            start = CoroutineStart.UNDISPATCHED,
        ) {
            flow
                .collect { process(it) }
        }
    }

    /**
     * Writes data to the listener for processing.
     *
     */
    override suspend fun write(data: String) {

        logger.debug { "Writing data to input queue: $data" }

        flow.emit(data)
    }

    /**
     * Closes the listener, preventing new data from being written and signaling the end of the
     * data stream for readers. The underlying processing coroutine is cancelled.
     */
    override suspend fun close() {
        logger.info { "Closing BufferedListener and cancelling processor coroutine." }
        job?.cancel()
        isClosed.set(true)
    }

    /**
     * Processes the incoming data. One call to this method may end up in either 0, 1 or multiple calls to [sink].
     *
     * Classes that override it must call `super.process()`
     *
     * @param data The input data to process.
     */
    open suspend fun process(data: String) {
        logger.trace { "Sending data to sink '$data'" }
        sink(data)
    }
}