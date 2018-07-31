package ro.lockdowncode.eyedread;

import android.Manifest;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.cameraview.CameraView;

import ro.lockdowncode.eyedread.UI.FlashButton;
import ro.lockdowncode.eyedread.UI.RectSizeHandler;
import ro.lockdowncode.eyedread.Utils.Document;


public class LicenseActivity extends AppCompatActivity {


    private static final String TAG = "EDR-L";
    private CameraView mCameraView;
    private FlashButton btnFlash;
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private Window wind;


    private CameraView.Callback mCallback
            = new CameraView.Callback() {

        @Override
        public void onCameraOpened(CameraView cameraView) {
            Log.d(TAG, "onCameraOpened");
        }

        @Override
        public void onCameraClosed(CameraView cameraView) {
            Log.d(TAG, "onCameraClosed");
        }

        @Override
        public void onPictureTaken(CameraView cameraView, final byte[] data) {
            Log.d(TAG, "onPictureTaken " + data.length);
            Toast.makeText(cameraView.getContext(), R.string.picture_taken, Toast.LENGTH_SHORT)
                    .show();

            EyeDRead.getInstance().setCapturedPhotoData(data);
            mCameraView.stop();

            Intent sendDocument = new Intent(LicenseActivity.this, SendDocument.class);
            sendDocument.putExtra("source", "camera");
            sendDocument.putExtra("type", getIntent().getStringExtra("type"));
            sendDocument.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(sendDocument);
        }

    };

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.fab_take_picture:
                    if (mCameraView != null) {
                        mCameraView.takePicture();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license);

        // wakes up device if screen is locked
        wind = this.getWindow();
        wind.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        wind.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        wind.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        wind.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        wind.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);


        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        ActionBar actionBar = getActionBar();
        if(actionBar != null)
            actionBar.hide();


        mCameraView = findViewById(R.id.camera);
        if (mCameraView != null) {
            mCameraView.addCallback(mCallback);
        }
        Intent intent = getIntent();
        Document type = Document.valueOf(intent.getStringExtra("type"));

        View rectview = findViewById(R.id.Rect);
        ViewGroup.LayoutParams params = rectview.getLayoutParams();

        RectSizeHandler sizeHandler = new RectSizeHandler();
        int[] widthHeight = sizeHandler.getRectSizes(type);
        params.width = widthHeight[0];
        params.height = widthHeight[1];
        rectview.setLayoutParams(params);

        FloatingActionButton fab = findViewById(R.id.fab_take_picture);
        if (fab != null) {
            fab.setOnClickListener(mOnClickListener);
        }

        btnFlash = findViewById(R.id.btnFlash);
        btnFlash.setCameraView(mCameraView);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            mCameraView.start();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        }
    }

    @Override
    protected void onPause() {
        mCameraView.stop();
        super.onPause();
    }

}


