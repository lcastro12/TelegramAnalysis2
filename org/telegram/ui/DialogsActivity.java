package org.telegram.ui;

import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Outline;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import java.util.ArrayList;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.AnimationCompat.ObjectAnimatorProxy;
import org.telegram.messenger.AnimationCompat.ViewProxy;
import org.telegram.messenger.C0553R;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView;
import org.telegram.messenger.support.widget.RecyclerView.Adapter;
import org.telegram.messenger.support.widget.RecyclerView.OnScrollListener;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.Dialog;
import org.telegram.tgnet.TLRPC.EncryptedChat;
import org.telegram.tgnet.TLRPC.TL_dialogChannel;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.ActionBarMenuItem.ActionBarMenuItemSearchListener;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet.Builder;
import org.telegram.ui.ActionBar.MenuDrawable;
import org.telegram.ui.Adapters.DialogsAdapter;
import org.telegram.ui.Adapters.DialogsSearchAdapter;
import org.telegram.ui.Adapters.DialogsSearchAdapter.MessagesActivitySearchAdapterDelegate;
import org.telegram.ui.Cells.DialogCell;
import org.telegram.ui.Cells.ProfileSearchCell;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.PlayerView;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.RecyclerListView.OnItemClickListener;
import org.telegram.ui.Components.RecyclerListView.OnItemLongClickListener;
import org.telegram.ui.Components.ResourceLoader;

public class DialogsActivity extends BaseFragment implements NotificationCenterDelegate {
    private static boolean dialogsLoaded;
    private String addToGroupAlertString;
    private boolean checkPermission = true;
    private MessagesActivityDelegate delegate;
    private DialogsAdapter dialogsAdapter;
    private DialogsSearchAdapter dialogsSearchAdapter;
    private int dialogsType;
    private LinearLayout emptyView;
    private ImageView floatingButton;
    private boolean floatingHidden;
    private final AccelerateDecelerateInterpolator floatingInterpolator = new AccelerateDecelerateInterpolator();
    private LinearLayoutManager layoutManager;
    private RecyclerListView listView;
    private boolean onlySelect;
    private long openedDialogId;
    private ActionBarMenuItem passcodeItem;
    private AlertDialog permissionDialog;
    private int prevPosition;
    private int prevTop;
    private ProgressBar progressView;
    private boolean scrollUpdated;
    private EmptyTextProgressView searchEmptyView;
    private String searchString;
    private boolean searchWas;
    private boolean searching;
    private String selectAlertString;
    private String selectAlertStringGroup;
    private long selectedDialog;

    class C09986 implements OnTouchListener {
        C09986() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            return true;
        }
    }

    class C09997 extends ViewOutlineProvider {
        C09997() {
        }

        @SuppressLint({"NewApi"})
        public void getOutline(View view, Outline outline) {
            outline.setOval(0, 0, AndroidUtilities.dp(56.0f), AndroidUtilities.dp(56.0f));
        }
    }

    class C10008 implements OnClickListener {
        C10008() {
        }

        public void onClick(View v) {
            Bundle args = new Bundle();
            args.putBoolean("destroyAfterSelect", true);
            DialogsActivity.this.presentFragment(new ContactsActivity(args));
        }
    }

    public interface MessagesActivityDelegate {
        void didSelectDialog(DialogsActivity dialogsActivity, long j, boolean z);
    }

    class C15831 extends ActionBarMenuItemSearchListener {
        C15831() {
        }

        public void onSearchExpand() {
            DialogsActivity.this.searching = true;
            if (DialogsActivity.this.listView != null) {
                if (DialogsActivity.this.searchString != null) {
                    DialogsActivity.this.listView.setEmptyView(DialogsActivity.this.searchEmptyView);
                    DialogsActivity.this.progressView.setVisibility(8);
                    DialogsActivity.this.emptyView.setVisibility(8);
                }
                if (!DialogsActivity.this.onlySelect) {
                    DialogsActivity.this.floatingButton.setVisibility(8);
                }
            }
            DialogsActivity.this.updatePasscodeButton();
        }

        public boolean canCollapseSearch() {
            if (DialogsActivity.this.searchString == null) {
                return true;
            }
            DialogsActivity.this.finishFragment();
            return false;
        }

        public void onSearchCollapse() {
            DialogsActivity.this.searching = false;
            DialogsActivity.this.searchWas = false;
            if (DialogsActivity.this.listView != null) {
                DialogsActivity.this.searchEmptyView.setVisibility(8);
                if (MessagesController.getInstance().loadingDialogs && MessagesController.getInstance().dialogs.isEmpty()) {
                    DialogsActivity.this.emptyView.setVisibility(8);
                    DialogsActivity.this.listView.setEmptyView(DialogsActivity.this.progressView);
                } else {
                    DialogsActivity.this.progressView.setVisibility(8);
                    DialogsActivity.this.listView.setEmptyView(DialogsActivity.this.emptyView);
                }
                if (!DialogsActivity.this.onlySelect) {
                    DialogsActivity.this.floatingButton.setVisibility(0);
                    DialogsActivity.this.floatingHidden = true;
                    ViewProxy.setTranslationY(DialogsActivity.this.floatingButton, (float) AndroidUtilities.dp(100.0f));
                    DialogsActivity.this.hideFloatingButton(false);
                }
                if (DialogsActivity.this.listView.getAdapter() != DialogsActivity.this.dialogsAdapter) {
                    DialogsActivity.this.listView.setAdapter(DialogsActivity.this.dialogsAdapter);
                    DialogsActivity.this.dialogsAdapter.notifyDataSetChanged();
                }
            }
            if (DialogsActivity.this.dialogsSearchAdapter != null) {
                DialogsActivity.this.dialogsSearchAdapter.searchDialogs(null);
            }
            DialogsActivity.this.updatePasscodeButton();
        }

        public void onTextChanged(EditText editText) {
            String text = editText.getText().toString();
            if (text.length() != 0 || (DialogsActivity.this.dialogsSearchAdapter != null && DialogsActivity.this.dialogsSearchAdapter.hasRecentRearch())) {
                DialogsActivity.this.searchWas = true;
                if (!(DialogsActivity.this.dialogsSearchAdapter == null || DialogsActivity.this.listView.getAdapter() == DialogsActivity.this.dialogsSearchAdapter)) {
                    DialogsActivity.this.listView.setAdapter(DialogsActivity.this.dialogsSearchAdapter);
                    DialogsActivity.this.dialogsSearchAdapter.notifyDataSetChanged();
                }
                if (!(DialogsActivity.this.searchEmptyView == null || DialogsActivity.this.listView.getEmptyView() == DialogsActivity.this.searchEmptyView)) {
                    DialogsActivity.this.emptyView.setVisibility(8);
                    DialogsActivity.this.progressView.setVisibility(8);
                    DialogsActivity.this.searchEmptyView.showTextView();
                    DialogsActivity.this.listView.setEmptyView(DialogsActivity.this.searchEmptyView);
                }
            }
            if (DialogsActivity.this.dialogsSearchAdapter != null) {
                DialogsActivity.this.dialogsSearchAdapter.searchDialogs(text);
            }
        }
    }

    class C15842 extends ActionBarMenuOnItemClick {
        C15842() {
        }

        public void onItemClick(int id) {
            boolean z = true;
            if (id == -1) {
                if (DialogsActivity.this.onlySelect) {
                    DialogsActivity.this.finishFragment();
                } else if (DialogsActivity.this.parentLayout != null) {
                    DialogsActivity.this.parentLayout.getDrawerLayoutContainer().openDrawer(false);
                }
            } else if (id == 1) {
                if (UserConfig.appLocked) {
                    z = false;
                }
                UserConfig.appLocked = z;
                UserConfig.saveConfig(false);
                DialogsActivity.this.updatePasscodeButton();
            }
        }
    }

    class C15854 implements OnItemClickListener {
        C15854() {
        }

        public void onItemClick(View view, int position) {
            if (DialogsActivity.this.listView != null && DialogsActivity.this.listView.getAdapter() != null) {
                long dialog_id = 0;
                int message_id = 0;
                Adapter adapter = DialogsActivity.this.listView.getAdapter();
                if (adapter == DialogsActivity.this.dialogsAdapter) {
                    Dialog dialog = DialogsActivity.this.dialogsAdapter.getItem(position);
                    if (dialog != null) {
                        dialog_id = dialog.id;
                    } else {
                        return;
                    }
                } else if (adapter == DialogsActivity.this.dialogsSearchAdapter) {
                    MessageObject obj = DialogsActivity.this.dialogsSearchAdapter.getItem(position);
                    if (obj instanceof User) {
                        dialog_id = (long) ((User) obj).id;
                        if (DialogsActivity.this.dialogsSearchAdapter.isGlobalSearch(position)) {
                            ArrayList<User> users = new ArrayList();
                            users.add((User) obj);
                            MessagesController.getInstance().putUsers(users, false);
                            MessagesStorage.getInstance().putUsersAndChats(users, null, false, true);
                        }
                        if (!DialogsActivity.this.onlySelect) {
                            DialogsActivity.this.dialogsSearchAdapter.putRecentSearch(dialog_id, (User) obj);
                        }
                    } else if (obj instanceof Chat) {
                        if (DialogsActivity.this.dialogsSearchAdapter.isGlobalSearch(position)) {
                            ArrayList<Chat> chats = new ArrayList();
                            chats.add((Chat) obj);
                            MessagesController.getInstance().putChats(chats, false);
                            MessagesStorage.getInstance().putUsersAndChats(null, chats, false, true);
                        }
                        if (((Chat) obj).id > 0) {
                            dialog_id = (long) (-((Chat) obj).id);
                        } else {
                            dialog_id = AndroidUtilities.makeBroadcastId(((Chat) obj).id);
                        }
                        if (!DialogsActivity.this.onlySelect) {
                            DialogsActivity.this.dialogsSearchAdapter.putRecentSearch(dialog_id, (Chat) obj);
                        }
                    } else if (obj instanceof EncryptedChat) {
                        dialog_id = ((long) ((EncryptedChat) obj).id) << 32;
                        if (!DialogsActivity.this.onlySelect) {
                            DialogsActivity.this.dialogsSearchAdapter.putRecentSearch(dialog_id, (EncryptedChat) obj);
                        }
                    } else if (obj instanceof MessageObject) {
                        MessageObject messageObject = obj;
                        dialog_id = messageObject.getDialogId();
                        message_id = messageObject.getId();
                        DialogsActivity.this.dialogsSearchAdapter.addHashtagsFromMessage(DialogsActivity.this.dialogsSearchAdapter.getLastSearchString());
                    } else if (obj instanceof String) {
                        DialogsActivity.this.actionBar.openSearchField((String) obj);
                    }
                }
                if (dialog_id == 0) {
                    return;
                }
                if (DialogsActivity.this.onlySelect) {
                    DialogsActivity.this.didSelectResult(dialog_id, true, false);
                    return;
                }
                Bundle args = new Bundle();
                int lower_part = (int) dialog_id;
                int high_id = (int) (dialog_id >> 32);
                if (lower_part == 0) {
                    args.putInt("enc_id", high_id);
                } else if (high_id == 1) {
                    args.putInt("chat_id", lower_part);
                } else if (lower_part > 0) {
                    args.putInt("user_id", lower_part);
                } else if (lower_part < 0) {
                    if (message_id != 0) {
                        Chat chat = MessagesController.getInstance().getChat(Integer.valueOf(-lower_part));
                        if (!(chat == null || chat.migrated_to == null)) {
                            args.putInt("migrated_to", lower_part);
                            lower_part = -chat.migrated_to.channel_id;
                        }
                    }
                    args.putInt("chat_id", -lower_part);
                }
                if (message_id != 0) {
                    args.putInt("message_id", message_id);
                } else if (DialogsActivity.this.actionBar != null) {
                    DialogsActivity.this.actionBar.closeSearchField();
                }
                if (AndroidUtilities.isTablet()) {
                    if (DialogsActivity.this.openedDialogId == dialog_id && adapter != DialogsActivity.this.dialogsSearchAdapter) {
                        return;
                    }
                    if (DialogsActivity.this.dialogsAdapter != null) {
                        DialogsActivity.this.dialogsAdapter.setOpenedDialogId(DialogsActivity.this.openedDialogId = dialog_id);
                        DialogsActivity.this.updateVisibleRows(512);
                    }
                }
                if (DialogsActivity.this.searchString != null) {
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats, new Object[0]);
                    DialogsActivity.this.presentFragment(new ChatActivity(args));
                    return;
                }
                DialogsActivity.this.presentFragment(new ChatActivity(args));
            }
        }
    }

    class C15865 implements OnItemLongClickListener {

        class C09921 implements DialogInterface.OnClickListener {
            C09921() {
            }

            public void onClick(DialogInterface dialogInterface, int i) {
                if (DialogsActivity.this.dialogsSearchAdapter.isRecentSearchDisplayed()) {
                    DialogsActivity.this.dialogsSearchAdapter.clearRecentSearch();
                } else {
                    DialogsActivity.this.dialogsSearchAdapter.clearRecentHashtags();
                }
            }
        }

        C15865() {
        }

        public boolean onItemClick(View view, int position) {
            if (!DialogsActivity.this.onlySelect && ((!DialogsActivity.this.searching || !DialogsActivity.this.searchWas) && DialogsActivity.this.getParentActivity() != null)) {
                ArrayList<Dialog> dialogs = DialogsActivity.this.getDialogsArray();
                if (position < 0 || position >= dialogs.size()) {
                    return false;
                }
                Dialog dialog = (Dialog) dialogs.get(position);
                DialogsActivity.this.selectedDialog = dialog.id;
                Builder builder = new Builder(DialogsActivity.this.getParentActivity());
                int lower_id = (int) DialogsActivity.this.selectedDialog;
                int high_id = (int) (DialogsActivity.this.selectedDialog >> 32);
                String string;
                if (dialog instanceof TL_dialogChannel) {
                    CharSequence[] items;
                    final Chat chat = MessagesController.getInstance().getChat(Integer.valueOf(-lower_id));
                    if (chat == null || !chat.megagroup) {
                        items = new CharSequence[2];
                        items[0] = LocaleController.getString("ClearHistoryCache", C0553R.string.ClearHistoryCache);
                        if (chat == null || !chat.creator) {
                            string = LocaleController.getString("LeaveChannelMenu", C0553R.string.LeaveChannelMenu);
                        } else {
                            string = LocaleController.getString("ChannelDeleteMenu", C0553R.string.ChannelDeleteMenu);
                        }
                        items[1] = string;
                    } else {
                        items = new CharSequence[2];
                        items[0] = LocaleController.getString("ClearHistoryCache", C0553R.string.ClearHistoryCache);
                        string = (chat == null || !chat.creator) ? LocaleController.getString("LeaveMegaMenu", C0553R.string.LeaveMegaMenu) : LocaleController.getString("DeleteMegaMenu", C0553R.string.DeleteMegaMenu);
                        items[1] = string;
                    }
                    builder.setItems(items, new DialogInterface.OnClickListener() {

                        class C09931 implements DialogInterface.OnClickListener {
                            C09931() {
                            }

                            public void onClick(DialogInterface dialogInterface, int i) {
                                MessagesController.getInstance().deleteDialog(DialogsActivity.this.selectedDialog, 2);
                            }
                        }

                        class C09942 implements DialogInterface.OnClickListener {
                            C09942() {
                            }

                            public void onClick(DialogInterface dialogInterface, int i) {
                                MessagesController.getInstance().deleteUserFromChat((int) (-DialogsActivity.this.selectedDialog), UserConfig.getCurrentUser(), null);
                                if (AndroidUtilities.isTablet()) {
                                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats, Long.valueOf(DialogsActivity.this.selectedDialog));
                                }
                            }
                        }

                        public void onClick(DialogInterface dialog, int which) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(DialogsActivity.this.getParentActivity());
                            builder.setTitle(LocaleController.getString("AppName", C0553R.string.AppName));
                            if (which == 0) {
                                if (chat == null || !chat.megagroup) {
                                    builder.setMessage(LocaleController.getString("AreYouSureClearHistoryChannel", C0553R.string.AreYouSureClearHistoryChannel));
                                } else {
                                    builder.setMessage(LocaleController.getString("AreYouSureClearHistorySuper", C0553R.string.AreYouSureClearHistorySuper));
                                }
                                builder.setPositiveButton(LocaleController.getString("OK", C0553R.string.OK), new C09931());
                            } else {
                                if (chat == null || !chat.megagroup) {
                                    if (chat == null || !chat.creator) {
                                        builder.setMessage(LocaleController.getString("ChannelLeaveAlert", C0553R.string.ChannelLeaveAlert));
                                    } else {
                                        builder.setMessage(LocaleController.getString("ChannelDeleteAlert", C0553R.string.ChannelDeleteAlert));
                                    }
                                } else if (chat.creator) {
                                    builder.setMessage(LocaleController.getString("MegaDeleteAlert", C0553R.string.MegaDeleteAlert));
                                } else {
                                    builder.setMessage(LocaleController.getString("MegaLeaveAlert", C0553R.string.MegaLeaveAlert));
                                }
                                builder.setPositiveButton(LocaleController.getString("OK", C0553R.string.OK), new C09942());
                            }
                            builder.setNegativeButton(LocaleController.getString("Cancel", C0553R.string.Cancel), null);
                            DialogsActivity.this.showDialog(builder.create());
                        }
                    });
                    DialogsActivity.this.showDialog(builder.create());
                } else {
                    final boolean isChat = lower_id < 0 && high_id != 1;
                    User user = null;
                    if (!(isChat || lower_id <= 0 || high_id == 1)) {
                        user = MessagesController.getInstance().getUser(Integer.valueOf(lower_id));
                    }
                    final boolean isBot = user != null && user.bot;
                    CharSequence[] charSequenceArr = new CharSequence[2];
                    charSequenceArr[0] = LocaleController.getString("ClearHistory", C0553R.string.ClearHistory);
                    string = isChat ? LocaleController.getString("DeleteChat", C0553R.string.DeleteChat) : isBot ? LocaleController.getString("DeleteAndStop", C0553R.string.DeleteAndStop) : LocaleController.getString("Delete", C0553R.string.Delete);
                    charSequenceArr[1] = string;
                    builder.setItems(charSequenceArr, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, final int which) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(DialogsActivity.this.getParentActivity());
                            builder.setTitle(LocaleController.getString("AppName", C0553R.string.AppName));
                            if (which == 0) {
                                builder.setMessage(LocaleController.getString("AreYouSureClearHistory", C0553R.string.AreYouSureClearHistory));
                            } else if (isChat) {
                                builder.setMessage(LocaleController.getString("AreYouSureDeleteAndExit", C0553R.string.AreYouSureDeleteAndExit));
                            } else {
                                builder.setMessage(LocaleController.getString("AreYouSureDeleteThisChat", C0553R.string.AreYouSureDeleteThisChat));
                            }
                            builder.setPositiveButton(LocaleController.getString("OK", C0553R.string.OK), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (which != 0) {
                                        if (isChat) {
                                            Chat currentChat = MessagesController.getInstance().getChat(Integer.valueOf((int) (-DialogsActivity.this.selectedDialog)));
                                            if (currentChat == null || !ChatObject.isNotInChat(currentChat)) {
                                                MessagesController.getInstance().deleteUserFromChat((int) (-DialogsActivity.this.selectedDialog), MessagesController.getInstance().getUser(Integer.valueOf(UserConfig.getClientUserId())), null);
                                            } else {
                                                MessagesController.getInstance().deleteDialog(DialogsActivity.this.selectedDialog, 0);
                                            }
                                        } else {
                                            MessagesController.getInstance().deleteDialog(DialogsActivity.this.selectedDialog, 0);
                                        }
                                        if (isBot) {
                                            MessagesController.getInstance().blockUser((int) DialogsActivity.this.selectedDialog);
                                        }
                                        if (AndroidUtilities.isTablet()) {
                                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats, Long.valueOf(DialogsActivity.this.selectedDialog));
                                            return;
                                        }
                                        return;
                                    }
                                    MessagesController.getInstance().deleteDialog(DialogsActivity.this.selectedDialog, 1);
                                }
                            });
                            builder.setNegativeButton(LocaleController.getString("Cancel", C0553R.string.Cancel), null);
                            DialogsActivity.this.showDialog(builder.create());
                        }
                    });
                    DialogsActivity.this.showDialog(builder.create());
                }
                return true;
            } else if (((!DialogsActivity.this.searchWas || !DialogsActivity.this.searching) && !DialogsActivity.this.dialogsSearchAdapter.isRecentSearchDisplayed()) || DialogsActivity.this.listView.getAdapter() != DialogsActivity.this.dialogsSearchAdapter || (!(DialogsActivity.this.dialogsSearchAdapter.getItem(position) instanceof String) && !DialogsActivity.this.dialogsSearchAdapter.isRecentSearchDisplayed())) {
                return false;
            } else {
                AlertDialog.Builder builder2 = new AlertDialog.Builder(DialogsActivity.this.getParentActivity());
                builder2.setTitle(LocaleController.getString("AppName", C0553R.string.AppName));
                builder2.setMessage(LocaleController.getString("ClearSearch", C0553R.string.ClearSearch));
                builder2.setPositiveButton(LocaleController.getString("ClearButton", C0553R.string.ClearButton).toUpperCase(), new C09921());
                builder2.setNegativeButton(LocaleController.getString("Cancel", C0553R.string.Cancel), null);
                DialogsActivity.this.showDialog(builder2.create());
                return true;
            }
        }
    }

    class C15879 extends OnScrollListener {
        C15879() {
        }

        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            if (newState == 1 && DialogsActivity.this.searching && DialogsActivity.this.searchWas) {
                AndroidUtilities.hideKeyboard(DialogsActivity.this.getParentActivity().getCurrentFocus());
            }
        }

        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            int firstVisibleItem = DialogsActivity.this.layoutManager.findFirstVisibleItemPosition();
            int visibleItemCount = Math.abs(DialogsActivity.this.layoutManager.findLastVisibleItemPosition() - firstVisibleItem) + 1;
            int totalItemCount = recyclerView.getAdapter().getItemCount();
            if (!DialogsActivity.this.searching || !DialogsActivity.this.searchWas) {
                if (visibleItemCount > 0 && DialogsActivity.this.layoutManager.findLastVisibleItemPosition() >= DialogsActivity.this.getDialogsArray().size() - 10) {
                    MessagesController.getInstance().loadDialogs(-1, 100, !MessagesController.getInstance().dialogsEndReached);
                }
                if (DialogsActivity.this.floatingButton.getVisibility() != 8) {
                    boolean goingDown;
                    View topChild = recyclerView.getChildAt(0);
                    int firstViewTop = 0;
                    if (topChild != null) {
                        firstViewTop = topChild.getTop();
                    }
                    boolean changed = true;
                    if (DialogsActivity.this.prevPosition == firstVisibleItem) {
                        int topDelta = DialogsActivity.this.prevTop - firstViewTop;
                        goingDown = firstViewTop < DialogsActivity.this.prevTop;
                        changed = Math.abs(topDelta) > 1;
                    } else {
                        goingDown = firstVisibleItem > DialogsActivity.this.prevPosition;
                    }
                    if (changed && DialogsActivity.this.scrollUpdated) {
                        DialogsActivity.this.hideFloatingButton(goingDown);
                    }
                    DialogsActivity.this.prevPosition = firstVisibleItem;
                    DialogsActivity.this.prevTop = firstViewTop;
                    DialogsActivity.this.scrollUpdated = true;
                }
            } else if (visibleItemCount > 0 && DialogsActivity.this.layoutManager.findLastVisibleItemPosition() == totalItemCount - 1 && !DialogsActivity.this.dialogsSearchAdapter.isMessagesSearchEndReached()) {
                DialogsActivity.this.dialogsSearchAdapter.loadMoreSearchMessages();
            }
        }
    }

    public DialogsActivity(Bundle args) {
        super(args);
    }

    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        if (getArguments() != null) {
            this.onlySelect = this.arguments.getBoolean("onlySelect", false);
            this.dialogsType = this.arguments.getInt("dialogsType", 0);
            this.selectAlertString = this.arguments.getString("selectAlertString");
            this.selectAlertStringGroup = this.arguments.getString("selectAlertStringGroup");
            this.addToGroupAlertString = this.arguments.getString("addToGroupAlertString");
        }
        if (this.searchString == null) {
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.dialogsNeedReload);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.emojiDidLoaded);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.updateInterfaces);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.encryptedChatUpdated);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.contactsDidLoaded);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.appDidLogout);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.openedChatChanged);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.notificationsSettingsUpdated);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.messageReceivedByAck);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.messageReceivedByServer);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.messageSendError);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.didSetPasscode);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.needReloadRecentDialogsSearch);
        }
        if (!dialogsLoaded) {
            MessagesController.getInstance().loadDialogs(0, 100, true);
            ContactsController.getInstance().checkInviteText();
            dialogsLoaded = true;
        }
        return true;
    }

    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        if (this.searchString == null) {
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.dialogsNeedReload);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.emojiDidLoaded);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.updateInterfaces);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.encryptedChatUpdated);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.contactsDidLoaded);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.appDidLogout);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.openedChatChanged);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.notificationsSettingsUpdated);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.messageReceivedByAck);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.messageReceivedByServer);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.messageSendError);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.didSetPasscode);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.needReloadRecentDialogsSearch);
        }
        this.delegate = null;
    }

    public View createView(Context context) {
        float f;
        float f2;
        this.searching = false;
        this.searchWas = false;
        ResourceLoader.loadRecources(context);
        ActionBarMenu menu = this.actionBar.createMenu();
        if (!this.onlySelect && this.searchString == null) {
            this.passcodeItem = menu.addItem(1, (int) C0553R.drawable.lock_close);
            updatePasscodeButton();
        }
        menu.addItem(0, (int) C0553R.drawable.ic_ab_search).setIsSearchField(true).setActionBarMenuItemSearchListener(new C15831()).getSearchField().setHint(LocaleController.getString("Search", C0553R.string.Search));
        if (this.onlySelect) {
            this.actionBar.setBackButtonImage(C0553R.drawable.ic_ab_back);
            this.actionBar.setTitle(LocaleController.getString("SelectChat", C0553R.string.SelectChat));
        } else {
            if (this.searchString != null) {
                this.actionBar.setBackButtonImage(C0553R.drawable.ic_ab_back);
            } else {
                this.actionBar.setBackButtonDrawable(new MenuDrawable());
            }
            this.actionBar.setTitle(LocaleController.getString("AppName", C0553R.string.AppName));
        }
        this.actionBar.setAllowOverlayTitle(true);
        this.actionBar.setActionBarMenuOnItemClick(new C15842());
        FrameLayout frameLayout = new FrameLayout(context);
        this.fragmentView = frameLayout;
        this.listView = new RecyclerListView(context);
        this.listView.setVerticalScrollBarEnabled(true);
        this.listView.setItemAnimator(null);
        this.listView.setInstantClick(true);
        this.listView.setLayoutAnimation(null);
        this.layoutManager = new LinearLayoutManager(context) {
            public boolean supportsPredictiveItemAnimations() {
                return false;
            }
        };
        this.layoutManager.setOrientation(1);
        this.listView.setLayoutManager(this.layoutManager);
        if (VERSION.SDK_INT >= 11) {
            this.listView.setVerticalScrollbarPosition(LocaleController.isRTL ? 1 : 2);
        }
        frameLayout.addView(this.listView, LayoutHelper.createFrame(-1, GroundOverlayOptions.NO_DIMENSION));
        this.listView.setOnItemClickListener(new C15854());
        this.listView.setOnItemLongClickListener(new C15865());
        this.searchEmptyView = new EmptyTextProgressView(context);
        this.searchEmptyView.setVisibility(8);
        this.searchEmptyView.setShowAtCenter(true);
        this.searchEmptyView.setText(LocaleController.getString("NoResult", C0553R.string.NoResult));
        frameLayout.addView(this.searchEmptyView, LayoutHelper.createFrame(-1, GroundOverlayOptions.NO_DIMENSION));
        this.emptyView = new LinearLayout(context);
        this.emptyView.setOrientation(1);
        this.emptyView.setVisibility(8);
        this.emptyView.setGravity(17);
        frameLayout.addView(this.emptyView, LayoutHelper.createFrame(-1, GroundOverlayOptions.NO_DIMENSION));
        this.emptyView.setOnTouchListener(new C09986());
        View textView = new TextView(context);
        textView.setText(LocaleController.getString("NoChats", C0553R.string.NoChats));
        textView.setTextColor(-6974059);
        textView.setGravity(17);
        textView.setTextSize(1, 20.0f);
        this.emptyView.addView(textView, LayoutHelper.createLinear(-2, -2));
        textView = new TextView(context);
        String help = LocaleController.getString("NoChatsHelp", C0553R.string.NoChatsHelp);
        if (AndroidUtilities.isTablet() && !AndroidUtilities.isSmallTablet()) {
            help = help.replace("\n", " ");
        }
        textView.setText(help);
        textView.setTextColor(-6974059);
        textView.setTextSize(1, 15.0f);
        textView.setGravity(17);
        textView.setPadding(AndroidUtilities.dp(8.0f), AndroidUtilities.dp(6.0f), AndroidUtilities.dp(8.0f), 0);
        textView.setLineSpacing((float) AndroidUtilities.dp(2.0f), 1.0f);
        this.emptyView.addView(textView, LayoutHelper.createLinear(-2, -2));
        this.progressView = new ProgressBar(context);
        this.progressView.setVisibility(8);
        frameLayout.addView(this.progressView, LayoutHelper.createFrame(-2, -2, 17));
        this.floatingButton = new ImageView(context);
        this.floatingButton.setVisibility(this.onlySelect ? 8 : 0);
        this.floatingButton.setScaleType(ScaleType.CENTER);
        this.floatingButton.setBackgroundResource(C0553R.drawable.floating_states);
        this.floatingButton.setImageResource(C0553R.drawable.floating_pencil);
        if (VERSION.SDK_INT >= 21) {
            StateListAnimator animator = new StateListAnimator();
            animator.addState(new int[]{16842919}, ObjectAnimator.ofFloat(this.floatingButton, "translationZ", new float[]{(float) AndroidUtilities.dp(2.0f), (float) AndroidUtilities.dp(4.0f)}).setDuration(200));
            animator.addState(new int[0], ObjectAnimator.ofFloat(this.floatingButton, "translationZ", new float[]{(float) AndroidUtilities.dp(4.0f), (float) AndroidUtilities.dp(2.0f)}).setDuration(200));
            this.floatingButton.setStateListAnimator(animator);
            this.floatingButton.setOutlineProvider(new C09997());
        }
        View view = this.floatingButton;
        int i = (LocaleController.isRTL ? 3 : 5) | 80;
        if (LocaleController.isRTL) {
            f = 14.0f;
        } else {
            f = 0.0f;
        }
        if (LocaleController.isRTL) {
            f2 = 0.0f;
        } else {
            f2 = 14.0f;
        }
        frameLayout.addView(view, LayoutHelper.createFrame(-2, -2.0f, i, f, 0.0f, f2, 14.0f));
        this.floatingButton.setOnClickListener(new C10008());
        this.listView.setOnScrollListener(new C15879());
        if (this.searchString == null) {
            this.dialogsAdapter = new DialogsAdapter(context, this.dialogsType);
            if (AndroidUtilities.isTablet() && this.openedDialogId != 0) {
                this.dialogsAdapter.setOpenedDialogId(this.openedDialogId);
            }
            this.listView.setAdapter(this.dialogsAdapter);
        }
        int type = 0;
        if (this.searchString != null) {
            type = 2;
        } else if (!this.onlySelect) {
            type = 1;
        }
        this.dialogsSearchAdapter = new DialogsSearchAdapter(context, type, this.dialogsType);
        this.dialogsSearchAdapter.setDelegate(new MessagesActivitySearchAdapterDelegate() {
            public void searchStateChanged(boolean search) {
                if (!DialogsActivity.this.searching || !DialogsActivity.this.searchWas || DialogsActivity.this.searchEmptyView == null) {
                    return;
                }
                if (search) {
                    DialogsActivity.this.searchEmptyView.showProgress();
                } else {
                    DialogsActivity.this.searchEmptyView.showTextView();
                }
            }
        });
        if (MessagesController.getInstance().loadingDialogs && MessagesController.getInstance().dialogs.isEmpty()) {
            this.searchEmptyView.setVisibility(8);
            this.emptyView.setVisibility(8);
            this.listView.setEmptyView(this.progressView);
        } else {
            this.searchEmptyView.setVisibility(8);
            this.progressView.setVisibility(8);
            this.listView.setEmptyView(this.emptyView);
        }
        if (this.searchString != null) {
            this.actionBar.openSearchField(this.searchString);
        }
        if (!this.onlySelect && this.dialogsType == 0) {
            frameLayout.addView(new PlayerView(context, this), LayoutHelper.createFrame(-1, 39.0f, 51, 0.0f, -36.0f, 0.0f, 0.0f));
        }
        return this.fragmentView;
    }

    public void onResume() {
        super.onResume();
        if (this.dialogsAdapter != null) {
            this.dialogsAdapter.notifyDataSetChanged();
        }
        if (this.dialogsSearchAdapter != null) {
            this.dialogsSearchAdapter.notifyDataSetChanged();
        }
        if (this.checkPermission && !this.onlySelect && VERSION.SDK_INT >= 23) {
            Activity activity = getParentActivity();
            if (activity != null) {
                this.checkPermission = false;
                if (activity.checkSelfPermission("android.permission.READ_CONTACTS") != 0 || activity.checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") != 0) {
                    AlertDialog.Builder builder;
                    android.app.Dialog create;
                    if (activity.shouldShowRequestPermissionRationale("android.permission.READ_CONTACTS")) {
                        builder = new AlertDialog.Builder(activity);
                        builder.setTitle(LocaleController.getString("AppName", C0553R.string.AppName));
                        builder.setMessage(LocaleController.getString("PermissionContacts", C0553R.string.PermissionContacts));
                        builder.setPositiveButton(LocaleController.getString("OK", C0553R.string.OK), null);
                        create = builder.create();
                        this.permissionDialog = create;
                        showDialog(create);
                    } else if (activity.shouldShowRequestPermissionRationale("android.permission.WRITE_EXTERNAL_STORAGE")) {
                        builder = new AlertDialog.Builder(activity);
                        builder.setTitle(LocaleController.getString("AppName", C0553R.string.AppName));
                        builder.setMessage(LocaleController.getString("PermissionStorage", C0553R.string.PermissionStorage));
                        builder.setPositiveButton(LocaleController.getString("OK", C0553R.string.OK), null);
                        create = builder.create();
                        this.permissionDialog = create;
                        showDialog(create);
                    } else {
                        askForPermissons();
                    }
                }
            }
        }
    }

    @TargetApi(23)
    private void askForPermissons() {
        Activity activity = getParentActivity();
        if (activity != null) {
            ArrayList<String> permissons = new ArrayList();
            if (activity.checkSelfPermission("android.permission.READ_CONTACTS") != 0) {
                permissons.add("android.permission.READ_CONTACTS");
                permissons.add("android.permission.WRITE_CONTACTS");
                permissons.add("android.permission.GET_ACCOUNTS");
            }
            if (activity.checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") != 0) {
                permissons.add("android.permission.READ_EXTERNAL_STORAGE");
                permissons.add("android.permission.WRITE_EXTERNAL_STORAGE");
            }
            activity.requestPermissions((String[]) permissons.toArray(new String[permissons.size()]), 1);
        }
    }

    protected void onDialogDismiss(android.app.Dialog dialog) {
        super.onDialogDismiss(dialog);
        if (this.permissionDialog != null && dialog == this.permissionDialog && getParentActivity() != null) {
            askForPermissons();
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (!this.onlySelect && this.floatingButton != null) {
            this.floatingButton.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    ViewProxy.setTranslationY(DialogsActivity.this.floatingButton, DialogsActivity.this.floatingHidden ? (float) AndroidUtilities.dp(100.0f) : 0.0f);
                    DialogsActivity.this.floatingButton.setClickable(!DialogsActivity.this.floatingHidden);
                    if (DialogsActivity.this.floatingButton == null) {
                        return;
                    }
                    if (VERSION.SDK_INT < 16) {
                        DialogsActivity.this.floatingButton.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    } else {
                        DialogsActivity.this.floatingButton.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            });
        }
    }

    public void onRequestPermissionsResultFragment(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            for (int a = 0; a < permissions.length; a++) {
                if (grantResults[a] == 0) {
                    String str = permissions[a];
                    Object obj = -1;
                    switch (str.hashCode()) {
                        case 1365911975:
                            if (str.equals("android.permission.WRITE_EXTERNAL_STORAGE")) {
                                int i = 1;
                                break;
                            }
                            break;
                        case 1977429404:
                            if (str.equals("android.permission.READ_CONTACTS")) {
                                obj = null;
                                break;
                            }
                            break;
                    }
                    switch (obj) {
                        case null:
                            ContactsController.getInstance().readContacts();
                            break;
                        case 1:
                            ImageLoader.getInstance().createMediaPaths();
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }

    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.dialogsNeedReload) {
            if (this.dialogsAdapter != null) {
                if (this.dialogsAdapter.isDataSetChanged()) {
                    this.dialogsAdapter.notifyDataSetChanged();
                } else {
                    updateVisibleRows(2048);
                }
            }
            if (this.dialogsSearchAdapter != null) {
                this.dialogsSearchAdapter.notifyDataSetChanged();
            }
            if (this.listView != null) {
                if (MessagesController.getInstance().loadingDialogs && MessagesController.getInstance().dialogs.isEmpty()) {
                    this.searchEmptyView.setVisibility(8);
                    this.emptyView.setVisibility(8);
                    this.listView.setEmptyView(this.progressView);
                } else {
                    try {
                        this.progressView.setVisibility(8);
                        if (this.searching && this.searchWas) {
                            this.emptyView.setVisibility(8);
                            this.listView.setEmptyView(this.searchEmptyView);
                        } else {
                            this.searchEmptyView.setVisibility(8);
                            this.listView.setEmptyView(this.emptyView);
                        }
                    } catch (Throwable e) {
                        FileLog.m611e("tmessages", e);
                    }
                }
            }
        } else if (id == NotificationCenter.emojiDidLoaded) {
            if (this.listView != null) {
                updateVisibleRows(0);
            }
        } else if (id == NotificationCenter.updateInterfaces) {
            updateVisibleRows(((Integer) args[0]).intValue());
        } else if (id == NotificationCenter.appDidLogout) {
            dialogsLoaded = false;
        } else if (id == NotificationCenter.encryptedChatUpdated) {
            updateVisibleRows(0);
        } else if (id == NotificationCenter.contactsDidLoaded) {
            updateVisibleRows(0);
        } else if (id == NotificationCenter.openedChatChanged) {
            if (this.dialogsType == 0 && AndroidUtilities.isTablet()) {
                boolean close = ((Boolean) args[1]).booleanValue();
                long dialog_id = ((Long) args[0]).longValue();
                if (!close) {
                    this.openedDialogId = dialog_id;
                } else if (dialog_id == this.openedDialogId) {
                    this.openedDialogId = 0;
                }
                if (this.dialogsAdapter != null) {
                    this.dialogsAdapter.setOpenedDialogId(this.openedDialogId);
                }
                updateVisibleRows(512);
            }
        } else if (id == NotificationCenter.notificationsSettingsUpdated) {
            updateVisibleRows(0);
        } else if (id == NotificationCenter.messageReceivedByAck || id == NotificationCenter.messageReceivedByServer || id == NotificationCenter.messageSendError) {
            updateVisibleRows(4096);
        } else if (id == NotificationCenter.didSetPasscode) {
            updatePasscodeButton();
        }
        if (id == NotificationCenter.needReloadRecentDialogsSearch && this.dialogsSearchAdapter != null) {
            this.dialogsSearchAdapter.loadRecentSearch();
        }
    }

    private ArrayList<Dialog> getDialogsArray() {
        if (this.dialogsType == 0) {
            return MessagesController.getInstance().dialogs;
        }
        if (this.dialogsType == 1) {
            return MessagesController.getInstance().dialogsServerOnly;
        }
        if (this.dialogsType == 2) {
            return MessagesController.getInstance().dialogsGroupsOnly;
        }
        return null;
    }

    private void updatePasscodeButton() {
        if (this.passcodeItem != null) {
            if (UserConfig.passcodeHash.length() == 0 || this.searching) {
                this.passcodeItem.setVisibility(8);
                return;
            }
            this.passcodeItem.setVisibility(0);
            if (UserConfig.appLocked) {
                this.passcodeItem.setIcon(C0553R.drawable.lock_close);
            } else {
                this.passcodeItem.setIcon(C0553R.drawable.lock_open);
            }
        }
    }

    private void hideFloatingButton(boolean hide) {
        if (this.floatingHidden != hide) {
            boolean z;
            this.floatingHidden = hide;
            ImageView imageView = this.floatingButton;
            String str = "translationY";
            float[] fArr = new float[1];
            fArr[0] = this.floatingHidden ? (float) AndroidUtilities.dp(100.0f) : 0.0f;
            ObjectAnimatorProxy animator = ObjectAnimatorProxy.ofFloatProxy(imageView, str, fArr).setDuration(300);
            animator.setInterpolator(this.floatingInterpolator);
            imageView = this.floatingButton;
            if (hide) {
                z = false;
            } else {
                z = true;
            }
            imageView.setClickable(z);
            animator.start();
        }
    }

    private void updateVisibleRows(int mask) {
        if (this.listView != null) {
            int count = this.listView.getChildCount();
            for (int a = 0; a < count; a++) {
                View child = this.listView.getChildAt(a);
                if (child instanceof DialogCell) {
                    if (this.listView.getAdapter() != this.dialogsSearchAdapter) {
                        DialogCell cell = (DialogCell) child;
                        if ((mask & 2048) != 0) {
                            cell.checkCurrentDialogIndex();
                            if (this.dialogsType == 0 && AndroidUtilities.isTablet()) {
                                cell.setDialogSelected(cell.getDialogId() == this.openedDialogId);
                            }
                        } else if ((mask & 512) == 0) {
                            cell.update(mask);
                        } else if (this.dialogsType == 0 && AndroidUtilities.isTablet()) {
                            cell.setDialogSelected(cell.getDialogId() == this.openedDialogId);
                        }
                    }
                } else if (child instanceof UserCell) {
                    ((UserCell) child).update(mask);
                } else if (child instanceof ProfileSearchCell) {
                    ((ProfileSearchCell) child).update(mask);
                }
            }
        }
    }

    public void setDelegate(MessagesActivityDelegate delegate) {
        this.delegate = delegate;
    }

    public void setSearchString(String string) {
        this.searchString = string;
    }

    public boolean isMainDialogList() {
        return this.delegate == null && this.searchString == null;
    }

    private void didSelectResult(final long dialog_id, boolean useAlert, boolean param) {
        AlertDialog.Builder builder;
        if (this.addToGroupAlertString == null && ((int) dialog_id) < 0 && ChatObject.isChannel(-((int) dialog_id)) && !ChatObject.isCanWriteToChannel(-((int) dialog_id))) {
            builder = new AlertDialog.Builder(getParentActivity());
            builder.setTitle(LocaleController.getString("AppName", C0553R.string.AppName));
            builder.setMessage(LocaleController.getString("ChannelCantSendMessage", C0553R.string.ChannelCantSendMessage));
            builder.setNegativeButton(LocaleController.getString("OK", C0553R.string.OK), null);
            showDialog(builder.create());
        } else if (!useAlert || ((this.selectAlertString == null || this.selectAlertStringGroup == null) && this.addToGroupAlertString == null)) {
            if (this.delegate != null) {
                this.delegate.didSelectDialog(this, dialog_id, param);
                this.delegate = null;
                return;
            }
            finishFragment();
        } else if (getParentActivity() != null) {
            builder = new AlertDialog.Builder(getParentActivity());
            builder.setTitle(LocaleController.getString("AppName", C0553R.string.AppName));
            int lower_part = (int) dialog_id;
            int high_id = (int) (dialog_id >> 32);
            if (lower_part == 0) {
                if (MessagesController.getInstance().getUser(Integer.valueOf(MessagesController.getInstance().getEncryptedChat(Integer.valueOf(high_id)).user_id)) != null) {
                    builder.setMessage(LocaleController.formatStringSimple(this.selectAlertString, UserObject.getUserName(user)));
                } else {
                    return;
                }
            } else if (high_id == 1) {
                if (MessagesController.getInstance().getChat(Integer.valueOf(lower_part)) != null) {
                    builder.setMessage(LocaleController.formatStringSimple(this.selectAlertStringGroup, chat.title));
                } else {
                    return;
                }
            } else if (lower_part > 0) {
                if (MessagesController.getInstance().getUser(Integer.valueOf(lower_part)) != null) {
                    builder.setMessage(LocaleController.formatStringSimple(this.selectAlertString, UserObject.getUserName(user)));
                } else {
                    return;
                }
            } else if (lower_part < 0) {
                if (MessagesController.getInstance().getChat(Integer.valueOf(-lower_part)) == null) {
                    return;
                }
                if (this.addToGroupAlertString != null) {
                    builder.setMessage(LocaleController.formatStringSimple(this.addToGroupAlertString, chat.title));
                } else {
                    builder.setMessage(LocaleController.formatStringSimple(this.selectAlertStringGroup, chat.title));
                }
            }
            builder.setPositiveButton(LocaleController.getString("OK", C0553R.string.OK), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    DialogsActivity.this.didSelectResult(dialog_id, false, false);
                }
            });
            builder.setNegativeButton(LocaleController.getString("Cancel", C0553R.string.Cancel), null);
            showDialog(builder.create());
        }
    }
}
