package org.telegram.messenger.AnimationCompat;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.view.animation.Interpolator;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.telegram.messenger.Animation.Animator10;
import org.telegram.messenger.Animation.AnimatorListenerAdapter10;
import org.telegram.messenger.Animation.AnimatorSet10;
import org.telegram.messenger.Animation.View10;

public class AnimatorSetProxy {
    private Object animatorSet;

    public static <T, U> T[] copyOf(U[] original, int newLength, Class<? extends T[]> newType) {
        return copyOfRange(original, 0, newLength, newType);
    }

    public static <T, U> T[] copyOfRange(U[] original, int start, int end, Class<? extends T[]> newType) {
        if (start > end) {
            throw new IllegalArgumentException();
        }
        int originalLength = original.length;
        if (start < 0 || start > originalLength) {
            throw new ArrayIndexOutOfBoundsException();
        }
        int resultLength = end - start;
        Object[] result = (Object[]) ((Object[]) Array.newInstance(newType.getComponentType(), resultLength));
        System.arraycopy(original, start, result, 0, Math.min(resultLength, originalLength - start));
        return result;
    }

    public AnimatorSetProxy() {
        if (View10.NEED_PROXY) {
            this.animatorSet = new AnimatorSet10();
        } else {
            this.animatorSet = new AnimatorSet();
        }
    }

    public void playTogether(Object... items) {
        if (View10.NEED_PROXY) {
            ((AnimatorSet10) this.animatorSet).playTogether((Animator10[]) copyOf(items, items.length, Animator10[].class));
            return;
        }
        ((AnimatorSet) this.animatorSet).playTogether((Animator[]) copyOf(items, items.length, Animator[].class));
    }

    public void playTogether(ArrayList<Object> items) {
        Iterator i$;
        if (View10.NEED_PROXY) {
            Collection animators = new ArrayList();
            i$ = items.iterator();
            while (i$.hasNext()) {
                animators.add((Animator10) i$.next());
            }
            ((AnimatorSet10) this.animatorSet).playTogether(animators);
            return;
        }
        ArrayList<Animator> animators2 = new ArrayList();
        i$ = items.iterator();
        while (i$.hasNext()) {
            animators2.add((Animator) i$.next());
        }
        ((AnimatorSet) this.animatorSet).playTogether(animators2);
    }

    public AnimatorSetProxy setDuration(long duration) {
        if (View10.NEED_PROXY) {
            ((AnimatorSet10) this.animatorSet).setDuration(duration);
        } else {
            ((AnimatorSet) this.animatorSet).setDuration(duration);
        }
        return this;
    }

    public AnimatorSetProxy setStartDelay(long delay) {
        if (View10.NEED_PROXY) {
            ((AnimatorSet10) this.animatorSet).setStartDelay(delay);
        } else {
            ((AnimatorSet) this.animatorSet).setStartDelay(delay);
        }
        return this;
    }

    public void start() {
        if (View10.NEED_PROXY) {
            ((AnimatorSet10) this.animatorSet).start();
        } else {
            ((AnimatorSet) this.animatorSet).start();
        }
    }

    public void cancel() {
        if (View10.NEED_PROXY) {
            ((AnimatorSet10) this.animatorSet).cancel();
        } else {
            ((AnimatorSet) this.animatorSet).cancel();
        }
    }

    public void addListener(AnimatorListenerAdapterProxy listener) {
        if (View10.NEED_PROXY) {
            ((AnimatorSet10) this.animatorSet).addListener((AnimatorListenerAdapter10) listener.animatorListenerAdapter);
        } else {
            ((AnimatorSet) this.animatorSet).addListener((AnimatorListenerAdapter) listener.animatorListenerAdapter);
        }
    }

    public void setInterpolator(Interpolator interpolator) {
        if (View10.NEED_PROXY) {
            ((AnimatorSet10) this.animatorSet).setInterpolator(interpolator);
        } else {
            ((AnimatorSet) this.animatorSet).setInterpolator(interpolator);
        }
    }

    public boolean equals(Object o) {
        return this.animatorSet == o;
    }
}
