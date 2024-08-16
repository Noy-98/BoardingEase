package com.example.boardingease

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
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
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class LandlordHomeDashboard : AppCompatActivity() {

    private lateinit var boardingAdapter: BoardingAdapter
    private lateinit var databaseReference: DatabaseReference
    private lateinit var boardingList: MutableList<BoardingDataStructureDB>
    private lateinit var storageReference: StorageReference
    private lateinit var searchBox: EditText
    private lateinit var noPostText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_landlord_home_dashboard)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNavigationView.selectedItemId = R.id.home
        bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
            if (item.itemId == R.id.home) {
                // Handle Home item
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

        // Initialize RecyclerView and Adapter
        val recyclerView: RecyclerView = findViewById(R.id.boardingList)
        boardingList = mutableListOf()
        boardingAdapter = BoardingAdapter(this, boardingList)
        recyclerView.adapter = boardingAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize Firebase database reference
        val currentUser = FirebaseAuth.getInstance().currentUser

        storageReference = FirebaseStorage.getInstance().reference

        val post_bttn = findViewById<ImageView>(R.id.add_post_bttn)
        searchBox = findViewById(R.id.search_box)
        noPostText = findViewById(R.id.no_post_text)

        post_bttn.setOnClickListener {
            val intent = Intent(this, LandlordAddingPostDashboard::class.java)
            startActivity(intent)
        }

        loadUserInfo()

        searchBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                filterBoarding(charSequence.toString())
            }

            override fun afterTextChanged(editable: Editable?) {
            }

        })

        if (currentUser != null) {
            val uid = currentUser.uid
            databaseReference = FirebaseDatabase.getInstance().getReference("UsersTbl/$uid/BoardingsTbl")

            // Retrieve boardings data from Firebase
            databaseReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        boardingList.clear()
                        for (boardingSnapshot in snapshot.children) {
                            val boarding = boardingSnapshot.getValue(BoardingDataStructureDB::class.java)
                            if (boarding != null) {
                                boardingList.add(boarding)
                            }
                        }
                        boardingAdapter.notifyDataSetChanged()

                        // Show/hide "No post Added!" text based on data availability
                        if (boardingList.isEmpty()) {
                            noPostText.visibility = View.VISIBLE
                        } else {
                            noPostText.visibility = View.GONE
                        }

                    } else {
                        noPostText.visibility = View.VISIBLE  // Show the text if no data exists
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
        }
    }

    private fun filterBoarding(query: String) {
        val filteredList = ArrayList<BoardingDataStructureDB>()

        for (boarding in boardingList) {
            val lastName = "${boarding.landlord_lastname?.orEmpty()} ${boarding.price?.orEmpty()}".toLowerCase()
            if (lastName.contains(query.toLowerCase())) {
                filteredList.add(boarding)
            }
        }

        boardingAdapter.filterList(filteredList)
    }

    private fun loadUserInfo() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            val userReference = FirebaseDatabase.getInstance().getReference("UsersTbl/$uid")

            // Retrieve doctor data from Firebase
            userReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        val users = snapshot.getValue(UsersStructureDB::class.java)
                        if (users != null) {
                            findViewById<TextView>(R.id.landlord_lastname).text = users.last_name
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        }
    }
}