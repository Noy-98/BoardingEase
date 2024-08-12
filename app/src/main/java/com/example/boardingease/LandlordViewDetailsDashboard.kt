package com.example.boardingease

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class LandlordViewDetailsDashboard : AppCompatActivity() {

    private lateinit var imageUri: Uri
    private lateinit var storageReference: StorageReference
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var boardingData: BoardingDataStructureDB

    private lateinit var lastNameEditText: TextInputEditText
    private lateinit var roomNoEditText: TextInputEditText
    private lateinit var numBordersEditText: TextInputEditText
    private lateinit var statusEditText: TextInputEditText
    private lateinit var priceEditText: TextInputEditText
    private lateinit var contactNoEditText: TextInputEditText
    private lateinit var addressEditText: TextInputEditText

    private val getContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    imageUri = uri
                    val boardingImageView = findViewById<ImageView>(R.id.boarding_pic)
                    Glide.with(this)
                        .load(imageUri)
                        .into(boardingImageView)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_landlord_view_details_dashboard)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNavigationView.selectedItemId = R.id.home
        bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
            if (item.itemId == R.id.home) {
                startActivity(Intent(applicationContext, LandlordHomeDashboard::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                finish()
                return@setOnItemSelectedListener true
            } else if (item.itemId == R.id.chat) {
                startActivity(Intent(applicationContext, ChatLandlordDashboard::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                finish()
                return@setOnItemSelectedListener true
            } else if (item.itemId == R.id.gcash) {
                startActivity(Intent(applicationContext, LandlordPaymentDashboard::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                finish()
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
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference

        boardingData = intent.getParcelableExtra("boardingData") ?: BoardingDataStructureDB()

        // Initialize UI elements
        lastNameEditText = findViewById(R.id.last_name)
        roomNoEditText = findViewById(R.id.room_no)
        numBordersEditText = findViewById(R.id.num_borders)
        statusEditText = findViewById(R.id.status)
        priceEditText = findViewById(R.id.price)
        contactNoEditText = findViewById(R.id.contact_no)
        addressEditText = findViewById(R.id.address)

        val editImageButton = findViewById<AppCompatButton>(R.id.upload_bttn)

        editImageButton.setOnClickListener {
            openImagePicker()
        }

        val editProfileButton = findViewById<AppCompatButton>(R.id.edit)
        editProfileButton.setOnClickListener {
            updateProfile()
        }

        populateFields()
    }

    private fun populateFields() {
        lastNameEditText.setText(boardingData.landlord_lastname)
        roomNoEditText.setText(boardingData.room_number)
        numBordersEditText.setText(boardingData.number_of_borders)
        statusEditText.setText(boardingData.status)
        priceEditText.setText(boardingData.price)
        contactNoEditText.setText(boardingData.contact_number)
        addressEditText.setText(boardingData.address)

        boardingData.unitPictureUrl?.let {
            val boardingImageView = findViewById<ImageView>(R.id.boarding_pic)
            Glide.with(this)
                .load(it)
                .into(boardingImageView)
        }
    }

    private fun updateProfile() {
        val userId = auth.currentUser?.uid ?: return

        val lastName = lastNameEditText.text.toString().trim()
        val roomNo = roomNoEditText.text.toString().trim()
        val numBorders = numBordersEditText.text.toString().trim()
        val status = statusEditText.text.toString().trim()
        val price = priceEditText.text.toString().trim()
        val contactNo = contactNoEditText.text.toString().trim()
        val address = addressEditText.text.toString().trim()

        if (lastName.isEmpty() || roomNo.isEmpty() || numBorders.isEmpty() ||
            status.isEmpty() || price.isEmpty() || contactNo.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Use the current b_id from the boardingData object
        val currentBId = boardingData.b_id

        // Create a new instance of BoardingDataStructureDB with updated data
        val updatedBoardingData = BoardingDataStructureDB(
            b_id = currentBId, // Use the same b_id
            address = address,
            contact_number = contactNo,
            landlord_lastname = lastName,
            number_of_borders = numBorders,
            price = price,
            room_number = roomNo,
            status = status,
            unitPictureUrl = boardingData.unitPictureUrl // Retain original image URL if not being edited
        )

        // Update the data under both UsersTbl and BoardingsTbl
        val updates = hashMapOf<String, Any>(
            "/UsersTbl/$userId/BoardingsTbl/$currentBId" to updatedBoardingData,
            "/BoardingsTbl/$currentBId" to updatedBoardingData
        )

        databaseReference.updateChildren(updates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Data updated successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to update data: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                Log.e("UpdateProfile", "Error updating data", task.exception)
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        getContent.launch(intent)
    }
}
