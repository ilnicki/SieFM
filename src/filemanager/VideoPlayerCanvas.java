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
public class VideoPlayerCanvas
        extends KeyCodeCanvas
        implements PlayerListener, Runnable
{

    Displayable parent;
    private static Player player;
    private boolean enableUI = true;
    VolumeControl volumeControl;
    VideoControl videoControl;
    boolean running;
    boolean needRepaint;
    boolean drawWaitImage;
    Thread t = null;
    String currentVideoFile;
    int selectedIndex;
    int w, h, w2, h2;
    Font infoFont;
    int scrollPos = 0;
    boolean scrolldirection = false;

    /**
     * Конструктор.
     */
    public VideoPlayerCanvas()
    {
        setFullScreenMode(true);
        w = getWidth();
        h = getHeight();
        w2 = w / 2;
        h2 = h / 2;
        infoFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL);
    }

    /**
     * Обновление данных в соответствии с состоянием плеера.
     *
     * @param player
     * @param event
     * @param data
     */
    public void playerUpdate(Player player, String event, Object data)
    {
        //System.out.println ("EVENT =" + event);
        if (event == PlayerListener.VOLUME_CHANGED)
        {
            Options.volume = volumeControl.getLevel();
            //System.out.println ( "Volume =" + Options.volume );
            needRepaint = enableUI;
            repaint();
        } else if (event == PlayerListener.END_OF_MEDIA)
        {
            try
            {
                player.stop();
            } catch (Exception e)
            {
                //System.out.println ("Cannot stop player");
                //e.printStackTrace ();
            }
            long time = System.currentTimeMillis();
            while ((time + 1000L) > System.currentTimeMillis());
            playerStart();
        }
    }

    /**
     * Играть файл videoFile.
     *
     * @param videoFile
     * @param parent
     */
    public void playVideo(String videoFile, Displayable parent)
    {
        this.parent = parent;
        currentVideoFile = videoFile;
        selectedIndex = Main.FileSelect.scrSel;
        player = null;
        running = false;
        needRepaint = true;
        repaint();
        try
        {
            player = Manager.createPlayer("file:///" + videoFile);
            if (player != null)
            {
                player.realize();
                player.prefetch();
                videoControl = (VideoControl) player.getControl("VideoControl");
                if (videoControl != null)
                {
                    videoControl.initDisplayMode(VideoControl.USE_DIRECT_VIDEO, this);
                    //main.dsp.setCurrent ((Displayable)videoControl.initDisplayMode (VideoControl.USE_GUI_PRIMITIVE, null));
                    setDisplay();
                }
                volumeControl = (VolumeControl) player.getControl("VolumeControl");
                if (volumeControl != null)
                {
                    volumeControl.setLevel(Options.volume);
                    volumeControl.setMute(Options.muted);
                }
                player.addPlayerListener(this);
            }
        } catch (Exception ioe)
        {
            System.out.println("Error while loading file " + videoFile);
            running = false;
        }
        if (t == null && !running)
        {
            t = new Thread(this);
            t.start();
        }
        //playerStart ();
        setLightOn();
    }

    /**
     * Функция отрисовки.
     *
     * @param g
     */
    protected void paint(Graphics g)
    {
        if (drawWaitImage)
        {
            g.setColor(0x000000);
            g.fillRect(0, 0, w, h);
            g.drawRegion(Images.waitAnim, 0, 0, 32, 32, Sprite.TRANS_NONE, w / 2 - 16, h / 2 - 16, Graphics.LEFT | Graphics.TOP);
        } else
        {
            if (needRepaint)
            {
                if (!running)
                {
                    g.setColor(0x000000);
                    g.fillRect(0, 0, w, h);
                    running = true;
                }
                if (enableUI)
                {
                    g.setColor(0x000000);
                    g.fillRect(0, 0, w, h);
                    g.drawRegion(Images.playerUI, 0, 146, w2, 30, 0, 0, h - 30, Graphics.LEFT | Graphics.TOP);
                    g.drawRegion(Images.playerUI, 132 - w2, 146, w2, 30, 0, w - w2, h - 30, Graphics.LEFT | Graphics.TOP);
                    g.drawRegion(Images.buttons, 42, 0, 21, 21, Sprite.TRANS_NONE, w - 28, h - 25, Graphics.LEFT | Graphics.TOP);
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
                        g.setColor(0x000080);
                        for (int i = 0; i < Options.volume; i = i + 25)
                            g.fillRect(w2 + 4, h - 12 - (i / 25) * 3, 10, 2);
                    }
                } else
                {
                    g.setColor(0x000000);
                    g.fillRect(0, 0, w, h);
                }
                needRepaint = false;
            }
            if (enableUI)
            {
                if (h > 130)
                {
                    g.drawRegion(Images.playerUI, 0, 0, w2, 17, 0, 0, 0, Graphics.LEFT | Graphics.TOP);
                    g.drawRegion(Images.playerUI, 132 - w2, 0, w2, 17, 0, w - w2, 0, Graphics.LEFT | Graphics.TOP);
                }
                g.setFont(infoFont);
                g.setColor(0xFFFFFF);
                g.drawString(scrollText(currentVideoFile.substring(currentVideoFile.lastIndexOf('/') + 1), 20),
                        6, 4, g.TOP | g.LEFT);
            }
        }
    }

    /**
     * Установить параметры места для отображения видео.
     */
    public void setDisplay()
    {
        if (enableUI)
            videoControl.setDisplayLocation(w / 2 - 64, (h > 130 ? h / 2 - 48 : 0));
        else
            videoControl.setDisplayLocation(w / 2 - 64, h / 2 - 48);
        try
        {
            videoControl.setDisplaySize(128, 96);
        } catch (Exception e)
        {
        }
        videoControl.setVisible(true);
    }

    /**
     * Функция потока.
     */
    public void run()
    {
        if (player != null)
        {
            if (player.getState() == Player.PREFETCHED)
            {
                try
                {
                    player.start(); // стартуем плейер
                } catch (Exception e)
                {
                    //System.out.println ("Cannot start player");
                    //e.printStackTrace ();
                }
            }
            while (running) // цикл
            {
                try
                {
                    Thread.sleep(500);
                    repaint();
                } catch (Exception e)
                {
                    //e.printStackTrace ();
                }
            }
        }
    }

    /**
     * Обработчик нажатия кнопок.
     *
     * @param keyCode int
     */
    protected void keyPressed(int keyCode)
    {
        if (keyCode == KEY_POUND) //#
        {
            enableUI = !enableUI;
            needRepaint = true;
            setDisplay();
            videoControl.setVisible(true);
        } // красная выход, но на S75 - не катит. длинное нажатие правой софт тоже выход
        else if ((keyCode == KEY_CANCEL) || (keyCode == KEY_RSK))
        {
            if (player.getState() == Player.STARTED)
                try
                {
                    player.stop();
                } catch (Exception x)
                {
                }
            drawWaitImage = true;
            repaint();
            serviceRepaints();
            destroyPlayer(false);
            // Возврат
            setLightOff();
            //com.siemens.mp.lcdui.Graphics.setLightOff ();
            t = null;
            Main.dsp.setCurrent(parent);
            drawWaitImage = false;
        } // левая софт или зеленая - пауза/воспроизведение
        else if (((keyCode == KEY_LSK) || (keyCode == KEY_DIAL)) && (player.getState() == Player.PREFETCHED))
            playerStart();
        // Volume up
        else if (keyCode == KEY_UP || keyCode == KEY_NUM2)
        {
            if ((Options.volume < 100) && !Options.muted)
            {
                if (player.getState() == Player.STARTED)
                {
                    playerStop();
                    volumeControl.setLevel(Options.volume + 25);
                    playerStart();
                } else
                    volumeControl.setLevel(Options.volume + 25);
            }
        } // Volume down
        else if (keyCode == KEY_DOWN || keyCode == KEY_NUM8)
        {
            if ((Options.volume > 0) && !Options.muted)
            {
                if (player.getState() == Player.STARTED)
                {
                    playerStop();
                    volumeControl.setLevel(Options.volume - 25);
                    playerStart();
                } else
                    volumeControl.setLevel(Options.volume - 25);
            }
        } // Mute
        else if (keyCode == KEY_FIRE || keyCode == KEY_NUM5)
        {
            Options.muted = !Options.muted;
            if (player.getState() == Player.STARTED)
            {
                playerStop();
                volumeControl.setMute(Options.muted);
                playerStart();
            } else
                volumeControl.setMute(Options.muted);
        } // следующий клип
        else if ((keyCode == KEY_RIGHT) || (keyCode == KEY_NUM6))
        {
            destroyPlayer();
            nextClip();
        } // предыдущ клип
        else if ((keyCode == KEY_LEFT) || (keyCode == KEY_NUM4))
        {
            destroyPlayer();
            prevClip();
        }
    }

    /**
     * Уничтожение плеера с предварительным остановом.
     */
    private void destroyPlayer()
    {
        destroyPlayer(true);
    }

    /**
     * Уничтожение плеера (если alsoStop = true - сначала останов).
     */
    private void destroyPlayer(boolean alsoStop)
    {
        if (player == null)
            return;
        try
        {
            if (alsoStop && player.getState() == Player.STARTED)
                player.stop();
            player.deallocate();
            player.close();
            player = null;
        } catch (Exception e)
        {
            //player = null;
            //System.out.println ("Cannot stop and exit player");
            //e.printStackTrace ();
        }
    }

    /**
     * Останов плеера.
     */
    private void playerStop()
    {
        if (player == null)
            return;
        try
        {
            player.stop();
            player.setMediaTime(0);
        } catch (Exception e)
        {
            //System.out.println ("Cannot stop player");
            //e.printStackTrace ();
        }
    }

    /**
     * Запуск плеера.
     */
    private void playerStart()
    {
        if (player == null)
            return;
        try
        {
            player.start();
        } catch (Exception e)
        {
            //System.out.println ("Cannot start player");
            //e.printStackTrace ();
        }
    }

    /**
     * Перейти к следующему видеофайлу.
     */
    private void nextClip()
    {
        Main.FileSelect.select(selectedIndex = Main.FileSelect.getNextOfType(selectedIndex, Filesystem.TYPE_VIDEO));
        currentVideoFile = Main.currentPath + Main.FileSelect.files[selectedIndex];
        this.playVideo(currentVideoFile, parent);
        playerStart();
    }

    /**
     * Перейти к предыдущему видеофайлу.
     */
    private void prevClip()
    {
        Main.FileSelect.select(selectedIndex = Main.FileSelect.getPrevOfType(selectedIndex, Filesystem.TYPE_VIDEO));
        currentVideoFile = Main.currentPath + Main.FileSelect.files[selectedIndex];
        this.playVideo(currentVideoFile, parent);
        playerStart();
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
}
