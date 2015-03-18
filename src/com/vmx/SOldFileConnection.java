package com.vmx;

import com.siemens.mp.io.File;
import javax.microedition.io.file.*;
import java.io.*;
import java.util.Enumeration;

public class SOldFileConnection
        implements FileConnection
{
    protected File mf;
    protected String filename;
    /**
     * Конструктор. Открывает файл с именем filename. 
     *
     * filename вида "file:///0:/Misc/file.txt"
     */
    public SOldFileConnection (String URL) throws IOException
    {
        if (!URL.startsWith ("file:///"))
            throw new IOException ("Unknown schema");
        mf = new File ();
        this.filename = URL.substring (8);
    }
    /**
     * Открыть входной поток
     */
    public InputStream openInputStream () throws IOException
    {
        if (mf == null)
            return null;
        return new SOldFileInputStream (mf, mf.open (filename));
    }
    /**
     * Закрыть соединение наффик
     */
    public void close ()
    {
        mf = null;
    }

    public boolean isOpen() {
        return false;
    }

    public DataInputStream openDataInputStream() throws IOException {
        return null;
    }

    public OutputStream openOutputStream() throws IOException {
        return null;
    }

    public DataOutputStream openDataOutputStream() throws IOException {
        return null;
    }

    public OutputStream openOutputStream(long l) throws IOException {
        return null;
    }

    public long totalSize() {
        return -1;
    }

    public long availableSize() {
        return -1;
    }

    public long usedSize() {
        return -1;
    }

    public long directorySize(boolean flag) throws IOException {
        return -1;
    }

    public long fileSize() throws IOException {
        return -1;
    }

    public boolean canRead() {
        return false;
    }

    public boolean canWrite() {
        return false;
    }

    public boolean isHidden() {
        return false;
    }

    public void setReadable(boolean flag) throws IOException {
    }

    public void setWritable(boolean flag) throws IOException {
    }

    public void setHidden(boolean flag) throws IOException {
    }

    public Enumeration list() throws IOException {
        return null;
    }

    public Enumeration list(String s, boolean flag) throws IOException {
        return null;
    }

    public void create() throws IOException {
    }

    public void mkdir() throws IOException {
    }

    public boolean exists() {
        return false;
    }

    public boolean isDirectory() {
        return false;
    }

    public void delete() throws IOException {
    }

    public void rename(String s) throws IOException {
    }

    public void truncate(long l) throws IOException {
    }

    public void setFileConnection(String s) throws IOException {
    }

    public String getName() {
        return null;
    }

    public String getPath() {
        return null;
    }

    public String getURL() {
        return null;
    }

    public long lastModified() {
        return -1;
    }
}
