package com.google.android.gms.appstate;

import android.content.Context;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.internal.C0192s;
import com.google.android.gms.internal.C1712c;

public final class AppStateClient implements GooglePlayServicesClient {
    public static final int STATUS_CLIENT_RECONNECT_REQUIRED = 2;
    public static final int STATUS_DEVELOPER_ERROR = 7;
    public static final int STATUS_INTERNAL_ERROR = 1;
    public static final int STATUS_NETWORK_ERROR_NO_DATA = 4;
    public static final int STATUS_NETWORK_ERROR_OPERATION_DEFERRED = 5;
    public static final int STATUS_NETWORK_ERROR_OPERATION_FAILED = 6;
    public static final int STATUS_NETWORK_ERROR_STALE_DATA = 3;
    public static final int STATUS_OK = 0;
    public static final int STATUS_STATE_KEY_LIMIT_EXCEEDED = 2003;
    public static final int STATUS_STATE_KEY_NOT_FOUND = 2002;
    public static final int STATUS_WRITE_OUT_OF_DATE_VERSION = 2000;
    public static final int STATUS_WRITE_SIZE_EXCEEDED = 2001;
    private final C1712c f67b;

    public static final class Builder {
        private static final String[] f0c = new String[]{Scopes.APP_STATE};
        private ConnectionCallbacks f1d;
        private OnConnectionFailedListener f2e;
        private String[] f3f = f0c;
        private String f4g = "<<default account>>";
        private Context mContext;

        public Builder(Context context, ConnectionCallbacks connectedListener, OnConnectionFailedListener connectionFailedListener) {
            this.mContext = context;
            this.f1d = connectedListener;
            this.f2e = connectionFailedListener;
        }

        public AppStateClient create() {
            return new AppStateClient(this.mContext, this.f1d, this.f2e, this.f4g, this.f3f);
        }

        public Builder setAccountName(String accountName) {
            this.f4g = (String) C0192s.m521d(accountName);
            return this;
        }

        public Builder setScopes(String... scopes) {
            this.f3f = scopes;
            return this;
        }
    }

    private AppStateClient(Context context, ConnectionCallbacks connectedListener, OnConnectionFailedListener connectionFailedListener, String accountName, String[] scopes) {
        this.f67b = new C1712c(context, connectedListener, connectionFailedListener, accountName, scopes);
    }

    public void connect() {
        this.f67b.connect();
    }

    public void deleteState(OnStateDeletedListener listener, int stateKey) {
        this.f67b.deleteState(listener, stateKey);
    }

    public void disconnect() {
        this.f67b.disconnect();
    }

    public int getMaxNumKeys() {
        return this.f67b.getMaxNumKeys();
    }

    public int getMaxStateSize() {
        return this.f67b.getMaxStateSize();
    }

    public boolean isConnected() {
        return this.f67b.isConnected();
    }

    public boolean isConnecting() {
        return this.f67b.isConnecting();
    }

    public boolean isConnectionCallbacksRegistered(ConnectionCallbacks listener) {
        return this.f67b.isConnectionCallbacksRegistered(listener);
    }

    public boolean isConnectionFailedListenerRegistered(OnConnectionFailedListener listener) {
        return this.f67b.isConnectionFailedListenerRegistered(listener);
    }

    public void listStates(OnStateListLoadedListener listener) {
        this.f67b.listStates(listener);
    }

    public void loadState(OnStateLoadedListener listener, int stateKey) {
        this.f67b.loadState(listener, stateKey);
    }

    public void reconnect() {
        this.f67b.disconnect();
        this.f67b.connect();
    }

    public void registerConnectionCallbacks(ConnectionCallbacks listener) {
        this.f67b.registerConnectionCallbacks(listener);
    }

    public void registerConnectionFailedListener(OnConnectionFailedListener listener) {
        this.f67b.registerConnectionFailedListener(listener);
    }

    public void resolveState(OnStateLoadedListener listener, int stateKey, String resolvedVersion, byte[] resolvedData) {
        this.f67b.resolveState(listener, stateKey, resolvedVersion, resolvedData);
    }

    public void signOut() {
        this.f67b.signOut(null);
    }

    public void signOut(OnSignOutCompleteListener listener) {
        C0192s.m518b((Object) listener, (Object) "Must provide a valid listener");
        this.f67b.signOut(listener);
    }

    public void unregisterConnectionCallbacks(ConnectionCallbacks listener) {
        this.f67b.unregisterConnectionCallbacks(listener);
    }

    public void unregisterConnectionFailedListener(OnConnectionFailedListener listener) {
        this.f67b.unregisterConnectionFailedListener(listener);
    }

    public void updateState(int stateKey, byte[] data) {
        this.f67b.m1281a(null, stateKey, data);
    }

    public void updateStateImmediate(OnStateLoadedListener listener, int stateKey, byte[] data) {
        C0192s.m518b((Object) listener, (Object) "Must provide a valid listener");
        this.f67b.m1281a(listener, stateKey, data);
    }
}
