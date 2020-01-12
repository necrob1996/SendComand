package com.necsulescu_robert.sendcomand;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

public class MyBluetoothServiceClass extends Service {
    private IBinder mBinder = new LocalService();
    private static String TAG = "BluetoothCommunicationService";

    private static final String appName = "MYAPP";

    private static final UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    private AcceptThread mInsecureAcceptThread;
    Context mContext;

    private ConnectThread mConnectThread;
    private BluetoothDevice mmDevice;
    private UUID deviceUUID;
    ProgressDialog mProgressDialog;

    private ConnectedThread mConnectedThread;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: Created");
        return mBinder;
    }

    public class LocalService extends Binder{
        MyBluetoothServiceClass getService(){
            return  MyBluetoothServiceClass.this;
        }
    }

    public String getFirstMessage(){
        return "hello world";
    }

    private class AcceptThread extends Thread{
        // The local server socket
        private final BluetoothServerSocket mmServiceSocket;

        @SuppressLint("LongLogTag")
        public AcceptThread(){
            BluetoothServerSocket tmp = null;

            // Create a new listening server socket
            try{
                tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName,MY_UUID_INSECURE);

                Log.d(TAG, "AcceptThread: Setting up Server using" + MY_UUID_INSECURE);
            }catch(IOException e){
                Log.e(TAG, "AcceptThread: IOException:"+ e.getMessage() );
            }

            mmServiceSocket = tmp;
        }

        @SuppressLint("LongLogTag")
        public void run(){
            Log.d(TAG, "run: AcceptThread running");

            BluetoothSocket socked = null;

            try{
                //This is a blocking call and will only return on a
                //successful connection or an exception
                Log.d(TAG, "run: RECOM server socket start...");

                socked = mmServiceSocket.accept();

                Log.d(TAG, "run: RECOM server socket accepted connection");

            }catch (IOException e){
                Log.e(TAG, "AcceptThread: IOException:"+ e.getMessage() );
            }

            if (socked != null){
                connected(socked,mmDevice);
            }

            Log.i(TAG, "END mAcceptThread");
        }

        @SuppressLint("LongLogTag")
        public void cancel(){
            Log.d(TAG, "cancel: Canceling AcceptThread.");
            try{
                mmServiceSocket.close();
            }catch (IOException e){
                Log.e(TAG, "cancel: Close of AcceptThread Server Socked failed." + e.getMessage() );
            }
        }
    }

    private class  ConnectThread extends Thread{
        private BluetoothSocket mmSocket;

        @SuppressLint("LongLogTag")
        public ConnectThread(BluetoothDevice device, UUID uuid){
            Log.d(TAG, "ConnectThread: started.");
            mmDevice = device;
            deviceUUID = uuid;
        }

        @SuppressLint("LongLogTag")
        public void run(){
            BluetoothSocket tmp = null;
            Log.i(TAG, "run: mConnectThread");

            //Get a BluetoothSocket for a conction with the
            //given BluetoothDevice
            try{
                tmp = mmDevice.createRfcommSocketToServiceRecord(deviceUUID);
            }catch (IOException e){
                Log.e(TAG, "ConnectThread: Could not create InsecureRfcommSocket" + e.getMessage() );
            }

            mmSocket = tmp;

            //Always cancel discovery because it will slow down a connection
            mBluetoothAdapter.cancelDiscovery();

            //Make a connection to the Bluetooth


            try {
                //This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
                Log.d(TAG, "run: ConnectThread connected");
            } catch (IOException e) {
                try {
                    mmSocket.close();
                    Log.d(TAG, "run: Closed Socked");
                } catch (IOException ex) {
                    Log.e(TAG, "mConnectThread: run: Unable to close connection in socket" + ex.getMessage());
                }
                Log.e(TAG, "run: ConnectThread: Could not connect to UUID"+ MY_UUID_INSECURE );
            }

            connected(mmSocket,mmDevice);

        }

        @SuppressLint("LongLogTag")
        public void cancel(){
            Log.d(TAG, "cancel: Closing Client Socket");
            try{
                mmSocket.close();
            }catch (IOException e){
                Log.e(TAG, "cancel: Close of ConnectThread failed." + e.getMessage() );
            }
        }
    }

    @SuppressLint("LongLogTag")
    public synchronized void start(){
        Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if(mConnectThread!=null){
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if(mInsecureAcceptThread == null){
            mInsecureAcceptThread = new AcceptThread();
            mInsecureAcceptThread.start();
        }
    }

//    public MyBluetoothServiceClass(Context mContext) {
//        this.mContext = mContext;
//    }

    /**
     * AcceptThread starts and sits waiting for a connection.
     * then ConnectThread starts and attempts to make a connection with the other device AcceptThread
     */

    @SuppressLint("LongLogTag")
    public void startClient(BluetoothDevice device, UUID uuid){
        Log.d(TAG, "startClient: Started.  " + device);

        //initprogress dialog
//        mProgressDialog = ProgressDialog.show(mContext,"Connecting Bluetooth","Please Wait...",true);
        mConnectThread = new ConnectThread(device,uuid);
        mConnectThread.start();
    }

    /**
     * Finally the ConnectedThread witch is responsible for maintaining the BTConnection, Sending the data and
     * receiving incoming data through input/output streams respectively.
     */

    private class ConnectedThread extends Thread{
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        @SuppressLint("LongLogTag")
        public ConnectedThread(BluetoothSocket socket){
            Log.d(TAG, "ConnectedThread: Starting");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            //dismiss the progressdialog when connection is established
            try{
                mProgressDialog.dismiss();
            }catch(NullPointerException e){
                e.printStackTrace();
            }

            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            }catch(IOException e){
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }
        @SuppressLint("LongLogTag")
        public void run(){
            byte[] buffer = new byte[1024]; // buffer store for the stream

            int bytes; //bytes returned from read()

            //Keep listening to the InputStream until an exception occurs
            while (true){
                //Read from the InputStream
                try{
                    bytes = mmInStream.read(buffer);
                    String incomingMessage = new String(buffer,0,bytes);
                    Log.d(TAG, "InputStream: "+incomingMessage);

                    Intent incomingMessageIntent = new Intent("incomingMessage");
                    incomingMessageIntent.putExtra("theMessage", incomingMessage);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(incomingMessageIntent);

                }catch (IOException e){
                    Log.e(TAG, "write: Error reading Input Stream."+ e.getMessage() );
                    break;
                }
            }
        }
        @SuppressLint("LongLogTag")
        //Call this from the main activity to send data to the remote device
        public void write(byte[] bytes){
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: Writing to output stream"+text);
            try{
                mmOutStream.write(bytes);
            }catch(IOException e){
                Log.e(TAG, "write: Error writing to outputstream."+ e.getMessage() );
            }
        }

        public void cancel(){
            try{
                mmSocket.close();
            }catch (IOException e){

            }
        }

    }
    @SuppressLint("LongLogTag")
    private void connected(BluetoothSocket mmSocket, BluetoothDevice mmDevice) {
        Log.d(TAG, "connected: Starting.");

        // Start the thread to manage the connection and perform a transmission
        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    @SuppressLint("LongLogTag")
    public void write(byte[] out){
        // Create temporary object
        ConnectedThread r ;

        //Synchronize a copy of the ConnectedThread
        Log.d(TAG, "write: Write Called.");
        //perform the write
        r= mConnectedThread;
        r.write(out);
    }

}
