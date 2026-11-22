package kz.studyplanner.data

enum class StudyTopic(
    val title: String,
    val tag: String
) {
    MOBILE("Mobile Programming", "kotlin"),
    DEVOPS("DevOps", "docker"),
    CLOUD("Cloud Computing", "amazon-web-services")
}