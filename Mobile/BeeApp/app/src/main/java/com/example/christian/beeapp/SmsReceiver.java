package com.example.christian.beeapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SmsReceiver extends BroadcastReceiver {

    // SmsManager class is responsible for all SMS related actions
    final SmsManager sms = SmsManager.getDefault();

    private MainActivity mainInstance = MainActivity.getInstance();

    @Override
    public void onReceive(Context context, Intent intent) {
        // Get the SMS message received
        final Bundle bundle = intent.getExtras();
        try {
            if (bundle != null) {
                // A PDU is a "protocol data unit". This is the industrial standard for SMS message
                final Object[] pdusObj = (Object[]) bundle.get("pdus");
                for (int i = 0; i < pdusObj.length; i++) {
                    // This will create an SmsMessage object from the received pdu
                    SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                    // Get sender phone number
                    String phoneNumber = sms.getDisplayOriginatingAddress();
                    String sender = phoneNumber;
                    String message = sms.getDisplayMessageBody();

                    if(sender.equals(MainActivity.beeScalePhoneNumber)){
                        if(message.contains("*** B e e S c a l e ***")){
                            long smsDate = Calendar.getInstance().getTimeInMillis();

                            Message messageObject=mainInstance.getMessageObject(smsDate+MainActivity.SPLITER+message);
                            String displayText=mainInstance.constructDisplayMessage(messageObject);
                            mainInstance.updateList(displayText);

                            try {
                                MainActivity.database.messageDao().insertMessage(messageObject);
                                Toast.makeText(context,"Status received",Toast.LENGTH_SHORT).show();
                            }catch (Exception e){
                                Toast.makeText(context,"Failed insert into database "+e.getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        }

                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
