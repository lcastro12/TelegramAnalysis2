package net.hockeyapp.android.objects;

import java.io.Serializable;

public class ErrorObject implements Serializable {
    private static final long serialVersionUID = 1508110658372169868L;
    private int code;
    private String message;

    public int getCode() {
        return this.code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
