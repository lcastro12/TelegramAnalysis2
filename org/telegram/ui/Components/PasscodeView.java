package org.telegram.ui.Components;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Vibrator;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat.AuthenticationCallback;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat.AuthenticationResult;
import android.support.v4.os.CancellationSignal;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import java.util.ArrayList;
import java.util.Locale;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.AnimationCompat.AnimatorListenerAdapterProxy;
import org.telegram.messenger.AnimationCompat.AnimatorSetProxy;
import org.telegram.messenger.AnimationCompat.ObjectAnimatorProxy;
import org.telegram.messenger.AnimationCompat.ViewProxy;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.C0553R;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;

public class PasscodeView extends FrameLayout {
    private static final int id_fingerprint_imageview = 1001;
    private static final int id_fingerprint_textview = 1000;
    private Drawable backgroundDrawable;
    private FrameLayout backgroundFrameLayout;
    private CancellationSignal cancellationSignal;
    private ImageView checkImage;
    private PasscodeViewDelegate delegate;
    private ImageView eraseView;
    private AlertDialog fingerprintDialog;
    private ImageView fingerprintImageView;
    private TextView fingerprintStatusTextView;
    private int keyboardHeight = 0;
    private ArrayList<TextView> lettersTextViews;
    private ArrayList<FrameLayout> numberFrameLayouts;
    private ArrayList<TextView> numberTextViews;
    private FrameLayout numbersFrameLayout;
    private TextView passcodeTextView;
    private EditText passwordEditText;
    private AnimatingTextView passwordEditText2;
    private FrameLayout passwordFrameLayout;
    private Rect rect = new Rect();
    private boolean selfCancelled;

    class C09261 implements OnEditorActionListener {
        C09261() {
        }

        public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
            if (i != 6) {
                return false;
            }
            PasscodeView.this.processDone(false);
            return true;
        }
    }

    class C09272 implements TextWatcher {
        C09272() {
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        public void afterTextChanged(Editable s) {
            if (PasscodeView.this.passwordEditText.length() == 4 && UserConfig.passcodeType == 0) {
                PasscodeView.this.processDone(false);
            }
        }
    }

    class C09283 implements OnCreateContextMenuListener {
        C09283() {
        }

        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
            menu.clear();
        }
    }

    class C09294 implements Callback {
        C09294() {
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        public void onDestroyActionMode(ActionMode mode) {
        }

        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return false;
        }
    }

    class C09305 implements OnClickListener {
        C09305() {
        }

        public void onClick(View v) {
            PasscodeView.this.processDone(false);
        }
    }

    class C09316 implements OnLongClickListener {
        C09316() {
        }

        public boolean onLongClick(View v) {
            PasscodeView.this.passwordEditText.setText("");
            PasscodeView.this.passwordEditText2.eraseAllCharacters(true);
            return true;
        }
    }

    class C09327 implements OnClickListener {
        C09327() {
        }

        public void onClick(View v) {
            switch (((Integer) v.getTag()).intValue()) {
                case 0:
                    PasscodeView.this.passwordEditText2.appendCharacter("0");
                    break;
                case 1:
                    PasscodeView.this.passwordEditText2.appendCharacter("1");
                    break;
                case 2:
                    PasscodeView.this.passwordEditText2.appendCharacter("2");
                    break;
                case 3:
                    PasscodeView.this.passwordEditText2.appendCharacter("3");
                    break;
                case 4:
                    PasscodeView.this.passwordEditText2.appendCharacter("4");
                    break;
                case 5:
                    PasscodeView.this.passwordEditText2.appendCharacter("5");
                    break;
                case 6:
                    PasscodeView.this.passwordEditText2.appendCharacter("6");
                    break;
                case 7:
                    PasscodeView.this.passwordEditText2.appendCharacter("7");
                    break;
                case 8:
                    PasscodeView.this.passwordEditText2.appendCharacter("8");
                    break;
                case 9:
                    PasscodeView.this.passwordEditText2.appendCharacter("9");
                    break;
                case 10:
                    PasscodeView.this.passwordEditText2.eraseLastCharacter();
                    break;
            }
            if (PasscodeView.this.passwordEditText2.lenght() == 4) {
                PasscodeView.this.processDone(false);
            }
        }
    }

    private class AnimatingTextView extends FrameLayout {
        private String DOT = "•";
        private ArrayList<TextView> characterTextViews = new ArrayList(4);
        private AnimatorSetProxy currentAnimation;
        private Runnable dotRunnable;
        private ArrayList<TextView> dotTextViews = new ArrayList(4);
        private StringBuilder stringBuilder = new StringBuilder(4);

        class C15652 extends AnimatorListenerAdapterProxy {
            C15652() {
            }

            public void onAnimationEnd(Object animation) {
                if (AnimatingTextView.this.currentAnimation != null && AnimatingTextView.this.currentAnimation.equals(animation)) {
                    AnimatingTextView.this.currentAnimation = null;
                }
            }
        }

        class C15663 extends AnimatorListenerAdapterProxy {
            C15663() {
            }

            public void onAnimationEnd(Object animation) {
                if (AnimatingTextView.this.currentAnimation != null && AnimatingTextView.this.currentAnimation.equals(animation)) {
                    AnimatingTextView.this.currentAnimation = null;
                }
            }
        }

        class C15674 extends AnimatorListenerAdapterProxy {
            C15674() {
            }

            public void onAnimationEnd(Object animation) {
                if (AnimatingTextView.this.currentAnimation != null && AnimatingTextView.this.currentAnimation.equals(animation)) {
                    AnimatingTextView.this.currentAnimation = null;
                }
            }
        }

        public AnimatingTextView(Context context) {
            super(context);
            for (int a = 0; a < 4; a++) {
                TextView textView = new TextView(context);
                textView.setTextColor(-1);
                textView.setTextSize(1, 36.0f);
                textView.setGravity(17);
                ViewProxy.setAlpha(textView, 0.0f);
                ViewProxy.setPivotX(textView, (float) AndroidUtilities.dp(25.0f));
                ViewProxy.setPivotY(textView, (float) AndroidUtilities.dp(25.0f));
                addView(textView);
                LayoutParams layoutParams = (LayoutParams) textView.getLayoutParams();
                layoutParams.width = AndroidUtilities.dp(50.0f);
                layoutParams.height = AndroidUtilities.dp(50.0f);
                layoutParams.gravity = 51;
                textView.setLayoutParams(layoutParams);
                this.characterTextViews.add(textView);
                textView = new TextView(context);
                textView.setTextColor(-1);
                textView.setTextSize(1, 36.0f);
                textView.setGravity(17);
                ViewProxy.setAlpha(textView, 0.0f);
                textView.setText(this.DOT);
                ViewProxy.setPivotX(textView, (float) AndroidUtilities.dp(25.0f));
                ViewProxy.setPivotY(textView, (float) AndroidUtilities.dp(25.0f));
                addView(textView);
                layoutParams = (LayoutParams) textView.getLayoutParams();
                layoutParams.width = AndroidUtilities.dp(50.0f);
                layoutParams.height = AndroidUtilities.dp(50.0f);
                layoutParams.gravity = 51;
                textView.setLayoutParams(layoutParams);
                this.dotTextViews.add(textView);
            }
        }

        private int getXForTextView(int pos) {
            return (((getMeasuredWidth() - (this.stringBuilder.length() * AndroidUtilities.dp(BitmapDescriptorFactory.HUE_ORANGE))) / 2) + (AndroidUtilities.dp(BitmapDescriptorFactory.HUE_ORANGE) * pos)) - AndroidUtilities.dp(10.0f);
        }

        public void appendCharacter(String c) {
            if (this.stringBuilder.length() != 4) {
                int a;
                try {
                    performHapticFeedback(3);
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
                ArrayList animators = new ArrayList();
                final int newPos = this.stringBuilder.length();
                this.stringBuilder.append(c);
                TextView textView = (TextView) this.characterTextViews.get(newPos);
                textView.setText(c);
                ViewProxy.setTranslationX(textView, (float) getXForTextView(newPos));
                animators.add(ObjectAnimatorProxy.ofFloat(textView, "scaleX", 0.0f, 1.0f));
                animators.add(ObjectAnimatorProxy.ofFloat(textView, "scaleY", 0.0f, 1.0f));
                animators.add(ObjectAnimatorProxy.ofFloat(textView, "alpha", 0.0f, 1.0f));
                animators.add(ObjectAnimatorProxy.ofFloat(textView, "translationY", (float) AndroidUtilities.dp(20.0f), 0.0f));
                textView = (TextView) this.dotTextViews.get(newPos);
                ViewProxy.setTranslationX(textView, (float) getXForTextView(newPos));
                ViewProxy.setAlpha(textView, 0.0f);
                animators.add(ObjectAnimatorProxy.ofFloat(textView, "scaleX", 0.0f, 1.0f));
                animators.add(ObjectAnimatorProxy.ofFloat(textView, "scaleY", 0.0f, 1.0f));
                animators.add(ObjectAnimatorProxy.ofFloat(textView, "translationY", (float) AndroidUtilities.dp(20.0f), 0.0f));
                for (a = newPos + 1; a < 4; a++) {
                    textView = (TextView) this.characterTextViews.get(a);
                    if (ViewProxy.getAlpha(textView) != 0.0f) {
                        animators.add(ObjectAnimatorProxy.ofFloat(textView, "scaleX", 0.0f));
                        animators.add(ObjectAnimatorProxy.ofFloat(textView, "scaleY", 0.0f));
                        animators.add(ObjectAnimatorProxy.ofFloat(textView, "alpha", 0.0f));
                    }
                    textView = (TextView) this.dotTextViews.get(a);
                    if (ViewProxy.getAlpha(textView) != 0.0f) {
                        animators.add(ObjectAnimatorProxy.ofFloat(textView, "scaleX", 0.0f));
                        animators.add(ObjectAnimatorProxy.ofFloat(textView, "scaleY", 0.0f));
                        animators.add(ObjectAnimatorProxy.ofFloat(textView, "alpha", 0.0f));
                    }
                }
                if (this.dotRunnable != null) {
                    AndroidUtilities.cancelRunOnUIThread(this.dotRunnable);
                }
                this.dotRunnable = new Runnable() {

                    class C15641 extends AnimatorListenerAdapterProxy {
                        C15641() {
                        }

                        public void onAnimationEnd(Object animation) {
                            if (AnimatingTextView.this.currentAnimation != null && AnimatingTextView.this.currentAnimation.equals(animation)) {
                                AnimatingTextView.this.currentAnimation = null;
                            }
                        }
                    }

                    public void run() {
                        if (AnimatingTextView.this.dotRunnable == this) {
                            ArrayList animators = new ArrayList();
                            TextView textView = (TextView) AnimatingTextView.this.characterTextViews.get(newPos);
                            animators.add(ObjectAnimatorProxy.ofFloat(textView, "scaleX", 0.0f));
                            animators.add(ObjectAnimatorProxy.ofFloat(textView, "scaleY", 0.0f));
                            animators.add(ObjectAnimatorProxy.ofFloat(textView, "alpha", 0.0f));
                            textView = (TextView) AnimatingTextView.this.dotTextViews.get(newPos);
                            animators.add(ObjectAnimatorProxy.ofFloat(textView, "scaleX", 1.0f));
                            animators.add(ObjectAnimatorProxy.ofFloat(textView, "scaleY", 1.0f));
                            animators.add(ObjectAnimatorProxy.ofFloat(textView, "alpha", 1.0f));
                            AnimatingTextView.this.currentAnimation = new AnimatorSetProxy();
                            AnimatingTextView.this.currentAnimation.setDuration(150);
                            AnimatingTextView.this.currentAnimation.playTogether(animators);
                            AnimatingTextView.this.currentAnimation.addListener(new C15641());
                            AnimatingTextView.this.currentAnimation.start();
                        }
                    }
                };
                AndroidUtilities.runOnUIThread(this.dotRunnable, 1500);
                for (a = 0; a < newPos; a++) {
                    textView = (TextView) this.characterTextViews.get(a);
                    animators.add(ObjectAnimatorProxy.ofFloat(textView, "translationX", (float) getXForTextView(a)));
                    animators.add(ObjectAnimatorProxy.ofFloat(textView, "scaleX", 0.0f));
                    animators.add(ObjectAnimatorProxy.ofFloat(textView, "scaleY", 0.0f));
                    animators.add(ObjectAnimatorProxy.ofFloat(textView, "alpha", 0.0f));
                    animators.add(ObjectAnimatorProxy.ofFloat(textView, "translationY", 0.0f));
                    textView = (TextView) this.dotTextViews.get(a);
                    animators.add(ObjectAnimatorProxy.ofFloat(textView, "translationX", (float) getXForTextView(a)));
                    animators.add(ObjectAnimatorProxy.ofFloat(textView, "scaleX", 1.0f));
                    animators.add(ObjectAnimatorProxy.ofFloat(textView, "scaleY", 1.0f));
                    animators.add(ObjectAnimatorProxy.ofFloat(textView, "alpha", 1.0f));
                    animators.add(ObjectAnimatorProxy.ofFloat(textView, "translationY", 0.0f));
                }
                if (this.currentAnimation != null) {
                    this.currentAnimation.cancel();
                }
                this.currentAnimation = new AnimatorSetProxy();
                this.currentAnimation.setDuration(150);
                this.currentAnimation.playTogether(animators);
                this.currentAnimation.addListener(new C15652());
                this.currentAnimation.start();
            }
        }

        public String getString() {
            return this.stringBuilder.toString();
        }

        public int lenght() {
            return this.stringBuilder.length();
        }

        public void eraseLastCharacter() {
            if (this.stringBuilder.length() != 0) {
                int a;
                try {
                    performHapticFeedback(3);
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
                ArrayList animators = new ArrayList();
                int deletingPos = this.stringBuilder.length() - 1;
                if (deletingPos != 0) {
                    this.stringBuilder.deleteCharAt(deletingPos);
                }
                for (a = deletingPos; a < 4; a++) {
                    TextView textView = (TextView) this.characterTextViews.get(a);
                    if (ViewProxy.getAlpha(textView) != 0.0f) {
                        animators.add(ObjectAnimatorProxy.ofFloat(textView, "scaleX", 0.0f));
                        animators.add(ObjectAnimatorProxy.ofFloat(textView, "scaleY", 0.0f));
                        animators.add(ObjectAnimatorProxy.ofFloat(textView, "alpha", 0.0f));
                        animators.add(ObjectAnimatorProxy.ofFloat(textView, "translationY", 0.0f));
                        animators.add(ObjectAnimatorProxy.ofFloat(textView, "translationX", (float) getXForTextView(a)));
                    }
                    textView = (TextView) this.dotTextViews.get(a);
                    if (ViewProxy.getAlpha(textView) != 0.0f) {
                        animators.add(ObjectAnimatorProxy.ofFloat(textView, "scaleX", 0.0f));
                        animators.add(ObjectAnimatorProxy.ofFloat(textView, "scaleY", 0.0f));
                        animators.add(ObjectAnimatorProxy.ofFloat(textView, "alpha", 0.0f));
                        animators.add(ObjectAnimatorProxy.ofFloat(textView, "translationY", 0.0f));
                        animators.add(ObjectAnimatorProxy.ofFloat(textView, "translationX", (float) getXForTextView(a)));
                    }
                }
                if (deletingPos == 0) {
                    this.stringBuilder.deleteCharAt(deletingPos);
                }
                for (a = 0; a < deletingPos; a++) {
                    animators.add(ObjectAnimatorProxy.ofFloat((TextView) this.characterTextViews.get(a), "translationX", (float) getXForTextView(a)));
                    animators.add(ObjectAnimatorProxy.ofFloat((TextView) this.dotTextViews.get(a), "translationX", (float) getXForTextView(a)));
                }
                if (this.dotRunnable != null) {
                    AndroidUtilities.cancelRunOnUIThread(this.dotRunnable);
                    this.dotRunnable = null;
                }
                if (this.currentAnimation != null) {
                    this.currentAnimation.cancel();
                }
                this.currentAnimation = new AnimatorSetProxy();
                this.currentAnimation.setDuration(150);
                this.currentAnimation.playTogether(animators);
                this.currentAnimation.addListener(new C15663());
                this.currentAnimation.start();
            }
        }

        private void eraseAllCharacters(boolean animated) {
            if (this.stringBuilder.length() != 0) {
                if (this.dotRunnable != null) {
                    AndroidUtilities.cancelRunOnUIThread(this.dotRunnable);
                    this.dotRunnable = null;
                }
                if (this.currentAnimation != null) {
                    this.currentAnimation.cancel();
                    this.currentAnimation = null;
                }
                this.stringBuilder.delete(0, this.stringBuilder.length());
                int a;
                if (animated) {
                    ArrayList animators = new ArrayList();
                    for (a = 0; a < 4; a++) {
                        TextView textView = (TextView) this.characterTextViews.get(a);
                        if (ViewProxy.getAlpha(textView) != 0.0f) {
                            animators.add(ObjectAnimatorProxy.ofFloat(textView, "scaleX", 0.0f));
                            animators.add(ObjectAnimatorProxy.ofFloat(textView, "scaleY", 0.0f));
                            animators.add(ObjectAnimatorProxy.ofFloat(textView, "alpha", 0.0f));
                        }
                        textView = (TextView) this.dotTextViews.get(a);
                        if (ViewProxy.getAlpha(textView) != 0.0f) {
                            animators.add(ObjectAnimatorProxy.ofFloat(textView, "scaleX", 0.0f));
                            animators.add(ObjectAnimatorProxy.ofFloat(textView, "scaleY", 0.0f));
                            animators.add(ObjectAnimatorProxy.ofFloat(textView, "alpha", 0.0f));
                        }
                    }
                    this.currentAnimation = new AnimatorSetProxy();
                    this.currentAnimation.setDuration(150);
                    this.currentAnimation.playTogether(animators);
                    this.currentAnimation.addListener(new C15674());
                    this.currentAnimation.start();
                    return;
                }
                for (a = 0; a < 4; a++) {
                    ViewProxy.setAlpha((TextView) this.characterTextViews.get(a), 0.0f);
                    ViewProxy.setAlpha((TextView) this.dotTextViews.get(a), 0.0f);
                }
            }
        }

        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            if (this.dotRunnable != null) {
                AndroidUtilities.cancelRunOnUIThread(this.dotRunnable);
                this.dotRunnable = null;
            }
            if (this.currentAnimation != null) {
                this.currentAnimation.cancel();
                this.currentAnimation = null;
            }
            for (int a = 0; a < 4; a++) {
                if (a < this.stringBuilder.length()) {
                    TextView textView = (TextView) this.characterTextViews.get(a);
                    ViewProxy.setAlpha(textView, 0.0f);
                    ViewProxy.setScaleX(textView, 1.0f);
                    ViewProxy.setScaleY(textView, 1.0f);
                    ViewProxy.setTranslationY(textView, 0.0f);
                    ViewProxy.setTranslationX(textView, (float) getXForTextView(a));
                    textView = (TextView) this.dotTextViews.get(a);
                    ViewProxy.setAlpha(textView, 1.0f);
                    ViewProxy.setScaleX(textView, 1.0f);
                    ViewProxy.setScaleY(textView, 1.0f);
                    ViewProxy.setTranslationY(textView, 0.0f);
                    ViewProxy.setTranslationX(textView, (float) getXForTextView(a));
                } else {
                    ViewProxy.setAlpha((TextView) this.characterTextViews.get(a), 0.0f);
                    ViewProxy.setAlpha((TextView) this.dotTextViews.get(a), 0.0f);
                }
            }
            super.onLayout(changed, left, top, right, bottom);
        }
    }

    public interface PasscodeViewDelegate {
        void didAcceptedPassword();
    }

    class C15628 extends AnimatorListenerAdapterProxy {
        C15628() {
        }

        public void onAnimationEnd(Object animation) {
            PasscodeView.this.clearAnimation();
            PasscodeView.this.setVisibility(8);
        }
    }

    public PasscodeView(Context context) {
        int a;
        super(context);
        setWillNotDraw(false);
        setVisibility(8);
        this.backgroundFrameLayout = new FrameLayout(context);
        addView(this.backgroundFrameLayout);
        LayoutParams layoutParams = (LayoutParams) this.backgroundFrameLayout.getLayoutParams();
        layoutParams.width = -1;
        layoutParams.height = -1;
        this.backgroundFrameLayout.setLayoutParams(layoutParams);
        this.passwordFrameLayout = new FrameLayout(context);
        addView(this.passwordFrameLayout);
        layoutParams = (LayoutParams) this.passwordFrameLayout.getLayoutParams();
        layoutParams.width = -1;
        layoutParams.height = -1;
        layoutParams.gravity = 51;
        this.passwordFrameLayout.setLayoutParams(layoutParams);
        ImageView imageView = new ImageView(context);
        imageView.setScaleType(ScaleType.FIT_XY);
        imageView.setImageResource(C0553R.drawable.passcode_logo);
        this.passwordFrameLayout.addView(imageView);
        layoutParams = (LayoutParams) imageView.getLayoutParams();
        if (AndroidUtilities.density < 1.0f) {
            layoutParams.width = AndroidUtilities.dp(BitmapDescriptorFactory.HUE_ORANGE);
            layoutParams.height = AndroidUtilities.dp(BitmapDescriptorFactory.HUE_ORANGE);
        } else {
            layoutParams.width = AndroidUtilities.dp(40.0f);
            layoutParams.height = AndroidUtilities.dp(40.0f);
        }
        layoutParams.gravity = 81;
        layoutParams.bottomMargin = AndroidUtilities.dp(100.0f);
        imageView.setLayoutParams(layoutParams);
        this.passcodeTextView = new TextView(context);
        this.passcodeTextView.setTextColor(-1);
        this.passcodeTextView.setTextSize(1, 14.0f);
        this.passcodeTextView.setGravity(1);
        this.passwordFrameLayout.addView(this.passcodeTextView);
        layoutParams = (LayoutParams) this.passcodeTextView.getLayoutParams();
        layoutParams.width = -2;
        layoutParams.height = -2;
        layoutParams.bottomMargin = AndroidUtilities.dp(62.0f);
        layoutParams.gravity = 81;
        this.passcodeTextView.setLayoutParams(layoutParams);
        this.passwordEditText2 = new AnimatingTextView(context);
        this.passwordFrameLayout.addView(this.passwordEditText2);
        layoutParams = (LayoutParams) this.passwordEditText2.getLayoutParams();
        layoutParams.height = -2;
        layoutParams.width = -1;
        layoutParams.leftMargin = AndroidUtilities.dp(70.0f);
        layoutParams.rightMargin = AndroidUtilities.dp(70.0f);
        layoutParams.bottomMargin = AndroidUtilities.dp(6.0f);
        layoutParams.gravity = 81;
        this.passwordEditText2.setLayoutParams(layoutParams);
        this.passwordEditText = new EditText(context);
        this.passwordEditText.setTextSize(1, 36.0f);
        this.passwordEditText.setTextColor(-1);
        this.passwordEditText.setMaxLines(1);
        this.passwordEditText.setLines(1);
        this.passwordEditText.setGravity(1);
        this.passwordEditText.setSingleLine(true);
        this.passwordEditText.setImeOptions(6);
        this.passwordEditText.setTypeface(Typeface.DEFAULT);
        this.passwordEditText.setBackgroundDrawable(null);
        AndroidUtilities.clearCursorDrawable(this.passwordEditText);
        this.passwordFrameLayout.addView(this.passwordEditText);
        layoutParams = (LayoutParams) this.passwordEditText.getLayoutParams();
        layoutParams.height = -2;
        layoutParams.width = -1;
        layoutParams.leftMargin = AndroidUtilities.dp(70.0f);
        layoutParams.rightMargin = AndroidUtilities.dp(70.0f);
        layoutParams.bottomMargin = AndroidUtilities.dp(6.0f);
        layoutParams.gravity = 81;
        this.passwordEditText.setLayoutParams(layoutParams);
        this.passwordEditText.setOnEditorActionListener(new C09261());
        this.passwordEditText.addTextChangedListener(new C09272());
        if (VERSION.SDK_INT < 11) {
            this.passwordEditText.setOnCreateContextMenuListener(new C09283());
        } else {
            this.passwordEditText.setCustomSelectionActionModeCallback(new C09294());
        }
        this.checkImage = new ImageView(context);
        this.checkImage.setImageResource(C0553R.drawable.passcode_check);
        this.checkImage.setScaleType(ScaleType.CENTER);
        this.checkImage.setBackgroundResource(C0553R.drawable.bar_selector_lock);
        this.passwordFrameLayout.addView(this.checkImage);
        layoutParams = (LayoutParams) this.checkImage.getLayoutParams();
        layoutParams.width = AndroidUtilities.dp(BitmapDescriptorFactory.HUE_YELLOW);
        layoutParams.height = AndroidUtilities.dp(BitmapDescriptorFactory.HUE_YELLOW);
        layoutParams.bottomMargin = AndroidUtilities.dp(4.0f);
        layoutParams.rightMargin = AndroidUtilities.dp(10.0f);
        layoutParams.gravity = 85;
        this.checkImage.setLayoutParams(layoutParams);
        this.checkImage.setOnClickListener(new C09305());
        FrameLayout lineFrameLayout = new FrameLayout(context);
        lineFrameLayout.setBackgroundColor(654311423);
        this.passwordFrameLayout.addView(lineFrameLayout);
        layoutParams = (LayoutParams) lineFrameLayout.getLayoutParams();
        layoutParams.width = -1;
        layoutParams.height = AndroidUtilities.dp(1.0f);
        layoutParams.gravity = 83;
        layoutParams.leftMargin = AndroidUtilities.dp(20.0f);
        layoutParams.rightMargin = AndroidUtilities.dp(20.0f);
        lineFrameLayout.setLayoutParams(layoutParams);
        this.numbersFrameLayout = new FrameLayout(context);
        addView(this.numbersFrameLayout);
        layoutParams = (LayoutParams) this.numbersFrameLayout.getLayoutParams();
        layoutParams.width = -1;
        layoutParams.height = -1;
        layoutParams.gravity = 51;
        this.numbersFrameLayout.setLayoutParams(layoutParams);
        this.lettersTextViews = new ArrayList(10);
        this.numberTextViews = new ArrayList(10);
        this.numberFrameLayouts = new ArrayList(10);
        for (a = 0; a < 10; a++) {
            TextView textView = new TextView(context);
            textView.setTextColor(-1);
            textView.setTextSize(1, 36.0f);
            textView.setGravity(17);
            textView.setText(String.format(Locale.US, "%d", new Object[]{Integer.valueOf(a)}));
            this.numbersFrameLayout.addView(textView);
            layoutParams = (LayoutParams) textView.getLayoutParams();
            layoutParams.width = AndroidUtilities.dp(50.0f);
            layoutParams.height = AndroidUtilities.dp(50.0f);
            layoutParams.gravity = 51;
            textView.setLayoutParams(layoutParams);
            this.numberTextViews.add(textView);
            textView = new TextView(context);
            textView.setTextSize(1, 12.0f);
            textView.setTextColor(ConnectionsManager.DEFAULT_DATACENTER_ID);
            textView.setGravity(17);
            this.numbersFrameLayout.addView(textView);
            layoutParams = (LayoutParams) textView.getLayoutParams();
            layoutParams.width = AndroidUtilities.dp(50.0f);
            layoutParams.height = AndroidUtilities.dp(20.0f);
            layoutParams.gravity = 51;
            textView.setLayoutParams(layoutParams);
            switch (a) {
                case 0:
                    textView.setText("+");
                    break;
                case 2:
                    textView.setText("ABC");
                    break;
                case 3:
                    textView.setText("DEF");
                    break;
                case 4:
                    textView.setText("GHI");
                    break;
                case 5:
                    textView.setText("JKL");
                    break;
                case 6:
                    textView.setText("MNO");
                    break;
                case 7:
                    textView.setText("PQRS");
                    break;
                case 8:
                    textView.setText("TUV");
                    break;
                case 9:
                    textView.setText("WXYZ");
                    break;
                default:
                    break;
            }
            this.lettersTextViews.add(textView);
        }
        this.eraseView = new ImageView(context);
        this.eraseView.setScaleType(ScaleType.CENTER);
        this.eraseView.setImageResource(C0553R.drawable.passcode_delete);
        this.numbersFrameLayout.addView(this.eraseView);
        layoutParams = (LayoutParams) this.eraseView.getLayoutParams();
        layoutParams.width = AndroidUtilities.dp(50.0f);
        layoutParams.height = AndroidUtilities.dp(50.0f);
        layoutParams.gravity = 51;
        this.eraseView.setLayoutParams(layoutParams);
        for (a = 0; a < 11; a++) {
            FrameLayout frameLayout = new FrameLayout(context);
            frameLayout.setBackgroundResource(C0553R.drawable.bar_selector_lock);
            frameLayout.setTag(Integer.valueOf(a));
            if (a == 10) {
                frameLayout.setOnLongClickListener(new C09316());
            }
            frameLayout.setOnClickListener(new C09327());
            this.numberFrameLayouts.add(frameLayout);
        }
        for (a = 10; a >= 0; a--) {
            frameLayout = (FrameLayout) this.numberFrameLayouts.get(a);
            this.numbersFrameLayout.addView(frameLayout);
            layoutParams = (LayoutParams) frameLayout.getLayoutParams();
            layoutParams.width = AndroidUtilities.dp(100.0f);
            layoutParams.height = AndroidUtilities.dp(100.0f);
            layoutParams.gravity = 51;
            frameLayout.setLayoutParams(layoutParams);
        }
    }

    public void setDelegate(PasscodeViewDelegate delegate) {
        this.delegate = delegate;
    }

    private void processDone(boolean fingerprint) {
        if (!fingerprint) {
            String password = "";
            if (UserConfig.passcodeType == 0) {
                password = this.passwordEditText2.getString();
            } else if (UserConfig.passcodeType == 1) {
                password = this.passwordEditText.getText().toString();
            }
            if (password.length() == 0) {
                onPasscodeError();
                return;
            } else if (!UserConfig.checkPasscode(password)) {
                this.passwordEditText.setText("");
                this.passwordEditText2.eraseAllCharacters(true);
                onPasscodeError();
                return;
            }
        }
        this.passwordEditText.clearFocus();
        AndroidUtilities.hideKeyboard(this.passwordEditText);
        if (VERSION.SDK_INT >= 14) {
            AnimatorSetProxy animatorSetProxy = new AnimatorSetProxy();
            animatorSetProxy.setDuration(200);
            r2 = new Object[2];
            r2[0] = ObjectAnimatorProxy.ofFloat(this, "translationY", (float) AndroidUtilities.dp(20.0f));
            r2[1] = ObjectAnimatorProxy.ofFloat(this, "alpha", (float) AndroidUtilities.dp(0.0f));
            animatorSetProxy.playTogether(r2);
            animatorSetProxy.addListener(new C15628());
            animatorSetProxy.start();
        } else {
            setVisibility(8);
        }
        UserConfig.appLocked = false;
        UserConfig.saveConfig(false);
        NotificationCenter.getInstance().postNotificationName(NotificationCenter.didSetPasscode, new Object[0]);
        setOnTouchListener(null);
        if (this.delegate != null) {
            this.delegate.didAcceptedPassword();
        }
    }

    private void shakeTextView(final float x, final int num) {
        if (num == 6) {
            this.passcodeTextView.clearAnimation();
            return;
        }
        AnimatorSetProxy animatorSetProxy = new AnimatorSetProxy();
        Object[] objArr = new Object[1];
        objArr[0] = ObjectAnimatorProxy.ofFloat(this.passcodeTextView, "translationX", (float) AndroidUtilities.dp(x));
        animatorSetProxy.playTogether(objArr);
        animatorSetProxy.setDuration(50);
        animatorSetProxy.addListener(new AnimatorListenerAdapterProxy() {
            public void onAnimationEnd(Object animation) {
                PasscodeView.this.shakeTextView(num == 5 ? 0.0f : -x, num + 1);
            }
        });
        animatorSetProxy.start();
    }

    private void onPasscodeError() {
        Vibrator v = (Vibrator) getContext().getSystemService("vibrator");
        if (v != null) {
            v.vibrate(200);
        }
        shakeTextView(2.0f, 0);
    }

    public void onResume() {
        if (UserConfig.passcodeType == 1) {
            if (this.passwordEditText != null) {
                this.passwordEditText.requestFocus();
                AndroidUtilities.showKeyboard(this.passwordEditText);
            }
            AndroidUtilities.runOnUIThread(new Runnable() {
                public void run() {
                    if (PasscodeView.this.passwordEditText != null) {
                        PasscodeView.this.passwordEditText.requestFocus();
                        AndroidUtilities.showKeyboard(PasscodeView.this.passwordEditText);
                    }
                }
            }, 200);
        }
        checkFingerprint();
    }

    public void onPause() {
        if (this.fingerprintDialog != null) {
            try {
                if (this.fingerprintDialog.isShowing()) {
                    this.fingerprintDialog.dismiss();
                }
                this.fingerprintDialog = null;
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
        }
        try {
            if (VERSION.SDK_INT >= 23 && this.cancellationSignal != null) {
                this.cancellationSignal.cancel();
                this.cancellationSignal = null;
            }
        } catch (Throwable e2) {
            FileLog.m611e("tmessages", e2);
        }
    }

    private void checkFingerprint() {
        Activity parentActivity = (Activity) getContext();
        if (VERSION.SDK_INT >= 23 && parentActivity != null && UserConfig.useFingerprint && !ApplicationLoader.mainInterfacePaused) {
            try {
                if (this.fingerprintDialog != null && this.fingerprintDialog.isShowing()) {
                    return;
                }
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
            try {
                FingerprintManagerCompat fingerprintManager = FingerprintManagerCompat.from(ApplicationLoader.applicationContext);
                if (fingerprintManager.isHardwareDetected() && fingerprintManager.hasEnrolledFingerprints()) {
                    View relativeLayout = new RelativeLayout(getContext());
                    relativeLayout.setPadding(AndroidUtilities.dp(24.0f), AndroidUtilities.dp(16.0f), AndroidUtilities.dp(24.0f), AndroidUtilities.dp(8.0f));
                    TextView fingerprintTextView = new TextView(getContext());
                    fingerprintTextView.setTextColor(-7105645);
                    fingerprintTextView.setId(1000);
                    fingerprintTextView.setTextAppearance(16974344);
                    fingerprintTextView.setText(LocaleController.getString("FingerprintInfo", C0553R.string.FingerprintInfo));
                    relativeLayout.addView(fingerprintTextView);
                    RelativeLayout.LayoutParams layoutParams = LayoutHelper.createRelative(-2, -2);
                    layoutParams.addRule(10);
                    layoutParams.addRule(20);
                    fingerprintTextView.setLayoutParams(layoutParams);
                    this.fingerprintImageView = new ImageView(getContext());
                    this.fingerprintImageView.setImageResource(C0553R.drawable.ic_fp_40px);
                    this.fingerprintImageView.setId(1001);
                    relativeLayout.addView(this.fingerprintImageView, LayoutHelper.createRelative(-2.0f, -2.0f, 0, 20, 0, 0, 20, 3, 1000));
                    this.fingerprintStatusTextView = new TextView(getContext());
                    this.fingerprintStatusTextView.setGravity(16);
                    this.fingerprintStatusTextView.setText(LocaleController.getString("FingerprintHelp", C0553R.string.FingerprintHelp));
                    this.fingerprintStatusTextView.setTextAppearance(16974320);
                    this.fingerprintStatusTextView.setTextColor(1107296256);
                    relativeLayout.addView(this.fingerprintStatusTextView);
                    layoutParams = LayoutHelper.createRelative(-2, -2);
                    layoutParams.setMarginStart(AndroidUtilities.dp(16.0f));
                    layoutParams.addRule(8, 1001);
                    layoutParams.addRule(6, 1001);
                    layoutParams.addRule(17, 1001);
                    this.fingerprintStatusTextView.setLayoutParams(layoutParams);
                    Builder builder = new Builder(getContext());
                    builder.setTitle(LocaleController.getString("AppName", C0553R.string.AppName));
                    builder.setView(relativeLayout);
                    builder.setNegativeButton(LocaleController.getString("Cancel", C0553R.string.Cancel), null);
                    builder.setOnDismissListener(new OnDismissListener() {
                        public void onDismiss(DialogInterface dialog) {
                            if (PasscodeView.this.cancellationSignal != null) {
                                PasscodeView.this.selfCancelled = true;
                                PasscodeView.this.cancellationSignal.cancel();
                                PasscodeView.this.cancellationSignal = null;
                            }
                        }
                    });
                    if (this.fingerprintDialog != null) {
                        if (this.fingerprintDialog.isShowing()) {
                            this.fingerprintDialog.dismiss();
                        }
                    }
                    this.fingerprintDialog = builder.show();
                    this.cancellationSignal = new CancellationSignal();
                    this.selfCancelled = false;
                    fingerprintManager.authenticate(null, 0, this.cancellationSignal, new AuthenticationCallback() {
                        public void onAuthenticationError(int errMsgId, CharSequence errString) {
                            if (!PasscodeView.this.selfCancelled) {
                                PasscodeView.this.showFingerprintError(errString);
                            }
                        }

                        public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
                            PasscodeView.this.showFingerprintError(helpString);
                        }

                        public void onAuthenticationFailed() {
                            PasscodeView.this.showFingerprintError(LocaleController.getString("FingerprintNotRecognized", C0553R.string.FingerprintNotRecognized));
                        }

                        public void onAuthenticationSucceeded(AuthenticationResult result) {
                            try {
                                if (PasscodeView.this.fingerprintDialog.isShowing()) {
                                    PasscodeView.this.fingerprintDialog.dismiss();
                                }
                            } catch (Throwable e) {
                                FileLog.m611e("tmessages", e);
                            }
                            PasscodeView.this.fingerprintDialog = null;
                            PasscodeView.this.processDone(true);
                        }
                    }, null);
                }
            } catch (Throwable e2) {
                FileLog.m611e("tmessages", e2);
            } catch (Throwable th) {
            }
        }
    }

    public void onShow() {
        Activity parentActivity = (Activity) getContext();
        if (UserConfig.passcodeType == 1) {
            if (this.passwordEditText != null) {
                this.passwordEditText.requestFocus();
                AndroidUtilities.showKeyboard(this.passwordEditText);
            }
        } else if (parentActivity != null) {
            View currentFocus = parentActivity.getCurrentFocus();
            if (currentFocus != null) {
                currentFocus.clearFocus();
                AndroidUtilities.hideKeyboard(((Activity) getContext()).getCurrentFocus());
            }
        }
        checkFingerprint();
        if (getVisibility() != 0) {
            if (VERSION.SDK_INT >= 14) {
                ViewProxy.setAlpha(this, 1.0f);
                ViewProxy.setTranslationY(this, 0.0f);
                clearAnimation();
            }
            if (ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0).getInt("selectedBackground", 1000001) == 1000001) {
                this.backgroundFrameLayout.setBackgroundColor(-11436898);
            } else {
                this.backgroundDrawable = ApplicationLoader.getCachedWallpaper();
                if (this.backgroundDrawable != null) {
                    this.backgroundFrameLayout.setBackgroundColor(-1090519040);
                } else {
                    this.backgroundFrameLayout.setBackgroundColor(-11436898);
                }
            }
            this.passcodeTextView.setText(LocaleController.getString("EnterYourPasscode", C0553R.string.EnterYourPasscode));
            if (UserConfig.passcodeType == 0) {
                this.numbersFrameLayout.setVisibility(0);
                this.passwordEditText.setVisibility(8);
                this.passwordEditText2.setVisibility(0);
                this.checkImage.setVisibility(8);
            } else if (UserConfig.passcodeType == 1) {
                this.passwordEditText.setFilters(new InputFilter[0]);
                this.passwordEditText.setInputType(129);
                this.numbersFrameLayout.setVisibility(8);
                this.passwordEditText.setFocusable(true);
                this.passwordEditText.setFocusableInTouchMode(true);
                this.passwordEditText.setVisibility(0);
                this.passwordEditText2.setVisibility(8);
                this.checkImage.setVisibility(0);
            }
            setVisibility(0);
            this.passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            this.passwordEditText.setText("");
            this.passwordEditText2.eraseAllCharacters(false);
            setOnTouchListener(new OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });
        }
    }

    private void showFingerprintError(CharSequence error) {
        this.fingerprintImageView.setImageResource(C0553R.drawable.ic_fingerprint_error);
        this.fingerprintStatusTextView.setText(error);
        this.fingerprintStatusTextView.setTextColor(-765666);
        Vibrator v = (Vibrator) getContext().getSystemService("vibrator");
        if (v != null) {
            v.vibrate(200);
        }
        AndroidUtilities.shakeView(this.fingerprintStatusTextView, 2.0f, 0);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int top;
        LayoutParams layoutParams;
        int i;
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = AndroidUtilities.displaySize.y - (VERSION.SDK_INT >= 21 ? 0 : AndroidUtilities.statusBarHeight);
        if (AndroidUtilities.isTablet() || getContext().getResources().getConfiguration().orientation != 2) {
            top = 0;
            int left = 0;
            if (AndroidUtilities.isTablet()) {
                if (width > AndroidUtilities.dp(498.0f)) {
                    left = (width - AndroidUtilities.dp(498.0f)) / 2;
                    width = AndroidUtilities.dp(498.0f);
                }
                if (height > AndroidUtilities.dp(528.0f)) {
                    top = (height - AndroidUtilities.dp(528.0f)) / 2;
                    height = AndroidUtilities.dp(528.0f);
                }
            }
            layoutParams = (LayoutParams) this.passwordFrameLayout.getLayoutParams();
            layoutParams.height = height / 3;
            layoutParams.width = width;
            layoutParams.topMargin = top;
            layoutParams.leftMargin = left;
            this.passwordFrameLayout.setTag(Integer.valueOf(top));
            this.passwordFrameLayout.setLayoutParams(layoutParams);
            layoutParams = (LayoutParams) this.numbersFrameLayout.getLayoutParams();
            layoutParams.height = (height / 3) * 2;
            layoutParams.leftMargin = left;
            layoutParams.topMargin = (height - layoutParams.height) + top;
            layoutParams.width = width;
            this.numbersFrameLayout.setLayoutParams(layoutParams);
        } else {
            layoutParams = (LayoutParams) this.passwordFrameLayout.getLayoutParams();
            if (UserConfig.passcodeType == 0) {
                i = width / 2;
            } else {
                i = width;
            }
            layoutParams.width = i;
            layoutParams.height = AndroidUtilities.dp(140.0f);
            layoutParams.topMargin = (height - AndroidUtilities.dp(140.0f)) / 2;
            this.passwordFrameLayout.setLayoutParams(layoutParams);
            layoutParams = (LayoutParams) this.numbersFrameLayout.getLayoutParams();
            layoutParams.height = height;
            layoutParams.leftMargin = width / 2;
            layoutParams.topMargin = height - layoutParams.height;
            layoutParams.width = width / 2;
            this.numbersFrameLayout.setLayoutParams(layoutParams);
        }
        int sizeBetweenNumbersX = (layoutParams.width - (AndroidUtilities.dp(50.0f) * 3)) / 4;
        int sizeBetweenNumbersY = (layoutParams.height - (AndroidUtilities.dp(50.0f) * 4)) / 5;
        for (int a = 0; a < 11; a++) {
            int num;
            LayoutParams layoutParams1;
            if (a == 0) {
                num = 10;
            } else if (a == 10) {
                num = 11;
            } else {
                num = a - 1;
            }
            int row = num / 3;
            int col = num % 3;
            if (a < 10) {
                TextView textView = (TextView) this.numberTextViews.get(a);
                TextView textView1 = (TextView) this.lettersTextViews.get(a);
                layoutParams = (LayoutParams) textView.getLayoutParams();
                layoutParams1 = (LayoutParams) textView1.getLayoutParams();
                top = sizeBetweenNumbersY + ((AndroidUtilities.dp(50.0f) + sizeBetweenNumbersY) * row);
                layoutParams.topMargin = top;
                layoutParams1.topMargin = top;
                i = ((AndroidUtilities.dp(50.0f) + sizeBetweenNumbersX) * col) + sizeBetweenNumbersX;
                layoutParams.leftMargin = i;
                layoutParams1.leftMargin = i;
                layoutParams1.topMargin += AndroidUtilities.dp(40.0f);
                textView.setLayoutParams(layoutParams);
                textView1.setLayoutParams(layoutParams1);
            } else {
                layoutParams = (LayoutParams) this.eraseView.getLayoutParams();
                top = (((AndroidUtilities.dp(50.0f) + sizeBetweenNumbersY) * row) + sizeBetweenNumbersY) + AndroidUtilities.dp(8.0f);
                layoutParams.topMargin = top;
                layoutParams.leftMargin = ((AndroidUtilities.dp(50.0f) + sizeBetweenNumbersX) * col) + sizeBetweenNumbersX;
                top -= AndroidUtilities.dp(8.0f);
                this.eraseView.setLayoutParams(layoutParams);
            }
            FrameLayout frameLayout = (FrameLayout) this.numberFrameLayouts.get(a);
            layoutParams1 = (LayoutParams) frameLayout.getLayoutParams();
            layoutParams1.topMargin = top - AndroidUtilities.dp(17.0f);
            layoutParams1.leftMargin = layoutParams.leftMargin - AndroidUtilities.dp(25.0f);
            frameLayout.setLayoutParams(layoutParams1);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        View rootView = getRootView();
        int usableViewHeight = (rootView.getHeight() - AndroidUtilities.statusBarHeight) - AndroidUtilities.getViewInset(rootView);
        getWindowVisibleDisplayFrame(this.rect);
        this.keyboardHeight = usableViewHeight - (this.rect.bottom - this.rect.top);
        if (UserConfig.passcodeType == 1 && (AndroidUtilities.isTablet() || getContext().getResources().getConfiguration().orientation != 2)) {
            int i;
            int t = 0;
            if (this.passwordFrameLayout.getTag() != Integer.valueOf(0)) {
                t = ((Integer) this.passwordFrameLayout.getTag()).intValue();
            }
            LayoutParams layoutParams = (LayoutParams) this.passwordFrameLayout.getLayoutParams();
            int i2 = (layoutParams.height + t) - (this.keyboardHeight / 2);
            if (VERSION.SDK_INT >= 21) {
                i = AndroidUtilities.statusBarHeight;
            } else {
                i = 0;
            }
            layoutParams.topMargin = i2 - i;
            this.passwordFrameLayout.setLayoutParams(layoutParams);
        }
        super.onLayout(changed, left, top, right, bottom);
    }

    protected void onDraw(Canvas canvas) {
        if (getVisibility() == 0) {
            if (this.backgroundDrawable == null) {
                super.onDraw(canvas);
            } else if (this.backgroundDrawable instanceof ColorDrawable) {
                this.backgroundDrawable.setBounds(0, 0, getMeasuredWidth(), getMeasuredHeight());
                this.backgroundDrawable.draw(canvas);
            } else {
                float scale;
                float scaleX = ((float) getMeasuredWidth()) / ((float) this.backgroundDrawable.getIntrinsicWidth());
                float scaleY = ((float) (getMeasuredHeight() + this.keyboardHeight)) / ((float) this.backgroundDrawable.getIntrinsicHeight());
                if (scaleX < scaleY) {
                    scale = scaleY;
                } else {
                    scale = scaleX;
                }
                int width = (int) Math.ceil((double) (((float) this.backgroundDrawable.getIntrinsicWidth()) * scale));
                int height = (int) Math.ceil((double) (((float) this.backgroundDrawable.getIntrinsicHeight()) * scale));
                int x = (getMeasuredWidth() - width) / 2;
                int y = ((getMeasuredHeight() - height) + this.keyboardHeight) / 2;
                this.backgroundDrawable.setBounds(x, y, x + width, y + height);
                this.backgroundDrawable.draw(canvas);
            }
        }
    }
}
