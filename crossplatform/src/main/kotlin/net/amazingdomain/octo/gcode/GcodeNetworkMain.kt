package net.amazingdomain.octo.gcode

fun main() {

    println("Hello network world")

    val repository = MonitorRepository()


    repository
        .sentTextOverTcpAsync("127.0.0.1", 8899, GCode.readTemperature.code)
        .let {

            println("Answer is '$it' | ")
        }


    Thread.sleep(1000)

    repository.dispose()
    GCode("M105")
}

data class GCode(val code: String) {

    companion object {
        val readTemperature = GCode("~M105")
    }


}