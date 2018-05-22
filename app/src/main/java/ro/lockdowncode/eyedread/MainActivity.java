package ro.lockdowncode.eyedread;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


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

        if (isConnectionSaved()) {
            btnConnect.setText("Connected to " + getConnectionName());
        } else {
            btnConnect.setText("Connect to Desktop");
        }
    }

    private void initButtons() {
        btnConnect = findViewById(R.id.btnConnect);
        btnSearch = findViewById(R.id.btnSearch);
        btnID = findViewById(R.id.btnID);
        btnPass = findViewById(R.id.btnPass);
        btnLicense = findViewById(R.id.btnLicense);
    }

    private void handleConnectBtnClik() {
        if (isConnectionSaved()) {
            // custom dialog
            final Dialog connDialog = new Dialog(MainActivity.getInstance());
            connDialog.setContentView(R.layout.connection_details_dialog);
            connDialog.setTitle("Title...");

            TextView name = connDialog.findViewById(R.id.connectionName);
            name.setText(getConnectionName());
            TextView ip = connDialog.findViewById(R.id.connectionIP);
            ip.setText(getConnectionIP());
            TextView mac = connDialog.findViewById(R.id.connectionMAC);
            mac.setText(getConnectionMAC());

            Button newConnectionButton = connDialog.findViewById(R.id.newConnectionBtn);
            newConnectionButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                new AlertDialog.Builder(MainActivity.getInstance())
                    .setTitle("Title")
                    .setMessage("Do you really want to whatever?")
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
        } else {
                    Intent homepage = new Intent(MainActivity.this, PairingActivity.class);
                    startActivity(homepage);

        }
    }


    public void btnsClicked(View view) {
        int id = view.getId();

        Intent intent = null;
        switch (id) {
            case R.id.btnID:
                break;
            case R.id.btnLicense:
                intent = new Intent(this, LicenseActivity.class);
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

    public void resetConnectionPreferences() {
        SharedPreferences sharedPref = MainActivity.getInstance().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.commit();
    }

    public boolean isConnectionSaved() {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        String connName = sharedPref.getString(getString(R.string.connectionName), null);
        String connIP = sharedPref.getString(getString(R.string.connectionIP), null);
        String connMAC = sharedPref.getString(getString(R.string.connectionMAC), null);
        return (connName != null && !connName.isEmpty() && connIP != null && !connIP.isEmpty() && connMAC != null && !connMAC.isEmpty());
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
