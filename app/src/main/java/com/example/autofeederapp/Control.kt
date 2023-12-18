/*
    Author: LilWons
    Project: AutoFeeder Application.
    Date: 2023-12-11

    Description: AutoFeeder Control Activity.
    Gets Wi-Fi SSID and Password.
    Sends Wi-Fi SSID, Password, AutoFeeder MAC Address, and Firebase security token
    over BLE to AutoFeeder.
 */
package com.example.autofeederapp

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.autofeederapp.databinding.ActivityControlBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.min

class Control : AppCompatActivity() {
    companion object {
        lateinit var address: String
        const val BLUETOOTH_CONNECT_REQUEST_CODE = 150
    }

    private val TAG = "BleClientActivity"
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothGatt: BluetoothGatt
    private lateinit var deviceAddress: String
    private lateinit var binding: ActivityControlBinding
    private var writeContinuation: Continuation<Unit>? = null
    private var wifiGattCharacteristic: BluetoothGattCharacteristic? = null

    private val serviceUuid =
        UUID.fromString("0000180A-0000-1000-8000-00805f9b34fb")
    private val wifiCharacteristicUuid =
        UUID.fromString("00002A56-0000-1000-8000-00805f9b34fb")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityControlBinding.inflate(layoutInflater)
        setContentView(binding.root)

        address = intent.getStringExtra(Settings.EXTRA_ADDRESS).toString() //Gets String extra from Intent passed to Control Activity.
        deviceAddress = address.takeLast(17)
        Log.i(TAG, "DEVICE ADDRESS: $address")

        Utility.saveDevice(this, deviceAddress, "AutoFeederID") //Saves the 'deviceAddress' locally.
        connectToGatt()
        setupListeners()
    }
    /*
    connectToGatt: Setup and handles the initial steps for connecting to BLE Device.
     */
    private  fun connectToGatt(){
        if (ActivityCompat.checkSelfPermission( //Checks that BLUETOOTH_CONNECT permissions is granted.
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager //Gets BluetoothManager from system services and casts it to BluetoothManager type.
            bluetoothAdapter = bluetoothManager.adapter //Gets BluetoothAdapter from BluetoothManager.

            if (!bluetoothAdapter.isEnabled) { //If Bluetooth adapter is disabled, return from connectToGatt.
                Toast.makeText(this, "Bluetooth is Disabled. Please Enable BlueTooth.", Toast.LENGTH_SHORT).show()
                return
            }
            val device = bluetoothAdapter.getRemoteDevice(deviceAddress)
            bluetoothGatt = device.connectGatt(this, false, gattCallback)
        } else {
            Toast.makeText(this, "Bluetooth permission is required to setup WI-FI on AutoFeeder", Toast.LENGTH_SHORT).show()
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), BLUETOOTH_CONNECT_REQUEST_CODE) //Requests permission for BLUETOOTH_CONNECT.
        }
    }
    /*
    onRequestPermissionsResult: Callback function that handles the permission request response.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            BLUETOOTH_CONNECT_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    connectToGatt()
                } else {
                    Toast.makeText(this, "Bluetooth permission is required for initial setup of AutoFeeder.", Toast.LENGTH_SHORT).show()
                    Utility.goto(this, MainActivity::class.java)
                }
            }
        }
    }
    /*
    setupListeners(): Sets up view listeners on Control layout.
     */
    private fun setupListeners() {
        with(binding) {
            btCancel.setOnClickListener { disconnect() }
            btSetup.setOnClickListener { disconnect() }
            btConnectBT.setOnClickListener {
                wifiGattCharacteristic?.let { characteristic -> //Executes code in 'let' function only if  'wifiGattCharacteristic' is not null. If executed, 'characteristic' is given 'wifiGattCharacteristic' value.
                   pbBar.visibility = View.VISIBLE
                    val wifiArray = getWiFi()
                    val serializedString = serializeStringArray(wifiArray)
                    val dataToSend = serializedString.toByteArray()
                    val chunks = splitData(dataToSend, 20)
                    val endCfDataToSend = byteArrayOf(0x20,0x20,0x20) //End sequence to indicate end of data being sent.
                    val endCfData = splitData(endCfDataToSend, 3)

                    CoroutineScope(Dispatchers.IO).launch {//Starts coroutine on the I/O dispatcher.
                        sendData(bluetoothGatt, characteristic, chunks) //Sends data chunks over BLE.
                        sendData(bluetoothGatt, characteristic, endCfData) //Sends end sequence.

                        withContext(Dispatchers.Main) {//After sending the data, calls toAccount() function.
                            toAccount()
                        }
                    }
                }
            }
        }
    }


    /*
    gattCallback: Custom implementation of the BluetoothGattCallback() class.
     */
    private val gattCallback = object : BluetoothGattCallback() {
        /*
        onConnectionStateChange: Handles changes in Bluetooth connection state.
        If a successful connection is made, starts service discovery.
        If successful disconnection is made, notifies user of disconnection.
         */
        override fun onConnectionStateChange(
            gatt: BluetoothGatt?,
            status: Int,
            newState: Int
        ) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "Connected to GATT server.")
                //Checks required permissions are granted.
                if (ActivityCompat.checkSelfPermission(
                        this@Control,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Toast.makeText(this@Control, "Bluetooth permission is required for initial setup of AutoFeeder.", Toast.LENGTH_SHORT).show()
                    return
                }
                else{
                    bluetoothGatt.discoverServices() //Once discoverServices() is completed, onServicesDiscovered() is called.
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "Disconnected from GATT server.")
                Toast.makeText(this@Control, "Disconnected from AutoFeeder via Bluetooth.", Toast.LENGTH_SHORT).show()
            }
        }
        /*
        onServicesDiscovered: Called after service discovery is completed on the Bluetooth GATT Server.
        If the discovery is successful, attempts to get the serviceUuid and wifiCharacteristicUuid.
         */
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val service = gatt?.getService(serviceUuid)
                wifiGattCharacteristic = service?.getCharacteristic(wifiCharacteristicUuid)
                Log.d(TAG, "wifiNameCharacteristic: $wifiGattCharacteristic")
            }
        }

        /*
        onCharacteristicRead: Called after BLE Read operation has been completed.
        Logs if read operation was successful.
        */
        @Deprecated("Deprecated in Java")
        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (ActivityCompat.checkSelfPermission(
                        this@Control,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }else{
                    bluetoothGatt.readCharacteristic(characteristic)
                    Log.i(TAG,"Read Data Successful")
                }
            }
        }

        /*
        onCharacteristicWrite: Called after BLE Write operation has been completed.
        If the Write operation was successful, resumes coroutine.
         */
        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                writeContinuation?.resume(Unit)
                Log.i(TAG, "Write successful")
            }
            writeContinuation = null
        }
    }
    /*
    disconnect: Checks required permissions are granted.
    Disconnects from established Bluetooth connection.
    Closes Bluetooth GATT Client.
    Navigates to Settings Activity.
     */
    private fun disconnect() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "Bluetooth permission is required for initial setup of AutoFeeder.", Toast.LENGTH_SHORT).show()
            return
        }else{
            bluetoothGatt.disconnect()
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "Bluetooth permission is required for initial setup of AutoFeeder.", Toast.LENGTH_SHORT).show()
            return
        }else{
            bluetoothGatt.close()
        }
        Utility.goto(this, Settings::class.java)
    }
    /*
    toAccount: Hides loading bar.
    Calls disconnect() Function.
    Navigates to Account activity.
     */
    private fun toAccount() {
        binding.pbBar.visibility = View.GONE
        disconnect()
        Utility.goto(this, Account::class.java)
    }
    /*
    splitData: Splits ByteArray into a list of ByteArray's of specified chunkSize.
    Returns list of ByteArray's.
     */
    private fun splitData(data: ByteArray, chunkSize: Int): List<ByteArray> {
        val chunks = mutableListOf<ByteArray>()
        var start = 0
        while (start < data.size) {
            val end = min(start + chunkSize, data.size)
            chunks.add(data.copyOfRange(start, end))
            start += chunkSize
        }
        return chunks
    }
    /*
    sendData: Suspend function to send a chunk in dataChunks than wait until the chunk has been sent successfully.
     */
    private suspend fun sendData(
        bluetoothGatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        dataChunks: List<ByteArray>
    ) {
        for (chunk in dataChunks) {
            sendChunkAndWait(bluetoothGatt, characteristic, chunk)
        }
    }
    /*
    sendChunkAndWait: Suspend function that sends a chunk of data over Bluetooth Low Energy than waits for the operation to be completed.
     */
    private suspend fun sendChunkAndWait(
        bluetoothGatt: BluetoothGatt, //Bluetooth GATT Object for BLE Communication.
        characteristic: BluetoothGattCharacteristic, //Bluetooth GATT Characteristic that is being written to.
        chunk: ByteArray //Data being sent over BLE.
    ) = suspendCoroutine { continuation -> //Counter routine that suspends coroutine until manually resumed by lambda parameter continuation.
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this@Control, "Bluetooth permission is required for initial setup of AutoFeeder.", Toast.LENGTH_SHORT).show()
            return@suspendCoroutine
        }
        bluetoothGatt.writeCharacteristic(characteristic,chunk, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
        writeContinuation = continuation
    }
    /*
    onDestroy: Final cleanup of Control Activity.
    Checks for required permissions than disconnects from Bluetooth GATT Client.
     */
    override fun onDestroy() {
        super.onDestroy()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this@Control, "Bluetooth permission is required for initial setup of AutoFeeder.", Toast.LENGTH_SHORT).show()
            Utility.goto(this@Control,Settings::class.java)
            return
        }
        bluetoothGatt.close()
    }
    /*
    getWiFi: Gets Wi-Fi SSID and Password from user.
    Returns Wi-Fi SSID, Wi-Fi Password, MAC Address, and Firebase Token as an Array.
     */
    private fun getWiFi(): Array<String> {
        with(binding) {
            val wifiName = etWiFiName.text.toString()
            val wifiPassword = etWiFiPassword.text.toString()
            val tokenID = "5gTNvzDRFwWdi472s0T1Ftlal42kUVn2jVwmvq3S"
            return arrayOf(wifiName, deviceAddress, tokenID, wifiPassword)
        }
    }
    /*
    serializeStringArray: Joins String Array elements with not found in Wi-Fi SSID, MAC Address, and Firebase Token.
    Returns joined string.
     */
    private fun serializeStringArray(stringArray: Array<String>): String {
        return stringArray[0] + "?" + stringArray[1] + "$" + stringArray[2] + ")" + stringArray[3]
    }
}

