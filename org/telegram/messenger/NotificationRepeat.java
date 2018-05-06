package org.telegram.messenger;

import android.app.IntentService;
import android.content.Intent;

public class NotificationRepeat extends IntentService {

    class C05351 implements Runnable {
        C05351() {
        }

        public void run() {
            NotificationsController.getInstance().repeatNotificationMaybe();
        }
    }

    public NotificationRepeat() {
        super("NotificationRepeat");
    }

    protected void onHandleIntent(Intent intent) {
        AndroidUtilities.runOnUIThread(new C05351());
    }
}
