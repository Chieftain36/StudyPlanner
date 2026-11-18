package kz.studyplanner.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kz.studyplanner.data.StudyTask

private enum class TaskFilter(val label: String) {
    ALL("All"),
    ACTIVE("Active"),
    DONE("Done")
}

@Composable
fun TaskListScreen(
    tasks: List<StudyTask>,
    onToggleTask: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedFilterName by rememberSaveable {
        mutableStateOf(TaskFilter.ALL.name)
    }

    val selectedFilter = TaskFilter.valueOf(selectedFilterName)
    val query = searchQuery.trim()

    val filteredTasks = tasks.filter { task ->
        val matchesSearch =
            task.title.contains(query, ignoreCase = true) ||
                    task.course.contains(query, ignoreCase = true)

        val matchesFilter = when (selectedFilter) {
            TaskFilter.ALL -> true
            TaskFilter.ACTIVE -> !task.isCompleted
            TaskFilter.DONE -> task.isCompleted
        }

        matchesSearch && matchesFilter
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Study Planner",
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${tasks.count { !it.isCompleted }} active · " +
                        "${tasks.count { it.isCompleted }} completed",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search tasks or courses") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TaskFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = {
                            selectedFilterName = filter.name
                        },
                        label = {
                            Text(text = filter.label)
                        }
                    )
                }
            }
        }

        if (filteredTasks.isEmpty()) {
            item {
                Text(
                    text = if (tasks.isEmpty()) {
                        "No tasks yet."
                    } else {
                        "No tasks match your search and filter."
                    }
                )
            }
        }

        items(
            items = filteredTasks,
            key = { task -> task.id }
        ) { task ->
            TaskCard(
                task = task,
                onToggle = { onToggleTask(task.id) }
            )
        }
    }
}

@Composable
private fun TaskCard(
    task: StudyTask,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = task.course,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Due: ${task.dueDate}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = if (task.isCompleted) {
                        "Completed"
                    } else {
                        "In progress"
                    },
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggle() }
            )
        }
    }
}