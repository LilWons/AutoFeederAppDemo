/*
    Author: Richard Hart
    Student Number: 991627469
    Project: AutoFeeder Application.
    Class: 1239_44805
    Course: ENGI39228 Capstone Project for CET/EET
    School: Sheridan College
    Instructor: Ning Zhu
    Date: 2023-12-11

    Description: AutoFeeder Login Activity.
    Authenticates users with email and password before granting access to Firebase.
 */
package com.example.autofeederapp

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.autofeederapp.databinding.ActivityLoginBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

lateinit var auth: FirebaseAuth
var email: String = ""
var password: String = ""


class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    /*
    On Activity Start, check if there is already a user signed in.
    If user is signed in, navigate to MainActivity.
     */
    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Log.d(Pet.TAG, "currentUser:  $currentUser")
            Utility.goto(this,MainActivity::class.java)}
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth

        setupListeners()

    }
    /*
    setupListeners: Setup view listeners for Login Activity.
     */
    private fun setupListeners() {
        with(binding) {
            tvToRegister.setOnClickListener { Utility.goto(this@Login, Register::class.java) }
            btLogin.setOnClickListener {
                pbBar.visibility = View.VISIBLE
                //Gets Email and Password from user.
                email = etEmail.text?.toString() ?: ""
                password = etPassword.text?.toString() ?: ""
                //Check if email or password is empty.
                if (email.isEmpty()) {
                    Toast.makeText(this@Login, "Enter Email", Toast.LENGTH_SHORT).show()
                    pbBar.visibility = View.GONE
                } else if (password.isEmpty()) {
                    Toast.makeText(this@Login, "Enter Password", Toast.LENGTH_SHORT).show()
                    pbBar.visibility = View.GONE
                } else {
                    /*
                    Attempts to sign in with Email and Password.
                    If sign-in is successful, navigate to MainActivity.
                    If Sign-in is not successful, notify user to try again.
                     */
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this@Login) { task ->
                            if (task.isSuccessful) {
                                Log.d(TAG, "signInWithEmail:success")
                                val user = auth.currentUser
                                Log.d(Pet.TAG, "user:  $user")
                                if(user != null)
                                    Utility.goto(this@Login, MainActivity::class.java)
                            } else {
                                Log.w(TAG, "signInWithEmail:failure", task.exception)
                                // If sign in fails, display a message to the user.
                                Toast.makeText(
                                    baseContext,
                                    "Password/Email Incorrect. Please try again.",
                                    Toast.LENGTH_SHORT,
                                ).show()
                                pbBar.visibility = View.GONE
                            }
                        }
                }
            }
        }
    }
}