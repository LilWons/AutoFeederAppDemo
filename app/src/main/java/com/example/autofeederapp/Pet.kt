/*
    Author: Richard Hart
    Student Number: 991627469
    Project: AutoFeeder Application.
    Class: 1239_44805
    Course: ENGI39228 Capstone Project for CET/EET
    School: Sheridan College
    Instructor: Ning Zhu
    Date: 2023-12-11

    Description: AutoFeeder Pet Activity.
    Allows user to configure a custom pet feeding profile.
    Updates the Firebase with feeding profile settings under AutoFeeder MAC Address.
 */

package com.example.autofeederapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlin.String
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.example.autofeederapp.databinding.PetBinding
import com.google.firebase.database.DatabaseReference
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase
class Pet : AppCompatActivity() {

    private lateinit var binding: PetBinding
    private lateinit var database: DatabaseReference
    private  lateinit var address: String

    private val timeMeal = IntArray(8)
    private val timeMealWrite = IntArray(4)

    private var pM: Int = 12
    var perDay:Int = 0
    private var perMeal:Double = 0.00

    companion object {
        const val TAG = "MyActivity"
    }

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
        binding = PetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSpinners()
        setupListeners()

        address =  Utility.loadDevice(this,"AutoFeederID")?:"" // Loads AutoFeederID from device. If null, set to empty string.
        if(address.isEmpty() || address == "null"){ //If address is empty, prompt user and navigate to MainActivity Activity.
            Toast.makeText(this,"Connect to AutoFeeder", Toast.LENGTH_SHORT).show()
            Utility.goto(this@Pet, MainActivity::class.java)
        }
    }

    private val timeSelectionListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
            when(parent.id){
                R.id.spTimeOneHourPet ->{timeMeal[4] = position+1}
                R.id.spTimeTwoHourPet ->{timeMeal[5] = position+1}
                R.id.spTimeThreeHourPet ->{timeMeal[6] = position+1}
                R.id.spTimeFourHourPet ->{timeMeal[7] = position+1}
                R.id.spTimeOneMinPet ->{timeMeal[0] = position}
                R.id.spTimeTwoMinPet ->{timeMeal[1] = position}
                R.id.spTimeThreeMinPet ->{timeMeal[2] = position}
                R.id.spTimeFourMinPet ->{timeMeal[3] = position}
            }
        }
        override fun onNothingSelected(parent: AdapterView<*>) {
            when(parent.id){
                R.id.spTimeOneHourPet ->{timeMeal[4] = 0}
                R.id.spTimeTwoHourPet ->{timeMeal[5] = 0}
                R.id.spTimeThreeHourPet ->{timeMeal[6] = 0}
                R.id.spTimeFourHourPet ->{timeMeal[7] = 0}
                R.id.spTimeOneMinPet ->{timeMeal[0] = 0}
                R.id.spTimeTwoMinPet ->{timeMeal[1] = 0}
                R.id.spTimeThreeMinPet ->{timeMeal[2] = 0}
                R.id.spTimeFourMinPet ->{timeMeal[3] = 0}
            }
        }
    }

    private fun setupListeners() {
        with(binding) {
            btHomePet.setOnClickListener { Utility.goto(this@Pet, MainActivity::class.java) }
            btPetInfo.setOnClickListener{Utility.goto(this@Pet,PetInfo::class.java)}
            btDonePet.setOnClickListener {
                if(!tbFreeFeed.isChecked){
                    scheduleFeed(address)
                }
                sendName(address)
                Utility.goto(this@Pet, MainActivity::class.java)
            }

            spinnerAmountMeal.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    perMeal = when(position){
                        0 -> {
                            0.25
                        }

                        1->  {
                            0.33
                        }

                        2-> {
                            0.50
                        }

                        3-> {
                            0.66
                        }

                        4-> {
                            0.75
                        }

                        5 -> {
                            1.00
                        }

                        else -> {
                            0.00
                        }
                    }
                }
                override fun onNothingSelected(p0: AdapterView<*>?) {
                    perMeal = 0.00
                }
            }

            spinnerTimeZones.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val input = parent?.getItemAtPosition(position).toString()
                    val startIndex = 3
                    val endIndex = 9

                    if (endIndex <= input.length) {
                        var extractedString = input.substring(startIndex, endIndex)
                        extractedString = extractedString.replace(":",".")
                        println("Float Value: $extractedString")
                        try {
                            val floatValue = extractedString.toDouble()
                            println("Float Value: $floatValue")
                            database = FirebaseDatabase.getInstance().getReference(address)
                            database.child("timeZone").setValue(floatValue)
                        } catch (e: NumberFormatException) {
                            println("Invalid float format: $extractedString")
                        }
                    } else {
                        println("Invalid substring range.")
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    Log.d(TAG, "No TimeZone Selected")
                }
            }
            spinnerMeals.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    perDay = position + 1
                    when (perDay) {
                        1 -> {
                            spTimeOneMinPet.visibility = View.VISIBLE
                            spTimeTwoMinPet.visibility = View.GONE
                            spTimeThreeMinPet.visibility = View.GONE
                            spTimeFourMinPet.visibility = View.GONE
                            spTimeOneHourPet.visibility = View.VISIBLE
                            spTimeTwoHourPet.visibility = View.GONE
                            spTimeThreeHourPet.visibility = View.GONE
                            spTimeFourHourPet.visibility = View.GONE
                            tbAmPmOnePet.visibility = View.VISIBLE
                            tbAmPmTwoPet.visibility = View.GONE
                            tbAmPmThreePet.visibility = View.GONE
                            tbAmPmFourPet.visibility = View.GONE
                            tvColon.visibility = View.VISIBLE
                            tvColon2.visibility = View.GONE
                            tvColon3.visibility = View.GONE
                            tvColon4.visibility = View.GONE
                        }

                        2 -> {
                            spTimeOneMinPet.visibility = View.VISIBLE
                            spTimeTwoMinPet.visibility = View.VISIBLE
                            spTimeThreeMinPet.visibility = View.GONE
                            spTimeFourMinPet.visibility = View.GONE
                            spTimeOneHourPet.visibility = View.VISIBLE
                            spTimeTwoHourPet.visibility = View.VISIBLE
                            spTimeThreeHourPet.visibility = View.GONE
                            spTimeFourHourPet.visibility = View.GONE
                            tbAmPmOnePet.visibility = View.VISIBLE
                            tbAmPmTwoPet.visibility = View.VISIBLE
                            tbAmPmThreePet.visibility = View.GONE
                            tbAmPmFourPet.visibility = View.GONE
                            tvWhatTimePet.visibility = View.VISIBLE
                            tvColon.visibility = View.VISIBLE
                            tvColon2.visibility = View.VISIBLE
                            tvColon3.visibility = View.GONE
                            tvColon4.visibility = View.GONE
                        }

                        3 -> {
                            spTimeOneMinPet.visibility = View.VISIBLE
                            spTimeTwoMinPet.visibility = View.VISIBLE
                            spTimeThreeMinPet.visibility = View.VISIBLE
                            spTimeFourMinPet.visibility = View.GONE
                            spTimeOneHourPet.visibility = View.VISIBLE
                            spTimeTwoHourPet.visibility = View.VISIBLE
                            spTimeThreeHourPet.visibility = View.VISIBLE
                            spTimeFourHourPet.visibility = View.GONE
                            tbAmPmOnePet.visibility = View.VISIBLE
                            tbAmPmTwoPet.visibility = View.VISIBLE
                            tbAmPmThreePet.visibility = View.VISIBLE
                            tbAmPmFourPet.visibility = View.GONE
                            tvColon.visibility = View.VISIBLE
                            tvColon2.visibility = View.VISIBLE
                            tvColon3.visibility = View.VISIBLE
                            tvColon4.visibility = View.GONE
                            tvWhatTimePet.visibility = View.VISIBLE
                        }

                        4 -> {
                            spTimeOneMinPet.visibility = View.VISIBLE
                            spTimeTwoMinPet.visibility = View.VISIBLE
                            spTimeThreeMinPet.visibility = View.VISIBLE
                            spTimeFourMinPet.visibility = View.VISIBLE
                            spTimeOneHourPet.visibility = View.VISIBLE
                            spTimeTwoHourPet.visibility = View.VISIBLE
                            spTimeThreeHourPet.visibility = View.VISIBLE
                            spTimeFourHourPet.visibility = View.VISIBLE
                            tbAmPmOnePet.visibility = View.VISIBLE
                            tbAmPmTwoPet.visibility = View.VISIBLE
                            tbAmPmThreePet.visibility = View.VISIBLE
                            tbAmPmFourPet.visibility = View.VISIBLE
                            tvColon.visibility = View.VISIBLE
                            tvColon2.visibility = View.VISIBLE
                            tvColon3.visibility = View.VISIBLE
                            tvColon4.visibility = View.VISIBLE
                            tvWhatTimePet.visibility = View.VISIBLE
                        }


                        else -> {
                            spTimeOneMinPet.visibility = View.GONE
                            spTimeTwoMinPet.visibility = View.GONE
                            spTimeThreeMinPet.visibility = View.GONE
                            spTimeFourMinPet.visibility = View.GONE
                            spTimeOneHourPet.visibility = View.GONE
                            spTimeTwoHourPet.visibility = View.GONE
                            spTimeThreeHourPet.visibility = View.GONE
                            spTimeFourHourPet.visibility = View.GONE
                            tbAmPmOnePet.visibility = View.GONE
                            tbAmPmTwoPet.visibility = View.GONE
                            tbAmPmThreePet.visibility = View.GONE
                            tbAmPmFourPet.visibility = View.GONE
                            tvWhatTimePet.visibility = View.GONE
                            tvColon.visibility = View.VISIBLE
                            tvColon2.visibility = View.VISIBLE
                            tvColon3.visibility = View.VISIBLE
                            tvColon4.visibility = View.VISIBLE
                            tvWhatTimePet.visibility = View.VISIBLE
                        }
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    Log.d(TAG, "No meals selected.")
                }
            }
            tbFreeFeed.setOnCheckedChangeListener { _, isChecked ->
                database = FirebaseDatabase.getInstance().getReference(address)
                if (isChecked) {
                    database.child("freeFeed").setValue(true)
                    sendName(address)
                    tbAmPmOnePet.visibility = View.GONE
                    tbAmPmTwoPet.visibility = View.GONE
                    tbAmPmThreePet.visibility = View.GONE
                    tbAmPmFourPet.visibility = View.GONE
                    spTimeOneMinPet.visibility = View.GONE
                    spTimeTwoMinPet.visibility = View.GONE
                    spTimeThreeMinPet.visibility = View.GONE
                    spTimeFourMinPet.visibility = View.GONE
                    spTimeOneHourPet.visibility = View.GONE
                    spTimeTwoHourPet.visibility = View.GONE
                    spTimeThreeHourPet.visibility = View.GONE
                    spTimeFourHourPet.visibility = View.GONE
                    spinnerAmountMeal.visibility = View.GONE
                    tvWhatTimePet.visibility = View.GONE
                    tvColon.visibility = View.GONE
                    tvColon2.visibility = View.GONE
                    tvColon3.visibility = View.GONE
                    tvColon4.visibility = View.GONE
                    tvSpinnerMeals.visibility = View.GONE
                    tvSpinnerTZ.visibility = View.GONE
                    spinnerMeals.visibility = View.GONE
                    spinnerTimeZones.visibility = View.GONE
                    tv14.visibility = View.GONE
                    spinnerAmountMeal.visibility = View.GONE
                    tvWhatTimePet.visibility = View.GONE
                    spTimeOneHourPet.visibility = View.GONE
                    spTimeOneMinPet.visibility = View.GONE
                    tvColon.visibility = View.GONE
                    tbAmPmOnePet.visibility = View.GONE
                } else {
                    database.child("freeFeed").setValue(false)
                    spinnerAmountMeal.visibility = View.VISIBLE
                    tvSpinnerMeals.visibility = View.VISIBLE
                    tvSpinnerTZ.visibility = View.VISIBLE
                    spinnerMeals.visibility = View.VISIBLE
                    spinnerTimeZones.visibility = View.VISIBLE
                    tv14.visibility = View.VISIBLE
                    spinnerAmountMeal.visibility = View.VISIBLE
                    tvWhatTimePet.visibility = View.VISIBLE
                    spTimeOneHourPet.visibility = View.VISIBLE
                    spTimeOneMinPet.visibility = View.VISIBLE
                    tvColon.visibility = View.VISIBLE
                    tbAmPmOnePet.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setupSpinners() {
        val adapterMinutes = ArrayAdapter.createFromResource(
            this,
            R.array.minute,
            R.layout.spinner_item
        )
        val adapterHour = ArrayAdapter.createFromResource(
            this,
            R.array.hour,
            R.layout.spinner_item
        )
        val adapterTZ = ArrayAdapter.createFromResource(
            this,
            R.array.timeZones,
            R.layout.spinner_item
        )
        val adapterMeals = ArrayAdapter.createFromResource(
            this,
            R.array.mealPerDay,
            R.layout.spinner_item
        )

        val perMealAmount = ArrayAdapter.createFromResource(
            this,
            R.array.perMealAmount,
            R.layout.spinner_item
        )

        adapterMinutes.setDropDownViewResource(R.layout.spinner_item)
        adapterHour.setDropDownViewResource(R.layout.spinner_item)
        adapterTZ.setDropDownViewResource(R.layout.spinner_item)
        adapterMeals.setDropDownViewResource(R.layout.spinner_item)
        perMealAmount.setDropDownViewResource(R.layout.spinner_item)

        with(binding) {
            listOf(spTimeOneMinPet, spTimeTwoMinPet, spTimeThreeMinPet, spTimeFourMinPet).forEach { spinner ->
                spinner.adapter = adapterMinutes
                spinner.onItemSelectedListener = timeSelectionListener

            }
            listOf(spTimeOneHourPet, spTimeTwoHourPet, spTimeThreeHourPet, spTimeFourHourPet).forEach { spinner ->
                spinner.adapter = adapterHour
                spinner.onItemSelectedListener = timeSelectionListener
            }
            spinnerTimeZones.adapter = adapterTZ
            spinnerMeals.adapter = adapterMeals
            spinnerAmountMeal.adapter = perMealAmount
        }
    }


    private fun scheduleFeed(address: String){
        setTimes()
        database = FirebaseDatabase.getInstance().getReference(address)
        database.child("timeMealOneHour").setValue(timeMealWrite[0])
        database.child("timeMealTwoHour").setValue(timeMealWrite[1])
        database.child("timeMealThreeHour").setValue(timeMealWrite[2])
        database.child("timeMealFourHour").setValue(timeMealWrite[3])
        database.child("timeMealOneMin").setValue(timeMeal[0])
        database.child("timeMealTwoMin").setValue(timeMeal[1])
        database.child("timeMealThreeMin").setValue(timeMeal[2])
        database.child("timeMealFourMin").setValue(timeMeal[3])
        database.child("perMeal").setValue(perMeal)
        database.child("perDay").setValue(perDay)
    }

    private fun sendName(address: String){
        val petName = binding.etPetNamePet.text.toString()
        database = FirebaseDatabase.getInstance().getReference(address)
        database.child("petName").setValue(petName)
        database.child("freeFeed").setValue(binding.tbFreeFeed.isChecked)}

    private fun setTimes() {
        with(binding) {
            timeMealWrite[0] = amPMCheck(timeMeal[4], tbAmPmOnePet.isChecked)
            timeMealWrite[1] = amPMCheck(timeMeal[5], tbAmPmTwoPet.isChecked)
            timeMealWrite[2] = amPMCheck(timeMeal[6], tbAmPmThreePet.isChecked)
            timeMealWrite[3] = amPMCheck(timeMeal[7], tbAmPmFourPet.isChecked) }
    }

    private fun amPMCheck(hour:Int, amPM:Boolean): Int {
        var time = hour

        if (amPM && hour != 12) {
            time += pM
        } else if (!amPM && hour == 12) {
            time = 0
        } else{
            time = hour
        }
        return time
    }
}
