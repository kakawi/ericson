package com.hlebon.client.task.message;

public class MessageResponse {

    private final int identifier;
    private final int sequenceNumber;
    private final byte ack;

    public MessageResponse(final int identifier, final int sequenceNumber, final byte ack) {
        this.identifier = identifier;
        this.sequenceNumber = sequenceNumber;
        this.ack = ack;
    }

    public int getIdentifier() {
        return identifier;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public byte getAck() {
        return ack;
    }
}
