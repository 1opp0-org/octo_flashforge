package net.amazingdomain.octo.core.networking

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LoopbackTest {

    @Test
    fun `when data is written then it can be read`() = runTest {
        val listener = Loopback(bufferSize = 1)
        val testData = "Hello, World!"

        listener.write(testData)
        val readData = listener.read()

        assertEquals(testData, readData)
    }
}