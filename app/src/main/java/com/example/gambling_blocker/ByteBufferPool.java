package com.example.gambling_blocker;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ByteBufferPool { // construct a Byte Buffer Pool
        private static final int BUFFER_SIZE = 16384; // set the buffer size
        private static ConcurrentLinkedQueue<ByteBuffer> pool = new ConcurrentLinkedQueue<>();

        public static ByteBuffer acquire()
        {
            ByteBuffer buffer = pool.poll();// the queue method used to retrieve and remove the
            if (buffer == null)
                buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
            return buffer;
        }

        public static void release(ByteBuffer buffer)
        {
            buffer.clear();  // make a buffer ready for a new sequence of channel-read or relative operations
            // it sets limit to the capacity and the position to zero
            pool.offer(buffer);   // insert the buffer at the tail of the queue
        }
        public static void clear()
        {
            pool.clear();
        }
}
