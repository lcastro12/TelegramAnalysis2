package org.telegram.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils.TruncateAt;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Scroller;
import android.widget.TextView;
import com.google.android.gms.location.LocationStatusCodes;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.AnimationCompat.AnimatorListenerAdapterProxy;
import org.telegram.messenger.AnimationCompat.AnimatorSetProxy;
import org.telegram.messenger.AnimationCompat.ObjectAnimatorProxy;
import org.telegram.messenger.AnimationCompat.ViewProxy;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.C0553R;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MediaController.PhotoEntry;
import org.telegram.messenger.MediaController.SearchImage;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.query.SharedMediaQuery;
import org.telegram.messenger.support.widget.helper.ItemTouchHelper.Callback;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.EncryptedChat;
import org.telegram.tgnet.TLRPC.FileLocation;
import org.telegram.tgnet.TLRPC.InputFileLocation;
import org.telegram.tgnet.TLRPC.Photo;
import org.telegram.tgnet.TLRPC.PhotoSize;
import org.telegram.tgnet.TLRPC.TL_inputFileLocation;
import org.telegram.tgnet.TLRPC.TL_inputPhoto;
import org.telegram.tgnet.TLRPC.TL_inputVideoFileLocation;
import org.telegram.tgnet.TLRPC.TL_messageActionEmpty;
import org.telegram.tgnet.TLRPC.TL_messageActionUserUpdatedPhoto;
import org.telegram.tgnet.TLRPC.TL_messageMediaPhoto;
import org.telegram.tgnet.TLRPC.TL_messageMediaVideo;
import org.telegram.tgnet.TLRPC.TL_messageMediaWebPage;
import org.telegram.tgnet.TLRPC.TL_messageService;
import org.telegram.tgnet.TLRPC.TL_photoEmpty;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.DrawerLayoutContainer;
import org.telegram.ui.Adapters.MentionsAdapter;
import org.telegram.ui.Adapters.MentionsAdapter.MentionsAdapterDelegate;
import org.telegram.ui.Components.CheckBox;
import org.telegram.ui.Components.ClippingImageView;
import org.telegram.ui.Components.GifDrawable;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.PhotoCropView;
import org.telegram.ui.Components.PhotoCropView.PhotoCropViewDelegate;
import org.telegram.ui.Components.PhotoFilterView;
import org.telegram.ui.Components.PhotoViewerCaptionEnterView;
import org.telegram.ui.Components.PhotoViewerCaptionEnterView.PhotoViewerCaptionEnterViewDelegate;
import org.telegram.ui.Components.PickerBottomLayout;
import org.telegram.ui.Components.SizeNotifierFrameLayoutPhoto;

public class PhotoViewer implements NotificationCenterDelegate, OnGestureListener, OnDoubleTapListener {
    private static volatile PhotoViewer Instance = null;
    private static final int PAGE_SPACING = AndroidUtilities.dp(BitmapDescriptorFactory.HUE_ORANGE);
    private static DecelerateInterpolator decelerateInterpolator = null;
    private static final int gallery_menu_caption = 8;
    private static final int gallery_menu_caption_done = 9;
    private static final int gallery_menu_crop = 4;
    private static final int gallery_menu_delete = 6;
    private static final int gallery_menu_save = 1;
    private static final int gallery_menu_send = 3;
    private static final int gallery_menu_showall = 2;
    private static final int gallery_menu_tune = 7;
    private static Drawable[] progressDrawables;
    private static Paint progressPaint = null;
    private ActionBar actionBar;
    private boolean allowMentions;
    private float animateToScale;
    private float animateToX;
    private float animateToY;
    private ClippingImageView animatingImageView;
    private Runnable animationEndRunnable = null;
    private int animationInProgress = 0;
    private long animationStartTime;
    private float animationValue;
    private float[][] animationValues = ((float[][]) Array.newInstance(Float.TYPE, new int[]{2, 8}));
    private ArrayList<Photo> avatarsArr = new ArrayList();
    private int avatarsUserId;
    private BackgroundDrawable backgroundDrawable = new BackgroundDrawable(ViewCompat.MEASURED_STATE_MASK);
    private FrameLayout bottomLayout;
    private boolean canDragDown = true;
    private boolean canShowBottom = true;
    private boolean canZoom = true;
    private ActionBarMenuItem captionDoneItem;
    private PhotoViewerCaptionEnterView captionEditText;
    private ActionBarMenuItem captionItem;
    private TextView captionTextView;
    private TextView captionTextViewNew;
    private TextView captionTextViewOld;
    private ImageReceiver centerImage = new ImageReceiver();
    private AnimatorSetProxy changeModeAnimation;
    private boolean changingPage = false;
    private CheckBox checkImageView;
    private int classGuid;
    private FrameLayoutDrawer containerView;
    private ActionBarMenuItem cropItem;
    private AnimatorSetProxy currentActionBarAnimation;
    private long currentDialogId;
    private int currentEditMode;
    private FileLocation currentFileLocation;
    private String[] currentFileNames = new String[3];
    private int currentIndex;
    private MessageObject currentMessageObject;
    private String currentPathObject;
    private PlaceProviderObject currentPlaceObject;
    private Bitmap currentThumb = null;
    private FileLocation currentUserAvatarLocation = null;
    private TextView dateTextView;
    private boolean disableShowCheck = false;
    private boolean discardTap = false;
    private boolean doubleTap = false;
    private float dragY;
    private boolean draggingDown = false;
    private PickerBottomLayout editorDoneLayout;
    private boolean[] endReached = new boolean[]{false, true};
    private GestureDetector gestureDetector;
    private GifDrawable gifDrawable;
    private PlaceProviderObject hideAfterAnimation;
    private AnimatorSetProxy imageMoveAnimation;
    private ArrayList<MessageObject> imagesArr = new ArrayList();
    private ArrayList<Object> imagesArrLocals = new ArrayList();
    private ArrayList<FileLocation> imagesArrLocations = new ArrayList();
    private ArrayList<Integer> imagesArrLocationsSizes = new ArrayList();
    private ArrayList<MessageObject> imagesArrTemp = new ArrayList();
    private HashMap<Integer, MessageObject>[] imagesByIds = new HashMap[]{new HashMap(), new HashMap()};
    private HashMap<Integer, MessageObject>[] imagesByIdsTemp = new HashMap[]{new HashMap(), new HashMap()};
    private DecelerateInterpolator interpolator = new DecelerateInterpolator(1.5f);
    private boolean invalidCoords = false;
    private boolean isActionBarVisible = true;
    private boolean isFirstLoading;
    private boolean isVisible;
    private String lastTitle;
    private ImageReceiver leftImage = new ImageReceiver();
    private boolean loadingMoreImages;
    private float maxX;
    private float maxY;
    private AnimatorSetProxy mentionListAnimation;
    private ListView mentionListView;
    private MentionsAdapter mentionsAdapter;
    private ActionBarMenuItem menuItem;
    private long mergeDialogId;
    private float minX;
    private float minY;
    private float moveStartX;
    private float moveStartY;
    private boolean moving = false;
    private TextView nameTextView;
    private boolean needCaptionLayout;
    private boolean needSearchImageInArr;
    private boolean opennedFromMedia;
    private Activity parentActivity;
    private ChatActivity parentChatActivity;
    private PhotoCropView photoCropView;
    private PhotoFilterView photoFilterView;
    private PickerBottomLayout pickerView;
    private float pinchCenterX;
    private float pinchCenterY;
    private float pinchStartDistance = 0.0f;
    private float pinchStartScale = 1.0f;
    private float pinchStartX;
    private float pinchStartY;
    private PhotoViewerProvider placeProvider;
    private RadialProgressView[] radialProgressViews = new RadialProgressView[3];
    private ImageReceiver rightImage = new ImageReceiver();
    private float scale = 1.0f;
    private Scroller scroller = null;
    private int sendPhotoType = 0;
    private ImageView shareButton;
    private PlaceProviderObject showAfterAnimation;
    private int switchImageAfterAnimation = 0;
    private int totalImagesCount;
    private int totalImagesCountMerge;
    private long transitionAnimationStartTime = 0;
    private float translationX = 0.0f;
    private float translationY = 0.0f;
    private ActionBarMenuItem tuneItem;
    private VelocityTracker velocityTracker = null;
    private AlertDialog visibleDialog;
    private LayoutParams windowLayoutParams;
    private FrameLayoutTouchListener windowView;
    private boolean zoomAnimation = false;
    private boolean zooming = false;

    class C11503 implements OnClickListener {
        C11503() {
        }

        public void onClick(View v) {
            if (PhotoViewer.this.parentActivity != null) {
                File f = null;
                try {
                    Intent intent;
                    if (PhotoViewer.this.currentMessageObject != null) {
                        if (PhotoViewer.this.currentMessageObject.messageOwner.media instanceof TL_messageMediaWebPage) {
                            intent = new Intent("android.intent.action.VIEW", Uri.parse(PhotoViewer.this.currentMessageObject.messageOwner.media.webpage.url));
                            intent.putExtra("com.android.browser.application_id", PhotoViewer.this.parentActivity.getPackageName());
                            PhotoViewer.this.parentActivity.startActivity(intent);
                            return;
                        }
                        f = FileLoader.getPathToMessage(PhotoViewer.this.currentMessageObject.messageOwner);
                    } else if (PhotoViewer.this.currentFileLocation != null) {
                        f = FileLoader.getPathToAttach(PhotoViewer.this.currentFileLocation, PhotoViewer.this.avatarsUserId != 0);
                    }
                    if (f.exists()) {
                        intent = new Intent("android.intent.action.SEND");
                        if (f.toString().endsWith("mp4")) {
                            intent.setType("video/mp4");
                        } else {
                            intent.setType("image/jpeg");
                        }
                        intent.putExtra("android.intent.extra.STREAM", Uri.fromFile(f));
                        PhotoViewer.this.parentActivity.startActivityForResult(Intent.createChooser(intent, LocaleController.getString("ShareFile", C0553R.string.ShareFile)), 500);
                        return;
                    }
                    Builder builder = new Builder(PhotoViewer.this.parentActivity);
                    builder.setTitle(LocaleController.getString("AppName", C0553R.string.AppName));
                    builder.setPositiveButton(LocaleController.getString("OK", C0553R.string.OK), null);
                    builder.setMessage(LocaleController.getString("PleaseDownload", C0553R.string.PleaseDownload));
                    PhotoViewer.this.showAlertDialog(builder);
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
        }
    }

    class C11514 implements OnClickListener {
        C11514() {
        }

        public void onClick(View view) {
            if (PhotoViewer.this.placeProvider != null) {
                boolean z;
                PhotoViewer photoViewer = PhotoViewer.this;
                if (PhotoViewer.this.placeProvider.cancelButtonPressed()) {
                    z = false;
                } else {
                    z = true;
                }
                photoViewer.closePhoto(z, false);
            }
        }
    }

    class C11525 implements OnClickListener {
        C11525() {
        }

        public void onClick(View view) {
            if (PhotoViewer.this.placeProvider != null) {
                PhotoViewer.this.placeProvider.sendButtonPressed(PhotoViewer.this.currentIndex);
                PhotoViewer.this.closePhoto(false, false);
            }
        }
    }

    class C11536 implements OnClickListener {
        C11536() {
        }

        public void onClick(View view) {
            if (PhotoViewer.this.currentEditMode == 1) {
                PhotoViewer.this.photoCropView.cancelAnimationRunnable();
            }
            PhotoViewer.this.switchToEditMode(0);
        }
    }

    class C11547 implements OnClickListener {
        C11547() {
        }

        public void onClick(View view) {
            if (PhotoViewer.this.currentEditMode == 1) {
                PhotoViewer.this.photoCropView.cancelAnimationRunnable();
                if (PhotoViewer.this.imageMoveAnimation != null) {
                    return;
                }
            }
            PhotoViewer.this.applyCurrentEditMode();
            PhotoViewer.this.switchToEditMode(0);
        }
    }

    class C11558 implements OnClickListener {
        C11558() {
        }

        public void onClick(View v) {
            if (PhotoViewer.this.placeProvider != null) {
                PhotoViewer.this.placeProvider.setPhotoChecked(PhotoViewer.this.currentIndex);
                PhotoViewer.this.checkImageView.setChecked(PhotoViewer.this.placeProvider.isPhotoChecked(PhotoViewer.this.currentIndex), true);
                PhotoViewer.this.updateSelectedCount();
            }
        }
    }

    private class BackgroundDrawable extends ColorDrawable {
        private Runnable drawRunnable;

        public BackgroundDrawable(int color) {
            super(color);
        }

        public void setAlpha(int alpha) {
            if (PhotoViewer.this.parentActivity instanceof LaunchActivity) {
                DrawerLayoutContainer drawerLayoutContainer = ((LaunchActivity) PhotoViewer.this.parentActivity).drawerLayoutContainer;
                boolean z = (PhotoViewer.this.isVisible && alpha == 255) ? false : true;
                drawerLayoutContainer.setAllowDrawContent(z);
            }
            super.setAlpha(alpha);
        }

        public void draw(Canvas canvas) {
            super.draw(canvas);
            if (getAlpha() != 0 && this.drawRunnable != null) {
                this.drawRunnable.run();
                this.drawRunnable = null;
            }
        }
    }

    private class FrameLayoutTouchListener extends FrameLayout {
        private Runnable attachRunnable;
        private boolean attachedToWindow;

        public FrameLayoutTouchListener(Context context) {
            super(context);
        }

        public boolean onTouchEvent(MotionEvent event) {
            return PhotoViewer.getInstance().onTouchEvent(event);
        }

        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            super.onLayout(changed, left, top, right, bottom);
            PhotoViewer.getInstance().onLayout(changed, left, top, right, bottom);
        }

        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            this.attachedToWindow = true;
        }

        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            this.attachedToWindow = false;
        }
    }

    public interface PhotoViewerProvider {
        boolean cancelButtonPressed();

        PlaceProviderObject getPlaceForPhoto(MessageObject messageObject, FileLocation fileLocation, int i);

        int getSelectedCount();

        Bitmap getThumbForPhoto(MessageObject messageObject, FileLocation fileLocation, int i);

        boolean isPhotoChecked(int i);

        void sendButtonPressed(int i);

        void setPhotoChecked(int i);

        void updatePhotoAtIndex(int i);

        void willHidePhotoViewer();

        void willSwitchFromPhoto(MessageObject messageObject, FileLocation fileLocation, int i);
    }

    public static class PlaceProviderObject {
        public int clipBottomAddition;
        public int clipTopAddition;
        public ImageReceiver imageReceiver;
        public int index;
        public View parentView;
        public int radius;
        public float scale = 1.0f;
        public int size;
        public Bitmap thumb;
        public int user_id;
        public int viewX;
        public int viewY;
    }

    private class RadialProgressView {
        private float alpha = 1.0f;
        private float animatedAlphaValue = 1.0f;
        private float animatedProgressValue = 0.0f;
        private float animationProgressStart = 0.0f;
        private int backgroundState = -1;
        private float currentProgress = 0.0f;
        private long currentProgressTime = 0;
        private long lastUpdateTime = 0;
        private View parent = null;
        private int previousBackgroundState = -2;
        private RectF progressRect = new RectF();
        private float radOffset = 0.0f;
        private float scale = 1.0f;
        private int size = AndroidUtilities.dp(64.0f);

        public RadialProgressView(Context context, View parentView) {
            if (PhotoViewer.decelerateInterpolator == null) {
                PhotoViewer.decelerateInterpolator = new DecelerateInterpolator(1.5f);
                PhotoViewer.progressPaint = new Paint(1);
                PhotoViewer.progressPaint.setStyle(Style.STROKE);
                PhotoViewer.progressPaint.setStrokeCap(Cap.ROUND);
                PhotoViewer.progressPaint.setStrokeWidth((float) AndroidUtilities.dp(2.0f));
                PhotoViewer.progressPaint.setColor(-1);
            }
            this.parent = parentView;
        }

        private void updateAnimation() {
            long newTime = System.currentTimeMillis();
            long dt = newTime - this.lastUpdateTime;
            this.lastUpdateTime = newTime;
            if (this.animatedProgressValue != 1.0f) {
                this.radOffset += ((float) (360 * dt)) / 3000.0f;
                float progressDiff = this.currentProgress - this.animationProgressStart;
                if (progressDiff > 0.0f) {
                    this.currentProgressTime += dt;
                    if (this.currentProgressTime >= 300) {
                        this.animatedProgressValue = this.currentProgress;
                        this.animationProgressStart = this.currentProgress;
                        this.currentProgressTime = 0;
                    } else {
                        this.animatedProgressValue = this.animationProgressStart + (PhotoViewer.decelerateInterpolator.getInterpolation(((float) this.currentProgressTime) / BitmapDescriptorFactory.HUE_MAGENTA) * progressDiff);
                    }
                }
                this.parent.invalidate();
            }
            if (this.animatedProgressValue >= 1.0f && this.previousBackgroundState != -2) {
                this.animatedAlphaValue -= ((float) dt) / 200.0f;
                if (this.animatedAlphaValue <= 0.0f) {
                    this.animatedAlphaValue = 0.0f;
                    this.previousBackgroundState = -2;
                }
                this.parent.invalidate();
            }
        }

        public void setProgress(float value, boolean animated) {
            if (animated) {
                this.animationProgressStart = this.animatedProgressValue;
            } else {
                this.animatedProgressValue = value;
                this.animationProgressStart = value;
            }
            this.currentProgress = value;
            this.currentProgressTime = 0;
        }

        public void setBackgroundState(int state, boolean animated) {
            this.lastUpdateTime = System.currentTimeMillis();
            if (!animated || this.backgroundState == state) {
                this.previousBackgroundState = -2;
            } else {
                this.previousBackgroundState = this.backgroundState;
                this.animatedAlphaValue = 1.0f;
            }
            this.backgroundState = state;
            this.parent.invalidate();
        }

        public void setAlpha(float value) {
            this.alpha = value;
        }

        public void setScale(float value) {
            this.scale = value;
        }

        public void onDraw(Canvas canvas) {
            Drawable drawable;
            int sizeScaled = (int) (((float) this.size) * this.scale);
            int x = (PhotoViewer.this.getContainerViewWidth() - sizeScaled) / 2;
            int y = (PhotoViewer.this.getContainerViewHeight() - sizeScaled) / 2;
            if (this.previousBackgroundState >= 0 && this.previousBackgroundState < 4) {
                drawable = PhotoViewer.progressDrawables[this.previousBackgroundState];
                if (drawable != null) {
                    drawable.setAlpha((int) ((this.animatedAlphaValue * 255.0f) * this.alpha));
                    drawable.setBounds(x, y, x + sizeScaled, y + sizeScaled);
                    drawable.draw(canvas);
                }
            }
            if (this.backgroundState >= 0 && this.backgroundState < 4) {
                drawable = PhotoViewer.progressDrawables[this.backgroundState];
                if (drawable != null) {
                    if (this.previousBackgroundState != -2) {
                        drawable.setAlpha((int) (((1.0f - this.animatedAlphaValue) * 255.0f) * this.alpha));
                    } else {
                        drawable.setAlpha((int) (this.alpha * 255.0f));
                    }
                    drawable.setBounds(x, y, x + sizeScaled, y + sizeScaled);
                    drawable.draw(canvas);
                }
            }
            if (this.backgroundState == 0 || this.backgroundState == 1 || this.previousBackgroundState == 0 || this.previousBackgroundState == 1) {
                int diff = AndroidUtilities.dp(1.0f);
                if (this.previousBackgroundState != -2) {
                    PhotoViewer.progressPaint.setAlpha((int) ((this.animatedAlphaValue * 255.0f) * this.alpha));
                } else {
                    PhotoViewer.progressPaint.setAlpha((int) (this.alpha * 255.0f));
                }
                this.progressRect.set((float) (x + diff), (float) (y + diff), (float) ((x + sizeScaled) - diff), (float) ((y + sizeScaled) - diff));
                canvas.drawArc(this.progressRect, this.radOffset - 0.049804688f, Math.max(4.0f, 360.0f * this.animatedProgressValue), false, PhotoViewer.progressPaint);
                updateAnimation();
            }
        }
    }

    class C16462 extends ActionBarMenuOnItemClick {

        class C11471 implements DialogInterface.OnClickListener {
            C11471() {
            }

            public void onClick(DialogInterface dialogInterface, int i) {
                if (PhotoViewer.this.imagesArr.isEmpty()) {
                    if (!PhotoViewer.this.avatarsArr.isEmpty() && PhotoViewer.this.currentIndex >= 0 && PhotoViewer.this.currentIndex < PhotoViewer.this.avatarsArr.size()) {
                        Photo photo = (Photo) PhotoViewer.this.avatarsArr.get(PhotoViewer.this.currentIndex);
                        FileLocation currentLocation = (FileLocation) PhotoViewer.this.imagesArrLocations.get(PhotoViewer.this.currentIndex);
                        if (photo instanceof TL_photoEmpty) {
                            photo = null;
                        }
                        boolean current = false;
                        if (PhotoViewer.this.currentUserAvatarLocation != null) {
                            if (photo != null) {
                                Iterator i$ = photo.sizes.iterator();
                                while (i$.hasNext()) {
                                    PhotoSize size = (PhotoSize) i$.next();
                                    if (size.location.local_id == PhotoViewer.this.currentUserAvatarLocation.local_id && size.location.volume_id == PhotoViewer.this.currentUserAvatarLocation.volume_id) {
                                        current = true;
                                        break;
                                    }
                                }
                            } else if (currentLocation.local_id == PhotoViewer.this.currentUserAvatarLocation.local_id && currentLocation.volume_id == PhotoViewer.this.currentUserAvatarLocation.volume_id) {
                                current = true;
                            }
                        }
                        if (current) {
                            MessagesController.getInstance().deleteUserPhoto(null);
                            PhotoViewer.this.closePhoto(false, false);
                        } else if (photo != null) {
                            TL_inputPhoto inputPhoto = new TL_inputPhoto();
                            inputPhoto.id = photo.id;
                            inputPhoto.access_hash = photo.access_hash;
                            MessagesController.getInstance().deleteUserPhoto(inputPhoto);
                            MessagesStorage.getInstance().clearUserPhoto(PhotoViewer.this.avatarsUserId, photo.id);
                            PhotoViewer.this.imagesArrLocations.remove(PhotoViewer.this.currentIndex);
                            PhotoViewer.this.imagesArrLocationsSizes.remove(PhotoViewer.this.currentIndex);
                            PhotoViewer.this.avatarsArr.remove(PhotoViewer.this.currentIndex);
                            if (PhotoViewer.this.imagesArrLocations.isEmpty()) {
                                PhotoViewer.this.closePhoto(false, false);
                                return;
                            }
                            int index = PhotoViewer.this.currentIndex;
                            if (index >= PhotoViewer.this.avatarsArr.size()) {
                                index = PhotoViewer.this.avatarsArr.size() - 1;
                            }
                            PhotoViewer.this.currentIndex = -1;
                            PhotoViewer.this.setImageIndex(index, true);
                        }
                    }
                } else if (PhotoViewer.this.currentIndex >= 0 && PhotoViewer.this.currentIndex < PhotoViewer.this.imagesArr.size()) {
                    MessageObject obj = (MessageObject) PhotoViewer.this.imagesArr.get(PhotoViewer.this.currentIndex);
                    if (obj.isSent()) {
                        ArrayList<Integer> arr = new ArrayList();
                        arr.add(Integer.valueOf(obj.getId()));
                        ArrayList<Long> random_ids = null;
                        EncryptedChat encryptedChat = null;
                        if (((int) obj.getDialogId()) == 0 && obj.messageOwner.random_id != 0) {
                            random_ids = new ArrayList();
                            random_ids.add(Long.valueOf(obj.messageOwner.random_id));
                            encryptedChat = MessagesController.getInstance().getEncryptedChat(Integer.valueOf((int) (obj.getDialogId() >> 32)));
                        }
                        MessagesController.getInstance().deleteMessages(arr, random_ids, encryptedChat, obj.messageOwner.to_id.channel_id);
                        PhotoViewer.this.closePhoto(false, false);
                    }
                }
            }
        }

        C16462() {
        }

        public void onItemClick(int id) {
            int i = 1;
            if (id == -1) {
                if (PhotoViewer.this.needCaptionLayout && (PhotoViewer.this.captionEditText.isPopupShowing() || PhotoViewer.this.captionEditText.isKeyboardVisible())) {
                    PhotoViewer.this.closeCaptionEnter(false);
                } else {
                    PhotoViewer.this.closePhoto(true, false);
                }
            } else if (id == 1) {
                if (VERSION.SDK_INT < 23 || PhotoViewer.this.parentActivity.checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") == 0) {
                    File f = null;
                    if (PhotoViewer.this.currentMessageObject != null) {
                        f = FileLoader.getPathToMessage(PhotoViewer.this.currentMessageObject.messageOwner);
                    } else if (PhotoViewer.this.currentFileLocation != null) {
                        f = FileLoader.getPathToAttach(PhotoViewer.this.currentFileLocation, PhotoViewer.this.avatarsUserId != 0);
                    }
                    if (f == null || !f.exists()) {
                        builder = new Builder(PhotoViewer.this.parentActivity);
                        builder.setTitle(LocaleController.getString("AppName", C0553R.string.AppName));
                        builder.setPositiveButton(LocaleController.getString("OK", C0553R.string.OK), null);
                        builder.setMessage(LocaleController.getString("PleaseDownload", C0553R.string.PleaseDownload));
                        PhotoViewer.this.showAlertDialog(builder);
                        return;
                    }
                    String file = f.toString();
                    Context access$000 = PhotoViewer.this.parentActivity;
                    if (!PhotoViewer.this.currentFileNames[0].endsWith("mp4")) {
                        i = 0;
                    }
                    MediaController.saveFile(file, access$000, i, null);
                    return;
                }
                PhotoViewer.this.parentActivity.requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 4);
            } else if (id == 2) {
                if (PhotoViewer.this.opennedFromMedia) {
                    PhotoViewer.this.closePhoto(true, false);
                } else if (PhotoViewer.this.currentDialogId != 0) {
                    PhotoViewer.this.disableShowCheck = true;
                    PhotoViewer.this.closePhoto(false, false);
                    Bundle args2 = new Bundle();
                    args2.putLong("dialog_id", PhotoViewer.this.currentDialogId);
                    ((LaunchActivity) PhotoViewer.this.parentActivity).presentFragment(new MediaActivity(args2), false, true);
                }
            } else if (id == 3) {
            } else {
                if (id == 4) {
                    PhotoViewer.this.switchToEditMode(1);
                } else if (id == 7) {
                    PhotoViewer.this.switchToEditMode(2);
                } else if (id == 6) {
                    if (PhotoViewer.this.parentActivity != null) {
                        builder = new Builder(PhotoViewer.this.parentActivity);
                        if (PhotoViewer.this.currentFileNames[0] == null || !PhotoViewer.this.currentFileNames[0].endsWith("mp4")) {
                            builder.setMessage(LocaleController.formatString("AreYouSureDeletePhoto", C0553R.string.AreYouSureDeletePhoto, new Object[0]));
                        } else {
                            builder.setMessage(LocaleController.formatString("AreYouSureDeleteVideo", C0553R.string.AreYouSureDeleteVideo, new Object[0]));
                        }
                        builder.setTitle(LocaleController.getString("AppName", C0553R.string.AppName));
                        builder.setPositiveButton(LocaleController.getString("OK", C0553R.string.OK), new C11471());
                        builder.setNegativeButton(LocaleController.getString("Cancel", C0553R.string.Cancel), null);
                        PhotoViewer.this.showAlertDialog(builder);
                    }
                } else if (id == 8) {
                    if (PhotoViewer.this.imageMoveAnimation == null && PhotoViewer.this.changeModeAnimation == null) {
                        PhotoViewer.this.cropItem.setVisibility(8);
                        PhotoViewer.this.tuneItem.setVisibility(8);
                        PhotoViewer.this.captionItem.setVisibility(8);
                        PhotoViewer.this.checkImageView.setVisibility(8);
                        PhotoViewer.this.captionDoneItem.setVisibility(0);
                        PhotoViewer.this.pickerView.setVisibility(8);
                        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) PhotoViewer.this.captionEditText.getLayoutParams();
                        layoutParams.bottomMargin = 0;
                        PhotoViewer.this.captionEditText.setLayoutParams(layoutParams);
                        layoutParams = (FrameLayout.LayoutParams) PhotoViewer.this.mentionListView.getLayoutParams();
                        layoutParams.bottomMargin = 0;
                        PhotoViewer.this.mentionListView.setLayoutParams(layoutParams);
                        PhotoViewer.this.captionTextView.clearAnimation();
                        PhotoViewer.this.captionTextView.setVisibility(4);
                        PhotoViewer.this.captionEditText.openKeyboard();
                        PhotoViewer.this.lastTitle = PhotoViewer.this.actionBar.getTitle();
                        PhotoViewer.this.actionBar.setTitle(LocaleController.getString("PhotoCaption", C0553R.string.PhotoCaption));
                    }
                } else if (id == 9) {
                    PhotoViewer.this.closeCaptionEnter(true);
                }
            }
        }

        public boolean canOpenMenu() {
            if (PhotoViewer.this.currentMessageObject != null) {
                if (FileLoader.getPathToMessage(PhotoViewer.this.currentMessageObject.messageOwner).exists()) {
                    return true;
                }
            } else if (PhotoViewer.this.currentFileLocation != null) {
                if (FileLoader.getPathToAttach(PhotoViewer.this.currentFileLocation, PhotoViewer.this.avatarsUserId != 0).exists()) {
                    return true;
                }
            }
            return false;
        }
    }

    class C16479 implements PhotoViewerCaptionEnterViewDelegate {
        C16479() {
        }

        public void onCaptionEnter() {
            PhotoViewer.this.closeCaptionEnter(true);
        }

        public void onTextChanged(CharSequence text) {
            if (PhotoViewer.this.mentionsAdapter != null && PhotoViewer.this.captionEditText != null && PhotoViewer.this.parentChatActivity != null && text != null) {
                PhotoViewer.this.mentionsAdapter.searchUsernameOrHashtag(text.toString(), PhotoViewer.this.captionEditText.getCursorPosition(), PhotoViewer.this.parentChatActivity.messages);
            }
        }

        public void onWindowSizeChanged(int size) {
            int i;
            int min = Math.min(3, PhotoViewer.this.mentionsAdapter.getCount()) * 36;
            if (PhotoViewer.this.mentionsAdapter.getCount() > 3) {
                i = 18;
            } else {
                i = 0;
            }
            if (size - (ActionBar.getCurrentActionBarHeight() * 2) < AndroidUtilities.dp((float) (i + min))) {
                PhotoViewer.this.allowMentions = false;
                if (PhotoViewer.this.mentionListView != null && PhotoViewer.this.mentionListView.getVisibility() == 0) {
                    PhotoViewer.this.mentionListView.clearAnimation();
                    PhotoViewer.this.mentionListView.setVisibility(4);
                    return;
                }
                return;
            }
            PhotoViewer.this.allowMentions = true;
            if (PhotoViewer.this.mentionListView != null && PhotoViewer.this.mentionListView.getVisibility() == 4) {
                PhotoViewer.this.mentionListView.clearAnimation();
                PhotoViewer.this.mentionListView.setVisibility(0);
            }
        }
    }

    public static class EmptyPhotoViewerProvider implements PhotoViewerProvider {
        public PlaceProviderObject getPlaceForPhoto(MessageObject messageObject, FileLocation fileLocation, int index) {
            return null;
        }

        public Bitmap getThumbForPhoto(MessageObject messageObject, FileLocation fileLocation, int index) {
            return null;
        }

        public void willSwitchFromPhoto(MessageObject messageObject, FileLocation fileLocation, int index) {
        }

        public void willHidePhotoViewer() {
        }

        public boolean isPhotoChecked(int index) {
            return false;
        }

        public void setPhotoChecked(int index) {
        }

        public boolean cancelButtonPressed() {
            return true;
        }

        public void sendButtonPressed(int index) {
        }

        public int getSelectedCount() {
            return 0;
        }

        public void updatePhotoAtIndex(int index) {
        }
    }

    private class FrameLayoutDrawer extends SizeNotifierFrameLayoutPhoto {
        public FrameLayoutDrawer(Context context) {
            super(context);
            setWillNotDraw(false);
        }

        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightSize = MeasureSpec.getSize(heightMeasureSpec);
            if (heightSize > AndroidUtilities.displaySize.y - AndroidUtilities.statusBarHeight) {
                heightSize = AndroidUtilities.displaySize.y - AndroidUtilities.statusBarHeight;
            }
            setMeasuredDimension(widthSize, heightSize);
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = getChildAt(i);
                if (child.getVisibility() != 8) {
                    if (PhotoViewer.this.captionEditText.isPopupView(child)) {
                        child.measure(MeasureSpec.makeMeasureSpec(widthSize, 1073741824), MeasureSpec.makeMeasureSpec(child.getLayoutParams().height, 1073741824));
                    } else {
                        measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                    }
                }
            }
        }

        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            int count = getChildCount();
            int paddingBottom = getKeyboardHeight() <= AndroidUtilities.dp(20.0f) ? PhotoViewer.this.captionEditText.getEmojiPadding() : 0;
            for (int i = 0; i < count; i++) {
                View child = getChildAt(i);
                if (child.getVisibility() != 8) {
                    int childLeft;
                    int childTop;
                    FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) child.getLayoutParams();
                    int width = child.getMeasuredWidth();
                    int height = child.getMeasuredHeight();
                    int gravity = lp.gravity;
                    if (gravity == -1) {
                        gravity = 51;
                    }
                    int verticalGravity = gravity & 112;
                    switch ((gravity & 7) & 7) {
                        case 1:
                            childLeft = ((((r - l) - width) / 2) + lp.leftMargin) - lp.rightMargin;
                            break;
                        case 5:
                            childLeft = (r - width) - lp.rightMargin;
                            break;
                        default:
                            childLeft = lp.leftMargin;
                            break;
                    }
                    switch (verticalGravity) {
                        case 16:
                            childTop = (((((b - paddingBottom) - t) - height) / 2) + lp.topMargin) - lp.bottomMargin;
                            break;
                        case 48:
                            childTop = lp.topMargin;
                            break;
                        case 80:
                            childTop = (((b - paddingBottom) - t) - height) - lp.bottomMargin;
                            break;
                        default:
                            childTop = lp.topMargin;
                            break;
                    }
                    if (child == PhotoViewer.this.mentionListView) {
                        if (PhotoViewer.this.captionEditText.isPopupShowing() || PhotoViewer.this.captionEditText.isKeyboardVisible() || PhotoViewer.this.captionEditText.getEmojiPadding() != 0) {
                            childTop -= PhotoViewer.this.captionEditText.getMeasuredHeight();
                        } else {
                            childTop += AndroidUtilities.dp(400.0f);
                        }
                    } else if (child == PhotoViewer.this.captionEditText) {
                        if (!(PhotoViewer.this.captionEditText.isPopupShowing() || PhotoViewer.this.captionEditText.isKeyboardVisible() || PhotoViewer.this.captionEditText.getEmojiPadding() != 0)) {
                            childTop += AndroidUtilities.dp(400.0f);
                        }
                    } else if (child == PhotoViewer.this.pickerView || child == PhotoViewer.this.captionTextViewNew || child == PhotoViewer.this.captionTextViewOld) {
                        if (PhotoViewer.this.captionEditText.isPopupShowing() || PhotoViewer.this.captionEditText.isKeyboardVisible()) {
                            childTop += AndroidUtilities.dp(400.0f);
                        }
                    } else if (PhotoViewer.this.captionEditText.isPopupView(child)) {
                        childTop = PhotoViewer.this.captionEditText.getBottom();
                    }
                    child.layout(childLeft, childTop, childLeft + width, childTop + height);
                }
            }
            notifyHeightChanged();
        }

        protected void onDraw(Canvas canvas) {
            PhotoViewer.getInstance().onDraw(canvas);
        }
    }

    public static PhotoViewer getInstance() {
        PhotoViewer localInstance = Instance;
        if (localInstance == null) {
            synchronized (PhotoViewer.class) {
                try {
                    localInstance = Instance;
                    if (localInstance == null) {
                        PhotoViewer localInstance2 = new PhotoViewer();
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

    public void didReceivedNotification(int id, Object... args) {
        String location;
        int a;
        if (id == NotificationCenter.FileDidFailedLoad) {
            location = args[0];
            a = 0;
            while (a < 3) {
                if (this.currentFileNames[a] == null || !this.currentFileNames[a].equals(location)) {
                    a++;
                } else {
                    this.radialProgressViews[a].setProgress(1.0f, true);
                    checkProgress(a, true);
                    return;
                }
            }
        } else if (id == NotificationCenter.FileDidLoaded) {
            location = (String) args[0];
            a = 0;
            while (a < 3) {
                if (this.currentFileNames[a] == null || !this.currentFileNames[a].equals(location)) {
                    a++;
                } else {
                    this.radialProgressViews[a].setProgress(1.0f, true);
                    checkProgress(a, true);
                    if (a == 0) {
                        createGifForCurrentImage();
                        return;
                    }
                    return;
                }
            }
        } else if (id == NotificationCenter.FileLoadProgressChanged) {
            location = (String) args[0];
            a = 0;
            while (a < 3) {
                if (this.currentFileNames[a] != null && this.currentFileNames[a].equals(location)) {
                    this.radialProgressViews[a].setProgress(args[1].floatValue(), true);
                }
                a++;
            }
        } else if (id == NotificationCenter.userPhotosLoaded) {
            guid = ((Integer) args[4]).intValue();
            if (this.avatarsUserId == ((Integer) args[0]).intValue() && this.classGuid == guid) {
                boolean fromCache = ((Boolean) args[3]).booleanValue();
                int setToImage = -1;
                ArrayList<Photo> photos = args[5];
                if (!photos.isEmpty()) {
                    this.imagesArrLocations.clear();
                    this.imagesArrLocationsSizes.clear();
                    this.avatarsArr.clear();
                    r17 = photos.iterator();
                    while (r17.hasNext()) {
                        Photo photo = (Photo) r17.next();
                        if (!(photo == null || (photo instanceof TL_photoEmpty) || photo.sizes == null)) {
                            PhotoSize sizeFull = FileLoader.getClosestPhotoSizeWithSize(photo.sizes, 640);
                            if (sizeFull != null) {
                                if (this.currentFileLocation != null) {
                                    Iterator i$ = photo.sizes.iterator();
                                    while (i$.hasNext()) {
                                        PhotoSize size = (PhotoSize) i$.next();
                                        if (size.location.local_id == this.currentFileLocation.local_id && size.location.volume_id == this.currentFileLocation.volume_id) {
                                            setToImage = this.imagesArrLocations.size();
                                            break;
                                        }
                                    }
                                }
                                this.imagesArrLocations.add(sizeFull.location);
                                this.imagesArrLocationsSizes.add(Integer.valueOf(sizeFull.size));
                                this.avatarsArr.add(photo);
                            }
                        }
                    }
                    if (this.avatarsArr.isEmpty()) {
                        this.menuItem.hideSubItem(6);
                    } else {
                        this.menuItem.showSubItem(6);
                    }
                    this.needSearchImageInArr = false;
                    this.currentIndex = -1;
                    if (setToImage != -1) {
                        setImageIndex(setToImage, true);
                    } else {
                        this.avatarsArr.add(0, new TL_photoEmpty());
                        this.imagesArrLocations.add(0, this.currentFileLocation);
                        this.imagesArrLocationsSizes.add(0, Integer.valueOf(0));
                        setImageIndex(0, true);
                    }
                    if (fromCache) {
                        MessagesController.getInstance().loadUserPhotos(this.avatarsUserId, 0, 80, 0, false, this.classGuid);
                    }
                }
            }
        } else if (id == NotificationCenter.mediaCountDidLoaded) {
            uid = ((Long) args[0]).longValue();
            if (uid == this.currentDialogId || uid == this.mergeDialogId) {
                if (uid == this.currentDialogId) {
                    this.totalImagesCount = ((Integer) args[1]).intValue();
                } else if (uid == this.mergeDialogId) {
                    this.totalImagesCountMerge = ((Integer) args[1]).intValue();
                }
                if (this.needSearchImageInArr && this.isFirstLoading) {
                    this.isFirstLoading = false;
                    this.loadingMoreImages = true;
                    SharedMediaQuery.loadMedia(this.currentDialogId, 0, 80, 0, 0, true, this.classGuid);
                } else if (!this.imagesArr.isEmpty()) {
                    if (this.opennedFromMedia) {
                        this.actionBar.setTitle(LocaleController.formatString("Of", C0553R.string.Of, Integer.valueOf(this.currentIndex + 1), Integer.valueOf(this.totalImagesCount + this.totalImagesCountMerge)));
                    } else {
                        this.actionBar.setTitle(LocaleController.formatString("Of", C0553R.string.Of, Integer.valueOf((((this.totalImagesCount + this.totalImagesCountMerge) - this.imagesArr.size()) + this.currentIndex) + 1), Integer.valueOf(this.totalImagesCount + this.totalImagesCountMerge)));
                    }
                }
            }
        } else if (id == NotificationCenter.mediaDidLoaded) {
            uid = ((Long) args[0]).longValue();
            guid = ((Integer) args[3]).intValue();
            if ((uid == this.currentDialogId || uid == this.mergeDialogId) && guid == this.classGuid) {
                this.loadingMoreImages = false;
                int loadIndex = uid == this.currentDialogId ? 0 : 1;
                ArrayList<MessageObject> arr = args[2];
                this.endReached[loadIndex] = ((Boolean) args[5]).booleanValue();
                int added;
                MessageObject message;
                if (!this.needSearchImageInArr) {
                    added = 0;
                    r17 = arr.iterator();
                    while (r17.hasNext()) {
                        message = (MessageObject) r17.next();
                        if (!this.imagesByIds[loadIndex].containsKey(Integer.valueOf(message.getId()))) {
                            added++;
                            if (this.opennedFromMedia) {
                                this.imagesArr.add(message);
                            } else {
                                this.imagesArr.add(0, message);
                            }
                            this.imagesByIds[loadIndex].put(Integer.valueOf(message.getId()), message);
                        }
                    }
                    if (this.opennedFromMedia) {
                        if (added == 0) {
                            this.totalImagesCount = this.imagesArr.size();
                            this.totalImagesCountMerge = 0;
                        }
                    } else if (added != 0) {
                        int index = this.currentIndex;
                        this.currentIndex = -1;
                        setImageIndex(index + added, true);
                    } else {
                        this.totalImagesCount = this.imagesArr.size();
                        this.totalImagesCountMerge = 0;
                    }
                } else if (!arr.isEmpty() || (loadIndex == 0 && this.mergeDialogId != 0)) {
                    int foundIndex = -1;
                    MessageObject currentMessage = (MessageObject) this.imagesArr.get(this.currentIndex);
                    added = 0;
                    for (a = 0; a < arr.size(); a++) {
                        message = (MessageObject) arr.get(a);
                        if (!this.imagesByIdsTemp[loadIndex].containsKey(Integer.valueOf(message.getId()))) {
                            this.imagesByIdsTemp[loadIndex].put(Integer.valueOf(message.getId()), message);
                            if (this.opennedFromMedia) {
                                this.imagesArrTemp.add(message);
                                if (message.getId() == currentMessage.getId()) {
                                    foundIndex = added;
                                }
                                added++;
                            } else {
                                added++;
                                this.imagesArrTemp.add(0, message);
                                if (message.getId() == currentMessage.getId()) {
                                    foundIndex = arr.size() - added;
                                }
                            }
                        }
                    }
                    if (added == 0 && (loadIndex != 0 || this.mergeDialogId == 0)) {
                        this.totalImagesCount = this.imagesArr.size();
                        this.totalImagesCountMerge = 0;
                    }
                    if (foundIndex != -1) {
                        this.imagesArr.clear();
                        this.imagesArr.addAll(this.imagesArrTemp);
                        for (a = 0; a < 2; a++) {
                            this.imagesByIds[a].clear();
                            this.imagesByIds[a].putAll(this.imagesByIdsTemp[a]);
                            this.imagesByIdsTemp[a].clear();
                        }
                        this.imagesArrTemp.clear();
                        this.needSearchImageInArr = false;
                        this.currentIndex = -1;
                        if (foundIndex >= this.imagesArr.size()) {
                            foundIndex = this.imagesArr.size() - 1;
                        }
                        setImageIndex(foundIndex, true);
                        return;
                    }
                    int loadFromMaxId;
                    if (this.opennedFromMedia) {
                        loadFromMaxId = this.imagesArrTemp.isEmpty() ? 0 : ((MessageObject) this.imagesArrTemp.get(this.imagesArrTemp.size() - 1)).getId();
                        if (loadIndex == 0 && this.endReached[loadIndex] && this.mergeDialogId != 0) {
                            loadIndex = 1;
                            if (!(this.imagesArrTemp.isEmpty() || ((MessageObject) this.imagesArrTemp.get(this.imagesArrTemp.size() - 1)).getDialogId() == this.mergeDialogId)) {
                                loadFromMaxId = 0;
                            }
                        }
                    } else {
                        if (this.imagesArrTemp.isEmpty()) {
                            loadFromMaxId = 0;
                        } else {
                            loadFromMaxId = ((MessageObject) this.imagesArrTemp.get(0)).getId();
                        }
                        if (loadIndex == 0 && this.endReached[loadIndex] && this.mergeDialogId != 0) {
                            loadIndex = 1;
                            if (!(this.imagesArrTemp.isEmpty() || ((MessageObject) this.imagesArrTemp.get(0)).getDialogId() == this.mergeDialogId)) {
                                loadFromMaxId = 0;
                            }
                        }
                    }
                    if (!this.endReached[loadIndex]) {
                        this.loadingMoreImages = true;
                        if (this.opennedFromMedia) {
                            long j;
                            if (loadIndex == 0) {
                                j = this.currentDialogId;
                            } else {
                                j = this.mergeDialogId;
                            }
                            SharedMediaQuery.loadMedia(j, 0, 80, loadFromMaxId, 0, true, this.classGuid);
                            return;
                        }
                        SharedMediaQuery.loadMedia(loadIndex == 0 ? this.currentDialogId : this.mergeDialogId, 0, 80, loadFromMaxId, 0, true, this.classGuid);
                    }
                } else {
                    this.needSearchImageInArr = false;
                }
            }
        } else if (id == NotificationCenter.emojiDidLoaded && this.captionTextView != null) {
            this.captionTextView.invalidate();
        }
    }

    public void setParentActivity(Activity activity) {
        if (this.parentActivity != activity) {
            this.parentActivity = activity;
            if (progressDrawables == null) {
                progressDrawables = new Drawable[4];
                progressDrawables[0] = this.parentActivity.getResources().getDrawable(C0553R.drawable.circle_big);
                progressDrawables[1] = this.parentActivity.getResources().getDrawable(C0553R.drawable.cancel_big);
                progressDrawables[2] = this.parentActivity.getResources().getDrawable(C0553R.drawable.load_big);
                progressDrawables[3] = this.parentActivity.getResources().getDrawable(C0553R.drawable.play_big);
            }
            this.scroller = new Scroller(activity);
            this.windowView = new FrameLayoutTouchListener(activity) {
                public boolean dispatchKeyEventPreIme(KeyEvent event) {
                    if (event == null || event.getKeyCode() != 4 || event.getAction() != 1) {
                        return super.dispatchKeyEventPreIme(event);
                    }
                    if (PhotoViewer.this.captionEditText.isPopupShowing() || PhotoViewer.this.captionEditText.isKeyboardVisible()) {
                        PhotoViewer.this.closeCaptionEnter(false);
                        return false;
                    }
                    PhotoViewer.getInstance().closePhoto(true, false);
                    return true;
                }
            };
            this.windowView.setBackgroundDrawable(this.backgroundDrawable);
            this.windowView.setFocusable(false);
            this.animatingImageView = new ClippingImageView(activity);
            this.animatingImageView.setAnimationValues(this.animationValues);
            this.windowView.addView(this.animatingImageView, LayoutHelper.createFrame(40, 40.0f));
            this.containerView = new FrameLayoutDrawer(activity);
            this.containerView.setFocusable(false);
            this.windowView.addView(this.containerView, LayoutHelper.createFrame(-1, -1, 51));
            this.windowLayoutParams = new LayoutParams();
            this.windowLayoutParams.height = -1;
            this.windowLayoutParams.format = -3;
            this.windowLayoutParams.width = -1;
            this.windowLayoutParams.gravity = 48;
            this.windowLayoutParams.type = 99;
            this.windowLayoutParams.flags = 8;
            this.actionBar = new ActionBar(activity);
            this.actionBar.setBackgroundColor(2130706432);
            this.actionBar.setOccupyStatusBar(false);
            this.actionBar.setItemsBackground(C0553R.drawable.bar_selector_white);
            this.actionBar.setBackButtonImage(C0553R.drawable.ic_ab_back);
            this.actionBar.setTitle(LocaleController.formatString("Of", C0553R.string.Of, Integer.valueOf(1), Integer.valueOf(1)));
            this.containerView.addView(this.actionBar, LayoutHelper.createFrame(-1, -2.0f));
            this.actionBar.setActionBarMenuOnItemClick(new C16462());
            ActionBarMenu menu = this.actionBar.createMenu();
            this.menuItem = menu.addItem(0, (int) C0553R.drawable.ic_ab_other);
            this.menuItem.addSubItem(2, LocaleController.getString("ShowAllMedia", C0553R.string.ShowAllMedia), 0);
            this.menuItem.addSubItem(1, LocaleController.getString("SaveToGallery", C0553R.string.SaveToGallery), 0);
            this.menuItem.addSubItem(6, LocaleController.getString("Delete", C0553R.string.Delete), 0);
            this.captionDoneItem = menu.addItemWithWidth(9, C0553R.drawable.ic_done, AndroidUtilities.dp(56.0f));
            this.captionItem = menu.addItemWithWidth(8, C0553R.drawable.photo_text, AndroidUtilities.dp(56.0f));
            this.cropItem = menu.addItemWithWidth(4, C0553R.drawable.photo_crop, AndroidUtilities.dp(56.0f));
            this.tuneItem = menu.addItemWithWidth(7, C0553R.drawable.photo_tools, AndroidUtilities.dp(56.0f));
            this.bottomLayout = new FrameLayout(this.parentActivity);
            this.bottomLayout.setBackgroundColor(2130706432);
            this.containerView.addView(this.bottomLayout, LayoutHelper.createFrame(-1, 48, 83));
            this.captionTextViewOld = new TextView(this.parentActivity);
            this.captionTextViewOld.setMaxLines(10);
            this.captionTextViewOld.setBackgroundColor(2130706432);
            this.captionTextViewOld.setPadding(AndroidUtilities.dp(16.0f), AndroidUtilities.dp(8.0f), AndroidUtilities.dp(16.0f), AndroidUtilities.dp(8.0f));
            this.captionTextViewOld.setLinkTextColor(-1);
            this.captionTextViewOld.setTextColor(-1);
            this.captionTextViewOld.setGravity(19);
            this.captionTextViewOld.setTextSize(1, 16.0f);
            this.captionTextViewOld.setVisibility(4);
            this.containerView.addView(this.captionTextViewOld, LayoutHelper.createFrame(-1, -2.0f, 83, 0.0f, 0.0f, 0.0f, 48.0f));
            TextView textView = new TextView(this.parentActivity);
            this.captionTextViewNew = textView;
            this.captionTextView = textView;
            this.captionTextViewNew.setMaxLines(10);
            this.captionTextViewNew.setBackgroundColor(2130706432);
            this.captionTextViewNew.setPadding(AndroidUtilities.dp(16.0f), AndroidUtilities.dp(8.0f), AndroidUtilities.dp(16.0f), AndroidUtilities.dp(8.0f));
            this.captionTextViewNew.setLinkTextColor(-1);
            this.captionTextViewNew.setTextColor(-1);
            this.captionTextViewNew.setGravity(19);
            this.captionTextViewNew.setTextSize(1, 16.0f);
            this.captionTextViewNew.setVisibility(4);
            this.containerView.addView(this.captionTextViewNew, LayoutHelper.createFrame(-1, -2.0f, 83, 0.0f, 0.0f, 0.0f, 48.0f));
            this.radialProgressViews[0] = new RadialProgressView(this.containerView.getContext(), this.containerView);
            this.radialProgressViews[0].setBackgroundState(0, false);
            this.radialProgressViews[1] = new RadialProgressView(this.containerView.getContext(), this.containerView);
            this.radialProgressViews[1].setBackgroundState(0, false);
            this.radialProgressViews[2] = new RadialProgressView(this.containerView.getContext(), this.containerView);
            this.radialProgressViews[2].setBackgroundState(0, false);
            this.shareButton = new ImageView(this.containerView.getContext());
            this.shareButton.setImageResource(C0553R.drawable.share);
            this.shareButton.setScaleType(ScaleType.CENTER);
            this.shareButton.setBackgroundResource(C0553R.drawable.bar_selector_white);
            this.bottomLayout.addView(this.shareButton, LayoutHelper.createFrame(50, -1, 53));
            this.shareButton.setOnClickListener(new C11503());
            this.nameTextView = new TextView(this.containerView.getContext());
            this.nameTextView.setTextSize(1, 14.0f);
            this.nameTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            this.nameTextView.setSingleLine(true);
            this.nameTextView.setMaxLines(1);
            this.nameTextView.setEllipsize(TruncateAt.END);
            this.nameTextView.setTextColor(-1);
            this.nameTextView.setGravity(3);
            this.bottomLayout.addView(this.nameTextView, LayoutHelper.createFrame(-1, -2.0f, 51, 16.0f, 5.0f, BitmapDescriptorFactory.HUE_YELLOW, 0.0f));
            this.dateTextView = new TextView(this.containerView.getContext());
            this.dateTextView.setTextSize(1, 13.0f);
            this.dateTextView.setSingleLine(true);
            this.dateTextView.setMaxLines(1);
            this.dateTextView.setEllipsize(TruncateAt.END);
            this.dateTextView.setTextColor(-1);
            this.dateTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            this.dateTextView.setGravity(3);
            this.bottomLayout.addView(this.dateTextView, LayoutHelper.createFrame(-1, -2.0f, 51, 16.0f, 25.0f, 50.0f, 0.0f));
            this.pickerView = new PickerBottomLayout(this.parentActivity);
            this.pickerView.setBackgroundColor(2130706432);
            this.containerView.addView(this.pickerView, LayoutHelper.createFrame(-1, 48, 83));
            this.pickerView.cancelButton.setOnClickListener(new C11514());
            this.pickerView.doneButton.setOnClickListener(new C11525());
            this.editorDoneLayout = new PickerBottomLayout(this.parentActivity);
            this.editorDoneLayout.setBackgroundColor(2130706432);
            this.editorDoneLayout.updateSelectedCount(0, false);
            this.editorDoneLayout.setVisibility(8);
            this.containerView.addView(this.editorDoneLayout, LayoutHelper.createFrame(-1, 48, 83));
            this.editorDoneLayout.cancelButton.setOnClickListener(new C11536());
            this.editorDoneLayout.doneButton.setOnClickListener(new C11547());
            this.gestureDetector = new GestureDetector(this.containerView.getContext(), this);
            this.gestureDetector.setOnDoubleTapListener(this);
            this.centerImage.setParentView(this.containerView);
            this.centerImage.setCrossfadeAlpha((byte) 2);
            this.centerImage.setInvalidateAll(true);
            this.leftImage.setParentView(this.containerView);
            this.leftImage.setCrossfadeAlpha((byte) 2);
            this.leftImage.setInvalidateAll(true);
            this.rightImage.setParentView(this.containerView);
            this.rightImage.setCrossfadeAlpha((byte) 2);
            this.rightImage.setInvalidateAll(true);
            int rotation = ((WindowManager) ApplicationLoader.applicationContext.getSystemService("window")).getDefaultDisplay().getRotation();
            this.checkImageView = new CheckBox(this.containerView.getContext(), C0553R.drawable.selectphoto_large);
            this.checkImageView.setDrawBackground(true);
            this.checkImageView.setSize(45);
            this.checkImageView.setCheckOffset(AndroidUtilities.dp(1.0f));
            this.checkImageView.setColor(-12793105);
            this.checkImageView.setVisibility(8);
            FrameLayoutDrawer frameLayoutDrawer = this.containerView;
            View view = this.checkImageView;
            float f = (rotation == 3 || rotation == 1) ? 58.0f : 68.0f;
            frameLayoutDrawer.addView(view, LayoutHelper.createFrame(45, 45.0f, 53, 0.0f, f, 10.0f, 0.0f));
            this.checkImageView.setOnClickListener(new C11558());
            this.captionEditText = new PhotoViewerCaptionEnterView(this.parentActivity, this.containerView);
            this.captionEditText.setDelegate(new C16479());
            this.containerView.addView(this.captionEditText, LayoutHelper.createFrame(-1, -2.0f, 83, 0.0f, 0.0f, 0.0f, -400.0f));
            this.mentionListView = new ListView(this.parentActivity);
            this.mentionListView.setBackgroundColor(2130706432);
            this.mentionListView.setVisibility(8);
            this.mentionListView.setClipToPadding(true);
            this.mentionListView.setDividerHeight(0);
            this.mentionListView.setDivider(null);
            if (VERSION.SDK_INT > 8) {
                this.mentionListView.setOverScrollMode(2);
            }
            this.containerView.addView(this.mentionListView, LayoutHelper.createFrame(-1, 110, 83));
            ListView listView = this.mentionListView;
            ListAdapter mentionsAdapter = new MentionsAdapter(this.parentActivity, true, new MentionsAdapterDelegate() {

                class C16401 extends AnimatorListenerAdapterProxy {
                    C16401() {
                    }

                    public void onAnimationEnd(Object animation) {
                        if (PhotoViewer.this.mentionListAnimation != null && PhotoViewer.this.mentionListAnimation.equals(animation)) {
                            PhotoViewer.this.mentionListView.clearAnimation();
                            PhotoViewer.this.mentionListAnimation = null;
                        }
                    }
                }

                class C16412 extends AnimatorListenerAdapterProxy {
                    C16412() {
                    }

                    public void onAnimationEnd(Object animation) {
                        if (PhotoViewer.this.mentionListAnimation != null && PhotoViewer.this.mentionListAnimation.equals(animation)) {
                            PhotoViewer.this.mentionListView.clearAnimation();
                            PhotoViewer.this.mentionListView.setVisibility(8);
                            PhotoViewer.this.mentionListAnimation = null;
                        }
                    }
                }

                public void needChangePanelVisibility(boolean show) {
                    if (show) {
                        int i;
                        FrameLayout.LayoutParams layoutParams3 = (FrameLayout.LayoutParams) PhotoViewer.this.mentionListView.getLayoutParams();
                        int min = Math.min(3, PhotoViewer.this.mentionsAdapter.getCount()) * 36;
                        if (PhotoViewer.this.mentionsAdapter.getCount() > 3) {
                            i = 18;
                        } else {
                            i = 0;
                        }
                        int height = min + i;
                        layoutParams3.height = AndroidUtilities.dp((float) height);
                        layoutParams3.topMargin = -AndroidUtilities.dp((float) height);
                        PhotoViewer.this.mentionListView.setLayoutParams(layoutParams3);
                        if (PhotoViewer.this.mentionListAnimation != null) {
                            PhotoViewer.this.mentionListAnimation.cancel();
                            PhotoViewer.this.mentionListAnimation = null;
                        }
                        if (PhotoViewer.this.mentionListView.getVisibility() == 0) {
                            ViewProxy.setAlpha(PhotoViewer.this.mentionListView, 1.0f);
                            return;
                        } else if (PhotoViewer.this.allowMentions) {
                            PhotoViewer.this.mentionListView.setVisibility(0);
                            PhotoViewer.this.mentionListAnimation = new AnimatorSetProxy();
                            PhotoViewer.this.mentionListAnimation.playTogether(ObjectAnimatorProxy.ofFloat(PhotoViewer.this.mentionListView, "alpha", 0.0f, 1.0f));
                            PhotoViewer.this.mentionListAnimation.addListener(new C16401());
                            PhotoViewer.this.mentionListAnimation.setDuration(200);
                            PhotoViewer.this.mentionListAnimation.start();
                            return;
                        } else {
                            ViewProxy.setAlpha(PhotoViewer.this.mentionListView, 1.0f);
                            PhotoViewer.this.mentionListView.clearAnimation();
                            PhotoViewer.this.mentionListView.setVisibility(4);
                            return;
                        }
                    }
                    if (PhotoViewer.this.mentionListAnimation != null) {
                        PhotoViewer.this.mentionListAnimation.cancel();
                        PhotoViewer.this.mentionListAnimation = null;
                    }
                    if (PhotoViewer.this.mentionListView.getVisibility() == 8) {
                        return;
                    }
                    if (PhotoViewer.this.allowMentions) {
                        PhotoViewer.this.mentionListAnimation = new AnimatorSetProxy();
                        AnimatorSetProxy access$5100 = PhotoViewer.this.mentionListAnimation;
                        Object[] objArr = new Object[1];
                        objArr[0] = ObjectAnimatorProxy.ofFloat(PhotoViewer.this.mentionListView, "alpha", 0.0f);
                        access$5100.playTogether(objArr);
                        PhotoViewer.this.mentionListAnimation.addListener(new C16412());
                        PhotoViewer.this.mentionListAnimation.setDuration(200);
                        PhotoViewer.this.mentionListAnimation.start();
                        return;
                    }
                    PhotoViewer.this.mentionListView.clearAnimation();
                    PhotoViewer.this.mentionListView.setVisibility(8);
                }
            });
            this.mentionsAdapter = mentionsAdapter;
            listView.setAdapter(mentionsAdapter);
            this.mentionListView.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    User object = PhotoViewer.this.mentionsAdapter.getItem(position);
                    int start = PhotoViewer.this.mentionsAdapter.getResultStartPosition();
                    int len = PhotoViewer.this.mentionsAdapter.getResultLength();
                    if (object instanceof User) {
                        User user = object;
                        if (user != null) {
                            PhotoViewer.this.captionEditText.replaceWithText(start, len, "@" + user.username + " ");
                        }
                    } else if (object instanceof String) {
                        PhotoViewer.this.captionEditText.replaceWithText(start, len, object + " ");
                    }
                }
            });
            this.mentionListView.setOnItemLongClickListener(new OnItemLongClickListener() {

                class C11451 implements DialogInterface.OnClickListener {
                    C11451() {
                    }

                    public void onClick(DialogInterface dialogInterface, int i) {
                        PhotoViewer.this.mentionsAdapter.clearRecentHashtags();
                    }
                }

                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                    if (!(PhotoViewer.this.mentionsAdapter.getItem(position) instanceof String)) {
                        return false;
                    }
                    Builder builder = new Builder(PhotoViewer.this.parentActivity);
                    builder.setTitle(LocaleController.getString("AppName", C0553R.string.AppName));
                    builder.setMessage(LocaleController.getString("ClearSearch", C0553R.string.ClearSearch));
                    builder.setPositiveButton(LocaleController.getString("ClearButton", C0553R.string.ClearButton).toUpperCase(), new C11451());
                    builder.setNegativeButton(LocaleController.getString("Cancel", C0553R.string.Cancel), null);
                    PhotoViewer.this.showAlertDialog(builder);
                    return true;
                }
            });
        }
    }

    private void updateCaptionTextForCurrentPhoto(Object object) {
        CharSequence caption = null;
        if (object instanceof PhotoEntry) {
            caption = ((PhotoEntry) object).caption;
        } else if (object instanceof SearchImage) {
            caption = ((SearchImage) object).caption;
        }
        if (caption == null || caption.length() == 0) {
            this.captionEditText.setFieldText("");
        } else {
            this.captionEditText.setFieldText(caption);
        }
    }

    private void closeCaptionEnter(boolean apply) {
        Object object = this.imagesArrLocals.get(this.currentIndex);
        if (apply) {
            if (object instanceof PhotoEntry) {
                ((PhotoEntry) object).caption = this.captionEditText.getFieldCharSequence();
            } else if (object instanceof SearchImage) {
                ((SearchImage) object).caption = this.captionEditText.getFieldCharSequence();
            }
            if (!(this.captionEditText.getFieldCharSequence().length() == 0 || this.placeProvider.isPhotoChecked(this.currentIndex))) {
                this.placeProvider.setPhotoChecked(this.currentIndex);
                this.checkImageView.setChecked(this.placeProvider.isPhotoChecked(this.currentIndex), true);
                updateSelectedCount();
            }
        }
        this.cropItem.setVisibility(0);
        this.captionItem.setVisibility(0);
        if (VERSION.SDK_INT >= 16) {
            this.tuneItem.setVisibility(0);
        }
        if (this.sendPhotoType == 0) {
            this.checkImageView.setVisibility(0);
        }
        this.captionDoneItem.setVisibility(8);
        this.pickerView.setVisibility(0);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.captionEditText.getLayoutParams();
        layoutParams.bottomMargin = -AndroidUtilities.dp(400.0f);
        this.captionEditText.setLayoutParams(layoutParams);
        layoutParams = (FrameLayout.LayoutParams) this.mentionListView.getLayoutParams();
        layoutParams.bottomMargin = -AndroidUtilities.dp(400.0f);
        this.mentionListView.setLayoutParams(layoutParams);
        if (this.lastTitle != null) {
            this.actionBar.setTitle(this.lastTitle);
            this.lastTitle = null;
        }
        updateCaptionTextForCurrentPhoto(object);
        setCurrentCaption(this.captionEditText.getFieldCharSequence());
        if (this.captionEditText.isPopupShowing()) {
            this.captionEditText.hidePopup();
        } else {
            this.captionEditText.closeKeyboard();
        }
    }

    private void showAlertDialog(Builder builder) {
        if (this.parentActivity != null) {
            try {
                if (this.visibleDialog != null) {
                    this.visibleDialog.dismiss();
                    this.visibleDialog = null;
                }
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
            try {
                this.visibleDialog = builder.show();
                this.visibleDialog.setCanceledOnTouchOutside(true);
                this.visibleDialog.setOnDismissListener(new OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                        PhotoViewer.this.visibleDialog = null;
                    }
                });
            } catch (Throwable e2) {
                FileLog.m611e("tmessages", e2);
            }
        }
    }

    private void applyCurrentEditMode() {
        Bitmap bitmap = null;
        if (this.currentEditMode == 1) {
            bitmap = this.photoCropView.getBitmap();
        } else if (this.currentEditMode == 2) {
            bitmap = this.photoFilterView.getBitmap();
        }
        if (bitmap != null) {
            PhotoSize size = ImageLoader.scaleAndSaveImage(bitmap, (float) AndroidUtilities.getPhotoSize(), (float) AndroidUtilities.getPhotoSize(), 80, false, 101, 101);
            if (size != null) {
                PhotoEntry object = this.imagesArrLocals.get(this.currentIndex);
                if (object instanceof PhotoEntry) {
                    PhotoEntry entry = object;
                    entry.imagePath = FileLoader.getPathToAttach(size, true).toString();
                    size = ImageLoader.scaleAndSaveImage(bitmap, (float) AndroidUtilities.dp(BitmapDescriptorFactory.HUE_GREEN), (float) AndroidUtilities.dp(BitmapDescriptorFactory.HUE_GREEN), 70, false, 101, 101);
                    if (size != null) {
                        entry.thumbPath = FileLoader.getPathToAttach(size, true).toString();
                    }
                } else if (object instanceof SearchImage) {
                    SearchImage entry2 = (SearchImage) object;
                    entry2.imagePath = FileLoader.getPathToAttach(size, true).toString();
                    size = ImageLoader.scaleAndSaveImage(bitmap, (float) AndroidUtilities.dp(BitmapDescriptorFactory.HUE_GREEN), (float) AndroidUtilities.dp(BitmapDescriptorFactory.HUE_GREEN), 70, false, 101, 101);
                    if (size != null) {
                        entry2.thumbPath = FileLoader.getPathToAttach(size, true).toString();
                    }
                }
                if (this.sendPhotoType == 0 && this.placeProvider != null) {
                    this.placeProvider.updatePhotoAtIndex(this.currentIndex);
                    if (!this.placeProvider.isPhotoChecked(this.currentIndex)) {
                        this.placeProvider.setPhotoChecked(this.currentIndex);
                        this.checkImageView.setChecked(this.placeProvider.isPhotoChecked(this.currentIndex), true);
                        updateSelectedCount();
                    }
                }
                if (this.currentEditMode == 1) {
                    float scaleX = this.photoCropView.getRectSizeX() / ((float) getContainerViewWidth());
                    float scaleY = this.photoCropView.getRectSizeY() / ((float) getContainerViewHeight());
                    if (scaleX <= scaleY) {
                        scaleX = scaleY;
                    }
                    this.scale = scaleX;
                    this.translationX = (this.photoCropView.getRectX() + (this.photoCropView.getRectSizeX() / 2.0f)) - ((float) (getContainerViewWidth() / 2));
                    this.translationY = (this.photoCropView.getRectY() + (this.photoCropView.getRectSizeY() / 2.0f)) - ((float) (getContainerViewHeight() / 2));
                    this.zoomAnimation = true;
                }
                this.centerImage.setParentView(null);
                this.centerImage.setOrientation(0, true);
                this.centerImage.setImageBitmap(bitmap);
                this.centerImage.setParentView(this.containerView);
            }
        }
    }

    private void switchToEditMode(int mode) {
        if (this.currentEditMode == mode || this.centerImage.getBitmap() == null || this.changeModeAnimation != null || this.imageMoveAnimation != null || this.radialProgressViews[0].backgroundState != -1) {
            return;
        }
        final int i;
        if (mode == 0) {
            if (this.currentEditMode != 2 || this.photoFilterView.getToolsView().getVisibility() == 0) {
                if (this.centerImage.getBitmap() != null) {
                    float scale;
                    float newScale;
                    int bitmapWidth = this.centerImage.getBitmapWidth();
                    int bitmapHeight = this.centerImage.getBitmapHeight();
                    float scaleX = ((float) getContainerViewWidth()) / ((float) bitmapWidth);
                    float scaleY = ((float) getContainerViewHeight()) / ((float) bitmapHeight);
                    float newScaleX = ((float) getContainerViewWidth(0)) / ((float) bitmapWidth);
                    float newScaleY = ((float) getContainerViewHeight(0)) / ((float) bitmapHeight);
                    if (scaleX > scaleY) {
                        scale = scaleY;
                    } else {
                        scale = scaleX;
                    }
                    if (newScaleX > newScaleY) {
                        newScale = newScaleY;
                    } else {
                        newScale = newScaleX;
                    }
                    this.animateToScale = newScale / scale;
                    this.animateToX = 0.0f;
                    if (this.currentEditMode == 1) {
                        this.animateToY = (float) AndroidUtilities.dp(24.0f);
                    } else if (this.currentEditMode == 2) {
                        this.animateToY = (float) AndroidUtilities.dp(62.0f);
                    }
                    this.animationStartTime = System.currentTimeMillis();
                    this.zoomAnimation = true;
                }
                this.imageMoveAnimation = new AnimatorSetProxy();
                AnimatorSetProxy animatorSetProxy;
                Object[] objArr;
                if (this.currentEditMode == 1) {
                    animatorSetProxy = this.imageMoveAnimation;
                    objArr = new Object[3];
                    objArr[0] = ObjectAnimatorProxy.ofFloat(this.editorDoneLayout, "translationY", (float) AndroidUtilities.dp(48.0f));
                    objArr[1] = ObjectAnimatorProxy.ofFloat(this, "animationValue", 0.0f, 1.0f);
                    objArr[2] = ObjectAnimatorProxy.ofFloat(this.photoCropView, "alpha", 0.0f);
                    animatorSetProxy.playTogether(objArr);
                } else if (this.currentEditMode == 2) {
                    this.photoFilterView.shutdown();
                    animatorSetProxy = this.imageMoveAnimation;
                    objArr = new Object[2];
                    objArr[0] = ObjectAnimatorProxy.ofFloat(this.photoFilterView.getToolsView(), "translationY", (float) AndroidUtilities.dp(126.0f));
                    objArr[1] = ObjectAnimatorProxy.ofFloat(this, "animationValue", 0.0f, 1.0f);
                    animatorSetProxy.playTogether(objArr);
                }
                this.imageMoveAnimation.setDuration(200);
                i = mode;
                this.imageMoveAnimation.addListener(new AnimatorListenerAdapterProxy() {

                    class C16421 extends AnimatorListenerAdapterProxy {
                        C16421() {
                        }

                        public void onAnimationStart(Object animation) {
                            PhotoViewer.this.pickerView.setVisibility(0);
                            PhotoViewer.this.actionBar.setVisibility(0);
                            if (PhotoViewer.this.needCaptionLayout) {
                                PhotoViewer.this.captionTextView.setVisibility(PhotoViewer.this.captionTextView.getTag() != null ? 0 : 4);
                            }
                            if (PhotoViewer.this.sendPhotoType == 0) {
                                PhotoViewer.this.checkImageView.setVisibility(0);
                            }
                        }

                        public void onAnimationEnd(Object animation) {
                            PhotoViewer.this.pickerView.clearAnimation();
                            PhotoViewer.this.actionBar.clearAnimation();
                            if (PhotoViewer.this.needCaptionLayout) {
                                PhotoViewer.this.captionTextView.clearAnimation();
                            }
                            if (PhotoViewer.this.sendPhotoType == 0) {
                                PhotoViewer.this.checkImageView.clearAnimation();
                            }
                        }
                    }

                    public void onAnimationEnd(Object animation) {
                        if (PhotoViewer.this.currentEditMode == 1) {
                            PhotoViewer.this.photoCropView.clearAnimation();
                            PhotoViewer.this.editorDoneLayout.clearAnimation();
                            PhotoViewer.this.editorDoneLayout.setVisibility(8);
                            PhotoViewer.this.photoCropView.setVisibility(8);
                        } else if (PhotoViewer.this.currentEditMode == 2) {
                            PhotoViewer.this.photoFilterView.getToolsView().clearAnimation();
                            PhotoViewer.this.containerView.removeView(PhotoViewer.this.photoFilterView);
                            PhotoViewer.this.photoFilterView = null;
                        }
                        PhotoViewer.this.imageMoveAnimation = null;
                        PhotoViewer.this.currentEditMode = i;
                        PhotoViewer.this.animateToScale = 1.0f;
                        PhotoViewer.this.animateToX = 0.0f;
                        PhotoViewer.this.animateToY = 0.0f;
                        PhotoViewer.this.scale = 1.0f;
                        PhotoViewer.this.updateMinMax(PhotoViewer.this.scale);
                        PhotoViewer.this.containerView.invalidate();
                        AnimatorSetProxy animatorSet = new AnimatorSetProxy();
                        ArrayList arrayList = new ArrayList();
                        arrayList.add(ObjectAnimatorProxy.ofFloat(PhotoViewer.this.pickerView, "translationY", 0.0f));
                        arrayList.add(ObjectAnimatorProxy.ofFloat(PhotoViewer.this.actionBar, "translationY", 0.0f));
                        if (PhotoViewer.this.needCaptionLayout) {
                            arrayList.add(ObjectAnimatorProxy.ofFloat(PhotoViewer.this.captionTextView, "translationY", 0.0f));
                        }
                        if (PhotoViewer.this.sendPhotoType == 0) {
                            arrayList.add(ObjectAnimatorProxy.ofFloat(PhotoViewer.this.checkImageView, "alpha", 1.0f));
                        }
                        animatorSet.playTogether(arrayList);
                        animatorSet.setDuration(200);
                        animatorSet.addListener(new C16421());
                        animatorSet.start();
                    }
                });
                this.imageMoveAnimation.start();
                return;
            }
            this.photoFilterView.switchToOrFromEditMode();
        } else if (mode == 1) {
            if (this.photoCropView == null) {
                this.photoCropView = new PhotoCropView(this.parentActivity);
                this.photoCropView.setVisibility(8);
                FrameLayoutDrawer frameLayoutDrawer = this.containerView;
                frameLayoutDrawer.addView(this.photoCropView, LayoutHelper.createFrame(-1, GroundOverlayOptions.NO_DIMENSION, 51, 0.0f, 0.0f, 0.0f, 48.0f));
                this.photoCropView.setDelegate(new PhotoCropViewDelegate() {
                    public void needMoveImageTo(float x, float y, float s, boolean animated) {
                        if (animated) {
                            PhotoViewer.this.animateTo(s, x, y, true);
                            return;
                        }
                        PhotoViewer.this.translationX = x;
                        PhotoViewer.this.translationY = y;
                        PhotoViewer.this.scale = s;
                        PhotoViewer.this.containerView.invalidate();
                    }
                });
            }
            this.editorDoneLayout.doneButtonTextView.setText(LocaleController.getString("Crop", C0553R.string.Crop));
            this.changeModeAnimation = new AnimatorSetProxy();
            arrayList = new ArrayList();
            arrayList.add(ObjectAnimatorProxy.ofFloat(this.pickerView, "translationY", 0.0f, (float) AndroidUtilities.dp(96.0f)));
            arrayList.add(ObjectAnimatorProxy.ofFloat(this.actionBar, "translationY", 0.0f, (float) (-this.actionBar.getHeight())));
            if (this.needCaptionLayout) {
                arrayList.add(ObjectAnimatorProxy.ofFloat(this.captionTextView, "translationY", 0.0f, (float) AndroidUtilities.dp(96.0f)));
            }
            if (this.sendPhotoType == 0) {
                arrayList.add(ObjectAnimatorProxy.ofFloat(this.checkImageView, "alpha", 1.0f, 0.0f));
            }
            this.changeModeAnimation.playTogether(arrayList);
            this.changeModeAnimation.setDuration(200);
            i = mode;
            this.changeModeAnimation.addListener(new AnimatorListenerAdapterProxy() {

                class C16431 extends AnimatorListenerAdapterProxy {
                    C16431() {
                    }

                    public void onAnimationStart(Object animation) {
                        PhotoViewer.this.editorDoneLayout.setVisibility(0);
                        PhotoViewer.this.photoCropView.setVisibility(0);
                    }

                    public void onAnimationEnd(Object animation) {
                        PhotoViewer.this.imageMoveAnimation = null;
                        PhotoViewer.this.currentEditMode = i;
                        PhotoViewer.this.animateToScale = 1.0f;
                        PhotoViewer.this.animateToX = 0.0f;
                        PhotoViewer.this.animateToY = 0.0f;
                        PhotoViewer.this.scale = 1.0f;
                        PhotoViewer.this.updateMinMax(PhotoViewer.this.scale);
                        PhotoViewer.this.containerView.invalidate();
                        PhotoViewer.this.editorDoneLayout.clearAnimation();
                        PhotoViewer.this.photoCropView.clearAnimation();
                    }
                }

                public void onAnimationEnd(Object animation) {
                    PhotoViewer.this.changeModeAnimation = null;
                    PhotoViewer.this.pickerView.clearAnimation();
                    PhotoViewer.this.actionBar.clearAnimation();
                    PhotoViewer.this.pickerView.setVisibility(8);
                    PhotoViewer.this.actionBar.setVisibility(8);
                    if (PhotoViewer.this.needCaptionLayout) {
                        PhotoViewer.this.captionTextView.clearAnimation();
                        PhotoViewer.this.captionTextView.setVisibility(4);
                    }
                    if (PhotoViewer.this.sendPhotoType == 0) {
                        PhotoViewer.this.checkImageView.clearAnimation();
                        PhotoViewer.this.checkImageView.setVisibility(8);
                    }
                    Bitmap bitmap = PhotoViewer.this.centerImage.getBitmap();
                    if (bitmap != null) {
                        float scale;
                        float newScale;
                        PhotoViewer.this.photoCropView.setBitmap(bitmap, PhotoViewer.this.centerImage.getOrientation(), PhotoViewer.this.sendPhotoType != 1);
                        int bitmapWidth = PhotoViewer.this.centerImage.getBitmapWidth();
                        int bitmapHeight = PhotoViewer.this.centerImage.getBitmapHeight();
                        float scaleX = ((float) PhotoViewer.this.getContainerViewWidth()) / ((float) bitmapWidth);
                        float scaleY = ((float) PhotoViewer.this.getContainerViewHeight()) / ((float) bitmapHeight);
                        float newScaleX = ((float) PhotoViewer.this.getContainerViewWidth(1)) / ((float) bitmapWidth);
                        float newScaleY = ((float) PhotoViewer.this.getContainerViewHeight(1)) / ((float) bitmapHeight);
                        if (scaleX > scaleY) {
                            scale = scaleY;
                        } else {
                            scale = scaleX;
                        }
                        if (newScaleX > newScaleY) {
                            newScale = newScaleY;
                        } else {
                            newScale = newScaleX;
                        }
                        PhotoViewer.this.animateToScale = newScale / scale;
                        PhotoViewer.this.animateToX = 0.0f;
                        PhotoViewer.this.animateToY = (float) (-AndroidUtilities.dp(24.0f));
                        PhotoViewer.this.animationStartTime = System.currentTimeMillis();
                        PhotoViewer.this.zoomAnimation = true;
                    }
                    PhotoViewer.this.imageMoveAnimation = new AnimatorSetProxy();
                    AnimatorSetProxy access$3300 = PhotoViewer.this.imageMoveAnimation;
                    r12 = new Object[3];
                    r12[0] = ObjectAnimatorProxy.ofFloat(PhotoViewer.this.editorDoneLayout, "translationY", (float) AndroidUtilities.dp(48.0f), 0.0f);
                    float[] fArr = new float[2];
                    r12[1] = ObjectAnimatorProxy.ofFloat(PhotoViewer.this, "animationValue", 0.0f, 1.0f);
                    fArr = new float[2];
                    r12[2] = ObjectAnimatorProxy.ofFloat(PhotoViewer.this.photoCropView, "alpha", 0.0f, 1.0f);
                    access$3300.playTogether(r12);
                    PhotoViewer.this.imageMoveAnimation.setDuration(200);
                    PhotoViewer.this.imageMoveAnimation.addListener(new C16431());
                    PhotoViewer.this.imageMoveAnimation.start();
                }
            });
            this.changeModeAnimation.start();
        } else if (mode == 2) {
            if (this.photoFilterView == null) {
                this.photoFilterView = new PhotoFilterView(this.parentActivity, this.centerImage.getBitmap(), this.centerImage.getOrientation());
                this.containerView.addView(this.photoFilterView, LayoutHelper.createFrame(-1, GroundOverlayOptions.NO_DIMENSION));
                this.photoFilterView.getDoneTextView().setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        PhotoViewer.this.applyCurrentEditMode();
                        PhotoViewer.this.switchToEditMode(0);
                    }
                });
                this.photoFilterView.getCancelTextView().setOnClickListener(new OnClickListener() {

                    class C11461 implements DialogInterface.OnClickListener {
                        C11461() {
                        }

                        public void onClick(DialogInterface dialogInterface, int i) {
                            PhotoViewer.this.switchToEditMode(0);
                        }
                    }

                    public void onClick(View v) {
                        if (!PhotoViewer.this.photoFilterView.hasChanges()) {
                            PhotoViewer.this.switchToEditMode(0);
                        } else if (PhotoViewer.this.parentActivity != null) {
                            Builder builder = new Builder(PhotoViewer.this.parentActivity);
                            builder.setMessage(LocaleController.getString("DiscardChanges", C0553R.string.DiscardChanges));
                            builder.setTitle(LocaleController.getString("AppName", C0553R.string.AppName));
                            builder.setPositiveButton(LocaleController.getString("OK", C0553R.string.OK), new C11461());
                            builder.setNegativeButton(LocaleController.getString("Cancel", C0553R.string.Cancel), null);
                            PhotoViewer.this.showAlertDialog(builder);
                        }
                    }
                });
                ViewProxy.setTranslationY(this.photoFilterView.getToolsView(), (float) AndroidUtilities.dp(126.0f));
            }
            this.changeModeAnimation = new AnimatorSetProxy();
            arrayList = new ArrayList();
            arrayList.add(ObjectAnimatorProxy.ofFloat(this.pickerView, "translationY", 0.0f, (float) AndroidUtilities.dp(96.0f)));
            arrayList.add(ObjectAnimatorProxy.ofFloat(this.actionBar, "translationY", 0.0f, (float) (-this.actionBar.getHeight())));
            if (this.needCaptionLayout) {
                arrayList.add(ObjectAnimatorProxy.ofFloat(this.captionTextView, "translationY", 0.0f, (float) AndroidUtilities.dp(96.0f)));
            }
            if (this.sendPhotoType == 0) {
                arrayList.add(ObjectAnimatorProxy.ofFloat(this.checkImageView, "alpha", 1.0f, 0.0f));
            }
            this.changeModeAnimation.playTogether(arrayList);
            this.changeModeAnimation.setDuration(200);
            i = mode;
            this.changeModeAnimation.addListener(new AnimatorListenerAdapterProxy() {

                class C16441 extends AnimatorListenerAdapterProxy {
                    C16441() {
                    }

                    public void onAnimationStart(Object animation) {
                    }

                    public void onAnimationEnd(Object animation) {
                        PhotoViewer.this.photoFilterView.init();
                        PhotoViewer.this.imageMoveAnimation = null;
                        PhotoViewer.this.currentEditMode = i;
                        PhotoViewer.this.animateToScale = 1.0f;
                        PhotoViewer.this.animateToX = 0.0f;
                        PhotoViewer.this.animateToY = 0.0f;
                        PhotoViewer.this.scale = 1.0f;
                        PhotoViewer.this.updateMinMax(PhotoViewer.this.scale);
                        PhotoViewer.this.containerView.invalidate();
                        PhotoViewer.this.photoFilterView.getToolsView().clearAnimation();
                    }
                }

                public void onAnimationEnd(Object animation) {
                    PhotoViewer.this.changeModeAnimation = null;
                    PhotoViewer.this.pickerView.clearAnimation();
                    PhotoViewer.this.actionBar.clearAnimation();
                    PhotoViewer.this.pickerView.setVisibility(8);
                    PhotoViewer.this.actionBar.setVisibility(8);
                    if (PhotoViewer.this.needCaptionLayout) {
                        PhotoViewer.this.captionTextView.clearAnimation();
                        PhotoViewer.this.captionTextView.setVisibility(4);
                    }
                    if (PhotoViewer.this.sendPhotoType == 0) {
                        PhotoViewer.this.checkImageView.clearAnimation();
                        PhotoViewer.this.checkImageView.setVisibility(8);
                    }
                    if (PhotoViewer.this.centerImage.getBitmap() != null) {
                        float scale;
                        float newScale;
                        int bitmapWidth = PhotoViewer.this.centerImage.getBitmapWidth();
                        int bitmapHeight = PhotoViewer.this.centerImage.getBitmapHeight();
                        float scaleX = ((float) PhotoViewer.this.getContainerViewWidth()) / ((float) bitmapWidth);
                        float scaleY = ((float) PhotoViewer.this.getContainerViewHeight()) / ((float) bitmapHeight);
                        float newScaleX = ((float) PhotoViewer.this.getContainerViewWidth(2)) / ((float) bitmapWidth);
                        float newScaleY = ((float) PhotoViewer.this.getContainerViewHeight(2)) / ((float) bitmapHeight);
                        if (scaleX > scaleY) {
                            scale = scaleY;
                        } else {
                            scale = scaleX;
                        }
                        if (newScaleX > newScaleY) {
                            newScale = newScaleY;
                        } else {
                            newScale = newScaleX;
                        }
                        PhotoViewer.this.animateToScale = newScale / scale;
                        PhotoViewer.this.animateToX = 0.0f;
                        PhotoViewer.this.animateToY = (float) (-AndroidUtilities.dp(62.0f));
                        PhotoViewer.this.animationStartTime = System.currentTimeMillis();
                        PhotoViewer.this.zoomAnimation = true;
                    }
                    PhotoViewer.this.imageMoveAnimation = new AnimatorSetProxy();
                    AnimatorSetProxy access$3300 = PhotoViewer.this.imageMoveAnimation;
                    r12 = new Object[2];
                    float[] fArr = new float[2];
                    r12[0] = ObjectAnimatorProxy.ofFloat(PhotoViewer.this, "animationValue", 0.0f, 1.0f);
                    r12[1] = ObjectAnimatorProxy.ofFloat(PhotoViewer.this.photoFilterView.getToolsView(), "translationY", (float) AndroidUtilities.dp(126.0f), 0.0f);
                    access$3300.playTogether(r12);
                    PhotoViewer.this.imageMoveAnimation.setDuration(200);
                    PhotoViewer.this.imageMoveAnimation.addListener(new C16441());
                    PhotoViewer.this.imageMoveAnimation.start();
                }
            });
            this.changeModeAnimation.start();
        }
    }

    private void toggleCheckImageView(boolean show) {
        float f = 1.0f;
        AnimatorSetProxy animatorSet = new AnimatorSetProxy();
        ArrayList arrayList = new ArrayList();
        PickerBottomLayout pickerBottomLayout = this.pickerView;
        String str = "alpha";
        float[] fArr = new float[1];
        fArr[0] = show ? 1.0f : 0.0f;
        arrayList.add(ObjectAnimatorProxy.ofFloat(pickerBottomLayout, str, fArr));
        if (this.needCaptionLayout) {
            float f2;
            TextView textView = this.captionTextView;
            str = "alpha";
            fArr = new float[1];
            if (show) {
                f2 = 1.0f;
            } else {
                f2 = 0.0f;
            }
            fArr[0] = f2;
            arrayList.add(ObjectAnimatorProxy.ofFloat(textView, str, fArr));
        }
        if (this.sendPhotoType == 0) {
            CheckBox checkBox = this.checkImageView;
            String str2 = "alpha";
            float[] fArr2 = new float[1];
            if (!show) {
                f = 0.0f;
            }
            fArr2[0] = f;
            arrayList.add(ObjectAnimatorProxy.ofFloat(checkBox, str2, fArr2));
        }
        animatorSet.playTogether(arrayList);
        animatorSet.setDuration(200);
        animatorSet.start();
    }

    private void toggleActionBar(boolean show, boolean animated) {
        float f = 1.0f;
        if (show) {
            this.actionBar.setVisibility(0);
            if (this.canShowBottom) {
                this.bottomLayout.setVisibility(0);
                if (this.captionTextView.getTag() != null) {
                    this.captionTextView.setVisibility(0);
                }
            }
        }
        this.isActionBarVisible = show;
        this.actionBar.setEnabled(show);
        this.bottomLayout.setEnabled(show);
        float f2;
        if (animated) {
            ArrayList arrayList = new ArrayList();
            ActionBar actionBar = this.actionBar;
            String str = "alpha";
            float[] fArr = new float[1];
            if (show) {
                f2 = 1.0f;
            } else {
                f2 = 0.0f;
            }
            fArr[0] = f2;
            arrayList.add(ObjectAnimatorProxy.ofFloat(actionBar, str, fArr));
            FrameLayout frameLayout = this.bottomLayout;
            str = "alpha";
            fArr = new float[1];
            if (show) {
                f2 = 1.0f;
            } else {
                f2 = 0.0f;
            }
            fArr[0] = f2;
            arrayList.add(ObjectAnimatorProxy.ofFloat(frameLayout, str, fArr));
            if (this.captionTextView.getTag() != null) {
                TextView textView = this.captionTextView;
                String str2 = "alpha";
                float[] fArr2 = new float[1];
                if (!show) {
                    f = 0.0f;
                }
                fArr2[0] = f;
                arrayList.add(ObjectAnimatorProxy.ofFloat(textView, str2, fArr2));
            }
            this.currentActionBarAnimation = new AnimatorSetProxy();
            this.currentActionBarAnimation.playTogether(arrayList);
            if (!show) {
                this.currentActionBarAnimation.addListener(new AnimatorListenerAdapterProxy() {
                    public void onAnimationEnd(Object animation) {
                        if (PhotoViewer.this.currentActionBarAnimation != null && PhotoViewer.this.currentActionBarAnimation.equals(animation)) {
                            PhotoViewer.this.actionBar.setVisibility(8);
                            if (PhotoViewer.this.canShowBottom) {
                                PhotoViewer.this.bottomLayout.clearAnimation();
                                PhotoViewer.this.bottomLayout.setVisibility(8);
                                if (PhotoViewer.this.captionTextView.getTag() != null) {
                                    PhotoViewer.this.captionTextView.clearAnimation();
                                    PhotoViewer.this.captionTextView.setVisibility(4);
                                }
                            }
                            PhotoViewer.this.currentActionBarAnimation = null;
                        }
                    }
                });
            }
            this.currentActionBarAnimation.setDuration(200);
            this.currentActionBarAnimation.start();
            return;
        }
        ViewProxy.setAlpha(this.actionBar, show ? 1.0f : 0.0f);
        View view = this.bottomLayout;
        if (show) {
            f2 = 1.0f;
        } else {
            f2 = 0.0f;
        }
        ViewProxy.setAlpha(view, f2);
        if (this.captionTextView.getTag() != null) {
            View view2 = this.captionTextView;
            if (!show) {
                f = 0.0f;
            }
            ViewProxy.setAlpha(view2, f);
        }
        if (!show) {
            this.actionBar.setVisibility(8);
            if (this.canShowBottom) {
                this.bottomLayout.clearAnimation();
                this.bottomLayout.setVisibility(8);
                if (this.captionTextView.getTag() != null) {
                    this.captionTextView.clearAnimation();
                    this.captionTextView.setVisibility(4);
                }
            }
        }
    }

    private String getFileName(int index) {
        if (index < 0) {
            return null;
        }
        if (!this.imagesArrLocations.isEmpty() || !this.imagesArr.isEmpty()) {
            InputFileLocation file = getInputFileLocation(index);
            if (file == null) {
                return null;
            }
            if (!this.imagesArrLocations.isEmpty()) {
                return file.volume_id + "_" + file.local_id + ".jpg";
            }
            if (this.imagesArr.isEmpty()) {
                return null;
            }
            MessageObject message = (MessageObject) this.imagesArr.get(index);
            if (message.messageOwner instanceof TL_messageService) {
                return file.volume_id + "_" + file.local_id + ".jpg";
            }
            if (message.messageOwner.media == null) {
                return null;
            }
            if (message.messageOwner.media instanceof TL_messageMediaVideo) {
                return file.volume_id + "_" + file.id + ".mp4";
            }
            if ((message.messageOwner.media instanceof TL_messageMediaPhoto) || (message.messageOwner.media instanceof TL_messageMediaWebPage)) {
                return file.volume_id + "_" + file.local_id + ".jpg";
            }
            return null;
        } else if (this.imagesArrLocals.isEmpty() || index >= this.imagesArrLocals.size()) {
            return null;
        } else {
            SearchImage object = this.imagesArrLocals.get(index);
            if (!(object instanceof SearchImage)) {
                return null;
            }
            SearchImage searchImage = object;
            if (searchImage.localUrl != null && searchImage.localUrl.length() > 0) {
                File file2 = new File(searchImage.localUrl);
                if (file2.exists()) {
                    return file2.getName();
                }
                searchImage.localUrl = "";
            }
            return Utilities.MD5(searchImage.imageUrl) + "." + ImageLoader.getHttpUrlExtension(searchImage.imageUrl);
        }
    }

    private FileLocation getFileLocation(int index, int[] size) {
        if (index < 0) {
            return null;
        }
        if (this.imagesArrLocations.isEmpty()) {
            if (this.imagesArr.isEmpty() || index >= this.imagesArr.size()) {
                return null;
            }
            MessageObject message = (MessageObject) this.imagesArr.get(index);
            PhotoSize sizeFull;
            if (message.messageOwner instanceof TL_messageService) {
                if (message.messageOwner.action instanceof TL_messageActionUserUpdatedPhoto) {
                    return message.messageOwner.action.newUserPhoto.photo_big;
                }
                sizeFull = FileLoader.getClosestPhotoSizeWithSize(message.photoThumbs, AndroidUtilities.getPhotoSize());
                if (sizeFull != null) {
                    size[0] = sizeFull.size;
                    if (size[0] == 0) {
                        size[0] = -1;
                    }
                    return sizeFull.location;
                }
                size[0] = -1;
                return null;
            } else if (((message.messageOwner.media instanceof TL_messageMediaPhoto) && message.messageOwner.media.photo != null) || ((message.messageOwner.media instanceof TL_messageMediaWebPage) && message.messageOwner.media.webpage != null)) {
                sizeFull = FileLoader.getClosestPhotoSizeWithSize(message.photoThumbs, AndroidUtilities.getPhotoSize());
                if (sizeFull != null) {
                    size[0] = sizeFull.size;
                    if (size[0] == 0) {
                        size[0] = -1;
                    }
                    return sizeFull.location;
                }
                size[0] = -1;
                return null;
            } else if (!(message.messageOwner.media instanceof TL_messageMediaVideo) || message.messageOwner.media.video == null || message.messageOwner.media.video.thumb == null) {
                return null;
            } else {
                size[0] = message.messageOwner.media.video.thumb.size;
                if (size[0] == 0) {
                    size[0] = -1;
                }
                return message.messageOwner.media.video.thumb.location;
            }
        } else if (index >= this.imagesArrLocations.size()) {
            return null;
        } else {
            size[0] = ((Integer) this.imagesArrLocationsSizes.get(index)).intValue();
            return (FileLocation) this.imagesArrLocations.get(index);
        }
    }

    private InputFileLocation getInputFileLocation(int index) {
        if (index < 0) {
            return null;
        }
        FileLocation sizeFull;
        InputFileLocation location;
        if (this.imagesArrLocations.isEmpty()) {
            if (this.imagesArr.isEmpty() || index >= this.imagesArr.size()) {
                return null;
            }
            MessageObject message = (MessageObject) this.imagesArr.get(index);
            PhotoSize sizeFull2;
            if (message.messageOwner instanceof TL_messageService) {
                if (message.messageOwner.action instanceof TL_messageActionUserUpdatedPhoto) {
                    sizeFull = message.messageOwner.action.newUserPhoto.photo_big;
                    location = new TL_inputFileLocation();
                    location.local_id = sizeFull.local_id;
                    location.volume_id = sizeFull.volume_id;
                    location.id = (long) sizeFull.dc_id;
                    location.secret = sizeFull.secret;
                    return location;
                }
                sizeFull2 = FileLoader.getClosestPhotoSizeWithSize(message.photoThumbs, AndroidUtilities.getPhotoSize());
                if (sizeFull2 == null) {
                    return null;
                }
                location = new TL_inputFileLocation();
                location.local_id = sizeFull2.location.local_id;
                location.volume_id = sizeFull2.location.volume_id;
                location.id = (long) sizeFull2.location.dc_id;
                location.secret = sizeFull2.location.secret;
                return location;
            } else if ((message.messageOwner.media instanceof TL_messageMediaPhoto) || (message.messageOwner.media instanceof TL_messageMediaWebPage)) {
                sizeFull2 = FileLoader.getClosestPhotoSizeWithSize(message.photoThumbs, AndroidUtilities.getPhotoSize());
                if (sizeFull2 == null) {
                    return null;
                }
                location = new TL_inputFileLocation();
                location.local_id = sizeFull2.location.local_id;
                location.volume_id = sizeFull2.location.volume_id;
                location.id = (long) sizeFull2.location.dc_id;
                location.secret = sizeFull2.location.secret;
                return location;
            } else if (!(message.messageOwner.media instanceof TL_messageMediaVideo)) {
                return null;
            } else {
                location = new TL_inputVideoFileLocation();
                location.volume_id = (long) message.messageOwner.media.video.dc_id;
                location.id = message.messageOwner.media.video.id;
                return location;
            }
        } else if (index >= this.imagesArrLocations.size()) {
            return null;
        } else {
            sizeFull = (FileLocation) this.imagesArrLocations.get(index);
            location = new TL_inputFileLocation();
            location.local_id = sizeFull.local_id;
            location.volume_id = sizeFull.volume_id;
            location.id = (long) sizeFull.dc_id;
            location.secret = sizeFull.secret;
            return location;
        }
    }

    private void updateSelectedCount() {
        if (this.placeProvider != null) {
            this.pickerView.updateSelectedCount(this.placeProvider.getSelectedCount(), false);
        }
    }

    private void onPhotoShow(MessageObject messageObject, FileLocation fileLocation, ArrayList<MessageObject> messages, ArrayList<Object> photos, int index, PlaceProviderObject object) {
        int a;
        this.classGuid = ConnectionsManager.getInstance().generateClassGuid();
        this.currentMessageObject = null;
        this.currentFileLocation = null;
        this.currentPathObject = null;
        this.currentIndex = -1;
        this.currentFileNames[0] = null;
        this.currentFileNames[1] = null;
        this.currentFileNames[2] = null;
        this.avatarsUserId = 0;
        this.totalImagesCount = 0;
        this.totalImagesCountMerge = 0;
        this.currentEditMode = 0;
        this.isFirstLoading = true;
        this.needSearchImageInArr = false;
        this.loadingMoreImages = false;
        this.endReached[0] = false;
        this.endReached[1] = this.mergeDialogId == 0;
        this.opennedFromMedia = false;
        this.needCaptionLayout = false;
        this.canShowBottom = true;
        this.imagesArr.clear();
        this.imagesArrLocations.clear();
        this.imagesArrLocationsSizes.clear();
        this.avatarsArr.clear();
        this.imagesArrLocals.clear();
        for (a = 0; a < 2; a++) {
            this.imagesByIds[a].clear();
            this.imagesByIdsTemp[a].clear();
        }
        this.imagesArrTemp.clear();
        this.currentUserAvatarLocation = null;
        this.containerView.setPadding(0, 0, 0, 0);
        this.currentThumb = object != null ? object.thumb : null;
        this.menuItem.setVisibility(0);
        this.bottomLayout.setVisibility(0);
        this.shareButton.setVisibility(8);
        this.menuItem.hideSubItem(2);
        ViewProxy.setTranslationY(this.actionBar, 0.0f);
        ViewProxy.setTranslationY(this.pickerView, 0.0f);
        ViewProxy.setAlpha(this.checkImageView, 1.0f);
        ViewProxy.setAlpha(this.pickerView, 1.0f);
        this.checkImageView.clearAnimation();
        this.pickerView.clearAnimation();
        this.editorDoneLayout.clearAnimation();
        this.checkImageView.setVisibility(8);
        this.pickerView.setVisibility(8);
        this.cropItem.setVisibility(8);
        this.tuneItem.setVisibility(8);
        this.captionItem.setVisibility(8);
        this.captionDoneItem.setVisibility(8);
        this.captionEditText.clearAnimation();
        this.captionEditText.setVisibility(8);
        this.mentionListView.setVisibility(8);
        this.editorDoneLayout.setVisibility(8);
        this.captionTextView.setTag(null);
        this.captionTextView.clearAnimation();
        this.captionTextView.setVisibility(4);
        if (this.photoCropView != null) {
            this.photoCropView.clearAnimation();
            this.photoCropView.setVisibility(8);
        }
        if (this.photoFilterView != null) {
            this.photoFilterView.clearAnimation();
            this.photoFilterView.setVisibility(8);
        }
        for (a = 0; a < 3; a++) {
            if (this.radialProgressViews[a] != null) {
                this.radialProgressViews[a].setBackgroundState(-1, false);
            }
        }
        if (messageObject != null && messages == null) {
            this.imagesArr.add(messageObject);
            if ((messageObject.messageOwner.media instanceof TL_messageMediaWebPage) || !(messageObject.messageOwner.action == null || (messageObject.messageOwner.action instanceof TL_messageActionEmpty))) {
                this.menuItem.hideSubItem(2);
            } else {
                this.needSearchImageInArr = true;
                this.imagesByIds[0].put(Integer.valueOf(messageObject.getId()), messageObject);
                this.menuItem.showSubItem(2);
            }
            setImageIndex(0, true);
        } else if (fileLocation != null) {
            this.avatarsUserId = object.user_id;
            this.imagesArrLocations.add(fileLocation);
            this.imagesArrLocationsSizes.add(Integer.valueOf(object.size));
            this.avatarsArr.add(new TL_photoEmpty());
            this.bottomLayout.clearAnimation();
            this.shareButton.setVisibility(0);
            this.menuItem.hideSubItem(2);
            setImageIndex(0, true);
            this.currentUserAvatarLocation = fileLocation;
        } else if (messages != null) {
            this.menuItem.showSubItem(2);
            this.opennedFromMedia = true;
            this.imagesArr.addAll(messages);
            if (!this.opennedFromMedia) {
                Collections.reverse(this.imagesArr);
                index = (this.imagesArr.size() - index) - 1;
            }
            for (a = 0; a < this.imagesArr.size(); a++) {
                MessageObject message = (MessageObject) this.imagesArr.get(a);
                this.imagesByIds[message.getDialogId() == this.currentDialogId ? 0 : 1].put(Integer.valueOf(message.getId()), message);
            }
            setImageIndex(index, true);
        } else if (photos != null) {
            if (this.sendPhotoType == 0) {
                this.checkImageView.setVisibility(0);
            }
            this.menuItem.setVisibility(8);
            this.imagesArrLocals.addAll(photos);
            setImageIndex(index, true);
            this.pickerView.setVisibility(0);
            this.bottomLayout.clearAnimation();
            this.bottomLayout.setVisibility(8);
            this.canShowBottom = false;
            Object obj = this.imagesArrLocals.get(index);
            ActionBarMenuItem actionBarMenuItem = this.cropItem;
            int i = ((obj instanceof PhotoEntry) || ((obj instanceof SearchImage) && ((SearchImage) obj).type == 0)) ? 0 : 8;
            actionBarMenuItem.setVisibility(i);
            if (this.parentChatActivity != null && this.parentChatActivity.currentEncryptedChat == null) {
                this.mentionsAdapter.setChatInfo(this.parentChatActivity.info);
                this.mentionsAdapter.setNeedUsernames(this.parentChatActivity.currentChat != null);
                this.captionItem.setVisibility(this.cropItem.getVisibility());
                this.captionEditText.setVisibility(this.cropItem.getVisibility());
                this.needCaptionLayout = this.captionItem.getVisibility() == 0;
                if (this.needCaptionLayout) {
                    this.captionEditText.onCreate();
                }
            }
            if (VERSION.SDK_INT >= 16) {
                this.tuneItem.setVisibility(this.cropItem.getVisibility());
            }
            updateSelectedCount();
        }
        if (this.currentDialogId != 0 && this.totalImagesCount == 0) {
            SharedMediaQuery.getMediaCount(this.currentDialogId, 0, this.classGuid, true);
            if (this.mergeDialogId != 0) {
                SharedMediaQuery.getMediaCount(this.mergeDialogId, 0, this.classGuid, true);
            }
        } else if (this.avatarsUserId != 0) {
            MessagesController.getInstance().loadUserPhotos(this.avatarsUserId, 0, 80, 0, true, this.classGuid);
        }
    }

    private void setImages() {
        if (this.animationInProgress == 0) {
            setIndexToImage(this.centerImage, this.currentIndex);
            setIndexToImage(this.rightImage, this.currentIndex + 1);
            setIndexToImage(this.leftImage, this.currentIndex - 1);
        }
    }

    private void setImageIndex(int index, boolean init) {
        if (this.currentIndex != index) {
            if (!init) {
                this.currentThumb = null;
            }
            this.currentFileNames[0] = getFileName(index);
            this.currentFileNames[1] = getFileName(index + 1);
            this.currentFileNames[2] = getFileName(index - 1);
            this.placeProvider.willSwitchFromPhoto(this.currentMessageObject, this.currentFileLocation, this.currentIndex);
            int prevIndex = this.currentIndex;
            this.currentIndex = index;
            boolean sameImage = false;
            if (this.imagesArr.isEmpty()) {
                if (!this.imagesArrLocations.isEmpty()) {
                    this.nameTextView.setText("");
                    this.dateTextView.setText("");
                    if (this.avatarsUserId != UserConfig.getClientUserId() || this.avatarsArr.isEmpty()) {
                        this.menuItem.hideSubItem(6);
                    } else {
                        this.menuItem.showSubItem(6);
                    }
                    FileLocation old = this.currentFileLocation;
                    if (index < 0 || index >= this.imagesArrLocations.size()) {
                        closePhoto(false, false);
                        return;
                    }
                    this.currentFileLocation = (FileLocation) this.imagesArrLocations.get(index);
                    if (old != null && this.currentFileLocation != null && old.local_id == this.currentFileLocation.local_id && old.volume_id == this.currentFileLocation.volume_id) {
                        sameImage = true;
                    }
                    this.actionBar.setTitle(LocaleController.formatString("Of", C0553R.string.Of, Integer.valueOf(this.currentIndex + 1), Integer.valueOf(this.imagesArrLocations.size())));
                    this.menuItem.showSubItem(1);
                    this.shareButton.setVisibility(0);
                } else if (!this.imagesArrLocals.isEmpty()) {
                    Object object = this.imagesArrLocals.get(index);
                    if (index < 0 || index >= this.imagesArrLocals.size()) {
                        closePhoto(false, false);
                        return;
                    }
                    boolean fromCamera = false;
                    CharSequence caption = null;
                    if (object instanceof PhotoEntry) {
                        this.currentPathObject = ((PhotoEntry) object).path;
                        fromCamera = ((PhotoEntry) object).bucketId == 0 && ((PhotoEntry) object).dateTaken == 0 && this.imagesArrLocals.size() == 1;
                        caption = ((PhotoEntry) object).caption;
                    } else if (object instanceof SearchImage) {
                        this.currentPathObject = ((SearchImage) object).imageUrl;
                        caption = ((SearchImage) object).caption;
                    }
                    if (fromCamera) {
                        this.actionBar.setTitle(LocaleController.getString("AttachPhoto", C0553R.string.AttachPhoto));
                    } else {
                        this.actionBar.setTitle(LocaleController.formatString("Of", C0553R.string.Of, Integer.valueOf(this.currentIndex + 1), Integer.valueOf(this.imagesArrLocals.size())));
                    }
                    if (this.sendPhotoType == 0) {
                        this.checkImageView.setChecked(this.placeProvider.isPhotoChecked(this.currentIndex), false);
                    }
                    setCurrentCaption(caption);
                    updateCaptionTextForCurrentPhoto(object);
                }
            } else if (this.currentIndex < 0 || this.currentIndex >= this.imagesArr.size()) {
                closePhoto(false, false);
                return;
            } else {
                this.currentMessageObject = (MessageObject) this.imagesArr.get(this.currentIndex);
                if (this.currentMessageObject.canDeleteMessage(null)) {
                    this.menuItem.showSubItem(6);
                } else {
                    this.menuItem.hideSubItem(6);
                }
                if (this.currentMessageObject.messageOwner.from_id > 0) {
                    User user = MessagesController.getInstance().getUser(Integer.valueOf(this.currentMessageObject.messageOwner.from_id));
                    if (user != null) {
                        this.nameTextView.setText(UserObject.getUserName(user));
                    } else {
                        this.nameTextView.setText("");
                    }
                } else {
                    Chat chat = MessagesController.getInstance().getChat(Integer.valueOf(-this.currentMessageObject.messageOwner.from_id));
                    if (chat != null) {
                        this.nameTextView.setText(chat.title);
                    } else {
                        this.nameTextView.setText("");
                    }
                }
                long date = ((long) this.currentMessageObject.messageOwner.date) * 1000;
                String dateString = LocaleController.formatString("formatDateAtTime", C0553R.string.formatDateAtTime, LocaleController.getInstance().formatterYear.format(new Date(date)), LocaleController.getInstance().formatterDay.format(new Date(date)));
                if (this.currentFileNames[0] == null || !this.currentFileNames[0].endsWith("mp4")) {
                    this.dateTextView.setText(dateString);
                } else {
                    this.dateTextView.setText(String.format("%s (%s)", new Object[]{dateString, AndroidUtilities.formatFileSize((long) this.currentMessageObject.messageOwner.media.video.size)}));
                }
                setCurrentCaption(this.currentMessageObject.caption);
                if (this.totalImagesCount + this.totalImagesCountMerge == 0 || this.needSearchImageInArr) {
                    if (this.currentMessageObject.messageOwner.media instanceof TL_messageMediaWebPage) {
                        this.actionBar.setTitle(LocaleController.getString("AttachPhoto", C0553R.string.AttachPhoto));
                    }
                } else if (this.opennedFromMedia) {
                    if (this.imagesArr.size() < this.totalImagesCount + this.totalImagesCountMerge && !this.loadingMoreImages && this.currentIndex > this.imagesArr.size() - 5) {
                        loadFromMaxId = this.imagesArr.isEmpty() ? 0 : ((MessageObject) this.imagesArr.get(this.imagesArr.size() - 1)).getId();
                        loadIndex = 0;
                        if (this.endReached[0] && this.mergeDialogId != 0) {
                            loadIndex = 1;
                            if (!(this.imagesArr.isEmpty() || ((MessageObject) this.imagesArr.get(this.imagesArr.size() - 1)).getDialogId() == this.mergeDialogId)) {
                                loadFromMaxId = 0;
                            }
                        }
                        SharedMediaQuery.loadMedia(loadIndex == 0 ? this.currentDialogId : this.mergeDialogId, 0, 80, loadFromMaxId, 0, true, this.classGuid);
                        this.loadingMoreImages = true;
                    }
                    this.actionBar.setTitle(LocaleController.formatString("Of", C0553R.string.Of, Integer.valueOf(this.currentIndex + 1), Integer.valueOf(this.totalImagesCount + this.totalImagesCountMerge)));
                } else {
                    if (this.imagesArr.size() < this.totalImagesCount + this.totalImagesCountMerge && !this.loadingMoreImages && this.currentIndex < 5) {
                        loadFromMaxId = this.imagesArr.isEmpty() ? 0 : ((MessageObject) this.imagesArr.get(0)).getId();
                        loadIndex = 0;
                        if (this.endReached[0] && this.mergeDialogId != 0) {
                            loadIndex = 1;
                            if (!(this.imagesArr.isEmpty() || ((MessageObject) this.imagesArr.get(0)).getDialogId() == this.mergeDialogId)) {
                                loadFromMaxId = 0;
                            }
                        }
                        SharedMediaQuery.loadMedia(loadIndex == 0 ? this.currentDialogId : this.mergeDialogId, 0, 80, loadFromMaxId, 0, true, this.classGuid);
                        this.loadingMoreImages = true;
                    }
                    this.actionBar.setTitle(LocaleController.formatString("Of", C0553R.string.Of, Integer.valueOf((((this.totalImagesCount + this.totalImagesCountMerge) - this.imagesArr.size()) + this.currentIndex) + 1), Integer.valueOf(this.totalImagesCount + this.totalImagesCountMerge)));
                }
                if (this.currentMessageObject.messageOwner.ttl != 0) {
                    this.menuItem.hideSubItem(1);
                    this.shareButton.setVisibility(8);
                } else {
                    this.menuItem.showSubItem(1);
                    this.shareButton.setVisibility(0);
                }
            }
            if (this.currentPlaceObject != null) {
                if (this.animationInProgress == 0) {
                    this.currentPlaceObject.imageReceiver.setVisible(true, true);
                } else {
                    this.showAfterAnimation = this.currentPlaceObject;
                }
            }
            this.currentPlaceObject = this.placeProvider.getPlaceForPhoto(this.currentMessageObject, this.currentFileLocation, this.currentIndex);
            if (this.currentPlaceObject != null) {
                if (this.animationInProgress == 0) {
                    this.currentPlaceObject.imageReceiver.setVisible(false, true);
                } else {
                    this.hideAfterAnimation = this.currentPlaceObject;
                }
            }
            if (!sameImage) {
                this.draggingDown = false;
                this.translationX = 0.0f;
                this.translationY = 0.0f;
                this.scale = 1.0f;
                this.animateToX = 0.0f;
                this.animateToY = 0.0f;
                this.animateToScale = 1.0f;
                this.animationStartTime = 0;
                this.imageMoveAnimation = null;
                this.changeModeAnimation = null;
                this.pinchStartDistance = 0.0f;
                this.pinchStartScale = 1.0f;
                this.pinchCenterX = 0.0f;
                this.pinchCenterY = 0.0f;
                this.pinchStartX = 0.0f;
                this.pinchStartY = 0.0f;
                this.moveStartX = 0.0f;
                this.moveStartY = 0.0f;
                this.zooming = false;
                this.moving = false;
                this.doubleTap = false;
                this.invalidCoords = false;
                this.canDragDown = true;
                this.changingPage = false;
                this.switchImageAfterAnimation = 0;
                boolean z = (this.imagesArrLocals.isEmpty() && (this.currentFileNames[0] == null || this.currentFileNames[0].endsWith("mp4") || this.radialProgressViews[0].backgroundState == 0)) ? false : true;
                this.canZoom = z;
                updateMinMax(this.scale);
            }
            if (prevIndex == -1) {
                setImages();
                for (int a = 0; a < 3; a++) {
                    checkProgress(a, false);
                }
            } else {
                checkProgress(0, false);
                ImageReceiver temp;
                RadialProgressView tempProgress;
                if (prevIndex > this.currentIndex) {
                    temp = this.rightImage;
                    this.rightImage = this.centerImage;
                    this.centerImage = this.leftImage;
                    this.leftImage = temp;
                    tempProgress = this.radialProgressViews[0];
                    this.radialProgressViews[0] = this.radialProgressViews[2];
                    this.radialProgressViews[2] = tempProgress;
                    setIndexToImage(this.leftImage, this.currentIndex - 1);
                    checkProgress(1, false);
                    checkProgress(2, false);
                } else if (prevIndex < this.currentIndex) {
                    temp = this.leftImage;
                    this.leftImage = this.centerImage;
                    this.centerImage = this.rightImage;
                    this.rightImage = temp;
                    tempProgress = this.radialProgressViews[0];
                    this.radialProgressViews[0] = this.radialProgressViews[1];
                    this.radialProgressViews[1] = tempProgress;
                    setIndexToImage(this.rightImage, this.currentIndex + 1);
                    checkProgress(1, false);
                    checkProgress(2, false);
                }
            }
            createGifForCurrentImage();
        }
    }

    private void setCurrentCaption(CharSequence caption) {
        if (caption == null || caption.length() <= 0) {
            this.captionItem.setIcon(C0553R.drawable.photo_text);
            this.captionTextView.setTag(null);
            this.captionTextView.clearAnimation();
            this.captionTextView.setVisibility(4);
            return;
        }
        this.captionTextView = this.captionTextViewOld;
        this.captionTextViewOld = this.captionTextViewNew;
        this.captionTextViewNew = this.captionTextView;
        this.captionItem.setIcon(C0553R.drawable.photo_text2);
        this.captionTextView.setTag(caption);
        this.captionTextView.setText(caption);
        View view = this.captionTextView;
        float f = (this.bottomLayout.getVisibility() == 0 || this.pickerView.getVisibility() == 0) ? 1.0f : 0.0f;
        ViewProxy.setAlpha(view, f);
        AndroidUtilities.runOnUIThread(new Runnable() {
            public void run() {
                int i = 4;
                PhotoViewer.this.captionTextViewOld.setTag(null);
                PhotoViewer.this.captionTextViewOld.clearAnimation();
                PhotoViewer.this.captionTextViewOld.setVisibility(4);
                TextView access$1200 = PhotoViewer.this.captionTextViewNew;
                if (PhotoViewer.this.bottomLayout.getVisibility() == 0 || PhotoViewer.this.pickerView.getVisibility() == 0) {
                    i = 0;
                }
                access$1200.setVisibility(i);
            }
        });
    }

    private void createGifForCurrentImage() {
        if (this.gifDrawable != null) {
            this.gifDrawable.recycle();
            this.gifDrawable = null;
        }
        if (!this.imagesArrLocals.isEmpty() && this.currentIndex >= 0 && this.currentIndex < this.imagesArrLocals.size()) {
            Object object = this.imagesArrLocals.get(this.currentIndex);
            if ((object instanceof SearchImage) && ((SearchImage) object).type == 1) {
                File f = new File(FileLoader.getInstance().getDirectory(3), this.currentFileNames[0]);
                if (!f.exists()) {
                    f = new File(FileLoader.getInstance().getDirectory(4), this.currentFileNames[0]);
                }
                if (f.exists()) {
                    try {
                        this.gifDrawable = new GifDrawable(f);
                        this.gifDrawable.parentView = new WeakReference(this.containerView);
                    } catch (Throwable e) {
                        FileLog.m611e("tmessages", e);
                    }
                    if (this.gifDrawable != null) {
                        this.gifDrawable.start();
                    }
                }
            }
        }
    }

    private void checkProgress(int a, boolean animated) {
        boolean z = false;
        if (this.currentFileNames[a] != null) {
            int index = this.currentIndex;
            if (a == 1) {
                index++;
            } else if (a == 2) {
                index--;
            }
            File f = null;
            if (this.currentMessageObject != null) {
                f = FileLoader.getPathToMessage(((MessageObject) this.imagesArr.get(index)).messageOwner);
            } else if (this.currentFileLocation != null) {
                boolean z2;
                FileLocation location = (FileLocation) this.imagesArrLocations.get(index);
                if (this.avatarsUserId != 0) {
                    z2 = true;
                } else {
                    z2 = false;
                }
                f = FileLoader.getPathToAttach(location, z2);
            } else if (this.currentPathObject != null) {
                f = new File(FileLoader.getInstance().getDirectory(3), this.currentFileNames[a]);
                if (!f.exists()) {
                    f = new File(FileLoader.getInstance().getDirectory(4), this.currentFileNames[a]);
                }
            }
            if (f == null || !f.exists()) {
                if (!this.currentFileNames[a].endsWith("mp4")) {
                    this.radialProgressViews[a].setBackgroundState(0, animated);
                } else if (FileLoader.getInstance().isLoadingFile(this.currentFileNames[a])) {
                    this.radialProgressViews[a].setBackgroundState(1, false);
                } else {
                    this.radialProgressViews[a].setBackgroundState(2, false);
                }
                Float progress = ImageLoader.getInstance().getFileProgress(this.currentFileNames[a]);
                if (progress == null) {
                    progress = Float.valueOf(0.0f);
                }
                this.radialProgressViews[a].setProgress(progress.floatValue(), false);
            } else if (this.currentFileNames[a].endsWith("mp4")) {
                this.radialProgressViews[a].setBackgroundState(3, animated);
            } else {
                this.radialProgressViews[a].setBackgroundState(-1, animated);
            }
            if (a == 0) {
                if (!(this.imagesArrLocals.isEmpty() && (this.currentFileNames[0] == null || this.currentFileNames[0].endsWith("mp4") || this.radialProgressViews[0].backgroundState == 0))) {
                    z = true;
                }
                this.canZoom = z;
                return;
            }
            return;
        }
        this.radialProgressViews[a].setBackgroundState(-1, animated);
    }

    private void setIndexToImage(ImageReceiver imageReceiver, int index) {
        imageReceiver.setOrientation(0, false);
        Bitmap placeHolder;
        if (this.imagesArrLocals.isEmpty()) {
            int[] size = new int[1];
            FileLocation fileLocation = getFileLocation(index, size);
            if (fileLocation != null) {
                MessageObject messageObject = null;
                if (!this.imagesArr.isEmpty()) {
                    messageObject = (MessageObject) this.imagesArr.get(index);
                }
                imageReceiver.setParentMessageObject(messageObject);
                if (messageObject != null) {
                    imageReceiver.setShouldGenerateQualityThumb(true);
                }
                if (messageObject == null || !(messageObject.messageOwner.media instanceof TL_messageMediaVideo)) {
                    imageReceiver.setNeedsQualityThumb(false);
                    placeHolder = null;
                    if (this.currentThumb != null && imageReceiver == this.centerImage) {
                        placeHolder = this.currentThumb;
                    }
                    if (size[0] == 0) {
                        size[0] = -1;
                    }
                    PhotoSize thumbLocation = messageObject != null ? FileLoader.getClosestPhotoSizeWithSize(messageObject.photoThumbs, 100) : null;
                    imageReceiver.setImage(fileLocation, null, null, placeHolder != null ? new BitmapDrawable(null, placeHolder) : null, thumbLocation != null ? thumbLocation.location : null, "b", size[0], null, this.avatarsUserId != 0);
                    return;
                }
                imageReceiver.setNeedsQualityThumb(true);
                if (messageObject.messageOwner.media.video.thumb != null) {
                    placeHolder = null;
                    if (this.currentThumb != null && imageReceiver == this.centerImage) {
                        placeHolder = this.currentThumb;
                    }
                    imageReceiver.setImage(null, null, null, placeHolder != null ? new BitmapDrawable(null, placeHolder) : null, FileLoader.getClosestPhotoSizeWithSize(messageObject.photoThumbs, 100).location, "b", 0, null, true);
                    return;
                }
                imageReceiver.setImageBitmap(this.parentActivity.getResources().getDrawable(C0553R.drawable.photoview_placeholder));
                return;
            }
            imageReceiver.setNeedsQualityThumb(false);
            imageReceiver.setParentMessageObject(null);
            if (size[0] == 0) {
                imageReceiver.setImageBitmap((Bitmap) null);
                return;
            } else {
                imageReceiver.setImageBitmap(this.parentActivity.getResources().getDrawable(C0553R.drawable.photoview_placeholder));
                return;
            }
        }
        imageReceiver.setParentMessageObject(null);
        if (index < 0 || index >= this.imagesArrLocals.size()) {
            imageReceiver.setImageBitmap((Bitmap) null);
            return;
        }
        Drawable bitmapDrawable;
        PhotoEntry object = this.imagesArrLocals.get(index);
        int size2 = (int) (((float) AndroidUtilities.getPhotoSize()) / AndroidUtilities.density);
        placeHolder = null;
        if (this.currentThumb != null && imageReceiver == this.centerImage) {
            placeHolder = this.currentThumb;
        }
        if (placeHolder == null) {
            placeHolder = this.placeProvider.getThumbForPhoto(null, null, index);
        }
        String path = null;
        int imageSize = 0;
        if (object instanceof PhotoEntry) {
            PhotoEntry photoEntry = object;
            if (photoEntry.imagePath != null) {
                path = photoEntry.imagePath;
            } else {
                imageReceiver.setOrientation(photoEntry.orientation, false);
                path = photoEntry.path;
            }
        } else if (object instanceof SearchImage) {
            SearchImage photoEntry2 = (SearchImage) object;
            if (photoEntry2.imagePath != null) {
                path = photoEntry2.imagePath;
            } else {
                path = photoEntry2.imageUrl;
                imageSize = ((SearchImage) object).size;
            }
        }
        String format = String.format(Locale.US, "%d_%d", new Object[]{Integer.valueOf(size2), Integer.valueOf(size2)});
        if (placeHolder != null) {
            bitmapDrawable = new BitmapDrawable(null, placeHolder);
        } else {
            bitmapDrawable = null;
        }
        imageReceiver.setImage(path, format, bitmapDrawable, null, imageSize);
    }

    public boolean isShowingImage(MessageObject object) {
        return (!this.isVisible || this.disableShowCheck || object == null || this.currentMessageObject == null || this.currentMessageObject.getId() != object.getId()) ? false : true;
    }

    public boolean isShowingImage(FileLocation object) {
        return this.isVisible && !this.disableShowCheck && object != null && this.currentFileLocation != null && object.local_id == this.currentFileLocation.local_id && object.volume_id == this.currentFileLocation.volume_id && object.dc_id == this.currentFileLocation.dc_id;
    }

    public boolean isShowingImage(String object) {
        return (!this.isVisible || this.disableShowCheck || object == null || this.currentPathObject == null || !object.equals(this.currentPathObject)) ? false : true;
    }

    public void openPhoto(MessageObject messageObject, long dialogId, long mergeDialogId, PhotoViewerProvider provider) {
        openPhoto(messageObject, null, null, null, 0, provider, null, dialogId, mergeDialogId);
    }

    public void openPhoto(FileLocation fileLocation, PhotoViewerProvider provider) {
        openPhoto(null, fileLocation, null, null, 0, provider, null, 0, 0);
    }

    public void openPhoto(ArrayList<MessageObject> messages, int index, long dialogId, long mergeDialogId, PhotoViewerProvider provider) {
        openPhoto((MessageObject) messages.get(index), null, messages, null, index, provider, null, dialogId, mergeDialogId);
    }

    public void openPhotoForSelect(ArrayList<Object> photos, int index, int type, PhotoViewerProvider provider, ChatActivity chatActivity) {
        this.sendPhotoType = type;
        if (this.pickerView != null) {
            this.pickerView.doneButtonTextView.setText(this.sendPhotoType == 1 ? LocaleController.getString("Set", C0553R.string.Set).toUpperCase() : LocaleController.getString("Send", C0553R.string.Send).toUpperCase());
        }
        openPhoto(null, null, null, photos, index, provider, chatActivity, 0, 0);
    }

    private boolean checkAnimation() {
        if (this.animationInProgress != 0 && Math.abs(this.transitionAnimationStartTime - System.currentTimeMillis()) >= 500) {
            if (this.animationEndRunnable != null) {
                this.animationEndRunnable.run();
                this.animationEndRunnable = null;
            }
            this.animationInProgress = 0;
        }
        if (this.animationInProgress != 0) {
            return true;
        }
        return false;
    }

    public void openPhoto(MessageObject messageObject, FileLocation fileLocation, ArrayList<MessageObject> messages, ArrayList<Object> photos, int index, PhotoViewerProvider provider, ChatActivity chatActivity, long dialogId, long mDialogId) {
        if (this.parentActivity != null && !this.isVisible) {
            if (provider != null || !checkAnimation()) {
                if (messageObject != null || fileLocation != null || messages != null || photos != null) {
                    final PlaceProviderObject object = provider.getPlaceForPhoto(messageObject, fileLocation, index);
                    if (object != null || photos != null) {
                        WindowManager wm = (WindowManager) this.parentActivity.getSystemService("window");
                        if (this.windowView.attachedToWindow) {
                            try {
                                wm.removeView(this.windowView);
                            } catch (Exception e) {
                            }
                        }
                        try {
                            this.windowLayoutParams.type = 99;
                            this.windowLayoutParams.flags = 8;
                            this.windowLayoutParams.softInputMode = 0;
                            this.windowView.setFocusable(false);
                            this.containerView.setFocusable(false);
                            wm.addView(this.windowView, this.windowLayoutParams);
                            this.parentChatActivity = chatActivity;
                            this.actionBar.setTitle(LocaleController.formatString("Of", C0553R.string.Of, Integer.valueOf(1), Integer.valueOf(1)));
                            NotificationCenter.getInstance().addObserver(this, NotificationCenter.FileDidFailedLoad);
                            NotificationCenter.getInstance().addObserver(this, NotificationCenter.FileDidLoaded);
                            NotificationCenter.getInstance().addObserver(this, NotificationCenter.FileLoadProgressChanged);
                            NotificationCenter.getInstance().addObserver(this, NotificationCenter.mediaCountDidLoaded);
                            NotificationCenter.getInstance().addObserver(this, NotificationCenter.mediaDidLoaded);
                            NotificationCenter.getInstance().addObserver(this, NotificationCenter.userPhotosLoaded);
                            NotificationCenter.getInstance().addObserver(this, NotificationCenter.emojiDidLoaded);
                            this.placeProvider = provider;
                            this.mergeDialogId = mDialogId;
                            this.currentDialogId = dialogId;
                            if (this.velocityTracker == null) {
                                this.velocityTracker = VelocityTracker.obtain();
                            }
                            this.isVisible = true;
                            toggleActionBar(true, false);
                            if (object != null) {
                                float scale;
                                this.disableShowCheck = true;
                                this.animationInProgress = 1;
                                onPhotoShow(messageObject, fileLocation, messages, photos, index, object);
                                Rect drawRegion = object.imageReceiver.getDrawRegion();
                                int orientation = object.imageReceiver.getOrientation();
                                this.animatingImageView.setVisibility(0);
                                this.animatingImageView.setRadius(object.radius);
                                this.animatingImageView.setOrientation(orientation);
                                this.animatingImageView.setNeedRadius(object.radius != 0);
                                this.animatingImageView.setImageBitmap(object.thumb);
                                ViewProxy.setAlpha(this.animatingImageView, 1.0f);
                                ViewProxy.setPivotX(this.animatingImageView, 0.0f);
                                ViewProxy.setPivotY(this.animatingImageView, 0.0f);
                                ViewProxy.setScaleX(this.animatingImageView, object.scale);
                                ViewProxy.setScaleY(this.animatingImageView, object.scale);
                                ViewProxy.setTranslationX(this.animatingImageView, ((float) object.viewX) + (((float) drawRegion.left) * object.scale));
                                ViewProxy.setTranslationY(this.animatingImageView, ((float) object.viewY) + (((float) drawRegion.top) * object.scale));
                                ViewGroup.LayoutParams layoutParams = this.animatingImageView.getLayoutParams();
                                layoutParams.width = drawRegion.right - drawRegion.left;
                                layoutParams.height = drawRegion.bottom - drawRegion.top;
                                this.animatingImageView.setLayoutParams(layoutParams);
                                float scaleX = ((float) AndroidUtilities.displaySize.x) / ((float) layoutParams.width);
                                float scaleY = ((float) (AndroidUtilities.displaySize.y - AndroidUtilities.statusBarHeight)) / ((float) layoutParams.height);
                                if (scaleX > scaleY) {
                                    scale = scaleY;
                                } else {
                                    scale = scaleX;
                                }
                                float xPos = (((float) AndroidUtilities.displaySize.x) - (((float) layoutParams.width) * scale)) / 2.0f;
                                float yPos = (((float) (AndroidUtilities.displaySize.y - AndroidUtilities.statusBarHeight)) - (((float) layoutParams.height) * scale)) / 2.0f;
                                int clipHorizontal = Math.abs(drawRegion.left - object.imageReceiver.getImageX());
                                int clipVertical = Math.abs(drawRegion.top - object.imageReceiver.getImageY());
                                int[] coords2 = new int[2];
                                object.parentView.getLocationInWindow(coords2);
                                int clipTop = ((coords2[1] - AndroidUtilities.statusBarHeight) - (object.viewY + drawRegion.top)) + object.clipTopAddition;
                                if (clipTop < 0) {
                                    clipTop = 0;
                                }
                                int clipBottom = (((object.viewY + drawRegion.top) + layoutParams.height) - ((coords2[1] + object.parentView.getHeight()) - AndroidUtilities.statusBarHeight)) + object.clipBottomAddition;
                                if (clipBottom < 0) {
                                    clipBottom = 0;
                                }
                                clipTop = Math.max(clipTop, clipVertical);
                                clipBottom = Math.max(clipBottom, clipVertical);
                                this.animationValues[0][0] = ViewProxy.getScaleX(this.animatingImageView);
                                this.animationValues[0][1] = ViewProxy.getScaleY(this.animatingImageView);
                                this.animationValues[0][2] = ViewProxy.getTranslationX(this.animatingImageView);
                                this.animationValues[0][3] = ViewProxy.getTranslationY(this.animatingImageView);
                                this.animationValues[0][4] = ((float) clipHorizontal) * object.scale;
                                this.animationValues[0][5] = ((float) clipTop) * object.scale;
                                this.animationValues[0][6] = ((float) clipBottom) * object.scale;
                                this.animationValues[0][7] = (float) this.animatingImageView.getRadius();
                                this.animationValues[1][0] = scale;
                                this.animationValues[1][1] = scale;
                                this.animationValues[1][2] = xPos;
                                this.animationValues[1][3] = yPos;
                                this.animationValues[1][4] = 0.0f;
                                this.animationValues[1][5] = 0.0f;
                                this.animationValues[1][6] = 0.0f;
                                this.animationValues[1][7] = 0.0f;
                                this.animatingImageView.setAnimationProgress(0.0f);
                                this.backgroundDrawable.setAlpha(0);
                                ViewProxy.setAlpha(this.containerView, 0.0f);
                                final AnimatorSetProxy animatorSet = new AnimatorSetProxy();
                                animatorSet.playTogether(ObjectAnimatorProxy.ofFloat(this.animatingImageView, "animationProgress", 0.0f, 1.0f), ObjectAnimatorProxy.ofInt(this.backgroundDrawable, "alpha", 0, 255), ObjectAnimatorProxy.ofFloat(this.containerView, "alpha", 0.0f, 1.0f));
                                final ArrayList<Object> arrayList = photos;
                                this.animationEndRunnable = new Runnable() {
                                    public void run() {
                                        if (PhotoViewer.this.containerView != null) {
                                            if (VERSION.SDK_INT >= 18) {
                                                PhotoViewer.this.containerView.setLayerType(0, null);
                                            }
                                            PhotoViewer.this.animationInProgress = 0;
                                            PhotoViewer.this.transitionAnimationStartTime = 0;
                                            PhotoViewer.this.setImages();
                                            PhotoViewer.this.containerView.invalidate();
                                            PhotoViewer.this.animatingImageView.setVisibility(8);
                                            if (PhotoViewer.this.showAfterAnimation != null) {
                                                PhotoViewer.this.showAfterAnimation.imageReceiver.setVisible(true, true);
                                            }
                                            if (PhotoViewer.this.hideAfterAnimation != null) {
                                                PhotoViewer.this.hideAfterAnimation.imageReceiver.setVisible(false, true);
                                            }
                                            if (arrayList != null) {
                                                PhotoViewer.this.windowLayoutParams.flags = 0;
                                                PhotoViewer.this.windowLayoutParams.softInputMode = 32;
                                                ((WindowManager) PhotoViewer.this.parentActivity.getSystemService("window")).updateViewLayout(PhotoViewer.this.windowView, PhotoViewer.this.windowLayoutParams);
                                                PhotoViewer.this.windowView.setFocusable(true);
                                                PhotoViewer.this.containerView.setFocusable(true);
                                            }
                                        }
                                    }
                                };
                                animatorSet.setDuration(200);
                                animatorSet.addListener(new AnimatorListenerAdapterProxy() {

                                    class C11481 implements Runnable {
                                        C11481() {
                                        }

                                        public void run() {
                                            NotificationCenter.getInstance().setAnimationInProgress(false);
                                            if (PhotoViewer.this.animationEndRunnable != null) {
                                                PhotoViewer.this.animationEndRunnable.run();
                                                PhotoViewer.this.animationEndRunnable = null;
                                            }
                                        }
                                    }

                                    public void onAnimationEnd(Object animation) {
                                        AndroidUtilities.runOnUIThread(new C11481());
                                    }

                                    public void onAnimationCancel(Object animation) {
                                        onAnimationEnd(animation);
                                    }
                                });
                                this.transitionAnimationStartTime = System.currentTimeMillis();
                                AndroidUtilities.runOnUIThread(new Runnable() {
                                    public void run() {
                                        NotificationCenter.getInstance().setAnimationInProgress(true);
                                        animatorSet.start();
                                    }
                                });
                                if (VERSION.SDK_INT >= 18) {
                                    this.containerView.setLayerType(2, null);
                                }
                                this.backgroundDrawable.drawRunnable = new Runnable() {
                                    public void run() {
                                        PhotoViewer.this.disableShowCheck = false;
                                        object.imageReceiver.setVisible(false, true);
                                    }
                                };
                                return;
                            }
                            if (photos != null) {
                                this.windowLayoutParams.flags = 0;
                                this.windowLayoutParams.softInputMode = 32;
                                wm.updateViewLayout(this.windowView, this.windowLayoutParams);
                                this.windowView.setFocusable(true);
                                this.containerView.setFocusable(true);
                            }
                            this.backgroundDrawable.setAlpha(255);
                            ViewProxy.setAlpha(this.containerView, 1.0f);
                            onPhotoShow(messageObject, fileLocation, messages, photos, index, object);
                        } catch (Throwable e2) {
                            FileLog.m611e("tmessages", e2);
                        }
                    }
                }
            }
        }
    }

    public void closePhoto(boolean animated, boolean fromEditMode) {
        if (fromEditMode || this.currentEditMode == 0) {
            try {
                if (this.visibleDialog != null) {
                    this.visibleDialog.dismiss();
                    this.visibleDialog = null;
                }
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
            if (this.currentEditMode != 0) {
                if (this.currentEditMode == 2) {
                    this.photoFilterView.shutdown();
                    this.containerView.removeView(this.photoFilterView);
                    this.photoFilterView = null;
                } else if (this.currentEditMode == 1) {
                    this.editorDoneLayout.setVisibility(8);
                    this.photoCropView.setVisibility(8);
                }
                this.currentEditMode = 0;
            }
            if (this.parentActivity != null && this.isVisible && !checkAnimation() && this.placeProvider != null) {
                this.captionEditText.onDestroy();
                this.parentChatActivity = null;
                NotificationCenter.getInstance().removeObserver(this, NotificationCenter.FileDidFailedLoad);
                NotificationCenter.getInstance().removeObserver(this, NotificationCenter.FileDidLoaded);
                NotificationCenter.getInstance().removeObserver(this, NotificationCenter.FileLoadProgressChanged);
                NotificationCenter.getInstance().removeObserver(this, NotificationCenter.mediaCountDidLoaded);
                NotificationCenter.getInstance().removeObserver(this, NotificationCenter.mediaDidLoaded);
                NotificationCenter.getInstance().removeObserver(this, NotificationCenter.userPhotosLoaded);
                NotificationCenter.getInstance().removeObserver(this, NotificationCenter.emojiDidLoaded);
                ConnectionsManager.getInstance().cancelRequestsForGuid(this.classGuid);
                this.isActionBarVisible = false;
                if (this.velocityTracker != null) {
                    this.velocityTracker.recycle();
                    this.velocityTracker = null;
                }
                ConnectionsManager.getInstance().cancelRequestsForGuid(this.classGuid);
                final PlaceProviderObject object = this.placeProvider.getPlaceForPhoto(this.currentMessageObject, this.currentFileLocation, this.currentIndex);
                AnimatorSetProxy animatorSet;
                Object[] objArr;
                if (animated) {
                    float scale2;
                    this.animationInProgress = 1;
                    this.animatingImageView.setVisibility(0);
                    this.containerView.invalidate();
                    animatorSet = new AnimatorSetProxy();
                    ViewGroup.LayoutParams layoutParams = this.animatingImageView.getLayoutParams();
                    Rect drawRegion = null;
                    this.animatingImageView.setOrientation(this.centerImage.getOrientation());
                    if (object != null) {
                        this.animatingImageView.setNeedRadius(object.radius != 0);
                        drawRegion = object.imageReceiver.getDrawRegion();
                        layoutParams.width = drawRegion.right - drawRegion.left;
                        layoutParams.height = drawRegion.bottom - drawRegion.top;
                        this.animatingImageView.setImageBitmap(object.thumb);
                    } else {
                        this.animatingImageView.setNeedRadius(false);
                        layoutParams.width = this.centerImage.getImageWidth();
                        layoutParams.height = this.centerImage.getImageHeight();
                        this.animatingImageView.setImageBitmap(this.centerImage.getBitmap());
                    }
                    this.animatingImageView.setLayoutParams(layoutParams);
                    float scaleX = ((float) AndroidUtilities.displaySize.x) / ((float) layoutParams.width);
                    float scaleY = ((float) (AndroidUtilities.displaySize.y - AndroidUtilities.statusBarHeight)) / ((float) layoutParams.height);
                    if (scaleX > scaleY) {
                        scale2 = scaleY;
                    } else {
                        scale2 = scaleX;
                    }
                    float yPos = (((float) (AndroidUtilities.displaySize.y - AndroidUtilities.statusBarHeight)) - ((((float) layoutParams.height) * this.scale) * scale2)) / 2.0f;
                    ViewProxy.setTranslationX(this.animatingImageView, this.translationX + ((((float) AndroidUtilities.displaySize.x) - ((((float) layoutParams.width) * this.scale) * scale2)) / 2.0f));
                    ViewProxy.setTranslationY(this.animatingImageView, this.translationY + yPos);
                    ViewProxy.setScaleX(this.animatingImageView, this.scale * scale2);
                    ViewProxy.setScaleY(this.animatingImageView, this.scale * scale2);
                    if (object != null) {
                        object.imageReceiver.setVisible(false, true);
                        int clipHorizontal = Math.abs(drawRegion.left - object.imageReceiver.getImageX());
                        int clipVertical = Math.abs(drawRegion.top - object.imageReceiver.getImageY());
                        int[] coords2 = new int[2];
                        object.parentView.getLocationInWindow(coords2);
                        int clipTop = ((coords2[1] - AndroidUtilities.statusBarHeight) - (object.viewY + drawRegion.top)) + object.clipTopAddition;
                        if (clipTop < 0) {
                            clipTop = 0;
                        }
                        int clipBottom = (((object.viewY + drawRegion.top) + (drawRegion.bottom - drawRegion.top)) - ((coords2[1] + object.parentView.getHeight()) - AndroidUtilities.statusBarHeight)) + object.clipBottomAddition;
                        if (clipBottom < 0) {
                            clipBottom = 0;
                        }
                        clipTop = Math.max(clipTop, clipVertical);
                        clipBottom = Math.max(clipBottom, clipVertical);
                        this.animationValues[0][0] = ViewProxy.getScaleX(this.animatingImageView);
                        this.animationValues[0][1] = ViewProxy.getScaleY(this.animatingImageView);
                        this.animationValues[0][2] = ViewProxy.getTranslationX(this.animatingImageView);
                        this.animationValues[0][3] = ViewProxy.getTranslationY(this.animatingImageView);
                        this.animationValues[0][4] = 0.0f;
                        this.animationValues[0][5] = 0.0f;
                        this.animationValues[0][6] = 0.0f;
                        this.animationValues[0][7] = 0.0f;
                        this.animationValues[1][0] = object.scale;
                        this.animationValues[1][1] = object.scale;
                        this.animationValues[1][2] = ((float) object.viewX) + (((float) drawRegion.left) * object.scale);
                        this.animationValues[1][3] = ((float) object.viewY) + (((float) drawRegion.top) * object.scale);
                        this.animationValues[1][4] = ((float) clipHorizontal) * object.scale;
                        this.animationValues[1][5] = ((float) clipTop) * object.scale;
                        this.animationValues[1][6] = ((float) clipBottom) * object.scale;
                        this.animationValues[1][7] = (float) object.radius;
                        objArr = new Object[3];
                        float[] fArr = new float[2];
                        objArr[0] = ObjectAnimatorProxy.ofFloat(this.animatingImageView, "animationProgress", 0.0f, 1.0f);
                        objArr[1] = ObjectAnimatorProxy.ofInt(this.backgroundDrawable, "alpha", 0);
                        objArr[2] = ObjectAnimatorProxy.ofFloat(this.containerView, "alpha", 0.0f);
                        animatorSet.playTogether(objArr);
                    } else {
                        Object[] objArr2 = new Object[4];
                        objArr2[0] = ObjectAnimatorProxy.ofInt(this.backgroundDrawable, "alpha", 0);
                        objArr2[1] = ObjectAnimatorProxy.ofFloat(this.animatingImageView, "alpha", 0.0f);
                        ClippingImageView clippingImageView = this.animatingImageView;
                        String str = "translationY";
                        float[] fArr2 = new float[1];
                        fArr2[0] = this.translationY >= 0.0f ? (float) AndroidUtilities.displaySize.y : (float) (-AndroidUtilities.displaySize.y);
                        objArr2[2] = ObjectAnimatorProxy.ofFloat(clippingImageView, str, fArr2);
                        objArr2[3] = ObjectAnimatorProxy.ofFloat(this.containerView, "alpha", 0.0f);
                        animatorSet.playTogether(objArr2);
                    }
                    this.animationEndRunnable = new Runnable() {
                        public void run() {
                            if (VERSION.SDK_INT >= 18) {
                                PhotoViewer.this.containerView.setLayerType(0, null);
                            }
                            PhotoViewer.this.animationInProgress = 0;
                            PhotoViewer.this.onPhotoClosed(object);
                        }
                    };
                    animatorSet.setDuration(200);
                    animatorSet.addListener(new AnimatorListenerAdapterProxy() {

                        class C11491 implements Runnable {
                            C11491() {
                            }

                            public void run() {
                                if (PhotoViewer.this.animationEndRunnable != null) {
                                    PhotoViewer.this.animationEndRunnable.run();
                                    PhotoViewer.this.animationEndRunnable = null;
                                }
                            }
                        }

                        public void onAnimationEnd(Object animation) {
                            AndroidUtilities.runOnUIThread(new C11491());
                        }

                        public void onAnimationCancel(Object animation) {
                            onAnimationEnd(animation);
                        }
                    });
                    this.transitionAnimationStartTime = System.currentTimeMillis();
                    if (VERSION.SDK_INT >= 18) {
                        this.containerView.setLayerType(2, null);
                    }
                    animatorSet.start();
                    return;
                }
                animatorSet = new AnimatorSetProxy();
                objArr = new Object[4];
                objArr[0] = ObjectAnimatorProxy.ofFloat(this.containerView, "scaleX", 0.9f);
                objArr[1] = ObjectAnimatorProxy.ofFloat(this.containerView, "scaleY", 0.9f);
                objArr[2] = ObjectAnimatorProxy.ofInt(this.backgroundDrawable, "alpha", 0);
                objArr[3] = ObjectAnimatorProxy.ofFloat(this.containerView, "alpha", 0.0f);
                animatorSet.playTogether(objArr);
                this.animationInProgress = 2;
                this.animationEndRunnable = new Runnable() {
                    public void run() {
                        if (PhotoViewer.this.containerView != null) {
                            if (VERSION.SDK_INT >= 18) {
                                PhotoViewer.this.containerView.setLayerType(0, null);
                            }
                            PhotoViewer.this.animationInProgress = 0;
                            PhotoViewer.this.onPhotoClosed(object);
                            ViewProxy.setScaleX(PhotoViewer.this.containerView, 1.0f);
                            ViewProxy.setScaleY(PhotoViewer.this.containerView, 1.0f);
                            PhotoViewer.this.containerView.clearAnimation();
                        }
                    }
                };
                animatorSet.setDuration(200);
                animatorSet.addListener(new AnimatorListenerAdapterProxy() {
                    public void onAnimationEnd(Object animation) {
                        if (PhotoViewer.this.animationEndRunnable != null) {
                            PhotoViewer.this.animationEndRunnable.run();
                            PhotoViewer.this.animationEndRunnable = null;
                        }
                    }
                });
                this.transitionAnimationStartTime = System.currentTimeMillis();
                if (VERSION.SDK_INT >= 18) {
                    this.containerView.setLayerType(2, null);
                }
                animatorSet.start();
                return;
            }
            return;
        }
        if (this.currentEditMode == 1) {
            this.photoCropView.cancelAnimationRunnable();
        }
        switchToEditMode(0);
    }

    public void destroyPhotoViewer() {
        if (this.parentActivity != null && this.windowView != null) {
            try {
                if (this.windowView.getParent() != null) {
                    ((WindowManager) this.parentActivity.getSystemService("window")).removeViewImmediate(this.windowView);
                }
                this.windowView = null;
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
            if (this.captionEditText != null) {
                this.captionEditText.onDestroy();
            }
            Instance = null;
        }
    }

    private void onPhotoClosed(PlaceProviderObject object) {
        this.isVisible = false;
        this.disableShowCheck = true;
        this.currentMessageObject = null;
        this.currentFileLocation = null;
        this.currentPathObject = null;
        this.currentThumb = null;
        if (this.gifDrawable != null) {
            this.gifDrawable.recycle();
            this.gifDrawable = null;
        }
        for (int a = 0; a < 3; a++) {
            if (this.radialProgressViews[a] != null) {
                this.radialProgressViews[a].setBackgroundState(-1, false);
            }
        }
        this.centerImage.setImageBitmap((Bitmap) null);
        this.leftImage.setImageBitmap((Bitmap) null);
        this.rightImage.setImageBitmap((Bitmap) null);
        this.containerView.post(new Runnable() {
            public void run() {
                PhotoViewer.this.animatingImageView.setImageBitmap(null);
                try {
                    if (PhotoViewer.this.windowView.getParent() != null) {
                        ((WindowManager) PhotoViewer.this.parentActivity.getSystemService("window")).removeView(PhotoViewer.this.windowView);
                    }
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
        });
        if (this.placeProvider != null) {
            this.placeProvider.willHidePhotoViewer();
        }
        this.placeProvider = null;
        this.disableShowCheck = false;
        if (object != null) {
            object.imageReceiver.setVisible(true, true);
        }
    }

    private void redraw(final int count) {
        if (count < 6 && this.containerView != null) {
            this.containerView.invalidate();
            AndroidUtilities.runOnUIThread(new Runnable() {
                public void run() {
                    PhotoViewer.this.redraw(count + 1);
                }
            }, 100);
        }
    }

    public void onResume() {
        redraw(0);
    }

    public boolean isVisible() {
        return this.isVisible && this.placeProvider != null;
    }

    private void updateMinMax(float scale) {
        int maxW = ((int) ((((float) this.centerImage.getImageWidth()) * scale) - ((float) getContainerViewWidth()))) / 2;
        int maxH = ((int) ((((float) this.centerImage.getImageHeight()) * scale) - ((float) getContainerViewHeight()))) / 2;
        if (maxW > 0) {
            this.minX = (float) (-maxW);
            this.maxX = (float) maxW;
        } else {
            this.maxX = 0.0f;
            this.minX = 0.0f;
        }
        if (maxH > 0) {
            this.minY = (float) (-maxH);
            this.maxY = (float) maxH;
        } else {
            this.maxY = 0.0f;
            this.minY = 0.0f;
        }
        if (this.currentEditMode == 1) {
            this.maxX += this.photoCropView.getLimitX();
            this.maxY += this.photoCropView.getLimitY();
            this.minX -= this.photoCropView.getLimitWidth();
            this.minY -= this.photoCropView.getLimitHeight();
        }
    }

    private int getAdditionX() {
        if (this.currentEditMode != 0) {
            return AndroidUtilities.dp(14.0f);
        }
        return 0;
    }

    private int getAdditionY() {
        if (this.currentEditMode != 0) {
            return AndroidUtilities.dp(14.0f);
        }
        return 0;
    }

    private int getContainerViewWidth() {
        return getContainerViewWidth(this.currentEditMode);
    }

    private int getContainerViewWidth(int mode) {
        int width = this.containerView.getWidth();
        if (mode != 0) {
            return width - AndroidUtilities.dp(28.0f);
        }
        return width;
    }

    private int getContainerViewHeight() {
        return getContainerViewHeight(this.currentEditMode);
    }

    private int getContainerViewHeight(int mode) {
        int height = AndroidUtilities.displaySize.y - AndroidUtilities.statusBarHeight;
        if (mode == 1) {
            return height - AndroidUtilities.dp(76.0f);
        }
        if (mode == 2) {
            return height - AndroidUtilities.dp(154.0f);
        }
        return height;
    }

    private boolean onTouchEvent(MotionEvent ev) {
        if (this.animationInProgress != 0 || this.animationStartTime != 0) {
            return false;
        }
        if (this.currentEditMode == 2) {
            this.photoFilterView.onTouch(ev);
            return true;
        }
        if (this.currentEditMode == 1) {
            if (ev.getPointerCount() != 1) {
                this.photoCropView.onTouch(null);
            } else if (this.photoCropView.onTouch(ev)) {
                updateMinMax(this.scale);
                return true;
            }
        }
        if (this.captionEditText.isPopupShowing() || this.captionEditText.isKeyboardVisible()) {
            return true;
        }
        if (this.currentEditMode == 0 && ev.getPointerCount() == 1 && this.gestureDetector.onTouchEvent(ev) && this.doubleTap) {
            this.doubleTap = false;
            this.moving = false;
            this.zooming = false;
            checkMinMax(false);
            return true;
        }
        if (ev.getActionMasked() == 0 || ev.getActionMasked() == 5) {
            if (this.currentEditMode == 1) {
                this.photoCropView.cancelAnimationRunnable();
            }
            this.discardTap = false;
            if (!this.scroller.isFinished()) {
                this.scroller.abortAnimation();
            }
            if (!(this.draggingDown || this.changingPage)) {
                if (this.canZoom && ev.getPointerCount() == 2) {
                    this.pinchStartDistance = (float) Math.hypot((double) (ev.getX(1) - ev.getX(0)), (double) (ev.getY(1) - ev.getY(0)));
                    this.pinchStartScale = this.scale;
                    this.pinchCenterX = (ev.getX(0) + ev.getX(1)) / 2.0f;
                    this.pinchCenterY = (ev.getY(0) + ev.getY(1)) / 2.0f;
                    this.pinchStartX = this.translationX;
                    this.pinchStartY = this.translationY;
                    this.zooming = true;
                    this.moving = false;
                    if (this.velocityTracker != null) {
                        this.velocityTracker.clear();
                    }
                } else if (ev.getPointerCount() == 1) {
                    this.moveStartX = ev.getX();
                    float y = ev.getY();
                    this.moveStartY = y;
                    this.dragY = y;
                    this.draggingDown = false;
                    this.canDragDown = true;
                    if (this.velocityTracker != null) {
                        this.velocityTracker.clear();
                    }
                }
            }
        } else if (ev.getActionMasked() == 2) {
            if (this.currentEditMode == 1) {
                this.photoCropView.cancelAnimationRunnable();
            }
            if (this.canZoom && ev.getPointerCount() == 2 && !this.draggingDown && this.zooming && !this.changingPage) {
                this.discardTap = true;
                this.scale = (((float) Math.hypot((double) (ev.getX(1) - ev.getX(0)), (double) (ev.getY(1) - ev.getY(0)))) / this.pinchStartDistance) * this.pinchStartScale;
                this.translationX = (this.pinchCenterX - ((float) (getContainerViewWidth() / 2))) - (((this.pinchCenterX - ((float) (getContainerViewWidth() / 2))) - this.pinchStartX) * (this.scale / this.pinchStartScale));
                this.translationY = (this.pinchCenterY - ((float) (getContainerViewHeight() / 2))) - (((this.pinchCenterY - ((float) (getContainerViewHeight() / 2))) - this.pinchStartY) * (this.scale / this.pinchStartScale));
                updateMinMax(this.scale);
                this.containerView.invalidate();
            } else if (ev.getPointerCount() == 1) {
                if (this.velocityTracker != null) {
                    this.velocityTracker.addMovement(ev);
                }
                float dx = Math.abs(ev.getX() - this.moveStartX);
                float dy = Math.abs(ev.getY() - this.dragY);
                if (dx > ((float) AndroidUtilities.dp(3.0f)) || dy > ((float) AndroidUtilities.dp(3.0f))) {
                    this.discardTap = true;
                }
                if (!(this.placeProvider instanceof EmptyPhotoViewerProvider) && this.currentEditMode == 0 && this.canDragDown && !this.draggingDown && this.scale == 1.0f && dy >= ((float) AndroidUtilities.dp(BitmapDescriptorFactory.HUE_ORANGE)) && dy / 2.0f > dx) {
                    this.draggingDown = true;
                    this.moving = false;
                    this.dragY = ev.getY();
                    if (this.isActionBarVisible && this.canShowBottom) {
                        toggleActionBar(false, true);
                    } else if (this.pickerView.getVisibility() == 0) {
                        toggleActionBar(false, true);
                        toggleCheckImageView(false);
                    }
                    return true;
                } else if (this.draggingDown) {
                    this.translationY = ev.getY() - this.dragY;
                    this.containerView.invalidate();
                } else if (this.invalidCoords || this.animationStartTime != 0) {
                    this.invalidCoords = false;
                    this.moveStartX = ev.getX();
                    this.moveStartY = ev.getY();
                } else {
                    float moveDx = this.moveStartX - ev.getX();
                    float moveDy = this.moveStartY - ev.getY();
                    if (this.moving || this.currentEditMode != 0 || ((this.scale == 1.0f && Math.abs(moveDy) + ((float) AndroidUtilities.dp(12.0f)) < Math.abs(moveDx)) || this.scale != 1.0f)) {
                        if (!this.moving) {
                            moveDx = 0.0f;
                            moveDy = 0.0f;
                            this.moving = true;
                            this.canDragDown = false;
                        }
                        this.moveStartX = ev.getX();
                        this.moveStartY = ev.getY();
                        updateMinMax(this.scale);
                        if ((this.translationX < this.minX && !(this.currentEditMode == 0 && this.rightImage.hasImage())) || (this.translationX > this.maxX && !(this.currentEditMode == 0 && this.leftImage.hasImage()))) {
                            moveDx /= 3.0f;
                        }
                        if (this.maxY == 0.0f && this.minY == 0.0f && this.currentEditMode == 0) {
                            if (this.translationY - moveDy < this.minY) {
                                this.translationY = this.minY;
                                moveDy = 0.0f;
                            } else if (this.translationY - moveDy > this.maxY) {
                                this.translationY = this.maxY;
                                moveDy = 0.0f;
                            }
                        } else if (this.translationY < this.minY || this.translationY > this.maxY) {
                            moveDy /= 3.0f;
                        }
                        this.translationX -= moveDx;
                        if (!(this.scale == 1.0f && this.currentEditMode == 0)) {
                            this.translationY -= moveDy;
                        }
                        this.containerView.invalidate();
                    }
                }
            }
        } else if (ev.getActionMasked() == 3 || ev.getActionMasked() == 1 || ev.getActionMasked() == 6) {
            if (this.currentEditMode == 1) {
                this.photoCropView.startAnimationRunnable();
            }
            if (this.zooming) {
                this.invalidCoords = true;
                if (this.scale < 1.0f) {
                    updateMinMax(1.0f);
                    animateTo(1.0f, 0.0f, 0.0f, true);
                } else if (this.scale > 3.0f) {
                    float atx = (this.pinchCenterX - ((float) (getContainerViewWidth() / 2))) - (((this.pinchCenterX - ((float) (getContainerViewWidth() / 2))) - this.pinchStartX) * (3.0f / this.pinchStartScale));
                    float aty = (this.pinchCenterY - ((float) (getContainerViewHeight() / 2))) - (((this.pinchCenterY - ((float) (getContainerViewHeight() / 2))) - this.pinchStartY) * (3.0f / this.pinchStartScale));
                    updateMinMax(3.0f);
                    if (atx < this.minX) {
                        atx = this.minX;
                    } else if (atx > this.maxX) {
                        atx = this.maxX;
                    }
                    if (aty < this.minY) {
                        aty = this.minY;
                    } else if (aty > this.maxY) {
                        aty = this.maxY;
                    }
                    animateTo(3.0f, atx, aty, true);
                } else {
                    checkMinMax(true);
                }
                this.zooming = false;
            } else if (this.draggingDown) {
                if (Math.abs(this.dragY - ev.getY()) > ((float) getContainerViewHeight()) / 6.0f) {
                    closePhoto(true, false);
                } else {
                    if (this.pickerView.getVisibility() == 0) {
                        toggleActionBar(true, true);
                        toggleCheckImageView(true);
                    }
                    animateTo(1.0f, 0.0f, 0.0f, false);
                }
                this.draggingDown = false;
            } else if (this.moving) {
                float moveToX = this.translationX;
                float moveToY = this.translationY;
                updateMinMax(this.scale);
                this.moving = false;
                this.canDragDown = true;
                float velocity = 0.0f;
                if (this.velocityTracker != null && this.scale == 1.0f) {
                    this.velocityTracker.computeCurrentVelocity(LocationStatusCodes.GEOFENCE_NOT_AVAILABLE);
                    velocity = this.velocityTracker.getXVelocity();
                }
                if (this.currentEditMode == 0) {
                    if ((this.translationX < this.minX - ((float) (getContainerViewWidth() / 3)) || velocity < ((float) (-AndroidUtilities.dp(650.0f)))) && this.rightImage.hasImage()) {
                        goToNext();
                        return true;
                    } else if ((this.translationX > this.maxX + ((float) (getContainerViewWidth() / 3)) || velocity > ((float) AndroidUtilities.dp(650.0f))) && this.leftImage.hasImage()) {
                        goToPrev();
                        return true;
                    }
                }
                if (this.translationX < this.minX) {
                    moveToX = this.minX;
                } else if (this.translationX > this.maxX) {
                    moveToX = this.maxX;
                }
                if (this.translationY < this.minY) {
                    moveToY = this.minY;
                } else if (this.translationY > this.maxY) {
                    moveToY = this.maxY;
                }
                animateTo(this.scale, moveToX, moveToY, false);
            }
        }
        return false;
    }

    private void checkMinMax(boolean zoom) {
        float moveToX = this.translationX;
        float moveToY = this.translationY;
        updateMinMax(this.scale);
        if (this.translationX < this.minX) {
            moveToX = this.minX;
        } else if (this.translationX > this.maxX) {
            moveToX = this.maxX;
        }
        if (this.translationY < this.minY) {
            moveToY = this.minY;
        } else if (this.translationY > this.maxY) {
            moveToY = this.maxY;
        }
        animateTo(this.scale, moveToX, moveToY, zoom);
    }

    private void goToNext() {
        float extra = 0.0f;
        if (this.scale != 1.0f) {
            extra = ((float) ((getContainerViewWidth() - this.centerImage.getImageWidth()) / 2)) * this.scale;
        }
        this.switchImageAfterAnimation = 1;
        animateTo(this.scale, ((this.minX - ((float) getContainerViewWidth())) - extra) - ((float) (PAGE_SPACING / 2)), this.translationY, false);
    }

    private void goToPrev() {
        float extra = 0.0f;
        if (this.scale != 1.0f) {
            extra = ((float) ((getContainerViewWidth() - this.centerImage.getImageWidth()) / 2)) * this.scale;
        }
        this.switchImageAfterAnimation = 2;
        animateTo(this.scale, ((this.maxX + ((float) getContainerViewWidth())) + extra) + ((float) (PAGE_SPACING / 2)), this.translationY, false);
    }

    private void animateTo(float newScale, float newTx, float newTy, boolean isZoom) {
        animateTo(newScale, newTx, newTy, isZoom, Callback.DEFAULT_SWIPE_ANIMATION_DURATION);
    }

    private void animateTo(float newScale, float newTx, float newTy, boolean isZoom, int duration) {
        if (this.scale != newScale || this.translationX != newTx || this.translationY != newTy) {
            this.zoomAnimation = isZoom;
            this.animateToScale = newScale;
            this.animateToX = newTx;
            this.animateToY = newTy;
            this.animationStartTime = System.currentTimeMillis();
            this.imageMoveAnimation = new AnimatorSetProxy();
            this.imageMoveAnimation.playTogether(ObjectAnimatorProxy.ofFloat(this, "animationValue", 0.0f, 1.0f));
            this.imageMoveAnimation.setInterpolator(this.interpolator);
            this.imageMoveAnimation.setDuration((long) duration);
            this.imageMoveAnimation.addListener(new AnimatorListenerAdapterProxy() {
                public void onAnimationEnd(Object animation) {
                    PhotoViewer.this.imageMoveAnimation = null;
                    PhotoViewer.this.containerView.invalidate();
                }
            });
            this.imageMoveAnimation.start();
        }
    }

    public void setAnimationValue(float value) {
        this.animationValue = value;
        this.containerView.invalidate();
    }

    public float getAnimationValue() {
        return this.animationValue;
    }

    private void onDraw(Canvas canvas) {
        if (this.animationInProgress == 1) {
            return;
        }
        if (this.isVisible || this.animationInProgress == 2) {
            float currentScale;
            float currentTranslationY;
            float currentTranslationX;
            float tranlateX;
            float scaleDiff;
            float alpha;
            int bitmapWidth;
            int bitmapHeight;
            float scaleX;
            float scaleY;
            float scale;
            int width;
            int height;
            float aty = GroundOverlayOptions.NO_DIMENSION;
            if (this.imageMoveAnimation != null) {
                if (!this.scroller.isFinished()) {
                    this.scroller.abortAnimation();
                }
                float ts = this.scale + ((this.animateToScale - this.scale) * this.animationValue);
                float tx = this.translationX + ((this.animateToX - this.translationX) * this.animationValue);
                float ty = this.translationY + ((this.animateToY - this.translationY) * this.animationValue);
                if (this.currentEditMode == 1) {
                    this.photoCropView.setAnimationProgress(this.animationValue);
                }
                if (this.animateToScale == 1.0f && this.scale == 1.0f && this.translationX == 0.0f) {
                    aty = ty;
                }
                currentScale = ts;
                currentTranslationY = ty;
                currentTranslationX = tx;
                this.containerView.invalidate();
            } else {
                if (this.animationStartTime != 0) {
                    this.translationX = this.animateToX;
                    this.translationY = this.animateToY;
                    this.scale = this.animateToScale;
                    this.animationStartTime = 0;
                    if (this.currentEditMode == 1) {
                        this.photoCropView.setAnimationProgress(1.0f);
                    }
                    updateMinMax(this.scale);
                    this.zoomAnimation = false;
                }
                if (!this.scroller.isFinished() && this.scroller.computeScrollOffset()) {
                    if (((float) this.scroller.getStartX()) < this.maxX && ((float) this.scroller.getStartX()) > this.minX) {
                        this.translationX = (float) this.scroller.getCurrX();
                    }
                    if (((float) this.scroller.getStartY()) < this.maxY && ((float) this.scroller.getStartY()) > this.minY) {
                        this.translationY = (float) this.scroller.getCurrY();
                    }
                    this.containerView.invalidate();
                }
                if (this.switchImageAfterAnimation != 0) {
                    if (this.switchImageAfterAnimation == 1) {
                        setImageIndex(this.currentIndex + 1, false);
                    } else if (this.switchImageAfterAnimation == 2) {
                        setImageIndex(this.currentIndex - 1, false);
                    }
                    this.switchImageAfterAnimation = 0;
                }
                currentScale = this.scale;
                currentTranslationY = this.translationY;
                currentTranslationX = this.translationX;
                if (!this.moving) {
                    aty = this.translationY;
                }
            }
            if (this.currentEditMode != 0 || this.scale != 1.0f || aty == GroundOverlayOptions.NO_DIMENSION || this.zoomAnimation) {
                this.backgroundDrawable.setAlpha(255);
            } else {
                float maxValue = ((float) getContainerViewHeight()) / 4.0f;
                this.backgroundDrawable.setAlpha((int) Math.max(127.0f, 255.0f * (1.0f - (Math.min(Math.abs(aty), maxValue) / maxValue))));
            }
            ImageReceiver sideImage = null;
            if (this.currentEditMode == 0) {
                if (!(this.scale < 1.0f || this.zoomAnimation || this.zooming)) {
                    if (currentTranslationX > this.maxX + ((float) AndroidUtilities.dp(5.0f))) {
                        sideImage = this.leftImage;
                    } else if (currentTranslationX < this.minX - ((float) AndroidUtilities.dp(5.0f))) {
                        sideImage = this.rightImage;
                    }
                }
                this.changingPage = sideImage != null;
            }
            if (sideImage == this.rightImage) {
                tranlateX = currentTranslationX;
                scaleDiff = 0.0f;
                alpha = 1.0f;
                if (!this.zoomAnimation && tranlateX < this.minX) {
                    alpha = Math.min(1.0f, (this.minX - tranlateX) / ((float) canvas.getWidth()));
                    scaleDiff = (1.0f - alpha) * 0.3f;
                    tranlateX = (float) ((-canvas.getWidth()) - (PAGE_SPACING / 2));
                }
                if (sideImage.getBitmap() != null) {
                    canvas.save();
                    canvas.translate((float) (getContainerViewWidth() / 2), (float) (getContainerViewHeight() / 2));
                    canvas.translate(((float) (canvas.getWidth() + (PAGE_SPACING / 2))) + tranlateX, 0.0f);
                    canvas.scale(1.0f - scaleDiff, 1.0f - scaleDiff);
                    bitmapWidth = sideImage.getBitmapWidth();
                    bitmapHeight = sideImage.getBitmapHeight();
                    scaleX = ((float) getContainerViewWidth()) / ((float) bitmapWidth);
                    scaleY = ((float) getContainerViewHeight()) / ((float) bitmapHeight);
                    if (scaleX > scaleY) {
                        scale = scaleY;
                    } else {
                        scale = scaleX;
                    }
                    width = (int) (((float) bitmapWidth) * scale);
                    height = (int) (((float) bitmapHeight) * scale);
                    sideImage.setAlpha(alpha);
                    sideImage.setImageCoords((-width) / 2, (-height) / 2, width, height);
                    sideImage.draw(canvas);
                    canvas.restore();
                }
                canvas.save();
                canvas.translate(tranlateX, currentTranslationY / currentScale);
                canvas.translate(((((float) canvas.getWidth()) * (this.scale + 1.0f)) + ((float) PAGE_SPACING)) / 2.0f, (-currentTranslationY) / currentScale);
                this.radialProgressViews[1].setScale(1.0f - scaleDiff);
                this.radialProgressViews[1].setAlpha(alpha);
                this.radialProgressViews[1].onDraw(canvas);
                canvas.restore();
            }
            tranlateX = currentTranslationX;
            scaleDiff = 0.0f;
            alpha = 1.0f;
            if (!this.zoomAnimation && tranlateX > this.maxX && this.currentEditMode == 0) {
                alpha = Math.min(1.0f, (tranlateX - this.maxX) / ((float) canvas.getWidth()));
                scaleDiff = alpha * 0.3f;
                alpha = 1.0f - alpha;
                tranlateX = this.maxX;
            }
            if (this.centerImage.getBitmap() != null) {
                canvas.save();
                canvas.translate((float) ((getContainerViewWidth() / 2) + getAdditionX()), (float) ((getContainerViewHeight() / 2) + getAdditionY()));
                canvas.translate(tranlateX, currentTranslationY);
                canvas.scale(currentScale - scaleDiff, currentScale - scaleDiff);
                if (this.currentEditMode == 1) {
                    this.photoCropView.setBitmapParams(currentScale, tranlateX, currentTranslationY);
                }
                bitmapWidth = this.centerImage.getBitmapWidth();
                bitmapHeight = this.centerImage.getBitmapHeight();
                scaleX = ((float) getContainerViewWidth()) / ((float) bitmapWidth);
                scaleY = ((float) getContainerViewHeight()) / ((float) bitmapHeight);
                scale = scaleX > scaleY ? scaleY : scaleX;
                width = (int) (((float) bitmapWidth) * scale);
                height = (int) (((float) bitmapHeight) * scale);
                if (this.gifDrawable != null) {
                    canvas.save();
                    this.gifDrawable.setAlpha((int) (255.0f * alpha));
                    this.gifDrawable.setBounds((-width) / 2, (-height) / 2, width / 2, height / 2);
                    this.gifDrawable.draw(canvas);
                    canvas.restore();
                } else {
                    this.centerImage.setAlpha(alpha);
                    this.centerImage.setImageCoords((-width) / 2, (-height) / 2, width, height);
                    this.centerImage.draw(canvas);
                }
                canvas.restore();
            }
            canvas.save();
            canvas.translate(tranlateX, currentTranslationY / currentScale);
            this.radialProgressViews[0].setScale(1.0f - scaleDiff);
            this.radialProgressViews[0].setAlpha(alpha);
            this.radialProgressViews[0].onDraw(canvas);
            canvas.restore();
            if (sideImage == this.leftImage) {
                if (sideImage.getBitmap() != null) {
                    canvas.save();
                    canvas.translate((float) (getContainerViewWidth() / 2), (float) (getContainerViewHeight() / 2));
                    canvas.translate(((-((((float) canvas.getWidth()) * (this.scale + 1.0f)) + ((float) PAGE_SPACING))) / 2.0f) + currentTranslationX, 0.0f);
                    bitmapWidth = sideImage.getBitmapWidth();
                    bitmapHeight = sideImage.getBitmapHeight();
                    scaleX = ((float) getContainerViewWidth()) / ((float) bitmapWidth);
                    scaleY = ((float) getContainerViewHeight()) / ((float) bitmapHeight);
                    if (scaleX > scaleY) {
                        scale = scaleY;
                    } else {
                        scale = scaleX;
                    }
                    width = (int) (((float) bitmapWidth) * scale);
                    height = (int) (((float) bitmapHeight) * scale);
                    sideImage.setAlpha(1.0f);
                    sideImage.setImageCoords((-width) / 2, (-height) / 2, width, height);
                    sideImage.draw(canvas);
                    canvas.restore();
                }
                canvas.save();
                canvas.translate(currentTranslationX, currentTranslationY / currentScale);
                canvas.translate((-((((float) canvas.getWidth()) * (this.scale + 1.0f)) + ((float) PAGE_SPACING))) / 2.0f, (-currentTranslationY) / currentScale);
                this.radialProgressViews[2].setScale(1.0f);
                this.radialProgressViews[2].setAlpha(1.0f);
                this.radialProgressViews[2].onDraw(canvas);
                canvas.restore();
            }
        }
    }

    @SuppressLint({"DrawAllocation"})
    private void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed) {
            this.scale = 1.0f;
            this.translationX = 0.0f;
            this.translationY = 0.0f;
            updateMinMax(this.scale);
            if (this.checkImageView != null) {
                this.checkImageView.post(new Runnable() {
                    public void run() {
                        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) PhotoViewer.this.checkImageView.getLayoutParams();
                        int rotation = ((WindowManager) ApplicationLoader.applicationContext.getSystemService("window")).getDefaultDisplay().getRotation();
                        float f = (rotation == 3 || rotation == 1) ? 58.0f : 68.0f;
                        layoutParams.topMargin = AndroidUtilities.dp(f);
                        PhotoViewer.this.checkImageView.setLayoutParams(layoutParams);
                    }
                });
            }
        }
    }

    private void onActionClick() {
        if (this.currentMessageObject != null && this.currentFileNames[0] != null) {
            boolean loadFile = false;
            Intent intent;
            if (this.currentMessageObject.messageOwner.attachPath == null || this.currentMessageObject.messageOwner.attachPath.length() == 0) {
                File cacheFile = FileLoader.getPathToMessage(this.currentMessageObject.messageOwner);
                if (cacheFile.exists()) {
                    intent = new Intent("android.intent.action.VIEW");
                    intent.setDataAndType(Uri.fromFile(cacheFile), "video/mp4");
                    this.parentActivity.startActivityForResult(intent, 500);
                } else {
                    loadFile = true;
                }
            } else {
                File f = new File(this.currentMessageObject.messageOwner.attachPath);
                if (f.exists()) {
                    intent = new Intent("android.intent.action.VIEW");
                    intent.setDataAndType(Uri.fromFile(f), "video/mp4");
                    this.parentActivity.startActivityForResult(intent, 500);
                } else {
                    loadFile = true;
                }
            }
            if (!loadFile) {
                return;
            }
            if (FileLoader.getInstance().isLoadingFile(this.currentFileNames[0])) {
                FileLoader.getInstance().cancelLoadFile(this.currentMessageObject.messageOwner.media.video);
            } else {
                FileLoader.getInstance().loadFile(this.currentMessageObject.messageOwner.media.video, true);
            }
        }
    }

    public boolean onDown(MotionEvent e) {
        return false;
    }

    public void onShowPress(MotionEvent e) {
    }

    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    public void onLongPress(MotionEvent e) {
    }

    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (this.scale != 1.0f) {
            this.scroller.abortAnimation();
            this.scroller.fling(Math.round(this.translationX), Math.round(this.translationY), Math.round(velocityX), Math.round(velocityY), (int) this.minX, (int) this.maxX, (int) this.minY, (int) this.maxY);
            this.containerView.postInvalidate();
        }
        return false;
    }

    public boolean onSingleTapConfirmed(MotionEvent e) {
        boolean z = false;
        if (this.discardTap) {
            return false;
        }
        if (this.canShowBottom) {
            if (!(this.radialProgressViews[0] == null || this.containerView == null)) {
                int state = this.radialProgressViews[0].backgroundState;
                if (state > 0 && state <= 3) {
                    float x = e.getX();
                    float y = e.getY();
                    if (x >= ((float) (getContainerViewWidth() - AndroidUtilities.dp(64.0f))) / 2.0f && x <= ((float) (getContainerViewWidth() + AndroidUtilities.dp(64.0f))) / 2.0f && y >= ((float) (getContainerViewHeight() - AndroidUtilities.dp(64.0f))) / 2.0f && y <= ((float) (getContainerViewHeight() + AndroidUtilities.dp(64.0f))) / 2.0f) {
                        onActionClick();
                        checkProgress(0, true);
                        return true;
                    }
                }
            }
            if (!this.isActionBarVisible) {
                z = true;
            }
            toggleActionBar(z, true);
            return true;
        } else if (this.sendPhotoType != 0) {
            return true;
        } else {
            this.checkImageView.performClick();
            return true;
        }
    }

    public boolean onDoubleTap(MotionEvent e) {
        if (!this.canZoom || (this.scale == 1.0f && (this.translationY != 0.0f || this.translationX != 0.0f))) {
            return false;
        }
        if (this.animationStartTime != 0 || this.animationInProgress != 0) {
            return false;
        }
        if (this.scale == 1.0f) {
            float atx = (e.getX() - ((float) (getContainerViewWidth() / 2))) - (((e.getX() - ((float) (getContainerViewWidth() / 2))) - this.translationX) * (3.0f / this.scale));
            float aty = (e.getY() - ((float) (getContainerViewHeight() / 2))) - (((e.getY() - ((float) (getContainerViewHeight() / 2))) - this.translationY) * (3.0f / this.scale));
            updateMinMax(3.0f);
            if (atx < this.minX) {
                atx = this.minX;
            } else if (atx > this.maxX) {
                atx = this.maxX;
            }
            if (aty < this.minY) {
                aty = this.minY;
            } else if (aty > this.maxY) {
                aty = this.maxY;
            }
            animateTo(3.0f, atx, aty, true);
        } else {
            animateTo(1.0f, 0.0f, 0.0f, true);
        }
        this.doubleTap = true;
        return true;
    }

    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }
}
