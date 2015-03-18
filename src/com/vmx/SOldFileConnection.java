package com.vmx;

import com.siemens.mp.io.File;
import javax.microedition.io.file.*;
import java.io.*;
import java.util.Enumeration;

/**
 *
 * @author Dmytro
 */
public class SOldFileConnection
        implements FileConnection
{

    protected File mf;
    protected String filename;

    /**
     * Конструктор. Открывает файл с именем URL. URK должен начинаться со схемы
     * "file:///".
     *
     * @param URL
     * @throws java.io.IOException
     */
    public SOldFileConnection(String URL) throws IOException
    {
        if (!URL.startsWith("file:///"))
            throw new IOException("Unknown schema");
        mf = new File();
        this.filename = URL.substring(8);
    }

    /**
     * Открыть входной поток.
     *
     * @return
     * @throws java.io.IOException
     */
    public InputStream openInputStream() throws IOException
    {
        if (mf == null)
            return null;
        return new SOldFileInputStream(mf, mf.open(filename));
    }

    /**
     * Закрыть соединение.
     */
    public void close()
    {
        mf = null;
    }

    /**
     * Проверка, открыто ли соединение.
     * 
     * @return
     */
    public boolean isOpen()
    {
        return false;
    }

    /**
     *
     * @return @throws IOException
     */
    public DataInputStream openDataInputStream() throws IOException
    {
        return null;
    }

    /**
     *
     * @return @throws IOException
     */
    public OutputStream openOutputStream() throws IOException
    {
        return null;
    }

    /**
     *
     * @return @throws IOException
     */
    public DataOutputStream openDataOutputStream() throws IOException
    {
        return null;
    }

    /**
     *
     * @param l
     * @return
     * @throws IOException
     */
    public OutputStream openOutputStream(long l) throws IOException
    {
        return null;
    }

    /**
     *
     * @return
     */
    public long totalSize()
    {
        return -1;
    }

    /**
     *
     * @return
     */
    public long availableSize()
    {
        return -1;
    }

    /**
     *
     * @return
     */
    public long usedSize()
    {
        return -1;
    }

    /**
     *
     * @param flag
     * @return
     * @throws IOException
     */
    public long directorySize(boolean flag) throws IOException
    {
        return -1;
    }

    /**
     *
     * @return @throws IOException
     */
    public long fileSize() throws IOException
    {
        return -1;
    }

    /**
     *
     * @return
     */
    public boolean canRead()
    {
        return false;
    }

    /**
     *
     * @return
     */
    public boolean canWrite()
    {
        return false;
    }

    /**
     *
     * @return
     */
    public boolean isHidden()
    {
        return false;
    }

    /**
     *
     * @param flag
     * @throws IOException
     */
    public void setReadable(boolean flag) throws IOException
    {
    }

    /**
     *
     * @param flag
     * @throws IOException
     */
    public void setWritable(boolean flag) throws IOException
    {
    }

    /**
     *
     * @param flag
     * @throws IOException
     */
    public void setHidden(boolean flag) throws IOException
    {
    }

    /**
     *
     * @return @throws IOException
     */
    public Enumeration list() throws IOException
    {
        return null;
    }

    /**
     *
     * @param s
     * @param flag
     * @return
     * @throws IOException
     */
    public Enumeration list(String s, boolean flag) throws IOException
    {
        return null;
    }

    /**
     *
     * @throws IOException
     */
    public void create() throws IOException
    {
    }

    /**
     *
     * @throws IOException
     */
    public void mkdir() throws IOException
    {
    }

    /**
     *
     * @return
     */
    public boolean exists()
    {
        return false;
    }

    /**
     *
     * @return
     */
    public boolean isDirectory()
    {
        return false;
    }

    /**
     *
     * @throws IOException
     */
    public void delete() throws IOException
    {
    }

    /**
     *
     * @param s
     * @throws IOException
     */
    public void rename(String s) throws IOException
    {
    }

    /**
     *
     * @param l
     * @throws IOException
     */
    public void truncate(long l) throws IOException
    {
    }

    /**
     *
     * @param s
     * @throws IOException
     */
    public void setFileConnection(String s) throws IOException
    {
    }

    /**
     *
     * @return
     */
    public String getName()
    {
        return null;
    }

    /**
     *
     * @return
     */
    public String getPath()
    {
        return null;
    }

    /**
     *
     * @return
     */
    public String getURL()
    {
        return null;
    }

    /**
     *
     * @return
     */
    public long lastModified()
    {
        return -1;
    }
}
