package ro.lockdowncode.eyedread;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.DialogOnAnyDeniedMultiplePermissionsListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import ro.lockdowncode.eyedread.UI.MenuButton;
import ro.lockdowncode.eyedread.communication.CommunicationService;
import ro.lockdowncode.eyedread.pairing.PairingActivity;

public class MainActivity extends AppCompatActivity {

    private static int RESULT_LOAD_IMG = 1;

    private static MainActivity instance;

    public static MainActivity getInstance() {
        return instance;
    }

    private MenuButton btnConnect;

    private Dialog connDialog;
    private Dialog docTypeSelectionDialog;
    private Dialog passDialog;
    private AlertDialog wifiAlertDialog;
    private AlertDialog desktopBusy;
    private AlertDialog activityCanceledDialog;

    private Utils.Document searchDocType;

    private boolean wifiOn = false;
    private boolean isDesktopAvailable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Have no idea why I put this here but at this point I'm too afraid to remove it
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        instance = this;
        btnConnect = findViewById(R.id.btnConnect);

        handlePermissions();

        Intent intent = new Intent(MainActivity.this, CommunicationService.class);
        startService(intent);

        resetConnectionButtonText();
    }

    private void handlePermissions() {

        MultiplePermissionsListener dialogMultiplePermissionsListener =
                DialogOnAnyDeniedMultiplePermissionsListener.Builder
                        .withContext(this)
                        .withTitle("Permisiuni necesare")
                        .withMessage("Aplicatia necesita acces la urmatoarele capabilitati ale telefonului:\n" +
                                "-Camera : pentru a face poze la documente si a recunoaste informatiile din pasaport\n" +
                                "-Stocare interna: pentru a putea salva pozele facute cu aplicatia in memoria telefonului\n" +
                                "-Internet si WiFi : pentru a se putea realiza conexiunea cu aplicatia de desktop EyeDRead\n" +
                                "-NFC : Pentru a putea scana cip-ul pasaportului\n" +
                                "Acceptati aceste permisiuni pentru functionarea corespunzatoare a aplicatiei\n" +
                                "Refuzul acceptarii permisiunilor poate produce comportament necorespunzator al aplicatiei.")
                        .withButtonText(android.R.string.ok)
                        .build();

        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.NFC,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CHANGE_WIFI_MULTICAST_STATE,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.ACCESS_WIFI_STATE,
                        Manifest.permission.RECEIVE_BOOT_COMPLETED,
                        Manifest.permission.WAKE_LOCK,
                        Manifest.permission.INTERNET
                ).withListener(dialogMultiplePermissionsListener).check();
    }

    private void handleConnectBtnClik() {
        wifiStatusChange();
        if (wifiOn) {
            if (getConnStatus() == CONNECTION_STATUS.CONNECTED) {
                handleConnectionStatusClick_Connected(false);
            } else if (getConnStatus() == CONNECTION_STATUS.WAITING) {
                handleConnectionStatusClick_Connected(true);
            } else {
                Intent homepage = new Intent(MainActivity.this, PairingActivity.class);
                startActivity(homepage);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (wifiAlertDialog != null) {
            wifiAlertDialog.dismiss();
        }
        if (passDialog != null) {
            passDialog.dismiss();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        resetConnectionButtonText();
        if (connDialog != null) {
            connDialog.dismiss();
        }
        if (docTypeSelectionDialog != null) {
            docTypeSelectionDialog.dismiss();
        }
        if (wifiAlertDialog != null) {
            wifiAlertDialog.dismiss();
        }

        if (passDialog != null) {
            passDialog.dismiss();
        }
        if (desktopBusy != null) {
            desktopBusy.dismiss();
        }

        if (getConnStatus() == CONNECTION_STATUS.CONNECTED && CommunicationService.uiMessageReceiverHandler != null) {
            // ping desktop
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("destination", getActiveDesktopConnection().getIp());
            data.putString("message", "0012:" + Build.SERIAL + ":Ping:" + getActiveDesktopConnection().getId());
            msg.setData(data);
            CommunicationService.uiMessageReceiverHandler.sendMessage(msg);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            // When an Image is picked
            if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK
                    && null != data) {
                // Get the Image from data

                Uri selectedImage = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };

                // Get the cursor
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                // Move to first row
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String imgDecodableString = cursor.getString(columnIndex);
                cursor.close();

                Intent intent = new Intent(this, SendDocument.class);
                intent.putExtra("imgString", imgDecodableString);
                intent.putExtra("type", searchDocType.name());
                intent.putExtra("source", "gallery");
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);

            } else {
                Toast.makeText(this, "You haven't picked Image",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }

    }

    public void resetConnectionButtonText() {
        if (getConnStatus() == CONNECTION_STATUS.CONNECTED) {
            btnConnect.setText("Conecteaza-te la "+getActiveDesktopConnection().getName());
        } else if (getConnStatus() == CONNECTION_STATUS.WAITING) {
            btnConnect.setText("Se asteapta conexiune de la "+getActiveDesktopConnection().getName());
        } else {
            btnConnect.setText("Conecteaza-te");
        }
        setConnectionVisibility(false);

    }

    private void handleConnectionStatusClick_Connected(boolean waiting) {
        // custom dialog
        connDialog = new Dialog(MainActivity.getInstance());
        connDialog.setContentView(R.layout.connection_details_dialog);
        connDialog.setTitle("Title...");

        String status = waiting ? "Se asteapta conexiune de la "+ getActiveDesktopConnection().getName(): (isDesktopAvailable) ?
                "Desktopul "+ getActiveDesktopConnection().getName()+" este online":
                "Desktopul "+ getActiveDesktopConnection().getName()+" este offline";
        TextView name = connDialog.findViewById(R.id.connectionName);
        name.setText(status);
        TextView ip = connDialog.findViewById(R.id.connectionIP);
        ip.setText(getActiveDesktopConnection().getIp());

        ImageView refreshBtn = connDialog.findViewById(R.id.refreshBtn);
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getConnStatus() == CONNECTION_STATUS.CONNECTED && CommunicationService.uiMessageReceiverHandler != null) {
                    // ping desktop
                    Message msg = new Message();
                    Bundle data = new Bundle();
                    data.putString("destination", getActiveDesktopConnection().getIp());
                    data.putString("message", "0012:" + Build.SERIAL + ":Ping:" + getActiveDesktopConnection().getId());
                    msg.setData(data);
                    CommunicationService.uiMessageReceiverHandler.sendMessage(msg);
                    connDialog.dismiss();
                }
            }
        });
        if (isDesktopAvailable) {
            refreshBtn.setVisibility(View.GONE);
        }

        Button newConnectionButton = connDialog.findViewById(R.id.newConnectionBtn);
        newConnectionButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new AlertDialog.Builder(MainActivity.getInstance())
                        .setMessage("Doriti sa va conectati cu alt calculator ?")
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                resetConnectionPreferences();
                                Intent homepage = new Intent(MainActivity.this, PairingActivity.class);
                                startActivity(homepage);
                            }})
                        .setNegativeButton(R.string.no, null).show();
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

    public void searchDocSelected(View view) {
        int id = view.getId();
        if (id == R.id.btnDocCancel)
            docTypeSelectionDialog.dismiss();
        else
        {
            switch (id)
            {
                case R.id.btnID:
                    searchDocType = Utils.Document.BULETIN;
                    break;
                case R.id.btnLicense:
                    searchDocType = Utils.Document.PERMIS;
                    break;
                case R.id.btnPass:
                    searchDocType = Utils.Document.PASAPORT;
                    break;
            }

            Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
        }
    }

    public void btnsClicked(View view) {
        int id = view.getId();

        Intent intent = null;
        switch (id) {
            case R.id.btnID:
                intent = new Intent(this, LicenseActivity.class);
                intent.putExtra("type", Utils.Document.BULETIN.name());
                break;
            case R.id.btnLicense:
                intent = new Intent(this, LicenseActivity.class);
                intent.putExtra("type", Utils.Document.PERMIS.name());
                break;
            case R.id.btnPass:
                //handlePassportSelect();
                intent = new Intent(this, LicenseActivity.class);
                intent.putExtra("type", Utils.Document.PASAPORT.name());
                break;
            case R.id.btnSearch:
                docTypeSelectionPopup();
                break;
            case R.id.btnConnect:
                handleConnectBtnClik();
                break;
        }
        if (intent != null)
            startActivity(intent);
    }

    private void handlePassportSelect() {

        passDialog = new Dialog(this);
        passDialog.setContentView(R.layout.passport_action_dialog);
        passDialog.setTitle("Titlu..");


        Button btnPic = passDialog.findViewById(R.id.btnPic);
        Button btnScan = passDialog.findViewById(R.id.btnScan);
        Button btnBack = passDialog.findViewById(R.id.btnBack);

        btnPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.instance,LicenseActivity.class);
                intent.putExtra("type", Utils.Document.PASAPORT.name());
                startActivity(intent);
            }
        });

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                NfcManager manager = (NfcManager) getSystemService(NFC_SERVICE);
                NfcAdapter adapter = manager.getDefaultAdapter();

                if(adapter != null && adapter.isEnabled())
                {
                    Intent intent;
                    intent = new Intent(MainActivity.instance, PassportOCRActivity.class);
                    startActivity(intent);
                }
                else if(adapter!=null && !adapter.isEnabled())
                {
                    //go to activate NFC TODO
                    AlertDialog.Builder alertbox = new AlertDialog.Builder(v.getContext());
                    alertbox.setTitle("Info");
                    alertbox.setMessage("Apasati butonul Continuati pentru a activa NFC-ul din meniul de setari.\n" +
                            "Dupa ce este activat, apasati pe butonul de revenire pentru a continua.");
                    alertbox.setPositiveButton("Continuati", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);
                                startActivity(intent);
                            } else {
                                Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                                startActivity(intent);
                            }
                        }
                    });
                    alertbox.setNegativeButton("Reveniti", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    alertbox.show();
                }
                else if(adapter == null)
                {
                    // phone has no NFC, buy better hardware poor man
                    // exit and tell user

                }
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passDialog.dismiss();
            }
        });

        btnScan.setWidth(btnPic.getWidth());
        passDialog.show();

    }

    private void docTypeSelectionPopup() {
        // custom dialog
        docTypeSelectionDialog = new Dialog(MainActivity.getInstance());
        docTypeSelectionDialog.setContentView(R.layout.doc_type_selection_dialog);
        docTypeSelectionDialog.show();
    }

    public void pairingSuccessful(String desktopID) {
        saveNewConnection(getActiveDesktopConnection().getName(), getActiveDesktopConnection().getIp(), desktopID, 2);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                resetConnectionButtonText();
                setConnectionVisibility(true);
                new AlertDialog.Builder(MainActivity.getInstance())
                        .setTitle("Conectat la "+getActiveDesktopConnection().getName())
                        .setMessage("Puteti transmite imagini direct din aceasta aplicatie fara a mai fi nevoie sa accesati meniul Telefon Mobil al aplicatiei de pe calculator. Aplicatia desktop le va procesa automat")
                        .setPositiveButton("OK", null)
                        .setIcon(android.R.drawable.ic_dialog_info).show();
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
                MenuButton btn = findViewById(R.id.btnConnect);
                Drawable background = getApplicationContext().getResources().getDrawable(R.drawable.button_bg);

                if (getConnStatus() == CONNECTION_STATUS.CONNECTED) {
                    if (connectionVisibility) {
                        background = getApplicationContext().getResources().getDrawable(R.drawable.button_bg_on);
                        btnConnect.setText("Conectat la "+getActiveDesktopConnection().getName());
                     }
                }
                btn.setBackground(background);
                isDesktopAvailable = connectionVisibility && getConnStatus()==CONNECTION_STATUS.CONNECTED;
            }
        });
    }

    public void wifiStatusChange() {
        if(!Utils.checkWifiOnAndConnected(this)) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (wifiAlertDialog == null) {
                        wifiAlertDialog = new AlertDialog.Builder(MainActivity.getInstance())
                                .setTitle("Wifi este oprit")
                                .setMessage("Conecteaza-te la wifi pentru a putea comunica cu calculatorul")
                                .setPositiveButton("OK", null)
                                .setIcon(android.R.drawable.ic_dialog_alert).create();
                    }
                    if (!wifiAlertDialog.isShowing()) {
                        if (!isFinishing()) {
                            wifiAlertDialog.show();
                        }
                    }
                    resetConnectionButtonText();
                }
            });
            wifiOn = false;
        } else {
            if (!wifiOn) {
                if (getConnStatus() == CONNECTION_STATUS.CONNECTED && CommunicationService.uiMessageReceiverHandler != null) {
                    // ping desktop
                    Message msg = new Message();
                    Bundle data = new Bundle();
                    data.putString("destination", getActiveDesktopConnection().getIp());
                    data.putString("message", "0012:" + Build.SERIAL + ":Ping:" + getActiveDesktopConnection().getId());
                    msg.setData(data);
                    CommunicationService.uiMessageReceiverHandler.sendMessage(msg);
                }
            }
            wifiOn = true;
        }
    }

    public void desktopBusy() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (desktopBusy == null) {
                    desktopBusy = new AlertDialog.Builder(MainActivity.getInstance())
                            .setTitle("Calculatorul este ocupat")
                            .setMessage("Te rugam incearca mai tarziu")
                            .setPositiveButton("OK", null)
                            .setIcon(android.R.drawable.ic_dialog_alert).create();
                }
                if (!desktopBusy.isShowing()) {
                    if (!isFinishing()) {
                        desktopBusy.show();
                    }
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
            final RealmResults<DesktopConnection> matchingConnections = realm.where(DesktopConnection.class)
                    .equalTo("id", id).or().isNull("id")
                    .findAll();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    matchingConnections.deleteAllFromRealm();
                    DesktopConnection newConnection = realm.createObject(DesktopConnection.class, id);
                    newConnection.setName(name);
                    newConnection.setIp(ip);
                    newConnection.setStatus(status);
                    newConnection.setLastConnected(new Date());
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

    public void cancelCurrentServerProcess() {
        if (MainActivity.getInstance().getActiveDesktopConnection() != null) {
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("destination", MainActivity.getInstance().getActiveDesktopConnection().getIp());
            data.putString("message", "0022:CancelCurrentProcess");
            msg.setData(data);
            CommunicationService.uiMessageReceiverHandler.sendMessage(msg);
        }
    }

    public void cancelActivity() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (activityCanceledDialog == null) {
                    activityCanceledDialog = new AlertDialog.Builder(MainActivity.getInstance())
                            .setMessage("Activitatea a fost oprita !")
                            .setPositiveButton("OK", null).create();
                }
                if (!activityCanceledDialog.isShowing()) {
                    if (!isFinishing()) {
                        activityCanceledDialog.show();
                    }
                }
            }
        });
    }

}
