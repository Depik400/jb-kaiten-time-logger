package com.depik400.kaitentimelogger.models

data class TimeLogData(
    val cardId: Int,
    val timeSpent: Int,
    val forDate: String,
    val comment: String,
    val roleId: Int
)