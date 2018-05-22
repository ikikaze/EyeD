package ro.lockdowncode.eyedread.communication;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by Adi Neag on 05.05.2018.
 */

public class ClientSocket {

    public void startClient(String strMessage, String destination, int port) throws IOException, InterruptedException {
        InetSocketAddress hostAddress = new InetSocketAddress(destination, port);
        SocketChannel client = SocketChannel.open(hostAddress);
        byte [] message = new String(strMessage).getBytes();
        ByteBuffer buffer = ByteBuffer.wrap(message);
        client.write(buffer);
        buffer.clear();
        client.close();
    }

}
