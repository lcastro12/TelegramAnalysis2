package org.telegram.messenger;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build.VERSION;
import android.webkit.MimeTypeMap;
import android.widget.Toast;
import com.google.android.gms.location.LocationStatusCodes;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import org.telegram.messenger.MediaController.SearchImage;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.audioinfo.AudioInfo;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.QuickAckDelegate;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.ChatFull;
import org.telegram.tgnet.TLRPC.ChatParticipant;
import org.telegram.tgnet.TLRPC.DecryptedMessage;
import org.telegram.tgnet.TLRPC.Document;
import org.telegram.tgnet.TLRPC.DocumentAttribute;
import org.telegram.tgnet.TLRPC.EncryptedChat;
import org.telegram.tgnet.TLRPC.FileLocation;
import org.telegram.tgnet.TLRPC.InputEncryptedFile;
import org.telegram.tgnet.TLRPC.InputFile;
import org.telegram.tgnet.TLRPC.InputMedia;
import org.telegram.tgnet.TLRPC.InputPeer;
import org.telegram.tgnet.TLRPC.InputUser;
import org.telegram.tgnet.TLRPC.Message;
import org.telegram.tgnet.TLRPC.MessageMedia;
import org.telegram.tgnet.TLRPC.Peer;
import org.telegram.tgnet.TLRPC.PhotoSize;
import org.telegram.tgnet.TLRPC.TL_audio;
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
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionTyping;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageMediaAudio;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageMediaAudio_old;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageMediaContact;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageMediaDocument;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageMediaEmpty;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageMediaExternalDocument;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageMediaGeoPoint;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageMediaPhoto;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageMediaVideo;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageMediaVideo_old;
import org.telegram.tgnet.TLRPC.TL_decryptedMessage_old;
import org.telegram.tgnet.TLRPC.TL_document;
import org.telegram.tgnet.TLRPC.TL_documentAttributeAudio;
import org.telegram.tgnet.TLRPC.TL_documentAttributeAudio_old;
import org.telegram.tgnet.TLRPC.TL_documentAttributeFilename;
import org.telegram.tgnet.TLRPC.TL_documentAttributeImageSize;
import org.telegram.tgnet.TLRPC.TL_documentAttributeSticker;
import org.telegram.tgnet.TLRPC.TL_documentAttributeSticker_old;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_fileLocation;
import org.telegram.tgnet.TLRPC.TL_fileLocationUnavailable;
import org.telegram.tgnet.TLRPC.TL_inputAudio;
import org.telegram.tgnet.TLRPC.TL_inputDocument;
import org.telegram.tgnet.TLRPC.TL_inputEncryptedFile;
import org.telegram.tgnet.TLRPC.TL_inputGeoPoint;
import org.telegram.tgnet.TLRPC.TL_inputMediaAudio;
import org.telegram.tgnet.TLRPC.TL_inputMediaContact;
import org.telegram.tgnet.TLRPC.TL_inputMediaDocument;
import org.telegram.tgnet.TLRPC.TL_inputMediaEmpty;
import org.telegram.tgnet.TLRPC.TL_inputMediaGeoPoint;
import org.telegram.tgnet.TLRPC.TL_inputMediaPhoto;
import org.telegram.tgnet.TLRPC.TL_inputMediaUploadedAudio;
import org.telegram.tgnet.TLRPC.TL_inputMediaUploadedDocument;
import org.telegram.tgnet.TLRPC.TL_inputMediaUploadedPhoto;
import org.telegram.tgnet.TLRPC.TL_inputMediaUploadedThumbDocument;
import org.telegram.tgnet.TLRPC.TL_inputMediaUploadedThumbVideo;
import org.telegram.tgnet.TLRPC.TL_inputMediaUploadedVideo;
import org.telegram.tgnet.TLRPC.TL_inputMediaVenue;
import org.telegram.tgnet.TLRPC.TL_inputMediaVideo;
import org.telegram.tgnet.TLRPC.TL_inputPeerChannel;
import org.telegram.tgnet.TLRPC.TL_inputPeerEmpty;
import org.telegram.tgnet.TLRPC.TL_inputPhoto;
import org.telegram.tgnet.TLRPC.TL_inputStickerSetEmpty;
import org.telegram.tgnet.TLRPC.TL_inputVideo;
import org.telegram.tgnet.TLRPC.TL_message;
import org.telegram.tgnet.TLRPC.TL_messageEncryptedAction;
import org.telegram.tgnet.TLRPC.TL_messageMediaAudio;
import org.telegram.tgnet.TLRPC.TL_messageMediaContact;
import org.telegram.tgnet.TLRPC.TL_messageMediaDocument;
import org.telegram.tgnet.TLRPC.TL_messageMediaEmpty;
import org.telegram.tgnet.TLRPC.TL_messageMediaGeo;
import org.telegram.tgnet.TLRPC.TL_messageMediaPhoto;
import org.telegram.tgnet.TLRPC.TL_messageMediaVenue;
import org.telegram.tgnet.TLRPC.TL_messageMediaVideo;
import org.telegram.tgnet.TLRPC.TL_messageMediaWebPage;
import org.telegram.tgnet.TLRPC.TL_message_secret;
import org.telegram.tgnet.TLRPC.TL_messages_forwardMessages;
import org.telegram.tgnet.TLRPC.TL_messages_sendBroadcast;
import org.telegram.tgnet.TLRPC.TL_messages_sendMedia;
import org.telegram.tgnet.TLRPC.TL_messages_sendMessage;
import org.telegram.tgnet.TLRPC.TL_peerChannel;
import org.telegram.tgnet.TLRPC.TL_peerChat;
import org.telegram.tgnet.TLRPC.TL_peerUser;
import org.telegram.tgnet.TLRPC.TL_photo;
import org.telegram.tgnet.TLRPC.TL_photoCachedSize;
import org.telegram.tgnet.TLRPC.TL_photoSize;
import org.telegram.tgnet.TLRPC.TL_photoSizeEmpty;
import org.telegram.tgnet.TLRPC.TL_updateMessageID;
import org.telegram.tgnet.TLRPC.TL_updateNewChannelMessage;
import org.telegram.tgnet.TLRPC.TL_updateNewMessage;
import org.telegram.tgnet.TLRPC.TL_updateShortSentMessage;
import org.telegram.tgnet.TLRPC.TL_userContact_old2;
import org.telegram.tgnet.TLRPC.TL_userRequest_old2;
import org.telegram.tgnet.TLRPC.TL_video;
import org.telegram.tgnet.TLRPC.Update;
import org.telegram.tgnet.TLRPC.Updates;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.WebPage;

public class SendMessagesHelper implements NotificationCenterDelegate {
    private static volatile SendMessagesHelper Instance = null;
    private ChatFull currentChatInfo = null;
    private HashMap<String, ArrayList<DelayedMessage>> delayedMessages = new HashMap();
    private HashMap<Integer, Message> sendingMessages = new HashMap();
    private HashMap<Integer, MessageObject> unsentMessages = new HashMap();

    protected class DelayedMessage {
        public TL_audio audioLocation;
        public TL_document documentLocation;
        public EncryptedChat encryptedChat;
        public String httpLocation;
        public FileLocation location;
        public MessageObject obj;
        public String originalPath;
        public TL_decryptedMessage sendEncryptedRequest;
        public TLObject sendRequest;
        public int type;
        public VideoEditedInfo videoEditedInfo;
        public TL_video videoLocation;

        protected DelayedMessage() {
        }
    }

    public static SendMessagesHelper getInstance() {
        SendMessagesHelper localInstance = Instance;
        if (localInstance == null) {
            synchronized (SendMessagesHelper.class) {
                try {
                    localInstance = Instance;
                    if (localInstance == null) {
                        SendMessagesHelper localInstance2 = new SendMessagesHelper();
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

    public SendMessagesHelper() {
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.FileDidUpload);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.FileDidFailUpload);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.FilePreparingStarted);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.FileNewChunkAvailable);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.FilePreparingFailed);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.httpFileDidFailedLoad);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.httpFileDidLoaded);
    }

    public void cleanUp() {
        this.delayedMessages.clear();
        this.unsentMessages.clear();
        this.sendingMessages.clear();
        this.currentChatInfo = null;
    }

    public void setCurrentChatInfo(ChatFull info) {
        this.currentChatInfo = info;
    }

    public void didReceivedNotification(int id, Object... args) {
        String location;
        ArrayList<DelayedMessage> arr;
        int a;
        DelayedMessage message;
        if (id == NotificationCenter.FileDidUpload) {
            location = args[0];
            InputFile file = args[1];
            InputEncryptedFile encryptedFile = args[2];
            arr = (ArrayList) this.delayedMessages.get(location);
            if (arr != null) {
                a = 0;
                while (a < arr.size()) {
                    message = (DelayedMessage) arr.get(a);
                    InputMedia media = null;
                    if (message.sendRequest instanceof TL_messages_sendMedia) {
                        media = ((TL_messages_sendMedia) message.sendRequest).media;
                    } else if (message.sendRequest instanceof TL_messages_sendBroadcast) {
                        media = ((TL_messages_sendBroadcast) message.sendRequest).media;
                    }
                    if (file != null && media != null) {
                        if (message.type == 0) {
                            media.file = file;
                            performSendMessageRequest(message.sendRequest, message.obj.messageOwner, message.originalPath);
                        } else if (message.type == 1) {
                            if (media.file == null) {
                                media.file = file;
                                if (media.thumb != null || message.location == null) {
                                    performSendMessageRequest(message.sendRequest, message.obj.messageOwner, message.originalPath);
                                } else {
                                    performSendDelayedMessage(message);
                                }
                            } else {
                                media.thumb = file;
                                performSendMessageRequest(message.sendRequest, message.obj.messageOwner, message.originalPath);
                            }
                        } else if (message.type == 2) {
                            if (media.file == null) {
                                media.file = file;
                                if (media.thumb != null || message.location == null) {
                                    performSendMessageRequest(message.sendRequest, message.obj.messageOwner, message.originalPath);
                                } else {
                                    performSendDelayedMessage(message);
                                }
                            } else {
                                media.thumb = file;
                                performSendMessageRequest(message.sendRequest, message.obj.messageOwner, message.originalPath);
                            }
                        } else if (message.type == 3) {
                            media.file = file;
                            performSendMessageRequest(message.sendRequest, message.obj.messageOwner, message.originalPath);
                        }
                        arr.remove(a);
                        a--;
                    } else if (!(encryptedFile == null || message.sendEncryptedRequest == null)) {
                        if (message.sendEncryptedRequest.media instanceof TL_decryptedMessageMediaVideo) {
                            message.sendEncryptedRequest.media.size = (int) ((Long) args[5]).longValue();
                        }
                        message.sendEncryptedRequest.media.key = (byte[]) args[3];
                        message.sendEncryptedRequest.media.iv = (byte[]) args[4];
                        SecretChatHelper.getInstance().performSendEncryptedRequest(message.sendEncryptedRequest, message.obj.messageOwner, message.encryptedChat, encryptedFile, message.originalPath);
                        arr.remove(a);
                        a--;
                    }
                    a++;
                }
                if (arr.isEmpty()) {
                    this.delayedMessages.remove(location);
                }
            }
        } else if (id == NotificationCenter.FileDidFailUpload) {
            location = (String) args[0];
            boolean enc = ((Boolean) args[1]).booleanValue();
            arr = (ArrayList) this.delayedMessages.get(location);
            if (arr != null) {
                a = 0;
                while (a < arr.size()) {
                    DelayedMessage obj = (DelayedMessage) arr.get(a);
                    if ((enc && obj.sendEncryptedRequest != null) || !(enc || obj.sendRequest == null)) {
                        MessagesStorage.getInstance().markMessageAsSendError(obj.obj.messageOwner);
                        obj.obj.messageOwner.send_state = 2;
                        arr.remove(a);
                        a--;
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.messageSendError, Integer.valueOf(obj.obj.getId()));
                        processSentMessage(obj.obj.getId());
                    }
                    a++;
                }
                if (arr.isEmpty()) {
                    this.delayedMessages.remove(location);
                }
            }
        } else if (id == NotificationCenter.FilePreparingStarted) {
            messageObject = args[0];
            finalPath = args[1];
            arr = (ArrayList) this.delayedMessages.get(messageObject.messageOwner.attachPath);
            if (arr != null) {
                for (a = 0; a < arr.size(); a++) {
                    message = (DelayedMessage) arr.get(a);
                    if (message.obj == messageObject) {
                        message.videoEditedInfo = null;
                        performSendDelayedMessage(message);
                        arr.remove(a);
                        break;
                    }
                }
                if (arr.isEmpty()) {
                    this.delayedMessages.remove(messageObject.messageOwner.attachPath);
                }
            }
        } else if (id == NotificationCenter.FileNewChunkAvailable) {
            messageObject = (MessageObject) args[0];
            finalPath = (String) args[1];
            long finalSize = ((Long) args[2]).longValue();
            FileLoader.getInstance().checkUploadNewDataAvailable(finalPath, ((int) messageObject.getDialogId()) == 0, finalSize);
            if (finalSize != 0) {
                arr = (ArrayList) this.delayedMessages.get(messageObject.messageOwner.attachPath);
                if (arr != null) {
                    i$ = arr.iterator();
                    while (i$.hasNext()) {
                        message = (DelayedMessage) i$.next();
                        if (message.obj == messageObject) {
                            message.obj.videoEditedInfo = null;
                            message.obj.messageOwner.message = "-1";
                            message.obj.messageOwner.media.video.size = (int) finalSize;
                            ArrayList<Message> messages = new ArrayList();
                            messages.add(message.obj.messageOwner);
                            MessagesStorage.getInstance().putMessages(messages, false, true, false, 0);
                            break;
                        }
                    }
                    if (arr.isEmpty()) {
                        this.delayedMessages.remove(messageObject.messageOwner.attachPath);
                    }
                }
            }
        } else if (id == NotificationCenter.FilePreparingFailed) {
            messageObject = (MessageObject) args[0];
            finalPath = (String) args[1];
            stopVideoService(messageObject.messageOwner.attachPath);
            arr = (ArrayList) this.delayedMessages.get(finalPath);
            if (arr != null) {
                a = 0;
                while (a < arr.size()) {
                    message = (DelayedMessage) arr.get(a);
                    if (message.obj == messageObject) {
                        MessagesStorage.getInstance().markMessageAsSendError(message.obj.messageOwner);
                        message.obj.messageOwner.send_state = 2;
                        arr.remove(a);
                        a--;
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.messageSendError, Integer.valueOf(message.obj.getId()));
                        processSentMessage(message.obj.getId());
                    }
                    a++;
                }
                if (arr.isEmpty()) {
                    this.delayedMessages.remove(finalPath);
                }
            }
        } else if (id == NotificationCenter.httpFileDidLoaded) {
            path = args[0];
            String file2 = args[1];
            arr = (ArrayList) this.delayedMessages.get(path);
            if (arr != null) {
                for (a = 0; a < arr.size(); a++) {
                    message = (DelayedMessage) arr.get(a);
                    if (message.type == 0) {
                        final File file3 = new File(FileLoader.getInstance().getDirectory(4), Utilities.MD5(message.httpLocation) + ".jpg");
                        final DelayedMessage delayedMessage = message;
                        Utilities.globalQueue.postRunnable(new Runnable() {
                            public void run() {
                                final TL_photo photo = SendMessagesHelper.getInstance().generatePhotoSizes(file3.toString(), null);
                                AndroidUtilities.runOnUIThread(new Runnable() {
                                    public void run() {
                                        if (photo != null) {
                                            delayedMessage.httpLocation = null;
                                            delayedMessage.obj.messageOwner.media.photo = photo;
                                            delayedMessage.obj.messageOwner.attachPath = file3.toString();
                                            delayedMessage.location = ((PhotoSize) photo.sizes.get(photo.sizes.size() - 1)).location;
                                            ArrayList<Message> messages = new ArrayList();
                                            messages.add(delayedMessage.obj.messageOwner);
                                            MessagesStorage.getInstance().putMessages(messages, false, true, false, 0);
                                            SendMessagesHelper.this.performSendDelayedMessage(delayedMessage);
                                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.updateMessageMedia, delayedMessage.obj);
                                            return;
                                        }
                                        FileLog.m609e("tmessages", "can't load image " + delayedMessage.httpLocation + " to file " + file3.toString());
                                        MessagesStorage.getInstance().markMessageAsSendError(delayedMessage.obj.messageOwner);
                                        delayedMessage.obj.messageOwner.send_state = 2;
                                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.messageSendError, Integer.valueOf(delayedMessage.obj.getId()));
                                        SendMessagesHelper.this.processSentMessage(delayedMessage.obj.getId());
                                    }
                                });
                            }
                        });
                    } else if (message.type == 2) {
                        final DelayedMessage delayedMessage2 = message;
                        final File file4 = new File(FileLoader.getInstance().getDirectory(4), Utilities.MD5(message.httpLocation) + ".gif");
                        Utilities.globalQueue.postRunnable(new Runnable() {

                            class C05851 implements Runnable {
                                C05851() {
                                }

                                public void run() {
                                    delayedMessage2.httpLocation = null;
                                    delayedMessage2.obj.messageOwner.attachPath = file4.toString();
                                    delayedMessage2.location = delayedMessage2.documentLocation.thumb.location;
                                    ArrayList<Message> messages = new ArrayList();
                                    messages.add(delayedMessage2.obj.messageOwner);
                                    MessagesStorage.getInstance().putMessages(messages, false, true, false, 0);
                                    SendMessagesHelper.this.performSendDelayedMessage(delayedMessage2);
                                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.updateMessageMedia, delayedMessage2.obj);
                                }
                            }

                            public void run() {
                                boolean z = true;
                                if (delayedMessage2.documentLocation.thumb.location instanceof TL_fileLocationUnavailable) {
                                    try {
                                        Bitmap bitmap = ImageLoader.loadBitmap(file4.getAbsolutePath(), null, 90.0f, 90.0f, true);
                                        if (bitmap != null) {
                                            TL_document tL_document = delayedMessage2.documentLocation;
                                            if (delayedMessage2.sendEncryptedRequest == null) {
                                                z = false;
                                            }
                                            tL_document.thumb = ImageLoader.scaleAndSaveImage(bitmap, 90.0f, 90.0f, 55, z);
                                            bitmap.recycle();
                                        }
                                    } catch (Throwable e) {
                                        delayedMessage2.documentLocation.thumb = null;
                                        FileLog.m611e("tmessages", e);
                                    }
                                    if (delayedMessage2.documentLocation.thumb == null) {
                                        delayedMessage2.documentLocation.thumb = new TL_photoSizeEmpty();
                                        delayedMessage2.documentLocation.thumb.type = "s";
                                    }
                                }
                                AndroidUtilities.runOnUIThread(new C05851());
                            }
                        });
                    }
                }
                this.delayedMessages.remove(path);
            }
        } else if (id == NotificationCenter.httpFileDidFailedLoad) {
            path = (String) args[0];
            arr = (ArrayList) this.delayedMessages.get(path);
            if (arr != null) {
                i$ = arr.iterator();
                while (i$.hasNext()) {
                    message = (DelayedMessage) i$.next();
                    MessagesStorage.getInstance().markMessageAsSendError(message.obj.messageOwner);
                    message.obj.messageOwner.send_state = 2;
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.messageSendError, Integer.valueOf(message.obj.getId()));
                    processSentMessage(message.obj.getId());
                }
                this.delayedMessages.remove(path);
            }
        }
    }

    public void cancelSendingMessage(MessageObject object) {
        String keyToRemvoe = null;
        boolean enc = false;
        for (Entry<String, ArrayList<DelayedMessage>> entry : this.delayedMessages.entrySet()) {
            ArrayList<DelayedMessage> messages = (ArrayList) entry.getValue();
            int a = 0;
            while (a < messages.size()) {
                DelayedMessage message = (DelayedMessage) messages.get(a);
                if (message.obj.getId() == object.getId()) {
                    messages.remove(a);
                    MediaController.getInstance().cancelVideoConvert(message.obj);
                    if (messages.size() == 0) {
                        keyToRemvoe = (String) entry.getKey();
                        if (message.sendEncryptedRequest != null) {
                            enc = true;
                        }
                    }
                } else {
                    a++;
                }
            }
        }
        if (keyToRemvoe != null) {
            if (keyToRemvoe.startsWith("http")) {
                ImageLoader.getInstance().cancelLoadHttpFile(keyToRemvoe);
            } else {
                FileLoader.getInstance().cancelUploadFile(keyToRemvoe, enc);
            }
            stopVideoService(keyToRemvoe);
        }
        ArrayList<Integer> messages2 = new ArrayList();
        messages2.add(Integer.valueOf(object.getId()));
        MessagesController.getInstance().deleteMessages(messages2, null, null, object.messageOwner.to_id.channel_id);
    }

    public boolean retrySendMessage(MessageObject messageObject, boolean unsent) {
        boolean z = false;
        if (messageObject.getId() >= 0) {
            return false;
        }
        if (messageObject.messageOwner.action instanceof TL_messageEncryptedAction) {
            EncryptedChat encryptedChat = MessagesController.getInstance().getEncryptedChat(Integer.valueOf((int) (messageObject.getDialogId() >> 32)));
            if (encryptedChat == null) {
                MessagesStorage.getInstance().markMessageAsSendError(messageObject.messageOwner);
                messageObject.messageOwner.send_state = 2;
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.messageSendError, Integer.valueOf(messageObject.getId()));
                processSentMessage(messageObject.getId());
                return false;
            }
            if (messageObject.messageOwner.random_id == 0) {
                messageObject.messageOwner.random_id = getNextRandomId();
            }
            if (messageObject.messageOwner.action.encryptedAction instanceof TL_decryptedMessageActionSetMessageTTL) {
                SecretChatHelper.getInstance().sendTTLMessage(encryptedChat, messageObject.messageOwner);
            } else if (messageObject.messageOwner.action.encryptedAction instanceof TL_decryptedMessageActionDeleteMessages) {
                SecretChatHelper.getInstance().sendMessagesDeleteMessage(encryptedChat, null, messageObject.messageOwner);
            } else if (messageObject.messageOwner.action.encryptedAction instanceof TL_decryptedMessageActionFlushHistory) {
                SecretChatHelper.getInstance().sendClearHistoryMessage(encryptedChat, messageObject.messageOwner);
            } else if (messageObject.messageOwner.action.encryptedAction instanceof TL_decryptedMessageActionNotifyLayer) {
                SecretChatHelper.getInstance().sendNotifyLayerMessage(encryptedChat, messageObject.messageOwner);
            } else if (messageObject.messageOwner.action.encryptedAction instanceof TL_decryptedMessageActionReadMessages) {
                SecretChatHelper.getInstance().sendMessagesReadMessage(encryptedChat, null, messageObject.messageOwner);
            } else if (messageObject.messageOwner.action.encryptedAction instanceof TL_decryptedMessageActionScreenshotMessages) {
                SecretChatHelper.getInstance().sendScreenshotMessage(encryptedChat, null, messageObject.messageOwner);
            } else if (!((messageObject.messageOwner.action.encryptedAction instanceof TL_decryptedMessageActionTyping) || (messageObject.messageOwner.action.encryptedAction instanceof TL_decryptedMessageActionResend))) {
                if (messageObject.messageOwner.action.encryptedAction instanceof TL_decryptedMessageActionCommitKey) {
                    SecretChatHelper.getInstance().sendCommitKeyMessage(encryptedChat, messageObject.messageOwner);
                } else if (messageObject.messageOwner.action.encryptedAction instanceof TL_decryptedMessageActionAbortKey) {
                    SecretChatHelper.getInstance().sendAbortKeyMessage(encryptedChat, messageObject.messageOwner, 0);
                } else if (messageObject.messageOwner.action.encryptedAction instanceof TL_decryptedMessageActionRequestKey) {
                    SecretChatHelper.getInstance().sendRequestKeyMessage(encryptedChat, messageObject.messageOwner);
                } else if (messageObject.messageOwner.action.encryptedAction instanceof TL_decryptedMessageActionAcceptKey) {
                    SecretChatHelper.getInstance().sendAcceptKeyMessage(encryptedChat, messageObject.messageOwner);
                } else if (messageObject.messageOwner.action.encryptedAction instanceof TL_decryptedMessageActionNoop) {
                    SecretChatHelper.getInstance().sendNoopMessage(encryptedChat, messageObject.messageOwner);
                }
            }
            return true;
        }
        if (unsent) {
            this.unsentMessages.put(Integer.valueOf(messageObject.getId()), messageObject);
        }
        if (messageObject.messageOwner.from_id < 0) {
            z = true;
        }
        sendMessage(messageObject, z);
        return true;
    }

    protected void processSentMessage(int id) {
        int prevSize = this.unsentMessages.size();
        this.unsentMessages.remove(Integer.valueOf(id));
        if (prevSize != 0 && this.unsentMessages.size() == 0) {
            checkUnsentMessages();
        }
    }

    public void processForwardFromMyName(MessageObject messageObject, long did, boolean asAdmin) {
        if (messageObject != null) {
            ArrayList<MessageObject> arrayList;
            if (messageObject.messageOwner.media == null || (messageObject.messageOwner.media instanceof TL_messageMediaEmpty) || (messageObject.messageOwner.media instanceof TL_messageMediaWebPage)) {
                if (messageObject.messageOwner.message != null) {
                    WebPage webPage = null;
                    if (messageObject.messageOwner.media instanceof TL_messageMediaWebPage) {
                        webPage = messageObject.messageOwner.media.webpage;
                    }
                    sendMessage(messageObject.messageOwner.message, did, messageObject.replyMessageObject, webPage, true, asAdmin);
                    return;
                }
                arrayList = new ArrayList();
                arrayList.add(messageObject);
                sendMessage(arrayList, did, asAdmin);
            } else if (messageObject.messageOwner.media.photo instanceof TL_photo) {
                sendMessage((TL_photo) messageObject.messageOwner.media.photo, null, null, did, messageObject.replyMessageObject, asAdmin);
            } else if (messageObject.messageOwner.media.audio instanceof TL_audio) {
                sendMessage((TL_audio) messageObject.messageOwner.media.audio, messageObject.messageOwner.attachPath, did, messageObject.replyMessageObject, asAdmin);
            } else if (messageObject.messageOwner.media.video instanceof TL_video) {
                sendMessage(messageObject.messageOwner.media.video, messageObject.videoEditedInfo, null, messageObject.messageOwner.attachPath, did, messageObject.replyMessageObject, asAdmin);
            } else if (messageObject.messageOwner.media.document instanceof TL_document) {
                sendMessage((TL_document) messageObject.messageOwner.media.document, null, messageObject.messageOwner.attachPath, did, messageObject.replyMessageObject, asAdmin);
            } else if ((messageObject.messageOwner.media instanceof TL_messageMediaVenue) || (messageObject.messageOwner.media instanceof TL_messageMediaGeo)) {
                sendMessage(messageObject.messageOwner.media, did, messageObject.replyMessageObject, asAdmin);
            } else if (messageObject.messageOwner.media.phone_number != null) {
                User user = new TL_userContact_old2();
                user.phone = messageObject.messageOwner.media.phone_number;
                user.first_name = messageObject.messageOwner.media.first_name;
                user.last_name = messageObject.messageOwner.media.last_name;
                user.id = messageObject.messageOwner.media.user_id;
                sendMessage(user, did, messageObject.replyMessageObject, asAdmin);
            } else {
                arrayList = new ArrayList();
                arrayList.add(messageObject);
                sendMessage(arrayList, did, asAdmin);
            }
        }
    }

    public void sendSticker(Document document, long peer, MessageObject replyingMessageObject, boolean asAdmin) {
        if (document != null) {
            if (((int) peer) == 0 && (document.thumb instanceof TL_photoSize)) {
                File file = FileLoader.getPathToAttach(document.thumb, true);
                if (file.exists()) {
                    try {
                        int len = (int) file.length();
                        byte[] arr = new byte[((int) file.length())];
                        new RandomAccessFile(file, "r").readFully(arr);
                        Document newDocument = new TL_document();
                        newDocument.thumb = new TL_photoCachedSize();
                        newDocument.thumb.location = document.thumb.location;
                        newDocument.thumb.size = document.thumb.size;
                        newDocument.thumb.f139w = document.thumb.f139w;
                        newDocument.thumb.f138h = document.thumb.f138h;
                        newDocument.thumb.type = document.thumb.type;
                        newDocument.thumb.bytes = arr;
                        newDocument.id = document.id;
                        newDocument.access_hash = document.access_hash;
                        newDocument.date = document.date;
                        newDocument.mime_type = document.mime_type;
                        newDocument.size = document.size;
                        newDocument.dc_id = document.dc_id;
                        newDocument.attributes = document.attributes;
                        if (newDocument.mime_type == null) {
                            newDocument.mime_type = "";
                        }
                        document = newDocument;
                    } catch (Throwable e) {
                        FileLog.m611e("tmessages", e);
                    }
                }
            }
            if (((int) peer) == 0) {
                for (int a = 0; a < document.attributes.size(); a++) {
                    if (((DocumentAttribute) document.attributes.get(a)) instanceof TL_documentAttributeSticker) {
                        document.attributes.remove(a);
                        document.attributes.add(new TL_documentAttributeSticker_old());
                        break;
                    }
                }
            }
            getInstance().sendMessage((TL_document) document, null, null, peer, replyingMessageObject, asAdmin);
        }
    }

    public void sendMessage(User user, long peer, MessageObject reply_to_msg, boolean asAdmin) {
        sendMessage(null, null, null, null, null, user, null, null, null, peer, null, reply_to_msg, null, true, asAdmin, null);
    }

    public void sendMessage(ArrayList<MessageObject> messages, long peer, boolean asAdmin) {
        if (((int) peer) != 0 && messages != null && !messages.isEmpty()) {
            int lower_id = (int) peer;
            Peer to_id = MessagesController.getPeer((int) peer);
            boolean isMegagroup = false;
            if (lower_id <= 0) {
                Chat chat = MessagesController.getInstance().getChat(Integer.valueOf(-lower_id));
                isMegagroup = ChatObject.isChannel(chat) && chat.megagroup;
            } else if (MessagesController.getInstance().getUser(Integer.valueOf(lower_id)) == null) {
                return;
            }
            ArrayList<MessageObject> objArr = new ArrayList();
            ArrayList<Message> arr = new ArrayList();
            ArrayList<Long> randomIds = new ArrayList();
            ArrayList<Integer> ids = new ArrayList();
            HashMap<Long, Message> messagesByRandomIds = new HashMap();
            InputPeer inputPeer = MessagesController.getInputPeer(lower_id);
            int a = 0;
            while (a < messages.size()) {
                MessageObject msgObj = (MessageObject) messages.get(a);
                if (msgObj.getId() > 0) {
                    Message newMsg = new TL_message();
                    if (msgObj.isForwarded()) {
                        newMsg.fwd_from_id = msgObj.messageOwner.fwd_from_id;
                        newMsg.fwd_date = msgObj.messageOwner.fwd_date;
                    } else {
                        if (msgObj.messageOwner.from_id > 0) {
                            newMsg.fwd_from_id = new TL_peerUser();
                            newMsg.fwd_from_id.user_id = msgObj.messageOwner.from_id;
                        } else {
                            newMsg.fwd_from_id = msgObj.messageOwner.to_id;
                        }
                        newMsg.fwd_date = msgObj.messageOwner.date;
                    }
                    newMsg.media = msgObj.messageOwner.media;
                    newMsg.flags = 4;
                    if (newMsg.media != null) {
                        newMsg.flags |= 512;
                    }
                    if (isMegagroup) {
                        newMsg.flags |= Integer.MIN_VALUE;
                    }
                    newMsg.message = msgObj.messageOwner.message;
                    newMsg.fwd_msg_id = msgObj.getId();
                    newMsg.attachPath = msgObj.messageOwner.attachPath;
                    newMsg.entities = msgObj.messageOwner.entities;
                    if (!newMsg.entities.isEmpty()) {
                        newMsg.flags |= 128;
                    }
                    if (newMsg.attachPath == null) {
                        newMsg.attachPath = "";
                    }
                    int newMessageId = UserConfig.getNewMessageId();
                    newMsg.id = newMessageId;
                    newMsg.local_id = newMessageId;
                    newMsg.out = true;
                    if (!asAdmin || to_id.channel_id == 0 || isMegagroup) {
                        newMsg.from_id = UserConfig.getClientUserId();
                        newMsg.flags |= 256;
                    } else {
                        newMsg.from_id = -to_id.channel_id;
                    }
                    if (newMsg.random_id == 0) {
                        newMsg.random_id = getNextRandomId();
                    }
                    randomIds.add(Long.valueOf(newMsg.random_id));
                    messagesByRandomIds.put(Long.valueOf(newMsg.random_id), newMsg);
                    ids.add(Integer.valueOf(newMsg.fwd_msg_id));
                    newMsg.date = ConnectionsManager.getInstance().getCurrentTime();
                    if (newMsg.media instanceof TL_messageMediaAudio) {
                        newMsg.media_unread = true;
                    }
                    if (!(inputPeer instanceof TL_inputPeerChannel)) {
                        if ((msgObj.messageOwner.flags & 1024) != 0) {
                            newMsg.views = msgObj.messageOwner.views;
                            newMsg.flags |= 1024;
                        }
                        newMsg.unread = true;
                    } else if (asAdmin && !isMegagroup) {
                        newMsg.views = 1;
                        newMsg.flags |= 1024;
                    }
                    newMsg.dialog_id = peer;
                    newMsg.to_id = to_id;
                    if (msgObj.messageOwner.to_id instanceof TL_peerChannel) {
                        newMsg.ttl = -msgObj.messageOwner.to_id.channel_id;
                    }
                    MessageObject messageObject = new MessageObject(newMsg, null, true);
                    messageObject.messageOwner.send_state = 1;
                    objArr.add(messageObject);
                    arr.add(newMsg);
                    putToSendingMessages(newMsg);
                    if (!(arr.size() == 100 || a == messages.size() - 1)) {
                        if (a != messages.size() - 1) {
                            if (((MessageObject) messages.get(a + 1)).getDialogId() == msgObj.getDialogId()) {
                            }
                        }
                    }
                    MessagesStorage.getInstance().putMessages(new ArrayList(arr), false, true, false, 0);
                    MessagesController.getInstance().updateInterfaceWithMessages(peer, objArr);
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
                    UserConfig.saveConfig(false);
                    TLObject req = new TL_messages_forwardMessages();
                    req.to_peer = inputPeer;
                    if (msgObj.messageOwner.to_id instanceof TL_peerChannel) {
                        req.from_peer = MessagesController.getInputPeer(-msgObj.messageOwner.to_id.channel_id);
                    } else {
                        req.from_peer = new TL_inputPeerEmpty();
                    }
                    req.random_id = randomIds;
                    req.id = ids;
                    if (!(!asAdmin || req.to_peer.channel_id == 0 || isMegagroup)) {
                        req.broadcast = true;
                    }
                    final ArrayList<Message> newMsgObjArr = arr;
                    final HashMap<Long, Message> messagesByRandomIdsFinal = messagesByRandomIds;
                    final boolean isMegagroupFinal = isMegagroup;
                    final Peer peer2 = to_id;
                    final long j = peer;
                    ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
                        public void run(TLObject response, TL_error error) {
                            final Message newMsgObj;
                            if (error == null) {
                                Update update;
                                HashMap<Integer, Long> newMessagesByIds = new HashMap();
                                Updates updates = (Updates) response;
                                int a = 0;
                                while (a < updates.updates.size()) {
                                    update = (Update) updates.updates.get(a);
                                    if (update instanceof TL_updateMessageID) {
                                        newMessagesByIds.put(Integer.valueOf(update.id), Long.valueOf(update.random_id));
                                        updates.updates.remove(a);
                                        a--;
                                    }
                                    a++;
                                }
                                for (a = 0; a < updates.updates.size(); a++) {
                                    update = (Update) updates.updates.get(a);
                                    if ((update instanceof TL_updateNewMessage) || (update instanceof TL_updateNewChannelMessage)) {
                                        Message message;
                                        if (update instanceof TL_updateNewMessage) {
                                            message = ((TL_updateNewMessage) update).message;
                                            MessagesController.getInstance().processNewDifferenceParams(-1, update.pts, -1, update.pts_count);
                                        } else {
                                            message = ((TL_updateNewChannelMessage) update).message;
                                            MessagesController.getInstance().processNewChannelDifferenceParams(update.pts, update.pts_count, message.to_id.channel_id);
                                            if (isMegagroupFinal) {
                                                message.flags |= Integer.MIN_VALUE;
                                            }
                                        }
                                        Long random_id = (Long) newMessagesByIds.get(Integer.valueOf(message.id));
                                        if (random_id != null) {
                                            newMsgObj = (Message) messagesByRandomIdsFinal.get(random_id);
                                            if (newMsgObj != null) {
                                                newMsgObjArr.remove(newMsgObj);
                                                final int oldId = newMsgObj.id;
                                                final ArrayList<Message> sentMessages = new ArrayList();
                                                sentMessages.add(message);
                                                newMsgObj.id = message.id;
                                                SendMessagesHelper.this.processSentMessage(newMsgObj, message, null);
                                                MessagesStorage.getInstance().getStorageQueue().postRunnable(new Runnable() {

                                                    class C05871 implements Runnable {
                                                        C05871() {
                                                        }

                                                        public void run() {
                                                            newMsgObj.send_state = 0;
                                                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.messageReceivedByServer, Integer.valueOf(oldId), Integer.valueOf(newMsgObj.id), newMsgObj, Long.valueOf(j));
                                                            SendMessagesHelper.this.processSentMessage(oldId);
                                                            SendMessagesHelper.this.removeFromSendingMessages(oldId);
                                                        }
                                                    }

                                                    public void run() {
                                                        MessagesStorage.getInstance().updateMessageStateAndId(newMsgObj.random_id, Integer.valueOf(oldId), newMsgObj.id, 0, false, peer2.channel_id);
                                                        MessagesStorage.getInstance().putMessages(sentMessages, true, false, false, 0);
                                                        AndroidUtilities.runOnUIThread(new C05871());
                                                        if (newMsgObj.media instanceof TL_messageMediaVideo) {
                                                            SendMessagesHelper.this.stopVideoService(newMsgObj.attachPath);
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    }
                                }
                            } else {
                                final TL_error tL_error = error;
                                AndroidUtilities.runOnUIThread(new Runnable() {
                                    public void run() {
                                        if (tL_error.text.equals("PEER_FLOOD")) {
                                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.needShowAlert, Integer.valueOf(0));
                                        }
                                    }
                                });
                            }
                            Iterator i$ = newMsgObjArr.iterator();
                            while (i$.hasNext()) {
                                newMsgObj = (Message) i$.next();
                                MessagesStorage.getInstance().markMessageAsSendError(newMsgObj);
                                AndroidUtilities.runOnUIThread(new Runnable() {
                                    public void run() {
                                        newMsgObj.send_state = 2;
                                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.messageSendError, Integer.valueOf(newMsgObj.id));
                                        SendMessagesHelper.this.processSentMessage(newMsgObj.id);
                                        if (newMsgObj.media instanceof TL_messageMediaVideo) {
                                            SendMessagesHelper.this.stopVideoService(newMsgObj.attachPath);
                                        }
                                        SendMessagesHelper.this.removeFromSendingMessages(newMsgObj.id);
                                    }
                                });
                            }
                        }
                    }, 68);
                    if (a != messages.size() - 1) {
                        objArr = new ArrayList();
                        arr = new ArrayList();
                        randomIds = new ArrayList();
                        ids = new ArrayList();
                        messagesByRandomIds = new HashMap();
                    }
                }
                a++;
            }
        }
    }

    public void sendMessage(MessageObject retryMessageObject, boolean asAdmin) {
        sendMessage(null, null, null, null, null, null, null, null, null, retryMessageObject.getDialogId(), retryMessageObject.messageOwner.attachPath, null, null, true, asAdmin, retryMessageObject);
    }

    public void sendMessage(TL_document document, String originalPath, String path, long peer, MessageObject reply_to_msg, boolean asAdmin) {
        sendMessage(null, null, null, null, null, null, document, null, originalPath, peer, path, reply_to_msg, null, true, asAdmin, null);
    }

    public void sendMessage(String message, long peer, MessageObject reply_to_msg, WebPage webPage, boolean searchLinks, boolean asAdmin) {
        sendMessage(message, null, null, null, null, null, null, null, null, peer, null, reply_to_msg, webPage, searchLinks, asAdmin, null);
    }

    public void sendMessage(MessageMedia location, long peer, MessageObject reply_to_msg, boolean asAdmin) {
        sendMessage(null, location, null, null, null, null, null, null, null, peer, null, reply_to_msg, null, true, asAdmin, null);
    }

    public void sendMessage(TL_photo photo, String originalPath, String path, long peer, MessageObject reply_to_msg, boolean asAdmin) {
        sendMessage(null, null, photo, null, null, null, null, null, originalPath, peer, path, reply_to_msg, null, true, asAdmin, null);
    }

    public void sendMessage(TL_video video, VideoEditedInfo videoEditedInfo, String originalPath, String path, long peer, MessageObject reply_to_msg, boolean asAdmin) {
        sendMessage(null, null, null, video, videoEditedInfo, null, null, null, originalPath, peer, path, reply_to_msg, null, true, asAdmin, null);
    }

    public void sendMessage(TL_audio audio, String path, long peer, MessageObject reply_to_msg, boolean asAdmin) {
        sendMessage(null, null, null, null, null, null, null, audio, null, peer, path, reply_to_msg, null, true, asAdmin, null);
    }

    private void sendMessage(String message, MessageMedia location, TL_photo photo, TL_video video, VideoEditedInfo videoEditedInfo, User user, TL_document document, TL_audio audio, String originalPath, long peer, String path, MessageObject reply_to_msg, WebPage webPage, boolean searchLinks, boolean asAdmin, MessageObject retryMessageObject) {
        Throwable e;
        if (peer != 0) {
            Iterator i$;
            Message newMsg = null;
            MessageObject newMsgObj = null;
            int type = -1;
            int lower_id = (int) peer;
            int high_id = (int) (peer >> 32);
            EncryptedChat encryptedChat = null;
            InputPeer sendToPeer = lower_id != 0 ? MessagesController.getInputPeer(lower_id) : null;
            ArrayList<InputUser> sendToPeers = null;
            if (lower_id == 0) {
                encryptedChat = MessagesController.getInstance().getEncryptedChat(Integer.valueOf(high_id));
                if (encryptedChat == null) {
                    if (retryMessageObject != null) {
                        MessagesStorage.getInstance().markMessageAsSendError(retryMessageObject.messageOwner);
                        retryMessageObject.messageOwner.send_state = 2;
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.messageSendError, Integer.valueOf(retryMessageObject.getId()));
                        processSentMessage(retryMessageObject.getId());
                        return;
                    }
                    return;
                }
            } else if (asAdmin && (sendToPeer instanceof TL_inputPeerChannel) && MessagesController.getInstance().getChat(Integer.valueOf(sendToPeer.channel_id)).megagroup) {
                asAdmin = false;
            }
            if (retryMessageObject != null) {
                try {
                    newMsg = retryMessageObject.messageOwner;
                    if (retryMessageObject.isForwarded()) {
                        type = 4;
                    } else if (retryMessageObject.type == 0) {
                        message = newMsg.message;
                        type = 0;
                    } else if (retryMessageObject.type == 4) {
                        location = newMsg.media;
                        type = 1;
                    } else if (retryMessageObject.type == 1) {
                        photo = (TL_photo) newMsg.media.photo;
                        type = 2;
                    } else if (retryMessageObject.type == 3) {
                        type = 3;
                        video = (TL_video) newMsg.media.video;
                    } else if (retryMessageObject.type == 12) {
                        User user2 = new TL_userRequest_old2();
                        try {
                            user2.phone = newMsg.media.phone_number;
                            user2.first_name = newMsg.media.first_name;
                            user2.last_name = newMsg.media.last_name;
                            user2.id = newMsg.media.user_id;
                            type = 6;
                            user = user2;
                        } catch (Exception e2) {
                            e = e2;
                            user = user2;
                            FileLog.m611e("tmessages", e);
                            MessagesStorage.getInstance().markMessageAsSendError(newMsg);
                            if (newMsgObj != null) {
                                newMsgObj.messageOwner.send_state = 2;
                            }
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.messageSendError, Integer.valueOf(newMsg.id));
                            processSentMessage(newMsg.id);
                        }
                    } else if (retryMessageObject.type == 8 || retryMessageObject.type == 9 || retryMessageObject.type == 13) {
                        document = (TL_document) newMsg.media.document;
                        type = 7;
                    } else if (retryMessageObject.type == 2) {
                        audio = (TL_audio) newMsg.media.audio;
                        type = 8;
                    }
                } catch (Exception e3) {
                    e = e3;
                    FileLog.m611e("tmessages", e);
                    MessagesStorage.getInstance().markMessageAsSendError(newMsg);
                    if (newMsgObj != null) {
                        newMsgObj.messageOwner.send_state = 2;
                    }
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.messageSendError, Integer.valueOf(newMsg.id));
                    processSentMessage(newMsg.id);
                }
            }
            if (message != null) {
                if (encryptedChat == null || AndroidUtilities.getPeerLayerVersion(encryptedChat.layer) < 17) {
                    newMsg = new TL_message();
                } else {
                    newMsg = new TL_message_secret();
                }
                if (encryptedChat != null || webPage == null) {
                    newMsg.media = new TL_messageMediaEmpty();
                } else {
                    newMsg.media = new TL_messageMediaWebPage();
                    newMsg.media.webpage = webPage;
                }
                type = 0;
                newMsg.message = message;
            } else if (location != null) {
                if (encryptedChat == null || AndroidUtilities.getPeerLayerVersion(encryptedChat.layer) < 17) {
                    newMsg = new TL_message();
                } else {
                    newMsg = new TL_message_secret();
                }
                newMsg.media = location;
                newMsg.message = "";
                type = 1;
            } else if (photo != null) {
                if (encryptedChat == null || AndroidUtilities.getPeerLayerVersion(encryptedChat.layer) < 17) {
                    newMsg = new TL_message();
                } else {
                    newMsg = new TL_message_secret();
                }
                newMsg.media = new TL_messageMediaPhoto();
                newMsg.media.caption = photo.caption != null ? photo.caption : "";
                newMsg.media.photo = photo;
                type = 2;
                newMsg.message = "-1";
                if (path != null && path.length() > 0) {
                    if (path.startsWith("http")) {
                        newMsg.attachPath = path;
                    }
                }
                newMsg.attachPath = FileLoader.getPathToAttach(((PhotoSize) photo.sizes.get(photo.sizes.size() - 1)).location, true).toString();
            } else if (video != null) {
                if (encryptedChat == null || AndroidUtilities.getPeerLayerVersion(encryptedChat.layer) < 17) {
                    newMsg = new TL_message();
                } else {
                    newMsg = new TL_message_secret();
                }
                newMsg.media = new TL_messageMediaVideo();
                newMsg.media.caption = video.caption != null ? video.caption : "";
                newMsg.media.video = video;
                type = 3;
                if (videoEditedInfo == null) {
                    newMsg.message = "-1";
                } else {
                    newMsg.message = videoEditedInfo.getString();
                }
                newMsg.attachPath = path;
            } else if (user != null) {
                String str;
                if (encryptedChat == null || AndroidUtilities.getPeerLayerVersion(encryptedChat.layer) < 17) {
                    newMsg = new TL_message();
                } else {
                    newMsg = new TL_message_secret();
                }
                newMsg.media = new TL_messageMediaContact();
                newMsg.media.phone_number = user.phone;
                newMsg.media.first_name = user.first_name;
                newMsg.media.last_name = user.last_name;
                newMsg.media.user_id = user.id;
                if (newMsg.media.first_name == null) {
                    str = "";
                    newMsg.media.first_name = str;
                    user.first_name = str;
                }
                if (newMsg.media.last_name == null) {
                    str = "";
                    newMsg.media.last_name = str;
                    user.last_name = str;
                }
                newMsg.message = "";
                type = 6;
            } else if (document != null) {
                if (encryptedChat == null || AndroidUtilities.getPeerLayerVersion(encryptedChat.layer) < 17) {
                    newMsg = new TL_message();
                } else {
                    newMsg = new TL_message_secret();
                }
                newMsg.media = new TL_messageMediaDocument();
                newMsg.media.document = document;
                type = 7;
                newMsg.message = "-1";
                newMsg.attachPath = path;
            } else if (audio != null) {
                if (encryptedChat == null || AndroidUtilities.getPeerLayerVersion(encryptedChat.layer) < 17) {
                    newMsg = new TL_message();
                } else {
                    newMsg = new TL_message_secret();
                }
                newMsg.media = new TL_messageMediaAudio();
                newMsg.media.audio = audio;
                type = 8;
                newMsg.message = "-1";
                newMsg.attachPath = path;
            }
            if (newMsg.attachPath == null) {
                newMsg.attachPath = "";
            }
            int newMessageId = UserConfig.getNewMessageId();
            newMsg.id = newMessageId;
            newMsg.local_id = newMessageId;
            newMsg.out = true;
            if (!asAdmin || sendToPeer == null || sendToPeer.channel_id == 0) {
                newMsg.from_id = UserConfig.getClientUserId();
                newMsg.flags |= 256;
            } else {
                newMsg.from_id = -sendToPeer.channel_id;
            }
            UserConfig.saveConfig(false);
            if (newMsg.random_id == 0) {
                newMsg.random_id = getNextRandomId();
            }
            newMsg.date = ConnectionsManager.getInstance().getCurrentTime();
            newMsg.flags |= 512;
            if (encryptedChat == null && high_id != 1 && (newMsg.media instanceof TL_messageMediaAudio)) {
                newMsg.media_unread = true;
            }
            if (sendToPeer instanceof TL_inputPeerChannel) {
                if (asAdmin) {
                    newMsg.views = 1;
                    newMsg.flags |= 1024;
                }
                Chat chat = MessagesController.getInstance().getChat(Integer.valueOf(sendToPeer.channel_id));
                if (chat != null && chat.megagroup) {
                    newMsg.flags |= Integer.MIN_VALUE;
                }
            } else {
                newMsg.unread = true;
            }
            newMsg.dialog_id = peer;
            if (reply_to_msg != null) {
                newMsg.flags |= 8;
                newMsg.reply_to_msg_id = reply_to_msg.getId();
            }
            if (lower_id == 0) {
                newMsg.to_id = new TL_peerUser();
                if (encryptedChat.participant_id == UserConfig.getClientUserId()) {
                    newMsg.to_id.user_id = encryptedChat.admin_id;
                } else {
                    newMsg.to_id.user_id = encryptedChat.participant_id;
                }
                newMsg.ttl = encryptedChat.ttl;
                if (newMsg.ttl != 0) {
                    if (newMsg.media instanceof TL_messageMediaAudio) {
                        newMsg.ttl = Math.max(encryptedChat.ttl, newMsg.media.audio.duration + 1);
                    } else if (newMsg.media instanceof TL_messageMediaVideo) {
                        newMsg.ttl = Math.max(encryptedChat.ttl, newMsg.media.video.duration + 1);
                    }
                }
            } else if (high_id != 1) {
                newMsg.to_id = MessagesController.getPeer(lower_id);
                if (lower_id > 0) {
                    User sendToUser = MessagesController.getInstance().getUser(Integer.valueOf(lower_id));
                    if (sendToUser == null) {
                        processSentMessage(newMsg.id);
                        return;
                    } else if (sendToUser.bot) {
                        newMsg.unread = false;
                    }
                }
            } else if (this.currentChatInfo == null) {
                MessagesStorage.getInstance().markMessageAsSendError(newMsg);
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.messageSendError, Integer.valueOf(newMsg.id));
                processSentMessage(newMsg.id);
                return;
            } else {
                ArrayList<InputUser> sendToPeers2 = new ArrayList();
                try {
                    i$ = this.currentChatInfo.participants.participants.iterator();
                    while (i$.hasNext()) {
                        InputUser peerUser = MessagesController.getInputUser(MessagesController.getInstance().getUser(Integer.valueOf(((ChatParticipant) i$.next()).user_id)));
                        if (peerUser != null) {
                            sendToPeers2.add(peerUser);
                        }
                    }
                    newMsg.to_id = new TL_peerChat();
                    newMsg.to_id.chat_id = lower_id;
                    sendToPeers = sendToPeers2;
                } catch (Exception e4) {
                    e = e4;
                    sendToPeers = sendToPeers2;
                    FileLog.m611e("tmessages", e);
                    MessagesStorage.getInstance().markMessageAsSendError(newMsg);
                    if (newMsgObj != null) {
                        newMsgObj.messageOwner.send_state = 2;
                    }
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.messageSendError, Integer.valueOf(newMsg.id));
                    processSentMessage(newMsg.id);
                }
            }
            MessageObject messageObject = new MessageObject(newMsg, null, true);
            try {
                messageObject.replyMessageObject = reply_to_msg;
                messageObject.messageOwner.send_state = 1;
                ArrayList<MessageObject> objArr = new ArrayList();
                objArr.add(messageObject);
                ArrayList<Message> arr = new ArrayList();
                arr.add(newMsg);
                MessagesStorage.getInstance().putMessages(arr, false, true, false, 0);
                MessagesController.getInstance().updateInterfaceWithMessages(peer, objArr);
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
                ArrayList<Long> random_ids;
                int a;
                if (type == 0) {
                    if (encryptedChat != null) {
                        TL_decryptedMessage reqSend;
                        if (AndroidUtilities.getPeerLayerVersion(encryptedChat.layer) >= 17) {
                            reqSend = new TL_decryptedMessage();
                            reqSend.ttl = newMsg.ttl;
                        } else {
                            reqSend = new TL_decryptedMessage_old();
                            reqSend.random_bytes = new byte[15];
                            Utilities.random.nextBytes(reqSend.random_bytes);
                        }
                        reqSend.random_id = newMsg.random_id;
                        reqSend.message = message;
                        reqSend.media = new TL_decryptedMessageMediaEmpty();
                        SecretChatHelper.getInstance().performSendEncryptedRequest(reqSend, messageObject.messageOwner, encryptedChat, null, null);
                    } else if (sendToPeers != null) {
                        TL_messages_sendBroadcast reqSend2 = new TL_messages_sendBroadcast();
                        random_ids = new ArrayList();
                        for (a = 0; a < sendToPeers.size(); a++) {
                            random_ids.add(Long.valueOf(Utilities.random.nextLong()));
                        }
                        reqSend2.message = message;
                        reqSend2.contacts = sendToPeers;
                        reqSend2.media = new TL_inputMediaEmpty();
                        reqSend2.random_id = random_ids;
                        performSendMessageRequest(reqSend2, messageObject.messageOwner, null);
                    } else {
                        TL_messages_sendMessage reqSend3 = new TL_messages_sendMessage();
                        reqSend3.message = message;
                        reqSend3.peer = sendToPeer;
                        reqSend3.random_id = newMsg.random_id;
                        if (asAdmin && (sendToPeer instanceof TL_inputPeerChannel)) {
                            reqSend3.broadcast = true;
                        }
                        if (reply_to_msg != null) {
                            reqSend3.flags |= 1;
                            reqSend3.reply_to_msg_id = reply_to_msg.getId();
                        }
                        if (!searchLinks) {
                            reqSend3.no_webpage = true;
                        }
                        performSendMessageRequest(reqSend3, messageObject.messageOwner, null);
                    }
                } else if ((type < 1 || type > 3) && (type < 5 || type > 8)) {
                    if (type == 4) {
                        TL_messages_forwardMessages reqSend4 = new TL_messages_forwardMessages();
                        reqSend4.to_peer = sendToPeer;
                        if (retryMessageObject.messageOwner.ttl != 0) {
                            reqSend4.from_peer = MessagesController.getInputPeer(retryMessageObject.messageOwner.ttl);
                        } else {
                            reqSend4.from_peer = new TL_inputPeerEmpty();
                        }
                        reqSend4.random_id.add(Long.valueOf(newMsg.random_id));
                        if (retryMessageObject.getId() >= 0) {
                            reqSend4.id.add(Integer.valueOf(retryMessageObject.getId()));
                        } else {
                            reqSend4.id.add(Integer.valueOf(retryMessageObject.messageOwner.fwd_msg_id));
                        }
                        if (asAdmin && reqSend4.to_peer.channel_id != 0) {
                            reqSend4.broadcast = true;
                        }
                        performSendMessageRequest(reqSend4, messageObject.messageOwner, null);
                    }
                } else if (encryptedChat == null) {
                    TLObject reqSend5;
                    InputMedia inputMedia = null;
                    DelayedMessage delayedMessage = null;
                    if (type == 1) {
                        if (location instanceof TL_messageMediaVenue) {
                            inputMedia = new TL_inputMediaVenue();
                            inputMedia.address = location.address;
                            inputMedia.title = location.title;
                            inputMedia.provider = location.provider;
                            inputMedia.venue_id = location.venue_id;
                        } else {
                            inputMedia = new TL_inputMediaGeoPoint();
                        }
                        inputMedia.geo_point = new TL_inputGeoPoint();
                        inputMedia.geo_point.lat = location.geo.lat;
                        inputMedia.geo_point._long = location.geo._long;
                    } else if (type == 2) {
                        if (photo.access_hash == 0) {
                            inputMedia = new TL_inputMediaUploadedPhoto();
                            inputMedia.caption = photo.caption != null ? photo.caption : "";
                            r0 = new DelayedMessage();
                            r0.originalPath = originalPath;
                            r0.type = 0;
                            r0.obj = messageObject;
                            if (path != null && path.length() > 0) {
                                if (path.startsWith("http")) {
                                    r0.httpLocation = path;
                                }
                            }
                            r0.location = ((PhotoSize) photo.sizes.get(photo.sizes.size() - 1)).location;
                        } else {
                            media = new TL_inputMediaPhoto();
                            media.id = new TL_inputPhoto();
                            media.caption = photo.caption != null ? photo.caption : "";
                            media.id.id = photo.id;
                            media.id.access_hash = photo.access_hash;
                            inputMedia = media;
                        }
                    } else if (type == 3) {
                        if (video.access_hash == 0) {
                            if (video.thumb.location != null) {
                                inputMedia = new TL_inputMediaUploadedThumbVideo();
                            } else {
                                inputMedia = new TL_inputMediaUploadedVideo();
                            }
                            inputMedia.caption = video.caption != null ? video.caption : "";
                            inputMedia.duration = video.duration;
                            inputMedia.f137w = video.w;
                            inputMedia.f136h = video.h;
                            inputMedia.mime_type = video.mime_type;
                            r0 = new DelayedMessage();
                            r0.originalPath = originalPath;
                            r0.type = 1;
                            r0.obj = messageObject;
                            r0.location = video.thumb.location;
                            r0.videoLocation = video;
                            r0.videoEditedInfo = videoEditedInfo;
                        } else {
                            media = new TL_inputMediaVideo();
                            media.id = new TL_inputVideo();
                            media.caption = video.caption != null ? video.caption : "";
                            media.id.id = video.id;
                            media.id.access_hash = video.access_hash;
                            inputMedia = media;
                        }
                    } else if (type == 6) {
                        inputMedia = new TL_inputMediaContact();
                        inputMedia.phone_number = user.phone;
                        inputMedia.first_name = user.first_name;
                        inputMedia.last_name = user.last_name;
                    } else if (type == 7) {
                        if (document.access_hash == 0) {
                            if (document.thumb.location == null || !(document.thumb.location instanceof TL_fileLocation)) {
                                inputMedia = new TL_inputMediaUploadedDocument();
                            } else {
                                inputMedia = new TL_inputMediaUploadedThumbDocument();
                            }
                            inputMedia.mime_type = document.mime_type;
                            inputMedia.attributes = document.attributes;
                            r0 = new DelayedMessage();
                            r0.originalPath = originalPath;
                            r0.type = 2;
                            r0.obj = messageObject;
                            if (path != null && path.length() > 0) {
                                if (path.startsWith("http")) {
                                    r0.httpLocation = path;
                                }
                            }
                            r0.documentLocation = document;
                            r0.location = document.thumb.location;
                        } else {
                            media = new TL_inputMediaDocument();
                            media.id = new TL_inputDocument();
                            media.id.id = document.id;
                            media.id.access_hash = document.access_hash;
                            inputMedia = media;
                        }
                    } else if (type == 8) {
                        if (audio.access_hash == 0) {
                            inputMedia = new TL_inputMediaUploadedAudio();
                            inputMedia.duration = audio.duration;
                            inputMedia.mime_type = audio.mime_type;
                            r0 = new DelayedMessage();
                            r0.type = 3;
                            r0.obj = messageObject;
                            r0.audioLocation = audio;
                        } else {
                            media = new TL_inputMediaAudio();
                            media.id = new TL_inputAudio();
                            media.id.id = audio.id;
                            media.id.access_hash = audio.access_hash;
                            inputMedia = media;
                        }
                    }
                    TLObject request;
                    if (sendToPeers != null) {
                        request = new TL_messages_sendBroadcast();
                        random_ids = new ArrayList();
                        for (a = 0; a < sendToPeers.size(); a++) {
                            random_ids.add(Long.valueOf(Utilities.random.nextLong()));
                        }
                        request.contacts = sendToPeers;
                        request.media = inputMedia;
                        request.random_id = random_ids;
                        request.message = "";
                        if (delayedMessage != null) {
                            delayedMessage.sendRequest = request;
                        }
                        reqSend5 = request;
                    } else {
                        request = new TL_messages_sendMedia();
                        request.peer = sendToPeer;
                        request.random_id = newMsg.random_id;
                        request.media = inputMedia;
                        if (asAdmin && (sendToPeer instanceof TL_inputPeerChannel)) {
                            request.broadcast = true;
                        }
                        if (reply_to_msg != null) {
                            request.flags |= 1;
                            request.reply_to_msg_id = reply_to_msg.getId();
                        }
                        if (delayedMessage != null) {
                            delayedMessage.sendRequest = request;
                        }
                        reqSend5 = request;
                    }
                    if (type == 1) {
                        performSendMessageRequest(reqSend5, messageObject.messageOwner, null);
                    } else if (type == 2) {
                        if (photo.access_hash == 0) {
                            performSendDelayedMessage(delayedMessage);
                        } else {
                            performSendMessageRequest(reqSend5, messageObject.messageOwner, null);
                        }
                    } else if (type == 3) {
                        if (video.access_hash == 0) {
                            performSendDelayedMessage(delayedMessage);
                        } else {
                            performSendMessageRequest(reqSend5, messageObject.messageOwner, null);
                        }
                    } else if (type == 6) {
                        performSendMessageRequest(reqSend5, messageObject.messageOwner, null);
                    } else if (type == 7) {
                        if (document.access_hash == 0) {
                            performSendDelayedMessage(delayedMessage);
                        } else {
                            performSendMessageRequest(reqSend5, messageObject.messageOwner, originalPath);
                        }
                    } else if (type == 8) {
                        if (audio.access_hash == 0) {
                            performSendDelayedMessage(delayedMessage);
                        } else {
                            performSendMessageRequest(reqSend5, messageObject.messageOwner, null);
                        }
                    }
                } else {
                    DecryptedMessage reqSend6;
                    if (AndroidUtilities.getPeerLayerVersion(encryptedChat.layer) >= 17) {
                        reqSend6 = new TL_decryptedMessage();
                        reqSend6.ttl = newMsg.ttl;
                    } else {
                        reqSend6 = new TL_decryptedMessage_old();
                        reqSend6.random_bytes = new byte[15];
                        Utilities.random.nextBytes(reqSend6.random_bytes);
                    }
                    reqSend6.random_id = newMsg.random_id;
                    reqSend6.message = "";
                    if (type == 1) {
                        reqSend6.media = new TL_decryptedMessageMediaGeoPoint();
                        reqSend6.media.lat = location.geo.lat;
                        reqSend6.media._long = location.geo._long;
                        SecretChatHelper.getInstance().performSendEncryptedRequest(reqSend6, messageObject.messageOwner, encryptedChat, null, null);
                    } else if (type == 2) {
                        PhotoSize small = (PhotoSize) photo.sizes.get(0);
                        PhotoSize big = (PhotoSize) photo.sizes.get(photo.sizes.size() - 1);
                        reqSend6.media = new TL_decryptedMessageMediaPhoto();
                        ImageLoader.fillPhotoSizeWithBytes(small);
                        if (small.bytes != null) {
                            ((TL_decryptedMessageMediaPhoto) reqSend6.media).thumb = small.bytes;
                        } else {
                            ((TL_decryptedMessageMediaPhoto) reqSend6.media).thumb = new byte[0];
                        }
                        reqSend6.media.thumb_h = small.f138h;
                        reqSend6.media.thumb_w = small.f139w;
                        reqSend6.media.f133w = big.f139w;
                        reqSend6.media.f132h = big.f138h;
                        reqSend6.media.size = big.size;
                        if (big.location.key == null) {
                            r0 = new DelayedMessage();
                            r0.originalPath = originalPath;
                            r0.sendEncryptedRequest = reqSend6;
                            r0.type = 0;
                            r0.obj = messageObject;
                            r0.encryptedChat = encryptedChat;
                            if (path != null && path.length() > 0) {
                                if (path.startsWith("http")) {
                                    r0.httpLocation = path;
                                    performSendDelayedMessage(r0);
                                }
                            }
                            r0.location = ((PhotoSize) photo.sizes.get(photo.sizes.size() - 1)).location;
                            performSendDelayedMessage(r0);
                        } else {
                            encryptedFile = new TL_inputEncryptedFile();
                            encryptedFile.id = big.location.volume_id;
                            encryptedFile.access_hash = big.location.secret;
                            reqSend6.media.key = big.location.key;
                            reqSend6.media.iv = big.location.iv;
                            SecretChatHelper.getInstance().performSendEncryptedRequest(reqSend6, messageObject.messageOwner, encryptedChat, encryptedFile, null);
                        }
                    } else if (type == 3) {
                        ImageLoader.fillPhotoSizeWithBytes(video.thumb);
                        if (AndroidUtilities.getPeerLayerVersion(encryptedChat.layer) >= 17) {
                            reqSend6.media = new TL_decryptedMessageMediaVideo();
                            if (video.thumb == null || video.thumb.bytes == null) {
                                ((TL_decryptedMessageMediaVideo) reqSend6.media).thumb = new byte[0];
                            } else {
                                ((TL_decryptedMessageMediaVideo) reqSend6.media).thumb = video.thumb.bytes;
                            }
                        } else {
                            reqSend6.media = new TL_decryptedMessageMediaVideo_old();
                            if (video.thumb == null || video.thumb.bytes == null) {
                                ((TL_decryptedMessageMediaVideo_old) reqSend6.media).thumb = new byte[0];
                            } else {
                                ((TL_decryptedMessageMediaVideo_old) reqSend6.media).thumb = video.thumb.bytes;
                            }
                        }
                        reqSend6.media.duration = video.duration;
                        reqSend6.media.size = video.size;
                        reqSend6.media.f133w = video.w;
                        reqSend6.media.f132h = video.h;
                        reqSend6.media.thumb_h = video.thumb.f138h;
                        reqSend6.media.thumb_w = video.thumb.f139w;
                        reqSend6.media.mime_type = "video/mp4";
                        if (video.access_hash == 0) {
                            r0 = new DelayedMessage();
                            r0.originalPath = originalPath;
                            r0.sendEncryptedRequest = reqSend6;
                            r0.type = 1;
                            r0.obj = messageObject;
                            r0.encryptedChat = encryptedChat;
                            r0.videoLocation = video;
                            r0.videoEditedInfo = videoEditedInfo;
                            performSendDelayedMessage(r0);
                        } else {
                            encryptedFile = new TL_inputEncryptedFile();
                            encryptedFile.id = video.id;
                            encryptedFile.access_hash = video.access_hash;
                            reqSend6.media.key = video.key;
                            reqSend6.media.iv = video.iv;
                            SecretChatHelper.getInstance().performSendEncryptedRequest(reqSend6, messageObject.messageOwner, encryptedChat, encryptedFile, null);
                        }
                    } else if (type == 6) {
                        reqSend6.media = new TL_decryptedMessageMediaContact();
                        reqSend6.media.phone_number = user.phone;
                        reqSend6.media.first_name = user.first_name;
                        reqSend6.media.last_name = user.last_name;
                        reqSend6.media.user_id = user.id;
                        SecretChatHelper.getInstance().performSendEncryptedRequest(reqSend6, messageObject.messageOwner, encryptedChat, null, null);
                    } else if (type == 7) {
                        boolean isSticker = false;
                        i$ = document.attributes.iterator();
                        while (i$.hasNext()) {
                            if (((DocumentAttribute) i$.next()) instanceof TL_documentAttributeSticker) {
                                isSticker = true;
                            }
                        }
                        if (isSticker) {
                            reqSend6.media = new TL_decryptedMessageMediaExternalDocument();
                            reqSend6.media.id = document.id;
                            reqSend6.media.date = document.date;
                            reqSend6.media.access_hash = document.access_hash;
                            reqSend6.media.mime_type = document.mime_type;
                            reqSend6.media.size = document.size;
                            reqSend6.media.dc_id = document.dc_id;
                            reqSend6.media.attributes = document.attributes;
                            if (document.thumb == null) {
                                ((TL_decryptedMessageMediaExternalDocument) reqSend6.media).thumb = new TL_photoSizeEmpty();
                            } else {
                                ((TL_decryptedMessageMediaExternalDocument) reqSend6.media).thumb = document.thumb;
                            }
                            SecretChatHelper.getInstance().performSendEncryptedRequest(reqSend6, messageObject.messageOwner, encryptedChat, null, null);
                        } else {
                            ImageLoader.fillPhotoSizeWithBytes(document.thumb);
                            reqSend6.media = new TL_decryptedMessageMediaDocument();
                            reqSend6.media.size = document.size;
                            if (document.thumb == null || document.thumb.bytes == null) {
                                ((TL_decryptedMessageMediaDocument) reqSend6.media).thumb = new byte[0];
                                reqSend6.media.thumb_h = 0;
                                reqSend6.media.thumb_w = 0;
                            } else {
                                ((TL_decryptedMessageMediaDocument) reqSend6.media).thumb = document.thumb.bytes;
                                reqSend6.media.thumb_h = document.thumb.f138h;
                                reqSend6.media.thumb_w = document.thumb.f139w;
                            }
                            reqSend6.media.file_name = FileLoader.getDocumentFileName(document);
                            reqSend6.media.mime_type = document.mime_type;
                            if (document.access_hash == 0) {
                                r0 = new DelayedMessage();
                                r0.originalPath = originalPath;
                                r0.sendEncryptedRequest = reqSend6;
                                r0.type = 2;
                                r0.obj = messageObject;
                                r0.encryptedChat = encryptedChat;
                                if (path != null && path.length() > 0) {
                                    if (path.startsWith("http")) {
                                        r0.httpLocation = path;
                                    }
                                }
                                r0.documentLocation = document;
                                performSendDelayedMessage(r0);
                            } else {
                                encryptedFile = new TL_inputEncryptedFile();
                                encryptedFile.id = document.id;
                                encryptedFile.access_hash = document.access_hash;
                                reqSend6.media.key = document.key;
                                reqSend6.media.iv = document.iv;
                                SecretChatHelper.getInstance().performSendEncryptedRequest(reqSend6, messageObject.messageOwner, encryptedChat, encryptedFile, null);
                            }
                        }
                    } else if (type == 8) {
                        if (AndroidUtilities.getPeerLayerVersion(encryptedChat.layer) >= 17) {
                            reqSend6.media = new TL_decryptedMessageMediaAudio();
                        } else {
                            reqSend6.media = new TL_decryptedMessageMediaAudio_old();
                        }
                        reqSend6.media.duration = audio.duration;
                        reqSend6.media.size = audio.size;
                        reqSend6.media.mime_type = "audio/ogg";
                        r0 = new DelayedMessage();
                        r0.sendEncryptedRequest = reqSend6;
                        r0.type = 3;
                        r0.obj = messageObject;
                        r0.encryptedChat = encryptedChat;
                        r0.audioLocation = audio;
                        performSendDelayedMessage(r0);
                    }
                }
                newMsgObj = messageObject;
            } catch (Exception e5) {
                e = e5;
                newMsgObj = messageObject;
                FileLog.m611e("tmessages", e);
                MessagesStorage.getInstance().markMessageAsSendError(newMsg);
                if (newMsgObj != null) {
                    newMsgObj.messageOwner.send_state = 2;
                }
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.messageSendError, Integer.valueOf(newMsg.id));
                processSentMessage(newMsg.id);
            }
        }
    }

    private void performSendDelayedMessage(DelayedMessage message) {
        String location;
        if (message.type == 0) {
            if (message.httpLocation != null) {
                putToDelayedMessages(message.httpLocation, message);
                ImageLoader.getInstance().loadHttpFile(message.httpLocation, "jpg");
                return;
            }
            location = FileLoader.getPathToAttach(message.location, true).toString();
            putToDelayedMessages(location, message);
            if (message.sendRequest != null) {
                FileLoader.getInstance().uploadFile(location, false, true);
            } else {
                FileLoader.getInstance().uploadFile(location, true, true);
            }
        } else if (message.type == 1) {
            if (message.videoEditedInfo != null) {
                location = message.obj.messageOwner.attachPath;
                if (location == null) {
                    location = FileLoader.getInstance().getDirectory(4) + "/" + message.videoLocation.id + ".mp4";
                }
                putToDelayedMessages(location, message);
                MediaController.getInstance().scheduleVideoConvert(message.obj);
            } else if (message.sendRequest != null) {
                if (message.sendRequest instanceof TL_messages_sendMedia) {
                    media = ((TL_messages_sendMedia) message.sendRequest).media;
                } else {
                    media = ((TL_messages_sendBroadcast) message.sendRequest).media;
                }
                if (media.file == null) {
                    location = message.obj.messageOwner.attachPath;
                    if (location == null) {
                        location = FileLoader.getInstance().getDirectory(4) + "/" + message.videoLocation.id + ".mp4";
                    }
                    putToDelayedMessages(location, message);
                    if (message.obj.videoEditedInfo != null) {
                        FileLoader.getInstance().uploadFile(location, false, false, message.videoLocation.size);
                        return;
                    } else {
                        FileLoader.getInstance().uploadFile(location, false, false);
                        return;
                    }
                }
                location = FileLoader.getInstance().getDirectory(4) + "/" + message.location.volume_id + "_" + message.location.local_id + ".jpg";
                putToDelayedMessages(location, message);
                FileLoader.getInstance().uploadFile(location, false, true);
            } else {
                location = message.obj.messageOwner.attachPath;
                if (location == null) {
                    location = FileLoader.getInstance().getDirectory(4) + "/" + message.videoLocation.id + ".mp4";
                }
                putToDelayedMessages(location, message);
                if (message.obj.videoEditedInfo != null) {
                    FileLoader.getInstance().uploadFile(location, true, false, message.videoLocation.size);
                } else {
                    FileLoader.getInstance().uploadFile(location, true, false);
                }
            }
        } else if (message.type == 2) {
            if (message.httpLocation != null) {
                putToDelayedMessages(message.httpLocation, message);
                ImageLoader.getInstance().loadHttpFile(message.httpLocation, "gif");
            } else if (message.sendRequest != null) {
                if (message.sendRequest instanceof TL_messages_sendMedia) {
                    media = ((TL_messages_sendMedia) message.sendRequest).media;
                } else {
                    media = ((TL_messages_sendBroadcast) message.sendRequest).media;
                }
                if (media.file == null) {
                    location = message.obj.messageOwner.attachPath;
                    putToDelayedMessages(location, message);
                    if (message.sendRequest != null) {
                        FileLoader.getInstance().uploadFile(location, false, false);
                    } else {
                        FileLoader.getInstance().uploadFile(location, true, false);
                    }
                } else if (media.thumb == null && message.location != null) {
                    location = FileLoader.getInstance().getDirectory(4) + "/" + message.location.volume_id + "_" + message.location.local_id + ".jpg";
                    putToDelayedMessages(location, message);
                    FileLoader.getInstance().uploadFile(location, false, true);
                }
            } else {
                location = message.obj.messageOwner.attachPath;
                putToDelayedMessages(location, message);
                FileLoader.getInstance().uploadFile(location, true, false);
            }
        } else if (message.type == 3) {
            location = message.obj.messageOwner.attachPath;
            putToDelayedMessages(location, message);
            if (message.sendRequest != null) {
                FileLoader.getInstance().uploadFile(location, false, true);
            } else {
                FileLoader.getInstance().uploadFile(location, true, true);
            }
        }
    }

    protected void stopVideoService(final String path) {
        MessagesStorage.getInstance().getStorageQueue().postRunnable(new Runnable() {

            class C05911 implements Runnable {
                C05911() {
                }

                public void run() {
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.stopEncodingService, path);
                }
            }

            public void run() {
                AndroidUtilities.runOnUIThread(new C05911());
            }
        });
    }

    protected void putToSendingMessages(Message message) {
        this.sendingMessages.put(Integer.valueOf(message.id), message);
    }

    protected void removeFromSendingMessages(int mid) {
        this.sendingMessages.remove(Integer.valueOf(mid));
    }

    public boolean isSendingMessage(int mid) {
        return this.sendingMessages.containsKey(Integer.valueOf(mid));
    }

    private void performSendMessageRequest(final TLObject req, final Message newMsgObj, final String originalPath) {
        int i;
        putToSendingMessages(newMsgObj);
        ConnectionsManager instance = ConnectionsManager.getInstance();
        RequestDelegate c14635 = new RequestDelegate() {

            class C05963 implements Runnable {
                C05963() {
                }

                public void run() {
                    newMsgObj.send_state = 2;
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.messageSendError, Integer.valueOf(newMsgObj.id));
                    SendMessagesHelper.this.processSentMessage(newMsgObj.id);
                    if (newMsgObj.media instanceof TL_messageMediaVideo) {
                        SendMessagesHelper.this.stopVideoService(newMsgObj.attachPath);
                    }
                    SendMessagesHelper.this.removeFromSendingMessages(newMsgObj.id);
                }
            }

            public void run(TLObject response, TL_error error) {
                boolean isSentError = false;
                if (error == null) {
                    final int oldId = newMsgObj.id;
                    final boolean isBroadcast = req instanceof TL_messages_sendBroadcast;
                    final ArrayList<Message> sentMessages = new ArrayList();
                    final String attachPath = newMsgObj.attachPath;
                    Message message;
                    if (response instanceof TL_updateShortSentMessage) {
                        TL_updateShortSentMessage res = (TL_updateShortSentMessage) response;
                        message = newMsgObj;
                        Message message2 = newMsgObj;
                        int i = res.id;
                        message2.id = i;
                        message.local_id = i;
                        newMsgObj.date = res.date;
                        newMsgObj.entities = res.entities;
                        newMsgObj.out = res.out;
                        newMsgObj.unread = res.unread;
                        if (res.media != null) {
                            newMsgObj.media = res.media;
                            message = newMsgObj;
                            message.flags |= 512;
                        }
                        if (!newMsgObj.entities.isEmpty()) {
                            message = newMsgObj;
                            message.flags |= 128;
                        }
                        MessagesController.getInstance().processNewDifferenceParams(-1, res.pts, res.date, res.pts_count);
                        sentMessages.add(newMsgObj);
                    } else if (response instanceof Updates) {
                        boolean ok = false;
                        Iterator i$ = ((Updates) response).updates.iterator();
                        while (i$.hasNext()) {
                            Update update = (Update) i$.next();
                            if (update instanceof TL_updateNewMessage) {
                                TL_updateNewMessage newMessage = (TL_updateNewMessage) update;
                                sentMessages.add(newMessage.message);
                                newMsgObj.id = newMessage.message.id;
                                SendMessagesHelper.this.processSentMessage(newMsgObj, newMessage.message, originalPath);
                                MessagesController.getInstance().processNewDifferenceParams(-1, newMessage.pts, -1, newMessage.pts_count);
                                ok = true;
                                break;
                            } else if (update instanceof TL_updateNewChannelMessage) {
                                TL_updateNewChannelMessage newMessage2 = (TL_updateNewChannelMessage) update;
                                sentMessages.add(newMessage2.message);
                                newMsgObj.id = newMessage2.message.id;
                                if ((newMsgObj.flags & Integer.MIN_VALUE) != 0) {
                                    message = newMessage2.message;
                                    message.flags |= Integer.MIN_VALUE;
                                }
                                SendMessagesHelper.this.processSentMessage(newMsgObj, newMessage2.message, originalPath);
                                MessagesController.getInstance().processNewChannelDifferenceParams(newMessage2.pts, newMessage2.pts_count, newMessage2.message.to_id.channel_id);
                                ok = true;
                            }
                        }
                        if (!ok) {
                            isSentError = true;
                        }
                    }
                    if (!isSentError) {
                        MessagesStorage.getInstance().getStorageQueue().postRunnable(new Runnable() {

                            class C05931 implements Runnable {
                                C05931() {
                                }

                                public void run() {
                                    newMsgObj.send_state = 0;
                                    if (isBroadcast) {
                                        Iterator i$ = sentMessages.iterator();
                                        while (i$.hasNext()) {
                                            Message message = (Message) i$.next();
                                            ArrayList<MessageObject> arr = new ArrayList();
                                            MessageObject messageObject = new MessageObject(message, null, false);
                                            arr.add(messageObject);
                                            MessagesController.getInstance().updateInterfaceWithMessages(messageObject.getDialogId(), arr, true);
                                        }
                                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
                                    }
                                    NotificationCenter instance = NotificationCenter.getInstance();
                                    int i = NotificationCenter.messageReceivedByServer;
                                    Object[] objArr = new Object[4];
                                    objArr[0] = Integer.valueOf(oldId);
                                    objArr[1] = Integer.valueOf(isBroadcast ? oldId : newMsgObj.id);
                                    objArr[2] = newMsgObj;
                                    objArr[3] = Long.valueOf(newMsgObj.dialog_id);
                                    instance.postNotificationName(i, objArr);
                                    SendMessagesHelper.this.processSentMessage(oldId);
                                    SendMessagesHelper.this.removeFromSendingMessages(oldId);
                                }
                            }

                            public void run() {
                                MessagesStorage.getInstance().updateMessageStateAndId(newMsgObj.random_id, Integer.valueOf(oldId), isBroadcast ? oldId : newMsgObj.id, 0, false, newMsgObj.to_id.channel_id);
                                MessagesStorage.getInstance().putMessages(sentMessages, true, false, isBroadcast, 0);
                                if (isBroadcast) {
                                    ArrayList<Message> currentMessage = new ArrayList();
                                    currentMessage.add(newMsgObj);
                                    newMsgObj.send_state = 0;
                                    MessagesStorage.getInstance().putMessages(currentMessage, true, false, false, 0);
                                }
                                AndroidUtilities.runOnUIThread(new C05931());
                                if (newMsgObj.media instanceof TL_messageMediaVideo) {
                                    SendMessagesHelper.this.stopVideoService(attachPath);
                                }
                            }
                        });
                    }
                } else {
                    final TL_error tL_error = error;
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        public void run() {
                            if (tL_error.text.equals("PEER_FLOOD")) {
                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.needShowAlert, Integer.valueOf(0));
                            }
                        }
                    });
                    isSentError = true;
                }
                if (isSentError) {
                    MessagesStorage.getInstance().markMessageAsSendError(newMsgObj);
                    AndroidUtilities.runOnUIThread(new C05963());
                }
            }
        };
        QuickAckDelegate c14646 = new QuickAckDelegate() {
            public void run() {
                final int msg_id = newMsgObj.id;
                AndroidUtilities.runOnUIThread(new Runnable() {
                    public void run() {
                        newMsgObj.send_state = 0;
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.messageReceivedByAck, Integer.valueOf(msg_id));
                    }
                });
            }
        };
        if (req instanceof TL_messages_sendMessage) {
            i = 128;
        } else {
            i = 0;
        }
        instance.sendRequest(req, c14635, c14646, i | 68);
    }

    private void processSentMessage(Message newMsg, Message sentMessage, String originalPath) {
        if (sentMessage != null) {
            PhotoSize size;
            PhotoSize size2;
            String fileName;
            String fileName2;
            File cacheFile;
            File cacheFile2;
            if ((sentMessage.media instanceof TL_messageMediaPhoto) && sentMessage.media.photo != null && (newMsg.media instanceof TL_messageMediaPhoto) && newMsg.media.photo != null) {
                MessagesStorage.getInstance().putSentFile(originalPath, sentMessage.media.photo, 0);
                Iterator it = sentMessage.media.photo.sizes.iterator();
                while (it.hasNext()) {
                    size = (PhotoSize) it.next();
                    if (!(size == null || size.location == null || (size instanceof TL_photoSizeEmpty) || size.type == null)) {
                        Iterator i$ = newMsg.media.photo.sizes.iterator();
                        while (i$.hasNext()) {
                            size2 = (PhotoSize) i$.next();
                            if (!(size2 == null || size2.location == null || size2.type == null)) {
                                if ((size2.location.volume_id == -2147483648L && size.type.equals(size2.type)) || (size.f139w == size2.f139w && size.f138h == size2.f138h)) {
                                    fileName = size2.location.volume_id + "_" + size2.location.local_id;
                                    fileName2 = size.location.volume_id + "_" + size.location.local_id;
                                    if (!fileName.equals(fileName2)) {
                                        cacheFile = new File(FileLoader.getInstance().getDirectory(4), fileName + ".jpg");
                                        if (sentMessage.media.photo.sizes.size() == 1 || size.f139w > 90 || size.f138h > 90) {
                                            cacheFile2 = FileLoader.getPathToAttach(size);
                                        } else {
                                            cacheFile2 = new File(FileLoader.getInstance().getDirectory(4), fileName2 + ".jpg");
                                        }
                                        cacheFile.renameTo(cacheFile2);
                                        ImageLoader.getInstance().replaceImageInCache(fileName, fileName2, size.location);
                                        size2.location = size.location;
                                    }
                                }
                            }
                        }
                    }
                }
                sentMessage.message = newMsg.message;
                sentMessage.attachPath = newMsg.attachPath;
                newMsg.media.photo.id = sentMessage.media.photo.id;
                newMsg.media.photo.access_hash = sentMessage.media.photo.access_hash;
            } else if ((sentMessage.media instanceof TL_messageMediaVideo) && sentMessage.media.video != null && (newMsg.media instanceof TL_messageMediaVideo) && newMsg.media.video != null) {
                MessagesStorage.getInstance().putSentFile(originalPath, sentMessage.media.video, 2);
                size2 = newMsg.media.video.thumb;
                size = sentMessage.media.video.thumb;
                if (!(size2 == null || size2.location == null || size2.location.volume_id != -2147483648L || size == null || size.location == null || (size instanceof TL_photoSizeEmpty) || (size2 instanceof TL_photoSizeEmpty))) {
                    fileName = size2.location.volume_id + "_" + size2.location.local_id;
                    fileName2 = size.location.volume_id + "_" + size.location.local_id;
                    if (!fileName.equals(fileName2)) {
                        new File(FileLoader.getInstance().getDirectory(4), fileName + ".jpg").renameTo(new File(FileLoader.getInstance().getDirectory(4), fileName2 + ".jpg"));
                        ImageLoader.getInstance().replaceImageInCache(fileName, fileName2, size.location);
                        size2.location = size.location;
                    }
                }
                sentMessage.message = newMsg.message;
                newMsg.media.video.dc_id = sentMessage.media.video.dc_id;
                newMsg.media.video.id = sentMessage.media.video.id;
                newMsg.media.video.access_hash = sentMessage.media.video.access_hash;
                if (newMsg.attachPath == null || !newMsg.attachPath.startsWith(FileLoader.getInstance().getDirectory(4).getAbsolutePath())) {
                    sentMessage.attachPath = newMsg.attachPath;
                } else if (!new File(newMsg.attachPath).renameTo(FileLoader.getPathToAttach(newMsg.media.video))) {
                    sentMessage.attachPath = newMsg.attachPath;
                }
            } else if ((sentMessage.media instanceof TL_messageMediaDocument) && sentMessage.media.document != null && (newMsg.media instanceof TL_messageMediaDocument) && newMsg.media.document != null) {
                MessagesStorage.getInstance().putSentFile(originalPath, sentMessage.media.document, 1);
                size2 = newMsg.media.document.thumb;
                size = sentMessage.media.document.thumb;
                if (size2 != null && size2.location != null && size2.location.volume_id == -2147483648L && size != null && size.location != null && !(size instanceof TL_photoSizeEmpty) && !(size2 instanceof TL_photoSizeEmpty)) {
                    fileName = size2.location.volume_id + "_" + size2.location.local_id;
                    fileName2 = size.location.volume_id + "_" + size.location.local_id;
                    if (!fileName.equals(fileName2)) {
                        new File(FileLoader.getInstance().getDirectory(4), fileName + ".jpg").renameTo(new File(FileLoader.getInstance().getDirectory(4), fileName2 + ".jpg"));
                        ImageLoader.getInstance().replaceImageInCache(fileName, fileName2, size.location);
                        size2.location = size.location;
                    }
                } else if (!(!MessageObject.isStickerMessage(sentMessage) || size2 == null || size2.location == null)) {
                    size.location = size2.location;
                }
                newMsg.media.document.dc_id = sentMessage.media.document.dc_id;
                newMsg.media.document.id = sentMessage.media.document.id;
                newMsg.media.document.access_hash = sentMessage.media.document.access_hash;
                newMsg.media.document.attributes = sentMessage.media.document.attributes;
                if (newMsg.attachPath == null || !newMsg.attachPath.startsWith(FileLoader.getInstance().getDirectory(4).getAbsolutePath())) {
                    sentMessage.attachPath = newMsg.attachPath;
                    sentMessage.message = newMsg.message;
                    return;
                }
                cacheFile = new File(newMsg.attachPath);
                cacheFile2 = FileLoader.getPathToAttach(sentMessage.media.document);
                if (cacheFile.renameTo(cacheFile2)) {
                    newMsg.attachPath = "";
                    if (originalPath != null && originalPath.startsWith("http")) {
                        MessagesStorage.getInstance().addRecentLocalFile(originalPath, cacheFile2.toString());
                        return;
                    }
                    return;
                }
                sentMessage.attachPath = newMsg.attachPath;
                sentMessage.message = newMsg.message;
            } else if ((sentMessage.media instanceof TL_messageMediaAudio) && sentMessage.media.audio != null && (newMsg.media instanceof TL_messageMediaAudio) && newMsg.media.audio != null) {
                sentMessage.message = newMsg.message;
                fileName = newMsg.media.audio.dc_id + "_" + newMsg.media.audio.id + ".ogg";
                newMsg.media.audio.dc_id = sentMessage.media.audio.dc_id;
                newMsg.media.audio.id = sentMessage.media.audio.id;
                newMsg.media.audio.access_hash = sentMessage.media.audio.access_hash;
                if (!fileName.equals(sentMessage.media.audio.dc_id + "_" + sentMessage.media.audio.id + ".ogg") && !new File(FileLoader.getInstance().getDirectory(4), fileName).renameTo(FileLoader.getPathToAttach(sentMessage.media.audio))) {
                    sentMessage.attachPath = newMsg.attachPath;
                }
            } else if ((sentMessage.media instanceof TL_messageMediaContact) && (newMsg.media instanceof TL_messageMediaContact)) {
                newMsg.media = sentMessage.media;
            } else if (sentMessage.media instanceof TL_messageMediaWebPage) {
                newMsg.media = sentMessage.media;
            }
        }
    }

    private void putToDelayedMessages(String location, DelayedMessage message) {
        ArrayList<DelayedMessage> arrayList = (ArrayList) this.delayedMessages.get(location);
        if (arrayList == null) {
            arrayList = new ArrayList();
            this.delayedMessages.put(location, arrayList);
        }
        arrayList.add(message);
    }

    protected ArrayList<DelayedMessage> getDelayedMessages(String location) {
        return (ArrayList) this.delayedMessages.get(location);
    }

    protected long getNextRandomId() {
        long val = 0;
        while (val == 0) {
            val = Utilities.random.nextLong();
        }
        return val;
    }

    public void checkUnsentMessages() {
        MessagesStorage.getInstance().getUnsentMessages(LocationStatusCodes.GEOFENCE_NOT_AVAILABLE);
    }

    protected void processUnsentMessages(ArrayList<Message> messages, ArrayList<User> users, ArrayList<Chat> chats, ArrayList<EncryptedChat> encryptedChats) {
        final ArrayList<User> arrayList = users;
        final ArrayList<Chat> arrayList2 = chats;
        final ArrayList<EncryptedChat> arrayList3 = encryptedChats;
        final ArrayList<Message> arrayList4 = messages;
        AndroidUtilities.runOnUIThread(new Runnable() {
            public void run() {
                MessagesController.getInstance().putUsers(arrayList, true);
                MessagesController.getInstance().putChats(arrayList2, true);
                MessagesController.getInstance().putEncryptedChats(arrayList3, true);
                Iterator i$ = arrayList4.iterator();
                while (i$.hasNext()) {
                    SendMessagesHelper.this.retrySendMessage(new MessageObject((Message) i$.next(), null, false), true);
                }
            }
        });
    }

    public TL_photo generatePhotoSizes(String path, Uri imageUri) {
        Bitmap bitmap = ImageLoader.loadBitmap(path, imageUri, (float) AndroidUtilities.getPhotoSize(), (float) AndroidUtilities.getPhotoSize(), true);
        if (bitmap == null && AndroidUtilities.getPhotoSize() != 800) {
            bitmap = ImageLoader.loadBitmap(path, imageUri, 800.0f, 800.0f, true);
        }
        ArrayList<PhotoSize> sizes = new ArrayList();
        PhotoSize size = ImageLoader.scaleAndSaveImage(bitmap, 90.0f, 90.0f, 55, true);
        if (size != null) {
            sizes.add(size);
        }
        size = ImageLoader.scaleAndSaveImage(bitmap, (float) AndroidUtilities.getPhotoSize(), (float) AndroidUtilities.getPhotoSize(), 80, false, 101, 101);
        if (size != null) {
            sizes.add(size);
        }
        if (bitmap != null) {
            bitmap.recycle();
        }
        if (sizes.isEmpty()) {
            return null;
        }
        UserConfig.saveConfig(false);
        TL_photo photo = new TL_photo();
        photo.date = ConnectionsManager.getInstance().getCurrentTime();
        photo.sizes = sizes;
        return photo;
    }

    private static boolean prepareSendingDocumentInternal(String path, String originalPath, Uri uri, String mime, long dialog_id, MessageObject reply_to_msg, boolean asAdmin) {
        if ((path == null || path.length() == 0) && uri == null) {
            return false;
        }
        MimeTypeMap myMime = MimeTypeMap.getSingleton();
        TL_documentAttributeAudio attributeAudio = null;
        if (uri != null) {
            String extension = null;
            if (mime != null) {
                extension = myMime.getExtensionFromMimeType(mime);
            }
            if (extension == null) {
                extension = "txt";
            }
            path = MediaController.copyDocumentToCache(uri, extension);
            if (path == null) {
                return false;
            }
        }
        File file = new File(path);
        if (!file.exists() || file.length() == 0) {
            return false;
        }
        boolean isEncrypted = ((int) dialog_id) == 0;
        boolean allowSticker = !isEncrypted;
        String name = file.getName();
        String ext = "";
        int idx = path.lastIndexOf(".");
        if (idx != -1) {
            ext = path.substring(idx + 1);
        }
        if (ext.toLowerCase().equals("mp3") || ext.toLowerCase().equals("m4a")) {
            AudioInfo audioInfo = AudioInfo.getAudioInfo(file);
            if (!(audioInfo == null || audioInfo.getDuration() == 0)) {
                if (isEncrypted) {
                    attributeAudio = new TL_documentAttributeAudio_old();
                } else {
                    attributeAudio = new TL_documentAttributeAudio();
                }
                attributeAudio.duration = (int) (audioInfo.getDuration() / 1000);
                attributeAudio.title = audioInfo.getTitle();
                attributeAudio.performer = audioInfo.getArtist();
                if (attributeAudio.title == null) {
                    attributeAudio.title = "";
                }
                if (attributeAudio.performer == null) {
                    attributeAudio.performer = "";
                }
            }
        }
        if (originalPath != null) {
            if (attributeAudio != null) {
                originalPath = originalPath + "audio" + file.length();
            } else {
                originalPath = originalPath + "" + file.length();
            }
        }
        TL_document tL_document = null;
        if (!isEncrypted) {
            tL_document = (TL_document) MessagesStorage.getInstance().getSentFile(originalPath, !isEncrypted ? 1 : 4);
            if (!(tL_document != null || path.equals(originalPath) || isEncrypted)) {
                tL_document = (TL_document) MessagesStorage.getInstance().getSentFile(path + file.length(), !isEncrypted ? 1 : 4);
            }
        }
        if (tL_document == null) {
            tL_document = new TL_document();
            tL_document.id = 0;
            tL_document.date = ConnectionsManager.getInstance().getCurrentTime();
            TL_documentAttributeFilename fileName = new TL_documentAttributeFilename();
            fileName.file_name = name;
            tL_document.attributes.add(fileName);
            tL_document.size = (int) file.length();
            tL_document.dc_id = 0;
            if (attributeAudio != null) {
                tL_document.attributes.add(attributeAudio);
            }
            if (ext.length() == 0) {
                tL_document.mime_type = "application/octet-stream";
            } else if (ext.toLowerCase().equals("webp")) {
                tL_document.mime_type = "image/webp";
            } else {
                String mimeType = myMime.getMimeTypeFromExtension(ext.toLowerCase());
                if (mimeType != null) {
                    tL_document.mime_type = mimeType;
                } else {
                    tL_document.mime_type = "application/octet-stream";
                }
            }
            if (tL_document.mime_type.equals("image/gif")) {
                try {
                    Bitmap bitmap = ImageLoader.loadBitmap(file.getAbsolutePath(), null, 90.0f, 90.0f, true);
                    if (bitmap != null) {
                        fileName.file_name = "animation.gif";
                        tL_document.thumb = ImageLoader.scaleAndSaveImage(bitmap, 90.0f, 90.0f, 55, isEncrypted);
                        bitmap.recycle();
                    }
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
            if (tL_document.mime_type.equals("image/webp") && allowSticker) {
                Options bmOptions = new Options();
                try {
                    bmOptions.inJustDecodeBounds = true;
                    RandomAccessFile randomAccessFile = new RandomAccessFile(path, "r");
                    ByteBuffer buffer = randomAccessFile.getChannel().map(MapMode.READ_ONLY, 0, (long) path.length());
                    Utilities.loadWebpImage(null, buffer, buffer.limit(), bmOptions, true);
                    randomAccessFile.close();
                } catch (Throwable e2) {
                    FileLog.m611e("tmessages", e2);
                }
                if (bmOptions.outWidth != 0 && bmOptions.outHeight != 0 && bmOptions.outWidth <= 800 && bmOptions.outHeight <= 800) {
                    TL_documentAttributeSticker attributeSticker;
                    if (isEncrypted) {
                        attributeSticker = new TL_documentAttributeSticker_old();
                    } else {
                        attributeSticker = new TL_documentAttributeSticker();
                        attributeSticker.alt = "";
                        attributeSticker.stickerset = new TL_inputStickerSetEmpty();
                    }
                    tL_document.attributes.add(attributeSticker);
                    TL_documentAttributeImageSize attributeImageSize = new TL_documentAttributeImageSize();
                    attributeImageSize.w = bmOptions.outWidth;
                    attributeImageSize.h = bmOptions.outHeight;
                    tL_document.attributes.add(attributeImageSize);
                }
            }
            if (tL_document.thumb == null) {
                tL_document.thumb = new TL_photoSizeEmpty();
                tL_document.thumb.type = "s";
            }
        }
        final TL_document documentFinal = tL_document;
        final String originalPathFinal = originalPath;
        final String pathFinal = path;
        final long j = dialog_id;
        final MessageObject messageObject = reply_to_msg;
        final boolean z = asAdmin;
        AndroidUtilities.runOnUIThread(new Runnable() {
            public void run() {
                SendMessagesHelper.getInstance().sendMessage(documentFinal, originalPathFinal, pathFinal, j, messageObject, z);
            }
        });
        return true;
    }

    public static void prepareSendingDocument(String path, String originalPath, Uri uri, String mine, long dialog_id, MessageObject reply_to_msg, boolean asAdmin) {
        if ((path != null && originalPath != null) || uri != null) {
            ArrayList<String> paths = new ArrayList();
            ArrayList<String> originalPaths = new ArrayList();
            ArrayList<Uri> uris = null;
            if (uri != null) {
                uris = new ArrayList();
            }
            paths.add(path);
            originalPaths.add(originalPath);
            prepareSendingDocuments(paths, originalPaths, uris, mine, dialog_id, reply_to_msg, asAdmin);
        }
    }

    public static void prepareSendingAudioDocuments(ArrayList<MessageObject> messageObjects, long dialog_id, MessageObject reply_to_msg, boolean asAdmin) {
        final ArrayList<MessageObject> arrayList = messageObjects;
        final long j = dialog_id;
        final MessageObject messageObject = reply_to_msg;
        final boolean z = asAdmin;
        new Thread(new Runnable() {
            public void run() {
                int size = arrayList.size();
                for (int a = 0; a < size; a++) {
                    final MessageObject messageObject = (MessageObject) arrayList.get(a);
                    String originalPath = messageObject.messageOwner.attachPath;
                    File f = new File(originalPath);
                    boolean isEncrypted = ((int) j) == 0;
                    if (originalPath != null) {
                        originalPath = originalPath + "audio" + f.length();
                    }
                    TL_document tL_document = null;
                    if (!isEncrypted) {
                        tL_document = (TL_document) MessagesStorage.getInstance().getSentFile(originalPath, !isEncrypted ? 1 : 4);
                    }
                    if (tL_document == null) {
                        tL_document = messageObject.messageOwner.media.document;
                    }
                    if (isEncrypted) {
                        for (int b = 0; b < tL_document.attributes.size(); b++) {
                            if (tL_document.attributes.get(b) instanceof TL_documentAttributeAudio) {
                                TL_documentAttributeAudio_old old = new TL_documentAttributeAudio_old();
                                old.duration = ((DocumentAttribute) tL_document.attributes.get(b)).duration;
                                tL_document.attributes.remove(b);
                                tL_document.attributes.add(old);
                                break;
                            }
                        }
                    }
                    final String originalPathFinal = originalPath;
                    final TL_document documentFinal = tL_document;
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        public void run() {
                            SendMessagesHelper.getInstance().sendMessage(documentFinal, originalPathFinal, messageObject.messageOwner.attachPath, j, messageObject, z);
                        }
                    });
                }
            }
        }).start();
    }

    public static void prepareSendingDocuments(ArrayList<String> paths, ArrayList<String> originalPaths, ArrayList<Uri> uris, String mime, long dialog_id, MessageObject reply_to_msg, boolean asAdmin) {
        if (paths != null || originalPaths != null || uris != null) {
            if (paths == null || originalPaths == null || paths.size() == originalPaths.size()) {
                final ArrayList<String> arrayList = paths;
                final ArrayList<String> arrayList2 = originalPaths;
                final String str = mime;
                final long j = dialog_id;
                final MessageObject messageObject = reply_to_msg;
                final boolean z = asAdmin;
                final ArrayList<Uri> arrayList3 = uris;
                new Thread(new Runnable() {

                    class C05771 implements Runnable {
                        C05771() {
                        }

                        public void run() {
                            try {
                                Toast.makeText(ApplicationLoader.applicationContext, LocaleController.getString("UnsupportedAttachment", C0553R.string.UnsupportedAttachment), 0).show();
                            } catch (Throwable e) {
                                FileLog.m611e("tmessages", e);
                            }
                        }
                    }

                    public void run() {
                        int a;
                        boolean error = false;
                        if (arrayList != null) {
                            for (a = 0; a < arrayList.size(); a++) {
                                if (!SendMessagesHelper.prepareSendingDocumentInternal((String) arrayList.get(a), (String) arrayList2.get(a), null, str, j, messageObject, z)) {
                                    error = true;
                                }
                            }
                        }
                        if (arrayList3 != null) {
                            for (a = 0; a < arrayList3.size(); a++) {
                                if (!SendMessagesHelper.prepareSendingDocumentInternal(null, null, (Uri) arrayList3.get(a), str, j, messageObject, z)) {
                                    error = true;
                                }
                            }
                        }
                        if (error) {
                            AndroidUtilities.runOnUIThread(new C05771());
                        }
                    }
                }).start();
            }
        }
    }

    public static void prepareSendingPhoto(String imageFilePath, Uri imageUri, long dialog_id, MessageObject reply_to_msg, CharSequence caption, boolean asAdmin) {
        ArrayList<String> paths = null;
        ArrayList<Uri> uris = null;
        ArrayList<String> captions = null;
        if (!(imageFilePath == null || imageFilePath.length() == 0)) {
            paths = new ArrayList();
            paths.add(imageFilePath);
        }
        if (imageUri != null) {
            uris = new ArrayList();
            uris.add(imageUri);
        }
        if (caption != null) {
            captions = new ArrayList();
            captions.add(caption.toString());
        }
        prepareSendingPhotos(paths, uris, dialog_id, reply_to_msg, captions, asAdmin);
    }

    public static void prepareSendingPhotosSearch(ArrayList<SearchImage> photos, long dialog_id, MessageObject reply_to_msg, boolean asAdmin) {
        if (photos != null && !photos.isEmpty()) {
            final long j = dialog_id;
            final ArrayList<SearchImage> arrayList = photos;
            final MessageObject messageObject = reply_to_msg;
            final boolean z = asAdmin;
            new Thread(new Runnable() {
                public void run() {
                    boolean isEncrypted = ((int) j) == 0;
                    for (int a = 0; a < arrayList.size(); a++) {
                        final SearchImage searchImage = (SearchImage) arrayList.get(a);
                        File cacheFile;
                        final String originalPathFinal;
                        if (searchImage.type == 1) {
                            TL_document tL_document = null;
                            if (!isEncrypted) {
                                tL_document = (TL_document) MessagesStorage.getInstance().getSentFile(searchImage.imageUrl, !isEncrypted ? 1 : 4);
                            }
                            cacheFile = new File(FileLoader.getInstance().getDirectory(4), Utilities.MD5(searchImage.imageUrl) + "." + ImageLoader.getHttpUrlExtension(searchImage.imageUrl));
                            if (tL_document == null) {
                                File thumbFile;
                                tL_document = new TL_document();
                                tL_document.id = 0;
                                tL_document.date = ConnectionsManager.getInstance().getCurrentTime();
                                TL_documentAttributeFilename fileName = new TL_documentAttributeFilename();
                                fileName.file_name = "animation.gif";
                                tL_document.attributes.add(fileName);
                                tL_document.size = searchImage.size;
                                tL_document.dc_id = 0;
                                tL_document.mime_type = "image/gif";
                                if (cacheFile.exists()) {
                                    thumbFile = cacheFile;
                                } else {
                                    cacheFile = null;
                                    File file = new File(FileLoader.getInstance().getDirectory(4), Utilities.MD5(searchImage.thumbUrl) + "." + ImageLoader.getHttpUrlExtension(searchImage.imageUrl));
                                    if (!file.exists()) {
                                        thumbFile = null;
                                    }
                                }
                                if (thumbFile != null) {
                                    try {
                                        Bitmap bitmap = ImageLoader.loadBitmap(thumbFile.getAbsolutePath(), null, 90.0f, 90.0f, true);
                                        if (bitmap != null) {
                                            tL_document.thumb = ImageLoader.scaleAndSaveImage(bitmap, 90.0f, 90.0f, 55, isEncrypted);
                                            bitmap.recycle();
                                        }
                                    } catch (Throwable e) {
                                        FileLog.m611e("tmessages", e);
                                    }
                                } else {
                                    tL_document.thumb = new TL_photoSize();
                                    tL_document.thumb.f139w = searchImage.width;
                                    tL_document.thumb.f138h = searchImage.height;
                                    tL_document.thumb.size = 0;
                                    tL_document.thumb.location = new TL_fileLocationUnavailable();
                                    tL_document.thumb.type = "x";
                                }
                            }
                            final TL_document documentFinal = tL_document;
                            originalPathFinal = searchImage.imageUrl;
                            final String file2 = cacheFile == null ? searchImage.imageUrl : cacheFile.toString();
                            AndroidUtilities.runOnUIThread(new Runnable() {
                                public void run() {
                                    SendMessagesHelper.getInstance().sendMessage(documentFinal, originalPathFinal, file2, j, messageObject, z);
                                }
                            });
                        } else {
                            boolean needDownloadHttp = true;
                            TL_photo tL_photo = null;
                            if (!isEncrypted) {
                                tL_photo = (TL_photo) MessagesStorage.getInstance().getSentFile(searchImage.imageUrl, !isEncrypted ? 0 : 3);
                            }
                            if (tL_photo == null) {
                                cacheFile = new File(FileLoader.getInstance().getDirectory(4), Utilities.MD5(searchImage.imageUrl) + "." + ImageLoader.getHttpUrlExtension(searchImage.imageUrl));
                                if (cacheFile.exists() && cacheFile.length() != 0) {
                                    tL_photo = SendMessagesHelper.getInstance().generatePhotoSizes(cacheFile.toString(), null);
                                    if (tL_photo != null) {
                                        needDownloadHttp = false;
                                    }
                                }
                                if (tL_photo == null) {
                                    cacheFile = new File(FileLoader.getInstance().getDirectory(4), Utilities.MD5(searchImage.thumbUrl) + "." + ImageLoader.getHttpUrlExtension(searchImage.thumbUrl));
                                    if (cacheFile.exists()) {
                                        tL_photo = SendMessagesHelper.getInstance().generatePhotoSizes(cacheFile.toString(), null);
                                    }
                                    if (tL_photo == null) {
                                        tL_photo = new TL_photo();
                                        tL_photo.date = ConnectionsManager.getInstance().getCurrentTime();
                                        TL_photoSize photoSize = new TL_photoSize();
                                        photoSize.w = searchImage.width;
                                        photoSize.h = searchImage.height;
                                        photoSize.size = 0;
                                        photoSize.location = new TL_fileLocationUnavailable();
                                        photoSize.type = "x";
                                        tL_photo.sizes.add(photoSize);
                                    }
                                }
                            }
                            if (tL_photo != null) {
                                if (searchImage.caption != null) {
                                    tL_photo.caption = searchImage.caption.toString();
                                }
                                originalPathFinal = searchImage.imageUrl;
                                final TL_photo photoFinal = tL_photo;
                                final boolean needDownloadHttpFinal = needDownloadHttp;
                                AndroidUtilities.runOnUIThread(new Runnable() {
                                    public void run() {
                                        SendMessagesHelper.getInstance().sendMessage(photoFinal, originalPathFinal, needDownloadHttpFinal ? searchImage.imageUrl : null, j, messageObject, z);
                                    }
                                });
                            }
                        }
                    }
                }
            }).start();
        }
    }

    private static String getTrimmedString(String src) {
        String result = src.trim();
        if (result.length() == 0) {
            return result;
        }
        while (src.startsWith("\n")) {
            src = src.substring(1);
        }
        while (src.endsWith("\n")) {
            src = src.substring(0, src.length() - 1);
        }
        return src;
    }

    public static void prepareSendingText(final String text, final long dialog_id, final boolean asAdmin) {
        MessagesStorage.getInstance().getStorageQueue().postRunnable(new Runnable() {

            class C05811 implements Runnable {

                class C05801 implements Runnable {
                    C05801() {
                    }

                    public void run() {
                        String textFinal = SendMessagesHelper.getTrimmedString(text);
                        if (textFinal.length() != 0) {
                            int count = (int) Math.ceil((double) (((float) textFinal.length()) / 4096.0f));
                            for (int a = 0; a < count; a++) {
                                String mess = textFinal.substring(a * 4096, Math.min((a + 1) * 4096, textFinal.length()));
                                SendMessagesHelper.getInstance().sendMessage(mess, dialog_id, null, null, true, asAdmin);
                            }
                        }
                    }
                }

                C05811() {
                }

                public void run() {
                    AndroidUtilities.runOnUIThread(new C05801());
                }
            }

            public void run() {
                Utilities.stageQueue.postRunnable(new C05811());
            }
        });
    }

    public static void prepareSendingPhotos(ArrayList<String> paths, ArrayList<Uri> uris, long dialog_id, MessageObject reply_to_msg, ArrayList<String> captions, boolean asAdmin) {
        if (paths != null || uris != null) {
            if (paths != null && paths.isEmpty()) {
                return;
            }
            if (uris == null || !uris.isEmpty()) {
                final ArrayList<String> pathsCopy = new ArrayList();
                final ArrayList<Uri> urisCopy = new ArrayList();
                if (paths != null) {
                    pathsCopy.addAll(paths);
                }
                if (uris != null) {
                    urisCopy.addAll(uris);
                }
                final long j = dialog_id;
                final ArrayList<String> arrayList = captions;
                final MessageObject messageObject = reply_to_msg;
                final boolean z = asAdmin;
                new Thread(new Runnable() {
                    /* JADX WARNING: inconsistent code. */
                    /* Code decompiled incorrectly, please refer to instructions dump. */
                    public void run() {
                        /*
                        r24 = this;
                        r0 = r24;
                        r2 = r2;
                        r2 = (int) r2;
                        if (r2 != 0) goto L_0x008b;
                    L_0x0007:
                        r13 = 1;
                    L_0x0008:
                        r19 = 0;
                        r20 = 0;
                        r0 = r24;
                        r2 = r4;
                        r2 = r2.isEmpty();
                        if (r2 != 0) goto L_0x008e;
                    L_0x0016:
                        r0 = r24;
                        r2 = r4;
                        r11 = r2.size();
                    L_0x001e:
                        r16 = 0;
                        r23 = 0;
                        r5 = 0;
                        r10 = 0;
                    L_0x0024:
                        if (r10 >= r11) goto L_0x016a;
                    L_0x0026:
                        r0 = r24;
                        r2 = r4;
                        r2 = r2.isEmpty();
                        if (r2 != 0) goto L_0x0097;
                    L_0x0030:
                        r0 = r24;
                        r2 = r4;
                        r16 = r2.get(r10);
                        r16 = (java.lang.String) r16;
                    L_0x003a:
                        r14 = r16;
                        r22 = r16;
                        if (r22 != 0) goto L_0x004a;
                    L_0x0040:
                        if (r23 == 0) goto L_0x004a;
                    L_0x0042:
                        r22 = org.telegram.messenger.AndroidUtilities.getPath(r23);
                        r14 = r23.toString();
                    L_0x004a:
                        r12 = 0;
                        if (r22 == 0) goto L_0x00af;
                    L_0x004d:
                        r2 = ".gif";
                        r0 = r22;
                        r2 = r0.endsWith(r2);
                        if (r2 != 0) goto L_0x0061;
                    L_0x0057:
                        r2 = ".webp";
                        r0 = r22;
                        r2 = r0.endsWith(r2);
                        if (r2 == 0) goto L_0x00af;
                    L_0x0061:
                        r2 = ".gif";
                        r0 = r22;
                        r2 = r0.endsWith(r2);
                        if (r2 == 0) goto L_0x00ac;
                    L_0x006b:
                        r5 = "gif";
                    L_0x006d:
                        r12 = 1;
                    L_0x006e:
                        if (r12 == 0) goto L_0x00df;
                    L_0x0070:
                        if (r19 != 0) goto L_0x007c;
                    L_0x0072:
                        r19 = new java.util.ArrayList;
                        r19.<init>();
                        r20 = new java.util.ArrayList;
                        r20.<init>();
                    L_0x007c:
                        r0 = r19;
                        r1 = r22;
                        r0.add(r1);
                        r0 = r20;
                        r0.add(r14);
                    L_0x0088:
                        r10 = r10 + 1;
                        goto L_0x0024;
                    L_0x008b:
                        r13 = 0;
                        goto L_0x0008;
                    L_0x008e:
                        r0 = r24;
                        r2 = r5;
                        r11 = r2.size();
                        goto L_0x001e;
                    L_0x0097:
                        r0 = r24;
                        r2 = r5;
                        r2 = r2.isEmpty();
                        if (r2 != 0) goto L_0x003a;
                    L_0x00a1:
                        r0 = r24;
                        r2 = r5;
                        r23 = r2.get(r10);
                        r23 = (android.net.Uri) r23;
                        goto L_0x003a;
                    L_0x00ac:
                        r5 = "webp";
                        goto L_0x006d;
                    L_0x00af:
                        if (r22 != 0) goto L_0x006e;
                    L_0x00b1:
                        if (r23 == 0) goto L_0x006e;
                    L_0x00b3:
                        r2 = org.telegram.messenger.MediaController.isGif(r23);
                        if (r2 == 0) goto L_0x00c9;
                    L_0x00b9:
                        r12 = 1;
                        r14 = r23.toString();
                        r2 = "gif";
                        r0 = r23;
                        r22 = org.telegram.messenger.MediaController.copyDocumentToCache(r0, r2);
                        r5 = "gif";
                        goto L_0x006e;
                    L_0x00c9:
                        r2 = org.telegram.messenger.MediaController.isWebp(r23);
                        if (r2 == 0) goto L_0x006e;
                    L_0x00cf:
                        r12 = 1;
                        r14 = r23.toString();
                        r2 = "webp";
                        r0 = r23;
                        r22 = org.telegram.messenger.MediaController.copyDocumentToCache(r0, r2);
                        r5 = "webp";
                        goto L_0x006e;
                    L_0x00df:
                        if (r22 == 0) goto L_0x0164;
                    L_0x00e1:
                        r21 = new java.io.File;
                        r21.<init>(r22);
                        r2 = new java.lang.StringBuilder;
                        r2.<init>();
                        r2 = r2.append(r14);
                        r6 = r21.length();
                        r2 = r2.append(r6);
                        r3 = "_";
                        r2 = r2.append(r3);
                        r6 = r21.lastModified();
                        r2 = r2.append(r6);
                        r14 = r2.toString();
                    L_0x0109:
                        r17 = 0;
                        if (r13 != 0) goto L_0x012f;
                    L_0x010d:
                        r3 = org.telegram.messenger.MessagesStorage.getInstance();
                        if (r13 != 0) goto L_0x0166;
                    L_0x0113:
                        r2 = 0;
                    L_0x0114:
                        r17 = r3.getSentFile(r14, r2);
                        r17 = (org.telegram.tgnet.TLRPC.TL_photo) r17;
                        if (r17 != 0) goto L_0x012f;
                    L_0x011c:
                        if (r23 == 0) goto L_0x012f;
                    L_0x011e:
                        r3 = org.telegram.messenger.MessagesStorage.getInstance();
                        r4 = org.telegram.messenger.AndroidUtilities.getPath(r23);
                        if (r13 != 0) goto L_0x0168;
                    L_0x0128:
                        r2 = 0;
                    L_0x0129:
                        r17 = r3.getSentFile(r4, r2);
                        r17 = (org.telegram.tgnet.TLRPC.TL_photo) r17;
                    L_0x012f:
                        if (r17 != 0) goto L_0x013d;
                    L_0x0131:
                        r2 = org.telegram.messenger.SendMessagesHelper.getInstance();
                        r0 = r16;
                        r1 = r23;
                        r17 = r2.generatePhotoSizes(r0, r1);
                    L_0x013d:
                        if (r17 == 0) goto L_0x0088;
                    L_0x013f:
                        r0 = r24;
                        r2 = r6;
                        if (r2 == 0) goto L_0x0153;
                    L_0x0145:
                        r0 = r24;
                        r2 = r6;
                        r2 = r2.get(r10);
                        r2 = (java.lang.String) r2;
                        r0 = r17;
                        r0.caption = r2;
                    L_0x0153:
                        r15 = r14;
                        r18 = r17;
                        r2 = new org.telegram.messenger.SendMessagesHelper$13$1;
                        r0 = r24;
                        r1 = r18;
                        r2.<init>(r1, r15);
                        org.telegram.messenger.AndroidUtilities.runOnUIThread(r2);
                        goto L_0x0088;
                    L_0x0164:
                        r14 = 0;
                        goto L_0x0109;
                    L_0x0166:
                        r2 = 3;
                        goto L_0x0114;
                    L_0x0168:
                        r2 = 3;
                        goto L_0x0129;
                    L_0x016a:
                        if (r19 == 0) goto L_0x019c;
                    L_0x016c:
                        r2 = r19.isEmpty();
                        if (r2 != 0) goto L_0x019c;
                    L_0x0172:
                        r10 = 0;
                    L_0x0173:
                        r2 = r19.size();
                        if (r10 >= r2) goto L_0x019c;
                    L_0x0179:
                        r0 = r19;
                        r2 = r0.get(r10);
                        r2 = (java.lang.String) r2;
                        r0 = r20;
                        r3 = r0.get(r10);
                        r3 = (java.lang.String) r3;
                        r4 = 0;
                        r0 = r24;
                        r6 = r2;
                        r0 = r24;
                        r8 = r7;
                        r0 = r24;
                        r9 = r8;
                        org.telegram.messenger.SendMessagesHelper.prepareSendingDocumentInternal(r2, r3, r4, r5, r6, r8, r9);
                        r10 = r10 + 1;
                        goto L_0x0173;
                    L_0x019c:
                        return;
                        */
                        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.SendMessagesHelper.13.run():void");
                    }
                }).start();
            }
        }
    }

    public static void prepareSendingVideo(String videoPath, long estimatedSize, long duration, int width, int height, VideoEditedInfo videoEditedInfo, long dialog_id, MessageObject reply_to_msg, boolean asAdmin) {
        if (videoPath != null && videoPath.length() != 0) {
            final long j = dialog_id;
            final VideoEditedInfo videoEditedInfo2 = videoEditedInfo;
            final String str = videoPath;
            final long j2 = duration;
            final int i = height;
            final int i2 = width;
            final long j3 = estimatedSize;
            final MessageObject messageObject = reply_to_msg;
            final boolean z = asAdmin;
            new Thread(new Runnable() {
                public void run() {
                    Throwable e;
                    MediaPlayer mp;
                    final TL_video tL_video;
                    final String str;
                    final String str2;
                    Throwable th;
                    boolean isEncrypted = ((int) j) == 0;
                    if (videoEditedInfo2 != null || str.endsWith("mp4")) {
                        String path = str;
                        String originalPath = str;
                        File file = new File(originalPath);
                        originalPath = originalPath + file.length() + "_" + file.lastModified();
                        if (videoEditedInfo2 != null) {
                            originalPath = originalPath + j2 + "_" + videoEditedInfo2.startTime + "_" + videoEditedInfo2.endTime;
                            if (videoEditedInfo2.resultWidth == videoEditedInfo2.originalWidth) {
                                originalPath = originalPath + "_" + videoEditedInfo2.resultWidth;
                            }
                        }
                        TL_video tL_video2 = null;
                        if (!isEncrypted) {
                            tL_video2 = (TL_video) MessagesStorage.getInstance().getSentFile(originalPath, !isEncrypted ? 2 : 5);
                        }
                        if (tL_video2 == null) {
                            PhotoSize size = ImageLoader.scaleAndSaveImage(ThumbnailUtils.createVideoThumbnail(str, 1), 90.0f, 90.0f, 55, isEncrypted);
                            tL_video2 = new TL_video();
                            tL_video2.thumb = size;
                            if (tL_video2.thumb == null) {
                                tL_video2.thumb = new TL_photoSizeEmpty();
                                tL_video2.thumb.type = "s";
                            } else {
                                tL_video2.thumb.type = "s";
                            }
                            tL_video2.mime_type = "video/mp4";
                            tL_video2.id = 0;
                            UserConfig.saveConfig(false);
                            if (videoEditedInfo2 != null) {
                                tL_video2.duration = (int) (j2 / 1000);
                                if (videoEditedInfo2.rotationValue == 90 || videoEditedInfo2.rotationValue == 270) {
                                    tL_video2.w = i;
                                    tL_video2.h = i2;
                                } else {
                                    tL_video2.w = i2;
                                    tL_video2.h = i;
                                }
                                tL_video2.size = (int) j3;
                                String fileName = "-2147483648_" + UserConfig.lastLocalId + ".mp4";
                                UserConfig.lastLocalId--;
                                File cacheFile = new File(FileLoader.getInstance().getDirectory(4), fileName);
                                UserConfig.saveConfig(false);
                                path = cacheFile.getAbsolutePath();
                            } else {
                                if (file.exists()) {
                                    tL_video2.size = (int) file.length();
                                }
                                boolean infoObtained = false;
                                if (VERSION.SDK_INT >= 14) {
                                    MediaMetadataRetriever mediaMetadataRetriever = null;
                                    try {
                                        MediaMetadataRetriever mediaMetadataRetriever2 = new MediaMetadataRetriever();
                                        try {
                                            mediaMetadataRetriever2.setDataSource(str);
                                            String width = mediaMetadataRetriever2.extractMetadata(18);
                                            if (width != null) {
                                                tL_video2.w = Integer.parseInt(width);
                                            }
                                            String height = mediaMetadataRetriever2.extractMetadata(19);
                                            if (height != null) {
                                                tL_video2.h = Integer.parseInt(height);
                                            }
                                            String duration = mediaMetadataRetriever2.extractMetadata(9);
                                            if (duration != null) {
                                                tL_video2.duration = (int) Math.ceil((double) (((float) Long.parseLong(duration)) / 1000.0f));
                                            }
                                            infoObtained = true;
                                            if (mediaMetadataRetriever2 != null) {
                                                try {
                                                    mediaMetadataRetriever2.release();
                                                } catch (Throwable e2) {
                                                    FileLog.m611e("tmessages", e2);
                                                }
                                            }
                                        } catch (Exception e3) {
                                            e2 = e3;
                                            mediaMetadataRetriever = mediaMetadataRetriever2;
                                            try {
                                                FileLog.m611e("tmessages", e2);
                                                if (mediaMetadataRetriever != null) {
                                                    try {
                                                        mediaMetadataRetriever.release();
                                                    } catch (Throwable e22) {
                                                        FileLog.m611e("tmessages", e22);
                                                    }
                                                }
                                                if (!infoObtained) {
                                                    try {
                                                        mp = MediaPlayer.create(ApplicationLoader.applicationContext, Uri.fromFile(new File(str)));
                                                        if (mp != null) {
                                                            tL_video2.duration = (int) Math.ceil((double) (((float) mp.getDuration()) / 1000.0f));
                                                            tL_video2.w = mp.getVideoWidth();
                                                            tL_video2.h = mp.getVideoHeight();
                                                            mp.release();
                                                        }
                                                    } catch (Throwable e222) {
                                                        FileLog.m611e("tmessages", e222);
                                                    }
                                                }
                                                tL_video = tL_video2;
                                                str = originalPath;
                                                str2 = path;
                                                AndroidUtilities.runOnUIThread(new Runnable() {
                                                    public void run() {
                                                        SendMessagesHelper.getInstance().sendMessage(tL_video, videoEditedInfo2, str, str2, j, messageObject, z);
                                                    }
                                                });
                                                return;
                                            } catch (Throwable th2) {
                                                th = th2;
                                                if (mediaMetadataRetriever != null) {
                                                    try {
                                                        mediaMetadataRetriever.release();
                                                    } catch (Throwable e2222) {
                                                        FileLog.m611e("tmessages", e2222);
                                                    }
                                                }
                                                throw th;
                                            }
                                        } catch (Throwable th3) {
                                            th = th3;
                                            mediaMetadataRetriever = mediaMetadataRetriever2;
                                            if (mediaMetadataRetriever != null) {
                                                mediaMetadataRetriever.release();
                                            }
                                            throw th;
                                        }
                                    } catch (Exception e4) {
                                        e2222 = e4;
                                        FileLog.m611e("tmessages", e2222);
                                        if (mediaMetadataRetriever != null) {
                                            mediaMetadataRetriever.release();
                                        }
                                        if (infoObtained) {
                                            mp = MediaPlayer.create(ApplicationLoader.applicationContext, Uri.fromFile(new File(str)));
                                            if (mp != null) {
                                                tL_video2.duration = (int) Math.ceil((double) (((float) mp.getDuration()) / 1000.0f));
                                                tL_video2.w = mp.getVideoWidth();
                                                tL_video2.h = mp.getVideoHeight();
                                                mp.release();
                                            }
                                        }
                                        tL_video = tL_video2;
                                        str = originalPath;
                                        str2 = path;
                                        AndroidUtilities.runOnUIThread(/* anonymous class already generated */);
                                        return;
                                    }
                                }
                                if (infoObtained) {
                                    mp = MediaPlayer.create(ApplicationLoader.applicationContext, Uri.fromFile(new File(str)));
                                    if (mp != null) {
                                        tL_video2.duration = (int) Math.ceil((double) (((float) mp.getDuration()) / 1000.0f));
                                        tL_video2.w = mp.getVideoWidth();
                                        tL_video2.h = mp.getVideoHeight();
                                        mp.release();
                                    }
                                }
                            }
                        }
                        tL_video = tL_video2;
                        str = originalPath;
                        str2 = path;
                        AndroidUtilities.runOnUIThread(/* anonymous class already generated */);
                        return;
                    }
                    SendMessagesHelper.prepareSendingDocumentInternal(str, str, null, null, j, messageObject, z);
                }
            }).start();
        }
    }
}
