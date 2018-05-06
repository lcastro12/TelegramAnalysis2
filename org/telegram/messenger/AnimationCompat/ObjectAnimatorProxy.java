package org.telegram.messenger.AnimationCompat;

import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.view.animation.Interpolator;
import org.telegram.messenger.Animation.AnimatorListenerAdapter10;
import org.telegram.messenger.Animation.ObjectAnimator10;
import org.telegram.messenger.Animation.View10;

public class ObjectAnimatorProxy {
    private Object objectAnimator;

    public ObjectAnimatorProxy(Object animator) {
        this.objectAnimator = animator;
    }

    public static Object ofFloat(Object target, String propertyName, float... values) {
        if (View10.NEED_PROXY) {
            return ObjectAnimator10.ofFloat(target, propertyName, values);
        }
        return ObjectAnimator.ofFloat(target, propertyName, values);
    }

    public static Object ofInt(Object target, String propertyName, int... values) {
        if (View10.NEED_PROXY) {
            return ObjectAnimator10.ofInt(target, propertyName, values);
        }
        return ObjectAnimator.ofInt(target, propertyName, values);
    }

    public static ObjectAnimatorProxy ofFloatProxy(Object target, String propertyName, float... values) {
        if (View10.NEED_PROXY) {
            return new ObjectAnimatorProxy(ObjectAnimator10.ofFloat(target, propertyName, values));
        }
        return new ObjectAnimatorProxy(ObjectAnimator.ofFloat(target, propertyName, values));
    }

    public static ObjectAnimatorProxy ofIntProxy(Object target, String propertyName, int... values) {
        if (View10.NEED_PROXY) {
            return new ObjectAnimatorProxy(ObjectAnimator10.ofInt(target, propertyName, values));
        }
        return new ObjectAnimatorProxy(ObjectAnimator.ofInt(target, propertyName, values));
    }

    public ObjectAnimatorProxy setDuration(long duration) {
        if (View10.NEED_PROXY) {
            ((ObjectAnimator10) this.objectAnimator).setDuration(duration);
        } else {
            ((ObjectAnimator) this.objectAnimator).setDuration(duration);
        }
        return this;
    }

    public void setInterpolator(Interpolator value) {
        if (View10.NEED_PROXY) {
            ((ObjectAnimator10) this.objectAnimator).setInterpolator(value);
        } else {
            ((ObjectAnimator) this.objectAnimator).setInterpolator(value);
        }
    }

    public ObjectAnimatorProxy start() {
        if (View10.NEED_PROXY) {
            ((ObjectAnimator10) this.objectAnimator).start();
        } else {
            ((ObjectAnimator) this.objectAnimator).start();
        }
        return this;
    }

    public void setAutoCancel(boolean cancel) {
        if (View10.NEED_PROXY) {
            ((ObjectAnimator10) this.objectAnimator).setAutoCancel(cancel);
        } else {
            ((ObjectAnimator) this.objectAnimator).setAutoCancel(cancel);
        }
    }

    public boolean isRunning() {
        if (View10.NEED_PROXY) {
            return ((ObjectAnimator10) this.objectAnimator).isRunning();
        }
        return ((ObjectAnimator) this.objectAnimator).isRunning();
    }

    public void end() {
        if (View10.NEED_PROXY) {
            ((ObjectAnimator10) this.objectAnimator).end();
        } else {
            ((ObjectAnimator) this.objectAnimator).end();
        }
    }

    public void cancel() {
        if (View10.NEED_PROXY) {
            ((ObjectAnimator10) this.objectAnimator).cancel();
        } else {
            ((ObjectAnimator) this.objectAnimator).cancel();
        }
    }

    public ObjectAnimatorProxy addListener(AnimatorListenerAdapterProxy listener) {
        if (View10.NEED_PROXY) {
            ((ObjectAnimator10) this.objectAnimator).addListener((AnimatorListenerAdapter10) listener.animatorListenerAdapter);
        } else {
            ((ObjectAnimator) this.objectAnimator).addListener((AnimatorListenerAdapter) listener.animatorListenerAdapter);
        }
        return this;
    }

    public boolean equals(Object o) {
        return this.objectAnimator == o;
    }
}
