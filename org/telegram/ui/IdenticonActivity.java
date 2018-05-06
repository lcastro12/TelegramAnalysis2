package org.telegram.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.C0553R;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.tgnet.TLRPC.EncryptedChat;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.IdenticonDrawable;

public class IdenticonActivity extends BaseFragment {
    private int chat_id;

    class C10232 implements OnTouchListener {
        C10232() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            return true;
        }
    }

    class C10243 implements OnPreDrawListener {
        C10243() {
        }

        public boolean onPreDraw() {
            if (IdenticonActivity.this.fragmentView != null) {
                IdenticonActivity.this.fragmentView.getViewTreeObserver().removeOnPreDrawListener(this);
                LinearLayout layout = (LinearLayout) IdenticonActivity.this.fragmentView;
                int rotation = ((WindowManager) ApplicationLoader.applicationContext.getSystemService("window")).getDefaultDisplay().getRotation();
                if (rotation == 3 || rotation == 1) {
                    layout.setOrientation(0);
                } else {
                    layout.setOrientation(1);
                }
                IdenticonActivity.this.fragmentView.setPadding(IdenticonActivity.this.fragmentView.getPaddingLeft(), 0, IdenticonActivity.this.fragmentView.getPaddingRight(), IdenticonActivity.this.fragmentView.getPaddingBottom());
            }
            return true;
        }
    }

    class C15931 extends ActionBarMenuOnItemClick {
        C15931() {
        }

        public void onItemClick(int id) {
            if (id == -1) {
                IdenticonActivity.this.finishFragment();
            }
        }
    }

    public IdenticonActivity(Bundle args) {
        super(args);
    }

    public boolean onFragmentCreate() {
        this.chat_id = getArguments().getInt("chat_id");
        return super.onFragmentCreate();
    }

    public View createView(Context context) {
        this.actionBar.setBackButtonImage(C0553R.drawable.ic_ab_back);
        this.actionBar.setAllowOverlayTitle(true);
        this.actionBar.setTitle(LocaleController.getString("EncryptionKey", C0553R.string.EncryptionKey));
        this.actionBar.setActionBarMenuOnItemClick(new C15931());
        this.fragmentView = getParentActivity().getLayoutInflater().inflate(C0553R.layout.identicon_layout, null, false);
        ImageView identiconView = (ImageView) this.fragmentView.findViewById(C0553R.id.identicon_view);
        TextView textView = (TextView) this.fragmentView.findViewById(C0553R.id.identicon_text);
        EncryptedChat encryptedChat = MessagesController.getInstance().getEncryptedChat(Integer.valueOf(this.chat_id));
        if (encryptedChat != null) {
            IdenticonDrawable drawable = new IdenticonDrawable();
            identiconView.setImageDrawable(drawable);
            drawable.setEncryptedChat(encryptedChat);
            User user = MessagesController.getInstance().getUser(Integer.valueOf(encryptedChat.user_id));
            textView.setText(AndroidUtilities.replaceTags(LocaleController.formatString("EncryptionKeyDescription", C0553R.string.EncryptionKeyDescription, user.first_name, user.first_name)));
        }
        this.fragmentView.setOnTouchListener(new C10232());
        return this.fragmentView;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        fixLayout();
    }

    public void onResume() {
        super.onResume();
        fixLayout();
    }

    private void fixLayout() {
        this.fragmentView.getViewTreeObserver().addOnPreDrawListener(new C10243());
    }
}
