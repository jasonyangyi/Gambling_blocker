package com.example.gambling_blocker;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public class UDP_data_output implements Runnable {

    private Gambling_Block_Service vpnService;
    private ConcurrentLinkedQueue<IPPacket> inputQueue;
    private Selector selector;

    private static final int MAX_CACHE_SIZE = 50;
    private LRUCache<String, DatagramChannel> channelCache =
            new LRUCache<>(MAX_CACHE_SIZE, new LRUCache.CleanupCallback<String, DatagramChannel>()
            {
                @Override
                public void cleanup(Map.Entry<String, DatagramChannel> eldest)
                {
                    closeChannel(eldest.getValue()); // clean up the least recently used resources
                }
            });

    public UDP_data_output(ConcurrentLinkedQueue<IPPacket> inputQueue, Selector selector, Gambling_Block_Service vpnService)
    {
        this.inputQueue = inputQueue;
        this.selector = selector;
        this.vpnService = vpnService;
    }
    @Override
    public void run() {

        try
        {

            Thread currentThread = Thread.currentThread();
            while (true)
            {
                IPPacket currentPacket;
                do
                {
                    // get the first element in the queue
                    // get the buffer first
                    currentPacket = inputQueue.poll();
                    if (currentPacket != null)
                        break;
                    Thread.sleep(10);
                } while (!currentThread.isInterrupted());

                if (currentThread.isInterrupted())
                    break;

                InetAddress destinationAddress = currentPacket.ip4Header.destinationAddress;
                int destinationPort = currentPacket.udpHeader.destinationPort;
                int sourcePort = currentPacket.udpHeader.sourcePort;

                String ipAndPort = destinationAddress.getHostAddress() + ":" + destinationPort + ":" + sourcePort;
                DatagramChannel outputChannel = channelCache.get(ipAndPort);
                if (outputChannel == null) {
                    outputChannel = DatagramChannel.open();
                    vpnService.protect(outputChannel.socket()); // avoid the loop
                    try
                    {
                        outputChannel.connect(new InetSocketAddress(destinationAddress, destinationPort));
                    }
                    catch (IOException e)
                    {
                        closeChannel(outputChannel);
                        ByteBufferPool.release(currentPacket.backingBuffer);
                        continue;
                    }
                    outputChannel.configureBlocking(false);
                    currentPacket.swapSourceAndDestination();
                    // update the new source address and destination address

                    // wake up the selector
                    selector.wakeup();
                    outputChannel.register(selector, SelectionKey.OP_READ, currentPacket);
                    // register the channel to read the packet

                    channelCache.put(ipAndPort, outputChannel);
                    // put it to the map list: key entry
                }

                try
                {
                    ByteBuffer payloadBuffer = currentPacket.backingBuffer;
                    while (payloadBuffer.hasRemaining())
                        outputChannel.write(payloadBuffer); // write the backing buffer back to physical network
                }
                catch (IOException e)
                {
                    channelCache.remove(ipAndPort);
                    closeChannel(outputChannel);
                }
                ByteBufferPool.release(currentPacket.backingBuffer); // make a buffer ready for a new sequence of channel-read or relative operations
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
        finally
        {
            closeAll();
        }
    }

    private void closeAll()
    {
        Iterator<Map.Entry<String, DatagramChannel>> it = channelCache.entrySet().iterator();
        while (it.hasNext())
        {
            /*
            close all of the channel managed by the selector
             */
            closeChannel(it.next().getValue());
            it.remove();
        }
    }



    private void closeChannel(DatagramChannel channel)
    {
        try
        {
            channel.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}

