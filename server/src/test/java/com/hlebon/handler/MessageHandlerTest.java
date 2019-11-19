package com.hlebon.handler;

import com.hlebon.handler.message.Message;
import com.hlebon.service.MessageSender;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.InetAddress;

@RunWith(MockitoJUnitRunner.class)
public class MessageHandlerTest {

    @Mock
    private MessageSender messageSender;

    @InjectMocks
    private MessageHandler messageHandler;

    @Test
    public void validateCheckSum() throws ValidationException {
        // prerequisites
        int identifier = 1;
        int sequenceNumber = 2;
        int value = 3;
        int checkSum = -1889021706;


        final Message message = new Message(
                identifier,
                sequenceNumber,
                value,
                checkSum,
                null,
                100
        );

        // test
        messageHandler.validateCheckSum(message);
    }
}
