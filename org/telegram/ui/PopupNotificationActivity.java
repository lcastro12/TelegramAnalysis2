package org.telegram.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.google.android.gms.location.LocationStatusCodes;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import java.util.ArrayList;
import java.util.Locale;
import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.C0553R;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.NotificationsController;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.PhotoSize;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.ChatActivityEnterView;
import org.telegram.ui.Components.ChatActivityEnterView.ChatActivityEnterViewDelegate;
import org.telegram.ui.Components.FrameLayoutFixed;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.PopupAudioView;
import org.telegram.ui.Components.RecordStatusDrawable;
import org.telegram.ui.Components.SizeNotifierFrameLayout;
import org.telegram.ui.Components.TypingDotsDrawable;

public class PopupNotificationActivity extends Activity implements NotificationCenterDelegate {
    private ActionBar actionBar;
    private boolean animationInProgress = false;
    private long animationStartTime = 0;
    private ArrayList<ViewGroup> audioViews = new ArrayList();
    private FrameLayout avatarContainer;
    private BackupImageView avatarImageView;
    private ViewGroup centerView;
    private ChatActivityEnterView chatActivityEnterView;
    private int classGuid;
    private TextView countText;
    private Chat currentChat;
    private int currentMessageNum = 0;
    private MessageObject currentMessageObject = null;
    private User currentUser;
    private boolean finished = false;
    private ArrayList<ViewGroup> imageViews = new ArrayList();
    private CharSequence lastPrintString;
    private ViewGroup leftView;
    private ViewGroup messageContainer;
    private float moveStartX = GroundOverlayOptions.NO_DIMENSION;
    private TextView nameTextView;
    private Runnable onAnimationEndRunnable = null;
    private TextView onlineTextView;
    private RelativeLayout popupContainer;
    private RecordStatusDrawable recordStatusDrawable;
    private ViewGroup rightView;
    private boolean startedMoving = false;
    private ArrayList<ViewGroup> textViews = new ArrayList();
    private TypingDotsDrawable typingDotsDrawable;
    private VelocityTracker velocityTracker = null;
    private WakeLock wakeLock = null;

    class C11564 implements OnClickListener {
        C11564() {
        }

        @TargetApi(9)
        public void onClick(DialogInterface dialog, int which) {
            try {
                Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
                intent.setData(Uri.parse("package:" + ApplicationLoader.applicationContext.getPackageName()));
                PopupNotificationActivity.this.startActivity(intent);
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
        }
    }

    class C11575 implements Runnable {
        C11575() {
        }

        public void run() {
            PopupNotificationActivity.this.animationInProgress = false;
            PopupNotificationActivity.this.switchToPreviousMessage();
            AndroidUtilities.unlockOrientation(PopupNotificationActivity.this);
        }
    }

    class C11586 implements Runnable {
        C11586() {
        }

        public void run() {
            PopupNotificationActivity.this.animationInProgress = false;
            PopupNotificationActivity.this.switchToNextMessage();
            AndroidUtilities.unlockOrientation(PopupNotificationActivity.this);
        }
    }

    class C11597 implements Runnable {
        C11597() {
        }

        public void run() {
            PopupNotificationActivity.this.animationInProgress = false;
            PopupNotificationActivity.this.applyViewsLayoutParams(0);
            AndroidUtilities.unlockOrientation(PopupNotificationActivity.this);
        }
    }

    class C11608 implements View.OnClickListener {
        C11608() {
        }

        public void onClick(View v) {
            PopupNotificationActivity.this.openCurrentMessage();
        }
    }

    class C11619 implements View.OnClickListener {
        C11619() {
        }

        public void onClick(View v) {
            PopupNotificationActivity.this.openCurrentMessage();
        }
    }

    class C16492 implements ChatActivityEnterViewDelegate {
        C16492() {
        }

        public void onMessageSend(String message) {
            if (PopupNotificationActivity.this.currentMessageObject != null) {
                if (PopupNotificationActivity.this.currentMessageNum >= 0 && PopupNotificationActivity.this.currentMessageNum < NotificationsController.getInstance().popupMessages.size()) {
                    NotificationsController.getInstance().popupMessages.remove(PopupNotificationActivity.this.currentMessageNum);
                }
                MessagesController.getInstance().markDialogAsRead(PopupNotificationActivity.this.currentMessageObject.getDialogId(), PopupNotificationActivity.this.currentMessageObject.getId(), Math.max(0, PopupNotificationActivity.this.currentMessageObject.getId()), PopupNotificationActivity.this.currentMessageObject.messageOwner.date, true, true);
                PopupNotificationActivity.this.currentMessageObject = null;
                PopupNotificationActivity.this.getNewMessage();
            }
        }

        public void onTextChanged(CharSequence text, boolean big) {
        }

        public void needSendTyping() {
            if (PopupNotificationActivity.this.currentMessageObject != null) {
                MessagesController.getInstance().sendTyping(PopupNotificationActivity.this.currentMessageObject.getDialogId(), 0, PopupNotificationActivity.this.classGuid);
            }
        }

        public void onAttachButtonHidden() {
        }

        public void onAttachButtonShow() {
        }

        public void onWindowSizeChanged(int size) {
        }
    }

    class C16503 extends ActionBarMenuOnItemClick {
        C16503() {
        }

        public void onItemClick(int id) {
            if (id == -1) {
                PopupNotificationActivity.this.onFinish();
                PopupNotificationActivity.this.finish();
            } else if (id == 1) {
                PopupNotificationActivity.this.openCurrentMessage();
            } else if (id == 2) {
                PopupNotificationActivity.this.switchToNextMessage();
            }
        }
    }

    public class FrameLayoutAnimationListener extends FrameLayoutFixed {
        public FrameLayoutAnimationListener(Context context) {
            super(context);
        }

        public FrameLayoutAnimationListener(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public FrameLayoutAnimationListener(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        protected void onAnimationEnd() {
            super.onAnimationEnd();
            if (PopupNotificationActivity.this.onAnimationEndRunnable != null) {
                PopupNotificationActivity.this.onAnimationEndRunnable.run();
                PopupNotificationActivity.this.onAnimationEndRunnable = null;
            }
        }
    }

    private class FrameLayoutTouch extends FrameLayoutFixed {
        public FrameLayoutTouch(Context context) {
            super(context);
        }

        public FrameLayoutTouch(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public FrameLayoutTouch(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        public boolean onInterceptTouchEvent(MotionEvent ev) {
            return PopupNotificationActivity.this.checkTransitionAnimation() || ((PopupNotificationActivity) getContext()).onTouchEventMy(ev);
        }

        public boolean onTouchEvent(MotionEvent ev) {
            return PopupNotificationActivity.this.checkTransitionAnimation() || ((PopupNotificationActivity) getContext()).onTouchEventMy(ev);
        }

        public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            ((PopupNotificationActivity) getContext()).onTouchEventMy(null);
            super.requestDisallowInterceptTouchEvent(disallowIntercept);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            AndroidUtilities.statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        this.classGuid = ConnectionsManager.getInstance().generateClassGuid();
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.appDidLogout);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.pushMessagesUpdated);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.updateInterfaces);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.audioProgressDidChanged);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.audioDidReset);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.contactsDidLoaded);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.emojiDidLoaded);
        this.typingDotsDrawable = new TypingDotsDrawable();
        this.recordStatusDrawable = new RecordStatusDrawable();
        SizeNotifierFrameLayout contentView = new SizeNotifierFrameLayout(this) {
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                int widthMode = MeasureSpec.getMode(widthMeasureSpec);
                int heightMode = MeasureSpec.getMode(heightMeasureSpec);
                int widthSize = MeasureSpec.getSize(widthMeasureSpec);
                int heightSize = MeasureSpec.getSize(heightMeasureSpec);
                setMeasuredDimension(widthSize, heightSize);
                if (getKeyboardHeight() <= AndroidUtilities.dp(20.0f)) {
                    heightSize -= PopupNotificationActivity.this.chatActivityEnterView.getEmojiPadding();
                }
                int childCount = getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View child = getChildAt(i);
                    if (child.getVisibility() != 8) {
                        if (PopupNotificationActivity.this.chatActivityEnterView.isPopupView(child)) {
                            child.measure(MeasureSpec.makeMeasureSpec(widthSize, 1073741824), MeasureSpec.makeMeasureSpec(child.getLayoutParams().height, 1073741824));
                        } else if (PopupNotificationActivity.this.chatActivityEnterView.isRecordCircle(child)) {
                            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                        } else {
                            child.measure(MeasureSpec.makeMeasureSpec(widthSize, 1073741824), MeasureSpec.makeMeasureSpec(Math.max(AndroidUtilities.dp(10.0f), AndroidUtilities.dp(2.0f) + heightSize), 1073741824));
                        }
                    }
                }
            }

            protected void onLayout(boolean changed, int l, int t, int r, int b) {
                int count = getChildCount();
                int paddingBottom = getKeyboardHeight() <= AndroidUtilities.dp(20.0f) ? PopupNotificationActivity.this.chatActivityEnterView.getEmojiPadding() : 0;
                for (int i = 0; i < count; i++) {
                    View child = getChildAt(i);
                    if (child.getVisibility() != 8) {
                        int childLeft;
                        int childTop;
                        LayoutParams lp = (LayoutParams) child.getLayoutParams();
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
                        if (PopupNotificationActivity.this.chatActivityEnterView.isPopupView(child)) {
                            childTop = paddingBottom != 0 ? getMeasuredHeight() - paddingBottom : getMeasuredHeight();
                        } else if (PopupNotificationActivity.this.chatActivityEnterView.isRecordCircle(child)) {
                            childTop = ((PopupNotificationActivity.this.popupContainer.getTop() + PopupNotificationActivity.this.popupContainer.getMeasuredHeight()) - child.getMeasuredHeight()) - lp.bottomMargin;
                            childLeft = ((PopupNotificationActivity.this.popupContainer.getLeft() + PopupNotificationActivity.this.popupContainer.getMeasuredWidth()) - child.getMeasuredWidth()) - lp.rightMargin;
                        }
                        child.layout(childLeft, childTop, childLeft + width, childTop + height);
                    }
                }
                notifyHeightChanged();
            }
        };
        setContentView(contentView);
        contentView.setBackgroundColor(-1728053248);
        RelativeLayout relativeLayout = new RelativeLayout(this);
        contentView.addView(relativeLayout, LayoutHelper.createFrame(-1, GroundOverlayOptions.NO_DIMENSION));
        this.popupContainer = new RelativeLayout(this);
        this.popupContainer.setBackgroundColor(-1);
        relativeLayout.addView(this.popupContainer, LayoutHelper.createRelative(-1, 240, 12, 0, 12, 0, 13));
        if (this.chatActivityEnterView != null) {
            this.chatActivityEnterView.onDestroy();
        }
        this.chatActivityEnterView = new ChatActivityEnterView(this, contentView, null, false);
        this.popupContainer.addView(this.chatActivityEnterView, LayoutHelper.createRelative(-1, -2, 12));
        this.chatActivityEnterView.setDelegate(new C16492());
        this.messageContainer = new FrameLayoutTouch(this);
        this.popupContainer.addView(this.messageContainer, 0);
        this.actionBar = new ActionBar(this);
        this.actionBar.setOccupyStatusBar(false);
        this.actionBar.setBackButtonImage(C0553R.drawable.ic_ab_back);
        this.actionBar.setBackgroundColor(-11242082);
        this.actionBar.setItemsBackground(C0553R.drawable.bar_selector);
        this.popupContainer.addView(this.actionBar);
        ViewGroup.LayoutParams layoutParams = this.actionBar.getLayoutParams();
        layoutParams.width = -1;
        this.actionBar.setLayoutParams(layoutParams);
        this.countText = (TextView) this.actionBar.createMenu().addItemResource(2, C0553R.layout.popup_count_layout).findViewById(C0553R.id.count_text);
        this.avatarContainer = new FrameLayoutFixed(this);
        this.avatarContainer.setBackgroundResource(C0553R.drawable.bar_selector);
        this.avatarContainer.setPadding(AndroidUtilities.dp(4.0f), 0, AndroidUtilities.dp(4.0f), 0);
        this.actionBar.addView(this.avatarContainer);
        LayoutParams layoutParams2 = (LayoutParams) this.avatarContainer.getLayoutParams();
        layoutParams2.height = -1;
        layoutParams2.width = -2;
        layoutParams2.rightMargin = AndroidUtilities.dp(48.0f);
        layoutParams2.leftMargin = AndroidUtilities.dp(BitmapDescriptorFactory.HUE_YELLOW);
        layoutParams2.gravity = 51;
        this.avatarContainer.setLayoutParams(layoutParams2);
        this.avatarImageView = new BackupImageView(this);
        this.avatarImageView.setRoundRadius(AndroidUtilities.dp(21.0f));
        this.avatarContainer.addView(this.avatarImageView);
        layoutParams2 = (LayoutParams) this.avatarImageView.getLayoutParams();
        layoutParams2.width = AndroidUtilities.dp(42.0f);
        layoutParams2.height = AndroidUtilities.dp(42.0f);
        layoutParams2.topMargin = AndroidUtilities.dp(3.0f);
        this.avatarImageView.setLayoutParams(layoutParams2);
        this.nameTextView = new TextView(this);
        this.nameTextView.setTextColor(-1);
        this.nameTextView.setTextSize(1, 18.0f);
        this.nameTextView.setLines(1);
        this.nameTextView.setMaxLines(1);
        this.nameTextView.setSingleLine(true);
        this.nameTextView.setEllipsize(TruncateAt.END);
        this.nameTextView.setGravity(3);
        this.nameTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        this.avatarContainer.addView(this.nameTextView);
        layoutParams2 = (LayoutParams) this.nameTextView.getLayoutParams();
        layoutParams2.width = -2;
        layoutParams2.height = -2;
        layoutParams2.leftMargin = AndroidUtilities.dp(54.0f);
        layoutParams2.bottomMargin = AndroidUtilities.dp(22.0f);
        layoutParams2.gravity = 80;
        this.nameTextView.setLayoutParams(layoutParams2);
        this.onlineTextView = new TextView(this);
        this.onlineTextView.setTextColor(-2627337);
        this.onlineTextView.setTextSize(1, 14.0f);
        this.onlineTextView.setLines(1);
        this.onlineTextView.setMaxLines(1);
        this.onlineTextView.setSingleLine(true);
        this.onlineTextView.setEllipsize(TruncateAt.END);
        this.onlineTextView.setGravity(3);
        this.avatarContainer.addView(this.onlineTextView);
        layoutParams2 = (LayoutParams) this.onlineTextView.getLayoutParams();
        layoutParams2.width = -2;
        layoutParams2.height = -2;
        layoutParams2.leftMargin = AndroidUtilities.dp(54.0f);
        layoutParams2.bottomMargin = AndroidUtilities.dp(4.0f);
        layoutParams2.gravity = 80;
        this.onlineTextView.setLayoutParams(layoutParams2);
        this.actionBar.setActionBarMenuOnItemClick(new C16503());
        this.wakeLock = ((PowerManager) ApplicationLoader.applicationContext.getSystemService("power")).newWakeLock(268435462, "screen");
        this.wakeLock.setReferenceCounted(false);
        handleIntent(getIntent());
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        AndroidUtilities.checkDisplaySize();
        fixLayout();
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 3 && grantResults[0] != 0) {
            Builder builder = new Builder(this);
            builder.setTitle(LocaleController.getString("AppName", C0553R.string.AppName));
            builder.setMessage(LocaleController.getString("PermissionNoAudio", C0553R.string.PermissionNoAudio));
            builder.setNegativeButton(LocaleController.getString("PermissionOpenSettings", C0553R.string.PermissionOpenSettings), new C11564());
            builder.setPositiveButton(LocaleController.getString("OK", C0553R.string.OK), null);
            builder.show();
        }
    }

    private void switchToNextMessage() {
        if (NotificationsController.getInstance().popupMessages.size() > 1) {
            if (this.currentMessageNum < NotificationsController.getInstance().popupMessages.size() - 1) {
                this.currentMessageNum++;
            } else {
                this.currentMessageNum = 0;
            }
            this.currentMessageObject = (MessageObject) NotificationsController.getInstance().popupMessages.get(this.currentMessageNum);
            updateInterfaceForCurrentMessage(2);
            this.countText.setText(String.format("%d/%d", new Object[]{Integer.valueOf(this.currentMessageNum + 1), Integer.valueOf(NotificationsController.getInstance().popupMessages.size())}));
        }
    }

    private void switchToPreviousMessage() {
        if (NotificationsController.getInstance().popupMessages.size() > 1) {
            if (this.currentMessageNum > 0) {
                this.currentMessageNum--;
            } else {
                this.currentMessageNum = NotificationsController.getInstance().popupMessages.size() - 1;
            }
            this.currentMessageObject = (MessageObject) NotificationsController.getInstance().popupMessages.get(this.currentMessageNum);
            updateInterfaceForCurrentMessage(1);
            this.countText.setText(String.format("%d/%d", new Object[]{Integer.valueOf(this.currentMessageNum + 1), Integer.valueOf(NotificationsController.getInstance().popupMessages.size())}));
        }
    }

    public boolean checkTransitionAnimation() {
        if (this.animationInProgress && this.animationStartTime < System.currentTimeMillis() - 400) {
            this.animationInProgress = false;
            if (this.onAnimationEndRunnable != null) {
                this.onAnimationEndRunnable.run();
                this.onAnimationEndRunnable = null;
            }
        }
        return this.animationInProgress;
    }

    public boolean onTouchEventMy(MotionEvent motionEvent) {
        if (checkTransitionAnimation()) {
            return false;
        }
        if (motionEvent != null && motionEvent.getAction() == 0) {
            this.moveStartX = motionEvent.getX();
        } else if (motionEvent != null && motionEvent.getAction() == 2) {
            float x = motionEvent.getX();
            diff = (int) (x - this.moveStartX);
            if (!(this.moveStartX == GroundOverlayOptions.NO_DIMENSION || this.startedMoving || Math.abs(diff) <= AndroidUtilities.dp(10.0f))) {
                this.startedMoving = true;
                this.moveStartX = x;
                AndroidUtilities.lockOrientation(this);
                diff = 0;
                if (this.velocityTracker == null) {
                    this.velocityTracker = VelocityTracker.obtain();
                } else {
                    this.velocityTracker.clear();
                }
            }
            if (this.startedMoving) {
                if (this.leftView == null && diff > 0) {
                    diff = 0;
                }
                if (this.rightView == null && diff < 0) {
                    diff = 0;
                }
                if (this.velocityTracker != null) {
                    this.velocityTracker.addMovement(motionEvent);
                }
                applyViewsLayoutParams(diff);
            }
        } else if (motionEvent == null || motionEvent.getAction() == 1 || motionEvent.getAction() == 3) {
            if (motionEvent == null || !this.startedMoving) {
                applyViewsLayoutParams(0);
            } else {
                LayoutParams layoutParams = (LayoutParams) this.centerView.getLayoutParams();
                diff = (int) (motionEvent.getX() - this.moveStartX);
                int width = AndroidUtilities.displaySize.x - AndroidUtilities.dp(24.0f);
                int moveDiff = 0;
                int forceMove = 0;
                View otherView = null;
                if (this.velocityTracker != null) {
                    this.velocityTracker.computeCurrentVelocity(LocationStatusCodes.GEOFENCE_NOT_AVAILABLE);
                    if (this.velocityTracker.getXVelocity() >= 3500.0f) {
                        forceMove = 1;
                    } else if (this.velocityTracker.getXVelocity() <= -3500.0f) {
                        forceMove = 2;
                    }
                }
                if ((forceMove == 1 || diff > width / 3) && this.leftView != null) {
                    moveDiff = width - layoutParams.leftMargin;
                    otherView = this.leftView;
                    this.onAnimationEndRunnable = new C11575();
                } else if ((forceMove == 2 || diff < (-width) / 3) && this.rightView != null) {
                    moveDiff = (-width) - layoutParams.leftMargin;
                    otherView = this.rightView;
                    this.onAnimationEndRunnable = new C11586();
                } else if (layoutParams.leftMargin != 0) {
                    moveDiff = -layoutParams.leftMargin;
                    otherView = diff > 0 ? this.leftView : this.rightView;
                    this.onAnimationEndRunnable = new C11597();
                }
                if (moveDiff != 0) {
                    int time = (int) (Math.abs(((float) moveDiff) / ((float) width)) * 200.0f);
                    TranslateAnimation animation = new TranslateAnimation(0.0f, (float) moveDiff, 0.0f, 0.0f);
                    animation.setDuration((long) time);
                    this.centerView.startAnimation(animation);
                    if (otherView != null) {
                        animation = new TranslateAnimation(0.0f, (float) moveDiff, 0.0f, 0.0f);
                        animation.setDuration((long) time);
                        otherView.startAnimation(animation);
                    }
                    this.animationInProgress = true;
                    this.animationStartTime = System.currentTimeMillis();
                }
            }
            if (this.velocityTracker != null) {
                this.velocityTracker.recycle();
                this.velocityTracker = null;
            }
            this.startedMoving = false;
            this.moveStartX = GroundOverlayOptions.NO_DIMENSION;
        }
        return this.startedMoving;
    }

    private void applyViewsLayoutParams(int xOffset) {
        int widht = AndroidUtilities.displaySize.x - AndroidUtilities.dp(24.0f);
        if (this.leftView != null) {
            LayoutParams layoutParams = (LayoutParams) this.leftView.getLayoutParams();
            layoutParams.gravity = 51;
            layoutParams.height = -1;
            layoutParams.width = widht;
            layoutParams.leftMargin = (-widht) + xOffset;
            this.leftView.setLayoutParams(layoutParams);
        }
        if (this.centerView != null) {
            layoutParams = (LayoutParams) this.centerView.getLayoutParams();
            layoutParams.gravity = 51;
            layoutParams.height = -1;
            layoutParams.width = widht;
            layoutParams.leftMargin = xOffset;
            this.centerView.setLayoutParams(layoutParams);
        }
        if (this.rightView != null) {
            layoutParams = (LayoutParams) this.rightView.getLayoutParams();
            layoutParams.gravity = 51;
            layoutParams.height = -1;
            layoutParams.width = widht;
            layoutParams.leftMargin = widht + xOffset;
            this.rightView.setLayoutParams(layoutParams);
        }
        this.messageContainer.invalidate();
    }

    private ViewGroup getViewForMessage(int num, boolean applyOffset) {
        if (NotificationsController.getInstance().popupMessages.size() == 1 && (num < 0 || num >= NotificationsController.getInstance().popupMessages.size())) {
            return null;
        }
        View view;
        if (num == -1) {
            num = NotificationsController.getInstance().popupMessages.size() - 1;
        } else if (num == NotificationsController.getInstance().popupMessages.size()) {
            num = 0;
        }
        MessageObject messageObject = (MessageObject) NotificationsController.getInstance().popupMessages.get(num);
        View frameLayoutAnimationListener;
        TextView messageText;
        if (messageObject.type == 1 || messageObject.type == 4) {
            if (this.imageViews.size() > 0) {
                view = (ViewGroup) this.imageViews.get(0);
                this.imageViews.remove(0);
            } else {
                frameLayoutAnimationListener = new FrameLayoutAnimationListener(this);
                frameLayoutAnimationListener.addView(getLayoutInflater().inflate(C0553R.layout.popup_image_layout, null));
                frameLayoutAnimationListener.setTag(Integer.valueOf(2));
                frameLayoutAnimationListener.setOnClickListener(new C11608());
            }
            messageText = (TextView) view.findViewById(C0553R.id.message_text);
            BackupImageView imageView = (BackupImageView) view.findViewById(C0553R.id.message_image);
            imageView.setAspectFit(true);
            if (messageObject.type == 1) {
                PhotoSize currentPhotoObject = FileLoader.getClosestPhotoSizeWithSize(messageObject.photoThumbs, AndroidUtilities.getPhotoSize());
                PhotoSize thumb = FileLoader.getClosestPhotoSizeWithSize(messageObject.photoThumbs, 100);
                boolean photoSet = false;
                if (currentPhotoObject != null) {
                    boolean photoExist = true;
                    if (messageObject.type == 1 && !FileLoader.getPathToMessage(messageObject.messageOwner).exists()) {
                        photoExist = false;
                    }
                    if (photoExist || MediaController.getInstance().canDownloadMedia(1)) {
                        imageView.setImage(currentPhotoObject.location, "100_100", thumb.location, currentPhotoObject.size);
                        photoSet = true;
                    } else if (thumb != null) {
                        imageView.setImage(thumb.location, null, (Drawable) null);
                        photoSet = true;
                    }
                }
                if (photoSet) {
                    imageView.setVisibility(0);
                    messageText.setVisibility(8);
                } else {
                    imageView.setVisibility(8);
                    messageText.setVisibility(0);
                    messageText.setTextSize(2, (float) MessagesController.getInstance().fontSize);
                    messageText.setText(messageObject.messageText);
                }
            } else if (messageObject.type == 4) {
                messageText.setVisibility(8);
                messageText.setText(messageObject.messageText);
                imageView.setVisibility(0);
                double lat = messageObject.messageOwner.media.geo.lat;
                double lon = messageObject.messageOwner.media.geo._long;
                imageView.setImage(String.format(Locale.US, "https://maps.googleapis.com/maps/api/staticmap?center=%f,%f&zoom=13&size=100x100&maptype=roadmap&scale=%d&markers=color:red|size:big|%f,%f&sensor=false", new Object[]{Double.valueOf(lat), Double.valueOf(lon), Integer.valueOf(Math.min(2, (int) Math.ceil((double) AndroidUtilities.density))), Double.valueOf(lat), Double.valueOf(lon)}), null, null);
            }
        } else if (messageObject.type == 2) {
            PopupAudioView cell;
            if (this.audioViews.size() > 0) {
                view = (ViewGroup) this.audioViews.get(0);
                this.audioViews.remove(0);
                cell = (PopupAudioView) view.findViewWithTag(Integer.valueOf(300));
            } else {
                frameLayoutAnimationListener = new FrameLayoutAnimationListener(this);
                frameLayoutAnimationListener.addView(getLayoutInflater().inflate(C0553R.layout.popup_audio_layout, null));
                frameLayoutAnimationListener.setTag(Integer.valueOf(3));
                frameLayoutAnimationListener.setOnClickListener(new C11619());
                ViewGroup audioContainer = (ViewGroup) frameLayoutAnimationListener.findViewById(C0553R.id.audio_container);
                cell = new PopupAudioView(this);
                cell.setTag(Integer.valueOf(300));
                audioContainer.addView(cell);
            }
            cell.setMessageObject(messageObject);
            if (MediaController.getInstance().canDownloadMedia(2)) {
                cell.downloadAudioIfNeed();
            }
        } else {
            if (this.textViews.size() > 0) {
                view = (ViewGroup) this.textViews.get(0);
                this.textViews.remove(0);
            } else {
                frameLayoutAnimationListener = new FrameLayoutAnimationListener(this);
                frameLayoutAnimationListener.addView(getLayoutInflater().inflate(C0553R.layout.popup_text_layout, null));
                frameLayoutAnimationListener.setTag(Integer.valueOf(1));
                frameLayoutAnimationListener.findViewById(C0553R.id.text_container).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        PopupNotificationActivity.this.openCurrentMessage();
                    }
                });
            }
            messageText = (TextView) view.findViewById(C0553R.id.message_text);
            messageText.setTag(Integer.valueOf(301));
            messageText.setTextSize(2, (float) MessagesController.getInstance().fontSize);
            messageText.setText(messageObject.messageText);
        }
        if (view.getParent() == null) {
            this.messageContainer.addView(view);
        }
        view.setVisibility(0);
        if (!applyOffset) {
            return view;
        }
        int widht = AndroidUtilities.displaySize.x - AndroidUtilities.dp(24.0f);
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        layoutParams.gravity = 51;
        layoutParams.height = -1;
        layoutParams.width = widht;
        if (num == this.currentMessageNum) {
            layoutParams.leftMargin = 0;
        } else if (num == this.currentMessageNum - 1) {
            layoutParams.leftMargin = -widht;
        } else if (num == this.currentMessageNum + 1) {
            layoutParams.leftMargin = widht;
        }
        view.setLayoutParams(layoutParams);
        view.invalidate();
        return view;
    }

    private void reuseView(ViewGroup view) {
        if (view != null) {
            int tag = ((Integer) view.getTag()).intValue();
            view.setVisibility(8);
            if (tag == 1) {
                this.textViews.add(view);
            } else if (tag == 2) {
                this.imageViews.add(view);
            } else if (tag == 3) {
                this.audioViews.add(view);
            }
        }
    }

    private void prepareLayouts(int move) {
        if (move == 0) {
            reuseView(this.centerView);
            reuseView(this.leftView);
            reuseView(this.rightView);
            for (int a = this.currentMessageNum - 1; a < this.currentMessageNum + 2; a++) {
                if (a == this.currentMessageNum - 1) {
                    this.leftView = getViewForMessage(a, true);
                } else if (a == this.currentMessageNum) {
                    this.centerView = getViewForMessage(a, true);
                } else if (a == this.currentMessageNum + 1) {
                    this.rightView = getViewForMessage(a, true);
                }
            }
        } else if (move == 1) {
            reuseView(this.rightView);
            this.rightView = this.centerView;
            this.centerView = this.leftView;
            this.leftView = getViewForMessage(this.currentMessageNum - 1, true);
        } else if (move == 2) {
            reuseView(this.leftView);
            this.leftView = this.centerView;
            this.centerView = this.rightView;
            this.rightView = getViewForMessage(this.currentMessageNum + 1, true);
        } else if (move == 3) {
            if (this.rightView != null) {
                offset = ((LayoutParams) this.rightView.getLayoutParams()).leftMargin;
                reuseView(this.rightView);
                this.rightView = getViewForMessage(this.currentMessageNum + 1, false);
                widht = AndroidUtilities.displaySize.x - AndroidUtilities.dp(24.0f);
                layoutParams = (LayoutParams) this.rightView.getLayoutParams();
                layoutParams.gravity = 51;
                layoutParams.height = -1;
                layoutParams.width = widht;
                layoutParams.leftMargin = offset;
                this.rightView.setLayoutParams(layoutParams);
                this.rightView.invalidate();
            }
        } else if (move == 4 && this.leftView != null) {
            offset = ((LayoutParams) this.leftView.getLayoutParams()).leftMargin;
            reuseView(this.leftView);
            this.leftView = getViewForMessage(0, false);
            widht = AndroidUtilities.displaySize.x - AndroidUtilities.dp(24.0f);
            layoutParams = (LayoutParams) this.leftView.getLayoutParams();
            layoutParams.gravity = 51;
            layoutParams.height = -1;
            layoutParams.width = widht;
            layoutParams.leftMargin = offset;
            this.leftView.setLayoutParams(layoutParams);
            this.leftView.invalidate();
        }
    }

    private void fixLayout() {
        if (this.avatarContainer != null) {
            this.avatarContainer.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
                public boolean onPreDraw() {
                    if (PopupNotificationActivity.this.avatarContainer != null) {
                        PopupNotificationActivity.this.avatarContainer.getViewTreeObserver().removeOnPreDrawListener(this);
                    }
                    int padding = (ActionBar.getCurrentActionBarHeight() - AndroidUtilities.dp(48.0f)) / 2;
                    PopupNotificationActivity.this.avatarContainer.setPadding(PopupNotificationActivity.this.avatarContainer.getPaddingLeft(), padding, PopupNotificationActivity.this.avatarContainer.getPaddingRight(), padding);
                    return true;
                }
            });
        }
        if (this.messageContainer != null) {
            this.messageContainer.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
                public boolean onPreDraw() {
                    PopupNotificationActivity.this.messageContainer.getViewTreeObserver().removeOnPreDrawListener(this);
                    if (!(PopupNotificationActivity.this.checkTransitionAnimation() || PopupNotificationActivity.this.startedMoving)) {
                        MarginLayoutParams layoutParams = (MarginLayoutParams) PopupNotificationActivity.this.messageContainer.getLayoutParams();
                        layoutParams.topMargin = ActionBar.getCurrentActionBarHeight();
                        layoutParams.bottomMargin = AndroidUtilities.dp(48.0f);
                        layoutParams.width = -1;
                        layoutParams.height = -1;
                        PopupNotificationActivity.this.messageContainer.setLayoutParams(layoutParams);
                        PopupNotificationActivity.this.applyViewsLayoutParams(0);
                    }
                    return true;
                }
            });
        }
    }

    private void handleIntent(Intent intent) {
        if (((KeyguardManager) getSystemService("keyguard")).inKeyguardRestrictedInputMode() || !ApplicationLoader.isScreenOn) {
            getWindow().addFlags(2623490);
        } else {
            getWindow().addFlags(2623488);
            getWindow().clearFlags(2);
        }
        if (this.currentMessageObject == null) {
            this.currentMessageNum = 0;
        }
        getNewMessage();
    }

    private void getNewMessage() {
        if (NotificationsController.getInstance().popupMessages.isEmpty()) {
            onFinish();
            finish();
            return;
        }
        boolean found = false;
        if ((this.currentMessageNum != 0 || this.chatActivityEnterView.hasText() || this.startedMoving) && this.currentMessageObject != null) {
            for (int a = 0; a < NotificationsController.getInstance().popupMessages.size(); a++) {
                if (((MessageObject) NotificationsController.getInstance().popupMessages.get(a)).getId() == this.currentMessageObject.getId()) {
                    this.currentMessageNum = a;
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            this.currentMessageNum = 0;
            this.currentMessageObject = (MessageObject) NotificationsController.getInstance().popupMessages.get(0);
            updateInterfaceForCurrentMessage(0);
        } else if (this.startedMoving) {
            if (this.currentMessageNum == NotificationsController.getInstance().popupMessages.size() - 1) {
                prepareLayouts(3);
            } else if (this.currentMessageNum == 1) {
                prepareLayouts(4);
            }
        }
        this.countText.setText(String.format("%d/%d", new Object[]{Integer.valueOf(this.currentMessageNum + 1), Integer.valueOf(NotificationsController.getInstance().popupMessages.size())}));
    }

    private void openCurrentMessage() {
        if (this.currentMessageObject != null) {
            Intent intent = new Intent(ApplicationLoader.applicationContext, LaunchActivity.class);
            long dialog_id = this.currentMessageObject.getDialogId();
            if (((int) dialog_id) != 0) {
                int lower_id = (int) dialog_id;
                if (lower_id < 0) {
                    intent.putExtra("chatId", -lower_id);
                } else {
                    intent.putExtra("userId", lower_id);
                }
            } else {
                intent.putExtra("encId", (int) (dialog_id >> 32));
            }
            intent.setAction("com.tmessages.openchat" + Math.random() + ConnectionsManager.DEFAULT_DATACENTER_ID);
            intent.setFlags(32768);
            startActivity(intent);
            onFinish();
            finish();
        }
    }

    private void updateInterfaceForCurrentMessage(int move) {
        if (this.actionBar != null) {
            this.currentChat = null;
            this.currentUser = null;
            long dialog_id = this.currentMessageObject.getDialogId();
            this.chatActivityEnterView.setDialogId(dialog_id);
            if (((int) dialog_id) != 0) {
                int lower_id = (int) dialog_id;
                if (lower_id > 0) {
                    this.currentUser = MessagesController.getInstance().getUser(Integer.valueOf(lower_id));
                } else {
                    this.currentChat = MessagesController.getInstance().getChat(Integer.valueOf(-lower_id));
                    this.currentUser = MessagesController.getInstance().getUser(Integer.valueOf(this.currentMessageObject.messageOwner.from_id));
                }
            } else {
                this.currentUser = MessagesController.getInstance().getUser(Integer.valueOf(MessagesController.getInstance().getEncryptedChat(Integer.valueOf((int) (dialog_id >> 32))).user_id));
            }
            if (this.currentChat != null && this.currentUser != null) {
                this.nameTextView.setText(this.currentChat.title);
                this.onlineTextView.setText(UserObject.getUserName(this.currentUser));
                this.nameTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                this.nameTextView.setCompoundDrawablePadding(0);
            } else if (this.currentUser != null) {
                this.nameTextView.setText(UserObject.getUserName(this.currentUser));
                if (((int) dialog_id) == 0) {
                    this.nameTextView.setCompoundDrawablesWithIntrinsicBounds(C0553R.drawable.ic_lock_white, 0, 0, 0);
                    this.nameTextView.setCompoundDrawablePadding(AndroidUtilities.dp(4.0f));
                } else {
                    this.nameTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    this.nameTextView.setCompoundDrawablePadding(0);
                }
            }
            prepareLayouts(move);
            updateSubtitle();
            checkAndUpdateAvatar();
            applyViewsLayoutParams(0);
        }
    }

    private void updateSubtitle() {
        if (this.actionBar != null && this.currentChat == null && this.currentUser != null) {
            if (this.currentUser.id / LocationStatusCodes.GEOFENCE_NOT_AVAILABLE == 777 || this.currentUser.id / LocationStatusCodes.GEOFENCE_NOT_AVAILABLE == 333 || ContactsController.getInstance().contactsDict.get(this.currentUser.id) != null || (ContactsController.getInstance().contactsDict.size() == 0 && ContactsController.getInstance().isLoadingContacts())) {
                this.nameTextView.setText(UserObject.getUserName(this.currentUser));
            } else if (this.currentUser.phone == null || this.currentUser.phone.length() == 0) {
                this.nameTextView.setText(UserObject.getUserName(this.currentUser));
            } else {
                this.nameTextView.setText(PhoneFormat.getInstance().format("+" + this.currentUser.phone));
            }
            CharSequence printString = (CharSequence) MessagesController.getInstance().printingStrings.get(Long.valueOf(this.currentMessageObject.getDialogId()));
            if (printString == null || printString.length() == 0) {
                this.lastPrintString = null;
                setTypingAnimation(false);
                User user = MessagesController.getInstance().getUser(Integer.valueOf(this.currentUser.id));
                if (user != null) {
                    this.currentUser = user;
                }
                this.onlineTextView.setText(LocaleController.formatUserStatus(this.currentUser));
                return;
            }
            this.lastPrintString = printString;
            this.onlineTextView.setText(printString);
            setTypingAnimation(true);
        }
    }

    private void checkAndUpdateAvatar() {
        TLObject newPhoto = null;
        Drawable avatarDrawable = null;
        if (this.currentChat != null) {
            Chat chat = MessagesController.getInstance().getChat(Integer.valueOf(this.currentChat.id));
            if (chat != null) {
                this.currentChat = chat;
                if (this.currentChat.photo != null) {
                    newPhoto = this.currentChat.photo.photo_small;
                }
                avatarDrawable = new AvatarDrawable(this.currentChat);
            } else {
                return;
            }
        } else if (this.currentUser != null) {
            User user = MessagesController.getInstance().getUser(Integer.valueOf(this.currentUser.id));
            if (user != null) {
                this.currentUser = user;
                if (this.currentUser.photo != null) {
                    newPhoto = this.currentUser.photo.photo_small;
                }
                avatarDrawable = new AvatarDrawable(this.currentUser);
            } else {
                return;
            }
        }
        if (this.avatarImageView != null) {
            this.avatarImageView.setImage(newPhoto, "50_50", avatarDrawable);
        }
    }

    private void setTypingAnimation(boolean start) {
        if (this.actionBar != null) {
            if (start) {
                try {
                    Integer type = (Integer) MessagesController.getInstance().printingStringsTypes.get(Long.valueOf(this.currentMessageObject.getDialogId()));
                    if (type.intValue() == 0) {
                        this.onlineTextView.setCompoundDrawablesWithIntrinsicBounds(this.typingDotsDrawable, null, null, null);
                        this.onlineTextView.setCompoundDrawablePadding(AndroidUtilities.dp(4.0f));
                        this.typingDotsDrawable.start();
                        this.recordStatusDrawable.stop();
                        return;
                    } else if (type.intValue() == 1) {
                        this.onlineTextView.setCompoundDrawablesWithIntrinsicBounds(this.recordStatusDrawable, null, null, null);
                        this.onlineTextView.setCompoundDrawablePadding(AndroidUtilities.dp(4.0f));
                        this.recordStatusDrawable.start();
                        this.typingDotsDrawable.stop();
                        return;
                    } else {
                        return;
                    }
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                    return;
                }
            }
            this.onlineTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            this.onlineTextView.setCompoundDrawablePadding(0);
            this.typingDotsDrawable.stop();
            this.recordStatusDrawable.stop();
        }
    }

    public void onBackPressed() {
        if (this.chatActivityEnterView.isPopupShowing()) {
            this.chatActivityEnterView.hidePopup(true);
        } else {
            super.onBackPressed();
        }
    }

    protected void onResume() {
        super.onResume();
        if (this.chatActivityEnterView != null) {
            this.chatActivityEnterView.setFieldFocused(true);
        }
        ConnectionsManager.getInstance().setAppPaused(false, false);
        fixLayout();
        checkAndUpdateAvatar();
        this.wakeLock.acquire(7000);
    }

    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
        if (this.chatActivityEnterView != null) {
            this.chatActivityEnterView.hidePopup(false);
            this.chatActivityEnterView.setFieldFocused(false);
        }
        ConnectionsManager.getInstance().setAppPaused(true, false);
    }

    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.appDidLogout) {
            onFinish();
            finish();
        } else if (id == NotificationCenter.pushMessagesUpdated) {
            getNewMessage();
        } else if (id == NotificationCenter.updateInterfaces) {
            if (this.currentMessageObject != null) {
                int updateMask = ((Integer) args[0]).intValue();
                if (!((updateMask & 1) == 0 && (updateMask & 4) == 0 && (updateMask & 16) == 0 && (updateMask & 32) == 0)) {
                    updateSubtitle();
                }
                if (!((updateMask & 2) == 0 && (updateMask & 8) == 0)) {
                    checkAndUpdateAvatar();
                }
                if ((updateMask & 64) != 0) {
                    CharSequence printString = (CharSequence) MessagesController.getInstance().printingStrings.get(Long.valueOf(this.currentMessageObject.getDialogId()));
                    if ((this.lastPrintString != null && printString == null) || ((this.lastPrintString == null && printString != null) || (this.lastPrintString != null && printString != null && !this.lastPrintString.equals(printString)))) {
                        updateSubtitle();
                    }
                }
            }
        } else if (id == NotificationCenter.audioDidReset) {
            mid = args[0];
            if (this.messageContainer != null) {
                count = this.messageContainer.getChildCount();
                for (a = 0; a < count; a++) {
                    view = this.messageContainer.getChildAt(a);
                    if (((Integer) view.getTag()).intValue() == 3) {
                        cell = (PopupAudioView) view.findViewWithTag(Integer.valueOf(300));
                        if (cell.getMessageObject() != null && cell.getMessageObject().getId() == mid.intValue()) {
                            cell.updateButtonState();
                            return;
                        }
                    }
                }
            }
        } else if (id == NotificationCenter.audioProgressDidChanged) {
            mid = (Integer) args[0];
            if (this.messageContainer != null) {
                count = this.messageContainer.getChildCount();
                for (a = 0; a < count; a++) {
                    view = this.messageContainer.getChildAt(a);
                    if (((Integer) view.getTag()).intValue() == 3) {
                        cell = (PopupAudioView) view.findViewWithTag(Integer.valueOf(300));
                        if (cell.getMessageObject() != null && cell.getMessageObject().getId() == mid.intValue()) {
                            cell.updateProgress();
                            return;
                        }
                    }
                }
            }
        } else if (id == NotificationCenter.emojiDidLoaded) {
            if (this.messageContainer != null) {
                count = this.messageContainer.getChildCount();
                for (a = 0; a < count; a++) {
                    view = this.messageContainer.getChildAt(a);
                    if (((Integer) view.getTag()).intValue() == 1) {
                        TextView textView = (TextView) view.findViewWithTag(Integer.valueOf(301));
                        if (textView != null) {
                            textView.invalidate();
                        }
                    }
                }
            }
        } else if (id == NotificationCenter.contactsDidLoaded) {
            updateSubtitle();
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        onFinish();
        if (this.wakeLock.isHeld()) {
            this.wakeLock.release();
        }
        if (this.avatarImageView != null) {
            this.avatarImageView.setImageDrawable(null);
        }
    }

    protected void onFinish() {
        if (!this.finished) {
            this.finished = true;
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.appDidLogout);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.pushMessagesUpdated);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.updateInterfaces);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.audioProgressDidChanged);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.audioDidReset);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.contactsDidLoaded);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.emojiDidLoaded);
            if (this.chatActivityEnterView != null) {
                this.chatActivityEnterView.onDestroy();
            }
            if (this.wakeLock.isHeld()) {
                this.wakeLock.release();
            }
        }
    }
}
