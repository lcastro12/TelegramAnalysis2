package net.hockeyapp.android.views;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class FeedbackMessageView extends LinearLayout {
    public static final int ATTACHMENT_LIST_VIEW_ID = 12292;
    public static final int AUTHOR_TEXT_VIEW_ID = 12289;
    public static final int DATE_TEXT_VIEW_ID = 12290;
    public static final int MESSAGE_TEXT_VIEW_ID = 12291;
    private AttachmentListView attachmentListView;
    private TextView authorTextView;
    private TextView dateTextView;
    private TextView messageTextView;
    private boolean ownMessage;

    public FeedbackMessageView(Context context) {
        this(context, true);
    }

    public FeedbackMessageView(Context context, boolean ownMessage) {
        super(context);
        this.ownMessage = ownMessage;
        loadLayoutParams(context);
        loadAuthorLabel(context);
        loadDateLabel(context);
        loadMessageLabel(context);
        loadAttachmentList(context);
    }

    private void loadLayoutParams(Context context) {
        setOrientation(1);
        setGravity(3);
        setBackgroundColor(-3355444);
    }

    private void loadAuthorLabel(Context context) {
        this.authorTextView = new TextView(context);
        this.authorTextView.setId(12289);
        LayoutParams params = new LayoutParams(-2, -2);
        int margin = (int) TypedValue.applyDimension(1, 20.0f, getResources().getDisplayMetrics());
        params.setMargins(margin, margin, margin, 0);
        this.authorTextView.setLayoutParams(params);
        this.authorTextView.setShadowLayer(1.0f, 0.0f, 1.0f, -1);
        this.authorTextView.setSingleLine(true);
        this.authorTextView.setTextColor(-7829368);
        this.authorTextView.setTextSize(2, 15.0f);
        this.authorTextView.setTypeface(null, 0);
        addView(this.authorTextView);
    }

    public void setAuthorLabelText(String name) {
        if (this.authorTextView != null && name != null) {
            this.authorTextView.setText(name);
        }
    }

    private void setAuthorLaberColor(int color) {
        if (this.authorTextView != null) {
            this.authorTextView.setTextColor(color);
        }
    }

    private void loadDateLabel(Context context) {
        this.dateTextView = new TextView(context);
        this.dateTextView.setId(12290);
        LayoutParams params = new LayoutParams(-2, -2);
        int margin = (int) TypedValue.applyDimension(1, 20.0f, getResources().getDisplayMetrics());
        params.setMargins(margin, 0, margin, 0);
        this.dateTextView.setLayoutParams(params);
        this.dateTextView.setShadowLayer(1.0f, 0.0f, 1.0f, -1);
        this.dateTextView.setSingleLine(true);
        this.dateTextView.setTextColor(-7829368);
        this.dateTextView.setTextSize(2, 15.0f);
        this.dateTextView.setTypeface(null, 2);
        addView(this.dateTextView);
    }

    public void setDateLabelText(String text) {
        if (this.dateTextView != null && text != null) {
            this.dateTextView.setText(text);
        }
    }

    private void setDateLaberColor(int color) {
        if (this.dateTextView != null) {
            this.dateTextView.setTextColor(color);
        }
    }

    private void loadMessageLabel(Context context) {
        this.messageTextView = new TextView(context);
        this.messageTextView.setId(12291);
        LayoutParams params = new LayoutParams(-2, -2);
        int margin = (int) TypedValue.applyDimension(1, 20.0f, getResources().getDisplayMetrics());
        params.setMargins(margin, 0, margin, margin);
        this.messageTextView.setLayoutParams(params);
        this.messageTextView.setShadowLayer(1.0f, 0.0f, 1.0f, -1);
        this.messageTextView.setSingleLine(false);
        this.messageTextView.setTextColor(ViewCompat.MEASURED_STATE_MASK);
        this.messageTextView.setTextSize(2, 18.0f);
        this.messageTextView.setTypeface(null, 0);
        addView(this.messageTextView);
    }

    public void setMessageLabelText(String text) {
        if (this.messageTextView != null && text != null) {
            this.messageTextView.setText(text);
        }
    }

    private void setMessageLaberColor(int color) {
        if (this.messageTextView != null) {
            this.messageTextView.setTextColor(color);
        }
    }

    private void loadAttachmentList(Context context) {
        this.attachmentListView = new AttachmentListView(context);
        this.attachmentListView.setId(12292);
        LayoutParams params = new LayoutParams(-1, -1);
        int margin = (int) TypedValue.applyDimension(1, 20.0f, getResources().getDisplayMetrics());
        params.setMargins(margin, 0, margin, margin);
        this.attachmentListView.setLayoutParams(params);
        addView(this.attachmentListView);
    }

    public void setFeedbackMessageViewBgAndTextColor(int decisionValue) {
        if (decisionValue == 0) {
            setBackgroundColor(-3355444);
            setAuthorLaberColor(-1);
            setDateLaberColor(-1);
        } else if (decisionValue == 1) {
            setBackgroundColor(-1);
            setAuthorLaberColor(-3355444);
            setDateLaberColor(-3355444);
        }
        setMessageLaberColor(ViewCompat.MEASURED_STATE_MASK);
    }
}
