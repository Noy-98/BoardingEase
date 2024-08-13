package com.example.boardingease

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class TenantsViewDetailsDashboard : AppCompatActivity() {

    private lateinit var boardingData: BoardingDataStructureDB
    private lateinit var currentBId: String

    private lateinit var lastNameEditText: TextView
    private lateinit var roomNoEditText: TextView
    private lateinit var numBordersEditText: TextView
    private lateinit var statusEditText: TextView
    private lateinit var priceEditText: TextView
    private lateinit var contactNoEditText: TextView
    private lateinit var addressEditText: TextView
    private lateinit var rulesEditText: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tenants_view_details_dashboard)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNavigationView.selectedItemId = R.id.home
        bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
            if (item.itemId == R.id.home) {
                startActivity(Intent(applicationContext, TenantHomeDashboard::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                finish()
                return@setOnItemSelectedListener true
            } else if (item.itemId == R.id.notification) {
                startActivity(Intent(applicationContext, TenantsNotificationDashboard::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                finish()
                return@setOnItemSelectedListener true
            } else if (item.itemId == R.id.profile) {
                startActivity(Intent(applicationContext, TenantProfileDashboard::class.java))
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

        currentBId = intent.getStringExtra("b_id") ?: ""

        boardingData = intent.getSerializableExtra("boardingData") as BoardingDataStructureDB

        // Initialize UI elements
        lastNameEditText = findViewById(R.id.last_name)
        roomNoEditText = findViewById(R.id.room_no)
        numBordersEditText = findViewById(R.id.number_of_borders)
        statusEditText = findViewById(R.id.status)
        priceEditText = findViewById(R.id.price)
        contactNoEditText = findViewById(R.id.contact_no)
        addressEditText = findViewById(R.id.address)
        rulesEditText = findViewById(R.id.rules_and_regulations)

        populateFields()

        val reserve_bttn = findViewById<AppCompatButton>(R.id.reserve_button)
        reserve_bttn.setOnClickListener {
            val intent = Intent(this, TenantsPaymentDashboard::class.java)
            intent.putExtra("b_id", currentBId)
            intent.putExtra("boardingData", boardingData)
            startActivity(intent)
        }
    }

    private fun populateFields() {
        lastNameEditText.setText(boardingData.landlord_lastname)
        roomNoEditText.setText(boardingData.room_number)
        numBordersEditText.setText(boardingData.number_of_borders)
        statusEditText.setText(boardingData.status)
        priceEditText.setText(boardingData.price)
        contactNoEditText.setText(boardingData.contact_number)
        addressEditText.setText(boardingData.address)
        rulesEditText.setText(boardingData.rules_and_regulations)

        boardingData.unitPictureUrl?.let {
            val boardingImageView = findViewById<ImageView>(R.id.boarding_pic)
            Glide.with(this)
                .load(it)
                .into(boardingImageView)
        }
        boardingData.permitImageUrl?.let {
            val permitImageView = findViewById<ImageView>(R.id.permit_pic)
            Glide.with(this)
                .load(it)
                .into(permitImageView)
        }
    }
}