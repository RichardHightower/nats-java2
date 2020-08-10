package io.nats.java.internal;

public class ServerMessage {
    private final byte[] bytes;
    private final NATSProtocolVerb verb;

    public ServerMessage(byte[] bytes, NATSProtocolVerb verb) {
        this.bytes = bytes;
        this.verb = verb;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public NATSProtocolVerb getVerb() {
        return verb;
    }
    
}
