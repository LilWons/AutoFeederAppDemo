/*
    Author: LilWons
    Project: AutoFeeder Application.
    Date: 2023-12-11

    Description: AutoFeeder Account Activity.
    Displays current AutoFeeder MAC Address
    Allows user to sign out of AutoFeeder account.
 */
package com.example.autofeederapp

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.autofeederapp.databinding.ActivityAccountBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class Account : AppCompatActivity() {

    private lateinit var binding: ActivityAccountBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val address = Utility.loadDevice(this, "AutoFeederID").toString() //Loads AutoFeederID from device.

        if (address.isNotEmpty()) {
            binding.tvPetFeederID.text = address
            Log.d(TAG, "Address: $address")
        }
        setupListeners()
    }
    /*
    setupListeners: Sets up view listeners for Account Activity.
     */
    private fun setupListeners(){
        with(binding){
            btSetup.setOnClickListener {Utility.goto(this@Account,Settings::class.java) }
            btHomePet.setOnClickListener {
                val currentUser = auth.currentUser //Currently signed in Firebase User.
                if (currentUser != null) {
                    Utility.goto(this@Account,MainActivity::class.java)
                }else{
                    Utility.goto(this@Account,Login::class.java) //If there is no user logged in, go to Login Activity.
                }
            }
            btSignOut.setOnClickListener {
                Firebase.auth.signOut() //Signs current user out of Firebase.
                Utility.goto(this@Account,Login::class.java)
            }
        }
    }

}