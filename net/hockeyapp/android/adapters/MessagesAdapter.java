package net.hockeyapp.android.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import net.hockeyapp.android.objects.FeedbackAttachment;
import net.hockeyapp.android.objects.FeedbackMessage;
import net.hockeyapp.android.tasks.AttachmentDownloader;
import net.hockeyapp.android.views.AttachmentListView;
import net.hockeyapp.android.views.AttachmentView;
import net.hockeyapp.android.views.FeedbackMessageView;

public class MessagesAdapter extends BaseAdapter {
    private AttachmentListView attachmentListView;
    private TextView authorTextView;
    private Context context;
    private Date date;
    private TextView dateTextView;
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private SimpleDateFormat formatNew = new SimpleDateFormat("d MMM h:mm a");
    private TextView messageTextView;
    private ArrayList<FeedbackMessage> messagesList;

    public MessagesAdapter(Context context, ArrayList<FeedbackMessage> messagesList) {
        this.context = context;
        this.messagesList = messagesList;
    }

    public int getCount() {
        return this.messagesList.size();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        FeedbackMessageView view;
        FeedbackMessage feedbackMessage = (FeedbackMessage) this.messagesList.get(position);
        if (convertView == null) {
            view = new FeedbackMessageView(this.context);
        } else {
            view = (FeedbackMessageView) convertView;
        }
        if (feedbackMessage != null) {
            this.authorTextView = (TextView) view.findViewById(12289);
            this.dateTextView = (TextView) view.findViewById(12290);
            this.messageTextView = (TextView) view.findViewById(12291);
            this.attachmentListView = (AttachmentListView) view.findViewById(12292);
            try {
                this.date = this.format.parse(feedbackMessage.getCreatedAt());
                this.dateTextView.setText(this.formatNew.format(this.date));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            this.authorTextView.setText(feedbackMessage.getName());
            this.messageTextView.setText(feedbackMessage.getText());
            this.attachmentListView.removeAllViews();
            for (FeedbackAttachment feedbackAttachment : feedbackMessage.getFeedbackAttachments()) {
                AttachmentView attachmentView = new AttachmentView(this.context, this.attachmentListView, feedbackAttachment, false);
                AttachmentDownloader.getInstance().download(feedbackAttachment, attachmentView);
                this.attachmentListView.addView(attachmentView);
            }
        }
        view.setFeedbackMessageViewBgAndTextColor(position % 2 == 0 ? 0 : 1);
        return view;
    }

    public Object getItem(int position) {
        return this.messagesList.get(position);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public void clear() {
        if (this.messagesList != null) {
            this.messagesList.clear();
        }
    }

    public void add(FeedbackMessage message) {
        if (message != null && this.messagesList != null) {
            this.messagesList.add(message);
        }
    }
}
