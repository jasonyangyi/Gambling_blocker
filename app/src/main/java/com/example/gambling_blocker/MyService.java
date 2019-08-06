package com.example.gambling_blocker;

import android.app.Service;
import android.content.Intent;
import android.net.VpnService;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;

public class MyService extends VpnService {
    private ParcelFileDescriptor vpnInterface;
    private Thread vpnThread;
    private Builder builder;
    private DatagramChannel tunnel;


    public MyService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
       Toast.makeText(getApplicationContext(),"The service has been started",Toast.LENGTH_LONG).show();

                // create a vpn tunnel interface
             /*   Builder builder = new Builder();
                vpnInterface = builder.setSession("Gambling site blocker") // configure the vpn tunnel with parameters
                        .addAddress("192.168.0.1",24)
                        .addDnsServer("8.8.8.8")
                        .addRoute("0.0.0.0",0).establish();
                FileInputStream in = new FileInputStream(vpnInterface.getFileDescriptor());
                FileOutputStream out = new FileOutputStream(vpnInterface.getFileDescriptor());*/
             builder = new Builder();
             vpnThread = new Thread(new Runnable() {
                 @Override
                 public void run() {
                     try {
                         vpnInterface = builder.setSession("Gambling site blocker") // configure the vpn tunnel with parameters
                                 .addAddress("192.168.0.1", 24)
                                 .addDnsServer("8.8.8.8")
                                 .addRoute("0.0.0.0", 0).establish();
                         FileInputStream in = new FileInputStream(vpnInterface.getFileDescriptor());
                         FileOutputStream out = new FileOutputStream(vpnInterface.getFileDescriptor());
                         tunnel = DatagramChannel.open();
                         tunnel.connect(new InetSocketAddress("127.0.0.1", 8087));
                      //   protect(tunnel.socket());
                         while(true)
                         {

                         }
                     }
                     catch (IOException e){
                         e.printStackTrace();
                     }
                     finally {
                         if(vpnInterface!=null)
                         {
                             try {
                                 vpnInterface.close();
                                 vpnInterface = null;
                             }
                             catch (IOException e)
                             {
                                 e.printStackTrace();
                             }
                         }

                     }

                 }
             });
            vpnThread.start();



        return START_STICKY;
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
       // Stopvpn();
        if(vpnThread!=null)
        {
            vpnThread.interrupt();
        }
        stopSelf();
        Toast.makeText(getApplicationContext(),"The service has been stoped",Toast.LENGTH_LONG).show();
    }

  /*  private void Stopvpn()
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
    }*/
}
