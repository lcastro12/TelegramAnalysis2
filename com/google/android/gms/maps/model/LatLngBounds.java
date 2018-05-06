package com.google.android.gms.maps.model;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.internal.C0191r;
import com.google.android.gms.internal.C0192s;
import com.google.android.gms.maps.internal.C0215q;

public final class LatLngBounds implements SafeParcelable {
    public static final LatLngBoundsCreator CREATOR = new LatLngBoundsCreator();
    private final int ab;
    public final LatLng northeast;
    public final LatLng southwest;

    public static final class Builder {
        private double hm = Double.POSITIVE_INFINITY;
        private double hn = Double.NEGATIVE_INFINITY;
        private double ho = Double.NaN;
        private double hp = Double.NaN;

        private boolean m565b(double d) {
            boolean z = false;
            if (this.ho <= this.hp) {
                return this.ho <= d && d <= this.hp;
            } else {
                if (this.ho <= d || d <= this.hp) {
                    z = true;
                }
                return z;
            }
        }

        public LatLngBounds build() {
            C0192s.m516a(!Double.isNaN(this.ho), "no included points");
            return new LatLngBounds(new LatLng(this.hm, this.ho), new LatLng(this.hn, this.hp));
        }

        public Builder include(LatLng point) {
            this.hm = Math.min(this.hm, point.latitude);
            this.hn = Math.max(this.hn, point.latitude);
            double d = point.longitude;
            if (Double.isNaN(this.ho)) {
                this.ho = d;
                this.hp = d;
            } else if (!m565b(d)) {
                if (LatLngBounds.m1090b(this.ho, d) < LatLngBounds.m1092c(this.hp, d)) {
                    this.ho = d;
                } else {
                    this.hp = d;
                }
            }
            return this;
        }
    }

    LatLngBounds(int versionCode, LatLng southwest, LatLng northeast) {
        C0192s.m518b((Object) southwest, (Object) "null southwest");
        C0192s.m518b((Object) northeast, (Object) "null northeast");
        C0192s.m517a(northeast.latitude >= southwest.latitude, "southern latitude exceeds northern latitude (%s > %s)", Double.valueOf(southwest.latitude), Double.valueOf(northeast.latitude));
        this.ab = versionCode;
        this.southwest = southwest;
        this.northeast = northeast;
    }

    public LatLngBounds(LatLng southwest, LatLng northeast) {
        this(1, southwest, northeast);
    }

    private boolean m1089a(double d) {
        return this.southwest.latitude <= d && d <= this.northeast.latitude;
    }

    private static double m1090b(double d, double d2) {
        return ((d - d2) + 360.0d) % 360.0d;
    }

    private boolean m1091b(double d) {
        boolean z = false;
        if (this.southwest.longitude <= this.northeast.longitude) {
            return this.southwest.longitude <= d && d <= this.northeast.longitude;
        } else {
            if (this.southwest.longitude <= d || d <= this.northeast.longitude) {
                z = true;
            }
            return z;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private static double m1092c(double d, double d2) {
        return ((d2 - d) + 360.0d) % 360.0d;
    }

    public boolean contains(LatLng point) {
        return m1089a(point.latitude) && m1091b(point.longitude);
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LatLngBounds)) {
            return false;
        }
        LatLngBounds latLngBounds = (LatLngBounds) o;
        return this.southwest.equals(latLngBounds.southwest) && this.northeast.equals(latLngBounds.northeast);
    }

    public LatLng getCenter() {
        double d = (this.southwest.latitude + this.northeast.latitude) / 2.0d;
        double d2 = this.northeast.longitude;
        double d3 = this.southwest.longitude;
        return new LatLng(d, d3 <= d2 ? (d2 + d3) / 2.0d : ((d2 + 360.0d) + d3) / 2.0d);
    }

    public int hashCode() {
        return C0191r.hashCode(this.southwest, this.northeast);
    }

    int m1095i() {
        return this.ab;
    }

    public LatLngBounds including(LatLng point) {
        double min = Math.min(this.southwest.latitude, point.latitude);
        double max = Math.max(this.northeast.latitude, point.latitude);
        double d = this.northeast.longitude;
        double d2 = this.southwest.longitude;
        double d3 = point.longitude;
        if (m1091b(d3)) {
            d3 = d2;
            d2 = d;
        } else if (m1090b(d2, d3) < m1092c(d, d3)) {
            d2 = d;
        } else {
            double d4 = d2;
            d2 = d3;
            d3 = d4;
        }
        return new LatLngBounds(new LatLng(min, d3), new LatLng(max, d2));
    }

    public String toString() {
        return C0191r.m514c(this).m512a("southwest", this.southwest).m512a("northeast", this.northeast).toString();
    }

    public void writeToParcel(Parcel out, int flags) {
        if (C0215q.bn()) {
            C0219d.m577a(this, out, flags);
        } else {
            LatLngBoundsCreator.m566a(this, out, flags);
        }
    }
}
