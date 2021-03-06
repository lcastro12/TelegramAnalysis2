package org.telegram.ui.Cells;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Build.VERSION;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;
import java.util.Iterator;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.query.StickersQuery;
import org.telegram.tgnet.TLRPC.Document;
import org.telegram.tgnet.TLRPC.DocumentAttribute;
import org.telegram.tgnet.TLRPC.TL_documentAttributeSticker;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;

public class StickerEmojiCell extends FrameLayout {
    private float alpha = 1.0f;
    private boolean changingAlpha;
    private TextView emojiTextView;
    private BackupImageView imageView;
    private AccelerateInterpolator interpolator = new AccelerateInterpolator(0.5f);
    private long lastUpdateTime;
    private float scale;
    private boolean scaled;
    private Document sticker;
    private long time = 0;

    public StickerEmojiCell(Context context) {
        super(context);
        this.imageView = new BackupImageView(context);
        this.imageView.setAspectFit(true);
        addView(this.imageView, LayoutHelper.createFrame(66, 66, 17));
        this.emojiTextView = new TextView(context);
        this.emojiTextView.setTextSize(1, 16.0f);
        addView(this.emojiTextView, LayoutHelper.createFrame(28, 28, 85));
    }

    public Document getSticker() {
        return this.sticker;
    }

    public void setSticker(Document document, boolean showEmoji) {
        if (document != null) {
            this.sticker = document;
            if (document.thumb != null) {
                this.imageView.setImage(document.thumb.location, null, "webp", null);
            }
            if (showEmoji) {
                boolean set = false;
                Iterator i$ = document.attributes.iterator();
                while (i$.hasNext()) {
                    DocumentAttribute attribute = (DocumentAttribute) i$.next();
                    if (attribute instanceof TL_documentAttributeSticker) {
                        if (attribute.alt != null && attribute.alt.length() > 0) {
                            this.emojiTextView.setText(Emoji.replaceEmoji(attribute.alt, this.emojiTextView.getPaint().getFontMetricsInt(), AndroidUtilities.dp(16.0f), false));
                            set = true;
                        }
                        if (!set) {
                            this.emojiTextView.setText(Emoji.replaceEmoji(StickersQuery.getEmojiForSticker(this.sticker.id), this.emojiTextView.getPaint().getFontMetricsInt(), AndroidUtilities.dp(16.0f), false));
                        }
                        this.emojiTextView.setVisibility(0);
                        return;
                    }
                }
                if (set) {
                    this.emojiTextView.setText(Emoji.replaceEmoji(StickersQuery.getEmojiForSticker(this.sticker.id), this.emojiTextView.getPaint().getFontMetricsInt(), AndroidUtilities.dp(16.0f), false));
                }
                this.emojiTextView.setVisibility(0);
                return;
            }
            this.emojiTextView.setVisibility(4);
        }
    }

    public void disable() {
        this.changingAlpha = true;
        this.alpha = 0.5f;
        this.time = 0;
        this.imageView.getImageReceiver().setAlpha(this.alpha);
        this.imageView.invalidate();
        this.lastUpdateTime = System.currentTimeMillis();
        invalidate();
    }

    public void setScaled(boolean value) {
        this.scaled = value;
        this.lastUpdateTime = System.currentTimeMillis();
        invalidate();
    }

    public boolean isDisabled() {
        return this.changingAlpha;
    }

    public boolean showingBitmap() {
        return this.imageView.getImageReceiver().getBitmap() != null;
    }

    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        boolean result = super.drawChild(canvas, child, drawingTime);
        if (child == this.imageView && (this.changingAlpha || ((this.scaled && this.scale != 0.8f) || !(this.scaled || this.scale == 1.0f)))) {
            long newTime = System.currentTimeMillis();
            long dt = newTime - this.lastUpdateTime;
            this.lastUpdateTime = newTime;
            if (this.changingAlpha) {
                this.time += dt;
                if (this.time > 1050) {
                    this.time = 1050;
                }
                this.alpha = 0.5f + (this.interpolator.getInterpolation(((float) this.time) / 1050.0f) * 0.5f);
                if (this.alpha >= 1.0f) {
                    this.changingAlpha = false;
                    this.alpha = 1.0f;
                }
                this.imageView.getImageReceiver().setAlpha(this.alpha);
            } else if (!this.scaled || this.scale == 0.8f) {
                this.scale += ((float) dt) / 400.0f;
                if (this.scale > 1.0f) {
                    this.scale = 1.0f;
                }
            } else {
                this.scale -= ((float) dt) / 400.0f;
                if (this.scale < 0.8f) {
                    this.scale = 0.8f;
                }
            }
            if (VERSION.SDK_INT >= 11) {
                this.imageView.setScaleX(this.scale);
                this.imageView.setScaleY(this.scale);
            }
            this.imageView.invalidate();
            invalidate();
        }
        return result;
    }
}
