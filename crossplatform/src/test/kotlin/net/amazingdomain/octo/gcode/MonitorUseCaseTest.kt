package net.amazingdomain.octo.gcode

import io.mockk.mockk
import net.amazingdomain.octo.gcode.MonitorUseCase
import org.junit.Assert.assertEquals
import org.junit.Test


class MonitorUseCaseTest {

    private val useCase = MonitorUseCase(mockk())

    @Test
    fun `Given string When valid Then parse it`() {

        val expected = MonitorUseCase.TemperatureQuery(
            extruderCurrentTemp = 40,
            baseCurrentTemp = 41,
            extruderTargetTemp = 42,
            baseTargetTemp = 43
        )

        val actualString = "T0:40/42 B:41/43"


        val actualResponse = useCase.parseResponse(actualString)

        assertEquals(expected, actualResponse)

    }
}