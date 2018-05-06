package org.telegram.ui;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.text.style.ClickableSpan;
import android.util.Base64;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.plus.PlusShare;
import java.io.File;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import net.hockeyapp.android.utils.HttpURLConnectionBuilder;
import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.AnimationCompat.AnimatorListenerAdapterProxy;
import org.telegram.messenger.AnimationCompat.AnimatorSetProxy;
import org.telegram.messenger.AnimationCompat.ObjectAnimatorProxy;
import org.telegram.messenger.AnimationCompat.ViewProxy;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.C0553R;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
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
import org.telegram.messenger.NotificationsController;
import org.telegram.messenger.SecretChatHelper;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.VideoEditedInfo;
import org.telegram.messenger.query.BotQuery;
import org.telegram.messenger.query.MessagesSearchQuery;
import org.telegram.messenger.query.ReplyMessageQuery;
import org.telegram.messenger.query.StickersQuery;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView;
import org.telegram.messenger.support.widget.RecyclerView.Adapter;
import org.telegram.messenger.support.widget.RecyclerView.LayoutManager;
import org.telegram.messenger.support.widget.RecyclerView.OnScrollListener;
import org.telegram.messenger.support.widget.RecyclerView.ViewHolder;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.SerializedData;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.tgnet.TLRPC.BotInfo;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.ChatFull;
import org.telegram.tgnet.TLRPC.ChatParticipant;
import org.telegram.tgnet.TLRPC.Document;
import org.telegram.tgnet.TLRPC.EncryptedChat;
import org.telegram.tgnet.TLRPC.FileLocation;
import org.telegram.tgnet.TLRPC.InputStickerSet;
import org.telegram.tgnet.TLRPC.Message;
import org.telegram.tgnet.TLRPC.MessageMedia;
import org.telegram.tgnet.TLRPC.PhotoSize;
import org.telegram.tgnet.TLRPC.TL_botCommand;
import org.telegram.tgnet.TLRPC.TL_channelForbidden;
import org.telegram.tgnet.TLRPC.TL_channelFull;
import org.telegram.tgnet.TLRPC.TL_chatForbidden;
import org.telegram.tgnet.TLRPC.TL_chatFull;
import org.telegram.tgnet.TLRPC.TL_chatParticipantsForbidden;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionSetMessageTTL;
import org.telegram.tgnet.TLRPC.TL_document;
import org.telegram.tgnet.TLRPC.TL_encryptedChat;
import org.telegram.tgnet.TLRPC.TL_encryptedChatDiscarded;
import org.telegram.tgnet.TLRPC.TL_encryptedChatRequested;
import org.telegram.tgnet.TLRPC.TL_encryptedChatWaiting;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_inputPeerUser;
import org.telegram.tgnet.TLRPC.TL_messageActionChatAddUser;
import org.telegram.tgnet.TLRPC.TL_messageActionChatCreate;
import org.telegram.tgnet.TLRPC.TL_messageActionChatDeleteUser;
import org.telegram.tgnet.TLRPC.TL_messageActionChatMigrateTo;
import org.telegram.tgnet.TLRPC.TL_messageEncryptedAction;
import org.telegram.tgnet.TLRPC.TL_messageGroup;
import org.telegram.tgnet.TLRPC.TL_messageMediaDocument;
import org.telegram.tgnet.TLRPC.TL_messageMediaPhoto;
import org.telegram.tgnet.TLRPC.TL_messageMediaVideo;
import org.telegram.tgnet.TLRPC.TL_messageMediaWebPage;
import org.telegram.tgnet.TLRPC.TL_messages_getWebPagePreview;
import org.telegram.tgnet.TLRPC.TL_messages_reportSpam;
import org.telegram.tgnet.TLRPC.TL_peerNotifySettings;
import org.telegram.tgnet.TLRPC.TL_replyKeyboardForceReply;
import org.telegram.tgnet.TLRPC.TL_webPage;
import org.telegram.tgnet.TLRPC.TL_webPageEmpty;
import org.telegram.tgnet.TLRPC.TL_webPagePending;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.WebPage;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.ActionBarLayout;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.ActionBarMenuItem.ActionBarMenuItemSearchListener;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.BottomSheet.BottomSheetDelegate;
import org.telegram.ui.Adapters.MentionsAdapter;
import org.telegram.ui.Adapters.MentionsAdapter.MentionsAdapterDelegate;
import org.telegram.ui.Adapters.StickersAdapter;
import org.telegram.ui.Adapters.StickersAdapter.StickersAdapterDelegate;
import org.telegram.ui.AudioSelectActivity.AudioSelectActivityDelegate;
import org.telegram.ui.Cells.BotHelpCell;
import org.telegram.ui.Cells.BotHelpCell.BotHelpCellDelegate;
import org.telegram.ui.Cells.ChatActionCell;
import org.telegram.ui.Cells.ChatActionCell.ChatActionCellDelegate;
import org.telegram.ui.Cells.ChatAudioCell;
import org.telegram.ui.Cells.ChatBaseCell;
import org.telegram.ui.Cells.ChatBaseCell.ChatBaseCellDelegate;
import org.telegram.ui.Cells.ChatContactCell;
import org.telegram.ui.Cells.ChatContactCell.ChatContactCellDelegate;
import org.telegram.ui.Cells.ChatLoadingCell;
import org.telegram.ui.Cells.ChatMediaCell;
import org.telegram.ui.Cells.ChatMediaCell.ChatMediaCellDelegate;
import org.telegram.ui.Cells.ChatMessageCell;
import org.telegram.ui.Cells.ChatMusicCell;
import org.telegram.ui.Cells.ChatMusicCell.ChatMusicCellDelegate;
import org.telegram.ui.Cells.ChatUnreadCell;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.ChatActivityEnterView;
import org.telegram.ui.Components.ChatActivityEnterView.ChatActivityEnterViewDelegate;
import org.telegram.ui.Components.ChatAttachView;
import org.telegram.ui.Components.ChatAttachView.ChatAttachViewDelegate;
import org.telegram.ui.Components.FrameLayoutFixed;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.NumberTextView;
import org.telegram.ui.Components.PlayerView;
import org.telegram.ui.Components.RadioButton;
import org.telegram.ui.Components.RecordStatusDrawable;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.RecyclerListView.OnInterceptTouchListener;
import org.telegram.ui.Components.RecyclerListView.OnItemClickListener;
import org.telegram.ui.Components.RecyclerListView.OnItemLongClickListener;
import org.telegram.ui.Components.ResourceLoader;
import org.telegram.ui.Components.SendingFileExDrawable;
import org.telegram.ui.Components.ShareFrameLayout;
import org.telegram.ui.Components.SizeNotifierFrameLayout;
import org.telegram.ui.Components.TimerDrawable;
import org.telegram.ui.Components.TypingDotsDrawable;
import org.telegram.ui.Components.URLSpanBotCommand;
import org.telegram.ui.Components.URLSpanNoUnderline;
import org.telegram.ui.Components.URLSpanReplacement;
import org.telegram.ui.Components.WebFrameLayout;
import org.telegram.ui.DialogsActivity.MessagesActivityDelegate;
import org.telegram.ui.DocumentSelectActivity.DocumentSelectActivityDelegate;
import org.telegram.ui.LocationActivity.LocationActivityDelegate;
import org.telegram.ui.PhotoAlbumPickerActivity.PhotoAlbumPickerActivityDelegate;
import org.telegram.ui.PhotoViewer.EmptyPhotoViewerProvider;
import org.telegram.ui.PhotoViewer.PhotoViewerProvider;
import org.telegram.ui.PhotoViewer.PlaceProviderObject;
import org.telegram.ui.VideoEditorActivity.VideoEditorActivityDelegate;

public class ChatActivity extends BaseFragment implements NotificationCenterDelegate, MessagesActivityDelegate, PhotoViewerProvider {
    private static final int attach_audio = 3;
    private static final int attach_contact = 5;
    private static final int attach_document = 4;
    private static final int attach_gallery = 1;
    private static final int attach_location = 6;
    private static final int attach_photo = 0;
    private static final int attach_video = 2;
    private static final int bot_help = 30;
    private static final int bot_settings = 31;
    private static final int chat_enc_timer = 13;
    private static final int chat_menu_attach = 14;
    private static final int clear_history = 15;
    private static final int copy = 10;
    private static final int delete = 12;
    private static final int delete_chat = 16;
    private static final int forward = 11;
    private static final int id_chat_compose_panel = 1000;
    private static final int mute = 18;
    private static final int open_channel_profile = 50;
    private static final int reply = 19;
    private static final int search = 40;
    private static final int search_down = 42;
    private static final int search_up = 41;
    private static final int share_contact = 17;
    private ArrayList<View> actionModeViews = new ArrayList();
    private TextView addContactItem;
    private TextView addToContactsButton;
    private boolean allowStickersPanel;
    private ActionBarMenuItem attachItem;
    private FrameLayout avatarContainer;
    private BackupImageView avatarImageView;
    private MessageObject botButtons;
    private HashMap<Integer, BotInfo> botInfo = new HashMap();
    private MessageObject botReplyButtons;
    private String botUser;
    private int botsCount;
    private FrameLayout bottomOverlay;
    private FrameLayout bottomOverlayChat;
    private TextView bottomOverlayChatText;
    private TextView bottomOverlayText;
    private boolean[] cacheEndReached = new boolean[2];
    private int cantDeleteMessagesCount;
    private int channelMessagesImportant;
    protected ChatActivityEnterView chatActivityEnterView;
    private ChatActivityAdapter chatAdapter;
    private ChatAttachView chatAttachView;
    private BottomSheet chatAttachViewSheet;
    private long chatEnterTime = 0;
    private LinearLayoutManager chatLayoutManager;
    private long chatLeaveTime = 0;
    private RecyclerListView chatListView;
    private ArrayList<ChatMediaCell> chatMediaCellsCache = new ArrayList();
    private ArrayList<ChatMessageCell> chatMessageCellsCache = new ArrayList();
    private Dialog closeChatDialog;
    protected Chat currentChat;
    protected EncryptedChat currentEncryptedChat;
    private String currentPicturePath;
    protected User currentUser;
    private long dialog_id;
    private FrameLayout emptyViewContainer;
    private boolean[] endReached = new boolean[2];
    private boolean first = true;
    private boolean firstLoading = true;
    private int first_unread_id;
    private boolean[] forwardEndReached = new boolean[]{true, true};
    private ArrayList<MessageObject> forwardingMessages;
    private MessageObject forwaringMessage;
    private ArrayList<CharSequence> foundUrls;
    private WebPage foundWebPage;
    private boolean hasBotsCommands;
    private ActionBarMenuItem headerItem;
    private int highlightMessageId = ConnectionsManager.DEFAULT_DATACENTER_ID;
    protected ChatFull info = null;
    private boolean isBroadcast;
    private int lastLoadIndex;
    private CharSequence lastPrintString;
    private String lastStatus;
    private int lastStatusDrawable;
    private int last_message_id = 0;
    private int linkSearchRequestId;
    private boolean loading;
    private boolean loadingForward;
    private int loadsCount;
    private int[] maxDate = new int[]{Integer.MIN_VALUE, Integer.MIN_VALUE};
    private int[] maxMessageId = new int[]{ConnectionsManager.DEFAULT_DATACENTER_ID, ConnectionsManager.DEFAULT_DATACENTER_ID};
    private AnimatorSetProxy mentionListAnimation;
    private ListView mentionListView;
    private MentionsAdapter mentionsAdapter;
    private ActionBarMenuItem menuItem;
    private long mergeDialogId;
    protected ArrayList<MessageObject> messages = new ArrayList();
    private HashMap<String, ArrayList<MessageObject>> messagesByDays = new HashMap();
    private HashMap<Integer, MessageObject>[] messagesDict = new HashMap[]{new HashMap(), new HashMap()};
    private int[] minDate = new int[2];
    private int[] minMessageId = new int[]{Integer.MIN_VALUE, Integer.MIN_VALUE};
    private TextView muteItem;
    private TextView nameTextView;
    private boolean needSelectFromMessageId;
    OnItemClickListener onItemClickListener = new C15442();
    OnItemLongClickListener onItemLongClickListener = new C15391();
    private int onlineCount = -1;
    private TextView onlineTextView;
    private boolean openAnimationEnded;
    private boolean openSearchKeyboard;
    private Runnable openSecretPhotoRunnable = null;
    private ImageView pagedownButton;
    private ObjectAnimatorProxy pagedownButtonAnimation;
    private boolean paused = true;
    private String pendingLinkSearchString;
    private Runnable pendingWebPageTimeoutRunnable;
    private PlayerView playerView;
    private FrameLayout progressView;
    private RadioButton radioButton;
    private boolean readWhenResume = false;
    private int readWithDate;
    private int readWithMid;
    private RecordStatusDrawable recordStatusDrawable;
    private AnimatorSetProxy replyButtonAnimation;
    private ImageView replyIconImageView;
    private FileLocation replyImageLocation;
    private BackupImageView replyImageView;
    private TextView replyNameTextView;
    private TextView replyObjectTextView;
    private MessageObject replyingMessageObject;
    private TextView reportSpamButton;
    private FrameLayout reportSpamContainer;
    private User reportSpamUser;
    private LinearLayout reportSpamView;
    private int returnToMessageId;
    private AnimatorSetProxy runningAnimation;
    private Rect scrollRect = new Rect();
    private MessageObject scrollToMessage;
    private int scrollToMessagePosition = -10000;
    private boolean scrollToTopOnResume;
    private boolean scrollToTopUnReadOnResume;
    private ActionBarMenuItem searchDownItem;
    private ActionBarMenuItem searchItem;
    private ActionBarMenuItem searchUpItem;
    private TextView secretViewStatusTextView;
    private HashMap<Integer, MessageObject>[] selectedMessagesCanCopyIds = new HashMap[]{new HashMap(), new HashMap()};
    private NumberTextView selectedMessagesCountTextView;
    private HashMap<Integer, MessageObject>[] selectedMessagesIds = new HashMap[]{new HashMap(), new HashMap()};
    private MessageObject selectedObject;
    private SendingFileExDrawable sendingFileDrawable;
    private int startLoadFromMessageId;
    private String startVideoEdit = null;
    private float startX = 0.0f;
    private float startY = 0.0f;
    private StickersAdapter stickersAdapter;
    private RecyclerListView stickersListView;
    private FrameLayout stickersPanel;
    private ImageView timeItem;
    private View timeItem2;
    private TimerDrawable timerDrawable;
    private TypingDotsDrawable typingDotsDrawable;
    private MessageObject unreadMessageObject;
    private int unread_to_load;
    private boolean userBlocked = false;
    private Runnable waitingForCharaterEnterRunnable;
    private boolean waitingForImportantLoad;
    private ArrayList<Integer> waitingForLoad = new ArrayList();
    private boolean wasPaused = false;

    class C08868 implements OnClickListener {
        C08868() {
        }

        public void onClick(View v) {
            if (ChatActivity.this.radioButton != null && ChatActivity.this.radioButton.getVisibility() == 0) {
                ChatActivity.this.switchImportantMode(null);
            } else if (ChatActivity.this.currentUser != null) {
                args = new Bundle();
                args.putInt("user_id", ChatActivity.this.currentUser.id);
                if (ChatActivity.this.currentEncryptedChat != null) {
                    args.putLong("dialog_id", ChatActivity.this.dialog_id);
                }
                fragment = new ProfileActivity(args);
                fragment.setPlayProfileAnimation(true);
                ChatActivity.this.presentFragment(fragment);
            } else if (ChatActivity.this.currentChat != null) {
                args = new Bundle();
                args.putInt("chat_id", ChatActivity.this.currentChat.id);
                fragment = new ProfileActivity(args);
                fragment.setChatInfo(ChatActivity.this.info);
                fragment.setPlayProfileAnimation(true);
                ChatActivity.this.presentFragment(fragment);
            }
        }
    }

    class C08879 implements OnClickListener {
        C08879() {
        }

        public void onClick(View v) {
            if (ChatActivity.this.getParentActivity() != null) {
                ChatActivity.this.showDialog(AndroidUtilities.buildTTLAlert(ChatActivity.this.getParentActivity(), ChatActivity.this.currentEncryptedChat).create());
            }
        }
    }

    class C15391 implements OnItemLongClickListener {
        C15391() {
        }

        public boolean onItemClick(View view, int position) {
            if (ChatActivity.this.actionBar.isActionModeShowed()) {
                return false;
            }
            ChatActivity.this.createMenu(view, false);
            return true;
        }
    }

    class C15442 implements OnItemClickListener {
        C15442() {
        }

        public void onItemClick(View view, int position) {
            if (ChatActivity.this.actionBar.isActionModeShowed()) {
                ChatActivity.this.processRowSelect(view);
            } else {
                ChatActivity.this.createMenu(view, true);
            }
        }
    }

    class C15477 extends ActionBarMenuOnItemClick {

        class C08831 implements DialogInterface.OnClickListener {
            C08831() {
            }

            public void onClick(DialogInterface dialogInterface, int i) {
                for (int a = 1; a >= 0; a--) {
                    MessageObject msg;
                    ArrayList<Integer> ids = new ArrayList(ChatActivity.this.selectedMessagesIds[a].keySet());
                    ArrayList<Long> random_ids = null;
                    int channelId = 0;
                    if (!ids.isEmpty()) {
                        msg = (MessageObject) ChatActivity.this.selectedMessagesIds[a].get(ids.get(0));
                        if (null == null && msg.messageOwner.to_id.channel_id != 0) {
                            channelId = msg.messageOwner.to_id.channel_id;
                        }
                    }
                    if (ChatActivity.this.currentEncryptedChat != null) {
                        random_ids = new ArrayList();
                        for (Entry<Integer, MessageObject> entry : ChatActivity.this.selectedMessagesIds[a].entrySet()) {
                            msg = (MessageObject) entry.getValue();
                            if (!(msg.messageOwner.random_id == 0 || msg.type == 10)) {
                                random_ids.add(Long.valueOf(msg.messageOwner.random_id));
                            }
                        }
                    }
                    MessagesController.getInstance().deleteMessages(ids, random_ids, ChatActivity.this.currentEncryptedChat, channelId);
                }
                ChatActivity.this.actionBar.hideActionMode();
            }
        }

        class C08853 implements DialogInterface.OnClickListener {
            C08853() {
            }

            public void onClick(DialogInterface dialogInterface, int i) {
                boolean z;
                SendMessagesHelper instance = SendMessagesHelper.getInstance();
                User currentUser = UserConfig.getCurrentUser();
                long access$1200 = ChatActivity.this.dialog_id;
                MessageObject access$1300 = ChatActivity.this.replyingMessageObject;
                if (ChatActivity.this.chatActivityEnterView == null || ChatActivity.this.chatActivityEnterView.asAdmin()) {
                    z = true;
                } else {
                    z = false;
                }
                instance.sendMessage(currentUser, access$1200, access$1300, z);
                ChatActivity.this.moveScrollToLastMessage();
                ChatActivity.this.showReplyPanel(false, null, null, null, false, true);
            }
        }

        class C15464 implements ChatAttachViewDelegate {
            C15464() {
            }

            public void didPressedButton(int button) {
                if (button == 7) {
                    ChatActivity.this.chatAttachViewSheet.dismiss();
                    HashMap<Integer, PhotoEntry> selectedPhotos = ChatActivity.this.chatAttachView.getSelectedPhotos();
                    if (!selectedPhotos.isEmpty()) {
                        ArrayList<String> photos = new ArrayList();
                        ArrayList<String> captions = new ArrayList();
                        for (Entry<Integer, PhotoEntry> entry : selectedPhotos.entrySet()) {
                            PhotoEntry photoEntry = (PhotoEntry) entry.getValue();
                            if (photoEntry.imagePath != null) {
                                photos.add(photoEntry.imagePath);
                                captions.add(photoEntry.caption != null ? photoEntry.caption.toString() : null);
                            } else if (photoEntry.path != null) {
                                photos.add(photoEntry.path);
                                captions.add(photoEntry.caption != null ? photoEntry.caption.toString() : null);
                            }
                            photoEntry.imagePath = null;
                            photoEntry.thumbPath = null;
                            photoEntry.caption = null;
                        }
                        long access$1200 = ChatActivity.this.dialog_id;
                        MessageObject access$1300 = ChatActivity.this.replyingMessageObject;
                        boolean z = ChatActivity.this.chatActivityEnterView == null || ChatActivity.this.chatActivityEnterView.asAdmin();
                        SendMessagesHelper.prepareSendingPhotos(photos, null, access$1200, access$1300, captions, z);
                        ChatActivity.this.showReplyPanel(false, null, null, null, false, true);
                        return;
                    }
                    return;
                }
                ChatActivity.this.chatAttachViewSheet.dismissWithButtonClick(button);
                ChatActivity.this.processSelectedAttach(button);
            }
        }

        class C17635 extends BottomSheetDelegate {
            C17635() {
            }

            public void onRevealAnimationStart(boolean open) {
                if (ChatActivity.this.chatAttachView != null) {
                    ChatActivity.this.chatAttachView.onRevealAnimationStart(open);
                }
            }

            public void onRevealAnimationProgress(boolean open, float radius, int x, int y) {
                if (ChatActivity.this.chatAttachView != null) {
                    ChatActivity.this.chatAttachView.onRevealAnimationProgress(open, radius, x, y);
                }
            }

            public void onRevealAnimationEnd(boolean open) {
                if (ChatActivity.this.chatAttachView != null) {
                    ChatActivity.this.chatAttachView.onRevealAnimationEnd(open);
                }
            }

            public void onOpenAnimationEnd() {
                if (ChatActivity.this.chatAttachView != null) {
                    ChatActivity.this.chatAttachView.onRevealAnimationEnd(true);
                }
            }

            public View getRevealView() {
                return ChatActivity.this.menuItem;
            }
        }

        C15477() {
        }

        public void onItemClick(int id) {
            int a;
            if (id == -1) {
                if (ChatActivity.this.actionBar.isActionModeShowed()) {
                    for (a = 1; a >= 0; a--) {
                        ChatActivity.this.selectedMessagesIds[a].clear();
                        ChatActivity.this.selectedMessagesCanCopyIds[a].clear();
                    }
                    ChatActivity.this.cantDeleteMessagesCount = 0;
                    ChatActivity.this.actionBar.hideActionMode();
                    ChatActivity.this.updateVisibleRows();
                    return;
                }
                ChatActivity.this.finishFragment();
            } else if (id == 10) {
                String str = "";
                for (a = 1; a >= 0; a--) {
                    ArrayList<Integer> arrayList = new ArrayList(ChatActivity.this.selectedMessagesCanCopyIds[a].keySet());
                    if (ChatActivity.this.currentEncryptedChat == null) {
                        Collections.sort(arrayList);
                    } else {
                        Collections.sort(arrayList, Collections.reverseOrder());
                    }
                    for (int b = 0; b < arrayList.size(); b++) {
                        messageObject = (MessageObject) ChatActivity.this.selectedMessagesCanCopyIds[a].get((Integer) arrayList.get(b));
                        if (str.length() != 0) {
                            str = str + "\n";
                        }
                        if (messageObject.messageOwner.message != null) {
                            str = str + messageObject.messageOwner.message;
                        } else {
                            str = str + messageObject.messageText;
                        }
                    }
                }
                if (str.length() != 0) {
                    try {
                        if (VERSION.SDK_INT < 11) {
                            ((ClipboardManager) ApplicationLoader.applicationContext.getSystemService("clipboard")).setText(str);
                        } else {
                            ((android.content.ClipboardManager) ApplicationLoader.applicationContext.getSystemService("clipboard")).setPrimaryClip(ClipData.newPlainText(PlusShare.KEY_CALL_TO_ACTION_LABEL, str));
                        }
                    } catch (Throwable e) {
                        FileLog.m611e("tmessages", e);
                    }
                }
                for (a = 1; a >= 0; a--) {
                    ChatActivity.this.selectedMessagesIds[a].clear();
                    ChatActivity.this.selectedMessagesCanCopyIds[a].clear();
                }
                ChatActivity.this.cantDeleteMessagesCount = 0;
                ChatActivity.this.actionBar.hideActionMode();
                ChatActivity.this.updateVisibleRows();
            } else if (id == 12) {
                if (ChatActivity.this.getParentActivity() != null) {
                    r0 = new Builder(ChatActivity.this.getParentActivity());
                    r0.setMessage(LocaleController.formatString("AreYouSureDeleteMessages", C0553R.string.AreYouSureDeleteMessages, LocaleController.formatPluralString("messages", ChatActivity.this.selectedMessagesIds[0].size() + ChatActivity.this.selectedMessagesIds[1].size())));
                    r0.setTitle(LocaleController.getString("AppName", C0553R.string.AppName));
                    r0.setPositiveButton(LocaleController.getString("OK", C0553R.string.OK), new C08831());
                    r0.setNegativeButton(LocaleController.getString("Cancel", C0553R.string.Cancel), null);
                    ChatActivity.this.showDialog(r0.create());
                }
            } else if (id == 11) {
                args = new Bundle();
                args.putBoolean("onlySelect", true);
                args.putInt("dialogsType", 1);
                r0 = new DialogsActivity(args);
                r0.setDelegate(ChatActivity.this);
                ChatActivity.this.presentFragment(r0);
            } else if (id == 13) {
                if (ChatActivity.this.getParentActivity() != null) {
                    ChatActivity.this.showDialog(AndroidUtilities.buildTTLAlert(ChatActivity.this.getParentActivity(), ChatActivity.this.currentEncryptedChat).create());
                }
            } else if (id == 15 || id == 16) {
                if (ChatActivity.this.getParentActivity() != null) {
                    boolean isChat = ((int) ChatActivity.this.dialog_id) < 0 && ((int) (ChatActivity.this.dialog_id >> 32)) != 1;
                    r0 = new Builder(ChatActivity.this.getParentActivity());
                    r0.setTitle(LocaleController.getString("AppName", C0553R.string.AppName));
                    if (id == 15) {
                        r0.setMessage(LocaleController.getString("AreYouSureClearHistory", C0553R.string.AreYouSureClearHistory));
                    } else if (isChat) {
                        r0.setMessage(LocaleController.getString("AreYouSureDeleteAndExit", C0553R.string.AreYouSureDeleteAndExit));
                    } else {
                        r0.setMessage(LocaleController.getString("AreYouSureDeleteThisChat", C0553R.string.AreYouSureDeleteThisChat));
                    }
                    final int i = id;
                    final boolean z = isChat;
                    r0.setPositiveButton(LocaleController.getString("OK", C0553R.string.OK), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (i != 15) {
                                if (!z) {
                                    MessagesController.getInstance().deleteDialog(ChatActivity.this.dialog_id, 0);
                                } else if (ChatObject.isNotInChat(ChatActivity.this.currentChat)) {
                                    MessagesController.getInstance().deleteDialog(ChatActivity.this.dialog_id, 0);
                                } else {
                                    MessagesController.getInstance().deleteUserFromChat((int) (-ChatActivity.this.dialog_id), MessagesController.getInstance().getUser(Integer.valueOf(UserConfig.getClientUserId())), null);
                                }
                                ChatActivity.this.finishFragment();
                                return;
                            }
                            MessagesController.getInstance().deleteDialog(ChatActivity.this.dialog_id, 1);
                        }
                    });
                    r0.setNegativeButton(LocaleController.getString("Cancel", C0553R.string.Cancel), null);
                    ChatActivity.this.showDialog(r0.create());
                }
            } else if (id == 17) {
                if (ChatActivity.this.currentUser != null && ChatActivity.this.getParentActivity() != null) {
                    if (ChatActivity.this.currentUser.phone == null || ChatActivity.this.currentUser.phone.length() == 0) {
                        r0 = new Builder(ChatActivity.this.getParentActivity());
                        r0.setMessage(LocaleController.getString("AreYouSureShareMyContactInfo", C0553R.string.AreYouSureShareMyContactInfo));
                        r0.setTitle(LocaleController.getString("AppName", C0553R.string.AppName));
                        r0.setPositiveButton(LocaleController.getString("OK", C0553R.string.OK), new C08853());
                        r0.setNegativeButton(LocaleController.getString("Cancel", C0553R.string.Cancel), null);
                        ChatActivity.this.showDialog(r0.create());
                        return;
                    }
                    args = new Bundle();
                    args.putInt("user_id", ChatActivity.this.currentUser.id);
                    args.putBoolean("addContact", true);
                    ChatActivity.this.presentFragment(new ContactAddActivity(args));
                }
            } else if (id == 18) {
                ChatActivity.this.toggleMute(false);
            } else if (id == 19) {
                messageObject = null;
                a = 1;
                while (a >= 0) {
                    if (messageObject == null && ChatActivity.this.selectedMessagesIds[a].size() == 1) {
                        messageObject = (MessageObject) ChatActivity.this.messagesDict[a].get(new ArrayList(ChatActivity.this.selectedMessagesIds[a].keySet()).get(0));
                    }
                    ChatActivity.this.selectedMessagesIds[a].clear();
                    ChatActivity.this.selectedMessagesCanCopyIds[a].clear();
                    a--;
                }
                if (messageObject != null && messageObject.messageOwner.id > 0) {
                    ChatActivity.this.showReplyPanel(true, messageObject, null, null, false, true);
                }
                ChatActivity.this.cantDeleteMessagesCount = 0;
                ChatActivity.this.actionBar.hideActionMode();
                ChatActivity.this.updateVisibleRows();
            } else if (id == 14) {
                if (ChatActivity.this.getParentActivity() != null) {
                    if (ChatActivity.this.chatAttachView == null) {
                        BottomSheet.Builder builder = new BottomSheet.Builder(ChatActivity.this.getParentActivity());
                        ChatActivity.this.chatAttachView = new ChatAttachView(ChatActivity.this.getParentActivity());
                        ChatActivity.this.chatAttachView.setDelegate(new C15464());
                        builder.setDelegate(new C17635());
                        builder.setApplyTopPaddings(false);
                        builder.setUseRevealAnimation();
                        builder.setCustomView(ChatActivity.this.chatAttachView);
                        ChatActivity.this.chatAttachViewSheet = builder.create();
                    }
                    if (VERSION.SDK_INT == 21 || VERSION.SDK_INT == 22) {
                        ChatActivity.this.chatActivityEnterView.closeKeyboard();
                    }
                    ChatActivity.this.chatAttachView.init(ChatActivity.this);
                    ChatActivity.this.showDialog(ChatActivity.this.chatAttachViewSheet);
                }
            } else if (id == 30) {
                r8 = SendMessagesHelper.getInstance();
                r9 = "/help";
                r10 = ChatActivity.this.dialog_id;
                r15 = ChatActivity.this.chatActivityEnterView == null || ChatActivity.this.chatActivityEnterView.asAdmin();
                r8.sendMessage(r9, r10, null, null, false, r15);
            } else if (id == 31) {
                r8 = SendMessagesHelper.getInstance();
                r9 = "/settings";
                r10 = ChatActivity.this.dialog_id;
                r15 = ChatActivity.this.chatActivityEnterView == null || ChatActivity.this.chatActivityEnterView.asAdmin();
                r8.sendMessage(r9, r10, null, null, false, r15);
            } else if (id == 40) {
                ChatActivity.this.openSearchWithText(null);
            } else if (id == 41) {
                MessagesSearchQuery.searchMessagesInChat(null, ChatActivity.this.dialog_id, ChatActivity.this.mergeDialogId, ChatActivity.this.classGuid, 1);
            } else if (id == 42) {
                MessagesSearchQuery.searchMessagesInChat(null, ChatActivity.this.dialog_id, ChatActivity.this.mergeDialogId, ChatActivity.this.classGuid, 2);
            } else if (id == 50) {
                args = new Bundle();
                args.putInt("chat_id", ChatActivity.this.currentChat.id);
                r0 = new ProfileActivity(args);
                r0.setChatInfo(ChatActivity.this.info);
                r0.setPlayProfileAnimation(true);
                ChatActivity.this.presentFragment(r0);
            }
        }
    }

    public class ChatActivityAdapter extends Adapter {
        private int botInfoRow = -1;
        private boolean isBot;
        private int loadingDownRow;
        private int loadingUpRow;
        private Context mContext;
        private int messagesEndRow;
        private int messagesStartRow;
        private int rowCount;

        class C15481 implements BotHelpCellDelegate {
            C15481() {
            }

            public void didPressUrl(String url) {
                if (url.startsWith("@")) {
                    MessagesController.openByUserName(url.substring(1), ChatActivity.this, 0);
                } else if (url.startsWith("#")) {
                    DialogsActivity fragment = new DialogsActivity(null);
                    fragment.setSearchString(url);
                    ChatActivity.this.presentFragment(fragment);
                } else if (url.startsWith("/")) {
                    ChatActivity.this.chatActivityEnterView.setCommand(null, url, false, false);
                }
            }
        }

        class C15492 implements ChatBaseCellDelegate {
            C15492() {
            }

            public void didPressShare(ChatBaseCell cell) {
                if (ChatActivity.this.getParentActivity() != null) {
                    if (ChatActivity.this.chatActivityEnterView != null) {
                        ChatActivity.this.chatActivityEnterView.closeKeyboard();
                    }
                    BottomSheet.Builder builder = new BottomSheet.Builder(ChatActivityAdapter.this.mContext, true);
                    builder.setCustomView(new ShareFrameLayout(ChatActivityAdapter.this.mContext, builder.create(), cell.getMessageObject())).setApplyTopPaddings(false);
                    builder.setUseFullWidth(false);
                    ChatActivity.this.showDialog(builder.create());
                }
            }

            public void didPressedChannelAvatar(ChatBaseCell cell, Chat chat) {
                if (ChatActivity.this.actionBar.isActionModeShowed()) {
                    ChatActivity.this.processRowSelect(cell);
                } else if (chat != null && chat != ChatActivity.this.currentChat) {
                    Bundle args = new Bundle();
                    args.putInt("chat_id", chat.id);
                    ChatActivity.this.presentFragment(new ChatActivity(args), true);
                }
            }

            public void didPressedUserAvatar(ChatBaseCell cell, User user) {
                if (ChatActivity.this.actionBar.isActionModeShowed()) {
                    ChatActivity.this.processRowSelect(cell);
                } else if (user != null && user.id != UserConfig.getClientUserId()) {
                    Bundle args = new Bundle();
                    args.putInt("user_id", user.id);
                    ProfileActivity fragment = new ProfileActivity(args);
                    boolean z = ChatActivity.this.currentUser != null && ChatActivity.this.currentUser.id == user.id;
                    fragment.setPlayProfileAnimation(z);
                    ChatActivity.this.presentFragment(fragment);
                }
            }

            public void didPressedCancelSendButton(ChatBaseCell cell) {
                MessageObject message = cell.getMessageObject();
                if (message.messageOwner.send_state != 0) {
                    SendMessagesHelper.getInstance().cancelSendingMessage(message);
                }
            }

            public void didLongPressed(ChatBaseCell cell) {
                ChatActivity.this.createMenu(cell, false);
            }

            public boolean canPerformActions() {
                return (ChatActivity.this.actionBar == null || ChatActivity.this.actionBar.isActionModeShowed()) ? false : true;
            }

            public void didPressUrl(MessageObject messageObject, final ClickableSpan url, boolean longPress) {
                boolean z = true;
                if (url instanceof URLSpanNoUnderline) {
                    String str = ((URLSpanNoUnderline) url).getURL();
                    if (str.startsWith("@")) {
                        MessagesController.openByUserName(str.substring(1), ChatActivity.this, 0);
                    } else if (str.startsWith("#")) {
                        if (ChatObject.isChannel(ChatActivity.this.currentChat)) {
                            ChatActivity.this.openSearchWithText(str);
                            return;
                        }
                        DialogsActivity fragment = new DialogsActivity(null);
                        fragment.setSearchString(str);
                        ChatActivity.this.presentFragment(fragment);
                    } else if (str.startsWith("/") && URLSpanBotCommand.enabled) {
                        ChatActivityEnterView chatActivityEnterView = ChatActivity.this.chatActivityEnterView;
                        if (ChatActivity.this.currentChat == null || !ChatActivity.this.currentChat.megagroup) {
                            z = false;
                        }
                        chatActivityEnterView.setCommand(messageObject, str, longPress, z);
                    }
                } else if (url instanceof URLSpanReplacement) {
                    Builder builder = new Builder(ChatActivity.this.getParentActivity());
                    builder.setMessage(LocaleController.formatString("OpenUrlAlert", C0553R.string.OpenUrlAlert, ((URLSpanReplacement) url).getURL()));
                    builder.setTitle(LocaleController.getString("AppName", C0553R.string.AppName));
                    builder.setPositiveButton(LocaleController.getString("Open", C0553R.string.Open), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            try {
                                url.onClick(ChatActivity.this.fragmentView);
                            } catch (Throwable e) {
                                FileLog.m611e("tmessages", e);
                            }
                        }
                    });
                    builder.setNegativeButton(LocaleController.getString("Cancel", C0553R.string.Cancel), null);
                    ChatActivity.this.showDialog(builder.create());
                } else {
                    url.onClick(ChatActivity.this.fragmentView);
                }
            }

            public void needOpenWebView(String url, String title, String originalUrl, int w, int h) {
                BottomSheet.Builder builder = new BottomSheet.Builder(ChatActivityAdapter.this.mContext);
                builder.setCustomView(new WebFrameLayout(ChatActivityAdapter.this.mContext, builder.create(), title, originalUrl, url, w, h));
                builder.setUseFullWidth(true);
                ChatActivity.this.showDialog(builder.create());
            }

            public void didPressReplyMessage(ChatBaseCell cell, int id) {
                int i = 0;
                MessageObject messageObject = cell.getMessageObject();
                if (!(messageObject.replyMessageObject == null || messageObject.replyMessageObject.isImportant() || ChatActivity.this.channelMessagesImportant != 2)) {
                    boolean z;
                    ChatActivity.this.channelMessagesImportant = 1;
                    RadioButton access$2600 = ChatActivity.this.radioButton;
                    if (ChatActivity.this.channelMessagesImportant == 1) {
                        z = true;
                    } else {
                        z = false;
                    }
                    access$2600.setChecked(z, false);
                }
                ChatActivity chatActivity = ChatActivity.this;
                int id2 = messageObject.getId();
                if (messageObject.getDialogId() == ChatActivity.this.mergeDialogId) {
                    i = 1;
                }
                chatActivity.scrollToMessageId(id, id2, true, i);
            }

            public void didClickedImage(ChatBaseCell cell) {
                Intent intent;
                MessageObject message = cell.getMessageObject();
                if (message.isSendError()) {
                    ChatActivity.this.createMenu(cell, false);
                } else if (!message.isSending()) {
                    if (message.type == 1 || message.type == 0) {
                        PhotoViewer.getInstance().setParentActivity(ChatActivity.this.getParentActivity());
                        PhotoViewer.getInstance().openPhoto(message, message.contentType == 1 ? ChatActivity.this.dialog_id : 0, message.contentType == 1 ? ChatActivity.this.mergeDialogId : 0, ChatActivity.this);
                    } else if (message.type == 3) {
                        ChatActivity.this.sendSecretMessageRead(message);
                        f = null;
                        try {
                            if (!(message.messageOwner.attachPath == null || message.messageOwner.attachPath.length() == 0)) {
                                f = new File(message.messageOwner.attachPath);
                            }
                            if (f == null || !f.exists()) {
                                f = FileLoader.getPathToMessage(message.messageOwner);
                            }
                            intent = new Intent("android.intent.action.VIEW");
                            intent.setDataAndType(Uri.fromFile(f), "video/mp4");
                            ChatActivity.this.getParentActivity().startActivityForResult(intent, 500);
                        } catch (Exception e) {
                            ChatActivity.this.alertUserOpenError(message);
                        }
                    } else if (message.type == 4) {
                        if (ChatActivity.this.isGoogleMapsInstalled()) {
                            LocationActivity fragment = new LocationActivity();
                            fragment.setMessageObject(message);
                            ChatActivity.this.presentFragment(fragment);
                        }
                    } else if (message.type == 9) {
                        f = null;
                        String fileName = message.getFileName();
                        if (!(message.messageOwner.attachPath == null || message.messageOwner.attachPath.length() == 0)) {
                            f = new File(message.messageOwner.attachPath);
                        }
                        if (f == null || !f.exists()) {
                            f = FileLoader.getPathToMessage(message.messageOwner);
                        }
                        if (f != null && f.exists()) {
                            String realMimeType = null;
                            try {
                                intent = new Intent("android.intent.action.VIEW");
                                if (message.type == 8 || message.type == 9) {
                                    MimeTypeMap myMime = MimeTypeMap.getSingleton();
                                    int idx = fileName.lastIndexOf(".");
                                    if (idx != -1) {
                                        realMimeType = myMime.getMimeTypeFromExtension(fileName.substring(idx + 1).toLowerCase());
                                        if (realMimeType == null) {
                                            realMimeType = message.messageOwner.media.document.mime_type;
                                            if (realMimeType == null || realMimeType.length() == 0) {
                                                realMimeType = null;
                                            }
                                        }
                                        if (realMimeType != null) {
                                            intent.setDataAndType(Uri.fromFile(f), realMimeType);
                                        } else {
                                            intent.setDataAndType(Uri.fromFile(f), "text/plain");
                                        }
                                    } else {
                                        intent.setDataAndType(Uri.fromFile(f), "text/plain");
                                    }
                                }
                                if (realMimeType != null) {
                                    try {
                                        ChatActivity.this.getParentActivity().startActivityForResult(intent, 500);
                                        return;
                                    } catch (Exception e2) {
                                        intent.setDataAndType(Uri.fromFile(f), "text/plain");
                                        ChatActivity.this.getParentActivity().startActivityForResult(intent, 500);
                                        return;
                                    }
                                }
                                ChatActivity.this.getParentActivity().startActivityForResult(intent, 500);
                            } catch (Exception e3) {
                                ChatActivity.this.alertUserOpenError(message);
                            }
                        }
                    }
                }
            }
        }

        class C15503 implements ChatMediaCellDelegate {
            C15503() {
            }

            public void didPressedOther(ChatMediaCell cell) {
                ChatActivity.this.createMenu(cell, true);
            }
        }

        class C15514 implements ChatContactCellDelegate {
            C15514() {
            }

            public void didClickAddButton(ChatContactCell cell, User user) {
                if (ChatActivity.this.actionBar.isActionModeShowed()) {
                    ChatActivity.this.processRowSelect(cell);
                    return;
                }
                MessageObject messageObject = cell.getMessageObject();
                Bundle args = new Bundle();
                args.putInt("user_id", messageObject.messageOwner.media.user_id);
                args.putString("phone", messageObject.messageOwner.media.phone_number);
                args.putBoolean("addContact", true);
                ChatActivity.this.presentFragment(new ContactAddActivity(args));
            }

            public void didClickPhone(ChatContactCell cell) {
                if (ChatActivity.this.actionBar.isActionModeShowed()) {
                    ChatActivity.this.processRowSelect(cell);
                    return;
                }
                final MessageObject messageObject = cell.getMessageObject();
                if (ChatActivity.this.getParentActivity() != null && messageObject.messageOwner.media.phone_number != null && messageObject.messageOwner.media.phone_number.length() != 0) {
                    Builder builder = new Builder(ChatActivity.this.getParentActivity());
                    builder.setItems(new CharSequence[]{LocaleController.getString("Copy", C0553R.string.Copy), LocaleController.getString("Call", C0553R.string.Call)}, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (i == 1) {
                                try {
                                    Intent intent = new Intent("android.intent.action.DIAL", Uri.parse("tel:" + messageObject.messageOwner.media.phone_number));
                                    intent.addFlags(268435456);
                                    ChatActivity.this.getParentActivity().startActivityForResult(intent, 500);
                                } catch (Throwable e) {
                                    FileLog.m611e("tmessages", e);
                                }
                            } else if (i == 0) {
                                try {
                                    if (VERSION.SDK_INT < 11) {
                                        ((ClipboardManager) ApplicationLoader.applicationContext.getSystemService("clipboard")).setText(messageObject.messageOwner.media.phone_number);
                                    } else {
                                        ((android.content.ClipboardManager) ApplicationLoader.applicationContext.getSystemService("clipboard")).setPrimaryClip(ClipData.newPlainText(PlusShare.KEY_CALL_TO_ACTION_LABEL, messageObject.messageOwner.media.phone_number));
                                    }
                                } catch (Throwable e2) {
                                    FileLog.m611e("tmessages", e2);
                                }
                            }
                        }
                    });
                    ChatActivity.this.showDialog(builder.create());
                }
            }
        }

        class C15525 implements ChatMusicCellDelegate {
            C15525() {
            }

            public boolean needPlayMusic(MessageObject messageObject) {
                return MediaController.getInstance().setPlaylist(ChatActivity.this.messages, messageObject);
            }
        }

        class C15536 implements ChatActionCellDelegate {
            C15536() {
            }

            public void didClickedImage(ChatActionCell cell) {
                MessageObject message = cell.getMessageObject();
                PhotoViewer.getInstance().setParentActivity(ChatActivity.this.getParentActivity());
                PhotoViewer.getInstance().openPhoto(message, 0, 0, ChatActivity.this);
            }

            public void didLongPressed(ChatActionCell cell) {
                ChatActivity.this.createMenu(cell, false);
            }

            public void needOpenUserProfile(int uid) {
                boolean z = true;
                Bundle args;
                if (uid < 0) {
                    args = new Bundle();
                    args.putInt("chat_id", -uid);
                    ChatActivity.this.presentFragment(new ChatActivity(args), true);
                } else if (uid != UserConfig.getClientUserId()) {
                    args = new Bundle();
                    args.putInt("user_id", uid);
                    if (ChatActivity.this.currentEncryptedChat != null && uid == ChatActivity.this.currentUser.id) {
                        args.putLong("dialog_id", ChatActivity.this.dialog_id);
                    }
                    ProfileActivity fragment = new ProfileActivity(args);
                    if (ChatActivity.this.currentUser == null || ChatActivity.this.currentUser.id != uid) {
                        z = false;
                    }
                    fragment.setPlayProfileAnimation(z);
                    ChatActivity.this.presentFragment(fragment);
                }
            }
        }

        private class Holder extends ViewHolder {
            public Holder(View itemView) {
                super(itemView);
            }
        }

        public ChatActivityAdapter(Context context) {
            this.mContext = context;
            boolean z = ChatActivity.this.currentUser != null && ChatActivity.this.currentUser.bot;
            this.isBot = z;
        }

        public void updateRows() {
            this.rowCount = 0;
            if (ChatActivity.this.currentUser == null || !ChatActivity.this.currentUser.bot) {
                this.botInfoRow = -1;
            } else {
                int i = this.rowCount;
                this.rowCount = i + 1;
                this.botInfoRow = i;
            }
            if (ChatActivity.this.messages.isEmpty()) {
                this.loadingUpRow = -1;
                this.loadingDownRow = -1;
                this.messagesStartRow = -1;
                this.messagesEndRow = -1;
                return;
            }
            if (ChatActivity.this.endReached[0] && (ChatActivity.this.mergeDialogId == 0 || ChatActivity.this.endReached[1])) {
                this.loadingUpRow = -1;
            } else {
                i = this.rowCount;
                this.rowCount = i + 1;
                this.loadingUpRow = i;
            }
            this.messagesStartRow = this.rowCount;
            this.rowCount += ChatActivity.this.messages.size();
            this.messagesEndRow = this.rowCount;
            if (ChatActivity.this.forwardEndReached[0] && (ChatActivity.this.mergeDialogId == 0 || ChatActivity.this.forwardEndReached[1])) {
                this.loadingDownRow = -1;
                return;
            }
            i = this.rowCount;
            this.rowCount = i + 1;
            this.loadingDownRow = i;
        }

        public int getItemCount() {
            return this.rowCount;
        }

        public long getItemId(int i) {
            return -1;
        }

        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = null;
            if (viewType == 0) {
                if (ChatActivity.this.chatMessageCellsCache.isEmpty()) {
                    view = new ChatMessageCell(this.mContext);
                } else {
                    view = (View) ChatActivity.this.chatMessageCellsCache.get(0);
                    ChatActivity.this.chatMessageCellsCache.remove(0);
                }
            } else if (viewType == 1) {
                if (ChatActivity.this.chatMediaCellsCache.isEmpty()) {
                    view = new ChatMediaCell(this.mContext);
                } else {
                    view = (View) ChatActivity.this.chatMediaCellsCache.get(0);
                    ChatActivity.this.chatMediaCellsCache.remove(0);
                }
            } else if (viewType == 2) {
                view = new ChatAudioCell(this.mContext);
            } else if (viewType == 3) {
                view = new ChatContactCell(this.mContext);
            } else if (viewType == 4) {
                view = new ChatActionCell(this.mContext);
            } else if (viewType == 5) {
                view = new ChatLoadingCell(this.mContext);
            } else if (viewType == 6) {
                view = new ChatUnreadCell(this.mContext);
            } else if (viewType == 7) {
                view = new BotHelpCell(this.mContext);
                ((BotHelpCell) view).setDelegate(new C15481());
            } else if (viewType == 8) {
                view = new ChatMusicCell(this.mContext);
            }
            if (view instanceof ChatBaseCell) {
                if (ChatActivity.this.currentEncryptedChat == null) {
                    ((ChatBaseCell) view).setAllowAssistant(true);
                }
                ((ChatBaseCell) view).setDelegate(new C15492());
                if (view instanceof ChatMediaCell) {
                    ((ChatMediaCell) view).setAllowedToSetPhoto(ChatActivity.this.openAnimationEnded);
                    ((ChatMediaCell) view).setMediaDelegate(new C15503());
                } else if (view instanceof ChatContactCell) {
                    ((ChatContactCell) view).setContactDelegate(new C15514());
                } else if (view instanceof ChatMusicCell) {
                    ((ChatMusicCell) view).setMusicDelegate(new C15525());
                }
            } else if (view instanceof ChatActionCell) {
                ((ChatActionCell) view).setDelegate(new C15536());
            }
            return new Holder(view);
        }

        public void onBindViewHolder(ViewHolder holder, int position) {
            if (position == this.botInfoRow) {
                String str;
                BotHelpCell helpView = holder.itemView;
                if (ChatActivity.this.botInfo.isEmpty()) {
                    str = null;
                } else {
                    str = ((BotInfo) ChatActivity.this.botInfo.get(Integer.valueOf(ChatActivity.this.currentUser.id))).description;
                }
                helpView.setText(str);
            } else if (position == this.loadingDownRow || position == this.loadingUpRow) {
                holder.itemView.setProgressVisible(ChatActivity.this.loadsCount > 1);
            } else if (position >= this.messagesStartRow && position < this.messagesEndRow) {
                MessageObject message = (MessageObject) ChatActivity.this.messages.get((ChatActivity.this.messages.size() - (position - this.messagesStartRow)) - 1);
                View view = holder.itemView;
                boolean selected = false;
                boolean disableSelection = false;
                if (ChatActivity.this.actionBar.isActionModeShowed()) {
                    if (ChatActivity.this.selectedMessagesIds[message.getDialogId() == ChatActivity.this.dialog_id ? 0 : 1].containsKey(Integer.valueOf(message.getId()))) {
                        view.setBackgroundColor(1714664933);
                        selected = true;
                    } else {
                        view.setBackgroundColor(0);
                    }
                    disableSelection = true;
                } else {
                    view.setBackgroundColor(0);
                }
                if (view instanceof ChatBaseCell) {
                    ChatBaseCell baseCell = (ChatBaseCell) view;
                    baseCell.isChat = ChatActivity.this.currentChat != null;
                    baseCell.setMessageObject(message);
                    boolean z = !disableSelection;
                    boolean z2 = disableSelection && selected;
                    baseCell.setCheckPressed(z, z2);
                    if ((view instanceof ChatAudioCell) && MediaController.getInstance().canDownloadMedia(2)) {
                        ((ChatAudioCell) view).downloadAudioIfNeed();
                    }
                    if (ChatActivity.this.highlightMessageId == ConnectionsManager.DEFAULT_DATACENTER_ID || message.getId() != ChatActivity.this.highlightMessageId) {
                        z2 = false;
                    } else {
                        z2 = true;
                    }
                    baseCell.setHighlighted(z2);
                } else if (view instanceof ChatActionCell) {
                    ((ChatActionCell) view).setMessageObject(message);
                } else if (view instanceof ChatUnreadCell) {
                    ((ChatUnreadCell) view).setText(LocaleController.formatPluralString("NewMessages", ChatActivity.this.unread_to_load));
                }
            }
        }

        public int getItemViewType(int position) {
            if (position == this.loadingUpRow || position == this.loadingDownRow) {
                return 5;
            }
            if (position == this.botInfoRow) {
                return 7;
            }
            if (position < this.messagesStartRow || position >= this.messagesEndRow) {
                return 5;
            }
            return ((MessageObject) ChatActivity.this.messages.get((ChatActivity.this.messages.size() - (position - this.messagesStartRow)) - 1)).contentType;
        }

        public void onViewAttachedToWindow(ViewHolder holder) {
            if (holder.itemView instanceof ChatBaseCell) {
                ChatBaseCell baseCell = holder.itemView;
                boolean z = ChatActivity.this.highlightMessageId != ConnectionsManager.DEFAULT_DATACENTER_ID && baseCell.getMessageObject().getId() == ChatActivity.this.highlightMessageId;
                baseCell.setHighlighted(z);
            }
            if (holder.itemView instanceof ChatMessageCell) {
                final ChatMessageCell messageCell = holder.itemView;
                messageCell.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
                    public boolean onPreDraw() {
                        messageCell.getViewTreeObserver().removeOnPreDrawListener(this);
                        messageCell.getLocalVisibleRect(ChatActivity.this.scrollRect);
                        messageCell.setVisiblePart(ChatActivity.this.scrollRect.top, ChatActivity.this.scrollRect.bottom - ChatActivity.this.scrollRect.top);
                        return true;
                    }
                });
            }
        }

        public void updateRowWithMessageObject(MessageObject messageObject) {
            int index = ChatActivity.this.messages.indexOf(messageObject);
            if (index != -1) {
                notifyItemChanged(((this.messagesStartRow + ChatActivity.this.messages.size()) - index) - 1);
            }
        }

        public void removeMessageObject(MessageObject messageObject) {
            int index = ChatActivity.this.messages.indexOf(messageObject);
            if (index != -1) {
                ChatActivity.this.messages.remove(index);
                notifyItemRemoved(((this.messagesStartRow + ChatActivity.this.messages.size()) - index) - 1);
            }
        }

        public void notifyDataSetChanged() {
            updateRows();
            try {
                super.notifyDataSetChanged();
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
        }

        public void notifyItemChanged(int position) {
            updateRows();
            try {
                super.notifyItemChanged(position);
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
        }

        public void notifyItemRangeChanged(int positionStart, int itemCount) {
            updateRows();
            try {
                super.notifyItemRangeChanged(positionStart, itemCount);
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
        }

        public void notifyItemInserted(int position) {
            updateRows();
            try {
                super.notifyItemInserted(position);
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
        }

        public void notifyItemMoved(int fromPosition, int toPosition) {
            updateRows();
            try {
                super.notifyItemMoved(fromPosition, toPosition);
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
        }

        public void notifyItemRangeInserted(int positionStart, int itemCount) {
            updateRows();
            try {
                super.notifyItemRangeInserted(positionStart, itemCount);
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
        }

        public void notifyItemRemoved(int position) {
            updateRows();
            try {
                super.notifyItemRemoved(position);
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
        }

        public void notifyItemRangeRemoved(int positionStart, int itemCount) {
            updateRows();
            try {
                super.notifyItemRangeRemoved(positionStart, itemCount);
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
        }
    }

    public ChatActivity(Bundle args) {
        super(args);
    }

    public boolean onFragmentCreate() {
        Semaphore semaphore;
        int chatId = this.arguments.getInt("chat_id", 0);
        int userId = this.arguments.getInt("user_id", 0);
        int encId = this.arguments.getInt("enc_id", 0);
        this.startLoadFromMessageId = this.arguments.getInt("message_id", 0);
        int migrated_to = this.arguments.getInt("migrated_to", 0);
        this.scrollToTopOnResume = this.arguments.getBoolean("scrollToTopOnResume", false);
        final int i;
        final Semaphore semaphore2;
        if (chatId != 0) {
            this.currentChat = MessagesController.getInstance().getChat(Integer.valueOf(chatId));
            if (this.currentChat == null) {
                semaphore = new Semaphore(0);
                i = chatId;
                semaphore2 = semaphore;
                MessagesStorage.getInstance().getStorageQueue().postRunnable(new Runnable() {
                    public void run() {
                        ChatActivity.this.currentChat = MessagesStorage.getInstance().getChat(i);
                        semaphore2.release();
                    }
                });
                try {
                    semaphore.acquire();
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
                if (this.currentChat == null) {
                    return false;
                }
                MessagesController.getInstance().putChat(this.currentChat, true);
            }
            if (chatId > 0) {
                this.dialog_id = (long) (-chatId);
            } else {
                this.isBroadcast = true;
                this.dialog_id = AndroidUtilities.makeBroadcastId(chatId);
            }
            if (ChatObject.isChannel(this.currentChat)) {
                if (this.currentChat.megagroup) {
                    this.channelMessagesImportant = 1;
                } else {
                    this.channelMessagesImportant = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0).getInt("important_" + this.dialog_id, 2);
                }
                MessagesController.getInstance().startShortPoll(chatId, false);
            }
        } else if (userId != 0) {
            this.currentUser = MessagesController.getInstance().getUser(Integer.valueOf(userId));
            if (this.currentUser == null) {
                semaphore = new Semaphore(0);
                i = userId;
                semaphore2 = semaphore;
                MessagesStorage.getInstance().getStorageQueue().postRunnable(new Runnable() {
                    public void run() {
                        ChatActivity.this.currentUser = MessagesStorage.getInstance().getUser(i);
                        semaphore2.release();
                    }
                });
                try {
                    semaphore.acquire();
                } catch (Throwable e2) {
                    FileLog.m611e("tmessages", e2);
                }
                if (this.currentUser == null) {
                    return false;
                }
                MessagesController.getInstance().putUser(this.currentUser, true);
            }
            this.dialog_id = (long) userId;
            this.botUser = this.arguments.getString("botUser");
        } else if (encId == 0) {
            return false;
        } else {
            this.currentEncryptedChat = MessagesController.getInstance().getEncryptedChat(Integer.valueOf(encId));
            if (this.currentEncryptedChat == null) {
                semaphore = new Semaphore(0);
                i = encId;
                semaphore2 = semaphore;
                MessagesStorage.getInstance().getStorageQueue().postRunnable(new Runnable() {
                    public void run() {
                        ChatActivity.this.currentEncryptedChat = MessagesStorage.getInstance().getEncryptedChat(i);
                        semaphore2.release();
                    }
                });
                try {
                    semaphore.acquire();
                } catch (Throwable e22) {
                    FileLog.m611e("tmessages", e22);
                }
                if (this.currentEncryptedChat == null) {
                    return false;
                }
                MessagesController.getInstance().putEncryptedChat(this.currentEncryptedChat, true);
            }
            this.currentUser = MessagesController.getInstance().getUser(Integer.valueOf(this.currentEncryptedChat.user_id));
            if (this.currentUser == null) {
                semaphore = new Semaphore(0);
                final Semaphore semaphore3 = semaphore;
                MessagesStorage.getInstance().getStorageQueue().postRunnable(new Runnable() {
                    public void run() {
                        ChatActivity.this.currentUser = MessagesStorage.getInstance().getUser(ChatActivity.this.currentEncryptedChat.user_id);
                        semaphore3.release();
                    }
                });
                try {
                    semaphore.acquire();
                } catch (Throwable e222) {
                    FileLog.m611e("tmessages", e222);
                }
                if (this.currentUser == null) {
                    return false;
                }
                MessagesController.getInstance().putUser(this.currentUser, true);
            }
            this.dialog_id = ((long) encId) << 32;
            int[] iArr = this.maxMessageId;
            this.maxMessageId[1] = Integer.MIN_VALUE;
            iArr[0] = Integer.MIN_VALUE;
            iArr = this.minMessageId;
            this.minMessageId[1] = ConnectionsManager.DEFAULT_DATACENTER_ID;
            iArr[0] = ConnectionsManager.DEFAULT_DATACENTER_ID;
            MediaController.getInstance().startMediaObserver();
        }
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.messagesDidLoaded);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.emojiDidLoaded);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.updateInterfaces);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.didReceivedNewMessages);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.closeChats);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.messagesRead);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.messagesDeleted);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.messageReceivedByServer);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.messageReceivedByAck);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.messageSendError);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.chatInfoDidLoaded);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.contactsDidLoaded);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.encryptedChatUpdated);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.messagesReadEncrypted);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.removeAllMessagesFromDialog);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.audioProgressDidChanged);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.audioDidReset);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.audioPlayStateChanged);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.screenshotTook);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.blockedUsersDidLoaded);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.FileNewChunkAvailable);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.didCreatedNewDeleteTask);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.audioDidStarted);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.updateMessageMedia);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.replaceMessagesObjects);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.notificationsSettingsUpdated);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.didLoadedReplyMessages);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.didReceivedWebpages);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.didReceivedWebpagesInUpdates);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.messagesReadContent);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.botInfoDidLoaded);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.botKeyboardDidLoaded);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.chatSearchResultsAvailable);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.didUpdatedMessagesViews);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.chatInfoCantLoad);
        super.onFragmentCreate();
        if (this.currentEncryptedChat == null && !this.isBroadcast) {
            BotQuery.loadBotKeyboard(this.dialog_id);
        }
        this.loading = true;
        MessagesController instance;
        long j;
        int i2;
        int i3;
        int i4;
        int i5;
        if (this.startLoadFromMessageId != 0) {
            this.needSelectFromMessageId = true;
            this.waitingForLoad.add(Integer.valueOf(this.lastLoadIndex));
            int i6;
            if (migrated_to != 0) {
                this.mergeDialogId = (long) migrated_to;
                instance = MessagesController.getInstance();
                j = this.mergeDialogId;
                i2 = AndroidUtilities.isTablet() ? 30 : 20;
                i6 = this.startLoadFromMessageId;
                i3 = this.classGuid;
                i4 = this.lastLoadIndex;
                this.lastLoadIndex = i4 + 1;
                instance.loadMessages(j, i2, i6, true, 0, i3, 3, 0, 0, i4);
            } else {
                instance = MessagesController.getInstance();
                j = this.dialog_id;
                i2 = AndroidUtilities.isTablet() ? 30 : 20;
                i6 = this.startLoadFromMessageId;
                i3 = this.classGuid;
                i5 = this.channelMessagesImportant;
                i4 = this.lastLoadIndex;
                this.lastLoadIndex = i4 + 1;
                instance.loadMessages(j, i2, i6, true, 0, i3, 3, 0, i5, i4);
            }
        } else {
            this.waitingForLoad.add(Integer.valueOf(this.lastLoadIndex));
            instance = MessagesController.getInstance();
            j = this.dialog_id;
            i2 = AndroidUtilities.isTablet() ? 30 : 20;
            i3 = this.classGuid;
            i5 = this.channelMessagesImportant;
            i4 = this.lastLoadIndex;
            this.lastLoadIndex = i4 + 1;
            instance.loadMessages(j, i2, 0, true, 0, i3, 2, 0, i5, i4);
        }
        if (this.currentChat != null) {
            Semaphore semaphore4 = null;
            if (this.isBroadcast) {
                semaphore = new Semaphore(0);
            }
            MessagesController.getInstance().loadChatInfo(this.currentChat.id, semaphore4, ChatObject.isChannel(this.currentChat));
            if (this.isBroadcast && semaphore4 != null) {
                try {
                    semaphore4.acquire();
                } catch (Throwable e2222) {
                    FileLog.m611e("tmessages", e2222);
                }
            }
        }
        URLSpanBotCommand.enabled = false;
        if (userId != 0 && this.currentUser.bot) {
            BotQuery.loadBotInfo(userId, true, this.classGuid);
            URLSpanBotCommand.enabled = true;
        } else if (this.info instanceof TL_chatFull) {
            for (int a = 0; a < this.info.participants.participants.size(); a++) {
                User user = MessagesController.getInstance().getUser(Integer.valueOf(((ChatParticipant) this.info.participants.participants.get(a)).user_id));
                if (user != null && user.bot) {
                    BotQuery.loadBotInfo(user.id, true, this.classGuid);
                    URLSpanBotCommand.enabled = true;
                }
            }
        }
        if (this.currentUser != null) {
            this.userBlocked = MessagesController.getInstance().blockedUsers.contains(Integer.valueOf(this.currentUser.id));
        }
        if (AndroidUtilities.isTablet()) {
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.openedChatChanged, Long.valueOf(this.dialog_id), Boolean.valueOf(false));
        }
        this.typingDotsDrawable = new TypingDotsDrawable();
        this.typingDotsDrawable.setIsChat(this.currentChat != null);
        this.recordStatusDrawable = new RecordStatusDrawable();
        this.recordStatusDrawable.setIsChat(this.currentChat != null);
        this.sendingFileDrawable = new SendingFileExDrawable();
        this.sendingFileDrawable.setIsChat(this.currentChat != null);
        if (!(this.currentEncryptedChat == null || AndroidUtilities.getMyLayerVersion(this.currentEncryptedChat.layer) == 23)) {
            SecretChatHelper.getInstance().sendNotifyLayerMessage(this.currentEncryptedChat, null);
        }
        return true;
    }

    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        if (this.chatActivityEnterView != null) {
            this.chatActivityEnterView.onDestroy();
        }
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.messagesDidLoaded);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.emojiDidLoaded);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.updateInterfaces);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.didReceivedNewMessages);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.closeChats);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.messagesRead);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.messagesDeleted);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.messageReceivedByServer);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.messageReceivedByAck);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.messageSendError);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.chatInfoDidLoaded);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.encryptedChatUpdated);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.messagesReadEncrypted);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.removeAllMessagesFromDialog);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.contactsDidLoaded);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.audioProgressDidChanged);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.audioDidReset);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.screenshotTook);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.blockedUsersDidLoaded);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.FileNewChunkAvailable);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.didCreatedNewDeleteTask);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.audioDidStarted);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.updateMessageMedia);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.replaceMessagesObjects);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.notificationsSettingsUpdated);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.didLoadedReplyMessages);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.didReceivedWebpages);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.didReceivedWebpagesInUpdates);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.messagesReadContent);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.botInfoDidLoaded);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.botKeyboardDidLoaded);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.chatSearchResultsAvailable);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.audioPlayStateChanged);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.didUpdatedMessagesViews);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.chatInfoCantLoad);
        if (AndroidUtilities.isTablet()) {
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.openedChatChanged, Long.valueOf(this.dialog_id), Boolean.valueOf(true));
        }
        if (this.currentEncryptedChat != null) {
            MediaController.getInstance().stopMediaObserver();
        }
        if (this.currentUser != null) {
            MessagesController.getInstance().cancelLoadFullUser(this.currentUser.id);
        }
        AndroidUtilities.removeAdjustResize(getParentActivity(), this.classGuid);
        if (this.stickersAdapter != null) {
            this.stickersAdapter.onDestroy();
        }
        if (this.chatAttachView != null) {
            this.chatAttachView.onDestroy();
        }
        AndroidUtilities.unlockOrientation(getParentActivity());
        MessageObject messageObject = MediaController.getInstance().getPlayingMessageObject();
        if (!(messageObject == null || messageObject.isMusic())) {
            MediaController.getInstance().stopAudio();
        }
        if (ChatObject.isChannel(this.currentChat)) {
            MessagesController.getInstance().startShortPoll(this.currentChat.id, true);
        }
    }

    public View createView(Context context) {
        int a;
        View linearLayout;
        MessageObject messageObject;
        if (this.chatMessageCellsCache.isEmpty()) {
            for (a = 0; a < 8; a++) {
                this.chatMessageCellsCache.add(new ChatMessageCell(context));
            }
        }
        if (this.chatMediaCellsCache.isEmpty()) {
            for (a = 0; a < 4; a++) {
                this.chatMediaCellsCache.add(new ChatMediaCell(context));
            }
        }
        for (a = 1; a >= 0; a--) {
            this.selectedMessagesIds[a].clear();
            this.selectedMessagesCanCopyIds[a].clear();
        }
        this.cantDeleteMessagesCount = 0;
        this.lastPrintString = null;
        this.lastStatus = null;
        this.hasOwnBackground = true;
        this.chatAttachView = null;
        this.chatAttachViewSheet = null;
        ResourceLoader.loadRecources(context);
        this.actionBar.setBackButtonDrawable(new BackDrawable(false));
        this.actionBar.setActionBarMenuOnItemClick(new C15477());
        this.avatarContainer = new FrameLayoutFixed(context);
        this.avatarContainer.setBackgroundResource(C0553R.drawable.bar_selector);
        this.avatarContainer.setPadding(AndroidUtilities.dp(8.0f), 0, AndroidUtilities.dp(8.0f), 0);
        this.actionBar.addView(this.avatarContainer, 0, LayoutHelper.createFrame(-2, GroundOverlayOptions.NO_DIMENSION, 51, 56.0f, 0.0f, 40.0f, 0.0f));
        this.avatarContainer.setOnClickListener(new C08868());
        if (!(this.currentChat == null || ChatObject.isChannel(this.currentChat))) {
            int count = this.currentChat.participants_count;
            if (this.info != null) {
                count = this.info.participants.participants.size();
            }
            if (count == 0 || this.currentChat.deactivated || this.currentChat.left || (this.currentChat instanceof TL_chatForbidden) || (this.info != null && (this.info.participants instanceof TL_chatParticipantsForbidden))) {
                this.avatarContainer.setEnabled(false);
            }
        }
        this.avatarImageView = new BackupImageView(context);
        this.avatarImageView.setRoundRadius(AndroidUtilities.dp(21.0f));
        this.avatarContainer.addView(this.avatarImageView, LayoutHelper.createFrame(42, 42.0f, 51, 0.0f, 3.0f, 0.0f, 0.0f));
        if (this.currentEncryptedChat != null) {
            this.timeItem = new ImageView(context);
            this.timeItem.setPadding(AndroidUtilities.dp(10.0f), AndroidUtilities.dp(10.0f), AndroidUtilities.dp(5.0f), AndroidUtilities.dp(5.0f));
            this.timeItem.setScaleType(ScaleType.CENTER);
            ImageView imageView = this.timeItem;
            Drawable timerDrawable = new TimerDrawable(context);
            this.timerDrawable = timerDrawable;
            imageView.setImageDrawable(timerDrawable);
            this.avatarContainer.addView(this.timeItem, LayoutHelper.createFrame(34, 34.0f, 51, 16.0f, 18.0f, 0.0f, 0.0f));
            this.timeItem.setOnClickListener(new C08879());
        }
        this.nameTextView = new TextView(context);
        this.nameTextView.setTextColor(-1);
        this.nameTextView.setTextSize(1, 18.0f);
        this.nameTextView.setLines(1);
        this.nameTextView.setMaxLines(1);
        this.nameTextView.setSingleLine(true);
        this.nameTextView.setEllipsize(TruncateAt.END);
        this.nameTextView.setGravity(3);
        this.nameTextView.setCompoundDrawablePadding(AndroidUtilities.dp(4.0f));
        this.nameTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        this.avatarContainer.addView(this.nameTextView, LayoutHelper.createFrame(-2, -2.0f, 83, 54.0f, 0.0f, 0.0f, 22.0f));
        this.onlineTextView = new TextView(context);
        this.onlineTextView.setTextColor(-2627337);
        this.onlineTextView.setTextSize(1, 14.0f);
        this.onlineTextView.setLines(1);
        this.onlineTextView.setMaxLines(1);
        this.onlineTextView.setSingleLine(true);
        this.onlineTextView.setEllipsize(TruncateAt.END);
        this.onlineTextView.setGravity(3);
        if (!ChatObject.isChannel(this.currentChat) || this.currentChat.megagroup || (this.currentChat instanceof TL_channelForbidden)) {
            this.avatarContainer.addView(this.onlineTextView, LayoutHelper.createFrame(-2, -2.0f, 83, 54.0f, 0.0f, 0.0f, 4.0f));
        } else {
            this.radioButton = new RadioButton(context);
            this.radioButton.setChecked(this.channelMessagesImportant == 1, false);
            this.radioButton.setVisibility(8);
            this.avatarContainer.addView(this.radioButton, LayoutHelper.createFrame(24, 24.0f, 83, 50.0f, 0.0f, 0.0f, 0.0f));
            this.avatarContainer.addView(this.onlineTextView, LayoutHelper.createFrame(-2, -2.0f, 83, 54.0f, 0.0f, 0.0f, 4.0f));
        }
        ActionBarMenu menu = this.actionBar.createMenu();
        if (this.currentEncryptedChat == null && !this.isBroadcast) {
            this.searchItem = menu.addItem(0, (int) C0553R.drawable.ic_ab_search).setIsSearchField(true, false).setActionBarMenuItemSearchListener(new ActionBarMenuItemSearchListener() {

                class C08641 implements Runnable {
                    C08641() {
                    }

                    public void run() {
                        ChatActivity.this.searchItem.getSearchField().requestFocus();
                        AndroidUtilities.showKeyboard(ChatActivity.this.searchItem.getSearchField());
                    }
                }

                public void onSearchCollapse() {
                    ChatActivity.this.avatarContainer.setVisibility(0);
                    if (ChatActivity.this.chatActivityEnterView.hasText()) {
                        if (ChatActivity.this.headerItem != null) {
                            ChatActivity.this.headerItem.setVisibility(8);
                        }
                        if (ChatActivity.this.attachItem != null) {
                            ChatActivity.this.attachItem.setVisibility(0);
                        }
                    } else {
                        if (ChatActivity.this.headerItem != null) {
                            ChatActivity.this.headerItem.setVisibility(0);
                        }
                        if (ChatActivity.this.attachItem != null) {
                            ChatActivity.this.attachItem.setVisibility(8);
                        }
                    }
                    ChatActivity.this.searchItem.setVisibility(8);
                    ChatActivity.this.searchUpItem.clearAnimation();
                    ChatActivity.this.searchDownItem.clearAnimation();
                    ChatActivity.this.searchUpItem.setVisibility(8);
                    ChatActivity.this.searchDownItem.setVisibility(8);
                    ChatActivity.this.highlightMessageId = ConnectionsManager.DEFAULT_DATACENTER_ID;
                    ChatActivity.this.updateVisibleRows();
                    ChatActivity.this.scrollToLastMessage(false);
                }

                public void onSearchExpand() {
                    if (ChatActivity.this.openSearchKeyboard) {
                        AndroidUtilities.runOnUIThread(new C08641(), 300);
                    }
                }

                public void onSearchPressed(EditText editText) {
                    ChatActivity.this.updateSearchButtons(0);
                    MessagesSearchQuery.searchMessagesInChat(editText.getText().toString(), ChatActivity.this.dialog_id, ChatActivity.this.mergeDialogId, ChatActivity.this.classGuid, 0);
                }
            });
            this.searchItem.getSearchField().setHint(LocaleController.getString("Search", C0553R.string.Search));
            this.searchItem.setVisibility(8);
            this.searchUpItem = menu.addItem(41, (int) C0553R.drawable.search_up);
            this.searchUpItem.setVisibility(8);
            this.searchDownItem = menu.addItem(42, (int) C0553R.drawable.search_down);
            this.searchDownItem.setVisibility(8);
        }
        this.headerItem = menu.addItem(0, (int) C0553R.drawable.ic_ab_other);
        if (!(this.channelMessagesImportant == 0 || this.currentChat.megagroup)) {
            this.headerItem.addSubItem(50, LocaleController.getString("OpenChannelProfile", C0553R.string.OpenChannelProfile), 0);
        }
        if (this.searchItem != null) {
            this.headerItem.addSubItem(40, LocaleController.getString("Search", C0553R.string.Search), 0);
        }
        if (this.currentUser != null) {
            this.addContactItem = this.headerItem.addSubItem(17, "", 0);
        }
        if (this.currentEncryptedChat != null) {
            this.timeItem2 = this.headerItem.addSubItem(13, LocaleController.getString("SetTimer", C0553R.string.SetTimer), 0);
        }
        if (this.channelMessagesImportant == 0) {
            this.headerItem.addSubItem(15, LocaleController.getString("ClearHistory", C0553R.string.ClearHistory), 0);
            if (this.currentChat == null || this.isBroadcast) {
                this.headerItem.addSubItem(16, LocaleController.getString("DeleteChatUser", C0553R.string.DeleteChatUser), 0);
            } else {
                this.headerItem.addSubItem(16, LocaleController.getString("DeleteAndExit", C0553R.string.DeleteAndExit), 0);
            }
        }
        this.muteItem = this.headerItem.addSubItem(18, null, 0);
        if (this.currentUser != null && this.currentEncryptedChat == null && this.currentUser.bot) {
            this.headerItem.addSubItem(31, LocaleController.getString("BotSettings", C0553R.string.BotSettings), 0);
            this.headerItem.addSubItem(30, LocaleController.getString("BotHelp", C0553R.string.BotHelp), 0);
            updateBotButtons();
        }
        updateTitle();
        updateSubtitle();
        updateTitleIcons();
        this.attachItem = menu.addItem(14, (int) C0553R.drawable.ic_ab_other).setOverrideMenuClick(true).setAllowCloseAnimation(false);
        this.attachItem.setVisibility(8);
        this.menuItem = menu.addItem(14, (int) C0553R.drawable.ic_ab_attach).setAllowCloseAnimation(false);
        this.menuItem.setBackgroundDrawable(null);
        this.actionModeViews.clear();
        ActionBarMenu actionMode = this.actionBar.createActionMode();
        this.selectedMessagesCountTextView = new NumberTextView(actionMode.getContext());
        this.selectedMessagesCountTextView.setTextSize(18);
        this.selectedMessagesCountTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        this.selectedMessagesCountTextView.setTextColor(-9211021);
        actionMode.addView(this.selectedMessagesCountTextView, LayoutHelper.createLinear(0, -1, 1.0f, 65, 0, 0, 0));
        this.selectedMessagesCountTextView.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        if (this.currentEncryptedChat == null) {
            if (!this.isBroadcast) {
                this.actionModeViews.add(actionMode.addItem(19, C0553R.drawable.ic_ab_reply, C0553R.drawable.bar_selector_mode, null, AndroidUtilities.dp(54.0f)));
            }
            this.actionModeViews.add(actionMode.addItem(10, C0553R.drawable.ic_ab_fwd_copy, C0553R.drawable.bar_selector_mode, null, AndroidUtilities.dp(54.0f)));
            this.actionModeViews.add(actionMode.addItem(11, C0553R.drawable.ic_ab_fwd_forward, C0553R.drawable.bar_selector_mode, null, AndroidUtilities.dp(54.0f)));
            this.actionModeViews.add(actionMode.addItem(12, C0553R.drawable.ic_ab_fwd_delete, C0553R.drawable.bar_selector_mode, null, AndroidUtilities.dp(54.0f)));
        } else {
            this.actionModeViews.add(actionMode.addItem(10, C0553R.drawable.ic_ab_fwd_copy, C0553R.drawable.bar_selector_mode, null, AndroidUtilities.dp(54.0f)));
            this.actionModeViews.add(actionMode.addItem(12, C0553R.drawable.ic_ab_fwd_delete, C0553R.drawable.bar_selector_mode, null, AndroidUtilities.dp(54.0f)));
        }
        actionMode.getItem(10).setVisibility(this.selectedMessagesCanCopyIds[0].size() + this.selectedMessagesCanCopyIds[1].size() != 0 ? 0 : 8);
        actionMode.getItem(12).setVisibility(this.cantDeleteMessagesCount == 0 ? 0 : 8);
        checkActionBarMenu();
        this.fragmentView = new SizeNotifierFrameLayout(context) {
            int inputFieldHeight = 0;

            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                int widthSize = MeasureSpec.getSize(widthMeasureSpec);
                int heightSize = MeasureSpec.getSize(heightMeasureSpec);
                setMeasuredDimension(widthSize, heightSize);
                heightSize -= getPaddingTop();
                if (getKeyboardHeight() <= AndroidUtilities.dp(20.0f)) {
                    heightSize -= ChatActivity.this.chatActivityEnterView.getEmojiPadding();
                }
                int childCount = getChildCount();
                measureChildWithMargins(ChatActivity.this.chatActivityEnterView, widthMeasureSpec, 0, heightMeasureSpec, 0);
                this.inputFieldHeight = ChatActivity.this.chatActivityEnterView.getMeasuredHeight();
                for (int i = 0; i < childCount; i++) {
                    View child = getChildAt(i);
                    if (!(child.getVisibility() == 8 || child == ChatActivity.this.chatActivityEnterView)) {
                        try {
                            if (child == ChatActivity.this.chatListView || child == ChatActivity.this.progressView) {
                                child.measure(MeasureSpec.makeMeasureSpec(widthSize, 1073741824), MeasureSpec.makeMeasureSpec(Math.max(AndroidUtilities.dp(10.0f), (heightSize - this.inputFieldHeight) + AndroidUtilities.dp(2.0f)), 1073741824));
                            } else if (child == ChatActivity.this.emptyViewContainer) {
                                child.measure(MeasureSpec.makeMeasureSpec(widthSize, 1073741824), MeasureSpec.makeMeasureSpec(heightSize, 1073741824));
                            } else if (ChatActivity.this.chatActivityEnterView.isPopupView(child)) {
                                child.measure(MeasureSpec.makeMeasureSpec(widthSize, 1073741824), MeasureSpec.makeMeasureSpec(child.getLayoutParams().height, 1073741824));
                            } else {
                                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                            }
                        } catch (Throwable e) {
                            FileLog.m611e("tmessages", e);
                        }
                    }
                }
            }

            protected void onLayout(boolean changed, int l, int t, int r, int b) {
                int count = getChildCount();
                int paddingBottom = getKeyboardHeight() <= AndroidUtilities.dp(20.0f) ? ChatActivity.this.chatActivityEnterView.getEmojiPadding() : 0;
                setBottomClip(paddingBottom);
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
                                childTop = lp.topMargin + getPaddingTop();
                                break;
                            case 80:
                                childTop = (((b - paddingBottom) - t) - height) - lp.bottomMargin;
                                break;
                            default:
                                childTop = lp.topMargin;
                                break;
                        }
                        if (child == ChatActivity.this.mentionListView) {
                            childTop -= ChatActivity.this.chatActivityEnterView.getMeasuredHeight() - AndroidUtilities.dp(2.0f);
                        } else if (child == ChatActivity.this.pagedownButton) {
                            childTop -= ChatActivity.this.chatActivityEnterView.getMeasuredHeight();
                        } else if (child == ChatActivity.this.emptyViewContainer) {
                            childTop -= this.inputFieldHeight / 2;
                        } else if (ChatActivity.this.chatActivityEnterView.isPopupView(child)) {
                            childTop = ChatActivity.this.chatActivityEnterView.getBottom();
                        }
                        child.layout(childLeft, childTop, childLeft + width, childTop + height);
                    }
                }
                notifyHeightChanged();
            }
        };
        SizeNotifierFrameLayout contentView = this.fragmentView;
        contentView.setBackgroundImage(ApplicationLoader.getCachedWallpaper());
        this.emptyViewContainer = new FrameLayout(context);
        this.emptyViewContainer.setVisibility(4);
        contentView.addView(this.emptyViewContainer, LayoutHelper.createFrame(-1, -2, 17));
        this.emptyViewContainer.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        if (this.currentEncryptedChat == null) {
            TextView emptyView = new TextView(context);
            if (this.currentUser == null || this.currentUser.id == 777000 || this.currentUser.id == 429000 || !(this.currentUser.id / 1000 == 333 || this.currentUser.id % 1000 == 0)) {
                emptyView.setText(LocaleController.getString("NoMessages", C0553R.string.NoMessages));
            } else {
                emptyView.setText(LocaleController.getString("GotAQuestion", C0553R.string.GotAQuestion));
            }
            emptyView.setTextSize(1, 16.0f);
            emptyView.setGravity(17);
            emptyView.setTextColor(-1);
            emptyView.setBackgroundResource(ApplicationLoader.isCustomTheme() ? C0553R.drawable.system_black : C0553R.drawable.system_blue);
            emptyView.setPadding(AndroidUtilities.dp(7.0f), AndroidUtilities.dp(1.0f), AndroidUtilities.dp(7.0f), AndroidUtilities.dp(1.0f));
            this.emptyViewContainer.addView(emptyView, new LayoutParams(-2, -2, 17));
        } else {
            linearLayout = new LinearLayout(context);
            linearLayout.setBackgroundResource(ApplicationLoader.isCustomTheme() ? C0553R.drawable.system_black : C0553R.drawable.system_blue);
            linearLayout.setPadding(AndroidUtilities.dp(16.0f), AndroidUtilities.dp(12.0f), AndroidUtilities.dp(16.0f), AndroidUtilities.dp(12.0f));
            linearLayout.setOrientation(1);
            this.emptyViewContainer.addView(linearLayout, new LayoutParams(-2, -2, 17));
            this.secretViewStatusTextView = new TextView(context);
            this.secretViewStatusTextView.setTextSize(1, 15.0f);
            this.secretViewStatusTextView.setTextColor(-1);
            this.secretViewStatusTextView.setGravity(1);
            this.secretViewStatusTextView.setMaxWidth(AndroidUtilities.dp(BitmapDescriptorFactory.HUE_AZURE));
            if (this.currentEncryptedChat.admin_id == UserConfig.getClientUserId()) {
                this.secretViewStatusTextView.setText(LocaleController.formatString("EncryptedPlaceholderTitleOutgoing", C0553R.string.EncryptedPlaceholderTitleOutgoing, UserObject.getFirstName(this.currentUser)));
            } else {
                this.secretViewStatusTextView.setText(LocaleController.formatString("EncryptedPlaceholderTitleIncoming", C0553R.string.EncryptedPlaceholderTitleIncoming, UserObject.getFirstName(this.currentUser)));
            }
            linearLayout.addView(this.secretViewStatusTextView, LayoutHelper.createLinear(-2, -2, 49));
            linearLayout = new TextView(context);
            linearLayout.setText(LocaleController.getString("EncryptedDescriptionTitle", C0553R.string.EncryptedDescriptionTitle));
            linearLayout.setTextSize(1, 15.0f);
            linearLayout.setTextColor(-1);
            linearLayout.setGravity(1);
            linearLayout.setMaxWidth(AndroidUtilities.dp(260.0f));
            linearLayout.addView(linearLayout, LayoutHelper.createLinear(-2, -2, (LocaleController.isRTL ? 5 : 3) | 48, 0, 8, 0, 0));
            for (a = 0; a < 4; a++) {
                linearLayout = new LinearLayout(context);
                linearLayout.setOrientation(0);
                linearLayout.addView(linearLayout, LayoutHelper.createLinear(-2, -2, LocaleController.isRTL ? 5 : 3, 0, 8, 0, 0));
                linearLayout = new ImageView(context);
                linearLayout.setImageResource(C0553R.drawable.ic_lock_white);
                linearLayout = new TextView(context);
                linearLayout.setTextSize(1, 15.0f);
                linearLayout.setTextColor(-1);
                linearLayout.setGravity((LocaleController.isRTL ? 5 : 3) | 16);
                linearLayout.setMaxWidth(AndroidUtilities.dp(260.0f));
                switch (a) {
                    case 0:
                        linearLayout.setText(LocaleController.getString("EncryptedDescription1", C0553R.string.EncryptedDescription1));
                        break;
                    case 1:
                        linearLayout.setText(LocaleController.getString("EncryptedDescription2", C0553R.string.EncryptedDescription2));
                        break;
                    case 2:
                        linearLayout.setText(LocaleController.getString("EncryptedDescription3", C0553R.string.EncryptedDescription3));
                        break;
                    case 3:
                        linearLayout.setText(LocaleController.getString("EncryptedDescription4", C0553R.string.EncryptedDescription4));
                        break;
                }
                if (LocaleController.isRTL) {
                    linearLayout.addView(linearLayout, LayoutHelper.createLinear(-2, -2));
                    linearLayout.addView(linearLayout, LayoutHelper.createLinear(-2, -2, 8.0f, 3.0f, 0.0f, 0.0f));
                } else {
                    linearLayout.addView(linearLayout, LayoutHelper.createLinear(-2, -2, 0.0f, 4.0f, 8.0f, 0.0f));
                    linearLayout.addView(linearLayout, LayoutHelper.createLinear(-2, -2));
                }
            }
        }
        if (this.chatActivityEnterView != null) {
            this.chatActivityEnterView.onDestroy();
        }
        this.chatListView = new RecyclerListView(context) {
            protected void onLayout(boolean changed, int l, int t, int r, int b) {
                super.onLayout(changed, l, t, r, b);
                if (ChatActivity.this.chatAdapter.isBot) {
                    int childCount = getChildCount();
                    for (int a = 0; a < childCount; a++) {
                        View child = getChildAt(a);
                        if (child instanceof BotHelpCell) {
                            int top = ((b - t) / 2) - (child.getMeasuredHeight() / 2);
                            if (child.getTop() > top) {
                                child.layout(0, top, r - l, child.getMeasuredHeight() + top);
                                return;
                            }
                            return;
                        }
                    }
                }
            }
        };
        this.chatListView.setVerticalScrollBarEnabled(true);
        RecyclerListView recyclerListView = this.chatListView;
        Adapter chatActivityAdapter = new ChatActivityAdapter(context);
        this.chatAdapter = chatActivityAdapter;
        recyclerListView.setAdapter(chatActivityAdapter);
        this.chatListView.setClipToPadding(false);
        this.chatListView.setPadding(0, AndroidUtilities.dp(4.0f), 0, AndroidUtilities.dp(3.0f));
        this.chatListView.setItemAnimator(null);
        this.chatListView.setLayoutAnimation(null);
        this.chatLayoutManager = new LinearLayoutManager(context) {
            public boolean supportsPredictiveItemAnimations() {
                return false;
            }
        };
        this.chatLayoutManager.setOrientation(1);
        this.chatLayoutManager.setStackFromEnd(true);
        this.chatListView.setLayoutManager(this.chatLayoutManager);
        contentView.addView(this.chatListView, LayoutHelper.createFrame(-1, GroundOverlayOptions.NO_DIMENSION));
        this.chatListView.setOnItemLongClickListener(this.onItemLongClickListener);
        this.chatListView.setOnItemClickListener(this.onItemClickListener);
        this.chatListView.setOnScrollListener(new OnScrollListener() {
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == 1 && ChatActivity.this.highlightMessageId != ConnectionsManager.DEFAULT_DATACENTER_ID) {
                    ChatActivity.this.highlightMessageId = ConnectionsManager.DEFAULT_DATACENTER_ID;
                    ChatActivity.this.updateVisibleRows();
                }
            }

            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                ChatActivity.this.checkScrollForLoad();
                int firstVisibleItem = ChatActivity.this.chatLayoutManager.findFirstVisibleItemPosition();
                int visibleItemCount = firstVisibleItem == -1 ? 0 : Math.abs(ChatActivity.this.chatLayoutManager.findLastVisibleItemPosition() - firstVisibleItem) + 1;
                if (visibleItemCount > 0 && firstVisibleItem + visibleItemCount == ChatActivity.this.chatAdapter.getItemCount() && ChatActivity.this.forwardEndReached[0]) {
                    ChatActivity.this.showPagedownButton(false, true);
                }
                ChatActivity.this.updateMessagesVisisblePart();
            }
        });
        this.chatListView.setOnTouchListener(new OnTouchListener() {

            class C08651 implements Runnable {
                C08651() {
                }

                public void run() {
                    ChatActivity.this.chatListView.setOnItemClickListener(ChatActivity.this.onItemClickListener);
                }
            }

            class C08662 implements Runnable {
                C08662() {
                }

                public void run() {
                    ChatActivity.this.chatListView.setOnItemLongClickListener(ChatActivity.this.onItemLongClickListener);
                    ChatActivity.this.chatListView.setLongClickable(true);
                }
            }

            public boolean onTouch(View v, MotionEvent event) {
                if (ChatActivity.this.openSecretPhotoRunnable != null || SecretPhotoViewer.getInstance().isVisible()) {
                    if (event.getAction() == 1 || event.getAction() == 3 || event.getAction() == 6) {
                        AndroidUtilities.runOnUIThread(new C08651(), 150);
                        if (ChatActivity.this.openSecretPhotoRunnable != null) {
                            AndroidUtilities.cancelRunOnUIThread(ChatActivity.this.openSecretPhotoRunnable);
                            ChatActivity.this.openSecretPhotoRunnable = null;
                            try {
                                Toast.makeText(v.getContext(), LocaleController.getString("PhotoTip", C0553R.string.PhotoTip), 0).show();
                            } catch (Throwable e) {
                                FileLog.m611e("tmessages", e);
                            }
                        } else if (SecretPhotoViewer.getInstance().isVisible()) {
                            AndroidUtilities.runOnUIThread(new C08662());
                            SecretPhotoViewer.getInstance().closePhoto();
                        }
                    } else if (event.getAction() != 0) {
                        if (SecretPhotoViewer.getInstance().isVisible()) {
                            return true;
                        }
                        if (ChatActivity.this.openSecretPhotoRunnable != null) {
                            if (event.getAction() != 2) {
                                AndroidUtilities.cancelRunOnUIThread(ChatActivity.this.openSecretPhotoRunnable);
                                ChatActivity.this.openSecretPhotoRunnable = null;
                            } else if (Math.hypot((double) (ChatActivity.this.startX - event.getX()), (double) (ChatActivity.this.startY - event.getY())) > ((double) AndroidUtilities.dp(5.0f))) {
                                AndroidUtilities.cancelRunOnUIThread(ChatActivity.this.openSecretPhotoRunnable);
                                ChatActivity.this.openSecretPhotoRunnable = null;
                            }
                            ChatActivity.this.chatListView.setOnItemClickListener(ChatActivity.this.onItemClickListener);
                            ChatActivity.this.chatListView.setOnItemLongClickListener(ChatActivity.this.onItemLongClickListener);
                            ChatActivity.this.chatListView.setLongClickable(true);
                        }
                    }
                }
                return false;
            }
        });
        this.chatListView.setOnInterceptTouchListener(new OnInterceptTouchListener() {
            public boolean onInterceptTouchEvent(MotionEvent event) {
                if (ChatActivity.this.actionBar.isActionModeShowed() || event.getAction() != 0) {
                    return false;
                }
                int x = (int) event.getX();
                int y = (int) event.getY();
                int count = ChatActivity.this.chatListView.getChildCount();
                int a = 0;
                while (a < count) {
                    View view = ChatActivity.this.chatListView.getChildAt(a);
                    int top = view.getTop();
                    int bottom = view.getBottom();
                    if (top > y || bottom < y) {
                        a++;
                    } else if (!(view instanceof ChatMediaCell)) {
                        return false;
                    } else {
                        final ChatMediaCell cell = (ChatMediaCell) view;
                        final MessageObject messageObject = cell.getMessageObject();
                        if (messageObject == null || messageObject.isSending() || !messageObject.isSecretPhoto() || !cell.getPhotoImage().isInsideImage((float) x, (float) (y - top)) || !FileLoader.getPathToMessage(messageObject.messageOwner).exists()) {
                            return false;
                        }
                        ChatActivity.this.startX = (float) x;
                        ChatActivity.this.startY = (float) y;
                        ChatActivity.this.chatListView.setOnItemClickListener(null);
                        ChatActivity.this.openSecretPhotoRunnable = new Runnable() {
                            public void run() {
                                if (ChatActivity.this.openSecretPhotoRunnable != null) {
                                    ChatActivity.this.chatListView.requestDisallowInterceptTouchEvent(true);
                                    ChatActivity.this.chatListView.setOnItemLongClickListener(null);
                                    ChatActivity.this.chatListView.setLongClickable(false);
                                    ChatActivity.this.openSecretPhotoRunnable = null;
                                    if (ChatActivity.this.sendSecretMessageRead(messageObject)) {
                                        cell.invalidate();
                                    }
                                    SecretPhotoViewer.getInstance().setParentActivity(ChatActivity.this.getParentActivity());
                                    SecretPhotoViewer.getInstance().openPhoto(messageObject);
                                }
                            }
                        };
                        AndroidUtilities.runOnUIThread(ChatActivity.this.openSecretPhotoRunnable, 100);
                        return true;
                    }
                }
                return false;
            }
        });
        this.progressView = new FrameLayout(context);
        this.progressView.setVisibility(4);
        contentView.addView(this.progressView, LayoutHelper.createFrame(-1, -1, 51));
        linearLayout = new View(context);
        linearLayout.setBackgroundResource(ApplicationLoader.isCustomTheme() ? C0553R.drawable.system_loader2 : C0553R.drawable.system_loader1);
        this.progressView.addView(linearLayout, LayoutHelper.createFrame(36, 36, 17));
        linearLayout = new ProgressBar(context);
        try {
            linearLayout.setIndeterminateDrawable(context.getResources().getDrawable(C0553R.drawable.loading_animation));
        } catch (Exception e) {
        }
        linearLayout.setIndeterminate(true);
        AndroidUtilities.setProgressBarAnimationDuration(linearLayout, 1500);
        this.progressView.addView(linearLayout, LayoutHelper.createFrame(32, 32, 17));
        this.reportSpamView = new LinearLayout(context);
        this.reportSpamView.setVisibility(8);
        this.reportSpamView.setBackgroundResource(C0553R.drawable.blockpanel);
        contentView.addView(this.reportSpamView, LayoutHelper.createFrame(-1, 50, 51));
        this.addToContactsButton = new TextView(context);
        this.addToContactsButton.setTextColor(-11894091);
        this.addToContactsButton.setTextSize(1, 14.0f);
        this.addToContactsButton.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        this.addToContactsButton.setSingleLine(true);
        this.addToContactsButton.setMaxLines(1);
        this.addToContactsButton.setPadding(AndroidUtilities.dp(4.0f), 0, AndroidUtilities.dp(4.0f), 0);
        this.addToContactsButton.setGravity(17);
        this.addToContactsButton.setText(LocaleController.getString("AddContactChat", C0553R.string.AddContactChat));
        this.reportSpamView.addView(this.addToContactsButton, LayoutHelper.createLinear(-1, -1, 0.5f, 51, 0, 0, 0, AndroidUtilities.dp(1.0f)));
        this.addToContactsButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putInt("user_id", ChatActivity.this.currentUser.id);
                args.putBoolean("addContact", true);
                ChatActivity.this.presentFragment(new ContactAddActivity(args));
            }
        });
        this.reportSpamContainer = new FrameLayout(context);
        this.reportSpamView.addView(this.reportSpamContainer, LayoutHelper.createLinear(-1, -1, 0.5f, 51));
        this.reportSpamButton = new TextView(context);
        this.reportSpamButton.setTextColor(-3188393);
        this.reportSpamButton.setTextSize(1, 14.0f);
        this.reportSpamButton.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        this.reportSpamButton.setSingleLine(true);
        this.reportSpamButton.setMaxLines(1);
        this.reportSpamButton.setText(LocaleController.getString("ReportSpam", C0553R.string.ReportSpam));
        this.reportSpamButton.setGravity(17);
        this.reportSpamButton.setPadding(AndroidUtilities.dp(4.0f), 0, AndroidUtilities.dp(50.0f), 0);
        this.reportSpamContainer.addView(this.reportSpamButton, LayoutHelper.createFrame(-1, -1, 51));
        this.reportSpamButton.setOnClickListener(new OnClickListener() {

            class C08691 implements DialogInterface.OnClickListener {

                class C15401 implements RequestDelegate {

                    class C08681 implements Runnable {
                        C08681() {
                        }

                        public void run() {
                            ApplicationLoader.applicationContext.getSharedPreferences("Notifications", 0).edit().putBoolean("spam_" + ChatActivity.this.dialog_id, true).commit();
                            ChatActivity.this.updateSpamView();
                        }
                    }

                    C15401() {
                    }

                    public void run(TLObject response, TL_error error) {
                        if (error == null) {
                            AndroidUtilities.runOnUIThread(new C08681());
                        }
                    }
                }

                C08691() {
                }

                public void onClick(DialogInterface dialogInterface, int i) {
                    if (ChatActivity.this.reportSpamUser != null) {
                        TL_messages_reportSpam req = new TL_messages_reportSpam();
                        req.peer = new TL_inputPeerUser();
                        req.peer.user_id = ChatActivity.this.reportSpamUser.id;
                        req.peer.access_hash = ChatActivity.this.reportSpamUser.access_hash;
                        MessagesController.getInstance().blockUser(ChatActivity.this.reportSpamUser.id);
                        ConnectionsManager.getInstance().sendRequest(req, new C15401(), 2);
                    }
                }
            }

            public void onClick(View v) {
                if (ChatActivity.this.reportSpamUser != null && ChatActivity.this.getParentActivity() != null) {
                    Builder builder = new Builder(ChatActivity.this.getParentActivity());
                    if (ChatActivity.this.currentChat != null) {
                        builder.setMessage(LocaleController.getString("ReportSpamAlertGroup", C0553R.string.ReportSpamAlertGroup));
                    } else {
                        builder.setMessage(LocaleController.getString("ReportSpamAlert", C0553R.string.ReportSpamAlert));
                    }
                    builder.setTitle(LocaleController.getString("AppName", C0553R.string.AppName));
                    builder.setPositiveButton(LocaleController.getString("OK", C0553R.string.OK), new C08691());
                    builder.setNegativeButton(LocaleController.getString("Cancel", C0553R.string.Cancel), null);
                    ChatActivity.this.showDialog(builder.create());
                }
            }
        });
        ImageView closeReportSpam = new ImageView(context);
        closeReportSpam.setImageResource(C0553R.drawable.delete_reply);
        closeReportSpam.setScaleType(ScaleType.CENTER);
        this.reportSpamContainer.addView(closeReportSpam, LayoutHelper.createFrame(48, 48, 53));
        closeReportSpam.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ApplicationLoader.applicationContext.getSharedPreferences("Notifications", 0).edit().putBoolean("spam_" + ChatActivity.this.dialog_id, true).commit();
                ChatActivity.this.updateSpamView();
            }
        });
        if (this.currentEncryptedChat == null && !this.isBroadcast) {
            this.mentionListView = new ListView(context);
            this.mentionListView.setBackgroundResource(C0553R.drawable.compose_panel);
            this.mentionListView.setVisibility(8);
            this.mentionListView.setPadding(0, AndroidUtilities.dp(2.0f), 0, 0);
            this.mentionListView.setClipToPadding(true);
            this.mentionListView.setDividerHeight(0);
            this.mentionListView.setDivider(null);
            if (VERSION.SDK_INT > 8) {
                this.mentionListView.setOverScrollMode(2);
            }
            contentView.addView(this.mentionListView, LayoutHelper.createFrame(-1, 110, 83));
            ListView listView = this.mentionListView;
            ListAdapter mentionsAdapter = new MentionsAdapter(context, false, new MentionsAdapterDelegate() {

                class C15411 extends AnimatorListenerAdapterProxy {
                    C15411() {
                    }

                    public void onAnimationEnd(Object animation) {
                        if (ChatActivity.this.mentionListAnimation != null && ChatActivity.this.mentionListAnimation.equals(animation)) {
                            ChatActivity.this.mentionListView.clearAnimation();
                            ChatActivity.this.mentionListAnimation = null;
                        }
                    }
                }

                class C15422 extends AnimatorListenerAdapterProxy {
                    C15422() {
                    }

                    public void onAnimationEnd(Object animation) {
                        if (ChatActivity.this.mentionListAnimation != null && ChatActivity.this.mentionListAnimation.equals(animation)) {
                            ChatActivity.this.mentionListView.clearAnimation();
                            ChatActivity.this.mentionListView.setVisibility(8);
                            ChatActivity.this.mentionListAnimation = null;
                        }
                    }
                }

                public void needChangePanelVisibility(boolean show) {
                    if (show) {
                        int i;
                        LayoutParams layoutParams3 = (LayoutParams) ChatActivity.this.mentionListView.getLayoutParams();
                        int min = Math.min(3, ChatActivity.this.mentionsAdapter.getCount()) * 36;
                        if (ChatActivity.this.mentionsAdapter.getCount() > 3) {
                            i = 18;
                        } else {
                            i = 0;
                        }
                        int height = min + i;
                        layoutParams3.height = AndroidUtilities.dp((float) (height + 2));
                        layoutParams3.topMargin = -AndroidUtilities.dp((float) height);
                        ChatActivity.this.mentionListView.setLayoutParams(layoutParams3);
                        if (ChatActivity.this.mentionListAnimation != null) {
                            ChatActivity.this.mentionListAnimation.cancel();
                            ChatActivity.this.mentionListAnimation = null;
                        }
                        if (ChatActivity.this.mentionListView.getVisibility() == 0) {
                            ViewProxy.setAlpha(ChatActivity.this.mentionListView, 1.0f);
                            return;
                        } else if (ChatActivity.this.allowStickersPanel) {
                            ChatActivity.this.mentionListView.setVisibility(0);
                            ChatActivity.this.mentionListAnimation = new AnimatorSetProxy();
                            ChatActivity.this.mentionListAnimation.playTogether(ObjectAnimatorProxy.ofFloat(ChatActivity.this.mentionListView, "alpha", 0.0f, 1.0f));
                            ChatActivity.this.mentionListAnimation.addListener(new C15411());
                            ChatActivity.this.mentionListAnimation.setDuration(200);
                            ChatActivity.this.mentionListAnimation.start();
                            return;
                        } else {
                            ViewProxy.setAlpha(ChatActivity.this.mentionListView, 1.0f);
                            ChatActivity.this.mentionListView.clearAnimation();
                            ChatActivity.this.mentionListView.setVisibility(4);
                            return;
                        }
                    }
                    if (ChatActivity.this.mentionListAnimation != null) {
                        ChatActivity.this.mentionListAnimation.cancel();
                        ChatActivity.this.mentionListAnimation = null;
                    }
                    if (ChatActivity.this.mentionListView.getVisibility() == 8) {
                        return;
                    }
                    if (ChatActivity.this.allowStickersPanel) {
                        ChatActivity.this.mentionListAnimation = new AnimatorSetProxy();
                        AnimatorSetProxy access$5900 = ChatActivity.this.mentionListAnimation;
                        Object[] objArr = new Object[1];
                        objArr[0] = ObjectAnimatorProxy.ofFloat(ChatActivity.this.mentionListView, "alpha", 0.0f);
                        access$5900.playTogether(objArr);
                        ChatActivity.this.mentionListAnimation.addListener(new C15422());
                        ChatActivity.this.mentionListAnimation.setDuration(200);
                        ChatActivity.this.mentionListAnimation.start();
                        return;
                    }
                    ChatActivity.this.mentionListView.clearAnimation();
                    ChatActivity.this.mentionListView.setVisibility(8);
                }
            });
            this.mentionsAdapter = mentionsAdapter;
            listView.setAdapter(mentionsAdapter);
            this.mentionsAdapter.setBotInfo(this.botInfo);
            this.mentionsAdapter.setChatInfo(this.info);
            this.mentionsAdapter.setNeedUsernames(this.currentChat != null);
            this.mentionsAdapter.setBotsCount(this.currentChat != null ? this.botsCount : 1);
            this.mentionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    User object = ChatActivity.this.mentionsAdapter.getItem(position);
                    int start = ChatActivity.this.mentionsAdapter.getResultStartPosition();
                    int len = ChatActivity.this.mentionsAdapter.getResultLength();
                    if (object instanceof User) {
                        User user = object;
                        if (user != null) {
                            ChatActivity.this.chatActivityEnterView.replaceWithText(start, len, "@" + user.username + " ");
                        }
                    } else if (!(object instanceof String)) {
                    } else {
                        if (ChatActivity.this.mentionsAdapter.isBotCommands()) {
                            SendMessagesHelper instance = SendMessagesHelper.getInstance();
                            String str = (String) object;
                            long access$1200 = ChatActivity.this.dialog_id;
                            boolean z = ChatActivity.this.chatActivityEnterView == null || ChatActivity.this.chatActivityEnterView.asAdmin();
                            instance.sendMessage(str, access$1200, null, null, false, z);
                            ChatActivity.this.chatActivityEnterView.setFieldText("");
                            return;
                        }
                        ChatActivity.this.chatActivityEnterView.replaceWithText(start, len, object + " ");
                    }
                }
            });
            this.mentionListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

                class C08701 implements DialogInterface.OnClickListener {
                    C08701() {
                    }

                    public void onClick(DialogInterface dialogInterface, int i) {
                        ChatActivity.this.mentionsAdapter.clearRecentHashtags();
                    }
                }

                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                    if (!ChatActivity.this.mentionsAdapter.isLongClickEnabled() || !(ChatActivity.this.mentionsAdapter.getItem(position) instanceof String)) {
                        return false;
                    }
                    Builder builder = new Builder(ChatActivity.this.getParentActivity());
                    builder.setTitle(LocaleController.getString("AppName", C0553R.string.AppName));
                    builder.setMessage(LocaleController.getString("ClearSearch", C0553R.string.ClearSearch));
                    builder.setPositiveButton(LocaleController.getString("ClearButton", C0553R.string.ClearButton).toUpperCase(), new C08701());
                    builder.setNegativeButton(LocaleController.getString("Cancel", C0553R.string.Cancel), null);
                    ChatActivity.this.showDialog(builder.create());
                    return true;
                }
            });
        }
        this.pagedownButton = new ImageView(context);
        this.pagedownButton.setVisibility(4);
        this.pagedownButton.setImageResource(C0553R.drawable.pagedown);
        contentView.addView(this.pagedownButton, LayoutHelper.createFrame(-2, -2.0f, 85, 0.0f, 0.0f, 6.0f, 4.0f));
        this.pagedownButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (ChatActivity.this.returnToMessageId > 0) {
                    ChatActivity.this.scrollToMessageId(ChatActivity.this.returnToMessageId, 0, true, 0);
                } else {
                    ChatActivity.this.scrollToLastMessage(true);
                }
            }
        });
        this.chatActivityEnterView = new ChatActivityEnterView(getParentActivity(), contentView, this, true);
        this.chatActivityEnterView.setDialogId(this.dialog_id);
        this.chatActivityEnterView.addToAttachLayout(this.menuItem);
        this.chatActivityEnterView.setId(1000);
        this.chatActivityEnterView.setBotsCount(this.botsCount, this.hasBotsCommands);
        contentView.addView(this.chatActivityEnterView, contentView.getChildCount() - 1, LayoutHelper.createFrame(-1, -2, 83));
        this.chatActivityEnterView.setDelegate(new ChatActivityEnterViewDelegate() {
            public void onMessageSend(String message) {
                ChatActivity.this.moveScrollToLastMessage();
                ChatActivity.this.showReplyPanel(false, null, null, null, false, true);
                if (ChatActivity.this.mentionsAdapter != null) {
                    ChatActivity.this.mentionsAdapter.addHashtagsFromMessage(message);
                }
            }

            public void onTextChanged(final CharSequence text, boolean bigChange) {
                if (ChatActivity.this.stickersAdapter != null) {
                    ChatActivity.this.stickersAdapter.loadStikersForEmoji(text);
                }
                if (ChatActivity.this.mentionsAdapter != null) {
                    ChatActivity.this.mentionsAdapter.searchUsernameOrHashtag(text.toString(), ChatActivity.this.chatActivityEnterView.getCursorPosition(), ChatActivity.this.messages);
                }
                if (ChatActivity.this.waitingForCharaterEnterRunnable != null) {
                    AndroidUtilities.cancelRunOnUIThread(ChatActivity.this.waitingForCharaterEnterRunnable);
                    ChatActivity.this.waitingForCharaterEnterRunnable = null;
                }
                if (!ChatActivity.this.chatActivityEnterView.isMessageWebPageSearchEnabled()) {
                    return;
                }
                if (bigChange) {
                    ChatActivity.this.searchLinks(text, true);
                    return;
                }
                ChatActivity.this.waitingForCharaterEnterRunnable = new Runnable() {
                    public void run() {
                        if (this == ChatActivity.this.waitingForCharaterEnterRunnable) {
                            ChatActivity.this.searchLinks(text, false);
                            ChatActivity.this.waitingForCharaterEnterRunnable = null;
                        }
                    }
                };
                AndroidUtilities.runOnUIThread(ChatActivity.this.waitingForCharaterEnterRunnable, AndroidUtilities.WEB_URL == null ? 3000 : 1000);
            }

            public void needSendTyping() {
                MessagesController.getInstance().sendTyping(ChatActivity.this.dialog_id, 0, ChatActivity.this.classGuid);
            }

            public void onAttachButtonHidden() {
                if (!ChatActivity.this.actionBar.isSearchFieldVisible()) {
                    if (ChatActivity.this.attachItem != null) {
                        ChatActivity.this.attachItem.setVisibility(0);
                    }
                    if (ChatActivity.this.headerItem != null) {
                        ChatActivity.this.headerItem.setVisibility(8);
                    }
                }
            }

            public void onAttachButtonShow() {
                if (!ChatActivity.this.actionBar.isSearchFieldVisible()) {
                    if (ChatActivity.this.attachItem != null) {
                        ChatActivity.this.attachItem.setVisibility(8);
                    }
                    if (ChatActivity.this.headerItem != null) {
                        ChatActivity.this.headerItem.setVisibility(0);
                    }
                }
            }

            public void onWindowSizeChanged(int size) {
                if (size < AndroidUtilities.dp(72.0f) + ActionBar.getCurrentActionBarHeight()) {
                    ChatActivity.this.allowStickersPanel = false;
                    if (ChatActivity.this.stickersPanel.getVisibility() == 0) {
                        ChatActivity.this.stickersPanel.clearAnimation();
                        ChatActivity.this.stickersPanel.setVisibility(4);
                    }
                    if (ChatActivity.this.mentionListView != null && ChatActivity.this.mentionListView.getVisibility() == 0) {
                        ChatActivity.this.mentionListView.clearAnimation();
                        ChatActivity.this.mentionListView.setVisibility(4);
                    }
                } else {
                    ChatActivity.this.allowStickersPanel = true;
                    if (ChatActivity.this.stickersPanel.getVisibility() == 4) {
                        ChatActivity.this.stickersPanel.clearAnimation();
                        ChatActivity.this.stickersPanel.setVisibility(0);
                    }
                    if (ChatActivity.this.mentionListView != null && ChatActivity.this.mentionListView.getVisibility() == 4) {
                        ChatActivity.this.mentionListView.clearAnimation();
                        ChatActivity.this.mentionListView.setVisibility(0);
                    }
                }
                ChatActivity.this.updateMessagesVisisblePart();
            }
        });
        linearLayout = new FrameLayout(context);
        linearLayout.setClickable(true);
        this.chatActivityEnterView.addTopView(linearLayout, 48);
        linearLayout = new View(context);
        linearLayout.setBackgroundColor(-1513240);
        linearLayout.addView(linearLayout, LayoutHelper.createFrame(-1, 1, 83));
        this.replyIconImageView = new ImageView(context);
        this.replyIconImageView.setScaleType(ScaleType.CENTER);
        linearLayout.addView(this.replyIconImageView, LayoutHelper.createFrame(52, 46, 51));
        linearLayout = new ImageView(context);
        linearLayout.setImageResource(C0553R.drawable.delete_reply);
        linearLayout.setScaleType(ScaleType.CENTER);
        linearLayout.addView(linearLayout, LayoutHelper.createFrame(52, 46.0f, 53, 0.0f, 0.5f, 0.0f, 0.0f));
        linearLayout.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (ChatActivity.this.forwardingMessages != null) {
                    ChatActivity.this.forwardingMessages.clear();
                }
                ChatActivity.this.showReplyPanel(false, null, null, ChatActivity.this.foundWebPage, true, true);
            }
        });
        this.replyNameTextView = new TextView(context);
        this.replyNameTextView.setTextSize(1, 14.0f);
        this.replyNameTextView.setTextColor(-13141330);
        this.replyNameTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        this.replyNameTextView.setSingleLine(true);
        this.replyNameTextView.setEllipsize(TruncateAt.END);
        this.replyNameTextView.setMaxLines(1);
        linearLayout.addView(this.replyNameTextView, LayoutHelper.createFrame(-2, -2.0f, 51, 52.0f, 4.0f, 52.0f, 0.0f));
        this.replyObjectTextView = new TextView(context);
        this.replyObjectTextView.setTextSize(1, 14.0f);
        this.replyObjectTextView.setTextColor(-6710887);
        this.replyObjectTextView.setSingleLine(true);
        this.replyObjectTextView.setEllipsize(TruncateAt.END);
        this.replyObjectTextView.setMaxLines(1);
        linearLayout.addView(this.replyObjectTextView, LayoutHelper.createFrame(-2, -2.0f, 51, 52.0f, 22.0f, 52.0f, 0.0f));
        this.replyImageView = new BackupImageView(context);
        linearLayout.addView(this.replyImageView, LayoutHelper.createFrame(34, 34.0f, 51, 52.0f, 6.0f, 0.0f, 0.0f));
        this.stickersPanel = new FrameLayout(context);
        this.stickersPanel.setVisibility(8);
        contentView.addView(this.stickersPanel, LayoutHelper.createFrame(-2, 81.5f, 83, 0.0f, 0.0f, 0.0f, 38.0f));
        this.stickersListView = new RecyclerListView(context);
        this.stickersListView.setDisallowInterceptTouchEvents(true);
        LayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(0);
        this.stickersListView.setLayoutManager(linearLayoutManager);
        this.stickersListView.setClipToPadding(false);
        if (VERSION.SDK_INT >= 9) {
            this.stickersListView.setOverScrollMode(2);
        }
        this.stickersPanel.addView(this.stickersListView, LayoutHelper.createFrame(-1, 78.0f));
        if (this.currentEncryptedChat == null || (this.currentEncryptedChat != null && AndroidUtilities.getPeerLayerVersion(this.currentEncryptedChat.layer) >= 23)) {
            this.chatActivityEnterView.setAllowStickers(true);
            if (this.stickersAdapter != null) {
                this.stickersAdapter.onDestroy();
            }
            this.stickersListView.setPadding(AndroidUtilities.dp(18.0f), 0, AndroidUtilities.dp(18.0f), 0);
            recyclerListView = this.stickersListView;
            chatActivityAdapter = new StickersAdapter(context, new StickersAdapterDelegate() {
                public void needChangePanelVisibility(final boolean show) {
                    float f = 1.0f;
                    if (!show || ChatActivity.this.stickersPanel.getVisibility() != 0) {
                        if (show || ChatActivity.this.stickersPanel.getVisibility() != 8) {
                            if (show) {
                                ChatActivity.this.stickersListView.scrollToPosition(0);
                                ChatActivity.this.stickersPanel.clearAnimation();
                                ChatActivity.this.stickersPanel.setVisibility(ChatActivity.this.allowStickersPanel ? 0 : 4);
                            }
                            if (ChatActivity.this.runningAnimation != null) {
                                ChatActivity.this.runningAnimation.cancel();
                                ChatActivity.this.runningAnimation = null;
                            }
                            if (ChatActivity.this.stickersPanel.getVisibility() != 4) {
                                float f2;
                                ChatActivity.this.runningAnimation = new AnimatorSetProxy();
                                AnimatorSetProxy access$7300 = ChatActivity.this.runningAnimation;
                                Object[] objArr = new Object[1];
                                FrameLayout access$6900 = ChatActivity.this.stickersPanel;
                                String str = "alpha";
                                float[] fArr = new float[2];
                                if (show) {
                                    f2 = 0.0f;
                                } else {
                                    f2 = 1.0f;
                                }
                                fArr[0] = f2;
                                if (!show) {
                                    f = 0.0f;
                                }
                                fArr[1] = f;
                                objArr[0] = ObjectAnimatorProxy.ofFloat(access$6900, str, fArr);
                                access$7300.playTogether(objArr);
                                ChatActivity.this.runningAnimation.setDuration(150);
                                ChatActivity.this.runningAnimation.addListener(new AnimatorListenerAdapterProxy() {
                                    public void onAnimationEnd(Object animation) {
                                        if (ChatActivity.this.runningAnimation != null && ChatActivity.this.runningAnimation.equals(animation)) {
                                            if (!show) {
                                                ChatActivity.this.stickersAdapter.clearStickers();
                                                ChatActivity.this.stickersPanel.clearAnimation();
                                                ChatActivity.this.stickersPanel.setVisibility(8);
                                            }
                                            ChatActivity.this.runningAnimation = null;
                                        }
                                    }
                                });
                                ChatActivity.this.runningAnimation.start();
                            } else if (!show) {
                                ChatActivity.this.stickersPanel.setVisibility(8);
                            }
                        }
                    }
                }
            });
            this.stickersAdapter = chatActivityAdapter;
            recyclerListView.setAdapter(chatActivityAdapter);
            this.stickersListView.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(View view, int position) {
                    Document document = ChatActivity.this.stickersAdapter.getItem(position);
                    if (document instanceof TL_document) {
                        boolean z;
                        SendMessagesHelper instance = SendMessagesHelper.getInstance();
                        long access$1200 = ChatActivity.this.dialog_id;
                        MessageObject access$1300 = ChatActivity.this.replyingMessageObject;
                        if (ChatActivity.this.chatActivityEnterView == null || ChatActivity.this.chatActivityEnterView.asAdmin()) {
                            z = true;
                        } else {
                            z = false;
                        }
                        instance.sendSticker(document, access$1200, access$1300, z);
                        ChatActivity.this.showReplyPanel(false, null, null, null, false, true);
                    }
                    ChatActivity.this.chatActivityEnterView.setFieldText("");
                }
            });
        }
        linearLayout = new ImageView(context);
        linearLayout.setImageResource(C0553R.drawable.stickers_back_arrow);
        this.stickersPanel.addView(linearLayout, LayoutHelper.createFrame(-2, -2.0f, 83, 53.0f, 0.0f, 0.0f, 0.0f));
        this.bottomOverlay = new FrameLayout(context);
        this.bottomOverlay.setBackgroundColor(-1);
        this.bottomOverlay.setVisibility(4);
        this.bottomOverlay.setFocusable(true);
        this.bottomOverlay.setFocusableInTouchMode(true);
        this.bottomOverlay.setClickable(true);
        contentView.addView(this.bottomOverlay, LayoutHelper.createFrame(-1, 48, 80));
        this.bottomOverlayText = new TextView(context);
        this.bottomOverlayText.setTextSize(1, 16.0f);
        this.bottomOverlayText.setTextColor(-8421505);
        this.bottomOverlay.addView(this.bottomOverlayText, LayoutHelper.createFrame(-2, -2, 17));
        this.bottomOverlayChat = new FrameLayout(context);
        this.bottomOverlayChat.setBackgroundColor(-262915);
        this.bottomOverlayChat.setVisibility(4);
        contentView.addView(this.bottomOverlayChat, LayoutHelper.createFrame(-1, 48, 80));
        this.bottomOverlayChat.setOnClickListener(new OnClickListener() {

            class C08721 implements DialogInterface.OnClickListener {
                C08721() {
                }

                public void onClick(DialogInterface dialogInterface, int i) {
                    MessagesController.getInstance().unblockUser(ChatActivity.this.currentUser.id);
                }
            }

            class C08732 implements DialogInterface.OnClickListener {
                C08732() {
                }

                public void onClick(DialogInterface dialogInterface, int i) {
                    MessagesController.getInstance().deleteDialog(ChatActivity.this.dialog_id, 0);
                    ChatActivity.this.finishFragment();
                }
            }

            public void onClick(View view) {
                if (ChatActivity.this.getParentActivity() != null) {
                    Builder builder = null;
                    SendMessagesHelper instance;
                    String str;
                    long access$1200;
                    boolean z;
                    if (ChatActivity.this.currentUser == null || !ChatActivity.this.userBlocked) {
                        if (ChatActivity.this.currentUser != null && ChatActivity.this.currentUser.bot && ChatActivity.this.botUser != null) {
                            if (ChatActivity.this.botUser.length() != 0) {
                                MessagesController.getInstance().sendBotStart(ChatActivity.this.currentUser, ChatActivity.this.botUser);
                            } else {
                                instance = SendMessagesHelper.getInstance();
                                str = "/start";
                                access$1200 = ChatActivity.this.dialog_id;
                                z = ChatActivity.this.chatActivityEnterView == null || ChatActivity.this.chatActivityEnterView.asAdmin();
                                instance.sendMessage(str, access$1200, null, null, false, z);
                            }
                            ChatActivity.this.botUser = null;
                            ChatActivity.this.updateBottomOverlay();
                        } else if (!ChatObject.isChannel(ChatActivity.this.currentChat) || ChatActivity.this.currentChat.megagroup || (ChatActivity.this.currentChat instanceof TL_channelForbidden)) {
                            builder = new Builder(ChatActivity.this.getParentActivity());
                            builder.setMessage(LocaleController.getString("AreYouSureDeleteThisChat", C0553R.string.AreYouSureDeleteThisChat));
                            builder.setPositiveButton(LocaleController.getString("OK", C0553R.string.OK), new C08732());
                        } else if (ChatObject.isNotInChat(ChatActivity.this.currentChat)) {
                            MessagesController.getInstance().addUserToChat(ChatActivity.this.currentChat.id, UserConfig.getCurrentUser(), null, 0, null, null);
                        } else {
                            ChatActivity.this.toggleMute(true);
                        }
                    } else if (ChatActivity.this.currentUser.bot) {
                        String botUserLast = ChatActivity.this.botUser;
                        ChatActivity.this.botUser = null;
                        MessagesController.getInstance().unblockUser(ChatActivity.this.currentUser.id);
                        if (botUserLast == null || botUserLast.length() == 0) {
                            instance = SendMessagesHelper.getInstance();
                            str = "/start";
                            access$1200 = ChatActivity.this.dialog_id;
                            z = ChatActivity.this.chatActivityEnterView == null || ChatActivity.this.chatActivityEnterView.asAdmin();
                            instance.sendMessage(str, access$1200, null, null, false, z);
                        } else {
                            MessagesController.getInstance().sendBotStart(ChatActivity.this.currentUser, botUserLast);
                        }
                    } else {
                        builder = new Builder(ChatActivity.this.getParentActivity());
                        builder.setMessage(LocaleController.getString("AreYouSureUnblockContact", C0553R.string.AreYouSureUnblockContact));
                        builder.setPositiveButton(LocaleController.getString("OK", C0553R.string.OK), new C08721());
                    }
                    if (builder != null) {
                        builder.setTitle(LocaleController.getString("AppName", C0553R.string.AppName));
                        builder.setNegativeButton(LocaleController.getString("Cancel", C0553R.string.Cancel), null);
                        ChatActivity.this.showDialog(builder.create());
                    }
                }
            }
        });
        this.bottomOverlayChatText = new TextView(context);
        this.bottomOverlayChatText.setTextSize(1, 18.0f);
        this.bottomOverlayChatText.setTextColor(-12685407);
        this.bottomOverlayChat.addView(this.bottomOverlayChatText, LayoutHelper.createFrame(-2, -2, 17));
        this.chatAdapter.updateRows();
        if (this.loading && this.messages.isEmpty()) {
            this.progressView.setVisibility(this.chatAdapter.botInfoRow == -1 ? 0 : 4);
            this.chatListView.setEmptyView(null);
        } else {
            this.progressView.setVisibility(4);
            this.chatListView.setEmptyView(this.emptyViewContainer);
        }
        ChatActivityEnterView chatActivityEnterView = this.chatActivityEnterView;
        if (this.userBlocked) {
            messageObject = null;
        } else {
            messageObject = this.botButtons;
        }
        chatActivityEnterView.setButtons(messageObject);
        if (!AndroidUtilities.isTablet() || AndroidUtilities.isSmallTablet()) {
            View playerView = new PlayerView(context, this);
            this.playerView = playerView;
            contentView.addView(playerView, LayoutHelper.createFrame(-1, 39.0f, 51, 0.0f, -36.0f, 0.0f, 0.0f));
        }
        updateContactStatus();
        updateBottomOverlay();
        updateSecretStatus();
        updateSpamView();
        return this.fragmentView;
    }

    private void checkScrollForLoad() {
        if (this.chatLayoutManager != null && !this.paused) {
            int firstVisibleItem = this.chatLayoutManager.findFirstVisibleItemPosition();
            int visibleItemCount = firstVisibleItem == -1 ? 0 : Math.abs(this.chatLayoutManager.findLastVisibleItemPosition() - firstVisibleItem) + 1;
            if (visibleItemCount > 0) {
                MessagesController instance;
                long j;
                int i;
                int i2;
                int i3;
                int i4;
                int i5;
                int totalItemCount = this.chatAdapter.getItemCount();
                if (firstVisibleItem <= 25 && !this.loading) {
                    boolean z;
                    if (!this.endReached[0]) {
                        this.loading = true;
                        this.waitingForLoad.add(Integer.valueOf(this.lastLoadIndex));
                        if (this.messagesByDays.size() != 0) {
                            instance = MessagesController.getInstance();
                            j = this.dialog_id;
                            i = this.maxMessageId[0];
                            z = !this.cacheEndReached[0];
                            i2 = this.minDate[0];
                            i3 = this.classGuid;
                            i4 = this.channelMessagesImportant;
                            i5 = this.lastLoadIndex;
                            this.lastLoadIndex = i5 + 1;
                            instance.loadMessages(j, 50, i, z, i2, i3, 0, 0, i4, i5);
                        } else {
                            instance = MessagesController.getInstance();
                            j = this.dialog_id;
                            z = !this.cacheEndReached[0];
                            i2 = this.minDate[0];
                            i3 = this.classGuid;
                            i4 = this.channelMessagesImportant;
                            i5 = this.lastLoadIndex;
                            this.lastLoadIndex = i5 + 1;
                            instance.loadMessages(j, 50, 0, z, i2, i3, 0, 0, i4, i5);
                        }
                    } else if (!(this.mergeDialogId == 0 || this.endReached[1])) {
                        this.loading = true;
                        this.waitingForLoad.add(Integer.valueOf(this.lastLoadIndex));
                        instance = MessagesController.getInstance();
                        j = this.mergeDialogId;
                        i = this.maxMessageId[1];
                        z = !this.cacheEndReached[1];
                        i2 = this.minDate[1];
                        i3 = this.classGuid;
                        i5 = this.lastLoadIndex;
                        this.lastLoadIndex = i5 + 1;
                        instance.loadMessages(j, 50, i, z, i2, i3, 0, 0, 0, i5);
                    }
                }
                if (!this.loadingForward && firstVisibleItem + visibleItemCount >= totalItemCount - 10) {
                    if (this.mergeDialogId != 0 && !this.forwardEndReached[1]) {
                        this.waitingForLoad.add(Integer.valueOf(this.lastLoadIndex));
                        instance = MessagesController.getInstance();
                        j = this.mergeDialogId;
                        i = this.minMessageId[1];
                        i2 = this.maxDate[1];
                        i3 = this.classGuid;
                        i5 = this.lastLoadIndex;
                        this.lastLoadIndex = i5 + 1;
                        instance.loadMessages(j, 50, i, true, i2, i3, 1, 0, 0, i5);
                        this.loadingForward = true;
                    } else if (!this.forwardEndReached[0]) {
                        this.waitingForLoad.add(Integer.valueOf(this.lastLoadIndex));
                        instance = MessagesController.getInstance();
                        j = this.dialog_id;
                        i = this.minMessageId[0];
                        i2 = this.maxDate[0];
                        i3 = this.classGuid;
                        i4 = this.channelMessagesImportant;
                        i5 = this.lastLoadIndex;
                        this.lastLoadIndex = i5 + 1;
                        instance.loadMessages(j, 50, i, true, i2, i3, 1, 0, i4, i5);
                        this.loadingForward = true;
                    }
                }
            }
        }
    }

    private void processSelectedAttach(int which) {
        if (which == 0 || which == 1 || which == 4 || which == 2) {
            String action;
            if (this.currentChat != null) {
                if (this.currentChat.participants_count > MessagesController.getInstance().groupBigSize) {
                    if (which == 0 || which == 1) {
                        action = "bigchat_upload_photo";
                    } else {
                        action = "bigchat_upload_document";
                    }
                } else if (which == 0 || which == 1) {
                    action = "chat_upload_photo";
                } else {
                    action = "chat_upload_document";
                }
            } else if (which == 0 || which == 1) {
                action = "pm_upload_photo";
            } else {
                action = "pm_upload_document";
            }
            if (!MessagesController.isFeatureEnabled(action, this)) {
                return;
            }
        }
        if (which == 0) {
            try {
                Intent takePictureIntent = new Intent("android.media.action.IMAGE_CAPTURE");
                File image = AndroidUtilities.generatePicturePath();
                if (image != null) {
                    takePictureIntent.putExtra("output", Uri.fromFile(image));
                    this.currentPicturePath = image.getAbsolutePath();
                }
                startActivityForResult(takePictureIntent, 0);
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
        } else if (which == 1) {
            if (VERSION.SDK_INT < 23 || getParentActivity().checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE") == 0) {
                PhotoAlbumPickerActivity fragment = new PhotoAlbumPickerActivity(false, this);
                fragment.setDelegate(new PhotoAlbumPickerActivityDelegate() {
                    public void didSelectPhotos(ArrayList<String> photos, ArrayList<String> captions, ArrayList<SearchImage> webPhotos) {
                        boolean z;
                        boolean z2;
                        long access$1200 = ChatActivity.this.dialog_id;
                        MessageObject access$1300 = ChatActivity.this.replyingMessageObject;
                        if (ChatActivity.this.chatActivityEnterView == null || ChatActivity.this.chatActivityEnterView.asAdmin()) {
                            z = true;
                        } else {
                            z = false;
                        }
                        SendMessagesHelper.prepareSendingPhotos(photos, null, access$1200, access$1300, captions, z);
                        access$1200 = ChatActivity.this.dialog_id;
                        access$1300 = ChatActivity.this.replyingMessageObject;
                        if (ChatActivity.this.chatActivityEnterView == null || ChatActivity.this.chatActivityEnterView.asAdmin()) {
                            z2 = true;
                        } else {
                            z2 = false;
                        }
                        SendMessagesHelper.prepareSendingPhotosSearch(webPhotos, access$1200, access$1300, z2);
                        ChatActivity.this.showReplyPanel(false, null, null, null, false, true);
                    }

                    public void startPhotoSelectActivity() {
                        try {
                            Intent videoPickerIntent = new Intent();
                            videoPickerIntent.setType("video/*");
                            videoPickerIntent.setAction("android.intent.action.GET_CONTENT");
                            videoPickerIntent.putExtra("android.intent.extra.sizeLimit", 1610612736);
                            Intent photoPickerIntent = new Intent("android.intent.action.PICK");
                            photoPickerIntent.setType("image/*");
                            Intent chooserIntent = Intent.createChooser(photoPickerIntent, null);
                            chooserIntent.putExtra("android.intent.extra.INITIAL_INTENTS", new Intent[]{videoPickerIntent});
                            ChatActivity.this.startActivityForResult(chooserIntent, 1);
                        } catch (Throwable e) {
                            FileLog.m611e("tmessages", e);
                        }
                    }

                    public boolean didSelectVideo(String path) {
                        if (VERSION.SDK_INT < 16) {
                            boolean z;
                            long access$1200 = ChatActivity.this.dialog_id;
                            MessageObject access$1300 = ChatActivity.this.replyingMessageObject;
                            if (ChatActivity.this.chatActivityEnterView == null || ChatActivity.this.chatActivityEnterView.asAdmin()) {
                                z = true;
                            } else {
                                z = false;
                            }
                            SendMessagesHelper.prepareSendingVideo(path, 0, 0, 0, 0, null, access$1200, access$1300, z);
                            ChatActivity.this.showReplyPanel(false, null, null, null, false, true);
                            return true;
                        } else if (ChatActivity.this.openVideoEditor(path, true, true)) {
                            return false;
                        } else {
                            return true;
                        }
                    }
                });
                presentFragment(fragment);
                return;
            }
            getParentActivity().requestPermissions(new String[]{"android.permission.READ_EXTERNAL_STORAGE"}, 4);
        } else if (which == 2) {
            try {
                Intent takeVideoIntent = new Intent("android.media.action.VIDEO_CAPTURE");
                File video = AndroidUtilities.generateVideoPath();
                if (video != null) {
                    if (VERSION.SDK_INT >= 18) {
                        takeVideoIntent.putExtra("output", Uri.fromFile(video));
                    }
                    takeVideoIntent.putExtra("android.intent.extra.sizeLimit", 1610612736);
                    this.currentPicturePath = video.getAbsolutePath();
                }
                startActivityForResult(takeVideoIntent, 2);
            } catch (Throwable e2) {
                FileLog.m611e("tmessages", e2);
            }
        } else if (which == 6) {
            if (isGoogleMapsInstalled()) {
                LocationActivity fragment2 = new LocationActivity();
                fragment2.setDelegate(new LocationActivityDelegate() {
                    public void didSelectLocation(MessageMedia location) {
                        SendMessagesHelper instance = SendMessagesHelper.getInstance();
                        long access$1200 = ChatActivity.this.dialog_id;
                        MessageObject access$1300 = ChatActivity.this.replyingMessageObject;
                        boolean z = ChatActivity.this.chatActivityEnterView == null || ChatActivity.this.chatActivityEnterView.asAdmin();
                        instance.sendMessage(location, access$1200, access$1300, z);
                        ChatActivity.this.moveScrollToLastMessage();
                        ChatActivity.this.showReplyPanel(false, null, null, null, false, true);
                        if (ChatActivity.this.paused) {
                            ChatActivity.this.scrollToTopOnResume = true;
                        }
                    }
                });
                presentFragment(fragment2);
            }
        } else if (which == 4) {
            if (VERSION.SDK_INT < 23 || getParentActivity().checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE") == 0) {
                DocumentSelectActivity fragment3 = new DocumentSelectActivity();
                fragment3.setDelegate(new DocumentSelectActivityDelegate() {
                    public void didSelectFiles(DocumentSelectActivity activity, ArrayList<String> files) {
                        boolean z;
                        activity.finishFragment();
                        long access$1200 = ChatActivity.this.dialog_id;
                        MessageObject access$1300 = ChatActivity.this.replyingMessageObject;
                        if (ChatActivity.this.chatActivityEnterView == null || ChatActivity.this.chatActivityEnterView.asAdmin()) {
                            z = true;
                        } else {
                            z = false;
                        }
                        SendMessagesHelper.prepareSendingDocuments(files, files, null, null, access$1200, access$1300, z);
                        ChatActivity.this.showReplyPanel(false, null, null, null, false, true);
                    }

                    public void startDocumentSelectActivity() {
                        try {
                            Intent photoPickerIntent = new Intent("android.intent.action.PICK");
                            photoPickerIntent.setType("*/*");
                            ChatActivity.this.startActivityForResult(photoPickerIntent, 21);
                        } catch (Throwable e) {
                            FileLog.m611e("tmessages", e);
                        }
                    }
                });
                presentFragment(fragment3);
                return;
            }
            getParentActivity().requestPermissions(new String[]{"android.permission.READ_EXTERNAL_STORAGE"}, 4);
        } else if (which == 3) {
            if (VERSION.SDK_INT < 23 || getParentActivity().checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE") == 0) {
                AudioSelectActivity fragment4 = new AudioSelectActivity();
                fragment4.setDelegate(new AudioSelectActivityDelegate() {
                    public void didSelectAudio(ArrayList<MessageObject> audios) {
                        boolean z;
                        long access$1200 = ChatActivity.this.dialog_id;
                        MessageObject access$1300 = ChatActivity.this.replyingMessageObject;
                        if (ChatActivity.this.chatActivityEnterView == null || ChatActivity.this.chatActivityEnterView.asAdmin()) {
                            z = true;
                        } else {
                            z = false;
                        }
                        SendMessagesHelper.prepareSendingAudioDocuments(audios, access$1200, access$1300, z);
                        ChatActivity.this.showReplyPanel(false, null, null, null, false, true);
                    }
                });
                presentFragment(fragment4);
                return;
            }
            getParentActivity().requestPermissions(new String[]{"android.permission.READ_EXTERNAL_STORAGE"}, 4);
        } else if (which != 5) {
        } else {
            if (VERSION.SDK_INT < 23 || getParentActivity().checkSelfPermission("android.permission.READ_CONTACTS") == 0) {
                try {
                    Intent intent = new Intent("android.intent.action.PICK", Contacts.CONTENT_URI);
                    intent.setType("vnd.android.cursor.dir/phone_v2");
                    startActivityForResult(intent, 31);
                    return;
                } catch (Throwable e22) {
                    FileLog.m611e("tmessages", e22);
                    return;
                }
            }
            getParentActivity().requestPermissions(new String[]{"android.permission.READ_CONTACTS"}, 5);
        }
    }

    public boolean dismissDialogOnPause(Dialog dialog) {
        return !(dialog == this.chatAttachViewSheet && PhotoViewer.getInstance().isVisible()) && super.dismissDialogOnPause(dialog);
    }

    private void searchLinks(final CharSequence charSequence, boolean force) {
        if (this.currentEncryptedChat == null) {
            if (force && this.foundWebPage != null) {
                if (this.foundWebPage.url != null) {
                    int index = TextUtils.indexOf(charSequence, this.foundWebPage.url);
                    boolean lenEqual;
                    char lastChar;
                    if (index == -1) {
                        index = TextUtils.indexOf(charSequence, this.foundWebPage.display_url);
                        if (index == -1 || this.foundWebPage.display_url.length() + index != charSequence.length()) {
                            lenEqual = false;
                        } else {
                            lenEqual = true;
                        }
                        if (index == -1 || lenEqual) {
                            lastChar = '\u0000';
                        } else {
                            lastChar = charSequence.charAt(this.foundWebPage.display_url.length() + index);
                        }
                    } else {
                        if (this.foundWebPage.url.length() + index == charSequence.length()) {
                            lenEqual = true;
                        } else {
                            lenEqual = false;
                        }
                        if (lenEqual) {
                            lastChar = '\u0000';
                        } else {
                            lastChar = charSequence.charAt(this.foundWebPage.url.length() + index);
                        }
                    }
                    if (index != -1 && (lenEqual || lastChar == ' ' || lastChar == ',' || lastChar == '.' || lastChar == '!' || lastChar == '/')) {
                        return;
                    }
                }
                this.pendingLinkSearchString = null;
                showReplyPanel(false, null, null, this.foundWebPage, false, true);
            }
            Utilities.searchQueue.postRunnable(new Runnable() {

                class C08741 implements Runnable {
                    C08741() {
                    }

                    public void run() {
                        if (ChatActivity.this.foundWebPage != null) {
                            ChatActivity.this.showReplyPanel(false, null, null, ChatActivity.this.foundWebPage, false, true);
                            ChatActivity.this.foundWebPage = null;
                        }
                    }
                }

                class C08752 implements Runnable {
                    C08752() {
                    }

                    public void run() {
                        if (ChatActivity.this.foundWebPage != null) {
                            ChatActivity.this.showReplyPanel(false, null, null, ChatActivity.this.foundWebPage, false, true);
                            ChatActivity.this.foundWebPage = null;
                        }
                    }
                }

                public void run() {
                    Throwable e;
                    CharSequence textToCheck;
                    final TL_messages_getWebPagePreview req;
                    if (ChatActivity.this.linkSearchRequestId != 0) {
                        ConnectionsManager.getInstance().cancelRequest(ChatActivity.this.linkSearchRequestId, true);
                        ChatActivity.this.linkSearchRequestId = 0;
                    }
                    try {
                        ArrayList<CharSequence> urls;
                        Matcher m = AndroidUtilities.WEB_URL.matcher(charSequence);
                        ArrayList<CharSequence> urls2 = null;
                        while (m.find()) {
                            try {
                                if (urls2 == null) {
                                    urls = new ArrayList();
                                } else {
                                    urls = urls2;
                                }
                                urls.add(charSequence.subSequence(m.start(), m.end()));
                                urls2 = urls;
                            } catch (Exception e2) {
                                e = e2;
                                urls = urls2;
                            }
                        }
                        if (urls2 != null) {
                            if (ChatActivity.this.foundUrls != null && urls2.size() == ChatActivity.this.foundUrls.size()) {
                                boolean clear = true;
                                for (int a = 0; a < urls2.size(); a++) {
                                    if (!TextUtils.equals((CharSequence) urls2.get(a), (CharSequence) ChatActivity.this.foundUrls.get(a))) {
                                        clear = false;
                                    }
                                }
                                if (clear) {
                                    urls = urls2;
                                    return;
                                }
                            }
                        }
                        ChatActivity.this.foundUrls = urls2;
                        if (urls2 == null) {
                            AndroidUtilities.runOnUIThread(new C08741());
                            urls = urls2;
                            return;
                        }
                        textToCheck = TextUtils.join(" ", urls2);
                        urls = urls2;
                        req = new TL_messages_getWebPagePreview();
                        if (textToCheck instanceof String) {
                            req.message = textToCheck.toString();
                        } else {
                            req.message = (String) textToCheck;
                        }
                        ChatActivity.this.linkSearchRequestId = ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
                            public void run(final TLObject response, final TL_error error) {
                                AndroidUtilities.runOnUIThread(new Runnable() {
                                    public void run() {
                                        ChatActivity.this.linkSearchRequestId = 0;
                                        if (error != null) {
                                            return;
                                        }
                                        if (response instanceof TL_messageMediaWebPage) {
                                            ChatActivity.this.foundWebPage = ((TL_messageMediaWebPage) response).webpage;
                                            if ((ChatActivity.this.foundWebPage instanceof TL_webPage) || (ChatActivity.this.foundWebPage instanceof TL_webPagePending)) {
                                                if (ChatActivity.this.foundWebPage instanceof TL_webPagePending) {
                                                    ChatActivity.this.pendingLinkSearchString = req.message;
                                                }
                                                ChatActivity.this.showReplyPanel(true, null, null, ChatActivity.this.foundWebPage, false, true);
                                            } else if (ChatActivity.this.foundWebPage != null) {
                                                ChatActivity.this.showReplyPanel(false, null, null, ChatActivity.this.foundWebPage, false, true);
                                                ChatActivity.this.foundWebPage = null;
                                            }
                                        } else if (ChatActivity.this.foundWebPage != null) {
                                            ChatActivity.this.showReplyPanel(false, null, null, ChatActivity.this.foundWebPage, false, true);
                                            ChatActivity.this.foundWebPage = null;
                                        }
                                    }
                                });
                            }
                        });
                        ConnectionsManager.getInstance().bindRequestToGuid(ChatActivity.this.linkSearchRequestId, ChatActivity.this.classGuid);
                    } catch (Exception e3) {
                        e = e3;
                    }
                    FileLog.m611e("tmessages", e);
                    String text = charSequence.toString().toLowerCase();
                    if (charSequence.length() < 13 || !(text.contains("http://") || text.contains("https://"))) {
                        AndroidUtilities.runOnUIThread(new C08752());
                        return;
                    }
                    textToCheck = charSequence;
                    req = new TL_messages_getWebPagePreview();
                    if (textToCheck instanceof String) {
                        req.message = textToCheck.toString();
                    } else {
                        req.message = (String) textToCheck;
                    }
                    ChatActivity.this.linkSearchRequestId = ConnectionsManager.getInstance().sendRequest(req, /* anonymous class already generated */);
                    ConnectionsManager.getInstance().bindRequestToGuid(ChatActivity.this.linkSearchRequestId, ChatActivity.this.classGuid);
                }
            });
        }
    }

    private void forwardMessages(ArrayList<MessageObject> arrayList, boolean fromMyName) {
        boolean z = false;
        if (arrayList != null && !arrayList.isEmpty()) {
            long j;
            if (fromMyName) {
                Iterator i$ = arrayList.iterator();
                while (i$.hasNext()) {
                    MessageObject object = (MessageObject) i$.next();
                    SendMessagesHelper instance = SendMessagesHelper.getInstance();
                    j = this.dialog_id;
                    boolean z2 = this.chatActivityEnterView == null || this.chatActivityEnterView.asAdmin();
                    instance.processForwardFromMyName(object, j, z2);
                }
                return;
            }
            SendMessagesHelper instance2 = SendMessagesHelper.getInstance();
            j = this.dialog_id;
            if (this.chatActivityEnterView == null || this.chatActivityEnterView.asAdmin()) {
                z = true;
            }
            instance2.sendMessage(arrayList, j, z);
        }
    }

    public void showReplyPanel(boolean show, MessageObject messageObject, ArrayList<MessageObject> messageObjects, WebPage webPage, boolean cancel, boolean animated) {
        if (this.chatActivityEnterView != null) {
            if (show) {
                if (messageObject != null || messageObjects != null || webPage != null) {
                    boolean openKeyboard = false;
                    if (!(messageObject == null || messageObject.getDialogId() == this.dialog_id)) {
                        messageObjects = new ArrayList();
                        messageObjects.add(messageObject);
                        messageObject = null;
                        openKeyboard = true;
                    }
                    User user;
                    String name;
                    Chat chat;
                    String mess;
                    if (messageObject != null) {
                        if (messageObject.messageOwner.from_id > 0) {
                            user = MessagesController.getInstance().getUser(Integer.valueOf(messageObject.messageOwner.from_id));
                            if (user != null) {
                                name = UserObject.getUserName(user);
                            } else {
                                return;
                            }
                        }
                        chat = MessagesController.getInstance().getChat(Integer.valueOf(-messageObject.messageOwner.from_id));
                        if (chat != null) {
                            name = chat.title;
                        } else {
                            return;
                        }
                        this.forwardingMessages = null;
                        this.replyingMessageObject = messageObject;
                        this.chatActivityEnterView.setReplyingMessageObject(messageObject);
                        if (this.foundWebPage == null) {
                            this.replyIconImageView.setImageResource(C0553R.drawable.reply);
                            this.replyNameTextView.setText(name);
                            if (messageObject.messageText != null) {
                                mess = messageObject.messageText.toString();
                                if (mess.length() > 150) {
                                    mess = mess.substring(0, 150);
                                }
                                this.replyObjectTextView.setText(Emoji.replaceEmoji(mess.replace("\n", " "), this.replyObjectTextView.getPaint().getFontMetricsInt(), AndroidUtilities.dp(14.0f), false));
                            }
                        } else {
                            return;
                        }
                    } else if (messageObjects == null) {
                        this.replyIconImageView.setImageResource(C0553R.drawable.link);
                        if (webPage instanceof TL_webPagePending) {
                            this.replyNameTextView.setText(LocaleController.getString("GettingLinkInfo", C0553R.string.GettingLinkInfo));
                            this.replyObjectTextView.setText(this.pendingLinkSearchString);
                        } else {
                            if (webPage.site_name != null) {
                                this.replyNameTextView.setText(webPage.site_name);
                            } else if (webPage.title != null) {
                                this.replyNameTextView.setText(webPage.title);
                            } else {
                                this.replyNameTextView.setText(LocaleController.getString("LinkPreview", C0553R.string.LinkPreview));
                            }
                            if (webPage.description != null) {
                                this.replyObjectTextView.setText(webPage.description);
                            } else if (webPage.title != null && webPage.site_name != null) {
                                this.replyObjectTextView.setText(webPage.title);
                            } else if (webPage.author != null) {
                                this.replyObjectTextView.setText(webPage.author);
                            } else {
                                this.replyObjectTextView.setText(webPage.display_url);
                            }
                            this.chatActivityEnterView.setWebPage(webPage, true);
                        }
                    } else if (!messageObjects.isEmpty()) {
                        this.replyingMessageObject = null;
                        this.chatActivityEnterView.setReplyingMessageObject(null);
                        this.forwardingMessages = messageObjects;
                        if (this.foundWebPage == null) {
                            int a;
                            Integer uid;
                            this.chatActivityEnterView.setForceShowSendButton(true, animated);
                            ArrayList<Integer> uids = new ArrayList();
                            this.replyIconImageView.setImageResource(C0553R.drawable.forward_blue);
                            uids.add(Integer.valueOf(((MessageObject) messageObjects.get(0)).messageOwner.from_id));
                            int type = ((MessageObject) messageObjects.get(0)).type;
                            for (a = 1; a < messageObjects.size(); a++) {
                                uid = Integer.valueOf(((MessageObject) messageObjects.get(a)).messageOwner.from_id);
                                if (!uids.contains(uid)) {
                                    uids.add(uid);
                                }
                                if (((MessageObject) messageObjects.get(a)).type != type) {
                                    type = -1;
                                }
                            }
                            StringBuilder userNames = new StringBuilder();
                            for (a = 0; a < uids.size(); a++) {
                                uid = (Integer) uids.get(a);
                                chat = null;
                                user = null;
                                if (uid.intValue() > 0) {
                                    user = MessagesController.getInstance().getUser(uid);
                                } else {
                                    chat = MessagesController.getInstance().getChat(Integer.valueOf(-uid.intValue()));
                                }
                                if (user != null || chat != null) {
                                    if (uids.size() != 1) {
                                        if (uids.size() != 2 && userNames.length() != 0) {
                                            userNames.append(" ");
                                            userNames.append(LocaleController.formatPluralString("AndOther", uids.size() - 1));
                                            break;
                                        }
                                        if (userNames.length() > 0) {
                                            userNames.append(", ");
                                        }
                                        if (user == null) {
                                            userNames.append(chat.title);
                                        } else if (user.first_name != null && user.first_name.length() > 0) {
                                            userNames.append(user.first_name);
                                        } else if (user.last_name == null || user.last_name.length() <= 0) {
                                            userNames.append(" ");
                                        } else {
                                            userNames.append(user.last_name);
                                        }
                                    } else if (user != null) {
                                        userNames.append(UserObject.getUserName(user));
                                    } else {
                                        userNames.append(chat.title);
                                    }
                                }
                            }
                            this.replyNameTextView.setText(userNames);
                            if (type == -1 || type == 0 || type == 10 || type == 11) {
                                if (messageObjects.size() != 1 || ((MessageObject) messageObjects.get(0)).messageText == null) {
                                    this.replyObjectTextView.setText(LocaleController.formatPluralString("ForwardedMessage", messageObjects.size()));
                                } else {
                                    mess = ((MessageObject) messageObjects.get(0)).messageText.toString();
                                    if (mess.length() > 150) {
                                        mess = mess.substring(0, 150);
                                    }
                                    this.replyObjectTextView.setText(Emoji.replaceEmoji(mess.replace("\n", " "), this.replyObjectTextView.getPaint().getFontMetricsInt(), AndroidUtilities.dp(14.0f), false));
                                }
                            } else if (type == 1) {
                                this.replyObjectTextView.setText(LocaleController.formatPluralString("ForwardedPhoto", messageObjects.size()));
                                if (messageObjects.size() == 1) {
                                    messageObject = (MessageObject) messageObjects.get(0);
                                }
                            } else if (type == 4) {
                                this.replyObjectTextView.setText(LocaleController.formatPluralString("ForwardedLocation", messageObjects.size()));
                            } else if (type == 3) {
                                this.replyObjectTextView.setText(LocaleController.formatPluralString("ForwardedVideo", messageObjects.size()));
                                if (messageObjects.size() == 1) {
                                    messageObject = (MessageObject) messageObjects.get(0);
                                }
                            } else if (type == 12) {
                                this.replyObjectTextView.setText(LocaleController.formatPluralString("ForwardedContact", messageObjects.size()));
                            } else if (type == 2 || type == 14) {
                                this.replyObjectTextView.setText(LocaleController.formatPluralString("ForwardedAudio", messageObjects.size()));
                            } else if (type == 13) {
                                this.replyObjectTextView.setText(LocaleController.formatPluralString("ForwardedSticker", messageObjects.size()));
                            } else if (type == 8 || type == 9) {
                                if (messageObjects.size() == 1) {
                                    name = FileLoader.getDocumentFileName(((MessageObject) messageObjects.get(0)).messageOwner.media.document);
                                    if (name.length() != 0) {
                                        this.replyObjectTextView.setText(name);
                                    }
                                    messageObject = (MessageObject) messageObjects.get(0);
                                } else {
                                    this.replyObjectTextView.setText(LocaleController.formatPluralString("ForwardedFile", messageObjects.size()));
                                }
                            }
                        } else {
                            return;
                        }
                    } else {
                        return;
                    }
                    LayoutParams layoutParams1 = (LayoutParams) this.replyNameTextView.getLayoutParams();
                    LayoutParams layoutParams2 = (LayoutParams) this.replyObjectTextView.getLayoutParams();
                    PhotoSize photoSize = messageObject != null ? FileLoader.getClosestPhotoSizeWithSize(messageObject.photoThumbs, 80) : null;
                    int dp;
                    if (photoSize == null || messageObject.type == 13) {
                        this.replyImageView.setImageBitmap(null);
                        this.replyImageLocation = null;
                        this.replyImageView.setVisibility(4);
                        dp = AndroidUtilities.dp(52.0f);
                        layoutParams2.leftMargin = dp;
                        layoutParams1.leftMargin = dp;
                    } else {
                        this.replyImageLocation = photoSize.location;
                        this.replyImageView.setImage(this.replyImageLocation, "50_50", (Drawable) null);
                        this.replyImageView.setVisibility(0);
                        dp = AndroidUtilities.dp(96.0f);
                        layoutParams2.leftMargin = dp;
                        layoutParams1.leftMargin = dp;
                    }
                    this.replyNameTextView.setLayoutParams(layoutParams1);
                    this.replyObjectTextView.setLayoutParams(layoutParams2);
                    this.chatActivityEnterView.showTopView(animated, openKeyboard);
                }
            } else if (this.replyingMessageObject != null || this.forwardingMessages != null || this.foundWebPage != null) {
                if (this.replyingMessageObject != null && (this.replyingMessageObject.messageOwner.reply_markup instanceof TL_replyKeyboardForceReply)) {
                    ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0).edit().putInt("answered_" + this.dialog_id, this.replyingMessageObject.getId()).commit();
                }
                if (this.foundWebPage != null) {
                    this.foundWebPage = null;
                    this.chatActivityEnterView.setWebPage(null, !cancel);
                    if (!(webPage == null || (this.replyingMessageObject == null && this.forwardingMessages == null))) {
                        showReplyPanel(true, this.replyingMessageObject, this.forwardingMessages, null, false, true);
                        return;
                    }
                }
                if (this.forwardingMessages != null) {
                    forwardMessages(this.forwardingMessages, false);
                }
                this.chatActivityEnterView.setForceShowSendButton(false, animated);
                this.chatActivityEnterView.hideTopView(animated);
                this.chatActivityEnterView.setReplyingMessageObject(null);
                this.replyingMessageObject = null;
                this.forwardingMessages = null;
                this.replyImageLocation = null;
                ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0).edit().remove("reply_" + this.dialog_id).commit();
            }
        }
    }

    private void moveScrollToLastMessage() {
        if (this.chatListView != null) {
            this.chatLayoutManager.scrollToPositionWithOffset(this.messages.size() - 1, -100000 - this.chatListView.getPaddingTop());
        }
    }

    private boolean sendSecretMessageRead(MessageObject messageObject) {
        if (messageObject == null || messageObject.isOut() || !messageObject.isSecretMedia() || messageObject.messageOwner.destroyTime != 0 || messageObject.messageOwner.ttl <= 0) {
            return false;
        }
        MessagesController.getInstance().markMessageAsRead(this.dialog_id, messageObject.messageOwner.random_id, messageObject.messageOwner.ttl);
        messageObject.messageOwner.destroyTime = messageObject.messageOwner.ttl + ConnectionsManager.getInstance().getCurrentTime();
        return true;
    }

    private void clearChatData() {
        this.messages.clear();
        this.messagesByDays.clear();
        this.waitingForLoad.clear();
        this.progressView.setVisibility(this.chatAdapter.botInfoRow == -1 ? 0 : 4);
        this.chatListView.setEmptyView(null);
        for (int a = 0; a < 2; a++) {
            this.messagesDict[a].clear();
            if (this.currentEncryptedChat == null) {
                this.maxMessageId[a] = ConnectionsManager.DEFAULT_DATACENTER_ID;
                this.minMessageId[a] = Integer.MIN_VALUE;
            } else {
                this.maxMessageId[a] = Integer.MIN_VALUE;
                this.minMessageId[a] = ConnectionsManager.DEFAULT_DATACENTER_ID;
            }
            this.maxDate[a] = Integer.MIN_VALUE;
            this.minDate[a] = 0;
            this.endReached[a] = false;
            this.cacheEndReached[a] = false;
            this.forwardEndReached[a] = true;
        }
        this.first = true;
        this.firstLoading = true;
        this.loading = true;
        this.waitingForImportantLoad = false;
        this.startLoadFromMessageId = 0;
        this.last_message_id = 0;
        this.needSelectFromMessageId = false;
        this.chatAdapter.notifyDataSetChanged();
    }

    private void scrollToLastMessage(boolean pagedown) {
        if (!this.forwardEndReached[0] || this.first_unread_id != 0 || this.startLoadFromMessageId != 0) {
            clearChatData();
            this.waitingForLoad.add(Integer.valueOf(this.lastLoadIndex));
            MessagesController instance = MessagesController.getInstance();
            long j = this.dialog_id;
            int i = this.classGuid;
            int i2 = this.channelMessagesImportant;
            int i3 = this.lastLoadIndex;
            this.lastLoadIndex = i3 + 1;
            instance.loadMessages(j, 30, 0, true, 0, i, 0, 0, i2, i3);
        } else if (pagedown && this.chatLayoutManager.findLastCompletelyVisibleItemPosition() == this.chatAdapter.getItemCount() - 1) {
            showPagedownButton(false, true);
            this.highlightMessageId = ConnectionsManager.DEFAULT_DATACENTER_ID;
            updateVisibleRows();
        } else {
            this.chatLayoutManager.scrollToPositionWithOffset(this.messages.size() - 1, -100000 - this.chatListView.getPaddingTop());
        }
    }

    private void updateMessagesVisisblePart() {
        if (this.chatListView != null) {
            int count = this.chatListView.getChildCount();
            for (int a = 0; a < count; a++) {
                View view = this.chatListView.getChildAt(a);
                if (view instanceof ChatMessageCell) {
                    ChatMessageCell messageCell = (ChatMessageCell) view;
                    messageCell.getLocalVisibleRect(this.scrollRect);
                    messageCell.setVisiblePart(this.scrollRect.top, this.scrollRect.bottom - this.scrollRect.top);
                }
            }
        }
    }

    private void toggleMute(boolean instant) {
        Editor editor;
        TLRPC.Dialog dialog;
        if (MessagesController.getInstance().isDialogMuted(this.dialog_id)) {
            editor = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", 0).edit();
            editor.putInt("notify2_" + this.dialog_id, 0);
            MessagesStorage.getInstance().setDialogFlags(this.dialog_id, 0);
            editor.commit();
            dialog = (TLRPC.Dialog) MessagesController.getInstance().dialogs_dict.get(Long.valueOf(this.dialog_id));
            if (dialog != null) {
                dialog.notify_settings = new TL_peerNotifySettings();
            }
            NotificationsController.updateServerNotificationsSettings(this.dialog_id);
        } else if (instant) {
            editor = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", 0).edit();
            editor.putInt("notify2_" + this.dialog_id, 2);
            MessagesStorage.getInstance().setDialogFlags(this.dialog_id, 1);
            editor.commit();
            dialog = (TLRPC.Dialog) MessagesController.getInstance().dialogs_dict.get(Long.valueOf(this.dialog_id));
            if (dialog != null) {
                dialog.notify_settings = new TL_peerNotifySettings();
                dialog.notify_settings.mute_until = ConnectionsManager.DEFAULT_DATACENTER_ID;
            }
            NotificationsController.updateServerNotificationsSettings(this.dialog_id);
            NotificationsController.getInstance().removeNotificationsForDialog(this.dialog_id);
        } else {
            showDialog(AlertsCreator.createMuteAlert(getParentActivity(), this.dialog_id));
        }
    }

    private void scrollToMessageId(int id, int fromMessageId, boolean select, int loadIndex) {
        MessageObject object = (MessageObject) this.messagesDict[loadIndex].get(Integer.valueOf(id));
        boolean query = false;
        if (object == null) {
            query = true;
        } else if (this.messages.indexOf(object) != -1) {
            if (select) {
                this.highlightMessageId = id;
            } else {
                this.highlightMessageId = ConnectionsManager.DEFAULT_DATACENTER_ID;
            }
            int yOffset = Math.max(0, (this.chatListView.getHeight() - object.getApproximateHeight()) / 2);
            if (this.messages.get(this.messages.size() - 1) == object) {
                this.chatLayoutManager.scrollToPositionWithOffset(0, ((-this.chatListView.getPaddingTop()) - AndroidUtilities.dp(7.0f)) + yOffset);
            } else {
                this.chatLayoutManager.scrollToPositionWithOffset(((this.chatAdapter.messagesStartRow + this.messages.size()) - this.messages.indexOf(object)) - 1, ((-this.chatListView.getPaddingTop()) - AndroidUtilities.dp(7.0f)) + yOffset);
            }
            updateVisibleRows();
            boolean found = false;
            int count = this.chatListView.getChildCount();
            for (int a = 0; a < count; a++) {
                View view = this.chatListView.getChildAt(a);
                if (view instanceof ChatBaseCell) {
                    ChatBaseCell cell = (ChatBaseCell) view;
                    if (cell.getMessageObject() != null && cell.getMessageObject().getId() == object.getId()) {
                        found = true;
                        break;
                    }
                } else if (view instanceof ChatActionCell) {
                    ChatActionCell cell2 = (ChatActionCell) view;
                    if (cell2.getMessageObject() != null && cell2.getMessageObject().getId() == object.getId()) {
                        found = true;
                        break;
                    }
                } else {
                    continue;
                }
            }
            if (!found) {
                showPagedownButton(true, true);
            }
        } else {
            query = true;
        }
        if (query) {
            clearChatData();
            this.loadsCount = 0;
            this.unread_to_load = 0;
            this.first_unread_id = 0;
            this.loadingForward = false;
            this.unreadMessageObject = null;
            this.scrollToMessage = null;
            this.highlightMessageId = ConnectionsManager.DEFAULT_DATACENTER_ID;
            this.scrollToMessagePosition = -10000;
            this.startLoadFromMessageId = id;
            this.waitingForLoad.add(Integer.valueOf(this.lastLoadIndex));
            MessagesController instance = MessagesController.getInstance();
            long j = loadIndex == 0 ? this.dialog_id : this.mergeDialogId;
            int i = AndroidUtilities.isTablet() ? 30 : 20;
            int i2 = this.startLoadFromMessageId;
            int i3 = this.classGuid;
            int i4 = loadIndex == 0 ? this.channelMessagesImportant : 0;
            int i5 = this.lastLoadIndex;
            this.lastLoadIndex = i5 + 1;
            instance.loadMessages(j, i, i2, true, 0, i3, 3, 0, i4, i5);
            this.emptyViewContainer.setVisibility(4);
        }
        this.returnToMessageId = fromMessageId;
        this.needSelectFromMessageId = select;
    }

    private void showPagedownButton(boolean show, boolean animated) {
        if (this.pagedownButton != null) {
            if (!show) {
                this.returnToMessageId = 0;
                if (this.pagedownButton.getTag() != null) {
                    this.pagedownButton.setTag(null);
                    if (this.pagedownButtonAnimation != null) {
                        this.pagedownButtonAnimation.cancel();
                        this.pagedownButtonAnimation = null;
                    }
                    if (animated) {
                        this.pagedownButtonAnimation = ObjectAnimatorProxy.ofFloatProxy(this.pagedownButton, "translationY", (float) AndroidUtilities.dp(100.0f)).setDuration(200).addListener(new AnimatorListenerAdapterProxy() {
                            public void onAnimationEnd(Object animation) {
                                ChatActivity.this.pagedownButton.clearAnimation();
                                ChatActivity.this.pagedownButton.setVisibility(4);
                            }
                        }).start();
                        return;
                    }
                    this.pagedownButton.clearAnimation();
                    this.pagedownButton.setVisibility(4);
                }
            } else if (this.pagedownButton.getTag() == null) {
                if (this.pagedownButtonAnimation != null) {
                    this.pagedownButtonAnimation.cancel();
                    this.pagedownButtonAnimation = null;
                }
                if (animated) {
                    if (ViewProxy.getTranslationY(this.pagedownButton) == 0.0f) {
                        ViewProxy.setTranslationY(this.pagedownButton, (float) AndroidUtilities.dp(100.0f));
                    }
                    this.pagedownButton.setVisibility(0);
                    this.pagedownButton.setTag(Integer.valueOf(1));
                    this.pagedownButtonAnimation = ObjectAnimatorProxy.ofFloatProxy(this.pagedownButton, "translationY", 0.0f).setDuration(200).start();
                    return;
                }
                this.pagedownButton.setVisibility(0);
            }
        }
    }

    private void updateSecretStatus() {
        if (this.bottomOverlay != null) {
            if (this.currentEncryptedChat == null || this.secretViewStatusTextView == null) {
                this.bottomOverlay.setVisibility(4);
                return;
            }
            boolean hideKeyboard = false;
            if (this.currentEncryptedChat instanceof TL_encryptedChatRequested) {
                this.bottomOverlayText.setText(LocaleController.getString("EncryptionProcessing", C0553R.string.EncryptionProcessing));
                this.bottomOverlay.setVisibility(0);
                hideKeyboard = true;
            } else if (this.currentEncryptedChat instanceof TL_encryptedChatWaiting) {
                this.bottomOverlayText.setText(AndroidUtilities.replaceTags(LocaleController.formatString("AwaitingEncryption", C0553R.string.AwaitingEncryption, "<b>" + this.currentUser.first_name + "</b>")));
                this.bottomOverlay.setVisibility(0);
                hideKeyboard = true;
            } else if (this.currentEncryptedChat instanceof TL_encryptedChatDiscarded) {
                this.bottomOverlayText.setText(LocaleController.getString("EncryptionRejected", C0553R.string.EncryptionRejected));
                this.bottomOverlay.setVisibility(0);
                this.chatActivityEnterView.setFieldText("");
                ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0).edit().remove("dialog_" + this.dialog_id).commit();
                hideKeyboard = true;
            } else if (this.currentEncryptedChat instanceof TL_encryptedChat) {
                this.bottomOverlay.setVisibility(4);
            }
            if (hideKeyboard) {
                this.chatActivityEnterView.hidePopup(false);
                if (getParentActivity() != null) {
                    AndroidUtilities.hideKeyboard(getParentActivity().getCurrentFocus());
                }
            }
            checkActionBarMenu();
        }
    }

    private void checkActionBarMenu() {
        if ((this.currentEncryptedChat == null || (this.currentEncryptedChat instanceof TL_encryptedChat)) && ((this.currentChat == null || !ChatObject.isNotInChat(this.currentChat)) && (this.currentUser == null || !UserObject.isDeleted(this.currentUser)))) {
            if (this.menuItem != null) {
                this.menuItem.setVisibility(0);
            }
            if (this.timeItem != null) {
                this.timeItem.setVisibility(0);
            }
            if (this.timeItem2 != null) {
                this.timeItem2.setVisibility(0);
            }
        } else {
            if (this.menuItem != null) {
                this.menuItem.setVisibility(8);
            }
            if (this.timeItem != null) {
                this.timeItem.setVisibility(8);
            }
            if (this.timeItem2 != null) {
                this.timeItem2.setVisibility(8);
            }
        }
        if (!(this.timerDrawable == null || this.currentEncryptedChat == null)) {
            this.timerDrawable.setTime(this.currentEncryptedChat.ttl);
        }
        checkAndUpdateAvatar();
    }

    private int updateOnlineCount() {
        this.onlineCount = 0;
        if (!(this.info instanceof TL_chatFull)) {
            return 0;
        }
        int currentTime = ConnectionsManager.getInstance().getCurrentTime();
        for (int a = 0; a < this.info.participants.participants.size(); a++) {
            User user = MessagesController.getInstance().getUser(Integer.valueOf(((ChatParticipant) this.info.participants.participants.get(a)).user_id));
            if (!(user == null || user.status == null || ((user.status.expires <= currentTime && user.id != UserConfig.getClientUserId()) || user.status.expires <= 10000))) {
                this.onlineCount++;
            }
        }
        return this.onlineCount;
    }

    private int getMessageType(MessageObject messageObject) {
        if (messageObject == null) {
            return -1;
        }
        InputStickerSet inputStickerSet;
        boolean canSave;
        String mime;
        if (this.currentEncryptedChat == null) {
            boolean isBroadcastError;
            if (this.isBroadcast && messageObject.getId() <= 0 && messageObject.isSendError()) {
                isBroadcastError = true;
            } else {
                isBroadcastError = false;
            }
            if ((this.isBroadcast || messageObject.getId() > 0 || !messageObject.isOut()) && !isBroadcastError) {
                if (messageObject.type == 6) {
                    return -1;
                }
                if (messageObject.type == 10 || messageObject.type == 11) {
                    if (messageObject.getId() == 0) {
                        return -1;
                    }
                    return 1;
                } else if (messageObject.isMediaEmpty()) {
                    return 3;
                } else {
                    if ((messageObject.messageOwner.media instanceof TL_messageMediaVideo) || (messageObject.messageOwner.media instanceof TL_messageMediaPhoto) || (messageObject.messageOwner.media instanceof TL_messageMediaDocument)) {
                        if (messageObject.isSticker()) {
                            inputStickerSet = messageObject.getInputStickerSet();
                            if (!(inputStickerSet == null || StickersQuery.isStickerPackInstalled(inputStickerSet.id))) {
                                return 7;
                            }
                        }
                        canSave = false;
                        if (!(messageObject.messageOwner.attachPath == null || messageObject.messageOwner.attachPath.length() == 0 || !new File(messageObject.messageOwner.attachPath).exists())) {
                            canSave = true;
                        }
                        if (!canSave && FileLoader.getPathToMessage(messageObject.messageOwner).exists()) {
                            canSave = true;
                        }
                        if (canSave) {
                            if (messageObject.messageOwner.media instanceof TL_messageMediaDocument) {
                                mime = messageObject.messageOwner.media.document.mime_type;
                                if (mime != null) {
                                    if (mime.endsWith("/xml")) {
                                        return 5;
                                    }
                                    if (mime.endsWith("/png") || mime.endsWith("/jpg") || mime.endsWith("/jpeg")) {
                                        return 6;
                                    }
                                }
                            }
                            return 4;
                        }
                    }
                    return 2;
                }
            } else if (!messageObject.isSendError()) {
                return -1;
            } else {
                if (messageObject.isMediaEmpty()) {
                    return 20;
                }
                return 0;
            }
        } else if (messageObject.isSending()) {
            return -1;
        } else {
            if (messageObject.type == 6) {
                return -1;
            }
            if (messageObject.isSendError()) {
                if (messageObject.isMediaEmpty()) {
                    return 20;
                }
                return 0;
            } else if (messageObject.type == 10 || messageObject.type == 11) {
                if (messageObject.getId() == 0 || messageObject.isSending()) {
                    return -1;
                }
                return 1;
            } else if (messageObject.isMediaEmpty()) {
                return 3;
            } else {
                if ((messageObject.messageOwner.media instanceof TL_messageMediaVideo) || (messageObject.messageOwner.media instanceof TL_messageMediaPhoto) || (messageObject.messageOwner.media instanceof TL_messageMediaDocument)) {
                    if (messageObject.isSticker()) {
                        inputStickerSet = messageObject.getInputStickerSet();
                        if (!(inputStickerSet == null || StickersQuery.isStickerPackInstalled(inputStickerSet.id))) {
                            return 7;
                        }
                    }
                    canSave = false;
                    if (!(messageObject.messageOwner.attachPath == null || messageObject.messageOwner.attachPath.length() == 0 || !new File(messageObject.messageOwner.attachPath).exists())) {
                        canSave = true;
                    }
                    if (!canSave && FileLoader.getPathToMessage(messageObject.messageOwner).exists()) {
                        canSave = true;
                    }
                    if (canSave) {
                        if (messageObject.messageOwner.media instanceof TL_messageMediaDocument) {
                            mime = messageObject.messageOwner.media.document.mime_type;
                            if (mime != null && mime.endsWith("text/xml")) {
                                return 5;
                            }
                        }
                        if (messageObject.messageOwner.ttl <= 0) {
                            return 4;
                        }
                    }
                }
                return 2;
            }
        }
    }

    private void addToSelectedMessages(MessageObject messageObject) {
        int index = messageObject.getDialogId() == this.dialog_id ? 0 : 1;
        if (this.selectedMessagesIds[index].containsKey(Integer.valueOf(messageObject.getId()))) {
            this.selectedMessagesIds[index].remove(Integer.valueOf(messageObject.getId()));
            if (messageObject.type == 0) {
                this.selectedMessagesCanCopyIds[index].remove(Integer.valueOf(messageObject.getId()));
            }
            if (!messageObject.canDeleteMessage(this.currentChat)) {
                this.cantDeleteMessagesCount--;
            }
        } else {
            this.selectedMessagesIds[index].put(Integer.valueOf(messageObject.getId()), messageObject);
            if (messageObject.type == 0) {
                this.selectedMessagesCanCopyIds[index].put(Integer.valueOf(messageObject.getId()), messageObject);
            }
            if (!messageObject.canDeleteMessage(this.currentChat)) {
                this.cantDeleteMessagesCount++;
            }
        }
        if (!this.actionBar.isActionModeShowed()) {
            return;
        }
        if (this.selectedMessagesIds[0].isEmpty() && this.selectedMessagesIds[1].isEmpty()) {
            this.actionBar.hideActionMode();
            return;
        }
        int copyVisible = this.actionBar.createActionMode().getItem(10).getVisibility();
        this.actionBar.createActionMode().getItem(10).setVisibility(this.selectedMessagesCanCopyIds[0].size() + this.selectedMessagesCanCopyIds[1].size() != 0 ? 0 : 8);
        int newCopyVisible = this.actionBar.createActionMode().getItem(10).getVisibility();
        this.actionBar.createActionMode().getItem(12).setVisibility(this.cantDeleteMessagesCount == 0 ? 0 : 8);
        final ActionBarMenuItem replyItem = this.actionBar.createActionMode().getItem(19);
        if (replyItem != null) {
            boolean allowChatActions = true;
            if (this.isBroadcast || (this.currentChat != null && (ChatObject.isNotInChat(this.currentChat) || !(!ChatObject.isChannel(this.currentChat) || this.currentChat.creator || this.currentChat.editor || this.currentChat.megagroup)))) {
                allowChatActions = false;
            }
            final int newVisibility = (allowChatActions && this.selectedMessagesIds[0].size() + this.selectedMessagesIds[1].size() == 1) ? 0 : 8;
            if (replyItem.getVisibility() != newVisibility) {
                if (this.replyButtonAnimation != null) {
                    this.replyButtonAnimation.cancel();
                    this.replyButtonAnimation = null;
                }
                if (copyVisible != newCopyVisible) {
                    if (newVisibility == 0) {
                        ViewProxy.setAlpha(replyItem, 1.0f);
                        ViewProxy.setScaleX(replyItem, 1.0f);
                    } else {
                        ViewProxy.setAlpha(replyItem, 0.0f);
                        ViewProxy.setScaleX(replyItem, 0.0f);
                    }
                    replyItem.setVisibility(newVisibility);
                    replyItem.clearAnimation();
                    return;
                }
                this.replyButtonAnimation = new AnimatorSetProxy();
                ViewProxy.setPivotX(replyItem, (float) AndroidUtilities.dp(54.0f));
                AnimatorSetProxy animatorSetProxy;
                Object[] objArr;
                if (newVisibility == 0) {
                    replyItem.setVisibility(newVisibility);
                    animatorSetProxy = this.replyButtonAnimation;
                    objArr = new Object[2];
                    objArr[0] = ObjectAnimatorProxy.ofFloat(replyItem, "alpha", 1.0f);
                    objArr[1] = ObjectAnimatorProxy.ofFloat(replyItem, "scaleX", 1.0f);
                    animatorSetProxy.playTogether(objArr);
                } else {
                    animatorSetProxy = this.replyButtonAnimation;
                    objArr = new Object[2];
                    objArr[0] = ObjectAnimatorProxy.ofFloat(replyItem, "alpha", 0.0f);
                    objArr[1] = ObjectAnimatorProxy.ofFloat(replyItem, "scaleX", 0.0f);
                    animatorSetProxy.playTogether(objArr);
                }
                this.replyButtonAnimation.setDuration(100);
                this.replyButtonAnimation.addListener(new AnimatorListenerAdapterProxy() {
                    public void onAnimationEnd(Object animation) {
                        if (ChatActivity.this.replyButtonAnimation.equals(animation)) {
                            replyItem.clearAnimation();
                            if (newVisibility == 8) {
                                replyItem.setVisibility(8);
                            }
                        }
                    }
                });
                this.replyButtonAnimation.start();
            }
        }
    }

    private void processRowSelect(View view) {
        MessageObject message = null;
        if (view instanceof ChatBaseCell) {
            message = ((ChatBaseCell) view).getMessageObject();
        } else if (view instanceof ChatActionCell) {
            message = ((ChatActionCell) view).getMessageObject();
        }
        int type = getMessageType(message);
        if (type >= 2 && type != 20) {
            addToSelectedMessages(message);
            updateActionModeTitle();
            updateVisibleRows();
        }
    }

    private void updateActionModeTitle() {
        if (!this.actionBar.isActionModeShowed()) {
            return;
        }
        if (!this.selectedMessagesIds[0].isEmpty() || !this.selectedMessagesIds[1].isEmpty()) {
            this.selectedMessagesCountTextView.setNumber(this.selectedMessagesIds[0].size() + this.selectedMessagesIds[1].size(), true);
        }
    }

    private void updateTitle() {
        if (this.nameTextView != null) {
            if (this.currentChat != null) {
                this.nameTextView.setText(this.currentChat.title);
            } else if (this.currentUser == null) {
            } else {
                if (this.currentUser.id / 1000 == 777 || this.currentUser.id / 1000 == 333 || ContactsController.getInstance().contactsDict.get(this.currentUser.id) != null || (ContactsController.getInstance().contactsDict.size() == 0 && ContactsController.getInstance().isLoadingContacts())) {
                    this.nameTextView.setText(UserObject.getUserName(this.currentUser));
                } else if (this.currentUser.phone == null || this.currentUser.phone.length() == 0) {
                    this.nameTextView.setText(UserObject.getUserName(this.currentUser));
                } else {
                    this.nameTextView.setText(PhoneFormat.getInstance().format("+" + this.currentUser.phone));
                }
            }
        }
    }

    private void updateBotButtons() {
        if (this.headerItem != null && this.currentUser != null && this.currentEncryptedChat == null && this.currentUser.bot) {
            boolean hasHelp = false;
            boolean hasSettings = false;
            if (!this.botInfo.isEmpty()) {
                for (Entry<Integer, BotInfo> entry : this.botInfo.entrySet()) {
                    BotInfo info = (BotInfo) entry.getValue();
                    for (int a = 0; a < info.commands.size(); a++) {
                        TL_botCommand command = (TL_botCommand) info.commands.get(a);
                        if (command.command.toLowerCase().equals("help")) {
                            hasHelp = true;
                        } else if (command.command.toLowerCase().equals("settings")) {
                            hasSettings = true;
                        }
                        if (hasSettings && hasHelp) {
                            break;
                        }
                    }
                }
            }
            if (hasHelp) {
                this.headerItem.showSubItem(30);
            } else {
                this.headerItem.hideSubItem(30);
            }
            if (hasSettings) {
                this.headerItem.showSubItem(31);
            } else {
                this.headerItem.hideSubItem(31);
            }
        }
    }

    private void updateTitleIcons() {
        if (this.nameTextView != null) {
            int leftIcon;
            int rightIcon;
            if (this.currentEncryptedChat != null) {
                leftIcon = C0553R.drawable.ic_lock_header;
            } else {
                leftIcon = 0;
            }
            if (MessagesController.getInstance().isDialogMuted(this.dialog_id)) {
                rightIcon = C0553R.drawable.mute_fixed;
            } else {
                rightIcon = 0;
            }
            this.nameTextView.setCompoundDrawablesWithIntrinsicBounds(leftIcon, 0, rightIcon, 0);
            if (rightIcon != 0) {
                this.muteItem.setText(LocaleController.getString("UnmuteNotifications", C0553R.string.UnmuteNotifications));
            } else {
                this.muteItem.setText(LocaleController.getString("MuteNotifications", C0553R.string.MuteNotifications));
            }
        }
    }

    private void updateSubtitle() {
        if (this.onlineTextView != null) {
            CharSequence printString = (CharSequence) MessagesController.getInstance().printingStrings.get(Long.valueOf(this.dialog_id));
            if (printString != null) {
                printString = TextUtils.replace(printString, new String[]{"..."}, new String[]{""});
            }
            if (printString == null || printString.length() == 0 || (ChatObject.isChannel(this.currentChat) && !this.currentChat.megagroup)) {
                setTypingAnimation(false);
                if (this.currentChat != null) {
                    if (ChatObject.isChannel(this.currentChat)) {
                        if (this.currentChat.broadcast || this.currentChat.megagroup || (this.currentChat instanceof TL_channelForbidden)) {
                            if (this.info != null && this.info.participants_count != 0) {
                                int[] result = new int[1];
                                String shortNumber = LocaleController.formatShortNumber(this.info.participants_count, result);
                                this.onlineTextView.setText(LocaleController.formatPluralString("Members", result[0]).replace(String.format("%d", new Object[]{Integer.valueOf(result[0])}), shortNumber));
                            } else if (this.currentChat.megagroup) {
                                this.onlineTextView.setText(LocaleController.getString("Loading", C0553R.string.Loading).toLowerCase());
                            } else if ((this.currentChat.flags & 64) != 0) {
                                this.onlineTextView.setText(LocaleController.getString("ChannelPublic", C0553R.string.ChannelPublic).toLowerCase());
                            } else {
                                this.onlineTextView.setText(LocaleController.getString("ChannelPrivate", C0553R.string.ChannelPrivate).toLowerCase());
                            }
                            if (!(this.radioButton == null || this.radioButton.getVisibility() == 8)) {
                                this.radioButton.setVisibility(8);
                                this.onlineTextView.setLayoutParams(LayoutHelper.createFrame(-2, -2.0f, 83, 54.0f, 0.0f, 0.0f, 4.0f));
                            }
                        } else {
                            this.onlineTextView.setText(LocaleController.getString("ShowDiscussion", C0553R.string.ShowDiscussion));
                            if (!(this.radioButton == null || this.radioButton.getVisibility() == 0)) {
                                this.radioButton.setVisibility(0);
                                this.onlineTextView.setLayoutParams(LayoutHelper.createFrame(-2, -2.0f, 83, 74.0f, 0.0f, 0.0f, 4.0f));
                            }
                        }
                    } else if (ChatObject.isKickedFromChat(this.currentChat)) {
                        this.onlineTextView.setText(LocaleController.getString("YouWereKicked", C0553R.string.YouWereKicked));
                    } else if (ChatObject.isLeftFromChat(this.currentChat)) {
                        this.onlineTextView.setText(LocaleController.getString("YouLeft", C0553R.string.YouLeft));
                    } else {
                        int count = this.currentChat.participants_count;
                        if (this.info != null) {
                            count = this.info.participants.participants.size();
                        }
                        if (this.onlineCount <= 1 || count == 0) {
                            this.onlineTextView.setText(LocaleController.formatPluralString("Members", count));
                        } else {
                            this.onlineTextView.setText(String.format("%s, %s", new Object[]{LocaleController.formatPluralString("Members", count), LocaleController.formatPluralString("Online", this.onlineCount)}));
                        }
                    }
                } else if (this.currentUser != null) {
                    String newStatus;
                    User user = MessagesController.getInstance().getUser(Integer.valueOf(this.currentUser.id));
                    if (user != null) {
                        this.currentUser = user;
                    }
                    if (this.currentUser.id == 333000 || this.currentUser.id == 777000) {
                        newStatus = LocaleController.getString("ServiceNotifications", C0553R.string.ServiceNotifications);
                    } else if (this.currentUser.bot) {
                        newStatus = LocaleController.getString("Bot", C0553R.string.Bot);
                    } else {
                        newStatus = LocaleController.formatUserStatus(this.currentUser);
                    }
                    if (!(this.lastStatus != null && this.lastPrintString == null && this.lastStatus.equals(newStatus))) {
                        this.lastStatus = newStatus;
                        this.onlineTextView.setText(newStatus);
                    }
                }
                this.lastPrintString = null;
                return;
            }
            this.lastPrintString = printString;
            this.onlineTextView.setText(printString);
            setTypingAnimation(true);
        }
    }

    private void setTypingAnimation(boolean start) {
        if (this.actionBar != null) {
            if (start) {
                try {
                    Integer type = (Integer) MessagesController.getInstance().printingStringsTypes.get(Long.valueOf(this.dialog_id));
                    if (type.intValue() == 0) {
                        if (this.lastStatusDrawable != 1) {
                            this.lastStatusDrawable = 1;
                            if (this.onlineTextView != null) {
                                this.onlineTextView.setCompoundDrawablesWithIntrinsicBounds(this.typingDotsDrawable, null, null, null);
                                this.onlineTextView.setCompoundDrawablePadding(AndroidUtilities.dp(4.0f));
                                this.typingDotsDrawable.start();
                                this.recordStatusDrawable.stop();
                                this.sendingFileDrawable.stop();
                            }
                        }
                    } else if (type.intValue() == 1) {
                        if (this.lastStatusDrawable != 2) {
                            this.lastStatusDrawable = 2;
                            if (this.onlineTextView != null) {
                                this.onlineTextView.setCompoundDrawablesWithIntrinsicBounds(this.recordStatusDrawable, null, null, null);
                                this.onlineTextView.setCompoundDrawablePadding(AndroidUtilities.dp(4.0f));
                                this.recordStatusDrawable.start();
                                this.typingDotsDrawable.stop();
                                this.sendingFileDrawable.stop();
                            }
                        }
                    } else if (type.intValue() == 2 && this.lastStatusDrawable != 3) {
                        this.lastStatusDrawable = 3;
                        if (this.onlineTextView != null) {
                            this.onlineTextView.setCompoundDrawablesWithIntrinsicBounds(this.sendingFileDrawable, null, null, null);
                            this.onlineTextView.setCompoundDrawablePadding(AndroidUtilities.dp(4.0f));
                            this.sendingFileDrawable.start();
                            this.typingDotsDrawable.stop();
                            this.recordStatusDrawable.stop();
                        }
                    }
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            } else if (this.lastStatusDrawable != 0) {
                this.lastStatusDrawable = 0;
                if (this.onlineTextView != null) {
                    this.onlineTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                    this.onlineTextView.setCompoundDrawablePadding(0);
                    this.typingDotsDrawable.stop();
                    this.recordStatusDrawable.stop();
                    this.sendingFileDrawable.stop();
                }
            }
        }
    }

    private void checkAndUpdateAvatar() {
        TLObject newPhoto = null;
        Drawable avatarDrawable = null;
        if (this.currentUser != null) {
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
        } else if (this.currentChat != null) {
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
        }
        if (this.avatarImageView != null) {
            this.avatarImageView.setImage(newPhoto, "50_50", avatarDrawable);
        }
    }

    public boolean openVideoEditor(String videoPath, boolean removeLast, boolean animated) {
        Bundle args = new Bundle();
        args.putString("videoPath", videoPath);
        BaseFragment videoEditorActivity = new VideoEditorActivity(args);
        videoEditorActivity.setDelegate(new VideoEditorActivityDelegate() {
            public void didFinishEditVideo(String videoPath, long startTime, long endTime, int resultWidth, int resultHeight, int rotationValue, int originalWidth, int originalHeight, int bitrate, long estimatedSize, long estimatedDuration) {
                VideoEditedInfo videoEditedInfo = new VideoEditedInfo();
                videoEditedInfo.startTime = startTime;
                videoEditedInfo.endTime = endTime;
                videoEditedInfo.rotationValue = rotationValue;
                videoEditedInfo.originalWidth = originalWidth;
                videoEditedInfo.originalHeight = originalHeight;
                videoEditedInfo.bitrate = bitrate;
                videoEditedInfo.resultWidth = resultWidth;
                videoEditedInfo.resultHeight = resultHeight;
                videoEditedInfo.originalPath = videoPath;
                long access$1200 = ChatActivity.this.dialog_id;
                MessageObject access$1300 = ChatActivity.this.replyingMessageObject;
                boolean z = ChatActivity.this.chatActivityEnterView == null || ChatActivity.this.chatActivityEnterView.asAdmin();
                SendMessagesHelper.prepareSendingVideo(videoPath, estimatedSize, estimatedDuration, resultWidth, resultHeight, videoEditedInfo, access$1200, access$1300, z);
                ChatActivity.this.showReplyPanel(false, null, null, null, false, true);
            }
        });
        if (this.parentLayout == null || !videoEditorActivity.onFragmentCreate()) {
            long j = this.dialog_id;
            MessageObject messageObject = this.replyingMessageObject;
            boolean z = this.chatActivityEnterView == null || this.chatActivityEnterView.asAdmin();
            SendMessagesHelper.prepareSendingVideo(videoPath, 0, 0, 0, 0, null, j, messageObject, z);
            showReplyPanel(false, null, null, null, false, true);
            return false;
        }
        this.parentLayout.presentFragment(videoEditorActivity, removeLast, !animated, true);
        return true;
    }

    private void showAttachmentError() {
        if (getParentActivity() != null) {
            Toast.makeText(getParentActivity(), LocaleController.getString("UnsupportedAttachment", C0553R.string.UnsupportedAttachment), 0).show();
        }
    }

    public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {
        if (resultCode != -1) {
            return;
        }
        if (requestCode == 0) {
            PhotoViewer.getInstance().setParentActivity(getParentActivity());
            ArrayList<Object> arrayList = new ArrayList();
            int orientation = 0;
            try {
                switch (new ExifInterface(this.currentPicturePath).getAttributeInt("Orientation", 1)) {
                    case 3:
                        orientation = 180;
                        break;
                    case 6:
                        orientation = 90;
                        break;
                    case 8:
                        orientation = 270;
                        break;
                }
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
            arrayList.add(new PhotoEntry(0, 0, 0, this.currentPicturePath, orientation, false));
            final ArrayList<Object> arrayList2 = arrayList;
            PhotoViewer.getInstance().openPhotoForSelect(arrayList, 0, 2, new EmptyPhotoViewerProvider() {
                public void sendButtonPressed(int index) {
                    PhotoEntry photoEntry = (PhotoEntry) arrayList2.get(0);
                    String str;
                    long access$1200;
                    MessageObject access$1300;
                    CharSequence charSequence;
                    boolean z;
                    if (photoEntry.imagePath != null) {
                        str = photoEntry.imagePath;
                        access$1200 = ChatActivity.this.dialog_id;
                        access$1300 = ChatActivity.this.replyingMessageObject;
                        charSequence = photoEntry.caption;
                        z = ChatActivity.this.chatActivityEnterView == null || ChatActivity.this.chatActivityEnterView.asAdmin();
                        SendMessagesHelper.prepareSendingPhoto(str, null, access$1200, access$1300, charSequence, z);
                        ChatActivity.this.showReplyPanel(false, null, null, null, false, true);
                    } else if (photoEntry.path != null) {
                        str = photoEntry.path;
                        access$1200 = ChatActivity.this.dialog_id;
                        access$1300 = ChatActivity.this.replyingMessageObject;
                        charSequence = photoEntry.caption;
                        if (ChatActivity.this.chatActivityEnterView == null || ChatActivity.this.chatActivityEnterView.asAdmin()) {
                            z = true;
                        } else {
                            z = false;
                        }
                        SendMessagesHelper.prepareSendingPhoto(str, null, access$1200, access$1300, charSequence, z);
                        ChatActivity.this.showReplyPanel(false, null, null, null, false, true);
                    }
                }
            }, this);
            AndroidUtilities.addMediaToGallery(this.currentPicturePath);
            this.currentPicturePath = null;
        } else if (requestCode == 1) {
            if (data == null || data.getData() == null) {
                showAttachmentError();
                return;
            }
            uri = data.getData();
            if (uri.toString().contains("video")) {
                videoPath = null;
                try {
                    videoPath = AndroidUtilities.getPath(uri);
                } catch (Throwable e2) {
                    FileLog.m611e("tmessages", e2);
                }
                if (videoPath == null) {
                    showAttachmentError();
                }
                if (VERSION.SDK_INT < 16) {
                    r19 = this.dialog_id;
                    r21 = this.replyingMessageObject;
                    r22 = this.chatActivityEnterView == null || this.chatActivityEnterView.asAdmin();
                    SendMessagesHelper.prepareSendingVideo(videoPath, 0, 0, 0, 0, null, r19, r21, r22);
                    showReplyPanel(null, null, null, null, false, true);
                } else if (this.paused) {
                    this.startVideoEdit = videoPath;
                } else {
                    openVideoEditor(videoPath, false, false);
                }
            } else {
                long j = this.dialog_id;
                MessageObject messageObject = this.replyingMessageObject;
                boolean z = this.chatActivityEnterView == null || this.chatActivityEnterView.asAdmin();
                SendMessagesHelper.prepareSendingPhoto(null, uri, j, messageObject, null, z);
            }
            showReplyPanel(false, null, null, null, false, true);
        } else if (requestCode == 2) {
            videoPath = null;
            if (data != null) {
                uri = data.getData();
                if (uri != null) {
                    videoPath = uri.getPath();
                } else {
                    videoPath = this.currentPicturePath;
                }
                AndroidUtilities.addMediaToGallery(this.currentPicturePath);
                this.currentPicturePath = null;
            }
            if (videoPath == null && this.currentPicturePath != null) {
                if (new File(this.currentPicturePath).exists()) {
                    videoPath = this.currentPicturePath;
                }
                this.currentPicturePath = null;
            }
            if (VERSION.SDK_INT < 16) {
                r19 = this.dialog_id;
                r21 = this.replyingMessageObject;
                r22 = this.chatActivityEnterView == null || this.chatActivityEnterView.asAdmin();
                SendMessagesHelper.prepareSendingVideo(videoPath, 0, 0, 0, 0, null, r19, r21, r22);
                showReplyPanel(false, null, null, null, false, true);
            } else if (this.paused) {
                this.startVideoEdit = videoPath;
            } else {
                openVideoEditor(videoPath, false, false);
            }
        } else if (requestCode == 21) {
            if (data == null || data.getData() == null) {
                showAttachmentError();
                return;
            }
            uri = data.getData();
            String extractUriFrom = uri.toString();
            if (extractUriFrom.contains("com.google.android.apps.photos.contentprovider")) {
                try {
                    String firstExtraction = extractUriFrom.split("/1/")[1];
                    int index = firstExtraction.indexOf("/ACTUAL");
                    if (index != -1) {
                        uri = Uri.parse(URLDecoder.decode(firstExtraction.substring(0, index), HttpURLConnectionBuilder.DEFAULT_CHARSET));
                    }
                } catch (Throwable e22) {
                    FileLog.m611e("tmessages", e22);
                }
            }
            String tempPath = AndroidUtilities.getPath(uri);
            String originalPath = tempPath;
            if (tempPath == null) {
                originalPath = data.toString();
                tempPath = MediaController.copyDocumentToCache(data.getData(), "file");
            }
            if (tempPath == null) {
                showAttachmentError();
                return;
            }
            r16 = this.dialog_id;
            r18 = this.replyingMessageObject;
            r19 = this.chatActivityEnterView == null || this.chatActivityEnterView.asAdmin();
            SendMessagesHelper.prepareSendingDocument(tempPath, originalPath, null, null, r16, r18, r19);
            showReplyPanel(false, null, null, null, false, true);
        } else if (requestCode != 31) {
        } else {
            if (data == null || data.getData() == null) {
                showAttachmentError();
                return;
            }
            Cursor c = null;
            try {
                c = getParentActivity().getContentResolver().query(data.getData(), new String[]{"display_name", "data1"}, null, null, null);
                if (c != null) {
                    boolean sent = false;
                    while (c.moveToNext()) {
                        sent = true;
                        String name = c.getString(0);
                        String number = c.getString(1);
                        User user = new User();
                        user.first_name = name;
                        user.last_name = "";
                        user.phone = number;
                        SendMessagesHelper instance = SendMessagesHelper.getInstance();
                        r16 = this.dialog_id;
                        r18 = this.replyingMessageObject;
                        r19 = this.chatActivityEnterView == null || this.chatActivityEnterView.asAdmin();
                        instance.sendMessage(user, r16, r18, r19);
                    }
                    if (sent) {
                        showReplyPanel(false, null, null, null, false, true);
                    }
                }
                if (c != null) {
                    try {
                        if (!c.isClosed()) {
                            c.close();
                        }
                    } catch (Throwable e222) {
                        FileLog.m611e("tmessages", e222);
                    }
                }
            } catch (Throwable th) {
                if (c != null) {
                    try {
                        if (!c.isClosed()) {
                            c.close();
                        }
                    } catch (Throwable e2222) {
                        FileLog.m611e("tmessages", e2222);
                    }
                }
            }
        }
    }

    public void saveSelfArgs(Bundle args) {
        if (this.currentPicturePath != null) {
            args.putString("path", this.currentPicturePath);
        }
    }

    public void restoreSelfArgs(Bundle args) {
        this.currentPicturePath = args.getString("path");
    }

    private void removeUnreadPlane() {
        if (this.unreadMessageObject != null) {
            boolean[] zArr = this.forwardEndReached;
            this.forwardEndReached[1] = true;
            zArr[0] = true;
            this.first_unread_id = 0;
            this.last_message_id = 0;
            this.unread_to_load = 0;
            if (this.chatAdapter != null) {
                this.chatAdapter.removeMessageObject(this.unreadMessageObject);
            } else {
                this.messages.remove(this.unreadMessageObject);
            }
            this.unreadMessageObject = null;
        }
    }

    public boolean processSendingText(String text) {
        return this.chatActivityEnterView.processSendingText(text);
    }

    public void didReceivedNotification(int id, Object... args) {
        int index;
        int loadIndex;
        int count;
        ArrayList<MessageObject> messArr;
        int a;
        MessageObject obj;
        ArrayList<MessageObject> dayArray;
        Message dateMsg;
        MessageObject messageObject;
        if (id == NotificationCenter.messagesDidLoaded) {
            if (((Integer) args[11]).intValue() == this.classGuid) {
                index = this.waitingForLoad.indexOf(Integer.valueOf(((Integer) args[12]).intValue()));
                if (index != -1) {
                    TL_messageGroup group;
                    this.waitingForLoad.remove(index);
                    if (this.waitingForImportantLoad) {
                        int startLoadFrom = this.startLoadFromMessageId;
                        clearChatData();
                        this.startLoadFromMessageId = startLoadFrom;
                    }
                    this.loadsCount++;
                    loadIndex = ((Long) args[0]).longValue() == this.dialog_id ? 0 : 1;
                    count = ((Integer) args[1]).intValue();
                    boolean isCache = ((Boolean) args[3]).booleanValue();
                    int fnid = ((Integer) args[4]).intValue();
                    int last_unread_date = ((Integer) args[7]).intValue();
                    int load_type = ((Integer) args[8]).intValue();
                    boolean wasUnread = false;
                    if (fnid != 0) {
                        this.first_unread_id = fnid;
                        this.last_message_id = ((Integer) args[5]).intValue();
                        this.unread_to_load = ((Integer) args[6]).intValue();
                    } else if (this.startLoadFromMessageId != 0 && load_type == 3) {
                        this.last_message_id = ((Integer) args[5]).intValue();
                    }
                    messArr = args[2];
                    ArrayList<TL_messageGroup> groups = args[9];
                    SparseArray<TL_messageGroup> groupsByStart = null;
                    if (!(groups == null || groups.isEmpty())) {
                        groupsByStart = new SparseArray();
                        for (a = 0; a < groups.size(); a++) {
                            group = (TL_messageGroup) groups.get(a);
                            groupsByStart.put(group.min_id, group);
                        }
                    }
                    int newRowsCount = 0;
                    boolean[] zArr = this.forwardEndReached;
                    boolean z = this.startLoadFromMessageId == 0 && this.last_message_id == 0;
                    zArr[loadIndex] = z;
                    if ((load_type == 1 || load_type == 3) && loadIndex == 1) {
                        boolean[] zArr2 = this.endReached;
                        this.cacheEndReached[0] = true;
                        zArr2[0] = true;
                        this.forwardEndReached[0] = false;
                        this.minMessageId[0] = 0;
                    }
                    if (this.loadsCount == 1 && messArr.size() > 20) {
                        this.loadsCount++;
                    }
                    if (this.firstLoading) {
                        if (!this.forwardEndReached[loadIndex]) {
                            this.messages.clear();
                            this.messagesByDays.clear();
                            for (a = 0; a < 2; a++) {
                                this.messagesDict[a].clear();
                                if (this.currentEncryptedChat == null) {
                                    this.maxMessageId[a] = ConnectionsManager.DEFAULT_DATACENTER_ID;
                                    this.minMessageId[a] = Integer.MIN_VALUE;
                                } else {
                                    this.maxMessageId[a] = Integer.MIN_VALUE;
                                    this.minMessageId[a] = ConnectionsManager.DEFAULT_DATACENTER_ID;
                                }
                                this.maxDate[a] = Integer.MIN_VALUE;
                                this.minDate[a] = 0;
                            }
                        }
                        this.firstLoading = false;
                    }
                    if (load_type == 1) {
                        Collections.reverse(messArr);
                    }
                    ReplyMessageQuery.loadReplyMessagesForMessages(messArr, this.dialog_id);
                    for (a = 0; a < messArr.size(); a++) {
                        obj = (MessageObject) messArr.get(a);
                        if (!this.messagesDict[loadIndex].containsKey(Integer.valueOf(obj.getId()))) {
                            if (loadIndex == 1) {
                                obj.setIsRead();
                            }
                            if (loadIndex == 0 && this.channelMessagesImportant != 0 && obj.getId() == 1) {
                                this.endReached[loadIndex] = true;
                                this.cacheEndReached[loadIndex] = true;
                            }
                            if (obj.getId() > 0) {
                                this.maxMessageId[loadIndex] = Math.min(obj.getId(), this.maxMessageId[loadIndex]);
                                this.minMessageId[loadIndex] = Math.max(obj.getId(), this.minMessageId[loadIndex]);
                            } else if (this.currentEncryptedChat != null) {
                                this.maxMessageId[loadIndex] = Math.max(obj.getId(), this.maxMessageId[loadIndex]);
                                this.minMessageId[loadIndex] = Math.min(obj.getId(), this.minMessageId[loadIndex]);
                            }
                            if (obj.messageOwner.date != 0) {
                                this.maxDate[loadIndex] = Math.max(this.maxDate[loadIndex], obj.messageOwner.date);
                                if (this.minDate[loadIndex] == 0 || obj.messageOwner.date < this.minDate[loadIndex]) {
                                    this.minDate[loadIndex] = obj.messageOwner.date;
                                }
                            }
                            if (obj.type >= 0 && !(loadIndex == 1 && (obj.messageOwner.action instanceof TL_messageActionChatMigrateTo))) {
                                if (!obj.isOut() && obj.isUnread()) {
                                    wasUnread = true;
                                }
                                this.messagesDict[loadIndex].put(Integer.valueOf(obj.getId()), obj);
                                dayArray = (ArrayList) this.messagesByDays.get(obj.dateKey);
                                if (dayArray == null) {
                                    dayArray = new ArrayList();
                                    this.messagesByDays.put(obj.dateKey, dayArray);
                                    dateMsg = new Message();
                                    dateMsg.message = LocaleController.formatDateChat((long) obj.messageOwner.date);
                                    dateMsg.id = 0;
                                    messageObject = new MessageObject(dateMsg, null, false);
                                    messageObject.type = 10;
                                    messageObject.contentType = 4;
                                    if (load_type == 1) {
                                        this.messages.add(0, messageObject);
                                    } else {
                                        this.messages.add(messageObject);
                                    }
                                    newRowsCount++;
                                }
                                newRowsCount++;
                                if (load_type == 1) {
                                    dayArray.add(obj);
                                    this.messages.add(0, obj);
                                }
                                if (groupsByStart != null) {
                                    group = (TL_messageGroup) groupsByStart.get(obj.getId());
                                    if (group != null) {
                                        dateMsg = new Message();
                                        dateMsg.message = "+" + LocaleController.formatPluralString("comments", group.count);
                                        dateMsg.id = 0;
                                        dateMsg.date = group.min_id;
                                        dateMsg.from_id = group.max_id;
                                        messageObject = new MessageObject(dateMsg, null, false);
                                        messageObject.type = 10;
                                        messageObject.contentType = 4;
                                        dayArray.add(messageObject);
                                        if (load_type == 1) {
                                            this.messages.add(0, messageObject);
                                        } else {
                                            this.messages.add(this.messages.size() - 1, messageObject);
                                        }
                                        newRowsCount++;
                                    }
                                }
                                if (load_type != 1) {
                                    dayArray.add(obj);
                                    this.messages.add(this.messages.size() - 1, obj);
                                }
                                if (load_type == 2 && obj.getId() == this.first_unread_id) {
                                    dateMsg = new Message();
                                    dateMsg.message = "";
                                    dateMsg.id = 0;
                                    messageObject = new MessageObject(dateMsg, null, false);
                                    messageObject.type = 6;
                                    messageObject.contentType = 6;
                                    this.messages.add(this.messages.size() - 1, messageObject);
                                    this.unreadMessageObject = messageObject;
                                    this.scrollToMessage = this.unreadMessageObject;
                                    this.scrollToMessagePosition = -10000;
                                    newRowsCount++;
                                } else if (load_type == 3 && obj.getId() == this.startLoadFromMessageId) {
                                    if (this.needSelectFromMessageId) {
                                        this.highlightMessageId = obj.getId();
                                    } else {
                                        this.highlightMessageId = ConnectionsManager.DEFAULT_DATACENTER_ID;
                                    }
                                    this.scrollToMessage = obj;
                                    this.startLoadFromMessageId = 0;
                                    if (this.scrollToMessagePosition == -10000) {
                                        this.scrollToMessagePosition = -9000;
                                    }
                                }
                                if (obj.getId() == this.last_message_id) {
                                    this.forwardEndReached[loadIndex] = true;
                                }
                            }
                        }
                    }
                    if (this.forwardEndReached[loadIndex] && loadIndex != 1) {
                        this.first_unread_id = 0;
                        this.last_message_id = 0;
                    }
                    if (this.loadsCount <= 2 && (this.messages.size() >= 20 || !isCache)) {
                        updateSpamView();
                    }
                    int firstVisPos;
                    int top;
                    View firstVisView;
                    if (load_type == 1) {
                        if (!(messArr.size() == count || isCache)) {
                            this.forwardEndReached[loadIndex] = true;
                            if (loadIndex != 1) {
                                this.first_unread_id = 0;
                                this.last_message_id = 0;
                                this.chatAdapter.notifyItemRemoved(this.chatAdapter.getItemCount() - 1);
                                newRowsCount--;
                            }
                            this.startLoadFromMessageId = 0;
                        }
                        if (newRowsCount != 0) {
                            firstVisPos = this.chatLayoutManager.findLastVisibleItemPosition();
                            top = 0;
                            if (firstVisPos != this.chatLayoutManager.getItemCount() - 1) {
                                firstVisPos = -1;
                            } else {
                                firstVisView = this.chatListView.getChildAt(this.chatListView.getChildCount() - 1);
                                top = (firstVisView == null ? 0 : firstVisView.getTop()) - this.chatListView.getPaddingTop();
                            }
                            this.chatAdapter.notifyItemRangeInserted(this.chatAdapter.getItemCount() - 1, newRowsCount);
                            if (firstVisPos != -1) {
                                this.chatLayoutManager.scrollToPositionWithOffset(firstVisPos, top);
                            }
                        }
                        this.loadingForward = false;
                    } else {
                        if (messArr.size() < count && load_type != 3) {
                            if (isCache) {
                                if (this.currentEncryptedChat != null || this.isBroadcast) {
                                    this.endReached[loadIndex] = true;
                                }
                                this.cacheEndReached[loadIndex] = true;
                            } else {
                                this.endReached[loadIndex] = true;
                            }
                        }
                        this.loading = false;
                        if (this.chatListView != null) {
                            if (this.first || this.scrollToTopOnResume) {
                                this.chatAdapter.notifyDataSetChanged();
                                if (this.scrollToMessage != null) {
                                    int yOffset;
                                    if (this.scrollToMessagePosition == -9000) {
                                        yOffset = Math.max(0, (this.chatListView.getHeight() - this.scrollToMessage.getApproximateHeight()) / 2);
                                    } else if (this.scrollToMessagePosition == -10000) {
                                        yOffset = 0;
                                    } else {
                                        yOffset = this.scrollToMessagePosition;
                                    }
                                    if (!this.messages.isEmpty()) {
                                        if (this.messages.get(this.messages.size() - 1) == this.scrollToMessage || this.messages.get(this.messages.size() - 2) == this.scrollToMessage) {
                                            this.chatLayoutManager.scrollToPositionWithOffset(this.chatAdapter.isBot ? 1 : 0, ((-this.chatListView.getPaddingTop()) - AndroidUtilities.dp(7.0f)) + yOffset);
                                        } else {
                                            this.chatLayoutManager.scrollToPositionWithOffset(((this.chatAdapter.messagesStartRow + this.messages.size()) - this.messages.indexOf(this.scrollToMessage)) - 1, ((-this.chatListView.getPaddingTop()) - AndroidUtilities.dp(7.0f)) + yOffset);
                                        }
                                    }
                                    this.chatListView.invalidate();
                                    if (this.scrollToMessagePosition == -10000 || this.scrollToMessagePosition == -9000) {
                                        showPagedownButton(true, true);
                                    }
                                    this.scrollToMessagePosition = -10000;
                                    this.scrollToMessage = null;
                                } else {
                                    moveScrollToLastMessage();
                                }
                            } else if (newRowsCount != 0) {
                                boolean end = false;
                                if (this.endReached[loadIndex] && ((loadIndex == 0 && this.mergeDialogId == 0) || loadIndex == 1)) {
                                    end = true;
                                    this.chatAdapter.notifyItemRangeChanged(this.chatAdapter.isBot ? 1 : 0, 2);
                                }
                                firstVisPos = this.chatLayoutManager.findLastVisibleItemPosition();
                                firstVisView = this.chatListView.getChildAt(this.chatListView.getChildCount() - 1);
                                top = (firstVisView == null ? 0 : firstVisView.getTop()) - this.chatListView.getPaddingTop();
                                if (newRowsCount - (end ? 1 : 0) > 0) {
                                    this.chatAdapter.notifyItemRangeInserted((this.chatAdapter.isBot ? 2 : 1) + (end ? 0 : 1), newRowsCount - (end ? 1 : 0));
                                }
                                if (firstVisPos != -1) {
                                    int i;
                                    LinearLayoutManager linearLayoutManager = this.chatLayoutManager;
                                    int i2 = firstVisPos + newRowsCount;
                                    if (end) {
                                        i = 1;
                                    } else {
                                        i = 0;
                                    }
                                    linearLayoutManager.scrollToPositionWithOffset(i2 - i, top);
                                }
                            } else if (this.endReached[loadIndex] && ((loadIndex == 0 && this.mergeDialogId == 0) || loadIndex == 1)) {
                                this.chatAdapter.notifyItemRemoved(this.chatAdapter.isBot ? 1 : 0);
                            }
                            if (this.paused) {
                                this.scrollToTopOnResume = true;
                                if (this.scrollToMessage != null) {
                                    this.scrollToTopUnReadOnResume = true;
                                }
                            }
                            if (this.first && this.chatListView != null) {
                                this.chatListView.setEmptyView(this.emptyViewContainer);
                            }
                        } else {
                            this.scrollToTopOnResume = true;
                            if (this.scrollToMessage != null) {
                                this.scrollToTopUnReadOnResume = true;
                            }
                        }
                    }
                    if (this.first && this.messages.size() > 0) {
                        if (loadIndex == 0) {
                            boolean wasUnreadFinal = wasUnread;
                            int last_unread_date_final = last_unread_date;
                            final int id2 = ((MessageObject) this.messages.get(0)).getId();
                            final int i3 = last_unread_date_final;
                            final boolean z2 = wasUnreadFinal;
                            AndroidUtilities.runOnUIThread(new Runnable() {
                                public void run() {
                                    if (ChatActivity.this.last_message_id != 0) {
                                        MessagesController.getInstance().markDialogAsRead(ChatActivity.this.dialog_id, id2, ChatActivity.this.last_message_id, i3, z2, false);
                                    } else {
                                        MessagesController.getInstance().markDialogAsRead(ChatActivity.this.dialog_id, id2, ChatActivity.this.minMessageId[0], ChatActivity.this.maxDate[0], z2, false);
                                    }
                                }
                            }, 700);
                        }
                        this.first = false;
                    }
                    if (this.messages.isEmpty() && this.currentEncryptedChat == null && this.currentUser != null && this.currentUser.bot && this.botUser == null) {
                        this.botUser = "";
                        updateBottomOverlay();
                    }
                    if (newRowsCount == 0 && this.currentEncryptedChat != null && !this.endReached[0]) {
                        this.first = true;
                        if (this.chatListView != null) {
                            this.chatListView.setEmptyView(null);
                        }
                        if (this.emptyViewContainer != null) {
                            this.emptyViewContainer.setVisibility(4);
                        }
                    } else if (this.progressView != null) {
                        this.progressView.setVisibility(4);
                    }
                    checkScrollForLoad();
                }
            }
        } else if (id == NotificationCenter.emojiDidLoaded) {
            if (this.chatListView != null) {
                this.chatListView.invalidateViews();
            }
            if (this.replyObjectTextView != null) {
                this.replyObjectTextView.invalidate();
            }
        } else if (id == NotificationCenter.updateInterfaces) {
            int updateMask = ((Integer) args[0]).intValue();
            if (!((updateMask & 1) == 0 && (updateMask & 16) == 0)) {
                updateTitle();
            }
            boolean updateSubtitle = false;
            if (!((updateMask & 32) == 0 && (updateMask & 4) == 0)) {
                if (this.currentChat != null) {
                    if (this.onlineCount != updateOnlineCount()) {
                        updateSubtitle = true;
                    }
                } else {
                    updateSubtitle = true;
                }
            }
            if (!((updateMask & 2) == 0 && (updateMask & 8) == 0 && (updateMask & 1) == 0)) {
                checkAndUpdateAvatar();
                updateVisibleRows();
            }
            if ((updateMask & 64) != 0) {
                CharSequence printString = (CharSequence) MessagesController.getInstance().printingStrings.get(Long.valueOf(this.dialog_id));
                if ((this.lastPrintString != null && printString == null) || ((this.lastPrintString == null && printString != null) || !(this.lastPrintString == null || printString == null || this.lastPrintString.equals(printString)))) {
                    updateSubtitle = true;
                }
            }
            if ((updateMask & 8192) != 0 && ChatObject.isChannel(this.currentChat)) {
                Chat chat = MessagesController.getInstance().getChat(Integer.valueOf(this.currentChat.id));
                if (chat != null) {
                    this.currentChat = chat;
                    updateSubtitle = true;
                    updateBottomOverlay();
                    if (this.chatActivityEnterView != null) {
                        this.chatActivityEnterView.setDialogId(this.dialog_id);
                    }
                } else {
                    return;
                }
            }
            if (updateSubtitle) {
                updateSubtitle();
            }
            if ((updateMask & 128) != 0) {
                updateContactStatus();
                updateSpamView();
            }
        } else if (id == NotificationCenter.didReceivedNewMessages) {
            if (((Long) args[0]).longValue() == this.dialog_id) {
                boolean updateChat = false;
                boolean hasFromMe = false;
                ArrayList<MessageObject> arr = args[1];
                if (this.currentEncryptedChat != null && arr.size() == 1) {
                    obj = (MessageObject) arr.get(0);
                    if (this.currentEncryptedChat != null && obj.isOut() && obj.messageOwner.action != null && (obj.messageOwner.action instanceof TL_messageEncryptedAction) && (obj.messageOwner.action.encryptedAction instanceof TL_decryptedMessageActionSetMessageTTL) && getParentActivity() != null && AndroidUtilities.getPeerLayerVersion(this.currentEncryptedChat.layer) < 17 && this.currentEncryptedChat.ttl > 0 && this.currentEncryptedChat.ttl <= 60) {
                        r0 = new Builder(getParentActivity());
                        r0.setTitle(LocaleController.getString("AppName", C0553R.string.AppName));
                        r0.setPositiveButton(LocaleController.getString("OK", C0553R.string.OK), null);
                        r0.setMessage(LocaleController.formatString("CompatibilityChat", C0553R.string.CompatibilityChat, this.currentUser.first_name, this.currentUser.first_name));
                        showDialog(r0.create());
                    }
                }
                ReplyMessageQuery.loadReplyMessagesForMessages(arr, this.dialog_id);
                boolean reloadMegagroup = false;
                Bundle bundle;
                final BaseFragment baseFragment;
                final Bundle bundle2;
                final int i4;
                if (this.forwardEndReached[0]) {
                    boolean markAsRead = false;
                    boolean unreadUpdated = true;
                    int oldCount = this.messages.size();
                    int addedCount = 0;
                    for (a = 0; a < arr.size(); a++) {
                        obj = (MessageObject) arr.get(a);
                        if (!(this.currentEncryptedChat == null || obj.messageOwner.action == null || !(obj.messageOwner.action instanceof TL_messageEncryptedAction) || !(obj.messageOwner.action.encryptedAction instanceof TL_decryptedMessageActionSetMessageTTL) || this.timerDrawable == null)) {
                            this.timerDrawable.setTime(((TL_decryptedMessageActionSetMessageTTL) obj.messageOwner.action.encryptedAction).ttl_seconds);
                        }
                        if (!this.messagesDict[0].containsKey(Integer.valueOf(obj.getId()))) {
                            if (obj.messageOwner.action instanceof TL_messageActionChatMigrateTo) {
                                bundle = new Bundle();
                                bundle.putInt("chat_id", obj.messageOwner.action.channel_id);
                                baseFragment = this.parentLayout.fragmentsStack.size() > 0 ? (BaseFragment) this.parentLayout.fragmentsStack.get(this.parentLayout.fragmentsStack.size() - 1) : null;
                                bundle2 = bundle;
                                i4 = obj.messageOwner.action.channel_id;
                                AndroidUtilities.runOnUIThread(new Runnable() {

                                    class C08791 implements Runnable {
                                        C08791() {
                                        }

                                        public void run() {
                                            MessagesController.getInstance().loadFullChat(i4, 0, true);
                                        }
                                    }

                                    public void run() {
                                        ActionBarLayout parentLayout = ChatActivity.this.parentLayout;
                                        if (baseFragment != null) {
                                            NotificationCenter.getInstance().removeObserver(baseFragment, NotificationCenter.closeChats);
                                        }
                                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats, new Object[0]);
                                        parentLayout.presentFragment(new ChatActivity(bundle2), true);
                                        AndroidUtilities.runOnUIThread(new C08791(), 1000);
                                    }
                                });
                                return;
                            }
                            if (this.currentChat != null && this.currentChat.megagroup && ((obj.messageOwner.action instanceof TL_messageActionChatAddUser) || (obj.messageOwner.action instanceof TL_messageActionChatDeleteUser))) {
                                reloadMegagroup = true;
                            }
                            if (this.minDate[0] == 0 || obj.messageOwner.date < this.minDate[0]) {
                                this.minDate[0] = obj.messageOwner.date;
                            }
                            if (obj.isOut()) {
                                removeUnreadPlane();
                                hasFromMe = true;
                            }
                            if (obj.getId() > 0) {
                                this.maxMessageId[0] = Math.min(obj.getId(), this.maxMessageId[0]);
                                this.minMessageId[0] = Math.max(obj.getId(), this.minMessageId[0]);
                            } else if (this.currentEncryptedChat != null) {
                                this.maxMessageId[0] = Math.max(obj.getId(), this.maxMessageId[0]);
                                this.minMessageId[0] = Math.min(obj.getId(), this.minMessageId[0]);
                            }
                            this.maxDate[0] = Math.max(this.maxDate[0], obj.messageOwner.date);
                            this.messagesDict[0].put(Integer.valueOf(obj.getId()), obj);
                            dayArray = (ArrayList) this.messagesByDays.get(obj.dateKey);
                            if (dayArray == null) {
                                dayArray = new ArrayList();
                                this.messagesByDays.put(obj.dateKey, dayArray);
                                dateMsg = new Message();
                                dateMsg.message = LocaleController.formatDateChat((long) obj.messageOwner.date);
                                dateMsg.id = 0;
                                messageObject = new MessageObject(dateMsg, null, false);
                                messageObject.type = 10;
                                messageObject.contentType = 4;
                                this.messages.add(0, messageObject);
                                addedCount++;
                            }
                            if (!obj.isOut()) {
                                if (this.paused) {
                                    if (!(this.scrollToTopUnReadOnResume || this.unreadMessageObject == null)) {
                                        if (this.chatAdapter != null) {
                                            this.chatAdapter.removeMessageObject(this.unreadMessageObject);
                                        } else {
                                            this.messages.remove(this.unreadMessageObject);
                                        }
                                        this.unreadMessageObject = null;
                                    }
                                    if (this.unreadMessageObject == null) {
                                        dateMsg = new Message();
                                        dateMsg.message = "";
                                        dateMsg.id = 0;
                                        messageObject = new MessageObject(dateMsg, null, false);
                                        messageObject.type = 6;
                                        messageObject.contentType = 6;
                                        this.messages.add(0, messageObject);
                                        this.unreadMessageObject = messageObject;
                                        this.scrollToMessage = this.unreadMessageObject;
                                        this.scrollToMessagePosition = -10000;
                                        unreadUpdated = false;
                                        this.unread_to_load = 0;
                                        this.scrollToTopUnReadOnResume = true;
                                        addedCount++;
                                    }
                                }
                                if (this.unreadMessageObject != null) {
                                    this.unread_to_load++;
                                    unreadUpdated = true;
                                }
                                if (obj.isUnread()) {
                                    if (!this.paused) {
                                        obj.setIsRead();
                                    }
                                    markAsRead = true;
                                }
                            }
                            dayArray.add(0, obj);
                            this.messages.add(0, obj);
                            addedCount++;
                            if (obj.type == 10 || obj.type == 11) {
                                updateChat = true;
                            }
                        }
                    }
                    if (this.progressView != null) {
                        this.progressView.setVisibility(4);
                    }
                    if (this.chatAdapter != null) {
                        if (unreadUpdated) {
                            this.chatAdapter.updateRowWithMessageObject(this.unreadMessageObject);
                        }
                        if (addedCount != 0) {
                            this.chatAdapter.notifyItemRangeInserted(this.chatAdapter.getItemCount(), addedCount);
                        }
                    } else {
                        this.scrollToTopOnResume = true;
                    }
                    if (this.chatListView == null || this.chatAdapter == null) {
                        this.scrollToTopOnResume = true;
                    } else {
                        int lastVisible = this.chatLayoutManager.findLastVisibleItemPosition();
                        if (lastVisible == -1) {
                            lastVisible = 0;
                        }
                        if (this.endReached[0]) {
                            lastVisible++;
                        }
                        if (this.chatAdapter.isBot) {
                            oldCount++;
                        }
                        if (lastVisible < oldCount && !hasFromMe) {
                            showPagedownButton(true, true);
                        } else if (!this.firstLoading) {
                            if (this.paused) {
                                this.scrollToTopOnResume = true;
                            } else {
                                moveScrollToLastMessage();
                            }
                        }
                    }
                    if (markAsRead) {
                        if (this.paused) {
                            this.readWhenResume = true;
                            this.readWithDate = this.maxDate[0];
                            this.readWithMid = this.minMessageId[0];
                        } else {
                            MessagesController.getInstance().markDialogAsRead(this.dialog_id, ((MessageObject) this.messages.get(0)).getId(), this.minMessageId[0], this.maxDate[0], true, false);
                        }
                    }
                } else {
                    int currentMaxDate = Integer.MIN_VALUE;
                    int currentMinMsgId = Integer.MIN_VALUE;
                    if (this.currentEncryptedChat != null) {
                        currentMinMsgId = ConnectionsManager.DEFAULT_DATACENTER_ID;
                    }
                    boolean currentMarkAsRead = false;
                    for (a = 0; a < arr.size(); a++) {
                        obj = (MessageObject) arr.get(a);
                        if (!(this.currentEncryptedChat == null || obj.messageOwner.action == null || !(obj.messageOwner.action instanceof TL_messageEncryptedAction) || !(obj.messageOwner.action.encryptedAction instanceof TL_decryptedMessageActionSetMessageTTL) || this.timerDrawable == null)) {
                            this.timerDrawable.setTime(((TL_decryptedMessageActionSetMessageTTL) obj.messageOwner.action.encryptedAction).ttl_seconds);
                        }
                        if (obj.messageOwner.action instanceof TL_messageActionChatMigrateTo) {
                            bundle = new Bundle();
                            bundle.putInt("chat_id", obj.messageOwner.action.channel_id);
                            baseFragment = this.parentLayout.fragmentsStack.size() > 0 ? (BaseFragment) this.parentLayout.fragmentsStack.get(this.parentLayout.fragmentsStack.size() - 1) : null;
                            bundle2 = bundle;
                            i4 = obj.messageOwner.action.channel_id;
                            AndroidUtilities.runOnUIThread(new Runnable() {

                                class C08781 implements Runnable {
                                    C08781() {
                                    }

                                    public void run() {
                                        MessagesController.getInstance().loadFullChat(i4, 0, true);
                                    }
                                }

                                public void run() {
                                    ActionBarLayout parentLayout = ChatActivity.this.parentLayout;
                                    if (baseFragment != null) {
                                        NotificationCenter.getInstance().removeObserver(baseFragment, NotificationCenter.closeChats);
                                    }
                                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats, new Object[0]);
                                    parentLayout.presentFragment(new ChatActivity(bundle2), true);
                                    AndroidUtilities.runOnUIThread(new C08781(), 1000);
                                }
                            });
                            return;
                        }
                        if (this.currentChat != null && this.currentChat.megagroup && ((obj.messageOwner.action instanceof TL_messageActionChatAddUser) || (obj.messageOwner.action instanceof TL_messageActionChatDeleteUser))) {
                            reloadMegagroup = true;
                        }
                        if (obj.isOut() && obj.isSending()) {
                            scrollToLastMessage(false);
                            return;
                        }
                        if (!this.messagesDict[0].containsKey(Integer.valueOf(obj.getId()))) {
                            currentMaxDate = Math.max(currentMaxDate, obj.messageOwner.date);
                            if (obj.getId() > 0) {
                                currentMinMsgId = Math.max(obj.getId(), currentMinMsgId);
                                this.last_message_id = Math.max(this.last_message_id, obj.getId());
                            } else if (this.currentEncryptedChat != null) {
                                currentMinMsgId = Math.min(obj.getId(), currentMinMsgId);
                                this.last_message_id = Math.min(this.last_message_id, obj.getId());
                            }
                            if (!obj.isOut() && obj.isUnread()) {
                                this.unread_to_load++;
                                currentMarkAsRead = true;
                            }
                            if (obj.type == 10 || obj.type == 11) {
                                updateChat = true;
                            }
                        }
                    }
                    if (currentMarkAsRead) {
                        if (this.paused) {
                            this.readWhenResume = true;
                            this.readWithDate = currentMaxDate;
                            this.readWithMid = currentMinMsgId;
                        } else if (this.messages.size() > 0) {
                            MessagesController.getInstance().markDialogAsRead(this.dialog_id, ((MessageObject) this.messages.get(0)).getId(), currentMinMsgId, currentMaxDate, true, false);
                        }
                    }
                    updateVisibleRows();
                }
                if (!(this.messages.isEmpty() || this.botUser == null || this.botUser.length() != 0)) {
                    this.botUser = null;
                    updateBottomOverlay();
                }
                if (updateChat) {
                    updateTitle();
                    checkAndUpdateAvatar();
                }
                if (reloadMegagroup) {
                    MessagesController.getInstance().loadFullChat(this.currentChat.id, 0, true);
                }
            }
        } else if (id == NotificationCenter.closeChats) {
            if (args == null || args.length <= 0) {
                removeSelfFromStack();
            } else if (((Long) args[0]).longValue() == this.dialog_id) {
                finishFragment();
            }
        } else if (id == NotificationCenter.messagesRead) {
            SparseArray<Long> inbox = args[0];
            SparseIntArray outbox = args[1];
            updated = false;
            int b = 0;
            while (b < inbox.size()) {
                key = inbox.keyAt(b);
                long messageId = ((Long) inbox.get(key)).longValue();
                if (((long) key) != this.dialog_id) {
                    b++;
                } else {
                    for (a = 0; a < this.messages.size(); a++) {
                        obj = (MessageObject) this.messages.get(a);
                        if (!obj.isOut() && obj.getId() > 0 && obj.getId() <= ((int) messageId)) {
                            if (!obj.isUnread()) {
                                break;
                            }
                            obj.setIsRead();
                            updated = true;
                        }
                    }
                    while (b < outbox.size()) {
                        key = outbox.keyAt(b);
                        messageId = outbox.get(key);
                        if (((long) key) == this.dialog_id) {
                        } else {
                            for (a = 0; a < this.messages.size(); a++) {
                                obj = (MessageObject) this.messages.get(a);
                                if (obj.isOut() && obj.getId() > 0 && obj.getId() <= messageId) {
                                    if (!obj.isUnread()) {
                                        break;
                                    }
                                    obj.setIsRead();
                                    updated = true;
                                }
                            }
                            if (!updated) {
                                updateVisibleRows();
                            }
                        }
                    }
                    if (!updated) {
                        updateVisibleRows();
                    }
                }
            }
            for (b = 0; b < outbox.size(); b++) {
                key = outbox.keyAt(b);
                messageId = outbox.get(key);
                if (((long) key) == this.dialog_id) {
                    for (a = 0; a < this.messages.size(); a++) {
                        obj = (MessageObject) this.messages.get(a);
                        if (!obj.isUnread()) {
                            break;
                        }
                        obj.setIsRead();
                        updated = true;
                    }
                    if (!updated) {
                        updateVisibleRows();
                    }
                }
            }
            if (!updated) {
                updateVisibleRows();
            }
        } else if (id == NotificationCenter.messagesDeleted) {
            ArrayList<Integer> markAsDeletedMessages = args[0];
            int channelId = ((Integer) args[1]).intValue();
            loadIndex = 0;
            if (ChatObject.isChannel(this.currentChat)) {
                if (channelId == 0 && this.mergeDialogId != 0) {
                    loadIndex = 1;
                } else if (channelId == this.currentChat.id) {
                    loadIndex = 0;
                } else {
                    return;
                }
            } else if (channelId != 0) {
                return;
            }
            updated = false;
            for (a = 0; a < markAsDeletedMessages.size(); a++) {
                Integer ids = (Integer) markAsDeletedMessages.get(a);
                obj = (MessageObject) this.messagesDict[loadIndex].get(ids);
                if (obj != null) {
                    index = this.messages.indexOf(obj);
                    if (index != -1) {
                        this.messages.remove(index);
                        this.messagesDict[loadIndex].remove(ids);
                        dayArr = (ArrayList) this.messagesByDays.get(obj.dateKey);
                        if (dayArr != null) {
                            dayArr.remove(obj);
                            if (dayArr.isEmpty()) {
                                this.messagesByDays.remove(obj.dateKey);
                                if (index >= 0 && index < this.messages.size()) {
                                    this.messages.remove(index);
                                }
                            }
                            updated = true;
                        }
                    }
                }
            }
            if (this.messages.isEmpty()) {
                if (this.endReached[0] || this.loading) {
                    if (this.botButtons != null) {
                        this.botButtons = null;
                        if (this.chatActivityEnterView != null) {
                            this.chatActivityEnterView.setButtons(null, false);
                        }
                    }
                    if (this.currentEncryptedChat == null && this.currentUser != null && this.currentUser.bot && this.botUser == null) {
                        this.botUser = "";
                        updateBottomOverlay();
                    }
                } else {
                    int[] iArr;
                    if (this.progressView != null) {
                        this.progressView.setVisibility(4);
                    }
                    if (this.chatListView != null) {
                        this.chatListView.setEmptyView(null);
                    }
                    if (this.currentEncryptedChat == null) {
                        iArr = this.maxMessageId;
                        this.maxMessageId[1] = ConnectionsManager.DEFAULT_DATACENTER_ID;
                        iArr[0] = ConnectionsManager.DEFAULT_DATACENTER_ID;
                        iArr = this.minMessageId;
                        this.minMessageId[1] = Integer.MIN_VALUE;
                        iArr[0] = Integer.MIN_VALUE;
                    } else {
                        iArr = this.maxMessageId;
                        this.maxMessageId[1] = Integer.MIN_VALUE;
                        iArr[0] = Integer.MIN_VALUE;
                        iArr = this.minMessageId;
                        this.minMessageId[1] = ConnectionsManager.DEFAULT_DATACENTER_ID;
                        iArr[0] = ConnectionsManager.DEFAULT_DATACENTER_ID;
                    }
                    iArr = this.maxDate;
                    this.maxDate[1] = Integer.MIN_VALUE;
                    iArr[0] = Integer.MIN_VALUE;
                    iArr = this.minDate;
                    this.minDate[1] = 0;
                    iArr[0] = 0;
                    this.waitingForLoad.add(Integer.valueOf(this.lastLoadIndex));
                    r11 = MessagesController.getInstance();
                    r12 = this.dialog_id;
                    boolean z3 = !this.cacheEndReached[0];
                    int i5 = this.minDate[0];
                    r18 = this.classGuid;
                    r21 = this.channelMessagesImportant;
                    r22 = this.lastLoadIndex;
                    this.lastLoadIndex = r22 + 1;
                    r11.loadMessages(r12, 30, 0, z3, i5, r18, 0, 0, r21, r22);
                    this.loading = true;
                }
            }
            if (updated && this.chatAdapter != null) {
                removeUnreadPlane();
                this.chatAdapter.notifyDataSetChanged();
            }
        } else if (id == NotificationCenter.messageReceivedByServer) {
            Integer msgId = args[0];
            obj = (MessageObject) this.messagesDict[0].get(msgId);
            if (obj != null) {
                Integer newMsgId = args[1];
                if (newMsgId.equals(msgId) || !this.messagesDict[0].containsKey(newMsgId)) {
                    Message newMsgObj = args[2];
                    mediaUpdated = false;
                    try {
                        mediaUpdated = (newMsgObj.media == null || obj.messageOwner.media == null || newMsgObj.media.getClass().equals(obj.messageOwner.media.getClass())) ? false : true;
                    } catch (Throwable e) {
                        FileLog.m611e("tmessages", e);
                    }
                    if (newMsgObj != null) {
                        obj.messageOwner.media = newMsgObj.media;
                        obj.generateThumbs(true);
                    }
                    this.messagesDict[0].remove(msgId);
                    this.messagesDict[0].put(newMsgId, obj);
                    obj.messageOwner.id = newMsgId.intValue();
                    obj.messageOwner.send_state = 0;
                    messArr = new ArrayList();
                    messArr.add(obj);
                    ReplyMessageQuery.loadReplyMessagesForMessages(messArr, this.dialog_id);
                    if (this.chatAdapter != null) {
                        this.chatAdapter.updateRowWithMessageObject(obj);
                    }
                    if (mediaUpdated && this.chatLayoutManager.findLastVisibleItemPosition() >= this.messages.size() - 1) {
                        moveScrollToLastMessage();
                    }
                    NotificationsController.getInstance().playOutChatSound();
                    return;
                }
                MessageObject removed = (MessageObject) this.messagesDict[0].remove(msgId);
                if (removed != null) {
                    index = this.messages.indexOf(removed);
                    this.messages.remove(index);
                    dayArr = (ArrayList) this.messagesByDays.get(removed.dateKey);
                    dayArr.remove(obj);
                    if (dayArr.isEmpty()) {
                        this.messagesByDays.remove(obj.dateKey);
                        if (index >= 0 && index < this.messages.size()) {
                            this.messages.remove(index);
                        }
                    }
                    this.chatAdapter.notifyDataSetChanged();
                }
            }
        } else if (id == NotificationCenter.messageReceivedByAck) {
            obj = (MessageObject) this.messagesDict[0].get((Integer) args[0]);
            if (obj != null) {
                obj.messageOwner.send_state = 0;
                if (this.chatAdapter != null) {
                    this.chatAdapter.updateRowWithMessageObject(obj);
                }
            }
        } else if (id == NotificationCenter.messageSendError) {
            obj = (MessageObject) this.messagesDict[0].get((Integer) args[0]);
            if (obj != null) {
                obj.messageOwner.send_state = 2;
                updateVisibleRows();
            }
        } else if (id == NotificationCenter.chatInfoDidLoaded) {
            ChatFull chatFull = args[0];
            if (this.currentChat != null && chatFull.id == this.currentChat.id) {
                if (chatFull instanceof TL_channelFull) {
                    if (this.currentChat.megagroup) {
                        int lastDate = 0;
                        if (chatFull.participants != null) {
                            for (a = 0; a < chatFull.participants.participants.size(); a++) {
                                lastDate = Math.max(((ChatParticipant) chatFull.participants.participants.get(a)).date, lastDate);
                            }
                        }
                        if (lastDate == 0 || Math.abs((System.currentTimeMillis() / 1000) - ((long) lastDate)) > 3600) {
                            MessagesController.getInstance().loadChannelParticipants(Integer.valueOf(this.currentChat.id));
                        }
                    }
                    if (chatFull.participants == null && this.info != null) {
                        chatFull.participants = this.info.participants;
                    }
                }
                this.info = chatFull;
                if (this.mentionsAdapter != null) {
                    this.mentionsAdapter.setChatInfo(this.info);
                }
                updateOnlineCount();
                updateSubtitle();
                if (this.isBroadcast) {
                    SendMessagesHelper.getInstance().setCurrentChatInfo(this.info);
                }
                if (this.info instanceof TL_chatFull) {
                    this.hasBotsCommands = false;
                    this.botInfo.clear();
                    this.botsCount = 0;
                    URLSpanBotCommand.enabled = false;
                    for (a = 0; a < this.info.participants.participants.size(); a++) {
                        User user = MessagesController.getInstance().getUser(Integer.valueOf(((ChatParticipant) this.info.participants.participants.get(a)).user_id));
                        if (user != null && user.bot) {
                            URLSpanBotCommand.enabled = true;
                            this.botsCount++;
                            BotQuery.loadBotInfo(user.id, true, this.classGuid);
                        }
                    }
                    if (this.chatListView != null) {
                        this.chatListView.invalidateViews();
                    }
                } else if (this.info instanceof TL_channelFull) {
                    this.hasBotsCommands = false;
                    this.botInfo.clear();
                    this.botsCount = 0;
                    URLSpanBotCommand.enabled = !this.info.bot_info.isEmpty();
                    this.botsCount = this.info.bot_info.size();
                    for (a = 0; a < this.info.bot_info.size(); a++) {
                        BotInfo bot = (BotInfo) this.info.bot_info.get(a);
                        if (!bot.commands.isEmpty()) {
                            this.hasBotsCommands = true;
                        }
                        this.botInfo.put(Integer.valueOf(bot.user_id), bot);
                    }
                    if (this.chatListView != null) {
                        this.chatListView.invalidateViews();
                    }
                    if (this.mentionsAdapter != null) {
                        this.mentionsAdapter.setBotInfo(this.botInfo);
                    }
                }
                if (this.chatActivityEnterView != null) {
                    this.chatActivityEnterView.setBotsCount(this.botsCount, this.hasBotsCommands);
                }
                if (this.mentionsAdapter != null) {
                    this.mentionsAdapter.setBotsCount(this.botsCount);
                }
                if (ChatObject.isChannel(this.currentChat) && this.mergeDialogId == 0 && this.info.migrated_from_chat_id != 0) {
                    this.mergeDialogId = (long) (-this.info.migrated_from_chat_id);
                    this.maxMessageId[1] = this.info.migrated_from_max_id;
                    if (this.chatAdapter != null) {
                        this.chatAdapter.notifyDataSetChanged();
                    }
                }
            }
        } else if (id == NotificationCenter.chatInfoCantLoad) {
            int chatId = ((Integer) args[0]).intValue();
            if (this.currentChat != null && this.currentChat.id == chatId && getParentActivity() != null && this.closeChatDialog == null) {
                r0 = new Builder(getParentActivity());
                r0.setTitle(LocaleController.getString("AppName", C0553R.string.AppName));
                r0.setMessage(LocaleController.getString("ChannelCantOpenPrivate", C0553R.string.ChannelCantOpenPrivate));
                r0.setPositiveButton(LocaleController.getString("OK", C0553R.string.OK), null);
                Dialog create = r0.create();
                this.closeChatDialog = create;
                showDialog(create);
                this.loading = false;
                if (this.progressView != null) {
                    this.progressView.setVisibility(4);
                }
                if (this.chatAdapter != null) {
                    this.chatAdapter.notifyDataSetChanged();
                }
            }
        } else if (id == NotificationCenter.contactsDidLoaded) {
            updateContactStatus();
            updateSubtitle();
            updateSpamView();
        } else if (id == NotificationCenter.encryptedChatUpdated) {
            EncryptedChat chat2 = args[0];
            if (this.currentEncryptedChat != null && chat2.id == this.currentEncryptedChat.id) {
                this.currentEncryptedChat = chat2;
                updateContactStatus();
                updateSecretStatus();
            }
        } else if (id == NotificationCenter.messagesReadEncrypted) {
            int encId = ((Integer) args[0]).intValue();
            if (this.currentEncryptedChat != null && this.currentEncryptedChat.id == encId) {
                int date = ((Integer) args[1]).intValue();
                i$ = this.messages.iterator();
                while (i$.hasNext()) {
                    obj = (MessageObject) i$.next();
                    if (obj.isOut()) {
                        if (obj.isOut() && !obj.isUnread()) {
                            break;
                        } else if (obj.messageOwner.date - 1 <= date) {
                            obj.setIsRead();
                        }
                    }
                }
                updateVisibleRows();
            }
        } else if (id == NotificationCenter.audioDidReset || id == NotificationCenter.audioPlayStateChanged) {
            if (this.chatListView != null) {
                count = this.chatListView.getChildCount();
                for (a = 0; a < count; a++) {
                    view = this.chatListView.getChildAt(a);
                    if (view instanceof ChatAudioCell) {
                        cell = (ChatAudioCell) view;
                        if (cell.getMessageObject() != null) {
                            cell.updateButtonState(false);
                        }
                    } else if (view instanceof ChatMusicCell) {
                        cell = (ChatMusicCell) view;
                        if (cell.getMessageObject() != null) {
                            cell.updateButtonState(false);
                        }
                    }
                }
            }
        } else if (id == NotificationCenter.audioProgressDidChanged) {
            Integer mid = args[0];
            if (this.chatListView != null) {
                count = this.chatListView.getChildCount();
                for (a = 0; a < count; a++) {
                    view = this.chatListView.getChildAt(a);
                    if (view instanceof ChatAudioCell) {
                        cell = (ChatAudioCell) view;
                        if (cell.getMessageObject() != null && cell.getMessageObject().getId() == mid.intValue()) {
                            cell.updateProgress();
                            return;
                        }
                    } else if (view instanceof ChatMusicCell) {
                        cell = (ChatMusicCell) view;
                        if (cell.getMessageObject() != null && cell.getMessageObject().getId() == mid.intValue()) {
                            MessageObject playing = cell.getMessageObject();
                            MessageObject player = MediaController.getInstance().getPlayingMessageObject();
                            if (player != null) {
                                playing.audioProgress = player.audioProgress;
                                playing.audioProgressSec = player.audioProgressSec;
                                cell.updateProgress();
                                return;
                            }
                            return;
                        }
                    } else {
                        continue;
                    }
                }
            }
        } else if (id == NotificationCenter.removeAllMessagesFromDialog) {
            if (this.dialog_id == ((Long) args[0]).longValue()) {
                this.messages.clear();
                this.waitingForLoad.clear();
                this.messagesByDays.clear();
                for (a = 1; a >= 0; a--) {
                    this.messagesDict[a].clear();
                    if (this.currentEncryptedChat == null) {
                        this.maxMessageId[a] = ConnectionsManager.DEFAULT_DATACENTER_ID;
                        this.minMessageId[a] = Integer.MIN_VALUE;
                    } else {
                        this.maxMessageId[a] = Integer.MIN_VALUE;
                        this.minMessageId[a] = ConnectionsManager.DEFAULT_DATACENTER_ID;
                    }
                    this.maxDate[a] = Integer.MIN_VALUE;
                    this.minDate[a] = 0;
                    this.selectedMessagesIds[a].clear();
                    this.selectedMessagesCanCopyIds[a].clear();
                }
                this.cantDeleteMessagesCount = 0;
                this.actionBar.hideActionMode();
                if (this.botButtons != null) {
                    this.botButtons = null;
                    this.chatActivityEnterView.setButtons(null, false);
                }
                if (this.currentEncryptedChat == null && this.currentUser != null && this.currentUser.bot && this.botUser == null) {
                    this.botUser = "";
                    updateBottomOverlay();
                }
                if (((Boolean) args[1]).booleanValue()) {
                    this.progressView.setVisibility(this.chatAdapter.botInfoRow == -1 ? 0 : 4);
                    this.chatListView.setEmptyView(null);
                    for (a = 0; a < 2; a++) {
                        this.endReached[a] = false;
                        this.cacheEndReached[a] = false;
                        this.forwardEndReached[a] = true;
                    }
                    this.first = true;
                    this.firstLoading = true;
                    this.loading = true;
                    this.waitingForImportantLoad = false;
                    this.startLoadFromMessageId = 0;
                    this.needSelectFromMessageId = false;
                    this.waitingForLoad.add(Integer.valueOf(this.lastLoadIndex));
                    r11 = MessagesController.getInstance();
                    r12 = this.dialog_id;
                    int i6 = AndroidUtilities.isTablet() ? 30 : 20;
                    r18 = this.classGuid;
                    r21 = this.channelMessagesImportant;
                    r22 = this.lastLoadIndex;
                    this.lastLoadIndex = r22 + 1;
                    r11.loadMessages(r12, i6, 0, true, 0, r18, 2, 0, r21, r22);
                } else if (this.progressView != null) {
                    this.progressView.setVisibility(4);
                    this.chatListView.setEmptyView(this.emptyViewContainer);
                }
                if (this.chatAdapter != null) {
                    this.chatAdapter.notifyDataSetChanged();
                }
            }
        } else if (id == NotificationCenter.screenshotTook) {
            updateInformationForScreenshotDetector();
        } else if (id == NotificationCenter.blockedUsersDidLoaded) {
            if (this.currentUser != null) {
                boolean oldValue = this.userBlocked;
                this.userBlocked = MessagesController.getInstance().blockedUsers.contains(Integer.valueOf(this.currentUser.id));
                if (oldValue != this.userBlocked) {
                    updateBottomOverlay();
                    updateSpamView();
                }
            }
        } else if (id == NotificationCenter.FileNewChunkAvailable) {
            messageObject = args[0];
            long finalSize = ((Long) args[2]).longValue();
            if (finalSize != 0 && this.dialog_id == messageObject.getDialogId()) {
                MessageObject currentObject = (MessageObject) this.messagesDict[0].get(Integer.valueOf(messageObject.getId()));
                if (currentObject != null) {
                    currentObject.messageOwner.media.video.size = (int) finalSize;
                    updateVisibleRows();
                }
            }
        } else if (id == NotificationCenter.didCreatedNewDeleteTask) {
            SparseArray<ArrayList<Integer>> mids = args[0];
            changed = false;
            for (int i7 = 0; i7 < mids.size(); i7++) {
                key = mids.keyAt(i7);
                i$ = ((ArrayList) mids.get(key)).iterator();
                while (i$.hasNext()) {
                    messageObject = (MessageObject) this.messagesDict[0].get((Integer) i$.next());
                    if (messageObject != null) {
                        messageObject.messageOwner.destroyTime = key;
                        changed = true;
                    }
                }
            }
            if (changed) {
                updateVisibleRows();
            }
        } else if (id == NotificationCenter.audioDidStarted) {
            sendSecretMessageRead((MessageObject) args[0]);
            if (this.chatListView != null) {
                count = this.chatListView.getChildCount();
                for (a = 0; a < count; a++) {
                    view = this.chatListView.getChildAt(a);
                    if (view instanceof ChatAudioCell) {
                        cell = (ChatAudioCell) view;
                        if (cell.getMessageObject() != null) {
                            cell.updateButtonState(false);
                        }
                    } else if (view instanceof ChatMusicCell) {
                        cell = (ChatMusicCell) view;
                        if (cell.getMessageObject() != null) {
                            cell.updateButtonState(false);
                        }
                    }
                }
            }
        } else if (id == NotificationCenter.updateMessageMedia) {
            messageObject = (MessageObject) args[0];
            MessageObject existMessageObject = (MessageObject) this.messagesDict[0].get(Integer.valueOf(messageObject.getId()));
            if (existMessageObject != null) {
                existMessageObject.messageOwner.media = messageObject.messageOwner.media;
                existMessageObject.messageOwner.attachPath = messageObject.messageOwner.attachPath;
                existMessageObject.generateThumbs(false);
            }
            updateVisibleRows();
        } else if (id == NotificationCenter.replaceMessagesObjects) {
            did = ((Long) args[0]).longValue();
            if (did == this.dialog_id || did == this.mergeDialogId) {
                loadIndex = did == this.dialog_id ? 0 : 1;
                changed = false;
                mediaUpdated = false;
                i$ = args[1].iterator();
                while (i$.hasNext()) {
                    messageObject = (MessageObject) i$.next();
                    MessageObject old = (MessageObject) this.messagesDict[loadIndex].get(Integer.valueOf(messageObject.getId()));
                    if (old != null) {
                        if (!mediaUpdated && (messageObject.messageOwner.media instanceof TL_messageMediaWebPage)) {
                            mediaUpdated = true;
                        }
                        this.messagesDict[loadIndex].put(Integer.valueOf(old.getId()), messageObject);
                        index = this.messages.indexOf(old);
                        if (index >= 0) {
                            this.messages.set(index, messageObject);
                            if (this.chatAdapter != null) {
                                this.chatAdapter.notifyItemChanged(((this.chatAdapter.messagesStartRow + this.messages.size()) - index) - 1);
                            }
                            changed = true;
                        }
                    }
                }
                if (changed && this.chatLayoutManager != null && mediaUpdated) {
                    if (this.chatLayoutManager.findLastVisibleItemPosition() >= this.messages.size() - (this.chatAdapter.isBot ? 2 : 1)) {
                        moveScrollToLastMessage();
                    }
                }
            }
        } else if (id == NotificationCenter.notificationsSettingsUpdated) {
            updateTitleIcons();
            if (ChatObject.isChannel(this.currentChat)) {
                updateBottomOverlay();
            }
        } else if (id == NotificationCenter.didLoadedReplyMessages) {
            if (((Long) args[0]).longValue() == this.dialog_id) {
                updateVisibleRows();
            }
        } else if (id == NotificationCenter.didReceivedWebpages) {
            ArrayList<Message> arrayList = args[0];
            updated = false;
            for (a = 0; a < arrayList.size(); a++) {
                message = (Message) arrayList.get(a);
                did = MessageObject.getDialogId(message);
                if (did == this.dialog_id || did == this.mergeDialogId) {
                    currentMessage = (MessageObject) this.messagesDict[did == this.dialog_id ? 0 : 1].get(Integer.valueOf(message.id));
                    if (currentMessage != null) {
                        currentMessage.messageOwner.media = new TL_messageMediaWebPage();
                        currentMessage.messageOwner.media.webpage = message.media.webpage;
                        currentMessage.generateThumbs(true);
                        updated = true;
                    }
                }
            }
            if (updated) {
                updateVisibleRows();
                if (this.chatLayoutManager.findLastVisibleItemPosition() >= this.messages.size() - 1) {
                    moveScrollToLastMessage();
                }
            }
        } else if (id == NotificationCenter.didReceivedWebpagesInUpdates) {
            if (this.foundWebPage != null) {
                for (WebPage webPage : args[0].values()) {
                    if (webPage.id == this.foundWebPage.id) {
                        showReplyPanel(!(webPage instanceof TL_webPageEmpty), null, null, webPage, false, true);
                        return;
                    }
                }
            }
        } else if (id == NotificationCenter.messagesReadContent) {
            ArrayList<Long> arrayList2 = args[0];
            updated = false;
            for (a = 0; a < arrayList2.size(); a++) {
                currentMessage = (MessageObject) this.messagesDict[0].get(Integer.valueOf((int) ((Long) arrayList2.get(a)).longValue()));
                if (currentMessage != null) {
                    currentMessage.setContentIsRead();
                    updated = true;
                }
            }
            if (updated) {
                updateVisibleRows();
            }
        } else if (id == NotificationCenter.botInfoDidLoaded) {
            if (this.classGuid == ((Integer) args[1]).intValue()) {
                BotInfo info = args[0];
                if (this.currentEncryptedChat == null) {
                    if (!info.commands.isEmpty()) {
                        this.hasBotsCommands = true;
                    }
                    this.botInfo.put(Integer.valueOf(info.user_id), info);
                    if (this.chatAdapter != null) {
                        this.chatAdapter.notifyItemChanged(0);
                    }
                    if (this.mentionsAdapter != null) {
                        this.mentionsAdapter.setBotInfo(this.botInfo);
                    }
                    if (this.chatActivityEnterView != null) {
                        this.chatActivityEnterView.setBotsCount(this.botsCount, this.hasBotsCommands);
                    }
                }
                updateBotButtons();
            }
        } else if (id == NotificationCenter.botKeyboardDidLoaded) {
            if (this.dialog_id == ((Long) args[1]).longValue()) {
                message = (Message) args[0];
                if (message == null || this.userBlocked) {
                    this.botButtons = null;
                    if (this.chatActivityEnterView != null) {
                        if (this.replyingMessageObject != null && this.botReplyButtons == this.replyingMessageObject) {
                            this.botReplyButtons = null;
                            showReplyPanel(false, null, null, null, false, true);
                        }
                        this.chatActivityEnterView.setButtons(this.botButtons);
                        return;
                    }
                    return;
                }
                this.botButtons = new MessageObject(message, null, false);
                if (this.chatActivityEnterView == null) {
                    return;
                }
                if (this.botButtons.messageOwner.reply_markup instanceof TL_replyKeyboardForceReply) {
                    if (ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0).getInt("answered_" + this.dialog_id, 0) == this.botButtons.getId()) {
                        return;
                    }
                    if (this.replyingMessageObject == null || this.chatActivityEnterView.getFieldText() == null) {
                        this.botReplyButtons = this.botButtons;
                        this.chatActivityEnterView.setButtons(this.botButtons);
                        showReplyPanel(true, this.botButtons, null, null, false, true);
                        return;
                    }
                    return;
                }
                if (this.replyingMessageObject != null && this.botReplyButtons == this.replyingMessageObject) {
                    this.botReplyButtons = null;
                    showReplyPanel(false, null, null, null, false, true);
                }
                this.chatActivityEnterView.setButtons(this.botButtons);
            }
        } else if (id == NotificationCenter.chatSearchResultsAvailable) {
            if (this.classGuid == ((Integer) args[0]).intValue()) {
                messageId = ((Integer) args[1]).intValue();
                did = ((Long) args[3]).longValue();
                if (messageId != 0) {
                    scrollToMessageId(messageId, 0, true, did == this.dialog_id ? 0 : 1);
                }
                updateSearchButtons(((Integer) args[2]).intValue());
            }
        } else if (id == NotificationCenter.didUpdatedMessagesViews) {
            SparseIntArray array = (SparseIntArray) args[0].get((int) this.dialog_id);
            if (array != null) {
                updated = false;
                for (a = 0; a < array.size(); a++) {
                    messageId = array.keyAt(a);
                    messageObject = (MessageObject) this.messagesDict[0].get(Integer.valueOf(messageId));
                    if (messageObject != null) {
                        int newValue = array.get(messageId);
                        if (newValue > messageObject.messageOwner.views) {
                            messageObject.messageOwner.views = newValue;
                            updated = true;
                        }
                    }
                }
                if (updated) {
                    updateVisibleRows();
                }
            }
        }
    }

    private void updateSearchButtons(int mask) {
        boolean z = true;
        float f = 1.0f;
        if (this.searchUpItem != null) {
            float f2;
            this.searchUpItem.setEnabled((mask & 1) != 0);
            ActionBarMenuItem actionBarMenuItem = this.searchDownItem;
            if ((mask & 2) == 0) {
                z = false;
            }
            actionBarMenuItem.setEnabled(z);
            View view = this.searchUpItem;
            if (this.searchUpItem.isEnabled()) {
                f2 = 1.0f;
            } else {
                f2 = 0.6f;
            }
            ViewProxy.setAlpha(view, f2);
            View view2 = this.searchDownItem;
            if (!this.searchDownItem.isEnabled()) {
                f = 0.6f;
            }
            ViewProxy.setAlpha(view2, f);
        }
    }

    public void onTransitionAnimationStart(boolean isOpen, boolean backward) {
        if (isOpen) {
            NotificationCenter.getInstance().setAnimationInProgress(true);
            this.openAnimationEnded = false;
        }
    }

    public void onTransitionAnimationEnd(boolean isOpen, boolean backward) {
        if (isOpen) {
            NotificationCenter.getInstance().setAnimationInProgress(false);
            this.openAnimationEnded = true;
            int count = this.chatListView.getChildCount();
            for (int a = 0; a < count; a++) {
                View view = this.chatListView.getChildAt(a);
                if (view instanceof ChatMediaCell) {
                    ((ChatMediaCell) view).setAllowedToSetPhoto(true);
                }
            }
            if (this.currentUser != null) {
                MessagesController.getInstance().loadFullUser(this.currentUser, this.classGuid);
            }
        }
    }

    protected void onDialogDismiss(Dialog dialog) {
        if (this.closeChatDialog != null && dialog == this.closeChatDialog) {
            MessagesController.getInstance().deleteDialog(this.dialog_id, 0);
            finishFragment();
        }
    }

    private void updateBottomOverlay() {
        if (this.bottomOverlayChatText != null) {
            if (this.currentChat != null) {
                if (!ChatObject.isChannel(this.currentChat) || this.currentChat.megagroup || (this.currentChat instanceof TL_channelForbidden)) {
                    this.bottomOverlayChatText.setText(LocaleController.getString("DeleteThisGroup", C0553R.string.DeleteThisGroup));
                } else if (ChatObject.isNotInChat(this.currentChat)) {
                    this.bottomOverlayChatText.setText(LocaleController.getString("ChannelJoin", C0553R.string.ChannelJoin));
                } else if (MessagesController.getInstance().isDialogMuted(this.dialog_id)) {
                    this.bottomOverlayChatText.setText(LocaleController.getString("ChannelUnmute", C0553R.string.ChannelUnmute));
                } else {
                    this.bottomOverlayChatText.setText(LocaleController.getString("ChannelMute", C0553R.string.ChannelMute));
                }
            } else if (this.userBlocked) {
                if (this.currentUser.bot) {
                    this.bottomOverlayChatText.setText(LocaleController.getString("BotUnblock", C0553R.string.BotUnblock));
                } else {
                    this.bottomOverlayChatText.setText(LocaleController.getString("Unblock", C0553R.string.Unblock));
                }
                if (this.botButtons != null) {
                    this.botButtons = null;
                    if (this.chatActivityEnterView != null) {
                        if (this.replyingMessageObject != null && this.botReplyButtons == this.replyingMessageObject) {
                            this.botReplyButtons = null;
                            showReplyPanel(false, null, null, null, false, true);
                        }
                        this.chatActivityEnterView.setButtons(this.botButtons, false);
                    }
                }
            } else if (this.botUser == null || !this.currentUser.bot) {
                this.bottomOverlayChatText.setText(LocaleController.getString("DeleteThisChat", C0553R.string.DeleteThisChat));
            } else {
                this.bottomOverlayChatText.setText(LocaleController.getString("BotStart", C0553R.string.BotStart));
                this.chatActivityEnterView.hidePopup(false);
                if (getParentActivity() != null) {
                    AndroidUtilities.hideKeyboard(getParentActivity().getCurrentFocus());
                }
            }
            if ((this.currentChat == null || (!ChatObject.isNotInChat(this.currentChat) && ChatObject.canWriteToChat(this.currentChat))) && (this.currentUser == null || !(UserObject.isDeleted(this.currentUser) || this.userBlocked))) {
                if (this.botUser == null || !this.currentUser.bot) {
                    this.chatActivityEnterView.setVisibility(0);
                    this.bottomOverlayChat.setVisibility(4);
                } else {
                    this.bottomOverlayChat.setVisibility(0);
                    this.chatActivityEnterView.setVisibility(4);
                }
                this.muteItem.setVisibility(0);
                return;
            }
            this.bottomOverlayChat.setVisibility(0);
            this.muteItem.setVisibility(8);
            this.chatActivityEnterView.setFieldFocused(false);
            this.chatActivityEnterView.setVisibility(4);
        }
    }

    private void updateSpamView() {
        if (this.reportSpamView != null) {
            this.reportSpamUser = null;
            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", 0);
            if (!this.messages.isEmpty() && !preferences.getBoolean("spam_" + this.dialog_id, false)) {
                int count;
                int a;
                if (this.currentChat != null) {
                    count = this.messages.size() - 1;
                    for (a = count; a >= Math.max(count - 50, 0); a--) {
                        MessageObject messageObject = (MessageObject) this.messages.get(a);
                        if (messageObject.isOut()) {
                            this.reportSpamUser = null;
                        } else if (messageObject.messageOwner.action instanceof TL_messageActionChatCreate) {
                            this.reportSpamUser = MessagesController.getInstance().getUser(Integer.valueOf(messageObject.messageOwner.from_id));
                        } else if ((messageObject.messageOwner.action instanceof TL_messageActionChatAddUser) && (messageObject.messageOwner.action.user_id == UserConfig.getClientUserId() || messageObject.messageOwner.action.users.contains(Integer.valueOf(UserConfig.getClientUserId())))) {
                            this.reportSpamUser = MessagesController.getInstance().getUser(Integer.valueOf(messageObject.messageOwner.from_id));
                        }
                    }
                    if (!(this.reportSpamUser == null || ContactsController.getInstance().contactsDict.get(this.reportSpamUser.id) == null)) {
                        this.reportSpamUser = null;
                    }
                    if (this.reportSpamUser != null) {
                        this.addToContactsButton.setVisibility(8);
                        this.reportSpamButton.setPadding(AndroidUtilities.dp(50.0f), 0, AndroidUtilities.dp(50.0f), 0);
                        this.reportSpamContainer.setLayoutParams(LayoutHelper.createLinear(-1, -1, 1.0f, 51, 0, 0, 0, AndroidUtilities.dp(1.0f)));
                    }
                } else if (this.currentUser != null) {
                    if (!(this.currentUser.bot || this.currentUser.id / 1000 == 333 || this.currentUser.id / 1000 == 777 || UserObject.isDeleted(this.currentUser) || this.userBlocked || ContactsController.getInstance().isLoadingContacts() || (this.currentUser.phone != null && this.currentUser.phone.length() != 0 && ContactsController.getInstance().contactsDict.get(this.currentUser.id) != null))) {
                        if (this.currentUser.phone == null || this.currentUser.phone.length() == 0) {
                            this.reportSpamButton.setPadding(AndroidUtilities.dp(50.0f), 0, AndroidUtilities.dp(50.0f), 0);
                            this.addToContactsButton.setVisibility(8);
                            this.reportSpamContainer.setLayoutParams(LayoutHelper.createLinear(-1, -1, 1.0f, 51, 0, 0, 0, AndroidUtilities.dp(1.0f)));
                        } else {
                            this.reportSpamButton.setPadding(AndroidUtilities.dp(4.0f), 0, AndroidUtilities.dp(50.0f), 0);
                            this.addToContactsButton.setVisibility(0);
                            this.reportSpamContainer.setLayoutParams(LayoutHelper.createLinear(-1, -1, 0.5f, 51, 0, 0, 0, AndroidUtilities.dp(1.0f)));
                        }
                        this.reportSpamUser = this.currentUser;
                    }
                    if (this.reportSpamUser != null) {
                        count = this.messages.size() - 1;
                        for (a = count; a >= Math.max(count - 50, 0); a--) {
                            if (((MessageObject) this.messages.get(a)).isOut()) {
                                this.reportSpamUser = null;
                                break;
                            }
                        }
                    }
                }
            }
            if (this.reportSpamUser != null) {
                if (this.reportSpamView.getVisibility() != 0) {
                    this.reportSpamView.setVisibility(0);
                    this.reportSpamView.setTag(Integer.valueOf(1));
                    this.chatListView.setTopGlowOffset(AndroidUtilities.dp(48.0f));
                    this.chatListView.setPadding(0, AndroidUtilities.dp(52.0f), 0, AndroidUtilities.dp(3.0f));
                }
            } else if (this.reportSpamView.getVisibility() != 8) {
                this.reportSpamView.setVisibility(8);
                this.reportSpamView.setTag(null);
                this.chatListView.setPadding(0, AndroidUtilities.dp(4.0f), 0, AndroidUtilities.dp(3.0f));
                this.chatListView.setTopGlowOffset(0);
                this.chatLayoutManager.scrollToPositionWithOffset(this.messages.size() - 1, -100000 - this.chatListView.getPaddingTop());
            }
        }
    }

    private void updateContactStatus() {
        if (this.addContactItem != null) {
            if (this.currentUser == null) {
                this.addContactItem.setVisibility(8);
                return;
            }
            User user = MessagesController.getInstance().getUser(Integer.valueOf(this.currentUser.id));
            if (user != null) {
                this.currentUser = user;
            }
            if ((this.currentEncryptedChat != null && !(this.currentEncryptedChat instanceof TL_encryptedChat)) || this.currentUser.id / 1000 == 333 || this.currentUser.id / 1000 == 777 || UserObject.isDeleted(this.currentUser) || ContactsController.getInstance().isLoadingContacts() || (this.currentUser.phone != null && this.currentUser.phone.length() != 0 && ContactsController.getInstance().contactsDict.get(this.currentUser.id) != null && (ContactsController.getInstance().contactsDict.size() != 0 || !ContactsController.getInstance().isLoadingContacts()))) {
                this.addContactItem.setVisibility(8);
                this.reportSpamView.setVisibility(8);
                this.chatListView.setTopGlowOffset(0);
                this.chatListView.setPadding(0, AndroidUtilities.dp(4.0f), 0, AndroidUtilities.dp(3.0f));
                return;
            }
            this.addContactItem.setVisibility(0);
            if (this.reportSpamView.getTag() != null) {
                this.reportSpamView.setVisibility(0);
                this.chatListView.setPadding(0, AndroidUtilities.dp(52.0f), 0, AndroidUtilities.dp(3.0f));
                this.chatListView.setTopGlowOffset(AndroidUtilities.dp(48.0f));
            }
            if (this.currentUser.phone == null || this.currentUser.phone.length() == 0) {
                this.addContactItem.setText(LocaleController.getString("ShareMyContactInfo", C0553R.string.ShareMyContactInfo));
                this.addToContactsButton.setVisibility(8);
                this.reportSpamContainer.setLayoutParams(LayoutHelper.createLinear(-1, -1, 1.0f, 51, 0, 0, 0, AndroidUtilities.dp(1.0f)));
                return;
            }
            this.addContactItem.setText(LocaleController.getString("AddToContacts", C0553R.string.AddToContacts));
            this.addToContactsButton.setVisibility(0);
            this.reportSpamContainer.setLayoutParams(LayoutHelper.createLinear(-1, -1, 0.5f, 51, 0, 0, 0, AndroidUtilities.dp(1.0f)));
        }
    }

    public void onResume() {
        super.onResume();
        AndroidUtilities.requestAdjustResize(getParentActivity(), this.classGuid);
        checkActionBarMenu();
        if (!(this.replyImageLocation == null || this.replyImageView == null)) {
            this.replyImageView.setImage(this.replyImageLocation, "50_50", (Drawable) null);
        }
        NotificationsController.getInstance().setOpennedDialogId(this.dialog_id);
        if (this.scrollToTopOnResume) {
            if (!this.scrollToTopUnReadOnResume || this.scrollToMessage == null) {
                moveScrollToLastMessage();
            } else if (this.chatListView != null) {
                int yOffset;
                if (this.scrollToMessagePosition == -9000) {
                    yOffset = Math.max(0, (this.chatListView.getHeight() - this.scrollToMessage.getApproximateHeight()) / 2);
                } else if (this.scrollToMessagePosition == -10000) {
                    yOffset = 0;
                } else {
                    yOffset = this.scrollToMessagePosition;
                }
                this.chatLayoutManager.scrollToPositionWithOffset(this.messages.size() - this.messages.indexOf(this.scrollToMessage), ((-this.chatListView.getPaddingTop()) - AndroidUtilities.dp(7.0f)) + yOffset);
            }
            this.scrollToTopUnReadOnResume = false;
            this.scrollToTopOnResume = false;
            this.scrollToMessage = null;
        }
        this.paused = false;
        if (this.readWhenResume && !this.messages.isEmpty()) {
            Iterator i$ = this.messages.iterator();
            while (i$.hasNext()) {
                MessageObject messageObject = (MessageObject) i$.next();
                if (!messageObject.isUnread() && !messageObject.isOut()) {
                    break;
                } else if (!messageObject.isOut()) {
                    messageObject.setIsRead();
                }
            }
            this.readWhenResume = false;
            MessagesController.getInstance().markDialogAsRead(this.dialog_id, ((MessageObject) this.messages.get(0)).getId(), this.readWithMid, this.readWithDate, true, false);
        }
        checkScrollForLoad();
        if (this.wasPaused) {
            this.wasPaused = false;
            if (this.chatAdapter != null) {
                this.chatAdapter.notifyDataSetChanged();
            }
        }
        fixLayout(true);
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0);
        if (this.chatActivityEnterView.getFieldText() == null) {
            String lastMessageText = preferences.getString("dialog_" + this.dialog_id, null);
            if (lastMessageText != null) {
                preferences.edit().remove("dialog_" + this.dialog_id).commit();
                this.chatActivityEnterView.setFieldText(lastMessageText);
                if (getArguments().getBoolean("hasUrl", false)) {
                    this.chatActivityEnterView.setSelection(lastMessageText.indexOf(10) + 1);
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        public void run() {
                            if (ChatActivity.this.chatActivityEnterView != null) {
                                ChatActivity.this.chatActivityEnterView.setFieldFocused(true);
                                ChatActivity.this.chatActivityEnterView.openKeyboard();
                            }
                        }
                    }, 700);
                }
            }
        } else {
            preferences.edit().remove("dialog_" + this.dialog_id).commit();
        }
        if (this.replyingMessageObject == null) {
            String lastReplyMessage = preferences.getString("reply_" + this.dialog_id, null);
            if (!(lastReplyMessage == null || lastReplyMessage.length() == 0)) {
                preferences.edit().remove("reply_" + this.dialog_id).commit();
                try {
                    byte[] bytes = Base64.decode(lastReplyMessage, 0);
                    if (bytes != null) {
                        SerializedData data = new SerializedData(bytes);
                        Message message = Message.TLdeserialize(data, data.readInt32(false), false);
                        if (message != null) {
                            this.replyingMessageObject = new MessageObject(message, MessagesController.getInstance().getUsers(), false);
                            showReplyPanel(true, this.replyingMessageObject, null, null, false, false);
                        }
                    }
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
        } else {
            preferences.edit().remove("reply_" + this.dialog_id).commit();
        }
        if (this.bottomOverlayChat.getVisibility() != 0) {
            this.chatActivityEnterView.setFieldFocused(true);
        }
        this.chatActivityEnterView.onResume();
        if (this.currentEncryptedChat != null) {
            this.chatEnterTime = System.currentTimeMillis();
            this.chatLeaveTime = 0;
        }
        if (this.startVideoEdit != null) {
            AndroidUtilities.runOnUIThread(new Runnable() {
                public void run() {
                    ChatActivity.this.openVideoEditor(ChatActivity.this.startVideoEdit, false, false);
                    ChatActivity.this.startVideoEdit = null;
                }
            });
        }
        this.chatListView.setOnItemLongClickListener(this.onItemLongClickListener);
        this.chatListView.setOnItemClickListener(this.onItemClickListener);
        this.chatListView.setLongClickable(true);
    }

    public void onPause() {
        super.onPause();
        if (this.menuItem != null) {
            this.menuItem.closeSubMenu();
        }
        this.paused = true;
        this.wasPaused = true;
        NotificationsController.getInstance().setOpennedDialogId(0);
        if (this.chatActivityEnterView != null) {
            this.chatActivityEnterView.onPause();
            String text = this.chatActivityEnterView.getFieldText();
            if (text != null) {
                Editor editor = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0).edit();
                editor.putString("dialog_" + this.dialog_id, text);
                editor.commit();
            }
            this.chatActivityEnterView.setFieldFocused(false);
        }
        if (this.replyingMessageObject != null) {
            editor = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0).edit();
            try {
                SerializedData data = new SerializedData();
                this.replyingMessageObject.messageOwner.serializeToStream(data);
                String string = Base64.encodeToString(data.toByteArray(), 0);
                if (string.length() != 0) {
                    editor.putString("reply_" + this.dialog_id, string);
                }
            } catch (Throwable e) {
                editor.remove("reply_" + this.dialog_id);
                FileLog.m611e("tmessages", e);
            }
            editor.commit();
        }
        MessagesController.getInstance().cancelTyping(0, this.dialog_id);
        if (this.currentEncryptedChat != null) {
            this.chatLeaveTime = System.currentTimeMillis();
            updateInformationForScreenshotDetector();
        }
    }

    private void updateInformationForScreenshotDetector() {
        if (this.currentEncryptedChat != null) {
            ArrayList<Long> visibleMessages = new ArrayList();
            if (this.chatListView != null) {
                int count = this.chatListView.getChildCount();
                for (int a = 0; a < count; a++) {
                    View view = this.chatListView.getChildAt(a);
                    MessageObject object = null;
                    if (view instanceof ChatBaseCell) {
                        object = ((ChatBaseCell) view).getMessageObject();
                    }
                    if (!(object == null || object.getId() >= 0 || object.messageOwner.random_id == 0)) {
                        visibleMessages.add(Long.valueOf(object.messageOwner.random_id));
                    }
                }
            }
            MediaController.getInstance().setLastEncryptedChatParams(this.chatEnterTime, this.chatLeaveTime, this.currentEncryptedChat, visibleMessages);
        }
    }

    private void fixLayout(boolean resume) {
        if (this.avatarContainer != null) {
            this.avatarContainer.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
                public boolean onPreDraw() {
                    int i;
                    if (ChatActivity.this.avatarContainer != null) {
                        ChatActivity.this.avatarContainer.getViewTreeObserver().removeOnPreDrawListener(this);
                    }
                    if (AndroidUtilities.isTablet() || ApplicationLoader.applicationContext.getResources().getConfiguration().orientation != 2) {
                        ChatActivity.this.selectedMessagesCountTextView.setTextSize(20);
                    } else {
                        ChatActivity.this.selectedMessagesCountTextView.setTextSize(18);
                    }
                    int padding = (ActionBar.getCurrentActionBarHeight() - AndroidUtilities.dp(48.0f)) / 2;
                    if (ChatActivity.this.avatarContainer.getPaddingTop() != padding) {
                        ChatActivity.this.avatarContainer.setPadding(ChatActivity.this.avatarContainer.getPaddingLeft(), padding, ChatActivity.this.avatarContainer.getPaddingRight(), padding);
                    }
                    LayoutParams layoutParams = (LayoutParams) ChatActivity.this.avatarContainer.getLayoutParams();
                    int i2 = layoutParams.topMargin;
                    if (VERSION.SDK_INT >= 21) {
                        i = AndroidUtilities.statusBarHeight;
                    } else {
                        i = 0;
                    }
                    if (i2 != i) {
                        if (VERSION.SDK_INT >= 21) {
                            i = AndroidUtilities.statusBarHeight;
                        } else {
                            i = 0;
                        }
                        layoutParams.topMargin = i;
                        ChatActivity.this.avatarContainer.setLayoutParams(layoutParams);
                    }
                    if (!AndroidUtilities.isTablet()) {
                        return true;
                    }
                    if (AndroidUtilities.isSmallTablet() && ApplicationLoader.applicationContext.getResources().getConfiguration().orientation == 1) {
                        ChatActivity.this.actionBar.setBackButtonDrawable(new BackDrawable(false));
                        if (ChatActivity.this.playerView == null || ChatActivity.this.playerView.getParent() != null) {
                            return false;
                        }
                        ((ViewGroup) ChatActivity.this.fragmentView).addView(ChatActivity.this.playerView, LayoutHelper.createFrame(-1, 39.0f, 51, 0.0f, -36.0f, 0.0f, 0.0f));
                        return false;
                    }
                    ChatActivity.this.actionBar.setBackButtonDrawable(new BackDrawable(true));
                    if (ChatActivity.this.playerView == null || ChatActivity.this.playerView.getParent() == null) {
                        return false;
                    }
                    ChatActivity.this.fragmentView.setPadding(0, 0, 0, 0);
                    ((ViewGroup) ChatActivity.this.fragmentView).removeView(ChatActivity.this.playerView);
                    return false;
                }
            });
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        fixLayout(false);
    }

    private void switchImportantMode(MessageObject searchBeforeMessage) {
        int a;
        View child;
        MessageObject message;
        int count = this.chatListView.getChildCount();
        MessageObject messageObject = null;
        if (searchBeforeMessage == null) {
            for (a = 0; a <= count; a++) {
                child = this.chatListView.getChildAt(a);
                message = null;
                if (child instanceof ChatBaseCell) {
                    message = ((ChatBaseCell) child).getMessageObject();
                } else if (child instanceof ChatActionCell) {
                    message = ((ChatActionCell) child).getMessageObject();
                }
                if (message != null && message.getId() > 0) {
                    if (message.isImportant()) {
                        messageObject = message;
                        break;
                    } else if (searchBeforeMessage == null) {
                        searchBeforeMessage = message;
                    }
                }
            }
        }
        if (messageObject == null) {
            int index = this.messages.indexOf(searchBeforeMessage);
            if (index >= 0) {
                for (a = index + 1; a < this.messages.size(); a++) {
                    message = (MessageObject) this.messages.get(a);
                    if (message.getId() > 0 && message.isImportant()) {
                        messageObject = message;
                        break;
                    }
                }
            }
        }
        if (messageObject != null) {
            this.scrollToMessagePosition = -10000;
            for (a = 0; a < count; a++) {
                child = this.chatListView.getChildAt(a);
                message = null;
                if (child instanceof ChatBaseCell) {
                    message = ((ChatBaseCell) child).getMessageObject();
                } else if (child instanceof ChatActionCell) {
                    message = ((ChatActionCell) child).getMessageObject();
                }
                if (message == messageObject) {
                    this.scrollToMessagePosition = child.getTop() + AndroidUtilities.dp(7.0f);
                    break;
                }
            }
            if (this.scrollToMessagePosition == -10000) {
                this.scrollToMessagePosition = this.chatListView.getPaddingTop();
            }
        }
        this.radioButton.setChecked(!this.radioButton.isChecked(), true);
        this.channelMessagesImportant = this.radioButton.isChecked() ? 1 : 2;
        ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0).edit().putInt("important_" + this.dialog_id, this.channelMessagesImportant).commit();
        this.waitingForImportantLoad = true;
        this.waitingForLoad.add(Integer.valueOf(this.lastLoadIndex));
        if (messageObject != null) {
            this.startLoadFromMessageId = messageObject.getId();
            MessagesController instance = MessagesController.getInstance();
            long j = this.dialog_id;
            int i = AndroidUtilities.isTablet() ? 30 : 20;
            int i2 = this.startLoadFromMessageId;
            int i3 = this.classGuid;
            int i4 = this.channelMessagesImportant;
            int i5 = this.lastLoadIndex;
            this.lastLoadIndex = i5 + 1;
            instance.loadMessages(j, i, i2, true, 0, i3, 3, 0, i4, i5);
            return;
        }
        instance = MessagesController.getInstance();
        j = this.dialog_id;
        i3 = this.classGuid;
        i4 = this.channelMessagesImportant;
        i5 = this.lastLoadIndex;
        this.lastLoadIndex = i5 + 1;
        instance.loadMessages(j, 30, 0, true, 0, i3, 0, 0, i4, i5);
    }

    private void createMenu(View v, boolean single) {
        if (!this.actionBar.isActionModeShowed()) {
            MessageObject message = null;
            if (v instanceof ChatBaseCell) {
                message = ((ChatBaseCell) v).getMessageObject();
            } else if (v instanceof ChatActionCell) {
                message = ((ChatActionCell) v).getMessageObject();
            }
            if (message != null) {
                int type = getMessageType(message);
                if (this.channelMessagesImportant == 2 && message.getId() == 0 && message.contentType == 4 && message.type == 10 && message.messageOwner.from_id != 0) {
                    switchImportantMode(message);
                    return;
                }
                int a;
                this.selectedObject = null;
                this.forwaringMessage = null;
                for (a = 1; a >= 0; a--) {
                    this.selectedMessagesCanCopyIds[a].clear();
                    this.selectedMessagesIds[a].clear();
                }
                this.cantDeleteMessagesCount = 0;
                this.actionBar.hideActionMode();
                boolean allowChatActions = true;
                if ((type == 1 && message.getDialogId() == this.mergeDialogId) || message.getId() < 0 || this.isBroadcast || (this.currentChat != null && (ChatObject.isNotInChat(this.currentChat) || !(!ChatObject.isChannel(this.currentChat) || this.currentChat.creator || this.currentChat.editor || this.currentChat.megagroup)))) {
                    allowChatActions = false;
                }
                if (!single && type >= 2 && type != 20) {
                    this.actionBar.showActionMode();
                    if (VERSION.SDK_INT >= 11) {
                        AnimatorSetProxy animatorSet = new AnimatorSetProxy();
                        ArrayList animators = new ArrayList();
                        for (a = 0; a < this.actionModeViews.size(); a++) {
                            View view = (View) this.actionModeViews.get(a);
                            AndroidUtilities.clearDrawableAnimation(view);
                            animators.add(ObjectAnimatorProxy.ofFloat(view, "scaleY", 0.1f, 1.0f));
                        }
                        animatorSet.playTogether(animators);
                        animatorSet.setDuration(250);
                        animatorSet.start();
                    }
                    addToSelectedMessages(message);
                    this.selectedMessagesCountTextView.setNumber(1, false);
                    updateVisibleRows();
                } else if (type >= 0) {
                    this.selectedObject = message;
                    if (getParentActivity() != null) {
                        Builder builder = new Builder(getParentActivity());
                        ArrayList<CharSequence> items = new ArrayList();
                        final ArrayList<Integer> options = new ArrayList();
                        if (type == 0) {
                            items.add(LocaleController.getString("Retry", C0553R.string.Retry));
                            options.add(Integer.valueOf(0));
                            items.add(LocaleController.getString("Delete", C0553R.string.Delete));
                            options.add(Integer.valueOf(1));
                        } else if (type == 1) {
                            if (this.currentChat != null && !this.isBroadcast) {
                                if (allowChatActions) {
                                    items.add(LocaleController.getString("Reply", C0553R.string.Reply));
                                    options.add(Integer.valueOf(8));
                                }
                                if (message.canDeleteMessage(this.currentChat)) {
                                    items.add(LocaleController.getString("Delete", C0553R.string.Delete));
                                    options.add(Integer.valueOf(1));
                                }
                            } else if (message.canDeleteMessage(this.currentChat)) {
                                items.add(LocaleController.getString("Delete", C0553R.string.Delete));
                                options.add(Integer.valueOf(1));
                            }
                        } else if (type == 20) {
                            items.add(LocaleController.getString("Retry", C0553R.string.Retry));
                            options.add(Integer.valueOf(0));
                            items.add(LocaleController.getString("Copy", C0553R.string.Copy));
                            options.add(Integer.valueOf(3));
                            items.add(LocaleController.getString("Delete", C0553R.string.Delete));
                            options.add(Integer.valueOf(1));
                        } else if (this.currentEncryptedChat == null) {
                            if (allowChatActions) {
                                items.add(LocaleController.getString("Reply", C0553R.string.Reply));
                                options.add(Integer.valueOf(8));
                            }
                            if (type == 3) {
                                items.add(LocaleController.getString("Copy", C0553R.string.Copy));
                                options.add(Integer.valueOf(3));
                            } else if (type == 4) {
                                if (this.selectedObject.messageOwner.media instanceof TL_messageMediaDocument) {
                                    items.add(this.selectedObject.isMusic() ? LocaleController.getString("SaveToMusic", C0553R.string.SaveToMusic) : LocaleController.getString("SaveToDownloads", C0553R.string.SaveToDownloads));
                                    options.add(Integer.valueOf(10));
                                    items.add(LocaleController.getString("ShareFile", C0553R.string.ShareFile));
                                    options.add(Integer.valueOf(4));
                                } else {
                                    items.add(LocaleController.getString("SaveToGallery", C0553R.string.SaveToGallery));
                                    options.add(Integer.valueOf(4));
                                }
                            } else if (type == 5) {
                                items.add(LocaleController.getString("ApplyLocalizationFile", C0553R.string.ApplyLocalizationFile));
                                options.add(Integer.valueOf(5));
                                items.add(LocaleController.getString("ShareFile", C0553R.string.ShareFile));
                                options.add(Integer.valueOf(4));
                            } else if (type == 6) {
                                items.add(LocaleController.getString("SaveToGallery", C0553R.string.SaveToGallery));
                                options.add(Integer.valueOf(7));
                                items.add(this.selectedObject.isMusic() ? LocaleController.getString("SaveToMusic", C0553R.string.SaveToMusic) : LocaleController.getString("SaveToDownloads", C0553R.string.SaveToDownloads));
                                options.add(Integer.valueOf(10));
                                items.add(LocaleController.getString("ShareFile", C0553R.string.ShareFile));
                                options.add(Integer.valueOf(6));
                            } else if (type == 7) {
                                items.add(LocaleController.getString("AddToStickers", C0553R.string.AddToStickers));
                                options.add(Integer.valueOf(9));
                            }
                            items.add(LocaleController.getString("Forward", C0553R.string.Forward));
                            options.add(Integer.valueOf(2));
                            if (message.canDeleteMessage(this.currentChat)) {
                                items.add(LocaleController.getString("Delete", C0553R.string.Delete));
                                options.add(Integer.valueOf(1));
                            }
                        } else {
                            if (type == 3) {
                                items.add(LocaleController.getString("Copy", C0553R.string.Copy));
                                options.add(Integer.valueOf(3));
                            } else if (type == 4) {
                                if (this.selectedObject.messageOwner.media instanceof TL_messageMediaDocument) {
                                    items.add(this.selectedObject.isMusic() ? LocaleController.getString("SaveToMusic", C0553R.string.SaveToMusic) : LocaleController.getString("SaveToDownloads", C0553R.string.SaveToDownloads));
                                    options.add(Integer.valueOf(10));
                                    items.add(LocaleController.getString("ShareFile", C0553R.string.ShareFile));
                                    options.add(Integer.valueOf(4));
                                } else {
                                    items.add(LocaleController.getString("SaveToGallery", C0553R.string.SaveToGallery));
                                    options.add(Integer.valueOf(4));
                                }
                            } else if (type == 5) {
                                items.add(LocaleController.getString("ApplyLocalizationFile", C0553R.string.ApplyLocalizationFile));
                                options.add(Integer.valueOf(5));
                            } else if (type == 7) {
                                items.add(LocaleController.getString("AddToStickers", C0553R.string.AddToStickers));
                                options.add(Integer.valueOf(9));
                            }
                            items.add(LocaleController.getString("Delete", C0553R.string.Delete));
                            options.add(Integer.valueOf(1));
                        }
                        if (!options.isEmpty()) {
                            builder.setItems((CharSequence[]) items.toArray(new CharSequence[items.size()]), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (ChatActivity.this.selectedObject != null && i >= 0 && i < options.size()) {
                                        ChatActivity.this.processSelectedOption(((Integer) options.get(i)).intValue());
                                    }
                                }
                            });
                            builder.setTitle(LocaleController.getString("Message", C0553R.string.Message));
                            showDialog(builder.create());
                        }
                    }
                }
            }
        }
    }

    private void processSelectedOption(int option) {
        if (this.selectedObject != null) {
            if (option == 0) {
                if (SendMessagesHelper.getInstance().retrySendMessage(this.selectedObject, false)) {
                    moveScrollToLastMessage();
                }
            } else if (option == 1) {
                if (getParentActivity() != null) {
                    MessageObject finalSelectedObject = this.selectedObject;
                    builder = new Builder(getParentActivity());
                    builder.setMessage(LocaleController.formatString("AreYouSureDeleteMessages", C0553R.string.AreYouSureDeleteMessages, LocaleController.formatPluralString("messages", 1)));
                    builder.setTitle(LocaleController.getString("AppName", C0553R.string.AppName));
                    final MessageObject messageObject = finalSelectedObject;
                    builder.setPositiveButton(LocaleController.getString("OK", C0553R.string.OK), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ArrayList<Integer> ids = new ArrayList();
                            ids.add(Integer.valueOf(messageObject.getId()));
                            ChatActivity.this.removeUnreadPlane();
                            ArrayList<Long> random_ids = null;
                            if (!(ChatActivity.this.currentEncryptedChat == null || messageObject.messageOwner.random_id == 0 || messageObject.type == 10)) {
                                random_ids = new ArrayList();
                                random_ids.add(Long.valueOf(messageObject.messageOwner.random_id));
                            }
                            MessagesController.getInstance().deleteMessages(ids, random_ids, ChatActivity.this.currentEncryptedChat, messageObject.messageOwner.to_id.channel_id);
                        }
                    });
                    builder.setNegativeButton(LocaleController.getString("Cancel", C0553R.string.Cancel), null);
                    showDialog(builder.create());
                } else {
                    return;
                }
            } else if (option == 2) {
                this.forwaringMessage = this.selectedObject;
                Bundle args = new Bundle();
                args.putBoolean("onlySelect", true);
                args.putInt("dialogsType", 1);
                BaseFragment dialogsActivity = new DialogsActivity(args);
                dialogsActivity.setDelegate(this);
                presentFragment(dialogsActivity);
            } else if (option == 3) {
                try {
                    if (VERSION.SDK_INT < 11) {
                        ((ClipboardManager) ApplicationLoader.applicationContext.getSystemService("clipboard")).setText(this.selectedObject.messageText);
                    } else {
                        ((android.content.ClipboardManager) ApplicationLoader.applicationContext.getSystemService("clipboard")).setPrimaryClip(ClipData.newPlainText(PlusShare.KEY_CALL_TO_ACTION_LABEL, this.selectedObject.messageText));
                    }
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            } else if (option == 4) {
                path = this.selectedObject.messageOwner.attachPath;
                if (!(path == null || path.length() <= 0 || new File(path).exists())) {
                    path = null;
                }
                if (path == null || path.length() == 0) {
                    path = FileLoader.getPathToMessage(this.selectedObject.messageOwner).toString();
                }
                if (this.selectedObject.type == 3 || this.selectedObject.type == 1) {
                    if (VERSION.SDK_INT < 23 || getParentActivity().checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") == 0) {
                        MediaController.saveFile(path, getParentActivity(), this.selectedObject.type == 3 ? 1 : 0, null);
                    } else {
                        getParentActivity().requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 4);
                        return;
                    }
                } else if (this.selectedObject.type == 8 || this.selectedObject.type == 9 || this.selectedObject.type == 14) {
                    r0 = new Intent("android.intent.action.SEND");
                    r0.setType(this.selectedObject.messageOwner.media.document.mime_type);
                    r0.putExtra("android.intent.extra.STREAM", Uri.fromFile(new File(path)));
                    getParentActivity().startActivityForResult(Intent.createChooser(r0, LocaleController.getString("ShareFile", C0553R.string.ShareFile)), 500);
                }
            } else if (option == 5) {
                File f;
                File locFile = null;
                if (!(this.selectedObject.messageOwner.attachPath == null || this.selectedObject.messageOwner.attachPath.length() == 0)) {
                    f = new File(this.selectedObject.messageOwner.attachPath);
                    if (f.exists()) {
                        locFile = f;
                    }
                }
                if (locFile == null) {
                    f = FileLoader.getPathToMessage(this.selectedObject.messageOwner);
                    if (f.exists()) {
                        locFile = f;
                    }
                }
                if (locFile != null) {
                    if (LocaleController.getInstance().applyLanguageFile(locFile)) {
                        presentFragment(new LanguageSelectActivity());
                    } else if (getParentActivity() != null) {
                        builder = new Builder(getParentActivity());
                        builder.setTitle(LocaleController.getString("AppName", C0553R.string.AppName));
                        builder.setMessage(LocaleController.getString("IncorrectLocalization", C0553R.string.IncorrectLocalization));
                        builder.setPositiveButton(LocaleController.getString("OK", C0553R.string.OK), null);
                        showDialog(builder.create());
                    } else {
                        return;
                    }
                }
            } else if (option == 6 || option == 7) {
                path = this.selectedObject.messageOwner.attachPath;
                if (!(path == null || path.length() <= 0 || new File(path).exists())) {
                    path = null;
                }
                if (path == null || path.length() == 0) {
                    path = FileLoader.getPathToMessage(this.selectedObject.messageOwner).toString();
                }
                if (this.selectedObject.type == 8 || this.selectedObject.type == 9 || this.selectedObject.type == 14) {
                    if (option == 6) {
                        r0 = new Intent("android.intent.action.SEND");
                        r0.setType(this.selectedObject.messageOwner.media.document.mime_type);
                        r0.putExtra("android.intent.extra.STREAM", Uri.fromFile(new File(path)));
                        getParentActivity().startActivityForResult(Intent.createChooser(r0, LocaleController.getString("ShareFile", C0553R.string.ShareFile)), 500);
                    } else if (VERSION.SDK_INT < 23 || getParentActivity().checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") == 0) {
                        MediaController.saveFile(path, getParentActivity(), 0, null);
                    } else {
                        getParentActivity().requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 4);
                        return;
                    }
                }
            } else if (option == 8) {
                showReplyPanel(true, this.selectedObject, null, null, false, true);
            } else if (option == 9) {
                StickersQuery.loadStickers((BaseFragment) this, this.selectedObject.getInputStickerSet());
            } else if (option == 10) {
                if (VERSION.SDK_INT < 23 || getParentActivity().checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") == 0) {
                    String fileName = FileLoader.getDocumentFileName(this.selectedObject.messageOwner.media.document);
                    if (fileName == null || fileName.length() == 0) {
                        fileName = this.selectedObject.getFileName();
                    }
                    path = this.selectedObject.messageOwner.attachPath;
                    if (!(path == null || path.length() <= 0 || new File(path).exists())) {
                        path = null;
                    }
                    if (path == null || path.length() == 0) {
                        path = FileLoader.getPathToMessage(this.selectedObject.messageOwner).toString();
                    }
                    MediaController.saveFile(path, getParentActivity(), this.selectedObject.isMusic() ? 3 : 2, fileName);
                } else {
                    getParentActivity().requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 4);
                    return;
                }
            }
            this.selectedObject = null;
        }
    }

    public void didSelectDialog(DialogsActivity activity, long did, boolean param) {
        if (this.dialog_id == 0) {
            return;
        }
        if (this.forwaringMessage != null || !this.selectedMessagesIds[0].isEmpty() || !this.selectedMessagesIds[1].isEmpty()) {
            ArrayList<MessageObject> fmessages = new ArrayList();
            if (this.forwaringMessage != null) {
                fmessages.add(this.forwaringMessage);
                this.forwaringMessage = null;
            } else {
                for (int a = 1; a >= 0; a--) {
                    ArrayList<Integer> arrayList = new ArrayList(this.selectedMessagesIds[a].keySet());
                    Collections.sort(arrayList);
                    for (int b = 0; b < arrayList.size(); b++) {
                        Integer id = (Integer) arrayList.get(b);
                        MessageObject message = (MessageObject) this.selectedMessagesIds[a].get(id);
                        if (message != null && id.intValue() > 0) {
                            fmessages.add(message);
                        }
                    }
                    this.selectedMessagesCanCopyIds[a].clear();
                    this.selectedMessagesIds[a].clear();
                }
                this.cantDeleteMessagesCount = 0;
                this.actionBar.hideActionMode();
            }
            if (did != this.dialog_id) {
                int lower_part = (int) did;
                if (lower_part != 0) {
                    Bundle args = new Bundle();
                    args.putBoolean("scrollToTopOnResume", this.scrollToTopOnResume);
                    if (lower_part > 0) {
                        args.putInt("user_id", lower_part);
                    } else if (lower_part < 0) {
                        args.putInt("chat_id", -lower_part);
                    }
                    ChatActivity chatActivity = new ChatActivity(args);
                    if (presentFragment(chatActivity, true)) {
                        chatActivity.showReplyPanel(true, null, fmessages, null, false, false);
                        if (!AndroidUtilities.isTablet()) {
                            removeSelfFromStack();
                            return;
                        }
                        return;
                    }
                    activity.finishFragment();
                    return;
                }
                activity.finishFragment();
                return;
            }
            activity.finishFragment();
            moveScrollToLastMessage();
            showReplyPanel(true, null, fmessages, null, false, AndroidUtilities.isTablet());
            if (AndroidUtilities.isTablet()) {
                this.actionBar.hideActionMode();
            }
            updateVisibleRows();
        }
    }

    public boolean onBackPressed() {
        if (this.actionBar.isActionModeShowed()) {
            for (int a = 1; a >= 0; a--) {
                this.selectedMessagesIds[a].clear();
                this.selectedMessagesCanCopyIds[a].clear();
            }
            this.actionBar.hideActionMode();
            this.cantDeleteMessagesCount = 0;
            updateVisibleRows();
            return false;
        } else if (!this.chatActivityEnterView.isPopupShowing()) {
            return true;
        } else {
            this.chatActivityEnterView.hidePopup(true);
            return false;
        }
    }

    public boolean isGoogleMapsInstalled() {
        try {
            ApplicationLoader.applicationContext.getPackageManager().getApplicationInfo("com.google.android.apps.maps", 0);
            return true;
        } catch (NameNotFoundException e) {
            if (getParentActivity() == null) {
                return false;
            }
            Builder builder = new Builder(getParentActivity());
            builder.setMessage("Install Google Maps?");
            builder.setCancelable(true);
            builder.setPositiveButton(LocaleController.getString("OK", C0553R.string.OK), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    try {
                        ChatActivity.this.getParentActivity().startActivityForResult(new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=com.google.android.apps.maps")), 500);
                    } catch (Throwable e) {
                        FileLog.m611e("tmessages", e);
                    }
                }
            });
            builder.setNegativeButton(LocaleController.getString("Cancel", C0553R.string.Cancel), null);
            showDialog(builder.create());
            return false;
        }
    }

    private void updateVisibleRows() {
        if (this.chatListView != null) {
            int count = this.chatListView.getChildCount();
            for (int a = 0; a < count; a++) {
                View view = this.chatListView.getChildAt(a);
                if (view instanceof ChatBaseCell) {
                    ChatBaseCell cell = (ChatBaseCell) view;
                    boolean disableSelection = false;
                    boolean selected = false;
                    if (this.actionBar.isActionModeShowed()) {
                        MessageObject messageObject = cell.getMessageObject();
                        if (this.selectedMessagesIds[messageObject.getDialogId() == this.dialog_id ? 0 : 1].containsKey(Integer.valueOf(messageObject.getId()))) {
                            view.setBackgroundColor(1714664933);
                            selected = true;
                        } else {
                            view.setBackgroundColor(0);
                        }
                        disableSelection = true;
                    } else {
                        view.setBackgroundColor(0);
                    }
                    cell.setMessageObject(cell.getMessageObject());
                    boolean z = !disableSelection;
                    boolean z2 = disableSelection && selected;
                    cell.setCheckPressed(z, z2);
                    if (this.highlightMessageId == ConnectionsManager.DEFAULT_DATACENTER_ID || cell.getMessageObject() == null || cell.getMessageObject().getId() != this.highlightMessageId) {
                        z2 = false;
                    } else {
                        z2 = true;
                    }
                    cell.setHighlighted(z2);
                }
            }
        }
    }

    private void alertUserOpenError(MessageObject message) {
        if (getParentActivity() != null) {
            Builder builder = new Builder(getParentActivity());
            builder.setTitle(LocaleController.getString("AppName", C0553R.string.AppName));
            builder.setPositiveButton(LocaleController.getString("OK", C0553R.string.OK), null);
            if (message.type == 3) {
                builder.setMessage(LocaleController.getString("NoPlayerInstalled", C0553R.string.NoPlayerInstalled));
            } else {
                builder.setMessage(LocaleController.formatString("NoHandleAppInstalled", C0553R.string.NoHandleAppInstalled, message.messageOwner.media.document.mime_type));
            }
            showDialog(builder.create());
        }
    }

    private void openSearchWithText(String text) {
        this.avatarContainer.setVisibility(8);
        this.headerItem.setVisibility(8);
        this.attachItem.setVisibility(8);
        this.searchItem.setVisibility(0);
        this.searchUpItem.setVisibility(0);
        this.searchDownItem.setVisibility(0);
        updateSearchButtons(0);
        this.openSearchKeyboard = text == null;
        this.searchItem.openSearch(this.openSearchKeyboard);
        if (text != null) {
            this.searchItem.getSearchField().setText(text);
            this.searchItem.getSearchField().setSelection(this.searchItem.getSearchField().length());
            MessagesSearchQuery.searchMessagesInChat(text, this.dialog_id, this.mergeDialogId, this.classGuid, 0);
        }
    }

    public void updatePhotoAtIndex(int index) {
    }

    public PlaceProviderObject getPlaceForPhoto(MessageObject messageObject, FileLocation fileLocation, int index) {
        if (messageObject == null) {
            return null;
        }
        int count = this.chatListView.getChildCount();
        for (int a = 0; a < count; a++) {
            MessageObject messageToOpen = null;
            ImageReceiver imageReceiver = null;
            View view = this.chatListView.getChildAt(a);
            MessageObject message;
            if (view instanceof ChatBaseCell) {
                ChatBaseCell cell = (ChatBaseCell) view;
                message = cell.getMessageObject();
                if (message != null && message.getId() == messageObject.getId()) {
                    messageToOpen = message;
                    imageReceiver = cell.getPhotoImage();
                }
            } else if (view instanceof ChatActionCell) {
                ChatActionCell cell2 = (ChatActionCell) view;
                message = cell2.getMessageObject();
                if (message != null && message.getId() == messageObject.getId()) {
                    messageToOpen = message;
                    imageReceiver = cell2.getPhotoImage();
                }
            }
            if (messageToOpen != null) {
                int[] coords = new int[2];
                view.getLocationInWindow(coords);
                PlaceProviderObject object = new PlaceProviderObject();
                object.viewX = coords[0];
                object.viewY = coords[1] - AndroidUtilities.statusBarHeight;
                object.parentView = this.chatListView;
                object.imageReceiver = imageReceiver;
                object.thumb = imageReceiver.getBitmap();
                object.radius = imageReceiver.getRoundRadius();
                return object;
            }
        }
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
}
