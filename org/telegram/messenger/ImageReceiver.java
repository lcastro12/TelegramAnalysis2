package org.telegram.messenger;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.Document;
import org.telegram.tgnet.TLRPC.FileLocation;
import org.telegram.tgnet.TLRPC.TL_document;
import org.telegram.tgnet.TLRPC.TL_fileEncryptedLocation;
import org.telegram.tgnet.TLRPC.TL_fileLocation;

public class ImageReceiver implements NotificationCenterDelegate {
    private RectF bitmapRect;
    private BitmapShader bitmapShader;
    private boolean canceledLoading;
    private boolean centerRotation;
    private ColorFilter colorFilter;
    private byte crossfadeAlpha = (byte) 1;
    private boolean crossfadeWithThumb;
    private float currentAlpha;
    private boolean currentCacheOnly;
    private String currentExt;
    private String currentFilter;
    private String currentHttpUrl;
    private BitmapDrawable currentImage;
    private TLObject currentImageLocation;
    private String currentKey;
    private int currentSize;
    private BitmapDrawable currentThumb;
    private String currentThumbFilter;
    private String currentThumbKey;
    private FileLocation currentThumbLocation;
    private ImageReceiverDelegate delegate;
    private Rect drawRegion = new Rect();
    private boolean forcePreview;
    private int imageH;
    private int imageW;
    private int imageX;
    private int imageY;
    private boolean invalidateAll;
    private boolean isAspectFit;
    private boolean isPressed;
    private boolean isVisible = true;
    private long lastUpdateAlphaTime;
    private boolean needsQualityThumb;
    private int orientation;
    private float overrideAlpha = 1.0f;
    private MessageObject parentMessageObject;
    private View parentView;
    private Paint roundPaint;
    private int roundRadius;
    private RectF roundRect;
    private SetImageBackup setImageBackup;
    private Matrix shaderMatrix;
    private boolean shouldGenerateQualityThumb;
    private Drawable staticThumb;
    private Integer tag;
    private Integer thumbTag;

    public interface ImageReceiverDelegate {
        void didSetImage(ImageReceiver imageReceiver, boolean z, boolean z2);
    }

    private class SetImageBackup {
        public boolean cacheOnly;
        public String ext;
        public TLObject fileLocation;
        public String filter;
        public String httpUrl;
        public int size;
        public Drawable thumb;
        public String thumbFilter;
        public FileLocation thumbLocation;

        private SetImageBackup() {
        }
    }

    public ImageReceiver(View view) {
        this.parentView = view;
    }

    public void cancelLoadImage() {
        ImageLoader.getInstance().cancelLoadingForImageReceiver(this, 0);
        this.canceledLoading = true;
    }

    public void setImage(TLObject path, String filter, Drawable thumb, String ext, boolean cacheOnly) {
        setImage(path, null, filter, thumb, null, null, 0, ext, cacheOnly);
    }

    public void setImage(TLObject path, String filter, Drawable thumb, int size, String ext, boolean cacheOnly) {
        setImage(path, null, filter, thumb, null, null, size, ext, cacheOnly);
    }

    public void setImage(String httpUrl, String filter, Drawable thumb, String ext, int size) {
        setImage(null, httpUrl, filter, thumb, null, null, size, ext, true);
    }

    public void setImage(TLObject fileLocation, String filter, FileLocation thumbLocation, String thumbFilter, String ext, boolean cacheOnly) {
        setImage(fileLocation, null, filter, null, thumbLocation, thumbFilter, 0, ext, cacheOnly);
    }

    public void setImage(TLObject fileLocation, String filter, FileLocation thumbLocation, String thumbFilter, int size, String ext, boolean cacheOnly) {
        setImage(fileLocation, null, filter, null, thumbLocation, thumbFilter, size, ext, cacheOnly);
    }

    public void setImage(TLObject fileLocation, String httpUrl, String filter, Drawable thumb, FileLocation thumbLocation, String thumbFilter, int size, String ext, boolean cacheOnly) {
        if (this.setImageBackup != null) {
            this.setImageBackup.fileLocation = null;
            this.setImageBackup.httpUrl = null;
            this.setImageBackup.thumbLocation = null;
            this.setImageBackup.thumb = null;
        }
        ImageReceiverDelegate imageReceiverDelegate;
        boolean z;
        boolean z2;
        if (!(fileLocation == null && httpUrl == null && thumbLocation == null) && (fileLocation == null || (fileLocation instanceof TL_fileLocation) || (fileLocation instanceof TL_fileEncryptedLocation) || (fileLocation instanceof TL_document))) {
            if (!(thumbLocation instanceof TL_fileLocation)) {
                thumbLocation = null;
            }
            String key = null;
            if (fileLocation != null) {
                if (fileLocation instanceof FileLocation) {
                    FileLocation location = (FileLocation) fileLocation;
                    key = location.volume_id + "_" + location.local_id;
                } else {
                    Document location2 = (Document) fileLocation;
                    key = location2.dc_id + "_" + location2.id;
                }
            } else if (httpUrl != null) {
                key = Utilities.MD5(httpUrl);
            }
            if (!(key == null || filter == null)) {
                key = key + "@" + filter;
            }
            if (!(this.currentKey == null || key == null || !this.currentKey.equals(key))) {
                if (this.delegate != null) {
                    imageReceiverDelegate = this.delegate;
                    z = (this.currentImage == null && this.currentThumb == null && this.staticThumb == null) ? false : true;
                    if (this.currentImage == null) {
                        z2 = true;
                    } else {
                        z2 = false;
                    }
                    imageReceiverDelegate.didSetImage(this, z, z2);
                }
                if (!(this.canceledLoading || this.forcePreview)) {
                    return;
                }
            }
            String thumbKey = null;
            if (thumbLocation != null) {
                thumbKey = thumbLocation.volume_id + "_" + thumbLocation.local_id;
                if (thumbFilter != null) {
                    thumbKey = thumbKey + "@" + thumbFilter;
                }
            }
            recycleBitmap(key, false);
            recycleBitmap(thumbKey, true);
            this.currentThumbKey = thumbKey;
            this.currentKey = key;
            this.currentExt = ext;
            this.currentImageLocation = fileLocation;
            this.currentHttpUrl = httpUrl;
            this.currentFilter = filter;
            this.currentThumbFilter = thumbFilter;
            this.currentSize = size;
            this.currentCacheOnly = cacheOnly;
            this.currentThumbLocation = thumbLocation;
            this.staticThumb = thumb;
            this.bitmapShader = null;
            this.currentAlpha = 1.0f;
            if (this.delegate != null) {
                imageReceiverDelegate = this.delegate;
                z = (this.currentImage == null && this.currentThumb == null && this.staticThumb == null) ? false : true;
                imageReceiverDelegate.didSetImage(this, z, this.currentImage == null);
            }
            ImageLoader.getInstance().loadImageForImageReceiver(this);
            if (this.parentView == null) {
                return;
            }
            if (this.invalidateAll) {
                this.parentView.invalidate();
                return;
            } else {
                this.parentView.invalidate(this.imageX, this.imageY, this.imageX + this.imageW, this.imageY + this.imageH);
                return;
            }
        }
        recycleBitmap(null, false);
        recycleBitmap(null, true);
        this.currentKey = null;
        this.currentExt = ext;
        this.currentThumbKey = null;
        this.currentThumbFilter = null;
        this.currentImageLocation = null;
        this.currentHttpUrl = null;
        this.currentFilter = null;
        this.currentCacheOnly = false;
        this.staticThumb = thumb;
        this.currentAlpha = 1.0f;
        this.currentThumbLocation = null;
        this.currentSize = 0;
        this.currentImage = null;
        this.bitmapShader = null;
        ImageLoader.getInstance().cancelLoadingForImageReceiver(this, 0);
        if (this.parentView != null) {
            if (this.invalidateAll) {
                this.parentView.invalidate();
            } else {
                this.parentView.invalidate(this.imageX, this.imageY, this.imageX + this.imageW, this.imageY + this.imageH);
            }
        }
        if (this.delegate != null) {
            imageReceiverDelegate = this.delegate;
            if (this.currentImage == null && this.currentThumb == null && this.staticThumb == null) {
                z = false;
            } else {
                z = true;
            }
            if (this.currentImage == null) {
                z2 = true;
            } else {
                z2 = false;
            }
            imageReceiverDelegate.didSetImage(this, z, z2);
        }
    }

    public void setColorFilter(ColorFilter filter) {
        this.colorFilter = filter;
    }

    public void setDelegate(ImageReceiverDelegate delegate) {
        this.delegate = delegate;
    }

    public void setPressed(boolean value) {
        this.isPressed = value;
    }

    public boolean getPressed() {
        return this.isPressed;
    }

    public void setOrientation(int angle, boolean center) {
        this.orientation = angle;
        this.centerRotation = center;
    }

    public void setInvalidateAll(boolean value) {
        this.invalidateAll = value;
    }

    public int getOrientation() {
        return this.orientation;
    }

    public void setImageBitmap(Bitmap bitmap) {
        Drawable bitmapDrawable;
        if (bitmap != null) {
            bitmapDrawable = new BitmapDrawable(null, bitmap);
        } else {
            bitmapDrawable = null;
        }
        setImageBitmap(bitmapDrawable);
    }

    public void setImageBitmap(Drawable bitmap) {
        boolean z = false;
        ImageLoader.getInstance().cancelLoadingForImageReceiver(this, 0);
        recycleBitmap(null, false);
        recycleBitmap(null, true);
        this.staticThumb = bitmap;
        this.currentThumbLocation = null;
        this.currentKey = null;
        this.currentExt = null;
        this.currentThumbKey = null;
        this.currentImage = null;
        this.currentThumbFilter = null;
        this.currentImageLocation = null;
        this.currentHttpUrl = null;
        this.currentFilter = null;
        this.currentSize = 0;
        this.currentCacheOnly = false;
        this.bitmapShader = null;
        if (this.setImageBackup != null) {
            this.setImageBackup.fileLocation = null;
            this.setImageBackup.httpUrl = null;
            this.setImageBackup.thumbLocation = null;
            this.setImageBackup.thumb = null;
        }
        this.currentAlpha = 1.0f;
        if (this.delegate != null) {
            ImageReceiverDelegate imageReceiverDelegate = this.delegate;
            if (!(this.currentThumb == null && this.staticThumb == null)) {
                z = true;
            }
            imageReceiverDelegate.didSetImage(this, z, true);
        }
        if (this.parentView == null) {
            return;
        }
        if (this.invalidateAll) {
            this.parentView.invalidate();
        } else {
            this.parentView.invalidate(this.imageX, this.imageY, this.imageX + this.imageW, this.imageY + this.imageH);
        }
    }

    public void clearImage() {
        recycleBitmap(null, false);
        recycleBitmap(null, true);
        if (this.needsQualityThumb) {
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.messageThumbGenerated);
            ImageLoader.getInstance().cancelLoadingForImageReceiver(this, 0);
        }
    }

    public void onDetachedFromWindow() {
        if (!(this.currentImageLocation == null && this.currentHttpUrl == null && this.currentThumbLocation == null && this.staticThumb == null)) {
            if (this.setImageBackup == null) {
                this.setImageBackup = new SetImageBackup();
            }
            this.setImageBackup.fileLocation = this.currentImageLocation;
            this.setImageBackup.httpUrl = this.currentHttpUrl;
            this.setImageBackup.filter = this.currentFilter;
            this.setImageBackup.thumb = this.staticThumb;
            this.setImageBackup.thumbLocation = this.currentThumbLocation;
            this.setImageBackup.thumbFilter = this.currentThumbFilter;
            this.setImageBackup.size = this.currentSize;
            this.setImageBackup.ext = this.currentExt;
            this.setImageBackup.cacheOnly = this.currentCacheOnly;
        }
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.didReplacedPhotoInMemCache);
        clearImage();
    }

    public boolean onAttachedToWindow() {
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.didReplacedPhotoInMemCache);
        if (this.setImageBackup == null || (this.setImageBackup.fileLocation == null && this.setImageBackup.httpUrl == null && this.setImageBackup.thumbLocation == null && this.setImageBackup.thumb == null)) {
            return false;
        }
        setImage(this.setImageBackup.fileLocation, this.setImageBackup.httpUrl, this.setImageBackup.filter, this.setImageBackup.thumb, this.setImageBackup.thumbLocation, this.setImageBackup.thumbFilter, this.setImageBackup.size, this.setImageBackup.ext, this.setImageBackup.cacheOnly);
        return true;
    }

    private void drawDrawable(Canvas canvas, Drawable drawable, int alpha) {
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            Paint paint = bitmapDrawable.getPaint();
            boolean hasFilter = (paint == null || paint.getColorFilter() == null) ? false : true;
            if (hasFilter && !this.isPressed) {
                bitmapDrawable.setColorFilter(null);
            } else if (!hasFilter && this.isPressed) {
                bitmapDrawable.setColorFilter(new PorterDuffColorFilter(-2236963, Mode.MULTIPLY));
            }
            if (this.colorFilter != null) {
                bitmapDrawable.setColorFilter(this.colorFilter);
            }
            if (this.bitmapShader != null) {
                this.drawRegion.set(this.imageX, this.imageY, this.imageX + this.imageW, this.imageY + this.imageH);
                if (this.isVisible) {
                    this.roundRect.set(this.drawRegion);
                    this.shaderMatrix.reset();
                    this.shaderMatrix.setRectToRect(this.bitmapRect, this.roundRect, ScaleToFit.FILL);
                    this.bitmapShader.setLocalMatrix(this.shaderMatrix);
                    this.roundPaint.setAlpha(alpha);
                    canvas.drawRoundRect(this.roundRect, (float) this.roundRadius, (float) this.roundRadius, this.roundPaint);
                    return;
                }
                return;
            }
            int bitmapW;
            int bitmapH;
            if (this.orientation == 90 || this.orientation == 270) {
                bitmapW = bitmapDrawable.getIntrinsicHeight();
                bitmapH = bitmapDrawable.getIntrinsicWidth();
            } else {
                bitmapW = bitmapDrawable.getIntrinsicWidth();
                bitmapH = bitmapDrawable.getIntrinsicHeight();
            }
            float scaleW = ((float) bitmapW) / ((float) this.imageW);
            float scaleH = ((float) bitmapH) / ((float) this.imageH);
            if (this.isAspectFit) {
                float scale = Math.max(scaleW, scaleH);
                canvas.save();
                bitmapW = (int) (((float) bitmapW) / scale);
                bitmapH = (int) (((float) bitmapH) / scale);
                this.drawRegion.set(this.imageX + ((this.imageW - bitmapW) / 2), this.imageY + ((this.imageH - bitmapH) / 2), this.imageX + ((this.imageW + bitmapW) / 2), this.imageY + ((this.imageH + bitmapH) / 2));
                bitmapDrawable.setBounds(this.drawRegion);
                try {
                    bitmapDrawable.setAlpha(alpha);
                    bitmapDrawable.draw(canvas);
                } catch (Throwable e) {
                    if (bitmapDrawable == this.currentImage && this.currentKey != null) {
                        ImageLoader.getInstance().removeImage(this.currentKey);
                        this.currentKey = null;
                    } else if (bitmapDrawable == this.currentThumb && this.currentThumbKey != null) {
                        ImageLoader.getInstance().removeImage(this.currentThumbKey);
                        this.currentThumbKey = null;
                    }
                    setImage(this.currentImageLocation, this.currentHttpUrl, this.currentFilter, this.currentThumb, this.currentThumbLocation, this.currentThumbFilter, this.currentSize, this.currentExt, this.currentCacheOnly);
                    FileLog.m611e("tmessages", e);
                }
                canvas.restore();
                return;
            } else if (Math.abs(scaleW - scaleH) > 1.0E-5f) {
                canvas.save();
                canvas.clipRect(this.imageX, this.imageY, this.imageX + this.imageW, this.imageY + this.imageH);
                if (this.orientation != 0) {
                    if (this.centerRotation) {
                        canvas.rotate((float) this.orientation, (float) (this.imageW / 2), (float) (this.imageH / 2));
                    } else {
                        canvas.rotate((float) this.orientation, 0.0f, 0.0f);
                    }
                }
                if (((float) bitmapW) / scaleH > ((float) this.imageW)) {
                    bitmapW = (int) (((float) bitmapW) / scaleH);
                    this.drawRegion.set(this.imageX - ((bitmapW - this.imageW) / 2), this.imageY, this.imageX + ((this.imageW + bitmapW) / 2), this.imageY + this.imageH);
                } else {
                    bitmapH = (int) (((float) bitmapH) / scaleW);
                    this.drawRegion.set(this.imageX, this.imageY - ((bitmapH - this.imageH) / 2), this.imageX + this.imageW, this.imageY + ((this.imageH + bitmapH) / 2));
                }
                if (this.orientation == 90 || this.orientation == 270) {
                    width = (this.drawRegion.right - this.drawRegion.left) / 2;
                    height = (this.drawRegion.bottom - this.drawRegion.top) / 2;
                    centerX = (this.drawRegion.right + this.drawRegion.left) / 2;
                    centerY = (this.drawRegion.top + this.drawRegion.bottom) / 2;
                    bitmapDrawable.setBounds(centerX - height, centerY - width, centerX + height, centerY + width);
                } else {
                    bitmapDrawable.setBounds(this.drawRegion);
                }
                if (this.isVisible) {
                    try {
                        bitmapDrawable.setAlpha(alpha);
                        bitmapDrawable.draw(canvas);
                    } catch (Throwable e2) {
                        if (bitmapDrawable == this.currentImage && this.currentKey != null) {
                            ImageLoader.getInstance().removeImage(this.currentKey);
                            this.currentKey = null;
                        } else if (bitmapDrawable == this.currentThumb && this.currentThumbKey != null) {
                            ImageLoader.getInstance().removeImage(this.currentThumbKey);
                            this.currentThumbKey = null;
                        }
                        setImage(this.currentImageLocation, this.currentHttpUrl, this.currentFilter, this.currentThumb, this.currentThumbLocation, this.currentThumbFilter, this.currentSize, this.currentExt, this.currentCacheOnly);
                        FileLog.m611e("tmessages", e2);
                    }
                }
                canvas.restore();
                return;
            } else {
                canvas.save();
                if (this.orientation != 0) {
                    if (this.centerRotation) {
                        canvas.rotate((float) this.orientation, (float) (this.imageW / 2), (float) (this.imageH / 2));
                    } else {
                        canvas.rotate((float) this.orientation, 0.0f, 0.0f);
                    }
                }
                this.drawRegion.set(this.imageX, this.imageY, this.imageX + this.imageW, this.imageY + this.imageH);
                if (this.orientation == 90 || this.orientation == 270) {
                    width = (this.drawRegion.right - this.drawRegion.left) / 2;
                    height = (this.drawRegion.bottom - this.drawRegion.top) / 2;
                    centerX = (this.drawRegion.right + this.drawRegion.left) / 2;
                    centerY = (this.drawRegion.top + this.drawRegion.bottom) / 2;
                    bitmapDrawable.setBounds(centerX - height, centerY - width, centerX + height, centerY + width);
                } else {
                    bitmapDrawable.setBounds(this.drawRegion);
                }
                if (this.isVisible) {
                    try {
                        bitmapDrawable.setAlpha(alpha);
                        bitmapDrawable.draw(canvas);
                    } catch (Throwable e22) {
                        if (bitmapDrawable == this.currentImage && this.currentKey != null) {
                            ImageLoader.getInstance().removeImage(this.currentKey);
                            this.currentKey = null;
                        } else if (bitmapDrawable == this.currentThumb && this.currentThumbKey != null) {
                            ImageLoader.getInstance().removeImage(this.currentThumbKey);
                            this.currentThumbKey = null;
                        }
                        setImage(this.currentImageLocation, this.currentHttpUrl, this.currentFilter, this.currentThumb, this.currentThumbLocation, this.currentThumbFilter, this.currentSize, this.currentExt, this.currentCacheOnly);
                        FileLog.m611e("tmessages", e22);
                    }
                }
                canvas.restore();
                return;
            }
        }
        this.drawRegion.set(this.imageX, this.imageY, this.imageX + this.imageW, this.imageY + this.imageH);
        drawable.setBounds(this.drawRegion);
        if (this.isVisible) {
            try {
                drawable.setAlpha(alpha);
                drawable.draw(canvas);
            } catch (Throwable e222) {
                FileLog.m611e("tmessages", e222);
            }
        }
    }

    private void checkAlphaAnimation() {
        if (this.currentAlpha != 1.0f) {
            this.currentAlpha += ((float) (System.currentTimeMillis() - this.lastUpdateAlphaTime)) / 150.0f;
            if (this.currentAlpha > 1.0f) {
                this.currentAlpha = 1.0f;
            }
            this.lastUpdateAlphaTime = System.currentTimeMillis();
            if (this.parentView == null) {
                return;
            }
            if (this.invalidateAll) {
                this.parentView.invalidate();
            } else {
                this.parentView.invalidate(this.imageX, this.imageY, this.imageX + this.imageW, this.imageY + this.imageH);
            }
        }
    }

    public boolean draw(Canvas canvas) {
        BitmapDrawable bitmapDrawable = null;
        try {
            if (!this.forcePreview && this.currentImage != null) {
                bitmapDrawable = this.currentImage;
            } else if (this.staticThumb instanceof BitmapDrawable) {
                bitmapDrawable = (BitmapDrawable) this.staticThumb;
            } else if (this.currentThumb != null) {
                bitmapDrawable = this.currentThumb;
            }
            if (bitmapDrawable != null) {
                if (this.crossfadeAlpha != (byte) 0) {
                    if (this.crossfadeWithThumb && this.currentAlpha != 1.0f) {
                        Drawable thumbDrawable = null;
                        if (bitmapDrawable == this.currentImage) {
                            if (this.staticThumb != null) {
                                thumbDrawable = this.staticThumb;
                            } else if (this.currentThumb != null) {
                                thumbDrawable = this.currentThumb;
                            }
                        } else if (bitmapDrawable == this.currentThumb && this.staticThumb != null) {
                            thumbDrawable = this.staticThumb;
                        }
                        if (thumbDrawable != null) {
                            drawDrawable(canvas, thumbDrawable, (int) (this.overrideAlpha * 255.0f));
                        }
                    }
                    drawDrawable(canvas, bitmapDrawable, (int) ((this.overrideAlpha * this.currentAlpha) * 255.0f));
                } else {
                    drawDrawable(canvas, bitmapDrawable, (int) (this.overrideAlpha * 255.0f));
                }
                checkAlphaAnimation();
                return true;
            }
            if (this.staticThumb != null) {
                drawDrawable(canvas, this.staticThumb, 255);
                checkAlphaAnimation();
                return true;
            }
            return false;
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
        }
    }

    public Bitmap getBitmap() {
        if (this.currentImage != null) {
            return this.currentImage.getBitmap();
        }
        if (this.currentThumb != null) {
            return this.currentThumb.getBitmap();
        }
        if (this.staticThumb instanceof BitmapDrawable) {
            return ((BitmapDrawable) this.staticThumb).getBitmap();
        }
        return null;
    }

    public int getBitmapWidth() {
        Bitmap bitmap = getBitmap();
        return (this.orientation == 0 || this.orientation == 180) ? bitmap.getWidth() : bitmap.getHeight();
    }

    public int getBitmapHeight() {
        Bitmap bitmap = getBitmap();
        return (this.orientation == 0 || this.orientation == 180) ? bitmap.getHeight() : bitmap.getWidth();
    }

    public void setVisible(boolean value, boolean invalidate) {
        if (this.isVisible != value) {
            this.isVisible = value;
            if (invalidate && this.parentView != null) {
                if (this.invalidateAll) {
                    this.parentView.invalidate();
                } else {
                    this.parentView.invalidate(this.imageX, this.imageY, this.imageX + this.imageW, this.imageY + this.imageH);
                }
            }
        }
    }

    public boolean getVisible() {
        return this.isVisible;
    }

    public void setAlpha(float value) {
        this.overrideAlpha = value;
    }

    public void setCrossfadeAlpha(byte value) {
        this.crossfadeAlpha = value;
    }

    public boolean hasImage() {
        return (this.currentImage == null && this.currentThumb == null && this.currentKey == null && this.currentHttpUrl == null && this.staticThumb == null) ? false : true;
    }

    public void setAspectFit(boolean value) {
        this.isAspectFit = value;
    }

    public void setParentView(View view) {
        this.parentView = view;
    }

    public void setImageCoords(int x, int y, int width, int height) {
        this.imageX = x;
        this.imageY = y;
        this.imageW = width;
        this.imageH = height;
    }

    public int getImageX() {
        return this.imageX;
    }

    public int getImageX2() {
        return this.imageX + this.imageW;
    }

    public int getImageY() {
        return this.imageY;
    }

    public int getImageY2() {
        return this.imageY + this.imageH;
    }

    public int getImageWidth() {
        return this.imageW;
    }

    public int getImageHeight() {
        return this.imageH;
    }

    public String getExt() {
        return this.currentExt;
    }

    public boolean isInsideImage(float x, float y) {
        return x >= ((float) this.imageX) && x <= ((float) (this.imageX + this.imageW)) && y >= ((float) this.imageY) && y <= ((float) (this.imageY + this.imageH));
    }

    public Rect getDrawRegion() {
        return this.drawRegion;
    }

    public String getFilter() {
        return this.currentFilter;
    }

    public String getThumbFilter() {
        return this.currentThumbFilter;
    }

    public String getKey() {
        return this.currentKey;
    }

    public String getThumbKey() {
        return this.currentThumbKey;
    }

    public int getSize() {
        return this.currentSize;
    }

    public TLObject getImageLocation() {
        return this.currentImageLocation;
    }

    public FileLocation getThumbLocation() {
        return this.currentThumbLocation;
    }

    public String getHttpImageLocation() {
        return this.currentHttpUrl;
    }

    public boolean getCacheOnly() {
        return this.currentCacheOnly;
    }

    public void setForcePreview(boolean value) {
        this.forcePreview = value;
    }

    public boolean isForcePreview() {
        return this.forcePreview;
    }

    public void setRoundRadius(int value) {
        this.roundRadius = value;
        if (this.roundRadius == 0) {
            this.roundPaint = null;
            this.roundRect = null;
            this.shaderMatrix = null;
            this.bitmapRect = null;
        } else if (this.roundPaint == null) {
            this.roundPaint = new Paint(1);
            this.roundRect = new RectF();
            this.shaderMatrix = new Matrix();
            this.bitmapRect = new RectF();
        }
    }

    public int getRoundRadius() {
        return this.roundRadius;
    }

    public void setParentMessageObject(MessageObject messageObject) {
        this.parentMessageObject = messageObject;
    }

    public MessageObject getParentMessageObject() {
        return this.parentMessageObject;
    }

    public void setNeedsQualityThumb(boolean value) {
        this.needsQualityThumb = value;
        if (this.needsQualityThumb) {
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.messageThumbGenerated);
        } else {
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.messageThumbGenerated);
        }
    }

    public boolean isNeedsQualityThumb() {
        return this.needsQualityThumb;
    }

    public void setShouldGenerateQualityThumb(boolean value) {
        this.shouldGenerateQualityThumb = value;
    }

    public boolean isShouldGenerateQualityThumb() {
        return this.shouldGenerateQualityThumb;
    }

    protected Integer getTag(boolean thumb) {
        if (thumb) {
            return this.thumbTag;
        }
        return this.tag;
    }

    protected void setTag(Integer value, boolean thumb) {
        if (thumb) {
            this.thumbTag = value;
        } else {
            this.tag = value;
        }
    }

    protected void setImageBitmapByKey(BitmapDrawable bitmap, String key, boolean thumb, boolean memCache) {
        boolean z = true;
        if (bitmap != null && key != null) {
            boolean z2;
            if (thumb) {
                if (this.currentThumb == null && (this.currentImage == null || this.forcePreview)) {
                    if (this.currentThumbKey != null && key.equals(this.currentThumbKey)) {
                        ImageLoader.getInstance().incrementUseCount(this.currentThumbKey);
                        this.currentThumb = bitmap;
                        if (memCache || this.crossfadeAlpha == (byte) 2) {
                            this.currentAlpha = 1.0f;
                        } else {
                            this.currentAlpha = 0.0f;
                            this.lastUpdateAlphaTime = System.currentTimeMillis();
                            z2 = this.staticThumb != null && this.currentKey == null;
                            this.crossfadeWithThumb = z2;
                        }
                        if (!((this.staticThumb instanceof BitmapDrawable) || this.parentView == null)) {
                            if (this.invalidateAll) {
                                this.parentView.invalidate();
                            } else {
                                this.parentView.invalidate(this.imageX, this.imageY, this.imageX + this.imageW, this.imageY + this.imageH);
                            }
                        }
                    } else {
                        return;
                    }
                }
            } else if (this.currentKey != null && key.equals(this.currentKey)) {
                ImageLoader.getInstance().incrementUseCount(this.currentKey);
                this.currentImage = bitmap;
                if (this.roundRadius != 0) {
                    Bitmap object = bitmap.getBitmap();
                    this.bitmapShader = new BitmapShader(object, TileMode.CLAMP, TileMode.CLAMP);
                    this.roundPaint.setShader(this.bitmapShader);
                    this.bitmapRect.set(0.0f, 0.0f, (float) object.getWidth(), (float) object.getHeight());
                }
                if (memCache || this.forcePreview) {
                    this.currentAlpha = 1.0f;
                } else if ((this.currentThumb == null && this.staticThumb == null) || this.currentAlpha == 1.0f) {
                    this.currentAlpha = 0.0f;
                    this.lastUpdateAlphaTime = System.currentTimeMillis();
                    z2 = (this.currentThumb == null && this.staticThumb == null) ? false : true;
                    this.crossfadeWithThumb = z2;
                }
                if (this.parentView != null) {
                    if (this.invalidateAll) {
                        this.parentView.invalidate();
                    } else {
                        this.parentView.invalidate(this.imageX, this.imageY, this.imageX + this.imageW, this.imageY + this.imageH);
                    }
                }
            } else {
                return;
            }
            if (this.delegate != null) {
                ImageReceiverDelegate imageReceiverDelegate = this.delegate;
                if (this.currentImage == null && this.currentThumb == null && this.staticThumb == null) {
                    z2 = false;
                } else {
                    z2 = true;
                }
                if (this.currentImage != null) {
                    z = false;
                }
                imageReceiverDelegate.didSetImage(this, z2, z);
            }
        }
    }

    private void recycleBitmap(String newKey, boolean thumb) {
        String key;
        BitmapDrawable image;
        if (thumb) {
            key = this.currentThumbKey;
            image = this.currentThumb;
        } else {
            key = this.currentKey;
            image = this.currentImage;
        }
        BitmapDrawable newBitmap = null;
        if (newKey != null) {
            newBitmap = ImageLoader.getInstance().getImageFromMemory(newKey);
        }
        if (!(key == null || image == newBitmap || image == null)) {
            Bitmap bitmap = image.getBitmap();
            boolean canDelete = ImageLoader.getInstance().decrementUseCount(key);
            if (!ImageLoader.getInstance().isInCache(key)) {
                if (ImageLoader.getInstance().runtimeHack != null) {
                    ImageLoader.getInstance().runtimeHack.trackAlloc((long) (bitmap.getRowBytes() * bitmap.getHeight()));
                }
                if (canDelete) {
                    bitmap.recycle();
                }
            }
        }
        if (thumb) {
            this.currentThumb = null;
            this.currentThumbKey = null;
            return;
        }
        this.currentImage = null;
        this.currentKey = null;
    }

    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.messageThumbGenerated) {
            String key = args[1];
            if (this.currentThumbKey != null && this.currentThumbKey.equals(key)) {
                if (this.currentThumb == null) {
                    ImageLoader.getInstance().incrementUseCount(this.currentThumbKey);
                }
                this.currentThumb = (BitmapDrawable) args[0];
                if (this.staticThumb instanceof BitmapDrawable) {
                    this.staticThumb = null;
                }
                if (this.parentView == null) {
                    return;
                }
                if (this.invalidateAll) {
                    this.parentView.invalidate();
                } else {
                    this.parentView.invalidate(this.imageX, this.imageY, this.imageX + this.imageW, this.imageY + this.imageH);
                }
            }
        } else if (id == NotificationCenter.didReplacedPhotoInMemCache) {
            String oldKey = args[0];
            if (this.currentKey != null && this.currentKey.equals(oldKey)) {
                this.currentKey = (String) args[1];
                this.currentImageLocation = (FileLocation) args[2];
            }
            if (this.currentThumbKey != null && this.currentThumbKey.equals(oldKey)) {
                this.currentThumbKey = (String) args[1];
                this.currentThumbLocation = (FileLocation) args[2];
            }
            if (this.setImageBackup != null) {
                if (this.currentKey != null && this.currentKey.equals(oldKey)) {
                    this.currentKey = (String) args[1];
                    this.currentImageLocation = (FileLocation) args[2];
                }
                if (this.currentThumbKey != null && this.currentThumbKey.equals(oldKey)) {
                    this.currentThumbKey = (String) args[1];
                    this.currentThumbLocation = (FileLocation) args[2];
                }
            }
        }
    }
}
