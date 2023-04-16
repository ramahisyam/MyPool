package com.example.mypool

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.google.firebase.database.*

class PhActivity : AppCompatActivity() {
    private lateinit var database: FirebaseDatabase
    private lateinit var dbRef: DatabaseReference
    private lateinit var phData: TextView
    private lateinit var phStatus: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ph)
        phData = findViewById(R.id.detail_ph_data)
        phStatus = findViewById(R.id.detail_ph_status)

        database = FirebaseDatabase.getInstance()
        dbRef = database.reference.child("Data")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {
                    val dataPH = snapshot.child("PH").value.toString()
                    phData.text = dataPH
                    if (dataPH.toFloat() <= 5.0) {
                        phStatus.text = "Low"
                        phStatus.setTextColor(Color.parseColor("#FEDB39"))
                    } else if (dataPH.toFloat() in 5.1..8.0) {
                        phStatus.text = "Normal"
                        phStatus.setTextColor(Color.parseColor("#1CD6CE"))
                    } else if (dataPH.toFloat() in 8.0..14.0) {
                        phStatus.text = "High"
                        phStatus.setTextColor(Color.parseColor("#D61C4E"))
                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {
                
            }

        })
    }
}