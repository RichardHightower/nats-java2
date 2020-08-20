package io.nats.java.internal.io;

import io.nats.java.internal.ByteUtils;
import io.nats.java.internal.ServerMessage;
import io.nats.java.internal.actions.OutputQueue;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

public class NatsIOReader {

    private final byte[] buffer = new byte[1024 * 64];


    private final InputStream inputStream;
    private final Socket clientSocket;
    private final OutputQueue<ServerMessage> serverMessages;
    private int highWaterMark = 0;
    private int positionOfEndOfLine;
    private int startOfBuffer;


    public NatsIOReader(InputStream inputStream, Socket clientSocket, OutputQueue<ServerMessage> serverMessages) {
        this.inputStream = inputStream;
        this.clientSocket = clientSocket;
        this.serverMessages = serverMessages;
    }

    public void run() throws IOException {


        int[] bytesReadHolder = new int[1];
        int bytesRead;

        while (!clientSocket.isClosed()) {

            for (int i = 0; i < 10; i++) {

                bytesReadHolder[0] = -1;
                startOfBuffer = 0;
                positionOfEndOfLine = InputStreamUtils.readLine(inputStream, buffer, highWaterMark, bytesReadHolder);
                bytesRead = bytesReadHolder[0];

                if (bytesRead == -1) {
                    //Do something here.
                } else {
                    highWaterMark += bytesRead;
                }

                byte b = buffer[0];

                switch (b) {
                    case 'I':
                    case 'i':
                        processInfo();
                        break;
                    case 'M':
                    case 'm':
                        processMessage();
                        break;
                    case 'P':
                    case 'p':
                        processPingPong();
                        break;
                    case '+':
                        processOK();
                        break;
                    case '-':
                        processError();
                        break;

                    default:
                        throw new IOException("Illegal message char at start of message from server '" + (char) b + "'");
                }

            }


        }

    }

    private void processError() throws IOException {

    }

    private void processOK() throws IOException {

    }

    private void processPingPong() throws IOException {

    }

    private void processMessage() throws IOException {


        int[] indexes = new int[8];
        int[] locationIndex = new int[1];
        int[] numFeatures = new int[1];
        locationIndex[0] = startOfBuffer;
        int size = ByteUtils.readSizeFromMsgAndValidate(buffer, locationIndex, highWaterMark, indexes, numFeatures);
        int index = locationIndex[0];

        if (highWaterMark - index < size) {
            if (buffer.length - index < size) {

                final ByteBuffer byteBuffer = ByteBuffer.allocate((index - startOfBuffer) + 2 + size);

                byteBuffer.put(buffer, startOfBuffer, index - startOfBuffer);

                int bytesRead;

                do  {

                    bytesRead = inputStream.read(buffer);

                    if (bytesRead > 0) {
                        byteBuffer.put(buffer, 0, bytesRead);
                    }
                }while (bytesRead != -1 && bytesRead != 0);


                byte[] b = new byte[byteBuffer.remaining()];
                byteBuffer.get(b);



            } else {

            }
        }


    }

    private void processInfo() throws IOException {

    }
}
