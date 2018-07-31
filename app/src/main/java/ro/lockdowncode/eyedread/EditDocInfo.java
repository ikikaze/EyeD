package ro.lockdowncode.eyedread;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EditDocInfo extends AppCompatActivity {

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
        CustomDocFieldValueAdapter adapter = new CustomDocFieldValueAdapter(list, getApplicationContext());
        listView.setAdapter(adapter);
    }
}
