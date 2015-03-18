package filemanager; // ���������

import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.*;
import javax.microedition.media.*;
import javax.microedition.media.control.*;
import com.vmx.*;

public class cvsPlayer
       extends gkcCanvas
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
    
    public cvsPlayer ()
    {
        setFullScreenMode (true);
        w = getWidth ();
        h = getHeight ();
        w2 = w/2;
        h2 = h/2;
        infoFont = Font.getFont (Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL);
    }

    public void playSound (String soundFile, Displayable parent)
    {
        this.parent = parent;
        player = null;
        selectedIndex = main.FileSelect.scrSel;
        currentSoundFile = soundFile;
        // ������ ��� ����� ��� ���������
        OnlyFileName = currentSoundFile.substring (currentSoundFile.lastIndexOf ('/') + 1);
        try
        {
            tagstr = FontWidthCache.getCache(infoFont).insert_lf (id3Editor.getTagString (soundFile), w-8);
            player = Manager.createPlayer ("file:///" + soundFile);
            player.realize ();
            player.prefetch ();
            vc = (VolumeControl) player.getControl ("VolumeControl");
            vc.setLevel (options.volume);
            vc.setMute (options.muted);
            player.addPlayerListener (this);
            player.setLoopCount (-1);
            scrollPos = 0;
        }
        catch (Exception ioe)
        {
            running = false;
        }
        if (t == null)
        {
            t = new Thread (this);
            t.start ();
        }
        setLightOn();
    }
    
    protected void paint (Graphics g)
    {
        // ���������
        g.drawRegion (images.playerUI, 0, 0,  w2, 146,  0,  0, 0, Graphics.TOP | Graphics.LEFT);
        g.drawRegion (images.playerUI, 132 - w2, 0,  w2, 146,  0,  w - w2, 0, Graphics.TOP | Graphics.LEFT);
        g.drawRegion (images.playerUI, 0, 146,  w2, 30,  0,  0, h - 30, Graphics.TOP | Graphics.LEFT);
        g.drawRegion (images.playerUI, 132 - w2, 146,  w2, 30,  0,  w - w2, h - 30, Graphics.TOP | Graphics.LEFT);
        // ������ PLAY
        g.drawRegion (images.buttons, 0, 0, 21, 21, Sprite.TRANS_NONE, 7, h - 25, Graphics.LEFT | Graphics.TOP);
        // ������ STOP
        g.drawRegion (images.buttons, 42, 0, 21, 21, Sprite.TRANS_NONE, w - 28, h - 25, Graphics.LEFT | Graphics.TOP);
        g.setColor (0x000080);
        if (options.volume == 0) // ����� ������ ���������
        {
            images.drawIcon (g, images.iMute, w2 - 16, h - 23);
            g.fillRect (w2 + 4, h - 11, 10, 1);
        }
        else
        {
            if (!options.muted)
                images.drawIcon (g, images.iNoMute, w2-16, h-23);
            else
                images.drawIcon (g, images.iMute, w2-16, h-23);
            for (int i = 0; i < options.volume; i = i + 25)
                g.fillRect (w2 + 4, h - 12 - (i / 25) * 3, 10, 2);
        }
        g.setFont (infoFont);
        g.setColor (0x800000);
        if ((player.getState () == Player.PREFETCHED) && !paused)
            g.drawString(Locale.Strings[Locale.PLAYER_STOP], w2, h - h/4, Graphics.BOTTOM | Graphics.HCENTER);
        else if ((player.getState () == Player.PREFETCHED) && paused)
            g.drawString(Locale.Strings[Locale.PLAYER_PAUSE], w2, h - h/4, Graphics.BOTTOM | Graphics.HCENTER);
        if (player.getState () == Player.STARTED) // �����
        {
            g.drawRegion (images.buttons, 21, 0, 21, 21, Sprite.TRANS_NONE, 7, h - 25, Graphics.LEFT | Graphics.TOP);
            g.drawString(Locale.Strings[Locale.PLAYER_PLAY], w2, h - h/4, Graphics.BOTTOM | Graphics.HCENTER);
        }
        if (tagstr != null && tagstr.length () > 0)
            g.drawString (tagstr, 4, 20, Graphics.LEFT | Graphics.TOP);
        else
            g.drawRegion (images.playAnim, 0, animFrame*images.playAnim.getHeight()/4,
                images.playAnim.getWidth (), images.playAnim.getHeight()/4,
                Sprite.TRANS_NONE, w/2 - images.playAnim.getWidth ()/2, h/2 - images.playAnim.getHeight()/8,
                Graphics.LEFT | Graphics.TOP);
        g.setColor (0xFFFFFF);
        g.drawString (scrollText (OnlyFileName, 20), 6, 4, g.TOP | g.LEFT);
    }
    
    public void playerUpdate (Player player, String event, Object data)
    {
        if (event == PlayerListener.VOLUME_CHANGED)
            options.volume = vc.getLevel();
        /*if (event == PlayerListener.END_OF_MEDIA)
        {
            nextSound();
        }*/
    }
    
    public void run ()
    {
        running = true;
        playerStart ();
        while (running) // ����
        {
            if (player != null)
            {
                if (player.getState () == Player.STARTED)
                    animFrame = (animFrame+1)%4;
                repaint ();
                try
                {
                    Thread.sleep (300);
                } catch (java.lang.InterruptedException ie) {}
            }
        }
    }
    
    /**
     * ���������� ������� ������
     *
     * @param key int
     * @todo Implement this javax.microedition.lcdui.Canvas method
     */
    protected void keyPressed (int key)
    {
        // ������� ��� ������ ���� - �����
        if (key == KEY_CANCEL || key == KEY_RSK)
        {
            destroyPlayer ();
            // �������
            running = false;
            t = null;
            setLightOff();
            main.dsp.setCurrent (parent);
        }
        // ����� ���� ��� ������� - �����\���������.
        else if ((key == KEY_LSK) || (key == KEY_DIAL))
        {
            if (player.getState () == Player.STARTED)
                playerPause ();
            else if (player.getState () == Player.PREFETCHED)
                playerStart ();
        }
        // Volume up
        else if ((key == KEY_UP || key == KEY_NUM2) && player != null)
        {
            if ((options.volume <= 75) && !options.muted)
            {
                options.volume += 25;
                vc.setLevel (options.volume);
            }
        }
        // Volume down
        else if ((key == KEY_DOWN || key == KEY_NUM8) && player != null)
        {
            if ((options.volume >= 25) && !options.muted)
            {
                options.volume -= 25;
                vc.setLevel (options.volume);
            }
        }
        // Volume mute
        else if ((key == KEY_FIRE || key == KEY_NUM5) && player != null)
        {
            options.muted = !options.muted;
            vc.setMute (options.muted);
        }
        // ��������� ����
        else if ((key == KEY_RIGHT) || (key == KEY_NUM6))
        {
            destroyPlayer ();
            nextSound ();
        }
        // ���������� ����
        else if ((key == KEY_LEFT) || (key == KEY_NUM4))
        {
            destroyPlayer ();
            prevSound ();
        }
    }
    
    private void destroyPlayer ()
    {
        try
        {
            player.stop ();
            player.close ();
            player = null;
        }
        catch (Exception e)
        {
            //System.out.println ("Cannot stop and exit player");
            //e.printStackTrace ();
        }
    }
    /** ������� ������ */
    private void playerStop ()
    {
        try
        {
            paused = false;
            player.stop ();
            player.setMediaTime (0);
            repaint ();
        }
        catch (Exception e)
        {
            //System.out.println ("Cannot stop player");
            //e.printStackTrace ();
        }
    }
    /** ����� ������ */
    private void playerPause ()
    {
        try
        {
            player.stop ();
            paused = true;
            repaint ();
        }
        catch (Exception e)
        {
            //System.out.println ("Cannot pause player");
            //e.printStackTrace ();
        }
    }
    /** ������ ������ */
    private void playerStart ()
    {
        try
        {
            player.start ();
            paused = false;
            repaint ();
        }
        catch (Exception e)
        {
            //System.out.println ("Cannot start player");
            //e.printStackTrace ();
        }
    }
    /**
     * ��������� ������ TEXT (���-�� �������� COUNT)
     *
     * @param text String
     * @param count int
     * @return String
     */
    private String scrollText (String text, int count)
    {
        String tmp = text;
        int len = text.length ();
        int scr = len - count;
        if (len > count)
        {
            tmp = text.substring (scrollPos, scrollPos + count);
            if (scrolldirection)
                scrollPos--;
            else
                scrollPos++;
            if (scrollPos > scr)
            {
                scrollPos--;
                scrolldirection = !scrolldirection;
            }
            else if (scrollPos < 0)
            {
                scrollPos = 0;
                scrolldirection = !scrolldirection;
            }
        }
        return tmp;
    }
    /**
     * ��������� ����
     */
    private void nextSound ()
    {
        main.FileSelect.select (selectedIndex = main.FileSelect.getNextOfType (selectedIndex, filesystem.TYPE_SOUND));
        this.playSound (currentSoundFile = main.currentPath + main.FileSelect.files[main.FileSelect.scrSel], parent);
        playerStart ();
    }
    /**
     * ���������� ����
     */
    private void prevSound ()
    {
        main.FileSelect.select (selectedIndex = main.FileSelect.getPrevOfType (selectedIndex, filesystem.TYPE_SOUND));
        this.playSound (currentSoundFile = main.currentPath + main.FileSelect.files[main.FileSelect.scrSel], parent);
        playerStart ();
    }
}