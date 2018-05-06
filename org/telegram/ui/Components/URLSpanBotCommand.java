package org.telegram.ui.Components;

import android.support.v4.view.ViewCompat;
import android.text.TextPaint;

public class URLSpanBotCommand extends URLSpanNoUnderline {
    public static boolean enabled = true;

    public URLSpanBotCommand(String url) {
        super(url);
    }

    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        ds.setColor(enabled ? -13537377 : ViewCompat.MEASURED_STATE_MASK);
        ds.setUnderlineText(false);
    }
}
