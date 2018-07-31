package ro.lockdowncode.eyedread.pairing.tabs;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.bouncycastle.jcajce.provider.symmetric.DES;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import ro.lockdowncode.eyedread.DesktopConnection;
import ro.lockdowncode.eyedread.R;

/**
 * Created by Adi Neag on 13.07.2018.
 */

public class TabSaved extends Fragment {

    private DesktopConnectionRowAdapter adapter;

    public TabSaved() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        List<DesktopConnection> saved = new ArrayList<>();
        Realm realm = Realm.getDefaultInstance();
        try {
            RealmResults<DesktopConnection> results = realm.where(DesktopConnection.class).findAll();
            for (DesktopConnection dc: results) {
                saved.add(realm.copyFromRealm(dc));
            }
        } finally {
            realm.close();
        }

        View v =  inflater.inflate(R.layout.fragment_tab_connections, container, false);
        RecyclerView recyclerView = (RecyclerView)v.findViewById(R.id.connectionsView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new DesktopConnectionRowAdapter(saved, true);
        recyclerView.setAdapter(adapter);

        return v;
    }

}
