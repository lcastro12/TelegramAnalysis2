package org.telegram.ui.Components;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.TLRPC.EncryptedChat;

public class IdenticonDrawable extends Drawable {
    private int[] colors = new int[]{-1, -2758925, -13805707, -13657655};
    private byte[] data;
    private Paint paint = new Paint();

    private int getBits(int bitOffset) {
        return (this.data[bitOffset / 8] >> (bitOffset % 8)) & 3;
    }

    public void setEncryptedChat(EncryptedChat encryptedChat) {
        this.data = encryptedChat.key_hash;
        if (this.data == null) {
            byte[] sha1 = Utilities.computeSHA1(encryptedChat.auth_key);
            byte[] bArr = new byte[16];
            this.data = bArr;
            encryptedChat.key_hash = bArr;
            System.arraycopy(sha1, 0, this.data, 0, this.data.length);
        }
        invalidateSelf();
    }

    public void draw(Canvas canvas) {
        if (this.data != null) {
            int bitPointer = 0;
            float rectSize = (float) Math.floor((double) (((float) Math.min(getBounds().width(), getBounds().height())) / 8.0f));
            float xOffset = Math.max(0.0f, (((float) getBounds().width()) - (8.0f * rectSize)) / 2.0f);
            float yOffset = Math.max(0.0f, (((float) getBounds().height()) - (8.0f * rectSize)) / 2.0f);
            for (int iy = 0; iy < 8; iy++) {
                for (int ix = 0; ix < 8; ix++) {
                    int byteValue = getBits(bitPointer);
                    bitPointer += 2;
                    this.paint.setColor(this.colors[Math.abs(byteValue) % 4]);
                    canvas.drawRect(xOffset + (((float) ix) * rectSize), (((float) iy) * rectSize) + yOffset, ((((float) ix) * rectSize) + xOffset) + rectSize, ((((float) iy) * rectSize) + rectSize) + yOffset, this.paint);
                }
            }
        }
    }

    public void setAlpha(int alpha) {
    }

    public void setColorFilter(ColorFilter cf) {
    }

    public int getOpacity() {
        return 0;
    }

    public int getIntrinsicWidth() {
        return AndroidUtilities.dp(32.0f);
    }

    public int getIntrinsicHeight() {
        return AndroidUtilities.dp(32.0f);
    }
}
