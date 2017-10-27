/*

Network Notifier

Copyright (C) 2015 Jesús Diéguez Fernández

This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA or see http://www.gnu.org/licenses/.

*/

package es.reprogramador.util;

import android.app.PendingIntent;
import android.app.NotificationManager;
import android.support.v4.app.NotificationCompat;
import android.graphics.Bitmap;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

public class notificationHelper {

    private Context _context = null;
    private int _icon = 0;
    private Bitmap _iconBitmap = null;

    /**
     *
     * @param c             Application context, usually "this".
     * @param icon          Icon identifier to be shown in the notification bar.
     * @param iconBitmap    Icon bitmap to be shown in the notification list.
     */
    public notificationHelper(Context c, int icon, Bitmap iconBitmap)
    {
        _context=c;
        _icon = icon;
        _iconBitmap = iconBitmap;
    }

    /**
     *
     * @param id            Indicates an unique identifier for a specific notification message, you can reuse it to update information.
     * @param title         Text to be shown in the notification title
     * @param message       Text to be shown in the notification body
     * @param subMessage    Text to be shown in the notification footer
     */
    public void showNotification(int id, String title, String message, String subMessage) {
        showNotification(id, title, message, subMessage, true, true, true);
    }

    /**
     *
     * @param id                Indicates an unique identifier for a specific notification message, you can reuse it to update information.
     * @param title             Text to be shown in the notification title
     * @param message           Text to be shown in the notification body
     * @param subMessage        Text to be shown in the notification footer
     * @param enableSound       Play sound with the notification?
     * @param enableVibration   Vibrate with the notification?
     * @param enableLights      Turn on the notification led?
     */
    public void showNotification(int id, String title, String message, String subMessage, Boolean enableSound, Boolean enableVibration, Boolean enableLights) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(_context);

        builder.setSmallIcon(_icon);
        builder.setLargeIcon(_iconBitmap);
        builder.setContentTitle(title);
        builder.setContentText(message);
        builder.setSubText(subMessage);
        if (enableSound) {
            builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        }
        if (enableVibration) {
            builder.setVibrate(new long[] { 0, 200, 50, 500 });
        }
        if (enableLights) {
            builder.setLights(Color.RED, 1000, 250);
        }


        // force having to swipe the notification so as to remove it
        builder.setAutoCancel(false);

        if (message.toLowerCase().startsWith("http") || message.toLowerCase().startsWith("ftp")) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(message));
            PendingIntent pendingIntent = PendingIntent.getActivity(_context, 0, intent, 0);
            builder.setContentIntent(pendingIntent);
        }

        NotificationManager notificationManager = (NotificationManager) _context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            try {
                notificationManager.notify(id, builder.build());
            } catch(Exception ex) { }
        }

    }

    /**
     * Play the default "ding" sound
     */
    private void playNotificationSound() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(_context.getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
