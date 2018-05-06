package net.hockeyapp.android.objects;

public enum FeedbackUserDataElement {
    DONT_SHOW(0),
    OPTIONAL(1),
    REQUIRED(2);
    
    private final int value;

    private FeedbackUserDataElement(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}
