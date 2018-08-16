package ro.lockdowncode.eyedread;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Adi Neag on 23.07.2018.
 */

public class CustomDocFieldValueAdapter extends BaseAdapter{

    Context context;
    List<FieldValueDataModel> listforview;
    LayoutInflater inflator=null;
    View v;
    ViewHolder vholder;
    //Constructor

    public CustomDocFieldValueAdapter(List<FieldValueDataModel> data, Context con) {
        super();
        context=con;
        listforview=data;
        inflator=LayoutInflater.from(con);
    }

    // return position here
    @Override
    public long getItemId(int position) {
        return position;
    }
    // return size of list
    @Override
    public int getCount() {
        return listforview.size();
    }
    //get Object from each position
    @Override
    public Object getItem(int position) {
        return listforview.get(position);
    }
    //Viewholder class to contain inflated xml views
    private class ViewHolder
    {
        TextView name;
        EditText value;
    }

    // Called for each view
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        v=convertView;
        if(convertView==null)
        {
            //inflate the view for each row of listview
            v=inflator.inflate(R.layout.doc_field_item,null);
            //ViewHolder object to contain myadapter.xml elements
            vholder=new ViewHolder();
            vholder.name=(TextView)v.findViewById(R.id.fieldName);
            vholder.value=(EditText)v.findViewById(R.id.fieldValue);
            //set holder to the view
            v.setTag(vholder);
        }
        else
            vholder=(ViewHolder)v.getTag();

        //getting MyItem Object for each position
        FieldValueDataModel item=(FieldValueDataModel)listforview.get(position);
        //set the id to editetxt important line here as it will be helpful to set text according to position
        vholder.name.setText(item.getName());
        vholder.value.setText(item.getValue());

        vholder.value.setId(position);
        //setting the values from object to holder views for each row
        vholder.value.setOnFocusChangeListener(
                new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (!hasFocus) {
                            final int id = v.getId();
                            final EditText field = ((EditText) v);
                            listforview.get(id).value=(field.getText().toString());
                        }
                    }
                }
        );
        return v;
    }

    public List<FieldValueDataModel> getItems() {
        return listforview;
    }
}