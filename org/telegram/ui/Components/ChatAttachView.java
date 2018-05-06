package org.telegram.ui.Components;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build.VERSION;
import android.text.TextUtils.TruncateAt;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import com.google.android.gms.location.LocationRequest;
import java.util.ArrayList;
import java.util.HashMap;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.AnimationCompat.ViewProxy;
import org.telegram.messenger.C0553R;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MediaController.PhotoEntry;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView.Adapter;
import org.telegram.tgnet.TLRPC.FileLocation;
import org.telegram.ui.Adapters.PhotoAttachAdapter;
import org.telegram.ui.Adapters.PhotoAttachAdapter.PhotoAttachAdapterDelegate;
import org.telegram.ui.Cells.PhotoAttachPhotoCell;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.RecyclerListView.OnItemClickListener;
import org.telegram.ui.PhotoViewer;
import org.telegram.ui.PhotoViewer.PhotoViewerProvider;
import org.telegram.ui.PhotoViewer.PlaceProviderObject;

public class ChatAttachView extends FrameLayout implements NotificationCenterDelegate, PhotoViewerProvider {
    private LinearLayoutManager attachPhotoLayoutManager;
    private RecyclerListView attachPhotoRecyclerView;
    private ChatActivity baseFragment;
    private DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator();
    private ChatAttachViewDelegate delegate;
    private float[] distCache = new float[20];
    private View lineView;
    private boolean loading;
    private PhotoAttachAdapter photoAttachAdapter;
    private EmptyTextProgressView progressView;
    private AttachButton sendPhotosButton;
    private View[] views = new View[20];

    class C09034 implements OnClickListener {
        C09034() {
        }

        public void onClick(View v) {
            if (ChatAttachView.this.delegate != null) {
                ChatAttachView.this.delegate.didPressedButton(((Integer) v.getTag()).intValue());
            }
        }
    }

    class C09045 implements OnTouchListener {
        C09045() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            return true;
        }
    }

    private static class AttachButton extends FrameLayout {
        private ImageView imageView;
        private TextView textView;

        public AttachButton(Context context) {
            super(context);
            this.imageView = new ImageView(context);
            this.imageView.setScaleType(ScaleType.CENTER);
            addView(this.imageView, LayoutHelper.createFrame(64, 64, 49));
            this.textView = new TextView(context);
            this.textView.setLines(1);
            this.textView.setSingleLine(true);
            this.textView.setGravity(1);
            this.textView.setEllipsize(TruncateAt.END);
            this.textView.setTextColor(-9079435);
            this.textView.setTextSize(1, 12.0f);
            addView(this.textView, LayoutHelper.createFrame(-1, -2.0f, 51, 0.0f, 64.0f, 0.0f, 0.0f));
        }

        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(85.0f), 1073741824), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(90.0f), 1073741824));
        }

        public void setTextAndIcon(CharSequence text, int icon) {
            this.textView.setText(text);
            this.imageView.setBackgroundResource(icon);
        }
    }

    public interface ChatAttachViewDelegate {
        void didPressedButton(int i);
    }

    class C15552 implements PhotoAttachAdapterDelegate {
        C15552() {
        }

        public void selectedPhotosChanged() {
            ChatAttachView.this.updatePhotosButton();
        }
    }

    class C15563 implements OnItemClickListener {
        C15563() {
        }

        public void onItemClick(View view, int position) {
            if (ChatAttachView.this.baseFragment != null && ChatAttachView.this.baseFragment.getParentActivity() != null) {
                ArrayList<Object> arrayList = MediaController.allPhotosAlbumEntry.photos;
                if (position >= 0 && position < arrayList.size()) {
                    PhotoViewer.getInstance().setParentActivity(ChatAttachView.this.baseFragment.getParentActivity());
                    PhotoViewer.getInstance().openPhotoForSelect(arrayList, position, 0, ChatAttachView.this, ChatAttachView.this.baseFragment);
                    AndroidUtilities.hideKeyboard(ChatAttachView.this.baseFragment.getFragmentView().findFocus());
                }
            }
        }
    }

    public ChatAttachView(Context context) {
        super(context);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.albumsDidLoaded);
        if (MediaController.allPhotosAlbumEntry == null) {
            if (VERSION.SDK_INT >= 21) {
                MediaController.loadGalleryPhotosAlbums(0);
            }
            this.loading = true;
        }
        View[] viewArr = this.views;
        RecyclerListView recyclerListView = new RecyclerListView(context);
        this.attachPhotoRecyclerView = recyclerListView;
        viewArr[8] = recyclerListView;
        this.attachPhotoRecyclerView.setVerticalScrollBarEnabled(true);
        RecyclerListView recyclerListView2 = this.attachPhotoRecyclerView;
        Adapter photoAttachAdapter = new PhotoAttachAdapter(context);
        this.photoAttachAdapter = photoAttachAdapter;
        recyclerListView2.setAdapter(photoAttachAdapter);
        this.attachPhotoRecyclerView.setClipToPadding(false);
        this.attachPhotoRecyclerView.setPadding(AndroidUtilities.dp(8.0f), 0, AndroidUtilities.dp(8.0f), 0);
        this.attachPhotoRecyclerView.setItemAnimator(null);
        this.attachPhotoRecyclerView.setLayoutAnimation(null);
        if (VERSION.SDK_INT >= 9) {
            this.attachPhotoRecyclerView.setOverScrollMode(2);
        }
        addView(this.attachPhotoRecyclerView, LayoutHelper.createFrame(-1, 80.0f));
        this.attachPhotoLayoutManager = new LinearLayoutManager(context) {
            public boolean supportsPredictiveItemAnimations() {
                return false;
            }
        };
        this.attachPhotoLayoutManager.setOrientation(0);
        this.attachPhotoRecyclerView.setLayoutManager(this.attachPhotoLayoutManager);
        this.photoAttachAdapter.setDelegate(new C15552());
        this.attachPhotoRecyclerView.setOnItemClickListener(new C15563());
        viewArr = this.views;
        EmptyTextProgressView emptyTextProgressView = new EmptyTextProgressView(context);
        this.progressView = emptyTextProgressView;
        viewArr[9] = emptyTextProgressView;
        if (VERSION.SDK_INT < 23 || getContext().checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE") == 0) {
            this.progressView.setText(LocaleController.getString("NoPhotos", C0553R.string.NoPhotos));
            this.progressView.setTextSize(20);
        } else {
            this.progressView.setText(LocaleController.getString("PermissionStorage", C0553R.string.PermissionStorage));
            this.progressView.setTextSize(16);
        }
        addView(this.progressView, LayoutHelper.createFrame(-1, 80.0f));
        this.attachPhotoRecyclerView.setEmptyView(this.progressView);
        viewArr = this.views;
        View view = new View(getContext());
        this.lineView = view;
        viewArr[10] = view;
        this.lineView.setBackgroundColor(-2960686);
        addView(this.lineView, new LayoutParams(-1, 1, 51));
        CharSequence[] items = new CharSequence[]{LocaleController.getString("ChatCamera", C0553R.string.ChatCamera), LocaleController.getString("ChatGallery", C0553R.string.ChatGallery), LocaleController.getString("ChatVideo", C0553R.string.ChatVideo), LocaleController.getString("AttachAudio", C0553R.string.AttachAudio), LocaleController.getString("ChatDocument", C0553R.string.ChatDocument), LocaleController.getString("AttachContact", C0553R.string.AttachContact), LocaleController.getString("ChatLocation", C0553R.string.ChatLocation), ""};
        int[] itemIcons = new int[]{C0553R.drawable.attach_camera_states, C0553R.drawable.attach_gallery_states, C0553R.drawable.attach_video_states, C0553R.drawable.attach_audio_states, C0553R.drawable.attach_file_states, C0553R.drawable.attach_contact_states, C0553R.drawable.attach_location_states, C0553R.drawable.attach_hide_states};
        for (int a = 0; a < 8; a++) {
            AttachButton attachButton = new AttachButton(context);
            attachButton.setTextAndIcon(items[a], itemIcons[a]);
            addView(attachButton, LayoutHelper.createFrame(85, 90, 51));
            attachButton.setTag(Integer.valueOf(a));
            this.views[a] = attachButton;
            if (a == 7) {
                this.sendPhotosButton = attachButton;
                this.sendPhotosButton.imageView.setPadding(0, AndroidUtilities.dp(4.0f), 0, 0);
            }
            attachButton.setOnClickListener(new C09034());
        }
        setOnTouchListener(new C09045());
        if (this.loading) {
            this.progressView.showProgress();
        } else {
            this.progressView.showTextView();
        }
    }

    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.albumsDidLoaded && this.photoAttachAdapter != null) {
            this.loading = false;
            this.progressView.showTextView();
            this.photoAttachAdapter.notifyDataSetChanged();
        }
    }

    protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(294.0f), 1073741824));
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int width = right - left;
        int t = AndroidUtilities.dp(8.0f);
        this.attachPhotoRecyclerView.layout(0, t, width, this.attachPhotoRecyclerView.getMeasuredHeight() + t);
        this.progressView.layout(0, t, width, this.progressView.getMeasuredHeight() + t);
        this.lineView.layout(0, AndroidUtilities.dp(96.0f), width, AndroidUtilities.dp(96.0f) + this.lineView.getMeasuredHeight());
        int diff = (width - AndroidUtilities.dp(360.0f)) / 3;
        for (int a = 0; a < 8; a++) {
            int y = AndroidUtilities.dp((float) (((a / 4) * 95) + LocationRequest.PRIORITY_NO_POWER));
            int x = AndroidUtilities.dp(10.0f) + ((a % 4) * (AndroidUtilities.dp(85.0f) + diff));
            this.views[a].layout(x, y, this.views[a].getMeasuredWidth() + x, this.views[a].getMeasuredHeight() + y);
        }
    }

    public void updatePhotosButton() {
        if (this.photoAttachAdapter.getSelectedPhotos().size() == 0) {
            this.sendPhotosButton.imageView.setPadding(0, AndroidUtilities.dp(4.0f), 0, 0);
            this.sendPhotosButton.imageView.setBackgroundResource(C0553R.drawable.attach_hide_states);
            this.sendPhotosButton.imageView.setImageResource(C0553R.drawable.attach_hide2);
            this.sendPhotosButton.textView.setText("");
        } else {
            this.sendPhotosButton.imageView.setPadding(AndroidUtilities.dp(2.0f), 0, 0, 0);
            this.sendPhotosButton.imageView.setBackgroundResource(C0553R.drawable.attach_send_states);
            this.sendPhotosButton.imageView.setImageResource(C0553R.drawable.attach_send2);
            TextView access$300 = this.sendPhotosButton.textView;
            Object[] objArr = new Object[1];
            objArr[0] = String.format("(%d)", new Object[]{Integer.valueOf(count)});
            access$300.setText(LocaleController.formatString("SendItems", C0553R.string.SendItems, objArr));
        }
        if (VERSION.SDK_INT < 23 || getContext().checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE") == 0) {
            this.progressView.setText(LocaleController.getString("NoPhotos", C0553R.string.NoPhotos));
            this.progressView.setTextSize(20);
            return;
        }
        this.progressView.setText(LocaleController.getString("PermissionStorage", C0553R.string.PermissionStorage));
        this.progressView.setTextSize(16);
    }

    public void setDelegate(ChatAttachViewDelegate chatAttachViewDelegate) {
        this.delegate = chatAttachViewDelegate;
    }

    public void onRevealAnimationEnd(boolean open) {
        if (open && VERSION.SDK_INT <= 19 && MediaController.allPhotosAlbumEntry == null) {
            MediaController.loadGalleryPhotosAlbums(0);
        }
    }

    @SuppressLint({"NewApi"})
    public void onRevealAnimationStart(boolean open) {
        if (open) {
            int count;
            if (VERSION.SDK_INT <= 19) {
                count = 11;
            } else {
                count = 8;
            }
            for (int a = 0; a < count; a++) {
                if (VERSION.SDK_INT <= 19) {
                    if (a < 8) {
                        this.views[a].setScaleX(0.1f);
                        this.views[a].setScaleY(0.1f);
                    }
                    this.views[a].setAlpha(0.0f);
                } else {
                    this.views[a].setScaleX(0.7f);
                    this.views[a].setScaleY(0.7f);
                }
                this.views[a].setTag(C0553R.string.AppName, null);
                this.distCache[a] = 0.0f;
            }
        }
    }

    @SuppressLint({"NewApi"})
    public void onRevealAnimationProgress(boolean open, float radius, int x, int y) {
        if (open) {
            int count = VERSION.SDK_INT <= 19 ? 11 : 8;
            for (int a = 0; a < count; a++) {
                if (this.views[a].getTag(C0553R.string.AppName) == null) {
                    if (this.distCache[a] == 0.0f) {
                        int buttonX = this.views[a].getLeft() + (this.views[a].getMeasuredWidth() / 2);
                        int buttonY = this.views[a].getTop() + (this.views[a].getMeasuredHeight() / 2);
                        this.distCache[a] = (float) Math.sqrt((double) (((x - buttonX) * (x - buttonX)) + ((y - buttonY) * (y - buttonY))));
                        float vecY = ((float) (y - buttonY)) / this.distCache[a];
                        this.views[a].setPivotX(((float) (this.views[a].getMeasuredWidth() / 2)) + (((float) AndroidUtilities.dp(20.0f)) * (((float) (x - buttonX)) / this.distCache[a])));
                        this.views[a].setPivotY(((float) (this.views[a].getMeasuredHeight() / 2)) + (((float) AndroidUtilities.dp(20.0f)) * vecY));
                    }
                    if (this.distCache[a] <= ((float) AndroidUtilities.dp(27.0f)) + radius) {
                        this.views[a].setTag(C0553R.string.AppName, Integer.valueOf(1));
                        ArrayList<Animator> animators = new ArrayList();
                        final ArrayList<Animator> animators2 = new ArrayList();
                        if (a < 8) {
                            animators.add(ObjectAnimator.ofFloat(this.views[a], "scaleX", new float[]{0.7f, 1.05f}));
                            animators.add(ObjectAnimator.ofFloat(this.views[a], "scaleY", new float[]{0.7f, 1.05f}));
                            animators2.add(ObjectAnimator.ofFloat(this.views[a], "scaleX", new float[]{1.0f}));
                            animators2.add(ObjectAnimator.ofFloat(this.views[a], "scaleY", new float[]{1.0f}));
                        }
                        if (VERSION.SDK_INT <= 19) {
                            animators.add(ObjectAnimator.ofFloat(this.views[a], "alpha", new float[]{1.0f}));
                        }
                        AnimatorSet animatorSet = new AnimatorSet();
                        animatorSet.playTogether(animators);
                        animatorSet.setDuration(150);
                        animatorSet.setInterpolator(this.decelerateInterpolator);
                        animatorSet.addListener(new AnimatorListenerAdapter() {
                            public void onAnimationEnd(Animator animation) {
                                AnimatorSet animatorSet = new AnimatorSet();
                                animatorSet.playTogether(animators2);
                                animatorSet.setDuration(100);
                                animatorSet.setInterpolator(ChatAttachView.this.decelerateInterpolator);
                                animatorSet.start();
                            }
                        });
                        animatorSet.start();
                    }
                }
            }
        }
    }

    public void init(ChatActivity parentFragment) {
        if (MediaController.allPhotosAlbumEntry != null) {
            for (int a = 0; a < Math.min(100, MediaController.allPhotosAlbumEntry.photos.size()); a++) {
                PhotoEntry photoEntry = (PhotoEntry) MediaController.allPhotosAlbumEntry.photos.get(a);
                photoEntry.caption = null;
                photoEntry.imagePath = null;
                photoEntry.thumbPath = null;
            }
        }
        this.attachPhotoLayoutManager.scrollToPositionWithOffset(0, 1000000);
        this.photoAttachAdapter.clearSelectedPhotos();
        this.baseFragment = parentFragment;
        updatePhotosButton();
    }

    public HashMap<Integer, PhotoEntry> getSelectedPhotos() {
        return this.photoAttachAdapter.getSelectedPhotos();
    }

    public void onDestroy() {
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.albumsDidLoaded);
        this.baseFragment = null;
    }

    private PhotoAttachPhotoCell getCellForIndex(int index) {
        int count = this.attachPhotoRecyclerView.getChildCount();
        for (int a = 0; a < count; a++) {
            View view = this.attachPhotoRecyclerView.getChildAt(a);
            if (view instanceof PhotoAttachPhotoCell) {
                PhotoAttachPhotoCell photoAttachPhotoCell = (PhotoAttachPhotoCell) view;
                int num = ((Integer) photoAttachPhotoCell.getImageView().getTag()).intValue();
                if (num >= 0 && num < MediaController.allPhotosAlbumEntry.photos.size() && num == index) {
                    return photoAttachPhotoCell;
                }
            }
        }
        return null;
    }

    public PlaceProviderObject getPlaceForPhoto(MessageObject messageObject, FileLocation fileLocation, int index) {
        int i = 0;
        PhotoAttachPhotoCell cell = getCellForIndex(index);
        if (cell == null) {
            return null;
        }
        int i2;
        int[] coords = new int[2];
        cell.getImageView().getLocationInWindow(coords);
        PlaceProviderObject object = new PlaceProviderObject();
        object.viewX = coords[0];
        int i3 = coords[1];
        if (VERSION.SDK_INT >= 21) {
            i2 = AndroidUtilities.statusBarHeight;
        } else {
            i2 = 0;
        }
        object.viewY = i3 - i2;
        object.parentView = this.attachPhotoRecyclerView;
        object.imageReceiver = cell.getImageView().getImageReceiver();
        object.thumb = object.imageReceiver.getBitmap();
        object.scale = ViewProxy.getScaleX(cell.getImageView());
        if (VERSION.SDK_INT < 21) {
            i = -AndroidUtilities.statusBarHeight;
        }
        object.clipBottomAddition = i;
        cell.getCheckBox().setVisibility(8);
        return object;
    }

    public void updatePhotoAtIndex(int index) {
        PhotoAttachPhotoCell cell = getCellForIndex(index);
        if (cell != null) {
            cell.getImageView().setOrientation(0, true);
            PhotoEntry photoEntry = (PhotoEntry) MediaController.allPhotosAlbumEntry.photos.get(index);
            if (photoEntry.thumbPath != null) {
                cell.getImageView().setImage(photoEntry.thumbPath, null, cell.getContext().getResources().getDrawable(C0553R.drawable.nophotos));
            } else if (photoEntry.path != null) {
                cell.getImageView().setOrientation(photoEntry.orientation, true);
                cell.getImageView().setImage("thumb://" + photoEntry.imageId + ":" + photoEntry.path, null, cell.getContext().getResources().getDrawable(C0553R.drawable.nophotos));
            } else {
                cell.getImageView().setImageResource(C0553R.drawable.nophotos);
            }
        }
    }

    public Bitmap getThumbForPhoto(MessageObject messageObject, FileLocation fileLocation, int index) {
        PhotoAttachPhotoCell cell = getCellForIndex(index);
        if (cell != null) {
            return cell.getImageView().getImageReceiver().getBitmap();
        }
        return null;
    }

    public void willSwitchFromPhoto(MessageObject messageObject, FileLocation fileLocation, int index) {
        PhotoAttachPhotoCell cell = getCellForIndex(index);
        if (cell != null) {
            cell.getCheckBox().setVisibility(0);
        }
    }

    public void willHidePhotoViewer() {
        int count = this.attachPhotoRecyclerView.getChildCount();
        for (int a = 0; a < count; a++) {
            View view = this.attachPhotoRecyclerView.getChildAt(a);
            if (view instanceof PhotoAttachPhotoCell) {
                PhotoAttachPhotoCell cell = (PhotoAttachPhotoCell) view;
                if (cell.getCheckBox().getVisibility() != 0) {
                    cell.getCheckBox().setVisibility(0);
                }
            }
        }
    }

    public boolean isPhotoChecked(int index) {
        return index >= 0 && index < MediaController.allPhotosAlbumEntry.photos.size() && this.photoAttachAdapter.getSelectedPhotos().containsKey(Integer.valueOf(((PhotoEntry) MediaController.allPhotosAlbumEntry.photos.get(index)).imageId));
    }

    public void setPhotoChecked(int index) {
        boolean add = true;
        if (index >= 0 && index < MediaController.allPhotosAlbumEntry.photos.size()) {
            PhotoEntry photoEntry = (PhotoEntry) MediaController.allPhotosAlbumEntry.photos.get(index);
            if (this.photoAttachAdapter.getSelectedPhotos().containsKey(Integer.valueOf(photoEntry.imageId))) {
                this.photoAttachAdapter.getSelectedPhotos().remove(Integer.valueOf(photoEntry.imageId));
                add = false;
            } else {
                this.photoAttachAdapter.getSelectedPhotos().put(Integer.valueOf(photoEntry.imageId), photoEntry);
            }
            int count = this.attachPhotoRecyclerView.getChildCount();
            for (int a = 0; a < count; a++) {
                View view = this.attachPhotoRecyclerView.getChildAt(a);
                if (((Integer) view.getTag()).intValue() == index) {
                    ((PhotoAttachPhotoCell) view).setChecked(add, false);
                    break;
                }
            }
            updatePhotosButton();
        }
    }

    public boolean cancelButtonPressed() {
        return false;
    }

    public void sendButtonPressed(int index) {
        if (this.photoAttachAdapter.getSelectedPhotos().isEmpty()) {
            if (index >= 0 && index < MediaController.allPhotosAlbumEntry.photos.size()) {
                PhotoEntry photoEntry = (PhotoEntry) MediaController.allPhotosAlbumEntry.photos.get(index);
                this.photoAttachAdapter.getSelectedPhotos().put(Integer.valueOf(photoEntry.imageId), photoEntry);
            } else {
                return;
            }
        }
        this.delegate.didPressedButton(7);
    }

    public int getSelectedCount() {
        return this.photoAttachAdapter.getSelectedPhotos().size();
    }
}
