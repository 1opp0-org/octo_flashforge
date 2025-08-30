package net.amazingdomain.octo.core.networking

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@OptIn(ExperimentalCoroutinesApi::class)
class ListenerWithProcessorsTest {

    private lateinit var listener: ListenerWithProcessors
    private lateinit var counterProcessor: CounterProcessor

    @AfterTest
    fun tearDown() = runTest {
        if (::listener.isInitialized) {
            listener.close()
        }
    }

    @Test
    fun `when data is processed, counter processor should count correctly`() =
        runTest(timeout = 5.toDuration(DurationUnit.SECONDS)) {
            counterProcessor = CounterProcessor()
            listener = ListenerWithProcessors(processors = listOf(counterProcessor))

            val testData = "hello world\nthis is a test"
            listener.write(testData)

            val readData = listener.read()
            assertEquals(expected = testData, actual = readData)

            val counts = counterProcessor.getCounts()
            assertEquals(
                expected = 2L to 6L,
                actual = counts,
                message = "The line and word counts should match the input data"
            )
        }

    @Test
    fun `when multiple data chunks are processed, counter processor should accumulate counts`() =
        runTest(timeout = 5.toDuration(DurationUnit.SECONDS)) {
            counterProcessor = CounterProcessor()
            listener = ListenerWithProcessors(processors = listOf(counterProcessor))

            val testData1 = "first line"
            listener.write(testData1)
            val readData1 = listener.read()
            assertEquals(expected = testData1, actual = readData1)
            assertEquals(expected = 1L to 2L, actual = counterProcessor.getCounts())

            val testData2 = "second line with more words\n"
            listener.write(testData2)
            val readData2 = listener.read()
            assertEquals(expected = testData2, actual = readData2)
            assertEquals(expected = 3L to 7L, actual = counterProcessor.getCounts())
        }
}