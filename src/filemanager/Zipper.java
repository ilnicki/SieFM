package filemanager;

import com.vmx.*;
import java.util.zip.*;
import java.util.*;
import java.io.*;
import javax.microedition.io.Connector;
import com.siemens.mp.io.file.FileConnection;

/**
 * Класс-"оболочка" для java.util.zip. Для записи файлов использует
 * com.siemens.mp.io.file.FileConnection.
 */
public class Zipper implements Runnable
{
    protected String filename;
    protected int comprLevel;
    protected Vector files;
    protected ProgressCallback callback;
    public String error;
    public boolean interrupt;
    /**
     * Архивирует файлы/папки, заданные вектором files, методом Deflate и
     * уровнем сжатия comprLevel в новый ZIP-файл filename. Для индикации
     * прогресса операции используется callback.
     */
    public Zipper (String fn, int level, Vector filelist, ProgressCallback cb)
    {
        filename = fn;
        comprLevel = level;
        files = filelist;
        callback = cb;
        error = null;
        interrupt = false;
    }
    /**
     * Добавляет файл/папку file к открытому ZipOutputStream-у zos
     * во внутренний путь path
     */
    protected void zipDirOrFile (ZipOutputStream zos, String file,
        String path, ProgressCallback callback) throws IOException, ZipException
    {
        if (interrupt)
            return;
        if (file.charAt (file.length()-1) == '/')
            zip_a_dir (zos, file, path, callback);
        else zip_a_file (zos, file, path, callback);
    }
    /**
     * Добавляет файл file к открытому ZipOutputStream-у zos во
     * внутренний путь path
     */
    protected void zip_a_file (ZipOutputStream zos, String file,
        String path, ProgressCallback callback) throws IOException, ZipException
    {
        if (interrupt)
            return;
        //FileConnection fc = (FileConnection)Connector.open ("file:///" + file, Connector.READ);
        InputStream is = Connector.openInputStream ("file:///" + file);//fc.openInputStream ();
        zos.putNextEntry (new ZipEntry (path + file.substring (file.lastIndexOf('/')+1)));
        byte [] buf = new byte [filemanager.main.ARCBUFSIZE];
        int r;
        while (is.available () > 0 && !interrupt)
        {
            r = is.read (buf);
            if (callback != null)
                callback.progress (r);
            zos.write (buf, 0, r);
        }
        is.close ();
        //fc.close ();
    }
    /**
     * Добавляет папку dir к открытому ZipOutputStream-у zos во
     * внутренний путь path
     */
    protected void zip_a_dir (ZipOutputStream zos, String file,
        String path, ProgressCallback callback) throws IOException, ZipException
    {
        if (interrupt)
            return;
        FileConnection fc = (FileConnection)Connector.open ("file:///" + file, Connector.READ);
        String cdir = file.substring (0, file.length ()-1);
        int pos;
        if ((pos = cdir.lastIndexOf ('/')) >= 0)
            cdir = cdir.substring (pos+1);
        Enumeration files = fc.list ();
        while (files.hasMoreElements ())
            zipDirOrFile (zos, file+(String)files.nextElement (), path + cdir + "/", callback);
        fc.close ();
    }
    /**
     * Функция, осуществляющая собственно создание ZIP-архива
     */
    public void run ()
    {
        FileConnection fc = null;
        try
        {
            fc = (FileConnection)Connector.open ("file:///" + filename);
            if (fc.exists () && fc.isDirectory ())
                fc.delete ();
            if (!fc.exists ())
                fc.create ();
            OutputStream os;
            ZipOutputStream zos = new ZipOutputStream (os = fc.openOutputStream ());
            zos.setMethod (ZipOutputStream.DEFLATED);
            zos.setLevel (comprLevel);
            if (callback != null)
            {
                int max = 0;
                for (int i = 0; i < files.size (); i++)
                    max += filesystem.getSize ((String)files.elementAt (i));
                callback.setMax (max);
                callback.setProgress (0);
            }
            for (int i = 0; i < files.size () && !interrupt; i++)
                zipDirOrFile (zos, (String)files.elementAt (i), "", callback);
            zos.finish ();
            os.close ();
            fc.close ();
        }
        catch (Exception x)
        {
            error = x.getClass().getName () + ": " + x.getMessage ();
            if (fc != null)
            {
                try { fc.close (); }
                catch (Exception xx) {}
            }
        }
    }
}
