package pro.fixaverse.app

import android.app.Application
import pro.fixaverse.app.di.initKoin

class FixaverseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin()
    }
}
