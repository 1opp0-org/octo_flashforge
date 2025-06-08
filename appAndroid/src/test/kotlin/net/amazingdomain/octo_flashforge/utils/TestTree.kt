package net.amazingdomain.octo_flashforge.utils

import timber.log.Timber

// TODO move into proper package
class TestTree: Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        println("$tag: $message")
    }

}
