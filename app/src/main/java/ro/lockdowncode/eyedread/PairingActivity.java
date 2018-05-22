package ro.lockdowncode.eyedread;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import ro.lockdowncode.eyedread.communication.CommunicationService;
import ro.lockdowncode.eyedread.communication.MultiCastSender;

public class PairingActivity extends AppCompatActivity {

    private static PairingActivity instance;

    public static PairingActivity getInstance() {
        return instance;
    }

    private ProgressDialog waitingDialog;

    //DEFINING A STRING ADAPTER WHICH WILL HANDLE THE DATA OF THE LISTVIEW
    CustomConnectionListAdapter adapter;

    ArrayList<ConnectionDataModel> dataModels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pairing);

        instance = this;

        ListView lv = this.findViewById(R.id.listView);
        dataModels = new ArrayList<>();

        adapter= new CustomConnectionListAdapter(dataModels,getApplicationContext());

        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final ConnectionDataModel item = adapter.getItem(position);

                // custom dialog
                final Dialog dialog = new Dialog(PairingActivity.getInstance());
                dialog.setContentView(R.layout.pin_dialog);

                final TextView pin = dialog.findViewById(R.id.pinText);
                Button sendPin = dialog.findViewById(R.id.sendPIN);

                sendPin.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        MainActivity.getInstance().saveNewConnection(item.getName(), item.getIp(), item.getMac());

                        // send paring pin to desktop
                        Message msg = new Message();
                        Bundle data = new Bundle();
                        data.putString("destination", item.getIp());
                        data.putString("message", "0002:"+pin.getText());
                        msg.setData(data);
                        CommunicationService.uiMessageReceiverHandler.sendMessage(msg);

                        dialog.dismiss();

                        waitingDialog = ProgressDialog.show(PairingActivity.this, "",
                                "Waiting for connection...", true);

                        /*Intent homepage = new Intent(MainActivity.getInstance(), MainActivity.class);
                        homepage.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        startActivity(homepage);*/
                    }
                });

                dialog.show();

            }
        });

        addDesktopClient("Adi-Desktop","192.168.100.8", "f8a8214ed");
        addDesktopClient("Toni-Desktop","192.168.0.104", "eda3244ed");

        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i=0; i<10; i++) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    String multicastMessage = "0000:09fe5d9775f04a4b8b9b081a8e732bae:Adi";
                    new MultiCastSender(MainActivity.getInstance(), "255.255.255.255", 33558, multicastMessage).start();
                }
                PairingActivity.this.stopSearching();
            }
        }).start();
    }

    public void stopSearching() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PairingActivity.getInstance().findViewById(R.id.textView8).setVisibility(View.GONE);
                PairingActivity.getInstance().findViewById(R.id.progressBar4).setVisibility(View.GONE);
            }});
    }

    public void addDesktopClient(final String name, final String ip, final String mac) {
        synchronized (adapter) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    boolean exists = false;
                    for (int i=0; i<adapter.getCount(); i++) {
                        if (adapter.getItem(i).getMac().equals(mac)) {
                            exists = true;
                        }
                    }
                    if (!exists) {
                        adapter.add(new ConnectionDataModel(name, ip, mac));
                    }
                }
            });
        }
    }

    public void pairingSuccessful() {
        if (waitingDialog != null) {
            waitingDialog.dismiss();
        }
    }
}
