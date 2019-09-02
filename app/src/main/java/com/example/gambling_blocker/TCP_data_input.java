package com.example.gambling_blocker;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TCP_data_input implements Runnable {

    // this class used to process the TCP data in
    private static final int HEADER_SIZE = IPPacket.IP4_HEADER_SIZE + IPPacket.TCP_HEADER_SIZE;
    private ConcurrentLinkedQueue<ByteBuffer> outputQueue;
    private Selector selector;  // the selector used to manage the channel

    public TCP_data_input(ConcurrentLinkedQueue<ByteBuffer> outputQueue, Selector selector)
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
                    if (key.isValid())
                    {
                        if (key.isConnectable())
                            processConnect(key, keyIterator);
                        else if (key.isReadable())
                            processInput(key, keyIterator);
                    }
                }
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

    private void processConnect(SelectionKey key, Iterator<SelectionKey> keyIterator)
    {
        TCB tcb = (TCB) key.attachment(); // attach a tcb object
        IPPacket referencePacket = tcb.referencePacket;
        try
        {
            if (tcb.channel.finishConnect())
            {
                keyIterator.remove();
                /*
                if receive the syn
                update the TCP buffer
                insert this buffer to the output queue
                 */
                tcb.status = TCB.TCBStatus.SYN_RECEIVED;
                ByteBuffer responseBuffer = ByteBufferPool.acquire();
                referencePacket.updateTCPBuffer(responseBuffer, (byte) (IPPacket.TCPHeader.SYN | IPPacket.TCPHeader.ACK),
                        tcb.mySequenceNum, tcb.myAcknowledgementNum, 0);
                outputQueue.offer(responseBuffer);

                tcb.mySequenceNum++; //  seq+1
                key.interestOps(SelectionKey.OP_READ);
            }
        }
        catch (IOException e)
        {
            ByteBuffer responseBuffer = ByteBufferPool.acquire();
            referencePacket.updateTCPBuffer(responseBuffer, (byte) IPPacket.TCPHeader.RST, 0, tcb.myAcknowledgementNum, 0);
            outputQueue.offer(responseBuffer);
            TCB.closeTCB(tcb);
        }
    }

    private void processInput(SelectionKey key, Iterator<SelectionKey> keyIterator)
    {
        keyIterator.remove();
        ByteBuffer receiveBuffer = ByteBufferPool.acquire();
        //  this space is left for adding the header
        receiveBuffer.position(HEADER_SIZE);

        // attach an tcb reference
        TCB tcb = (TCB) key.attachment();
        synchronized (tcb)
        {
            IPPacket referencePacket = tcb.referencePacket;
            SocketChannel inputChannel = (SocketChannel) key.channel();
            /*
            open the input channel and read the packet
             */
            int readBytes;
            try
            {
                readBytes = inputChannel.read(receiveBuffer);
            }
            catch (IOException e)
            {
                referencePacket.updateTCPBuffer(receiveBuffer, (byte) IPPacket.TCPHeader.RST, 0, tcb.myAcknowledgementNum, 0);
                outputQueue.offer(receiveBuffer);
                TCB.closeTCB(tcb);
                return;
            }

            if (readBytes == -1)
            {
                // if no packet received
                key.interestOps(0);
                tcb.waitingForNetworkData = false;

                if (tcb.status != TCB.TCBStatus.CLOSE_WAIT)
                {
                    ByteBufferPool.release(receiveBuffer);
                    return;
                }
                tcb.status = TCB.TCBStatus.LAST_ACK;
                referencePacket.updateTCPBuffer(receiveBuffer, (byte) IPPacket.TCPHeader.FIN, tcb.mySequenceNum, tcb.myAcknowledgementNum, 0);
                tcb.mySequenceNum++;
            }
            else
            {
                referencePacket.updateTCPBuffer(receiveBuffer, (byte) (IPPacket.TCPHeader.PSH | IPPacket.TCPHeader.ACK),
                        tcb.mySequenceNum, tcb.myAcknowledgementNum, readBytes);
                tcb.mySequenceNum += readBytes; // Next sequence number
                receiveBuffer.position(HEADER_SIZE + readBytes);
            }
        }
        outputQueue.offer(receiveBuffer);
    }


}
