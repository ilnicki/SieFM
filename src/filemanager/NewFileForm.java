package filemanager;

import javax.microedition.lcdui.*;

/**
 *
 * @author Dmytro
 */
public class NewFileForm extends Form implements CommandListener
{

    Displayable parent;
    TextField tf;
    ChoiceGroup cg;
    String fileName;

    /**
     *
     * @param parent
     */
    public NewFileForm(Displayable parent)
    {
        super(Locale.Strings[Locale.CREATE_NEW_FILE]);
        this.parent = parent;
        tf = new TextField(Locale.Strings[Locale.NAME], Filesystem.time2fileName(), 256, TextField.ANY);
        cg = new ChoiceGroup(Locale.Strings[Locale.FILE_TYPE], Choice.EXCLUSIVE);
        cg.append("*.txt", Images.getIcon(Images.iText));
        cg.append("*.txt (Win)", Images.getIcon(Images.iText));
        cg.append("*.tmo", Images.getIcon(Images.iTMO));
        this.append(tf);
        this.append(cg);
        setCommandListener(this);
        addCommand(new Command(Locale.Strings[Locale.CANCEL_CMD], Command.BACK, 1));
        addCommand(new Command(Locale.Strings[Locale.OK_CMD], Command.OK, 1));
        com.siemens.mp.lcdui.Displayable.setHeadlineIcon(this, Images.getIcon(Images.iFile));
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
            fileName = tf.getString();
            switch (cg.getSelectedIndex())
            {
                case 0:
                case 1:
                    fileName = Main.currentPath + fileName + ".txt";
                    break;
                case 2:
                    fileName = Main.currentPath + fileName + ".tmo";
                    break;
            }
            if (Filesystem.isFileExist(fileName)) // уже существует
            {
                Alert al = new Alert("", Locale.Strings[Locale.FILE_NAME_EXIST],
                        null, AlertType.ERROR);
                //images.error, null);
                al.setTimeout(3000);
                Main.dsp.setCurrent(al, parent);
            } else
            {
                switch (cg.getSelectedIndex())
                {
                    case 0: // текстовый файл UTF
                    case 1: // текстовый файл Win
                        Filesystem.makeNewFile(fileName, "", cg.getSelectedIndex() == 0);
                        Main.dsp.setCurrent(Main.textEditor);
                        Main.textEditor.rescanAfterExit = true;
                        Main.textEditor.openFile(fileName);
                        Main.textEditor.editText(Main.textEditor.scrStart, Main.textEditor.scrEnd);
                        break;
                    case 2: // текстовый файл tmo
                        Filesystem.makeNewFile(fileName, "\0\0\0\0", false);
                        Main.dsp.setCurrent(new TmoEditTextBox(fileName, true, false, parent));
                        break;
                } // switch
            }
        } // if
    }
}
