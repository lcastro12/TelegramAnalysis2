package com.google.android.gms.plus;

import android.content.Context;
import android.net.Uri;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.internal.bt;
import com.google.android.gms.plus.model.moments.Moment;
import com.google.android.gms.plus.model.moments.MomentBuffer;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.PersonBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class PlusClient implements GooglePlayServicesClient {
    @Deprecated
    public static final String KEY_REQUEST_VISIBLE_ACTIVITIES = "request_visible_actions";
    final bt hU;

    public static class Builder {
        private OnConnectionFailedListener f40e;
        private String f41g;
        private ConnectionCallbacks hV;
        private ArrayList<String> hW = new ArrayList();
        private String[] hX;
        private String[] hY;
        private String hZ = this.mContext.getPackageName();
        private String ia = this.mContext.getPackageName();
        private String ib;
        private Context mContext;

        public Builder(Context context, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener connectionFailedListener) {
            this.mContext = context;
            this.hV = connectionCallbacks;
            this.f40e = connectionFailedListener;
            this.hW.add(Scopes.PLUS_LOGIN);
        }

        public PlusClient build() {
            if (this.f41g == null) {
                this.f41g = "<<default account>>";
            }
            return new PlusClient(new bt(this.mContext, new C1433a(this.f41g, (String[]) this.hW.toArray(new String[this.hW.size()]), this.hX, this.hY, this.hZ, this.ia, this.ib), this.hV, this.f40e));
        }

        public Builder clearScopes() {
            this.hW.clear();
            return this;
        }

        public Builder setAccountName(String accountName) {
            this.f41g = accountName;
            return this;
        }

        public Builder setActions(String... actions) {
            this.hX = actions;
            return this;
        }

        public Builder setScopes(String... scopes) {
            this.hW.clear();
            this.hW.addAll(Arrays.asList(scopes));
            return this;
        }

        @Deprecated
        public Builder setVisibleActivities(String... actions) {
            setActions(actions);
            return this;
        }
    }

    public interface OnAccessRevokedListener {
        void onAccessRevoked(ConnectionResult connectionResult);
    }

    public interface OnMomentsLoadedListener {
        void onMomentsLoaded(ConnectionResult connectionResult, MomentBuffer momentBuffer, String str, String str2);
    }

    public interface OnPeopleLoadedListener {
        void onPeopleLoaded(ConnectionResult connectionResult, PersonBuffer personBuffer, String str);
    }

    public interface OrderBy {
        public static final int ALPHABETICAL = 0;
        public static final int BEST = 1;
    }

    PlusClient(bt plusClientImpl) {
        this.hU = plusClientImpl;
    }

    bt bu() {
        return this.hU;
    }

    public void clearDefaultAccount() {
        this.hU.clearDefaultAccount();
    }

    public void connect() {
        this.hU.connect();
    }

    public void disconnect() {
        this.hU.disconnect();
    }

    public String getAccountName() {
        return this.hU.getAccountName();
    }

    public Person getCurrentPerson() {
        return this.hU.getCurrentPerson();
    }

    public boolean isConnected() {
        return this.hU.isConnected();
    }

    public boolean isConnecting() {
        return this.hU.isConnecting();
    }

    public boolean isConnectionCallbacksRegistered(ConnectionCallbacks listener) {
        return this.hU.isConnectionCallbacksRegistered(listener);
    }

    public boolean isConnectionFailedListenerRegistered(OnConnectionFailedListener listener) {
        return this.hU.isConnectionFailedListenerRegistered(listener);
    }

    public void loadMoments(OnMomentsLoadedListener listener) {
        this.hU.loadMoments(listener);
    }

    public void loadMoments(OnMomentsLoadedListener listener, int maxResults, String pageToken, Uri targetUrl, String type, String userId) {
        this.hU.loadMoments(listener, maxResults, pageToken, targetUrl, type, userId);
    }

    public void loadPeople(OnPeopleLoadedListener listener, Collection<String> personIds) {
        this.hU.m1260a(listener, (Collection) personIds);
    }

    public void loadPeople(OnPeopleLoadedListener listener, String... personIds) {
        this.hU.m1261a(listener, personIds);
    }

    public void loadVisiblePeople(OnPeopleLoadedListener listener, int orderBy, String pageToken) {
        this.hU.loadVisiblePeople(listener, orderBy, pageToken);
    }

    public void loadVisiblePeople(OnPeopleLoadedListener listener, String pageToken) {
        this.hU.loadVisiblePeople(listener, pageToken);
    }

    public void registerConnectionCallbacks(ConnectionCallbacks listener) {
        this.hU.registerConnectionCallbacks(listener);
    }

    public void registerConnectionFailedListener(OnConnectionFailedListener listener) {
        this.hU.registerConnectionFailedListener(listener);
    }

    public void removeMoment(String momentId) {
        this.hU.removeMoment(momentId);
    }

    public void revokeAccessAndDisconnect(OnAccessRevokedListener listener) {
        this.hU.revokeAccessAndDisconnect(listener);
    }

    public void unregisterConnectionCallbacks(ConnectionCallbacks listener) {
        this.hU.unregisterConnectionCallbacks(listener);
    }

    public void unregisterConnectionFailedListener(OnConnectionFailedListener listener) {
        this.hU.unregisterConnectionFailedListener(listener);
    }

    public void writeMoment(Moment moment) {
        this.hU.writeMoment(moment);
    }
}
