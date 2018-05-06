package com.google.android.gms.common.data;

import com.google.android.gms.internal.C0192s;
import java.util.Iterator;
import java.util.NoSuchElementException;

public final class C0129a<T> implements Iterator<T> {
    private final DataBuffer<T> f30T;
    private int f31U = -1;

    public C0129a(DataBuffer<T> dataBuffer) {
        this.f30T = (DataBuffer) C0192s.m521d(dataBuffer);
    }

    public boolean hasNext() {
        return this.f31U < this.f30T.getCount() + -1;
    }

    public T next() {
        if (hasNext()) {
            DataBuffer dataBuffer = this.f30T;
            int i = this.f31U + 1;
            this.f31U = i;
            return dataBuffer.get(i);
        }
        throw new NoSuchElementException("Cannot advance the iterator beyond " + this.f31U);
    }

    public void remove() {
        throw new UnsupportedOperationException("Cannot remove elements from a DataBufferIterator");
    }
}
