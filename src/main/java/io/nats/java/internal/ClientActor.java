package io.nats.java.internal;

import io.nats.java.ClientErrorHandler;
import io.nats.java.internal.actions.server.ServerInformation;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientActor {

    private final Queue<ServerMessage> queue;
    private final Duration pauseDuration;
    private final ClientErrorHandler errorHandler;


    private AtomicBoolean doStop = new AtomicBoolean();
    private boolean connected = false;
    private ServerInformation serverInformation;

    public ClientActor(Queue<ServerMessage> queue, Duration pauseDuration, ClientErrorHandler errorHandler) {
        this.queue = queue;
        this.pauseDuration = pauseDuration;
        this.errorHandler = errorHandler;

    }

    public void stop() {
        doStop.set(true);
    }

    private void run() {

        boolean pause = true;
        loop_exit:
        while (!doStop.get()) {
            for (int index = 0; index < 100; index++) {

                QueueMessage<ServerMessage> next = queue.next();
                if (next.doStop()) {
                    break loop_exit;
                } else if (next.isError()) {
                    errorHandler.handleError(next.error());
                    break loop_exit;
                } else if (!next.isPresent()) {
                    pause = true;
                    handleServerMessage(next.value());
                } else {
                    pause = false;
                }

            }
        }
    }

    private void handleServerMessage(final ServerMessage message) {

        switch (message.getVerb() ) {
            case INFO:
                handleServerInfo(ServerInformation.parse(message.getBytes()));
        }

    }

    private void handleServerInfo(final ServerInformation newServerInfo) {
        if (!connected) {
            serverInformation = newServerInfo;
        } else {
            if (serverInformation==null) {
                serverInformation = newServerInfo;
            } else {
                serverInformation = serverInformation.merge(newServerInfo);
            }
        }
    }
}
