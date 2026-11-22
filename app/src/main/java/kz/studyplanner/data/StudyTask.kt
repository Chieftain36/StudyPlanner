package kz.studyplanner.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class StudyTask(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val course: String,
    val dueDate: String,
    val notes: String = "",
    val isCompleted: Boolean = false
)