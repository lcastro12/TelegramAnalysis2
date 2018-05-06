package org.telegram.ui.ActionBar;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.text.TextUtils.TruncateAt;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import java.util.ArrayList;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.AnimationCompat.AnimatorListenerAdapterProxy;
import org.telegram.messenger.AnimationCompat.AnimatorSetProxy;
import org.telegram.messenger.AnimationCompat.ObjectAnimatorProxy;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.C0553R;
import org.telegram.ui.Components.LayoutHelper;

public class ActionBar extends FrameLayout {
    public ActionBarMenuOnItemClick actionBarMenuOnItemClick;
    private ActionBarMenu actionMode;
    private View actionModeTop;
    private boolean actionModeVisible;
    private boolean addToContainer;
    private boolean allowOverlayTitle;
    private ImageView backButtonImageView;
    private boolean castShadows;
    private boolean isBackOverlayVisible;
    protected boolean isSearchFieldVisible;
    protected int itemsBackgroundResourceId;
    private CharSequence lastTitle;
    private ActionBarMenu menu;
    private boolean occupyStatusBar;
    protected BaseFragment parentFragment;
    private TextView subTitleTextView;
    private TextView titleTextView;

    class C06881 implements OnClickListener {
        C06881() {
        }

        public void onClick(View v) {
            if (ActionBar.this.isSearchFieldVisible) {
                ActionBar.this.closeSearchField();
            } else if (ActionBar.this.actionBarMenuOnItemClick != null) {
                ActionBar.this.actionBarMenuOnItemClick.onItemClick(-1);
            }
        }
    }

    public static class ActionBarMenuOnItemClick {
        public void onItemClick(int id) {
        }

        public boolean canOpenMenu() {
            return true;
        }
    }

    class C14932 extends AnimatorListenerAdapterProxy {
        C14932() {
        }

        public void onAnimationStart(Object animation) {
            ActionBar.this.actionMode.setVisibility(0);
            if (ActionBar.this.occupyStatusBar && ActionBar.this.actionModeTop != null) {
                ActionBar.this.actionModeTop.setVisibility(0);
            }
        }

        public void onAnimationEnd(Object animation) {
            if (ActionBar.this.titleTextView != null) {
                ActionBar.this.titleTextView.setVisibility(4);
            }
            if (ActionBar.this.subTitleTextView != null) {
                ActionBar.this.subTitleTextView.setVisibility(4);
            }
            if (ActionBar.this.menu != null) {
                ActionBar.this.menu.setVisibility(4);
            }
        }
    }

    class C14943 extends AnimatorListenerAdapterProxy {
        C14943() {
        }

        public void onAnimationEnd(Object animation) {
            ActionBar.this.actionMode.setVisibility(4);
            if (ActionBar.this.occupyStatusBar && ActionBar.this.actionModeTop != null) {
                ActionBar.this.actionModeTop.setVisibility(4);
            }
        }
    }

    public ActionBar(Context context) {
        super(context);
        this.occupyStatusBar = VERSION.SDK_INT >= 21;
        this.addToContainer = true;
        this.castShadows = true;
    }

    private void createBackButtonImage() {
        if (this.backButtonImageView == null) {
            this.backButtonImageView = new ImageView(getContext());
            this.backButtonImageView.setScaleType(ScaleType.CENTER);
            this.backButtonImageView.setBackgroundResource(this.itemsBackgroundResourceId);
            this.backButtonImageView.setPadding(AndroidUtilities.dp(1.0f), 0, 0, 0);
            addView(this.backButtonImageView, LayoutHelper.createFrame(54, 54, 51));
            this.backButtonImageView.setOnClickListener(new C06881());
        }
    }

    public void setBackButtonDrawable(Drawable drawable) {
        int i;
        if (this.backButtonImageView == null) {
            createBackButtonImage();
        }
        ImageView imageView = this.backButtonImageView;
        if (drawable == null) {
            i = 8;
        } else {
            i = 0;
        }
        imageView.setVisibility(i);
        this.backButtonImageView.setImageDrawable(drawable);
        if (drawable instanceof BackDrawable) {
            ((BackDrawable) drawable).setRotation(isActionModeShowed() ? 1.0f : 0.0f, false);
        }
    }

    public void setBackButtonImage(int resource) {
        if (this.backButtonImageView == null) {
            createBackButtonImage();
        }
        this.backButtonImageView.setVisibility(resource == 0 ? 8 : 0);
        this.backButtonImageView.setImageResource(resource);
    }

    private void createSubtitleTextView() {
        if (this.subTitleTextView == null) {
            this.subTitleTextView = new TextView(getContext());
            this.subTitleTextView.setGravity(3);
            this.subTitleTextView.setTextColor(-2627337);
            this.subTitleTextView.setSingleLine(true);
            this.subTitleTextView.setLines(1);
            this.subTitleTextView.setMaxLines(1);
            this.subTitleTextView.setEllipsize(TruncateAt.END);
            addView(this.subTitleTextView, 0, LayoutHelper.createFrame(-2, -2, 51));
        }
    }

    public void setAddToContainer(boolean value) {
        this.addToContainer = value;
    }

    public boolean getAddToContainer() {
        return this.addToContainer;
    }

    public void setSubtitle(CharSequence value) {
        if (value != null && this.subTitleTextView == null) {
            createSubtitleTextView();
        }
        if (this.subTitleTextView != null) {
            TextView textView = this.subTitleTextView;
            int i = (value == null || this.isSearchFieldVisible) ? 4 : 0;
            textView.setVisibility(i);
            this.subTitleTextView.setText(value);
        }
    }

    private void createTitleTextView() {
        if (this.titleTextView == null) {
            this.titleTextView = new TextView(getContext());
            this.titleTextView.setGravity(3);
            this.titleTextView.setLines(1);
            this.titleTextView.setMaxLines(1);
            this.titleTextView.setSingleLine(true);
            this.titleTextView.setEllipsize(TruncateAt.END);
            this.titleTextView.setTextColor(-1);
            this.titleTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            addView(this.titleTextView, 0, LayoutHelper.createFrame(-2, -2, 51));
        }
    }

    public void setTitle(CharSequence value) {
        if (value != null && this.titleTextView == null) {
            createTitleTextView();
        }
        if (this.titleTextView != null) {
            this.lastTitle = value;
            TextView textView = this.titleTextView;
            int i = (value == null || this.isSearchFieldVisible) ? 4 : 0;
            textView.setVisibility(i);
            this.titleTextView.setText(value);
        }
    }

    public TextView getSubTitleTextView() {
        return this.subTitleTextView;
    }

    public TextView getTitleTextView() {
        return this.titleTextView;
    }

    public Drawable getSubTitleIcon() {
        return this.subTitleTextView.getCompoundDrawables()[0];
    }

    public String getTitle() {
        if (this.titleTextView == null) {
            return null;
        }
        return this.titleTextView.getText().toString();
    }

    public ActionBarMenu createMenu() {
        if (this.menu != null) {
            return this.menu;
        }
        this.menu = new ActionBarMenu(getContext(), this);
        addView(this.menu, 0, LayoutHelper.createFrame(-2, -1, 5));
        return this.menu;
    }

    public void setActionBarMenuOnItemClick(ActionBarMenuOnItemClick listener) {
        this.actionBarMenuOnItemClick = listener;
    }

    public ActionBarMenu createActionMode() {
        if (this.actionMode != null) {
            return this.actionMode;
        }
        int i;
        this.actionMode = new ActionBarMenu(getContext(), this);
        this.actionMode.setBackgroundColor(-1);
        addView(this.actionMode, indexOfChild(this.backButtonImageView));
        ActionBarMenu actionBarMenu = this.actionMode;
        if (this.occupyStatusBar) {
            i = AndroidUtilities.statusBarHeight;
        } else {
            i = 0;
        }
        actionBarMenu.setPadding(0, i, 0, 0);
        LayoutParams layoutParams = (LayoutParams) this.actionMode.getLayoutParams();
        layoutParams.height = -1;
        layoutParams.width = -1;
        layoutParams.gravity = 5;
        this.actionMode.setLayoutParams(layoutParams);
        this.actionMode.setVisibility(4);
        if (this.occupyStatusBar && this.actionModeTop == null) {
            this.actionModeTop = new View(getContext());
            this.actionModeTop.setBackgroundColor(-1728053248);
            addView(this.actionModeTop);
            layoutParams = (LayoutParams) this.actionModeTop.getLayoutParams();
            layoutParams.height = AndroidUtilities.statusBarHeight;
            layoutParams.width = -1;
            layoutParams.gravity = 51;
            this.actionModeTop.setLayoutParams(layoutParams);
            this.actionModeTop.setVisibility(4);
        }
        return this.actionMode;
    }

    public void showActionMode() {
        if (this.actionMode != null && !this.actionModeVisible) {
            this.actionModeVisible = true;
            if (VERSION.SDK_INT >= 14) {
                ArrayList animators = new ArrayList();
                animators.add(ObjectAnimatorProxy.ofFloat(this.actionMode, "alpha", 0.0f, 1.0f));
                if (this.occupyStatusBar && this.actionModeTop != null) {
                    animators.add(ObjectAnimatorProxy.ofFloat(this.actionModeTop, "alpha", 0.0f, 1.0f));
                }
                AnimatorSetProxy animatorSetProxy = new AnimatorSetProxy();
                animatorSetProxy.playTogether(animators);
                animatorSetProxy.setDuration(200);
                animatorSetProxy.addListener(new C14932());
                animatorSetProxy.start();
            } else {
                this.actionMode.setVisibility(0);
                if (this.occupyStatusBar && this.actionModeTop != null) {
                    this.actionModeTop.setVisibility(0);
                }
                if (this.titleTextView != null) {
                    this.titleTextView.setVisibility(4);
                }
                if (this.subTitleTextView != null) {
                    this.subTitleTextView.setVisibility(4);
                }
                if (this.menu != null) {
                    this.menu.setVisibility(4);
                }
            }
            if (this.backButtonImageView != null) {
                Drawable drawable = this.backButtonImageView.getDrawable();
                if (drawable instanceof BackDrawable) {
                    ((BackDrawable) drawable).setRotation(1.0f, true);
                }
                this.backButtonImageView.setBackgroundResource(C0553R.drawable.bar_selector_mode);
            }
        }
    }

    public void hideActionMode() {
        if (this.actionMode != null && this.actionModeVisible) {
            this.actionModeVisible = false;
            if (VERSION.SDK_INT >= 14) {
                ArrayList animators = new ArrayList();
                animators.add(ObjectAnimatorProxy.ofFloat(this.actionMode, "alpha", 0.0f));
                if (this.occupyStatusBar && this.actionModeTop != null) {
                    animators.add(ObjectAnimatorProxy.ofFloat(this.actionModeTop, "alpha", 0.0f));
                }
                AnimatorSetProxy animatorSetProxy = new AnimatorSetProxy();
                animatorSetProxy.playTogether(animators);
                animatorSetProxy.setDuration(200);
                animatorSetProxy.addListener(new C14943());
                animatorSetProxy.start();
            } else {
                this.actionMode.setVisibility(4);
                if (this.occupyStatusBar && this.actionModeTop != null) {
                    this.actionModeTop.setVisibility(4);
                }
            }
            if (this.titleTextView != null) {
                this.titleTextView.setVisibility(0);
            }
            if (this.subTitleTextView != null) {
                this.subTitleTextView.setVisibility(0);
            }
            if (this.menu != null) {
                this.menu.setVisibility(0);
            }
            if (this.backButtonImageView != null) {
                Drawable drawable = this.backButtonImageView.getDrawable();
                if (drawable instanceof BackDrawable) {
                    ((BackDrawable) drawable).setRotation(0.0f, true);
                }
                this.backButtonImageView.setBackgroundResource(this.itemsBackgroundResourceId);
            }
        }
    }

    public void showActionModeTop() {
        if (this.occupyStatusBar && this.actionModeTop == null) {
            this.actionModeTop = new View(getContext());
            this.actionModeTop.setBackgroundColor(-1728053248);
            addView(this.actionModeTop);
            LayoutParams layoutParams = (LayoutParams) this.actionModeTop.getLayoutParams();
            layoutParams.height = AndroidUtilities.statusBarHeight;
            layoutParams.width = -1;
            layoutParams.gravity = 51;
            this.actionModeTop.setLayoutParams(layoutParams);
        }
    }

    public boolean isActionModeShowed() {
        return this.actionMode != null && this.actionModeVisible;
    }

    protected void onSearchFieldVisibilityChanged(boolean visible) {
        int i = 4;
        this.isSearchFieldVisible = visible;
        if (this.titleTextView != null) {
            this.titleTextView.setVisibility(visible ? 4 : 0);
        }
        if (this.subTitleTextView != null) {
            TextView textView = this.subTitleTextView;
            if (!visible) {
                i = 0;
            }
            textView.setVisibility(i);
        }
        Drawable drawable = this.backButtonImageView.getDrawable();
        if (drawable != null && (drawable instanceof MenuDrawable)) {
            ((MenuDrawable) drawable).setRotation(visible ? 1.0f : 0.0f, true);
        }
    }

    public void closeSearchField() {
        if (this.isSearchFieldVisible && this.menu != null) {
            this.menu.closeSearchField();
        }
    }

    public void openSearchField(String text) {
        if (this.menu != null && text != null) {
            this.menu.openSearchField(!this.isSearchFieldVisible, text);
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int textLeft;
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int actionBarHeight = getCurrentActionBarHeight();
        int actionBarHeightSpec = MeasureSpec.makeMeasureSpec(actionBarHeight, 1073741824);
        setMeasuredDimension(width, (this.occupyStatusBar ? AndroidUtilities.statusBarHeight : 0) + actionBarHeight);
        if (this.backButtonImageView == null || this.backButtonImageView.getVisibility() == 8) {
            textLeft = AndroidUtilities.dp(AndroidUtilities.isTablet() ? 26.0f : 18.0f);
        } else {
            this.backButtonImageView.measure(MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(54.0f), 1073741824), actionBarHeightSpec);
            textLeft = AndroidUtilities.dp(AndroidUtilities.isTablet() ? 80.0f : 72.0f);
        }
        if (!(this.menu == null || this.menu.getVisibility() == 8)) {
            int menuWidth;
            if (this.isSearchFieldVisible) {
                menuWidth = MeasureSpec.makeMeasureSpec(width - AndroidUtilities.dp(AndroidUtilities.isTablet() ? 74.0f : 66.0f), 1073741824);
            } else {
                menuWidth = MeasureSpec.makeMeasureSpec(width, Integer.MIN_VALUE);
            }
            this.menu.measure(menuWidth, actionBarHeightSpec);
        }
        if (!((this.titleTextView == null || this.titleTextView.getVisibility() == 8) && (this.subTitleTextView == null || this.subTitleTextView.getVisibility() == 8))) {
            TextView textView;
            float f;
            int availableWidth = ((width - (this.menu != null ? this.menu.getMeasuredWidth() : 0)) - AndroidUtilities.dp(16.0f)) - textLeft;
            if (!(this.titleTextView == null || this.titleTextView.getVisibility() == 8)) {
                textView = this.titleTextView;
                f = (AndroidUtilities.isTablet() || getResources().getConfiguration().orientation != 2) ? 20.0f : 18.0f;
                textView.setTextSize(1, f);
                this.titleTextView.measure(MeasureSpec.makeMeasureSpec(availableWidth, Integer.MIN_VALUE), MeasureSpec.makeMeasureSpec(actionBarHeight, Integer.MIN_VALUE));
            }
            if (!(this.subTitleTextView == null || this.subTitleTextView.getVisibility() == 8)) {
                textView = this.subTitleTextView;
                f = (AndroidUtilities.isTablet() || getResources().getConfiguration().orientation != 2) ? 16.0f : 14.0f;
                textView.setTextSize(1, f);
                this.subTitleTextView.measure(MeasureSpec.makeMeasureSpec(availableWidth, Integer.MIN_VALUE), MeasureSpec.makeMeasureSpec(actionBarHeight, Integer.MIN_VALUE));
            }
        }
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (!(child.getVisibility() == 8 || child == this.titleTextView || child == this.subTitleTextView || child == this.menu || child == this.backButtonImageView)) {
                measureChildWithMargins(child, widthMeasureSpec, 0, MeasureSpec.makeMeasureSpec(getMeasuredHeight(), 1073741824), 0);
            }
        }
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int textLeft;
        int textTop;
        int additionalTop = this.occupyStatusBar ? AndroidUtilities.statusBarHeight : 0;
        if (this.backButtonImageView == null || this.backButtonImageView.getVisibility() == 8) {
            textLeft = AndroidUtilities.dp(AndroidUtilities.isTablet() ? 26.0f : 18.0f);
        } else {
            this.backButtonImageView.layout(0, additionalTop, this.backButtonImageView.getMeasuredWidth(), this.backButtonImageView.getMeasuredHeight() + additionalTop);
            textLeft = AndroidUtilities.dp(AndroidUtilities.isTablet() ? 80.0f : 72.0f);
        }
        if (!(this.menu == null || this.menu.getVisibility() == 8)) {
            int menuLeft;
            if (this.isSearchFieldVisible) {
                menuLeft = AndroidUtilities.dp(AndroidUtilities.isTablet() ? 74.0f : 66.0f);
            } else {
                menuLeft = (right - left) - this.menu.getMeasuredWidth();
            }
            this.menu.layout(menuLeft, additionalTop, this.menu.getMeasuredWidth() + menuLeft, this.menu.getMeasuredHeight() + additionalTop);
        }
        float f = (AndroidUtilities.isTablet() || getResources().getConfiguration().orientation != 2) ? 2.0f : 1.0f;
        int offset = AndroidUtilities.dp(f);
        if (!(this.titleTextView == null || this.titleTextView.getVisibility() == 8)) {
            if (this.subTitleTextView == null || this.subTitleTextView.getVisibility() == 8) {
                textTop = ((getCurrentActionBarHeight() - this.titleTextView.getMeasuredHeight()) / 2) - AndroidUtilities.dp(1.0f);
            } else {
                textTop = (((getCurrentActionBarHeight() / 2) - this.titleTextView.getMeasuredHeight()) / 2) + offset;
            }
            this.titleTextView.layout(textLeft, additionalTop + textTop, this.titleTextView.getMeasuredWidth() + textLeft, (additionalTop + textTop) + this.titleTextView.getMeasuredHeight());
        }
        if (!(this.subTitleTextView == null || this.subTitleTextView.getVisibility() == 8)) {
            textTop = ((getCurrentActionBarHeight() / 2) + (((getCurrentActionBarHeight() / 2) - this.subTitleTextView.getMeasuredHeight()) / 2)) - offset;
            this.subTitleTextView.layout(textLeft, additionalTop + textTop, this.subTitleTextView.getMeasuredWidth() + textLeft, (additionalTop + textTop) + this.subTitleTextView.getMeasuredHeight());
        }
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (!(child.getVisibility() == 8 || child == this.titleTextView || child == this.subTitleTextView || child == this.menu || child == this.backButtonImageView)) {
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

    public void onMenuButtonPressed() {
        if (this.menu != null) {
            this.menu.onMenuButtonPressed();
        }
    }

    protected void onPause() {
        if (this.menu != null) {
            this.menu.hideAllPopupMenus();
        }
    }

    public void setAllowOverlayTitle(boolean value) {
        this.allowOverlayTitle = value;
    }

    public void setTitleOverlayText(String text) {
        if (this.allowOverlayTitle && this.parentFragment.parentLayout != null) {
            CharSequence textToSet = text != null ? text : this.lastTitle;
            if (textToSet != null && this.titleTextView == null) {
                createTitleTextView();
            }
            if (this.titleTextView != null) {
                TextView textView = this.titleTextView;
                int i = (textToSet == null || this.isSearchFieldVisible) ? 4 : 0;
                textView.setVisibility(i);
                this.titleTextView.setText(textToSet);
            }
        }
    }

    public boolean isSearchFieldVisible() {
        return this.isSearchFieldVisible;
    }

    public void setOccupyStatusBar(boolean value) {
        this.occupyStatusBar = value;
        if (this.actionMode != null) {
            int i;
            ActionBarMenu actionBarMenu = this.actionMode;
            if (this.occupyStatusBar) {
                i = AndroidUtilities.statusBarHeight;
            } else {
                i = 0;
            }
            actionBarMenu.setPadding(0, i, 0, 0);
        }
    }

    public boolean getOccupyStatusBar() {
        return this.occupyStatusBar;
    }

    public void setItemsBackground(int resourceId) {
        this.itemsBackgroundResourceId = resourceId;
        if (this.backButtonImageView != null) {
            this.backButtonImageView.setBackgroundResource(this.itemsBackgroundResourceId);
        }
    }

    public void setCastShadows(boolean value) {
        this.castShadows = value;
    }

    public boolean getCastShadows() {
        return this.castShadows;
    }

    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        return true;
    }

    public static int getCurrentActionBarHeight() {
        if (AndroidUtilities.isTablet()) {
            return AndroidUtilities.dp(64.0f);
        }
        if (ApplicationLoader.applicationContext.getResources().getConfiguration().orientation == 2) {
            return AndroidUtilities.dp(48.0f);
        }
        return AndroidUtilities.dp(56.0f);
    }

    public boolean hasOverlappingRendering() {
        return false;
    }
}
