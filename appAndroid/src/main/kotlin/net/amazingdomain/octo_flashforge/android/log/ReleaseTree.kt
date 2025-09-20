package net.amazingdomain.octo_flashforge.android.log

import android.util.Log
import timber.log.Timber

/**
 * A Timber tree for release builds.
 *
 * It filters out DEBUG, VERBOSE, and INFO logs.
 * It can be configured to send WARNING and ERROR logs and exceptions to a crash reporting service.
 */
class ReleaseTree : Timber.Tree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO) {
            return
        }

        // TODO: Send logs and exceptions to your crash reporting service
        // For example, using Firebase Crashlytics:
        // val crashlytics = FirebaseCrashlytics.getInstance()
        // crashlytics.log(message)
        // t?.let { crashlytics.recordException(it) }
    }
}