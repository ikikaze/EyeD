package ro.lockdowncode.eyedread;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import ro.lockdowncode.eyedread.Utils.Type;

import static ro.lockdowncode.eyedread.Utils.typeToString;

public class PictureHandler {

    private static final String TAG = "PictureSaver";
    private Handler mBackgroundHandler;
    private Type mType;
    private Context mContext;

    public PictureHandler(Context context, Type type) {
        mType = type;
        mContext = context;

        getBackgroundHandler();
    }


    private void getBackgroundHandler() {
        if (mBackgroundHandler == null) {
            HandlerThread thread = new HandlerThread("background" + typeToString(mType));
            thread.start();
            mBackgroundHandler = new Handler(thread.getLooper());
        }
    }


    public void savePicture(final byte[] data) {
        mBackgroundHandler.post(new Runnable() {
            @Override
            public void run() {
                //first check folders for existence, create if not
                String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + "EyeDRead" + File.separator + typeToString(mType);
                File folder = new File(path);
                if (!folder.exists())
                    folder.mkdirs();

                //make file name
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                String currentDateandTime = sdf.format(new Date());
                String picName = typeToString(mType) + currentDateandTime + ".jpg";

                File file = new File(path, picName);
                OutputStream os = null;
                try {

                    os = new FileOutputStream(file);
                    os.write(data);
                    os.close();
                } catch (IOException e) {
                    Log.w(TAG, "Cannot write to " + file, e);
                } finally {
                    if (os != null) {
                        try {
                            os.close();
                            Intent intent =
                                    new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                            intent.setData(Uri.fromFile(file));
                            mContext.sendBroadcast(intent);
                        } catch (IOException e) {
                            // Ignore
                        }
                    }
                }
            }
        });
    }


    public void sendPictureToPC(final byte[] data)
    {

        //ADD SENDING LOGIC HERE
    }


}
