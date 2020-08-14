package io.nats.java.internal;

public interface Logger {

    default boolean isInfo() {
        return false;
    }

    default boolean isVerbose() {
        return false;
    }

    default void info(String string) {
    }

    default void handleException(String string, Exception exception) {
    }

    default void verbose(String string) {
    }
}
