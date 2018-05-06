package com.google.android.gms.maps;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.dynamic.C0145a;
import com.google.android.gms.dynamic.C0147d;
import com.google.android.gms.dynamic.C1689c;
import com.google.android.gms.dynamic.LifecycleDelegate;
import com.google.android.gms.internal.C0192s;
import com.google.android.gms.maps.internal.C0214p;
import com.google.android.gms.maps.internal.IMapViewDelegate;
import com.google.android.gms.maps.model.RuntimeRemoteException;

public class MapView extends FrameLayout {
    private final C1370b gD;
    private GoogleMap gz;

    static class C1369a implements LifecycleDelegate {
        private final ViewGroup gE;
        private final IMapViewDelegate gF;
        private View gG;

        public C1369a(ViewGroup viewGroup, IMapViewDelegate iMapViewDelegate) {
            this.gF = (IMapViewDelegate) C0192s.m521d(iMapViewDelegate);
            this.gE = (ViewGroup) C0192s.m521d(viewGroup);
        }

        public IMapViewDelegate bj() {
            return this.gF;
        }

        public void onCreate(Bundle savedInstanceState) {
            try {
                this.gF.onCreate(savedInstanceState);
                this.gG = (View) C1689c.m1134a(this.gF.getView());
                this.gE.removeAllViews();
                this.gE.addView(this.gG);
            } catch (RemoteException e) {
                throw new RuntimeRemoteException(e);
            }
        }

        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            throw new UnsupportedOperationException("onCreateView not allowed on MapViewDelegate");
        }

        public void onDestroy() {
            try {
                this.gF.onDestroy();
            } catch (RemoteException e) {
                throw new RuntimeRemoteException(e);
            }
        }

        public void onDestroyView() {
            throw new UnsupportedOperationException("onDestroyView not allowed on MapViewDelegate");
        }

        public void onInflate(Activity activity, Bundle attrs, Bundle savedInstanceState) {
            throw new UnsupportedOperationException("onInflate not allowed on MapViewDelegate");
        }

        public void onLowMemory() {
            try {
                this.gF.onLowMemory();
            } catch (RemoteException e) {
                throw new RuntimeRemoteException(e);
            }
        }

        public void onPause() {
            try {
                this.gF.onPause();
            } catch (RemoteException e) {
                throw new RuntimeRemoteException(e);
            }
        }

        public void onResume() {
            try {
                this.gF.onResume();
            } catch (RemoteException e) {
                throw new RuntimeRemoteException(e);
            }
        }

        public void onSaveInstanceState(Bundle outState) {
            try {
                this.gF.onSaveInstanceState(outState);
            } catch (RemoteException e) {
                throw new RuntimeRemoteException(e);
            }
        }
    }

    static class C1370b extends C0145a<C1369a> {
        protected C0147d<C1369a> gC;
        private final ViewGroup gH;
        private final GoogleMapOptions gI;
        private final Context mContext;

        C1370b(ViewGroup viewGroup, Context context, GoogleMapOptions googleMapOptions) {
            this.gH = viewGroup;
            this.mContext = context;
            this.gI = googleMapOptions;
        }

        protected void mo1349a(C0147d<C1369a> c0147d) {
            this.gC = c0147d;
            bi();
        }

        public void bi() {
            if (this.gC != null && at() == null) {
                try {
                    this.gC.mo1039a(new C1369a(this.gH, C0214p.m557i(this.mContext).mo1447a(C1689c.m1135f(this.mContext), this.gI)));
                } catch (RemoteException e) {
                    throw new RuntimeRemoteException(e);
                } catch (GooglePlayServicesNotAvailableException e2) {
                }
            }
        }
    }

    public MapView(Context context) {
        super(context);
        this.gD = new C1370b(this, context, null);
    }

    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.gD = new C1370b(this, context, GoogleMapOptions.createFromAttributes(context, attrs));
    }

    public MapView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.gD = new C1370b(this, context, GoogleMapOptions.createFromAttributes(context, attrs));
    }

    public MapView(Context context, GoogleMapOptions options) {
        super(context);
        this.gD = new C1370b(this, context, options);
    }

    public final GoogleMap getMap() {
        if (this.gz != null) {
            return this.gz;
        }
        this.gD.bi();
        if (this.gD.at() == null) {
            return null;
        }
        try {
            this.gz = new GoogleMap(((C1369a) this.gD.at()).bj().getMap());
            return this.gz;
        } catch (RemoteException e) {
            throw new RuntimeRemoteException(e);
        }
    }

    public final void onCreate(Bundle savedInstanceState) {
        this.gD.onCreate(savedInstanceState);
        if (this.gD.at() == null) {
            this.gD.m139a((FrameLayout) this);
        }
    }

    public final void onDestroy() {
        this.gD.onDestroy();
    }

    public final void onLowMemory() {
        this.gD.onLowMemory();
    }

    public final void onPause() {
        this.gD.onPause();
    }

    public final void onResume() {
        this.gD.onResume();
    }

    public final void onSaveInstanceState(Bundle outState) {
        this.gD.onSaveInstanceState(outState);
    }
}
