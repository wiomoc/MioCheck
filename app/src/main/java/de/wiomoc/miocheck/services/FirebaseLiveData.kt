package de.wiomoc.miocheck.services

import androidx.lifecycle.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

inline fun <T> Query.toLiveData(noinline transformer: (DataSnapshot) -> T) = FirebaseLiveData(this, transformer)

class FirebaseLiveData<T>(private val query: Query, transformer: (DataSnapshot) -> T) : LiveData<T>() {
    private val valueEventListener: FirebaseLiveDataEventListener

    init {
        valueEventListener = FirebaseLiveDataEventListener(transformer)
    }

    override fun onActive() {
        query.addValueEventListener(valueEventListener)
    }

    override fun onInactive() {
        query.removeEventListener(valueEventListener)
    }

    inner class FirebaseLiveDataEventListener(private val transformer: (DataSnapshot) -> T) : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {}

        override fun onDataChange(snapshot: DataSnapshot) {
            postValue(transformer(snapshot))
        }
    }
}

