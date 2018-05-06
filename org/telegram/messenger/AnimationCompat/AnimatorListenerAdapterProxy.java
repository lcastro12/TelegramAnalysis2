package org.telegram.messenger.AnimationCompat;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import org.telegram.messenger.Animation.Animator10;
import org.telegram.messenger.Animation.AnimatorListenerAdapter10;
import org.telegram.messenger.Animation.View10;

public class AnimatorListenerAdapterProxy {
    protected Object animatorListenerAdapter;

    class C03012 extends AnimatorListenerAdapter {
        C03012() {
        }

        public void onAnimationCancel(Animator animation) {
            AnimatorListenerAdapterProxy.this.onAnimationCancel(animation);
        }

        public void onAnimationEnd(Animator animation) {
            AnimatorListenerAdapterProxy.this.onAnimationEnd(animation);
        }

        public void onAnimationRepeat(Animator animation) {
            AnimatorListenerAdapterProxy.this.onAnimationRepeat(animation);
        }

        public void onAnimationStart(Animator animation) {
            AnimatorListenerAdapterProxy.this.onAnimationStart(animation);
        }

        public void onAnimationPause(Animator animation) {
            AnimatorListenerAdapterProxy.this.onAnimationPause(animation);
        }

        public void onAnimationResume(Animator animation) {
            AnimatorListenerAdapterProxy.this.onAnimationResume(animation);
        }
    }

    class C17501 extends AnimatorListenerAdapter10 {
        C17501() {
        }

        public void onAnimationCancel(Animator10 animation) {
            AnimatorListenerAdapterProxy.this.onAnimationCancel(animation);
        }

        public void onAnimationEnd(Animator10 animation) {
            AnimatorListenerAdapterProxy.this.onAnimationEnd(animation);
        }

        public void onAnimationRepeat(Animator10 animation) {
            AnimatorListenerAdapterProxy.this.onAnimationRepeat(animation);
        }

        public void onAnimationStart(Animator10 animation) {
            AnimatorListenerAdapterProxy.this.onAnimationStart(animation);
        }

        public void onAnimationPause(Animator10 animation) {
            AnimatorListenerAdapterProxy.this.onAnimationPause(animation);
        }

        public void onAnimationResume(Animator10 animation) {
            AnimatorListenerAdapterProxy.this.onAnimationResume(animation);
        }
    }

    public AnimatorListenerAdapterProxy() {
        if (View10.NEED_PROXY) {
            this.animatorListenerAdapter = new C17501();
        } else {
            this.animatorListenerAdapter = new C03012();
        }
    }

    public void onAnimationCancel(Object animation) {
    }

    public void onAnimationEnd(Object animation) {
    }

    public void onAnimationRepeat(Object animation) {
    }

    public void onAnimationStart(Object animation) {
    }

    public void onAnimationPause(Object animation) {
    }

    public void onAnimationResume(Object animation) {
    }
}
