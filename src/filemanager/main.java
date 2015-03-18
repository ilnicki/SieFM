package filemanager;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import java.io.*;

public class main extends MIDlet
{
    public static main midlet;
    public static images img = null;
    public static Locale locale;
    public static String currentPath = null, currentFile = null;
    public static cvsFileSelect FileSelect;
    public static keyConfig keycfg;
    public static diskInfo diskinfo;
    public static cvsImageView imageview;
    public static cvsPlayer player;
    public static cvsMenu menu;
    public static cvsVideoPlayer videoplayer;
    public static cvsTextView textEditor;
    public static filesystem fs;
    public static Display dsp;
    public static cvsWait wait;
    public static boolean isFavorite = false;

    public boolean alreadyStarted;
    public static int loadStage = -1;
    public static int stagesCount = 7;
    public cvsSplash splashScreen;
    
    public static final int COPYBUFSIZE = 65536;
    public static final int ARCBUFSIZE = 8192;
    
    public main ()
    {
        midlet = this;
        alreadyStarted = false;
        img = null;
    }
    
    public void startApp () throws MIDletStateChangeException
    {
        if (!alreadyStarted)
        {
            String lang = getAppProperty ("Default-language");
            options.restoreOptions ();
            if (!options.firstTime)
                lang = options.language;
            // Показываем заставку
            dsp = Display.getDisplay (this);
            loadStage = 1;
            options.loadFavorites ();
            if (!options.quickSplash)
                dsp.setCurrent (splashScreen = new cvsSplash ());
            // Загружаем список языков и язык
            if (!Locale.readLocaleList ())
                throw new MIDletStateChangeException ("Fatal error: /lang/lang.ini not found");
            try 
            {
                locale = new Locale (lang);
                options.language = lang;
            }
            catch (IOException iox)
            {
                options.language = "en";
                try
                {
                    locale = new Locale ("en");
                }
                catch (IOException iox1)
                {
                    throw new MIDletStateChangeException ("Cannot load even default \"en\" locale!");
                }
            }
            loadStage++;
            // Загружаем картинки...
            try
            {
                img = new images ();
                loadStage++;
            }
            catch (IOException iox1)
            {
                throw new MIDletStateChangeException ("Cannot load images: " + iox1.getMessage ());
            }
            textEditor = new cvsTextView ();
            keycfg = new keyConfig ();
            loadStage++;
            imageview = new cvsImageView ();
            player = new cvsPlayer ();
            wait = new cvsWait ();
            loadStage++;
            videoplayer = new cvsVideoPlayer ();
            menu = new cvsMenu ();
            loadStage++;
            try
            {
                fs = new filesystem ();
            }
            catch (Exception iox)
            {
                throw new MIDletStateChangeException ("Fatal error: cannot init filesystem driver (" + iox.getMessage () + ")");
            }
            FileSelect = new cvsFileSelect ();
            diskinfo = new diskInfo ();
            alreadyStarted = true;
            loadStage++;
            loadStage = 0x100; // 0x100 = подождать ещё
            if (options.quickSplash)
            {
                splashScreen = null;
                startUI ();
            }
        }
    }
    /**
     * Запуск интерфейса
     */
    public static void startUI ()
    {
        if (options.firstTime)
            dsp.setCurrent (new frmEULA ());
        else
            wait.start ();
    }
    /** Приостановить выполнение приложения - вызывается JRE */
    public void pauseApp ()
    {
        notifyPaused ();
    }
    /** Уничтожить приложение - вызывается JRE */
    public void destroyApp (boolean unconditional)
    {
        options.saveOptions ();
        options.saveFavorites ();
        midlet.notifyDestroyed ();
    }
}
