package ro.lockdowncode.eyedread.communication;

import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.sql.ClientInfoStatus;

/**
 * Created by Adi Neag on 05.05.2018.
 */

public class ClientSocket {

    private String destinationAddress;
    private int destinationPort;
    private SocketChannel client;

    public ClientSocket(String destinationAddress, int destinationPort) throws IOException {
        this.destinationAddress = destinationAddress;
        this.destinationPort = destinationPort;
        client = openChannel();
    }

    public String sendString(String strMessage) throws IOException, InterruptedException {
        byte [] message = new String(strMessage).getBytes();
        ByteBuffer buffer = ByteBuffer.wrap(message);
        client.write(buffer);
        Log.d(ClientSocket.class.getName(), "Sent message: "+strMessage);
        buffer.clear();
        return read();
    }

    public void sendStringWithoutResponse(String strMessage) throws IOException, InterruptedException {
        byte [] message = new String(strMessage).getBytes();
        ByteBuffer buffer = ByteBuffer.wrap(message);
        client.write(buffer);
        Log.d(ClientSocket.class.getName(), "Sent message: "+strMessage);
        buffer.clear();
    }

    public void sendByteArrayWithoutResponse(byte[] data) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        Log.d(ClientSocket.class.getName(), "Byte buffer data wrapped");
        client.write(buffer);
        Log.d(ClientSocket.class.getName(), "Sent byte array of length "+data.length);
        buffer.clear();
    }

    public String sendByteArray(byte[] data) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        Log.d(ClientSocket.class.getName(), "Byte buffer data wrapped");
        client.write(buffer);
        Log.d(ClientSocket.class.getName(), "Sent byte array of length "+data.length);
        buffer.clear();
        return read();
    }

    public void close() {
        if (client != null) {
            try {
                client.close();
            } catch (IOException e) {
                Log.d(ClientSocket.class.getName(), "Could not close client socket");
            }
        }
    }

    private SocketChannel openChannel() throws IOException {
        InetSocketAddress hostAddress = new InetSocketAddress(destinationAddress, destinationPort);
        SocketChannel client = SocketChannel.open();
        Socket socket = client.socket();
        socket.setSoTimeout(2000);
        socket.connect(hostAddress, 2000);
        return client;
    }

    //read from the socket channel
    private String read() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int numRead = -1;
        numRead = client.read(buffer);
        if (numRead == -1) {
            return "";
        }
        byte[] data = new byte[numRead];
        System.arraycopy(buffer.array(), 0, data, 0, numRead);
        String strData = new String(data);
        return strData;
    }
}
