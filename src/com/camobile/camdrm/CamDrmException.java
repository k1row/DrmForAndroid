package com.camobile.camdrm;

import java.io.Serializable;
import android.util.Log;


public class CamDrmException extends Exception implements Serializable
{
  private static final long serialVersionUID = 1L;

  private String m_message;
  public CamDrmException (String message)
  {
    this.m_message = message;
    Log.e ("CamDrm", message);
  }

  public String getMessage ()
  {
    return m_message;
  }
}
