package filemanager;

import javax.microedition.lcdui.*;

/**
 *
 * @author Dmytro
 */
public class PropertiesForm
        extends Form
        implements CommandListener
{

    Command cmdPropOK = new Command(Locale.Strings[Locale.OK_CMD], Command.OK, 1);
    Command cmdPropBack = new Command(Locale.Strings[Locale.CANCEL_CMD], Command.BACK, 1);
    ChoiceGroup cgAttrib;
    Displayable parent;
    boolean hidden, readonly;

    /**
     * Конструктор.
     *
     * @param parent
     */
    public PropertiesForm(Displayable parent)
    {
        super(Locale.Strings[Locale.INFORMATION]);
        this.parent = parent;
        cgAttrib = null;
        String tmp = Main.currentPath;
        if ("fav:/".equals(tmp))
            tmp = "";
        if (!Main.currentFile.equalsIgnoreCase(".."))
            tmp += Main.currentFile;
        // Значок файла или папки
        if (Filesystem.isDir(tmp))
        {
            if (Filesystem.isHidden(tmp))
                this.append(Images.getIcon(Images.iHiddenFolder));
            else
                this.append(Images.getIcon(Images.iFolder));
        } else
            this.append(Images.getIcon(Filesystem.fileType(tmp)));
        // Имя
        if (Filesystem.isDir(tmp))
        {
            StringItem si = new StringItem("\n", Locale.Strings[Locale.FOLDER_NAME] + "\n");
            this.append(si);
        } else
        {
            StringItem si = new StringItem("\n", Locale.Strings[Locale.FILE_NAME] + "\n");
            this.append(si);
        }
        StringItem si = new StringItem("", tmp + "\n");
        si.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_MEDIUM));
        this.append(si);
        // Размер
        long size;
        int zipext;
        if ((zipext = Filesystem.divideZipName(tmp)) < 0)
        {
            size = Filesystem.getSize(tmp);
            if (size >= 0)
            {
                si = new StringItem(Locale.Strings[Locale.SIZE], Filesystem.getSizeString(size));
                si.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_MEDIUM));
                this.append(si);
            }
            // Attributes
            // при выбранном диске b и c свойств ReadOnly и Hidden нет
            if (!Main.currentFile.equalsIgnoreCase("..")
                    && !Main.currentPath.startsWith("b:/") && !Main.currentPath.startsWith("3:/"))
            {
                cgAttrib = new ChoiceGroup(Locale.Strings[Locale.ATTR], ChoiceGroup.MULTIPLE);
                cgAttrib.append("Read Only", null);
                if (Options.showHidden)
                {
                    cgAttrib.append("Hidden", null);
                    hidden = Filesystem.isHidden(tmp);
                    cgAttrib.setSelectedIndex(1, hidden);
                }
                readonly = Filesystem.isReadOnly(tmp);
                cgAttrib.setSelectedIndex(0, readonly);
                this.append(cgAttrib);
            }
            // Посл изм.
            si = new StringItem(Locale.Strings[Locale.LAST_MODIF],
                    Filesystem.time2String(Filesystem.lastModified(tmp)));
            si.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_MEDIUM));
            this.append(si);
        } else
        {
            size = Filesystem.getInZipSize(tmp.substring(zipext), false);
            long compsize = Filesystem.getInZipSize(tmp.substring(zipext), true);
            if (size >= 0)
            {
                si = new StringItem(Locale.Strings[Locale.SIZE], Filesystem.getSizeString(size) + "\n");
                si.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_MEDIUM));
                this.append(si);
            }
            if (compsize >= 0)
            {
                si = new StringItem(Locale.Strings[Locale.COMPRESSED_SIZE], Filesystem.getSizeString(compsize));
                si.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_MEDIUM));
                this.append(si);
            }
        }
        if (cgAttrib != null)
            this.addCommand(cmdPropOK);
        this.addCommand(cmdPropBack);
        this.setCommandListener(this);
        com.siemens.mp.lcdui.Displayable.setHeadlineIcon(this, Images.getIcon(Images.iProperties));
    }

    /**
     *
     * @param command
     * @param displayable
     */
    public void commandAction(Command command, Displayable displayable)
    {
        // Команда ОК - изменяем свойства файла или папки
        if (command == cmdPropOK)
        {
            if ((readonly != cgAttrib.isSelected(0))
                    || (Options.showHidden) && (hidden != cgAttrib.isSelected(1)))
            {
                Filesystem.setReadOnly(Main.currentPath + Main.currentFile, cgAttrib.isSelected(0));
                if (Options.showHidden)
                    Filesystem.setHidden(Main.currentPath + Main.currentFile, cgAttrib.isSelected(1));
                Main.FileSelect.updateFileType(Main.FileSelect.scrSel);
            }
            Main.dsp.setCurrent(parent);
        } // НАЗАД - ВЫХОД ИЗ ОКНА СВОЙСТВ
        else if (command == cmdPropBack)
            Main.dsp.setCurrent(parent);
    }
}
