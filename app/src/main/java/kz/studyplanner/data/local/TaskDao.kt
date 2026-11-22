package kz.studyplanner.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kz.studyplanner.data.StudyTask

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks ORDER BY dueDate ASC, id DESC")
    fun observeTasks(): Flow<List<StudyTask>>

    @Insert
    suspend fun insertTask(task: StudyTask): Long

    @Query(
        """
        UPDATE tasks
        SET title = :title,
            course = :course,
            dueDate = :dueDate,
            notes = :notes
        WHERE id = :taskId
        """
    )
    suspend fun updateTask(
        taskId: Long,
        title: String,
        course: String,
        dueDate: String,
        notes: String
    ): Int

    @Query(
        """
        UPDATE tasks
        SET isCompleted = NOT isCompleted
        WHERE id = :taskId
        """
    )
    suspend fun toggleTaskCompletion(taskId: Long): Int

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTask(taskId: Long): Int
}