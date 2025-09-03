package net.amazingdomain.octo.core.networking

/**
 * Represents a source of data, providing a stream of strings.
 * It's a read-only view of a data stream.
 */
interface SourceListener {
    /**
     * Starts the listener. For some implementations, this might be a no-op.
     * For others, it might start a server or connect to a remote endpoint.
     * This function should be non-blocking.
     */
    suspend fun start()

    /** Closes the source and releases any underlying resources. */
    suspend fun close()
    fun isOpen(): Boolean

}