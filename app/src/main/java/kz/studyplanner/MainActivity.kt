package kz.studyplanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kz.studyplanner.navigation.PlannerNavigation
import kz.studyplanner.navigation.StudyLabNavigation
import kz.studyplanner.ui.theme.StudyPlannerTheme
import kz.studyplanner.viewmodel.PlannerViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            StudyPlannerTheme {
                val plannerViewModel: PlannerViewModel = viewModel(
                    factory = PlannerViewModel.factory(
                        applicationContext
                    )
                )

                val mainNavController = rememberNavController()

                val backStackEntry by
                mainNavController.currentBackStackEntryAsState()

                val currentRoute =
                    backStackEntry?.destination?.route ?: "planner"

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = currentRoute == "planner",
                                onClick = {
                                    mainNavController.navigate("planner") {
                                        popUpTo("planner") {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Text("✓") },
                                label = { Text("Tasks") }
                            )

                            NavigationBarItem(
                                selected = currentRoute == "study_lab",
                                onClick = {
                                    mainNavController.navigate("study_lab") {
                                        popUpTo("planner") {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Text("?") },
                                label = { Text("Study Lab") }
                            )
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = mainNavController,
                        startDestination = "planner",
                        modifier = Modifier.padding(
                            bottom = innerPadding.calculateBottomPadding()
                        )
                    ) {
                        composable("planner") {
                            PlannerNavigation(
                                plannerViewModel = plannerViewModel
                            )
                        }

                        composable("study_lab") {
                            StudyLabNavigation(
                                plannerViewModel = plannerViewModel,
                                modifier = Modifier.padding(
                                    top = innerPadding.calculateTopPadding()
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}