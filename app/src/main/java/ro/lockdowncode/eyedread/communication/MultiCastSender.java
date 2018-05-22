package ro.lockdowncode.eyedread.communication;


import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import ro.lockdowncode.eyedread.MainActivity;

/**
 * Created by Adi Neag on 03.05.2018.
 */

public class MultiCastSender extends Thread {

    private final MainActivity activity;
    private final String broadcastIp;
    private final int broadcastPort;
    private final String message;

    public MultiCastSender(MainActivity activity, String ip, int port, String message) {
        this.activity = activity;
        this.broadcastIp = ip;
        this.broadcastPort = port;
        this.message = message;
    }

    @Override
    public void run() {
        try {
            DatagramSocket socket = new DatagramSocket();
            socket.setBroadcast(true);

            byte[] buffer = message.getBytes();

            DatagramPacket packet
                    = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(broadcastIp), broadcastPort);
            socket.send(packet);
            socket.close();

        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }
}
