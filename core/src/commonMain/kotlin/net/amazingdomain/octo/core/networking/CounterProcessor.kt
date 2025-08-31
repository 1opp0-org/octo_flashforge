package net.amazingdomain.octo.core.networking

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging

/**
 * A stateful [ListenerWithProcessors.ListenerProcessor] that counts the cumulative number of lines and words from all processed inputs.
 * The counts can be retrieved via the [getCounts] method.
 * The [process] function returns the input string unmodified.
 * The counts are logged at the trace level.
 * This class is thread-safe.
 */
class CounterProcessor : ListenerWithProcessors.ListenerProcessor {

    private val logger = KotlinLogging.logger {}
    private val mutex = Mutex()

    private var totalLines = 0L
    private var totalWords = 0L

    override suspend fun process(input: String): String {
        val linesInBatch = if (input.isEmpty()) 0 else input.lines().size
        val wordsInBatch = input.split(Regex("\\s+")).filter { it.isNotBlank() }.size

        if (linesInBatch > 0 || wordsInBatch > 0) {
            mutex.withLock {
                totalLines += linesInBatch
                totalWords += wordsInBatch
            }
        }

        logger.trace { "Processing '$input'" }
        logger.trace { "Processed input with $linesInBatch lines and $wordsInBatch words. New totals: Lines=$totalLines, Words=$totalWords" }

        return input
    }

    /**
     * Returns a snapshot of the current line and word counts.
     * @return A [Pair] where [Pair.first] is the total lines and [Pair.second] is the total words.
     */
    suspend fun getCounts(): Pair<Long, Long> = mutex.withLock {
        totalLines to totalWords
    }
}