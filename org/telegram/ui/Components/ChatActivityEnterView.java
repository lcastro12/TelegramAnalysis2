package org.telegram.ui.Components;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.view.ViewCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import java.util.Locale;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.AnimationCompat.AnimatorListenerAdapterProxy;
import org.telegram.messenger.AnimationCompat.AnimatorSetProxy;
import org.telegram.messenger.AnimationCompat.ObjectAnimatorProxy;
import org.telegram.messenger.AnimationCompat.ViewProxy;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.C0553R;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.Document;
import org.telegram.tgnet.TLRPC.TL_replyKeyboardMarkup;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.WebPage;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.BotKeyboardView.BotKeyboardViewDelegate;
import org.telegram.ui.Components.EmojiView.Listener;
import org.telegram.ui.Components.SizeNotifierFrameLayout.SizeNotifierFrameLayoutDelegate;
import org.telegram.ui.StickersActivity;

public class ChatActivityEnterView extends FrameLayoutFixed implements NotificationCenterDelegate, SizeNotifierFrameLayoutDelegate {
    private boolean adminModeAvailable;
    private boolean allowShowTopView;
    private boolean allowStickers;
    private ImageView asAdminButton;
    private LinearLayout attachButton;
    private int audioInterfaceState;
    private ImageView audioSendButton;
    private ImageView botButton;
    private MessageObject botButtonsMessageObject;
    private int botCount;
    private PopupWindow botKeyboardPopup;
    private BotKeyboardView botKeyboardView;
    private MessageObject botMessageObject;
    private TL_replyKeyboardMarkup botReplyMarkup;
    private int currentPopupContentType = -1;
    private AnimatorSetProxy currentTopViewAnimation;
    private ChatActivityEnterViewDelegate delegate;
    private long dialog_id;
    private float distCanMove = ((float) AndroidUtilities.dp(80.0f));
    private ImageView emojiButton;
    private int emojiPadding;
    private EmojiView emojiView;
    private boolean forceShowSendButton;
    private boolean hasBotCommands;
    private boolean ignoreTextChange;
    private int innerTextChange;
    private boolean isAsAdmin;
    private boolean isPaused;
    private int keyboardHeight;
    private int keyboardHeightLand;
    private boolean keyboardVisible;
    private int lastSizeChangeValue1;
    private boolean lastSizeChangeValue2;
    private String lastTimeString;
    private long lastTypingTimeSend;
    private WakeLock mWakeLock;
    private EditText messageEditText;
    private WebPage messageWebPage;
    private boolean messageWebPageSearch = true;
    private boolean needShowTopView;
    private Runnable openKeyboardRunnable = new C08941();
    private Activity parentActivity;
    private BaseFragment parentFragment;
    private RecordCircle recordCircle;
    private RecordDot recordDot;
    private FrameLayout recordPanel;
    private TextView recordTimeText;
    private boolean recordingAudio;
    private MessageObject replyingMessageObject;
    private AnimatorSetProxy runningAnimation;
    private AnimatorSetProxy runningAnimation2;
    private AnimatorSetProxy runningAnimationAudio;
    private int runningAnimationType;
    private ImageView sendButton;
    private boolean sendByEnter;
    private boolean showKeyboardOnResume;
    private SizeNotifierFrameLayout sizeNotifierLayout;
    private LinearLayout slideText;
    private float startedDraggingX = GroundOverlayOptions.NO_DIMENSION;
    private LinearLayout textFieldContainer;
    private View topView;
    private float topViewAnimation;
    private boolean topViewShowed;
    private boolean waitingForKeyboardOpen;

    class C08941 implements Runnable {
        C08941() {
        }

        public void run() {
            if (ChatActivityEnterView.this.messageEditText != null && ChatActivityEnterView.this.waitingForKeyboardOpen && !ChatActivityEnterView.this.keyboardVisible && !AndroidUtilities.usingHardwareInput) {
                ChatActivityEnterView.this.messageEditText.requestFocus();
                AndroidUtilities.showKeyboard(ChatActivityEnterView.this.messageEditText);
                AndroidUtilities.cancelRunOnUIThread(ChatActivityEnterView.this.openKeyboardRunnable);
                AndroidUtilities.runOnUIThread(ChatActivityEnterView.this.openKeyboardRunnable, 100);
            }
        }
    }

    class C08952 implements OnClickListener {
        C08952() {
        }

        public void onClick(View view) {
            if (ChatActivityEnterView.this.isPopupShowing() && ChatActivityEnterView.this.currentPopupContentType == 0) {
                ChatActivityEnterView.this.openKeyboardInternal();
            } else {
                ChatActivityEnterView.this.showPopup(1, 0);
            }
        }
    }

    class C08974 implements OnKeyListener {
        boolean ctrlPressed = false;

        C08974() {
        }

        public boolean onKey(View view, int i, KeyEvent keyEvent) {
            boolean z = false;
            if (i == 4 && !ChatActivityEnterView.this.keyboardVisible && ChatActivityEnterView.this.isPopupShowing()) {
                if (keyEvent.getAction() != 1) {
                    return true;
                }
                if (ChatActivityEnterView.this.currentPopupContentType == 1 && ChatActivityEnterView.this.botButtonsMessageObject != null) {
                    ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0).edit().putInt("hidekeyboard_" + ChatActivityEnterView.this.dialog_id, ChatActivityEnterView.this.botButtonsMessageObject.getId()).commit();
                }
                ChatActivityEnterView.this.showPopup(0, 0);
                return true;
            } else if (i == 66 && ((this.ctrlPressed || ChatActivityEnterView.this.sendByEnter) && keyEvent.getAction() == 0)) {
                ChatActivityEnterView.this.sendMessage();
                return true;
            } else if (i != 113 && i != 114) {
                return false;
            } else {
                if (keyEvent.getAction() == 0) {
                    z = true;
                }
                this.ctrlPressed = z;
                return true;
            }
        }
    }

    class C08985 implements OnEditorActionListener {
        boolean ctrlPressed = false;

        C08985() {
        }

        public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
            boolean z = false;
            if (i == 4) {
                ChatActivityEnterView.this.sendMessage();
                return true;
            }
            if (keyEvent != null && i == 0) {
                if ((this.ctrlPressed || ChatActivityEnterView.this.sendByEnter) && keyEvent.getAction() == 0) {
                    ChatActivityEnterView.this.sendMessage();
                    return true;
                } else if (i == 113 || i == 114) {
                    if (keyEvent.getAction() == 0) {
                        z = true;
                    }
                    this.ctrlPressed = z;
                    return true;
                }
            }
            return false;
        }
    }

    class C08996 implements TextWatcher {
        boolean processChange = false;

        C08996() {
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            if (ChatActivityEnterView.this.innerTextChange != 1) {
                ChatActivityEnterView.this.checkSendButton(true);
                String message = ChatActivityEnterView.this.getTrimmedString(charSequence.toString());
                if (ChatActivityEnterView.this.delegate != null) {
                    if (count > 2 || charSequence == null || charSequence.length() == 0) {
                        ChatActivityEnterView.this.messageWebPageSearch = true;
                    }
                    ChatActivityEnterViewDelegate access$1400 = ChatActivityEnterView.this.delegate;
                    boolean z = before > count + 1 || count - before > 2;
                    access$1400.onTextChanged(charSequence, z);
                }
                if (!(ChatActivityEnterView.this.innerTextChange == 2 || before == count || count - before <= 1)) {
                    this.processChange = true;
                }
                if (!ChatActivityEnterView.this.isAsAdmin && message.length() != 0 && ChatActivityEnterView.this.lastTypingTimeSend < System.currentTimeMillis() - 5000 && !ChatActivityEnterView.this.ignoreTextChange) {
                    int currentTime = ConnectionsManager.getInstance().getCurrentTime();
                    User currentUser = null;
                    if (((int) ChatActivityEnterView.this.dialog_id) > 0) {
                        currentUser = MessagesController.getInstance().getUser(Integer.valueOf((int) ChatActivityEnterView.this.dialog_id));
                    }
                    if (currentUser != null) {
                        if (currentUser.id == UserConfig.getClientUserId()) {
                            return;
                        }
                        if (!(currentUser.status == null || currentUser.status.expires >= currentTime || MessagesController.getInstance().onlinePrivacy.containsKey(Integer.valueOf(currentUser.id)))) {
                            return;
                        }
                    }
                    ChatActivityEnterView.this.lastTypingTimeSend = System.currentTimeMillis();
                    if (ChatActivityEnterView.this.delegate != null) {
                        ChatActivityEnterView.this.delegate.needSendTyping();
                    }
                }
            }
        }

        public void afterTextChanged(Editable editable) {
            if (ChatActivityEnterView.this.innerTextChange == 0) {
                if (ChatActivityEnterView.this.sendByEnter && editable.length() > 0 && editable.charAt(editable.length() - 1) == '\n') {
                    ChatActivityEnterView.this.sendMessage();
                }
                if (this.processChange) {
                    ImageSpan[] spans = (ImageSpan[]) editable.getSpans(0, editable.length(), ImageSpan.class);
                    for (Object removeSpan : spans) {
                        editable.removeSpan(removeSpan);
                    }
                    Emoji.replaceEmoji(editable, ChatActivityEnterView.this.messageEditText.getPaint().getFontMetricsInt(), AndroidUtilities.dp(20.0f), false);
                    this.processChange = false;
                }
            }
        }
    }

    class C09007 implements OnClickListener {
        C09007() {
        }

        public void onClick(View v) {
            if (ChatActivityEnterView.this.botReplyMarkup != null) {
                if (ChatActivityEnterView.this.isPopupShowing() && ChatActivityEnterView.this.currentPopupContentType == 1) {
                    if (ChatActivityEnterView.this.currentPopupContentType == 1 && ChatActivityEnterView.this.botButtonsMessageObject != null) {
                        ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0).edit().putInt("hidekeyboard_" + ChatActivityEnterView.this.dialog_id, ChatActivityEnterView.this.botButtonsMessageObject.getId()).commit();
                    }
                    ChatActivityEnterView.this.openKeyboardInternal();
                    return;
                }
                ChatActivityEnterView.this.showPopup(1, 1);
                ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0).edit().remove("hidekeyboard_" + ChatActivityEnterView.this.dialog_id).commit();
            } else if (ChatActivityEnterView.this.hasBotCommands) {
                ChatActivityEnterView.this.setFieldText("/");
                ChatActivityEnterView.this.openKeyboard();
            }
        }
    }

    class C09018 implements OnClickListener {
        C09018() {
        }

        public void onClick(View v) {
            boolean z;
            ChatActivityEnterView chatActivityEnterView = ChatActivityEnterView.this;
            if (ChatActivityEnterView.this.isAsAdmin) {
                z = false;
            } else {
                z = true;
            }
            chatActivityEnterView.isAsAdmin = z;
            ChatActivityEnterView.this.asAdminButton.setImageResource(ChatActivityEnterView.this.isAsAdmin ? C0553R.drawable.publish_active : C0553R.drawable.publish);
            ChatActivityEnterView.this.updateFieldHint();
            ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0).edit().putBoolean("asadmin_" + ChatActivityEnterView.this.dialog_id, ChatActivityEnterView.this.isAsAdmin).commit();
        }
    }

    class C09029 implements OnTouchListener {
        C09029() {
        }

        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == 0) {
                if (ChatActivityEnterView.this.parentFragment != null) {
                    if (VERSION.SDK_INT < 23 || ChatActivityEnterView.this.parentActivity.checkSelfPermission("android.permission.RECORD_AUDIO") == 0) {
                        String action;
                        if (((int) ChatActivityEnterView.this.dialog_id) < 0) {
                            Chat currentChat = MessagesController.getInstance().getChat(Integer.valueOf(-((int) ChatActivityEnterView.this.dialog_id)));
                            if (currentChat == null || currentChat.participants_count <= MessagesController.getInstance().groupBigSize) {
                                action = "chat_upload_audio";
                            } else {
                                action = "bigchat_upload_audio";
                            }
                        } else {
                            action = "pm_upload_audio";
                        }
                        if (!MessagesController.isFeatureEnabled(action, ChatActivityEnterView.this.parentFragment)) {
                            return false;
                        }
                    }
                    ChatActivityEnterView.this.parentActivity.requestPermissions(new String[]{"android.permission.RECORD_AUDIO"}, 3);
                    return false;
                }
                ChatActivityEnterView.this.startedDraggingX = GroundOverlayOptions.NO_DIMENSION;
                MediaController.getInstance().startRecording(ChatActivityEnterView.this.dialog_id, ChatActivityEnterView.this.replyingMessageObject, ChatActivityEnterView.this.asAdmin());
                ChatActivityEnterView.this.updateAudioRecordIntefrace();
                ChatActivityEnterView.this.audioSendButton.getParent().requestDisallowInterceptTouchEvent(true);
            } else if (motionEvent.getAction() == 1 || motionEvent.getAction() == 3) {
                ChatActivityEnterView.this.startedDraggingX = GroundOverlayOptions.NO_DIMENSION;
                MediaController.getInstance().stopRecording(true);
                ChatActivityEnterView.this.recordingAudio = false;
                ChatActivityEnterView.this.updateAudioRecordIntefrace();
            } else if (motionEvent.getAction() == 2 && ChatActivityEnterView.this.recordingAudio) {
                float x = motionEvent.getX();
                if (x < (-ChatActivityEnterView.this.distCanMove)) {
                    MediaController.getInstance().stopRecording(false);
                    ChatActivityEnterView.this.recordingAudio = false;
                    ChatActivityEnterView.this.updateAudioRecordIntefrace();
                }
                x += ViewProxy.getX(ChatActivityEnterView.this.audioSendButton);
                LayoutParams params = (LayoutParams) ChatActivityEnterView.this.slideText.getLayoutParams();
                if (ChatActivityEnterView.this.startedDraggingX != GroundOverlayOptions.NO_DIMENSION) {
                    float dist = x - ChatActivityEnterView.this.startedDraggingX;
                    ViewProxy.setTranslationX(ChatActivityEnterView.this.recordCircle, dist);
                    params.leftMargin = AndroidUtilities.dp(BitmapDescriptorFactory.HUE_ORANGE) + ((int) dist);
                    ChatActivityEnterView.this.slideText.setLayoutParams(params);
                    float alpha = 1.0f + (dist / ChatActivityEnterView.this.distCanMove);
                    if (alpha > 1.0f) {
                        alpha = 1.0f;
                    } else if (alpha < 0.0f) {
                        alpha = 0.0f;
                    }
                    ViewProxy.setAlpha(ChatActivityEnterView.this.slideText, alpha);
                }
                if (x <= (ViewProxy.getX(ChatActivityEnterView.this.slideText) + ((float) ChatActivityEnterView.this.slideText.getWidth())) + ((float) AndroidUtilities.dp(BitmapDescriptorFactory.HUE_ORANGE)) && ChatActivityEnterView.this.startedDraggingX == GroundOverlayOptions.NO_DIMENSION) {
                    ChatActivityEnterView.this.startedDraggingX = x;
                    ChatActivityEnterView.this.distCanMove = ((float) ((ChatActivityEnterView.this.recordPanel.getMeasuredWidth() - ChatActivityEnterView.this.slideText.getMeasuredWidth()) - AndroidUtilities.dp(48.0f))) / 2.0f;
                    if (ChatActivityEnterView.this.distCanMove <= 0.0f) {
                        ChatActivityEnterView.this.distCanMove = (float) AndroidUtilities.dp(80.0f);
                    } else if (ChatActivityEnterView.this.distCanMove > ((float) AndroidUtilities.dp(80.0f))) {
                        ChatActivityEnterView.this.distCanMove = (float) AndroidUtilities.dp(80.0f);
                    }
                }
                if (params.leftMargin > AndroidUtilities.dp(BitmapDescriptorFactory.HUE_ORANGE)) {
                    params.leftMargin = AndroidUtilities.dp(BitmapDescriptorFactory.HUE_ORANGE);
                    ViewProxy.setTranslationX(ChatActivityEnterView.this.recordCircle, 0.0f);
                    ChatActivityEnterView.this.slideText.setLayoutParams(params);
                    ViewProxy.setAlpha(ChatActivityEnterView.this.slideText, 1.0f);
                    ChatActivityEnterView.this.startedDraggingX = GroundOverlayOptions.NO_DIMENSION;
                }
            }
            view.onTouchEvent(motionEvent);
            return true;
        }
    }

    public interface ChatActivityEnterViewDelegate {
        void needSendTyping();

        void onAttachButtonHidden();

        void onAttachButtonShow();

        void onMessageSend(String str);

        void onTextChanged(CharSequence charSequence, boolean z);

        void onWindowSizeChanged(int i);
    }

    private class RecordCircle extends View {
        private float amplitude;
        private float animateAmplitudeDiff;
        private float animateToAmplitude;
        private long lastUpdateTime;
        private Drawable micDrawable;
        private Paint paint = new Paint(1);
        private Paint paintRecord = new Paint(1);
        private float scale;

        public RecordCircle(Context context) {
            super(context);
            this.paint.setColor(-11037236);
            this.paintRecord.setColor(218103808);
            this.micDrawable = getResources().getDrawable(C0553R.drawable.mic_pressed);
        }

        public void setAmplitude(double value) {
            this.animateToAmplitude = ((float) Math.min(100.0d, value)) / 100.0f;
            this.animateAmplitudeDiff = (this.animateToAmplitude - this.amplitude) / 150.0f;
            this.lastUpdateTime = System.currentTimeMillis();
            invalidate();
        }

        public float getScale() {
            return this.scale;
        }

        public void setScale(float value) {
            this.scale = value;
            invalidate();
        }

        protected void onDraw(Canvas canvas) {
            float sc;
            float alpha;
            int cx = getMeasuredWidth() / 2;
            int cy = getMeasuredHeight() / 2;
            if (this.scale <= 0.5f) {
                sc = this.scale / 0.5f;
                alpha = sc;
            } else if (this.scale <= 0.75f) {
                sc = 1.0f - (((this.scale - 0.5f) / 0.25f) * 0.1f);
                alpha = 1.0f;
            } else {
                sc = 0.9f + (((this.scale - 0.75f) / 0.25f) * 0.1f);
                alpha = 1.0f;
            }
            long dt = System.currentTimeMillis() - this.lastUpdateTime;
            if (this.animateToAmplitude != this.amplitude) {
                this.amplitude += this.animateAmplitudeDiff * ((float) dt);
                if (this.animateAmplitudeDiff > 0.0f) {
                    if (this.amplitude > this.animateToAmplitude) {
                        this.amplitude = this.animateToAmplitude;
                    }
                } else if (this.amplitude < this.animateToAmplitude) {
                    this.amplitude = this.animateToAmplitude;
                }
                invalidate();
            }
            this.lastUpdateTime = System.currentTimeMillis();
            if (this.amplitude != 0.0f) {
                canvas.drawCircle(((float) getMeasuredWidth()) / 2.0f, ((float) getMeasuredHeight()) / 2.0f, (((float) AndroidUtilities.dp(42.0f)) + (((float) AndroidUtilities.dp(20.0f)) * this.amplitude)) * this.scale, this.paintRecord);
            }
            canvas.drawCircle(((float) getMeasuredWidth()) / 2.0f, ((float) getMeasuredHeight()) / 2.0f, ((float) AndroidUtilities.dp(42.0f)) * sc, this.paint);
            this.micDrawable.setBounds(cx - (this.micDrawable.getIntrinsicWidth() / 2), cy - (this.micDrawable.getIntrinsicHeight() / 2), (this.micDrawable.getIntrinsicWidth() / 2) + cx, (this.micDrawable.getIntrinsicHeight() / 2) + cy);
            this.micDrawable.setAlpha((int) (255.0f * alpha));
            this.micDrawable.draw(canvas);
        }
    }

    private class RecordDot extends View {
        private float alpha;
        private Drawable dotDrawable = getResources().getDrawable(C0553R.drawable.rec);
        private boolean isIncr;
        private long lastUpdateTime;

        public RecordDot(Context context) {
            super(context);
        }

        public void resetAlpha() {
            this.alpha = 1.0f;
            this.lastUpdateTime = System.currentTimeMillis();
            this.isIncr = false;
            invalidate();
        }

        protected void onDraw(Canvas canvas) {
            this.dotDrawable.setBounds(0, 0, AndroidUtilities.dp(11.0f), AndroidUtilities.dp(11.0f));
            this.dotDrawable.setAlpha(((int) (70.0f * this.alpha)) + 185);
            long dt = System.currentTimeMillis() - this.lastUpdateTime;
            if (this.isIncr) {
                this.alpha += ((float) dt) / 200.0f;
                if (this.alpha >= 1.0f) {
                    this.alpha = 1.0f;
                    this.isIncr = false;
                }
            } else {
                this.alpha -= ((float) dt) / 200.0f;
                if (this.alpha <= 0.0f) {
                    this.alpha = 0.0f;
                    this.isIncr = true;
                }
            }
            this.lastUpdateTime = System.currentTimeMillis();
            this.dotDrawable.draw(canvas);
            invalidate();
        }
    }

    public ChatActivityEnterView(Activity context, SizeNotifierFrameLayout parent, BaseFragment fragment, boolean isChat) {
        super(context);
        setBackgroundResource(C0553R.drawable.compose_panel);
        setFocusable(true);
        setFocusableInTouchMode(true);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.recordStarted);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.recordStartError);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.recordStopped);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.recordProgressChanged);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.closeChats);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.audioDidSent);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.emojiDidLoaded);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.audioRouteChanged);
        this.parentActivity = context;
        this.parentFragment = fragment;
        this.sizeNotifierLayout = parent;
        this.sizeNotifierLayout.setDelegate(this);
        this.sendByEnter = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0).getBoolean("send_by_enter", false);
        this.textFieldContainer = new LinearLayout(context);
        this.textFieldContainer.setBackgroundColor(-1);
        this.textFieldContainer.setOrientation(0);
        addView(this.textFieldContainer, LayoutHelper.createFrame(-1, -2.0f, 51, 0.0f, 2.0f, 0.0f, 0.0f));
        FrameLayoutFixed frameLayout = new FrameLayoutFixed(context);
        this.textFieldContainer.addView(frameLayout, LayoutHelper.createLinear(0, -2, 1.0f));
        this.emojiButton = new ImageView(context);
        this.emojiButton.setImageResource(C0553R.drawable.ic_msg_panel_smiles);
        this.emojiButton.setScaleType(ScaleType.CENTER_INSIDE);
        this.emojiButton.setPadding(AndroidUtilities.dp(4.0f), AndroidUtilities.dp(1.0f), 0, 0);
        frameLayout.addView(this.emojiButton, LayoutHelper.createFrame(48, 48, 80));
        this.emojiButton.setOnClickListener(new C08952());
        this.messageEditText = new EditText(context) {
            public boolean onTouchEvent(MotionEvent event) {
                if (ChatActivityEnterView.this.isPopupShowing() && event.getAction() == 0) {
                    ChatActivityEnterView.this.showPopup(AndroidUtilities.usingHardwareInput ? 0 : 2, 0);
                    ChatActivityEnterView.this.openKeyboardInternal();
                }
                return super.onTouchEvent(event);
            }
        };
        updateFieldHint();
        this.messageEditText.setImeOptions(268435456);
        this.messageEditText.setInputType((this.messageEditText.getInputType() | 16384) | 131072);
        this.messageEditText.setSingleLine(false);
        this.messageEditText.setMaxLines(4);
        this.messageEditText.setTextSize(1, 18.0f);
        this.messageEditText.setGravity(80);
        this.messageEditText.setPadding(0, AndroidUtilities.dp(11.0f), 0, AndroidUtilities.dp(12.0f));
        this.messageEditText.setBackgroundDrawable(null);
        AndroidUtilities.clearCursorDrawable(this.messageEditText);
        this.messageEditText.setTextColor(ViewCompat.MEASURED_STATE_MASK);
        this.messageEditText.setHintTextColor(-5066062);
        frameLayout.addView(this.messageEditText, LayoutHelper.createFrame(-1, -2.0f, 80, 52.0f, 0.0f, isChat ? 50.0f : 2.0f, 0.0f));
        this.messageEditText.setOnKeyListener(new C08974());
        this.messageEditText.setOnEditorActionListener(new C08985());
        this.messageEditText.addTextChangedListener(new C08996());
        if (isChat) {
            this.attachButton = new LinearLayout(context);
            this.attachButton.setOrientation(0);
            this.attachButton.setEnabled(false);
            ViewProxy.setPivotX(this.attachButton, (float) AndroidUtilities.dp(48.0f));
            frameLayout.addView(this.attachButton, LayoutHelper.createFrame(-2, 48, 85));
            this.botButton = new ImageView(context);
            this.botButton.setImageResource(C0553R.drawable.bot_keyboard2);
            this.botButton.setScaleType(ScaleType.CENTER);
            this.botButton.setVisibility(8);
            this.attachButton.addView(this.botButton, LayoutHelper.createLinear(48, 48));
            this.botButton.setOnClickListener(new C09007());
            this.asAdminButton = new ImageView(context);
            this.asAdminButton.setImageResource(this.isAsAdmin ? C0553R.drawable.publish_active : C0553R.drawable.publish);
            this.asAdminButton.setScaleType(ScaleType.CENTER);
            this.asAdminButton.setVisibility(this.adminModeAvailable ? 0 : 8);
            this.attachButton.addView(this.asAdminButton, LayoutHelper.createLinear(48, 48));
            this.asAdminButton.setOnClickListener(new C09018());
        }
        this.recordPanel = new FrameLayoutFixed(context);
        this.recordPanel.setVisibility(8);
        this.recordPanel.setBackgroundColor(-1);
        frameLayout.addView(this.recordPanel, LayoutHelper.createFrame(-1, 48, 80));
        this.slideText = new LinearLayout(context);
        this.slideText.setOrientation(0);
        this.recordPanel.addView(this.slideText, LayoutHelper.createFrame(-2, -2.0f, 17, BitmapDescriptorFactory.HUE_ORANGE, 0.0f, 0.0f, 0.0f));
        ImageView imageView = new ImageView(context);
        imageView.setImageResource(C0553R.drawable.slidearrow);
        this.slideText.addView(imageView, LayoutHelper.createLinear(-2, -2, 16, 0, 1, 0, 0));
        TextView textView = new TextView(context);
        textView.setText(LocaleController.getString("SlideToCancel", C0553R.string.SlideToCancel));
        textView.setTextColor(-6710887);
        textView.setTextSize(1, 12.0f);
        this.slideText.addView(textView, LayoutHelper.createLinear(-2, -2, 16, 6, 0, 0, 0));
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(0);
        linearLayout.setPadding(AndroidUtilities.dp(13.0f), 0, 0, 0);
        linearLayout.setBackgroundColor(-1);
        this.recordPanel.addView(linearLayout, LayoutHelper.createFrame(-2, -2, 16));
        this.recordDot = new RecordDot(context);
        linearLayout.addView(this.recordDot, LayoutHelper.createLinear(11, 11, 16, 0, 1, 0, 0));
        this.recordTimeText = new TextView(context);
        this.recordTimeText.setText("00:00");
        this.recordTimeText.setTextColor(-11711413);
        this.recordTimeText.setTextSize(1, 16.0f);
        linearLayout.addView(this.recordTimeText, LayoutHelper.createLinear(-2, -2, 16, 6, 0, 0, 0));
        FrameLayout frameLayout1 = new FrameLayout(context);
        this.textFieldContainer.addView(frameLayout1, LayoutHelper.createLinear(48, 48, 80));
        this.audioSendButton = new ImageView(context);
        this.audioSendButton.setScaleType(ScaleType.CENTER_INSIDE);
        this.audioSendButton.setImageResource(C0553R.drawable.mic);
        this.audioSendButton.setBackgroundColor(-1);
        this.audioSendButton.setSoundEffectsEnabled(false);
        this.audioSendButton.setPadding(0, 0, AndroidUtilities.dp(4.0f), 0);
        frameLayout1.addView(this.audioSendButton, LayoutHelper.createFrame(48, 48.0f));
        this.audioSendButton.setOnTouchListener(new C09029());
        this.recordCircle = new RecordCircle(context);
        this.recordCircle.setVisibility(8);
        this.sizeNotifierLayout.addView(this.recordCircle, LayoutHelper.createFrame(124, 124.0f, 85, 0.0f, 0.0f, -36.0f, -38.0f));
        this.sendButton = new ImageView(context);
        this.sendButton.setVisibility(4);
        this.sendButton.setScaleType(ScaleType.CENTER_INSIDE);
        this.sendButton.setImageResource(C0553R.drawable.ic_send);
        this.sendButton.setSoundEffectsEnabled(false);
        ViewProxy.setScaleX(this.sendButton, 0.1f);
        ViewProxy.setScaleY(this.sendButton, 0.1f);
        ViewProxy.setAlpha(this.sendButton, 0.0f);
        this.sendButton.clearAnimation();
        frameLayout1.addView(this.sendButton, LayoutHelper.createFrame(48, 48.0f));
        this.sendButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                ChatActivityEnterView.this.sendMessage();
            }
        });
        SharedPreferences sharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("emoji", 0);
        this.keyboardHeight = sharedPreferences.getInt("kbd_height", AndroidUtilities.dp(200.0f));
        this.keyboardHeightLand = sharedPreferences.getInt("kbd_height_land3", AndroidUtilities.dp(200.0f));
        checkSendButton(false);
    }

    public void addTopView(View view, int height) {
        if (view != null) {
            this.topView = view;
            this.topView.setVisibility(8);
            addView(this.topView, 0, LayoutHelper.createFrame(-1, (float) height, 51, 0.0f, 2.0f, 0.0f, 0.0f));
            this.needShowTopView = false;
        }
    }

    public void setTopViewAnimation(float progress) {
        this.topViewAnimation = progress;
        LayoutParams layoutParams2 = (LayoutParams) this.textFieldContainer.getLayoutParams();
        layoutParams2.topMargin = AndroidUtilities.dp(2.0f) + ((int) (((float) this.topView.getLayoutParams().height) * progress));
        this.textFieldContainer.setLayoutParams(layoutParams2);
    }

    public float getTopViewAnimation() {
        return this.topViewAnimation;
    }

    public void setForceShowSendButton(boolean value, boolean animated) {
        this.forceShowSendButton = value;
        checkSendButton(animated);
    }

    public void setAllowStickers(boolean value) {
        this.allowStickers = value;
    }

    public boolean asAdmin() {
        return this.isAsAdmin;
    }

    public void showTopView(boolean animated, final boolean openKeyboard) {
        if (this.topView != null && !this.topViewShowed && getVisibility() == 0) {
            this.needShowTopView = true;
            this.topViewShowed = true;
            if (this.allowShowTopView) {
                this.topView.setVisibility(0);
                if (this.currentTopViewAnimation != null) {
                    this.currentTopViewAnimation.cancel();
                    this.currentTopViewAnimation = null;
                }
                if (!animated) {
                    setTopViewAnimation(1.0f);
                } else if (this.keyboardVisible || isPopupShowing()) {
                    this.currentTopViewAnimation = new AnimatorSetProxy();
                    AnimatorSetProxy animatorSetProxy = this.currentTopViewAnimation;
                    Object[] objArr = new Object[1];
                    objArr[0] = ObjectAnimatorProxy.ofFloat(this, "topViewAnimation", 1.0f);
                    animatorSetProxy.playTogether(objArr);
                    this.currentTopViewAnimation.addListener(new AnimatorListenerAdapterProxy() {
                        public void onAnimationEnd(Object animation) {
                            if (ChatActivityEnterView.this.currentTopViewAnimation != null && ChatActivityEnterView.this.currentTopViewAnimation.equals(animation)) {
                                ChatActivityEnterView.this.setTopViewAnimation(1.0f);
                                if (!ChatActivityEnterView.this.forceShowSendButton || openKeyboard) {
                                    ChatActivityEnterView.this.openKeyboard();
                                }
                                ChatActivityEnterView.this.currentTopViewAnimation = null;
                            }
                        }
                    });
                    this.currentTopViewAnimation.setDuration(200);
                    this.currentTopViewAnimation.start();
                } else {
                    setTopViewAnimation(1.0f);
                    if (!this.forceShowSendButton || openKeyboard) {
                        openKeyboard();
                    }
                }
            }
        }
    }

    public void hideTopView(boolean animated) {
        if (this.topView != null && this.topViewShowed) {
            this.topViewShowed = false;
            this.needShowTopView = false;
            if (this.allowShowTopView) {
                float resumeValue = 1.0f;
                if (this.currentTopViewAnimation != null) {
                    resumeValue = this.topViewAnimation;
                    this.currentTopViewAnimation.cancel();
                    this.currentTopViewAnimation = null;
                }
                if (animated) {
                    this.currentTopViewAnimation = new AnimatorSetProxy();
                    AnimatorSetProxy animatorSetProxy = this.currentTopViewAnimation;
                    Object[] objArr = new Object[1];
                    objArr[0] = ObjectAnimatorProxy.ofFloat(this, "topViewAnimation", resumeValue, 0.0f);
                    animatorSetProxy.playTogether(objArr);
                    this.currentTopViewAnimation.addListener(new AnimatorListenerAdapterProxy() {
                        public void onAnimationEnd(Object animation) {
                            if (ChatActivityEnterView.this.currentTopViewAnimation != null && ChatActivityEnterView.this.currentTopViewAnimation.equals(animation)) {
                                ChatActivityEnterView.this.topView.setVisibility(8);
                                ChatActivityEnterView.this.setTopViewAnimation(0.0f);
                                ChatActivityEnterView.this.currentTopViewAnimation = null;
                            }
                        }
                    });
                    this.currentTopViewAnimation.setDuration(200);
                    this.currentTopViewAnimation.start();
                    return;
                }
                this.topView.setVisibility(8);
                setTopViewAnimation(0.0f);
            }
        }
    }

    public boolean isTopViewVisible() {
        return this.topView != null && this.topView.getVisibility() == 0;
    }

    private void onWindowSizeChanged() {
        int size = this.sizeNotifierLayout.getHeight();
        if (!this.keyboardVisible) {
            size -= this.emojiPadding;
        }
        if (this.delegate != null) {
            this.delegate.onWindowSizeChanged(size);
        }
        if (this.topView == null) {
            return;
        }
        if (size < AndroidUtilities.dp(72.0f) + ActionBar.getCurrentActionBarHeight()) {
            if (this.allowShowTopView) {
                this.allowShowTopView = false;
                if (this.needShowTopView) {
                    this.topView.setVisibility(8);
                    setTopViewAnimation(0.0f);
                }
            }
        } else if (!this.allowShowTopView) {
            this.allowShowTopView = true;
            if (this.needShowTopView) {
                this.topView.setVisibility(0);
                setTopViewAnimation(1.0f);
            }
        }
    }

    public void onDestroy() {
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.recordStarted);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.recordStartError);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.recordStopped);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.recordProgressChanged);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.closeChats);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.audioDidSent);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.emojiDidLoaded);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.audioRouteChanged);
        if (this.mWakeLock != null) {
            try {
                this.mWakeLock.release();
                this.mWakeLock = null;
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
        }
        if (this.sizeNotifierLayout != null) {
            this.sizeNotifierLayout.setDelegate(null);
        }
    }

    public void onPause() {
        this.isPaused = true;
        closeKeyboard();
    }

    public void onResume() {
        this.isPaused = false;
        if (this.showKeyboardOnResume) {
            this.showKeyboardOnResume = false;
            this.messageEditText.requestFocus();
            AndroidUtilities.showKeyboard(this.messageEditText);
            if (!AndroidUtilities.usingHardwareInput && !this.keyboardVisible) {
                this.waitingForKeyboardOpen = true;
                AndroidUtilities.cancelRunOnUIThread(this.openKeyboardRunnable);
                AndroidUtilities.runOnUIThread(this.openKeyboardRunnable, 100);
            }
        }
    }

    public void setDialogId(long id) {
        int i = 0;
        this.dialog_id = id;
        if (((int) this.dialog_id) < 0) {
            boolean z;
            Chat currentChat = MessagesController.getInstance().getChat(Integer.valueOf(-((int) this.dialog_id)));
            if (!ChatObject.isChannel(currentChat) || (!(currentChat.creator || currentChat.editor) || currentChat.megagroup)) {
                z = false;
            } else {
                z = true;
            }
            this.isAsAdmin = z;
            if (!this.isAsAdmin || currentChat.broadcast) {
                z = false;
            } else {
                z = true;
            }
            this.adminModeAvailable = z;
            if (this.adminModeAvailable) {
                this.isAsAdmin = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0).getBoolean("asadmin_" + this.dialog_id, true);
            }
            if (this.asAdminButton != null) {
                ImageView imageView = this.asAdminButton;
                if (!this.adminModeAvailable) {
                    i = 8;
                }
                imageView.setVisibility(i);
                this.asAdminButton.setImageResource(this.isAsAdmin ? C0553R.drawable.publish_active : C0553R.drawable.publish);
                updateFieldHint();
            }
        }
    }

    private void updateFieldHint() {
        boolean isChannel = false;
        if (((int) this.dialog_id) < 0) {
            Chat chat = MessagesController.getInstance().getChat(Integer.valueOf(-((int) this.dialog_id)));
            isChannel = ChatObject.isChannel(chat) && !chat.megagroup;
        }
        if (isChannel) {
            this.messageEditText.setHint(this.isAsAdmin ? LocaleController.getString("ChannelBroadcast", C0553R.string.ChannelBroadcast) : LocaleController.getString("ChannelComment", C0553R.string.ChannelComment));
        } else {
            this.messageEditText.setHint(LocaleController.getString("TypeMessage", C0553R.string.TypeMessage));
        }
    }

    public void setReplyingMessageObject(MessageObject messageObject) {
        if (messageObject != null) {
            if (this.botMessageObject == null && this.botButtonsMessageObject != this.replyingMessageObject) {
                this.botMessageObject = this.botButtonsMessageObject;
            }
            this.replyingMessageObject = messageObject;
            setButtons(this.replyingMessageObject, true);
        } else if (messageObject == null && this.replyingMessageObject == this.botButtonsMessageObject) {
            this.replyingMessageObject = null;
            setButtons(this.botMessageObject, false);
            this.botMessageObject = null;
        } else {
            this.replyingMessageObject = messageObject;
        }
    }

    public void setWebPage(WebPage webPage, boolean searchWebPages) {
        this.messageWebPage = webPage;
        this.messageWebPageSearch = searchWebPages;
    }

    public boolean isMessageWebPageSearchEnabled() {
        return this.messageWebPageSearch;
    }

    private void sendMessage() {
        if (this.parentFragment != null) {
            String action;
            if (((int) this.dialog_id) < 0) {
                Chat currentChat = MessagesController.getInstance().getChat(Integer.valueOf(-((int) this.dialog_id)));
                if (currentChat == null || currentChat.participants_count <= MessagesController.getInstance().groupBigSize) {
                    action = "chat_message";
                } else {
                    action = "bigchat_message";
                }
            } else {
                action = "pm_message";
            }
            if (!MessagesController.isFeatureEnabled(action, this.parentFragment)) {
                return;
            }
        }
        String message = this.messageEditText.getText().toString();
        if (processSendingText(message)) {
            this.messageEditText.setText("");
            this.lastTypingTimeSend = 0;
            if (this.delegate != null) {
                this.delegate.onMessageSend(message);
            }
        } else if (this.forceShowSendButton && this.delegate != null) {
            this.delegate.onMessageSend(null);
        }
    }

    public boolean processSendingText(String text) {
        text = getTrimmedString(text);
        if (text.length() == 0) {
            return false;
        }
        int count = (int) Math.ceil((double) (((float) text.length()) / 4096.0f));
        for (int a = 0; a < count; a++) {
            SendMessagesHelper.getInstance().sendMessage(text.substring(a * 4096, Math.min((a + 1) * 4096, text.length())), this.dialog_id, this.replyingMessageObject, this.messageWebPage, this.messageWebPageSearch, asAdmin());
        }
        return true;
    }

    private String getTrimmedString(String src) {
        src = src.trim();
        if (src.length() == 0) {
            return src;
        }
        while (src.startsWith("\n")) {
            src = src.substring(1);
        }
        while (src.endsWith("\n")) {
            src = src.substring(0, src.length() - 1);
        }
        return src;
    }

    private void checkSendButton(boolean animated) {
        AnimatorSetProxy animatorSetProxy;
        Object[] objArr;
        if (getTrimmedString(this.messageEditText.getText().toString()).length() > 0 || this.forceShowSendButton) {
            if (this.audioSendButton.getVisibility() != 0) {
                return;
            }
            if (!animated) {
                ViewProxy.setScaleX(this.audioSendButton, 0.1f);
                ViewProxy.setScaleY(this.audioSendButton, 0.1f);
                ViewProxy.setAlpha(this.audioSendButton, 0.0f);
                ViewProxy.setScaleX(this.sendButton, 1.0f);
                ViewProxy.setScaleY(this.sendButton, 1.0f);
                ViewProxy.setAlpha(this.sendButton, 1.0f);
                this.sendButton.setVisibility(0);
                this.audioSendButton.setVisibility(8);
                this.audioSendButton.clearAnimation();
                if (this.attachButton != null) {
                    this.attachButton.setVisibility(8);
                    this.attachButton.clearAnimation();
                    this.delegate.onAttachButtonHidden();
                    updateFieldRight(0);
                }
            } else if (this.runningAnimationType != 1) {
                if (this.runningAnimation != null) {
                    this.runningAnimation.cancel();
                    this.runningAnimation = null;
                }
                if (this.runningAnimation2 != null) {
                    this.runningAnimation2.cancel();
                    this.runningAnimation2 = null;
                }
                if (this.attachButton != null) {
                    this.runningAnimation2 = new AnimatorSetProxy();
                    animatorSetProxy = this.runningAnimation2;
                    objArr = new Object[2];
                    objArr[0] = ObjectAnimatorProxy.ofFloat(this.attachButton, "alpha", 0.0f);
                    objArr[1] = ObjectAnimatorProxy.ofFloat(this.attachButton, "scaleX", 0.0f);
                    animatorSetProxy.playTogether(objArr);
                    this.runningAnimation2.setDuration(100);
                    this.runningAnimation2.addListener(new AnimatorListenerAdapterProxy() {
                        public void onAnimationEnd(Object animation) {
                            if (ChatActivityEnterView.this.runningAnimation2.equals(animation)) {
                                ChatActivityEnterView.this.attachButton.setVisibility(8);
                                ChatActivityEnterView.this.attachButton.clearAnimation();
                            }
                        }
                    });
                    this.runningAnimation2.start();
                    updateFieldRight(0);
                    this.delegate.onAttachButtonHidden();
                }
                this.sendButton.setVisibility(0);
                this.runningAnimation = new AnimatorSetProxy();
                this.runningAnimationType = 1;
                animatorSetProxy = this.runningAnimation;
                objArr = new Object[6];
                objArr[0] = ObjectAnimatorProxy.ofFloat(this.audioSendButton, "scaleX", 0.1f);
                objArr[1] = ObjectAnimatorProxy.ofFloat(this.audioSendButton, "scaleY", 0.1f);
                objArr[2] = ObjectAnimatorProxy.ofFloat(this.audioSendButton, "alpha", 0.0f);
                objArr[3] = ObjectAnimatorProxy.ofFloat(this.sendButton, "scaleX", 1.0f);
                objArr[4] = ObjectAnimatorProxy.ofFloat(this.sendButton, "scaleY", 1.0f);
                objArr[5] = ObjectAnimatorProxy.ofFloat(this.sendButton, "alpha", 1.0f);
                animatorSetProxy.playTogether(objArr);
                this.runningAnimation.setDuration(150);
                this.runningAnimation.addListener(new AnimatorListenerAdapterProxy() {
                    public void onAnimationEnd(Object animation) {
                        if (ChatActivityEnterView.this.runningAnimation != null && ChatActivityEnterView.this.runningAnimation.equals(animation)) {
                            ChatActivityEnterView.this.sendButton.setVisibility(0);
                            ChatActivityEnterView.this.audioSendButton.setVisibility(8);
                            ChatActivityEnterView.this.audioSendButton.clearAnimation();
                            ChatActivityEnterView.this.runningAnimation = null;
                            ChatActivityEnterView.this.runningAnimationType = 0;
                        }
                    }
                });
                this.runningAnimation.start();
            }
        } else if (this.sendButton.getVisibility() != 0) {
        } else {
            if (!animated) {
                ViewProxy.setScaleX(this.sendButton, 0.1f);
                ViewProxy.setScaleY(this.sendButton, 0.1f);
                ViewProxy.setAlpha(this.sendButton, 0.0f);
                ViewProxy.setScaleX(this.audioSendButton, 1.0f);
                ViewProxy.setScaleY(this.audioSendButton, 1.0f);
                ViewProxy.setAlpha(this.audioSendButton, 1.0f);
                this.sendButton.setVisibility(8);
                this.sendButton.clearAnimation();
                this.audioSendButton.setVisibility(0);
                if (this.attachButton != null) {
                    this.delegate.onAttachButtonShow();
                    this.attachButton.setVisibility(0);
                    updateFieldRight(1);
                }
            } else if (this.runningAnimationType != 2) {
                if (this.runningAnimation != null) {
                    this.runningAnimation.cancel();
                    this.runningAnimation = null;
                }
                if (this.runningAnimation2 != null) {
                    this.runningAnimation2.cancel();
                    this.runningAnimation2 = null;
                }
                if (this.attachButton != null) {
                    this.attachButton.setVisibility(0);
                    this.runningAnimation2 = new AnimatorSetProxy();
                    animatorSetProxy = this.runningAnimation2;
                    objArr = new Object[2];
                    objArr[0] = ObjectAnimatorProxy.ofFloat(this.attachButton, "alpha", 1.0f);
                    objArr[1] = ObjectAnimatorProxy.ofFloat(this.attachButton, "scaleX", 1.0f);
                    animatorSetProxy.playTogether(objArr);
                    this.runningAnimation2.setDuration(100);
                    this.runningAnimation2.start();
                    updateFieldRight(1);
                    this.delegate.onAttachButtonShow();
                }
                this.audioSendButton.setVisibility(0);
                this.runningAnimation = new AnimatorSetProxy();
                this.runningAnimationType = 2;
                animatorSetProxy = this.runningAnimation;
                objArr = new Object[6];
                objArr[0] = ObjectAnimatorProxy.ofFloat(this.sendButton, "scaleX", 0.1f);
                objArr[1] = ObjectAnimatorProxy.ofFloat(this.sendButton, "scaleY", 0.1f);
                objArr[2] = ObjectAnimatorProxy.ofFloat(this.sendButton, "alpha", 0.0f);
                objArr[3] = ObjectAnimatorProxy.ofFloat(this.audioSendButton, "scaleX", 1.0f);
                objArr[4] = ObjectAnimatorProxy.ofFloat(this.audioSendButton, "scaleY", 1.0f);
                objArr[5] = ObjectAnimatorProxy.ofFloat(this.audioSendButton, "alpha", 1.0f);
                animatorSetProxy.playTogether(objArr);
                this.runningAnimation.setDuration(150);
                this.runningAnimation.addListener(new AnimatorListenerAdapterProxy() {
                    public void onAnimationEnd(Object animation) {
                        if (ChatActivityEnterView.this.runningAnimation != null && ChatActivityEnterView.this.runningAnimation.equals(animation)) {
                            ChatActivityEnterView.this.sendButton.setVisibility(8);
                            ChatActivityEnterView.this.sendButton.clearAnimation();
                            ChatActivityEnterView.this.audioSendButton.setVisibility(0);
                            ChatActivityEnterView.this.runningAnimation = null;
                            ChatActivityEnterView.this.runningAnimationType = 0;
                        }
                    }
                });
                this.runningAnimation.start();
            }
        }
    }

    private void updateFieldRight(int attachVisible) {
        if (this.messageEditText != null) {
            LayoutParams layoutParams = (LayoutParams) this.messageEditText.getLayoutParams();
            if (attachVisible == 1) {
                if (this.botButton == null || this.botButton.getVisibility() != 0) {
                    layoutParams.rightMargin = AndroidUtilities.dp(50.0f);
                } else {
                    layoutParams.rightMargin = AndroidUtilities.dp(98.0f);
                }
            } else if (attachVisible != 2) {
                layoutParams.rightMargin = AndroidUtilities.dp(2.0f);
            } else if (layoutParams.rightMargin != AndroidUtilities.dp(2.0f)) {
                if (this.botButton == null || this.botButton.getVisibility() != 0) {
                    layoutParams.rightMargin = AndroidUtilities.dp(50.0f);
                } else {
                    layoutParams.rightMargin = AndroidUtilities.dp(98.0f);
                }
            }
            this.messageEditText.setLayoutParams(layoutParams);
        }
    }

    private void updateAudioRecordIntefrace() {
        AnimatorSetProxy animatorSetProxy;
        Object[] objArr;
        if (!this.recordingAudio) {
            if (this.mWakeLock != null) {
                try {
                    this.mWakeLock.release();
                    this.mWakeLock = null;
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
            AndroidUtilities.unlockOrientation(this.parentActivity);
            if (this.audioInterfaceState != 0) {
                this.audioInterfaceState = 0;
                if (this.runningAnimationAudio != null) {
                    this.runningAnimationAudio.cancel();
                }
                this.runningAnimationAudio = new AnimatorSetProxy();
                animatorSetProxy = this.runningAnimationAudio;
                objArr = new Object[3];
                objArr[0] = ObjectAnimatorProxy.ofFloat(this.recordPanel, "translationX", (float) AndroidUtilities.displaySize.x);
                objArr[1] = ObjectAnimatorProxy.ofFloat(this.recordCircle, "scale", 0.0f);
                objArr[2] = ObjectAnimatorProxy.ofFloat(this.audioSendButton, "alpha", 1.0f);
                animatorSetProxy.playTogether(objArr);
                this.runningAnimationAudio.setDuration(300);
                this.runningAnimationAudio.addListener(new AnimatorListenerAdapterProxy() {
                    public void onAnimationEnd(Object animator) {
                        if (ChatActivityEnterView.this.runningAnimationAudio != null && ChatActivityEnterView.this.runningAnimationAudio.equals(animator)) {
                            LayoutParams params = (LayoutParams) ChatActivityEnterView.this.slideText.getLayoutParams();
                            params.leftMargin = AndroidUtilities.dp(BitmapDescriptorFactory.HUE_ORANGE);
                            ChatActivityEnterView.this.slideText.setLayoutParams(params);
                            ViewProxy.setAlpha(ChatActivityEnterView.this.slideText, 1.0f);
                            ChatActivityEnterView.this.recordPanel.setVisibility(8);
                            ChatActivityEnterView.this.recordCircle.setVisibility(8);
                            ChatActivityEnterView.this.runningAnimationAudio = null;
                        }
                    }
                });
                this.runningAnimationAudio.setInterpolator(new AccelerateInterpolator());
                this.runningAnimationAudio.start();
            }
        } else if (this.audioInterfaceState != 1) {
            this.audioInterfaceState = 1;
            try {
                if (this.mWakeLock == null) {
                    this.mWakeLock = ((PowerManager) ApplicationLoader.applicationContext.getSystemService("power")).newWakeLock(536870918, "audio record lock");
                    this.mWakeLock.acquire();
                }
            } catch (Throwable e2) {
                FileLog.m611e("tmessages", e2);
            }
            AndroidUtilities.lockOrientation(this.parentActivity);
            this.recordPanel.setVisibility(0);
            this.recordCircle.setVisibility(0);
            this.recordCircle.setAmplitude(0.0d);
            this.recordTimeText.setText("00:00");
            this.recordDot.resetAlpha();
            this.lastTimeString = null;
            LayoutParams params = (LayoutParams) this.slideText.getLayoutParams();
            params.leftMargin = AndroidUtilities.dp(BitmapDescriptorFactory.HUE_ORANGE);
            this.slideText.setLayoutParams(params);
            ViewProxy.setAlpha(this.slideText, 1.0f);
            ViewProxy.setX(this.recordPanel, (float) AndroidUtilities.displaySize.x);
            ViewProxy.setTranslationX(this.recordCircle, 0.0f);
            if (this.runningAnimationAudio != null) {
                this.runningAnimationAudio.cancel();
            }
            this.runningAnimationAudio = new AnimatorSetProxy();
            animatorSetProxy = this.runningAnimationAudio;
            objArr = new Object[3];
            objArr[0] = ObjectAnimatorProxy.ofFloat(this.recordPanel, "translationX", 0.0f);
            objArr[1] = ObjectAnimatorProxy.ofFloat(this.recordCircle, "scale", 1.0f);
            objArr[2] = ObjectAnimatorProxy.ofFloat(this.audioSendButton, "alpha", 0.0f);
            animatorSetProxy.playTogether(objArr);
            this.runningAnimationAudio.setDuration(300);
            this.runningAnimationAudio.addListener(new AnimatorListenerAdapterProxy() {
                public void onAnimationEnd(Object animator) {
                    if (ChatActivityEnterView.this.runningAnimationAudio != null && ChatActivityEnterView.this.runningAnimationAudio.equals(animator)) {
                        ViewProxy.setX(ChatActivityEnterView.this.recordPanel, 0.0f);
                        ChatActivityEnterView.this.runningAnimationAudio = null;
                    }
                }
            });
            this.runningAnimationAudio.setInterpolator(new DecelerateInterpolator());
            this.runningAnimationAudio.start();
        }
    }

    public void setDelegate(ChatActivityEnterViewDelegate delegate) {
        this.delegate = delegate;
    }

    public void setCommand(MessageObject messageObject, String command, boolean longPress, boolean username) {
        if (command != null && getVisibility() == 0) {
            User user;
            if (longPress) {
                String text = this.messageEditText.getText().toString();
                if (messageObject == null || ((int) this.dialog_id) >= 0) {
                    user = null;
                } else {
                    user = MessagesController.getInstance().getUser(Integer.valueOf(messageObject.messageOwner.from_id));
                }
                if ((this.botCount != 1 || username) && user != null && user.bot && !command.contains("@")) {
                    text = String.format(Locale.US, "%s@%s", new Object[]{command, user.username}) + " " + text.replaceFirst("^/[a-zA-Z@\\d_]{1,255}(\\s|$)", "");
                } else {
                    text = command + " " + text.replaceFirst("^/[a-zA-Z@\\d_]{1,255}(\\s|$)", "");
                }
                this.ignoreTextChange = true;
                this.messageEditText.setText(text);
                this.messageEditText.setSelection(this.messageEditText.getText().length());
                this.ignoreTextChange = false;
                if (!this.keyboardVisible && this.currentPopupContentType == -1) {
                    openKeyboard();
                    return;
                }
                return;
            }
            if (messageObject == null || ((int) this.dialog_id) >= 0) {
                user = null;
            } else {
                user = MessagesController.getInstance().getUser(Integer.valueOf(messageObject.messageOwner.from_id));
            }
            if ((this.botCount != 1 || username) && user != null && user.bot && !command.contains("@")) {
                WebPage webPage = null;
                SendMessagesHelper.getInstance().sendMessage(String.format(Locale.US, "%s@%s", new Object[]{command, user.username}), this.dialog_id, null, webPage, false, asAdmin());
                return;
            }
            SendMessagesHelper.getInstance().sendMessage(command, this.dialog_id, null, null, false, asAdmin());
        }
    }

    public void setFieldText(String text) {
        if (this.messageEditText != null) {
            this.ignoreTextChange = true;
            this.messageEditText.setText(text);
            this.messageEditText.setSelection(this.messageEditText.getText().length());
            this.ignoreTextChange = false;
            if (this.delegate != null) {
                this.delegate.onTextChanged(this.messageEditText.getText(), true);
            }
        }
    }

    public void setSelection(int start) {
        if (this.messageEditText != null) {
            this.messageEditText.setSelection(start, this.messageEditText.length());
        }
    }

    public int getCursorPosition() {
        if (this.messageEditText == null) {
            return 0;
        }
        return this.messageEditText.getSelectionStart();
    }

    public void replaceWithText(int start, int len, String text) {
        try {
            StringBuilder builder = new StringBuilder(this.messageEditText.getText());
            builder.replace(start, start + len, text);
            this.messageEditText.setText(builder);
            this.messageEditText.setSelection(text.length() + start);
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
        }
    }

    public void setFieldFocused(boolean focus) {
        if (this.messageEditText != null) {
            if (focus) {
                if (!this.messageEditText.isFocused()) {
                    this.messageEditText.postDelayed(new Runnable() {
                        public void run() {
                            if (ChatActivityEnterView.this.messageEditText != null) {
                                try {
                                    ChatActivityEnterView.this.messageEditText.requestFocus();
                                } catch (Throwable e) {
                                    FileLog.m611e("tmessages", e);
                                }
                            }
                        }
                    }, 600);
                }
            } else if (this.messageEditText.isFocused() && !this.keyboardVisible) {
                this.messageEditText.clearFocus();
            }
        }
    }

    public boolean hasText() {
        return this.messageEditText != null && this.messageEditText.length() > 0;
    }

    public String getFieldText() {
        if (this.messageEditText == null || this.messageEditText.length() <= 0) {
            return null;
        }
        return this.messageEditText.getText().toString();
    }

    public void addToAttachLayout(View view) {
        if (this.attachButton != null) {
            if (view.getParent() != null) {
                ((ViewGroup) view.getParent()).removeView(view);
            }
            this.attachButton.addView(view, LayoutHelper.createLinear(48, 48));
        }
    }

    private void updateBotButton() {
        if (this.botButton != null) {
            if (this.hasBotCommands || this.botReplyMarkup != null) {
                if (this.botButton.getVisibility() != 0) {
                    this.botButton.setVisibility(0);
                }
                if (this.botReplyMarkup == null) {
                    this.botButton.setImageResource(C0553R.drawable.bot_keyboard);
                } else if (isPopupShowing() && this.currentPopupContentType == 1) {
                    this.botButton.setImageResource(C0553R.drawable.ic_msg_panel_kb);
                } else {
                    this.botButton.setImageResource(C0553R.drawable.bot_keyboard2);
                }
            } else {
                this.botButton.setVisibility(8);
            }
            updateFieldRight(2);
            ViewProxy.setPivotX(this.attachButton, (float) AndroidUtilities.dp(this.botButton.getVisibility() == 8 ? 48.0f : 96.0f));
            this.attachButton.clearAnimation();
        }
    }

    public void setBotsCount(int count, boolean hasCommands) {
        this.botCount = count;
        if (this.hasBotCommands != hasCommands) {
            this.hasBotCommands = hasCommands;
            updateBotButton();
        }
    }

    public void setButtons(MessageObject messageObject) {
        setButtons(messageObject, true);
    }

    public void setButtons(MessageObject messageObject, boolean openKeyboard) {
        TL_replyKeyboardMarkup tL_replyKeyboardMarkup = null;
        if (this.replyingMessageObject != null && this.replyingMessageObject == this.botButtonsMessageObject && this.replyingMessageObject != messageObject) {
            this.botMessageObject = messageObject;
        } else if (this.botButton == null) {
        } else {
            if (this.botButtonsMessageObject != null && this.botButtonsMessageObject == messageObject) {
                return;
            }
            if (this.botButtonsMessageObject != null || messageObject != null) {
                if (this.botKeyboardView == null) {
                    this.botKeyboardView = new BotKeyboardView(this.parentActivity);
                    this.botKeyboardView.setVisibility(8);
                    this.botKeyboardView.setDelegate(new BotKeyboardViewDelegate() {
                        public void didPressedButton(CharSequence text) {
                            MessageObject object = ChatActivityEnterView.this.replyingMessageObject != null ? ChatActivityEnterView.this.replyingMessageObject : ((int) ChatActivityEnterView.this.dialog_id) < 0 ? ChatActivityEnterView.this.botButtonsMessageObject : null;
                            SendMessagesHelper.getInstance().sendMessage(text.toString(), ChatActivityEnterView.this.dialog_id, object, null, false, ChatActivityEnterView.this.asAdmin());
                            if (ChatActivityEnterView.this.replyingMessageObject != null) {
                                ChatActivityEnterView.this.openKeyboardInternal();
                                ChatActivityEnterView.this.setButtons(ChatActivityEnterView.this.botMessageObject, false);
                            } else if (ChatActivityEnterView.this.botButtonsMessageObject.messageOwner.reply_markup.single_use) {
                                ChatActivityEnterView.this.openKeyboardInternal();
                                ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0).edit().putInt("answered_" + ChatActivityEnterView.this.dialog_id, ChatActivityEnterView.this.botButtonsMessageObject.getId()).commit();
                            }
                            if (ChatActivityEnterView.this.delegate != null) {
                                ChatActivityEnterView.this.delegate.onMessageSend(null);
                            }
                        }
                    });
                    this.sizeNotifierLayout.addView(this.botKeyboardView);
                }
                this.botButtonsMessageObject = messageObject;
                TL_replyKeyboardMarkup tL_replyKeyboardMarkup2 = (messageObject == null || !(messageObject.messageOwner.reply_markup instanceof TL_replyKeyboardMarkup)) ? null : (TL_replyKeyboardMarkup) messageObject.messageOwner.reply_markup;
                this.botReplyMarkup = tL_replyKeyboardMarkup2;
                this.botKeyboardView.setPanelHeight(AndroidUtilities.displaySize.x > AndroidUtilities.displaySize.y ? this.keyboardHeightLand : this.keyboardHeight);
                BotKeyboardView botKeyboardView = this.botKeyboardView;
                if (this.botReplyMarkup != null) {
                    tL_replyKeyboardMarkup = this.botReplyMarkup;
                }
                botKeyboardView.setButtons(tL_replyKeyboardMarkup);
                if (this.botReplyMarkup != null) {
                    boolean keyboardHidden;
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0);
                    if (preferences.getInt("hidekeyboard_" + this.dialog_id, 0) == messageObject.getId()) {
                        keyboardHidden = true;
                    } else {
                        keyboardHidden = false;
                    }
                    if (this.botButtonsMessageObject == this.replyingMessageObject || !this.botReplyMarkup.single_use || preferences.getInt("answered_" + this.dialog_id, 0) != messageObject.getId()) {
                        if (!(keyboardHidden || this.messageEditText.length() != 0 || isPopupShowing())) {
                            showPopup(1, 1);
                        }
                    } else {
                        return;
                    }
                } else if (isPopupShowing() && this.currentPopupContentType == 1) {
                    if (openKeyboard) {
                        openKeyboardInternal();
                    } else {
                        showPopup(0, 1);
                    }
                }
                updateBotButton();
            }
        }
    }

    public boolean isPopupView(View view) {
        return view == this.botKeyboardView || view == this.emojiView;
    }

    public boolean isRecordCircle(View view) {
        return view == this.recordCircle;
    }

    private void showPopup(int show, int contentType) {
        if (show == 1) {
            if (contentType == 0 && this.emojiView == null) {
                if (this.parentActivity != null) {
                    this.emojiView = new EmojiView(this.allowStickers, this.parentActivity);
                    this.emojiView.setVisibility(8);
                    this.emojiView.setListener(new Listener() {
                        public boolean onBackspace() {
                            if (ChatActivityEnterView.this.messageEditText.length() == 0) {
                                return false;
                            }
                            ChatActivityEnterView.this.messageEditText.dispatchKeyEvent(new KeyEvent(0, 67));
                            return true;
                        }

                        public void onEmojiSelected(String symbol) {
                            int i = ChatActivityEnterView.this.messageEditText.getSelectionEnd();
                            if (i < 0) {
                                i = 0;
                            }
                            try {
                                ChatActivityEnterView.this.innerTextChange = 2;
                                CharSequence localCharSequence = Emoji.replaceEmoji(symbol, ChatActivityEnterView.this.messageEditText.getPaint().getFontMetricsInt(), AndroidUtilities.dp(20.0f), false);
                                ChatActivityEnterView.this.messageEditText.setText(ChatActivityEnterView.this.messageEditText.getText().insert(i, localCharSequence));
                                int j = i + localCharSequence.length();
                                ChatActivityEnterView.this.messageEditText.setSelection(j, j);
                            } catch (Throwable e) {
                                FileLog.m611e("tmessages", e);
                            } finally {
                                ChatActivityEnterView.this.innerTextChange = 0;
                            }
                        }

                        public void onStickerSelected(Document sticker) {
                            SendMessagesHelper.getInstance().sendSticker(sticker, ChatActivityEnterView.this.dialog_id, ChatActivityEnterView.this.replyingMessageObject, ChatActivityEnterView.this.asAdmin());
                            if (ChatActivityEnterView.this.delegate != null) {
                                ChatActivityEnterView.this.delegate.onMessageSend(null);
                            }
                        }

                        public void onStickersSettingsClick() {
                            if (ChatActivityEnterView.this.parentFragment != null) {
                                ChatActivityEnterView.this.parentFragment.presentFragment(new StickersActivity());
                            }
                        }
                    });
                    this.sizeNotifierLayout.addView(this.emojiView);
                } else {
                    return;
                }
            }
            View currentView = null;
            if (contentType == 0) {
                this.emojiView.setVisibility(0);
                if (!(this.botKeyboardView == null || this.botKeyboardView.getVisibility() == 8)) {
                    this.botKeyboardView.setVisibility(8);
                }
                currentView = this.emojiView;
            } else if (contentType == 1) {
                if (!(this.emojiView == null || this.emojiView.getVisibility() == 8)) {
                    this.emojiView.setVisibility(8);
                }
                this.botKeyboardView.setVisibility(0);
                currentView = this.botKeyboardView;
            }
            this.currentPopupContentType = contentType;
            if (this.keyboardHeight <= 0) {
                this.keyboardHeight = ApplicationLoader.applicationContext.getSharedPreferences("emoji", 0).getInt("kbd_height", AndroidUtilities.dp(200.0f));
            }
            if (this.keyboardHeightLand <= 0) {
                this.keyboardHeightLand = ApplicationLoader.applicationContext.getSharedPreferences("emoji", 0).getInt("kbd_height_land3", AndroidUtilities.dp(200.0f));
            }
            int currentHeight = AndroidUtilities.displaySize.x > AndroidUtilities.displaySize.y ? this.keyboardHeightLand : this.keyboardHeight;
            if (contentType == 1) {
                currentHeight = Math.min(this.botKeyboardView.getKeyboardHeight(), currentHeight);
            }
            if (this.botKeyboardView != null) {
                this.botKeyboardView.setPanelHeight(currentHeight);
            }
            LayoutParams layoutParams = (LayoutParams) currentView.getLayoutParams();
            layoutParams.width = AndroidUtilities.displaySize.x;
            layoutParams.height = currentHeight;
            currentView.setLayoutParams(layoutParams);
            AndroidUtilities.hideKeyboard(this.messageEditText);
            if (this.sizeNotifierLayout != null) {
                this.emojiPadding = currentHeight;
                this.sizeNotifierLayout.requestLayout();
                if (contentType == 0) {
                    this.emojiButton.setImageResource(C0553R.drawable.ic_msg_panel_kb);
                } else if (contentType == 1) {
                    this.emojiButton.setImageResource(C0553R.drawable.ic_msg_panel_smiles);
                }
                updateBotButton();
                onWindowSizeChanged();
                return;
            }
            return;
        }
        if (this.emojiButton != null) {
            this.emojiButton.setImageResource(C0553R.drawable.ic_msg_panel_smiles);
        }
        this.currentPopupContentType = -1;
        if (this.emojiView != null) {
            this.emojiView.setVisibility(8);
        }
        if (this.botKeyboardView != null) {
            this.botKeyboardView.setVisibility(8);
        }
        if (this.sizeNotifierLayout != null) {
            if (show == 0) {
                this.emojiPadding = 0;
            }
            this.sizeNotifierLayout.requestLayout();
            onWindowSizeChanged();
        }
        updateBotButton();
    }

    public void hidePopup(boolean byBackButton) {
        if (isPopupShowing()) {
            if (this.currentPopupContentType == 1 && byBackButton && this.botButtonsMessageObject != null) {
                ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0).edit().putInt("hidekeyboard_" + this.dialog_id, this.botButtonsMessageObject.getId()).commit();
            }
            showPopup(0, 0);
        }
    }

    private void openKeyboardInternal() {
        int i = (AndroidUtilities.usingHardwareInput || this.isPaused) ? 0 : 2;
        showPopup(i, 0);
        this.messageEditText.requestFocus();
        AndroidUtilities.showKeyboard(this.messageEditText);
        if (this.isPaused) {
            this.showKeyboardOnResume = true;
        } else if (!AndroidUtilities.usingHardwareInput && !this.keyboardVisible) {
            this.waitingForKeyboardOpen = true;
            AndroidUtilities.cancelRunOnUIThread(this.openKeyboardRunnable);
            AndroidUtilities.runOnUIThread(this.openKeyboardRunnable, 100);
        }
    }

    public void openKeyboard() {
        AndroidUtilities.showKeyboard(this.messageEditText);
    }

    public void closeKeyboard() {
        AndroidUtilities.hideKeyboard(this.messageEditText);
    }

    public boolean isPopupShowing() {
        return (this.emojiView != null && this.emojiView.getVisibility() == 0) || (this.botKeyboardView != null && this.botKeyboardView.getVisibility() == 0);
    }

    public void onSizeChanged(int height, boolean isWidthGreater) {
        boolean z = true;
        if (height > AndroidUtilities.dp(50.0f) && this.keyboardVisible) {
            if (isWidthGreater) {
                this.keyboardHeightLand = height;
                ApplicationLoader.applicationContext.getSharedPreferences("emoji", 0).edit().putInt("kbd_height_land3", this.keyboardHeightLand).commit();
            } else {
                this.keyboardHeight = height;
                ApplicationLoader.applicationContext.getSharedPreferences("emoji", 0).edit().putInt("kbd_height", this.keyboardHeight).commit();
            }
        }
        if (isPopupShowing()) {
            int newHeight = isWidthGreater ? this.keyboardHeightLand : this.keyboardHeight;
            if (this.currentPopupContentType == 1 && !this.botKeyboardView.isFullSize()) {
                newHeight = Math.min(this.botKeyboardView.getKeyboardHeight(), newHeight);
            }
            View currentView = null;
            if (this.currentPopupContentType == 0) {
                currentView = this.emojiView;
            } else if (this.currentPopupContentType == 1) {
                currentView = this.botKeyboardView;
            }
            if (this.botKeyboardView != null) {
                this.botKeyboardView.setPanelHeight(newHeight);
            }
            LayoutParams layoutParams = (LayoutParams) currentView.getLayoutParams();
            if (!(layoutParams.width == AndroidUtilities.displaySize.x && layoutParams.height == newHeight)) {
                layoutParams.width = AndroidUtilities.displaySize.x;
                layoutParams.height = newHeight;
                currentView.setLayoutParams(layoutParams);
                if (this.sizeNotifierLayout != null) {
                    this.emojiPadding = layoutParams.height;
                    this.sizeNotifierLayout.requestLayout();
                    onWindowSizeChanged();
                }
            }
        }
        if (this.lastSizeChangeValue1 == height && this.lastSizeChangeValue2 == isWidthGreater) {
            onWindowSizeChanged();
            return;
        }
        this.lastSizeChangeValue1 = height;
        this.lastSizeChangeValue2 = isWidthGreater;
        boolean oldValue = this.keyboardVisible;
        if (height <= 0) {
            z = false;
        }
        this.keyboardVisible = z;
        if (this.keyboardVisible && isPopupShowing()) {
            showPopup(0, this.currentPopupContentType);
        }
        if (!(this.emojiPadding == 0 || this.keyboardVisible || this.keyboardVisible == oldValue || isPopupShowing())) {
            this.emojiPadding = 0;
            this.sizeNotifierLayout.requestLayout();
        }
        if (this.keyboardVisible && this.waitingForKeyboardOpen) {
            this.waitingForKeyboardOpen = false;
            AndroidUtilities.cancelRunOnUIThread(this.openKeyboardRunnable);
        }
        onWindowSizeChanged();
    }

    public int getEmojiPadding() {
        return this.emojiPadding;
    }

    public int getEmojiHeight() {
        if (AndroidUtilities.displaySize.x > AndroidUtilities.displaySize.y) {
            return this.keyboardHeightLand;
        }
        return this.keyboardHeight;
    }

    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.emojiDidLoaded) {
            if (this.emojiView != null) {
                this.emojiView.invalidateViews();
            }
            if (this.botKeyboardView != null) {
                this.botKeyboardView.invalidateViews();
            }
        } else if (id == NotificationCenter.recordProgressChanged) {
            String str = String.format("%02d:%02d", new Object[]{Long.valueOf(Long.valueOf(((Long) args[0]).longValue() / 1000).longValue() / 60), Long.valueOf(Long.valueOf(((Long) args[0]).longValue() / 1000).longValue() % 60)});
            if (this.lastTimeString == null || !this.lastTimeString.equals(str)) {
                if (time.longValue() % 5 == 0) {
                    MessagesController.getInstance().sendTyping(this.dialog_id, 1, 0);
                }
                if (this.recordTimeText != null) {
                    this.recordTimeText.setText(str);
                }
            }
            if (this.recordCircle != null) {
                this.recordCircle.setAmplitude(((Double) args[1]).doubleValue());
            }
        } else if (id == NotificationCenter.closeChats) {
            if (this.messageEditText != null && this.messageEditText.isFocused()) {
                AndroidUtilities.hideKeyboard(this.messageEditText);
            }
        } else if (id == NotificationCenter.recordStartError || id == NotificationCenter.recordStopped) {
            if (this.recordingAudio) {
                MessagesController.getInstance().sendTyping(this.dialog_id, 2, 0);
                this.recordingAudio = false;
                updateAudioRecordIntefrace();
            }
        } else if (id == NotificationCenter.recordStarted) {
            if (!this.recordingAudio) {
                this.recordingAudio = true;
                updateAudioRecordIntefrace();
            }
        } else if (id == NotificationCenter.audioDidSent) {
            if (this.delegate != null) {
                this.delegate.onMessageSend(null);
            }
        } else if (id == NotificationCenter.audioRouteChanged && this.parentActivity != null) {
            this.parentActivity.setVolumeControlStream(((Boolean) args[0]).booleanValue() ? 0 : Integer.MIN_VALUE);
        }
    }
}
