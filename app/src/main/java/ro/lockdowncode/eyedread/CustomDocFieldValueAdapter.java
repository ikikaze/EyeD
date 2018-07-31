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

public class CustomDocFieldValueAdapter extends ArrayAdapter<FieldValueDataModel> implements View.OnClickListener{

    private List<FieldValueDataModel> dataSet;
    Context mContext;

    // View lookup cache
    private static class ViewHolder {
        TextView txtName;
        EditText txtValue;
    }

    public CustomDocFieldValueAdapter(List<FieldValueDataModel> data, Context context) {
        super(context, R.layout.doc_field_item, data);
        this.dataSet = data;
        this.mContext=context;
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FieldValueDataModel dataModel = getItem(position);
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.doc_field_item, parent, false);
            viewHolder.txtName = convertView.findViewById(R.id.fieldName);
            viewHolder.txtValue = convertView.findViewById(R.id.fieldValue);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.txtName.setText(dataModel.getName());
        viewHolder.txtValue.setText(dataModel.getValue());
        return convertView;
    }
}