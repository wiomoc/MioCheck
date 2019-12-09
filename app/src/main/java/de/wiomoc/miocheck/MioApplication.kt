package de.wiomoc.miocheck

import android.app.Application
import de.wiomoc.miocheck.services.*
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.dsl.module

class MioApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val appModule = module {
            single { PreferenceService(get()) }
            single { PushMessageService(get()) }
            single { AvailabilityService(get()) }
            single { LockersService(get(), get(), get(), get()) }
            single { LockerInfoService(get(), get(), get(), get()) }
            single { LockerAdminService(get(), get(), get(), get()) }
            single { ConnectionService(get()) }
        }

        startKoin {
            androidLogger()
            androidContext(this@MioApplication)
            modules(listOf(appModule, firebaseModule))
        }
    }
}
