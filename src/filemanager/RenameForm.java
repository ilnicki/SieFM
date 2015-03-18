package filemanager;

import javax.microedition.lcdui.*;

/**
 *
 * @author Dmytro
 */
public class RenameForm
        extends Form
        implements CommandListener
{

    String oldFileName;
    String newfileName;
    TextField tf;
    Displayable parent;
    boolean isFolder = false;

    /**
     *
     * @param parent
     */
    public RenameForm(Displayable parent)
    {
        super(Locale.Strings[Locale.RENAME]);
        this.parent = parent;
        oldFileName = Main.currentFile;
        if (oldFileName.endsWith("/")) //если выбранный файл - папка, убираем посл/ слеш
        {
            oldFileName = oldFileName.substring(0, oldFileName.length() - 1);
            isFolder = true;
        }
        tf = new TextField(Locale.Strings[Locale.NAME], oldFileName, 256, TextField.ANY);
        this.append(tf);
        setCommandListener(this);
        addCommand(new Command(Locale.Strings[Locale.BACK_CMD], Command.BACK, 1));
        addCommand(new Command(Locale.Strings[Locale.OK_CMD], Command.OK, 1));
        com.siemens.mp.lcdui.Displayable.setHeadlineIcon(this, Images.getIcon(Images.iRename));
    }

    /**
     *
     * @param command
     * @param displayable
     */
    public void commandAction(Command command, Displayable displayable)
    {
        if (command.getCommandType() == Command.BACK)
            Main.dsp.setCurrent(parent);
        else if (command.getCommandType() == Command.OK)
        {
            if (isFolder)
                newfileName = tf.getString() + "/";
            else
                newfileName = tf.getString();
            if ((newfileName.equalsIgnoreCase(oldFileName))
                    || (newfileName.equalsIgnoreCase(oldFileName + "/"))) // имя не изменилось
                Main.dsp.setCurrent(parent);
            else
            {
                if (Filesystem.isFileExist(Main.currentPath + newfileName)
                        && (!oldFileName.equalsIgnoreCase(newfileName)))
                {
                    Alert al = new Alert(Locale.Strings[Locale.ERROR],
                            Locale.Strings[Locale.NAME_EXIST_SELECT_ANOTHER],
                            null, AlertType.ERROR);
                    //images.error, null);
                    al.setTimeout(3000);
                    Main.dsp.setCurrent(al, this);
                } else // переименовываем
                {
                    if (Filesystem.renameFile(Main.currentPath + oldFileName, newfileName)) // если переименован
                    {
                        Main.FileSelect.files[Main.FileSelect.scrSel] = newfileName;
                        // меняем имя и, если не папка, значок
                        if (!Filesystem.isDir(Main.currentPath + newfileName))
                            Main.FileSelect.updateFileType(Main.FileSelect.scrSel);
                        Main.dsp.setCurrent(parent);
                    } else // не переименован
                    {
                        if (Filesystem.isReadOnly(Main.currentPath + Main.currentFile)) // ReadOnly
                        {
                            Alert al = new Alert(Locale.Strings[Locale.ERROR],
                                    Locale.Strings[Locale.READ_ONLY],
                                    null, AlertType.ERROR);
                            //images.error, null);
                            al.setTimeout(3000);
                            Main.dsp.setCurrent(al, parent);
                        } else
                        {
                            Alert al = new Alert(Locale.Strings[Locale.ERROR],
                                    Locale.Strings[Locale.NOT_RENAMED],
                                    null, AlertType.ERROR);
                            //images.error, null);
                            al.setTimeout(3000);
                            Main.dsp.setCurrent(al, parent);
                        }
                    }
                }
            }
        }
    }
}
