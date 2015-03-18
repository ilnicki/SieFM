package filemanager;

import javax.microedition.lcdui.*;

/**
 *
 * @author Dmytro
 */
public class EulaForm
       extends Form
       implements CommandListener
{

    /**
     *
     */
    public EulaForm ()
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
        com.siemens.mp.lcdui.Displayable.setHeadlineIcon (this, Images.getIcon (Images.iText));
    }

    /**
     *
     * @param command
     * @param displayable
     */
    public void commandAction (Command command, Displayable displayable)
    {
        if (command.getCommandType () == Command.OK)
        {
            Options.firstTime = false;
            WaitCanvas.start ();
        }
        else if (command.getCommandType () == Command.EXIT)
        {
            Options.firstTime = true;
            Main.midlet.destroyApp (false);
        }
    }
}
