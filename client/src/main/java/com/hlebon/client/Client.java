package com.hlebon.client;

import com.hlebon.client.service.MessageSender;
import com.hlebon.client.task.message.MessageRequest;
import com.hlebon.client.task.MessageTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.CRC32;

public class Client {

    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    private static final int THREAD_POOL = 100;
    private static final Random RANDOM = new Random();
    private static final int CLIENT_MESSAGE_DELAY = 10;

    public static void main(final String[] args) {
        final MessageSender clientMessageSender = new MessageSender();

        final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL);
        int identifier = RANDOM.nextInt() & Integer.MAX_VALUE;
        int sequenceNumber = 1;
        while (true) {
            // generate message
            int value = RANDOM.nextInt();
            int checkSum = generateCheckSum(identifier, sequenceNumber, value);
            logger.debug("Generated message Identifier - {}, SequenceNumber - {}, Value - {}, CheckSum - {}",
                    identifier, sequenceNumber, value, checkSum
            );
            final MessageRequest messageRequest = new MessageRequest(
                    identifier, sequenceNumber, value, checkSum
            );

            // send
            executorService.submit(new MessageTask(messageRequest, clientMessageSender));
            sequenceNumber++;
            delay(CLIENT_MESSAGE_DELAY);
        }

    }

    private static void delay(final long delay) {
        try {
            Thread.sleep(delay);
        } catch (final InterruptedException e) {
            // do nothing
        }
    }

    private static int generateCheckSum(final int identifier, final int sequenceNumber, final int value) {
        final CRC32 crc32 = new CRC32();
        final ByteBuffer byteBuffer = ByteBuffer.allocate(12);
        byteBuffer.putInt(identifier);
        byteBuffer.putInt(sequenceNumber);
        byteBuffer.putInt(value);
        byteBuffer.flip();
        crc32.update(byteBuffer);
        return LongToInt(crc32.getValue());
    }

    private static int LongToInt(long value) {
        return (int) (value & 0xFFFFFFFFL);
    }
}
