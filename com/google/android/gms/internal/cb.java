package com.google.android.gms.internal;

import android.os.Parcel;
import com.google.android.gms.common.data.C0130b;
import com.google.android.gms.common.data.C1287d;
import com.google.android.gms.plus.model.moments.ItemScope;
import com.google.android.gms.plus.model.moments.Moment;

public final class cb extends C0130b implements Moment {
    private bz jD;

    public cb(C1287d c1287d, int i) {
        super(c1287d, i);
    }

    private bz cb() {
        synchronized (this) {
            if (this.jD == null) {
                byte[] byteArray = getByteArray("momentImpl");
                Parcel obtain = Parcel.obtain();
                obtain.unmarshall(byteArray, 0, byteArray.length);
                obtain.setDataPosition(0);
                this.jD = bz.CREATOR.m420x(obtain);
                obtain.recycle();
            }
        }
        return this.jD;
    }

    public bz ca() {
        return cb();
    }

    public /* synthetic */ Object freeze() {
        return ca();
    }

    public String getId() {
        return cb().getId();
    }

    public ItemScope getResult() {
        return cb().getResult();
    }

    public String getStartDate() {
        return cb().getStartDate();
    }

    public ItemScope getTarget() {
        return cb().getTarget();
    }

    public String getType() {
        return cb().getType();
    }

    public boolean hasId() {
        return cb().hasId();
    }

    public boolean hasResult() {
        return cb().hasId();
    }

    public boolean hasStartDate() {
        return cb().hasStartDate();
    }

    public boolean hasTarget() {
        return cb().hasTarget();
    }

    public boolean hasType() {
        return cb().hasType();
    }
}
