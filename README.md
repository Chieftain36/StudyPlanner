# StudyPlanner

Android application for managing study tasks, courses and deadlines.

CSS 216 Mobile Application Development.

## Author

Aibek Zhanat — [Chieftain36](https://github.com/Chieftain36)

## Features

- View study tasks and their deadlines.
- Search tasks by title or course.
- Filter all, active and completed tasks.
- View task details and notes.
- Create and edit tasks.
- Validate required fields and calendar dates.
- Mark tasks as completed or active.
- Delete tasks with confirmation.
- Preserve search, filters and form fields during screen rotation.
- Provide descriptive accessibility labels for task checkboxes.

## Technology

- Kotlin
- Jetpack Compose
- Material 3
- ViewModel
- StateFlow
- Navigation Compose
- JUnit

## Architecture

The application follows MVVM.

```text
Compose screens -> PlannerViewModel -> TaskRepository
Compose screens <- StateFlow        <- TaskRepository
```

- `StudyTask` is an immutable task data model.
- `TaskRepository` stores tasks and performs data operations.
- `PlannerViewModel` validates input and handles task actions.
- Compose screens display state and send user actions.
- `PlannerNavigation` connects screens and passes task IDs.

The UI collects StateFlow using `collectAsStateWithLifecycle`.

Search, filters and form drafts use `rememberSaveable`.

## Project structure

```text
app/src/main/java/kz/studyplanner/
├── MainActivity.kt
├── data/
│   ├── StudyTask.kt
│   └── TaskRepository.kt
├── viewmodel/
│   └── PlannerViewModel.kt
├── screens/
│   ├── TaskListScreen.kt
│   └── TaskFormScreen.kt
├── navigation/
│   └── PlannerNavigation.kt
└── ui/theme/
```

The files in `screens/` declare the package
`kz.studyplanner.ui.screens`.

## Navigation

- `tasks`: task list.
- `new`: task creation form.
- `task/{taskId}`: task details.
- `edit/{taskId}`: task editing form.

`taskId` is passed as a Long argument.

After creation, the application opens the new task's details.
After editing, it returns to the existing details screen.
Back navigation uses the Navigation Compose back stack.

## Run

1. Clone the repository:

   ```powershell
   git clone https://github.com/Chieftain36/StudyPlanner.git
   ```

2. Open the StudyPlanner folder in Android Studio.
3. Install Android SDK 37 if requested.
4. Use the bundled Android Studio JDK and sync Gradle.
5. Select an emulator or device with Android API 26 or later.
6. Run the `app` configuration.

## Build and tests

Run from the project root:

```powershell
./gradlew.bat assembleDebug testDebugUnitTest
```

Repository tests cover task creation, editing, completion,
deletion and rejected blank fields.

ViewModel tests cover input validation, invalid creation,
editing while preserving identity and completion, and
editing a deleted task.

## Current limitations

Tasks are stored in memory.

ViewModel preserves task data during configuration changes,
but process termination resets the task list to sample data.

Saved UI state is not a persistent task database.

The application currently has no Room database, server
synchronization or authentication.
