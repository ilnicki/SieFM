package filemanager;

import java.io.*;
import java.util.*;
import javax.microedition.io.Connector;
import javax.microedition.lcdui.*;
import com.siemens.mp.io.file.*;
import com.siemens.mp.util.zip.*;
import com.vmx.*;

public class filesystem
{
    protected static boolean sFile = false;
    public static String exts [];
    protected static String zipexts [];
    public static final int TYPE_UNKNOWN = 0, TYPE_SOUND = 1, TYPE_PICTURE = 2,
        TYPE_VIDEO = 3, TYPE_TEXT = 4, TYPE_ZIP = 5, TYPE_TMO = 6;
    /**
     * Конструктор
     */
    public filesystem () throws IOException
    {
        try { Class cl = Class.forName ("com.siemens.mp.io.File"); sFile = true; }
        catch (ClassNotFoundException x) { sFile = false; }
        BufDataInputStream bdis = new BufDataInputStream (2048, getClass().getResourceAsStream ("/ext.ini"));
        if (!bdis.checkBOM ())
        {
            bdis.close();
            throw new IOException ("/ext.ini is not in UTF-8 BOM");
        }
        Vector v = new Vector (16, 16);
        String s;
        char c;
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
        exts = new String [v.size ()];
        v.copyInto (exts);
        bdis.close();
        v.setSize (0);
        s = exts [TYPE_ZIP-1];
        String s3 = "";
        int i;
        while (s.length () > 0)
        {
            s = s.substring (1);
            i = s.indexOf (".");
            if (i < 0)
                i = s.length ();
            v.addElement ("." + s.substring (0, i) + "/");
            s3 += "." + s.substring (0, i) + "/";
            s = s.substring (i);
        }
        zipexts = new String [v.size ()];
        v.copyInto (zipexts);
    }
    /**
     *  Проверка "это директория"?
     *
     * @param fn String  - имя файла
     * @throws IOException
     * @return boolean  True - да, False - нет
     */
    protected static String lastIDR = null;
    protected static boolean lastIsDir = false;
    public static boolean isDir (String fn)
    {
        if (lastIDR == fn)
            return lastIsDir;
        if (divideZipName (fn) >= 0 && fn.charAt (fn.length()-1) == '/')
            return true;
        FileConnection conn = null;
        boolean r = false;
        try
        {
            conn = (FileConnection) Connector.open ("file:///" + fn, Connector.READ);
            r = conn.isDirectory ();
            conn.close ();
        } catch (Exception e) { r = false; }
        lastIDR = fn;
        lastIsDir = r;
        return r;
    }
    
    /**
     * Файл существует?
     *
     * @param fileName name of file to be examined
     * @return true if file exists, false otherwise
     */
    public static boolean isFileExist (String fileName)
    {
        FileConnection conn = null;
        boolean r = false;
        try
        {
            conn = (FileConnection) Connector.open ("file:///" + fileName, Connector.READ);
            r = conn.exists ();
            conn.close ();
        } catch (Exception e) { r = false; }
        return r;
    }
    
    /**
     * Получить список доступных дисков телефона 0:/ 1:/ ...
     *
     * @throws IOException
     * @return String[]  - список строк с именами доступных дисков
     */
    public static String[] listRoots () throws IOException
    {
        String roots[] = null;
        Vector vector = new Vector ();
        for (Enumeration enumeration = FileSystemRegistry.listRoots ();
             enumeration.hasMoreElements ();
             vector.addElement ((String) enumeration.nextElement ()));
        if (options.showDisk3)
        {
            vector.addElement ("b:/");
            vector.addElement ("3:/");
        }
        roots = new String [vector.size ()];
        vector.copyInto (roots);
        return roots;
    }
    /**
     * Распаковать в dir файлы из текущего ZIP-архива, перечисленные в vs
     * (с сохранением относительных путей). Если vs == null => распаковать всё
     */
    public static boolean unpackZip (Vector vs, String dir, Gauge progress)
    {
        if (zf == null)
            return false;
        try
        {
            Enumeration zfenum = zf.entries ();
            ZipEntry ze;
            String zn;
            String cm = "";
            int i = 0;
            while (zfenum.hasMoreElements ())
            {
                ze = (ZipEntry)zfenum.nextElement ();
                zn = ze.getName();
                if (vs != null)
                {
                    for (i = 0; i < vs.size (); i++)
                    {
                        cm = (String)vs.elementAt (i);
                        if ((cm.charAt (cm.length()-1) == '/' && zn.startsWith (cm)) || (zn.compareTo (cm) == 0))
                            break;
                    }
                }
                if (vs == null || i < vs.size ())
                {
                    if (zn.charAt (zn.length()-1) == '/')
                        makeNewDir (dir+zn);
                    else
                        copyStreamToFile (zf.getInputStream (ze), dir+zn, progress);
                }
            }
        } catch (IOException iox) { iox.printStackTrace (); return false; }
        return true;
    }
    /**
     * Получить список файлов из ZIP-файла zipfile,
     * находящихся внутри него в папке zipdir.
     */
    protected static ZipFile zf = null;
    protected static long zfsize = 0, zfcompsize = 0;
    public static String[] listZIP (String zipfile, String zipdir) throws Exception
    {
        boolean countsize = false;
        try
        {
            if (zf == null)
            {
                zf = new ZipFile ("file:///" + zipfile);
                zfsize = 0;
                zfcompsize = 0;
                countsize = true;
            }
            Enumeration zfenum = zf.entries ();
            String r[] = null;
            Vector vector = new Vector ();
            IntVector dirs = new IntVector ();
            int i, updirhash;
            String updir;
            ZipEntry ze;
            boolean addfld, killdirs;
            String zn;
            while (zfenum.hasMoreElements ())
            {
                ze = (ZipEntry)zfenum.nextElement ();
                if (countsize)
                {
                    zfsize += ze.getSize ();
                    zfcompsize += ze.getCompressedSize ();
                }
                zn = "/" + ze.getName();
                //if (zn.charAt (0) != '/')
                //    zn = '/' + zn;
                //System.out.println (zn);
                addfld = false;
                if (zn.charAt (zn.length()-1) == '/')
                {
                    // считаем что если в архиве хоть одна папка есть -
                    // - тогда он нормальный
                    dirs = null;
                    //addfld = true; // это папка, её надо в хэш-мэп добавить
                    zn = zn.substring (0, zn.length()-1);
                }
                if (zn.startsWith (zipdir))
                {
                    //if (zn.indexOf ("/", zipdir.length ()))
                    if (zn.lastIndexOf ('/') < zipdir.length ())
                    {
                        //if (addfld)
                        //    dirs.add (zn.substring (zipdir.length()).hashCode ());
                        vector.addElement (("/" + ze.getName()).substring (zipdir.length ()));
                    }
                    else if (dirs != null)
                    {
                        // этот else для кривых архивов! где папки не
                        // записаны, т.е идёт сразу folder/file без folder/
                        updir = zn.substring (zipdir.length (), zn.indexOf ("/", zipdir.length()));
                        updirhash = updir.hashCode ();
                        // если папки, блин, нету - её добавить надо...
                        if (dirs.find (updirhash) < 0)
                        {
                            dirs.add (updirhash);
                            vector.addElement (updir + "/");
                        }
                    }
                }
            }
            r = new String [vector.size ()];
            int ri = 0;
            for (i = 0; i < vector.size (); i++)
            {
                updir = (String)vector.elementAt (i);
                if (updir.charAt (updir.length()-1) == '/')
                    r [ri++] = updir;
            }
            for (i = 0; i < vector.size (); i++)
            {
                updir = (String)vector.elementAt (i);
                if (updir.charAt (updir.length()-1) != '/')
                    r [ri++] = updir;
            }
            return r;
        }
        catch (Exception x)
        {
            throw new Exception (x.getMessage ());
        }
    }
    /**
     * Получить суммарный размер всех элементов вектора
     */
    public static long getInZipSize (Vector vs, boolean compr)
    {
        if (zf == null)
            return -1;
        long r = 0;
        Enumeration zfenum = zf.entries ();
        ZipEntry ze;
        String zn;
        String cm = "";
        int i = 0;
        while (zfenum.hasMoreElements ())
        {
            ze = (ZipEntry)zfenum.nextElement ();
            zn = ze.getName();
            if (vs != null)
            {
                for (i = 0; i < vs.size (); i++)
                {
                    cm = (String)vs.elementAt (i);
                    if (cm.charAt (cm.length()-1) == '/')
                    {
                        if (zn.startsWith (cm))
                            break;
                    }
                    else if (zn.compareTo (cm) == 0)
                        break;
                }
            }
            if (vs == null || i < vs.size ())
            {
                if (!compr)
                    r += ze.getSize ();
                else r += ze.getCompressedSize();
            }
        }
        return r;
    }
    /**
     * Получить размер файла из zip-а
     */
    public static long getInZipSize (String name, boolean compr)
    {
        if (zf != null)
        {
            if (name.length () == 0)
            {
                if (!compr)
                    return zfsize;
                return zfcompsize;
            }
            else if (name.charAt (name.length ()-1) == '/')
            {
                Vector v = new Vector ();
                v.addElement (name);
                return getInZipSize (v, compr);
            }
            else
            {
                ZipEntry ze = zf.getEntry (name);
                if (ze != null)
                {
                    if (!compr)
                        return ze.getSize ();
                    else return ze.getCompressedSize ();
                }
            }
        }
        return -1;
    }
    /**
     * Получить InputStream файла inZip из открытого ZIP-архива
     */
    public static InputStream getZipInputStream (String inZip) throws IOException
    {
        if (zf != null)
            return zf.getInputStream (zf.getEntry(inZip));
        return null;
    }
    /**
     * Вернуть начало имени файла ВНУТРИ zip-архива (начинается после .../archive.zip/)
     */
    public static int divideZipName (String filename)
    {
        String fld = filename.toLowerCase ();
        int zipext = -1, j, si = -1;
        for (int i = 0; i < zipexts.length; i++)
        {
            j = filename.indexOf (zipexts[i]);
            if ((zipext < 0 && j >= 0) || j > zipext)
            {
                zipext = j;
                si = i;
            }
        }
        if (zipext >= 0 && !isDir (fld.substring (0, zipext+zipexts[si].length()-1)))
            return zipext+zipexts[si].length();
        return -1;
    }
    /**
     * Получить список файлов и папок в данной папке
     * Работа с ZIP-архивами прозрачная.
     *
     * @param folder String  - имя файла
     * @param includeHidden boolean - включая скрытые
     * @throws IOException
     * @return String[]  - массив строк с именами файлов или директорий
     */
    public static String[] list (String folder, boolean includeHidden) throws Exception
    {
        int zipext;
        if ((zipext = divideZipName (folder)) >= 0)
            return listZIP (folder.substring (0, zipext-1), folder.substring (zipext-1));
        String files[] = null;
        Vector vector = new Vector ();
        FileConnection fc = null;
        try
        {
            if (zf != null)
            {
                zf.close(); 
                zf = null;
            }
            fc = (FileConnection) Connector.open ("file:///" + folder, Connector.READ);
            for (Enumeration enumeration = fc.list ("*.*", includeHidden);
                 enumeration != null && enumeration.hasMoreElements ();
                 vector.addElement ((String)enumeration.nextElement()));
            fc.close();
            if (vector.size () == 0)
                return new String[0];
            files = new String[vector.size ()];
            vector.copyInto (files);
        }
        catch (Exception e)
        {
            if (files == null && sFile) // через FileConnection не вышло, пробуем через File
            {
                files = com.siemens.mp.io.File.list (main.currentPath, includeHidden);
                return files;
            }
            throw e;
        }
        return files;
    }
    public static String listError = "";
    /**
     * Определям тип файла по расширению
     */
    public static int fileType (String filename)
    {
        String ext = filename.toLowerCase();
        int extpos = ext.lastIndexOf ('.');
        if (extpos >= 0)
        {
            ext = ext.substring (extpos);
            for (int i = 0; i < exts.length; i++)
            {
                extpos = exts[i].indexOf(ext);
                if (extpos >= 0 && exts[i].charAt (extpos) == '.')
                    return i+1;
            }
        }
        return 0;
    }
    
    public static void deleteFileExFC (String fileName, boolean recursively) throws IOException
    {
        FileConnection conn = (FileConnection) Connector.open ("file:///" + fileName);
        if (conn.isDirectory () && recursively)
        {
            if (fileName.charAt(fileName.length()-1) == '/')
                fileName = fileName.substring (0, fileName.length () - 1);
            Enumeration en = conn.list ("*.*", true);
            while (en.hasMoreElements ())
                deleteFileExFC (fileName + "/" + (String)(en.nextElement()), true);
        }
        conn.delete ();
        conn.close ();
    }
    
    /**
     * Удалить файл / папку
     *
     * @param fileName String  - имя файла
     * @return boolean операция удачна
     */
    public static boolean deleteFile (String fileName, boolean recursively)
    {
        FileConnection conn = null;
        try
        {
            if (recursively)
                deleteFileExFC (fileName, recursively);
            else
            {
                conn = (FileConnection) Connector.open ("file:///" + fileName);
                conn.delete ();
                conn.close ();
            }
        }
        catch (Exception e) // пытаемся удалить через File.delete
        {
            if (sFile)
            {
                try
                {
                    if (com.siemens.mp.io.File.delete (fileName) == 1)
                        return true;
                } catch (Exception e1) { return false; }
            }
            return false;
        }
        return true;
    }
    
    /**
     * Папка / файл скрытый ?
     *
     * @param fileName String
     * @return boolean
     */
    public static boolean isHidden (String fileName)
    {
        FileConnection conn = null;
        boolean r = false;
        try
        {
            conn = (FileConnection) Connector.open ("file:///" + fileName, Connector.READ);
            r = conn.isHidden ();
            conn.close();
        }
        catch (Exception e)
        {
            //System.out.println("is Hidden error");
            r = false;
        }
        return r;
    }
    
    /**
     * Размер файла или папки
     *
     * @param fileName String  имя файла/папки
     * @return long  размер в байтах
     */
    public static long getSize (String fileName)
    {
        long size = -1;
        FileConnection conn = null;
        try
        {
            conn = (FileConnection) Connector.open ("file:///" + fileName, Connector.READ);
            if (conn.isDirectory ())
            {
                size = conn.directorySize (true);
                if (size < 0)
                    size = -2;
            }
            else size = conn.fileSize ();
            conn.close ();
        } catch (Exception e) { size = -1; }
        if (sFile && size == -2) // пытаемся получить размер папки через File.getDirectorySize
        { 
            try
            { 
                size = com.siemens.mp.io.File.getDirectorySize (fileName);
            } catch (Exception e1) {}
        }
        return size;
    }
    /*
    public static int getFileCount (String dirName, boolean includeHidden)
    {
        FileConnection conn = null;
        int r = 0;
        try
        {
            conn = (FileConnection) Connector.open ("file:///" + dirName, Connector.READ);
            if (conn.isDirectory ())
            {
                Enumeration en = conn.list ("*.*", includeHidden);
                for (r = 0; en.hasMoreElements(); en.nextElement(), r++);
            }
            conn.close ();
        } catch (Exception e) {}
        return r;
    }
    */
    /**
     * Файл только для чтения ?
     *
     * @param fn String
     * @return boolean
     */
    public static boolean isReadOnly (String fn)
    {
        if (divideZipName (fn) >= 0)
            return true;
        FileConnection conn = null;
        boolean r = false;
        try
        {
            conn = (FileConnection) Connector.open ("file:///" + fn, Connector.READ);
            r = !conn.canWrite ();
            conn.close ();
        } catch (Exception e) { r = false; }
        return r;
    }
    
    /**
     * Доступный размер диска
     *
     * @param fn String
     * @return long
     */
    public static long getDiskSpaceAvailable (String fn)
    {
        long size = -1;
        FileConnection conn = null;
        try
        {
            conn = (FileConnection) Connector.open ("file:///" + fn, Connector.READ);
            size = conn.availableSize ();
            conn.close ();
        } catch (Exception e) {}
        return size;
    }
    
    /**
     * Общий размер диска
     *
     * @param fn String
     * @return long
     */
    public static long getDiskSpaceTotal (String fn)
    {
        long size = -1;
        FileConnection conn = null;
        try
        {
            conn = (FileConnection) Connector.open ("file:///" + fn, Connector.READ);
            size = conn.totalSize ();
            conn.close();
        } catch (Exception e) {}
        return size;
    }
    
    /**
     * Сделать файл Read-only
     *
     * @param fn String  файл/папка
     * @param yes boolean да/нет
     * @return boolean операция успешна да/нет
     */
    public static boolean setReadOnly (String fn, boolean readOnly)
    {
        FileConnection conn = null;
        try
        {
            conn = (FileConnection) Connector.open ("file:///" + fn, Connector.READ_WRITE);
            conn.setWritable (!readOnly);
            conn.close();
        } catch (Exception e) { return false; }
        return true;
    }
    
    /**
     * Сделать файл скрытым
     *
     * @param fn String файл/папка
     * @param yes boolean да/нет
     * @return boolean операция успешна да/нет
     */
    public static boolean setHidden (String fn, boolean hidden)
    {
        FileConnection conn = null;
        try
        {
            conn = (FileConnection) Connector.open ("file:///" + fn, Connector.READ_WRITE);
            conn.setHidden (hidden);
            conn.close();
        }
        catch (Exception e) { return false; }
        return true;
    }
    
    /**
     * Дата последнего изменения файла
     *
     * @param fn String имя файла
     * @return long
     */
    public static long lastModified (String fn)
    {
        long time = 0;
        FileConnection conn = null;
        try
        {
            conn = (FileConnection) Connector.open ("file:///" + fn, Connector.READ);
            time = conn.lastModified ();
            conn.close ();
        } catch (Exception e) {}
        return time;
    }
    
    /**
     * Переименовать файл/папку
     *
     * @param oldName String
     * @param newName String
     * @return boolean
     */
    public static boolean renameFile (String oldName, String newName)
    {
        FileConnection conn = null;
        try
        {
            conn = (FileConnection) Connector.open ("file:///" + oldName);
            conn.rename (newName);
            conn.close();
        }
        catch (Exception e)
        {
            if (sFile)
            {
                try // пытаемся переименовать через File.rename
                {
                    if (com.siemens.mp.io.File.rename (oldName, main.currentPath + newName) >= 0)
                        return true;
                    else
                        return false;
                }
                catch (Exception e1)
                {
                    return false;
                }
            }
            return false;
        }
        return true;
    }
    /**
     * Нерекурсивно создать папку
     */
    public static boolean makeNewDirNotRec (String fn)
    {
        FileConnection conn = null;
        try
        {
            conn = (FileConnection) Connector.open ("file:///" + fn, Connector.READ_WRITE);
            conn.mkdir ();
            conn.close ();
        }
        catch (Exception e)
        {
            if (sFile)
            {
                try
                { // пытаемся создать папку средствами File
                    if (com.siemens.mp.io.File.mkdir (fn))
                        return true;
                    else
                        return false;
                }
                catch (Exception e1)
                {
                    return false;
                }
            }
            return false;
        }
        return true;
    }
    /**
     * Рекурсивно создать папку (т.е можно создавать Folder1/Folder2 где не
     * существует ещё даже Folder1)
     */
    public static boolean makeNewDir (String fn)
    {
        if (!makeNewDirNotRec (fn))
        {
            if (fn.charAt (fn.length() - 1) == '/')
                fn = fn.substring (0, fn.length()-1);
            if (makeNewDir (fn.substring (0, fn.lastIndexOf ('/'))))
                return makeNewDirNotRec (fn+"/");
        }
        else
            return true;
        return false;
    }
    /**
     * Копировать содержимое папки
     */
    public static boolean copyDirectoryContents (String oldName, String newName)
    {
        if (oldName.charAt (oldName.length()-1) != '/')
            oldName = oldName + "/";
        if (newName.charAt (newName.length()-1) != '/')
            newName = newName + "/";
        boolean r = false;
        try
        {
            String [] files = list (oldName, true);
            r = true;
            for (int i = 0; i < files.length; i++)
                r = r && copyFile (oldName + files [i], newName + files [i]);
        } catch (Exception x) {}
        return r;
    }
    /**
     * Копировать поток в файл и закрыть его
     */
    public static boolean copyStreamToFile (InputStream is, String fileName,
            Gauge progress)
    {
        FileConnection fc = null;
        try
        {
            fc = (FileConnection) Connector.open ("file:///" + fileName);
            if (fc.isDirectory ())
                fc.delete ();
            if (!fc.exists ())
                fc.create ();
            else
                fc.truncate (0);
            byte [] buf = new byte [main.COPYBUFSIZE];
            int buflen;
            OutputStream os = fc.openOutputStream ();
            while (is.available () > 0)
            {
                os.write (buf, 0, buflen = is.read (buf));
                if (progress != null)
                    progress.setValue (progress.getValue () + buflen);
            }
            os.close();
            is.close();
            fc.close ();
        }
        catch (IOException iox) 
        {
            if (fc != null)
                try { fc.close (); } catch (IOException iox1) {}
            return false;
        }
        return true;
    }
    /**
     * Копировать файл
     *
     * @param oldFileName String
     * @param newFileName String
     * @return boolean
     */
    public static boolean copyFile (String oldFileName, String newFileName)
    {
        FileConnection fc1 = null, fc2 = null;
        int bufsize = main.COPYBUFSIZE;
        //System.out.println (oldFileName + " -> " + newFileName);
        try
        {
            fc1 = (FileConnection) Connector.open ("file:///" + oldFileName, Connector.READ);
            if (!fc1.exists ())
                throw new Exception ();
            if (fc1.isDirectory ())
            {
                fc2 = (FileConnection) Connector.open ("file:///" + newFileName);
                if (fc2.exists () && !fc2.isDirectory ())
                    fc2.delete ();
                if (!fc2.exists ())
                {
                    fc2.close ();
                    makeNewDir (newFileName + "/");
                }
                else fc2.close ();
                fc1.close ();
                return copyDirectoryContents (oldFileName, newFileName);
            }
            fc2 = (FileConnection) Connector.open ("file:///" + newFileName);
            if (!fc2.exists ())
                fc2.create ();
            else fc2.truncate (0);
            byte [] buf = new byte [bufsize];
            InputStream is = fc1.openInputStream ();
            OutputStream os = fc2.openOutputStream ();
            while (is.available () > 0)
                os.write (buf, 0, is.read (buf));
            os.close();
            is.close();
            fc2.close ();
            fc1.close ();
        }
        catch (Exception e)
        {
            System.out.println (e.getMessage ());
            if (sFile)
            {
                try
                {
                    com.siemens.mp.io.File.copy (oldFileName, newFileName);
                } catch (Exception e1) { return false; }
                return true;
            }
            return false;
        }
        return true;
    }
    
    /**
     * Перевод времени в строку вида 01.01.2005 15:00:00
     *
     * @return String
     */
    public static String time2fileName ()
    {
        String dd, mm, yy, h, m, s;
        Calendar cal = Calendar.getInstance ();
        dd = String.valueOf (cal.get (cal.DAY_OF_MONTH));
        if (dd.length () == 1) 
            dd = "0" + dd;
        mm = String.valueOf (cal.get (cal.MONTH) + 1);
        if (mm.length () == 1)
            mm = "0" + mm;
        yy = String.valueOf (cal.get (cal.YEAR));
        h = String.valueOf (cal.get (cal.HOUR_OF_DAY));
        if (h.length () == 1)
            h = "0" + h;
        m = String.valueOf (cal.get (cal.MINUTE));
        if (m.length () == 1)
            m = "0" + m;
        s = String.valueOf (cal.get (cal.SECOND));
        if (s.length () == 1)
            s = "0" + s;
        return (dd + mm + yy.substring (2) + "_" + h + m + s);
    }
    
    /**
     *  Перевод времени в строку вида 01.01.2005 15:00:00
     * @param time long
     * @return String
     */
    public static String time2String (long time)
    {
        String dd, mm, yy, h, m, s;
        Calendar cal = Calendar.getInstance ();
        cal.setTime (new Date (time));
        dd = String.valueOf (cal.get (cal.DAY_OF_MONTH));
        if (dd.length () == 1)
            dd = "0" + dd;
        mm = String.valueOf (cal.get (cal.MONTH) + 1);
        if (mm.length () == 1)
            mm = "0" + mm;
        yy = String.valueOf (cal.get (cal.YEAR));
        h = String.valueOf (cal.get (cal.HOUR_OF_DAY));
        if (h.length () == 1)
            h = "0" + h;
        m = String.valueOf (cal.get (cal.MINUTE));
        if (m.length () == 1)
            m = "0" + m;
        s = String.valueOf (cal.get (cal.SECOND));
        if (s.length () == 1)
            s = "0" + s;
        return (dd + "." + mm + "." + yy + " " + h + ":" + m + ":" + s);
    }
    /**
     * Создать новый файл, записать туда text в UTF-8
     * и опционально BOM UTF-8 сигнатуру
     */
    public static boolean makeNewFile (String fn, String text, boolean writeBOM)
    {
        try
        {
            FileConnection fc = (FileConnection) Connector.open ("file:///" + fn);
            if (!fc.exists ())
                fc.create ();
            else fc.truncate (0);
            DataOutputStream dos = fc.openDataOutputStream ();
            if (writeBOM)
            {
                dos.writeByte (0xEF);
                dos.writeByte (0xBB);
                dos.writeByte (0xBF);
            }
            dos.write (text.getBytes ("UTF-8"));
            dos.close ();
            fc.close ();
        } catch (Exception e) { return false; }
        return true;
    }
    /**
     * Возвращает true если доступен класс com.siemens.mp.io.File
     */
    public static boolean hasFile ()
    {
        return sFile;
    }
    /**
     * Преобразовать double d в строку с точностью afterdot знаков после '.'
     */
    public static String doubleToString (double d, int afterdot)
    {
        String s = String.valueOf (d);
        int pos = s.indexOf ('.'), len = s.length ();
        if (pos >= 0 && len > pos+afterdot+1)
            return s.substring (0, pos+afterdot+1);
        return s;
    }
    /**
     * Преобразовывает число байт в нормальную строку с размером
     */
    public static String getSizeString (long bytes)
    {
        if (bytes < 1024)
            return String.valueOf (bytes) + " " + Locale.Strings[Locale.BYTE];
        else if (bytes < 1024*1024)
            return doubleToString ((double)bytes/1024, 2) + " " + Locale.Strings[Locale.KB];
        else
            return doubleToString ((double)bytes/(1024*1024), 2) + " " + Locale.Strings[Locale.MB];
    }
}
