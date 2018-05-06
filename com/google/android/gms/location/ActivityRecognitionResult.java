package com.google.android.gms.location;

import android.content.Intent;
import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.internal.C0192s;
import java.util.Collections;
import java.util.List;

public class ActivityRecognitionResult implements SafeParcelable {
    public static final ActivityRecognitionResultCreator CREATOR = new ActivityRecognitionResultCreator();
    public static final String EXTRA_ACTIVITY_RESULT = "com.google.android.location.internal.EXTRA_ACTIVITY_RESULT";
    private final int ab;
    List<DetectedActivity> fp;
    long fq;
    long fr;

    public ActivityRecognitionResult(int versionCode, List<DetectedActivity> probableActivities, long timeMillis, long elapsedRealtimeMillis) {
        this.ab = 1;
        this.fp = probableActivities;
        this.fq = timeMillis;
        this.fr = elapsedRealtimeMillis;
    }

    public ActivityRecognitionResult(DetectedActivity mostProbableActivity, long time, long elapsedRealtimeMillis) {
        this(Collections.singletonList(mostProbableActivity), time, elapsedRealtimeMillis);
    }

    public ActivityRecognitionResult(List<DetectedActivity> probableActivities, long time, long elapsedRealtimeMillis) {
        boolean z = probableActivities != null && probableActivities.size() > 0;
        C0192s.m519b(z, (Object) "Must have at least 1 detected activity");
        this.ab = 1;
        this.fp = probableActivities;
        this.fq = time;
        this.fr = elapsedRealtimeMillis;
    }

    public static ActivityRecognitionResult extractResult(Intent intent) {
        return !hasResult(intent) ? null : (ActivityRecognitionResult) intent.getExtras().get(EXTRA_ACTIVITY_RESULT);
    }

    public static boolean hasResult(Intent intent) {
        return intent == null ? false : intent.hasExtra(EXTRA_ACTIVITY_RESULT);
    }

    public int describeContents() {
        return 0;
    }

    public int getActivityConfidence(int activityType) {
        for (DetectedActivity detectedActivity : this.fp) {
            if (detectedActivity.getType() == activityType) {
                return detectedActivity.getConfidence();
            }
        }
        return 0;
    }

    public long getElapsedRealtimeMillis() {
        return this.fr;
    }

    public DetectedActivity getMostProbableActivity() {
        return (DetectedActivity) this.fp.get(0);
    }

    public List<DetectedActivity> getProbableActivities() {
        return this.fp;
    }

    public long getTime() {
        return this.fq;
    }

    public int m1036i() {
        return this.ab;
    }

    public String toString() {
        return "ActivityRecognitionResult [probableActivities=" + this.fp + ", timeMillis=" + this.fq + ", elapsedRealtimeMillis=" + this.fr + "]";
    }

    public void writeToParcel(Parcel out, int flags) {
        ActivityRecognitionResultCreator.m532a(this, out, flags);
    }
}
