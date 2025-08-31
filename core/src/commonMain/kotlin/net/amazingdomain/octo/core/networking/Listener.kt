package net.amazingdomain.octo.core.networking

/**
 * Basic interface that behaves like a black box, where a client writes date to it and
 * reads data back from it after being processed.
 *
 */
interface Listener {
    /**
     * Writes a piece of data to the listener.
     * @param data The string data to write.
     */
    suspend fun write(data: String)

    /**
     *  Closes the listener and releases any underlying resources.
     *  */
    suspend fun close()
}
