package filemanager;

import javax.microedition.lcdui.*;
import com.vmx.*;

/**
 * Конфигуратор раскладки клавиатуры
 */
public class keyConfig
       extends gkcCanvas
       implements MenuListener
{
    protected MenuListener oldMML;
    protected FontWidthCache fch;
    protected Font fnt;
    int w, h;
    public String keyNames [];
    Displayable parent;
    int oneh, sel, start, screen;
    int header, footer;
    protected Image offscreen;
    /**
     * Конструктор
     */
    public keyConfig ()
    {
        parent = null;
        setFullScreenMode (true);
        w = getWidth ();
        h = getHeight ();
        fnt = Font.getFont (Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        fch = FontWidthCache.getCache (fnt);
        keyNames = new String [keyConfig.length];
        for (int i = 0; i < lockeyNames.length; i++)
            keyNames [i] = Locale.Strings[lockeyNames[i]];
        keyNames [CONF_POUND] = "#";
        keyNames [CONF_STAR] = "*";
        for (int i = 0; i < 10; i++)
            keyNames [CONF_NUM+i] = String.valueOf (i);
        sel = 0;
        oneh = 17;
        start = 0;
        header = 20;
        footer = 20;
        screen = (h-header-footer)/oneh;
        offscreen = Image.createImage (w, h);
    }
    /**
     * Отрисовка
     */
    protected void paint (Graphics g)
    {
        g.setColor (Colors.back);
        g.fillRect (0, 0, w, h);
        g.setColor (Colors.fore);
        g.drawLine (0, header-1, w, header-1);
        g.drawLine (0, h-footer, w, h-footer);
        g.drawString (Locale.Strings[Locale.SELECT_CMD], 30, h-1, Graphics.HCENTER|Graphics.BOTTOM);
        g.drawString (Locale.Strings[Locale.BACK_CMD], w-31, h-1, Graphics.HCENTER|Graphics.BOTTOM);
        images.drawIcon (g, images.iKey, 2, 2);
        g.drawString (Locale.Strings[Locale.KEYBOARD_CONFIG_CMD], 20, 10-g.getFont().getHeight()/2, Graphics.LEFT|Graphics.TOP);
        g.setFont (fnt);
        int act, gf2 = fnt.getHeight()/2;
        g.setClip (0, header, w-3, h-header-footer);
        for (int i = start; i < keyConfig.length && i < start+screen; i++)
        {
            if (i != sel)
                g.setColor (Colors.fore);
            else
            {
                g.setColor (Colors.selback);
                g.fillRect (0, header+(i-start)*oneh, w, 1+oneh);
                g.setColor (Colors.selfore);
            }
            act = keyConfig[i];
            if (act < 0)
                act = Locale.KEY_NO_ACTION;
            g.drawString (keyNames[i] + ": " + Locale.Strings[act], 1, header+(i-start)*oneh+9-gf2, Graphics.LEFT|Graphics.TOP);
        }
        g.setClip (0, 0, w, h);
        g.setColor (Colors.fore);
        g.drawLine (w-2, header, w-2, h-footer);
        int sbstart = header + (h-header-footer) * start/keyConfig.length,
            sbsize = (h-header-footer) * screen/keyConfig.length;
        if (sbsize > (h-header-footer))
            sbsize = (h-header-footer);
        g.drawLine (w-1, sbstart, w-1, sbstart+sbsize);
        g.drawLine (w-3, sbstart, w-3, sbstart+sbsize);
    }
    /**
     * Показать :)
     */
    public void show ()
    {
        oldMML = main.menu.listen;
        parent = main.menu.parent;
        main.dsp.setCurrent (this);
    }
    /**
     * Обработчик нажатий клавиш
     */
    public void keyPressed (int key)
    {
        if (key == KEY_DOWN)
            sel = (sel+1)%keyConfig.length;
        else if (key == KEY_UP)
            sel = (sel+keyConfig.length-1)%keyConfig.length;
        else if (key == KEY_FIRE || key == KEY_LSK)
        {
            paint (offscreen.getGraphics());
            main.menu.back = offscreen;
            main.menu.listen = this;
            main.menu.parent = this;
            main.menu.setType (cvsMenu.MENU_SELECT_ACTION);
            main.dsp.setCurrent (main.menu);
            return;
        }
        else if (key == KEY_CANCEL || key == KEY_RSK)
        {
            main.menu.listen = oldMML;
            if (parent != null)
                main.dsp.setCurrent (parent);
            return;
        }
        start = sel-screen/2;
        if (start < 0)
            start = 0;
        else if (start + screen > keyConfig.length)
            start = keyConfig.length - screen;
        repaint();
    }
    /**
     * Выбор команды меню
     */
    public void menuAction (int code)
    {
        if (code == Locale.KEY_NO_ACTION)
            code = -1;
        keyConfig [sel] = code;
        main.menu.ret ();
    }
    /**
     * Константы положений настроек клавиш в массиве
     */
    public static final int CONF_LSK = 0, CONF_RSK = 1, CONF_DIAL = 2,
            CONF_CANCEL = 3, CONF_UP = 4, CONF_DOWN = 5, CONF_LEFT = 6,
            CONF_RIGHT = 7, CONF_FIRE = 8, CONF_STAR = 9, CONF_POUND = 10,
            CONF_NUM = 11;
    /**
     * Массив конфигурации
     */
    public static int keyConfig [] =
    {
        Locale.OPTIONS_CMD,
        Locale.BACK_CMD,
        Locale.MARK_CMD,
        Locale.BACK_CMD,
        Locale.PREV_FILE_CMD,
        Locale.NEXT_FILE_CMD,
        Locale.PREV_SCREEN_CMD,
        Locale.NEXT_SCREEN_CMD,
        Locale.SELECT_CMD,
        -1,
        Locale.FULLSCREEN_CMD,
        -1,
        Locale.PANEL_NUMS,
        Locale.PANEL_NUMS+1,
        -1, -1, -1,
        -1, -1, -1, -1
    };
    /**
     * Имена клавиш из локали
     */
    protected static final int lockeyNames [] =
    {
        Locale.KEY_LSK, Locale.KEY_RSK, Locale.KEY_DIAL, Locale.KEY_CANCEL,
        Locale.KEY_UP, Locale.KEY_DOWN, Locale.KEY_LEFT, Locale.KEY_RIGHT,
        Locale.KEY_JOY
    };
}
