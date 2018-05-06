package org.telegram.messenger.query;

import android.text.TextUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import org.telegram.SQLite.SQLiteCursor;
import org.telegram.SQLite.SQLitePreparedStatement;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.NativeByteBuffer;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.Message;
import org.telegram.tgnet.TLRPC.TL_channels_getMessages;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_messages_getMessages;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.messages_Messages;

public class ReplyMessageQuery {
    public static void loadReplyMessagesForMessages(ArrayList<MessageObject> messages, long dialog_id) {
        final ArrayList<Integer> replyMessages = new ArrayList();
        final HashMap<Integer, ArrayList<MessageObject>> replyMessageOwners = new HashMap();
        final StringBuilder stringBuilder = new StringBuilder();
        int channelId = 0;
        Iterator i$ = messages.iterator();
        while (i$.hasNext()) {
            MessageObject messageObject = (MessageObject) i$.next();
            if (messageObject.getId() > 0 && messageObject.isReply() && messageObject.replyMessageObject == null) {
                Integer id = Integer.valueOf(messageObject.messageOwner.reply_to_msg_id);
                long messageId = (long) id.intValue();
                if (messageObject.messageOwner.to_id.channel_id != 0) {
                    messageId |= ((long) messageObject.messageOwner.to_id.channel_id) << 32;
                    channelId = messageObject.messageOwner.to_id.channel_id;
                }
                if (stringBuilder.length() > 0) {
                    stringBuilder.append(',');
                }
                stringBuilder.append(messageId);
                ArrayList<MessageObject> messageObjects = (ArrayList) replyMessageOwners.get(id);
                if (messageObjects == null) {
                    messageObjects = new ArrayList();
                    replyMessageOwners.put(id, messageObjects);
                }
                messageObjects.add(messageObject);
                if (!replyMessages.contains(id)) {
                    replyMessages.add(id);
                }
            }
        }
        if (!replyMessages.isEmpty()) {
            final int channelIdFinal = channelId;
            final long j = dialog_id;
            MessagesStorage.getInstance().getStorageQueue().postRunnable(new Runnable() {

                class C14671 implements RequestDelegate {
                    C14671() {
                    }

                    public void run(TLObject response, TL_error error) {
                        if (error == null) {
                            messages_Messages messagesRes = (messages_Messages) response;
                            ImageLoader.saveMessagesThumbs(messagesRes.messages);
                            ReplyMessageQuery.broadcastReplyMessages(messagesRes.messages, replyMessageOwners, messagesRes.users, messagesRes.chats, j, false);
                            MessagesStorage.getInstance().putUsersAndChats(messagesRes.users, messagesRes.chats, true, true);
                            ReplyMessageQuery.saveReplyMessages(replyMessageOwners, messagesRes.messages);
                        }
                    }
                }

                class C14682 implements RequestDelegate {
                    C14682() {
                    }

                    public void run(TLObject response, TL_error error) {
                        if (error == null) {
                            messages_Messages messagesRes = (messages_Messages) response;
                            ImageLoader.saveMessagesThumbs(messagesRes.messages);
                            ReplyMessageQuery.broadcastReplyMessages(messagesRes.messages, replyMessageOwners, messagesRes.users, messagesRes.chats, j, false);
                            MessagesStorage.getInstance().putUsersAndChats(messagesRes.users, messagesRes.chats, true, true);
                            ReplyMessageQuery.saveReplyMessages(replyMessageOwners, messagesRes.messages);
                        }
                    }
                }

                public void run() {
                    try {
                        ArrayList<Message> result = new ArrayList();
                        ArrayList<User> users = new ArrayList();
                        ArrayList<Chat> chats = new ArrayList();
                        ArrayList<Integer> usersToLoad = new ArrayList();
                        ArrayList<Integer> chatsToLoad = new ArrayList();
                        SQLiteCursor cursor = MessagesStorage.getInstance().getDatabase().queryFinalized(String.format(Locale.US, "SELECT data, mid, date FROM messages WHERE mid IN(%s)", new Object[]{stringBuilder.toString()}), new Object[0]);
                        while (cursor.next()) {
                            NativeByteBuffer data = new NativeByteBuffer(cursor.byteArrayLength(0));
                            if (!(data == null || cursor.byteBufferValue(0, data) == 0)) {
                                Message message = Message.TLdeserialize(data, data.readInt32(false), false);
                                message.id = cursor.intValue(1);
                                message.date = cursor.intValue(2);
                                message.dialog_id = j;
                                MessagesStorage.addUsersAndChatsFromMessage(message, usersToLoad, chatsToLoad);
                                result.add(message);
                                replyMessages.remove(Integer.valueOf(message.id));
                            }
                            data.reuse();
                        }
                        cursor.dispose();
                        if (!usersToLoad.isEmpty()) {
                            MessagesStorage.getInstance().getUsersInternal(TextUtils.join(",", usersToLoad), users);
                        }
                        if (!chatsToLoad.isEmpty()) {
                            MessagesStorage.getInstance().getChatsInternal(TextUtils.join(",", chatsToLoad), chats);
                        }
                        ReplyMessageQuery.broadcastReplyMessages(result, replyMessageOwners, users, chats, j, true);
                        if (!replyMessages.isEmpty()) {
                            if (channelIdFinal != 0) {
                                TL_channels_getMessages req = new TL_channels_getMessages();
                                req.channel = MessagesController.getInputChannel(channelIdFinal);
                                req.id = replyMessages;
                                ConnectionsManager.getInstance().sendRequest(req, new C14671());
                                return;
                            }
                            TL_messages_getMessages req2 = new TL_messages_getMessages();
                            req2.id = replyMessages;
                            ConnectionsManager.getInstance().sendRequest(req2, new C14682());
                        }
                    } catch (Throwable e) {
                        FileLog.m611e("tmessages", e);
                    }
                }
            });
        }
    }

    private static void saveReplyMessages(final HashMap<Integer, ArrayList<MessageObject>> replyMessageOwners, final ArrayList<Message> result) {
        MessagesStorage.getInstance().getStorageQueue().postRunnable(new Runnable() {
            public void run() {
                try {
                    MessagesStorage.getInstance().getDatabase().beginTransaction();
                    SQLitePreparedStatement state = MessagesStorage.getInstance().getDatabase().executeFast("UPDATE messages SET replydata = ? WHERE mid = ?");
                    Iterator it = result.iterator();
                    while (it.hasNext()) {
                        Message message = (Message) it.next();
                        ArrayList<MessageObject> messageObjects = (ArrayList) replyMessageOwners.get(Integer.valueOf(message.id));
                        if (messageObjects != null) {
                            NativeByteBuffer data = new NativeByteBuffer(message.getObjectSize());
                            message.serializeToStream(data);
                            Iterator i$ = messageObjects.iterator();
                            while (i$.hasNext()) {
                                MessageObject messageObject = (MessageObject) i$.next();
                                state.requery();
                                long messageId = (long) messageObject.getId();
                                if (messageObject.messageOwner.to_id.channel_id != 0) {
                                    messageId |= ((long) messageObject.messageOwner.to_id.channel_id) << 32;
                                }
                                state.bindByteBuffer(1, data);
                                state.bindLong(2, messageId);
                                state.step();
                            }
                            data.reuse();
                        }
                    }
                    state.dispose();
                    MessagesStorage.getInstance().getDatabase().commitTransaction();
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
        });
    }

    private static void broadcastReplyMessages(ArrayList<Message> result, HashMap<Integer, ArrayList<MessageObject>> replyMessageOwners, ArrayList<User> users, ArrayList<Chat> chats, long dialog_id, boolean isCache) {
        int a;
        final HashMap<Integer, User> usersDict = new HashMap();
        for (a = 0; a < users.size(); a++) {
            User user = (User) users.get(a);
            usersDict.put(Integer.valueOf(user.id), user);
        }
        final HashMap<Integer, Chat> chatsDict = new HashMap();
        for (a = 0; a < chats.size(); a++) {
            Chat chat = (Chat) chats.get(a);
            chatsDict.put(Integer.valueOf(chat.id), chat);
        }
        final ArrayList<User> arrayList = users;
        final boolean z = isCache;
        final ArrayList<Chat> arrayList2 = chats;
        final ArrayList<Message> arrayList3 = result;
        final HashMap<Integer, ArrayList<MessageObject>> hashMap = replyMessageOwners;
        final long j = dialog_id;
        AndroidUtilities.runOnUIThread(new Runnable() {
            public void run() {
                MessagesController.getInstance().putUsers(arrayList, z);
                MessagesController.getInstance().putChats(arrayList2, z);
                boolean changed = false;
                for (int a = 0; a < arrayList3.size(); a++) {
                    Message message = (Message) arrayList3.get(a);
                    ArrayList<MessageObject> arrayList = (ArrayList) hashMap.get(Integer.valueOf(message.id));
                    if (arrayList != null) {
                        MessageObject messageObject = new MessageObject(message, usersDict, chatsDict, false);
                        for (int b = 0; b < arrayList.size(); b++) {
                            ((MessageObject) arrayList.get(b)).replyMessageObject = messageObject;
                        }
                        changed = true;
                    }
                }
                if (changed) {
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.didLoadedReplyMessages, Long.valueOf(j));
                }
            }
        });
    }
}
