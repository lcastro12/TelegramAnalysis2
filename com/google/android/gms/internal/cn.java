package com.google.android.gms.internal;

import com.google.android.gms.common.data.C0130b;
import com.google.android.gms.common.data.C1287d;
import com.google.android.gms.internal.cc.C0167e;
import com.google.android.gms.internal.cc.C1717c;
import com.google.android.gms.plus.PlusShare;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.Person.AgeRange;
import com.google.android.gms.plus.model.people.Person.Cover;
import com.google.android.gms.plus.model.people.Person.Emails;
import com.google.android.gms.plus.model.people.Person.Image;
import com.google.android.gms.plus.model.people.Person.Name;
import com.google.android.gms.plus.model.people.Person.Organizations;
import com.google.android.gms.plus.model.people.Person.PlacesLived;
import com.google.android.gms.plus.model.people.Person.Urls;
import java.util.ArrayList;
import java.util.List;

public final class cn extends C0130b implements Person {
    public cn(C1287d c1287d, int i) {
        super(c1287d, i);
    }

    public Person cA() {
        return new cc(getDisplayName(), getId(), (C1717c) getImage(), getObjectType(), getUrl());
    }

    public ArrayList<Emails> cw() {
        return null;
    }

    public ArrayList<Organizations> cx() {
        return null;
    }

    public ArrayList<PlacesLived> cy() {
        return null;
    }

    public ArrayList<Urls> cz() {
        return null;
    }

    public /* synthetic */ Object freeze() {
        return cA();
    }

    public String getAboutMe() {
        return null;
    }

    public AgeRange getAgeRange() {
        return null;
    }

    public String getBirthday() {
        return null;
    }

    public String getBraggingRights() {
        return null;
    }

    public int getCircledByCount() {
        return 0;
    }

    public Cover getCover() {
        return null;
    }

    public String getCurrentLocation() {
        return null;
    }

    public String getDisplayName() {
        return getString("displayName");
    }

    public /* synthetic */ List getEmails() {
        return cw();
    }

    public int getGender() {
        return 0;
    }

    public String getId() {
        return getString("personId");
    }

    public Image getImage() {
        return new C1717c(getString("image"));
    }

    public String getLanguage() {
        return null;
    }

    public Name getName() {
        return null;
    }

    public String getNickname() {
        return null;
    }

    public int getObjectType() {
        return C0167e.m421G(getString("objectType"));
    }

    public /* synthetic */ List getOrganizations() {
        return cx();
    }

    public /* synthetic */ List getPlacesLived() {
        return cy();
    }

    public int getPlusOneCount() {
        return 0;
    }

    public int getRelationshipStatus() {
        return 0;
    }

    public String getTagline() {
        return null;
    }

    public String getUrl() {
        return getString(PlusShare.KEY_CALL_TO_ACTION_URL);
    }

    public /* synthetic */ List getUrls() {
        return cz();
    }

    public boolean hasAboutMe() {
        return false;
    }

    public boolean hasAgeRange() {
        return false;
    }

    public boolean hasBirthday() {
        return false;
    }

    public boolean hasBraggingRights() {
        return false;
    }

    public boolean hasCircledByCount() {
        return false;
    }

    public boolean hasCover() {
        return false;
    }

    public boolean hasCurrentLocation() {
        return false;
    }

    public boolean hasDisplayName() {
        return true;
    }

    public boolean hasEmails() {
        return false;
    }

    public boolean hasGender() {
        return false;
    }

    public boolean hasId() {
        return true;
    }

    public boolean hasImage() {
        return true;
    }

    public boolean hasIsPlusUser() {
        return false;
    }

    public boolean hasLanguage() {
        return false;
    }

    public boolean hasName() {
        return false;
    }

    public boolean hasNickname() {
        return false;
    }

    public boolean hasObjectType() {
        return true;
    }

    public boolean hasOrganizations() {
        return false;
    }

    public boolean hasPlacesLived() {
        return false;
    }

    public boolean hasPlusOneCount() {
        return false;
    }

    public boolean hasRelationshipStatus() {
        return false;
    }

    public boolean hasTagline() {
        return false;
    }

    public boolean hasUrl() {
        return true;
    }

    public boolean hasUrls() {
        return false;
    }

    public boolean hasVerified() {
        return false;
    }

    public boolean isPlusUser() {
        return false;
    }

    public boolean isVerified() {
        return false;
    }
}
