package com.example.christian.beeapp;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "messages")
public class Message {

    @ColumnInfo(name = "sender_number")
    private String number;

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "receiving_date")
    private String date;

    @ColumnInfo(name = "weight")
    private String weight;

    @ColumnInfo(name = "temperature")
    private String temperature;

    @ColumnInfo(name = "humidity")
    private String humidity;

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
