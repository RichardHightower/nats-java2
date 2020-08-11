package io.nats.java.internal;

import io.nats.java.ClientErrorHandler;
import io.nats.java.InputQueue;
import io.nats.java.InputQueueMessage;
import io.nats.java.Subscription;
import io.nats.java.internal.actions.OutputQueue;
import io.nats.java.internal.actions.PingPong;
import io.nats.java.internal.actions.ServerErrorException;
import io.nats.java.internal.actions.client.*;
import io.nats.java.internal.actions.server.ReceiveMessage;
import io.nats.java.internal.actions.server.ServerError;
import io.nats.java.internal.actions.server.ServerInformation;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class ClientActor {

    /** Input IO sits on the other side of this channel. */
    private final InputQueue<ServerMessage> serverInputChannel;

    /** Client Actions sent with methods on this interface. */
    private final TransferQueue<Action> clientInputActions = new LinkedTransferQueue<>(); //Implement subscribe, publish, unsubscribe, etc. with this.

    /** Output IO sits on the other side of this channel. */
    private final OutputQueue<Action> serverOutputChannel;

    /** How long to pause/poll when there are no Server Messages in the queue. */
    private final Duration pauseDuration;

    /** Error handler if you are interested in such. */
    private final ClientErrorHandler errorHandler;

    /** Connection Info to send to the server after we get INFO from the server. */
    private final Connect connectInfo;

    /** Map of sid to subscription. */
    private final Map<String, SubscriptionHandler> subscriptions = new HashMap<>();


    private final AtomicLong now = new AtomicLong(System.currentTimeMillis());

    private final Random random = new Random();

    /** Used to generate sid. */
    private AtomicLong sid = new AtomicLong(random.nextLong());

    /** Used to generate inbox. */
    private long replyInboxId = random.nextLong();
    /** Used to generate inbox stub. */
    private String baseReplyInboxId =  UUID.randomUUID().toString();

    /** Used to implement close/stop. */
    private AtomicBoolean doStop = new AtomicBoolean();


    /** Are we connected? */
    private boolean connected = false;

    /** Server Info. */
    private ServerInformation serverInformation;

    public ClientActor(final InputQueue<ServerMessage> queue,
                       final OutputQueue<Action> output,
                       final Duration pauseDuration,
                       final ClientErrorHandler errorHandler,
                       final Connect connectInfo) {
        this.serverInputChannel = queue;
        this.pauseDuration = pauseDuration;
        this.errorHandler = errorHandler;
        this.serverOutputChannel = output;
        this.connectInfo = connectInfo;
    }


    /** Generate next inbox for request/reply. */
    private String nextInbox() {
        return String.format("inbox%s-%s-%s", baseReplyInboxId, replyInboxId++);
    }


    /** Stop the client. */
    public void stop() {
        doStop.set(true);
    }

    public void run() {

        try {
            boolean pause = false;
            InputQueueMessage<io.nats.java.internal.ServerMessage> next;
            Exception lastError = null;
            Action clientAction;
            long startTime = System.currentTimeMillis();
            long lastTime = startTime;

            loop_exit:
            while (!doStop.get()) {
                for (int index = 0; index < 100; index++) {

                    clientAction = clientInputActions.poll();
                    while (clientAction != null) {
                        handleClientAction(clientAction);
                        clientAction = clientInputActions.poll();
                    }

                    next = !pause ? serverInputChannel.next() : serverInputChannel.next(pauseDuration);
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
                now.set( System.currentTimeMillis());
                if ( now.get() - lastTime > 30_000) {
                    lastTime = now.get();
                    cleanup();
                }
            }

            if (doStop.get()) {
                serverOutputChannel.send(Disconnect.DISCONNECT);
                this.connected = false;
            } else if (lastError != null) {
                serverOutputChannel.send(Disconnect.DISCONNECT);
                this.connected = false;
                //TODO maybe log this
            }
        } catch (Exception exception) {
            serverOutputChannel.send(Disconnect.DISCONNECT);
            this.connected = false;
            errorHandler.handleError(exception);
        }
    }

    private void cleanup() {
        //TODO cleanup old subscriptions
    }

    private void handleClientAction(Action clientAction) {
        if (connected) {
            switch (clientAction.verb()) {
                case SUBSCRIBE:
                    handleSubscribe((Subscribe) clientAction);
                case UNSUBSCRIBE:
                    handleUnsubscribe((Unsubscribe) clientAction);
                case PUBLISH:
                    handlePublish((Publish) clientAction);
            }
        } else {
            //TODO warn.. subscribe or unsubscribe called before connecting.
        }
    }

    private void handlePublish(final Publish clientActionPublish) {
            serverOutputChannel.send(clientActionPublish);
     }

    private void handleServerMessage(final io.nats.java.internal.ServerMessage message) {

        if (connected) {
            switch (message.verb()) {
                case INFO:
                    handleServerInfo(ServerInformation.parse(message.getBytes()));
                case OK:
                    handleOk();
                case PING:
                    handlePing();
                case MESSAGE:
                    handleMessage(ReceiveMessage.parse(message.getBytes()));
            }
        } else {
            switch (message.verb()) {
                case INFO:
                    handleServerConnectInfo(ServerInformation.parse(message.getBytes()));
                case OK:
                    handleOk();
                case PING:
                    handlePing();
                case ERROR:
                    handleError(ServerError.parse(message.getBytes()));
                default:
                    throw new IllegalStateException(String.format("CAN'T RECEIVE %s MESSAGE until connected", message.verb()));
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

        final SubscriptionHandler subscriptionHandler = subscriptions.get(message.getSid());

        if (subscriptionHandler != null) {
            subscriptionHandler.send(message);
        } else {
            //TODO log this or something.
        }

    }

    private void handleUnsubscribe(final Unsubscribe unsubscribe) {
        final SubscriptionHandler subscriptionHandler = subscriptions.get(unsubscribe.getSid());

        if (subscriptionHandler != null) {
            subscriptionHandler.unsubscribe(unsubscribe.getMaxMessages());
            serverOutputChannel.send(unsubscribe);
        } else {
            //TODO log this;
        }

    }

    private void handleSubscribe(final Subscribe subscribe) {
        subscriptions.put(subscribe.getSid(), subscribe.getSubscription());
        serverOutputChannel.send(subscribe);
    }

    private void handleServerInfo(ServerInformation newInfo) {
        if (this.serverInformation != null) {
            this.serverInformation = this.serverInformation.merge(newInfo);
        } else {
            this.serverInformation = newInfo;
        }
    }

    private void handlePing() {
        serverOutputChannel.send(PingPong.PONG);
    }

    private void handleOk() {

    }

    private void handleServerConnectInfo(final ServerInformation newServerInfo) {
        serverInformation = newServerInfo;
        serverOutputChannel.send(connectInfo);
    }

    /**
     * Send a message to the specified subject. The message body <strong>will
     * not</strong> be copied.
     *
     *
     * where the sender creates a byte array immediately before calling publish.
     *
     * See {@link #publish(String, String, byte[]) publish()} for more details on
     * publish during reconnect.
     *
     * @param subject the subject to send the message to
     * @param body the message body
     */
    public void publish(String subject, byte[] body) {
        this.publish(subject, null, body);
    }

    /**
     * Send a request. The returned future will be completed when the
     * response comes back.
     *
     * @param subject the subject for the service that will handle the request
     * @param data the content of the message
     * @return a Subscription for the response.
     */
    public Subscription request(String subject, byte[] data) {
        final String replyTo = this.nextInbox();
        final Subscription subscription = subscribe(subject);
        clientInputActions.add(new Unsubscribe(subscription.sid(), 1));
        this.publish(subject, replyTo, data);
        return subscription;
    }

    /**
     * Send a request to the specified subject, providing a replyTo subject. The
     * message body <strong>will not</strong> be copied.
     *
     * where the sender creates a byte array immediately before calling publish.
     * <p>
     * During reconnect the client will try to buffer messages. The buffer size is set
     * in the connect options, see  reconnectBufferSize()}
     * with a default value of DEFAULT_RECONNECT_BUF_SIZE 8 * 1024 * 1024 bytes.
     * If the buffer is exceeded an IllegalStateException is thrown. Applications should use
     * this exception as a signal to wait for reconnect before continuing.
     * </p>
     * @param subject the subject to send the message to
     * @param replyTo the subject the receiver should send the response to
     * @param body the message body
     */
    public void publish(String subject, String replyTo, byte[] body) {
        clientInputActions.add(new Publish(subject, replyTo, body));
    }

    /**
     * Create a synchronous subscription to the specified subject.
     *
     * @param subject the subject to subscribe to
     * @return an object representing the subscription
     */
    public Subscription subscribe(String subject) {
        return this.subscribe(subject, null);
    }

    /**
     * Create a synchronous subscription to the specified subject and queue.
     *
     * <p>Use the {@link Subscription#next(Duration) nextMessage} method to read
     * messages for this subscription.
     *
     *
     *
     * @param subject the subject to subscribe to
     * @param queueGroup the queue group to join
     * @return an object representing the subscription
     */
    public Subscription subscribe(final String subject, final String queueGroup) {
        final String sid = Long.toString(this.sid.incrementAndGet());
        final Subscribe subscribe = new Subscribe(subject, queueGroup, sid, this.now.get());
        this.clientInputActions.add(subscribe);
        return subscribe.getSubscription();
    }
}
