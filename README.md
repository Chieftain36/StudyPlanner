# StudyPlanner

An Android application for managing study tasks and turning
programming questions into personal learning tasks.

CSS 216 Mobile Application Development.

## Author

Aibek Zhanat — [Chieftain36](https://github.com/Chieftain36)

## Features

### Task planner

- Create tasks with a course, deadline and personal notes.
- Search tasks by title or course.
- Filter all, active and completed tasks.
- View and edit task details.
- Validate required fields and calendar dates.
- Mark tasks as completed or active.
- Delete tasks with confirmation.
- Store tasks locally with Room.
- Keep tasks, notes and completion status after restarting the app.

### Study Lab

- Browse Stack Overflow questions related to university courses.
- Choose Mobile Programming, DevOps or Cloud Computing.
- Load questions through the public Stack Exchange REST API.
- Display loading, success, empty and error states.
- Retry failed requests.
- Display author avatars with Coil.
- Open the original question in a browser.
- Create a study task from a question.
- Prefill the title, course, author and source link.
- Set a deadline and add personal notes before saving.
- Remember the selected topic with Preferences DataStore.

## Technologies

- Kotlin
- Jetpack Compose and Material 3
- ViewModel and StateFlow
- Navigation Compose
- Kotlin Coroutines and Flow
- Retrofit and Kotlin Serialization
- Coil
- Room and KSP
- Preferences DataStore
- JUnit and Android instrumentation tests

## Architecture

The application follows MVVM.

```text
Task screens
    -> PlannerViewModel
    -> TaskRepository
    -> TaskDao
    -> Room database

Study Lab screen
    -> StudyLabViewModel
    -> StudyLabRepository
    -> StudyLabApi
    -> Stack Exchange REST API

Topic selection
    -> StudyLabViewModel
    -> StudyPreferencesRepository
    -> Preferences DataStore
```

Repositories handle data access.

ViewModels coordinate actions and expose read-only StateFlow
properties. Compose screens observe these properties with
collectAsStateWithLifecycle.

Room exposes task changes through Flow. PlannerViewModel
collects them and updates the task list automatically.

Form fields, task search and filters use rememberSaveable.

## Project structure

```text
app/src/main/java/kz/studyplanner/
├── MainActivity.kt
├── data/
│   ├── StudyTask.kt
│   ├── StudyTopic.kt
│   ├── TaskRepository.kt
│   ├── StudyLabRepository.kt
│   ├── StudyPreferencesRepository.kt
│   ├── local/
│   │   ├── TaskDao.kt
│   │   └── PlannerDatabase.kt
│   └── remote/
│       ├── ApiClient.kt
│       ├── StudyLabApi.kt
│       └── QuestionResponse.kt
├── viewmodel/
│   ├── PlannerViewModel.kt
│   └── StudyLabViewModel.kt
├── screens/
│   ├── TaskListScreen.kt
│   ├── TaskFormScreen.kt
│   └── StudyLabScreen.kt
├── navigation/
│   ├── PlannerNavigation.kt
│   └── StudyLabNavigation.kt
└── ui/theme/
```

Files in screens/ declare the package kz.studyplanner.ui.screens.

## REST API

Study Lab uses the public Stack Exchange API:

https://api.stackexchange.com/2.3/questions

Requests use these parameters:

- site=stackoverflow
- sort=votes
- order=desc
- pagesize=20
- filter=withbody
- tagged=<selected topic tag>

Topic tags:

- Mobile Programming: kotlin
- DevOps: docker
- Cloud Computing: amazon-web-services

Retrofit defines a suspend API request. Kotlin Serialization
maps JSON fields to Kotlin DTOs.

The repository checks API errors and rejects incomplete
question entries. It respects the backoff value returned
by the service before making another request.

Study Lab requires internet access and is subject to API quotas.

## Local storage

### Room

StudyTask is the entity for the tasks table.

TaskDao provides:

- Observable task retrieval.
- Task insertion.
- Task editing.
- Completion status changes.
- Task deletion.

PlannerDatabase creates the local study_planner.db database.

Task data is stored on the device and survives app restarts.

### DataStore

StudyPreferencesRepository stores the selected Study Lab topic
under the selected_topic key.

The saved topic is read before the initial question request.

Room stores tasks; DataStore stores the topic preference.

## Navigation

The bottom navigation contains Tasks and Study Lab.

Task routes:

- tasks
- new
- task/{taskId}
- edit/{taskId}

Task IDs use Long navigation arguments.

Study Lab routes:

- questions
- study_task with title, course, link and author arguments

Text arguments are encoded with Uri.encode.

After creating a regular task, the app returns to the task list.
After creating a task from Study Lab, it returns to the questions
and displays a confirmation message.

## Run

1. Clone the repository:

   ```powershell
   git clone https://github.com/Chieftain36/StudyPlanner.git
   ```

2. Open the StudyPlanner folder in Android Studio.
3. Install Android SDK 37 if requested.
4. Use the bundled Android Studio JDK.
5. Sync Gradle.
6. Select an emulator or device with Android API 26 or later.
7. Run the app configuration.

## Build and tests

Run from the project root:

```powershell
.\gradlew.bat assembleDebug testDebugUnitTest assembleDebugAndroidTest
```

This builds the app, runs local unit tests and builds the
Android test APK.

To run Android tests, start an emulator or connect a device:

```powershell
.\gradlew.bat connectedDebugAndroidTest
```

Local tests cover JSON decoding, including question and author
fields, empty results and API error responses.

Android tests cover:

- Task creation, editing, completion and deletion with Room.
- Task persistence after closing and reopening a database.
- ViewModel validation and task state updates.
- Topic tags passed to the API.
- Filtering incomplete questions.
- Empty, invalid and failed API responses.
- Retrying after a connection failure.
- Reading saved DataStore preferences.
- Observing preference changes through Flow.

Repository API tests use a fake API and do not require internet.

## Manual verification

1. Create a task with a deadline and notes.
2. Edit the task and mark it as completed.
3. Force-stop and reopen the app.
4. Verify the task and its completion status remain.
5. Delete the task and reopen the app.
6. Verify the deleted task does not return.
7. Open Study Lab and switch topics.
8. Verify questions and author avatars appear.
9. Open an original question in the browser.
10. Create a study task from a question and add personal notes.
11. Find the saved task in Tasks.
12. Select DevOps, force-stop and reopen the app.
13. Verify DevOps remains selected in Study Lab.
14. Disable internet and switch to another topic.
15. Verify an error appears.
16. Enable internet and press Retry.

## Limitations

- Tasks are stored locally without account or cloud synchronization.
- Study Lab needs internet to load questions.
- Question lists are not cached for offline browsing.
- Stack Overflow questions are learning resources, not official
  university assignments or course materials.
- Clearing app storage or uninstalling the app removes local data.