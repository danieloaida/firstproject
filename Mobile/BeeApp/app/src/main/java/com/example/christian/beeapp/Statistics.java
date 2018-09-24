package com.example.christian.beeapp;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Statistics extends AppCompatActivity {

    List<Message> databaseMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        databaseMessages = MainActivity.database.messageDao().getAllMessagesAsc();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Statistics");
        LineChart chart = (LineChart) findViewById(R.id.chart);


        if(databaseMessages.size()!=0) {
            List<List<Entry>> wthEntries = getWeightTemperatureHumidityEntries(databaseMessages);

            LineDataSet weightDataSet = new LineDataSet(wthEntries.get(0), "Weight"); // add entries to dataset
            weightDataSet.setColor(Color.RED);
            weightDataSet.setValueTextColor(Color.RED);
            weightDataSet.setDrawCircleHole(false);
            weightDataSet.setDrawCircles(false);
            weightDataSet.setDrawValues(false);


            LineDataSet temperatureDataSet = new LineDataSet(wthEntries.get(1), "Temperature");
            temperatureDataSet.setColor(Color.BLUE);
            temperatureDataSet.setValueTextColor(Color.BLUE); // styling, ...
            temperatureDataSet.setDrawCircleHole(false);
            temperatureDataSet.setDrawCircles(false);
            temperatureDataSet.setDrawValues(false);

            LineDataSet humidityDataSet = new LineDataSet(wthEntries.get(2), "Humidity");
            humidityDataSet.setColor(Color.GREEN);
            humidityDataSet.setValueTextColor(Color.GREEN);
            humidityDataSet.setDrawCircleHole(false);
            humidityDataSet.setDrawCircles(false);
            humidityDataSet.setDrawValues(false);

            LineData lineData = new LineData();
            lineData.addDataSet(weightDataSet);
            lineData.addDataSet(temperatureDataSet);
            lineData.addDataSet(humidityDataSet);
            chart.setData(lineData);
            chart.getDescription().setText("Bee Scale");
            chart.getDescription().setTextSize(15);
            chart.invalidate(); // refresh
        }else{
            Toast.makeText(getApplicationContext(),"No data to show",Toast.LENGTH_SHORT).show();
        }
    }

    private List<List<Entry>> getWeightTemperatureHumidityEntries(List<Message> databaseMessages) {
        List<List<Entry>> entries = new ArrayList<>();
        List<Entry> weightEntries = new ArrayList<>();
        List<Entry> temperatureEntries = new ArrayList<>();
        List<Entry> humidityEntries = new ArrayList<>();

        for (int i = 0; i < databaseMessages.size(); i++) {
            weightEntries.add(new Entry(i+1, Long.parseLong(databaseMessages.get(i).getWeight())));
            temperatureEntries.add(new Entry(i+1, Long.parseLong(databaseMessages.get(i).getTemperature())));
            humidityEntries.add(new Entry(i+1, Long.parseLong(databaseMessages.get(i).getHumidity())));
        }

        entries.add(weightEntries);
        entries.add(temperatureEntries);
        entries.add(humidityEntries);

        return entries;
    }
}
