package kz.studyplanner.ui.screens

import android.text.Html
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kz.studyplanner.data.StudyTopic
import kz.studyplanner.data.remote.QuestionDto
import kz.studyplanner.viewmodel.StudyLabUiState

@Composable
fun StudyLabScreen(
    selectedTopic: StudyTopic,
    uiState: StudyLabUiState,
    onSelectTopic: (StudyTopic) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Study Lab",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(top = 16.dp)
        )

        Text(
            text = "Explore real questions related to your courses.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StudyTopic.entries.forEach { topic ->
                FilterChip(
                    selected = selectedTopic == topic,
                    onClick = { onSelectTopic(topic) },
                    label = { Text(topic.title) }
                )
            }
        }

        when (uiState) {
            StudyLabUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator()
                        Text("Loading questions...")
                    }
                }
            }

            is StudyLabUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "${uiState.questions.size} questions · Stack Overflow",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    items(
                        items = uiState.questions,
                        key = { it.questionId }
                    ) { question ->
                        QuestionCard(question = question)
                    }
                }
            }

            StudyLabUiState.Empty -> {
                StudyLabMessage(
                    message = "No questions found for this topic.",
                    buttonText = "Try again",
                    onClick = onRetry,
                    modifier = Modifier.weight(1f)
                )
            }

            is StudyLabUiState.Error -> {
                StudyLabMessage(
                    message = uiState.message,
                    buttonText = "Retry",
                    onClick = onRetry,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun QuestionCard(
    question: QuestionDto
) {
    val uriHandler = LocalUriHandler.current

    val title = remember(question.title) {
        decodeHtml(question.title.orEmpty())
    }

    val author = remember(question.owner?.displayName) {
        decodeHtml(
            question.owner?.displayName ?: "Unknown author"
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AuthorAvatar(
                    imageUrl = question.owner?.profileImage,
                    authorName = author
                )

                Text(
                    text = "By $author",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = "Score: ${question.score} · " +
                        if (question.isAnswered) {
                            "Answered"
                        } else {
                            "Not answered yet"
                        },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )

            if (!question.tags.isNullOrEmpty()) {
                Text(
                    text = question.tags.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            TextButton(
                onClick = {
                    val link = question.link

                    if (!link.isNullOrBlank()) {
                        uriHandler.openUri(link)
                    }
                }
            ) {
                Text("Open on Stack Overflow")
            }
        }
    }
}

@Composable
private fun AuthorAvatar(
    imageUrl: String?,
    authorName: String
) {
    val context = LocalContext.current

    val imageRequest = remember(context, imageUrl) {
        ImageRequest.Builder(context)
            .data(imageUrl?.takeIf { it.isNotBlank() })
            .crossfade(true)
            .build()
    }

    val defaultAvatar = painterResource(
        id = android.R.drawable.ic_menu_myplaces
    )

    AsyncImage(
        model = imageRequest,
        contentDescription = "Avatar of $authorName",
        placeholder = defaultAvatar,
        error = defaultAvatar,
        fallback = defaultAvatar,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(
                MaterialTheme.colorScheme.surfaceVariant
            )
    )
}

@Composable
private fun StudyLabMessage(
    message: String,
    buttonText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge
            )

            Button(onClick = onClick) {
                Text(buttonText)
            }
        }
    }
}

private fun decodeHtml(text: String): String {
    return Html.fromHtml(
        text,
        Html.FROM_HTML_MODE_LEGACY
    ).toString().trim()
}