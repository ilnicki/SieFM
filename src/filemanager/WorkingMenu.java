package filemanager;

import javax.microedition.lcdui.*;
import java.util.Vector;
import com.vmx.*;

/**
 *
 * @author Dmytro
 */
public class WorkingMenu
        implements MenuListener
{

    protected MenuCanvas mn;

    /**
     *
     * @param menu
     */
    public WorkingMenu(MenuCanvas menu)
    {
        mn = menu;
    }

    /**
     * Обработчик для меню.
     *
     * @param string
     */
    public void menuAction(int string)
    {
        if (string < 0)
            return;
        else if (string >= Locale.PANEL_NUMS && string < Locale.PANEL_NUMS + 10)
        {
            Main.FileSelect.changePanel(string - Locale.PANEL_NUMS);
            return;
        }
        int moveit = 0;
        Alert al;
        Vector v = null;
        String s;
        Main.currentFile = Main.FileSelect.files[Main.FileSelect.scrSel];
        if (string == Locale.PROPERTY_CMD && mn.type == 3
                && Main.currentPath.endsWith(":/") && "..".equals(Main.currentFile))
            string = Locale.DISK_INFO_CMD;
        switch (string)
        {
            case Locale.EXIT_CMD: // выход
                Main.midlet.destroyApp(true);
                break;
            case Locale.MARK_CMD: // выделить
                Main.FileSelect.markSelected();
                mn.ret();
                break;
            case Locale.MARK_ALL_CMD: // выделить все
                Main.FileSelect.markAll();
                mn.ret();
                break;
            case Locale.DEMARK_ALL_CMD: // сбросить выделение
                Main.FileSelect.demarkAll();
                mn.ret();
                break;
            case Locale.HELP_CMD: // справка
                Main.textEditor.openStream(Locale.getAboutStream(), "UTF-8");
                Main.textEditor.parent = mn.parent;
                Main.textEditor.caption = Locale.ABOUT_MIDLET_NAME;
                Main.dsp.setCurrent(Main.textEditor);
                break;
            case Locale.TO_FAVOUR_CMD: // папку в избранное
                if ("..".equals(Main.currentFile))
                    Options.addFavorite(Main.currentPath);
                else if (Main.currentPath != null)
                    Options.addFavorite(Main.currentPath + Main.currentFile);
                else
                    Options.addFavorite(Main.currentFile.substring(0, 3));
                Main.dsp.setCurrent(mn.parent);
                break;
            case Locale.DISK_INFO_CMD: // инфо о диске
                Main.diskinfo.showDiskProperties(mn.parent);
                break;
            case Locale.PROPERTY_CMD: // свойства
                Main.dsp.setCurrent(new PropertiesForm(mn.parent));
                break;
            case Locale.BUFFER: // показать буфер обмена
                Main.FileSelect.showBuffer();
                break;
            case Locale.OPEN_CMD: // открыть
                mn.ret();
                Main.FileSelect.selectFile();
                break;
            case Locale.RENAME_CMD: // переименовать
                Main.dsp.setCurrent(new RenameForm(mn.parent));
                break;
            case Locale.DELETE_CMD: // удалить выбранный/выделенные
                if ("fav:/".equals(Main.currentPath)) // удаляем из избранного
                {
                    if (Main.FileSelect.markMode)
                    {
                        for (int i = 1; i < Main.FileSelect.files.length; i++)
                            if (Main.FileSelect.marked[i])
                                Options.deleteFavorite(Main.FileSelect.files[i]);
                    } else if (Main.FileSelect.scrSel > 0)
                        Options.deleteFavorite(Main.FileSelect.files[Main.FileSelect.scrSel]);
                    WaitCanvas.start();
                } else if ("buf:/".equals(Main.currentPath)) // удаляем из буфера
                {
                    if (Main.FileSelect.markMode)
                    {
                        for (int i = Main.FileSelect.files.length - 1; i > 0; i--)
                            if (Main.FileSelect.marked[i])
                                Buffer.remove(i - 1);
                    } else if (Main.FileSelect.scrSel > 0)
                        Buffer.remove(Main.FileSelect.scrSel - 1);
                    WaitCanvas.start();
                } else
                {
                    if (!Main.FileSelect.markMode) // удаляем просто немаркированный файл
                        Main.dsp.setCurrent(new ConfirmDeleteAlert(Main.currentPath + Main.currentFile, mn.parent));
                    else
                        Main.dsp.setCurrent(new ConfirmDeleteSelAlert(mn.parent));
                }
                break;
            case Locale.NEW_FOLDER_CMD: // создать папку
                Main.dsp.setCurrent(new NewFolderForm(mn.parent));
                break;
            case Locale.NEW_FILE_CMD: // создать файл
                Main.dsp.setCurrent(new NewFileForm(mn.parent));
                break;
            case Locale.MOVE_CMD: // переместить
                moveit = 1;
            case Locale.COPY_CMD: // копировать
                if (!Main.FileSelect.markMode) // копируем поодиночку
                    Buffer.add(Main.currentPath + Main.currentFile, moveit);
                else // копируем выделенные в буфер
                {
                    for (int i = 1; i < Main.FileSelect.files.length; i++)
                        if (Main.FileSelect.marked[i])
                            Buffer.add(Main.currentPath + Main.FileSelect.files[i], moveit);
                    Main.FileSelect.demarkAll();
                }
                al = new Alert("",
                        Locale.Strings[Locale.SELECT_PASTE_IN_FOLDER],
                        null, AlertType.WARNING);
                //images.warn, null);
                al.setTimeout(3000);
                Main.dsp.setCurrent(al, mn.parent);
                break;
            case Locale.INSERT_CMD: // вставить
                al = new Alert(Locale.Strings[Locale.WAIT],
                        Locale.Strings[Locale.WAIT_PLEASE],
                        null, AlertType.WARNING);
                //images.warn, null);
                al.setTimeout(al.FOREVER);
                Main.dsp.setCurrent(al, mn.parent);
                Buffer.copyMoveFiles();
                break;
            case Locale.FAVOURITE: // показать избранное
                Main.currentPath = "fav:/";
                WaitCanvas.start();
                break;
            case Locale.PREFERENCES_CMD: // настройки
                Main.dsp.setCurrent(new OptionsForm(mn.parent));
                break;
            case Locale.EXTRACT_ALL_CMD: // извлечь всё
                Main.dsp.setCurrent(new ExtractToForm(mn.parent, null));
                break;
            case Locale.EXTRACT_CMD: // извлечь
                v = new Vector();
                if (Main.FileSelect.markMode)
                {
                    for (int i = 1; i < Main.FileSelect.files.length; i++)
                    {
                        if (Main.FileSelect.marked[i])
                        {
                            s = Main.currentPath + Main.FileSelect.files[i];
                            v.addElement(s.substring(Filesystem.divideZipName(s)));
                        }
                    }
                    Main.FileSelect.demarkAll();
                } else
                {
                    s = Main.currentPath + Main.currentFile;
                    v.addElement(s.substring(Filesystem.divideZipName(s)));
                }
                if (v.size() > 0)
                    Main.dsp.setCurrent(new ExtractToForm(mn.parent, v));
                break;
            case Locale.CREATE_ZIP: // создать ZIP-архив из файлов из буфера
                Main.dsp.setCurrent(new CompressForm(mn.parent));
                break;
            case Locale.EDIT_ID3_CMD:
                try
                {
                    Main.dsp.setCurrent(new Id3EditorForm(Main.currentPath + Main.currentFile, Main.FileSelect));
                } catch (Exception x)
                {
                    al = new Alert(Locale.Strings[Locale.ERROR],
                            x.getClass().getName() + ": " + x.getMessage(), null, AlertType.ERROR);
                    al.setTimeout(3000);
                    Main.dsp.setCurrent(al, Main.FileSelect);
                }
                break;
            case Locale.SELECT_CMD:
                mn.ret();
                Main.FileSelect.fireSelectAction();
                break;
            case Locale.PREV_FILE_CMD:
                Main.FileSelect.directKeyPressed(Main.FileSelect.KEY_UP);
                mn.ret();
                break;
            case Locale.NEXT_FILE_CMD:
                Main.FileSelect.directKeyPressed(Main.FileSelect.KEY_DOWN);
                mn.ret();
                break;
            case Locale.PREV_SCREEN_CMD:
                Main.FileSelect.directKeyPressed(Main.FileSelect.KEY_LEFT);
                mn.ret();
                break;
            case Locale.NEXT_SCREEN_CMD:
                Main.FileSelect.directKeyPressed(Main.FileSelect.KEY_RIGHT);
                mn.ret();
                break;
            case Locale.UP_LEVEL_CMD:
                if (Main.currentPath != null && Main.currentPath.length() > 0)
                    Main.FileSelect.upDir();
                else
                    mn.ret();
                break;
            case Locale.OPTIONS_CMD:
                Main.FileSelect.showMenu();
                break;
            case Locale.FULLSCREEN_CMD:
                Main.FileSelect.initFSMode(!Main.FileSelect.fsmode);
                mn.ret();
                break;
            case Locale.BACK_CMD:
                Main.FileSelect.upDir();
                break;
            case Locale.KEYBOARD_CONFIG_CMD:
                Main.keycfg.show();
                break;
        }
    }
}
