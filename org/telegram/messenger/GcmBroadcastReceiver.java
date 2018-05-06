package org.telegram.messenger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import org.json.JSONObject;
import org.telegram.tgnet.ConnectionsManager;

public class GcmBroadcastReceiver extends BroadcastReceiver {
    public static final int NOTIFICATION_ID = 1;

    public void onReceive(Context context, final Intent intent) {
        FileLog.m608d("tmessages", "GCM received intent: " + intent);
        if (intent.getAction().equals("com.google.android.c2dm.intent.RECEIVE")) {
            AndroidUtilities.runOnUIThread(new Runnable() {
                public void run() {
                    ApplicationLoader.postInitApplication();
                    try {
                        if ("DC_UPDATE".equals(intent.getStringExtra("loc_key"))) {
                            JSONObject object = new JSONObject(intent.getStringExtra("custom"));
                            int dc = object.getInt("dc");
                            String[] parts = object.getString("addr").split(":");
                            if (parts.length == 2) {
                                ConnectionsManager.getInstance().applyDatacenterAddress(dc, parts[0], Integer.parseInt(parts[1]));
                            } else {
                                return;
                            }
                        }
                    } catch (Throwable e) {
                        FileLog.m611e("tmessages", e);
                    }
                    ConnectionsManager.getInstance().resumeNetworkMaybe();
                }
            });
        } else if (intent.getAction().equals("com.google.android.c2dm.intent.REGISTRATION")) {
            String registration = intent.getStringExtra(ApplicationLoader.PROPERTY_REG_ID);
            if (intent.getStringExtra("error") != null) {
                FileLog.m609e("tmessages", "Registration failed, should try again later.");
            } else if (intent.getStringExtra("unregistered") != null) {
                FileLog.m609e("tmessages", "unregistration done, new messages from the authorized sender will be rejected");
            } else if (registration != null) {
                FileLog.m609e("tmessages", "registration id = " + registration);
            }
        }
        setResultCode(-1);
    }
}
