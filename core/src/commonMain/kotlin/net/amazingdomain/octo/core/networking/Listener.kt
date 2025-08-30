package net.amazingdomain.octo.core.networking

/**
 * Basic interface that behaves like a black box, where a client writes date to it and
 * reads data back from it after being processed.
 *
 * It's basic contract is:
 * - all data is delimited by newline '\n' character
 * - its functions are asynchronous and non-blocking through use of coroutines
 */
interface Listener {
    /**
     * Writes a line of data. The implementation should handle appending the newline character.
     * @param data The string data to write, without a newline.
     */
    suspend fun write(data: String)

    /**
     * Reads a line of data. The implementation should read until a newline character is found
     * and return the string without it.
     * @return The data read from the listener.
     */
    suspend fun read(): String

    /** Closes the listener and releases any underlying resources. */
    suspend fun close()
}
