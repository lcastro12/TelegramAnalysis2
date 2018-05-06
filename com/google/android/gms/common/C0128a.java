package com.google.android.gms.common;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class C0128a implements ServiceConnection {
    private final BlockingQueue<IBinder> f27A = new LinkedBlockingQueue();
    boolean f28z = false;

    public IBinder m33e() throws InterruptedException {
        if (this.f28z) {
            throw new IllegalStateException();
        }
        this.f28z = true;
        return (IBinder) this.f27A.take();
    }

    public void onServiceConnected(ComponentName name, IBinder service) {
        try {
            this.f27A.put(service);
        } catch (InterruptedException e) {
        }
    }

    public void onServiceDisconnected(ComponentName name) {
    }
}
