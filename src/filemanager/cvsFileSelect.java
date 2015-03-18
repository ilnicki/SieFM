package filemanager; // переведен

import java.util.*;
import javax.microedition.lcdui.*;
import com.vmx.*;

public class cvsFileSelect
       extends gkcCanvas
{
    /** Данные списка */
    String title;
    String files [] = new String [0];
    int types [] = new int [0];
    boolean marked [] = new boolean [0];
    boolean readonly [] = new boolean [0];
    boolean exact [] = new boolean [0];
    int scrStart, scrSel, scrLen;
    /** Панели */
    String panels [] = new String [10];
    String pansel [] = new String [10];
    int curPanel = 0;
    /** Вспомогательные данные */
    String oldpath, oldfile;
    int w, h, allh, clntop, fileH;
    boolean fsmode; // полноэкранный режим
    Font mf, mfro;
    FontWidthCache fc, fcro; // кэши ширины шрифтов %)
    boolean lskp = false, rskp = false, firep = false; // для мигания
    boolean markMode = false;
    protected Image offscreen;
    /**
     * Запуск рескана каталога через cvsWait
     */
    public void showWait ()
    {
        cvsWait.start().back = options.noEffects ? null : paintToImage();
    }
    /**
     * Запуск рескана каталога через cvsWait, после рескана надо выбрать
     * файл selectAfter
     */
    public void showWait (String selectAfter)
    {
        cvsWait.start(selectAfter).back = options.noEffects ? null : paintToImage();
    }
    /**
     * Показ буфера обмена
     */
    public void showBuffer ()
    {
        oldpath = main.currentPath;
        oldfile = files [scrSel];
        main.currentPath = "buf:/";
        showWait ();
    }
    /**
     * Переключение между панелями
     */
    public void changePanel (int to)
    {
        if (curPanel != to)
        {
            panels [curPanel] = main.currentPath;
            pansel [curPanel] = files [scrSel];
            curPanel = to;
            main.currentPath = panels [curPanel];
            showWait(pansel[curPanel]);
        }
    }
    /**
     * Конструктор
     */
    public cvsFileSelect ()
    {
        setFullScreenMode (true);
        w = getWidth ();
        h = allh = getHeight ();
        offscreen = Image.createImage (w, h);
        clntop = 0;
        fsmode = true;
        mf = Font.getFont (Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        mfro = Font.getFont (Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_SMALL);
        fc = FontWidthCache.getCache (mf);
        fcro = FontWidthCache.getCache (mfro);
        fileH = 16;
        scrLen = h/fileH;
        for (int i = 0; i < panels.length; i++)
        {
            panels [i] = null;
            pansel [i] = null;
        }
        initFSMode (false);
    }
    /**
     * Функция обновления списка - по возможности после обновления
     * выберется файл/папка/диск selectAfter
     */
    public void list (String selectAfter) throws Exception
    {
        markMode = false;
        if (main.currentPath == null || main.currentPath.length () == 0)
            listDrives(selectAfter);
        else if (main.currentPath.equals ("fav:/"))
            listFavorites(selectAfter);
        else if (main.currentPath.equals ("buf:/"))
            listBuffer();
        else
            listFiles(selectAfter);
        lskp = rskp = firep = false;
        select (scrSel);
    }
    /** Удаление из списка файла #x */
    public void delete (int x)
    {
        if (x < 1 || x >= files.length)
            return;
        String [] tmp = new String [files.length-1];
        System.arraycopy (files, 0, tmp, 0, x);
        System.arraycopy (files, x+1, tmp, x, files.length-x-1);
        files = tmp;
        int [] tmpi = new int [types.length-1];
        System.arraycopy (types, 0, tmpi, 0, x);
        System.arraycopy (types, x+1, tmpi, x, types.length-x-1);
        types = tmpi;
        boolean [] tmpb = new boolean [readonly.length-1];
        System.arraycopy (readonly, 0, tmpb, 0, x);
        System.arraycopy (readonly, x+1, tmpb, x, readonly.length-x-1);
        readonly = tmpb;
        tmpb = new boolean [exact.length-1];
        System.arraycopy (exact, 0, tmpb, 0, x);
        System.arraycopy (exact, x+1, tmpb, x, exact.length-x-1);
        exact = tmpb;
        if (marked != null)
        {
            tmpb = new boolean [marked.length-1];
            System.arraycopy (marked, 0, tmpb, 0, x);
            System.arraycopy (marked, x+1, tmpb, x, marked.length-x-1);
            marked = tmpb;
        }
        select (scrSel);
    }
    /** Функция обновления списка дисков */
    public void listDrives (String selectAfter) throws Exception
    {
        String [] files1 = filesystem.listRoots ();
        files = new String [files1.length+1];
        System.arraycopy (files1, 0, files, 0, files1.length);
        title = Locale.Strings[Locale.SELECT_DRIVE];
        marked = null;
        readonly = new boolean [files.length];
        types = new int [files.length];
        exact = new boolean [files.length];
        files [files.length-1] = Locale.Strings[Locale.FAVOURITE];
        scrStart = 0;
        scrSel = 0;
        if ("fav:/".equals (selectAfter))
            selectAfter = Locale.Strings[Locale.FAVOURITE];
        for (int i = 0; i < files.length; i++)
        {
            exact [i] = true;
            types [i] = images.iDisk;
            readonly [i] = false;
            if (files [i].equals ("3:/") || files [i].equals ("b:/"))
                readonly [i] = true;
            if (i == files.length-1)
                types [i] = images.iFavorites;
            else if (files [i].equals ("4:/"))
                types [i] = images.iMMC;
            if (files [i].equals (selectAfter))
                scrSel = i;
            // Добавляем название
            if (files [i].equals ("0:/"))
                files [i] += " (Data)";
            else if (files [i].equals ("1:/") || files[i].equals ("b:/"))
                files [i] += " (Cache)";
            else if (files [i].equals ("2:/") || files[i].equals ("3:/"))
                files [i] += " (Config)";
            else if (files [i].equals ("4:/"))
                files [i] += " (MMC)";
        }
    }
    /** Функция обновления списка файлов */
    public void listFiles (String selectAfter) throws Exception
    {
        String [] files1 = filesystem.list (main.currentPath, options.showHidden);
        if (files1 == null)
            files = new String [1];
        else
        {
            files = new String [1+files1.length];
            System.arraycopy (files1, 0, files, 1, files1.length);
        }
        files [0] = "..";
        title = getLastPartOfString (main.currentPath, 13);
        marked = new boolean [files.length];
        types = new int [files.length];
        readonly = new boolean [files.length];
        exact = new boolean [files.length];
        marked [0] = readonly [0] = false;
        types [0] = images.iUp;
        exact [0] = true;
        scrStart = 0;
        scrSel = 1;
        for (int i = 1; i < files.length; i++)
        {
            marked [i] = false;
            exact [i] = false;
            if (files [i].charAt (files[i].length()-1) == '/')
                types [i] = images.iFolder;
            else
                types [i] = filesystem.fileType (files[i]);
            if (files [i].equals (selectAfter))
                scrSel = i;
        }
    }
    /** Функция обновления списка "Избранное" */
    public void listFavorites (String selectAfter)
    {
        String [] files1 = options.getFavorites ();
        files = new String [files1.length+1];
        System.arraycopy (files1, 0, files, 1, files1.length);
        files [0] = "..";
        marked = new boolean [files.length];
        types = new int [files.length];
        readonly = new boolean [files.length];
        exact = new boolean [files.length];
        marked [0] = readonly [0] = false;
        types [0] = images.iUp;
        exact [0] = true;
        title = Locale.Strings[Locale.FAVOURITE];
        scrStart = 0;
        scrSel = 1;
        for (int i = 1; i < files.length; i++)
        {
            marked [i] = false;
            exact [i] = true;
            types [i] = images.iFolder;
            if (files [i].equals (selectAfter))
                scrSel = i;
        }
        repaint ();
    }
    public void listBuffer ()
    {
        String [] files1 = Buffer.getBuffer();
        files = new String [files1.length+1];
        System.arraycopy (files1, 0, files, 1, files1.length);
        files [0] = "..";
        marked = new boolean [files.length];
        types = new int [files.length];
        readonly = new boolean [files.length];
        exact = new boolean [files.length];
        marked [0] = readonly [0] = false;
        types [0] = images.iUp;
        exact [0] = true;
        title = Locale.Strings[Locale.BUFFER];
        scrStart = 0;
        scrSel = 1;
        for (int i = 1; i < files.length; i++)
        {
            marked [i] = false;
            exact [i] = false;
            if (files [i].charAt (files[i].length()-1) == '/')
                types [i] = images.iFolder;
            else
                types [i] = filesystem.fileType (files[i]);
        }
        repaint ();
    }
    /** Обновить тип файла #i */
    public void updateFileType (int i)
    {
        if (i < 1 || i >= files.length ||
            main.currentPath == null || main.currentPath.length () == 0)
            return;
        String file = main.currentPath;
        if (file.equals ("buf:/"))
            file = "";
        file += files[i];
        readonly [i] = filesystem.isReadOnly (file);
        if (!filesystem.isDir (file))
            types [i] = filesystem.fileType (file);
        else
        {
            if (files [i].charAt (files[i].length()-1) != '/')
                files [i] += "/";
            if (!filesystem.isHidden (file))
                types [i] = images.iFolder;
            else
                types [i] = images.iHiddenFolder;
        }
    }
    /**
     * Инициализация режима выделения
     */
    public void startMarkMode ()
    {
        for (int i = 0; i < files.length; i++)
            marked [i] = false;
        markMode = true;
    }
    /**
     * Пометить выбранный
     */
    public void markSelected ()
    {
        if (main.currentPath != null)
        {
            if (!markMode)
                startMarkMode ();
            if (scrSel > 0)
                marked [scrSel] = !marked [scrSel];
            repaint ();
        }
    }
    /**
     * Пометить все
     */
    public void markAll ()
    {
        if (main.currentPath != null)
        {
            if (!markMode)
                startMarkMode ();
            for (int i = 1; i < files.length; i++)
                marked [i] = true;
            repaint ();
        }
    }
    /**
     * Сбросить все отметки
     */
    public void demarkAll ()
    {
        if (markMode)
        {
            for (int i = 0; i < files.length; i++)
                marked [i] = false;
            markMode = false;
            repaint ();
        }
    }
    /**
     * Перемещение по дереву каталогов вверх, переключение на меню
     * выбора дисков или Избранное, или выход из программы наффик
     */
    public void upDir ()
    {
        int pos, go = 1;
        String str = null;
        if (main.currentPath == null || main.currentPath.length () == 0)
            main.midlet.destroyApp (true);
        if (!main.isFavorite)
        {
            if (main.currentPath.equals ("fav:/"))
            {
                str = "fav:/";
                main.currentPath = null;
            }
            else if (main.currentPath.equals ("buf:/"))
            {
                str = oldfile;
                main.currentPath = oldpath;
            }
            else
            {
                str = main.currentPath.substring (0, main.currentPath.length()-1);
                if ((pos = str.lastIndexOf ('/')) > 0)
                {
                    str = main.currentPath.substring (pos+1);
                    if (filesystem.divideZipName (str) == str.length ())
                        str = str.substring (0,str.length()-1);
                    main.currentPath = main.currentPath.substring (0, pos+1);
                }
                else
                {
                    str = main.currentPath;
                    main.currentPath = null;
                }
            }
        }
        else
        {
            str = main.currentPath;
            main.currentPath = "fav:/";
        }
        showWait (str);
    }
    /**
     * Выбор файла/папки, заданного main.currentPath и main.currentFile
     */
    public void selectFile ()
    {
        prevpath = main.currentPath;
        main.currentFile = files[scrSel];
        if (main.currentPath == null || main.currentPath.length () == 0)
        {
            if (!files[scrSel].equals (Locale.Strings[Locale.FAVOURITE]))
                main.currentFile = main.currentFile.substring (0, 3);
            else main.currentFile = "fav:/";
        }
        if (!main.currentFile.equals (".."))
        {
            // если выбранный файл директория то входим в нее
            if (main.currentFile.charAt (main.currentFile.length()-1) == '/')
            {
                if (main.currentPath == null || "fav:/".equals (main.currentPath) ||
                    "buf:/".equals (main.currentPath))
                    main.currentPath = "";
                main.currentPath = main.currentPath + main.currentFile;
                showWait ();
            }
            // если не вложенный ZIP-файл - входим в него
            else if (filesystem.divideZipName (main.currentPath) < 0 &&
                     filesystem.fileType (main.currentFile) == filesystem.TYPE_ZIP)
            {
                main.currentPath = main.currentPath + main.currentFile + "/";
                showWait ();
            }
            // Если мы уже внутри ZIP-файла, то открываем только текст и
            // изображения (без масштабирования)
            else
            {
                String fileName = main.currentPath + main.currentFile;
                int type = filesystem.fileType(fileName), zipext;
                if ((zipext = filesystem.divideZipName (fileName)) >= 0)
                {
                    switch (type)
                    {
                        case filesystem.TYPE_PICTURE:
                            try
                            {
                                main.imageview.displayImageFromStream (filesystem.getZipInputStream (fileName.substring (zipext)), this);
                                main.dsp.setCurrent (main.imageview);
                            } catch (Exception x) {}
                            break;
                        default:
                            main.dsp.setCurrent (main.textEditor);
                            main.textEditor.openFile (fileName);
                            break;
                    }
                }
                else // файл
                {
                    switch (type)
                    {
                        case filesystem.TYPE_SOUND: // мелодия
                            main.dsp.setCurrent (main.player);
                            main.player.playSound (fileName, this);
                            break;
                        case filesystem.TYPE_PICTURE: // картинка
                            main.dsp.setCurrent (main.imageview);
                            main.imageview.displayImage (fileName, this);
                            break;
                        case filesystem.TYPE_VIDEO: // видео
                            main.dsp.setCurrent (main.videoplayer);
                            main.videoplayer.playVideo (fileName, this);
                            break;
                        case filesystem.TYPE_TEXT: // текст
                            main.dsp.setCurrent (main.textEditor);
                            main.textEditor.openFile (fileName);
                            break;
                        case filesystem.TYPE_TMO: // TMO
                            if (filesystem.isReadOnly (fileName)) // проверка на readonly
                                main.dsp.setCurrent (new tbTmoEdit (fileName, false, true, this));
                            else
                                main.dsp.setCurrent (new tbTmoEdit (fileName, false, false, this));
                            break;
                        default: // всё остальное
                            if (options.openNotSupported)
                            {
                                main.dsp.setCurrent (main.textEditor);
                                main.textEditor.openFile (fileName);
                            }
                            else
                            {
                                Alert al = new Alert (Locale.Strings[Locale.ERROR],
                                        Locale.Strings[Locale.FORMAT_NOT_SUPP],
                                        null, AlertType.ERROR);
                                        //images.error, null);
                                al.setTimeout (3000);
                                main.dsp.setCurrent (al, this);
                            }
                            break;
                    }
                }
            }
        }
        else // если выбрано .. или Назад переходим по папке вверх
            upDir ();
    }
    /**
     * Показать меню
     */
    public void showMenu ()
    {
        int menuType;
        if (main.currentPath != null)
        {
            if (main.currentPath.equals ("fav:/"))
                menuType = cvsMenu.MENU_FAVORITES_SELECTED;
            else if (main.currentPath.equals ("buf:/"))
                menuType = cvsMenu.MENU_BUFFER_SELECTED;
            else if (filesystem.divideZipName (main.currentPath) >= 0)
                menuType = cvsMenu.MENU_INSIDE_ARCHIVE;
            else
            {
                if ("..".equals(main.currentFile))
                    menuType = cvsMenu.MENU_DOTDOT_SELECTED;
                else
                {
                    if (filesystem.isDir (main.currentPath + main.currentFile))
                        menuType = cvsMenu.MENU_FOLDER_SELECTED;
                    else
                        menuType = cvsMenu.MENU_FILE_SELECTED;
                }
            }
        }
        else// if (!Locale.Strings[Locale.FAVOURITE].equals (main.currentFile)) // выбран диск
            menuType = cvsMenu.MENU_DISK_SELECTED;
        //else menuType = cvsMenu.MENU_FAVORITES_SELECTED;
        main.menu.back = paintToImage ();
        main.menu.parent = this;
        main.menu.setType (menuType);
        main.dsp.setCurrent (main.menu);
    }
    /**
     * Нарисовать текущее содержимое в картинку
     */
    public Image paintToImage ()
    {
        paint (offscreen.getGraphics());
        return offscreen;
    }
    /**
     * Выбрать файл #index
     */
    public void select (int index)
    {
        scrSel = index;
        if (scrSel >= files.length)
            scrSel = files.length-1;
        scrStart = scrSel - scrLen/2;
        if (scrStart + scrLen >= files.length)
            scrStart = files.length-scrLen;
        if (scrStart < 0)
            scrStart = 0;
        repaint ();
    }
    /**
     * Вернуть индекс следующего файла типа type
     */
    int getNextOfType (int current, int type)
    {
        boolean notfound = true;
        int old = current;
        do
        {
            current++;
            if (current >= files.length)
                current = 1;
            if (filesystem.fileType (files[current]) == type)
                break;
        } while (current != old);
        return current;
    }
    /**
     * Вернуть индекс предыдущего файла типа type
     */
    int getPrevOfType (int current, int type)
    {
        boolean notfound = true;
        int old = current;
        do
        {
            current--;
            if (current < 1)
                current = files.length-1;
            if (filesystem.fileType (files[current]) == type)
                break;
        } while (current != old);
        return current;
    }
    /**
     * Функция переключения между собственными полноэкранным
     * и неполноэкранным режимами
     */
    public void initFSMode (boolean mode)
    {
        if (fsmode != mode)
        {
            fsmode = mode;
            h = allh;
            clntop = 0;
            if (!fsmode)
            {
                h -= 38;
                clntop = 19;
            }
            scrLen = h/fileH;
            select (scrSel);
        }
    }
    /**
     * Функция отрисовки
     */
    protected void paint (Graphics g)
    {
        int i, fh, p, icon;
        g.setColor (Colors.back);
        g.setClip (0, 0, w, allh);
        g.fillRect (0, 0, w, allh);
        boolean inBuffer = "buf:/".equals (main.currentPath);
        if (!fsmode) // рисуем интерфейс
        {
            g.setColor (Colors.fore);
            g.drawLine (0, 18, w, 18);
            g.drawLine (0, allh-19, w, allh-19);
            icon = images.iFolder;
            if (title.endsWith (":/"))
            {
                icon = images.iDisk;
                if (title.endsWith("4:/"))
                    icon = images.iMMC;
            }
            else if (title.equals (Locale.Strings[Locale.SELECT_DRIVE]))
                icon = images.iSieFM;
            else if (title.equals (Locale.Strings[Locale.FAVOURITE]))
                icon = images.iFavorites;
            else if (title.equals (Locale.Strings[Locale.BUFFER]))
                icon = images.iClipboard;
            images.drawIcon (g, icon, 1, 1);
            // заглавие
            g.drawString (title, 19, 9-g.getFont().getHeight ()/2, Graphics.LEFT|Graphics.TOP);
            // номер панели
            String cp = String.valueOf (curPanel+1);
            g.setColor (Colors.back);
            g.fillRect (w-g.getFont().stringWidth(cp), 0, w, 17);
            g.setColor (Colors.fore1);
            g.drawString (cp, w, 9-g.getFont().getHeight()/2, Graphics.RIGHT|Graphics.TOP);
            // подписи к LSK, RSK и джойстику
            int lskstr = keyConfig.keyConfig[keyConfig.CONF_LSK],
                rskstr = keyConfig.keyConfig[keyConfig.CONF_RSK];
            if (main.currentPath == null || main.currentPath.length () == 0)
            {
                if (lskstr == Locale.BACK_CMD)
                    lskstr = Locale.EXIT_CMD;
                if (rskstr == Locale.BACK_CMD)
                    rskstr = Locale.EXIT_CMD;
            }
            if (lskp)
            {
                g.setColor (Colors.selback);
                g.fillRect (0, allh-18, 60, 18);
                g.setColor (Colors.selfore);
            }
            else
                g.setColor (Colors.fore);
            g.setClip (0, allh-18, 60, 18);
            if (lskstr >= 0)
                g.drawString (Locale.Strings[lskstr], 30, allh-1, Graphics.HCENTER|Graphics.BOTTOM);
            g.setClip (0, 0, w, allh);
            if (rskp)
            {
                g.setColor (Colors.selback);
                g.fillRect (w-60, allh-18, 60, 18);
                g.setColor (Colors.selfore);
            }
            else
                g.setColor (Colors.fore);
            g.setClip (w-60, allh-18, 60, 18);
            if (rskstr >= 0)
                g.drawString (Locale.Strings[rskstr], w-31, allh-1, Graphics.HCENTER|Graphics.BOTTOM);
            g.setClip (0, 0, w, allh);
            if (firep)
            {
                g.setColor (Colors.selback);
                g.fillRect (w/2-9, allh-18, 18, 18);
            }
            images.drawIcon (g, images.iSelect, w/2-8, allh-17);
        }
        g.translate (0, clntop);
        g.setClip (0, 0, w-3, h);
        for (i = scrStart, p = 0; i < files.length && i < scrStart+scrLen+1; i++, p++)
        {
            if (!exact [i])
            {
                updateFileType (i);
                exact [i] = true;
            }
            if (scrSel == i)
            {
                g.setColor (Colors.selback);
                g.fillRect (0, p*fileH, w, fileH);
                g.setColor (Colors.selfore);
            }
            else
                g.setColor (Colors.fore);
            if (markMode && marked [i])
                images.drawIcon (g, images.iMark, 1, p*fileH);
            else
                images.drawIcon (g, types[i], 1, p*fileH);
            if (inBuffer && i > 0 && Buffer.move.get (i-1) > 0)
                images.drawIcon (g, images.iMoveIt, 1, p*fileH);
            if (readonly [i])
            {
                fh = mfro.getHeight ();
                g.setFont (mfro);
            }
            else
            {
                fh = mf.getHeight ();
                g.setFont (mf);
            }
            g.drawString (files[i], 18, p*fileH + fileH/2 - fh/2, Graphics.LEFT|Graphics.TOP);
        }
        g.setClip (0, 0, w, h);
        g.setColor (Colors.fore);
        g.drawLine (w-2, 0, w-2, h);
        int sbstart = h * scrStart/files.length, sbsize = h * scrLen/files.length;
        if (sbsize > h)
            sbsize = h;
        g.drawLine (w-1, sbstart, w-1, sbstart+sbsize);
        g.drawLine (w-3, sbstart, w-3, sbstart+sbsize);
    }
    /**
     * Обработчик нажатий клавиш
     */
    public void keyPressed (int keyCode)
    {
        int id = -1;
        if (keyCode == KEY_LEFT)
            id = keyConfig.CONF_LEFT;
        else if (keyCode == KEY_RIGHT)
            id = keyConfig.CONF_RIGHT;
        else if (keyCode == KEY_UP)
            id = keyConfig.CONF_UP;
        else if (keyCode == KEY_DOWN)
            id = keyConfig.CONF_DOWN;
        else if (keyCode == KEY_FIRE)
        {
            if (!fsmode && !options.noEffects)
            {
                firep = true;
                repaint ();
            }
            else 
                id = keyConfig.CONF_FIRE;            
        }
        else switch (keyCode)
        {
            case KEY_STAR:   id = keyConfig.CONF_STAR; break;
            case KEY_POUND:  id = keyConfig.CONF_POUND; break;
            case KEY_DIAL:   id = keyConfig.CONF_DIAL; break;
            case KEY_CANCEL: id = keyConfig.CONF_CANCEL; break;
            case KEY_LSK:
                if (!fsmode && !options.noEffects)
                {
                    lskp = true;
                    repaint ();
                }
                else 
                    id = keyConfig.CONF_LSK;
                break;
            case KEY_RSK:
                if (!fsmode && !options.noEffects)
                {
                    rskp = true;
                    repaint ();
                }
                else 
                    id = keyConfig.CONF_RSK;
                break;
            case KEY_NUM0: id = keyConfig.CONF_NUM;   break;
            case KEY_NUM1: id = keyConfig.CONF_NUM+1; break;
            case KEY_NUM2: id = keyConfig.CONF_NUM+2; break;
            case KEY_NUM3: id = keyConfig.CONF_NUM+3; break;
            case KEY_NUM4: id = keyConfig.CONF_NUM+4; break;
            case KEY_NUM5: id = keyConfig.CONF_NUM+5; break;
            case KEY_NUM6: id = keyConfig.CONF_NUM+6; break;
            case KEY_NUM7: id = keyConfig.CONF_NUM+7; break;
            case KEY_NUM8: id = keyConfig.CONF_NUM+8; break;
            case KEY_NUM9: id = keyConfig.CONF_NUM+9; break;
        }   
        if (id >= 0)
        {
            main.menu.parent = this;
            main.menu.listen.menuAction (keyConfig.keyConfig[id]);
        }
    }
    public void directKeyPressed (int keyCode)
    {
        main.currentFile = files[scrSel];
        if (keyCode == KEY_DOWN || keyCode == KEY_RIGHT)
        {
            if (keyCode == KEY_DOWN)
                scrSel++;
            else scrSel += scrLen;
            if (scrSel >= files.length)
                scrSel = 0;
            select (scrSel);
        }
        else if (keyCode == KEY_UP || keyCode == KEY_LEFT)
        {
            if (keyCode == KEY_UP)
                scrSel--;
            else scrSel -= scrLen;
            if (scrSel < 0)
                scrSel = files.length-1;
            select (scrSel);
        }
    }
    /**
     * Выделение по нажатию кнопы "огонь", т.е джойстика
     */
    public void fireSelectAction ()
    {
        if (!markMode)
            selectFile ();
        else // если идёт процесс выделения
        {
            markSelected ();
            repaint ();
        }
    }
    public String prevpath;
    /**
     * Обработчик отпусканий клавиш
     */
    public void keyReleased (int keyCode)
    {
        if (fsmode)
            return;
        int id = -1;
        if (keyCode == KEY_LSK && lskp)
        {
            id = keyConfig.CONF_LSK;
            lskp = false;
        }
        else if (keyCode == KEY_RSK && rskp)
        {
            id = keyConfig.CONF_RSK;
            rskp = false;
        }
        else if (keyCode == KEY_FIRE && firep)
        {
            id = keyConfig.CONF_FIRE;
            firep = false;
        }
        if (id >= 0)
        {
            main.menu.parent = this;
            main.menu.listen.menuAction (keyConfig.keyConfig[id]);
        }
    }
    /**
     * Обработчик повторений клавиш
     */
    int starHeld = 0;
    public void keyRepeated (int keyCode)
    {
        if (keyCode == KEY_DOWN || keyCode == KEY_UP)
            keyPressed (keyCode);
        else if (keyCode == KEY_STAR)
        {
            starHeld++;
            if (starHeld > 3)
            {
                starHeld = 0;
                main.menu.parent = this;
                (main.menu.listen = new WorkingMenu (main.menu)).menuAction (Locale.KEYBOARD_CONFIG_CMD);
            }
        }
    }
    /**
     *  Получить последние n символов (для титула)
     *
     * @param path String
     * @param n int
     * @return String
     */
    public static String getLastPartOfString (String path, int n)
    {
        if (path.length () <= n)
            return path;
        else
            return (".." + path.substring (path.length()-n-1, path.length ()));
    }
}
