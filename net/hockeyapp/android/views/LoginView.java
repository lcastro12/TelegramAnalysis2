package net.hockeyapp.android.views;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build.VERSION;
import android.support.v4.view.ViewCompat;
import android.text.method.PasswordTransformationMethod;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import net.hockeyapp.android.Strings;

public class LoginView extends LinearLayout {
    public static final int EMAIL_INPUT_ID = 12291;
    public static final int HEADLINE_TEXT_ID = 12290;
    public static final int LOGIN_BUTTON_ID = 12293;
    public static final int PASSWORD_INPUT_ID = 12292;
    public static final int WRAPPER_BASE_ID = 12289;
    private LinearLayout wrapperBase;

    public LoginView(Context context) {
        this(context, 0);
    }

    public LoginView(Context context, int mode) {
        super(context);
        loadLayoutParams(context);
        loadWrapperBase(context);
        loadHeadlineTextView(context);
        loadEmailInput(context);
        loadPasswordInput(context);
        loadLoginButton(context);
    }

    private void loadLayoutParams(Context context) {
        LayoutParams params = new LayoutParams(-1, -2);
        setBackgroundColor(-1);
        setLayoutParams(params);
    }

    private void loadWrapperBase(Context context) {
        this.wrapperBase = new LinearLayout(context);
        this.wrapperBase.setId(12289);
        LayoutParams params = new LayoutParams(-1, -1);
        int padding = (int) TypedValue.applyDimension(1, 20.0f, getResources().getDisplayMetrics());
        params.gravity = 49;
        this.wrapperBase.setLayoutParams(params);
        this.wrapperBase.setPadding(padding, padding, padding, padding);
        this.wrapperBase.setOrientation(1);
        addView(this.wrapperBase);
    }

    private void loadHeadlineTextView(Context context) {
        TextView textView = new TextView(context);
        textView.setId(12290);
        LayoutParams params = new LayoutParams(-1, -2);
        params.setMargins(0, 0, 0, (int) TypedValue.applyDimension(1, BitmapDescriptorFactory.HUE_ORANGE, getResources().getDisplayMetrics()));
        textView.setLayoutParams(params);
        textView.setText(Strings.get(Strings.LOGIN_HEADLINE_TEXT_ID));
        textView.setTextColor(-7829368);
        textView.setTextSize(2, 18.0f);
        textView.setTypeface(null, 0);
        this.wrapperBase.addView(textView);
    }

    private void loadEmailInput(Context context) {
        EditText editText = new EditText(context);
        editText.setId(12291);
        LayoutParams params = new LayoutParams(-1, -2);
        params.setMargins(0, 0, 0, (int) TypedValue.applyDimension(1, BitmapDescriptorFactory.HUE_ORANGE, getResources().getDisplayMetrics()));
        editText.setLayoutParams(params);
        editText.setHint(Strings.get(Strings.LOGIN_EMAIL_INPUT_HINT_ID));
        editText.setImeOptions(5);
        editText.setInputType(33);
        editText.setTextColor(-7829368);
        editText.setTextSize(2, 15.0f);
        editText.setTypeface(null, 0);
        editText.setHintTextColor(-3355444);
        setEditTextBackground(context, editText);
        this.wrapperBase.addView(editText);
    }

    private void loadPasswordInput(Context context) {
        EditText editText = new EditText(context);
        editText.setId(12292);
        LayoutParams params = new LayoutParams(-1, -2);
        params.setMargins(0, 0, 0, (int) TypedValue.applyDimension(1, BitmapDescriptorFactory.HUE_ORANGE, getResources().getDisplayMetrics()));
        editText.setLayoutParams(params);
        editText.setHint(Strings.get(Strings.LOGIN_PASSWORD_INPUT_HINT_ID));
        editText.setImeOptions(5);
        editText.setInputType(128);
        editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
        editText.setTextColor(-7829368);
        editText.setTextSize(2, 15.0f);
        editText.setTypeface(null, 0);
        editText.setHintTextColor(-3355444);
        setEditTextBackground(context, editText);
        this.wrapperBase.addView(editText);
    }

    private void loadLoginButton(Context context) {
        Button button = new Button(context);
        button.setId(LOGIN_BUTTON_ID);
        LayoutParams params = new LayoutParams(-1, -2);
        params.setMargins(0, 0, 0, (int) TypedValue.applyDimension(1, BitmapDescriptorFactory.HUE_ORANGE, getResources().getDisplayMetrics()));
        button.setLayoutParams(params);
        button.setBackgroundDrawable(getButtonSelector());
        button.setText(Strings.get(Strings.LOGIN_LOGIN_BUTTON_TEXT_ID));
        button.setTextColor(-1);
        button.setTextSize(2, 15.0f);
        this.wrapperBase.addView(button);
    }

    private Drawable getButtonSelector() {
        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[]{-16842919}, new ColorDrawable(ViewCompat.MEASURED_STATE_MASK));
        drawable.addState(new int[]{-16842919, 16842908}, new ColorDrawable(-12303292));
        drawable.addState(new int[]{16842919}, new ColorDrawable(-7829368));
        return drawable;
    }

    private void setEditTextBackground(Context context, EditText editText) {
        if (VERSION.SDK_INT < 11) {
            editText.setBackgroundDrawable(getEditTextBackground(context));
        }
    }

    private Drawable getEditTextBackground(Context context) {
        int outerPadding = (int) (context.getResources().getDisplayMetrics().density * 10.0f);
        ShapeDrawable outerShape = new ShapeDrawable(new RectShape());
        Paint outerPaint = outerShape.getPaint();
        outerPaint.setColor(-1);
        outerPaint.setStyle(Style.FILL_AND_STROKE);
        outerPaint.setStrokeWidth(1.0f);
        outerShape.setPadding(outerPadding, outerPadding, outerPadding, outerPadding);
        int innerPadding = (int) (((double) context.getResources().getDisplayMetrics().density) * 1.5d);
        ShapeDrawable innerShape = new ShapeDrawable(new RectShape());
        Paint innerPaint = innerShape.getPaint();
        innerPaint.setColor(-12303292);
        innerPaint.setStyle(Style.FILL_AND_STROKE);
        innerPaint.setStrokeWidth(1.0f);
        innerShape.setPadding(0, 0, 0, innerPadding);
        return new LayerDrawable(new Drawable[]{innerShape, outerShape});
    }
}
