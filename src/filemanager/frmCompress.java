package filemanager;

import javax.microedition.lcdui.*;
import java.util.Vector;

public class frmCompress
       extends Form
       implements CommandListener
{
    Displayable parent;
    TextField tf;
    Gauge gg;
    public frmCompress (Displayable p)
    {
        super (Locale.Strings[Locale.CREATE_ZIP]);
        parent = p;
        String cs = main.currentPath.substring (0, main.currentPath.length()-1);
        int pos;
        if ((pos = cs.lastIndexOf ('/')) >= 0)
            cs = cs.substring (pos+1);
        if ((pos = cs.lastIndexOf (':')) >= 0)
            cs = cs.substring (0, pos);
        cs += ".zip";
        this.append (tf = new TextField (Locale.Strings[Locale.FILE_NAME], cs, 1024, TextField.ANY));
        this.append (gg = new Gauge (Locale.Strings[Locale.COMPRESSION_LEVEL], true, 9, 9));
        setCommandListener (this);
        addCommand (new Command (Locale.Strings[Locale.CANCEL_CMD], Command.BACK, 1));
        addCommand (new Command (Locale.Strings[Locale.OK_CMD], Command.OK, 1));
        com.siemens.mp.lcdui.Displayable.setHeadlineIcon (this, images.getIcon (images.iPack));
    }
    /**
     * Обработчик команд
     */
    public void commandAction (Command c, Displayable d)
    {
        main.dsp.setCurrent (parent);
        if (c.getCommandType () == Command.OK)
            Buffer.zipFiles (main.currentPath + tf.getString (), gg.getValue ());
    }
}
