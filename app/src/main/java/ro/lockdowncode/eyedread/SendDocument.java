package ro.lockdowncode.eyedread;

import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

public class SendDocument extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_document);

        ImageView imgView = (ImageView) findViewById(R.id.documentView);
        imgView.setImageBitmap(BitmapFactory
                .decodeFile(getIntent().getStringExtra("imgString")));
    }
}
