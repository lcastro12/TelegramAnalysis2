package org.telegram.ui.Adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import java.util.HashMap;
import java.util.Map.Entry;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MediaController.PhotoEntry;
import org.telegram.messenger.support.widget.RecyclerView.Adapter;
import org.telegram.messenger.support.widget.RecyclerView.ViewHolder;
import org.telegram.ui.Cells.PhotoAttachPhotoCell;
import org.telegram.ui.Cells.PhotoAttachPhotoCell.PhotoAttachPhotoCellDelegate;

public class PhotoAttachAdapter extends Adapter {
    private PhotoAttachAdapterDelegate delegate;
    private Context mContext;
    private HashMap<Integer, PhotoEntry> selectedPhotos = new HashMap();

    public interface PhotoAttachAdapterDelegate {
        void selectedPhotosChanged();
    }

    class C15051 implements PhotoAttachPhotoCellDelegate {
        C15051() {
        }

        public void onCheckClick(PhotoAttachPhotoCell v) {
            boolean z = true;
            PhotoEntry photoEntry = v.getPhotoEntry();
            if (PhotoAttachAdapter.this.selectedPhotos.containsKey(Integer.valueOf(photoEntry.imageId))) {
                PhotoAttachAdapter.this.selectedPhotos.remove(Integer.valueOf(photoEntry.imageId));
                v.setChecked(false, true);
                photoEntry.imagePath = null;
                photoEntry.thumbPath = null;
                if (v.getTag() != Integer.valueOf(MediaController.allPhotosAlbumEntry.photos.size() - 1)) {
                    z = false;
                }
                v.setPhotoEntry(photoEntry, z);
            } else {
                PhotoAttachAdapter.this.selectedPhotos.put(Integer.valueOf(photoEntry.imageId), photoEntry);
                v.setChecked(true, true);
            }
            PhotoAttachAdapter.this.delegate.selectedPhotosChanged();
        }
    }

    private class Holder extends ViewHolder {
        public Holder(View itemView) {
            super(itemView);
        }
    }

    public PhotoAttachAdapter(Context context) {
        this.mContext = context;
    }

    public void clearSelectedPhotos() {
        if (!this.selectedPhotos.isEmpty()) {
            for (Entry<Integer, PhotoEntry> entry : this.selectedPhotos.entrySet()) {
                PhotoEntry photoEntry = (PhotoEntry) entry.getValue();
                photoEntry.imagePath = null;
                photoEntry.thumbPath = null;
                photoEntry.caption = null;
            }
            this.selectedPhotos.clear();
            this.delegate.selectedPhotosChanged();
            notifyDataSetChanged();
        }
    }

    public HashMap<Integer, PhotoEntry> getSelectedPhotos() {
        return this.selectedPhotos;
    }

    public void setDelegate(PhotoAttachAdapterDelegate photoAttachAdapterDelegate) {
        this.delegate = photoAttachAdapterDelegate;
    }

    public void onBindViewHolder(ViewHolder holder, int position) {
        boolean z;
        PhotoAttachPhotoCell cell = holder.itemView;
        PhotoEntry photoEntry = (PhotoEntry) MediaController.allPhotosAlbumEntry.photos.get(position);
        if (position == MediaController.allPhotosAlbumEntry.photos.size() - 1) {
            z = true;
        } else {
            z = false;
        }
        cell.setPhotoEntry(photoEntry, z);
        cell.setChecked(this.selectedPhotos.containsKey(Integer.valueOf(photoEntry.imageId)), false);
        cell.getImageView().setTag(Integer.valueOf(position));
        cell.setTag(Integer.valueOf(position));
    }

    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        PhotoAttachPhotoCell cell = new PhotoAttachPhotoCell(this.mContext);
        cell.setDelegate(new C15051());
        return new Holder(cell);
    }

    public int getItemCount() {
        return MediaController.allPhotosAlbumEntry != null ? MediaController.allPhotosAlbumEntry.photos.size() : 0;
    }

    public int getItemViewType(int position) {
        return 0;
    }
}
