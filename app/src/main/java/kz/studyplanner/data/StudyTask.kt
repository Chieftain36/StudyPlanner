package kz.studyplanner.data

data class StudyTask(
    val id: Long,
    val title: String,
    val course: String,
    val dueDate: String,
    val notes: String = "",
    val isCompleted: Boolean = false
)