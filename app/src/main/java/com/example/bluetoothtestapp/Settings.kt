package com.example.bluetoothtestapp

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class Settings : AppCompatActivity() {

    private lateinit var s1TempMin: EditText
    private lateinit var s1TempMax: EditText
    private lateinit var s1HumMin: EditText
    private lateinit var s1HumMax: EditText

    private lateinit var s2TempMin: EditText
    private lateinit var s2TempMax: EditText
    private lateinit var s2HumMin: EditText
    private lateinit var s2HumMax: EditText

    private lateinit var s3TempMin: EditText
    private lateinit var s3TempMax: EditText
    private lateinit var s3HumMin: EditText
    private lateinit var s3HumMax: EditText

    private lateinit var s4TempMin: EditText
    private lateinit var s4TempMax: EditText
    private lateinit var s4HumMin: EditText
    private lateinit var s4HumMax: EditText

    private lateinit var s5TempMin: EditText
    private lateinit var s5TempMax: EditText
    private lateinit var s5HumMin: EditText
    private lateinit var s5HumMax: EditText

    private lateinit var s6TempMin: EditText
    private lateinit var s6TempMax: EditText
    private lateinit var s6HumMin: EditText
    private lateinit var s6HumMax: EditText

    private lateinit var s7TempMin: EditText
    private lateinit var s7TempMax: EditText
    private lateinit var s7HumMin: EditText
    private lateinit var s7HumMax: EditText

    private lateinit var s8TempMin: EditText
    private lateinit var s8TempMax: EditText
    private lateinit var s8HumMin: EditText
    private lateinit var s8HumMax: EditText

    private lateinit var saveButton: Button
    private lateinit var sensorEditTexts: MutableList<MutableList<EditText>>
    private lateinit var clearSettings: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize sensorEditTexts before accessing it
        sensorEditTexts = mutableListOf()

        s1TempMin = findViewById(R.id.s1TempMin)
        s1TempMax = findViewById(R.id.s1TempMax)
        s1HumMin = findViewById(R.id.s1HumMin)
        s1HumMax = findViewById(R.id.s1HumMax)

        // Find and initialize EditText fields for sensor 2
        s2TempMin = findViewById(R.id.s2TempMin)
        s2TempMax = findViewById(R.id.s2TempMax)
        s2HumMin = findViewById(R.id.s2HumMin)
        s2HumMax = findViewById(R.id.s2HumMax)

        // Find and initialize EditText fields for sensor 3
        s3TempMin = findViewById(R.id.s3TempMin)
        s3TempMax = findViewById(R.id.s3TempMax)
        s3HumMin = findViewById(R.id.s3HumMin)
        s3HumMax = findViewById(R.id.s3HumMax)

        // Find and initialize EditText fields for sensor 4
        s4TempMin = findViewById(R.id.s4TempMin)
        s4TempMax = findViewById(R.id.s4TempMax)
        s4HumMin = findViewById(R.id.s4HumMin)
        s4HumMax = findViewById(R.id.s4HumMax)

        // Find and initialize EditText fields for sensor 5
        s5TempMin = findViewById(R.id.s5TempMin)
        s5TempMax = findViewById(R.id.s5TempMax)
        s5HumMin = findViewById(R.id.s5HumMin)
        s5HumMax = findViewById(R.id.s5HumMax)

        // Find and initialize EditText fields for sensor 6
        s6TempMin = findViewById(R.id.s6TempMin)
        s6TempMax = findViewById(R.id.s6TempMax)
        s6HumMin = findViewById(R.id.s6HumMin)
        s6HumMax = findViewById(R.id.s6HumMax)

        // Find and initialize EditText fields for sensor 7
        s7TempMin = findViewById(R.id.s7TempMin)
        s7TempMax = findViewById(R.id.s7TempMax)
        s7HumMin = findViewById(R.id.s7HumMin)
        s7HumMax = findViewById(R.id.s7HumMax)

        // Find and initialize EditText fields for sensor 8
        s8TempMin = findViewById(R.id.s8TempMin)
        s8TempMax = findViewById(R.id.s8TempMax)
        s8HumMin = findViewById(R.id.s8HumMin)
        s8HumMax = findViewById(R.id.s8HumMax)

        // Add EditTexts for each sensor to sensorEditTexts
        sensorEditTexts?.apply {
            add(mutableListOf(s1TempMin, s1TempMax, s1HumMin, s1HumMax))
            add(mutableListOf(s2TempMin, s2TempMax, s2HumMin, s2HumMax))
            add(mutableListOf(s3TempMin, s3TempMax, s3HumMin, s3HumMax))
            add(mutableListOf(s4TempMin, s4TempMax, s4HumMin, s4HumMax))
            add(mutableListOf(s5TempMin, s5TempMax, s5HumMin, s5HumMax))
            add(mutableListOf(s6TempMin, s6TempMax, s6HumMin, s6HumMax))
            add(mutableListOf(s7TempMin, s7TempMax, s7HumMin, s7HumMax))
            add(mutableListOf(s8TempMin, s8TempMax, s8HumMin, s8HumMax))
        }
        clearSettings = findViewById(R.id.clearSettings)
        // Set placeholder text for all EditText fields
        sensorEditTexts.forEach { sensorList ->
            sensorList.forEach { editText ->
                editText.setText("-1") // Set '-' as placeholder text
            }
        }

        loadThresholds()

        saveButton = findViewById(R.id.saveButton)
        // Save thresholds when the save button is clicked
        saveButton.setOnClickListener {
            if(checkInput()){
                saveThresholds()
            }
        clearSettings.setOnClickListener{
            resetSettings()
        }
            // Optionally, you can show a confirmation message to the user
        }
    }

    private fun saveThresholds() {
        val sharedPreferences: SharedPreferences =
            getSharedPreferences("ThresholdPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        fun saveThreshold(sensorNum: Int, valueType: String, minText: String, maxText: String) {
            val min = minText.toIntOrNull()
            val max = maxText.toIntOrNull()

            // Convert Int values to String, or use "-" if they cannot be parsed
            val minString = min?.toString() ?: "-1"
            val maxString = max?.toString() ?: "-1"

            editor.putString("s${sensorNum}${valueType}Min", minString)
            editor.putString("s${sensorNum}${valueType}Max", maxString)
        }


        // Save thresholds for sensor 1
        saveThreshold(1, "Temp", s1TempMin.text.toString(), s1TempMax.text.toString())
        saveThreshold(1, "Hum", s1HumMin.text.toString(), s1HumMax.text.toString())

        // Save thresholds for sensor 2
        saveThreshold(2, "Temp", s2TempMin.text.toString(), s2TempMax.text.toString())
        saveThreshold(2, "Hum", s2HumMin.text.toString(), s2HumMax.text.toString())

        // Save thresholds for sensor 3
        saveThreshold(3, "Temp", s3TempMin.text.toString(), s3TempMax.text.toString())
        saveThreshold(3, "Hum", s3HumMin.text.toString(), s3HumMax.text.toString())

        // Save thresholds for sensor 4
        saveThreshold(4, "Temp", s4TempMin.text.toString(), s4TempMax.text.toString())
        saveThreshold(4, "Hum", s4HumMin.text.toString(), s4HumMax.text.toString())

        // Save thresholds for sensor 5
        saveThreshold(5, "Temp", s5TempMin.text.toString(), s5TempMax.text.toString())
        saveThreshold(5, "Hum", s5HumMin.text.toString(), s5HumMax.text.toString())

        // Save thresholds for sensor 6
        saveThreshold(6, "Temp", s6TempMin.text.toString(), s6TempMax.text.toString())
        saveThreshold(6, "Hum", s6HumMin.text.toString(), s6HumMax.text.toString())

        // Save thresholds for sensor 7
        saveThreshold(7, "Temp", s7TempMin.text.toString(), s7TempMax.text.toString())
        saveThreshold(7, "Hum", s7HumMin.text.toString(), s7HumMax.text.toString())

        // Save thresholds for sensor 8
        saveThreshold(8, "Temp", s8TempMin.text.toString(), s8TempMax.text.toString())
        saveThreshold(8, "Hum", s8HumMin.text.toString(), s8HumMax.text.toString())

        editor.apply()
    }


    private fun loadThresholds() {
        val sharedPreferences: SharedPreferences =
            getSharedPreferences("ThresholdPrefs", Context.MODE_PRIVATE)

        for (sensorNum in 1..8) {
            val tempMinStr = sharedPreferences.getString("s$sensorNum" + "TempMin", "-1")
            val tempMaxStr = sharedPreferences.getString("s$sensorNum" + "TempMax", "-1")
            val humMinStr = sharedPreferences.getString("s$sensorNum" + "HumMin", "-1")
            val humMaxStr = sharedPreferences.getString("s$sensorNum" + "HumMax", "-1")

            // Update UI with thresholds
            updateThresholdUI(sensorNum, tempMinStr, tempMaxStr, humMinStr, humMaxStr)
        }
    }

    private fun updateThresholdUI(
        sensorNum: Int,
        tempMinStr: String?,
        tempMaxStr: String?,
        humMinStr: String?,
        humMaxStr: String?
    ) {
        // Assuming you have TextViews named accordingly, update them with the retrieved values
        // Replace `s1TempMin`, `s1TempMax`, etc., with your actual TextView IDs

        findViewById<TextView>(resources.getIdentifier("s$sensorNum" + "TempMin", "id", packageName)).text =
            if (tempMinStr != "-1") tempMinStr else "-1"

        findViewById<TextView>(resources.getIdentifier("s$sensorNum" + "TempMax", "id", packageName)).text =
            if (tempMaxStr != "-1") tempMaxStr else "-1"

        findViewById<TextView>(resources.getIdentifier("s$sensorNum" + "HumMin", "id", packageName)).text =
            if (humMinStr != "-1") humMinStr else "-1"

        findViewById<TextView>(resources.getIdentifier("s$sensorNum" + "HumMax", "id", packageName)).text =
            if (humMaxStr != "-1") humMaxStr else "-1"
    }

    private fun checkInput(): Boolean {
        val thresholds = listOf(
            Pair("Temperature min must be at least 30") { value: String ->
                try {
                    val intValue = value.toIntOrNull()
                    intValue != null && intValue >= 30
                } catch (e: NumberFormatException) {
                    true
                }
            },
            Pair("Temperature max cannot exceed 85") { value: String ->
                try {
                    val intValue = value.toIntOrNull()
                    intValue != null && intValue <= 85
                } catch (e: NumberFormatException) {
                    true
                }
            },
            Pair("Humidity min must be at least 20") { value: String ->
                try {
                    val intValue = value.toIntOrNull()
                    intValue != null && intValue >= 20
                } catch (e: NumberFormatException) {
                    true
                }
            },
            Pair("Humidity max cannot exceed 75") { value: String ->
                try {
                    val intValue = value.toIntOrNull()
                    intValue != null && intValue <= 75
                } catch (e: NumberFormatException) {
                    true
                }
            }
        )

        // Assuming you have EditTexts named accordingly, get their values and validate
        // Replace `s1TempMin`, `s1TempMax`, etc., with your actual EditText IDs
        val editTextValues = listOf(
            s1TempMin.text.toString(),
            s1TempMax.text.toString(),
            s1HumMin.text.toString(),
            s1HumMax.text.toString()
            // Repeat the above for other EditTexts
        )

        // Iterate over each threshold and check its corresponding EditText value
        for ((index, threshold) in thresholds.withIndex()) {
            val editTextValue = editTextValues[index]
            // Skip if the editTextValue is "-1"
            if (editTextValue != "-1") {
                if (!threshold.second(editTextValue)) {
                    // If any threshold fails, show an error message
                    Toast.makeText(this, threshold.first, Toast.LENGTH_SHORT).show()
                    return false
                }
            }
        }


        // All thresholds passed
        return true
    }



    private fun resetSettings() {
        try {// Reset EditText fields for sensor 1
            s1TempMin.setText("-1")
            s1TempMax.setText("-1")
            s1HumMin.setText("-1")
            s1HumMax.setText("-1")

            // Reset EditText fields for sensor 2
            s2TempMin.setText("-1")
            s2TempMax.setText("-1")
            s2HumMin.setText("-1")
            s2HumMax.setText("-1")

            // Reset EditText fields for sensor 3
            s3TempMin.setText("-1")
            s3TempMax.setText("-1")
            s3HumMin.setText("-1")
            s3HumMax.setText("-1")

            // Reset EditText fields for sensor 4
            s4TempMin.setText("-1")
            s4TempMax.setText("-1")
            s4HumMin.setText("-1")
            s4HumMax.setText("-1")

            // Reset EditText fields for sensor 5
            s5TempMin.setText("-1")
            s5TempMax.setText("-1")
            s5HumMin.setText("-1")
            s5HumMax.setText("-1")

            // Reset EditText fields for sensor 6
            s6TempMin.setText("-1")
            s6TempMax.setText("-1")
            s6HumMin.setText("-1")
            s6HumMax.setText("-1")

            // Reset EditText fields for sensor 7
            s7TempMin.setText("-1")
            s7TempMax.setText("-1")
            s7HumMin.setText("-1")
            s7HumMax.setText("-1")

            // Reset EditText fields for sensor 8
            s8TempMin.setText("-1")
            s8TempMax.setText("-1")
            s8HumMin.setText("-1")
            s8HumMax.setText("-1")

            saveThresholds()
            Log.d("reset","Resetting")
        } catch (e: Exception) {
            Log.e("reset","Not Resetting")
        }
    }

    fun backToMain(view: View) {
        finish() // Close the settings activity and go back to the main activity
    }


}