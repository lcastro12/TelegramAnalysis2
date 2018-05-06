package org.telegram.messenger.Animation;

import android.view.View;
import java.util.HashMap;

public final class ObjectAnimator10 extends ValueAnimator {
    private static final HashMap<String, Property> PROXY_PROPERTIES = new HashMap();
    private boolean mAutoCancel = false;
    private Property mProperty;
    private String mPropertyName;
    private Object mTarget;

    static {
        Property<View, Float> ALPHA = new FloatProperty10<View>("alpha") {
            public void setValue(View object, float value) {
                View10.wrap(object).setAlpha(value);
            }

            public Float get(View object) {
                return Float.valueOf(View10.wrap(object).getAlpha());
            }
        };
        Property<View, Float> PIVOT_X = new FloatProperty10<View>("pivotX") {
            public void setValue(View object, float value) {
                View10.wrap(object).setPivotX(value);
            }

            public Float get(View object) {
                return Float.valueOf(View10.wrap(object).getPivotX());
            }
        };
        Property<View, Float> PIVOT_Y = new FloatProperty10<View>("pivotY") {
            public void setValue(View object, float value) {
                View10.wrap(object).setPivotY(value);
            }

            public Float get(View object) {
                return Float.valueOf(View10.wrap(object).getPivotY());
            }
        };
        Property<View, Float> TRANSLATION_X = new FloatProperty10<View>("translationX") {
            public void setValue(View object, float value) {
                View10.wrap(object).setTranslationX(value);
            }

            public Float get(View object) {
                return Float.valueOf(View10.wrap(object).getTranslationX());
            }
        };
        Property<View, Float> TRANSLATION_Y = new FloatProperty10<View>("translationY") {
            public void setValue(View object, float value) {
                View10.wrap(object).setTranslationY(value);
            }

            public Float get(View object) {
                return Float.valueOf(View10.wrap(object).getTranslationY());
            }
        };
        Property<View, Float> ROTATION = new FloatProperty10<View>("rotation") {
            public void setValue(View object, float value) {
                View10.wrap(object).setRotation(value);
            }

            public Float get(View object) {
                return Float.valueOf(View10.wrap(object).getRotation());
            }
        };
        Property<View, Float> ROTATION_X = new FloatProperty10<View>("rotationX") {
            public void setValue(View object, float value) {
                View10.wrap(object).setRotationX(value);
            }

            public Float get(View object) {
                return Float.valueOf(View10.wrap(object).getRotationX());
            }
        };
        Property<View, Float> ROTATION_Y = new FloatProperty10<View>("rotationY") {
            public void setValue(View object, float value) {
                View10.wrap(object).setRotationY(value);
            }

            public Float get(View object) {
                return Float.valueOf(View10.wrap(object).getRotationY());
            }
        };
        Property<View, Float> SCALE_X = new FloatProperty10<View>("scaleX") {
            public void setValue(View object, float value) {
                View10.wrap(object).setScaleX(value);
            }

            public Float get(View object) {
                return Float.valueOf(View10.wrap(object).getScaleX());
            }
        };
        Property<View, Float> SCALE_Y = new FloatProperty10<View>("scaleY") {
            public void setValue(View object, float value) {
                View10.wrap(object).setScaleY(value);
            }

            public Float get(View object) {
                return Float.valueOf(View10.wrap(object).getScaleY());
            }
        };
        Property<View, Integer> SCROLL_X = new IntProperty<View>("scrollX") {
            public void setValue(View object, int value) {
                View10.wrap(object).setScrollX(value);
            }

            public Integer get(View object) {
                return Integer.valueOf(View10.wrap(object).getScrollX());
            }
        };
        Property<View, Integer> SCROLL_Y = new IntProperty<View>("scrollY") {
            public void setValue(View object, int value) {
                View10.wrap(object).setScrollY(value);
            }

            public Integer get(View object) {
                return Integer.valueOf(View10.wrap(object).getScrollY());
            }
        };
        Property<View, Float> X = new FloatProperty10<View>("x") {
            public void setValue(View object, float value) {
                View10.wrap(object).setX(value);
            }

            public Float get(View object) {
                return Float.valueOf(View10.wrap(object).getX());
            }
        };
        Property<View, Float> Y = new FloatProperty10<View>("y") {
            public void setValue(View object, float value) {
                View10.wrap(object).setY(value);
            }

            public Float get(View object) {
                return Float.valueOf(View10.wrap(object).getY());
            }
        };
        PROXY_PROPERTIES.put("alpha", ALPHA);
        PROXY_PROPERTIES.put("pivotX", PIVOT_X);
        PROXY_PROPERTIES.put("pivotY", PIVOT_Y);
        PROXY_PROPERTIES.put("translationX", TRANSLATION_X);
        PROXY_PROPERTIES.put("translationY", TRANSLATION_Y);
        PROXY_PROPERTIES.put("rotation", ROTATION);
        PROXY_PROPERTIES.put("rotationX", ROTATION_X);
        PROXY_PROPERTIES.put("rotationY", ROTATION_Y);
        PROXY_PROPERTIES.put("scaleX", SCALE_X);
        PROXY_PROPERTIES.put("scaleY", SCALE_Y);
        PROXY_PROPERTIES.put("scrollX", SCROLL_X);
        PROXY_PROPERTIES.put("scrollY", SCROLL_Y);
        PROXY_PROPERTIES.put("x", X);
        PROXY_PROPERTIES.put("y", Y);
    }

    public void setPropertyName(String propertyName) {
        if (this.mValues != null) {
            PropertyValuesHolder valuesHolder = this.mValues[0];
            String oldName = valuesHolder.getPropertyName();
            valuesHolder.setPropertyName(propertyName);
            this.mValuesMap.remove(oldName);
            this.mValuesMap.put(propertyName, valuesHolder);
        }
        this.mPropertyName = propertyName;
        this.mInitialized = false;
    }

    public void setProperty(Property property) {
        if (this.mValues != null) {
            PropertyValuesHolder valuesHolder = this.mValues[0];
            String oldName = valuesHolder.getPropertyName();
            valuesHolder.setProperty(property);
            this.mValuesMap.remove(oldName);
            this.mValuesMap.put(this.mPropertyName, valuesHolder);
        }
        if (this.mProperty != null) {
            this.mPropertyName = property.getName();
        }
        this.mProperty = property;
        this.mInitialized = false;
    }

    public String getPropertyName() {
        String propertyName = null;
        if (this.mPropertyName != null) {
            return this.mPropertyName;
        }
        if (this.mProperty != null) {
            return this.mProperty.getName();
        }
        if (this.mValues == null || this.mValues.length <= 0) {
            return null;
        }
        for (int i = 0; i < this.mValues.length; i++) {
            if (i == 0) {
                propertyName = "";
            } else {
                propertyName = propertyName + ",";
            }
            propertyName = propertyName + this.mValues[i].getPropertyName();
        }
        return propertyName;
    }

    private ObjectAnimator10(Object target, String propertyName) {
        this.mTarget = target;
        setPropertyName(propertyName);
    }

    private <T> ObjectAnimator10(T target, Property<T, ?> property) {
        this.mTarget = target;
        setProperty(property);
    }

    public static ObjectAnimator10 ofInt(Object target, String propertyName, int... values) {
        ObjectAnimator10 anim = new ObjectAnimator10(target, propertyName);
        anim.setIntValues(values);
        return anim;
    }

    public static <T> ObjectAnimator10 ofInt(T target, Property<T, Integer> property, int... values) {
        ObjectAnimator10 anim = new ObjectAnimator10((Object) target, (Property) property);
        anim.setIntValues(values);
        return anim;
    }

    public static ObjectAnimator10 ofFloat(Object target, String propertyName, float... values) {
        ObjectAnimator10 anim = new ObjectAnimator10(target, propertyName);
        anim.setFloatValues(values);
        return anim;
    }

    public static <T> ObjectAnimator10 ofFloat(T target, Property<T, Float> property, float... values) {
        ObjectAnimator10 anim = new ObjectAnimator10((Object) target, (Property) property);
        anim.setFloatValues(values);
        return anim;
    }

    public static ObjectAnimator10 ofObject(Object target, String propertyName, TypeEvaluator evaluator, Object... values) {
        ObjectAnimator10 anim = new ObjectAnimator10(target, propertyName);
        anim.setObjectValues(values);
        anim.setEvaluator(evaluator);
        return anim;
    }

    public static <T, V> ObjectAnimator10 ofObject(T target, Property<T, V> property, TypeEvaluator<V> evaluator, V... values) {
        ObjectAnimator10 anim = new ObjectAnimator10((Object) target, (Property) property);
        anim.setObjectValues(values);
        anim.setEvaluator(evaluator);
        return anim;
    }

    public static ObjectAnimator10 ofPropertyValuesHolder(Object target, PropertyValuesHolder... values) {
        ObjectAnimator10 anim = new ObjectAnimator10();
        anim.mTarget = target;
        anim.setValues(values);
        return anim;
    }

    public void setIntValues(int... values) {
        if (this.mValues != null && this.mValues.length != 0) {
            super.setIntValues(values);
        } else if (this.mProperty != null) {
            setValues(PropertyValuesHolder.ofInt(this.mProperty, values));
        } else {
            setValues(PropertyValuesHolder.ofInt(this.mPropertyName, values));
        }
    }

    public void setFloatValues(float... values) {
        if (this.mValues != null && this.mValues.length != 0) {
            super.setFloatValues(values);
        } else if (this.mProperty != null) {
            setValues(PropertyValuesHolder.ofFloat(this.mProperty, values));
        } else {
            setValues(PropertyValuesHolder.ofFloat(this.mPropertyName, values));
        }
    }

    public void setObjectValues(Object... values) {
        if (this.mValues != null && this.mValues.length != 0) {
            super.setObjectValues(values);
        } else if (this.mProperty != null) {
            setValues(PropertyValuesHolder.ofObject(this.mProperty, null, values));
        } else {
            setValues(PropertyValuesHolder.ofObject(this.mPropertyName, null, values));
        }
    }

    public void setAutoCancel(boolean cancel) {
        this.mAutoCancel = cancel;
    }

    private boolean hasSameTargetAndProperties(Animator10 anim) {
        if (anim instanceof ObjectAnimator10) {
            PropertyValuesHolder[] theirValues = ((ObjectAnimator10) anim).getValues();
            if (((ObjectAnimator10) anim).getTarget() == this.mTarget && this.mValues.length == theirValues.length) {
                for (int i = 0; i < this.mValues.length; i++) {
                    PropertyValuesHolder pvhMine = this.mValues[i];
                    PropertyValuesHolder pvhTheirs = theirValues[i];
                    if (pvhMine.getPropertyName() == null || !pvhMine.getPropertyName().equals(pvhTheirs.getPropertyName())) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public void start() {
        AnimationHandler handler = (AnimationHandler) sAnimationHandler.get();
        if (handler != null) {
            int i;
            ObjectAnimator10 anim;
            for (i = handler.mAnimations.size() - 1; i >= 0; i--) {
                if (handler.mAnimations.get(i) instanceof ObjectAnimator10) {
                    anim = (ObjectAnimator10) handler.mAnimations.get(i);
                    if (anim.mAutoCancel && hasSameTargetAndProperties(anim)) {
                        anim.cancel();
                    }
                }
            }
            for (i = handler.mPendingAnimations.size() - 1; i >= 0; i--) {
                if (handler.mPendingAnimations.get(i) instanceof ObjectAnimator10) {
                    anim = (ObjectAnimator10) handler.mPendingAnimations.get(i);
                    if (anim.mAutoCancel && hasSameTargetAndProperties(anim)) {
                        anim.cancel();
                    }
                }
            }
            for (i = handler.mDelayedAnims.size() - 1; i >= 0; i--) {
                if (handler.mDelayedAnims.get(i) instanceof ObjectAnimator10) {
                    anim = (ObjectAnimator10) handler.mDelayedAnims.get(i);
                    if (anim.mAutoCancel && hasSameTargetAndProperties(anim)) {
                        anim.cancel();
                    }
                }
            }
        }
        super.start();
    }

    void initAnimation() {
        if (!this.mInitialized) {
            if (this.mProperty == null && (this.mTarget instanceof View) && PROXY_PROPERTIES.containsKey(this.mPropertyName)) {
                setProperty((Property) PROXY_PROPERTIES.get(this.mPropertyName));
            }
            int numValues = this.mValues.length;
            for (PropertyValuesHolder mValue : this.mValues) {
                mValue.setupSetterAndGetter(this.mTarget);
            }
            super.initAnimation();
        }
    }

    public ObjectAnimator10 setDuration(long duration) {
        super.setDuration(duration);
        return this;
    }

    public Object getTarget() {
        return this.mTarget;
    }

    public void setTarget(Object target) {
        if (this.mTarget != target) {
            Object oldTarget = this.mTarget;
            this.mTarget = target;
            if (oldTarget == null || target == null || oldTarget.getClass() != target.getClass()) {
                this.mInitialized = false;
            }
        }
    }

    public void setupStartValues() {
        initAnimation();
        int numValues = this.mValues.length;
        for (PropertyValuesHolder mValue : this.mValues) {
            mValue.setupStartValue(this.mTarget);
        }
    }

    public void setupEndValues() {
        initAnimation();
        int numValues = this.mValues.length;
        for (PropertyValuesHolder mValue : this.mValues) {
            mValue.setupEndValue(this.mTarget);
        }
    }

    void animateValue(float fraction) {
        super.animateValue(fraction);
        int numValues = this.mValues.length;
        for (PropertyValuesHolder mValue : this.mValues) {
            mValue.setAnimatedValue(this.mTarget);
        }
    }

    public ObjectAnimator10 clone() {
        return (ObjectAnimator10) super.clone();
    }
}
