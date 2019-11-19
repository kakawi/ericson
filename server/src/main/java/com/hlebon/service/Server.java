package com.hlebon.service;

import com.hlebon.handler.message.Message;
import com.hlebon.handler.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

@Service
public class Server implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    private final MessageHandler messageHandler;
    private final DatagramSocket datagramSocket;

    @Autowired
    public Server(
            final MessageHandler messageHandler,
            final DatagramSocket datagramSocket
    ) {
        this.messageHandler = messageHandler;
        this.datagramSocket = datagramSocket;
    }

    public void run() {
        byte[] receiveData = new byte[16];
        byte[] sendData = new byte[1024];
        logger.info("The server has started");
        while (true) {
            try {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                this.datagramSocket.receive(receivePacket);
                int identifier = ByteBuffer.wrap(receiveData, 0, 4).getInt();
                int sequenceNumber = ByteBuffer.wrap(receiveData, 4, 4).getInt();
                int value = ByteBuffer.wrap(receiveData, 8, 4).getInt();
                int checkSum = ByteBuffer.wrap(receiveData, 12, 4).getInt();
                InetAddress sourceAddress = InetAddress.getLocalHost();
                int sourcePort = receivePacket.getPort();
                logger.debug("Received message from Client Identifier - {}, SequenceNumber - {}, Value - {}, CheckSum - {}, Host - {}, Port - {}",
                        identifier, sequenceNumber, value, checkSum, sourceAddress, sourcePort);
                final Message message = new Message(
                        identifier,
                        sequenceNumber,
                        value,
                        checkSum,
                        sourceAddress,
                        sourcePort
                );
                messageHandler.addMessage(message);
            } catch (final IOException e) {
                logger.error("Server Error", e);
            }
        }
    }
}
