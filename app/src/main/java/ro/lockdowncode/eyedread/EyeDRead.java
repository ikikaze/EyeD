package ro.lockdowncode.eyedread;

import android.app.Application;

import io.realm.Realm;

/**
 * Created by Adi Neag on 13.07.2018.
 */

public class EyeDRead extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(getApplicationContext());
    }
}
