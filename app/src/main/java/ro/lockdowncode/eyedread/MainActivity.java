package ro.lockdowncode.eyedread;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import ro.lockdowncode.eyedread.communication.ClientSocket;
import ro.lockdowncode.eyedread.communication.CommunicationService;

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
        if (getConnStatus() == CONNECTION_STATUS.CONNECTED && CommunicationService.uiMessageReceiverHandler != null) {
            // ping desktop
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("destination", getConnectionIP());
            data.putString("message", "0012:" + Build.SERIAL + ":Ping");
            msg.setData(data);
            CommunicationService.uiMessageReceiverHandler.sendMessage(msg);
        }
    }

    public void resetConnectionButtonText() {
        if (getConnStatus() == CONNECTION_STATUS.CONNECTED) {
            btnConnect.setText("Connected to "+getConnectionName());
        } else if (getConnStatus() == CONNECTION_STATUS.WAITING) {
            btnConnect.setText("Waiting for connection to "+getConnectionName());
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
        name.setText(status + getConnectionName());
        TextView ip = connDialog.findViewById(R.id.connectionIP);
        ip.setText(getConnectionIP());
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

    public void pairingSuccessful(String desktopMAC) {
        saveNewConnection(getConnectionName(), getConnectionIP(), desktopMAC);
        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                resetConnectionButtonText();

            }
        });

    }

    public void updateDsktopIP(String desktopIP) {
        saveNewConnection(getConnectionName(), desktopIP, getConnectionMAC());
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

    public void showStatus(ClientSocket.DESKTOP_RESPONSE response) {

        new AlertDialog.Builder(MainActivity.getInstance())
                .setTitle("Photo Transfer Status")
                .setMessage(response.toString())
                .setIcon(android.R.drawable.ic_dialog_alert).show();
    }

    public enum CONNECTION_STATUS { UNCONNECTED, WAITING, CONNECTED};

    public CONNECTION_STATUS getConnStatus() {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        String connName = sharedPref.getString(getString(R.string.connectionName), null);
        String connIP = sharedPref.getString(getString(R.string.connectionIP), null);
        String connMAC = sharedPref.getString(getString(R.string.connectionMAC), null);

        if (connName != null && !connName.isEmpty() && connIP != null && !connIP.isEmpty()) {
            if (connMAC != null && !connMAC.isEmpty()) {
                return CONNECTION_STATUS.CONNECTED;
            }
            return CONNECTION_STATUS.WAITING;
        }
        return CONNECTION_STATUS.UNCONNECTED;
    }

    public void resetConnectionPreferences() {
        SharedPreferences sharedPref = MainActivity.getInstance().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.commit();
    }

    public void saveNewConnection(String name, String ip, String mac) {
        SharedPreferences sharedPref = MainActivity.getInstance().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.connectionName), name);
        editor.putString(getString(R.string.connectionIP), ip);
        editor.putString(getString(R.string.connectionMAC), mac);
        editor.commit();
    }

    public String getConnectionName() {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getString(getString(R.string.connectionName), null);
    }

    public String getConnectionIP() {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getString(getString(R.string.connectionIP), null);
    }

    public String getConnectionMAC() {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getString(getString(R.string.connectionMAC), null);
    }
}
