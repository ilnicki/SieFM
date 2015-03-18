package filemanager;

import java.io.*;
import java.util.Vector;
import com.vmx.*;

/**
 *
 * @author Dmytro
 */
public class Locale
{

    public static final int SELECT_DRIVE = 0;
    public static final int FAVOURITE = 1;
    public static final int OPTIONS_CMD = 2;
    public static final int EXIT_CMD = 3;
    public static final int DISK = 4;
    public static final int DISK_INFO = 5;
    public static final int DISK_TOTAL_SIZE = 6;
    public static final int KB = 7;
    public static final int BYTE = 8;
    public static final int DISK_AVAILABLE = 9;
    public static final int SELECT_CMD = 10;
    public static final int DELETE_CMD = 11;
    public static final int DELETE_ALL_CMD = 12;
    public static final int BACK_CMD = 13;
    public static final int PATH_NOT_EXIST = 14;
    public static final int CONFIRMATION = 15;
    public static final int DEL_RECORD_FAVOR = 16;
    public static final int DEL_ALL_FAVOR = 17;
    public static final int YES_CMD = 18;
    public static final int NO_CMD = 19;
    public static final int SAVE_CMD = 20;
    public static final int CLEAR_CMD = 21;
    public static final int ERROR = 22;
    public static final int FILE_NAME_EXIST = 23;
    public static final int FILE_NOT_COPIED = 24;
    public static final int FILE_NOT_MOVED = 25;
    public static final int FORMAT_NOT_SUPP = 26;
    public static final int OPEN_CMD = 27;
    public static final int INSERT_CMD = 28;
    public static final int PROPERTY_CMD = 29;
    public static final int RENAME_CMD = 30;
    public static final int COPY_CMD = 31;
    public static final int MOVE_CMD = 32;
    public static final int NEW_FOLDER_CMD = 33;
    public static final int NEW_FILE_CMD = 34;
    public static final int TO_FAVOUR_CMD = 35;
    public static final int DISK_INFO_CMD = 36;
    public static final int MMC_INFO_CMD = 37;
    public static final int PREFERENCES_CMD = 38;
    public static final int HELP_CMD = 39;
    public static final int OK_CMD = 40;
    public static final int CANCEL_CMD = 41;
    public static final int SELECT_PASTE_IN_FOLDER = 42;
    public static final int INFORMATION = 43;
    public static final int FOLDER_NAME = 44;
    public static final int FILE_NAME = 45;
    public static final int SIZE = 46;
    public static final int ATTR = 47;
    public static final int LAST_MODIF = 48;
    public static final int RENAME = 49;
    public static final int NAME = 50;
    public static final int NAME_EXIST_SELECT_ANOTHER = 51;
    public static final int READ_ONLY = 52;
    public static final int NOT_RENAMED = 53;
    public static final int PREF_SHOW_HIDDEN_FILES = 54;
    public static final int PREF_SHOW = 55;
    public static final int PREF_DISK_3 = 56;
    public static final int PREF_OPEN = 57;
    public static final int PREF_SPLASH = 58;
    public static final int PREF_DONOTSHOW = 59;
    public static final int CREATE_NEW_FOLDER = 60;
    public static final int CREATE_NEW_FILE = 61;
    public static final int NOT_CREATE_NEW_FOLDER = 62;
    public static final int WAIT_PLEASE = 63;
    public static final int WAIT = 64; // = 63 + 64
    public static final int PLAYER_STOP = 65;
    public static final int PLAYER_PLAY = 66;
    public static final int PLAYER_PAUSE = 67;
    public static final int IMAGEVIEW_SCALED = 68;
    public static final int ATTENTION = 69;
    public static final int SOURCE_FILE_READONLY_BE_COPIED = 70;
    public static final int DEL_SELECTED_FILE = 71;
    public static final int DEL_MARKED_FILES = 72;
    public static final int FOLDER_DELETED = 73;
    public static final int FOLDER_NOT_EMPTY = 74;
    public static final int FILE_DELETED = 75;
    public static final int FILE_NOT_DELETED = 76;
    public static final int LICENSE_AGR = 77;
    public static final int FILE_TYPE = 78;
    public static final int FILE_NOT_SAVED_EXIT = 79;
    public static final int SAVED = 80;
    public static final int BUFFER = 81;
    public static final int DEL_FROM_BUFFER = 82;
    public static final int DEL_ALL_FROM_BUFFER = 83;
    public static final int OPERATION_OK = 84;
    public static final int FILE = 85;
    public static final int FILE_NOT_SAVED = 86;
    public static final int BUFFER_EMPTY = 87;
    public static final int MARK_CMD = 88;
    public static final int MARK_ALL_CMD = 89;
    public static final int DEMARK_ALL_CMD = 90;
    public static final int PREFS_OPEN_NOT_SUPP = 91;
    public static final int PREFS_YES = 92;
    public static final int FILE_TOO_BIG = 93;
    public static final int NEED_RESTART = 94;
    public static final int EXTRACT_CMD = 95;
    public static final int EXTRACT_ALL_CMD = 96;
    public static final int EXTRACT_TO = 97;
    public static final int FOLDER_IMPOSSIBLE = 98;
    public static final int FILES_EXTRACTED = 99;
    public static final int YES_FOR_ALL = 100;
    public static final int NO_FOR_ALL = 101;
    public static final int OVERWRITE_QUESTION = 102;
    public static final int COMPRESSED_SIZE = 103;
    public static final int MENU_FILE = 104;
    public static final int MENU_ARCHIVE = 105;
    public static final int MENU_PROPERTIES = 106;
    public static final int MENU_OPERATIONS = 107;
    public static final int MENU_CREATE = 108;
    public static final int MENU_SHOW = 109;
    public static final int PREF_NO_EFFECTS = 110;
    public static final int CREATE_ZIP = 111;
    public static final int COMPRESSION_LEVEL = 112;
    public static final int TAG_EDITOR = 113;
    public static final int ID3_SONG = 114;
    public static final int ID3_ARTIST = 115;
    public static final int ID3_ALBUM = 116;
    public static final int ID3_YEAR = 117;
    public static final int ID3_COMMENT = 118;
    public static final int ID3_TRACKNUM = 119;
    public static final int EDIT_ID3_CMD = 120;
    public static final int MB = 121;
    public static final int KEY_LEFT = 122;
    public static final int KEY_RIGHT = 123;
    public static final int KEY_UP = 124;
    public static final int KEY_DOWN = 125;
    public static final int KEY_LSK = 126;
    public static final int KEY_RSK = 127;
    public static final int KEY_JOY = 128;
    public static final int KEY_DIAL = 129;
    public static final int KEY_CANCEL = 130;
    public static final int PREV_SCREEN_CMD = 131;
    public static final int NEXT_SCREEN_CMD = 132;
    public static final int PREV_FILE_CMD = 133;
    public static final int NEXT_FILE_CMD = 134;
    public static final int UP_LEVEL_CMD = 135;
    public static final int MENU_ADDITIONAL = 136;
    public static final int PANELS = 137;
    public static final int PANEL_NUMS = 138 /*[+0..9]*/;
    public static final int FULLSCREEN_CMD = 148;
    public static final int KEY_NO_ACTION = 149;
    public static final int KEYBOARD_CONFIG_CMD = 150;

    public static String LICENSE_AGR_TEXT;
    public static String ABOUT_MIDLET_NAME = "SieFM v" + Main.midlet.getAppProperty("MIDlet-Version");
    public static String Strings[];
    public static String lang;
    public static String locales[] = null;   // имена языков типа "ru", "en"...
    public static String languages[] = null; // названия языков (русский, english, ...)

    /**
     * Конструктор.
     *
     * @param lang
     * @throws java.io.IOException
     * @throws java.io.UTFDataFormatException
     */
    public Locale(String lang) throws IOException, UTFDataFormatException
    {
        this.lang = lang;
        BufDataInputStream bdis = new BufDataInputStream(2048, getClass().getResourceAsStream("/lang/" + lang + "/strings.lng"));
        if (!bdis.checkBOM())
        {
            bdis.close();
            throw new IOException("/lang/" + lang + "/strings.lng is not in UTF-8 BOM");
        }
        char c;
        Vector v = new Vector(16, 16);
        String s;
        while (bdis.available() > 0)
        {
            s = "";
            do
            {
                c = bdis.readCharUTF();
                if (c == '\n')
                    break;
                s += c;
            } while (bdis.available() > 0);
            v.addElement(s);
        }
        Strings = new String[v.size()];
        v.copyInto(Strings);
        bdis.close();
        v = null;
        Strings[63] += "\n" + Strings[64];
        // Текст лицензии
        bdis = new BufDataInputStream(2048, getClass().getResourceAsStream("/lang/" + lang + "/license.lng"));
        if (!bdis.checkBOM())
        {
            bdis.close();
            throw new IOException("/lang/" + lang + "/license.lng is not in UTF-8 BOM");
        }
        LICENSE_AGR_TEXT = bdis.readUTF(bdis.available());
        bdis.close();
    }

    /**
     * Получить поток с текстом about'а.
     *
     * @return
     */
    public static InputStream getAboutStream()
    {
        return InputStream.class.getResourceAsStream("/lang/" + lang + "/about.lng");
    }

    /**
     * Получить список доступных языков.
     *
     * @return
     */
    public static boolean readLocaleList()
    {
        Vector v = new Vector(8, 8);
        String s;
        try
        {
            BufDataInputStream bdis = new BufDataInputStream(2048, Main.class.getResourceAsStream("/lang/lang.ini"));
            if (!bdis.checkBOM())
            {
                bdis.close();
                throw new IOException("/lang/lang.ini is not in UTF-8 BOM");
            }
            char c;
            while (bdis.available() > 0)
            {
                s = "";
                do
                {
                    c = bdis.readCharUTF();
                    if (c == '\n')
                        break;
                    s += c;
                } while (bdis.available() > 0);
                if (s.indexOf(",") <= 0)
                {
                    bdis.close();
                    throw new IOException("Invalid language specification: " + s);
                }
                v.addElement(s);
            }
            bdis.close();
        } catch (IOException iox)
        {
            return false;
        }
        languages = new String[v.size()];
        locales = new String[v.size()];
        for (int i = 0; i < languages.length; i++)
        {
            s = (String) (v.elementAt(i));
            locales[i] = s.substring(0, s.indexOf(","));
            languages[i] = s.substring(s.indexOf(",") + 1);
        }
        return true;
    }
}
