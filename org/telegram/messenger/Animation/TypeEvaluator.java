package org.telegram.messenger.Animation;

public interface TypeEvaluator<T> {
    T evaluate(float f, T t, T t2);
}
