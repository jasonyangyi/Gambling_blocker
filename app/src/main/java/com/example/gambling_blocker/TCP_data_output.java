package com.example.gambling_blocker;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TCP_data_output implements Runnable {

    private Gambling_Block_Service vpnService;
    private ConcurrentLinkedQueue<IPPacket> inputQueue;
    private ConcurrentLinkedQueue<ByteBuffer> outputQueue;
    private Selector selector;

    private Random random = new Random();
    public TCP_data_output(ConcurrentLinkedQueue<IPPacket> inputQueue, ConcurrentLinkedQueue<ByteBuffer> outputQueue,
                     Selector selector, Gambling_Block_Service vpnService)
    {
        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;
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
                    currentPacket = inputQueue.poll(); // get the IP packet from the input queue
                    if (currentPacket != null)
                        break;
                    Thread.sleep(10);
                } while (!currentThread.isInterrupted());

                if (currentThread.isInterrupted())
                    break;

                ByteBuffer payloadBuffer = currentPacket.backingBuffer;
                currentPacket.backingBuffer = null;
                ByteBuffer responseBuffer = ByteBufferPool.acquire();
                // acquire the buffer from ByteBuffer pool

                InetAddress destinationAddress = currentPacket.ip4Header.destinationAddress;

                IPPacket.TCPHeader tcpHeader = currentPacket.tcpHeader;
                int destinationPort = tcpHeader.destinationPort;  // define the destination port
                int sourcePort = tcpHeader.sourcePort;  // define the source port

                String ipAndPort = destinationAddress.getHostAddress() + ":" +
                        destinationPort + ":" + sourcePort;
                TCB tcb = TCB.getTCB(ipAndPort);  // TCP handshake protocol
                if (tcb == null)
                    initializeConnection(ipAndPort, destinationAddress, destinationPort,
                            currentPacket, tcpHeader, responseBuffer);
                else if (tcpHeader.isSYN()) // if receive the SYN
                    processDuplicateSYN(tcb, tcpHeader, responseBuffer);
                else if (tcpHeader.isRST())  // if receives the RST
                    closeCleanly(tcb, responseBuffer);
                else if (tcpHeader.isFIN())  // if receives the FIN
                    processFIN(tcb, tcpHeader, responseBuffer);
                else if (tcpHeader.isACK())  // if receives the ACK
                    processACK(tcb, tcpHeader, payloadBuffer, responseBuffer);

                if (responseBuffer.position() == 0)
                    ByteBufferPool.release(responseBuffer);
                ByteBufferPool.release(payloadBuffer);
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
            TCB.closeAll();
        }
    }

    private void initializeConnection(String ipAndPort, InetAddress destinationAddress, int destinationPort,
                                      IPPacket currentPacket, IPPacket.TCPHeader tcpHeader, ByteBuffer responseBuffer)
            throws IOException
    {
        currentPacket.swapSourceAndDestination(); // generate the new source address and destination address
        if (tcpHeader.isSYN())
        {
            // if it sends the syn
            SocketChannel outputChannel = SocketChannel.open();
            outputChannel.configureBlocking(false);
            vpnService.protect(outputChannel.socket()); // to avoid the loop

            TCB tcb = new TCB(ipAndPort, random.nextInt(Short.MAX_VALUE + 1), tcpHeader.sequenceNumber, tcpHeader.sequenceNumber + 1,
                    tcpHeader.acknowledgementNumber, outputChannel, currentPacket);
            TCB.putTCB(ipAndPort, tcb);

            try
            {
                outputChannel.connect(new InetSocketAddress(destinationAddress, destinationPort)); // connect to the remote server
                if (outputChannel.finishConnect())
                {
                    tcb.status = TCB.TCBStatus.SYN_RECEIVED; // if it receives the syn
                    currentPacket.updateTCPBuffer(responseBuffer, (byte) (IPPacket.TCPHeader.SYN | IPPacket.TCPHeader.ACK),
                            tcb.mySequenceNum, tcb.myAcknowledgementNum, 0); // use syn  and ACk to update the TCP buffer
                    tcb.mySequenceNum++; //  sequence number +1
                }
                else
                {
                    tcb.status = TCB.TCBStatus.SYN_SENT;  // if not connected prepare to be connected
                    selector.wakeup();
                    tcb.selectionKey = outputChannel.register(selector, SelectionKey.OP_CONNECT, tcb);
                    return;
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
                currentPacket.updateTCPBuffer(responseBuffer, (byte) IPPacket.TCPHeader.RST, 0, tcb.myAcknowledgementNum, 0);
                TCB.closeTCB(tcb);
            }
        }
        else
        {
            /*
            when it receive the RST
            close the connection (an exception occurs)
             */
            currentPacket.updateTCPBuffer(responseBuffer, (byte) IPPacket.TCPHeader.RST,
                    0, tcpHeader.sequenceNumber + 1, 0);
        }
        outputQueue.offer(responseBuffer);
    }

    private void processDuplicateSYN(TCB tcb, IPPacket.TCPHeader tcpHeader, ByteBuffer responseBuffer)
    {
        /*
        in TCP three hand-shake protocol
        in one transmission block
        if the SYN received
        it returns ack = seq +1
         */
        synchronized (tcb)
        {
            if (tcb.status == TCB.TCBStatus.SYN_SENT)
            {
                tcb.myAcknowledgementNum = tcpHeader.sequenceNumber + 1;
                return;
            }
        }
        sendRST(tcb, 1, responseBuffer);
    }

    private void processFIN(TCB tcb, IPPacket.TCPHeader tcpHeader, ByteBuffer responseBuffer)
    {
        /*
        the process of the FIN is to close the TCP connection
         */
        synchronized (tcb)
        {
            IPPacket referencePacket = tcb.referencePacket;
            tcb.myAcknowledgementNum = tcpHeader.sequenceNumber + 1;
            tcb.theirAcknowledgementNum = tcpHeader.acknowledgementNumber;
            /*
            the client receives the ack from the server
            wait for the network data
             */

            if (tcb.waitingForNetworkData)
            {
                /*
                in the waiting state
                wait for the data to come in
                update the TCP buffer
                 */
                tcb.status = TCB.TCBStatus.CLOSE_WAIT;
                referencePacket.updateTCPBuffer(responseBuffer, (byte) IPPacket.TCPHeader.ACK,
                        tcb.mySequenceNum, tcb.myAcknowledgementNum, 0);
            }
            else
            {
                /*
                when it receives the last ack
                no data transmission
                update TCP buffer
                 */
                tcb.status = TCB.TCBStatus.LAST_ACK;
                referencePacket.updateTCPBuffer(responseBuffer, (byte) (IPPacket.TCPHeader.FIN | IPPacket.TCPHeader.ACK),
                        tcb.mySequenceNum, tcb.myAcknowledgementNum, 0);
                tcb.mySequenceNum++; // FIN counts as a byte
            }
        }
        // put the updated buffer in the output queue
        outputQueue.offer(responseBuffer);
    }

    private void processACK(TCB tcb, IPPacket.TCPHeader tcpHeader, ByteBuffer payloadBuffer, ByteBuffer responseBuffer) throws IOException
    {
        /*
        this function is used to process
        the ACK signal
        in TCP connection and disconnection
         */
        int payloadSize = payloadBuffer.limit() - payloadBuffer.position();

        synchronized (tcb)
        {
            SocketChannel outputChannel = tcb.channel;
            if (tcb.status == TCB.TCBStatus.SYN_RECEIVED)
            {
                tcb.status = TCB.TCBStatus.ESTABLISHED;
                selector.wakeup();
                tcb.selectionKey = outputChannel.register(selector, SelectionKey.OP_READ, tcb);
                tcb.waitingForNetworkData = true;
            }
            else if (tcb.status == TCB.TCBStatus.LAST_ACK)
            {
                closeCleanly(tcb, responseBuffer);
                return;
            }

            if (payloadSize == 0) return; // Empty ACK, ignore

            if (!tcb.waitingForNetworkData)
            {
                selector.wakeup();
                tcb.selectionKey.interestOps(SelectionKey.OP_READ);
                tcb.waitingForNetworkData = true;
            }

            // Forward to remote server
            try
            {
                while (payloadBuffer.hasRemaining())
                    outputChannel.write(payloadBuffer);
            }
            catch (IOException e)
            {
                e.printStackTrace();
                sendRST(tcb, payloadSize, responseBuffer);
                return;
            }
            tcb.myAcknowledgementNum = tcpHeader.sequenceNumber + payloadSize;
            tcb.theirAcknowledgementNum = tcpHeader.acknowledgementNumber;
            IPPacket referencePacket = tcb.referencePacket;
            referencePacket.updateTCPBuffer(responseBuffer, (byte) IPPacket.TCPHeader.ACK, tcb.mySequenceNum, tcb.myAcknowledgementNum, 0);
        }
        outputQueue.offer(responseBuffer);
    }

    private void sendRST(TCB tcb, int prevPayloadSize, ByteBuffer buffer)
    {
        /*
        the RST:
        when an exception occurs
         update the TCP buffer and close the connection
         */
        tcb.referencePacket.updateTCPBuffer(buffer, (byte) IPPacket.TCPHeader.RST, 0, tcb.myAcknowledgementNum + prevPayloadSize, 0);
        outputQueue.offer(buffer);
        TCB.closeTCB(tcb);
    }

    private void closeCleanly(TCB tcb, ByteBuffer buffer)
    {
        ByteBufferPool.release(buffer);
        TCB.closeTCB(tcb);
    }
}

