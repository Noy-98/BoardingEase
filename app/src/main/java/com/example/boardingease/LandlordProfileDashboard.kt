package com.example.boardingease

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.UploadTask

class LandlordProfileDashboard : AppCompatActivity() {

    private lateinit var databaseReference: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private val passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&_-])[A-Za-z\\d@\$!%*?&_-]{6,}$"
    private val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_landlord_profile_dashboard)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNavigationView.selectedItemId = R.id.profile
        bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
            if (item.itemId == R.id.home) {
                startActivity(Intent(applicationContext, LandlordHomeDashboard::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                finish()
                return@setOnItemSelectedListener true
            } else if (item.itemId == R.id.notification) {
                startActivity(Intent(applicationContext, LandlordNotificationDashboard::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                finish()
                return@setOnItemSelectedListener true
            } else if (item.itemId == R.id.profile) {
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

        databaseReference = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()

        val editProfileButton = findViewById<AppCompatButton>(R.id.change_profile_bttn)
        editProfileButton.setOnClickListener {
            updateProfile()
        }

        loadUsersProfile()
    }

    private fun loadUsersProfile() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null){
            val uid = currentUser.uid
            val usersReference = FirebaseDatabase.getInstance().getReference("UsersTbl/$uid")

            usersReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val users = snapshot.getValue(UsersStructureDB::class.java)
                        if (users != null) {
                            // Set the text views with the doctor's information
                            findViewById<TextView>(R.id.first_name).text = users.first_name
                            findViewById<TextView>(R.id.last_name).text = users.last_name
                            findViewById<TextView>(R.id.gender).text = users.gender
                            findViewById<TextView>(R.id.mobile_number).text = users.mobile_num
                            findViewById<TextView>(R.id.email).text = users.email
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@LandlordProfileDashboard, "Failed to load profile", Toast.LENGTH_SHORT).show()
                }

            })
        }
    }

    private fun updateProfile() {
        val firstName = findViewById<TextInputEditText>(R.id.first_name).text.toString().trim()
        val lastName = findViewById<TextInputEditText>(R.id.last_name).text.toString().trim()
        val gen = findViewById<TextInputEditText>(R.id.gender).text.toString().trim()
        val mobileNum = findViewById<TextInputEditText>(R.id.mobile_number).text.toString().trim()
        val em = findViewById<TextInputEditText>(R.id.email).text.toString().trim()
        val password = findViewById<TextInputEditText>(R.id.password).text.toString().trim()
        val confirmPassword = findViewById<TextInputEditText>(R.id.confirm_password).text.toString().trim()

        if (firstName.isEmpty() || lastName.isEmpty() || gen.isEmpty() || mobileNum.isEmpty() || em.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "All fields are required to fill in", Toast.LENGTH_SHORT).show()
            return
        }

        if (!em.matches(emailPattern.toRegex())) {
            Toast.makeText(this, "Invalid Email Address!", Toast.LENGTH_SHORT).show()
            return
        }

        if (!password.matches(passwordPattern.toRegex())) {
            Toast.makeText(this, "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character!", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            val usersReference = databaseReference.getReference("UsersTbl/$uid")

            usersReference.child("first_name").setValue(firstName)
            usersReference.child("last_name").setValue(lastName)
            usersReference.child("gender").setValue(gen)
            usersReference.child("mobile_num").setValue(mobileNum)

            // Re-authenticate user before updating email
            val credential = EmailAuthProvider.getCredential(currentUser.email!!, password)
            currentUser.reauthenticate(credential)
                .addOnCompleteListener { reauthTask ->
                    if (reauthTask.isSuccessful) {
                        currentUser.updateEmail(em)
                            .addOnCompleteListener { updateEmailTask ->
                                if (updateEmailTask.isSuccessful) {
                                    usersReference.child("email").setValue(em)
                                    Toast.makeText(this, "Email updated successfully", Toast.LENGTH_SHORT).show()

                                    if (password == confirmPassword) {
                                        currentUser.updatePassword(password)
                                            .addOnCompleteListener { updatePasswordTask ->
                                                if (updatePasswordTask.isSuccessful) {
                                                    usersReference.child("password").setValue(password)
                                                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(this, "Failed to update password: ${updatePasswordTask.exception?.message}", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                    } else {
                                        Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(this, "Failed to update email: ${updateEmailTask.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        Toast.makeText(this, "Re-authentication failed: ${reauthTask.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}