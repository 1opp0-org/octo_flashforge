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
    fun start()

    /**
     * Reads a line of data from the source.
     * This method will suspend until data is available.
     *
     * @return The data read from the source.
     *
     * @throws IllegalOperationException if read is performed when
     */
    @Throws(IllegalOperationException::class)
    suspend fun read(): String

    /** Closes the source and releases any underlying resources. */
    suspend fun close()
    fun isOpenForRead(): Boolean

    class IllegalOperationException(message: String) :
        IllegalStateException(message)
}