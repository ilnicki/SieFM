package filemanager;

import javax.microedition.lcdui.*;

/**
 *
 * @author Dmytro
 */
public class NewFolderForm
        extends Form
        implements CommandListener
{
    Displayable parent;
    TextField tf;
    String FolderName;
    
    /**
     *
     * @param parent
     */
    public NewFolderForm (Displayable parent)
    {
        super (Locale.Strings[Locale.CREATE_NEW_FOLDER]);
        this.parent = parent;
        tf = new TextField (Locale.Strings[Locale.NAME], "", 256, TextField.ANY);
        this.append (tf);
        setCommandListener (this);
        addCommand (new Command (Locale.Strings[Locale.CANCEL_CMD], Command.BACK, 1));
        addCommand (new Command (Locale.Strings[Locale.OK_CMD], Command.OK, 1));
        com.siemens.mp.lcdui.Displayable.setHeadlineIcon (this, Images.getIcon(Images.iFolder));
    }
    
    /**
     *
     * @param command
     * @param displayable
     */
    public void commandAction (Command command, Displayable displayable)
    {
        if (command.getCommandType () == Command.BACK)
            Main.dsp.setCurrent (parent);
        else
        {
            if (command.getCommandType () == Command.OK)
            {
                if (Filesystem.makeNewDir (Main.currentPath + tf.getString () + "/")) // если папка создана
                    WaitCanvas.start ();
                else // не создана
                {
                    if ((Filesystem.isFileExist (Main.currentPath + tf.getString () + "/")) &&
                        (!tf.getString ().equalsIgnoreCase (""))) // такой файл уже есть
                    {
                        Alert al = new Alert (Locale.Strings[Locale.ERROR],
                                Locale.Strings[Locale.NAME_EXIST_SELECT_ANOTHER],
                                null, AlertType.ERROR);
                                //images.error, null);
                        al.setTimeout (3000);
                        Main.dsp.setCurrent (al, this);
                    }
                    else
                    {
                        Alert al = new Alert (Locale.Strings[Locale.ERROR],
                                Locale.Strings[Locale.NOT_CREATE_NEW_FOLDER],
                                null, AlertType.ERROR);
                                //images.error, null);
                        al.setTimeout (3000);
                        Main.dsp.setCurrent (al, parent);
                    }
                }
            }
        }
    }
}
