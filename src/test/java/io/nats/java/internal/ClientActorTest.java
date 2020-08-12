package io.nats.java.internal;

import io.nats.java.InputQueue;
import io.nats.java.InputQueueMessage;
import io.nats.java.Message;
import io.nats.java.Subscription;
import io.nats.java.internal.actions.OutputQueue;
import io.nats.java.internal.actions.client.Connect;
import io.nats.java.internal.actions.client.ConnectBuilder;
import io.nats.java.internal.actions.client.Disconnect;
import io.nats.java.internal.actions.client.Subscribe;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TransferQueue;
import java.util.concurrent.atomic.AtomicReference;

import static junit.framework.TestCase.*;

public class ClientActorTest {
    final TransferQueue<InputQueueMessage<ServerMessage>> serverInputChannel = new LinkedTransferQueue<>();
    final TransferQueue<Action> serverOutputChannel = new LinkedTransferQueue<>();
    final ClientActorBuilder builder = ClientActorBuilder.builder();
    final AtomicReference<Exception> exceptionAtomicReference = new AtomicReference<>();


    @After
    public void after() {
        serverInputChannel.clear();
        serverOutputChannel.clear();
        exceptionAtomicReference.set(null);
    }

    @Before
    public void before() {


        builder.withConnectInfo(ConnectBuilder.builder().build());
        builder.withErrorHandler(exception -> {
            exception.printStackTrace();
            exceptionAtomicReference.set(exception);
        });


        builder.withServerInputChannel(new InputQueue<ServerMessage>() {
            @Override
            public InputQueueMessage<ServerMessage> next() {
                InputQueueMessage<ServerMessage> next = serverInputChannel.poll();
                return getServerMessageInputQueueMessage(next);
            }

            @Override
            public InputQueueMessage<ServerMessage> next(Duration duration) {
                try {
                    InputQueueMessage<ServerMessage> next = serverInputChannel.poll(duration.toMillis(), TimeUnit.MILLISECONDS);
                    return getServerMessageInputQueueMessage(next);
                } catch (InterruptedException e) {
                    return new InputQueueMessage<ServerMessage>() {
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
                        public ServerMessage value() {
                            return null;
                        }
                    };

                }
            }
        });


        builder.withServerOutputChannel(new OutputQueue<Action>() {
            @Override
            public void send(Action item) {
                try {
                    serverOutputChannel.put(item);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public boolean isClosed() {
                return false;
            }
        });

    }

    @Test
    public void testConnect() throws Exception {
        final ClientActor clientActor = builder.build();

        Thread thread = createRunner(clientActor);


        sendConnectInfo();
        Thread.sleep(100);

        stopRunner(clientActor, thread);
        Thread.sleep(100);


        //assertEquals("Zk0GQ3JBSrg3oyxCRRlE09", clientActor.getServerInformation().getServerId());


        Action action = serverOutputChannel.poll(10, TimeUnit.SECONDS);

        assertTrue(action instanceof Connect);

        action = serverOutputChannel.poll();

        assertTrue(action instanceof Disconnect);


        assertNull(exceptionAtomicReference.get());


    }


    @Test
    public void testMultipleInfoAfterConnect() throws Exception {
        final ClientActor clientActor = builder.build();

        Thread thread = createRunner(clientActor);


        sendConnectInfo();
        Thread.sleep(100);
        sendConnectInfo("server1");
        Thread.sleep(100);
        sendConnectInfo("server2");
        Thread.sleep(100);
        stopRunner(clientActor, thread);

        //assertEquals("Zk0GQ3JBSrg3oyxCRRlE09", clientActor.getServerInformation().getServerId());


        System.out.println(serverOutputChannel);
        Action action = serverOutputChannel.poll(10, TimeUnit.SECONDS);

        assertTrue(action instanceof Connect);

        action = serverOutputChannel.poll();

        assertTrue(action instanceof Disconnect);

        action = serverOutputChannel.poll();

        assertNull(action);

        assertNull(exceptionAtomicReference.get());


        stopRunner(clientActor, thread);
        Thread.sleep(100);





    }

    @Test
    public void testSubscribe() throws Exception {

        final String subject = "subject1";

        final ClientActor clientActor = builder.build();

        Thread thread = createRunner(clientActor);

        sendConnectInfo();


        Action action = serverOutputChannel.poll(10, TimeUnit.SECONDS);

        assertTrue(action instanceof Connect);


        assertNull(exceptionAtomicReference.get());

        final Subscription subscription = clientActor.subscribe(subject);
        final String sid = subscription.sid();


        action = serverOutputChannel.poll(10, TimeUnit.SECONDS);

        while (action != null && !(action instanceof Subscribe)) {
            action = serverOutputChannel.poll(1, TimeUnit.SECONDS);
        }

        assertNotNull(action);
        assertTrue(action instanceof Subscribe);

        sendMessage("Hello Mom", subject, sid);

        final InputQueueMessage<Message> next = subscription.next(Duration.ofSeconds(10));
        assertTrue(next.isPresent());


        assertEquals("Hello Mom", new String(next.value().getPayload(), StandardCharsets.UTF_8));

        stopRunner(clientActor, thread);
    }

    @Test
    public void testSubscribe2() throws Exception {

        final String subject = "subject1";

        final ClientActor clientActor = builder.build();

        Thread thread = createRunner(clientActor);

        sendConnectInfo();


        Action action = serverOutputChannel.poll(10, TimeUnit.SECONDS);

        assertTrue(action instanceof Connect);


        assertNull(exceptionAtomicReference.get());

        final Subscription subscription = clientActor.subscribe(subject);
        final String sid = subscription.sid();


        action = serverOutputChannel.poll(10, TimeUnit.SECONDS);

        while (action != null && !(action instanceof Subscribe)) {
            action = serverOutputChannel.poll(1, TimeUnit.SECONDS);
        }

        assertNotNull(action);
        assertTrue(action instanceof Subscribe);

        sendMessage("Hello Mom", subject, sid);

        final InputQueueMessage<Message> next = subscription.next(Duration.ofSeconds(10));
        assertTrue(next.isPresent());


        assertEquals("Hello Mom", new String(next.value().getPayload(), StandardCharsets.UTF_8));

        stopRunner(clientActor, thread);
    }

    private void stopRunner(ClientActor clientActor, Thread thread) {
        clientActor.stop();

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Thread createRunner(ClientActor clientActor) {
        Thread thread = new Thread(() -> clientActor.run());
        thread.start();
        return thread;
    }


    private void sendMessage(final String payLoad, final String subject, final String sid) {
        final String message = String.format("MSG %s %s  %d\r\n%s\r\n", subject, sid, payLoad.length(), payLoad);

        serverInputChannel.add(new InputQueueMessage<ServerMessage>() {

            @Override
            public boolean isPresent() {
                return true;
            }


            @Override
            public ServerMessage value() {
                return new ServerMessage(message.getBytes(StandardCharsets.UTF_8), NATSProtocolVerb.MESSAGE);
            }
        });
    }

    private void sendConnectInfo() {

        sendConnectInfo("Zk0GQ3JBSrg3oyxCRRlE09");
    }

    private void sendConnectInfo(String server) {
        final String info = String.format("INFO {\"server_id\":\"%s\",\"version\":\"1.2.0\",\"proto\":1,\"go\":\"go1." +
                "10.3\",\"host\":\"0.0.0.0\",\"port\":4222,\"max_payload\":1048576,\"client_id\":2392}\r\n", server);

        serverInputChannel.add(new InputQueueMessage<ServerMessage>() {

            @Override
            public boolean isPresent() {
                return true;
            }


            @Override
            public ServerMessage value() {
                return new ServerMessage(info.getBytes(StandardCharsets.UTF_8), NATSProtocolVerb.INFO);
            }
        });
    }

    private InputQueueMessage<ServerMessage> getServerMessageInputQueueMessage(InputQueueMessage<ServerMessage> next) {
        if (next == null) {
            return new InputQueueMessage<ServerMessage>() {
            };
        } else {
            return next;
        }
    }
}