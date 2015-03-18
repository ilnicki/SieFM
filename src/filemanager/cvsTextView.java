package filemanager;

import javax.microedition.lcdui.*;
import java.io.*;
import com.vmx.*;

import javax.microedition.io.*;
import com.siemens.mp.io.file.*;

class FileConnectionCloser implements InputStreamCloser
{
    protected FileConnection fc;
    public FileConnectionCloser (FileConnection fc)
    {
        this.fc = fc;
    }
    public void close ()
    {
        if (fc != null)
        {
            try { fc.close(); }
            catch (Exception x) {}
        }
    }
}

class FileConnectionOutputOpener implements OutputStreamOpener
{
    protected FileConnection fc;
    protected int base;
    public FileConnectionOutputOpener (FileConnection fc, int base)
    {
        if (base < 0) base = 0;
        this.fc = fc;
        this.base = base;
    }
    public InputStream openInputStream ()
    {
        try
        {
            InputStream is = fc.openInputStream ();
            is.skip (base);
            return is;
        } catch (IOException iox) { return null; }
    }
    public OutputStream openOutputStream (int offset)
    {
        try
        {
            OutputStream os = fc.openOutputStream (base+offset);
            return os;
        } catch (IOException iox) { return null; }
    }
    public boolean truncate (int offset)
    {
        try
        {
            fc.truncate (base+offset);
        } catch (IOException iox) { return false; }
        return true;
    }
}

public class cvsTextView extends gkcCanvas implements CommandListener
{
    public Displayable parent;
    // Чтение и запись текста
    BufDataInputStream bdis = null;
    InputStreamDecoder isd = null;
    InputStreamCloser closer = null;
    OutputStreamOpener opener = null;
    static int BUFSIZE = 2048;
    String enc;
    int scrStart, scrEnd;
    // Отрисовка текста и заголовок
    public String caption;
    private int linesPerScreen;
    int w, h;
    Font fntText;
    int fntTextHeight;
    FontWidthCache fch;
    String lines [];
    int lineposes [];
    boolean wordWrapGlobal = true;
    // Полноэкранный режим
    boolean fsmode;
    int header, footer;
    // Правка текста
    int selStart;
    int editStart, editEnd;
    String edittext = null;
    TextBox tb;
    Command cmdCancel = new Command (Locale.Strings[Locale.BACK_CMD], Command.BACK, 2);
    Command cmdClear = new Command (Locale.Strings[Locale.CLEAR_CMD], Command.ITEM, 3);
    Command cmdSave = new Command (Locale.Strings[Locale.SAVE_CMD], Command.ITEM, 1);
    Command cmdYes = new Command (Locale.Strings[Locale.YES_CMD], Command.ITEM, 2);
    Command cmdNo = new Command (Locale.Strings[Locale.NO_CMD], Command.BACK, 1);
    boolean rescanAfterExit;
    /**
     * Конструктор
     */
    public cvsTextView ()
    {
        rescanAfterExit = false;
        setFullScreenMode (true);
        w = getWidth ();
        h = getHeight ();
        fntText = Font.getFont (Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        fntTextHeight = fntText.getHeight ();
        fch = FontWidthCache.getCache (fntText);
        linesPerScreen = (h-30)/fntText.getHeight() - 1;
        lines = new String [linesPerScreen];
        lineposes = new int [linesPerScreen+1];
        selStart = -1;
        // инициализация переменных i/o
        bdis = null;
        isd = null;
        opener = null;
        closer = null;
        // фулл/не фулл скрин
        fsmode = true;
        setFSMode (false);
    }
    /**
     * Установить полноэкранный/не полноэкранный режим
     */
    public void setFSMode (boolean nm)
    {
        if (fsmode != nm)
        {
            fsmode = nm;
            if (!fsmode)
            {
                header = 20;
                footer = 8;
            }
            else
                header = footer = 0;
            linesPerScreen = (h-header-footer-2)/fntText.getHeight() - 1;
            lines = new String [linesPerScreen];
            lineposes = new int [linesPerScreen+1];
            if (bdis != null)
            {
                try
                {
                    bdis.seek (scrStart);
                    readScreen (wordWrapGlobal);
                } catch (IOException x) {}
            }
            repaint ();
        }
    }
    /**
     * Просто открыть входной поток, без дополнительных процедур закрытия
     * и без возможности записи.
     */
    public boolean openStream (InputStream is, String enc)
    {
        return openStream (is, enc, null, null);
    }
    /**
     * Открыть входной поток с использованием дополнительных процедур
     * закрытия потока (например, закрытие соответствующего FileConnection)
     */
    public boolean openStream (InputStream is, String enc, InputStreamCloser cls)
    {
        return openStream (is, enc, cls, null);
    }
    /**
     * Открыть входной поток с использованием дополнительных процедур
     * закрытия потока (например, закрытие соответствующего FileConnection) и
     * с возможностью записи через oso.
     */
    public boolean openStream (InputStream is, String enc, InputStreamCloser cls,
            OutputStreamOpener oso)
    {
        selStart = -1;
        clearScreen ();
        if (bdis != null)
            closeStream ();
        closer = cls;
        opener = oso;
        try
        {
            bdis = new BufDataInputStream (BUFSIZE, is);
            isd = new InputStreamDecoder (bdis, enc);
            this.enc = enc;
            readScreen (wordWrapGlobal);
        }
        catch (Exception iox)
        {
            iox.printStackTrace ();
            return false;
        }
        return true;
    }
    /**
     * Осознать, в какой кодировке is, записать кодировку в enc,
     * позиционировать is на начало текста и вернуть это смещение
     */
    public int detectEncoding (InputStream is) throws IOException
    {
        enc = "windows-1251";
        int off = 0;
        is.mark (10);
        if (is.available () >= 3 &&
            is.read () == 0xEF &&
            is.read () == 0xBB && 
            is.read () == 0xBF) // UTF-8 BOM signature
        {
            enc = "UTF-8";
            off = 3;
        }
        else
            is.reset ();
        return off;
    }
    /**
     * Открыть файл через FileConnection со всеми возможностями
     */
    public void openFile (String filename)
    {
        caption = filename.substring (filename.lastIndexOf ('/')+1);
        try
        {
            int zipext = filesystem.divideZipName (filename);
            if (zipext >= 0)
            {
                InputStream is = filesystem.getZipInputStream (filename.substring (zipext));
                detectEncoding (is);
                openStream (is, enc);
            }
            else
            {
                FileConnection fc = (FileConnection)Connector.open ("file:///" + filename);
                InputStream is = fc.openInputStream ();
                int off = detectEncoding (is);
                openStream (is, enc, new FileConnectionCloser (fc), new FileConnectionOutputOpener (fc, off));
            }
        }
        catch (Exception x)
        {
            if (filesystem.hasFile ())
            {
                try
                {
                    SOldFileConnection sofc = new SOldFileConnection ("file:///" + filename);
                    InputStream is = sofc.openInputStream ();
                    detectEncoding (is);
                    openStream (is, enc);
                }
                catch (Exception iox1)
                {
                    System.out.println ("Exception with message: " + iox1.getMessage ());
                    iox1.printStackTrace ();
                }
            }
            else
            {
                System.out.println ("Exception with message: " + x.getMessage ());
                x.printStackTrace ();
            }
        }
    }
    /**
     * Закрыть входной поток, и если closer != null => вызвать closer.close();
     */
    public void closeStream ()
    {
        if (bdis != null)
        {
            try { bdis.close (); } catch (IOException iox) { iox.printStackTrace (); }
            if (closer != null)
                closer.close ();
            bdis = null;
            isd = null;
        }
    }
    /**
     * Прочитать строку из потока
     */
    public String readString (boolean wordWrap)
    {
        String s = "";
        int maxStrWidth = w-7;
        char c = '0', cmin1 = (char)-1;
        int afterspace = -1, cut = 0;
        int strwidth = 0;
        try
        {
            if (bdis == null || bdis.available () <= 0)
                return null;
            do
            {
                c = isd.readChar ();
                if (c != cmin1 && c != '\r')
                {
                    s += c;
                    strwidth += fch.charWidth (c);
                }
                if ((c == cmin1) || (c == '\n')) // новая строка или конец файла
                {
                    wordWrap = false;
                    break;
                }
                if (c == ' ')
                    afterspace = 0;
                else if (afterspace >= 0)
                    afterspace++;
            } while (strwidth < maxStrWidth);
            if (wordWrap && afterspace >= 0)
                cut += afterspace;
            else if (strwidth > maxStrWidth && c != ' ')
                cut = 1;
            if (cut > 0)
            {
                s = s.substring (0, s.length () - cut);
                if (enc.compareTo ("UTF-8") != 0)
                    bdis.seek (bdis.tell() - cut);
                else
                    for (int i = 0; i < cut; i++)
                        isd.readCharBack ();
            }
        } catch (IOException iox) { return null; }
        return s;
    }
    /**
     * Прочитать строку из потока НАЗАД
     */
    public String readStringBack (boolean wordWrap)
    {
        String s = "";
        int maxStrWidth = w-7;
        char c = '0', cmin1 = (char)-1;
        int afterspace = -1;
        int cut = 0;
        int strwidth = 0;
        try
        {
            if (bdis == null || bdis.tell () <= 0)
                return null;
            do
            {
                c = isd.readCharBack ();
                if (c != cmin1 && c != '\r')
                {
                    s = c + s;
                    strwidth += fch.charWidth (c);
                }
                if ((c == cmin1) || (c == '\n' && s.length () > 1)) // новая строка или начало файла
                {
                    wordWrap = false;
                    break;
                }
                if (c == ' ')
                    afterspace = 1;
                else if (c == '\n')
                    afterspace = 0;
                else if (afterspace >= 0)
                    afterspace++;
            } while (strwidth < maxStrWidth);
            if (wordWrap && afterspace >= 0 && s.length () > afterspace)
                cut += afterspace;
            else if ((strwidth > maxStrWidth && c != ' ') || (c == '\n' && s.length () > 1))
                cut = 1;
            if (cut > 0)
            {
                s = s.substring (cut, s.length ());
                if (enc.compareTo ("UTF-8") != 0)
                    bdis.seek (bdis.tell() + cut);
                else
                    for (int i = 0; i < cut; i++)
                        isd.readChar ();
            }
        } catch (IOException iox) { return null; }
        return s;
    }
    /*
    // Отладочная функция
    void printposes ()
    {
        System.out.print ("lineposes:");
        for (int i = 0; i < linesPerScreen+1; i++)
            System.out.print (" " + lineposes [i]);
        System.out.println ();
    }*/
    /**
     * Прочитать экран из потока
     */
    void readScreen (boolean wordWrap)
    {
        try
        {
            for (int i = 0; i < linesPerScreen; i++)
            {
                lineposes [i] = bdis.tell ();
                lines [i] = readString (wordWrap);
            }
            lineposes [linesPerScreen] = bdis.tell ();
            scrStart = lineposes [0];
            scrEnd = lineposes [linesPerScreen];
        } catch (IOException iox) { iox.printStackTrace (); }
    }
    /**
     * Очистить экран
     */
    void clearScreen ()
    {
        for (int i = 0; i < linesPerScreen; i++)
        {
            lineposes [i] = 0;
            lines [i] = null;
        }
        lineposes [linesPerScreen] = 0;
    }
    /**
     * Перейти строчкой выше
     */
    void lineUp ()
    {
        try
        {
            if (scrStart > 0)
            {
                bdis.seek (scrStart);
                scrEnd = lineposes [linesPerScreen] = lineposes [linesPerScreen-1];
                for (int i = linesPerScreen-1; i > 0; i--)
                {
                    lines [i] = lines [i-1];
                    lineposes [i] = lineposes [i-1];
                }
                lines [0] = readStringBack (wordWrapGlobal);
                scrStart = lineposes [0] = bdis.tell ();
                bdis.seek (scrEnd);
            }
        } catch (IOException iox) { return; }
    }
    /**
     * Перейти строчкой ниже
     */
    void lineDown ()
    {
        try
        {
            if (scrEnd < bdis.getCapacity ())
            {
                scrStart = lineposes [1];
                for (int i = 0; i < linesPerScreen-1; i++)
                {
                    lines [i] = lines [i+1];
                    lineposes [i] = lineposes [i+1];
                }
                lineposes [linesPerScreen-1] = lineposes [linesPerScreen];
                lines [linesPerScreen-1] = readString (wordWrapGlobal);
                lineposes [linesPerScreen] = scrEnd = bdis.tell ();
            }
        } catch (IOException iox) {}
    }
    /**
     * Перейти экраном выше
     */
    void screenUp ()
    {
        try
        {
            bdis.seek (scrStart);
            for (int i = 0; i < linesPerScreen; i++)
                readStringBack (false);
            readScreen (wordWrapGlobal);
        } catch (IOException iox) { return; }
    }
    /**
     * Перейти экраном ниже
     */
    void screenDown ()
    {
        try
        {
            if (scrEnd < bdis.getCapacity ())
                readScreen (wordWrapGlobal);
        } catch (IOException iox) {}
    }
    /**
     * Редактировать кусок текста с start до end
     */
    public void editText (int start, int end)
    {
        editStart = start;
        editEnd = end;
        // Читаем то, что будем редактировать
        try
        {
            bdis.seek (editStart);
            byte [] bs = new byte [editEnd-editStart];
            int rl = bdis.read (bs);
            edittext = StringEncoder.decodeString (bs, 0, rl, enc);
        } catch (Exception iox) { edittext = null; return; }
        int maxsize = BUFSIZE;
        // Запихиваем это в TextBox
        tb = new TextBox (caption, edittext, edittext.length () + BUFSIZE, TextField.ANY);
        tb.addCommand (cmdCancel);
        tb.addCommand (cmdSave);
        tb.addCommand (cmdClear);
        tb.setCommandListener (this);
        tb.setTicker (new Ticker (enc));
        // А TextBox - на экран
        main.dsp.setCurrent (tb);
    }
    /**
     * Сохранить текущий текст из окна редактирования, если оно идёт
     */
    public void saveEditText ()
    {
        if (tb == null)
            return;
        String newtext = tb.getString ();
        int oldlen = 0, newlen = 0;
        try
        {
            oldlen = StringEncoder.getEncodedLength (edittext, enc);
            newlen = StringEncoder.getEncodedLength (newtext, enc);
        } catch (UnsupportedEncodingException x) { return; }
        OutputStream os;
        InputStream is;
        try
        {
            bdis.close ();
            is = opener.openInputStream ();
            int oldsize = is.available ();
            is.mark (is.available () + 0x100);
            is.skip (editStart + oldlen);
            if (newlen < oldlen) // если так, то удаляем кусок потока
            {
                byte [] copybs = new byte [main.COPYBUFSIZE];
                os = opener.openOutputStream (editStart + newlen);
                while (is.available () > 0)
                    os.write (copybs, 0, is.read (copybs));
                os.close ();
                is.close ();
                opener.truncate (oldsize + newlen - oldlen);
            }
            else if (newlen > oldlen) // а если так, то вставляем свободного места
            {
                int copylen = is.available ();
                byte [] copybs;
                // сдвигаем вперёд
                copybs = new byte [main.COPYBUFSIZE];
                int copycnt = (copylen+main.COPYBUFSIZE-1) / main.COPYBUFSIZE;
                int i = copycnt-1, cblen = 0;
                if (copycnt > 0)
                {
                    do
                    {
                        is.reset ();
                        is.skip (editStart + oldlen + i * main.COPYBUFSIZE);
                        cblen = is.read (copybs);
                        os = opener.openOutputStream (editStart + newlen + i * main.COPYBUFSIZE);
                        os.write (copybs, 0, cblen);
                        os.close ();
                        i--;
                    } while (i >= 0);
                }
                is.close ();
            }
            else is.close ();
            // записываем текст
            os = opener.openOutputStream (editStart);
            os.write (StringEncoder.encodeString (newtext, enc));
            os.close ();
            // открываем заново
            int oldstart = editStart;
            bdis = null;
            openStream (opener.openInputStream (), enc, closer, opener);
            bdis.seek (oldstart);
            readScreen (wordWrapGlobal);
        } catch (IOException x) { x.printStackTrace (); return; }
    }
    /**
     * Прервать редактирование, если оно идёт
     */
    public void breakEditing ()
    {
        if (tb == null)
            return;
        main.dsp.setCurrent (this);
        repaint ();
        tb = null;
        edittext = null;
    }
    /**
     * Функция отрисовки
     */
    protected void paint (Graphics g)
    {
        g.setColor (Colors.back);
        g.fillRect (0, 0, w, h);
        // Рисуем сам текст
        g.setColor (Colors.fore);
        g.setFont (fntText);
        for (int i = 0; i < linesPerScreen; i++)
            if (lines [i] != null)
                g.drawString (lines[i], 2, header + 2 + i*(fntTextHeight+1), Graphics.LEFT | Graphics.TOP);
        if (!fsmode)
        {
            // Рисуем заголовок
            //g.setColor (Colors.back);
            //g.fillRect (0, 0, w, header);
            images.drawIcon (g, images.iText, (header-16)/2, (header-16)/2);
            g.setColor (Colors.fore);
            g.drawString (caption, (header+16)/2 + 2, (header-fntTextHeight)/2, Graphics.LEFT | Graphics.TOP);
            g.drawLine (0, header-1, w, header-1); // header = 20; footer = 8;
            g.drawLine (0, h - footer, w, h - footer);
            // Рисуем "подписи" к софт-кнопкам (крестик и "...")
            g.setColor (Colors.fore);
            g.drawLine (w-footer+2, h-footer+2, w-footer+6, h-footer+6);
            g.drawLine (w-footer+6, h-footer+2, w-footer+2, h-footer+6);
            if (opener != null)
                g.drawString ("...", 3, h - fntTextHeight - 1, Graphics.LEFT | Graphics.TOP);
        }
        // Рисуем полосу прокрутки
        g.drawLine (w-3, header, w-3, h-footer);
        int slall = h-header-footer, slpos = 0, slend = 0;
        try
        {
            slpos = header + slall*scrStart/bdis.getCapacity ();
            slend = header + slall*scrEnd/bdis.getCapacity ();
            if (slend > h-footer)
                slend = h-footer;
        } catch (IOException iox) { return; }
        g.drawLine (w-2, slpos, w-2, slend);
        g.drawLine (w-4, slpos, w-4, slend);
        if (selStart >= 0)
        {
            int start = selStart, end = scrEnd;
            if (end < start)
            {
                int t = start;
                start = end;
                end = t;
            }
            try
            {
                slpos = header + slall*start/bdis.getCapacity ();
                slend = header + slall*end/bdis.getCapacity ();
                if (slend > h-footer)
                    slend = h-footer;
            } catch (IOException iox) { return; }
            g.setColor (Colors.fore2);
            g.drawLine (w-2, slpos, w-2, slend);
            g.drawLine (w-4, slpos, w-4, slend);
        }
    }
    /**
     * Функция обработки команд
     */
    public void commandAction (Command c, Displayable d)
    {
        if (c == cmdSave) // сохранение, перезагрузка и выход из редактора
        {
            saveEditText ();
            breakEditing ();
        }
        else if (c == cmdCancel) // выход из редактора без сохранения
            breakEditing ();
        else if (c == cmdClear) // очистка
            tb.setString ("");
    }
    /**
     * Обработчик нажатия клавиши
     */
    protected void keyPressed (int keyCode)
    {
        if (keyCode == KEY_UP || keyCode == KEY_NUM2)
        {
            lineUp ();
            repaint ();
        }
        else if (keyCode == KEY_DOWN || keyCode == KEY_NUM8)
        {
            lineDown ();
            repaint ();
        }
        else if (keyCode == KEY_RIGHT || keyCode == KEY_NUM6)
        {
            screenDown ();
            repaint ();
        }
        else if (keyCode == KEY_LEFT || keyCode == KEY_NUM4)
        {
            screenUp ();
            repaint ();
        }
        else if (keyCode == KEY_STAR)
        {
            if (selStart < 0)
                selStart = scrStart;
            else selStart = -1;
            repaint ();
        }
        else if (keyCode == KEY_POUND)
            setFSMode (!fsmode);
        else if (keyCode == KEY_LSK && opener != null) // редактирование
        {
            if (selStart < 0)
                editText (scrStart, scrEnd);
            else
            {
                int start = selStart, end = scrEnd;
                selStart = -1;
                if (end < start)
                {
                    int t = start;
                    start = end;
                    end = t;
                }
                if (end > start)
                    editText (start, end);
                else
                    repaint ();
            }
        }
        else if (keyCode == KEY_RSK || keyCode == KEY_CANCEL) // выход
        {
            closeStream ();
            if (rescanAfterExit) // если новый файл правили
            {
                cvsWait.start ();
                rescanAfterExit = false;
            }
            else if (parent != null)
            {
                main.dsp.setCurrent (parent);
                parent = null;
            }
            else
                main.dsp.setCurrent (main.FileSelect);
        }
    }
    /**
     * Обработчик повторения (зажатия) клавиши
     */
    protected void keyRepeated (int keyCode)
    {
        if (keyCode == KEY_UP || keyCode == KEY_NUM2)
        {
            lineUp ();
            repaint ();
        }
        else if (keyCode == KEY_DOWN || keyCode == KEY_NUM8)
        {
            lineDown ();
            repaint ();
        }
        else if (keyCode == KEY_RIGHT || keyCode == KEY_NUM6)
        {
            screenDown ();
            repaint ();
        }
        else if (keyCode == KEY_LEFT || keyCode == KEY_NUM4)
        {
            screenUp ();
            repaint ();
        }
    }
}
