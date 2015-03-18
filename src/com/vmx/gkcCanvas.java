/*
 * gkcCanvas.java
 *
 * Get Key Codes canvas
 * (c) 2006, Виталий Филиппов [VMX]
 */

package com.vmx;
import javax.microedition.lcdui.*;

public abstract class gkcCanvas extends Canvas
{
    public final int KEY_LEFT, KEY_RIGHT, KEY_UP, KEY_DOWN, KEY_FIRE;
    public static final int KEY_DIAL = -11, KEY_CANCEL = -12, KEY_RSK = -4, KEY_LSK = -1;
    public boolean iAmS75;
    
    public gkcCanvas ()
    {
        super ();
        // get key codes
        KEY_LEFT = getKeyCode (LEFT);
        KEY_RIGHT = getKeyCode (RIGHT);
        KEY_DOWN = getKeyCode (DOWN);
        KEY_UP = getKeyCode (UP);
        KEY_FIRE = getKeyCode (FIRE);
        iAmS75 = false;
        if (System.getProperty ("microedition.platform").indexOf("S75") > -1 ||
            System.getProperty ("microedition.platform").indexOf("SL75") > -1)
            iAmS75 = true;
    }
    
    public void setLightOn ()
    {
        if (iAmS75)
            com.siemens.mp.lcdui.Graphics.setLightOn ();
        else com.siemens.mp.game.Light.setLightOn ();
    }
    
    public void setLightOff ()
    {
        if (iAmS75)
            com.siemens.mp.lcdui.Graphics.setLightOff ();
        else com.siemens.mp.game.Light.setLightOff ();
    }
}
