package io.nats.java;

public class ConnectURL {
    private final String ipAddress;
    private final int port;

    public ConnectURL(String ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;
    }

}
