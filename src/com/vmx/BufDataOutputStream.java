package com.vmx;

import java.io.*;

/**
 *
 * @author Dmytro
 */
public class BufDataOutputStream implements DataOutput
{

    final byte[] buffer;
    int bmax, bpos;
    OutputStream os;

    /**
     * Creates a new instance of BufDataOutputStream
     *
     * @param bufsize
     * @param oos
     * @throws java.io.IOException
     */
    public BufDataOutputStream(int bufsize, OutputStream oos) throws IOException
    {
        if (bufsize <= 0)
            throw new IOException("Buffer size must be greater than 0");
        bmax = bufsize;
        bpos = 0;
        buffer = new byte[bmax];
        os = oos;
    }

    /**
     *
     * @throws IOException
     */
    public void close() throws IOException
    {
        flush();
        os.close();
    }

    /**
     *
     * @throws IOException
     */
    public void flush() throws IOException
    {
        os.write(buffer, 0, bpos);
        bpos = 0;
    }

    /**
     *
     * @param b
     * @param off
     * @param len
     * @throws IOException
     */
    public void write(byte[] b, int off, int len) throws IOException
    {
        int rest = len, n, pos = off;
        while (rest > 0)
        {
            if (bpos >= bmax)
                flush();
            n = rest;
            if (n > bmax - bpos)
                n = bmax - bpos;
            System.arraycopy(b, pos, buffer, bpos, n);
            pos += n;
            bpos += n;
            rest -= n;
        }
    }

    /**
     *
     * @param b
     * @throws IOException
     */
    public void write(byte[] b) throws IOException
    {
        write(b, 0, b.length);
    }

    /**
     *
     * @param b
     * @throws IOException
     */
    public void write(int b) throws IOException
    {
        if (bpos >= bmax)
            flush();
        buffer[bpos++] = (byte) b;
    }

    /**
     *
     * @param b
     * @throws IOException
     */
    public void writeBoolean(boolean b) throws IOException
    {
        byte bb = 0;
        if (b)
            bb = 1;
        write(bb);
    }

    /**
     *
     * @param v
     * @throws IOException
     */
    public void writeByte(int v) throws IOException
    {
        write(v);
    }

    /**
     *
     * @param c
     * @throws IOException
     */
    public void writeChar(int c) throws IOException
    {
        write((c >> 8) & 0xFF);
        write(c & 0xFF);
    }

    /**
     *
     * @param s
     * @throws IOException
     */
    public void writeChars(String s) throws IOException
    {
        for (int i = 0; i < s.length(); i++)
            writeChar(s.charAt(i));
    }

    /**
     *
     * @param d
     * @throws IOException
     */
    public void writeDouble(double d) throws IOException
    {
        writeLong(Double.doubleToLongBits(d));
    }

    /**
     *
     * @param f
     * @throws IOException
     */
    public void writeFloat(float f) throws IOException
    {
        writeInt(Float.floatToIntBits(f));
    }

    /**
     *
     * @param i
     * @throws IOException
     */
    public void writeInt(int i) throws IOException
    {
        write((i >> 24) & 0xFF);
        write((i >> 16) & 0xFF);
        write((i >> 8) & 0xFF);
        write((i) & 0xFF);
    }

    /**
     *
     * @param l
     * @throws IOException
     */
    public void writeLong(long l) throws IOException
    {
        write((int) ((l >> 56) & 0xFF));
        write((int) ((l >> 48) & 0xFF));
        write((int) ((l >> 40) & 0xFF));
        write((int) ((l >> 32) & 0xFF));
        write((int) ((l >> 24) & 0xFF));
        write((int) ((l >> 16) & 0xFF));
        write((int) ((l >> 8) & 0xFF));
        write((int) ((l) & 0xFF));
    }

    /**
     *
     * @param s
     * @throws IOException
     */
    public void writeShort(int s) throws IOException
    {
        write((s >> 8) & 0xFF);
        write(s & 0xFF);
    }

    /**
     *
     * @param s
     * @param writeLength
     * @throws IOException
     */
    public void writeUTF(String s, boolean writeLength) throws IOException
    {
        flush();
        if (!writeLength)
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeUTF(s);
            dos.flush();
            byte[] bytes = baos.toByteArray();
            os.write(bytes, 2, bytes.length - 2);
            dos.close();
        } else
        {
            DataOutputStream dos = new DataOutputStream(os);
            dos.writeUTF(s);
            dos.flush();
        }
    }

    /**
     *
     * @param s
     * @throws IOException
     */
    public void writeUTF(String s) throws IOException
    {
        writeUTF(s, true);
    }
}
