package com.example.boardingease

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
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
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream

class TenantsPaymentDashboard : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storageReference: StorageReference

    private lateinit var boardingData: BoardingDataStructureDB
    private lateinit var currentBId: String
    private lateinit var currentUsersId: String
    private lateinit var rid: String

    private lateinit var lastNameText: TextView
    private lateinit var roomNoText: TextView
    private lateinit var numBordersText: TextView
    private lateinit var statusText: TextView
    private lateinit var priceText: TextView
    private lateinit var gcashNameText: TextView
    private lateinit var gcashNumberText: TextView

    private lateinit var landlordLNameEditText: TextInputEditText
    private lateinit var roomNoEditText: TextInputEditText
    private lateinit var numBordersEditText: TextInputEditText
    private lateinit var statusEditText: TextInputEditText
    private lateinit var priceEditText: TextInputEditText
    private lateinit var TenantsFNameEditText: TextInputEditText
    private lateinit var TenantsLNameEditText: TextInputEditText
    private lateinit var TenantsCnumEditText: TextInputEditText
    private lateinit var concernEditText: TextInputEditText

    private lateinit var ProgressBar: ProgressBar

    private val PICK_VALID_DOCS_REQUEST = 1
    private val PICK_GCASH_SS_REQUEST = 2
    private lateinit var selectedImageUri: Uri
    private lateinit var selectedImageUri2: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tenants_payment_dashboard)
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

        FirebaseApp.initializeApp(this)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storageReference = FirebaseStorage.getInstance().reference

        val currentUser = auth.currentUser
        currentUsersId = currentUser?.uid ?: ""

        currentBId = intent.getStringExtra("b_id") ?: ""

        boardingData = intent.getSerializableExtra("boardingData") as BoardingDataStructureDB

        val newReservedRef = FirebaseDatabase.getInstance().reference.child("ReservedTenantsTbl").push()
        rid = newReservedRef.key ?: ""

        // Initialize UI elements
        lastNameText = findViewById(R.id.last_name)
        roomNoText = findViewById(R.id.room_no)
        numBordersText = findViewById(R.id.number_of_borders)
        statusText = findViewById(R.id.status)
        priceText = findViewById(R.id.price)
        gcashNameText = findViewById(R.id.gcash_name)
        gcashNumberText = findViewById(R.id.gcash_number)

        landlordLNameEditText = findViewById(R.id.landlord_last_name)
        roomNoEditText = findViewById(R.id.room_num)
        numBordersEditText = findViewById(R.id.num_borders)
        statusEditText = findViewById(R.id.stat)
        priceEditText = findViewById(R.id.price_num)
        TenantsFNameEditText = findViewById(R.id.tenant_first_name)
        TenantsLNameEditText = findViewById(R.id.tenant_last_name)
        TenantsCnumEditText = findViewById(R.id.tenant_contact_num)
        concernEditText = findViewById(R.id.concern)

        ProgressBar = findViewById(R.id.progressBar)

        val upload_validDocs_bttn = findViewById<AppCompatButton>(R.id.validDocs_bttn)
        upload_validDocs_bttn.setOnClickListener {
            openValidDocsImagePicker()
        }

        val upload_gcash_ss_bttn = findViewById<AppCompatButton>(R.id.gcash_ss_bttn)
        upload_gcash_ss_bttn.setOnClickListener {
            openGcashSSImagePicker()
        }

        val submitBttn = findViewById<AppCompatButton>(R.id.submit_bttn)
        submitBttn.setOnClickListener {
            val landlord_lName = landlordLNameEditText.text.toString().trim()
            val roomNo = roomNoEditText.text.toString().trim()
            val numBorders = numBordersEditText.text.toString().trim()
            val status = statusEditText.text.toString().trim()
            val price = priceEditText.text.toString().trim()
            val tenantFName = TenantsFNameEditText.text.toString().trim()
            val tenantLName = TenantsLNameEditText.text.toString().trim()
            val tenantCnum = TenantsCnumEditText.text.toString().trim()
            val concern = concernEditText.text.toString().trim()

            ProgressBar.visibility = View.VISIBLE

            if (landlord_lName.isEmpty() || roomNo.isEmpty() || numBorders.isEmpty() || status.isEmpty() || price.isEmpty() || tenantFName.isEmpty() || tenantLName.isEmpty() || tenantCnum.isEmpty() || concern.isEmpty()){
                if (landlord_lName.isEmpty()){
                    landlordLNameEditText.error = "Please enter your last name"
                }
                if (roomNo.isEmpty()){
                    roomNoEditText.error = "Please enter your room number"
                }
                if (numBorders.isEmpty()){
                    numBordersEditText.error = "Please enter the number of borders"
                }
                if (status.isEmpty()){
                    statusEditText.error = "Please enter the status"
                }
                if (price.isEmpty()) {
                    priceEditText.error = "Please enter the price"
                }
                if (tenantFName.isEmpty()){
                    TenantsFNameEditText.error = "Please enter your first name"
                }
                if (tenantLName.isEmpty()) {
                    TenantsLNameEditText.error = "Please enter your last name"
                }
                if (tenantCnum.isEmpty()) {
                    TenantsCnumEditText.error = "Please enter your contact number"
                }
                if (concern.isEmpty()) {
                    concernEditText.error = "Please enter your concern"
                }
                Toast.makeText(this, "All fields are Required!", Toast.LENGTH_SHORT).show()
                ProgressBar.visibility = View.GONE
            } else {
                val tenantsReservedData = TenantsReservedDataStructureDB(
                    reserved_id = rid,
                    landlord_lastname = landlord_lName,
                    room_number = roomNo,
                    number_of_borders = numBorders,
                    status = status,
                    price = price,
                    tenants_first_name = tenantFName,
                    tenants_last_name = tenantLName,
                    tenants_contact_number = tenantCnum,
                    concern = concern
                )
                uploadImage(tenantsReservedData)
            }
        }

        populateFields()
    }

    private fun uploadImage(tenantsReservedData: TenantsReservedDataStructureDB) {
        val imageRef = storageReference.child("ValidDicsImages/${System.currentTimeMillis()}.jpg")
        val imageView = findViewById<ImageView>(R.id.validDocs_pic)
        imageView.isDrawingCacheEnabled = true
        imageView.buildDrawingCache()
        val bitmap = (imageView.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageData = baos.toByteArray()

        val uploadTask = imageRef.putBytes(imageData)
        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            imageRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUrl = task.result.toString()
                tenantsReservedData.valid_docs_image_url = downloadUrl
                if (::selectedImageUri2.isInitialized) { // Only proceed if permit image is selected
                    uploadGcashSSImage(tenantsReservedData)
                } else {
                    savetenantsReservedData(tenantsReservedData) // Save directly if no permit image is selected
                }
            } else {
                Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                ProgressBar.visibility = View.GONE
            }
        }
    }

    private fun uploadGcashSSImage(tenantsReservedData: TenantsReservedDataStructureDB) {
        val permitImageRef = storageReference.child("GcashSSImages/${System.currentTimeMillis()}.jpg")
        val permitBitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri2)
        val baos = ByteArrayOutputStream()
        permitBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val permitData = baos.toByteArray()

        val uploadTask = permitImageRef.putBytes(permitData)
        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            permitImageRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val permitDownloadUrl = task.result.toString()
                tenantsReservedData.g_cash_ss_image_url = permitDownloadUrl
                savetenantsReservedData(tenantsReservedData)
            } else {
                Toast.makeText(this, "Failed to upload permit image", Toast.LENGTH_SHORT).show()
                ProgressBar.visibility = View.GONE
            }
        }
    }

    private fun savetenantsReservedData(tenantsReservedData: TenantsReservedDataStructureDB) {
        val updates = hashMapOf<String, Any>(
            "/UsersTbl/$currentUsersId/ReservedTenantsTbl/$rid" to tenantsReservedData,
            "/BoardingsTbl/$currentBId/ReservedTenantsTbl/$rid" to tenantsReservedData
        )

        database.reference.updateChildren(updates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Upload successfully", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, TenantsViewDetailsDashboard::class.java))
            } else {
                Toast.makeText(this, "Failed to Upload", Toast.LENGTH_SHORT).show()
            }
            ProgressBar.visibility = View.GONE
        }
    }

    private fun openGcashSSImagePicker() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, PICK_GCASH_SS_REQUEST)
    }

    private fun openValidDocsImagePicker() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, PICK_VALID_DOCS_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_VALID_DOCS_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data!!
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)

            val imageView = findViewById<ImageView>(R.id.validDocs_pic)
            imageView.setImageBitmap(bitmap)
            imageView.visibility = View.VISIBLE
        } else if (requestCode == PICK_GCASH_SS_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedImageUri2 = data.data!!
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri2)

            val permitImageView = findViewById<ImageView>(R.id.gcash_ss)
            permitImageView.setImageBitmap(bitmap)
            permitImageView.visibility = View.VISIBLE
        }
    }

    private fun populateFields() {
        lastNameText.setText(boardingData.landlord_lastname)
        roomNoText.setText(boardingData.room_number)
        numBordersText.setText(boardingData.number_of_borders)
        statusText.setText(boardingData.status)
        priceText.setText(boardingData.price)
        gcashNameText.setText(boardingData.g_cash_name)
        gcashNumberText.setText(boardingData.g_cash_number)
    }
}