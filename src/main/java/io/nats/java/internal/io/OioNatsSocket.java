package io.nats.java.internal.io;

import io.nats.java.internal.ServerMessage;
import io.nats.java.internal.actions.OutputQueue;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

public class OioNatsSocket {

    private final Socket clientSocket;
    private final InputStream inputStream;
    private final ExecutorService executorService;
    private final OutputQueue<ServerMessage> serverMessages;



    public OioNatsSocket(final String ip, final int port, final ExecutorService executorService, OutputQueue<ServerMessage> serverMessages) throws IOException {
        this.executorService = executorService;
        this.serverMessages = serverMessages;
        this.clientSocket = new Socket(ip, port);
        this.inputStream = clientSocket.getInputStream();
        NatsIOReader reader = new NatsIOReader(inputStream, clientSocket, serverMessages);
    }




}
