package net.amazingdomain.octo.core.networking

import io.mockk.coVerify
import io.mockk.spyk
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@OptIn(ExperimentalCoroutinesApi::class)
class BufferedListenerTest {

    private suspend fun testMe(data: String) {}

    @Test
    fun `coroutine waiting timeout test`() {

        val error = assertFails {

            runTest(timeout = 2.seconds) {

                val callback = spyk(::testMe)

                val listener = BufferedListener(scope = this, sink = callback)
                listener.start() // let's never stop its coroutine and make sure that the test can cancel it when the timeout comes
            }
        }

        // this means that the coroutine inside listener is started and can be cancelled
        assertEquals(actual = error.javaClass.simpleName, expected = "UncompletedCoroutinesError")
    }

    @Test
    fun `close test`() {

        runTest(timeout = 5.seconds) {

            val callback = spyk(::testMe)

            val listener = BufferedListener(scope = this, sink = callback)
            listener.close()
        }
    }

    @Test
    fun `call sink function`() = runTest(timeout = 5.seconds) {

        val callback = spyk(::testMe)
        val listener = spyk(BufferedListener(scope = this, sink = callback))
        listener.start()
        listener.write("line 1")

        advanceUntilIdle()
        coVerify(exactly = 1) { listener.write(any()) }
        coVerify { callback("line 1") }


        listener.close()
    }

    @Test
    fun `when multiple lines are processed, queue them`() {

        runTest(timeout = 5.seconds) {

            val j = this.launch(
                context = this.coroutineContext,
            ) {
                val callback = spyk(::testMe)
                val listener = spyk(BufferedListener(scope = this, sink = callback))
                listener.start()

                listener.write("line 1")
                listener.write("line 2")
                listener.write("line 3")

                coVerify(exactly = 0) { callback(any()) }

                advanceUntilIdle()
                coVerify(exactly = 3) { callback(any()) }
                coVerify { callback("line 1") }
                coVerify { callback("line 2") }
                coVerify { callback("line 3") }

                listener.close()
            }
        }
    }


    @Test
    fun flowTest() {
        runTest(timeout = 5.toDuration(DurationUnit.SECONDS)) {

            val f = MutableSharedFlow<String>()
            var count = 0

            val j = this.launch(
                context = this.coroutineContext,
                start = CoroutineStart.UNDISPATCHED,
            ) {
                f
                    .collect {
                        count++
                    }
            }

            f.emit("1")
            f.emit("1")
            f.emit("1")
            f.emit("1")
            f.emit("1")

            advanceUntilIdle()

            assertEquals(expected = 5, actual = count)

            j.cancel()
        }
    }
}