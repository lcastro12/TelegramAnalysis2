package org.telegram.ui.Adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.HashMap;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.query.StickersQuery;
import org.telegram.messenger.support.widget.RecyclerView.Adapter;
import org.telegram.messenger.support.widget.RecyclerView.ViewHolder;
import org.telegram.tgnet.TLRPC.Document;
import org.telegram.ui.Cells.StickerCell;

public class StickersAdapter extends Adapter implements NotificationCenterDelegate {
    private StickersAdapterDelegate delegate;
    private String lastSticker;
    private Context mContext;
    private ArrayList<Document> stickers;
    private ArrayList<String> stickersToLoad = new ArrayList();
    private boolean visible;

    public interface StickersAdapterDelegate {
        void needChangePanelVisibility(boolean z);
    }

    private class Holder extends ViewHolder {
        public Holder(View itemView) {
            super(itemView);
        }
    }

    public StickersAdapter(Context context, StickersAdapterDelegate delegate) {
        this.mContext = context;
        this.delegate = delegate;
        StickersQuery.checkStickers();
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.FileDidLoaded);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.FileDidFailedLoad);
    }

    public void onDestroy() {
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.FileDidLoaded);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.FileDidFailedLoad);
    }

    public void didReceivedNotification(int id, Object... args) {
        boolean z = false;
        if ((id == NotificationCenter.FileDidLoaded || id == NotificationCenter.FileDidFailedLoad) && this.stickers != null && !this.stickers.isEmpty() && !this.stickersToLoad.isEmpty() && this.visible) {
            this.stickersToLoad.remove(args[0]);
            if (this.stickersToLoad.isEmpty()) {
                StickersAdapterDelegate stickersAdapterDelegate = this.delegate;
                if (!(this.stickers == null || this.stickers.isEmpty() || !this.stickersToLoad.isEmpty())) {
                    z = true;
                }
                stickersAdapterDelegate.needChangePanelVisibility(z);
            }
        }
    }

    private boolean checkStickerFilesExistAndDownload() {
        if (this.stickers == null) {
            return false;
        }
        this.stickersToLoad.clear();
        int size = Math.min(10, this.stickers.size());
        for (int a = 0; a < size; a++) {
            Document document = (Document) this.stickers.get(a);
            if (!FileLoader.getPathToAttach(document.thumb, "webp", true).exists()) {
                this.stickersToLoad.add(FileLoader.getAttachFileName(document.thumb, "webp"));
                FileLoader.getInstance().loadFile(document.thumb.location, "webp", 0, true);
            }
        }
        return this.stickersToLoad.isEmpty();
    }

    public void loadStikersForEmoji(CharSequence emoji) {
        boolean search;
        if (emoji == null || emoji.length() <= 0 || emoji.length() > 14) {
            search = false;
        } else {
            search = true;
        }
        if (search) {
            int length = emoji.length();
            int a = 0;
            while (a < length) {
                if (a < length - 1 && emoji.charAt(a) == '?' && emoji.charAt(a + 1) >= '?' && emoji.charAt(a + 1) <= '?') {
                    emoji = TextUtils.concat(new CharSequence[]{emoji.subSequence(0, a), emoji.subSequence(a + 2, emoji.length())});
                    break;
                }
                if (emoji.charAt(a) == '️') {
                    emoji = TextUtils.concat(new CharSequence[]{emoji.subSequence(0, a), emoji.subSequence(a + 1, emoji.length())});
                    length--;
                }
                a++;
            }
            this.lastSticker = emoji.toString();
            HashMap<String, ArrayList<Document>> allStickers = StickersQuery.getAllStickers();
            if (allStickers != null) {
                ArrayList<Document> newStickers = (ArrayList) allStickers.get(this.lastSticker);
                if (this.stickers == null || newStickers != null) {
                    boolean z;
                    this.stickers = newStickers;
                    checkStickerFilesExistAndDownload();
                    StickersAdapterDelegate stickersAdapterDelegate = this.delegate;
                    if (this.stickers == null || this.stickers.isEmpty() || !this.stickersToLoad.isEmpty()) {
                        z = false;
                    } else {
                        z = true;
                    }
                    stickersAdapterDelegate.needChangePanelVisibility(z);
                    notifyDataSetChanged();
                    this.visible = true;
                } else if (this.visible) {
                    this.delegate.needChangePanelVisibility(false);
                    this.visible = false;
                }
            }
        }
        if (!search && this.visible && this.stickers != null) {
            this.visible = false;
            this.delegate.needChangePanelVisibility(false);
        }
    }

    public void clearStickers() {
        this.lastSticker = null;
        this.stickers = null;
        this.stickersToLoad.clear();
        notifyDataSetChanged();
    }

    public int getItemCount() {
        return this.stickers != null ? this.stickers.size() : 0;
    }

    public Document getItem(int i) {
        return (this.stickers == null || i < 0 || i >= this.stickers.size()) ? null : (Document) this.stickers.get(i);
    }

    public long getItemId(int i) {
        return (long) i;
    }

    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new Holder(new StickerCell(this.mContext));
    }

    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        int side = 0;
        if (i == 0) {
            if (this.stickers.size() == 1) {
                side = 2;
            } else {
                side = -1;
            }
        } else if (i == this.stickers.size() - 1) {
            side = 1;
        }
        ((StickerCell) viewHolder.itemView).setSticker((Document) this.stickers.get(i), side);
    }
}
