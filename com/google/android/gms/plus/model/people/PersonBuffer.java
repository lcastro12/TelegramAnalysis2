package com.google.android.gms.plus.model.people;

import com.google.android.gms.common.data.C1285c;
import com.google.android.gms.common.data.C1287d;
import com.google.android.gms.common.data.DataBuffer;
import com.google.android.gms.internal.cc;
import com.google.android.gms.internal.cn;

public final class PersonBuffer extends DataBuffer<Person> {
    private final C1285c<cc> kp;

    public PersonBuffer(C1287d dataHolder) {
        super(dataHolder);
        if (dataHolder.m639l() == null || !dataHolder.m639l().getBoolean("com.google.android.gms.plus.IsSafeParcelable", false)) {
            this.kp = null;
        } else {
            this.kp = new C1285c(dataHolder, cc.CREATOR);
        }
    }

    public Person get(int position) {
        return this.kp != null ? (Person) this.kp.m620d(position) : new cn(this.S, position);
    }
}
