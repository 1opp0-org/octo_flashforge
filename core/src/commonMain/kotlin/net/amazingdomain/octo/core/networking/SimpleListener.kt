package net.amazingdomain.octo.core.networking

open class SimpleListener(private val sink: suspend (String) -> Unit) : Listener {

    override suspend fun write(data: String) {
        sink(data)
    }

    override suspend fun close() {
    }

}