package net.hockeyapp.android;

import net.hockeyapp.android.objects.FeedbackMessage;

public abstract class FeedbackManagerListener extends StringListener {
    public abstract boolean feedbackAnswered(FeedbackMessage feedbackMessage);

    public Class<? extends FeedbackActivity> getFeedbackActivityClass() {
        return FeedbackActivity.class;
    }
}
