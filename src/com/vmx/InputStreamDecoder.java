package com.vmx;

import java.io.*;

public class InputStreamDecoder
{
    int enc;
    BufDataInputStream bdis;
    /** Конструктор */
    public InputStreamDecoder (InputStream is, String enc) throws UnsupportedEncodingException, IOException
    {
        if (enc.compareTo ("UTF-8") == 0)
            this.enc = 1;
        else if (enc.compareTo ("windows-1251") == 0)
            this.enc = 2;
        else
            throw new UnsupportedEncodingException ("Encoding " + enc + " is not supported by InputStreamDecoder");
        bdis = new BufDataInputStream (2048, is);
    }
    /** Конструктор */
    public InputStreamDecoder (BufDataInputStream bdis, String enc) throws UnsupportedEncodingException
    {
        if (enc.compareTo ("UTF-8") == 0)
            this.enc = 1;
        else if (enc.compareTo ("windows-1251") == 0)
            this.enc = 2;
        else
            throw new UnsupportedEncodingException ("Encoding " + enc + " is not supported by InputStreamDecoder");
        this.bdis = bdis;
    }
    /** Считать символ */
    public char readChar () throws IOException
    {
        char c = (char)-1;
        if (bdis.available () > 0)
        {
            if (enc == 2)
            {
                int i = bdis.read();
                if (i > -1)
                    c = StringEncoder.decodeCharCP1251 ((byte)i);
            }
            else if (enc == 1)
                c = bdis.readCharUTF ();
            else
                throw new IOException ("Internal InputStreamDecoder error");
        }
        return c;
    }
    /** Считать строку длиной максимум len */
    public String readChars (int len) throws IOException
    {
        if (bdis.available () <= 0)
            return null;
        if (enc == 1)
            return bdis.readUTF (len);
        else if (enc == 2)
        {
            byte [] bs = new byte [len];
            int rl = bdis.read (bs);
            return StringEncoder.decodeString (bs, 0, rl, "windows-1251");
        }
        throw new IOException ("Internal InputStreamDecoder error");
    }
    /** Считать символ назад */
    public char readCharBack () throws IOException
    {
        if (bdis.tell () <= 0)
            return (char)-1;
        if (enc == 1)
            return bdis.readCharBackUTF ();
        else if (enc == 2)
        {
            int i = bdis.readBack();
            if (i == -1)
                return (char)-1;
            return StringEncoder.decodeCharCP1251 ((byte)i);
        }
        throw new IOException ("Internal InputStreamDecoder error");
    }
    /** Пропустить n символов, возвращает число пропущенных байт */
    public int skipChars (int n) throws IOException
    {
        if (enc == 1)
            return bdis.skipUTF (n);
        else if (enc == 2)
            return bdis.skipBytes (n);
        throw new IOException ("Internal InputStreamDecoder error");
    }
}
