package com.vmx;

public interface ProgressCallback
{
    public void setMax (int max);
    public void setProgress (int progress);
    public void progress (int plus);
}
