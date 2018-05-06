package org.telegram.ui.ActionBar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils.TruncateAt;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnApplyWindowInsetsListener;
import android.view.View.OnTouchListener;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import java.util.ArrayList;
import net.hockeyapp.android.Strings;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.AnimationCompat.AnimatorListenerAdapterProxy;
import org.telegram.messenger.AnimationCompat.AnimatorSetProxy;
import org.telegram.messenger.AnimationCompat.ObjectAnimatorProxy;
import org.telegram.messenger.AnimationCompat.ViewProxy;
import org.telegram.messenger.C0553R;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.ui.Components.LayoutHelper;

public class BottomSheet extends Dialog {
    private static int backgroundPaddingLeft;
    private static int backgroundPaddingTop;
    private static Drawable shadowDrawable;
    private AccelerateInterpolator accelerateInterpolator = new AccelerateInterpolator();
    private boolean applyTopPaddings = true;
    private ColorDrawable backgroundDrawable = new ColorDrawable(ViewCompat.MEASURED_STATE_MASK);
    private Paint ciclePaint = new Paint(1);
    private FrameLayout container = new FrameLayout(getContext()) {
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int height = MeasureSpec.getSize(heightMeasureSpec);
            setMeasuredDimension(width, height);
            boolean isPortrait = width < height;
            if (BottomSheet.this.containerView != null) {
                int left = (!BottomSheet.this.useRevealAnimation || VERSION.SDK_INT > 19) ? BottomSheet.backgroundPaddingLeft : 0;
                if (BottomSheet.this.fullWidth) {
                    BottomSheet.this.containerView.measure(MeasureSpec.makeMeasureSpec((left * 2) + width, 1073741824), MeasureSpec.makeMeasureSpec(height, Integer.MIN_VALUE));
                } else if (AndroidUtilities.isTablet()) {
                    BottomSheet.this.containerView.measure(MeasureSpec.makeMeasureSpec((left * 2) + ((int) (((float) Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y)) * 0.8f)), 1073741824), MeasureSpec.makeMeasureSpec(height, Integer.MIN_VALUE));
                } else {
                    BottomSheet.this.containerView.measure(isPortrait ? MeasureSpec.makeMeasureSpec((left * 2) + width, 1073741824) : MeasureSpec.makeMeasureSpec(((int) Math.max(((float) width) * 0.8f, (float) Math.min(AndroidUtilities.dp(480.0f), width))) + (left * 2), 1073741824), MeasureSpec.makeMeasureSpec(height, Integer.MIN_VALUE));
                }
            }
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = getChildAt(i);
                if (!(child.getVisibility() == 8 || child == BottomSheet.this.containerView)) {
                    if (BottomSheet.this.lastInsets != null && VERSION.SDK_INT >= 21) {
                        WindowInsets wi = (WindowInsets) BottomSheet.this.lastInsets;
                        child.dispatchApplyWindowInsets(wi.replaceSystemWindowInsets(wi.getSystemWindowInsetLeft(), wi.getSystemWindowInsetTop(), 0, wi.getSystemWindowInsetBottom()));
                    }
                    measureChildWithMargins(child, MeasureSpec.makeMeasureSpec(width, 1073741824), 0, MeasureSpec.makeMeasureSpec(height, 1073741824), 0);
                }
            }
        }

        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            if (BottomSheet.this.containerView != null) {
                int l = ((right - left) - BottomSheet.this.containerView.getMeasuredWidth()) / 2;
                int t = (bottom - top) - BottomSheet.this.containerView.getMeasuredHeight();
                BottomSheet.this.containerView.layout(l, t, BottomSheet.this.containerView.getMeasuredWidth() + l, getMeasuredHeight() + t);
            }
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                View child = getChildAt(i);
                if (!(child.getVisibility() == 8 || child == BottomSheet.this.containerView)) {
                    int childLeft;
                    int childTop;
                    LayoutParams lp = (LayoutParams) child.getLayoutParams();
                    int width = child.getMeasuredWidth();
                    int height = child.getMeasuredHeight();
                    int gravity = lp.gravity;
                    if (gravity == -1) {
                        gravity = 51;
                    }
                    int verticalGravity = gravity & 112;
                    switch ((gravity & 7) & 7) {
                        case 1:
                            childLeft = ((((right - left) - width) / 2) + lp.leftMargin) - lp.rightMargin;
                            break;
                        case 5:
                            childLeft = (right - width) - lp.rightMargin;
                            break;
                        default:
                            childLeft = lp.leftMargin;
                            break;
                    }
                    switch (verticalGravity) {
                        case 16:
                            childTop = ((((bottom - top) - height) / 2) + lp.topMargin) - lp.bottomMargin;
                            break;
                        case 48:
                            childTop = lp.topMargin;
                            break;
                        case 80:
                            childTop = ((bottom - top) - height) - lp.bottomMargin;
                            break;
                        default:
                            childTop = lp.topMargin;
                            break;
                    }
                    child.layout(childLeft, childTop, childLeft + width, childTop + height);
                }
            }
        }
    };
    private LinearLayout containerView;
    private View customView;
    private DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator();
    private BottomSheetDelegateInterface delegate;
    private boolean dismissed;
    private boolean focusable;
    private boolean fullWidth;
    private boolean isGrid;
    private int[] itemIcons;
    private ArrayList<BottomSheetCell> itemViews = new ArrayList();
    private CharSequence[] items;
    private Object lastInsets;
    private OnClickListener onClickListener;
    private float revealRadius;
    private int revealX;
    private int revealY;
    private int tag;
    private CharSequence title;
    private boolean useRevealAnimation;

    class C07132 implements OnTouchListener {
        C07132() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            BottomSheet.this.dismiss();
            return false;
        }
    }

    class C07143 implements OnApplyWindowInsetsListener {
        C07143() {
        }

        @SuppressLint({"NewApi"})
        public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
            BottomSheet.this.lastInsets = insets;
            BottomSheet.this.container.requestLayout();
            return insets.consumeSystemWindowInsets();
        }
    }

    class C07165 implements OnTouchListener {
        C07165() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            return true;
        }
    }

    class C07176 implements OnTouchListener {
        C07176() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            return true;
        }
    }

    class C07187 implements View.OnClickListener {
        C07187() {
        }

        public void onClick(View v) {
            BottomSheet.this.dismissWithButtonClick(((Integer) v.getTag()).intValue());
        }
    }

    class C07198 implements Runnable {
        C07198() {
        }

        public void run() {
            BottomSheet.this.startOpenAnimation();
        }
    }

    public static class BottomSheetCell extends FrameLayout {
        private ImageView imageView;
        private boolean isGrid;
        private TextView textView;

        public BottomSheetCell(Context context, int type) {
            super(context);
            this.isGrid = type == 1;
            setBackgroundResource(C0553R.drawable.list_selector);
            if (type != 1) {
                setPadding(AndroidUtilities.dp(16.0f), 0, AndroidUtilities.dp(16.0f), 0);
            }
            this.imageView = new ImageView(context);
            this.imageView.setScaleType(ScaleType.CENTER);
            if (type == 1) {
                addView(this.imageView, LayoutHelper.createFrame(48, 48.0f, 49, 0.0f, 8.0f, 0.0f, 0.0f));
            } else {
                addView(this.imageView, LayoutHelper.createFrame(24, 24, (LocaleController.isRTL ? 5 : 3) | 16));
            }
            this.textView = new TextView(context);
            this.textView.setLines(1);
            this.textView.setSingleLine(true);
            this.textView.setGravity(1);
            this.textView.setEllipsize(TruncateAt.END);
            if (type == 1) {
                this.textView.setTextColor(-9079435);
                this.textView.setTextSize(1, 12.0f);
                addView(this.textView, LayoutHelper.createFrame(-1, -2.0f, 51, 0.0f, BitmapDescriptorFactory.HUE_YELLOW, 0.0f, 0.0f));
            } else if (type == 0) {
                this.textView.setTextColor(-14606047);
                this.textView.setTextSize(1, 16.0f);
                addView(this.textView, LayoutHelper.createFrame(-2, -2, (LocaleController.isRTL ? 5 : 3) | 16));
            } else if (type == 2) {
                this.textView.setGravity(17);
                this.textView.setTextColor(-14606047);
                this.textView.setTextSize(1, 14.0f);
                this.textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
                addView(this.textView, LayoutHelper.createFrame(-1, GroundOverlayOptions.NO_DIMENSION));
            }
        }

        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            if (this.isGrid) {
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(96.0f), 1073741824);
            }
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(this.isGrid ? 80.0f : 48.0f), 1073741824));
        }

        public void setTextColor(int color) {
            this.textView.setTextColor(color);
        }

        public void setGravity(int gravity) {
            this.textView.setGravity(gravity);
        }

        public void setTextAndIcon(CharSequence text, int icon) {
            this.textView.setText(text);
            if (icon != 0) {
                this.imageView.setImageResource(icon);
                this.imageView.setVisibility(0);
                if (!this.isGrid) {
                    this.textView.setPadding(LocaleController.isRTL ? 0 : AndroidUtilities.dp(56.0f), 0, LocaleController.isRTL ? AndroidUtilities.dp(56.0f) : 0, 0);
                    return;
                }
                return;
            }
            this.imageView.setVisibility(4);
            this.textView.setPadding(0, 0, 0, 0);
        }
    }

    public interface BottomSheetDelegateInterface {
        View getRevealView();

        void onOpenAnimationEnd();

        void onOpenAnimationStart();

        void onRevealAnimationEnd(boolean z);

        void onRevealAnimationProgress(boolean z, float f, int i, int i2);

        void onRevealAnimationStart(boolean z);
    }

    public static class Builder {
        private BottomSheet bottomSheet;

        public Builder(Context context) {
            this.bottomSheet = new BottomSheet(context, false);
        }

        public Builder(Context context, boolean needFocus) {
            this.bottomSheet = new BottomSheet(context, needFocus);
        }

        public Builder setItems(CharSequence[] items, OnClickListener onClickListener) {
            this.bottomSheet.items = items;
            this.bottomSheet.onClickListener = onClickListener;
            return this;
        }

        public Builder setItems(CharSequence[] items, int[] icons, OnClickListener onClickListener) {
            this.bottomSheet.items = items;
            this.bottomSheet.itemIcons = icons;
            this.bottomSheet.onClickListener = onClickListener;
            return this;
        }

        public Builder setCustomView(View view) {
            this.bottomSheet.customView = view;
            return this;
        }

        public Builder setTitle(CharSequence title) {
            this.bottomSheet.title = title;
            return this;
        }

        public BottomSheet create() {
            return this.bottomSheet;
        }

        public BottomSheet show() {
            this.bottomSheet.show();
            return this.bottomSheet;
        }

        public Builder setTag(int tag) {
            this.bottomSheet.tag = tag;
            return this;
        }

        public Builder setUseRevealAnimation() {
            if (VERSION.SDK_INT >= 18 && !AndroidUtilities.isTablet()) {
                this.bottomSheet.useRevealAnimation = true;
            }
            return this;
        }

        public Builder setDelegate(BottomSheetDelegate delegate) {
            this.bottomSheet.setDelegate(delegate);
            return this;
        }

        public Builder setIsGrid(boolean value) {
            this.bottomSheet.isGrid = value;
            return this;
        }

        public Builder setApplyTopPaddings(boolean value) {
            this.bottomSheet.applyTopPaddings = value;
            return this;
        }

        public BottomSheet setUseFullWidth(boolean value) {
            this.bottomSheet.fullWidth = value;
            return this.bottomSheet;
        }
    }

    public static class BottomSheetDelegate implements BottomSheetDelegateInterface {
        public void onOpenAnimationStart() {
        }

        public void onOpenAnimationEnd() {
        }

        public void onRevealAnimationStart(boolean open) {
        }

        public void onRevealAnimationEnd(boolean open) {
        }

        public void onRevealAnimationProgress(boolean open, float radius, int x, int y) {
        }

        public View getRevealView() {
            return null;
        }
    }

    public BottomSheet(Context context, boolean needFocus) {
        super(context);
        this.container.setOnTouchListener(new C07132());
        this.container.setBackgroundDrawable(this.backgroundDrawable);
        this.focusable = needFocus;
        if (VERSION.SDK_INT >= 21 && !this.focusable) {
            this.container.setFitsSystemWindows(true);
            this.container.setOnApplyWindowInsetsListener(new C07143());
            this.container.setSystemUiVisibility(Strings.LOGIN_HEADLINE_TEXT_ID);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.setBackgroundDrawableResource(C0553R.drawable.transparent);
        window.requestFeature(1);
        window.setWindowAnimations(C0553R.style.DialogNoAnimation);
        if (shadowDrawable == null) {
            Rect padding = new Rect();
            shadowDrawable = getContext().getResources().getDrawable(C0553R.drawable.sheet_shadow);
            shadowDrawable.getPadding(padding);
            backgroundPaddingLeft = padding.left;
            backgroundPaddingTop = padding.top;
        }
        setContentView(this.container, new ViewGroup.LayoutParams(-1, -1));
        this.ciclePaint.setColor(-1);
        this.containerView = new LinearLayout(getContext()) {
            protected void onDraw(Canvas canvas) {
                if (BottomSheet.this.useRevealAnimation && VERSION.SDK_INT <= 19) {
                    canvas.drawCircle((float) BottomSheet.this.revealX, (float) BottomSheet.this.revealY, BottomSheet.this.revealRadius, BottomSheet.this.ciclePaint);
                }
            }

            protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
                return super.drawChild(canvas, child, drawingTime);
            }
        };
        this.containerView.setWillNotDraw(false);
        this.containerView.setOrientation(1);
        this.container.addView(this.containerView, 0, LayoutHelper.createFrame(-1, -2, 80));
        if (this.title != null) {
            View textView = new TextView(getContext());
            textView.setLines(1);
            textView.setSingleLine(true);
            textView.setText(this.title);
            textView.setTextColor(-9079435);
            textView.setTextSize(1, 16.0f);
            textView.setPadding(AndroidUtilities.dp(16.0f), 0, AndroidUtilities.dp(16.0f), AndroidUtilities.dp(8.0f));
            textView.setGravity(16);
            this.containerView.addView(textView, LayoutHelper.createLinear(-1, 48));
            textView.setOnTouchListener(new C07165());
        }
        if (this.customView != null) {
            if (this.customView.getParent() != null) {
                ((ViewGroup) this.customView.getParent()).removeView(this.customView);
            }
            this.containerView.addView(this.customView, LayoutHelper.createLinear(-1, -2));
        }
        if (this.items != null) {
            if (this.customView != null) {
                FrameLayout frameLayout = new FrameLayout(getContext());
                frameLayout.setPadding(0, AndroidUtilities.dp(8.0f), 0, 0);
                this.containerView.addView(frameLayout, LayoutHelper.createLinear(-1, 16));
                View lineView = new View(getContext());
                lineView.setBackgroundColor(-2960686);
                frameLayout.addView(lineView, new LayoutParams(-1, 1));
            }
            FrameLayout rowLayout = null;
            int lastRowLayoutNum = 0;
            int a = 0;
            while (a < this.items.length) {
                BottomSheetCell cell = new BottomSheetCell(getContext(), this.isGrid ? 1 : 0);
                cell.setTextAndIcon(this.items[a], this.itemIcons != null ? this.itemIcons[a] : 0);
                if (this.isGrid) {
                    int gravity;
                    int row = a / 3;
                    if (rowLayout == null || lastRowLayoutNum != row) {
                        FrameLayout frameLayout2 = new FrameLayout(getContext());
                        lastRowLayoutNum = row;
                        this.containerView.addView(frameLayout2, LayoutHelper.createLinear(-1, 80, 0.0f, lastRowLayoutNum != 0 ? 8.0f : 0.0f, 0.0f, 0.0f));
                        frameLayout2.setOnTouchListener(new C07176());
                    }
                    int col = a % 3;
                    if (col == 0) {
                        gravity = 51;
                    } else if (col == 1) {
                        gravity = 49;
                    } else {
                        gravity = 53;
                    }
                    rowLayout.addView(cell, LayoutHelper.createFrame(96, 80, gravity));
                } else {
                    this.containerView.addView(cell, LayoutHelper.createLinear(-1, 48));
                }
                cell.setTag(Integer.valueOf(a));
                cell.setOnClickListener(new C07187());
                this.itemViews.add(cell);
                a++;
            }
        }
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = -1;
        params.gravity = 51;
        if (this.focusable) {
            params.dimAmount = 0.2f;
        } else {
            params.flags |= 131072;
            params.dimAmount = 0.0f;
            params.flags &= -3;
        }
        if (VERSION.SDK_INT < 21) {
            params.height = -1;
        }
        getWindow().setAttributes(params);
    }

    public void show() {
        int dp;
        int i = 0;
        super.show();
        if (this.focusable) {
            getWindow().setSoftInputMode(16);
        }
        this.dismissed = false;
        if (VERSION.SDK_INT >= 21 || !this.useRevealAnimation) {
            this.containerView.setBackgroundDrawable(shadowDrawable);
        } else {
            this.containerView.setBackgroundDrawable(null);
        }
        int left = (!this.useRevealAnimation || VERSION.SDK_INT > 19) ? backgroundPaddingLeft : 0;
        int top = (!this.useRevealAnimation || VERSION.SDK_INT > 19) ? backgroundPaddingTop : 0;
        LinearLayout linearLayout = this.containerView;
        if (this.applyTopPaddings) {
            dp = AndroidUtilities.dp(8.0f);
        } else {
            dp = 0;
        }
        dp += top;
        if (this.applyTopPaddings) {
            i = AndroidUtilities.dp(this.isGrid ? 16.0f : 8.0f);
        }
        linearLayout.setPadding(left, dp, left, i);
        if (VERSION.SDK_INT >= 21) {
            AndroidUtilities.runOnUIThread(new C07198());
        } else {
            startOpenAnimation();
        }
    }

    protected void setRevealRadius(float radius) {
        this.revealRadius = radius;
        this.delegate.onRevealAnimationProgress(!this.dismissed, radius, this.revealX, this.revealY);
        if (VERSION.SDK_INT <= 19) {
            this.containerView.invalidate();
        }
    }

    protected float getRevealRadius() {
        return this.revealRadius;
    }

    @SuppressLint({"NewApi"})
    private void startRevealAnimation(final boolean open) {
        if (open) {
            this.backgroundDrawable.setAlpha(0);
            this.containerView.setVisibility(0);
        } else {
            this.backgroundDrawable.setAlpha(51);
        }
        ViewProxy.setTranslationY(this.containerView, 0.0f);
        AnimatorSet animatorSet = new AnimatorSet();
        View view = this.delegate.getRevealView();
        if (view.getVisibility() == 0 && ((ViewGroup) view.getParent()).getVisibility() == 0) {
            float top;
            int[] coords = new int[2];
            view.getLocationInWindow(coords);
            if (VERSION.SDK_INT <= 19) {
                top = (float) ((AndroidUtilities.displaySize.y - this.containerView.getMeasuredHeight()) - AndroidUtilities.statusBarHeight);
            } else {
                top = this.containerView.getY();
            }
            this.revealX = coords[0] + (view.getMeasuredWidth() / 2);
            this.revealY = (int) (((float) (coords[1] + (view.getMeasuredHeight() / 2))) - top);
            if (VERSION.SDK_INT <= 19) {
                this.revealY -= AndroidUtilities.statusBarHeight;
            }
        } else {
            this.revealX = (AndroidUtilities.displaySize.x / 2) + backgroundPaddingLeft;
            this.revealY = (int) (((float) AndroidUtilities.displaySize.y) - this.containerView.getY());
        }
        corners = new int[4][];
        corners[1] = new int[]{0, this.containerView.getMeasuredHeight()};
        corners[2] = new int[]{this.containerView.getMeasuredWidth(), 0};
        corners[3] = new int[]{this.containerView.getMeasuredWidth(), this.containerView.getMeasuredHeight()};
        int finalRevealRadius = 0;
        for (int a = 0; a < 4; a++) {
            finalRevealRadius = Math.max(finalRevealRadius, (int) Math.ceil(Math.sqrt((double) (((this.revealX - corners[a][0]) * (this.revealX - corners[a][0])) + ((this.revealY - corners[a][1]) * (this.revealY - corners[a][1]))))));
        }
        ArrayList<Animator> animators = new ArrayList(3);
        String str = "revealRadius";
        float[] fArr = new float[2];
        fArr[0] = open ? 0.0f : (float) finalRevealRadius;
        fArr[1] = open ? (float) finalRevealRadius : 0.0f;
        animators.add(ObjectAnimator.ofFloat(this, str, fArr));
        ColorDrawable colorDrawable = this.backgroundDrawable;
        String str2 = "alpha";
        int[] iArr = new int[1];
        iArr[0] = open ? 51 : 0;
        animators.add(ObjectAnimator.ofInt(colorDrawable, str2, iArr));
        if (VERSION.SDK_INT >= 21) {
            this.containerView.setElevation((float) AndroidUtilities.dp(10.0f));
            try {
                animators.add(ViewAnimationUtils.createCircularReveal(this.containerView, this.revealX <= this.containerView.getMeasuredWidth() ? this.revealX : this.containerView.getMeasuredWidth(), this.revealY, open ? 0.0f : (float) finalRevealRadius, open ? (float) finalRevealRadius : 0.0f));
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
            animatorSet.setDuration(300);
        } else if (open) {
            animatorSet.setDuration(250);
            this.containerView.setScaleX(1.0f);
            this.containerView.setScaleY(1.0f);
            this.containerView.setAlpha(1.0f);
            if (VERSION.SDK_INT <= 19) {
                animatorSet.setStartDelay(20);
            }
        } else {
            animatorSet.setDuration(200);
            this.containerView.setPivotX(this.revealX <= this.containerView.getMeasuredWidth() ? (float) this.revealX : (float) this.containerView.getMeasuredWidth());
            this.containerView.setPivotY((float) this.revealY);
            animators.add(ObjectAnimator.ofFloat(this.containerView, "scaleX", new float[]{0.0f}));
            animators.add(ObjectAnimator.ofFloat(this.containerView, "scaleY", new float[]{0.0f}));
            animators.add(ObjectAnimator.ofFloat(this.containerView, "alpha", new float[]{0.0f}));
        }
        animatorSet.playTogether(animators);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animation) {
                if (BottomSheet.this.delegate != null) {
                    BottomSheet.this.delegate.onRevealAnimationStart(open);
                }
            }

            public void onAnimationEnd(Animator animation) {
                if (BottomSheet.this.delegate != null) {
                    BottomSheet.this.delegate.onRevealAnimationEnd(open);
                }
                BottomSheet.this.containerView.invalidate();
                if (VERSION.SDK_INT >= 11) {
                    BottomSheet.this.container.setLayerType(0, null);
                }
                if (!open) {
                    BottomSheet.this.containerView.setVisibility(4);
                    try {
                        super.dismiss();
                    } catch (Throwable e) {
                        FileLog.m611e("tmessages", e);
                    }
                }
            }

            public void onAnimationCancel(Animator animation) {
                onAnimationEnd(animation);
            }
        });
        animatorSet.start();
    }

    private void startOpenAnimation() {
        if (VERSION.SDK_INT >= 20) {
            this.container.setLayerType(2, null);
        }
        if (this.containerView.getMeasuredHeight() == 0) {
            this.containerView.measure(MeasureSpec.makeMeasureSpec(AndroidUtilities.displaySize.x, Integer.MIN_VALUE), MeasureSpec.makeMeasureSpec(AndroidUtilities.displaySize.y, Integer.MIN_VALUE));
        }
        if (this.useRevealAnimation) {
            startRevealAnimation(true);
            return;
        }
        ViewProxy.setTranslationY(this.containerView, (float) this.containerView.getMeasuredHeight());
        this.backgroundDrawable.setAlpha(0);
        AnimatorSetProxy animatorSetProxy = new AnimatorSetProxy();
        r3 = new Object[2];
        r3[0] = ObjectAnimatorProxy.ofFloat(this.containerView, "translationY", 0.0f);
        ColorDrawable colorDrawable = this.backgroundDrawable;
        String str = "alpha";
        int[] iArr = new int[1];
        iArr[0] = this.focusable ? 0 : 51;
        r3[1] = ObjectAnimatorProxy.ofInt(colorDrawable, str, iArr);
        animatorSetProxy.playTogether(r3);
        animatorSetProxy.setDuration(200);
        animatorSetProxy.setStartDelay(20);
        animatorSetProxy.setInterpolator(new DecelerateInterpolator());
        animatorSetProxy.addListener(new AnimatorListenerAdapterProxy() {
            public void onAnimationEnd(Object animation) {
                if (BottomSheet.this.delegate != null) {
                    BottomSheet.this.delegate.onOpenAnimationEnd();
                }
                if (VERSION.SDK_INT >= 11) {
                    BottomSheet.this.container.setLayerType(0, null);
                }
            }
        });
        animatorSetProxy.start();
    }

    public void setDelegate(BottomSheetDelegate delegate) {
        this.delegate = delegate;
    }

    public FrameLayout getContainer() {
        return this.container;
    }

    public LinearLayout getSheetContainer() {
        return this.containerView;
    }

    public int getTag() {
        return this.tag;
    }

    public void setItemText(int item, CharSequence text) {
        if (item >= 0 && item < this.itemViews.size()) {
            ((BottomSheetCell) this.itemViews.get(item)).textView.setText(text);
        }
    }

    public void dismissWithButtonClick(final int item) {
        if (!this.dismissed) {
            this.dismissed = true;
            AnimatorSetProxy animatorSetProxy = new AnimatorSetProxy();
            r1 = new Object[2];
            r1[0] = ObjectAnimatorProxy.ofFloat(this.containerView, "translationY", (float) (this.containerView.getMeasuredHeight() + AndroidUtilities.dp(10.0f)));
            r1[1] = ObjectAnimatorProxy.ofInt(this.backgroundDrawable, "alpha", 0);
            animatorSetProxy.playTogether(r1);
            animatorSetProxy.setDuration(180);
            animatorSetProxy.setInterpolator(new AccelerateInterpolator());
            animatorSetProxy.addListener(new AnimatorListenerAdapterProxy() {

                class C07101 implements Runnable {
                    C07101() {
                    }

                    public void run() {
                        try {
                            super.dismiss();
                        } catch (Throwable e) {
                            FileLog.m611e("tmessages", e);
                        }
                    }
                }

                public void onAnimationEnd(Object animation) {
                    if (BottomSheet.this.onClickListener != null) {
                        BottomSheet.this.onClickListener.onClick(BottomSheet.this, item);
                    }
                    AndroidUtilities.runOnUIThread(new C07101());
                }

                public void onAnimationCancel(Object animation) {
                    onAnimationEnd(animation);
                }
            });
            animatorSetProxy.start();
        }
    }

    public void dismiss() {
        if (!this.dismissed) {
            this.dismissed = true;
            if (this.useRevealAnimation) {
                startRevealAnimation(false);
                return;
            }
            AnimatorSetProxy animatorSetProxy = new AnimatorSetProxy();
            r1 = new Object[2];
            r1[0] = ObjectAnimatorProxy.ofFloat(this.containerView, "translationY", (float) (this.containerView.getMeasuredHeight() + AndroidUtilities.dp(10.0f)));
            r1[1] = ObjectAnimatorProxy.ofInt(this.backgroundDrawable, "alpha", 0);
            animatorSetProxy.playTogether(r1);
            animatorSetProxy.setDuration(180);
            animatorSetProxy.setInterpolator(new AccelerateInterpolator());
            animatorSetProxy.addListener(new AnimatorListenerAdapterProxy() {

                class C07111 implements Runnable {
                    C07111() {
                    }

                    public void run() {
                        try {
                            super.dismiss();
                        } catch (Throwable e) {
                            FileLog.m611e("tmessages", e);
                        }
                    }
                }

                public void onAnimationEnd(Object animation) {
                    AndroidUtilities.runOnUIThread(new C07111());
                }

                public void onAnimationCancel(Object animation) {
                    onAnimationEnd(animation);
                }
            });
            animatorSetProxy.start();
        }
    }
}
