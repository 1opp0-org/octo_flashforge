package net.amazingdomain.octo.gcode

import kotlinx.coroutines.channels.Channel
import net.amazingdomain.octo.testapplication.GCode


/**
 * Creates the behaviour of a 3d printer, with no buffer and blocking calls for writing and reading.
 * You literally have to write one command, and read one command before you can write another one.
 */
open class Virtual3dPrinter() {

    private val channel = Channel<List<String >>(Channel.CONFLATED)

    suspend fun writeGcode(command:String ) {
        val response = produceResponse(command )
        channel.send(response)
    }

    suspend fun readData(): List<String>  {
        return channel.receive()
    }


    private fun GCode.cleanGcode(): String {
        return code.uppercase()
            .replace("~", "")
    }

    /**
     * This method specifies actual behaviour of what a printer responds when it receives a GCode command.
     *
     * For the moment it's stateless.
     */
    open fun produceResponse(command:String ): List<String>  {


        return when (command) {
            GCode.readTemperature.cleanGcode() -> RealResponses.printing.M105
            else -> """
                CMD $command Received.
                ok
            """.trimIndent()
        }
            .split("\n")


    }

    /**
     * This is a compiled list of real responses made by a FlashForge Adventurer 3 3D printer.
     */
    private object RealResponses {

        object Idle {

            val M27 = """
            CMD M27 Received.
            SD printing byte 0/100
            ok
        """.trimIndent()


            val M105 = """
            CMD M105 Received.
            T0:240/0 B:17/0
            ok
        """.trimIndent()

            val M115 = """
            CMD M115 Received.
            Machine Type: FlashForge Adventurer III
            Machine Name: THE printer
            Firmware: v1.3.7
            SN: SNFFAD230566
            X: 150 Y: 150 Z: 150
            Tool Count: 1
            Mac Address:88:A9:A7:90:75:C6
             
            ok
        """.trimIndent()

            val M119 = """
            CMD M119 Received.
            Endstop: X-max:0 Y-max:0 Z-max:0
            MachineStatus: READY
            MoveMode: READY
            Status: S:0 L:0 J:0 F:0
            LED: 1
            CurrentFile: 
            ok
        """.trimIndent()

            val M140 = """
                CMD M140 Received.
                ok
            """.trimIndent()

            val M601 = """
            CMD M601 Received.
            Control Success V2.1.
            ok
        """.trimIndent()

            val M650 = """
            CMD M650 Received.
            X: 1.0 Y: 0.5
            ok
        """.trimIndent()
        }

        object printing {

            val M27 = """
                CMD M27 Received.
                SD printing byte 48/100
                ok
            """.trimIndent()

            val M105 = """
                CMD M105 Received.
                T0:239/240 B:100/100
                ok
            """.trimIndent()

            val M119 = """
                CMD M119 Received.
                Endstop: X-max:0 Y-max:0 Z-max:0
                MachineStatus: BUILDING_FROM_SD
                MoveMode: MOVING
                Status: S:1 L:0 J:0 F:0
                LED: 1
                CurrentFile: among_us_vanilla.gx
                ok
            """.trimIndent()
        }

        // endregion

        // start region G code
        val G91 = """
            CMD G91 Received.
            ok
        """.trimIndent()

    }
}
