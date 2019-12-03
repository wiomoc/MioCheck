package de.wiomoc.miocheck.services

import android.content.Context

class PreferenceService(context: Context) {
    private val preferences = context.getSharedPreferences("mio", Context.MODE_PRIVATE)!!

    val TOPICS_PREF_KEY = "topics"
    val SELECTED_LOCKER_PREF_KEY = "locker"

    var selectedLockerId: String?
        get() = preferences.getString(SELECTED_LOCKER_PREF_KEY, null)
        set(value) = preferences.edit().putString(SELECTED_LOCKER_PREF_KEY, value).apply()

    val subscribedTopics by lazy {
        preferences.getStringSet(TOPICS_PREF_KEY, mutableSetOf())!!
    }

    fun addSubscription(id: String) {
        subscribedTopics.add(id)
        writeTopics()
    }

    fun removeSubscription(id: String) {
        subscribedTopics.remove(id)
        writeTopics()
    }

    fun hasSubscribedToTopic(id: String) = subscribedTopics.contains(id)

    private fun writeTopics() {
        preferences.edit().putStringSet(TOPICS_PREF_KEY, subscribedTopics).apply()
    }
}
