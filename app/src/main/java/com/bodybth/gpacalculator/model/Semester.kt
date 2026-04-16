package com.bodybth.gpacalculator.model

import java.util.UUID

data class Semester(
    val id: String = UUID.randomUUID().toString(),
    var name: String = "Semester 1",
    val courses: MutableList<Course> = mutableListOf()
) {
    fun calculateGpa(): Double {
        if (courses.isEmpty()) return 0.0
        var totalPoints = 0.0
        var totalCredits = 0
        for (course in courses) {
            val pts = gradeToPoints(course.grade) + weightBonus(course.weight)
            totalPoints += pts.coerceAtMost(4.0) * course.creditHours
            totalCredits += course.creditHours
        }
        return if (totalCredits == 0) 0.0
        else (totalPoints / totalCredits).coerceIn(0.0, 4.0)
    }

    companion object {
        val GRADES = listOf("A+", "A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "D-", "F")
        val WEIGHTS = listOf("Regular", "Honors", "AP")

        fun gradeToPoints(grade: String): Double = when (grade) {
            "A+", "A" -> 4.0
            "A-"      -> 3.7
            "B+"      -> 3.3
            "B"       -> 3.0
            "B-"      -> 2.7
            "C+"      -> 2.3
            "C"       -> 2.0
            "C-"      -> 1.7
            "D+"      -> 1.3
            "D"       -> 1.0
            "D-"      -> 0.7
            else      -> 0.0   // F
        }

        fun weightBonus(weight: String): Double = when (weight) {
            "Honors" -> 0.5
            "AP"     -> 1.0
            else     -> 0.0
        }
    }
}
