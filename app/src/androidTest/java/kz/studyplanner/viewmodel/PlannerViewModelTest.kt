package kz.studyplanner.viewmodel

import androidx.lifecycle.ViewModelStore
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kz.studyplanner.data.TaskRepository
import kz.studyplanner.data.local.PlannerDatabase
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlannerViewModelTest {

    private lateinit var database: PlannerDatabase
    private lateinit var repository: TaskRepository
    private lateinit var viewModel: PlannerViewModel

    private val store = ViewModelStore()

    @Before
    fun setUp() {
        val instrumentation =
            InstrumentationRegistry.getInstrumentation()

        database = Room.inMemoryDatabaseBuilder(
            instrumentation.targetContext,
            PlannerDatabase::class.java
        ).build()

        repository = TaskRepository(database.taskDao())

        instrumentation.runOnMainSync {
            viewModel = PlannerViewModel(repository)
            store.put("planner", viewModel)
        }
    }

    @After
    fun tearDown() {
        InstrumentationRegistry
            .getInstrumentation()
            .runOnMainSync {
                store.clear()
            }

        database.close()
    }

    @Test
    fun validationRejectsBlankFieldsAndInvalidDates() {
        assertNotNull(
            viewModel.validateTask(" ", "CSS", "2026-11-30")
        )

        assertNotNull(
            viewModel.validateTask("Task", " ", "2026-11-30")
        )

        assertNotNull(
            viewModel.validateTask("Task", "CSS", "2026-02-30")
        )

        assertNotNull(
            viewModel.validateTask("Task", "CSS", "30-11-2026")
        )

        assertNull(
            viewModel.validateTask("Task", "CSS", "2028-02-29")
        )
    }

    @Test
    fun invalidAddDoesNotSaveTask() = runBlocking {
        try {
            viewModel.addTask(
                "Task",
                "CSS",
                "wrong",
                ""
            )

            fail("Invalid date must be rejected")
        } catch (expected: IllegalArgumentException) {
            assertTrue(
                repository.tasks.first().isEmpty()
            )
        }
    }

    @Test
    fun changesReachStateFlowAndDeletedTaskCannotBeEdited() =
        runBlocking {
            val id = viewModel.addTask(
                "Task",
                "CSS",
                "2026-11-30",
                "Notes"
            )

            viewModel.toggleTaskCompletion(id)

            assertTrue(
                viewModel.updateTask(
                    id,
                    "Changed",
                    "Math",
                    "2026-12-01",
                    "New"
                )
            )

            val tasks = withTimeout(5000) {
                viewModel.tasks.first { list ->
                    list.any {
                        it.id == id &&
                                it.title == "Changed" &&
                                it.isCompleted
                    }
                }
            }

            val task = tasks.single {
                it.id == id
            }

            assertEquals("Math", task.course)
            assertEquals("New", task.notes)

            viewModel.deleteTask(id)

            withTimeout(5000) {
                viewModel.tasks.first { list ->
                    list.none { it.id == id }
                }
            }

            assertFalse(
                viewModel.updateTask(
                    id,
                    "Missing",
                    "CSS",
                    "2026-12-01",
                    ""
                )
            )
        }
}