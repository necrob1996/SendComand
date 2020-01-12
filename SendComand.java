package com.necsulescu_robert.sendcomand;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.nio.charset.Charset;

public class SendCommand extends AppCompatActivity {

    private EditText command;
    private TextView response;
    MyBluetoothServiceClass myService;
    boolean isBind = false;
    String TAG="SendCommand";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_command);

        Button closeServer = (Button) findViewById(R.id.closeServer);
        Button sendCommand = (Button) findViewById(R.id.button);
         command = (EditText) findViewById(R.id.editText);
         response = (TextView) findViewById(R.id.textView);

        Intent intent = new Intent(this,MyBluetoothServiceClass.class);
        bindService(intent,Mconnection,Context.BIND_AUTO_CREATE);

         sendCommand.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 String cm = command.getText().toString();
                 byte[] bytes = cm.getBytes(Charset.defaultCharset());
                 myService.write(bytes);
             }
         });

         closeServer.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 unbindService(Mconnection);
                 Toast.makeText(myService,"Server closed",Toast.LENGTH_SHORT).show();
             }
         });

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,new IntentFilter("incomingMessage"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mGetMessage,new IntentFilter("incomingSMSMessage"));



//            if(smsView.getText().toString().equals("aprinde")){
//                byte[] bytes = smsView.getText().toString().getBytes(Charset.defaultCharset());
//                mBluetoothConnection.write(bytes);
//            }

    }

    private ServiceConnection Mconnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            MyBluetoothServiceClass.LocalService localService = (MyBluetoothServiceClass.LocalService) iBinder;
            myService = localService.getService();
            isBind = true;

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

            isBind = false;

        }
    };

    BroadcastReceiver mGetMessage = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String message = intent.getStringExtra("message");
            Log.d(TAG, "onReceive: got " + message);
//
            byte[] bytes = message.getBytes(Charset.defaultCharset());
            myService.write(bytes);
        }
    };

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra("theMessage");
            response.setText(text);

            SmsManager mySmsManager = SmsManager.getDefault();
            mySmsManager.sendTextMessage("07xxxxxxxx",null,text,null,null);
        }
    };
}
