package com.example.data.repositories

import com.example.data.IDatabaseService
import com.example.data.models.Lesson
import com.example.data.models.Student
import java.util.*
import javax.inject.Inject

/**
 * Class responsible for the Lesson objects interaction with
 * the Cloud Database using Dependency Injection
 * @param databaseService - database to which we're connecting
 **/
class LessonRepository @Inject constructor(private val databaseService: IDatabaseService) {

    /** Add new lesson to the database **/
    fun addLesson(courseID: String, lesson: Lesson, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        databaseService.db.collection("courses").document(courseID).collection("lessons").add(lesson)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onFailure(exception) }
    }

    /** Get the lessons for the course **/
    fun getCourseLessons(courseID: String, onSuccess: (List<Lesson>) -> Unit, onFailure: (Exception) -> Unit) {
        val lessonsRef = databaseService.db.collection("courses").document(courseID).collection("lessons")

        lessonsRef.get()
            .addOnSuccessListener { documents ->
                val lessons = documents.map { it.toObject(Lesson::class.java) }
                onSuccess(lessons)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    /** Get the list of today's lessons for the student **/
    fun getTodayLessons(studentID: String, onSuccess: (List<Lesson>) -> Unit, onFailure: (Exception) -> Unit) {
        // Fetch the student document
        databaseService.db.collection("students").document(studentID).get()
            .addOnSuccessListener { studentDoc ->
                val student = studentDoc.toObject(Student::class.java)
                val dailyStudyTime = student?.dailyStudyTimeMinutes ?: 0

                // Fetch the active courses for the student
                databaseService.db.collection("studentCourses").whereEqualTo("student_id", studentID).whereEqualTo("active", true).get()
                    .addOnSuccessListener { courseDocs ->
                        val courseIds = courseDocs.mapNotNull { it.getString("course_id") }
                        val lessonQueues = mutableListOf<Queue<Lesson>>()
                        var coursesProcessed = 0

                        // Fetch the lessons for each active course
                        for (courseID in courseIds) {
                            databaseService.db.collection("courses").document(courseID).collection("lessons").orderBy("lesson_order").get()
                                .addOnSuccessListener { lessonDocs ->
                                    val lessons = lessonDocs.mapNotNull { it.toObject(Lesson::class.java) }
                                    lessonQueues.add(LinkedList(lessons))
                                    coursesProcessed++

                                    // Round-robin through the lesson queues once all courses have been processed
                                    if (coursesProcessed == courseIds.size) {
                                        val todayLessons = mutableListOf<Lesson>()
                                        var totalLessonTime = 0
                                        var i = 0

                                        while (totalLessonTime < dailyStudyTime && lessonQueues.any { it.isNotEmpty() }) {
                                            val queue = lessonQueues[i % lessonQueues.size]
                                            if (queue.isNotEmpty()) {
                                                val lesson = queue.poll()
                                                val lessonTime = lesson.length
                                                // Add the lesson to the list if there is enough remaining study time
                                                if (totalLessonTime + lessonTime <= (dailyStudyTime * 60)) {
                                                    todayLessons.add(lesson)
                                                    totalLessonTime += lessonTime
                                                }
                                            }
                                            i++
                                        }
                                        onSuccess(todayLessons)
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    onFailure(exception)
                                    return@addOnFailureListener
                                }
                        }
                    }
                    .addOnFailureListener { exception ->
                        onFailure(exception)
                        return@addOnFailureListener
                    }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
}
