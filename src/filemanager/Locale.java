package filemanager;

import java.io.*;
import java.util.Vector;
import com.vmx.*;

public class Locale
{
    public static final int SELECT_DRIVE = 0, FAVOURITE = 1, OPTIONS_CMD = 2,
        EXIT_CMD = 3, DISK = 4, DISK_INFO = 5, DISK_TOTAL_SIZE = 6, KB = 7,
        BYTE = 8, DISK_AVAILABLE = 9, SELECT_CMD = 10, DELETE_CMD = 11,
        DELETE_ALL_CMD = 12, BACK_CMD = 13, PATH_NOT_EXIST = 14,
        CONFIRMATION = 15, DEL_RECORD_FAVOR = 16, DEL_ALL_FAVOR = 17,
        YES_CMD = 18, NO_CMD = 19, SAVE_CMD = 20, CLEAR_CMD = 21,
        ERROR = 22, FILE_NAME_EXIST = 23, FILE_NOT_COPIED = 24,
        FILE_NOT_MOVED = 25, FORMAT_NOT_SUPP = 26, OPEN_CMD = 27,
        INSERT_CMD = 28, PROPERTY_CMD = 29, RENAME_CMD = 30, COPY_CMD = 31,
        MOVE_CMD = 32, NEW_FOLDER_CMD = 33, NEW_FILE_CMD = 34,
        TO_FAVOUR_CMD = 35, DISK_INFO_CMD = 36, MMC_INFO_CMD = 37,
        PREFERENCES_CMD = 38, HELP_CMD = 39, OK_CMD = 40, CANCEL_CMD = 41,
        SELECT_PASTE_IN_FOLDER = 42, INFORMATION = 43, FOLDER_NAME = 44,
        FILE_NAME = 45, SIZE = 46, ATTR = 47, LAST_MODIF = 48, RENAME = 49,
        NAME = 50, NAME_EXIST_SELECT_ANOTHER = 51, READ_ONLY = 52,
        NOT_RENAMED = 53, PREF_SHOW_HIDDEN_FILES = 54, PREF_SHOW = 55,
        PREF_DISK_3 = 56, PREF_OPEN = 57, PREF_SPLASH = 58,
        PREF_DONOTSHOW = 59, CREATE_NEW_FOLDER = 60, CREATE_NEW_FILE = 61,
        NOT_CREATE_NEW_FOLDER = 62, WAIT_PLEASE = 63, WAIT = 64, // = 63 + 64
        PLAYER_STOP = 65,  PLAYER_PLAY = 66, PLAYER_PAUSE = 67,
        IMAGEVIEW_SCALED = 68, ATTENTION = 69, SOURCE_FILE_READONLY_BE_COPIED = 70,
        DEL_SELECTED_FILE = 71, DEL_MARKED_FILES = 72, FOLDER_DELETED = 73,
        FOLDER_NOT_EMPTY = 74, FILE_DELETED = 75, FILE_NOT_DELETED = 76,
        LICENSE_AGR = 77, FILE_TYPE = 78, FILE_NOT_SAVED_EXIT = 79,
        SAVED = 80, BUFFER = 81, DEL_FROM_BUFFER = 82,
        DEL_ALL_FROM_BUFFER = 83, OPERATION_OK = 84, FILE = 85,
        FILE_NOT_SAVED = 86, BUFFER_EMPTY = 87, MARK_CMD = 88,
        MARK_ALL_CMD = 89, DEMARK_ALL_CMD = 90, PREFS_OPEN_NOT_SUPP = 91,
        PREFS_YES = 92, FILE_TOO_BIG = 93, NEED_RESTART = 94,
        EXTRACT_CMD = 95, EXTRACT_ALL_CMD = 96, EXTRACT_TO = 97,
        FOLDER_IMPOSSIBLE = 98, FILES_EXTRACTED = 99, YES_FOR_ALL = 100,
        NO_FOR_ALL = 101, OVERWRITE_QUESTION = 102, COMPRESSED_SIZE = 103,
        MENU_FILE = 104, MENU_ARCHIVE = 105, MENU_PROPERTIES = 106,
        MENU_OPERATIONS = 107, MENU_CREATE = 108, MENU_SHOW = 109,
        PREF_NO_EFFECTS = 110, CREATE_ZIP = 111, COMPRESSION_LEVEL = 112,
        TAG_EDITOR = 113, ID3_SONG = 114, ID3_ARTIST = 115, ID3_ALBUM = 116,
        ID3_YEAR = 117, ID3_COMMENT = 118, ID3_TRACKNUM = 119, EDIT_ID3_CMD = 120,
        MB = 121, KEY_LEFT = 122, KEY_RIGHT = 123, KEY_UP = 124, KEY_DOWN = 125,
        KEY_LSK = 126, KEY_RSK = 127, KEY_JOY = 128, KEY_DIAL = 129,
        KEY_CANCEL = 130, PREV_SCREEN_CMD = 131, NEXT_SCREEN_CMD = 132,
        PREV_FILE_CMD = 133, NEXT_FILE_CMD = 134, UP_LEVEL_CMD = 135,
        MENU_ADDITIONAL = 136, PANELS = 137, PANEL_NUMS = 138 /*[+0..9]*/,
        FULLSCREEN_CMD = 148, KEY_NO_ACTION = 149, KEYBOARD_CONFIG_CMD = 150;
    public static String LICENSE_AGR_TEXT;
    public static String ABOUT_MIDLET_NAME = "SieFM v" + main.midlet.getAppProperty ("MIDlet-Version");
    public static String Strings [];
    public static String lang;
    
    public static String locales [] = null;   // имена языков типа "ru", "en"...
    public static String languages [] = null; // названия языков (русский, english, ...)
    /**
     * Конструктор
     */
    public Locale (String lang) throws IOException, UTFDataFormatException
    {
        this.lang = lang;
        BufDataInputStream bdis = new BufDataInputStream (2048, getClass().getResourceAsStream ("/lang/" + lang + "/strings.lng"));
        if (!bdis.checkBOM ())
        {
            bdis.close ();
            throw new IOException ("/lang/" + lang + "/strings.lng is not in UTF-8 BOM");
        }
        char c = ' ';
        Vector v = new Vector (16, 16);
        String s;
        while (bdis.available () > 0)
        {
            s = "";
            do
            {
                c = bdis.readCharUTF ();
                if (c == '\n')
                    break;
                s += c;
            } while (bdis.available () > 0);
            v.addElement (s);
        }
        Strings = new String [v.size ()];
        v.copyInto (Strings);
        bdis.close ();
        v = null;
        Strings [63] += "\n" + Strings [64];
        // Текст лицензии
        bdis = new BufDataInputStream (2048, getClass().getResourceAsStream ("/lang/" + lang + "/license.lng"));
        if (!bdis.checkBOM ())
        {
            bdis.close ();
            throw new IOException ("/lang/" + lang + "/license.lng is not in UTF-8 BOM");
        }
        LICENSE_AGR_TEXT = bdis.readUTF (bdis.available ());
        bdis.close ();
    }
    /**
     * Получить поток с текстом about'а
     */
    public static InputStream getAboutStream ()
    {
        return InputStream.class.getResourceAsStream ("/lang/" + lang + "/about.lng");
    }
    /**
     * Получить список доступных языков
     */
    public static boolean readLocaleList ()
    {
        Vector v = new Vector (8, 8);
        String s;
        try
        {
            BufDataInputStream bdis = new BufDataInputStream (2048, main.class.getResourceAsStream ("/lang/lang.ini"));
            if (!bdis.checkBOM ())
            {
                bdis.close ();
                throw new IOException ("/lang/lang.ini is not in UTF-8 BOM");
            }
            char c = ' ';
            while (bdis.available () > 0)
            {
                s = "";
                do
                {
                    c = bdis.readCharUTF ();
                    if (c == '\n')
                        break;
                    s += c;
                } while (bdis.available () > 0);
                if (s.indexOf (",") <= 0)
                {
                    bdis.close ();
                    throw new IOException ("Invalid language specification: " + s);
                }
                v.addElement (s);
            }
            bdis.close ();
        } catch (IOException iox) { return false; }
        languages = new String [v.size()];
        locales = new String [v.size()];
        for (int i = 0; i < languages.length; i++)
        {
            s = (String)(v.elementAt (i));
            locales [i] = s.substring (0, s.indexOf (","));
            languages [i] = s.substring (s.indexOf (",")+1);
        }
        return true;
    }
}
