package io.nats.java.internal;

import io.nats.java.ClientErrorHandler;
import io.nats.java.InputQueue;
import io.nats.java.internal.actions.OutputQueue;
import io.nats.java.internal.actions.client.Connect;

import java.time.Duration;

public class ClientActorBuilder {
    /**
     * Input IO sits on the other side of this channel.
     */
    private InputQueue<ServerMessage> serverInputChannel;


    /**
     * Output IO sits on the other side of this channel.
     */
    private OutputQueue<Action> serverOutputChannel;

    /**
     * How long to pause/poll when there are no Server Messages in the queue.
     */
    private Duration pauseDuration = Duration.ofMillis(50);

    /**
     * Error handler if you are interested in such.
     */
    private ClientErrorHandler errorHandler;

    /**
     * Connection Info to send to the server after we get INFO from the server.
     */
    private Connect connectInfo;

    public InputQueue<ServerMessage> getServerInputChannel() {
        return serverInputChannel;
    }

    public ClientActorBuilder withServerInputChannel(InputQueue<ServerMessage> serverInputChannel) {
        this.serverInputChannel = serverInputChannel;
        return this;
    }

    public OutputQueue<Action> getServerOutputChannel() {
        return serverOutputChannel;
    }

    public ClientActorBuilder withServerOutputChannel(OutputQueue<Action> serverOutputChannel) {
        this.serverOutputChannel = serverOutputChannel;
        return this;
    }

    public Duration getPauseDuration() {
        return pauseDuration;
    }

    public ClientActorBuilder withPauseDuration(Duration pauseDuration) {
        this.pauseDuration = pauseDuration;
        return this;
    }

    public ClientErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public ClientActorBuilder withErrorHandler(ClientErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
        return this;
    }

    public Connect getConnectInfo() {
        return connectInfo;
    }

    public ClientActorBuilder withConnectInfo(Connect connectInfo) {
        this.connectInfo = connectInfo;
        return this;
    }

    public ClientActor build() {
        return new ClientActor(this.getServerInputChannel(), this.getServerOutputChannel(), this.getPauseDuration(),
                this.getErrorHandler(),
                this.getConnectInfo());
    }

    public static ClientActorBuilder builder() {
        return new ClientActorBuilder();
    }
}
