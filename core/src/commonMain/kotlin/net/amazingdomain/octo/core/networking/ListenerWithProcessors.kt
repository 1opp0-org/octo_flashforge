package net.amazingdomain.octo.core.networking

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import mu.KotlinLogging

/**
 * An implementation of a [BufferedListener] that processes data through a chain of [ListenerProcessor]s.
 *
 * @param processors A list of [ListenerProcessor]s to apply to the data.
 * @param scope The [CoroutineScope] to launch the processing coroutine in. Defaults to a scope on [Dispatchers.Default],
 * suitable for CPU-heavy tasks.
 * @param bufferSize The buffer size for the input and output flows.
 */
open class ListenerWithProcessors(
    private val processors: List<ListenerProcessor>,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    bufferSize: Int = 100,
    sink: suspend (String) -> Unit
) : BufferedListener(scope, bufferSize, sink = sink) {

    interface ListenerProcessor {
        suspend fun process(input: String): String
    }

    private val logger = KotlinLogging.logger {}

    override suspend fun process(data: String) {

        processors.foldIndexed(data) { index, acc, processor ->
            logger.trace { "Before processing with processor #$index: $acc" }
            val result = processor.process(acc)
            logger.trace { "After processing with processor #$index: $result" }
            result
        }
            .let { super.process(data) }
    }
}
