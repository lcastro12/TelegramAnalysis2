package org.telegram.ui.Components;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.database.DataSetObserver;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import java.util.ArrayList;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.tgnet.TLRPC.Document;
import org.telegram.tgnet.TLRPC.TL_messages_stickerSet;
import org.telegram.ui.Cells.StickerEmojiCell;

public class StickersAlert extends AlertDialog implements NotificationCenterDelegate {
    private GridView gridView;
    private ArrayList<Document> stickers;

    class C09732 implements OnShowListener {
        C09732() {
        }

        public void onShow(DialogInterface arg0) {
            if (StickersAlert.this.getButton(-3) != null) {
                StickersAlert.this.getButton(-3).setTextColor(-3319206);
            }
            if (StickersAlert.this.getButton(-1) != null) {
                StickersAlert.this.getButton(-1).setTextColor(-13129447);
            }
        }
    }

    private class GridAdapter extends BaseAdapter {
        Context context;

        public GridAdapter(Context context) {
            this.context = context;
        }

        public int getCount() {
            return StickersAlert.this.stickers.size();
        }

        public Object getItem(int i) {
            return StickersAlert.this.stickers.get(i);
        }

        public long getItemId(int i) {
            return ((Document) StickersAlert.this.stickers.get(i)).id;
        }

        public boolean areAllItemsEnabled() {
            return false;
        }

        public boolean isEnabled(int position) {
            return false;
        }

        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = new StickerEmojiCell(this.context) {
                    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(82.0f), 1073741824));
                    }
                };
            }
            ((StickerEmojiCell) view).setSticker((Document) StickersAlert.this.stickers.get(i), true);
            return view;
        }

        public void unregisterDataSetObserver(DataSetObserver observer) {
            if (observer != null) {
                super.unregisterDataSetObserver(observer);
            }
        }
    }

    public StickersAlert(Context context, TL_messages_stickerSet set) {
        super(context);
        this.stickers = set.documents;
        FrameLayout container = new FrameLayout(context) {
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec((int) Math.min(Math.ceil((double) (((float) StickersAlert.this.stickers.size()) / 4.0f)) * ((double) AndroidUtilities.dp(82.0f)), (double) ((AndroidUtilities.displaySize.y / 5) * 3)), 1073741824));
            }
        };
        setView(container, AndroidUtilities.dp(16.0f), 0, AndroidUtilities.dp(16.0f), 0);
        this.gridView = new GridView(context);
        this.gridView.setNumColumns(4);
        this.gridView.setAdapter(new GridAdapter(context));
        this.gridView.setVerticalScrollBarEnabled(false);
        container.addView(this.gridView, LayoutHelper.createFrame(-1, GroundOverlayOptions.NO_DIMENSION));
        setTitle(set.set.title);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.emojiDidLoaded);
        setOnShowListener(new C09732());
    }

    public void dismiss() {
        super.dismiss();
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.emojiDidLoaded);
    }

    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.emojiDidLoaded && this.gridView != null) {
            this.gridView.invalidateViews();
        }
    }
}
