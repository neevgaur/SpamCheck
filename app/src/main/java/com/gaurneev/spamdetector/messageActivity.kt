package com.gaurneev.spamdetector

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gaurneev.spamdetector.databinding.ActivityMessageBinding
import kotlinx.android.synthetic.main.activity_message.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class messageActivity : AppCompatActivity() {
    lateinit var messageActivityBinding:ActivityMessageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        messageActivityBinding=ActivityMessageBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(messageActivityBinding.root)


        messageActivityBinding.imCl.setOnClickListener {
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }
        messageActivityBinding.imPr.setOnClickListener {
            startActivity(Intent(this,profile::class.java))
            finish()
        }

        messageActivityBinding.spCl.setOnClickListener {
            messageActivityBinding.card1.visibility=View.VISIBLE
            messageActivityBinding.card1.visibility=View.VISIBLE
            messageActivityBinding.card.visibility= View.INVISIBLE
            messageActivityBinding.gnLi.visibility= View.INVISIBLE
            messageActivityBinding.spLi.visibility= View.VISIBLE
        }

        messageActivityBinding.gnCl.setOnClickListener {
            messageActivityBinding.card1.visibility= View.INVISIBLE
            messageActivityBinding.card.visibility= View.VISIBLE
            messageActivityBinding.gnLi.visibility= View.VISIBLE
            messageActivityBinding.spLi.visibility= View.INVISIBLE
        }

        val spam_message_list: MutableList<Message> = ArrayList()



        messageList1.layoutManager=LinearLayoutManager(this)

        val uri = Uri.parse("content://sms/inbox")
        val cursor = contentResolver.query(uri, null, null, null, null)

        val messages = mutableListOf<Message>()
        var messBody = mutableListOf<String>()

        val jsonArray = JSONArray()

        var count =0

        if (cursor != null && cursor.moveToFirst()) {
            while (cursor.moveToNext() && count<50) {
                // Access message details using cursor
                val jsonObject = JSONObject()
                val messageBody = cursor.getString(cursor.getColumnIndexOrThrow("body"))
                val sender = cursor.getString(cursor.getColumnIndexOrThrow("address"))
                val timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("date"))
                messages.add(Message(messageBody, sender, timestamp))
                messBody.add(messageBody)
                Log.d("messCheck",messBody.toString())
                jsonObject.put("data",messageBody)
                jsonArray.put(jsonObject)
                Log.d("rashm",jsonObject.toString())

                Log.d("rashm1",jsonArray.toString())
                count++
            }
        }


        cursor?.close()

        val url = "https://kavach-ml-sms-spam-detect-api.onrender.com/predict_all"
        val client = OkHttpClient()



        val requestBody =
            RequestBody.create("application/json".toMediaTypeOrNull(), jsonArray.toString())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback{
            override fun onFailure(call: Call, e: IOException) {
                Log.d("ashish ml","failed")
            }

            override fun onResponse(call: Call, response: Response) {
               messageActivityBinding.progressBar3.visibility=View.INVISIBLE
                val responseJson = JSONArray(response.body?.string())
                Log.d("mlCheck",responseJson.getJSONObject(0).getString("result"))

                //responseJson.getJSONArray(0).getString(1)

                val spamMessage = contentResolver.query(uri, null, null, null, null)

                var i=0
                if (spamMessage != null && spamMessage.moveToFirst()) {
                    while (spamMessage.moveToNext()&&i<50) {
                        // Access message details using cursor
                        if (responseJson.getJSONObject(i).getString("result") == "spam"){
                            val messageBody = spamMessage.getString(spamMessage.getColumnIndexOrThrow("body"))
                            val sender = spamMessage.getString(spamMessage.getColumnIndexOrThrow("address"))
                            val timestamp = spamMessage.getLong(spamMessage.getColumnIndexOrThrow("date"))
                            spam_message_list.add(Message(messageBody, sender, timestamp))
                            Log.d("ckeck ml",Message(messageBody, sender, timestamp).toString())
//                            jsonObject.put("data",messageBody)
                        }
                        i++
                    }
                }
                Log.d("messageCheck",spam_message_list.toString())
                runOnUiThread { messageList1.adapter= MessageAdapter(this@messageActivity,spam_message_list) }
                spamMessage?.close()
            }
        })


        val messageAdapter = MessageAdapter(this,messages)
        messageList.adapter = messageAdapter
        messageList.layoutManager = LinearLayoutManager(this)
    }




//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == 1) {
//            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                readSms()
//            }
//        }
//    }
}