package filemanager; // переведен

import javax.microedition.lcdui.*;

public class alConfirmDeleteSel
        extends Alert
        implements CommandListener
{
    Command cmdYes = new Command (Locale.Strings[Locale.YES_CMD], Command.CANCEL, 2);
    Command cmdNo = new Command (Locale.Strings[Locale.NO_CMD], Command.OK, 1);
    Displayable parent;
    String fileFullName, fileOnlyName;
    String errors = "";
    
    public alConfirmDeleteSel (Displayable parent)
    {
        super (Locale.Strings[Locale.CONFIRMATION], Locale.Strings[Locale.DEL_MARKED_FILES],
               null, AlertType.CONFIRMATION);
                //images.question, null);
        this.parent = parent;
        addCommand (cmdYes);
        addCommand (cmdNo);
        setCommandListener (this);
    }
    
    public void commandAction (Command command, Displayable displayable)
    {
        if (command == cmdYes) 
        {
            for (int i = 1; i < main.FileSelect.files.length; i++) // это файл
            {
                if (main.FileSelect.marked[i])
                {
                    fileFullName = main.currentPath + main.FileSelect.files[i];
                    fileOnlyName = fileFullName.substring (fileFullName.lastIndexOf ('/') + 1);
                    if (filesystem.isReadOnly (fileFullName)) // потому что ReadOnly
                        errors = errors + Locale.Strings[Locale.FILE] + fileOnlyName + Locale.Strings[Locale.READ_ONLY] + "\n";
                    else if (!filesystem.deleteFile (fileFullName, true)) // файл удален Ќ≈успешно?
                        errors = errors + Locale.Strings[Locale.FILE] + fileOnlyName + Locale.Strings[Locale.FILE_NOT_DELETED] + "\n";
                }
            }
            main.dsp.setCurrent (new alMessage (errors));
        }
        else if (command == cmdNo)
            main.dsp.setCurrent (parent);
    }
}
