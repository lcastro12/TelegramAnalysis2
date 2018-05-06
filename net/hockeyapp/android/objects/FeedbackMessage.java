package net.hockeyapp.android.objects;

import java.io.Serializable;
import java.util.List;

public class FeedbackMessage implements Serializable {
    private static final long serialVersionUID = -8773015828853994624L;
    private String appId;
    private String cleanText;
    private String createdAt;
    private List<FeedbackAttachment> feedbackAttachments;
    private int id;
    private String model;
    private String name;
    private String oem;
    private String osVersion;
    private String subject;
    private String text;
    private String token;
    private String userString;
    private int via;

    public String getSubjec() {
        return this.subject;
    }

    public void setSubjec(String subjec) {
        this.subject = subjec;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getOem() {
        return this.oem;
    }

    public void setOem(String oem) {
        this.oem = oem;
    }

    public String getModel() {
        return this.model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getOsVersion() {
        return this.osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getToken() {
        return this.token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getVia() {
        return this.via;
    }

    public void setVia(int via) {
        this.via = via;
    }

    public String getUserString() {
        return this.userString;
    }

    public void setUserString(String userString) {
        this.userString = userString;
    }

    public String getCleanText() {
        return this.cleanText;
    }

    public void setCleanText(String cleanText) {
        this.cleanText = cleanText;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAppId() {
        return this.appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public List<FeedbackAttachment> getFeedbackAttachments() {
        return this.feedbackAttachments;
    }

    public void setFeedbackAttachments(List<FeedbackAttachment> feedbackAttachments) {
        this.feedbackAttachments = feedbackAttachments;
    }
}
