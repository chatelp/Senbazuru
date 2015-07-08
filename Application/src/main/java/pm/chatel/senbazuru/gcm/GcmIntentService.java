package pm.chatel.senbazuru.gcm;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.util.logging.Level;
import java.util.logging.Logger;

import pm.chatel.senbazuru.Constants;
import pm.chatel.senbazuru.MainApplication;
import pm.chatel.senbazuru.R;
import pm.chatel.senbazuru.activity.HomeActivity;
import pm.chatel.senbazuru.utils.PrefUtils;


/**
 * Created by pierre on 12/06/15.
 */
public class GcmIntentService extends IntentService {

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (extras != null && !extras.isEmpty()) {  // has effect of unparcelling Bundle
            // Since we're not using two way messaging, this is all we really to check for
            if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                Logger.getLogger("GCM_RECEIVED").log(Level.INFO, extras.toString());

                handleNewTutorial(extras.getString("message"));
            }
        }
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    protected void handleNewTutorial(final String message) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (PrefUtils.getBoolean(PrefUtils.NOTIFICATIONS_ENABLED, true)) {
                    Intent notificationIntent = new Intent(getApplicationContext(), HomeActivity.class).putExtra(HomeActivity.EXTRA_SHOULD_REFRESH, true);

                    PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);

                    Notification.Builder notifBuilder = new Notification.Builder(MainApplication.getContext()) //
                            .setContentIntent(contentIntent)
                            .setSmallIcon(R.drawable.senbazuru_ui_icon_44pt) //
                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.android_icon)) //
                            .setWhen(System.currentTimeMillis()) //
                            .setAutoCancel(true) //
                            .setContentTitle(getString(R.string.notifications_title)) //
                            .setContentText(message) //
                            .setLights(0xffffffff, 0, 0);

                    if (PrefUtils.getBoolean(PrefUtils.NOTIFICATIONS_VIBRATE, false)) {
                        notifBuilder.setVibrate(new long[]{0, 1000});
                    }

                    if (PrefUtils.getBoolean(PrefUtils.NOTIFICATIONS_RINGTONE, false)) {
                        Uri notifURI = Uri.parse("android.resource://"
                                + getBaseContext().getPackageName() + "/" + R.raw.dada);
                        notifBuilder.setSound(notifURI);
                    }

                    if (PrefUtils.getBoolean(PrefUtils.NOTIFICATIONS_LIGHT, false)) {
                        notifBuilder.setLights(0xffffffff, 300, 1000);
                    }

                    if (Constants.NOTIF_MGR != null) {
                        Constants.NOTIF_MGR.notify(0, notifBuilder.getNotification());
                    }
                }
            }
        });
    }
}