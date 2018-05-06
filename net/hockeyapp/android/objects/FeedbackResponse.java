package net.hockeyapp.android.objects;

import java.io.Serializable;

public class FeedbackResponse implements Serializable {
    private static final long serialVersionUID = -1093570359639034766L;
    private Feedback feedback;
    private String status;
    private String token;

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Feedback getFeedback() {
        return this.feedback;
    }

    public void setFeedback(Feedback feedback) {
        this.feedback = feedback;
    }

    public String getToken() {
        return this.token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
