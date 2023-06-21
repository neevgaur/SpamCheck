package com.gaurneev.spamdetector

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.CallLog
import android.provider.Telephony
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.gaurneev.spamdetector.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.popup_layout.*
import kotlinx.android.synthetic.main.popup_layout.view.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.internal.notify
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException


class MainActivity : AppCompatActivity() {

    private lateinit var telephonyManager: TelephonyManager
    lateinit var MainActivityBinding: ActivityMainBinding
    private val incomingCallReceiver = IncomingCallReceiver()
    private var callerNum :String? =null

//    private val customPhoneStateListener = CustomPhoneStateListener()

    private val phoneStateListener = object : PhoneStateListener(){
        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            super.onCallStateChanged(state, phoneNumber)
            when (state) {
                TelephonyManager.CALL_STATE_RINGING -> {
                    val incomingNumber = phoneNumber ?: "Unknown number"
                    Log.d("MainActivity", "Incoming call from $incomingNumber")
                    callerNum=incomingNumber

                    if (callerNum != null){
                        Log.d("Caller1", callerNum!!)
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

//        val builder = AlertDialog.Builder(this)
//        builder.setTitle("Incoming Call")
//        builder.setMessage("You have a new call")
//        builder.setPositiveButton("Answer") { dialog, which ->
//            Log.d("Callevent","Answered")
//        }
//        builder.setNegativeButton("Reject") { dialog, which ->
//            Log.d("Callevent","Rejected")
//        }
//        dialog = builder.create()
//
//        val incomingCallReceiver = object: BroadcastReceiver(){
//            override fun onReceive(context: Context?, intent: Intent?) {
//                if (intent?.action==TelephonyManager.ACTION_PHONE_STATE_CHANGED){
//                    val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
//                    if (state == TelephonyManager.EXTRA_STATE_RINGING){
//                        dialog.show()
//                    }
//                }
//            }
//        }
//        val filter = IntentFilter()
//        filter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
//        registerReceiver(incomingCallReceiver, filter)



//        telephonyManager = getSystemService(Context.TELECOM_SERVICE) as TelephonyManager
//        telephonyManager.listen(customPhoneStateListener,PhoneStateListener.LISTEN_CALL_STATE)
//
//        val incomingNumber = customPhoneStateListener.getIncomingNum()
//
//        popNum.text=incomingNumber
//
//        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
//        val phoneStateListener = CustomPhoneStateListener()
//
//        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
//
//        val callerNum = phoneStateListener.getIncomingNum()
//
//        if (callerNum != null){
//            Log.d("aakar",callerNum)
//        }

        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)

        if (callerNum != null){
            Log.d("Caller", callerNum!!)
        }

        val intentFilter = IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
        registerReceiver(incomingCallReceiver, intentFilter)

        MainActivityBinding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(MainActivityBinding.root)

        supportActionBar?.hide()



        MainActivityBinding.message.setOnClickListener {
            startActivity(Intent(this, messageActivity::class.java))
        }

        MainActivityBinding.imPr.setOnClickListener {
            startActivity(Intent(this, profile::class.java))
        }

        val jsonArray = JSONArray()

        contactList.layoutManager = LinearLayoutManager(this)

        contactList1.layoutManager = LinearLayoutManager(this)

        var count = 0

        val contact_list: MutableList<com.gaurneev.spamdetector.CallLog> = ArrayList()

        val spam_list: MutableList<com.gaurneev.spamdetector.spamCalllog> = ArrayList()

        fetchCalllog(jsonArray, contact_list, count)


        MainActivityBinding.spCl.setOnClickListener {
            MainActivityBinding.card1.visibility=View.VISIBLE
            MainActivityBinding.card.visibility = View.INVISIBLE
            MainActivityBinding.gnLi.visibility = View.INVISIBLE
            MainActivityBinding.spLi.visibility = View.VISIBLE
            contact_list.clear()
            spamApicall(spam_list,jsonArray)
            MainActivityBinding.contactList1.adapter?.notifyDataSetChanged()
        }

        MainActivityBinding.gnCl.setOnClickListener {
            MainActivityBinding.card1.visibility = View.INVISIBLE
            MainActivityBinding.card.visibility = View.VISIBLE
            MainActivityBinding.gnLi.visibility = View.VISIBLE
            MainActivityBinding.spLi.visibility = View.INVISIBLE
            MainActivityBinding.progressBar3.visibility=View.VISIBLE
            spam_list.clear()
            fetchCalllog(jsonArray, contact_list, count)
        }

    }



    class CustomPhoneStateListener : PhoneStateListener(){

        private var incomingNumber: String? = null

        override fun onCallStateChanged(state: Int, incomingNumber: String?) {
            super.onCallStateChanged(state, incomingNumber)
            when (state) {
                TelephonyManager.CALL_STATE_RINGING -> {
//                    // Store the incoming phone number
//                    phoneNumber = incomingNumber
//                    phoneNumber?.let { Log.d("PhoneAAJa", it) }
                    this.incomingNumber=incomingNumber
                }
                TelephonyManager.CALL_STATE_IDLE -> {
                    // Clear the stored phone number when the call ends
//                    phoneNumber = null
//                    phoneNumber?.let { Log.d("PhoneAAJa", it) }
                    this.incomingNumber=null
                }
            }
        }

        fun getIncomingNum(): String?{
            return incomingNumber
        }
    }

    class IncomingCallReceiver : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED){
                val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
                if (state == TelephonyManager.EXTRA_STATE_RINGING){
                    val windowManager = context?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                    val layoutParams = WindowManager.LayoutParams(
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        PixelFormat.TRANSLUCENT)
                    val popupView = LayoutInflater.from(context).inflate(R.layout.popup_layout, null)
                    windowManager.addView(popupView, layoutParams)
                    popupView.closeBtn.setOnClickListener {
                        popupView.visibility=View.GONE
//                        phoneNumber?.let { Log.d("PhoneAAJa", it) }
//                        popupView.popNum.text= phoneNumber
//                        Log.d("aakar",popupView.popNum.text.toString())
                    }
                }
            }
        }
    }



    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(incomingCallReceiver)
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
//        telephonyManager.listen(customPhoneStateListener,PhoneStateListener.LISTEN_NONE)
    }

    override fun onResume() {
        super.onResume()

        // Show popup
        val window = window
        window?.let {
            it.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            it.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
            it.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
            it.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }
    }

    override fun onPause() {
        super.onPause()

        // Dismiss popup
        val window = window
        window?.clearFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )
    }


    @SuppressLint("Range")
    private fun fetchCalllog(jsonArray: JSONArray, contact_list: MutableList<com.gaurneev.spamdetector.CallLog>, count: Int) {
        var count1 = count
        val contact = applicationContext.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            null,
            null,
            null,
            CallLog.Calls.DEFAULT_SORT_ORDER
        )
        if (contact != null && contact.moveToFirst()) {
            do {
                val number = contact.getString(contact.getColumnIndex(CallLog.Calls.NUMBER))
                val duration = contact.getString(contact.getColumnIndex(CallLog.Calls.DURATION))
                val type = when (contact.getInt(contact.getColumnIndex(CallLog.Calls.TYPE))) {
                    CallLog.Calls.INCOMING_TYPE -> "Incoming"
                    CallLog.Calls.OUTGOING_TYPE -> "Outgoing"
                    CallLog.Calls.MISSED_TYPE -> "Missed"
                    else -> "Declined"
                }
    //                        val type = contact.getInt(contact.getColumnIndex(CallLog.Calls.TYPE)).toString()
                val obj = CallLog(number, duration, type)

                jsonArray.put(number)
                contact_list.add(obj)
                count1++
            } while (contact.moveToNext() && count1 <= 5)
        }
        Log.d("response", contact_list.toString())

        contactList.adapter = callLogAdapter(contact_list, this)

        contact?.close()
    }

    private fun spamApicall(spam_list: MutableList<com.gaurneev.spamdetector.spamCalllog>, jsonArray: JSONArray) {
        val url = "https://kavach-backend.onrender.com/users/Report"
        // Create an instance of OkHttpClient
        val client = OkHttpClient()

        // Create a JSON object to store the data to be sent
        val jsonObject = JSONObject()
        jsonObject.put("Phone_no", jsonArray)

        // Create a RequestBody object with the JSON object as the request body
        val requestBody =
            RequestBody.create("application/json".toMediaTypeOrNull(), jsonObject.toString())

        // Create a request object with the URL and request body
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        // Execute the request using the client and handle the response
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.d("naman2", "failed data")
            }

            @SuppressLint("Range")
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                MainActivityBinding.progressBar3.visibility=View.INVISIBLE
                val responseJson = JSONArray(response.body?.string())
                Log.d("naman2", "success")
                Log.d("data response", "Api response $responseJson")


                val spamContacts = applicationContext.contentResolver.query(
                    CallLog.Calls.CONTENT_URI,
                    null,
                    null,
                    null,
                    CallLog.Calls.DEFAULT_SORT_ORDER
                )
                var i = 0
                if (spamContacts != null && spamContacts.moveToFirst()) {
                    do{
                        if (responseJson.getJSONObject(i).getInt("score") >= 30) {
                           val number = spamContacts.getString(spamContacts.getColumnIndexOrThrow(CallLog.Calls.NUMBER))
                            val duration = spamContacts.getString(spamContacts.getColumnIndexOrThrow(CallLog.Calls.DURATION))
                            val type = when(spamContacts.getInt(spamContacts.getColumnIndexOrThrow(CallLog.Calls.TYPE))){
                                CallLog.Calls.INCOMING_TYPE -> "Incoming"
                                CallLog.Calls.OUTGOING_TYPE -> "Outgoing"
                                CallLog.Calls.MISSED_TYPE -> "Missed"
                            else -> "Declined"
                            }

                            val risk= responseJson.getJSONObject(i).getInt("score")

//                        val type = contact.getInt(contact.getColumnIndex(CallLog.Calls.TYPE)).toString()
                            val obj = com.gaurneev.spamdetector.spamCalllog(number,duration,type,risk)
                            val nb = obj.num
                            val tp= obj.type
                            val rk= risk
                            spam_list.add(obj)
                        }
                        i++
                    }while (spamContacts.moveToNext() && i <= 4)
                }

                runOnUiThread {
                    contactList1.adapter = ContactAdapter1(spam_list, this@MainActivity)
                }

                if (spamContacts != null) {
                    spamContacts.close()
                }
            }
        })
    }

}

