package com.example.bluetoothtestapp

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID


@SuppressLint("MissingPermission")
class MainActivity : androidx.activity.ComponentActivity() {

    //Regular Bluetooth Initializations
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothSocket: BluetoothSocket
    private lateinit var inputStream: InputStream
    private lateinit var outputStream: OutputStream
    private lateinit var workerThread: Thread
    private lateinit var readBuffer: ByteArray
    private var readBufferPosition: Int = 0
    private @Volatile var stopWorker: Boolean = false
    private val PERMISSION_REQUEST_BLUETOOTH_CONNECT = 1
    private val MY_UUID: UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb") // Standard SerialPortService ID
    private val characteristicUUID: UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9B34fb")
    private lateinit var messageCountTextView: TextView
    private lateinit var textView: TextView
    private lateinit var resetButton: Button
    private lateinit var reconnectButton: Button
    private var messageCount = 0
    private var macAddress = "98:DA:60:07:B0:64"

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        messageCountTextView = findViewById(R.id.messageCountTextView)
        messageCountTextView.text = "Message Count: $messageCount"

        textView = findViewById(R.id.textView)
        textView.text = "Message: "

        resetButton = findViewById<Button>(R.id.resetButton)
        resetButton.setOnClickListener {
            messageCount = 0
            messageCountTextView.text = "Message Count: $messageCount"
        }

        reconnectButton = findViewById<Button>(R.id.reconnectBtn)
        reconnectButton.setOnClickListener {
            reconnect()
        }

        if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            // Permission is already granted, proceed with your Bluetooth operations
        } else {
            // Permission is not granted, request it
            requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_CONNECT), PERMISSION_REQUEST_BLUETOOTH_CONNECT)
        }

        val bluetoothManager = getSystemService(BluetoothManager::class.java)

        // Get the BluetoothAdapter object
        bluetoothAdapter = bluetoothManager.adapter

        if (!bluetoothAdapter.isEnabled) {
            // Enable Bluetooth
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            val REQUEST_ENABLE_BT = 1
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }
        val pairedDevices = bluetoothAdapter.bondedDevices
        // Get the BluetoothDevice object by its MAC address
        val device = bluetoothAdapter.getRemoteDevice(macAddress)
        var socket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID)

        // Get a BluetoothSocket object by using the UUID

        try {
            // Connect with a timeout
            socket = device.javaClass.getMethod(
                "createRfcommSocket", *arrayOf<Class<*>?>(
                    Int::class.javaPrimitiveType
                )
            ).invoke(device, 1) as BluetoothSocket?
            socket.connect()

            inputStream = socket.inputStream
            outputStream = socket.outputStream
            beginListenForData()

            // Continue with other operations if the connection is successful

        } catch (e: IOException) {
            Log.e("BluetoothError", "Connection failed: ${e.message}")
        }


        // Connect to the device


    }

    private fun beginListenForData() {
        val handler = Handler()
        val delimiter: Byte = 10 // ASCII code for a new line character

        stopWorker = false
        readBufferPosition = 0
        readBuffer = ByteArray(11800)

        workerThread = Thread {
            while (!Thread.currentThread().isInterrupted && !stopWorker) {
                try {
                    val bytesAvailable: Int = inputStream.available()
                    if (bytesAvailable > 0) {
                        val packetBytes = ByteArray(bytesAvailable)
                        inputStream.read(packetBytes)
                        for (i in 0 until bytesAvailable) {
                            val b: Byte = packetBytes[i]
                            if (b == delimiter) {
                                val encodedBytes = ByteArray(readBufferPosition)
                                System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.size)
                                val data = String(encodedBytes, charset("US-ASCII"))
                                readBufferPosition = 0

                                // Increment the message count
                                messageCount++

                                sendAcknowledgement()
                                // Update the UI with the new message count
                                handler.post {
                                    messageCountTextView.text = "Message Count: $messageCount"
                                    textView.text = data
                                }
                            } else {
                                readBuffer[readBufferPosition++] = b
                            }
                        }
                    }
                } catch (ex: IOException) {
                    stopWorker = true
                }
            }
        }

        workerThread.start()

    }

    private fun sendAcknowledgement() {
        val message = "ACK\n" // Message to be sent as an acknowledgement
        try {
            outputStream.write(message.toByteArray())
        } catch (e: IOException) {
            // Handle errors in sending acknowledgement
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            stopWorker = true
            inputStream.close()
            outputStream.close()
            bluetoothSocket.close()
        } catch (e: IOException) {
            // Handle errors here
        }
    }

    private fun reconnect(){
        val device = bluetoothAdapter.getRemoteDevice(macAddress)
        var socket = device.createRfcommSocketToServiceRecord(MY_UUID)
        try {
            // Connect with a timeout
            socket = (device.javaClass.getMethod(
                "createRfcommSocket", *arrayOf<Class<*>?>(
                    Int::class.javaPrimitiveType
                )
            ).invoke(device, 1) as BluetoothSocket?)!!
            socket.connect()

            inputStream = socket.inputStream
            outputStream = socket.outputStream
            beginListenForData()

            // Continue with other operations if the connection is successful

        } catch (e: IOException) {
            Log.e("BluetoothError", "Connection failed: ${e.message}")

            // Handle connection failure or timeout

        }
    }

}
