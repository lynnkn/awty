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
        startBtn.isEnabled = false
        messageET.isEnabled = true
        phoneET.isEnabled = true
        minutesET.isEnabled = true

        // listeners for all the text edits
        messageET.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s:CharSequence, start:Int, before:Int, count:Int) {
                startBtn.isEnabled =
                    isValidInput(messageET.text.toString(), phoneET.text.toString(), minutesET.text.toString())
            }
            override fun beforeTextChanged(s:CharSequence, start:Int, count:Int,
                                           after:Int) {
                // TODO Auto-generated method stub
            }
            override fun afterTextChanged(s: Editable) {
                // TODO Auto-generated method stub
            }
        })
        phoneET.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s:CharSequence, start:Int, before:Int, count:Int) {
                startBtn.isEnabled =
                    isValidInput(messageET.text.toString(), phoneET.text.toString(), minutesET.text.toString())
            }
            override fun beforeTextChanged(s:CharSequence, start:Int, count:Int,
                                           after:Int) {
                // TODO Auto-generated method stub
            }
            override fun afterTextChanged(s: Editable) {
                // TODO Auto-generated method stub
            }
        })
        minutesET.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s:CharSequence, start:Int, before:Int, count:Int) {
                startBtn.isEnabled =
                    isValidInput(messageET.text.toString(), phoneET.text.toString(), minutesET.text.toString())
            }
            override fun beforeTextChanged(s:CharSequence, start:Int, count:Int,
                                           after:Int) {
                // TODO Auto-generated method stub
            }
            override fun afterTextChanged(s: Editable) {
                // TODO Auto-generated method stub
            }
        })

        // set up button
        startBtn.setOnClickListener{
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

    private fun sendMessages(alarm: AlarmManager, message: String, phone: String, min: Int) {
        // register alarm to go off
        val time = System.currentTimeMillis() //+ (min * 60000)
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
            return false
        }

        // checking regex pattern on phone number
        val pattern: Pattern = Pattern.compile("\\(\\d{3}\\) \\d{3}-\\d{4}")
        val matcher: Matcher = pattern.matcher(num)

        return message.isNotEmpty() && matcher.matches() && min.toDouble() > 0 && (min.toDouble() % 1 == 0.0)
    }
}