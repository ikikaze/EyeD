package ro.lockdowncode.eyedread;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import java.util.Date;

import io.realm.Realm;
import io.realm.RealmResults;
import ro.lockdowncode.eyedread.communication.CommunicationService;
import ro.lockdowncode.eyedread.pairing.PairingActivity;

public class MainActivity extends AppCompatActivity {

    private static MainActivity instance;

    public static MainActivity getInstance() {
        return instance;
    }

    private Button btnSearch;
    private Button btnID;
    private Button btnPass;
    private Button btnLicense;
    private Button btnConnect;

    private Dialog connDialog;
    private AlertDialog wifiAlertDialog;
    private AlertDialog desktopBusy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        instance = this;

        initButtons();

        Intent intent = new Intent(MainActivity.this, CommunicationService.class);
        startService(intent);

        resetConnectionButtonText();
    }

    private void initButtons() {
        btnConnect = findViewById(R.id.btnConnect);
        btnSearch = findViewById(R.id.btnSearch);
        btnID = findViewById(R.id.btnID);
        btnPass = findViewById(R.id.btnPass);
        btnLicense = findViewById(R.id.btnLicense);
    }

    private void handleConnectBtnClik() {
        if (getConnStatus() == CONNECTION_STATUS.CONNECTED) {
            handleConnectionStatusClick_Connected(false);
        } else if (getConnStatus() == CONNECTION_STATUS.WAITING) {
            handleConnectionStatusClick_Connected(true);
        } else {
            Intent homepage = new Intent(MainActivity.this, PairingActivity.class);
            startActivity(homepage);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        resetConnectionButtonText();
        if (connDialog != null) {
            connDialog.dismiss();
        }
        if (wifiAlertDialog != null) {
            wifiAlertDialog.dismiss();
        }
        if (desktopBusy != null) {
            desktopBusy.dismiss();
        }
        if (getConnStatus() == CONNECTION_STATUS.CONNECTED && CommunicationService.uiMessageReceiverHandler != null) {
            // ping desktop
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("destination", getActiveDesktopConnection().getIp());
            data.putString("message", "0012:" + Build.SERIAL + ":Ping");
            msg.setData(data);
            CommunicationService.uiMessageReceiverHandler.sendMessage(msg);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        /*runOnUiThread(new Runnable() {

            @Override
            public void run() {

                if (wifiAlertDialog != null) {
                    wifiAlertDialog.dismiss();
                }

            }
        });*/

    }

    public void resetConnectionButtonText() {
        if (getConnStatus() == CONNECTION_STATUS.CONNECTED) {
            btnConnect.setText("Connected to "+getActiveDesktopConnection().getName());
        } else if (getConnStatus() == CONNECTION_STATUS.WAITING) {
            btnConnect.setText("Waiting for connection to "+getActiveDesktopConnection().getName());
        } else {
            btnConnect.setText("Connect to desktop");
        }
    }

    private void handleConnectionStatusClick_Connected(boolean waiting) {
        // custom dialog
        connDialog = new Dialog(MainActivity.getInstance());
        connDialog.setContentView(R.layout.connection_details_dialog);
        connDialog.setTitle("Title...");

        String status = waiting ? "Connection waiting for ": "Connected to ";
        TextView name = connDialog.findViewById(R.id.connectionName);
        name.setText(status + getActiveDesktopConnection().getName());
        TextView ip = connDialog.findViewById(R.id.connectionIP);
        ip.setText(getActiveDesktopConnection().getIp());
        //TextView mac = connDialog.findViewById(R.id.connectionMAC);
        //mac.setText(getConnectionMAC());

        Button newConnectionButton = connDialog.findViewById(R.id.newConnectionBtn);
        newConnectionButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new AlertDialog.Builder(MainActivity.getInstance())
                        .setTitle("Title")
                        .setMessage("Do you really want to make a new connection ?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                resetConnectionPreferences();
                                Intent homepage = new Intent(MainActivity.this, PairingActivity.class);
                                startActivity(homepage);
                            }})
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });
        Button backButton = connDialog.findViewById(R.id.backBtn);
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                connDialog.dismiss();
            }
        });

        connDialog.show();
    }


    public void btnsClicked(View view) {
        int id = view.getId();

        Intent intent = null;
        switch (id) {
            case R.id.btnID:
                intent = new Intent(this, LicenseActivity.class);
                intent.putExtra("type", Utils.Type.BULETIN.name());
                break;
            case R.id.btnLicense:
                intent = new Intent(this, LicenseActivity.class);
                intent.putExtra("type", Utils.Type.PERMIS.name());
                break;
            case R.id.btnPass:
                intent = new Intent(this,PassportOCRActivity.class);
                break;
            case R.id.btnSearch:
                break;
            case R.id.btnConnect:
                handleConnectBtnClik();
                break;
        }
        if (intent != null)
            startActivity(intent);
    }

    public void pairingSuccessful(String desktopID) {
        saveNewConnection(getActiveDesktopConnection().getName(), getActiveDesktopConnection().getIp(), desktopID, 2);
        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                resetConnectionButtonText();

            }
        });

    }

    public void updateDsktopIP(String desktopIP) {
        saveNewConnection(getActiveDesktopConnection().getName(), desktopIP, getActiveDesktopConnection().getId(), 2);
        setConnectionVisibility(true);
    }

    public void setConnectionVisibility(final boolean connectionVisibility) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (getConnStatus() == CONNECTION_STATUS.CONNECTED) {
                    String vis = (connectionVisibility) ? "ON":"OFF";
                    String text = btnConnect.getText().toString();
                    String[] msgChunks = text.split(":");
                    String stat = msgChunks[0];
                    String finalText;
                    if (!stat.equals("ON") && !stat.equals("OFF")) {
                        finalText = vis + ":"+stat;
                    } else {
                        finalText = vis + ":"+msgChunks[1];
                    }
                    btnConnect.setText(finalText);
                }
            }});

    }

    public void showStatus(String response) {

        new AlertDialog.Builder(MainActivity.getInstance())
                .setTitle("Photo Transfer Status")
                .setMessage(response)
                .setIcon(android.R.drawable.ic_dialog_alert).show();
    }


    public void notifyWifiOff() {
        if(!Utils.checkWifiOnAndConnected(this)) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (wifiAlertDialog == null) {
                        wifiAlertDialog = new AlertDialog.Builder(MainActivity.getInstance())
                                .setTitle("Wifi is off")
                                .setMessage("Please turn on wifi to be able to communicate with desktop app")
                                .setIcon(android.R.drawable.ic_dialog_alert).create();
                    }
                    if (!wifiAlertDialog.isShowing()) {
                        wifiAlertDialog.show();
                    }
                }
            });
        }
    }

    public void desktopBusy() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (desktopBusy == null) {
                    desktopBusy = new AlertDialog.Builder(MainActivity.getInstance())
                            .setTitle("Desktop is busy")
                            .setMessage("Please try again later")
                            .setIcon(android.R.drawable.ic_dialog_alert).create();
                }
                if (!desktopBusy.isShowing()) {
                    desktopBusy.show();
                }
            }
        });
    }

    public enum CONNECTION_STATUS { UNCONNECTED, WAITING, CONNECTED};

    public CONNECTION_STATUS getConnStatus() {
        DesktopConnection activeDC = getActiveDesktopConnection();
        if (activeDC == null) {
            return CONNECTION_STATUS.UNCONNECTED;
        }
        if (activeDC.getStatus() == 1) {
            return CONNECTION_STATUS.WAITING;
        }
        return CONNECTION_STATUS.CONNECTED;
    }

    public void resetConnectionPreferences() {
        Realm realm = Realm.getDefaultInstance();
        try {
            RealmResults<DesktopConnection> r = realm.where(DesktopConnection.class)
                    .greaterThan("status", 0)
                    .findAll();
            realm.beginTransaction();
            for (DesktopConnection dc: r) {
                dc.setStatus(0);
            }
            realm.commitTransaction();
        } finally {
            realm.close();
        }
    }

    public void saveNewConnection(final String name, final String ip, final String id, final int status) {
        Realm realm = Realm.getDefaultInstance();
        try {
            final RealmResults<DesktopConnection> r = realm.where(DesktopConnection.class)
                    .equalTo("id", id).or().isNull("id")
                    .findAll();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    if (!r.isEmpty()) {
                        DesktopConnection existingDesktopConnection = r.get(0);
                        existingDesktopConnection.setIp(ip);
                        existingDesktopConnection.setName(name);
                        existingDesktopConnection.setStatus(status);
                        existingDesktopConnection.setLastConnected(new Date());
                    } else {
                        DesktopConnection newDesktopConnection = realm.createObject(DesktopConnection.class, id);
                        newDesktopConnection.setName(name);
                        newDesktopConnection.setIp(ip);
                        newDesktopConnection.setStatus(status);
                        newDesktopConnection.setLastConnected(new Date());
                    }
                }
            });
        } finally {
            realm.close();
        }
    }

    public DesktopConnection getActiveDesktopConnection() {
        Realm realm = Realm.getDefaultInstance();
        try {
            RealmResults<DesktopConnection> r = realm.where(DesktopConnection.class)
                    .greaterThan("status", 0)
                    .findAll();
            if (r.isEmpty()) {return null;}
            return realm.copyFromRealm(r.get(0));
        } finally {
            realm.close();
        }
    }

}
