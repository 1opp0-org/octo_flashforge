package net.amazingdomain.octo.core.networking

/**
 * A test implementation of [SourceListener] that emits a predefined list of items from an
 * iterator. When the items are exhausted, it throws a [kotlinx.coroutines.CancellationException] to signal the
 * end of the stream, which is a clean way to terminate the consuming coroutine in the pipeline.
 */
class SimpleSourceListener(data: List<String>) : SourceListener {
    private var isOpen: Boolean = true
    private val iterator =data.iterator()

    override suspend fun read(): String {

        if (iterator.hasNext()) {
            return iterator.next()
        } else {
            close()
            throw SourceListener.IllegalOperationException("Class is closed")
        }
    }

    override fun isOpenForRead(): Boolean {
        return iterator.hasNext()
    }

    override fun start() {
        // No op
    }

    override suspend fun close() {
        isOpen = false
    }

}