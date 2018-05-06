package org.telegram.messenger.Animation;

import android.os.Looper;
import android.util.AndroidRuntimeException;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.Animation.Animator10.AnimatorListener;

public class ValueAnimator extends Animator10 {
    public static final int INFINITE = -1;
    public static final int RESTART = 1;
    public static final int REVERSE = 2;
    static final int RUNNING = 1;
    static final int SEEKED = 2;
    static final int STOPPED = 0;
    protected static ThreadLocal<AnimationHandler> sAnimationHandler = new ThreadLocal();
    private static final Interpolator sDefaultInterpolator = new AccelerateDecelerateInterpolator();
    private static float sDurationScale = 1.0f;
    private float mCurrentFraction = 0.0f;
    private int mCurrentIteration = 0;
    private long mDelayStartTime;
    private long mDuration = ((long) (BitmapDescriptorFactory.HUE_MAGENTA * sDurationScale));
    boolean mInitialized = false;
    private Interpolator mInterpolator = sDefaultInterpolator;
    private long mPauseTime;
    private boolean mPlayingBackwards = false;
    int mPlayingState = 0;
    private int mRepeatCount = 0;
    private int mRepeatMode = 1;
    private boolean mResumed = false;
    private boolean mRunning = false;
    long mSeekTime = -1;
    private long mStartDelay = 0;
    private boolean mStartListenersCalled = false;
    long mStartTime;
    private boolean mStarted = false;
    private boolean mStartedDelay = false;
    private long mUnscaledDuration = 300;
    private long mUnscaledStartDelay = 0;
    private ArrayList<AnimatorUpdateListener> mUpdateListeners = null;
    PropertyValuesHolder[] mValues;
    HashMap<String, PropertyValuesHolder> mValuesMap;

    protected static class AnimationHandler implements Runnable {
        private boolean mAnimationScheduled;
        protected final ArrayList<ValueAnimator> mAnimations = new ArrayList();
        protected final ArrayList<ValueAnimator> mDelayedAnims = new ArrayList();
        private final ArrayList<ValueAnimator> mEndingAnims = new ArrayList();
        protected final ArrayList<ValueAnimator> mPendingAnimations = new ArrayList();
        private final ArrayList<ValueAnimator> mReadyAnims = new ArrayList();
        private final ArrayList<ValueAnimator> mTmpAnimations = new ArrayList();

        protected AnimationHandler() {
        }

        public void start() {
            scheduleAnimation();
        }

        private void doAnimationFrame(long frameTime) {
            Iterator i$;
            while (this.mPendingAnimations.size() > 0) {
                ArrayList<ValueAnimator> pendingCopy = (ArrayList) this.mPendingAnimations.clone();
                this.mPendingAnimations.clear();
                int count = pendingCopy.size();
                i$ = pendingCopy.iterator();
                while (i$.hasNext()) {
                    ValueAnimator anim = (ValueAnimator) i$.next();
                    if (anim.mStartDelay == 0) {
                        anim.startAnimation(this);
                    } else {
                        this.mDelayedAnims.add(anim);
                    }
                }
            }
            int numDelayedAnims = this.mDelayedAnims.size();
            i$ = this.mDelayedAnims.iterator();
            while (i$.hasNext()) {
                anim = (ValueAnimator) i$.next();
                if (anim.delayedAnimationFrame(frameTime)) {
                    this.mReadyAnims.add(anim);
                }
            }
            if (this.mReadyAnims.size() > 0) {
                i$ = this.mReadyAnims.iterator();
                while (i$.hasNext()) {
                    anim = (ValueAnimator) i$.next();
                    anim.startAnimation(this);
                    anim.mRunning = true;
                    this.mDelayedAnims.remove(anim);
                }
                this.mReadyAnims.clear();
            }
            int numAnims = this.mAnimations.size();
            i$ = this.mAnimations.iterator();
            while (i$.hasNext()) {
                this.mTmpAnimations.add((ValueAnimator) i$.next());
            }
            for (int i = 0; i < numAnims; i++) {
                anim = (ValueAnimator) this.mTmpAnimations.get(i);
                if (this.mAnimations.contains(anim) && anim.doAnimationFrame(frameTime)) {
                    this.mEndingAnims.add(anim);
                }
            }
            this.mTmpAnimations.clear();
            if (this.mEndingAnims.size() > 0) {
                i$ = this.mEndingAnims.iterator();
                while (i$.hasNext()) {
                    ((ValueAnimator) i$.next()).endAnimation(this);
                }
                this.mEndingAnims.clear();
            }
            if (!this.mAnimations.isEmpty() || !this.mDelayedAnims.isEmpty()) {
                scheduleAnimation();
            }
        }

        public void run() {
            this.mAnimationScheduled = false;
            doAnimationFrame(System.nanoTime() / 1000000);
        }

        private void scheduleAnimation() {
            if (!this.mAnimationScheduled) {
                AndroidUtilities.runOnUIThread(this);
                this.mAnimationScheduled = true;
            }
        }
    }

    public interface AnimatorUpdateListener {
        void onAnimationUpdate(ValueAnimator valueAnimator);
    }

    public static void setDurationScale(float durationScale) {
        sDurationScale = durationScale;
    }

    public static float getDurationScale() {
        return sDurationScale;
    }

    public static ValueAnimator ofInt(int... values) {
        ValueAnimator anim = new ValueAnimator();
        anim.setIntValues(values);
        return anim;
    }

    public static ValueAnimator ofFloat(float... values) {
        ValueAnimator anim = new ValueAnimator();
        anim.setFloatValues(values);
        return anim;
    }

    public static ValueAnimator ofPropertyValuesHolder(PropertyValuesHolder... values) {
        ValueAnimator anim = new ValueAnimator();
        anim.setValues(values);
        return anim;
    }

    public static ValueAnimator ofObject(TypeEvaluator evaluator, Object... values) {
        ValueAnimator anim = new ValueAnimator();
        anim.setObjectValues(values);
        anim.setEvaluator(evaluator);
        return anim;
    }

    public void setIntValues(int... values) {
        if (values != null && values.length != 0) {
            if (this.mValues == null || this.mValues.length == 0) {
                setValues(PropertyValuesHolder.ofInt("", values));
            } else {
                this.mValues[0].setIntValues(values);
            }
            this.mInitialized = false;
        }
    }

    public void setFloatValues(float... values) {
        if (values != null && values.length != 0) {
            if (this.mValues == null || this.mValues.length == 0) {
                setValues(PropertyValuesHolder.ofFloat("", values));
            } else {
                this.mValues[0].setFloatValues(values);
            }
            this.mInitialized = false;
        }
    }

    public void setObjectValues(Object... values) {
        if (values != null && values.length != 0) {
            if (this.mValues == null || this.mValues.length == 0) {
                setValues(PropertyValuesHolder.ofObject("", null, values));
            } else {
                this.mValues[0].setObjectValues(values);
            }
            this.mInitialized = false;
        }
    }

    public void setValues(PropertyValuesHolder... values) {
        int numValues = values.length;
        this.mValues = values;
        this.mValuesMap = new HashMap(numValues);
        for (PropertyValuesHolder valuesHolder : values) {
            this.mValuesMap.put(valuesHolder.getPropertyName(), valuesHolder);
        }
        this.mInitialized = false;
    }

    public PropertyValuesHolder[] getValues() {
        return this.mValues;
    }

    void initAnimation() {
        if (!this.mInitialized) {
            int numValues = this.mValues.length;
            for (PropertyValuesHolder mValue : this.mValues) {
                mValue.init();
            }
            this.mInitialized = true;
        }
    }

    public ValueAnimator setDuration(long duration) {
        if (duration < 0) {
            throw new IllegalArgumentException("Animators cannot have negative duration: " + duration);
        }
        this.mUnscaledDuration = duration;
        this.mDuration = (long) (((float) duration) * sDurationScale);
        return this;
    }

    public long getDuration() {
        return this.mUnscaledDuration;
    }

    public void setCurrentPlayTime(long playTime) {
        initAnimation();
        long currentTime = AnimationUtils.currentAnimationTimeMillis();
        if (this.mPlayingState != 1) {
            this.mSeekTime = playTime;
            this.mPlayingState = 2;
        }
        this.mStartTime = currentTime - playTime;
        doAnimationFrame(currentTime);
    }

    public long getCurrentPlayTime() {
        if (!this.mInitialized || this.mPlayingState == 0) {
            return 0;
        }
        return AnimationUtils.currentAnimationTimeMillis() - this.mStartTime;
    }

    public long getStartDelay() {
        return this.mUnscaledStartDelay;
    }

    public void setStartDelay(long startDelay) {
        this.mStartDelay = (long) (((float) startDelay) * sDurationScale);
        this.mUnscaledStartDelay = startDelay;
    }

    public Object getAnimatedValue() {
        if (this.mValues == null || this.mValues.length <= 0) {
            return null;
        }
        return this.mValues[0].getAnimatedValue();
    }

    public Object getAnimatedValue(String propertyName) {
        PropertyValuesHolder valuesHolder = (PropertyValuesHolder) this.mValuesMap.get(propertyName);
        if (valuesHolder != null) {
            return valuesHolder.getAnimatedValue();
        }
        return null;
    }

    public void setRepeatCount(int value) {
        this.mRepeatCount = value;
    }

    public int getRepeatCount() {
        return this.mRepeatCount;
    }

    public void setRepeatMode(int value) {
        this.mRepeatMode = value;
    }

    public int getRepeatMode() {
        return this.mRepeatMode;
    }

    public void addUpdateListener(AnimatorUpdateListener listener) {
        if (this.mUpdateListeners == null) {
            this.mUpdateListeners = new ArrayList();
        }
        this.mUpdateListeners.add(listener);
    }

    public void removeAllUpdateListeners() {
        if (this.mUpdateListeners != null) {
            this.mUpdateListeners.clear();
            this.mUpdateListeners = null;
        }
    }

    public void removeUpdateListener(AnimatorUpdateListener listener) {
        if (this.mUpdateListeners != null) {
            this.mUpdateListeners.remove(listener);
            if (this.mUpdateListeners.size() == 0) {
                this.mUpdateListeners = null;
            }
        }
    }

    public void setInterpolator(Interpolator value) {
        if (value != null) {
            this.mInterpolator = value;
        } else {
            this.mInterpolator = new LinearInterpolator();
        }
    }

    public Interpolator getInterpolator() {
        return this.mInterpolator;
    }

    public void setEvaluator(TypeEvaluator value) {
        if (value != null && this.mValues != null && this.mValues.length > 0) {
            this.mValues[0].setEvaluator(value);
        }
    }

    private void notifyStartListeners() {
        if (!(this.mListeners == null || this.mStartListenersCalled)) {
            ArrayList<AnimatorListener> tmpListeners = (ArrayList) this.mListeners.clone();
            int numListeners = tmpListeners.size();
            Iterator i$ = tmpListeners.iterator();
            while (i$.hasNext()) {
                ((AnimatorListener) i$.next()).onAnimationStart(this);
            }
        }
        this.mStartListenersCalled = true;
    }

    private void start(boolean playBackwards) {
        if (Looper.myLooper() == null) {
            throw new AndroidRuntimeException("Animators may only be run on Looper threads");
        }
        this.mPlayingBackwards = playBackwards;
        this.mCurrentIteration = 0;
        this.mPlayingState = 0;
        this.mStarted = true;
        this.mStartedDelay = false;
        this.mPaused = false;
        AnimationHandler animationHandler = getOrCreateAnimationHandler();
        animationHandler.mPendingAnimations.add(this);
        if (this.mStartDelay == 0) {
            setCurrentPlayTime(0);
            this.mPlayingState = 0;
            this.mRunning = true;
            notifyStartListeners();
        }
        animationHandler.start();
    }

    public void start() {
        start(false);
    }

    public void cancel() {
        AnimationHandler handler = getOrCreateAnimationHandler();
        if (this.mPlayingState != 0 || handler.mPendingAnimations.contains(this) || handler.mDelayedAnims.contains(this)) {
            if ((this.mStarted || this.mRunning) && this.mListeners != null) {
                if (!this.mRunning) {
                    notifyStartListeners();
                }
                Iterator i$ = ((ArrayList) this.mListeners.clone()).iterator();
                while (i$.hasNext()) {
                    ((AnimatorListener) i$.next()).onAnimationCancel(this);
                }
            }
            endAnimation(handler);
        }
    }

    public void end() {
        AnimationHandler handler = getOrCreateAnimationHandler();
        if (!handler.mAnimations.contains(this) && !handler.mPendingAnimations.contains(this)) {
            this.mStartedDelay = false;
            startAnimation(handler);
            this.mStarted = true;
        } else if (!this.mInitialized) {
            initAnimation();
        }
        animateValue(this.mPlayingBackwards ? 0.0f : 1.0f);
        endAnimation(handler);
    }

    public void resume() {
        if (this.mPaused) {
            this.mResumed = true;
        }
        super.resume();
    }

    public void pause() {
        boolean previouslyPaused = this.mPaused;
        super.pause();
        if (!previouslyPaused && this.mPaused) {
            this.mPauseTime = -1;
            this.mResumed = false;
        }
    }

    public boolean isRunning() {
        return this.mPlayingState == 1 || this.mRunning;
    }

    public boolean isStarted() {
        return this.mStarted;
    }

    public void reverse() {
        this.mPlayingBackwards = !this.mPlayingBackwards;
        if (this.mPlayingState == 1) {
            long currentTime = AnimationUtils.currentAnimationTimeMillis();
            this.mStartTime = currentTime - (this.mDuration - (currentTime - this.mStartTime));
        } else if (this.mStarted) {
            end();
        } else {
            start(true);
        }
    }

    private void endAnimation(AnimationHandler handler) {
        handler.mAnimations.remove(this);
        handler.mPendingAnimations.remove(this);
        handler.mDelayedAnims.remove(this);
        this.mPlayingState = 0;
        this.mPaused = false;
        if ((this.mStarted || this.mRunning) && this.mListeners != null) {
            if (!this.mRunning) {
                notifyStartListeners();
            }
            ArrayList<AnimatorListener> tmpListeners = (ArrayList) this.mListeners.clone();
            int numListeners = tmpListeners.size();
            Iterator i$ = tmpListeners.iterator();
            while (i$.hasNext()) {
                ((AnimatorListener) i$.next()).onAnimationEnd(this);
            }
        }
        this.mRunning = false;
        this.mStarted = false;
        this.mStartListenersCalled = false;
        this.mPlayingBackwards = false;
    }

    private void startAnimation(AnimationHandler handler) {
        initAnimation();
        handler.mAnimations.add(this);
        if (this.mStartDelay > 0 && this.mListeners != null) {
            notifyStartListeners();
        }
    }

    private boolean delayedAnimationFrame(long currentTime) {
        if (!this.mStartedDelay) {
            this.mStartedDelay = true;
            this.mDelayStartTime = currentTime;
            return false;
        } else if (!this.mPaused) {
            if (this.mResumed) {
                this.mResumed = false;
                if (this.mPauseTime > 0) {
                    this.mDelayStartTime += currentTime - this.mPauseTime;
                }
            }
            long deltaTime = currentTime - this.mDelayStartTime;
            if (deltaTime <= this.mStartDelay) {
                return false;
            }
            this.mStartTime = currentTime - (deltaTime - this.mStartDelay);
            this.mPlayingState = 1;
            return true;
        } else if (this.mPauseTime >= 0) {
            return false;
        } else {
            this.mPauseTime = currentTime;
            return false;
        }
    }

    boolean animationFrame(long currentTime) {
        boolean done = false;
        switch (this.mPlayingState) {
            case 1:
            case 2:
                float fraction;
                if (this.mDuration > 0) {
                    fraction = ((float) (currentTime - this.mStartTime)) / ((float) this.mDuration);
                } else {
                    fraction = 1.0f;
                }
                if (fraction >= 1.0f) {
                    if (this.mCurrentIteration < this.mRepeatCount || this.mRepeatCount == -1) {
                        if (this.mListeners != null) {
                            int numListeners = this.mListeners.size();
                            Iterator i$ = this.mListeners.iterator();
                            while (i$.hasNext()) {
                                ((AnimatorListener) i$.next()).onAnimationRepeat(this);
                            }
                        }
                        if (this.mRepeatMode == 2) {
                            this.mPlayingBackwards = !this.mPlayingBackwards;
                        }
                        this.mCurrentIteration += (int) fraction;
                        fraction %= 1.0f;
                        this.mStartTime += this.mDuration;
                    } else {
                        done = true;
                        fraction = Math.min(fraction, 1.0f);
                    }
                }
                if (this.mPlayingBackwards) {
                    fraction = 1.0f - fraction;
                }
                animateValue(fraction);
                break;
        }
        return done;
    }

    final boolean doAnimationFrame(long frameTime) {
        if (this.mPlayingState == 0) {
            this.mPlayingState = 1;
            if (this.mSeekTime < 0) {
                this.mStartTime = frameTime;
            } else {
                this.mStartTime = frameTime - this.mSeekTime;
                this.mSeekTime = -1;
            }
        }
        if (!this.mPaused) {
            if (this.mResumed) {
                this.mResumed = false;
                if (this.mPauseTime > 0) {
                    this.mStartTime += frameTime - this.mPauseTime;
                }
            }
            return animationFrame(Math.max(frameTime, this.mStartTime));
        } else if (this.mPauseTime >= 0) {
            return false;
        } else {
            this.mPauseTime = frameTime;
            return false;
        }
    }

    public float getAnimatedFraction() {
        return this.mCurrentFraction;
    }

    void animateValue(float fraction) {
        fraction = this.mInterpolator.getInterpolation(fraction);
        this.mCurrentFraction = fraction;
        int numValues = this.mValues.length;
        for (PropertyValuesHolder mValue : this.mValues) {
            mValue.calculateValue(fraction);
        }
        if (this.mUpdateListeners != null) {
            int numListeners = this.mUpdateListeners.size();
            Iterator i$ = this.mUpdateListeners.iterator();
            while (i$.hasNext()) {
                ((AnimatorUpdateListener) i$.next()).onAnimationUpdate(this);
            }
        }
    }

    public ValueAnimator clone() {
        ValueAnimator anim = (ValueAnimator) super.clone();
        if (this.mUpdateListeners != null) {
            ArrayList<AnimatorUpdateListener> oldListeners = this.mUpdateListeners;
            anim.mUpdateListeners = new ArrayList();
            int numListeners = oldListeners.size();
            Iterator i$ = oldListeners.iterator();
            while (i$.hasNext()) {
                anim.mUpdateListeners.add((AnimatorUpdateListener) i$.next());
            }
        }
        anim.mSeekTime = -1;
        anim.mPlayingBackwards = false;
        anim.mCurrentIteration = 0;
        anim.mInitialized = false;
        anim.mPlayingState = 0;
        anim.mStartedDelay = false;
        PropertyValuesHolder[] oldValues = this.mValues;
        if (oldValues != null) {
            int numValues = oldValues.length;
            anim.mValues = new PropertyValuesHolder[numValues];
            anim.mValuesMap = new HashMap(numValues);
            for (int i = 0; i < numValues; i++) {
                PropertyValuesHolder newValuesHolder = oldValues[i].clone();
                anim.mValues[i] = newValuesHolder;
                anim.mValuesMap.put(newValuesHolder.getPropertyName(), newValuesHolder);
            }
        }
        return anim;
    }

    public static int getCurrentAnimationsCount() {
        AnimationHandler handler = (AnimationHandler) sAnimationHandler.get();
        return handler != null ? handler.mAnimations.size() : 0;
    }

    public static void clearAllAnimations() {
        AnimationHandler handler = (AnimationHandler) sAnimationHandler.get();
        if (handler != null) {
            handler.mAnimations.clear();
            handler.mPendingAnimations.clear();
            handler.mDelayedAnims.clear();
        }
    }

    private static AnimationHandler getOrCreateAnimationHandler() {
        AnimationHandler handler = (AnimationHandler) sAnimationHandler.get();
        if (handler != null) {
            return handler;
        }
        handler = new AnimationHandler();
        sAnimationHandler.set(handler);
        return handler;
    }
}
