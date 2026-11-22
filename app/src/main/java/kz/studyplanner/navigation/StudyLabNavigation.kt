package kz.studyplanner.navigation

import android.net.Uri
import android.text.Html
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.launch
import kz.studyplanner.ui.screens.StudyLabScreen
import kz.studyplanner.ui.screens.TaskFormScreen
import kz.studyplanner.viewmodel.PlannerViewModel
import kz.studyplanner.viewmodel.StudyLabViewModel

@Composable
fun StudyLabNavigation(
    plannerViewModel: PlannerViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    val context = LocalContext.current

    val studyLabViewModel: StudyLabViewModel = viewModel(
        factory = StudyLabViewModel.factory(context)
    )

    val selectedTopic by studyLabViewModel
        .selectedTopic
        .collectAsStateWithLifecycle()

    val uiState by studyLabViewModel
        .uiState
        .collectAsStateWithLifecycle()

    val snackbarHostState = remember {
        SnackbarHostState()
    }

    val scope = rememberCoroutineScope()

    Box(modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = "questions"
        ) {
            composable("questions") {
                StudyLabScreen(
                    selectedTopic = selectedTopic,
                    uiState = uiState,
                    onSelectTopic = studyLabViewModel::selectTopic,
                    onRetry = studyLabViewModel::retry,
                    onCreateTask = { question ->
                        val title = Html.fromHtml(
                            question.title.orEmpty(),
                            Html.FROM_HTML_MODE_LEGACY
                        ).toString().trim()

                        val author = Html.fromHtml(
                            question.owner?.displayName
                                ?: "Unknown author",
                            Html.FROM_HTML_MODE_LEGACY
                        ).toString().trim()

                        navController.navigate(
                            "study_task?title=${Uri.encode(title)}" +
                                    "&course=${Uri.encode(selectedTopic.title)}" +
                                    "&link=${Uri.encode(question.link.orEmpty())}" +
                                    "&author=${Uri.encode(author)}"
                        ) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(
                route = "study_task?title={title}&course={course}&link={link}&author={author}",
                arguments = listOf(
                    "title",
                    "course",
                    "link",
                    "author"
                ).map { name ->
                    navArgument(name) {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                }
            ) { entry ->
                val title = entry.arguments
                    ?.getString("title")
                    .orEmpty()

                val course = entry.arguments
                    ?.getString("course")
                    .orEmpty()

                val link = entry.arguments
                    ?.getString("link")
                    .orEmpty()

                val author = entry.arguments
                    ?.getString("author")
                    .orEmpty()

                TaskFormScreen(
                    initialTitle = title,
                    initialCourse = course,
                    initialNotes =
                        "Source: Stack Overflow\n" +
                                "Author: $author\n" +
                                "$link\n\n" +
                                "My notes:\n",
                    onBack = {
                        navController.popBackStack()
                    },
                    onSave = { taskTitle, taskCourse, dueDate, notes ->
                        val error = plannerViewModel.validateTask(
                            taskTitle,
                            taskCourse,
                            dueDate
                        )

                        if (error == null) {
                            plannerViewModel.addTask(
                                taskTitle,
                                taskCourse,
                                dueDate,
                                notes
                            )

                            navController.popBackStack()

                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    "Task saved. Find it in Tasks."
                                )
                            }
                        }

                        error
                    }
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(
                Alignment.BottomCenter
            )
        )
    }
}