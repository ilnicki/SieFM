package filemanager;

import javax.microedition.lcdui.*;

/**
 *
 * @author Dmytro
 */
public class CompressForm
        extends Form
        implements CommandListener
{

    Displayable parent;
    TextField tf;
    Gauge gg;

    /**
     *
     * @param p
     */
    public CompressForm(Displayable p)
    {
        super(Locale.Strings[Locale.CREATE_ZIP]);
        parent = p;
        String cs = Main.currentPath.substring(0, Main.currentPath.length() - 1);
        int pos;
        if ((pos = cs.lastIndexOf('/')) >= 0)
            cs = cs.substring(pos + 1);
        if ((pos = cs.lastIndexOf(':')) >= 0)
            cs = cs.substring(0, pos);
        cs += ".zip";
        this.append(tf = new TextField(Locale.Strings[Locale.FILE_NAME], cs, 1024, TextField.ANY));
        this.append(gg = new Gauge(Locale.Strings[Locale.COMPRESSION_LEVEL], true, 9, 9));
        setCommandListener(this);
        addCommand(new Command(Locale.Strings[Locale.CANCEL_CMD], Command.BACK, 1));
        addCommand(new Command(Locale.Strings[Locale.OK_CMD], Command.OK, 1));
        com.siemens.mp.lcdui.Displayable.setHeadlineIcon(this, Images.getIcon(Images.iPack));
    }

    /**
     * Обработчик команд.
     *
     * @param c
     * @param d
     */
    public void commandAction(Command c, Displayable d)
    {
        Main.dsp.setCurrent(parent);
        if (c.getCommandType() == Command.OK)
            Buffer.zipFiles(Main.currentPath + tf.getString(), gg.getValue());
    }
}
