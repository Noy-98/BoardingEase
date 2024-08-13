package com.example.boardingease

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class LandlordNotificationViewDetailsDashboard : AppCompatActivity() {

    private lateinit var reservedData: TenantsReservedDataStructureDB
    private lateinit var currentReserveId: String

    private lateinit var LandlordlastNameText: TextView
    private lateinit var roomNumberText: TextView
    private lateinit var numberofBordersText: TextView
    private lateinit var statusText: TextView
    private lateinit var priceText: TextView
    private lateinit var tenantsFirstNameText: TextView
    private lateinit var tenantsLastNameText: TextView
    private lateinit var tenantsContactNumberText: TextView
    private lateinit var tenantsConcernText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_landlord_notification_view_details_dashboard)
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

        currentReserveId = intent.getStringExtra("reserved_id") ?: ""

        reservedData = intent.getSerializableExtra("notificationData") as TenantsReservedDataStructureDB

        // Initialize UI elements
        LandlordlastNameText = findViewById(R.id.landlord_last_name)
        roomNumberText = findViewById(R.id.room_no)
        numberofBordersText = findViewById(R.id.number_of_borders)
        statusText = findViewById(R.id.status)
        priceText = findViewById(R.id.price)
        tenantsFirstNameText = findViewById(R.id.tenant_first_name)
        tenantsLastNameText = findViewById(R.id.tenant_last_name)
        tenantsContactNumberText = findViewById(R.id.contact_number)
        tenantsConcernText = findViewById(R.id.concern)

        populateFields()
    }

    private fun populateFields() {
        LandlordlastNameText.setText(reservedData.landlord_lastname)
        roomNumberText.setText(reservedData.room_number)
        numberofBordersText.setText(reservedData.number_of_borders)
        statusText.setText(reservedData.status)
        priceText.setText(reservedData.price)
        tenantsFirstNameText.setText(reservedData.tenants_first_name)
        tenantsLastNameText.setText(reservedData.tenants_last_name)
        tenantsContactNumberText.setText(reservedData.tenants_contact_number)
        tenantsConcernText.setText(reservedData.concern)

        reservedData.valid_docs_image_url?.let {
            val validDocsImageView = findViewById<ImageView>(R.id.validDocs_pic)
            Glide.with(this)
                .load(it)
                .into(validDocsImageView)
        }
        reservedData.g_cash_ss_image_url?.let {
            val gcashSSImageView = findViewById<ImageView>(R.id.gcashSS_pic)
            Glide.with(this)
                .load(it)
                .into(gcashSSImageView)
        }
    }
}