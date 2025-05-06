package net.amazingdomain.octo_flashforge.android

import android.app.Application
import timber.log.Timber

class OctoApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // TODO plant release timber tree
        Timber.plant(Timber.DebugTree())
    }

}