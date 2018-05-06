package org.telegram.ui.Adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.TLRPC.BotInfo;
import org.telegram.tgnet.TLRPC.ChatFull;
import org.telegram.tgnet.TLRPC.ChatParticipant;
import org.telegram.tgnet.TLRPC.TL_botCommand;
import org.telegram.tgnet.TLRPC.TL_channelFull;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.ui.Cells.MentionCell;

public class MentionsAdapter extends BaseSearchAdapter {
    private HashMap<Integer, BotInfo> botInfo;
    private int botsCount;
    private MentionsAdapterDelegate delegate;
    private ChatFull info;
    private boolean isDarkTheme;
    private int lastPosition;
    private String lastText;
    private Context mContext;
    private ArrayList<MessageObject> messages;
    private boolean needUsernames = true;
    private int resultLength;
    private int resultStartPosition;
    private ArrayList<String> searchResultCommands;
    private ArrayList<String> searchResultCommandsHelp;
    private ArrayList<User> searchResultCommandsUsers;
    private ArrayList<String> searchResultHashtags;
    private ArrayList<User> searchResultUsernames;

    public interface MentionsAdapterDelegate {
        void needChangePanelVisibility(boolean z);
    }

    public MentionsAdapter(Context context, boolean isDarkTheme, MentionsAdapterDelegate delegate) {
        this.mContext = context;
        this.delegate = delegate;
        this.isDarkTheme = isDarkTheme;
    }

    public void setChatInfo(ChatFull chatParticipants) {
        this.info = chatParticipants;
        if (this.lastText != null) {
            searchUsernameOrHashtag(this.lastText, this.lastPosition, this.messages);
        }
    }

    public void setNeedUsernames(boolean value) {
        this.needUsernames = value;
    }

    public void setBotInfo(HashMap<Integer, BotInfo> info) {
        this.botInfo = info;
    }

    public void setBotsCount(int count) {
        this.botsCount = count;
    }

    public void clearRecentHashtags() {
        super.clearRecentHashtags();
        this.searchResultHashtags.clear();
        notifyDataSetChanged();
        if (this.delegate != null) {
            this.delegate.needChangePanelVisibility(false);
        }
    }

    protected void setHashtags(ArrayList<HashtagObject> arrayList, HashMap<String, HashtagObject> hashMap) {
        super.setHashtags(arrayList, hashMap);
        if (this.lastText != null) {
            searchUsernameOrHashtag(this.lastText, this.lastPosition, this.messages);
        }
    }

    public void searchUsernameOrHashtag(String text, int position, ArrayList<MessageObject> messageObjects) {
        if (text == null || text.length() == 0) {
            this.delegate.needChangePanelVisibility(false);
            this.lastText = null;
            return;
        }
        int searchPostion = position;
        if (text.length() > 0) {
            searchPostion--;
        }
        this.lastText = null;
        StringBuilder result = new StringBuilder();
        int foundType = -1;
        boolean hasIllegalUsernameCharacters = false;
        int a = searchPostion;
        while (a >= 0) {
            if (a < text.length()) {
                char ch = text.charAt(a);
                if (a == 0 || text.charAt(a - 1) == ' ' || text.charAt(a - 1) == '\n') {
                    if (!this.needUsernames || ch != '@') {
                        if (ch != '#') {
                            if (a == 0 && this.botInfo != null && ch == '/') {
                                foundType = 2;
                                this.resultStartPosition = a;
                                this.resultLength = result.length() + 1;
                                break;
                            }
                        } else if (this.hashtagsLoadedFromDb) {
                            foundType = 1;
                            this.resultStartPosition = a;
                            this.resultLength = result.length() + 1;
                            result.insert(0, ch);
                        } else {
                            loadRecentHashtags();
                            this.lastText = text;
                            this.lastPosition = position;
                            this.messages = messageObjects;
                            this.delegate.needChangePanelVisibility(false);
                            return;
                        }
                    } else if (hasIllegalUsernameCharacters) {
                        this.delegate.needChangePanelVisibility(false);
                        return;
                    } else if (this.info == null) {
                        this.lastText = text;
                        this.lastPosition = position;
                        this.messages = messageObjects;
                        this.delegate.needChangePanelVisibility(false);
                        return;
                    } else {
                        foundType = 0;
                        this.resultStartPosition = a;
                        this.resultLength = result.length() + 1;
                    }
                }
                if ((ch < '0' || ch > '9') && ((ch < 'a' || ch > 'z') && ((ch < 'A' || ch > 'Z') && ch != '_'))) {
                    hasIllegalUsernameCharacters = true;
                }
                result.insert(0, ch);
            }
            a--;
        }
        if (foundType == -1) {
            this.delegate.needChangePanelVisibility(false);
        } else if (foundType == 0) {
            ArrayList<Integer> users = new ArrayList();
            for (a = 0; a < Math.min(100, messageObjects.size()); a++) {
                int from_id = ((MessageObject) messageObjects.get(a)).messageOwner.from_id;
                if (!users.contains(Integer.valueOf(from_id))) {
                    users.add(Integer.valueOf(from_id));
                }
            }
            String usernameString = result.toString().toLowerCase();
            ArrayList<User> newResult = new ArrayList();
            if (this.info.participants != null) {
                for (a = 0; a < this.info.participants.participants.size(); a++) {
                    User user = MessagesController.getInstance().getUser(Integer.valueOf(((ChatParticipant) this.info.participants.participants.get(a)).user_id));
                    if (!(user == null || UserObject.isUserSelf(user) || user.username == null || user.username.length() <= 0 || ((usernameString.length() <= 0 || !user.username.toLowerCase().startsWith(usernameString)) && usernameString.length() != 0))) {
                        newResult.add(user);
                    }
                }
            }
            this.searchResultHashtags = null;
            this.searchResultCommands = null;
            this.searchResultCommandsHelp = null;
            this.searchResultCommandsUsers = null;
            this.searchResultUsernames = newResult;
            final ArrayList<Integer> arrayList = users;
            Collections.sort(this.searchResultUsernames, new Comparator<User>() {
                public int compare(User lhs, User rhs) {
                    int lhsNum = arrayList.indexOf(Integer.valueOf(lhs.id));
                    int rhsNum = arrayList.indexOf(Integer.valueOf(rhs.id));
                    if (lhsNum == -1 || rhsNum == -1) {
                        if (lhsNum != -1 && rhsNum == -1) {
                            return -1;
                        }
                        if (lhsNum != -1 || rhsNum == -1) {
                            return 0;
                        }
                        return 1;
                    } else if (lhsNum < rhsNum) {
                        return -1;
                    } else {
                        return lhsNum == rhsNum ? 0 : 1;
                    }
                }
            });
            notifyDataSetChanged();
            this.delegate.needChangePanelVisibility(!newResult.isEmpty());
        } else if (foundType == 1) {
            newResult = new ArrayList();
            String hashtagString = result.toString().toLowerCase();
            i$ = this.hashtags.iterator();
            while (i$.hasNext()) {
                HashtagObject hashtagObject = (HashtagObject) i$.next();
                if (!(hashtagObject == null || hashtagObject.hashtag == null || !hashtagObject.hashtag.startsWith(hashtagString))) {
                    newResult.add(hashtagObject.hashtag);
                }
            }
            this.searchResultHashtags = newResult;
            this.searchResultUsernames = null;
            this.searchResultCommands = null;
            this.searchResultCommandsHelp = null;
            this.searchResultCommandsUsers = null;
            notifyDataSetChanged();
            this.delegate.needChangePanelVisibility(!newResult.isEmpty());
        } else if (foundType == 2) {
            newResult = new ArrayList();
            ArrayList<String> newResultHelp = new ArrayList();
            ArrayList<User> newResultUsers = new ArrayList();
            String command = result.toString().toLowerCase();
            for (Entry<Integer, BotInfo> entry : this.botInfo.entrySet()) {
                BotInfo botInfo = (BotInfo) entry.getValue();
                for (a = 0; a < botInfo.commands.size(); a++) {
                    TL_botCommand botCommand = (TL_botCommand) botInfo.commands.get(a);
                    if (!(botCommand == null || botCommand.command == null || !botCommand.command.startsWith(command))) {
                        newResult.add("/" + botCommand.command);
                        newResultHelp.add(botCommand.description);
                        newResultUsers.add(MessagesController.getInstance().getUser(Integer.valueOf(botInfo.user_id)));
                    }
                }
            }
            this.searchResultHashtags = null;
            this.searchResultUsernames = null;
            this.searchResultCommands = newResult;
            this.searchResultCommandsHelp = newResultHelp;
            this.searchResultCommandsUsers = newResultUsers;
            notifyDataSetChanged();
            this.delegate.needChangePanelVisibility(!newResult.isEmpty());
        }
    }

    public int getResultStartPosition() {
        return this.resultStartPosition;
    }

    public int getResultLength() {
        return this.resultLength;
    }

    public int getViewTypeCount() {
        return 1;
    }

    public int getCount() {
        if (this.searchResultUsernames != null) {
            return this.searchResultUsernames.size();
        }
        if (this.searchResultHashtags != null) {
            return this.searchResultHashtags.size();
        }
        if (this.searchResultCommands != null) {
            return this.searchResultCommands.size();
        }
        return 0;
    }

    public boolean isEmpty() {
        if (this.searchResultUsernames != null) {
            return this.searchResultUsernames.isEmpty();
        }
        if (this.searchResultHashtags != null) {
            return this.searchResultHashtags.isEmpty();
        }
        if (this.searchResultCommands != null) {
            return this.searchResultCommands.isEmpty();
        }
        return true;
    }

    public int getItemViewType(int position) {
        return 0;
    }

    public boolean hasStableIds() {
        return true;
    }

    public boolean areAllItemsEnabled() {
        return true;
    }

    public boolean isEnabled(int position) {
        return true;
    }

    public Object getItem(int i) {
        if (this.searchResultUsernames != null) {
            if (i < 0 || i >= this.searchResultUsernames.size()) {
                return null;
            }
            return this.searchResultUsernames.get(i);
        } else if (this.searchResultHashtags != null) {
            if (i < 0 || i >= this.searchResultHashtags.size()) {
                return null;
            }
            return this.searchResultHashtags.get(i);
        } else if (this.searchResultCommands == null || i < 0 || i >= this.searchResultCommands.size()) {
            return null;
        } else {
            if (this.searchResultCommandsUsers == null || (this.botsCount == 1 && !(this.info instanceof TL_channelFull))) {
                return this.searchResultCommands.get(i);
            }
            if (this.searchResultCommandsUsers.get(i) != null) {
                String str = "%s@%s";
                Object[] objArr = new Object[2];
                objArr[0] = this.searchResultCommands.get(i);
                objArr[1] = this.searchResultCommandsUsers.get(i) != null ? ((User) this.searchResultCommandsUsers.get(i)).username : "";
                return String.format(str, objArr);
            }
            return String.format("%s", new Object[]{this.searchResultCommands.get(i)});
        }
    }

    public boolean isLongClickEnabled() {
        return this.searchResultHashtags != null;
    }

    public boolean isBotCommands() {
        return this.searchResultCommands != null;
    }

    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = new MentionCell(this.mContext);
            ((MentionCell) view).setIsDarkTheme(this.isDarkTheme);
        }
        if (this.searchResultUsernames != null) {
            ((MentionCell) view).setUser((User) this.searchResultUsernames.get(i));
        } else if (this.searchResultHashtags != null) {
            ((MentionCell) view).setText((String) this.searchResultHashtags.get(i));
        } else if (this.searchResultCommands != null) {
            ((MentionCell) view).setBotCommand((String) this.searchResultCommands.get(i), (String) this.searchResultCommandsHelp.get(i), this.searchResultCommandsUsers != null ? (User) this.searchResultCommandsUsers.get(i) : null);
        }
        return view;
    }
}
