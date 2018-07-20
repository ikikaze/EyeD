package ro.lockdowncode.eyedread.communication;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import ro.lockdowncode.eyedread.LicenseActivity;
import ro.lockdowncode.eyedread.MainActivity;
import ro.lockdowncode.eyedread.pairing.PairingActivity;
import ro.lockdowncode.eyedread.Utils;

import static ro.lockdowncode.eyedread.Utils.getIpAddress;


/**
 * Created by Adi Neag on 04.05.2018.
 */

public class CommunicationService extends Service implements MessageListener {


    private static final int COMMUNICATION_PORT = 33779;

    public static Handler uiMessageReceiverHandler = null;

    private DesktopCommunicator desktopCommunicator;

    public DesktopCommunicator getDesktopCommunicator() {
        if (Utils.checkWifiOnAndConnected(getApplicationContext())) {
            if (desktopCommunicator == null) {
                desktopCommunicator = new DesktopCommunicator(getIpAddress(), COMMUNICATION_PORT, this);
            }
        } else {
            if (desktopCommunicator != null) {
                desktopCommunicator.finish();
            }
            desktopCommunicator = null;
        }
        if (MainActivity.getInstance() != null) {
            MainActivity.getInstance().notifyWifiOff();
        }
        return desktopCommunicator;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        getDesktopCommunicator();
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
                    if (getDesktopCommunicator() != null) {
                        Bundle data = msg.getData();
                        String dest = data.getString("destination");
                        String action = data.getString("action");

                        if (action!=null && action.equalsIgnoreCase("imageTransfer")) {
                            byte[] imageData = data.getByteArray("photoData");
                            int docType = data.getInt("docType" );
                            getDesktopCommunicator().sendPhoto(imageData, dest, docType);
                        } else {
                            getDesktopCommunicator().sendMessage(data.getString("message"), data.getString("destination"));
                        }
                    }
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
                getDesktopCommunicator().sendMessage("0005:"+ Build.SERIAL+":"+android.os.Build.MODEL, desktopIP);
                break;
            case "0004": // DESKTOP BUSY
                desktopBusy(desktopIP);
                break;
            case "0007":
                MainActivity.getInstance().updateDsktopIP(desktopIP);
                break;
            case "0009":
                System.out.println(msgChunks[1]);
                break;
            case "0011": // START CAMERA FROM DESKTOP 0011:desktopId:StartCamera:tipAct(1,2,3)
                Intent intentHome = new Intent(getApplicationContext(), LicenseActivity.class);
                intentHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intentHome.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intentHome.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intentHome.putExtra("type", Utils.Document.BULETIN.name());
                startActivity(intentHome);
                break;

            case "0012":
                desktopMAC = msgChunks[1];
                String cmd = msgChunks[2];
                if (cmd.equalsIgnoreCase("available") && desktopMAC.equals(MainActivity.getInstance().getActiveDesktopConnection().getId())) {
                    MainActivity.getInstance().setConnectionVisibility(true);
                } else if (cmd.equalsIgnoreCase("ping") && desktopMAC.equals(MainActivity.getInstance().getActiveDesktopConnection().getId())) {
                    getDesktopCommunicator().sendMessage("0012:" + Build.SERIAL + ":Ping", desktopIP);
                }
                break;
        }
    }

    @Override
    public void hostUnavailable(String hostIP) {
        Log.d(CommunicationService.class.getName(), "Host unavailable");
        if (MainActivity.getInstance().getConnStatus() == MainActivity.CONNECTION_STATUS.CONNECTED) {
            MainActivity.getInstance().setConnectionVisibility(false);
            String multicastMessage = "0006:09fe5d9775f04a4b8b9b081a8e732bae:"+Build.SERIAL;
            new MultiCastSender(MainActivity.getInstance(), "255.255.255.255", 33558, multicastMessage).start();
        }
    }

    @Override
    public void desktopBusy(String destinationAddress) {
        MainActivity.getInstance().desktopBusy();
    }

}