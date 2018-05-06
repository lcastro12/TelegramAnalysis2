package org.telegram.messenger.Animation;

public abstract class FloatProperty10<T> extends Property<T, Float> {
    public abstract void setValue(T t, float f);

    public FloatProperty10(String name) {
        super(Float.class, name);
    }

    public final void set(T object, Float value) {
        setValue(object, value.floatValue());
    }
}
