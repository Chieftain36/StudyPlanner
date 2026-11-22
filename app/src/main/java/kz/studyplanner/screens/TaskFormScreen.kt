package kz.studyplanner.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kz.studyplanner.data.StudyTask

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskFormScreen(
    onBack: () -> Unit,
    onSave: suspend (String, String, String, String) -> String?,
    initialTask: StudyTask? = null,
    initialTitle: String = "",
    initialCourse: String = "",
    initialNotes: String = ""
) {
    var title by rememberSaveable(initialTask?.id, initialTitle) {
        mutableStateOf(initialTask?.title ?: initialTitle)
    }

    var course by rememberSaveable(initialTask?.id, initialCourse) {
        mutableStateOf(initialTask?.course ?: initialCourse)
    }

    var dueDate by rememberSaveable(initialTask?.id) {
        mutableStateOf(
            initialTask?.dueDate ?: LocalDate.now().toString()
        )
    }

    var notes by rememberSaveable(initialTask?.id, initialNotes) {
        mutableStateOf(initialTask?.notes ?: initialNotes)
    }

    var error by rememberSaveable(initialTask?.id) {
        mutableStateOf<String?>(null)
    }

    val scope = rememberCoroutineScope()

    var isSaving by remember {
        mutableStateOf(false)
    }

    val isEditing = initialTask != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditing) "Edit task" else "New task"
                    )
                },
                navigationIcon = {
                    TextButton(
                        onClick = onBack,
                        enabled = !isSaving
                    ) {
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
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (isEditing) {
                    "Update your plan"
                } else {
                    "Plan your next step"
                },
                style = MaterialTheme.typography.headlineMedium
            )

            OutlinedTextField(
                enabled = !isSaving,
                value = title,
                onValueChange = {
                    title = it
                    error = null
                },
                label = { Text("Task title *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                enabled = !isSaving,
                value = course,
                onValueChange = {
                    course = it
                    error = null
                },
                label = { Text("Course *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                enabled = !isSaving,
                value = dueDate,
                onValueChange = {
                    dueDate = it
                    error = null
                },
                label = { Text("Due date *") },
                supportingText = {
                    Text("YYYY-MM-DD, for example 2026-11-25")
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                enabled = !isSaving,
                value = notes,
                onValueChange = {
                    notes = it
                    error = null
                },
                label = { Text("Notes") },
                minLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            error?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                enabled = !isSaving,
                onClick = {
                    if (!isSaving) {
                        isSaving = true

                        scope.launch {
                            try {
                                error = onSave(
                                    title,
                                    course,
                                    dueDate,
                                    notes
                                )
                            } catch (exception: CancellationException) {
                                throw exception
                            } catch (exception: Exception) {
                                error =
                                    "Could not save the task. Please try again."
                            } finally {
                                isSaving = false
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (isEditing) {
                        "Save changes"
                    } else {
                        "Save task"
                    }
                )
            }
        }
    }
}