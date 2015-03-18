package filemanager;

import javax.microedition.lcdui.*;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.game.*;
import java.io.*;
import com.vmx.*;

/**
 * Класс - просмотрщик картинок.
 */
public class ImageViewCanvas extends KeyCodeCanvas
{
    private Displayable parent;
    private javax.microedition.lcdui.Image currentImage = null;
    private boolean enableUI = true;
    private boolean rotate;
    private boolean scaled;
    private int pictureWidth, pictureHeight;
    int w, h;
    private int curposx, curposy;
    private Sprite picture;
    int selectedIndex;
    String currentPictureFile;
    Font nameFont;
    /**
     * Конструктор
     */
    ImageViewCanvas ()
    {
        setFullScreenMode (true);
        w = getWidth ();
        h = getHeight ();
        nameFont = Font.getFont (Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
    }
    /**
     * Префиксная функция загрузки картинки
     */
    protected void predisplay ()
    {
        serviceRepaints ();
        selectedIndex = Main.FileSelect.scrSel;
        currentImage = null;
        pictureWidth = -1;
        pictureHeight = -1;
        curposx = 0;
        curposy = 0;
        scaled = false;
        rotate = false;
    }
    /**
     * Постфиксная функция загрузки картинки
     */
    protected void postdisplay ()
    {
        if (currentImage != null)
        {
            pictureWidth = currentImage.getWidth ();
            pictureHeight = currentImage.getHeight ();
            picture = new Sprite (currentImage);
            if (currentImage.getWidth() > currentImage.getHeight())
            {
                rotate = true;
                swapHW ();
            }
            placeImageToCenter ();
        }
    }
    
    /**
     *
     * @param is
     * @param parent
     */
    public void displayImageFromStream (InputStream is, Displayable parent)
    {
        predisplay ();
        this.parent = parent;
        currentPictureFile = Main.currentPath + Main.FileSelect.files[selectedIndex]; //!!!
        try
        {
            currentImage = Image.createImage (is);
        } catch (Exception x) { currentImage = null; }
        try
        {
            is.close ();
            postdisplay ();
        } catch (Exception x) { currentImage = null; }
        repaint ();
    }
    
    /**
     *
     * @param imgName
     * @param parent
     */
    public void displayImage (String imgName, Displayable parent)
    {
        predisplay ();
        this.parent = parent;
        currentPictureFile = imgName;
        // грузим картинку в полный размер, потом по ширине, потом по высоте, если не выходит
        if ((currentImage = readImageFromFile (imgName, false)) == null)
        {
            if ((currentImage = readImageFromFile (imgName, 0, h)) == null)
                currentImage = readImageFromFile (imgName, h, 0);
            if (currentImage != null)
                scaled = true;
        }
        if (currentImage != null)
        {
            boolean hbig = currentImage.getHeight () > h;
            boolean wbig = currentImage.getWidth () > w;
            if (hbig || wbig)
            {
                scaled = true;
                // если и ширина велика, и высота тоже, тогда выбираем,
                // по высоте или ширине вписывать в экран
                if (hbig && wbig)
                {
                    currentImage = readImageFromFile (imgName, 0, h);
                    if (currentImage.getHeight()*w/h < currentImage.getWidth())
                    {
                        if (currentImage.getWidth () > currentImage.getHeight ())
                            currentImage = readImageFromFile (imgName, h, 0);
                        else currentImage = readImageFromFile (imgName, w, 0);
                    }
                }
                // если только высота - то по высоте
                else if (hbig)
                    currentImage = readImageFromFile (imgName, 0, h);
                // если только ширина - то смотрим, а не придётся ли его потом поворачивать?
                // если придётся, то выравниваем по "ширине", но "ширина" это сейчас высота экрана
                else if (wbig)
                {
                    if (currentImage.getWidth () > currentImage.getHeight ())
                        currentImage = readImageFromFile (imgName, h, 0);
                    else currentImage = readImageFromFile (imgName, w, 0);
                }
            }
            postdisplay();
        }
        repaint ();
    }
    /**
     * Открыть рисунок средствами Siemens
     *
     * @param imgName String
     * @param h int - высота
     * @param w int - ширина
     * @return Image
     */
    public final javax.microedition.lcdui.Image readImageFromFile (String imgName, int w, int h)
    {
        javax.microedition.lcdui.Image image = null;
        try
        {
            image = com.siemens.mp.lcdui.Image.createImageFromFile (imgName, w, h);
        } catch (Exception e) { image = null; }
        return image;
    }
    /**
     * Открыть рисунок средствами Siemens
     *
     * @param imgName String
     * @param Scale boolean
     * @return Image
     */
    public final javax.microedition.lcdui.Image readImageFromFile (String imgName, boolean Scale)
    {
        javax.microedition.lcdui.Image image = null;
        try
        {
            image = com.siemens.mp.lcdui.Image.createImageFromFile (imgName, Scale);
        } catch (Exception e) { image = null; }
        return image;
    }
    /**
     * Функция отрисовки
     * @param g
     */
    protected void paint (Graphics g)
    {
        // фон
        if (currentImage != null)
        {
            g.setColor (0x000000);
            g.fillRect (0, 0, w, h);
            if (rotate)
                picture.setTransform (Sprite.TRANS_ROT270);
            placeImageToCenter ();
            picture.setPosition (curposx, curposy);
            picture.paint (g);
        }
        else // нет изображения
            g.drawRegion (Images.waitAnim, 0, 0, 32, 32, Sprite.TRANS_NONE, w/2-16, h/2-16, Graphics.LEFT | Graphics.TOP);
        if (enableUI)
        {
            g.drawRegion (Images.playerUI, 0, 146,  w/2, 30,  0,  0, h - 30, Graphics.TOP | Graphics.LEFT);
            g.drawRegion (Images.playerUI, 132 - w/2, 146,  w/2, 30,  0,  w - w/2, h - 30, Graphics.TOP | Graphics.LEFT);
            String tmp = Main.FileSelect.files[selectedIndex];
            g.setFont (nameFont);
            g.setColor (0x800000);
            g.drawString (tmp, w/2, h - 26, g.TOP | g.HCENTER);
            tmp = pictureWidth + " x " + pictureHeight + (scaled ? Locale.Strings[Locale.IMAGEVIEW_SCALED] : "");
            g.setColor (0x000080);
            g.drawString (tmp, w/2, h - 13, g.TOP | g.HCENTER);
        }
    }
    /** Пересчитать положение левого верхнего угла картинки */
    private void placeImageToCenter ()
    {
        curposx = getWidth()/2 - pictureWidth/2;
        curposy = getHeight()/2 - pictureHeight/2;
    }
    /** Поменять местами pictureHeight и pictureWidth */
    private void swapHW ()
    {
        int r = pictureHeight;
        pictureHeight = pictureWidth;
        pictureWidth = r;
    }
    /** Обработчик нажатий клавиш
     * @param keyCode */
    protected void keyPressed (int keyCode)
    {
        if (keyCode == KEY_POUND)
        {
            enableUI = !enableUI;
            repaint ();
        }
        // Следующая картинка
        else if (keyCode == KEY_DOWN || keyCode == KEY_NUM8)
            nextPicture ();
        // Предыдущ картинка
        else if (keyCode == KEY_UP || keyCode == KEY_NUM2)
            prevPicture ();
        // Выход
        else if (keyCode == KEY_CANCEL || keyCode == KEY_RSK)
            Main.dsp.setCurrent (parent);
    }
    /**
     * следующая картинка
     */
    private void nextPicture ()
    {
        currentImage = null;
        repaint ();
        Main.FileSelect.select (selectedIndex = Main.FileSelect.getNextOfType (selectedIndex, Filesystem.TYPE_PICTURE));
        currentPictureFile = Main.currentPath + Main.FileSelect.files[selectedIndex];
        Main.currentFile = Main.FileSelect.files[selectedIndex];
        Main.FileSelect.selectFile ();
    }
    /**
     * предыдущая картинка
     */
    private void prevPicture ()
    {
        currentImage = null;
        repaint ();
        Main.FileSelect.select (selectedIndex = Main.FileSelect.getPrevOfType (selectedIndex, Filesystem.TYPE_PICTURE));
        currentPictureFile = Main.currentPath + Main.FileSelect.files[selectedIndex];
        Main.currentFile = Main.FileSelect.files[selectedIndex];
        Main.FileSelect.selectFile ();
    }
}
