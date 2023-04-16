package com.example.mypool

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.google.firebase.database.*

class TurbidityActivity : AppCompatActivity() {

    private lateinit var database: FirebaseDatabase
    private lateinit var dbRef: DatabaseReference
    private lateinit var turbidityData: TextView
    private lateinit var turbidityStatus: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_turbidity)
        turbidityData = findViewById(R.id.detail_turbidity_data)
        turbidityStatus = findViewById(R.id.detail_turbidity_status)

        database = FirebaseDatabase.getInstance()
        dbRef = database.reference.child("Data")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {
                    val dataTurbidity = snapshot.child("Turbidity").value.toString()
                    turbidityData.text = dataTurbidity + " NTU"

                    if (dataTurbidity.toFloat().toInt() in 0..50) {
                        turbidityStatus.text = "Normal"
                        turbidityStatus.setTextColor(Color.parseColor("#1CD6CE"))
                    } else if (dataTurbidity.toFloat().toInt() in 51..100) {
                        turbidityStatus.text = "Warning"
                        turbidityStatus.setTextColor(Color.parseColor("#FEDB39"))
                    } else if (dataTurbidity.toFloat().toInt() > 100) {
                        turbidityStatus.text = "Danger"
                        turbidityStatus.setTextColor(Color.parseColor("#D61C4E"))
                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
}