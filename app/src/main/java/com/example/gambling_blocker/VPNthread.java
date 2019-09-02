package com.example.gambling_blocker;

import android.util.Log;
import android.widget.Toast;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

import static android.content.ContentValues.TAG;

public class VPNthread implements Runnable {

    private FileDescriptor vpninterface;
    // The three queue used to store the different kinds of packets
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
                    bufferToNetwork = ByteBufferPool.acquire();// get the first the element in the ByteBuffer pool
                else
                    bufferToNetwork.clear();


                Analyze_packet(bufferToNetwork);
                int readBytes = vpnInput.read(bufferToNetwork); // first read the data packet
                if (readBytes > 0)
                {
                    dataSent = true;
                    bufferToNetwork.flip();
                    IPPacket packet = new IPPacket(bufferToNetwork); // build an instance of packet


                    //  intercept the data packet here
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
                        dataSent = false;  // if the packet do not have TCP header or UDP header, we do not receive the data packet
                    }
                }
                else
                {
                    dataSent = false;
                }

                ByteBuffer bufferFromNetwork = networktoDevice.poll();// return and remove the first element at the head of teh queue
                if (bufferFromNetwork != null)
                {
                    bufferFromNetwork.flip();
                    while (bufferFromNetwork.hasRemaining())
                        vpnOutput.write(bufferFromNetwork); // invoke the write method to write the packet to physical network
                    dataReceived = true;
                    ByteBufferPool.release(bufferFromNetwork); //  // make a buffer ready for a new sequence of channel-read or relative operations
                    // insert the buffer at the tail of the queue
                }
                else
                {
                    dataReceived = false;
                }
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

    private void Analyze_packet(ByteBuffer packet)
    {
        /*
        this method used to parse the  IP packet
        get the Destination IP
        and build destination host name
         */
        int buffer = packet.get();
        int ipVersion = buffer >> 4;
        int headerLength = buffer & 0x0F;   // the 32 bits  in the header
        headerLength *= 4;
        packet.get();                       // get the DSCP + EN
        int totalLength = packet.getChar(); // get the total length of header
        packet.getChar();                   //Identification
        packet.getChar();                   // get Flags + Fragment Offset
        packet.get();                       // get the time to live
        int protocol = packet.get();        // get the protocol
        packet.getChar();                   // get the header checksum

        String sourceIP  = "";
        sourceIP += packet.get() & 0xFF; //Source IP 1st Octet
        sourceIP += ".";
        sourceIP += packet.get() & 0xFF; //Source IP 2nd Octet
        sourceIP += ".";
        sourceIP += packet.get() & 0xFF; //Source IP 3rd Octet
        sourceIP += ".";
        sourceIP += packet.get() & 0xFF; //Source IP 4th Octet

        String destIP  = "";
        destIP += packet.get() & 0xFF; //Destination IP 1st Octet
        destIP += ".";
        destIP += packet.get() & 0xFF; //Destination IP 2nd Octet
        destIP += ".";
        destIP += packet.get() & 0xFF; //Destination IP 3rd Octet
        destIP += ".";
        destIP += packet.get() & 0xFF; //Destination IP 4th Octet


        int sourcePortUdp = packet.getChar(); // the source port of UDP datagram
        int destPortUdp = packet.getChar();
        packet.getChar(); //UDP Data Length
        packet.getChar(); //UDP Checksum
        packet.getChar(); //DNS ID
        packet.get();
        packet.get();
        packet.getChar();
        packet.getChar();
        packet.getChar();
        packet.getChar();
        packet.getChar();
        packet.getChar();

        String sourceHostname; // get the source host name from the source ip
      try{
          InetAddress souaddr = InetAddress.getByName(sourceIP);
          sourceHostname = souaddr.getHostName();
      }catch (UnknownHostException e){
          sourceHostname = "Unresolved";
      }


        String destHostName;
        try {
            InetAddress addr = InetAddress.getByName(destIP);
            destHostName = addr.getHostName();
        } catch (UnknownHostException e) {
            destHostName = "Unresolved";
        }


       Log.e(TAG, "---\nHeaders:\nIP Version=" + ipVersion + "\nHeader-Length=" + headerLength
                + "\nTotal-Length=" + totalLength + "\nDestination=" + destIP + " / "
                + destHostName +"\nSourcehost ="+sourceHostname );
    }


}
