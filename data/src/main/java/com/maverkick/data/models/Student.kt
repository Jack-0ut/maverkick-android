package com.maverkick.data.models

/**
 * Class storage for the Student objects
 * @param studentId  the id of the student in the database
 * @param age age to make better recommendations and greater personalization
 * @param dailyStudyTimeMinutes  the number of minutes student would like study daily
 * @param interests  the list of Tags(disciplines) in which student interested
 * @param bricksCollected shows the total number of lessons completed
 * @param enrolledCourses lists all the courses student enrolled in right now
 * @param enrolledGeneratedCourses lists all the text courses generated in which it enrolled in
 * @param generatedTextCourses all the text courses generated by student
 * @param courseGenerationTries number of courses student could generate
 * @param finishedCourses lists all the courses, which have been finished
 **/
data class Student(
    val studentId: String,
    val age: Int,
    val dailyStudyTimeMinutes: Int,
    val interests: List<String>,
    var bricksCollected: Int = 0,
    var enrolledCourses: List<String> = emptyList(),
    var enrolledGeneratedCourses: List<String> = emptyList(),
    var generatedTextCourses: List<String> = emptyList(),
    var courseGenerationTries: Int = 0,
    var finishedCourses: List<String> = emptyList()
) {
    fun toFirebaseStudent(): FirebaseStudent {
        return FirebaseStudent(
            age,
            dailyStudyTimeMinutes,
            interests,
            bricksCollected,
            enrolledCourses,
            enrolledGeneratedCourses,
            generatedTextCourses,
            courseGenerationTries,
            finishedCourses
        )
    }
}

data class FirebaseStudent(
    val age: Int,
    val dailyStudyTimeMinutes: Int,
    val interests: List<String>,
    val bricksCollected: Int = 0,
    var enrolledCourses: List<String> = emptyList(),
    var enrolledGeneratedCourses: List<String> = emptyList(),
    var generatedTextCourses: List<String> = emptyList(),
    val courseGenerationTries: Int = 5,
    var finishedCourses: List<String> = emptyList()
) {
    fun toStudent(studentId: String): Student {
        return Student(
            studentId,
            age,
            dailyStudyTimeMinutes,
            interests,
            bricksCollected,
            enrolledCourses,
            enrolledGeneratedCourses,
            generatedTextCourses,
            courseGenerationTries,
            finishedCourses
        )
    }
}
