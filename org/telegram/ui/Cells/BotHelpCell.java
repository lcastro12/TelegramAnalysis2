package org.telegram.ui.Cells;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.view.ViewCompat;
import android.text.Layout.Alignment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.C0553R;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.ui.Components.LinkPath;
import org.telegram.ui.Components.ResourceLoader;
import org.telegram.ui.Components.TypefaceSpan;
import org.telegram.ui.Components.URLSpanNoUnderline;

public class BotHelpCell extends View {
    private BotHelpCellDelegate delegate;
    private int height;
    private String oldText;
    private ClickableSpan pressedLink;
    private StaticLayout textLayout;
    private TextPaint textPaint = new TextPaint(1);
    private int textX;
    private int textY;
    private Paint urlPaint;
    private LinkPath urlPath = new LinkPath();
    private int width;

    public interface BotHelpCellDelegate {
        void didPressUrl(String str);
    }

    public BotHelpCell(Context context) {
        super(context);
        this.textPaint.setTextSize((float) AndroidUtilities.dp(16.0f));
        this.textPaint.setColor(ViewCompat.MEASURED_STATE_MASK);
        this.textPaint.linkColor = -13537377;
        this.urlPaint = new Paint();
        this.urlPaint.setColor(858877855);
    }

    public void setDelegate(BotHelpCellDelegate botHelpCellDelegate) {
        this.delegate = botHelpCellDelegate;
    }

    private void resetPressedLink() {
        if (this.pressedLink != null) {
            this.pressedLink = null;
        }
        invalidate();
    }

    public void setText(String text) {
        if (text == null || text.length() == 0) {
            setVisibility(8);
        } else if (text == null || this.oldText == null || !text.equals(this.oldText)) {
            this.oldText = text;
            setVisibility(0);
            if (AndroidUtilities.isTablet()) {
                this.width = (int) (((float) AndroidUtilities.getMinTabletSide()) * 0.7f);
            } else {
                this.width = (int) (((float) Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y)) * 0.7f);
            }
            SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
            String help = LocaleController.getString("BotInfoTitle", C0553R.string.BotInfoTitle);
            stringBuilder.append(help);
            stringBuilder.append("\n\n");
            stringBuilder.append(text);
            MessageObject.addLinks(stringBuilder);
            stringBuilder.setSpan(new TypefaceSpan(AndroidUtilities.getTypeface("fonts/rmedium.ttf")), 0, help.length(), 33);
            Emoji.replaceEmoji(stringBuilder, this.textPaint.getFontMetricsInt(), AndroidUtilities.dp(20.0f), false);
            this.textLayout = new StaticLayout(stringBuilder, this.textPaint, this.width, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            this.width = 0;
            this.height = this.textLayout.getHeight() + AndroidUtilities.dp(22.0f);
            int count = this.textLayout.getLineCount();
            for (int a = 0; a < count; a++) {
                this.width = (int) Math.ceil((double) Math.max((float) this.width, this.textLayout.getLineWidth(a) + this.textLayout.getLineLeft(a)));
            }
            this.width += AndroidUtilities.dp(22.0f);
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        boolean result = false;
        if (this.textLayout != null) {
            if (event.getAction() == 0 || (this.pressedLink != null && event.getAction() == 1)) {
                if (event.getAction() == 0) {
                    resetPressedLink();
                    try {
                        int x2 = (int) (x - ((float) this.textX));
                        int line = this.textLayout.getLineForVertical((int) (y - ((float) this.textY)));
                        int off = this.textLayout.getOffsetForHorizontal(line, (float) x2);
                        float left = this.textLayout.getLineLeft(line);
                        if (left > ((float) x2) || this.textLayout.getLineWidth(line) + left < ((float) x2)) {
                            resetPressedLink();
                        } else {
                            Spannable buffer = (Spannable) this.textLayout.getText();
                            ClickableSpan[] link = (ClickableSpan[]) buffer.getSpans(off, off, ClickableSpan.class);
                            if (link.length != 0) {
                                resetPressedLink();
                                this.pressedLink = link[0];
                                result = true;
                                try {
                                    int start = buffer.getSpanStart(this.pressedLink);
                                    this.urlPath.setCurrentLayout(this.textLayout, start);
                                    this.textLayout.getSelectionPath(start, buffer.getSpanEnd(this.pressedLink), this.urlPath);
                                } catch (Throwable e) {
                                    FileLog.m611e("tmessages", e);
                                }
                            } else {
                                resetPressedLink();
                            }
                        }
                    } catch (Throwable e2) {
                        resetPressedLink();
                        FileLog.m611e("tmessages", e2);
                    }
                } else if (this.pressedLink != null) {
                    try {
                        if (this.pressedLink instanceof URLSpanNoUnderline) {
                            String url = ((URLSpanNoUnderline) this.pressedLink).getURL();
                            if ((url.startsWith("@") || url.startsWith("#") || url.startsWith("/")) && this.delegate != null) {
                                this.delegate.didPressUrl(url);
                            }
                        } else {
                            this.pressedLink.onClick(this);
                        }
                    } catch (Throwable e22) {
                        FileLog.m611e("tmessages", e22);
                    }
                    resetPressedLink();
                    result = true;
                }
            } else if (event.getAction() == 3) {
                resetPressedLink();
            }
        }
        if (result || super.onTouchEvent(event)) {
            return true;
        }
        return false;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), 1073741824), this.height + AndroidUtilities.dp(8.0f));
    }

    protected void onDraw(Canvas canvas) {
        int x = (canvas.getWidth() - this.width) / 2;
        int y = AndroidUtilities.dp(4.0f);
        ResourceLoader.backgroundMediaDrawableIn.setBounds(x, y, this.width + x, this.height + y);
        ResourceLoader.backgroundMediaDrawableIn.draw(canvas);
        canvas.save();
        int dp = AndroidUtilities.dp(11.0f) + x;
        this.textX = dp;
        float f = (float) dp;
        int dp2 = AndroidUtilities.dp(11.0f) + y;
        this.textY = dp2;
        canvas.translate(f, (float) dp2);
        if (this.pressedLink != null) {
            canvas.drawPath(this.urlPath, this.urlPaint);
        }
        this.textLayout.draw(canvas);
        canvas.restore();
    }
}
