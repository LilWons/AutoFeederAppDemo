/*
    Author: Richard Hart
    Student Number: 991627469
    Project: AutoFeeder Application.
    Class: 1239_44805
    Course: ENGI39228 Capstone Project for CET/EET
    School: Sheridan College
    Instructor: Ning Zhu
    Date: 2023-12-11

    Description: AutoFeeder MainActivity for displaying PetFeeder Status and Bowl/Hopper Level.
    Allows user to manually feed the pet with the 'Feed Now' button.
    Provides Setting menu for AutoFeeder setup and account information.
 */

package com.example.autofeederapp

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.autofeederapp.databinding.ActivityMainBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private  lateinit var address: String
    private lateinit var binding: ActivityMainBinding
    private lateinit var database: DatabaseReference

    /*
    On Activity Start, check if there is already a user signed in.
    If no user is signed in, navigate to Login Activity.
     */
    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Utility.goto(this,Login::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //Loads AutoFeederID from device.
        address = Utility.loadDevice(this, "AutoFeederID").toString()
        Log.d(TAG, "Saved Info $address")

        feederSetup(address)
        setupListeners()
    }

    /*
    setupListeners: Sets up view listeners for MainActivity Activity.
     */
    private fun setupListeners() {
        val swipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.refreshLayout)
        val textView = findViewById<TextView>(R.id.tv1)
        //Triggered when swipeRefreshLayout is refreshed with down gesture.
        swipeRefreshLayout.setOnRefreshListener{
            readData(address)
            val buffer = "Refreshed"
            val text = getString(R.string.format_string,buffer)
            textView.text = text
            swipeRefreshLayout.isRefreshing = false
        }
        with(binding){
            btRefresh.setOnClickListener { readData(address) }
            btFeedHome.setOnClickListener { manualFeed() }
            btPetInfo.setOnClickListener{Utility.goto(this@MainActivity,PetInfo::class.java)}
            ivSettings.setOnClickListener { Utility.goto(this@MainActivity,Settings::class.java) }
            btPetHome.setOnClickListener { Utility.goto(this@MainActivity,Pet::class.java) }
        }
    }
    /*
    feederSetup: Creates a DatabaseReference with string than populates the Firebase with default values.
     */
    private fun feederSetup(string: String) {
        val disable = false
        val dub = 0.0
        val emptyString = ""
        val zero = 0

        if(string != "null") {
            database = FirebaseDatabase.getInstance().getReference(string)
            database.addListenerForSingleValueEvent(object : ValueEventListener {
                //Checks to make sure DatabaseReference does not already exist.
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        database = FirebaseDatabase.getInstance().getReference(string)
                        database.child("AutoFeederID").setValue(string)
                        database.child("feederFault").setValue(disable)
                        database.child("lvlBowl").setValue(zero)
                        database.child("lvlFood").setValue(zero)
                        database.child("manualFeed").setValue(disable)
                        database.child("timeZone").setValue(dub)

                        database.child("freeFeed").setValue(!disable)
                        database.child("perDay").setValue(zero)
                        database.child("perMeal").setValue(dub)
                        database.child("petName").setValue(emptyString)
                        database.child("timeMealFourHour").setValue(zero)
                        database.child("timeMealFourMin").setValue(zero)

                        database.child("timeMealOneHour").setValue(zero)
                        database.child("timeMealOneMin").setValue(zero)
                        database.child("timeMealThreeHour").setValue(zero)
                        database.child("timeMealThreeMin").setValue(zero)
                        database.child("timeMealTwoHour").setValue(zero)
                        database.child("timeMealTwoMin").setValue(zero)
                        Log.i(TAG, "Device $string Setup success. Database Created.")
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    Log.i(TAG, "Device $string Setup failed.")
                }
            })
        }else{
            Log.i(TAG, "Device $string Setup success. Database exists.")
        }
    }
    /*
    readData: Reads values from Database under DatabaseReference 'string'.
    Updates view colours based on number value.
     */
    private fun readData(string: String) {
        database = FirebaseDatabase.getInstance().getReference(string)
        Utility.readInt(database,"lvlBowl"){ value ->
            with(binding){
            val lvlBowl: Int = value.toString().toInt()
            if(lvlBowl > 74){
                tvLvlBowl.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.GREEN))
            }
            else if(lvlBowl in 50..74){
                tvLvlBowl.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.DARK_GREEN))
            }
            else if(lvlBowl in 25..49){
                tvLvlBowl.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.ORANGE))
            }
            else{
                tvLvlBowl.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.RED))
            }
            tvLvlBowl.text = Utility.setText(this@MainActivity, "$lvlBowl%")
            }
        }

        Utility.readInt(database,"lvlFood"){ value ->
            with(binding) {
                val lvlFood: Int = value.toString().toInt()
                if (lvlFood > 74) {
                    tvLvlFood.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.GREEN))
                } else if (lvlFood in 50..74) {
                    tvLvlFood.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.DARK_GREEN))
                } else if (lvlFood in 25..49) {
                    tvLvlFood.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.ORANGE))
                } else {
                    tvLvlFood.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.RED))
                }
                tvLvlFood.text = Utility.setText(this@MainActivity, "$lvlFood%")
            }
        }

        Utility.readBool(database,"feederFault"){ value ->
            with(binding){
            val feederFault: Boolean = value.toString().toBoolean()
            if(feederFault) {
                tvFoodStatus.text = Utility.setText(this@MainActivity, "Inactive - Check AutoFeeder")
                tvFoodStatus.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.RED))
            }
            else {
            tvFoodStatus.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.GREEN))
            tvFoodStatus.text = Utility.setText(this@MainActivity, "Active")
            }
            }
        }
    }
    /*
    manualFeed: Sets "manualFeed" to true in at DatabaseReference 'address'.
     */
    private fun manualFeed() {
        val manualFeed = true
        database = FirebaseDatabase.getInstance().getReference(address)
        database.child("manualFeed").setValue(manualFeed)
    }
    /*
    onResume: When the Activity is resumed, call readData() with 'address' DatabaseReference.
     */
    public override fun onResume() {
        super.onResume()
        readData(address)
    }
}
