package com.google.android.gms.maps;

import android.app.Activity;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.dynamic.C0145a;
import com.google.android.gms.dynamic.C0147d;
import com.google.android.gms.dynamic.C1689c;
import com.google.android.gms.dynamic.LifecycleDelegate;
import com.google.android.gms.internal.C0192s;
import com.google.android.gms.maps.internal.C0213o;
import com.google.android.gms.maps.internal.C0214p;
import com.google.android.gms.maps.internal.IGoogleMapDelegate;
import com.google.android.gms.maps.internal.IMapFragmentDelegate;
import com.google.android.gms.maps.model.RuntimeRemoteException;

public class SupportMapFragment extends Fragment {
    private final C1372b gK = new C1372b(this);
    private GoogleMap gz;

    static class C1371a implements LifecycleDelegate {
        private final IMapFragmentDelegate gB;
        private final Fragment gL;

        public C1371a(Fragment fragment, IMapFragmentDelegate iMapFragmentDelegate) {
            this.gB = (IMapFragmentDelegate) C0192s.m521d(iMapFragmentDelegate);
            this.gL = (Fragment) C0192s.m521d(fragment);
        }

        public IMapFragmentDelegate bh() {
            return this.gB;
        }

        public void onCreate(Bundle savedInstanceState) {
            if (savedInstanceState == null) {
                try {
                    savedInstanceState = new Bundle();
                } catch (RemoteException e) {
                    throw new RuntimeRemoteException(e);
                }
            }
            Bundle arguments = this.gL.getArguments();
            if (arguments != null && arguments.containsKey("MapOptions")) {
                C0213o.m554a(savedInstanceState, "MapOptions", arguments.getParcelable("MapOptions"));
            }
            this.gB.onCreate(savedInstanceState);
        }

        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            try {
                return (View) C1689c.m1134a(this.gB.onCreateView(C1689c.m1135f(inflater), C1689c.m1135f(container), savedInstanceState));
            } catch (RemoteException e) {
                throw new RuntimeRemoteException(e);
            }
        }

        public void onDestroy() {
            try {
                this.gB.onDestroy();
            } catch (RemoteException e) {
                throw new RuntimeRemoteException(e);
            }
        }

        public void onDestroyView() {
            try {
                this.gB.onDestroyView();
            } catch (RemoteException e) {
                throw new RuntimeRemoteException(e);
            }
        }

        public void onInflate(Activity activity, Bundle attrs, Bundle savedInstanceState) {
            try {
                this.gB.onInflate(C1689c.m1135f(activity), (GoogleMapOptions) attrs.getParcelable("MapOptions"), savedInstanceState);
            } catch (RemoteException e) {
                throw new RuntimeRemoteException(e);
            }
        }

        public void onLowMemory() {
            try {
                this.gB.onLowMemory();
            } catch (RemoteException e) {
                throw new RuntimeRemoteException(e);
            }
        }

        public void onPause() {
            try {
                this.gB.onPause();
            } catch (RemoteException e) {
                throw new RuntimeRemoteException(e);
            }
        }

        public void onResume() {
            try {
                this.gB.onResume();
            } catch (RemoteException e) {
                throw new RuntimeRemoteException(e);
            }
        }

        public void onSaveInstanceState(Bundle outState) {
            try {
                this.gB.onSaveInstanceState(outState);
            } catch (RemoteException e) {
                throw new RuntimeRemoteException(e);
            }
        }
    }

    static class C1372b extends C0145a<C1371a> {
        private Activity bm;
        protected C0147d<C1371a> gC;
        private final Fragment gL;

        C1372b(Fragment fragment) {
            this.gL = fragment;
        }

        private void setActivity(Activity activity) {
            this.bm = activity;
            bi();
        }

        protected void mo1349a(C0147d<C1371a> c0147d) {
            this.gC = c0147d;
            bi();
        }

        public void bi() {
            if (this.bm != null && this.gC != null && at() == null) {
                try {
                    MapsInitializer.initialize(this.bm);
                    this.gC.mo1039a(new C1371a(this.gL, C0214p.m557i(this.bm).mo1452d(C1689c.m1135f(this.bm))));
                } catch (RemoteException e) {
                    throw new RuntimeRemoteException(e);
                } catch (GooglePlayServicesNotAvailableException e2) {
                }
            }
        }
    }

    public static SupportMapFragment newInstance() {
        return new SupportMapFragment();
    }

    public static SupportMapFragment newInstance(GoogleMapOptions options) {
        SupportMapFragment supportMapFragment = new SupportMapFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("MapOptions", options);
        supportMapFragment.setArguments(bundle);
        return supportMapFragment;
    }

    protected IMapFragmentDelegate bh() {
        this.gK.bi();
        return this.gK.at() == null ? null : ((C1371a) this.gK.at()).bh();
    }

    public final GoogleMap getMap() {
        IMapFragmentDelegate bh = bh();
        if (bh == null) {
            return null;
        }
        try {
            IGoogleMapDelegate map = bh.getMap();
            if (map == null) {
                return null;
            }
            if (this.gz == null || this.gz.aY().asBinder() != map.asBinder()) {
                this.gz = new GoogleMap(map);
            }
            return this.gz;
        } catch (RemoteException e) {
            throw new RuntimeRemoteException(e);
        }
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            savedInstanceState.setClassLoader(SupportMapFragment.class.getClassLoader());
        }
        super.onActivityCreated(savedInstanceState);
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.gK.setActivity(activity);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.gK.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return this.gK.onCreateView(inflater, container, savedInstanceState);
    }

    public void onDestroy() {
        this.gK.onDestroy();
        super.onDestroy();
    }

    public void onDestroyView() {
        this.gK.onDestroyView();
        super.onDestroyView();
    }

    public void onInflate(Activity activity, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(activity, attrs, savedInstanceState);
        this.gK.setActivity(activity);
        Parcelable createFromAttributes = GoogleMapOptions.createFromAttributes(activity, attrs);
        Bundle bundle = new Bundle();
        bundle.putParcelable("MapOptions", createFromAttributes);
        this.gK.onInflate(activity, bundle, savedInstanceState);
    }

    public void onLowMemory() {
        this.gK.onLowMemory();
        super.onLowMemory();
    }

    public void onPause() {
        this.gK.onPause();
        super.onPause();
    }

    public void onResume() {
        super.onResume();
        this.gK.onResume();
    }

    public void onSaveInstanceState(Bundle outState) {
        if (outState != null) {
            outState.setClassLoader(SupportMapFragment.class.getClassLoader());
        }
        super.onSaveInstanceState(outState);
        this.gK.onSaveInstanceState(outState);
    }

    public void setArguments(Bundle args) {
        super.setArguments(args);
    }
}
