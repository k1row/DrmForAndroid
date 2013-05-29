package com.camobile.camdrm;

import android.app.Activity;
import android.os.Bundle;

import java.io.IOException;
import java.io.FileInputStream;
import java.io.File;

import java.lang.IllegalArgumentException;
import java.lang.IllegalStateException;


import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.Gravity;

import android.widget.Button;
import android.widget.Toast;

import android.util.Log;


public class CamDrm extends Activity implements OnClickListener, OnCompletionListener
{
  private MediaPlayer m_player = null;
  private Button m_btn = null;

  private final float FONT_SIZE = 30;

  public void show_toast (String s)
  {
    if (s == null)
      return;

    Log.e ("CamDrm", s);

    Toast t = Toast.makeText (getApplicationContext (), s, Toast.LENGTH_LONG);
    t.setGravity (Gravity.CENTER, 0, 0);
    t.show ();
  }

  @Override
    public void onCreate(Bundle savedInstanceState)
    {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.main);

      m_btn = (Button)findViewById (R.id.btn);
      m_btn.setTextSize (FONT_SIZE);
      m_btn.setText ("再生");
      m_btn.setOnClickListener (this);
    }

  @Override
    public void onClick (View v)
    {
      if (m_player.isPlaying ())
      {
        m_player.pause ();
        m_btn.setText ("再生");
      }
      else
      {
        m_player.seekTo (0);
        m_player.start ();
        m_btn.setText ("停止");
      }
    }

  @Override
    public void onStart ()
    {
      super.onStart ();
      call_decrypt_camdrm ();
    }

  void call_unit_decrypt ()
  {
    if(m_player != null)
      m_player.release ();

    m_player = new MediaPlayer ();
    String path = null;
    try
    {
      File file = new File (path);
      FileInputStream fis = new FileInputStream (file);

      if (fis != null)
        m_player.setDataSource (fis.getFD());
    }
    catch (Exception e)
    {
      show_toast (e.getMessage ());
      e.printStackTrace ();
    }

    try
    {
      m_player.prepare ();
    }
    catch (IllegalStateException e)
    {
      show_toast (e.getMessage ());
      e.printStackTrace ();

    }
    catch (IOException e)
    {
      show_toast (e.getMessage ());
      e.printStackTrace ();
    }

    m_player.setOnCompletionListener (this);
  }

  void call_decrypt_camdrm ()
  {
    if(m_player != null)
      m_player.release ();

    m_player = new MediaPlayer ();
    try
    {
      String aes_file = "/mnt/sdcard/Thriller.mp3";
      String path = null;

      {
        //TelephonyManager telephonyManager = (TelephonyManager)getSystemService (TELEPHONY_SERVICE);
        //String subscriberid = telephonyManager.getSubscriberId ();  // IMSI(サブスクライバ)の取得
        //String iccid = telephonyManager.getSimSerialNumber ();    // ICCIDの取得

        String subscriberid = "440103012592792";

        try
        {
          DecryptCamDrm drm = new DecryptCamDrm (aes_file, subscriberid);
          path = drm.do_decrypt ();
        }
        catch (CamDrmException e)
        {
          show_toast (e.getMessage ());
          e.printStackTrace ();
        }
      }

      File file = new File (path);
      FileInputStream fis = new FileInputStream (file);

      if (fis != null)
        m_player.setDataSource (fis.getFD());
    }
    catch (IllegalArgumentException e)
    {
      show_toast (e.getMessage ());
      e.printStackTrace ();
    }
    catch (IllegalStateException e)
    {
      show_toast (e.getMessage ());
      e.printStackTrace ();
    }
    catch (IOException e)
    {
      show_toast (e.getMessage ());
      e.printStackTrace ();
    }
    catch (Exception e)
    {
      show_toast (e.getMessage ());
      e.printStackTrace ();
    }

    try
    {
      m_player.prepare ();
    }
    catch (IllegalStateException e)
    {
      show_toast (e.getMessage ());
      e.printStackTrace ();

    }
    catch (IOException e)
    {
      show_toast (e.getMessage ());
      e.printStackTrace ();
    }

    m_player.setOnCompletionListener (this);
  }

  @Override
    public void onStop ()
    {
      super.onStop ();
      if (m_player != null)
      {
        if (m_player.isPlaying ())
          m_player.stop ();

        m_player.release ();
        m_player = null;
      }
    }

  // メディアファイルの再生が終わった時のイベント
  @Override
    public void onCompletion (MediaPlayer arg0)
    {
      // 再生ボタン表示に切り替える
      m_btn.setText("再生");
    }
}