package filemanager; // с диском 3 не работает!

import java.io.*;
import javax.microedition.io.*;
import javax.microedition.lcdui.*;

/**
 * Класс для чтения и редактирования TMO файлов.
 *
 * @author Dmytro
 */
public class TmoEditTextBox
        extends TextBox
        implements CommandListener
{

    Displayable parent;
    String filename;
    String savedText = "";
    boolean newfile;
    boolean readOnly;
    Command cmdCancel = new Command(Locale.Strings[Locale.BACK_CMD], Command.BACK, 2);
    Command cmdNo = new Command(Locale.Strings[Locale.NO_CMD], Command.BACK, 1);
    Command cmdYes = new Command(Locale.Strings[Locale.YES_CMD], Command.ITEM, 2);
    Command cmdSave = new Command(Locale.Strings[Locale.SAVE_CMD], Command.ITEM, 1);
    Command cmdClear = new Command(Locale.Strings[Locale.CLEAR_CMD], Command.ITEM, 3);

    /**
     *
     * @param filename
     * @param newfile
     * @param ReadOnly
     * @param parent
     */
    public TmoEditTextBox(String filename, boolean newfile, boolean ReadOnly, Displayable parent)
    {
        super(FileSelectCanvas.getLastPartOfString(filename, 11), "", 400, TextField.ANY);
        this.parent = parent;
        this.newfile = newfile;
        this.readOnly = ReadOnly;
        this.filename = filename;
        if (!newfile) // если файл не новый, читаем его
        {
            savedText = readTMO_UTF(filename);
            this.setString(savedText);
        }
        this.addCommand(cmdCancel);
        this.addCommand(cmdSave);
        this.addCommand(cmdClear);
        this.setCommandListener(this);
    }

    /**
     *
     * @param command Command
     * @param displayable Displayable
     */
    public void commandAction(Command command, Displayable displayable)
    {
        if (command == cmdCancel)
        {
            if (this.getString().compareTo(savedText) == 0 || readOnly) // не изменялось или readonly
                back();
            else
            {
                Alert al = new Alert("?",
                        Locale.Strings[Locale.FILE_NOT_SAVED_EXIT],
                        null, AlertType.WARNING);
                //images.warn, null);
                al.addCommand(cmdYes);
                al.addCommand(cmdNo);
                al.setCommandListener(this);
                Main.dsp.setCurrent(al, this);
            }
        } else if (command == cmdSave)
        {
            if (!readOnly)
            {
                // запись
                saveTMO_UTF(filename, this.getString());
                savedText = this.getString();
                Alert al = new Alert("",
                        Locale.Strings[Locale.SAVED],
                        null, AlertType.INFO);
                //images.ok, null);
                al.setTimeout(1500);
                Main.dsp.setCurrent(al, this);
            } else
            {
                // только для чтения
                Alert al = new Alert(Locale.Strings[Locale.ERROR],
                        Locale.Strings[Locale.FILE_NOT_SAVED],
                        null, AlertType.ERROR);
                //images.error, null);
                al.setTimeout(1500);
                Main.dsp.setCurrent(al, this);
            }
        } // очистка
        else if (command == cmdClear)
            this.setString("");
        // выход
        else if (command == cmdYes)
            back();
        // отмена
        else if (command == cmdNo)
            Main.dsp.setCurrent(this);
    }

    private void back()
    {
        if (newfile) // новый файл перечитываем ФС
            WaitCanvas.start();
        else
            Main.dsp.setCurrent(parent);
    }

    /**
     * Запись TMO файла
     *
     * @param filename String
     * @param str
     */
    public static void saveTMO_UTF(String filename, String str)
    {
        int length = str.length();
        int ksum = length;
        char curr_ch;
        byte byte_1, byte_2;
        Filesystem.deleteFile(filename, false);
        Filesystem.makeNewFile(filename, "", false);
        try
        {
            DataOutputStream dos = Connector.openDataOutputStream("file:///" + filename);
            dos.writeByte(length);
            dos.writeByte(length >> 8);
            for (int i = 0; i < length; i++)
            {
                curr_ch = str.charAt(i); // сохраняем как тмо файл
                byte_1 = (byte) (curr_ch);
                byte_2 = (byte) (curr_ch >> 8);
                ksum = ksum ^ curr_ch;
                dos.writeByte(byte_1);
                dos.writeByte(byte_2);
            }
            dos.writeByte(ksum);
            dos.flush();
            dos.close();
        } catch (IOException ioe)
        {
            //System.out.println ("Error save tmo file");
        }
    }

    /**
     * Чтение TMO файла
     *
     * @param filename String
     * @return String
     */
    public static String readTMO_UTF(String filename)
    {
        StringBuffer str = new StringBuffer();
        char ch;
        byte byte_1, byte_2;
        try
        {
            DataInputStream dis = Connector.openDataInputStream("file:///" + filename);
            int length = (dis.readUnsignedByte() + dis.readUnsignedByte() * 256) * 2;
            for (int i = 0; i < length; i = i + 2)
            {
                byte_1 = dis.readByte();
                byte_2 = dis.readByte();
                ch = (char) ((char) (byte_1) | (char) (byte_2 << 8));
                str.append(ch);
            }
            dis.close();
        } catch (IOException ioe)
        {
            //System.out.println("Error read tmo file");
        } finally
        {
            return str.toString();
        }
    }
}
