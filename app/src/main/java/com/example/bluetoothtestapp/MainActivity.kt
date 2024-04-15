package com.example.bluetoothtestapp

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
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
    private lateinit var connectThread: Thread
    private lateinit var readBuffer: ByteArray
    private var readBufferPosition: Int = 0
    private @Volatile var stopWorker: Boolean = false
    private val PERMISSION_REQUEST_BLUETOOTH_CONNECT = 1
    private val MY_UUID: UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb") // Standard SerialPortService ID
    private val characteristicUUID: UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9B34fb")
    private lateinit var messageCountTextView: TextView
    //sensor temperature variables
    private lateinit var sensor1Temp:TextView
    private lateinit var sensor2Temp:TextView
    private lateinit var sensor3Temp:TextView
    private lateinit var sensor4Temp:TextView
    private lateinit var sensor5Temp:TextView
    private lateinit var sensor6Temp:TextView
    private lateinit var sensor7Temp:TextView
    private lateinit var sensor8Temp:TextView
    //sensor voltage variables
    private var sensor1Voltage: Int? = null
    private var sensor2Voltage: Int? = null
    private var sensor3Voltage: Int? = null
    private var sensor4Voltage: Int? = null
    private var sensor5Voltage: Int? = null
    private var sensor6Voltage: Int? = null
    private var sensor7Voltage: Int? = null
    private var sensor8Voltage: Int? = null
    //sensor energy saving mode variables
    private var sensor1ESM:Int = 0
    private var sensor2ESM:Int = 0
    private var sensor3ESM:Int = 0
    private var sensor4ESM:Int = 0
    private var sensor5ESM:Int = 0
    private var sensor6ESM:Int = 0
    private var sensor7ESM:Int = 0
    private var sensor8ESM:Int = 0
    //sensor humidity variables
    private lateinit var sensor1Humidity:TextView
    private lateinit var sensor2Humidity:TextView
    private lateinit var sensor3Humidity:TextView
    private lateinit var sensor4Humidity:TextView
    private lateinit var sensor5Humidity:TextView
    private lateinit var sensor6Humidity:TextView
    private lateinit var sensor7Humidity:TextView
    private lateinit var sensor8Humidity:TextView

    private lateinit var isConnected:TextView
    val messageList = mutableListOf<String>()

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
        isConnected = findViewById(R.id.isConnected)
        isConnected.text = "Not Connected"
        sensor1Temp = findViewById(R.id.sensor1Temp)
        sensor1Humidity = findViewById(R.id.sensor1Humidity)

        sensor2Temp = findViewById(R.id.sensor2Temp)
        sensor2Humidity = findViewById(R.id.sensor2Humidity)

        sensor3Temp = findViewById(R.id.sensor3Temp)
        sensor3Humidity = findViewById(R.id.sensor3Humidity)

        sensor4Temp = findViewById(R.id.sensor4Temp)
        sensor4Humidity = findViewById(R.id.sensor4Humidity)

        sensor5Temp = findViewById(R.id.sensor5Temp)
        sensor5Humidity = findViewById(R.id.sensor5Humidity)

        sensor6Temp = findViewById(R.id.sensor6Temp)
        sensor6Humidity = findViewById(R.id.sensor6Humidity)

        sensor7Temp = findViewById(R.id.sensor7Temp)
        sensor7Humidity = findViewById(R.id.sensor7Humidity)

        sensor8Temp = findViewById(R.id.sensor8Temp)
        sensor8Humidity = findViewById(R.id.sensor8Humidity)

        resetButton = findViewById(R.id.resetButton)
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

        //val pairedDevices = bluetoothAdapter.bondedDevices
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
            if (socket.isConnected){
                isConnected.text = "Connected"
            }

            // Continue with other operations if the connection is successful

        } catch (e: IOException) {
            Log.e("BluetoothError", "Connection failed: ${e.message}")
        }

    }
    private fun remainConnected(){
        connectThread = Thread{

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
                    if (socket.isConnected){
                        isConnected.text = "Connected"
                    }

                    // Continue with other operations if the connection is successful

                } catch (e: IOException) {
                    Log.e("BluetoothError", "Connection failed: ${e.message}")
                }

                while(!socket.isConnected){

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
                        if (socket.isConnected){
                            isConnected.text = "Connected"
                        }

                        // Continue with other operations if the connection is successful

                    } catch (e: IOException) {
                        Log.e("BluetoothError", "Connection failed: ${e.message}")
                    }
                }
        }
        connectThread.start()
    }

    private fun beginListenForData() {
        val handler = Handler()
        val delimiter: Byte = 10 // ASCII code for a new line character

        stopWorker = false
        readBufferPosition = 0
        readBuffer = ByteArray(100)

        workerThread = Thread {
            while (/*!Thread.currentThread().isInterrupted && */!stopWorker) {
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

                                //parsing here
                                val dataList = data.split(":")
                                val sensorID = dataList[0]
                                val temperature = dataList[1]
                                val formattedTemp = "${temperature.toDouble()/100} F"
                                val humidity = dataList[2]
                                val formattedHum = "${humidity.toDouble() /100} %"
                                val batteryVoltage = dataList[3]
                                val energySavingMode = dataList[4]
                                //sendAcknowledgement()
                                // Add the new message to the list
                                messageList.add(data)

                                // Keep only the last 8 messages
                                if (messageList.size > 8) {
                                    messageList.removeAt(0)
                                }
                                // Update the UI with the new message count
                                handler.post {
                                    messageCountTextView.text = "Message Count: $messageCount"
                                    val messages = messageList.joinToString("\n") // Combine messages with newline
                                    textView.text = messages
                                    //textView.text = data
                                    // parseAndPlace(data)
                                    when(sensorID){
                                        "1"-> {
                                            sensor1Temp.text = formattedTemp
                                            sensor1Humidity.text = formattedHum
                                            sensor1Voltage = batteryVoltage.toInt()
                                            sensor1ESM = energySavingMode.toInt()
                                        }
                                        "2"-> {
                                            sensor2Temp.text = formattedTemp
                                            sensor2Humidity.text = formattedHum
                                            sensor2Voltage = batteryVoltage.toInt()
                                            sensor2ESM = energySavingMode.toInt()
                                        }
                                        "3"-> {
                                            sensor3Temp.text = formattedTemp
                                            sensor3Humidity.text = formattedTemp
                                            sensor3Voltage = batteryVoltage.toInt()
                                            sensor3ESM = energySavingMode.toInt()
                                        }
                                        "4"-> {
                                            sensor4Temp.text = formattedTemp
                                            sensor4Humidity.text = formattedHum
                                            sensor4Voltage = batteryVoltage.toInt()
                                            sensor4ESM = energySavingMode.toInt()
                                        }
                                        "5"-> {
                                            sensor5Temp.text = formattedTemp
                                            sensor5Humidity.text = formattedHum
                                            sensor5Voltage = batteryVoltage.toInt()
                                            sensor5ESM = energySavingMode.toInt()
                                        }
                                        "6"-> {
                                            sensor6Temp.text = formattedTemp
                                            sensor6Humidity.text = formattedHum
                                            sensor6Voltage = batteryVoltage.toInt()
                                            sensor6ESM = energySavingMode.toInt()
                                        }
                                        "7"-> {
                                            sensor7Temp.text = formattedTemp
                                            sensor7Humidity.text = formattedHum
                                            sensor7Voltage = batteryVoltage.toInt()
                                            sensor7ESM = energySavingMode.toInt()
                                        }
                                        "8"-> {
                                            sensor8Temp.text = formattedTemp
                                            sensor8Humidity.text = formattedHum
                                            sensor8Voltage = batteryVoltage.toInt()
                                            sensor8ESM = energySavingMode.toInt()
                                        }
                                    }

                                }
                            } else {
                                readBuffer[readBufferPosition++] = b
                            }
                        }
                    }
                } catch (ex: IOException) {
                    //stopWorker = true
                }
            }
        }

        workerThread.start()
    }

    private fun parseAndPlace(data: String){
        val dataList = data.split(":");
        val sensorID = dataList[0];
        val temperature = dataList[1];
        val humidity = dataList[2];
        val batteryVoltage = dataList[3];
        val energySavingMode = dataList[4];

        when(sensorID){
            "1"-> {
                sensor1Temp.text = temperature
                sensor1Humidity.text = humidity   
            }
            "2"-> {
                sensor2Temp.text = temperature
                sensor2Humidity.text = humidity
            }
            "3"-> {
                sensor3Temp.text = temperature
                sensor3Humidity.text = humidity
            }
            "4"-> {
                sensor4Temp.text = temperature
                sensor4Humidity.text = humidity
            }
            "5"-> {
                sensor5Temp.text = temperature
                sensor5Humidity.text = humidity
            }
            "6"-> {
                sensor6Temp.text = temperature
                sensor6Humidity.text = humidity
            }
            "7"-> {
                sensor7Temp.text = temperature
                sensor7Humidity.text = humidity
            }
            "8"-> {
                sensor8Temp.text = temperature
                sensor8Humidity.text = humidity
            }
        }
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
