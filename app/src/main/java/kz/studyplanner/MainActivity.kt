package kz.studyplanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kz.studyplanner.ui.screens.TaskListScreen
import kz.studyplanner.ui.theme.StudyPlannerTheme
import kz.studyplanner.viewmodel.PlannerViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            StudyPlannerTheme {
                val plannerViewModel: PlannerViewModel = viewModel()

                val tasks by plannerViewModel.tasks
                    .collectAsStateWithLifecycle()

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    TaskListScreen(
                        tasks = tasks,
                        onToggleTask = plannerViewModel::toggleTaskCompletion,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}