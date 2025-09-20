package net.amazingdomain.octo_flashforge.android

import android.app.Application
import net.amazingdomain.octo_flashforge.android.log.ReleaseTree
import timber.log.Timber

class OctoApplication : Application() {

    override fun onCreate() {
        super.onCreate()
//        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
//        } else {
//            Timber.plant(ReleaseTree())
//        }
    }

}