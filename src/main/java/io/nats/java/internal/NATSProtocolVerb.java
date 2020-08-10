package io.nats.java.internal;

public enum NATSProtocolVerb {

    //Server only
    INFO ("INFO", false, true),
    OK ("OK", false, true),
    ERROR("ERR", false, true),
    MESSAGE ("MSG", false, true),

    //Both
    PING("PING", false, false),
    PONG ("PONG", false, false),

    //Client only
    CONNECT ("CONNECT", true, false),
    DISCONNECT ("DISCONNECT", true, false), //NOT SENT TO SERVER, just terminate conneciton.
    PUBLISH ("PUB", true, false),
    SUBSCRIBE ("SUB", true, false),
    UNSUBSCRIBE ("UNSUB", true, false);




    private final String text;
    private final char[] protocolChars;
    private final boolean clientOnly;
    private final boolean serverOnly;

    NATSProtocolVerb(final String text, boolean clientOnly, boolean serverOnly) {
        this.text = text;
        this.protocolChars = text.toCharArray();
        this.clientOnly = clientOnly;
        this.serverOnly = serverOnly;
    }
}
