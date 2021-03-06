package filemanager;

import javax.microedition.lcdui.*;

/**
 *
 * @author Dmytro
 */
public class ConfirmDeleteAlert extends Alert implements CommandListener
{
    Command cmdYes = new Command (Locale.Strings[Locale.YES_CMD], Command.CANCEL, 2);
    Command cmdNo = new Command (Locale.Strings[Locale.NO_CMD], Command.OK, 1);
    //public static boolean yes = false;
    Displayable parent;
    String fn;
    boolean tryrec;

    /**
     *
     * @param fileName
     * @param parent
     */
    public ConfirmDeleteAlert (String fileName, Displayable parent)
    {
        super (Locale.Strings[Locale.CONFIRMATION], Locale.Strings[Locale.DEL_SELECTED_FILE] + "?",
               null, AlertType.CONFIRMATION);
        this.parent = parent;
        this.fn = fileName;
        this.tryrec = false;
        addCommand (cmdYes);
        addCommand (cmdNo);
        setCommandListener (this);
    }
    
    /**
     *
     * @param command
     * @param displayable
     */
    public void commandAction (Command command, Displayable displayable)
    {
        if (command == cmdYes)
        {
            if (Filesystem.isDir (fn)) // это папка
            {
                //System.out.println (fn);
                if (tryrec)
                {
                    this.setType (AlertType.INFO);
                    this.setTitle (Locale.Strings[Locale.WAIT]);
                    this.setString (Locale.Strings[Locale.WAIT_PLEASE]);
                    this.setTimeout (Alert.FOREVER);
                    this.removeCommand (cmdYes);
                    this.removeCommand (cmdNo);
                    Main.dsp.setCurrent (this, parent);
                }
                if (Filesystem.deleteFile (fn, tryrec)) // папка удаленa успешно?
                {
                    Main.FileSelect.delete (Main.FileSelect.scrSel);
                    Alert al = new Alert ("", Locale.Strings[Locale.FOLDER_DELETED], null, AlertType.INFO);
                    al.setTimeout (1500);
                    Main.dsp.setCurrent (al, parent);
                }
                else if (Filesystem.isReadOnly (fn)) // потому что ReadOnly
                {
                    Alert al = new Alert (Locale.Strings[Locale.ERROR],
                            Locale.Strings[Locale.READ_ONLY],
                            null, AlertType.ERROR);
                            //images.error, null);
                    al.setTimeout (3000);
                    Main.dsp.setCurrent (al, parent);
                }
                else // папка не пуста
                { 
                    this.setString (Locale.Strings[Locale.FOLDER_NOT_EMPTY]);
                    this.setTimeout (3000);
                    this.tryrec = true;
                    Main.dsp.setCurrent (this, parent);
                }
            }
            else // это файл
            {
                if (Filesystem.isReadOnly (fn)) // потому что ReadOnly
                {
                    Alert al = new Alert (Locale.Strings[Locale.ERROR],
                            Locale.Strings[Locale.READ_ONLY],
                            null, AlertType.ERROR);
                    al.setTimeout (3000);
                    Main.dsp.setCurrent (al, parent);
                }
                else if (Filesystem.deleteFile (fn, false)) // файл удален успешно?
                {
                    Main.FileSelect.delete (Main.FileSelect.scrSel);
                    Alert al = new Alert ("",
                            Locale.Strings[Locale.FILE_DELETED],
                            null, AlertType.INFO);
                    al.setTimeout (1500);
                    Main.dsp.setCurrent (al, parent);
                }
                else // файл не удален неивестно почему
                {
                    Alert al = new Alert (Locale.Strings[Locale.ERROR],
                            Locale.Strings[Locale.FILE_NOT_DELETED],
                            null, AlertType.ERROR);
                    al.setTimeout (3000);
                    Main.dsp.setCurrent (al, parent);
                }
            }
        }
        if (command == cmdNo)
            Main.dsp.setCurrent (parent);
    }
}
