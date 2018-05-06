package com.google.android.gms.maps.model;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.maps.internal.C0215q;

public final class Tile implements SafeParcelable {
    public static final TileCreator CREATOR = new TileCreator();
    private final int ab;
    public final byte[] data;
    public final int height;
    public final int width;

    Tile(int versionCode, int width, int height, byte[] data) {
        this.ab = versionCode;
        this.width = width;
        this.height = height;
        this.data = data;
    }

    public Tile(int width, int height, byte[] data) {
        this(1, width, height, data);
    }

    public int describeContents() {
        return 0;
    }

    int m1099i() {
        return this.ab;
    }

    public void writeToParcel(Parcel out, int flags) {
        if (C0215q.bn()) {
            C0224i.m582a(this, out, flags);
        } else {
            TileCreator.m571a(this, out, flags);
        }
    }
}
