package com.example.boardingease

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class LandlordNotificationConfirmationDashboard : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private lateinit var currentBId: String
    private lateinit var currentUsersId: String
    private lateinit var cid: String

    private lateinit var landlordLNameEditText: TextInputEditText
    private lateinit var roomNoEditText: TextInputEditText
    private lateinit var statusEditText: TextInputEditText
    private lateinit var concernEditText: TextInputEditText

    private lateinit var ProgressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_landlord_notification_confirmation_dashboard)
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

        FirebaseApp.initializeApp(this)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val currentUser = auth.currentUser
        currentUsersId = currentUser?.uid ?: ""

        currentBId = intent.getStringExtra("bid") ?: ""

        val newConfirmationRef = FirebaseDatabase.getInstance().reference.child("ConfirmationTbl").push()
        cid = newConfirmationRef.key ?: ""

        landlordLNameEditText = findViewById(R.id.landlord_last_name)
        roomNoEditText = findViewById(R.id.room_num)
        statusEditText = findViewById(R.id.stat)
        concernEditText = findViewById(R.id.concern)

        ProgressBar = findViewById(R.id.progressBar)

        val submitBttn = findViewById<AppCompatButton>(R.id.submit_bttn)
        submitBttn.setOnClickListener {
            val landlord_lName = landlordLNameEditText.text.toString().trim()
            val roomNo = roomNoEditText.text.toString().trim()
            val status = statusEditText.text.toString().trim()
            val concern = concernEditText.text.toString().trim()

            ProgressBar.visibility = View.VISIBLE

            if (landlord_lName.isEmpty() || roomNo.isEmpty() || status.isEmpty() || concern.isEmpty()) {
                if (landlord_lName.isEmpty()){
                    landlordLNameEditText.error = "Please enter your last name"
                }
                if (roomNo.isEmpty()){
                    roomNoEditText.error = "Please enter your room number"
                }
                if (status.isEmpty()){
                    statusEditText.error = "Please enter the status"
                }
                if (concern.isEmpty()) {
                    concernEditText.error = "Please enter your concern"
                }
                Toast.makeText(this, "All fields are Required!", Toast.LENGTH_SHORT).show()
                ProgressBar.visibility = View.GONE
            } else {
                val confirmationData = LandlordConfirmationDataStructureDB(
                    confirmation_id = cid,
                    landlord_last_name = landlord_lName,
                    room_number = roomNo,
                    status = status,
                    concern = concern
                )
                saveConfirmationData(confirmationData)
            }
        }
    }

    private fun saveConfirmationData(confirmationData: LandlordConfirmationDataStructureDB) {
        val updates = hashMapOf<String, Any>(
            "/UsersTbl/$currentUsersId/ConfirmationTbl/$cid" to confirmationData,
            "/BoardingsTbl/ConfirmationTbl/$cid" to confirmationData
        )

        database.reference.updateChildren(updates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Upload successfully", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LandlordNotificationViewDetailsDashboard::class.java))
            } else {
                Toast.makeText(this, "Failed to Upload", Toast.LENGTH_SHORT).show()
            }
            ProgressBar.visibility = View.GONE
        }
    }
}