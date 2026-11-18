package kz.studyplanner.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kz.studyplanner.data.StudyTask
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskFormScreen(
    onBack: () -> Unit,
    onSave: (String, String, String, String) -> String?,
    initialTask: StudyTask? = null
) {
    var title by rememberSaveable(initialTask?.id) {
        mutableStateOf(initialTask?.title ?: "")
    }

    var course by rememberSaveable(initialTask?.id) {
        mutableStateOf(initialTask?.course ?: "")
    }

    var dueDate by rememberSaveable(initialTask?.id) {
        mutableStateOf(
            initialTask?.dueDate ?: LocalDate.now().toString()
        )
    }

    var notes by rememberSaveable(initialTask?.id) {
        mutableStateOf(initialTask?.notes ?: "")
    }

    var error by rememberSaveable(initialTask?.id) {
        mutableStateOf<String?>(null)
    }

    val isEditing = initialTask != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) {
                            "Edit task"
                        } else {
                            "New task"
                        }
                    )
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
                onClick = {
                    error = onSave(title, course, dueDate, notes)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isEditing) {
                        "Save changes"
                    } else {
                        "Save task"
                    }
                )
            }
        }
    }
}