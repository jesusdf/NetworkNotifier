package es.reprogramador.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.PendingIntent;
import android.app.AlarmManager;

public class bootHelper extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int interval = 60000;
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = PendingIntent.getService(context, 0, new Intent(context, es.reprogramador.networknotifier.NetworkNotifierService.class), PendingIntent.FLAG_UPDATE_CURRENT);
        if (pi != null) {
            try {
                am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + interval, interval, pi);
            } catch(Exception ex) { }
        }
    }
}