package kz.studyplanner.viewmodel

import org.junit.Assert.*
import org.junit.Test

class PlannerViewModelTest {

    @Test
    fun validationRejectsBlankFieldsAndInvalidDates() {
        val viewModel = PlannerViewModel()

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
    fun invalidAddDoesNotChangeTasks() {
        val viewModel = PlannerViewModel()
        val before = viewModel.tasks.value

        assertThrows(IllegalArgumentException::class.java) {
            viewModel.addTask(
                title = "Task",
                course = "CSS",
                dueDate = "wrong",
                notes = ""
            )
        }

        assertEquals(before, viewModel.tasks.value)
    }

    @Test
    fun editPreservesIdAndCompletion() {
        val viewModel = PlannerViewModel()

        val id = viewModel.addTask(
            title = "Task",
            course = "CSS",
            dueDate = "2026-11-30",
            notes = "Notes"
        )

        viewModel.toggleTaskCompletion(id)

        val countBefore = viewModel.tasks.value.size

        val updated = viewModel.updateTask(
            taskId = id,
            title = "Changed",
            course = "Math",
            dueDate = "2026-12-01",
            notes = "New notes"
        )

        val task = viewModel.tasks.value.single { it.id == id }

        assertTrue(updated)
        assertEquals(id, task.id)
        assertEquals("Changed", task.title)
        assertEquals("Math", task.course)
        assertEquals("2026-12-01", task.dueDate)
        assertEquals("New notes", task.notes)
        assertTrue(task.isCompleted)
        assertEquals(countBefore, viewModel.tasks.value.size)
    }

    @Test
    fun deletedTaskCannotBeEdited() {
        val viewModel = PlannerViewModel()

        val id = viewModel.addTask(
            title = "Task",
            course = "CSS",
            dueDate = "2026-11-30",
            notes = ""
        )

        viewModel.deleteTask(id)

        val before = viewModel.tasks.value

        val updated = viewModel.updateTask(
            taskId = id,
            title = "Changed",
            course = "CSS",
            dueDate = "2026-12-01",
            notes = ""
        )

        assertFalse(updated)
        assertEquals(before, viewModel.tasks.value)
    }
}