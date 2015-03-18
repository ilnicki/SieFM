package filemanager;

import javax.microedition.lcdui.*;
import java.io.IOException;
import com.vmx.*;

public class cvsSplash extends gkcCanvas implements Runnable
{
    Thread t;
    int i = 0, w, h;
    int backx, backy, aboutx, abouty;
    Image splashBack, splashAbout;
    /**
     * Конструктор
     */
    public cvsSplash ()
    {
        setFullScreenMode (true);
        w = getWidth ();
        h = getHeight ();
        t = new Thread (this);
        t.start ();
        splashBack = null;
        splashAbout = null;
        try
        {
            splashBack = Image.createImage ("/img/splash.png");
            if (splashBack.getWidth () > w)
                backx = 0;
            else backx = (w-splashBack.getWidth())/2;
            if (splashBack.getHeight () > h)
                backy = 0;
            else backy = (h-splashBack.getHeight())/2;
            aboutx = backx + splashBack.getWidth();
            abouty = backy + splashBack.getHeight();
            if (aboutx > w)
                aboutx = w;
            if (abouty > h)
                abouty = h;
            splashAbout = Image.createImage ("/img/about.png");
        } catch (IOException iox) {}
    }
    /**
     * Функция отрисовки
     */
    protected void paint (Graphics g)
    {
        g.setColor (0x000000);
        g.fillRect (0,0,w,h);
        if (splashBack != null)
            g.drawImage (splashBack, backx, backy, Graphics.LEFT|Graphics.TOP);
        if (splashAbout != null)
            g.drawImage (splashAbout, aboutx, abouty, Graphics.RIGHT|Graphics.BOTTOM);
        g.setColor (0xff0000);
        if (main.loadStage <= main.stagesCount && main.loadStage >= 0)
            g.fillRect (2, 2, (w-4)*main.loadStage/main.stagesCount, 3);
        else g.fillRect (2, 2, w-4, 3);
    }
    /**
     * Функция потока (ожидающая завершения по main.loadStage)
     */
    public void run ()
    {
        try
        {
            while (main.loadStage <= main.stagesCount)
            {
                t.sleep (100);
                repaint ();
            }
        } catch (Exception x) {}
        main.startUI ();
    }
}
