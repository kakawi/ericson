package com.hlebon.client.task;

import com.hlebon.client.task.message.MessageRequest;
import com.hlebon.client.task.message.MessageResponse;
import com.hlebon.client.service.MessageSender;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;

public class MessageTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(MessageTask.class);

    private final MessageRequest messageRequest;
    private final MessageSender clientMessageSender;

    public MessageTask(
            final MessageRequest messageRequest,
            final MessageSender clientMessageSender
    ) {
        this.messageRequest = messageRequest;
        this.clientMessageSender = clientMessageSender;
    }

    @Override
    public void run() {
        logger.debug("Start task");
        final Optional<MessageResponse> messageResponseOptional = clientMessageSender.sendMessage(messageRequest);
        if (!messageResponseOptional.isPresent()) {
            logger.debug("Client didn't get an ACK message");
            try {
                final CSVPrinter printer = new CSVPrinter(new FileWriter(messageRequest.getIdentifier() + ".lost", true), CSVFormat.DEFAULT);
                printer.printRecord(messageRequest.getSequenceNumber(), messageRequest.getValue());
                printer.close();
            } catch (final IOException e) {
                logger.error("MessageTask error", e);
            }
            return;
        }
        final MessageResponse messageResponse = messageResponseOptional.get();
        if (messageResponse.getAck() == 1) {
            logger.info("The message is ACK");
        } else {
            logger.info("The message is NACK");
        }
    }
}
