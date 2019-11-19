package com.hlebon.client.task.message;

public class MessageRequest {
    private final int identifier;
    private final int sequenceNumber;
    private final int value;
    private final int checkSum;

    public MessageRequest(final int identifier, final int sequenceNumber, final int value, final int checkSum) {
        this.identifier = identifier;
        this.sequenceNumber = sequenceNumber;
        this.value = value;
        this.checkSum = checkSum;
    }

    public int getIdentifier() {
        return identifier;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public int getValue() {
        return value;
    }

    public int getCheckSum() {
        return checkSum;
    }
}
