package io.nats.java.internal;

import io.nats.java.InputQueueMessage;
import io.nats.java.Message;
import io.nats.java.Subscription;
import io.nats.java.internal.actions.OutputQueue;
import io.nats.java.internal.actions.client.Subscribe;

import java.time.Duration;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TransferQueue;

public class SubscriptionHandler implements Subscription, OutputQueue<Message> {

    private boolean closed = false;
    private int count = Integer.MIN_VALUE;
    private final Subscribe subscribe;
    private final long created;

    private TransferQueue<InputQueueMessage<Message>> messageQueue = new LinkedTransferQueue<>();

    public SubscriptionHandler(Subscribe subscribe, long created) {
        this.subscribe = subscribe;
        this.created = created;
    }


    public static class UnsubscribeInputQueueMessage implements InputQueueMessage<Message> {

        private final int count;

        public UnsubscribeInputQueueMessage(int count) {
            this.count = count;
        }

    }

    public static class NoMessage implements InputQueueMessage<Message> {

    }

    public static class DoneMessage implements InputQueueMessage<Message> {

        @Override
        public boolean isDone() {
            return true;
        }

    }

    private static NoMessage NO_MESSAGE = new NoMessage();
    private static DoneMessage DONE_MESSAGE = new DoneMessage();

    public static class NextQueueMessage implements InputQueueMessage<Message> {
        private final Message message;

        public NextQueueMessage(Message message) {
            this.message = message;
        }

        @Override
        public boolean isPresent() {
            return true;
        }

        @Override
        public Message value() {
            return message;
        }
    }

    @Override
    public InputQueueMessage<Message> next() {
        preConditions();
        final InputQueueMessage<Message> inputQueueMessage = messageQueue.poll();

        return handleMessagePoll(inputQueueMessage);
    }

    private void preConditions() {
        //if (closed) throw new IllegalStateException("Subscription is done");
    }

    private InputQueueMessage<Message> handleMessagePoll(InputQueueMessage<Message> inputQueueMessage) {
        if (inputQueueMessage == null) {
            return NO_MESSAGE;
        } else if (inputQueueMessage.isDone()) {
            this.closed = true;
            return inputQueueMessage;
        } else if (inputQueueMessage instanceof UnsubscribeInputQueueMessage) {
            this.count = ((UnsubscribeInputQueueMessage) inputQueueMessage).count;
            if (this.count ==0) {
                this.closed = true;
                messageQueue.add(DONE_MESSAGE);
            }
            return next();
        } else if (inputQueueMessage instanceof DoneMessage) {
            return inputQueueMessage;
        } else {
            if (count != Integer.MIN_VALUE) {
                count--;
                if (count <= 0) {
                    this.closed = true;
                    messageQueue.add(DONE_MESSAGE);
                }
            }
            return inputQueueMessage;
        }

    }

    @Override
    public InputQueueMessage<Message> next(final Duration duration) {
        preConditions();

        final InputQueueMessage<Message> inputQueueMessage;
        try {
            inputQueueMessage = messageQueue.poll(duration.toMillis(), TimeUnit.MILLISECONDS);
            return handleMessagePoll(inputQueueMessage);
        } catch (InterruptedException e) {
            //e.printStackTrace();
            return NO_MESSAGE;
        }
    }


    @Override
    public void send(Message item) {
        messageQueue.add(new NextQueueMessage(item));
    }

    public void unsubscribe(final int count) {
        messageQueue.add(new UnsubscribeInputQueueMessage(count));
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public boolean isDone() {
        return isClosed();
    }

    @Override
    public String subject() {
        return subscribe.getSubject();
    }

    @Override
    public String queueGroup() {
        return subscribe.getQueueGroup();
    }

    @Override
    public String sid() {
        return subscribe.getSid();
    }

    public long getCreated() {
        return created;
    }

    public int getCount() {
        return count;
    }
}
