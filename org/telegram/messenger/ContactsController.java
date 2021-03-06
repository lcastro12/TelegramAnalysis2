package org.telegram.messenger;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build.VERSION;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;
import android.util.SparseArray;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.InputUser;
import org.telegram.tgnet.TLRPC.PrivacyRule;
import org.telegram.tgnet.TLRPC.TL_account_getAccountTTL;
import org.telegram.tgnet.TLRPC.TL_account_getPrivacy;
import org.telegram.tgnet.TLRPC.TL_account_privacyRules;
import org.telegram.tgnet.TLRPC.TL_contact;
import org.telegram.tgnet.TLRPC.TL_contactStatus;
import org.telegram.tgnet.TLRPC.TL_contacts_contactsNotModified;
import org.telegram.tgnet.TLRPC.TL_contacts_deleteContacts;
import org.telegram.tgnet.TLRPC.TL_contacts_getContacts;
import org.telegram.tgnet.TLRPC.TL_contacts_getStatuses;
import org.telegram.tgnet.TLRPC.TL_contacts_importContacts;
import org.telegram.tgnet.TLRPC.TL_contacts_importedContacts;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_help_getInviteText;
import org.telegram.tgnet.TLRPC.TL_help_inviteText;
import org.telegram.tgnet.TLRPC.TL_importedContact;
import org.telegram.tgnet.TLRPC.TL_inputPhoneContact;
import org.telegram.tgnet.TLRPC.TL_inputPrivacyKeyStatusTimestamp;
import org.telegram.tgnet.TLRPC.TL_userStatusLastMonth;
import org.telegram.tgnet.TLRPC.TL_userStatusLastWeek;
import org.telegram.tgnet.TLRPC.TL_userStatusRecently;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.Vector;
import org.telegram.tgnet.TLRPC.contacts_Contacts;

public class ContactsController {
    private static volatile ContactsController Instance = null;
    private static final Object loadContactsSync = new Object();
    public ArrayList<TL_contact> contacts = new ArrayList();
    public HashMap<Integer, Contact> contactsBook = new HashMap();
    private boolean contactsBookLoaded = false;
    public HashMap<String, Contact> contactsBookSPhones = new HashMap();
    public HashMap<String, TL_contact> contactsByPhone = new HashMap();
    public SparseArray<TL_contact> contactsDict = new SparseArray();
    public boolean contactsLoaded = false;
    private boolean contactsSyncInProgress = false;
    private Account currentAccount;
    private ArrayList<Integer> delayedContactsUpdate = new ArrayList();
    private int deleteAccountTTL;
    private boolean ignoreChanges = false;
    private String inviteText;
    private String lastContactsVersions = "";
    private boolean loadingContacts = false;
    private int loadingDeleteInfo = 0;
    private int loadingLastSeenInfo = 0;
    private final Object observerLock = new Object();
    public ArrayList<Contact> phoneBookContacts = new ArrayList();
    private ArrayList<PrivacyRule> privacyRules = null;
    private String[] projectionNames = new String[]{"contact_id", "data2", "data3", "display_name", "data5"};
    private String[] projectionPhones = new String[]{"contact_id", "data1", "data2", "data3"};
    private HashMap<String, String> sectionsToReplace = new HashMap();
    public ArrayList<String> sortedUsersMutualSectionsArray = new ArrayList();
    public ArrayList<String> sortedUsersSectionsArray = new ArrayList();
    private boolean updatingInviteText = false;
    public HashMap<String, ArrayList<TL_contact>> usersMutualSectionsDict = new HashMap();
    public HashMap<String, ArrayList<TL_contact>> usersSectionsDict = new HashMap();

    class C03192 implements Runnable {
        C03192() {
        }

        public void run() {
            if (ContactsController.this.checkContactsInternal()) {
                FileLog.m609e("tmessages", "detected contacts change");
                ContactsController.getInstance().performSyncPhoneBook(ContactsController.getInstance().getContactsCopy(ContactsController.getInstance().contactsBook), true, false, true);
            }
        }
    }

    class C03203 implements Runnable {
        C03203() {
        }

        public void run() {
            if (!ContactsController.this.contacts.isEmpty() || ContactsController.this.contactsLoaded) {
                synchronized (ContactsController.loadContactsSync) {
                    ContactsController.this.loadingContacts = false;
                }
                return;
            }
            ContactsController.this.loadContacts(true, false);
        }
    }

    class C03377 implements Comparator<Contact> {
        C03377() {
        }

        public int compare(Contact contact, Contact contact2) {
            String toComapre1 = contact.first_name;
            if (toComapre1.length() == 0) {
                toComapre1 = contact.last_name;
            }
            String toComapre2 = contact2.first_name;
            if (toComapre2.length() == 0) {
                toComapre2 = contact2.last_name;
            }
            return toComapre1.compareTo(toComapre2);
        }
    }

    class C03388 implements Comparator<TL_contact> {
        C03388() {
        }

        public int compare(TL_contact tl_contact, TL_contact tl_contact2) {
            return UserObject.getFirstName(MessagesController.getInstance().getUser(Integer.valueOf(tl_contact.user_id))).compareTo(UserObject.getFirstName(MessagesController.getInstance().getUser(Integer.valueOf(tl_contact2.user_id))));
        }
    }

    class C03399 implements Comparator<String> {
        C03399() {
        }

        public int compare(String s, String s2) {
            char cv1 = s.charAt(0);
            char cv2 = s2.charAt(0);
            if (cv1 == '#') {
                return 1;
            }
            if (cv2 == '#') {
                return -1;
            }
            return s.compareTo(s2);
        }
    }

    public static class Contact {
        public String first_name;
        public int id;
        public String last_name;
        public ArrayList<Integer> phoneDeleted = new ArrayList();
        public ArrayList<String> phoneTypes = new ArrayList();
        public ArrayList<String> phones = new ArrayList();
        public ArrayList<String> shortPhones = new ArrayList();
    }

    class C14451 implements RequestDelegate {
        C14451() {
        }

        public void run(TLObject response, TL_error error) {
            if (error == null) {
                final TL_help_inviteText res = (TL_help_inviteText) response;
                if (res.message.length() != 0) {
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        public void run() {
                            ContactsController.this.updatingInviteText = false;
                            Editor editor = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0).edit();
                            editor.putString("invitetext", res.message);
                            editor.putInt("invitetexttime", (int) (System.currentTimeMillis() / 1000));
                            editor.commit();
                        }
                    });
                }
            }
        }
    }

    class C14475 implements RequestDelegate {

        class C03271 implements Runnable {
            C03271() {
            }

            public void run() {
                synchronized (ContactsController.loadContactsSync) {
                    ContactsController.this.loadingContacts = false;
                }
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.contactsDidLoaded, new Object[0]);
            }
        }

        C14475() {
        }

        public void run(TLObject response, TL_error error) {
            if (error == null) {
                contacts_Contacts res = (contacts_Contacts) response;
                if (res instanceof TL_contacts_contactsNotModified) {
                    ContactsController.this.contactsLoaded = true;
                    if (!ContactsController.this.delayedContactsUpdate.isEmpty() && ContactsController.this.contactsBookLoaded) {
                        ContactsController.this.applyContactsUpdates(ContactsController.this.delayedContactsUpdate, null, null, null);
                        ContactsController.this.delayedContactsUpdate.clear();
                    }
                    AndroidUtilities.runOnUIThread(new C03271());
                    FileLog.m609e("tmessages", "load contacts don't change");
                    return;
                }
                ContactsController.this.processLoadedContacts(res.contacts, res.users, 0);
            }
        }
    }

    public static ContactsController getInstance() {
        ContactsController localInstance = Instance;
        if (localInstance == null) {
            synchronized (ContactsController.class) {
                try {
                    localInstance = Instance;
                    if (localInstance == null) {
                        ContactsController localInstance2 = new ContactsController();
                        try {
                            Instance = localInstance2;
                            localInstance = localInstance2;
                        } catch (Throwable th) {
                            Throwable th2 = th;
                            localInstance = localInstance2;
                            throw th2;
                        }
                    }
                } catch (Throwable th3) {
                    th2 = th3;
                    throw th2;
                }
            }
        }
        return localInstance;
    }

    public ContactsController() {
        if (ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0).getBoolean("needGetStatuses", false)) {
            reloadContactsStatuses();
        }
        this.sectionsToReplace.put("À", "A");
        this.sectionsToReplace.put("Á", "A");
        this.sectionsToReplace.put("Ä", "A");
        this.sectionsToReplace.put("Ù", "U");
        this.sectionsToReplace.put("Ú", "U");
        this.sectionsToReplace.put("Ü", "U");
        this.sectionsToReplace.put("Ì", "I");
        this.sectionsToReplace.put("Í", "I");
        this.sectionsToReplace.put("Ï", "I");
        this.sectionsToReplace.put("È", "E");
        this.sectionsToReplace.put("É", "E");
        this.sectionsToReplace.put("Ê", "E");
        this.sectionsToReplace.put("Ë", "E");
        this.sectionsToReplace.put("Ò", "O");
        this.sectionsToReplace.put("Ó", "O");
        this.sectionsToReplace.put("Ö", "O");
        this.sectionsToReplace.put("Ç", "C");
        this.sectionsToReplace.put("Ñ", "N");
        this.sectionsToReplace.put("Ÿ", "Y");
        this.sectionsToReplace.put("Ý", "Y");
        this.sectionsToReplace.put("Ţ", "Y");
    }

    public void cleanup() {
        this.contactsBook.clear();
        this.contactsBookSPhones.clear();
        this.phoneBookContacts.clear();
        this.contacts.clear();
        this.contactsDict.clear();
        this.usersSectionsDict.clear();
        this.usersMutualSectionsDict.clear();
        this.sortedUsersSectionsArray.clear();
        this.sortedUsersMutualSectionsArray.clear();
        this.delayedContactsUpdate.clear();
        this.contactsByPhone.clear();
        this.loadingContacts = false;
        this.contactsSyncInProgress = false;
        this.contactsLoaded = false;
        this.contactsBookLoaded = false;
        this.lastContactsVersions = "";
        this.loadingDeleteInfo = 0;
        this.deleteAccountTTL = 0;
        this.loadingLastSeenInfo = 0;
        this.privacyRules = null;
    }

    public void checkInviteText() {
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0);
        this.inviteText = preferences.getString("invitetext", null);
        int time = preferences.getInt("invitetexttime", 0);
        if (!this.updatingInviteText) {
            if (this.inviteText == null || 86400 + time < ((int) (System.currentTimeMillis() / 1000))) {
                this.updatingInviteText = true;
                TL_help_getInviteText req = new TL_help_getInviteText();
                req.lang_code = LocaleController.getLocaleString(LocaleController.getInstance().getSystemDefaultLocale());
                if (req.lang_code.length() == 0) {
                    req.lang_code = "en";
                }
                ConnectionsManager.getInstance().sendRequest(req, new C14451(), 2);
            }
        }
    }

    public String getInviteText() {
        return this.inviteText != null ? this.inviteText : LocaleController.getString("InviteText", C0553R.string.InviteText);
    }

    public void checkAppAccount() {
        Account[] accounts;
        int a;
        AccountManager am = AccountManager.get(ApplicationLoader.applicationContext);
        try {
            accounts = am.getAccountsByType("org.telegram.account");
            if (accounts != null && accounts.length > 0) {
                for (Account removeAccount : accounts) {
                    am.removeAccount(removeAccount, null, null);
                }
            }
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
        }
        accounts = am.getAccountsByType(BuildConfig.APPLICATION_ID);
        boolean recreateAccount = false;
        if (UserConfig.isClientActivated()) {
            if (accounts.length == 1) {
                Account acc = accounts[0];
                if (acc.name.equals("" + UserConfig.getClientUserId())) {
                    this.currentAccount = acc;
                } else {
                    recreateAccount = true;
                }
            } else {
                recreateAccount = true;
            }
            readContacts();
        } else if (accounts.length > 0) {
            recreateAccount = true;
        }
        if (recreateAccount) {
            a = 0;
            while (a < accounts.length) {
                try {
                    am.removeAccount(accounts[a], null, null);
                    a++;
                } catch (Throwable e2) {
                    FileLog.m611e("tmessages", e2);
                }
            }
            if (UserConfig.isClientActivated()) {
                try {
                    this.currentAccount = new Account("" + UserConfig.getClientUserId(), BuildConfig.APPLICATION_ID);
                    am.addAccountExplicitly(this.currentAccount, "", null);
                } catch (Throwable e22) {
                    FileLog.m611e("tmessages", e22);
                }
            }
        }
    }

    public void deleteAllAppAccounts() {
        try {
            AccountManager am = AccountManager.get(ApplicationLoader.applicationContext);
            Account[] accounts = am.getAccountsByType(BuildConfig.APPLICATION_ID);
            for (Account removeAccount : accounts) {
                am.removeAccount(removeAccount, null, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void checkContacts() {
        Utilities.globalQueue.postRunnable(new C03192());
    }

    private boolean checkContactsInternal() {
        boolean reload = false;
        try {
            if (!hasContactsPermission()) {
                return false;
            }
            ContentResolver cr = ApplicationLoader.applicationContext.getContentResolver();
            Cursor pCur = null;
            try {
                pCur = cr.query(RawContacts.CONTENT_URI, new String[]{"version"}, null, null, null);
                if (pCur != null) {
                    StringBuilder currentVersion = new StringBuilder();
                    while (pCur.moveToNext()) {
                        currentVersion.append(pCur.getString(pCur.getColumnIndex("version")));
                    }
                    String newContactsVersion = currentVersion.toString();
                    if (!(this.lastContactsVersions.length() == 0 || this.lastContactsVersions.equals(newContactsVersion))) {
                        reload = true;
                    }
                    this.lastContactsVersions = newContactsVersion;
                }
                if (pCur != null) {
                    pCur.close();
                }
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
                if (pCur != null) {
                    pCur.close();
                }
            } catch (Throwable th) {
                if (pCur != null) {
                    pCur.close();
                }
            }
            return reload;
        } catch (Throwable e2) {
            FileLog.m611e("tmessages", e2);
        }
    }

    public void readContacts() {
        synchronized (loadContactsSync) {
            if (this.loadingContacts) {
                return;
            }
            this.loadingContacts = true;
            Utilities.stageQueue.postRunnable(new C03203());
        }
    }

    private HashMap<Integer, Contact> readContactsFromPhoneBook() {
        HashMap<Integer, Contact> contactsMap = new HashMap();
        try {
            if (hasContactsPermission()) {
                Contact contact;
                ContentResolver cr = ApplicationLoader.applicationContext.getContentResolver();
                HashMap<String, Contact> shortContacts = new HashMap();
                ArrayList<Integer> idsArr = new ArrayList();
                Cursor pCur = cr.query(Phone.CONTENT_URI, this.projectionPhones, null, null, null);
                if (pCur != null) {
                    if (pCur.getCount() > 0) {
                        while (pCur.moveToNext()) {
                            String number = pCur.getString(1);
                            if (!(number == null || number.length() == 0)) {
                                number = PhoneFormat.stripExceptNumbers(number, true);
                                if (number.length() != 0) {
                                    String shortNumber = number;
                                    if (number.startsWith("+")) {
                                        shortNumber = number.substring(1);
                                    }
                                    if (shortContacts.containsKey(shortNumber)) {
                                        continue;
                                    } else {
                                        Integer id = Integer.valueOf(pCur.getInt(0));
                                        if (!idsArr.contains(id)) {
                                            idsArr.add(id);
                                        }
                                        int type = pCur.getInt(2);
                                        contact = (Contact) contactsMap.get(id);
                                        if (contact == null) {
                                            contact = new Contact();
                                            contact.first_name = "";
                                            contact.last_name = "";
                                            contact.id = id.intValue();
                                            contactsMap.put(id, contact);
                                        }
                                        contact.shortPhones.add(shortNumber);
                                        contact.phones.add(number);
                                        contact.phoneDeleted.add(Integer.valueOf(0));
                                        if (type == 0) {
                                            contact.phoneTypes.add(pCur.getString(3));
                                        } else if (type == 1) {
                                            contact.phoneTypes.add(LocaleController.getString("PhoneHome", C0553R.string.PhoneHome));
                                        } else if (type == 2) {
                                            contact.phoneTypes.add(LocaleController.getString("PhoneMobile", C0553R.string.PhoneMobile));
                                        } else if (type == 3) {
                                            contact.phoneTypes.add(LocaleController.getString("PhoneWork", C0553R.string.PhoneWork));
                                        } else if (type == 12) {
                                            contact.phoneTypes.add(LocaleController.getString("PhoneMain", C0553R.string.PhoneMain));
                                        } else {
                                            contact.phoneTypes.add(LocaleController.getString("PhoneOther", C0553R.string.PhoneOther));
                                        }
                                        shortContacts.put(shortNumber, contact);
                                    }
                                } else {
                                    continue;
                                }
                            }
                        }
                    }
                    pCur.close();
                }
                pCur = cr.query(Data.CONTENT_URI, this.projectionNames, "contact_id IN (" + TextUtils.join(",", idsArr) + ") AND " + "mimetype" + " = '" + "vnd.android.cursor.item/name" + "'", null, null);
                if (pCur != null && pCur.getCount() > 0) {
                    while (pCur.moveToNext()) {
                        int id2 = pCur.getInt(0);
                        String fname = pCur.getString(1);
                        String sname = pCur.getString(2);
                        String sname2 = pCur.getString(3);
                        String mname = pCur.getString(4);
                        contact = (Contact) contactsMap.get(Integer.valueOf(id2));
                        if (contact != null && contact.first_name.length() == 0 && contact.last_name.length() == 0) {
                            contact.first_name = fname;
                            contact.last_name = sname;
                            if (contact.first_name == null) {
                                contact.first_name = "";
                            }
                            if (!(mname == null || mname.length() == 0)) {
                                if (contact.first_name.length() != 0) {
                                    contact.first_name += " " + mname;
                                } else {
                                    contact.first_name = mname;
                                }
                            }
                            if (contact.last_name == null) {
                                contact.last_name = "";
                            }
                            if (contact.last_name.length() == 0 && contact.first_name.length() == 0 && sname2 != null && sname2.length() != 0) {
                                contact.first_name = sname2;
                            }
                        }
                    }
                    pCur.close();
                }
                try {
                    pCur = cr.query(RawContacts.CONTENT_URI, new String[]{"display_name", "sync1", "contact_id"}, "account_type = 'com.whatsapp'", null, null);
                    if (pCur != null) {
                        while (pCur.moveToNext()) {
                            String phone = pCur.getString(1);
                            if (!(phone == null || phone.length() == 0)) {
                                boolean withPlus = phone.startsWith("+");
                                phone = Utilities.parseIntToString(phone);
                                if (!(phone == null || phone.length() == 0)) {
                                    String shortPhone = phone;
                                    if (!withPlus) {
                                        phone = "+" + phone;
                                    }
                                    if (!shortContacts.containsKey(shortPhone)) {
                                        String name = pCur.getString(0);
                                        if (name == null || name.length() == 0) {
                                            name = PhoneFormat.getInstance().format(phone);
                                        }
                                        contact = new Contact();
                                        contact.first_name = name;
                                        contact.last_name = "";
                                        contact.id = pCur.getInt(2);
                                        contactsMap.put(Integer.valueOf(contact.id), contact);
                                        contact.phoneDeleted.add(Integer.valueOf(0));
                                        contact.shortPhones.add(shortPhone);
                                        contact.phones.add(phone);
                                        contact.phoneTypes.add(LocaleController.getString("PhoneMobile", C0553R.string.PhoneMobile));
                                        shortContacts.put(shortPhone, contact);
                                    }
                                }
                            }
                        }
                        pCur.close();
                    }
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
        } catch (Throwable e2) {
            FileLog.m611e("tmessages", e2);
            contactsMap.clear();
        }
        return contactsMap;
    }

    public HashMap<Integer, Contact> getContactsCopy(HashMap<Integer, Contact> original) {
        HashMap<Integer, Contact> ret = new HashMap();
        for (Entry<Integer, Contact> entry : original.entrySet()) {
            Contact copyContact = new Contact();
            Contact originalContact = (Contact) entry.getValue();
            copyContact.phoneDeleted.addAll(originalContact.phoneDeleted);
            copyContact.phones.addAll(originalContact.phones);
            copyContact.phoneTypes.addAll(originalContact.phoneTypes);
            copyContact.shortPhones.addAll(originalContact.shortPhones);
            copyContact.first_name = originalContact.first_name;
            copyContact.last_name = originalContact.last_name;
            copyContact.id = originalContact.id;
            ret.put(Integer.valueOf(copyContact.id), copyContact);
        }
        return ret;
    }

    public void performSyncPhoneBook(HashMap<Integer, Contact> contactHashMap, boolean requ, boolean first, boolean schedule) {
        if (first || this.contactsBookLoaded) {
            final boolean z = requ;
            final boolean z2 = first;
            final HashMap<Integer, Contact> hashMap = contactHashMap;
            final boolean z3 = schedule;
            Utilities.globalQueue.postRunnable(new Runnable() {

                class C03211 implements Runnable {
                    C03211() {
                    }

                    public void run() {
                        ArrayList<User> toDelete = new ArrayList();
                        if (!(hashMap == null || hashMap.isEmpty())) {
                            try {
                                User user;
                                HashMap<String, User> contactsPhonesShort = new HashMap();
                                Iterator i$ = ContactsController.this.contacts.iterator();
                                while (i$.hasNext()) {
                                    user = MessagesController.getInstance().getUser(Integer.valueOf(((TL_contact) i$.next()).user_id));
                                    if (!(user == null || user.phone == null || user.phone.length() == 0)) {
                                        contactsPhonesShort.put(user.phone, user);
                                    }
                                }
                                int removed = 0;
                                for (Entry<Integer, Contact> entry : hashMap.entrySet()) {
                                    Contact contact = (Contact) entry.getValue();
                                    boolean was = false;
                                    int a = 0;
                                    while (a < contact.shortPhones.size()) {
                                        user = (User) contactsPhonesShort.get((String) contact.shortPhones.get(a));
                                        if (user != null) {
                                            was = true;
                                            toDelete.add(user);
                                            contact.shortPhones.remove(a);
                                            a--;
                                        }
                                        a++;
                                    }
                                    if (!was || contact.shortPhones.size() == 0) {
                                        removed++;
                                    }
                                }
                            } catch (Throwable e) {
                                FileLog.m611e("tmessages", e);
                            }
                        }
                        if (!toDelete.isEmpty()) {
                            ContactsController.this.deleteContact(toDelete);
                        }
                    }
                }

                class C03244 implements Runnable {
                    C03244() {
                    }

                    public void run() {
                        ContactsController.this.updateUnregisteredContacts(ContactsController.this.contacts);
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.contactsDidLoaded, new Object[0]);
                    }
                }

                public void run() {
                    int a;
                    boolean request = z;
                    if (request && z2 && !((UserConfig.importHash == null || UserConfig.importHash.length() == 0) && UserConfig.contactsVersion == 1)) {
                        UserConfig.importHash = "";
                        UserConfig.contactsVersion = 1;
                        UserConfig.saveConfig(false);
                        request = false;
                    }
                    HashMap<String, Contact> contactShortHashMap = new HashMap();
                    for (Entry<Integer, Contact> entry : hashMap.entrySet()) {
                        Contact c = (Contact) entry.getValue();
                        Iterator i$ = c.shortPhones.iterator();
                        while (i$.hasNext()) {
                            contactShortHashMap.put((String) i$.next(), c);
                        }
                    }
                    FileLog.m609e("tmessages", "start read contacts from phone");
                    if (!z3) {
                        ContactsController.this.checkContactsInternal();
                    }
                    final HashMap<Integer, Contact> contactsMap = ContactsController.this.readContactsFromPhoneBook();
                    final HashMap<String, Contact> contactsBookShort = new HashMap();
                    int oldCount = hashMap.size();
                    ArrayList<TL_inputPhoneContact> toImport = new ArrayList();
                    Contact value;
                    TL_inputPhoneContact imp;
                    TL_contact contact;
                    User user;
                    if (!hashMap.isEmpty()) {
                        for (Entry<Integer, Contact> pair : contactsMap.entrySet()) {
                            Integer id = (Integer) pair.getKey();
                            value = (Contact) pair.getValue();
                            Contact existing = (Contact) hashMap.get(id);
                            if (existing == null) {
                                i$ = value.shortPhones.iterator();
                                while (i$.hasNext()) {
                                    c = (Contact) contactShortHashMap.get((String) i$.next());
                                    if (c != null) {
                                        existing = c;
                                        id = Integer.valueOf(existing.id);
                                        break;
                                    }
                                }
                            }
                            boolean nameChanged = (existing == null || ((value.first_name == null || value.first_name.length() == 0 || existing.first_name.equals(value.first_name)) && (value.last_name == null || existing.last_name == null || existing.last_name.equals(value.last_name)))) ? false : true;
                            String sphone;
                            int index;
                            if (existing == null || nameChanged) {
                                for (a = 0; a < value.phones.size(); a++) {
                                    sphone = (String) value.shortPhones.get(a);
                                    contactsBookShort.put(sphone, value);
                                    if (existing != null) {
                                        index = existing.shortPhones.indexOf(sphone);
                                        if (index != -1) {
                                            Integer deleted = (Integer) existing.phoneDeleted.get(index);
                                            value.phoneDeleted.set(a, deleted);
                                            if (deleted.intValue() == 1) {
                                            }
                                        }
                                    }
                                    if (request && (nameChanged || !ContactsController.this.contactsByPhone.containsKey(sphone))) {
                                        imp = new TL_inputPhoneContact();
                                        imp.client_id = (long) value.id;
                                        imp.first_name = value.first_name;
                                        imp.last_name = value.last_name;
                                        imp.phone = (String) value.phones.get(a);
                                        toImport.add(imp);
                                    }
                                }
                                if (existing != null) {
                                    hashMap.remove(id);
                                }
                            } else {
                                for (a = 0; a < value.phones.size(); a++) {
                                    sphone = (String) value.shortPhones.get(a);
                                    contactsBookShort.put(sphone, value);
                                    index = existing.shortPhones.indexOf(sphone);
                                    if (index != -1) {
                                        value.phoneDeleted.set(a, existing.phoneDeleted.get(index));
                                        existing.phones.remove(index);
                                        existing.shortPhones.remove(index);
                                        existing.phoneDeleted.remove(index);
                                        existing.phoneTypes.remove(index);
                                    } else if (request) {
                                        contact = (TL_contact) ContactsController.this.contactsByPhone.get(sphone);
                                        if (contact != null) {
                                            user = MessagesController.getInstance().getUser(Integer.valueOf(contact.user_id));
                                            if (user != null) {
                                                if (user.first_name != null) {
                                                    if (user.first_name.length() != 0) {
                                                    }
                                                }
                                                if (!(user.last_name == null || user.last_name.length() == 0)) {
                                                }
                                            }
                                        }
                                        imp = new TL_inputPhoneContact();
                                        imp.client_id = (long) value.id;
                                        imp.first_name = value.first_name;
                                        imp.last_name = value.last_name;
                                        imp.phone = (String) value.phones.get(a);
                                        toImport.add(imp);
                                    }
                                }
                                if (existing.phones.isEmpty()) {
                                    hashMap.remove(id);
                                }
                            }
                        }
                        if (!z2 && hashMap.isEmpty() && toImport.isEmpty() && oldCount == contactsMap.size()) {
                            FileLog.m609e("tmessages", "contacts not changed!");
                            return;
                        } else if (!(!request || hashMap.isEmpty() || contactsMap.isEmpty())) {
                            if (toImport.isEmpty()) {
                                MessagesStorage.getInstance().putCachedPhoneBook(contactsMap);
                            }
                            if (!(true || hashMap.isEmpty())) {
                                AndroidUtilities.runOnUIThread(new C03211());
                            }
                        }
                    } else if (request) {
                        for (Entry<Integer, Contact> pair2 : contactsMap.entrySet()) {
                            value = (Contact) pair2.getValue();
                            int id2 = ((Integer) pair2.getKey()).intValue();
                            for (a = 0; a < value.phones.size(); a++) {
                                contact = (TL_contact) ContactsController.this.contactsByPhone.get((String) value.shortPhones.get(a));
                                if (contact != null) {
                                    user = MessagesController.getInstance().getUser(Integer.valueOf(contact.user_id));
                                    if (user != null) {
                                        if (user.first_name != null) {
                                            if (user.first_name.length() != 0) {
                                            }
                                        }
                                        if (!(user.last_name == null || user.last_name.length() == 0)) {
                                        }
                                    }
                                }
                                imp = new TL_inputPhoneContact();
                                imp.client_id = (long) id2;
                                imp.first_name = value.first_name;
                                imp.last_name = value.last_name;
                                imp.phone = (String) value.phones.get(a);
                                toImport.add(imp);
                            }
                        }
                    }
                    FileLog.m609e("tmessages", "done processing contacts");
                    if (!request) {
                        Utilities.stageQueue.postRunnable(new Runnable() {
                            public void run() {
                                ContactsController.this.contactsBookSPhones = contactsBookShort;
                                ContactsController.this.contactsBook = contactsMap;
                                ContactsController.this.contactsSyncInProgress = false;
                                ContactsController.this.contactsBookLoaded = true;
                                if (z2) {
                                    ContactsController.this.contactsLoaded = true;
                                }
                                if (!ContactsController.this.delayedContactsUpdate.isEmpty() && ContactsController.this.contactsLoaded && ContactsController.this.contactsBookLoaded) {
                                    ContactsController.this.applyContactsUpdates(ContactsController.this.delayedContactsUpdate, null, null, null);
                                    ContactsController.this.delayedContactsUpdate.clear();
                                }
                            }
                        });
                        if (!contactsMap.isEmpty()) {
                            MessagesStorage.getInstance().putCachedPhoneBook(contactsMap);
                        }
                    } else if (toImport.isEmpty()) {
                        Utilities.stageQueue.postRunnable(new Runnable() {
                            public void run() {
                                ContactsController.this.contactsBookSPhones = contactsBookShort;
                                ContactsController.this.contactsBook = contactsMap;
                                ContactsController.this.contactsSyncInProgress = false;
                                ContactsController.this.contactsBookLoaded = true;
                                if (z2) {
                                    ContactsController.this.contactsLoaded = true;
                                }
                                if (!ContactsController.this.delayedContactsUpdate.isEmpty() && ContactsController.this.contactsLoaded) {
                                    ContactsController.this.applyContactsUpdates(ContactsController.this.delayedContactsUpdate, null, null, null);
                                    ContactsController.this.delayedContactsUpdate.clear();
                                }
                            }
                        });
                        AndroidUtilities.runOnUIThread(new C03244());
                    } else {
                        int count = (int) Math.ceil((double) (((float) toImport.size()) / 500.0f));
                        a = 0;
                        while (a < count) {
                            ArrayList<TL_inputPhoneContact> finalToImport = new ArrayList();
                            finalToImport.addAll(toImport.subList(a * 500, Math.min((a + 1) * 500, toImport.size())));
                            TLObject req = new TL_contacts_importContacts();
                            req.contacts = finalToImport;
                            req.replace = false;
                            final boolean z = a == count + -1;
                            ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {

                                class C03221 implements Runnable {
                                    C03221() {
                                    }

                                    public void run() {
                                        ContactsController.this.contactsBookSPhones = contactsBookShort;
                                        ContactsController.this.contactsBook = contactsMap;
                                        ContactsController.this.contactsSyncInProgress = false;
                                        ContactsController.this.contactsBookLoaded = true;
                                        if (z2) {
                                            ContactsController.this.contactsLoaded = true;
                                        }
                                        if (!ContactsController.this.delayedContactsUpdate.isEmpty() && ContactsController.this.contactsLoaded) {
                                            ContactsController.this.applyContactsUpdates(ContactsController.this.delayedContactsUpdate, null, null, null);
                                            ContactsController.this.delayedContactsUpdate.clear();
                                        }
                                    }
                                }

                                public void run(TLObject response, TL_error error) {
                                    if (error == null) {
                                        FileLog.m609e("tmessages", "contacts imported");
                                        if (z && !contactsMap.isEmpty()) {
                                            MessagesStorage.getInstance().putCachedPhoneBook(contactsMap);
                                        }
                                        TL_contacts_importedContacts res = (TL_contacts_importedContacts) response;
                                        MessagesStorage.getInstance().putUsersAndChats(res.users, null, true, true);
                                        ArrayList<TL_contact> cArr = new ArrayList();
                                        Iterator i$ = res.imported.iterator();
                                        while (i$.hasNext()) {
                                            TL_importedContact c = (TL_importedContact) i$.next();
                                            TL_contact contact = new TL_contact();
                                            contact.user_id = c.user_id;
                                            cArr.add(contact);
                                        }
                                        ContactsController.this.processLoadedContacts(cArr, res.users, 2);
                                    } else {
                                        FileLog.m609e("tmessages", "import contacts error " + error.text);
                                    }
                                    if (z) {
                                        Utilities.stageQueue.postRunnable(new C03221());
                                    }
                                }
                            }, 6);
                            a++;
                        }
                    }
                }
            });
        }
    }

    public boolean isLoadingContacts() {
        boolean z;
        synchronized (loadContactsSync) {
            z = this.loadingContacts;
        }
        return z;
    }

    public void loadContacts(boolean fromCache, boolean cacheEmpty) {
        synchronized (loadContactsSync) {
            this.loadingContacts = true;
        }
        if (fromCache) {
            FileLog.m609e("tmessages", "load contacts from cache");
            MessagesStorage.getInstance().getContacts();
            return;
        }
        FileLog.m609e("tmessages", "load contacts from server");
        TL_contacts_getContacts req = new TL_contacts_getContacts();
        req.hash = cacheEmpty ? "" : UserConfig.contactsHash;
        ConnectionsManager.getInstance().sendRequest(req, new C14475());
    }

    public void processLoadedContacts(final ArrayList<TL_contact> contactsArr, final ArrayList<User> usersArr, final int from) {
        AndroidUtilities.runOnUIThread(new Runnable() {
            public void run() {
                boolean z = true;
                MessagesController instance = MessagesController.getInstance();
                ArrayList arrayList = usersArr;
                if (from != 1) {
                    z = false;
                }
                instance.putUsers(arrayList, z);
                final HashMap<Integer, User> usersDict = new HashMap();
                final boolean isEmpty = contactsArr.isEmpty();
                if (!ContactsController.this.contacts.isEmpty()) {
                    int a = 0;
                    while (a < contactsArr.size()) {
                        if (ContactsController.this.contactsDict.get(((TL_contact) contactsArr.get(a)).user_id) != null) {
                            contactsArr.remove(a);
                            a--;
                        }
                        a++;
                    }
                    contactsArr.addAll(ContactsController.this.contacts);
                }
                Iterator i$ = contactsArr.iterator();
                while (i$.hasNext()) {
                    User user = MessagesController.getInstance().getUser(Integer.valueOf(((TL_contact) i$.next()).user_id));
                    if (user != null) {
                        usersDict.put(Integer.valueOf(user.id), user);
                    }
                }
                Utilities.stageQueue.postRunnable(new Runnable() {

                    class C03281 implements Comparator<TL_contact> {
                        C03281() {
                        }

                        public int compare(TL_contact tl_contact, TL_contact tl_contact2) {
                            if (tl_contact.user_id > tl_contact2.user_id) {
                                return 1;
                            }
                            if (tl_contact.user_id < tl_contact2.user_id) {
                                return -1;
                            }
                            return 0;
                        }
                    }

                    class C03292 implements Comparator<TL_contact> {
                        C03292() {
                        }

                        public int compare(TL_contact tl_contact, TL_contact tl_contact2) {
                            return UserObject.getFirstName((User) usersDict.get(Integer.valueOf(tl_contact.user_id))).compareTo(UserObject.getFirstName((User) usersDict.get(Integer.valueOf(tl_contact2.user_id))));
                        }
                    }

                    class C03303 implements Comparator<String> {
                        C03303() {
                        }

                        public int compare(String s, String s2) {
                            char cv1 = s.charAt(0);
                            char cv2 = s2.charAt(0);
                            if (cv1 == '#') {
                                return 1;
                            }
                            if (cv2 == '#') {
                                return -1;
                            }
                            return s.compareTo(s2);
                        }
                    }

                    class C03314 implements Comparator<String> {
                        C03314() {
                        }

                        public int compare(String s, String s2) {
                            char cv1 = s.charAt(0);
                            char cv2 = s2.charAt(0);
                            if (cv1 == '#') {
                                return 1;
                            }
                            if (cv2 == '#') {
                                return -1;
                            }
                            return s.compareTo(s2);
                        }
                    }

                    public void run() {
                        FileLog.m609e("tmessages", "done loading contacts");
                        if (from != 1 || (!contactsArr.isEmpty() && UserConfig.lastContactsSyncTime >= ((int) (System.currentTimeMillis() / 1000)) - 86400)) {
                            if (from == 0) {
                                UserConfig.lastContactsSyncTime = (int) (System.currentTimeMillis() / 1000);
                                UserConfig.saveConfig(false);
                            }
                            Iterator i$ = contactsArr.iterator();
                            while (i$.hasNext()) {
                                TL_contact contact = (TL_contact) i$.next();
                                if (usersDict.get(Integer.valueOf(contact.user_id)) == null && contact.user_id != UserConfig.getClientUserId()) {
                                    ContactsController.this.loadContacts(false, true);
                                    FileLog.m609e("tmessages", "contacts are broken, load from server");
                                    return;
                                }
                            }
                            if (from != 1) {
                                MessagesStorage.getInstance().putUsersAndChats(usersArr, null, true, true);
                                MessagesStorage.getInstance().putContacts(contactsArr, from != 2);
                                Collections.sort(contactsArr, new C03281());
                                StringBuilder ids = new StringBuilder();
                                i$ = contactsArr.iterator();
                                while (i$.hasNext()) {
                                    TL_contact aContactsArr = (TL_contact) i$.next();
                                    if (ids.length() != 0) {
                                        ids.append(",");
                                    }
                                    ids.append(aContactsArr.user_id);
                                }
                                UserConfig.contactsHash = Utilities.MD5(ids.toString());
                                UserConfig.saveConfig(false);
                            }
                            Collections.sort(contactsArr, new C03292());
                            final SparseArray<TL_contact> contactsDictionary = new SparseArray();
                            final HashMap<String, ArrayList<TL_contact>> sectionsDict = new HashMap();
                            final HashMap<String, ArrayList<TL_contact>> sectionsDictMutual = new HashMap();
                            final ArrayList<String> sortedSectionsArray = new ArrayList();
                            final ArrayList<String> sortedSectionsArrayMutual = new ArrayList();
                            HashMap<String, TL_contact> contactsByPhonesDict = null;
                            if (!ContactsController.this.contactsBookLoaded) {
                                contactsByPhonesDict = new HashMap();
                            }
                            final HashMap<String, TL_contact> contactsByPhonesDictFinal = contactsByPhonesDict;
                            i$ = contactsArr.iterator();
                            while (i$.hasNext()) {
                                TL_contact value = (TL_contact) i$.next();
                                User user = (User) usersDict.get(Integer.valueOf(value.user_id));
                                if (user != null) {
                                    contactsDictionary.put(value.user_id, value);
                                    if (contactsByPhonesDict != null) {
                                        contactsByPhonesDict.put(user.phone, value);
                                    }
                                    String key = UserObject.getFirstName(user);
                                    if (key.length() > 1) {
                                        key = key.substring(0, 1);
                                    }
                                    if (key.length() == 0) {
                                        key = "#";
                                    } else {
                                        key = key.toUpperCase();
                                    }
                                    String replace = (String) ContactsController.this.sectionsToReplace.get(key);
                                    if (replace != null) {
                                        key = replace;
                                    }
                                    ArrayList<TL_contact> arr = (ArrayList) sectionsDict.get(key);
                                    if (arr == null) {
                                        arr = new ArrayList();
                                        sectionsDict.put(key, arr);
                                        sortedSectionsArray.add(key);
                                    }
                                    arr.add(value);
                                    if (user.mutual_contact) {
                                        arr = (ArrayList) sectionsDictMutual.get(key);
                                        if (arr == null) {
                                            arr = new ArrayList();
                                            sectionsDictMutual.put(key, arr);
                                            sortedSectionsArrayMutual.add(key);
                                        }
                                        arr.add(value);
                                    }
                                }
                            }
                            Collections.sort(sortedSectionsArray, new C03303());
                            Collections.sort(sortedSectionsArrayMutual, new C03314());
                            AndroidUtilities.runOnUIThread(new Runnable() {
                                public void run() {
                                    ContactsController.this.contacts = contactsArr;
                                    ContactsController.this.contactsDict = contactsDictionary;
                                    ContactsController.this.usersSectionsDict = sectionsDict;
                                    ContactsController.this.usersMutualSectionsDict = sectionsDictMutual;
                                    ContactsController.this.sortedUsersSectionsArray = sortedSectionsArray;
                                    ContactsController.this.sortedUsersMutualSectionsArray = sortedSectionsArrayMutual;
                                    if (from != 2) {
                                        synchronized (ContactsController.loadContactsSync) {
                                            ContactsController.this.loadingContacts = false;
                                        }
                                    }
                                    ContactsController.this.performWriteContactsToPhoneBook();
                                    ContactsController.this.updateUnregisteredContacts(contactsArr);
                                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.contactsDidLoaded, new Object[0]);
                                    if (from == 1 || isEmpty) {
                                        ContactsController.this.reloadContactsStatusesMaybe();
                                    } else {
                                        ContactsController.this.saveContactsLoadTime();
                                    }
                                }
                            });
                            if (!ContactsController.this.delayedContactsUpdate.isEmpty() && ContactsController.this.contactsLoaded && ContactsController.this.contactsBookLoaded) {
                                ContactsController.this.applyContactsUpdates(ContactsController.this.delayedContactsUpdate, null, null, null);
                                ContactsController.this.delayedContactsUpdate.clear();
                            }
                            if (contactsByPhonesDictFinal != null) {
                                AndroidUtilities.runOnUIThread(new Runnable() {

                                    class C03331 implements Runnable {
                                        C03331() {
                                        }

                                        public void run() {
                                            ContactsController.this.contactsByPhone = contactsByPhonesDictFinal;
                                        }
                                    }

                                    public void run() {
                                        Utilities.globalQueue.postRunnable(new C03331());
                                        if (!ContactsController.this.contactsSyncInProgress) {
                                            ContactsController.this.contactsSyncInProgress = true;
                                            MessagesStorage.getInstance().getCachedPhoneBook();
                                        }
                                    }
                                });
                                return;
                            } else {
                                ContactsController.this.contactsLoaded = true;
                                return;
                            }
                        }
                        ContactsController.this.loadContacts(false, true);
                    }
                });
            }
        });
    }

    private void reloadContactsStatusesMaybe() {
        try {
            if (ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0).getLong("lastReloadStatusTime", 0) < System.currentTimeMillis() - 86400000) {
                reloadContactsStatuses();
            }
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
        }
    }

    private void saveContactsLoadTime() {
        try {
            ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0).edit().putLong("lastReloadStatusTime", System.currentTimeMillis()).commit();
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
        }
    }

    private void updateUnregisteredContacts(ArrayList<TL_contact> contactsArr) {
        HashMap<String, TL_contact> contactsPhonesShort = new HashMap();
        Iterator i$ = contactsArr.iterator();
        while (i$.hasNext()) {
            TL_contact value = (TL_contact) i$.next();
            User user = MessagesController.getInstance().getUser(Integer.valueOf(value.user_id));
            if (!(user == null || user.phone == null || user.phone.length() == 0)) {
                contactsPhonesShort.put(user.phone, value);
            }
        }
        ArrayList<Contact> sortedPhoneBookContacts = new ArrayList();
        for (Entry<Integer, Contact> pair : this.contactsBook.entrySet()) {
            Contact value2 = (Contact) pair.getValue();
            int id = ((Integer) pair.getKey()).intValue();
            boolean skip = false;
            int a = 0;
            while (a < value2.phones.size()) {
                if (contactsPhonesShort.containsKey((String) value2.shortPhones.get(a)) || ((Integer) value2.phoneDeleted.get(a)).intValue() == 1) {
                    skip = true;
                    break;
                }
                a++;
            }
            if (!skip) {
                sortedPhoneBookContacts.add(value2);
            }
        }
        Collections.sort(sortedPhoneBookContacts, new C03377());
        this.phoneBookContacts = sortedPhoneBookContacts;
    }

    private void buildContactsSectionsArrays(boolean sort) {
        if (sort) {
            Collections.sort(this.contacts, new C03388());
        }
        StringBuilder ids = new StringBuilder();
        HashMap<String, ArrayList<TL_contact>> sectionsDict = new HashMap();
        ArrayList<String> sortedSectionsArray = new ArrayList();
        Iterator i$ = this.contacts.iterator();
        while (i$.hasNext()) {
            TL_contact value = (TL_contact) i$.next();
            User user = MessagesController.getInstance().getUser(Integer.valueOf(value.user_id));
            if (user != null) {
                String key = UserObject.getFirstName(user);
                if (key.length() > 1) {
                    key = key.substring(0, 1);
                }
                if (key.length() == 0) {
                    key = "#";
                } else {
                    key = key.toUpperCase();
                }
                String replace = (String) this.sectionsToReplace.get(key);
                if (replace != null) {
                    key = replace;
                }
                ArrayList<TL_contact> arr = (ArrayList) sectionsDict.get(key);
                if (arr == null) {
                    arr = new ArrayList();
                    sectionsDict.put(key, arr);
                    sortedSectionsArray.add(key);
                }
                arr.add(value);
                if (ids.length() != 0) {
                    ids.append(",");
                }
                ids.append(value.user_id);
            }
        }
        UserConfig.contactsHash = Utilities.MD5(ids.toString());
        UserConfig.saveConfig(false);
        Collections.sort(sortedSectionsArray, new C03399());
        this.usersSectionsDict = sectionsDict;
        this.sortedUsersSectionsArray = sortedSectionsArray;
    }

    private boolean hasContactsPermission() {
        if (VERSION.SDK_INT < 23) {
            Cursor cursor = null;
            try {
                cursor = ApplicationLoader.applicationContext.getContentResolver().query(Phone.CONTENT_URI, this.projectionPhones, null, null, null);
                if (cursor == null || cursor.getCount() == 0) {
                    if (cursor != null) {
                        try {
                            cursor.close();
                        } catch (Throwable e) {
                            FileLog.m611e("tmessages", e);
                        }
                    }
                    return false;
                }
                if (cursor != null) {
                    try {
                        cursor.close();
                    } catch (Throwable e2) {
                        FileLog.m611e("tmessages", e2);
                    }
                }
                return true;
            } catch (Throwable e22) {
                FileLog.m611e("tmessages", e22);
                if (cursor != null) {
                    try {
                        cursor.close();
                    } catch (Throwable e222) {
                        FileLog.m611e("tmessages", e222);
                    }
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    try {
                        cursor.close();
                    } catch (Throwable e2222) {
                        FileLog.m611e("tmessages", e2222);
                    }
                }
            }
        } else if (ApplicationLoader.applicationContext.checkSelfPermission("android.permission.READ_CONTACTS") == 0) {
            return true;
        } else {
            return false;
        }
    }

    private void performWriteContactsToPhoneBookInternal(ArrayList<TL_contact> contactsArray) {
        try {
            if (hasContactsPermission()) {
                Uri rawContactUri = RawContacts.CONTENT_URI.buildUpon().appendQueryParameter("account_name", this.currentAccount.name).appendQueryParameter("account_type", this.currentAccount.type).build();
                Cursor c1 = ApplicationLoader.applicationContext.getContentResolver().query(rawContactUri, new String[]{"_id", "sync2"}, null, null, null);
                HashMap<Integer, Long> bookContacts = new HashMap();
                if (c1 != null) {
                    while (c1.moveToNext()) {
                        bookContacts.put(Integer.valueOf(c1.getInt(1)), Long.valueOf(c1.getLong(0)));
                    }
                    c1.close();
                    for (int a = 0; a < contactsArray.size(); a++) {
                        TL_contact u = (TL_contact) contactsArray.get(a);
                        if (!bookContacts.containsKey(Integer.valueOf(u.user_id))) {
                            addContactToPhoneBook(MessagesController.getInstance().getUser(Integer.valueOf(u.user_id)), false);
                        }
                    }
                }
            }
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
        }
    }

    private void performWriteContactsToPhoneBook() {
        final ArrayList<TL_contact> contactsArray = new ArrayList();
        contactsArray.addAll(this.contacts);
        Utilities.phoneBookQueue.postRunnable(new Runnable() {
            public void run() {
                ContactsController.this.performWriteContactsToPhoneBookInternal(contactsArray);
            }
        });
    }

    private void applyContactsUpdates(ArrayList<Integer> ids, ConcurrentHashMap<Integer, User> userDict, ArrayList<TL_contact> newC, ArrayList<Integer> contactsTD) {
        Iterator i$;
        Integer uid;
        if (newC == null || contactsTD == null) {
            newC = new ArrayList();
            contactsTD = new ArrayList();
            i$ = ids.iterator();
            while (i$.hasNext()) {
                uid = (Integer) i$.next();
                if (uid.intValue() > 0) {
                    TL_contact contact = new TL_contact();
                    contact.user_id = uid.intValue();
                    newC.add(contact);
                } else if (uid.intValue() < 0) {
                    contactsTD.add(Integer.valueOf(-uid.intValue()));
                }
            }
        }
        FileLog.m609e("tmessages", "process update - contacts add = " + newC.size() + " delete = " + contactsTD.size());
        StringBuilder toAdd = new StringBuilder();
        StringBuilder toDelete = new StringBuilder();
        boolean reloadContacts = false;
        i$ = newC.iterator();
        while (i$.hasNext()) {
            Contact contact2;
            int index;
            TL_contact newContact = (TL_contact) i$.next();
            User user = null;
            if (userDict != null) {
                user = (User) userDict.get(Integer.valueOf(newContact.user_id));
            }
            if (user == null) {
                user = MessagesController.getInstance().getUser(Integer.valueOf(newContact.user_id));
            } else {
                MessagesController.getInstance().putUser(user, true);
            }
            if (user == null || user.phone == null || user.phone.length() == 0) {
                reloadContacts = true;
            } else {
                contact2 = (Contact) this.contactsBookSPhones.get(user.phone);
                if (contact2 != null) {
                    index = contact2.shortPhones.indexOf(user.phone);
                    if (index != -1) {
                        contact2.phoneDeleted.set(index, Integer.valueOf(0));
                    }
                }
                if (toAdd.length() != 0) {
                    toAdd.append(",");
                }
                toAdd.append(user.phone);
            }
        }
        i$ = contactsTD.iterator();
        while (i$.hasNext()) {
            uid = (Integer) i$.next();
            Utilities.phoneBookQueue.postRunnable(new Runnable() {
                public void run() {
                    ContactsController.this.deleteContactFromPhoneBook(uid.intValue());
                }
            });
            user = null;
            if (userDict != null) {
                user = (User) userDict.get(uid);
            }
            if (user == null) {
                user = MessagesController.getInstance().getUser(uid);
            } else {
                MessagesController.getInstance().putUser(user, true);
            }
            if (user == null) {
                reloadContacts = true;
            } else if (user.phone != null && user.phone.length() > 0) {
                contact2 = (Contact) this.contactsBookSPhones.get(user.phone);
                if (contact2 != null) {
                    index = contact2.shortPhones.indexOf(user.phone);
                    if (index != -1) {
                        contact2.phoneDeleted.set(index, Integer.valueOf(1));
                    }
                }
                if (toDelete.length() != 0) {
                    toDelete.append(",");
                }
                toDelete.append(user.phone);
            }
        }
        if (!(toAdd.length() == 0 && toDelete.length() == 0)) {
            MessagesStorage.getInstance().applyPhoneBookUpdates(toAdd.toString(), toDelete.toString());
        }
        if (reloadContacts) {
            Utilities.stageQueue.postRunnable(new Runnable() {
                public void run() {
                    ContactsController.this.loadContacts(false, true);
                }
            });
            return;
        }
        final ArrayList<TL_contact> newContacts = newC;
        final ArrayList<Integer> contactsToDelete = contactsTD;
        AndroidUtilities.runOnUIThread(new Runnable() {
            public void run() {
                boolean z;
                Iterator i$ = newContacts.iterator();
                while (i$.hasNext()) {
                    TL_contact contact = (TL_contact) i$.next();
                    if (ContactsController.this.contactsDict.get(contact.user_id) == null) {
                        ContactsController.this.contacts.add(contact);
                        ContactsController.this.contactsDict.put(contact.user_id, contact);
                    }
                }
                i$ = contactsToDelete.iterator();
                while (i$.hasNext()) {
                    Integer uid = (Integer) i$.next();
                    contact = (TL_contact) ContactsController.this.contactsDict.get(uid.intValue());
                    if (contact != null) {
                        ContactsController.this.contacts.remove(contact);
                        ContactsController.this.contactsDict.remove(uid.intValue());
                    }
                }
                if (!newContacts.isEmpty()) {
                    ContactsController.this.updateUnregisteredContacts(ContactsController.this.contacts);
                    ContactsController.this.performWriteContactsToPhoneBook();
                }
                ContactsController.this.performSyncPhoneBook(ContactsController.this.getContactsCopy(ContactsController.this.contactsBook), false, false, false);
                ContactsController contactsController = ContactsController.this;
                if (newContacts.isEmpty()) {
                    z = false;
                } else {
                    z = true;
                }
                contactsController.buildContactsSectionsArrays(z);
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.contactsDidLoaded, new Object[0]);
            }
        });
    }

    public void processContactsUpdates(ArrayList<Integer> ids, ConcurrentHashMap<Integer, User> userDict) {
        ArrayList<TL_contact> newContacts = new ArrayList();
        ArrayList<Integer> contactsToDelete = new ArrayList();
        Iterator i$ = ids.iterator();
        while (i$.hasNext()) {
            Integer uid = (Integer) i$.next();
            int idx;
            if (uid.intValue() > 0) {
                TL_contact contact = new TL_contact();
                contact.user_id = uid.intValue();
                newContacts.add(contact);
                if (!this.delayedContactsUpdate.isEmpty()) {
                    idx = this.delayedContactsUpdate.indexOf(Integer.valueOf(-uid.intValue()));
                    if (idx != -1) {
                        this.delayedContactsUpdate.remove(idx);
                    }
                }
            } else if (uid.intValue() < 0) {
                contactsToDelete.add(Integer.valueOf(-uid.intValue()));
                if (!this.delayedContactsUpdate.isEmpty()) {
                    idx = this.delayedContactsUpdate.indexOf(Integer.valueOf(-uid.intValue()));
                    if (idx != -1) {
                        this.delayedContactsUpdate.remove(idx);
                    }
                }
            }
        }
        if (!contactsToDelete.isEmpty()) {
            MessagesStorage.getInstance().deleteContacts(contactsToDelete);
        }
        if (!newContacts.isEmpty()) {
            MessagesStorage.getInstance().putContacts(newContacts, false);
        }
        if (this.contactsLoaded && this.contactsBookLoaded) {
            applyContactsUpdates(ids, userDict, newContacts, contactsToDelete);
            return;
        }
        this.delayedContactsUpdate.addAll(ids);
        FileLog.m609e("tmessages", "delay update - contacts add = " + newContacts.size() + " delete = " + contactsToDelete.size());
    }

    public long addContactToPhoneBook(User user, boolean check) {
        long j = -1;
        if (!(this.currentAccount == null || user == null || user.phone == null || user.phone.length() == 0 || !hasContactsPermission())) {
            j = -1;
            synchronized (this.observerLock) {
                this.ignoreChanges = true;
            }
            ContentResolver contentResolver = ApplicationLoader.applicationContext.getContentResolver();
            if (check) {
                try {
                    contentResolver.delete(RawContacts.CONTENT_URI.buildUpon().appendQueryParameter("caller_is_syncadapter", "true").appendQueryParameter("account_name", this.currentAccount.name).appendQueryParameter("account_type", this.currentAccount.type).build(), "sync2 = " + user.id, null);
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
            ArrayList<ContentProviderOperation> query = new ArrayList();
            Builder builder = ContentProviderOperation.newInsert(RawContacts.CONTENT_URI);
            builder.withValue("account_name", this.currentAccount.name);
            builder.withValue("account_type", this.currentAccount.type);
            builder.withValue("sync1", user.phone);
            builder.withValue("sync2", Integer.valueOf(user.id));
            query.add(builder.build());
            builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
            builder.withValueBackReference("raw_contact_id", 0);
            builder.withValue("mimetype", "vnd.android.cursor.item/name");
            builder.withValue("data2", user.first_name);
            builder.withValue("data3", user.last_name);
            query.add(builder.build());
            builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
            builder.withValueBackReference("raw_contact_id", 0);
            builder.withValue("mimetype", "vnd.android.cursor.item/vnd.org.telegram.messenger.android.profile");
            builder.withValue("data1", Integer.valueOf(user.id));
            builder.withValue("data2", "Telegram Profile");
            builder.withValue("data3", "+" + user.phone);
            builder.withValue("data4", Integer.valueOf(user.id));
            query.add(builder.build());
            try {
                ContentProviderResult[] result = contentResolver.applyBatch("com.android.contacts", query);
                if (!(result == null || result.length <= 0 || result[0].uri == null)) {
                    j = Long.parseLong(result[0].uri.getLastPathSegment());
                }
            } catch (Throwable e2) {
                FileLog.m611e("tmessages", e2);
            }
            synchronized (this.observerLock) {
                this.ignoreChanges = false;
            }
        }
        return j;
    }

    private void deleteContactFromPhoneBook(int uid) {
        if (hasContactsPermission()) {
            synchronized (this.observerLock) {
                this.ignoreChanges = true;
            }
            try {
                ApplicationLoader.applicationContext.getContentResolver().delete(RawContacts.CONTENT_URI.buildUpon().appendQueryParameter("caller_is_syncadapter", "true").appendQueryParameter("account_name", this.currentAccount.name).appendQueryParameter("account_type", this.currentAccount.type).build(), "sync2 = " + uid, null);
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
            synchronized (this.observerLock) {
                this.ignoreChanges = false;
            }
        }
    }

    protected void markAsContacted(final String contactId) {
        if (contactId != null) {
            Utilities.phoneBookQueue.postRunnable(new Runnable() {
                public void run() {
                    Uri uri = Uri.parse(contactId);
                    ContentValues values = new ContentValues();
                    values.put("last_time_contacted", Long.valueOf(System.currentTimeMillis()));
                    ApplicationLoader.applicationContext.getContentResolver().update(uri, values, null, null);
                }
            });
        }
    }

    public void addContact(User user) {
        if (user != null && user.phone != null) {
            TL_contacts_importContacts req = new TL_contacts_importContacts();
            ArrayList<TL_inputPhoneContact> contactsParams = new ArrayList();
            TL_inputPhoneContact c = new TL_inputPhoneContact();
            c.phone = user.phone;
            if (!c.phone.startsWith("+")) {
                c.phone = "+" + c.phone;
            }
            c.first_name = user.first_name;
            c.last_name = user.last_name;
            c.client_id = 0;
            contactsParams.add(c);
            req.contacts = contactsParams;
            req.replace = false;
            ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
                public void run(TLObject response, TL_error error) {
                    if (error == null) {
                        final TL_contacts_importedContacts res = (TL_contacts_importedContacts) response;
                        MessagesStorage.getInstance().putUsersAndChats(res.users, null, true, true);
                        Iterator i$ = res.users.iterator();
                        while (i$.hasNext()) {
                            final User u = (User) i$.next();
                            Utilities.phoneBookQueue.postRunnable(new Runnable() {
                                public void run() {
                                    ContactsController.this.addContactToPhoneBook(u, true);
                                }
                            });
                            TL_contact newContact = new TL_contact();
                            newContact.user_id = u.id;
                            ArrayList<TL_contact> arrayList = new ArrayList();
                            arrayList.add(newContact);
                            MessagesStorage.getInstance().putContacts(arrayList, false);
                            if (u.phone != null && u.phone.length() > 0) {
                                CharSequence name = ContactsController.formatName(u.first_name, u.last_name);
                                MessagesStorage.getInstance().applyPhoneBookUpdates(u.phone, "");
                                Contact contact = (Contact) ContactsController.this.contactsBookSPhones.get(u.phone);
                                if (contact != null) {
                                    int index = contact.shortPhones.indexOf(u.phone);
                                    if (index != -1) {
                                        contact.phoneDeleted.set(index, Integer.valueOf(0));
                                    }
                                }
                            }
                        }
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            public void run() {
                                Iterator i$ = res.users.iterator();
                                while (i$.hasNext()) {
                                    User u = (User) i$.next();
                                    MessagesController.getInstance().putUser(u, false);
                                    if (ContactsController.this.contactsDict.get(u.id) == null) {
                                        TL_contact newContact = new TL_contact();
                                        newContact.user_id = u.id;
                                        ContactsController.this.contacts.add(newContact);
                                        ContactsController.this.contactsDict.put(newContact.user_id, newContact);
                                    }
                                }
                                ContactsController.this.buildContactsSectionsArrays(true);
                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.contactsDidLoaded, new Object[0]);
                            }
                        });
                    }
                }
            }, 6);
        }
    }

    public void deleteContact(final ArrayList<User> users) {
        if (users != null && !users.isEmpty()) {
            TL_contacts_deleteContacts req = new TL_contacts_deleteContacts();
            final ArrayList<Integer> uids = new ArrayList();
            Iterator i$ = users.iterator();
            while (i$.hasNext()) {
                User user = (User) i$.next();
                InputUser inputUser = MessagesController.getInputUser(user);
                if (inputUser != null) {
                    uids.add(Integer.valueOf(user.id));
                    req.id.add(inputUser);
                }
            }
            ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {

                class C03141 implements Runnable {
                    C03141() {
                    }

                    public void run() {
                        Iterator i$ = users.iterator();
                        while (i$.hasNext()) {
                            ContactsController.this.deleteContactFromPhoneBook(((User) i$.next()).id);
                        }
                    }
                }

                class C03152 implements Runnable {
                    C03152() {
                    }

                    public void run() {
                        boolean remove = false;
                        Iterator i$ = users.iterator();
                        while (i$.hasNext()) {
                            User user = (User) i$.next();
                            TL_contact contact = (TL_contact) ContactsController.this.contactsDict.get(user.id);
                            if (contact != null) {
                                remove = true;
                                ContactsController.this.contacts.remove(contact);
                                ContactsController.this.contactsDict.remove(user.id);
                            }
                        }
                        if (remove) {
                            ContactsController.this.buildContactsSectionsArrays(false);
                        }
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.updateInterfaces, Integer.valueOf(1));
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.contactsDidLoaded, new Object[0]);
                    }
                }

                public void run(TLObject response, TL_error error) {
                    if (error == null) {
                        MessagesStorage.getInstance().deleteContacts(uids);
                        Utilities.phoneBookQueue.postRunnable(new C03141());
                        Iterator i$ = users.iterator();
                        while (i$.hasNext()) {
                            User user = (User) i$.next();
                            if (user.phone != null && user.phone.length() > 0) {
                                CharSequence name = UserObject.getUserName(user);
                                MessagesStorage.getInstance().applyPhoneBookUpdates(user.phone, "");
                                Contact contact = (Contact) ContactsController.this.contactsBookSPhones.get(user.phone);
                                if (contact != null) {
                                    int index = contact.shortPhones.indexOf(user.phone);
                                    if (index != -1) {
                                        contact.phoneDeleted.set(index, Integer.valueOf(1));
                                    }
                                }
                            }
                        }
                        AndroidUtilities.runOnUIThread(new C03152());
                    }
                }
            });
        }
    }

    public void reloadContactsStatuses() {
        saveContactsLoadTime();
        MessagesController.getInstance().clearFullUsers();
        final Editor editor = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0).edit();
        editor.putBoolean("needGetStatuses", true).commit();
        ConnectionsManager.getInstance().sendRequest(new TL_contacts_getStatuses(), new RequestDelegate() {
            public void run(final TLObject response, TL_error error) {
                if (error == null) {
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        public void run() {
                            editor.remove("needGetStatuses").commit();
                            Vector vector = response;
                            if (!vector.objects.isEmpty()) {
                                ArrayList<User> dbUsersStatus = new ArrayList();
                                Iterator i$ = vector.objects.iterator();
                                while (i$.hasNext()) {
                                    TL_contactStatus object = i$.next();
                                    User toDbUser = new User();
                                    TL_contactStatus status = object;
                                    if (status != null) {
                                        if (status.status instanceof TL_userStatusRecently) {
                                            status.status.expires = -100;
                                        } else if (status.status instanceof TL_userStatusLastWeek) {
                                            status.status.expires = -101;
                                        } else if (status.status instanceof TL_userStatusLastMonth) {
                                            status.status.expires = -102;
                                        }
                                        User user = MessagesController.getInstance().getUser(Integer.valueOf(status.user_id));
                                        if (user != null) {
                                            user.status = status.status;
                                        }
                                        toDbUser.status = status.status;
                                        dbUsersStatus.add(toDbUser);
                                    }
                                }
                                MessagesStorage.getInstance().updateUsers(dbUsersStatus, true, true, true);
                            }
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.updateInterfaces, Integer.valueOf(4));
                        }
                    });
                }
            }
        });
    }

    public void loadPrivacySettings() {
        if (this.loadingDeleteInfo == 0) {
            this.loadingDeleteInfo = 1;
            ConnectionsManager.getInstance().sendRequest(new TL_account_getAccountTTL(), new RequestDelegate() {
                public void run(final TLObject response, final TL_error error) {
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        public void run() {
                            if (error == null) {
                                ContactsController.this.deleteAccountTTL = response.days;
                                ContactsController.this.loadingDeleteInfo = 2;
                            } else {
                                ContactsController.this.loadingDeleteInfo = 0;
                            }
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.privacyRulesUpdated, new Object[0]);
                        }
                    });
                }
            });
        }
        if (this.loadingLastSeenInfo == 0) {
            this.loadingLastSeenInfo = 1;
            TL_account_getPrivacy req = new TL_account_getPrivacy();
            req.key = new TL_inputPrivacyKeyStatusTimestamp();
            ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
                public void run(final TLObject response, final TL_error error) {
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        public void run() {
                            if (error == null) {
                                TL_account_privacyRules rules = response;
                                MessagesController.getInstance().putUsers(rules.users, false);
                                ContactsController.this.privacyRules = rules.rules;
                                ContactsController.this.loadingLastSeenInfo = 2;
                            } else {
                                ContactsController.this.loadingLastSeenInfo = 0;
                            }
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.privacyRulesUpdated, new Object[0]);
                        }
                    });
                }
            });
        }
        NotificationCenter.getInstance().postNotificationName(NotificationCenter.privacyRulesUpdated, new Object[0]);
    }

    public void setDeleteAccountTTL(int ttl) {
        this.deleteAccountTTL = ttl;
    }

    public int getDeleteAccountTTL() {
        return this.deleteAccountTTL;
    }

    public boolean getLoadingDeleteInfo() {
        return this.loadingDeleteInfo != 2;
    }

    public boolean getLoadingLastSeenInfo() {
        return this.loadingLastSeenInfo != 2;
    }

    public ArrayList<PrivacyRule> getPrivacyRules() {
        return this.privacyRules;
    }

    public void setPrivacyRules(ArrayList<PrivacyRule> rules) {
        this.privacyRules = rules;
        NotificationCenter.getInstance().postNotificationName(NotificationCenter.privacyRulesUpdated, new Object[0]);
        reloadContactsStatuses();
    }

    public static String formatName(String firstName, String lastName) {
        int length;
        int i = 0;
        if (firstName != null) {
            firstName = firstName.trim();
        }
        if (lastName != null) {
            lastName = lastName.trim();
        }
        if (firstName != null) {
            length = firstName.length();
        } else {
            length = 0;
        }
        if (lastName != null) {
            i = lastName.length();
        }
        StringBuilder result = new StringBuilder((i + length) + 1);
        if (LocaleController.nameDisplayOrder == 1) {
            if (firstName != null && firstName.length() > 0) {
                result.append(firstName);
                if (lastName != null && lastName.length() > 0) {
                    result.append(" ");
                    result.append(lastName);
                }
            } else if (lastName != null && lastName.length() > 0) {
                result.append(lastName);
            }
        } else if (lastName != null && lastName.length() > 0) {
            result.append(lastName);
            if (firstName != null && firstName.length() > 0) {
                result.append(" ");
                result.append(firstName);
            }
        } else if (firstName != null && firstName.length() > 0) {
            result.append(firstName);
        }
        return result.toString();
    }
}
