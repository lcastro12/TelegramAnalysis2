package net.hockeyapp.android.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.os.EnvironmentCompat;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.hockeyapp.android.Constants;
import net.hockeyapp.android.utils.HttpURLConnectionBuilder;
import net.hockeyapp.android.utils.Util;

public class SendFeedbackTask extends ConnectionTask<Void, Void, HashMap<String, String>> {
    private List<Uri> attachmentUris;
    private Context context;
    private String email;
    private Handler handler;
    private boolean isFetchMessages;
    private int lastMessageId = -1;
    private String name;
    private ProgressDialog progressDialog;
    private boolean showProgressDialog = true;
    private String subject;
    private String text;
    private String token;
    private HttpURLConnection urlConnection;
    private String urlString;

    public SendFeedbackTask(Context context, String urlString, String name, String email, String subject, String text, List<Uri> attachmentUris, String token, Handler handler, boolean isFetchMessages) {
        this.context = context;
        this.urlString = urlString;
        this.name = name;
        this.email = email;
        this.subject = subject;
        this.text = text;
        this.attachmentUris = attachmentUris;
        this.token = token;
        this.handler = handler;
        this.isFetchMessages = isFetchMessages;
        if (context != null) {
            Constants.loadFromContext(context);
        }
    }

    public void setShowProgressDialog(boolean showProgressDialog) {
        this.showProgressDialog = showProgressDialog;
    }

    public void setLastMessageId(int lastMessageId) {
        this.lastMessageId = lastMessageId;
    }

    public void attach(Context context) {
        this.context = context;
    }

    public void detach() {
        this.context = null;
        if (this.progressDialog != null) {
            this.progressDialog.dismiss();
        }
        this.progressDialog = null;
    }

    protected void onPreExecute() {
        String loadingMessage = "Sending feedback..";
        if (this.isFetchMessages) {
            loadingMessage = "Retrieving discussions...";
        }
        if ((this.progressDialog == null || !this.progressDialog.isShowing()) && this.showProgressDialog) {
            this.progressDialog = ProgressDialog.show(this.context, "", loadingMessage, true, false);
        }
    }

    protected HashMap<String, String> doInBackground(Void... args) {
        if (this.isFetchMessages && this.token != null) {
            return doGet();
        }
        if (this.isFetchMessages) {
            return null;
        }
        if (this.attachmentUris.isEmpty()) {
            return doPostPut();
        }
        HashMap<String, String> result = doPostPutWithAttachments();
        String status = (String) result.get("status");
        if (status == null || !status.startsWith("2") || this.context == null) {
            return result;
        }
        File folder = new File(this.context.getCacheDir(), "HockeyApp");
        if (!folder.exists()) {
            return result;
        }
        for (File file : folder.listFiles()) {
            file.delete();
        }
        return result;
    }

    protected void onPostExecute(HashMap<String, String> result) {
        if (this.progressDialog != null) {
            try {
                this.progressDialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (this.handler != null) {
            Message msg = new Message();
            Bundle bundle = new Bundle();
            if (result != null) {
                bundle.putString("request_type", (String) result.get("type"));
                bundle.putString("feedback_response", (String) result.get("response"));
                bundle.putString("feedback_status", (String) result.get("status"));
            } else {
                bundle.putString("request_type", EnvironmentCompat.MEDIA_UNKNOWN);
            }
            msg.setData(bundle);
            this.handler.sendMessage(msg);
        }
    }

    private HashMap<String, String> doPostPut() {
        HashMap<String, String> result = new HashMap();
        result.put("type", "send");
        HttpURLConnection urlConnection = null;
        try {
            Map<String, String> parameters = new HashMap();
            parameters.put("name", this.name);
            parameters.put("email", this.email);
            parameters.put("subject", this.subject);
            parameters.put("text", this.text);
            parameters.put("bundle_identifier", Constants.APP_PACKAGE);
            parameters.put("bundle_short_version", Constants.APP_VERSION_NAME);
            parameters.put("bundle_version", Constants.APP_VERSION);
            parameters.put("os_version", Constants.ANDROID_VERSION);
            parameters.put("oem", Constants.PHONE_MANUFACTURER);
            parameters.put("model", Constants.PHONE_MODEL);
            if (this.token != null) {
                this.urlString += this.token + "/";
            }
            urlConnection = new HttpURLConnectionBuilder(this.urlString).setRequestMethod(this.token != null ? "PUT" : "POST").writeFormFields(parameters).build();
            urlConnection.connect();
            result.put("status", String.valueOf(urlConnection.getResponseCode()));
            result.put("response", ConnectionTask.getStringFromConnection(urlConnection));
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        } catch (Throwable th) {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return result;
    }

    private HashMap<String, String> doPostPutWithAttachments() {
        HashMap<String, String> result = new HashMap();
        result.put("type", "send");
        HttpURLConnection urlConnection = null;
        try {
            Map<String, String> parameters = new HashMap();
            parameters.put("name", this.name);
            parameters.put("email", this.email);
            parameters.put("subject", this.subject);
            parameters.put("text", this.text);
            parameters.put("bundle_identifier", Constants.APP_PACKAGE);
            parameters.put("bundle_short_version", Constants.APP_VERSION_NAME);
            parameters.put("bundle_version", Constants.APP_VERSION);
            parameters.put("os_version", Constants.ANDROID_VERSION);
            parameters.put("oem", Constants.PHONE_MANUFACTURER);
            parameters.put("model", Constants.PHONE_MODEL);
            if (this.token != null) {
                this.urlString += this.token + "/";
            }
            urlConnection = new HttpURLConnectionBuilder(this.urlString).setRequestMethod(this.token != null ? "PUT" : "POST").writeMultipartData(parameters, this.context, this.attachmentUris).build();
            urlConnection.connect();
            result.put("status", String.valueOf(urlConnection.getResponseCode()));
            result.put("response", ConnectionTask.getStringFromConnection(urlConnection));
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        } catch (Throwable th) {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return result;
    }

    private HashMap<String, String> doGet() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.urlString + Util.encodeParam(this.token));
        if (this.lastMessageId != -1) {
            sb.append("?last_message_id=" + this.lastMessageId);
        }
        HashMap<String, String> result = new HashMap();
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = new HttpURLConnectionBuilder(sb.toString()).build();
            result.put("type", "fetch");
            urlConnection.connect();
            result.put("status", String.valueOf(urlConnection.getResponseCode()));
            result.put("response", ConnectionTask.getStringFromConnection(urlConnection));
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        } catch (Throwable th) {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return result;
    }
}
