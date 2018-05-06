package org.telegram.ui;

import android.app.AlertDialog.Builder;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Build.VERSION;
import android.os.Message;
import android.text.ClipboardManager;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.plus.PlusShare;
import java.util.ArrayList;
import java.util.Locale;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.C0553R;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.query.StickersQuery;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView;
import org.telegram.messenger.support.widget.RecyclerView.Adapter;
import org.telegram.messenger.support.widget.RecyclerView.ViewHolder;
import org.telegram.messenger.support.widget.helper.ItemTouchHelper;
import org.telegram.messenger.support.widget.helper.ItemTouchHelper.Callback;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.Document;
import org.telegram.tgnet.TLRPC.StickerSet;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_messages_reorderStickerSets;
import org.telegram.tgnet.TLRPC.TL_messages_stickerSet;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Cells.StickerSetCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.RecyclerListView.OnItemClickListener;
import org.telegram.ui.Components.StickersAlert;
import org.telegram.ui.Components.URLSpanNoUnderline;

public class StickersActivity extends BaseFragment implements NotificationCenterDelegate {
    private ListAdapter listAdapter;
    private RecyclerListView listView;
    private boolean needReorder;
    private int rowCount;
    private int stickersEndRow;
    private int stickersInfoRow;
    private int stickersStartRow;

    class C16741 extends ActionBarMenuOnItemClick {
        C16741() {
        }

        public void onItemClick(int id) {
            if (id == -1) {
                StickersActivity.this.finishFragment();
            }
        }
    }

    class C16752 implements OnItemClickListener {
        C16752() {
        }

        public void onItemClick(View view, int position) {
            if (position >= StickersActivity.this.stickersStartRow && position < StickersActivity.this.stickersEndRow && StickersActivity.this.getParentActivity() != null) {
                StickersActivity.this.sendReorder();
                final TL_messages_stickerSet stickerSet = (TL_messages_stickerSet) StickersQuery.getStickerSets().get(position);
                ArrayList<Document> stickers = stickerSet.documents;
                if (stickers != null && !stickers.isEmpty()) {
                    StickersAlert alert = new StickersAlert(StickersActivity.this.getParentActivity(), stickerSet);
                    alert.setButton(-2, LocaleController.getString("Close", C0553R.string.Close), (Message) null);
                    if (!stickerSet.set.official) {
                        alert.setButton(-3, LocaleController.getString("StickersRemove", C0553R.string.StickersRemove), new OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                StickersQuery.removeStickersSet(StickersActivity.this.getParentActivity(), stickerSet.set, 0);
                            }
                        });
                    }
                    StickersActivity.this.setVisibleDialog(alert);
                    alert.show();
                }
            }
        }
    }

    class C16763 implements RequestDelegate {
        C16763() {
        }

        public void run(TLObject response, TL_error error) {
        }
    }

    private class ListAdapter extends Adapter {
        private Context mContext;

        class C12251 implements View.OnClickListener {
            C12251() {
            }

            public void onClick(View v) {
                int[] options;
                CharSequence[] items;
                StickersActivity.this.sendReorder();
                final TL_messages_stickerSet stickerSet = ((StickerSetCell) v.getParent()).getStickersSet();
                Builder builder = new Builder(StickersActivity.this.getParentActivity());
                builder.setTitle(stickerSet.set.title);
                if (stickerSet.set.official) {
                    String string;
                    options = new int[]{0};
                    items = new CharSequence[1];
                    if (stickerSet.set.disabled) {
                        string = LocaleController.getString("StickersShow", C0553R.string.StickersShow);
                    } else {
                        string = LocaleController.getString("StickersHide", C0553R.string.StickersHide);
                    }
                    items[0] = string;
                } else {
                    options = new int[]{0, 1, 2, 3};
                    items = new CharSequence[4];
                    items[0] = !stickerSet.set.disabled ? LocaleController.getString("StickersHide", C0553R.string.StickersHide) : LocaleController.getString("StickersShow", C0553R.string.StickersShow);
                    items[1] = LocaleController.getString("StickersRemove", C0553R.string.StickersRemove);
                    items[2] = LocaleController.getString("StickersShare", C0553R.string.StickersShare);
                    items[3] = LocaleController.getString("StickersCopy", C0553R.string.StickersCopy);
                }
                builder.setItems(items, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ListAdapter.this.processSelectionOption(options[which], stickerSet);
                    }
                });
                StickersActivity.this.showDialog(builder.create());
            }
        }

        private class Holder extends ViewHolder {
            public Holder(View itemView) {
                super(itemView);
            }
        }

        public ListAdapter(Context context) {
            this.mContext = context;
        }

        public int getItemCount() {
            return StickersActivity.this.rowCount;
        }

        public long getItemId(int i) {
            if (i >= StickersActivity.this.stickersStartRow && i < StickersActivity.this.stickersEndRow) {
                return ((TL_messages_stickerSet) StickersQuery.getStickerSets().get(i)).set.id;
            }
            if (i == StickersActivity.this.stickersInfoRow) {
                return -2147483648L;
            }
            return (long) i;
        }

        private void processSelectionOption(int which, TL_messages_stickerSet stickerSet) {
            int i = 1;
            if (which == 0) {
                Context parentActivity = StickersActivity.this.getParentActivity();
                StickerSet stickerSet2 = stickerSet.set;
                if (stickerSet.set.disabled) {
                    i = 2;
                }
                StickersQuery.removeStickersSet(parentActivity, stickerSet2, i);
            } else if (which == 1) {
                StickersQuery.removeStickersSet(StickersActivity.this.getParentActivity(), stickerSet.set, 0);
            } else if (which == 2) {
                try {
                    Intent intent = new Intent("android.intent.action.SEND");
                    intent.setType("text/plain");
                    intent.putExtra("android.intent.extra.TEXT", String.format(Locale.US, "https://telegram.me/addstickers/%s", new Object[]{stickerSet.set.short_name}));
                    StickersActivity.this.getParentActivity().startActivityForResult(Intent.createChooser(intent, LocaleController.getString("StickersShare", C0553R.string.StickersShare)), 500);
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            } else if (which == 3) {
                try {
                    if (VERSION.SDK_INT < 11) {
                        ((ClipboardManager) ApplicationLoader.applicationContext.getSystemService("clipboard")).setText(String.format(Locale.US, "https://telegram.me/addstickers/%s", new Object[]{stickerSet.set.short_name}));
                    } else {
                        ((android.content.ClipboardManager) ApplicationLoader.applicationContext.getSystemService("clipboard")).setPrimaryClip(ClipData.newPlainText(PlusShare.KEY_CALL_TO_ACTION_LABEL, String.format(Locale.US, "https://telegram.me/addstickers/%s", new Object[]{stickerSet.set.short_name})));
                    }
                    Toast.makeText(StickersActivity.this.getParentActivity(), LocaleController.getString("LinkCopied", C0553R.string.LinkCopied), 0).show();
                } catch (Throwable e2) {
                    FileLog.m611e("tmessages", e2);
                }
            }
        }

        public void onBindViewHolder(ViewHolder holder, int position) {
            if (holder.getItemViewType() == 0) {
                ArrayList<TL_messages_stickerSet> arrayList = StickersQuery.getStickerSets();
                ((StickerSetCell) holder.itemView).setStickersSet((TL_messages_stickerSet) arrayList.get(position), position != arrayList.size() + -1);
            }
        }

        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = null;
            switch (viewType) {
                case 0:
                    view = new StickerSetCell(this.mContext);
                    view.setBackgroundColor(-1);
                    view.setBackgroundResource(C0553R.drawable.list_selector_white);
                    ((StickerSetCell) view).setOnOptionsClick(new C12251());
                    break;
                case 1:
                    view = new TextInfoPrivacyCell(this.mContext);
                    String text = LocaleController.getString("StickersInfo", C0553R.string.StickersInfo);
                    String botName = "@stickers";
                    int index = text.indexOf(botName);
                    if (index != -1) {
                        try {
                            SpannableStringBuilder stringBuilder = new SpannableStringBuilder(text);
                            stringBuilder.setSpan(new URLSpanNoUnderline("@stickers") {
                                public void onClick(View widget) {
                                    MessagesController.openByUserName("stickers", StickersActivity.this, 1);
                                }
                            }, index, botName.length() + index, 18);
                            ((TextInfoPrivacyCell) view).setText(stringBuilder);
                        } catch (Throwable e) {
                            FileLog.m611e("tmessages", e);
                            ((TextInfoPrivacyCell) view).setText(text);
                        }
                    } else {
                        ((TextInfoPrivacyCell) view).setText(text);
                    }
                    view.setBackgroundResource(C0553R.drawable.greydivider_bottom);
                    break;
            }
            return new Holder(view);
        }

        public int getItemViewType(int i) {
            if ((i < StickersActivity.this.stickersStartRow || i >= StickersActivity.this.stickersEndRow) && i == StickersActivity.this.stickersInfoRow) {
                return 1;
            }
            return 0;
        }

        public void swapElements(int fromIndex, int toIndex) {
            if (fromIndex != toIndex) {
                StickersActivity.this.needReorder = true;
            }
            ArrayList<TL_messages_stickerSet> arrayList = StickersQuery.getStickerSets();
            TL_messages_stickerSet from = (TL_messages_stickerSet) arrayList.get(fromIndex);
            arrayList.set(fromIndex, arrayList.get(toIndex));
            arrayList.set(toIndex, from);
            notifyItemMoved(fromIndex, toIndex);
        }
    }

    public class TouchHelperCallback extends Callback {
        public static final float ALPHA_FULL = 1.0f;

        public boolean isLongPressDragEnabled() {
            return true;
        }

        public int getMovementFlags(RecyclerView recyclerView, ViewHolder viewHolder) {
            if (viewHolder.getItemViewType() != 0) {
                return Callback.makeMovementFlags(0, 0);
            }
            return Callback.makeMovementFlags(3, 0);
        }

        public boolean onMove(RecyclerView recyclerView, ViewHolder source, ViewHolder target) {
            if (source.getItemViewType() != target.getItemViewType()) {
                return false;
            }
            StickersActivity.this.listAdapter.swapElements(source.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        public void onChildDraw(Canvas c, RecyclerView recyclerView, ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }

        public void onSelectedChanged(ViewHolder viewHolder, int actionState) {
            if (actionState != 0) {
                StickersActivity.this.listView.cancelClickRunnables(false);
                viewHolder.itemView.setPressed(true);
            }
            super.onSelectedChanged(viewHolder, actionState);
        }

        public void onSwiped(ViewHolder viewHolder, int direction) {
        }

        public void clearView(RecyclerView recyclerView, ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
            viewHolder.itemView.setPressed(false);
        }
    }

    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        StickersQuery.checkStickers();
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.stickersDidLoaded);
        updateRows();
        return true;
    }

    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        sendReorder();
    }

    public View createView(Context context) {
        this.actionBar.setBackButtonImage(C0553R.drawable.ic_ab_back);
        this.actionBar.setAllowOverlayTitle(true);
        this.actionBar.setTitle(LocaleController.getString("Stickers", C0553R.string.Stickers));
        this.actionBar.setActionBarMenuOnItemClick(new C16741());
        this.listAdapter = new ListAdapter(context);
        this.fragmentView = new FrameLayout(context);
        FrameLayout frameLayout = this.fragmentView;
        frameLayout.setBackgroundColor(-986896);
        this.listView = new RecyclerListView(context);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(1);
        this.listView.setLayoutManager(layoutManager);
        new ItemTouchHelper(new TouchHelperCallback()).attachToRecyclerView(this.listView);
        frameLayout.addView(this.listView, LayoutHelper.createFrame(-1, GroundOverlayOptions.NO_DIMENSION));
        this.listView.setAdapter(this.listAdapter);
        this.listView.setOnItemClickListener(new C16752());
        return this.fragmentView;
    }

    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.stickersDidLoaded) {
            updateRows();
        }
    }

    private void sendReorder() {
        if (this.needReorder) {
            StickersQuery.calcNewHash();
            this.needReorder = false;
            TL_messages_reorderStickerSets req = new TL_messages_reorderStickerSets();
            ArrayList<TL_messages_stickerSet> arrayList = StickersQuery.getStickerSets();
            for (int a = 0; a < arrayList.size(); a++) {
                req.order.add(Long.valueOf(((TL_messages_stickerSet) arrayList.get(a)).set.id));
            }
            ConnectionsManager.getInstance().sendRequest(req, new C16763());
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.stickersDidLoaded, new Object[0]);
        }
    }

    private void updateRows() {
        this.rowCount = 0;
        ArrayList<TL_messages_stickerSet> stickerSets = StickersQuery.getStickerSets();
        if (stickerSets.isEmpty()) {
            this.stickersStartRow = -1;
            this.stickersEndRow = -1;
        } else {
            this.stickersStartRow = 0;
            this.stickersEndRow = stickerSets.size();
            this.rowCount += stickerSets.size();
        }
        int i = this.rowCount;
        this.rowCount = i + 1;
        this.stickersInfoRow = i;
        if (this.listAdapter != null) {
            this.listAdapter.notifyDataSetChanged();
        }
    }

    public void onResume() {
        super.onResume();
        if (this.listAdapter != null) {
            this.listAdapter.notifyDataSetChanged();
        }
    }
}
