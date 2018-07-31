package ro.lockdowncode.eyedread;

import android.app.Application;

import io.realm.Realm;

/**
 * Created by Adi Neag on 13.07.2018.
 */

public final class EyeDRead extends Application {

    private static EyeDRead sInstance;
    private byte[] mCapturedPhotoData;

    // Getters & Setters
    public byte[] getCapturedPhotoData() {
        return mCapturedPhotoData;
    }

    public void setCapturedPhotoData(byte[] capturedPhotoData) {
        mCapturedPhotoData = capturedPhotoData;
    }

    // Singleton code
    public static EyeDRead getInstance() { return sInstance; }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        Realm.init(getApplicationContext());
    }
}
