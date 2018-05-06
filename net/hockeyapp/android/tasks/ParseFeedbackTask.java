package net.hockeyapp.android.tasks;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.google.android.gms.plus.PlusShare;
import java.util.ArrayList;
import net.hockeyapp.android.FeedbackActivity;
import net.hockeyapp.android.FeedbackManager;
import net.hockeyapp.android.FeedbackManagerListener;
import net.hockeyapp.android.objects.FeedbackMessage;
import net.hockeyapp.android.objects.FeedbackResponse;
import net.hockeyapp.android.utils.FeedbackParser;
import net.hockeyapp.android.utils.PrefsUtil;
import net.hockeyapp.android.utils.Util;

public class ParseFeedbackTask extends AsyncTask<Void, Void, FeedbackResponse> {
    public static final String ID_LAST_MESSAGE_PROCESSED = "idLastMessageProcessed";
    public static final String ID_LAST_MESSAGE_SEND = "idLastMessageSend";
    public static final int NEW_ANSWER_NOTIFICATION_ID = 2;
    public static final String PREFERENCES_NAME = "net.hockeyapp.android.feedback";
    private Context context;
    private String feedbackResponse;
    private Handler handler;
    private String requestType;
    private String urlString = null;

    public ParseFeedbackTask(Context context, String feedbackResponse, Handler handler, String requestType) {
        this.context = context;
        this.feedbackResponse = feedbackResponse;
        this.handler = handler;
        this.requestType = requestType;
    }

    public void setUrlString(String urlString) {
        this.urlString = urlString;
    }

    protected FeedbackResponse doInBackground(Void... params) {
        if (this.context == null || this.feedbackResponse == null) {
            return null;
        }
        FeedbackResponse response = FeedbackParser.getInstance().parseFeedbackResponse(this.feedbackResponse);
        if (response == null || response.getFeedback() == null) {
            return response;
        }
        ArrayList<FeedbackMessage> messages = response.getFeedback().getMessages();
        if (messages == null || messages.isEmpty()) {
            return response;
        }
        checkForNewAnswers(messages);
        return response;
    }

    protected void onPostExecute(FeedbackResponse result) {
        if (result != null && this.handler != null) {
            Message msg = new Message();
            Bundle bundle = new Bundle();
            bundle.putSerializable("parse_feedback_response", result);
            msg.setData(bundle);
            this.handler.sendMessage(msg);
        }
    }

    private void checkForNewAnswers(ArrayList<FeedbackMessage> messages) {
        FeedbackMessage latestMessage = (FeedbackMessage) messages.get(messages.size() - 1);
        int idLatestMessage = latestMessage.getId();
        SharedPreferences preferences = this.context.getSharedPreferences(PREFERENCES_NAME, 0);
        if (this.requestType.equals("send")) {
            PrefsUtil.applyChanges(preferences.edit().putInt(ID_LAST_MESSAGE_SEND, idLatestMessage).putInt(ID_LAST_MESSAGE_PROCESSED, idLatestMessage));
        } else if (this.requestType.equals("fetch")) {
            int idLastMessageSend = preferences.getInt(ID_LAST_MESSAGE_SEND, -1);
            int idLastMessageProcessed = preferences.getInt(ID_LAST_MESSAGE_PROCESSED, -1);
            if (idLatestMessage != idLastMessageSend && idLatestMessage != idLastMessageProcessed) {
                PrefsUtil.applyChanges(preferences.edit().putInt(ID_LAST_MESSAGE_PROCESSED, idLatestMessage));
                boolean eventHandled = false;
                FeedbackManagerListener listener = FeedbackManager.getLastListener();
                if (listener != null) {
                    eventHandled = listener.feedbackAnswered(latestMessage);
                }
                if (!eventHandled) {
                    startNotification(this.context);
                }
            }
        }
    }

    private void startNotification(Context context) {
        if (this.urlString != null) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService("notification");
            int iconId = context.getResources().getIdentifier("ic_menu_refresh", "drawable", "android");
            Class<?> activityClass = null;
            if (FeedbackManager.getLastListener() != null) {
                activityClass = FeedbackManager.getLastListener().getFeedbackActivityClass();
            }
            if (activityClass == null) {
                activityClass = FeedbackActivity.class;
            }
            Intent intent = new Intent();
            intent.setFlags(805306368);
            intent.setClass(context, activityClass);
            intent.putExtra(PlusShare.KEY_CALL_TO_ACTION_URL, this.urlString);
            Notification notification = Util.createNotification(context, PendingIntent.getActivity(context, 0, intent, 1073741824), "HockeyApp Feedback", "A new answer to your feedback is available.", iconId);
            if (notification != null) {
                notificationManager.notify(2, notification);
            }
        }
    }
}
