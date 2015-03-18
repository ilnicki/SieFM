package filemanager;

import javax.microedition.lcdui.*;
import java.util.Vector;
import com.vmx.*;

class CopyMoveThread implements Runnable
{
    public boolean interrupt = false;
    public CopyMoveThread ()
    {
        interrupt = false;
    }
    /**
     * Функция потока копирования/перемещения
     */
    public void run ()
    {
        // папка, куда копируем/перемещаем
        String targetPath = main.currentPath;
        // файл для операции
        String sourceFileFullName;
        String sourceOnlyName;
        boolean mustMove;
        boolean forAll = false, yes = false, fileExists = false;
        while (Buffer.buf.size() > 0 && !interrupt)
        {
            mustMove = Buffer.move.get (Buffer.buf.size()-1) == 1 ? true : false; // true, если надо перемещать!
            sourceFileFullName = noLastSlash((String)Buffer.buf.elementAt (Buffer.buf.size () - 1)); // посл файл из буфера
            sourceOnlyName = sourceFileFullName.substring (sourceFileFullName.lastIndexOf ('/') + 1);
            fileExists = filesystem.isFileExist (targetPath + sourceOnlyName);
            if (fileExists && !forAll) // файл существует и ещё не было команды "... для всех!"
            {
                alConfirmOverwrite al = new alConfirmOverwrite (sourceOnlyName, main.FileSelect);
                main.dsp.setCurrent (al);
                al.t.start ();
                try
                {
                    al.t.join(); // ждём, пока он СДОХНЕТ :)
                } catch (InterruptedException x) {}
                yes = false;
                // если да или да для всех => yes!
                if (al.modalResult == al.cmdYes || al.modalResult == al.cmdYesForAll)
                    yes = true;
                // если "отмена", значит стоп.
                if (al.modalResult == al.cmdCancel)
                    break;
                // команда для всех
                if (al.modalResult == al.cmdYesForAll || al.modalResult == al.cmdNoForAll)
                    forAll = true;
                al = null;
                main.dsp.setCurrent (Buffer.waitAlert);
            }
            if (!fileExists || yes)
            {
                if (!mustMove) // копирование
                {
                    if (!filesystem.copyFile (sourceFileFullName, targetPath + sourceOnlyName))
                        Buffer.errors += Locale.Strings[Locale.FILE] + sourceOnlyName + " " + Locale.Strings[Locale.FILE_NOT_COPIED] + "\n\n";
                }
                else // перемещение
                {
                    if (filesystem.copyFile (sourceFileFullName, targetPath + sourceOnlyName)) // скопирован?
                    {
                        if (filesystem.isReadOnly (sourceFileFullName)) // источник readonly
                            Buffer.errors += sourceOnlyName + " " + Locale.Strings[Locale.SOURCE_FILE_READONLY_BE_COPIED] + "\n\n";
                        else
                            filesystem.deleteFile (sourceFileFullName, true); // удаляем исходный файл
                    }
                    else // не перемещен
                        Buffer.errors += Locale.Strings[Locale.FILE] + sourceOnlyName + " " + Locale.Strings[Locale.FILE_NOT_MOVED] + "\n\n";
                }
            }
            // удалить последний из буфера
            Buffer.remove (Buffer.buf.size() - 1);
        }
        Buffer.waitAlert = null;
        main.dsp.setCurrent (new alMessage (Buffer.errors));
    }
    /** Обрезание последнего слэша у s */
    public static String noLastSlash (String s)
    {
        if (s.charAt (s.length()-1) == '/')
            return s.substring (0,s.length()-1);
        return s;
    }
}

class GaugeCallback
    implements ProgressCallback
{
    protected Gauge gg;
    public GaugeCallback (Gauge assoc)
    {
        gg = assoc;
    }
    public void setMax (int max)
    {
        gg.setMaxValue (max);
    }
    public void setProgress (int progress)
    {
        gg.setValue (progress);
    }
    public void progress (int plus)
    {
        gg.setValue (gg.getValue () + plus);
    }
}

class WaitComLis implements CommandListener
{
    public void commandAction (Command c, Displayable d)
    {
        Buffer.stopThread ();
        main.dsp.setCurrent (main.FileSelect);
    }
}

class AfterburnThread implements Runnable
{
    protected Thread t;
    public AfterburnThread (Thread wait)
    {
        t = wait;
    }
    public void run ()
    {
        try
        {
            t.join ();
        } catch (Exception xx) {}
        Buffer.clear ();
        main.dsp.setCurrent (new alMessage (Buffer.zz.error));
        Buffer.zz = null;
    }
}

public class Buffer
{
    protected static Thread t = null;
    public static String errors = "";
    public static Alert waitAlert = null;
    protected static WaitComLis wcl = null;
    public static Vector buf = new Vector ();
    public static IntVector move = new IntVector ();
    public static Zipper zz = null;
    public static CopyMoveThread cmt = null;
    /**
     * Добавить в буфер
     */
    public static void add (String file, int moveit)
    {
        buf.addElement (file);
        move.add (moveit);
    }
    /**
     * Удалить из буфера
     */
    public static void remove (int index)
    {
        buf.removeElementAt (index);
        move.remove (index, 1);
    }
    /**
     * Очистить буфер
     */
    public static void clear ()
    {
        buf.removeAllElements ();
        move.remove (0, move.size ());
    }
    /**
     * Вернуть буфер как массив строк
     */
    public static String[] getBuffer ()
    {
        String bufs [] = new String [buf.size ()];
        buf.copyInto (bufs);
        return bufs;
    }
    /**
     * Запустить поток копирования/перемещения
     */
    public static void copyMoveFiles ()
    {
        errors = "";
        if (buf.size () > 0)
        {
            createWait ();
            main.dsp.setCurrent (waitAlert);
            stopThread ();
            cmt = new CopyMoveThread ();
            cmt.interrupt = false;
            t = new Thread (cmt);
            t.start ();
        }
    }
    /**
     * Запустить поток сжатия файлов
     */
    public static void zipFiles (String zipName, int zipLevel)
    {
        errors = "";
        if (buf.size () > 0)
        {
            createWait ();
            Gauge gg = new Gauge (null, false, 1, 0);
            waitAlert.setIndicator (gg);
            GaugeCallback callback = new GaugeCallback (gg);
            main.dsp.setCurrent (waitAlert);
            stopThread ();
            zz = new Zipper (zipName, zipLevel, buf, callback);
            t = new Thread (zz);
            t.start ();
            Thread t2 = new Thread (new AfterburnThread (t));
            t2.start ();
        }
    }
    /**
     * Создать waitAlert
     */
    public static void createWait ()
    {
        waitAlert = new Alert (Locale.Strings[Locale.WAIT],
                Locale.Strings[Locale.WAIT_PLEASE],
                null, AlertType.INFO);
        waitAlert.setTimeout (Alert.FOREVER);
        waitAlert.addCommand (new Command (Locale.Strings[Locale.CANCEL_CMD], Command.CANCEL, 1));
        waitAlert.setCommandListener (wcl = new WaitComLis ());
    }
    /**
     * Остановить процесс
     */
    public static void stopThread ()
    {
        if (t != null && t.isAlive ())
        {
            if (zz != null)
                zz.interrupt = true;
            if (cmt != null)
                cmt.interrupt = true;
            t.interrupt ();
            try
            {
                t.join ();
            } catch (Exception x) {}
            t = null;
        }
    }
}
