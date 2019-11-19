package com.hlebon.handler;

import com.hlebon.handler.message.Message;
import com.hlebon.handler.message.MessageResponse;
import com.hlebon.service.MessageSender;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.zip.CRC32;

@Service
public class MessageHandler implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);

    private BlockingQueue<Message> queue = new ArrayBlockingQueue<>(1024);
    private final MessageSender messageSender;
    private final Map<Integer, Integer> lastSequenceNumber = new HashMap<>(); // can be a cache here
    private final Map<Message, Integer> resendCount = new HashMap<>();
    private final static int MAX_NACK_RESEND = 3;

    public MessageHandler(final MessageSender messageSender) {
        this.messageSender = messageSender;
    }

    public void addMessage(final Message message) {
        queue.add(message);
    }

    @Override
    public void run() {
        while (true) { // grace stop
            final Message message;
            try {
                message = queue.take();
            } catch (final InterruptedException e) {
                continue;
            }
            logger.debug("Handle message: Identifier - {}, SequenceNumber - {}, Value - {}, CheckSum - {}",
                    message.getIdentifier(), message.getSequenceNumber(), message.getValue(), message.getCheckSum()
            );
            try {
                validateCheckSum(message);
                validateSequenceNumber(message);
                logger.debug("Message is VALID");
                lastSequenceNumber.put(message.getIdentifier(), message.getSequenceNumber());
                saveMessage(message);
                // Send ACK
                logger.debug("Send ACK message");
                sendMessage(
                        generateAckMessage(
                                message.getIdentifier(),
                                message.getSequenceNumber(),
                                message.getSourceAddress(),
                                message.getSourcePort()
                        ));
            } catch (final ValidationException e) {
                logger.debug("Message is INVALID");
                // Send NACK
                final Integer count = resendCount.get(message);
                logger.debug("Count of this message - {}", count);
                if (count == null) {
                    resendCount.put(message, 1);
                } else {
                    if (count == MAX_NACK_RESEND) {
                        logger.warn("Maximum Nack messages was reached");
                        // Save missing message
                        try {
                            final CSVPrinter printer = new CSVPrinter(new FileWriter(message.getIdentifier() + ".missed", true), CSVFormat.DEFAULT);
                            printer.printRecord(message.getSequenceNumber(), message.getValue());
                            printer.close();
                        } catch (IOException e1) {
                            // do nothing
                        }
                        resendCount.remove(message);
                        return;
                    } else {
                        resendCount.put(message, count + 1);
                    }
                }
                logger.debug("Send NACK message");
                sendMessage(
                        generateNackMessage(
                                message.getIdentifier(),
                                message.getSequenceNumber(),
                                message.getSourceAddress(),
                                message.getSourcePort()
                        ));
            }
        }
    }

    private void saveMessage(final Message message) {
        // Save values
        try {
            final CSVPrinter printer = new CSVPrinter(new FileWriter(message.getIdentifier() + ".values", true), CSVFormat.DEFAULT);
            printer.printRecord(message.getSequenceNumber(), message.getValue());
            printer.close();
        } catch (final IOException e) {
            // do nothing
        }
        // Save sum
        try {
            final CSVPrinter printer = new CSVPrinter(new FileWriter(message.getIdentifier() + ".sum", true), CSVFormat.DEFAULT);
            printer.printRecord(message.getSequenceNumber(), message.getCheckSum());
            printer.close();
        } catch (final IOException e) {
            // do nothing
        }
    }

    /* default */ void validateCheckSum(final Message message) throws ValidationException {
        final CRC32 crc32 = new CRC32();
        final ByteBuffer byteBuffer = ByteBuffer.allocate(12);
        byteBuffer.putInt(message.getIdentifier());
        byteBuffer.putInt(message.getSequenceNumber());
        byteBuffer.putInt(message.getValue());
        byteBuffer.flip();
        crc32.update(byteBuffer);
        final long expected = crc32.getValue();
        final int actual = message.getCheckSum();
        if (LongToInt(expected) != actual) {
            logger.warn("Incorrect Check Sum: expected - {}, actual - {}", expected, actual);
            throw new ValidationException("Incorrect Check Sum");
        }
    }

    private void validateSequenceNumber(final Message message) throws ValidationException {
        final Path path = Paths.get(message.getIdentifier() + ".values");
        final int lastSequenceNumber;
        if (Files.exists(path)) {
            try {
                lastSequenceNumber = getLastSequenceNumber(message.getIdentifier());
            } catch (final IOException e) {
                logger.error("Can't get LastSequenceNumber from file");
                throw new ValidationException(e.getMessage());
            }
        } else {
            lastSequenceNumber = 0;
        }

        logger.debug("SequenceNumber: expected - {}, actual - {}", lastSequenceNumber + 1, message.getSequenceNumber());
        if (lastSequenceNumber + 1 != message.getSequenceNumber()) {
            throw new ValidationException("Sequence Number is wrong");
        }
    }

    private Integer getLastSequenceNumber(final int identifier) throws IOException {
        if (lastSequenceNumber.get(identifier) != null) {
            return lastSequenceNumber.get(identifier);
        }
        final Path path = Paths.get(identifier + ".values");
        final RandomAccessFile file = new RandomAccessFile(path.toAbsolutePath().toFile(), "r");
        if (file.length() > 25) {
            file.seek(file.length() - 25);
        }
        int b = file.read();
        String res = "";

        // read the string;
        while (b != -1) {
            res = res + (char) b;

            b = file.read();
        }
        final String[] parts = res.split(",");
        final String partWithLastSequenceNumber = parts[parts.length - 2];
        final int indexOfLineSeparator = partWithLastSequenceNumber.indexOf('\n');
        Integer lastSequenceNumber;
        if (indexOfLineSeparator < 0) {
            lastSequenceNumber = Integer.valueOf(partWithLastSequenceNumber);
        } else {
            lastSequenceNumber = Integer.valueOf(partWithLastSequenceNumber.substring(indexOfLineSeparator + 1));
        }
        file.close();
        return lastSequenceNumber;
    }

    private MessageResponse generateAckMessage(
            final int identifier,
            final int sequenceNumber,
            final InetAddress address,
            final int port
    ) {
        return new MessageResponse(
                identifier,
                sequenceNumber,
                (byte) 1,
                address,
                port
        );
    }

    private MessageResponse generateNackMessage(
            final int identifier,
            final int sequenceNumber,
            final InetAddress address,
            final int port
    ) {
        return new MessageResponse(
                identifier,
                sequenceNumber,
                (byte) 0,
                address,
                port
        );
    }

    private void sendMessage(final MessageResponse messageResponse) {
        messageSender.addMessage(messageResponse);
    }

    private static int LongToInt(long value) {
        return (int) (value & 0xFFFFFFFFL);
    }
}
