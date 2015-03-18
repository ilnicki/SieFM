package com.vmx;

import java.io.UnsupportedEncodingException;

/**
 *
 * @author Dmytro
 */
public class StringEncoder
{

    /**
     * Таблица кодировки "windows-1251"
     */
    protected static char cp1251[] =
    {
        '\u0410', '\u0411', '\u0412', '\u0413', '\u0414', '\u0415', '\u0416',
        '\u0417', '\u0418', '\u0419', '\u041A', '\u041B', '\u041C', '\u041D',
        '\u041E', '\u041F', '\u0420', '\u0421', '\u0422', '\u0423', '\u0424',
        '\u0425', '\u0426', '\u0427', '\u0428', '\u0429', '\u042A', '\u042B',
        '\u042C', '\u042D', '\u042E', '\u042F', '\u0430', '\u0431', '\u0432',
        '\u0433', '\u0434', '\u0435', '\u0436', '\u0437', '\u0438', '\u0439',
        '\u043A', '\u043B', '\u043C', '\u043D', '\u043E', '\u043F', '\u0440',
        '\u0441', '\u0442', '\u0443', '\u0444', '\u0445', '\u0446', '\u0447',
        '\u0448', '\u0449', '\u042A', '\u044B', '\u044C', '\u044D', '\u044E',
        '\u044F'
    };

    /**
     * Конструктор. Пустой.
     */
    public StringEncoder()
    {
    }

    /**
     * Кодировать строку s в кодировку enc.
     *
     * @param s
     * @param enc
     * @return
     * @throws java.io.UnsupportedEncodingException
     */
    public static byte[] encodeString(String s, String enc) throws UnsupportedEncodingException
    {
        byte[] bs;
        try
        {
            bs = s.getBytes(enc);
        } catch (UnsupportedEncodingException x)
        {
            if (enc.compareTo("windows-1251") == 0)
            {
                bs = new byte[s.length()];
                for (int i = 0; i < s.length(); i++)
                    bs[i] = encodeCharCP1251(s.charAt(i));
                return bs;
            }
            throw x;
        }
        return bs;
    }

    /**
     * Получить длину строки s в байтах в кодировке enc.
     *
     * @param s
     * @param enc
     * @return
     * @throws java.io.UnsupportedEncodingException
     */
    public static int getEncodedLength(String s, String enc) throws UnsupportedEncodingException
    {
        byte[] bs;
        try
        {
            bs = s.getBytes(enc);
            return bs.length;
        } catch (UnsupportedEncodingException x)
        {
            if (enc.compareTo("windows-1251") == 0)
                return s.length();
            throw x;
        }
    }

    /**
     * Декодировать участок массива b длиной len со смещения off из кодировки
     * enc.
     *
     * @param bs
     * @param off
     * @param len
     * @param enc
     * @return
     * @throws java.io.UnsupportedEncodingException
     */
    public static String decodeString(byte[] bs, int off, int len, String enc) throws UnsupportedEncodingException
    {
        String s;
        try
        {
            s = new String(bs, off, len, enc);
        } catch (UnsupportedEncodingException x)
        {
            if (enc.compareTo("windows-1251") == 0)
            {
                s = "";
                for (int i = 0; i < len; i++)
                    s += decodeCharCP1251(bs[off + i]);
                return s;
            }
            throw x;
        }
        return s;
    }

    /**
     * Декодировать символ в windows-1251.
     *
     * @param b
     * @return
     */
    public static char decodeCharCP1251(byte b)
    {
        int ich = b & 0xff;
        if (ich == 0xb8) // ё
            return 0x0451;
        else if (ich == 0xa8) // Ё
            return 0x0401;
        else if (ich >= 0xc0)
            return cp1251[ich - 192];
        return (char) ich;
    }

    /**
     * Кодировать символ в windows-1251.
     *
     * @param ch
     * @return
     */
    public static byte encodeCharCP1251(char ch)
    {
        if (ch > 0 && ch < 128)
            return (byte) ch;
        else if (ch == 0x401)
            return -88; // Ё
        else if (ch == 0x404)
            return -86; // Є
        else if (ch == 0x407)
            return -81; // Ї
        else if (ch == 0x451)
            return -72; // ё
        else if (ch == 0x454)
            return -70; // є
        else if (ch == 0x457)
            return -65; // ї
        return (byte) ((byte) (ch) + 176);
    }
}
