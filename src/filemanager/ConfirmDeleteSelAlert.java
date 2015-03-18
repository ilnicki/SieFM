package filemanager;

import javax.microedition.lcdui.*;

/**
 *
 * @author Dmytro
 */
public class ConfirmDeleteSelAlert
        extends Alert
        implements CommandListener
{

    Command cmdYes = new Command(Locale.Strings[Locale.YES_CMD], Command.CANCEL, 2);
    Command cmdNo = new Command(Locale.Strings[Locale.NO_CMD], Command.OK, 1);
    Displayable parent;
    String fileFullName, fileOnlyName;
    String errors = "";

    /**
     *
     * @param parent
     */
    public ConfirmDeleteSelAlert(Displayable parent)
    {
        super(Locale.Strings[Locale.CONFIRMATION], Locale.Strings[Locale.DEL_MARKED_FILES],
                null, AlertType.CONFIRMATION);
        //images.question, null);
        this.parent = parent;
        addCommand(cmdYes);
        addCommand(cmdNo);
        setCommandListener(this);
    }

    /**
     *
     * @param command
     * @param displayable
     */
    public void commandAction(Command command, Displayable displayable)
    {
        if (command == cmdYes)
        {
            for (int i = 1; i < Main.FileSelect.files.length; i++) // это файл
            {
                if (Main.FileSelect.marked[i])
                {
                    fileFullName = Main.currentPath + Main.FileSelect.files[i];
                    fileOnlyName = fileFullName.substring(fileFullName.lastIndexOf('/') + 1);
                    if (Filesystem.isReadOnly(fileFullName)) // потому что ReadOnly
                        errors = errors + Locale.Strings[Locale.FILE] + fileOnlyName + Locale.Strings[Locale.READ_ONLY] + "\n";
                    else if (!Filesystem.deleteFile(fileFullName, true)) // файл удален НЕуспешно?
                        errors = errors + Locale.Strings[Locale.FILE] + fileOnlyName + Locale.Strings[Locale.FILE_NOT_DELETED] + "\n";
                }
            }
            Main.dsp.setCurrent(new MessageAlert(errors));
        } else if (command == cmdNo)
            Main.dsp.setCurrent(parent);
    }
}
