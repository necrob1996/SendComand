package com.necsulescu_robert.sendcomand;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class ReceiveSms extends BroadcastReceiver {
    Context mContext;
    String TAG = "ReceiveSms: ";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
            Bundle bundle = intent.getExtras();
            SmsMessage[] msgs;
            String msg_from;
            Log.d(TAG, "onReceive: started");
            if(bundle != null){
                try {
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    msgs = new SmsMessage[pdus.length];
                    for (int i=0; i< msgs.length; i++){
                        msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                        msg_from= msgs[i].getOriginatingAddress();
                        String msgBody = msgs[i].getMessageBody();

                        Log.d(TAG, "onReceive: message from " + msg_from);

                        Intent incomingSMSMessageIntent = new Intent("incomingSMSMessage");
                        incomingSMSMessageIntent.putExtra("message", msgBody);
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(incomingSMSMessageIntent);

//                        Toast.makeText(context, "From: "+msg_from+", Body: "+msgBody, Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
}
