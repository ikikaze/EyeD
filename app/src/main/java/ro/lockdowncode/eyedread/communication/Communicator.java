package ro.lockdowncode.eyedread.communication;

import java.io.IOException;
import java.net.ConnectException;
import java.nio.channels.ClosedSelectorException;

/**
 * Created by Adi Neag on 05.05.2018.
 */

public class Communicator {

    private final int serverPort;
    private final MessageListener messageListener;
    private final Thread serverThread;
    private ServerSocket serverSocket;

    public Communicator(final String serverAddress, final int port, final MessageListener listener) {
        serverPort = port;
        messageListener = listener;
        Runnable server = new Runnable() {
            @Override
            public void run() {
                try {
                    serverSocket = new ServerSocket(serverAddress, serverPort, listener);
                    serverSocket.startServer();
                } catch (IOException e) {
                } catch (ClosedSelectorException e) {
                    // do nothing (expected to get here)
                }
            }
        };
        serverThread = new Thread(server);
        serverThread.start();
    }

    public void finish() {
        if (serverSocket != null) {
            serverSocket.stopServer();
        }
        serverThread.interrupt();
    }

    public void sendMessage(final String message, final String destinationAddress) {
        Runnable client = new Runnable() {
            @Override
            public void run() {
                try {
                    new ClientSocket().startClient(message, destinationAddress, serverPort);
                } catch (ConnectException e) {
                    messageListener.hostUnavailable(destinationAddress);
                } catch (IOException | InterruptedException e) {
                }
            }
        };
        new Thread(client).start();
    }
}
