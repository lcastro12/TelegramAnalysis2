package org.telegram.ui.Cells;

import android.content.Context;
import android.os.Build.VERSION;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.AnimationCompat.AnimatorListenerAdapterProxy;
import org.telegram.messenger.AnimationCompat.AnimatorSetProxy;
import org.telegram.messenger.AnimationCompat.ObjectAnimatorProxy;
import org.telegram.messenger.AnimationCompat.ViewProxy;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.C0553R;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.MessageObject;
import org.telegram.tgnet.TLRPC.TL_messageMediaPhoto;
import org.telegram.tgnet.TLRPC.TL_messageMediaVideo;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.CheckBox;
import org.telegram.ui.Components.FrameLayoutFixed;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.PhotoViewer;

public class SharedPhotoVideoCell extends FrameLayoutFixed {
    private SharedPhotoVideoCellDelegate delegate;
    private int[] indeces = new int[6];
    private boolean isFirst;
    private int itemsCount;
    private MessageObject[] messageObjects = new MessageObject[6];
    private PhotoVideoView[] photoVideoViews = new PhotoVideoView[6];

    class C07881 implements OnClickListener {
        C07881() {
        }

        public void onClick(View v) {
            if (SharedPhotoVideoCell.this.delegate != null) {
                int a = ((Integer) v.getTag()).intValue();
                SharedPhotoVideoCell.this.delegate.didClickItem(SharedPhotoVideoCell.this, SharedPhotoVideoCell.this.indeces[a], SharedPhotoVideoCell.this.messageObjects[a], a);
            }
        }
    }

    class C07892 implements OnLongClickListener {
        C07892() {
        }

        public boolean onLongClick(View v) {
            if (SharedPhotoVideoCell.this.delegate == null) {
                return false;
            }
            int a = ((Integer) v.getTag()).intValue();
            return SharedPhotoVideoCell.this.delegate.didLongClickItem(SharedPhotoVideoCell.this, SharedPhotoVideoCell.this.indeces[a], SharedPhotoVideoCell.this.messageObjects[a], a);
        }
    }

    public interface SharedPhotoVideoCellDelegate {
        void didClickItem(SharedPhotoVideoCell sharedPhotoVideoCell, int i, MessageObject messageObject, int i2);

        boolean didLongClickItem(SharedPhotoVideoCell sharedPhotoVideoCell, int i, MessageObject messageObject, int i2);
    }

    private class PhotoVideoView extends FrameLayoutFixed {
        private AnimatorSetProxy animator;
        private CheckBox checkBox;
        private FrameLayoutFixed container;
        private BackupImageView imageView;
        private View selector;
        private LinearLayout videoInfoContainer;
        private TextView videoTextView;

        public PhotoVideoView(Context context) {
            super(context);
            this.container = new FrameLayoutFixed(context);
            addView(this.container, LayoutHelper.createFrame(-1, GroundOverlayOptions.NO_DIMENSION));
            this.imageView = new BackupImageView(context);
            this.imageView.getImageReceiver().setNeedsQualityThumb(true);
            this.imageView.getImageReceiver().setShouldGenerateQualityThumb(true);
            this.container.addView(this.imageView, LayoutHelper.createFrame(-1, GroundOverlayOptions.NO_DIMENSION));
            this.videoInfoContainer = new LinearLayout(context);
            this.videoInfoContainer.setOrientation(0);
            this.videoInfoContainer.setBackgroundResource(C0553R.drawable.phototime);
            this.videoInfoContainer.setPadding(AndroidUtilities.dp(3.0f), 0, AndroidUtilities.dp(3.0f), 0);
            this.videoInfoContainer.setGravity(16);
            this.container.addView(this.videoInfoContainer, LayoutHelper.createFrame(-1, 16, 83));
            ImageView imageView1 = new ImageView(context);
            imageView1.setImageResource(C0553R.drawable.ic_video);
            this.videoInfoContainer.addView(imageView1, LayoutHelper.createLinear(-2, -2));
            this.videoTextView = new TextView(context);
            this.videoTextView.setTextColor(-1);
            this.videoTextView.setTextSize(1, 12.0f);
            this.videoTextView.setGravity(16);
            this.videoInfoContainer.addView(this.videoTextView, LayoutHelper.createLinear(-2, -2, 16, 4, 0, 0, 1));
            this.selector = new View(context);
            this.selector.setBackgroundResource(C0553R.drawable.list_selector);
            addView(this.selector, LayoutHelper.createFrame(-1, GroundOverlayOptions.NO_DIMENSION));
            this.checkBox = new CheckBox(context, C0553R.drawable.round_check2);
            this.checkBox.setVisibility(4);
            addView(this.checkBox, LayoutHelper.createFrame(22, 22.0f, 53, 0.0f, 2.0f, 2.0f, 0.0f));
        }

        public boolean onTouchEvent(MotionEvent event) {
            if (VERSION.SDK_INT >= 21) {
                this.selector.drawableHotspotChanged(event.getX(), event.getY());
            }
            return super.onTouchEvent(event);
        }

        public void setChecked(final boolean checked, boolean animated) {
            int i = -657931;
            float f = 0.85f;
            if (this.checkBox.getVisibility() != 0) {
                this.checkBox.setVisibility(0);
            }
            this.checkBox.setChecked(checked, animated);
            if (this.animator != null) {
                this.animator.cancel();
                this.animator = null;
            }
            if (animated) {
                if (checked) {
                    setBackgroundColor(-657931);
                }
                this.animator = new AnimatorSetProxy();
                AnimatorSetProxy animatorSetProxy = this.animator;
                Object[] objArr = new Object[2];
                FrameLayoutFixed frameLayoutFixed = this.container;
                String str = "scaleX";
                float[] fArr = new float[1];
                fArr[0] = checked ? 0.85f : 1.0f;
                objArr[0] = ObjectAnimatorProxy.ofFloat(frameLayoutFixed, str, fArr);
                FrameLayoutFixed frameLayoutFixed2 = this.container;
                String str2 = "scaleY";
                float[] fArr2 = new float[1];
                if (!checked) {
                    f = 1.0f;
                }
                fArr2[0] = f;
                objArr[1] = ObjectAnimatorProxy.ofFloat(frameLayoutFixed2, str2, fArr2);
                animatorSetProxy.playTogether(objArr);
                this.animator.setDuration(200);
                this.animator.addListener(new AnimatorListenerAdapterProxy() {
                    public void onAnimationEnd(Object animation) {
                        if (PhotoVideoView.this.animator.equals(animation)) {
                            PhotoVideoView.this.animator = null;
                            if (!checked) {
                                PhotoVideoView.this.setBackgroundColor(0);
                            }
                        }
                    }
                });
                this.animator.start();
                return;
            }
            float f2;
            if (!checked) {
                i = 0;
            }
            setBackgroundColor(i);
            View view = this.container;
            if (checked) {
                f2 = 0.85f;
            } else {
                f2 = 1.0f;
            }
            ViewProxy.setScaleX(view, f2);
            View view2 = this.container;
            if (!checked) {
                f = 1.0f;
            }
            ViewProxy.setScaleY(view2, f);
        }

        public void clearAnimation() {
            super.clearAnimation();
            if (this.animator != null) {
                this.animator.cancel();
                this.animator = null;
            }
        }
    }

    public SharedPhotoVideoCell(Context context) {
        super(context);
        for (int a = 0; a < 6; a++) {
            this.photoVideoViews[a] = new PhotoVideoView(context);
            addView(this.photoVideoViews[a]);
            this.photoVideoViews[a].setVisibility(4);
            this.photoVideoViews[a].setTag(Integer.valueOf(a));
            this.photoVideoViews[a].setOnClickListener(new C07881());
            this.photoVideoViews[a].setOnLongClickListener(new C07892());
        }
    }

    public void setDelegate(SharedPhotoVideoCellDelegate delegate) {
        this.delegate = delegate;
    }

    public void setItemsCount(int count) {
        int a = 0;
        while (a < this.photoVideoViews.length) {
            this.photoVideoViews[a].clearAnimation();
            this.photoVideoViews[a].setVisibility(a < count ? 0 : 4);
            a++;
        }
        this.itemsCount = count;
    }

    public BackupImageView getImageView(int a) {
        if (a >= this.itemsCount) {
            return null;
        }
        return this.photoVideoViews[a].imageView;
    }

    public MessageObject getMessageObject(int a) {
        if (a >= this.itemsCount) {
            return null;
        }
        return this.messageObjects[a];
    }

    public void setIsFirst(boolean first) {
        this.isFirst = first;
    }

    public void setChecked(int a, boolean checked, boolean animated) {
        this.photoVideoViews[a].setChecked(checked, animated);
    }

    public void setItem(int a, int index, MessageObject messageObject) {
        this.messageObjects[a] = messageObject;
        this.indeces[a] = index;
        if (messageObject != null) {
            this.photoVideoViews[a].setVisibility(0);
            PhotoVideoView photoVideoView = this.photoVideoViews[a];
            photoVideoView.imageView.getImageReceiver().setParentMessageObject(messageObject);
            photoVideoView.imageView.getImageReceiver().setVisible(!PhotoViewer.getInstance().isShowingImage(messageObject), false);
            if ((messageObject.messageOwner.media instanceof TL_messageMediaVideo) && messageObject.messageOwner.media.video != null) {
                photoVideoView.videoInfoContainer.setVisibility(0);
                int duration = messageObject.messageOwner.media.video.duration;
                int seconds = duration - ((duration / 60) * 60);
                photoVideoView.videoTextView.setText(String.format("%d:%02d", new Object[]{Integer.valueOf(minutes), Integer.valueOf(seconds)}));
                if (messageObject.messageOwner.media.video.thumb != null) {
                    photoVideoView.imageView.setImage(null, null, null, ApplicationLoader.applicationContext.getResources().getDrawable(C0553R.drawable.photo_placeholder_in), null, messageObject.messageOwner.media.video.thumb.location, "b", null, 0);
                    return;
                } else {
                    photoVideoView.imageView.setImageResource(C0553R.drawable.photo_placeholder_in);
                    return;
                }
            } else if (!(messageObject.messageOwner.media instanceof TL_messageMediaPhoto) || messageObject.messageOwner.media.photo == null || messageObject.photoThumbs.isEmpty()) {
                photoVideoView.videoInfoContainer.setVisibility(4);
                photoVideoView.imageView.setImageResource(C0553R.drawable.photo_placeholder_in);
                return;
            } else {
                photoVideoView.videoInfoContainer.setVisibility(4);
                photoVideoView.imageView.setImage(null, null, null, ApplicationLoader.applicationContext.getResources().getDrawable(C0553R.drawable.photo_placeholder_in), null, FileLoader.getClosestPhotoSizeWithSize(messageObject.photoThumbs, 80).location, "b", null, 0);
                return;
            }
        }
        this.photoVideoViews[a].clearAnimation();
        this.photoVideoViews[a].setVisibility(4);
        this.messageObjects[a] = null;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int itemWidth;
        int i = 0;
        if (AndroidUtilities.isTablet()) {
            itemWidth = (AndroidUtilities.dp(490.0f) - ((this.itemsCount + 1) * AndroidUtilities.dp(4.0f))) / this.itemsCount;
        } else {
            itemWidth = (AndroidUtilities.displaySize.x - ((this.itemsCount + 1) * AndroidUtilities.dp(4.0f))) / this.itemsCount;
        }
        for (int a = 0; a < this.itemsCount; a++) {
            LayoutParams layoutParams = (LayoutParams) this.photoVideoViews[a].getLayoutParams();
            layoutParams.topMargin = this.isFirst ? 0 : AndroidUtilities.dp(4.0f);
            layoutParams.leftMargin = ((AndroidUtilities.dp(4.0f) + itemWidth) * a) + AndroidUtilities.dp(4.0f);
            layoutParams.width = itemWidth;
            layoutParams.height = itemWidth;
            layoutParams.gravity = 51;
            this.photoVideoViews[a].setLayoutParams(layoutParams);
        }
        if (!this.isFirst) {
            i = AndroidUtilities.dp(4.0f);
        }
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(i + itemWidth, 1073741824));
    }
}
