package filemanager;

import java.io.*;

public interface OutputStreamOpener
{
    public InputStream openInputStream ();
    public OutputStream openOutputStream (int offset);
    public boolean truncate (int offset);
}
