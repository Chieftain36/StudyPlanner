package kz.studyplanner.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StudyPreferencesRepositoryTest {

    @Test
    fun savedTopicCanBeReadByNewRepository() = runBlocking {
        val context = InstrumentationRegistry
            .getInstrumentation()
            .targetContext

        val repository = StudyPreferencesRepository(context)

        val originalTopic = repository.selectedTopic.first()

        try {
            for (topic in StudyTopic.entries) {
                repository.saveTopic(topic)

                val recreatedRepository =
                    StudyPreferencesRepository(context)

                assertEquals(
                    topic,
                    recreatedRepository.selectedTopic.first()
                )
            }
        } finally {
            repository.saveTopic(originalTopic)
        }
    }

    @Test
    fun flowNotifiesObserverWhenTopicChanges() = runBlocking {
        val context = InstrumentationRegistry
            .getInstrumentation()
            .targetContext

        val repository = StudyPreferencesRepository(context)

        val originalTopic = repository.selectedTopic.first()

        try {
            repository.saveTopic(StudyTopic.MOBILE)

            withTimeout(5000) {
                val update = async(
                    start = CoroutineStart.UNDISPATCHED
                ) {
                    repository.selectedTopic.first {
                        it == StudyTopic.CLOUD
                    }
                }

                repository.saveTopic(StudyTopic.CLOUD)

                assertEquals(
                    StudyTopic.CLOUD,
                    update.await()
                )
            }
        } finally {
            repository.saveTopic(originalTopic)
        }
    }
}