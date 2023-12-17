/*
    Author: Richard Hart
    Student Number: 991627469
    Project: AutoFeeder Application.
    Class: 1239_44805
    Course: ENGI39228 Capstone Project for CET/EET
    School: Sheridan College
    Instructor: Ning Zhu
    Date: 2023-12-11

    Description: AutoFeeder Settings Activity.
    Allows user to setup AutoFeeder with BLE.
    Scans for BLE devices and displays AutoFeeder with MAC Address.
 */
package com.example.autofeederapp

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.Manifest
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.autofeederapp.databinding.ActivitySettingsBinding

class Settings : AppCompatActivity() {


    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 135
        const val EXTRA_ADDRESS: String = "Device_address"
    }

    private lateinit var lvBTList: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var binding: ActivitySettingsBinding
    private val uniqueDeviceAddresses = HashSet<String>()
    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }
    private val bleScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            val device: BluetoothDevice? = result.device
            if (ActivityCompat.checkSelfPermission(
                    this@Settings,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permsDenied()
                return
            }
            val deviceName: String? = device?.name
            val deviceAddress: String? = device?.address

            if(deviceName == "AutoFeeder"){
                val list = deviceName.toString()+": " + deviceAddress.toString()
                addDeviceToList(list)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        lvBTList = findViewById(R.id.lvBTList)

        bleCheck()
        setupSpinners()
        setupListeners()
    }
    private fun bleCheck(){
        if (bluetoothAdapter?.isEnabled == true) {
            if(!checkBlePermissions()){
                requestPermissions()
            }
        }
        else{
            Toast.makeText(
                this,
                "Bluetooth is Disabled. Please Enable BlueTooth to Scan For AutoFeeder.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    private fun setupSpinners() {

        val list: ArrayList<String> = ArrayList()
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, list)
        lvBTList.adapter = adapter

        lvBTList.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val deviceAddress: String = adapter.getItem(position) ?: ""
            val controlActivity = Intent(this, Control::class.java)
            controlActivity.putExtra(EXTRA_ADDRESS, deviceAddress)
            Log.i(TAG, "EXTRA_ADDRESS: $EXTRA_ADDRESS  Device Address: $deviceAddress")
            startActivity(controlActivity)
        }
    }

    private fun setupListeners() {
        with(binding){
            btSearchBT.setOnClickListener {
                if (checkBlePermissions()) {
                    startBleScan()
                } else {
                    requestPermissions()
                }
            }
            btAccount.setOnClickListener{Utility.goto(this@Settings,Account::class.java)}
            btHomePet.setOnClickListener{Utility.goto(this@Settings,MainActivity::class.java)}
        }
    }
        override fun onPause() {
            super.onPause()
            stopBleScan()
        }

        private fun locationPerms(): Boolean{
            return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }

        private fun checkBlePermissions(): Boolean {
            return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED
        }


        private fun requestPermissions() {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_CONNECT),
                    PERMISSIONS_REQUEST_CODE
                )
        }

        override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            when (requestCode) {
                PERMISSIONS_REQUEST_CODE -> {
                    val allPermissionsGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }

                    if (allPermissionsGranted) {
                        startBleScan()
                    } else {
                        permsDenied()
                    }
                }
            }
        }

        private fun startBleScan() {
            if (checkBlePermissions()) {
                val scanner = bluetoothAdapter?.bluetoothLeScanner
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_SCAN
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    permsDenied()
                    return
                }
                scanner?.startScan(bleScanCallback)
            }
        }

        private fun stopBleScan() {
            if (checkBlePermissions()) {
                val scanner = bluetoothAdapter?.bluetoothLeScanner
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_SCAN
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    permsDenied()
                    return
                }
                scanner?.stopScan(bleScanCallback)
            }
        }

        private fun addDeviceToList(deviceAddress: String) {
            runOnUiThread {
                if (uniqueDeviceAddresses.add(deviceAddress)) {
                    // Only add the device if it's not already in the set
                    adapter.add(deviceAddress)
                    adapter.notifyDataSetChanged()
                }
            }
        }
    private fun permsDenied(){
        Toast.makeText(this, "Bluetooth permission is required for initial setup of AutoFeeder.", Toast.LENGTH_SHORT).show()
    }

    }
