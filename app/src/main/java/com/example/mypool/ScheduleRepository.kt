package com.example.mypool

import com.example.mypool.model.ScheduleData
import com.google.firebase.database.*
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.synchronized
import java.lang.Exception

class ScheduleRepository {
    private val databaseReference : DatabaseReference = FirebaseDatabase.getInstance().getReference("Schedule")

    @Volatile private var INSTANCE : ScheduleRepository ?= null

    @OptIn(InternalCoroutinesApi::class)
    fun getInstance() : ScheduleRepository {
        return INSTANCE ?: synchronized(this) {
            val instance = ScheduleRepository()
            INSTANCE = instance
            instance
        }
    }

    fun loadUsers(scheduleList : MutableList<List<ScheduleData>>) {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
//                    val scheduleList2 : List<ScheduleData> = snapshot
                }catch (e : Exception) {

                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
}