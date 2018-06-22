package ro.lockdowncode.eyedread.communication;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;

import ro.lockdowncode.eyedread.MainActivity;
import ro.lockdowncode.eyedread.PairingActivity;
import ro.lockdowncode.eyedread.Utils;

import static ro.lockdowncode.eyedread.Utils.getIpAddress;


/**
 * Created by Adi Neag on 04.05.2018.
 */

public class CommunicationService extends Service implements MessageListener {


    private static final int COMMUNICATION_PORT = 33778;

    public static Handler uiMessageReceiverHandler = null;

    private Communicator communicator;

    private Communicator getCommunicator() {
        if (Utils.checkWifiOnAndConnected(getApplicationContext())) {
            if (communicator == null) {
                communicator = new Communicator(getIpAddress(), COMMUNICATION_PORT, this);
            }
        } else {
            if (communicator != null) {
                communicator.finish();
            }
            communicator = null;
        }
        if (MainActivity.getInstance() != null) {
            //MainActivity.getInstance().notifyWifiOff();
        }
        return communicator;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        getCommunicator();
        new UIMessageReceiver().start();
        // We need to return if we want to handle this service explicitly.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Intent intent = new Intent("service.communication.stopped");
        intent.putExtra("key", "value"); //dummy
        sendBroadcast(intent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent intent = new Intent("service.communication.stopped");
        intent.putExtra("key", "value"); //dummy
        sendBroadcast(intent);
    }

    class UIMessageReceiver extends Thread
    {

        public void run()
        {
            this.setName("UIMessageReceiver");

            // Prepare the looper before creating the handler.
            Looper.prepare();
            uiMessageReceiverHandler = new Handler()
            {
                //here we will receive messages from activity(using sendMessage() from activity)
                public void handleMessage(Message msg)
                {
                    Bundle data = msg.getData();

                    getCommunicator().sendMessage(data.getString("message"), data.getString("destination"));

                }
            };
            Looper.loop();
        }
    }

    @Override // Handle messages received on socket from other devices
    public void messageReceived(String message, String desktopIP) {
        System.out.println(".............................................. Message received: " + message);
        String[] msgChunks = message.split(":");
        String messageCode = msgChunks[0];
        switch (messageCode) {
            case "0001":
                String desktopName = msgChunks[1];
                String desktopMAC = "";
                PairingActivity.getInstance().addDesktopClient(desktopName, desktopIP, desktopMAC);
                break;
            case "0003":
                desktopMAC = msgChunks[1];
                MainActivity.getInstance().pairingSuccessful(desktopMAC);
                // send mac to desktop
                WifiManager manager = (WifiManager) MainActivity.getInstance().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo info = manager.getConnectionInfo();
                String address = info.getMacAddress().replaceAll(":","");

                getCommunicator().sendMessage("0005:"+address+":"+android.os.Build.MODEL, desktopIP);
                break;
            case "0007":
                MainActivity.getInstance().updateDsktopIP(desktopIP);
                break;

        }
    }

    @Override
    public void hostUnavailable(String hostIP) {
        if (MainActivity.getInstance().getConnStatus() == MainActivity.CONNECTION_STATUS.CONNECTED) {
            String multicastMessage = "0006:09fe5d9775f04a4b8b9b081a8e732bae:"+MainActivity.getInstance().getConnectionMAC();
            new MultiCastSender(MainActivity.getInstance(), "255.255.255.255", 33558, multicastMessage).start();
        }
    }

}