package kz.studyplanner.data

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class TaskRepositoryTest {

    private lateinit var repository: TaskRepository

    @Before
    fun setUp() {
        repository = TaskRepository()
    }

    @Test
    fun addTask_addsTrimmedTaskWithUniqueId() {
        val initialTasks = repository.tasks.value

        val id = repository.addTask(
            title = "  Prepare presentation  ",
            course = "  CSS 216  ",
            dueDate = " 2026-11-30 ",
            notes = "  Explain navigation  "
        )

        val tasks = repository.tasks.value
        val addedTask = tasks.single { it.id == id }

        assertEquals(initialTasks.size + 1, tasks.size)
        assertFalse(initialTasks.any { it.id == id })

        assertEquals("Prepare presentation", addedTask.title)
        assertEquals("CSS 216", addedTask.course)
        assertEquals("2026-11-30", addedTask.dueDate)
        assertEquals("Explain navigation", addedTask.notes)
        assertFalse(addedTask.isCompleted)

        assertEquals(
            initialTasks,
            tasks.filterNot { it.id == id }
        )
    }

    @Test
    fun addTask_afterDeletion_doesNotReuseDeletedId() {
        val firstId = addExampleTask()

        repository.deleteTask(firstId)

        val secondId = addExampleTask()

        assertNotEquals(firstId, secondId)
    }

    @Test
    fun updateTask_changesFieldsAndPreservesCompletion() {
        val id = addExampleTask()

        repository.toggleTaskCompletion(id)

        val othersBefore = repository.tasks.value.filterNot {
            it.id == id
        }

        val updated = repository.updateTask(
            taskId = id,
            title = "  Updated title  ",
            course = "  Mathematics  ",
            dueDate = " 2026-12-01 ",
            notes = "  Updated notes  "
        )

        val tasks = repository.tasks.value
        val task = tasks.single { it.id == id }

        assertTrue(updated)
        assertEquals(id, task.id)
        assertEquals("Updated title", task.title)
        assertEquals("Mathematics", task.course)
        assertEquals("2026-12-01", task.dueDate)
        assertEquals("Updated notes", task.notes)
        assertTrue(task.isCompleted)

        assertEquals(
            othersBefore,
            tasks.filterNot { it.id == id }
        )
    }

    @Test
    fun updateTask_withUnknownId_returnsFalseWithoutChanges() {
        val before = repository.tasks.value
        val unknownId = before.maxOf { it.id } + 100

        val updated = repository.updateTask(
            taskId = unknownId,
            title = "Unknown task",
            course = "CSS 216",
            dueDate = "2026-12-01",
            notes = ""
        )

        assertFalse(updated)
        assertEquals(before, repository.tasks.value)
    }

    @Test
    fun toggleTaskCompletion_changesOnlySelectedTask() {
        val id = addExampleTask()

        val othersBefore = repository.tasks.value.filterNot {
            it.id == id
        }

        repository.toggleTaskCompletion(id)

        assertTrue(
            repository.tasks.value.single { it.id == id }.isCompleted
        )

        repository.toggleTaskCompletion(id)

        assertFalse(
            repository.tasks.value.single { it.id == id }.isCompleted
        )

        assertEquals(
            othersBefore,
            repository.tasks.value.filterNot { it.id == id }
        )
    }

    @Test
    fun deleteTask_removesOnlySelectedTask() {
        val id = addExampleTask()

        val expectedTasks = repository.tasks.value.filterNot {
            it.id == id
        }

        repository.deleteTask(id)

        assertEquals(expectedTasks, repository.tasks.value)
        assertFalse(repository.tasks.value.any { it.id == id })
    }

    @Test
    fun deleteTask_withUnknownId_doesNotChangeTasks() {
        val before = repository.tasks.value
        val unknownId = before.maxOf { it.id } + 100

        repository.deleteTask(unknownId)

        assertEquals(before, repository.tasks.value)
    }

    @Test
    fun addTask_withBlankTitle_isRejectedWithoutChanges() {
        val before = repository.tasks.value

        assertThrows(IllegalArgumentException::class.java) {
            repository.addTask(
                title = "   ",
                course = "CSS 216",
                dueDate = "2026-11-30",
                notes = ""
            )
        }

        assertEquals(before, repository.tasks.value)
    }

    @Test
    fun addTask_withBlankCourse_isRejectedWithoutChanges() {
        val before = repository.tasks.value

        assertThrows(IllegalArgumentException::class.java) {
            repository.addTask(
                title = "Prepare presentation",
                course = "   ",
                dueDate = "2026-11-30",
                notes = ""
            )
        }

        assertEquals(before, repository.tasks.value)
    }

    private fun addExampleTask(): Long {
        return repository.addTask(
            title = "Prepare presentation",
            course = "CSS 216",
            dueDate = "2026-11-30",
            notes = "Explain the architecture"
        )
    }
}