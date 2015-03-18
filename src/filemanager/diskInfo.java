package filemanager;

import javax.microedition.lcdui.*;

/**
 *
 * @author Dmytro
 */
public class DiskInfo implements CommandListener
{

    Displayable parent;
    Command cmdpropsOK = new Command(Locale.Strings[Locale.OK_CMD], Command.BACK, 1);

    /**
     * Конструктор.
     */
    public DiskInfo()
    {
        parent = null;
    }

    /**
     * Показ информации о диске.
     *
     * @param parent
     */
    public void showDiskProperties(Displayable parent)
    {
        this.parent = parent;
        Form frmProps = new Form(Locale.Strings[Locale.DISK_INFO]);

        String disk = "";
        if (!("fav:/".equals(Main.currentPath) && "..".equals(Main.currentFile)))
        {
            if (Main.currentPath != null)
                disk = Main.currentPath.substring(0, 2);
            else if (!Main.currentFile.equals(Locale.Strings[Locale.FAVOURITE]))
                disk = Main.currentFile.substring(0, 2);
            else
                return;
        } else
            return;

        if (disk.startsWith("4:"))
            frmProps.append(Images.getIcon(Images.iMMC));
        else
            frmProps.append(Images.getIcon(Images.iDisk));

        StringItem si = new StringItem("", Locale.Strings[Locale.DISK] + " " + disk + "\n");
        frmProps.append(si);
        long size = Filesystem.getDiskSpaceTotal(disk + "/");
        if (size >= 0)
        {
            si = new StringItem(Locale.Strings[Locale.DISK_TOTAL_SIZE], Filesystem.getSizeString(size));
            si.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_MEDIUM));
            frmProps.append(si);
        }
        frmProps.append(new Spacer(130, 1));
        size = Filesystem.getDiskSpaceAvailable(disk + "/");
        if (size >= 0)
        {
            si = new StringItem(Locale.Strings[Locale.DISK_AVAILABLE], Filesystem.getSizeString(size));
            si.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD,
                    Font.SIZE_MEDIUM));
            frmProps.append(si);
        }
        frmProps.addCommand(cmdpropsOK);
        frmProps.setCommandListener(this);
        Main.dsp.setCurrent(frmProps);
    }

    /**
     * Обработчик команд от формы свойств.
     *
     * @param c
     * @param d
     */
    public void commandAction(Command c, Displayable d)
    {
        Main.dsp.setCurrent(parent);
    }
}
