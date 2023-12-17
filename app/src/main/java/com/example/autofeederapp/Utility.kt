/*
    Author: Richard Hart
    Student Number: 991627469
    Project: AutoFeeder Application.
    Class: 1239_44805
    Course: ENGI39228 Capstone Project for CET/EET
    School: Sheridan College
    Instructor: Ning Zhu
    Date: 2023-12-11

    Description: AutoFeeder Utility class.
    Provides functions for global use.
 */
package com.example.autofeederapp

import android.content.Context
import android.content.Intent
import com.google.firebase.database.DatabaseReference


class Utility {

    companion object {
        /*
        goto: Starts an Activity Intent.
         */
        fun <T> goto(context: Context, activityClass: Class<T>) {
            val intent = Intent(context, activityClass)
            context.startActivity(intent)
        }
        /*
        readInt: Reads Integer datatype from Database.
        Invokes 'callback' method and passes Integer value as argument.
        */
        fun readInt(database: DatabaseReference, path: String, callback: (Int) -> Unit) {
            database.child(path).get().addOnSuccessListener {
                val value = if (it.exists()) {
                    it.value.toString().toInt()
                } else {
                    0
                }
                callback(value)
            }
        }
        /*
        readBool: Reads Boolean datatype from Database.
        Invokes 'callback' method and passes Boolean value as argument.
        */
        fun readBool(database: DatabaseReference, path: String, callback: (Boolean) -> Unit) {
            database.child(path).get().addOnSuccessListener {
                val value = if (it.exists()) {
                    it.value.toString().toBoolean()
                } else {
                    false
                }
                callback(value)
            }
        }
        /*
        readString: Reads String datatype from Database.
        Invokes 'callback' method and passes String value as argument.
        */
        fun readString(database: DatabaseReference, path: String, callback: (String) -> Unit) {
            database.child(path).get().addOnSuccessListener {
               val value = if (it.exists()) {
                    it.value.toString()
                } else {
                    ""
                }
                callback(value)
            }
        }
        /*
        readDouble: Reads double datatype from Database.
        Invokes 'callback' method and passes double value as argument.
         */
        fun readDouble(database: DatabaseReference, path: String, callback: (Double) -> Unit) {
            database.child(path).get().addOnSuccessListener {
                val value = if (it.exists()) {
                    it.value.toString().toDouble()
                } else {
                    0.0
                }
                callback(value)
            }
        }

        fun saveDevice(context: Context, data: String, dataType: String) {
            val sharedPreferences = context.getSharedPreferences("AutoFeeder", Context.MODE_PRIVATE)
            sharedPreferences.edit().apply {
                putString(dataType, data)
                apply()
            }
        }

        fun loadDevice(context: Context, dataType: String): String? {
            val sharedPreferences = context.getSharedPreferences("AutoFeeder", Context.MODE_PRIVATE)
            return sharedPreferences.getString(dataType, "null")
        }

        fun setText(context: Context, string: String): String {
            return context.getString(R.string.format_string, string)
        }
    }
}
