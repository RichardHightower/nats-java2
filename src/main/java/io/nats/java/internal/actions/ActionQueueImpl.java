package io.nats.java.internal.actions;

import io.nats.java.internal.Action;

import java.util.concurrent.BlockingQueue;

public class ActionQueueImpl implements ActionQueue{
    private final BlockingQueue<Action> queue;

    public ActionQueueImpl(BlockingQueue<Action> queue) {
        this.queue = queue;
    }

    @Override
    public Action poll() {
        return queue.poll();
    }

    @Override
    public void send(Action item) {
        try {
            queue.put(item);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }
}
