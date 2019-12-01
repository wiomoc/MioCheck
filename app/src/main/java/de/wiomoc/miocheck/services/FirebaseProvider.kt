package de.wiomoc.miocheck.services

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.functions.FirebaseFunctions
import org.koin.dsl.module

val firebaseModule = module {
    single { FirebaseDatabase.getInstance().apply { setPersistenceEnabled(true) }.reference }
    single { FirebaseFunctions.getInstance("europe-west1") }
}
