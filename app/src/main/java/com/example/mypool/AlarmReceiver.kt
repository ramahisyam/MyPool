package com.example.mypool

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.database.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    private lateinit var database: FirebaseDatabase
    private lateinit var dbRefFeed: DatabaseReference
    private lateinit var dbRefData: DatabaseReference

    private var jumlah_ikan: String = ""
    private var few: String = "Few"
    private var moderate: String = "Moderate"
    private var many: String = "Many"

    private lateinit var phAir: String
    private var phNetral: String = "Netral"
    private var phAsam: String = "Asam"
    private var phBasa: String = "Basa"

    private lateinit var turbidity: String
    private var turbidityClear: String = "Normal"
    private var turbidityCloudly: String = "Keruh"

    private var jumlah_pakan: String = ""
    private var low: String = "Low"
    private var medium: String = "Medium"
    private var high: String = "High"

    override fun onReceive(p0: Context?, p1: Intent?) {
//        showNotification(p0, "Alarm berbunyi!")
        val i = Intent(p0, MainActivity::class.java)
        p1!!.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(p0,0, p1, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)

        val builder = NotificationCompat.Builder(p0!!, "foxandroid")
            .setSmallIcon(R.drawable.ic_baseline_notifications_24)
            .setContentTitle("Alarm Manager")
            .setContentText("test")
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        val notificationManager = NotificationManagerCompat.from(p0)
        notificationManager.notify(123, builder.build())
//        fuzzy()
        database = FirebaseDatabase.getInstance()
        dbRefFeed = database.reference.child("Feed")
        dbRefFeed.child("feed_now").setValue(1)
    }

    private fun fuzzy() {
        database = FirebaseDatabase.getInstance()
        dbRefData = database.reference.child("Data")

        dbRefData.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val dataJumlah = snapshot.child("jumlah_ikan").value.toString()
                    val dataPH = snapshot.child("PH").value.toString()
                    val dataTurbidity = snapshot.child("Turbidity").value.toString()

                    if (dataJumlah.toInt() in 1..7) {
                        jumlah_ikan = few
                    } else if (dataJumlah.toInt() in 8..15) {
                        jumlah_ikan = moderate
                    } else if (dataJumlah.toInt() in 16..25) {
                        jumlah_ikan = many
                    }

                    if (dataPH.toFloat() <= 5.0) {
                        phAir = phAsam
                    } else if (dataPH.toFloat() in 5.1..8.0) {
                        phAir = phNetral
                    } else if (dataPH.toFloat() in 8.1..14.0) {
                        phAir = phBasa
                    }

                    turbidity = if (dataTurbidity.toFloat().toInt() in 0..50) {
                        turbidityClear
                    } else {
                        turbidityCloudly
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("TAG", "onCancelled: $error")
            }

        })

        if (jumlah_ikan == few && phAir == phAsam && turbidity == turbidityClear){
            //1
            jumlah_pakan = low
        } else if (jumlah_ikan == few && phAir == phAsam && turbidity == turbidityCloudly){
            //2
            jumlah_pakan = low
        } else if (jumlah_ikan == few && phAir == phNetral && turbidity == turbidityClear) {
            //3
            jumlah_pakan = medium
        } else if (jumlah_ikan == few && phAir == phNetral && turbidity == turbidityCloudly) {
            //4
            jumlah_pakan = medium
        } else if (jumlah_ikan == few && phAir == phBasa && turbidity == turbidityClear) {
            //5
            jumlah_pakan = high
        } else if (jumlah_ikan == few && phAir == phBasa && turbidity == turbidityCloudly) {
            //6
            jumlah_pakan = medium
        } else if (jumlah_ikan == moderate && phAir == phAsam && turbidity == turbidityClear) {
            //7
            jumlah_pakan = high
        } else if (jumlah_ikan == moderate && phAir == phAsam && turbidity == turbidityCloudly) {
            //8
            jumlah_pakan = medium
        } else if (jumlah_ikan == moderate && phAir == phNetral && turbidity == turbidityClear) {
            //9
            jumlah_pakan = high
        } else if (jumlah_ikan == moderate && phAir == phNetral && turbidity == turbidityCloudly) {
            //10
            jumlah_pakan = high
        } else if (jumlah_ikan == moderate && phAir == phBasa && turbidity == turbidityClear) {
            //11
            jumlah_pakan = high
        } else if (jumlah_ikan == moderate && phAir == phBasa && turbidity == turbidityCloudly) {
            //12
            jumlah_pakan = medium
        } else if (jumlah_ikan == many && phAir == phAsam && turbidity == turbidityClear) {
            //13
            jumlah_pakan = high
        } else if (jumlah_ikan == many && phAir == phAsam && turbidity == turbidityCloudly) {
            //14
            jumlah_pakan = high
        } else if (jumlah_ikan == many && phAir == phNetral && turbidity == turbidityClear) {
            //15
            jumlah_pakan = high
        } else if (jumlah_ikan == many && phAir == phNetral && turbidity == turbidityCloudly) {
            //16
            jumlah_pakan = high
        } else if (jumlah_ikan == many && phAir == phBasa && turbidity == turbidityClear) {
            //17
            jumlah_pakan = high
        } else if (jumlah_ikan == many && phAir == phBasa && turbidity == turbidityCloudly) {
            //18
            jumlah_pakan = medium
        }

        when (jumlah_pakan) {
            low -> {
                looping(1)
            }
            medium -> {
                looping(3)
            }
            high -> {
                looping(5)
            }
        }

    }

    private fun looping(j: Int) {
        database = FirebaseDatabase.getInstance()
        dbRefFeed = database.reference.child("Feed")
        val delayMillis = 7000L

        CoroutineScope(Dispatchers.Main).launch {
            for (i in 0 until j) {
                delay(delayMillis)

                // Code to be executed after the delay
                // Place your loop logic here
                dbRefFeed.child("feed_now").setValue(1)
                println("Iteration: $i")
            }
        }
    }
}
