package net.hockeyapp.android.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.Map;
import net.hockeyapp.android.Constants;
import net.hockeyapp.android.utils.HttpURLConnectionBuilder;
import net.hockeyapp.android.utils.PrefsUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.support.widget.helper.ItemTouchHelper.Callback;

public class LoginTask extends ConnectionTask<Void, Void, Boolean> {
    private Context context;
    private Handler handler;
    private final int mode;
    private final Map<String, String> params;
    private ProgressDialog progressDialog;
    private boolean showProgressDialog = true;
    private final String urlString;

    public LoginTask(Context context, Handler handler, String urlString, int mode, Map<String, String> params) {
        this.context = context;
        this.handler = handler;
        this.urlString = urlString;
        this.mode = mode;
        this.params = params;
        if (context != null) {
            Constants.loadFromContext(context);
        }
    }

    public void setShowProgressDialog(boolean showProgressDialog) {
        this.showProgressDialog = showProgressDialog;
    }

    public void attach(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;
    }

    public void detach() {
        this.context = null;
        this.handler = null;
        this.progressDialog = null;
    }

    protected void onPreExecute() {
        if ((this.progressDialog == null || !this.progressDialog.isShowing()) && this.showProgressDialog) {
            this.progressDialog = ProgressDialog.show(this.context, "", "Please wait...", true, false);
        }
    }

    protected Boolean doInBackground(Void... args) {
        HttpURLConnection connection = null;
        try {
            connection = makeRequest(this.mode, this.params);
            connection.connect();
            if (connection.getResponseCode() == Callback.DEFAULT_DRAG_ANIMATION_DURATION) {
                String responseStr = ConnectionTask.getStringFromConnection(connection);
                if (!TextUtils.isEmpty(responseStr)) {
                    Boolean valueOf = Boolean.valueOf(handleResponse(responseStr));
                    if (connection == null) {
                        return valueOf;
                    }
                    connection.disconnect();
                    return valueOf;
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            if (connection != null) {
                connection.disconnect();
            }
        } catch (IOException e2) {
            e2.printStackTrace();
            if (connection != null) {
                connection.disconnect();
            }
        } catch (Throwable th) {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return Boolean.valueOf(false);
    }

    protected void onPostExecute(Boolean success) {
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
            bundle.putBoolean("success", success.booleanValue());
            msg.setData(bundle);
            this.handler.sendMessage(msg);
        }
    }

    private HttpURLConnection makeRequest(int mode, Map<String, String> params) throws IOException {
        if (mode == 1) {
            return new HttpURLConnectionBuilder(this.urlString).setRequestMethod("POST").writeFormFields(params).build();
        }
        if (mode == 2) {
            return new HttpURLConnectionBuilder(this.urlString).setRequestMethod("POST").setBasicAuthorization((String) params.get("email"), (String) params.get("password")).build();
        }
        if (mode == 3) {
            return new HttpURLConnectionBuilder(this.urlString + "?" + ((String) params.get("type")) + "=" + ((String) params.get("id"))).build();
        }
        throw new IllegalArgumentException("Login mode " + mode + " not supported.");
    }

    private boolean handleResponse(String responseStr) {
        SharedPreferences prefs = this.context.getSharedPreferences("net.hockeyapp.android.login", 0);
        try {
            JSONObject response = new JSONObject(responseStr);
            String status = response.getString("status");
            if (TextUtils.isEmpty(status)) {
                return false;
            }
            if (this.mode == 1) {
                if (!status.equals("identified")) {
                    return false;
                }
                String iuid = response.getString("iuid");
                if (TextUtils.isEmpty(iuid)) {
                    return false;
                }
                PrefsUtil.applyChanges(prefs.edit().putString("iuid", iuid));
                return true;
            } else if (this.mode == 2) {
                if (!status.equals("authorized")) {
                    return false;
                }
                String auid = response.getString("auid");
                if (TextUtils.isEmpty(auid)) {
                    return false;
                }
                PrefsUtil.applyChanges(prefs.edit().putString("auid", auid));
                return true;
            } else if (this.mode != 3) {
                throw new IllegalArgumentException("Login mode " + this.mode + " not supported.");
            } else if (status.equals("validated")) {
                return true;
            } else {
                PrefsUtil.applyChanges(prefs.edit().remove("iuid").remove("auid"));
                return false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }
}
