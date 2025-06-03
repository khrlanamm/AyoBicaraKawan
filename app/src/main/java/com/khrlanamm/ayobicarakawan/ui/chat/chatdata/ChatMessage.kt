package com.khrlanamm.ayobicarakawan.ui.chat.chatdata

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val message: String,
    val timestamp: Long,
    val isSentByUser: Boolean // Untuk membedakan pesan user atau balasan (meski dummy, ini bagus untuk masa depan)
)
