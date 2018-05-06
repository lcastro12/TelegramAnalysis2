package net.hockeyapp.android;

import android.content.Context;
import android.text.TextUtils;

public class LocaleManager {
    public static void initialize(Context context) {
        loadFromResources("hockeyapp_crash_dialog_title", 0, context);
        loadFromResources("hockeyapp_crash_dialog_message", 1, context);
        loadFromResources("hockeyapp_crash_dialog_negative_button", 2, context);
        loadFromResources("hockeyapp_crash_dialog_neutral_button", 3, context);
        loadFromResources("hockeyapp_crash_dialog_positive_button", 4, context);
        loadFromResources("hockeyapp_download_failed_dialog_title", 256, context);
        loadFromResources("hockeyapp_download_failed_dialog_message", 257, context);
        loadFromResources("hockeyapp_download_failed_dialog_negative_button", Strings.DOWNLOAD_FAILED_DIALOG_NEGATIVE_BUTTON_ID, context);
        loadFromResources("hockeyapp_download_failed_dialog_positive_button", Strings.DOWNLOAD_FAILED_DIALOG_POSITIVE_BUTTON_ID, context);
        loadFromResources("hockeyapp_update_mandatory_toast", 512, context);
        loadFromResources("hockeyapp_update_dialog_title", 513, context);
        loadFromResources("hockeyapp_update_dialog_message", Strings.UPDATE_DIALOG_MESSAGE_ID, context);
        loadFromResources("hockeyapp_update_dialog_negative_button", Strings.UPDATE_DIALOG_NEGATIVE_BUTTON_ID, context);
        loadFromResources("hockeyapp_update_dialog_positive_button", Strings.UPDATE_DIALOG_POSITIVE_BUTTON_ID, context);
        loadFromResources("hockeyapp_expiry_info_title", Strings.EXPIRY_INFO_TITLE_ID, context);
        loadFromResources("hockeyapp_expiry_info_text", Strings.EXPIRY_INFO_TEXT_ID, context);
        loadFromResources("hockeyapp_feedback_failed_title", 1024, context);
        loadFromResources("hockeyapp_feedback_failed_text", 1025, context);
        loadFromResources("hockeyapp_feedback_name_hint", Strings.FEEDBACK_NAME_INPUT_HINT_ID, context);
        loadFromResources("hockeyapp_feedback_email_hint", Strings.FEEDBACK_EMAIL_INPUT_HINT_ID, context);
        loadFromResources("hockeyapp_feedback_subject_hint", Strings.FEEDBACK_SUBJECT_INPUT_HINT_ID, context);
        loadFromResources("hockeyapp_feedback_message_hint", Strings.FEEDBACK_MESSAGE_INPUT_HINT_ID, context);
        loadFromResources("hockeyapp_feedback_last_updated_text", Strings.FEEDBACK_LAST_UPDATED_TEXT_ID, context);
        loadFromResources("hockeyapp_feedback_attachment_button_text", Strings.FEEDBACK_ATTACHMENT_BUTTON_TEXT_ID, context);
        loadFromResources("hockeyapp_feedback_send_button_text", Strings.FEEDBACK_SEND_BUTTON_TEXT_ID, context);
        loadFromResources("hockeyapp_feedback_response_button_text", Strings.FEEDBACK_RESPONSE_BUTTON_TEXT_ID, context);
        loadFromResources("hockeyapp_feedback_refresh_button_text", Strings.FEEDBACK_REFRESH_BUTTON_TEXT_ID, context);
        loadFromResources("hockeyapp_feedback_title", Strings.FEEDBACK_TITLE_ID, context);
        loadFromResources("hockeyapp_feedback_send_generic_error", Strings.FEEDBACK_SEND_GENERIC_ERROR_ID, context);
        loadFromResources("hockeyapp_feedback_send_network_error", Strings.FEEDBACK_SEND_NETWORK_ERROR_ID, context);
        loadFromResources("hockeyapp_feedback_validate_subject_error", Strings.FEEDBACK_VALIDATE_SUBJECT_ERROR_ID, context);
        loadFromResources("hockeyapp_feedback_validate_email_error", Strings.FEEDBACK_VALIDATE_EMAIL_ERROR_ID, context);
        loadFromResources("hockeyapp_feedback_validate_email_empty", Strings.FEEDBACK_VALIDATE_EMAIL_EMPTY_ID, context);
        loadFromResources("hockeyapp_feedback_validate_name_error", Strings.FEEDBACK_VALIDATE_NAME_ERROR_ID, context);
        loadFromResources("hockeyapp_feedback_validate_text_error", Strings.FEEDBACK_VALIDATE_TEXT_ERROR_ID, context);
        loadFromResources("hockeyapp_feedback_generic_error", Strings.FEEDBACK_GENERIC_ERROR_ID, context);
        loadFromResources("hockeyapp_feedback_attach_file", Strings.FEEDBACK_ATTACH_FILE_ID, context);
        loadFromResources("hockeyapp_feedback_attach_picture", Strings.FEEDBACK_ATTACH_PICTURE_ID, context);
        loadFromResources("hockeyapp_feedback_select_file", Strings.FEEDBACK_SELECT_FILE_ID, context);
        loadFromResources("hockeyapp_feedback_select_picture", Strings.FEEDBACK_SELECT_PICTURE_ID, context);
        loadFromResources("hockeyapp_feedback_max_attachments_allowed", Strings.FEEDBACK_MAX_ATTACHMENTS_ALLOWED, context);
        loadFromResources("hockeyapp_login_headline_text", Strings.LOGIN_HEADLINE_TEXT_ID, context);
        loadFromResources("hockeyapp_login_missing_credentials_toast", Strings.LOGIN_MISSING_CREDENTIALS_TOAST_ID, context);
        loadFromResources("hockeyapp_login_email_hint", Strings.LOGIN_EMAIL_INPUT_HINT_ID, context);
        loadFromResources("hockeyapp_login_password_hint", Strings.LOGIN_PASSWORD_INPUT_HINT_ID, context);
        loadFromResources("hockeyapp_login_login_button_text", Strings.LOGIN_LOGIN_BUTTON_TEXT_ID, context);
        loadFromResources("hockeyapp_paint_indicator_toast", Strings.PAINT_INDICATOR_TOAST_ID, context);
        loadFromResources("hockeyapp_paint_menu_save", Strings.PAINT_MENU_SAVE_ID, context);
        loadFromResources("hockeyapp_paint_menu_undo", Strings.PAINT_MENU_UNDO_ID, context);
        loadFromResources("hockeyapp_paint_menu_clear", Strings.PAINT_MENU_CLEAR_ID, context);
        loadFromResources("hockeyapp_paint_dialog_message", Strings.PAINT_DIALOG_MESSAGE_ID, context);
        loadFromResources("hockeyapp_paint_dialog_negative_button", Strings.PAINT_DIALOG_NEGATIVE_BUTTON_ID, context);
        loadFromResources("hockeyapp_paint_dialog_positive_button", Strings.PAINT_DIALOG_POSITIVE_BUTTON_ID, context);
        loadFromResources("hockeyapp_permission_update_title", Strings.PERMISSION_UPDATE_TITLE_ID, context);
        loadFromResources("hockeyapp_permission_update_message", Strings.PERMISSION_UPDATE_MESSAGE_ID, context);
        loadFromResources("hockeyapp_permission_dialog_negative_button", Strings.PERMISSION_DIALOG_NEGATIVE_BUTTON_ID, context);
        loadFromResources("hockeyapp_permission_dialog_positive_button", Strings.PERMISSION_DIALOG_POSITIVE_BUTTON_ID, context);
        loadFromResources("hockeyapp_dialog_positive_button", 2048, context);
        loadFromResources("hockeyapp_dialog_negative_button", Strings.DIALOG_NEGATIVE_BUTTON_ID, context);
        loadFromResources("hockeyapp_dialog_error_title", Strings.DIALOG_ERROR_TITLE_ID, context);
        loadFromResources("hockeyapp_dialog_error_message", Strings.DIALOG_ERROR_MESSAGE_ID, context);
        loadFromResources("hockeyapp_error_no_network_message", Strings.ERROR_NO_NETWORK_MESSAGE_ID, context);
    }

    private static void loadFromResources(String name, int resourceId, Context context) {
        int resId = context.getResources().getIdentifier(name, "string", context.getPackageName());
        if (resId != 0) {
            String string = context.getString(resId);
            if (!TextUtils.isEmpty(string)) {
                Strings.set(resourceId, string);
            }
        }
    }
}
