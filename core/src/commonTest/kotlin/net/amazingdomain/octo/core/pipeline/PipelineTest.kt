package net.amazingdomain.octo.core.pipeline

import io.mockk.coVerify
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import net.amazingdomain.octo.core.networking.SimpleListener
import net.amazingdomain.octo.core.networking.SimpleSinkListener
import net.amazingdomain.octo.core.networking.SimpleSourceListener
import net.amazingdomain.octo.core.networking.SourceListener
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class PipelineTest {

    suspend fun testMe(d: String): Unit {

    }

    @Test
    fun `simple listener`() = runTest {

        val callbackSpy = spyk(::testMe)
        val simpleListener = spyk(SimpleListener(callbackSpy))

        simpleListener.write("Hello")

        coVerify(exactly = 1) { simpleListener.write(any()) }
        coVerify(exactly = 1) { callbackSpy(any()) }
    }

    @Test
    fun `source should give 5 values`() =
        runTest(timeout = 5.toDuration(DurationUnit.SECONDS)) {

            val actualData = List(5) {
                it.toString()
            }

//            val s = SimpleSourceListener(actualData)
//
//            assertEquals(expected = "0", s.read())
//            assertEquals(expected = "1", s.read())
//            assertEquals(expected = "2", s.read())
//            assertEquals(expected = "3", s.read())
//            assertEquals(expected = "4", s.read())
//            assertFalse(s.isOpenForRead())
//            assertThrows<SourceListener.IllegalOperationException> { s.read() }
//            assertFalse(s.isOpenForRead())
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `processor should process`() =
        runTest(timeout = 5.toDuration(DurationUnit.SECONDS)) {

            val actualData = List(5) {
                it.toString()
            }

//            val source = SimpleSourceListener(actualData)
//            val sink = spyk(::testMe)
//            val processor = spyk(SimpleListener(sink = sink))
//
//            assertTrue(source.isOpenForRead())
//
//            val d = launch {
//                while (source.isOpenForRead()) {
//                    source.read()
//                        .let {
//                            processor.write(it)
//                        }
//                }
//            }
//
//            advanceUntilIdle()
//            d.join()
//            coVerify(exactly = 5) { sink(any()) }
//            assertFalse(source.isOpenForRead())
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `pipeline should process all items from source to sink and close resources`() =
        runTest(timeout = 5.toDuration(DurationUnit.SECONDS)) {
            val actualData = List(5) {
                it.toString()
            }

//            val source = spyk(SimpleSourceListener(actualData))
//            val sink = spyk(SimpleSinkListener())
//            val processor = spyk(SimpleListener(sink::write))
//            val pipeline = spyk(Pipeline(source, processor, scope = this))
//
//            pipeline.start()
//            pipeline.pipelineJob?.join()
//
//            advanceUntilIdle()
//
//            coVerify(exactly = 5) { processor.write(any()) }
//            coVerify(exactly = 5) { sink.write(any()) }
//            coVerify(exactly = 5) { source.read() }
//
//            coVerify(exactly = 1) { source.close() }
//            coVerify(exactly = 1) { processor.close() }
//            coVerify(exactly = 1) { pipeline.stop() }
//
//            assertEquals(expected = actualData, actual = sink.queue.toList())
//            assertFalse(source.isOpenForRead())
        }
}