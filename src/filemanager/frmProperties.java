package filemanager; // переведен

import javax.microedition.lcdui.*;
import java.util.*;

public class frmProperties
       extends Form
       implements CommandListener
{
    Command cmdPropOK = new Command (Locale.Strings[Locale.OK_CMD], Command.OK, 1);
    Command cmdPropBack = new Command (Locale.Strings[Locale.CANCEL_CMD], Command.BACK, 1);
    ChoiceGroup cgAttrib;
    Displayable parent;
    boolean hidden, readonly;
    /**
     * Конструктор
     */
    public frmProperties (Displayable parent)
    {
        super (Locale.Strings[Locale.INFORMATION]);
        this.parent = parent;
        cgAttrib = null;
        String tmp = main.currentPath;
        if ("fav:/".equals (tmp))
            tmp = "";
        if (!main.currentFile.equalsIgnoreCase (".."))
            tmp += main.currentFile;
        // Значок файла или папки
        if (filesystem.isDir (tmp))
        {
            if (filesystem.isHidden (tmp))
                this.append (images.getIcon(images.iHiddenFolder));
            else
                this.append (images.getIcon(images.iFolder));
        }
        else
            this.append (images.getIcon(filesystem.fileType (tmp)));
        // Имя
        if (filesystem.isDir (tmp))
        {
            StringItem si = new StringItem ("\n", Locale.Strings[Locale.FOLDER_NAME] + "\n");
            this.append (si);
        }
        else
        {
            StringItem si = new StringItem ("\n", Locale.Strings[Locale.FILE_NAME] + "\n");
            this.append (si);
        }
        StringItem si = new StringItem ("", tmp + "\n");
        si.setFont (Font.getFont (Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_MEDIUM));
        this.append (si);
        // Размер
        long size;
        int zipext;
        if ((zipext = filesystem.divideZipName (tmp)) < 0)
        {
            size = filesystem.getSize (tmp);
            if (size >= 0)
            {
                si = new StringItem (Locale.Strings[Locale.SIZE], filesystem.getSizeString (size));
                si.setFont (Font.getFont (Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_MEDIUM));
                this.append (si);
            }
            // Attributes
            // при выбранном диске b и c свойств ReadOnly и Hidden нет
            if (!main.currentFile.equalsIgnoreCase ("..") &&
                !main.currentPath.startsWith ("b:/") && !main.currentPath.startsWith ("3:/"))
            {
                cgAttrib = new ChoiceGroup (Locale.Strings[Locale.ATTR], ChoiceGroup.MULTIPLE);
                cgAttrib.append ("Read Only", null);
                if (options.showHidden)
                {
                    cgAttrib.append ("Hidden", null);
                    hidden = filesystem.isHidden (tmp);
                    cgAttrib.setSelectedIndex (1, hidden);
                }
                readonly = filesystem.isReadOnly (tmp);
                cgAttrib.setSelectedIndex (0, readonly);
                this.append (cgAttrib);
            }
            // Посл изм.
            si = new StringItem (Locale.Strings[Locale.LAST_MODIF],
                    filesystem.time2String (filesystem.lastModified (tmp)));
            si.setFont (Font.getFont (Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_MEDIUM));
            this.append (si);
        }
        else
        {
            size = filesystem.getInZipSize (tmp.substring (zipext), false);
            long compsize = filesystem.getInZipSize (tmp.substring (zipext), true);
            if (size >= 0)
            {
                si = new StringItem (Locale.Strings[Locale.SIZE], filesystem.getSizeString (size) + "\n");
                si.setFont (Font.getFont (Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_MEDIUM));
                this.append (si);
            }
            if (compsize >= 0)
            {
                si = new StringItem (Locale.Strings[Locale.COMPRESSED_SIZE], filesystem.getSizeString (compsize));
                si.setFont (Font.getFont (Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_MEDIUM));
                this.append (si);
            }
        }
        if (cgAttrib != null)
            this.addCommand (cmdPropOK);
        this.addCommand (cmdPropBack);
        this.setCommandListener (this);
        com.siemens.mp.lcdui.Displayable.setHeadlineIcon(this, images.getIcon(images.iProperties));
    }
    
    public void commandAction (Command command, Displayable displayable)
    {
        // Команда ОК - изменяем свойства файла или папки
        if (command == cmdPropOK)
        {
            if ((readonly != cgAttrib.isSelected (0)) || 
                (options.showHidden) && (hidden != cgAttrib.isSelected (1)))
            {
                filesystem.setReadOnly (main.currentPath + main.currentFile, cgAttrib.isSelected (0));
                if (options.showHidden)
                    filesystem.setHidden (main.currentPath + main.currentFile, cgAttrib.isSelected (1));
                main.FileSelect.updateFileType (main.FileSelect.scrSel);
            }
            main.dsp.setCurrent (parent);
        }
        // НАЗАД - ВЫХОД ИЗ ОКНА СВОЙСТВ
        else if (command == cmdPropBack)
            main.dsp.setCurrent (parent);
    }
}
