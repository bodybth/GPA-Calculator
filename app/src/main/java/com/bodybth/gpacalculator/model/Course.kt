package com.bodybth.gpacalculator.model

import java.util.UUID

data class Course(
    val id: String = UUID.randomUUID().toString(),
    var name: String = "",
    var grade: String = "A",
    var creditHours: Int = 3,
    var weight: String = "Regular"
)
