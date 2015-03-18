package com.vmx;

public class IntVector
{
    protected int [] array;
    protected int count;
    public final int arrplus;
    public IntVector ()
    {
        array = new int [0];
        count = 0;
        arrplus = 32;
    }
    /**
     * Удалить элементы с first по first+n-1
     */
    public void remove (int first, int n)
    {
        if (first < 0 || first >= count || n <= 0)
            return;
        if (first+n >= count)
            count = first;
        else
            for (int i = first; i < count-n; i++)
                array [i] = array [i+n];
    }
    /**
     * Добавить в array значение n
     */
    public void add (int el)
    {
        if (count >= array.length)
        {
            int [] newarray = new int [array.length + arrplus];
            System.arraycopy (array, 0, newarray, 0, array.length);
            array = newarray;
        }
        array [count++] = el;
    }
    /**
     * Получить элемент номер index
     */
    public int get (int index)
    {
        return array[index];
    }
    /**
     * Найти value в векторе
     */
    public int find (int value)
    {
        int i;
        for (i = 0; i < count; i++)
            if (array [i] == value)
                break;
        if (i >= count)
            return -1;
        return i;
    }
    /**
     * Получить размер
     */
    public int size ()
    {
        return count;
    }
}
