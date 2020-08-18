package io.nats.java.internal;

import io.nats.java.ClientErrorHandler;
import io.nats.java.InputQueue;
import io.nats.java.internal.actions.ActionQueue;
import io.nats.java.internal.actions.ActionQueueImpl;
import io.nats.java.internal.actions.OutputQueue;
import io.nats.java.internal.actions.client.Connect;

import java.time.Duration;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;

public class ClientActorBuilder {
    /**
     * Input IO sits on the other side of this channel.
     */
    private InputQueue<ServerMessage> serverInputChannel;


    private ActionQueue clientInputActions;


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

    /**
     * Internal setting for checking for expired subscriptions and such.
     * Defaults to 30 seconds.
     */
    private Duration cleanupDuration = Duration.ofSeconds(30);


    /**
     *
     */
    private Logger logger;

    private boolean verbose;
    private boolean info;

    public InputQueue<ServerMessage> getServerInputChannel() {
        return serverInputChannel;
    }

    public Duration getCleanupDuration() {
        return cleanupDuration;
    }


    public ActionQueue getClientInputActions() {

        if (clientInputActions == null) {
            TransferQueue<Action> actionQueue = new LinkedTransferQueue<>();
            this.clientInputActions = new ActionQueueImpl(actionQueue);
        }
        return clientInputActions;
    }

    public ClientActorBuilder withClientInputActions(ActionQueue clientInputActions) {
        this.clientInputActions = clientInputActions;
        return this;
    }


    public ClientActorBuilder withCleanupDuration(Duration cleanupDuration) {
        this.cleanupDuration = cleanupDuration;
        return this;
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

    public ClientActorBuilder withLogger(final Logger logger) {
        this.logger = logger;
        return this;
    }

    public Logger getLogger() {

        if (logger == null) {
            if (info && verbose) {
                return new Logger() {
                    @Override
                    public boolean isVerbose() {
                        return true;
                    }

                    @Override
                    public void verbose(String string) {
                        System.out.println(string);
                    }

                    @Override
                    public void handleException(String string, Exception exception) {
                        System.out.println(string);
                        exception.printStackTrace();
                    }

                    @Override
                    public boolean isInfo() {
                        return true;
                    }

                    @Override
                    public void info(String string) {
                        System.out.println(string);
                    }
                };
            } else if (verbose) {
                return new Logger() {
                    @Override
                    public boolean isVerbose() {
                        return true;
                    }

                    @Override
                    public void verbose(String string) {
                        System.out.println(string);
                    }

                    @Override
                    public void handleException(String string, Exception exception) {
                        System.out.println(string);
                        exception.printStackTrace();
                    }
                };
            } else {
                return new Logger() {
                };
            }
        }

        return logger;
    }


    public ClientActor build() {
        return new ClientActor(this.getServerInputChannel(), this.getServerOutputChannel(), this.getPauseDuration(),
                this.getErrorHandler(),
                this.getConnectInfo(), this.getLogger(), this.getCleanupDuration(), getClientInputActions());
    }

    public boolean isVerbose() {
        return verbose;
    }

    public ClientActorBuilder verbose() {
        this.verbose = true;
        return this;
    }

    public boolean isInfo() {
        return info;
    }

    public ClientActorBuilder logInfo() {
        this.info = true;
        return this;
    }

    public static ClientActorBuilder builder() {
        return new ClientActorBuilder();
    }
}
