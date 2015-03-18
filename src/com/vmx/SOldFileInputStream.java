package com.vmx;

import java.io.*;
import com.siemens.mp.io.File;

/**
 * Эмулятор входного потока через com.siemens.mp.io.File
 */
public class SOldFileInputStream
       extends InputStream
{
    protected File mf;
    protected int fd;
    protected byte [] onebyte;
    protected int lastMark;
    /**
     * Конструктор
     */
    public SOldFileInputStream (File mf, int fd)
    {
        this.mf = mf;
        this.fd = fd;
        this.lastMark = -1;
        this.onebyte = new byte [1];
    }
    /**
     * Чтение одного байта из потока
     */
    public int read () throws IOException
    {
        if (mf.read (fd, onebyte, 0, 1) > 0)
            return ((int)onebyte [0])&0xFF;
        return -1;
    }
    /**
     * Чтение максимум len байт и запись их по смещению off в b
     */
    public int read (byte [] b, int off, int len) throws IOException
    {
        return mf.read (fd, b, off, len);
    }
    /**
     * Чтение в b, на сколько места хватит
     */
    public int read (byte [] b) throws IOException
    {
        return read (b, 0, b.length);
    }
    /**
     * Здесь mark всегда supported
     */
    public boolean markSupported ()
    {
        return true;
    }
    /**
     * Пометка места чтобы потом вернуться по reset
     */
    public void mark (int readlimit)
    {
        try
        {
            lastMark = mf.seek (fd, 0);
        } catch (IOException iox) { lastMark = -1; }
    }
    /**
     * Возвращение на последнюю метку
     */
    public void reset () throws IOException
    {
        mf.seek (fd, lastMark);
    }
    /**
     * Получить количество доступных байт
     */
    public int available () throws IOException
    {
        int len = mf.length (fd);
        int pos = mf.seek (fd, 0);
        return len-pos;
    }
    /**
     * Пропустить n байт
     */
    public long skip (long n) throws IOException
    {
        int oldpos = mf.seek (fd, 0);
        mf.seek (fd, (int)n);
        return mf.seek (fd, 0) - oldpos;
    }
    /**
     * Закрытие потока
     */
    public void close () throws IOException
    {
        mf.close (fd);
    }
}
