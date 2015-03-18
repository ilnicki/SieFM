package filemanager;

import javax.microedition.lcdui.*;
import com.vmx.*;

/**
 *
 * @author Dmytro
 */
public class MenuCanvas
        extends KeyCodeCanvas
{

    public Displayable parent;
    public Image back;
    public MenuListener listen;
    protected int type, w, h;
    protected int sel1, sel2;
    protected int mw1, mw2, mh1, mh2; // width & height для меню и подменю
    protected int mx1, mx2, my1, my2; // положение меню и подменю

    protected Font mf; // шрифт меню
    protected int mfh; // высота шрифта
    protected FontWidthCache mfwc; // кэш ширины шрифта %)))

    public boolean enabled[][];

    public static final int MENU_DISK_SELECTED = 0;
    public static final int MENU_FILE_SELECTED = 1;
    public static final int MENU_FOLDER_SELECTED = 2;
    public static final int MENU_DOTDOT_SELECTED = 3;
    public static final int MENU_FAVORITES_SELECTED = 4;
    public static final int MENU_INSIDE_ARCHIVE = 5;
    public static final int MENU_BUFFER_SELECTED = 6;
    public static final int MENU_SELECT_ACTION = 0x100;

    /**
     * Конструктор.
     */
    public MenuCanvas()
    {
        listen = null;
        parent = null;
        back = null;
        type = -1;
        sel1 = 0;
        sel2 = -1;
        setFullScreenMode(true);
        w = getWidth();
        h = getHeight();
        menu_length = menu_length_fix;
        mf = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        mfh = mf.getHeight();
        mfwc = FontWidthCache.getCache(mf);
        // Подсчёт требуемой ширины и высоты меню ...
        int i, j, tt;
        for (mw1 = 0, i = 0, tt = 0; i < menu.length; i++)
            if ((tt = mfwc.stringWidth(Locale.Strings[menu[i][0]])) > mw1)
                mw1 = tt;
        mw1 += 4;
        if (mw1 + 20 < w)
            mw1 += 20;
        mx1 = 7;
        mh1 = 2 + 17 * menu_length;
        my1 = h - mh1 - 7;
        // ... и подменю
        for (mw2 = 0, i = 0, tt = 0; i < menu.length; i++)
            for (j = 0; j < (menu[i].length - 1) / 2; j++)
                if ((tt = mfwc.stringWidth(Locale.Strings[menu[i][1 + j * 2]])) > mw2)
                    mw2 = tt;
        mw2 += 21;
        mh2 = -1;
        mx2 = 14;
        // а также создаём и заполняем enabled
        enabled = new boolean[menu.length][];
        for (i = 0; i < menu.length; i++)
        {
            enabled[i] = new boolean[1 + (menu[i].length - 1) / 2];
            for (j = 0; j < 1 + (menu[i].length - 1) / 2; j++)
                enabled[i][j] = true;
        }
        listen = new WorkingMenu(this);
    }

    /**
     * Смена типа меню (затемнение ненужных элементов).
     *
     * @param type
     */
    public void setType(int type)
    {
        this.type = type;
        sel1 = 0;
        sel2 = -1;
        if (type >= MENU_DISK_SELECTED && type <= MENU_BUFFER_SELECTED)
        {
            menu_length = menu_length_fix;
            mh1 = 2 + 17 * menu_length;
            my1 = h - mh1 - 7;
            for (int i = 0; i < menu_length; i++)
                System.arraycopy(enabledModes[type][i], 0, enabled[i], 0, enabledModes[type][i].length);
            if (Buffer.buf.size() > 0)
            {
                if (type == MENU_FILE_SELECTED
                        || type == MENU_FOLDER_SELECTED
                        || type == MENU_DOTDOT_SELECTED)
                {
                    enabled[3][1] = true; //???//
                    enabled[4][3] = true;
                }
                enabled[5][1] = true;
            }
            if (type == MENU_FILE_SELECTED && Main.currentFile.toLowerCase().endsWith(".mp3"))
                enabled[0][5] = true;
            for (int i = 0; i < enabled.length; i++)
            {
                enabled[i][0] = false;
                for (int j = 1; j < enabled[i].length; j++)
                    if (enabled[i][j])
                    {
                        enabled[i][0] = true;
                        break;
                    }
            }
            if (type == MENU_DISK_SELECTED && Locale.Strings[Locale.FAVOURITE].equals(Main.currentFile))
            {
                enabled[2][0] = false;
                enabled[3][0] = false;
            }
        } else if (type == MENU_SELECT_ACTION)
        {
            menu_length = menu.length;
            mh1 = 2 + 17 * menu_length;
            my1 = h - mh1 - 7;
            for (int i = 0; i < menu.length; i++)
                for (int j = 0; j < 1 + (menu[i].length - 1) / 2; j++)
                    enabled[i][j] = true;
        }
    }

    /**
     * функция отрисовки.
     *
     * @param g
     */
    public void paint(Graphics g)
    {
        if (back != null)
            g.drawImage(back, 0, 0, Graphics.LEFT | Graphics.TOP);
        g.setColor(Colors.back);
        g.fillRect(mx1, my1, mw1, mh1);
        g.setColor(Colors.border);
        g.drawRect(mx1, my1, mw1, mh1);
        g.setFont(mf);
        // Отрисовка меню
        for (int i = 0; i < menu_length; i++)
        {
            if (i == sel1)
            {
                g.setColor(Colors.selback);
                g.fillRect(mx1 + 1, my1 + 1 + i * 17, mw1 - 1, 18);
                g.setColor(Colors.selfore);
            } else
                g.setColor(Colors.fore);
            if (!enabled[i][0])
                g.setColor(Colors.disabled);
            g.drawString(Locale.Strings[menu[i][0]] + " >", mx1 + 2, my1 + 2 + i * 17 + 8 - mfh / 2, Graphics.LEFT | Graphics.TOP);
        }
        // Если отображено подменю - рисуем его
        if (sel2 >= 0)
        {
            int m2l = (menu[sel1].length - 1) / 2;
            mh2 = 2 + m2l * 17;
            my2 = my1 + 2 + sel1 * 17 + 8;
            if (my2 + mh2 > h - 14)
                my2 = h - mh2 - 14;
            if (my2 < 0)
                my2 = 0;
            g.setColor(Colors.back);
            g.fillRect(mx2, my2, mw2, mh2);
            g.setColor(Colors.border);
            g.drawRect(mx2, my2, mw2, mh2);
            for (int i = 0; i < m2l; i++)
            {
                if (i == sel2)
                {
                    g.setColor(Colors.selback);
                    g.fillRect(mx2 + 1, my2 + 1 + i * 17, mw2 - 1, 18);
                    g.setColor(Colors.selfore);
                } else
                    g.setColor(Colors.fore);
                if (!enabled[sel1][i + 1])
                    g.setColor(Colors.disabled);
                Images.drawIcon(g, menu[sel1][i * 2 + 2], mx2 + 2, my2 + 2 + i * 17);
                g.drawString(Locale.Strings[menu[sel1][i * 2 + 1]], mx2 + 19, my2 + 2 + i * 17 + 8 - mfh / 2, Graphics.LEFT | Graphics.TOP);
            }
        }
    }

    /**
     * Функция возвращения к предыдущему экрану (к parent).
     */
    public void ret()
    {
        if (parent != null)
        {
            if (Main.dsp.getCurrent() != parent)
                Main.dsp.setCurrent(parent);
            parent = null;
        }
    }

    /**
     * Обработчик нажатий клавиш.
     *
     * @param keyCode
     */
    protected void keyPressed(int keyCode)
    {
        int osel, ml;
        if (keyCode == KEY_DOWN)
        {
            if (sel2 == -1) // гуляем по меню
            {
                osel = sel1;
                ml = menu_length;
                do
                {
                    sel1 = (sel1 + 1) % ml;
                } while (!enabled[sel1][0] && sel2 != osel);
            } else // гуляем по подменю
            {
                osel = sel2;
                ml = (menu[sel1].length - 1) / 2;
                do
                {
                    sel2 = (sel2 + 1) % ml;
                } while (!enabled[sel1][1 + sel2] && sel2 != osel);
            }
            repaint();
        } else if (keyCode == KEY_UP)
        {
            if (sel2 == -1) // гуляем по меню
            {
                osel = sel1;
                ml = menu_length;
                do
                {
                    sel1 = (sel1 - 1 + ml) % ml;
                } while (!enabled[sel1][0] && sel2 != osel);
            } else // гуляем по подменю
            {
                osel = sel2;
                ml = (menu[sel1].length - 1) / 2;
                do
                {
                    sel2 = (sel2 - 1 + ml) % ml;
                } while (!enabled[sel1][1 + sel2] && sel2 != osel);
            }
            repaint();
        } else if ((!isS75 && keyCode == KEY_LSK)
                || (isS75 && keyCode == KEY_RSK) || keyCode == KEY_LEFT
                || keyCode == KEY_CANCEL)
        {
            if (sel2 == -1)
                ret();
            else
            {
                sel2 = -1;
                repaint();
            }
        } else if (keyCode == KEY_FIRE || keyCode == KEY_RIGHT
                || (isS75 && keyCode == KEY_LSK)
                || (!isS75 && keyCode == KEY_RSK))
        {
            if (sel2 == -1)
            {
                sel2 = 0;
                while (!enabled[sel1][1 + sel2])
                    sel2++;
                repaint();
            } else if (listen != null)
                listen.menuAction(menu[sel1][1 + sel2 * 2]);
        }
    }

    /**
     * Обработчик повторений клавиш.
     *
     * @param keyCode
     */
    protected void keyRepeated(int keyCode)
    {
        if (keyCode == KEY_DOWN || keyCode == KEY_UP)
            keyPressed(keyCode);
    }
    /**
     * Данные меню.
     */
    static final int menu[][] =
    {
        {
            Locale.MENU_FILE,
            Locale.OPEN_CMD, Images.iNext,
            Locale.MARK_CMD, Images.iMark,
            Locale.MARK_ALL_CMD, Images.iMarkAll,
            Locale.DEMARK_ALL_CMD, Images.iDemarkAll,
            Locale.EDIT_ID3_CMD, Images.iMelody
        },
        {
            Locale.MENU_ARCHIVE,
            Locale.EXTRACT_CMD, Images.iUnpack,
            Locale.EXTRACT_ALL_CMD, Images.iUnpack
        },
        {
            Locale.MENU_PROPERTIES,
            Locale.PROPERTY_CMD, Images.iProperties,
            Locale.DISK_INFO_CMD, Images.iDisk
        },
        {
            Locale.MENU_OPERATIONS,
            Locale.INSERT_CMD, Images.iPaste,
            Locale.COPY_CMD, Images.iCopy,
            Locale.MOVE_CMD, Images.iMove,
            Locale.DELETE_CMD, Images.iDelete,
            Locale.RENAME_CMD, Images.iRename,
            Locale.TO_FAVOUR_CMD, Images.iFavorites
        },
        {
            Locale.MENU_CREATE,
            Locale.NEW_FILE_CMD, Images.iFile,
            Locale.NEW_FOLDER_CMD, Images.iFolder,
            Locale.CREATE_ZIP, Images.iPack
        },
        {
            Locale.MENU_SHOW,
            Locale.BUFFER, Images.iClipboard,
            Locale.FAVOURITE, Images.iFavorites,
            Locale.HELP_CMD, Images.iHelp,
            Locale.PREFERENCES_CMD, Images.iOptions,
            Locale.KEYBOARD_CONFIG_CMD, Images.iKey,
            Locale.EXIT_CMD, Images.iExit
        },
        {
            Locale.MENU_ADDITIONAL,
            Locale.KEY_NO_ACTION, Images.iMoveIt,
            Locale.OPTIONS_CMD, Images.iMenu,
            Locale.SELECT_CMD, Images.iSelect,
            Locale.PREV_FILE_CMD, -1,
            Locale.NEXT_FILE_CMD, -1,
            Locale.PREV_SCREEN_CMD, -1,
            Locale.NEXT_SCREEN_CMD, -1,
            Locale.FULLSCREEN_CMD, -1,
            Locale.UP_LEVEL_CMD, Images.iUp
        },
        {
            Locale.PANELS,
            Locale.PANEL_NUMS + 0, -1,
            Locale.PANEL_NUMS + 1, -1,
            Locale.PANEL_NUMS + 2, -1,
            Locale.PANEL_NUMS + 3, -1,
            Locale.PANEL_NUMS + 4, -1,
            Locale.PANEL_NUMS + 5, -1,
            Locale.PANEL_NUMS + 6, -1,
            Locale.PANEL_NUMS + 7, -1,
            Locale.PANEL_NUMS + 8, -1,
            Locale.PANEL_NUMS + 9, -1
        }
    };
    static int menu_length = 6;
    static final int menu_length_fix = 6;
    /* Данные enabled режимов меню */
    static final boolean enabledModes [][][] =
    {
        { // выбран диск
            { false, true, false, false, false, false },
            { false, false, false },
            { false, false, true },
            { false, false, false, false, false, false, true },
            { false, false, false, false },
            { false, false, true, true, true, true, true }
        },
        { // выбран файл
            { false, true, true, true, true, false },
            { false, false, false },
            { false, true, true },
            { false, false, true, true, true, true, false },
            { false, true, true, false },
            { false, false, true, true, true, true, true }
        },
        { // выбрана папка
            { false, true, true, true, true, false },
            { false, false, false },
            { false, true, true },
            { false, false, true, true, true, true, true },
            { false, true, true, false },
            { false, false, true, true, true, true, true }
        },
        { // выбрано ..
            { false, true, false, true, true, false },
            { false, false, false },
            { false, true, true },
            { false, false, false, false, false, false, true },
            { false, true, true, false },
            { false, false, true, true, true, true, true }
        },
        { // находимся в избранном
            { false, true, true, true, true, false },
            { false, false, false },
            { false, true, false },
            { false, false, false, false, true, false, false },
            { false, false, false, false },
            { false, false, true, true, true, true, true }
        },
        { // находимся в архиве
            { false, true, true, true, true, false },
            { false, true, true },
            { false, true, true },
            { false, false, false, false, false, false, false },
            { false, false, false, false },
            { false, false, true, true, true, true, true }
        },
        { // находимся в буфере обмена
            { false, true, true, true, true, false },
            { false, false, false },
            { false, false, false },
            { false, false, false, false, true, false, false },
            { false, false, false, false },
            { false, false, true, true, true, true, true }
        }
    };
}
