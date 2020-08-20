package io.nats.java.internal;

import java.nio.charset.StandardCharsets;

public class ServerMessage implements VerbHolder {
    private final byte[] bytes;
    private final NATSProtocolVerb verb;

    private final int[] indexes;

    public ServerMessage(byte[] bytes, NATSProtocolVerb verb, int[] indexes) {
        this.bytes = bytes;
        this.verb = verb;
        this.indexes = indexes;
    }

    public ServerMessage(byte[] bytes, NATSProtocolVerb verb) {
        this.bytes = bytes;
        this.verb = verb;
        this.indexes = null;
    }


    public byte[] getBytes() {
        return bytes;
    }

    public NATSProtocolVerb verb() {
        return verb;
    }


    public String firstArgAsString() {
        int start = indexes[0];
        int end = indexes[1];
        return getString(start, end);
    }

    public String secondArgAsString() {
        int start = indexes[2];
        int end = indexes[3];
        return getString(start, end);
    }

    public String thirdArgAsString() {
        int start = indexes[4];
        int end = indexes[5];
        return getString(start, end);
    }

    public String fourthArgAsString() {
        int start = indexes[6];
        int end = indexes[7];
        return getString(start, end);
    }

    public String fifthArgAsString() {
        int start = indexes[8];
        int end = indexes[9];
        return getString(start, end);
    }

    public byte[] fourthArgAsBytes() {
        int start = indexes[6];
        int end = indexes[7];
        return getBytes(start, end);
    }

    public byte[] fifthArgAsBytes() {
        int start = indexes[8];
        int end = indexes[9];
        return getBytes(start, end);
    }

    private String getString(int start, int end) {
        return new String(bytes, start, end - start, StandardCharsets.UTF_8).intern();
    }

    private byte[] getBytes(int start, int end) {
        final int length = end - start;
        final byte[] bytes = new byte[length];
        System.arraycopy(this.bytes, start, bytes, 0, length);
        return bytes;
    }

    @Override
    public String toString() {
        return "ServerMessage{" +
                "verb=" + verb +
                '}';
    }
}
