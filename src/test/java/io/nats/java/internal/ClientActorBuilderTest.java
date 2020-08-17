package io.nats.java.internal;


import org.junit.Test;

public class ClientActorBuilderTest {

    @Test
    public void test() {

        ClientActorBuilder builder = ClientActorBuilder.builder();
        builder.withLogger(builder.getLogger());
        builder.withConnectInfo(builder.getConnectInfo());
        builder.withServerOutputChannel(builder.getServerOutputChannel());
        builder.withServerInputChannel(builder.getServerInputChannel());
        builder.withErrorHandler(builder.getErrorHandler());
        builder.withCleanupDuration(builder.getCleanupDuration());
        builder.withPauseDuration(builder.getPauseDuration());

        builder.build();
        testLog(builder.getLogger());

        builder.withLogger(null);
        builder.verbose();
        builder.build();
        testLog(builder.getLogger());

        builder = ClientActorBuilder.builder();
        builder.logInfo();
        builder.build();
        testLog(builder.getLogger());

        builder = ClientActorBuilder.builder();
        builder.logInfo();  builder.verbose();
        builder.build();
        testLog(builder.getLogger());

        builder.isInfo();
        builder.isVerbose();







    }

    private void testLog(Logger logger) {
        logger.isInfo();
        logger.isVerbose();
        logger.verbose("verbose");
        logger.info("info");
        logger.handleException("exception", new Exception());
    }
}