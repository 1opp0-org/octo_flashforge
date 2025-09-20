package net.amazingdomain.octo.gcode

data class GCode(val code: String) {

    companion object {
        val readTemperature = GCode("~M105")
    }


}