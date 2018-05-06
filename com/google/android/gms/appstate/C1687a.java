package com.google.android.gms.appstate;

import com.google.android.gms.internal.C0191r;

public final class C1687a implements AppState {
    private final int f148h;
    private final String f149i;
    private final byte[] f150j;
    private final boolean f151k;
    private final String f152l;
    private final byte[] f153m;

    public C1687a(AppState appState) {
        this.f148h = appState.getKey();
        this.f149i = appState.getLocalVersion();
        this.f150j = appState.getLocalData();
        this.f151k = appState.hasConflict();
        this.f152l = appState.getConflictVersion();
        this.f153m = appState.getConflictData();
    }

    static int m1129a(AppState appState) {
        return C0191r.hashCode(Integer.valueOf(appState.getKey()), appState.getLocalVersion(), appState.getLocalData(), Boolean.valueOf(appState.hasConflict()), appState.getConflictVersion(), appState.getConflictData());
    }

    static boolean m1130a(AppState appState, Object obj) {
        if (!(obj instanceof AppState)) {
            return false;
        }
        if (appState == obj) {
            return true;
        }
        AppState appState2 = (AppState) obj;
        return C0191r.m513a(Integer.valueOf(appState2.getKey()), Integer.valueOf(appState.getKey())) && C0191r.m513a(appState2.getLocalVersion(), appState.getLocalVersion()) && C0191r.m513a(appState2.getLocalData(), appState.getLocalData()) && C0191r.m513a(Boolean.valueOf(appState2.hasConflict()), Boolean.valueOf(appState.hasConflict())) && C0191r.m513a(appState2.getConflictVersion(), appState.getConflictVersion()) && C0191r.m513a(appState2.getConflictData(), appState.getConflictData());
    }

    static String m1131b(AppState appState) {
        return C0191r.m514c(appState).m512a("Key", Integer.valueOf(appState.getKey())).m512a("LocalVersion", appState.getLocalVersion()).m512a("LocalData", appState.getLocalData()).m512a("HasConflict", Boolean.valueOf(appState.hasConflict())).m512a("ConflictVersion", appState.getConflictVersion()).m512a("ConflictData", appState.getConflictData()).toString();
    }

    public AppState m1132a() {
        return this;
    }

    public boolean equals(Object obj) {
        return C1687a.m1130a(this, obj);
    }

    public /* synthetic */ Object freeze() {
        return m1132a();
    }

    public byte[] getConflictData() {
        return this.f153m;
    }

    public String getConflictVersion() {
        return this.f152l;
    }

    public int getKey() {
        return this.f148h;
    }

    public byte[] getLocalData() {
        return this.f150j;
    }

    public String getLocalVersion() {
        return this.f149i;
    }

    public boolean hasConflict() {
        return this.f151k;
    }

    public int hashCode() {
        return C1687a.m1129a(this);
    }

    public boolean isDataValid() {
        return true;
    }

    public String toString() {
        return C1687a.m1131b(this);
    }
}
