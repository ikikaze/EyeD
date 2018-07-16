package ro.lockdowncode.eyedread.pairing.tabs;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import ro.lockdowncode.eyedread.DesktopConnection;
import ro.lockdowncode.eyedread.R;
import ro.lockdowncode.eyedread.pairing.PairingActivity;

/**
 * Created by Adi Neag on 13.07.2018.
 */

public class DesktopConnectionRowAdapter extends RecyclerView.Adapter<DesktopConnectionRowAdapter.ViewHolder> {

    private List<DesktopConnection> mItems;

    public DesktopConnectionRowAdapter(List<DesktopConnection> items) {
        mItems = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.connection_list_item, viewGroup, false);
        final DesktopConnection item = mItems.get(i);
        v.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                PairingActivity.getInstance().selectNewDesktopConnection(item.getName(), item.getIp());
            }
        });
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        DesktopConnection item = mItems.get(i);
        viewHolder.name.setText(item.getName());
        viewHolder.ip.setText(item.getIp());
        if (item.getLastConnected()!= null) {
            viewHolder.date.setText(new SimpleDateFormat("HH:mm dd.MM.yyyy").format(item.getLastConnected()));
            viewHolder.dateTitle.setText("Ultima conexiune");
        }
    }

    public void notifyData(List<DesktopConnection> myList) {
        this.mItems = myList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView name;
        private final TextView ip;
        private final TextView date;
        private final TextView dateTitle;

        ViewHolder(View v) {
            super(v);
            name = (TextView)v.findViewById(R.id.liConnName);
            ip = (TextView)v.findViewById(R.id.liConnIP);
            date = (TextView)v.findViewById(R.id.liConnDate);
            dateTitle = (TextView)v.findViewById(R.id.liConnDateTitle);
        }
    }
}
