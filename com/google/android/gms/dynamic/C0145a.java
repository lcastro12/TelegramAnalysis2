package com.google.android.gms.dynamic;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.gms.common.GooglePlayServicesUtil;
import java.util.Iterator;
import java.util.LinkedList;

public abstract class C0145a<T extends LifecycleDelegate> {
    private T cP;
    private Bundle cQ;
    private LinkedList<C0144a> cR;
    private final C0147d<T> cS = new C12901(this);

    private interface C0144a {
        void mo1040b(LifecycleDelegate lifecycleDelegate);

        int getState();
    }

    class C12901 implements C0147d<T> {
        final /* synthetic */ C0145a cT;

        C12901(C0145a c0145a) {
            this.cT = c0145a;
        }

        public void mo1039a(T t) {
            this.cT.cP = t;
            Iterator it = this.cT.cR.iterator();
            while (it.hasNext()) {
                ((C0144a) it.next()).mo1040b(this.cT.cP);
            }
            this.cT.cR.clear();
            this.cT.cQ = null;
        }
    }

    class C12946 implements C0144a {
        final /* synthetic */ C0145a cT;

        C12946(C0145a c0145a) {
            this.cT = c0145a;
        }

        public void mo1040b(LifecycleDelegate lifecycleDelegate) {
            this.cT.cP.onResume();
        }

        public int getState() {
            return 3;
        }
    }

    private void m136a(Bundle bundle, C0144a c0144a) {
        if (this.cP != null) {
            c0144a.mo1040b(this.cP);
            return;
        }
        if (this.cR == null) {
            this.cR = new LinkedList();
        }
        this.cR.add(c0144a);
        if (bundle != null) {
            if (this.cQ == null) {
                this.cQ = (Bundle) bundle.clone();
            } else {
                this.cQ.putAll(bundle);
            }
        }
        mo1349a(this.cS);
    }

    private void m138y(int i) {
        while (!this.cR.isEmpty() && ((C0144a) this.cR.getLast()).getState() >= i) {
            this.cR.removeLast();
        }
    }

    public void m139a(FrameLayout frameLayout) {
        final Context context = frameLayout.getContext();
        final int isGooglePlayServicesAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        CharSequence b = GooglePlayServicesUtil.m24b(context, isGooglePlayServicesAvailable, -1);
        CharSequence a = GooglePlayServicesUtil.m20a(context, isGooglePlayServicesAvailable);
        View linearLayout = new LinearLayout(frameLayout.getContext());
        linearLayout.setOrientation(1);
        linearLayout.setLayoutParams(new LayoutParams(-2, -2));
        frameLayout.addView(linearLayout);
        View textView = new TextView(frameLayout.getContext());
        textView.setLayoutParams(new LayoutParams(-2, -2));
        textView.setText(b);
        linearLayout.addView(textView);
        if (a != null) {
            View button = new Button(context);
            button.setLayoutParams(new LayoutParams(-2, -2));
            button.setText(a);
            linearLayout.addView(button);
            button.setOnClickListener(new OnClickListener(this) {
                final /* synthetic */ C0145a cT;

                public void onClick(View v) {
                    context.startActivity(GooglePlayServicesUtil.m19a(context, isGooglePlayServicesAvailable, -1));
                }
            });
        }
    }

    protected abstract void mo1349a(C0147d<T> c0147d);

    public T at() {
        return this.cP;
    }

    public void onCreate(final Bundle savedInstanceState) {
        m136a(savedInstanceState, new C0144a(this) {
            final /* synthetic */ C0145a cT;

            public void mo1040b(LifecycleDelegate lifecycleDelegate) {
                this.cT.cP.onCreate(savedInstanceState);
            }

            public int getState() {
                return 1;
            }
        });
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final FrameLayout frameLayout = new FrameLayout(inflater.getContext());
        final LayoutInflater layoutInflater = inflater;
        final ViewGroup viewGroup = container;
        final Bundle bundle = savedInstanceState;
        m136a(savedInstanceState, new C0144a(this) {
            final /* synthetic */ C0145a cT;

            public void mo1040b(LifecycleDelegate lifecycleDelegate) {
                frameLayout.removeAllViews();
                frameLayout.addView(this.cT.cP.onCreateView(layoutInflater, viewGroup, bundle));
            }

            public int getState() {
                return 2;
            }
        });
        if (this.cP == null) {
            m139a(frameLayout);
        }
        return frameLayout;
    }

    public void onDestroy() {
        if (this.cP != null) {
            this.cP.onDestroy();
        } else {
            m138y(1);
        }
    }

    public void onDestroyView() {
        if (this.cP != null) {
            this.cP.onDestroyView();
        } else {
            m138y(2);
        }
    }

    public void onInflate(final Activity activity, final Bundle attrs, final Bundle savedInstanceState) {
        m136a(savedInstanceState, new C0144a(this) {
            final /* synthetic */ C0145a cT;

            public void mo1040b(LifecycleDelegate lifecycleDelegate) {
                this.cT.cP.onInflate(activity, attrs, savedInstanceState);
            }

            public int getState() {
                return 0;
            }
        });
    }

    public void onLowMemory() {
        if (this.cP != null) {
            this.cP.onLowMemory();
        }
    }

    public void onPause() {
        if (this.cP != null) {
            this.cP.onPause();
        } else {
            m138y(3);
        }
    }

    public void onResume() {
        m136a(null, new C12946(this));
    }

    public void onSaveInstanceState(Bundle outState) {
        if (this.cP != null) {
            this.cP.onSaveInstanceState(outState);
        } else if (this.cQ != null) {
            outState.putAll(this.cQ);
        }
    }
}
