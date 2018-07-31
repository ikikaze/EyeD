package ro.lockdowncode.eyedread;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

public class SendDocument extends AppCompatActivity {

    private static int RESULT_LOAD_IMG = 1;

    private static SendDocument instance;

    public static SendDocument getInstance() {
        return instance;
    }

    private PictureHandler mPictureHandler;

    private String source;
    private Utils.Document type;

    private boolean failedStatus = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_document);

        instance = this;
        failedStatus = false;

        source = getIntent().getStringExtra("source");
        type = Utils.Document.valueOf(getIntent().getStringExtra("type"));
        mPictureHandler = new PictureHandler(this, type);

        TextView title = findViewById(R.id.scanningTypeTitle);
        switch (type) {
            case BULETIN: title.setText("Scanare Buletin");break;
            case PASAPORT: title.setText("Scanare Pasaport");break;
            case PERMIS: title.setText("Scanare Permis");break;
        }

        if (source.equals("camera")) {
            byte[] picData = EyeDRead.getInstance().getCapturedPhotoData();
            Bitmap bmp = BitmapFactory.decodeByteArray(picData, 0, picData.length);
            ImageView image = findViewById(R.id.documentView);
            image.setImageBitmap(Bitmap.createBitmap(bmp));

            mPictureHandler.sendPictureToPC(picData, type.getType());
        } else if (source.equals("gallery")) {
            ImageView imgView = findViewById(R.id.documentView);
            Bitmap bitmap = BitmapFactory
                    .decodeFile(getIntent().getStringExtra("imgString"));
            imgView.setImageBitmap(bitmap);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageInByte = baos.toByteArray();

            mPictureHandler.sendPictureToPC(imageInByte, type.getType());
        }
        updateStatus("Se trimite poza", false);
    }

    public void btnClicked(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.btnBack:
                Intent homeIntent = new Intent(this, MainActivity.class);
                startActivity(homeIntent);
                break;
            case R.id.btnAnotherPhoto:
                if (source.equals("camera")) {
                    Intent intent = new Intent(this, LicenseActivity.class);
                    intent.putExtra("type", type.name());
                    startActivity(intent);
                } else {
                    Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
                }

                break;
        }
    }

    public void updateStatus(final String message, final boolean failed) {
        this.failedStatus = failed;
        if (!failed && this.failedStatus) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView status = findViewById(R.id.sendingStatus);
                status.setText(message);
                ProgressBar pb = findViewById(R.id.progressBar);
                if (failed) {
                    pb.setVisibility(View.GONE);
                    status.setTextColor(Color.RED);
                } else {
                    pb.setVisibility(View.VISIBLE);
                    status.setTextColor(Color.BLACK);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            // When an Image is picked
            if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK
                    && null != data) {
                // Get the Image from data

                Uri selectedImage = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };

                // Get the cursor
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                // Move to first row
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String imgDecodableString = cursor.getString(columnIndex);
                cursor.close();

                ImageView imgView = findViewById(R.id.documentView);
                Bitmap bitmap = BitmapFactory.decodeFile(imgDecodableString);
                imgView.setImageBitmap(bitmap);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] imageInByte = baos.toByteArray();

                mPictureHandler.sendPictureToPC(imageInByte, type.getType());

            } else {
                Toast.makeText(this, "You haven't picked Image",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }

    }

    public void validDataFromDesktop(String dataJson) {
        Intent homeIntent = new Intent(this, EditDocInfo.class);
        homeIntent.putExtra("dataJson", dataJson);
        startActivity(homeIntent);
        this.finish();
    }
}
