package filemanager;

import javax.microedition.lcdui.*;

/**
 * ����� ��� ������ ��������� ���� "���� ... ����������. ������������?"
 * � ���������� ������ �� / ��� / �� ��� ���� / ��� ��� ���� / ������
 */
public class alConfirmOverwrite extends Alert implements CommandListener, Runnable
{
    public Command cmdNo = new Command (Locale.Strings[Locale.NO_CMD], Command.OK, 2);
    public Command cmdYes = new Command (Locale.Strings[Locale.YES_CMD], Command.CANCEL, 3);
    public Command cmdYesForAll = new Command (Locale.Strings[Locale.YES_FOR_ALL], Command.CANCEL, 4);
    public Command cmdNoForAll = new Command (Locale.Strings[Locale.NO_FOR_ALL], Command.OK, 5);
    public Command cmdCancel = new Command (Locale.Strings[Locale.CANCEL_CMD], Command.CANCEL, 6);
    public Command modalResult;
    public Thread t;
    Displayable nextShow;
    /**
     * ������������ ����� � ������ �����, ��������� ������ �������.
     * � lstBuffer ����� al.t.join() ��� ����, ����� ���������.
     */
    public alConfirmOverwrite (String filename, Displayable nextDsp)
    {
        super (Locale.Strings[Locale.CONFIRMATION], Locale.Strings[Locale.FILE] + 
               " " + filename + " " + Locale.Strings[Locale.FILE_NAME_EXIST] + " " +
               Locale.Strings[Locale.OVERWRITE_QUESTION],
               null, AlertType.CONFIRMATION);
               //images.question, null);
        addCommand (cmdYes);
        addCommand (cmdYesForAll);
        addCommand (cmdNo);
        addCommand (cmdNoForAll);
        addCommand (cmdCancel);
        setTimeout (FOREVER);
        setCommandListener (this);
        nextShow = nextDsp;
        t = new Thread (this);
    }
    /**
     * ������� �������� ������ �������
     */
    public void run ()
    {
        try
        {
            while (true)
                t.sleep (5000);
        } catch (InterruptedException ie) {}
        main.dsp.setCurrent (nextShow);
    }
    /**
     * ��������� ������� - ��������� ����� ���������� � ����� modalResult
     */
    public void commandAction (Command c, Displayable d)
    {
        modalResult = c;
        t.interrupt ();
    }
}