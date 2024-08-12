package com.example.boardingease

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class Signup : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
    private val passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&_-])[A-Za-z\\d@\$!%*?&_-]{6,}$"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signup)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val login_bttn = findViewById<TextView>(R.id.login_bttn)
        val firstname = findViewById<TextInputEditText>(R.id.first_name)
        val lastname = findViewById<TextInputEditText>(R.id.last_name)
        val mobilenumber = findViewById<TextInputEditText>(R.id.mobile_number)
        val em = findViewById<TextInputEditText>(R.id.email)
        val pass = findViewById<TextInputEditText>(R.id.password)
        val confirmpass = findViewById<TextInputEditText>(R.id.confirm_password)
        val signup_bttn = findViewById<AppCompatButton>(R.id.signup_bttn)
        val signUpProgressBar : ProgressBar = findViewById(R.id.signUpProgressBar)
        val radioGroup = findViewById<RadioGroup>(R.id.radio_group)

        // Set default value for gender selection
        val defaultGender = "Gender"

        signup_bttn.setOnClickListener {
            val first_name = firstname.text.toString()
            val last_name = lastname.text.toString()
            val mobile_number = mobilenumber.text.toString()
            val email = em.text.toString()
            val password = pass.text.toString()
            val confirm_password = confirmpass.text.toString()
            val selectedUserTypeId = radioGroup.checkedRadioButtonId

            signUpProgressBar.visibility = View.VISIBLE

            if(first_name.isEmpty() || last_name.isEmpty() || mobile_number.isEmpty() || email.isEmpty() || password.isEmpty() || confirm_password.isEmpty() || selectedUserTypeId == -1){
                if (first_name.isEmpty()){
                    firstname.error = "First Name is Required!"
                }
                if (last_name.isEmpty()){
                    lastname.error = "Last Name is Required!"
                }
                if (mobile_number.isEmpty()){
                    mobilenumber.error = "Mobile Number is Required!"
                }
                if (email.isEmpty()){
                    em.error = "Email is Required!"
                }
                if (password.isEmpty()){
                    pass.error = "Password is Required!"
                }
                if (confirm_password.isEmpty()){
                    confirmpass.error = "Confirm Password is Required!"
                }
                if (selectedUserTypeId == -1) {
                    Toast.makeText(this, "User Type is Required!", Toast.LENGTH_SHORT).show()
                }
                Toast.makeText(this,"All fields are Required!", Toast.LENGTH_SHORT).show()
                signUpProgressBar.visibility = View.GONE
            } else if (!email.matches(emailPattern.toRegex())){
                em.error = "Invalid Email Address!"
                Toast.makeText(this,"Invalid Email Address!", Toast.LENGTH_SHORT).show()
                signUpProgressBar.visibility = View.GONE
            } else if (!password.matches(passwordPattern.toRegex())) {
                pass.error = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character and have a 6 characters long!"
                Toast.makeText(this, "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character and have a 6 characters long!", Toast.LENGTH_LONG).show()
                signUpProgressBar.visibility = View.GONE
            } else if (password != confirm_password){
                confirmpass.error = "Password does not match!"
                Toast.makeText(this,"Password does not match!", Toast.LENGTH_SHORT).show()
                signUpProgressBar.visibility = View.GONE
            } else {
                val selectedUserType = findViewById<RadioButton>(selectedUserTypeId).text.toString()
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        val user = auth.currentUser
                        user?.sendEmailVerification()
                            ?.addOnSuccessListener {
                                Toast.makeText(this, "Verification email sent. Please check your email to verify your account.", Toast.LENGTH_SHORT).show()
                            }
                            ?.addOnFailureListener { e ->
                                Toast.makeText(this, "Failed to send verification email: ${e.message}", Toast.LENGTH_SHORT).show()
                            }

                        val databaseRef = database.reference.child("UsersTbl").child(auth.currentUser!!.uid)
                        val users: UsersStructureDB = UsersStructureDB(email, first_name, last_name, defaultGender, mobile_number, password, selectedUserType, auth.currentUser!!.uid)

                        databaseRef.setValue(users).addOnCompleteListener { dbTask ->
                            if (dbTask.isSuccessful) {
                                signUpProgressBar.visibility = View.GONE
                                val intent = Intent(this, Login::class.java)
                                startActivity(intent)
                                Toast.makeText(this, "Sign Up Successfully", Toast.LENGTH_SHORT).show()
                            } else {
                                signUpProgressBar.visibility = View.GONE
                                Log.d("Firebase", "Database write failed: ${dbTask.exception}")
                                Toast.makeText(this, "Sign Up Failed!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        signUpProgressBar.visibility = View.GONE
                        Log.d("Firebase", "User creation failed: ${task.exception}")
                        Toast.makeText(this, "Do you have internet Connection? or Do you have already account?, try again.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        login_bttn.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }
    }
}