package ro.lockdowncode.eyedread;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Adi Neag on 23.07.2018.
 */

public class CustomDocFieldValueAdapter extends ArrayAdapter<FieldValueDataModel>{

    public ArrayList<FieldValueDataModel> mItems = new ArrayList<>();

    public CustomDocFieldValueAdapter(List<FieldValueDataModel> data, Context context) {
        super(context, R.layout.doc_field_item, data);
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.doc_field_item, null, false);
        }

        final FieldValueDataModel p = getItem(position);

        if (p != null) {
            TextView tt1 = (TextView) v.findViewById(R.id.fieldName);
            EditText tt2 = (EditText) v.findViewById(R.id.fieldValue);

            if (tt1 != null) {
                tt1.setText(p.getName());
            }

            if (tt2 != null) {
                tt2.setText(p.getValue());
            }

        }

        return v;
    }
}