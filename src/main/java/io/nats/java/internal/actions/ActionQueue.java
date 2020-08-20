package io.nats.java.internal.actions;

import io.nats.java.internal.Action;

public interface ActionQueue {
    Action poll();
    void send(Action item);
}
