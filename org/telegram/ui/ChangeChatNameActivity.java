package org.telegram.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.C0553R;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.LayoutHelper;

public class ChangeChatNameActivity extends BaseFragment {
    private static final int done_button = 1;
    private int chat_id;
    private View doneButton;
    private EditText firstNameField;
    private View headerLabelView;

    class C07912 implements OnTouchListener {
        C07912() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            return true;
        }
    }

    class C07923 implements OnEditorActionListener {
        C07923() {
        }

        public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
            if (i != 6 || ChangeChatNameActivity.this.doneButton == null) {
                return false;
            }
            ChangeChatNameActivity.this.doneButton.performClick();
            return true;
        }
    }

    class C07934 implements Runnable {
        C07934() {
        }

        public void run() {
            if (ChangeChatNameActivity.this.firstNameField != null) {
                ChangeChatNameActivity.this.firstNameField.requestFocus();
                AndroidUtilities.showKeyboard(ChangeChatNameActivity.this.firstNameField);
            }
        }
    }

    class C15141 extends ActionBarMenuOnItemClick {
        C15141() {
        }

        public void onItemClick(int id) {
            if (id == -1) {
                ChangeChatNameActivity.this.finishFragment();
            } else if (id == 1 && ChangeChatNameActivity.this.firstNameField.getText().length() != 0) {
                ChangeChatNameActivity.this.saveName();
                ChangeChatNameActivity.this.finishFragment();
            }
        }
    }

    public ChangeChatNameActivity(Bundle args) {
        super(args);
    }

    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        this.chat_id = getArguments().getInt("chat_id", 0);
        return true;
    }

    public View createView(Context context) {
        int i = 3;
        this.actionBar.setBackButtonImage(C0553R.drawable.ic_ab_back);
        this.actionBar.setAllowOverlayTitle(true);
        this.actionBar.setTitle(LocaleController.getString("EditName", C0553R.string.EditName));
        this.actionBar.setActionBarMenuOnItemClick(new C15141());
        this.doneButton = this.actionBar.createMenu().addItemWithWidth(1, C0553R.drawable.ic_done, AndroidUtilities.dp(56.0f));
        Chat currentChat = MessagesController.getInstance().getChat(Integer.valueOf(this.chat_id));
        LinearLayout linearLayout = new LinearLayout(context);
        this.fragmentView = linearLayout;
        this.fragmentView.setLayoutParams(new LayoutParams(-1, -1));
        ((LinearLayout) this.fragmentView).setOrientation(1);
        this.fragmentView.setOnTouchListener(new C07912());
        this.firstNameField = new EditText(context);
        this.firstNameField.setText(currentChat.title);
        this.firstNameField.setTextSize(1, 18.0f);
        this.firstNameField.setHintTextColor(-6842473);
        this.firstNameField.setTextColor(-14606047);
        this.firstNameField.setMaxLines(3);
        this.firstNameField.setPadding(0, 0, 0, 0);
        this.firstNameField.setGravity(LocaleController.isRTL ? 5 : 3);
        this.firstNameField.setInputType(180224);
        this.firstNameField.setImeOptions(6);
        EditText editText = this.firstNameField;
        if (LocaleController.isRTL) {
            i = 5;
        }
        editText.setGravity(i);
        AndroidUtilities.clearCursorDrawable(this.firstNameField);
        this.firstNameField.setOnEditorActionListener(new C07923());
        linearLayout.addView(this.firstNameField, LayoutHelper.createLinear(-1, 36, 24.0f, 24.0f, 24.0f, 0.0f));
        if (this.chat_id > 0) {
            this.firstNameField.setHint(LocaleController.getString("GroupName", C0553R.string.GroupName));
        } else {
            this.firstNameField.setHint(LocaleController.getString("EnterListName", C0553R.string.EnterListName));
        }
        this.firstNameField.setSelection(this.firstNameField.length());
        return this.fragmentView;
    }

    public void onResume() {
        super.onResume();
        if (!ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0).getBoolean("view_animations", true)) {
            this.firstNameField.requestFocus();
            AndroidUtilities.showKeyboard(this.firstNameField);
        }
    }

    public void onTransitionAnimationEnd(boolean isOpen, boolean backward) {
        if (isOpen) {
            AndroidUtilities.runOnUIThread(new C07934(), 100);
        }
    }

    private void saveName() {
        MessagesController.getInstance().changeChatTitle(this.chat_id, this.firstNameField.getText().toString());
    }
}
