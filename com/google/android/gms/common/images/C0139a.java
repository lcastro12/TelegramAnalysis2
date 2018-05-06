package com.google.android.gms.common.images;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.gms.common.images.ImageManager.OnImageLoadedListener;
import com.google.android.gms.internal.C0174f;
import com.google.android.gms.internal.C0175g;
import com.google.android.gms.internal.C0176h;
import com.google.android.gms.internal.C0191r;
import com.google.android.gms.internal.as;
import java.lang.ref.WeakReference;
import org.telegram.messenger.support.widget.helper.ItemTouchHelper.Callback;

public final class C0139a {
    final C0138a aG;
    private int aH;
    private int aI;
    int aJ;
    private int aK;
    private WeakReference<OnImageLoadedListener> aL;
    private WeakReference<ImageView> aM;
    private WeakReference<TextView> aN;
    private int aO;
    private boolean aP;
    private boolean aQ;

    public static final class C0138a {
        public final Uri uri;

        public C0138a(Uri uri) {
            this.uri = uri;
        }

        public boolean equals(Object obj) {
            if (obj instanceof C0138a) {
                return this == obj || ((C0138a) obj).hashCode() == hashCode();
            } else {
                return false;
            }
        }

        public int hashCode() {
            return C0191r.hashCode(this.uri);
        }
    }

    public C0139a(int i) {
        this.aH = 0;
        this.aI = 0;
        this.aO = -1;
        this.aP = true;
        this.aQ = false;
        this.aG = new C0138a(null);
        this.aI = i;
    }

    public C0139a(Uri uri) {
        this.aH = 0;
        this.aI = 0;
        this.aO = -1;
        this.aP = true;
        this.aQ = false;
        this.aG = new C0138a(uri);
        this.aI = 0;
    }

    private C0174f m63a(Drawable drawable, Drawable drawable2) {
        if (drawable == null) {
            drawable = null;
        } else if (drawable instanceof C0174f) {
            drawable = ((C0174f) drawable).m459r();
        }
        return new C0174f(drawable, drawable2);
    }

    private void m64a(Drawable drawable, boolean z, boolean z2, boolean z3) {
        switch (this.aJ) {
            case 1:
                if (!z2) {
                    OnImageLoadedListener onImageLoadedListener = (OnImageLoadedListener) this.aL.get();
                    if (onImageLoadedListener != null) {
                        onImageLoadedListener.onImageLoaded(this.aG.uri, drawable);
                        return;
                    }
                    return;
                }
                return;
            case 2:
                ImageView imageView = (ImageView) this.aM.get();
                if (imageView != null) {
                    m65a(imageView, drawable, z, z2, z3);
                    return;
                }
                return;
            case 3:
                TextView textView = (TextView) this.aN.get();
                if (textView != null) {
                    m66a(textView, this.aO, drawable, z, z2);
                    return;
                }
                return;
            default:
                return;
        }
    }

    private void m65a(ImageView imageView, Drawable drawable, boolean z, boolean z2, boolean z3) {
        Object obj = (z2 || z3) ? null : 1;
        if (obj != null && (imageView instanceof C0175g)) {
            int t = ((C0175g) imageView).m462t();
            if (this.aI != 0 && t == this.aI) {
                return;
            }
        }
        boolean a = m67a(z, z2);
        Drawable a2 = a ? m63a(imageView.getDrawable(), drawable) : drawable;
        imageView.setImageDrawable(a2);
        if (imageView instanceof C0175g) {
            C0175g c0175g = (C0175g) imageView;
            c0175g.m460a(z3 ? this.aG.uri : null);
            c0175g.m461k(obj != null ? this.aI : 0);
        }
        if (a) {
            ((C0174f) a2).startTransition(Callback.DEFAULT_SWIPE_ANIMATION_DURATION);
        }
    }

    private void m66a(TextView textView, int i, Drawable drawable, boolean z, boolean z2) {
        boolean a = m67a(z, z2);
        Drawable[] compoundDrawablesRelative = as.as() ? textView.getCompoundDrawablesRelative() : textView.getCompoundDrawables();
        Drawable a2 = a ? m63a(compoundDrawablesRelative[i], drawable) : drawable;
        Drawable drawable2 = i == 0 ? a2 : compoundDrawablesRelative[0];
        Drawable drawable3 = i == 1 ? a2 : compoundDrawablesRelative[1];
        Drawable drawable4 = i == 2 ? a2 : compoundDrawablesRelative[2];
        Drawable drawable5 = i == 3 ? a2 : compoundDrawablesRelative[3];
        if (as.as()) {
            textView.setCompoundDrawablesRelativeWithIntrinsicBounds(drawable2, drawable3, drawable4, drawable5);
        } else {
            textView.setCompoundDrawablesWithIntrinsicBounds(drawable2, drawable3, drawable4, drawable5);
        }
        if (a) {
            ((C0174f) a2).startTransition(Callback.DEFAULT_SWIPE_ANIMATION_DURATION);
        }
    }

    private boolean m67a(boolean z, boolean z2) {
        return this.aP && !z2 && (!z || this.aQ);
    }

    void m68a(Context context, Bitmap bitmap, boolean z) {
        C0176h.m465b(bitmap);
        m64a(new BitmapDrawable(context.getResources(), bitmap), z, false, true);
    }

    public void m69a(ImageView imageView) {
        C0176h.m465b(imageView);
        this.aL = null;
        this.aM = new WeakReference(imageView);
        this.aN = null;
        this.aO = -1;
        this.aJ = 2;
        this.aK = imageView.hashCode();
    }

    public void m70a(OnImageLoadedListener onImageLoadedListener) {
        C0176h.m465b(onImageLoadedListener);
        this.aL = new WeakReference(onImageLoadedListener);
        this.aM = null;
        this.aN = null;
        this.aO = -1;
        this.aJ = 1;
        this.aK = C0191r.hashCode(onImageLoadedListener, this.aG);
    }

    void m71b(Context context, boolean z) {
        Drawable drawable = null;
        if (this.aI != 0) {
            drawable = context.getResources().getDrawable(this.aI);
        }
        m64a(drawable, z, false, false);
    }

    public boolean equals(Object obj) {
        if (obj instanceof C0139a) {
            return this == obj || ((C0139a) obj).hashCode() == hashCode();
        } else {
            return false;
        }
    }

    void m72f(Context context) {
        Drawable drawable = null;
        if (this.aH != 0) {
            drawable = context.getResources().getDrawable(this.aH);
        }
        m64a(drawable, false, true, false);
    }

    public int hashCode() {
        return this.aK;
    }

    public void m73j(int i) {
        this.aI = i;
    }
}
