package com.google.android.gms.common.data;

import java.util.Iterator;

public abstract class DataBuffer<T> implements Iterable<T> {
    protected final C1287d f29S;

    protected DataBuffer(C1287d dataHolder) {
        this.f29S = dataHolder;
    }

    public void close() {
        this.f29S.close();
    }

    public int describeContents() {
        return 0;
    }

    public abstract T get(int i);

    public int getCount() {
        return this.f29S.getCount();
    }

    public boolean isClosed() {
        return this.f29S.isClosed();
    }

    public Iterator<T> iterator() {
        return new C0129a(this);
    }
}
