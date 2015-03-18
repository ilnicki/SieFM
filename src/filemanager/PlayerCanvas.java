package filemanager;

import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.*;
import javax.microedition.media.*;
import javax.microedition.media.control.*;
import com.vmx.*;

/**
 *
 * @author Dmytro
 */
public class PlayerCanvas
        extends KeyCodeCanvas
        implements PlayerListener, Runnable
{

    Displayable parent;
    private static Player player;
    VolumeControl vc;
    int animFrame = 0;
    int scrollPos = 0;
    boolean scrolldirection = false;
    boolean running, paused;
    Thread t = null;
    int selectedIndex;
    int w, h, w2, h2;
    String currentSoundFile, OnlyFileName, tagstr = null;
    Font infoFont;

    /**
     *
     */
    public PlayerCanvas()
    {
        setFullScreenMode(true);
        w = getWidth();
        h = getHeight();
        w2 = w / 2;
        h2 = h / 2;
        infoFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL);
    }

    /**
     *
     * @param soundFile
     * @param parent
     */
    public void playSound(String soundFile, Displayable parent)
    {
        this.parent = parent;
        player = null;
        selectedIndex = Main.FileSelect.scrSel;
        currentSoundFile = soundFile;
        // только имя файла для прокрутки
        OnlyFileName = currentSoundFile.substring(currentSoundFile.lastIndexOf('/') + 1);
        try
        {
            tagstr = FontWidthCache.getCache(infoFont).insert_lf(Id3EditorForm.getTagString(soundFile), w - 8);
            player = Manager.createPlayer("file:///" + soundFile);
            player.realize();
            player.prefetch();
            vc = (VolumeControl) player.getControl("VolumeControl");
            vc.setLevel(Options.volume);
            vc.setMute(Options.muted);
            player.addPlayerListener(this);
            player.setLoopCount(-1);
            scrollPos = 0;
        } catch (Exception ioe)
        {
            running = false;
        }
        if (t == null)
        {
            t = new Thread(this);
            t.start();
        }
        setLightOn();
    }

    /**
     *
     * @param g
     */
    protected void paint(Graphics g)
    {
        // Бэкграунд
        g.drawRegion(Images.playerUI, 0, 0, w2, 146, 0, 0, 0, Graphics.TOP | Graphics.LEFT);
        g.drawRegion(Images.playerUI, 132 - w2, 0, w2, 146, 0, w - w2, 0, Graphics.TOP | Graphics.LEFT);
        g.drawRegion(Images.playerUI, 0, 146, w2, 30, 0, 0, h - 30, Graphics.TOP | Graphics.LEFT);
        g.drawRegion(Images.playerUI, 132 - w2, 146, w2, 30, 0, w - w2, h - 30, Graphics.TOP | Graphics.LEFT);
        // Кнопка PLAY
        g.drawRegion(Images.buttons, 0, 0, 21, 21, Sprite.TRANS_NONE, 7, h - 25, Graphics.LEFT | Graphics.TOP);
        // Кнопка STOP
        g.drawRegion(Images.buttons, 42, 0, 21, 21, Sprite.TRANS_NONE, w - 28, h - 25, Graphics.LEFT | Graphics.TOP);
        g.setColor(0x000080);
        if (Options.volume == 0) // вывод значка громкости
        {
            Images.drawIcon(g, Images.iMute, w2 - 16, h - 23);
            g.fillRect(w2 + 4, h - 11, 10, 1);
        } else
        {
            if (!Options.muted)
                Images.drawIcon(g, Images.iNoMute, w2 - 16, h - 23);
            else
                Images.drawIcon(g, Images.iMute, w2 - 16, h - 23);
            for (int i = 0; i < Options.volume; i = i + 25)
                g.fillRect(w2 + 4, h - 12 - (i / 25) * 3, 10, 2);
        }
        g.setFont(infoFont);
        g.setColor(0x800000);
        if ((player.getState() == Player.PREFETCHED) && !paused)
            g.drawString(Locale.Strings[Locale.PLAYER_STOP], w2, h - h / 4, Graphics.BOTTOM | Graphics.HCENTER);
        else if ((player.getState() == Player.PREFETCHED) && paused)
            g.drawString(Locale.Strings[Locale.PLAYER_PAUSE], w2, h - h / 4, Graphics.BOTTOM | Graphics.HCENTER);
        if (player.getState() == Player.STARTED) // старт
        {
            g.drawRegion(Images.buttons, 21, 0, 21, 21, Sprite.TRANS_NONE, 7, h - 25, Graphics.LEFT | Graphics.TOP);
            g.drawString(Locale.Strings[Locale.PLAYER_PLAY], w2, h - h / 4, Graphics.BOTTOM | Graphics.HCENTER);
        }
        if (tagstr != null && tagstr.length() > 0)
            g.drawString(tagstr, 4, 20, Graphics.LEFT | Graphics.TOP);
        else
            g.drawRegion(Images.playAnim, 0, animFrame * Images.playAnim.getHeight() / 4,
                    Images.playAnim.getWidth(), Images.playAnim.getHeight() / 4,
                    Sprite.TRANS_NONE, w / 2 - Images.playAnim.getWidth() / 2, h / 2 - Images.playAnim.getHeight() / 8,
                    Graphics.LEFT | Graphics.TOP);
        g.setColor(0xFFFFFF);
        g.drawString(scrollText(OnlyFileName, 20), 6, 4, g.TOP | g.LEFT);
    }

    /**
     *
     * @param player
     * @param event
     * @param data
     */
    public void playerUpdate(Player player, String event, Object data)
    {
        if (event == PlayerListener.VOLUME_CHANGED)
            Options.volume = vc.getLevel();
        /*if (event == PlayerListener.END_OF_MEDIA)
         {
         nextSound();
         }*/
    }

    /**
     *
     */
    public void run()
    {
        running = true;
        playerStart();
        while (running) // цикл
        {
            if (player != null)
            {
                if (player.getState() == Player.STARTED)
                    animFrame = (animFrame + 1) % 4;
                repaint();
                try
                {
                    Thread.sleep(300);
                } catch (java.lang.InterruptedException ie)
                {
                }
            }
        }
    }

    /**
     * Обработчик нажатия кнопок.
     *
     * @param key int
     * @todo Implement this javax.microedition.lcdui.Canvas method
     */
    protected void keyPressed(int key)
    {
        // красная или правая софт - выход
        if (key == KEY_CANCEL || key == KEY_RSK)
        {
            destroyPlayer();
            // Возврат
            running = false;
            t = null;
            setLightOff();
            Main.dsp.setCurrent(parent);
        } // левая софт или зеленая - пауза\воспроизв.
        else if ((key == KEY_LSK) || (key == KEY_DIAL))
        {
            if (player.getState() == Player.STARTED)
                playerPause();
            else if (player.getState() == Player.PREFETCHED)
                playerStart();
        } // Volume up
        else if ((key == KEY_UP || key == KEY_NUM2) && player != null)
        {
            if ((Options.volume <= 75) && !Options.muted)
            {
                Options.volume += 25;
                vc.setLevel(Options.volume);
            }
        } // Volume down
        else if ((key == KEY_DOWN || key == KEY_NUM8) && player != null)
        {
            if ((Options.volume >= 25) && !Options.muted)
            {
                Options.volume -= 25;
                vc.setLevel(Options.volume);
            }
        } // Volume mute
        else if ((key == KEY_FIRE || key == KEY_NUM5) && player != null)
        {
            Options.muted = !Options.muted;
            vc.setMute(Options.muted);
        } // следующий звук
        else if ((key == KEY_RIGHT) || (key == KEY_NUM6))
        {
            destroyPlayer();
            nextSound();
        } // предыдущий звук
        else if ((key == KEY_LEFT) || (key == KEY_NUM4))
        {
            destroyPlayer();
            prevSound();
        }
    }

    private void destroyPlayer()
    {
        try
        {
            player.stop();
            player.close();
            player = null;
        } catch (Exception e)
        {
            //System.out.println ("Cannot stop and exit player");
            //e.printStackTrace ();
        }
    }

    /**
     * Останов плеера.
     */
    private void playerStop()
    {
        try
        {
            paused = false;
            player.stop();
            player.setMediaTime(0);
            repaint();
        } catch (Exception e)
        {
            //System.out.println ("Cannot stop player");
            //e.printStackTrace ();
        }
    }

    /**
     * Пауза плеера.
     */
    private void playerPause()
    {
        try
        {
            player.stop();
            paused = true;
            repaint();
        } catch (Exception e)
        {
            //System.out.println ("Cannot pause player");
            //e.printStackTrace ();
        }
    }

    /**
     * Запуск плеера.
     */
    private void playerStart()
    {
        try
        {
            player.start();
            paused = false;
            repaint();
        } catch (Exception e)
        {
            //System.out.println ("Cannot start player");
            //e.printStackTrace ();
        }
    }

    /**
     * Скроллинг текста TEXT (кол-во символов COUNT).
     *
     * @param text String
     * @param count int
     * @return String
     */
    private String scrollText(String text, int count)
    {
        String tmp = text;
        int len = text.length();
        int scr = len - count;
        if (len > count)
        {
            tmp = text.substring(scrollPos, scrollPos + count);
            if (scrolldirection)
                scrollPos--;
            else
                scrollPos++;
            if (scrollPos > scr)
            {
                scrollPos--;
                scrolldirection = !scrolldirection;
            } else if (scrollPos < 0)
            {
                scrollPos = 0;
                scrolldirection = !scrolldirection;
            }
        }
        return tmp;
    }

    /**
     * Следующий звук.
     */
    private void nextSound()
    {
        Main.FileSelect.select(selectedIndex = Main.FileSelect.getNextOfType(selectedIndex, Filesystem.TYPE_SOUND));
        this.playSound(currentSoundFile = Main.currentPath + Main.FileSelect.files[Main.FileSelect.scrSel], parent);
        playerStart();
    }

    /**
     * Предыдущий звук.
     */
    private void prevSound()
    {
        Main.FileSelect.select(selectedIndex = Main.FileSelect.getPrevOfType(selectedIndex, Filesystem.TYPE_SOUND));
        this.playSound(currentSoundFile = Main.currentPath + Main.FileSelect.files[Main.FileSelect.scrSel], parent);
        playerStart();
    }
}
