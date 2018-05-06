package net.hockeyapp.android;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import net.hockeyapp.android.views.ExpiryInfoView;

public class ExpiryInfoActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getStringResource(Strings.EXPIRY_INFO_TITLE_ID));
        setContentView(getLayoutView());
    }

    protected View getLayoutView() {
        return new ExpiryInfoView(this, getStringResource(Strings.EXPIRY_INFO_TEXT_ID));
    }

    protected String getStringResource(int resourceID) {
        return Strings.get(UpdateManager.getLastListener(), resourceID);
    }
}
