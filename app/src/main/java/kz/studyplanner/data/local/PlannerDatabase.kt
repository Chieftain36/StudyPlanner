package kz.studyplanner.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kz.studyplanner.data.StudyTask

@Database(
    entities = [StudyTask::class],
    version = 1,
    exportSchema = false
)
abstract class PlannerDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao

    companion object {

        @Volatile
        private var instance: PlannerDatabase? = null

        fun getDatabase(context: Context): PlannerDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    PlannerDatabase::class.java,
                    "study_planner.db"
                ).build().also {
                    instance = it
                }
            }
        }
    }
}