/*
    Author: Richard Hart
    Student Number: 991627469
    Project: AutoFeeder Application.
    Class: 1239_44805
    Course: ENGI39228 Capstone Project for CET/EET
    School: Sheridan College
    Instructor: Ning Zhu
    Date: 2023-12-11

    Description: AutoFeeder PetInfo Activity.
    Displays current pet feeding profile as set in Firebase for AutoFeeder MAC address.
 */

package com.example.autofeederapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import com.example.autofeederapp.databinding.ActivityPetInfoBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class PetInfo : AppCompatActivity() {

    private lateinit var binding: ActivityPetInfoBinding
    private lateinit var database: DatabaseReference
    private lateinit var address: String

    private var timeMeal = IntArray(8)
    private var freeFeed: Boolean = false
    private var perDay: Int = 0
    private var perMeal: Double = 0.0
    private var petName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPetInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        address = Utility.loadDevice(this, "AutoFeederID").toString() // Loads AutoFeederID from device. If null, set to empty string.
        database = FirebaseDatabase.getInstance().getReference(address) //Gets DatabaseReference 'address'.

        setupListeners()
        readData(address)
        /*
        Execute code after 300 ms.
        As Firebase is asynchronous, allows buffer period after reading data.
         */
        Handler(Looper.getMainLooper()).postDelayed({
            configureData()
            showHide()
            displayInfo()
        },300)
    }

    /*
    onResume: Calls functions responsible for reading and displaying data from Database when Activity is resumed.
     */
    public override fun onResume() {
        super.onResume()
        readData(address)
        configureData()
        showHide()
        displayInfo()
    }
    /*
    setupListeners: Sets up view listeners for PetInfo Activity.
     */
    private fun setupListeners() {
            binding.btHomePet.setOnClickListener { Utility.goto(this, MainActivity::class.java) }
            binding.btPet.setOnClickListener { Utility.goto(this, Pet::class.java) }
    }
    /*
    displayInfo: Displays pet info and feeding profile currently in Database.
    Converts hour and minutes back into String.
     */
    private fun displayInfo() {
        with(binding) {
            tvPetName.text = petName
            if (freeFeed) {
                tvFreeFeed.text = Utility.setText(this@PetInfo,"Enabled")
            } else {
                tvFreeFeed.text = Utility.setText(this@PetInfo,"Disabled")
                tvFoodAmount.text = Utility.setText(this@PetInfo, "$perMeal cups")
                tvMealOne.text = getTime(timeMeal[4], timeMeal[0])
                tvMealTwo.text = getTime(timeMeal[5], timeMeal[1])
                tvMealThree.text = getTime(timeMeal[6], timeMeal[2])
                tvMealFour.text = getTime(timeMeal[7], timeMeal[3])
            }
        }
    }

    /*
    configureData: Loads meal hour and minute data from device and assigns to corresponding 'timeMeal' element index.
    Reads pets name and assigns to value petName.
    Reads 'freeFeed' from device and assigns value to freeFeed.
     */
    private fun configureData(){
        var buffer =  Utility.loadDevice(this, "timeMealOneMin").toString()
        Log.d("Debug", "Retrieved value: $buffer")
        timeMeal[0] = buffer.toInt()
        buffer =  Utility.loadDevice(this, "timeMealTwoMin").toString()
        Log.d("Debug", "Retrieved value: $buffer")
        timeMeal[1] = buffer.toInt()
        buffer =  Utility.loadDevice(this, "timeMealThreeMin").toString()
        Log.d("Debug", "Retrieved value: $buffer")
        timeMeal[2] = buffer.toInt()
        buffer =  Utility.loadDevice(this, "timeMealFourMin").toString()
        Log.d("Debug", "Retrieved value: $buffer")
        timeMeal[3] = buffer.toInt()
        buffer =  Utility.loadDevice(this, "timeMealOneHour").toString()
        Log.d("Debug", "Retrieved value: $buffer")
        timeMeal[4] = buffer.toInt()
        buffer =  Utility.loadDevice(this, "timeMealTwoHour").toString()
        Log.d("Debug", "Retrieved value: $buffer")
        timeMeal[5] = buffer.toInt()
        buffer =  Utility.loadDevice(this, "timeMealThreeHour").toString()
        Log.d("Debug", "Retrieved value: $buffer")
        timeMeal[6] = buffer.toInt()
        buffer =  Utility.loadDevice(this, "timeMealFourHour").toString()
        Log.d("Debug", "Retrieved value: $buffer")
        timeMeal[7] = buffer.toInt()
        buffer =  Utility.loadDevice(this, "perDay").toString()
        Log.d("Debug", "Retrieved value: $buffer")
        perDay = buffer.toInt()
        buffer =  Utility.loadDevice(this, "perMeal").toString()
        Log.d("Debug", "Retrieved value: $buffer")
        perMeal = buffer.toDouble()

        buffer =  Utility.loadDevice(this, "petName").toString()
        Log.d("Debug", "Retrieved value: $buffer")
        petName = buffer

        buffer =  Utility.loadDevice(this, "freeFeed").toString()
        Log.d("Debug", "Retrieved value: $buffer")
        freeFeed = buffer.toBoolean()
    }
    /*
    readData: Gets DatabaseReference for 'address' and reads values from Database.
    Saves data to device.
     */
    private fun readData(address: String) {

        database = FirebaseDatabase.getInstance().getReference(address)
        Utility.readInt(database, "timeMealOneMin") { value ->
            Utility.saveDevice(this,value.toString(),"timeMealOneMin")
        }
        Utility.readInt(database, "timeMealTwoMin") { value ->
            Utility.saveDevice(this,value.toString(),"timeMealTwoMin")
        }
        Utility.readInt(database, "timeMealThreeMin") { value ->
            Utility.saveDevice(this,value.toString(),"timeMealThreeMin")
        }
        Utility.readInt(database, "timeMealFourMin") { value ->
            Utility.saveDevice(this,value.toString(),"timeMealFourMin")
        }
        Utility.readInt(database, "timeMealOneHour") { value ->
            Utility.saveDevice(this,value.toString(),"timeMealOneHour")
        }
        Utility.readInt(database, "timeMealTwoHour") { value ->
            Utility.saveDevice(this,value.toString(),"timeMealTwoHour")
        }
        Utility.readInt(database, "timeMealThreeHour") { value ->
            Utility.saveDevice(this,value.toString(),"timeMealThreeHour")
        }
        Utility.readInt(database, "timeMealFourHour") { value ->
            Utility.saveDevice(this,value.toString(),"timeMealFourHour")
        }
        Utility.readDouble(database, "perMeal") { value ->
            Utility.saveDevice(this,value.toString(),"perMeal")
        }
        Utility.readInt(database, "perDay") { value ->
            Utility.saveDevice(this,value.toString(),"perDay")
        }
        Utility.readBool(database, "freeFeed") { value ->
            Utility.saveDevice(this,value.toString(),"freeFeed")
        }
        Utility.readString(database, "petName") { value ->
            Utility.saveDevice(this,value,"petName")
        }
    }
    /*
    getTime: Converts 'hourConvert' and 'minConvert' to 12 hour clock format and returns as String.
     */
    private fun getTime(hourConvert: Int, minConvert: Int): String {
        val time: String
        val min = minConvert.toString()
        var hour = hourConvert

        time = when (hour) {
            in 0..11 -> "$hour:${min.padStart(2, '0')} AM"
            12 -> "$hour:${min.padStart(2, '0')} PM"
            in 13..23 -> {
                hour -= 12
                "$hour:${min.padStart(2, '0')} PM"
            }
            else -> "Invalid time"
        }
        return time
    }
    /*
    showHide: Shows and hides views in PetInfo layout depending on 'freeFeed' state.
    Sets view 'tvFreeFeed' text to represent 'freeFeed' state.
     */
    private fun showHide() {
        with(binding) {
            if (freeFeed) {
                tvFoodAmountTitle.visibility = View.GONE
                tvFoodAmount.visibility = View.GONE
                tvWhatTimePet.visibility = View.GONE
                tvMealOne.visibility = View.GONE
                tvMealTwo.visibility = View.GONE
                tvMealThree.visibility = View.GONE
                tvMealFour.visibility = View.GONE

                tvFreeFeed.text = Utility.setText(this@PetInfo,"Enabled")
            } else {
                tvFreeFeed.text = Utility.setText(this@PetInfo,"Disabled")

                when (perDay) {
                    0 -> {
                        tvFoodAmountTitle.visibility = View.GONE
                        tvFoodAmount.visibility = View.GONE
                        tvWhatTimePet.visibility = View.GONE
                        tvMealOne.visibility = View.GONE
                        tvMealTwo.visibility = View.GONE
                        tvMealThree.visibility = View.GONE
                        tvMealFour.visibility = View.GONE
                    }

                    1 -> {
                        tvFoodAmountTitle.visibility = View.VISIBLE
                        tvFoodAmount.visibility = View.VISIBLE
                        tvWhatTimePet.visibility = View.VISIBLE
                        tvMealOne.visibility = View.VISIBLE
                        tvMealTwo.visibility = View.GONE
                        tvMealThree.visibility = View.GONE
                        tvMealFour.visibility = View.GONE
                    }

                    2 -> {
                        tvFoodAmountTitle.visibility = View.VISIBLE
                        tvFoodAmount.visibility = View.VISIBLE
                        tvWhatTimePet.visibility = View.VISIBLE
                        tvMealOne.visibility = View.VISIBLE
                        tvMealTwo.visibility = View.VISIBLE
                        tvMealThree.visibility = View.GONE
                        tvMealFour.visibility = View.GONE
                    }

                    3 -> {
                        tvFoodAmountTitle.visibility = View.VISIBLE
                        tvFoodAmount.visibility = View.VISIBLE
                        tvWhatTimePet.visibility = View.VISIBLE
                        tvMealOne.visibility = View.VISIBLE
                        tvMealTwo.visibility = View.VISIBLE
                        tvMealThree.visibility = View.VISIBLE
                        tvMealFour.visibility = View.GONE
                    }

                    4 -> {
                        tvFoodAmountTitle.visibility = View.VISIBLE
                        tvFoodAmount.visibility = View.VISIBLE
                        tvWhatTimePet.visibility = View.VISIBLE
                        tvMealOne.visibility = View.VISIBLE
                        tvMealTwo.visibility = View.VISIBLE
                        tvMealThree.visibility = View.VISIBLE
                        tvMealFour.visibility = View.VISIBLE
                    }
                }
            }
        }
    }
}