package ro.lockdowncode.eyedread.pairing.tabs;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import ro.lockdowncode.eyedread.DesktopConnection;
import ro.lockdowncode.eyedread.R;

/**
 * Created by Adi Neag on 13.07.2018.
 */

public class TabActive extends Fragment {

    private DesktopConnectionRowAdapter adapter;
    private List<DesktopConnection> myList;

    public TabActive() {
        myList = new ArrayList<>();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v =  inflater.inflate(R.layout.fragment_tab_connections, container, false);
        RecyclerView recyclerView = (RecyclerView)v.findViewById(R.id.connectionsView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new DesktopConnectionRowAdapter(myList, false);
        recyclerView.setAdapter(adapter);

        return v;
    }


    public void notifyData(List<DesktopConnection> myList) {
        this.myList = myList;
        adapter.notifyData(myList);
    }
}
