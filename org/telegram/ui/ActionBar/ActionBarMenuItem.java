package org.telegram.ui.ActionBar;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build.VERSION;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import java.lang.reflect.Field;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.AnimationCompat.ViewProxy;
import org.telegram.messenger.C0553R;
import org.telegram.messenger.LocaleController;
import org.telegram.ui.ActionBar.ActionBarPopupWindow.ActionBarPopupWindowLayout;
import org.telegram.ui.ActionBar.ActionBarPopupWindow.OnDispatchKeyEventListener;
import org.telegram.ui.Components.FrameLayoutFixed;

public class ActionBarMenuItem extends FrameLayoutFixed {
    private boolean allowCloseAnimation = true;
    private ImageView clearButton;
    private ActionBarMenuItemDelegate delegate;
    protected ImageView iconView;
    private boolean isSearchField = false;
    private ActionBarMenuItemSearchListener listener;
    private int[] location;
    private int menuHeight = AndroidUtilities.dp(16.0f);
    protected boolean overrideMenuClick;
    private ActionBarMenu parentMenu;
    private ActionBarPopupWindowLayout popupLayout;
    private ActionBarPopupWindow popupWindow;
    private boolean processedPopupClick;
    private Rect rect;
    private FrameLayout searchContainer;
    private EditText searchField;
    private View selectedMenuView;
    private boolean showFromBottom;
    private Runnable showMenuRunnable;
    private int subMenuOpenSide = 0;

    class C06981 implements Runnable {
        C06981() {
        }

        public void run() {
            if (ActionBarMenuItem.this.getParent() != null) {
                ActionBarMenuItem.this.getParent().requestDisallowInterceptTouchEvent(true);
            }
            ActionBarMenuItem.this.toggleSubMenu();
        }
    }

    class C06992 implements OnTouchListener {
        C06992() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            if (event.getActionMasked() == 0 && ActionBarMenuItem.this.popupWindow != null && ActionBarMenuItem.this.popupWindow.isShowing()) {
                v.getHitRect(ActionBarMenuItem.this.rect);
                if (!ActionBarMenuItem.this.rect.contains((int) event.getX(), (int) event.getY())) {
                    ActionBarMenuItem.this.popupWindow.dismiss();
                }
            }
            return false;
        }
    }

    class C07004 implements OnClickListener {
        C07004() {
        }

        public void onClick(View view) {
            if (ActionBarMenuItem.this.popupWindow != null && ActionBarMenuItem.this.popupWindow.isShowing()) {
                if (!ActionBarMenuItem.this.processedPopupClick) {
                    ActionBarMenuItem.this.processedPopupClick = true;
                    ActionBarMenuItem.this.popupWindow.dismiss(ActionBarMenuItem.this.allowCloseAnimation);
                } else {
                    return;
                }
            }
            if (ActionBarMenuItem.this.parentMenu != null) {
                ActionBarMenuItem.this.parentMenu.onItemClick(((Integer) view.getTag()).intValue());
            } else if (ActionBarMenuItem.this.delegate != null) {
                ActionBarMenuItem.this.delegate.onItemClick(((Integer) view.getTag()).intValue());
            }
        }
    }

    class C07015 implements OnKeyListener {
        C07015() {
        }

        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (keyCode != 82 || event.getRepeatCount() != 0 || event.getAction() != 1 || ActionBarMenuItem.this.popupWindow == null || !ActionBarMenuItem.this.popupWindow.isShowing()) {
                return false;
            }
            ActionBarMenuItem.this.popupWindow.dismiss();
            return true;
        }
    }

    class C07026 implements OnCreateContextMenuListener {
        C07026() {
        }

        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
            menu.clear();
        }
    }

    class C07037 implements Callback {
        C07037() {
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        public void onDestroyActionMode(ActionMode mode) {
        }

        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return false;
        }
    }

    class C07048 implements OnEditorActionListener {
        C07048() {
        }

        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == 3 || (event != null && ((event.getAction() == 1 && event.getKeyCode() == 84) || (event.getAction() == 0 && event.getKeyCode() == 66)))) {
                AndroidUtilities.hideKeyboard(ActionBarMenuItem.this.searchField);
                if (ActionBarMenuItem.this.listener != null) {
                    ActionBarMenuItem.this.listener.onSearchPressed(ActionBarMenuItem.this.searchField);
                }
            }
            return false;
        }
    }

    class C07059 implements TextWatcher {
        C07059() {
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (ActionBarMenuItem.this.listener != null) {
                ActionBarMenuItem.this.listener.onTextChanged(ActionBarMenuItem.this.searchField);
            }
            if (ActionBarMenuItem.this.clearButton != null) {
                View access$800 = ActionBarMenuItem.this.clearButton;
                float f = (s == null || s.length() == 0) ? 0.6f : 1.0f;
                ViewProxy.setAlpha(access$800, f);
            }
        }

        public void afterTextChanged(Editable s) {
        }
    }

    public interface ActionBarMenuItemDelegate {
        void onItemClick(int i);
    }

    public static class ActionBarMenuItemSearchListener {
        public void onSearchExpand() {
        }

        public boolean canCollapseSearch() {
            return true;
        }

        public void onSearchCollapse() {
        }

        public void onTextChanged(EditText editText) {
        }

        public void onSearchPressed(EditText editText) {
        }
    }

    class C14973 implements OnDispatchKeyEventListener {
        C14973() {
        }

        public void onDispatchKeyEvent(KeyEvent keyEvent) {
            if (keyEvent.getKeyCode() == 4 && keyEvent.getRepeatCount() == 0 && ActionBarMenuItem.this.popupWindow != null && ActionBarMenuItem.this.popupWindow.isShowing()) {
                ActionBarMenuItem.this.popupWindow.dismiss();
            }
        }
    }

    public ActionBarMenuItem(Context context, ActionBarMenu menu, int background) {
        super(context);
        setBackgroundResource(background);
        this.parentMenu = menu;
        this.iconView = new ImageView(context);
        this.iconView.setScaleType(ScaleType.CENTER);
        addView(this.iconView);
        LayoutParams layoutParams = (LayoutParams) this.iconView.getLayoutParams();
        layoutParams.width = -1;
        layoutParams.height = -1;
        this.iconView.setLayoutParams(layoutParams);
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == 0) {
            if (hasSubMenu() && (this.popupWindow == null || !(this.popupWindow == null || this.popupWindow.isShowing()))) {
                this.showMenuRunnable = new C06981();
                AndroidUtilities.runOnUIThread(this.showMenuRunnable, 200);
            }
        } else if (event.getActionMasked() == 2) {
            if (!hasSubMenu() || (this.popupWindow != null && (this.popupWindow == null || this.popupWindow.isShowing()))) {
                if (this.popupWindow != null && this.popupWindow.isShowing()) {
                    getLocationOnScreen(this.location);
                    float x = event.getX() + ((float) this.location[0]);
                    float y = event.getY() + ((float) this.location[1]);
                    this.popupLayout.getLocationOnScreen(this.location);
                    x -= (float) this.location[0];
                    y -= (float) this.location[1];
                    this.selectedMenuView = null;
                    for (int a = 0; a < this.popupLayout.getItemsCount(); a++) {
                        View child = this.popupLayout.getItemAt(a);
                        child.getHitRect(this.rect);
                        if (((Integer) child.getTag()).intValue() < 100) {
                            if (this.rect.contains((int) x, (int) y)) {
                                child.setPressed(true);
                                child.setSelected(true);
                                if (VERSION.SDK_INT >= 21) {
                                    if (VERSION.SDK_INT == 21) {
                                        child.getBackground().setVisible(true, false);
                                    }
                                    child.drawableHotspotChanged(x, y - ((float) child.getTop()));
                                }
                                this.selectedMenuView = child;
                            } else {
                                child.setPressed(false);
                                child.setSelected(false);
                                if (VERSION.SDK_INT == 21) {
                                    child.getBackground().setVisible(false, false);
                                }
                            }
                        }
                    }
                }
            } else if (event.getY() > ((float) getHeight())) {
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                toggleSubMenu();
                return true;
            }
        } else if (this.popupWindow != null && this.popupWindow.isShowing() && event.getActionMasked() == 1) {
            if (this.selectedMenuView != null) {
                this.selectedMenuView.setSelected(false);
                if (this.parentMenu != null) {
                    this.parentMenu.onItemClick(((Integer) this.selectedMenuView.getTag()).intValue());
                } else if (this.delegate != null) {
                    this.delegate.onItemClick(((Integer) this.selectedMenuView.getTag()).intValue());
                }
                this.popupWindow.dismiss(this.allowCloseAnimation);
            } else {
                this.popupWindow.dismiss();
            }
        } else if (this.selectedMenuView != null) {
            this.selectedMenuView.setSelected(false);
            this.selectedMenuView = null;
        }
        return super.onTouchEvent(event);
    }

    public void setDelegate(ActionBarMenuItemDelegate delegate) {
        this.delegate = delegate;
    }

    public void setShowFromBottom(boolean value) {
        this.showFromBottom = value;
        if (this.popupLayout != null) {
            this.popupLayout.setShowedFromBotton(this.showFromBottom);
        }
    }

    public void setSubMenuOpenSide(int side) {
        this.subMenuOpenSide = side;
    }

    public TextView addSubItem(int id, String text, int icon) {
        if (this.popupLayout == null) {
            this.rect = new Rect();
            this.location = new int[2];
            this.popupLayout = new ActionBarPopupWindowLayout(getContext());
            this.popupLayout.setOnTouchListener(new C06992());
            this.popupLayout.setDispatchKeyEventListener(new C14973());
        }
        TextView textView = new TextView(getContext());
        textView.setTextColor(-14606047);
        textView.setBackgroundResource(C0553R.drawable.list_selector);
        if (LocaleController.isRTL) {
            textView.setGravity(21);
        } else {
            textView.setGravity(16);
        }
        textView.setPadding(AndroidUtilities.dp(16.0f), 0, AndroidUtilities.dp(16.0f), 0);
        textView.setTextSize(18.0f);
        textView.setMinWidth(AndroidUtilities.dp(196.0f));
        textView.setTag(Integer.valueOf(id));
        textView.setText(text);
        if (icon != 0) {
            textView.setCompoundDrawablePadding(AndroidUtilities.dp(12.0f));
            if (LocaleController.isRTL) {
                textView.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(icon), null);
            } else {
                textView.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(icon), null, null, null);
            }
        }
        this.popupLayout.setShowedFromBotton(this.showFromBottom);
        this.popupLayout.addView(textView);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) textView.getLayoutParams();
        if (LocaleController.isRTL) {
            layoutParams.gravity = 5;
        }
        layoutParams.width = -1;
        layoutParams.height = AndroidUtilities.dp(48.0f);
        textView.setLayoutParams(layoutParams);
        textView.setOnClickListener(new C07004());
        this.menuHeight += layoutParams.height;
        return textView;
    }

    public boolean hasSubMenu() {
        return this.popupLayout != null;
    }

    public void toggleSubMenu() {
        if (this.popupLayout != null) {
            if (this.showMenuRunnable != null) {
                AndroidUtilities.cancelRunOnUIThread(this.showMenuRunnable);
                this.showMenuRunnable = null;
            }
            if (this.popupWindow == null || !this.popupWindow.isShowing()) {
                if (this.popupWindow == null) {
                    this.popupWindow = new ActionBarPopupWindow(this.popupLayout, -2, -2);
                    if (VERSION.SDK_INT >= 19) {
                        this.popupWindow.setAnimationStyle(0);
                    } else {
                        this.popupWindow.setAnimationStyle(C0553R.style.PopupAnimation);
                    }
                    this.popupWindow.setOutsideTouchable(true);
                    this.popupWindow.setClippingEnabled(true);
                    this.popupWindow.setInputMethodMode(2);
                    this.popupWindow.setSoftInputMode(0);
                    this.popupLayout.measure(MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(1000.0f), Integer.MIN_VALUE), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(1000.0f), Integer.MIN_VALUE));
                    this.popupWindow.getContentView().setFocusableInTouchMode(true);
                    this.popupWindow.getContentView().setOnKeyListener(new C07015());
                }
                this.processedPopupClick = false;
                this.popupWindow.setFocusable(true);
                if (this.popupLayout.getMeasuredWidth() == 0) {
                    updateOrShowPopup(true, true);
                } else {
                    updateOrShowPopup(true, false);
                }
                this.popupWindow.startAnimation();
                return;
            }
            this.popupWindow.dismiss();
        }
    }

    public void openSearch(boolean openKeyboard) {
        if (this.searchContainer != null && this.searchContainer.getVisibility() != 0 && this.parentMenu != null) {
            this.parentMenu.parentActionBar.onSearchFieldVisibilityChanged(toggleSearch(openKeyboard));
        }
    }

    public boolean toggleSearch(boolean openKeyboard) {
        if (this.searchContainer == null) {
            return false;
        }
        if (this.searchContainer.getVisibility() != 0) {
            this.searchContainer.setVisibility(0);
            setVisibility(8);
            this.searchField.setText("");
            this.searchField.requestFocus();
            if (openKeyboard) {
                AndroidUtilities.showKeyboard(this.searchField);
            }
            if (this.listener != null) {
                this.listener.onSearchExpand();
            }
            return true;
        } else if (this.listener != null && (this.listener == null || !this.listener.canCollapseSearch())) {
            return false;
        } else {
            this.searchContainer.setVisibility(8);
            setVisibility(0);
            AndroidUtilities.hideKeyboard(this.searchField);
            if (this.listener == null) {
                return false;
            }
            this.listener.onSearchCollapse();
            return false;
        }
    }

    public void closeSubMenu() {
        if (this.popupWindow != null && this.popupWindow.isShowing()) {
            this.popupWindow.dismiss();
        }
    }

    public void setIcon(int resId) {
        this.iconView.setImageResource(resId);
    }

    public EditText getSearchField() {
        return this.searchField;
    }

    public ActionBarMenuItem setIsSearchField(boolean value) {
        return setIsSearchField(value, true);
    }

    public ActionBarMenuItem setOverrideMenuClick(boolean value) {
        this.overrideMenuClick = value;
        return this;
    }

    public ActionBarMenuItem setIsSearchField(boolean value, boolean needClearButton) {
        int i = 0;
        if (this.parentMenu != null) {
            if (value && this.searchContainer == null) {
                this.searchContainer = new FrameLayout(getContext());
                this.parentMenu.addView(this.searchContainer, 0);
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.searchContainer.getLayoutParams();
                layoutParams.weight = 1.0f;
                layoutParams.width = 0;
                layoutParams.height = -1;
                layoutParams.leftMargin = AndroidUtilities.dp(6.0f);
                this.searchContainer.setLayoutParams(layoutParams);
                this.searchContainer.setVisibility(8);
                this.searchField = new EditText(getContext());
                this.searchField.setTextSize(1, 18.0f);
                this.searchField.setHintTextColor(-1996488705);
                this.searchField.setTextColor(-1);
                this.searchField.setSingleLine(true);
                this.searchField.setBackgroundResource(0);
                this.searchField.setPadding(0, 0, 0, 0);
                this.searchField.setInputType(this.searchField.getInputType() | 524288);
                if (VERSION.SDK_INT < 11) {
                    this.searchField.setOnCreateContextMenuListener(new C07026());
                } else {
                    this.searchField.setCustomSelectionActionModeCallback(new C07037());
                }
                this.searchField.setOnEditorActionListener(new C07048());
                this.searchField.addTextChangedListener(new C07059());
                try {
                    Field mCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
                    mCursorDrawableRes.setAccessible(true);
                    mCursorDrawableRes.set(this.searchField, Integer.valueOf(C0553R.drawable.search_carret));
                } catch (Exception e) {
                }
                if (VERSION.SDK_INT >= 11) {
                    this.searchField.setImeOptions(33554435);
                    this.searchField.setTextIsSelectable(false);
                } else {
                    this.searchField.setImeOptions(3);
                }
                this.searchContainer.addView(this.searchField);
                LayoutParams layoutParams2 = (LayoutParams) this.searchField.getLayoutParams();
                layoutParams2.width = -1;
                layoutParams2.gravity = 16;
                layoutParams2.height = AndroidUtilities.dp(36.0f);
                if (needClearButton) {
                    i = AndroidUtilities.dp(48.0f);
                }
                layoutParams2.rightMargin = i;
                this.searchField.setLayoutParams(layoutParams2);
                if (needClearButton) {
                    this.clearButton = new ImageView(getContext());
                    this.clearButton.setImageResource(C0553R.drawable.ic_close_white);
                    this.clearButton.setScaleType(ScaleType.CENTER);
                    this.clearButton.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            ActionBarMenuItem.this.searchField.setText("");
                            AndroidUtilities.showKeyboard(ActionBarMenuItem.this.searchField);
                        }
                    });
                    this.searchContainer.addView(this.clearButton);
                    layoutParams2 = (LayoutParams) this.clearButton.getLayoutParams();
                    layoutParams2.width = AndroidUtilities.dp(48.0f);
                    layoutParams2.gravity = 21;
                    layoutParams2.height = -1;
                    this.clearButton.setLayoutParams(layoutParams2);
                }
            }
            this.isSearchField = value;
        }
        return this;
    }

    public boolean isSearchField() {
        return this.isSearchField;
    }

    public ActionBarMenuItem setActionBarMenuItemSearchListener(ActionBarMenuItemSearchListener listener) {
        this.listener = listener;
        return this;
    }

    public ActionBarMenuItem setAllowCloseAnimation(boolean value) {
        this.allowCloseAnimation = value;
        return this;
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (this.popupWindow != null && this.popupWindow.isShowing()) {
            updateOrShowPopup(false, true);
        }
    }

    private void updateOrShowPopup(boolean show, boolean update) {
        int offsetY;
        if (this.showFromBottom) {
            getLocationOnScreen(this.location);
            int diff = ((this.location[1] - AndroidUtilities.statusBarHeight) + getMeasuredHeight()) - this.menuHeight;
            offsetY = -this.menuHeight;
            if (diff < 0) {
                offsetY -= diff;
            }
        } else if (this.parentMenu == null || this.subMenuOpenSide != 0) {
            offsetY = -getMeasuredHeight();
        } else {
            offsetY = (-this.parentMenu.parentActionBar.getMeasuredHeight()) + this.parentMenu.getTop();
        }
        if (show) {
            this.popupLayout.scrollToTop();
        }
        if (this.subMenuOpenSide != 0) {
            if (show) {
                this.popupWindow.showAsDropDown(this, -AndroidUtilities.dp(8.0f), offsetY);
            }
            if (update) {
                this.popupWindow.update(this, -AndroidUtilities.dp(8.0f), offsetY, -1, -1);
            }
        } else if (this.showFromBottom) {
            if (show) {
                this.popupWindow.showAsDropDown(this, (-this.popupLayout.getMeasuredWidth()) + getMeasuredWidth(), offsetY);
            }
            if (update) {
                this.popupWindow.update(this, (-this.popupLayout.getMeasuredWidth()) + getMeasuredWidth(), offsetY, -1, -1);
            }
        } else if (this.parentMenu != null) {
            parent = this.parentMenu.parentActionBar;
            if (show) {
                this.popupWindow.showAsDropDown(parent, ((getLeft() + this.parentMenu.getLeft()) + getMeasuredWidth()) - this.popupLayout.getMeasuredWidth(), offsetY);
            }
            if (update) {
                this.popupWindow.update(parent, ((getLeft() + this.parentMenu.getLeft()) + getMeasuredWidth()) - this.popupLayout.getMeasuredWidth(), offsetY, -1, -1);
            }
        } else if (getParent() != null) {
            parent = (View) getParent();
            if (show) {
                this.popupWindow.showAsDropDown(parent, ((parent.getMeasuredWidth() - this.popupLayout.getMeasuredWidth()) - getLeft()) - parent.getLeft(), offsetY);
            }
            if (update) {
                this.popupWindow.update(parent, ((parent.getMeasuredWidth() - this.popupLayout.getMeasuredWidth()) - getLeft()) - parent.getLeft(), offsetY, -1, -1);
            }
        }
    }

    public void hideSubItem(int id) {
        View view = this.popupLayout.findViewWithTag(Integer.valueOf(id));
        if (view != null) {
            view.setVisibility(8);
        }
    }

    public void showSubItem(int id) {
        View view = this.popupLayout.findViewWithTag(Integer.valueOf(id));
        if (view != null) {
            view.setVisibility(0);
        }
    }
}
