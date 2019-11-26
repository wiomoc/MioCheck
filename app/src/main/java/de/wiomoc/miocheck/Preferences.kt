package de.wiomoc.miocheck

import android.content.Context

class Preferences(context: Context) {
    val preferences = context.getSharedPreferences("mio", Context.MODE_PRIVATE)

    val TOPICS_PREF_KEY = "topics"

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
