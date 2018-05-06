package org.telegram.ui.ActionBar;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import org.telegram.messenger.AndroidUtilities;

public class ActionBarMenu extends LinearLayout {
    protected ActionBar parentActionBar;

    class C06961 implements OnClickListener {
        C06961() {
        }

        public void onClick(View view) {
            ActionBarMenu.this.onItemClick(((Integer) view.getTag()).intValue());
        }
    }

    class C06972 implements OnClickListener {
        C06972() {
        }

        public void onClick(View view) {
            ActionBarMenuItem item = (ActionBarMenuItem) view;
            if (item.hasSubMenu()) {
                if (ActionBarMenu.this.parentActionBar.actionBarMenuOnItemClick.canOpenMenu()) {
                    item.toggleSubMenu();
                }
            } else if (item.isSearchField()) {
                ActionBarMenu.this.parentActionBar.onSearchFieldVisibilityChanged(item.toggleSearch(true));
            } else {
                ActionBarMenu.this.onItemClick(((Integer) view.getTag()).intValue());
            }
        }
    }

    public ActionBarMenu(Context context, ActionBar layer) {
        super(context);
        setOrientation(0);
        this.parentActionBar = layer;
    }

    public ActionBarMenu(Context context) {
        super(context);
    }

    public View addItemResource(int id, int resourceId) {
        View view = ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(resourceId, null);
        view.setTag(Integer.valueOf(id));
        addView(view);
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        layoutParams.height = -1;
        view.setBackgroundResource(this.parentActionBar.itemsBackgroundResourceId);
        view.setLayoutParams(layoutParams);
        view.setOnClickListener(new C06961());
        return view;
    }

    public ActionBarMenuItem addItem(int id, Drawable drawable) {
        return addItem(id, 0, this.parentActionBar.itemsBackgroundResourceId, drawable, AndroidUtilities.dp(48.0f));
    }

    public ActionBarMenuItem addItem(int id, int icon) {
        return addItem(id, icon, this.parentActionBar.itemsBackgroundResourceId);
    }

    public ActionBarMenuItem addItem(int id, int icon, int backgroundResource) {
        return addItem(id, icon, backgroundResource, null, AndroidUtilities.dp(48.0f));
    }

    public ActionBarMenuItem addItemWithWidth(int id, int icon, int width) {
        return addItem(id, icon, this.parentActionBar.itemsBackgroundResourceId, null, width);
    }

    public ActionBarMenuItem addItem(int id, int icon, int backgroundResource, Drawable drawable, int width) {
        ActionBarMenuItem menuItem = new ActionBarMenuItem(getContext(), this, backgroundResource);
        menuItem.setTag(Integer.valueOf(id));
        if (drawable != null) {
            menuItem.iconView.setImageDrawable(drawable);
        } else {
            menuItem.iconView.setImageResource(icon);
        }
        addView(menuItem);
        LayoutParams layoutParams = (LayoutParams) menuItem.getLayoutParams();
        layoutParams.height = -1;
        layoutParams.width = width;
        menuItem.setLayoutParams(layoutParams);
        menuItem.setOnClickListener(new C06972());
        return menuItem;
    }

    public void hideAllPopupMenus() {
        for (int a = 0; a < getChildCount(); a++) {
            View view = getChildAt(a);
            if (view instanceof ActionBarMenuItem) {
                ((ActionBarMenuItem) view).closeSubMenu();
            }
        }
    }

    public void onItemClick(int id) {
        if (this.parentActionBar.actionBarMenuOnItemClick != null) {
            this.parentActionBar.actionBarMenuOnItemClick.onItemClick(id);
        }
    }

    public void clearItems() {
        for (int a = 0; a < getChildCount(); a++) {
            removeView(getChildAt(a));
        }
    }

    public void onMenuButtonPressed() {
        for (int a = 0; a < getChildCount(); a++) {
            View view = getChildAt(a);
            if (view instanceof ActionBarMenuItem) {
                ActionBarMenuItem item = (ActionBarMenuItem) view;
                if (item.getVisibility() != 0) {
                    continue;
                } else if (item.hasSubMenu()) {
                    item.toggleSubMenu();
                    return;
                } else if (item.overrideMenuClick) {
                    onItemClick(((Integer) item.getTag()).intValue());
                    return;
                }
            }
        }
    }

    public void closeSearchField() {
        for (int a = 0; a < getChildCount(); a++) {
            View view = getChildAt(a);
            if (view instanceof ActionBarMenuItem) {
                ActionBarMenuItem item = (ActionBarMenuItem) view;
                if (item.isSearchField()) {
                    this.parentActionBar.onSearchFieldVisibilityChanged(item.toggleSearch(false));
                    return;
                }
            }
        }
    }

    public void openSearchField(boolean toggle, String text) {
        for (int a = 0; a < getChildCount(); a++) {
            View view = getChildAt(a);
            if (view instanceof ActionBarMenuItem) {
                ActionBarMenuItem item = (ActionBarMenuItem) view;
                if (item.isSearchField()) {
                    if (toggle) {
                        this.parentActionBar.onSearchFieldVisibilityChanged(item.toggleSearch(true));
                    }
                    item.getSearchField().setText(text);
                    item.getSearchField().setSelection(text.length());
                    return;
                }
            }
        }
    }

    public ActionBarMenuItem getItem(int id) {
        View v = findViewWithTag(Integer.valueOf(id));
        if (v instanceof ActionBarMenuItem) {
            return (ActionBarMenuItem) v;
        }
        return null;
    }
}
