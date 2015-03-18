package filemanager;

import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.*;
import java.lang.*;

public class images
{
    public static final int iFile = 0, iMelody = 1, iPicture = 2,
            iVideo = 3, iText = 4, iZIP = 5, iTMO = 6,
            iFolder = 7, iHiddenFolder = 8, iDisk = 9,
            iMMC = 10, iFavorites = 11, iUp = 12, iClipboard = 13,
            iSieFM = 14, iCopy = 15, iMove = 16, iPaste = 17,
            iProperties = 18, iOptions = 19, iRename = 20,
            iHelp = 21, iDelete = 22, iExit = 23, iNext = 24,
            iUnpack = 25, iMark = 26, iMarkAll = 27, iDemarkAll = 28,
            iNoMute = 29, iMute = 30, iSelect = 31, iMoveIt = 32,
            iPack = 33, iKey = 34, iMenu = 35;
            
    public static Image //question, error, ok, warn,
            buttons, icons, playerUI, playAnim, waitAnim;
    protected static Image iconsExplode [];
    /**
     * Загрузка картинок в статические поля
     */
    public images () throws java.io.IOException
    {
        //question = Image.createImage ("/img/mb_question.png");
        //error = Image.createImage ("/img/mb_error.png");
        //ok = Image.createImage ("/img/mb_ok.png");
        //warn = Image.createImage ("/img/mb_warn.png");
        icons = Image.createImage ("/img/icons.png");
        iconsExplode = new Image [(icons.getWidth()+15)/16];
        for (int i = 0; i < iconsExplode.length; i++)
            iconsExplode [i] = null;
        buttons = Image.createImage ("/img/btn_.png");
        playerUI = Image.createImage ("/img/player_ui.jpg");
        playAnim = Image.createImage ("/img/play.png");
        waitAnim = Image.createImage ("/img/wait.png");
        playerUI = Image.createImage ("/img/player_ui.jpg");
    }
    /**
     * Получить иконку с номером index - вырезать из icons если ещё не вырезана
     */
    public static Image getIcon (int index)
    {
        if (index < 0 || index >= iconsExplode.length)
            return null;
        if (iconsExplode [index] == null)
            iconsExplode [index] = Image.createImage (icons, index*16, 0, 16, 16, Sprite.TRANS_NONE);
        return iconsExplode [index];
    }
    /**
     * Нарисовать иконку с номером index на объекте Graphics g
     * в положении x,y
     */
    public static void drawIcon (Graphics g, int index, int x, int y)
    {
        if (index >= 0 && index < iconsExplode.length)
            g.drawRegion (icons, index*16, 0, 16, 16, Sprite.TRANS_NONE, x, y, Graphics.LEFT|Graphics.TOP);
    }
}
