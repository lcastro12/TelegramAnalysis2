package org.telegram.ui;

import android.content.Context;
import android.os.Build.VERSION;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.C0553R;
import org.telegram.messenger.LocaleController;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.ActionBarMenuItem.ActionBarMenuItemSearchListener;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Adapters.CountryAdapter;
import org.telegram.ui.Adapters.CountryAdapter.Country;
import org.telegram.ui.Adapters.CountrySearchAdapter;
import org.telegram.ui.Components.LetterSectionsListView;

public class CountrySelectActivity extends BaseFragment {
    private CountrySelectActivityDelegate delegate;
    private TextView emptyTextView;
    private LetterSectionsListView listView;
    private CountryAdapter listViewAdapter;
    private CountrySearchAdapter searchListViewAdapter;
    private boolean searchWas;
    private boolean searching;

    class C09893 implements OnTouchListener {
        C09893() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            return true;
        }
    }

    class C09904 implements OnItemClickListener {
        C09904() {
        }

        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Country country;
            if (CountrySelectActivity.this.searching && CountrySelectActivity.this.searchWas) {
                country = CountrySelectActivity.this.searchListViewAdapter.getItem(i);
            } else {
                int section = CountrySelectActivity.this.listViewAdapter.getSectionForPosition(i);
                int row = CountrySelectActivity.this.listViewAdapter.getPositionInSectionForPosition(i);
                if (row >= 0 && section >= 0) {
                    country = CountrySelectActivity.this.listViewAdapter.getItem(section, row);
                } else {
                    return;
                }
            }
            if (i >= 0) {
                CountrySelectActivity.this.finishFragment();
                if (country != null && CountrySelectActivity.this.delegate != null) {
                    CountrySelectActivity.this.delegate.didSelectCountry(country.name);
                }
            }
        }
    }

    class C09915 implements OnScrollListener {
        C09915() {
        }

        public void onScrollStateChanged(AbsListView absListView, int i) {
            if (i == 1 && CountrySelectActivity.this.searching && CountrySelectActivity.this.searchWas) {
                AndroidUtilities.hideKeyboard(CountrySelectActivity.this.getParentActivity().getCurrentFocus());
            }
        }

        public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (absListView.isFastScrollEnabled()) {
                AndroidUtilities.clearDrawableAnimation(absListView);
            }
        }
    }

    public interface CountrySelectActivityDelegate {
        void didSelectCountry(String str);
    }

    class C15811 extends ActionBarMenuOnItemClick {
        C15811() {
        }

        public void onItemClick(int id) {
            if (id == -1) {
                CountrySelectActivity.this.finishFragment();
            }
        }
    }

    class C15822 extends ActionBarMenuItemSearchListener {
        C15822() {
        }

        public void onSearchExpand() {
            CountrySelectActivity.this.searching = true;
        }

        public void onSearchCollapse() {
            CountrySelectActivity.this.searchListViewAdapter.search(null);
            CountrySelectActivity.this.searching = false;
            CountrySelectActivity.this.searchWas = false;
            CountrySelectActivity.this.listView.setAdapter(CountrySelectActivity.this.listViewAdapter);
            if (VERSION.SDK_INT >= 11) {
                CountrySelectActivity.this.listView.setFastScrollAlwaysVisible(true);
            }
            CountrySelectActivity.this.listView.setFastScrollEnabled(true);
            CountrySelectActivity.this.listView.setVerticalScrollBarEnabled(false);
            CountrySelectActivity.this.emptyTextView.setText(LocaleController.getString("ChooseCountry", C0553R.string.ChooseCountry));
        }

        public void onTextChanged(EditText editText) {
            String text = editText.getText().toString();
            CountrySelectActivity.this.searchListViewAdapter.search(text);
            if (text.length() != 0) {
                CountrySelectActivity.this.searchWas = true;
                if (CountrySelectActivity.this.listView != null) {
                    CountrySelectActivity.this.listView.setAdapter(CountrySelectActivity.this.searchListViewAdapter);
                    if (VERSION.SDK_INT >= 11) {
                        CountrySelectActivity.this.listView.setFastScrollAlwaysVisible(false);
                    }
                    CountrySelectActivity.this.listView.setFastScrollEnabled(false);
                    CountrySelectActivity.this.listView.setVerticalScrollBarEnabled(true);
                }
                if (CountrySelectActivity.this.emptyTextView == null) {
                }
            }
        }
    }

    public boolean onFragmentCreate() {
        return super.onFragmentCreate();
    }

    public void onFragmentDestroy() {
        super.onFragmentDestroy();
    }

    public View createView(Context context) {
        this.actionBar.setBackButtonImage(C0553R.drawable.ic_ab_back);
        this.actionBar.setAllowOverlayTitle(true);
        this.actionBar.setTitle(LocaleController.getString("ChooseCountry", C0553R.string.ChooseCountry));
        this.actionBar.setActionBarMenuOnItemClick(new C15811());
        this.actionBar.createMenu().addItem(0, (int) C0553R.drawable.ic_ab_search).setIsSearchField(true).setActionBarMenuItemSearchListener(new C15822()).getSearchField().setHint(LocaleController.getString("Search", C0553R.string.Search));
        this.searching = false;
        this.searchWas = false;
        this.listViewAdapter = new CountryAdapter(context);
        this.searchListViewAdapter = new CountrySearchAdapter(context, this.listViewAdapter.getCountries());
        this.fragmentView = new FrameLayout(context);
        LinearLayout emptyTextLayout = new LinearLayout(context);
        emptyTextLayout.setVisibility(4);
        emptyTextLayout.setOrientation(1);
        ((FrameLayout) this.fragmentView).addView(emptyTextLayout);
        LayoutParams layoutParams = (LayoutParams) emptyTextLayout.getLayoutParams();
        layoutParams.width = -1;
        layoutParams.height = -1;
        layoutParams.gravity = 48;
        emptyTextLayout.setLayoutParams(layoutParams);
        emptyTextLayout.setOnTouchListener(new C09893());
        this.emptyTextView = new TextView(context);
        this.emptyTextView.setTextColor(-8355712);
        this.emptyTextView.setTextSize(20.0f);
        this.emptyTextView.setGravity(17);
        this.emptyTextView.setText(LocaleController.getString("NoResult", C0553R.string.NoResult));
        emptyTextLayout.addView(this.emptyTextView);
        LinearLayout.LayoutParams layoutParams1 = (LinearLayout.LayoutParams) this.emptyTextView.getLayoutParams();
        layoutParams1.width = -1;
        layoutParams1.height = -1;
        layoutParams1.weight = 0.5f;
        this.emptyTextView.setLayoutParams(layoutParams1);
        FrameLayout frameLayout = new FrameLayout(context);
        emptyTextLayout.addView(frameLayout);
        layoutParams1 = (LinearLayout.LayoutParams) frameLayout.getLayoutParams();
        layoutParams1.width = -1;
        layoutParams1.height = -1;
        layoutParams1.weight = 0.5f;
        frameLayout.setLayoutParams(layoutParams1);
        this.listView = new LetterSectionsListView(context);
        this.listView.setEmptyView(emptyTextLayout);
        this.listView.setVerticalScrollBarEnabled(false);
        this.listView.setDivider(null);
        this.listView.setDividerHeight(0);
        this.listView.setFastScrollEnabled(true);
        this.listView.setScrollBarStyle(33554432);
        this.listView.setAdapter(this.listViewAdapter);
        if (VERSION.SDK_INT >= 11) {
            this.listView.setFastScrollAlwaysVisible(true);
            this.listView.setVerticalScrollbarPosition(LocaleController.isRTL ? 1 : 2);
        }
        ((FrameLayout) this.fragmentView).addView(this.listView);
        layoutParams = (LayoutParams) this.listView.getLayoutParams();
        layoutParams.width = -1;
        layoutParams.height = -1;
        this.listView.setLayoutParams(layoutParams);
        this.listView.setOnItemClickListener(new C09904());
        this.listView.setOnScrollListener(new C09915());
        return this.fragmentView;
    }

    public void onResume() {
        super.onResume();
        if (this.listViewAdapter != null) {
            this.listViewAdapter.notifyDataSetChanged();
        }
    }

    public void setCountrySelectActivityDelegate(CountrySelectActivityDelegate delegate) {
        this.delegate = delegate;
    }
}
