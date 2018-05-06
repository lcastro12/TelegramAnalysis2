package org.telegram.messenger;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import net.hockeyapp.android.Strings;
import org.telegram.messenger.FileLoader.FileLoaderDelegate;
import org.telegram.messenger.support.widget.helper.ItemTouchHelper.Callback;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.Document;
import org.telegram.tgnet.TLRPC.FileLocation;
import org.telegram.tgnet.TLRPC.InputEncryptedFile;
import org.telegram.tgnet.TLRPC.InputFile;
import org.telegram.tgnet.TLRPC.Message;
import org.telegram.tgnet.TLRPC.PhotoSize;
import org.telegram.tgnet.TLRPC.TL_fileLocation;
import org.telegram.tgnet.TLRPC.TL_fileLocationUnavailable;
import org.telegram.tgnet.TLRPC.TL_messageMediaDocument;
import org.telegram.tgnet.TLRPC.TL_messageMediaPhoto;
import org.telegram.tgnet.TLRPC.TL_messageMediaVideo;
import org.telegram.tgnet.TLRPC.TL_messageMediaWebPage;
import org.telegram.tgnet.TLRPC.TL_photoCachedSize;
import org.telegram.tgnet.TLRPC.TL_photoSize;

public class ImageLoader {
    private static volatile ImageLoader Instance = null;
    private static byte[] bytes;
    private static byte[] bytesThumb;
    private static byte[] header = new byte[12];
    private static byte[] headerThumb = new byte[12];
    private HashMap<String, Integer> bitmapUseCounts = new HashMap();
    private DispatchQueue cacheOutQueue = new DispatchQueue("cacheOutQueue");
    private DispatchQueue cacheThumbOutQueue = new DispatchQueue("cacheThumbOutQueue");
    private int currentHttpFileLoadTasksCount = 0;
    private int currentHttpTasksCount = 0;
    private ConcurrentHashMap<String, Float> fileProgresses = new ConcurrentHashMap();
    private LinkedList<HttpFileTask> httpFileLoadTasks = new LinkedList();
    private HashMap<String, HttpFileTask> httpFileLoadTasksByKeys = new HashMap();
    private LinkedList<HttpImageTask> httpTasks = new LinkedList();
    private String ignoreRemoval = null;
    private DispatchQueue imageLoadQueue = new DispatchQueue("imageLoadQueue");
    private HashMap<String, CacheImage> imageLoadingByKeys = new HashMap();
    private HashMap<Integer, CacheImage> imageLoadingByTag = new HashMap();
    private HashMap<String, CacheImage> imageLoadingByUrl = new HashMap();
    private volatile long lastCacheOutTime = 0;
    private int lastImageNum = 0;
    private long lastProgressUpdateTime = 0;
    private LruCache memCache;
    private HashMap<String, Runnable> retryHttpsTasks = new HashMap();
    public VMRuntimeHack runtimeHack = null;
    private File telegramPath = null;
    private HashMap<String, ThumbGenerateTask> thumbGenerateTasks = new HashMap();
    private DispatchQueue thumbGeneratingQueue = new DispatchQueue("thumbGeneratingQueue");
    private HashMap<String, ThumbGenerateInfo> waitingForQualityThumb = new HashMap();
    private HashMap<Integer, String> waitingForQualityThumbByTag = new HashMap();

    class C03803 extends BroadcastReceiver {

        class C03791 implements Runnable {

            class C03781 implements Runnable {
                C03781() {
                }

                public void run() {
                    final HashMap<Integer, File> paths = ImageLoader.this.createMediaPaths();
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        public void run() {
                            FileLoader.getInstance().setMediaDirs(paths);
                        }
                    });
                }
            }

            C03791() {
            }

            public void run() {
                ImageLoader.this.cacheOutQueue.postRunnable(new C03781());
            }
        }

        C03803() {
        }

        public void onReceive(Context arg0, Intent intent) {
            FileLog.m609e("tmessages", "file system changed");
            Runnable r = new C03791();
            if ("android.intent.action.MEDIA_UNMOUNTED".equals(intent.getAction())) {
                AndroidUtilities.runOnUIThread(r, 1000);
            } else {
                r.run();
            }
        }
    }

    class C03824 implements Runnable {
        C03824() {
        }

        public void run() {
            final HashMap<Integer, File> paths = ImageLoader.this.createMediaPaths();
            AndroidUtilities.runOnUIThread(new Runnable() {
                public void run() {
                    FileLoader.getInstance().setMediaDirs(paths);
                }
            });
        }
    }

    private class CacheImage {
        protected CacheOutTask cacheTask;
        protected String ext;
        protected String filter;
        protected File finalFilePath;
        protected HttpImageTask httpTask;
        protected String httpUrl;
        protected ArrayList<ImageReceiver> imageReceiverArray;
        protected String key;
        protected TLObject location;
        protected File tempFilePath;
        protected boolean thumb;
        protected String url;

        private CacheImage() {
            this.imageReceiverArray = new ArrayList();
        }

        public void addImageReceiver(ImageReceiver imageReceiver) {
            boolean exist = false;
            Iterator i$ = this.imageReceiverArray.iterator();
            while (i$.hasNext()) {
                if (((ImageReceiver) i$.next()) == imageReceiver) {
                    exist = true;
                    break;
                }
            }
            if (!exist) {
                this.imageReceiverArray.add(imageReceiver);
                ImageLoader.this.imageLoadingByTag.put(imageReceiver.getTag(this.thumb), this);
            }
        }

        public void removeImageReceiver(ImageReceiver imageReceiver) {
            int a = 0;
            while (a < this.imageReceiverArray.size()) {
                ImageReceiver obj = (ImageReceiver) this.imageReceiverArray.get(a);
                if (obj == null || obj == imageReceiver) {
                    this.imageReceiverArray.remove(a);
                    if (obj != null) {
                        ImageLoader.this.imageLoadingByTag.remove(obj.getTag(this.thumb));
                    }
                    a--;
                }
                a++;
            }
            if (this.imageReceiverArray.size() == 0) {
                Iterator i$ = this.imageReceiverArray.iterator();
                while (i$.hasNext()) {
                    ImageLoader.this.imageLoadingByTag.remove(((ImageReceiver) i$.next()).getTag(this.thumb));
                }
                this.imageReceiverArray.clear();
                if (this.location != null) {
                    if (this.location instanceof FileLocation) {
                        FileLoader.getInstance().cancelLoadFile((FileLocation) this.location, this.ext);
                    } else if (this.location instanceof Document) {
                        FileLoader.getInstance().cancelLoadFile((Document) this.location);
                    }
                }
                if (this.cacheTask != null) {
                    if (this.thumb) {
                        ImageLoader.this.cacheThumbOutQueue.cancelRunnable(this.cacheTask);
                    } else {
                        ImageLoader.this.cacheOutQueue.cancelRunnable(this.cacheTask);
                    }
                    this.cacheTask.cancel();
                    this.cacheTask = null;
                }
                if (this.httpTask != null) {
                    ImageLoader.this.httpTasks.remove(this.httpTask);
                    this.httpTask.cancel(true);
                    this.httpTask = null;
                }
                if (this.url != null) {
                    ImageLoader.this.imageLoadingByUrl.remove(this.url);
                }
                if (this.key != null) {
                    ImageLoader.this.imageLoadingByKeys.remove(this.key);
                }
            }
        }

        public void setImageAndClear(final BitmapDrawable image) {
            if (image != null) {
                final ArrayList<ImageReceiver> finalImageReceiverArray = new ArrayList(this.imageReceiverArray);
                AndroidUtilities.runOnUIThread(new Runnable() {
                    public void run() {
                        Iterator i$ = finalImageReceiverArray.iterator();
                        while (i$.hasNext()) {
                            ((ImageReceiver) i$.next()).setImageBitmapByKey(image, CacheImage.this.key, CacheImage.this.thumb, false);
                        }
                    }
                });
            }
            Iterator i$ = this.imageReceiverArray.iterator();
            while (i$.hasNext()) {
                ImageLoader.this.imageLoadingByTag.remove(((ImageReceiver) i$.next()).getTag(this.thumb));
            }
            this.imageReceiverArray.clear();
            if (this.url != null) {
                ImageLoader.this.imageLoadingByUrl.remove(this.url);
            }
            if (this.key != null) {
                ImageLoader.this.imageLoadingByKeys.remove(this.key);
            }
        }
    }

    private class CacheOutTask implements Runnable {
        private CacheImage cacheImage;
        private boolean isCancelled;
        private Thread runningThread;
        private final Object sync = new Object();

        public CacheOutTask(CacheImage image) {
            this.cacheImage = image;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            /*
            r43 = this;
            r0 = r43;
            r5 = r0.sync;
            monitor-enter(r5);
            r4 = java.lang.Thread.currentThread();	 Catch:{ all -> 0x00d9 }
            r0 = r43;
            r0.runningThread = r4;	 Catch:{ all -> 0x00d9 }
            java.lang.Thread.interrupted();	 Catch:{ all -> 0x00d9 }
            r0 = r43;
            r4 = r0.isCancelled;	 Catch:{ all -> 0x00d9 }
            if (r4 == 0) goto L_0x0018;
        L_0x0016:
            monitor-exit(r5);	 Catch:{ all -> 0x00d9 }
        L_0x0017:
            return;
        L_0x0018:
            monitor-exit(r5);	 Catch:{ all -> 0x00d9 }
            r31 = 0;
            r32 = 0;
            r28 = 0;
            r0 = r43;
            r4 = r0.cacheImage;
            r0 = r4.finalFilePath;
            r19 = r0;
            r20 = 1;
            r41 = 0;
            r4 = android.os.Build.VERSION.SDK_INT;
            r5 = 19;
            if (r4 >= r5) goto L_0x0082;
        L_0x0031:
            r36 = 0;
            r37 = new java.io.RandomAccessFile;	 Catch:{ Exception -> 0x00eb }
            r4 = "r";
            r0 = r37;
            r1 = r19;
            r0.<init>(r1, r4);	 Catch:{ Exception -> 0x00eb }
            r0 = r43;
            r4 = r0.cacheImage;	 Catch:{ Exception -> 0x0636, all -> 0x0631 }
            r4 = r4.thumb;	 Catch:{ Exception -> 0x0636, all -> 0x0631 }
            if (r4 == 0) goto L_0x00dc;
        L_0x0046:
            r18 = org.telegram.messenger.ImageLoader.headerThumb;	 Catch:{ Exception -> 0x0636, all -> 0x0631 }
        L_0x004a:
            r4 = 0;
            r0 = r18;
            r5 = r0.length;	 Catch:{ Exception -> 0x0636, all -> 0x0631 }
            r0 = r37;
            r1 = r18;
            r0.readFully(r1, r4, r5);	 Catch:{ Exception -> 0x0636, all -> 0x0631 }
            r4 = new java.lang.String;	 Catch:{ Exception -> 0x0636, all -> 0x0631 }
            r0 = r18;
            r4.<init>(r0);	 Catch:{ Exception -> 0x0636, all -> 0x0631 }
            r40 = r4.toLowerCase();	 Catch:{ Exception -> 0x0636, all -> 0x0631 }
            r40 = r40.toLowerCase();	 Catch:{ Exception -> 0x0636, all -> 0x0631 }
            r4 = "riff";
            r0 = r40;
            r4 = r0.startsWith(r4);	 Catch:{ Exception -> 0x0636, all -> 0x0631 }
            if (r4 == 0) goto L_0x007a;
        L_0x006e:
            r4 = "webp";
            r0 = r40;
            r4 = r0.endsWith(r4);	 Catch:{ Exception -> 0x0636, all -> 0x0631 }
            if (r4 == 0) goto L_0x007a;
        L_0x0078:
            r41 = 1;
        L_0x007a:
            r37.close();	 Catch:{ Exception -> 0x0636, all -> 0x0631 }
            if (r37 == 0) goto L_0x0082;
        L_0x007f:
            r37.close();	 Catch:{ Exception -> 0x00e2 }
        L_0x0082:
            r0 = r43;
            r4 = r0.cacheImage;
            r4 = r4.thumb;
            if (r4 == 0) goto L_0x02a6;
        L_0x008a:
            r14 = 0;
            r0 = r43;
            r4 = r0.cacheImage;
            r4 = r4.filter;
            if (r4 == 0) goto L_0x00a2;
        L_0x0093:
            r0 = r43;
            r4 = r0.cacheImage;
            r4 = r4.filter;
            r5 = "b2";
            r4 = r4.contains(r5);
            if (r4 == 0) goto L_0x0112;
        L_0x00a1:
            r14 = 3;
        L_0x00a2:
            r0 = r43;
            r4 = org.telegram.messenger.ImageLoader.this;	 Catch:{ Throwable -> 0x00be }
            r6 = java.lang.System.currentTimeMillis();	 Catch:{ Throwable -> 0x00be }
            r4.lastCacheOutTime = r6;	 Catch:{ Throwable -> 0x00be }
            r0 = r43;
            r5 = r0.sync;	 Catch:{ Throwable -> 0x00be }
            monitor-enter(r5);	 Catch:{ Throwable -> 0x00be }
            r0 = r43;
            r4 = r0.isCancelled;	 Catch:{ all -> 0x00bb }
            if (r4 == 0) goto L_0x0133;
        L_0x00b8:
            monitor-exit(r5);	 Catch:{ all -> 0x00bb }
            goto L_0x0017;
        L_0x00bb:
            r4 = move-exception;
            monitor-exit(r5);	 Catch:{ all -> 0x00bb }
            throw r4;	 Catch:{ Throwable -> 0x00be }
        L_0x00be:
            r23 = move-exception;
            r4 = "tmessages";
            r0 = r23;
            org.telegram.messenger.FileLog.m611e(r4, r0);
        L_0x00c6:
            java.lang.Thread.interrupted();
            if (r28 == 0) goto L_0x062e;
        L_0x00cb:
            r4 = new android.graphics.drawable.BitmapDrawable;
            r0 = r28;
            r4.<init>(r0);
        L_0x00d2:
            r0 = r43;
            r0.onPostExecute(r4);
            goto L_0x0017;
        L_0x00d9:
            r4 = move-exception;
            monitor-exit(r5);	 Catch:{ all -> 0x00d9 }
            throw r4;
        L_0x00dc:
            r18 = org.telegram.messenger.ImageLoader.header;	 Catch:{ Exception -> 0x0636, all -> 0x0631 }
            goto L_0x004a;
        L_0x00e2:
            r23 = move-exception;
            r4 = "tmessages";
            r0 = r23;
            org.telegram.messenger.FileLog.m611e(r4, r0);
            goto L_0x0082;
        L_0x00eb:
            r23 = move-exception;
        L_0x00ec:
            r4 = "tmessages";
            r0 = r23;
            org.telegram.messenger.FileLog.m611e(r4, r0);	 Catch:{ all -> 0x0102 }
            if (r36 == 0) goto L_0x0082;
        L_0x00f5:
            r36.close();	 Catch:{ Exception -> 0x00f9 }
            goto L_0x0082;
        L_0x00f9:
            r23 = move-exception;
            r4 = "tmessages";
            r0 = r23;
            org.telegram.messenger.FileLog.m611e(r4, r0);
            goto L_0x0082;
        L_0x0102:
            r4 = move-exception;
        L_0x0103:
            if (r36 == 0) goto L_0x0108;
        L_0x0105:
            r36.close();	 Catch:{ Exception -> 0x0109 }
        L_0x0108:
            throw r4;
        L_0x0109:
            r23 = move-exception;
            r5 = "tmessages";
            r0 = r23;
            org.telegram.messenger.FileLog.m611e(r5, r0);
            goto L_0x0108;
        L_0x0112:
            r0 = r43;
            r4 = r0.cacheImage;
            r4 = r4.filter;
            r5 = "b1";
            r4 = r4.contains(r5);
            if (r4 == 0) goto L_0x0122;
        L_0x0120:
            r14 = 2;
            goto L_0x00a2;
        L_0x0122:
            r0 = r43;
            r4 = r0.cacheImage;
            r4 = r4.filter;
            r5 = "b";
            r4 = r4.contains(r5);
            if (r4 == 0) goto L_0x00a2;
        L_0x0130:
            r14 = 1;
            goto L_0x00a2;
        L_0x0133:
            monitor-exit(r5);	 Catch:{ all -> 0x00bb }
            r33 = new android.graphics.BitmapFactory$Options;	 Catch:{ Throwable -> 0x00be }
            r33.<init>();	 Catch:{ Throwable -> 0x00be }
            r4 = 1;
            r0 = r33;
            r0.inSampleSize = r4;	 Catch:{ Throwable -> 0x00be }
            r4 = android.os.Build.VERSION.SDK_INT;	 Catch:{ Throwable -> 0x00be }
            r5 = 14;
            if (r4 < r5) goto L_0x014f;
        L_0x0144:
            r4 = android.os.Build.VERSION.SDK_INT;	 Catch:{ Throwable -> 0x00be }
            r5 = 21;
            if (r4 >= r5) goto L_0x014f;
        L_0x014a:
            r4 = 1;
            r0 = r33;
            r0.inPurgeable = r4;	 Catch:{ Throwable -> 0x00be }
        L_0x014f:
            if (r41 == 0) goto L_0x01c2;
        L_0x0151:
            r25 = new java.io.RandomAccessFile;	 Catch:{ Throwable -> 0x00be }
            r4 = "r";
            r0 = r25;
            r1 = r19;
            r0.<init>(r1, r4);	 Catch:{ Throwable -> 0x00be }
            r4 = r25.getChannel();	 Catch:{ Throwable -> 0x00be }
            r5 = java.nio.channels.FileChannel.MapMode.READ_ONLY;	 Catch:{ Throwable -> 0x00be }
            r6 = 0;
            r8 = r19.length();	 Catch:{ Throwable -> 0x00be }
            r17 = r4.map(r5, r6, r8);	 Catch:{ Throwable -> 0x00be }
            r16 = new android.graphics.BitmapFactory$Options;	 Catch:{ Throwable -> 0x00be }
            r16.<init>();	 Catch:{ Throwable -> 0x00be }
            r4 = 1;
            r0 = r16;
            r0.inJustDecodeBounds = r4;	 Catch:{ Throwable -> 0x00be }
            r4 = 0;
            r5 = r17.limit();	 Catch:{ Throwable -> 0x00be }
            r6 = 1;
            r0 = r17;
            r1 = r16;
            org.telegram.messenger.Utilities.loadWebpImage(r4, r0, r5, r1, r6);	 Catch:{ Throwable -> 0x00be }
            r0 = r16;
            r4 = r0.outWidth;	 Catch:{ Throwable -> 0x00be }
            r0 = r16;
            r5 = r0.outHeight;	 Catch:{ Throwable -> 0x00be }
            r6 = android.graphics.Bitmap.Config.ARGB_8888;	 Catch:{ Throwable -> 0x00be }
            r28 = org.telegram.messenger.Bitmaps.createBitmap(r4, r5, r6);	 Catch:{ Throwable -> 0x00be }
            r5 = r17.limit();	 Catch:{ Throwable -> 0x00be }
            r6 = 0;
            r0 = r33;
            r4 = r0.inPurgeable;	 Catch:{ Throwable -> 0x00be }
            if (r4 != 0) goto L_0x01c0;
        L_0x019c:
            r4 = 1;
        L_0x019d:
            r0 = r28;
            r1 = r17;
            org.telegram.messenger.Utilities.loadWebpImage(r0, r1, r5, r6, r4);	 Catch:{ Throwable -> 0x00be }
            r25.close();	 Catch:{ Throwable -> 0x00be }
        L_0x01a7:
            if (r28 != 0) goto L_0x0227;
        L_0x01a9:
            r4 = r19.length();	 Catch:{ Throwable -> 0x00be }
            r6 = 0;
            r4 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1));
            if (r4 == 0) goto L_0x01bb;
        L_0x01b3:
            r0 = r43;
            r4 = r0.cacheImage;	 Catch:{ Throwable -> 0x00be }
            r4 = r4.filter;	 Catch:{ Throwable -> 0x00be }
            if (r4 != 0) goto L_0x00c6;
        L_0x01bb:
            r19.delete();	 Catch:{ Throwable -> 0x00be }
            goto L_0x00c6;
        L_0x01c0:
            r4 = 0;
            goto L_0x019d;
        L_0x01c2:
            r0 = r33;
            r4 = r0.inPurgeable;	 Catch:{ Throwable -> 0x00be }
            if (r4 == 0) goto L_0x0211;
        L_0x01c8:
            r24 = new java.io.RandomAccessFile;	 Catch:{ Throwable -> 0x00be }
            r4 = "r";
            r0 = r24;
            r1 = r19;
            r0.<init>(r1, r4);	 Catch:{ Throwable -> 0x00be }
            r4 = r24.length();	 Catch:{ Throwable -> 0x00be }
            r0 = (int) r4;	 Catch:{ Throwable -> 0x00be }
            r30 = r0;
            r4 = org.telegram.messenger.ImageLoader.bytesThumb;	 Catch:{ Throwable -> 0x00be }
            if (r4 == 0) goto L_0x020e;
        L_0x01e0:
            r4 = org.telegram.messenger.ImageLoader.bytesThumb;	 Catch:{ Throwable -> 0x00be }
            r4 = r4.length;	 Catch:{ Throwable -> 0x00be }
            r0 = r30;
            if (r4 < r0) goto L_0x020e;
        L_0x01e9:
            r21 = org.telegram.messenger.ImageLoader.bytesThumb;	 Catch:{ Throwable -> 0x00be }
        L_0x01ed:
            if (r21 != 0) goto L_0x01f8;
        L_0x01ef:
            r0 = r30;
            r0 = new byte[r0];	 Catch:{ Throwable -> 0x00be }
            r21 = r0;
            org.telegram.messenger.ImageLoader.bytesThumb = r21;	 Catch:{ Throwable -> 0x00be }
        L_0x01f8:
            r4 = 0;
            r0 = r24;
            r1 = r21;
            r2 = r30;
            r0.readFully(r1, r4, r2);	 Catch:{ Throwable -> 0x00be }
            r4 = 0;
            r0 = r21;
            r1 = r30;
            r2 = r33;
            r28 = android.graphics.BitmapFactory.decodeByteArray(r0, r4, r1, r2);	 Catch:{ Throwable -> 0x00be }
            goto L_0x01a7;
        L_0x020e:
            r21 = 0;
            goto L_0x01ed;
        L_0x0211:
            r29 = new java.io.FileInputStream;	 Catch:{ Throwable -> 0x00be }
            r0 = r29;
            r1 = r19;
            r0.<init>(r1);	 Catch:{ Throwable -> 0x00be }
            r4 = 0;
            r0 = r29;
            r1 = r33;
            r28 = android.graphics.BitmapFactory.decodeStream(r0, r4, r1);	 Catch:{ Throwable -> 0x00be }
            r29.close();	 Catch:{ Throwable -> 0x00be }
            goto L_0x01a7;
        L_0x0227:
            r4 = 1;
            if (r14 != r4) goto L_0x0256;
        L_0x022a:
            r5 = 3;
            r0 = r33;
            r4 = r0.inPurgeable;	 Catch:{ Throwable -> 0x00be }
            if (r4 == 0) goto L_0x0254;
        L_0x0231:
            r4 = 0;
        L_0x0232:
            r0 = r28;
            org.telegram.messenger.Utilities.blurBitmap(r0, r5, r4);	 Catch:{ Throwable -> 0x00be }
        L_0x0237:
            r0 = r43;
            r4 = org.telegram.messenger.ImageLoader.this;	 Catch:{ Throwable -> 0x00be }
            r4 = r4.runtimeHack;	 Catch:{ Throwable -> 0x00be }
            if (r4 == 0) goto L_0x00c6;
        L_0x023f:
            r0 = r43;
            r4 = org.telegram.messenger.ImageLoader.this;	 Catch:{ Throwable -> 0x00be }
            r4 = r4.runtimeHack;	 Catch:{ Throwable -> 0x00be }
            r5 = r28.getRowBytes();	 Catch:{ Throwable -> 0x00be }
            r6 = r28.getHeight();	 Catch:{ Throwable -> 0x00be }
            r5 = r5 * r6;
            r6 = (long) r5;	 Catch:{ Throwable -> 0x00be }
            r4.trackFree(r6);	 Catch:{ Throwable -> 0x00be }
            goto L_0x00c6;
        L_0x0254:
            r4 = 1;
            goto L_0x0232;
        L_0x0256:
            r4 = 2;
            if (r14 != r4) goto L_0x0269;
        L_0x0259:
            r5 = 1;
            r0 = r33;
            r4 = r0.inPurgeable;	 Catch:{ Throwable -> 0x00be }
            if (r4 == 0) goto L_0x0267;
        L_0x0260:
            r4 = 0;
        L_0x0261:
            r0 = r28;
            org.telegram.messenger.Utilities.blurBitmap(r0, r5, r4);	 Catch:{ Throwable -> 0x00be }
            goto L_0x0237;
        L_0x0267:
            r4 = 1;
            goto L_0x0261;
        L_0x0269:
            r4 = 3;
            if (r14 != r4) goto L_0x029a;
        L_0x026c:
            r5 = 7;
            r0 = r33;
            r4 = r0.inPurgeable;	 Catch:{ Throwable -> 0x00be }
            if (r4 == 0) goto L_0x0294;
        L_0x0273:
            r4 = 0;
        L_0x0274:
            r0 = r28;
            org.telegram.messenger.Utilities.blurBitmap(r0, r5, r4);	 Catch:{ Throwable -> 0x00be }
            r5 = 7;
            r0 = r33;
            r4 = r0.inPurgeable;	 Catch:{ Throwable -> 0x00be }
            if (r4 == 0) goto L_0x0296;
        L_0x0280:
            r4 = 0;
        L_0x0281:
            r0 = r28;
            org.telegram.messenger.Utilities.blurBitmap(r0, r5, r4);	 Catch:{ Throwable -> 0x00be }
            r5 = 7;
            r0 = r33;
            r4 = r0.inPurgeable;	 Catch:{ Throwable -> 0x00be }
            if (r4 == 0) goto L_0x0298;
        L_0x028d:
            r4 = 0;
        L_0x028e:
            r0 = r28;
            org.telegram.messenger.Utilities.blurBitmap(r0, r5, r4);	 Catch:{ Throwable -> 0x00be }
            goto L_0x0237;
        L_0x0294:
            r4 = 1;
            goto L_0x0274;
        L_0x0296:
            r4 = 1;
            goto L_0x0281;
        L_0x0298:
            r4 = 1;
            goto L_0x028e;
        L_0x029a:
            if (r14 != 0) goto L_0x0237;
        L_0x029c:
            r0 = r33;
            r4 = r0.inPurgeable;	 Catch:{ Throwable -> 0x00be }
            if (r4 == 0) goto L_0x0237;
        L_0x02a2:
            org.telegram.messenger.Utilities.pinBitmap(r28);	 Catch:{ Throwable -> 0x00be }
            goto L_0x0237;
        L_0x02a6:
            r0 = r43;
            r4 = r0.cacheImage;	 Catch:{ Throwable -> 0x0342 }
            r4 = r4.httpUrl;	 Catch:{ Throwable -> 0x0342 }
            if (r4 == 0) goto L_0x02e6;
        L_0x02ae:
            r0 = r43;
            r4 = r0.cacheImage;	 Catch:{ Throwable -> 0x0342 }
            r4 = r4.httpUrl;	 Catch:{ Throwable -> 0x0342 }
            r5 = "thumb://";
            r4 = r4.startsWith(r5);	 Catch:{ Throwable -> 0x0342 }
            if (r4 == 0) goto L_0x0345;
        L_0x02bc:
            r0 = r43;
            r4 = r0.cacheImage;	 Catch:{ Throwable -> 0x0342 }
            r4 = r4.httpUrl;	 Catch:{ Throwable -> 0x0342 }
            r5 = ":";
            r6 = 8;
            r27 = r4.indexOf(r5, r6);	 Catch:{ Throwable -> 0x0342 }
            if (r27 < 0) goto L_0x02e4;
        L_0x02cc:
            r0 = r43;
            r4 = r0.cacheImage;	 Catch:{ Throwable -> 0x0342 }
            r4 = r4.httpUrl;	 Catch:{ Throwable -> 0x0342 }
            r5 = 8;
            r0 = r27;
            r4 = r4.substring(r5, r0);	 Catch:{ Throwable -> 0x0342 }
            r4 = java.lang.Long.parseLong(r4);	 Catch:{ Throwable -> 0x0342 }
            r31 = java.lang.Long.valueOf(r4);	 Catch:{ Throwable -> 0x0342 }
            r32 = 0;
        L_0x02e4:
            r20 = 0;
        L_0x02e6:
            r22 = 20;
            r0 = r43;
            r4 = org.telegram.messenger.ImageLoader.this;	 Catch:{ Throwable -> 0x0342 }
            r4 = r4.runtimeHack;	 Catch:{ Throwable -> 0x0342 }
            if (r4 == 0) goto L_0x02f2;
        L_0x02f0:
            r22 = 60;
        L_0x02f2:
            if (r31 == 0) goto L_0x02f6;
        L_0x02f4:
            r22 = 0;
        L_0x02f6:
            if (r22 == 0) goto L_0x0326;
        L_0x02f8:
            r0 = r43;
            r4 = org.telegram.messenger.ImageLoader.this;	 Catch:{ Throwable -> 0x0342 }
            r4 = r4.lastCacheOutTime;	 Catch:{ Throwable -> 0x0342 }
            r6 = 0;
            r4 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1));
            if (r4 == 0) goto L_0x0326;
        L_0x0306:
            r0 = r43;
            r4 = org.telegram.messenger.ImageLoader.this;	 Catch:{ Throwable -> 0x0342 }
            r4 = r4.lastCacheOutTime;	 Catch:{ Throwable -> 0x0342 }
            r6 = java.lang.System.currentTimeMillis();	 Catch:{ Throwable -> 0x0342 }
            r0 = r22;
            r8 = (long) r0;	 Catch:{ Throwable -> 0x0342 }
            r6 = r6 - r8;
            r4 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1));
            if (r4 <= 0) goto L_0x0326;
        L_0x031a:
            r4 = android.os.Build.VERSION.SDK_INT;	 Catch:{ Throwable -> 0x0342 }
            r5 = 21;
            if (r4 >= r5) goto L_0x0326;
        L_0x0320:
            r0 = r22;
            r4 = (long) r0;	 Catch:{ Throwable -> 0x0342 }
            java.lang.Thread.sleep(r4);	 Catch:{ Throwable -> 0x0342 }
        L_0x0326:
            r0 = r43;
            r4 = org.telegram.messenger.ImageLoader.this;	 Catch:{ Throwable -> 0x0342 }
            r6 = java.lang.System.currentTimeMillis();	 Catch:{ Throwable -> 0x0342 }
            r4.lastCacheOutTime = r6;	 Catch:{ Throwable -> 0x0342 }
            r0 = r43;
            r5 = r0.sync;	 Catch:{ Throwable -> 0x0342 }
            monitor-enter(r5);	 Catch:{ Throwable -> 0x0342 }
            r0 = r43;
            r4 = r0.isCancelled;	 Catch:{ all -> 0x033f }
            if (r4 == 0) goto L_0x0391;
        L_0x033c:
            monitor-exit(r5);	 Catch:{ all -> 0x033f }
            goto L_0x0017;
        L_0x033f:
            r4 = move-exception;
            monitor-exit(r5);	 Catch:{ all -> 0x033f }
            throw r4;	 Catch:{ Throwable -> 0x0342 }
        L_0x0342:
            r4 = move-exception;
            goto L_0x00c6;
        L_0x0345:
            r0 = r43;
            r4 = r0.cacheImage;	 Catch:{ Throwable -> 0x0342 }
            r4 = r4.httpUrl;	 Catch:{ Throwable -> 0x0342 }
            r5 = "vthumb://";
            r4 = r4.startsWith(r5);	 Catch:{ Throwable -> 0x0342 }
            if (r4 == 0) goto L_0x037f;
        L_0x0353:
            r0 = r43;
            r4 = r0.cacheImage;	 Catch:{ Throwable -> 0x0342 }
            r4 = r4.httpUrl;	 Catch:{ Throwable -> 0x0342 }
            r5 = ":";
            r6 = 9;
            r27 = r4.indexOf(r5, r6);	 Catch:{ Throwable -> 0x0342 }
            if (r27 < 0) goto L_0x037b;
        L_0x0363:
            r0 = r43;
            r4 = r0.cacheImage;	 Catch:{ Throwable -> 0x0342 }
            r4 = r4.httpUrl;	 Catch:{ Throwable -> 0x0342 }
            r5 = 9;
            r0 = r27;
            r4 = r4.substring(r5, r0);	 Catch:{ Throwable -> 0x0342 }
            r4 = java.lang.Long.parseLong(r4);	 Catch:{ Throwable -> 0x0342 }
            r31 = java.lang.Long.valueOf(r4);	 Catch:{ Throwable -> 0x0342 }
            r32 = 1;
        L_0x037b:
            r20 = 0;
            goto L_0x02e6;
        L_0x037f:
            r0 = r43;
            r4 = r0.cacheImage;	 Catch:{ Throwable -> 0x0342 }
            r4 = r4.httpUrl;	 Catch:{ Throwable -> 0x0342 }
            r5 = "http";
            r4 = r4.startsWith(r5);	 Catch:{ Throwable -> 0x0342 }
            if (r4 != 0) goto L_0x02e6;
        L_0x038d:
            r20 = 0;
            goto L_0x02e6;
        L_0x0391:
            monitor-exit(r5);	 Catch:{ all -> 0x033f }
            r33 = new android.graphics.BitmapFactory$Options;	 Catch:{ Throwable -> 0x0342 }
            r33.<init>();	 Catch:{ Throwable -> 0x0342 }
            r4 = 1;
            r0 = r33;
            r0.inSampleSize = r4;	 Catch:{ Throwable -> 0x0342 }
            r42 = 0;
            r26 = 0;
            r13 = 0;
            r0 = r43;
            r4 = r0.cacheImage;	 Catch:{ Throwable -> 0x0342 }
            r4 = r4.filter;	 Catch:{ Throwable -> 0x0342 }
            if (r4 == 0) goto L_0x042b;
        L_0x03a9:
            r0 = r43;
            r4 = r0.cacheImage;	 Catch:{ Throwable -> 0x0342 }
            r4 = r4.filter;	 Catch:{ Throwable -> 0x0342 }
            r5 = "_";
            r10 = r4.split(r5);	 Catch:{ Throwable -> 0x0342 }
            r4 = r10.length;	 Catch:{ Throwable -> 0x0342 }
            r5 = 2;
            if (r4 < r5) goto L_0x03cf;
        L_0x03b9:
            r4 = 0;
            r4 = r10[r4];	 Catch:{ Throwable -> 0x0342 }
            r4 = java.lang.Float.parseFloat(r4);	 Catch:{ Throwable -> 0x0342 }
            r5 = org.telegram.messenger.AndroidUtilities.density;	 Catch:{ Throwable -> 0x0342 }
            r42 = r4 * r5;
            r4 = 1;
            r4 = r10[r4];	 Catch:{ Throwable -> 0x0342 }
            r4 = java.lang.Float.parseFloat(r4);	 Catch:{ Throwable -> 0x0342 }
            r5 = org.telegram.messenger.AndroidUtilities.density;	 Catch:{ Throwable -> 0x0342 }
            r26 = r4 * r5;
        L_0x03cf:
            r0 = r43;
            r4 = r0.cacheImage;	 Catch:{ Throwable -> 0x0342 }
            r4 = r4.filter;	 Catch:{ Throwable -> 0x0342 }
            r5 = "b";
            r4 = r4.contains(r5);	 Catch:{ Throwable -> 0x0342 }
            if (r4 == 0) goto L_0x03de;
        L_0x03dd:
            r13 = 1;
        L_0x03de:
            r4 = 0;
            r4 = (r42 > r4 ? 1 : (r42 == r4 ? 0 : -1));
            if (r4 == 0) goto L_0x042b;
        L_0x03e3:
            r4 = 0;
            r4 = (r26 > r4 ? 1 : (r26 == r4 ? 0 : -1));
            if (r4 == 0) goto L_0x042b;
        L_0x03e8:
            r4 = 1;
            r0 = r33;
            r0.inJustDecodeBounds = r4;	 Catch:{ Throwable -> 0x0342 }
            if (r31 == 0) goto L_0x044d;
        L_0x03ef:
            if (r32 == 0) goto L_0x043c;
        L_0x03f1:
            r4 = org.telegram.messenger.ApplicationLoader.applicationContext;	 Catch:{ Throwable -> 0x0342 }
            r4 = r4.getContentResolver();	 Catch:{ Throwable -> 0x0342 }
            r6 = r31.longValue();	 Catch:{ Throwable -> 0x0342 }
            r5 = 1;
            r0 = r33;
            android.provider.MediaStore.Video.Thumbnails.getThumbnail(r4, r6, r5, r0);	 Catch:{ Throwable -> 0x0342 }
        L_0x0401:
            r0 = r33;
            r4 = r0.outWidth;	 Catch:{ Throwable -> 0x0342 }
            r0 = (float) r4;	 Catch:{ Throwable -> 0x0342 }
            r35 = r0;
            r0 = r33;
            r4 = r0.outHeight;	 Catch:{ Throwable -> 0x0342 }
            r0 = (float) r4;	 Catch:{ Throwable -> 0x0342 }
            r34 = r0;
            r4 = r35 / r42;
            r5 = r34 / r26;
            r38 = java.lang.Math.max(r4, r5);	 Catch:{ Throwable -> 0x0342 }
            r4 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
            r4 = (r38 > r4 ? 1 : (r38 == r4 ? 0 : -1));
            if (r4 >= 0) goto L_0x041f;
        L_0x041d:
            r38 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        L_0x041f:
            r4 = 0;
            r0 = r33;
            r0.inJustDecodeBounds = r4;	 Catch:{ Throwable -> 0x0342 }
            r0 = r38;
            r4 = (int) r0;	 Catch:{ Throwable -> 0x0342 }
            r0 = r33;
            r0.inSampleSize = r4;	 Catch:{ Throwable -> 0x0342 }
        L_0x042b:
            r0 = r43;
            r5 = r0.sync;	 Catch:{ Throwable -> 0x0342 }
            monitor-enter(r5);	 Catch:{ Throwable -> 0x0342 }
            r0 = r43;
            r4 = r0.isCancelled;	 Catch:{ all -> 0x0439 }
            if (r4 == 0) goto L_0x0463;
        L_0x0436:
            monitor-exit(r5);	 Catch:{ all -> 0x0439 }
            goto L_0x0017;
        L_0x0439:
            r4 = move-exception;
            monitor-exit(r5);	 Catch:{ all -> 0x0439 }
            throw r4;	 Catch:{ Throwable -> 0x0342 }
        L_0x043c:
            r4 = org.telegram.messenger.ApplicationLoader.applicationContext;	 Catch:{ Throwable -> 0x0342 }
            r4 = r4.getContentResolver();	 Catch:{ Throwable -> 0x0342 }
            r6 = r31.longValue();	 Catch:{ Throwable -> 0x0342 }
            r5 = 1;
            r0 = r33;
            android.provider.MediaStore.Images.Thumbnails.getThumbnail(r4, r6, r5, r0);	 Catch:{ Throwable -> 0x0342 }
            goto L_0x0401;
        L_0x044d:
            r29 = new java.io.FileInputStream;	 Catch:{ Throwable -> 0x0342 }
            r0 = r29;
            r1 = r19;
            r0.<init>(r1);	 Catch:{ Throwable -> 0x0342 }
            r4 = 0;
            r0 = r29;
            r1 = r33;
            r28 = android.graphics.BitmapFactory.decodeStream(r0, r4, r1);	 Catch:{ Throwable -> 0x0342 }
            r29.close();	 Catch:{ Throwable -> 0x0342 }
            goto L_0x0401;
        L_0x0463:
            monitor-exit(r5);	 Catch:{ all -> 0x0439 }
            r0 = r43;
            r4 = r0.cacheImage;	 Catch:{ Throwable -> 0x0342 }
            r4 = r4.filter;	 Catch:{ Throwable -> 0x0342 }
            if (r4 == 0) goto L_0x0476;
        L_0x046c:
            if (r13 != 0) goto L_0x0476;
        L_0x046e:
            r0 = r43;
            r4 = r0.cacheImage;	 Catch:{ Throwable -> 0x0342 }
            r4 = r4.httpUrl;	 Catch:{ Throwable -> 0x0342 }
            if (r4 == 0) goto L_0x051c;
        L_0x0476:
            r4 = android.graphics.Bitmap.Config.ARGB_8888;	 Catch:{ Throwable -> 0x0342 }
            r0 = r33;
            r0.inPreferredConfig = r4;	 Catch:{ Throwable -> 0x0342 }
        L_0x047c:
            r4 = android.os.Build.VERSION.SDK_INT;	 Catch:{ Throwable -> 0x0342 }
            r5 = 14;
            if (r4 < r5) goto L_0x048d;
        L_0x0482:
            r4 = android.os.Build.VERSION.SDK_INT;	 Catch:{ Throwable -> 0x0342 }
            r5 = 21;
            if (r4 >= r5) goto L_0x048d;
        L_0x0488:
            r4 = 1;
            r0 = r33;
            r0.inPurgeable = r4;	 Catch:{ Throwable -> 0x0342 }
        L_0x048d:
            r4 = 0;
            r0 = r33;
            r0.inDither = r4;	 Catch:{ Throwable -> 0x0342 }
            if (r31 == 0) goto L_0x04a7;
        L_0x0494:
            if (r32 == 0) goto L_0x0524;
        L_0x0496:
            r4 = org.telegram.messenger.ApplicationLoader.applicationContext;	 Catch:{ Throwable -> 0x0342 }
            r4 = r4.getContentResolver();	 Catch:{ Throwable -> 0x0342 }
            r6 = r31.longValue();	 Catch:{ Throwable -> 0x0342 }
            r5 = 1;
            r0 = r33;
            r28 = android.provider.MediaStore.Video.Thumbnails.getThumbnail(r4, r6, r5, r0);	 Catch:{ Throwable -> 0x0342 }
        L_0x04a7:
            if (r28 != 0) goto L_0x0501;
        L_0x04a9:
            if (r41 == 0) goto L_0x0539;
        L_0x04ab:
            r25 = new java.io.RandomAccessFile;	 Catch:{ Throwable -> 0x0342 }
            r4 = "r";
            r0 = r25;
            r1 = r19;
            r0.<init>(r1, r4);	 Catch:{ Throwable -> 0x0342 }
            r4 = r25.getChannel();	 Catch:{ Throwable -> 0x0342 }
            r5 = java.nio.channels.FileChannel.MapMode.READ_ONLY;	 Catch:{ Throwable -> 0x0342 }
            r6 = 0;
            r8 = r19.length();	 Catch:{ Throwable -> 0x0342 }
            r17 = r4.map(r5, r6, r8);	 Catch:{ Throwable -> 0x0342 }
            r16 = new android.graphics.BitmapFactory$Options;	 Catch:{ Throwable -> 0x0342 }
            r16.<init>();	 Catch:{ Throwable -> 0x0342 }
            r4 = 1;
            r0 = r16;
            r0.inJustDecodeBounds = r4;	 Catch:{ Throwable -> 0x0342 }
            r4 = 0;
            r5 = r17.limit();	 Catch:{ Throwable -> 0x0342 }
            r6 = 1;
            r0 = r17;
            r1 = r16;
            org.telegram.messenger.Utilities.loadWebpImage(r4, r0, r5, r1, r6);	 Catch:{ Throwable -> 0x0342 }
            r0 = r16;
            r4 = r0.outWidth;	 Catch:{ Throwable -> 0x0342 }
            r0 = r16;
            r5 = r0.outHeight;	 Catch:{ Throwable -> 0x0342 }
            r6 = android.graphics.Bitmap.Config.ARGB_8888;	 Catch:{ Throwable -> 0x0342 }
            r28 = org.telegram.messenger.Bitmaps.createBitmap(r4, r5, r6);	 Catch:{ Throwable -> 0x0342 }
            r5 = r17.limit();	 Catch:{ Throwable -> 0x0342 }
            r6 = 0;
            r0 = r33;
            r4 = r0.inPurgeable;	 Catch:{ Throwable -> 0x0342 }
            if (r4 != 0) goto L_0x0537;
        L_0x04f6:
            r4 = 1;
        L_0x04f7:
            r0 = r28;
            r1 = r17;
            org.telegram.messenger.Utilities.loadWebpImage(r0, r1, r5, r6, r4);	 Catch:{ Throwable -> 0x0342 }
            r25.close();	 Catch:{ Throwable -> 0x0342 }
        L_0x0501:
            if (r28 != 0) goto L_0x05a0;
        L_0x0503:
            if (r20 == 0) goto L_0x00c6;
        L_0x0505:
            r4 = r19.length();	 Catch:{ Throwable -> 0x0342 }
            r6 = 0;
            r4 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1));
            if (r4 == 0) goto L_0x0517;
        L_0x050f:
            r0 = r43;
            r4 = r0.cacheImage;	 Catch:{ Throwable -> 0x0342 }
            r4 = r4.filter;	 Catch:{ Throwable -> 0x0342 }
            if (r4 != 0) goto L_0x00c6;
        L_0x0517:
            r19.delete();	 Catch:{ Throwable -> 0x0342 }
            goto L_0x00c6;
        L_0x051c:
            r4 = android.graphics.Bitmap.Config.RGB_565;	 Catch:{ Throwable -> 0x0342 }
            r0 = r33;
            r0.inPreferredConfig = r4;	 Catch:{ Throwable -> 0x0342 }
            goto L_0x047c;
        L_0x0524:
            r4 = org.telegram.messenger.ApplicationLoader.applicationContext;	 Catch:{ Throwable -> 0x0342 }
            r4 = r4.getContentResolver();	 Catch:{ Throwable -> 0x0342 }
            r6 = r31.longValue();	 Catch:{ Throwable -> 0x0342 }
            r5 = 1;
            r0 = r33;
            r28 = android.provider.MediaStore.Images.Thumbnails.getThumbnail(r4, r6, r5, r0);	 Catch:{ Throwable -> 0x0342 }
            goto L_0x04a7;
        L_0x0537:
            r4 = 0;
            goto L_0x04f7;
        L_0x0539:
            r0 = r33;
            r4 = r0.inPurgeable;	 Catch:{ Throwable -> 0x0342 }
            if (r4 == 0) goto L_0x0589;
        L_0x053f:
            r24 = new java.io.RandomAccessFile;	 Catch:{ Throwable -> 0x0342 }
            r4 = "r";
            r0 = r24;
            r1 = r19;
            r0.<init>(r1, r4);	 Catch:{ Throwable -> 0x0342 }
            r4 = r24.length();	 Catch:{ Throwable -> 0x0342 }
            r0 = (int) r4;	 Catch:{ Throwable -> 0x0342 }
            r30 = r0;
            r4 = org.telegram.messenger.ImageLoader.bytes;	 Catch:{ Throwable -> 0x0342 }
            if (r4 == 0) goto L_0x0586;
        L_0x0557:
            r4 = org.telegram.messenger.ImageLoader.bytes;	 Catch:{ Throwable -> 0x0342 }
            r4 = r4.length;	 Catch:{ Throwable -> 0x0342 }
            r0 = r30;
            if (r4 < r0) goto L_0x0586;
        L_0x0560:
            r21 = org.telegram.messenger.ImageLoader.bytes;	 Catch:{ Throwable -> 0x0342 }
        L_0x0564:
            if (r21 != 0) goto L_0x056f;
        L_0x0566:
            r0 = r30;
            r0 = new byte[r0];	 Catch:{ Throwable -> 0x0342 }
            r21 = r0;
            org.telegram.messenger.ImageLoader.bytes = r21;	 Catch:{ Throwable -> 0x0342 }
        L_0x056f:
            r4 = 0;
            r0 = r24;
            r1 = r21;
            r2 = r30;
            r0.readFully(r1, r4, r2);	 Catch:{ Throwable -> 0x0342 }
            r4 = 0;
            r0 = r21;
            r1 = r30;
            r2 = r33;
            r28 = android.graphics.BitmapFactory.decodeByteArray(r0, r4, r1, r2);	 Catch:{ Throwable -> 0x0342 }
            goto L_0x0501;
        L_0x0586:
            r21 = 0;
            goto L_0x0564;
        L_0x0589:
            r29 = new java.io.FileInputStream;	 Catch:{ Throwable -> 0x0342 }
            r0 = r29;
            r1 = r19;
            r0.<init>(r1);	 Catch:{ Throwable -> 0x0342 }
            r4 = 0;
            r0 = r29;
            r1 = r33;
            r28 = android.graphics.BitmapFactory.decodeStream(r0, r4, r1);	 Catch:{ Throwable -> 0x0342 }
            r29.close();	 Catch:{ Throwable -> 0x0342 }
            goto L_0x0501;
        L_0x05a0:
            r15 = 0;
            r0 = r43;
            r4 = r0.cacheImage;	 Catch:{ Throwable -> 0x0342 }
            r4 = r4.filter;	 Catch:{ Throwable -> 0x0342 }
            if (r4 == 0) goto L_0x0602;
        L_0x05a9:
            r4 = r28.getWidth();	 Catch:{ Throwable -> 0x0342 }
            r12 = (float) r4;	 Catch:{ Throwable -> 0x0342 }
            r4 = r28.getHeight();	 Catch:{ Throwable -> 0x0342 }
            r11 = (float) r4;	 Catch:{ Throwable -> 0x0342 }
            r0 = r33;
            r4 = r0.inPurgeable;	 Catch:{ Throwable -> 0x0342 }
            if (r4 != 0) goto L_0x05e4;
        L_0x05b9:
            r4 = 0;
            r4 = (r42 > r4 ? 1 : (r42 == r4 ? 0 : -1));
            if (r4 == 0) goto L_0x05e4;
        L_0x05be:
            r4 = (r12 > r42 ? 1 : (r12 == r42 ? 0 : -1));
            if (r4 == 0) goto L_0x05e4;
        L_0x05c2:
            r4 = 1101004800; // 0x41a00000 float:20.0 double:5.439686476E-315;
            r4 = r4 + r42;
            r4 = (r12 > r4 ? 1 : (r12 == r4 ? 0 : -1));
            if (r4 <= 0) goto L_0x05e4;
        L_0x05ca:
            r38 = r12 / r42;
            r0 = r42;
            r4 = (int) r0;	 Catch:{ Throwable -> 0x0342 }
            r5 = r11 / r38;
            r5 = (int) r5;	 Catch:{ Throwable -> 0x0342 }
            r6 = 1;
            r0 = r28;
            r39 = org.telegram.messenger.Bitmaps.createScaledBitmap(r0, r4, r5, r6);	 Catch:{ Throwable -> 0x0342 }
            r0 = r28;
            r1 = r39;
            if (r0 == r1) goto L_0x05e4;
        L_0x05df:
            r28.recycle();	 Catch:{ Throwable -> 0x0342 }
            r28 = r39;
        L_0x05e4:
            if (r28 == 0) goto L_0x0602;
        L_0x05e6:
            if (r13 == 0) goto L_0x0602;
        L_0x05e8:
            r4 = 1120403456; // 0x42c80000 float:100.0 double:5.53552857E-315;
            r4 = (r11 > r4 ? 1 : (r11 == r4 ? 0 : -1));
            if (r4 >= 0) goto L_0x0602;
        L_0x05ee:
            r4 = 1120403456; // 0x42c80000 float:100.0 double:5.53552857E-315;
            r4 = (r12 > r4 ? 1 : (r12 == r4 ? 0 : -1));
            if (r4 >= 0) goto L_0x0602;
        L_0x05f4:
            r5 = 3;
            r0 = r33;
            r4 = r0.inPurgeable;	 Catch:{ Throwable -> 0x0342 }
            if (r4 == 0) goto L_0x062c;
        L_0x05fb:
            r4 = 0;
        L_0x05fc:
            r0 = r28;
            org.telegram.messenger.Utilities.blurBitmap(r0, r5, r4);	 Catch:{ Throwable -> 0x0342 }
            r15 = 1;
        L_0x0602:
            if (r15 != 0) goto L_0x060d;
        L_0x0604:
            r0 = r33;
            r4 = r0.inPurgeable;	 Catch:{ Throwable -> 0x0342 }
            if (r4 == 0) goto L_0x060d;
        L_0x060a:
            org.telegram.messenger.Utilities.pinBitmap(r28);	 Catch:{ Throwable -> 0x0342 }
        L_0x060d:
            r0 = r43;
            r4 = org.telegram.messenger.ImageLoader.this;	 Catch:{ Throwable -> 0x0342 }
            r4 = r4.runtimeHack;	 Catch:{ Throwable -> 0x0342 }
            if (r4 == 0) goto L_0x00c6;
        L_0x0615:
            if (r28 == 0) goto L_0x00c6;
        L_0x0617:
            r0 = r43;
            r4 = org.telegram.messenger.ImageLoader.this;	 Catch:{ Throwable -> 0x0342 }
            r4 = r4.runtimeHack;	 Catch:{ Throwable -> 0x0342 }
            r5 = r28.getRowBytes();	 Catch:{ Throwable -> 0x0342 }
            r6 = r28.getHeight();	 Catch:{ Throwable -> 0x0342 }
            r5 = r5 * r6;
            r6 = (long) r5;	 Catch:{ Throwable -> 0x0342 }
            r4.trackFree(r6);	 Catch:{ Throwable -> 0x0342 }
            goto L_0x00c6;
        L_0x062c:
            r4 = 1;
            goto L_0x05fc;
        L_0x062e:
            r4 = 0;
            goto L_0x00d2;
        L_0x0631:
            r4 = move-exception;
            r36 = r37;
            goto L_0x0103;
        L_0x0636:
            r23 = move-exception;
            r36 = r37;
            goto L_0x00ec;
            */
            throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.ImageLoader.CacheOutTask.run():void");
        }

        private void onPostExecute(final BitmapDrawable bitmapDrawable) {
            AndroidUtilities.runOnUIThread(new Runnable() {
                public void run() {
                    BitmapDrawable toSet = null;
                    if (bitmapDrawable != null) {
                        toSet = ImageLoader.this.memCache.get(CacheOutTask.this.cacheImage.key);
                        if (toSet == null) {
                            ImageLoader.this.memCache.put(CacheOutTask.this.cacheImage.key, bitmapDrawable);
                            toSet = bitmapDrawable;
                        } else {
                            Bitmap image = bitmapDrawable.getBitmap();
                            if (ImageLoader.this.runtimeHack != null) {
                                ImageLoader.this.runtimeHack.trackAlloc((long) (image.getRowBytes() * image.getHeight()));
                            }
                            image.recycle();
                        }
                    }
                    final BitmapDrawable toSetFinal = toSet;
                    ImageLoader.this.imageLoadQueue.postRunnable(new Runnable() {
                        public void run() {
                            CacheOutTask.this.cacheImage.setImageAndClear(toSetFinal);
                        }
                    });
                }
            });
        }

        public void cancel() {
            synchronized (this.sync) {
                try {
                    this.isCancelled = true;
                    if (this.runningThread != null) {
                        this.runningThread.interrupt();
                    }
                } catch (Exception e) {
                }
            }
        }
    }

    private class HttpFileTask extends AsyncTask<Void, Void, Boolean> {
        private boolean canRetry = true;
        private String ext;
        private RandomAccessFile fileOutputStream = null;
        private File tempFile;
        private String url;

        public HttpFileTask(String url, File tempFile, String ext) {
            this.url = url;
            this.tempFile = tempFile;
            this.ext = ext;
        }

        protected Boolean doInBackground(Void... voids) {
            InputStream httpConnectionStream = null;
            boolean done = false;
            URLConnection httpConnection = null;
            try {
                httpConnection = new URL(this.url).openConnection();
                httpConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 4.4; Nexus 5 Build/_BuildID_) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/30.0.0.0 Mobile Safari/537.36");
                httpConnection.addRequestProperty("Referer", "google.com");
                httpConnection.setConnectTimeout(5000);
                httpConnection.setReadTimeout(5000);
                if (httpConnection instanceof HttpURLConnection) {
                    HttpURLConnection httpURLConnection = (HttpURLConnection) httpConnection;
                    httpURLConnection.setInstanceFollowRedirects(true);
                    int status = httpURLConnection.getResponseCode();
                    if (status == 302 || status == 301 || status == 303) {
                        String newUrl = httpURLConnection.getHeaderField("Location");
                        String cookies = httpURLConnection.getHeaderField("Set-Cookie");
                        httpConnection = new URL(newUrl).openConnection();
                        httpConnection.setRequestProperty("Cookie", cookies);
                        httpConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 4.4; Nexus 5 Build/_BuildID_) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/30.0.0.0 Mobile Safari/537.36");
                        httpConnection.addRequestProperty("Referer", "google.com");
                    }
                }
                httpConnection.connect();
                httpConnectionStream = httpConnection.getInputStream();
                this.fileOutputStream = new RandomAccessFile(this.tempFile, "rws");
            } catch (Throwable e) {
                if (e instanceof UnknownHostException) {
                    this.canRetry = false;
                }
                FileLog.m611e("tmessages", e);
            }
            if (this.canRetry) {
                if (httpConnection != null) {
                    try {
                        if (httpConnection instanceof HttpURLConnection) {
                            int code = ((HttpURLConnection) httpConnection).getResponseCode();
                            if (!(code == Callback.DEFAULT_DRAG_ANIMATION_DURATION || code == 202 || code == 304)) {
                                this.canRetry = false;
                            }
                        }
                    } catch (Throwable e2) {
                        FileLog.m611e("tmessages", e2);
                    }
                }
                if (httpConnectionStream != null) {
                    try {
                        byte[] data = new byte[4096];
                        while (!isCancelled()) {
                            int read = httpConnectionStream.read(data);
                            if (read > 0) {
                                this.fileOutputStream.write(data, 0, read);
                            } else if (read == -1) {
                                done = true;
                            }
                        }
                    } catch (Throwable e22) {
                        FileLog.m611e("tmessages", e22);
                    } catch (Throwable e222) {
                        FileLog.m611e("tmessages", e222);
                    }
                }
                try {
                    if (this.fileOutputStream != null) {
                        this.fileOutputStream.close();
                        this.fileOutputStream = null;
                    }
                } catch (Throwable e2222) {
                    FileLog.m611e("tmessages", e2222);
                }
                if (httpConnectionStream != null) {
                    try {
                        httpConnectionStream.close();
                    } catch (Throwable e22222) {
                        FileLog.m611e("tmessages", e22222);
                    }
                }
            }
            return Boolean.valueOf(done);
        }

        protected void onPostExecute(Boolean result) {
            ImageLoader.this.runHttpFileLoadTasks(this, result.booleanValue() ? 2 : 1);
        }

        protected void onCancelled() {
            ImageLoader.this.runHttpFileLoadTasks(this, 2);
        }
    }

    private class HttpImageTask extends AsyncTask<Void, Void, Boolean> {
        private CacheImage cacheImage = null;
        private boolean canRetry = true;
        private RandomAccessFile fileOutputStream = null;
        private URLConnection httpConnection = null;
        private int imageSize;
        private long lastProgressTime;

        class C03953 implements Runnable {
            C03953() {
            }

            public void run() {
                ImageLoader.this.runHttpTasks(true);
            }
        }

        class C03964 implements Runnable {
            C03964() {
            }

            public void run() {
                ImageLoader.this.runHttpTasks(true);
            }
        }

        class C03985 implements Runnable {

            class C03971 implements Runnable {
                C03971() {
                }

                public void run() {
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.FileDidFailedLoad, HttpImageTask.this.cacheImage.url, Integer.valueOf(1));
                }
            }

            C03985() {
            }

            public void run() {
                ImageLoader.this.fileProgresses.remove(HttpImageTask.this.cacheImage.url);
                AndroidUtilities.runOnUIThread(new C03971());
            }
        }

        public HttpImageTask(CacheImage cacheImage, int size) {
            this.cacheImage = cacheImage;
            this.imageSize = size;
        }

        private void reportProgress(final float progress) {
            long currentTime = System.currentTimeMillis();
            if (progress == 1.0f || this.lastProgressTime == 0 || this.lastProgressTime < currentTime - 500) {
                this.lastProgressTime = currentTime;
                Utilities.stageQueue.postRunnable(new Runnable() {

                    class C03911 implements Runnable {
                        C03911() {
                        }

                        public void run() {
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.FileLoadProgressChanged, HttpImageTask.this.cacheImage.url, Float.valueOf(progress));
                        }
                    }

                    public void run() {
                        ImageLoader.this.fileProgresses.put(HttpImageTask.this.cacheImage.url, Float.valueOf(progress));
                        AndroidUtilities.runOnUIThread(new C03911());
                    }
                });
            }
        }

        protected Boolean doInBackground(Void... voids) {
            InputStream httpConnectionStream = null;
            boolean done = false;
            if (!isCancelled()) {
                try {
                    this.httpConnection = new URL(this.cacheImage.httpUrl).openConnection();
                    this.httpConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 4.4; Nexus 5 Build/_BuildID_) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/30.0.0.0 Mobile Safari/537.36");
                    this.httpConnection.addRequestProperty("Referer", "google.com");
                    this.httpConnection.setConnectTimeout(5000);
                    this.httpConnection.setReadTimeout(5000);
                    if (this.httpConnection instanceof HttpURLConnection) {
                        ((HttpURLConnection) this.httpConnection).setInstanceFollowRedirects(true);
                    }
                    if (!isCancelled()) {
                        this.httpConnection.connect();
                        httpConnectionStream = this.httpConnection.getInputStream();
                        this.fileOutputStream = new RandomAccessFile(this.cacheImage.tempFilePath, "rws");
                    }
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
            if (!isCancelled()) {
                try {
                    if (this.httpConnection != null && (this.httpConnection instanceof HttpURLConnection)) {
                        int code = ((HttpURLConnection) this.httpConnection).getResponseCode();
                        if (!(code == Callback.DEFAULT_DRAG_ANIMATION_DURATION || code == 202 || code == 304)) {
                            this.canRetry = false;
                        }
                    }
                } catch (Throwable e2) {
                    FileLog.m611e("tmessages", e2);
                }
                if (httpConnectionStream != null) {
                    try {
                        byte[] data = new byte[2048];
                        int totalLoaded = 0;
                        while (!isCancelled()) {
                            int read = httpConnectionStream.read(data);
                            if (read > 0) {
                                totalLoaded += read;
                                this.fileOutputStream.write(data, 0, read);
                                if (this.imageSize != 0) {
                                    reportProgress(((float) totalLoaded) / ((float) this.imageSize));
                                }
                            } else if (read == -1) {
                                done = true;
                                if (this.imageSize != 0) {
                                    reportProgress(1.0f);
                                }
                            }
                        }
                    } catch (Throwable e22) {
                        FileLog.m611e("tmessages", e22);
                    } catch (Throwable e222) {
                        FileLog.m611e("tmessages", e222);
                    }
                }
            }
            try {
                if (this.fileOutputStream != null) {
                    this.fileOutputStream.close();
                    this.fileOutputStream = null;
                }
            } catch (Throwable e2222) {
                FileLog.m611e("tmessages", e2222);
            }
            if (httpConnectionStream != null) {
                try {
                    httpConnectionStream.close();
                } catch (Throwable e22222) {
                    FileLog.m611e("tmessages", e22222);
                }
            }
            if (!(!done || this.cacheImage.tempFilePath == null || this.cacheImage.tempFilePath.renameTo(this.cacheImage.finalFilePath))) {
                this.cacheImage.finalFilePath = this.cacheImage.tempFilePath;
            }
            return Boolean.valueOf(done);
        }

        protected void onPostExecute(final Boolean result) {
            if (result.booleanValue() || !this.canRetry) {
                ImageLoader.this.fileDidLoaded(this.cacheImage.url, this.cacheImage.finalFilePath, 0);
            } else {
                ImageLoader.this.httpFileLoadError(this.cacheImage.url);
            }
            Utilities.stageQueue.postRunnable(new Runnable() {

                class C03931 implements Runnable {
                    C03931() {
                    }

                    public void run() {
                        if (result.booleanValue()) {
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.FileDidLoaded, HttpImageTask.this.cacheImage.url);
                            return;
                        }
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.FileDidFailedLoad, HttpImageTask.this.cacheImage.url, Integer.valueOf(2));
                    }
                }

                public void run() {
                    ImageLoader.this.fileProgresses.remove(HttpImageTask.this.cacheImage.url);
                    AndroidUtilities.runOnUIThread(new C03931());
                }
            });
            ImageLoader.this.imageLoadQueue.postRunnable(new C03953());
        }

        protected void onCancelled() {
            ImageLoader.this.imageLoadQueue.postRunnable(new C03964());
            Utilities.stageQueue.postRunnable(new C03985());
        }
    }

    private class ThumbGenerateInfo {
        private int count;
        private FileLocation fileLocation;
        private String filter;

        private ThumbGenerateInfo() {
        }
    }

    private class ThumbGenerateTask implements Runnable {
        private String filter;
        private int mediaType;
        private File originalPath;
        private FileLocation thumbLocation;

        public ThumbGenerateTask(int type, File path, FileLocation location, String f) {
            this.mediaType = type;
            this.originalPath = path;
            this.thumbLocation = location;
            this.filter = f;
        }

        private void removeTask() {
            if (this.thumbLocation != null) {
                final String name = FileLoader.getAttachFileName(this.thumbLocation);
                ImageLoader.this.imageLoadQueue.postRunnable(new Runnable() {
                    public void run() {
                        ImageLoader.this.thumbGenerateTasks.remove(name);
                    }
                });
            }
        }

        public void run() {
            try {
                if (this.thumbLocation == null) {
                    removeTask();
                    return;
                }
                final String key = this.thumbLocation.volume_id + "_" + this.thumbLocation.local_id;
                File thumbFile = new File(FileLoader.getInstance().getDirectory(4), "q_" + key + ".jpg");
                if (thumbFile.exists() || !this.originalPath.exists()) {
                    removeTask();
                    return;
                }
                int size = Math.min(180, Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y) / 4);
                Bitmap originalBitmap = null;
                if (this.mediaType == 0) {
                    originalBitmap = ImageLoader.loadBitmap(this.originalPath.toString(), null, (float) size, (float) size, false);
                } else if (this.mediaType == 2) {
                    originalBitmap = ThumbnailUtils.createVideoThumbnail(this.originalPath.toString(), 1);
                } else if (this.mediaType == 3) {
                    String path = this.originalPath.toString().toLowerCase();
                    if (path.endsWith(".jpg") || path.endsWith(".jpeg") || path.endsWith(".png") || path.endsWith(".gif")) {
                        originalBitmap = ImageLoader.loadBitmap(path, null, (float) size, (float) size, false);
                    } else {
                        removeTask();
                        return;
                    }
                }
                if (originalBitmap == null) {
                    removeTask();
                    return;
                }
                int w = originalBitmap.getWidth();
                int h = originalBitmap.getHeight();
                if (w == 0 || h == 0) {
                    removeTask();
                    return;
                }
                float scaleFactor = Math.min(((float) w) / ((float) size), ((float) h) / ((float) size));
                Bitmap scaledBitmap = Bitmaps.createScaledBitmap(originalBitmap, (int) (((float) w) / scaleFactor), (int) (((float) h) / scaleFactor), true);
                if (scaledBitmap != originalBitmap) {
                    originalBitmap.recycle();
                }
                originalBitmap = scaledBitmap;
                FileOutputStream stream = new FileOutputStream(thumbFile);
                originalBitmap.compress(CompressFormat.JPEG, 60, stream);
                stream.close();
                final BitmapDrawable bitmapDrawable = new BitmapDrawable(originalBitmap);
                AndroidUtilities.runOnUIThread(new Runnable() {
                    public void run() {
                        ThumbGenerateTask.this.removeTask();
                        String kf = key;
                        if (ThumbGenerateTask.this.filter != null) {
                            kf = kf + "@" + ThumbGenerateTask.this.filter;
                        }
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.messageThumbGenerated, bitmapDrawable, kf);
                        ImageLoader.this.memCache.put(kf, bitmapDrawable);
                    }
                });
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            } catch (Throwable e2) {
                FileLog.m611e("tmessages", e2);
                removeTask();
            }
        }
    }

    public class VMRuntimeHack {
        private Object runtime = null;
        private Method trackAllocation = null;
        private Method trackFree = null;

        public boolean trackAlloc(long size) {
            if (this.runtime == null) {
                return false;
            }
            try {
                Object res = this.trackAllocation.invoke(this.runtime, new Object[]{Long.valueOf(size)});
                if (res instanceof Boolean) {
                    return ((Boolean) res).booleanValue();
                }
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        public boolean trackFree(long size) {
            if (this.runtime == null) {
                return false;
            }
            try {
                Object res = this.trackFree.invoke(this.runtime, new Object[]{Long.valueOf(size)});
                if (res instanceof Boolean) {
                    return ((Boolean) res).booleanValue();
                }
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        public VMRuntimeHack() {
            try {
                Class cl = Class.forName("dalvik.system.VMRuntime");
                this.runtime = cl.getMethod("getRuntime", new Class[0]).invoke(null, new Object[0]);
                this.trackAllocation = cl.getMethod("trackExternalAllocation", new Class[]{Long.TYPE});
                this.trackFree = cl.getMethod("trackExternalFree", new Class[]{Long.TYPE});
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
                this.runtime = null;
                this.trackAllocation = null;
                this.trackFree = null;
            }
        }
    }

    class C14532 implements FileLoaderDelegate {
        C14532() {
        }

        public void fileUploadProgressChanged(final String location, final float progress, final boolean isEncrypted) {
            ImageLoader.this.fileProgresses.put(location, Float.valueOf(progress));
            long currentTime = System.currentTimeMillis();
            if (ImageLoader.this.lastProgressUpdateTime == 0 || ImageLoader.this.lastProgressUpdateTime < currentTime - 500) {
                ImageLoader.this.lastProgressUpdateTime = currentTime;
                AndroidUtilities.runOnUIThread(new Runnable() {
                    public void run() {
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.FileUploadProgressChanged, location, Float.valueOf(progress), Boolean.valueOf(isEncrypted));
                    }
                });
            }
        }

        public void fileDidUploaded(String location, InputFile inputFile, InputEncryptedFile inputEncryptedFile, byte[] key, byte[] iv, long totalFileSize) {
            final String str = location;
            final InputFile inputFile2 = inputFile;
            final InputEncryptedFile inputEncryptedFile2 = inputEncryptedFile;
            final byte[] bArr = key;
            final byte[] bArr2 = iv;
            final long j = totalFileSize;
            Utilities.stageQueue.postRunnable(new Runnable() {

                class C03701 implements Runnable {
                    C03701() {
                    }

                    public void run() {
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.FileDidUpload, str, inputFile2, inputEncryptedFile2, bArr, bArr2, Long.valueOf(j));
                    }
                }

                public void run() {
                    AndroidUtilities.runOnUIThread(new C03701());
                    ImageLoader.this.fileProgresses.remove(str);
                }
            });
        }

        public void fileDidFailedUpload(final String location, final boolean isEncrypted) {
            Utilities.stageQueue.postRunnable(new Runnable() {

                class C03721 implements Runnable {
                    C03721() {
                    }

                    public void run() {
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.FileDidFailUpload, location, Boolean.valueOf(isEncrypted));
                    }
                }

                public void run() {
                    AndroidUtilities.runOnUIThread(new C03721());
                    ImageLoader.this.fileProgresses.remove(location);
                }
            });
        }

        public void fileDidLoaded(final String location, final File finalFile, final int type) {
            ImageLoader.this.fileProgresses.remove(location);
            AndroidUtilities.runOnUIThread(new Runnable() {
                public void run() {
                    if (MediaController.getInstance().canSaveToGallery() && ImageLoader.this.telegramPath != null && finalFile != null && finalFile.exists() && ((location.endsWith(".mp4") || location.endsWith(".jpg")) && finalFile.toString().startsWith(ImageLoader.this.telegramPath.toString()))) {
                        AndroidUtilities.addMediaToGallery(finalFile.toString());
                    }
                    ImageLoader.this.fileDidLoaded(location, finalFile, type);
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.FileDidLoaded, location);
                }
            });
        }

        public void fileDidFailedLoad(final String location, final int canceled) {
            ImageLoader.this.fileProgresses.remove(location);
            AndroidUtilities.runOnUIThread(new Runnable() {
                public void run() {
                    ImageLoader.this.fileDidFailedLoad(location, canceled);
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.FileDidFailedLoad, location, Integer.valueOf(canceled));
                }
            });
        }

        public void fileLoadProgressChanged(final String location, final float progress) {
            ImageLoader.this.fileProgresses.put(location, Float.valueOf(progress));
            long currentTime = System.currentTimeMillis();
            if (ImageLoader.this.lastProgressUpdateTime == 0 || ImageLoader.this.lastProgressUpdateTime < currentTime - 500) {
                ImageLoader.this.lastProgressUpdateTime = currentTime;
                AndroidUtilities.runOnUIThread(new Runnable() {
                    public void run() {
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.FileLoadProgressChanged, location, Float.valueOf(progress));
                    }
                });
            }
        }
    }

    public static ImageLoader getInstance() {
        ImageLoader localInstance = Instance;
        if (localInstance == null) {
            synchronized (ImageLoader.class) {
                try {
                    localInstance = Instance;
                    if (localInstance == null) {
                        ImageLoader localInstance2 = new ImageLoader();
                        try {
                            Instance = localInstance2;
                            localInstance = localInstance2;
                        } catch (Throwable th) {
                            Throwable th2 = th;
                            localInstance = localInstance2;
                            throw th2;
                        }
                    }
                } catch (Throwable th3) {
                    th2 = th3;
                    throw th2;
                }
            }
        }
        return localInstance;
    }

    public ImageLoader() {
        this.cacheOutQueue.setPriority(1);
        this.cacheThumbOutQueue.setPriority(1);
        this.thumbGeneratingQueue.setPriority(1);
        this.imageLoadQueue.setPriority(1);
        int cacheSize = (Math.min(15, ((ActivityManager) ApplicationLoader.applicationContext.getSystemService("activity")).getMemoryClass() / 7) * 1024) * 1024;
        if (VERSION.SDK_INT < 11) {
            this.runtimeHack = new VMRuntimeHack();
            cacheSize = 3145728;
        }
        this.memCache = new LruCache(cacheSize) {
            protected int sizeOf(String key, BitmapDrawable value) {
                Bitmap b = value.getBitmap();
                if (VERSION.SDK_INT < 12) {
                    return b.getRowBytes() * b.getHeight();
                }
                return b.getByteCount();
            }

            protected void entryRemoved(boolean evicted, String key, BitmapDrawable oldValue, BitmapDrawable newValue) {
                if (ImageLoader.this.ignoreRemoval == null || key == null || !ImageLoader.this.ignoreRemoval.equals(key)) {
                    Integer count = (Integer) ImageLoader.this.bitmapUseCounts.get(key);
                    if (count == null || count.intValue() == 0) {
                        Bitmap b = oldValue.getBitmap();
                        if (ImageLoader.this.runtimeHack != null) {
                            ImageLoader.this.runtimeHack.trackAlloc((long) (b.getRowBytes() * b.getHeight()));
                        }
                        if (!b.isRecycled()) {
                            b.recycle();
                        }
                    }
                }
            }
        };
        FileLoader.getInstance().setDelegate(new C14532());
        BroadcastReceiver receiver = new C03803();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.MEDIA_BAD_REMOVAL");
        filter.addAction("android.intent.action.MEDIA_CHECKING");
        filter.addAction("android.intent.action.MEDIA_EJECT");
        filter.addAction("android.intent.action.MEDIA_MOUNTED");
        filter.addAction("android.intent.action.MEDIA_NOFS");
        filter.addAction("android.intent.action.MEDIA_REMOVED");
        filter.addAction("android.intent.action.MEDIA_SHARED");
        filter.addAction("android.intent.action.MEDIA_UNMOUNTABLE");
        filter.addAction("android.intent.action.MEDIA_UNMOUNTED");
        filter.addDataScheme("file");
        ApplicationLoader.applicationContext.registerReceiver(receiver, filter);
        HashMap<Integer, File> mediaDirs = new HashMap();
        File cachePath = AndroidUtilities.getCacheDir();
        if (!cachePath.isDirectory()) {
            try {
                cachePath.mkdirs();
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
        }
        try {
            new File(cachePath, ".nomedia").createNewFile();
        } catch (Throwable e2) {
            FileLog.m611e("tmessages", e2);
        }
        mediaDirs.put(Integer.valueOf(4), cachePath);
        FileLoader.getInstance().setMediaDirs(mediaDirs);
        this.cacheOutQueue.postRunnable(new C03824());
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.util.HashMap<java.lang.Integer, java.io.File> createMediaPaths() {
        /*
        r10 = this;
        r5 = new java.util.HashMap;
        r5.<init>();
        r1 = org.telegram.messenger.AndroidUtilities.getCacheDir();
        r7 = r1.isDirectory();
        if (r7 != 0) goto L_0x0012;
    L_0x000f:
        r1.mkdirs();	 Catch:{ Exception -> 0x0162 }
    L_0x0012:
        r7 = new java.io.File;	 Catch:{ Exception -> 0x016a }
        r8 = ".nomedia";
        r7.<init>(r1, r8);	 Catch:{ Exception -> 0x016a }
        r7.createNewFile();	 Catch:{ Exception -> 0x016a }
    L_0x001c:
        r7 = 4;
        r7 = java.lang.Integer.valueOf(r7);
        r5.put(r7, r1);
        r7 = "tmessages";
        r8 = new java.lang.StringBuilder;
        r8.<init>();
        r9 = "cache path = ";
        r8 = r8.append(r9);
        r8 = r8.append(r1);
        r8 = r8.toString();
        org.telegram.messenger.FileLog.m609e(r7, r8);
        r7 = "mounted";
        r8 = android.os.Environment.getExternalStorageState();	 Catch:{ Exception -> 0x017a }
        r7 = r7.equals(r8);	 Catch:{ Exception -> 0x017a }
        if (r7 == 0) goto L_0x0197;
    L_0x0048:
        r7 = new java.io.File;	 Catch:{ Exception -> 0x017a }
        r8 = android.os.Environment.getExternalStorageDirectory();	 Catch:{ Exception -> 0x017a }
        r9 = "Telegram";
        r7.<init>(r8, r9);	 Catch:{ Exception -> 0x017a }
        r10.telegramPath = r7;	 Catch:{ Exception -> 0x017a }
        r7 = r10.telegramPath;	 Catch:{ Exception -> 0x017a }
        r7.mkdirs();	 Catch:{ Exception -> 0x017a }
        r7 = r10.telegramPath;	 Catch:{ Exception -> 0x017a }
        r7 = r7.isDirectory();	 Catch:{ Exception -> 0x017a }
        if (r7 == 0) goto L_0x015a;
    L_0x0062:
        r4 = new java.io.File;	 Catch:{ Exception -> 0x0172 }
        r7 = r10.telegramPath;	 Catch:{ Exception -> 0x0172 }
        r8 = "Telegram Images";
        r4.<init>(r7, r8);	 Catch:{ Exception -> 0x0172 }
        r4.mkdir();	 Catch:{ Exception -> 0x0172 }
        r7 = r4.isDirectory();	 Catch:{ Exception -> 0x0172 }
        if (r7 == 0) goto L_0x009b;
    L_0x0074:
        r7 = 0;
        r7 = r10.canMoveFiles(r1, r4, r7);	 Catch:{ Exception -> 0x0172 }
        if (r7 == 0) goto L_0x009b;
    L_0x007b:
        r7 = 0;
        r7 = java.lang.Integer.valueOf(r7);	 Catch:{ Exception -> 0x0172 }
        r5.put(r7, r4);	 Catch:{ Exception -> 0x0172 }
        r7 = "tmessages";
        r8 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0172 }
        r8.<init>();	 Catch:{ Exception -> 0x0172 }
        r9 = "image path = ";
        r8 = r8.append(r9);	 Catch:{ Exception -> 0x0172 }
        r8 = r8.append(r4);	 Catch:{ Exception -> 0x0172 }
        r8 = r8.toString();	 Catch:{ Exception -> 0x0172 }
        org.telegram.messenger.FileLog.m609e(r7, r8);	 Catch:{ Exception -> 0x0172 }
    L_0x009b:
        r6 = new java.io.File;	 Catch:{ Exception -> 0x0181 }
        r7 = r10.telegramPath;	 Catch:{ Exception -> 0x0181 }
        r8 = "Telegram Video";
        r6.<init>(r7, r8);	 Catch:{ Exception -> 0x0181 }
        r6.mkdir();	 Catch:{ Exception -> 0x0181 }
        r7 = r6.isDirectory();	 Catch:{ Exception -> 0x0181 }
        if (r7 == 0) goto L_0x00d4;
    L_0x00ad:
        r7 = 2;
        r7 = r10.canMoveFiles(r1, r6, r7);	 Catch:{ Exception -> 0x0181 }
        if (r7 == 0) goto L_0x00d4;
    L_0x00b4:
        r7 = 2;
        r7 = java.lang.Integer.valueOf(r7);	 Catch:{ Exception -> 0x0181 }
        r5.put(r7, r6);	 Catch:{ Exception -> 0x0181 }
        r7 = "tmessages";
        r8 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0181 }
        r8.<init>();	 Catch:{ Exception -> 0x0181 }
        r9 = "video path = ";
        r8 = r8.append(r9);	 Catch:{ Exception -> 0x0181 }
        r8 = r8.append(r6);	 Catch:{ Exception -> 0x0181 }
        r8 = r8.toString();	 Catch:{ Exception -> 0x0181 }
        org.telegram.messenger.FileLog.m609e(r7, r8);	 Catch:{ Exception -> 0x0181 }
    L_0x00d4:
        r0 = new java.io.File;	 Catch:{ Exception -> 0x0189 }
        r7 = r10.telegramPath;	 Catch:{ Exception -> 0x0189 }
        r8 = "Telegram Audio";
        r0.<init>(r7, r8);	 Catch:{ Exception -> 0x0189 }
        r0.mkdir();	 Catch:{ Exception -> 0x0189 }
        r7 = r0.isDirectory();	 Catch:{ Exception -> 0x0189 }
        if (r7 == 0) goto L_0x0117;
    L_0x00e6:
        r7 = 1;
        r7 = r10.canMoveFiles(r1, r0, r7);	 Catch:{ Exception -> 0x0189 }
        if (r7 == 0) goto L_0x0117;
    L_0x00ed:
        r7 = new java.io.File;	 Catch:{ Exception -> 0x0189 }
        r8 = ".nomedia";
        r7.<init>(r0, r8);	 Catch:{ Exception -> 0x0189 }
        r7.createNewFile();	 Catch:{ Exception -> 0x0189 }
        r7 = 1;
        r7 = java.lang.Integer.valueOf(r7);	 Catch:{ Exception -> 0x0189 }
        r5.put(r7, r0);	 Catch:{ Exception -> 0x0189 }
        r7 = "tmessages";
        r8 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0189 }
        r8.<init>();	 Catch:{ Exception -> 0x0189 }
        r9 = "audio path = ";
        r8 = r8.append(r9);	 Catch:{ Exception -> 0x0189 }
        r8 = r8.append(r0);	 Catch:{ Exception -> 0x0189 }
        r8 = r8.toString();	 Catch:{ Exception -> 0x0189 }
        org.telegram.messenger.FileLog.m609e(r7, r8);	 Catch:{ Exception -> 0x0189 }
    L_0x0117:
        r2 = new java.io.File;	 Catch:{ Exception -> 0x0190 }
        r7 = r10.telegramPath;	 Catch:{ Exception -> 0x0190 }
        r8 = "Telegram Documents";
        r2.<init>(r7, r8);	 Catch:{ Exception -> 0x0190 }
        r2.mkdir();	 Catch:{ Exception -> 0x0190 }
        r7 = r2.isDirectory();	 Catch:{ Exception -> 0x0190 }
        if (r7 == 0) goto L_0x015a;
    L_0x0129:
        r7 = 3;
        r7 = r10.canMoveFiles(r1, r2, r7);	 Catch:{ Exception -> 0x0190 }
        if (r7 == 0) goto L_0x015a;
    L_0x0130:
        r7 = new java.io.File;	 Catch:{ Exception -> 0x0190 }
        r8 = ".nomedia";
        r7.<init>(r2, r8);	 Catch:{ Exception -> 0x0190 }
        r7.createNewFile();	 Catch:{ Exception -> 0x0190 }
        r7 = 3;
        r7 = java.lang.Integer.valueOf(r7);	 Catch:{ Exception -> 0x0190 }
        r5.put(r7, r2);	 Catch:{ Exception -> 0x0190 }
        r7 = "tmessages";
        r8 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0190 }
        r8.<init>();	 Catch:{ Exception -> 0x0190 }
        r9 = "documents path = ";
        r8 = r8.append(r9);	 Catch:{ Exception -> 0x0190 }
        r8 = r8.append(r2);	 Catch:{ Exception -> 0x0190 }
        r8 = r8.toString();	 Catch:{ Exception -> 0x0190 }
        org.telegram.messenger.FileLog.m609e(r7, r8);	 Catch:{ Exception -> 0x0190 }
    L_0x015a:
        r7 = org.telegram.messenger.MediaController.getInstance();	 Catch:{ Exception -> 0x017a }
        r7.checkSaveToGalleryFiles();	 Catch:{ Exception -> 0x017a }
    L_0x0161:
        return r5;
    L_0x0162:
        r3 = move-exception;
        r7 = "tmessages";
        org.telegram.messenger.FileLog.m611e(r7, r3);
        goto L_0x0012;
    L_0x016a:
        r3 = move-exception;
        r7 = "tmessages";
        org.telegram.messenger.FileLog.m611e(r7, r3);
        goto L_0x001c;
    L_0x0172:
        r3 = move-exception;
        r7 = "tmessages";
        org.telegram.messenger.FileLog.m611e(r7, r3);	 Catch:{ Exception -> 0x017a }
        goto L_0x009b;
    L_0x017a:
        r3 = move-exception;
        r7 = "tmessages";
        org.telegram.messenger.FileLog.m611e(r7, r3);
        goto L_0x0161;
    L_0x0181:
        r3 = move-exception;
        r7 = "tmessages";
        org.telegram.messenger.FileLog.m611e(r7, r3);	 Catch:{ Exception -> 0x017a }
        goto L_0x00d4;
    L_0x0189:
        r3 = move-exception;
        r7 = "tmessages";
        org.telegram.messenger.FileLog.m611e(r7, r3);	 Catch:{ Exception -> 0x017a }
        goto L_0x0117;
    L_0x0190:
        r3 = move-exception;
        r7 = "tmessages";
        org.telegram.messenger.FileLog.m611e(r7, r3);	 Catch:{ Exception -> 0x017a }
        goto L_0x015a;
    L_0x0197:
        r7 = "tmessages";
        r8 = "this Android can't rename files";
        org.telegram.messenger.FileLog.m609e(r7, r8);	 Catch:{ Exception -> 0x017a }
        goto L_0x015a;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.ImageLoader.createMediaPaths():java.util.HashMap<java.lang.Integer, java.io.File>");
    }

    private boolean canMoveFiles(File from, File to, int type) {
        File srcFile;
        Throwable e;
        Throwable th;
        RandomAccessFile file = null;
        File srcFile2 = null;
        File dstFile = null;
        if (type == 0) {
            try {
                srcFile = new File(from, "000000000_999999_temp.jpg");
                try {
                    dstFile = new File(to, "000000000_999999.jpg");
                    srcFile2 = srcFile;
                } catch (Exception e2) {
                    e = e2;
                    srcFile2 = srcFile;
                    try {
                        FileLog.m611e("tmessages", e);
                        if (file != null) {
                            try {
                                file.close();
                            } catch (Throwable e3) {
                                FileLog.m611e("tmessages", e3);
                            }
                        }
                        return false;
                    } catch (Throwable th2) {
                        th = th2;
                        if (file != null) {
                            try {
                                file.close();
                            } catch (Throwable e32) {
                                FileLog.m611e("tmessages", e32);
                            }
                        }
                        throw th;
                    }
                }
            } catch (Exception e4) {
                e32 = e4;
                FileLog.m611e("tmessages", e32);
                if (file != null) {
                    file.close();
                }
                return false;
            }
        } else if (type == 3) {
            srcFile = new File(from, "000000000_999999_temp.doc");
            dstFile = new File(to, "000000000_999999.doc");
            srcFile2 = srcFile;
        } else if (type == 1) {
            srcFile = new File(from, "000000000_999999_temp.ogg");
            dstFile = new File(to, "000000000_999999.ogg");
            srcFile2 = srcFile;
        } else if (type == 2) {
            srcFile = new File(from, "000000000_999999_temp.mp4");
            dstFile = new File(to, "000000000_999999.mp4");
            srcFile2 = srcFile;
        }
        byte[] buffer = new byte[1024];
        srcFile2.createNewFile();
        RandomAccessFile file2 = new RandomAccessFile(srcFile2, "rws");
        try {
            file2.write(buffer);
            file2.close();
            file = null;
            boolean canRename = srcFile2.renameTo(dstFile);
            srcFile2.delete();
            dstFile.delete();
            if (!canRename) {
                if (file != null) {
                    try {
                        file.close();
                    } catch (Throwable e322) {
                        FileLog.m611e("tmessages", e322);
                    }
                }
                return false;
            } else if (file == null) {
                return true;
            } else {
                try {
                    file.close();
                    return true;
                } catch (Throwable e3222) {
                    FileLog.m611e("tmessages", e3222);
                    return true;
                }
            }
        } catch (Exception e5) {
            e3222 = e5;
            file = file2;
            FileLog.m611e("tmessages", e3222);
            if (file != null) {
                file.close();
            }
            return false;
        } catch (Throwable th3) {
            th = th3;
            file = file2;
            if (file != null) {
                file.close();
            }
            throw th;
        }
    }

    public Float getFileProgress(String location) {
        if (location == null) {
            return null;
        }
        return (Float) this.fileProgresses.get(location);
    }

    private void performReplace(String oldKey, String newKey) {
        BitmapDrawable b = this.memCache.get(oldKey);
        if (b != null) {
            this.ignoreRemoval = oldKey;
            this.memCache.remove(oldKey);
            this.memCache.put(newKey, b);
            this.ignoreRemoval = null;
        }
        Integer val = (Integer) this.bitmapUseCounts.get(oldKey);
        if (val != null) {
            this.bitmapUseCounts.put(newKey, val);
            this.bitmapUseCounts.remove(oldKey);
        }
    }

    public void incrementUseCount(String key) {
        Integer count = (Integer) this.bitmapUseCounts.get(key);
        if (count == null) {
            this.bitmapUseCounts.put(key, Integer.valueOf(1));
        } else {
            this.bitmapUseCounts.put(key, Integer.valueOf(count.intValue() + 1));
        }
    }

    public boolean decrementUseCount(String key) {
        Integer count = (Integer) this.bitmapUseCounts.get(key);
        if (count == null) {
            return true;
        }
        if (count.intValue() == 1) {
            this.bitmapUseCounts.remove(key);
            return true;
        }
        this.bitmapUseCounts.put(key, Integer.valueOf(count.intValue() - 1));
        return false;
    }

    public void removeImage(String key) {
        this.bitmapUseCounts.remove(key);
        this.memCache.remove(key);
    }

    public boolean isInCache(String key) {
        return this.memCache.get(key) != null;
    }

    public void clearMemory() {
        this.memCache.evictAll();
    }

    private void removeFromWaitingForThumb(Integer TAG) {
        String location = (String) this.waitingForQualityThumbByTag.get(TAG);
        if (location != null) {
            ThumbGenerateInfo info = (ThumbGenerateInfo) this.waitingForQualityThumb.get(location);
            if (info != null) {
                info.count = info.count - 1;
                if (info.count == 0) {
                    this.waitingForQualityThumb.remove(location);
                }
            }
            this.waitingForQualityThumbByTag.remove(TAG);
        }
    }

    public void cancelLoadingForImageReceiver(final ImageReceiver imageReceiver, final int type) {
        if (imageReceiver != null) {
            this.imageLoadQueue.postRunnable(new Runnable() {
                public void run() {
                    int start = 0;
                    int count = 2;
                    if (type == 1) {
                        count = 1;
                    } else if (type == 2) {
                        start = 1;
                    }
                    int a = start;
                    while (a < count) {
                        Integer TAG = imageReceiver.getTag(a == 0);
                        if (a == 0) {
                            ImageLoader.this.removeFromWaitingForThumb(TAG);
                        }
                        if (TAG != null) {
                            CacheImage ei = (CacheImage) ImageLoader.this.imageLoadingByTag.get(TAG);
                            if (ei != null) {
                                ei.removeImageReceiver(imageReceiver);
                            }
                        }
                        a++;
                    }
                }
            });
        }
    }

    public BitmapDrawable getImageFromMemory(String key) {
        return this.memCache.get(key);
    }

    public BitmapDrawable getImageFromMemory(TLObject fileLocation, String httpUrl, String filter) {
        if (fileLocation == null && httpUrl == null) {
            return null;
        }
        String key = null;
        if (httpUrl != null) {
            key = Utilities.MD5(httpUrl);
        } else if (fileLocation instanceof FileLocation) {
            FileLocation location = (FileLocation) fileLocation;
            key = location.volume_id + "_" + location.local_id;
        } else if (fileLocation instanceof Document) {
            Document location2 = (Document) fileLocation;
            key = location2.dc_id + "_" + location2.id;
        }
        if (filter != null) {
            key = key + "@" + filter;
        }
        return this.memCache.get(key);
    }

    public void replaceImageInCache(final String oldKey, final String newKey, final FileLocation newLocation) {
        AndroidUtilities.runOnUIThread(new Runnable() {
            public void run() {
                ArrayList<String> arr = ImageLoader.this.memCache.getFilterKeys(oldKey);
                if (arr != null) {
                    Iterator i$ = arr.iterator();
                    while (i$.hasNext()) {
                        String filter = (String) i$.next();
                        ImageLoader.this.performReplace(oldKey + "@" + filter, newKey + "@" + filter);
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.didReplacedPhotoInMemCache, oldK, newK, newLocation);
                    }
                    return;
                }
                ImageLoader.this.performReplace(oldKey, newKey);
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.didReplacedPhotoInMemCache, oldKey, newKey, newLocation);
            }
        });
    }

    public void putImageToCache(BitmapDrawable bitmap, String key) {
        this.memCache.put(key, bitmap);
    }

    private void generateThumb(int mediaType, File originalPath, FileLocation thumbLocation, String filter) {
        if ((mediaType == 0 || mediaType == 2 || mediaType == 3) && originalPath != null && thumbLocation != null) {
            if (((ThumbGenerateTask) this.thumbGenerateTasks.get(FileLoader.getAttachFileName(thumbLocation))) == null) {
                this.thumbGeneratingQueue.postRunnable(new ThumbGenerateTask(mediaType, originalPath, thumbLocation, filter));
            }
        }
    }

    private void createLoadOperationForImageReceiver(ImageReceiver imageReceiver, String key, String url, String ext, TLObject imageLocation, String httpLocation, String filter, int size, boolean cacheOnly, int thumb) {
        if (imageReceiver != null && url != null && key != null) {
            Integer TAG = imageReceiver.getTag(thumb != 0);
            if (TAG == null) {
                TAG = Integer.valueOf(this.lastImageNum);
                imageReceiver.setTag(TAG, thumb != 0);
                this.lastImageNum++;
                if (this.lastImageNum == ConnectionsManager.DEFAULT_DATACENTER_ID) {
                    this.lastImageNum = 0;
                }
            }
            final Integer finalTag = TAG;
            final boolean finalIsNeedsQualityThumb = imageReceiver.isNeedsQualityThumb();
            final MessageObject parentMessageObject = imageReceiver.getParentMessageObject();
            final boolean shouldGenerateQualityThumb = imageReceiver.isShouldGenerateQualityThumb();
            final int i = thumb;
            final String str = url;
            final String str2 = key;
            final ImageReceiver imageReceiver2 = imageReceiver;
            final String str3 = httpLocation;
            final TLObject tLObject = imageLocation;
            final String str4 = filter;
            final boolean z = cacheOnly;
            final int i2 = size;
            final String str5 = ext;
            this.imageLoadQueue.postRunnable(new Runnable() {
                public void run() {
                    boolean added = false;
                    if (i != 2) {
                        CacheImage alreadyLoadingUrl = (CacheImage) ImageLoader.this.imageLoadingByUrl.get(str);
                        CacheImage alreadyLoadingCache = (CacheImage) ImageLoader.this.imageLoadingByKeys.get(str2);
                        CacheImage alreadyLoadingImage = (CacheImage) ImageLoader.this.imageLoadingByTag.get(finalTag);
                        if (alreadyLoadingImage != null) {
                            if (alreadyLoadingImage == alreadyLoadingUrl || alreadyLoadingImage == alreadyLoadingCache) {
                                added = true;
                            } else {
                                alreadyLoadingImage.removeImageReceiver(imageReceiver2);
                            }
                        }
                        if (!(added || alreadyLoadingCache == null)) {
                            alreadyLoadingCache.addImageReceiver(imageReceiver2);
                            added = true;
                        }
                        if (!(added || alreadyLoadingUrl == null)) {
                            alreadyLoadingUrl.addImageReceiver(imageReceiver2);
                            added = true;
                        }
                    }
                    if (!added) {
                        boolean onlyCache = false;
                        File cacheFile = null;
                        if (str3 != null) {
                            if (!str3.startsWith("http")) {
                                onlyCache = true;
                                int idx;
                                if (str3.startsWith("thumb://")) {
                                    idx = str3.indexOf(":", 8);
                                    if (idx >= 0) {
                                        cacheFile = new File(str3.substring(idx + 1));
                                    }
                                } else if (str3.startsWith("vthumb://")) {
                                    idx = str3.indexOf(":", 9);
                                    if (idx >= 0) {
                                        cacheFile = new File(str3.substring(idx + 1));
                                    }
                                } else {
                                    cacheFile = new File(str3);
                                }
                            }
                        } else if (i != 0) {
                            if (finalIsNeedsQualityThumb) {
                                cacheFile = new File(FileLoader.getInstance().getDirectory(4), "q_" + str);
                                if (!cacheFile.exists()) {
                                    cacheFile = null;
                                }
                            }
                            if (parentMessageObject != null) {
                                File attachPath = null;
                                if (parentMessageObject.messageOwner.attachPath != null && parentMessageObject.messageOwner.attachPath.length() > 0) {
                                    attachPath = new File(parentMessageObject.messageOwner.attachPath);
                                    if (!attachPath.exists()) {
                                        attachPath = null;
                                    }
                                }
                                if (attachPath == null) {
                                    attachPath = FileLoader.getPathToMessage(parentMessageObject.messageOwner);
                                }
                                if (finalIsNeedsQualityThumb && cacheFile == null) {
                                    String location = parentMessageObject.getFileName();
                                    ThumbGenerateInfo info = (ThumbGenerateInfo) ImageLoader.this.waitingForQualityThumb.get(location);
                                    if (info == null) {
                                        info = new ThumbGenerateInfo();
                                        info.fileLocation = (TL_fileLocation) tLObject;
                                        info.filter = str4;
                                        ImageLoader.this.waitingForQualityThumb.put(location, info);
                                    }
                                    info.count = info.count + 1;
                                    ImageLoader.this.waitingForQualityThumbByTag.put(finalTag, location);
                                }
                                if (attachPath.exists() && shouldGenerateQualityThumb) {
                                    ImageLoader.this.generateThumb(parentMessageObject.getFileType(), attachPath, (TL_fileLocation) tLObject, str4);
                                }
                            }
                        }
                        if (i != 2) {
                            if (cacheFile == null) {
                                if (z || i2 == 0 || str3 != null) {
                                    cacheFile = new File(FileLoader.getInstance().getDirectory(4), str);
                                } else {
                                    cacheFile = new File(FileLoader.getInstance().getDirectory(0), str);
                                }
                            }
                            CacheImage img = new CacheImage();
                            img.thumb = i != 0;
                            img.key = str2;
                            img.filter = str4;
                            img.httpUrl = str3;
                            img.ext = str5;
                            img.addImageReceiver(imageReceiver2);
                            if (onlyCache || cacheFile.exists()) {
                                img.finalFilePath = cacheFile;
                                img.cacheTask = new CacheOutTask(img);
                                ImageLoader.this.imageLoadingByKeys.put(str2, img);
                                if (i != 0) {
                                    ImageLoader.this.cacheThumbOutQueue.postRunnable(img.cacheTask);
                                    return;
                                } else {
                                    ImageLoader.this.cacheOutQueue.postRunnable(img.cacheTask);
                                    return;
                                }
                            }
                            img.url = str;
                            img.location = tLObject;
                            ImageLoader.this.imageLoadingByUrl.put(str, img);
                            if (str3 != null) {
                                img.tempFilePath = new File(FileLoader.getInstance().getDirectory(4), Utilities.MD5(str3) + "_temp.jpg");
                                img.finalFilePath = cacheFile;
                                img.httpTask = new HttpImageTask(img, i2);
                                ImageLoader.this.httpTasks.add(img.httpTask);
                                ImageLoader.this.runHttpTasks(false);
                            } else if (tLObject instanceof FileLocation) {
                                FileLocation location2 = (FileLocation) tLObject;
                                FileLoader instance = FileLoader.getInstance();
                                String str = str5;
                                int i = i2;
                                boolean z = i2 == 0 || location2.key != null || z;
                                instance.loadFile(location2, str, i, z);
                            } else if (tLObject instanceof Document) {
                                FileLoader.getInstance().loadFile((Document) tLObject, true, true);
                            }
                        }
                    }
                }
            });
        }
    }

    public void loadImageForImageReceiver(ImageReceiver imageReceiver) {
        if (imageReceiver != null) {
            BitmapDrawable bitmapDrawable;
            String key = imageReceiver.getKey();
            if (key != null) {
                bitmapDrawable = this.memCache.get(key);
                if (bitmapDrawable != null) {
                    cancelLoadingForImageReceiver(imageReceiver, 0);
                    if (!imageReceiver.isForcePreview()) {
                        imageReceiver.setImageBitmapByKey(bitmapDrawable, key, false, true);
                        return;
                    }
                }
            }
            boolean thumbSet = false;
            String thumbKey = imageReceiver.getThumbKey();
            if (thumbKey != null) {
                bitmapDrawable = this.memCache.get(thumbKey);
                if (bitmapDrawable != null) {
                    imageReceiver.setImageBitmapByKey(bitmapDrawable, thumbKey, true, true);
                    cancelLoadingForImageReceiver(imageReceiver, 1);
                    thumbSet = true;
                }
            }
            TLObject thumbLocation = imageReceiver.getThumbLocation();
            TLObject imageLocation = imageReceiver.getImageLocation();
            String httpLocation = imageReceiver.getHttpImageLocation();
            boolean saveImageToCache = false;
            String url = null;
            String thumbUrl = null;
            key = null;
            thumbKey = null;
            String ext = imageReceiver.getExt();
            if (ext == null) {
                ext = "jpg";
            }
            if (httpLocation != null) {
                key = Utilities.MD5(httpLocation);
                url = key + "." + getHttpUrlExtension(httpLocation);
            } else if (imageLocation != null) {
                if (imageLocation instanceof FileLocation) {
                    FileLocation location = (FileLocation) imageLocation;
                    key = location.volume_id + "_" + location.local_id;
                    url = key + "." + ext;
                    if (!(imageReceiver.getExt() == null && location.key == null && (location.volume_id != -2147483648L || location.local_id >= 0))) {
                        saveImageToCache = true;
                    }
                } else if (imageLocation instanceof Document) {
                    Document location2 = (Document) imageLocation;
                    if (location2.id != 0 && location2.dc_id != 0) {
                        key = location2.dc_id + "_" + location2.id;
                        String docExt = FileLoader.getDocumentFileName(location2);
                        if (docExt != null) {
                            int idx = docExt.lastIndexOf(".");
                            if (idx != -1) {
                                docExt = docExt.substring(idx);
                                if (docExt.length() <= 1) {
                                    docExt = "";
                                }
                                url = key + docExt;
                                if (null != null) {
                                    thumbUrl = null + "." + ext;
                                }
                                saveImageToCache = true;
                            }
                        }
                        docExt = "";
                        url = key + docExt;
                        if (null != null) {
                            thumbUrl = null + "." + ext;
                        }
                        saveImageToCache = true;
                    } else {
                        return;
                    }
                }
                if (imageLocation == thumbLocation) {
                    imageLocation = null;
                    key = null;
                    url = null;
                }
            }
            if (thumbLocation != null) {
                thumbKey = thumbLocation.volume_id + "_" + thumbLocation.local_id;
                thumbUrl = thumbKey + "." + ext;
            }
            String filter = imageReceiver.getFilter();
            String thumbFilter = imageReceiver.getThumbFilter();
            if (!(key == null || filter == null)) {
                key = key + "@" + filter;
            }
            if (!(thumbKey == null || thumbFilter == null)) {
                thumbKey = thumbKey + "@" + thumbFilter;
            }
            if (httpLocation != null) {
                createLoadOperationForImageReceiver(imageReceiver, key, url, ext, null, httpLocation, filter, 0, true, null);
                return;
            }
            createLoadOperationForImageReceiver(imageReceiver, thumbKey, thumbUrl, ext, thumbLocation, null, thumbFilter, 0, true, thumbSet ? 2 : 1);
            int size = imageReceiver.getSize();
            boolean z = saveImageToCache || imageReceiver.getCacheOnly();
            createLoadOperationForImageReceiver(imageReceiver, key, url, ext, imageLocation, null, filter, size, z, 0);
        }
    }

    private void httpFileLoadError(final String location) {
        this.imageLoadQueue.postRunnable(new Runnable() {
            public void run() {
                CacheImage img = (CacheImage) ImageLoader.this.imageLoadingByUrl.get(location);
                if (img != null) {
                    HttpImageTask oldTask = img.httpTask;
                    img.httpTask = new HttpImageTask(oldTask.cacheImage, oldTask.imageSize);
                    ImageLoader.this.httpTasks.add(img.httpTask);
                    ImageLoader.this.runHttpTasks(false);
                }
            }
        });
    }

    private void fileDidLoaded(final String location, final File finalFile, final int type) {
        this.imageLoadQueue.postRunnable(new Runnable() {
            public void run() {
                ThumbGenerateInfo info = (ThumbGenerateInfo) ImageLoader.this.waitingForQualityThumb.get(location);
                if (info != null) {
                    ImageLoader.this.generateThumb(type, finalFile, info.fileLocation, info.filter);
                    ImageLoader.this.waitingForQualityThumb.remove(location);
                }
                CacheImage img = (CacheImage) ImageLoader.this.imageLoadingByUrl.get(location);
                if (img != null) {
                    ImageLoader.this.imageLoadingByUrl.remove(location);
                    CacheOutTask task = null;
                    Iterator i$ = img.imageReceiverArray.iterator();
                    while (i$.hasNext()) {
                        ImageReceiver imageReceiver = (ImageReceiver) i$.next();
                        CacheImage cacheImage = (CacheImage) ImageLoader.this.imageLoadingByKeys.get(img.key);
                        if (cacheImage == null) {
                            cacheImage = new CacheImage();
                            cacheImage.finalFilePath = finalFile;
                            cacheImage.key = img.key;
                            cacheImage.httpUrl = img.httpUrl;
                            cacheImage.thumb = img.thumb;
                            cacheImage.ext = img.ext;
                            task = new CacheOutTask(cacheImage);
                            cacheImage.cacheTask = task;
                            cacheImage.filter = img.filter;
                            ImageLoader.this.imageLoadingByKeys.put(cacheImage.key, cacheImage);
                        }
                        cacheImage.addImageReceiver(imageReceiver);
                    }
                    if (task == null) {
                        return;
                    }
                    if (img.thumb) {
                        ImageLoader.this.cacheThumbOutQueue.postRunnable(task);
                    } else {
                        ImageLoader.this.cacheOutQueue.postRunnable(task);
                    }
                }
            }
        });
    }

    private void fileDidFailedLoad(final String location, int canceled) {
        if (canceled != 1) {
            this.imageLoadQueue.postRunnable(new Runnable() {
                public void run() {
                    CacheImage img = (CacheImage) ImageLoader.this.imageLoadingByUrl.get(location);
                    if (img != null) {
                        img.setImageAndClear(null);
                    }
                }
            });
        }
    }

    private void runHttpTasks(boolean complete) {
        if (complete) {
            this.currentHttpTasksCount--;
        }
        while (this.currentHttpTasksCount < 1 && !this.httpTasks.isEmpty()) {
            HttpImageTask task = (HttpImageTask) this.httpTasks.poll();
            if (VERSION.SDK_INT >= 11) {
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[]{null, null, null});
            } else {
                task.execute(new Void[]{null, null, null});
            }
            this.currentHttpTasksCount++;
        }
    }

    public void loadHttpFile(String url, String extension) {
        if (url != null && url.length() != 0 && !this.httpFileLoadTasksByKeys.containsKey(url)) {
            String ext = extension;
            if (ext == null) {
                int idx = url.lastIndexOf(".");
                if (idx != -1) {
                    ext = url.substring(idx + 1);
                }
                if (ext == null || ext.length() == 0 || ext.length() > 4) {
                    ext = "jpg";
                }
            }
            File file = new File(FileLoader.getInstance().getDirectory(4), Utilities.MD5(url) + "_temp." + ext);
            file.delete();
            HttpFileTask task = new HttpFileTask(url, file, ext);
            this.httpFileLoadTasks.add(task);
            this.httpFileLoadTasksByKeys.put(url, task);
            runHttpFileLoadTasks(null, 0);
        }
    }

    public void cancelLoadHttpFile(String url) {
        HttpFileTask task = (HttpFileTask) this.httpFileLoadTasksByKeys.get(url);
        if (task != null) {
            task.cancel(true);
            this.httpFileLoadTasksByKeys.remove(url);
            this.httpFileLoadTasks.remove(task);
        }
        Runnable runnable = (Runnable) this.retryHttpsTasks.get(url);
        if (runnable != null) {
            AndroidUtilities.cancelRunOnUIThread(runnable);
        }
        runHttpFileLoadTasks(null, 0);
    }

    private void runHttpFileLoadTasks(final HttpFileTask oldTask, final int reason) {
        AndroidUtilities.runOnUIThread(new Runnable() {
            public void run() {
                if (oldTask != null) {
                    ImageLoader.this.currentHttpFileLoadTasksCount = ImageLoader.this.currentHttpFileLoadTasksCount - 1;
                }
                if (oldTask != null) {
                    if (reason == 1) {
                        if (oldTask.canRetry) {
                            final HttpFileTask newTask = new HttpFileTask(oldTask.url, oldTask.tempFile, oldTask.ext);
                            Runnable runnable = new Runnable() {
                                public void run() {
                                    ImageLoader.this.httpFileLoadTasks.add(newTask);
                                    ImageLoader.this.runHttpFileLoadTasks(null, 0);
                                }
                            };
                            ImageLoader.this.retryHttpsTasks.put(oldTask.url, runnable);
                            AndroidUtilities.runOnUIThread(runnable, 1000);
                        } else {
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.httpFileDidFailedLoad, oldTask.url);
                        }
                    } else if (reason == 2) {
                        ImageLoader.this.httpFileLoadTasksByKeys.remove(oldTask.url);
                        File file = new File(FileLoader.getInstance().getDirectory(4), Utilities.MD5(oldTask.url) + "." + oldTask.ext);
                        String result = oldTask.tempFile.renameTo(file) ? file.toString() : oldTask.tempFile.toString();
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.httpFileDidLoaded, oldTask.url, result);
                    }
                }
                while (ImageLoader.this.currentHttpFileLoadTasksCount < 2 && !ImageLoader.this.httpFileLoadTasks.isEmpty()) {
                    HttpFileTask task = (HttpFileTask) ImageLoader.this.httpFileLoadTasks.poll();
                    if (VERSION.SDK_INT >= 11) {
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[]{null, null, null});
                    } else {
                        task.execute(new Void[]{null, null, null});
                    }
                    ImageLoader.this.currentHttpFileLoadTasksCount = ImageLoader.this.currentHttpFileLoadTasksCount + 1;
                }
            }
        });
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static android.graphics.Bitmap loadBitmap(java.lang.String r23, android.net.Uri r24, float r25, float r26, boolean r27) {
        /*
        r8 = new android.graphics.BitmapFactory$Options;
        r8.<init>();
        r2 = 1;
        r8.inJustDecodeBounds = r2;
        r14 = 0;
        r19 = 0;
        if (r23 != 0) goto L_0x0026;
    L_0x000d:
        if (r24 == 0) goto L_0x0026;
    L_0x000f:
        r2 = r24.getScheme();
        if (r2 == 0) goto L_0x0026;
    L_0x0015:
        r15 = 0;
        r2 = r24.getScheme();
        r3 = "file";
        r2 = r2.contains(r3);
        if (r2 == 0) goto L_0x00a9;
    L_0x0022:
        r23 = r24.getPath();
    L_0x0026:
        if (r23 == 0) goto L_0x00b7;
    L_0x0028:
        r0 = r23;
        android.graphics.BitmapFactory.decodeFile(r0, r8);
    L_0x002d:
        r2 = r8.outWidth;
        r0 = (float) r2;
        r21 = r0;
        r2 = r8.outHeight;
        r0 = (float) r2;
        r20 = r0;
        if (r27 == 0) goto L_0x00da;
    L_0x0039:
        r2 = r21 / r25;
        r3 = r20 / r26;
        r22 = java.lang.Math.max(r2, r3);
    L_0x0041:
        r2 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        r2 = (r22 > r2 ? 1 : (r22 == r2 ? 0 : -1));
        if (r2 >= 0) goto L_0x0049;
    L_0x0047:
        r22 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
    L_0x0049:
        r2 = 0;
        r8.inJustDecodeBounds = r2;
        r0 = r22;
        r2 = (int) r0;
        r8.inSampleSize = r2;
        r2 = android.os.Build.VERSION.SDK_INT;
        r3 = 14;
        if (r2 < r3) goto L_0x00e4;
    L_0x0057:
        r2 = android.os.Build.VERSION.SDK_INT;
        r3 = 21;
        if (r2 >= r3) goto L_0x00e4;
    L_0x005d:
        r2 = 1;
    L_0x005e:
        r8.inPurgeable = r2;
        r13 = 0;
        if (r23 == 0) goto L_0x00e7;
    L_0x0063:
        r13 = r23;
    L_0x0065:
        r6 = 0;
        if (r13 == 0) goto L_0x007e;
    L_0x0068:
        r12 = new android.media.ExifInterface;	 Catch:{ Throwable -> 0x01a9 }
        r12.<init>(r13);	 Catch:{ Throwable -> 0x01a9 }
        r2 = "Orientation";
        r3 = 1;
        r18 = r12.getAttributeInt(r2, r3);	 Catch:{ Throwable -> 0x01a9 }
        r16 = new android.graphics.Matrix;	 Catch:{ Throwable -> 0x01a9 }
        r16.<init>();	 Catch:{ Throwable -> 0x01a9 }
        switch(r18) {
            case 3: goto L_0x0101;
            case 4: goto L_0x007c;
            case 5: goto L_0x007c;
            case 6: goto L_0x00ef;
            case 7: goto L_0x007c;
            case 8: goto L_0x010a;
            default: goto L_0x007c;
        };
    L_0x007c:
        r6 = r16;
    L_0x007e:
        r1 = 0;
        if (r23 == 0) goto L_0x0155;
    L_0x0081:
        r0 = r23;
        r1 = android.graphics.BitmapFactory.decodeFile(r0, r8);	 Catch:{ Throwable -> 0x0113 }
        if (r1 == 0) goto L_0x00a8;
    L_0x0089:
        r2 = r8.inPurgeable;	 Catch:{ Throwable -> 0x0113 }
        if (r2 == 0) goto L_0x0090;
    L_0x008d:
        org.telegram.messenger.Utilities.pinBitmap(r1);	 Catch:{ Throwable -> 0x0113 }
    L_0x0090:
        r2 = 0;
        r3 = 0;
        r4 = r1.getWidth();	 Catch:{ Throwable -> 0x0113 }
        r5 = r1.getHeight();	 Catch:{ Throwable -> 0x0113 }
        r7 = 1;
        r17 = org.telegram.messenger.Bitmaps.createBitmap(r1, r2, r3, r4, r5, r6, r7);	 Catch:{ Throwable -> 0x0113 }
        r0 = r17;
        if (r0 == r1) goto L_0x00a8;
    L_0x00a3:
        r1.recycle();	 Catch:{ Throwable -> 0x0113 }
        r1 = r17;
    L_0x00a8:
        return r1;
    L_0x00a9:
        r23 = org.telegram.messenger.AndroidUtilities.getPath(r24);	 Catch:{ Throwable -> 0x00af }
        goto L_0x0026;
    L_0x00af:
        r9 = move-exception;
        r2 = "tmessages";
        org.telegram.messenger.FileLog.m611e(r2, r9);
        goto L_0x0026;
    L_0x00b7:
        if (r24 == 0) goto L_0x002d;
    L_0x00b9:
        r11 = 0;
        r2 = org.telegram.messenger.ApplicationLoader.applicationContext;	 Catch:{ Throwable -> 0x00d2 }
        r2 = r2.getContentResolver();	 Catch:{ Throwable -> 0x00d2 }
        r3 = "r";
        r0 = r24;
        r19 = r2.openFileDescriptor(r0, r3);	 Catch:{ Throwable -> 0x00d2 }
        r14 = r19.getFileDescriptor();	 Catch:{ Throwable -> 0x00d2 }
        r2 = 0;
        android.graphics.BitmapFactory.decodeFileDescriptor(r14, r2, r8);	 Catch:{ Throwable -> 0x00d2 }
        goto L_0x002d;
    L_0x00d2:
        r9 = move-exception;
        r2 = "tmessages";
        org.telegram.messenger.FileLog.m611e(r2, r9);
        r1 = 0;
        goto L_0x00a8;
    L_0x00da:
        r2 = r21 / r25;
        r3 = r20 / r26;
        r22 = java.lang.Math.min(r2, r3);
        goto L_0x0041;
    L_0x00e4:
        r2 = 0;
        goto L_0x005e;
    L_0x00e7:
        if (r24 == 0) goto L_0x0065;
    L_0x00e9:
        r13 = org.telegram.messenger.AndroidUtilities.getPath(r24);
        goto L_0x0065;
    L_0x00ef:
        r2 = 1119092736; // 0x42b40000 float:90.0 double:5.529052754E-315;
        r0 = r16;
        r0.postRotate(r2);	 Catch:{ Throwable -> 0x00f7 }
        goto L_0x007c;
    L_0x00f7:
        r9 = move-exception;
        r6 = r16;
    L_0x00fa:
        r2 = "tmessages";
        org.telegram.messenger.FileLog.m611e(r2, r9);
        goto L_0x007e;
    L_0x0101:
        r2 = 1127481344; // 0x43340000 float:180.0 double:5.570497984E-315;
        r0 = r16;
        r0.postRotate(r2);	 Catch:{ Throwable -> 0x00f7 }
        goto L_0x007c;
    L_0x010a:
        r2 = 1132920832; // 0x43870000 float:270.0 double:5.597372625E-315;
        r0 = r16;
        r0.postRotate(r2);	 Catch:{ Throwable -> 0x00f7 }
        goto L_0x007c;
    L_0x0113:
        r9 = move-exception;
        r2 = "tmessages";
        org.telegram.messenger.FileLog.m611e(r2, r9);
        r2 = getInstance();
        r2.clearMemory();
        if (r1 != 0) goto L_0x0131;
    L_0x0122:
        r0 = r23;
        r1 = android.graphics.BitmapFactory.decodeFile(r0, r8);	 Catch:{ Throwable -> 0x014d }
        if (r1 == 0) goto L_0x0131;
    L_0x012a:
        r2 = r8.inPurgeable;	 Catch:{ Throwable -> 0x014d }
        if (r2 == 0) goto L_0x0131;
    L_0x012e:
        org.telegram.messenger.Utilities.pinBitmap(r1);	 Catch:{ Throwable -> 0x014d }
    L_0x0131:
        if (r1 == 0) goto L_0x00a8;
    L_0x0133:
        r2 = 0;
        r3 = 0;
        r4 = r1.getWidth();	 Catch:{ Throwable -> 0x014d }
        r5 = r1.getHeight();	 Catch:{ Throwable -> 0x014d }
        r7 = 1;
        r17 = org.telegram.messenger.Bitmaps.createBitmap(r1, r2, r3, r4, r5, r6, r7);	 Catch:{ Throwable -> 0x014d }
        r0 = r17;
        if (r0 == r1) goto L_0x00a8;
    L_0x0146:
        r1.recycle();	 Catch:{ Throwable -> 0x014d }
        r1 = r17;
        goto L_0x00a8;
    L_0x014d:
        r10 = move-exception;
        r2 = "tmessages";
        org.telegram.messenger.FileLog.m611e(r2, r10);
        goto L_0x00a8;
    L_0x0155:
        if (r24 == 0) goto L_0x00a8;
    L_0x0157:
        r2 = 0;
        r1 = android.graphics.BitmapFactory.decodeFileDescriptor(r14, r2, r8);	 Catch:{ Throwable -> 0x018a }
        if (r1 == 0) goto L_0x017d;
    L_0x015e:
        r2 = r8.inPurgeable;	 Catch:{ Throwable -> 0x018a }
        if (r2 == 0) goto L_0x0165;
    L_0x0162:
        org.telegram.messenger.Utilities.pinBitmap(r1);	 Catch:{ Throwable -> 0x018a }
    L_0x0165:
        r2 = 0;
        r3 = 0;
        r4 = r1.getWidth();	 Catch:{ Throwable -> 0x018a }
        r5 = r1.getHeight();	 Catch:{ Throwable -> 0x018a }
        r7 = 1;
        r17 = org.telegram.messenger.Bitmaps.createBitmap(r1, r2, r3, r4, r5, r6, r7);	 Catch:{ Throwable -> 0x018a }
        r0 = r17;
        if (r0 == r1) goto L_0x017d;
    L_0x0178:
        r1.recycle();	 Catch:{ Throwable -> 0x018a }
        r1 = r17;
    L_0x017d:
        r19.close();	 Catch:{ Throwable -> 0x0182 }
        goto L_0x00a8;
    L_0x0182:
        r9 = move-exception;
        r2 = "tmessages";
        org.telegram.messenger.FileLog.m611e(r2, r9);
        goto L_0x00a8;
    L_0x018a:
        r9 = move-exception;
        r2 = "tmessages";
        org.telegram.messenger.FileLog.m611e(r2, r9);	 Catch:{ all -> 0x019d }
        r19.close();	 Catch:{ Throwable -> 0x0195 }
        goto L_0x00a8;
    L_0x0195:
        r9 = move-exception;
        r2 = "tmessages";
        org.telegram.messenger.FileLog.m611e(r2, r9);
        goto L_0x00a8;
    L_0x019d:
        r2 = move-exception;
        r19.close();	 Catch:{ Throwable -> 0x01a2 }
    L_0x01a1:
        throw r2;
    L_0x01a2:
        r9 = move-exception;
        r3 = "tmessages";
        org.telegram.messenger.FileLog.m611e(r3, r9);
        goto L_0x01a1;
    L_0x01a9:
        r9 = move-exception;
        goto L_0x00fa;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.ImageLoader.loadBitmap(java.lang.String, android.net.Uri, float, float, boolean):android.graphics.Bitmap");
    }

    public static void fillPhotoSizeWithBytes(PhotoSize photoSize) {
        if (photoSize != null && photoSize.bytes == null) {
            try {
                RandomAccessFile f = new RandomAccessFile(FileLoader.getPathToAttach(photoSize, true), "r");
                if (((int) f.length()) < 20000) {
                    photoSize.bytes = new byte[((int) f.length())];
                    f.readFully(photoSize.bytes, 0, photoSize.bytes.length);
                }
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
        }
    }

    private static PhotoSize scaleAndSaveImageInternal(Bitmap bitmap, int w, int h, float photoW, float photoH, float scaleFactor, int quality, boolean cache, boolean scaleAnyway) throws Exception {
        Bitmap scaledBitmap;
        if (scaleFactor > 1.0f || scaleAnyway) {
            scaledBitmap = Bitmaps.createScaledBitmap(bitmap, w, h, true);
        } else {
            scaledBitmap = bitmap;
        }
        TL_fileLocation location = new TL_fileLocation();
        location.volume_id = -2147483648L;
        location.dc_id = Integer.MIN_VALUE;
        location.local_id = UserConfig.lastLocalId;
        UserConfig.lastLocalId--;
        PhotoSize size = new TL_photoSize();
        size.location = location;
        size.f139w = scaledBitmap.getWidth();
        size.f138h = scaledBitmap.getHeight();
        if (size.f139w <= 100 && size.f138h <= 100) {
            size.type = "s";
        } else if (size.f139w <= 320 && size.f138h <= 320) {
            size.type = "m";
        } else if (size.f139w <= 800 && size.f138h <= 800) {
            size.type = "x";
        } else if (size.f139w > Strings.LOGIN_HEADLINE_TEXT_ID || size.f138h > Strings.LOGIN_HEADLINE_TEXT_ID) {
            size.type = "w";
        } else {
            size.type = "y";
        }
        FileOutputStream stream = new FileOutputStream(new File(FileLoader.getInstance().getDirectory(4), location.volume_id + "_" + location.local_id + ".jpg"));
        scaledBitmap.compress(CompressFormat.JPEG, quality, stream);
        if (cache) {
            ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
            scaledBitmap.compress(CompressFormat.JPEG, quality, stream2);
            size.bytes = stream2.toByteArray();
            size.size = size.bytes.length;
            stream2.close();
        } else {
            size.size = (int) stream.getChannel().size();
        }
        stream.close();
        if (scaledBitmap != bitmap) {
            scaledBitmap.recycle();
        }
        return size;
    }

    public static PhotoSize scaleAndSaveImage(Bitmap bitmap, float maxWidth, float maxHeight, int quality, boolean cache) {
        return scaleAndSaveImage(bitmap, maxWidth, maxHeight, quality, cache, 0, 0);
    }

    public static PhotoSize scaleAndSaveImage(Bitmap bitmap, float maxWidth, float maxHeight, int quality, boolean cache, int minWidth, int minHeight) {
        if (bitmap == null) {
            return null;
        }
        float photoW = (float) bitmap.getWidth();
        float photoH = (float) bitmap.getHeight();
        if (photoW == 0.0f || photoH == 0.0f) {
            return null;
        }
        boolean scaleAnyway = false;
        float scaleFactor = Math.max(photoW / maxWidth, photoH / maxHeight);
        if (!(minWidth == 0 || minHeight == 0 || (photoW >= ((float) minWidth) && photoH >= ((float) minHeight)))) {
            if (photoW < ((float) minWidth) && photoH > ((float) minHeight)) {
                scaleFactor = photoW / ((float) minWidth);
            } else if (photoW <= ((float) minWidth) || photoH >= ((float) minHeight)) {
                scaleFactor = Math.max(photoW / ((float) minWidth), photoH / ((float) minHeight));
            } else {
                scaleFactor = photoH / ((float) minHeight);
            }
            scaleAnyway = true;
        }
        int w = (int) (photoW / scaleFactor);
        int h = (int) (photoH / scaleFactor);
        if (h == 0 || w == 0) {
            return null;
        }
        try {
            return scaleAndSaveImageInternal(bitmap, w, h, photoW, photoH, scaleFactor, quality, cache, scaleAnyway);
        } catch (Throwable e2) {
            FileLog.m611e("tmessages", e2);
            return null;
        }
    }

    public static String getHttpUrlExtension(String url) {
        String ext = null;
        int idx = url.lastIndexOf(".");
        if (idx != -1) {
            ext = url.substring(idx + 1);
        }
        if (ext == null || ext.length() == 0 || ext.length() > 4) {
            return "jpg";
        }
        return ext;
    }

    public static void saveMessageThumbs(Message message) {
        PhotoSize photoSize = null;
        Iterator i$;
        PhotoSize size;
        if (message.media instanceof TL_messageMediaPhoto) {
            i$ = message.media.photo.sizes.iterator();
            while (i$.hasNext()) {
                size = (PhotoSize) i$.next();
                if (size instanceof TL_photoCachedSize) {
                    photoSize = size;
                    break;
                }
            }
        } else if (message.media instanceof TL_messageMediaVideo) {
            if (message.media.video.thumb instanceof TL_photoCachedSize) {
                photoSize = message.media.video.thumb;
            }
        } else if (message.media instanceof TL_messageMediaDocument) {
            if (message.media.document.thumb instanceof TL_photoCachedSize) {
                photoSize = message.media.document.thumb;
            }
        } else if ((message.media instanceof TL_messageMediaWebPage) && message.media.webpage.photo != null) {
            i$ = message.media.webpage.photo.sizes.iterator();
            while (i$.hasNext()) {
                size = (PhotoSize) i$.next();
                if (size instanceof TL_photoCachedSize) {
                    photoSize = size;
                    break;
                }
            }
        }
        if (photoSize != null && photoSize.bytes != null && photoSize.bytes.length != 0) {
            if (photoSize.location instanceof TL_fileLocationUnavailable) {
                photoSize.location = new TL_fileLocation();
                photoSize.location.volume_id = -2147483648L;
                photoSize.location.dc_id = Integer.MIN_VALUE;
                photoSize.location.local_id = UserConfig.lastLocalId;
                UserConfig.lastLocalId--;
            }
            File file = FileLoader.getPathToAttach(photoSize, true);
            if (!file.exists()) {
                try {
                    RandomAccessFile writeFile = new RandomAccessFile(file, "rws");
                    writeFile.write(photoSize.bytes);
                    writeFile.close();
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
            TL_photoSize newPhotoSize = new TL_photoSize();
            newPhotoSize.w = photoSize.f139w;
            newPhotoSize.h = photoSize.f138h;
            newPhotoSize.location = photoSize.location;
            newPhotoSize.size = photoSize.size;
            newPhotoSize.type = photoSize.type;
            int a;
            if (message.media instanceof TL_messageMediaPhoto) {
                for (a = 0; a < message.media.photo.sizes.size(); a++) {
                    if (message.media.photo.sizes.get(a) instanceof TL_photoCachedSize) {
                        message.media.photo.sizes.set(a, newPhotoSize);
                        return;
                    }
                }
            } else if (message.media instanceof TL_messageMediaVideo) {
                message.media.video.thumb = newPhotoSize;
            } else if (message.media instanceof TL_messageMediaDocument) {
                message.media.document.thumb = newPhotoSize;
            } else if (message.media instanceof TL_messageMediaWebPage) {
                for (a = 0; a < message.media.webpage.photo.sizes.size(); a++) {
                    if (message.media.webpage.photo.sizes.get(a) instanceof TL_photoCachedSize) {
                        message.media.webpage.photo.sizes.set(a, newPhotoSize);
                        return;
                    }
                }
            }
        }
    }

    public static void saveMessagesThumbs(ArrayList<Message> messages) {
        if (messages != null && !messages.isEmpty()) {
            Iterator i$ = messages.iterator();
            while (i$.hasNext()) {
                saveMessageThumbs((Message) i$.next());
            }
        }
    }
}
