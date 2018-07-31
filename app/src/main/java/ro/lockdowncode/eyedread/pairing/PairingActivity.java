package ro.lockdowncode.eyedread.pairing;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.realm.Progress;
import ro.lockdowncode.eyedread.DesktopConnection;
import ro.lockdowncode.eyedread.MainActivity;
import ro.lockdowncode.eyedread.R;
import ro.lockdowncode.eyedread.communication.CommunicationService;
import ro.lockdowncode.eyedread.communication.MultiCastSender;
import ro.lockdowncode.eyedread.pairing.tabs.FragmentsPagerAdapter;

public class PairingActivity extends AppCompatActivity {

    private static PairingActivity instance;

    private FloatingActionButton refreshButton;

    private ProgressBar loadingSpinner;

    public static PairingActivity getInstance() {
        return instance;
    }

    public final FragmentsPagerAdapter adapter = new FragmentsPagerAdapter( getSupportFragmentManager() );

    public List<DesktopConnection> activeConnections;

    @Override
    protected void onResume() {
        super.onResume();
        activeConnections = new ArrayList<>();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pairing);

        instance = this;

        refreshButton = this.findViewById(R.id.refreshBtn);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchForDesktops();
            }
        });
        loadingSpinner = this.findViewById(R.id.loadingSpinner);

        final ViewPager viewPager = (ViewPager)findViewById(R.id.viewpager);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(0);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                Fragment fragment = adapter.getRegisteredFragment(position);
                getSupportFragmentManager()
                        .beginTransaction()
                        .detach(fragment)
                        .attach(fragment)
                        .commit();
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Code goes here
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // Code goes here
            }
        });
        TabLayout tabLayout = (TabLayout)findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        searchForDesktops();

    }

    public void searchForDesktops() {
        refreshButton.setVisibility(View.GONE);
        loadingSpinner.setVisibility(View.VISIBLE);
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
                stopSearching();
            }
        }).start();
    }

    public void stopSearching() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                refreshButton.setVisibility(View.VISIBLE);
                loadingSpinner.setVisibility(View.GONE);
            }
        });
    }

    public void addDesktopClient(final String name, final String ip, final String mac) {
        synchronized (adapter) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    boolean found = false;
                    for (DesktopConnection dc : activeConnections) {
                        if (dc.getName().equals(name) && dc.getIp().equals(ip)) {
                            found = true;
                        }
                    }
                    if (!found) {
                        activeConnections.add(new DesktopConnection(null, name, ip));
                        adapter.notifyData(activeConnections);
                    }
                }
            });
        }
    }

    public void selectSavedDesktopConnection(String name, String ip, String id) {
        MainActivity.getInstance().saveNewConnection(name, ip, id, 2);
        Intent homepage = new Intent(this, MainActivity.class);
        homepage.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(homepage);
    }

    public void selectNewDesktopConnection(final String name, final String ip) {
        // custom dialog
        final Dialog dialog = new Dialog(PairingActivity.getInstance());
        dialog.setContentView(R.layout.pin_dialog);

        TextView title = dialog.findViewById(R.id.pinDialogTitle);
        title.setText("Conectare "+name);

        final TextView hint = dialog.findViewById(R.id.pinDialogHint);

        final EditText pin = dialog.findViewById(R.id.pinText);
        Button sendPin = dialog.findViewById(R.id.sendPIN);

        sendPin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (!pinValid(pin.getText().toString())) {
                    hint.setTextColor(Color.parseColor("red"));
                    return;
                }
                MainActivity.getInstance().saveNewConnection(name, ip, null, 1);

                // send paring pin to desktop
                Message msg = new Message();
                Bundle data = new Bundle();
                data.putString("destination", ip);
                data.putString("message", "0002:"+pin.getText());
                msg.setData(data);
                CommunicationService.uiMessageReceiverHandler.sendMessage(msg);

                dialog.dismiss();

                Intent homepage = new Intent(PairingActivity.getInstance(), MainActivity.class);
                homepage.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(homepage);
            }
        });

        dialog.show();
    }

    private boolean pinValid(String pin) {
        if (pin.length() != 4) {
            return false;
        }
        try {
            Integer.parseInt(pin);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
}
