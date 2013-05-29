package com.camobile.camdrm;

import java.io.File;
import java.io.FileInputStream;
//import java.io.FileOutputStream;
import java.io.IOException;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import android.util.Log;


public class UnitDecrypt
{
  //private static final String STATIC_IV = "__CAM_STATICIV__";  // 16byte
  //private static final String STATIC_KEY = "himitsu##himitsu"; // 16byte

  private static final String STATIC_IV = "gj7WqMqyAy31EdXp";
  private static final String STATIC_KEY = "SQezoCwMUg1g5P40";

  static
  {
    // JNIライブラリのロード
    System.loadLibrary ("DecryptNative");
  }

  // JNIメソッドの定義
  public native byte[] decrypt_native (String key, String iv, byte[] data);

  public byte[] main_native (String m_file_path)
  {
    FileInputStream fis = null;
    byte[] ret = null;
    try
    {
      File file = new File (m_file_path);
      fis = new FileInputStream (file);

      int available = fis.available ();
      byte[] tmp_contents_info = new byte[available];
      fis.read (tmp_contents_info);

      ret = decrypt_native (STATIC_KEY, STATIC_IV, tmp_contents_info);
      Log.d ("UnitDecrypt", "ret size = " + ret.length);

    }
    catch (Exception e)
    {
      Log.e ("UnitDecrypt", e.getMessage ());
    }
    finally
    {
      try
      {
        fis.close ();
      }
      catch (Exception e)
      {
      }
    }

    return ret;
  }

  public void main (String m_file_path)
  {
    FileInputStream fis = null;
    try
    {
      File file = new File (m_file_path);
      fis = new FileInputStream (file);

      int available = fis.available ();
      byte[] tmp_contents_info = new byte[available];
      fis.read (tmp_contents_info);
      //Log.d ("UnitDecrypt", new String (tmp_contents_info, "UTF-8"));

      try
      {
        long start, stop;
        start = System.currentTimeMillis ();
        //byte[] decrypt_contents_info = decrypt (STATIC_KEY.getBytes (), STATIC_IV.getBytes (), tmp_contents_info);
        decrypt (STATIC_KEY.getBytes (), STATIC_IV.getBytes (), tmp_contents_info);
        stop = System.currentTimeMillis ();

        Log.d ("UnitDecrypt", String.valueOf (stop - start));
      }
      catch (Exception e)
      {
        Log.e ("UnitDecrypt", e.getMessage ());
      }
    }
    catch (IllegalArgumentException e)
    {
      Log.e ("UnitDecrypt", e.getMessage ());
    }
    catch (IllegalStateException e)
    {
      Log.e ("UnitDecrypt", e.getMessage ());
    }
    catch (IndexOutOfBoundsException e)
    {
      Log.e ("UnitDecrypt", e.getMessage ());
    }
    catch (IOException e)
    {
      Log.e ("UnitDecrypt", e.getMessage ());
    }
    finally
    {
      try
      {
        fis.close ();
      }
      catch (Exception e)
      {
      }
    }
  }

  private static byte[] decrypt (byte[] key, byte[] iv, byte[] input)
    throws
          NoSuchAlgorithmException,
          NoSuchPaddingException,
          InvalidKeyException,
          InvalidAlgorithmParameterException,
          IllegalBlockSizeException,
          BadPaddingException
  {
    final SecretKey secret_key = new SecretKeySpec (key, "AES");
    final Cipher cipher = Cipher.getInstance ("AES/CBC/PKCS5Padding");
    cipher.init (Cipher.DECRYPT_MODE, secret_key, new IvParameterSpec (iv));
    return cipher.doFinal (input);
  }

}
