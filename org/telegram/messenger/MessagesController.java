package org.telegram.messenger;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.util.Base64;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.widget.Toast;
import com.google.android.gms.location.LocationStatusCodes;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import org.telegram.SQLite.SQLiteCursor;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.query.BotQuery;
import org.telegram.messenger.query.StickersQuery;
import org.telegram.messenger.support.widget.helper.ItemTouchHelper.Callback;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.SerializedData;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.BotInfo;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.ChatFull;
import org.telegram.tgnet.TLRPC.ChatParticipant;
import org.telegram.tgnet.TLRPC.ChatParticipants;
import org.telegram.tgnet.TLRPC.Dialog;
import org.telegram.tgnet.TLRPC.EncryptedChat;
import org.telegram.tgnet.TLRPC.EncryptedMessage;
import org.telegram.tgnet.TLRPC.ExportedChatInvite;
import org.telegram.tgnet.TLRPC.InputChannel;
import org.telegram.tgnet.TLRPC.InputFile;
import org.telegram.tgnet.TLRPC.InputPeer;
import org.telegram.tgnet.TLRPC.InputPhoto;
import org.telegram.tgnet.TLRPC.InputUser;
import org.telegram.tgnet.TLRPC.Message;
import org.telegram.tgnet.TLRPC.Peer;
import org.telegram.tgnet.TLRPC.PeerNotifySettings;
import org.telegram.tgnet.TLRPC.PhotoSize;
import org.telegram.tgnet.TLRPC.SendMessageAction;
import org.telegram.tgnet.TLRPC.TL_account_registerDevice;
import org.telegram.tgnet.TLRPC.TL_account_unregisterDevice;
import org.telegram.tgnet.TLRPC.TL_account_updateStatus;
import org.telegram.tgnet.TLRPC.TL_auth_logOut;
import org.telegram.tgnet.TLRPC.TL_boolTrue;
import org.telegram.tgnet.TLRPC.TL_botInfo;
import org.telegram.tgnet.TLRPC.TL_channel;
import org.telegram.tgnet.TLRPC.TL_channelForbidden;
import org.telegram.tgnet.TLRPC.TL_channelMessagesFilterEmpty;
import org.telegram.tgnet.TLRPC.TL_channelParticipantSelf;
import org.telegram.tgnet.TLRPC.TL_channelParticipantsRecent;
import org.telegram.tgnet.TLRPC.TL_channelRoleEditor;
import org.telegram.tgnet.TLRPC.TL_channels_channelParticipant;
import org.telegram.tgnet.TLRPC.TL_channels_channelParticipants;
import org.telegram.tgnet.TLRPC.TL_channels_createChannel;
import org.telegram.tgnet.TLRPC.TL_channels_deleteChannel;
import org.telegram.tgnet.TLRPC.TL_channels_deleteMessages;
import org.telegram.tgnet.TLRPC.TL_channels_editAbout;
import org.telegram.tgnet.TLRPC.TL_channels_editAdmin;
import org.telegram.tgnet.TLRPC.TL_channels_editPhoto;
import org.telegram.tgnet.TLRPC.TL_channels_editTitle;
import org.telegram.tgnet.TLRPC.TL_channels_getFullChannel;
import org.telegram.tgnet.TLRPC.TL_channels_getImportantHistory;
import org.telegram.tgnet.TLRPC.TL_channels_getMessages;
import org.telegram.tgnet.TLRPC.TL_channels_getParticipant;
import org.telegram.tgnet.TLRPC.TL_channels_getParticipants;
import org.telegram.tgnet.TLRPC.TL_channels_inviteToChannel;
import org.telegram.tgnet.TLRPC.TL_channels_joinChannel;
import org.telegram.tgnet.TLRPC.TL_channels_kickFromChannel;
import org.telegram.tgnet.TLRPC.TL_channels_leaveChannel;
import org.telegram.tgnet.TLRPC.TL_channels_readHistory;
import org.telegram.tgnet.TLRPC.TL_channels_toggleComments;
import org.telegram.tgnet.TLRPC.TL_channels_updateUsername;
import org.telegram.tgnet.TLRPC.TL_chat;
import org.telegram.tgnet.TLRPC.TL_chatFull;
import org.telegram.tgnet.TLRPC.TL_chatInviteEmpty;
import org.telegram.tgnet.TLRPC.TL_chatParticipant;
import org.telegram.tgnet.TLRPC.TL_chatParticipants;
import org.telegram.tgnet.TLRPC.TL_chatPhotoEmpty;
import org.telegram.tgnet.TLRPC.TL_config;
import org.telegram.tgnet.TLRPC.TL_contactBlocked;
import org.telegram.tgnet.TLRPC.TL_contactLinkContact;
import org.telegram.tgnet.TLRPC.TL_contacts_block;
import org.telegram.tgnet.TLRPC.TL_contacts_getBlocked;
import org.telegram.tgnet.TLRPC.TL_contacts_resolveUsername;
import org.telegram.tgnet.TLRPC.TL_contacts_resolvedPeer;
import org.telegram.tgnet.TLRPC.TL_contacts_unblock;
import org.telegram.tgnet.TLRPC.TL_dialog;
import org.telegram.tgnet.TLRPC.TL_dialogChannel;
import org.telegram.tgnet.TLRPC.TL_disabledFeature;
import org.telegram.tgnet.TLRPC.TL_encryptedChat;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_inputChannel;
import org.telegram.tgnet.TLRPC.TL_inputChannelEmpty;
import org.telegram.tgnet.TLRPC.TL_inputChatPhotoEmpty;
import org.telegram.tgnet.TLRPC.TL_inputChatUploadedPhoto;
import org.telegram.tgnet.TLRPC.TL_inputEncryptedChat;
import org.telegram.tgnet.TLRPC.TL_inputGeoPointEmpty;
import org.telegram.tgnet.TLRPC.TL_inputPeerChannel;
import org.telegram.tgnet.TLRPC.TL_inputPeerChat;
import org.telegram.tgnet.TLRPC.TL_inputPeerEmpty;
import org.telegram.tgnet.TLRPC.TL_inputPeerUser;
import org.telegram.tgnet.TLRPC.TL_inputPhotoCropAuto;
import org.telegram.tgnet.TLRPC.TL_inputPhotoEmpty;
import org.telegram.tgnet.TLRPC.TL_inputUser;
import org.telegram.tgnet.TLRPC.TL_inputUserEmpty;
import org.telegram.tgnet.TLRPC.TL_inputUserSelf;
import org.telegram.tgnet.TLRPC.TL_message;
import org.telegram.tgnet.TLRPC.TL_messageActionChannelCreate;
import org.telegram.tgnet.TLRPC.TL_messageActionChatAddUser;
import org.telegram.tgnet.TLRPC.TL_messageActionChatDeleteUser;
import org.telegram.tgnet.TLRPC.TL_messageActionChatMigrateTo;
import org.telegram.tgnet.TLRPC.TL_messageActionCreatedBroadcastList;
import org.telegram.tgnet.TLRPC.TL_messageActionLoginUnknownLocation;
import org.telegram.tgnet.TLRPC.TL_messageActionUserJoined;
import org.telegram.tgnet.TLRPC.TL_messageGroup;
import org.telegram.tgnet.TLRPC.TL_messageMediaEmpty;
import org.telegram.tgnet.TLRPC.TL_messageMediaUnsupported;
import org.telegram.tgnet.TLRPC.TL_messageMediaWebPage;
import org.telegram.tgnet.TLRPC.TL_messageService;
import org.telegram.tgnet.TLRPC.TL_messages_addChatUser;
import org.telegram.tgnet.TLRPC.TL_messages_affectedHistory;
import org.telegram.tgnet.TLRPC.TL_messages_affectedMessages;
import org.telegram.tgnet.TLRPC.TL_messages_channelMessages;
import org.telegram.tgnet.TLRPC.TL_messages_chatFull;
import org.telegram.tgnet.TLRPC.TL_messages_createChat;
import org.telegram.tgnet.TLRPC.TL_messages_deleteChatUser;
import org.telegram.tgnet.TLRPC.TL_messages_deleteHistory;
import org.telegram.tgnet.TLRPC.TL_messages_deleteMessages;
import org.telegram.tgnet.TLRPC.TL_messages_editChatAdmin;
import org.telegram.tgnet.TLRPC.TL_messages_editChatPhoto;
import org.telegram.tgnet.TLRPC.TL_messages_editChatTitle;
import org.telegram.tgnet.TLRPC.TL_messages_getDialogs;
import org.telegram.tgnet.TLRPC.TL_messages_getFullChat;
import org.telegram.tgnet.TLRPC.TL_messages_getHistory;
import org.telegram.tgnet.TLRPC.TL_messages_getMessages;
import org.telegram.tgnet.TLRPC.TL_messages_getMessagesViews;
import org.telegram.tgnet.TLRPC.TL_messages_migrateChat;
import org.telegram.tgnet.TLRPC.TL_messages_readEncryptedHistory;
import org.telegram.tgnet.TLRPC.TL_messages_readHistory;
import org.telegram.tgnet.TLRPC.TL_messages_readMessageContents;
import org.telegram.tgnet.TLRPC.TL_messages_receivedQueue;
import org.telegram.tgnet.TLRPC.TL_messages_setEncryptedTyping;
import org.telegram.tgnet.TLRPC.TL_messages_setTyping;
import org.telegram.tgnet.TLRPC.TL_messages_startBot;
import org.telegram.tgnet.TLRPC.TL_messages_toggleChatAdmins;
import org.telegram.tgnet.TLRPC.TL_notifyPeer;
import org.telegram.tgnet.TLRPC.TL_peerChannel;
import org.telegram.tgnet.TLRPC.TL_peerChat;
import org.telegram.tgnet.TLRPC.TL_peerNotifySettings;
import org.telegram.tgnet.TLRPC.TL_peerNotifySettingsEmpty;
import org.telegram.tgnet.TLRPC.TL_peerUser;
import org.telegram.tgnet.TLRPC.TL_photoEmpty;
import org.telegram.tgnet.TLRPC.TL_photos_deletePhotos;
import org.telegram.tgnet.TLRPC.TL_photos_getUserPhotos;
import org.telegram.tgnet.TLRPC.TL_photos_photo;
import org.telegram.tgnet.TLRPC.TL_photos_updateProfilePhoto;
import org.telegram.tgnet.TLRPC.TL_photos_uploadProfilePhoto;
import org.telegram.tgnet.TLRPC.TL_privacyKeyStatusTimestamp;
import org.telegram.tgnet.TLRPC.TL_replyKeyboardHide;
import org.telegram.tgnet.TLRPC.TL_sendMessageCancelAction;
import org.telegram.tgnet.TLRPC.TL_sendMessageRecordAudioAction;
import org.telegram.tgnet.TLRPC.TL_sendMessageRecordVideoAction;
import org.telegram.tgnet.TLRPC.TL_sendMessageTypingAction;
import org.telegram.tgnet.TLRPC.TL_sendMessageUploadAudioAction;
import org.telegram.tgnet.TLRPC.TL_sendMessageUploadDocumentAction;
import org.telegram.tgnet.TLRPC.TL_sendMessageUploadPhotoAction;
import org.telegram.tgnet.TLRPC.TL_sendMessageUploadVideoAction;
import org.telegram.tgnet.TLRPC.TL_updateChannel;
import org.telegram.tgnet.TLRPC.TL_updateChannelGroup;
import org.telegram.tgnet.TLRPC.TL_updateChannelMessageViews;
import org.telegram.tgnet.TLRPC.TL_updateChannelTooLong;
import org.telegram.tgnet.TLRPC.TL_updateChatAdmins;
import org.telegram.tgnet.TLRPC.TL_updateChatParticipantAdd;
import org.telegram.tgnet.TLRPC.TL_updateChatParticipantAdmin;
import org.telegram.tgnet.TLRPC.TL_updateChatParticipantDelete;
import org.telegram.tgnet.TLRPC.TL_updateChatParticipants;
import org.telegram.tgnet.TLRPC.TL_updateChatUserTyping;
import org.telegram.tgnet.TLRPC.TL_updateContactLink;
import org.telegram.tgnet.TLRPC.TL_updateContactRegistered;
import org.telegram.tgnet.TLRPC.TL_updateDcOptions;
import org.telegram.tgnet.TLRPC.TL_updateDeleteChannelMessages;
import org.telegram.tgnet.TLRPC.TL_updateDeleteMessages;
import org.telegram.tgnet.TLRPC.TL_updateEncryptedChatTyping;
import org.telegram.tgnet.TLRPC.TL_updateEncryptedMessagesRead;
import org.telegram.tgnet.TLRPC.TL_updateEncryption;
import org.telegram.tgnet.TLRPC.TL_updateMessageID;
import org.telegram.tgnet.TLRPC.TL_updateNewAuthorization;
import org.telegram.tgnet.TLRPC.TL_updateNewChannelMessage;
import org.telegram.tgnet.TLRPC.TL_updateNewEncryptedMessage;
import org.telegram.tgnet.TLRPC.TL_updateNewGeoChatMessage;
import org.telegram.tgnet.TLRPC.TL_updateNewMessage;
import org.telegram.tgnet.TLRPC.TL_updateNewStickerSet;
import org.telegram.tgnet.TLRPC.TL_updateNotifySettings;
import org.telegram.tgnet.TLRPC.TL_updatePrivacy;
import org.telegram.tgnet.TLRPC.TL_updateReadChannelInbox;
import org.telegram.tgnet.TLRPC.TL_updateReadHistoryInbox;
import org.telegram.tgnet.TLRPC.TL_updateReadHistoryOutbox;
import org.telegram.tgnet.TLRPC.TL_updateReadMessagesContents;
import org.telegram.tgnet.TLRPC.TL_updateServiceNotification;
import org.telegram.tgnet.TLRPC.TL_updateShort;
import org.telegram.tgnet.TLRPC.TL_updateShortChatMessage;
import org.telegram.tgnet.TLRPC.TL_updateShortMessage;
import org.telegram.tgnet.TLRPC.TL_updateStickerSets;
import org.telegram.tgnet.TLRPC.TL_updateStickerSetsOrder;
import org.telegram.tgnet.TLRPC.TL_updateUserBlocked;
import org.telegram.tgnet.TLRPC.TL_updateUserName;
import org.telegram.tgnet.TLRPC.TL_updateUserPhone;
import org.telegram.tgnet.TLRPC.TL_updateUserPhoto;
import org.telegram.tgnet.TLRPC.TL_updateUserStatus;
import org.telegram.tgnet.TLRPC.TL_updateUserTyping;
import org.telegram.tgnet.TLRPC.TL_updateWebPage;
import org.telegram.tgnet.TLRPC.TL_updates;
import org.telegram.tgnet.TLRPC.TL_updatesCombined;
import org.telegram.tgnet.TLRPC.TL_updatesTooLong;
import org.telegram.tgnet.TLRPC.TL_updates_channelDifference;
import org.telegram.tgnet.TLRPC.TL_updates_channelDifferenceEmpty;
import org.telegram.tgnet.TLRPC.TL_updates_channelDifferenceTooLong;
import org.telegram.tgnet.TLRPC.TL_updates_difference;
import org.telegram.tgnet.TLRPC.TL_updates_differenceEmpty;
import org.telegram.tgnet.TLRPC.TL_updates_differenceSlice;
import org.telegram.tgnet.TLRPC.TL_updates_getChannelDifference;
import org.telegram.tgnet.TLRPC.TL_updates_getDifference;
import org.telegram.tgnet.TLRPC.TL_updates_getState;
import org.telegram.tgnet.TLRPC.TL_updates_state;
import org.telegram.tgnet.TLRPC.TL_userForeign_old2;
import org.telegram.tgnet.TLRPC.TL_userFull;
import org.telegram.tgnet.TLRPC.TL_userProfilePhoto;
import org.telegram.tgnet.TLRPC.TL_userProfilePhotoEmpty;
import org.telegram.tgnet.TLRPC.TL_userStatusLastMonth;
import org.telegram.tgnet.TLRPC.TL_userStatusLastWeek;
import org.telegram.tgnet.TLRPC.TL_userStatusRecently;
import org.telegram.tgnet.TLRPC.TL_users_getFullUser;
import org.telegram.tgnet.TLRPC.TL_webPagePending;
import org.telegram.tgnet.TLRPC.Update;
import org.telegram.tgnet.TLRPC.Updates;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.UserProfilePhoto;
import org.telegram.tgnet.TLRPC.Vector;
import org.telegram.tgnet.TLRPC.WebPage;
import org.telegram.tgnet.TLRPC.contacts_Blocked;
import org.telegram.tgnet.TLRPC.messages_Dialogs;
import org.telegram.tgnet.TLRPC.messages_Messages;
import org.telegram.tgnet.TLRPC.photos_Photos;
import org.telegram.tgnet.TLRPC.updates_ChannelDifference;
import org.telegram.tgnet.TLRPC.updates_Difference;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.ProfileActivity;

public class MessagesController implements NotificationCenterDelegate {
    private static volatile MessagesController Instance = null;
    public static final int UPDATE_MASK_ALL = 1535;
    public static final int UPDATE_MASK_AVATAR = 2;
    public static final int UPDATE_MASK_CHANNEL = 8192;
    public static final int UPDATE_MASK_CHAT_ADMINS = 16384;
    public static final int UPDATE_MASK_CHAT_AVATAR = 8;
    public static final int UPDATE_MASK_CHAT_MEMBERS = 32;
    public static final int UPDATE_MASK_CHAT_NAME = 16;
    public static final int UPDATE_MASK_NAME = 1;
    public static final int UPDATE_MASK_NEW_MESSAGE = 2048;
    public static final int UPDATE_MASK_PHONE = 1024;
    public static final int UPDATE_MASK_READ_DIALOG_MESSAGE = 256;
    public static final int UPDATE_MASK_SELECT_DIALOG = 512;
    public static final int UPDATE_MASK_SEND_STATE = 4096;
    public static final int UPDATE_MASK_STATUS = 4;
    public static final int UPDATE_MASK_USER_PHONE = 128;
    public static final int UPDATE_MASK_USER_PRINT = 64;
    public ArrayList<Integer> blockedUsers = new ArrayList();
    private SparseArray<ArrayList<Integer>> channelViewsToReload = new SparseArray();
    private SparseArray<ArrayList<Integer>> channelViewsToSend = new SparseArray();
    private HashMap<Integer, Integer> channelsPts = new HashMap();
    private ConcurrentHashMap<Integer, Chat> chats = new ConcurrentHashMap(100, 1.0f, 2);
    private Runnable currentDeleteTaskRunnable = null;
    private ArrayList<Integer> currentDeletingTaskMids = null;
    private int currentDeletingTaskTime = 0;
    public HashMap<Long, MessageObject> dialogMessage = new HashMap();
    public HashMap<Integer, MessageObject> dialogMessagesByIds = new HashMap();
    public ArrayList<Dialog> dialogs = new ArrayList();
    public boolean dialogsEndReached = false;
    public ArrayList<Dialog> dialogsGroupsOnly = new ArrayList();
    public ArrayList<Dialog> dialogsServerOnly = new ArrayList();
    public ConcurrentHashMap<Long, Dialog> dialogs_dict = new ConcurrentHashMap(100, 1.0f, 2);
    public ConcurrentHashMap<Long, Integer> dialogs_read_inbox_max = new ConcurrentHashMap(100, 1.0f, 2);
    private ArrayList<TL_disabledFeature> disabledFeatures = new ArrayList();
    public boolean enableJoined = true;
    private ConcurrentHashMap<Integer, EncryptedChat> encryptedChats = new ConcurrentHashMap(10, 1.0f, 2);
    private HashMap<Integer, ExportedChatInvite> exportedChats = new HashMap();
    public boolean firstGettingTask = false;
    public int fontSize = AndroidUtilities.dp(16.0f);
    public boolean gettingDifference = false;
    private HashMap<Integer, Boolean> gettingDifferenceChannels = new HashMap();
    private boolean gettingNewDeleteTask = false;
    public int groupBigSize;
    private ArrayList<Integer> joiningToChannels = new ArrayList();
    private int lastPrintingStringCount = 0;
    private long lastStatusUpdateTime = 0;
    private long lastViewsCheckTime;
    private ArrayList<Integer> loadedFullChats = new ArrayList();
    private ArrayList<Integer> loadedFullParticipants = new ArrayList();
    private ArrayList<Integer> loadedFullUsers = new ArrayList();
    public boolean loadingBlockedUsers = false;
    public boolean loadingDialogs = false;
    private ArrayList<Integer> loadingFullChats = new ArrayList();
    private ArrayList<Integer> loadingFullParticipants = new ArrayList();
    private ArrayList<Integer> loadingFullUsers = new ArrayList();
    public int maxBroadcastCount = 100;
    public int maxGroupCount = Callback.DEFAULT_DRAG_ANIMATION_DURATION;
    public int maxMegagroupCount = LocationStatusCodes.GEOFENCE_NOT_AVAILABLE;
    private boolean migratingDialogs = false;
    public int minGroupConvertSize = Callback.DEFAULT_DRAG_ANIMATION_DURATION;
    private SparseIntArray needShortPollChannels = new SparseIntArray();
    public int nextDialogsCacheOffset;
    private boolean offlineSent = false;
    public ConcurrentHashMap<Integer, Integer> onlinePrivacy = new ConcurrentHashMap(20, 1.0f, 2);
    public HashMap<Long, CharSequence> printingStrings = new HashMap();
    public HashMap<Long, Integer> printingStringsTypes = new HashMap();
    public ConcurrentHashMap<Long, ArrayList<PrintingUser>> printingUsers = new ConcurrentHashMap(20, 1.0f, 2);
    public boolean registeringForPush = false;
    private HashMap<Long, ArrayList<Integer>> reloadingMessages = new HashMap();
    public HashMap<Integer, HashMap<Long, Boolean>> sendingTypings = new HashMap();
    private SparseIntArray shortPollChannels = new SparseIntArray();
    private int statusRequest = 0;
    private int statusSettingState = 0;
    private HashMap<Integer, ArrayList<Updates>> updatesQueueChannels = new HashMap();
    private ArrayList<Updates> updatesQueuePts = new ArrayList();
    private ArrayList<Updates> updatesQueueQts = new ArrayList();
    private ArrayList<Updates> updatesQueueSeq = new ArrayList();
    private HashMap<Integer, Long> updatesStartWaitTimeChannels = new HashMap();
    private long updatesStartWaitTimePts = 0;
    private long updatesStartWaitTimeQts = 0;
    private long updatesStartWaitTimeSeq = 0;
    public boolean updatingState = false;
    private String uploadingAvatar = null;
    private ConcurrentHashMap<Integer, User> users = new ConcurrentHashMap(100, 1.0f, 2);
    private ConcurrentHashMap<String, User> usersByUsernames = new ConcurrentHashMap(100, 1.0f, 2);

    class C04473 implements Runnable {
        C04473() {
        }

        public void run() {
            ConnectionsManager.getInstance().setIsUpdating(false);
            MessagesController.this.updatesQueueChannels.clear();
            MessagesController.this.updatesStartWaitTimeChannels.clear();
            MessagesController.this.gettingDifferenceChannels.clear();
            MessagesController.this.channelsPts.clear();
            MessagesController.this.shortPollChannels.clear();
            MessagesController.this.needShortPollChannels.clear();
        }
    }

    class C04574 implements Runnable {
        C04574() {
        }

        public void run() {
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.updateInterfaces, Integer.valueOf(4));
        }
    }

    public static class PrintingUser {
        public SendMessageAction action;
        public long lastTime;
        public int userId;
    }

    class C14542 implements RequestDelegate {

        class C04361 implements Runnable {
            C04361() {
            }

            public void run() {
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.updateInterfaces, Integer.valueOf(2));
                UserConfig.saveConfig(true);
            }
        }

        C14542() {
        }

        public void run(TLObject response, TL_error error) {
            if (error == null) {
                User user = MessagesController.this.getUser(Integer.valueOf(UserConfig.getClientUserId()));
                if (user == null) {
                    user = UserConfig.getCurrentUser();
                    MessagesController.this.putUser(user, true);
                } else {
                    UserConfig.setCurrentUser(user);
                }
                if (user != null) {
                    TL_photos_photo photo = (TL_photos_photo) response;
                    ArrayList<PhotoSize> sizes = photo.photo.sizes;
                    PhotoSize smallSize = FileLoader.getClosestPhotoSizeWithSize(sizes, 100);
                    PhotoSize bigSize = FileLoader.getClosestPhotoSizeWithSize(sizes, LocationStatusCodes.GEOFENCE_NOT_AVAILABLE);
                    user.photo = new TL_userProfilePhoto();
                    user.photo.photo_id = photo.photo.id;
                    if (smallSize != null) {
                        user.photo.photo_small = smallSize.location;
                    }
                    if (bigSize != null) {
                        user.photo.photo_big = bigSize.location;
                    } else if (smallSize != null) {
                        user.photo.photo_small = smallSize.location;
                    }
                    MessagesStorage.getInstance().clearUserPhotos(user.id);
                    ArrayList<User> users = new ArrayList();
                    users.add(user);
                    MessagesStorage.getInstance().putUsersAndChats(users, null, false, true);
                    AndroidUtilities.runOnUIThread(new C04361());
                }
            }
        }
    }

    private class UserActionUpdatesPts extends Updates {
        private UserActionUpdatesPts() {
        }
    }

    private class UserActionUpdatesSeq extends Updates {
        private UserActionUpdatesSeq() {
        }
    }

    static /* synthetic */ long access$2414(MessagesController x0, long x1) {
        long j = x0.lastStatusUpdateTime + x1;
        x0.lastStatusUpdateTime = j;
        return j;
    }

    public static MessagesController getInstance() {
        MessagesController localInstance = Instance;
        if (localInstance == null) {
            synchronized (MessagesController.class) {
                try {
                    localInstance = Instance;
                    if (localInstance == null) {
                        MessagesController localInstance2 = new MessagesController();
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

    public MessagesController() {
        ImageLoader.getInstance();
        MessagesStorage.getInstance();
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.FileDidUpload);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.FileDidFailUpload);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.FileDidLoaded);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.FileDidFailedLoad);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.messageReceivedByServer);
        addSupportUser();
        this.enableJoined = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", 0).getBoolean("EnableContactJoined", true);
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0);
        this.maxGroupCount = preferences.getInt("maxGroupCount", Callback.DEFAULT_DRAG_ANIMATION_DURATION);
        this.maxMegagroupCount = preferences.getInt("maxMegagroupCount", LocationStatusCodes.GEOFENCE_NOT_AVAILABLE);
        this.groupBigSize = preferences.getInt("groupBigSize", 10);
        this.fontSize = preferences.getInt("fons_size", AndroidUtilities.isTablet() ? 18 : 16);
        String disabledFeaturesString = preferences.getString("disabledFeatures", null);
        if (disabledFeaturesString != null && disabledFeaturesString.length() != 0) {
            try {
                byte[] bytes = Base64.decode(disabledFeaturesString, 0);
                if (bytes != null) {
                    SerializedData data = new SerializedData(bytes);
                    int count = data.readInt32(false);
                    for (int a = 0; a < count; a++) {
                        TL_disabledFeature feature = TL_disabledFeature.TLdeserialize(data, data.readInt32(false), false);
                        if (!(feature == null || feature.feature == null || feature.description == null)) {
                            this.disabledFeatures.add(feature);
                        }
                    }
                }
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
        }
    }

    public void updateConfig(final TL_config config) {
        AndroidUtilities.runOnUIThread(new Runnable() {
            public void run() {
                MessagesController.this.maxMegagroupCount = config.megagroup_size_max;
                MessagesController.this.maxGroupCount = config.chat_size_max;
                MessagesController.this.groupBigSize = config.chat_big_size;
                MessagesController.this.disabledFeatures = config.disabled_features;
                Editor editor = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0).edit();
                editor.putInt("maxGroupCount", MessagesController.this.maxGroupCount);
                editor.putInt("maxMegagroupCount", MessagesController.this.maxMegagroupCount);
                editor.putInt("groupBigSize", MessagesController.this.groupBigSize);
                try {
                    SerializedData data = new SerializedData();
                    data.writeInt32(MessagesController.this.disabledFeatures.size());
                    Iterator i$ = MessagesController.this.disabledFeatures.iterator();
                    while (i$.hasNext()) {
                        ((TL_disabledFeature) i$.next()).serializeToStream(data);
                    }
                    String string = Base64.encodeToString(data.toByteArray(), 0);
                    if (string.length() != 0) {
                        editor.putString("disabledFeatures", string);
                    }
                } catch (Throwable e) {
                    editor.remove("disabledFeatures");
                    FileLog.m611e("tmessages", e);
                }
                editor.commit();
            }
        });
    }

    public static boolean isFeatureEnabled(String feature, BaseFragment fragment) {
        if (feature == null || feature.length() == 0 || getInstance().disabledFeatures.isEmpty() || fragment == null) {
            return true;
        }
        Iterator i$ = getInstance().disabledFeatures.iterator();
        while (i$.hasNext()) {
            TL_disabledFeature disabledFeature = (TL_disabledFeature) i$.next();
            if (disabledFeature.feature.equals(feature)) {
                if (fragment.getParentActivity() != null) {
                    Builder builder = new Builder(fragment.getParentActivity());
                    builder.setTitle("Oops!");
                    builder.setPositiveButton(LocaleController.getString("OK", C0553R.string.OK), null);
                    builder.setMessage(disabledFeature.description);
                    fragment.showDialog(builder.create());
                }
                return false;
            }
        }
        return true;
    }

    public void addSupportUser() {
        TL_userForeign_old2 user = new TL_userForeign_old2();
        user.phone = "333";
        user.id = 333000;
        user.first_name = "Telegram";
        user.last_name = "";
        user.status = null;
        user.photo = new TL_userProfilePhotoEmpty();
        putUser(user, true);
        user = new TL_userForeign_old2();
        user.phone = "42777";
        user.id = 777000;
        user.first_name = "Telegram";
        user.last_name = "Notifications";
        user.status = null;
        user.photo = new TL_userProfilePhotoEmpty();
        putUser(user, true);
    }

    public static InputUser getInputUser(User user) {
        if (user == null) {
            return new TL_inputUserEmpty();
        }
        if (user.id == UserConfig.getClientUserId()) {
            return new TL_inputUserSelf();
        }
        InputUser inputUser = new TL_inputUser();
        inputUser.user_id = user.id;
        inputUser.access_hash = user.access_hash;
        return inputUser;
    }

    public static InputUser getInputUser(int user_id) {
        return getInputUser(getInstance().getUser(Integer.valueOf(user_id)));
    }

    public static InputChannel getInputChannel(Chat chat) {
        if (!(chat instanceof TL_channel) && !(chat instanceof TL_channelForbidden)) {
            return new TL_inputChannelEmpty();
        }
        InputChannel inputChat = new TL_inputChannel();
        inputChat.channel_id = chat.id;
        inputChat.access_hash = chat.access_hash;
        return inputChat;
    }

    public static InputChannel getInputChannel(int chatId) {
        return getInputChannel(getInstance().getChat(Integer.valueOf(chatId)));
    }

    public static InputPeer getInputPeer(int id) {
        InputPeer inputPeer;
        if (id < 0) {
            Chat chat = getInstance().getChat(Integer.valueOf(-id));
            if (ChatObject.isChannel(chat)) {
                inputPeer = new TL_inputPeerChannel();
                inputPeer.channel_id = -id;
                inputPeer.access_hash = chat.access_hash;
                return inputPeer;
            }
            inputPeer = new TL_inputPeerChat();
            inputPeer.chat_id = -id;
            return inputPeer;
        }
        User user = getInstance().getUser(Integer.valueOf(id));
        inputPeer = new TL_inputPeerUser();
        inputPeer.user_id = id;
        if (user == null) {
            return inputPeer;
        }
        inputPeer.access_hash = user.access_hash;
        return inputPeer;
    }

    public static Peer getPeer(int id) {
        Peer inputPeer;
        if (id < 0) {
            Chat chat = getInstance().getChat(Integer.valueOf(-id));
            if ((chat instanceof TL_channel) || (chat instanceof TL_channelForbidden)) {
                inputPeer = new TL_peerChannel();
                inputPeer.channel_id = -id;
                return inputPeer;
            }
            inputPeer = new TL_peerChat();
            inputPeer.chat_id = -id;
            return inputPeer;
        }
        User user = getInstance().getUser(Integer.valueOf(id));
        inputPeer = new TL_peerUser();
        inputPeer.user_id = id;
        return inputPeer;
    }

    public void didReceivedNotification(int id, Object... args) {
        String location;
        if (id == NotificationCenter.FileDidUpload) {
            location = args[0];
            InputFile file = args[1];
            if (this.uploadingAvatar != null && this.uploadingAvatar.equals(location)) {
                TL_photos_uploadProfilePhoto req = new TL_photos_uploadProfilePhoto();
                req.caption = "";
                req.crop = new TL_inputPhotoCropAuto();
                req.file = file;
                req.geo_point = new TL_inputGeoPointEmpty();
                ConnectionsManager.getInstance().sendRequest(req, new C14542());
            }
        } else if (id == NotificationCenter.FileDidFailUpload) {
            location = (String) args[0];
            if (this.uploadingAvatar != null && this.uploadingAvatar.equals(location)) {
                this.uploadingAvatar = null;
            }
        } else if (id == NotificationCenter.messageReceivedByServer) {
            Integer msgId = args[0];
            Integer newMsgId = args[1];
            Long did = args[3];
            MessageObject obj = (MessageObject) this.dialogMessage.get(did);
            if (obj != null && obj.getId() == msgId.intValue()) {
                obj.messageOwner.id = newMsgId.intValue();
                obj.messageOwner.send_state = 0;
                Dialog dialog = (Dialog) this.dialogs_dict.get(did);
                if (dialog != null && dialog.top_message == msgId.intValue()) {
                    dialog.top_message = newMsgId.intValue();
                }
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
            }
            obj = (MessageObject) this.dialogMessagesByIds.remove(msgId);
            if (obj != null) {
                this.dialogMessagesByIds.put(newMsgId, obj);
            }
        }
    }

    public void cleanUp() {
        ContactsController.getInstance().cleanup();
        MediaController.getInstance().cleanup();
        NotificationsController.getInstance().cleanup();
        SendMessagesHelper.getInstance().cleanUp();
        SecretChatHelper.getInstance().cleanUp();
        StickersQuery.cleanup();
        this.dialogs_dict.clear();
        this.dialogs_read_inbox_max.clear();
        this.exportedChats.clear();
        this.dialogs.clear();
        this.joiningToChannels.clear();
        this.channelViewsToSend.clear();
        this.channelViewsToReload.clear();
        this.dialogsServerOnly.clear();
        this.dialogsGroupsOnly.clear();
        this.users.clear();
        this.usersByUsernames.clear();
        this.chats.clear();
        this.dialogMessage.clear();
        this.printingUsers.clear();
        this.printingStrings.clear();
        this.printingStringsTypes.clear();
        this.onlinePrivacy.clear();
        this.lastPrintingStringCount = 0;
        this.nextDialogsCacheOffset = 0;
        this.updatesQueueSeq.clear();
        this.updatesQueuePts.clear();
        this.updatesQueueQts.clear();
        this.blockedUsers.clear();
        this.sendingTypings.clear();
        this.loadingFullUsers.clear();
        this.loadedFullUsers.clear();
        this.reloadingMessages.clear();
        this.loadingFullChats.clear();
        this.loadingFullParticipants.clear();
        this.loadedFullParticipants.clear();
        this.loadedFullChats.clear();
        this.updatesStartWaitTimeSeq = 0;
        this.updatesStartWaitTimePts = 0;
        this.updatesStartWaitTimeQts = 0;
        this.currentDeletingTaskTime = 0;
        this.currentDeletingTaskMids = null;
        this.gettingNewDeleteTask = false;
        this.loadingDialogs = false;
        this.dialogsEndReached = false;
        this.gettingDifference = false;
        this.loadingBlockedUsers = false;
        this.firstGettingTask = false;
        this.updatingState = false;
        this.lastStatusUpdateTime = 0;
        this.offlineSent = false;
        this.registeringForPush = false;
        this.uploadingAvatar = null;
        this.statusRequest = 0;
        this.statusSettingState = 0;
        Utilities.stageQueue.postRunnable(new C04473());
        if (this.currentDeleteTaskRunnable != null) {
            Utilities.stageQueue.cancelRunnable(this.currentDeleteTaskRunnable);
            this.currentDeleteTaskRunnable = null;
        }
        addSupportUser();
    }

    public User getUser(Integer id) {
        return (User) this.users.get(id);
    }

    public User getUser(String username) {
        if (username == null || username.length() == 0) {
            return null;
        }
        return (User) this.usersByUsernames.get(username.toLowerCase());
    }

    public ConcurrentHashMap<Integer, User> getUsers() {
        return this.users;
    }

    public Chat getChat(Integer id) {
        return (Chat) this.chats.get(id);
    }

    public EncryptedChat getEncryptedChat(Integer id) {
        return (EncryptedChat) this.encryptedChats.get(id);
    }

    public EncryptedChat getEncryptedChatDB(int chat_id) {
        EncryptedChat chat = (EncryptedChat) this.encryptedChats.get(Integer.valueOf(chat_id));
        if (chat != null) {
            return chat;
        }
        Semaphore semaphore = new Semaphore(0);
        ArrayList<TLObject> result = new ArrayList();
        MessagesStorage.getInstance().getEncryptedChat(chat_id, semaphore, result);
        try {
            semaphore.acquire();
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
        }
        if (result.size() != 2) {
            return chat;
        }
        chat = (EncryptedChat) result.get(0);
        User user = (User) result.get(1);
        putEncryptedChat(chat, false);
        putUser(user, true);
        return chat;
    }

    public ExportedChatInvite getExportedInvite(int chat_id) {
        return (ExportedChatInvite) this.exportedChats.get(Integer.valueOf(chat_id));
    }

    public boolean putUser(User user, boolean fromCache) {
        if (user == null) {
            return false;
        }
        if (!fromCache || user.id / LocationStatusCodes.GEOFENCE_NOT_AVAILABLE == 333 || user.id == 777000) {
            fromCache = false;
        } else {
            fromCache = true;
        }
        User oldUser = (User) this.users.get(Integer.valueOf(user.id));
        if (!(oldUser == null || oldUser.username == null || oldUser.username.length() <= 0)) {
            this.usersByUsernames.remove(oldUser.username);
        }
        if (user.username != null && user.username.length() > 0) {
            this.usersByUsernames.put(user.username.toLowerCase(), user);
        }
        if (!fromCache) {
            this.users.put(Integer.valueOf(user.id), user);
            if (user.id == UserConfig.getClientUserId()) {
                UserConfig.setCurrentUser(user);
                UserConfig.saveConfig(true);
            }
            if (oldUser == null || user.status == null || oldUser.status == null || user.status.expires == oldUser.status.expires) {
                return false;
            }
            return true;
        } else if (oldUser != null) {
            return false;
        } else {
            this.users.put(Integer.valueOf(user.id), user);
            return false;
        }
    }

    public void putUsers(ArrayList<User> users, boolean fromCache) {
        if (users != null && !users.isEmpty()) {
            boolean updateStatus = false;
            int count = users.size();
            for (int a = 0; a < count; a++) {
                if (putUser((User) users.get(a), fromCache)) {
                    updateStatus = true;
                }
            }
            if (updateStatus) {
                AndroidUtilities.runOnUIThread(new C04574());
            }
        }
    }

    public void putChat(Chat chat, boolean fromCache) {
        if (chat != null) {
            Chat oldChat = (Chat) this.chats.get(Integer.valueOf(chat.id));
            if (!fromCache) {
                if (!(oldChat == null || chat.version == oldChat.version)) {
                    this.loadedFullChats.remove(Integer.valueOf(chat.id));
                }
                this.chats.put(Integer.valueOf(chat.id), chat);
            } else if (oldChat == null) {
                this.chats.put(Integer.valueOf(chat.id), chat);
            }
        }
    }

    public void putChats(ArrayList<Chat> chats, boolean fromCache) {
        if (chats != null && !chats.isEmpty()) {
            int count = chats.size();
            for (int a = 0; a < count; a++) {
                putChat((Chat) chats.get(a), fromCache);
            }
        }
    }

    public void putEncryptedChat(EncryptedChat encryptedChat, boolean fromCache) {
        if (encryptedChat != null) {
            if (fromCache) {
                this.encryptedChats.putIfAbsent(Integer.valueOf(encryptedChat.id), encryptedChat);
            } else {
                this.encryptedChats.put(Integer.valueOf(encryptedChat.id), encryptedChat);
            }
        }
    }

    public void putEncryptedChats(ArrayList<EncryptedChat> encryptedChats, boolean fromCache) {
        if (encryptedChats != null && !encryptedChats.isEmpty()) {
            int count = encryptedChats.size();
            for (int a = 0; a < count; a++) {
                putEncryptedChat((EncryptedChat) encryptedChats.get(a), fromCache);
            }
        }
    }

    public void cancelLoadFullUser(int uid) {
        this.loadingFullUsers.remove(Integer.valueOf(uid));
    }

    public void cancelLoadFullChat(int cid) {
        this.loadingFullChats.remove(Integer.valueOf(cid));
    }

    protected void clearFullUsers() {
        this.loadedFullUsers.clear();
        this.loadedFullChats.clear();
    }

    public void loadFullChat(int chat_id, int classGuid) {
        loadFullChat(chat_id, classGuid, false);
    }

    public void loadFullChat(final int chat_id, final int classGuid, boolean force) {
        if (!this.loadingFullChats.contains(Integer.valueOf(chat_id))) {
            if (force || !this.loadedFullChats.contains(Integer.valueOf(chat_id))) {
                TLObject request;
                this.loadingFullChats.add(Integer.valueOf(chat_id));
                final Chat chat = getChat(Integer.valueOf(chat_id));
                TLObject req;
                if (ChatObject.isChannel(chat_id)) {
                    req = new TL_channels_getFullChannel();
                    req.channel = getInputChannel(chat_id);
                    request = req;
                } else {
                    req = new TL_messages_getFullChat();
                    req.chat_id = chat_id;
                    request = req;
                }
                int reqId = ConnectionsManager.getInstance().sendRequest(request, new RequestDelegate() {
                    public void run(TLObject response, final TL_error error) {
                        if (error == null) {
                            final TL_messages_chatFull res = (TL_messages_chatFull) response;
                            if (chat != null && chat.megagroup) {
                                res.full_chat.unread_important_count = Math.max(res.full_chat.unread_important_count, res.full_chat.unread_count);
                            }
                            MessagesStorage.getInstance().putUsersAndChats(res.users, res.chats, true, true);
                            MessagesStorage.getInstance().updateChatInfo(res.full_chat, false);
                            AndroidUtilities.runOnUIThread(new Runnable() {
                                public void run() {
                                    MessagesController.this.applyDialogNotificationsSettings((long) (-chat_id), res.full_chat.notify_settings);
                                    for (int a = 0; a < res.full_chat.bot_info.size(); a++) {
                                        BotQuery.putBotInfo((BotInfo) res.full_chat.bot_info.get(a));
                                    }
                                    MessagesController.this.exportedChats.put(Integer.valueOf(chat_id), res.full_chat.exported_invite);
                                    MessagesController.this.loadingFullChats.remove(Integer.valueOf(chat_id));
                                    MessagesController.this.loadedFullChats.add(Integer.valueOf(chat_id));
                                    if (!res.chats.isEmpty()) {
                                        ((Chat) res.chats.get(0)).address = res.full_chat.about;
                                    }
                                    MessagesController.this.putUsers(res.users, false);
                                    MessagesController.this.putChats(res.chats, false);
                                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.chatInfoDidLoaded, res.full_chat, Integer.valueOf(classGuid), Boolean.valueOf(false));
                                }
                            });
                            return;
                        }
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            public void run() {
                                if (error.text.equals("CHANNEL_PRIVATE")) {
                                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.chatInfoCantLoad, Integer.valueOf(chat_id));
                                }
                                MessagesController.this.loadingFullChats.remove(Integer.valueOf(chat_id));
                            }
                        });
                    }
                });
                if (classGuid != 0) {
                    ConnectionsManager.getInstance().bindRequestToGuid(reqId, classGuid);
                }
            }
        }
    }

    public void loadFullUser(final User user, final int classGuid) {
        if (user != null && !this.loadingFullUsers.contains(Integer.valueOf(user.id)) && !this.loadedFullUsers.contains(Integer.valueOf(user.id))) {
            this.loadingFullUsers.add(Integer.valueOf(user.id));
            TL_users_getFullUser req = new TL_users_getFullUser();
            req.id = getInputUser(user);
            ConnectionsManager.getInstance().bindRequestToGuid(ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {

                class C04692 implements Runnable {
                    C04692() {
                    }

                    public void run() {
                        MessagesController.this.loadingFullUsers.remove(Integer.valueOf(user.id));
                    }
                }

                public void run(final TLObject response, TL_error error) {
                    if (error == null) {
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            public void run() {
                                TL_userFull userFull = response;
                                MessagesController.this.applyDialogNotificationsSettings((long) user.id, userFull.notify_settings);
                                if (userFull.bot_info instanceof TL_botInfo) {
                                    BotQuery.putBotInfo(userFull.bot_info);
                                }
                                MessagesController.this.loadingFullUsers.remove(Integer.valueOf(user.id));
                                MessagesController.this.loadedFullUsers.add(Integer.valueOf(user.id));
                                String names = user.first_name + user.last_name + user.username;
                                ArrayList<User> users = new ArrayList();
                                users.add(userFull.user);
                                MessagesController.this.putUsers(users, false);
                                MessagesStorage.getInstance().putUsersAndChats(users, null, false, true);
                                if (!(names == null || names.equals(userFull.user.first_name + userFull.user.last_name + userFull.user.username))) {
                                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.updateInterfaces, Integer.valueOf(1));
                                }
                                if (userFull.bot_info instanceof TL_botInfo) {
                                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.botInfoDidLoaded, userFull.bot_info, Integer.valueOf(classGuid));
                                }
                            }
                        });
                    } else {
                        AndroidUtilities.runOnUIThread(new C04692());
                    }
                }
            }), classGuid);
        }
    }

    private void reloadMessages(ArrayList<Integer> mids, long dialog_id) {
        if (!mids.isEmpty()) {
            TLObject request;
            final ArrayList<Integer> result = new ArrayList();
            final Chat chat = ChatObject.getChatByDialog(dialog_id);
            TLObject req;
            if (ChatObject.isChannel(chat)) {
                req = new TL_channels_getMessages();
                req.channel = getInputChannel(chat);
                req.id = result;
                request = req;
            } else {
                req = new TL_messages_getMessages();
                req.id = result;
                request = req;
            }
            ArrayList<Integer> arrayList = (ArrayList) this.reloadingMessages.get(Long.valueOf(dialog_id));
            for (int a = 0; a < mids.size(); a++) {
                Integer mid = (Integer) mids.get(a);
                if (arrayList == null || !arrayList.contains(mid)) {
                    result.add(mid);
                }
            }
            if (!result.isEmpty()) {
                if (arrayList == null) {
                    arrayList = new ArrayList();
                    this.reloadingMessages.put(Long.valueOf(dialog_id), arrayList);
                }
                arrayList.addAll(result);
                final long j = dialog_id;
                ConnectionsManager.getInstance().sendRequest(request, new RequestDelegate() {
                    public void run(TLObject response, TL_error error) {
                        if (error == null) {
                            int a;
                            messages_Messages messagesRes = (messages_Messages) response;
                            AbstractMap usersLocal = new HashMap();
                            for (a = 0; a < messagesRes.users.size(); a++) {
                                User u = (User) messagesRes.users.get(a);
                                usersLocal.put(Integer.valueOf(u.id), u);
                            }
                            HashMap<Integer, Chat> chatsLocal = new HashMap();
                            for (a = 0; a < messagesRes.chats.size(); a++) {
                                Chat c = (Chat) messagesRes.chats.get(a);
                                chatsLocal.put(Integer.valueOf(c.id), c);
                            }
                            final ArrayList<MessageObject> objects = new ArrayList();
                            for (a = 0; a < messagesRes.messages.size(); a++) {
                                Message message = (Message) messagesRes.messages.get(a);
                                if (chat != null && chat.megagroup) {
                                    message.flags |= Integer.MIN_VALUE;
                                }
                                message.dialog_id = j;
                                objects.add(new MessageObject(message, usersLocal, chatsLocal, true));
                            }
                            ImageLoader.saveMessagesThumbs(messagesRes.messages);
                            MessagesStorage.getInstance().putMessages(messagesRes, j, -1, 0, 0, false);
                            AndroidUtilities.runOnUIThread(new Runnable() {
                                public void run() {
                                    ArrayList<Integer> arrayList = (ArrayList) MessagesController.this.reloadingMessages.get(Long.valueOf(j));
                                    if (arrayList != null) {
                                        arrayList.removeAll(result);
                                        if (arrayList.isEmpty()) {
                                            MessagesController.this.reloadingMessages.remove(Long.valueOf(j));
                                        }
                                    }
                                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.replaceMessagesObjects, Long.valueOf(j), objects);
                                }
                            });
                        }
                    }
                });
            }
        }
    }

    protected void processNewChannelDifferenceParams(int pts, int pts_count, int channelId) {
        FileLog.m609e("tmessages", "processNewChannelDifferenceParams pts = " + pts + " pts_count = " + pts_count + " channeldId = " + channelId);
        if (((Dialog) this.dialogs_dict.get(Long.valueOf((long) (-channelId)))) instanceof TL_dialogChannel) {
            Integer channelPts = (Integer) this.channelsPts.get(Integer.valueOf(channelId));
            if (channelPts == null) {
                channelPts = Integer.valueOf(MessagesStorage.getInstance().getChannelPtsSync(channelId));
                if (channelPts.intValue() == 0) {
                    channelPts = Integer.valueOf(1);
                }
                this.channelsPts.put(Integer.valueOf(channelId), channelPts);
            }
            if (channelPts.intValue() + pts_count == pts) {
                FileLog.m609e("tmessages", "APPLY CHANNEL PTS");
                this.channelsPts.put(Integer.valueOf(channelId), Integer.valueOf(pts));
                MessagesStorage.getInstance().saveChannelPts(channelId, pts);
            } else if (channelPts.intValue() != pts) {
                Long updatesStartWaitTime = (Long) this.updatesStartWaitTimeChannels.get(Integer.valueOf(channelId));
                Boolean gettingDifferenceChannel = (Boolean) this.gettingDifferenceChannels.get(Integer.valueOf(channelId));
                if (gettingDifferenceChannel == null) {
                    gettingDifferenceChannel = Boolean.valueOf(false);
                }
                if (gettingDifferenceChannel.booleanValue() || updatesStartWaitTime == null || Math.abs(System.currentTimeMillis() - updatesStartWaitTime.longValue()) <= 1500) {
                    FileLog.m609e("tmessages", "ADD CHANNEL UPDATE TO QUEUE pts = " + pts + " pts_count = " + pts_count);
                    if (updatesStartWaitTime == null) {
                        this.updatesStartWaitTimeChannels.put(Integer.valueOf(channelId), Long.valueOf(System.currentTimeMillis()));
                    }
                    UserActionUpdatesPts updates = new UserActionUpdatesPts();
                    updates.pts = pts;
                    updates.pts_count = pts_count;
                    updates.chat_id = channelId;
                    ArrayList<Updates> arrayList = (ArrayList) this.updatesQueueChannels.get(Integer.valueOf(channelId));
                    if (arrayList == null) {
                        arrayList = new ArrayList();
                        this.updatesQueueChannels.put(Integer.valueOf(channelId), arrayList);
                    }
                    arrayList.add(updates);
                    return;
                }
                getChannelDifference(channelId);
            }
        }
    }

    protected void processNewDifferenceParams(int seq, int pts, int date, int pts_count) {
        FileLog.m609e("tmessages", "processNewDifferenceParams seq = " + seq + " pts = " + pts + " date = " + date + " pts_count = " + pts_count);
        if (pts != -1) {
            if (MessagesStorage.lastPtsValue + pts_count == pts) {
                FileLog.m609e("tmessages", "APPLY PTS");
                MessagesStorage.lastPtsValue = pts;
                MessagesStorage.getInstance().saveDiffParams(MessagesStorage.lastSeqValue, MessagesStorage.lastPtsValue, MessagesStorage.lastDateValue, MessagesStorage.lastQtsValue);
            } else if (MessagesStorage.lastPtsValue != pts) {
                if (this.gettingDifference || this.updatesStartWaitTimePts == 0 || Math.abs(System.currentTimeMillis() - this.updatesStartWaitTimePts) <= 1500) {
                    FileLog.m609e("tmessages", "ADD UPDATE TO QUEUE pts = " + pts + " pts_count = " + pts_count);
                    if (this.updatesStartWaitTimePts == 0) {
                        this.updatesStartWaitTimePts = System.currentTimeMillis();
                    }
                    UserActionUpdatesPts updates = new UserActionUpdatesPts();
                    updates.pts = pts;
                    updates.pts_count = pts_count;
                    this.updatesQueuePts.add(updates);
                } else {
                    getDifference();
                }
            }
        }
        if (seq == -1) {
            return;
        }
        if (MessagesStorage.lastSeqValue + 1 == seq) {
            FileLog.m609e("tmessages", "APPLY SEQ");
            MessagesStorage.lastSeqValue = seq;
            if (date != -1) {
                MessagesStorage.lastDateValue = date;
            }
            MessagesStorage.getInstance().saveDiffParams(MessagesStorage.lastSeqValue, MessagesStorage.lastPtsValue, MessagesStorage.lastDateValue, MessagesStorage.lastQtsValue);
        } else if (MessagesStorage.lastSeqValue == seq) {
        } else {
            if (this.gettingDifference || this.updatesStartWaitTimeSeq == 0 || Math.abs(System.currentTimeMillis() - this.updatesStartWaitTimeSeq) <= 1500) {
                FileLog.m609e("tmessages", "ADD UPDATE TO QUEUE seq = " + seq);
                if (this.updatesStartWaitTimeSeq == 0) {
                    this.updatesStartWaitTimeSeq = System.currentTimeMillis();
                }
                UserActionUpdatesSeq updates2 = new UserActionUpdatesSeq();
                updates2.seq = seq;
                this.updatesQueueSeq.add(updates2);
                return;
            }
            getDifference();
        }
    }

    public void didAddedNewTask(final int minDate, final SparseArray<ArrayList<Integer>> mids) {
        Utilities.stageQueue.postRunnable(new Runnable() {
            public void run() {
                if ((MessagesController.this.currentDeletingTaskMids == null && !MessagesController.this.gettingNewDeleteTask) || (MessagesController.this.currentDeletingTaskTime != 0 && minDate < MessagesController.this.currentDeletingTaskTime)) {
                    MessagesController.this.getNewDeleteTask(null);
                }
            }
        });
        AndroidUtilities.runOnUIThread(new Runnable() {
            public void run() {
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.didCreatedNewDeleteTask, mids);
            }
        });
    }

    public void getNewDeleteTask(final ArrayList<Integer> oldTask) {
        Utilities.stageQueue.postRunnable(new Runnable() {
            public void run() {
                MessagesController.this.gettingNewDeleteTask = true;
                MessagesStorage.getInstance().getNewTask(oldTask);
            }
        });
    }

    private boolean checkDeletingTask(boolean runnable) {
        int currentServerTime = ConnectionsManager.getInstance().getCurrentTime();
        if (this.currentDeletingTaskMids == null) {
            return false;
        }
        if (!runnable && (this.currentDeletingTaskTime == 0 || this.currentDeletingTaskTime > currentServerTime)) {
            return false;
        }
        this.currentDeletingTaskTime = 0;
        if (!(this.currentDeleteTaskRunnable == null || runnable)) {
            Utilities.stageQueue.cancelRunnable(this.currentDeleteTaskRunnable);
        }
        this.currentDeleteTaskRunnable = null;
        AndroidUtilities.runOnUIThread(new Runnable() {

            class C04321 implements Runnable {
                C04321() {
                }

                public void run() {
                    MessagesController.this.getNewDeleteTask(MessagesController.this.currentDeletingTaskMids);
                    MessagesController.this.currentDeletingTaskTime = 0;
                    MessagesController.this.currentDeletingTaskMids = null;
                }
            }

            public void run() {
                MessagesController.this.deleteMessages(MessagesController.this.currentDeletingTaskMids, null, null, 0);
                Utilities.stageQueue.postRunnable(new C04321());
            }
        });
        return true;
    }

    public void processLoadedDeleteTask(final int taskTime, final ArrayList<Integer> messages) {
        Utilities.stageQueue.postRunnable(new Runnable() {

            class C04331 implements Runnable {
                C04331() {
                }

                public void run() {
                    MessagesController.this.checkDeletingTask(true);
                }
            }

            public void run() {
                MessagesController.this.gettingNewDeleteTask = false;
                if (messages != null) {
                    MessagesController.this.currentDeletingTaskTime = taskTime;
                    MessagesController.this.currentDeletingTaskMids = messages;
                    if (MessagesController.this.currentDeleteTaskRunnable != null) {
                        Utilities.stageQueue.cancelRunnable(MessagesController.this.currentDeleteTaskRunnable);
                        MessagesController.this.currentDeleteTaskRunnable = null;
                    }
                    if (!MessagesController.this.checkDeletingTask(false)) {
                        MessagesController.this.currentDeleteTaskRunnable = new C04331();
                        Utilities.stageQueue.postRunnable(MessagesController.this.currentDeleteTaskRunnable, ((long) Math.abs(ConnectionsManager.getInstance().getCurrentTime() - MessagesController.this.currentDeletingTaskTime)) * 1000);
                        return;
                    }
                    return;
                }
                MessagesController.this.currentDeletingTaskTime = 0;
                MessagesController.this.currentDeletingTaskMids = null;
            }
        });
    }

    public void loadUserPhotos(int uid, int offset, int count, long max_id, boolean fromCache, int classGuid) {
        if (fromCache) {
            MessagesStorage.getInstance().getUserPhotos(uid, offset, count, max_id, classGuid);
            return;
        }
        User user = getUser(Integer.valueOf(uid));
        if (user != null) {
            TL_photos_getUserPhotos req = new TL_photos_getUserPhotos();
            req.limit = count;
            req.offset = offset;
            req.max_id = (long) ((int) max_id);
            req.user_id = getInputUser(user);
            final int i = uid;
            final int i2 = offset;
            final int i3 = count;
            final long j = max_id;
            final int i4 = classGuid;
            ConnectionsManager.getInstance().bindRequestToGuid(ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
                public void run(TLObject response, TL_error error) {
                    if (error == null) {
                        MessagesController.this.processLoadedUserPhotos((photos_Photos) response, i, i2, i3, j, false, i4);
                    }
                }
            }), classGuid);
        }
    }

    public void blockUser(int user_id) {
        final User user = getUser(Integer.valueOf(user_id));
        if (user != null && !getInstance().blockedUsers.contains(Integer.valueOf(user_id))) {
            this.blockedUsers.add(Integer.valueOf(user_id));
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.blockedUsersDidLoaded, new Object[0]);
            TL_contacts_block req = new TL_contacts_block();
            req.id = getInputUser(user);
            ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
                public void run(TLObject response, TL_error error) {
                    if (error == null) {
                        ArrayList<Integer> ids = new ArrayList();
                        ids.add(Integer.valueOf(user.id));
                        MessagesStorage.getInstance().putBlockedUsers(ids, false);
                    }
                }
            });
        }
    }

    public void unblockUser(int user_id) {
        TL_contacts_unblock req = new TL_contacts_unblock();
        final User user = getInstance().getUser(Integer.valueOf(user_id));
        if (user != null) {
            this.blockedUsers.remove(Integer.valueOf(user.id));
            req.id = getInputUser(user);
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.blockedUsersDidLoaded, new Object[0]);
            ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
                public void run(TLObject response, TL_error error) {
                    MessagesStorage.getInstance().deleteBlockedUser(user.id);
                }
            });
        }
    }

    public void getBlockedUsers(boolean cache) {
        if (UserConfig.isClientActivated() && !this.loadingBlockedUsers) {
            this.loadingBlockedUsers = true;
            if (cache) {
                MessagesStorage.getInstance().getBlockedUsers();
                return;
            }
            TL_contacts_getBlocked req = new TL_contacts_getBlocked();
            req.offset = 0;
            req.limit = Callback.DEFAULT_DRAG_ANIMATION_DURATION;
            ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
                public void run(TLObject response, TL_error error) {
                    ArrayList<Integer> blocked = new ArrayList();
                    ArrayList<User> users = null;
                    if (error == null) {
                        contacts_Blocked res = (contacts_Blocked) response;
                        Iterator i$ = res.blocked.iterator();
                        while (i$.hasNext()) {
                            blocked.add(Integer.valueOf(((TL_contactBlocked) i$.next()).user_id));
                        }
                        users = res.users;
                        MessagesStorage.getInstance().putUsersAndChats(res.users, null, true, true);
                        MessagesStorage.getInstance().putBlockedUsers(blocked, true);
                    }
                    MessagesController.this.processLoadedBlockedUsers(blocked, users, false);
                }
            });
        }
    }

    public void processLoadedBlockedUsers(final ArrayList<Integer> ids, final ArrayList<User> users, final boolean cache) {
        AndroidUtilities.runOnUIThread(new Runnable() {
            public void run() {
                if (users != null) {
                    MessagesController.getInstance().putUsers(users, cache);
                }
                MessagesController.this.loadingBlockedUsers = false;
                if (ids.isEmpty() && cache && !UserConfig.blockedUsersLoaded) {
                    MessagesController.this.getBlockedUsers(false);
                    return;
                }
                if (!cache) {
                    UserConfig.blockedUsersLoaded = true;
                    UserConfig.saveConfig(false);
                }
                MessagesController.this.blockedUsers = ids;
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.blockedUsersDidLoaded, new Object[0]);
            }
        });
    }

    public void deleteUserPhoto(InputPhoto photo) {
        if (photo == null) {
            TL_photos_updateProfilePhoto req = new TL_photos_updateProfilePhoto();
            req.id = new TL_inputPhotoEmpty();
            req.crop = new TL_inputPhotoCropAuto();
            UserConfig.getCurrentUser().photo = new TL_userProfilePhotoEmpty();
            User user = getInstance().getUser(Integer.valueOf(UserConfig.getClientUserId()));
            if (user == null) {
                user = UserConfig.getCurrentUser();
            }
            if (user != null) {
                user.photo = UserConfig.getCurrentUser().photo;
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.mainUserInfoChanged, new Object[0]);
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.updateInterfaces, Integer.valueOf(UPDATE_MASK_ALL));
                ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {

                    class C04341 implements Runnable {
                        C04341() {
                        }

                        public void run() {
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.mainUserInfoChanged, new Object[0]);
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.updateInterfaces, Integer.valueOf(MessagesController.UPDATE_MASK_ALL));
                            UserConfig.saveConfig(true);
                        }
                    }

                    public void run(TLObject response, TL_error error) {
                        if (error == null) {
                            User user = MessagesController.getInstance().getUser(Integer.valueOf(UserConfig.getClientUserId()));
                            if (user == null) {
                                user = UserConfig.getCurrentUser();
                                MessagesController.getInstance().putUser(user, false);
                            } else {
                                UserConfig.setCurrentUser(user);
                            }
                            if (user != null) {
                                MessagesStorage.getInstance().clearUserPhotos(user.id);
                                ArrayList<User> users = new ArrayList();
                                users.add(user);
                                MessagesStorage.getInstance().putUsersAndChats(users, null, false, true);
                                user.photo = (UserProfilePhoto) response;
                                AndroidUtilities.runOnUIThread(new C04341());
                            }
                        }
                    }
                });
                return;
            }
            return;
        }
        TL_photos_deletePhotos req2 = new TL_photos_deletePhotos();
        req2.id.add(photo);
        ConnectionsManager.getInstance().sendRequest(req2, new RequestDelegate() {
            public void run(TLObject response, TL_error error) {
            }
        });
    }

    public void processLoadedUserPhotos(photos_Photos res, int uid, int offset, int count, long max_id, boolean fromCache, int classGuid) {
        if (!fromCache) {
            MessagesStorage.getInstance().putUsersAndChats(res.users, null, true, true);
            MessagesStorage.getInstance().putUserPhotos(uid, res);
        } else if (res == null || res.photos.isEmpty()) {
            loadUserPhotos(uid, offset, count, max_id, false, classGuid);
            return;
        }
        final photos_Photos org_telegram_tgnet_TLRPC_photos_Photos = res;
        final boolean z = fromCache;
        final int i = uid;
        final int i2 = offset;
        final int i3 = count;
        final int i4 = classGuid;
        AndroidUtilities.runOnUIThread(new Runnable() {
            public void run() {
                MessagesController.this.putUsers(org_telegram_tgnet_TLRPC_photos_Photos.users, z);
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.userPhotosLoaded, Integer.valueOf(i), Integer.valueOf(i2), Integer.valueOf(i3), Boolean.valueOf(z), Integer.valueOf(i4), org_telegram_tgnet_TLRPC_photos_Photos.photos);
            }
        });
    }

    public void uploadAndApplyUserAvatar(PhotoSize bigPhoto) {
        if (bigPhoto != null) {
            this.uploadingAvatar = FileLoader.getInstance().getDirectory(4) + "/" + bigPhoto.location.volume_id + "_" + bigPhoto.location.local_id + ".jpg";
            FileLoader.getInstance().uploadFile(this.uploadingAvatar, false, true);
        }
    }

    public void deleteMessages(ArrayList<Integer> messages, ArrayList<Long> randoms, EncryptedChat encryptedChat, final int channelId) {
        if (messages != null && !messages.isEmpty()) {
            Iterator i$;
            MessageObject obj;
            if (channelId == 0) {
                i$ = messages.iterator();
                while (i$.hasNext()) {
                    obj = (MessageObject) this.dialogMessagesByIds.get((Integer) i$.next());
                    if (obj != null) {
                        obj.deleted = true;
                    }
                }
            } else {
                obj = (MessageObject) this.dialogMessage.get(Long.valueOf((long) (-channelId)));
                if (obj != null) {
                    i$ = messages.iterator();
                    while (i$.hasNext()) {
                        if (obj.getId() == ((Integer) i$.next()).intValue()) {
                            obj.deleted = true;
                            break;
                        }
                    }
                }
            }
            ArrayList<Integer> toSend = new ArrayList();
            i$ = messages.iterator();
            while (i$.hasNext()) {
                Integer mid = (Integer) i$.next();
                if (mid.intValue() > 0) {
                    toSend.add(mid);
                }
            }
            MessagesStorage.getInstance().markMessagesAsDeleted(messages, true, channelId);
            MessagesStorage.getInstance().updateDialogsWithDeletedMessages(messages, true, channelId);
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.messagesDeleted, messages, Integer.valueOf(channelId));
            if (channelId != 0) {
                TL_channels_deleteMessages req = new TL_channels_deleteMessages();
                req.id = toSend;
                req.channel = getInputChannel(channelId);
                ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
                    public void run(TLObject response, TL_error error) {
                        if (error == null) {
                            TL_messages_affectedMessages res = (TL_messages_affectedMessages) response;
                            MessagesController.this.processNewChannelDifferenceParams(res.pts, res.pts_count, channelId);
                        }
                    }
                });
                return;
            }
            if (!(randoms == null || encryptedChat == null || randoms.isEmpty())) {
                SecretChatHelper.getInstance().sendMessagesDeleteMessage(encryptedChat, randoms, null);
            }
            TL_messages_deleteMessages req2 = new TL_messages_deleteMessages();
            req2.id = toSend;
            ConnectionsManager.getInstance().sendRequest(req2, new RequestDelegate() {
                public void run(TLObject response, TL_error error) {
                    if (error == null) {
                        TL_messages_affectedMessages res = (TL_messages_affectedMessages) response;
                        MessagesController.this.processNewDifferenceParams(-1, res.pts, -1, res.pts_count);
                    }
                }
            });
        }
    }

    public void deleteDialog(long did, int onlyHistory) {
        deleteDialog(did, true, onlyHistory, 0);
    }

    private void deleteDialog(long did, boolean first, int onlyHistory, int max_id) {
        int lower_part = (int) did;
        int high_id = (int) (did >> 32);
        int max_id_delete = max_id;
        if (onlyHistory == 2) {
            MessagesStorage.getInstance().deleteDialog(did, onlyHistory);
            return;
        }
        if (first) {
            final long j;
            Dialog dialog = (Dialog) this.dialogs_dict.get(Long.valueOf(did));
            if (dialog != null) {
                if (max_id_delete == 0) {
                    max_id_delete = Math.max(0, dialog.top_message);
                }
                if (onlyHistory == 0) {
                    this.dialogs.remove(dialog);
                    if (this.dialogsServerOnly.remove(dialog) && (dialog instanceof TL_dialogChannel)) {
                        j = did;
                        Utilities.stageQueue.postRunnable(new Runnable() {
                            public void run() {
                                MessagesController.this.channelsPts.remove(Integer.valueOf(-((int) j)));
                                MessagesController.this.shortPollChannels.delete(-((int) j));
                                MessagesController.this.needShortPollChannels.delete(-((int) j));
                            }
                        });
                    }
                    this.dialogsGroupsOnly.remove(dialog);
                    this.dialogs_dict.remove(Long.valueOf(did));
                    this.dialogs_read_inbox_max.remove(Long.valueOf(did));
                    this.nextDialogsCacheOffset--;
                } else {
                    dialog.unread_count = 0;
                }
                this.dialogMessage.remove(Long.valueOf(dialog.id));
                this.dialogMessagesByIds.remove(Integer.valueOf(dialog.top_message));
                dialog.top_message = 0;
            }
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.removeAllMessagesFromDialog, Long.valueOf(did), Boolean.valueOf(false));
            j = did;
            MessagesStorage.getInstance().getStorageQueue().postRunnable(new Runnable() {

                class C04371 implements Runnable {
                    C04371() {
                    }

                    public void run() {
                        NotificationsController.getInstance().removeNotificationsForDialog(j);
                    }
                }

                public void run() {
                    AndroidUtilities.runOnUIThread(new C04371());
                }
            });
            MessagesStorage.getInstance().deleteDialog(did, onlyHistory);
        }
        if (high_id == 1) {
            return;
        }
        if (lower_part != 0) {
            InputPeer peer = getInputPeer(lower_part);
            if (peer != null && !(peer instanceof TL_inputPeerChannel)) {
                TL_messages_deleteHistory req = new TL_messages_deleteHistory();
                req.peer = peer;
                req.max_id = max_id_delete;
                final int max_id_delete_final = max_id_delete;
                final long j2 = did;
                final int i = onlyHistory;
                ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
                    public void run(TLObject response, TL_error error) {
                        if (error == null) {
                            TL_messages_affectedHistory res = (TL_messages_affectedHistory) response;
                            if (res.offset > 0) {
                                MessagesController.this.deleteDialog(j2, false, i, max_id_delete_final);
                            }
                            MessagesController.this.processNewDifferenceParams(-1, res.pts, -1, res.pts_count);
                        }
                    }
                }, 64);
            }
        } else if (onlyHistory == 1) {
            SecretChatHelper.getInstance().sendClearHistoryMessage(getEncryptedChat(Integer.valueOf(high_id)), null);
        } else {
            SecretChatHelper.getInstance().declineSecretChat(high_id);
        }
    }

    public void loadChannelParticipants(final Integer chat_id) {
        if (!this.loadingFullParticipants.contains(chat_id) && !this.loadedFullParticipants.contains(chat_id)) {
            this.loadingFullParticipants.add(chat_id);
            TL_channels_getParticipants req = new TL_channels_getParticipants();
            req.channel = getInputChannel(chat_id.intValue());
            req.filter = new TL_channelParticipantsRecent();
            req.offset = 0;
            req.limit = 32;
            ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
                public void run(final TLObject response, final TL_error error) {
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        public void run() {
                            if (error == null) {
                                TL_channels_channelParticipants res = response;
                                MessagesController.this.putUsers(res.users, false);
                                MessagesStorage.getInstance().putUsersAndChats(res.users, null, true, true);
                                MessagesStorage.getInstance().updateChannelUsers(chat_id.intValue(), res.participants);
                                MessagesController.this.loadedFullParticipants.add(chat_id);
                            }
                            MessagesController.this.loadingFullParticipants.remove(chat_id);
                        }
                    });
                }
            });
        }
    }

    public void loadChatInfo(int chat_id, Semaphore semaphore, boolean force) {
        MessagesStorage.getInstance().loadChatInfo(chat_id, semaphore, force, false);
    }

    public void processChatInfo(int chat_id, ChatFull info, ArrayList<User> usersArr, boolean fromCache, boolean force, boolean byChannelUsers) {
        if (fromCache && chat_id > 0 && !byChannelUsers) {
            loadFullChat(chat_id, 0, force);
        }
        if (info != null) {
            final ArrayList<User> arrayList = usersArr;
            final boolean z = fromCache;
            final ChatFull chatFull = info;
            final boolean z2 = byChannelUsers;
            AndroidUtilities.runOnUIThread(new Runnable() {
                public void run() {
                    MessagesController.this.putUsers(arrayList, z);
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.chatInfoDidLoaded, chatFull, Integer.valueOf(0), Boolean.valueOf(z2));
                }
            });
        }
    }

    public void updateTimerProc() {
        TLObject req;
        int a;
        int key;
        int b;
        long currentTime = System.currentTimeMillis();
        checkDeletingTask(false);
        if (UserConfig.isClientActivated()) {
            if (ConnectionsManager.getInstance().getPauseTime() == 0 && ApplicationLoader.isScreenOn && !ApplicationLoader.mainInterfacePaused) {
                if (this.statusSettingState != 1 && (this.lastStatusUpdateTime == 0 || Math.abs(System.currentTimeMillis() - this.lastStatusUpdateTime) >= 55000 || this.offlineSent)) {
                    this.statusSettingState = 1;
                    if (this.statusRequest != 0) {
                        ConnectionsManager.getInstance().cancelRequest(this.statusRequest, true);
                    }
                    req = new TL_account_updateStatus();
                    req.offline = false;
                    this.statusRequest = ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
                        public void run(TLObject response, TL_error error) {
                            if (error == null) {
                                MessagesController.this.lastStatusUpdateTime = System.currentTimeMillis();
                                MessagesController.this.offlineSent = false;
                                MessagesController.this.statusSettingState = 0;
                            } else if (MessagesController.this.lastStatusUpdateTime != 0) {
                                MessagesController.access$2414(MessagesController.this, 5000);
                            }
                            MessagesController.this.statusRequest = 0;
                        }
                    });
                }
            } else if (!(this.statusSettingState == 2 || this.offlineSent || Math.abs(System.currentTimeMillis() - ConnectionsManager.getInstance().getPauseTime()) < 2000)) {
                this.statusSettingState = 2;
                if (this.statusRequest != 0) {
                    ConnectionsManager.getInstance().cancelRequest(this.statusRequest, true);
                }
                req = new TL_account_updateStatus();
                req.offline = true;
                this.statusRequest = ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
                    public void run(TLObject response, TL_error error) {
                        if (error == null) {
                            MessagesController.this.offlineSent = true;
                        } else if (MessagesController.this.lastStatusUpdateTime != 0) {
                            MessagesController.access$2414(MessagesController.this, 5000);
                        }
                        MessagesController.this.statusRequest = 0;
                    }
                });
            }
            if (!this.updatesQueueChannels.isEmpty()) {
                ArrayList<Integer> keys = new ArrayList(this.updatesQueueChannels.keySet());
                for (a = 0; a < keys.size(); a++) {
                    key = ((Integer) keys.get(a)).intValue();
                    Long updatesStartWaitTime = (Long) this.updatesStartWaitTimeChannels.get(Integer.valueOf(key));
                    if (updatesStartWaitTime != null && updatesStartWaitTime.longValue() + 1500 < currentTime) {
                        FileLog.m609e("tmessages", "QUEUE CHANNEL " + key + " UPDATES WAIT TIMEOUT - CHECK QUEUE");
                        processChannelsUpdatesQueue(key, 0);
                    }
                }
            }
            a = 0;
            while (a < 3) {
                if (getUpdatesStartTime(a) != 0 && getUpdatesStartTime(a) + 1500 < currentTime) {
                    FileLog.m609e("tmessages", a + " QUEUE UPDATES WAIT TIMEOUT - CHECK QUEUE");
                    processUpdatesQueue(a, 0);
                }
                a++;
            }
        }
        if (!(this.channelViewsToSend.size() == 0 && this.channelViewsToReload.size() == 0) && Math.abs(System.currentTimeMillis() - this.lastViewsCheckTime) >= 5000) {
            this.lastViewsCheckTime = System.currentTimeMillis();
            b = 0;
            while (b < 2) {
                SparseArray<ArrayList<Integer>> array = b == 0 ? this.channelViewsToSend : this.channelViewsToReload;
                if (array.size() != 0) {
                    a = 0;
                    while (a < array.size()) {
                        key = array.keyAt(a);
                        req = new TL_messages_getMessagesViews();
                        req.peer = getInputPeer(key);
                        req.id = (ArrayList) array.get(key);
                        req.increment = a == 0;
                        final TLObject tLObject = req;
                        ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
                            public void run(TLObject response, TL_error error) {
                                if (error == null) {
                                    Vector vector = (Vector) response;
                                    final SparseArray<SparseIntArray> channelViews = new SparseArray();
                                    SparseIntArray array = (SparseIntArray) channelViews.get(key);
                                    if (array == null) {
                                        array = new SparseIntArray();
                                        channelViews.put(key, array);
                                    }
                                    int a = 0;
                                    while (a < tLObject.id.size() && a < vector.objects.size()) {
                                        array.put(((Integer) tLObject.id.get(a)).intValue(), ((Integer) vector.objects.get(a)).intValue());
                                        a++;
                                    }
                                    MessagesStorage.getInstance().putChannelViews(channelViews, tLObject.peer instanceof TL_inputPeerChannel);
                                    AndroidUtilities.runOnUIThread(new Runnable() {
                                        public void run() {
                                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.didUpdatedMessagesViews, channelViews);
                                        }
                                    });
                                }
                            }
                        });
                        a++;
                    }
                    array.clear();
                }
                b++;
            }
        }
        if (!this.onlinePrivacy.isEmpty()) {
            Iterator i$;
            ArrayList<Integer> toRemove = null;
            int currentServerTime = ConnectionsManager.getInstance().getCurrentTime();
            for (Entry<Integer, Integer> entry : this.onlinePrivacy.entrySet()) {
                if (((Integer) entry.getValue()).intValue() < currentServerTime - 30) {
                    if (toRemove == null) {
                        toRemove = new ArrayList();
                    }
                    toRemove.add(entry.getKey());
                }
            }
            if (toRemove != null) {
                i$ = toRemove.iterator();
                while (i$.hasNext()) {
                    this.onlinePrivacy.remove((Integer) i$.next());
                }
                AndroidUtilities.runOnUIThread(new Runnable() {
                    public void run() {
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.updateInterfaces, Integer.valueOf(4));
                    }
                });
            }
        }
        if (this.shortPollChannels.size() != 0) {
            for (a = 0; a < this.shortPollChannels.size(); a++) {
                key = this.shortPollChannels.keyAt(a);
                if (((long) this.shortPollChannels.get(key)) < System.currentTimeMillis() / 1000) {
                    this.shortPollChannels.delete(key);
                    if (this.needShortPollChannels.indexOfKey(key) >= 0) {
                        getChannelDifference(key);
                    }
                }
            }
        }
        if (!this.printingUsers.isEmpty() || this.lastPrintingStringCount != this.printingUsers.size()) {
            boolean updated = false;
            ArrayList<Long> keys2 = new ArrayList(this.printingUsers.keySet());
            b = 0;
            while (b < keys2.size()) {
                Long key2 = (Long) keys2.get(b);
                ArrayList<PrintingUser> arr = (ArrayList) this.printingUsers.get(key2);
                a = 0;
                while (a < arr.size()) {
                    PrintingUser user = (PrintingUser) arr.get(a);
                    if (user.lastTime + 5900 < currentTime) {
                        updated = true;
                        arr.remove(user);
                        a--;
                    }
                    a++;
                }
                if (arr.isEmpty()) {
                    this.printingUsers.remove(key2);
                    keys2.remove(b);
                    b--;
                }
                b++;
            }
            updatePrintingStrings();
            if (updated) {
                AndroidUtilities.runOnUIThread(new Runnable() {
                    public void run() {
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.updateInterfaces, Integer.valueOf(64));
                    }
                });
            }
        }
    }

    private String getUserNameForTyping(User user) {
        if (user == null) {
            return "";
        }
        if (user.first_name != null && user.first_name.length() > 0) {
            return user.first_name;
        }
        if (user.last_name == null || user.last_name.length() <= 0) {
            return "";
        }
        return user.last_name;
    }

    private void updatePrintingStrings() {
        final HashMap<Long, CharSequence> newPrintingStrings = new HashMap();
        final HashMap<Long, Integer> newPrintingStringsTypes = new HashMap();
        ArrayList<Long> keys = new ArrayList(this.printingUsers.keySet());
        for (Entry<Long, ArrayList<PrintingUser>> entry : this.printingUsers.entrySet()) {
            long key = ((Long) entry.getKey()).longValue();
            ArrayList<PrintingUser> arr = (ArrayList) entry.getValue();
            int lower_id = (int) key;
            User user;
            if (lower_id > 0 || lower_id == 0 || arr.size() == 1) {
                PrintingUser pu = (PrintingUser) arr.get(0);
                if (getUser(Integer.valueOf(pu.userId)) != null) {
                    if ((pu.action instanceof TL_sendMessageUploadAudioAction) || (pu.action instanceof TL_sendMessageRecordAudioAction)) {
                        if (lower_id < 0) {
                            newPrintingStrings.put(Long.valueOf(key), LocaleController.formatString("IsRecordingAudio", C0553R.string.IsRecordingAudio, getUserNameForTyping(user)));
                        } else {
                            newPrintingStrings.put(Long.valueOf(key), LocaleController.getString("RecordingAudio", C0553R.string.RecordingAudio));
                        }
                        newPrintingStringsTypes.put(Long.valueOf(key), Integer.valueOf(1));
                    } else if ((pu.action instanceof TL_sendMessageUploadVideoAction) || (pu.action instanceof TL_sendMessageRecordVideoAction)) {
                        if (lower_id < 0) {
                            newPrintingStrings.put(Long.valueOf(key), LocaleController.formatString("IsSendingVideo", C0553R.string.IsSendingVideo, getUserNameForTyping(user)));
                        } else {
                            newPrintingStrings.put(Long.valueOf(key), LocaleController.getString("SendingVideoStatus", C0553R.string.SendingVideoStatus));
                        }
                        newPrintingStringsTypes.put(Long.valueOf(key), Integer.valueOf(2));
                    } else if (pu.action instanceof TL_sendMessageUploadDocumentAction) {
                        if (lower_id < 0) {
                            newPrintingStrings.put(Long.valueOf(key), LocaleController.formatString("IsSendingFile", C0553R.string.IsSendingFile, getUserNameForTyping(user)));
                        } else {
                            newPrintingStrings.put(Long.valueOf(key), LocaleController.getString("SendingFile", C0553R.string.SendingFile));
                        }
                        newPrintingStringsTypes.put(Long.valueOf(key), Integer.valueOf(2));
                    } else if (pu.action instanceof TL_sendMessageUploadPhotoAction) {
                        if (lower_id < 0) {
                            newPrintingStrings.put(Long.valueOf(key), LocaleController.formatString("IsSendingPhoto", C0553R.string.IsSendingPhoto, getUserNameForTyping(user)));
                        } else {
                            newPrintingStrings.put(Long.valueOf(key), LocaleController.getString("SendingPhoto", C0553R.string.SendingPhoto));
                        }
                        newPrintingStringsTypes.put(Long.valueOf(key), Integer.valueOf(2));
                    } else {
                        if (lower_id < 0) {
                            newPrintingStrings.put(Long.valueOf(key), String.format("%s %s", new Object[]{getUserNameForTyping(user), LocaleController.getString("IsTyping", C0553R.string.IsTyping)}));
                        } else {
                            newPrintingStrings.put(Long.valueOf(key), LocaleController.getString("Typing", C0553R.string.Typing));
                        }
                        newPrintingStringsTypes.put(Long.valueOf(key), Integer.valueOf(0));
                    }
                } else {
                    return;
                }
            }
            int count = 0;
            String label = "";
            Iterator i$ = arr.iterator();
            while (i$.hasNext()) {
                user = getUser(Integer.valueOf(((PrintingUser) i$.next()).userId));
                if (user != null) {
                    if (label.length() != 0) {
                        label = label + ", ";
                    }
                    label = label + getUserNameForTyping(user);
                    count++;
                }
                if (count == 2) {
                    break;
                }
            }
            if (label.length() != 0) {
                if (count == 1) {
                    newPrintingStrings.put(Long.valueOf(key), String.format("%s %s", new Object[]{label, LocaleController.getString("IsTyping", C0553R.string.IsTyping)}));
                } else if (arr.size() > 2) {
                    newPrintingStrings.put(Long.valueOf(key), String.format("%s %s", new Object[]{label, LocaleController.formatPluralString("AndMoreTyping", arr.size() - 2)}));
                } else {
                    newPrintingStrings.put(Long.valueOf(key), String.format("%s %s", new Object[]{label, LocaleController.getString("AreTyping", C0553R.string.AreTyping)}));
                }
                newPrintingStringsTypes.put(Long.valueOf(key), Integer.valueOf(0));
            }
        }
        this.lastPrintingStringCount = newPrintingStrings.size();
        AndroidUtilities.runOnUIThread(new Runnable() {
            public void run() {
                MessagesController.this.printingStrings = newPrintingStrings;
                MessagesController.this.printingStringsTypes = newPrintingStringsTypes;
            }
        });
    }

    public void cancelTyping(int action, long dialog_id) {
        HashMap<Long, Boolean> typings = (HashMap) this.sendingTypings.get(Integer.valueOf(action));
        if (typings != null) {
            typings.remove(Long.valueOf(dialog_id));
        }
    }

    public void sendTyping(final long dialog_id, final int action, int classGuid) {
        if (dialog_id != 0) {
            HashMap<Long, Boolean> typings = (HashMap) this.sendingTypings.get(Integer.valueOf(action));
            if (typings == null || typings.get(Long.valueOf(dialog_id)) == null) {
                if (typings == null) {
                    typings = new HashMap();
                    this.sendingTypings.put(Integer.valueOf(action), typings);
                }
                int lower_part = (int) dialog_id;
                int high_id = (int) (dialog_id >> 32);
                int reqId;
                if (lower_part != 0) {
                    if (high_id != 1) {
                        TL_messages_setTyping req = new TL_messages_setTyping();
                        req.peer = getInputPeer(lower_part);
                        if (req.peer instanceof TL_inputPeerChannel) {
                            Chat chat = getInstance().getChat(Integer.valueOf(req.peer.channel_id));
                            if (chat == null || !chat.megagroup) {
                                return;
                            }
                        }
                        if (req.peer != null) {
                            if (action == 0) {
                                req.action = new TL_sendMessageTypingAction();
                            } else if (action == 1) {
                                req.action = new TL_sendMessageRecordAudioAction();
                            } else if (action == 2) {
                                req.action = new TL_sendMessageCancelAction();
                            } else if (action == 3) {
                                req.action = new TL_sendMessageUploadDocumentAction();
                            } else if (action == 4) {
                                req.action = new TL_sendMessageUploadPhotoAction();
                            } else if (action == 5) {
                                req.action = new TL_sendMessageUploadVideoAction();
                            }
                            typings.put(Long.valueOf(dialog_id), Boolean.valueOf(true));
                            reqId = ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {

                                class C04401 implements Runnable {
                                    C04401() {
                                    }

                                    public void run() {
                                        HashMap<Long, Boolean> typings = (HashMap) MessagesController.this.sendingTypings.get(Integer.valueOf(action));
                                        if (typings != null) {
                                            typings.remove(Long.valueOf(dialog_id));
                                        }
                                    }
                                }

                                public void run(TLObject response, TL_error error) {
                                    AndroidUtilities.runOnUIThread(new C04401());
                                }
                            }, 2);
                            if (classGuid != 0) {
                                ConnectionsManager.getInstance().bindRequestToGuid(reqId, classGuid);
                            }
                        }
                    }
                } else if (action == 0) {
                    EncryptedChat chat2 = getEncryptedChat(Integer.valueOf(high_id));
                    if (chat2.auth_key != null && chat2.auth_key.length > 1 && (chat2 instanceof TL_encryptedChat)) {
                        TL_messages_setEncryptedTyping req2 = new TL_messages_setEncryptedTyping();
                        req2.peer = new TL_inputEncryptedChat();
                        req2.peer.chat_id = chat2.id;
                        req2.peer.access_hash = chat2.access_hash;
                        req2.typing = true;
                        typings.put(Long.valueOf(dialog_id), Boolean.valueOf(true));
                        reqId = ConnectionsManager.getInstance().sendRequest(req2, new RequestDelegate() {

                            class C04411 implements Runnable {
                                C04411() {
                                }

                                public void run() {
                                    HashMap<Long, Boolean> typings = (HashMap) MessagesController.this.sendingTypings.get(Integer.valueOf(action));
                                    if (typings != null) {
                                        typings.remove(Long.valueOf(dialog_id));
                                    }
                                }
                            }

                            public void run(TLObject response, TL_error error) {
                                AndroidUtilities.runOnUIThread(new C04411());
                            }
                        }, 2);
                        if (classGuid != 0) {
                            ConnectionsManager.getInstance().bindRequestToGuid(reqId, classGuid);
                        }
                    }
                }
            }
        }
    }

    public void loadMessages(long dialog_id, int count, int max_id, boolean fromCache, int midDate, int classGuid, int load_type, int last_message_id, int important, int loadIndex) {
        loadMessages(dialog_id, count, max_id, fromCache, midDate, classGuid, load_type, last_message_id, important, loadIndex, 0, 0, 0, false);
    }

    public void loadMessages(long dialog_id, int count, int max_id, boolean fromCache, int midDate, int classGuid, int load_type, int last_message_id, int important, int loadIndex, int first_unread, int unread_count, int last_date, boolean queryFromServer) {
        int lower_part = (int) dialog_id;
        if (fromCache || lower_part == 0) {
            MessagesStorage.getInstance().getMessages(dialog_id, count, max_id, midDate, classGuid, load_type, important, loadIndex);
            return;
        }
        TLObject request;
        TLObject req;
        if (important == 2) {
            req = new TL_channels_getImportantHistory();
            req.channel = getInputChannel(-lower_part);
            if (load_type == 3) {
                req.add_offset = (-count) / 2;
            } else if (load_type == 1) {
                req.add_offset = (-count) - 1;
            } else if (load_type == 2 && max_id != 0) {
                req.add_offset = (-count) + 6;
            } else if (max_id != 0) {
                req.add_offset = -1;
                req.limit++;
            }
            req.limit += count;
            req.offset_id = max_id;
            request = req;
        } else {
            req = new TL_messages_getHistory();
            req.peer = getInputPeer(lower_part);
            if (load_type == 3) {
                req.add_offset = (-count) / 2;
            } else if (load_type == 1) {
                req.add_offset = (-count) - 1;
            } else if (load_type != 2 || max_id == 0) {
                req.add_offset = 0;
            } else {
                req.add_offset = (-count) + 6;
            }
            req.limit = count;
            req.offset_id = max_id;
            request = req;
        }
        final int i = count;
        final long j = dialog_id;
        final int i2 = max_id;
        final int i3 = classGuid;
        final int i4 = first_unread;
        final int i5 = last_message_id;
        final int i6 = unread_count;
        final int i7 = last_date;
        final int i8 = load_type;
        final int i9 = important;
        final int i10 = loadIndex;
        final boolean z = queryFromServer;
        ConnectionsManager.getInstance().bindRequestToGuid(ConnectionsManager.getInstance().sendRequest(request, new RequestDelegate() {
            public void run(TLObject response, TL_error error) {
                if (error == null) {
                    messages_Messages res = (messages_Messages) response;
                    if (res.messages.size() > i) {
                        res.messages.remove(0);
                    }
                    MessagesController.this.processLoadedMessages(res, j, i, i2, false, i3, i4, i5, i6, i7, i8, i9, false, i10, z);
                }
            }
        }), classGuid);
    }

    public void processLoadedMessages(messages_Messages messagesRes, long dialog_id, int count, int max_id, boolean isCache, int classGuid, int first_unread, int last_message_id, int unread_count, int last_date, int load_type, int important, boolean isEnd, int loadIndex, boolean queryFromServer) {
        final messages_Messages org_telegram_tgnet_TLRPC_messages_Messages = messagesRes;
        final long j = dialog_id;
        final boolean z = isCache;
        final int i = count;
        final int i2 = load_type;
        final boolean z2 = queryFromServer;
        final int i3 = first_unread;
        final int i4 = max_id;
        final int i5 = classGuid;
        final int i6 = last_message_id;
        final int i7 = important;
        final int i8 = loadIndex;
        final int i9 = unread_count;
        final int i10 = last_date;
        final boolean z3 = isEnd;
        Utilities.stageQueue.postRunnable(new Runnable() {

            class C04421 implements Runnable {
                C04421() {
                }

                public void run() {
                    MessagesController messagesController = MessagesController.this;
                    long j = j;
                    int i = i;
                    int i2 = (i2 == 2 && z2) ? i3 : i4;
                    messagesController.loadMessages(j, i, i2, false, 0, i5, i2, i6, i7, i8, i3, i9, i10, z2);
                }
            }

            public void run() {
                int a;
                boolean createDialog = false;
                boolean isMegagroup = false;
                if (org_telegram_tgnet_TLRPC_messages_Messages instanceof TL_messages_channelMessages) {
                    int channelId = -((int) j);
                    if (((Integer) MessagesController.this.channelsPts.get(Integer.valueOf(channelId))) == null && Integer.valueOf(MessagesStorage.getInstance().getChannelPtsSync(channelId)).intValue() == 0) {
                        MessagesController.this.channelsPts.put(Integer.valueOf(channelId), Integer.valueOf(org_telegram_tgnet_TLRPC_messages_Messages.pts));
                        createDialog = true;
                        MessagesController.this.getChannelDifference(channelId);
                    }
                    for (a = 0; a < org_telegram_tgnet_TLRPC_messages_Messages.chats.size(); a++) {
                        Chat chat = (Chat) org_telegram_tgnet_TLRPC_messages_Messages.chats.get(a);
                        if (chat.id == channelId) {
                            isMegagroup = chat.megagroup;
                            break;
                        }
                    }
                }
                int lower_id = (int) j;
                int high_id = (int) (j >> 32);
                if (!z) {
                    ImageLoader.saveMessagesThumbs(org_telegram_tgnet_TLRPC_messages_Messages.messages);
                }
                if (high_id == 1 || lower_id == 0 || !z || org_telegram_tgnet_TLRPC_messages_Messages.messages.size() != 0) {
                    Message message;
                    AbstractMap usersDict = new HashMap();
                    AbstractMap chatsDict = new HashMap();
                    for (a = 0; a < org_telegram_tgnet_TLRPC_messages_Messages.users.size(); a++) {
                        User u = (User) org_telegram_tgnet_TLRPC_messages_Messages.users.get(a);
                        usersDict.put(Integer.valueOf(u.id), u);
                    }
                    for (a = 0; a < org_telegram_tgnet_TLRPC_messages_Messages.chats.size(); a++) {
                        Chat c = (Chat) org_telegram_tgnet_TLRPC_messages_Messages.chats.get(a);
                        chatsDict.put(Integer.valueOf(c.id), c);
                    }
                    int size = org_telegram_tgnet_TLRPC_messages_Messages.messages.size();
                    if (!z) {
                        for (a = 0; a < size; a++) {
                            message = (Message) org_telegram_tgnet_TLRPC_messages_Messages.messages.get(a);
                            if (!(z || message.from_id > 0 || message.out)) {
                                message.media_unread = true;
                            }
                            if (isMegagroup) {
                                message.flags |= Integer.MIN_VALUE;
                            }
                            if (message.action instanceof TL_messageActionChatDeleteUser) {
                                User user = (User) usersDict.get(Integer.valueOf(message.action.user_id));
                                if (user != null && user.bot) {
                                    message.reply_markup = new TL_replyKeyboardHide();
                                }
                            } else if ((message.action instanceof TL_messageActionChatMigrateTo) || (message.action instanceof TL_messageActionChannelCreate)) {
                                message.unread = false;
                                message.media_unread = false;
                            }
                        }
                        MessagesStorage.getInstance().putMessages(org_telegram_tgnet_TLRPC_messages_Messages, j, i2, i4, i7, createDialog);
                    }
                    ArrayList<MessageObject> objects = new ArrayList();
                    ArrayList<Integer> messagesToReload = new ArrayList();
                    for (a = 0; a < size; a++) {
                        message = (Message) org_telegram_tgnet_TLRPC_messages_Messages.messages.get(a);
                        message.dialog_id = j;
                        objects.add(new MessageObject(message, usersDict, chatsDict, true));
                        if (z) {
                            if (message.media instanceof TL_messageMediaUnsupported) {
                                if (message.media.bytes.length == 0 || (message.media.bytes.length == 1 && message.media.bytes[0] < (byte) 43)) {
                                    messagesToReload.add(Integer.valueOf(message.id));
                                }
                            } else if ((message.media instanceof TL_messageMediaWebPage) && (message.media.webpage instanceof TL_webPagePending) && message.media.webpage.date <= ConnectionsManager.getInstance().getCurrentTime()) {
                                messagesToReload.add(Integer.valueOf(message.id));
                            }
                        }
                    }
                    final ArrayList<MessageObject> arrayList = objects;
                    final ArrayList<Integer> arrayList2 = messagesToReload;
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        public void run() {
                            MessagesController.this.putUsers(org_telegram_tgnet_TLRPC_messages_Messages.users, z);
                            MessagesController.this.putChats(org_telegram_tgnet_TLRPC_messages_Messages.chats, z);
                            int first_unread_final = ConnectionsManager.DEFAULT_DATACENTER_ID;
                            if (z2 && i2 == 2) {
                                for (int a = 0; a < org_telegram_tgnet_TLRPC_messages_Messages.messages.size(); a++) {
                                    Message message = (Message) org_telegram_tgnet_TLRPC_messages_Messages.messages.get(a);
                                    if (!message.out && message.id > i3 && message.id < first_unread_final) {
                                        first_unread_final = message.id;
                                    }
                                }
                            }
                            if (first_unread_final == ConnectionsManager.DEFAULT_DATACENTER_ID) {
                                first_unread_final = i3;
                            }
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.messagesDidLoaded, Long.valueOf(j), Integer.valueOf(i), arrayList, Boolean.valueOf(z), Integer.valueOf(first_unread_final), Integer.valueOf(i6), Integer.valueOf(i9), Integer.valueOf(i10), Integer.valueOf(i2), org_telegram_tgnet_TLRPC_messages_Messages.collapsed, Boolean.valueOf(z3), Integer.valueOf(i5), Integer.valueOf(i8));
                            if (arrayList2 != null) {
                                MessagesController.this.reloadMessages(arrayList2, j);
                            }
                        }
                    });
                    return;
                }
                AndroidUtilities.runOnUIThread(new C04421());
            }
        });
    }

    public void loadDialogs(int offset, final int count, boolean fromCache) {
        if (!this.loadingDialogs) {
            this.loadingDialogs = true;
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
            FileLog.m609e("tmessages", "load cacheOffset = " + offset + " count = " + count + " cache = " + fromCache);
            if (fromCache) {
                MessagesStorage.getInstance().getDialogs(offset == 0 ? 0 : this.nextDialogsCacheOffset, count);
                return;
            }
            TL_messages_getDialogs req = new TL_messages_getDialogs();
            req.limit = count;
            boolean found = false;
            for (int a = this.dialogs.size() - 1; a >= 0; a--) {
                Dialog dialog = (Dialog) this.dialogs.get(a);
                int high_id = (int) (dialog.id >> 32);
                if (!(((int) dialog.id) == 0 || high_id == 1 || dialog.top_message <= 0)) {
                    MessageObject message = (MessageObject) this.dialogMessage.get(Long.valueOf(dialog.id));
                    if (message != null && message.getId() > 0) {
                        int id;
                        req.offset_date = message.messageOwner.date;
                        req.offset_id = message.messageOwner.id;
                        if (message.messageOwner.to_id.channel_id != 0) {
                            id = -message.messageOwner.to_id.channel_id;
                        } else if (message.messageOwner.to_id.chat_id != 0) {
                            id = -message.messageOwner.to_id.chat_id;
                        } else {
                            id = message.messageOwner.to_id.user_id;
                        }
                        req.offset_peer = getInputPeer(id);
                        found = true;
                        if (!found) {
                            req.offset_peer = new TL_inputPeerEmpty();
                        }
                        ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
                            public void run(TLObject response, TL_error error) {
                                if (error == null) {
                                    MessagesController.this.processLoadedDialogs((messages_Dialogs) response, null, 0, count, false, false, false);
                                }
                            }
                        });
                    }
                }
            }
            if (found) {
                req.offset_peer = new TL_inputPeerEmpty();
            }
            ConnectionsManager.getInstance().sendRequest(req, /* anonymous class already generated */);
        }
    }

    private void migrateDialogs(int offset, int offsetDate, int offsetUser, int offsetChat, int offsetChannel, long accessPeer) {
        if (!this.migratingDialogs && offset != -1) {
            this.migratingDialogs = true;
            TL_messages_getDialogs req = new TL_messages_getDialogs();
            req.limit = 100;
            req.offset_id = offset;
            req.offset_date = offsetDate;
            if (offset == 0) {
                req.offset_peer = new TL_inputPeerEmpty();
            } else {
                if (offsetChannel != 0) {
                    req.offset_peer = new TL_inputPeerChannel();
                    req.offset_peer.channel_id = offsetChannel;
                } else if (offsetUser != 0) {
                    req.offset_peer = new TL_inputPeerUser();
                    req.offset_peer.user_id = offsetUser;
                } else {
                    req.offset_peer = new TL_inputPeerChat();
                    req.offset_peer.chat_id = offsetChat;
                }
                req.offset_peer.access_hash = accessPeer;
            }
            ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {

                class C04462 implements Runnable {
                    C04462() {
                    }

                    public void run() {
                        MessagesController.this.migratingDialogs = false;
                    }
                }

                public void run(TLObject response, TL_error error) {
                    if (error == null) {
                        final messages_Dialogs dialogsRes = (messages_Dialogs) response;
                        MessagesStorage.getInstance().getStorageQueue().postRunnable(new Runnable() {

                            class C04441 implements Runnable {
                                C04441() {
                                }

                                public void run() {
                                    MessagesController.this.migratingDialogs = false;
                                }
                            }

                            public void run() {
                                try {
                                    int a;
                                    Message message;
                                    int offsetId;
                                    Dialog dialog;
                                    if (dialogsRes.dialogs.size() == 100) {
                                        Message lastMessage = null;
                                        for (a = 0; a < dialogsRes.messages.size(); a++) {
                                            message = (Message) dialogsRes.messages.get(a);
                                            if (lastMessage == null || message.date < lastMessage.date) {
                                                lastMessage = message;
                                            }
                                        }
                                        offsetId = lastMessage.id;
                                        UserConfig.migrateOffsetDate = lastMessage.date;
                                        Chat chat;
                                        if (lastMessage.to_id.channel_id != 0) {
                                            UserConfig.migrateOffsetChannelId = lastMessage.to_id.channel_id;
                                            UserConfig.migrateOffsetChatId = 0;
                                            UserConfig.migrateOffsetUserId = 0;
                                            for (a = 0; a < dialogsRes.chats.size(); a++) {
                                                chat = (Chat) dialogsRes.chats.get(a);
                                                if (chat.id == UserConfig.migrateOffsetChannelId) {
                                                    UserConfig.migrateOffsetAccess = chat.access_hash;
                                                    break;
                                                }
                                            }
                                        } else if (lastMessage.to_id.chat_id != 0) {
                                            UserConfig.migrateOffsetChatId = lastMessage.to_id.chat_id;
                                            UserConfig.migrateOffsetChannelId = 0;
                                            UserConfig.migrateOffsetUserId = 0;
                                            for (a = 0; a < dialogsRes.chats.size(); a++) {
                                                chat = (Chat) dialogsRes.chats.get(a);
                                                if (chat.id == UserConfig.migrateOffsetChatId) {
                                                    UserConfig.migrateOffsetAccess = chat.access_hash;
                                                    break;
                                                }
                                            }
                                        } else if (lastMessage.to_id.user_id != 0) {
                                            UserConfig.migrateOffsetUserId = lastMessage.to_id.user_id;
                                            UserConfig.migrateOffsetChatId = 0;
                                            UserConfig.migrateOffsetChannelId = 0;
                                            for (a = 0; a < dialogsRes.users.size(); a++) {
                                                User user = (User) dialogsRes.users.get(a);
                                                if (user.id == UserConfig.migrateOffsetUserId) {
                                                    UserConfig.migrateOffsetAccess = user.access_hash;
                                                    break;
                                                }
                                            }
                                        }
                                    } else {
                                        offsetId = -1;
                                    }
                                    StringBuilder stringBuilder = new StringBuilder(dialogsRes.dialogs.size() * 12);
                                    HashMap<Long, Dialog> dialogHashMap = new HashMap();
                                    for (a = 0; a < dialogsRes.dialogs.size(); a++) {
                                        dialog = (Dialog) dialogsRes.dialogs.get(a);
                                        if (dialog.peer.channel_id != 0) {
                                            dialog.id = (long) (-dialog.peer.channel_id);
                                        } else if (dialog.peer.chat_id != 0) {
                                            dialog.id = (long) (-dialog.peer.chat_id);
                                        } else {
                                            dialog.id = (long) dialog.peer.user_id;
                                        }
                                        if (stringBuilder.length() > 0) {
                                            stringBuilder.append(",");
                                        }
                                        stringBuilder.append(dialog.id);
                                        dialogHashMap.put(Long.valueOf(dialog.id), dialog);
                                    }
                                    SQLiteCursor cursor = MessagesStorage.getInstance().getDatabase().queryFinalized(String.format(Locale.US, "SELECT did FROM dialogs WHERE did IN (%s)", new Object[]{stringBuilder.toString()}), new Object[0]);
                                    while (cursor.next()) {
                                        long did = cursor.longValue(0);
                                        dialog = (Dialog) dialogHashMap.remove(Long.valueOf(did));
                                        if (dialog != null) {
                                            dialogsRes.dialogs.remove(dialog);
                                            a = 0;
                                            while (a < dialogsRes.messages.size()) {
                                                message = (Message) dialogsRes.messages.get(a);
                                                if (MessageObject.getDialogId(message) == did) {
                                                    dialogsRes.messages.remove(a);
                                                    a--;
                                                    if (message.id == dialog.top_message) {
                                                        dialog.top_message = 0;
                                                    }
                                                    if (message.id == dialog.top_not_important_message) {
                                                        dialog.top_not_important_message = 0;
                                                    }
                                                    if (dialog.top_message == 0 && dialog.top_not_important_message == 0) {
                                                        break;
                                                    }
                                                }
                                                a++;
                                            }
                                        }
                                    }
                                    cursor.dispose();
                                    cursor = MessagesStorage.getInstance().getDatabase().queryFinalized("SELECT min(date) FROM dialogs WHERE date != 0 AND did >> 32 IN (0, -1)", new Object[0]);
                                    if (cursor.next()) {
                                        int date = Math.max(1441062000, cursor.intValue(0));
                                        a = 0;
                                        while (a < dialogsRes.messages.size()) {
                                            message = (Message) dialogsRes.messages.get(a);
                                            if (message.date < date) {
                                                offsetId = -1;
                                                dialogsRes.messages.remove(a);
                                                a--;
                                                dialog = (Dialog) dialogHashMap.remove(Long.valueOf(MessageObject.getDialogId(message)));
                                                if (dialog != null) {
                                                    dialogsRes.dialogs.remove(dialog);
                                                }
                                            }
                                            a++;
                                        }
                                    }
                                    cursor.dispose();
                                    MessagesController.getInstance().processLoadedDialogs(dialogsRes, null, offsetId, 0, false, false, true);
                                } catch (Throwable e) {
                                    FileLog.m611e("tmessages", e);
                                    AndroidUtilities.runOnUIThread(new C04441());
                                }
                            }
                        });
                        return;
                    }
                    AndroidUtilities.runOnUIThread(new C04462());
                }
            });
        }
    }

    public void processLoadedDialogs(messages_Dialogs dialogsRes, ArrayList<EncryptedChat> encChats, int offset, int count, boolean isCache, boolean resetEnd, boolean migrate) {
        final boolean z = isCache;
        final messages_Dialogs org_telegram_tgnet_TLRPC_messages_Dialogs = dialogsRes;
        final boolean z2 = resetEnd;
        final int i = count;
        final int i2 = offset;
        final ArrayList<EncryptedChat> arrayList = encChats;
        final boolean z3 = migrate;
        Utilities.stageQueue.postRunnable(new Runnable() {

            class C04481 implements Runnable {
                C04481() {
                }

                public void run() {
                    MessagesController.this.putUsers(org_telegram_tgnet_TLRPC_messages_Dialogs.users, true);
                    MessagesController.this.loadingDialogs = false;
                    if (z2) {
                        MessagesController.this.dialogsEndReached = false;
                    }
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
                    MessagesController.this.loadDialogs(0, i, false);
                }
            }

            public void run() {
                FileLog.m609e("tmessages", "loaded from " + z + " count " + org_telegram_tgnet_TLRPC_messages_Dialogs.dialogs.size());
                if (z && org_telegram_tgnet_TLRPC_messages_Dialogs.dialogs.size() == 0) {
                    AndroidUtilities.runOnUIThread(new C04481());
                    return;
                }
                int a;
                final HashMap<Long, Dialog> new_dialogs_dict = new HashMap();
                final HashMap<Long, MessageObject> new_dialogMessage = new HashMap();
                HashMap<Integer, User> usersDict = new HashMap();
                final HashMap<Integer, Chat> chatsDict = new HashMap();
                for (a = 0; a < org_telegram_tgnet_TLRPC_messages_Dialogs.users.size(); a++) {
                    User u = (User) org_telegram_tgnet_TLRPC_messages_Dialogs.users.get(a);
                    usersDict.put(Integer.valueOf(u.id), u);
                }
                for (a = 0; a < org_telegram_tgnet_TLRPC_messages_Dialogs.chats.size(); a++) {
                    Chat c = (Chat) org_telegram_tgnet_TLRPC_messages_Dialogs.chats.get(a);
                    chatsDict.put(Integer.valueOf(c.id), c);
                }
                if (z) {
                    MessagesController.this.nextDialogsCacheOffset = i2 + i;
                }
                for (a = 0; a < org_telegram_tgnet_TLRPC_messages_Dialogs.messages.size(); a++) {
                    Chat chat;
                    Message message = (Message) org_telegram_tgnet_TLRPC_messages_Dialogs.messages.get(a);
                    MessageObject messageObject;
                    if (message.to_id.channel_id != 0) {
                        chat = (Chat) chatsDict.get(Integer.valueOf(message.to_id.channel_id));
                        if (chat == null || !chat.left || chat.megagroup) {
                            if (chat != null && chat.megagroup) {
                                message.flags |= Integer.MIN_VALUE;
                            }
                            if (!(z || message.from_id > 0 || message.out)) {
                                message.media_unread = true;
                            }
                            messageObject = new MessageObject(message, usersDict, chatsDict, false);
                            if (((MessageObject) new_dialogMessage.get(Long.valueOf(messageObject.getDialogId()))) != null || messageObject.isMegagroup() || messageObject.isImportant()) {
                                new_dialogMessage.put(Long.valueOf(messageObject.getDialogId()), messageObject);
                            }
                        }
                    } else {
                        if (message.to_id.chat_id != 0) {
                            chat = (Chat) chatsDict.get(Integer.valueOf(message.to_id.chat_id));
                            if (!(chat == null || chat.migrated_to == null)) {
                            }
                        }
                        message.media_unread = true;
                        messageObject = new MessageObject(message, usersDict, chatsDict, false);
                        if (((MessageObject) new_dialogMessage.get(Long.valueOf(messageObject.getDialogId()))) != null) {
                        }
                        new_dialogMessage.put(Long.valueOf(messageObject.getDialogId()), messageObject);
                    }
                }
                for (a = 0; a < org_telegram_tgnet_TLRPC_messages_Dialogs.dialogs.size(); a++) {
                    Dialog d = (Dialog) org_telegram_tgnet_TLRPC_messages_Dialogs.dialogs.get(a);
                    if (d.id == 0 && d.peer != null) {
                        if (d.peer.user_id != 0) {
                            d.id = (long) d.peer.user_id;
                        } else if (d.peer.chat_id != 0) {
                            d.id = (long) (-d.peer.chat_id);
                        } else if (d.peer.channel_id != 0) {
                            d.id = (long) (-d.peer.channel_id);
                        }
                    }
                    if (d.id != 0) {
                        if (d instanceof TL_dialogChannel) {
                            chat = (Chat) chatsDict.get(Integer.valueOf(-((int) d.id)));
                            if (chat != null && chat.megagroup) {
                                d.top_message = Math.max(d.top_message, d.top_not_important_message);
                                d.unread_count = Math.max(d.unread_count, d.unread_not_important_count);
                            }
                            if (chat == null || !chat.left || chat.megagroup) {
                                MessagesController.this.channelsPts.put(Integer.valueOf(-((int) d.id)), Integer.valueOf(d.pts));
                            }
                        } else if (((int) d.id) < 0) {
                            chat = (Chat) chatsDict.get(Integer.valueOf(-((int) d.id)));
                            if (!(chat == null || chat.migrated_to == null)) {
                            }
                        }
                        if (d.last_message_date == 0) {
                            MessageObject mess = (MessageObject) new_dialogMessage.get(Long.valueOf(d.id));
                            if (mess != null) {
                                d.last_message_date = mess.messageOwner.date;
                            }
                        }
                        new_dialogs_dict.put(Long.valueOf(d.id), d);
                        Integer value = (Integer) MessagesController.this.dialogs_read_inbox_max.get(Long.valueOf(d.id));
                        if (value == null) {
                            value = Integer.valueOf(0);
                        }
                        MessagesController.this.dialogs_read_inbox_max.put(Long.valueOf(d.id), Integer.valueOf(Math.max(value.intValue(), d.read_inbox_max_id)));
                    }
                }
                if (!z) {
                    ImageLoader.saveMessagesThumbs(org_telegram_tgnet_TLRPC_messages_Dialogs.messages);
                    for (a = 0; a < org_telegram_tgnet_TLRPC_messages_Dialogs.messages.size(); a++) {
                        message = (Message) org_telegram_tgnet_TLRPC_messages_Dialogs.messages.get(a);
                        if (message.action instanceof TL_messageActionChatDeleteUser) {
                            User user = (User) usersDict.get(Integer.valueOf(message.action.user_id));
                            if (user != null && user.bot) {
                                message.reply_markup = new TL_replyKeyboardHide();
                            }
                        } else if ((message.action instanceof TL_messageActionChatMigrateTo) || (message.action instanceof TL_messageActionChannelCreate)) {
                            message.unread = false;
                            message.media_unread = false;
                        }
                    }
                    MessagesStorage.getInstance().putDialogs(org_telegram_tgnet_TLRPC_messages_Dialogs);
                }
                AndroidUtilities.runOnUIThread(new Runnable() {

                    class C04491 implements Comparator<Dialog> {
                        C04491() {
                        }

                        public int compare(Dialog tl_dialog, Dialog tl_dialog2) {
                            if (tl_dialog.last_message_date == tl_dialog2.last_message_date) {
                                return 0;
                            }
                            if (tl_dialog.last_message_date < tl_dialog2.last_message_date) {
                                return 1;
                            }
                            return -1;
                        }
                    }

                    public void run() {
                        int a;
                        if (!z) {
                            MessagesController.this.applyDialogsNotificationsSettings(org_telegram_tgnet_TLRPC_messages_Dialogs.dialogs);
                        }
                        MessagesController.this.putUsers(org_telegram_tgnet_TLRPC_messages_Dialogs.users, z);
                        MessagesController.this.putChats(org_telegram_tgnet_TLRPC_messages_Dialogs.chats, z);
                        if (arrayList != null) {
                            for (a = 0; a < arrayList.size(); a++) {
                                EncryptedChat encryptedChat = (EncryptedChat) arrayList.get(a);
                                if ((encryptedChat instanceof TL_encryptedChat) && AndroidUtilities.getMyLayerVersion(encryptedChat.layer) < 23) {
                                    SecretChatHelper.getInstance().sendNotifyLayerMessage(encryptedChat, null);
                                }
                                MessagesController.this.putEncryptedChat(encryptedChat, true);
                            }
                        }
                        if (!z3) {
                            MessagesController.this.loadingDialogs = false;
                        }
                        boolean added = false;
                        int lastDialogDate = (!z3 || MessagesController.this.dialogs.isEmpty()) ? 0 : ((Dialog) MessagesController.this.dialogs.get(MessagesController.this.dialogs.size() - 1)).last_message_date;
                        for (Entry<Long, Dialog> pair : new_dialogs_dict.entrySet()) {
                            Long key = (Long) pair.getKey();
                            Dialog value = (Dialog) pair.getValue();
                            if (!z3 || lastDialogDate == 0 || value.last_message_date >= lastDialogDate) {
                                Dialog currentDialog = (Dialog) MessagesController.this.dialogs_dict.get(key);
                                MessageObject messageObject;
                                if (currentDialog == null) {
                                    added = true;
                                    MessagesController.this.dialogs_dict.put(key, value);
                                    messageObject = (MessageObject) new_dialogMessage.get(Long.valueOf(value.id));
                                    MessagesController.this.dialogMessage.put(key, messageObject);
                                    if (messageObject != null && messageObject.messageOwner.to_id.channel_id == 0) {
                                        MessagesController.this.dialogMessagesByIds.put(Integer.valueOf(messageObject.getId()), messageObject);
                                    }
                                } else {
                                    MessageObject oldMsg = (MessageObject) MessagesController.this.dialogMessage.get(key);
                                    if ((oldMsg == null || !oldMsg.deleted) && oldMsg != null && currentDialog.top_message <= 0) {
                                        MessageObject newMsg = (MessageObject) new_dialogMessage.get(Long.valueOf(value.id));
                                        if (oldMsg.deleted || newMsg == null || newMsg.messageOwner.date > oldMsg.messageOwner.date) {
                                            MessagesController.this.dialogs_dict.put(key, value);
                                            MessagesController.this.dialogMessage.put(key, newMsg);
                                            if (newMsg != null && newMsg.messageOwner.to_id.channel_id == 0) {
                                                MessagesController.this.dialogMessagesByIds.put(Integer.valueOf(newMsg.getId()), newMsg);
                                            }
                                        }
                                    } else if (value.top_message >= currentDialog.top_message) {
                                        MessagesController.this.dialogs_dict.put(key, value);
                                        messageObject = (MessageObject) new_dialogMessage.get(Long.valueOf(value.id));
                                        MessagesController.this.dialogMessage.put(key, messageObject);
                                        if (messageObject != null && messageObject.messageOwner.to_id.channel_id == 0) {
                                            MessagesController.this.dialogMessagesByIds.put(Integer.valueOf(messageObject.getId()), messageObject);
                                        }
                                    }
                                }
                            }
                        }
                        MessagesController.this.dialogs.clear();
                        MessagesController.this.dialogsServerOnly.clear();
                        MessagesController.this.dialogsGroupsOnly.clear();
                        MessagesController.this.dialogs.addAll(MessagesController.this.dialogs_dict.values());
                        Collections.sort(MessagesController.this.dialogs, new C04491());
                        a = 0;
                        while (a < MessagesController.this.dialogs.size()) {
                            Dialog d = (Dialog) MessagesController.this.dialogs.get(a);
                            int high_id = (int) (d.id >> 32);
                            if (!(((int) d.id) == 0 || high_id == 1)) {
                                Chat chat;
                                if (d instanceof TL_dialog) {
                                    if (d.id < 0) {
                                        if (z3) {
                                            chat = (Chat) chatsDict.get(Integer.valueOf(-((int) d.id)));
                                            if (!(chat == null || chat.migrated_to == null)) {
                                                MessagesController.this.dialogs.remove(a);
                                                a--;
                                            }
                                        }
                                        MessagesController.this.dialogsGroupsOnly.add(d);
                                    }
                                } else if (d instanceof TL_dialogChannel) {
                                    chat = MessagesController.this.getChat(Integer.valueOf(-((int) d.id)));
                                    if (chat != null && ((chat.megagroup && chat.editor) || chat.creator)) {
                                        MessagesController.this.dialogsGroupsOnly.add(d);
                                    }
                                }
                                MessagesController.this.dialogsServerOnly.add(d);
                            }
                            a++;
                        }
                        if (!z3) {
                            MessagesController messagesController = MessagesController.this;
                            boolean z = (org_telegram_tgnet_TLRPC_messages_Dialogs.dialogs.size() == 0 || org_telegram_tgnet_TLRPC_messages_Dialogs.dialogs.size() != i) && !z;
                            messagesController.dialogsEndReached = z;
                        }
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
                        if (z3) {
                            UserConfig.migrateOffsetId = i2;
                            UserConfig.saveConfig(false);
                            MessagesController.this.migratingDialogs = false;
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.needReloadRecentDialogsSearch, new Object[0]);
                        } else {
                            MessagesController.this.generateUpdateMessage();
                            if (!added && z) {
                                MessagesController.this.loadDialogs(0, i, false);
                            }
                        }
                        MessagesController.this.migrateDialogs(UserConfig.migrateOffsetId, UserConfig.migrateOffsetDate, UserConfig.migrateOffsetUserId, UserConfig.migrateOffsetChatId, UserConfig.migrateOffsetChannelId, UserConfig.migrateOffsetAccess);
                    }
                });
            }
        });
    }

    private void applyDialogNotificationsSettings(long dialog_id, PeerNotifySettings notify_settings) {
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", 0);
        int currentValue = preferences.getInt("notify2_" + dialog_id, 0);
        int currentValue2 = preferences.getInt("notifyuntil_" + dialog_id, 0);
        Editor editor = null;
        Dialog dialog = (Dialog) this.dialogs_dict.get(Long.valueOf(dialog_id));
        if (dialog != null) {
            dialog.notify_settings = notify_settings;
        }
        if (notify_settings.mute_until > ConnectionsManager.getInstance().getCurrentTime()) {
            int until = 0;
            if (notify_settings.mute_until > ConnectionsManager.getInstance().getCurrentTime() + 31536000) {
                if (currentValue != 2) {
                    editor = preferences.edit();
                    editor.putInt("notify2_" + dialog_id, 2);
                    if (dialog != null) {
                        dialog.notify_settings.mute_until = ConnectionsManager.DEFAULT_DATACENTER_ID;
                    }
                }
            } else if (!(currentValue == 3 && currentValue2 == notify_settings.mute_until)) {
                until = notify_settings.mute_until;
                editor = preferences.edit();
                editor.putInt("notify2_" + dialog_id, 3);
                editor.putInt("notifyuntil_" + dialog_id, notify_settings.mute_until);
                if (dialog != null) {
                    dialog.notify_settings.mute_until = until;
                }
            }
            MessagesStorage.getInstance().setDialogFlags(dialog_id, (((long) until) << 32) | 1);
            NotificationsController.getInstance().removeNotificationsForDialog(dialog_id);
        } else {
            if (currentValue != 0) {
                if (dialog != null) {
                    dialog.notify_settings.mute_until = 0;
                }
                editor = preferences.edit();
                editor.remove("notify2_" + dialog_id);
            }
            MessagesStorage.getInstance().setDialogFlags(dialog_id, 0);
        }
        if (editor != null) {
            editor.commit();
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.notificationsSettingsUpdated, new Object[0]);
        }
    }

    private void applyDialogsNotificationsSettings(ArrayList<Dialog> dialogs) {
        Editor editor = null;
        for (int a = 0; a < dialogs.size(); a++) {
            Dialog dialog = (Dialog) dialogs.get(a);
            if (dialog.peer != null && (dialog.notify_settings instanceof TL_peerNotifySettings)) {
                int dialog_id;
                if (editor == null) {
                    editor = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", 0).edit();
                }
                if (dialog.peer.user_id != 0) {
                    dialog_id = dialog.peer.user_id;
                } else if (dialog.peer.chat_id != 0) {
                    dialog_id = -dialog.peer.chat_id;
                } else {
                    dialog_id = -dialog.peer.channel_id;
                }
                if (dialog.notify_settings.mute_until == 0) {
                    editor.remove("notify2_" + dialog_id);
                } else if (dialog.notify_settings.mute_until > ConnectionsManager.getInstance().getCurrentTime() + 31536000) {
                    editor.putInt("notify2_" + dialog_id, 2);
                    dialog.notify_settings.mute_until = ConnectionsManager.DEFAULT_DATACENTER_ID;
                } else {
                    editor.putInt("notify2_" + dialog_id, 3);
                    editor.putInt("notifyuntil_" + dialog_id, dialog.notify_settings.mute_until);
                }
            }
        }
        if (editor != null) {
            editor.commit();
        }
    }

    public void processDialogsUpdateRead(final HashMap<Long, Integer> dialogsToUpdate) {
        AndroidUtilities.runOnUIThread(new Runnable() {
            public void run() {
                for (Entry<Long, Integer> entry : dialogsToUpdate.entrySet()) {
                    Dialog currentDialog = (Dialog) MessagesController.this.dialogs_dict.get(entry.getKey());
                    if (currentDialog != null) {
                        currentDialog.unread_count = ((Integer) entry.getValue()).intValue();
                    }
                }
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.updateInterfaces, Integer.valueOf(256));
                NotificationsController.getInstance().processDialogsUpdateRead(dialogsToUpdate);
            }
        });
    }

    public void processDialogsUpdate(final messages_Dialogs dialogsRes, ArrayList<EncryptedChat> arrayList) {
        Utilities.stageQueue.postRunnable(new Runnable() {
            public void run() {
                int a;
                final HashMap<Long, Dialog> new_dialogs_dict = new HashMap();
                final HashMap<Long, MessageObject> new_dialogMessage = new HashMap();
                HashMap<Integer, User> usersDict = new HashMap();
                HashMap<Integer, Chat> chatsDict = new HashMap();
                final HashMap<Long, Integer> dialogsToUpdate = new HashMap();
                for (a = 0; a < dialogsRes.users.size(); a++) {
                    User u = (User) dialogsRes.users.get(a);
                    usersDict.put(Integer.valueOf(u.id), u);
                }
                for (a = 0; a < dialogsRes.chats.size(); a++) {
                    Chat c = (Chat) dialogsRes.chats.get(a);
                    chatsDict.put(Integer.valueOf(c.id), c);
                }
                for (a = 0; a < dialogsRes.messages.size(); a++) {
                    Chat chat;
                    Message message = (Message) dialogsRes.messages.get(a);
                    MessageObject messageObject;
                    if (message.to_id.channel_id != 0) {
                        chat = (Chat) chatsDict.get(Integer.valueOf(message.to_id.channel_id));
                        if (!(chat == null || !chat.left || chat.megagroup)) {
                        }
                        messageObject = new MessageObject(message, usersDict, chatsDict, false);
                        new_dialogMessage.put(Long.valueOf(messageObject.getDialogId()), messageObject);
                    } else {
                        if (message.to_id.chat_id != 0) {
                            chat = (Chat) chatsDict.get(Integer.valueOf(message.to_id.chat_id));
                            if (!(chat == null || chat.migrated_to == null)) {
                            }
                        }
                        messageObject = new MessageObject(message, usersDict, chatsDict, false);
                        new_dialogMessage.put(Long.valueOf(messageObject.getDialogId()), messageObject);
                    }
                }
                for (a = 0; a < dialogsRes.dialogs.size(); a++) {
                    Dialog d = (Dialog) dialogsRes.dialogs.get(a);
                    if (d.id == 0) {
                        if (d.peer.user_id != 0) {
                            d.id = (long) d.peer.user_id;
                        } else if (d.peer.chat_id != 0) {
                            d.id = (long) (-d.peer.chat_id);
                        } else if (d.peer.channel_id != 0) {
                            d.id = (long) (-d.peer.channel_id);
                        }
                    }
                    MessageObject mess;
                    Integer value;
                    if (d instanceof TL_dialogChannel) {
                        chat = (Chat) chatsDict.get(Integer.valueOf(-((int) d.id)));
                        if (!(chat == null || !chat.left || chat.megagroup)) {
                        }
                        if (d.last_message_date == 0) {
                            mess = (MessageObject) new_dialogMessage.get(Long.valueOf(d.id));
                            if (mess != null) {
                                d.last_message_date = mess.messageOwner.date;
                            }
                        }
                        new_dialogs_dict.put(Long.valueOf(d.id), d);
                        dialogsToUpdate.put(Long.valueOf(d.id), Integer.valueOf(d.unread_count));
                        value = (Integer) MessagesController.this.dialogs_read_inbox_max.get(Long.valueOf(d.id));
                        if (value == null) {
                            value = Integer.valueOf(0);
                        }
                        MessagesController.this.dialogs_read_inbox_max.put(Long.valueOf(d.id), Integer.valueOf(Math.max(value.intValue(), d.read_inbox_max_id)));
                    } else {
                        if (((int) d.id) < 0) {
                            chat = (Chat) chatsDict.get(Integer.valueOf(-((int) d.id)));
                            if (!(chat == null || chat.migrated_to == null)) {
                            }
                        }
                        if (d.last_message_date == 0) {
                            mess = (MessageObject) new_dialogMessage.get(Long.valueOf(d.id));
                            if (mess != null) {
                                d.last_message_date = mess.messageOwner.date;
                            }
                        }
                        new_dialogs_dict.put(Long.valueOf(d.id), d);
                        dialogsToUpdate.put(Long.valueOf(d.id), Integer.valueOf(d.unread_count));
                        value = (Integer) MessagesController.this.dialogs_read_inbox_max.get(Long.valueOf(d.id));
                        if (value == null) {
                            value = Integer.valueOf(0);
                        }
                        MessagesController.this.dialogs_read_inbox_max.put(Long.valueOf(d.id), Integer.valueOf(Math.max(value.intValue(), d.read_inbox_max_id)));
                    }
                }
                AndroidUtilities.runOnUIThread(new Runnable() {

                    class C04511 implements Comparator<Dialog> {
                        C04511() {
                        }

                        public int compare(Dialog dialog, Dialog dialog2) {
                            if (dialog.last_message_date == dialog2.last_message_date) {
                                return 0;
                            }
                            if (dialog.last_message_date < dialog2.last_message_date) {
                                return 1;
                            }
                            return -1;
                        }
                    }

                    public void run() {
                        MessagesController.this.putUsers(dialogsRes.users, true);
                        MessagesController.this.putChats(dialogsRes.chats, true);
                        for (Entry<Long, Dialog> pair : new_dialogs_dict.entrySet()) {
                            Long key = (Long) pair.getKey();
                            Dialog value = (Dialog) pair.getValue();
                            Dialog currentDialog = (Dialog) MessagesController.this.dialogs_dict.get(key);
                            MessageObject messageObject;
                            if (currentDialog == null) {
                                MessagesController messagesController = MessagesController.this;
                                messagesController.nextDialogsCacheOffset++;
                                MessagesController.this.dialogs_dict.put(key, value);
                                messageObject = (MessageObject) new_dialogMessage.get(Long.valueOf(value.id));
                                MessagesController.this.dialogMessage.put(key, messageObject);
                                if (messageObject != null && messageObject.messageOwner.to_id.channel_id == 0) {
                                    MessagesController.this.dialogMessagesByIds.put(Integer.valueOf(messageObject.getId()), messageObject);
                                }
                            } else {
                                currentDialog.unread_count = value.unread_count;
                                MessageObject oldMsg = (MessageObject) MessagesController.this.dialogMessage.get(key);
                                if (oldMsg != null && currentDialog.top_message <= 0) {
                                    MessageObject newMsg = (MessageObject) new_dialogMessage.get(Long.valueOf(value.id));
                                    if (oldMsg.deleted || newMsg == null || newMsg.messageOwner.date > oldMsg.messageOwner.date) {
                                        MessagesController.this.dialogs_dict.put(key, value);
                                        MessagesController.this.dialogMessage.put(key, newMsg);
                                        if (newMsg != null && newMsg.messageOwner.to_id.channel_id == 0) {
                                            MessagesController.this.dialogMessagesByIds.put(Integer.valueOf(newMsg.getId()), newMsg);
                                        }
                                    }
                                } else if ((oldMsg != null && oldMsg.deleted) || value.top_message > currentDialog.top_message) {
                                    MessagesController.this.dialogs_dict.put(key, value);
                                    messageObject = (MessageObject) new_dialogMessage.get(Long.valueOf(value.id));
                                    MessagesController.this.dialogMessage.put(key, messageObject);
                                    if (messageObject != null && messageObject.messageOwner.to_id.channel_id == 0) {
                                        MessagesController.this.dialogMessagesByIds.put(Integer.valueOf(messageObject.getId()), messageObject);
                                    }
                                }
                            }
                        }
                        MessagesController.this.dialogs.clear();
                        MessagesController.this.dialogsServerOnly.clear();
                        MessagesController.this.dialogsGroupsOnly.clear();
                        MessagesController.this.dialogs.addAll(MessagesController.this.dialogs_dict.values());
                        Collections.sort(MessagesController.this.dialogs, new C04511());
                        for (int a = 0; a < MessagesController.this.dialogs.size(); a++) {
                            Dialog d = (Dialog) MessagesController.this.dialogs.get(a);
                            int high_id = (int) (d.id >> 32);
                            if (!(((int) d.id) == 0 || high_id == 1)) {
                                MessagesController.this.dialogsServerOnly.add(d);
                                if (d instanceof TL_dialog) {
                                    if (d.id < 0) {
                                        MessagesController.this.dialogsGroupsOnly.add(d);
                                    }
                                } else if (d instanceof TL_dialogChannel) {
                                    Chat chat = MessagesController.this.getChat(Integer.valueOf(-((int) d.id)));
                                    if (chat != null && ((chat.megagroup && chat.editor) || chat.creator)) {
                                        MessagesController.this.dialogsGroupsOnly.add(d);
                                    }
                                }
                            }
                        }
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
                        NotificationsController.getInstance().processDialogsUpdateRead(dialogsToUpdate);
                    }
                });
            }
        });
    }

    public void addToViewsQueue(final Message message, boolean reload) {
        ArrayList<Long> arrayList = new ArrayList();
        long messageId = (long) message.id;
        if (message.to_id.channel_id != 0) {
            messageId |= ((long) message.to_id.channel_id) << 32;
        }
        arrayList.add(Long.valueOf(messageId));
        MessagesStorage.getInstance().markMessagesContentAsRead(arrayList);
        Utilities.stageQueue.postRunnable(new Runnable() {
            public void run() {
                int peer;
                SparseArray<ArrayList<Integer>> array = MessagesController.this.channelViewsToSend;
                if (message.to_id.channel_id != 0) {
                    peer = -message.to_id.channel_id;
                } else if (message.to_id.chat_id != 0) {
                    peer = -message.to_id.chat_id;
                } else {
                    peer = message.to_id.user_id;
                }
                ArrayList<Integer> ids = (ArrayList) array.get(peer);
                if (ids == null) {
                    ids = new ArrayList();
                    array.put(peer, ids);
                }
                if (!ids.contains(Integer.valueOf(message.id))) {
                    ids.add(Integer.valueOf(message.id));
                }
            }
        });
    }

    public void markMessageContentAsRead(Message message) {
        ArrayList<Long> arrayList = new ArrayList();
        long messageId = (long) message.id;
        if (message.to_id.channel_id != 0) {
            messageId |= ((long) message.to_id.channel_id) << 32;
        }
        arrayList.add(Long.valueOf(messageId));
        MessagesStorage.getInstance().markMessagesContentAsRead(arrayList);
        TL_messages_readMessageContents req = new TL_messages_readMessageContents();
        req.id.add(Integer.valueOf(message.id));
        NotificationCenter.getInstance().postNotificationName(NotificationCenter.messagesReadContent, arrayList);
        ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
            public void run(TLObject response, TL_error error) {
                if (error == null) {
                    TL_messages_affectedMessages res = (TL_messages_affectedMessages) response;
                    MessagesController.this.processNewDifferenceParams(-1, res.pts, -1, res.pts_count);
                }
            }
        });
    }

    public void markMessageAsRead(long dialog_id, long random_id, int ttl) {
        if (random_id != 0 && dialog_id != 0 && ttl > 0) {
            int high_id = (int) (dialog_id >> 32);
            if (((int) dialog_id) == 0) {
                EncryptedChat chat = getEncryptedChat(Integer.valueOf(high_id));
                if (chat != null) {
                    ArrayList<Long> random_ids = new ArrayList();
                    random_ids.add(Long.valueOf(random_id));
                    SecretChatHelper.getInstance().sendMessagesReadMessage(chat, random_ids, null);
                    int time = ConnectionsManager.getInstance().getCurrentTime();
                    MessagesStorage.getInstance().createTaskForSecretChat(chat.id, time, time, 0, random_ids);
                }
            }
        }
    }

    public void markDialogAsRead(long dialog_id, int max_id, int max_positive_id, int max_date, boolean was, boolean popup) {
        int lower_part = (int) dialog_id;
        int high_id = (int) (dialog_id >> 32);
        TLObject req;
        if (lower_part != 0) {
            if (max_positive_id != 0 && high_id != 1) {
                InputPeer inputPeer = getInputPeer(lower_part);
                long messageId = (long) max_positive_id;
                TLObject request;
                if (inputPeer instanceof TL_inputPeerChannel) {
                    request = new TL_channels_readHistory();
                    request.channel = getInputChannel(-lower_part);
                    request.max_id = max_positive_id;
                    req = request;
                    messageId |= ((long) (-lower_part)) << 32;
                } else {
                    request = new TL_messages_readHistory();
                    request.peer = inputPeer;
                    request.max_id = max_positive_id;
                    req = request;
                }
                Integer value = (Integer) this.dialogs_read_inbox_max.get(Long.valueOf(dialog_id));
                if (value == null) {
                    value = Integer.valueOf(0);
                }
                this.dialogs_read_inbox_max.put(Long.valueOf(dialog_id), Integer.valueOf(Math.max(value.intValue(), max_positive_id)));
                MessagesStorage.getInstance().processPendingRead(dialog_id, messageId, max_date, false);
                final long j = dialog_id;
                final boolean z = popup;
                final int i = max_positive_id;
                MessagesStorage.getInstance().getStorageQueue().postRunnable(new Runnable() {

                    class C04531 implements Runnable {
                        C04531() {
                        }

                        public void run() {
                            Dialog dialog = (Dialog) MessagesController.this.dialogs_dict.get(Long.valueOf(j));
                            if (dialog != null) {
                                dialog.unread_count = 0;
                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.updateInterfaces, Integer.valueOf(256));
                            }
                            if (z) {
                                NotificationsController.getInstance().processReadMessages(null, j, 0, i, true);
                                HashMap<Long, Integer> dialogsToUpdate = new HashMap();
                                dialogsToUpdate.put(Long.valueOf(j), Integer.valueOf(-1));
                                NotificationsController.getInstance().processDialogsUpdateRead(dialogsToUpdate);
                                return;
                            }
                            NotificationsController.getInstance().processReadMessages(null, j, 0, i, false);
                            dialogsToUpdate = new HashMap();
                            dialogsToUpdate.put(Long.valueOf(j), Integer.valueOf(0));
                            NotificationsController.getInstance().processDialogsUpdateRead(dialogsToUpdate);
                        }
                    }

                    public void run() {
                        AndroidUtilities.runOnUIThread(new C04531());
                    }
                });
                if (max_positive_id != Integer.MAX_VALUE) {
                    final long messageIdFinal = messageId;
                    j = dialog_id;
                    final int i2 = max_date;
                    ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
                        public void run(TLObject response, TL_error error) {
                            if (error == null) {
                                MessagesStorage.getInstance().processPendingRead(j, messageIdFinal, i2, true);
                                if (response instanceof TL_messages_affectedMessages) {
                                    TL_messages_affectedMessages res = (TL_messages_affectedMessages) response;
                                    MessagesController.this.processNewDifferenceParams(-1, res.pts, -1, res.pts_count);
                                }
                            }
                        }
                    });
                }
            }
        } else if (max_date != 0) {
            EncryptedChat chat = getEncryptedChat(Integer.valueOf(high_id));
            if (chat.auth_key != null && chat.auth_key.length > 1 && (chat instanceof TL_encryptedChat)) {
                req = new TL_messages_readEncryptedHistory();
                req.peer = new TL_inputEncryptedChat();
                req.peer.chat_id = chat.id;
                req.peer.access_hash = chat.access_hash;
                req.max_date = max_date;
                ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
                    public void run(TLObject response, TL_error error) {
                    }
                });
            }
            MessagesStorage.getInstance().processPendingRead(dialog_id, (long) max_id, max_date, false);
            final long j2 = dialog_id;
            final int i3 = max_date;
            final boolean z2 = popup;
            MessagesStorage.getInstance().getStorageQueue().postRunnable(new Runnable() {

                class C04541 implements Runnable {
                    C04541() {
                    }

                    public void run() {
                        NotificationsController.getInstance().processReadMessages(null, j2, i3, 0, z2);
                        Dialog dialog = (Dialog) MessagesController.this.dialogs_dict.get(Long.valueOf(j2));
                        if (dialog != null) {
                            dialog.unread_count = 0;
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.updateInterfaces, Integer.valueOf(256));
                        }
                        HashMap<Long, Integer> dialogsToUpdate = new HashMap();
                        dialogsToUpdate.put(Long.valueOf(j2), Integer.valueOf(0));
                        NotificationsController.getInstance().processDialogsUpdateRead(dialogsToUpdate);
                    }
                }

                public void run() {
                    AndroidUtilities.runOnUIThread(new C04541());
                }
            });
            if (chat.ttl > 0 && was) {
                int serverTime = Math.max(ConnectionsManager.getInstance().getCurrentTime(), max_date);
                MessagesStorage.getInstance().createTaskForSecretChat(chat.id, serverTime, serverTime, 0, null);
            }
        }
    }

    public int createChat(String title, ArrayList<Integer> selectedContacts, String about, int type) {
        Iterator i$;
        if (type == 1) {
            TL_chat chat = new TL_chat();
            chat.id = UserConfig.lastBroadcastId;
            chat.title = title;
            chat.photo = new TL_chatPhotoEmpty();
            chat.participants_count = selectedContacts.size();
            chat.date = (int) (System.currentTimeMillis() / 1000);
            chat.version = 1;
            UserConfig.lastBroadcastId--;
            putChat(chat, false);
            ArrayList<Chat> chatsArrays = new ArrayList();
            chatsArrays.add(chat);
            MessagesStorage.getInstance().putUsersAndChats(null, chatsArrays, true, true);
            TL_chatFull chatFull = new TL_chatFull();
            chatFull.id = chat.id;
            chatFull.chat_photo = new TL_photoEmpty();
            chatFull.notify_settings = new TL_peerNotifySettingsEmpty();
            chatFull.exported_invite = new TL_chatInviteEmpty();
            chatFull.participants = new TL_chatParticipants();
            chatFull.participants.chat_id = chat.id;
            chatFull.participants.admin_id = UserConfig.getClientUserId();
            chatFull.participants.version = 1;
            i$ = selectedContacts.iterator();
            while (i$.hasNext()) {
                Integer id = (Integer) i$.next();
                TL_chatParticipant participant = new TL_chatParticipant();
                participant.user_id = id.intValue();
                participant.inviter_id = UserConfig.getClientUserId();
                participant.date = (int) (System.currentTimeMillis() / 1000);
                chatFull.participants.participants.add(participant);
            }
            MessagesStorage.getInstance().updateChatInfo(chatFull, false);
            TL_messageService newMsg = new TL_messageService();
            newMsg.action = new TL_messageActionCreatedBroadcastList();
            int newMessageId = UserConfig.getNewMessageId();
            newMsg.id = newMessageId;
            newMsg.local_id = newMessageId;
            newMsg.from_id = UserConfig.getClientUserId();
            newMsg.dialog_id = AndroidUtilities.makeBroadcastId(chat.id);
            newMsg.to_id = new TL_peerChat();
            newMsg.to_id.chat_id = chat.id;
            newMsg.date = ConnectionsManager.getInstance().getCurrentTime();
            newMsg.random_id = 0;
            newMsg.flags |= 256;
            UserConfig.saveConfig(false);
            MessageObject newMsgObj = new MessageObject(newMsg, this.users, true);
            newMsgObj.messageOwner.send_state = 0;
            ArrayList<MessageObject> objArr = new ArrayList();
            objArr.add(newMsgObj);
            ArrayList<Message> arr = new ArrayList();
            arr.add(newMsg);
            MessagesStorage.getInstance().putMessages(arr, false, true, false, 0);
            updateInterfaceWithMessages(newMsg.dialog_id, objArr);
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.chatDidCreated, Integer.valueOf(chat.id));
            return 0;
        } else if (type == 0) {
            req = new TL_messages_createChat();
            req.title = title;
            i$ = selectedContacts.iterator();
            while (i$.hasNext()) {
                User user = getUser((Integer) i$.next());
                if (user != null) {
                    req.users.add(getInputUser(user));
                }
            }
            return ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {

                class C04551 implements Runnable {
                    C04551() {
                    }

                    public void run() {
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.chatDidFailCreate, new Object[0]);
                    }
                }

                public void run(TLObject response, TL_error error) {
                    if (error != null) {
                        AndroidUtilities.runOnUIThread(new C04551());
                        return;
                    }
                    final Updates updates = (Updates) response;
                    MessagesController.this.processUpdates(updates, false);
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        public void run() {
                            MessagesController.this.putUsers(updates.users, false);
                            MessagesController.this.putChats(updates.chats, false);
                            if (updates.chats == null || updates.chats.isEmpty()) {
                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.chatDidFailCreate, new Object[0]);
                                return;
                            }
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.chatDidCreated, Integer.valueOf(((Chat) updates.chats.get(0)).id));
                        }
                    });
                }
            });
        } else if (type != 2 && type != 4) {
            return 0;
        } else {
            req = new TL_channels_createChannel();
            req.title = title;
            req.about = about;
            if (type == 4) {
                req.megagroup = true;
            } else {
                req.broadcast = true;
            }
            return ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {

                class C04601 implements Runnable {
                    C04601() {
                    }

                    public void run() {
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.chatDidFailCreate, new Object[0]);
                    }
                }

                public void run(TLObject response, TL_error error) {
                    if (error != null) {
                        AndroidUtilities.runOnUIThread(new C04601());
                        return;
                    }
                    final Updates updates = (Updates) response;
                    MessagesController.this.processUpdates(updates, false);
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        public void run() {
                            MessagesController.this.putUsers(updates.users, false);
                            MessagesController.this.putChats(updates.chats, false);
                            if (updates.chats == null || updates.chats.isEmpty()) {
                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.chatDidFailCreate, new Object[0]);
                                return;
                            }
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.chatDidCreated, Integer.valueOf(((Chat) updates.chats.get(0)).id));
                        }
                    });
                }
            });
        }
    }

    public void convertToMegaGroup(final Context context, int chat_id) {
        TL_messages_migrateChat req = new TL_messages_migrateChat();
        req.chat_id = chat_id;
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(LocaleController.getString("Loading", C0553R.string.Loading));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        final int reqId = ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {

            class C04621 implements Runnable {
                C04621() {
                }

                public void run() {
                    if (!((Activity) context).isFinishing()) {
                        try {
                            progressDialog.dismiss();
                        } catch (Throwable e) {
                            FileLog.m611e("tmessages", e);
                        }
                    }
                }
            }

            class C04632 implements Runnable {
                C04632() {
                }

                public void run() {
                    if (!((Activity) context).isFinishing()) {
                        try {
                            progressDialog.dismiss();
                        } catch (Throwable e) {
                            FileLog.m611e("tmessages", e);
                        }
                        Builder builder = new Builder(context);
                        builder.setTitle(LocaleController.getString("AppName", C0553R.string.AppName));
                        builder.setMessage(LocaleController.getString("ErrorOccurred", C0553R.string.ErrorOccurred));
                        builder.setPositiveButton(LocaleController.getString("OK", C0553R.string.OK), null);
                        builder.show().setCanceledOnTouchOutside(true);
                    }
                }
            }

            public void run(TLObject response, TL_error error) {
                if (error == null) {
                    AndroidUtilities.runOnUIThread(new C04621());
                    Updates updates = (Updates) response;
                    MessagesController.this.processUpdates((Updates) response, false);
                    return;
                }
                AndroidUtilities.runOnUIThread(new C04632());
            }
        });
        progressDialog.setButton(-2, LocaleController.getString("Cancel", C0553R.string.Cancel), new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ConnectionsManager.getInstance().cancelRequest(reqId, true);
                try {
                    dialog.dismiss();
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
        });
        try {
            progressDialog.show();
        } catch (Exception e) {
        }
    }

    public void addUsersToChannel(int chat_id, ArrayList<InputUser> users, final BaseFragment fragment) {
        if (users != null && !users.isEmpty()) {
            TL_channels_inviteToChannel req = new TL_channels_inviteToChannel();
            req.channel = getInputChannel(chat_id);
            req.users = users;
            ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
                public void run(TLObject response, final TL_error error) {
                    if (error != null) {
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            public void run() {
                                if (fragment != null) {
                                    AlertsCreator.showAddUserAlert(error.text, fragment, true);
                                } else if (error.text.equals("PEER_FLOOD")) {
                                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.needShowAlert, Integer.valueOf(1));
                                }
                            }
                        });
                    } else {
                        MessagesController.this.processUpdates((Updates) response, false);
                    }
                }
            });
        }
    }

    public void toogleChannelComments(int chat_id, boolean enabled) {
        TL_channels_toggleComments req = new TL_channels_toggleComments();
        req.channel = getInputChannel(chat_id);
        req.enabled = enabled;
        ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {

            class C04651 implements Runnable {
                C04651() {
                }

                public void run() {
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.updateInterfaces, Integer.valueOf(8192));
                }
            }

            public void run(TLObject response, TL_error error) {
                if (response != null) {
                    MessagesController.this.processUpdates((Updates) response, false);
                    AndroidUtilities.runOnUIThread(new C04651());
                }
            }
        }, 64);
    }

    public void updateChannelAbout(int chat_id, final String about, final ChatFull info) {
        if (info != null) {
            TL_channels_editAbout req = new TL_channels_editAbout();
            req.channel = getInputChannel(chat_id);
            req.about = about;
            ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {

                class C04661 implements Runnable {
                    C04661() {
                    }

                    public void run() {
                        info.about = about;
                        MessagesStorage.getInstance().updateChatInfo(info, false);
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.chatInfoDidLoaded, info, Integer.valueOf(0), Boolean.valueOf(false));
                    }
                }

                public void run(TLObject response, TL_error error) {
                    if (response instanceof TL_boolTrue) {
                        AndroidUtilities.runOnUIThread(new C04661());
                    }
                }
            }, 64);
        }
    }

    public void updateChannelUserName(final int chat_id, final String userName) {
        TL_channels_updateUsername req = new TL_channels_updateUsername();
        req.channel = getInputChannel(chat_id);
        req.username = userName;
        ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {

            class C04671 implements Runnable {
                C04671() {
                }

                public void run() {
                    Chat chat = MessagesController.this.getChat(Integer.valueOf(chat_id));
                    if (userName.length() != 0) {
                        chat.flags |= 64;
                    } else {
                        chat.flags &= -65;
                    }
                    chat.username = userName;
                    ArrayList<Chat> arrayList = new ArrayList();
                    arrayList.add(chat);
                    MessagesStorage.getInstance().putUsersAndChats(null, arrayList, true, true);
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.updateInterfaces, Integer.valueOf(8192));
                }
            }

            public void run(TLObject response, TL_error error) {
                if (response instanceof TL_boolTrue) {
                    AndroidUtilities.runOnUIThread(new C04671());
                }
            }
        }, 64);
    }

    public void sendBotStart(User user, String botHash) {
        if (user != null) {
            TL_messages_startBot req = new TL_messages_startBot();
            req.bot = getInputUser(user);
            req.peer = getInputPeer(user.id);
            req.start_param = botHash;
            req.random_id = Utilities.random.nextLong();
            ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
                public void run(TLObject response, TL_error error) {
                    if (error == null) {
                        MessagesController.this.processUpdates((Updates) response, false);
                    }
                }
            });
        }
    }

    public void toggleAdminMode(final int chat_id, boolean enabled) {
        TL_messages_toggleChatAdmins req = new TL_messages_toggleChatAdmins();
        req.chat_id = chat_id;
        req.enabled = enabled;
        ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
            public void run(TLObject response, TL_error error) {
                if (error == null) {
                    MessagesController.this.processUpdates((Updates) response, false);
                    MessagesController.this.loadFullChat(chat_id, 0, true);
                }
            }
        });
    }

    public void toggleUserAdmin(int chat_id, int user_id, boolean admin) {
        TL_messages_editChatAdmin req = new TL_messages_editChatAdmin();
        req.chat_id = chat_id;
        req.user_id = getInputUser(user_id);
        req.is_admin = admin;
        ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
            public void run(TLObject response, TL_error error) {
            }
        });
    }

    public void addUserToChat(int chat_id, User user, ChatFull info, int count_fwd, String botHash, BaseFragment fragment) {
        if (user != null) {
            if (chat_id > 0) {
                TLObject request;
                final boolean isChannel = ChatObject.isChannel(chat_id);
                final boolean isMegagroup = isChannel && getInstance().getChat(Integer.valueOf(chat_id)).megagroup;
                final InputUser inputUser = getInputUser(user);
                TLObject req;
                if (botHash != null && (!isChannel || isMegagroup)) {
                    req = new TL_messages_startBot();
                    req.bot = inputUser;
                    if (isChannel) {
                        req.peer = getInputPeer(-chat_id);
                    } else {
                        req.peer = new TL_inputPeerChat();
                        req.peer.chat_id = chat_id;
                    }
                    req.start_param = botHash;
                    req.random_id = Utilities.random.nextLong();
                    request = req;
                } else if (!isChannel) {
                    req = new TL_messages_addChatUser();
                    req.chat_id = chat_id;
                    req.fwd_limit = count_fwd;
                    req.user_id = inputUser;
                    request = req;
                } else if (inputUser instanceof TL_inputUserSelf) {
                    if (!this.joiningToChannels.contains(Integer.valueOf(chat_id))) {
                        req = new TL_channels_joinChannel();
                        req.channel = getInputChannel(chat_id);
                        request = req;
                        this.joiningToChannels.add(Integer.valueOf(chat_id));
                    } else {
                        return;
                    }
                } else if (!user.bot || isMegagroup) {
                    req = new TL_channels_inviteToChannel();
                    req.channel = getInputChannel(chat_id);
                    req.users.add(inputUser);
                    request = req;
                } else {
                    req = new TL_channels_editAdmin();
                    req.channel = getInputChannel(chat_id);
                    req.user_id = getInputUser(user);
                    req.role = new TL_channelRoleEditor();
                    request = req;
                }
                final int i = chat_id;
                final BaseFragment baseFragment = fragment;
                ConnectionsManager.getInstance().sendRequest(request, new RequestDelegate() {

                    class C04701 implements Runnable {
                        C04701() {
                        }

                        public void run() {
                            MessagesController.this.joiningToChannels.remove(Integer.valueOf(i));
                        }
                    }

                    class C04723 implements Runnable {
                        C04723() {
                        }

                        public void run() {
                            MessagesController.this.loadFullChat(i, 0, true);
                        }
                    }

                    public void run(TLObject response, final TL_error error) {
                        if (isChannel && (inputUser instanceof TL_inputUserSelf)) {
                            AndroidUtilities.runOnUIThread(new C04701());
                        }
                        if (error != null) {
                            AndroidUtilities.runOnUIThread(new Runnable() {
                                public void run() {
                                    boolean z = true;
                                    if (baseFragment != null) {
                                        String str = error.text;
                                        BaseFragment baseFragment = baseFragment;
                                        if (!isChannel || isMegagroup) {
                                            z = false;
                                        }
                                        AlertsCreator.showAddUserAlert(str, baseFragment, z);
                                    } else if (error.text.equals("PEER_FLOOD")) {
                                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.needShowAlert, Integer.valueOf(1));
                                    }
                                }
                            });
                            return;
                        }
                        MessagesController.this.processUpdates((Updates) response, false);
                        if (isChannel) {
                            if (inputUser instanceof TL_inputUserSelf) {
                                MessagesController.this.generateJoinMessage(i, true);
                            }
                            AndroidUtilities.runOnUIThread(new C04723(), 1000);
                        }
                        if (isChannel && (inputUser instanceof TL_inputUserSelf)) {
                            MessagesStorage.getInstance().updateDialogsWithDeletedMessages(new ArrayList(), true, i);
                        }
                    }
                });
            } else if (info instanceof TL_chatFull) {
                Iterator i$ = info.participants.participants.iterator();
                while (i$.hasNext()) {
                    if (((ChatParticipant) i$.next()).user_id == user.id) {
                        return;
                    }
                }
                Chat chat = getChat(Integer.valueOf(chat_id));
                chat.participants_count++;
                ArrayList<Chat> chatArrayList = new ArrayList();
                chatArrayList.add(chat);
                MessagesStorage.getInstance().putUsersAndChats(null, chatArrayList, true, true);
                TL_chatParticipant newPart = new TL_chatParticipant();
                newPart.user_id = user.id;
                newPart.inviter_id = UserConfig.getClientUserId();
                newPart.date = ConnectionsManager.getInstance().getCurrentTime();
                info.participants.participants.add(0, newPart);
                MessagesStorage.getInstance().updateChatInfo(info, true);
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.chatInfoDidLoaded, info, Integer.valueOf(0), Boolean.valueOf(false));
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.updateInterfaces, Integer.valueOf(32));
            }
        }
    }

    public void deleteUserFromChat(int chat_id, User user, ChatFull info) {
        if (user != null) {
            Chat chat;
            if (chat_id > 0) {
                TLObject request;
                final InputUser inputUser = getInputUser(user);
                chat = getChat(Integer.valueOf(chat_id));
                final boolean isChannel = ChatObject.isChannel(chat);
                TLObject req;
                if (!isChannel) {
                    req = new TL_messages_deleteChatUser();
                    req.chat_id = chat_id;
                    req.user_id = getInputUser(user);
                    request = req;
                } else if (!(inputUser instanceof TL_inputUserSelf)) {
                    req = new TL_channels_kickFromChannel();
                    req.channel = getInputChannel(chat);
                    req.user_id = inputUser;
                    req.kicked = true;
                    request = req;
                } else if (chat.creator) {
                    req = new TL_channels_deleteChannel();
                    req.channel = getInputChannel(chat);
                    request = req;
                } else {
                    req = new TL_channels_leaveChannel();
                    req.channel = getInputChannel(chat);
                    request = req;
                }
                final User user2 = user;
                final int i = chat_id;
                ConnectionsManager.getInstance().sendRequest(request, new RequestDelegate() {

                    class C04731 implements Runnable {
                        C04731() {
                        }

                        public void run() {
                            MessagesController.getInstance().deleteDialog((long) (-i), 0);
                        }
                    }

                    class C04742 implements Runnable {
                        C04742() {
                        }

                        public void run() {
                            MessagesController.this.loadFullChat(i, 0, true);
                        }
                    }

                    public void run(TLObject response, TL_error error) {
                        if (user2.id == UserConfig.getClientUserId()) {
                            AndroidUtilities.runOnUIThread(new C04731());
                        }
                        if (error == null) {
                            MessagesController.this.processUpdates((Updates) response, false);
                            if (isChannel && !(inputUser instanceof TL_inputUserSelf)) {
                                AndroidUtilities.runOnUIThread(new C04742(), 1000);
                            }
                        }
                    }
                }, 64);
            } else if (info instanceof TL_chatFull) {
                chat = getChat(Integer.valueOf(chat_id));
                chat.participants_count--;
                ArrayList<Chat> chatArrayList = new ArrayList();
                chatArrayList.add(chat);
                MessagesStorage.getInstance().putUsersAndChats(null, chatArrayList, true, true);
                boolean changed = false;
                for (int a = 0; a < info.participants.participants.size(); a++) {
                    if (((ChatParticipant) info.participants.participants.get(a)).user_id == user.id) {
                        info.participants.participants.remove(a);
                        changed = true;
                        break;
                    }
                }
                if (changed) {
                    MessagesStorage.getInstance().updateChatInfo(info, true);
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.chatInfoDidLoaded, info, Integer.valueOf(0), Boolean.valueOf(false));
                }
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.updateInterfaces, Integer.valueOf(32));
            }
        }
    }

    public void changeChatTitle(int chat_id, String title) {
        if (chat_id > 0) {
            TLObject request;
            TLObject req;
            if (ChatObject.isChannel(chat_id)) {
                req = new TL_channels_editTitle();
                req.channel = getInputChannel(chat_id);
                req.title = title;
                request = req;
            } else {
                req = new TL_messages_editChatTitle();
                req.chat_id = chat_id;
                req.title = title;
                request = req;
            }
            ConnectionsManager.getInstance().sendRequest(request, new RequestDelegate() {
                public void run(TLObject response, TL_error error) {
                    if (error == null) {
                        MessagesController.this.processUpdates((Updates) response, false);
                    }
                }
            }, 64);
            return;
        }
        Chat chat = getChat(Integer.valueOf(chat_id));
        chat.title = title;
        ArrayList<Chat> chatArrayList = new ArrayList();
        chatArrayList.add(chat);
        MessagesStorage.getInstance().putUsersAndChats(null, chatArrayList, true, true);
        NotificationCenter.getInstance().postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
        NotificationCenter.getInstance().postNotificationName(NotificationCenter.updateInterfaces, Integer.valueOf(16));
    }

    public void changeChatAvatar(int chat_id, InputFile uploadedAvatar) {
        TLObject request;
        TLObject req;
        if (ChatObject.isChannel(chat_id)) {
            req = new TL_channels_editPhoto();
            req.channel = getInputChannel(chat_id);
            if (uploadedAvatar != null) {
                req.photo = new TL_inputChatUploadedPhoto();
                req.photo.file = uploadedAvatar;
                req.photo.crop = new TL_inputPhotoCropAuto();
            } else {
                req.photo = new TL_inputChatPhotoEmpty();
            }
            request = req;
        } else {
            req = new TL_messages_editChatPhoto();
            req.chat_id = chat_id;
            if (uploadedAvatar != null) {
                req.photo = new TL_inputChatUploadedPhoto();
                req.photo.file = uploadedAvatar;
                req.photo.crop = new TL_inputPhotoCropAuto();
            } else {
                req.photo = new TL_inputChatPhotoEmpty();
            }
            request = req;
        }
        ConnectionsManager.getInstance().sendRequest(request, new RequestDelegate() {
            public void run(TLObject response, TL_error error) {
                if (error == null) {
                    MessagesController.this.processUpdates((Updates) response, false);
                }
            }
        }, 64);
    }

    public void unregistedPush() {
        if (UserConfig.registeredForPush && UserConfig.pushString.length() == 0) {
            TL_account_unregisterDevice req = new TL_account_unregisterDevice();
            req.token = UserConfig.pushString;
            req.token_type = 2;
            ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
                public void run(TLObject response, TL_error error) {
                }
            });
        }
    }

    public void performLogout(boolean byUser) {
        ApplicationLoader.applicationContext.getSharedPreferences("Notifications", 0).edit().clear().commit();
        if (byUser) {
            unregistedPush();
            ConnectionsManager.getInstance().sendRequest(new TL_auth_logOut(), new RequestDelegate() {
                public void run(TLObject response, TL_error error) {
                    ConnectionsManager.getInstance().cleanUp();
                }
            });
        } else {
            ConnectionsManager.getInstance().cleanUp();
        }
        UserConfig.clearConfig();
        NotificationCenter.getInstance().postNotificationName(NotificationCenter.appDidLogout, new Object[0]);
        MessagesStorage.getInstance().cleanUp(false);
        cleanUp();
        ContactsController.getInstance().deleteAllAppAccounts();
    }

    public void generateUpdateMessage() {
        Utilities.stageQueue.postRunnable(new Runnable() {
            public void run() {
                try {
                    String build = LocaleController.getString("updateBuild", C0553R.string.updateBuild);
                    if (build != null) {
                        int version = Utilities.parseInt(build).intValue();
                        if (version == 0) {
                            version = 524;
                        }
                        if (version > UserConfig.lastUpdateVersion) {
                            UserConfig.lastUpdateVersion = version;
                            UserConfig.saveConfig(false);
                        } else {
                            return;
                        }
                    }
                    TL_updateServiceNotification update = new TL_updateServiceNotification();
                    update.message = LocaleController.getString("updateText", C0553R.string.updateText);
                    update.media = new TL_messageMediaEmpty();
                    update.type = "update";
                    update.popup = false;
                    ArrayList<Update> updates = new ArrayList();
                    updates.add(update);
                    MessagesController.this.processUpdateArray(updates, null, null);
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
        });
    }

    public void registerForPush(final String regid) {
        if (regid != null && regid.length() != 0 && !this.registeringForPush && UserConfig.getClientUserId() != 0) {
            if (!UserConfig.registeredForPush || !regid.equals(UserConfig.pushString)) {
                this.registeringForPush = true;
                TL_account_registerDevice req = new TL_account_registerDevice();
                req.token_type = 2;
                req.token = regid;
                req.app_sandbox = false;
                try {
                    req.lang_code = LocaleController.getLocaleString(LocaleController.getInstance().getSystemDefaultLocale());
                    if (req.lang_code.length() == 0) {
                        req.lang_code = "en";
                    }
                    req.device_model = Build.MANUFACTURER + Build.MODEL;
                    req.system_version = "SDK " + VERSION.SDK_INT;
                    PackageInfo pInfo = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
                    req.app_version = pInfo.versionName + " (" + pInfo.versionCode + ")";
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                    req.lang_code = "en";
                    req.device_model = "Android unknown";
                    req.system_version = "SDK " + VERSION.SDK_INT;
                    req.app_version = "App version unknown";
                }
                if (req.lang_code == null || req.lang_code.trim().length() == 0) {
                    req.lang_code = "en";
                }
                if (req.device_model == null || req.device_model.trim().length() == 0) {
                    req.device_model = "Android unknown";
                }
                if (req.app_version == null || req.app_version.trim().length() == 0) {
                    req.app_version = "App version unknown";
                }
                if (req.system_version == null || req.system_version.trim().length() == 0) {
                    req.system_version = "SDK Unknown";
                }
                if (req.app_version != null) {
                    ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {

                        class C04751 implements Runnable {
                            C04751() {
                            }

                            public void run() {
                                MessagesController.this.registeringForPush = false;
                            }
                        }

                        public void run(TLObject response, TL_error error) {
                            if (error == null) {
                                FileLog.m609e("tmessages", "registered for push");
                                UserConfig.registeredForPush = true;
                                UserConfig.pushString = regid;
                                UserConfig.saveConfig(false);
                            }
                            AndroidUtilities.runOnUIThread(new C04751());
                        }
                    });
                }
            }
        }
    }

    public void loadCurrentState() {
        if (!this.updatingState) {
            this.updatingState = true;
            ConnectionsManager.getInstance().sendRequest(new TL_updates_getState(), new RequestDelegate() {
                public void run(TLObject response, TL_error error) {
                    MessagesController.this.updatingState = false;
                    if (error == null) {
                        TL_updates_state res = (TL_updates_state) response;
                        MessagesStorage.lastDateValue = res.date;
                        MessagesStorage.lastPtsValue = res.pts;
                        MessagesStorage.lastSeqValue = res.seq;
                        MessagesStorage.lastQtsValue = res.qts;
                        for (int a = 0; a < 3; a++) {
                            MessagesController.this.processUpdatesQueue(a, 2);
                        }
                        MessagesStorage.getInstance().saveDiffParams(MessagesStorage.lastSeqValue, MessagesStorage.lastPtsValue, MessagesStorage.lastDateValue, MessagesStorage.lastQtsValue);
                    } else if (error.code != 401) {
                        MessagesController.this.loadCurrentState();
                    }
                }
            });
        }
    }

    private int getUpdateSeq(Updates updates) {
        if (updates instanceof TL_updatesCombined) {
            return updates.seq_start;
        }
        return updates.seq;
    }

    private void setUpdatesStartTime(int type, long time) {
        if (type == 0) {
            this.updatesStartWaitTimeSeq = time;
        } else if (type == 1) {
            this.updatesStartWaitTimePts = time;
        } else if (type == 2) {
            this.updatesStartWaitTimeQts = time;
        }
    }

    public long getUpdatesStartTime(int type) {
        if (type == 0) {
            return this.updatesStartWaitTimeSeq;
        }
        if (type == 1) {
            return this.updatesStartWaitTimePts;
        }
        if (type == 2) {
            return this.updatesStartWaitTimeQts;
        }
        return 0;
    }

    private int isValidUpdate(Updates updates, int type) {
        if (type == 0) {
            int seq = getUpdateSeq(updates);
            if (MessagesStorage.lastSeqValue + 1 == seq || MessagesStorage.lastSeqValue == seq) {
                return 0;
            }
            if (MessagesStorage.lastSeqValue >= seq) {
                return 2;
            }
            return 1;
        } else if (type == 1) {
            if (updates.pts <= MessagesStorage.lastPtsValue) {
                return 2;
            }
            if (MessagesStorage.lastPtsValue + updates.pts_count == updates.pts) {
                return 0;
            }
            return 1;
        } else if (type != 2) {
            return 0;
        } else {
            if (updates.pts <= MessagesStorage.lastQtsValue) {
                return 2;
            }
            if (MessagesStorage.lastQtsValue + updates.updates.size() == updates.pts) {
                return 0;
            }
            return 1;
        }
    }

    private void processChannelsUpdatesQueue(int channelId, int state) {
        ArrayList<Updates> updatesQueue = (ArrayList) this.updatesQueueChannels.get(Integer.valueOf(channelId));
        if (updatesQueue != null) {
            Integer channelPts = (Integer) this.channelsPts.get(Integer.valueOf(channelId));
            if (updatesQueue.isEmpty() || channelPts == null) {
                this.updatesQueueChannels.remove(Integer.valueOf(channelId));
                return;
            }
            Collections.sort(updatesQueue, new Comparator<Updates>() {
                public int compare(Updates updates, Updates updates2) {
                    return AndroidUtilities.compare(updates.pts, updates2.pts);
                }
            });
            boolean anyProceed = false;
            if (state == 2) {
                this.channelsPts.put(Integer.valueOf(channelId), Integer.valueOf(((Updates) updatesQueue.get(0)).pts));
            }
            int a = 0;
            while (updatesQueue.size() > 0) {
                int updateState;
                Updates updates = (Updates) updatesQueue.get(a);
                if (updates.pts <= channelPts.intValue()) {
                    updateState = 2;
                } else if (channelPts.intValue() + updates.pts_count == updates.pts) {
                    updateState = 0;
                } else {
                    updateState = 1;
                }
                if (updateState == 0) {
                    processUpdates(updates, true);
                    anyProceed = true;
                    updatesQueue.remove(a);
                    a--;
                } else if (updateState == 1) {
                    Long updatesStartWaitTime = (Long) this.updatesStartWaitTimeChannels.get(Integer.valueOf(channelId));
                    if (updatesStartWaitTime == null || (!anyProceed && Math.abs(System.currentTimeMillis() - updatesStartWaitTime.longValue()) > 1500)) {
                        FileLog.m609e("tmessages", "HOLE IN CHANNEL " + channelId + " UPDATES QUEUE - getChannelDifference ");
                        this.updatesStartWaitTimeChannels.remove(Integer.valueOf(channelId));
                        this.updatesQueueChannels.remove(Integer.valueOf(channelId));
                        getChannelDifference(channelId);
                        return;
                    }
                    FileLog.m609e("tmessages", "HOLE IN CHANNEL " + channelId + " UPDATES QUEUE - will wait more time");
                    if (anyProceed) {
                        this.updatesStartWaitTimeChannels.put(Integer.valueOf(channelId), Long.valueOf(System.currentTimeMillis()));
                        return;
                    }
                    return;
                } else {
                    updatesQueue.remove(a);
                    a--;
                }
                a++;
            }
            this.updatesQueueChannels.remove(Integer.valueOf(channelId));
            this.updatesStartWaitTimeChannels.remove(Integer.valueOf(channelId));
            FileLog.m609e("tmessages", "UPDATES CHANNEL " + channelId + " QUEUE PROCEED - OK");
        }
    }

    private void processUpdatesQueue(int type, int state) {
        ArrayList<Updates> updatesQueue = null;
        if (type == 0) {
            updatesQueue = this.updatesQueueSeq;
            Collections.sort(updatesQueue, new Comparator<Updates>() {
                public int compare(Updates updates, Updates updates2) {
                    return AndroidUtilities.compare(MessagesController.this.getUpdateSeq(updates), MessagesController.this.getUpdateSeq(updates2));
                }
            });
        } else if (type == 1) {
            updatesQueue = this.updatesQueuePts;
            Collections.sort(updatesQueue, new Comparator<Updates>() {
                public int compare(Updates updates, Updates updates2) {
                    return AndroidUtilities.compare(updates.pts, updates2.pts);
                }
            });
        } else if (type == 2) {
            updatesQueue = this.updatesQueueQts;
            Collections.sort(updatesQueue, new Comparator<Updates>() {
                public int compare(Updates updates, Updates updates2) {
                    return AndroidUtilities.compare(updates.pts, updates2.pts);
                }
            });
        }
        if (!(updatesQueue == null || updatesQueue.isEmpty())) {
            Updates updates;
            boolean anyProceed = false;
            if (state == 2) {
                updates = (Updates) updatesQueue.get(0);
                if (type == 0) {
                    MessagesStorage.lastSeqValue = getUpdateSeq(updates);
                } else if (type == 1) {
                    MessagesStorage.lastPtsValue = updates.pts;
                } else {
                    MessagesStorage.lastQtsValue = updates.pts;
                }
            }
            int a = 0;
            while (updatesQueue.size() > 0) {
                updates = (Updates) updatesQueue.get(a);
                int updateState = isValidUpdate(updates, type);
                if (updateState == 0) {
                    processUpdates(updates, true);
                    anyProceed = true;
                    updatesQueue.remove(a);
                    a--;
                } else if (updateState != 1) {
                    updatesQueue.remove(a);
                    a--;
                } else if (getUpdatesStartTime(type) == 0 || (!anyProceed && Math.abs(System.currentTimeMillis() - getUpdatesStartTime(type)) > 1500)) {
                    FileLog.m609e("tmessages", "HOLE IN UPDATES QUEUE - getDifference");
                    setUpdatesStartTime(type, 0);
                    updatesQueue.clear();
                    getDifference();
                    return;
                } else {
                    FileLog.m609e("tmessages", "HOLE IN UPDATES QUEUE - will wait more time");
                    if (anyProceed) {
                        setUpdatesStartTime(type, System.currentTimeMillis());
                        return;
                    }
                    return;
                }
                a++;
            }
            updatesQueue.clear();
            FileLog.m609e("tmessages", "UPDATES QUEUE PROCEED - OK");
        }
        setUpdatesStartTime(type, 0);
    }

    public void startShortPoll(final int channelId, final boolean stop) {
        Utilities.stageQueue.postRunnable(new Runnable() {
            public void run() {
                if (stop) {
                    MessagesController.this.needShortPollChannels.delete(channelId);
                    return;
                }
                MessagesController.this.needShortPollChannels.put(channelId, 0);
                if (MessagesController.this.shortPollChannels.indexOfKey(channelId) < 0) {
                    MessagesController.this.getChannelDifference(channelId, 2);
                }
            }
        });
    }

    public void getChannelDifference(int channelId) {
        getChannelDifference(channelId, 0);
    }

    public void getChannelDifference(final int channelId, final int newDialogType) {
        Boolean gettingDifferenceChannel = (Boolean) this.gettingDifferenceChannels.get(Integer.valueOf(channelId));
        if (gettingDifferenceChannel == null) {
            gettingDifferenceChannel = Boolean.valueOf(false);
        }
        if (!gettingDifferenceChannel.booleanValue()) {
            Integer channelPts;
            int limit = 100;
            if (newDialogType != 1) {
                channelPts = (Integer) this.channelsPts.get(Integer.valueOf(channelId));
                if (channelPts == null) {
                    channelPts = Integer.valueOf(MessagesStorage.getInstance().getChannelPtsSync(channelId));
                    if (channelPts.intValue() != 0) {
                        this.channelsPts.put(Integer.valueOf(channelId), channelPts);
                    }
                    if (channelPts.intValue() == 0 && newDialogType == 2) {
                        channelPts = Integer.valueOf(1);
                        limit = 1;
                    }
                }
                if (channelPts.intValue() == 0) {
                    return;
                }
            } else if (((Integer) this.channelsPts.get(Integer.valueOf(channelId))) == null) {
                channelPts = Integer.valueOf(1);
                limit = 1;
            } else {
                return;
            }
            this.gettingDifferenceChannels.put(Integer.valueOf(channelId), Boolean.valueOf(true));
            TL_updates_getChannelDifference req = new TL_updates_getChannelDifference();
            req.channel = getInputChannel(channelId);
            req.filter = new TL_channelMessagesFilterEmpty();
            req.pts = channelPts.intValue();
            req.limit = limit;
            FileLog.m609e("tmessages", "start getChannelDifference with pts = " + channelPts + " channelId = " + channelId);
            ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {

                class C04843 implements Runnable {
                    C04843() {
                    }

                    public void run() {
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.chatInfoCantLoad, Integer.valueOf(channelId));
                    }
                }

                public void run(TLObject response, TL_error error) {
                    if (error == null) {
                        int a;
                        final updates_ChannelDifference res = (updates_ChannelDifference) response;
                        final HashMap<Integer, User> usersDict = new HashMap();
                        for (a = 0; a < res.users.size(); a++) {
                            User user = (User) res.users.get(a);
                            usersDict.put(Integer.valueOf(user.id), user);
                        }
                        Chat channel = null;
                        for (a = 0; a < res.chats.size(); a++) {
                            Chat chat = (Chat) res.chats.get(a);
                            if (chat.id == channelId) {
                                channel = chat;
                                break;
                            }
                        }
                        final Chat channelFinal = channel;
                        final ArrayList<TL_updateMessageID> msgUpdates = new ArrayList();
                        if (!res.other_updates.isEmpty()) {
                            a = 0;
                            while (a < res.other_updates.size()) {
                                Update upd = (Update) res.other_updates.get(a);
                                if (upd instanceof TL_updateMessageID) {
                                    msgUpdates.add((TL_updateMessageID) upd);
                                    res.other_updates.remove(a);
                                    a--;
                                }
                                a++;
                            }
                        }
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            public void run() {
                                MessagesController.this.putUsers(res.users, false);
                                MessagesController.this.putChats(res.chats, false);
                            }
                        });
                        MessagesStorage.getInstance().getStorageQueue().postRunnable(new Runnable() {

                            class C04822 implements Runnable {
                                C04822() {
                                }

                                public void run() {
                                    int a;
                                    Message message;
                                    if ((res instanceof TL_updates_channelDifference) || (res instanceof TL_updates_channelDifferenceEmpty)) {
                                        if (!res.new_messages.isEmpty()) {
                                            final HashMap<Long, ArrayList<MessageObject>> messages = new HashMap();
                                            ImageLoader.saveMessagesThumbs(res.new_messages);
                                            final ArrayList<MessageObject> pushMessages = new ArrayList();
                                            for (a = 0; a < res.new_messages.size(); a++) {
                                                message = (Message) res.new_messages.get(a);
                                                if (!message.out) {
                                                    message.unread = true;
                                                    if (message.from_id <= 0 || (channelFinal != null && channelFinal.megagroup)) {
                                                        message.media_unread = true;
                                                    }
                                                }
                                                if (channelFinal != null && channelFinal.megagroup) {
                                                    message.flags |= Integer.MIN_VALUE;
                                                }
                                                Integer value = (Integer) MessagesController.this.dialogs_read_inbox_max.get(Long.valueOf((long) (-channelId)));
                                                if (value == null) {
                                                    value = Integer.valueOf(MessagesStorage.getInstance().getChannelReadInboxMax(channelId));
                                                }
                                                MessageObject obj = new MessageObject(message, usersDict, true);
                                                if ((channelFinal != null && channelFinal.left) || value.intValue() >= obj.getId()) {
                                                    obj.setIsRead();
                                                    obj.setContentIsRead();
                                                }
                                                if (!obj.isOut() && obj.isContentUnread()) {
                                                    pushMessages.add(obj);
                                                }
                                                long uid = (long) (-channelId);
                                                ArrayList<MessageObject> arr = (ArrayList) messages.get(Long.valueOf(uid));
                                                if (arr == null) {
                                                    arr = new ArrayList();
                                                    messages.put(Long.valueOf(uid), arr);
                                                }
                                                arr.add(obj);
                                            }
                                            AndroidUtilities.runOnUIThread(new Runnable() {
                                                public void run() {
                                                    for (Entry<Long, ArrayList<MessageObject>> pair : messages.entrySet()) {
                                                        ArrayList<MessageObject> value = (ArrayList) pair.getValue();
                                                        MessagesController.this.updateInterfaceWithMessages(((Long) pair.getKey()).longValue(), value);
                                                    }
                                                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
                                                }
                                            });
                                            MessagesStorage.getInstance().getStorageQueue().postRunnable(new Runnable() {

                                                class C04801 implements Runnable {
                                                    C04801() {
                                                    }

                                                    public void run() {
                                                        NotificationsController.getInstance().processNewMessages(pushMessages, true);
                                                    }
                                                }

                                                public void run() {
                                                    if (!pushMessages.isEmpty()) {
                                                        AndroidUtilities.runOnUIThread(new C04801());
                                                    }
                                                    MessagesStorage.getInstance().startTransaction(false);
                                                    MessagesStorage.getInstance().putMessages(res.new_messages, false, false, false, MediaController.getInstance().getAutodownloadMask());
                                                    MessagesStorage.getInstance().putUsersAndChats(res.users, res.chats, false, false);
                                                    MessagesStorage.getInstance().commitTransaction(false);
                                                }
                                            });
                                        }
                                        if (!res.other_updates.isEmpty()) {
                                            MessagesController.this.processUpdateArray(res.other_updates, res.users, res.chats);
                                        }
                                        MessagesController.this.processChannelsUpdatesQueue(channelId, 1);
                                        MessagesStorage.getInstance().saveChannelPts(channelId, res.pts);
                                    } else if (res instanceof TL_updates_channelDifferenceTooLong) {
                                        for (a = 0; a < res.messages.size(); a++) {
                                            message = (Message) res.messages.get(a);
                                            message.dialog_id = (long) (-channelId);
                                            if (!message.out) {
                                                message.unread = true;
                                                if (message.from_id <= 0 || (channelFinal != null && channelFinal.megagroup)) {
                                                    message.media_unread = true;
                                                }
                                            }
                                            if (channelFinal != null && channelFinal.megagroup) {
                                                message.flags |= Integer.MIN_VALUE;
                                            }
                                            if ((channelFinal != null && channelFinal.left) || res.read_inbox_max_id >= message.id) {
                                                message.unread = false;
                                                message.media_unread = false;
                                            }
                                            if (channelFinal != null && channelFinal.megagroup) {
                                                message.flags |= Integer.MIN_VALUE;
                                            }
                                        }
                                        MessagesStorage.getInstance().putUsersAndChats(res.users, res.chats, true, true);
                                        if (channelFinal.megagroup) {
                                            res.unread_important_count = Math.max(res.unread_count, res.unread_important_count);
                                            res.top_important_message = Math.max(res.top_important_message, res.top_message);
                                        }
                                        MessagesStorage.getInstance().overwriteChannel(channelId, (TL_updates_channelDifferenceTooLong) res, newDialogType);
                                    }
                                    MessagesController.this.gettingDifferenceChannels.remove(Integer.valueOf(channelId));
                                    MessagesController.this.channelsPts.put(Integer.valueOf(channelId), Integer.valueOf(res.pts));
                                    if ((res.flags & 2) != 0) {
                                        MessagesController.this.shortPollChannels.put(channelId, ((int) (System.currentTimeMillis() / 1000)) + res.timeout);
                                    }
                                    if (!res.isFinal) {
                                        MessagesController.this.getChannelDifference(channelId);
                                    }
                                    FileLog.m609e("tmessages", "received channel difference with pts = " + res.pts + " channelId = " + channelId);
                                    FileLog.m609e("tmessages", "messages = " + res.new_messages.size() + " users = " + res.users.size() + " chats = " + res.chats.size() + " other updates = " + res.other_updates.size());
                                }
                            }

                            public void run() {
                                if (!msgUpdates.isEmpty()) {
                                    final HashMap<Integer, long[]> corrected = new HashMap();
                                    Iterator i$ = msgUpdates.iterator();
                                    while (i$.hasNext()) {
                                        TL_updateMessageID update = (TL_updateMessageID) i$.next();
                                        long[] ids = MessagesStorage.getInstance().updateMessageStateAndId(update.random_id, null, update.id, 0, false, channelId);
                                        if (ids != null) {
                                            corrected.put(Integer.valueOf(update.id), ids);
                                        }
                                    }
                                    if (!corrected.isEmpty()) {
                                        AndroidUtilities.runOnUIThread(new Runnable() {
                                            public void run() {
                                                for (Entry<Integer, long[]> entry : corrected.entrySet()) {
                                                    Integer newId = (Integer) entry.getKey();
                                                    SendMessagesHelper.getInstance().processSentMessage(Integer.valueOf((int) ((long[]) entry.getValue())[1]).intValue());
                                                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.messageReceivedByServer, oldId, newId, null, Long.valueOf(ids[0]));
                                                }
                                            }
                                        });
                                    }
                                }
                                Utilities.stageQueue.postRunnable(new C04822());
                            }
                        });
                        return;
                    }
                    if (error.text.equals("CHANNEL_PRIVATE")) {
                        AndroidUtilities.runOnUIThread(new C04843());
                    }
                    MessagesController.this.gettingDifferenceChannels.remove(Integer.valueOf(channelId));
                }
            });
        }
    }

    public void getDifference() {
        getDifference(MessagesStorage.lastPtsValue, MessagesStorage.lastDateValue, MessagesStorage.lastQtsValue, false);
    }

    public void getDifference(int pts, int date, int qts, boolean slice) {
        registerForPush(UserConfig.pushString);
        if (MessagesStorage.lastPtsValue == 0) {
            loadCurrentState();
        } else if (slice || !this.gettingDifference) {
            if (!this.firstGettingTask) {
                getNewDeleteTask(null);
                this.firstGettingTask = true;
            }
            this.gettingDifference = true;
            TL_updates_getDifference req = new TL_updates_getDifference();
            req.pts = pts;
            req.date = date;
            req.qts = qts;
            if (req.date == 0) {
                req.date = ConnectionsManager.getInstance().getCurrentTime();
            }
            FileLog.m609e("tmessages", "start getDifference with date = " + MessagesStorage.lastDateValue + " pts = " + MessagesStorage.lastPtsValue + " seq = " + MessagesStorage.lastSeqValue);
            ConnectionsManager.getInstance().setIsUpdating(true);
            ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
                public void run(TLObject response, TL_error error) {
                    if (error == null) {
                        int a;
                        final updates_Difference res = (updates_Difference) response;
                        if (res instanceof TL_updates_differenceSlice) {
                            MessagesController.this.getDifference(res.intermediate_state.pts, res.intermediate_state.date, res.intermediate_state.qts, true);
                        }
                        final HashMap<Integer, User> usersDict = new HashMap();
                        final HashMap<Integer, Chat> chatsDict = new HashMap();
                        for (a = 0; a < res.users.size(); a++) {
                            User user = (User) res.users.get(a);
                            usersDict.put(Integer.valueOf(user.id), user);
                        }
                        for (a = 0; a < res.chats.size(); a++) {
                            Chat chat = (Chat) res.chats.get(a);
                            chatsDict.put(Integer.valueOf(chat.id), chat);
                        }
                        final ArrayList<TL_updateMessageID> msgUpdates = new ArrayList();
                        if (!res.other_updates.isEmpty()) {
                            a = 0;
                            while (a < res.other_updates.size()) {
                                Update upd = (Update) res.other_updates.get(a);
                                if (upd instanceof TL_updateMessageID) {
                                    msgUpdates.add((TL_updateMessageID) upd);
                                    res.other_updates.remove(a);
                                    a--;
                                }
                                a++;
                            }
                        }
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            public void run() {
                                MessagesController.this.putUsers(res.users, false);
                                MessagesController.this.putChats(res.chats, false);
                            }
                        });
                        MessagesStorage.getInstance().getStorageQueue().postRunnable(new Runnable() {

                            class C04902 implements Runnable {
                                C04902() {
                                }

                                public void run() {
                                    int a;
                                    if (!(res.new_messages.isEmpty() && res.new_encrypted_messages.isEmpty())) {
                                        final HashMap<Long, ArrayList<MessageObject>> messages = new HashMap();
                                        Iterator it = res.new_encrypted_messages.iterator();
                                        while (it.hasNext()) {
                                            ArrayList<Message> decryptedMessages = SecretChatHelper.getInstance().decryptMessage((EncryptedMessage) it.next());
                                            if (!(decryptedMessages == null || decryptedMessages.isEmpty())) {
                                                Iterator i$ = decryptedMessages.iterator();
                                                while (i$.hasNext()) {
                                                    res.new_messages.add((Message) i$.next());
                                                }
                                            }
                                        }
                                        ImageLoader.saveMessagesThumbs(res.new_messages);
                                        final ArrayList<MessageObject> pushMessages = new ArrayList();
                                        for (a = 0; a < res.new_messages.size(); a++) {
                                            long uid;
                                            Message message = (Message) res.new_messages.get(a);
                                            if (message.action instanceof TL_messageActionChatDeleteUser) {
                                                User user = (User) usersDict.get(Integer.valueOf(message.action.user_id));
                                                if (user != null && user.bot) {
                                                    message.reply_markup = new TL_replyKeyboardHide();
                                                }
                                            } else if ((message.action instanceof TL_messageActionChatMigrateTo) || (message.action instanceof TL_messageActionChannelCreate)) {
                                                message.unread = false;
                                                message.media_unread = false;
                                            }
                                            MessageObject obj = new MessageObject(message, usersDict, chatsDict, true);
                                            if (!obj.isOut() && obj.isUnread()) {
                                                pushMessages.add(obj);
                                            }
                                            if (message.dialog_id != 0) {
                                                uid = message.dialog_id;
                                            } else if (message.to_id.chat_id != 0) {
                                                uid = (long) (-message.to_id.chat_id);
                                            } else {
                                                if (message.to_id.user_id == UserConfig.getClientUserId()) {
                                                    message.to_id.user_id = message.from_id;
                                                }
                                                uid = (long) message.to_id.user_id;
                                            }
                                            ArrayList<MessageObject> arr = (ArrayList) messages.get(Long.valueOf(uid));
                                            if (arr == null) {
                                                arr = new ArrayList();
                                                messages.put(Long.valueOf(uid), arr);
                                            }
                                            arr.add(obj);
                                        }
                                        AndroidUtilities.runOnUIThread(new Runnable() {
                                            public void run() {
                                                for (Entry<Long, ArrayList<MessageObject>> pair : messages.entrySet()) {
                                                    ArrayList<MessageObject> value = (ArrayList) pair.getValue();
                                                    MessagesController.this.updateInterfaceWithMessages(((Long) pair.getKey()).longValue(), value);
                                                }
                                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
                                            }
                                        });
                                        MessagesStorage.getInstance().getStorageQueue().postRunnable(new Runnable() {

                                            class C04881 implements Runnable {
                                                C04881() {
                                                }

                                                public void run() {
                                                    NotificationsController.getInstance().processNewMessages(pushMessages, !(res instanceof TL_updates_differenceSlice));
                                                }
                                            }

                                            public void run() {
                                                if (!pushMessages.isEmpty()) {
                                                    AndroidUtilities.runOnUIThread(new C04881());
                                                }
                                                MessagesStorage.getInstance().startTransaction(false);
                                                MessagesStorage.getInstance().putMessages(res.new_messages, false, false, false, MediaController.getInstance().getAutodownloadMask());
                                                MessagesStorage.getInstance().putUsersAndChats(res.users, res.chats, false, false);
                                                MessagesStorage.getInstance().commitTransaction(false);
                                            }
                                        });
                                        SecretChatHelper.getInstance().processPendingEncMessages();
                                    }
                                    if (!res.other_updates.isEmpty()) {
                                        MessagesController.this.processUpdateArray(res.other_updates, res.users, res.chats);
                                    }
                                    if (res instanceof TL_updates_difference) {
                                        MessagesController.this.gettingDifference = false;
                                        MessagesStorage.lastSeqValue = res.state.seq;
                                        MessagesStorage.lastDateValue = res.state.date;
                                        MessagesStorage.lastPtsValue = res.state.pts;
                                        MessagesStorage.lastQtsValue = res.state.qts;
                                        ConnectionsManager.getInstance().setIsUpdating(false);
                                        for (a = 0; a < 3; a++) {
                                            MessagesController.this.processUpdatesQueue(a, 1);
                                        }
                                    } else if (res instanceof TL_updates_differenceSlice) {
                                        MessagesStorage.lastDateValue = res.intermediate_state.date;
                                        MessagesStorage.lastPtsValue = res.intermediate_state.pts;
                                        MessagesStorage.lastQtsValue = res.intermediate_state.qts;
                                    } else if (res instanceof TL_updates_differenceEmpty) {
                                        MessagesController.this.gettingDifference = false;
                                        MessagesStorage.lastSeqValue = res.seq;
                                        MessagesStorage.lastDateValue = res.date;
                                        ConnectionsManager.getInstance().setIsUpdating(false);
                                        for (a = 0; a < 3; a++) {
                                            MessagesController.this.processUpdatesQueue(a, 1);
                                        }
                                    }
                                    MessagesStorage.getInstance().saveDiffParams(MessagesStorage.lastSeqValue, MessagesStorage.lastPtsValue, MessagesStorage.lastDateValue, MessagesStorage.lastQtsValue);
                                    FileLog.m609e("tmessages", "received difference with date = " + MessagesStorage.lastDateValue + " pts = " + MessagesStorage.lastPtsValue + " seq = " + MessagesStorage.lastSeqValue);
                                    FileLog.m609e("tmessages", "messages = " + res.new_messages.size() + " users = " + res.users.size() + " chats = " + res.chats.size() + " other updates = " + res.other_updates.size());
                                }
                            }

                            public void run() {
                                if (!msgUpdates.isEmpty()) {
                                    final HashMap<Integer, long[]> corrected = new HashMap();
                                    Iterator i$ = msgUpdates.iterator();
                                    while (i$.hasNext()) {
                                        TL_updateMessageID update = (TL_updateMessageID) i$.next();
                                        long[] ids = MessagesStorage.getInstance().updateMessageStateAndId(update.random_id, null, update.id, 0, false, 0);
                                        if (ids != null) {
                                            corrected.put(Integer.valueOf(update.id), ids);
                                        }
                                    }
                                    if (!corrected.isEmpty()) {
                                        AndroidUtilities.runOnUIThread(new Runnable() {
                                            public void run() {
                                                for (Entry<Integer, long[]> entry : corrected.entrySet()) {
                                                    Integer newId = (Integer) entry.getKey();
                                                    SendMessagesHelper.getInstance().processSentMessage(Integer.valueOf((int) ((long[]) entry.getValue())[1]).intValue());
                                                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.messageReceivedByServer, oldId, newId, null, Long.valueOf(ids[0]));
                                                }
                                            }
                                        });
                                    }
                                }
                                Utilities.stageQueue.postRunnable(new C04902());
                            }
                        });
                        return;
                    }
                    MessagesController.this.gettingDifference = false;
                    ConnectionsManager.getInstance().setIsUpdating(false);
                }
            });
        }
    }

    public void generateJoinMessage(final int chat_id, boolean ignoreLeft) {
        Chat chat = getChat(Integer.valueOf(chat_id));
        if (chat != null && ChatObject.isChannel(chat_id) && !chat.megagroup) {
            if ((!chat.left && !chat.kicked) || ignoreLeft) {
                TL_messageService message = new TL_messageService();
                message.flags = 256;
                int newMessageId = UserConfig.getNewMessageId();
                message.id = newMessageId;
                message.local_id = newMessageId;
                message.date = ConnectionsManager.getInstance().getCurrentTime();
                message.from_id = -chat_id;
                message.to_id = new TL_peerChannel();
                message.to_id.channel_id = chat_id;
                message.dialog_id = (long) (-chat_id);
                message.action = new TL_messageActionChatAddUser();
                message.action.users.add(Integer.valueOf(UserConfig.getClientUserId()));
                UserConfig.saveConfig(false);
                final ArrayList<MessageObject> pushMessages = new ArrayList();
                ArrayList<Message> messagesArr = new ArrayList();
                messagesArr.add(message);
                pushMessages.add(new MessageObject(message, null, true));
                MessagesStorage.getInstance().getStorageQueue().postRunnable(new Runnable() {

                    class C04921 implements Runnable {
                        C04921() {
                        }

                        public void run() {
                            NotificationsController.getInstance().processNewMessages(pushMessages, true);
                        }
                    }

                    public void run() {
                        AndroidUtilities.runOnUIThread(new C04921());
                    }
                });
                MessagesStorage.getInstance().putMessages(messagesArr, true, true, false, MediaController.getInstance().getAutodownloadMask());
                AndroidUtilities.runOnUIThread(new Runnable() {
                    public void run() {
                        MessagesController.this.updateInterfaceWithMessages((long) (-chat_id), pushMessages);
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
                    }
                });
            }
        }
    }

    public void convertGroup() {
    }

    public void checkChannelInviter(final int chat_id) {
        AndroidUtilities.runOnUIThread(new Runnable() {
            public void run() {
                final Chat chat = MessagesController.this.getChat(Integer.valueOf(chat_id));
                if (chat != null && ChatObject.isChannel(chat_id) && !chat.creator) {
                    TL_channels_getParticipant req = new TL_channels_getParticipant();
                    req.channel = MessagesController.getInputChannel(chat_id);
                    req.user_id = new TL_inputUserSelf();
                    ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
                        public void run(TLObject response, TL_error error) {
                            final TL_channels_channelParticipant res = (TL_channels_channelParticipant) response;
                            if (res != null && (res.participant instanceof TL_channelParticipantSelf) && res.participant.inviter_id != UserConfig.getClientUserId()) {
                                if (!chat.megagroup || !MessagesStorage.getInstance().isMigratedChat(chat.id)) {
                                    AndroidUtilities.runOnUIThread(new Runnable() {
                                        public void run() {
                                            MessagesController.this.putUsers(res.users, false);
                                        }
                                    });
                                    MessagesStorage.getInstance().putUsersAndChats(res.users, null, true, true);
                                    TL_messageService message = new TL_messageService();
                                    message.media_unread = true;
                                    message.unread = true;
                                    message.flags = 256;
                                    if (chat.megagroup) {
                                        message.flags |= Integer.MIN_VALUE;
                                    }
                                    int newMessageId = UserConfig.getNewMessageId();
                                    message.id = newMessageId;
                                    message.local_id = newMessageId;
                                    message.date = res.participant.date;
                                    message.action = new TL_messageActionChatAddUser();
                                    if (chat.megagroup) {
                                        message.from_id = res.participant.inviter_id;
                                        message.action.users.add(Integer.valueOf(UserConfig.getClientUserId()));
                                    } else {
                                        message.from_id = -chat_id;
                                        message.action.users.add(Integer.valueOf(res.participant.inviter_id));
                                    }
                                    message.to_id = new TL_peerChannel();
                                    message.to_id.channel_id = chat_id;
                                    message.dialog_id = (long) (-chat_id);
                                    UserConfig.saveConfig(false);
                                    final ArrayList<MessageObject> pushMessages = new ArrayList();
                                    ArrayList<Message> messagesArr = new ArrayList();
                                    ConcurrentHashMap<Integer, User> usersDict = new ConcurrentHashMap();
                                    for (int a = 0; a < res.users.size(); a++) {
                                        User user = (User) res.users.get(a);
                                        usersDict.put(Integer.valueOf(user.id), user);
                                    }
                                    messagesArr.add(message);
                                    pushMessages.add(new MessageObject(message, usersDict, true));
                                    MessagesStorage.getInstance().getStorageQueue().postRunnable(new Runnable() {

                                        class C04941 implements Runnable {
                                            C04941() {
                                            }

                                            public void run() {
                                                NotificationsController.getInstance().processNewMessages(pushMessages, true);
                                            }
                                        }

                                        public void run() {
                                            AndroidUtilities.runOnUIThread(new C04941());
                                        }
                                    });
                                    MessagesStorage.getInstance().putMessages(messagesArr, true, true, false, MediaController.getInstance().getAutodownloadMask());
                                    AndroidUtilities.runOnUIThread(new Runnable() {
                                        public void run() {
                                            MessagesController.this.updateInterfaceWithMessages((long) (-chat_id), pushMessages);
                                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
                                        }
                                    });
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    private int getUpdateType(Update update) {
        if ((update instanceof TL_updateNewMessage) || (update instanceof TL_updateReadMessagesContents) || (update instanceof TL_updateReadHistoryInbox) || (update instanceof TL_updateReadHistoryOutbox) || (update instanceof TL_updateDeleteMessages) || (update instanceof TL_updateWebPage)) {
            return 0;
        }
        if (update instanceof TL_updateNewEncryptedMessage) {
            return 1;
        }
        if ((update instanceof TL_updateNewChannelMessage) || (update instanceof TL_updateDeleteChannelMessages)) {
            return 2;
        }
        return 3;
    }

    public void processUpdates(Updates updates, boolean fromQueue) {
        int a;
        ArrayList<Integer> needGetChannelsDiff = null;
        boolean needGetDiff = false;
        boolean needReceivedQueue = false;
        boolean updateStatus = false;
        if (updates instanceof TL_updateShort) {
            ArrayList<Update> arr = new ArrayList();
            arr.add(updates.update);
            processUpdateArray(arr, null, null);
        } else if ((updates instanceof TL_updateShortChatMessage) || (updates instanceof TL_updateShortMessage)) {
            int user_id;
            boolean missingData;
            if (updates instanceof TL_updateShortChatMessage) {
                user_id = updates.from_id;
            } else {
                user_id = updates.user_id;
            }
            User user = getUser(Integer.valueOf(user_id));
            User user2 = null;
            Chat channel = null;
            if (user == null) {
                user = MessagesStorage.getInstance().getUserSync(user_id);
                putUser(user, true);
            }
            boolean needFwdUser = false;
            if (updates.fwd_from_id instanceof TL_peerUser) {
                user2 = getUser(Integer.valueOf(updates.fwd_from_id.user_id));
                if (user2 == null) {
                    user2 = MessagesStorage.getInstance().getUserSync(updates.fwd_from_id.user_id);
                    putUser(user2, true);
                }
                needFwdUser = true;
            } else if (updates.fwd_from_id instanceof TL_peerChannel) {
                channel = getChat(Integer.valueOf(updates.fwd_from_id.channel_id));
                if (channel == null) {
                    channel = MessagesStorage.getInstance().getChatSync(updates.fwd_from_id.channel_id);
                    putChat(channel, true);
                }
                needFwdUser = true;
            }
            if (updates instanceof TL_updateShortMessage) {
                missingData = user == null || (needFwdUser && user2 == null && channel == null);
            } else {
                Chat chat = getChat(Integer.valueOf(updates.chat_id));
                if (chat == null) {
                    chat = MessagesStorage.getInstance().getChatSync(updates.chat_id);
                    putChat(chat, true);
                }
                missingData = chat == null || user == null || (needFwdUser && user2 == null && channel == null);
            }
            if (!(user == null || user.status == null || user.status.expires > 0)) {
                this.onlinePrivacy.put(Integer.valueOf(user.id), Integer.valueOf(ConnectionsManager.getInstance().getCurrentTime()));
                updateStatus = true;
            }
            if (missingData) {
                needGetDiff = true;
            } else if (MessagesStorage.lastPtsValue + updates.pts_count == updates.pts) {
                Message message = new TL_message();
                message.id = updates.id;
                if (updates instanceof TL_updateShortMessage) {
                    if (updates.out) {
                        message.from_id = UserConfig.getClientUserId();
                    } else {
                        message.from_id = user_id;
                    }
                    message.to_id = new TL_peerUser();
                    message.to_id.user_id = user_id;
                    message.dialog_id = (long) user_id;
                } else {
                    message.from_id = user_id;
                    message.to_id = new TL_peerChat();
                    message.to_id.chat_id = updates.chat_id;
                    message.dialog_id = (long) (-updates.chat_id);
                }
                message.out = updates.out;
                message.unread = updates.unread;
                message.mentioned = updates.mentioned;
                message.media_unread = updates.media_unread;
                message.entities = updates.entities;
                message.message = updates.message;
                message.date = updates.date;
                message.flags = updates.flags | 256;
                message.fwd_from_id = updates.fwd_from_id;
                message.fwd_date = updates.fwd_date;
                message.reply_to_msg_id = updates.reply_to_msg_id;
                message.media = new TL_messageMediaEmpty();
                MessagesStorage.lastPtsValue = updates.pts;
                MessageObject messageObject = new MessageObject(message, null, true);
                ArrayList<MessageObject> objArr = new ArrayList();
                objArr.add(messageObject);
                ArrayList<Message> arr2 = new ArrayList();
                arr2.add(message);
                boolean printUpdate;
                final boolean z;
                final ArrayList<MessageObject> arrayList;
                if (updates instanceof TL_updateShortMessage) {
                    printUpdate = !updates.out && updatePrintingUsersWithNewMessages((long) updates.user_id, objArr);
                    if (printUpdate) {
                        updatePrintingStrings();
                    }
                    z = printUpdate;
                    final int i = user_id;
                    arrayList = objArr;
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        public void run() {
                            if (z) {
                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.updateInterfaces, Integer.valueOf(64));
                            }
                            MessagesController.this.updateInterfaceWithMessages((long) i, arrayList);
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
                        }
                    });
                } else {
                    printUpdate = updatePrintingUsersWithNewMessages((long) (-updates.chat_id), objArr);
                    if (printUpdate) {
                        updatePrintingStrings();
                    }
                    z = printUpdate;
                    final Updates updates2 = updates;
                    arrayList = objArr;
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        public void run() {
                            if (z) {
                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.updateInterfaces, Integer.valueOf(64));
                            }
                            MessagesController.this.updateInterfaceWithMessages((long) (-updates2.chat_id), arrayList);
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
                        }
                    });
                }
                final MessageObject messageObject2 = messageObject;
                final ArrayList<MessageObject> arrayList2 = objArr;
                MessagesStorage.getInstance().getStorageQueue().postRunnable(new Runnable() {

                    class C04971 implements Runnable {
                        C04971() {
                        }

                        public void run() {
                            if (!messageObject2.isOut()) {
                                NotificationsController.getInstance().processNewMessages(arrayList2, true);
                            }
                        }
                    }

                    public void run() {
                        AndroidUtilities.runOnUIThread(new C04971());
                    }
                });
                MessagesStorage.getInstance().putMessages(arr2, false, true, false, 0);
            } else if (MessagesStorage.lastPtsValue != updates.pts) {
                FileLog.m609e("tmessages", "need get diff short message, pts: " + MessagesStorage.lastPtsValue + " " + updates.pts + " count = " + updates.pts_count);
                if (this.gettingDifference || this.updatesStartWaitTimePts == 0 || Math.abs(System.currentTimeMillis() - this.updatesStartWaitTimePts) <= 1500) {
                    if (this.updatesStartWaitTimePts == 0) {
                        this.updatesStartWaitTimePts = System.currentTimeMillis();
                    }
                    FileLog.m609e("tmessages", "add to queue");
                    this.updatesQueuePts.add(updates);
                } else {
                    needGetDiff = true;
                }
            }
        } else if ((updates instanceof TL_updatesCombined) || (updates instanceof TL_updates)) {
            MessagesStorage.getInstance().putUsersAndChats(updates.users, updates.chats, true, true);
            Collections.sort(updates.updates, new Comparator<Update>() {
                public int compare(Update lhs, Update rhs) {
                    int ltype = MessagesController.this.getUpdateType(lhs);
                    int rtype = MessagesController.this.getUpdateType(rhs);
                    if (ltype != rtype) {
                        return AndroidUtilities.compare(ltype, rtype);
                    }
                    if (ltype == 0) {
                        return AndroidUtilities.compare(lhs.pts, rhs.pts);
                    }
                    if (ltype == 1) {
                        return AndroidUtilities.compare(lhs.qts, rhs.qts);
                    }
                    return 0;
                }
            });
            a = 0;
            while (updates.updates.size() > 0) {
                Update update = (Update) updates.updates.get(a);
                TL_updates updatesNew;
                int b;
                Update update2;
                if (getUpdateType(update) != 0) {
                    if (getUpdateType(update) != 1) {
                        if (getUpdateType(update) != 2) {
                            break;
                        }
                        int channelId;
                        if (update instanceof TL_updateNewChannelMessage) {
                            channelId = ((TL_updateNewChannelMessage) update).message.to_id.channel_id;
                        } else {
                            channelId = update.channel_id;
                        }
                        Integer channelPts = (Integer) this.channelsPts.get(Integer.valueOf(channelId));
                        if (channelPts == null) {
                            channelPts = Integer.valueOf(MessagesStorage.getInstance().getChannelPtsSync(channelId));
                            if (channelPts.intValue() == 0) {
                                channelPts = Integer.valueOf(update.pts - update.pts_count);
                            }
                            this.channelsPts.put(Integer.valueOf(channelId), channelPts);
                        }
                        updatesNew = new TL_updates();
                        updatesNew.updates.add(update);
                        updatesNew.pts = update.pts;
                        updatesNew.pts_count = update.pts_count;
                        for (b = a + 1; b < updates.updates.size(); b = (b - 1) + 1) {
                            update2 = (Update) updates.updates.get(b);
                            if (getUpdateType(update2) != 2 || updatesNew.pts + update2.pts_count != update2.pts) {
                                break;
                            }
                            updatesNew.updates.add(update2);
                            updatesNew.pts = update2.pts;
                            updatesNew.pts_count += update2.pts_count;
                            updates.updates.remove(b);
                        }
                        if (channelPts.intValue() + updatesNew.pts_count == updatesNew.pts) {
                            if (processUpdateArray(updatesNew.updates, updates.users, updates.chats)) {
                                this.channelsPts.put(Integer.valueOf(channelId), Integer.valueOf(updatesNew.pts));
                                MessagesStorage.getInstance().saveChannelPts(channelId, updatesNew.pts);
                            } else {
                                FileLog.m609e("tmessages", "need get channel diff inner TL_updates, channel_id = " + channelId);
                                if (needGetChannelsDiff == null) {
                                    needGetChannelsDiff = new ArrayList();
                                } else {
                                    if (!needGetChannelsDiff.contains(Integer.valueOf(channelId))) {
                                        needGetChannelsDiff.add(Integer.valueOf(channelId));
                                    }
                                }
                            }
                        } else if (channelPts.intValue() != updatesNew.pts) {
                            FileLog.m609e("tmessages", update + " need get channel diff, pts: " + channelPts + " " + updatesNew.pts + " count = " + updatesNew.pts_count + " channelId = " + channelId);
                            Long updatesStartWaitTime = (Long) this.updatesStartWaitTimeChannels.get(Integer.valueOf(channelId));
                            Boolean gettingDifferenceChannel = (Boolean) this.gettingDifferenceChannels.get(Integer.valueOf(channelId));
                            if (gettingDifferenceChannel == null) {
                                gettingDifferenceChannel = Boolean.valueOf(false);
                            }
                            if (gettingDifferenceChannel.booleanValue() || updatesStartWaitTime == null || Math.abs(System.currentTimeMillis() - updatesStartWaitTime.longValue()) <= 1500) {
                                if (updatesStartWaitTime == null) {
                                    this.updatesStartWaitTimeChannels.put(Integer.valueOf(channelId), Long.valueOf(System.currentTimeMillis()));
                                }
                                FileLog.m609e("tmessages", "add to queue");
                                ArrayList<Updates> arrayList3 = (ArrayList) this.updatesQueueChannels.get(Integer.valueOf(channelId));
                                if (arrayList3 == null) {
                                    arrayList3 = new ArrayList();
                                    this.updatesQueueChannels.put(Integer.valueOf(channelId), arrayList3);
                                }
                                arrayList3.add(updatesNew);
                            } else if (needGetChannelsDiff == null) {
                                needGetChannelsDiff = new ArrayList();
                            } else {
                                if (!needGetChannelsDiff.contains(Integer.valueOf(channelId))) {
                                    needGetChannelsDiff.add(Integer.valueOf(channelId));
                                }
                            }
                        }
                    } else {
                        updatesNew = new TL_updates();
                        updatesNew.updates.add(update);
                        updatesNew.pts = update.qts;
                        for (b = a + 1; b < updates.updates.size(); b = (b - 1) + 1) {
                            update2 = (Update) updates.updates.get(b);
                            if (getUpdateType(update2) != 1 || updatesNew.pts + 1 != update2.qts) {
                                break;
                            }
                            updatesNew.updates.add(update2);
                            updatesNew.pts = update2.qts;
                            updates.updates.remove(b);
                        }
                        if (MessagesStorage.lastQtsValue == 0 || MessagesStorage.lastQtsValue + updatesNew.updates.size() == updatesNew.pts) {
                            processUpdateArray(updatesNew.updates, updates.users, updates.chats);
                            MessagesStorage.lastQtsValue = updatesNew.pts;
                            needReceivedQueue = true;
                        } else if (MessagesStorage.lastPtsValue != updatesNew.pts) {
                            FileLog.m609e("tmessages", update + " need get diff, qts: " + MessagesStorage.lastQtsValue + " " + updatesNew.pts);
                            if (this.gettingDifference || this.updatesStartWaitTimeQts == 0 || (this.updatesStartWaitTimeQts != 0 && Math.abs(System.currentTimeMillis() - this.updatesStartWaitTimeQts) <= 1500)) {
                                if (this.updatesStartWaitTimeQts == 0) {
                                    this.updatesStartWaitTimeQts = System.currentTimeMillis();
                                }
                                FileLog.m609e("tmessages", "add to queue");
                                this.updatesQueueQts.add(updatesNew);
                            } else {
                                needGetDiff = true;
                            }
                        }
                    }
                } else {
                    updatesNew = new TL_updates();
                    updatesNew.updates.add(update);
                    updatesNew.pts = update.pts;
                    updatesNew.pts_count = update.pts_count;
                    for (b = a + 1; b < updates.updates.size(); b = (b - 1) + 1) {
                        update2 = (Update) updates.updates.get(b);
                        if (getUpdateType(update2) != 0 || updatesNew.pts + update2.pts_count != update2.pts) {
                            break;
                        }
                        updatesNew.updates.add(update2);
                        updatesNew.pts = update2.pts;
                        updatesNew.pts_count += update2.pts_count;
                        updates.updates.remove(b);
                    }
                    if (MessagesStorage.lastPtsValue + updatesNew.pts_count == updatesNew.pts) {
                        if (processUpdateArray(updatesNew.updates, updates.users, updates.chats)) {
                            MessagesStorage.lastPtsValue = updatesNew.pts;
                        } else {
                            FileLog.m609e("tmessages", "need get diff inner TL_updates, seq: " + MessagesStorage.lastSeqValue + " " + updates.seq);
                            needGetDiff = true;
                        }
                    } else if (MessagesStorage.lastPtsValue != updatesNew.pts) {
                        FileLog.m609e("tmessages", update + " need get diff, pts: " + MessagesStorage.lastPtsValue + " " + updatesNew.pts + " count = " + updatesNew.pts_count);
                        if (this.gettingDifference || this.updatesStartWaitTimePts == 0 || (this.updatesStartWaitTimePts != 0 && Math.abs(System.currentTimeMillis() - this.updatesStartWaitTimePts) <= 1500)) {
                            if (this.updatesStartWaitTimePts == 0) {
                                this.updatesStartWaitTimePts = System.currentTimeMillis();
                            }
                            FileLog.m609e("tmessages", "add to queue");
                            this.updatesQueuePts.add(updatesNew);
                        } else {
                            needGetDiff = true;
                        }
                    }
                }
                updates.updates.remove(a);
                a = (a - 1) + 1;
            }
            boolean processUpdate = updates instanceof TL_updatesCombined ? MessagesStorage.lastSeqValue + 1 == updates.seq_start || MessagesStorage.lastSeqValue == updates.seq_start : MessagesStorage.lastSeqValue + 1 == updates.seq || updates.seq == 0 || updates.seq == MessagesStorage.lastSeqValue;
            if (processUpdate) {
                processUpdateArray(updates.updates, updates.users, updates.chats);
                if (updates.date != 0) {
                    MessagesStorage.lastDateValue = updates.date;
                }
                if (updates.seq != 0) {
                    MessagesStorage.lastSeqValue = updates.seq;
                }
            } else {
                if (updates instanceof TL_updatesCombined) {
                    FileLog.m609e("tmessages", "need get diff TL_updatesCombined, seq: " + MessagesStorage.lastSeqValue + " " + updates.seq_start);
                } else {
                    FileLog.m609e("tmessages", "need get diff TL_updates, seq: " + MessagesStorage.lastSeqValue + " " + updates.seq);
                }
                if (this.gettingDifference || this.updatesStartWaitTimeSeq == 0 || Math.abs(System.currentTimeMillis() - this.updatesStartWaitTimeSeq) <= 1500) {
                    if (this.updatesStartWaitTimeSeq == 0) {
                        this.updatesStartWaitTimeSeq = System.currentTimeMillis();
                    }
                    FileLog.m609e("tmessages", "add TL_updates/Combined to queue");
                    this.updatesQueueSeq.add(updates);
                } else {
                    needGetDiff = true;
                }
            }
        } else if (updates instanceof TL_updatesTooLong) {
            FileLog.m609e("tmessages", "need get diff TL_updatesTooLong");
            needGetDiff = true;
        } else if (updates instanceof UserActionUpdatesSeq) {
            MessagesStorage.lastSeqValue = updates.seq;
        } else if (updates instanceof UserActionUpdatesPts) {
            if (updates.chat_id != 0) {
                this.channelsPts.put(Integer.valueOf(updates.chat_id), Integer.valueOf(updates.pts));
                MessagesStorage.getInstance().saveChannelPts(updates.chat_id, updates.pts);
            } else {
                MessagesStorage.lastPtsValue = updates.pts;
            }
        }
        SecretChatHelper.getInstance().processPendingEncMessages();
        if (!fromQueue) {
            ArrayList<Integer> arrayList4 = new ArrayList(this.updatesQueueChannels.keySet());
            for (a = 0; a < arrayList4.size(); a++) {
                Integer key = (Integer) arrayList4.get(a);
                if (needGetChannelsDiff == null || !needGetChannelsDiff.contains(key)) {
                    processChannelsUpdatesQueue(key.intValue(), 0);
                } else {
                    getChannelDifference(key.intValue());
                }
            }
            if (needGetDiff) {
                getDifference();
            } else {
                for (a = 0; a < 3; a++) {
                    processUpdatesQueue(a, 0);
                }
            }
        }
        if (needReceivedQueue) {
            TLObject req = new TL_messages_receivedQueue();
            req.max_qts = MessagesStorage.lastQtsValue;
            ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
                public void run(TLObject response, TL_error error) {
                }
            });
        }
        if (updateStatus) {
            AndroidUtilities.runOnUIThread(new Runnable() {
                public void run() {
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.updateInterfaces, Integer.valueOf(4));
                }
            });
        }
        MessagesStorage.getInstance().saveDiffParams(MessagesStorage.lastSeqValue, MessagesStorage.lastPtsValue, MessagesStorage.lastDateValue, MessagesStorage.lastQtsValue);
    }

    public boolean processUpdateArray(ArrayList<Update> updates, ArrayList<User> usersArr, ArrayList<Chat> chatsArr) {
        if (updates.isEmpty()) {
            if (!(usersArr == null && chatsArr == null)) {
                final ArrayList<User> arrayList = usersArr;
                final ArrayList<Chat> arrayList2 = chatsArr;
                AndroidUtilities.runOnUIThread(new Runnable() {
                    public void run() {
                        MessagesController.this.putUsers(arrayList, false);
                        MessagesController.this.putChats(arrayList2, false);
                    }
                });
            }
            return true;
        }
        AbstractMap usersDict;
        Iterator i$;
        AbstractMap chatsDict;
        long currentTime = System.currentTimeMillis();
        HashMap<Long, ArrayList<MessageObject>> messages = new HashMap();
        HashMap<Long, WebPage> webPages = new HashMap();
        ArrayList<MessageObject> pushMessages = new ArrayList();
        ArrayList<Message> messagesArr = new ArrayList();
        final SparseArray<SparseIntArray> channelViews = new SparseArray();
        SparseArray<ArrayList<TL_messageGroup>> channelsGroups = new SparseArray();
        final SparseArray<Long> markAsReadMessagesInbox = new SparseArray();
        final SparseIntArray markAsReadMessagesOutbox = new SparseIntArray();
        final ArrayList<Long> markAsReadMessages = new ArrayList();
        final HashMap<Integer, Integer> markAsReadEncrypted = new HashMap();
        final SparseArray<ArrayList<Integer>> deletedMessages = new SparseArray();
        boolean printChanged = false;
        final ArrayList<ChatParticipants> chatInfoToUpdate = new ArrayList();
        ArrayList<Update> updatesOnMainThread = new ArrayList();
        ArrayList<TL_updateEncryptedMessagesRead> tasks = new ArrayList();
        final ArrayList<Integer> contactsIds = new ArrayList();
        boolean checkForUsers = true;
        if (usersArr != null) {
            usersDict = new ConcurrentHashMap();
            i$ = usersArr.iterator();
            while (i$.hasNext()) {
                User user = (User) i$.next();
                usersDict.put(Integer.valueOf(user.id), user);
            }
        } else {
            checkForUsers = false;
            usersDict = this.users;
        }
        if (chatsArr != null) {
            chatsDict = new ConcurrentHashMap();
            i$ = chatsArr.iterator();
            while (i$.hasNext()) {
                Chat chat = (Chat) i$.next();
                chatsDict.put(Integer.valueOf(chat.id), chat);
            }
        } else {
            checkForUsers = false;
            chatsDict = this.chats;
        }
        if (!(usersArr == null && chatsArr == null)) {
            arrayList = usersArr;
            arrayList2 = chatsArr;
            AndroidUtilities.runOnUIThread(new Runnable() {
                public void run() {
                    MessagesController.this.putUsers(arrayList, false);
                    MessagesController.this.putChats(arrayList2, false);
                }
            });
        }
        int interfaceUpdateMask = 0;
        for (int c = 0; c < updates.size(); c++) {
            int a;
            ArrayList<Integer> arrayList3;
            Update update = (Update) updates.get(c);
            Message message;
            Integer value;
            MessageObject messageObject;
            ArrayList<MessageObject> arr;
            if ((update instanceof TL_updateNewMessage) || (update instanceof TL_updateNewChannelMessage)) {
                if (update instanceof TL_updateNewMessage) {
                    message = ((TL_updateNewMessage) update).message;
                } else {
                    message = ((TL_updateNewChannelMessage) update).message;
                }
                if (checkForUsers) {
                    int chat_id = 0;
                    if (message.to_id.channel_id != 0) {
                        chat_id = message.to_id.channel_id;
                    } else if (message.to_id.chat_id != 0) {
                        chat_id = message.to_id.chat_id;
                    }
                    if (chat_id != 0) {
                        chat = (Chat) chatsDict.get(Integer.valueOf(chat_id));
                        if (chat == null) {
                            chat = getChat(Integer.valueOf(chat_id));
                        }
                        if (chat == null) {
                            return false;
                        }
                        if (chat.megagroup) {
                            message.flags |= Integer.MIN_VALUE;
                        }
                        if (chat.left) {
                            continue;
                        }
                    }
                    if (message.from_id > 0) {
                        user = getUser(Integer.valueOf(message.from_id));
                        if (usersDict.get(Integer.valueOf(message.from_id)) == null && user == null) {
                            return false;
                        }
                        if (!(user == null || user.status == null || user.status.expires > 0)) {
                            this.onlinePrivacy.put(Integer.valueOf(message.from_id), Integer.valueOf(ConnectionsManager.getInstance().getCurrentTime()));
                            interfaceUpdateMask |= 4;
                        }
                    }
                }
                if (message.action instanceof TL_messageActionChatDeleteUser) {
                    user = (User) usersDict.get(Integer.valueOf(message.action.user_id));
                    if (user != null && user.bot) {
                        message.reply_markup = new TL_replyKeyboardHide();
                    } else if (message.action.user_id == UserConfig.getClientUserId()) {
                    }
                } else if ((message.action instanceof TL_messageActionChatMigrateTo) || (message.action instanceof TL_messageActionChannelCreate)) {
                    message.unread = false;
                    message.media_unread = false;
                }
                if (update instanceof TL_updateNewChannelMessage) {
                    if (!(message.to_id.channel_id == 0 || message.out)) {
                        message.unread = true;
                        if (message.from_id <= 0 || (message.flags & Integer.MIN_VALUE) != 0) {
                            message.media_unread = true;
                        }
                    }
                    value = (Integer) this.dialogs_read_inbox_max.get(Long.valueOf((long) (-update.channel_id)));
                    if (value == null) {
                        value = Integer.valueOf(MessagesStorage.getInstance().getChannelReadInboxMax(update.channel_id));
                    }
                    if (value.intValue() >= message.id) {
                        message.unread = false;
                        message.media_unread = false;
                    }
                }
                messagesArr.add(message);
                ImageLoader.saveMessageThumbs(message);
                messageObject = new MessageObject(message, usersDict, chatsDict, true);
                if (messageObject.type == 11) {
                    interfaceUpdateMask |= 8;
                } else if (messageObject.type == 10) {
                    interfaceUpdateMask |= 16;
                }
                if (message.to_id.chat_id != 0) {
                    message.dialog_id = (long) (-message.to_id.chat_id);
                } else if (message.to_id.channel_id != 0) {
                    message.dialog_id = (long) (-message.to_id.channel_id);
                } else {
                    if (message.to_id.user_id == UserConfig.getClientUserId()) {
                        message.to_id.user_id = message.from_id;
                    }
                    message.dialog_id = (long) message.to_id.user_id;
                }
                arr = (ArrayList) messages.get(Long.valueOf(message.dialog_id));
                if (arr == null) {
                    arr = new ArrayList();
                    messages.put(Long.valueOf(message.dialog_id), arr);
                }
                arr.add(messageObject);
                if (!messageObject.isOut() && ((messageObject.isUnread() && message.to_id.channel_id == 0) || messageObject.isContentUnread())) {
                    pushMessages.add(messageObject);
                }
            } else if (update instanceof TL_updateReadMessagesContents) {
                for (a = 0; a < update.messages.size(); a++) {
                    markAsReadMessages.add(Long.valueOf((long) ((Integer) update.messages.get(a)).intValue()));
                }
            } else if (update instanceof TL_updateReadHistoryInbox) {
                peer = ((TL_updateReadHistoryInbox) update).peer;
                if (peer.chat_id != 0) {
                    markAsReadMessagesInbox.put(-peer.chat_id, Long.valueOf((long) update.max_id));
                } else {
                    markAsReadMessagesInbox.put(peer.user_id, Long.valueOf((long) update.max_id));
                }
            } else if (update instanceof TL_updateReadHistoryOutbox) {
                peer = ((TL_updateReadHistoryOutbox) update).peer;
                if (peer.chat_id != 0) {
                    markAsReadMessagesOutbox.put(-peer.chat_id, update.max_id);
                } else {
                    markAsReadMessagesOutbox.put(peer.user_id, update.max_id);
                }
            } else if (update instanceof TL_updateDeleteMessages) {
                arrayList3 = (ArrayList) deletedMessages.get(0);
                if (arrayList3 == null) {
                    arrayList3 = new ArrayList();
                    deletedMessages.put(0, arrayList3);
                }
                arrayList3.addAll(update.messages);
            } else if ((update instanceof TL_updateUserTyping) || (update instanceof TL_updateChatUserTyping)) {
                if (update.user_id != UserConfig.getClientUserId()) {
                    uid = (long) (-update.chat_id);
                    if (uid == 0) {
                        uid = (long) update.user_id;
                    }
                    arr = (ArrayList) this.printingUsers.get(Long.valueOf(uid));
                    if (!(update.action instanceof TL_sendMessageCancelAction)) {
                        if (arr == null) {
                            arr = new ArrayList();
                            this.printingUsers.put(Long.valueOf(uid), arr);
                        }
                        exist = false;
                        i$ = arr.iterator();
                        while (i$.hasNext()) {
                            u = (PrintingUser) i$.next();
                            if (u.userId == update.user_id) {
                                exist = true;
                                u.lastTime = currentTime;
                                if (u.action.getClass() != update.action.getClass()) {
                                    printChanged = true;
                                }
                                u.action = update.action;
                                if (!exist) {
                                    newUser = new PrintingUser();
                                    newUser.userId = update.user_id;
                                    newUser.lastTime = currentTime;
                                    newUser.action = update.action;
                                    arr.add(newUser);
                                    printChanged = true;
                                }
                            }
                        }
                        if (exist) {
                            newUser = new PrintingUser();
                            newUser.userId = update.user_id;
                            newUser.lastTime = currentTime;
                            newUser.action = update.action;
                            arr.add(newUser);
                            printChanged = true;
                        }
                    } else if (arr != null) {
                        for (a = 0; a < arr.size(); a++) {
                            if (((PrintingUser) arr.get(a)).userId == update.user_id) {
                                arr.remove(a);
                                printChanged = true;
                                break;
                            }
                        }
                        if (arr.isEmpty()) {
                            this.printingUsers.remove(Long.valueOf(uid));
                        }
                    }
                    this.onlinePrivacy.put(Integer.valueOf(update.user_id), Integer.valueOf(ConnectionsManager.getInstance().getCurrentTime()));
                }
            } else if (update instanceof TL_updateChatParticipants) {
                interfaceUpdateMask |= 32;
                chatInfoToUpdate.add(update.participants);
            } else if (update instanceof TL_updateUserStatus) {
                interfaceUpdateMask |= 4;
                updatesOnMainThread.add(update);
            } else if (update instanceof TL_updateUserName) {
                interfaceUpdateMask |= 1;
                updatesOnMainThread.add(update);
            } else if (update instanceof TL_updateUserPhoto) {
                interfaceUpdateMask |= 2;
                MessagesStorage.getInstance().clearUserPhotos(update.user_id);
                updatesOnMainThread.add(update);
            } else if (update instanceof TL_updateUserPhone) {
                interfaceUpdateMask |= 1024;
                updatesOnMainThread.add(update);
            } else if (update instanceof TL_updateContactRegistered) {
                if (this.enableJoined) {
                    if (usersDict.containsKey(Integer.valueOf(update.user_id)) && !MessagesStorage.getInstance().isDialogHasMessages((long) update.user_id)) {
                        newMessage = new TL_messageService();
                        newMessage.action = new TL_messageActionUserJoined();
                        r4 = UserConfig.getNewMessageId();
                        newMessage.id = r4;
                        newMessage.local_id = r4;
                        UserConfig.saveConfig(false);
                        newMessage.unread = false;
                        newMessage.flags = 256;
                        newMessage.date = update.date;
                        newMessage.from_id = update.user_id;
                        newMessage.to_id = new TL_peerUser();
                        newMessage.to_id.user_id = UserConfig.getClientUserId();
                        newMessage.dialog_id = (long) update.user_id;
                        messagesArr.add(newMessage);
                        messageObject = new MessageObject(newMessage, usersDict, chatsDict, true);
                        arr = (ArrayList) messages.get(Long.valueOf(newMessage.dialog_id));
                        if (arr == null) {
                            arr = new ArrayList();
                            messages.put(Long.valueOf(newMessage.dialog_id), arr);
                        }
                        arr.add(messageObject);
                    }
                }
            } else if (update instanceof TL_updateContactLink) {
                int idx;
                if (update.my_link instanceof TL_contactLinkContact) {
                    idx = contactsIds.indexOf(Integer.valueOf(-update.user_id));
                    if (idx != -1) {
                        contactsIds.remove(idx);
                    }
                    if (!contactsIds.contains(Integer.valueOf(update.user_id))) {
                        contactsIds.add(Integer.valueOf(update.user_id));
                    }
                } else {
                    idx = contactsIds.indexOf(Integer.valueOf(update.user_id));
                    if (idx != -1) {
                        contactsIds.remove(idx);
                    }
                    if (!contactsIds.contains(Integer.valueOf(update.user_id))) {
                        contactsIds.add(Integer.valueOf(-update.user_id));
                    }
                }
            } else if (update instanceof TL_updateNewAuthorization) {
                AndroidUtilities.runOnUIThread(new Runnable() {
                    public void run() {
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.newSessionReceived, new Object[0]);
                    }
                });
                newMessage = new TL_messageService();
                newMessage.action = new TL_messageActionLoginUnknownLocation();
                newMessage.action.title = update.device;
                newMessage.action.address = update.location;
                r4 = UserConfig.getNewMessageId();
                newMessage.id = r4;
                newMessage.local_id = r4;
                UserConfig.saveConfig(false);
                newMessage.unread = true;
                newMessage.flags = 256;
                newMessage.date = update.date;
                newMessage.from_id = 777000;
                newMessage.to_id = new TL_peerUser();
                newMessage.to_id.user_id = UserConfig.getClientUserId();
                newMessage.dialog_id = 777000;
                messagesArr.add(newMessage);
                messageObject = new MessageObject(newMessage, usersDict, chatsDict, true);
                arr = (ArrayList) messages.get(Long.valueOf(newMessage.dialog_id));
                if (arr == null) {
                    arr = new ArrayList();
                    messages.put(Long.valueOf(newMessage.dialog_id), arr);
                }
                arr.add(messageObject);
                pushMessages.add(messageObject);
            } else if (!(update instanceof TL_updateNewGeoChatMessage)) {
                if (update instanceof TL_updateNewEncryptedMessage) {
                    ArrayList<Message> decryptedMessages = SecretChatHelper.getInstance().decryptMessage(((TL_updateNewEncryptedMessage) update).message);
                    if (!(decryptedMessages == null || decryptedMessages.isEmpty())) {
                        uid = ((long) ((TL_updateNewEncryptedMessage) update).message.chat_id) << 32;
                        arr = (ArrayList) messages.get(Long.valueOf(uid));
                        if (arr == null) {
                            arr = new ArrayList();
                            messages.put(Long.valueOf(uid), arr);
                        }
                        i$ = decryptedMessages.iterator();
                        while (i$.hasNext()) {
                            message = (Message) i$.next();
                            ImageLoader.saveMessageThumbs(message);
                            messagesArr.add(message);
                            messageObject = new MessageObject(message, usersDict, chatsDict, true);
                            arr.add(messageObject);
                            pushMessages.add(messageObject);
                        }
                    }
                } else if (update instanceof TL_updateEncryptedChatTyping) {
                    EncryptedChat encryptedChat = getEncryptedChatDB(update.chat_id);
                    if (encryptedChat != null) {
                        update.user_id = encryptedChat.user_id;
                        uid = ((long) update.chat_id) << 32;
                        arr = (ArrayList) this.printingUsers.get(Long.valueOf(uid));
                        if (arr == null) {
                            arr = new ArrayList();
                            this.printingUsers.put(Long.valueOf(uid), arr);
                        }
                        exist = false;
                        i$ = arr.iterator();
                        while (i$.hasNext()) {
                            u = (PrintingUser) i$.next();
                            if (u.userId == update.user_id) {
                                exist = true;
                                u.lastTime = currentTime;
                                u.action = new TL_sendMessageTypingAction();
                                break;
                            }
                        }
                        if (!exist) {
                            newUser = new PrintingUser();
                            newUser.userId = update.user_id;
                            newUser.lastTime = currentTime;
                            newUser.action = new TL_sendMessageTypingAction();
                            arr.add(newUser);
                            printChanged = true;
                        }
                        this.onlinePrivacy.put(Integer.valueOf(update.user_id), Integer.valueOf(ConnectionsManager.getInstance().getCurrentTime()));
                    }
                } else if (update instanceof TL_updateEncryptedMessagesRead) {
                    markAsReadEncrypted.put(Integer.valueOf(update.chat_id), Integer.valueOf(Math.max(update.max_date, update.date)));
                    tasks.add((TL_updateEncryptedMessagesRead) update);
                } else if (update instanceof TL_updateChatParticipantAdd) {
                    MessagesStorage.getInstance().updateChatInfo(update.chat_id, update.user_id, 0, update.inviter_id, update.version);
                } else if (update instanceof TL_updateChatParticipantDelete) {
                    MessagesStorage.getInstance().updateChatInfo(update.chat_id, update.user_id, 1, 0, update.version);
                } else if (update instanceof TL_updateDcOptions) {
                    ConnectionsManager.getInstance().updateDcSettings();
                } else if (update instanceof TL_updateEncryption) {
                    SecretChatHelper.getInstance().processUpdateEncryption((TL_updateEncryption) update, usersDict);
                } else if (update instanceof TL_updateUserBlocked) {
                    TL_updateUserBlocked finalUpdate = (TL_updateUserBlocked) update;
                    if (finalUpdate.blocked) {
                        ArrayList<Integer> ids = new ArrayList();
                        ids.add(Integer.valueOf(finalUpdate.user_id));
                        MessagesStorage.getInstance().putBlockedUsers(ids, false);
                    } else {
                        MessagesStorage.getInstance().deleteBlockedUser(finalUpdate.user_id);
                    }
                    final TL_updateUserBlocked tL_updateUserBlocked = finalUpdate;
                    MessagesStorage.getInstance().getStorageQueue().postRunnable(new Runnable() {

                        class C04981 implements Runnable {
                            C04981() {
                            }

                            public void run() {
                                if (!tL_updateUserBlocked.blocked) {
                                    MessagesController.this.blockedUsers.remove(Integer.valueOf(tL_updateUserBlocked.user_id));
                                } else if (!MessagesController.this.blockedUsers.contains(Integer.valueOf(tL_updateUserBlocked.user_id))) {
                                    MessagesController.this.blockedUsers.add(Integer.valueOf(tL_updateUserBlocked.user_id));
                                }
                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.blockedUsersDidLoaded, new Object[0]);
                            }
                        }

                        public void run() {
                            AndroidUtilities.runOnUIThread(new C04981());
                        }
                    });
                } else if (update instanceof TL_updateNotifySettings) {
                    updatesOnMainThread.add(update);
                } else if (update instanceof TL_updateServiceNotification) {
                    TL_updateServiceNotification notification = (TL_updateServiceNotification) update;
                    if (notification.popup && notification.message != null && notification.message.length() > 0) {
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.needShowAlert, Integer.valueOf(2), notification.message);
                    }
                    newMessage = new TL_message();
                    r4 = UserConfig.getNewMessageId();
                    newMessage.id = r4;
                    newMessage.local_id = r4;
                    UserConfig.saveConfig(false);
                    newMessage.unread = true;
                    newMessage.flags = 256;
                    newMessage.date = ConnectionsManager.getInstance().getCurrentTime();
                    newMessage.from_id = 777000;
                    newMessage.to_id = new TL_peerUser();
                    newMessage.to_id.user_id = UserConfig.getClientUserId();
                    newMessage.dialog_id = 777000;
                    newMessage.media = update.media;
                    newMessage.flags |= 512;
                    newMessage.message = notification.message;
                    messagesArr.add(newMessage);
                    messageObject = new MessageObject(newMessage, usersDict, chatsDict, true);
                    arr = (ArrayList) messages.get(Long.valueOf(newMessage.dialog_id));
                    if (arr == null) {
                        arr = new ArrayList();
                        messages.put(Long.valueOf(newMessage.dialog_id), arr);
                    }
                    arr.add(messageObject);
                    pushMessages.add(messageObject);
                } else if (update instanceof TL_updatePrivacy) {
                    updatesOnMainThread.add(update);
                } else if (update instanceof TL_updateWebPage) {
                    webPages.put(Long.valueOf(update.webpage.id), update.webpage);
                } else if (update instanceof TL_updateChannelTooLong) {
                    getChannelDifference(update.channel_id);
                } else if (update instanceof TL_updateChannelGroup) {
                    ArrayList<TL_messageGroup> arrayList4 = (ArrayList) channelsGroups.get(update.channel_id);
                    if (arrayList4 == null) {
                        arrayList4 = new ArrayList();
                        channelsGroups.put(update.channel_id, arrayList4);
                    }
                    arrayList4.add(update.group);
                } else if (update instanceof TL_updateReadChannelInbox) {
                    markAsReadMessagesInbox.put(-update.channel_id, Long.valueOf(((long) update.max_id) | (((long) update.channel_id) << 32)));
                    long dialog_id = (long) (-update.channel_id);
                    value = (Integer) this.dialogs_read_inbox_max.get(Long.valueOf(dialog_id));
                    if (value == null) {
                        value = Integer.valueOf(MessagesStorage.getInstance().getChannelReadInboxMax(update.channel_id));
                    }
                    this.dialogs_read_inbox_max.put(Long.valueOf(dialog_id), Integer.valueOf(Math.max(value.intValue(), update.max_id)));
                } else if (update instanceof TL_updateDeleteChannelMessages) {
                    arrayList3 = (ArrayList) deletedMessages.get(update.channel_id);
                    if (arrayList3 == null) {
                        arrayList3 = new ArrayList();
                        deletedMessages.put(update.channel_id, arrayList3);
                    }
                    arrayList3.addAll(update.messages);
                } else if (update instanceof TL_updateChannel) {
                    updatesOnMainThread.add(update);
                } else if (update instanceof TL_updateChannelMessageViews) {
                    SparseIntArray array = (SparseIntArray) channelViews.get(update.channel_id);
                    if (array == null) {
                        array = new SparseIntArray();
                        channelViews.put(update.channel_id, array);
                    }
                    array.put(update.id, update.views);
                } else if (update instanceof TL_updateChatParticipantAdmin) {
                    MessagesStorage.getInstance().updateChatInfo(update.chat_id, update.user_id, 2, update.is_admin ? 1 : 0, update.version);
                } else if (update instanceof TL_updateChatAdmins) {
                    updatesOnMainThread.add(update);
                } else if (update instanceof TL_updateStickerSets) {
                    updatesOnMainThread.add(update);
                } else if (update instanceof TL_updateStickerSetsOrder) {
                    updatesOnMainThread.add(update);
                } else if (update instanceof TL_updateNewStickerSet) {
                    updatesOnMainThread.add(update);
                }
            }
        }
        if (!messages.isEmpty()) {
            for (Entry<Long, ArrayList<MessageObject>> pair : messages.entrySet()) {
                if (updatePrintingUsersWithNewMessages(((Long) pair.getKey()).longValue(), (ArrayList) pair.getValue())) {
                    printChanged = true;
                }
            }
        }
        if (printChanged) {
            updatePrintingStrings();
        }
        int interfaceUpdateMaskFinal = interfaceUpdateMask;
        final boolean printChangedArg = printChanged;
        if (!contactsIds.isEmpty()) {
            ContactsController.getInstance().processContactsUpdates(contactsIds, usersDict);
        }
        if (!pushMessages.isEmpty()) {
            final ArrayList<MessageObject> arrayList5 = pushMessages;
            MessagesStorage.getInstance().getStorageQueue().postRunnable(new Runnable() {

                class C04991 implements Runnable {
                    C04991() {
                    }

                    public void run() {
                        NotificationsController.getInstance().processNewMessages(arrayList5, true);
                    }
                }

                public void run() {
                    AndroidUtilities.runOnUIThread(new C04991());
                }
            });
        }
        if (!messagesArr.isEmpty()) {
            MessagesStorage.getInstance().putMessages(messagesArr, true, true, false, MediaController.getInstance().getAutodownloadMask());
        }
        if (channelViews.size() != 0) {
            MessagesStorage.getInstance().putChannelViews(channelViews, true);
        }
        final int i;
        final ArrayList<Update> arrayList6;
        final HashMap<Long, WebPage> hashMap;
        final HashMap<Long, ArrayList<MessageObject>> hashMap2;
        if (channelsGroups.size() != 0) {
            i = interfaceUpdateMaskFinal;
            arrayList6 = updatesOnMainThread;
            hashMap = webPages;
            hashMap2 = messages;
            AndroidUtilities.runOnUIThread(new Runnable() {
                public void run() {
                    Iterator i$;
                    int updateMask = i;
                    if (!arrayList6.isEmpty()) {
                        ArrayList<User> dbUsers = new ArrayList();
                        ArrayList<User> dbUsersStatus = new ArrayList();
                        Editor editor = null;
                        for (int a = 0; a < arrayList6.size(); a++) {
                            Update update = (Update) arrayList6.get(a);
                            User toDbUser = new User();
                            toDbUser.id = update.user_id;
                            final User currentUser = MessagesController.this.getUser(Integer.valueOf(update.user_id));
                            if (update instanceof TL_updatePrivacy) {
                                if (update.key instanceof TL_privacyKeyStatusTimestamp) {
                                    ContactsController.getInstance().setPrivacyRules(update.rules);
                                }
                            } else if (update instanceof TL_updateUserStatus) {
                                if (update.status instanceof TL_userStatusRecently) {
                                    update.status.expires = -100;
                                } else if (update.status instanceof TL_userStatusLastWeek) {
                                    update.status.expires = -101;
                                } else if (update.status instanceof TL_userStatusLastMonth) {
                                    update.status.expires = -102;
                                }
                                if (currentUser != null) {
                                    currentUser.id = update.user_id;
                                    currentUser.status = update.status;
                                }
                                toDbUser.status = update.status;
                                dbUsersStatus.add(toDbUser);
                                if (update.user_id == UserConfig.getClientUserId()) {
                                    NotificationsController.getInstance().setLastOnlineFromOtherDevice(update.status.expires);
                                }
                            } else if (update instanceof TL_updateUserName) {
                                if (currentUser != null) {
                                    if (!UserObject.isContact(currentUser)) {
                                        currentUser.first_name = update.first_name;
                                        currentUser.last_name = update.last_name;
                                    }
                                    if (currentUser.username != null && currentUser.username.length() > 0) {
                                        MessagesController.this.usersByUsernames.remove(currentUser.username);
                                    }
                                    if (update.username != null && update.username.length() > 0) {
                                        MessagesController.this.usersByUsernames.put(update.username, currentUser);
                                    }
                                    currentUser.username = update.username;
                                }
                                toDbUser.first_name = update.first_name;
                                toDbUser.last_name = update.last_name;
                                toDbUser.username = update.username;
                                dbUsers.add(toDbUser);
                            } else if (update instanceof TL_updateUserPhoto) {
                                if (currentUser != null) {
                                    currentUser.photo = update.photo;
                                }
                                toDbUser.photo = update.photo;
                                dbUsers.add(toDbUser);
                            } else if (update instanceof TL_updateUserPhone) {
                                if (currentUser != null) {
                                    currentUser.phone = update.phone;
                                    Utilities.phoneBookQueue.postRunnable(new Runnable() {
                                        public void run() {
                                            ContactsController.getInstance().addContactToPhoneBook(currentUser, true);
                                        }
                                    });
                                }
                                toDbUser.phone = update.phone;
                                dbUsers.add(toDbUser);
                            } else if (update instanceof TL_updateNotifySettings) {
                                TL_updateNotifySettings updateNotifySettings = (TL_updateNotifySettings) update;
                                if ((update.notify_settings instanceof TL_peerNotifySettings) && (updateNotifySettings.peer instanceof TL_notifyPeer)) {
                                    long dialog_id;
                                    if (editor == null) {
                                        editor = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", 0).edit();
                                    }
                                    if (updateNotifySettings.peer.peer.user_id != 0) {
                                        dialog_id = (long) updateNotifySettings.peer.peer.user_id;
                                    } else if (updateNotifySettings.peer.peer.chat_id != 0) {
                                        dialog_id = (long) (-updateNotifySettings.peer.peer.chat_id);
                                    } else {
                                        dialog_id = (long) (-updateNotifySettings.peer.peer.channel_id);
                                    }
                                    dialog = (Dialog) MessagesController.this.dialogs_dict.get(Long.valueOf(dialog_id));
                                    if (dialog != null) {
                                        dialog.notify_settings = update.notify_settings;
                                    }
                                    if (update.notify_settings.mute_until > ConnectionsManager.getInstance().getCurrentTime()) {
                                        int until = 0;
                                        if (update.notify_settings.mute_until > ConnectionsManager.getInstance().getCurrentTime() + 31536000) {
                                            editor.putInt("notify2_" + dialog_id, 2);
                                            if (dialog != null) {
                                                dialog.notify_settings.mute_until = ConnectionsManager.DEFAULT_DATACENTER_ID;
                                            }
                                        } else {
                                            until = update.notify_settings.mute_until;
                                            editor.putInt("notify2_" + dialog_id, 3);
                                            editor.putInt("notifyuntil_" + dialog_id, update.notify_settings.mute_until);
                                            if (dialog != null) {
                                                dialog.notify_settings.mute_until = until;
                                            }
                                        }
                                        MessagesStorage.getInstance().setDialogFlags(dialog_id, (((long) until) << 32) | 1);
                                        NotificationsController.getInstance().removeNotificationsForDialog(dialog_id);
                                    } else {
                                        if (dialog != null) {
                                            dialog.notify_settings.mute_until = 0;
                                        }
                                        editor.remove("notify2_" + dialog_id);
                                        MessagesStorage.getInstance().setDialogFlags(dialog_id, 0);
                                    }
                                }
                            } else if (update instanceof TL_updateChannel) {
                                dialog = (Dialog) MessagesController.this.dialogs_dict.get(Long.valueOf(-((long) update.channel_id)));
                                Chat chat = MessagesController.this.getChat(Integer.valueOf(update.channel_id));
                                if (dialog == null && (chat instanceof TL_channel) && !chat.left) {
                                    final Update update2 = update;
                                    Utilities.stageQueue.postRunnable(new Runnable() {
                                        public void run() {
                                            MessagesController.this.getChannelDifference(update2.channel_id, 1);
                                        }
                                    });
                                } else if (chat.left && dialog != null) {
                                    MessagesController.this.deleteDialog(dialog.id, 0);
                                }
                                updateMask |= 8192;
                                MessagesController.this.loadFullChat(update.channel_id, 0, true);
                            } else if (update instanceof TL_updateChatAdmins) {
                                updateMask |= 16384;
                            } else if (update instanceof TL_updateStickerSets) {
                                StickersQuery.loadStickers(false, true);
                            } else if (update instanceof TL_updateStickerSetsOrder) {
                                StickersQuery.reorderStickers(update.order);
                            } else if (update instanceof TL_updateNewStickerSet) {
                                StickersQuery.addNewStickerSet(update.stickerset);
                            }
                        }
                        if (editor != null) {
                            editor.commit();
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.notificationsSettingsUpdated, new Object[0]);
                        }
                        MessagesStorage.getInstance().updateUsers(dbUsersStatus, true, true, true);
                        MessagesStorage.getInstance().updateUsers(dbUsers, false, true, true);
                    }
                    if (!hashMap.isEmpty()) {
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.didReceivedWebpagesInUpdates, hashMap);
                    }
                    if (!hashMap2.isEmpty()) {
                        for (Entry<Long, ArrayList<MessageObject>> entry : hashMap2.entrySet()) {
                            Long key = (Long) entry.getKey();
                            ArrayList<MessageObject> value = (ArrayList) entry.getValue();
                            MessagesController.this.updateInterfaceWithMessages(key.longValue(), value);
                        }
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
                    }
                    if (printChangedArg) {
                        updateMask |= 64;
                    }
                    if (!contactsIds.isEmpty()) {
                        updateMask = (updateMask | 1) | 128;
                    }
                    if (!chatInfoToUpdate.isEmpty()) {
                        i$ = chatInfoToUpdate.iterator();
                        while (i$.hasNext()) {
                            MessagesStorage.getInstance().updateChatParticipants((ChatParticipants) i$.next());
                        }
                    }
                    if (channelViews.size() != 0) {
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.didUpdatedMessagesViews, channelViews);
                    }
                    if (updateMask != 0) {
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.updateInterfaces, Integer.valueOf(updateMask));
                    }
                }
            });
            MessagesStorage.getInstance().getStorageQueue().postRunnable(new Runnable() {

                class C05031 implements Runnable {
                    C05031() {
                    }

                    public void run() {
                        int b;
                        int key;
                        MessageObject obj;
                        int updateMask = 0;
                        if (!(markAsReadMessagesInbox.size() == 0 && markAsReadMessagesOutbox.size() == 0)) {
                            Dialog dialog;
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.messagesRead, markAsReadMessagesInbox, markAsReadMessagesOutbox);
                            NotificationsController.getInstance().processReadMessages(markAsReadMessagesInbox, 0, 0, 0, false);
                            for (b = 0; b < markAsReadMessagesInbox.size(); b++) {
                                key = markAsReadMessagesInbox.keyAt(b);
                                long messageId = ((Long) markAsReadMessagesInbox.get(key)).longValue();
                                dialog = (Dialog) MessagesController.this.dialogs_dict.get(Long.valueOf((long) key));
                                if (dialog != null && dialog.top_message <= ((int) messageId)) {
                                    obj = (MessageObject) MessagesController.this.dialogMessage.get(Long.valueOf(dialog.id));
                                    if (obj != null) {
                                        obj.setIsRead();
                                        updateMask |= 256;
                                    }
                                }
                            }
                            for (b = 0; b < markAsReadMessagesOutbox.size(); b++) {
                                key = markAsReadMessagesOutbox.keyAt(b);
                                int messageId2 = markAsReadMessagesOutbox.get(key);
                                dialog = (Dialog) MessagesController.this.dialogs_dict.get(Long.valueOf((long) key));
                                if (dialog != null && dialog.top_message <= messageId2) {
                                    obj = (MessageObject) MessagesController.this.dialogMessage.get(Long.valueOf(dialog.id));
                                    if (obj != null) {
                                        obj.setIsRead();
                                        updateMask |= 256;
                                    }
                                }
                            }
                        }
                        if (!markAsReadEncrypted.isEmpty()) {
                            for (Entry<Integer, Integer> entry : markAsReadEncrypted.entrySet()) {
                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.messagesReadEncrypted, entry.getKey(), entry.getValue());
                                long dialog_id = ((long) ((Integer) entry.getKey()).intValue()) << 32;
                                if (((Dialog) MessagesController.this.dialogs_dict.get(Long.valueOf(dialog_id))) != null) {
                                    MessageObject message = (MessageObject) MessagesController.this.dialogMessage.get(Long.valueOf(dialog_id));
                                    if (message != null && message.messageOwner.date <= ((Integer) entry.getValue()).intValue()) {
                                        message.setIsRead();
                                        updateMask |= 256;
                                    }
                                }
                            }
                        }
                        if (!markAsReadMessages.isEmpty()) {
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.messagesReadContent, markAsReadMessages);
                        }
                        if (deletedMessages.size() != 0) {
                            for (int a = 0; a < deletedMessages.size(); a++) {
                                key = deletedMessages.keyAt(a);
                                ArrayList<Integer> arrayList = (ArrayList) deletedMessages.get(key);
                                if (arrayList != null) {
                                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.messagesDeleted, arrayList, Integer.valueOf(key));
                                    if (key == 0) {
                                        for (b = 0; b < arrayList.size(); b++) {
                                            obj = (MessageObject) MessagesController.this.dialogMessagesByIds.get((Integer) arrayList.get(b));
                                            if (obj != null) {
                                                obj.deleted = true;
                                            }
                                        }
                                    } else {
                                        obj = (MessageObject) MessagesController.this.dialogMessage.get(Long.valueOf((long) (-key)));
                                        if (obj != null) {
                                            for (b = 0; b < arrayList.size(); b++) {
                                                if (obj.getId() == ((Integer) arrayList.get(b)).intValue()) {
                                                    obj.deleted = true;
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            NotificationsController.getInstance().removeDeletedMessagesFromNotifications(deletedMessages);
                        }
                        if (updateMask != 0) {
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.updateInterfaces, Integer.valueOf(updateMask));
                        }
                    }
                }

                public void run() {
                    AndroidUtilities.runOnUIThread(new C05031());
                }
            });
        } else {
            i = interfaceUpdateMaskFinal;
            arrayList6 = updatesOnMainThread;
            hashMap = webPages;
            hashMap2 = messages;
            AndroidUtilities.runOnUIThread(/* anonymous class already generated */);
            MessagesStorage.getInstance().getStorageQueue().postRunnable(/* anonymous class already generated */);
        }
        if (!webPages.isEmpty()) {
            MessagesStorage.getInstance().putWebPages(webPages);
        }
        if (!(markAsReadMessagesInbox.size() == 0 && markAsReadMessagesOutbox.size() == 0 && markAsReadEncrypted.isEmpty())) {
            if (markAsReadMessagesInbox.size() != 0) {
                MessagesStorage.getInstance().updateDialogsWithReadMessages(markAsReadMessagesInbox, true);
            }
            MessagesStorage.getInstance().markMessagesAsRead(markAsReadMessagesInbox, markAsReadMessagesOutbox, markAsReadEncrypted, true);
        }
        if (!markAsReadMessages.isEmpty()) {
            MessagesStorage.getInstance().markMessagesContentAsRead(markAsReadMessages);
        }
        if (deletedMessages.size() != 0) {
            for (a = 0; a < deletedMessages.size(); a++) {
                int key = deletedMessages.keyAt(a);
                arrayList3 = (ArrayList) deletedMessages.get(key);
                MessagesStorage.getInstance().markMessagesAsDeleted(arrayList3, true, key);
                MessagesStorage.getInstance().updateDialogsWithDeletedMessages(arrayList3, true, key);
            }
        }
        if (!tasks.isEmpty()) {
            i$ = tasks.iterator();
            while (i$.hasNext()) {
                TL_updateEncryptedMessagesRead update2 = (TL_updateEncryptedMessagesRead) i$.next();
                MessagesStorage.getInstance().createTaskForSecretChat(update2.chat_id, update2.max_date, update2.date, 1, null);
            }
        }
        return true;
    }

    private boolean isNotifySettingsMuted(PeerNotifySettings settings) {
        return (settings instanceof TL_peerNotifySettings) && settings.mute_until > ConnectionsManager.getInstance().getCurrentTime();
    }

    public boolean isDialogMuted(long dialog_id) {
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", 0);
        int mute_type = preferences.getInt("notify2_" + dialog_id, 0);
        if (mute_type == 2) {
            return true;
        }
        if (mute_type != 3 || preferences.getInt("notifyuntil_" + dialog_id, 0) < ConnectionsManager.getInstance().getCurrentTime()) {
            return false;
        }
        return true;
    }

    private boolean updatePrintingUsersWithNewMessages(long uid, ArrayList<MessageObject> messages) {
        if (uid > 0) {
            if (((ArrayList) this.printingUsers.get(Long.valueOf(uid))) != null) {
                this.printingUsers.remove(Long.valueOf(uid));
                return true;
            }
        } else if (uid < 0) {
            ArrayList<Integer> messagesUsers = new ArrayList();
            Iterator i$ = messages.iterator();
            while (i$.hasNext()) {
                MessageObject message = (MessageObject) i$.next();
                if (!messagesUsers.contains(Integer.valueOf(message.messageOwner.from_id))) {
                    messagesUsers.add(Integer.valueOf(message.messageOwner.from_id));
                }
            }
            ArrayList<PrintingUser> arr = (ArrayList) this.printingUsers.get(Long.valueOf(uid));
            boolean changed = false;
            if (arr != null) {
                int a = 0;
                while (a < arr.size()) {
                    if (messagesUsers.contains(Integer.valueOf(((PrintingUser) arr.get(a)).userId))) {
                        arr.remove(a);
                        a--;
                        if (arr.isEmpty()) {
                            this.printingUsers.remove(Long.valueOf(uid));
                        }
                        changed = true;
                    }
                    a++;
                }
            }
            if (changed) {
                return true;
            }
        }
        return false;
    }

    protected void updateInterfaceWithMessages(long uid, ArrayList<MessageObject> messages) {
        updateInterfaceWithMessages(uid, messages, false);
    }

    protected void updateInterfaceWithMessages(long uid, ArrayList<MessageObject> messages, boolean isBroadcast) {
        if (messages != null && !messages.isEmpty()) {
            int a;
            boolean isEncryptedChat = ((int) uid) == 0;
            MessageObject lastMessage = null;
            int channelId = 0;
            for (a = 0; a < messages.size(); a++) {
                MessageObject message = (MessageObject) messages.get(a);
                if (lastMessage == null || ((!isEncryptedChat && message.getId() > lastMessage.getId()) || (((isEncryptedChat || (message.getId() < 0 && lastMessage.getId() < 0)) && message.getId() < lastMessage.getId()) || message.messageOwner.date > lastMessage.messageOwner.date))) {
                    if (message.messageOwner.to_id.channel_id == 0 || message.isMegagroup() || message.isImportant()) {
                        lastMessage = message;
                    }
                    if (message.messageOwner.to_id.channel_id != 0) {
                        channelId = message.messageOwner.to_id.channel_id;
                    }
                }
            }
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.didReceivedNewMessages, Long.valueOf(uid), messages);
            if (lastMessage != null) {
                Dialog dialog = (Dialog) this.dialogs_dict.get(Long.valueOf(uid));
                if (!(lastMessage.messageOwner.action instanceof TL_messageActionChatMigrateTo)) {
                    Chat chat;
                    boolean changed = false;
                    if (dialog == null) {
                        if (!isBroadcast) {
                            chat = getChat(Integer.valueOf(channelId));
                            if (chat == null || !chat.left) {
                                if (ChatObject.isChannel(chat)) {
                                    dialog = new TL_dialogChannel();
                                } else {
                                    dialog = new TL_dialog();
                                }
                                dialog.id = uid;
                                dialog.unread_count = 0;
                                dialog.top_message = lastMessage.getId();
                                dialog.last_message_date = lastMessage.messageOwner.date;
                                this.dialogs_dict.put(Long.valueOf(uid), dialog);
                                this.dialogs.add(dialog);
                                this.dialogMessage.put(Long.valueOf(uid), lastMessage);
                                if (lastMessage.messageOwner.to_id.channel_id == 0) {
                                    this.dialogMessagesByIds.put(Integer.valueOf(lastMessage.getId()), lastMessage);
                                }
                                this.nextDialogsCacheOffset++;
                                changed = true;
                            } else {
                                return;
                            }
                        }
                    } else if ((dialog.top_message > 0 && lastMessage.getId() > 0 && lastMessage.getId() > dialog.top_message) || ((dialog.top_message < 0 && lastMessage.getId() < 0 && lastMessage.getId() < dialog.top_message) || !this.dialogMessage.containsKey(Long.valueOf(uid)) || dialog.top_message < 0 || dialog.last_message_date <= lastMessage.messageOwner.date)) {
                        dialog.top_message = lastMessage.getId();
                        if (!isBroadcast) {
                            dialog.last_message_date = lastMessage.messageOwner.date;
                            changed = true;
                        }
                        this.dialogMessage.put(Long.valueOf(uid), lastMessage);
                        if (lastMessage.messageOwner.to_id.channel_id == 0) {
                            this.dialogMessagesByIds.put(Integer.valueOf(lastMessage.getId()), lastMessage);
                        }
                    }
                    if (changed) {
                        this.dialogsServerOnly.clear();
                        this.dialogsGroupsOnly.clear();
                        Collections.sort(this.dialogs, new Comparator<Dialog>() {
                            public int compare(Dialog tl_dialog, Dialog tl_dialog2) {
                                if (tl_dialog.last_message_date == tl_dialog2.last_message_date) {
                                    return 0;
                                }
                                if (tl_dialog.last_message_date < tl_dialog2.last_message_date) {
                                    return 1;
                                }
                                return -1;
                            }
                        });
                        for (a = 0; a < this.dialogs.size(); a++) {
                            Dialog d = (Dialog) this.dialogs.get(a);
                            int high_id = (int) (d.id >> 32);
                            if (!(((int) d.id) == 0 || high_id == 1)) {
                                this.dialogsServerOnly.add(d);
                                if (d instanceof TL_dialog) {
                                    if (d.id < 0) {
                                        this.dialogsGroupsOnly.add(d);
                                    }
                                } else if (d instanceof TL_dialogChannel) {
                                    chat = getChat(Integer.valueOf(-((int) d.id)));
                                    if (chat != null && ((chat.megagroup && chat.editor) || chat.creator)) {
                                        this.dialogsGroupsOnly.add(d);
                                    }
                                }
                            }
                        }
                    }
                } else if (dialog != null) {
                    this.dialogs.remove(dialog);
                    this.dialogsServerOnly.remove(dialog);
                    this.dialogsGroupsOnly.remove(dialog);
                    this.dialogs_dict.remove(Long.valueOf(dialog.id));
                    this.dialogs_read_inbox_max.remove(Long.valueOf(dialog.id));
                    this.nextDialogsCacheOffset--;
                    this.dialogMessage.remove(Long.valueOf(dialog.id));
                    this.dialogMessagesByIds.remove(Integer.valueOf(dialog.top_message));
                    dialog.top_message = 0;
                    NotificationsController.getInstance().removeNotificationsForDialog(dialog.id);
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.needReloadRecentDialogsSearch, new Object[0]);
                }
            }
        }
    }

    public static void openByUserName(String username, final BaseFragment fragment, final int type) {
        if (username != null && fragment != null) {
            User user = getInstance().getUser(username);
            if (user != null) {
                Bundle args = new Bundle();
                args.putInt("user_id", user.id);
                if (type == 0) {
                    fragment.presentFragment(new ProfileActivity(args));
                } else {
                    fragment.presentFragment(new ChatActivity(args));
                }
            } else if (fragment.getParentActivity() != null) {
                final ProgressDialog progressDialog = new ProgressDialog(fragment.getParentActivity());
                progressDialog.setMessage(LocaleController.getString("Loading", C0553R.string.Loading));
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.setCancelable(false);
                TL_contacts_resolveUsername req = new TL_contacts_resolveUsername();
                req.username = username;
                final int reqId = ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
                    public void run(final TLObject response, final TL_error error) {
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            public void run() {
                                try {
                                    progressDialog.dismiss();
                                } catch (Throwable e) {
                                    FileLog.m611e("tmessages", e);
                                }
                                fragment.setVisibleDialog(null);
                                if (error == null) {
                                    TL_contacts_resolvedPeer res = response;
                                    MessagesController.getInstance().putUsers(res.users, false);
                                    MessagesController.getInstance().putChats(res.chats, false);
                                    MessagesStorage.getInstance().putUsersAndChats(res.users, res.chats, false, true);
                                    Bundle args = new Bundle();
                                    if (res.chats.isEmpty()) {
                                        args.putInt("user_id", ((User) res.users.get(0)).id);
                                    } else {
                                        args.putInt("chat_id", ((Chat) res.chats.get(0)).id);
                                    }
                                    if (fragment == null) {
                                        return;
                                    }
                                    if (type == 0 && res.chats.isEmpty()) {
                                        fragment.presentFragment(new ProfileActivity(args));
                                    } else {
                                        fragment.presentFragment(new ChatActivity(args));
                                    }
                                } else if (fragment != null && fragment.getParentActivity() != null) {
                                    try {
                                        Toast.makeText(fragment.getParentActivity(), LocaleController.getString("NoUsernameFound", C0553R.string.NoUsernameFound), 0).show();
                                    } catch (Throwable e2) {
                                        FileLog.m611e("tmessages", e2);
                                    }
                                }
                            }
                        });
                    }
                });
                progressDialog.setButton(-2, LocaleController.getString("Cancel", C0553R.string.Cancel), new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ConnectionsManager.getInstance().cancelRequest(reqId, true);
                        try {
                            dialog.dismiss();
                        } catch (Throwable e) {
                            FileLog.m611e("tmessages", e);
                        }
                        if (fragment != null) {
                            fragment.setVisibleDialog(null);
                        }
                    }
                });
                fragment.setVisibleDialog(progressDialog);
                progressDialog.show();
            }
        }
    }
}
