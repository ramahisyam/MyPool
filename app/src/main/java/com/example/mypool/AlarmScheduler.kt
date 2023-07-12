package com.example.mypool

import com.example.mypool.model.ScheduleData

interface AlarmScheduler {
    fun schedule(item: ScheduleData)
    fun cancel(item: ScheduleData)
}