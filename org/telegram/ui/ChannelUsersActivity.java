package org.telegram.ui;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.C0553R;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.support.widget.helper.ItemTouchHelper.Callback;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.ChannelParticipant;
import org.telegram.tgnet.TLRPC.ChannelParticipantRole;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.TL_channelParticipantCreator;
import org.telegram.tgnet.TLRPC.TL_channelParticipantEditor;
import org.telegram.tgnet.TLRPC.TL_channelParticipantModerator;
import org.telegram.tgnet.TLRPC.TL_channelParticipantSelf;
import org.telegram.tgnet.TLRPC.TL_channelParticipantsAdmins;
import org.telegram.tgnet.TLRPC.TL_channelParticipantsKicked;
import org.telegram.tgnet.TLRPC.TL_channelParticipantsRecent;
import org.telegram.tgnet.TLRPC.TL_channelRoleEditor;
import org.telegram.tgnet.TLRPC.TL_channelRoleEmpty;
import org.telegram.tgnet.TLRPC.TL_channels_channelParticipants;
import org.telegram.tgnet.TLRPC.TL_channels_editAdmin;
import org.telegram.tgnet.TLRPC.TL_channels_getParticipants;
import org.telegram.tgnet.TLRPC.TL_channels_kickFromChannel;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.Updates;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Adapters.BaseFragmentAdapter;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.ContactsActivity.ContactsActivityDelegate;

public class ChannelUsersActivity extends BaseFragment implements NotificationCenterDelegate {
    private int chatId = this.arguments.getInt("chat_id");
    private EmptyTextProgressView emptyView;
    private boolean firstLoaded;
    private boolean isAdmin;
    private boolean isMegagroup;
    private boolean isPublic;
    private ListAdapter listViewAdapter;
    private boolean loadingUsers;
    private ArrayList<ChannelParticipant> participants = new ArrayList();
    private int participantsStartRow;
    private int type = this.arguments.getInt("type");

    class C08542 implements OnItemClickListener {

        class C15341 implements ContactsActivityDelegate {
            C15341() {
            }

            public void didSelectContact(User user, String param) {
                MessagesController.getInstance().addUserToChat(ChannelUsersActivity.this.chatId, user, null, param != null ? Utilities.parseInt(param).intValue() : 0, null, ChannelUsersActivity.this);
            }
        }

        class C15352 implements ContactsActivityDelegate {
            C15352() {
            }

            public void didSelectContact(User user, String param) {
                ChannelUsersActivity.this.setUserChannelRole(user, new TL_channelRoleEditor());
            }
        }

        C08542() {
        }

        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Bundle args;
            ContactsActivity fragment;
            if (ChannelUsersActivity.this.type == 2) {
                if (ChannelUsersActivity.this.isAdmin) {
                    if (i == 0) {
                        args = new Bundle();
                        args.putBoolean("onlyUsers", true);
                        args.putBoolean("destroyAfterSelect", true);
                        args.putBoolean("returnAsResult", true);
                        args.putBoolean("needForwardCount", false);
                        args.putBoolean("allowUsernameSearch", false);
                        args.putString("selectAlertString", LocaleController.getString("ChannelAddTo", C0553R.string.ChannelAddTo));
                        fragment = new ContactsActivity(args);
                        fragment.setDelegate(new C15341());
                        ChannelUsersActivity.this.presentFragment(fragment);
                    } else if (!ChannelUsersActivity.this.isPublic && i == 1) {
                        ChannelUsersActivity.this.presentFragment(new GroupInviteActivity(ChannelUsersActivity.this.chatId));
                    }
                }
            } else if (ChannelUsersActivity.this.type == 1 && ChannelUsersActivity.this.isAdmin && i == 0) {
                args = new Bundle();
                args.putBoolean("onlyUsers", true);
                args.putBoolean("destroyAfterSelect", true);
                args.putBoolean("returnAsResult", true);
                args.putBoolean("needForwardCount", false);
                args.putBoolean("allowUsernameSearch", true);
                if (ChannelUsersActivity.this.isMegagroup) {
                    args.putBoolean("allowBots", false);
                }
                args.putString("selectAlertString", LocaleController.getString("ChannelAddUserAdminAlert", C0553R.string.ChannelAddUserAdminAlert));
                fragment = new ContactsActivity(args);
                fragment.setDelegate(new C15352());
                ChannelUsersActivity.this.presentFragment(fragment);
            }
            ChannelParticipant participant = null;
            if (i >= ChannelUsersActivity.this.participantsStartRow && i < ChannelUsersActivity.this.participants.size() + ChannelUsersActivity.this.participantsStartRow) {
                participant = (ChannelParticipant) ChannelUsersActivity.this.participants.get(i - ChannelUsersActivity.this.participantsStartRow);
            }
            if (participant != null) {
                args = new Bundle();
                args.putInt("user_id", participant.user_id);
                ChannelUsersActivity.this.presentFragment(new ProfileActivity(args));
            }
        }
    }

    class C08573 implements OnItemLongClickListener {
        C08573() {
        }

        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
            if (ChannelUsersActivity.this.getParentActivity() == null) {
                return false;
            }
            ChannelParticipant participant = null;
            if (i >= ChannelUsersActivity.this.participantsStartRow && i < ChannelUsersActivity.this.participants.size() + ChannelUsersActivity.this.participantsStartRow) {
                participant = (ChannelParticipant) ChannelUsersActivity.this.participants.get(i - ChannelUsersActivity.this.participantsStartRow);
            }
            if (participant == null) {
                return false;
            }
            final ChannelParticipant finalParticipant = participant;
            Builder builder = new Builder(ChannelUsersActivity.this.getParentActivity());
            CharSequence[] items = null;
            if (ChannelUsersActivity.this.type == 0) {
                items = new CharSequence[]{LocaleController.getString("Unblock", C0553R.string.Unblock)};
            } else if (ChannelUsersActivity.this.type == 1) {
                items = new CharSequence[]{LocaleController.getString("ChannelRemoveUserAdmin", C0553R.string.ChannelRemoveUserAdmin)};
            } else if (ChannelUsersActivity.this.type == 2) {
                items = new CharSequence[]{LocaleController.getString("ChannelRemoveUser", C0553R.string.ChannelRemoveUser)};
            }
            builder.setItems(items, new OnClickListener() {

                class C15361 implements RequestDelegate {
                    C15361() {
                    }

                    public void run(TLObject response, TL_error error) {
                        final Updates updates = (Updates) response;
                        MessagesController.getInstance().processUpdates(updates, false);
                        if (!updates.chats.isEmpty()) {
                            AndroidUtilities.runOnUIThread(new Runnable() {
                                public void run() {
                                    MessagesController.getInstance().loadFullChat(((Chat) updates.chats.get(0)).id, 0, true);
                                }
                            }, 1000);
                        }
                    }
                }

                public void onClick(DialogInterface dialogInterface, int i) {
                    if (i != 0) {
                        return;
                    }
                    if (ChannelUsersActivity.this.type == 0) {
                        ChannelUsersActivity.this.participants.remove(finalParticipant);
                        ChannelUsersActivity.this.listViewAdapter.notifyDataSetChanged();
                        TL_channels_kickFromChannel req = new TL_channels_kickFromChannel();
                        req.kicked = false;
                        req.user_id = MessagesController.getInputUser(finalParticipant.user_id);
                        req.channel = MessagesController.getInputChannel(ChannelUsersActivity.this.chatId);
                        ConnectionsManager.getInstance().sendRequest(req, new C15361());
                    } else if (ChannelUsersActivity.this.type == 1) {
                        ChannelUsersActivity.this.setUserChannelRole(MessagesController.getInstance().getUser(Integer.valueOf(finalParticipant.user_id)), new TL_channelRoleEmpty());
                    } else if (ChannelUsersActivity.this.type == 2) {
                        MessagesController.getInstance().deleteUserFromChat(ChannelUsersActivity.this.chatId, MessagesController.getInstance().getUser(Integer.valueOf(finalParticipant.user_id)), null);
                    }
                }
            });
            ChannelUsersActivity.this.showDialog(builder.create());
            return true;
        }
    }

    class C08605 implements Runnable {
        C08605() {
        }

        public void run() {
            ChannelUsersActivity.this.getChannelParticipants(0, Callback.DEFAULT_DRAG_ANIMATION_DURATION);
        }
    }

    class C15331 extends ActionBarMenuOnItemClick {
        C15331() {
        }

        public void onItemClick(int id) {
            if (id == -1) {
                ChannelUsersActivity.this.finishFragment();
            }
        }
    }

    class C15374 implements RequestDelegate {

        class C08581 implements Runnable {
            C08581() {
            }

            public void run() {
                MessagesController.getInstance().loadFullChat(ChannelUsersActivity.this.chatId, 0, true);
            }
        }

        C15374() {
        }

        public void run(TLObject response, final TL_error error) {
            if (error == null) {
                MessagesController.getInstance().processUpdates((Updates) response, false);
                AndroidUtilities.runOnUIThread(new C08581(), 1000);
                return;
            }
            AndroidUtilities.runOnUIThread(new Runnable() {
                public void run() {
                    AlertsCreator.showAddUserAlert(error.text, ChannelUsersActivity.this, !ChannelUsersActivity.this.isMegagroup);
                }
            });
        }
    }

    class C15386 implements RequestDelegate {
        C15386() {
        }

        public void run(final TLObject response, final TL_error error) {
            AndroidUtilities.runOnUIThread(new Runnable() {

                class C08611 implements Comparator<ChannelParticipant> {
                    C08611() {
                    }

                    public int compare(ChannelParticipant lhs, ChannelParticipant rhs) {
                        User user1 = MessagesController.getInstance().getUser(Integer.valueOf(rhs.user_id));
                        User user2 = MessagesController.getInstance().getUser(Integer.valueOf(lhs.user_id));
                        int status1 = 0;
                        int status2 = 0;
                        if (!(user1 == null || user1.status == null)) {
                            status1 = user1.id == UserConfig.getClientUserId() ? ConnectionsManager.getInstance().getCurrentTime() + 50000 : user1.status.expires;
                        }
                        if (!(user2 == null || user2.status == null)) {
                            status2 = user2.id == UserConfig.getClientUserId() ? ConnectionsManager.getInstance().getCurrentTime() + 50000 : user2.status.expires;
                        }
                        if (status1 <= 0 || status2 <= 0) {
                            if (status1 >= 0 || status2 >= 0) {
                                if ((status1 < 0 && status2 > 0) || (status1 == 0 && status2 != 0)) {
                                    return -1;
                                }
                                if (status2 < 0 && status1 > 0) {
                                    return 1;
                                }
                                if (status2 != 0 || status1 == 0) {
                                    return 0;
                                }
                                return 1;
                            } else if (status1 > status2) {
                                return 1;
                            } else {
                                if (status1 < status2) {
                                    return -1;
                                }
                                return 0;
                            }
                        } else if (status1 > status2) {
                            return 1;
                        } else {
                            if (status1 < status2) {
                                return -1;
                            }
                            return 0;
                        }
                    }
                }

                class C08622 implements Comparator<ChannelParticipant> {
                    C08622() {
                    }

                    public int compare(ChannelParticipant lhs, ChannelParticipant rhs) {
                        int type1 = ChannelUsersActivity.this.getChannelAdminParticipantType(lhs);
                        int type2 = ChannelUsersActivity.this.getChannelAdminParticipantType(rhs);
                        if (type1 > type2) {
                            return 1;
                        }
                        if (type1 < type2) {
                            return -1;
                        }
                        return 0;
                    }
                }

                public void run() {
                    if (error == null) {
                        TL_channels_channelParticipants res = response;
                        MessagesController.getInstance().putUsers(res.users, false);
                        ChannelUsersActivity.this.participants = res.participants;
                        try {
                            if (ChannelUsersActivity.this.type == 0 || ChannelUsersActivity.this.type == 2) {
                                Collections.sort(ChannelUsersActivity.this.participants, new C08611());
                            } else if (ChannelUsersActivity.this.type == 1) {
                                Collections.sort(res.participants, new C08622());
                            }
                        } catch (Throwable e) {
                            FileLog.m611e("tmessages", e);
                        }
                    }
                    ChannelUsersActivity.this.loadingUsers = false;
                    ChannelUsersActivity.this.firstLoaded = true;
                    if (ChannelUsersActivity.this.emptyView != null) {
                        ChannelUsersActivity.this.emptyView.showTextView();
                    }
                    if (ChannelUsersActivity.this.listViewAdapter != null) {
                        ChannelUsersActivity.this.listViewAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }

    private class ListAdapter extends BaseFragmentAdapter {
        private Context mContext;

        public ListAdapter(Context context) {
            this.mContext = context;
        }

        public boolean areAllItemsEnabled() {
            return false;
        }

        public boolean isEnabled(int i) {
            boolean z;
            if (ChannelUsersActivity.this.type == 2) {
                if (ChannelUsersActivity.this.isAdmin) {
                    if (ChannelUsersActivity.this.isPublic) {
                        if (i == 0) {
                            return true;
                        }
                        if (i == 1) {
                            return false;
                        }
                    } else if (i == 0 || i == 1) {
                        return true;
                    } else {
                        if (i == 2) {
                            return false;
                        }
                    }
                }
            } else if (ChannelUsersActivity.this.type == 1 && ChannelUsersActivity.this.isAdmin) {
                if (i == 0) {
                    return true;
                }
                if (i == 1) {
                    return false;
                }
            }
            if (i == ChannelUsersActivity.this.participants.size() + ChannelUsersActivity.this.participantsStartRow || ((ChannelParticipant) ChannelUsersActivity.this.participants.get(i - ChannelUsersActivity.this.participantsStartRow)).user_id == UserConfig.getClientUserId()) {
                z = false;
            } else {
                z = true;
            }
            return z;
        }

        public int getCount() {
            if ((!ChannelUsersActivity.this.participants.isEmpty() || ChannelUsersActivity.this.type != 0) && (!ChannelUsersActivity.this.loadingUsers || ChannelUsersActivity.this.firstLoaded)) {
                return (ChannelUsersActivity.this.participants.size() + ChannelUsersActivity.this.participantsStartRow) + 1;
            }
            return 0;
        }

        public Object getItem(int i) {
            return null;
        }

        public long getItemId(int i) {
            return (long) i;
        }

        public boolean hasStableIds() {
            return false;
        }

        public View getView(int i, View view, ViewGroup viewGroup) {
            int viewType = getItemViewType(i);
            if (viewType == 0) {
                if (view == null) {
                    view = new UserCell(this.mContext, 1, 0);
                    view.setBackgroundColor(-1);
                }
                UserCell userCell = (UserCell) view;
                ChannelParticipant participant = (ChannelParticipant) ChannelUsersActivity.this.participants.get(i - ChannelUsersActivity.this.participantsStartRow);
                User user = MessagesController.getInstance().getUser(Integer.valueOf(participant.user_id));
                if (user == null) {
                    return view;
                }
                if (ChannelUsersActivity.this.type == 0) {
                    CharSequence string;
                    if (user.phone == null || user.phone.length() == 0) {
                        string = LocaleController.getString("NumberUnknown", C0553R.string.NumberUnknown);
                    } else {
                        string = PhoneFormat.getInstance().format("+" + user.phone);
                    }
                    userCell.setData(user, null, string, 0);
                    return view;
                } else if (ChannelUsersActivity.this.type == 1) {
                    String role = null;
                    if ((participant instanceof TL_channelParticipantCreator) || (participant instanceof TL_channelParticipantSelf)) {
                        role = LocaleController.getString("ChannelCreator", C0553R.string.ChannelCreator);
                    } else if (participant instanceof TL_channelParticipantModerator) {
                        role = LocaleController.getString("ChannelModerator", C0553R.string.ChannelModerator);
                    } else if (participant instanceof TL_channelParticipantEditor) {
                        role = LocaleController.getString("ChannelEditor", C0553R.string.ChannelEditor);
                    }
                    userCell.setData(user, null, role, 0);
                    return view;
                } else if (ChannelUsersActivity.this.type != 2) {
                    return view;
                } else {
                    userCell.setData(user, null, null, 0);
                    return view;
                }
            } else if (viewType == 1) {
                if (view == null) {
                    view = new TextInfoPrivacyCell(this.mContext);
                }
                if (ChannelUsersActivity.this.type == 0) {
                    ((TextInfoPrivacyCell) view).setText(String.format("%1$s\n\n%2$s", new Object[]{LocaleController.getString("NoBlockedGroup", C0553R.string.NoBlockedGroup), LocaleController.getString("UnblockText", C0553R.string.UnblockText)}));
                    view.setBackgroundResource(C0553R.drawable.greydivider_bottom);
                    return view;
                } else if (ChannelUsersActivity.this.type == 1) {
                    if (i == 1 && ChannelUsersActivity.this.isAdmin) {
                        if (ChannelUsersActivity.this.isMegagroup) {
                            ((TextInfoPrivacyCell) view).setText(LocaleController.getString("MegaAdminsInfo", C0553R.string.MegaAdminsInfo));
                        } else {
                            ((TextInfoPrivacyCell) view).setText(LocaleController.getString("ChannelAdminsInfo", C0553R.string.ChannelAdminsInfo));
                        }
                        view.setBackgroundResource(C0553R.drawable.greydivider);
                        return view;
                    }
                    ((TextInfoPrivacyCell) view).setText("");
                    view.setBackgroundResource(C0553R.drawable.greydivider_bottom);
                    return view;
                } else if (ChannelUsersActivity.this.type != 2) {
                    return view;
                } else {
                    if (((ChannelUsersActivity.this.isPublic || i != 2) && i != 1) || !ChannelUsersActivity.this.isAdmin) {
                        ((TextInfoPrivacyCell) view).setText("");
                        view.setBackgroundResource(C0553R.drawable.greydivider_bottom);
                        return view;
                    }
                    if (ChannelUsersActivity.this.isMegagroup) {
                        ((TextInfoPrivacyCell) view).setText("");
                    } else {
                        ((TextInfoPrivacyCell) view).setText(LocaleController.getString("ChannelMembersInfo", C0553R.string.ChannelMembersInfo));
                    }
                    view.setBackgroundResource(C0553R.drawable.greydivider);
                    return view;
                }
            } else if (viewType == 2) {
                if (view == null) {
                    view = new TextSettingsCell(this.mContext);
                    view.setBackgroundColor(-1);
                }
                TextSettingsCell actionCell = (TextSettingsCell) view;
                if (ChannelUsersActivity.this.type == 2) {
                    if (i == 0) {
                        actionCell.setText(LocaleController.getString("AddMember", C0553R.string.AddMember), true);
                        return view;
                    } else if (i != 1) {
                        return view;
                    } else {
                        actionCell.setText(LocaleController.getString("ChannelInviteViaLink", C0553R.string.ChannelInviteViaLink), false);
                        return view;
                    }
                } else if (ChannelUsersActivity.this.type != 1) {
                    return view;
                } else {
                    actionCell.setText(LocaleController.getString("ChannelAddAdmin", C0553R.string.ChannelAddAdmin), true);
                    return view;
                }
            } else if (viewType == 3 && view == null) {
                return new ShadowSectionCell(this.mContext);
            } else {
                return view;
            }
        }

        public int getItemViewType(int i) {
            if (ChannelUsersActivity.this.type == 1) {
                if (ChannelUsersActivity.this.isAdmin) {
                    if (i == 0) {
                        return 2;
                    }
                    if (i == 1) {
                        return 1;
                    }
                }
            } else if (ChannelUsersActivity.this.type == 2 && ChannelUsersActivity.this.isAdmin) {
                if (ChannelUsersActivity.this.isPublic) {
                    if (i == 0) {
                        return 2;
                    }
                    if (i == 1) {
                        return 1;
                    }
                } else if (i == 0 || i == 1) {
                    return 2;
                } else {
                    if (i == 2) {
                        return 1;
                    }
                }
            }
            if (i == ChannelUsersActivity.this.participants.size() + ChannelUsersActivity.this.participantsStartRow) {
                return 1;
            }
            return 0;
        }

        public int getViewTypeCount() {
            return 4;
        }

        public boolean isEmpty() {
            return ChannelUsersActivity.this.participants.isEmpty();
        }
    }

    public ChannelUsersActivity(Bundle args) {
        int i = 2;
        int i2 = 0;
        super(args);
        Chat chat = MessagesController.getInstance().getChat(Integer.valueOf(this.chatId));
        if (chat != null) {
            if (chat.creator) {
                this.isAdmin = true;
                this.isPublic = (chat.flags & 64) != 0;
            }
            this.isMegagroup = chat.megagroup;
        }
        if (this.type == 0) {
            this.participantsStartRow = 0;
        } else if (this.type == 1) {
            if (this.isAdmin) {
                i2 = 2;
            }
            this.participantsStartRow = i2;
        } else if (this.type == 2) {
            if (!this.isAdmin) {
                i = 0;
            } else if (!this.isPublic) {
                i = 3;
            }
            this.participantsStartRow = i;
        }
    }

    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.chatInfoDidLoaded);
        getChannelParticipants(0, Callback.DEFAULT_DRAG_ANIMATION_DURATION);
        return true;
    }

    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.chatInfoDidLoaded);
    }

    public View createView(Context context) {
        int i = 1;
        this.actionBar.setBackButtonImage(C0553R.drawable.ic_ab_back);
        this.actionBar.setAllowOverlayTitle(true);
        if (this.type == 0) {
            this.actionBar.setTitle(LocaleController.getString("ChannelBlockedUsers", C0553R.string.ChannelBlockedUsers));
        } else if (this.type == 1) {
            this.actionBar.setTitle(LocaleController.getString("ChannelAdministrators", C0553R.string.ChannelAdministrators));
        } else if (this.type == 2) {
            this.actionBar.setTitle(LocaleController.getString("ChannelMembers", C0553R.string.ChannelMembers));
        }
        this.actionBar.setActionBarMenuOnItemClick(new C15331());
        ActionBarMenu menu = this.actionBar.createMenu();
        this.fragmentView = new FrameLayout(context);
        this.fragmentView.setBackgroundColor(-986896);
        FrameLayout frameLayout = this.fragmentView;
        this.emptyView = new EmptyTextProgressView(context);
        if (this.type == 0) {
            if (this.isMegagroup) {
                this.emptyView.setText(LocaleController.getString("NoBlockedGroup", C0553R.string.NoBlockedGroup));
            } else {
                this.emptyView.setText(LocaleController.getString("NoBlocked", C0553R.string.NoBlocked));
            }
        }
        frameLayout.addView(this.emptyView, LayoutHelper.createFrame(-1, GroundOverlayOptions.NO_DIMENSION));
        ListView listView = new ListView(context);
        listView.setEmptyView(this.emptyView);
        listView.setDivider(null);
        listView.setDividerHeight(0);
        listView.setDrawSelectorOnTop(true);
        android.widget.ListAdapter listAdapter = new ListAdapter(context);
        this.listViewAdapter = listAdapter;
        listView.setAdapter(listAdapter);
        if (VERSION.SDK_INT >= 11) {
            if (!LocaleController.isRTL) {
                i = 2;
            }
            listView.setVerticalScrollbarPosition(i);
        }
        frameLayout.addView(listView, LayoutHelper.createFrame(-1, GroundOverlayOptions.NO_DIMENSION));
        listView.setOnItemClickListener(new C08542());
        if (this.isAdmin || (this.isMegagroup && this.type == 0)) {
            listView.setOnItemLongClickListener(new C08573());
        }
        if (this.loadingUsers) {
            this.emptyView.showProgress();
        } else {
            this.emptyView.showTextView();
        }
        return this.fragmentView;
    }

    public void setUserChannelRole(User user, ChannelParticipantRole role) {
        if (user != null && role != null) {
            TL_channels_editAdmin req = new TL_channels_editAdmin();
            req.channel = MessagesController.getInputChannel(this.chatId);
            req.user_id = MessagesController.getInputUser(user);
            req.role = role;
            ConnectionsManager.getInstance().sendRequest(req, new C15374());
        }
    }

    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.chatInfoDidLoaded && args[0].id == this.chatId) {
            AndroidUtilities.runOnUIThread(new C08605());
        }
    }

    private int getChannelAdminParticipantType(ChannelParticipant participant) {
        if ((participant instanceof TL_channelParticipantCreator) || (participant instanceof TL_channelParticipantSelf)) {
            return 0;
        }
        if (participant instanceof TL_channelParticipantEditor) {
            return 1;
        }
        return 2;
    }

    private void getChannelParticipants(int offset, int count) {
        if (!this.loadingUsers) {
            this.loadingUsers = true;
            if (!(this.emptyView == null || this.firstLoaded)) {
                this.emptyView.showProgress();
            }
            if (this.listViewAdapter != null) {
                this.listViewAdapter.notifyDataSetChanged();
            }
            TL_channels_getParticipants req = new TL_channels_getParticipants();
            req.channel = MessagesController.getInputChannel(this.chatId);
            if (this.type == 0) {
                req.filter = new TL_channelParticipantsKicked();
            } else if (this.type == 1) {
                req.filter = new TL_channelParticipantsAdmins();
            } else if (this.type == 2) {
                req.filter = new TL_channelParticipantsRecent();
            }
            req.offset = offset;
            req.limit = count;
            ConnectionsManager.getInstance().bindRequestToGuid(ConnectionsManager.getInstance().sendRequest(req, new C15386()), this.classGuid);
        }
    }

    public void onResume() {
        super.onResume();
        if (this.listViewAdapter != null) {
            this.listViewAdapter.notifyDataSetChanged();
        }
    }
}
