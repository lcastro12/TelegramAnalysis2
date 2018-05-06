package net.hockeyapp.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.location.LocationStatusCodes;
import com.google.android.gms.plus.PlusShare;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import net.hockeyapp.android.adapters.MessagesAdapter;
import net.hockeyapp.android.objects.ErrorObject;
import net.hockeyapp.android.objects.FeedbackMessage;
import net.hockeyapp.android.objects.FeedbackResponse;
import net.hockeyapp.android.objects.FeedbackUserDataElement;
import net.hockeyapp.android.tasks.ParseFeedbackTask;
import net.hockeyapp.android.tasks.SendFeedbackTask;
import net.hockeyapp.android.utils.AsyncTaskUtils;
import net.hockeyapp.android.utils.PrefsUtil;
import net.hockeyapp.android.utils.Util;
import net.hockeyapp.android.views.AttachmentListView;
import net.hockeyapp.android.views.AttachmentView;
import net.hockeyapp.android.views.FeedbackView;

public class FeedbackActivity extends Activity implements FeedbackActivityInterface, OnClickListener {
    private static final int MAX_ATTACHMENTS_PER_MSG = 3;
    private final int ATTACH_FILE = 2;
    private final int ATTACH_PICTURE = 1;
    private final int DIALOG_ERROR_ID = 0;
    private final int PAINT_IMAGE = 3;
    private Button addAttachmentButton;
    private Button addResponseButton;
    private Context context;
    private EditText emailInput;
    private ErrorObject error;
    private Handler feedbackHandler;
    private ArrayList<FeedbackMessage> feedbackMessages;
    private ScrollView feedbackScrollView;
    private boolean feedbackViewInitialized;
    private boolean inSendFeedback;
    private List<Uri> initialAttachments;
    private TextView lastUpdatedTextView;
    private MessagesAdapter messagesAdapter;
    private ListView messagesListView;
    private EditText nameInput;
    private Handler parseFeedbackHandler;
    private ParseFeedbackTask parseFeedbackTask;
    private Button refreshButton;
    private Button sendFeedbackButton;
    private SendFeedbackTask sendFeedbackTask;
    private EditText subjectInput;
    private EditText textInput;
    private String token;
    private String url;
    private LinearLayout wrapperLayoutFeedbackAndMessages;

    class C02551 implements DialogInterface.OnClickListener {
        C02551() {
        }

        public void onClick(DialogInterface dialog, int id) {
            FeedbackActivity.this.error = null;
            dialog.cancel();
        }
    }

    class C02572 extends Handler {

        class C02561 implements Runnable {
            C02561() {
            }

            public void run() {
                FeedbackActivity.this.enableDisableSendFeedbackButton(true);
                FeedbackActivity.this.showDialog(0);
            }
        }

        C02572() {
        }

        public void handleMessage(Message msg) {
            boolean success = false;
            FeedbackActivity.this.error = new ErrorObject();
            if (msg == null || msg.getData() == null) {
                FeedbackActivity.this.error.setMessage(Strings.get(Strings.FEEDBACK_SEND_GENERIC_ERROR_ID));
            } else {
                Bundle bundle = msg.getData();
                String responseString = bundle.getString("feedback_response");
                String statusCode = bundle.getString("feedback_status");
                String requestType = bundle.getString("request_type");
                if (requestType.equals("send") && (responseString == null || Integer.parseInt(statusCode) != 201)) {
                    FeedbackActivity.this.error.setMessage(Strings.get(Strings.FEEDBACK_SEND_GENERIC_ERROR_ID));
                } else if (requestType.equals("fetch") && statusCode != null && (Integer.parseInt(statusCode) == 404 || Integer.parseInt(statusCode) == 422)) {
                    FeedbackActivity.this.resetFeedbackView();
                    success = true;
                } else if (responseString != null) {
                    FeedbackActivity.this.startParseFeedbackTask(responseString, requestType);
                    success = true;
                } else {
                    FeedbackActivity.this.error.setMessage(Strings.get(Strings.FEEDBACK_SEND_NETWORK_ERROR_ID));
                }
            }
            if (!success) {
                FeedbackActivity.this.runOnUiThread(new C02561());
            }
            FeedbackActivity.this.onSendFeedbackResult(success);
        }
    }

    class C02593 extends Handler {

        class C02581 implements Runnable {
            C02581() {
            }

            public void run() {
                FeedbackActivity.this.showDialog(0);
            }
        }

        C02593() {
        }

        public void handleMessage(Message msg) {
            boolean success = false;
            FeedbackActivity.this.error = new ErrorObject();
            if (!(msg == null || msg.getData() == null)) {
                FeedbackResponse feedbackResponse = (FeedbackResponse) msg.getData().getSerializable("parse_feedback_response");
                if (feedbackResponse != null) {
                    if (feedbackResponse.getStatus().equalsIgnoreCase("success")) {
                        success = true;
                        if (feedbackResponse.getToken() != null) {
                            PrefsUtil.getInstance().saveFeedbackTokenToPrefs(FeedbackActivity.this.context, feedbackResponse.getToken());
                            FeedbackActivity.this.loadFeedbackMessages(feedbackResponse);
                            FeedbackActivity.this.inSendFeedback = false;
                        }
                    } else {
                        success = false;
                    }
                }
            }
            if (!success) {
                FeedbackActivity.this.runOnUiThread(new C02581());
            }
            FeedbackActivity.this.enableDisableSendFeedbackButton(true);
        }
    }

    class C02615 implements Runnable {
        C02615() {
        }

        public void run() {
            PrefsUtil.getInstance().saveFeedbackTokenToPrefs(FeedbackActivity.this, null);
            PrefsUtil.applyChanges(FeedbackActivity.this.getSharedPreferences(ParseFeedbackTask.PREFERENCES_NAME, 0).edit().remove(ParseFeedbackTask.ID_LAST_MESSAGE_SEND).remove(ParseFeedbackTask.ID_LAST_MESSAGE_PROCESSED));
            FeedbackActivity.this.configureFeedbackView(false);
        }
    }

    public void enableDisableSendFeedbackButton(boolean isEnable) {
        if (this.sendFeedbackButton != null) {
            this.sendFeedbackButton.setEnabled(isEnable);
        }
    }

    public ViewGroup getLayoutView() {
        return new FeedbackView(this);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case FeedbackView.SEND_FEEDBACK_BUTTON_ID /*8201*/:
                sendFeedback();
                return;
            case FeedbackView.ADD_ATTACHMENT_BUTTON_ID /*8208*/:
                if (((ViewGroup) findViewById(FeedbackView.WRAPPER_LAYOUT_ATTACHMENTS)).getChildCount() >= 3) {
                    Toast.makeText(this, String.format("", new Object[]{Integer.valueOf(3)}), LocationStatusCodes.GEOFENCE_NOT_AVAILABLE).show();
                    return;
                }
                openContextMenu(v);
                return;
            case FeedbackView.ADD_RESPONSE_BUTTON_ID /*131088*/:
                configureFeedbackView(false);
                this.inSendFeedback = true;
                return;
            case FeedbackView.REFRESH_BUTTON_ID /*131089*/:
                sendFetchFeedback(this.url, null, null, null, null, null, PrefsUtil.getInstance().getFeedbackTokenFromPrefs(this.context), this.feedbackHandler, true);
                return;
            default:
                return;
        }
    }

    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
            case 2:
                return addAttachment(item.getItemId());
            default:
                return super.onContextItemSelected(item);
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutView());
        setTitle(Strings.get(Strings.FEEDBACK_TITLE_ID));
        this.context = this;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            this.url = extras.getString(PlusShare.KEY_CALL_TO_ACTION_URL);
            Parcelable[] initialAttachmentsArray = extras.getParcelableArray("initialAttachments");
            if (initialAttachmentsArray != null) {
                this.initialAttachments = new ArrayList();
                for (Parcelable parcelable : initialAttachmentsArray) {
                    this.initialAttachments.add((Uri) parcelable);
                }
            }
        }
        if (savedInstanceState != null) {
            this.feedbackViewInitialized = savedInstanceState.getBoolean("feedbackViewInitialized");
            this.inSendFeedback = savedInstanceState.getBoolean("inSendFeedback");
        } else {
            this.inSendFeedback = false;
            this.feedbackViewInitialized = false;
        }
        ((NotificationManager) getSystemService("notification")).cancel(2);
        initFeedbackHandler();
        initParseFeedbackHandler();
        configureAppropriateView();
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, 2, 0, Strings.get(Strings.FEEDBACK_ATTACH_FILE_ID));
        menu.add(0, 1, 0, Strings.get(Strings.FEEDBACK_ATTACH_PICTURE_ID));
    }

    protected void onStop() {
        super.onStop();
        if (this.sendFeedbackTask != null) {
            this.sendFeedbackTask.detach();
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode != 4) {
            return super.onKeyDown(keyCode, event);
        }
        if (this.inSendFeedback) {
            this.inSendFeedback = false;
            configureAppropriateView();
        } else {
            finish();
        }
        return true;
    }

    public Object onRetainNonConfigurationInstance() {
        if (this.sendFeedbackTask != null) {
            this.sendFeedbackTask.detach();
        }
        return this.sendFeedbackTask;
    }

    protected void configureFeedbackView(boolean haveToken) {
        this.feedbackScrollView = (ScrollView) findViewById(FeedbackView.FEEDBACK_SCROLLVIEW_ID);
        this.wrapperLayoutFeedbackAndMessages = (LinearLayout) findViewById(FeedbackView.WRAPPER_LAYOUT_FEEDBACK_AND_MESSAGES_ID);
        this.messagesListView = (ListView) findViewById(FeedbackView.MESSAGES_LISTVIEW_ID);
        if (haveToken) {
            this.wrapperLayoutFeedbackAndMessages.setVisibility(0);
            this.feedbackScrollView.setVisibility(8);
            this.lastUpdatedTextView = (TextView) findViewById(8192);
            this.addResponseButton = (Button) findViewById(FeedbackView.ADD_RESPONSE_BUTTON_ID);
            this.addResponseButton.setOnClickListener(this);
            this.refreshButton = (Button) findViewById(FeedbackView.REFRESH_BUTTON_ID);
            this.refreshButton.setOnClickListener(this);
            return;
        }
        this.wrapperLayoutFeedbackAndMessages.setVisibility(8);
        this.feedbackScrollView.setVisibility(0);
        this.nameInput = (EditText) findViewById(8194);
        this.emailInput = (EditText) findViewById(FeedbackView.EMAIL_EDIT_TEXT_ID);
        this.subjectInput = (EditText) findViewById(FeedbackView.SUBJECT_EDIT_TEXT_ID);
        this.textInput = (EditText) findViewById(FeedbackView.TEXT_EDIT_TEXT_ID);
        if (!this.feedbackViewInitialized) {
            String nameEmailSubject = PrefsUtil.getInstance().getNameEmailFromPrefs(this.context);
            if (nameEmailSubject != null) {
                String[] nameEmailSubjectArray = nameEmailSubject.split("\\|");
                if (nameEmailSubjectArray != null && nameEmailSubjectArray.length >= 2) {
                    this.nameInput.setText(nameEmailSubjectArray[0]);
                    this.emailInput.setText(nameEmailSubjectArray[1]);
                    if (nameEmailSubjectArray.length >= 3) {
                        this.subjectInput.setText(nameEmailSubjectArray[2]);
                        this.textInput.requestFocus();
                    } else {
                        this.subjectInput.requestFocus();
                    }
                }
            } else {
                this.nameInput.setText("");
                this.emailInput.setText("");
                this.subjectInput.setText("");
                this.nameInput.requestFocus();
            }
            this.feedbackViewInitialized = true;
        }
        this.textInput.setText("");
        if (PrefsUtil.getInstance().getFeedbackTokenFromPrefs(this.context) != null) {
            this.subjectInput.setVisibility(8);
        } else {
            this.subjectInput.setVisibility(0);
        }
        ViewGroup attachmentListView = (ViewGroup) findViewById(FeedbackView.WRAPPER_LAYOUT_ATTACHMENTS);
        attachmentListView.removeAllViews();
        if (this.initialAttachments != null) {
            for (Uri attachmentUri : this.initialAttachments) {
                attachmentListView.addView(new AttachmentView((Context) this, attachmentListView, attachmentUri, true));
            }
        }
        this.addAttachmentButton = (Button) findViewById(FeedbackView.ADD_ATTACHMENT_BUTTON_ID);
        this.addAttachmentButton.setOnClickListener(this);
        registerForContextMenu(this.addAttachmentButton);
        this.sendFeedbackButton = (Button) findViewById(FeedbackView.SEND_FEEDBACK_BUTTON_ID);
        this.sendFeedbackButton.setOnClickListener(this);
    }

    protected void onSendFeedbackResult(boolean success) {
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == -1) {
            Uri uri;
            ViewGroup attachments;
            if (requestCode == 2) {
                uri = data.getData();
                if (uri != null) {
                    attachments = (ViewGroup) findViewById(FeedbackView.WRAPPER_LAYOUT_ATTACHMENTS);
                    attachments.addView(new AttachmentView((Context) this, attachments, uri, true));
                }
            } else if (requestCode == 1) {
                uri = data.getData();
                if (uri != null) {
                    try {
                        Intent intent = new Intent(this, PaintActivity.class);
                        intent.putExtra("imageUri", uri);
                        startActivityForResult(intent, 3);
                    } catch (ActivityNotFoundException e) {
                        Log.e("HockeyApp", "Paint activity not declared!", e);
                    }
                }
            } else if (requestCode == 3) {
                uri = (Uri) data.getParcelableExtra("imageUri");
                if (uri != null) {
                    attachments = (ViewGroup) findViewById(FeedbackView.WRAPPER_LAYOUT_ATTACHMENTS);
                    attachments.addView(new AttachmentView((Context) this, attachments, uri, true));
                }
            }
        }
    }

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case 0:
                return new Builder(this).setMessage(Strings.get(Strings.DIALOG_ERROR_MESSAGE_ID)).setCancelable(false).setTitle(Strings.get(Strings.DIALOG_ERROR_TITLE_ID)).setIcon(17301543).setPositiveButton(Strings.get(2048), new C02551()).create();
            default:
                return null;
        }
    }

    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case 0:
                AlertDialog messageDialogError = (AlertDialog) dialog;
                if (this.error != null) {
                    messageDialogError.setMessage(this.error.getMessage());
                    return;
                } else {
                    messageDialogError.setMessage(Strings.get(Strings.FEEDBACK_GENERIC_ERROR_ID));
                    return;
                }
            default:
                return;
        }
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            ViewGroup attachmentList = (ViewGroup) findViewById(FeedbackView.WRAPPER_LAYOUT_ATTACHMENTS);
            Iterator it = savedInstanceState.getParcelableArrayList("attachments").iterator();
            while (it.hasNext()) {
                attachmentList.addView(new AttachmentView((Context) this, attachmentList, (Uri) it.next(), true));
            }
            this.feedbackViewInitialized = savedInstanceState.getBoolean("feedbackViewInitialized");
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("attachments", ((AttachmentListView) findViewById(FeedbackView.WRAPPER_LAYOUT_ATTACHMENTS)).getAttachments());
        outState.putBoolean("feedbackViewInitialized", this.feedbackViewInitialized);
        outState.putBoolean("inSendFeedback", this.inSendFeedback);
        super.onSaveInstanceState(outState);
    }

    private boolean addAttachment(int request) {
        Intent intent;
        if (request == 2) {
            intent = new Intent();
            intent.setType("*/*");
            intent.setAction("android.intent.action.GET_CONTENT");
            startActivityForResult(Intent.createChooser(intent, Strings.get(Strings.FEEDBACK_SELECT_FILE_ID)), 2);
            return true;
        } else if (request != 1) {
            return false;
        } else {
            intent = new Intent();
            intent.setType("image/*");
            intent.setAction("android.intent.action.GET_CONTENT");
            startActivityForResult(Intent.createChooser(intent, Strings.get(Strings.FEEDBACK_SELECT_PICTURE_ID)), 1);
            return true;
        }
    }

    private void configureAppropriateView() {
        this.token = PrefsUtil.getInstance().getFeedbackTokenFromPrefs(this);
        if (this.token == null || this.inSendFeedback) {
            configureFeedbackView(false);
            return;
        }
        configureFeedbackView(true);
        sendFetchFeedback(this.url, null, null, null, null, null, this.token, this.feedbackHandler, true);
    }

    private void createParseFeedbackTask(String feedbackResponseString, String requestType) {
        this.parseFeedbackTask = new ParseFeedbackTask(this, feedbackResponseString, this.parseFeedbackHandler, requestType);
    }

    private void hideKeyboard() {
        if (this.textInput != null) {
            ((InputMethodManager) getSystemService("input_method")).hideSoftInputFromWindow(this.textInput.getWindowToken(), 0);
        }
    }

    private void initFeedbackHandler() {
        this.feedbackHandler = new C02572();
    }

    private void initParseFeedbackHandler() {
        this.parseFeedbackHandler = new C02593();
    }

    private void loadFeedbackMessages(final FeedbackResponse feedbackResponse) {
        runOnUiThread(new Runnable() {
            public void run() {
                FeedbackActivity.this.configureFeedbackView(true);
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                SimpleDateFormat formatNew = new SimpleDateFormat("d MMM h:mm a");
                if (feedbackResponse != null && feedbackResponse.getFeedback() != null && feedbackResponse.getFeedback().getMessages() != null && feedbackResponse.getFeedback().getMessages().size() > 0) {
                    FeedbackActivity.this.feedbackMessages = feedbackResponse.getFeedback().getMessages();
                    Collections.reverse(FeedbackActivity.this.feedbackMessages);
                    try {
                        Date date = format.parse(((FeedbackMessage) FeedbackActivity.this.feedbackMessages.get(0)).getCreatedAt());
                        FeedbackActivity.this.lastUpdatedTextView.setText(String.format(Strings.get(Strings.FEEDBACK_LAST_UPDATED_TEXT_ID) + " %s", new Object[]{formatNew.format(date)}));
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }
                    if (FeedbackActivity.this.messagesAdapter == null) {
                        FeedbackActivity.this.messagesAdapter = new MessagesAdapter(FeedbackActivity.this.context, FeedbackActivity.this.feedbackMessages);
                    } else {
                        FeedbackActivity.this.messagesAdapter.clear();
                        Iterator it = FeedbackActivity.this.feedbackMessages.iterator();
                        while (it.hasNext()) {
                            FeedbackActivity.this.messagesAdapter.add((FeedbackMessage) it.next());
                        }
                        FeedbackActivity.this.messagesAdapter.notifyDataSetChanged();
                    }
                    FeedbackActivity.this.messagesListView.setAdapter(FeedbackActivity.this.messagesAdapter);
                }
            }
        });
    }

    private void resetFeedbackView() {
        runOnUiThread(new C02615());
    }

    private void sendFeedback() {
        if (Util.isConnectedToNetwork(this)) {
            enableDisableSendFeedbackButton(false);
            hideKeyboard();
            String token = PrefsUtil.getInstance().getFeedbackTokenFromPrefs(this.context);
            String name = this.nameInput.getText().toString().trim();
            String email = this.emailInput.getText().toString().trim();
            String subject = this.subjectInput.getText().toString().trim();
            String text = this.textInput.getText().toString().trim();
            if (TextUtils.isEmpty(subject)) {
                this.subjectInput.setVisibility(0);
                setError(this.subjectInput, Strings.FEEDBACK_VALIDATE_SUBJECT_ERROR_ID);
                return;
            } else if (FeedbackManager.getRequireUserName() == FeedbackUserDataElement.REQUIRED && TextUtils.isEmpty(name)) {
                setError(this.nameInput, Strings.FEEDBACK_VALIDATE_NAME_ERROR_ID);
                return;
            } else if (FeedbackManager.getRequireUserEmail() == FeedbackUserDataElement.REQUIRED && TextUtils.isEmpty(email)) {
                setError(this.emailInput, Strings.FEEDBACK_VALIDATE_EMAIL_EMPTY_ID);
                return;
            } else if (TextUtils.isEmpty(text)) {
                setError(this.textInput, Strings.FEEDBACK_VALIDATE_TEXT_ERROR_ID);
                return;
            } else if (FeedbackManager.getRequireUserEmail() != FeedbackUserDataElement.REQUIRED || Util.isValidEmail(email)) {
                PrefsUtil.getInstance().saveNameEmailSubjectToPrefs(this.context, name, email, subject);
                sendFetchFeedback(this.url, name, email, subject, text, ((AttachmentListView) findViewById(FeedbackView.WRAPPER_LAYOUT_ATTACHMENTS)).getAttachments(), token, this.feedbackHandler, false);
                return;
            } else {
                setError(this.emailInput, Strings.FEEDBACK_VALIDATE_EMAIL_ERROR_ID);
                return;
            }
        }
        Toast.makeText(this, Strings.get(Strings.ERROR_NO_NETWORK_MESSAGE_ID), 1).show();
    }

    private void setError(EditText inputField, int feedbackStringId) {
        inputField.setError(Strings.get(feedbackStringId));
        enableDisableSendFeedbackButton(true);
    }

    private void sendFetchFeedback(String url, String name, String email, String subject, String text, List<Uri> attachmentUris, String token, Handler feedbackHandler, boolean isFetchMessages) {
        this.sendFeedbackTask = new SendFeedbackTask(this.context, url, name, email, subject, text, attachmentUris, token, feedbackHandler, isFetchMessages);
        AsyncTaskUtils.execute(this.sendFeedbackTask);
    }

    private void startParseFeedbackTask(String feedbackResponseString, String requestType) {
        createParseFeedbackTask(feedbackResponseString, requestType);
        AsyncTaskUtils.execute(this.parseFeedbackTask);
    }
}
