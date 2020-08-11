package io.nats.java.internal.actions;

import io.nats.java.internal.actions.server.ServerError;

public class ServerErrorException extends RuntimeException{
    private final ServerError serverError;

    public ServerErrorException(ServerError serverError) {
        super(serverError.getMessage());
        this.serverError = serverError;
    }



}
