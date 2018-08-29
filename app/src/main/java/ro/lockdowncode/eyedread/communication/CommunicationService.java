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

import okhttp3.internal.Util;
import ro.lockdowncode.eyedread.EditDocInfo;
import ro.lockdowncode.eyedread.LicenseActivity;
import ro.lockdowncode.eyedread.MainActivity;
import ro.lockdowncode.eyedread.SendDocument;
import ro.lockdowncode.eyedread.TemplatesList;
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
            MainActivity.getInstance().wifiStatusChange();
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
                    } else {
                        if (SendDocument.getInstance() != null) {
                            SendDocument.getInstance().wifiOff();
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
                String desktopIPFormMsg = msgChunks[2];
                PairingActivity.getInstance().addDesktopClient(desktopName, desktopIPFormMsg, desktopMAC);
                break;
            case "0003":
                desktopMAC = msgChunks[1];
                MainActivity.getInstance().pairingSuccessful(desktopMAC);
                // send mac to desktop
                getDesktopCommunicator().sendMessage("0005:"+ Build.SERIAL+":"+android.os.Build.MODEL, desktopIP);
                break;
            case "0004": // DESKTOP ERROR
                if (msgChunks[1].equalsIgnoreCase("busy")) {
                    desktopBusy(desktopIP);
                }
                break;
            case "0007":
                MainActivity.getInstance().updateDsktopIP(desktopIP);
                break;
            case "0009":
                if (msgChunks[1].equals("TransferComplete")) {
                    SendDocument.getInstance().updateStatus("Se proceseaza poza", false);
                } else if (msgChunks[1].equals("TransferIncomplete")){
                    SendDocument.getInstance().updateStatus(msgChunks[1], true);
                } else {
                    SendDocument.getInstance().updateStatus(msgChunks[1], false);
                }
                break;
            case "0011": // START CAMERA FROM DESKTOP 0011:desktopId:StartCamera:tipAct(1,2,3)
                String desktopID = msgChunks[1];
                if (MainActivity.getInstance().getActiveDesktopConnection()!=null && MainActivity.getInstance().getActiveDesktopConnection().getId().equals(desktopID)) {
                    Utils.Document docType = Utils.Document.getById(msgChunks[3]);
                    if (docType != null) {
                        Intent intentHome = new Intent(getApplicationContext(), LicenseActivity.class);
                        intentHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intentHome.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intentHome.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        intentHome.putExtra("type", docType.name());
                        startActivity(intentHome);
                    }
                }
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

            case "0013":
                if (msgChunks[1].equalsIgnoreCase("invalid")) {
                    SendDocument.getInstance().updateStatus(msgChunks[2], true);
                }
                break;
            case "0014":
                if (msgChunks[1].equalsIgnoreCase("valid")) {
                    String jsonContent = message.substring(msgChunks[0].length()+msgChunks[1].length()+2);
                    SendDocument.getInstance().validDataFromDesktop(jsonContent);
                    System.out.println(jsonContent);
                }
                break;
            case "0016":
                if (msgChunks[1].equalsIgnoreCase("TemplatesList")) {
                    String jsonContent = message.substring(msgChunks[0].length()+msgChunks[1].length()+2);
                    TemplatesList.getInstance().validTemplatesReceived(jsonContent);
                }
                break;
            case "0017":
                if (msgChunks[1].equalsIgnoreCase("TemplatesListInvalid")) {
                    System.out.println(msgChunks[2]);
                }
                break;
            case "0020":
                if (msgChunks[1].equalsIgnoreCase("DataOK")) {
                    TemplatesList.getInstance().readyToSendTemplates();
                }
                break;
            case "0021":
                TemplatesList.getInstance().requestStatus(msgChunks[1]);
                break;
            case "0022":
                if (msgChunks[1].equalsIgnoreCase("CancelCurrentProcess")) {
                    if (SendDocument.getInstance() != null) {
                        SendDocument.getInstance().processStoppedByDesktop();
                    }
                    if (EditDocInfo.getInstance() != null && !EditDocInfo.getInstance().isFinishing()) {
                        EditDocInfo.getInstance().finish();
                        MainActivity.getInstance().cancelActivity();
                    }
                    if (TemplatesList.getInstance() != null  && !TemplatesList.getInstance().isFinishing()) {
                        TemplatesList.getInstance().finish();
                        MainActivity.getInstance().cancelActivity();
                    }
                }
                break;
        }
    }

    @Override
    public void hostUnavailable(String hostIP) {
        Log.d(CommunicationService.class.getName(), "Host unavailable");
        if (MainActivity.getInstance().getConnStatus() == MainActivity.CONNECTION_STATUS.CONNECTED && Utils.checkWifiOnAndConnected(MainActivity.getInstance().getApplicationContext())) {
            MainActivity.getInstance().setConnectionVisibility(false);
            String multicastMessage = "0006:09fe5d9775f04a4b8b9b081a8e732bae:"+Build.SERIAL;
            new MultiCastSender(MainActivity.getInstance(), "255.255.255.255", 33558, multicastMessage).start();

            if (SendDocument.getInstance() != null) {
                SendDocument.getInstance().hostUnavailable();
            }
        }
    }

    @Override
    public void desktopBusy(String destinationAddress) {
        MainActivity.getInstance().desktopBusy();
        if (SendDocument.getInstance()!=null) {
            SendDocument.getInstance().updateStatus("Pe calculator exista deja un document de identitate in lucru. Inchideti activitatea curenta pentru a putea prelua noi imagini de la Telefonul mobil.", true);
        }
    }

}