package pro.masterdoc.app

import android.app.Application
import pro.masterdoc.app.di.initKoin

class MasterdocApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin()
    }
}
