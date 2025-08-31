package net.amazingdomain.octo.core.networking

import io.mockk.coVerify
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class ListenerWithProcessorsTest {

    private lateinit var listener: ListenerWithProcessors

    @AfterTest
    fun tearDown() = runTest {
        if (::listener.isInitialized) {
            listener.close()
        }
    }

    private suspend fun testMe(data: String) {}

    @Test
    fun `with no processors, listener should act as a pass-through buffer`() =
        runTest(timeout = 5.seconds) {
            val callback = spyk(::testMe)
            listener = ListenerWithProcessors(processors = emptyList(), sink = callback)
            listener.start()

            listener.write("line 1")
            listener.write("line 2")

            coVerify(exactly = 2) { callback(any()) }
            coVerify { callback("line 1") }
            coVerify { callback("line 2") }

        }

    @Test
    fun `when multiple lines are processed, counter processor should accumulate counts`() =
        runTest(timeout = 5.seconds) {
            val counterProcessor = spyk(CounterProcessor())
            val sink = mutableListOf<String>()
            listener = ListenerWithProcessors(
                processors = listOf(counterProcessor),
                scope = this,
                sink = { sink.add(it) }
            )
            listener.start()

            val testData1 = "first line"
            listener.write(testData1)

            advanceUntilIdle()
            coVerify { counterProcessor.process("first line") }
            assertEquals(expected = 1L to 2L, actual = counterProcessor.getCounts())
            assertEquals(expected = 1, sink.size)

            val testData2 = "second line with more words"
            listener.write(testData2)

            advanceUntilIdle()
            listener.close()
            assertEquals(
                expected = 2L to 7L, // 1+1 lines, 2+5 words
                actual = counterProcessor.getCounts()
            )
            coVerify { counterProcessor.process("second line with more words") }
            assertEquals(expected = 2, sink.size)
        }

}