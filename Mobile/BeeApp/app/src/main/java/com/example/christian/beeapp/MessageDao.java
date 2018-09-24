package com.example.christian.beeapp;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface MessageDao {

    @Query("select * from messages order by receiving_date desc")
    List<Message> getAllMessagesDesc();

    @Query("select * from messages order by receiving_date asc")
    List<Message> getAllMessagesAsc();

    @Insert
    void insertAllMessages(Message... messages);

    @Insert
    void insertMessage(Message message);

    @Delete
    void deleteMessage(Message message);

}
