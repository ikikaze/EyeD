package ro.lockdowncode.eyedread;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Adi Neag on 21.05.2018.
 */

public class CustomConnectionListAdapter extends ArrayAdapter<ConnectionDataModel> implements View.OnClickListener{

    private ArrayList<ConnectionDataModel> dataSet;
    Context mContext;

    // View lookup cache
    private static class ViewHolder {
        TextView txtName;
        TextView txtIP;
        TextView txtMAC;
    }

    public CustomConnectionListAdapter(ArrayList<ConnectionDataModel> data, Context context) {
        super(context, R.layout.connection_list_item, data);
        this.dataSet = data;
        this.mContext=context;

    }

    @Override
    public void onClick(View v) {

        int position=(Integer) v.getTag();
        Object object= getItem(position);
        ConnectionDataModel dataModel=(ConnectionDataModel)object;
    }

    private int lastPosition = -1;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        ConnectionDataModel dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.connection_list_item, parent, false);
            viewHolder.txtName = (TextView) convertView.findViewById(R.id.liConnName);
            viewHolder.txtIP = (TextView) convertView.findViewById(R.id.liConnIP);
            viewHolder.txtMAC = (TextView) convertView.findViewById(R.id.liConnMac);

            result=convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

        lastPosition = position;

        viewHolder.txtName.setText(dataModel.getName());
        viewHolder.txtIP.setText(dataModel.getIp());
        viewHolder.txtMAC.setText(dataModel.getMac());

        // Return the completed view to render on screen
        return convertView;
    }
}