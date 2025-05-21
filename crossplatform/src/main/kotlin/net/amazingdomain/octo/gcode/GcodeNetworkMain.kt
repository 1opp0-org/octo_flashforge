package net.amazingdomain.octo.gcode

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

fun main() {

    println("Hello network world")

    val repository = MonitorRepository("127.0.0.1", 8899)

    val monitorUseCase = MonitorUseCase(repository)

    val mainScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Scope for launching coroutines related to this repository
    // SupervisorJob ensures that if one child coroutine fails, others are not affected.
    // Dispatchers.IO is appropriate for network operations.
    val job = mainScope.launch {
        monitorUseCase
            .getExtruderTemperature()
            .let {
                println("*6 Answer is '$it' C | ")
            }
    }

    // TODO wait for job to complete
    Thread.sleep(1000)

}

// TODO refactor in its own file
data class GCode(val code: String) {

    companion object {
        val readTemperature = GCode("~M105")
    }


}