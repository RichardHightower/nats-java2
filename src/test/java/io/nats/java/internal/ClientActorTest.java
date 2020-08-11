package io.nats.java.internal;

import io.nats.java.InputQueue;
import io.nats.java.InputQueueMessage;
import io.nats.java.internal.actions.OutputQueue;
import io.nats.java.internal.actions.client.Connect;
import io.nats.java.internal.actions.client.ConnectBuilder;
import io.nats.java.internal.actions.client.Disconnect;
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
    public void testConnect() {
        final ClientActor clientActor = builder.build();

        Thread thread = createRunner(clientActor);

        sendConnectInfo();

        stopRunner(clientActor, thread);


        //assertEquals("Zk0GQ3JBSrg3oyxCRRlE09", clientActor.getServerInformation().getServerId());

        System.out.println(serverOutputChannel);
        assertEquals(2, serverOutputChannel.size());

        Action action = serverOutputChannel.poll();

        assertTrue(action instanceof Connect);

        action = serverOutputChannel.poll();

        assertTrue(action instanceof Disconnect);


        assertNull(exceptionAtomicReference.get());


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

    private void sendConnectInfo() {
        final String info = "INFO {\"server_id\":\"Zk0GQ3JBSrg3oyxCRRlE09\",\"version\":\"1.2.0\",\"proto\":1,\"go\":\"go1.10.3\",\"host\":\"0.0.0.0\",\"port\":4222,\"max_payload\":1048576,\"client_id\":2392}";

        serverInputChannel.add(new InputQueueMessage<ServerMessage>() {
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
            public ServerMessage value() {
                return new ServerMessage(info.getBytes(StandardCharsets.UTF_8), NATSProtocolVerb.INFO);
            }
        });
    }

    private InputQueueMessage<ServerMessage> getServerMessageInputQueueMessage(InputQueueMessage<ServerMessage> next) {
        if (next == null) {
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
        } else {
            return next;
        }
    }
}