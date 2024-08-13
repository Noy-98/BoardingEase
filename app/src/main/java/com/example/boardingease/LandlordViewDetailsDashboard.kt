package com.example.boardingease

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Tasks
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream

class LandlordViewDetailsDashboard : AppCompatActivity() {

    private lateinit var storageReference: StorageReference
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var boardingData: BoardingDataStructureDB
    private val PICK_IMAGE_REQUEST = 1
    private lateinit var currentUsersId: String
    private lateinit var currentBId: String
    private lateinit var ProgressBar: ProgressBar
    private lateinit var selectedImageUri: Uri
    // New variables for permit image
    private val PICK_PERMIT_REQUEST = 2
    private lateinit var selectedPermitUri: Uri

    private lateinit var lastNameEditText: TextInputEditText
    private lateinit var roomNoEditText: TextInputEditText
    private lateinit var numBordersEditText: TextInputEditText
    private lateinit var statusEditText: TextInputEditText
    private lateinit var priceEditText: TextInputEditText
    private lateinit var contactNoEditText: TextInputEditText
    private lateinit var addressEditText: TextInputEditText
    private lateinit var gcashName: TextInputEditText
    private lateinit var gcashNumber: TextInputEditText
    private lateinit var rulesEditText: TextInputEditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_landlord_view_details_dashboard)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        boardingData = intent.getSerializableExtra("boardingData") as BoardingDataStructureDB
        currentBId = intent.getStringExtra("b_id") ?: ""

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNavigationView.selectedItemId = R.id.home
        bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
            if (item.itemId == R.id.home) {
                startActivity(Intent(applicationContext, LandlordHomeDashboard::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                finish()
                return@setOnItemSelectedListener true
            } else if (item.itemId == R.id.notification) {
                val intent = Intent(this, LandlordNotificationDashboard::class.java)
                intent.putExtra("b_id", boardingData.b_id)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
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
        ProgressBar = findViewById(R.id.progressBar)
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference
        storageReference = FirebaseStorage.getInstance().reference

        val currentUser = auth.currentUser
        currentUsersId = currentUser?.uid ?: ""

        // Initialize UI elements
        lastNameEditText = findViewById(R.id.last_name)
        roomNoEditText = findViewById(R.id.room_no)
        numBordersEditText = findViewById(R.id.num_borders)
        statusEditText = findViewById(R.id.status)
        priceEditText = findViewById(R.id.price)
        contactNoEditText = findViewById(R.id.contact_no)
        addressEditText = findViewById(R.id.address)
        gcashName = findViewById(R.id.gcash_name)
        gcashNumber = findViewById(R.id.gcash_number)
        rulesEditText = findViewById(R.id.rules_and_regulations)

        val deleteButton = findViewById<AppCompatButton>(R.id.delete_bttn)
        deleteButton.setOnClickListener {
            deleteBoardingData()
        }

        val editImageButton = findViewById<AppCompatButton>(R.id.upload_bttn)
        editImageButton.setOnClickListener {
            openImagePicker()
        }

        val upload_permit_bttn = findViewById<AppCompatButton>(R.id.permit_bttn)
        upload_permit_bttn.setOnClickListener {
            openPermitImageChooser()
        }

        val editProfileButton = findViewById<AppCompatButton>(R.id.edit)
        editProfileButton.setOnClickListener {
            val lastName = lastNameEditText.text.toString().trim()
            val roomNo = roomNoEditText.text.toString().trim()
            val numBorders = numBordersEditText.text.toString().trim()
            val status = statusEditText.text.toString().trim()
            val price = priceEditText.text.toString().trim()
            val contactNo = contactNoEditText.text.toString().trim()
            val address = addressEditText.text.toString().trim()
            val g_cash_n = gcashName.text.toString().trim()
            val g_cash_num = gcashNumber.text.toString().trim()
            val rules = rulesEditText.text.toString().trim()

            ProgressBar.visibility = View.VISIBLE

            if (lastName.isEmpty() || roomNo.isEmpty() || numBorders.isEmpty() || status.isEmpty() || price.isEmpty() || contactNo.isEmpty() || address.isEmpty() || rules.isEmpty() || g_cash_n.isEmpty() || g_cash_num.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                ProgressBar.visibility = View.GONE
            } else {
                // Create a new instance of BoardingDataStructureDB with updated data
                val updatedBoardingData = BoardingDataStructureDB(
                    b_id = currentBId,
                    address = address,
                    contact_number = contactNo,
                    landlord_lastname = lastName,
                    number_of_borders = numBorders,
                    price = price,
                    room_number = roomNo,
                    status = status,
                    g_cash_name = g_cash_n,
                    g_cash_number = g_cash_num,
                    rules_and_regulations = rules
                )
                uploadImage(updatedBoardingData)
            }
        }

        populateFields()
    }

    private fun openPermitImageChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, PICK_PERMIT_REQUEST)
    }

    private fun deleteBoardingData() {

        // References to both paths
        val userBoardingRef = FirebaseDatabase.getInstance().getReference("UsersTbl").child(currentUsersId).child("BoardingsTbl").child(currentBId)
        val globalBoardingRef = FirebaseDatabase.getInstance().getReference("BoardingsTbl").child(currentBId)

        // Create deletion tasks
        val userDeleteTask = userBoardingRef.removeValue()
        val globalDeleteTask = globalBoardingRef.removeValue()

        // Run both deletion tasks in parallel and wait for them to finish
        Tasks.whenAll(userDeleteTask, globalDeleteTask).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // If both deletions were successful, navigate back to the dashboard
                val intent = Intent(this, LandlordHomeDashboard::class.java)
                startActivity(intent)
                Toast.makeText(this, "Boarding data deleted successfully", Toast.LENGTH_SHORT).show()
            } else {
                // Handle the failure of either task
                Toast.makeText(this, "Failed to delete boarding data: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                Log.e("DeleteBoardingData", "Error deleting data", task.exception)
            }
            ProgressBar.visibility = View.GONE
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
        gcashName.setText(boardingData.g_cash_name)
        gcashNumber.setText(boardingData.g_cash_number)
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

    private fun uploadImage(updatedBoardingData: BoardingDataStructureDB) {
        val imageRef = storageReference.child("images/${System.currentTimeMillis()}.jpg")
        val imageView = findViewById<ImageView>(R.id.boarding_pic)
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
                updatedBoardingData.unitPictureUrl = downloadUrl
                if (::selectedPermitUri.isInitialized) { // Only proceed if permit image is selected
                    uploadPermitImage(updatedBoardingData)
                } else {
                    saveBoardingData(updatedBoardingData) // Save directly if no permit image is selected
                }
            } else {
                Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                ProgressBar.visibility = View.GONE
            }
        }
    }

    private fun uploadPermitImage(updatedBoardingData: BoardingDataStructureDB) {
        val permitImageRef = storageReference.child("permits/${System.currentTimeMillis()}.jpg")
        val permitBitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPermitUri)
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
                updatedBoardingData.permitImageUrl = permitDownloadUrl
                saveBoardingData(updatedBoardingData)
            } else {
                Toast.makeText(this, "Failed to upload permit image", Toast.LENGTH_SHORT).show()
                ProgressBar.visibility = View.GONE
            }
        }
    }

    private fun saveBoardingData(updatedBoardingData: BoardingDataStructureDB) {
        // Update the data under both UsersTbl and BoardingsTbl
        val updates = hashMapOf<String, Any>(
            "/UsersTbl/$currentUsersId/BoardingsTbl/$currentBId" to updatedBoardingData,
            "/BoardingsTbl/$currentBId" to updatedBoardingData
        )

        databaseReference.updateChildren(updates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val intent = Intent(this, LandlordHomeDashboard::class.java)
                startActivity(intent)
                Toast.makeText(this, "Boarding Post successfully Changed!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to update data: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                Log.e("UpdateProfile", "Error updating Boarding Post", task.exception)
            }
            ProgressBar.visibility = View.GONE
        }
    }

    private fun openImagePicker() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data!!
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)

            val imageView = findViewById<ImageView>(R.id.boarding_pic)
            imageView.setImageBitmap(bitmap)
            imageView.visibility = View.VISIBLE
        } else if (requestCode == PICK_PERMIT_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedPermitUri = data.data!!
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPermitUri)

            val permitImageView = findViewById<ImageView>(R.id.permit_pic)
            permitImageView.setImageBitmap(bitmap)
            permitImageView.visibility = View.VISIBLE
        }
    }
}
