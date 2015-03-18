package filemanager;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import java.io.*;

/**
 *
 * @author Dmytro
 */
public class Main extends MIDlet
{
    public static Main midlet;
    public static Images img = null;
    public static Locale locale;
    public static String currentPath = null;
    public static String currentFile = null;
    public static FileSelectCanvas FileSelect;
    public static KeyConfigCanvas keycfg;
    public static DiskInfo diskinfo;
    public static ImageViewCanvas imageview;
    public static PlayerCanvas player;
    public static MenuCanvas menu;
    public static VideoPlayerCanvas videoplayer;
    public static TextViewCanvas textEditor;
    public static Filesystem fs;
    public static Display dsp;
    public static WaitCanvas wait;
    public static boolean isFavorite = false;
    public boolean alreadyStarted;
    public static int loadStage = -1;
    public static int stagesCount = 7;
    public SplashCanvas splashScreen;
    public static final int COPYBUFSIZE = 65536;
    public static final int ARCBUFSIZE = 8192;

    /**
     *
     */
    public Main()
    {
        midlet = this;
        alreadyStarted = false;
        img = null;
    }

    /**
     *
     * @throws MIDletStateChangeException
     */
    public void startApp() throws MIDletStateChangeException
    {
        if (!alreadyStarted)
        {
            String lang = getAppProperty("Default-language");
            
            Options.restoreOptions();
            if (!Options.firstTime)
                lang = Options.language;
            
            // Показываем заставку
            dsp = Display.getDisplay(this);
            loadStage = 1;
            Options.loadFavorites();
            
            if (!Options.quickSplash)
                dsp.setCurrent(splashScreen = new SplashCanvas());
            
            // Загружаем список языков и язык
            if (!Locale.readLocaleList())
                throw new MIDletStateChangeException("Fatal error: /lang/lang.ini not found");
            
            try
            {
                locale = new Locale(lang);
                Options.language = lang;
            } catch (IOException iox)
            {
                Options.language = "en";
                try
                {
                    locale = new Locale("en");
                } catch (IOException iox1)
                {
                    throw new MIDletStateChangeException("Cannot load even default \"en\" locale!");
                }
            }
            loadStage++;
            
            // Загружаем картинки...
            try
            {
                img = new Images();
                loadStage++;
            } catch (IOException iox1)
            {
                throw new MIDletStateChangeException("Cannot load images: " + iox1.getMessage());
            }
            
            textEditor = new TextViewCanvas();
            keycfg = new KeyConfigCanvas();
            loadStage++;
            imageview = new ImageViewCanvas();
            player = new PlayerCanvas();
            wait = new WaitCanvas();
            loadStage++;
            videoplayer = new VideoPlayerCanvas();
            menu = new MenuCanvas();
            loadStage++;
            
            try
            {
                fs = new Filesystem();
            } catch (Exception iox)
            {
                throw new MIDletStateChangeException("Fatal error: cannot init filesystem driver (" + iox.getMessage() + ")");
            }
            
            FileSelect = new FileSelectCanvas();
            diskinfo = new DiskInfo();
            alreadyStarted = true;
            loadStage++;
            loadStage = 0x100; // 0x100 = подождать ещё
            
            if (Options.quickSplash)
            {
                splashScreen = null;
                startUI();
            }
        }
    }

    /**
     * Запуск интерфейса.
     */
    public static void startUI()
    {
        if (Options.firstTime)
            dsp.setCurrent(new EulaForm());
        else
            WaitCanvas.start();
    }

    /**
     * Приостановить выполнение приложения - вызывается JRE.
     */
    public void pauseApp()
    {
        notifyPaused();
    }

    /**
     * Уничтожить приложение - вызывается JRE.
     * 
     * @param unconditional
     */
    public void destroyApp(boolean unconditional)
    {
        Options.saveOptions();
        Options.saveFavorites();
        this.notifyDestroyed();
    }
}
