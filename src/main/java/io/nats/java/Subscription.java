package io.nats.java;

public interface Subscription extends InputQueue<Message>{
    boolean isDone();
    String subject();
    String queueGroup();
    String sid();
}
