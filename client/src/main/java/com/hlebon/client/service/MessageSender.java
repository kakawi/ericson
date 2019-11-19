package com.hlebon.client.service;

import com.hlebon.client.task.message.MessageRequest;
import com.hlebon.client.task.message.MessageResponse;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.Optional;

public class MessageSender {

    private static final Logger logger = LoggerFactory.getLogger(MessageSender.class);

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 9876;
    private static final int WAIT_ACK_MESSAGE = 5000;
    private static final int MAX_RESEND_COUNT = 3;

    public Optional<MessageResponse> sendMessage(final MessageRequest messageRequest) {
        try {
            DatagramSocket clientSocket = new DatagramSocket();
            InetAddress IPAddress = InetAddress.getByName(SERVER_HOST);

            byte[] receiveData = new byte[9];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            boolean getAckMessage = false;
            int sendCount = 0;
            while (!getAckMessage) {
                sendCount++;

                int identifier = messageRequest.getIdentifier();
                int sequenceNumber = messageRequest.getSequenceNumber();
                int value = messageRequest.getValue();
                int checkSum = messageRequest.getCheckSum();

                final ByteBuffer byteBuffer = ByteBuffer.allocate(16);
                byteBuffer.putInt(identifier);
                byteBuffer.putInt(sequenceNumber);
                byteBuffer.putInt(value);
                byteBuffer.putInt(checkSum);
                final byte[] array = byteBuffer.array();

                DatagramPacket sendPacket = new DatagramPacket(array, array.length, IPAddress, SERVER_PORT);
                logger.debug("Send message Identifier - {}, SequenceNumber - {}, Value - {}, CheckSum - {}",
                        identifier, sequenceNumber, value, checkSum
                );
                clientSocket.send(sendPacket);
                clientSocket.setSoTimeout(WAIT_ACK_MESSAGE);

                try {
                    clientSocket.receive(receivePacket);
                    getAckMessage = true;
                    clientSocket.close();
                } catch (SocketTimeoutException e) {
                    logger.warn("Server is TIMEOUT");
                    // resend max 3 times
                    if (sendCount == MAX_RESEND_COUNT) {
                        // save in lost file
                        final CSVPrinter printer = new CSVPrinter(new FileWriter(identifier + ".lost", true), CSVFormat.DEFAULT);
                        printer.printRecord(sequenceNumber, value);
                        printer.close();
                        return Optional.empty();
                    }
                }
            }
            int identifier = ByteBuffer.wrap(receiveData, 0, 4).getInt();
            int sequenceNumber = ByteBuffer.wrap(receiveData, 4, 4).getInt();
            byte ack = ByteBuffer.wrap(receiveData, 8, 1).get();
            return Optional.of(
                    new MessageResponse(
                            identifier, sequenceNumber, ack
                    )
            );
        } catch (final Exception e) {
            logger.error("ClientMessageSender error", e);
            return Optional.empty();
        }
    }
}
