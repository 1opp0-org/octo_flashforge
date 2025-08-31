package net.amazingdomain.octo.core.networking

import org.jetbrains.annotations.TestOnly
import java.util.concurrent.LinkedBlockingQueue

class SimpleSinkListener : SinkListener {

    @TestOnly
    val queue = LinkedBlockingQueue<String>()
    private var _isOpenForWrite = true

    override suspend fun write(data: String) {
        queue.put(data)
    }

    override suspend fun close() {
        _isOpenForWrite = false
    }

    override fun isOpenForWrite(): Boolean = _isOpenForWrite

}