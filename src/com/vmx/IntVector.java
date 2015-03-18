package com.vmx;

/**
 *
 * @author Dmytro
 */
public class IntVector
{

    protected int[] array;
    protected int count;
    public final int arrplus;

    /**
     * Конструктор.
     */
    public IntVector()
    {
        array = new int[0];
        count = 0;
        arrplus = 32;
    }

    /**
     * Удалить элементы с first по first + n - 1.
     *
     * @param first
     * @param n
     */
    public void remove(int first, int n)
    {
        if (first < 0 || first >= count || n <= 0)
            return;
        if (first + n >= count)
            count = first;
        else
            for (int i = first; i < count - n; i++)
                array[i] = array[i + n];
    }

    /**
     * Добавить в array значение n.
     *
     * @param el
     */
    public void add(int el)
    {
        if (count >= array.length)
        {
            int[] newarray = new int[array.length + arrplus];
            System.arraycopy(array, 0, newarray, 0, array.length);
            array = newarray;
        }
        array[count++] = el;
    }

    /**
     * Получить элемент номер index.
     *
     * @param index
     * @return
     */
    public int get(int index)
    {
        return array[index];
    }

    /**
     * Найти value в векторе.
     *
     * @param value
     * @return
     */
    public int find(int value)
    {
        int i;
        for (i = 0; i < count; i++)
            if (array[i] == value)
                break;
        if (i >= count)
            return -1;
        return i;
    }

    /**
     * Получить размер.
     *
     * @return
     */
    public int size()
    {
        return count;
    }
}
