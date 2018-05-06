package org.telegram.ui.Cells;

import android.content.Context;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.AnimationCompat.AnimatorListenerAdapterProxy;
import org.telegram.messenger.AnimationCompat.AnimatorSetProxy;
import org.telegram.messenger.AnimationCompat.ObjectAnimatorProxy;
import org.telegram.messenger.AnimationCompat.ViewProxy;
import org.telegram.messenger.C0553R;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.CheckBox;
import org.telegram.ui.Components.LayoutHelper;

public class PhotoPickerPhotoCell extends FrameLayout {
    private AnimatorSetProxy animator;
    public CheckBox checkBox;
    public FrameLayout checkFrame;
    public int itemWidth;
    public BackupImageView photoImage;

    public PhotoPickerPhotoCell(Context context) {
        super(context);
        this.photoImage = new BackupImageView(context);
        addView(this.photoImage, LayoutHelper.createFrame(-1, GroundOverlayOptions.NO_DIMENSION));
        this.checkFrame = new FrameLayout(context);
        addView(this.checkFrame, LayoutHelper.createFrame(42, 42, 53));
        this.checkBox = new CheckBox(context, C0553R.drawable.checkbig);
        this.checkBox.setSize(30);
        this.checkBox.setCheckOffset(AndroidUtilities.dp(1.0f));
        this.checkBox.setDrawBackground(true);
        this.checkBox.setColor(-12793105);
        addView(this.checkBox, LayoutHelper.createFrame(30, BitmapDescriptorFactory.HUE_ORANGE, 53, 0.0f, 4.0f, 4.0f, 0.0f));
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(this.itemWidth, 1073741824), MeasureSpec.makeMeasureSpec(this.itemWidth, 1073741824));
    }

    public void setChecked(final boolean checked, boolean animated) {
        int i = -16119286;
        float f = 0.85f;
        this.checkBox.setChecked(checked, animated);
        if (this.animator != null) {
            this.animator.cancel();
            this.animator = null;
        }
        if (animated) {
            if (checked) {
                setBackgroundColor(-16119286);
            }
            this.animator = new AnimatorSetProxy();
            AnimatorSetProxy animatorSetProxy = this.animator;
            Object[] objArr = new Object[2];
            BackupImageView backupImageView = this.photoImage;
            String str = "scaleX";
            float[] fArr = new float[1];
            fArr[0] = checked ? 0.85f : 1.0f;
            objArr[0] = ObjectAnimatorProxy.ofFloat(backupImageView, str, fArr);
            BackupImageView backupImageView2 = this.photoImage;
            String str2 = "scaleY";
            float[] fArr2 = new float[1];
            if (!checked) {
                f = 1.0f;
            }
            fArr2[0] = f;
            objArr[1] = ObjectAnimatorProxy.ofFloat(backupImageView2, str2, fArr2);
            animatorSetProxy.playTogether(objArr);
            this.animator.setDuration(200);
            this.animator.addListener(new AnimatorListenerAdapterProxy() {
                public void onAnimationEnd(Object animation) {
                    if (PhotoPickerPhotoCell.this.animator.equals(animation)) {
                        PhotoPickerPhotoCell.this.animator = null;
                        if (!checked) {
                            PhotoPickerPhotoCell.this.setBackgroundColor(0);
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
        View view = this.photoImage;
        if (checked) {
            f2 = 0.85f;
        } else {
            f2 = 1.0f;
        }
        ViewProxy.setScaleX(view, f2);
        View view2 = this.photoImage;
        if (!checked) {
            f = 1.0f;
        }
        ViewProxy.setScaleY(view2, f);
    }
}
