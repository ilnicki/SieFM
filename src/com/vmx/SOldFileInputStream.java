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
    protected byte[] onebyte;
    protected int lastMark;

    /**
     * Конструктор.
     *
     * @param mf
     * @param fd
     */
    public SOldFileInputStream(File mf, int fd)
    {
        this.mf = mf;
        this.fd = fd;
        this.lastMark = -1;
        this.onebyte = new byte[1];
    }

    /**
     * Чтение одного байта из потока.
     *
     * @return
     * @throws java.io.IOException
     */
    public int read() throws IOException
    {
        if (mf.read(fd, onebyte, 0, 1) > 0)
            return ((int) onebyte[0]) & 0xFF;
        return -1;
    }

    /**
     * Чтение максимум len байт и запись их по смещению off в b.
     *
     * @param b
     * @param off
     * @param len
     * @return
     * @throws java.io.IOException
     */
    public int read(byte[] b, int off, int len) throws IOException
    {
        return mf.read(fd, b, off, len);
    }

    /**
     * Чтение в b, на сколько места хватит.
     *
     * @param b
     * @return
     * @throws java.io.IOException
     */
    public int read(byte[] b) throws IOException
    {
        return read(b, 0, b.length);
    }

    /**
     * Здесь mark всегда supported.
     *
     * @return
     */
    public boolean markSupported()
    {
        return true;
    }

    /**
     * Пометка места чтобы потом вернуться по reset.
     *
     * @param readlimit
     */
    public void mark(int readlimit)
    {
        try
        {
            lastMark = mf.seek(fd, 0);
        } catch (IOException iox)
        {
            lastMark = -1;
        }
    }

    /**
     * Возвращение на последнюю метку.
     *
     * @throws java.io.IOException
     */
    public void reset() throws IOException
    {
        mf.seek(fd, lastMark);
    }

    /**
     * Получить количество доступных байт.
     *
     * @return
     * @throws java.io.IOException
     */
    public int available() throws IOException
    {
        int len = mf.length(fd);
        int pos = mf.seek(fd, 0);
        return len - pos;
    }

    /**
     * Пропустить n байт.
     *
     * @param n
     * @return
     * @throws java.io.IOException
     */
    public long skip(long n) throws IOException
    {
        int oldpos = mf.seek(fd, 0);
        mf.seek(fd, (int) n);
        return mf.seek(fd, 0) - oldpos;
    }

    /**
     * Закрытие потока.
     *
     * @throws java.io.IOException
     */
    public void close() throws IOException
    {
        mf.close(fd);
    }
}
