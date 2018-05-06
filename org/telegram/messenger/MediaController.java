package org.telegram.messenger;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.AudioTrack.OnPlaybackPositionUpdateListener;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaCodecInfo;
import android.media.MediaCodecInfo.CodecCapabilities;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.support.v4.media.session.PlaybackStateCompat;
import com.google.android.gms.location.LocationStatusCodes;
import com.google.android.gms.plus.PlusShare;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;
import net.hockeyapp.android.Strings;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.audioinfo.AudioInfo;
import org.telegram.messenger.query.SharedMediaQuery;
import org.telegram.messenger.video.MP4Builder;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC.Audio;
import org.telegram.tgnet.TLRPC.Document;
import org.telegram.tgnet.TLRPC.EncryptedChat;
import org.telegram.tgnet.TLRPC.PhotoSize;
import org.telegram.tgnet.TLRPC.TL_audio;
import org.telegram.tgnet.TLRPC.TL_encryptedChat;
import org.telegram.tgnet.TLRPC.TL_messageMediaAudio;
import org.telegram.tgnet.TLRPC.Video;
import org.telegram.ui.Cells.ChatMediaCell;
import org.telegram.ui.Components.GifDrawable;

public class MediaController implements NotificationCenterDelegate, SensorEventListener {
    public static final int AUTODOWNLOAD_MASK_AUDIO = 2;
    public static final int AUTODOWNLOAD_MASK_DOCUMENT = 8;
    public static final int AUTODOWNLOAD_MASK_PHOTO = 1;
    public static final int AUTODOWNLOAD_MASK_VIDEO = 4;
    private static volatile MediaController Instance = null;
    public static final String MIME_TYPE = "video/avc";
    private static final int PROCESSOR_TYPE_INTEL = 2;
    private static final int PROCESSOR_TYPE_MTK = 3;
    private static final int PROCESSOR_TYPE_OTHER = 0;
    private static final int PROCESSOR_TYPE_QCOM = 1;
    private static final int PROCESSOR_TYPE_SEC = 4;
    private static final int PROCESSOR_TYPE_TI = 5;
    public static AlbumEntry allPhotosAlbumEntry;
    private static final String[] projectionPhotos = new String[]{"_id", "bucket_id", "bucket_display_name", "_data", "datetaken", "orientation"};
    private static final String[] projectionVideo = new String[]{"_id", "bucket_id", "bucket_display_name", "_data", "datetaken"};
    public static int[] readArgs = new int[3];
    private HashMap<String, FileDownloadProgressListener> addLaterArray = new HashMap();
    private ArrayList<DownloadObject> audioDownloadQueue = new ArrayList();
    private AudioInfo audioInfo;
    private MediaPlayer audioPlayer = null;
    private AudioRecord audioRecorder = null;
    private AudioTrack audioTrackPlayer = null;
    private int buffersWrited;
    private boolean cancelCurrentVideoConversion = false;
    private GifDrawable currentGifDrawable;
    private MessageObject currentGifMessageObject;
    private ChatMediaCell currentMediaCell;
    private int currentPlaylistNum;
    private long currentTotalPcmDuration;
    private boolean decodingFinished = false;
    private ArrayList<FileDownloadProgressListener> deleteLaterArray = new ArrayList();
    private ArrayList<DownloadObject> documentDownloadQueue = new ArrayList();
    private HashMap<String, DownloadObject> downloadQueueKeys = new HashMap();
    private boolean downloadingCurrentMessage;
    private ExternalObserver externalObserver = null;
    private ByteBuffer fileBuffer;
    private DispatchQueue fileDecodingQueue;
    private DispatchQueue fileEncodingQueue;
    private ArrayList<AudioBuffer> freePlayerBuffers = new ArrayList();
    private int ignoreFirstProgress = 0;
    private boolean ignoreProximity;
    private InternalObserver internalObserver = null;
    private boolean isPaused = false;
    private int lastCheckMask = 0;
    private long lastMediaCheckTime = 0;
    private long lastPlayPcm;
    private int lastProgress = 0;
    private EncryptedChat lastSecretChat = null;
    private long lastSecretChatEnterTime = 0;
    private long lastSecretChatLeaveTime = 0;
    private ArrayList<Long> lastSecretChatVisibleMessages = null;
    private int lastTag = 0;
    private boolean listenerInProgress = false;
    private HashMap<String, ArrayList<WeakReference<FileDownloadProgressListener>>> loadingFileObservers = new HashMap();
    private String[] mediaProjections = null;
    public int mobileDataDownloadMask = 0;
    private HashMap<Integer, String> observersByTag = new HashMap();
    private ArrayList<DownloadObject> photoDownloadQueue = new ArrayList();
    private boolean playMusicAgain;
    private int playerBufferSize = 0;
    private final Object playerObjectSync = new Object();
    private DispatchQueue playerQueue;
    private final Object playerSync = new Object();
    private MessageObject playingMessageObject;
    private ArrayList<MessageObject> playlist = new ArrayList();
    private Timer progressTimer = null;
    private final Object progressTimerSync = new Object();
    private Sensor proximitySensor;
    private WakeLock proximityWakeLock;
    private boolean recordAsAdmin;
    private int recordBufferSize;
    private ArrayList<ByteBuffer> recordBuffers = new ArrayList();
    private long recordDialogId;
    private DispatchQueue recordQueue;
    private MessageObject recordReplyingMessageObject;
    private Runnable recordRunnable = new C04171();
    private Runnable recordStartRunnable;
    private long recordStartTime;
    private long recordTimeCount;
    private TL_audio recordingAudio = null;
    private File recordingAudioFile = null;
    private Runnable refreshGalleryRunnable;
    private int repeatMode;
    public int roamingDownloadMask = 0;
    private boolean saveToGallery = true;
    private boolean sendAfterDone;
    private SensorManager sensorManager;
    private boolean shuffleMusic;
    private ArrayList<MessageObject> shuffledPlaylist = new ArrayList();
    private int startObserverToken = 0;
    private StopMediaObserverRunnable stopMediaObserverRunnable = null;
    private final Object sync = new Object();
    private HashMap<Long, Long> typingTimes = new HashMap();
    private boolean useFrontSpeaker;
    private ArrayList<AudioBuffer> usedPlayerBuffers = new ArrayList();
    private boolean videoConvertFirstWrite = true;
    private ArrayList<MessageObject> videoConvertQueue = new ArrayList();
    private final Object videoConvertSync = new Object();
    private ArrayList<DownloadObject> videoDownloadQueue = new ArrayList();
    private final Object videoQueueSync = new Object();
    public int wifiDownloadMask = 0;

    class C04171 implements Runnable {
        C04171() {
        }

        public void run() {
            if (MediaController.this.audioRecorder != null) {
                ByteBuffer buffer;
                if (MediaController.this.recordBuffers.isEmpty()) {
                    buffer = ByteBuffer.allocateDirect(MediaController.this.recordBufferSize);
                    buffer.order(ByteOrder.nativeOrder());
                } else {
                    buffer = (ByteBuffer) MediaController.this.recordBuffers.get(0);
                    MediaController.this.recordBuffers.remove(0);
                }
                buffer.rewind();
                int len = MediaController.this.audioRecorder.read(buffer, buffer.capacity());
                if (len > 0) {
                    double sum = 0.0d;
                    int i = 0;
                    while (i < len / 2) {
                        try {
                            short peak = buffer.getShort();
                            sum += (double) (peak * peak);
                            i++;
                        } catch (Throwable e) {
                            FileLog.m611e("tmessages", e);
                        }
                    }
                    buffer.position(0);
                    final double amplitude = Math.sqrt((sum / ((double) len)) / 2.0d);
                    buffer.limit(len);
                    final ByteBuffer finalBuffer = buffer;
                    final boolean flush = len != buffer.capacity();
                    if (len != 0) {
                        MediaController.this.fileEncodingQueue.postRunnable(new Runnable() {

                            class C04051 implements Runnable {
                                C04051() {
                                }

                                public void run() {
                                    MediaController.this.recordBuffers.add(finalBuffer);
                                }
                            }

                            public void run() {
                                while (finalBuffer.hasRemaining()) {
                                    int oldLimit = -1;
                                    if (finalBuffer.remaining() > MediaController.this.fileBuffer.remaining()) {
                                        oldLimit = finalBuffer.limit();
                                        finalBuffer.limit(MediaController.this.fileBuffer.remaining() + finalBuffer.position());
                                    }
                                    MediaController.this.fileBuffer.put(finalBuffer);
                                    if (MediaController.this.fileBuffer.position() == MediaController.this.fileBuffer.limit() || flush) {
                                        if (MediaController.this.writeFrame(MediaController.this.fileBuffer, !flush ? MediaController.this.fileBuffer.limit() : finalBuffer.position()) != 0) {
                                            MediaController.this.fileBuffer.rewind();
                                            MediaController.access$514(MediaController.this, (long) ((MediaController.this.fileBuffer.limit() / 2) / 16));
                                        }
                                    }
                                    if (oldLimit != -1) {
                                        finalBuffer.limit(oldLimit);
                                    }
                                }
                                MediaController.this.recordQueue.postRunnable(new C04051());
                            }
                        });
                    }
                    MediaController.this.recordQueue.postRunnable(MediaController.this.recordRunnable);
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        public void run() {
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.recordProgressChanged, Long.valueOf(System.currentTimeMillis() - MediaController.this.recordStartTime), Double.valueOf(amplitude));
                        }
                    });
                    return;
                }
                MediaController.this.recordBuffers.add(buffer);
                MediaController.this.stopRecordingInternal(MediaController.this.sendAfterDone);
            }
        }
    }

    class C04182 implements Runnable {
        C04182() {
        }

        public void run() {
            NotificationCenter.getInstance().addObserver(MediaController.this, NotificationCenter.FileDidFailedLoad);
            NotificationCenter.getInstance().addObserver(MediaController.this, NotificationCenter.FileDidLoaded);
            NotificationCenter.getInstance().addObserver(MediaController.this, NotificationCenter.FileLoadProgressChanged);
            NotificationCenter.getInstance().addObserver(MediaController.this, NotificationCenter.FileUploadProgressChanged);
            NotificationCenter.getInstance().addObserver(MediaController.this, NotificationCenter.messagesDeleted);
            NotificationCenter.getInstance().addObserver(MediaController.this, NotificationCenter.removeAllMessagesFromDialog);
            NotificationCenter.getInstance().addObserver(MediaController.this, NotificationCenter.musicDidLoaded);
        }
    }

    class C04193 extends BroadcastReceiver {
        C04193() {
        }

        public void onReceive(Context context, Intent intent) {
            MediaController.this.checkAutodownloadSettings();
        }
    }

    class C04214 extends TimerTask {

        class C04201 implements Runnable {
            C04201() {
            }

            public void run() {
                if (MediaController.this.playingMessageObject == null) {
                    return;
                }
                if ((MediaController.this.audioPlayer != null || MediaController.this.audioTrackPlayer != null) && !MediaController.this.isPaused) {
                    try {
                        if (MediaController.this.ignoreFirstProgress != 0) {
                            MediaController.this.ignoreFirstProgress = MediaController.this.ignoreFirstProgress - 1;
                            return;
                        }
                        int progress;
                        float value;
                        if (MediaController.this.audioPlayer != null) {
                            progress = MediaController.this.audioPlayer.getCurrentPosition();
                            value = ((float) MediaController.this.lastProgress) / ((float) MediaController.this.audioPlayer.getDuration());
                            if (progress <= MediaController.this.lastProgress) {
                                return;
                            }
                        }
                        progress = (int) (((float) MediaController.this.lastPlayPcm) / 48.0f);
                        value = ((float) MediaController.this.lastPlayPcm) / ((float) MediaController.this.currentTotalPcmDuration);
                        if (progress == MediaController.this.lastProgress) {
                            return;
                        }
                        MediaController.this.lastProgress = progress;
                        MediaController.this.playingMessageObject.audioProgress = value;
                        MediaController.this.playingMessageObject.audioProgressSec = MediaController.this.lastProgress / LocationStatusCodes.GEOFENCE_NOT_AVAILABLE;
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.audioProgressDidChanged, Integer.valueOf(MediaController.this.playingMessageObject.getId()), Float.valueOf(value));
                    } catch (Throwable e) {
                        FileLog.m611e("tmessages", e);
                    }
                }
            }
        }

        C04214() {
        }

        public void run() {
            synchronized (MediaController.this.sync) {
                AndroidUtilities.runOnUIThread(new C04201());
            }
        }
    }

    class C04236 implements Runnable {
        C04236() {
        }

        public void run() {
            if (MediaController.this.decodingFinished) {
                MediaController.this.checkPlayerQueue();
                return;
            }
            boolean was = false;
            while (true) {
                AudioBuffer buffer = null;
                synchronized (MediaController.this.playerSync) {
                    if (!MediaController.this.freePlayerBuffers.isEmpty()) {
                        buffer = (AudioBuffer) MediaController.this.freePlayerBuffers.get(0);
                        MediaController.this.freePlayerBuffers.remove(0);
                    }
                    if (!MediaController.this.usedPlayerBuffers.isEmpty()) {
                        was = true;
                    }
                }
                if (buffer == null) {
                    break;
                }
                MediaController.this.readOpusFile(buffer.buffer, MediaController.this.playerBufferSize, MediaController.readArgs);
                buffer.size = MediaController.readArgs[0];
                buffer.pcmOffset = (long) MediaController.readArgs[1];
                buffer.finished = MediaController.readArgs[2];
                if (buffer.finished == 1) {
                    MediaController.this.decodingFinished = true;
                }
                if (buffer.size == 0) {
                    break;
                }
                buffer.buffer.rewind();
                buffer.buffer.get(buffer.bufferBytes);
                synchronized (MediaController.this.playerSync) {
                    MediaController.this.usedPlayerBuffers.add(buffer);
                }
                was = true;
            }
            synchronized (MediaController.this.playerSync) {
                MediaController.this.freePlayerBuffers.add(buffer);
            }
            if (was) {
                MediaController.this.checkPlayerQueue();
            }
        }
    }

    class C04257 implements Runnable {
        C04257() {
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            /*
            r14 = this;
            r13 = 1;
            r2 = org.telegram.messenger.MediaController.this;
            r3 = r2.playerObjectSync;
            monitor-enter(r3);
            r2 = org.telegram.messenger.MediaController.this;	 Catch:{ all -> 0x00ab }
            r2 = r2.audioTrackPlayer;	 Catch:{ all -> 0x00ab }
            if (r2 == 0) goto L_0x001d;
        L_0x0010:
            r2 = org.telegram.messenger.MediaController.this;	 Catch:{ all -> 0x00ab }
            r2 = r2.audioTrackPlayer;	 Catch:{ all -> 0x00ab }
            r2 = r2.getPlayState();	 Catch:{ all -> 0x00ab }
            r11 = 3;
            if (r2 == r11) goto L_0x001f;
        L_0x001d:
            monitor-exit(r3);	 Catch:{ all -> 0x00ab }
        L_0x001e:
            return;
        L_0x001f:
            monitor-exit(r3);	 Catch:{ all -> 0x00ab }
            r8 = 0;
            r2 = org.telegram.messenger.MediaController.this;
            r3 = r2.playerSync;
            monitor-enter(r3);
            r2 = org.telegram.messenger.MediaController.this;	 Catch:{ all -> 0x00ae }
            r2 = r2.usedPlayerBuffers;	 Catch:{ all -> 0x00ae }
            r2 = r2.isEmpty();	 Catch:{ all -> 0x00ae }
            if (r2 != 0) goto L_0x004d;
        L_0x0034:
            r2 = org.telegram.messenger.MediaController.this;	 Catch:{ all -> 0x00ae }
            r2 = r2.usedPlayerBuffers;	 Catch:{ all -> 0x00ae }
            r11 = 0;
            r2 = r2.get(r11);	 Catch:{ all -> 0x00ae }
            r0 = r2;
            r0 = (org.telegram.messenger.MediaController.AudioBuffer) r0;	 Catch:{ all -> 0x00ae }
            r8 = r0;
            r2 = org.telegram.messenger.MediaController.this;	 Catch:{ all -> 0x00ae }
            r2 = r2.usedPlayerBuffers;	 Catch:{ all -> 0x00ae }
            r11 = 0;
            r2.remove(r11);	 Catch:{ all -> 0x00ae }
        L_0x004d:
            monitor-exit(r3);	 Catch:{ all -> 0x00ae }
            if (r8 == 0) goto L_0x0086;
        L_0x0050:
            r9 = 0;
            r2 = org.telegram.messenger.MediaController.this;	 Catch:{ Exception -> 0x00b1 }
            r2 = r2.audioTrackPlayer;	 Catch:{ Exception -> 0x00b1 }
            r3 = r8.bufferBytes;	 Catch:{ Exception -> 0x00b1 }
            r11 = 0;
            r12 = r8.size;	 Catch:{ Exception -> 0x00b1 }
            r9 = r2.write(r3, r11, r12);	 Catch:{ Exception -> 0x00b1 }
        L_0x0060:
            r2 = org.telegram.messenger.MediaController.this;
            r2.buffersWrited = r2.buffersWrited + 1;
            if (r9 <= 0) goto L_0x007d;
        L_0x0067:
            r4 = r8.pcmOffset;
            r2 = r8.finished;
            if (r2 != r13) goto L_0x00b8;
        L_0x006d:
            r6 = r9;
        L_0x006e:
            r2 = org.telegram.messenger.MediaController.this;
            r7 = r2.buffersWrited;
            r2 = new org.telegram.messenger.MediaController$7$1;
            r3 = r14;
            r2.<init>(r4, r6, r7);
            org.telegram.messenger.AndroidUtilities.runOnUIThread(r2);
        L_0x007d:
            r2 = r8.finished;
            if (r2 == r13) goto L_0x0086;
        L_0x0081:
            r2 = org.telegram.messenger.MediaController.this;
            r2.checkPlayerQueue();
        L_0x0086:
            if (r8 == 0) goto L_0x008e;
        L_0x0088:
            if (r8 == 0) goto L_0x0093;
        L_0x008a:
            r2 = r8.finished;
            if (r2 == r13) goto L_0x0093;
        L_0x008e:
            r2 = org.telegram.messenger.MediaController.this;
            r2.checkDecoderQueue();
        L_0x0093:
            if (r8 == 0) goto L_0x001e;
        L_0x0095:
            r2 = org.telegram.messenger.MediaController.this;
            r3 = r2.playerSync;
            monitor-enter(r3);
            r2 = org.telegram.messenger.MediaController.this;	 Catch:{ all -> 0x00a8 }
            r2 = r2.freePlayerBuffers;	 Catch:{ all -> 0x00a8 }
            r2.add(r8);	 Catch:{ all -> 0x00a8 }
            monitor-exit(r3);	 Catch:{ all -> 0x00a8 }
            goto L_0x001e;
        L_0x00a8:
            r2 = move-exception;
            monitor-exit(r3);	 Catch:{ all -> 0x00a8 }
            throw r2;
        L_0x00ab:
            r2 = move-exception;
            monitor-exit(r3);	 Catch:{ all -> 0x00ab }
            throw r2;
        L_0x00ae:
            r2 = move-exception;
            monitor-exit(r3);	 Catch:{ all -> 0x00ae }
            throw r2;
        L_0x00b1:
            r10 = move-exception;
            r2 = "tmessages";
            org.telegram.messenger.FileLog.m611e(r2, r10);
            goto L_0x0060;
        L_0x00b8:
            r6 = -1;
            goto L_0x006e;
            */
            throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MediaController.7.run():void");
        }
    }

    public static class AlbumEntry {
        public int bucketId;
        public String bucketName;
        public PhotoEntry coverPhoto;
        public boolean isVideo;
        public ArrayList<PhotoEntry> photos = new ArrayList();
        public HashMap<Integer, PhotoEntry> photosByIds = new HashMap();

        public AlbumEntry(int bucketId, String bucketName, PhotoEntry coverPhoto, boolean isVideo) {
            this.bucketId = bucketId;
            this.bucketName = bucketName;
            this.coverPhoto = coverPhoto;
            this.isVideo = isVideo;
        }

        public void addPhoto(PhotoEntry photoEntry) {
            this.photos.add(photoEntry);
            this.photosByIds.put(Integer.valueOf(photoEntry.imageId), photoEntry);
        }
    }

    private class AudioBuffer {
        ByteBuffer buffer;
        byte[] bufferBytes;
        int finished;
        long pcmOffset;
        int size;

        public AudioBuffer(int capacity) {
            this.buffer = ByteBuffer.allocateDirect(capacity);
            this.bufferBytes = new byte[capacity];
        }
    }

    public static class AudioEntry {
        public String author;
        public int duration;
        public String genre;
        public long id;
        public MessageObject messageObject;
        public String path;
        public String title;
    }

    private class ExternalObserver extends ContentObserver {
        public ExternalObserver() {
            super(null);
        }

        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            MediaController.this.processMediaObserver(Media.EXTERNAL_CONTENT_URI);
        }
    }

    public interface FileDownloadProgressListener {
        int getObserverTag();

        void onFailedDownload(String str);

        void onProgressDownload(String str, float f);

        void onProgressUpload(String str, float f, boolean z);

        void onSuccessDownload(String str);
    }

    private class GalleryObserverExternal extends ContentObserver {

        class C04291 implements Runnable {
            C04291() {
            }

            public void run() {
                MediaController.this.refreshGalleryRunnable = null;
                MediaController.loadGalleryPhotosAlbums(0);
            }
        }

        public GalleryObserverExternal() {
            super(null);
        }

        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            if (MediaController.this.refreshGalleryRunnable != null) {
                AndroidUtilities.cancelRunOnUIThread(MediaController.this.refreshGalleryRunnable);
            }
            AndroidUtilities.runOnUIThread(MediaController.this.refreshGalleryRunnable = new C04291(), 2000);
        }
    }

    private class GalleryObserverInternal extends ContentObserver {

        class C04301 implements Runnable {
            C04301() {
            }

            public void run() {
                MediaController.this.refreshGalleryRunnable = null;
                MediaController.loadGalleryPhotosAlbums(0);
            }
        }

        public GalleryObserverInternal() {
            super(null);
        }

        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            if (MediaController.this.refreshGalleryRunnable != null) {
                AndroidUtilities.cancelRunOnUIThread(MediaController.this.refreshGalleryRunnable);
            }
            AndroidUtilities.runOnUIThread(MediaController.this.refreshGalleryRunnable = new C04301(), 2000);
        }
    }

    private class InternalObserver extends ContentObserver {
        public InternalObserver() {
            super(null);
        }

        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            MediaController.this.processMediaObserver(Media.INTERNAL_CONTENT_URI);
        }
    }

    public static class PhotoEntry {
        public int bucketId;
        public CharSequence caption;
        public long dateTaken;
        public int imageId;
        public String imagePath;
        public boolean isVideo;
        public int orientation;
        public String path;
        public String thumbPath;

        public PhotoEntry(int bucketId, int imageId, long dateTaken, String path, int orientation, boolean isVideo) {
            this.bucketId = bucketId;
            this.imageId = imageId;
            this.dateTaken = dateTaken;
            this.path = path;
            this.orientation = orientation;
            this.isVideo = isVideo;
        }
    }

    public static class SearchImage {
        public CharSequence caption;
        public int date;
        public int height;
        public String id;
        public String imagePath;
        public String imageUrl;
        public String localUrl;
        public int size;
        public String thumbPath;
        public String thumbUrl;
        public int type;
        public int uid;
        public int width;
    }

    private final class StopMediaObserverRunnable implements Runnable {
        public int currentObserverToken;

        private StopMediaObserverRunnable() {
            this.currentObserverToken = 0;
        }

        public void run() {
            if (this.currentObserverToken == MediaController.this.startObserverToken) {
                try {
                    if (MediaController.this.internalObserver != null) {
                        ApplicationLoader.applicationContext.getContentResolver().unregisterContentObserver(MediaController.this.internalObserver);
                        MediaController.this.internalObserver = null;
                    }
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
                try {
                    if (MediaController.this.externalObserver != null) {
                        ApplicationLoader.applicationContext.getContentResolver().unregisterContentObserver(MediaController.this.externalObserver);
                        MediaController.this.externalObserver = null;
                    }
                } catch (Throwable e2) {
                    FileLog.m611e("tmessages", e2);
                }
            }
        }
    }

    private static class VideoConvertRunnable implements Runnable {
        private MessageObject messageObject;

        private VideoConvertRunnable(MessageObject message) {
            this.messageObject = message;
        }

        public void run() {
            MediaController.getInstance().convertVideo(this.messageObject);
        }

        public static void runConversion(final MessageObject obj) {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        Thread th = new Thread(new VideoConvertRunnable(obj), "VideoConvertRunnable");
                        th.start();
                        th.join();
                    } catch (Throwable e) {
                        FileLog.m611e("tmessages", e);
                    }
                }
            }).start();
        }
    }

    private native void closeOpusFile();

    private native long getTotalPcmDuration();

    private native int isOpusFile(String str);

    private native int openOpusFile(String str);

    private native void readOpusFile(ByteBuffer byteBuffer, int i, int[] iArr);

    private native int seekOpusFile(float f);

    private native int startRecord(String str);

    private native void stopRecord();

    private native int writeFrame(ByteBuffer byteBuffer, int i);

    static /* synthetic */ long access$514(MediaController x0, long x1) {
        long j = x0.recordTimeCount + x1;
        x0.recordTimeCount = j;
        return j;
    }

    public static MediaController getInstance() {
        MediaController localInstance = Instance;
        if (localInstance == null) {
            synchronized (MediaController.class) {
                try {
                    localInstance = Instance;
                    if (localInstance == null) {
                        MediaController localInstance2 = new MediaController();
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

    public MediaController() {
        try {
            int a;
            this.recordBufferSize = AudioRecord.getMinBufferSize(16000, 16, 2);
            if (this.recordBufferSize <= 0) {
                this.recordBufferSize = Strings.LOGIN_HEADLINE_TEXT_ID;
            }
            this.playerBufferSize = AudioTrack.getMinBufferSize(48000, 4, 2);
            if (this.playerBufferSize <= 0) {
                this.playerBufferSize = 3840;
            }
            for (a = 0; a < 5; a++) {
                ByteBuffer buffer = ByteBuffer.allocateDirect(4096);
                buffer.order(ByteOrder.nativeOrder());
                this.recordBuffers.add(buffer);
            }
            for (a = 0; a < 3; a++) {
                this.freePlayerBuffers.add(new AudioBuffer(this.playerBufferSize));
            }
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
        }
        try {
            this.sensorManager = (SensorManager) ApplicationLoader.applicationContext.getSystemService("sensor");
            this.proximitySensor = this.sensorManager.getDefaultSensor(8);
            this.proximityWakeLock = ((PowerManager) ApplicationLoader.applicationContext.getSystemService("power")).newWakeLock(32, "proximity");
        } catch (Throwable e2) {
            FileLog.m611e("tmessages", e2);
        }
        this.fileBuffer = ByteBuffer.allocateDirect(1920);
        this.recordQueue = new DispatchQueue("recordQueue");
        this.recordQueue.setPriority(10);
        this.fileEncodingQueue = new DispatchQueue("fileEncodingQueue");
        this.fileEncodingQueue.setPriority(10);
        this.playerQueue = new DispatchQueue("playerQueue");
        this.fileDecodingQueue = new DispatchQueue("fileDecodingQueue");
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0);
        this.mobileDataDownloadMask = preferences.getInt("mobileDataDownloadMask", 3);
        this.wifiDownloadMask = preferences.getInt("wifiDownloadMask", 3);
        this.roamingDownloadMask = preferences.getInt("roamingDownloadMask", 0);
        this.saveToGallery = preferences.getBoolean("save_gallery", false);
        this.shuffleMusic = preferences.getBoolean("shuffleMusic", false);
        this.repeatMode = preferences.getInt("repeatMode", 0);
        AndroidUtilities.runOnUIThread(new C04182());
        ApplicationLoader.applicationContext.registerReceiver(new C04193(), new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        if (UserConfig.isClientActivated()) {
            checkAutodownloadSettings();
        }
        if (VERSION.SDK_INT >= 16) {
            this.mediaProjections = new String[]{"_data", "_display_name", "bucket_display_name", "datetaken", PlusShare.KEY_CONTENT_DEEP_LINK_METADATA_TITLE, "width", "height"};
        } else {
            this.mediaProjections = new String[]{"_data", "_display_name", "bucket_display_name", "datetaken", PlusShare.KEY_CONTENT_DEEP_LINK_METADATA_TITLE};
        }
        try {
            ApplicationLoader.applicationContext.getContentResolver().registerContentObserver(Media.EXTERNAL_CONTENT_URI, false, new GalleryObserverExternal());
        } catch (Throwable e22) {
            FileLog.m611e("tmessages", e22);
        }
        try {
            ApplicationLoader.applicationContext.getContentResolver().registerContentObserver(Media.INTERNAL_CONTENT_URI, false, new GalleryObserverInternal());
        } catch (Throwable e222) {
            FileLog.m611e("tmessages", e222);
        }
    }

    private void startProgressTimer() {
        synchronized (this.progressTimerSync) {
            if (this.progressTimer != null) {
                try {
                    this.progressTimer.cancel();
                    this.progressTimer = null;
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
            this.progressTimer = new Timer();
            this.progressTimer.schedule(new C04214(), 0, 17);
        }
    }

    private void stopProgressTimer() {
        synchronized (this.progressTimerSync) {
            if (this.progressTimer != null) {
                try {
                    this.progressTimer.cancel();
                    this.progressTimer = null;
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
        }
    }

    public void cleanup() {
        cleanupPlayer(false, true);
        if (this.currentGifDrawable != null) {
            this.currentGifDrawable.recycle();
            this.currentGifDrawable = null;
        }
        this.currentMediaCell = null;
        this.audioInfo = null;
        this.playMusicAgain = false;
        this.currentGifMessageObject = null;
        this.photoDownloadQueue.clear();
        this.audioDownloadQueue.clear();
        this.documentDownloadQueue.clear();
        this.videoDownloadQueue.clear();
        this.downloadQueueKeys.clear();
        this.videoConvertQueue.clear();
        this.playlist.clear();
        this.shuffledPlaylist.clear();
        this.typingTimes.clear();
        cancelVideoConvert(null);
    }

    protected int getAutodownloadMask() {
        int mask = 0;
        if (!((this.mobileDataDownloadMask & 1) == 0 && (this.wifiDownloadMask & 1) == 0 && (this.roamingDownloadMask & 1) == 0)) {
            mask = 0 | 1;
        }
        if (!((this.mobileDataDownloadMask & 2) == 0 && (this.wifiDownloadMask & 2) == 0 && (this.roamingDownloadMask & 2) == 0)) {
            mask |= 2;
        }
        if (!((this.mobileDataDownloadMask & 4) == 0 && (this.wifiDownloadMask & 4) == 0 && (this.roamingDownloadMask & 4) == 0)) {
            mask |= 4;
        }
        if ((this.mobileDataDownloadMask & 8) == 0 && (this.wifiDownloadMask & 8) == 0 && (this.roamingDownloadMask & 8) == 0) {
            return mask;
        }
        return mask | 8;
    }

    public void checkAutodownloadSettings() {
        int currentMask = getCurrentDownloadMask();
        if (currentMask != this.lastCheckMask) {
            Iterator i$;
            this.lastCheckMask = currentMask;
            if ((currentMask & 1) == 0) {
                i$ = this.photoDownloadQueue.iterator();
                while (i$.hasNext()) {
                    FileLoader.getInstance().cancelLoadFile((PhotoSize) ((DownloadObject) i$.next()).object);
                }
                this.photoDownloadQueue.clear();
            } else if (this.photoDownloadQueue.isEmpty()) {
                newDownloadObjectsAvailable(1);
            }
            if ((currentMask & 2) == 0) {
                i$ = this.audioDownloadQueue.iterator();
                while (i$.hasNext()) {
                    FileLoader.getInstance().cancelLoadFile((Audio) ((DownloadObject) i$.next()).object);
                }
                this.audioDownloadQueue.clear();
            } else if (this.audioDownloadQueue.isEmpty()) {
                newDownloadObjectsAvailable(2);
            }
            if ((currentMask & 8) == 0) {
                i$ = this.documentDownloadQueue.iterator();
                while (i$.hasNext()) {
                    FileLoader.getInstance().cancelLoadFile((Document) ((DownloadObject) i$.next()).object);
                }
                this.documentDownloadQueue.clear();
            } else if (this.documentDownloadQueue.isEmpty()) {
                newDownloadObjectsAvailable(8);
            }
            if ((currentMask & 4) == 0) {
                i$ = this.videoDownloadQueue.iterator();
                while (i$.hasNext()) {
                    FileLoader.getInstance().cancelLoadFile((Video) ((DownloadObject) i$.next()).object);
                }
                this.videoDownloadQueue.clear();
            } else if (this.videoDownloadQueue.isEmpty()) {
                newDownloadObjectsAvailable(4);
            }
            int mask = getAutodownloadMask();
            if (mask == 0) {
                MessagesStorage.getInstance().clearDownloadQueue(0);
                return;
            }
            if ((mask & 1) == 0) {
                MessagesStorage.getInstance().clearDownloadQueue(1);
            }
            if ((mask & 2) == 0) {
                MessagesStorage.getInstance().clearDownloadQueue(2);
            }
            if ((mask & 4) == 0) {
                MessagesStorage.getInstance().clearDownloadQueue(4);
            }
            if ((mask & 8) == 0) {
                MessagesStorage.getInstance().clearDownloadQueue(8);
            }
        }
    }

    public boolean canDownloadMedia(int type) {
        return (getCurrentDownloadMask() & type) != 0;
    }

    private int getCurrentDownloadMask() {
        if (ConnectionsManager.isConnectedToWiFi()) {
            return this.wifiDownloadMask;
        }
        if (ConnectionsManager.isRoaming()) {
            return this.roamingDownloadMask;
        }
        return this.mobileDataDownloadMask;
    }

    protected void processDownloadObjects(int type, ArrayList<DownloadObject> objects) {
        if (!objects.isEmpty()) {
            ArrayList<DownloadObject> queue = null;
            if (type == 1) {
                queue = this.photoDownloadQueue;
            } else if (type == 2) {
                queue = this.audioDownloadQueue;
            } else if (type == 4) {
                queue = this.videoDownloadQueue;
            } else if (type == 8) {
                queue = this.documentDownloadQueue;
            }
            Iterator i$ = objects.iterator();
            while (i$.hasNext()) {
                DownloadObject downloadObject = (DownloadObject) i$.next();
                String path = FileLoader.getAttachFileName(downloadObject.object);
                if (!this.downloadQueueKeys.containsKey(path)) {
                    boolean added = true;
                    if (downloadObject.object instanceof Audio) {
                        FileLoader.getInstance().loadFile((Audio) downloadObject.object, false);
                    } else if (downloadObject.object instanceof PhotoSize) {
                        FileLoader.getInstance().loadFile((PhotoSize) downloadObject.object, null, false);
                    } else if (downloadObject.object instanceof Video) {
                        FileLoader.getInstance().loadFile((Video) downloadObject.object, false);
                    } else if (downloadObject.object instanceof Document) {
                        FileLoader.getInstance().loadFile((Document) downloadObject.object, false, false);
                    } else {
                        added = false;
                    }
                    if (added) {
                        queue.add(downloadObject);
                        this.downloadQueueKeys.put(path, downloadObject);
                    }
                }
            }
        }
    }

    protected void newDownloadObjectsAvailable(int downloadMask) {
        int mask = getCurrentDownloadMask();
        if (!((mask & 1) == 0 || (downloadMask & 1) == 0 || !this.photoDownloadQueue.isEmpty())) {
            MessagesStorage.getInstance().getDownloadQueue(1);
        }
        if (!((mask & 2) == 0 || (downloadMask & 2) == 0 || !this.audioDownloadQueue.isEmpty())) {
            MessagesStorage.getInstance().getDownloadQueue(2);
        }
        if (!((mask & 4) == 0 || (downloadMask & 4) == 0 || !this.videoDownloadQueue.isEmpty())) {
            MessagesStorage.getInstance().getDownloadQueue(4);
        }
        if ((mask & 8) != 0 && (downloadMask & 8) != 0 && this.documentDownloadQueue.isEmpty()) {
            MessagesStorage.getInstance().getDownloadQueue(8);
        }
    }

    private void checkDownloadFinished(String fileName, int state) {
        DownloadObject downloadObject = (DownloadObject) this.downloadQueueKeys.get(fileName);
        if (downloadObject != null) {
            this.downloadQueueKeys.remove(fileName);
            if (state == 0 || state == 2) {
                MessagesStorage.getInstance().removeFromDownloadQueue(downloadObject.id, downloadObject.type, false);
            }
            if (downloadObject.type == 1) {
                this.photoDownloadQueue.remove(downloadObject);
                if (this.photoDownloadQueue.isEmpty()) {
                    newDownloadObjectsAvailable(1);
                }
            } else if (downloadObject.type == 2) {
                this.audioDownloadQueue.remove(downloadObject);
                if (this.audioDownloadQueue.isEmpty()) {
                    newDownloadObjectsAvailable(2);
                }
            } else if (downloadObject.type == 4) {
                this.videoDownloadQueue.remove(downloadObject);
                if (this.videoDownloadQueue.isEmpty()) {
                    newDownloadObjectsAvailable(4);
                }
            } else if (downloadObject.type == 8) {
                this.documentDownloadQueue.remove(downloadObject);
                if (this.documentDownloadQueue.isEmpty()) {
                    newDownloadObjectsAvailable(8);
                }
            }
        }
    }

    public void startMediaObserver() {
        if (VERSION.SDK_INT >= 14) {
            ContentResolver contentResolver;
            Uri uri;
            ContentObserver externalObserver;
            ApplicationLoader.applicationHandler.removeCallbacks(this.stopMediaObserverRunnable);
            this.startObserverToken++;
            try {
                if (this.internalObserver == null) {
                    contentResolver = ApplicationLoader.applicationContext.getContentResolver();
                    uri = Media.EXTERNAL_CONTENT_URI;
                    externalObserver = new ExternalObserver();
                    this.externalObserver = externalObserver;
                    contentResolver.registerContentObserver(uri, false, externalObserver);
                }
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
            try {
                if (this.externalObserver == null) {
                    contentResolver = ApplicationLoader.applicationContext.getContentResolver();
                    uri = Media.INTERNAL_CONTENT_URI;
                    externalObserver = new InternalObserver();
                    this.internalObserver = externalObserver;
                    contentResolver.registerContentObserver(uri, false, externalObserver);
                }
            } catch (Throwable e2) {
                FileLog.m611e("tmessages", e2);
            }
        }
    }

    public void stopMediaObserver() {
        if (VERSION.SDK_INT >= 14) {
            if (this.stopMediaObserverRunnable == null) {
                this.stopMediaObserverRunnable = new StopMediaObserverRunnable();
            }
            this.stopMediaObserverRunnable.currentObserverToken = this.startObserverToken;
            ApplicationLoader.applicationHandler.postDelayed(this.stopMediaObserverRunnable, 5000);
        }
    }

    public void processMediaObserver(Uri uri) {
        try {
            Point size = AndroidUtilities.getRealScreenSize();
            Cursor cursor = ApplicationLoader.applicationContext.getContentResolver().query(uri, this.mediaProjections, null, null, "date_added DESC LIMIT 1");
            ArrayList<Long> screenshotDates = new ArrayList();
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String val = "";
                    String data = cursor.getString(0);
                    String display_name = cursor.getString(1);
                    String album_name = cursor.getString(2);
                    long date = cursor.getLong(3);
                    String title = cursor.getString(4);
                    int photoW = 0;
                    int photoH = 0;
                    if (VERSION.SDK_INT >= 16) {
                        photoW = cursor.getInt(5);
                        photoH = cursor.getInt(6);
                    }
                    if ((data != null && data.toLowerCase().contains("screenshot")) || ((display_name != null && display_name.toLowerCase().contains("screenshot")) || ((album_name != null && album_name.toLowerCase().contains("screenshot")) || (title != null && title.toLowerCase().contains("screenshot"))))) {
                        if (photoW == 0 || photoH == 0) {
                            try {
                                Options bmOptions = new Options();
                                bmOptions.inJustDecodeBounds = true;
                                BitmapFactory.decodeFile(data, bmOptions);
                                photoW = bmOptions.outWidth;
                                photoH = bmOptions.outHeight;
                            } catch (Exception e) {
                                screenshotDates.add(Long.valueOf(date));
                            }
                        }
                        if (photoW <= 0 || photoH <= 0 || ((photoW == size.x && photoH == size.y) || (photoH == size.x && photoW == size.y))) {
                            screenshotDates.add(Long.valueOf(date));
                        }
                    }
                }
                cursor.close();
            }
            if (!screenshotDates.isEmpty()) {
                final ArrayList<Long> arrayList = screenshotDates;
                AndroidUtilities.runOnUIThread(new Runnable() {
                    public void run() {
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.screenshotTook, new Object[0]);
                        MediaController.this.checkScreenshots(arrayList);
                    }
                });
            }
        } catch (Throwable e2) {
            FileLog.m611e("tmessages", e2);
        }
    }

    private void checkScreenshots(ArrayList<Long> dates) {
        if (dates != null && !dates.isEmpty() && this.lastSecretChatEnterTime != 0 && this.lastSecretChat != null && (this.lastSecretChat instanceof TL_encryptedChat)) {
            boolean send = false;
            Iterator i$ = dates.iterator();
            while (i$.hasNext()) {
                Long date = (Long) i$.next();
                if ((this.lastMediaCheckTime == 0 || date.longValue() > this.lastMediaCheckTime) && date.longValue() >= this.lastSecretChatEnterTime) {
                    if (this.lastSecretChatLeaveTime == 0 || date.longValue() <= this.lastSecretChatLeaveTime + 2000) {
                        this.lastMediaCheckTime = Math.max(this.lastMediaCheckTime, date.longValue());
                        send = true;
                    }
                }
            }
            if (send) {
                SecretChatHelper.getInstance().sendScreenshotMessage(this.lastSecretChat, this.lastSecretChatVisibleMessages, null);
            }
        }
    }

    public void setLastEncryptedChatParams(long enterTime, long leaveTime, EncryptedChat encryptedChat, ArrayList<Long> visibleMessages) {
        this.lastSecretChatEnterTime = enterTime;
        this.lastSecretChatLeaveTime = leaveTime;
        this.lastSecretChat = encryptedChat;
        this.lastSecretChatVisibleMessages = visibleMessages;
    }

    public int generateObserverTag() {
        int i = this.lastTag;
        this.lastTag = i + 1;
        return i;
    }

    public void addLoadingFileObserver(String fileName, FileDownloadProgressListener observer) {
        if (this.listenerInProgress) {
            this.addLaterArray.put(fileName, observer);
            return;
        }
        removeLoadingFileObserver(observer);
        ArrayList<WeakReference<FileDownloadProgressListener>> arrayList = (ArrayList) this.loadingFileObservers.get(fileName);
        if (arrayList == null) {
            arrayList = new ArrayList();
            this.loadingFileObservers.put(fileName, arrayList);
        }
        arrayList.add(new WeakReference(observer));
        this.observersByTag.put(Integer.valueOf(observer.getObserverTag()), fileName);
    }

    public void removeLoadingFileObserver(FileDownloadProgressListener observer) {
        if (this.listenerInProgress) {
            this.deleteLaterArray.add(observer);
            return;
        }
        String fileName = (String) this.observersByTag.get(Integer.valueOf(observer.getObserverTag()));
        if (fileName != null) {
            ArrayList<WeakReference<FileDownloadProgressListener>> arrayList = (ArrayList) this.loadingFileObservers.get(fileName);
            if (arrayList != null) {
                int a = 0;
                while (a < arrayList.size()) {
                    WeakReference<FileDownloadProgressListener> reference = (WeakReference) arrayList.get(a);
                    if (reference.get() == null || reference.get() == observer) {
                        arrayList.remove(a);
                        a--;
                    }
                    a++;
                }
                if (arrayList.isEmpty()) {
                    this.loadingFileObservers.remove(fileName);
                }
            }
            this.observersByTag.remove(Integer.valueOf(observer.getObserverTag()));
        }
    }

    private void processLaterArrays() {
        for (Entry<String, FileDownloadProgressListener> listener : this.addLaterArray.entrySet()) {
            addLoadingFileObserver((String) listener.getKey(), (FileDownloadProgressListener) listener.getValue());
        }
        this.addLaterArray.clear();
        Iterator i$ = this.deleteLaterArray.iterator();
        while (i$.hasNext()) {
            removeLoadingFileObserver((FileDownloadProgressListener) i$.next());
        }
        this.deleteLaterArray.clear();
    }

    public void didReceivedNotification(int id, Object... args) {
        String fileName;
        ArrayList<WeakReference<FileDownloadProgressListener>> arrayList;
        Iterator i$;
        WeakReference<FileDownloadProgressListener> reference;
        if (id == NotificationCenter.FileDidFailedLoad) {
            this.listenerInProgress = true;
            fileName = args[0];
            arrayList = (ArrayList) this.loadingFileObservers.get(fileName);
            if (arrayList != null) {
                i$ = arrayList.iterator();
                while (i$.hasNext()) {
                    reference = (WeakReference) i$.next();
                    if (reference.get() != null) {
                        ((FileDownloadProgressListener) reference.get()).onFailedDownload(fileName);
                        this.observersByTag.remove(Integer.valueOf(((FileDownloadProgressListener) reference.get()).getObserverTag()));
                    }
                }
                this.loadingFileObservers.remove(fileName);
            }
            this.listenerInProgress = false;
            processLaterArrays();
            checkDownloadFinished(fileName, ((Integer) args[1]).intValue());
        } else if (id == NotificationCenter.FileDidLoaded) {
            this.listenerInProgress = true;
            fileName = (String) args[0];
            if (this.downloadingCurrentMessage && this.playingMessageObject != null && FileLoader.getAttachFileName(this.playingMessageObject.messageOwner.media.document).equals(fileName)) {
                this.playMusicAgain = true;
                playAudio(this.playingMessageObject);
            }
            arrayList = (ArrayList) this.loadingFileObservers.get(fileName);
            if (arrayList != null) {
                i$ = arrayList.iterator();
                while (i$.hasNext()) {
                    reference = (WeakReference) i$.next();
                    if (reference.get() != null) {
                        ((FileDownloadProgressListener) reference.get()).onSuccessDownload(fileName);
                        this.observersByTag.remove(Integer.valueOf(((FileDownloadProgressListener) reference.get()).getObserverTag()));
                    }
                }
                this.loadingFileObservers.remove(fileName);
            }
            this.listenerInProgress = false;
            processLaterArrays();
            checkDownloadFinished(fileName, 0);
        } else if (id == NotificationCenter.FileLoadProgressChanged) {
            this.listenerInProgress = true;
            fileName = (String) args[0];
            arrayList = (ArrayList) this.loadingFileObservers.get(fileName);
            if (arrayList != null) {
                progress = args[1];
                i$ = arrayList.iterator();
                while (i$.hasNext()) {
                    reference = (WeakReference) i$.next();
                    if (reference.get() != null) {
                        ((FileDownloadProgressListener) reference.get()).onProgressDownload(fileName, progress.floatValue());
                    }
                }
            }
            this.listenerInProgress = false;
            processLaterArrays();
        } else if (id == NotificationCenter.FileUploadProgressChanged) {
            this.listenerInProgress = true;
            fileName = (String) args[0];
            arrayList = (ArrayList) this.loadingFileObservers.get(fileName);
            if (arrayList != null) {
                progress = (Float) args[1];
                Boolean enc = args[2];
                i$ = arrayList.iterator();
                while (i$.hasNext()) {
                    reference = (WeakReference) i$.next();
                    if (reference.get() != null) {
                        ((FileDownloadProgressListener) reference.get()).onProgressUpload(fileName, progress.floatValue(), enc.booleanValue());
                    }
                }
            }
            this.listenerInProgress = false;
            processLaterArrays();
            try {
                ArrayList<DelayedMessage> delayedMessages = SendMessagesHelper.getInstance().getDelayedMessages(fileName);
                if (delayedMessages != null) {
                    for (int a = 0; a < delayedMessages.size(); a++) {
                        DelayedMessage delayedMessage = (DelayedMessage) delayedMessages.get(a);
                        if (delayedMessage.encryptedChat == null) {
                            long dialog_id = delayedMessage.obj.getDialogId();
                            Long lastTime = (Long) this.typingTimes.get(Long.valueOf(dialog_id));
                            if (lastTime == null || lastTime.longValue() + 4000 < System.currentTimeMillis()) {
                                if (delayedMessage.videoLocation != null) {
                                    MessagesController.getInstance().sendTyping(dialog_id, 5, 0);
                                } else if (delayedMessage.documentLocation != null) {
                                    MessagesController.getInstance().sendTyping(dialog_id, 3, 0);
                                } else if (delayedMessage.location != null) {
                                    MessagesController.getInstance().sendTyping(dialog_id, 4, 0);
                                }
                                this.typingTimes.put(Long.valueOf(dialog_id), Long.valueOf(System.currentTimeMillis()));
                            }
                        }
                    }
                }
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
        } else if (id == NotificationCenter.messagesDeleted) {
            if (this.playingMessageObject != null && ((Integer) args[1]).intValue() == this.playingMessageObject.messageOwner.to_id.channel_id) {
                if (args[0].contains(Integer.valueOf(this.playingMessageObject.getId()))) {
                    cleanupPlayer(false, true);
                }
            }
        } else if (id == NotificationCenter.removeAllMessagesFromDialog) {
            did = ((Long) args[0]).longValue();
            if (this.playingMessageObject != null && this.playingMessageObject.getDialogId() == did) {
                cleanupPlayer(false, true);
            }
        } else if (id == NotificationCenter.musicDidLoaded) {
            did = ((Long) args[0]).longValue();
            if (this.playingMessageObject != null && this.playingMessageObject.isMusic() && this.playingMessageObject.getDialogId() == did) {
                ArrayList<MessageObject> arrayList2 = args[1];
                this.playlist.addAll(0, arrayList2);
                if (this.shuffleMusic) {
                    buildShuffledPlayList();
                    this.currentPlaylistNum = 0;
                    return;
                }
                this.currentPlaylistNum += arrayList2.size();
            }
        }
    }

    private void checkDecoderQueue() {
        this.fileDecodingQueue.postRunnable(new C04236());
    }

    private void checkPlayerQueue() {
        this.playerQueue.postRunnable(new C04257());
    }

    private boolean isNearToSensor(float value) {
        return value < 5.0f && value != this.proximitySensor.getMaximumRange();
    }

    public void onSensorChanged(SensorEvent event) {
        FileLog.m609e("tmessages", "proximity changed to " + event.values[0]);
        if ((this.proximitySensor == null || this.audioTrackPlayer != null || this.audioPlayer != null) && !this.isPaused && this.useFrontSpeaker != isNearToSensor(event.values[0])) {
            boolean newValue = isNearToSensor(event.values[0]);
            if (newValue) {
                try {
                    if (NotificationsController.getInstance().audioManager.isWiredHeadsetOn()) {
                        return;
                    }
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
            this.ignoreProximity = true;
            this.useFrontSpeaker = newValue;
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.audioRouteChanged, Boolean.valueOf(this.useFrontSpeaker));
            MessageObject currentMessageObject = this.playingMessageObject;
            float progress = this.playingMessageObject.audioProgress;
            cleanupPlayer(false, true);
            currentMessageObject.audioProgress = progress;
            playAudio(currentMessageObject);
            this.ignoreProximity = false;
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void stopProximitySensor() {
        if (!this.ignoreProximity) {
            try {
                this.useFrontSpeaker = false;
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.audioRouteChanged, Boolean.valueOf(this.useFrontSpeaker));
                if (!(this.sensorManager == null || this.proximitySensor == null)) {
                    this.sensorManager.unregisterListener(this);
                }
                if (this.proximityWakeLock != null && this.proximityWakeLock.isHeld()) {
                    this.proximityWakeLock.release();
                }
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
        }
    }

    private void startProximitySensor() {
        if (!this.ignoreProximity) {
            try {
                if (!(this.sensorManager == null || this.proximitySensor == null)) {
                    this.sensorManager.registerListener(this, this.proximitySensor, 3);
                }
                if (!NotificationsController.getInstance().audioManager.isWiredHeadsetOn() && this.proximityWakeLock != null && !this.proximityWakeLock.isHeld()) {
                    this.proximityWakeLock.acquire();
                }
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
        }
    }

    public void cleanupPlayer(boolean notify, boolean stopService) {
        stopProximitySensor();
        if (this.audioPlayer != null) {
            try {
                this.audioPlayer.reset();
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
            try {
                this.audioPlayer.stop();
            } catch (Throwable e2) {
                FileLog.m611e("tmessages", e2);
            }
            try {
                this.audioPlayer.release();
                this.audioPlayer = null;
            } catch (Throwable e22) {
                FileLog.m611e("tmessages", e22);
            }
        } else if (this.audioTrackPlayer != null) {
            synchronized (this.playerObjectSync) {
                try {
                    this.audioTrackPlayer.pause();
                    this.audioTrackPlayer.flush();
                } catch (Throwable e222) {
                    FileLog.m611e("tmessages", e222);
                }
                try {
                    this.audioTrackPlayer.release();
                    this.audioTrackPlayer = null;
                } catch (Throwable e2222) {
                    FileLog.m611e("tmessages", e2222);
                }
            }
        }
        stopProgressTimer();
        this.lastProgress = 0;
        this.buffersWrited = 0;
        this.isPaused = false;
        if (this.playingMessageObject != null) {
            if (this.downloadingCurrentMessage) {
                FileLoader.getInstance().cancelLoadFile(this.playingMessageObject.messageOwner.media.document);
            }
            MessageObject lastFile = this.playingMessageObject;
            this.playingMessageObject.audioProgress = 0.0f;
            this.playingMessageObject.audioProgressSec = 0;
            this.playingMessageObject = null;
            this.downloadingCurrentMessage = false;
            if (notify) {
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.audioDidReset, Integer.valueOf(lastFile.getId()), Boolean.valueOf(stopService));
            }
            if (stopService) {
                ApplicationLoader.applicationContext.stopService(new Intent(ApplicationLoader.applicationContext, MusicPlayerService.class));
            }
        }
    }

    private void seekOpusPlayer(final float progress) {
        if (((float) this.currentTotalPcmDuration) * progress != ((float) this.currentTotalPcmDuration)) {
            if (!this.isPaused) {
                this.audioTrackPlayer.pause();
            }
            this.audioTrackPlayer.flush();
            this.fileDecodingQueue.postRunnable(new Runnable() {

                class C04261 implements Runnable {
                    C04261() {
                    }

                    public void run() {
                        if (!MediaController.this.isPaused) {
                            MediaController.this.ignoreFirstProgress = 3;
                            MediaController.this.lastPlayPcm = (long) (((float) MediaController.this.currentTotalPcmDuration) * progress);
                            if (MediaController.this.audioTrackPlayer != null) {
                                MediaController.this.audioTrackPlayer.play();
                            }
                            MediaController.this.lastProgress = (int) ((((float) MediaController.this.currentTotalPcmDuration) / 48.0f) * progress);
                            MediaController.this.checkPlayerQueue();
                        }
                    }
                }

                public void run() {
                    MediaController.this.seekOpusFile(progress);
                    synchronized (MediaController.this.playerSync) {
                        MediaController.this.freePlayerBuffers.addAll(MediaController.this.usedPlayerBuffers);
                        MediaController.this.usedPlayerBuffers.clear();
                    }
                    AndroidUtilities.runOnUIThread(new C04261());
                }
            });
        }
    }

    public boolean seekToProgress(MessageObject messageObject, float progress) {
        if ((this.audioTrackPlayer == null && this.audioPlayer == null) || messageObject == null || this.playingMessageObject == null) {
            return false;
        }
        if (this.playingMessageObject != null && this.playingMessageObject.getId() != messageObject.getId()) {
            return false;
        }
        try {
            if (this.audioPlayer != null) {
                int seekTo = (int) (((float) this.audioPlayer.getDuration()) * progress);
                this.audioPlayer.seekTo(seekTo);
                this.lastProgress = seekTo;
            } else if (this.audioTrackPlayer != null) {
                seekOpusPlayer(progress);
            }
            return true;
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
            return false;
        }
    }

    public MessageObject getPlayingMessageObject() {
        return this.playingMessageObject;
    }

    private void buildShuffledPlayList() {
        if (!this.playlist.isEmpty()) {
            ArrayList<MessageObject> all = new ArrayList(this.playlist);
            this.shuffledPlaylist.clear();
            MessageObject messageObject = (MessageObject) this.playlist.get(this.currentPlaylistNum);
            all.remove(this.currentPlaylistNum);
            this.shuffledPlaylist.add(messageObject);
            int count = all.size();
            for (int a = 0; a < count; a++) {
                int index = Utilities.random.nextInt(all.size());
                this.shuffledPlaylist.add(all.get(index));
                all.remove(index);
            }
        }
    }

    public boolean setPlaylist(ArrayList<MessageObject> messageObjects, MessageObject current) {
        if (this.playingMessageObject == current) {
            return playAudio(current);
        }
        boolean z;
        if (this.playlist.isEmpty()) {
            z = false;
        } else {
            z = true;
        }
        this.playMusicAgain = z;
        this.playlist.clear();
        for (int a = messageObjects.size() - 1; a >= 0; a--) {
            MessageObject messageObject = (MessageObject) messageObjects.get(a);
            if (messageObject.isMusic()) {
                this.playlist.add(messageObject);
            }
        }
        this.currentPlaylistNum = this.playlist.indexOf(current);
        if (this.currentPlaylistNum == -1) {
            this.playlist.clear();
            this.shuffledPlaylist.clear();
            return false;
        }
        if (this.shuffleMusic) {
            buildShuffledPlayList();
            this.currentPlaylistNum = 0;
        }
        SharedMediaQuery.loadMusic(current.getDialogId(), ((MessageObject) this.playlist.get(0)).getId());
        return playAudio(current);
    }

    public void playNextMessage() {
        playNextMessage(false);
    }

    private void playNextMessage(boolean byStop) {
        ArrayList<MessageObject> currentPlayList = this.shuffleMusic ? this.shuffledPlaylist : this.playlist;
        if (byStop && this.repeatMode == 2) {
            cleanupPlayer(false, false);
            playAudio((MessageObject) currentPlayList.get(this.currentPlaylistNum));
            return;
        }
        this.currentPlaylistNum++;
        if (this.currentPlaylistNum >= currentPlayList.size()) {
            this.currentPlaylistNum = 0;
            if (byStop && this.repeatMode == 0) {
                stopProximitySensor();
                if (this.audioPlayer != null || this.audioTrackPlayer != null) {
                    if (this.audioPlayer != null) {
                        try {
                            this.audioPlayer.stop();
                        } catch (Throwable e) {
                            FileLog.m611e("tmessages", e);
                        }
                        try {
                            this.audioPlayer.release();
                            this.audioPlayer = null;
                        } catch (Throwable e2) {
                            FileLog.m611e("tmessages", e2);
                        }
                    } else if (this.audioTrackPlayer != null) {
                        synchronized (this.playerObjectSync) {
                            try {
                                this.audioTrackPlayer.pause();
                                this.audioTrackPlayer.flush();
                            } catch (Throwable e22) {
                                FileLog.m611e("tmessages", e22);
                            }
                            try {
                                this.audioTrackPlayer.release();
                                this.audioTrackPlayer = null;
                            } catch (Throwable e222) {
                                FileLog.m611e("tmessages", e222);
                            }
                        }
                    }
                    stopProgressTimer();
                    this.lastProgress = 0;
                    this.buffersWrited = 0;
                    this.isPaused = true;
                    this.playingMessageObject.audioProgress = 0.0f;
                    this.playingMessageObject.audioProgressSec = 0;
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.audioPlayStateChanged, Integer.valueOf(this.playingMessageObject.getId()));
                    return;
                }
                return;
            }
        }
        if (this.currentPlaylistNum >= 0 && this.currentPlaylistNum < currentPlayList.size()) {
            this.playMusicAgain = true;
            playAudio((MessageObject) currentPlayList.get(this.currentPlaylistNum));
        }
    }

    public void playPreviousMessage() {
        ArrayList<MessageObject> currentPlayList = this.shuffleMusic ? this.shuffledPlaylist : this.playlist;
        this.currentPlaylistNum--;
        if (this.currentPlaylistNum < 0) {
            this.currentPlaylistNum = currentPlayList.size() - 1;
        }
        if (this.currentPlaylistNum >= 0 && this.currentPlaylistNum < currentPlayList.size()) {
            this.playMusicAgain = true;
            playAudio((MessageObject) currentPlayList.get(this.currentPlaylistNum));
        }
    }

    private void checkIsNextMusicFileDownloaded() {
        ArrayList<MessageObject> currentPlayList = this.shuffleMusic ? this.shuffledPlaylist : this.playlist;
        if (currentPlayList != null && currentPlayList.size() >= 2) {
            int nextIndex = this.currentPlaylistNum + 1;
            if (nextIndex >= currentPlayList.size()) {
                nextIndex = 0;
            }
            MessageObject nextAudio = (MessageObject) currentPlayList.get(nextIndex);
            File file = null;
            if (nextAudio.messageOwner.attachPath != null && nextAudio.messageOwner.attachPath.length() > 0) {
                file = new File(nextAudio.messageOwner.attachPath);
                if (!file.exists()) {
                    file = null;
                }
            }
            File cacheFile = file != null ? file : FileLoader.getPathToMessage(nextAudio.messageOwner);
            boolean z;
            if (cacheFile == null || !cacheFile.exists()) {
                z = false;
            } else {
                z = true;
            }
            if (cacheFile != null && cacheFile != file && !cacheFile.exists() && nextAudio.isMusic()) {
                FileLoader.getInstance().loadFile(nextAudio.messageOwner.media.document, true, false);
            }
        }
    }

    public boolean playAudio(MessageObject messageObject) {
        if (messageObject == null) {
            return false;
        }
        if ((this.audioTrackPlayer == null && this.audioPlayer == null) || this.playingMessageObject == null || messageObject.getId() != this.playingMessageObject.getId()) {
            File cacheFile;
            if (this.audioTrackPlayer != null) {
                MusicPlayerService.setIgnoreAudioFocus();
            }
            cleanupPlayer(!this.playMusicAgain, false);
            this.playMusicAgain = false;
            File file = null;
            if (messageObject.messageOwner.attachPath != null && messageObject.messageOwner.attachPath.length() > 0) {
                file = new File(messageObject.messageOwner.attachPath);
                if (!file.exists()) {
                    file = null;
                }
            }
            if (file != null) {
                cacheFile = file;
            } else {
                cacheFile = FileLoader.getPathToMessage(messageObject.messageOwner);
            }
            if (cacheFile == null || cacheFile == file || cacheFile.exists() || !messageObject.isMusic()) {
                this.downloadingCurrentMessage = false;
                if (messageObject.isMusic()) {
                    checkIsNextMusicFileDownloaded();
                }
                if (isOpusFile(cacheFile.getAbsolutePath()) == 1) {
                    this.playlist.clear();
                    this.shuffledPlaylist.clear();
                    synchronized (this.playerObjectSync) {
                        try {
                            this.ignoreFirstProgress = 3;
                            Semaphore semaphore = new Semaphore(0);
                            final Boolean[] result = new Boolean[1];
                            final Semaphore semaphore2 = semaphore;
                            this.fileDecodingQueue.postRunnable(new Runnable() {
                                public void run() {
                                    boolean z;
                                    Boolean[] boolArr = result;
                                    if (MediaController.this.openOpusFile(cacheFile.getAbsolutePath()) != 0) {
                                        z = true;
                                    } else {
                                        z = false;
                                    }
                                    boolArr[0] = Boolean.valueOf(z);
                                    semaphore2.release();
                                }
                            });
                            semaphore.acquire();
                            if (result[0].booleanValue()) {
                                this.currentTotalPcmDuration = getTotalPcmDuration();
                                this.audioTrackPlayer = new AudioTrack(this.useFrontSpeaker ? 0 : 3, 48000, 4, 2, this.playerBufferSize, 1);
                                this.audioTrackPlayer.setStereoVolume(1.0f, 1.0f);
                                this.audioTrackPlayer.setPlaybackPositionUpdateListener(new OnPlaybackPositionUpdateListener() {
                                    public void onMarkerReached(AudioTrack audioTrack) {
                                        MediaController.this.cleanupPlayer(true, true);
                                    }

                                    public void onPeriodicNotification(AudioTrack audioTrack) {
                                    }
                                });
                                this.audioTrackPlayer.play();
                                startProgressTimer();
                                if (messageObject.messageOwner.media instanceof TL_messageMediaAudio) {
                                    startProximitySensor();
                                }
                            } else {
                                return false;
                            }
                        } catch (Throwable e) {
                            FileLog.m611e("tmessages", e);
                            if (this.audioTrackPlayer != null) {
                                this.audioTrackPlayer.release();
                                this.audioTrackPlayer = null;
                                this.isPaused = false;
                                this.playingMessageObject = null;
                                this.downloadingCurrentMessage = false;
                            }
                            return false;
                        }
                    }
                }
                try {
                    this.audioPlayer = new MediaPlayer();
                    this.audioPlayer.setAudioStreamType(this.useFrontSpeaker ? 0 : 3);
                    this.audioPlayer.setDataSource(cacheFile.getAbsolutePath());
                    this.audioPlayer.setOnCompletionListener(new OnCompletionListener() {
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            if (MediaController.this.playlist.isEmpty() || MediaController.this.playlist.size() <= 1) {
                                MediaController.this.cleanupPlayer(true, true);
                            } else {
                                MediaController.this.playNextMessage(true);
                            }
                        }
                    });
                    this.audioPlayer.prepare();
                    this.audioPlayer.start();
                    startProgressTimer();
                    if (messageObject.messageOwner.media instanceof TL_messageMediaAudio) {
                        this.audioInfo = null;
                        this.playlist.clear();
                        this.shuffledPlaylist.clear();
                        startProximitySensor();
                    } else {
                        try {
                            this.audioInfo = AudioInfo.getAudioInfo(cacheFile);
                        } catch (Throwable e2) {
                            FileLog.m611e("tmessages", e2);
                        }
                    }
                } catch (Throwable e22) {
                    FileLog.m611e("tmessages", e22);
                    NotificationCenter instance = NotificationCenter.getInstance();
                    int i = NotificationCenter.audioPlayStateChanged;
                    Object[] objArr = new Object[1];
                    objArr[0] = Integer.valueOf(this.playingMessageObject != null ? this.playingMessageObject.getId() : 0);
                    instance.postNotificationName(i, objArr);
                    if (this.audioPlayer != null) {
                        this.audioPlayer.release();
                        this.audioPlayer = null;
                        this.isPaused = false;
                        this.playingMessageObject = null;
                        this.downloadingCurrentMessage = false;
                    }
                    return false;
                }
                this.isPaused = false;
                this.lastProgress = 0;
                this.lastPlayPcm = 0;
                this.playingMessageObject = messageObject;
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.audioDidStarted, messageObject);
                if (this.audioPlayer != null) {
                    try {
                        if (this.playingMessageObject.audioProgress != 0.0f) {
                            this.audioPlayer.seekTo((int) (((float) this.audioPlayer.getDuration()) * this.playingMessageObject.audioProgress));
                        }
                    } catch (Throwable e23) {
                        this.playingMessageObject.audioProgress = 0.0f;
                        this.playingMessageObject.audioProgressSec = 0;
                        FileLog.m611e("tmessages", e23);
                    }
                } else if (this.audioTrackPlayer != null) {
                    if (this.playingMessageObject.audioProgress == 1.0f) {
                        this.playingMessageObject.audioProgress = 0.0f;
                    }
                    this.fileDecodingQueue.postRunnable(new Runnable() {
                        public void run() {
                            try {
                                if (!(MediaController.this.playingMessageObject == null || MediaController.this.playingMessageObject.audioProgress == 0.0f)) {
                                    MediaController.this.lastPlayPcm = (long) (((float) MediaController.this.currentTotalPcmDuration) * MediaController.this.playingMessageObject.audioProgress);
                                    MediaController.this.seekOpusFile(MediaController.this.playingMessageObject.audioProgress);
                                }
                            } catch (Throwable e) {
                                FileLog.m611e("tmessages", e);
                            }
                            synchronized (MediaController.this.playerSync) {
                                MediaController.this.freePlayerBuffers.addAll(MediaController.this.usedPlayerBuffers);
                                MediaController.this.usedPlayerBuffers.clear();
                            }
                            MediaController.this.decodingFinished = false;
                            MediaController.this.checkPlayerQueue();
                        }
                    });
                }
                if (this.playingMessageObject.messageOwner.media.document != null) {
                    ApplicationLoader.applicationContext.startService(new Intent(ApplicationLoader.applicationContext, MusicPlayerService.class));
                } else {
                    ApplicationLoader.applicationContext.stopService(new Intent(ApplicationLoader.applicationContext, MusicPlayerService.class));
                }
                return true;
            }
            FileLoader.getInstance().loadFile(messageObject.messageOwner.media.document, true, false);
            this.downloadingCurrentMessage = true;
            this.isPaused = false;
            this.lastProgress = 0;
            this.lastPlayPcm = 0;
            this.audioInfo = null;
            this.playingMessageObject = messageObject;
            if (this.playingMessageObject.messageOwner.media.document != null) {
                ApplicationLoader.applicationContext.startService(new Intent(ApplicationLoader.applicationContext, MusicPlayerService.class));
            } else {
                ApplicationLoader.applicationContext.stopService(new Intent(ApplicationLoader.applicationContext, MusicPlayerService.class));
            }
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.audioPlayStateChanged, Integer.valueOf(this.playingMessageObject.getId()));
            return true;
        }
        if (this.isPaused) {
            resumeAudio(messageObject);
        }
        return true;
    }

    public void stopAudio() {
        stopProximitySensor();
        if ((this.audioTrackPlayer != null || this.audioPlayer != null) && this.playingMessageObject != null) {
            try {
                if (this.audioPlayer != null) {
                    this.audioPlayer.stop();
                } else if (this.audioTrackPlayer != null) {
                    this.audioTrackPlayer.pause();
                    this.audioTrackPlayer.flush();
                }
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
            try {
                if (this.audioPlayer != null) {
                    this.audioPlayer.release();
                    this.audioPlayer = null;
                } else if (this.audioTrackPlayer != null) {
                    synchronized (this.playerObjectSync) {
                        this.audioTrackPlayer.release();
                        this.audioTrackPlayer = null;
                    }
                }
            } catch (Throwable e2) {
                FileLog.m611e("tmessages", e2);
            }
            stopProgressTimer();
            this.playingMessageObject = null;
            this.downloadingCurrentMessage = false;
            this.isPaused = false;
            ApplicationLoader.applicationContext.stopService(new Intent(ApplicationLoader.applicationContext, MusicPlayerService.class));
        }
    }

    public AudioInfo getAudioInfo() {
        return this.audioInfo;
    }

    public boolean isShuffleMusic() {
        return this.shuffleMusic;
    }

    public int getRepeatMode() {
        return this.repeatMode;
    }

    public void toggleShuffleMusic() {
        boolean z;
        if (this.shuffleMusic) {
            z = false;
        } else {
            z = true;
        }
        this.shuffleMusic = z;
        Editor editor = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0).edit();
        editor.putBoolean("shuffleMusic", this.shuffleMusic);
        editor.commit();
        if (this.shuffleMusic) {
            buildShuffledPlayList();
            this.currentPlaylistNum = 0;
        } else if (this.playingMessageObject != null) {
            this.currentPlaylistNum = this.playlist.indexOf(this.playingMessageObject);
            if (this.currentPlaylistNum == -1) {
                this.playlist.clear();
                this.shuffledPlaylist.clear();
                cleanupPlayer(true, true);
            }
        }
    }

    public void toggleRepeatMode() {
        this.repeatMode++;
        if (this.repeatMode > 2) {
            this.repeatMode = 0;
        }
        Editor editor = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0).edit();
        editor.putInt("repeatMode", this.repeatMode);
        editor.commit();
    }

    public boolean pauseAudio(MessageObject messageObject) {
        stopProximitySensor();
        if ((this.audioTrackPlayer == null && this.audioPlayer == null) || messageObject == null || this.playingMessageObject == null) {
            return false;
        }
        if (this.playingMessageObject != null && this.playingMessageObject.getId() != messageObject.getId()) {
            return false;
        }
        stopProgressTimer();
        try {
            if (this.audioPlayer != null) {
                this.audioPlayer.pause();
            } else if (this.audioTrackPlayer != null) {
                this.audioTrackPlayer.pause();
            }
            this.isPaused = true;
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.audioPlayStateChanged, Integer.valueOf(this.playingMessageObject.getId()));
            return true;
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
            this.isPaused = false;
            return false;
        }
    }

    public boolean resumeAudio(MessageObject messageObject) {
        if ((this.audioTrackPlayer == null && this.audioPlayer == null) || messageObject == null || this.playingMessageObject == null) {
            return false;
        }
        if (this.playingMessageObject != null && this.playingMessageObject.getId() != messageObject.getId()) {
            return false;
        }
        if (messageObject.messageOwner.media instanceof TL_messageMediaAudio) {
            startProximitySensor();
        }
        try {
            startProgressTimer();
            if (this.audioPlayer != null) {
                this.audioPlayer.start();
            } else if (this.audioTrackPlayer != null) {
                this.audioTrackPlayer.play();
                checkPlayerQueue();
            }
            this.isPaused = false;
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.audioPlayStateChanged, Integer.valueOf(this.playingMessageObject.getId()));
            return true;
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
            return false;
        }
    }

    public boolean isPlayingAudio(MessageObject messageObject) {
        return ((this.audioTrackPlayer == null && this.audioPlayer == null) || messageObject == null || this.playingMessageObject == null || (this.playingMessageObject != null && (this.playingMessageObject.getId() != messageObject.getId() || this.downloadingCurrentMessage))) ? false : true;
    }

    public boolean isAudioPaused() {
        return this.isPaused || this.downloadingCurrentMessage;
    }

    public boolean isDownloadingCurrentMessage() {
        return this.downloadingCurrentMessage;
    }

    public void startRecording(long dialog_id, MessageObject reply_to_msg, boolean asAdmin) {
        boolean paused = false;
        if (!(this.playingMessageObject == null || !isPlayingAudio(this.playingMessageObject) || isAudioPaused())) {
            paused = true;
            pauseAudio(this.playingMessageObject);
        }
        try {
            ((Vibrator) ApplicationLoader.applicationContext.getSystemService("vibrator")).vibrate(20);
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
        }
        DispatchQueue dispatchQueue = this.recordQueue;
        final long j = dialog_id;
        final MessageObject messageObject = reply_to_msg;
        final boolean z = asAdmin;
        Runnable anonymousClass13 = new Runnable() {

            class C04081 implements Runnable {
                C04081() {
                }

                public void run() {
                    MediaController.this.recordStartRunnable = null;
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.recordStartError, new Object[0]);
                }
            }

            class C04092 implements Runnable {
                C04092() {
                }

                public void run() {
                    MediaController.this.recordStartRunnable = null;
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.recordStartError, new Object[0]);
                }
            }

            class C04103 implements Runnable {
                C04103() {
                }

                public void run() {
                    MediaController.this.recordStartRunnable = null;
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.recordStartError, new Object[0]);
                }
            }

            class C04114 implements Runnable {
                C04114() {
                }

                public void run() {
                    MediaController.this.recordStartRunnable = null;
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.recordStarted, new Object[0]);
                }
            }

            public void run() {
                if (MediaController.this.audioRecorder != null) {
                    AndroidUtilities.runOnUIThread(new C04081());
                    return;
                }
                MediaController.this.recordingAudio = new TL_audio();
                MediaController.this.recordingAudio.dc_id = Integer.MIN_VALUE;
                MediaController.this.recordingAudio.id = (long) UserConfig.lastLocalId;
                MediaController.this.recordingAudio.user_id = UserConfig.getClientUserId();
                MediaController.this.recordingAudio.mime_type = "audio/ogg";
                UserConfig.lastLocalId--;
                UserConfig.saveConfig(false);
                MediaController.this.recordingAudioFile = new File(FileLoader.getInstance().getDirectory(4), FileLoader.getAttachFileName(MediaController.this.recordingAudio));
                try {
                    if (MediaController.this.startRecord(MediaController.this.recordingAudioFile.getAbsolutePath()) == 0) {
                        AndroidUtilities.runOnUIThread(new C04092());
                        return;
                    }
                    MediaController.this.audioRecorder = new AudioRecord(1, 16000, 16, 2, MediaController.this.recordBufferSize * 10);
                    MediaController.this.recordStartTime = System.currentTimeMillis();
                    MediaController.this.recordTimeCount = 0;
                    MediaController.this.recordDialogId = j;
                    MediaController.this.recordReplyingMessageObject = messageObject;
                    MediaController.this.recordAsAdmin = z;
                    MediaController.this.fileBuffer.rewind();
                    MediaController.this.audioRecorder.startRecording();
                    MediaController.this.recordQueue.postRunnable(MediaController.this.recordRunnable);
                    AndroidUtilities.runOnUIThread(new C04114());
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                    MediaController.this.recordingAudio = null;
                    MediaController.this.stopRecord();
                    MediaController.this.recordingAudioFile.delete();
                    MediaController.this.recordingAudioFile = null;
                    try {
                        MediaController.this.audioRecorder.release();
                        MediaController.this.audioRecorder = null;
                    } catch (Throwable e2) {
                        FileLog.m611e("tmessages", e2);
                    }
                    AndroidUtilities.runOnUIThread(new C04103());
                }
            }
        };
        this.recordStartRunnable = anonymousClass13;
        if (paused) {
            j = 500;
        } else {
            j = 0;
        }
        dispatchQueue.postRunnable(anonymousClass13, j);
    }

    private void stopRecordingInternal(boolean send) {
        if (send) {
            final TL_audio audioToSend = this.recordingAudio;
            final File recordingAudioFileToSend = this.recordingAudioFile;
            this.fileEncodingQueue.postRunnable(new Runnable() {

                class C04121 implements Runnable {
                    C04121() {
                    }

                    public void run() {
                        audioToSend.date = ConnectionsManager.getInstance().getCurrentTime();
                        audioToSend.size = (int) recordingAudioFileToSend.length();
                        long duration = MediaController.this.recordTimeCount;
                        audioToSend.duration = (int) (duration / 1000);
                        if (duration > 700) {
                            SendMessagesHelper.getInstance().sendMessage(audioToSend, recordingAudioFileToSend.getAbsolutePath(), MediaController.this.recordDialogId, MediaController.this.recordReplyingMessageObject, MediaController.this.recordAsAdmin);
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.audioDidSent, new Object[0]);
                            return;
                        }
                        recordingAudioFileToSend.delete();
                    }
                }

                public void run() {
                    MediaController.this.stopRecord();
                    AndroidUtilities.runOnUIThread(new C04121());
                }
            });
        }
        try {
            if (this.audioRecorder != null) {
                this.audioRecorder.release();
                this.audioRecorder = null;
            }
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
        }
        this.recordingAudio = null;
        this.recordingAudioFile = null;
    }

    public void stopRecording(final boolean send) {
        if (this.recordStartRunnable != null) {
            this.recordQueue.cancelRunnable(this.recordStartRunnable);
        }
        this.recordQueue.postRunnable(new Runnable() {

            class C04131 implements Runnable {
                C04131() {
                }

                public void run() {
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.recordStopped, new Object[0]);
                }
            }

            public void run() {
                if (MediaController.this.audioRecorder != null) {
                    try {
                        MediaController.this.sendAfterDone = send;
                        MediaController.this.audioRecorder.stop();
                    } catch (Throwable e) {
                        FileLog.m611e("tmessages", e);
                        if (MediaController.this.recordingAudioFile != null) {
                            MediaController.this.recordingAudioFile.delete();
                        }
                    }
                    if (!send) {
                        MediaController.this.stopRecordingInternal(false);
                    }
                    try {
                        ((Vibrator) ApplicationLoader.applicationContext.getSystemService("vibrator")).vibrate(20);
                    } catch (Throwable e2) {
                        FileLog.m611e("tmessages", e2);
                    }
                    AndroidUtilities.runOnUIThread(new C04131());
                }
            }
        });
    }

    public static void saveFile(String fullPath, Context context, final int type, final String name) {
        Throwable e;
        final ProgressDialog finalProgress;
        if (fullPath != null) {
            File file = null;
            if (!(fullPath == null || fullPath.length() == 0)) {
                file = new File(fullPath);
                if (!file.exists()) {
                    file = null;
                }
            }
            if (file != null) {
                final File sourceFile = file;
                if (sourceFile.exists()) {
                    ProgressDialog progressDialog = null;
                    if (context != null) {
                        try {
                            ProgressDialog progressDialog2 = new ProgressDialog(context);
                            try {
                                progressDialog2.setMessage(LocaleController.getString("Loading", C0553R.string.Loading));
                                progressDialog2.setCanceledOnTouchOutside(false);
                                progressDialog2.setCancelable(false);
                                progressDialog2.setProgressStyle(1);
                                progressDialog2.setMax(100);
                                progressDialog2.show();
                                progressDialog = progressDialog2;
                            } catch (Exception e2) {
                                e = e2;
                                progressDialog = progressDialog2;
                                FileLog.m611e("tmessages", e);
                                finalProgress = progressDialog;
                                new Thread(new Runnable() {

                                    class C04152 implements Runnable {
                                        C04152() {
                                        }

                                        public void run() {
                                            try {
                                                finalProgress.dismiss();
                                            } catch (Throwable e) {
                                                FileLog.m611e("tmessages", e);
                                            }
                                        }
                                    }

                                    public void run() {
                                        File destFile = null;
                                        try {
                                            if (type == 0) {
                                                destFile = AndroidUtilities.generatePicturePath();
                                            } else if (type == 1) {
                                                destFile = AndroidUtilities.generateVideoPath();
                                            } else if (type == 2) {
                                                f = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                                                f.mkdir();
                                                destFile = new File(f, name);
                                            } else if (type == 3) {
                                                f = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
                                                f.mkdirs();
                                                destFile = new File(f, name);
                                            }
                                            if (!destFile.exists()) {
                                                destFile.createNewFile();
                                            }
                                            FileChannel source = null;
                                            FileChannel destination = null;
                                            boolean result = true;
                                            long lastProgress = System.currentTimeMillis() - 500;
                                            try {
                                                source = new FileInputStream(sourceFile).getChannel();
                                                destination = new FileOutputStream(destFile).getChannel();
                                                long size = source.size();
                                                for (long a = 0; a < size; a += PlaybackStateCompat.ACTION_SKIP_TO_QUEUE_ITEM) {
                                                    destination.transferFrom(source, a, Math.min(PlaybackStateCompat.ACTION_SKIP_TO_QUEUE_ITEM, size - a));
                                                    if (finalProgress != null && lastProgress <= System.currentTimeMillis() - 500) {
                                                        lastProgress = System.currentTimeMillis();
                                                        final int progress = (int) ((((float) a) / ((float) size)) * 100.0f);
                                                        AndroidUtilities.runOnUIThread(new Runnable() {
                                                            public void run() {
                                                                try {
                                                                    finalProgress.setProgress(progress);
                                                                } catch (Throwable e) {
                                                                    FileLog.m611e("tmessages", e);
                                                                }
                                                            }
                                                        });
                                                    }
                                                }
                                                if (source != null) {
                                                    source.close();
                                                }
                                                if (destination != null) {
                                                    destination.close();
                                                }
                                            } catch (Throwable e) {
                                                FileLog.m611e("tmessages", e);
                                                result = false;
                                                if (source != null) {
                                                    source.close();
                                                }
                                                if (destination != null) {
                                                    destination.close();
                                                }
                                            } catch (Throwable th) {
                                                if (source != null) {
                                                    source.close();
                                                }
                                                if (destination != null) {
                                                    destination.close();
                                                }
                                            }
                                            if (result && (type == 0 || type == 1 || type == 3)) {
                                                AndroidUtilities.addMediaToGallery(Uri.fromFile(destFile));
                                            }
                                        } catch (Throwable e2) {
                                            FileLog.m611e("tmessages", e2);
                                        }
                                        if (finalProgress != null) {
                                            AndroidUtilities.runOnUIThread(new C04152());
                                        }
                                    }
                                }).start();
                            }
                        } catch (Exception e3) {
                            e = e3;
                            FileLog.m611e("tmessages", e);
                            finalProgress = progressDialog;
                            new Thread(/* anonymous class already generated */).start();
                        }
                    }
                    finalProgress = progressDialog;
                    new Thread(/* anonymous class already generated */).start();
                }
            }
        }
    }

    public GifDrawable getGifDrawable(ChatMediaCell cell, boolean create) {
        GifDrawable gifDrawable = null;
        if (cell == null) {
            return gifDrawable;
        }
        MessageObject messageObject = cell.getMessageObject();
        if (messageObject == null) {
            return gifDrawable;
        }
        if (this.currentGifDrawable != null && this.currentGifMessageObject != null && messageObject.getId() == this.currentGifMessageObject.getId()) {
            this.currentMediaCell = cell;
            this.currentGifDrawable.parentView = new WeakReference(cell);
            return this.currentGifDrawable;
        } else if (!create) {
            return gifDrawable;
        } else {
            if (this.currentMediaCell != null) {
                if (this.currentGifDrawable != null) {
                    this.currentGifDrawable.stop();
                    this.currentGifDrawable.recycle();
                }
                this.currentMediaCell.clearGifImage();
            }
            this.currentGifMessageObject = cell.getMessageObject();
            this.currentMediaCell = cell;
            File cacheFile = null;
            if (!(this.currentGifMessageObject.messageOwner.attachPath == null || this.currentGifMessageObject.messageOwner.attachPath.length() == 0)) {
                File f = new File(this.currentGifMessageObject.messageOwner.attachPath);
                if (f.length() > 0) {
                    cacheFile = f;
                }
            }
            if (cacheFile == null) {
                cacheFile = FileLoader.getPathToMessage(messageObject.messageOwner);
            }
            try {
                this.currentGifDrawable = new GifDrawable(cacheFile);
                this.currentGifDrawable.parentView = new WeakReference(cell);
                return this.currentGifDrawable;
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
                return gifDrawable;
            }
        }
    }

    public void clearGifDrawable(ChatMediaCell cell) {
        if (cell != null) {
            MessageObject messageObject = cell.getMessageObject();
            if (messageObject != null && this.currentGifMessageObject != null && messageObject.getId() == this.currentGifMessageObject.getId()) {
                if (this.currentGifDrawable != null) {
                    this.currentGifDrawable.stop();
                    this.currentGifDrawable.recycle();
                    this.currentGifDrawable = null;
                }
                this.currentMediaCell = null;
                this.currentGifMessageObject = null;
            }
        }
    }

    public static boolean isWebp(Uri uri) {
        Throwable e;
        Throwable th;
        boolean z = false;
        ParcelFileDescriptor parcelFD = null;
        FileInputStream input = null;
        try {
            parcelFD = ApplicationLoader.applicationContext.getContentResolver().openFileDescriptor(uri, "r");
            FileInputStream input2 = new FileInputStream(parcelFD.getFileDescriptor());
            try {
                if (input2.getChannel().size() > 12) {
                    byte[] header = new byte[12];
                    input2.read(header, 0, 12);
                    String str = new String(header);
                    if (str != null) {
                        str = str.toLowerCase();
                        if (str.startsWith("riff") && str.endsWith("webp")) {
                            z = true;
                            if (parcelFD != null) {
                                try {
                                    parcelFD.close();
                                } catch (Throwable e2) {
                                    FileLog.m611e("tmessages", e2);
                                }
                            }
                            if (input2 != null) {
                                try {
                                    input2.close();
                                } catch (Throwable e22) {
                                    FileLog.m611e("tmessages", e22);
                                }
                            }
                            input = input2;
                            return z;
                        }
                    }
                }
                if (parcelFD != null) {
                    try {
                        parcelFD.close();
                    } catch (Throwable e222) {
                        FileLog.m611e("tmessages", e222);
                    }
                }
                if (input2 != null) {
                    try {
                        input2.close();
                    } catch (Throwable e2222) {
                        FileLog.m611e("tmessages", e2222);
                        input = input2;
                    }
                }
                input = input2;
            } catch (Exception e3) {
                e = e3;
                input = input2;
                try {
                    FileLog.m611e("tmessages", e);
                    if (parcelFD != null) {
                        try {
                            parcelFD.close();
                        } catch (Throwable e22222) {
                            FileLog.m611e("tmessages", e22222);
                        }
                    }
                    if (input != null) {
                        try {
                            input.close();
                        } catch (Throwable e222222) {
                            FileLog.m611e("tmessages", e222222);
                        }
                    }
                    return z;
                } catch (Throwable th2) {
                    th = th2;
                    if (parcelFD != null) {
                        try {
                            parcelFD.close();
                        } catch (Throwable e2222222) {
                            FileLog.m611e("tmessages", e2222222);
                        }
                    }
                    if (input != null) {
                        try {
                            input.close();
                        } catch (Throwable e22222222) {
                            FileLog.m611e("tmessages", e22222222);
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                input = input2;
                if (parcelFD != null) {
                    parcelFD.close();
                }
                if (input != null) {
                    input.close();
                }
                throw th;
            }
        } catch (Exception e4) {
            e = e4;
            FileLog.m611e("tmessages", e);
            if (parcelFD != null) {
                parcelFD.close();
            }
            if (input != null) {
                input.close();
            }
            return z;
        }
        return z;
    }

    public static boolean isGif(Uri uri) {
        Throwable e;
        Throwable th;
        boolean z = false;
        ParcelFileDescriptor parcelFD = null;
        FileInputStream input = null;
        try {
            parcelFD = ApplicationLoader.applicationContext.getContentResolver().openFileDescriptor(uri, "r");
            FileInputStream input2 = new FileInputStream(parcelFD.getFileDescriptor());
            try {
                if (input2.getChannel().size() > 3) {
                    byte[] header = new byte[3];
                    input2.read(header, 0, 3);
                    String str = new String(header);
                    if (str != null && str.equalsIgnoreCase("gif")) {
                        z = true;
                        if (parcelFD != null) {
                            try {
                                parcelFD.close();
                            } catch (Throwable e2) {
                                FileLog.m611e("tmessages", e2);
                            }
                        }
                        if (input2 != null) {
                            try {
                                input2.close();
                            } catch (Throwable e22) {
                                FileLog.m611e("tmessages", e22);
                            }
                        }
                        input = input2;
                        return z;
                    }
                }
                if (parcelFD != null) {
                    try {
                        parcelFD.close();
                    } catch (Throwable e222) {
                        FileLog.m611e("tmessages", e222);
                    }
                }
                if (input2 != null) {
                    try {
                        input2.close();
                    } catch (Throwable e2222) {
                        FileLog.m611e("tmessages", e2222);
                        input = input2;
                    }
                }
                input = input2;
            } catch (Exception e3) {
                e = e3;
                input = input2;
                try {
                    FileLog.m611e("tmessages", e);
                    if (parcelFD != null) {
                        try {
                            parcelFD.close();
                        } catch (Throwable e22222) {
                            FileLog.m611e("tmessages", e22222);
                        }
                    }
                    if (input != null) {
                        try {
                            input.close();
                        } catch (Throwable e222222) {
                            FileLog.m611e("tmessages", e222222);
                        }
                    }
                    return z;
                } catch (Throwable th2) {
                    th = th2;
                    if (parcelFD != null) {
                        try {
                            parcelFD.close();
                        } catch (Throwable e2222222) {
                            FileLog.m611e("tmessages", e2222222);
                        }
                    }
                    if (input != null) {
                        try {
                            input.close();
                        } catch (Throwable e22222222) {
                            FileLog.m611e("tmessages", e22222222);
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                input = input2;
                if (parcelFD != null) {
                    parcelFD.close();
                }
                if (input != null) {
                    input.close();
                }
                throw th;
            }
        } catch (Exception e4) {
            e = e4;
            FileLog.m611e("tmessages", e);
            if (parcelFD != null) {
                parcelFD.close();
            }
            if (input != null) {
                input.close();
            }
            return z;
        }
        return z;
    }

    public static String copyDocumentToCache(Uri uri, String ext) {
        Throwable e;
        Throwable th;
        ParcelFileDescriptor parcelFD = null;
        FileInputStream input = null;
        FileOutputStream output = null;
        try {
            int id = UserConfig.lastLocalId;
            UserConfig.lastLocalId--;
            parcelFD = ApplicationLoader.applicationContext.getContentResolver().openFileDescriptor(uri, "r");
            FileInputStream input2 = new FileInputStream(parcelFD.getFileDescriptor());
            try {
                File f = new File(FileLoader.getInstance().getDirectory(4), String.format(Locale.US, "%d.%s", new Object[]{Integer.valueOf(id), ext}));
                FileOutputStream output2 = new FileOutputStream(f);
                try {
                    input2.getChannel().transferTo(0, input2.getChannel().size(), output2.getChannel());
                    UserConfig.saveConfig(false);
                    String absolutePath = f.getAbsolutePath();
                    if (parcelFD != null) {
                        try {
                            parcelFD.close();
                        } catch (Throwable e2) {
                            FileLog.m611e("tmessages", e2);
                        }
                    }
                    if (input2 != null) {
                        try {
                            input2.close();
                        } catch (Throwable e22) {
                            FileLog.m611e("tmessages", e22);
                        }
                    }
                    if (output2 != null) {
                        try {
                            output2.close();
                        } catch (Throwable e222) {
                            FileLog.m611e("tmessages", e222);
                        }
                    }
                    output = output2;
                    input = input2;
                    return absolutePath;
                } catch (Exception e3) {
                    e = e3;
                    output = output2;
                    input = input2;
                    try {
                        FileLog.m611e("tmessages", e);
                        if (parcelFD != null) {
                            try {
                                parcelFD.close();
                            } catch (Throwable e2222) {
                                FileLog.m611e("tmessages", e2222);
                            }
                        }
                        if (input != null) {
                            try {
                                input.close();
                            } catch (Throwable e22222) {
                                FileLog.m611e("tmessages", e22222);
                            }
                        }
                        if (output != null) {
                            try {
                                output.close();
                            } catch (Throwable e222222) {
                                FileLog.m611e("tmessages", e222222);
                            }
                        }
                        return null;
                    } catch (Throwable th2) {
                        th = th2;
                        if (parcelFD != null) {
                            try {
                                parcelFD.close();
                            } catch (Throwable e2222222) {
                                FileLog.m611e("tmessages", e2222222);
                            }
                        }
                        if (input != null) {
                            try {
                                input.close();
                            } catch (Throwable e22222222) {
                                FileLog.m611e("tmessages", e22222222);
                            }
                        }
                        if (output != null) {
                            try {
                                output.close();
                            } catch (Throwable e222222222) {
                                FileLog.m611e("tmessages", e222222222);
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    output = output2;
                    input = input2;
                    if (parcelFD != null) {
                        parcelFD.close();
                    }
                    if (input != null) {
                        input.close();
                    }
                    if (output != null) {
                        output.close();
                    }
                    throw th;
                }
            } catch (Exception e4) {
                e = e4;
                input = input2;
                FileLog.m611e("tmessages", e);
                if (parcelFD != null) {
                    parcelFD.close();
                }
                if (input != null) {
                    input.close();
                }
                if (output != null) {
                    output.close();
                }
                return null;
            } catch (Throwable th4) {
                th = th4;
                input = input2;
                if (parcelFD != null) {
                    parcelFD.close();
                }
                if (input != null) {
                    input.close();
                }
                if (output != null) {
                    output.close();
                }
                throw th;
            }
        } catch (Exception e5) {
            e = e5;
            FileLog.m611e("tmessages", e);
            if (parcelFD != null) {
                parcelFD.close();
            }
            if (input != null) {
                input.close();
            }
            if (output != null) {
                output.close();
            }
            return null;
        }
    }

    public void toggleSaveToGallery() {
        boolean z;
        if (this.saveToGallery) {
            z = false;
        } else {
            z = true;
        }
        this.saveToGallery = z;
        Editor editor = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0).edit();
        editor.putBoolean("save_gallery", this.saveToGallery);
        editor.commit();
        checkSaveToGalleryFiles();
    }

    public void checkSaveToGalleryFiles() {
        try {
            File telegramPath = new File(Environment.getExternalStorageDirectory(), "Telegram");
            File imagePath = new File(telegramPath, "Telegram Images");
            imagePath.mkdir();
            File videoPath = new File(telegramPath, "Telegram Video");
            videoPath.mkdir();
            if (this.saveToGallery) {
                if (imagePath.isDirectory()) {
                    new File(imagePath, ".nomedia").delete();
                }
                if (videoPath.isDirectory()) {
                    new File(videoPath, ".nomedia").delete();
                    return;
                }
                return;
            }
            if (imagePath.isDirectory()) {
                new File(imagePath, ".nomedia").createNewFile();
            }
            if (videoPath.isDirectory()) {
                new File(videoPath, ".nomedia").createNewFile();
            }
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
        }
    }

    public boolean canSaveToGallery() {
        return this.saveToGallery;
    }

    public static void loadGalleryPhotosAlbums(final int guid) {
        new Thread(new Runnable() {
            public void run() {
                int imageIdColumn;
                int bucketIdColumn;
                int bucketNameColumn;
                int dataColumn;
                int dateColumn;
                int imageId;
                int bucketId;
                String bucketName;
                String path;
                long dateTaken;
                AlbumEntry albumEntry;
                AlbumEntry albumEntry2;
                Throwable e;
                AlbumEntry allVideosAlbum;
                PhotoEntry photoEntry;
                ArrayList<AlbumEntry> albumsSorted = new ArrayList();
                ArrayList<AlbumEntry> videoAlbumsSorted = new ArrayList();
                HashMap<Integer, AlbumEntry> albums = new HashMap();
                AlbumEntry albumEntry3 = null;
                String cameraFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/" + "Camera/";
                Integer cameraAlbumId = null;
                Integer cameraAlbumVideoId = null;
                Cursor cursor = null;
                try {
                    if (VERSION.SDK_INT < 23 || (VERSION.SDK_INT >= 23 && ApplicationLoader.applicationContext.checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE") == 0)) {
                        cursor = Media.query(ApplicationLoader.applicationContext.getContentResolver(), Media.EXTERNAL_CONTENT_URI, MediaController.projectionPhotos, null, null, "datetaken DESC");
                        if (cursor != null) {
                            imageIdColumn = cursor.getColumnIndex("_id");
                            bucketIdColumn = cursor.getColumnIndex("bucket_id");
                            bucketNameColumn = cursor.getColumnIndex("bucket_display_name");
                            dataColumn = cursor.getColumnIndex("_data");
                            dateColumn = cursor.getColumnIndex("datetaken");
                            int orientationColumn = cursor.getColumnIndex("orientation");
                            AlbumEntry allPhotosAlbum = null;
                            while (cursor.moveToNext()) {
                                try {
                                    imageId = cursor.getInt(imageIdColumn);
                                    bucketId = cursor.getInt(bucketIdColumn);
                                    bucketName = cursor.getString(bucketNameColumn);
                                    path = cursor.getString(dataColumn);
                                    dateTaken = cursor.getLong(dateColumn);
                                    int orientation = cursor.getInt(orientationColumn);
                                    if (!(path == null || path.length() == 0)) {
                                        PhotoEntry photoEntry2 = new PhotoEntry(bucketId, imageId, dateTaken, path, orientation, false);
                                        if (allPhotosAlbum == null) {
                                            albumEntry = new AlbumEntry(0, LocaleController.getString("AllPhotos", C0553R.string.AllPhotos), photoEntry2, false);
                                            albumsSorted.add(0, albumEntry);
                                        } else {
                                            albumEntry3 = allPhotosAlbum;
                                        }
                                        if (albumEntry3 != null) {
                                            albumEntry3.addPhoto(photoEntry2);
                                        }
                                        albumEntry2 = (AlbumEntry) albums.get(Integer.valueOf(bucketId));
                                        if (albumEntry2 == null) {
                                            albumEntry = new AlbumEntry(bucketId, bucketName, photoEntry2, false);
                                            albums.put(Integer.valueOf(bucketId), albumEntry);
                                            if (cameraAlbumId != null || cameraFolder == null || path == null || !path.startsWith(cameraFolder)) {
                                                albumsSorted.add(albumEntry);
                                            } else {
                                                albumsSorted.add(0, albumEntry);
                                                cameraAlbumId = Integer.valueOf(bucketId);
                                            }
                                        }
                                        albumEntry2.addPhoto(photoEntry2);
                                        allPhotosAlbum = albumEntry3;
                                    }
                                } catch (Throwable th) {
                                    Throwable th2 = th;
                                    albumEntry3 = allPhotosAlbum;
                                }
                            }
                            albumEntry3 = allPhotosAlbum;
                        }
                    }
                    if (cursor != null) {
                        try {
                            cursor.close();
                        } catch (Throwable e2) {
                            FileLog.m611e("tmessages", e2);
                        }
                    }
                } catch (Throwable th3) {
                    e2 = th3;
                }
                try {
                    if (VERSION.SDK_INT < 23 || (VERSION.SDK_INT >= 23 && ApplicationLoader.applicationContext.checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE") == 0)) {
                        albums.clear();
                        allVideosAlbum = null;
                        cursor = Media.query(ApplicationLoader.applicationContext.getContentResolver(), MediaStore.Video.Media.EXTERNAL_CONTENT_URI, MediaController.projectionVideo, null, null, "datetaken DESC");
                        if (cursor != null) {
                            imageIdColumn = cursor.getColumnIndex("_id");
                            bucketIdColumn = cursor.getColumnIndex("bucket_id");
                            bucketNameColumn = cursor.getColumnIndex("bucket_display_name");
                            dataColumn = cursor.getColumnIndex("_data");
                            dateColumn = cursor.getColumnIndex("datetaken");
                            while (cursor.moveToNext()) {
                                imageId = cursor.getInt(imageIdColumn);
                                bucketId = cursor.getInt(bucketIdColumn);
                                bucketName = cursor.getString(bucketNameColumn);
                                path = cursor.getString(dataColumn);
                                dateTaken = cursor.getLong(dateColumn);
                                if (!(path == null || path.length() == 0)) {
                                    photoEntry = new PhotoEntry(bucketId, imageId, dateTaken, path, 0, true);
                                    if (allVideosAlbum == null) {
                                        albumEntry = new AlbumEntry(0, LocaleController.getString("AllVideo", C0553R.string.AllVideo), photoEntry, true);
                                        videoAlbumsSorted.add(0, albumEntry);
                                    }
                                    if (allVideosAlbum != null) {
                                        allVideosAlbum.addPhoto(photoEntry);
                                    }
                                    albumEntry2 = (AlbumEntry) albums.get(Integer.valueOf(bucketId));
                                    if (albumEntry2 == null) {
                                        albumEntry = new AlbumEntry(bucketId, bucketName, photoEntry, true);
                                        albums.put(Integer.valueOf(bucketId), albumEntry);
                                        if (cameraAlbumVideoId == null || cameraFolder == null || path == null || !path.startsWith(cameraFolder)) {
                                            videoAlbumsSorted.add(albumEntry);
                                        } else {
                                            videoAlbumsSorted.add(0, albumEntry);
                                            cameraAlbumVideoId = Integer.valueOf(bucketId);
                                        }
                                    }
                                    albumEntry2.addPhoto(photoEntry);
                                }
                            }
                        }
                    }
                    if (cursor != null) {
                        try {
                            cursor.close();
                        } catch (Throwable e22) {
                            FileLog.m611e("tmessages", e22);
                        }
                    }
                } catch (Throwable th4) {
                    if (cursor != null) {
                        try {
                            cursor.close();
                        } catch (Throwable e222) {
                            FileLog.m611e("tmessages", e222);
                        }
                    }
                }
                final Integer cameraAlbumIdFinal = cameraAlbumId;
                final Integer cameraAlbumVideoIdFinal = cameraAlbumVideoId;
                final AlbumEntry allPhotosAlbumFinal = albumEntry3;
                final ArrayList<AlbumEntry> arrayList = albumsSorted;
                final ArrayList<AlbumEntry> arrayList2 = videoAlbumsSorted;
                AndroidUtilities.runOnUIThread(new Runnable() {
                    public void run() {
                        MediaController.allPhotosAlbumEntry = allPhotosAlbumFinal;
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.albumsDidLoaded, Integer.valueOf(guid), arrayList, cameraAlbumIdFinal, arrayList2, cameraAlbumVideoIdFinal);
                    }
                });
                try {
                    FileLog.m611e("tmessages", e222);
                    if (cursor != null) {
                        try {
                            cursor.close();
                        } catch (Throwable e2222) {
                            FileLog.m611e("tmessages", e2222);
                        }
                    }
                    albums.clear();
                    allVideosAlbum = null;
                    cursor = Media.query(ApplicationLoader.applicationContext.getContentResolver(), MediaStore.Video.Media.EXTERNAL_CONTENT_URI, MediaController.projectionVideo, null, null, "datetaken DESC");
                    if (cursor != null) {
                        imageIdColumn = cursor.getColumnIndex("_id");
                        bucketIdColumn = cursor.getColumnIndex("bucket_id");
                        bucketNameColumn = cursor.getColumnIndex("bucket_display_name");
                        dataColumn = cursor.getColumnIndex("_data");
                        dateColumn = cursor.getColumnIndex("datetaken");
                        while (cursor.moveToNext()) {
                            imageId = cursor.getInt(imageIdColumn);
                            bucketId = cursor.getInt(bucketIdColumn);
                            bucketName = cursor.getString(bucketNameColumn);
                            path = cursor.getString(dataColumn);
                            dateTaken = cursor.getLong(dateColumn);
                            photoEntry = new PhotoEntry(bucketId, imageId, dateTaken, path, 0, true);
                            if (allVideosAlbum == null) {
                                albumEntry = new AlbumEntry(0, LocaleController.getString("AllVideo", C0553R.string.AllVideo), photoEntry, true);
                                videoAlbumsSorted.add(0, albumEntry);
                            }
                            if (allVideosAlbum != null) {
                                allVideosAlbum.addPhoto(photoEntry);
                            }
                            albumEntry2 = (AlbumEntry) albums.get(Integer.valueOf(bucketId));
                            if (albumEntry2 == null) {
                                albumEntry = new AlbumEntry(bucketId, bucketName, photoEntry, true);
                                albums.put(Integer.valueOf(bucketId), albumEntry);
                                if (cameraAlbumVideoId == null) {
                                }
                                videoAlbumsSorted.add(albumEntry);
                            }
                            albumEntry2.addPhoto(photoEntry);
                        }
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                    final Integer cameraAlbumIdFinal2 = cameraAlbumId;
                    final Integer cameraAlbumVideoIdFinal2 = cameraAlbumVideoId;
                    final AlbumEntry allPhotosAlbumFinal2 = albumEntry3;
                    final ArrayList<AlbumEntry> arrayList3 = albumsSorted;
                    final ArrayList<AlbumEntry> arrayList22 = videoAlbumsSorted;
                    AndroidUtilities.runOnUIThread(/* anonymous class already generated */);
                } catch (Throwable th5) {
                    th2 = th5;
                    if (cursor != null) {
                        try {
                            cursor.close();
                        } catch (Throwable e22222) {
                            FileLog.m611e("tmessages", e22222);
                        }
                    }
                    throw th2;
                }
            }
        }).start();
    }

    public void scheduleVideoConvert(MessageObject messageObject) {
        this.videoConvertQueue.add(messageObject);
        if (this.videoConvertQueue.size() == 1) {
            startVideoConvertFromQueue();
        }
    }

    public void cancelVideoConvert(MessageObject messageObject) {
        if (messageObject == null) {
            synchronized (this.videoConvertSync) {
                this.cancelCurrentVideoConversion = true;
            }
        } else if (!this.videoConvertQueue.isEmpty()) {
            if (this.videoConvertQueue.get(0) == messageObject) {
                synchronized (this.videoConvertSync) {
                    this.cancelCurrentVideoConversion = true;
                }
            }
            this.videoConvertQueue.remove(messageObject);
        }
    }

    private void startVideoConvertFromQueue() {
        if (!this.videoConvertQueue.isEmpty()) {
            synchronized (this.videoConvertSync) {
                this.cancelCurrentVideoConversion = false;
            }
            MessageObject messageObject = (MessageObject) this.videoConvertQueue.get(0);
            Intent intent = new Intent(ApplicationLoader.applicationContext, VideoEncodingService.class);
            intent.putExtra("path", messageObject.messageOwner.attachPath);
            ApplicationLoader.applicationContext.startService(intent);
            VideoConvertRunnable.runConversion(messageObject);
        }
    }

    @SuppressLint({"NewApi"})
    public static MediaCodecInfo selectCodec(String mimeType) {
        int numCodecs = MediaCodecList.getCodecCount();
        MediaCodecInfo lastCodecInfo = null;
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            if (codecInfo.isEncoder()) {
                for (String type : codecInfo.getSupportedTypes()) {
                    if (type.equalsIgnoreCase(mimeType)) {
                        lastCodecInfo = codecInfo;
                        if (!lastCodecInfo.getName().equals("OMX.SEC.avc.enc")) {
                            return lastCodecInfo;
                        }
                        if (lastCodecInfo.getName().equals("OMX.SEC.AVC.Encoder")) {
                            return lastCodecInfo;
                        }
                    }
                }
                continue;
            }
        }
        return lastCodecInfo;
    }

    private static boolean isRecognizedFormat(int colorFormat) {
        switch (colorFormat) {
            case 19:
            case 20:
            case 21:
            case 39:
            case 2130706688:
                return true;
            default:
                return false;
        }
    }

    @SuppressLint({"NewApi"})
    public static int selectColorFormat(MediaCodecInfo codecInfo, String mimeType) {
        CodecCapabilities capabilities = codecInfo.getCapabilitiesForType(mimeType);
        int lastColorFormat = 0;
        for (int colorFormat : capabilities.colorFormats) {
            if (isRecognizedFormat(colorFormat)) {
                lastColorFormat = colorFormat;
                if (!codecInfo.getName().equals("OMX.SEC.AVC.Encoder") || colorFormat != 19) {
                    return colorFormat;
                }
            }
        }
        return lastColorFormat;
    }

    @TargetApi(16)
    private int selectTrack(MediaExtractor extractor, boolean audio) {
        int numTracks = extractor.getTrackCount();
        for (int i = 0; i < numTracks; i++) {
            String mime = extractor.getTrackFormat(i).getString("mime");
            if (audio) {
                if (mime.startsWith("audio/")) {
                    return i;
                }
            } else if (mime.startsWith("video/")) {
                return i;
            }
        }
        return -5;
    }

    private void didWriteData(MessageObject messageObject, File file, boolean last, boolean error) {
        final boolean firstWrite = this.videoConvertFirstWrite;
        if (firstWrite) {
            this.videoConvertFirstWrite = false;
        }
        final boolean z = error;
        final MessageObject messageObject2 = messageObject;
        final File file2 = file;
        final boolean z2 = last;
        AndroidUtilities.runOnUIThread(new Runnable() {
            public void run() {
                if (z) {
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.FilePreparingFailed, messageObject2, file2.toString());
                } else {
                    if (firstWrite) {
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.FilePreparingStarted, messageObject2, file2.toString());
                    }
                    NotificationCenter instance = NotificationCenter.getInstance();
                    int i = NotificationCenter.FileNewChunkAvailable;
                    Object[] objArr = new Object[3];
                    objArr[0] = messageObject2;
                    objArr[1] = file2.toString();
                    objArr[2] = Long.valueOf(z2 ? file2.length() : 0);
                    instance.postNotificationName(i, objArr);
                }
                if (z || z2) {
                    synchronized (MediaController.this.videoConvertSync) {
                        MediaController.this.cancelCurrentVideoConversion = false;
                    }
                    MediaController.this.videoConvertQueue.remove(messageObject2);
                    MediaController.this.startVideoConvertFromQueue();
                }
            }
        });
    }

    @TargetApi(16)
    private long readAndWriteTrack(MessageObject messageObject, MediaExtractor extractor, MP4Builder mediaMuxer, BufferInfo info, long start, long end, File file, boolean isAudio) throws Exception {
        int trackIndex = selectTrack(extractor, isAudio);
        if (trackIndex < 0) {
            return -1;
        }
        extractor.selectTrack(trackIndex);
        MediaFormat trackFormat = extractor.getTrackFormat(trackIndex);
        int muxerTrackIndex = mediaMuxer.addTrack(trackFormat, isAudio);
        int maxBufferSize = trackFormat.getInteger("max-input-size");
        boolean inputDone = false;
        if (start > 0) {
            extractor.seekTo(start, 0);
        } else {
            extractor.seekTo(0, 0);
        }
        ByteBuffer buffer = ByteBuffer.allocateDirect(maxBufferSize);
        long startTime = -1;
        checkConversionCanceled();
        while (!inputDone) {
            checkConversionCanceled();
            boolean eof = false;
            int index = extractor.getSampleTrackIndex();
            if (index == trackIndex) {
                info.size = extractor.readSampleData(buffer, 0);
                if (info.size < 0) {
                    info.size = 0;
                    eof = true;
                } else {
                    info.presentationTimeUs = extractor.getSampleTime();
                    if (start > 0 && startTime == -1) {
                        startTime = info.presentationTimeUs;
                    }
                    if (end < 0 || info.presentationTimeUs < end) {
                        info.offset = 0;
                        info.flags = extractor.getSampleFlags();
                        if (mediaMuxer.writeSampleData(muxerTrackIndex, buffer, info, isAudio)) {
                            didWriteData(messageObject, file, false, false);
                        }
                        extractor.advance();
                    } else {
                        eof = true;
                    }
                }
            } else if (index == -1) {
                eof = true;
            }
            if (eof) {
                inputDone = true;
            }
        }
        extractor.unselectTrack(trackIndex);
        return startTime;
    }

    private void checkConversionCanceled() throws Exception {
        synchronized (this.videoConvertSync) {
            boolean cancelConversion = this.cancelCurrentVideoConversion;
        }
        if (cancelConversion) {
            throw new RuntimeException("canceled conversion");
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    @android.annotation.TargetApi(16)
    private boolean convertVideo(org.telegram.messenger.MessageObject r91) {
        /*
        r90 = this;
        r0 = r91;
        r6 = r0.videoEditedInfo;
        r0 = r6.originalPath;
        r84 = r0;
        r0 = r91;
        r6 = r0.videoEditedInfo;
        r0 = r6.startTime;
        r76 = r0;
        r0 = r91;
        r6 = r0.videoEditedInfo;
        r0 = r6.endTime;
        r18 = r0;
        r0 = r91;
        r6 = r0.videoEditedInfo;
        r0 = r6.resultWidth;
        r72 = r0;
        r0 = r91;
        r6 = r0.videoEditedInfo;
        r0 = r6.resultHeight;
        r70 = r0;
        r0 = r91;
        r6 = r0.videoEditedInfo;
        r0 = r6.rotationValue;
        r74 = r0;
        r0 = r91;
        r6 = r0.videoEditedInfo;
        r0 = r6.originalWidth;
        r61 = r0;
        r0 = r91;
        r6 = r0.videoEditedInfo;
        r0 = r6.originalHeight;
        r60 = r0;
        r0 = r91;
        r6 = r0.videoEditedInfo;
        r0 = r6.bitrate;
        r24 = r0;
        r73 = 0;
        r20 = new java.io.File;
        r0 = r91;
        r6 = r0.messageOwner;
        r6 = r6.attachPath;
        r0 = r20;
        r0.<init>(r6);
        r6 = android.os.Build.VERSION.SDK_INT;
        r10 = 18;
        if (r6 >= r10) goto L_0x00c5;
    L_0x005d:
        r0 = r70;
        r1 = r72;
        if (r0 <= r1) goto L_0x00c5;
    L_0x0063:
        r0 = r72;
        r1 = r61;
        if (r0 == r1) goto L_0x00c5;
    L_0x0069:
        r0 = r70;
        r1 = r60;
        if (r0 == r1) goto L_0x00c5;
    L_0x006f:
        r79 = r70;
        r70 = r72;
        r72 = r79;
        r74 = 90;
        r73 = 270; // 0x10e float:3.78E-43 double:1.334E-321;
    L_0x0079:
        r6 = org.telegram.messenger.ApplicationLoader.applicationContext;
        r10 = "videoconvert";
        r11 = 0;
        r68 = r6.getSharedPreferences(r10, r11);
        r6 = "isPreviousOk";
        r10 = 1;
        r0 = r68;
        r55 = r0.getBoolean(r6, r10);
        r6 = r68.edit();
        r10 = "isPreviousOk";
        r11 = 0;
        r6 = r6.putBoolean(r10, r11);
        r6.commit();
        r51 = new java.io.File;
        r0 = r51;
        r1 = r84;
        r0.<init>(r1);
        r6 = r51.canRead();
        if (r6 == 0) goto L_0x00aa;
    L_0x00a8:
        if (r55 != 0) goto L_0x00f8;
    L_0x00aa:
        r6 = 1;
        r10 = 1;
        r0 = r90;
        r1 = r91;
        r2 = r20;
        r0.didWriteData(r1, r2, r6, r10);
        r6 = r68.edit();
        r10 = "isPreviousOk";
        r11 = 1;
        r6 = r6.putBoolean(r10, r11);
        r6.commit();
        r6 = 0;
    L_0x00c4:
        return r6;
    L_0x00c5:
        r6 = android.os.Build.VERSION.SDK_INT;
        r10 = 20;
        if (r6 <= r10) goto L_0x0079;
    L_0x00cb:
        r6 = 90;
        r0 = r74;
        if (r0 != r6) goto L_0x00dc;
    L_0x00d1:
        r79 = r70;
        r70 = r72;
        r72 = r79;
        r74 = 0;
        r73 = 270; // 0x10e float:3.78E-43 double:1.334E-321;
        goto L_0x0079;
    L_0x00dc:
        r6 = 180; // 0xb4 float:2.52E-43 double:8.9E-322;
        r0 = r74;
        if (r0 != r6) goto L_0x00e7;
    L_0x00e2:
        r73 = 180; // 0xb4 float:2.52E-43 double:8.9E-322;
        r74 = 0;
        goto L_0x0079;
    L_0x00e7:
        r6 = 270; // 0x10e float:3.78E-43 double:1.334E-321;
        r0 = r74;
        if (r0 != r6) goto L_0x0079;
    L_0x00ed:
        r79 = r70;
        r70 = r72;
        r72 = r79;
        r74 = 0;
        r73 = 90;
        goto L_0x0079;
    L_0x00f8:
        r6 = 1;
        r0 = r90;
        r0.videoConvertFirstWrite = r6;
        r43 = 0;
        r86 = r76;
        r80 = java.lang.System.currentTimeMillis();
        if (r72 == 0) goto L_0x087e;
    L_0x0107:
        if (r70 == 0) goto L_0x087e;
    L_0x0109:
        r57 = 0;
        r45 = 0;
        r48 = new android.media.MediaCodec$BufferInfo;	 Catch:{ Exception -> 0x0832, all -> 0x089a }
        r48.<init>();	 Catch:{ Exception -> 0x0832, all -> 0x089a }
        r58 = new org.telegram.messenger.video.Mp4Movie;	 Catch:{ Exception -> 0x0832, all -> 0x089a }
        r58.<init>();	 Catch:{ Exception -> 0x0832, all -> 0x089a }
        r0 = r58;
        r1 = r20;
        r0.setCacheFile(r1);	 Catch:{ Exception -> 0x0832, all -> 0x089a }
        r0 = r58;
        r1 = r74;
        r0.setRotation(r1);	 Catch:{ Exception -> 0x0832, all -> 0x089a }
        r0 = r58;
        r1 = r72;
        r2 = r70;
        r0.setSize(r1, r2);	 Catch:{ Exception -> 0x0832, all -> 0x089a }
        r6 = new org.telegram.messenger.video.MP4Builder;	 Catch:{ Exception -> 0x0832, all -> 0x089a }
        r6.<init>();	 Catch:{ Exception -> 0x0832, all -> 0x089a }
        r0 = r58;
        r57 = r6.createMovie(r0);	 Catch:{ Exception -> 0x0832, all -> 0x089a }
        r46 = new android.media.MediaExtractor;	 Catch:{ Exception -> 0x0832, all -> 0x089a }
        r46.<init>();	 Catch:{ Exception -> 0x0832, all -> 0x089a }
        r6 = r51.toString();	 Catch:{ Exception -> 0x08a7, all -> 0x04aa }
        r0 = r46;
        r0.setDataSource(r6);	 Catch:{ Exception -> 0x08a7, all -> 0x04aa }
        r90.checkConversionCanceled();	 Catch:{ Exception -> 0x08a7, all -> 0x04aa }
        r0 = r72;
        r1 = r61;
        if (r0 != r1) goto L_0x0156;
    L_0x0150:
        r0 = r70;
        r1 = r60;
        if (r0 == r1) goto L_0x080c;
    L_0x0156:
        r6 = 0;
        r0 = r90;
        r1 = r46;
        r83 = r0.selectTrack(r1, r6);	 Catch:{ Exception -> 0x08a7, all -> 0x04aa }
        if (r83 < 0) goto L_0x08b6;
    L_0x0161:
        r4 = 0;
        r37 = 0;
        r53 = 0;
        r64 = 0;
        r88 = -1;
        r62 = 0;
        r50 = 0;
        r30 = 0;
        r78 = 0;
        r85 = -5;
        r69 = 0;
        r6 = android.os.Build.MANUFACTURER;	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r56 = r6.toLowerCase();	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r6 = android.os.Build.VERSION.SDK_INT;	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r10 = 18;
        if (r6 >= r10) goto L_0x0454;
    L_0x0182:
        r6 = "video/avc";
        r26 = selectCodec(r6);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r6 = "video/avc";
        r0 = r26;
        r28 = selectColorFormat(r0, r6);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        if (r28 != 0) goto L_0x0225;
    L_0x0192:
        r6 = new java.lang.RuntimeException;	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r10 = "no supported color format";
        r6.<init>(r10);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        throw r6;	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
    L_0x019a:
        r35 = move-exception;
    L_0x019b:
        r6 = "tmessages";
        r0 = r35;
        org.telegram.messenger.FileLog.m611e(r6, r0);	 Catch:{ Exception -> 0x08a7, all -> 0x04aa }
        r43 = 1;
        r16 = r86;
    L_0x01a6:
        r0 = r46;
        r1 = r83;
        r0.unselectTrack(r1);	 Catch:{ Exception -> 0x08ad, all -> 0x089f }
        if (r64 == 0) goto L_0x01b2;
    L_0x01af:
        r64.release();	 Catch:{ Exception -> 0x08ad, all -> 0x089f }
    L_0x01b2:
        if (r53 == 0) goto L_0x01b7;
    L_0x01b4:
        r53.release();	 Catch:{ Exception -> 0x08ad, all -> 0x089f }
    L_0x01b7:
        if (r4 == 0) goto L_0x01bf;
    L_0x01b9:
        r4.stop();	 Catch:{ Exception -> 0x08ad, all -> 0x089f }
        r4.release();	 Catch:{ Exception -> 0x08ad, all -> 0x089f }
    L_0x01bf:
        if (r37 == 0) goto L_0x01c7;
    L_0x01c1:
        r37.stop();	 Catch:{ Exception -> 0x08ad, all -> 0x089f }
        r37.release();	 Catch:{ Exception -> 0x08ad, all -> 0x089f }
    L_0x01c7:
        r90.checkConversionCanceled();	 Catch:{ Exception -> 0x08ad, all -> 0x089f }
    L_0x01ca:
        if (r43 != 0) goto L_0x01db;
    L_0x01cc:
        r21 = 1;
        r11 = r90;
        r12 = r91;
        r13 = r46;
        r14 = r57;
        r15 = r48;
        r11.readAndWriteTrack(r12, r13, r14, r15, r16, r18, r20, r21);	 Catch:{ Exception -> 0x08ad, all -> 0x089f }
    L_0x01db:
        if (r46 == 0) goto L_0x01e0;
    L_0x01dd:
        r46.release();
    L_0x01e0:
        if (r57 == 0) goto L_0x01e8;
    L_0x01e2:
        r6 = 0;
        r0 = r57;
        r0.finishMovie(r6);	 Catch:{ Exception -> 0x0828 }
    L_0x01e8:
        r6 = "tmessages";
        r10 = new java.lang.StringBuilder;
        r10.<init>();
        r11 = "time = ";
        r10 = r10.append(r11);
        r12 = java.lang.System.currentTimeMillis();
        r12 = r12 - r80;
        r10 = r10.append(r12);
        r10 = r10.toString();
        org.telegram.messenger.FileLog.m609e(r6, r10);
        r45 = r46;
    L_0x0208:
        r6 = r68.edit();
        r10 = "isPreviousOk";
        r11 = 1;
        r6 = r6.putBoolean(r10, r11);
        r6.commit();
        r6 = 1;
        r0 = r90;
        r1 = r91;
        r2 = r20;
        r3 = r43;
        r0.didWriteData(r1, r2, r6, r3);
        r6 = 1;
        goto L_0x00c4;
    L_0x0225:
        r27 = r26.getName();	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r6 = "OMX.qcom.";
        r0 = r27;
        r6 = r0.contains(r6);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        if (r6 == 0) goto L_0x041a;
    L_0x0233:
        r69 = 1;
        r6 = android.os.Build.VERSION.SDK_INT;	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r10 = 16;
        if (r6 != r10) goto L_0x0251;
    L_0x023b:
        r6 = "lge";
        r0 = r56;
        r6 = r0.equals(r6);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        if (r6 != 0) goto L_0x024f;
    L_0x0245:
        r6 = "nokia";
        r0 = r56;
        r6 = r0.equals(r6);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        if (r6 == 0) goto L_0x0251;
    L_0x024f:
        r78 = 1;
    L_0x0251:
        r6 = "tmessages";
        r10 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r10.<init>();	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r11 = "codec = ";
        r10 = r10.append(r11);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r11 = r26.getName();	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r10 = r10.append(r11);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r11 = " manufacturer = ";
        r10 = r10.append(r11);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r0 = r56;
        r10 = r10.append(r0);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r11 = "device = ";
        r10 = r10.append(r11);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r11 = android.os.Build.MODEL;	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r10 = r10.append(r11);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r10 = r10.toString();	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        org.telegram.messenger.FileLog.m609e(r6, r10);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
    L_0x0285:
        r6 = "tmessages";
        r10 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r10.<init>();	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r11 = "colorFormat = ";
        r10 = r10.append(r11);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r0 = r28;
        r10 = r10.append(r0);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r10 = r10.toString();	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        org.telegram.messenger.FileLog.m609e(r6, r10);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r71 = r70;
        r66 = 0;
        r6 = r72 * r70;
        r6 = r6 * 3;
        r25 = r6 / 2;
        if (r69 != 0) goto L_0x0459;
    L_0x02ab:
        r6 = r70 % 16;
        if (r6 == 0) goto L_0x02bf;
    L_0x02af:
        r6 = r70 % 16;
        r6 = 16 - r6;
        r71 = r71 + r6;
        r6 = r71 - r70;
        r66 = r72 * r6;
        r6 = r66 * 5;
        r6 = r6 / 4;
        r25 = r25 + r6;
    L_0x02bf:
        r0 = r46;
        r1 = r83;
        r0.selectTrack(r1);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r10 = 0;
        r6 = (r76 > r10 ? 1 : (r76 == r10 ? 0 : -1));
        if (r6 <= 0) goto L_0x04a0;
    L_0x02cc:
        r6 = 0;
        r0 = r46;
        r1 = r76;
        r0.seekTo(r1, r6);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
    L_0x02d4:
        r0 = r46;
        r1 = r83;
        r52 = r0.getTrackFormat(r1);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r6 = "video/avc";
        r0 = r72;
        r1 = r70;
        r63 = android.media.MediaFormat.createVideoFormat(r6, r0, r1);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r6 = "color-format";
        r0 = r63;
        r1 = r28;
        r0.setInteger(r6, r1);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r6 = "bitrate";
        if (r24 == 0) goto L_0x04db;
    L_0x02f3:
        r0 = r63;
        r1 = r24;
        r0.setInteger(r6, r1);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r6 = "frame-rate";
        r10 = 25;
        r0 = r63;
        r0.setInteger(r6, r10);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r6 = "i-frame-interval";
        r10 = 10;
        r0 = r63;
        r0.setInteger(r6, r10);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r6 = android.os.Build.VERSION.SDK_INT;	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r10 = 18;
        if (r6 >= r10) goto L_0x0324;
    L_0x0312:
        r6 = "stride";
        r10 = r72 + 32;
        r0 = r63;
        r0.setInteger(r6, r10);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r6 = "slice-height";
        r0 = r63;
        r1 = r70;
        r0.setInteger(r6, r1);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
    L_0x0324:
        r6 = "video/avc";
        r37 = android.media.MediaCodec.createEncoderByType(r6);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r6 = 0;
        r10 = 0;
        r11 = 1;
        r0 = r37;
        r1 = r63;
        r0.configure(r1, r6, r10, r11);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r6 = android.os.Build.VERSION.SDK_INT;	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r10 = 18;
        if (r6 < r10) goto L_0x034a;
    L_0x033a:
        r54 = new org.telegram.messenger.video.InputSurface;	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r6 = r37.createInputSurface();	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r0 = r54;
        r0.<init>(r6);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r54.makeCurrent();	 Catch:{ Exception -> 0x08b1, all -> 0x04aa }
        r53 = r54;
    L_0x034a:
        r37.start();	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r6 = "mime";
        r0 = r52;
        r6 = r0.getString(r6);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r4 = android.media.MediaCodec.createDecoderByType(r6);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r6 = android.os.Build.VERSION.SDK_INT;	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r10 = 18;
        if (r6 < r10) goto L_0x04e0;
    L_0x035f:
        r65 = new org.telegram.messenger.video.OutputSurface;	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r65.<init>();	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r64 = r65;
    L_0x0366:
        r6 = r64.getSurface();	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r10 = 0;
        r11 = 0;
        r0 = r52;
        r4.configure(r0, r6, r10, r11);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r4.start();	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r22 = 2500; // 0x9c4 float:3.503E-42 double:1.235E-320;
        r31 = 0;
        r40 = 0;
        r38 = 0;
        r6 = android.os.Build.VERSION.SDK_INT;	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r10 = 21;
        if (r6 >= r10) goto L_0x0394;
    L_0x0382:
        r31 = r4.getInputBuffers();	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r40 = r37.getOutputBuffers();	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r6 = android.os.Build.VERSION.SDK_INT;	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r10 = 18;
        if (r6 >= r10) goto L_0x0394;
    L_0x0390:
        r38 = r37.getInputBuffers();	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
    L_0x0394:
        r90.checkConversionCanceled();	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
    L_0x0397:
        if (r62 != 0) goto L_0x0802;
    L_0x0399:
        r90.checkConversionCanceled();	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        if (r50 != 0) goto L_0x03e5;
    L_0x039e:
        r42 = 0;
        r47 = r46.getSampleTrackIndex();	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r0 = r47;
        r1 = r83;
        if (r0 != r1) goto L_0x0505;
    L_0x03aa:
        r10 = 2500; // 0x9c4 float:3.503E-42 double:1.235E-320;
        r5 = r4.dequeueInputBuffer(r10);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        if (r5 < 0) goto L_0x03cf;
    L_0x03b2:
        r6 = android.os.Build.VERSION.SDK_INT;	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r10 = 21;
        if (r6 >= r10) goto L_0x04f1;
    L_0x03b8:
        r49 = r31[r5];	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
    L_0x03ba:
        r6 = 0;
        r0 = r46;
        r1 = r49;
        r7 = r0.readSampleData(r1, r6);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        if (r7 >= 0) goto L_0x04f7;
    L_0x03c5:
        r6 = 0;
        r7 = 0;
        r8 = 0;
        r10 = 4;
        r4.queueInputBuffer(r5, r6, r7, r8, r10);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r50 = 1;
    L_0x03cf:
        if (r42 == 0) goto L_0x03e5;
    L_0x03d1:
        r10 = 2500; // 0x9c4 float:3.503E-42 double:1.235E-320;
        r5 = r4.dequeueInputBuffer(r10);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        if (r5 < 0) goto L_0x03e5;
    L_0x03d9:
        r10 = 0;
        r11 = 0;
        r12 = 0;
        r14 = 4;
        r8 = r4;
        r9 = r5;
        r8.queueInputBuffer(r9, r10, r11, r12, r14);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r50 = 1;
    L_0x03e5:
        if (r30 != 0) goto L_0x050e;
    L_0x03e7:
        r32 = 1;
    L_0x03e9:
        r39 = 1;
    L_0x03eb:
        if (r32 != 0) goto L_0x03ef;
    L_0x03ed:
        if (r39 == 0) goto L_0x0397;
    L_0x03ef:
        r90.checkConversionCanceled();	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r10 = 2500; // 0x9c4 float:3.503E-42 double:1.235E-320;
        r0 = r37;
        r1 = r48;
        r41 = r0.dequeueOutputBuffer(r1, r10);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r6 = -1;
        r0 = r41;
        if (r0 != r6) goto L_0x0512;
    L_0x0401:
        r39 = 0;
    L_0x0403:
        r6 = -1;
        r0 = r41;
        if (r0 != r6) goto L_0x03eb;
    L_0x0408:
        if (r30 != 0) goto L_0x03eb;
    L_0x040a:
        r10 = 2500; // 0x9c4 float:3.503E-42 double:1.235E-320;
        r0 = r48;
        r33 = r4.dequeueOutputBuffer(r0, r10);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r6 = -1;
        r0 = r33;
        if (r0 != r6) goto L_0x0684;
    L_0x0417:
        r32 = 0;
        goto L_0x03eb;
    L_0x041a:
        r6 = "OMX.Intel.";
        r0 = r27;
        r6 = r0.contains(r6);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        if (r6 == 0) goto L_0x0428;
    L_0x0424:
        r69 = 2;
        goto L_0x0251;
    L_0x0428:
        r6 = "OMX.MTK.VIDEO.ENCODER.AVC";
        r0 = r27;
        r6 = r0.equals(r6);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        if (r6 == 0) goto L_0x0436;
    L_0x0432:
        r69 = 3;
        goto L_0x0251;
    L_0x0436:
        r6 = "OMX.SEC.AVC.Encoder";
        r0 = r27;
        r6 = r0.equals(r6);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        if (r6 == 0) goto L_0x0446;
    L_0x0440:
        r69 = 4;
        r78 = 1;
        goto L_0x0251;
    L_0x0446:
        r6 = "OMX.TI.DUCATI1.VIDEO.H264E";
        r0 = r27;
        r6 = r0.equals(r6);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        if (r6 == 0) goto L_0x0251;
    L_0x0450:
        r69 = 5;
        goto L_0x0251;
    L_0x0454:
        r28 = 2130708361; // 0x7f000789 float:1.701803E38 double:1.0527098025E-314;
        goto L_0x0285;
    L_0x0459:
        r6 = 1;
        r0 = r69;
        if (r0 != r6) goto L_0x047a;
    L_0x045e:
        r6 = r56.toLowerCase();	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r10 = "lge";
        r6 = r6.equals(r10);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        if (r6 != 0) goto L_0x02bf;
    L_0x046a:
        r6 = r72 * r70;
        r6 = r6 + 2047;
        r0 = r6 & -2048;
        r82 = r0;
        r6 = r72 * r70;
        r66 = r82 - r6;
        r25 = r25 + r66;
        goto L_0x02bf;
    L_0x047a:
        r6 = 5;
        r0 = r69;
        if (r0 == r6) goto L_0x02bf;
    L_0x047f:
        r6 = 3;
        r0 = r69;
        if (r0 != r6) goto L_0x02bf;
    L_0x0484:
        r6 = "baidu";
        r0 = r56;
        r6 = r0.equals(r6);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        if (r6 == 0) goto L_0x02bf;
    L_0x048e:
        r6 = r70 % 16;
        r6 = 16 - r6;
        r71 = r71 + r6;
        r6 = r71 - r70;
        r66 = r72 * r6;
        r6 = r66 * 5;
        r6 = r6 / 4;
        r25 = r25 + r6;
        goto L_0x02bf;
    L_0x04a0:
        r10 = 0;
        r6 = 0;
        r0 = r46;
        r0.seekTo(r10, r6);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        goto L_0x02d4;
    L_0x04aa:
        r6 = move-exception;
        r45 = r46;
        r16 = r86;
    L_0x04af:
        if (r45 == 0) goto L_0x04b4;
    L_0x04b1:
        r45.release();
    L_0x04b4:
        if (r57 == 0) goto L_0x04bc;
    L_0x04b6:
        r10 = 0;
        r0 = r57;
        r0.finishMovie(r10);	 Catch:{ Exception -> 0x0874 }
    L_0x04bc:
        r10 = "tmessages";
        r11 = new java.lang.StringBuilder;
        r11.<init>();
        r12 = "time = ";
        r11 = r11.append(r12);
        r12 = java.lang.System.currentTimeMillis();
        r12 = r12 - r80;
        r11 = r11.append(r12);
        r11 = r11.toString();
        org.telegram.messenger.FileLog.m609e(r10, r11);
        throw r6;
    L_0x04db:
        r24 = 921600; // 0xe1000 float:1.291437E-39 double:4.55331E-318;
        goto L_0x02f3;
    L_0x04e0:
        r65 = new org.telegram.messenger.video.OutputSurface;	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r0 = r65;
        r1 = r72;
        r2 = r70;
        r3 = r73;
        r0.<init>(r1, r2, r3);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r64 = r65;
        goto L_0x0366;
    L_0x04f1:
        r49 = r4.getInputBuffer(r5);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        goto L_0x03ba;
    L_0x04f7:
        r6 = 0;
        r8 = r46.getSampleTime();	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r10 = 0;
        r4.queueInputBuffer(r5, r6, r7, r8, r10);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r46.advance();	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        goto L_0x03cf;
    L_0x0505:
        r6 = -1;
        r0 = r47;
        if (r0 != r6) goto L_0x03cf;
    L_0x050a:
        r42 = 1;
        goto L_0x03cf;
    L_0x050e:
        r32 = 0;
        goto L_0x03e9;
    L_0x0512:
        r6 = -3;
        r0 = r41;
        if (r0 != r6) goto L_0x0523;
    L_0x0517:
        r6 = android.os.Build.VERSION.SDK_INT;	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r10 = 21;
        if (r6 >= r10) goto L_0x0403;
    L_0x051d:
        r40 = r37.getOutputBuffers();	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        goto L_0x0403;
    L_0x0523:
        r6 = -2;
        r0 = r41;
        if (r0 != r6) goto L_0x053c;
    L_0x0528:
        r59 = r37.getOutputFormat();	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r6 = -5;
        r0 = r85;
        if (r0 != r6) goto L_0x0403;
    L_0x0531:
        r6 = 0;
        r0 = r57;
        r1 = r59;
        r85 = r0.addTrack(r1, r6);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        goto L_0x0403;
    L_0x053c:
        if (r41 >= 0) goto L_0x0559;
    L_0x053e:
        r6 = new java.lang.RuntimeException;	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r10 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r10.<init>();	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r11 = "unexpected result from encoder.dequeueOutputBuffer: ";
        r10 = r10.append(r11);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r0 = r41;
        r10 = r10.append(r0);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r10 = r10.toString();	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r6.<init>(r10);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        throw r6;	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
    L_0x0559:
        r6 = android.os.Build.VERSION.SDK_INT;	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r10 = 21;
        if (r6 >= r10) goto L_0x0584;
    L_0x055f:
        r36 = r40[r41];	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
    L_0x0561:
        if (r36 != 0) goto L_0x058d;
    L_0x0563:
        r6 = new java.lang.RuntimeException;	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r10 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r10.<init>();	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r11 = "encoderOutputBuffer ";
        r10 = r10.append(r11);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r0 = r41;
        r10 = r10.append(r0);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r11 = " was null";
        r10 = r10.append(r11);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r10 = r10.toString();	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r6.<init>(r10);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        throw r6;	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
    L_0x0584:
        r0 = r37;
        r1 = r41;
        r36 = r0.getOutputBuffer(r1);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        goto L_0x0561;
    L_0x058d:
        r0 = r48;
        r6 = r0.size;	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r10 = 1;
        if (r6 <= r10) goto L_0x05b6;
    L_0x0594:
        r0 = r48;
        r6 = r0.flags;	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r6 = r6 & 2;
        if (r6 != 0) goto L_0x05ca;
    L_0x059c:
        r6 = 0;
        r0 = r57;
        r1 = r85;
        r2 = r36;
        r3 = r48;
        r6 = r0.writeSampleData(r1, r2, r3, r6);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        if (r6 == 0) goto L_0x05b6;
    L_0x05ab:
        r6 = 0;
        r10 = 0;
        r0 = r90;
        r1 = r91;
        r2 = r20;
        r0.didWriteData(r1, r2, r6, r10);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
    L_0x05b6:
        r0 = r48;
        r6 = r0.flags;	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r6 = r6 & 4;
        if (r6 == 0) goto L_0x0680;
    L_0x05be:
        r62 = 1;
    L_0x05c0:
        r6 = 0;
        r0 = r37;
        r1 = r41;
        r0.releaseOutputBuffer(r1, r6);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        goto L_0x0403;
    L_0x05ca:
        r6 = -5;
        r0 = r85;
        if (r0 != r6) goto L_0x05b6;
    L_0x05cf:
        r0 = r48;
        r6 = r0.size;	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r0 = new byte[r6];	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r29 = r0;
        r0 = r48;
        r6 = r0.offset;	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r0 = r48;
        r10 = r0.size;	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r6 = r6 + r10;
        r0 = r36;
        r0.limit(r6);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r0 = r48;
        r6 = r0.offset;	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r0 = r36;
        r0.position(r6);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r0 = r36;
        r1 = r29;
        r0.get(r1);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r75 = 0;
        r67 = 0;
        r0 = r48;
        r6 = r0.size;	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r23 = r6 + -1;
    L_0x05ff:
        if (r23 < 0) goto L_0x0652;
    L_0x0601:
        r6 = 3;
        r0 = r23;
        if (r0 <= r6) goto L_0x0652;
    L_0x0606:
        r6 = r29[r23];	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r10 = 1;
        if (r6 != r10) goto L_0x067d;
    L_0x060b:
        r6 = r23 + -1;
        r6 = r29[r6];	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        if (r6 != 0) goto L_0x067d;
    L_0x0611:
        r6 = r23 + -2;
        r6 = r29[r6];	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        if (r6 != 0) goto L_0x067d;
    L_0x0617:
        r6 = r23 + -3;
        r6 = r29[r6];	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        if (r6 != 0) goto L_0x067d;
    L_0x061d:
        r6 = r23 + -3;
        r75 = java.nio.ByteBuffer.allocate(r6);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r0 = r48;
        r6 = r0.size;	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r10 = r23 + -3;
        r6 = r6 - r10;
        r67 = java.nio.ByteBuffer.allocate(r6);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r6 = 0;
        r10 = r23 + -3;
        r0 = r75;
        r1 = r29;
        r6 = r0.put(r1, r6, r10);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r10 = 0;
        r6.position(r10);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r6 = r23 + -3;
        r0 = r48;
        r10 = r0.size;	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r11 = r23 + -3;
        r10 = r10 - r11;
        r0 = r67;
        r1 = r29;
        r6 = r0.put(r1, r6, r10);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r10 = 0;
        r6.position(r10);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
    L_0x0652:
        r6 = "video/avc";
        r0 = r72;
        r1 = r70;
        r59 = android.media.MediaFormat.createVideoFormat(r6, r0, r1);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        if (r75 == 0) goto L_0x0672;
    L_0x065e:
        if (r67 == 0) goto L_0x0672;
    L_0x0660:
        r6 = "csd-0";
        r0 = r59;
        r1 = r75;
        r0.setByteBuffer(r6, r1);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r6 = "csd-1";
        r0 = r59;
        r1 = r67;
        r0.setByteBuffer(r6, r1);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
    L_0x0672:
        r6 = 0;
        r0 = r57;
        r1 = r59;
        r85 = r0.addTrack(r1, r6);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        goto L_0x05b6;
    L_0x067d:
        r23 = r23 + -1;
        goto L_0x05ff;
    L_0x0680:
        r62 = 0;
        goto L_0x05c0;
    L_0x0684:
        r6 = -3;
        r0 = r33;
        if (r0 == r6) goto L_0x03eb;
    L_0x0689:
        r6 = -2;
        r0 = r33;
        if (r0 != r6) goto L_0x06ae;
    L_0x068e:
        r59 = r4.getOutputFormat();	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r6 = "tmessages";
        r10 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r10.<init>();	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r11 = "newFormat = ";
        r10 = r10.append(r11);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r0 = r59;
        r10 = r10.append(r0);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r10 = r10.toString();	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        org.telegram.messenger.FileLog.m609e(r6, r10);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        goto L_0x03eb;
    L_0x06ae:
        if (r33 >= 0) goto L_0x06cb;
    L_0x06b0:
        r6 = new java.lang.RuntimeException;	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r10 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r10.<init>();	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r11 = "unexpected result from decoder.dequeueOutputBuffer: ";
        r10 = r10.append(r11);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r0 = r33;
        r10 = r10.append(r0);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r10 = r10.toString();	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r6.<init>(r10);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        throw r6;	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
    L_0x06cb:
        r6 = android.os.Build.VERSION.SDK_INT;	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r10 = 18;
        if (r6 < r10) goto L_0x0780;
    L_0x06d1:
        r0 = r48;
        r6 = r0.size;	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        if (r6 == 0) goto L_0x077c;
    L_0x06d7:
        r34 = 1;
    L_0x06d9:
        r10 = 0;
        r6 = (r18 > r10 ? 1 : (r18 == r10 ? 0 : -1));
        if (r6 <= 0) goto L_0x06f7;
    L_0x06df:
        r0 = r48;
        r10 = r0.presentationTimeUs;	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r6 = (r10 > r18 ? 1 : (r10 == r18 ? 0 : -1));
        if (r6 < 0) goto L_0x06f7;
    L_0x06e7:
        r50 = 1;
        r30 = 1;
        r34 = 0;
        r0 = r48;
        r6 = r0.flags;	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r6 = r6 | 4;
        r0 = r48;
        r0.flags = r6;	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
    L_0x06f7:
        r10 = 0;
        r6 = (r76 > r10 ? 1 : (r76 == r10 ? 0 : -1));
        if (r6 <= 0) goto L_0x0735;
    L_0x06fd:
        r10 = -1;
        r6 = (r88 > r10 ? 1 : (r88 == r10 ? 0 : -1));
        if (r6 != 0) goto L_0x0735;
    L_0x0703:
        r0 = r48;
        r10 = r0.presentationTimeUs;	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r6 = (r10 > r76 ? 1 : (r10 == r76 ? 0 : -1));
        if (r6 >= 0) goto L_0x0797;
    L_0x070b:
        r34 = 0;
        r6 = "tmessages";
        r10 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r10.<init>();	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r11 = "drop frame startTime = ";
        r10 = r10.append(r11);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r0 = r76;
        r10 = r10.append(r0);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r11 = " present time = ";
        r10 = r10.append(r11);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r0 = r48;
        r12 = r0.presentationTimeUs;	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r10 = r10.append(r12);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r10 = r10.toString();	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        org.telegram.messenger.FileLog.m609e(r6, r10);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
    L_0x0735:
        r0 = r33;
        r1 = r34;
        r4.releaseOutputBuffer(r0, r1);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        if (r34 == 0) goto L_0x0760;
    L_0x073e:
        r44 = 0;
        r64.awaitNewImage();	 Catch:{ Exception -> 0x079e, all -> 0x04aa }
    L_0x0743:
        if (r44 != 0) goto L_0x0760;
    L_0x0745:
        r6 = android.os.Build.VERSION.SDK_INT;	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r10 = 18;
        if (r6 < r10) goto L_0x07a9;
    L_0x074b:
        r6 = 0;
        r0 = r64;
        r0.drawImage(r6);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r0 = r48;
        r10 = r0.presentationTimeUs;	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r12 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;
        r10 = r10 * r12;
        r0 = r53;
        r0.setPresentationTime(r10);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r53.swapBuffers();	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
    L_0x0760:
        r0 = r48;
        r6 = r0.flags;	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r6 = r6 & 4;
        if (r6 == 0) goto L_0x03eb;
    L_0x0768:
        r32 = 0;
        r6 = "tmessages";
        r10 = "decoder stream end";
        org.telegram.messenger.FileLog.m609e(r6, r10);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r6 = android.os.Build.VERSION.SDK_INT;	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r10 = 18;
        if (r6 < r10) goto L_0x07e8;
    L_0x0777:
        r37.signalEndOfInputStream();	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        goto L_0x03eb;
    L_0x077c:
        r34 = 0;
        goto L_0x06d9;
    L_0x0780:
        r0 = r48;
        r6 = r0.size;	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        if (r6 != 0) goto L_0x0790;
    L_0x0786:
        r0 = r48;
        r10 = r0.presentationTimeUs;	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r12 = 0;
        r6 = (r10 > r12 ? 1 : (r10 == r12 ? 0 : -1));
        if (r6 == 0) goto L_0x0794;
    L_0x0790:
        r34 = 1;
    L_0x0792:
        goto L_0x06d9;
    L_0x0794:
        r34 = 0;
        goto L_0x0792;
    L_0x0797:
        r0 = r48;
        r0 = r0.presentationTimeUs;	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r88 = r0;
        goto L_0x0735;
    L_0x079e:
        r35 = move-exception;
        r44 = 1;
        r6 = "tmessages";
        r0 = r35;
        org.telegram.messenger.FileLog.m611e(r6, r0);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        goto L_0x0743;
    L_0x07a9:
        r10 = 2500; // 0x9c4 float:3.503E-42 double:1.235E-320;
        r0 = r37;
        r5 = r0.dequeueInputBuffer(r10);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        if (r5 < 0) goto L_0x07df;
    L_0x07b3:
        r6 = 1;
        r0 = r64;
        r0.drawImage(r6);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r8 = r64.getFrame();	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r9 = r38[r5];	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r9.clear();	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r10 = r28;
        r11 = r72;
        r12 = r70;
        r13 = r66;
        r14 = r78;
        org.telegram.messenger.Utilities.convertVideoFrame(r8, r9, r10, r11, r12, r13, r14);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r12 = 0;
        r0 = r48;
        r14 = r0.presentationTimeUs;	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r16 = 0;
        r10 = r37;
        r11 = r5;
        r13 = r25;
        r10.queueInputBuffer(r11, r12, r13, r14, r16);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        goto L_0x0760;
    L_0x07df:
        r6 = "tmessages";
        r10 = "input buffer not available";
        org.telegram.messenger.FileLog.m609e(r6, r10);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        goto L_0x0760;
    L_0x07e8:
        r10 = 2500; // 0x9c4 float:3.503E-42 double:1.235E-320;
        r0 = r37;
        r5 = r0.dequeueInputBuffer(r10);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        if (r5 < 0) goto L_0x03eb;
    L_0x07f2:
        r12 = 0;
        r13 = 1;
        r0 = r48;
        r14 = r0.presentationTimeUs;	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        r16 = 4;
        r10 = r37;
        r11 = r5;
        r10.queueInputBuffer(r11, r12, r13, r14, r16);	 Catch:{ Exception -> 0x019a, all -> 0x04aa }
        goto L_0x03eb;
    L_0x0802:
        r10 = -1;
        r6 = (r88 > r10 ? 1 : (r88 == r10 ? 0 : -1));
        if (r6 == 0) goto L_0x08ba;
    L_0x0808:
        r16 = r88;
        goto L_0x01a6;
    L_0x080c:
        r21 = 0;
        r11 = r90;
        r12 = r91;
        r13 = r46;
        r14 = r57;
        r15 = r48;
        r16 = r76;
        r88 = r11.readAndWriteTrack(r12, r13, r14, r15, r16, r18, r20, r21);	 Catch:{ Exception -> 0x08a7, all -> 0x04aa }
        r10 = -1;
        r6 = (r88 > r10 ? 1 : (r88 == r10 ? 0 : -1));
        if (r6 == 0) goto L_0x08b6;
    L_0x0824:
        r16 = r88;
        goto L_0x01ca;
    L_0x0828:
        r35 = move-exception;
        r6 = "tmessages";
        r0 = r35;
        org.telegram.messenger.FileLog.m611e(r6, r0);
        goto L_0x01e8;
    L_0x0832:
        r35 = move-exception;
        r16 = r86;
    L_0x0835:
        r43 = 1;
        r6 = "tmessages";
        r0 = r35;
        org.telegram.messenger.FileLog.m611e(r6, r0);	 Catch:{ all -> 0x08a4 }
        if (r45 == 0) goto L_0x0843;
    L_0x0840:
        r45.release();
    L_0x0843:
        if (r57 == 0) goto L_0x084b;
    L_0x0845:
        r6 = 0;
        r0 = r57;
        r0.finishMovie(r6);	 Catch:{ Exception -> 0x086b }
    L_0x084b:
        r6 = "tmessages";
        r10 = new java.lang.StringBuilder;
        r10.<init>();
        r11 = "time = ";
        r10 = r10.append(r11);
        r12 = java.lang.System.currentTimeMillis();
        r12 = r12 - r80;
        r10 = r10.append(r12);
        r10 = r10.toString();
        org.telegram.messenger.FileLog.m609e(r6, r10);
        goto L_0x0208;
    L_0x086b:
        r35 = move-exception;
        r6 = "tmessages";
        r0 = r35;
        org.telegram.messenger.FileLog.m611e(r6, r0);
        goto L_0x084b;
    L_0x0874:
        r35 = move-exception;
        r10 = "tmessages";
        r0 = r35;
        org.telegram.messenger.FileLog.m611e(r10, r0);
        goto L_0x04bc;
    L_0x087e:
        r6 = r68.edit();
        r10 = "isPreviousOk";
        r11 = 1;
        r6 = r6.putBoolean(r10, r11);
        r6.commit();
        r6 = 1;
        r10 = 1;
        r0 = r90;
        r1 = r91;
        r2 = r20;
        r0.didWriteData(r1, r2, r6, r10);
        r6 = 0;
        goto L_0x00c4;
    L_0x089a:
        r6 = move-exception;
        r16 = r86;
        goto L_0x04af;
    L_0x089f:
        r6 = move-exception;
        r45 = r46;
        goto L_0x04af;
    L_0x08a4:
        r6 = move-exception;
        goto L_0x04af;
    L_0x08a7:
        r35 = move-exception;
        r45 = r46;
        r16 = r86;
        goto L_0x0835;
    L_0x08ad:
        r35 = move-exception;
        r45 = r46;
        goto L_0x0835;
    L_0x08b1:
        r35 = move-exception;
        r53 = r54;
        goto L_0x019b;
    L_0x08b6:
        r16 = r86;
        goto L_0x01ca;
    L_0x08ba:
        r16 = r86;
        goto L_0x01a6;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MediaController.convertVideo(org.telegram.messenger.MessageObject):boolean");
    }
}
