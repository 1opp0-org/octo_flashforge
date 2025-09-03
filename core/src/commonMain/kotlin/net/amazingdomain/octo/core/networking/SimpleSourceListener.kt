package net.amazingdomain.octo.core.networking

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * A test implementation of [SourceListener] that emits a predefined list of items from an
 * iterator. When the items are exhausted, it throws a [kotlinx.coroutines.CancellationException] to signal the
 * end of the stream, which is a clean way to terminate the consuming coroutine in the pipeline.
 */
class SimpleSourceListener(
    data: List<String>, private val scope: CoroutineScope,
    private val sink: suspend (data: String) -> Unit
) : SourceListener {
    private var isOpen: Boolean = true
    private val iterator = data.iterator()

    override fun isOpen(): Boolean {
        return iterator.hasNext()
    }

    override suspend fun start() {

        scope
            .launch {
                iterator
                    .forEach {
                        sink(it)
                    }
            }
    }

    override suspend fun close() {
        isOpen = false
        scope.cancel()
    }
}
