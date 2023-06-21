package com.gaurneev.spamdetector

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException
import java.sql.Time
import java.util.*
import kotlin.collections.ArrayList

class callLogAdapter(items: List<CallLog>, ctx: Context):
    RecyclerView.Adapter<callLogAdapter.ViewHolder>() {

    private var list = items
    private var context = ctx
    private var demo: ArrayList<String> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.calllog, parent, false))
    }

    override fun onBindViewHolder(holder: callLogAdapter.ViewHolder, position: Int) {
        val List = list[position]
        holder.number.text = list[position].num
        holder.type.text = list[position].type
        demo.add(list[position].num)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {
        val number = v.findViewById<TextView>(R.id.tv_num)!!
        val type = v.findViewById<TextView>(R.id.tv_type)
        var rpbtn: CardView

        init {
            rpbtn = v.findViewById(R.id.btnReport)
            rpbtn.setOnClickListener {
                val indices = adapterPosition
                reportDialog(it, indices)
            }
        }

        private fun reportDialog(it: View?, indices: Int) {
            var rep = arrayOf(
                "Abusive Language",
                "Use of inapropriate words",
                "Use of Hateful Words",
                "Use of threat languages",
                "Spam or misleading words",
                "Child Abuse"
            )
            var selectedIndex = 0
            var selReport= rep[selectedIndex]

            MaterialAlertDialogBuilder(context)
                .setTitle("Report Call")
                .setSingleChoiceItems(rep,selectedIndex) {dialog, which ->
                selectedIndex = which
                selReport = rep[which]
                }
                .setPositiveButton("Report"){dialog, which->
                    Toast.makeText(context,"$selReport content is reported",Toast.LENGTH_SHORT).show()
                    Log.d("Report check", demo.toString())
                    Log.d("Report check1", demo[indices].toString())

                    val url = "https://kavach-backend.onrender.com/users/call"
                    val client = OkHttpClient()
                    val jsonObject = JSONObject()
                    jsonObject.put("Phone_no",demo[indices].toString())
                    val requestBody =
                        RequestBody.create("application/json".toMediaTypeOrNull(), jsonObject.toString())
                    val request = Request.Builder()
                        .url(url)
                        .post(requestBody)
                        .build()

                    client.newCall(request).enqueue(object : okhttp3.Callback{
                        override fun onFailure(call: Call, e: IOException) {
                            Log.d("report api","fail")
                        }

                        override fun onResponse(call: Call, response: Response) {
                            Log.d("report api","success")
                        }
                    })
                }
                .setNeutralButton("Cancel"){dialog,which->

                }
                .show()
        }

        override fun onClick(v: View?) {
            Log.d("popup Check", "onclick: " + adapterPosition)
        }
    }
}