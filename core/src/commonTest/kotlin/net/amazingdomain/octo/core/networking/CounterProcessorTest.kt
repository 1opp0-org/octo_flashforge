package net.amazingdomain.octo.core.networking

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CounterProcessorTest {

    @Test
    fun `given a new processor, when processing an empty string, then totals are zero`() = runTest {
        val processor = CounterProcessor()
        val input = ""

        val result = processor.process(input)

        assertEquals(expected = input, actual = result)
        assertEquals(expected = 0L to 0L, actual = processor.getCounts())
    }

    @Test
    fun `given a new processor, when processing a single input, then totals match the input`() = runTest {
        val processor = CounterProcessor()
        val input = "Hello world\nThis is a test"

        val result = processor.process(input)

        assertEquals(expected = input, actual = result)
        assertEquals(expected = 2L to 6L, actual = processor.getCounts())
    }

    @Test
    fun `given a processor, when processing multiple inputs, then totals are accumulated`() = runTest {
        val processor = CounterProcessor()

        val input1 = "Hello world"
        val result1 = processor.process(input1)
        assertEquals(expected = input1, actual = result1)
        assertEquals(expected = 1L to 2L, actual = processor.getCounts())

        val input2 = "This is a test"
        val result2 = processor.process(input2)
        assertEquals(expected = input2, actual = result2)
        assertEquals(expected = 2L to 6L, actual = processor.getCounts())

        val input3 = ""
        val result3 = processor.process(input3)
        assertEquals(expected = input3, actual = result3)
        assertEquals(expected = 2L to 6L, actual = processor.getCounts())

        val input4 = "another line"
        val result4 = processor.process(input4)
        assertEquals(expected = input4, actual = result4)
        assertEquals(expected = 3L to 8L, actual = processor.getCounts())
    }
}