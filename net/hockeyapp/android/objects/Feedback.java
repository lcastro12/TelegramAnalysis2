package net.hockeyapp.android.objects;

import java.io.Serializable;
import java.util.ArrayList;

public class Feedback implements Serializable {
    private static final long serialVersionUID = 2590172806951065320L;
    private String createdAt;
    private String email;
    private int id;
    private ArrayList<FeedbackMessage> messages;
    private String name;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public ArrayList<FeedbackMessage> getMessages() {
        return this.messages;
    }

    public void setMessages(ArrayList<FeedbackMessage> messages) {
        this.messages = messages;
    }
}
