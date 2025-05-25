package net.amazingdomain.octo.testapplication

import net.amazingdomain.octo.networking.VirtualIp3dPrinter

fun main() {
    val virtualIp3dPrinter = VirtualIp3dPrinter(port = 9999)

    virtualIp3dPrinter.turnOn()


    Thread.sleep(200000L) // keep the application running for a while to test the socket

    virtualIp3dPrinter.turnOff()
}