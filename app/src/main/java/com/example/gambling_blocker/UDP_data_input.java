package com.example.gambling_blocker;

import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class UDP_data_input implements Runnable {
   // process the UDP data in
    private static final int HEADER_SIZE = IPPacket.IP4_HEADER_SIZE + IPPacket.UDP_HEADER_SIZE;
//  define the IPPacket header size
    private Selector selector;
    private ConcurrentLinkedQueue<ByteBuffer> outputQueue;

    public UDP_data_input(ConcurrentLinkedQueue<ByteBuffer> outputQueue, Selector selector)
    {
        this.outputQueue = outputQueue;
        this.selector = selector;
    }
    @Override
    public void run() {
        try
        {
            while (!Thread.interrupted())
            {
                int readyChannels = selector.select();

                if (readyChannels == 0) {
                    Thread.sleep(10);
                    continue;
                }

                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = keys.iterator();

                while (keyIterator.hasNext() && !Thread.interrupted())
                {
                    SelectionKey key = keyIterator.next();
                    if (key.isValid() && key.isReadable())
                    {
                        keyIterator.remove();

                        ByteBuffer receiveBuffer = ByteBufferPool.acquire();
                        // Leave space for the header
                        receiveBuffer.position(HEADER_SIZE);

                        DatagramChannel inputChannel = (DatagramChannel) key.channel();

                        int readBytes = inputChannel.read(receiveBuffer);

                        IPPacket referencePacket = (IPPacket) key.attachment();
                        referencePacket.updateUDPBuffer(receiveBuffer, readBytes);
                        receiveBuffer.position(HEADER_SIZE + readBytes);

                        outputQueue.offer(receiveBuffer);
                    }
                }
            }
        }
        catch (InterruptedException e){
               e.printStackTrace();
        }
        catch (IOException e)
        {
             e.printStackTrace();
        }

    }
}
