package filemanager;

import javax.microedition.lcdui.*;
import java.util.Vector;

public class frmExtractTo
       extends Form
       implements CommandListener
{
    Displayable parent;
    TextField tf;
    String FolderName;
    Vector vs;
    protected boolean blocked;
    
    public frmExtractTo (Displayable parent, Vector v)
    {
        super (Locale.Strings[Locale.EXTRACT_TO]);
        blocked = false;
        this.parent = parent;
        String cs = main.currentPath;
        int zip = filesystem.divideZipName (cs);
        if (zip >= 0)
            cs = cs.substring (0, zip-5) + "/";
        vs = v;
        tf = new TextField (Locale.Strings[Locale.EXTRACT_TO], cs, 1024, TextField.ANY);
        this.append (tf);
        setCommandListener (this);
        addCommand (new Command (Locale.Strings[Locale.CANCEL_CMD], Command.BACK, 1));
        addCommand (new Command (Locale.Strings[Locale.OK_CMD], Command.OK, 1));
        com.siemens.mp.lcdui.Displayable.setHeadlineIcon (this, images.getIcon (images.iUnpack));
    }
    
    public void commandAction (Command command, Displayable displayable)
    {
        if (blocked)
            return;
        if (command.getCommandType () == Command.BACK)
            main.dsp.setCurrent (parent);
        else
        {
            if (command.getCommandType () == Command.OK)
            {
                String dir = tf.getString ();
                if (dir.length () > 0 && dir.charAt (dir.length()-1) != '/')
                    dir = dir + '/';
                if (!filesystem.isFileExist (dir))
                    filesystem.makeNewDir (dir);
                if (filesystem.isFileExist (dir)) // папка есть, распаковываем
                {
                    Alert al = new Alert (Locale.Strings[Locale.WAIT],
                            Locale.Strings[Locale.WAIT_PLEASE],
                            null, AlertType.WARNING);
                            //images.warn, null);
                    al.setTimeout (al.FOREVER);
                    main.dsp.setCurrent (al, this);
                    Gauge gg = new Gauge ((String)null, false, (int)filesystem.getInZipSize (vs, false), 0);
                    al.setIndicator (gg);
                    blocked = true;
                    filesystem.unpackZip (vs, dir, gg);
                    blocked = false;
                    al = new Alert (Locale.Strings[Locale.OK_CMD],
                            Locale.Strings[Locale.FILES_EXTRACTED],
                            null, AlertType.INFO);
                            //images.ok, null);
                    al.setTimeout (3000);
                    main.dsp.setCurrent (al, parent);
                }
                else
                {
                    Alert al = new Alert (Locale.Strings[Locale.ERROR],
                            Locale.Strings[Locale.FOLDER_IMPOSSIBLE],
                            null, AlertType.ERROR);
                            //images.error, null);
                    al.setTimeout (3000);
                    main.dsp.setCurrent (al, this);
                }
            }
        }
    }
    
}
