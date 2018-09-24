package com.example.christian.beeapp;

import android.arch.persistence.room.Room;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.telephony.SmsManager;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    public static final String SCALE_PHONE_NUMBER_KEY = "SCALE_PHONE_NUMBER";
    public static final String SCALE_PREFERENCES = "SCALE_PREFERENCES";

    private static final String INBOX_URI = "content://sms/inbox";
    private static final String TEXT_SMS = "TURNON";
    protected static final String SPLITER = "-";

    public static String beeScalePhoneNumber = "+40743674322";


    private static MainActivity activity;
    private ArrayList<String> smsList;
    private ListView smsListView;
    private ArrayAdapter<String> adapter;

    public static MessageDatabase database;
    private SharedPreferences sharedPreferences;

    public static MainActivity getInstance() {
        return activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Home");
        database = Room.databaseBuilder(getApplicationContext(), MessageDatabase.class, "messagedb").allowMainThreadQueries().build();

        sharedPreferences = getSharedPreferences(SCALE_PREFERENCES, Context.MODE_PRIVATE);


        String savedNumber = sharedPreferences.getString(SCALE_PHONE_NUMBER_KEY, "");
        if (savedNumber.length() != 0) {
            beeScalePhoneNumber=savedNumber;
        }

        smsList= new ArrayList<String>();
        smsListView = (ListView) findViewById(R.id.SMSList);

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, smsList);
        smsListView.setAdapter(adapter);
        smsListView.setOnItemClickListener(MyItemClickListener);

        populateListViewFromDatabase();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    sendSmsToScale();
                    Snackbar.make(view, "Sent request", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    Toast.makeText(getApplicationContext(),
                            "Sent request", Toast.LENGTH_LONG).show();
                } catch (Exception e) {

                    Toast.makeText(getApplicationContext(),
                            e.getMessage().toString(), Toast.LENGTH_LONG).show();
                }

            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void sendSmsToScale() {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(beeScalePhoneNumber, null, TEXT_SMS, null, null);
    }

    private void populateListViewFromDatabase() {
        smsList.clear();
        List<Message> allMessages = database.messageDao().getAllMessagesDesc();
        for (Message m : allMessages) {
            String displayText = constructDisplayMessage(m);
            smsList.add(displayText);
        }
        adapter.notifyDataSetChanged();

    }

    protected String constructDisplayMessage(Message m) {
        String displayText;
        String weightText = "\nWeight: ";
        String temperatureText = "\nTemperature: ";
        String humidityText = "\nHumidity: ";

        displayText=m.getDate()+weightText+m.getWeight()+" g"+temperatureText+m.getTemperature()+" *C"+humidityText+m.getHumidity()+"%";

        return displayText;
    }

    private void readAllInboxMessages() {
        ContentResolver contentResolver = getContentResolver();
        Cursor smsInboxCursor = contentResolver.query(Uri.parse(INBOX_URI), null, "address='"+beeScalePhoneNumber+"'", null, "date desc");

        int senderIndex = smsInboxCursor.getColumnIndex("address");
        int messageIndex = smsInboxCursor.getColumnIndex("body");
        int dateIndex = smsInboxCursor.getColumnIndex("date");

        if (messageIndex < 0 || !smsInboxCursor.moveToFirst()) return;

        adapter.clear();

        do {

            String sender = smsInboxCursor.getString(senderIndex);
            String message = smsInboxCursor.getString(messageIndex);

            if (message.contains("*** B e e S c a l e ***")) {
                long smsDate = smsInboxCursor.getLong(dateIndex);
                Date smsRealDate = new Date(smsDate);
                String formattedText = smsRealDate.toString() + "\n" + message;
                updateList(formattedText);
            }
        } while (smsInboxCursor.moveToNext());
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, Settings.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private OnItemClickListener MyItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
            try {
                Toast.makeText(getApplicationContext(), adapter.getItem(pos), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    };

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_settings) {
            Intent intent = new Intent(MainActivity.this, Settings.class);
            startActivity(intent);

        } else if (id == R.id.nav_graphics) {
            Intent intent = new Intent(MainActivity.this, Statistics.class);
            startActivity(intent);
        } else if (id == R.id.nav_syncronize) {
            syncDatabaseWithSmsInbox();
            Snackbar.make(findViewById(R.id.drawer_layout), "Successfully syncronized ", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void syncDatabaseWithSmsInbox() {
        ArrayList<String> inboxMessages = readInboxSms();
        insertInboxMessagesIntoDatabase(inboxMessages);
    }

    private void insertInboxMessagesIntoDatabase(ArrayList<String> inboxMessages) {
        if (inboxMessages.size() != 0) {
            smsList.clear();
            for (String m : inboxMessages) {
                Message message = getMessageObject(m);
                try {
                    smsList.add(constructDisplayMessage(message));
                    database.messageDao().insertMessage(message);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }

            }
            populateListViewFromDatabase();
        }
    }

    protected Message getMessageObject(String m) {
        String dateBodySplits[] = m.split(SPLITER);
        String date = dateBodySplits[0];
        String body = dateBodySplits[1];
        String measuredData[] = getMeasuredData(body);
        Date smsRealDate = new Date(Long.parseLong(date));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());

        Message message = new Message();
        message.setNumber(beeScalePhoneNumber);
        message.setDate(formatter.format(smsRealDate));
        message.setWeight(measuredData[0]);
        message.setTemperature(measuredData[1]);
        message.setHumidity(measuredData[2]);


        return message;
    }

    private String[] getMeasuredData(String body) {
        String measuredData[] = new String[3];
        String titleAndDataSplits[] = body.split("\n");
        measuredData[0] = extractValueField(titleAndDataSplits[2]).trim();
        measuredData[1] = extractValueField(titleAndDataSplits[3]).trim();
        measuredData[2] = extractValueField(titleAndDataSplits[4]).trim();

        return measuredData;
    }

    private String extractValueField(String line) {

        String splits[] = (line.trim()).split(" ");
        String value = splits[1];

        if (value.endsWith("%")) {
            return value.substring(0, value.length() - 1);
        }

        return value;

    }

    private ArrayList<String> readInboxSms() {
        String selectionNumber = "address='" + beeScalePhoneNumber + "'";
        ArrayList<String> inboxMessages = new ArrayList<>();
        ContentResolver contentResolver = getContentResolver();
        Cursor smsInboxCursor = contentResolver.query(Uri.parse(INBOX_URI), null, selectionNumber, null, "date desc");

        //int senderIndex = smsInboxCursor.getColumnIndex("address");
        int messageIndex = smsInboxCursor.getColumnIndex("body");
        int dateIndex = smsInboxCursor.getColumnIndex("date");

        if (messageIndex < 0 || !smsInboxCursor.moveToFirst()) return inboxMessages;

        do {
            //String sender = smsInboxCursor.getString(senderIndex);
            String message = smsInboxCursor.getString(messageIndex);
            String date = smsInboxCursor.getString(dateIndex);

            if (message.contains("*** B e e S c a l e ***")) {
                inboxMessages.add(date + SPLITER + message);
            }
        } while (smsInboxCursor.moveToNext());

        return inboxMessages;
    }

    @Override
    protected void onStart() {
        super.onStart();
        activity = this;
    }

    public void updateList(String newSms) {
        adapter.insert(newSms, 0);
        adapter.notifyDataSetChanged();
    }
}
