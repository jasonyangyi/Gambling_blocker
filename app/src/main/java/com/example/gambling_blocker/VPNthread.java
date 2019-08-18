package com.example.gambling_blocker;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.ConcurrentLinkedQueue;

public class VPNthread implements Runnable {

    private FileDescriptor vpninterface;
    private ConcurrentLinkedQueue<IPPacket> devicetoNetwork_udp;
    private ConcurrentLinkedQueue<IPPacket> devicetoNetwork_tcp;
    private ConcurrentLinkedQueue<ByteBuffer> networktoDevice;

    /*
    the constructor used to build the VPN thread
     */

    public VPNthread(FileDescriptor vpninterface, ConcurrentLinkedQueue<IPPacket> devicetoNetwork_udp,
                     ConcurrentLinkedQueue<IPPacket> devicetoNetwork_tcp,
                     ConcurrentLinkedQueue<ByteBuffer> networktoDevice)
    {
        this.vpninterface = vpninterface;
        this.devicetoNetwork_udp = devicetoNetwork_udp;
        this.devicetoNetwork_tcp = devicetoNetwork_tcp;
        this.networktoDevice = networktoDevice;
    }

    @Override
    public void run() {
        FileChannel vpnInput = new FileInputStream(vpninterface).getChannel(); // the input tunnel used to read the packet from the device to the network
        FileChannel vpnOutput = new FileOutputStream(vpninterface).getChannel(); // the output tunnel used to write the packet from the network to the network

        try
        {
            ByteBuffer bufferToNetwork = null;
            boolean dataSent = true;
            boolean dataReceived;
            while (!Thread.interrupted())
            {
                if (dataSent)
                    bufferToNetwork = ByteBufferPool.acquire();
                else
                    bufferToNetwork.clear();


                //     Analyze_packet(bufferToNetwork);
                int readBytes = vpnInput.read(bufferToNetwork); // first read the data packet
                if (readBytes > 0)
                {
                    dataSent = true;
                    bufferToNetwork.flip();
                    IPPacket packet = new IPPacket(bufferToNetwork); // build an instance of packet


                    //  应该是在这儿对 packet数据进行拦截
                    if (packet.isUDP())
                    {  // insert the UDP packet to the queue
                        devicetoNetwork_udp.offer(packet);
                    }
                    else if (packet.isTCP())
                    {  // insert the TCP packet to the queue
                        devicetoNetwork_tcp.offer(packet);
                    }
                    else
                    {
                        dataSent = false;
                    }
                }
                else
                {
                    dataSent = false;
                }

                ByteBuffer bufferFromNetwork = networktoDevice.poll();
                if (bufferFromNetwork != null)
                {
                    bufferFromNetwork.flip();
                   // while (bufferFromNetwork.hasRemaining())
                        vpnOutput.write(bufferFromNetwork);
                    dataReceived = true;
                    ByteBufferPool.release(bufferFromNetwork);
                }
                else
                {
                    dataReceived = false;
                }
                // Confirm if throughput with ConcurrentQueue is really higher compared to BlockingQueue
                if (!dataSent && !dataReceived)
                    Thread.sleep(10);
            }
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }
}
