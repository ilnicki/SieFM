package filemanager; // переведен

import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.Sprite;
import java.util.*;
import com.vmx.*;

class waitTimerTask extends TimerTask
{
    cvsWait parent;
    public waitTimerTask (cvsWait p)
    {
        parent = p;
    }
    public void run ()
    {
        parent.repaint ();
    }
};

/** Пишем "Пожалуйста, ждите..." и читаем список файлов для FileSelect */
public class cvsWait extends gkcCanvas implements Runnable
{
    Thread t;
    Timer timer;
    waitTimerTask timerTask;
    int counter = 0;
    String selectAfter;
    public Image back;
    /**
     * Пустой конструктор
     */
    public cvsWait () {}
    /**
     * start для случая, когда после загрузки списка
     * файлов надо выбрать первый файл
     */
    public static cvsWait start ()
    {
        main.wait.selectAfter = null;
        main.wait.init ();
        return main.wait;
    }
    /**
     * start для случая, когда после загрузки списка
     * указывается то, какой файл надо выбрать
     */
    public static cvsWait start (String selectAfter)
    {
        main.wait.selectAfter = selectAfter;
        main.wait.init ();
        return main.wait;
    }
    /** Общая часть конструктора */
    public void init ()
    {
        back = null;
        setFullScreenMode (true);
        if (t != null && t.isAlive ())
        {
            t.interrupt ();
            t = null;
        }
        if (timer != null)
        {
            timer.cancel ();
            timer = null;
        }
        timerTask = new waitTimerTask (this);
        timer = new Timer ();
        timer.schedule (timerTask, 0, 100);
        t = new Thread (this);
        t.start ();
    }
    /** Функция потока */
    public void run ()
    {
        String err = null;
        boolean erred = false;
        try
        {
            if (!options.noEffects)
                main.dsp.setCurrent (this);
            main.FileSelect.list (selectAfter);
            timer.cancel ();
        }
        catch (Exception x)
        {
            //x.printStackTrace ();
            err = x.getMessage ();
            erred = true;
            timer.cancel ();
            timer = null;
        }
        finally
        {
            if (erred)
            {
                Alert al = new Alert ("Error", err, null, AlertType.ERROR); 
                        //images.error, null);
                al.setTimeout (3000);
                main.currentPath = main.FileSelect.prevpath;
                main.dsp.setCurrent (al, main.FileSelect);
            }
            else if (!options.noEffects || main.dsp.getCurrent () != main.FileSelect)
                main.dsp.setCurrent (main.FileSelect);
        }
    }
    /** Функция отрисовки */
    protected void paint (Graphics gWait)
    {
        if (back != null)
            gWait.drawImage (back, 0, 0, Graphics.LEFT|Graphics.TOP);
        else
        {
            gWait.setColor (Colors.back);
            gWait.fillRect (0, 0, this.getWidth (), this.getHeight ());
        }
        gWait.setColor (Colors.back);
        gWait.fillRoundRect (this.getWidth () / 2 - 60, this.getHeight () / 2 - 42, 120, 84, 8, 8);
        gWait.setColor (Colors.border);
        gWait.drawRoundRect (this.getWidth () / 2 - 60, this.getHeight () / 2 - 42, 120, 84, 8, 8);
        gWait.drawRegion (images.waitAnim, 32*counter, 0, 32, 32, Sprite.TRANS_NONE, this.getWidth () / 2 - 55, this.getHeight () / 2, 6);
        gWait.setFont (Font.getFont (0, 1, 0));
        gWait.translate (this.getWidth () / 2 - 25, this.getHeight () / 2 - 14);
        gWait.drawString (Locale.Strings[Locale.WAIT_PLEASE], 0, 0, 20);
        counter = (counter+1)%4;
    }
}
