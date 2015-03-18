package filemanager; // переведен

import javax.microedition.lcdui.*;
import java.util.*;

public class alMessage
       extends Alert
       implements Runnable, CommandListener
{
    Thread t;
    boolean ok;
    
    public alMessage (String text)
    {
        super ("", "", null, null);
        if (text == null || text.equalsIgnoreCase ("")) // копирование переименование удаление без ошибок
        {
            //this.setImage (images.ok);
            this.setType (AlertType.INFO);
            this.setString (Locale.Strings[Locale.OPERATION_OK]);
            this.setTitle ("OK");
            ok = true;
        }
        else
        {
            this.setType (AlertType.ERROR);
            this.setString (text);
            ok = false;
        }
        this.addCommand (new Command (Locale.Strings[Locale.OK_CMD], Command.OK, 1));
        this.setCommandListener (this);
        t = new Thread (this);
        t.start ();
    }
    
    public void run ()
    {
        if (ok)
        {
            try
            {
                t.sleep (5000);
            } catch (InterruptedException ie) {}
            // переход
            main.FileSelect.showWait ();
        }
    }
    
    public void commandAction (Command command, Displayable displayable)
    {
        if (command.getCommandType () == Command.OK)
        {
            t.interrupt ();
            // прерываем таймер выше, там будет переход
            if (!ok)
                main.FileSelect.showWait ();
        }
    }
}
