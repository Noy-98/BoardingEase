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
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream

class LandlordAddingPostDashboard : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var currentUsersId: String
    private lateinit var bid: String
    private lateinit var ProgressBar: ProgressBar
    private val PICK_IMAGE_REQUEST = 1
    private lateinit var selectedImageUri: Uri
    private lateinit var storageReference: StorageReference
    // New variables for permit image
    private val PICK_PERMIT_REQUEST = 2
    private lateinit var selectedPermitUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_landlord_adding_post_dashboard)
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
            } else if (item.itemId == R.id.confirmation) {
                startActivity(Intent(applicationContext, LandlordNotificationConfirmationDashboard::class.java))
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

        FirebaseApp.initializeApp(this)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val currentUser = auth.currentUser
        currentUsersId = currentUser?.uid ?: ""

        val newBoardingRef = FirebaseDatabase.getInstance().reference.child("BoardingsTbl").push()
        bid = newBoardingRef.key ?: ""

        val lastname = findViewById<TextInputEditText>(R.id.last_name)
        val roomNum = findViewById<TextInputEditText>(R.id.room_no)
        val numBorders = findViewById<TextInputEditText>(R.id.num_borders)
        val stat = findViewById<TextInputEditText>(R.id.status)
        val pricee = findViewById<TextInputEditText>(R.id.price)
        val contactNum = findViewById<TextInputEditText>(R.id.contact_no)
        val add = findViewById<TextInputEditText>(R.id.address)
        val gcash_n = findViewById<TextInputEditText>(R.id.gcash_name)
        val gcash_num = findViewById<TextInputEditText>(R.id.gcash_number)
        val rules = findViewById<TextInputEditText>(R.id.rules_and_regulations)
        val submitButton = findViewById<AppCompatButton>(R.id.submit_bttn)
        val Add_Picture = findViewById<AppCompatButton>(R.id.upload_bttn)
        val upload_permit_bttn = findViewById<AppCompatButton>(R.id.permit_bttn)
        ProgressBar = findViewById(R.id.progressBar)

        storageReference = FirebaseStorage.getInstance().reference

        Add_Picture.setOnClickListener {
            openImageChooser()
        }

        upload_permit_bttn.setOnClickListener {
            openPermitImageChooser()
        }

        submitButton.setOnClickListener {
            val last_name = lastname.text.toString().trim()
            val room_Num = roomNum.text.toString().trim()
            val num_Borders = numBorders.text.toString().trim()
            val status = stat.text.toString().trim()
            val price_tag = pricee.text.toString().trim()
            val contact_Num = contactNum.text.toString().trim()
            val adddress = add.text.toString().trim()
            val rules_reg = rules.text.toString().trim()
            val g_cash_n = gcash_n.text.toString().trim()
            val g_cash_num = gcash_num.text.toString().trim()

            ProgressBar.visibility = View.VISIBLE

            if (last_name.isEmpty() || room_Num.isEmpty() || num_Borders.isEmpty() || status.isEmpty() || price_tag.isEmpty() || contact_Num.isEmpty() || adddress.isEmpty() || rules_reg.isEmpty() || g_cash_n.isEmpty() || g_cash_num.isEmpty()) {
                if (last_name.isEmpty()) {
                    lastname.error = "Please enter your last name"
                }
                if (room_Num.isEmpty()) {
                    roomNum.error = "Please enter your room number"
                }
                if (num_Borders.isEmpty()) {
                    numBorders.error = "Please enter your number of borders"
                }
                if (status.isEmpty()) {
                    stat.error = "Please enter your status"
                }
                if (price_tag.isEmpty()) {
                    pricee.error = "Please enter your price tag"
                }
                if (contact_Num.isEmpty()) {
                    contactNum.error = "Please enter your contact number"
                }
                if (adddress.isEmpty()) {
                    add.error = "Please enter your address"
                }
                if (g_cash_n.isEmpty()) {
                    gcash_n.error = "Please enter your gcash name"
                }
                if (g_cash_num.isEmpty()) {
                    gcash_num.error = "Please enter your gcash number"
                }
                if (rules_reg.isEmpty()) {
                    rules.error = "Please enter your rules and regulations"
                }
                Toast.makeText(this, "All fields are Required!", Toast.LENGTH_SHORT).show()
                ProgressBar.visibility = View.GONE
            } else {

                val boardingData =BoardingDataStructureDB(
                    b_id = bid,
                    landlord_lastname = last_name,
                    room_number = room_Num,
                    number_of_borders = num_Borders,
                    status = status,
                    price = price_tag,
                    contact_number = contact_Num,
                    address = adddress,
                    g_cash_name = g_cash_n,
                    g_cash_number = g_cash_num,
                    rules_and_regulations = rules_reg
                )
                uploadImage(boardingData)
            }
        }
    }

    private fun openPermitImageChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, PICK_PERMIT_REQUEST)
    }

    private fun uploadImage(boardingData: BoardingDataStructureDB) {
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
                boardingData.unitPictureUrl = downloadUrl
                if (::selectedPermitUri.isInitialized) { // Only proceed if permit image is selected
                    uploadPermitImage(boardingData)
                } else {
                    saveBoardingData(boardingData) // Save directly if no permit image is selected
                }

            } else {
                Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                ProgressBar.visibility = View.GONE
            }
        }
    }

    private fun uploadPermitImage(boardingData: BoardingDataStructureDB) {
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
                boardingData.permitImageUrl = permitDownloadUrl
                saveBoardingData(boardingData)
            } else {
                Toast.makeText(this, "Failed to upload permit image", Toast.LENGTH_SHORT).show()
                ProgressBar.visibility = View.GONE
            }
        }
    }

    private fun saveBoardingData(boardingData: BoardingDataStructureDB) {

        val updates = hashMapOf<String, Any>(
            "/UsersTbl/$currentUsersId/BoardingsTbl/$bid" to boardingData,
            "/BoardingsTbl/$bid" to boardingData
        )

        database.reference.updateChildren(updates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Boarding Post Upload successfully", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LandlordHomeDashboard::class.java))
            } else {
                Toast.makeText(this, "Failed to Upload Boarding Post", Toast.LENGTH_SHORT).show()
            }
            ProgressBar.visibility = View.GONE
        }
    }

    private fun openImageChooser() {
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