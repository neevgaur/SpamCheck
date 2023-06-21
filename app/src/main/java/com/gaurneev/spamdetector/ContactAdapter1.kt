package com.gaurneev.spamdetector

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.internal.notify
import org.json.JSONObject
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class ContactAdapter1(items: MutableList<spamCalllog>, ctx: Context): RecyclerView.Adapter<ContactAdapter1.ViewHolder>(){

    private var list = items
    private var context =ctx
    private var gen: ArrayList<String> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactAdapter1.ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.contact_child_1,parent,false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ContactAdapter1.ViewHolder, position: Int) {
        holder.name.text=list[position].num
        holder.number.text=list[position].type
        gen.add(list[position].num)
        var per = list[position].rissk.toString()
        holder.rf.text= "Risk: $per %"
    }

    override fun getItemCount(): Int {
        return list.size
    }


    inner class ViewHolder(v : View) : RecyclerView.ViewHolder(v), View.OnClickListener{
        val name = v.findViewById<TextView>(R.id.tv_name1)!!
        val number = v.findViewById<TextView>(R.id.tv_number1)!!
        var rpbtn1 : CardView
        val rf = v.findViewById<TextView>(R.id.tv_risk)

        init {
            rpbtn1 = v.findViewById(R.id.btnReport1)
            rpbtn1.setOnClickListener {
                var index = adapterPosition
                apiCall(index)
                Toast.makeText(context,"Above contact is Genuine",Toast.LENGTH_SHORT).show()
            }
        }

        private fun apiCall(index: Int) {
            val url = "https://kavach-backend.onrender.com/users/Gen"
            val client = OkHttpClient()
            val jsonObject = JSONObject()
            jsonObject.put("Phone_no",gen[index].toString())
            val requestBody =
                RequestBody.create("application/json".toMediaTypeOrNull(), jsonObject.toString())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()
            client.newCall(request).enqueue(object : okhttp3.Callback{
                override fun onFailure(call: Call, e: IOException) {
                    Log.d("Genuine","fail")
                }

                override fun onResponse(call: Call, response: Response) {
                    Log.d("Genuine","success")
                }
            })
        }

        override fun onClick(v: View?) {
            Log.d("popup Check","onclick: " + adapterPosition)
        }

    }


}