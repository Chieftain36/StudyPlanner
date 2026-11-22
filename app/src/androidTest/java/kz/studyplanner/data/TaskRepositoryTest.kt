package kz.studyplanner.data

import android.content.Context
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kz.studyplanner.data.local.PlannerDatabase
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TaskRepositoryTest {

    private lateinit var database: PlannerDatabase
    private lateinit var repository: TaskRepository

    @Before
    fun setUp() {
        val context = InstrumentationRegistry
            .getInstrumentation()
            .targetContext

        database = Room.inMemoryDatabaseBuilder(
            context,
            PlannerDatabase::class.java
        ).build()

        repository = TaskRepository(database.taskDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun addTaskTrimsFieldsAndGeneratesDifferentIds() = runBlocking {
        val firstId = repository.addTask(
            " Task ",
            " CSS ",
            " 2026-11-30 ",
            " Notes "
        )

        val secondId = repository.addTask(
            "Other",
            "CSS",
            "2026-12-01",
            ""
        )

        val task = repository.tasks.first().single {
            it.id == firstId
        }

        assertNotEquals(firstId, secondId)
        assertEquals("Task", task.title)
        assertEquals("CSS", task.course)
        assertEquals("2026-11-30", task.dueDate)
        assertEquals("Notes", task.notes)
        assertFalse(task.isCompleted)
    }

    @Test
    fun editPreservesCompletionAndDeleteRemovesOnlySelectedTask() =
        runBlocking {
            val id = repository.addTask(
                "Task", "CSS", "2026-11-30", ""
            )

            val otherId = repository.addTask(
                "Other", "CSS", "2026-12-01", ""
            )

            repository.toggleTaskCompletion(id)

            assertTrue(
                repository.updateTask(
                    id,
                    " Changed ",
                    " Math ",
                    "2026-12-02",
                    " New "
                )
            )

            val task = repository.tasks.first().single {
                it.id == id
            }

            assertEquals("Changed", task.title)
            assertEquals("Math", task.course)
            assertEquals("New", task.notes)
            assertTrue(task.isCompleted)

            repository.deleteTask(id)

            assertEquals(
                listOf(otherId),
                repository.tasks.first().map { it.id }
            )

            assertFalse(
                repository.updateTask(
                    id,
                    "Missing",
                    "CSS",
                    "2026-12-02",
                    ""
                )
            )
        }

    @Test
    fun blankFieldsAreRejectedWithoutSaving() = runBlocking {
        val invalidFields = listOf(
            " " to "CSS",
            "Task" to " "
        )

        for ((title, course) in invalidFields) {
            try {
                repository.addTask(
                    title,
                    course,
                    "2026-11-30",
                    ""
                )

                fail("Blank fields must be rejected")
            } catch (expected: IllegalArgumentException) {
                assertTrue(
                    repository.tasks.first().isEmpty()
                )
            }
        }
    }

    @Test
    fun taskSurvivesClosingAndReopeningDatabase() = runBlocking {
        val context: Context = InstrumentationRegistry
            .getInstrumentation()
            .targetContext

        val name = "persistence-test-${System.nanoTime()}.db"

        var diskDatabase: PlannerDatabase? = null

        try {
            val first = Room.databaseBuilder(
                context,
                PlannerDatabase::class.java,
                name
            ).build()

            diskDatabase = first

            val firstRepository = TaskRepository(
                first.taskDao()
            )

            val id = firstRepository.addTask(
                "Persistent",
                "CSS",
                "2026-11-30",
                "Saved"
            )

            firstRepository.toggleTaskCompletion(id)

            first.close()

            val reopened = Room.databaseBuilder(
                context,
                PlannerDatabase::class.java,
                name
            ).build()

            diskDatabase = reopened

            val saved = TaskRepository(
                reopened.taskDao()
            ).tasks.first().single()

            assertEquals(id, saved.id)
            assertEquals("Persistent", saved.title)
            assertEquals("Saved", saved.notes)
            assertTrue(saved.isCompleted)
        } finally {
            diskDatabase?.close()
            context.deleteDatabase(name)
        }
    }
}