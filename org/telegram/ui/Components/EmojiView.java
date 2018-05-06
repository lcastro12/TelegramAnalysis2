package org.telegram.ui.Components;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnScrollChangedListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.AnimationCompat.ViewProxy;
import org.telegram.messenger.C0553R;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.EmojiData;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.query.StickersQuery;
import org.telegram.tgnet.TLRPC.Document;
import org.telegram.tgnet.TLRPC.TL_messages_stickerSet;
import org.telegram.ui.Cells.EmptyCell;
import org.telegram.ui.Cells.StickerEmojiCell;
import org.telegram.ui.Components.PagerSlidingTabStrip.IconTabProvider;
import org.telegram.ui.Components.ScrollSlidingTabStrip.ScrollSlidingTabStripDelegate;
import org.telegram.ui.StickerPreviewViewer;

public class EmojiView extends FrameLayout implements NotificationCenterDelegate {
    private static final OnScrollChangedListener NOP = new C09061();
    private static HashMap<String, String> emojiColor = new HashMap();
    private static final Field superListenerField;
    private ArrayList<EmojiGridAdapter> adapters = new ArrayList();
    private ImageView backspaceButton;
    private boolean backspaceOnce;
    private boolean backspacePressed;
    private StickerEmojiCell currentStickerPreviewCell;
    private int emojiSize;
    private HashMap<String, Integer> emojiUseHistory = new HashMap();
    private int[] icons = new int[]{C0553R.drawable.ic_emoji_recent, C0553R.drawable.ic_emoji_smile, C0553R.drawable.ic_emoji_flower, C0553R.drawable.ic_emoji_bell, C0553R.drawable.ic_emoji_car, C0553R.drawable.ic_emoji_symbol, C0553R.drawable.ic_emoji_sticker};
    private int lastNotifyWidth;
    private Listener listener;
    private int[] location = new int[2];
    private int oldWidth;
    private Runnable openStickerPreviewRunnable;
    private ViewPager pager;
    private LinearLayout pagerSlidingTabStripContainer;
    private EmojiColorPickerView pickerView;
    private EmojiPopupWindow pickerViewPopup;
    private int popupHeight;
    private int popupWidth;
    private ArrayList<String> recentEmoji = new ArrayList();
    private ArrayList<Document> recentStickers = new ArrayList();
    private FrameLayout recentsWrap;
    private ScrollSlidingTabStrip scrollSlidingTabStrip;
    private boolean showStickers;
    private int startX;
    private int startY;
    private ArrayList<TL_messages_stickerSet> stickerSets = new ArrayList();
    private StickersGridAdapter stickersGridAdapter;
    private GridView stickersGridView;
    private OnItemClickListener stickersOnItemClickListener;
    private HashMap<Long, Integer> stickersUseHistory = new HashMap();
    private FrameLayout stickersWrap;
    private ArrayList<GridView> views = new ArrayList();

    static class C09061 implements OnScrollChangedListener {
        C09061() {
        }

        public void onScrollChanged() {
        }
    }

    class C09103 implements OnTouchListener {

        class C09091 implements Runnable {
            C09091() {
            }

            public void run() {
                EmojiView.this.stickersGridView.setOnItemClickListener(EmojiView.this.stickersOnItemClickListener);
            }
        }

        C09103() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            if (EmojiView.this.openStickerPreviewRunnable != null || StickerPreviewViewer.getInstance().isVisible()) {
                if (event.getAction() == 1 || event.getAction() == 3 || event.getAction() == 6) {
                    AndroidUtilities.runOnUIThread(new C09091(), 150);
                    if (EmojiView.this.openStickerPreviewRunnable != null) {
                        AndroidUtilities.cancelRunOnUIThread(EmojiView.this.openStickerPreviewRunnable);
                        EmojiView.this.openStickerPreviewRunnable = null;
                    } else if (StickerPreviewViewer.getInstance().isVisible()) {
                        StickerPreviewViewer.getInstance().close();
                        if (EmojiView.this.currentStickerPreviewCell != null) {
                            EmojiView.this.currentStickerPreviewCell.setScaled(false);
                            EmojiView.this.currentStickerPreviewCell = null;
                        }
                    }
                } else if (event.getAction() != 0) {
                    if (StickerPreviewViewer.getInstance().isVisible()) {
                        if (event.getAction() == 2) {
                            int x = (int) event.getX();
                            int y = (int) event.getY();
                            int count = EmojiView.this.stickersGridView.getChildCount();
                            int a = 0;
                            while (a < count) {
                                View view = EmojiView.this.stickersGridView.getChildAt(a);
                                int top = view.getTop();
                                int bottom = view.getBottom();
                                int left = view.getLeft();
                                int right = view.getRight();
                                if (top > y || bottom < y || left > x || right < x) {
                                    a++;
                                } else if ((view instanceof StickerEmojiCell) && view != EmojiView.this.currentStickerPreviewCell) {
                                    if (EmojiView.this.currentStickerPreviewCell != null) {
                                        EmojiView.this.currentStickerPreviewCell.setScaled(false);
                                    }
                                    EmojiView.this.currentStickerPreviewCell = (StickerEmojiCell) view;
                                    StickerPreviewViewer.getInstance().setKeyboardHeight(EmojiView.this.getMeasuredHeight());
                                    StickerPreviewViewer.getInstance().open(EmojiView.this.currentStickerPreviewCell.getSticker());
                                    EmojiView.this.currentStickerPreviewCell.setScaled(true);
                                    return true;
                                }
                            }
                        }
                        return true;
                    } else if (EmojiView.this.openStickerPreviewRunnable != null) {
                        if (event.getAction() != 2) {
                            AndroidUtilities.cancelRunOnUIThread(EmojiView.this.openStickerPreviewRunnable);
                            EmojiView.this.openStickerPreviewRunnable = null;
                        } else if (Math.hypot((double) (((float) EmojiView.this.startX) - event.getX()), (double) (((float) EmojiView.this.startY) - event.getY())) > ((double) AndroidUtilities.dp(10.0f))) {
                            AndroidUtilities.cancelRunOnUIThread(EmojiView.this.openStickerPreviewRunnable);
                            EmojiView.this.openStickerPreviewRunnable = null;
                        }
                    }
                }
            }
            return false;
        }
    }

    class C09114 implements OnItemClickListener {
        C09114() {
        }

        public void onItemClick(AdapterView<?> adapterView, View view, int position, long i) {
            if (view instanceof StickerEmojiCell) {
                if (EmojiView.this.openStickerPreviewRunnable != null) {
                    AndroidUtilities.cancelRunOnUIThread(EmojiView.this.openStickerPreviewRunnable);
                    EmojiView.this.openStickerPreviewRunnable = null;
                }
                if (EmojiView.this.currentStickerPreviewCell != null) {
                    EmojiView.this.currentStickerPreviewCell.setScaled(false);
                    EmojiView.this.currentStickerPreviewCell = null;
                }
                StickerEmojiCell cell = (StickerEmojiCell) view;
                if (!cell.isDisabled()) {
                    cell.disable();
                    Document document = cell.getSticker();
                    Integer count = (Integer) EmojiView.this.stickersUseHistory.get(Long.valueOf(document.id));
                    if (count == null) {
                        count = Integer.valueOf(0);
                    }
                    if (count.intValue() == 0 && EmojiView.this.stickersUseHistory.size() > 19) {
                        for (int a = EmojiView.this.recentStickers.size() - 1; a >= 0; a--) {
                            EmojiView.this.stickersUseHistory.remove(Long.valueOf(((Document) EmojiView.this.recentStickers.get(a)).id));
                            EmojiView.this.recentStickers.remove(a);
                            if (EmojiView.this.stickersUseHistory.size() <= 19) {
                                break;
                            }
                        }
                    }
                    EmojiView.this.stickersUseHistory.put(Long.valueOf(document.id), Integer.valueOf(count.intValue() + 1));
                    EmojiView.this.saveRecentStickers();
                    if (EmojiView.this.listener != null) {
                        EmojiView.this.listener.onStickerSelected(document);
                    }
                }
            }
        }
    }

    class C09127 implements OnScrollListener {
        C09127() {
        }

        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }

        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            int count = view.getChildCount();
            for (int a = 0; a < count; a++) {
                View child = view.getChildAt(a);
                if (child.getHeight() + child.getTop() >= AndroidUtilities.dp(5.0f)) {
                    break;
                }
                firstVisibleItem++;
            }
            EmojiView.this.scrollSlidingTabStrip.onPageScrolled(EmojiView.this.stickersGridAdapter.getTabForPosition(firstVisibleItem) + 1, 0);
        }
    }

    private class EmojiColorPickerView extends View {
        private Drawable arrowDrawable = getResources().getDrawable(C0553R.drawable.stickers_back_arrow);
        private int arrowX;
        private Drawable backgroundDrawable = getResources().getDrawable(C0553R.drawable.stickers_back_all);
        private String currentEmoji;
        private RectF rect = new RectF();
        private Paint rectPaint = new Paint(1);
        private int selection;

        public void setEmoji(String emoji, int arrowPosition) {
            this.currentEmoji = emoji;
            this.arrowX = arrowPosition;
            this.rectPaint.setColor(788529152);
            invalidate();
        }

        public String getEmoji() {
            return this.currentEmoji;
        }

        public void setSelection(int position) {
            if (this.selection != position) {
                this.selection = position;
                invalidate();
            }
        }

        public int getSelection() {
            return this.selection;
        }

        public EmojiColorPickerView(Context context) {
            super(context);
        }

        protected void onDraw(Canvas canvas) {
            float f;
            float f2 = 55.5f;
            this.backgroundDrawable.setBounds(0, 0, getMeasuredWidth(), AndroidUtilities.dp(AndroidUtilities.isTablet() ? BitmapDescriptorFactory.HUE_YELLOW : 52.0f));
            this.backgroundDrawable.draw(canvas);
            Drawable drawable = this.arrowDrawable;
            int dp = this.arrowX - AndroidUtilities.dp(9.0f);
            if (AndroidUtilities.isTablet()) {
                f = 55.5f;
            } else {
                f = 47.5f;
            }
            int dp2 = AndroidUtilities.dp(f);
            int dp3 = this.arrowX + AndroidUtilities.dp(9.0f);
            if (!AndroidUtilities.isTablet()) {
                f2 = 47.5f;
            }
            drawable.setBounds(dp, dp2, dp3, AndroidUtilities.dp(f2 + 8.0f));
            this.arrowDrawable.draw(canvas);
            if (this.currentEmoji != null) {
                for (int a = 0; a < 6; a++) {
                    int x = (EmojiView.this.emojiSize * a) + AndroidUtilities.dp((float) ((a * 4) + 5));
                    int y = AndroidUtilities.dp(9.0f);
                    if (this.selection == a) {
                        this.rect.set((float) x, (float) (y - ((int) AndroidUtilities.dpf2(3.5f))), (float) (EmojiView.this.emojiSize + x), (float) ((EmojiView.this.emojiSize + y) + AndroidUtilities.dp(3.0f)));
                        canvas.drawRoundRect(this.rect, (float) AndroidUtilities.dp(4.0f), (float) AndroidUtilities.dp(4.0f), this.rectPaint);
                    }
                    String code = this.currentEmoji;
                    if (a != 0) {
                        code = code + "?";
                        switch (a) {
                            case 1:
                                code = code + "?";
                                break;
                            case 2:
                                code = code + "?";
                                break;
                            case 3:
                                code = code + "?";
                                break;
                            case 4:
                                code = code + "?";
                                break;
                            case 5:
                                code = code + "?";
                                break;
                        }
                    }
                    Drawable drawable2 = Emoji.getEmojiBigDrawable(code);
                    if (drawable2 != null) {
                        drawable2.setBounds(x, y, EmojiView.this.emojiSize + x, EmojiView.this.emojiSize + y);
                        drawable2.draw(canvas);
                    }
                }
            }
        }
    }

    private class EmojiGridAdapter extends BaseAdapter {
        private int emojiPage;

        public EmojiGridAdapter(int page) {
            this.emojiPage = page;
        }

        public int getCount() {
            if (this.emojiPage == -1) {
                return EmojiView.this.recentEmoji.size();
            }
            return EmojiData.dataColored[this.emojiPage].length;
        }

        public Object getItem(int i) {
            return null;
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int i, View view, ViewGroup paramViewGroup) {
            String code;
            String coloredCode;
            ImageViewEmoji imageView = (ImageViewEmoji) view;
            if (imageView == null) {
                imageView = new ImageViewEmoji(EmojiView.this.getContext());
            }
            if (this.emojiPage == -1) {
                code = (String) EmojiView.this.recentEmoji.get(i);
                coloredCode = code;
            } else {
                code = EmojiData.dataColored[this.emojiPage][i];
                coloredCode = code;
                String color = (String) EmojiView.emojiColor.get(code);
                if (color != null) {
                    coloredCode = coloredCode + color;
                }
            }
            imageView.setImageDrawable(Emoji.getEmojiBigDrawable(coloredCode));
            imageView.setTag(code);
            return imageView;
        }

        public void unregisterDataSetObserver(DataSetObserver observer) {
            if (observer != null) {
                super.unregisterDataSetObserver(observer);
            }
        }
    }

    private class EmojiPopupWindow extends PopupWindow {
        private OnScrollChangedListener mSuperScrollListener;
        private ViewTreeObserver mViewTreeObserver;

        public EmojiPopupWindow() {
            init();
        }

        public EmojiPopupWindow(Context context) {
            super(context);
            init();
        }

        public EmojiPopupWindow(int width, int height) {
            super(width, height);
            init();
        }

        public EmojiPopupWindow(View contentView) {
            super(contentView);
            init();
        }

        public EmojiPopupWindow(View contentView, int width, int height, boolean focusable) {
            super(contentView, width, height, focusable);
            init();
        }

        public EmojiPopupWindow(View contentView, int width, int height) {
            super(contentView, width, height);
            init();
        }

        private void init() {
            if (EmojiView.superListenerField != null) {
                try {
                    this.mSuperScrollListener = (OnScrollChangedListener) EmojiView.superListenerField.get(this);
                    EmojiView.superListenerField.set(this, EmojiView.NOP);
                } catch (Exception e) {
                    this.mSuperScrollListener = null;
                }
            }
        }

        private void unregisterListener() {
            if (this.mSuperScrollListener != null && this.mViewTreeObserver != null) {
                if (this.mViewTreeObserver.isAlive()) {
                    this.mViewTreeObserver.removeOnScrollChangedListener(this.mSuperScrollListener);
                }
                this.mViewTreeObserver = null;
            }
        }

        private void registerListener(View anchor) {
            if (this.mSuperScrollListener != null) {
                ViewTreeObserver vto = anchor.getWindowToken() != null ? anchor.getViewTreeObserver() : null;
                if (vto != this.mViewTreeObserver) {
                    if (this.mViewTreeObserver != null && this.mViewTreeObserver.isAlive()) {
                        this.mViewTreeObserver.removeOnScrollChangedListener(this.mSuperScrollListener);
                    }
                    this.mViewTreeObserver = vto;
                    if (vto != null) {
                        vto.addOnScrollChangedListener(this.mSuperScrollListener);
                    }
                }
            }
        }

        public void showAsDropDown(View anchor, int xoff, int yoff) {
            try {
                super.showAsDropDown(anchor, xoff, yoff);
                registerListener(anchor);
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
        }

        public void update(View anchor, int xoff, int yoff, int width, int height) {
            super.update(anchor, xoff, yoff, width, height);
            registerListener(anchor);
        }

        public void update(View anchor, int width, int height) {
            super.update(anchor, width, height);
            registerListener(anchor);
        }

        public void showAtLocation(View parent, int gravity, int x, int y) {
            super.showAtLocation(parent, gravity, x, y);
            unregisterListener();
        }

        public void dismiss() {
            setFocusable(false);
            try {
                super.dismiss();
            } catch (Exception e) {
            }
            unregisterListener();
        }
    }

    private class ImageViewEmoji extends ImageView {
        private float lastX;
        private float lastY;
        private boolean touched;
        private float touchedX;
        private float touchedY;

        public ImageViewEmoji(Context context) {
            super(context);
            setOnClickListener(new OnClickListener(EmojiView.this) {
                public void onClick(View view) {
                    ImageViewEmoji.this.sendEmoji(null);
                }
            });
            setOnLongClickListener(new OnLongClickListener(EmojiView.this) {
                public boolean onLongClick(View view) {
                    int yOffset = 0;
                    String code = (String) view.getTag();
                    if (!EmojiData.emojiColoredMap.containsKey(code)) {
                        return false;
                    }
                    int i;
                    ImageViewEmoji.this.touched = true;
                    ImageViewEmoji.this.touchedX = ImageViewEmoji.this.lastX;
                    ImageViewEmoji.this.touchedY = ImageViewEmoji.this.lastY;
                    String color = (String) EmojiView.emojiColor.get(code);
                    if (color != null) {
                        i = -1;
                        switch (color.hashCode()) {
                            case 1773375:
                                if (color.equals("🏻")) {
                                    i = 0;
                                    break;
                                }
                                break;
                            case 1773376:
                                if (color.equals("🏼")) {
                                    boolean z = true;
                                    break;
                                }
                                break;
                            case 1773377:
                                if (color.equals("🏽")) {
                                    i = 2;
                                    break;
                                }
                                break;
                            case 1773378:
                                if (color.equals("🏾")) {
                                    i = 3;
                                    break;
                                }
                                break;
                            case 1773379:
                                if (color.equals("🏿")) {
                                    i = 4;
                                    break;
                                }
                                break;
                        }
                        switch (i) {
                            case 0:
                                EmojiView.this.pickerView.setSelection(1);
                                break;
                            case 1:
                                EmojiView.this.pickerView.setSelection(2);
                                break;
                            case 2:
                                EmojiView.this.pickerView.setSelection(3);
                                break;
                            case 3:
                                EmojiView.this.pickerView.setSelection(4);
                                break;
                            case 4:
                                EmojiView.this.pickerView.setSelection(5);
                                break;
                        }
                    }
                    EmojiView.this.pickerView.setSelection(0);
                    view.getLocationOnScreen(EmojiView.this.location);
                    int selection = EmojiView.this.pickerView.getSelection() * EmojiView.this.emojiSize;
                    int selection2 = EmojiView.this.pickerView.getSelection() * 4;
                    if (AndroidUtilities.isTablet()) {
                        i = 5;
                    } else {
                        i = 1;
                    }
                    int x = selection + AndroidUtilities.dp((float) (selection2 - i));
                    if (EmojiView.this.location[0] - x < AndroidUtilities.dp(5.0f)) {
                        x += (EmojiView.this.location[0] - x) - AndroidUtilities.dp(5.0f);
                    } else if ((EmojiView.this.location[0] - x) + EmojiView.this.popupWidth > AndroidUtilities.displaySize.x - AndroidUtilities.dp(5.0f)) {
                        x += ((EmojiView.this.location[0] - x) + EmojiView.this.popupWidth) - (AndroidUtilities.displaySize.x - AndroidUtilities.dp(5.0f));
                    }
                    int xOffset = -x;
                    if (view.getTop() < 0) {
                        yOffset = view.getTop();
                    }
                    EmojiView.this.pickerView.setEmoji(code, (AndroidUtilities.dp(AndroidUtilities.isTablet() ? BitmapDescriptorFactory.HUE_ORANGE : 22.0f) - xOffset) + ((int) AndroidUtilities.dpf2(0.5f)));
                    EmojiView.this.pickerViewPopup.setFocusable(true);
                    EmojiView.this.pickerViewPopup.showAsDropDown(view, xOffset, (((-view.getMeasuredHeight()) - EmojiView.this.popupHeight) + ((view.getMeasuredHeight() - EmojiView.this.emojiSize) / 2)) - yOffset);
                    view.getParent().requestDisallowInterceptTouchEvent(true);
                    return true;
                }
            });
            setBackgroundResource(C0553R.drawable.list_selector);
            setScaleType(ScaleType.CENTER);
        }

        private void sendEmoji(String override) {
            String code;
            if (override != null) {
                code = override;
            } else {
                code = (String) getTag();
            }
            if (override == null) {
                if (EmojiView.this.pager.getCurrentItem() != 0) {
                    String color = (String) EmojiView.emojiColor.get(code);
                    if (color != null) {
                        code = code + color;
                    }
                }
                Integer count = (Integer) EmojiView.this.emojiUseHistory.get(code);
                if (count == null) {
                    count = Integer.valueOf(0);
                }
                if (count.intValue() == 0 && EmojiView.this.emojiUseHistory.size() > 50) {
                    for (int a = EmojiView.this.recentEmoji.size() - 1; a >= 0; a--) {
                        EmojiView.this.emojiUseHistory.remove((String) EmojiView.this.recentEmoji.get(a));
                        EmojiView.this.recentEmoji.remove(a);
                        if (EmojiView.this.emojiUseHistory.size() <= 50) {
                            break;
                        }
                    }
                }
                EmojiView.this.emojiUseHistory.put(code, Integer.valueOf(count.intValue() + 1));
                if (EmojiView.this.pager.getCurrentItem() != 0) {
                    EmojiView.this.sortEmoji();
                }
                EmojiView.this.saveRecentEmoji();
                ((EmojiGridAdapter) EmojiView.this.adapters.get(0)).notifyDataSetChanged();
                if (EmojiView.this.listener != null) {
                    EmojiView.this.listener.onEmojiSelected(Emoji.fixEmoji(code));
                }
            } else if (EmojiView.this.listener != null) {
                EmojiView.this.listener.onEmojiSelected(Emoji.fixEmoji(override));
            }
        }

        public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(widthMeasureSpec));
        }

        public boolean onTouchEvent(MotionEvent event) {
            if (this.touched) {
                if (event.getAction() == 1 || event.getAction() == 3) {
                    if (EmojiView.this.pickerViewPopup != null && EmojiView.this.pickerViewPopup.isShowing()) {
                        EmojiView.this.pickerViewPopup.dismiss();
                        String color = null;
                        switch (EmojiView.this.pickerView.getSelection()) {
                            case 1:
                                color = "🏻";
                                break;
                            case 2:
                                color = "🏼";
                                break;
                            case 3:
                                color = "🏽";
                                break;
                            case 4:
                                color = "🏾";
                                break;
                            case 5:
                                color = "🏿";
                                break;
                        }
                        String code = (String) getTag();
                        if (EmojiView.this.pager.getCurrentItem() != 0) {
                            if (color != null) {
                                EmojiView.emojiColor.put(code, color);
                                code = code + color;
                            } else {
                                EmojiView.emojiColor.remove(code);
                            }
                            setImageDrawable(Emoji.getEmojiBigDrawable(code));
                            sendEmoji(null);
                            EmojiView.this.saveEmojiColors();
                        } else {
                            StringBuilder append = new StringBuilder().append(code);
                            if (color == null) {
                                color = "";
                            }
                            sendEmoji(append.append(color).toString());
                        }
                    }
                    this.touched = false;
                    this.touchedX = -10000.0f;
                    this.touchedY = -10000.0f;
                } else if (event.getAction() == 2) {
                    boolean ignore = false;
                    if (this.touchedX != -10000.0f) {
                        if (Math.abs(this.touchedX - event.getX()) > AndroidUtilities.getPixelsInCM(0.2f, true) || Math.abs(this.touchedY - event.getY()) > AndroidUtilities.getPixelsInCM(0.2f, false)) {
                            this.touchedX = -10000.0f;
                            this.touchedY = -10000.0f;
                        } else {
                            ignore = true;
                        }
                    }
                    if (!ignore) {
                        getLocationOnScreen(EmojiView.this.location);
                        float x = ((float) EmojiView.this.location[0]) + event.getX();
                        EmojiView.this.pickerView.getLocationOnScreen(EmojiView.this.location);
                        int position = (int) ((x - ((float) (EmojiView.this.location[0] + AndroidUtilities.dp(3.0f)))) / ((float) (EmojiView.this.emojiSize + AndroidUtilities.dp(4.0f))));
                        if (position < 0) {
                            position = 0;
                        } else if (position > 5) {
                            position = 5;
                        }
                        EmojiView.this.pickerView.setSelection(position);
                    }
                }
            }
            this.lastX = event.getX();
            this.lastY = event.getY();
            return super.onTouchEvent(event);
        }
    }

    public interface Listener {
        boolean onBackspace();

        void onEmojiSelected(String str);

        void onStickerSelected(Document document);

        void onStickersSettingsClick();
    }

    private class StickersGridAdapter extends BaseAdapter {
        private HashMap<Integer, Document> cache = new HashMap();
        private Context context;
        private HashMap<TL_messages_stickerSet, Integer> packStartRow = new HashMap();
        private HashMap<Integer, TL_messages_stickerSet> rowStartPack = new HashMap();
        private int stickersPerRow;
        private int totalItems;

        public StickersGridAdapter(Context context) {
            this.context = context;
        }

        public int getCount() {
            return this.totalItems != 0 ? this.totalItems + 1 : 0;
        }

        public Object getItem(int i) {
            return this.cache.get(Integer.valueOf(i));
        }

        public long getItemId(int i) {
            return -1;
        }

        public int getPositionForPack(TL_messages_stickerSet stickerSet) {
            return ((Integer) this.packStartRow.get(stickerSet)).intValue() * this.stickersPerRow;
        }

        public boolean areAllItemsEnabled() {
            return false;
        }

        public boolean isEnabled(int position) {
            return this.cache.get(Integer.valueOf(position)) != null;
        }

        public int getItemViewType(int position) {
            if (this.cache.get(Integer.valueOf(position)) != null) {
                return 0;
            }
            return 1;
        }

        public int getViewTypeCount() {
            return 2;
        }

        public int getTabForPosition(int position) {
            int i = 0;
            if (this.stickersPerRow == 0) {
                int width = EmojiView.this.getMeasuredWidth();
                if (width == 0) {
                    width = AndroidUtilities.displaySize.x;
                }
                this.stickersPerRow = width / AndroidUtilities.dp(72.0f);
            }
            TL_messages_stickerSet pack = (TL_messages_stickerSet) this.rowStartPack.get(Integer.valueOf(position / this.stickersPerRow));
            if (pack == null) {
                return 0;
            }
            int indexOf = EmojiView.this.stickerSets.indexOf(pack);
            if (!EmojiView.this.recentStickers.isEmpty()) {
                i = 1;
            }
            return i + indexOf;
        }

        public View getView(int i, View view, ViewGroup viewGroup) {
            Document sticker = (Document) this.cache.get(Integer.valueOf(i));
            if (sticker != null) {
                if (view == null) {
                    view = new StickerEmojiCell(this.context) {
                        public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(82.0f), 1073741824));
                        }
                    };
                }
                ((StickerEmojiCell) view).setSticker(sticker, false);
            } else {
                if (view == null) {
                    view = new EmptyCell(this.context);
                }
                if (i == this.totalItems) {
                    TL_messages_stickerSet pack = (TL_messages_stickerSet) this.rowStartPack.get(Integer.valueOf((i - 1) / this.stickersPerRow));
                    if (pack == null) {
                        ((EmptyCell) view).setHeight(1);
                    } else {
                        int height = EmojiView.this.pager.getHeight() - (((int) Math.ceil((double) (((float) pack.documents.size()) / ((float) this.stickersPerRow)))) * AndroidUtilities.dp(82.0f));
                        EmptyCell emptyCell = (EmptyCell) view;
                        if (height <= 0) {
                            height = 1;
                        }
                        emptyCell.setHeight(height);
                    }
                } else {
                    ((EmptyCell) view).setHeight(AndroidUtilities.dp(82.0f));
                }
            }
            return view;
        }

        public void notifyDataSetChanged() {
            int width = EmojiView.this.getMeasuredWidth();
            if (width == 0) {
                width = AndroidUtilities.displaySize.x;
            }
            this.stickersPerRow = width / AndroidUtilities.dp(72.0f);
            this.rowStartPack.clear();
            this.packStartRow.clear();
            this.cache.clear();
            this.totalItems = 0;
            ArrayList<TL_messages_stickerSet> packs = EmojiView.this.stickerSets;
            for (int a = -1; a < packs.size(); a++) {
                ArrayList<Document> documents;
                TL_messages_stickerSet pack = null;
                int startRow = this.totalItems / this.stickersPerRow;
                if (a == -1) {
                    documents = EmojiView.this.recentStickers;
                } else {
                    pack = (TL_messages_stickerSet) packs.get(a);
                    documents = pack.documents;
                    this.packStartRow.put(pack, Integer.valueOf(startRow));
                }
                if (!documents.isEmpty()) {
                    int b;
                    int count = (int) Math.ceil((double) (((float) documents.size()) / ((float) this.stickersPerRow)));
                    for (b = 0; b < documents.size(); b++) {
                        this.cache.put(Integer.valueOf(this.totalItems + b), documents.get(b));
                    }
                    this.totalItems += this.stickersPerRow * count;
                    for (b = 0; b < count; b++) {
                        this.rowStartPack.put(Integer.valueOf(startRow + b), pack);
                    }
                }
            }
            super.notifyDataSetChanged();
        }

        public void unregisterDataSetObserver(DataSetObserver observer) {
            if (observer != null) {
                super.unregisterDataSetObserver(observer);
            }
        }
    }

    class C15586 implements ScrollSlidingTabStripDelegate {
        C15586() {
        }

        public void onPageSelected(int page) {
            int i = 1;
            if (page == 0) {
                EmojiView.this.pager.setCurrentItem(0);
            } else if (page != 1 || EmojiView.this.recentStickers.isEmpty()) {
                if (!EmojiView.this.recentStickers.isEmpty()) {
                    i = 2;
                }
                int index = page - i;
                if (index != EmojiView.this.stickerSets.size()) {
                    if (index >= EmojiView.this.stickerSets.size()) {
                        index = EmojiView.this.stickerSets.size() - 1;
                    }
                    ((GridView) EmojiView.this.views.get(6)).setSelection(EmojiView.this.stickersGridAdapter.getPositionForPack((TL_messages_stickerSet) EmojiView.this.stickerSets.get(index)));
                } else if (EmojiView.this.listener != null) {
                    EmojiView.this.listener.onStickersSettingsClick();
                }
            } else {
                ((GridView) EmojiView.this.views.get(6)).setSelection(0);
            }
        }
    }

    private class EmojiPagesAdapter extends PagerAdapter implements IconTabProvider {
        private EmojiPagesAdapter() {
        }

        public void destroyItem(ViewGroup viewGroup, int position, Object object) {
            View view;
            if (position == 0) {
                view = EmojiView.this.recentsWrap;
            } else if (position == 6) {
                view = EmojiView.this.stickersWrap;
            } else {
                view = (View) EmojiView.this.views.get(position);
            }
            viewGroup.removeView(view);
        }

        public int getCount() {
            return EmojiView.this.views.size();
        }

        public int getPageIconResId(int paramInt) {
            return EmojiView.this.icons[paramInt];
        }

        public Object instantiateItem(ViewGroup viewGroup, int position) {
            View view;
            if (position == 0) {
                view = EmojiView.this.recentsWrap;
            } else if (position == 6) {
                view = EmojiView.this.stickersWrap;
            } else {
                view = (View) EmojiView.this.views.get(position);
            }
            viewGroup.addView(view);
            return view;
        }

        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        public void unregisterDataSetObserver(DataSetObserver observer) {
            if (observer != null) {
                super.unregisterDataSetObserver(observer);
            }
        }
    }

    static {
        Field f = null;
        try {
            f = PopupWindow.class.getDeclaredField("mOnScrollChangedListener");
            f.setAccessible(true);
        } catch (NoSuchFieldException e) {
        }
        superListenerField = f;
    }

    public EmojiView(boolean needStickers, Context context) {
        TextView textView;
        float f;
        super(context);
        this.showStickers = needStickers;
        for (int i = 0; i < EmojiData.dataColored.length + 1; i++) {
            GridView gridView = new GridView(context);
            if (AndroidUtilities.isTablet()) {
                gridView.setColumnWidth(AndroidUtilities.dp(BitmapDescriptorFactory.HUE_YELLOW));
            } else {
                gridView.setColumnWidth(AndroidUtilities.dp(45.0f));
            }
            gridView.setNumColumns(-1);
            this.views.add(gridView);
            EmojiGridAdapter emojiGridAdapter = new EmojiGridAdapter(i - 1);
            gridView.setAdapter(emojiGridAdapter);
            AndroidUtilities.setListViewEdgeEffectColor(gridView, -657673);
            this.adapters.add(emojiGridAdapter);
        }
        if (this.showStickers) {
            StickersQuery.checkStickers();
            this.stickersGridView = new GridView(context) {

                class C09071 implements Runnable {
                    C09071() {
                    }

                    public void run() {
                        if (EmojiView.this.openStickerPreviewRunnable != null) {
                            EmojiView.this.stickersGridView.setOnItemClickListener(null);
                            EmojiView.this.stickersGridView.requestDisallowInterceptTouchEvent(true);
                            EmojiView.this.openStickerPreviewRunnable = null;
                            StickerPreviewViewer.getInstance().setParentActivity((Activity) C09082.this.getContext());
                            StickerPreviewViewer.getInstance().setKeyboardHeight(EmojiView.this.getMeasuredHeight());
                            StickerPreviewViewer.getInstance().open(EmojiView.this.currentStickerPreviewCell.getSticker());
                            EmojiView.this.currentStickerPreviewCell.setScaled(true);
                        }
                    }
                }

                public boolean onInterceptTouchEvent(MotionEvent event) {
                    if (event.getAction() == 0) {
                        int x = (int) event.getX();
                        int y = (int) event.getY();
                        int count = EmojiView.this.stickersGridView.getChildCount();
                        int a = 0;
                        while (a < count) {
                            View view = EmojiView.this.stickersGridView.getChildAt(a);
                            int top = view.getTop();
                            int bottom = view.getBottom();
                            int left = view.getLeft();
                            int right = view.getRight();
                            if (top > y || bottom < y || left > x || right < x) {
                                a++;
                            } else if (!(view instanceof StickerEmojiCell) || !((StickerEmojiCell) view).showingBitmap()) {
                                return super.onInterceptTouchEvent(event);
                            } else {
                                EmojiView.this.startX = x;
                                EmojiView.this.startY = y;
                                EmojiView.this.currentStickerPreviewCell = (StickerEmojiCell) view;
                                EmojiView.this.openStickerPreviewRunnable = new C09071();
                                AndroidUtilities.runOnUIThread(EmojiView.this.openStickerPreviewRunnable, 200);
                                return true;
                            }
                        }
                    }
                    return false;
                }
            };
            this.stickersGridView.setSelector(C0553R.drawable.transparent);
            this.stickersGridView.setColumnWidth(AndroidUtilities.dp(72.0f));
            this.stickersGridView.setNumColumns(-1);
            this.stickersGridView.setPadding(0, AndroidUtilities.dp(4.0f), 0, 0);
            this.stickersGridView.setClipToPadding(false);
            this.views.add(this.stickersGridView);
            this.stickersGridAdapter = new StickersGridAdapter(context);
            this.stickersGridView.setAdapter(this.stickersGridAdapter);
            this.stickersGridView.setOnTouchListener(new C09103());
            this.stickersOnItemClickListener = new C09114();
            this.stickersGridView.setOnItemClickListener(this.stickersOnItemClickListener);
            AndroidUtilities.setListViewEdgeEffectColor(this.stickersGridView, -657673);
            this.stickersWrap = new FrameLayout(context);
            this.stickersWrap.addView(this.stickersGridView);
            textView = new TextView(context);
            textView.setText(LocaleController.getString("NoStickers", C0553R.string.NoStickers));
            textView.setTextSize(1, 18.0f);
            textView.setTextColor(-7829368);
            this.stickersWrap.addView(textView, LayoutHelper.createFrame(-2, -2, 17));
            this.stickersGridView.setEmptyView(textView);
            this.scrollSlidingTabStrip = new ScrollSlidingTabStrip(context) {
                boolean first = true;
                float lastTranslateX;
                float lastX;
                boolean startedScroll;

                public boolean onInterceptTouchEvent(MotionEvent ev) {
                    if (getParent() != null) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                    return super.onInterceptTouchEvent(ev);
                }

                public boolean onTouchEvent(MotionEvent ev) {
                    if (VERSION.SDK_INT < 11) {
                        return super.onTouchEvent(ev);
                    }
                    if (this.first) {
                        this.first = false;
                        this.lastX = ev.getX();
                    }
                    float newTranslationX = ViewProxy.getTranslationX(EmojiView.this.scrollSlidingTabStrip);
                    if (EmojiView.this.scrollSlidingTabStrip.getScrollX() == 0 && newTranslationX == 0.0f) {
                        if (this.startedScroll || this.lastX - ev.getX() >= 0.0f) {
                            if (this.startedScroll && this.lastX - ev.getX() > 0.0f && EmojiView.this.pager.isFakeDragging()) {
                                EmojiView.this.pager.endFakeDrag();
                                this.startedScroll = false;
                            }
                        } else if (EmojiView.this.pager.beginFakeDrag()) {
                            this.startedScroll = true;
                            this.lastTranslateX = ViewProxy.getTranslationX(EmojiView.this.scrollSlidingTabStrip);
                        }
                    }
                    if (this.startedScroll) {
                        try {
                            EmojiView.this.pager.fakeDragBy((float) ((int) (((ev.getX() - this.lastX) + newTranslationX) - this.lastTranslateX)));
                            this.lastTranslateX = newTranslationX;
                        } catch (Throwable e) {
                            try {
                                EmojiView.this.pager.endFakeDrag();
                            } catch (Exception e2) {
                            }
                            this.startedScroll = false;
                            FileLog.m611e("tmessages", e);
                        }
                    }
                    this.lastX = ev.getX();
                    if (ev.getAction() == 3 || ev.getAction() == 1) {
                        this.first = true;
                        if (this.startedScroll) {
                            EmojiView.this.pager.endFakeDrag();
                            this.startedScroll = false;
                        }
                    }
                    if (this.startedScroll || super.onTouchEvent(ev)) {
                        return true;
                    }
                    return false;
                }
            };
            this.scrollSlidingTabStrip.setUnderlineHeight(AndroidUtilities.dp(1.0f));
            this.scrollSlidingTabStrip.setIndicatorColor(-1907225);
            this.scrollSlidingTabStrip.setUnderlineColor(-1907225);
            this.scrollSlidingTabStrip.setVisibility(4);
            addView(this.scrollSlidingTabStrip, LayoutHelper.createFrame(-1, 48, 51));
            ViewProxy.setTranslationX(this.scrollSlidingTabStrip, (float) AndroidUtilities.displaySize.x);
            updateStickerTabs();
            this.scrollSlidingTabStrip.setDelegate(new C15586());
            this.stickersGridView.setOnScrollListener(new C09127());
        }
        setBackgroundColor(-657673);
        this.pager = new ViewPager(context) {
            public boolean onInterceptTouchEvent(MotionEvent ev) {
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                return super.onInterceptTouchEvent(ev);
            }
        };
        EmojiView emojiView = this;
        this.pager.setAdapter(new EmojiPagesAdapter());
        this.pagerSlidingTabStripContainer = new LinearLayout(context) {
            public boolean onInterceptTouchEvent(MotionEvent ev) {
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                return super.onInterceptTouchEvent(ev);
            }
        };
        this.pagerSlidingTabStripContainer.setOrientation(0);
        this.pagerSlidingTabStripContainer.setBackgroundColor(-657673);
        addView(this.pagerSlidingTabStripContainer, LayoutHelper.createFrame(-1, 48.0f));
        PagerSlidingTabStrip pagerSlidingTabStrip = new PagerSlidingTabStrip(context);
        pagerSlidingTabStrip.setViewPager(this.pager);
        pagerSlidingTabStrip.setShouldExpand(true);
        pagerSlidingTabStrip.setIndicatorHeight(AndroidUtilities.dp(2.0f));
        pagerSlidingTabStrip.setUnderlineHeight(AndroidUtilities.dp(1.0f));
        pagerSlidingTabStrip.setIndicatorColor(-13920542);
        pagerSlidingTabStrip.setUnderlineColor(-1907225);
        this.pagerSlidingTabStripContainer.addView(pagerSlidingTabStrip, LayoutHelper.createLinear(0, 48, 1.0f));
        pagerSlidingTabStrip.setOnPageChangeListener(new OnPageChangeListener() {
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                EmojiView.this.onPageScrolled(position, EmojiView.this.getMeasuredWidth(), positionOffsetPixels);
            }

            public void onPageSelected(int position) {
            }

            public void onPageScrollStateChanged(int state) {
            }
        });
        FrameLayout frameLayout = new FrameLayout(context);
        this.pagerSlidingTabStripContainer.addView(frameLayout, LayoutHelper.createLinear(52, 48));
        this.backspaceButton = new ImageView(context) {
            public boolean onTouchEvent(MotionEvent event) {
                if (event.getAction() == 0) {
                    EmojiView.this.backspacePressed = true;
                    EmojiView.this.backspaceOnce = false;
                    EmojiView.this.postBackspaceRunnable(350);
                } else if (event.getAction() == 3 || event.getAction() == 1) {
                    EmojiView.this.backspacePressed = false;
                    if (!(EmojiView.this.backspaceOnce || EmojiView.this.listener == null || !EmojiView.this.listener.onBackspace())) {
                        EmojiView.this.backspaceButton.performHapticFeedback(3);
                    }
                }
                super.onTouchEvent(event);
                return true;
            }
        };
        this.backspaceButton.setImageResource(C0553R.drawable.ic_smiles_backspace);
        this.backspaceButton.setBackgroundResource(C0553R.drawable.ic_emoji_backspace);
        this.backspaceButton.setScaleType(ScaleType.CENTER);
        frameLayout.addView(this.backspaceButton, LayoutHelper.createFrame(52, 48.0f));
        View view = new View(context);
        view.setBackgroundColor(-1907225);
        frameLayout.addView(view, LayoutHelper.createFrame(52, 1, 83));
        this.recentsWrap = new FrameLayout(context);
        this.recentsWrap.addView((View) this.views.get(0));
        textView = new TextView(context);
        textView.setText(LocaleController.getString("NoRecent", C0553R.string.NoRecent));
        textView.setTextSize(18.0f);
        textView.setTextColor(-7829368);
        textView.setGravity(17);
        this.recentsWrap.addView(textView);
        ((GridView) this.views.get(0)).setEmptyView(textView);
        addView(this.pager, LayoutHelper.createFrame(-1, GroundOverlayOptions.NO_DIMENSION, 51, 0.0f, 48.0f, 0.0f, 0.0f));
        this.emojiSize = AndroidUtilities.dp(AndroidUtilities.isTablet() ? 40.0f : 32.0f);
        this.pickerView = new EmojiColorPickerView(context);
        View view2 = this.pickerView;
        int dp = AndroidUtilities.dp((float) ((((AndroidUtilities.isTablet() ? 40 : 32) * 6) + 10) + 20));
        this.popupWidth = dp;
        if (AndroidUtilities.isTablet()) {
            f = 64.0f;
        } else {
            f = 56.0f;
        }
        int dp2 = AndroidUtilities.dp(f);
        this.popupHeight = dp2;
        this.pickerViewPopup = new EmojiPopupWindow(view2, dp, dp2);
        this.pickerViewPopup.setOutsideTouchable(true);
        this.pickerViewPopup.setClippingEnabled(true);
        this.pickerViewPopup.setInputMethodMode(2);
        this.pickerViewPopup.setSoftInputMode(0);
        this.pickerViewPopup.getContentView().setFocusableInTouchMode(true);
        this.pickerViewPopup.getContentView().setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode != 82 || event.getRepeatCount() != 0 || event.getAction() != 1 || EmojiView.this.pickerViewPopup == null || !EmojiView.this.pickerViewPopup.isShowing()) {
                    return false;
                }
                EmojiView.this.pickerViewPopup.dismiss();
                return true;
            }
        });
        loadRecents();
    }

    private void onPageScrolled(int position, int width, int positionOffsetPixels) {
        if (this.scrollSlidingTabStrip != null) {
            if (width == 0) {
                width = AndroidUtilities.displaySize.x;
            }
            int margin = 0;
            if (position == 5) {
                margin = -positionOffsetPixels;
            } else if (position == 6) {
                margin = -width;
            }
            if (ViewProxy.getTranslationX(this.pagerSlidingTabStripContainer) != ((float) margin)) {
                ViewProxy.setTranslationX(this.pagerSlidingTabStripContainer, (float) margin);
                ViewProxy.setTranslationX(this.scrollSlidingTabStrip, (float) (width + margin));
                this.scrollSlidingTabStrip.setVisibility(margin < 0 ? 0 : 4);
                if (VERSION.SDK_INT >= 11) {
                    return;
                }
                if (margin <= (-width)) {
                    this.pagerSlidingTabStripContainer.clearAnimation();
                    this.pagerSlidingTabStripContainer.setVisibility(8);
                    return;
                }
                this.pagerSlidingTabStripContainer.setVisibility(0);
            } else if (VERSION.SDK_INT < 11 && this.pagerSlidingTabStripContainer.getVisibility() == 8) {
                this.pagerSlidingTabStripContainer.clearAnimation();
                this.pagerSlidingTabStripContainer.setVisibility(8);
            }
        }
    }

    private void postBackspaceRunnable(final int time) {
        AndroidUtilities.runOnUIThread(new Runnable() {
            public void run() {
                if (EmojiView.this.backspacePressed) {
                    if (EmojiView.this.listener != null && EmojiView.this.listener.onBackspace()) {
                        EmojiView.this.backspaceButton.performHapticFeedback(3);
                    }
                    EmojiView.this.backspaceOnce = true;
                    EmojiView.this.postBackspaceRunnable(Math.max(50, time - 100));
                }
            }
        }, (long) time);
    }

    private String convert(long paramLong) {
        String str = "";
        for (int i = 0; i < 4; i++) {
            int j = (int) (65535 & (paramLong >> ((3 - i) * 16)));
            if (j != 0) {
                str = str + ((char) j);
            }
        }
        return str;
    }

    private void saveRecentEmoji() {
        SharedPreferences preferences = getContext().getSharedPreferences("emoji", 0);
        StringBuilder stringBuilder = new StringBuilder();
        for (Entry<String, Integer> entry : this.emojiUseHistory.entrySet()) {
            if (stringBuilder.length() != 0) {
                stringBuilder.append(",");
            }
            stringBuilder.append((String) entry.getKey());
            stringBuilder.append("=");
            stringBuilder.append(entry.getValue());
        }
        preferences.edit().putString("emojis2", stringBuilder.toString()).commit();
    }

    private void saveEmojiColors() {
        SharedPreferences preferences = getContext().getSharedPreferences("emoji", 0);
        StringBuilder stringBuilder = new StringBuilder();
        for (Entry<String, String> entry : emojiColor.entrySet()) {
            if (stringBuilder.length() != 0) {
                stringBuilder.append(",");
            }
            stringBuilder.append((String) entry.getKey());
            stringBuilder.append("=");
            stringBuilder.append((String) entry.getValue());
        }
        preferences.edit().putString("color", stringBuilder.toString()).commit();
    }

    private void saveRecentStickers() {
        Editor editor = getContext().getSharedPreferences("emoji", 0).edit();
        StringBuilder stringBuilder = new StringBuilder();
        for (Entry<Long, Integer> entry : this.stickersUseHistory.entrySet()) {
            if (stringBuilder.length() != 0) {
                stringBuilder.append(",");
            }
            stringBuilder.append(entry.getKey());
            stringBuilder.append("=");
            stringBuilder.append(entry.getValue());
        }
        editor.putString("stickers", stringBuilder.toString());
        editor.commit();
    }

    private void sortEmoji() {
        this.recentEmoji.clear();
        for (Entry<String, Integer> entry : this.emojiUseHistory.entrySet()) {
            this.recentEmoji.add(entry.getKey());
        }
        Collections.sort(this.recentEmoji, new Comparator<String>() {
            public int compare(String lhs, String rhs) {
                Integer count1 = (Integer) EmojiView.this.emojiUseHistory.get(lhs);
                Integer count2 = (Integer) EmojiView.this.emojiUseHistory.get(rhs);
                if (count1 == null) {
                    count1 = Integer.valueOf(0);
                }
                if (count2 == null) {
                    count2 = Integer.valueOf(0);
                }
                if (count1.intValue() > count2.intValue()) {
                    return -1;
                }
                if (count1.intValue() < count2.intValue()) {
                    return 1;
                }
                return 0;
            }
        });
        while (this.recentEmoji.size() > 50) {
            this.recentEmoji.remove(this.recentEmoji.size() - 1);
        }
    }

    private void sortStickers() {
        if (StickersQuery.getStickerSets().isEmpty()) {
            this.recentStickers.clear();
            return;
        }
        this.recentStickers.clear();
        HashMap<Long, Integer> hashMap = new HashMap();
        for (Entry<Long, Integer> entry : this.stickersUseHistory.entrySet()) {
            Document sticker = StickersQuery.getStickerById(((Long) entry.getKey()).longValue());
            if (sticker != null) {
                this.recentStickers.add(sticker);
                hashMap.put(Long.valueOf(sticker.id), entry.getValue());
            }
        }
        if (this.stickersUseHistory.size() != hashMap.size()) {
            this.stickersUseHistory = hashMap;
            saveRecentStickers();
        }
        Collections.sort(this.recentStickers, new Comparator<Document>() {
            public int compare(Document lhs, Document rhs) {
                Integer count1 = (Integer) EmojiView.this.stickersUseHistory.get(Long.valueOf(lhs.id));
                Integer count2 = (Integer) EmojiView.this.stickersUseHistory.get(Long.valueOf(rhs.id));
                if (count1 == null) {
                    count1 = Integer.valueOf(0);
                }
                if (count2 == null) {
                    count2 = Integer.valueOf(0);
                }
                if (count1.intValue() > count2.intValue()) {
                    return -1;
                }
                if (count1.intValue() < count2.intValue()) {
                    return 1;
                }
                return 0;
            }
        });
        while (this.recentStickers.size() > 20) {
            this.recentStickers.remove(this.recentStickers.size() - 1);
        }
    }

    private void updateStickerTabs() {
        int a;
        this.scrollSlidingTabStrip.removeTabs();
        this.scrollSlidingTabStrip.addIconTab(C0553R.drawable.ic_emoji_smile);
        if (!this.recentStickers.isEmpty()) {
            this.scrollSlidingTabStrip.addIconTab(C0553R.drawable.ic_smiles_recent);
        }
        this.stickerSets.clear();
        ArrayList<TL_messages_stickerSet> packs = StickersQuery.getStickerSets();
        for (a = 0; a < packs.size(); a++) {
            TL_messages_stickerSet pack = (TL_messages_stickerSet) packs.get(a);
            if (!(pack.set.disabled || pack.documents == null || pack.documents.isEmpty())) {
                this.stickerSets.add(pack);
            }
        }
        for (a = 0; a < this.stickerSets.size(); a++) {
            this.scrollSlidingTabStrip.addStickerTab((Document) ((TL_messages_stickerSet) this.stickerSets.get(a)).documents.get(0));
        }
        this.scrollSlidingTabStrip.addIconTab(C0553R.drawable.ic_settings);
        this.scrollSlidingTabStrip.updateTabStyles();
    }

    public void loadRecents() {
        String str;
        String[] args2;
        SharedPreferences preferences = getContext().getSharedPreferences("emoji", 0);
        try {
            this.emojiUseHistory.clear();
            if (preferences.contains("emojis")) {
                str = preferences.getString("emojis", "");
                if (str != null && str.length() > 0) {
                    for (String arg : str.split(",")) {
                        args2 = arg.split("=");
                        long value = Long.parseLong(args2[0]);
                        String string = "";
                        for (int a = 0; a < 4; a++) {
                            string = String.valueOf((char) ((int) value)) + string;
                            value >>= 16;
                            if (value == 0) {
                                break;
                            }
                        }
                        if (string.length() > 0) {
                            this.emojiUseHistory.put(string, Integer.valueOf(Integer.parseInt(args2[1])));
                        }
                    }
                }
                preferences.edit().remove("emojis").commit();
                saveRecentEmoji();
            } else {
                str = preferences.getString("emojis2", "");
                if (str != null && str.length() > 0) {
                    for (String arg2 : str.split(",")) {
                        args2 = arg2.split("=");
                        this.emojiUseHistory.put(args2[0], Integer.valueOf(Integer.parseInt(args2[1])));
                    }
                }
            }
            if (this.emojiUseHistory.isEmpty()) {
                String[] newRecent = new String[]{"😂", "😘", "❤", "😍", "😊", "😁", "👍", "☺", "😔", "😄", "😭", "💋", "😒", "😳", "😜", "🙈", "😉", "😃", "😢", "😝", "😱", "😡", "😏", "😞", "😅", "😚", "🙊", "😌", "😀", "😋", "😆", "👌", "😐", "😕"};
                for (int i = 0; i < newRecent.length; i++) {
                    this.emojiUseHistory.put(newRecent[i], Integer.valueOf(newRecent.length - i));
                }
                saveRecentEmoji();
            }
            sortEmoji();
            ((EmojiGridAdapter) this.adapters.get(0)).notifyDataSetChanged();
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
        }
        try {
            str = preferences.getString("color", "");
            if (str != null && str.length() > 0) {
                for (String arg22 : str.split(",")) {
                    args2 = arg22.split("=");
                    emojiColor.put(args2[0], args2[1]);
                }
            }
        } catch (Throwable e2) {
            FileLog.m611e("tmessages", e2);
        }
        if (this.showStickers) {
            try {
                this.stickersUseHistory.clear();
                str = preferences.getString("stickers", "");
                if (str != null && str.length() > 0) {
                    for (String arg222 : str.split(",")) {
                        args2 = arg222.split("=");
                        this.stickersUseHistory.put(Long.valueOf(Long.parseLong(args2[0])), Integer.valueOf(Integer.parseInt(args2[1])));
                    }
                }
                sortStickers();
                updateStickerTabs();
            } catch (Throwable e22) {
                FileLog.m611e("tmessages", e22);
            }
        }
    }

    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        LayoutParams layoutParams = (LayoutParams) this.pagerSlidingTabStripContainer.getLayoutParams();
        LayoutParams layoutParams1 = null;
        layoutParams.width = MeasureSpec.getSize(widthMeasureSpec);
        if (this.scrollSlidingTabStrip != null) {
            layoutParams1 = (LayoutParams) this.scrollSlidingTabStrip.getLayoutParams();
            if (layoutParams1 != null) {
                layoutParams1.width = layoutParams.width;
            }
        }
        if (layoutParams.width != this.oldWidth) {
            if (!(this.scrollSlidingTabStrip == null || layoutParams1 == null)) {
                onPageScrolled(this.pager.getCurrentItem(), layoutParams.width, 0);
                this.scrollSlidingTabStrip.setLayoutParams(layoutParams1);
            }
            this.pagerSlidingTabStripContainer.setLayoutParams(layoutParams);
            this.oldWidth = layoutParams.width;
        }
        super.onMeasure(MeasureSpec.makeMeasureSpec(layoutParams.width, 1073741824), MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec), 1073741824));
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (this.lastNotifyWidth != right - left) {
            this.lastNotifyWidth = right - left;
            reloadStickersAdapter();
        }
        super.onLayout(changed, left, top, right, bottom);
    }

    private void reloadStickersAdapter() {
        if (this.stickersGridAdapter != null) {
            this.stickersGridAdapter.notifyDataSetChanged();
        }
        if (StickerPreviewViewer.getInstance().isVisible()) {
            StickerPreviewViewer.getInstance().close();
        }
        if (this.openStickerPreviewRunnable != null) {
            AndroidUtilities.cancelRunOnUIThread(this.openStickerPreviewRunnable);
            this.openStickerPreviewRunnable = null;
        }
        if (this.currentStickerPreviewCell != null) {
            this.currentStickerPreviewCell.setScaled(false);
            this.currentStickerPreviewCell = null;
        }
    }

    public void setListener(Listener value) {
        this.listener = value;
    }

    public void invalidateViews() {
        Iterator i$ = this.views.iterator();
        while (i$.hasNext()) {
            GridView gridView = (GridView) i$.next();
            if (gridView != null) {
                gridView.invalidateViews();
            }
        }
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.stickersGridAdapter != null) {
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.stickersDidLoaded);
        }
    }

    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility != 8) {
            sortEmoji();
            ((EmojiGridAdapter) this.adapters.get(0)).notifyDataSetChanged();
            if (this.stickersGridAdapter != null) {
                NotificationCenter.getInstance().addObserver(this, NotificationCenter.stickersDidLoaded);
                sortStickers();
                updateStickerTabs();
                reloadStickersAdapter();
            }
        }
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.stickersGridAdapter != null) {
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.stickersDidLoaded);
        }
        if (this.pickerViewPopup != null && this.pickerViewPopup.isShowing()) {
            this.pickerViewPopup.dismiss();
        }
    }

    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.stickersDidLoaded) {
            updateStickerTabs();
            reloadStickersAdapter();
        }
    }
}
