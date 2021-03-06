package org.telegram.ui.ActionBar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import com.google.android.gms.location.LocationStatusCodes;
import java.util.ArrayList;
import java.util.Iterator;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.AnimationCompat.AnimatorListenerAdapterProxy;
import org.telegram.messenger.AnimationCompat.AnimatorSetProxy;
import org.telegram.messenger.AnimationCompat.ObjectAnimatorProxy;
import org.telegram.messenger.AnimationCompat.ViewProxy;
import org.telegram.messenger.C0553R;

public class ActionBarLayout extends FrameLayout {
    private static Drawable headerShadowDrawable;
    private static Drawable layerShadowDrawable;
    private static Paint scrimPaint;
    private AccelerateDecelerateInterpolator accelerateDecelerateInterpolator = new AccelerateDecelerateInterpolator();
    protected boolean animationInProgress;
    private float animationProgress = 0.0f;
    private Runnable animationRunnable;
    private View backgroundView;
    private boolean beginTrackingSent;
    private LinearLayoutContainer containerView;
    private LinearLayoutContainer containerViewBack;
    private ActionBar currentActionBar;
    private AnimatorSetProxy currentAnimation;
    private DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator(1.5f);
    private ActionBarLayoutDelegate delegate = null;
    private DrawerLayoutContainer drawerLayoutContainer;
    public ArrayList<BaseFragment> fragmentsStack = null;
    private boolean inActionMode;
    public float innerTranslationX;
    private long lastFrameTime;
    private boolean maybeStartTracking;
    private Runnable onCloseAnimationEndRunnable;
    private Runnable onOpenAnimationEndRunnable;
    protected Activity parentActivity = null;
    private boolean removeActionBarExtraHeight;
    protected boolean startedTracking;
    private int startedTrackingPointerId;
    private int startedTrackingX;
    private int startedTrackingY;
    private String titleOverlayText;
    private boolean transitionAnimationInProgress;
    private long transitionAnimationStartTime;
    private boolean useAlphaAnimations;
    private VelocityTracker velocityTracker;

    class C06926 implements Runnable {
        C06926() {
        }

        public void run() {
            ActionBarLayout.this.onAnimationEndCheck(false);
        }
    }

    class C06948 implements Runnable {
        C06948() {
        }

        public void run() {
            ActionBarLayout.this.onAnimationEndCheck(false);
        }
    }

    public interface ActionBarLayoutDelegate {
        boolean needAddFragmentToStack(BaseFragment baseFragment, ActionBarLayout actionBarLayout);

        boolean needCloseLastFragment(ActionBarLayout actionBarLayout);

        boolean needPresentFragment(BaseFragment baseFragment, boolean z, boolean z2, ActionBarLayout actionBarLayout);

        boolean onPreIme();

        void onRebuildAllFragments(ActionBarLayout actionBarLayout);
    }

    public class LinearLayoutContainer extends LinearLayout {
        public LinearLayoutContainer(Context context) {
            super(context);
            setOrientation(1);
        }

        protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
            if (child instanceof ActionBar) {
                return super.drawChild(canvas, child, drawingTime);
            }
            boolean result;
            int actionBarHeight = 0;
            int childCount = getChildCount();
            for (int a = 0; a < childCount; a++) {
                View view = getChildAt(a);
                if (view != child && (view instanceof ActionBar) && view.getVisibility() == 0) {
                    if (((ActionBar) view).getCastShadows()) {
                        actionBarHeight = view.getMeasuredHeight();
                    }
                    result = super.drawChild(canvas, child, drawingTime);
                    if (actionBarHeight == 0 && ActionBarLayout.headerShadowDrawable != null) {
                        ActionBarLayout.headerShadowDrawable.setBounds(0, actionBarHeight, getMeasuredWidth(), ActionBarLayout.headerShadowDrawable.getIntrinsicHeight() + actionBarHeight);
                        ActionBarLayout.headerShadowDrawable.draw(canvas);
                        return result;
                    }
                }
            }
            result = super.drawChild(canvas, child, drawingTime);
            return actionBarHeight == 0 ? result : result;
        }

        public boolean hasOverlappingRendering() {
            return false;
        }
    }

    class C14964 extends AnimatorListenerAdapterProxy {
        C14964() {
        }

        public void onAnimationEnd(Object animation) {
            ActionBarLayout.this.onAnimationEndCheck(false);
        }

        public void onAnimationCancel(Object animation) {
            ActionBarLayout.this.onAnimationEndCheck(false);
        }
    }

    static /* synthetic */ float access$516(ActionBarLayout x0, float x1) {
        float f = x0.animationProgress + x1;
        x0.animationProgress = f;
        return f;
    }

    public ActionBarLayout(Context context) {
        super(context);
        this.parentActivity = (Activity) context;
        if (layerShadowDrawable == null) {
            layerShadowDrawable = getResources().getDrawable(C0553R.drawable.layer_shadow);
            headerShadowDrawable = getResources().getDrawable(C0553R.drawable.header_shadow);
            scrimPaint = new Paint();
        }
    }

    public void init(ArrayList<BaseFragment> stack) {
        this.fragmentsStack = stack;
        this.containerViewBack = new LinearLayoutContainer(this.parentActivity);
        addView(this.containerViewBack);
        LayoutParams layoutParams = (LayoutParams) this.containerViewBack.getLayoutParams();
        layoutParams.width = -1;
        layoutParams.height = -1;
        layoutParams.gravity = 51;
        this.containerViewBack.setLayoutParams(layoutParams);
        this.containerView = new LinearLayoutContainer(this.parentActivity);
        addView(this.containerView);
        layoutParams = (LayoutParams) this.containerView.getLayoutParams();
        layoutParams.width = -1;
        layoutParams.height = -1;
        layoutParams.gravity = 51;
        this.containerView.setLayoutParams(layoutParams);
        Iterator i$ = this.fragmentsStack.iterator();
        while (i$.hasNext()) {
            ((BaseFragment) i$.next()).setParentLayout(this);
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (!this.fragmentsStack.isEmpty()) {
            ((BaseFragment) this.fragmentsStack.get(this.fragmentsStack.size() - 1)).onConfigurationChanged(newConfig);
        }
    }

    public void drawHeaderShadow(Canvas canvas, int y) {
        if (headerShadowDrawable != null) {
            headerShadowDrawable.setBounds(0, y, getMeasuredWidth(), headerShadowDrawable.getIntrinsicHeight() + y);
            headerShadowDrawable.draw(canvas);
        }
    }

    public void setInnerTranslationX(float value) {
        this.innerTranslationX = value;
        invalidate();
    }

    public float getInnerTranslationX() {
        return this.innerTranslationX;
    }

    public void onResume() {
        if (this.transitionAnimationInProgress) {
            if (this.currentAnimation != null) {
                this.currentAnimation.cancel();
                this.currentAnimation = null;
            }
            if (this.onCloseAnimationEndRunnable != null) {
                onCloseAnimationEnd(false);
            } else if (this.onOpenAnimationEndRunnable != null) {
                onOpenAnimationEnd(false);
            }
        }
        if (!this.fragmentsStack.isEmpty()) {
            ((BaseFragment) this.fragmentsStack.get(this.fragmentsStack.size() - 1)).onResume();
        }
    }

    public void onPause() {
        if (!this.fragmentsStack.isEmpty()) {
            ((BaseFragment) this.fragmentsStack.get(this.fragmentsStack.size() - 1)).onPause();
        }
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return this.animationInProgress || checkTransitionAnimation() || onTouchEvent(ev);
    }

    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        onTouchEvent(null);
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
    }

    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        if (event == null || event.getKeyCode() != 4 || event.getAction() != 1) {
            return super.dispatchKeyEventPreIme(event);
        }
        if ((this.delegate == null || !this.delegate.onPreIme()) && !super.dispatchKeyEventPreIme(event)) {
            return false;
        }
        return true;
    }

    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        int width = (getWidth() - getPaddingLeft()) - getPaddingRight();
        int translationX = ((int) this.innerTranslationX) + getPaddingRight();
        int clipLeft = getPaddingLeft();
        int clipRight = width + getPaddingLeft();
        if (child == this.containerViewBack) {
            clipRight = translationX;
        } else if (child == this.containerView) {
            clipLeft = translationX;
        }
        int restoreCount = canvas.save();
        if (!(this.transitionAnimationInProgress || clipLeft == 0 || clipRight == 0)) {
            canvas.clipRect(clipLeft, 0, clipRight, getHeight());
        }
        boolean result = super.drawChild(canvas, child, drawingTime);
        canvas.restoreToCount(restoreCount);
        if (translationX != 0) {
            if (child == this.containerView) {
                float alpha = Math.max(0.0f, Math.min(((float) (width - translationX)) / ((float) AndroidUtilities.dp(20.0f)), 1.0f));
                layerShadowDrawable.setBounds(translationX - layerShadowDrawable.getIntrinsicWidth(), child.getTop(), translationX, child.getBottom());
                layerShadowDrawable.setAlpha((int) (255.0f * alpha));
                layerShadowDrawable.draw(canvas);
            } else if (child == this.containerViewBack) {
                float opacity = Math.min(0.8f, ((float) (width - translationX)) / ((float) width));
                if (opacity < 0.0f) {
                    opacity = 0.0f;
                }
                scrimPaint.setColor(((int) (153.0f * opacity)) << 24);
                canvas.drawRect((float) clipLeft, 0.0f, (float) clipRight, (float) getHeight(), scrimPaint);
            }
        }
        return result;
    }

    public void setDelegate(ActionBarLayoutDelegate delegate) {
        this.delegate = delegate;
    }

    private void onSlideAnimationEnd(boolean backAnimation) {
        BaseFragment lastFragment;
        if (backAnimation) {
            ViewGroup parent;
            lastFragment = (BaseFragment) this.fragmentsStack.get(this.fragmentsStack.size() - 2);
            lastFragment.onPause();
            if (lastFragment.fragmentView != null) {
                parent = (ViewGroup) lastFragment.fragmentView.getParent();
                if (parent != null) {
                    parent.removeView(lastFragment.fragmentView);
                }
            }
            if (lastFragment.actionBar != null && lastFragment.actionBar.getAddToContainer()) {
                parent = (ViewGroup) lastFragment.actionBar.getParent();
                if (parent != null) {
                    parent.removeView(lastFragment.actionBar);
                }
            }
        } else {
            lastFragment = (BaseFragment) this.fragmentsStack.get(this.fragmentsStack.size() - 1);
            lastFragment.onPause();
            lastFragment.onFragmentDestroy();
            lastFragment.setParentLayout(null);
            this.fragmentsStack.remove(this.fragmentsStack.size() - 1);
            LinearLayoutContainer temp = this.containerView;
            this.containerView = this.containerViewBack;
            this.containerViewBack = temp;
            bringChildToFront(this.containerView);
            lastFragment = (BaseFragment) this.fragmentsStack.get(this.fragmentsStack.size() - 1);
            this.currentActionBar = lastFragment.actionBar;
            lastFragment.onResume();
            lastFragment.onBecomeFullyVisible();
        }
        this.containerViewBack.setVisibility(8);
        this.startedTracking = false;
        this.animationInProgress = false;
        ViewProxy.setTranslationX(this.containerView, 0.0f);
        ViewProxy.setTranslationX(this.containerViewBack, 0.0f);
        setInnerTranslationX(0.0f);
    }

    private void prepareForMoving(MotionEvent ev) {
        ViewGroup parent;
        this.maybeStartTracking = false;
        this.startedTracking = true;
        this.startedTrackingX = (int) ev.getX();
        this.containerViewBack.setVisibility(0);
        this.beginTrackingSent = false;
        BaseFragment lastFragment = (BaseFragment) this.fragmentsStack.get(this.fragmentsStack.size() - 2);
        View fragmentView = lastFragment.fragmentView;
        if (fragmentView == null) {
            fragmentView = lastFragment.createView(this.parentActivity);
        } else {
            parent = (ViewGroup) fragmentView.getParent();
            if (parent != null) {
                parent.removeView(fragmentView);
            }
        }
        parent = (ViewGroup) fragmentView.getParent();
        if (parent != null) {
            parent.removeView(fragmentView);
        }
        if (lastFragment.actionBar != null && lastFragment.actionBar.getAddToContainer()) {
            parent = (ViewGroup) lastFragment.actionBar.getParent();
            if (parent != null) {
                parent.removeView(lastFragment.actionBar);
            }
            if (this.removeActionBarExtraHeight) {
                lastFragment.actionBar.setOccupyStatusBar(false);
            }
            this.containerViewBack.addView(lastFragment.actionBar);
            lastFragment.actionBar.setTitleOverlayText(this.titleOverlayText);
        }
        this.containerViewBack.addView(fragmentView);
        ViewGroup.LayoutParams layoutParams = fragmentView.getLayoutParams();
        layoutParams.width = -1;
        layoutParams.height = -1;
        fragmentView.setLayoutParams(layoutParams);
        if (!lastFragment.hasOwnBackground && fragmentView.getBackground() == null) {
            fragmentView.setBackgroundColor(-1);
        }
        lastFragment.onResume();
    }

    public boolean onTouchEvent(MotionEvent ev) {
        if (checkTransitionAnimation() || this.inActionMode || this.animationInProgress) {
            return false;
        }
        if (this.fragmentsStack.size() > 1) {
            if (ev == null || ev.getAction() != 0 || this.startedTracking || this.maybeStartTracking) {
                if (ev != null && ev.getAction() == 2 && ev.getPointerId(0) == this.startedTrackingPointerId) {
                    if (this.velocityTracker == null) {
                        this.velocityTracker = VelocityTracker.obtain();
                    }
                    int dx = Math.max(0, (int) (ev.getX() - ((float) this.startedTrackingX)));
                    int dy = Math.abs(((int) ev.getY()) - this.startedTrackingY);
                    this.velocityTracker.addMovement(ev);
                    if (this.maybeStartTracking && !this.startedTracking && ((float) dx) >= AndroidUtilities.getPixelsInCM(0.4f, true) && Math.abs(dx) / 3 > dy) {
                        prepareForMoving(ev);
                    } else if (this.startedTracking) {
                        if (!this.beginTrackingSent) {
                            if (this.parentActivity.getCurrentFocus() != null) {
                                AndroidUtilities.hideKeyboard(this.parentActivity.getCurrentFocus());
                            }
                            ((BaseFragment) this.fragmentsStack.get(this.fragmentsStack.size() - 1)).onBeginSlide();
                            this.beginTrackingSent = true;
                        }
                        ViewProxy.setTranslationX(this.containerView, (float) dx);
                        setInnerTranslationX((float) dx);
                    }
                } else if (ev != null && ev.getPointerId(0) == this.startedTrackingPointerId && (ev.getAction() == 3 || ev.getAction() == 1 || ev.getAction() == 6)) {
                    float velX;
                    if (this.velocityTracker == null) {
                        this.velocityTracker = VelocityTracker.obtain();
                    }
                    this.velocityTracker.computeCurrentVelocity(LocationStatusCodes.GEOFENCE_NOT_AVAILABLE);
                    if (!this.startedTracking && ((BaseFragment) this.fragmentsStack.get(this.fragmentsStack.size() - 1)).swipeBackEnabled) {
                        velX = this.velocityTracker.getXVelocity();
                        float velY = this.velocityTracker.getYVelocity();
                        if (velX >= 3500.0f && velX > Math.abs(velY)) {
                            prepareForMoving(ev);
                            if (!this.beginTrackingSent) {
                                if (((Activity) getContext()).getCurrentFocus() != null) {
                                    AndroidUtilities.hideKeyboard(((Activity) getContext()).getCurrentFocus());
                                }
                                this.beginTrackingSent = true;
                            }
                        }
                    }
                    if (this.startedTracking) {
                        float distToMove;
                        float x = ViewProxy.getX(this.containerView);
                        AnimatorSetProxy animatorSet = new AnimatorSetProxy();
                        velX = this.velocityTracker.getXVelocity();
                        final boolean backAnimation = x < ((float) this.containerView.getMeasuredWidth()) / 3.0f && (velX < 3500.0f || velX < this.velocityTracker.getYVelocity());
                        Object[] objArr;
                        if (backAnimation) {
                            distToMove = x;
                            objArr = new Object[2];
                            objArr[0] = ObjectAnimatorProxy.ofFloat(this.containerView, "translationX", 0.0f);
                            objArr[1] = ObjectAnimatorProxy.ofFloat(this, "innerTranslationX", 0.0f);
                            animatorSet.playTogether(objArr);
                        } else {
                            distToMove = ((float) this.containerView.getMeasuredWidth()) - x;
                            objArr = new Object[2];
                            objArr[0] = ObjectAnimatorProxy.ofFloat(this.containerView, "translationX", (float) this.containerView.getMeasuredWidth());
                            objArr[1] = ObjectAnimatorProxy.ofFloat(this, "innerTranslationX", (float) this.containerView.getMeasuredWidth());
                            animatorSet.playTogether(objArr);
                        }
                        animatorSet.setDuration((long) Math.max((int) ((200.0f / ((float) this.containerView.getMeasuredWidth())) * distToMove), 50));
                        animatorSet.addListener(new AnimatorListenerAdapterProxy() {
                            public void onAnimationEnd(Object animator) {
                                ActionBarLayout.this.onSlideAnimationEnd(backAnimation);
                            }

                            public void onAnimationCancel(Object animator) {
                                ActionBarLayout.this.onSlideAnimationEnd(backAnimation);
                            }
                        });
                        animatorSet.start();
                        this.animationInProgress = true;
                    } else {
                        this.maybeStartTracking = false;
                        this.startedTracking = false;
                    }
                    if (this.velocityTracker != null) {
                        this.velocityTracker.recycle();
                        this.velocityTracker = null;
                    }
                } else if (ev == null) {
                    this.maybeStartTracking = false;
                    this.startedTracking = false;
                    if (this.velocityTracker != null) {
                        this.velocityTracker.recycle();
                        this.velocityTracker = null;
                    }
                }
            } else if (!((BaseFragment) this.fragmentsStack.get(this.fragmentsStack.size() - 1)).swipeBackEnabled) {
                return false;
            } else {
                this.startedTrackingPointerId = ev.getPointerId(0);
                this.maybeStartTracking = true;
                this.startedTrackingX = (int) ev.getX();
                this.startedTrackingY = (int) ev.getY();
                if (this.velocityTracker != null) {
                    this.velocityTracker.clear();
                }
            }
        }
        return this.startedTracking;
    }

    public void onBackPressed() {
        if (!this.startedTracking && !checkTransitionAnimation() && !this.fragmentsStack.isEmpty()) {
            if (this.currentActionBar != null && this.currentActionBar.isSearchFieldVisible) {
                this.currentActionBar.closeSearchField();
            } else if (((BaseFragment) this.fragmentsStack.get(this.fragmentsStack.size() - 1)).onBackPressed() && !this.fragmentsStack.isEmpty()) {
                closeLastFragment(true);
            }
        }
    }

    public void onLowMemory() {
        Iterator i$ = this.fragmentsStack.iterator();
        while (i$.hasNext()) {
            ((BaseFragment) i$.next()).onLowMemory();
        }
    }

    private void onAnimationEndCheck(boolean byCheck) {
        onCloseAnimationEnd(false);
        onOpenAnimationEnd(false);
        if (this.currentAnimation != null) {
            if (byCheck) {
                this.currentAnimation.cancel();
            }
            this.currentAnimation = null;
        }
        if (this.animationRunnable != null) {
            AndroidUtilities.cancelRunOnUIThread(this.animationRunnable);
            this.animationRunnable = null;
        }
        ViewProxy.setAlpha(this, 1.0f);
        ViewProxy.setAlpha(this.containerView, 1.0f);
        ViewProxy.setScaleX(this.containerView, 1.0f);
        ViewProxy.setScaleY(this.containerView, 1.0f);
        ViewProxy.setAlpha(this.containerViewBack, 1.0f);
        ViewProxy.setScaleX(this.containerViewBack, 1.0f);
        ViewProxy.setScaleY(this.containerViewBack, 1.0f);
    }

    public boolean checkTransitionAnimation() {
        if (this.transitionAnimationInProgress && this.transitionAnimationStartTime < System.currentTimeMillis() - 1000) {
            onAnimationEndCheck(true);
        }
        return this.transitionAnimationInProgress;
    }

    private void presentFragmentInternalRemoveOld(boolean removeLast, BaseFragment fragment) {
        if (fragment != null) {
            fragment.onPause();
            if (removeLast) {
                fragment.onFragmentDestroy();
                fragment.setParentLayout(null);
                this.fragmentsStack.remove(fragment);
            } else {
                ViewGroup parent;
                if (fragment.fragmentView != null) {
                    parent = (ViewGroup) fragment.fragmentView.getParent();
                    if (parent != null) {
                        parent.removeView(fragment.fragmentView);
                    }
                }
                if (fragment.actionBar != null && fragment.actionBar.getAddToContainer()) {
                    parent = (ViewGroup) fragment.actionBar.getParent();
                    if (parent != null) {
                        parent.removeView(fragment.actionBar);
                    }
                }
            }
            this.containerViewBack.setVisibility(8);
        }
    }

    public boolean presentFragment(BaseFragment fragment) {
        return presentFragment(fragment, false, false, true);
    }

    public boolean presentFragment(BaseFragment fragment, boolean removeLast) {
        return presentFragment(fragment, removeLast, false, true);
    }

    private void startLayoutAnimation(final boolean open, final boolean first) {
        if (first) {
            this.animationProgress = 0.0f;
            this.lastFrameTime = System.nanoTime() / 1000000;
            if (VERSION.SDK_INT > 15) {
                this.containerView.setLayerType(2, null);
                this.containerViewBack.setLayerType(2, null);
            }
        }
        Runnable c06892 = new Runnable() {
            public void run() {
                if (ActionBarLayout.this.animationRunnable == this) {
                    ActionBarLayout.this.animationRunnable = null;
                    if (first) {
                        ActionBarLayout.this.transitionAnimationStartTime = System.currentTimeMillis();
                    }
                    long newTime = System.nanoTime() / 1000000;
                    long dt = newTime - ActionBarLayout.this.lastFrameTime;
                    if (dt > 18) {
                        dt = 18;
                    }
                    ActionBarLayout.this.lastFrameTime = newTime;
                    ActionBarLayout.access$516(ActionBarLayout.this, ((float) dt) / 150.0f);
                    if (ActionBarLayout.this.animationProgress > 1.0f) {
                        ActionBarLayout.this.animationProgress = 1.0f;
                    }
                    float interpolated = ActionBarLayout.this.decelerateInterpolator.getInterpolation(ActionBarLayout.this.animationProgress);
                    if (open) {
                        ViewProxy.setAlpha(ActionBarLayout.this.containerView, interpolated);
                        ViewProxy.setTranslationX(ActionBarLayout.this.containerView, ((float) AndroidUtilities.dp(48.0f)) * (1.0f - interpolated));
                    } else {
                        ViewProxy.setAlpha(ActionBarLayout.this.containerViewBack, 1.0f - interpolated);
                        ViewProxy.setTranslationX(ActionBarLayout.this.containerViewBack, ((float) AndroidUtilities.dp(48.0f)) * interpolated);
                    }
                    if (ActionBarLayout.this.animationProgress < 1.0f) {
                        ActionBarLayout.this.startLayoutAnimation(open, false);
                    } else {
                        ActionBarLayout.this.onAnimationEndCheck(false);
                    }
                }
            }
        };
        this.animationRunnable = c06892;
        AndroidUtilities.runOnUIThread(c06892);
    }

    public boolean presentFragment(final BaseFragment fragment, final boolean removeLast, boolean forceWithoutAnimation, boolean check) {
        if (checkTransitionAnimation() || ((this.delegate != null && check && !this.delegate.needPresentFragment(fragment, removeLast, forceWithoutAnimation, this)) || !fragment.onFragmentCreate())) {
            return false;
        }
        ViewGroup parent;
        if (this.parentActivity.getCurrentFocus() != null) {
            AndroidUtilities.hideKeyboard(this.parentActivity.getCurrentFocus());
        }
        boolean needAnimation = VERSION.SDK_INT > 10 && !forceWithoutAnimation && this.parentActivity.getSharedPreferences("mainconfig", 0).getBoolean("view_animations", true);
        final BaseFragment currentFragment = !this.fragmentsStack.isEmpty() ? (BaseFragment) this.fragmentsStack.get(this.fragmentsStack.size() - 1) : null;
        fragment.setParentLayout(this);
        View fragmentView = fragment.fragmentView;
        if (fragmentView == null) {
            fragmentView = fragment.createView(this.parentActivity);
        } else {
            parent = (ViewGroup) fragmentView.getParent();
            if (parent != null) {
                parent.removeView(fragmentView);
            }
        }
        if (fragment.actionBar != null && fragment.actionBar.getAddToContainer()) {
            if (this.removeActionBarExtraHeight) {
                fragment.actionBar.setOccupyStatusBar(false);
            }
            parent = (ViewGroup) fragment.actionBar.getParent();
            if (parent != null) {
                parent.removeView(fragment.actionBar);
            }
            this.containerViewBack.addView(fragment.actionBar);
            fragment.actionBar.setTitleOverlayText(this.titleOverlayText);
        }
        this.containerViewBack.addView(fragmentView);
        ViewGroup.LayoutParams layoutParams = fragmentView.getLayoutParams();
        layoutParams.width = -1;
        layoutParams.height = -1;
        fragmentView.setLayoutParams(layoutParams);
        this.fragmentsStack.add(fragment);
        fragment.onResume();
        this.currentActionBar = fragment.actionBar;
        if (!fragment.hasOwnBackground && fragmentView.getBackground() == null) {
            fragmentView.setBackgroundColor(-1);
        }
        LinearLayoutContainer temp = this.containerView;
        this.containerView = this.containerViewBack;
        this.containerViewBack = temp;
        this.containerView.setVisibility(0);
        setInnerTranslationX(0.0f);
        bringChildToFront(this.containerView);
        if (!needAnimation) {
            presentFragmentInternalRemoveOld(removeLast, currentFragment);
            if (this.backgroundView != null) {
                this.backgroundView.setVisibility(0);
            }
        }
        if (!needAnimation) {
            if (this.backgroundView != null) {
                ViewProxy.setAlpha(this.backgroundView, 1.0f);
                this.backgroundView.setVisibility(0);
            }
            fragment.onTransitionAnimationStart(true, false);
            fragment.onTransitionAnimationEnd(true, false);
            fragment.onBecomeFullyVisible();
        } else if (this.useAlphaAnimations && this.fragmentsStack.size() == 1) {
            presentFragmentInternalRemoveOld(removeLast, currentFragment);
            this.transitionAnimationStartTime = System.currentTimeMillis();
            this.transitionAnimationInProgress = true;
            this.onOpenAnimationEndRunnable = new Runnable() {
                public void run() {
                    fragment.onTransitionAnimationEnd(true, false);
                    fragment.onBecomeFullyVisible();
                }
            };
            ArrayList animators = new ArrayList();
            animators.add(ObjectAnimatorProxy.ofFloat(this, "alpha", 0.0f, 1.0f));
            if (this.backgroundView != null) {
                this.backgroundView.setVisibility(0);
                animators.add(ObjectAnimatorProxy.ofFloat(this.backgroundView, "alpha", 0.0f, 1.0f));
            }
            fragment.onTransitionAnimationStart(true, false);
            this.currentAnimation = new AnimatorSetProxy();
            this.currentAnimation.playTogether(animators);
            this.currentAnimation.setInterpolator(this.accelerateDecelerateInterpolator);
            this.currentAnimation.setDuration(200);
            this.currentAnimation.addListener(new C14964());
            this.currentAnimation.start();
        } else {
            this.transitionAnimationStartTime = System.currentTimeMillis();
            this.transitionAnimationInProgress = true;
            this.onOpenAnimationEndRunnable = new Runnable() {
                public void run() {
                    if (VERSION.SDK_INT > 15) {
                        ActionBarLayout.this.containerView.setLayerType(0, null);
                        ActionBarLayout.this.containerViewBack.setLayerType(0, null);
                    }
                    ActionBarLayout.this.presentFragmentInternalRemoveOld(removeLast, currentFragment);
                    fragment.onTransitionAnimationEnd(true, false);
                    fragment.onBecomeFullyVisible();
                    ViewProxy.setTranslationX(ActionBarLayout.this.containerView, 0.0f);
                }
            };
            fragment.onTransitionAnimationStart(true, false);
            AnimatorSetProxy animation = fragment.onCustomTransitionAnimation(true, new C06926());
            if (animation == null) {
                ViewProxy.setAlpha(this.containerView, 0.0f);
                ViewProxy.setTranslationX(this.containerView, 48.0f);
                startLayoutAnimation(true, true);
            } else if (VERSION.SDK_INT > 15) {
                ViewProxy.setAlpha(this.containerView, 1.0f);
                ViewProxy.setTranslationX(this.containerView, 0.0f);
                this.currentAnimation = animation;
            } else {
                ViewProxy.setAlpha(this.containerView, 1.0f);
                ViewProxy.setTranslationX(this.containerView, 0.0f);
                this.currentAnimation = animation;
            }
        }
        return true;
    }

    public boolean addFragmentToStack(BaseFragment fragment) {
        return addFragmentToStack(fragment, -1);
    }

    public boolean addFragmentToStack(BaseFragment fragment, int position) {
        if ((this.delegate != null && !this.delegate.needAddFragmentToStack(fragment, this)) || !fragment.onFragmentCreate()) {
            return false;
        }
        fragment.setParentLayout(this);
        if (position == -1) {
            if (!this.fragmentsStack.isEmpty()) {
                ViewGroup parent;
                BaseFragment previousFragment = (BaseFragment) this.fragmentsStack.get(this.fragmentsStack.size() - 1);
                previousFragment.onPause();
                if (previousFragment.actionBar != null) {
                    parent = (ViewGroup) previousFragment.actionBar.getParent();
                    if (parent != null) {
                        parent.removeView(previousFragment.actionBar);
                    }
                }
                if (previousFragment.fragmentView != null) {
                    parent = (ViewGroup) previousFragment.fragmentView.getParent();
                    if (parent != null) {
                        parent.removeView(previousFragment.fragmentView);
                    }
                }
            }
            this.fragmentsStack.add(fragment);
        } else {
            this.fragmentsStack.add(position, fragment);
        }
        return true;
    }

    private void closeLastFragmentInternalRemoveOld(BaseFragment fragment) {
        fragment.onPause();
        fragment.onFragmentDestroy();
        fragment.setParentLayout(null);
        this.fragmentsStack.remove(fragment);
        this.containerViewBack.setVisibility(8);
        bringChildToFront(this.containerView);
    }

    public void closeLastFragment(boolean animated) {
        if ((this.delegate == null || this.delegate.needCloseLastFragment(this)) && !checkTransitionAnimation() && !this.fragmentsStack.isEmpty()) {
            if (this.parentActivity.getCurrentFocus() != null) {
                AndroidUtilities.hideKeyboard(this.parentActivity.getCurrentFocus());
            }
            setInnerTranslationX(0.0f);
            boolean needAnimation = VERSION.SDK_INT > 10 && animated && this.parentActivity.getSharedPreferences("mainconfig", 0).getBoolean("view_animations", true);
            final BaseFragment currentFragment = (BaseFragment) this.fragmentsStack.get(this.fragmentsStack.size() - 1);
            BaseFragment previousFragment = null;
            if (this.fragmentsStack.size() > 1) {
                previousFragment = (BaseFragment) this.fragmentsStack.get(this.fragmentsStack.size() - 2);
            }
            if (previousFragment != null) {
                ViewGroup parent;
                LinearLayoutContainer temp = this.containerView;
                this.containerView = this.containerViewBack;
                this.containerViewBack = temp;
                this.containerView.setVisibility(0);
                previousFragment.setParentLayout(this);
                View fragmentView = previousFragment.fragmentView;
                if (fragmentView == null) {
                    fragmentView = previousFragment.createView(this.parentActivity);
                } else {
                    parent = (ViewGroup) fragmentView.getParent();
                    if (parent != null) {
                        parent.removeView(fragmentView);
                    }
                }
                if (previousFragment.actionBar != null && previousFragment.actionBar.getAddToContainer()) {
                    if (this.removeActionBarExtraHeight) {
                        previousFragment.actionBar.setOccupyStatusBar(false);
                    }
                    parent = (ViewGroup) previousFragment.actionBar.getParent();
                    if (parent != null) {
                        parent.removeView(previousFragment.actionBar);
                    }
                    this.containerView.addView(previousFragment.actionBar);
                    previousFragment.actionBar.setTitleOverlayText(this.titleOverlayText);
                }
                this.containerView.addView(fragmentView);
                ViewGroup.LayoutParams layoutParams = fragmentView.getLayoutParams();
                layoutParams.width = -1;
                layoutParams.height = -1;
                fragmentView.setLayoutParams(layoutParams);
                previousFragment.onTransitionAnimationStart(true, true);
                currentFragment.onTransitionAnimationStart(false, false);
                previousFragment.onResume();
                this.currentActionBar = previousFragment.actionBar;
                if (!previousFragment.hasOwnBackground && fragmentView.getBackground() == null) {
                    fragmentView.setBackgroundColor(-1);
                }
                if (!needAnimation) {
                    closeLastFragmentInternalRemoveOld(currentFragment);
                }
                if (needAnimation) {
                    this.transitionAnimationStartTime = System.currentTimeMillis();
                    this.transitionAnimationInProgress = true;
                    final BaseFragment previousFragmentFinal = previousFragment;
                    this.onCloseAnimationEndRunnable = new Runnable() {
                        public void run() {
                            if (VERSION.SDK_INT > 15) {
                                ActionBarLayout.this.containerView.setLayerType(0, null);
                                ActionBarLayout.this.containerViewBack.setLayerType(0, null);
                            }
                            ActionBarLayout.this.closeLastFragmentInternalRemoveOld(currentFragment);
                            ViewProxy.setTranslationX(ActionBarLayout.this.containerViewBack, 0.0f);
                            currentFragment.onTransitionAnimationEnd(false, false);
                            previousFragmentFinal.onTransitionAnimationEnd(true, true);
                            previousFragmentFinal.onBecomeFullyVisible();
                        }
                    };
                    AnimatorSetProxy animation = currentFragment.onCustomTransitionAnimation(false, new C06948());
                    if (animation == null) {
                        startLayoutAnimation(false, true);
                        return;
                    }
                    if (VERSION.SDK_INT > 15) {
                        this.currentAnimation = animation;
                    } else {
                        this.currentAnimation = animation;
                    }
                    return;
                }
                currentFragment.onTransitionAnimationEnd(false, false);
                previousFragment.onTransitionAnimationEnd(true, true);
                previousFragment.onBecomeFullyVisible();
            } else if (this.useAlphaAnimations) {
                this.transitionAnimationStartTime = System.currentTimeMillis();
                this.transitionAnimationInProgress = true;
                this.onCloseAnimationEndRunnable = new Runnable() {
                    public void run() {
                        ActionBarLayout.this.removeFragmentFromStackInternal(currentFragment);
                        ActionBarLayout.this.setVisibility(8);
                        if (ActionBarLayout.this.backgroundView != null) {
                            ActionBarLayout.this.backgroundView.setVisibility(8);
                        }
                        if (ActionBarLayout.this.drawerLayoutContainer != null) {
                            ActionBarLayout.this.drawerLayoutContainer.setAllowOpenDrawer(true, false);
                        }
                    }
                };
                ArrayList animators = new ArrayList();
                animators.add(ObjectAnimatorProxy.ofFloat(this, "alpha", 1.0f, 0.0f));
                if (this.backgroundView != null) {
                    animators.add(ObjectAnimatorProxy.ofFloat(this.backgroundView, "alpha", 1.0f, 0.0f));
                }
                this.currentAnimation = new AnimatorSetProxy();
                this.currentAnimation.playTogether(animators);
                this.currentAnimation.setInterpolator(this.accelerateDecelerateInterpolator);
                this.currentAnimation.setDuration(200);
                this.currentAnimation.addListener(new AnimatorListenerAdapterProxy() {
                    public void onAnimationStart(Object animation) {
                        ActionBarLayout.this.transitionAnimationStartTime = System.currentTimeMillis();
                    }

                    public void onAnimationEnd(Object animation) {
                        ActionBarLayout.this.onAnimationEndCheck(false);
                    }

                    public void onAnimationCancel(Object animation) {
                        ActionBarLayout.this.onAnimationEndCheck(false);
                    }
                });
                this.currentAnimation.start();
            } else {
                removeFragmentFromStackInternal(currentFragment);
                setVisibility(8);
                if (this.backgroundView != null) {
                    this.backgroundView.setVisibility(8);
                }
            }
        }
    }

    public void showLastFragment() {
        if (!this.fragmentsStack.isEmpty()) {
            BaseFragment previousFragment;
            ViewGroup parent;
            for (int a = 0; a < this.fragmentsStack.size() - 1; a++) {
                previousFragment = (BaseFragment) this.fragmentsStack.get(a);
                if (previousFragment.actionBar != null) {
                    parent = (ViewGroup) previousFragment.actionBar.getParent();
                    if (parent != null) {
                        parent.removeView(previousFragment.actionBar);
                    }
                }
                if (previousFragment.fragmentView != null) {
                    parent = (ViewGroup) previousFragment.fragmentView.getParent();
                    if (parent != null) {
                        previousFragment.onPause();
                        parent.removeView(previousFragment.fragmentView);
                    }
                }
            }
            previousFragment = (BaseFragment) this.fragmentsStack.get(this.fragmentsStack.size() - 1);
            previousFragment.setParentLayout(this);
            View fragmentView = previousFragment.fragmentView;
            if (fragmentView == null) {
                fragmentView = previousFragment.createView(this.parentActivity);
            } else {
                parent = (ViewGroup) fragmentView.getParent();
                if (parent != null) {
                    parent.removeView(fragmentView);
                }
            }
            if (previousFragment.actionBar != null && previousFragment.actionBar.getAddToContainer()) {
                if (this.removeActionBarExtraHeight) {
                    previousFragment.actionBar.setOccupyStatusBar(false);
                }
                parent = (ViewGroup) previousFragment.actionBar.getParent();
                if (parent != null) {
                    parent.removeView(previousFragment.actionBar);
                }
                this.containerView.addView(previousFragment.actionBar);
                previousFragment.actionBar.setTitleOverlayText(this.titleOverlayText);
            }
            this.containerView.addView(fragmentView);
            ViewGroup.LayoutParams layoutParams = fragmentView.getLayoutParams();
            layoutParams.width = -1;
            layoutParams.height = -1;
            fragmentView.setLayoutParams(layoutParams);
            previousFragment.onResume();
            this.currentActionBar = previousFragment.actionBar;
            if (!previousFragment.hasOwnBackground && fragmentView.getBackground() == null) {
                fragmentView.setBackgroundColor(-1);
            }
        }
    }

    private void removeFragmentFromStackInternal(BaseFragment fragment) {
        fragment.onPause();
        fragment.onFragmentDestroy();
        fragment.setParentLayout(null);
        this.fragmentsStack.remove(fragment);
    }

    public void removeFragmentFromStack(BaseFragment fragment) {
        if (this.useAlphaAnimations && this.fragmentsStack.size() == 1 && AndroidUtilities.isTablet()) {
            closeLastFragment(true);
        } else {
            removeFragmentFromStackInternal(fragment);
        }
    }

    public void removeAllFragments() {
        int a = 0;
        while (this.fragmentsStack.size() > 0) {
            removeFragmentFromStackInternal((BaseFragment) this.fragmentsStack.get(a));
            a = (a - 1) + 1;
        }
    }

    public void rebuildAllFragmentViews(boolean last) {
        int a = 0;
        while (true) {
            if (a >= this.fragmentsStack.size() - (last ? 0 : 1)) {
                break;
            }
            ((BaseFragment) this.fragmentsStack.get(a)).clearViews();
            ((BaseFragment) this.fragmentsStack.get(a)).setParentLayout(this);
            a++;
        }
        if (this.delegate != null) {
            this.delegate.onRebuildAllFragments(this);
        }
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (!(keyCode != 82 || checkTransitionAnimation() || this.startedTracking || this.currentActionBar == null)) {
            this.currentActionBar.onMenuButtonPressed();
        }
        return super.onKeyUp(keyCode, event);
    }

    public void onActionModeStarted(Object mode) {
        if (this.currentActionBar != null) {
            this.currentActionBar.setVisibility(8);
        }
        this.inActionMode = true;
    }

    public void onActionModeFinished(Object mode) {
        if (this.currentActionBar != null) {
            this.currentActionBar.setVisibility(0);
        }
        this.inActionMode = false;
    }

    private void onCloseAnimationEnd(boolean post) {
        if (this.transitionAnimationInProgress && this.onCloseAnimationEndRunnable != null) {
            this.transitionAnimationInProgress = false;
            this.transitionAnimationStartTime = 0;
            if (post) {
                new Handler().post(new Runnable() {
                    public void run() {
                        ActionBarLayout.this.onCloseAnimationEndRunnable.run();
                        ActionBarLayout.this.onCloseAnimationEndRunnable = null;
                    }
                });
                return;
            }
            this.onCloseAnimationEndRunnable.run();
            this.onCloseAnimationEndRunnable = null;
        }
    }

    private void onOpenAnimationEnd(boolean post) {
        if (this.transitionAnimationInProgress && this.onOpenAnimationEndRunnable != null) {
            this.transitionAnimationInProgress = false;
            this.transitionAnimationStartTime = 0;
            if (post) {
                new Handler().post(new Runnable() {
                    public void run() {
                        ActionBarLayout.this.onOpenAnimationEndRunnable.run();
                        ActionBarLayout.this.onOpenAnimationEndRunnable = null;
                    }
                });
                return;
            }
            this.onOpenAnimationEndRunnable.run();
            this.onOpenAnimationEndRunnable = null;
        }
    }

    public void startActivityForResult(Intent intent, int requestCode) {
        if (this.parentActivity != null) {
            if (this.transitionAnimationInProgress) {
                if (this.currentAnimation != null) {
                    this.currentAnimation.cancel();
                    this.currentAnimation = null;
                }
                if (this.onCloseAnimationEndRunnable != null) {
                    onCloseAnimationEnd(false);
                } else if (this.onOpenAnimationEndRunnable != null) {
                    onOpenAnimationEnd(false);
                }
                this.containerView.invalidate();
                if (intent != null) {
                    this.parentActivity.startActivityForResult(intent, requestCode);
                }
            } else if (intent != null) {
                this.parentActivity.startActivityForResult(intent, requestCode);
            }
        }
    }

    public void setUseAlphaAnimations(boolean value) {
        this.useAlphaAnimations = value;
    }

    public void setBackgroundView(View view) {
        this.backgroundView = view;
    }

    public void setDrawerLayoutContainer(DrawerLayoutContainer layout) {
        this.drawerLayoutContainer = layout;
    }

    public DrawerLayoutContainer getDrawerLayoutContainer() {
        return this.drawerLayoutContainer;
    }

    public void setRemoveActionBarExtraHeight(boolean value) {
        this.removeActionBarExtraHeight = value;
    }

    public void setTitleOverlayText(String text) {
        this.titleOverlayText = text;
        Iterator i$ = this.fragmentsStack.iterator();
        while (i$.hasNext()) {
            BaseFragment fragment = (BaseFragment) i$.next();
            if (fragment.actionBar != null) {
                fragment.actionBar.setTitleOverlayText(this.titleOverlayText);
            }
        }
    }

    public boolean hasOverlappingRendering() {
        return false;
    }
}
