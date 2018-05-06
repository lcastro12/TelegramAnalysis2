package org.telegram.messenger;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.support.v4.view.InputDeviceCompat;
import com.google.android.gms.games.GamesClient;
import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import net.hockeyapp.android.Strings;
import org.telegram.tgnet.AbstractSerializedData;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.NativeByteBuffer;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLClassStore;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.Audio;
import org.telegram.tgnet.TLRPC.DecryptedMessage;
import org.telegram.tgnet.TLRPC.DecryptedMessageAction;
import org.telegram.tgnet.TLRPC.Dialog;
import org.telegram.tgnet.TLRPC.Document;
import org.telegram.tgnet.TLRPC.EncryptedChat;
import org.telegram.tgnet.TLRPC.EncryptedFile;
import org.telegram.tgnet.TLRPC.EncryptedMessage;
import org.telegram.tgnet.TLRPC.InputEncryptedFile;
import org.telegram.tgnet.TLRPC.Message;
import org.telegram.tgnet.TLRPC.PhotoSize;
import org.telegram.tgnet.TLRPC.TL_audioEncrypted;
import org.telegram.tgnet.TLRPC.TL_decryptedMessage;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionAbortKey;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionAcceptKey;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionCommitKey;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionDeleteMessages;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionFlushHistory;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionNoop;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionNotifyLayer;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionReadMessages;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionRequestKey;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionResend;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionScreenshotMessages;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionSetMessageTTL;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageHolder;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageLayer;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageMediaAudio;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageMediaContact;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageMediaDocument;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageMediaEmpty;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageMediaExternalDocument;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageMediaGeoPoint;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageMediaPhoto;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageMediaVideo;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageService;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageService_old;
import org.telegram.tgnet.TLRPC.TL_dialog;
import org.telegram.tgnet.TLRPC.TL_document;
import org.telegram.tgnet.TLRPC.TL_documentAttributeFilename;
import org.telegram.tgnet.TLRPC.TL_documentEncrypted;
import org.telegram.tgnet.TLRPC.TL_encryptedChat;
import org.telegram.tgnet.TLRPC.TL_encryptedChatDiscarded;
import org.telegram.tgnet.TLRPC.TL_encryptedChatRequested;
import org.telegram.tgnet.TLRPC.TL_encryptedChatWaiting;
import org.telegram.tgnet.TLRPC.TL_encryptedFile;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_fileEncryptedLocation;
import org.telegram.tgnet.TLRPC.TL_fileLocationUnavailable;
import org.telegram.tgnet.TLRPC.TL_geoPoint;
import org.telegram.tgnet.TLRPC.TL_inputEncryptedChat;
import org.telegram.tgnet.TLRPC.TL_message;
import org.telegram.tgnet.TLRPC.TL_messageEncryptedAction;
import org.telegram.tgnet.TLRPC.TL_messageMediaAudio;
import org.telegram.tgnet.TLRPC.TL_messageMediaContact;
import org.telegram.tgnet.TLRPC.TL_messageMediaDocument;
import org.telegram.tgnet.TLRPC.TL_messageMediaEmpty;
import org.telegram.tgnet.TLRPC.TL_messageMediaGeo;
import org.telegram.tgnet.TLRPC.TL_messageMediaPhoto;
import org.telegram.tgnet.TLRPC.TL_messageMediaVideo;
import org.telegram.tgnet.TLRPC.TL_messageService;
import org.telegram.tgnet.TLRPC.TL_message_secret;
import org.telegram.tgnet.TLRPC.TL_messages_acceptEncryption;
import org.telegram.tgnet.TLRPC.TL_messages_dhConfig;
import org.telegram.tgnet.TLRPC.TL_messages_discardEncryption;
import org.telegram.tgnet.TLRPC.TL_messages_getDhConfig;
import org.telegram.tgnet.TLRPC.TL_messages_requestEncryption;
import org.telegram.tgnet.TLRPC.TL_messages_sendEncrypted;
import org.telegram.tgnet.TLRPC.TL_messages_sendEncryptedFile;
import org.telegram.tgnet.TLRPC.TL_messages_sendEncryptedService;
import org.telegram.tgnet.TLRPC.TL_peerUser;
import org.telegram.tgnet.TLRPC.TL_photo;
import org.telegram.tgnet.TLRPC.TL_photoCachedSize;
import org.telegram.tgnet.TLRPC.TL_photoSize;
import org.telegram.tgnet.TLRPC.TL_photoSizeEmpty;
import org.telegram.tgnet.TLRPC.TL_updateEncryption;
import org.telegram.tgnet.TLRPC.TL_videoEncrypted;
import org.telegram.tgnet.TLRPC.Update;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.Video;
import org.telegram.tgnet.TLRPC.messages_DhConfig;
import org.telegram.tgnet.TLRPC.messages_SentEncryptedMessage;

public class SecretChatHelper {
    public static final int CURRENT_SECRET_CHAT_LAYER = 23;
    private static volatile SecretChatHelper Instance = null;
    private HashMap<Integer, EncryptedChat> acceptingChats = new HashMap();
    public ArrayList<Update> delayedEncryptedChatUpdates = new ArrayList();
    private ArrayList<Long> pendingEncMessagesToDelete = new ArrayList();
    private HashMap<Integer, ArrayList<TL_decryptedMessageHolder>> secretHolesQueue = new HashMap();
    private ArrayList<Integer> sendingNotifyLayer = new ArrayList();
    private boolean startingSecretChat = false;

    class C05726 implements Comparator<TL_decryptedMessageHolder> {
        C05726() {
        }

        public int compare(TL_decryptedMessageHolder lhs, TL_decryptedMessageHolder rhs) {
            if (lhs.layer.out_seq_no > rhs.layer.out_seq_no) {
                return 1;
            }
            if (lhs.layer.out_seq_no < rhs.layer.out_seq_no) {
                return -1;
            }
            return 0;
        }
    }

    public static SecretChatHelper getInstance() {
        SecretChatHelper localInstance = Instance;
        if (localInstance == null) {
            synchronized (SecretChatHelper.class) {
                try {
                    localInstance = Instance;
                    if (localInstance == null) {
                        SecretChatHelper localInstance2 = new SecretChatHelper();
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

    public void cleanUp() {
        this.sendingNotifyLayer.clear();
        this.acceptingChats.clear();
        this.secretHolesQueue.clear();
        this.delayedEncryptedChatUpdates.clear();
        this.pendingEncMessagesToDelete.clear();
        this.startingSecretChat = false;
    }

    protected void processPendingEncMessages() {
        if (!this.pendingEncMessagesToDelete.isEmpty()) {
            MessagesStorage.getInstance().markMessagesAsDeletedByRandoms(new ArrayList(this.pendingEncMessagesToDelete));
            this.pendingEncMessagesToDelete.clear();
        }
    }

    private TL_messageService createServiceSecretMessage(EncryptedChat encryptedChat, DecryptedMessageAction decryptedMessage) {
        TL_messageService newMsg = new TL_messageService();
        newMsg.action = new TL_messageEncryptedAction();
        newMsg.action.encryptedAction = decryptedMessage;
        int newMessageId = UserConfig.getNewMessageId();
        newMsg.id = newMessageId;
        newMsg.local_id = newMessageId;
        newMsg.from_id = UserConfig.getClientUserId();
        newMsg.unread = true;
        newMsg.out = true;
        newMsg.flags = 256;
        newMsg.dialog_id = ((long) encryptedChat.id) << 32;
        newMsg.to_id = new TL_peerUser();
        newMsg.send_state = 1;
        if (encryptedChat.participant_id == UserConfig.getClientUserId()) {
            newMsg.to_id.user_id = encryptedChat.admin_id;
        } else {
            newMsg.to_id.user_id = encryptedChat.participant_id;
        }
        if ((decryptedMessage instanceof TL_decryptedMessageActionScreenshotMessages) || (decryptedMessage instanceof TL_decryptedMessageActionSetMessageTTL)) {
            newMsg.date = ConnectionsManager.getInstance().getCurrentTime();
        } else {
            newMsg.date = 0;
        }
        newMsg.random_id = SendMessagesHelper.getInstance().getNextRandomId();
        UserConfig.saveConfig(false);
        ArrayList<Message> arr = new ArrayList();
        arr.add(newMsg);
        MessagesStorage.getInstance().putMessages(arr, false, true, true, 0);
        return newMsg;
    }

    public void sendMessagesReadMessage(EncryptedChat encryptedChat, ArrayList<Long> random_ids, Message resendMessage) {
        if (encryptedChat instanceof TL_encryptedChat) {
            TL_decryptedMessageService reqSend;
            Message message;
            if (AndroidUtilities.getPeerLayerVersion(encryptedChat.layer) >= 17) {
                reqSend = new TL_decryptedMessageService();
            } else {
                reqSend = new TL_decryptedMessageService_old();
                reqSend.random_bytes = new byte[15];
                Utilities.random.nextBytes(reqSend.random_bytes);
            }
            if (resendMessage != null) {
                message = resendMessage;
                reqSend.action = message.action.encryptedAction;
            } else {
                reqSend.action = new TL_decryptedMessageActionReadMessages();
                reqSend.action.random_ids = random_ids;
                message = createServiceSecretMessage(encryptedChat, reqSend.action);
            }
            reqSend.random_id = message.random_id;
            performSendEncryptedRequest(reqSend, message, encryptedChat, null, null);
        }
    }

    protected void processUpdateEncryption(TL_updateEncryption update, ConcurrentHashMap<Integer, User> usersDict) {
        final EncryptedChat newChat = update.chat;
        long dialog_id = ((long) newChat.id) << 32;
        EncryptedChat existingChat = MessagesController.getInstance().getEncryptedChatDB(newChat.id);
        if ((newChat instanceof TL_encryptedChatRequested) && existingChat == null) {
            int user_id = newChat.participant_id;
            if (user_id == UserConfig.getClientUserId()) {
                user_id = newChat.admin_id;
            }
            User user = MessagesController.getInstance().getUser(Integer.valueOf(user_id));
            if (user == null) {
                user = (User) usersDict.get(Integer.valueOf(user_id));
            }
            newChat.user_id = user_id;
            final Dialog dialog = new TL_dialog();
            dialog.id = dialog_id;
            dialog.unread_count = 0;
            dialog.top_message = 0;
            dialog.last_message_date = update.date;
            AndroidUtilities.runOnUIThread(new Runnable() {

                class C05541 implements Comparator<Dialog> {
                    C05541() {
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
                    MessagesController.getInstance().dialogs_dict.put(Long.valueOf(dialog.id), dialog);
                    MessagesController.getInstance().dialogs.add(dialog);
                    MessagesController.getInstance().putEncryptedChat(newChat, false);
                    Collections.sort(MessagesController.getInstance().dialogs, new C05541());
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
                }
            });
            MessagesStorage.getInstance().putEncryptedChat(newChat, user, dialog);
            getInstance().acceptSecretChat(newChat);
        } else if (!(newChat instanceof TL_encryptedChat)) {
            final EncryptedChat exist = existingChat;
            if (exist != null) {
                newChat.user_id = exist.user_id;
                newChat.auth_key = exist.auth_key;
                newChat.key_create_date = exist.key_create_date;
                newChat.key_use_count_in = exist.key_use_count_in;
                newChat.key_use_count_out = exist.key_use_count_out;
                newChat.ttl = exist.ttl;
                newChat.seq_in = exist.seq_in;
                newChat.seq_out = exist.seq_out;
            }
            AndroidUtilities.runOnUIThread(new Runnable() {
                public void run() {
                    if (exist != null) {
                        MessagesController.getInstance().putEncryptedChat(newChat, false);
                    }
                    MessagesStorage.getInstance().updateEncryptedChat(newChat);
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.encryptedChatUpdated, newChat);
                }
            });
        } else if (existingChat != null && (existingChat instanceof TL_encryptedChatWaiting) && (existingChat.auth_key == null || existingChat.auth_key.length == 1)) {
            newChat.a_or_b = existingChat.a_or_b;
            newChat.user_id = existingChat.user_id;
            getInstance().processAcceptedSecretChat(newChat);
        } else if (existingChat == null && this.startingSecretChat) {
            this.delayedEncryptedChatUpdates.add(update);
        }
    }

    public void sendMessagesDeleteMessage(EncryptedChat encryptedChat, ArrayList<Long> random_ids, Message resendMessage) {
        if (encryptedChat instanceof TL_encryptedChat) {
            TL_decryptedMessageService reqSend;
            Message message;
            if (AndroidUtilities.getPeerLayerVersion(encryptedChat.layer) >= 17) {
                reqSend = new TL_decryptedMessageService();
            } else {
                reqSend = new TL_decryptedMessageService_old();
                reqSend.random_bytes = new byte[15];
                Utilities.random.nextBytes(reqSend.random_bytes);
            }
            if (resendMessage != null) {
                message = resendMessage;
                reqSend.action = message.action.encryptedAction;
            } else {
                reqSend.action = new TL_decryptedMessageActionDeleteMessages();
                reqSend.action.random_ids = random_ids;
                message = createServiceSecretMessage(encryptedChat, reqSend.action);
            }
            reqSend.random_id = message.random_id;
            performSendEncryptedRequest(reqSend, message, encryptedChat, null, null);
        }
    }

    public void sendClearHistoryMessage(EncryptedChat encryptedChat, Message resendMessage) {
        if (encryptedChat instanceof TL_encryptedChat) {
            TL_decryptedMessageService reqSend;
            Message message;
            if (AndroidUtilities.getPeerLayerVersion(encryptedChat.layer) >= 17) {
                reqSend = new TL_decryptedMessageService();
            } else {
                reqSend = new TL_decryptedMessageService_old();
                reqSend.random_bytes = new byte[15];
                Utilities.random.nextBytes(reqSend.random_bytes);
            }
            if (resendMessage != null) {
                message = resendMessage;
                reqSend.action = message.action.encryptedAction;
            } else {
                reqSend.action = new TL_decryptedMessageActionFlushHistory();
                message = createServiceSecretMessage(encryptedChat, reqSend.action);
            }
            reqSend.random_id = message.random_id;
            performSendEncryptedRequest(reqSend, message, encryptedChat, null, null);
        }
    }

    public void sendNotifyLayerMessage(EncryptedChat encryptedChat, Message resendMessage) {
        if ((encryptedChat instanceof TL_encryptedChat) && !this.sendingNotifyLayer.contains(Integer.valueOf(encryptedChat.id))) {
            TL_decryptedMessageService reqSend;
            Message message;
            this.sendingNotifyLayer.add(Integer.valueOf(encryptedChat.id));
            if (AndroidUtilities.getPeerLayerVersion(encryptedChat.layer) >= 17) {
                reqSend = new TL_decryptedMessageService();
            } else {
                reqSend = new TL_decryptedMessageService_old();
                reqSend.random_bytes = new byte[15];
                Utilities.random.nextBytes(reqSend.random_bytes);
            }
            if (resendMessage != null) {
                message = resendMessage;
                reqSend.action = message.action.encryptedAction;
            } else {
                reqSend.action = new TL_decryptedMessageActionNotifyLayer();
                reqSend.action.layer = 23;
                message = createServiceSecretMessage(encryptedChat, reqSend.action);
            }
            reqSend.random_id = message.random_id;
            performSendEncryptedRequest(reqSend, message, encryptedChat, null, null);
        }
    }

    public void sendRequestKeyMessage(EncryptedChat encryptedChat, Message resendMessage) {
        if (encryptedChat instanceof TL_encryptedChat) {
            TL_decryptedMessageService reqSend;
            Message message;
            if (AndroidUtilities.getPeerLayerVersion(encryptedChat.layer) >= 17) {
                reqSend = new TL_decryptedMessageService();
            } else {
                reqSend = new TL_decryptedMessageService_old();
                reqSend.random_bytes = new byte[15];
                Utilities.random.nextBytes(reqSend.random_bytes);
            }
            if (resendMessage != null) {
                message = resendMessage;
                reqSend.action = message.action.encryptedAction;
            } else {
                reqSend.action = new TL_decryptedMessageActionRequestKey();
                reqSend.action.exchange_id = encryptedChat.exchange_id;
                reqSend.action.g_a = encryptedChat.g_a;
                message = createServiceSecretMessage(encryptedChat, reqSend.action);
            }
            reqSend.random_id = message.random_id;
            performSendEncryptedRequest(reqSend, message, encryptedChat, null, null);
        }
    }

    public void sendAcceptKeyMessage(EncryptedChat encryptedChat, Message resendMessage) {
        if (encryptedChat instanceof TL_encryptedChat) {
            TL_decryptedMessageService reqSend;
            Message message;
            if (AndroidUtilities.getPeerLayerVersion(encryptedChat.layer) >= 17) {
                reqSend = new TL_decryptedMessageService();
            } else {
                reqSend = new TL_decryptedMessageService_old();
                reqSend.random_bytes = new byte[15];
                Utilities.random.nextBytes(reqSend.random_bytes);
            }
            if (resendMessage != null) {
                message = resendMessage;
                reqSend.action = message.action.encryptedAction;
            } else {
                reqSend.action = new TL_decryptedMessageActionAcceptKey();
                reqSend.action.exchange_id = encryptedChat.exchange_id;
                reqSend.action.key_fingerprint = encryptedChat.future_key_fingerprint;
                reqSend.action.g_b = encryptedChat.g_a_or_b;
                message = createServiceSecretMessage(encryptedChat, reqSend.action);
            }
            reqSend.random_id = message.random_id;
            performSendEncryptedRequest(reqSend, message, encryptedChat, null, null);
        }
    }

    public void sendCommitKeyMessage(EncryptedChat encryptedChat, Message resendMessage) {
        if (encryptedChat instanceof TL_encryptedChat) {
            TL_decryptedMessageService reqSend;
            Message message;
            if (AndroidUtilities.getPeerLayerVersion(encryptedChat.layer) >= 17) {
                reqSend = new TL_decryptedMessageService();
            } else {
                reqSend = new TL_decryptedMessageService_old();
                reqSend.random_bytes = new byte[15];
                Utilities.random.nextBytes(reqSend.random_bytes);
            }
            if (resendMessage != null) {
                message = resendMessage;
                reqSend.action = message.action.encryptedAction;
            } else {
                reqSend.action = new TL_decryptedMessageActionCommitKey();
                reqSend.action.exchange_id = encryptedChat.exchange_id;
                reqSend.action.key_fingerprint = encryptedChat.future_key_fingerprint;
                message = createServiceSecretMessage(encryptedChat, reqSend.action);
            }
            reqSend.random_id = message.random_id;
            performSendEncryptedRequest(reqSend, message, encryptedChat, null, null);
        }
    }

    public void sendAbortKeyMessage(EncryptedChat encryptedChat, Message resendMessage, long excange_id) {
        if (encryptedChat instanceof TL_encryptedChat) {
            TL_decryptedMessageService reqSend;
            Message message;
            if (AndroidUtilities.getPeerLayerVersion(encryptedChat.layer) >= 17) {
                reqSend = new TL_decryptedMessageService();
            } else {
                reqSend = new TL_decryptedMessageService_old();
                reqSend.random_bytes = new byte[15];
                Utilities.random.nextBytes(reqSend.random_bytes);
            }
            if (resendMessage != null) {
                message = resendMessage;
                reqSend.action = message.action.encryptedAction;
            } else {
                reqSend.action = new TL_decryptedMessageActionAbortKey();
                reqSend.action.exchange_id = excange_id;
                message = createServiceSecretMessage(encryptedChat, reqSend.action);
            }
            reqSend.random_id = message.random_id;
            performSendEncryptedRequest(reqSend, message, encryptedChat, null, null);
        }
    }

    public void sendNoopMessage(EncryptedChat encryptedChat, Message resendMessage) {
        if (encryptedChat instanceof TL_encryptedChat) {
            TL_decryptedMessageService reqSend;
            Message message;
            if (AndroidUtilities.getPeerLayerVersion(encryptedChat.layer) >= 17) {
                reqSend = new TL_decryptedMessageService();
            } else {
                reqSend = new TL_decryptedMessageService_old();
                reqSend.random_bytes = new byte[15];
                Utilities.random.nextBytes(reqSend.random_bytes);
            }
            if (resendMessage != null) {
                message = resendMessage;
                reqSend.action = message.action.encryptedAction;
            } else {
                reqSend.action = new TL_decryptedMessageActionNoop();
                message = createServiceSecretMessage(encryptedChat, reqSend.action);
            }
            reqSend.random_id = message.random_id;
            performSendEncryptedRequest(reqSend, message, encryptedChat, null, null);
        }
    }

    public void sendTTLMessage(EncryptedChat encryptedChat, Message resendMessage) {
        if (encryptedChat instanceof TL_encryptedChat) {
            TL_decryptedMessageService reqSend;
            Message message;
            if (AndroidUtilities.getPeerLayerVersion(encryptedChat.layer) >= 17) {
                reqSend = new TL_decryptedMessageService();
            } else {
                reqSend = new TL_decryptedMessageService_old();
                reqSend.random_bytes = new byte[15];
                Utilities.random.nextBytes(reqSend.random_bytes);
            }
            if (resendMessage != null) {
                message = resendMessage;
                reqSend.action = message.action.encryptedAction;
            } else {
                reqSend.action = new TL_decryptedMessageActionSetMessageTTL();
                reqSend.action.ttl_seconds = encryptedChat.ttl;
                message = createServiceSecretMessage(encryptedChat, reqSend.action);
                MessageObject newMsgObj = new MessageObject(message, null, false);
                newMsgObj.messageOwner.send_state = 1;
                ArrayList<MessageObject> objArr = new ArrayList();
                objArr.add(newMsgObj);
                MessagesController.getInstance().updateInterfaceWithMessages(message.dialog_id, objArr);
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
            }
            reqSend.random_id = message.random_id;
            performSendEncryptedRequest(reqSend, message, encryptedChat, null, null);
        }
    }

    public void sendScreenshotMessage(EncryptedChat encryptedChat, ArrayList<Long> random_ids, Message resendMessage) {
        if (encryptedChat instanceof TL_encryptedChat) {
            TL_decryptedMessageService reqSend;
            Message message;
            if (AndroidUtilities.getPeerLayerVersion(encryptedChat.layer) >= 17) {
                reqSend = new TL_decryptedMessageService();
            } else {
                reqSend = new TL_decryptedMessageService_old();
                reqSend.random_bytes = new byte[15];
                Utilities.random.nextBytes(reqSend.random_bytes);
            }
            if (resendMessage != null) {
                message = resendMessage;
                reqSend.action = message.action.encryptedAction;
            } else {
                reqSend.action = new TL_decryptedMessageActionScreenshotMessages();
                reqSend.action.random_ids = random_ids;
                message = createServiceSecretMessage(encryptedChat, reqSend.action);
                MessageObject newMsgObj = new MessageObject(message, null, false);
                newMsgObj.messageOwner.send_state = 1;
                ArrayList<MessageObject> objArr = new ArrayList();
                objArr.add(newMsgObj);
                MessagesController.getInstance().updateInterfaceWithMessages(message.dialog_id, objArr);
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
            }
            reqSend.random_id = message.random_id;
            performSendEncryptedRequest(reqSend, message, encryptedChat, null, null);
        }
    }

    private void processSentMessage(Message newMsg, EncryptedFile file, DecryptedMessage decryptedMessage, String originalPath) {
        if (file == null) {
            return;
        }
        String fileName;
        ArrayList<Message> arr;
        if ((newMsg.media instanceof TL_messageMediaPhoto) && newMsg.media.photo != null) {
            PhotoSize size = (PhotoSize) newMsg.media.photo.sizes.get(newMsg.media.photo.sizes.size() - 1);
            fileName = size.location.volume_id + "_" + size.location.local_id;
            size.location = new TL_fileEncryptedLocation();
            size.location.key = decryptedMessage.media.key;
            size.location.iv = decryptedMessage.media.iv;
            size.location.dc_id = file.dc_id;
            size.location.volume_id = file.id;
            size.location.secret = file.access_hash;
            size.location.local_id = file.key_fingerprint;
            String fileName2 = size.location.volume_id + "_" + size.location.local_id;
            new File(FileLoader.getInstance().getDirectory(4), fileName + ".jpg").renameTo(FileLoader.getPathToAttach(size));
            ImageLoader.getInstance().replaceImageInCache(fileName, fileName2, size.location);
            arr = new ArrayList();
            arr.add(newMsg);
            MessagesStorage.getInstance().putMessages(arr, false, true, false, 0);
        } else if ((newMsg.media instanceof TL_messageMediaVideo) && newMsg.media.video != null) {
            Video video = newMsg.media.video;
            newMsg.media.video = new TL_videoEncrypted();
            newMsg.media.video.duration = video.duration;
            newMsg.media.video.thumb = video.thumb;
            newMsg.media.video.dc_id = file.dc_id;
            newMsg.media.video.f145w = video.f145w;
            newMsg.media.video.f144h = video.f144h;
            newMsg.media.video.date = video.date;
            newMsg.media.caption = video.caption != null ? video.caption : "";
            newMsg.media.video.size = file.size;
            newMsg.media.video.id = file.id;
            newMsg.media.video.access_hash = file.access_hash;
            newMsg.media.video.key = decryptedMessage.media.key;
            newMsg.media.video.iv = decryptedMessage.media.iv;
            newMsg.media.video.mime_type = video.mime_type;
            newMsg.media.video.caption = video.caption != null ? video.caption : "";
            if (newMsg.attachPath != null && newMsg.attachPath.startsWith(FileLoader.getInstance().getDirectory(4).getAbsolutePath()) && new File(newMsg.attachPath).renameTo(FileLoader.getPathToAttach(newMsg.media.video))) {
                newMsg.attachPath = "";
            }
            arr = new ArrayList();
            arr.add(newMsg);
            MessagesStorage.getInstance().putMessages(arr, false, true, false, 0);
        } else if ((newMsg.media instanceof TL_messageMediaDocument) && newMsg.media.document != null) {
            Document document = newMsg.media.document;
            newMsg.media.document = new TL_documentEncrypted();
            newMsg.media.document.id = file.id;
            newMsg.media.document.access_hash = file.access_hash;
            newMsg.media.document.date = document.date;
            newMsg.media.document.attributes = document.attributes;
            newMsg.media.document.mime_type = document.mime_type;
            newMsg.media.document.size = file.size;
            newMsg.media.document.key = decryptedMessage.media.key;
            newMsg.media.document.iv = decryptedMessage.media.iv;
            newMsg.media.document.thumb = document.thumb;
            newMsg.media.document.dc_id = file.dc_id;
            if (newMsg.attachPath != null && newMsg.attachPath.startsWith(FileLoader.getInstance().getDirectory(4).getAbsolutePath()) && new File(newMsg.attachPath).renameTo(FileLoader.getPathToAttach(newMsg.media.document))) {
                newMsg.attachPath = "";
            }
            arr = new ArrayList();
            arr.add(newMsg);
            MessagesStorage.getInstance().putMessages(arr, false, true, false, 0);
        } else if ((newMsg.media instanceof TL_messageMediaAudio) && newMsg.media.audio != null) {
            Audio audio = newMsg.media.audio;
            newMsg.media.audio = new TL_audioEncrypted();
            newMsg.media.audio.id = file.id;
            newMsg.media.audio.access_hash = file.access_hash;
            newMsg.media.audio.user_id = audio.user_id;
            newMsg.media.audio.date = audio.date;
            newMsg.media.audio.duration = audio.duration;
            newMsg.media.audio.size = file.size;
            newMsg.media.audio.dc_id = file.dc_id;
            newMsg.media.audio.key = decryptedMessage.media.key;
            newMsg.media.audio.iv = decryptedMessage.media.iv;
            newMsg.media.audio.mime_type = audio.mime_type;
            fileName = audio.dc_id + "_" + audio.id + ".ogg";
            if (!fileName.equals(newMsg.media.audio.dc_id + "_" + newMsg.media.audio.id + ".ogg") && new File(FileLoader.getInstance().getDirectory(4), fileName).renameTo(FileLoader.getPathToAttach(newMsg.media.audio))) {
                newMsg.attachPath = "";
            }
            arr = new ArrayList();
            arr.add(newMsg);
            MessagesStorage.getInstance().putMessages(arr, false, true, false, 0);
        }
    }

    public static boolean isSecretVisibleMessage(Message message) {
        return (message.action instanceof TL_messageEncryptedAction) && ((message.action.encryptedAction instanceof TL_decryptedMessageActionScreenshotMessages) || (message.action.encryptedAction instanceof TL_decryptedMessageActionSetMessageTTL));
    }

    public static boolean isSecretInvisibleMessage(Message message) {
        return (!(message.action instanceof TL_messageEncryptedAction) || (message.action.encryptedAction instanceof TL_decryptedMessageActionScreenshotMessages) || (message.action.encryptedAction instanceof TL_decryptedMessageActionSetMessageTTL)) ? false : true;
    }

    protected void performSendEncryptedRequest(DecryptedMessage req, Message newMsgObj, EncryptedChat chat, InputEncryptedFile encryptedFile, String originalPath) {
        if (req != null && chat.auth_key != null && !(chat instanceof TL_encryptedChatRequested) && !(chat instanceof TL_encryptedChatWaiting)) {
            SendMessagesHelper.getInstance().putToSendingMessages(newMsgObj);
            final EncryptedChat encryptedChat = chat;
            final DecryptedMessage decryptedMessage = req;
            final Message message = newMsgObj;
            final InputEncryptedFile inputEncryptedFile = encryptedFile;
            final String str = originalPath;
            Utilities.stageQueue.postRunnable(new Runnable() {

                class C14611 implements RequestDelegate {

                    class C05662 implements Runnable {
                        C05662() {
                        }

                        public void run() {
                            message.send_state = 2;
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.messageSendError, Integer.valueOf(message.id));
                            SendMessagesHelper.getInstance().processSentMessage(message.id);
                            if (message.media instanceof TL_messageMediaVideo) {
                                SendMessagesHelper.getInstance().stopVideoService(message.attachPath);
                            }
                            SendMessagesHelper.getInstance().removeFromSendingMessages(message.id);
                        }
                    }

                    C14611() {
                    }

                    public void run(TLObject response, TL_error error) {
                        if (error == null && (decryptedMessage.action instanceof TL_decryptedMessageActionNotifyLayer)) {
                            EncryptedChat currentChat = MessagesController.getInstance().getEncryptedChat(Integer.valueOf(encryptedChat.id));
                            SecretChatHelper.this.sendingNotifyLayer.remove(Integer.valueOf(currentChat.id));
                            currentChat.layer = AndroidUtilities.setMyLayerVersion(currentChat.layer, 23);
                            MessagesStorage.getInstance().updateEncryptedChatLayer(currentChat);
                        }
                        if (message == null) {
                            return;
                        }
                        if (error == null) {
                            final String attachPath = message.attachPath;
                            final messages_SentEncryptedMessage res = (messages_SentEncryptedMessage) response;
                            if (SecretChatHelper.isSecretVisibleMessage(message)) {
                                message.date = res.date;
                            }
                            if (res.file instanceof TL_encryptedFile) {
                                SecretChatHelper.this.processSentMessage(message, res.file, decryptedMessage, str);
                            }
                            MessagesStorage.getInstance().getStorageQueue().postRunnable(new Runnable() {

                                class C05641 implements Runnable {
                                    C05641() {
                                    }

                                    public void run() {
                                        message.send_state = 0;
                                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.messageReceivedByServer, Integer.valueOf(message.id), Integer.valueOf(message.id), message, Long.valueOf(message.dialog_id));
                                        SendMessagesHelper.getInstance().processSentMessage(message.id);
                                        if (message.media instanceof TL_messageMediaVideo) {
                                            SendMessagesHelper.getInstance().stopVideoService(attachPath);
                                        }
                                        SendMessagesHelper.getInstance().removeFromSendingMessages(message.id);
                                    }
                                }

                                public void run() {
                                    if (SecretChatHelper.isSecretInvisibleMessage(message)) {
                                        res.date = 0;
                                    }
                                    MessagesStorage.getInstance().updateMessageStateAndId(message.random_id, Integer.valueOf(message.id), message.id, res.date, false, 0);
                                    AndroidUtilities.runOnUIThread(new C05641());
                                }
                            });
                            return;
                        }
                        MessagesStorage.getInstance().markMessageAsSendError(message);
                        AndroidUtilities.runOnUIThread(new C05662());
                    }
                }

                public void run() {
                    TLObject toEncryptObject;
                    TLObject reqToSend;
                    if (AndroidUtilities.getPeerLayerVersion(encryptedChat.layer) >= 17) {
                        TLObject layer = new TL_decryptedMessageLayer();
                        layer.layer = Math.min(Math.max(17, AndroidUtilities.getMyLayerVersion(encryptedChat.layer)), AndroidUtilities.getPeerLayerVersion(encryptedChat.layer));
                        layer.message = decryptedMessage;
                        layer.random_bytes = new byte[15];
                        Utilities.random.nextBytes(layer.random_bytes);
                        toEncryptObject = layer;
                        if (encryptedChat.seq_in == 0 && encryptedChat.seq_out == 0) {
                            if (encryptedChat.admin_id == UserConfig.getClientUserId()) {
                                encryptedChat.seq_out = 1;
                            } else {
                                encryptedChat.seq_in = 1;
                            }
                        }
                        if (message.seq_in == 0 && message.seq_out == 0) {
                            layer.in_seq_no = encryptedChat.seq_in;
                            layer.out_seq_no = encryptedChat.seq_out;
                            EncryptedChat encryptedChat = encryptedChat;
                            encryptedChat.seq_out += 2;
                            if (AndroidUtilities.getPeerLayerVersion(encryptedChat.layer) >= 20) {
                                if (encryptedChat.key_create_date == 0) {
                                    encryptedChat.key_create_date = ConnectionsManager.getInstance().getCurrentTime();
                                }
                                encryptedChat = encryptedChat;
                                encryptedChat.key_use_count_out = (short) (encryptedChat.key_use_count_out + 1);
                                if ((encryptedChat.key_use_count_out >= (short) 100 || encryptedChat.key_create_date < ConnectionsManager.getInstance().getCurrentTime() - 604800) && encryptedChat.exchange_id == 0 && encryptedChat.future_key_fingerprint == 0) {
                                    SecretChatHelper.this.requestNewSecretChatKey(encryptedChat);
                                }
                            }
                            MessagesStorage.getInstance().updateEncryptedChatSeq(encryptedChat);
                            if (message != null) {
                                message.seq_in = layer.in_seq_no;
                                message.seq_out = layer.out_seq_no;
                                MessagesStorage.getInstance().setMessageSeq(message.id, message.seq_in, message.seq_out);
                            }
                        } else {
                            layer.in_seq_no = message.seq_in;
                            layer.out_seq_no = message.seq_out;
                        }
                        FileLog.m609e("tmessages", decryptedMessage + " send message with in_seq = " + layer.in_seq_no + " out_seq = " + layer.out_seq_no);
                    } else {
                        toEncryptObject = decryptedMessage;
                    }
                    int len = toEncryptObject.getObjectSize();
                    AbstractSerializedData nativeByteBuffer = new NativeByteBuffer(len + 4);
                    nativeByteBuffer.writeInt32(len);
                    toEncryptObject.serializeToStream(nativeByteBuffer);
                    Object messageKeyFull = Utilities.computeSHA1(nativeByteBuffer.buffer);
                    Object messageKey = new byte[16];
                    if (messageKeyFull.length != 0) {
                        System.arraycopy(messageKeyFull, messageKeyFull.length - 16, messageKey, 0, 16);
                    }
                    MessageKeyData keyData = MessageKeyData.generateMessageKeyData(encryptedChat.auth_key, messageKey, false);
                    len = nativeByteBuffer.length();
                    int extraLen = len % 16 != 0 ? 16 - (len % 16) : 0;
                    NativeByteBuffer dataForEncryption = new NativeByteBuffer(len + extraLen);
                    nativeByteBuffer.position(0);
                    dataForEncryption.writeBytes((NativeByteBuffer) nativeByteBuffer);
                    if (extraLen != 0) {
                        byte[] b = new byte[extraLen];
                        Utilities.random.nextBytes(b);
                        dataForEncryption.writeBytes(b);
                    }
                    nativeByteBuffer.reuse();
                    Utilities.aesIgeEncryption(dataForEncryption.buffer, keyData.aesKey, keyData.aesIv, true, false, 0, dataForEncryption.limit());
                    NativeByteBuffer data = new NativeByteBuffer((messageKey.length + 8) + dataForEncryption.length());
                    dataForEncryption.position(0);
                    data.writeInt64(encryptedChat.key_fingerprint);
                    data.writeBytes((byte[]) messageKey);
                    data.writeBytes(dataForEncryption);
                    dataForEncryption.reuse();
                    data.position(0);
                    TLObject req2;
                    if (inputEncryptedFile != null) {
                        req2 = new TL_messages_sendEncryptedFile();
                        req2.data = data;
                        req2.random_id = decryptedMessage.random_id;
                        req2.peer = new TL_inputEncryptedChat();
                        req2.peer.chat_id = encryptedChat.id;
                        req2.peer.access_hash = encryptedChat.access_hash;
                        req2.file = inputEncryptedFile;
                        reqToSend = req2;
                    } else if (decryptedMessage instanceof TL_decryptedMessageService) {
                        req2 = new TL_messages_sendEncryptedService();
                        req2.data = data;
                        req2.random_id = decryptedMessage.random_id;
                        req2.peer = new TL_inputEncryptedChat();
                        req2.peer.chat_id = encryptedChat.id;
                        req2.peer.access_hash = encryptedChat.access_hash;
                        reqToSend = req2;
                    } else {
                        req2 = new TL_messages_sendEncrypted();
                        req2.data = data;
                        req2.random_id = decryptedMessage.random_id;
                        req2.peer = new TL_inputEncryptedChat();
                        req2.peer.chat_id = encryptedChat.id;
                        req2.peer.access_hash = encryptedChat.access_hash;
                        reqToSend = req2;
                    }
                    ConnectionsManager.getInstance().sendRequest(reqToSend, new C14611(), 64);
                }
            });
        }
    }

    public Message processDecryptedObject(EncryptedChat chat, EncryptedFile file, int date, long random_id, TLObject object, boolean new_key_used) {
        if (object != null) {
            int from_id = chat.admin_id;
            if (from_id == UserConfig.getClientUserId()) {
                from_id = chat.participant_id;
            }
            if (AndroidUtilities.getPeerLayerVersion(chat.layer) >= 20 && chat.exchange_id == 0 && chat.future_key_fingerprint == 0 && chat.key_use_count_in >= (short) 120) {
                requestNewSecretChatKey(chat);
            }
            if (chat.exchange_id == 0 && chat.future_key_fingerprint != 0 && !new_key_used) {
                chat.future_auth_key = new byte[256];
                chat.future_key_fingerprint = 0;
                MessagesStorage.getInstance().updateEncryptedChat(chat);
            } else if (chat.exchange_id != 0 && new_key_used) {
                chat.key_fingerprint = chat.future_key_fingerprint;
                chat.auth_key = chat.future_auth_key;
                chat.key_create_date = ConnectionsManager.getInstance().getCurrentTime();
                chat.future_auth_key = new byte[256];
                chat.future_key_fingerprint = 0;
                chat.key_use_count_in = (short) 0;
                chat.key_use_count_out = (short) 0;
                chat.exchange_id = 0;
                MessagesStorage.getInstance().updateEncryptedChat(chat);
            }
            int newMessageId;
            if (object instanceof TL_decryptedMessage) {
                TL_message newMessage;
                TL_decryptedMessage decryptedMessage = (TL_decryptedMessage) object;
                if (AndroidUtilities.getPeerLayerVersion(chat.layer) >= 17) {
                    newMessage = new TL_message_secret();
                    newMessage.ttl = decryptedMessage.ttl;
                } else {
                    newMessage = new TL_message();
                    newMessage.ttl = chat.ttl;
                }
                newMessage.message = decryptedMessage.message;
                newMessage.date = date;
                newMessageId = UserConfig.getNewMessageId();
                newMessage.id = newMessageId;
                newMessage.local_id = newMessageId;
                UserConfig.saveConfig(false);
                newMessage.from_id = from_id;
                newMessage.to_id = new TL_peerUser();
                newMessage.random_id = random_id;
                newMessage.to_id.user_id = UserConfig.getClientUserId();
                newMessage.unread = true;
                newMessage.flags = Strings.EXPIRY_INFO_TITLE_ID;
                newMessage.dialog_id = ((long) chat.id) << 32;
                if (decryptedMessage.media instanceof TL_decryptedMessageMediaEmpty) {
                    newMessage.media = new TL_messageMediaEmpty();
                    return newMessage;
                } else if (decryptedMessage.media instanceof TL_decryptedMessageMediaContact) {
                    newMessage.media = new TL_messageMediaContact();
                    newMessage.media.last_name = decryptedMessage.media.last_name;
                    newMessage.media.first_name = decryptedMessage.media.first_name;
                    newMessage.media.phone_number = decryptedMessage.media.phone_number;
                    newMessage.media.user_id = decryptedMessage.media.user_id;
                    return newMessage;
                } else if (decryptedMessage.media instanceof TL_decryptedMessageMediaGeoPoint) {
                    newMessage.media = new TL_messageMediaGeo();
                    newMessage.media.geo = new TL_geoPoint();
                    newMessage.media.geo.lat = decryptedMessage.media.lat;
                    newMessage.media.geo._long = decryptedMessage.media._long;
                    return newMessage;
                } else if (decryptedMessage.media instanceof TL_decryptedMessageMediaPhoto) {
                    if (decryptedMessage.media.key == null || decryptedMessage.media.key.length != 32 || decryptedMessage.media.iv == null || decryptedMessage.media.iv.length != 32) {
                        return null;
                    }
                    newMessage.media = new TL_messageMediaPhoto();
                    newMessage.media.caption = "";
                    newMessage.media.photo = new TL_photo();
                    newMessage.media.photo.date = newMessage.date;
                    thumb = ((TL_decryptedMessageMediaPhoto) decryptedMessage.media).thumb;
                    if (thumb != null && thumb.length != 0 && thumb.length <= GamesClient.STATUS_MULTIPLAYER_ERROR_CREATION_NOT_ALLOWED && decryptedMessage.media.thumb_w <= 100 && decryptedMessage.media.thumb_h <= 100) {
                        TL_photoCachedSize small = new TL_photoCachedSize();
                        small.w = decryptedMessage.media.thumb_w;
                        small.h = decryptedMessage.media.thumb_h;
                        small.bytes = thumb;
                        small.type = "s";
                        small.location = new TL_fileLocationUnavailable();
                        newMessage.media.photo.sizes.add(small);
                    }
                    TL_photoSize big = new TL_photoSize();
                    big.w = decryptedMessage.media.f133w;
                    big.h = decryptedMessage.media.f132h;
                    big.type = "x";
                    big.size = file.size;
                    big.location = new TL_fileEncryptedLocation();
                    big.location.key = decryptedMessage.media.key;
                    big.location.iv = decryptedMessage.media.iv;
                    big.location.dc_id = file.dc_id;
                    big.location.volume_id = file.id;
                    big.location.secret = file.access_hash;
                    big.location.local_id = file.key_fingerprint;
                    newMessage.media.photo.sizes.add(big);
                    return newMessage;
                } else if (decryptedMessage.media instanceof TL_decryptedMessageMediaVideo) {
                    if (decryptedMessage.media.key == null || decryptedMessage.media.key.length != 32 || decryptedMessage.media.iv == null || decryptedMessage.media.iv.length != 32) {
                        return null;
                    }
                    newMessage.media = new TL_messageMediaVideo();
                    newMessage.media.caption = "";
                    newMessage.media.video = new TL_videoEncrypted();
                    thumb = ((TL_decryptedMessageMediaVideo) decryptedMessage.media).thumb;
                    if (thumb == null || thumb.length == 0 || thumb.length > GamesClient.STATUS_MULTIPLAYER_ERROR_CREATION_NOT_ALLOWED || decryptedMessage.media.thumb_w > 100 || decryptedMessage.media.thumb_h > 100) {
                        newMessage.media.video.thumb = new TL_photoSizeEmpty();
                        newMessage.media.video.thumb.type = "s";
                    } else {
                        newMessage.media.video.thumb = new TL_photoCachedSize();
                        newMessage.media.video.thumb.bytes = thumb;
                        newMessage.media.video.thumb.f139w = decryptedMessage.media.thumb_w;
                        newMessage.media.video.thumb.f138h = decryptedMessage.media.thumb_h;
                        newMessage.media.video.thumb.type = "s";
                        newMessage.media.video.thumb.location = new TL_fileLocationUnavailable();
                    }
                    newMessage.media.video.duration = decryptedMessage.media.duration;
                    newMessage.media.video.dc_id = file.dc_id;
                    newMessage.media.video.f145w = decryptedMessage.media.f133w;
                    newMessage.media.video.f144h = decryptedMessage.media.f132h;
                    newMessage.media.video.date = date;
                    newMessage.media.video.size = file.size;
                    newMessage.media.video.id = file.id;
                    newMessage.media.video.access_hash = file.access_hash;
                    newMessage.media.video.key = decryptedMessage.media.key;
                    newMessage.media.video.iv = decryptedMessage.media.iv;
                    newMessage.media.video.mime_type = decryptedMessage.media.mime_type;
                    newMessage.media.video.caption = "";
                    if (newMessage.ttl != 0) {
                        newMessage.ttl = Math.max(newMessage.media.video.duration + 1, newMessage.ttl);
                    }
                    if (newMessage.media.video.mime_type != null) {
                        return newMessage;
                    }
                    newMessage.media.video.mime_type = "video/mp4";
                    return newMessage;
                } else if (decryptedMessage.media instanceof TL_decryptedMessageMediaDocument) {
                    if (decryptedMessage.media.key == null || decryptedMessage.media.key.length != 32 || decryptedMessage.media.iv == null || decryptedMessage.media.iv.length != 32) {
                        return null;
                    }
                    newMessage.media = new TL_messageMediaDocument();
                    newMessage.media.document = new TL_documentEncrypted();
                    newMessage.media.document.id = file.id;
                    newMessage.media.document.access_hash = file.access_hash;
                    newMessage.media.document.date = date;
                    TL_documentAttributeFilename fileName = new TL_documentAttributeFilename();
                    fileName.file_name = decryptedMessage.media.file_name;
                    newMessage.media.document.attributes.add(fileName);
                    newMessage.media.document.mime_type = decryptedMessage.media.mime_type;
                    newMessage.media.document.size = file.size;
                    newMessage.media.document.key = decryptedMessage.media.key;
                    newMessage.media.document.iv = decryptedMessage.media.iv;
                    if (newMessage.media.document.mime_type == null) {
                        newMessage.media.document.mime_type = "";
                    }
                    thumb = ((TL_decryptedMessageMediaDocument) decryptedMessage.media).thumb;
                    if (thumb == null || thumb.length == 0 || thumb.length > GamesClient.STATUS_MULTIPLAYER_ERROR_CREATION_NOT_ALLOWED || decryptedMessage.media.thumb_w > 100 || decryptedMessage.media.thumb_h > 100) {
                        newMessage.media.document.thumb = new TL_photoSizeEmpty();
                        newMessage.media.document.thumb.type = "s";
                    } else {
                        newMessage.media.document.thumb = new TL_photoCachedSize();
                        newMessage.media.document.thumb.bytes = thumb;
                        newMessage.media.document.thumb.f139w = decryptedMessage.media.thumb_w;
                        newMessage.media.document.thumb.f138h = decryptedMessage.media.thumb_h;
                        newMessage.media.document.thumb.type = "s";
                        newMessage.media.document.thumb.location = new TL_fileLocationUnavailable();
                    }
                    newMessage.media.document.dc_id = file.dc_id;
                    return newMessage;
                } else if (decryptedMessage.media instanceof TL_decryptedMessageMediaExternalDocument) {
                    newMessage.media = new TL_messageMediaDocument();
                    newMessage.media.document = new TL_document();
                    newMessage.media.document.id = decryptedMessage.media.id;
                    newMessage.media.document.access_hash = decryptedMessage.media.access_hash;
                    newMessage.media.document.date = decryptedMessage.media.date;
                    newMessage.media.document.attributes = decryptedMessage.media.attributes;
                    newMessage.media.document.mime_type = decryptedMessage.media.mime_type;
                    newMessage.media.document.dc_id = decryptedMessage.media.dc_id;
                    newMessage.media.document.size = decryptedMessage.media.size;
                    newMessage.media.document.thumb = ((TL_decryptedMessageMediaExternalDocument) decryptedMessage.media).thumb;
                    if (newMessage.media.document.mime_type != null) {
                        return newMessage;
                    }
                    newMessage.media.document.mime_type = "";
                    return newMessage;
                } else if (!(decryptedMessage.media instanceof TL_decryptedMessageMediaAudio)) {
                    return null;
                } else {
                    if (decryptedMessage.media.key == null || decryptedMessage.media.key.length != 32 || decryptedMessage.media.iv == null || decryptedMessage.media.iv.length != 32) {
                        return null;
                    }
                    newMessage.media = new TL_messageMediaAudio();
                    newMessage.media.audio = new TL_audioEncrypted();
                    newMessage.media.audio.id = file.id;
                    newMessage.media.audio.access_hash = file.access_hash;
                    newMessage.media.audio.user_id = from_id;
                    newMessage.media.audio.date = date;
                    newMessage.media.audio.size = file.size;
                    newMessage.media.audio.key = decryptedMessage.media.key;
                    newMessage.media.audio.iv = decryptedMessage.media.iv;
                    newMessage.media.audio.dc_id = file.dc_id;
                    newMessage.media.audio.duration = decryptedMessage.media.duration;
                    newMessage.media.audio.mime_type = decryptedMessage.media.mime_type;
                    if (newMessage.ttl != 0) {
                        newMessage.ttl = Math.max(newMessage.media.audio.duration + 1, newMessage.ttl);
                    }
                    if (newMessage.media.audio.mime_type != null) {
                        return newMessage;
                    }
                    newMessage.media.audio.mime_type = "audio/ogg";
                    return newMessage;
                }
            } else if (object instanceof TL_decryptedMessageService) {
                TL_decryptedMessageService serviceMessage = (TL_decryptedMessageService) object;
                if ((serviceMessage.action instanceof TL_decryptedMessageActionSetMessageTTL) || (serviceMessage.action instanceof TL_decryptedMessageActionScreenshotMessages)) {
                    Message newMessage2 = new TL_messageService();
                    if (serviceMessage.action instanceof TL_decryptedMessageActionSetMessageTTL) {
                        newMessage2.action = new TL_messageEncryptedAction();
                        if (serviceMessage.action.ttl_seconds < 0 || serviceMessage.action.ttl_seconds > 31536000) {
                            serviceMessage.action.ttl_seconds = 31536000;
                        }
                        chat.ttl = serviceMessage.action.ttl_seconds;
                        newMessage2.action.encryptedAction = serviceMessage.action;
                        MessagesStorage.getInstance().updateEncryptedChatTTL(chat);
                    } else if (serviceMessage.action instanceof TL_decryptedMessageActionScreenshotMessages) {
                        newMessage2.action = new TL_messageEncryptedAction();
                        newMessage2.action.encryptedAction = serviceMessage.action;
                    }
                    newMessageId = UserConfig.getNewMessageId();
                    newMessage2.id = newMessageId;
                    newMessage2.local_id = newMessageId;
                    UserConfig.saveConfig(false);
                    newMessage2.unread = true;
                    newMessage2.flags = 256;
                    newMessage2.date = date;
                    newMessage2.from_id = from_id;
                    newMessage2.to_id = new TL_peerUser();
                    newMessage2.to_id.user_id = UserConfig.getClientUserId();
                    newMessage2.dialog_id = ((long) chat.id) << 32;
                    return newMessage2;
                } else if (serviceMessage.action instanceof TL_decryptedMessageActionFlushHistory) {
                    final long j = ((long) chat.id) << 32;
                    AndroidUtilities.runOnUIThread(new Runnable() {

                        class C05691 implements Runnable {

                            class C05681 implements Runnable {
                                C05681() {
                                }

                                public void run() {
                                    NotificationsController.getInstance().processReadMessages(null, j, 0, ConnectionsManager.DEFAULT_DATACENTER_ID, false);
                                    HashMap<Long, Integer> dialogsToUpdate = new HashMap();
                                    dialogsToUpdate.put(Long.valueOf(j), Integer.valueOf(0));
                                    NotificationsController.getInstance().processDialogsUpdateRead(dialogsToUpdate);
                                }
                            }

                            C05691() {
                            }

                            public void run() {
                                AndroidUtilities.runOnUIThread(new C05681());
                            }
                        }

                        public void run() {
                            Dialog dialog = (Dialog) MessagesController.getInstance().dialogs_dict.get(Long.valueOf(j));
                            if (dialog != null) {
                                dialog.unread_count = 0;
                                MessagesController.getInstance().dialogMessage.remove(Long.valueOf(dialog.id));
                            }
                            MessagesStorage.getInstance().getStorageQueue().postRunnable(new C05691());
                            MessagesStorage.getInstance().deleteDialog(j, 1);
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.removeAllMessagesFromDialog, Long.valueOf(j), Boolean.valueOf(false));
                        }
                    });
                    return null;
                } else if (serviceMessage.action instanceof TL_decryptedMessageActionDeleteMessages) {
                    if (!serviceMessage.action.random_ids.isEmpty()) {
                        this.pendingEncMessagesToDelete.addAll(serviceMessage.action.random_ids);
                    }
                    return null;
                } else if (serviceMessage.action instanceof TL_decryptedMessageActionReadMessages) {
                    if (!serviceMessage.action.random_ids.isEmpty()) {
                        MessagesStorage.getInstance().createTaskForSecretChat(chat.id, ConnectionsManager.getInstance().getCurrentTime(), ConnectionsManager.getInstance().getCurrentTime(), 1, serviceMessage.action.random_ids);
                    }
                } else if (serviceMessage.action instanceof TL_decryptedMessageActionNotifyLayer) {
                    int currentPeerLayer = AndroidUtilities.getPeerLayerVersion(chat.layer);
                    chat.layer = AndroidUtilities.setPeerLayerVersion(chat.layer, serviceMessage.action.layer);
                    MessagesStorage.getInstance().updateEncryptedChatLayer(chat);
                    if (currentPeerLayer < 23) {
                        sendNotifyLayerMessage(chat, null);
                    }
                } else if (serviceMessage.action instanceof TL_decryptedMessageActionRequestKey) {
                    if (chat.exchange_id != 0) {
                        if (chat.exchange_id > serviceMessage.action.exchange_id) {
                            FileLog.m609e("tmessages", "we already have request key with higher exchange_id");
                            return null;
                        }
                        sendAbortKeyMessage(chat, null, chat.exchange_id);
                    }
                    byte[] salt = new byte[256];
                    Utilities.random.nextBytes(salt);
                    r0 = new BigInteger(1, MessagesStorage.secretPBytes);
                    BigInteger g_b = BigInteger.valueOf((long) MessagesStorage.secretG).modPow(new BigInteger(1, salt), r0);
                    r0 = new BigInteger(1, serviceMessage.action.g_a);
                    if (Utilities.isGoodGaAndGb(r0, r0)) {
                        byte[] g_b_bytes = g_b.toByteArray();
                        if (g_b_bytes.length > 256) {
                            correctedAuth = new byte[256];
                            System.arraycopy(g_b_bytes, 1, correctedAuth, 0, 256);
                            g_b_bytes = correctedAuth;
                        }
                        authKey = r0.modPow(new BigInteger(1, salt), r0).toByteArray();
                        if (authKey.length > 256) {
                            correctedAuth = new byte[256];
                            System.arraycopy(authKey, authKey.length + InputDeviceCompat.SOURCE_ANY, correctedAuth, 0, 256);
                            authKey = correctedAuth;
                        } else if (authKey.length < 256) {
                            correctedAuth = new byte[256];
                            System.arraycopy(authKey, 0, correctedAuth, 256 - authKey.length, authKey.length);
                            for (a = 0; a < 256 - authKey.length; a++) {
                                authKey[a] = (byte) 0;
                            }
                            authKey = correctedAuth;
                        }
                        authKeyHash = Utilities.computeSHA1(authKey);
                        authKeyId = new byte[8];
                        System.arraycopy(authKeyHash, authKeyHash.length - 8, authKeyId, 0, 8);
                        chat.exchange_id = serviceMessage.action.exchange_id;
                        chat.future_auth_key = authKey;
                        chat.future_key_fingerprint = Utilities.bytesToLong(authKeyId);
                        chat.g_a_or_b = g_b_bytes;
                        MessagesStorage.getInstance().updateEncryptedChat(chat);
                        sendAcceptKeyMessage(chat, null);
                    } else {
                        sendAbortKeyMessage(chat, null, serviceMessage.action.exchange_id);
                        return null;
                    }
                } else if (serviceMessage.action instanceof TL_decryptedMessageActionAcceptKey) {
                    if (chat.exchange_id == serviceMessage.action.exchange_id) {
                        r0 = new BigInteger(1, MessagesStorage.secretPBytes);
                        r0 = new BigInteger(1, serviceMessage.action.g_b);
                        if (Utilities.isGoodGaAndGb(r0, r0)) {
                            authKey = r0.modPow(new BigInteger(1, chat.a_or_b), r0).toByteArray();
                            if (authKey.length > 256) {
                                correctedAuth = new byte[256];
                                System.arraycopy(authKey, authKey.length + InputDeviceCompat.SOURCE_ANY, correctedAuth, 0, 256);
                                authKey = correctedAuth;
                            } else if (authKey.length < 256) {
                                correctedAuth = new byte[256];
                                System.arraycopy(authKey, 0, correctedAuth, 256 - authKey.length, authKey.length);
                                for (a = 0; a < 256 - authKey.length; a++) {
                                    authKey[a] = (byte) 0;
                                }
                                authKey = correctedAuth;
                            }
                            authKeyHash = Utilities.computeSHA1(authKey);
                            authKeyId = new byte[8];
                            System.arraycopy(authKeyHash, authKeyHash.length - 8, authKeyId, 0, 8);
                            long fingerprint = Utilities.bytesToLong(authKeyId);
                            if (serviceMessage.action.key_fingerprint == fingerprint) {
                                chat.future_auth_key = authKey;
                                chat.future_key_fingerprint = fingerprint;
                                MessagesStorage.getInstance().updateEncryptedChat(chat);
                                sendCommitKeyMessage(chat, null);
                            } else {
                                chat.future_auth_key = new byte[256];
                                chat.future_key_fingerprint = 0;
                                chat.exchange_id = 0;
                                MessagesStorage.getInstance().updateEncryptedChat(chat);
                                sendAbortKeyMessage(chat, null, serviceMessage.action.exchange_id);
                            }
                        } else {
                            chat.future_auth_key = new byte[256];
                            chat.future_key_fingerprint = 0;
                            chat.exchange_id = 0;
                            MessagesStorage.getInstance().updateEncryptedChat(chat);
                            sendAbortKeyMessage(chat, null, serviceMessage.action.exchange_id);
                            return null;
                        }
                    }
                    chat.future_auth_key = new byte[256];
                    chat.future_key_fingerprint = 0;
                    chat.exchange_id = 0;
                    MessagesStorage.getInstance().updateEncryptedChat(chat);
                    sendAbortKeyMessage(chat, null, serviceMessage.action.exchange_id);
                } else if (serviceMessage.action instanceof TL_decryptedMessageActionCommitKey) {
                    if (chat.exchange_id == serviceMessage.action.exchange_id && chat.future_key_fingerprint == serviceMessage.action.key_fingerprint) {
                        long old_fingerpring = chat.key_fingerprint;
                        byte[] old_key = chat.auth_key;
                        chat.key_fingerprint = chat.future_key_fingerprint;
                        chat.auth_key = chat.future_auth_key;
                        chat.key_create_date = ConnectionsManager.getInstance().getCurrentTime();
                        chat.future_auth_key = old_key;
                        chat.future_key_fingerprint = old_fingerpring;
                        chat.key_use_count_in = (short) 0;
                        chat.key_use_count_out = (short) 0;
                        chat.exchange_id = 0;
                        MessagesStorage.getInstance().updateEncryptedChat(chat);
                        sendNoopMessage(chat, null);
                    } else {
                        chat.future_auth_key = new byte[256];
                        chat.future_key_fingerprint = 0;
                        chat.exchange_id = 0;
                        MessagesStorage.getInstance().updateEncryptedChat(chat);
                        sendAbortKeyMessage(chat, null, serviceMessage.action.exchange_id);
                    }
                } else if (serviceMessage.action instanceof TL_decryptedMessageActionAbortKey) {
                    if (chat.exchange_id == serviceMessage.action.exchange_id) {
                        chat.future_auth_key = new byte[256];
                        chat.future_key_fingerprint = 0;
                        chat.exchange_id = 0;
                        MessagesStorage.getInstance().updateEncryptedChat(chat);
                    }
                } else if (!(serviceMessage.action instanceof TL_decryptedMessageActionNoop)) {
                    if (!(serviceMessage.action instanceof TL_decryptedMessageActionResend)) {
                        return null;
                    }
                    EncryptedChat newChat = new TL_encryptedChatDiscarded();
                    newChat.id = chat.id;
                    newChat.user_id = chat.user_id;
                    newChat.auth_key = chat.auth_key;
                    newChat.key_create_date = chat.key_create_date;
                    newChat.key_use_count_in = chat.key_use_count_in;
                    newChat.key_use_count_out = chat.key_use_count_out;
                    newChat.seq_in = chat.seq_in;
                    newChat.seq_out = chat.seq_out;
                    MessagesStorage.getInstance().updateEncryptedChat(newChat);
                    final EncryptedChat encryptedChat = newChat;
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        public void run() {
                            MessagesController.getInstance().putEncryptedChat(encryptedChat, false);
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.encryptedChatUpdated, encryptedChat);
                        }
                    });
                    declineSecretChat(chat.id);
                }
            } else {
                FileLog.m609e("tmessages", "unknown message " + object);
            }
        } else {
            FileLog.m609e("tmessages", "unknown TLObject");
        }
        return null;
    }

    public void checkSecretHoles(EncryptedChat chat, ArrayList<Message> messages) {
        ArrayList<TL_decryptedMessageHolder> holes = (ArrayList) this.secretHolesQueue.get(Integer.valueOf(chat.id));
        if (holes != null) {
            Collections.sort(holes, new C05726());
            boolean update = false;
            int a = 0;
            while (holes.size() > 0) {
                TL_decryptedMessageHolder holder = (TL_decryptedMessageHolder) holes.get(a);
                if (holder.layer.out_seq_no != chat.seq_in && chat.seq_in != holder.layer.out_seq_no - 2) {
                    break;
                }
                chat.seq_in = holder.layer.out_seq_no;
                holes.remove(a);
                a--;
                update = true;
                Message message = processDecryptedObject(chat, holder.file, holder.date, holder.random_id, holder.layer.message, holder.new_key_used);
                if (message != null) {
                    messages.add(message);
                }
                a++;
            }
            if (holes.isEmpty()) {
                this.secretHolesQueue.remove(Integer.valueOf(chat.id));
            }
            if (update) {
                MessagesStorage.getInstance().updateEncryptedChatSeq(chat);
            }
        }
    }

    protected ArrayList<Message> decryptMessage(EncryptedMessage message) {
        EncryptedChat chat = MessagesController.getInstance().getEncryptedChatDB(message.chat_id);
        if (chat == null || (chat instanceof TL_encryptedChatDiscarded)) {
            return null;
        }
        NativeByteBuffer nativeByteBuffer = new NativeByteBuffer(message.bytes.length);
        nativeByteBuffer.writeBytes(message.bytes);
        nativeByteBuffer.position(0);
        long fingerprint = nativeByteBuffer.readInt64(false);
        byte[] keyToDecrypt = null;
        boolean new_key_used = false;
        if (chat.key_fingerprint == fingerprint) {
            keyToDecrypt = chat.auth_key;
        } else if (chat.future_key_fingerprint != 0 && chat.future_key_fingerprint == fingerprint) {
            keyToDecrypt = chat.future_auth_key;
            new_key_used = true;
        }
        if (keyToDecrypt != null) {
            byte[] messageKey = nativeByteBuffer.readData(16, false);
            MessageKeyData keyData = MessageKeyData.generateMessageKeyData(keyToDecrypt, messageKey, false);
            Utilities.aesIgeEncryption(nativeByteBuffer.buffer, keyData.aesKey, keyData.aesIv, false, false, 24, nativeByteBuffer.limit() - 24);
            int len = nativeByteBuffer.readInt32(false);
            if (len < 0 || len > nativeByteBuffer.limit() - 28) {
                return null;
            }
            byte[] messageKeyFull = Utilities.computeSHA1(nativeByteBuffer.buffer, 24, Math.min((len + 4) + 24, nativeByteBuffer.buffer.limit()));
            if (!Utilities.arraysEquals(messageKey, 0, messageKeyFull, messageKeyFull.length - 16)) {
                return null;
            }
            TLObject object = TLClassStore.Instance().TLdeserialize(nativeByteBuffer, nativeByteBuffer.readInt32(false), false);
            nativeByteBuffer.reuse();
            if (!new_key_used && AndroidUtilities.getPeerLayerVersion(chat.layer) >= 20) {
                chat.key_use_count_in = (short) (chat.key_use_count_in + 1);
            }
            if (object instanceof TL_decryptedMessageLayer) {
                TL_decryptedMessageLayer layer = (TL_decryptedMessageLayer) object;
                if (chat.seq_in == 0 && chat.seq_out == 0) {
                    if (chat.admin_id == UserConfig.getClientUserId()) {
                        chat.seq_out = 1;
                    } else {
                        chat.seq_in = 1;
                    }
                }
                if (layer.random_bytes.length < 15) {
                    FileLog.m609e("tmessages", "got random bytes less than needed");
                    return null;
                }
                FileLog.m609e("tmessages", "current chat in_seq = " + chat.seq_in + " out_seq = " + chat.seq_out);
                FileLog.m609e("tmessages", "got message with in_seq = " + layer.in_seq_no + " out_seq = " + layer.out_seq_no);
                if (layer.out_seq_no < chat.seq_in) {
                    return null;
                }
                if (chat.seq_in == layer.out_seq_no || chat.seq_in == layer.out_seq_no - 2) {
                    chat.seq_in = layer.out_seq_no;
                    MessagesStorage.getInstance().updateEncryptedChatSeq(chat);
                    object = layer.message;
                } else {
                    FileLog.m609e("tmessages", "got hole");
                    ArrayList<TL_decryptedMessageHolder> arr = (ArrayList) this.secretHolesQueue.get(Integer.valueOf(chat.id));
                    if (arr == null) {
                        arr = new ArrayList();
                        this.secretHolesQueue.put(Integer.valueOf(chat.id), arr);
                    }
                    if (arr.size() >= 4) {
                        this.secretHolesQueue.remove(Integer.valueOf(chat.id));
                        TL_encryptedChatDiscarded newChat = new TL_encryptedChatDiscarded();
                        newChat.id = chat.id;
                        newChat.user_id = chat.user_id;
                        newChat.auth_key = chat.auth_key;
                        newChat.key_create_date = chat.key_create_date;
                        newChat.key_use_count_in = chat.key_use_count_in;
                        newChat.key_use_count_out = chat.key_use_count_out;
                        newChat.seq_in = chat.seq_in;
                        newChat.seq_out = chat.seq_out;
                        final TL_encryptedChatDiscarded tL_encryptedChatDiscarded = newChat;
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            public void run() {
                                MessagesController.getInstance().putEncryptedChat(tL_encryptedChatDiscarded, false);
                                MessagesStorage.getInstance().updateEncryptedChat(tL_encryptedChatDiscarded);
                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.encryptedChatUpdated, tL_encryptedChatDiscarded);
                            }
                        });
                        declineSecretChat(chat.id);
                        return null;
                    }
                    TL_decryptedMessageHolder holder = new TL_decryptedMessageHolder();
                    holder.layer = layer;
                    holder.file = message.file;
                    holder.random_id = message.random_id;
                    holder.date = message.date;
                    holder.new_key_used = new_key_used;
                    arr.add(holder);
                    return null;
                }
            }
            ArrayList<Message> messages = new ArrayList();
            Message decryptedMessage = processDecryptedObject(chat, message.file, message.date, message.random_id, object, new_key_used);
            if (decryptedMessage != null) {
                messages.add(decryptedMessage);
            }
            checkSecretHoles(chat, messages);
            return messages;
        }
        nativeByteBuffer.reuse();
        FileLog.m609e("tmessages", "fingerprint mismatch " + fingerprint);
        return null;
    }

    public void requestNewSecretChatKey(EncryptedChat encryptedChat) {
        if (AndroidUtilities.getPeerLayerVersion(encryptedChat.layer) >= 20) {
            byte[] salt = new byte[256];
            Utilities.random.nextBytes(salt);
            byte[] g_a = BigInteger.valueOf((long) MessagesStorage.secretG).modPow(new BigInteger(1, salt), new BigInteger(1, MessagesStorage.secretPBytes)).toByteArray();
            if (g_a.length > 256) {
                byte[] correctedAuth = new byte[256];
                System.arraycopy(g_a, 1, correctedAuth, 0, 256);
                g_a = correctedAuth;
            }
            encryptedChat.exchange_id = SendMessagesHelper.getInstance().getNextRandomId();
            encryptedChat.a_or_b = salt;
            encryptedChat.g_a = g_a;
            MessagesStorage.getInstance().updateEncryptedChat(encryptedChat);
            sendRequestKeyMessage(encryptedChat, null);
        }
    }

    public void processAcceptedSecretChat(final EncryptedChat encryptedChat) {
        BigInteger p = new BigInteger(1, MessagesStorage.secretPBytes);
        BigInteger i_authKey = new BigInteger(1, encryptedChat.g_a_or_b);
        if (Utilities.isGoodGaAndGb(i_authKey, p)) {
            byte[] authKey = i_authKey.modPow(new BigInteger(1, encryptedChat.a_or_b), p).toByteArray();
            byte[] correctedAuth;
            if (authKey.length > 256) {
                correctedAuth = new byte[256];
                System.arraycopy(authKey, authKey.length + InputDeviceCompat.SOURCE_ANY, correctedAuth, 0, 256);
                authKey = correctedAuth;
            } else if (authKey.length < 256) {
                correctedAuth = new byte[256];
                System.arraycopy(authKey, 0, correctedAuth, 256 - authKey.length, authKey.length);
                for (int a = 0; a < 256 - authKey.length; a++) {
                    authKey[a] = (byte) 0;
                }
                authKey = correctedAuth;
            }
            byte[] authKeyHash = Utilities.computeSHA1(authKey);
            byte[] authKeyId = new byte[8];
            System.arraycopy(authKeyHash, authKeyHash.length - 8, authKeyId, 0, 8);
            if (encryptedChat.key_fingerprint == Utilities.bytesToLong(authKeyId)) {
                encryptedChat.auth_key = authKey;
                encryptedChat.key_create_date = ConnectionsManager.getInstance().getCurrentTime();
                encryptedChat.seq_in = 0;
                encryptedChat.seq_out = 1;
                MessagesStorage.getInstance().updateEncryptedChat(encryptedChat);
                AndroidUtilities.runOnUIThread(new Runnable() {
                    public void run() {
                        MessagesController.getInstance().putEncryptedChat(encryptedChat, false);
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.encryptedChatUpdated, encryptedChat);
                        SecretChatHelper.this.sendNotifyLayerMessage(encryptedChat, null);
                    }
                });
                return;
            }
            final TL_encryptedChatDiscarded newChat = new TL_encryptedChatDiscarded();
            newChat.id = encryptedChat.id;
            newChat.user_id = encryptedChat.user_id;
            newChat.auth_key = encryptedChat.auth_key;
            newChat.key_create_date = encryptedChat.key_create_date;
            newChat.key_use_count_in = encryptedChat.key_use_count_in;
            newChat.key_use_count_out = encryptedChat.key_use_count_out;
            newChat.seq_in = encryptedChat.seq_in;
            newChat.seq_out = encryptedChat.seq_out;
            MessagesStorage.getInstance().updateEncryptedChat(newChat);
            AndroidUtilities.runOnUIThread(new Runnable() {
                public void run() {
                    MessagesController.getInstance().putEncryptedChat(newChat, false);
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.encryptedChatUpdated, newChat);
                }
            });
            declineSecretChat(encryptedChat.id);
            return;
        }
        declineSecretChat(encryptedChat.id);
    }

    public void declineSecretChat(int chat_id) {
        TL_messages_discardEncryption req = new TL_messages_discardEncryption();
        req.chat_id = chat_id;
        ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
            public void run(TLObject response, TL_error error) {
            }
        });
    }

    public void acceptSecretChat(final EncryptedChat encryptedChat) {
        if (this.acceptingChats.get(Integer.valueOf(encryptedChat.id)) == null) {
            this.acceptingChats.put(Integer.valueOf(encryptedChat.id), encryptedChat);
            TL_messages_getDhConfig req = new TL_messages_getDhConfig();
            req.random_length = 256;
            req.version = MessagesStorage.lastSecretVersion;
            ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {

                class C14591 implements RequestDelegate {
                    C14591() {
                    }

                    public void run(TLObject response, TL_error error) {
                        SecretChatHelper.this.acceptingChats.remove(Integer.valueOf(encryptedChat.id));
                        if (error == null) {
                            final EncryptedChat newChat = (EncryptedChat) response;
                            newChat.auth_key = encryptedChat.auth_key;
                            newChat.user_id = encryptedChat.user_id;
                            newChat.seq_in = encryptedChat.seq_in;
                            newChat.seq_out = encryptedChat.seq_out;
                            newChat.key_create_date = encryptedChat.key_create_date;
                            newChat.key_use_count_in = encryptedChat.key_use_count_in;
                            newChat.key_use_count_out = encryptedChat.key_use_count_out;
                            MessagesStorage.getInstance().updateEncryptedChat(newChat);
                            AndroidUtilities.runOnUIThread(new Runnable() {
                                public void run() {
                                    MessagesController.getInstance().putEncryptedChat(newChat, false);
                                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.encryptedChatUpdated, newChat);
                                    SecretChatHelper.this.sendNotifyLayerMessage(newChat, null);
                                }
                            });
                        }
                    }
                }

                public void run(TLObject response, TL_error error) {
                    if (error == null) {
                        int a;
                        messages_DhConfig res = (messages_DhConfig) response;
                        if (response instanceof TL_messages_dhConfig) {
                            if (Utilities.isGoodPrime(res.f147p, res.f146g)) {
                                MessagesStorage.secretPBytes = res.f147p;
                                MessagesStorage.secretG = res.f146g;
                                MessagesStorage.lastSecretVersion = res.version;
                                MessagesStorage.getInstance().saveSecretParams(MessagesStorage.lastSecretVersion, MessagesStorage.secretG, MessagesStorage.secretPBytes);
                            } else {
                                SecretChatHelper.this.acceptingChats.remove(Integer.valueOf(encryptedChat.id));
                                SecretChatHelper.this.declineSecretChat(encryptedChat.id);
                                return;
                            }
                        }
                        byte[] salt = new byte[256];
                        for (a = 0; a < 256; a++) {
                            salt[a] = (byte) (((byte) ((int) (Utilities.random.nextDouble() * 256.0d))) ^ res.random[a]);
                        }
                        encryptedChat.a_or_b = salt;
                        encryptedChat.seq_in = 1;
                        encryptedChat.seq_out = 0;
                        BigInteger p = new BigInteger(1, MessagesStorage.secretPBytes);
                        BigInteger g_b = BigInteger.valueOf((long) MessagesStorage.secretG).modPow(new BigInteger(1, salt), p);
                        BigInteger g_a = new BigInteger(1, encryptedChat.g_a);
                        if (Utilities.isGoodGaAndGb(g_a, p)) {
                            byte[] correctedAuth;
                            byte[] g_b_bytes = g_b.toByteArray();
                            if (g_b_bytes.length > 256) {
                                correctedAuth = new byte[256];
                                System.arraycopy(g_b_bytes, 1, correctedAuth, 0, 256);
                                g_b_bytes = correctedAuth;
                            }
                            byte[] authKey = g_a.modPow(new BigInteger(1, salt), p).toByteArray();
                            if (authKey.length > 256) {
                                correctedAuth = new byte[256];
                                System.arraycopy(authKey, authKey.length + InputDeviceCompat.SOURCE_ANY, correctedAuth, 0, 256);
                                authKey = correctedAuth;
                            } else if (authKey.length < 256) {
                                correctedAuth = new byte[256];
                                System.arraycopy(authKey, 0, correctedAuth, 256 - authKey.length, authKey.length);
                                for (a = 0; a < 256 - authKey.length; a++) {
                                    authKey[a] = (byte) 0;
                                }
                                authKey = correctedAuth;
                            }
                            byte[] authKeyHash = Utilities.computeSHA1(authKey);
                            byte[] authKeyId = new byte[8];
                            System.arraycopy(authKeyHash, authKeyHash.length - 8, authKeyId, 0, 8);
                            encryptedChat.auth_key = authKey;
                            encryptedChat.key_create_date = ConnectionsManager.getInstance().getCurrentTime();
                            TL_messages_acceptEncryption req2 = new TL_messages_acceptEncryption();
                            req2.g_b = g_b_bytes;
                            req2.peer = new TL_inputEncryptedChat();
                            req2.peer.chat_id = encryptedChat.id;
                            req2.peer.access_hash = encryptedChat.access_hash;
                            req2.key_fingerprint = Utilities.bytesToLong(authKeyId);
                            ConnectionsManager.getInstance().sendRequest(req2, new C14591());
                            return;
                        }
                        SecretChatHelper.this.acceptingChats.remove(Integer.valueOf(encryptedChat.id));
                        SecretChatHelper.this.declineSecretChat(encryptedChat.id);
                        return;
                    }
                    SecretChatHelper.this.acceptingChats.remove(Integer.valueOf(encryptedChat.id));
                }
            });
        }
    }

    public void startSecretChat(final Context context, final User user) {
        if (user != null && context != null) {
            this.startingSecretChat = true;
            final ProgressDialog progressDialog = new ProgressDialog(context);
            progressDialog.setMessage(LocaleController.getString("Loading", C0553R.string.Loading));
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setCancelable(false);
            TL_messages_getDhConfig req = new TL_messages_getDhConfig();
            req.random_length = 256;
            req.version = MessagesStorage.lastSecretVersion;
            final int reqId = ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {

                class C05561 implements Runnable {
                    C05561() {
                    }

                    public void run() {
                        try {
                            if (!((Activity) context).isFinishing()) {
                                progressDialog.dismiss();
                            }
                        } catch (Throwable e) {
                            FileLog.m611e("tmessages", e);
                        }
                    }
                }

                class C05613 implements Runnable {
                    C05613() {
                    }

                    public void run() {
                        SecretChatHelper.this.startingSecretChat = false;
                        if (!((Activity) context).isFinishing()) {
                            try {
                                progressDialog.dismiss();
                            } catch (Throwable e) {
                                FileLog.m611e("tmessages", e);
                            }
                        }
                    }
                }

                public void run(TLObject response, TL_error error) {
                    if (error == null) {
                        messages_DhConfig res = (messages_DhConfig) response;
                        if (response instanceof TL_messages_dhConfig) {
                            if (Utilities.isGoodPrime(res.f147p, res.f146g)) {
                                MessagesStorage.secretPBytes = res.f147p;
                                MessagesStorage.secretG = res.f146g;
                                MessagesStorage.lastSecretVersion = res.version;
                                MessagesStorage.getInstance().saveSecretParams(MessagesStorage.lastSecretVersion, MessagesStorage.secretG, MessagesStorage.secretPBytes);
                            } else {
                                AndroidUtilities.runOnUIThread(new C05561());
                                return;
                            }
                        }
                        final byte[] salt = new byte[256];
                        for (int a = 0; a < 256; a++) {
                            salt[a] = (byte) (((byte) ((int) (Utilities.random.nextDouble() * 256.0d))) ^ res.random[a]);
                        }
                        byte[] g_a = BigInteger.valueOf((long) MessagesStorage.secretG).modPow(new BigInteger(1, salt), new BigInteger(1, MessagesStorage.secretPBytes)).toByteArray();
                        if (g_a.length > 256) {
                            byte[] correctedAuth = new byte[256];
                            System.arraycopy(g_a, 1, correctedAuth, 0, 256);
                            g_a = correctedAuth;
                        }
                        TL_messages_requestEncryption req2 = new TL_messages_requestEncryption();
                        req2.g_a = g_a;
                        req2.user_id = MessagesController.getInputUser(user);
                        req2.random_id = Utilities.random.nextInt();
                        ConnectionsManager.getInstance().sendRequest(req2, new RequestDelegate() {

                            class C05602 implements Runnable {
                                C05602() {
                                }

                                public void run() {
                                    if (!((Activity) context).isFinishing()) {
                                        SecretChatHelper.this.startingSecretChat = false;
                                        try {
                                            progressDialog.dismiss();
                                        } catch (Throwable e) {
                                            FileLog.m611e("tmessages", e);
                                        }
                                        Builder builder = new Builder(context);
                                        builder.setTitle(LocaleController.getString("AppName", C0553R.string.AppName));
                                        builder.setMessage(LocaleController.getString("CreateEncryptedChatError", C0553R.string.CreateEncryptedChatError));
                                        builder.setPositiveButton(LocaleController.getString("OK", C0553R.string.OK), null);
                                        builder.show().setCanceledOnTouchOutside(true);
                                    }
                                }
                            }

                            public void run(final TLObject response, TL_error error) {
                                if (error == null) {
                                    AndroidUtilities.runOnUIThread(new Runnable() {

                                        class C05571 implements Comparator<Dialog> {
                                            C05571() {
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

                                        class C05582 implements Runnable {
                                            C05582() {
                                            }

                                            public void run() {
                                                if (!SecretChatHelper.this.delayedEncryptedChatUpdates.isEmpty()) {
                                                    MessagesController.getInstance().processUpdateArray(SecretChatHelper.this.delayedEncryptedChatUpdates, null, null);
                                                    SecretChatHelper.this.delayedEncryptedChatUpdates.clear();
                                                }
                                            }
                                        }

                                        public void run() {
                                            SecretChatHelper.this.startingSecretChat = false;
                                            if (!((Activity) context).isFinishing()) {
                                                try {
                                                    progressDialog.dismiss();
                                                } catch (Throwable e) {
                                                    FileLog.m611e("tmessages", e);
                                                }
                                            }
                                            EncryptedChat chat = response;
                                            chat.user_id = chat.participant_id;
                                            chat.seq_in = 0;
                                            chat.seq_out = 1;
                                            chat.a_or_b = salt;
                                            MessagesController.getInstance().putEncryptedChat(chat, false);
                                            Dialog dialog = new TL_dialog();
                                            dialog.id = ((long) chat.id) << 32;
                                            dialog.unread_count = 0;
                                            dialog.top_message = 0;
                                            dialog.last_message_date = ConnectionsManager.getInstance().getCurrentTime();
                                            MessagesController.getInstance().dialogs_dict.put(Long.valueOf(dialog.id), dialog);
                                            MessagesController.getInstance().dialogs.add(dialog);
                                            Collections.sort(MessagesController.getInstance().dialogs, new C05571());
                                            MessagesStorage.getInstance().putEncryptedChat(chat, user, dialog);
                                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
                                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.encryptedChatCreated, chat);
                                            Utilities.stageQueue.postRunnable(new C05582());
                                        }
                                    });
                                    return;
                                }
                                SecretChatHelper.this.delayedEncryptedChatUpdates.clear();
                                AndroidUtilities.runOnUIThread(new C05602());
                            }
                        }, 2);
                        return;
                    }
                    SecretChatHelper.this.delayedEncryptedChatUpdates.clear();
                    AndroidUtilities.runOnUIThread(new C05613());
                }
            }, 2);
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
    }
}
