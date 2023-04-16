package com.example.mypool.model

data class ScheduleData (
    var schedule: Long? = null
    ) {
    fun getSchedule(): Long {
        return schedule!!
    }
}