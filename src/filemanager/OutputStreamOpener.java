package filemanager;

import java.io.*;

/**
 *
 * @author Dmytro
 */
public interface OutputStreamOpener
{

    /**
     *
     * @return
     */
    public InputStream openInputStream ();

    /**
     *
     * @param offset
     * @return
     */
    public OutputStream openOutputStream (int offset);

    /**
     *
     * @param offset
     * @return
     */
    public boolean truncate (int offset);
}
