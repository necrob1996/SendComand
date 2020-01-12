package com.necsulescu_robert.sendcomand;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = "MainActivity";
    private int brod1;
    private int brod3;
    private int brod4;

    BluetoothAdapter mBluetoothAdapter;
    Button btnEnableDisable_Discoverable;


    Button btnStartConnection;
    Button btnSend;

    TextView incomingMessages;
    TextView smsView;
    StringBuilder messages;

    EditText etSend;

    MyBluetoothServiceClass myService;
    boolean isBind = false;

    public static final String EXTRA_NUMBER="com.necsulescu_robert.bluetooth2.EXTRA_NUMBER";

    private static final UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    BluetoothDevice mBTDevice;

    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();

    public DeviceListAdapter mDeviceListAdapter;

    ListView lvNewDevices;

    // Create a BroadcastReceiver for ACTION_FOUND
    private BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            brod1 = 1;
            // When discovery finds a device
            if (action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBluetoothAdapter.ERROR);

                switch(state){
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "onReceive: STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING ON");
                        break;
                }
            }
        }
    };

    /**
     * Broadcast Receiver for changes made to bluetooth states such as:
     * 1) Discoverability mode on/off or expire.
     */
//    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            final String action = intent.getAction();
//
//            if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
//
//                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);
//
//                switch (mode) {
//                    //Device is in Discoverable Mode
//                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
//                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Enabled.");
//                        break;
//                    //Device not in discoverable mode
//                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
//                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Disabled. Able to receive connections.");
//                        break;
//                    case BluetoothAdapter.SCAN_MODE_NONE:
//                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Disabled. Not able to receive connections.");
//                        break;
//                    case BluetoothAdapter.STATE_CONNECTING:
//                        Log.d(TAG, "mBroadcastReceiver2: Connecting....");
//                        break;
//                    case BluetoothAdapter.STATE_CONNECTED:
//                        Log.d(TAG, "mBroadcastReceiver2: Connected.");
//                        break;
//                }
//
//            }
//        }
//    };

    /**
     * Broadcast Receiver for listing devices that are not yet paired
     * -Executed by btnDiscover() method.
     */
    private BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            brod3 = 1;

            Log.d(TAG, "Broadcaast3: Sunt aici");
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND.");

            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
                mBTDevices.add(device);
                Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
                mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, mBTDevices);
                lvNewDevices.setAdapter(mDeviceListAdapter);
            }
        }
    };

    /**
     * Broadcast Receiver that detects bond state changes (Pairing status changes)
     */
    private final BroadcastReceiver mBroadcastReceiver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            brod4 = 1;

            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //3 cases:
                //case1: bonded already
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDED.");
                    mBTDevice = mDevice;
                }
                //case2: creating a bone
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDING.");
                }
                //case3: breaking a bond
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG, "BroadcastReceiver: BOND_NONE.");
                }
            }
        }
    };
    private Context context;


    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called.");
        super.onDestroy();
        if(brod1==1){
            unregisterReceiver(mBroadcastReceiver1);
            Log.d(TAG, "onDestroy: broadcast1 unregistred");
        }
        if(brod3==1){
            unregisterReceiver(mBroadcastReceiver3);
            Log.d(TAG, "onDestroy: broadcast3 unregistred");
        }
       if(brod4==1){
           unregisterReceiver(mBroadcastReceiver4);
           Log.d(TAG, "onDestroy: broadcast4 unregistred");
       }


//        mBluetoothAdapter.cancelDiscovery();
//        if(isBind){
//            unbindService(Mconnection);
//            isBind = false;
//        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: Is called ");
        Button btnONOFF = (Button) findViewById(R.id.btnONOFF);
        btnEnableDisable_Discoverable = (Button) findViewById(R.id.btnDiscoverable_on_off);
        lvNewDevices = (ListView) findViewById(R.id.lvNewDevices);
//        etSend = (EditText) findViewById(R.id.numbr);
//        smsView = (TextView) findViewById(R.id.smsView);
        mBTDevices = new ArrayList<>();

        brod1 = 0;
        brod3 = 0;
        brod4 = 0;



//        int androidVersion;
//
//        androidVersion = Build.VERSION.SDK_INT;
//        if (androidVersion >= 23){
//            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PackageManager.PERMISSION_GRANTED);
//        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.RECEIVE_SMS)!=PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.RECEIVE_SMS},1000);
        }

        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION}, PackageManager.PERMISSION_GRANTED);



        btnStartConnection = (Button) findViewById(R.id.btnStartConnection);
//        btnSend = (Button) findViewById(R.id.btnSend);


//        incomingMessages = (TextView) findViewById(R.id.incoming);
        messages = new StringBuilder();




        //Broadcasts when bond state changes (ie:pairing)
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver4,filter);

        mBluetoothAdapter = mBluetoothAdapter.getDefaultAdapter();

        lvNewDevices.setOnItemClickListener(MainActivity.this);

        btnONOFF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: enabling/disabling bluetooth");
                enableDisableBT();
            }
        });

        btnStartConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startConnection();
            }
        });

//        btnSend.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                byte[] bytes = etSend.getText().toString().getBytes(Charset.defaultCharset());
//                myService.write(bytes);
//
//                etSend.setText("!");
//            }
//        });


    }

//    public void SendThis(String send){
//        Log.d(TAG, "SendThis: "+send);
//        byte[] bytes = send.getBytes(Charset.defaultCharset());
//        myService.write(bytes);
//
////        etSend.setText("!");
//    }


    //create method for starting connection
    //***remember the connection will fail and app will crash if you haven't paired first
    public void startConnection(){
        Log.d(TAG, "startConnection: " + mBTDevice);
        startBTConnection(mBTDevice,MY_UUID_INSECURE);
        Intent intent = new Intent(this,SendCommand.class);
        startActivity(intent);
        
    }

    public void startBTConnection(BluetoothDevice device, UUID uuid){
        Log.d(TAG, "startBTConnection: Initializing RFCOM Bluetooth Connection.");

        myService.startClient(device,uuid);
    }

    private void enableDisableBT() {
        if (mBluetoothAdapter == null){
            Log.d(TAG, "enableDisableBT: Does not have BT capabilities.");
        }
        if(!mBluetoothAdapter.isEnabled()){
            Log.d(TAG, "enableDisableBT: enabling BT.");
            Intent enableBTIntent = new Intent((BluetoothAdapter.ACTION_REQUEST_ENABLE));
            startActivity(enableBTIntent);

            IntentFilter BTIntent = new IntentFilter((BluetoothAdapter.ACTION_STATE_CHANGED));
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }
        if (mBluetoothAdapter.isEnabled()){
            Log.d(TAG, "enableDisableBT: disabling BT.");
            mBluetoothAdapter.disable();

            IntentFilter BTIntent = new IntentFilter((BluetoothAdapter.ACTION_STATE_CHANGED));
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }
    }


    public void btnEnableDisable_Discoverable(View view) {
        Toast.makeText(this, "service is unbinded", Toast.LENGTH_SHORT).show();
        if(isBind){
            unbindService(Mconnection);
            isBind = false;
        }
//        Log.d(TAG, "btnEnableDisable_Discoverable: Making device discoverable for 300 seconds.");
//
//        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,300);
//        startActivity(discoverableIntent);
//
//        IntentFilter intentFilter = new IntentFilter(mBluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
//        registerReceiver(mBroadcastReceiver2,intentFilter);
    }

    public void btnDiscover(View view) {
        Intent intent = new Intent(this,MyBluetoothServiceClass.class);
        bindService(intent,Mconnection,Context.BIND_AUTO_CREATE);
        Log.d(TAG, "btnDiscover: Looking for unpaired devices.");

        if(mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "btnDiscover: Canceling discovery");

            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDeviceIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoverDeviceIntent);
        }

        if(!mBluetoothAdapter.isDiscovering()){

            mBluetoothAdapter.startDiscovery();

            IntentFilter discoverDeviceIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);

            registerReceiver(mBroadcastReceiver3, discoverDeviceIntent);
            Log.d(TAG, "btnDiscover: aici");
        }
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        //first cancel discovery because is very memory intensive

        mBluetoothAdapter.cancelDiscovery();

        Log.d(TAG, "onItemClick: You Clicked on a device");
        String deviceName = mBTDevices.get(i).getName();
        String deviceAddress = mBTDevices.get(i).getAddress();

        Log.d(TAG, "onItemClick: deviceName =" + deviceName);
        Log.d(TAG, "onItemClick: deviceAddress =" + deviceAddress);

        //create the bond.
        //NOTE: Requires API 17+? I think is JeallyBean
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2){
            Log.d(TAG, "Trying to pair with" + deviceName);
            mBTDevices.get(i).createBond();

            mBTDevice = mBTDevices.get(i);

            myService.start();

        }
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this, "Permision granted", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "Permision not granted", Toast.LENGTH_SHORT).show();
            finish();
        }
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {

        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

    }

}
