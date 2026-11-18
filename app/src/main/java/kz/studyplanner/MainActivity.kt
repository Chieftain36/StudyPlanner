package kz.studyplanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import kz.studyplanner.navigation.PlannerNavigation
import kz.studyplanner.ui.theme.StudyPlannerTheme
import kz.studyplanner.viewmodel.PlannerViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            StudyPlannerTheme {
                val plannerViewModel: PlannerViewModel = viewModel()

                PlannerNavigation(
                    plannerViewModel = plannerViewModel
                )
            }
        }
    }
}