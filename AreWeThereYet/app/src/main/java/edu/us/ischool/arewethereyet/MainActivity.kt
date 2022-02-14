package edu.us.ischool.arewethereyet

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.regex.Matcher
import java.util.regex.Pattern

class MainActivity : AppCompatActivity() {
    var isSending: Boolean = false
    lateinit var pendingIntent: PendingIntent

    class IntentListener : BroadcastReceiver() {
        init {
            Log.i("IntentListener", "Intent listener created")
        }

        override fun onReceive(context: Context?, intent: Intent?) {
            val message = intent?.extras?.getString("EXTRA_MESSAGE")
            val phone = intent?.extras?.getString("EXTRA_PHONE")
            Log.i("IntentListener", "We received an intent: $message : $phone")
            Toast.makeText(context?.getApplicationContext(), "${phone}:${message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // get values of views
        val messageET: EditText = findViewById(R.id.message)
        val phoneET: EditText = findViewById(R.id.phone)
        val minutesET: EditText = findViewById(R.id.minutes)
        val startBtn: Button = findViewById(R.id.start_btn)

        // setting up receiver
        val receiver = IntentListener()
        val intFilter = IntentFilter()
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        // listening for intents
        intFilter.addAction("edu.us.ischool.NAG")
        registerReceiver(receiver, intFilter)

        // initially disable button and enable edit texts
        messageET.isEnabled = true
        phoneET.isEnabled = true
        minutesET.isEnabled = true

        // set up button
        startBtn.setOnClickListener{

            if (isValidInput(messageET.text.toString(), phoneET.text.toString(), minutesET.text.toString())) {
                if (isSending) {
                    messageET.isEnabled = true
                    phoneET.isEnabled = true
                    minutesET.isEnabled = true

                    if (alarmManager != null) {
                        stopMessages(alarmManager)
                    }
                    startBtn.text = "Start"
                } else {
                    messageET.isEnabled = false;
                    phoneET.isEnabled = false;
                    minutesET.isEnabled = false;

                    if (alarmManager != null) {
                        sendMessages(alarmManager, messageET.text.toString(), phoneET.text.toString(), minutesET.text.toString().toInt())
                    }
                    startBtn.text = "Stop"
                }
            }
        }
    }

    private fun sendMessages(alarm: AlarmManager, message: String, phone: String, min: Int) {
        // register alarm to go off
        val time = System.currentTimeMillis() + (min * 60000)
        Log.i("IntentListener", time.toString())

        val intent = Intent("edu.us.ischool.NAG")
        intent.putExtra("EXTRA_MESSAGE", message)
        intent.putExtra("EXTRA_PHONE", phone)

        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, time, (min * 60000).toLong(), pendingIntent)

        isSending = true
    }

    private fun stopMessages(alarm: AlarmManager) {
        alarm.cancel(pendingIntent)

        isSending = false
    }

    private fun isValidInput(message: String, num: String, min: String) : Boolean {
        if (message.isEmpty() || num.isBlank() || min.isBlank()) {
            Toast.makeText(this, "Inputs cannot be empty", Toast.LENGTH_SHORT).show()
            return false
        }

        // checking regex pattern on phone number
        val pattern: Pattern = Pattern.compile("\\(\\d{3}\\) \\d{3}-\\d{4}")
        val matcher: Matcher = pattern.matcher(num)

        if (message.isEmpty()) {
            Toast.makeText(this, "Message should not be empty", Toast.LENGTH_SHORT).show()
            return false
        } else if (!matcher.matches()) {
            Toast.makeText(this, "Phone number does not match format", Toast.LENGTH_SHORT).show()
            return false
        } else if (!(min.toDouble() > 0 && (min.toDouble() % 1 == 0.0))) {
            Toast.makeText(this, "Minutes have to be above 0 and a whole number", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }
}