package com.hlebon.handler.message;

import java.net.InetAddress;
import java.util.Objects;

public class Message {

    private final int identifier;
    private final int sequenceNumber;
    private final int value;
    private final int checkSum;
    private final InetAddress sourceAddress;
    private final int sourcePort;

    public Message(final int identifier,
                   final int sequenceNumber,
                   final int value,
                   final int checkSum,
                   final InetAddress sourceAddress,
                   final int sourcePort) {
        this.identifier = identifier;
        this.sequenceNumber = sequenceNumber;
        this.value = value;
        this.checkSum = checkSum;
        this.sourceAddress = sourceAddress;
        this.sourcePort = sourcePort;
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

    public InetAddress getSourceAddress() {
        return sourceAddress;
    }

    public int getSourcePort() {
        return sourcePort;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Message message = (Message) o;
        return identifier == message.identifier &&
                sequenceNumber == message.sequenceNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, sequenceNumber);
    }
}
