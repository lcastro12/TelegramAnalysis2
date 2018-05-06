package com.google.android.gms.location;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

public class DetectedActivity implements SafeParcelable {
    public static final DetectedActivityCreator CREATOR = new DetectedActivityCreator();
    public static final int IN_VEHICLE = 0;
    public static final int ON_BICYCLE = 1;
    public static final int ON_FOOT = 2;
    public static final int STILL = 3;
    public static final int TILTING = 5;
    public static final int UNKNOWN = 4;
    private final int ab;
    int fs;
    int ft;

    public DetectedActivity(int activityType, int confidence) {
        this.ab = 1;
        this.fs = activityType;
        this.ft = confidence;
    }

    public DetectedActivity(int versionCode, int activityType, int confidence) {
        this.ab = versionCode;
        this.fs = activityType;
        this.ft = confidence;
    }

    private int m1037L(int i) {
        return i > 5 ? 4 : i;
    }

    public int describeContents() {
        return 0;
    }

    public int getConfidence() {
        return this.ft;
    }

    public int getType() {
        return m1037L(this.fs);
    }

    public int m1038i() {
        return this.ab;
    }

    public String toString() {
        return "DetectedActivity [type=" + getType() + ", confidence=" + this.ft + "]";
    }

    public void writeToParcel(Parcel out, int flags) {
        DetectedActivityCreator.m533a(this, out, flags);
    }
}
