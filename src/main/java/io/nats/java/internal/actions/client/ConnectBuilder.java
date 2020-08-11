package io.nats.java.internal.actions.client;

public class ConnectBuilder {

    private  boolean verbose; // verbose: Turns on +OK protocol acknowledgements.
    private  boolean pedantic; //      pedantic: Turns on additional strict format checking, e.g. for properly formed subjects
    private  boolean tlsRequired; // tls_required: Indicates whether the client requires an SSL connection.
    private  String authToken; // auth_token: Client authorization token (if auth_required is set)
    private  String user; // user : Connection username (if auth_required is set)
    private  char [] password; //pass : Connection password (if auth_required is set)
    private  String name; // name : Optional client name
    private  String lang; // lang : The implementation language of the client.
    private  String version; // version: The version of the client.
    private  int protocol=1; //        protocol: optional int. Sending 0 (or absent) indicates client supports original protocol. Sending 1 indicates that the client supports dynamic reconfiguration of cluster topology changes by asynchronously receiving INFO messages with known servers it can reconnect to.
    private  boolean echo; //        echo: Optional boolean. If set to true, the server (version 1.2.0+) will not send originating messages from this connection to its own subscriptions. Clients should set this to true only for server supporting this feature, which is when proto in the INFO protocol is set to at least 1.
    private  String sig; // sig: In case the server has responded with a nonce on INFO, then a NATS client must use this field to reply with the signed nonce.
    private  String jwt; //        jwt: The JWT that identifies a user permissions and acccount.

    public boolean isVerbose() {
        return verbose;
    }

    public ConnectBuilder setVerbose(boolean verbose) {
        this.verbose = verbose;
        return this;
    }

    public boolean isPedantic() {
        return pedantic;
    }

    public ConnectBuilder setPedantic(boolean pedantic) {
        this.pedantic = pedantic;
        return this;
    }

    public boolean isTlsRequired() {
        return tlsRequired;
    }

    public ConnectBuilder setTlsRequired(boolean tlsRequired) {
        this.tlsRequired = tlsRequired;
        return this;
    }

    public String getAuthToken() {
        return authToken;
    }

    public ConnectBuilder setAuthToken(String authToken) {
        this.authToken = authToken;
        return this;
    }

    public String getUser() {
        return user;
    }

    public ConnectBuilder setUser(String user) {
        this.user = user;
        return this;
    }

    public char[] getPassword() {
        return password;
    }

    public ConnectBuilder setPassword(char[] password) {
        this.password = password;
        return this;
    }

    public String getName() {
        return name;
    }

    public ConnectBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public String getLang() {
        return lang;
    }

    public ConnectBuilder setLang(String lang) {
        this.lang = lang;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public ConnectBuilder setVersion(String version) {
        this.version = version;
        return this;
    }

    public int getProtocol() {
        return protocol;
    }

    public ConnectBuilder setProtocol(int protocol) {
        this.protocol = protocol;
        return this;
    }

    public boolean isEcho() {
        return echo;
    }

    public ConnectBuilder setEcho(boolean echo) {
        this.echo = echo;
        return this;
    }

    public String getSig() {
        return sig;
    }

    public ConnectBuilder setSig(String sig) {
        this.sig = sig;
        return this;
    }

    public String getJwt() {
        return jwt;
    }

    public ConnectBuilder setJwt(String jwt) {
        this.jwt = jwt;
        return this;
    }

    public static ConnectBuilder builder() {
        return new ConnectBuilder();
    }

    public Connect build() {
        return new Connect(this.isVerbose(), this.isPedantic(), this.tlsRequired, this.getAuthToken(), this.getUser(),
                this.getPassword(), this.getName(), this.getLang(), this.getVersion(), this.getProtocol(),
                this.isEcho(), this.getSig(), this.getJwt());
    }
}
