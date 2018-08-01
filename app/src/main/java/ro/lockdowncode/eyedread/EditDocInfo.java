package ro.lockdowncode.eyedread;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ro.lockdowncode.eyedread.communication.CommunicationService;

public class EditDocInfo extends AppCompatActivity {

    private CustomDocFieldValueAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_doc_info);

        byte[] picData = EyeDRead.getInstance().getCapturedPhotoData();
        Bitmap bmp = BitmapFactory.decodeByteArray(picData, 0, picData.length);
        ImageView image = findViewById(R.id.imageView);
        image.setImageBitmap(Bitmap.createBitmap(bmp));

        List<FieldValueDataModel> list = new ArrayList<>();
        try {
            JSONObject json = new JSONObject(getIntent().getStringExtra("dataJson"));
            Iterator<?> keys = json.keys();
            while( keys.hasNext() ) {
                String key = (String)keys.next();
                String value = json.getString(key);
                list.add(new FieldValueDataModel(key, value));
            }
        } catch (JSONException e) {
            e.printStackTrace(System.out);
        }

        ListView listView = findViewById(R.id.listView);
        adapter = new CustomDocFieldValueAdapter(list, getApplicationContext());
        listView.setAdapter(adapter);
    }

    private String getFieldsValuesJson() {
        JSONObject json = new JSONObject();
        ListView listView = findViewById(R.id.listView);
        for(int i=0; i<listView.getAdapter().getCount(); i++) {
            View v = listView.getChildAt(i);
            TextView name = v.findViewById(R.id.fieldName);
            EditText value = v.findViewById(R.id.fieldValue);
            try {
                json.put(name.getText().toString(), value.getText().toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return json.toString();
    }

    public void btnClicked(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.btnBack:
                cancelCurrentServerProcess();
                Intent homeIntent = new Intent(this, MainActivity.class);
                startActivity(homeIntent);
                break;
            case R.id.btnNext:
                Intent intent = new Intent(this, TemplatesList.class);
                intent.putExtra("type", getIntent().getStringExtra("type"));
                intent.putExtra("dataJson", getFieldsValuesJson());
                startActivity(intent);
                break;
        }
    }

    private void cancelCurrentServerProcess() {
        //send templates
        Message msg = new Message();
        Bundle data = new Bundle();
        data.putString("destination", MainActivity.getInstance().getActiveDesktopConnection().getIp());
        data.putString("message", "0022:CancelCurrentProcess");
        msg.setData(data);
        CommunicationService.uiMessageReceiverHandler.sendMessage(msg);
    }
}
