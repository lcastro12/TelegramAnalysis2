package com.google.android.gms.location;

import android.os.Parcel;
import android.os.SystemClock;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.internal.C0191r;
import org.telegram.tgnet.ConnectionsManager;

public final class LocationRequest implements SafeParcelable {
    public static final LocationRequestCreator CREATOR = new LocationRequestCreator();
    public static final int PRIORITY_BALANCED_POWER_ACCURACY = 102;
    public static final int PRIORITY_HIGH_ACCURACY = 100;
    public static final int PRIORITY_LOW_POWER = 104;
    public static final int PRIORITY_NO_POWER = 105;
    private final int ab;
    long fB;
    long fC;
    boolean fD;
    int fE;
    float fF;
    long fw;
    int mPriority;

    public LocationRequest() {
        this.ab = 1;
        this.mPriority = PRIORITY_BALANCED_POWER_ACCURACY;
        this.fB = 3600000;
        this.fC = 600000;
        this.fD = false;
        this.fw = Long.MAX_VALUE;
        this.fE = ConnectionsManager.DEFAULT_DATACENTER_ID;
        this.fF = 0.0f;
    }

    LocationRequest(int versionCode, int priority, long interval, long fastestInterval, boolean explicitFastestInterval, long expireAt, int numUpdates, float smallestDisplacement) {
        this.ab = versionCode;
        this.mPriority = priority;
        this.fB = interval;
        this.fC = fastestInterval;
        this.fD = explicitFastestInterval;
        this.fw = expireAt;
        this.fE = numUpdates;
        this.fF = smallestDisplacement;
    }

    private static void m1039M(int i) {
        switch (i) {
            case PRIORITY_HIGH_ACCURACY /*100*/:
            case PRIORITY_BALANCED_POWER_ACCURACY /*102*/:
            case PRIORITY_LOW_POWER /*104*/:
            case PRIORITY_NO_POWER /*105*/:
                return;
            default:
                throw new IllegalArgumentException("invalid quality: " + i);
        }
    }

    public static String m1040N(int i) {
        switch (i) {
            case PRIORITY_HIGH_ACCURACY /*100*/:
                return "PRIORITY_HIGH_ACCURACY";
            case PRIORITY_BALANCED_POWER_ACCURACY /*102*/:
                return "PRIORITY_BALANCED_POWER_ACCURACY";
            case PRIORITY_LOW_POWER /*104*/:
                return "PRIORITY_LOW_POWER";
            case PRIORITY_NO_POWER /*105*/:
                return "PRIORITY_NO_POWER";
            default:
                return "???";
        }
    }

    private static void m1041a(float f) {
        if (f < 0.0f) {
            throw new IllegalArgumentException("invalid displacement: " + f);
        }
    }

    private static void m1042c(long j) {
        if (j < 0) {
            throw new IllegalArgumentException("invalid interval: " + j);
        }
    }

    public static LocationRequest create() {
        return new LocationRequest();
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof LocationRequest)) {
            return false;
        }
        LocationRequest locationRequest = (LocationRequest) object;
        return this.mPriority == locationRequest.mPriority && this.fB == locationRequest.fB && this.fC == locationRequest.fC && this.fD == locationRequest.fD && this.fw == locationRequest.fw && this.fE == locationRequest.fE && this.fF == locationRequest.fF;
    }

    public long getExpirationTime() {
        return this.fw;
    }

    public long getFastestInterval() {
        return this.fC;
    }

    public long getInterval() {
        return this.fB;
    }

    public int getNumUpdates() {
        return this.fE;
    }

    public int getPriority() {
        return this.mPriority;
    }

    public float getSmallestDisplacement() {
        return this.fF;
    }

    public int hashCode() {
        return C0191r.hashCode(Integer.valueOf(this.mPriority), Long.valueOf(this.fB), Long.valueOf(this.fC), Boolean.valueOf(this.fD), Long.valueOf(this.fw), Integer.valueOf(this.fE), Float.valueOf(this.fF));
    }

    int m1043i() {
        return this.ab;
    }

    public LocationRequest setExpirationDuration(long millis) {
        long elapsedRealtime = SystemClock.elapsedRealtime();
        if (millis > Long.MAX_VALUE - elapsedRealtime) {
            this.fw = Long.MAX_VALUE;
        } else {
            this.fw = elapsedRealtime + millis;
        }
        if (this.fw < 0) {
            this.fw = 0;
        }
        return this;
    }

    public LocationRequest setExpirationTime(long millis) {
        this.fw = millis;
        if (this.fw < 0) {
            this.fw = 0;
        }
        return this;
    }

    public LocationRequest setFastestInterval(long millis) {
        m1042c(millis);
        this.fD = true;
        this.fC = millis;
        return this;
    }

    public LocationRequest setInterval(long millis) {
        m1042c(millis);
        this.fB = millis;
        if (!this.fD) {
            this.fC = (long) (((double) this.fB) / 6.0d);
        }
        return this;
    }

    public LocationRequest setNumUpdates(int numUpdates) {
        if (numUpdates <= 0) {
            throw new IllegalArgumentException("invalid numUpdates: " + numUpdates);
        }
        this.fE = numUpdates;
        return this;
    }

    public LocationRequest setPriority(int priority) {
        m1039M(priority);
        this.mPriority = priority;
        return this;
    }

    public LocationRequest setSmallestDisplacement(float smallestDisplacementMeters) {
        m1041a(smallestDisplacementMeters);
        this.fF = smallestDisplacementMeters;
        return this;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Request[").append(m1040N(this.mPriority));
        if (this.mPriority != PRIORITY_NO_POWER) {
            stringBuilder.append(" requested=");
            stringBuilder.append(this.fB + "ms");
        }
        stringBuilder.append(" fastest=");
        stringBuilder.append(this.fC + "ms");
        if (this.fw != Long.MAX_VALUE) {
            long elapsedRealtime = this.fw - SystemClock.elapsedRealtime();
            stringBuilder.append(" expireIn=");
            stringBuilder.append(elapsedRealtime + "ms");
        }
        if (this.fE != ConnectionsManager.DEFAULT_DATACENTER_ID) {
            stringBuilder.append(" num=").append(this.fE);
        }
        stringBuilder.append(']');
        return stringBuilder.toString();
    }

    public void writeToParcel(Parcel parcel, int flags) {
        LocationRequestCreator.m534a(this, parcel, flags);
    }
}
