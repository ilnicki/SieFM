/*
 * BufDataOutputStream.java
 *
 * Created on 24 Октябрь 2005 г., 14:26
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.vmx;

import java.io.*;
import javax.microedition.io.*;

/**
 *
 * @author Виталий
 */
public class BufDataOutputStream implements DataOutput
{
    final byte [] buffer;
    int bmax, bpos;
    OutputStream os;
    
    /** Creates a new instance of BufDataOutputStream */
    public BufDataOutputStream (int bufsize, OutputStream oos) throws IOException
    {
        if (bufsize <= 0)
            throw new IOException ("Buffer size must be greater than 0");
        bmax = bufsize;
        bpos = 0;
        buffer = new byte [bmax];
        os = oos;
    }
    
    public void close () throws IOException
    {
        flush ();
        os.close ();
    }
    
    public void flush () throws IOException
    {
        os.write (buffer, 0, bpos);
        bpos = 0;
    }
    
    public void write (byte [] b, int off, int len) throws IOException
    {
        int rest = len, n, pos = off;
        while (rest > 0)
        {
            if (bpos >= bmax)
                flush ();
            n = rest;
            if (n > bmax-bpos)
                n = bmax-bpos;
            System.arraycopy (b, pos, buffer, bpos, n);
            pos += n;
            bpos += n;
            rest -= n;
        }
    }
    
    public void write (byte [] b) throws IOException
    {
        write (b, 0, b.length);
    }
    
    public void write (int b) throws IOException
    {
        if (bpos >= bmax)
            flush ();
        buffer [bpos++] = (byte)b;
    }
    
    public void writeBoolean (boolean b) throws IOException
    {
        byte bb = 0;
        if (b)
            bb = 1;
        write (bb);
    }
    
    public void writeByte (int v) throws IOException
    {
        write (v);
    }
    
    public void writeChar (int c) throws IOException
    {
        write ((c >> 8) & 0xFF);
        write (c & 0xFF);
    }
    
    public void writeChars (String s) throws IOException
    {
        for (int i = 0; i < s.length (); i++)
            writeChar (s.charAt (i));
    }
    
    public void writeDouble (double d) throws IOException
    {
        writeLong (Double.doubleToLongBits (d));
    }
    
    public void writeFloat (float f) throws IOException
    {
        writeInt (Float.floatToIntBits (f));
    }
    
    public void writeInt (int i) throws IOException
    {
        write ((i >> 24)&0xFF);
        write ((i >> 16)&0xFF);
        write ((i >>  8)&0xFF);
        write ((i      )&0xFF);
    }
    
    public void writeLong (long l) throws IOException
    {
        write ((int)((l >> 56)&0xFF));
        write ((int)((l >> 48)&0xFF));
        write ((int)((l >> 40)&0xFF));
        write ((int)((l >> 32)&0xFF));
        write ((int)((l >> 24)&0xFF));
        write ((int)((l >> 16)&0xFF));
        write ((int)((l >>  8)&0xFF));
        write ((int)((l      )&0xFF));
    }
    
    public void writeShort (int s) throws IOException
    {
        write ((s >> 8) & 0xFF);
        write (s & 0xFF);
    }
    
    public void writeUTF (String s, boolean writeLength) throws IOException
    {
        flush ();
        if (!writeLength)
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream ();
            DataOutputStream dos = new DataOutputStream (baos);
            dos.writeUTF (s);
            dos.flush ();
            byte [] bytes = baos.toByteArray ();
            os.write (bytes, 2, bytes.length-2);
            dos.close ();
        }
        else
        {
            DataOutputStream dos = new DataOutputStream (os);
            dos.writeUTF (s);
            dos.flush ();
        }
    }
    
    public void writeUTF (String s) throws IOException
    {
        writeUTF (s, true);
    }
}
