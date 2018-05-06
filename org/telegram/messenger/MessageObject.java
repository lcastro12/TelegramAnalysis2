package org.telegram.messenger;

import android.graphics.Typeface;
import android.support.v4.view.ViewCompat;
import android.text.Layout.Alignment;
import android.text.Spannable;
import android.text.Spannable.Factory;
import android.text.SpannableStringBuilder;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.DocumentAttribute;
import org.telegram.tgnet.TLRPC.InputStickerSet;
import org.telegram.tgnet.TLRPC.Message;
import org.telegram.tgnet.TLRPC.MessageEntity;
import org.telegram.tgnet.TLRPC.PhotoSize;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionScreenshotMessages;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionSetMessageTTL;
import org.telegram.tgnet.TLRPC.TL_documentAttributeAudio;
import org.telegram.tgnet.TLRPC.TL_documentAttributeImageSize;
import org.telegram.tgnet.TLRPC.TL_documentAttributeSticker;
import org.telegram.tgnet.TLRPC.TL_inputStickerSetEmpty;
import org.telegram.tgnet.TLRPC.TL_message;
import org.telegram.tgnet.TLRPC.TL_messageActionChannelCreate;
import org.telegram.tgnet.TLRPC.TL_messageActionChannelMigrateFrom;
import org.telegram.tgnet.TLRPC.TL_messageActionChatAddUser;
import org.telegram.tgnet.TLRPC.TL_messageActionChatCreate;
import org.telegram.tgnet.TLRPC.TL_messageActionChatDeletePhoto;
import org.telegram.tgnet.TLRPC.TL_messageActionChatDeleteUser;
import org.telegram.tgnet.TLRPC.TL_messageActionChatEditPhoto;
import org.telegram.tgnet.TLRPC.TL_messageActionChatEditTitle;
import org.telegram.tgnet.TLRPC.TL_messageActionChatJoinedByLink;
import org.telegram.tgnet.TLRPC.TL_messageActionChatMigrateTo;
import org.telegram.tgnet.TLRPC.TL_messageActionCreatedBroadcastList;
import org.telegram.tgnet.TLRPC.TL_messageActionLoginUnknownLocation;
import org.telegram.tgnet.TLRPC.TL_messageActionTTLChange;
import org.telegram.tgnet.TLRPC.TL_messageActionUserJoined;
import org.telegram.tgnet.TLRPC.TL_messageActionUserUpdatedPhoto;
import org.telegram.tgnet.TLRPC.TL_messageEmpty;
import org.telegram.tgnet.TLRPC.TL_messageEncryptedAction;
import org.telegram.tgnet.TLRPC.TL_messageEntityBold;
import org.telegram.tgnet.TLRPC.TL_messageEntityBotCommand;
import org.telegram.tgnet.TLRPC.TL_messageEntityCode;
import org.telegram.tgnet.TLRPC.TL_messageEntityEmail;
import org.telegram.tgnet.TLRPC.TL_messageEntityHashtag;
import org.telegram.tgnet.TLRPC.TL_messageEntityItalic;
import org.telegram.tgnet.TLRPC.TL_messageEntityMention;
import org.telegram.tgnet.TLRPC.TL_messageEntityPre;
import org.telegram.tgnet.TLRPC.TL_messageEntityTextUrl;
import org.telegram.tgnet.TLRPC.TL_messageEntityUrl;
import org.telegram.tgnet.TLRPC.TL_messageForwarded_old;
import org.telegram.tgnet.TLRPC.TL_messageForwarded_old2;
import org.telegram.tgnet.TLRPC.TL_messageMediaAudio;
import org.telegram.tgnet.TLRPC.TL_messageMediaContact;
import org.telegram.tgnet.TLRPC.TL_messageMediaDocument;
import org.telegram.tgnet.TLRPC.TL_messageMediaEmpty;
import org.telegram.tgnet.TLRPC.TL_messageMediaGeo;
import org.telegram.tgnet.TLRPC.TL_messageMediaPhoto;
import org.telegram.tgnet.TLRPC.TL_messageMediaUnsupported;
import org.telegram.tgnet.TLRPC.TL_messageMediaVenue;
import org.telegram.tgnet.TLRPC.TL_messageMediaVideo;
import org.telegram.tgnet.TLRPC.TL_messageMediaWebPage;
import org.telegram.tgnet.TLRPC.TL_messageService;
import org.telegram.tgnet.TLRPC.TL_message_old;
import org.telegram.tgnet.TLRPC.TL_message_old2;
import org.telegram.tgnet.TLRPC.TL_message_old3;
import org.telegram.tgnet.TLRPC.TL_message_old4;
import org.telegram.tgnet.TLRPC.TL_message_secret;
import org.telegram.tgnet.TLRPC.TL_peerChannel;
import org.telegram.tgnet.TLRPC.TL_peerUser;
import org.telegram.tgnet.TLRPC.TL_photoSizeEmpty;
import org.telegram.tgnet.TLRPC.TL_webPage;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.ui.Components.TypefaceSpan;
import org.telegram.ui.Components.URLSpanBotCommand;
import org.telegram.ui.Components.URLSpanNoUnderline;
import org.telegram.ui.Components.URLSpanNoUnderlineBold;
import org.telegram.ui.Components.URLSpanReplacement;

public class MessageObject {
    private static final int LINES_PER_BLOCK = 10;
    public static final int MESSAGE_SEND_STATE_SENDING = 1;
    public static final int MESSAGE_SEND_STATE_SEND_ERROR = 2;
    public static final int MESSAGE_SEND_STATE_SENT = 0;
    public static TextPaint textPaint;
    public static Pattern urlPattern;
    public float audioProgress;
    public int audioProgressSec;
    public int blockHeight;
    public CharSequence caption;
    public int contentType;
    public String dateKey;
    public boolean deleted;
    public int lastLineWidth;
    public CharSequence linkDescription;
    public Message messageOwner;
    public CharSequence messageText;
    public String monthKey;
    public ArrayList<PhotoSize> photoThumbs;
    public MessageObject replyMessageObject;
    public int textHeight;
    public ArrayList<TextLayoutBlock> textLayoutBlocks;
    public int textWidth;
    public int type;
    public VideoEditedInfo videoEditedInfo;
    public boolean viewsReloaded;

    public static class TextLayoutBlock {
        public int charactersOffset = 0;
        public StaticLayout textLayout;
        public float textXOffset = 0.0f;
        public float textYOffset = 0.0f;
    }

    public MessageObject(Message message, AbstractMap<Integer, User> users, boolean generateLayout) {
        this(message, users, null, generateLayout);
    }

    public MessageObject(Message message, AbstractMap<Integer, User> users, AbstractMap<Integer, Chat> chats, boolean generateLayout) {
        this.blockHeight = ConnectionsManager.DEFAULT_DATACENTER_ID;
        if (textPaint == null) {
            textPaint = new TextPaint(1);
            textPaint.setColor(ViewCompat.MEASURED_STATE_MASK);
            textPaint.linkColor = -13537377;
        }
        textPaint.setTextSize((float) AndroidUtilities.dp((float) MessagesController.getInstance().fontSize));
        this.messageOwner = message;
        if (message.replyMessage != null) {
            this.replyMessageObject = new MessageObject(message.replyMessage, users, chats, false);
        }
        String name;
        if (message instanceof TL_messageService) {
            if (message.action != null) {
                User fromUser = null;
                if (users != null) {
                    fromUser = (User) users.get(Integer.valueOf(message.from_id));
                }
                if (fromUser == null) {
                    fromUser = MessagesController.getInstance().getUser(Integer.valueOf(message.from_id));
                }
                if (message.action instanceof TL_messageActionChatCreate) {
                    if (isOut()) {
                        this.messageText = LocaleController.getString("ActionYouCreateGroup", C0553R.string.ActionYouCreateGroup);
                    } else if (fromUser != null) {
                        this.messageText = replaceWithLink(LocaleController.getString("ActionCreateGroup", C0553R.string.ActionCreateGroup), "un1", fromUser);
                    } else {
                        this.messageText = LocaleController.getString("ActionCreateGroup", C0553R.string.ActionCreateGroup).replace("un1", "");
                    }
                } else if (message.action instanceof TL_messageActionChatDeleteUser) {
                    if (message.action.user_id != message.from_id) {
                        whoUser = null;
                        if (users != null) {
                            whoUser = (User) users.get(Integer.valueOf(message.action.user_id));
                        }
                        if (whoUser == null) {
                            whoUser = MessagesController.getInstance().getUser(Integer.valueOf(message.action.user_id));
                        }
                        if (whoUser == null || fromUser == null) {
                            this.messageText = LocaleController.getString("ActionKickUser", C0553R.string.ActionKickUser).replace("un2", "").replace("un1", "");
                        } else if (isOut()) {
                            this.messageText = replaceWithLink(LocaleController.getString("ActionYouKickUser", C0553R.string.ActionYouKickUser), "un2", whoUser);
                        } else {
                            if (message.action.user_id == UserConfig.getClientUserId()) {
                                this.messageText = replaceWithLink(LocaleController.getString("ActionKickUserYou", C0553R.string.ActionKickUserYou), "un1", fromUser);
                            } else {
                                this.messageText = replaceWithLink(LocaleController.getString("ActionKickUser", C0553R.string.ActionKickUser), "un2", whoUser);
                                this.messageText = replaceWithLink(this.messageText, "un1", fromUser);
                            }
                        }
                    } else if (isOut()) {
                        this.messageText = LocaleController.getString("ActionYouLeftUser", C0553R.string.ActionYouLeftUser);
                    } else if (fromUser != null) {
                        this.messageText = replaceWithLink(LocaleController.getString("ActionLeftUser", C0553R.string.ActionLeftUser), "un1", fromUser);
                    } else {
                        this.messageText = LocaleController.getString("ActionLeftUser", C0553R.string.ActionLeftUser).replace("un1", "");
                    }
                } else if (message.action instanceof TL_messageActionChatAddUser) {
                    int singleUserId = this.messageOwner.action.user_id;
                    if (singleUserId == 0 && this.messageOwner.action.users.size() == 1) {
                        singleUserId = ((Integer) this.messageOwner.action.users.get(0)).intValue();
                    }
                    if (singleUserId != 0) {
                        whoUser = null;
                        if (users != null) {
                            whoUser = (User) users.get(Integer.valueOf(singleUserId));
                        }
                        if (whoUser == null) {
                            whoUser = MessagesController.getInstance().getUser(Integer.valueOf(singleUserId));
                        }
                        if (message.to_id.channel_id == 0 || isMegagroup()) {
                            if (whoUser == null || fromUser == null) {
                                this.messageText = LocaleController.getString("ActionAddUser", C0553R.string.ActionAddUser).replace("un2", "").replace("un1", "");
                            } else {
                                if (whoUser.id == fromUser.id) {
                                    if (isOut()) {
                                        this.messageText = LocaleController.getString("ActionAddUserSelfYou", C0553R.string.ActionAddUserSelfYou);
                                    } else {
                                        this.messageText = replaceWithLink(LocaleController.getString("ActionAddUserSelf", C0553R.string.ActionAddUserSelf), "un1", fromUser);
                                    }
                                } else if (isOut()) {
                                    this.messageText = replaceWithLink(LocaleController.getString("ActionYouAddUser", C0553R.string.ActionYouAddUser), "un2", whoUser);
                                } else if (singleUserId == UserConfig.getClientUserId()) {
                                    this.messageText = replaceWithLink(LocaleController.getString("ActionAddUserYou", C0553R.string.ActionAddUserYou), "un1", fromUser);
                                } else {
                                    this.messageText = replaceWithLink(LocaleController.getString("ActionAddUser", C0553R.string.ActionAddUser), "un2", whoUser);
                                    this.messageText = replaceWithLink(this.messageText, "un1", fromUser);
                                }
                            }
                        } else if (whoUser == null || whoUser.id == UserConfig.getClientUserId()) {
                            this.messageText = LocaleController.getString("ChannelJoined", C0553R.string.ChannelJoined);
                        } else if (isMegagroup()) {
                            this.messageText = replaceWithLink(LocaleController.getString("MegaAddedBy", C0553R.string.MegaAddedBy), "un1", whoUser);
                        } else {
                            this.messageText = replaceWithLink(LocaleController.getString("ChannelAddedBy", C0553R.string.ChannelAddedBy), "un1", whoUser);
                        }
                    } else if (isOut()) {
                        this.messageText = replaceWithLink(LocaleController.getString("ActionYouAddUser", C0553R.string.ActionYouAddUser), "un2", message.action.users, users);
                    } else {
                        this.messageText = replaceWithLink(LocaleController.getString("ActionAddUser", C0553R.string.ActionAddUser), "un2", message.action.users, users);
                        this.messageText = replaceWithLink(this.messageText, "un1", fromUser);
                    }
                } else if (message.action instanceof TL_messageActionChatJoinedByLink) {
                    if (fromUser == null) {
                        this.messageText = LocaleController.getString("ActionInviteUser", C0553R.string.ActionInviteUser).replace("un1", "");
                    } else if (isOut()) {
                        this.messageText = LocaleController.getString("ActionInviteYou", C0553R.string.ActionInviteYou);
                    } else {
                        this.messageText = replaceWithLink(LocaleController.getString("ActionInviteUser", C0553R.string.ActionInviteUser), "un1", fromUser);
                    }
                } else if (message.action instanceof TL_messageActionChatEditPhoto) {
                    if (message.to_id.channel_id != 0 && !isMegagroup()) {
                        this.messageText = LocaleController.getString("ActionChannelChangedPhoto", C0553R.string.ActionChannelChangedPhoto);
                    } else if (isOut()) {
                        this.messageText = LocaleController.getString("ActionYouChangedPhoto", C0553R.string.ActionYouChangedPhoto);
                    } else if (fromUser != null) {
                        this.messageText = replaceWithLink(LocaleController.getString("ActionChangedPhoto", C0553R.string.ActionChangedPhoto), "un1", fromUser);
                    } else {
                        this.messageText = LocaleController.getString("ActionChangedPhoto", C0553R.string.ActionChangedPhoto).replace("un1", "");
                    }
                } else if (message.action instanceof TL_messageActionChatEditTitle) {
                    if (message.to_id.channel_id != 0 && !isMegagroup()) {
                        this.messageText = LocaleController.getString("ActionChannelChangedTitle", C0553R.string.ActionChannelChangedTitle).replace("un2", message.action.title);
                    } else if (isOut()) {
                        this.messageText = LocaleController.getString("ActionYouChangedTitle", C0553R.string.ActionYouChangedTitle).replace("un2", message.action.title);
                    } else if (fromUser != null) {
                        this.messageText = replaceWithLink(LocaleController.getString("ActionChangedTitle", C0553R.string.ActionChangedTitle).replace("un2", message.action.title), "un1", fromUser);
                    } else {
                        this.messageText = LocaleController.getString("ActionChangedTitle", C0553R.string.ActionChangedTitle).replace("un1", "").replace("un2", message.action.title);
                    }
                } else if (message.action instanceof TL_messageActionChatDeletePhoto) {
                    if (message.to_id.channel_id != 0 && !isMegagroup()) {
                        this.messageText = LocaleController.getString("ActionChannelRemovedPhoto", C0553R.string.ActionChannelRemovedPhoto);
                    } else if (isOut()) {
                        this.messageText = LocaleController.getString("ActionYouRemovedPhoto", C0553R.string.ActionYouRemovedPhoto);
                    } else if (fromUser != null) {
                        this.messageText = replaceWithLink(LocaleController.getString("ActionRemovedPhoto", C0553R.string.ActionRemovedPhoto), "un1", fromUser);
                    } else {
                        this.messageText = LocaleController.getString("ActionRemovedPhoto", C0553R.string.ActionRemovedPhoto).replace("un1", "");
                    }
                } else if (message.action instanceof TL_messageActionTTLChange) {
                    if (message.action.ttl != 0) {
                        if (isOut()) {
                            this.messageText = LocaleController.formatString("MessageLifetimeChangedOutgoing", C0553R.string.MessageLifetimeChangedOutgoing, AndroidUtilities.formatTTLString(message.action.ttl));
                        } else if (fromUser != null) {
                            this.messageText = LocaleController.formatString("MessageLifetimeChanged", C0553R.string.MessageLifetimeChanged, UserObject.getFirstName(fromUser), AndroidUtilities.formatTTLString(message.action.ttl));
                        } else {
                            this.messageText = LocaleController.formatString("MessageLifetimeChanged", C0553R.string.MessageLifetimeChanged, "", AndroidUtilities.formatTTLString(message.action.ttl));
                        }
                    } else if (isOut()) {
                        this.messageText = LocaleController.getString("MessageLifetimeYouRemoved", C0553R.string.MessageLifetimeYouRemoved);
                    } else if (fromUser != null) {
                        this.messageText = LocaleController.formatString("MessageLifetimeRemoved", C0553R.string.MessageLifetimeRemoved, UserObject.getFirstName(fromUser));
                    } else {
                        this.messageText = LocaleController.formatString("MessageLifetimeRemoved", C0553R.string.MessageLifetimeRemoved, "");
                    }
                } else if (message.action instanceof TL_messageActionLoginUnknownLocation) {
                    String date;
                    long time = ((long) message.date) * 1000;
                    if (LocaleController.getInstance().formatterDay == null || LocaleController.getInstance().formatterYear == null) {
                        date = "" + message.date;
                    } else {
                        date = LocaleController.formatString("formatDateAtTime", C0553R.string.formatDateAtTime, LocaleController.getInstance().formatterYear.format(time), LocaleController.getInstance().formatterDay.format(time));
                    }
                    User to_user = UserConfig.getCurrentUser();
                    if (to_user == null) {
                        if (users != null) {
                            to_user = (User) users.get(Integer.valueOf(this.messageOwner.to_id.user_id));
                        }
                        if (to_user == null) {
                            to_user = MessagesController.getInstance().getUser(Integer.valueOf(this.messageOwner.to_id.user_id));
                        }
                    }
                    name = to_user != null ? UserObject.getFirstName(to_user) : "";
                    this.messageText = LocaleController.formatString("NotificationUnrecognizedDevice", C0553R.string.NotificationUnrecognizedDevice, name, date, message.action.title, message.action.address);
                } else if (message.action instanceof TL_messageActionUserJoined) {
                    if (fromUser != null) {
                        this.messageText = LocaleController.formatString("NotificationContactJoined", C0553R.string.NotificationContactJoined, UserObject.getUserName(fromUser));
                    } else {
                        this.messageText = LocaleController.formatString("NotificationContactJoined", C0553R.string.NotificationContactJoined, "");
                    }
                } else if (message.action instanceof TL_messageActionUserUpdatedPhoto) {
                    if (fromUser != null) {
                        this.messageText = LocaleController.formatString("NotificationContactNewPhoto", C0553R.string.NotificationContactNewPhoto, UserObject.getUserName(fromUser));
                    } else {
                        this.messageText = LocaleController.formatString("NotificationContactNewPhoto", C0553R.string.NotificationContactNewPhoto, "");
                    }
                } else if (message.action instanceof TL_messageEncryptedAction) {
                    if (message.action.encryptedAction instanceof TL_decryptedMessageActionScreenshotMessages) {
                        if (isOut()) {
                            this.messageText = LocaleController.formatString("ActionTakeScreenshootYou", C0553R.string.ActionTakeScreenshootYou, new Object[0]);
                        } else if (fromUser != null) {
                            this.messageText = replaceWithLink(LocaleController.getString("ActionTakeScreenshoot", C0553R.string.ActionTakeScreenshoot), "un1", fromUser);
                        } else {
                            this.messageText = LocaleController.formatString("ActionTakeScreenshoot", C0553R.string.ActionTakeScreenshoot, new Object[0]).replace("un1", "");
                        }
                    } else if (message.action.encryptedAction instanceof TL_decryptedMessageActionSetMessageTTL) {
                        if (message.action.encryptedAction.ttl_seconds != 0) {
                            if (isOut()) {
                                this.messageText = LocaleController.formatString("MessageLifetimeChangedOutgoing", C0553R.string.MessageLifetimeChangedOutgoing, AndroidUtilities.formatTTLString(action.ttl_seconds));
                            } else if (fromUser != null) {
                                this.messageText = LocaleController.formatString("MessageLifetimeChanged", C0553R.string.MessageLifetimeChanged, UserObject.getFirstName(fromUser), AndroidUtilities.formatTTLString(action.ttl_seconds));
                            } else {
                                this.messageText = LocaleController.formatString("MessageLifetimeChanged", C0553R.string.MessageLifetimeChanged, "", AndroidUtilities.formatTTLString(action.ttl_seconds));
                            }
                        } else if (isOut()) {
                            this.messageText = LocaleController.getString("MessageLifetimeYouRemoved", C0553R.string.MessageLifetimeYouRemoved);
                        } else if (fromUser != null) {
                            this.messageText = LocaleController.formatString("MessageLifetimeRemoved", C0553R.string.MessageLifetimeRemoved, UserObject.getFirstName(fromUser));
                        } else {
                            this.messageText = LocaleController.formatString("MessageLifetimeRemoved", C0553R.string.MessageLifetimeRemoved, "");
                        }
                    }
                } else if (message.action instanceof TL_messageActionCreatedBroadcastList) {
                    this.messageText = LocaleController.formatString("YouCreatedBroadcastList", C0553R.string.YouCreatedBroadcastList, new Object[0]);
                } else if (message.action instanceof TL_messageActionChannelCreate) {
                    if (isMegagroup()) {
                        this.messageText = LocaleController.getString("ActionCreateMega", C0553R.string.ActionCreateMega);
                    } else {
                        this.messageText = LocaleController.getString("ActionCreateChannel", C0553R.string.ActionCreateChannel);
                    }
                } else if (message.action instanceof TL_messageActionChatMigrateTo) {
                    this.messageText = LocaleController.getString("ActionMigrateFromGroup", C0553R.string.ActionMigrateFromGroup);
                } else if (message.action instanceof TL_messageActionChannelMigrateFrom) {
                    this.messageText = LocaleController.getString("ActionMigrateFromGroup", C0553R.string.ActionMigrateFromGroup);
                }
            }
        } else if (isMediaEmpty()) {
            this.messageText = message.message;
        } else if (message.media instanceof TL_messageMediaPhoto) {
            this.messageText = LocaleController.getString("AttachPhoto", C0553R.string.AttachPhoto);
        } else if (message.media instanceof TL_messageMediaVideo) {
            this.messageText = LocaleController.getString("AttachVideo", C0553R.string.AttachVideo);
        } else if ((message.media instanceof TL_messageMediaGeo) || (message.media instanceof TL_messageMediaVenue)) {
            this.messageText = LocaleController.getString("AttachLocation", C0553R.string.AttachLocation);
        } else if (message.media instanceof TL_messageMediaContact) {
            this.messageText = LocaleController.getString("AttachContact", C0553R.string.AttachContact);
        } else if (message.media instanceof TL_messageMediaUnsupported) {
            this.messageText = LocaleController.getString("UnsuppotedMedia", C0553R.string.UnsuppotedMedia);
        } else if (message.media instanceof TL_messageMediaDocument) {
            if (isSticker()) {
                String sch = getStrickerChar();
                if (sch == null || sch.length() <= 0) {
                    this.messageText = LocaleController.getString("AttachSticker", C0553R.string.AttachSticker);
                } else {
                    this.messageText = String.format("%s %s", new Object[]{sch, LocaleController.getString("AttachSticker", C0553R.string.AttachSticker)});
                }
            } else if (isMusic()) {
                this.messageText = LocaleController.getString("AttachMusic", C0553R.string.AttachMusic);
            } else {
                name = FileLoader.getDocumentFileName(message.media.document);
                if (name == null || name.length() <= 0) {
                    this.messageText = LocaleController.getString("AttachDocument", C0553R.string.AttachDocument);
                } else {
                    this.messageText = name;
                }
            }
        } else if (message.media instanceof TL_messageMediaAudio) {
            this.messageText = LocaleController.getString("AttachAudio", C0553R.string.AttachAudio);
        }
        if (this.messageText == null) {
            this.messageText = "";
        }
        if (generateLayout) {
            this.messageText = Emoji.replaceEmoji(this.messageText, textPaint.getFontMetricsInt(), AndroidUtilities.dp(20.0f), false);
        }
        if ((message instanceof TL_message) || (message instanceof TL_messageForwarded_old2)) {
            if (isMediaEmpty()) {
                this.type = 0;
                this.contentType = 0;
                if (this.messageText == null || this.messageText.length() == 0) {
                    this.messageText = "Empty message";
                }
            } else if (message.media instanceof TL_messageMediaPhoto) {
                this.type = 1;
                this.contentType = 1;
            } else if ((message.media instanceof TL_messageMediaGeo) || (message.media instanceof TL_messageMediaVenue)) {
                this.contentType = 1;
                this.type = 4;
            } else if (message.media instanceof TL_messageMediaVideo) {
                this.contentType = 1;
                this.type = 3;
            } else if (message.media instanceof TL_messageMediaContact) {
                this.contentType = 3;
                this.type = 12;
            } else if (message.media instanceof TL_messageMediaUnsupported) {
                this.type = 0;
                this.contentType = 0;
            } else if (message.media instanceof TL_messageMediaDocument) {
                this.contentType = 1;
                if (message.media.document.mime_type == null) {
                    this.type = 9;
                } else if (message.media.document.mime_type.equals("image/gif") && message.media.document.thumb != null && !(message.media.document.thumb instanceof TL_photoSizeEmpty)) {
                    this.type = 8;
                } else if (message.media.document.mime_type.equals("image/webp") && isSticker()) {
                    this.type = 13;
                } else if (isMusic()) {
                    this.type = 14;
                    this.contentType = 8;
                } else {
                    this.type = 9;
                }
            } else if (message.media instanceof TL_messageMediaAudio) {
                this.type = 2;
                this.contentType = 2;
            }
        } else if (message instanceof TL_messageService) {
            if (message.action instanceof TL_messageActionLoginUnknownLocation) {
                this.type = 0;
                this.contentType = 0;
            } else if ((message.action instanceof TL_messageActionChatEditPhoto) || (message.action instanceof TL_messageActionUserUpdatedPhoto)) {
                this.contentType = 4;
                this.type = 11;
            } else if (!(message.action instanceof TL_messageEncryptedAction)) {
                this.contentType = 4;
                this.type = 10;
            } else if ((message.action.encryptedAction instanceof TL_decryptedMessageActionScreenshotMessages) || (message.action.encryptedAction instanceof TL_decryptedMessageActionSetMessageTTL)) {
                this.contentType = 4;
                this.type = 10;
            } else {
                this.contentType = -1;
                this.type = -1;
            }
        }
        Calendar rightNow = new GregorianCalendar();
        rightNow.setTimeInMillis(((long) this.messageOwner.date) * 1000);
        int dateDay = rightNow.get(6);
        int dateYear = rightNow.get(1);
        int dateMonth = rightNow.get(2);
        this.dateKey = String.format("%d_%02d_%02d", new Object[]{Integer.valueOf(dateYear), Integer.valueOf(dateMonth), Integer.valueOf(dateDay)});
        if (this.contentType == 1 || this.contentType == 2 || this.contentType == 0 || this.contentType == 8) {
            this.monthKey = String.format("%d_%02d", new Object[]{Integer.valueOf(dateYear), Integer.valueOf(dateMonth)});
        } else if (this.contentType == 9) {
        }
        if (this.messageOwner.message != null && this.messageOwner.id < 0 && this.messageOwner.message.length() > 6 && (this.messageOwner.media instanceof TL_messageMediaVideo)) {
            this.videoEditedInfo = new VideoEditedInfo();
            this.videoEditedInfo.parseString(this.messageOwner.message);
        }
        generateCaption();
        if (generateLayout) {
            generateLayout();
        }
        generateThumbs(false);
    }

    public void generateThumbs(boolean update) {
        Iterator it;
        PhotoSize photoObject;
        Iterator i$;
        PhotoSize size;
        if (this.messageOwner instanceof TL_messageService) {
            if (!(this.messageOwner.action instanceof TL_messageActionChatEditPhoto)) {
                return;
            }
            if (!update) {
                this.photoThumbs = new ArrayList(this.messageOwner.action.photo.sizes);
            } else if (this.photoThumbs != null && !this.photoThumbs.isEmpty()) {
                it = this.photoThumbs.iterator();
                while (it.hasNext()) {
                    photoObject = (PhotoSize) it.next();
                    i$ = this.messageOwner.action.photo.sizes.iterator();
                    while (i$.hasNext()) {
                        size = (PhotoSize) i$.next();
                        if (!(size instanceof TL_photoSizeEmpty) && size.type.equals(photoObject.type)) {
                            photoObject.location = size.location;
                            break;
                        }
                    }
                }
            }
        } else if (this.messageOwner.media != null && !(this.messageOwner.media instanceof TL_messageMediaEmpty)) {
            if (this.messageOwner.media instanceof TL_messageMediaPhoto) {
                if (!update) {
                    this.photoThumbs = new ArrayList(this.messageOwner.media.photo.sizes);
                } else if (this.photoThumbs != null && !this.photoThumbs.isEmpty()) {
                    it = this.photoThumbs.iterator();
                    while (it.hasNext()) {
                        photoObject = (PhotoSize) it.next();
                        i$ = this.messageOwner.media.photo.sizes.iterator();
                        while (i$.hasNext()) {
                            size = (PhotoSize) i$.next();
                            if (!(size instanceof TL_photoSizeEmpty) && size.type.equals(photoObject.type)) {
                                photoObject.location = size.location;
                                break;
                            }
                        }
                    }
                }
            } else if (this.messageOwner.media instanceof TL_messageMediaVideo) {
                if (!update) {
                    this.photoThumbs = new ArrayList();
                    this.photoThumbs.add(this.messageOwner.media.video.thumb);
                } else if (this.photoThumbs != null && !this.photoThumbs.isEmpty() && this.messageOwner.media.video.thumb != null) {
                    ((PhotoSize) this.photoThumbs.get(0)).location = this.messageOwner.media.video.thumb.location;
                }
            } else if (this.messageOwner.media instanceof TL_messageMediaDocument) {
                if (!(this.messageOwner.media.document.thumb instanceof TL_photoSizeEmpty)) {
                    if (!update) {
                        this.photoThumbs = new ArrayList();
                        this.photoThumbs.add(this.messageOwner.media.document.thumb);
                    } else if (this.photoThumbs != null && !this.photoThumbs.isEmpty() && this.messageOwner.media.document.thumb != null) {
                        ((PhotoSize) this.photoThumbs.get(0)).location = this.messageOwner.media.document.thumb.location;
                    }
                }
            } else if ((this.messageOwner.media instanceof TL_messageMediaWebPage) && this.messageOwner.media.webpage.photo != null) {
                if (!update || this.photoThumbs == null) {
                    this.photoThumbs = new ArrayList(this.messageOwner.media.webpage.photo.sizes);
                } else if (!this.photoThumbs.isEmpty()) {
                    it = this.photoThumbs.iterator();
                    while (it.hasNext()) {
                        photoObject = (PhotoSize) it.next();
                        i$ = this.messageOwner.media.webpage.photo.sizes.iterator();
                        while (i$.hasNext()) {
                            size = (PhotoSize) i$.next();
                            if (!(size instanceof TL_photoSizeEmpty) && size.type.equals(photoObject.type)) {
                                photoObject.location = size.location;
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    public CharSequence replaceWithLink(CharSequence source, String param, ArrayList<Integer> uids, AbstractMap<Integer, User> usersDict) {
        if (TextUtils.indexOf(source, param) < 0) {
            return source;
        }
        SpannableStringBuilder names = new SpannableStringBuilder("");
        for (int a = 0; a < uids.size(); a++) {
            User user = null;
            if (usersDict != null) {
                user = (User) usersDict.get(uids.get(a));
            }
            if (user == null) {
                user = MessagesController.getInstance().getUser((Integer) uids.get(a));
            }
            if (user != null) {
                String name = UserObject.getUserName(user);
                int start = names.length();
                if (names.length() != 0) {
                    names.append(", ");
                }
                names.append(name);
                names.setSpan(new URLSpanNoUnderlineBold("" + user.id), start, name.length() + start, 33);
            }
        }
        return TextUtils.replace(source, new String[]{param}, new CharSequence[]{names});
    }

    public CharSequence replaceWithLink(CharSequence source, String param, TLObject object) {
        int start = TextUtils.indexOf(source, param);
        if (start < 0) {
            return source;
        }
        String name;
        int id;
        if (object instanceof User) {
            name = UserObject.getUserName((User) object);
            id = ((User) object).id;
        } else if (object instanceof Chat) {
            name = ((Chat) object).title;
            id = -((Chat) object).id;
        } else {
            name = "";
            id = 0;
        }
        SpannableStringBuilder builder = new SpannableStringBuilder(TextUtils.replace(source, new String[]{param}, new String[]{name}));
        builder.setSpan(new URLSpanNoUnderlineBold("" + id), start, name.length() + start, 33);
        return builder;
    }

    public String getExtension() {
        String fileName = getFileName();
        int idx = fileName.lastIndexOf(".");
        String ext = null;
        if (idx != -1) {
            ext = fileName.substring(idx + 1);
        }
        if (ext == null || ext.length() == 0) {
            ext = this.messageOwner.media.document.mime_type;
        }
        if (ext == null) {
            ext = "";
        }
        return ext.toUpperCase();
    }

    public String getFileName() {
        if (this.messageOwner.media instanceof TL_messageMediaVideo) {
            return FileLoader.getAttachFileName(this.messageOwner.media.video);
        }
        if (this.messageOwner.media instanceof TL_messageMediaDocument) {
            return FileLoader.getAttachFileName(this.messageOwner.media.document);
        }
        if (this.messageOwner.media instanceof TL_messageMediaAudio) {
            return FileLoader.getAttachFileName(this.messageOwner.media.audio);
        }
        if (this.messageOwner.media instanceof TL_messageMediaPhoto) {
            ArrayList<PhotoSize> sizes = this.messageOwner.media.photo.sizes;
            if (sizes.size() > 0) {
                PhotoSize sizeFull = FileLoader.getClosestPhotoSizeWithSize(sizes, AndroidUtilities.getPhotoSize());
                if (sizeFull != null) {
                    return FileLoader.getAttachFileName(sizeFull);
                }
            }
        }
        return "";
    }

    public int getFileType() {
        if (this.messageOwner.media instanceof TL_messageMediaVideo) {
            return 2;
        }
        if (this.messageOwner.media instanceof TL_messageMediaDocument) {
            return 3;
        }
        if (this.messageOwner.media instanceof TL_messageMediaAudio) {
            return 1;
        }
        if (this.messageOwner.media instanceof TL_messageMediaPhoto) {
            return 0;
        }
        return 4;
    }

    private static boolean containsUrls(CharSequence message) {
        if (message == null || message.length() < 2 || message.length() > 20480) {
            return false;
        }
        int length = message.length();
        int digitsInRow = 0;
        int schemeSequence = 0;
        int dotSequence = 0;
        char lastChar = '\u0000';
        int i = 0;
        while (i < length) {
            char c = message.charAt(i);
            if (c >= '0' && c <= '9') {
                digitsInRow++;
                if (digitsInRow >= 6) {
                    return true;
                }
                schemeSequence = 0;
                dotSequence = 0;
            } else if (c == ' ' || digitsInRow <= 0) {
                digitsInRow = 0;
            }
            if ((c == '@' || c == '#' || c == '/') && i == 0) {
                return true;
            }
            if (i != 0 && (message.charAt(i - 1) == ' ' || message.charAt(i - 1) == '\n')) {
                return true;
            }
            if (c == ':') {
                if (schemeSequence == 0) {
                    schemeSequence = 1;
                } else {
                    schemeSequence = 0;
                }
            } else if (c == '/') {
                if (schemeSequence == 2) {
                    return true;
                }
                if (schemeSequence == 1) {
                    schemeSequence++;
                } else {
                    schemeSequence = 0;
                }
            } else if (c == '.') {
                if (dotSequence != 0 || lastChar == ' ') {
                    dotSequence = 0;
                } else {
                    dotSequence++;
                }
            } else if (c != ' ' && lastChar == '.' && dotSequence == 1) {
                return true;
            } else {
                dotSequence = 0;
            }
            lastChar = c;
            i++;
        }
        return false;
    }

    public void generateLinkDescription() {
        if (this.linkDescription == null && (this.messageOwner.media instanceof TL_messageMediaWebPage) && (this.messageOwner.media.webpage instanceof TL_webPage) && this.messageOwner.media.webpage.description != null) {
            this.linkDescription = Factory.getInstance().newSpannable(this.messageOwner.media.webpage.description);
            if (containsUrls(this.linkDescription)) {
                try {
                    Linkify.addLinks((Spannable) this.linkDescription, 1);
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
            this.linkDescription = Emoji.replaceEmoji(this.linkDescription, textPaint.getFontMetricsInt(), AndroidUtilities.dp(20.0f), false);
        }
    }

    public void generateCaption() {
        if (this.caption == null && this.messageOwner.media != null && this.messageOwner.media.caption != null && this.messageOwner.media.caption.length() > 0) {
            this.caption = Emoji.replaceEmoji(this.messageOwner.media.caption, textPaint.getFontMetricsInt(), AndroidUtilities.dp(20.0f), false);
            if (containsUrls(this.caption)) {
                try {
                    Linkify.addLinks((Spannable) this.caption, 1);
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
                addUsernamesAndHashtags(this.caption, true);
            }
        }
    }

    private static void addUsernamesAndHashtags(CharSequence charSequence, boolean botCommands) {
        try {
            if (urlPattern == null) {
                urlPattern = Pattern.compile("(^|\\s)/[a-zA-Z@\\d_]{1,255}|(^|\\s)@[a-zA-Z\\d_]{5,32}|(^|\\s)#[\\w\\.]+");
            }
            Matcher matcher = urlPattern.matcher(charSequence);
            while (matcher.find()) {
                int start = matcher.start();
                int end = matcher.end();
                if (!(charSequence.charAt(start) == '@' || charSequence.charAt(start) == '#' || charSequence.charAt(start) == '/')) {
                    start++;
                }
                URLSpanNoUnderline url = null;
                if (charSequence.charAt(start) != '/') {
                    url = new URLSpanNoUnderline(charSequence.subSequence(start, end).toString());
                } else if (botCommands) {
                    url = new URLSpanBotCommand(charSequence.subSequence(start, end).toString());
                }
                if (url != null) {
                    ((Spannable) charSequence).setSpan(url, start, end, 0);
                }
            }
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
        }
    }

    public static void addLinks(CharSequence messageText) {
        addLinks(messageText, true);
    }

    public static void addLinks(CharSequence messageText, boolean botCommands) {
        if ((messageText instanceof Spannable) && containsUrls(messageText)) {
            if (messageText.length() < 100) {
                try {
                    Linkify.addLinks((Spannable) messageText, 5);
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            } else {
                try {
                    Linkify.addLinks((Spannable) messageText, 1);
                } catch (Throwable e2) {
                    FileLog.m611e("tmessages", e2);
                }
            }
            addUsernamesAndHashtags(messageText, botCommands);
        }
    }

    private void generateLayout() {
        if (this.type == 0 && this.messageOwner.to_id != null && this.messageText != null && this.messageText.length() != 0) {
            int a;
            int maxWidth;
            generateLinkDescription();
            this.textLayoutBlocks = new ArrayList();
            boolean useManualParse = this.messageOwner.entities.isEmpty() && ((this.messageOwner instanceof TL_message_old) || (this.messageOwner instanceof TL_message_old2) || (this.messageOwner instanceof TL_message_old3) || (this.messageOwner instanceof TL_message_old4) || (this.messageOwner instanceof TL_messageForwarded_old) || (this.messageOwner instanceof TL_messageForwarded_old2) || (this.messageOwner instanceof TL_message_secret) || ((isOut() && this.messageOwner.send_state != 0) || this.messageOwner.id < 0));
            if (useManualParse) {
                addLinks(this.messageText);
            } else if ((this.messageText instanceof Spannable) && this.messageText.length() < 100) {
                try {
                    Linkify.addLinks((Spannable) this.messageText, 4);
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
            if (this.messageText instanceof Spannable) {
                Spannable spannable = (Spannable) this.messageText;
                int count = this.messageOwner.entities.size();
                for (a = 0; a < count; a++) {
                    MessageEntity entity = (MessageEntity) this.messageOwner.entities.get(a);
                    if (entity.length > 0 && entity.offset >= 0 && entity.offset < this.messageOwner.message.length()) {
                        if (entity.offset + entity.length > this.messageOwner.message.length()) {
                            entity.length = this.messageOwner.message.length() - entity.offset;
                        }
                        if (entity instanceof TL_messageEntityBold) {
                            spannable.setSpan(new TypefaceSpan(AndroidUtilities.getTypeface("fonts/rmedium.ttf")), entity.offset, entity.offset + entity.length, 33);
                        } else if (entity instanceof TL_messageEntityItalic) {
                            spannable.setSpan(new TypefaceSpan(AndroidUtilities.getTypeface("fonts/ritalic.ttf")), entity.offset, entity.offset + entity.length, 33);
                        } else if ((entity instanceof TL_messageEntityCode) || (entity instanceof TL_messageEntityPre)) {
                            spannable.setSpan(new TypefaceSpan(Typeface.MONOSPACE), entity.offset, entity.offset + entity.length, 33);
                        } else if (!useManualParse) {
                            String url = this.messageOwner.message.substring(entity.offset, entity.offset + entity.length);
                            if (entity instanceof TL_messageEntityBotCommand) {
                                spannable.setSpan(new URLSpanBotCommand(url), entity.offset, entity.offset + entity.length, 33);
                            } else if ((entity instanceof TL_messageEntityHashtag) || (entity instanceof TL_messageEntityMention)) {
                                spannable.setSpan(new URLSpanNoUnderline(url), entity.offset, entity.offset + entity.length, 33);
                            } else if (entity instanceof TL_messageEntityEmail) {
                                spannable.setSpan(new URLSpanReplacement("mailto:" + url), entity.offset, entity.offset + entity.length, 33);
                            } else if (entity instanceof TL_messageEntityUrl) {
                                if (url.toLowerCase().startsWith("http")) {
                                    spannable.setSpan(new URLSpan(url), entity.offset, entity.offset + entity.length, 33);
                                } else {
                                    spannable.setSpan(new URLSpan("http://" + url), entity.offset, entity.offset + entity.length, 33);
                                }
                            } else if (entity instanceof TL_messageEntityTextUrl) {
                                spannable.setSpan(new URLSpanReplacement(entity.url), entity.offset, entity.offset + entity.length, 33);
                            }
                        }
                    }
                }
            }
            if (AndroidUtilities.isTablet()) {
                if (this.messageOwner.from_id <= 0 || ((this.messageOwner.to_id.channel_id == 0 && this.messageOwner.to_id.chat_id == 0) || isOut())) {
                    maxWidth = AndroidUtilities.getMinTabletSide() - AndroidUtilities.dp(80.0f);
                } else {
                    maxWidth = AndroidUtilities.getMinTabletSide() - AndroidUtilities.dp(122.0f);
                }
            } else if (this.messageOwner.from_id <= 0 || ((this.messageOwner.to_id.channel_id == 0 && this.messageOwner.to_id.chat_id == 0) || isOut())) {
                maxWidth = Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y) - AndroidUtilities.dp(80.0f);
            } else {
                maxWidth = Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y) - AndroidUtilities.dp(122.0f);
            }
            try {
                StaticLayout textLayout = new StaticLayout(this.messageText, textPaint, maxWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                this.textHeight = textLayout.getHeight();
                int linesCount = textLayout.getLineCount();
                int blocksCount = (int) Math.ceil((double) (((float) linesCount) / 10.0f));
                int linesOffset = 0;
                float prevOffset = 0.0f;
                for (a = 0; a < blocksCount; a++) {
                    int currentBlockLinesCount = Math.min(10, linesCount - linesOffset);
                    TextLayoutBlock block = new TextLayoutBlock();
                    if (blocksCount == 1) {
                        block.textLayout = textLayout;
                        block.textYOffset = 0.0f;
                        block.charactersOffset = 0;
                        this.blockHeight = this.textHeight;
                    } else {
                        int startCharacter = textLayout.getLineStart(linesOffset);
                        int endCharacter = textLayout.getLineEnd((linesOffset + currentBlockLinesCount) - 1);
                        if (endCharacter >= startCharacter) {
                            block.charactersOffset = startCharacter;
                            try {
                                block.textLayout = new StaticLayout(this.messageText.subSequence(startCharacter, endCharacter), textPaint, maxWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                                block.textYOffset = (float) textLayout.getLineTop(linesOffset);
                                if (a != 0) {
                                    this.blockHeight = Math.min(this.blockHeight, (int) (block.textYOffset - prevOffset));
                                }
                                prevOffset = block.textYOffset;
                                if (a == blocksCount - 1) {
                                    currentBlockLinesCount = Math.max(currentBlockLinesCount, block.textLayout.getLineCount());
                                    try {
                                        this.textHeight = Math.max(this.textHeight, (int) (block.textYOffset + ((float) block.textLayout.getHeight())));
                                    } catch (Throwable e2) {
                                        FileLog.m611e("tmessages", e2);
                                    }
                                }
                            } catch (Throwable e22) {
                                FileLog.m611e("tmessages", e22);
                            }
                        }
                    }
                    this.textLayoutBlocks.add(block);
                    float lastLeft = 0.0f;
                    block.textXOffset = 0.0f;
                    try {
                        float lastLeft2 = block.textLayout.getLineLeft(currentBlockLinesCount - 1);
                        block.textXOffset = lastLeft2;
                        lastLeft = lastLeft2;
                    } catch (Throwable e222) {
                        FileLog.m611e("tmessages", e222);
                    }
                    float lastLine = 0.0f;
                    try {
                        lastLine = block.textLayout.getLineWidth(currentBlockLinesCount - 1);
                    } catch (Throwable e2222) {
                        FileLog.m611e("tmessages", e2222);
                    }
                    int linesMaxWidth = (int) Math.ceil((double) lastLine);
                    boolean hasNonRTL = false;
                    if (a == blocksCount - 1) {
                        this.lastLineWidth = linesMaxWidth;
                    }
                    int lastLineWidthWithLeft = (int) Math.ceil((double) (lastLine + lastLeft));
                    int linesMaxWidthWithLeft = lastLineWidthWithLeft;
                    if (lastLeft == 0.0f) {
                        hasNonRTL = true;
                    }
                    if (currentBlockLinesCount > 1) {
                        float textRealMaxWidth = 0.0f;
                        float textRealMaxWidthWithLeft = 0.0f;
                        for (int n = 0; n < currentBlockLinesCount; n++) {
                            float lineWidth;
                            float lineLeft;
                            try {
                                lineWidth = block.textLayout.getLineWidth(n);
                            } catch (Throwable e22222) {
                                FileLog.m611e("tmessages", e22222);
                                lineWidth = 0.0f;
                            }
                            if (lineWidth > ((float) (maxWidth + 100))) {
                                lineWidth = (float) maxWidth;
                            }
                            try {
                                lineLeft = block.textLayout.getLineLeft(n);
                            } catch (Throwable e222222) {
                                FileLog.m611e("tmessages", e222222);
                                lineLeft = 0.0f;
                            }
                            block.textXOffset = Math.min(block.textXOffset, lineLeft);
                            if (lineLeft == 0.0f) {
                                hasNonRTL = true;
                            }
                            textRealMaxWidth = Math.max(textRealMaxWidth, lineWidth);
                            textRealMaxWidthWithLeft = Math.max(textRealMaxWidthWithLeft, lineWidth + lineLeft);
                            linesMaxWidth = Math.max(linesMaxWidth, (int) Math.ceil((double) lineWidth));
                            linesMaxWidthWithLeft = Math.max(linesMaxWidthWithLeft, (int) Math.ceil((double) (lineWidth + lineLeft)));
                        }
                        if (hasNonRTL) {
                            textRealMaxWidth = textRealMaxWidthWithLeft;
                            if (a == blocksCount - 1) {
                                this.lastLineWidth = lastLineWidthWithLeft;
                            }
                        } else if (a == blocksCount - 1) {
                            this.lastLineWidth = linesMaxWidth;
                        }
                        this.textWidth = Math.max(this.textWidth, (int) Math.ceil((double) textRealMaxWidth));
                    } else {
                        this.textWidth = Math.max(this.textWidth, Math.min(maxWidth, linesMaxWidth));
                    }
                    if (hasNonRTL) {
                        block.textXOffset = 0.0f;
                    }
                    linesOffset += currentBlockLinesCount;
                }
                if (this.blockHeight == 0) {
                    this.blockHeight = 1;
                }
            } catch (Throwable e2222222) {
                FileLog.m611e("tmessages", e2222222);
            }
        }
    }

    public boolean isOut() {
        return this.messageOwner.out;
    }

    public boolean isOutOwner() {
        return this.messageOwner.out && this.messageOwner.from_id > 0;
    }

    public boolean isUnread() {
        return this.messageOwner.unread;
    }

    public boolean isContentUnread() {
        return this.messageOwner.media_unread;
    }

    public void setIsRead() {
        this.messageOwner.unread = false;
    }

    public int getUnradFlags() {
        return getUnreadFlags(this.messageOwner);
    }

    public static int getUnreadFlags(Message message) {
        int flags = 0;
        if (!message.unread) {
            flags = 0 | 1;
        }
        if (message.media_unread) {
            return flags;
        }
        return flags | 2;
    }

    public void setContentIsRead() {
        this.messageOwner.media_unread = false;
    }

    public int getId() {
        return this.messageOwner.id;
    }

    public boolean isSecretPhoto() {
        return (this.messageOwner instanceof TL_message_secret) && (this.messageOwner.media instanceof TL_messageMediaPhoto) && this.messageOwner.ttl > 0 && this.messageOwner.ttl <= 60;
    }

    public boolean isSecretMedia() {
        return (this.messageOwner instanceof TL_message_secret) && (((this.messageOwner.media instanceof TL_messageMediaPhoto) && this.messageOwner.ttl > 0 && this.messageOwner.ttl <= 60) || (this.messageOwner.media instanceof TL_messageMediaAudio) || (this.messageOwner.media instanceof TL_messageMediaVideo));
    }

    public static void setUnreadFlags(Message message, int flag) {
        boolean z;
        boolean z2 = true;
        if ((flag & 1) == 0) {
            z = true;
        } else {
            z = false;
        }
        message.unread = z;
        if ((flag & 2) != 0) {
            z2 = false;
        }
        message.media_unread = z2;
    }

    public static boolean isUnread(Message message) {
        return message.unread;
    }

    public static boolean isContentUnread(Message message) {
        return message.media_unread;
    }

    public boolean isImportant() {
        return isImportant(this.messageOwner);
    }

    public boolean isMegagroup() {
        return isMegagroup(this.messageOwner);
    }

    public static boolean isImportant(Message message) {
        if (isMegagroup(message)) {
            if (message.from_id <= 0) {
                return true;
            }
            return false;
        } else if (message.to_id.channel_id == 0 || (message.from_id > 0 && !message.mentioned && !message.out && (message.flags & 256) != 0)) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean isMegagroup(Message message) {
        return (message.flags & Integer.MIN_VALUE) != 0;
    }

    public static boolean isOut(Message message) {
        return message.out;
    }

    public long getDialogId() {
        return getDialogId(this.messageOwner);
    }

    public static long getDialogId(Message message) {
        if (message.dialog_id == 0 && message.to_id != null) {
            if (message.to_id.chat_id != 0) {
                if (message.to_id.chat_id < 0) {
                    message.dialog_id = AndroidUtilities.makeBroadcastId(message.to_id.chat_id);
                } else {
                    message.dialog_id = (long) (-message.to_id.chat_id);
                }
            } else if (message.to_id.channel_id != 0) {
                message.dialog_id = (long) (-message.to_id.channel_id);
            } else if (isOut(message)) {
                message.dialog_id = (long) message.to_id.user_id;
            } else {
                message.dialog_id = (long) message.from_id;
            }
        }
        return message.dialog_id;
    }

    public boolean isSending() {
        return this.messageOwner.send_state == 1 && this.messageOwner.id < 0;
    }

    public boolean isSendError() {
        return this.messageOwner.send_state == 2 && this.messageOwner.id < 0;
    }

    public boolean isSent() {
        return this.messageOwner.send_state == 0 || this.messageOwner.id > 0;
    }

    public String getSecretTimeString() {
        if (!isSecretMedia()) {
            return null;
        }
        int secondsLeft = this.messageOwner.ttl;
        if (this.messageOwner.destroyTime != 0) {
            secondsLeft = Math.max(0, this.messageOwner.destroyTime - ConnectionsManager.getInstance().getCurrentTime());
        }
        if (secondsLeft < 60) {
            return secondsLeft + "s";
        }
        return (secondsLeft / 60) + "m";
    }

    public String getDocumentName() {
        if (this.messageOwner.media == null || this.messageOwner.media.document == null) {
            return "";
        }
        return FileLoader.getDocumentFileName(this.messageOwner.media.document);
    }

    public static boolean isStickerMessage(Message message) {
        if (!(message.media == null || message.media.document == null)) {
            Iterator i$ = message.media.document.attributes.iterator();
            while (i$.hasNext()) {
                if (((DocumentAttribute) i$.next()) instanceof TL_documentAttributeSticker) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isMusicMessage(Message message) {
        if (!(message.media == null || message.media.document == null)) {
            Iterator i$ = message.media.document.attributes.iterator();
            while (i$.hasNext()) {
                if (((DocumentAttribute) i$.next()) instanceof TL_documentAttributeAudio) {
                    return true;
                }
            }
        }
        return false;
    }

    public static InputStickerSet getInputStickerSet(Message message) {
        if (message.media == null || message.media.document == null) {
            return null;
        }
        Iterator i$ = message.media.document.attributes.iterator();
        while (i$.hasNext()) {
            DocumentAttribute attribute = (DocumentAttribute) i$.next();
            if (attribute instanceof TL_documentAttributeSticker) {
                if (attribute.stickerset instanceof TL_inputStickerSetEmpty) {
                    return null;
                }
                return attribute.stickerset;
            }
        }
        return null;
    }

    public String getStrickerChar() {
        if (!(this.messageOwner.media == null || this.messageOwner.media.document == null)) {
            Iterator i$ = this.messageOwner.media.document.attributes.iterator();
            while (i$.hasNext()) {
                DocumentAttribute attribute = (DocumentAttribute) i$.next();
                if (attribute instanceof TL_documentAttributeSticker) {
                    return attribute.alt;
                }
            }
        }
        return null;
    }

    public int getApproximateHeight() {
        if (this.type == 0) {
            return this.textHeight;
        }
        if (this.contentType == 2) {
            return AndroidUtilities.dp(68.0f);
        }
        if (this.contentType == 3) {
            return AndroidUtilities.dp(71.0f);
        }
        if (this.type == 9) {
            return AndroidUtilities.dp(100.0f);
        }
        if (this.type == 4) {
            return AndroidUtilities.dp(114.0f);
        }
        if (this.type == 14) {
            return AndroidUtilities.dp(78.0f);
        }
        int photoHeight;
        int photoWidth;
        if (this.type == 13) {
            float maxWidth;
            float maxHeight = ((float) AndroidUtilities.displaySize.y) * 0.4f;
            if (AndroidUtilities.isTablet()) {
                maxWidth = ((float) AndroidUtilities.getMinTabletSide()) * 0.5f;
            } else {
                maxWidth = ((float) AndroidUtilities.displaySize.x) * 0.5f;
            }
            photoHeight = 0;
            photoWidth = 0;
            Iterator i$ = this.messageOwner.media.document.attributes.iterator();
            while (i$.hasNext()) {
                DocumentAttribute attribute = (DocumentAttribute) i$.next();
                if (attribute instanceof TL_documentAttributeImageSize) {
                    photoWidth = attribute.f135w;
                    photoHeight = attribute.f134h;
                    break;
                }
            }
            if (photoWidth == 0) {
                photoHeight = (int) maxHeight;
                photoWidth = photoHeight + AndroidUtilities.dp(100.0f);
            }
            if (((float) photoHeight) > maxHeight) {
                photoWidth = (int) (((float) photoWidth) * (maxHeight / ((float) photoHeight)));
                photoHeight = (int) maxHeight;
            }
            if (((float) photoWidth) > maxWidth) {
                photoHeight = (int) (((float) photoHeight) * (maxWidth / ((float) photoWidth)));
            }
            return AndroidUtilities.dp(14.0f) + photoHeight;
        }
        if (AndroidUtilities.isTablet()) {
            photoWidth = (int) (((float) AndroidUtilities.getMinTabletSide()) * 0.7f);
        } else {
            photoWidth = (int) (((float) Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y)) * 0.7f);
        }
        photoHeight = photoWidth + AndroidUtilities.dp(100.0f);
        if (photoWidth > AndroidUtilities.getPhotoSize()) {
            photoWidth = AndroidUtilities.getPhotoSize();
        }
        if (photoHeight > AndroidUtilities.getPhotoSize()) {
            photoHeight = AndroidUtilities.getPhotoSize();
        }
        PhotoSize currentPhotoObject = FileLoader.getClosestPhotoSizeWithSize(this.photoThumbs, AndroidUtilities.getPhotoSize());
        if (currentPhotoObject != null) {
            int h = (int) (((float) currentPhotoObject.f138h) / (((float) currentPhotoObject.f139w) / ((float) photoWidth)));
            if (h == 0) {
                h = AndroidUtilities.dp(100.0f);
            }
            if (h > photoHeight) {
                h = photoHeight;
            } else if (h < AndroidUtilities.dp(BitmapDescriptorFactory.HUE_GREEN)) {
                h = AndroidUtilities.dp(BitmapDescriptorFactory.HUE_GREEN);
            }
            if (isSecretPhoto()) {
                if (AndroidUtilities.isTablet()) {
                    h = (int) (((float) AndroidUtilities.getMinTabletSide()) * 0.5f);
                } else {
                    h = (int) (((float) Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y)) * 0.5f);
                }
            }
            photoHeight = h;
        }
        return AndroidUtilities.dp(14.0f) + photoHeight;
    }

    public boolean isSticker() {
        return isStickerMessage(this.messageOwner);
    }

    public boolean isMusic() {
        return isMusicMessage(this.messageOwner);
    }

    public String getMusicTitle() {
        Iterator i$ = this.messageOwner.media.document.attributes.iterator();
        while (i$.hasNext()) {
            DocumentAttribute attribute = (DocumentAttribute) i$.next();
            if (attribute instanceof TL_documentAttributeAudio) {
                String title = attribute.title;
                if (title != null && title.length() != 0) {
                    return title;
                }
                title = FileLoader.getDocumentFileName(this.messageOwner.media.document);
                if (title == null || title.length() == 0) {
                    return LocaleController.getString("AudioUnknownTitle", C0553R.string.AudioUnknownTitle);
                }
                return title;
            }
        }
        return "";
    }

    public String getMusicAuthor() {
        Iterator i$ = this.messageOwner.media.document.attributes.iterator();
        while (i$.hasNext()) {
            DocumentAttribute attribute = (DocumentAttribute) i$.next();
            if (attribute instanceof TL_documentAttributeAudio) {
                String performer = attribute.performer;
                if (performer == null || performer.length() == 0) {
                    return LocaleController.getString("AudioUnknownArtist", C0553R.string.AudioUnknownArtist);
                }
                return performer;
            }
        }
        return "";
    }

    public InputStickerSet getInputStickerSet() {
        return getInputStickerSet(this.messageOwner);
    }

    public boolean isForwarded() {
        return (this.messageOwner.flags & 4) != 0;
    }

    public boolean isReply() {
        return ((this.replyMessageObject != null && (this.replyMessageObject.messageOwner instanceof TL_messageEmpty)) || this.messageOwner.reply_to_msg_id == 0 || (this.messageOwner.flags & 8) == 0) ? false : true;
    }

    public boolean isMediaEmpty() {
        return isMediaEmpty(this.messageOwner);
    }

    public static boolean isMediaEmpty(Message message) {
        return message == null || message.media == null || (message.media instanceof TL_messageMediaEmpty) || (message.media instanceof TL_messageMediaWebPage);
    }

    public boolean canDeleteMessage(Chat chat) {
        return canDeleteMessage(this.messageOwner, chat);
    }

    public static boolean canDeleteMessage(Message message, Chat chat) {
        boolean z = false;
        if (message.id < 0) {
            return true;
        }
        if (chat == null && message.to_id.channel_id != 0) {
            chat = MessagesController.getInstance().getChat(Integer.valueOf(message.to_id.channel_id));
        }
        if (ChatObject.isChannel(chat)) {
            if (message.id == 1) {
                return false;
            }
            if (chat.creator) {
                return true;
            }
            if (chat.editor) {
                if (isOut(message) || message.from_id > 0) {
                    return true;
                }
            } else if (chat.moderator) {
                if (message.from_id > 0) {
                    return true;
                }
            } else if (isOut(message) && message.from_id > 0) {
                return true;
            }
        }
        if (isOut(message) || !ChatObject.isChannel(chat)) {
            z = true;
        }
        return z;
    }

    public String getForwardedName() {
        if (this.messageOwner.fwd_from_id instanceof TL_peerChannel) {
            Chat chat = MessagesController.getInstance().getChat(Integer.valueOf(this.messageOwner.fwd_from_id.channel_id));
            if (chat != null) {
                return chat.title;
            }
        } else if (this.messageOwner.fwd_from_id instanceof TL_peerUser) {
            User user = MessagesController.getInstance().getUser(Integer.valueOf(this.messageOwner.fwd_from_id.user_id));
            if (user != null) {
                return UserObject.getUserName(user);
            }
        }
        return null;
    }
}
