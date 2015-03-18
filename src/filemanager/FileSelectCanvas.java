package filemanager;

import javax.microedition.lcdui.*;
import com.vmx.*;

/**
 *
 * @author Dmytro
 */
public class FileSelectCanvas
        extends KeyCodeCanvas
{

    /**
     * Данные списка.
     */
    String title;
    String files[] = new String[0];
    int types[] = new int[0];
    boolean marked[] = new boolean[0];
    boolean readonly[] = new boolean[0];
    boolean exact[] = new boolean[0];
    int scrStart, scrSel, scrLen;
    /**
     * Панели.
     */
    String panels[] = new String[10];
    String pansel[] = new String[10];
    int curPanel = 0;
    /**
     * Вспомогательные данные.
     */
    String oldpath, oldfile;
    int w, h, allh, clntop, fileH;
    boolean fsmode; // полноэкранный режим
    Font mf, mfro;
    FontWidthCache fc, fcro; // кэши ширины шрифтов %)
    boolean lskp = false, rskp = false, firep = false; // для мигания
    boolean markMode = false;
    protected Image offscreen;

    /**
     * Запуск рескана каталога через WaitCanvas.
     */
    public void showWait()
    {
        WaitCanvas.start().back = Options.noEffects ? null : paintToImage();
    }

    /**
     * Запуск рескана каталога через WaitCanvas, после рескана надо выбрать файл
 selectAfter.
     *
     * @param selectAfter
     */
    public void showWait(String selectAfter)
    {
        WaitCanvas.start(selectAfter).back = Options.noEffects ? null : paintToImage();
    }

    /**
     * Показ буфера обмена.
     */
    public void showBuffer()
    {
        oldpath = Main.currentPath;
        oldfile = files[scrSel];
        Main.currentPath = "buf:/";
        showWait();
    }

    /**
     * Переключение между панелями.
     *
     * @param to
     */
    public void changePanel(int to)
    {
        if (curPanel != to)
        {
            panels[curPanel] = Main.currentPath;
            pansel[curPanel] = files[scrSel];
            curPanel = to;
            Main.currentPath = panels[curPanel];
            showWait(pansel[curPanel]);
        }
    }

    /**
     * Конструктор.
     */
    public FileSelectCanvas()
    {
        setFullScreenMode(true);
        w = getWidth();
        h = allh = getHeight();
        offscreen = Image.createImage(w, h);
        clntop = 0;
        fsmode = true;
        mf = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        mfro = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_SMALL);
        fc = FontWidthCache.getCache(mf);
        fcro = FontWidthCache.getCache(mfro);
        fileH = 16;
        scrLen = h / fileH;
        for (int i = 0; i < panels.length; i++)
        {
            panels[i] = null;
            pansel[i] = null;
        }
        initFSMode(false);
    }

    /**
     * Функция обновления списка - по возможности после обновления выберется
     * файл/папка/диск selectAfter.
     *
     * @param selectAfter
     * @throws java.lang.Exception
     */
    public void list(String selectAfter) throws Exception
    {
        markMode = false;
        if (Main.currentPath == null || Main.currentPath.length() == 0)
            listDrives(selectAfter);
        else if (Main.currentPath.equals("fav:/"))
            listFavorites(selectAfter);
        else if (Main.currentPath.equals("buf:/"))
            listBuffer();
        else
            listFiles(selectAfter);
        lskp = rskp = firep = false;
        select(scrSel);
    }

    /**
     * Удаление файла x из списка.
     *
     * @param x
     */
    public void delete(int x)
    {
        if (x < 1 || x >= files.length)
            return;
        String[] tmp = new String[files.length - 1];
        System.arraycopy(files, 0, tmp, 0, x);
        System.arraycopy(files, x + 1, tmp, x, files.length - x - 1);
        files = tmp;
        int[] tmpi = new int[types.length - 1];
        System.arraycopy(types, 0, tmpi, 0, x);
        System.arraycopy(types, x + 1, tmpi, x, types.length - x - 1);
        types = tmpi;
        boolean[] tmpb = new boolean[readonly.length - 1];
        System.arraycopy(readonly, 0, tmpb, 0, x);
        System.arraycopy(readonly, x + 1, tmpb, x, readonly.length - x - 1);
        readonly = tmpb;
        tmpb = new boolean[exact.length - 1];
        System.arraycopy(exact, 0, tmpb, 0, x);
        System.arraycopy(exact, x + 1, tmpb, x, exact.length - x - 1);
        exact = tmpb;
        if (marked != null)
        {
            tmpb = new boolean[marked.length - 1];
            System.arraycopy(marked, 0, tmpb, 0, x);
            System.arraycopy(marked, x + 1, tmpb, x, marked.length - x - 1);
            marked = tmpb;
        }
        select(scrSel);
    }

    /**
     * Функция обновления списка дисков.
     *
     * @param selectAfter
     * @throws java.lang.Exception
     */
    public void listDrives(String selectAfter) throws Exception
    {
        String[] files1 = Filesystem.listRoots();
        files = new String[files1.length + 1];
        System.arraycopy(files1, 0, files, 0, files1.length);
        title = Locale.Strings[Locale.SELECT_DRIVE];
        marked = null;
        readonly = new boolean[files.length];
        types = new int[files.length];
        exact = new boolean[files.length];
        files[files.length - 1] = Locale.Strings[Locale.FAVOURITE];
        scrStart = 0;
        scrSel = 0;
        if ("fav:/".equals(selectAfter))
            selectAfter = Locale.Strings[Locale.FAVOURITE];
        for (int i = 0; i < files.length; i++)
        {
            exact[i] = true;
            types[i] = Images.iDisk;
            readonly[i] = false;
            if (files[i].equals("3:/") || files[i].equals("b:/"))
                readonly[i] = true;
            if (i == files.length - 1)
                types[i] = Images.iFavorites;
            else if (files[i].equals("4:/"))
                types[i] = Images.iMMC;
            if (files[i].equals(selectAfter))
                scrSel = i;
            // Добавляем название
            if (files[i].equals("0:/"))
                files[i] += " (Data)";
            else if (files[i].equals("1:/") || files[i].equals("b:/"))
                files[i] += " (Cache)";
            else if (files[i].equals("2:/") || files[i].equals("3:/"))
                files[i] += " (Config)";
            else if (files[i].equals("4:/"))
                files[i] += " (MMC)";
        }
    }

    /**
     * Функция обновления списка файлов.
     *
     * @param selectAfter
     * @throws java.lang.Exception
     */
    public void listFiles(String selectAfter) throws Exception
    {
        String[] files1 = Filesystem.list(Main.currentPath, Options.showHidden);
        if (files1 == null)
            files = new String[1];
        else
        {
            files = new String[1 + files1.length];
            System.arraycopy(files1, 0, files, 1, files1.length);
        }
        files[0] = "..";
        title = getLastPartOfString(Main.currentPath, 13);
        marked = new boolean[files.length];
        types = new int[files.length];
        readonly = new boolean[files.length];
        exact = new boolean[files.length];
        marked[0] = readonly[0] = false;
        types[0] = Images.iUp;
        exact[0] = true;
        scrStart = 0;
        scrSel = 1;
        for (int i = 1; i < files.length; i++)
        {
            marked[i] = false;
            exact[i] = false;
            if (files[i].charAt(files[i].length() - 1) == '/')
                types[i] = Images.iFolder;
            else
                types[i] = Filesystem.fileType(files[i]);
            if (files[i].equals(selectAfter))
                scrSel = i;
        }
    }

    /**
     * Функция обновления списка "Избранное".
     *
     * @param selectAfter
     */
    public void listFavorites(String selectAfter)
    {
        String[] files1 = Options.getFavorites();
        files = new String[files1.length + 1];
        System.arraycopy(files1, 0, files, 1, files1.length);
        files[0] = "..";
        marked = new boolean[files.length];
        types = new int[files.length];
        readonly = new boolean[files.length];
        exact = new boolean[files.length];
        marked[0] = readonly[0] = false;
        types[0] = Images.iUp;
        exact[0] = true;
        title = Locale.Strings[Locale.FAVOURITE];
        scrStart = 0;
        scrSel = 1;
        for (int i = 1; i < files.length; i++)
        {
            marked[i] = false;
            exact[i] = true;
            types[i] = Images.iFolder;
            if (files[i].equals(selectAfter))
                scrSel = i;
        }
        repaint();
    }

    /**
     *
     */
    public void listBuffer()
    {
        String[] files1 = Buffer.getBuffer();
        files = new String[files1.length + 1];
        System.arraycopy(files1, 0, files, 1, files1.length);
        files[0] = "..";
        marked = new boolean[files.length];
        types = new int[files.length];
        readonly = new boolean[files.length];
        exact = new boolean[files.length];
        marked[0] = readonly[0] = false;
        types[0] = Images.iUp;
        exact[0] = true;
        title = Locale.Strings[Locale.BUFFER];
        scrStart = 0;
        scrSel = 1;
        for (int i = 1; i < files.length; i++)
        {
            marked[i] = false;
            exact[i] = false;
            if (files[i].charAt(files[i].length() - 1) == '/')
                types[i] = Images.iFolder;
            else
                types[i] = Filesystem.fileType(files[i]);
        }
        repaint();
    }

    /**
     * Обновить тип файла i.
     *
     * @param i
     */
    public void updateFileType(int i)
    {
        if (i < 1 || i >= files.length
                || Main.currentPath == null || Main.currentPath.length() == 0)
            return;
        String file = Main.currentPath;
        if (file.equals("buf:/"))
            file = "";
        file += files[i];
        readonly[i] = Filesystem.isReadOnly(file);
        if (!Filesystem.isDir(file))
            types[i] = Filesystem.fileType(file);
        else
        {
            if (files[i].charAt(files[i].length() - 1) != '/')
                files[i] += "/";
            if (!Filesystem.isHidden(file))
                types[i] = Images.iFolder;
            else
                types[i] = Images.iHiddenFolder;
        }
    }

    /**
     * Инициализация режима выделения.
     */
    public void startMarkMode()
    {
        for (int i = 0; i < files.length; i++)
            marked[i] = false;
        markMode = true;
    }

    /**
     * Пометить выбранный.
     */
    public void markSelected()
    {
        if (Main.currentPath != null)
        {
            if (!markMode)
                startMarkMode();
            if (scrSel > 0)
                marked[scrSel] = !marked[scrSel];
            repaint();
        }
    }

    /**
     * Пометить все.
     */
    public void markAll()
    {
        if (Main.currentPath != null)
        {
            if (!markMode)
                startMarkMode();
            for (int i = 1; i < files.length; i++)
                marked[i] = true;
            repaint();
        }
    }

    /**
     * Сбросить все отметки.
     */
    public void demarkAll()
    {
        if (markMode)
        {
            for (int i = 0; i < files.length; i++)
                marked[i] = false;
            markMode = false;
            repaint();
        }
    }

    /**
     * Перемещение по дереву каталогов вверх, переключение на меню выбора дисков
     * или Избранное, или выход из программы наффик.
     */
    public void upDir()
    {
        int pos, go = 1;
        String str = null;
        if (Main.currentPath == null || Main.currentPath.length() == 0)
            Main.midlet.destroyApp(true);
        if (!Main.isFavorite)
        {
            if (Main.currentPath.equals("fav:/"))
            {
                str = "fav:/";
                Main.currentPath = null;
            } else if (Main.currentPath.equals("buf:/"))
            {
                str = oldfile;
                Main.currentPath = oldpath;
            } else
            {
                str = Main.currentPath.substring(0, Main.currentPath.length() - 1);
                if ((pos = str.lastIndexOf('/')) > 0)
                {
                    str = Main.currentPath.substring(pos + 1);
                    if (Filesystem.divideZipName(str) == str.length())
                        str = str.substring(0, str.length() - 1);
                    Main.currentPath = Main.currentPath.substring(0, pos + 1);
                } else
                {
                    str = Main.currentPath;
                    Main.currentPath = null;
                }
            }
        } else
        {
            str = Main.currentPath;
            Main.currentPath = "fav:/";
        }
        showWait(str);
    }

    /**
     * Выбор файла/папки, заданного Main.currentPath и Main.currentFile.
     */
    public void selectFile()
    {
        prevpath = Main.currentPath;
        Main.currentFile = files[scrSel];
        if (Main.currentPath == null || Main.currentPath.length() == 0)
        {
            if (!files[scrSel].equals(Locale.Strings[Locale.FAVOURITE]))
                Main.currentFile = Main.currentFile.substring(0, 3);
            else
                Main.currentFile = "fav:/";
        }
        if (!Main.currentFile.equals(".."))
        {
            // если выбранный файл директория то входим в нее
            if (Main.currentFile.charAt(Main.currentFile.length() - 1) == '/')
            {
                if (Main.currentPath == null || "fav:/".equals(Main.currentPath)
                        || "buf:/".equals(Main.currentPath))
                    Main.currentPath = "";
                Main.currentPath = Main.currentPath + Main.currentFile;
                showWait();
            } // если не вложенный ZIP-файл - входим в него
            else if (Filesystem.divideZipName(Main.currentPath) < 0
                    && Filesystem.fileType(Main.currentFile) == Filesystem.TYPE_ZIP)
            {
                Main.currentPath = Main.currentPath + Main.currentFile + "/";
                showWait();
            } // Если мы уже внутри ZIP-файла, то открываем только текст и
            // изображения (без масштабирования)
            else
            {
                String fileName = Main.currentPath + Main.currentFile;
                int type = Filesystem.fileType(fileName), zipext;
                if ((zipext = Filesystem.divideZipName(fileName)) >= 0)
                {
                    switch (type)
                    {
                        case Filesystem.TYPE_PICTURE:
                            try
                            {
                                Main.imageview.displayImageFromStream(Filesystem.getZipInputStream(fileName.substring(zipext)), this);
                                Main.dsp.setCurrent(Main.imageview);
                            } catch (Exception x)
                            {
                            }
                            break;
                        default:
                            Main.dsp.setCurrent(Main.textEditor);
                            Main.textEditor.openFile(fileName);
                            break;
                    }
                } else // файл
                {
                    switch (type)
                    {
                        case Filesystem.TYPE_SOUND: // мелодия
                            Main.dsp.setCurrent(Main.player);
                            Main.player.playSound(fileName, this);
                            break;
                        case Filesystem.TYPE_PICTURE: // картинка
                            Main.dsp.setCurrent(Main.imageview);
                            Main.imageview.displayImage(fileName, this);
                            break;
                        case Filesystem.TYPE_VIDEO: // видео
                            Main.dsp.setCurrent(Main.videoplayer);
                            Main.videoplayer.playVideo(fileName, this);
                            break;
                        case Filesystem.TYPE_TEXT: // текст
                            Main.dsp.setCurrent(Main.textEditor);
                            Main.textEditor.openFile(fileName);
                            break;
                        case Filesystem.TYPE_TMO: // TMO
                            if (Filesystem.isReadOnly(fileName)) // проверка на readonly
                                Main.dsp.setCurrent(new TmoEditTextBox(fileName, false, true, this));
                            else
                                Main.dsp.setCurrent(new TmoEditTextBox(fileName, false, false, this));
                            break;
                        default: // всё остальное
                            if (Options.openNotSupported)
                            {
                                Main.dsp.setCurrent(Main.textEditor);
                                Main.textEditor.openFile(fileName);
                            } else
                            {
                                Alert al = new Alert(Locale.Strings[Locale.ERROR],
                                        Locale.Strings[Locale.FORMAT_NOT_SUPP],
                                        null, AlertType.ERROR);
                                //images.error, null);
                                al.setTimeout(3000);
                                Main.dsp.setCurrent(al, this);
                            }
                            break;
                    }
                }
            }
        } else // если выбрано .. или Назад переходим по папке вверх
            upDir();
    }

    /**
     * Показать меню.
     */
    public void showMenu()
    {
        int menuType;
        if (Main.currentPath != null)
        {
            if (Main.currentPath.equals("fav:/"))
                menuType = MenuCanvas.MENU_FAVORITES_SELECTED;
            else if (Main.currentPath.equals("buf:/"))
                menuType = MenuCanvas.MENU_BUFFER_SELECTED;
            else if (Filesystem.divideZipName(Main.currentPath) >= 0)
                menuType = MenuCanvas.MENU_INSIDE_ARCHIVE;
            else
            {
                if ("..".equals(Main.currentFile))
                    menuType = MenuCanvas.MENU_DOTDOT_SELECTED;
                else
                {
                    if (Filesystem.isDir(Main.currentPath + Main.currentFile))
                        menuType = MenuCanvas.MENU_FOLDER_SELECTED;
                    else
                        menuType = MenuCanvas.MENU_FILE_SELECTED;
                }
            }
        } else// if (!Locale.Strings[Locale.FAVOURITE].equals (Main.currentFile)) // выбран диск
            menuType = MenuCanvas.MENU_DISK_SELECTED;
        //else menuType = MenuCanvas.MENU_FAVORITES_SELECTED;
        Main.menu.back = paintToImage();
        Main.menu.parent = this;
        Main.menu.setType(menuType);
        Main.dsp.setCurrent(Main.menu);
    }

    /**
     * Нарисовать текущее содержимое в картинку.
     *
     * @return
     */
    public Image paintToImage()
    {
        paint(offscreen.getGraphics());
        return offscreen;
    }

    /**
     * Выбрать файл #index
     *
     * @param index
     */
    public void select(int index)
    {
        scrSel = index;
        if (scrSel >= files.length)
            scrSel = files.length - 1;
        scrStart = scrSel - scrLen / 2;
        if (scrStart + scrLen >= files.length)
            scrStart = files.length - scrLen;
        if (scrStart < 0)
            scrStart = 0;
        repaint();
    }

    /**
     * Вернуть индекс следующего файла типа type.
     */
    int getNextOfType(int current, int type)
    {
        boolean notfound = true;
        int old = current;
        do
        {
            current++;
            if (current >= files.length)
                current = 1;
            if (Filesystem.fileType(files[current]) == type)
                break;
        } while (current != old);
        return current;
    }

    /**
     * Вернуть индекс предыдущего файла типа type.
     */
    int getPrevOfType(int current, int type)
    {
        //boolean notfound = true;
        int old = current;
        do
        {
            current--;
            if (current < 1)
                current = files.length - 1;
            if (Filesystem.fileType(files[current]) == type)
                break;
        } while (current != old);
        return current;
    }

    /**
     * Функция переключения между собственными полноэкранным и неполноэкранным
     * режимами.
     *
     * @param mode
     */
    public void initFSMode(boolean mode)
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
            scrLen = h / fileH;
            select(scrSel);
        }
    }

    /**
     * Функция отрисовки.
     *
     * @param g
     */
    protected void paint(Graphics g)
    {
        int i, fh, p, icon;
        g.setColor(Colors.back);
        g.setClip(0, 0, w, allh);
        g.fillRect(0, 0, w, allh);
        boolean inBuffer = "buf:/".equals(Main.currentPath);
        if (!fsmode) // рисуем интерфейс
        {
            g.setColor(Colors.fore);
            g.drawLine(0, 18, w, 18);
            g.drawLine(0, allh - 19, w, allh - 19);
            icon = Images.iFolder;
            if (title.endsWith(":/"))
            {
                icon = Images.iDisk;
                if (title.endsWith("4:/"))
                    icon = Images.iMMC;
            } else if (title.equals(Locale.Strings[Locale.SELECT_DRIVE]))
                icon = Images.iSieFM;
            else if (title.equals(Locale.Strings[Locale.FAVOURITE]))
                icon = Images.iFavorites;
            else if (title.equals(Locale.Strings[Locale.BUFFER]))
                icon = Images.iClipboard;
            Images.drawIcon(g, icon, 1, 1);
            // заглавие
            g.drawString(title, 19, 9 - g.getFont().getHeight() / 2, Graphics.LEFT | Graphics.TOP);
            // номер панели
            String cp = String.valueOf(curPanel + 1);
            g.setColor(Colors.back);
            g.fillRect(w - g.getFont().stringWidth(cp), 0, w, 17);
            g.setColor(Colors.fore1);
            g.drawString(cp, w, 9 - g.getFont().getHeight() / 2, Graphics.RIGHT | Graphics.TOP);
            // подписи к LSK, RSK и джойстику
            int lskstr = KeyConfigCanvas.keyConfig[KeyConfigCanvas.CONF_LSK],
                    rskstr = KeyConfigCanvas.keyConfig[KeyConfigCanvas.CONF_RSK];
            if (Main.currentPath == null || Main.currentPath.length() == 0)
            {
                if (lskstr == Locale.BACK_CMD)
                    lskstr = Locale.EXIT_CMD;
                if (rskstr == Locale.BACK_CMD)
                    rskstr = Locale.EXIT_CMD;
            }
            if (lskp)
            {
                g.setColor(Colors.selback);
                g.fillRect(0, allh - 18, 60, 18);
                g.setColor(Colors.selfore);
            } else
                g.setColor(Colors.fore);
            g.setClip(0, allh - 18, 60, 18);
            if (lskstr >= 0)
                g.drawString(Locale.Strings[lskstr], 30, allh - 1, Graphics.HCENTER | Graphics.BOTTOM);
            g.setClip(0, 0, w, allh);
            if (rskp)
            {
                g.setColor(Colors.selback);
                g.fillRect(w - 60, allh - 18, 60, 18);
                g.setColor(Colors.selfore);
            } else
                g.setColor(Colors.fore);
            g.setClip(w - 60, allh - 18, 60, 18);
            if (rskstr >= 0)
                g.drawString(Locale.Strings[rskstr], w - 31, allh - 1, Graphics.HCENTER | Graphics.BOTTOM);
            g.setClip(0, 0, w, allh);
            if (firep)
            {
                g.setColor(Colors.selback);
                g.fillRect(w / 2 - 9, allh - 18, 18, 18);
            }
            Images.drawIcon(g, Images.iSelect, w / 2 - 8, allh - 17);
        }
        g.translate(0, clntop);
        g.setClip(0, 0, w - 3, h);
        for (i = scrStart, p = 0; i < files.length && i < scrStart + scrLen + 1; i++, p++)
        {
            if (!exact[i])
            {
                updateFileType(i);
                exact[i] = true;
            }
            if (scrSel == i)
            {
                g.setColor(Colors.selback);
                g.fillRect(0, p * fileH, w, fileH);
                g.setColor(Colors.selfore);
            } else
                g.setColor(Colors.fore);
            if (markMode && marked[i])
                Images.drawIcon(g, Images.iMark, 1, p * fileH);
            else
                Images.drawIcon(g, types[i], 1, p * fileH);
            if (inBuffer && i > 0 && Buffer.move.get(i - 1) > 0)
                Images.drawIcon(g, Images.iMoveIt, 1, p * fileH);
            if (readonly[i])
            {
                fh = mfro.getHeight();
                g.setFont(mfro);
            } else
            {
                fh = mf.getHeight();
                g.setFont(mf);
            }
            g.drawString(files[i], 18, p * fileH + fileH / 2 - fh / 2, Graphics.LEFT | Graphics.TOP);
        }
        g.setClip(0, 0, w, h);
        g.setColor(Colors.fore);
        g.drawLine(w - 2, 0, w - 2, h);
        int sbstart = h * scrStart / files.length, sbsize = h * scrLen / files.length;
        if (sbsize > h)
            sbsize = h;
        g.drawLine(w - 1, sbstart, w - 1, sbstart + sbsize);
        g.drawLine(w - 3, sbstart, w - 3, sbstart + sbsize);
    }

    /**
     * Обработчик нажатий клавиш.
     *
     * @param keyCode
     */
    public void keyPressed(int keyCode)
    {
        int id = -1;
        if (keyCode == KEY_LEFT)
            id = KeyConfigCanvas.CONF_LEFT;
        else if (keyCode == KEY_RIGHT)
            id = KeyConfigCanvas.CONF_RIGHT;
        else if (keyCode == KEY_UP)
            id = KeyConfigCanvas.CONF_UP;
        else if (keyCode == KEY_DOWN)
            id = KeyConfigCanvas.CONF_DOWN;
        else if (keyCode == KEY_FIRE)
        {
            if (!fsmode && !Options.noEffects)
            {
                firep = true;
                repaint();
            } else
                id = KeyConfigCanvas.CONF_FIRE;
        } else
            switch (keyCode)
            {
                case KEY_STAR:
                    id = KeyConfigCanvas.CONF_STAR;
                    break;
                case KEY_POUND:
                    id = KeyConfigCanvas.CONF_POUND;
                    break;
                case KEY_DIAL:
                    id = KeyConfigCanvas.CONF_DIAL;
                    break;
                case KEY_CANCEL:
                    id = KeyConfigCanvas.CONF_CANCEL;
                    break;
                case KEY_LSK:
                    if (!fsmode && !Options.noEffects)
                    {
                        lskp = true;
                        repaint();
                    } else
                        id = KeyConfigCanvas.CONF_LSK;
                    break;
                case KEY_RSK:
                    if (!fsmode && !Options.noEffects)
                    {
                        rskp = true;
                        repaint();
                    } else
                        id = KeyConfigCanvas.CONF_RSK;
                    break;
                case KEY_NUM0:
                    id = KeyConfigCanvas.CONF_NUM;
                    break;
                case KEY_NUM1:
                    id = KeyConfigCanvas.CONF_NUM + 1;
                    break;
                case KEY_NUM2:
                    id = KeyConfigCanvas.CONF_NUM + 2;
                    break;
                case KEY_NUM3:
                    id = KeyConfigCanvas.CONF_NUM + 3;
                    break;
                case KEY_NUM4:
                    id = KeyConfigCanvas.CONF_NUM + 4;
                    break;
                case KEY_NUM5:
                    id = KeyConfigCanvas.CONF_NUM + 5;
                    break;
                case KEY_NUM6:
                    id = KeyConfigCanvas.CONF_NUM + 6;
                    break;
                case KEY_NUM7:
                    id = KeyConfigCanvas.CONF_NUM + 7;
                    break;
                case KEY_NUM8:
                    id = KeyConfigCanvas.CONF_NUM + 8;
                    break;
                case KEY_NUM9:
                    id = KeyConfigCanvas.CONF_NUM + 9;
                    break;
            }
        if (id >= 0)
        {
            Main.menu.parent = this;
            Main.menu.listen.menuAction(KeyConfigCanvas.keyConfig[id]);
        }
    }

    /**
     *
     * @param keyCode
     */
    public void directKeyPressed(int keyCode)
    {
        Main.currentFile = files[scrSel];
        if (keyCode == KEY_DOWN || keyCode == KEY_RIGHT)
        {
            if (keyCode == KEY_DOWN)
                scrSel++;
            else
                scrSel += scrLen;
            if (scrSel >= files.length)
                scrSel = 0;
            select(scrSel);
        } else if (keyCode == KEY_UP || keyCode == KEY_LEFT)
        {
            if (keyCode == KEY_UP)
                scrSel--;
            else
                scrSel -= scrLen;
            if (scrSel < 0)
                scrSel = files.length - 1;
            select(scrSel);
        }
    }

    /**
     * Выделение по нажатию кнопы "огонь", т.е джойстика.
     */
    public void fireSelectAction()
    {
        if (!markMode)
            selectFile();
        else // если идёт процесс выделения
        {
            markSelected();
            repaint();
        }
    }

    /**
     *
     */
    public String prevpath;

    /**
     * Обработчик отпусканий клавиш.
     *
     * @param keyCode
     */
    public void keyReleased(int keyCode)
    {
        if (fsmode)
            return;
        int id = -1;
        if (keyCode == KEY_LSK && lskp)
        {
            id = KeyConfigCanvas.CONF_LSK;
            lskp = false;
        } else if (keyCode == KEY_RSK && rskp)
        {
            id = KeyConfigCanvas.CONF_RSK;
            rskp = false;
        } else if (keyCode == KEY_FIRE && firep)
        {
            id = KeyConfigCanvas.CONF_FIRE;
            firep = false;
        }
        if (id >= 0)
        {
            Main.menu.parent = this;
            Main.menu.listen.menuAction(KeyConfigCanvas.keyConfig[id]);
        }
    }
    /**
     * Обработчик повторений клавиш.
     */
    int starHeld = 0;

    /**
     *
     * @param keyCode
     */
    public void keyRepeated(int keyCode)
    {
        if (keyCode == KEY_DOWN || keyCode == KEY_UP)
            keyPressed(keyCode);
        else if (keyCode == KEY_STAR)
        {
            starHeld++;
            if (starHeld > 3)
            {
                starHeld = 0;
                Main.menu.parent = this;
                (Main.menu.listen = new WorkingMenu(Main.menu)).menuAction(Locale.KEYBOARD_CONFIG_CMD);
            }
        }
    }

    /**
     * Получить последние n символов (для титула).
     *
     * @param path String
     * @param n int
     * @return String
     */
    public static String getLastPartOfString(String path, int n)
    {
        if (path.length() <= n)
            return path;
        else
            return (".." + path.substring(path.length() - n - 1, path.length()));
    }
}
