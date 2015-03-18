/**************************************\
 * Буферизованный ввод/вывод          *
 **                                  **
 * Буферизованный поток ввода         *
 * (c) 2005+, Vitali Filippov [VMX]   *
 *                                    *
 *            BufDataInputStream.java *
 *      Created on 24 Oct 2005, 13:25 *
\**************************************/

package com.vmx;

import java.io.*;
import javax.microedition.io.*;

/**
 * Class for buffered data input
 * For documentation, see java.io.DataInput
 */
public class BufDataInputStream extends InputStream implements DataInput
{
    protected byte buffer [];
    protected int bmax, blen, bpos;
    protected int bstreampos;
    protected int markedPos;
    protected int capacity, is_available;
    protected boolean markSupp;
    protected InputStream is;
    /**
     * Конструктор
     */
    public BufDataInputStream (int bufsize, InputStream iis) throws IOException
    {
        if (bufsize <= 0)
            throw new IOException ("Buffer size must be greater than 0");
        bmax = bufsize;
        bpos = blen = 0;
        bstreampos = 0;
        buffer = new byte [bmax];
        is = iis;
        capacity = is_available = is.available ();
        if (markSupp = is.markSupported ())
            is.mark (capacity + 0x100);
        markedPos = -1;
    }
    /**
     * Закрытие буферизованного потока вместе с тем, на котором
     * он основан.
     */
    public void close () throws IOException
    {
        is.close ();
    }
    /**
     * Возвращает количество байт, которые ещё возможно прочесть из
     * этого буферизованного потока.
     */
    public int available () throws IOException
    {
        return blen-bpos + is_available;
    }
    /**
     * Получить объём потока
     */
    public int getCapacity () throws IOException
    {
        return capacity;
    }
    /**
     * Перейти к положению pos
     */
    public void seek (int pos) throws IOException
    {
        //System.out.println (bstreampos + " <= " + pos + " < " + (bstreampos+blen));
        if (pos >= bstreampos && pos < bstreampos+blen)
            bpos = pos-bstreampos;
        else
        {
            is.reset ();
            is.skip (pos);
            bufferize ();
        }
    }
    /**
     * Возвращает текущую позицию в буферизованном потоке.
     */
    public int tell () throws IOException
    {
        return capacity-available();
    }
    /**
     * Ставит метку, на которую возвращаться потом можно по reset.
     */
    public void mark (int readlimit)
    {
        try
        {
            markedPos = tell();
        } catch (IOException ix) { markedPos = -1; }
    }
    /**
     * Возвращает, доступны ли функции mark() и reset()?
     */
    public boolean markSupported ()
    {
        return is.markSupported ();
    }
    /**
     * Перейти на последнюю заданную mark'ом позицию.
     */
    public void reset () throws IOException
    {
        if (markedPos >= 0)
            seek (markedPos);
        else
            throw new IOException ("call mark() before reset()");
    }
    /**
     * пропустить n байт
     */
    public long skip (long n) throws IOException
    {
        if (n < blen-bpos)
        {
            bpos += n;
            return n;
        }
        else
        {
            long act = blen-bpos;
            n -= blen-bpos;
            act += is.skip (n);
            bufferize ();
            return act;
        }
    }
    /**
     * Прочитать массив из потока: прочитать и записать максимум len байт,
     * записать их в b[], начиная со смещения off, и вернуть количество
     * считанных байт.
     */
    public int read (byte [] b, int off, int len) throws IOException
    {
        int rest = len, pos = off, n;
        while (rest > 0)
        {
            if (bpos >= blen && bufferize() <= 0) // данные кончились?
            {
                blen = 0;
                break;
            } // если буфер кончился, а bufferize заполнило его чем-то - будет ок
            n = rest;
            if (n > blen-bpos)
                n = blen-bpos;
            System.arraycopy (buffer, bpos, b, pos, n);
            pos += n;
            bpos += n;
            rest -= n;
        }
        return len-rest;
    }
    /**
     * Прочитать массив b[] полностью - эквивалентно read (b, 0, b.length);
     */
    public int read (byte [] b) throws IOException
    {
        return read (b, 0, b.length);
    }
    /**
     * Прочитать 1 байт из потока, вернуть его, если успешно, и -1,
     * если достигнут конец потока.
     */
    public int read () throws IOException
    {
        if (bpos >= blen && bufferize() <= 0) // данные кончились?
            return -1;
      	return ((int)buffer[bpos++])&0xFF;
    }
    /**
     * Прочитать 1 байт из потока назад, если начало файла - вернуть -1.
     * Для работы требуется поддержка mark() и reset()
     */
    public int readBack () throws IOException
    {
        if (bpos == 0)
        {
            if (available () == capacity || !markSupp)
                return -1;
            int old = tell ();
            bstreampos -= bmax;
            if (bstreampos < 0)
                bstreampos = 0;
            is.reset ();
            is.skip (bstreampos);
            bufferize ();
            bpos = old-bstreampos;
        }
        return ((int)buffer[--bpos])&0xFF;
    }
    /**
     * Если буфер не полон, дочитать и дополнить его.
     */
    public void flush () throws IOException
    {
        if (is_available > 0)
        {
            if (bpos != 0 && bpos != blen)
                System.arraycopy (buffer, bpos, buffer, 0, blen-bpos);
            blen -= bpos;
            bstreampos += bpos;
            bpos = 0;
            int blenp = is.read (buffer, blen, bmax-blen);
            blen += blenp;
            is_available -= blenp;
        }
    }
    /**
     * Убивает текущий буфер и буферизует с текущего положения InputStream'а
     */
    protected int bufferize () throws IOException
    {
        is_available = is.available ();
        bstreampos = capacity - is_available;
        blen = bpos = 0;
        if (is_available > 0)
        {
            blen = is.read (buffer, 0, bmax);
            is_available -= blen;
        }
        return blen;
    }
    /**
     * Обновить содержимое буфера в соответствии с потоком
     */
    public void updateBuffer () throws IOException
    {
        is.reset ();
        is.skip (bstreampos);
        bufferize ();
    }
    /**
     * Прочитать булево значение из потока (см. DataInput)
     */
    public boolean readBoolean () throws IOException
    {
        int r = read ();
        if (r == -1)
            throw new IOException ("EOF");
        return r != 0;
    }
    /**
     * Прочитать байт из потока; если достигнут конец потока,
     * генерируется исключение IOException с сообщением "EOF" (см. DataInput)
     */
    public byte readByte () throws IOException
    {
        int r = read ();
        if (r == -1)
            throw new IOException ("EOF");
        return (byte)r;
    }
    /**
     * Прочитать символ (Unicode Big Endian) из потока (см. DataInput)
     */
    public char readChar () throws IOException
    {
        return (char)((readUnsignedByte() << 8)|readUnsignedByte ());
    }
    /**
     * Прочитать число с плавающей точкой двойной точности (см. DataInput)
     */
    public double readDouble () throws IOException
    {
        return Double.longBitsToDouble (readLong ());
    }
    /**
     * Прочитать число с плавающей точкой одинарной точности (см. DataInput)
     */
    public float readFloat () throws IOException
    {
        return Float.intBitsToFloat (readInt());
    }
    /**
     * Прочитать массив b[] из потока целиком, если целиком не получится, 
     * сгенерировать исключение IOException с сообщением "EOF"
     */
    public void readFully (byte [] b) throws IOException
    {
        if (read (b) < b.length)
            throw new IOException ("EOF");
    }
    /**
     * Прочитать в точности len байт и записать их в массив b[], начиная
     * со смещения off. Если достигнут конец файла - сгенерировать
     * исключение IOException с сообщением "EOF"
     */
    public void readFully (byte [] b, int off, int len) throws IOException
    {
        if (read (b, off, len) < len)
            throw new IOException ("EOF");
    }
    /**
     * Прочитать из потока целое число (см. DataInput)
     */
    public int readInt () throws IOException
    {
        return (readUnsignedByte () << 24) |
               (readUnsignedByte () << 16) |
               (readUnsignedByte () << 8) |
               (readUnsignedByte ());
    }
    /**
     * Прочитать из потока длинное целое число (см. DataInput)
     */
    public long readLong () throws IOException
    {
        byte bb [] = new byte [8];
        readFully (bb);
        return (bb [0] << 24) |
               (bb [1] << 16) |
               (bb [2] << 8) |
               (bb [3]);
    }
    /**
     * Прочитать из потока короткое целое число (см. DataInput)
     */
    public short readShort () throws IOException
    {
        return (short)((readUnsignedByte ()<<8)|readUnsignedByte ());
    }
    /**
     * Прочитать из потока беззнаковый байт (см. DataInput)
     */
    public int readUnsignedByte () throws IOException
    {
        return ((int)readByte ())&0xFF;
    }
    /**
     * Прочитать из потока беззнаковое короткое целое (см. DataInput)
     */
    public int readUnsignedShort () throws IOException
    {
        return ((int)readShort ())&0xFFFF;
    }
    /**
     * Пропустить len байт (см. DataInput)
     */
    public int skipBytes (int len) throws IOException
    {
        return (int) skip(len);
    }
    /**
     * Прочитать из потока строку в UTF-8 в соответствии со
     * спецификацией в DataInput (см. DataInput)
     */
    public String readUTF () throws IOException, UTFDataFormatException
    {
        String s = "";
        int n = readUnsignedShort ();
        byte b [] = new byte [n];
        readFully (b);
        return new String (b, 0, b.length, "UTF-8");
    }
    /** Прочитать из потока символ в кодировке UTF-8 */
    public char readCharUTF () throws IOException, UTFDataFormatException
    {
        int b, c, d;
        b = read ();
        if (b == -1)
            return (char)-1;
        if ((b & 0x80) == 0)
            return (char)b;
        else if ((b & 0xE0) == 0xC0)
        {
            c = read ();
            if ((c & 0xC0) != 0x80)
                throw new UTFDataFormatException ();
            return (char)(((b&0x1F) << 6) | (c&0x3F));
        }
        else if ((b & 0xF0) == 0xE0)
        {
            c = read (); d = read ();
            if ((c & 0xC0) != 0x80 || (d & 0xC0) != 0x80)
                throw new UTFDataFormatException ();
            return (char)(((b&0x0F) << 12) | ((c&0x3F)<<6) | (d&0x3F));
        }
        throw new UTFDataFormatException ();
    }
    /** Прочитать из потока символ в кодировке UTF-8 НАЗАД */
    public char readCharBackUTF () throws IOException, UTFDataFormatException
    {
        int b, c, d;
        d = readBack ();
        c = readBack ();
        b = readBack ();
        if (d == -1)
            return (char)-1;
        if ((d & 0x80) == 0)
        {
            read (); read ();
            return (char)d;
        }
        else if ((c & 0xE0) == 0xC0 && (d & 0xC0) == 0x80)
        {
            read ();
            return (char)(((c&0x1F) << 6) | (d&0x3F));
        }
        else if ((b & 0xF0) == 0xE0 && (c & 0xC0) == 0x80 && (d & 0xC0) == 0x80)
            return (char)(((b&0x0F) << 12) | ((c&0x3F)<<6) | (d&0x3F));
        throw new UTFDataFormatException ();
    }
    /**
     * Прочитать из потока максимум count символов в кодировке UTF-8
     */
    public String readUTF (int count) throws IOException, UTFDataFormatException
    {
        String s = "";
        int i = 0;
        int b = 0, c = 0, d = 0;
        while (i < count && available() > 0)
        {
            s += readCharUTF ();
            i++;
        }
        return s;
    }
    /**
     * Пропустить в потоке максимум count символов в кодировке UTF-8.
     * Не очень чётко проверяет соответствие данных кодировке.
     */
    public int skipUTF (int count) throws IOException, UTFDataFormatException
    {
    	byte b;
        int i = 0, r = 0;
        while (i < count)
        {
            b = readByte ();
            if ((b & 0x80) == 0) r++;
            else if ((((int)b) & 0xE0) == 0xC0) { readByte (); r += 2; }
            else if ((((int)b) & 0xF0) == 0xE0) { readShort (); r += 3; }
            else throw new UTFDataFormatException ();
            i++;
        }
        return r;
    }
    /**
     * Проверить наличие BOM UTF-8 сигнатуры
     */
    public boolean checkBOM ()
    {
        try
        {
            if (available () < 3 ||
                read () != 0xEF ||
                read () != 0xBB || 
                read () != 0xBF)
                return false;
        } catch (IOException iox) { return false; }
        return true;
    }
}
