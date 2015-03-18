package filemanager;

import javax.microedition.lcdui.*;

public class frmEULA
       extends Form
       implements CommandListener
{
    public frmEULA ()
    {
        super (Locale.Strings[Locale.LICENSE_AGR]);
        StringItem si = new StringItem (null,
            "SieFM\n\n" +
            "(c) VMX 2006 [mvmx@sc] & DiHLoS 2005\nvmx@yourcmc.ru\nvitalif@mail.ru\n\n" + Locale.LICENSE_AGR_TEXT);
        si.setFont (Font.getFont (Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL));
        append (si);
        addCommand (new Command (Locale.Strings[Locale.NO_CMD], Command.EXIT, 1));
        addCommand (new Command (Locale.Strings[Locale.YES_CMD], Command.OK, 1));
        setCommandListener (this);
        com.siemens.mp.lcdui.Displayable.setHeadlineIcon (this, images.getIcon (images.iText));
    }
    public void commandAction (Command command, Displayable displayable)
    {
        if (command.getCommandType () == Command.OK)
        {
            options.firstTime = false;
            cvsWait.start ();
        }
        else if (command.getCommandType () == Command.EXIT)
        {
            options.firstTime = true;
            main.midlet.destroyApp (false);
        }
    }
}
