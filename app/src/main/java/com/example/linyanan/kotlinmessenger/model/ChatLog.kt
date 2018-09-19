package com.example.linyanan.kotlinmessenger.model

class ChatLog(val id: String, val text: String, val fromId: String, val toId: String, val saveTime: Long) {
    constructor():this("","","","",-1)
}