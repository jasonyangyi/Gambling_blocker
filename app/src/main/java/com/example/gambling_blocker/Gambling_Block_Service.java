package com.example.gambling_blocker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.VpnService;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class Gambling_Block_Service extends VpnService {

    private ParcelFileDescriptor vpnInterface;
    private Thread vpnThread;
    private Builder builder = new Builder();
    private BroadcastReceiver stopvpn = new BroadcastReceiver() { // create a local broadcast
        @Override
        public void onReceive(Context context, Intent intent) {
          if("stop".equals(intent.getAction())){
              Stopvpn();
        }
    }};

    @Override
    public void onCreate() {
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
        bm.registerReceiver(stopvpn,new IntentFilter("stop"));
     // we need to register this broadcast
     //   Toast.makeText(getApplicationContext(),"Has been registered",Toast.LENGTH_LONG).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


       vpnThread = new Thread(new Runnable() {
            @Override
            public void run()
            {
                // create a vpn tunnel interface

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




    private void Stopvpn()
    {
        if(vpnThread!=null)
        {
            vpnThread.interrupt(); // interrupt the thread
            try{
                vpnInterface.close(); // close the tunnel
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        stopSelf();
    }


    @Override
    public void onDestroy() { // stop the service

    }
}
