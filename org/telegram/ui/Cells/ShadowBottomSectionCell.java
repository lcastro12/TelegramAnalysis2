package org.telegram.ui.Cells;

import android.content.Context;
import android.view.View;
import android.view.View.MeasureSpec;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.C0553R;

public class ShadowBottomSectionCell extends View {
    public ShadowBottomSectionCell(Context context) {
        super(context);
        setBackgroundResource(C0553R.drawable.greydivider_bottom);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(6.0f), 1073741824));
    }
}
