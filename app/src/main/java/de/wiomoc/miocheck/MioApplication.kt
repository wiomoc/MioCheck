package de.wiomoc.miocheck

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.dsl.module

class MioApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val appModule = module {
            single { Preferences(get()) }
            single { NotificationService(get(), get()) }
            single { AvailabilityService() }
            single { LockerService() }
        }

        startKoin {
            androidLogger()
            androidContext(this@MioApplication)
            modules(appModule)
        }
    }
}
