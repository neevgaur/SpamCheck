package com.gaurneev.spamdetector

data class Message (
    val messageBody: String,
    val sender: String,
    val timestamp: Long
        )