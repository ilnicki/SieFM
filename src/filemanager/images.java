package filemanager;

import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.*;

/**
 *
 * @author Dmytro
 */
public class Images
{

    public static final int iFile = 0;
    public static final int iMelody = 1;
    public static final int iPicture = 2;
    public static final int iVideo = 3;
    public static final int iText = 4;
    public static final int iZIP = 5;
    public static final int iTMO = 6;
    public static final int iFolder = 7;
    public static final int iHiddenFolder = 8;
    public static final int iDisk = 9;
    public static final int iMMC = 10;
    public static final int iFavorites = 11;
    public static final int iUp = 12;
    public static final int iClipboard = 13;
    public static final int iSieFM = 14;
    public static final int iCopy = 15;
    public static final int iMove = 16;
    public static final int iPaste = 17;
    public static final int iProperties = 18;
    public static final int iOptions = 19;
    public static final int iRename = 20;
    public static final int iHelp = 21;
    public static final int iDelete = 22;
    public static final int iExit = 23;
    public static final int iNext = 24;
    public static final int iUnpack = 25;
    public static final int iMark = 26;
    public static final int iMarkAll = 27;
    public static final int iDemarkAll = 28;
    public static final int iNoMute = 29;
    public static final int iMute = 30;
    public static final int iSelect = 31;
    public static final int iMoveIt = 32;
    public static final int iPack = 33;
    public static final int iKey = 34;
    public static final int iMenu = 35;

    //public static Image question;
    //public static Image error;
    //public static Image ok;
    //public static Image warn;
    public static Image buttons;
    public static Image icons;
    public static Image playerUI;
    public static Image playAnim;
    public static Image waitAnim;

    protected static Image iconsExplode[];

    /**
     * Загрузка картинок в статические поля.
     *
     * @throws java.io.IOException
     */
    public Images() throws java.io.IOException
    {
        //question = Image.createImage ("/img/mb_question.png");
        //error = Image.createImage ("/img/mb_error.png");
        //ok = Image.createImage ("/img/mb_ok.png");
        //warn = Image.createImage ("/img/mb_warn.png");
        icons = Image.createImage("/img/icons.png");
        iconsExplode = new Image[(icons.getWidth() + 15) / 16];
        for (int i = 0; i < iconsExplode.length; i++)
            iconsExplode[i] = null;
        buttons = Image.createImage("/img/btn_.png");
        playerUI = Image.createImage("/img/player_ui.jpg");
        playAnim = Image.createImage("/img/play.png");
        waitAnim = Image.createImage("/img/wait.png");
        playerUI = Image.createImage("/img/player_ui.jpg");
    }

    /**
     * Получить иконку с номером index - вырезать из icons если ещё не вырезана.
     *
     * @param index
     * @return
     */
    public static Image getIcon(int index)
    {
        if (index < 0 || index >= iconsExplode.length)
            return null;
        if (iconsExplode[index] == null)
            iconsExplode[index] = Image.createImage(icons, index * 16, 0, 16, 16, Sprite.TRANS_NONE);
        return iconsExplode[index];
    }

    /**
     * Нарисовать иконку с номером index на объекте Graphics g в положении x,y.
     *
     * @param g
     * @param index
     * @param x
     * @param y
     */
    public static void drawIcon(Graphics g, int index, int x, int y)
    {
        if (index >= 0 && index < iconsExplode.length)
            g.drawRegion(icons, index * 16, 0, 16, 16, Sprite.TRANS_NONE, x, y, Graphics.LEFT | Graphics.TOP);
    }
}
