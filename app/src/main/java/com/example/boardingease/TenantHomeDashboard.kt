package com.example.boardingease

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.EditText
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

class TenantHomeDashboard : AppCompatActivity() {

    private lateinit var boardingAdapter: BoardingAdapter2
    private lateinit var databaseReference: DatabaseReference
    private lateinit var boardingList: MutableList<BoardingDataStructureDB>
    private lateinit var searchBox: EditText
    private lateinit var noPostText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tenant_home_dashboard)
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
            } else if (item.itemId == R.id.chat) {
                startActivity(Intent(applicationContext, ChatTenantsDashboard::class.java))
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

        // Initialize RecyclerView and Adapter
        val recyclerView: RecyclerView = findViewById(R.id.boardingList)
        boardingList = mutableListOf()
        boardingAdapter = BoardingAdapter2(this, boardingList)
        recyclerView.adapter = boardingAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        searchBox = findViewById(R.id.search_box)
        noPostText = findViewById(R.id.no_post_text)

        loadUserInfo()
        loadBoardingData()

        searchBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                filterBoarding(charSequence.toString())
            }

            override fun afterTextChanged(editable: Editable) {

            }

        })


    }

    private fun loadBoardingData() {
        databaseReference = FirebaseDatabase.getInstance().getReference("BoardingsTbl")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                boardingList.clear()
                if (snapshot.exists()) {
                    for (b_idSnapshot in snapshot.children) {
                        val boarding = b_idSnapshot.getValue(BoardingDataStructureDB::class.java)
                        if (boarding != null) {
                            boardingList.add(boarding)
                        }
                    }
                    boardingAdapter.notifyDataSetChanged()
                }
                noPostText.visibility = if (boardingList.isEmpty()) View.VISIBLE else View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
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
                            findViewById<TextView>(R.id.tenant_lastname).text = users.last_name
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