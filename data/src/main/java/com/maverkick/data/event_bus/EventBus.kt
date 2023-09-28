package com.maverkick.data.event_bus

import com.maverkick.data.models.CourseType
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * Data class representing an event when a lesson is completed.
 * This event is primarily triggered within the 'tasks' module, when a lesson finishes,
 * to notify other parts of the system to update the database and UI.
 *
 * @param courseId The unique identifier for the course.
 * @param lessonId The unique identifier for the lesson within the course.
 */
data class LessonCompletedEvent(val courseId: String, val lessonId: String)

data class CourseWithdrawnEvent(val studentId: String, val courseId: String, val courseType: CourseType)

data class CourseGenerationCompletedEvent(val courseId: String)

/**
 * Singleton object serving as an Event Bus to facilitate communication between different components.
 * Specifically, it is used within the 'tasks' module to publish an event when a lesson is completed,
 * which then triggers updates to the database and UI in the corresponding parts of the application.
 *
 * Usage within the 'tasks' module:
 * - Publish an event after lesson completion: `EventBus.lessonCompletedEvent.emit(LessonCompletedEvent(courseId, lessonId))`
 * - Other modules can subscribe to this event and respond with updates to the database and UI.
 */
object EventBus {
    /**
     * Mutable shared flow to handle lesson completed events, originating from the 'tasks' module.
     * Different parts of the application, such as database handlers and UI controllers, can collect these events
     * and execute necessary updates, such as storing completion status in the database and refreshing the UI.
     * The 'replay' parameter is set to 1 to ensure the most recent event is available to new collectors.
     */
    val lessonCompletedEvent = MutableSharedFlow<LessonCompletedEvent>(replay = 1)

    val courseWithdrawnEvent = MutableSharedFlow<CourseWithdrawnEvent>(replay = 1)

    val courseGenerationCompletedEvent = MutableSharedFlow<CourseGenerationCompletedEvent>(replay = 1)
}
