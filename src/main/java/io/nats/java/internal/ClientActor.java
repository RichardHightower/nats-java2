package io.nats.java.internal;

import io.nats.java.ClientErrorHandler;
import io.nats.java.InputQueue;
import io.nats.java.InputQueueMessage;
import io.nats.java.internal.actions.OutputQueue;
import io.nats.java.internal.actions.PingPong;
import io.nats.java.internal.actions.ServerErrorException;
import io.nats.java.internal.actions.client.Connect;
import io.nats.java.internal.actions.client.Disconnect;
import io.nats.java.internal.actions.client.Subscribe;
import io.nats.java.internal.actions.client.Unsubscribe;
import io.nats.java.internal.actions.server.ReceiveMessage;
import io.nats.java.internal.actions.server.ServerError;
import io.nats.java.internal.actions.server.ServerInformation;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientActor {

    private final InputQueue<io.nats.java.internal.ServerMessage> input;
    private final Duration pauseDuration;
    private final ClientErrorHandler errorHandler;
    private final OutputQueue<Action> output;
    private final Connect connectInfo;


    private AtomicBoolean doStop = new AtomicBoolean();
    private boolean connected = false;
    private ServerInformation serverInformation;

    public ClientActor(final InputQueue<io.nats.java.internal.ServerMessage> queue,
                       final OutputQueue<Action> output,
                       final Duration pauseDuration,
                       final ClientErrorHandler errorHandler,
                       final Connect connectInfo) {
        this.input = queue;
        this.pauseDuration = pauseDuration;
        this.errorHandler = errorHandler;
        this.output = output;
        this.connectInfo = connectInfo;
    }

    public void stop() {
        doStop.set(true);
    }

    private void run() {

        try {
            boolean pause = false;
            InputQueueMessage<io.nats.java.internal.ServerMessage> next;
            Exception lastError = null;
            loop_exit:
            while (!doStop.get()) {
                for (int index = 0; index < 100; index++) {

                    next = !pause ? input.next() : input.next(pauseDuration);
                    if (next.isDone()) {
                        break loop_exit;
                    } else if (next.isError()) {
                        errorHandler.handleError(next.error());
                        lastError = next.error();
                        break loop_exit;
                    } else if (!next.isPresent()) {
                        pause = true;
                        handleServerMessage(next.value());
                    } else {
                        pause = false;
                    }

                }
            }

            if (doStop.get()) {
                output.send(Disconnect.DISCONNECT);
                this.connected = false;
            } else if (lastError != null) {
                output.send(Disconnect.DISCONNECT);
                this.connected = false;
                //TODO maybe log this
            }
        } catch (Exception exception) {
            output.send(Disconnect.DISCONNECT);
            this.connected = false;
            errorHandler.handleError(exception);
        }
    }

    private void handleServerMessage(final io.nats.java.internal.ServerMessage message) {

        if (connected) {
            switch (message.getVerb()) {
                case INFO:
                    handleServerInfo(ServerInformation.parse(message.getBytes()));
                case OK:
                    handleOk();
                case PING:
                    handlePing();
                case SUBSCRIBE:
                    handleSubscribe(Subscribe.parse(message.getBytes()));
                case UNSUBSCRIBE:
                    handleUnsubscribe(Unsubscribe.parse(message.getBytes()));
                case MESSAGE:
                    handleMessage(ReceiveMessage.parse(message.getBytes()));
            }
        } else {
            switch (message.getVerb()) {
                case INFO:
                    handleServerConnectInfo(ServerInformation.parse(message.getBytes()));
                case OK:
                    handleOk();
                case PING:
                    handlePing();
                case ERROR:
                    handleError(ServerError.parse(message.getBytes()));
                default:
                    throw new IllegalStateException(String.format("CAN'T RECEIVE %s MESSAGE until connected", message.getVerb()));
            }
        }
    }

    private void handleError(final ServerError serverError) {
        if (!serverError.getErrorType().isKeepConnectionOpen()) {
            throw new ServerErrorException(serverError);
        } else {
            errorHandler.handleError(new ServerErrorException(serverError));
        }
    }

    private void handleMessage(final ReceiveMessage message) {
        //TODO
    }

    private void handleUnsubscribe(final Unsubscribe unsubscribe) {
        //TODO
    }

    private void handleSubscribe(final Subscribe subscribe) {
        //TODO

    }

    private void handleServerInfo(ServerInformation newInfo) {
        if (this.serverInformation != null) {
            this.serverInformation = this.serverInformation.merge(newInfo);
        } else {
            this.serverInformation = newInfo;
        }
    }

    private void handlePing() {
        output.send(PingPong.PONG);
    }

    private void handleOk() {

    }

    private void handleServerConnectInfo(final ServerInformation newServerInfo) {
        serverInformation = newServerInfo;
        output.send(connectInfo);
    }
}
