package net.amazingdomain.octo.core.networking

/**
 * Represents a sink for data, accepting a stream of strings.
 * It's a write-only view of a data stream.
 */
@Deprecated("Its interface is the same as Listener")
interface SinkListener {
    /**
     * Writes a line of data to the sink. The implementation should handle
     * any necessary formatting, like appending a newline character if required by the
     * underlying protocol.
     *
     * @param data The string data to write.
     */
    suspend fun write(data: String)

    /** Closes the sink and releases any underlying resources. */
    suspend fun close()

    fun isOpenForWrite(): Boolean


}