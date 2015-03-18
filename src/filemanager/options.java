package filemanager;

import javax.microedition.rms.*;
import java.io.*;
import java.util.Vector;

public class options
{
    // Названия хранилищ RMS
    private static final String optionsName = "SieFM";
    private static final String favoritesName = "SieFM_favorites";
    // Хранилище для настроек
    private static RecordStore optionsStore;
    private static RecordStore favoritesStore;
    // Настройки
    public static boolean firstTime = true; // первый раз запущен
    public static boolean showHidden = false; // показывать скрытые
    public static int volume = 100; // громкость аудио плейера
    public static boolean muted = false; // отключен звук в аудиоплейере?
    public static boolean quickSplash = false; // быстрая загрузка заставки
    public static boolean showDisk3 = false; // показывать диск 3:/
    public static boolean openNotSupported = false; // показывать неподдерживаемые
    public static boolean noEffects = false; // не показывать окошки "подождите"
    public static String language = "en";
    // Избранное
    protected static Vector favorites = new Vector ();
    /**
     * Пустой конструктор
     */
    public options ()
    {
    }
    /**
     * Сохранение настроек
     */
    public static void saveOptions ()
    {
        if (optionsStore != null)
        {
            byte [] options = null;
            try
            {
                ByteArrayOutputStream baos = new ByteArrayOutputStream ();
                DataOutputStream dos = new DataOutputStream (baos);
                dos.writeBoolean (firstTime);
                dos.writeBoolean (showHidden);
                dos.writeByte ((byte)volume);
                dos.writeBoolean (muted);
                dos.writeBoolean (quickSplash);
                dos.writeBoolean (showDisk3);
                dos.writeBoolean (openNotSupported);
                dos.writeBoolean (noEffects);
                dos.writeUTF (language);
                for (int i = 0; i < keyConfig.keyConfig.length; i++)
                    dos.writeInt (keyConfig.keyConfig[i]);
                dos.flush ();
                options = baos.toByteArray ();
                dos.close ();
                optionsStore.setRecord (1, options, 0, options.length);
            }
            catch (InvalidRecordIDException ridex)
            {
                // Запись #1 не существует, создать новую
                try { optionsStore.addRecord (options, 0, options.length); }
                catch (RecordStoreException ex)
                {
                    //System.out.println ("Could not add options record");
                }
            }
            catch (Exception ex)
            {
                //System.out.println ("Could not save options");
            }
        }
        if (optionsStore != null)
        {
            try
            {
                optionsStore.closeRecordStore ();
                optionsStore = null;
            }
            catch (RecordStoreException ex)
            {
                //System.out.println ("Could not close options storage");
            }
        }
    }
    /**
     * Восстановить настройки
     */
    public static void restoreOptions ()
    {
        try
        {
            optionsStore = RecordStore.openRecordStore (optionsName, true);
        }
        catch (RecordStoreException ex)
        {
            optionsStore = null;
            //System.out.println ("* optionsStore not created *");
        }
        if (optionsStore != null)
        {
            try
            {
                DataInputStream dis = new DataInputStream (new ByteArrayInputStream (optionsStore.getRecord (1)));
                firstTime = dis.readBoolean (); // первый раз запущен
                showHidden = dis.readBoolean (); // показывать скрытые
                volume = dis.readByte (); if (volume < 0) volume = -volume; // громкость аудио плейера
                muted = dis.readBoolean (); // отключен звук в аудиоплейере?
                quickSplash = dis.readBoolean (); // быстрая загрузка заставки
                showDisk3 = dis.readBoolean (); // показывать диск 3:/
                openNotSupported = dis.readBoolean (); // показывать неподдерживаемые
                noEffects = dis.readBoolean (); // без "подождите"
                language = dis.readUTF ();
                for (int i = 0; i < keyConfig.keyConfig.length; i++)
                    keyConfig.keyConfig[i] = dis.readInt ();
                dis.close ();
            } catch (Exception ex) {}
        }
    }
    /**
     * Сохранение избранного
     */
    public static void saveFavorites ()
    {
        try
        {
            RecordStore.deleteRecordStore (favoritesName);
        } catch (RecordStoreException e) {}
        try
        {
            favoritesStore = RecordStore.openRecordStore (favoritesName, true);
            for (int i = 0; i < favorites.size(); i++)
            {
                try
                {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream ();
                    DataOutputStream dos = new DataOutputStream (baos);
                    dos.writeUTF ((String)favorites.elementAt(i));
                    byte b[] = baos.toByteArray ();
                    favoritesStore.addRecord (b, 0, b.length);
                } catch (Exception e) {}
            }
            favoritesStore.closeRecordStore ();
        }
        catch (Exception e) {}
    }
    /**
     * Получение массива строк - избранного
     */
    public static String[] getFavorites ()
    {
        String [] fav = new String [favorites.size()];
        favorites.copyInto (fav);
        return fav;
    }
    /**
     * Добавление в избранное
     */
    public static void addFavorite (String nf)
    {
        favorites.addElement (nf);
    }
    /**
     * Удаление из избранного
     */
    public static void deleteFavorite (String ff)
    {
        favorites.removeElement (ff);
    }
    /**
     * Чтение строк из хранилища
     */
    public static void loadFavorites ()
    {
        favorites.removeAllElements ();
        int recordsNum = 0;
        try
        {
            favoritesStore = RecordStore.openRecordStore (favoritesName, true);
            try
            {
                for (RecordEnumeration enumX = favoritesStore.enumerateRecords (null, null, true); enumX.hasNextElement (); )
                {
                    int recId = enumX.nextRecordId ();
                    ByteArrayInputStream bais = new ByteArrayInputStream (favoritesStore.getRecord (recId));
                    DataInputStream dis = new DataInputStream (bais);
                    String in = dis.readUTF ();
                    favorites.addElement (in);
                }
            } catch (Exception e) {}
            favoritesStore.closeRecordStore ();
        } catch (RecordStoreNotFoundException e) {}
        catch (RecordStoreException e) {}
    }
}
