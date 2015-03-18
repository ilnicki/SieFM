/*
 * KeyCodeCanvas.java
 *
 * Get Key Codes canvas
 * (c) 2006, Виталий Филиппов [VMX]
 */
package com.vmx;

import javax.microedition.lcdui.*;

/**
 *
 * @author Dmytro
 */
public abstract class KeyCodeCanvas extends Canvas
{

    public final int KEY_LEFT;
    public final int KEY_RIGHT;
    public final int KEY_UP;
    public final int KEY_DOWN;
    public final int KEY_FIRE;

    public static final int KEY_DIAL = -11;
    public static final int KEY_CANCEL = -12;
    public static final int KEY_RSK = -4;
    public static final int KEY_LSK = -1;

    public boolean isS75;

    /**
     *
     */
    public KeyCodeCanvas()
    {
        super();
        // get key codes
        KEY_LEFT = getKeyCode(LEFT);
        KEY_RIGHT = getKeyCode(RIGHT);
        KEY_DOWN = getKeyCode(DOWN);
        KEY_UP = getKeyCode(UP);
        KEY_FIRE = getKeyCode(FIRE);
        isS75 = false;
        if (System.getProperty("microedition.platform").indexOf("S75") > -1
                || System.getProperty("microedition.platform").indexOf("SL75") > -1)
            isS75 = true;
    }

    /**
     *
     */
    public void setLightOn()
    {
        if (isS75)
            com.siemens.mp.lcdui.Graphics.setLightOn();
        else
            com.siemens.mp.game.Light.setLightOn();
    }

    /**
     *
     */
    public void setLightOff()
    {
        if (isS75)
            com.siemens.mp.lcdui.Graphics.setLightOff();
        else
            com.siemens.mp.game.Light.setLightOff();
    }
}
