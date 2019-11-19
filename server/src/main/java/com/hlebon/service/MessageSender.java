package com.hlebon.service;

import com.hlebon.handler.message.MessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Service
public class MessageSender implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(MessageSender.class);

    private final DatagramSocket datagramSocket;

    private BlockingQueue<MessageResponse> queue = new ArrayBlockingQueue<>(1024);

    @Autowired
    public MessageSender(final DatagramSocket datagramSocket) {
        this.datagramSocket = datagramSocket;
    }

    public void addMessage(final MessageResponse message) {
        queue.add(message);
    }

    @Override
    public void run() {
        while (true) {
            try {
                final MessageResponse messageResponse = queue.take();
                final ByteBuffer byteBuffer = ByteBuffer.allocate(9);
                byteBuffer.putInt(messageResponse.getIdentifier());
                byteBuffer.putInt(messageResponse.getSequenceNumber());
                byteBuffer.put(messageResponse.getAck());
                final byte[] array = byteBuffer.array();
                DatagramPacket sendPacket =
                        new DatagramPacket(array, array.length, messageResponse.getAddress(), messageResponse.getPort());
                datagramSocket.send(sendPacket);
            } catch (InterruptedException | IOException e) {
                logger.error("MessageSender error", e);
            }
        }
    }
}
