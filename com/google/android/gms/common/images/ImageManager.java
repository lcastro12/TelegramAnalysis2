package com.google.android.gms.common.images;

import android.app.ActivityManager;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.ImageView;
import com.google.android.gms.common.images.C0139a.C0138a;
import com.google.android.gms.internal.C0176h;
import com.google.android.gms.internal.C0195w;
import com.google.android.gms.internal.as;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ImageManager {
    private static final Object aq = new Object();
    private static HashSet<Uri> ar = new HashSet();
    private static ImageManager as;
    private static ImageManager at;
    private final ExecutorService au = Executors.newFixedThreadPool(4);
    private final C1289b av;
    private final Map<C0139a, ImageReceiver> aw;
    private final Map<Uri, ImageReceiver> ax;
    private final Context mContext;
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private final class ImageReceiver extends ResultReceiver {
        final /* synthetic */ ImageManager aA;
        private final ArrayList<C0139a> ay;
        boolean az = false;
        private final Uri mUri;

        ImageReceiver(ImageManager imageManager, Uri uri) {
            this.aA = imageManager;
            super(new Handler(Looper.getMainLooper()));
            this.mUri = uri;
            this.ay = new ArrayList();
        }

        public void m43c(C0139a c0139a) {
            C0176h.m464a(!this.az, "Cannot add an ImageRequest when mHandlingRequests is true");
            C0176h.m466f("ImageReceiver.addImageRequest() must be called in the main thread");
            this.ay.add(c0139a);
        }

        public void m44d(C0139a c0139a) {
            C0176h.m464a(!this.az, "Cannot remove an ImageRequest when mHandlingRequests is true");
            C0176h.m466f("ImageReceiver.removeImageRequest() must be called in the main thread");
            this.ay.remove(c0139a);
        }

        public void onReceiveResult(int resultCode, Bundle resultData) {
            this.aA.au.execute(new C0134c(this.aA, this.mUri, (ParcelFileDescriptor) resultData.getParcelable("com.google.android.gms.extra.fileDescriptor")));
        }

        public void m45q() {
            Intent intent = new Intent("com.google.android.gms.common.images.LOAD_IMAGE");
            intent.putExtra("com.google.android.gms.extras.uri", this.mUri);
            intent.putExtra("com.google.android.gms.extras.resultReceiver", this);
            intent.putExtra("com.google.android.gms.extras.priority", 3);
            this.aA.mContext.sendBroadcast(intent);
        }
    }

    public interface OnImageLoadedListener {
        void onImageLoaded(Uri uri, Drawable drawable);
    }

    private static final class C0133a {
        static int m46a(ActivityManager activityManager) {
            return activityManager.getLargeMemoryClass();
        }
    }

    private final class C0134c implements Runnable {
        final /* synthetic */ ImageManager aA;
        private final ParcelFileDescriptor aB;
        private final Uri mUri;

        public C0134c(ImageManager imageManager, Uri uri, ParcelFileDescriptor parcelFileDescriptor) {
            this.aA = imageManager;
            this.mUri = uri;
            this.aB = parcelFileDescriptor;
        }

        public void run() {
            C0176h.m467g("LoadBitmapFromDiskRunnable can't be executed in the main thread");
            boolean z = false;
            Bitmap bitmap = null;
            if (this.aB != null) {
                try {
                    bitmap = BitmapFactory.decodeFileDescriptor(this.aB.getFileDescriptor());
                } catch (Throwable e) {
                    Log.e("ImageManager", "OOM while loading bitmap for uri: " + this.mUri, e);
                    z = true;
                }
                try {
                    this.aB.close();
                } catch (Throwable e2) {
                    Log.e("ImageManager", "closed failed", e2);
                }
            }
            CountDownLatch countDownLatch = new CountDownLatch(1);
            this.aA.mHandler.post(new C0137f(this.aA, this.mUri, bitmap, z, countDownLatch));
            try {
                countDownLatch.await();
            } catch (InterruptedException e3) {
                Log.w("ImageManager", "Latch interrupted while posting " + this.mUri);
            }
        }
    }

    private final class C0135d implements Runnable {
        final /* synthetic */ ImageManager aA;
        private final C0139a aC;

        public C0135d(ImageManager imageManager, C0139a c0139a) {
            this.aA = imageManager;
            this.aC = c0139a;
        }

        public void run() {
            C0176h.m466f("LoadImageRunnable must be executed on the main thread");
            this.aA.m54b(this.aC);
            C0138a c0138a = this.aC.aG;
            if (c0138a.uri == null) {
                this.aC.m71b(this.aA.mContext, true);
                return;
            }
            Bitmap a = this.aA.m50a(c0138a);
            if (a != null) {
                this.aC.m68a(this.aA.mContext, a, true);
                return;
            }
            this.aC.m72f(this.aA.mContext);
            ImageReceiver imageReceiver = (ImageReceiver) this.aA.ax.get(c0138a.uri);
            if (imageReceiver == null) {
                imageReceiver = new ImageReceiver(this.aA, c0138a.uri);
                this.aA.ax.put(c0138a.uri, imageReceiver);
            }
            imageReceiver.m43c(this.aC);
            if (this.aC.aJ != 1) {
                this.aA.aw.put(this.aC, imageReceiver);
            }
            synchronized (ImageManager.aq) {
                if (!ImageManager.ar.contains(c0138a.uri)) {
                    ImageManager.ar.add(c0138a.uri);
                    imageReceiver.m45q();
                }
            }
        }
    }

    private static final class C0136e implements ComponentCallbacks2 {
        private final C1289b av;

        public C0136e(C1289b c1289b) {
            this.av = c1289b;
        }

        public void onConfigurationChanged(Configuration newConfig) {
        }

        public void onLowMemory() {
            this.av.evictAll();
        }

        public void onTrimMemory(int level) {
            if (level >= 60) {
                this.av.evictAll();
            } else if (level >= 20) {
                this.av.trimToSize(this.av.size() / 2);
            }
        }
    }

    private final class C0137f implements Runnable {
        final /* synthetic */ ImageManager aA;
        private final Bitmap aD;
        private final CountDownLatch aE;
        private boolean aF;
        private final Uri mUri;

        public C0137f(ImageManager imageManager, Uri uri, Bitmap bitmap, boolean z, CountDownLatch countDownLatch) {
            this.aA = imageManager;
            this.mUri = uri;
            this.aD = bitmap;
            this.aF = z;
            this.aE = countDownLatch;
        }

        private void m47a(ImageReceiver imageReceiver, boolean z) {
            imageReceiver.az = true;
            ArrayList a = imageReceiver.ay;
            int size = a.size();
            for (int i = 0; i < size; i++) {
                C0139a c0139a = (C0139a) a.get(i);
                if (z) {
                    c0139a.m68a(this.aA.mContext, this.aD, false);
                } else {
                    c0139a.m71b(this.aA.mContext, false);
                }
                if (c0139a.aJ != 1) {
                    this.aA.aw.remove(c0139a);
                }
            }
            imageReceiver.az = false;
        }

        public void run() {
            C0176h.m466f("OnBitmapLoadedRunnable must be executed in the main thread");
            boolean z = this.aD != null;
            if (this.aA.av != null) {
                if (this.aF) {
                    this.aA.av.evictAll();
                    System.gc();
                    this.aF = false;
                    this.aA.mHandler.post(this);
                    return;
                } else if (z) {
                    this.aA.av.put(new C0138a(this.mUri), this.aD);
                }
            }
            ImageReceiver imageReceiver = (ImageReceiver) this.aA.ax.remove(this.mUri);
            if (imageReceiver != null) {
                m47a(imageReceiver, z);
            }
            this.aE.countDown();
            synchronized (ImageManager.aq) {
                ImageManager.ar.remove(this.mUri);
            }
        }
    }

    private static final class C1289b extends C0195w<C0138a, Bitmap> {
        public C1289b(Context context) {
            super(C1289b.m644e(context));
        }

        private static int m644e(Context context) {
            ActivityManager activityManager = (ActivityManager) context.getSystemService("activity");
            int memoryClass = (((context.getApplicationInfo().flags & 1048576) != 0 ? 1 : null) == null || !as.an()) ? activityManager.getMemoryClass() : C0133a.m46a(activityManager);
            return (int) (((float) (memoryClass * 1048576)) * 0.33f);
        }

        protected int m645a(C0138a c0138a, Bitmap bitmap) {
            return bitmap.getHeight() * bitmap.getRowBytes();
        }

        protected void m646a(boolean z, C0138a c0138a, Bitmap bitmap, Bitmap bitmap2) {
            super.entryRemoved(z, c0138a, bitmap, bitmap2);
        }

        protected /* synthetic */ void entryRemoved(boolean x0, Object x1, Object x2, Object x3) {
            m646a(x0, (C0138a) x1, (Bitmap) x2, (Bitmap) x3);
        }

        protected /* synthetic */ int sizeOf(Object x0, Object x1) {
            return m645a((C0138a) x0, (Bitmap) x1);
        }
    }

    private ImageManager(Context context, boolean withMemoryCache) {
        this.mContext = context.getApplicationContext();
        if (withMemoryCache) {
            this.av = new C1289b(this.mContext);
            if (as.aq()) {
                m59n();
            }
        } else {
            this.av = null;
        }
        this.aw = new HashMap();
        this.ax = new HashMap();
    }

    private Bitmap m50a(C0138a c0138a) {
        return this.av == null ? null : (Bitmap) this.av.get(c0138a);
    }

    public static ImageManager m51a(Context context, boolean z) {
        if (z) {
            if (at == null) {
                at = new ImageManager(context, true);
            }
            return at;
        }
        if (as == null) {
            as = new ImageManager(context, false);
        }
        return as;
    }

    private boolean m54b(C0139a c0139a) {
        C0176h.m466f("ImageManager.cleanupHashMaps() must be called in the main thread");
        if (c0139a.aJ == 1) {
            return true;
        }
        ImageReceiver imageReceiver = (ImageReceiver) this.aw.get(c0139a);
        if (imageReceiver == null) {
            return true;
        }
        if (imageReceiver.az) {
            return false;
        }
        this.aw.remove(c0139a);
        imageReceiver.m44d(c0139a);
        return true;
    }

    public static ImageManager create(Context context) {
        return m51a(context, false);
    }

    private void m59n() {
        this.mContext.registerComponentCallbacks(new C0136e(this.av));
    }

    public void m62a(C0139a c0139a) {
        C0176h.m466f("ImageManager.loadImage() must be called in the main thread");
        boolean b = m54b(c0139a);
        Runnable c0135d = new C0135d(this, c0139a);
        if (b) {
            c0135d.run();
        } else {
            this.mHandler.post(c0135d);
        }
    }

    public void loadImage(ImageView imageView, int resId) {
        C0139a c0139a = new C0139a(resId);
        c0139a.m69a(imageView);
        m62a(c0139a);
    }

    public void loadImage(ImageView imageView, Uri uri) {
        C0139a c0139a = new C0139a(uri);
        c0139a.m69a(imageView);
        m62a(c0139a);
    }

    public void loadImage(ImageView imageView, Uri uri, int defaultResId) {
        C0139a c0139a = new C0139a(uri);
        c0139a.m73j(defaultResId);
        c0139a.m69a(imageView);
        m62a(c0139a);
    }

    public void loadImage(OnImageLoadedListener listener, Uri uri) {
        C0139a c0139a = new C0139a(uri);
        c0139a.m70a(listener);
        m62a(c0139a);
    }

    public void loadImage(OnImageLoadedListener listener, Uri uri, int defaultResId) {
        C0139a c0139a = new C0139a(uri);
        c0139a.m73j(defaultResId);
        c0139a.m70a(listener);
        m62a(c0139a);
    }
}
