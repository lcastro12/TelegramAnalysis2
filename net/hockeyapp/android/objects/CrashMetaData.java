package net.hockeyapp.android.objects;

public class CrashMetaData {
    private String userDescription;
    private String userEmail;
    private String userID;

    public String getUserDescription() {
        return this.userDescription;
    }

    public void setUserDescription(String userDescription) {
        this.userDescription = userDescription;
    }

    public String getUserEmail() {
        return this.userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserID() {
        return this.userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String toString() {
        return "\n" + CrashMetaData.class.getSimpleName() + "\n" + "userDescription " + this.userDescription + "\n" + "userEmail       " + this.userEmail + "\n" + "userID          " + this.userID;
    }
}
