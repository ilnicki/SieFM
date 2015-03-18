package filemanager;

import javax.microedition.lcdui.*;

public class frmNewFile extends Form implements CommandListener
{
    Displayable parent;
    TextField tf;
    ChoiceGroup cg;
    String fileName;

    public frmNewFile (Displayable parent)
    {
        super (Locale.Strings[Locale.CREATE_NEW_FILE]);
        this.parent = parent;
        tf = new TextField (Locale.Strings[Locale.NAME], filesystem.time2fileName (), 256, TextField.ANY);
        cg = new ChoiceGroup (Locale.Strings[Locale.FILE_TYPE], Choice.EXCLUSIVE);
        cg.append ("*.txt", images.getIcon(images.iText));
        cg.append ("*.txt (Win)", images.getIcon(images.iText));
        cg.append ("*.tmo", images.getIcon(images.iTMO));
        this.append (tf);
        this.append (cg);
        setCommandListener (this);
        addCommand (new Command (Locale.Strings[Locale.CANCEL_CMD], Command.BACK, 1));
        addCommand (new Command (Locale.Strings[Locale.OK_CMD], Command.OK, 1));
        com.siemens.mp.lcdui.Displayable.setHeadlineIcon (this, images.getIcon (images.iFile));
    }
    
    public void commandAction (Command command, Displayable displayable)
    {
        if (command.getCommandType () == Command.BACK)
            main.dsp.setCurrent (parent);
        else if (command.getCommandType () == Command.OK)
        {
            fileName = tf.getString ();
            switch (cg.getSelectedIndex ())
            {
                case 0: 
                case 1: fileName = main.currentPath + fileName + ".txt"; break;
                case 2: fileName = main.currentPath + fileName + ".tmo"; break;
            }
            if (filesystem.isFileExist (fileName)) // уже существует
            { 
                Alert al = new Alert ("", Locale.Strings[Locale.FILE_NAME_EXIST],
                                      null, AlertType.ERROR);
                                      //images.error, null);
                al.setTimeout (3000);
                main.dsp.setCurrent (al, parent);
            }
            else
            {
                switch (cg.getSelectedIndex ())
                {
                    case 0: // текстовый файл UTF
                    case 1: // текстовый файл Win
                        filesystem.makeNewFile (fileName, "", cg.getSelectedIndex () == 0);
                        main.dsp.setCurrent(main.textEditor);
                        main.textEditor.rescanAfterExit = true;
                        main.textEditor.openFile (fileName);
                        main.textEditor.editText (main.textEditor.scrStart, main.textEditor.scrEnd);
                        break;
                    case 2: // текстовый файл tmo
                        filesystem.makeNewFile (fileName, "\0\0\0\0", false);
                        main.dsp.setCurrent 
                            (new tbTmoEdit (fileName, true, false, parent));
                        break;
                } // switch
            }
        } // if
    }
}
