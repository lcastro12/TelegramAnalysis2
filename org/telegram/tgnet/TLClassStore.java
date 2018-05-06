package org.telegram.tgnet;

import java.util.HashMap;
import org.telegram.messenger.FileLog;
import org.telegram.tgnet.TLRPC.TL_audio;
import org.telegram.tgnet.TLRPC.TL_audioEmpty;
import org.telegram.tgnet.TLRPC.TL_audioEncrypted;
import org.telegram.tgnet.TLRPC.TL_audio_old;
import org.telegram.tgnet.TLRPC.TL_audio_old2;
import org.telegram.tgnet.TLRPC.TL_config;
import org.telegram.tgnet.TLRPC.TL_decryptedMessage;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageHolder;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageLayer;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageService;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageService_old;
import org.telegram.tgnet.TLRPC.TL_decryptedMessage_old;
import org.telegram.tgnet.TLRPC.TL_document;
import org.telegram.tgnet.TLRPC.TL_documentEmpty;
import org.telegram.tgnet.TLRPC.TL_documentEncrypted;
import org.telegram.tgnet.TLRPC.TL_documentEncrypted_old;
import org.telegram.tgnet.TLRPC.TL_document_old;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_messageEncryptedAction;
import org.telegram.tgnet.TLRPC.TL_message_secret;
import org.telegram.tgnet.TLRPC.TL_null;
import org.telegram.tgnet.TLRPC.TL_photo;
import org.telegram.tgnet.TLRPC.TL_photoCachedSize;
import org.telegram.tgnet.TLRPC.TL_photoEmpty;
import org.telegram.tgnet.TLRPC.TL_photoSize;
import org.telegram.tgnet.TLRPC.TL_photoSizeEmpty;
import org.telegram.tgnet.TLRPC.TL_photo_old;
import org.telegram.tgnet.TLRPC.TL_photo_old2;
import org.telegram.tgnet.TLRPC.TL_updateShort;
import org.telegram.tgnet.TLRPC.TL_updateShortChatMessage;
import org.telegram.tgnet.TLRPC.TL_updateShortMessage;
import org.telegram.tgnet.TLRPC.TL_updateShortSentMessage;
import org.telegram.tgnet.TLRPC.TL_updates;
import org.telegram.tgnet.TLRPC.TL_updatesCombined;
import org.telegram.tgnet.TLRPC.TL_updatesTooLong;
import org.telegram.tgnet.TLRPC.TL_video;
import org.telegram.tgnet.TLRPC.TL_videoEmpty;
import org.telegram.tgnet.TLRPC.TL_videoEncrypted;
import org.telegram.tgnet.TLRPC.TL_video_old;
import org.telegram.tgnet.TLRPC.TL_video_old2;
import org.telegram.tgnet.TLRPC.TL_video_old3;

public class TLClassStore {
    static TLClassStore store = null;
    private HashMap<Integer, Class> classStore = new HashMap();

    public TLClassStore() {
        this.classStore.put(Integer.valueOf(TL_error.constructor), TL_error.class);
        this.classStore.put(Integer.valueOf(TL_decryptedMessageService.constructor), TL_decryptedMessageService.class);
        this.classStore.put(Integer.valueOf(TL_decryptedMessage.constructor), TL_decryptedMessage.class);
        this.classStore.put(Integer.valueOf(TL_config.constructor), TL_config.class);
        this.classStore.put(Integer.valueOf(TL_decryptedMessageLayer.constructor), TL_decryptedMessageLayer.class);
        this.classStore.put(Integer.valueOf(TL_decryptedMessageService_old.constructor), TL_decryptedMessageService_old.class);
        this.classStore.put(Integer.valueOf(TL_decryptedMessage_old.constructor), TL_decryptedMessage_old.class);
        this.classStore.put(Integer.valueOf(TL_message_secret.constructor), TL_message_secret.class);
        this.classStore.put(Integer.valueOf(TL_messageEncryptedAction.constructor), TL_messageEncryptedAction.class);
        this.classStore.put(Integer.valueOf(TL_decryptedMessageHolder.constructor), TL_decryptedMessageHolder.class);
        this.classStore.put(Integer.valueOf(TL_null.constructor), TL_null.class);
        this.classStore.put(Integer.valueOf(TL_updateShortChatMessage.constructor), TL_updateShortChatMessage.class);
        this.classStore.put(Integer.valueOf(TL_updates.constructor), TL_updates.class);
        this.classStore.put(Integer.valueOf(TL_updateShortMessage.constructor), TL_updateShortMessage.class);
        this.classStore.put(Integer.valueOf(TL_updateShort.constructor), TL_updateShort.class);
        this.classStore.put(Integer.valueOf(TL_updatesCombined.constructor), TL_updatesCombined.class);
        this.classStore.put(Integer.valueOf(TL_updateShortSentMessage.constructor), TL_updateShortSentMessage.class);
        this.classStore.put(Integer.valueOf(TL_updatesTooLong.constructor), TL_updatesTooLong.class);
        this.classStore.put(Integer.valueOf(TL_video.constructor), TL_video.class);
        this.classStore.put(Integer.valueOf(TL_videoEmpty.constructor), TL_videoEmpty.class);
        this.classStore.put(Integer.valueOf(TL_video_old2.constructor), TL_video_old2.class);
        this.classStore.put(Integer.valueOf(TL_video_old.constructor), TL_video_old.class);
        this.classStore.put(Integer.valueOf(TL_videoEncrypted.constructor), TL_videoEncrypted.class);
        this.classStore.put(Integer.valueOf(TL_video_old3.constructor), TL_video_old3.class);
        this.classStore.put(Integer.valueOf(TL_audio.constructor), TL_audio.class);
        this.classStore.put(Integer.valueOf(TL_audioEncrypted.constructor), TL_audioEncrypted.class);
        this.classStore.put(Integer.valueOf(TL_audioEmpty.constructor), TL_audioEmpty.class);
        this.classStore.put(Integer.valueOf(TL_audio_old.constructor), TL_audio_old.class);
        this.classStore.put(Integer.valueOf(TL_audio_old2.constructor), TL_audio_old2.class);
        this.classStore.put(Integer.valueOf(TL_document.constructor), TL_document.class);
        this.classStore.put(Integer.valueOf(TL_documentEmpty.constructor), TL_documentEmpty.class);
        this.classStore.put(Integer.valueOf(TL_documentEncrypted_old.constructor), TL_documentEncrypted_old.class);
        this.classStore.put(Integer.valueOf(TL_documentEncrypted.constructor), TL_documentEncrypted.class);
        this.classStore.put(Integer.valueOf(TL_document_old.constructor), TL_document_old.class);
        this.classStore.put(Integer.valueOf(TL_photo.constructor), TL_photo.class);
        this.classStore.put(Integer.valueOf(TL_photoEmpty.constructor), TL_photoEmpty.class);
        this.classStore.put(Integer.valueOf(TL_photoSize.constructor), TL_photoSize.class);
        this.classStore.put(Integer.valueOf(TL_photoSizeEmpty.constructor), TL_photoSizeEmpty.class);
        this.classStore.put(Integer.valueOf(TL_photoCachedSize.constructor), TL_photoCachedSize.class);
        this.classStore.put(Integer.valueOf(TL_photo_old.constructor), TL_photo_old.class);
        this.classStore.put(Integer.valueOf(TL_photo_old2.constructor), TL_photo_old2.class);
    }

    public static TLClassStore Instance() {
        if (store == null) {
            store = new TLClassStore();
        }
        return store;
    }

    public TLObject TLdeserialize(NativeByteBuffer stream, int constructor, boolean exception) {
        Class objClass = (Class) this.classStore.get(Integer.valueOf(constructor));
        if (objClass == null) {
            return null;
        }
        try {
            TLObject response = (TLObject) objClass.newInstance();
            response.readParams(stream, exception);
            return response;
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
            return null;
        }
    }
}
