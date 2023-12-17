/*
    Author: Richard Hart
    Student Number: 991627469
    Project: AutoFeeder Application.
    Class: 1239_44805
    Course: ENGI39228 Capstone Project for CET/EET
    School: Sheridan College
    Instructor: Ning Zhu
    Date: 2023-12-11

    Description: AutoFeeder Register Activity.
    Allows users to register for AutoFeeder Firebase with email and password.
 */
package com.example.autofeederapp

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.autofeederapp.databinding.ActivityRegisterBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class Register : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var email: String = ""
    private var password: String = ""
    private lateinit var binding: ActivityRegisterBinding
    /*
    On Activity Start, check if there is already a user signed in.
    If user is signed in, navigate to MainActivity.
    */
    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Utility.goto(this, MainActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        setupListeners()
    }

    /*
    setupListeners: Setup view listeners for Login Activity.
    */
    private fun setupListeners() {
        with(binding) {
            tvToLogin.setOnClickListener { Utility.goto(this@Register, Login::class.java) }

            btRegister.setOnClickListener {
                pbBar.visibility = View.VISIBLE
                //Gets Email and Password from user.
                email = etEmail.text?.toString() ?: ""
                password = etPassword.text?.toString() ?: ""
                //Check if email or password is empty.
                if (email.isEmpty()) {
                    Toast.makeText(this@Register, "Enter Email", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                } else {
                    email = etEmail.text.toString()
                }
                if (password.isEmpty()) {
                    Toast.makeText(this@Register, "Enter Password", Toast.LENGTH_SHORT).show()
                } else {
                    password = etPassword.text.toString()
                }
                /*
                Attempts to create a user with Email and Password.
                If user is created successfully, navigate to MainActivity.
                If user is not created successfully, notify user to try again.
                */
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this@Register) { task ->
                        binding.pbBar.visibility = View.GONE
                        if (task.isSuccessful) {
                            Log.d(TAG, "createUserWithEmail:success")
                            Utility.goto(this@Register, Login::class.java)

                        } else {
                            Log.w(TAG, "createUserWithEmail:failure", task.exception)
                            Toast.makeText(
                                baseContext,
                                "Authentication failed.",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    }
            }
        }
    }
}