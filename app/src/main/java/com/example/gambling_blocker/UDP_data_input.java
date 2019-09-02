package com.example.gambling_blocker;
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

               // select the ready channel if the ready channel is 0
                if (readyChannels == 0) {
                    Thread.sleep(10);
                    continue;
                }

                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = keys.iterator();

                //  traverse the selection keys to find the ready channel
                while (keyIterator.hasNext() && !Thread.interrupted())
                {
                    SelectionKey key = keyIterator.next();
                    if (key.isValid() && key.isReadable())
                    {
                        // get the ByteBuffer from the ByteBuffer pool
                        ByteBuffer receiveBuffer = ByteBufferPool.acquire();

                        // for the space for UDP header
                        receiveBuffer.position(HEADER_SIZE);

                        // assign the selected key channel to the input channel
                        DatagramChannel inputChannel = (DatagramChannel) key.channel();

                        int readBytes = inputChannel.read(receiveBuffer);

                        // attach an reference IP packet
                        IPPacket referencePacket = (IPPacket) key.attachment();
                        referencePacket.updateUDPBuffer(receiveBuffer, readBytes);
                        receiveBuffer.position(HEADER_SIZE + readBytes);
                        /*
                        build the new UDP buffer and send it to the UDP output queue
                         */
                        outputQueue.offer(receiveBuffer);
                        keyIterator.remove();
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
