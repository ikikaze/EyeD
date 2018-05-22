package ro.lockdowncode.eyedread.communication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Adi Neag on 05.05.2018.
 */

public class CommunicationServiceRestratReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, CommunicationService.class));
    }
}
