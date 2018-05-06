package org.telegram.messenger.query;

import java.util.ArrayList;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.Message;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_inputMessagesFilterEmpty;
import org.telegram.tgnet.TLRPC.TL_messages_search;
import org.telegram.tgnet.TLRPC.messages_Messages;

public class MessagesSearchQuery {
    private static int lastReqId;
    private static int lastReturnedNum;
    private static String lastSearchQuery;
    private static boolean[] messagesSearchEndReached = new boolean[]{false, false};
    private static int reqId;
    private static ArrayList<MessageObject> searchResultMessages = new ArrayList();

    private static int getMask() {
        int mask = 0;
        if (!(lastReturnedNum >= searchResultMessages.size() - 1 && messagesSearchEndReached[0] && messagesSearchEndReached[1])) {
            mask = 0 | 1;
        }
        if (lastReturnedNum > 0) {
            return mask | 2;
        }
        return mask;
    }

    public static void searchMessagesInChat(String query, long dialog_id, long mergeDialogId, int guid, int direction) {
        if (reqId != 0) {
            ConnectionsManager.getInstance().cancelRequest(reqId, true);
            reqId = 0;
        }
        int max_id = 0;
        long queryWithDialog = dialog_id;
        if (query == null || query.length() == 0) {
            MessageObject messageObject;
            if (direction == 1) {
                lastReturnedNum++;
                if (lastReturnedNum < searchResultMessages.size()) {
                    messageObject = (MessageObject) searchResultMessages.get(lastReturnedNum);
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.chatSearchResultsAvailable, Integer.valueOf(guid), Integer.valueOf(messageObject.getId()), Integer.valueOf(getMask()), Long.valueOf(messageObject.getDialogId()));
                    return;
                } else if ((messagesSearchEndReached[0] && mergeDialogId == 0) || messagesSearchEndReached[1]) {
                    lastReturnedNum--;
                    return;
                } else {
                    query = lastSearchQuery;
                    messageObject = (MessageObject) searchResultMessages.get(searchResultMessages.size() - 1);
                    if (messageObject.getDialogId() != dialog_id || messagesSearchEndReached[0]) {
                        if (messageObject.getDialogId() == mergeDialogId) {
                            max_id = messageObject.getId();
                        }
                        queryWithDialog = mergeDialogId;
                        messagesSearchEndReached[1] = false;
                    } else {
                        max_id = messageObject.getId();
                        queryWithDialog = dialog_id;
                    }
                }
            } else if (direction == 2) {
                lastReturnedNum--;
                if (lastReturnedNum < 0) {
                    lastReturnedNum = 0;
                    return;
                }
                if (lastReturnedNum >= searchResultMessages.size()) {
                    lastReturnedNum = searchResultMessages.size() - 1;
                }
                messageObject = (MessageObject) searchResultMessages.get(lastReturnedNum);
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.chatSearchResultsAvailable, Integer.valueOf(guid), Integer.valueOf(messageObject.getId()), Integer.valueOf(getMask()), Long.valueOf(messageObject.getDialogId()));
                return;
            } else {
                return;
            }
        }
        if (!(!messagesSearchEndReached[0] || messagesSearchEndReached[1] || mergeDialogId == 0)) {
            queryWithDialog = mergeDialogId;
        }
        final TL_messages_search req = new TL_messages_search();
        req.limit = 21;
        req.peer = MessagesController.getInputPeer((int) queryWithDialog);
        if (req.peer != null) {
            req.f142q = query;
            req.max_id = max_id;
            req.filter = new TL_inputMessagesFilterEmpty();
            final int currentReqId = lastReqId + 1;
            lastReqId = currentReqId;
            lastSearchQuery = query;
            final long queryWithDialogFinal = queryWithDialog;
            final long j = dialog_id;
            final long j2 = mergeDialogId;
            final int i = guid;
            reqId = ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
                public void run(final TLObject response, final TL_error error) {
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        public void run() {
                            MessagesSearchQuery.reqId = 0;
                            if (currentReqId == MessagesSearchQuery.lastReqId && error == null) {
                                messages_Messages res = response;
                                MessagesStorage.getInstance().putUsersAndChats(res.users, res.chats, true, true);
                                MessagesController.getInstance().putUsers(res.users, false);
                                MessagesController.getInstance().putChats(res.chats, false);
                                if (req.max_id == 0 && queryWithDialogFinal == j) {
                                    MessagesSearchQuery.lastReturnedNum = 0;
                                    MessagesSearchQuery.searchResultMessages.clear();
                                }
                                boolean added = false;
                                for (int a = 0; a < Math.min(res.messages.size(), 20); a++) {
                                    added = true;
                                    MessagesSearchQuery.searchResultMessages.add(new MessageObject((Message) res.messages.get(a), null, false));
                                }
                                MessagesSearchQuery.messagesSearchEndReached[queryWithDialogFinal == j ? 0 : 1] = res.messages.size() != 21;
                                if (j2 == 0) {
                                    MessagesSearchQuery.messagesSearchEndReached[1] = MessagesSearchQuery.messagesSearchEndReached[0];
                                }
                                if (MessagesSearchQuery.searchResultMessages.isEmpty()) {
                                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.chatSearchResultsAvailable, Integer.valueOf(i), Integer.valueOf(0), Integer.valueOf(MessagesSearchQuery.getMask()), Long.valueOf(0));
                                } else if (added) {
                                    if (MessagesSearchQuery.lastReturnedNum >= MessagesSearchQuery.searchResultMessages.size()) {
                                        MessagesSearchQuery.lastReturnedNum = MessagesSearchQuery.searchResultMessages.size() - 1;
                                    }
                                    MessageObject messageObject = (MessageObject) MessagesSearchQuery.searchResultMessages.get(MessagesSearchQuery.lastReturnedNum);
                                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.chatSearchResultsAvailable, Integer.valueOf(i), Integer.valueOf(messageObject.getId()), Integer.valueOf(MessagesSearchQuery.getMask()), Long.valueOf(messageObject.getDialogId()));
                                }
                                if (queryWithDialogFinal == j && MessagesSearchQuery.messagesSearchEndReached[0] && j2 != 0) {
                                    MessagesSearchQuery.messagesSearchEndReached[1] = false;
                                    MessagesSearchQuery.searchMessagesInChat(MessagesSearchQuery.lastSearchQuery, j, j2, i, 0);
                                }
                            }
                        }
                    });
                }
            }, 2);
        }
    }
}
