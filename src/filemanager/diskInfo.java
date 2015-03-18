package filemanager;

import javax.bluetooth.LocalDevice;
import javax.microedition.lcdui.*;

public class diskInfo implements CommandListener
{
    Displayable parent;
    Command cmdpropsOK = new Command (Locale.Strings[Locale.OK_CMD], Command.BACK, 1);
    /** Конструктор */
    public diskInfo ()
    {
        parent = null;
    }
    /** Показ информации о диске */
    public void showDiskProperties (Displayable parent)
    {
        this.parent = parent;
        Form frmProps = new Form (Locale.Strings[Locale.DISK_INFO]);

        String disk = "";
        if (!("fav:/".equals (main.currentPath) && "..".equals (main.currentFile)))
        {
            if (main.currentPath != null)
                disk = main.currentPath.substring (0,2);
            else if (!main.currentFile.equals(Locale.Strings[Locale.FAVOURITE]))
                disk = main.currentFile.substring (0,2);
            else return;
        }
        else return;

        if (disk.startsWith ("4:"))
            frmProps.append (images.getIcon (images.iMMC));
        else
            frmProps.append (images.getIcon (images.iDisk));

        StringItem si = new StringItem ("", Locale.Strings[Locale.DISK] + " " + disk + "\n");
        frmProps.append (si);
        long size = filesystem.getDiskSpaceTotal (disk + "/");
        if (size >= 0)
        {
            si = new StringItem (Locale.Strings[Locale.DISK_TOTAL_SIZE], filesystem.getSizeString (size));
            si.setFont (Font.getFont (Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_MEDIUM));
            frmProps.append (si);
        }
        frmProps.append (new Spacer (130, 1));
        size = filesystem.getDiskSpaceAvailable (disk + "/");
        if (size >= 0)
        {
            si = new StringItem (Locale.Strings[Locale.DISK_AVAILABLE], filesystem.getSizeString (size));
            si.setFont (Font.getFont (Font.FACE_SYSTEM, Font.STYLE_BOLD,
                    Font.SIZE_MEDIUM));
            frmProps.append (si);
        }
        frmProps.addCommand (cmdpropsOK);
        frmProps.setCommandListener (this);
        main.dsp.setCurrent (frmProps);
    }
    /** Обработчик команд от формы свойств */
    public void commandAction (Command c, Displayable d)
    {
        main.dsp.setCurrent (parent);
    }
}
