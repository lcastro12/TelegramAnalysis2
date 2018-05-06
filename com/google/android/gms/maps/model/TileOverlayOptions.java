package com.google.android.gms.maps.model;

import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.maps.internal.C0215q;
import com.google.android.gms.maps.model.internal.C0231g;
import com.google.android.gms.maps.model.internal.C0231g.C1429a;

public final class TileOverlayOptions implements SafeParcelable {
    public static final TileOverlayOptionsCreator CREATOR = new TileOverlayOptionsCreator();
    private final int ab;
    private C0231g hG;
    private TileProvider hH;
    private float hb;
    private boolean hc;

    class C14131 implements TileProvider {
        private final C0231g hI = this.hJ.hG;
        final /* synthetic */ TileOverlayOptions hJ;

        C14131(TileOverlayOptions tileOverlayOptions) {
            this.hJ = tileOverlayOptions;
        }

        public Tile getTile(int x, int y, int zoom) {
            try {
                return this.hI.getTile(x, y, zoom);
            } catch (RemoteException e) {
                return null;
            }
        }
    }

    public TileOverlayOptions() {
        this.hc = true;
        this.ab = 1;
    }

    TileOverlayOptions(int versionCode, IBinder delegate, boolean visible, float zIndex) {
        this.hc = true;
        this.ab = versionCode;
        this.hG = C1429a.m1125U(delegate);
        this.hH = this.hG == null ? null : new C14131(this);
        this.hc = visible;
        this.hb = zIndex;
    }

    IBinder bs() {
        return this.hG.asBinder();
    }

    public int describeContents() {
        return 0;
    }

    public TileProvider getTileProvider() {
        return this.hH;
    }

    public float getZIndex() {
        return this.hb;
    }

    int m1101i() {
        return this.ab;
    }

    public boolean isVisible() {
        return this.hc;
    }

    public TileOverlayOptions tileProvider(final TileProvider tileProvider) {
        this.hH = tileProvider;
        this.hG = this.hH == null ? null : new C1429a(this) {
            final /* synthetic */ TileOverlayOptions hJ;

            public Tile getTile(int x, int y, int zoom) {
                return tileProvider.getTile(x, y, zoom);
            }
        };
        return this;
    }

    public TileOverlayOptions visible(boolean visible) {
        this.hc = visible;
        return this;
    }

    public void writeToParcel(Parcel out, int flags) {
        if (C0215q.bn()) {
            C0232j.m596a(this, out, flags);
        } else {
            TileOverlayOptionsCreator.m572a(this, out, flags);
        }
    }

    public TileOverlayOptions zIndex(float zIndex) {
        this.hb = zIndex;
        return this;
    }
}
