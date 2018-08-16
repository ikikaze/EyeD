package ro.lockdowncode.eyedread.communication;

import android.os.Build;

import java.io.IOException;
import java.net.ConnectException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedSelectorException;

/**
 * Created by Adi Neag on 05.05.2018.
 */

public class DesktopCommunicator {

    private final int serverPort;
    private final MessageListener messageListener;
    private final Thread serverThread;
    private ServerSocket serverSocket;

    public DesktopCommunicator(final String serverAddress, final int port, final MessageListener listener) {
        serverPort = port;
        messageListener = listener;
        Runnable server = new Runnable() {
            @Override
            public void run() {
                try {
                    serverSocket = new ServerSocket(serverAddress, serverPort, listener);
                    serverSocket.startServer();
                } catch (IOException | ClosedSelectorException e) {
                    e.printStackTrace(System.out);
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

    //public void ping()

    public void sendMessage(final String message, final String destinationAddress) {
        Runnable client = new Runnable() {
            @Override
            public void run() {
                ClientSocket clientSocket = null;
                try {
                    clientSocket = new ClientSocket(destinationAddress, 33778);
                    clientSocket.sendString(message);
                } catch (IOException | InterruptedException e) {
                    messageListener.hostUnavailable(destinationAddress);
                } finally {
                    if (clientSocket != null) {
                        clientSocket.close();
                    }
                }
            }
        };
        new Thread(client).start();
    }

    public void sendPhoto(final byte[] data, final String destinationAddress, final int type) {
        Runnable client = new Runnable() {
            @Override
            public void run() {
                ClientSocket clientSocket = null;
                try {
                    clientSocket = new ClientSocket(destinationAddress, 33778);
                    String resp = clientSocket.sendString("0008:"+ Build.SERIAL+":PrepareReceivePicture:"+type);
                    if (resp.equalsIgnoreCase("0009:ReadyToReceivePicture")) {
                        long length = data.length;
                        clientSocket.sendStringWithoutResponse("0009:"+length);
                        clientSocket.sendByteArrayWithoutResponse(data);
                    } else if (resp.equalsIgnoreCase("0004:Busy")) {
                        messageListener.desktopBusy(destinationAddress);
                    }
                } catch (IOException | InterruptedException e) {
                    messageListener.hostUnavailable(destinationAddress);
                } finally {
                    if (clientSocket != null) {
                        clientSocket.close();
                    }
                }
            }
        };
        new Thread(client).start();
    }
}
