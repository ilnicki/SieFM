package com.vmx;

/**
 *
 * @author Dmytro
 */
public interface ProgressCallback
{

    /**
     *
     * @param max
     */
    public void setMax(int max);

    /**
     *
     * @param progress
     */
    public void setProgress(int progress);

    /**
     *
     * @param plus
     */
    public void progress(int plus);
}
