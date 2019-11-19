package com.hlebon.handler.message;

import java.net.InetAddress;

public class MessageResponse {

    private final int identifier;
    private final int sequenceNumber;
    private final byte ack;
    private final InetAddress address;
    private final int port;

    public MessageResponse(
            final int identifier,
            final int sequenceNumber,
            final byte ack,
            final InetAddress address,
            final int port
    ) {
        this.identifier = identifier;
        this.sequenceNumber = sequenceNumber;
        this.ack = ack;
        this.address = address;
        this.port = port;
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

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }
}
