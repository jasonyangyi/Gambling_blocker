package com.example.gambling_blocker;

import android.content.Intent;
import android.net.VpnService;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;

public class Gambling_Block_Service extends VpnService {

    private ParcelFileDescriptor vpnInterface;
    private Thread vpnThread;
    private Builder builder;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

       vpnThread = new Thread(new Runnable() {
            @Override
            public void run()
            {
                // create a vpn tunnel interface
                builder = new Builder();
               vpnInterface = builder.setSession("Gambling site blocker") // configure the vpn tunnel with parameters
                       .addAddress("192.168.0.1",24)
                       .addDnsServer("8.8.8.8")
                       .addRoute("0.0.0.0",0).establish();

               //


               FileInputStream in = new FileInputStream(vpnInterface.getFileDescriptor());
               FileOutputStream out = new FileOutputStream(vpnInterface.getFileDescriptor());

                ByteBuffer packet = ByteBuffer.allocate(32767);
                try {
                    int length = in.read(packet.array());
                    out.write(packet.array(),0,length);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }



            }
        });

        vpnThread.start();
       Toast.makeText(getApplicationContext(),"The service has been started",Toast.LENGTH_LONG).show();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onRevoke() {
        super.onRevoke();
    }

    private void Stopvpn()
    {
        if(vpnThread!=null)
        {
            vpnThread.interrupt();
            try{
                vpnInterface.close();
                vpnInterface =null;
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        stopSelf();
    }


    @Override
    public void onDestroy() { // stop the service
        Stopvpn();
     Toast.makeText(getApplicationContext(),"The service has been stoped",Toast.LENGTH_LONG).show();

    }
}
