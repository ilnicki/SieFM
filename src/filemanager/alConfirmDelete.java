package filemanager; // переведен

import javax.microedition.lcdui.*;

public class alConfirmDelete extends Alert implements CommandListener
{
    Command cmdYes = new Command (Locale.Strings[Locale.YES_CMD], Command.CANCEL, 2);
    Command cmdNo = new Command (Locale.Strings[Locale.NO_CMD], Command.OK, 1);
    //public static boolean yes = false;
    Displayable parent;
    String fn;
    boolean tryrec;

    public alConfirmDelete (String fileName, Displayable parent)
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
    
    public void commandAction (Command command, Displayable displayable)
    {
        if (command == cmdYes)
        {
            if (filesystem.isDir (fn)) // это папка
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
                    main.dsp.setCurrent (this, parent);
                }
                if (filesystem.deleteFile (fn, tryrec)) // папка удаленa успешно?
                {
                    main.FileSelect.delete (main.FileSelect.scrSel);
                    Alert al = new Alert ("", Locale.Strings[Locale.FOLDER_DELETED], null, AlertType.INFO);
                    al.setTimeout (1500);
                    main.dsp.setCurrent (al, parent);
                }
                else if (filesystem.isReadOnly (fn)) // потому что ReadOnly
                {
                    Alert al = new Alert (Locale.Strings[Locale.ERROR],
                            Locale.Strings[Locale.READ_ONLY],
                            null, AlertType.ERROR);
                            //images.error, null);
                    al.setTimeout (3000);
                    main.dsp.setCurrent (al, parent);
                }
                else // папка не пуста
                { 
                    this.setString (Locale.Strings[Locale.FOLDER_NOT_EMPTY]);
                    this.setTimeout (3000);
                    this.tryrec = true;
                    main.dsp.setCurrent (this, parent);
                }
            }
            else // это файл
            {
                if (filesystem.isReadOnly (fn)) // потому что ReadOnly
                {
                    Alert al = new Alert (Locale.Strings[Locale.ERROR],
                            Locale.Strings[Locale.READ_ONLY],
                            null, AlertType.ERROR);
                    al.setTimeout (3000);
                    main.dsp.setCurrent (al, parent);
                }
                else if (filesystem.deleteFile (fn, false)) // файл удален успешно?
                {
                    main.FileSelect.delete (main.FileSelect.scrSel);
                    Alert al = new Alert ("",
                            Locale.Strings[Locale.FILE_DELETED],
                            null, AlertType.INFO);
                    al.setTimeout (1500);
                    main.dsp.setCurrent (al, parent);
                }
                else // файл не удален неивестно почему
                {
                    Alert al = new Alert (Locale.Strings[Locale.ERROR],
                            Locale.Strings[Locale.FILE_NOT_DELETED],
                            null, AlertType.ERROR);
                    al.setTimeout (3000);
                    main.dsp.setCurrent (al, parent);
                }
            }
        }
        if (command == cmdNo)
            main.dsp.setCurrent (parent);
    }
}
