package com.google.android.gms.common.data;

import java.util.ArrayList;

public abstract class C1288f<T> extends DataBuffer<T> {
    private boolean ao = false;
    private ArrayList<Integer> ap;

    protected C1288f(C1287d c1287d) {
        super(c1287d);
    }

    private int m640h(int i) {
        if (i >= 0 && i < this.ap.size()) {
            return ((Integer) this.ap.get(i)).intValue();
        }
        throw new IllegalArgumentException("Position " + i + " is out of bounds for this buffer");
    }

    private int m641i(int i) {
        return (i < 0 || i == this.ap.size()) ? 0 : i == this.ap.size() + -1 ? this.S.getCount() - ((Integer) this.ap.get(i)).intValue() : ((Integer) this.ap.get(i + 1)).intValue() - ((Integer) this.ap.get(i)).intValue();
    }

    private void m642m() {
        synchronized (this) {
            if (!this.ao) {
                int count = this.S.getCount();
                this.ap = new ArrayList();
                if (count > 0) {
                    this.ap.add(Integer.valueOf(0));
                    String primaryDataMarkerColumn = getPrimaryDataMarkerColumn();
                    String c = this.S.m629c(primaryDataMarkerColumn, 0, this.S.m631e(0));
                    int i = 1;
                    while (i < count) {
                        String c2 = this.S.m629c(primaryDataMarkerColumn, i, this.S.m631e(i));
                        if (c2.equals(c)) {
                            c2 = c;
                        } else {
                            this.ap.add(Integer.valueOf(i));
                        }
                        i++;
                        c = c2;
                    }
                }
                this.ao = true;
            }
        }
    }

    protected abstract T mo2301a(int i, int i2);

    public final T get(int position) {
        m642m();
        return mo2301a(m640h(position), m641i(position));
    }

    public int getCount() {
        m642m();
        return this.ap.size();
    }

    protected abstract String getPrimaryDataMarkerColumn();
}
