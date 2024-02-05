package com.example.bluetoothtestapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import java.util.*

class BluetoothLeService : Service() {

    private val binder: IBinder = LocalBinder()
    private var bluetoothGatt: BluetoothGatt? = null
    private val TAG = "BluetoothLeService"

    // UUIDs for standard BLE characteristics
    private val UUID_HEART_RATE_MEASUREMENT = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb")

    // Inner class for the client to get the service
    inner class LocalBinder : Binder() {
        fun getService(): BluetoothLeService {
            return this@BluetoothLeService
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    // Implement BluetoothGattCallback for handling GATT events
    private val gattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            // Handle connection state changes
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                broadcastUpdate(ACTION_GATT_CONNECTED)
                // Attempts to discover services after successful connection.

                bluetoothGatt?.discoverServices()
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                broadcastUpdate(ACTION_GATT_DISCONNECTED)
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            // Handle service discovery
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED)
            } else {
                Log.w(TAG, "onServicesDiscovered received: $status")
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            // Handle characteristic read
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            // Handle characteristic changes (notifications)
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
        }
    }

    // Broadcast helper method
    private fun broadcastUpdate(action: String) {
        val intent = Intent(action)
        sendBroadcast(intent)
    }

    // Broadcast helper method for data
    private fun broadcastUpdate(action: String, characteristic: BluetoothGattCharacteristic) {
        val intent = Intent(action)

        if (UUID_HEART_RATE_MEASUREMENT == characteristic.uuid) {
            // Special handling for Heart Rate Measurement profile
            val format = BluetoothGattCharacteristic.FORMAT_UINT8
            val heartRate = characteristic.getIntValue(format, 1)
            intent.putExtra(EXTRA_DATA, heartRate.toString())
        } else {
            // For other profiles, write the data formatted in HEX
            val data = characteristic.value
            if (data != null && data.isNotEmpty()) {
                val stringBuilder = StringBuilder(data.size)
                for (byteChar in data) {
                    stringBuilder.append(String.format("%02X ", byteChar))
                }
                intent.putExtra(EXTRA_DATA, String(data) + "\n" + stringBuilder.toString())
            }
        }

        sendBroadcast(intent)
    }

    // Method to initiate reading of a characteristic
    @SuppressLint("MissingPermission")
    fun readCharacteristic(characteristic: BluetoothGattCharacteristic) {
        bluetoothGatt?.readCharacteristic(characteristic)
    }

    // Method to enable or disable notifications for a characteristic
    @SuppressLint("MissingPermission")
    fun setCharacteristicNotification(characteristic: BluetoothGattCharacteristic, enabled: Boolean) {
        bluetoothGatt?.setCharacteristicNotification(characteristic, enabled)

        if (UUID_HEART_RATE_MEASUREMENT == characteristic.uuid) {
            val descriptor = characteristic.getDescriptor(UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb"))
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            bluetoothGatt?.writeDescriptor(descriptor)
        }
    }

    // Method to get the supported GATT services
    fun getSupportedGattServices(): List<BluetoothGattService>? {
        return bluetoothGatt?.services
    }
    fun getGattCallback(): BluetoothGattCallback {
        return gattCallback
    }
    companion object {
        const val ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED"
        const val ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED"
        const val ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED"
        const val ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE"
        const val EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA"
    }

    // Additional methods for connecting, disconnecting, etc., can be added as needed
}