package net.amazingdomain.octo.core.networking

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class NewLineProcessorTest {

    @Test
    fun `given a single complete line, when processing, then it should emit the line`() = runTest {
        val processor = NewLineProcessor()
        val result = processor.process("hello\n")
        assertEquals(expected = "hello", actual = result)
    }

    @Test
    fun `given multiple complete lines, when processing, then it should emit all lines joined by spaces`() = runTest {
        val processor = NewLineProcessor()
        val result = processor.process("hello\nworld\n")
        assertEquals(expected = "hello world", actual = result)
    }

    @Test
    fun `given a partial line, when processing, then it should buffer it and emit nothing`() = runTest {
        val processor = NewLineProcessor()
        val result = processor.process("hello")
        assertEquals(expected = "", actual = result)
    }

    @Test
    fun `given a partial line then the rest, when processing, then it should emit the complete line`() = runTest {
        val processor = NewLineProcessor()
        var result = processor.process("hello")
        assertEquals(expected = "", actual = result)
        result = processor.process(" world\n")
        assertEquals(expected = "hello world", actual = result)
    }

    @Test
    fun `given empty input, when processing, then it should emit nothing`() = runTest {
        val processor = NewLineProcessor()
        val result = processor.process("")
        assertEquals(expected = "", actual = result)
    }

    @Test
    fun `given only a newline, when processing, then it should emit an empty line`() = runTest {
        val processor = NewLineProcessor()
        val result = processor.process("\n")
        assertEquals(expected = "", actual = result)
    }

    @Test
    fun `given multiple inputs with buffering, when processing, then it should emit lines as they complete`() = runTest {
        val processor = NewLineProcessor()

        var result = processor.process("line 1\npart")
        assertEquals(expected = "line 1", actual = result)

        result = processor.process("ial line 2\nand another partial")
        assertEquals(expected = "partial line 2", actual = result)

        result = processor.process("\n")
        assertEquals(expected = "and another partial", actual = result)
    }

    @Test
    fun `given multiple newlines in a row, when processing, then it should treat them as empty lines`() = runTest {
        val processor = NewLineProcessor()
        val result = processor.process("a\n\nb\n")
        assertEquals(expected = "a  b", actual = result)
    }
}