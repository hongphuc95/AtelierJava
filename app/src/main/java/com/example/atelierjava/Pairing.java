package com.example.atelierjava;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.util.UUID;

public class Pairing extends AppCompatActivity {

    VideoView videoView;
    private LocalBroadcastManager localBroadcastManager;
    private ProgressBar progressBar;
    private TextView downloadTextView;

    private static final String TAG = "ClientService";
    private static final String OUTPUT_FILE_NAME = "DownloadedVideoStreaming.mp4";
    private static final UUID MY_UUID =
            UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66"); //TODO

    private Thread cThread;
    String receivedData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pairing);

        /*downloadTextView = findViewById(R.id.downloadStatusText);

        downloadTextView.setVisibility(View.VISIBLE);
        videoView.setVisibility(View.INVISIBLE);*/

        BluetoothDevice bd = getIntent().getExtras().getParcelable("SELECTED DEVICE");
        Toast.makeText(Pairing.this,bd.getName(),Toast.LENGTH_LONG).show();
        cThread = new ConnectThread(bd);
        cThread.start();


        videoView = findViewById(R.id.videoView);
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        Button scanButton = (Button) findViewById(R.id.view_video);
        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                playVideo();
            }
        });

        //Uri uri = Uri.parse(localFile.getPath());
        //videoView.setVideoURI(uri);
        //videoView.start();
        /*Intent serviceIntent = new Intent(this, ClientService.class);
        serviceIntent.putExtra("INTENT_CLIENT", bd);
        startService(serviceIntent);*/


        //broadcast receiver
        /*localBroadcastManager = LocalBroadcastManager.getInstance(this);
        BroadcastReceiver broadcastReceiver = new ClientReceiver(this);
        IntentFilter intentFilter = new IntentFilter(FILTER);
        localBroadcastManager.registerReceiver(broadcastReceiver, intentFilter);*/
    }

    public void playVideo() {
        Uri uri = Uri.parse("/storage/emulated/0/Video/DownloadedVideoStreaming.mp4");
        videoView.setVideoURI(uri);
        videoView.start();
    }


    /*
     ************************************************
     *                    CLIENT                    *
     ************************************************
     */

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private InputStream is;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                tmp = mmDevice.createRfcommSocketToServiceRecord(
                        MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {

            // Always cancel discovery because it will slow down a connection
            BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            connected(mmSocket);
            //streamVideo();


        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }



        private void connected(BluetoothSocket socket) {
            /*try {
                is = socket.getInputStream();

                String theString = convertStreamToString(is);
                Log.d("OUTPUT", theString);

            } catch (IOException e) {
                e.printStackTrace();
            }*/

           try {
                String rootDir = Environment.getExternalStorageDirectory()
                        + File.separator + "Video";
                File rootFile = new File(rootDir);
                //rootFile.mkdir();


                File localFile = new File(rootFile, OUTPUT_FILE_NAME);
                String output = "PATH OF LOCAL FILE : " + localFile.getPath();
                if (rootFile.exists()) Log.d("SERVICE_ACTIVITY", output);
                if (!localFile.exists()) {
                    localFile.createNewFile();
                } else {
                    localFile.delete();
                    localFile.createNewFile();
                }
                //FileOutputStream fos = new FileOutputStream(localFile);
                is = socket.getInputStream();
                //System.out.println(convertStreamToString(is));
                //Log.d("DEBUG: ", convertStreamToString(is));

                copyInputStreamToFile(is, localFile);
                //fos.close();




                /*byte[] buffer = new byte[1024];
                int len1 = 0;

                int nbOfPaquetsReceived = 0;
                FileDescriptor fileDescriptor = fos.getFD();

                try {
                    while ((len1 = is.read(buffer)) > 0) {
                        nbOfPaquetsReceived++;
                        fos.write(buffer, 0, len1);
                        fos.flush();
                        fileDescriptor.sync();
                    }
                } catch (IOException se) {
                    Log.d(TAG, "Server connection closed  ");
                }
                fos.close();
                is.close();*/



            } catch (IOException e) {
                Log.d("Error....", e.toString());
            }
        }

        void streamVideo () {

            String rootDir = Environment.getExternalStorageDirectory()
                    + File.separator + "Video";
            File rootFile = new File(rootDir);
            File localFile = new File(rootFile, OUTPUT_FILE_NAME);
            playVideo();
        }
    }

    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    private static void copyInputStreamToFile(InputStream inputStream, File file)
            throws IOException {

        try (FileOutputStream outputStream = new FileOutputStream(file)) {

            int read;
            byte[] bytes = new byte[1024];

            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }

        }
    }

    }
