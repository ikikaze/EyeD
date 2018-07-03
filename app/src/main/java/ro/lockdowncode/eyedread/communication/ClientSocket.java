package ro.lockdowncode.eyedread.communication;

import android.os.Build;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by Adi Neag on 05.05.2018.
 */

public class ClientSocket {

    public static enum DESKTOP_RESPONSE {PHOTO_RECEIVED_SUCCESS, PHOTO_RECEIVED_FAILURE, DESKTOP_BUSY, UNKNOWN_ERROR};

    public void sendString(String strMessage, String destination, int port) throws IOException, InterruptedException {
        InetSocketAddress hostAddress = new InetSocketAddress(destination, port);
        SocketChannel client = SocketChannel.open();
        Socket socket = client.socket();
        socket.setSoTimeout(2000);
        socket.connect(hostAddress, 2000);
        byte [] message = new String(strMessage).getBytes();
        ByteBuffer buffer = ByteBuffer.wrap(message);
        client.write(buffer);
        buffer.clear();
        client.close();
    }

    public DESKTOP_RESPONSE sendPhoto(byte[] data, String destination, int port) throws IOException, InterruptedException {
        InetSocketAddress hostAddress = new InetSocketAddress(destination, port);
        SocketChannel client = SocketChannel.open();
        Socket socket = client.socket();
        socket.setSoTimeout(2000);
        socket.connect(hostAddress, 2000);
        byte [] message = new String("0008:"+ Build.SERIAL+":PrepareReceivePicture:1").getBytes();
        client.write(ByteBuffer.wrap(message));

        String resp = read(client);
        System.out.println("Message sent ............................................("+resp);
        if (resp.equalsIgnoreCase("0009:ReadyToReceivePicture")) {
            System.out.println("---------------------- Ready to receive");
            long length = data.length;
            client.write(ByteBuffer.wrap(new String("0009:"+length).getBytes()));

            ByteBuffer buffer = ByteBuffer.wrap(data);
            client.write(buffer);
            //buffer.clear();

            //BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String result=read(client);
            //while ((result = in.readLine()) != null) {
            //}

            System.out.println("Transfer result ------------------------------"+result);
            if (result.equalsIgnoreCase("0009:TransferComplete")) {
                return DESKTOP_RESPONSE.PHOTO_RECEIVED_SUCCESS;
            } else if (result.equalsIgnoreCase("0009:TransferIncomplete")){
                return DESKTOP_RESPONSE.PHOTO_RECEIVED_FAILURE;
            }
        } else if (resp.equalsIgnoreCase("0004:Busy")) {
            return DESKTOP_RESPONSE.DESKTOP_BUSY;
        }
        client.close();
        return DESKTOP_RESPONSE.UNKNOWN_ERROR;
    }

    //read from the socket channel
    private String read(SocketChannel channel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int numRead = -1;
        numRead = channel.read(buffer);

        if (numRead == -1) {
            return "";
        }

        byte[] data = new byte[numRead];
        System.arraycopy(buffer.array(), 0, data, 0, numRead);
        String strData = new String(data);
        return strData;
    }
}
