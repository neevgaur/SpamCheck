package com.gaurneev.spamdetector

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.gaurneev.spamdetector.databinding.ActivityMessageBinding
import com.gaurneev.spamdetector.databinding.ActivityProfileBinding

class profile : AppCompatActivity() {
    lateinit var profileBinding: ActivityProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        profileBinding = ActivityProfileBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(profileBinding.root)

        profileBinding.imCl.setOnClickListener {
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }
        profileBinding.message.setOnClickListener {
            startActivity(Intent(this,messageActivity::class.java))
            finish()
        }
    }
}