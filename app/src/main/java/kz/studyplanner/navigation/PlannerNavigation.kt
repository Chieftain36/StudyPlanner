package kz.studyplanner.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kz.studyplanner.data.StudyTask
import kz.studyplanner.ui.screens.TaskFormScreen
import kz.studyplanner.ui.screens.TaskListScreen
import kz.studyplanner.viewmodel.PlannerViewModel

@Composable
fun PlannerNavigation(
    plannerViewModel: PlannerViewModel
) {
    val navController = rememberNavController()

    val tasks by plannerViewModel.tasks.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = "tasks"
    ) {
        composable("tasks") {
            Scaffold(
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            navController.navigate("new") {
                                launchSingleTop = true
                            }
                        }
                    ) {
                        Text(
                            text = "+ Add task",
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            ) { innerPadding ->
                TaskListScreen(
                    tasks = tasks,
                    onToggleTask = plannerViewModel::toggleTaskCompletion,
                    onOpenTask = { taskId ->
                        navController.navigate("task/$taskId") {
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier
                        .padding(innerPadding)
                        .padding(bottom = 80.dp)
                )
            }
        }

        composable("new") {
            TaskFormScreen(
                onBack = {
                    navController.popBackStack()
                },
                onSave = { title, course, dueDate, notes ->
                    val error = plannerViewModel.validateTask(
                        title = title,
                        course = course,
                        dueDate = dueDate
                    )

                    if (error == null) {
                        val taskId = plannerViewModel.addTask(
                            title = title,
                            course = course,
                            dueDate = dueDate,
                            notes = notes
                        )

                        navController.navigate("task/$taskId") {
                            popUpTo("tasks")
                            launchSingleTop = true
                        }
                    }

                    error
                }
            )
        }

        composable(
            route = "task/{taskId}",
            arguments = listOf(
                navArgument("taskId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getLong("taskId")
            val task = tasks.find { it.id == taskId }

            TaskDetailScreen(
                task = task,
                onBack = {
                    navController.popBackStack()
                },
                onToggle = {
                    task?.let {
                        plannerViewModel.toggleTaskCompletion(it.id)
                    }
                },
                onDelete = {
                    task?.let {
                        plannerViewModel.deleteTask(it.id)
                        navController.popBackStack()
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskDetailScreen(
    task: StudyTask?,
    onBack: () -> Unit,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by rememberSaveable(task?.id) {
        mutableStateOf(false)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Task details")
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (task == null) {
                Text(
                    text = "Task not found.",
                    style = MaterialTheme.typography.titleLarge
                )
            } else {
                Text(
                    text = task.course,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = task.title,
                    style = MaterialTheme.typography.headlineLarge
                )

                Text(
                    text = "Due: ${task.dueDate}",
                    style = MaterialTheme.typography.bodyLarge
                )

                Text(
                    text = if (task.isCompleted) {
                        "Completed"
                    } else {
                        "In progress"
                    },
                    style = MaterialTheme.typography.labelLarge
                )

                HorizontalDivider()

                Text(
                    text = "Notes",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = task.notes.ifBlank {
                        "No notes yet."
                    },
                    style = MaterialTheme.typography.bodyLarge
                )

                Button(
                    onClick = onToggle,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (task.isCompleted) {
                            "Mark as active"
                        } else {
                            "Mark as completed"
                        }
                    )
                }

                OutlinedButton(
                    onClick = {
                        showDeleteDialog = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete task")
                }
            }
        }
    }

    if (showDeleteDialog && task != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
            },
            title = {
                Text("Delete task?")
            },
            text = {
                Text(
                    text = "Delete \"${task.title}\"? " +
                            "This action cannot be undone."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text(
                        text = "Delete",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}