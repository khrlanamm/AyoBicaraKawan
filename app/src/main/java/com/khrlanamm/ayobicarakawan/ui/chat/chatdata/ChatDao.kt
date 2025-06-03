package com.khrlanamm.ayobicarakawan.ui.chat.chatdata

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ChatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)

    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): LiveData<List<ChatMessage>>

    // Jika ingin menghapus semua pesan (opsional)
    @Query("DELETE FROM chat_messages")
    suspend fun clearAllMessages()
}
