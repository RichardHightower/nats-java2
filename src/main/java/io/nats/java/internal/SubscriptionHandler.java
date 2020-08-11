package io.nats.java.internal;

import io.nats.java.InputQueueMessage;
import io.nats.java.Message;
import io.nats.java.Subscription;
import io.nats.java.internal.actions.OutputQueue;

import java.time.Duration;

import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TransferQueue;

public class SubscriptionHandler implements Subscription, OutputQueue<Message> {

    private TransferQueue<InputQueueMessage<Message>> messageQueue = new LinkedTransferQueue<>();

    public static class NoMessage implements InputQueueMessage<Message> {

        @Override
        public boolean isError() {
            return false;
        }

        @Override
        public boolean isPresent() {
            return false;
        }

        @Override
        public boolean isDone() {
            return false;
        }

        @Override
        public Exception error() {
            return null;
        }

        @Override
        public Message value() {
            return null;
        }
    }

    private static NoMessage  NO_MESSAGE = new NoMessage();

    public static class  NextQueueMessage implements InputQueueMessage<Message> {
        private final Message message;

        public NextQueueMessage(Message message) {
            this.message = message;
        }

        @Override
        public boolean isError() {
            return false;
        }

        @Override
        public boolean isPresent() {
            return true;
        }

        @Override
        public boolean isDone() {
            return false;
        }

        @Override
        public Exception error() {
            return null;
        }

        @Override
        public Message value() {
            return message;
        }
    }

    @Override
    public InputQueueMessage<Message> next() {

        final InputQueueMessage<Message> inputQueueMessage = messageQueue.poll();
        if (inputQueueMessage == null) {
            return NO_MESSAGE;
        } else {
            return inputQueueMessage;
        }
    }

    @Override
    public InputQueueMessage<Message> next(final Duration duration) {
        final InputQueueMessage<Message> inputQueueMessage;
        try {
            inputQueueMessage = messageQueue.poll(duration.toMillis(), TimeUnit.MILLISECONDS );
            if (inputQueueMessage == null) {
                return NO_MESSAGE;
            } else {
                return inputQueueMessage;
            }
        } catch (InterruptedException e) {
            //e.printStackTrace();
            return NO_MESSAGE;
        }
    }


    @Override
    public void send(Message item) {
        messageQueue.add(new NextQueueMessage(item));
    }

    @Override
    public boolean isClosed() {
        return false;
    }
}
