package com.example.boardingease

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LandlordNotificationDashboard : AppCompatActivity() {

    private lateinit var notificationAdapter: NotificationAdapter
    private lateinit var databaseReference: DatabaseReference
    private lateinit var notificationList: MutableList<TenantsReservedDataStructureDB>
    private lateinit var currentBId: String

    private lateinit var noPostText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_landlord_notification_dashboard)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNavigationView.selectedItemId = R.id.notification
        bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
            if (item.itemId == R.id.home) {
                startActivity(Intent(applicationContext, LandlordHomeDashboard::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                finish()
                return@setOnItemSelectedListener true
            } else if (item.itemId == R.id.notification) {
                return@setOnItemSelectedListener true
            } else if (item.itemId == R.id.profile) {
                startActivity(Intent(applicationContext, LandlordProfileDashboard::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                finish()
                return@setOnItemSelectedListener true
            } else if (item.itemId == R.id.logout) {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, Login::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                return@setOnItemSelectedListener true
            }
            false
        }

        // Initialize RecyclerView and Adapter
        val recyclerView: RecyclerView = findViewById(R.id.notificationList)
        notificationList = mutableListOf()
        notificationAdapter = NotificationAdapter(this, notificationList)
        recyclerView.adapter = notificationAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        currentBId = intent.getStringExtra("b_id") ?: ""

        noPostText = findViewById(R.id.no_post_text)

        loadNotificationsData()
    }

    private fun loadNotificationsData() {


        databaseReference = FirebaseDatabase.getInstance().getReference("BoardingsTbl/$currentBId/ReservedTenantsTbl")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                notificationList.clear()
                if (snapshot.exists()) {
                    for (b_idSnapshot in snapshot.children) {
                        val boarding = b_idSnapshot.getValue(TenantsReservedDataStructureDB::class.java)
                        if (boarding != null) {
                            notificationList.add(boarding)
                        }
                    }
                    notificationAdapter.notifyDataSetChanged()
                }
                noPostText.visibility = if (notificationList.isEmpty()) View.VISIBLE else View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }
}