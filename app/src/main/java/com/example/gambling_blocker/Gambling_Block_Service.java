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

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Gambling_Block_Service extends VpnService {

    private ParcelFileDescriptor vpninterface = null; // create an instance of FileDescriptor to get the IP data packet
    private ConcurrentLinkedQueue<IPPacket> devicetonetwork_udp; //  these three queues used to store the different kinds of packets
    private ConcurrentLinkedQueue<IPPacket> devicetonetwork_tdp;
    private ConcurrentLinkedQueue<ByteBuffer> networktodevice;
    private ExecutorService executorService;
    private Selector udpSelector;
    private Selector tcpSelector;


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
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        configureVPN();

        try {
            udpSelector = Selector.open(); // open the channel
            tcpSelector = Selector.open();
            devicetonetwork_udp = new ConcurrentLinkedQueue<>();
            devicetonetwork_tdp = new ConcurrentLinkedQueue<>();
            networktodevice = new ConcurrentLinkedQueue<>();
            executorService = Executors.newFixedThreadPool(5);
            executorService.submit(new UDP_data_input(networktodevice, udpSelector));
            executorService.submit(new UDP_data_output(devicetonetwork_udp, udpSelector, this));
            executorService.submit(new TCP_data_input(networktodevice, tcpSelector));
            executorService.submit(new TCP_data_output(devicetonetwork_tdp, networktodevice, tcpSelector, this));
            executorService.submit(new VPNthread(vpninterface.getFileDescriptor(),
                    devicetonetwork_udp, devicetonetwork_tdp, networktodevice));

        }catch (IOException e){
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),"Error! the service has terminated!",Toast.LENGTH_LONG).show();
        }

       Toast.makeText(getApplicationContext(),"The service has been started",Toast.LENGTH_LONG).show();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /*
    set the parameters to the VPN interface
    route address session name
     */
    private void configureVPN()
    {
      if(vpninterface==null)
      {
          Builder builder = new Builder();
          builder.addAddress("192.168.0.1", 24);
          builder.addRoute("0.0.0.0", 0);
          vpninterface = builder.setSession("Gambling blocker").establish();
      }

    }



    private void Stopvpn()
    {
        /*
        this method used to close the VPN interface
        and selector channels
         */
        executorService.shutdown();
        devicetonetwork_udp = null;
        devicetonetwork_tdp = null;
        networktodevice = null;
        ByteBufferPool.clear();
        close(udpSelector,tcpSelector,vpninterface);
        stopSelf();
    }

    private void close(Closeable...resources)
    {
        for (Closeable resource : resources)
        {
            try
            {
                resource.close();
            }
            catch (IOException e)
            {
            }
        }
    }


    @Override
    public void onDestroy() { }

}
