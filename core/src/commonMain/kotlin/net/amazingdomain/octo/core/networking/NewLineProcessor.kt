package net.amazingdomain.octo.core.networking

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging

/**
 * A stateful [ListenerProcessor] that processes streams of data and extracts newline-terminated lines.
 *
 * This processor buffers incoming data until it finds a newline character ('\n').
 * When one or more complete lines are buffered, the `process` method returns them,
 * joined by a single space. Any partial line at the end of the input is buffered for the next call.
 *
 * The output of this processor never contains newline characters.
 *
 * This class is thread-safe.
 */
class NewLineProcessor : ListenerProcessor {

    private val logger = KotlinLogging.logger {}
    private val mutex = Mutex()
    private var buffer = ""

    override suspend fun process(input: String): String {
        return mutex.withLock {
            buffer += input
            val lines = mutableListOf<String>()

            while (buffer.contains('\n')) {
                val parts = buffer.split('\n', limit = 2)
                val line = parts[0]
                buffer = parts.getOrNull(1) ?: ""
                lines.add(line)
            }

            if (lines.isNotEmpty()) {
                val result = lines.joinToString(" ")
                logger.trace { "Emitting ${lines.size} lines: '$result'" }
                return@withLock result
            }

            logger.trace { "Buffering partial input. Buffer size: ${buffer.length}" }
            return@withLock ""
        }
    }
}