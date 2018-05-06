package org.telegram.messenger.Animation;

import android.view.animation.Interpolator;
import java.util.ArrayList;
import java.util.Iterator;

public abstract class Animator10 implements Cloneable {
    ArrayList<AnimatorListener> mListeners = null;
    ArrayList<AnimatorPauseListener> mPauseListeners = null;
    boolean mPaused = false;

    public interface AnimatorListener {
        void onAnimationCancel(Animator10 animator10);

        void onAnimationEnd(Animator10 animator10);

        void onAnimationRepeat(Animator10 animator10);

        void onAnimationStart(Animator10 animator10);
    }

    public interface AnimatorPauseListener {
        void onAnimationPause(Animator10 animator10);

        void onAnimationResume(Animator10 animator10);
    }

    public abstract long getDuration();

    public abstract long getStartDelay();

    public abstract boolean isRunning();

    public abstract Animator10 setDuration(long j);

    public abstract void setInterpolator(Interpolator interpolator);

    public abstract void setStartDelay(long j);

    public void start() {
    }

    public void cancel() {
    }

    public void end() {
    }

    public void pause() {
        if (isStarted() && !this.mPaused) {
            this.mPaused = true;
            if (this.mPauseListeners != null) {
                ArrayList<AnimatorPauseListener> tmpListeners = (ArrayList) this.mPauseListeners.clone();
                int numListeners = tmpListeners.size();
                Iterator i$ = tmpListeners.iterator();
                while (i$.hasNext()) {
                    ((AnimatorPauseListener) i$.next()).onAnimationPause(this);
                }
            }
        }
    }

    public void resume() {
        if (this.mPaused) {
            this.mPaused = false;
            if (this.mPauseListeners != null) {
                ArrayList<AnimatorPauseListener> tmpListeners = (ArrayList) this.mPauseListeners.clone();
                int numListeners = tmpListeners.size();
                Iterator i$ = tmpListeners.iterator();
                while (i$.hasNext()) {
                    ((AnimatorPauseListener) i$.next()).onAnimationResume(this);
                }
            }
        }
    }

    public boolean isPaused() {
        return this.mPaused;
    }

    public boolean isStarted() {
        return isRunning();
    }

    public Interpolator getInterpolator() {
        return null;
    }

    public void addListener(AnimatorListener listener) {
        if (this.mListeners == null) {
            this.mListeners = new ArrayList();
        }
        this.mListeners.add(listener);
    }

    public void removeListener(AnimatorListener listener) {
        if (this.mListeners != null) {
            this.mListeners.remove(listener);
            if (this.mListeners.size() == 0) {
                this.mListeners = null;
            }
        }
    }

    public ArrayList<AnimatorListener> getListeners() {
        return this.mListeners;
    }

    public void addPauseListener(AnimatorPauseListener listener) {
        if (this.mPauseListeners == null) {
            this.mPauseListeners = new ArrayList();
        }
        this.mPauseListeners.add(listener);
    }

    public void removePauseListener(AnimatorPauseListener listener) {
        if (this.mPauseListeners != null) {
            this.mPauseListeners.remove(listener);
            if (this.mPauseListeners.size() == 0) {
                this.mPauseListeners = null;
            }
        }
    }

    public void removeAllListeners() {
        if (this.mListeners != null) {
            this.mListeners.clear();
            this.mListeners = null;
        }
        if (this.mPauseListeners != null) {
            this.mPauseListeners.clear();
            this.mPauseListeners = null;
        }
    }

    public Animator10 clone() {
        try {
            int numListeners;
            Iterator i$;
            Animator10 anim = (Animator10) super.clone();
            if (this.mListeners != null) {
                ArrayList<AnimatorListener> oldListeners = this.mListeners;
                anim.mListeners = new ArrayList();
                numListeners = oldListeners.size();
                i$ = oldListeners.iterator();
                while (i$.hasNext()) {
                    anim.mListeners.add((AnimatorListener) i$.next());
                }
            }
            if (this.mPauseListeners != null) {
                ArrayList<AnimatorPauseListener> oldListeners2 = this.mPauseListeners;
                anim.mPauseListeners = new ArrayList();
                numListeners = oldListeners2.size();
                i$ = oldListeners2.iterator();
                while (i$.hasNext()) {
                    anim.mPauseListeners.add((AnimatorPauseListener) i$.next());
                }
            }
            return anim;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public void setupStartValues() {
    }

    public void setupEndValues() {
    }

    public void setTarget(Object target) {
    }
}
