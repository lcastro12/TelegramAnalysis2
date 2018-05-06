package org.telegram.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.SurfaceTexture;
import android.media.MediaCodecInfo;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.view.Surface;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.Container;
import com.coremedia.iso.boxes.MediaBox;
import com.coremedia.iso.boxes.MediaHeaderBox;
import com.coremedia.iso.boxes.TrackBox;
import com.coremedia.iso.boxes.TrackHeaderBox;
import com.googlecode.mp4parser.util.Matrix;
import com.googlecode.mp4parser.util.Path;
import java.io.File;
import java.util.List;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.C0553R;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.VideoSeekBarView;
import org.telegram.ui.Components.VideoSeekBarView.SeekBarDelegate;
import org.telegram.ui.Components.VideoTimelineView;
import org.telegram.ui.Components.VideoTimelineView.VideoTimelineViewDelegate;

@TargetApi(16)
public class VideoEditorActivity extends BaseFragment implements SurfaceTextureListener, NotificationCenterDelegate {
    private long audioFramesSize = 0;
    private int bitrate = 0;
    private CheckBox compressVideo = null;
    private View controlView = null;
    private boolean created = false;
    private VideoEditorActivityDelegate delegate;
    private TextView editedSizeTextView = null;
    private long endTime = 0;
    private long esimatedDuration = 0;
    private int estimatedSize = 0;
    private float lastProgress = 0.0f;
    private boolean needSeek = false;
    private int originalBitrate = 0;
    private int originalHeight = 0;
    private long originalSize = 0;
    private TextView originalSizeTextView = null;
    private int originalWidth = 0;
    private ImageView playButton = null;
    private boolean playerPrepared = false;
    private Runnable progressRunnable = new C12461();
    private int resultHeight = 0;
    private int resultWidth = 0;
    private int rotationValue = 0;
    private long startTime = 0;
    private final Object sync = new Object();
    private View textContainerView = null;
    private TextureView textureView = null;
    private Thread thread = null;
    private View videoContainerView = null;
    private float videoDuration = 0.0f;
    private long videoFramesSize = 0;
    private String videoPath = null;
    private MediaPlayer videoPlayer = null;
    private VideoSeekBarView videoSeekBarView = null;
    private VideoTimelineView videoTimelineView = null;

    class C12461 implements Runnable {

        class C12451 implements Runnable {
            C12451() {
            }

            public void run() {
                if (VideoEditorActivity.this.videoPlayer != null && VideoEditorActivity.this.videoPlayer.isPlaying()) {
                    float startTime = VideoEditorActivity.this.videoTimelineView.getLeftProgress() * VideoEditorActivity.this.videoDuration;
                    float endTime = VideoEditorActivity.this.videoTimelineView.getRightProgress() * VideoEditorActivity.this.videoDuration;
                    if (startTime == endTime) {
                        startTime = endTime - 0.01f;
                    }
                    float lrdiff = VideoEditorActivity.this.videoTimelineView.getRightProgress() - VideoEditorActivity.this.videoTimelineView.getLeftProgress();
                    float progress = VideoEditorActivity.this.videoTimelineView.getLeftProgress() + (lrdiff * ((((float) VideoEditorActivity.this.videoPlayer.getCurrentPosition()) - startTime) / (endTime - startTime)));
                    if (progress > VideoEditorActivity.this.lastProgress) {
                        VideoEditorActivity.this.videoSeekBarView.setProgress(progress);
                        VideoEditorActivity.this.lastProgress = progress;
                    }
                    if (((float) VideoEditorActivity.this.videoPlayer.getCurrentPosition()) >= endTime) {
                        try {
                            VideoEditorActivity.this.videoPlayer.pause();
                            VideoEditorActivity.this.onPlayComplete();
                        } catch (Throwable e) {
                            FileLog.m611e("tmessages", e);
                        }
                    }
                }
            }
        }

        C12461() {
        }

        public void run() {
            while (true) {
                synchronized (VideoEditorActivity.this.sync) {
                    try {
                        boolean playerCheck = VideoEditorActivity.this.videoPlayer != null && VideoEditorActivity.this.videoPlayer.isPlaying();
                    } catch (Throwable e) {
                        playerCheck = false;
                        FileLog.m611e("tmessages", e);
                    }
                }
                if (playerCheck) {
                    AndroidUtilities.runOnUIThread(new C12451());
                    try {
                        Thread.sleep(50);
                    } catch (Throwable e2) {
                        FileLog.m611e("tmessages", e2);
                    }
                } else {
                    synchronized (VideoEditorActivity.this.sync) {
                        VideoEditorActivity.this.thread = null;
                    }
                    return;
                }
            }
        }
    }

    class C12482 implements OnCompletionListener {

        class C12471 implements Runnable {
            C12471() {
            }

            public void run() {
                VideoEditorActivity.this.onPlayComplete();
            }
        }

        C12482() {
        }

        public void onCompletion(MediaPlayer mp) {
            AndroidUtilities.runOnUIThread(new C12471());
        }
    }

    class C12493 implements OnPreparedListener {
        C12493() {
        }

        public void onPrepared(MediaPlayer mp) {
            VideoEditorActivity.this.playerPrepared = true;
            if (VideoEditorActivity.this.videoTimelineView != null && VideoEditorActivity.this.videoPlayer != null) {
                VideoEditorActivity.this.videoPlayer.seekTo((int) (VideoEditorActivity.this.videoTimelineView.getLeftProgress() * VideoEditorActivity.this.videoDuration));
            }
        }
    }

    class C12505 implements OnCheckedChangeListener {
        C12505() {
        }

        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Editor editor = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0).edit();
            editor.putBoolean("compress_video", isChecked);
            editor.commit();
            VideoEditorActivity.this.updateVideoEditedInfo();
        }
    }

    class C12518 implements OnClickListener {
        C12518() {
        }

        public void onClick(View v) {
            VideoEditorActivity.this.play();
        }
    }

    class C12529 implements OnGlobalLayoutListener {
        C12529() {
        }

        public void onGlobalLayout() {
            VideoEditorActivity.this.fixLayoutInternal();
            if (VideoEditorActivity.this.fragmentView == null) {
                return;
            }
            if (VERSION.SDK_INT < 16) {
                VideoEditorActivity.this.fragmentView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            } else {
                VideoEditorActivity.this.fragmentView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        }
    }

    public interface VideoEditorActivityDelegate {
        void didFinishEditVideo(String str, long j, long j2, int i, int i2, int i3, int i4, int i5, int i6, long j3, long j4);
    }

    class C16814 extends ActionBarMenuOnItemClick {
        C16814() {
        }

        public void onItemClick(int id) {
            if (id == -1) {
                VideoEditorActivity.this.finishFragment();
            } else if (id == 1) {
                synchronized (VideoEditorActivity.this.sync) {
                    if (VideoEditorActivity.this.videoPlayer != null) {
                        try {
                            VideoEditorActivity.this.videoPlayer.stop();
                            VideoEditorActivity.this.videoPlayer.release();
                            VideoEditorActivity.this.videoPlayer = null;
                        } catch (Throwable e) {
                            FileLog.m611e("tmessages", e);
                        }
                    }
                }
                if (VideoEditorActivity.this.delegate != null) {
                    if (VideoEditorActivity.this.compressVideo.getVisibility() == 8 || (VideoEditorActivity.this.compressVideo.getVisibility() == 0 && !VideoEditorActivity.this.compressVideo.isChecked())) {
                        VideoEditorActivity.this.delegate.didFinishEditVideo(VideoEditorActivity.this.videoPath, VideoEditorActivity.this.startTime, VideoEditorActivity.this.endTime, VideoEditorActivity.this.originalWidth, VideoEditorActivity.this.originalHeight, VideoEditorActivity.this.rotationValue, VideoEditorActivity.this.originalWidth, VideoEditorActivity.this.originalHeight, VideoEditorActivity.this.originalBitrate, (long) VideoEditorActivity.this.estimatedSize, VideoEditorActivity.this.esimatedDuration);
                    } else {
                        VideoEditorActivity.this.delegate.didFinishEditVideo(VideoEditorActivity.this.videoPath, VideoEditorActivity.this.startTime, VideoEditorActivity.this.endTime, VideoEditorActivity.this.resultWidth, VideoEditorActivity.this.resultHeight, VideoEditorActivity.this.rotationValue, VideoEditorActivity.this.originalWidth, VideoEditorActivity.this.originalHeight, VideoEditorActivity.this.bitrate, (long) VideoEditorActivity.this.estimatedSize, VideoEditorActivity.this.esimatedDuration);
                    }
                }
                VideoEditorActivity.this.finishFragment();
            }
        }
    }

    class C16826 implements VideoTimelineViewDelegate {
        C16826() {
        }

        public void onLeftProgressChanged(float progress) {
            if (VideoEditorActivity.this.videoPlayer != null && VideoEditorActivity.this.playerPrepared) {
                try {
                    if (VideoEditorActivity.this.videoPlayer.isPlaying()) {
                        VideoEditorActivity.this.videoPlayer.pause();
                        VideoEditorActivity.this.playButton.setImageResource(C0553R.drawable.video_play);
                    }
                    VideoEditorActivity.this.videoPlayer.setOnSeekCompleteListener(null);
                    VideoEditorActivity.this.videoPlayer.seekTo((int) (VideoEditorActivity.this.videoDuration * progress));
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
                VideoEditorActivity.this.needSeek = true;
                VideoEditorActivity.this.videoSeekBarView.setProgress(VideoEditorActivity.this.videoTimelineView.getLeftProgress());
                VideoEditorActivity.this.updateVideoEditedInfo();
            }
        }

        public void onRifhtProgressChanged(float progress) {
            if (VideoEditorActivity.this.videoPlayer != null && VideoEditorActivity.this.playerPrepared) {
                try {
                    if (VideoEditorActivity.this.videoPlayer.isPlaying()) {
                        VideoEditorActivity.this.videoPlayer.pause();
                        VideoEditorActivity.this.playButton.setImageResource(C0553R.drawable.video_play);
                    }
                    VideoEditorActivity.this.videoPlayer.setOnSeekCompleteListener(null);
                    VideoEditorActivity.this.videoPlayer.seekTo((int) (VideoEditorActivity.this.videoDuration * progress));
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
                VideoEditorActivity.this.needSeek = true;
                VideoEditorActivity.this.videoSeekBarView.setProgress(VideoEditorActivity.this.videoTimelineView.getLeftProgress());
                VideoEditorActivity.this.updateVideoEditedInfo();
            }
        }
    }

    class C16837 implements SeekBarDelegate {
        C16837() {
        }

        public void onSeekBarDrag(float progress) {
            if (progress < VideoEditorActivity.this.videoTimelineView.getLeftProgress()) {
                progress = VideoEditorActivity.this.videoTimelineView.getLeftProgress();
                VideoEditorActivity.this.videoSeekBarView.setProgress(progress);
            } else if (progress > VideoEditorActivity.this.videoTimelineView.getRightProgress()) {
                progress = VideoEditorActivity.this.videoTimelineView.getRightProgress();
                VideoEditorActivity.this.videoSeekBarView.setProgress(progress);
            }
            if (VideoEditorActivity.this.videoPlayer != null && VideoEditorActivity.this.playerPrepared) {
                if (VideoEditorActivity.this.videoPlayer.isPlaying()) {
                    try {
                        VideoEditorActivity.this.videoPlayer.seekTo((int) (VideoEditorActivity.this.videoDuration * progress));
                        VideoEditorActivity.this.lastProgress = progress;
                        return;
                    } catch (Throwable e) {
                        FileLog.m611e("tmessages", e);
                        return;
                    }
                }
                VideoEditorActivity.this.lastProgress = progress;
                VideoEditorActivity.this.needSeek = true;
            }
        }
    }

    public VideoEditorActivity(Bundle args) {
        super(args);
        this.videoPath = args.getString("videoPath");
    }

    public boolean onFragmentCreate() {
        if (this.created) {
            return true;
        }
        if (this.videoPath == null || !processOpenVideo()) {
            return false;
        }
        this.videoPlayer = new MediaPlayer();
        this.videoPlayer.setOnCompletionListener(new C12482());
        this.videoPlayer.setOnPreparedListener(new C12493());
        try {
            this.videoPlayer.setDataSource(this.videoPath);
            this.videoPlayer.prepareAsync();
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.closeChats);
            this.created = true;
            return super.onFragmentCreate();
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
            return false;
        }
    }

    public void onFragmentDestroy() {
        if (this.videoTimelineView != null) {
            this.videoTimelineView.destroy();
        }
        if (this.videoPlayer != null) {
            try {
                this.videoPlayer.stop();
                this.videoPlayer.release();
                this.videoPlayer = null;
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
        }
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.closeChats);
        super.onFragmentDestroy();
    }

    public View createView(Context context) {
        int i;
        this.actionBar.setBackgroundColor(-13421773);
        this.actionBar.setItemsBackground(C0553R.drawable.bar_selector_white);
        this.actionBar.setBackButtonImage(C0553R.drawable.ic_ab_back);
        this.actionBar.setTitle(LocaleController.getString("EditVideo", C0553R.string.EditVideo));
        this.actionBar.setActionBarMenuOnItemClick(new C16814());
        this.actionBar.createMenu().addItemWithWidth(1, C0553R.drawable.ic_done, AndroidUtilities.dp(56.0f));
        this.fragmentView = getParentActivity().getLayoutInflater().inflate(C0553R.layout.video_editor_layout, null, false);
        this.originalSizeTextView = (TextView) this.fragmentView.findViewById(C0553R.id.original_size);
        this.editedSizeTextView = (TextView) this.fragmentView.findViewById(C0553R.id.edited_size);
        this.videoContainerView = this.fragmentView.findViewById(C0553R.id.video_container);
        this.textContainerView = this.fragmentView.findViewById(C0553R.id.info_container);
        this.controlView = this.fragmentView.findViewById(C0553R.id.control_layout);
        this.compressVideo = (CheckBox) this.fragmentView.findViewById(C0553R.id.compress_video);
        this.compressVideo.setText(LocaleController.getString("CompressVideo", C0553R.string.CompressVideo));
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0);
        CheckBox checkBox = this.compressVideo;
        if (this.originalHeight == this.resultHeight && this.originalWidth == this.resultWidth) {
            i = 8;
        } else {
            i = 0;
        }
        checkBox.setVisibility(i);
        this.compressVideo.setChecked(preferences.getBoolean("compress_video", true));
        this.compressVideo.setOnCheckedChangeListener(new C12505());
        if (VERSION.SDK_INT < 18) {
            try {
                MediaCodecInfo codecInfo = MediaController.selectCodec(MediaController.MIME_TYPE);
                if (codecInfo == null) {
                    this.compressVideo.setVisibility(8);
                } else {
                    String name = codecInfo.getName();
                    if (name.equals("OMX.google.h264.encoder") || name.equals("OMX.ST.VFM.H264Enc") || name.equals("OMX.Exynos.avc.enc") || name.equals("OMX.MARVELL.VIDEO.HW.CODA7542ENCODER") || name.equals("OMX.MARVELL.VIDEO.H264ENCODER") || name.equals("OMX.k3.video.encoder.avc") || name.equals("OMX.TI.DUCATI1.VIDEO.H264E")) {
                        this.compressVideo.setVisibility(8);
                    } else if (MediaController.selectColorFormat(codecInfo, MediaController.MIME_TYPE) == 0) {
                        this.compressVideo.setVisibility(8);
                    }
                }
            } catch (Throwable e) {
                this.compressVideo.setVisibility(8);
                FileLog.m611e("tmessages", e);
            }
        }
        ((TextView) this.fragmentView.findViewById(C0553R.id.original_title)).setText(LocaleController.getString("OriginalVideo", C0553R.string.OriginalVideo));
        ((TextView) this.fragmentView.findViewById(C0553R.id.edited_title)).setText(LocaleController.getString("EditedVideo", C0553R.string.EditedVideo));
        this.videoTimelineView = (VideoTimelineView) this.fragmentView.findViewById(C0553R.id.video_timeline_view);
        this.videoTimelineView.setVideoPath(this.videoPath);
        this.videoTimelineView.setDelegate(new C16826());
        this.videoSeekBarView = (VideoSeekBarView) this.fragmentView.findViewById(C0553R.id.video_seekbar);
        this.videoSeekBarView.delegate = new C16837();
        this.playButton = (ImageView) this.fragmentView.findViewById(C0553R.id.play_button);
        this.playButton.setOnClickListener(new C12518());
        this.textureView = (TextureView) this.fragmentView.findViewById(C0553R.id.video_view);
        this.textureView.setSurfaceTextureListener(this);
        updateVideoOriginalInfo();
        updateVideoEditedInfo();
        return this.fragmentView;
    }

    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.closeChats) {
            removeSelfFromStack();
        }
    }

    private void setPlayerSurface() {
        if (this.textureView != null && this.textureView.isAvailable() && this.videoPlayer != null) {
            try {
                this.videoPlayer.setSurface(new Surface(this.textureView.getSurfaceTexture()));
                if (this.playerPrepared) {
                    this.videoPlayer.seekTo((int) (this.videoTimelineView.getLeftProgress() * this.videoDuration));
                }
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
        }
    }

    public void onResume() {
        super.onResume();
        fixLayoutInternal();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        fixLayout();
    }

    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        setPlayerSurface();
    }

    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    }

    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (this.videoPlayer != null) {
            this.videoPlayer.setDisplay(null);
        }
        return true;
    }

    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    private void onPlayComplete() {
        if (this.playButton != null) {
            this.playButton.setImageResource(C0553R.drawable.video_play);
        }
        if (!(this.videoSeekBarView == null || this.videoTimelineView == null)) {
            this.videoSeekBarView.setProgress(this.videoTimelineView.getLeftProgress());
        }
        try {
            if (this.videoPlayer != null && this.videoTimelineView != null) {
                this.videoPlayer.seekTo((int) (this.videoTimelineView.getLeftProgress() * this.videoDuration));
            }
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
        }
    }

    private void updateVideoOriginalInfo() {
        if (this.originalSizeTextView != null) {
            int width = (this.rotationValue == 90 || this.rotationValue == 270) ? this.originalHeight : this.originalWidth;
            int height = (this.rotationValue == 90 || this.rotationValue == 270) ? this.originalWidth : this.originalHeight;
            String videoDimension = String.format("%dx%d", new Object[]{Integer.valueOf(width), Integer.valueOf(height)});
            long duration = (long) Math.ceil((double) this.videoDuration);
            int seconds = ((int) Math.ceil((double) (duration / 1000))) - (((int) ((duration / 1000) / 60)) * 60);
            String videoTimeSize = String.format("%d:%02d, %s", new Object[]{Integer.valueOf((int) ((duration / 1000) / 60)), Integer.valueOf(seconds), AndroidUtilities.formatFileSize(this.originalSize)});
            this.originalSizeTextView.setText(String.format("%s, %s", new Object[]{videoDimension, videoTimeSize}));
        }
    }

    private void updateVideoEditedInfo() {
        if (this.editedSizeTextView != null) {
            int width;
            int height;
            this.esimatedDuration = (long) Math.ceil((double) ((this.videoTimelineView.getRightProgress() - this.videoTimelineView.getLeftProgress()) * this.videoDuration));
            if (this.compressVideo.getVisibility() == 8 || (this.compressVideo.getVisibility() == 0 && !this.compressVideo.isChecked())) {
                width = (this.rotationValue == 90 || this.rotationValue == 270) ? this.originalHeight : this.originalWidth;
                height = (this.rotationValue == 90 || this.rotationValue == 270) ? this.originalWidth : this.originalHeight;
                this.estimatedSize = (int) (((float) this.originalSize) * (((float) this.esimatedDuration) / this.videoDuration));
            } else {
                width = (this.rotationValue == 90 || this.rotationValue == 270) ? this.resultHeight : this.resultWidth;
                height = (this.rotationValue == 90 || this.rotationValue == 270) ? this.resultWidth : this.resultHeight;
                this.estimatedSize = calculateEstimatedSize(((float) this.esimatedDuration) / this.videoDuration);
            }
            if (this.videoTimelineView.getLeftProgress() == 0.0f) {
                this.startTime = -1;
            } else {
                this.startTime = ((long) (this.videoTimelineView.getLeftProgress() * this.videoDuration)) * 1000;
            }
            if (this.videoTimelineView.getRightProgress() == 1.0f) {
                this.endTime = -1;
            } else {
                this.endTime = ((long) (this.videoTimelineView.getRightProgress() * this.videoDuration)) * 1000;
            }
            String videoDimension = String.format("%dx%d", new Object[]{Integer.valueOf(width), Integer.valueOf(height)});
            int seconds = ((int) Math.ceil((double) (this.esimatedDuration / 1000))) - (((int) ((this.esimatedDuration / 1000) / 60)) * 60);
            String videoTimeSize = String.format("%d:%02d, ~%s", new Object[]{Integer.valueOf((int) ((this.esimatedDuration / 1000) / 60)), Integer.valueOf(seconds), AndroidUtilities.formatFileSize((long) this.estimatedSize)});
            this.editedSizeTextView.setText(String.format("%s, %s", new Object[]{videoDimension, videoTimeSize}));
        }
    }

    private void fixVideoSize() {
        if (this.fragmentView != null && getParentActivity() != null) {
            int viewHeight;
            int width;
            int height;
            if (AndroidUtilities.isTablet()) {
                viewHeight = AndroidUtilities.dp(472.0f);
            } else {
                viewHeight = (AndroidUtilities.displaySize.y - AndroidUtilities.statusBarHeight) - ActionBar.getCurrentActionBarHeight();
            }
            if (AndroidUtilities.isTablet()) {
                int i;
                width = AndroidUtilities.dp(490.0f);
                if (this.compressVideo.getVisibility() == 0) {
                    i = 20;
                } else {
                    i = 0;
                }
                height = viewHeight - AndroidUtilities.dp((float) (i + 276));
            } else if (getParentActivity().getResources().getConfiguration().orientation == 2) {
                width = (AndroidUtilities.displaySize.x / 3) - AndroidUtilities.dp(24.0f);
                height = viewHeight - AndroidUtilities.dp(32.0f);
            } else {
                width = AndroidUtilities.displaySize.x;
                height = viewHeight - AndroidUtilities.dp((float) ((this.compressVideo.getVisibility() == 0 ? 20 : 0) + 276));
            }
            int aWidth = width;
            int aHeight = height;
            int vwidth = (this.rotationValue == 90 || this.rotationValue == 270) ? this.originalHeight : this.originalWidth;
            int vheight = (this.rotationValue == 90 || this.rotationValue == 270) ? this.originalWidth : this.originalHeight;
            float ar = ((float) vwidth) / ((float) vheight);
            if (((float) width) / ((float) vwidth) > ((float) height) / ((float) vheight)) {
                width = (int) (((float) height) * ar);
            } else {
                height = (int) (((float) width) / ar);
            }
            if (this.textureView != null) {
                LayoutParams layoutParams = (LayoutParams) this.textureView.getLayoutParams();
                layoutParams.width = width;
                layoutParams.height = height;
                layoutParams.leftMargin = 0;
                layoutParams.topMargin = 0;
                this.textureView.setLayoutParams(layoutParams);
            }
        }
    }

    private void fixLayoutInternal() {
        int i = 20;
        if (getParentActivity() != null) {
            LayoutParams layoutParams;
            if (AndroidUtilities.isTablet() || getParentActivity().getResources().getConfiguration().orientation != 2) {
                layoutParams = (LayoutParams) this.videoContainerView.getLayoutParams();
                layoutParams.topMargin = AndroidUtilities.dp(16.0f);
                layoutParams.bottomMargin = AndroidUtilities.dp((float) ((this.compressVideo.getVisibility() == 0 ? 20 : 0) + 260));
                layoutParams.width = -1;
                layoutParams.leftMargin = 0;
                this.videoContainerView.setLayoutParams(layoutParams);
                layoutParams = (LayoutParams) this.controlView.getLayoutParams();
                layoutParams.topMargin = 0;
                layoutParams.leftMargin = 0;
                if (this.compressVideo.getVisibility() != 0) {
                    i = 0;
                }
                layoutParams.bottomMargin = AndroidUtilities.dp((float) (i + 150));
                layoutParams.width = -1;
                layoutParams.gravity = 80;
                this.controlView.setLayoutParams(layoutParams);
                layoutParams = (LayoutParams) this.textContainerView.getLayoutParams();
                layoutParams.width = -1;
                layoutParams.leftMargin = AndroidUtilities.dp(16.0f);
                layoutParams.rightMargin = AndroidUtilities.dp(16.0f);
                layoutParams.bottomMargin = AndroidUtilities.dp(16.0f);
                this.textContainerView.setLayoutParams(layoutParams);
            } else {
                layoutParams = (LayoutParams) this.videoContainerView.getLayoutParams();
                layoutParams.topMargin = AndroidUtilities.dp(16.0f);
                layoutParams.bottomMargin = AndroidUtilities.dp(16.0f);
                layoutParams.width = (AndroidUtilities.displaySize.x / 3) - AndroidUtilities.dp(24.0f);
                layoutParams.leftMargin = AndroidUtilities.dp(16.0f);
                this.videoContainerView.setLayoutParams(layoutParams);
                layoutParams = (LayoutParams) this.controlView.getLayoutParams();
                layoutParams.topMargin = AndroidUtilities.dp(16.0f);
                layoutParams.bottomMargin = 0;
                layoutParams.width = ((AndroidUtilities.displaySize.x / 3) * 2) - AndroidUtilities.dp(32.0f);
                layoutParams.leftMargin = (AndroidUtilities.displaySize.x / 3) + AndroidUtilities.dp(16.0f);
                layoutParams.gravity = 48;
                this.controlView.setLayoutParams(layoutParams);
                layoutParams = (LayoutParams) this.textContainerView.getLayoutParams();
                layoutParams.width = ((AndroidUtilities.displaySize.x / 3) * 2) - AndroidUtilities.dp(32.0f);
                layoutParams.leftMargin = (AndroidUtilities.displaySize.x / 3) + AndroidUtilities.dp(16.0f);
                layoutParams.rightMargin = AndroidUtilities.dp(16.0f);
                layoutParams.bottomMargin = AndroidUtilities.dp(16.0f);
                this.textContainerView.setLayoutParams(layoutParams);
            }
            fixVideoSize();
            this.videoTimelineView.clearFrames();
        }
    }

    private void fixLayout() {
        if (this.fragmentView != null) {
            this.fragmentView.getViewTreeObserver().addOnGlobalLayoutListener(new C12529());
        }
    }

    private void play() {
        if (this.videoPlayer != null && this.playerPrepared) {
            if (this.videoPlayer.isPlaying()) {
                this.videoPlayer.pause();
                this.playButton.setImageResource(C0553R.drawable.video_play);
                return;
            }
            try {
                this.playButton.setImageDrawable(null);
                this.lastProgress = 0.0f;
                if (this.needSeek) {
                    this.videoPlayer.seekTo((int) (this.videoDuration * this.videoSeekBarView.getProgress()));
                    this.needSeek = false;
                }
                this.videoPlayer.setOnSeekCompleteListener(new OnSeekCompleteListener() {
                    public void onSeekComplete(MediaPlayer mp) {
                        float startTime = VideoEditorActivity.this.videoTimelineView.getLeftProgress() * VideoEditorActivity.this.videoDuration;
                        float endTime = VideoEditorActivity.this.videoTimelineView.getRightProgress() * VideoEditorActivity.this.videoDuration;
                        if (startTime == endTime) {
                            startTime = endTime - 0.01f;
                        }
                        VideoEditorActivity.this.lastProgress = (((float) VideoEditorActivity.this.videoPlayer.getCurrentPosition()) - startTime) / (endTime - startTime);
                        VideoEditorActivity.this.lastProgress = VideoEditorActivity.this.videoTimelineView.getLeftProgress() + (VideoEditorActivity.this.lastProgress * (VideoEditorActivity.this.videoTimelineView.getRightProgress() - VideoEditorActivity.this.videoTimelineView.getLeftProgress()));
                        VideoEditorActivity.this.videoSeekBarView.setProgress(VideoEditorActivity.this.lastProgress);
                    }
                });
                this.videoPlayer.start();
                synchronized (this.sync) {
                    if (this.thread == null) {
                        this.thread = new Thread(this.progressRunnable);
                        this.thread.start();
                    }
                }
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
        }
    }

    public void setDelegate(VideoEditorActivityDelegate delegate) {
        this.delegate = delegate;
    }

    private boolean processOpenVideo() {
        try {
            this.originalSize = new File(this.videoPath).length();
            Container isoFile = new IsoFile(this.videoPath);
            List<Box> boxes = Path.getPaths(isoFile, "/moov/trak/");
            TrackHeaderBox trackHeaderBox = null;
            boolean isAvc = true;
            boolean isMp4A = true;
            if (Path.getPath(isoFile, "/moov/trak/mdia/minf/stbl/stsd/mp4a/") == null) {
                isMp4A = false;
            }
            if (!isMp4A) {
                return false;
            }
            int i;
            if (Path.getPath(isoFile, "/moov/trak/mdia/minf/stbl/stsd/avc1/") == null) {
                isAvc = false;
            }
            for (Box box : boxes) {
                TrackBox trackBox = (TrackBox) box;
                long sampleSizes = 0;
                long trackBitrate = 0;
                try {
                    MediaBox mediaBox = trackBox.getMediaBox();
                    MediaHeaderBox mediaHeaderBox = mediaBox.getMediaHeaderBox();
                    for (long size : mediaBox.getMediaInformationBox().getSampleTableBox().getSampleSizeBox().getSampleSizes()) {
                        sampleSizes += size;
                    }
                    this.videoDuration = ((float) mediaHeaderBox.getDuration()) / ((float) mediaHeaderBox.getTimescale());
                    trackBitrate = (long) ((int) (((float) (8 * sampleSizes)) / this.videoDuration));
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
                TrackHeaderBox headerBox = trackBox.getTrackHeaderBox();
                if (headerBox.getWidth() == 0.0d || headerBox.getHeight() == 0.0d) {
                    this.audioFramesSize += sampleSizes;
                } else {
                    trackHeaderBox = headerBox;
                    i = (int) ((trackBitrate / 100000) * 100000);
                    this.bitrate = i;
                    this.originalBitrate = i;
                    if (this.bitrate > 900000) {
                        this.bitrate = 900000;
                    }
                    this.videoFramesSize += sampleSizes;
                }
            }
            if (trackHeaderBox == null) {
                return false;
            }
            Matrix matrix = trackHeaderBox.getMatrix();
            if (matrix.equals(Matrix.ROTATE_90)) {
                this.rotationValue = 90;
            } else if (matrix.equals(Matrix.ROTATE_180)) {
                this.rotationValue = 180;
            } else if (matrix.equals(Matrix.ROTATE_270)) {
                this.rotationValue = 270;
            }
            i = (int) trackHeaderBox.getWidth();
            this.originalWidth = i;
            this.resultWidth = i;
            i = (int) trackHeaderBox.getHeight();
            this.originalHeight = i;
            this.resultHeight = i;
            if (this.resultWidth > 640 || this.resultHeight > 640) {
                float scale;
                if (this.resultWidth > this.resultHeight) {
                    scale = 640.0f / ((float) this.resultWidth);
                } else {
                    scale = 640.0f / ((float) this.resultHeight);
                }
                this.resultWidth = (int) (((float) this.resultWidth) * scale);
                this.resultHeight = (int) (((float) this.resultHeight) * scale);
                if (this.bitrate != 0) {
                    this.bitrate = (int) (((float) this.bitrate) * Math.max(0.5f, scale));
                    this.videoFramesSize = (long) (((float) (this.bitrate / 8)) * this.videoDuration);
                }
            }
            if (!isAvc && (this.resultWidth == this.originalWidth || this.resultHeight == this.originalHeight)) {
                return false;
            }
            this.videoDuration *= 1000.0f;
            updateVideoOriginalInfo();
            updateVideoEditedInfo();
            return true;
        } catch (Throwable e2) {
            FileLog.m611e("tmessages", e2);
            return false;
        }
    }

    private int calculateEstimatedSize(float timeDelta) {
        int size = (int) (((float) (this.audioFramesSize + this.videoFramesSize)) * timeDelta);
        return size + ((size / 32768) * 16);
    }
}
