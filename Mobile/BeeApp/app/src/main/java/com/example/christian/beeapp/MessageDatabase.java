package com.example.christian.beeapp;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {Message.class}, version = 1)
public abstract class MessageDatabase extends RoomDatabase{

    public abstract  MessageDao messageDao();
}
