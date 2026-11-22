package kz.studyplanner.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private val Context.studyPreferencesDataStore by preferencesDataStore(
    name = "study_preferences"
)

class StudyPreferencesRepository(
    context: Context
) {
    private val dataStore =
        context.applicationContext.studyPreferencesDataStore

    private companion object {
        val SELECTED_TOPIC =
            stringPreferencesKey("selected_topic")
    }

    val selectedTopic: Flow<StudyTopic> = dataStore.data
        .map { preferences ->
            val savedName = preferences[SELECTED_TOPIC]

            StudyTopic.entries.find {
                it.name == savedName
            } ?: StudyTopic.MOBILE
        }
        .distinctUntilChanged()

    suspend fun saveTopic(topic: StudyTopic) {
        dataStore.edit { preferences ->
            preferences[SELECTED_TOPIC] = topic.name
        }
    }
}