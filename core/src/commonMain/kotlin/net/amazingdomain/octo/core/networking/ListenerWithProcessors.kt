package net.amazingdomain.octo.core.networking

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import java.util.concurrent.LinkedBlockingQueue

/**
 * An abstract implementation of a [Listener] that processes data through a chain of [ListenerProcessor]s.
 * This class provides a buffered and decoupled way to handle data processing.
 *
 * @param processors A list of [ListenerProcessor]s to apply to the data.
 * @param scope The [CoroutineScope] to launch the processing coroutine in. Defaults to a scope on [Dispatchers.Default],
 * suitable for CPU-heavy tasks.
 * @param bufferSize The buffer size for the input and output flows.
 */
open class ListenerWithProcessors(
    private val processors: List<ListenerProcessor>,
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    bufferSize: Int = 100
) : Listener {

    private val logger = KotlinLogging.logger {}
    private val job: Job

    /**
     * Flow for incoming data from the [write] method.
     */
    private val inputFlow = MutableSharedFlow<String>(replay = bufferSize)

    /**
     * Queue for outgoing, processed data to be consumed by the [read] method.
     */
    private val outputQueue = LinkedBlockingQueue<String>(bufferSize)

    init {
        logger.info { "Initializing AbstractListenerProcessor and starting processor coroutine." }
        job = scope.launch {
            inputFlow.collect { data ->
                val processedData = process(data)
                outputQueue.put(processedData)
            }
        }
    }

    override suspend fun write(data: String) {
        logger.debug { "Writing data to input flow: $data" }
        inputFlow.emit(data)
    }

    override suspend fun read(): String {
        val data = withContext(Dispatchers.IO) {
            outputQueue.take()
        }
        logger.debug { "Reading data from output queue: $data" }
        return data
    }

    override suspend fun close() {
        logger.info { "Closing AbstractListenerProcessor and cancelling processor coroutine." }
        job.cancel()
    }

    open suspend fun process(data: String): String {
        return processors.foldIndexed(data) { index, acc, processor ->
            logger.trace { "Before processing with processor #$index: $acc" }
            val result = processor.process(acc)
            logger.trace { "After processing with processor #$index: $result" }
            result
        }
    }
}
