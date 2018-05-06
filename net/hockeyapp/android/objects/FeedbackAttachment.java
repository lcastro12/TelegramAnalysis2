package net.hockeyapp.android.objects;

import java.io.File;
import java.io.FilenameFilter;
import java.io.Serializable;
import net.hockeyapp.android.Constants;

public class FeedbackAttachment implements Serializable {
    private static final long serialVersionUID = 5059651319640956830L;
    private String createdAt;
    private String filename;
    private int id;
    private int messageId;
    private String updatedAt;
    private String url;

    class C02751 implements FilenameFilter {
        C02751() {
        }

        public boolean accept(File dir, String filename) {
            return filename.equals(FeedbackAttachment.this.getCacheId());
        }
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMessageId() {
        return this.messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public String getFilename() {
        return this.filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return this.updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCacheId() {
        return "" + this.messageId + this.id;
    }

    public boolean isAvailableInCache() {
        File folder = Constants.getHockeyAppStorageDir();
        if (!folder.exists() || !folder.isDirectory()) {
            return false;
        }
        File[] match = folder.listFiles(new C02751());
        if (match == null || match.length != 1) {
            return false;
        }
        return true;
    }

    public String toString() {
        return "\n" + FeedbackAttachment.class.getSimpleName() + "\n" + "id         " + this.id + "\n" + "message id " + this.messageId + "\n" + "filename   " + this.filename + "\n" + "url        " + this.url + "\n" + "createdAt  " + this.createdAt + "\n" + "updatedAt  " + this.updatedAt;
    }
}
