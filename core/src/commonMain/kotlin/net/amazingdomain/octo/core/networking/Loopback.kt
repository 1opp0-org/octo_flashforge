package net.amazingdomain.octo.core.networking


import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first

/**
 * Implements a basic [Listener] that simply echoes back any data written to it.
 */
class Loopback(bufferSize: Int) : Listener {
    private val flow = MutableSharedFlow<String>(replay = bufferSize)

    override suspend fun write(data: String) {
        flow.emit(data)
    }

    override suspend fun read(): String {
        return flow.first()
    }

    override suspend fun close() {
        // No-op
    }
}