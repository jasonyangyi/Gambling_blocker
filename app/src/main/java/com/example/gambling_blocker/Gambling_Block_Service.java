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
    private ConcurrentLinkedQueue<IPPacket> devicetonetwork_udp; //  this queue stores the udp data packet sent from device to the network
    private ConcurrentLinkedQueue<IPPacket> devicetonetwork_tdp; // the tcp data packet sent from device to network
    private ConcurrentLinkedQueue<ByteBuffer> networktodevice; // the outgoing packet from network to device
    private ExecutorService executorService;  // run multiple  tasks asynchronously in the background
    private Selector udpSelector; // use selector to manage multiple input/output channels
    private Selector tcpSelector;


    private BroadcastReceiver stopvpn = new BroadcastReceiver() { // create a local broadcast
        /*
        if this receiver receive the stop command
        invoke the stopVPN method to stop the VPN
         */
        @Override
        public void onReceive(Context context, Intent intent) {
          if("stop".equals(intent.getAction())){
              Stopvpn();
        }
    }};

    @Override
    public void onCreate() {
        /*
        use LocalBroadcastManger class to register this receiver
         */
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
        bm.registerReceiver(stopvpn,new IntentFilter("stop"));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startVPN();
       Toast.makeText(getApplicationContext(),"The service has been started",Toast.LENGTH_LONG).show();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startVPN()
            /*
            this method used to start the VPN service
            the data pass between VPN interface and physical network
             */
    {
        configureVPN();
        try {
            udpSelector = Selector.open(); // open the channel
            tcpSelector = Selector.open();
            devicetonetwork_udp = new ConcurrentLinkedQueue<>();
            devicetonetwork_tdp = new ConcurrentLinkedQueue<>();
            networktodevice = new ConcurrentLinkedQueue<>();
            /*
            invoke the newFixedThreadPool method
            to create a thread pool which can store
             5 threads: represent 5 different tasks
             invoke the submit method to allocate the task to the executorService
             */
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
    }

    /*
    set the parameters to the VPN interface
    route address session name
     */
    private void configureVPN()
    {
          Builder builder = new Builder();
          builder.addAddress("192.168.0.1", 24);
          builder.addRoute("0.0.0.0", 0);
          vpninterface = builder.setSession("Gambling blocker").establish();
    }

    private void Stopvpn()
    {
        /*
        clear the queue
        clear the ByteBuffer pool
        this method used to close the VPN interface
        and selector channels
         */
        Toast.makeText(getApplicationContext(),"The service has been stoped",Toast.LENGTH_LONG).show();
        executorService.shutdown();
        devicetonetwork_udp = null;
        devicetonetwork_tdp = null;
        networktodevice = null;
        ByteBufferPool.clear();
        close(udpSelector,tcpSelector,vpninterface);
    }

    private void close(Closeable...resources)
    {
        /*
        this method used to close any closable resources
         */
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
