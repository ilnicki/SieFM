package filemanager;

import com.vmx.StringEncoder;
import javax.microedition.lcdui.*;
import com.siemens.mp.io.file.*;
import javax.microedition.io.*;
import java.io.*;

/**
 * Форма-редактор ID3v1-тегов MP3
 */
public class id3Editor
       extends Form
       implements CommandListener
{
    protected Displayable parent;
    protected static byte [] tag = null;
    protected int tagAt = 0;
    protected String filename = "";
    protected static String id3v1enc = "windows-1251";
    protected TextField tf1, tf2, tf3, tf4, tf5, tf6;
    /**
     * Конструктор: создать форму для редактирования тегов файла filename
     */
    public id3Editor (String filename, Displayable parent) throws IOException
    {
        super (Locale.Strings[Locale.TAG_EDITOR]);
        this.filename = filename;
        this.parent = parent;
        int songnum = 1;
        InputConnection fc = (InputConnection)Connector.open ("file:///" + filename, Connector.READ);
        InputStream is = fc.openInputStream ();
        if (is.available() > 128)
        {
            tagAt = is.available()-128;
            is.skip (tagAt);
            tag = new byte [128];
            is.read (tag);
            if (tag [0] != 0x54 || tag [1] != 0x41 || tag [2] != 0x47)
            {
                tag = null;
                tagAt += 128;
            }
            else
            {
                songnum = ((int)tag[126])&0xFF;
                for (int i = 3; i < 125; i++)
                    if (tag [i] == 0)
                        tag [i] = 0x20;
            }
        }
        is.close ();
        fc.close ();
        append (tf1 = new TextField (Locale.Strings [Locale.ID3_SONG],    getFromTag (tag,3,30),  30, TextField.ANY));
        append (tf2 = new TextField (Locale.Strings [Locale.ID3_ARTIST],  getFromTag (tag,33,30), 30, TextField.ANY));
        append (tf3 = new TextField (Locale.Strings [Locale.ID3_ALBUM],   getFromTag (tag,63,30), 30, TextField.ANY));
        append (tf4 = new TextField (Locale.Strings [Locale.ID3_YEAR],    getFromTag (tag,93,4),  4, TextField.NUMERIC));
        append (tf6 = new TextField (Locale.Strings [Locale.ID3_TRACKNUM], String.valueOf (songnum), 3, TextField.NUMERIC));
        append (tf5 = new TextField (Locale.Strings [Locale.ID3_COMMENT], getFromTag (tag,97,28), 28, TextField.ANY));
        addCommand (new Command (Locale.Strings [Locale.SAVE_CMD], Command.OK, 1));
        addCommand (new Command (Locale.Strings [Locale.CANCEL_CMD], Command.CANCEL, 2));
        setCommandListener (this);
    }
    /**
     * Взять кусок тега и сделать строкой
     */
    protected static String getFromTag (byte [] tag, int offset, int len)
    {
        if (tag != null)
        {
            int i;
            for (i = offset+len-1; i >= offset; i--)
                if (tag[i] != 0x20 && tag[i] != 0)
                    break;
            try
            {
                String s = StringEncoder.decodeString (tag, offset, i-offset+1, id3v1enc);
                System.out.println (s.length ());
                return s;
            } catch (Exception x) {}
        }
        return "";
    }
    /**
     * Записать строку в кусок тега
     */
    protected static void setToTag (byte [] tag, String s, int offset, int len)
    {
        if (tag != null)
        {
            try
            {
                byte [] bs = StringEncoder.encodeString (s, id3v1enc);
                int i;
                for (i = 0; i < bs.length && i < len; i++)
                    tag [offset+i] = bs [i];
                for (; i < len; i++)
                    tag [offset+i] = 0;
            } catch (Exception x) {}
        }
    }
    /**
     * Обработчик команд
     */
    public void commandAction (Command c, Displayable d)
    {
        int ct = c.getCommandType ();
        if (ct == Command.OK)
        {
            if (tag == null)
                tag = new byte [128];
            tag [0] = 0x54;
            tag [1] = 0x41;
            tag [2] = 0x47;
            tag [0x7f] = 0x14;
            tag [0x7d] = 0;
            setToTag (tag, tf1.getString (), 3, 30);
            setToTag (tag, tf2.getString (), 33, 30);
            setToTag (tag, tf3.getString (), 63, 30);
            setToTag (tag, tf4.getString (), 93, 4);
            setToTag (tag, tf5.getString (), 97, 28);
            tag [0x7e] = (byte)Integer.parseInt(tf6.getString ());
            try
            {
                FileConnection fc = (FileConnection)Connector.open ("file:///" + filename);
                OutputStream os = fc.openOutputStream (tagAt);
                os.write (tag);
                os.close ();
                fc.close ();
                main.dsp.setCurrent (parent);
            }
            catch (Exception x)
            {
                Alert al = new Alert (Locale.Strings[Locale.ERROR],
                        x.getClass().getName() + ": " + x.getMessage (), null, AlertType.ERROR);
                al.setTimeout (3000);
                main.dsp.setCurrent (al, parent);
            }
        }
        else
            main.dsp.setCurrent (parent);
    }
    /**
     * Получить строку теговой информации из mp3-файла
     * (разделена на части символом \n)
     */
    public static String getTagString (String filename) throws IOException
    {
        if (!filename.toLowerCase().endsWith (".mp3"))
            return "";
        InputConnection fc = (InputConnection)Connector.open ("file:///" + filename, Connector.READ);
        InputStream is = fc.openInputStream ();
        int songnum;
        if (is.available() > 128)
        {
            is.skip (is.available()-128);
            byte [] tag = new byte [128];
            is.read (tag);
            is.close ();
            fc.close ();
            if (tag [0] != 0x54 || tag [1] != 0x41 || tag [2] != 0x47)
                tag = null;
            else
            {
                songnum = ((int)tag[126])&0xFF;
                for (int i = 3; i < 125; i++)
                    if (tag [i] == 0)
                        tag [i] = 0x20;
                String s = "", s2;
                if ((s2 = getFromTag (tag, 3, 30)).length () > 0)
                    s += s2;
                if ((s2 = getFromTag (tag, 33, 30)).length () > 0)
                    s += "\n" + s2;
                if ((s2 = getFromTag (tag, 63, 30)).length () > 0)
                    s += "\n" + s2;
                if ((s2 = getFromTag (tag, 93, 4)).length () > 0)
                    s += "\n" + s2;
                s += "\n" + Locale.Strings [Locale.ID3_TRACKNUM] + " " + String.valueOf (songnum);
                if ((s2 = getFromTag (tag, 97, 28)).length () > 0)
                    s += "\n" + s2;
                return s;
            }
        }
        is.close ();
        fc.close ();
        return "";
    }
}
