package com.camobile.camdrm;

import java.io.File;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.lang.IllegalArgumentException;
import java.lang.IllegalStateException;
import java.lang.IndexOutOfBoundsException;

import java.security.MessageDigest;

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

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.Log;


public class DecryptCamDrm
{
  // クラスでかいのでコンストラクタここへ
  public DecryptCamDrm (String _file_path, String _subscriberid)
  {
    this.m_file_path = _file_path;
    this.m_subscriberid = _subscriberid;

    this.m_contents_info = null;
    this.m_contents = null;

    this.m_dynamic_iv = null;
    this.m_dynamic_key = null;
  }

  private static final String SAVE_FOLDER_PATH = "/data/data/com.camobile.camdrm/decrypt/";

  // とりあえず固定(iv、key)を以下のように設定されているよ
  private static final String STATIC_IV = "__CAM_STATICIV__";  // 16byte
  private static final String STATIC_KEY = "himitsu##himitsu"; // 16byte

  private String m_file_path;      // 暗号化された大元のファイル
  private String m_subscriberid;   // この端末のIMSI

  private byte[] m_contents_info;  // コンテンツ情報
  private byte[] m_contents;       // コンテンツそのもの

  private byte[] m_dynamic_iv;     // コンテンツをAES復号する為のiv
  private byte[] m_dynamic_key;    // コンテンツをAES復号する為のkey

  static
  {
    // JNIライブラリのロード
    System.loadLibrary ("DecryptNative");
  }

  // JNIメソッドの定義
  public native byte[] decrypt_native (String key, String iv, byte[] data);

  /* 固定メッセージをここで定義しておく */
  private static final String ERR_MSG_NO_FILE = "複合ファイルが指定されていません";
  private static final String ERR_MSG_FAIL_DECRYPT_CONTENTS_INFO = "コンテンツ情報ファイルの復元に失敗しました。";
  private static final String ERR_MSG_FAIL_DECRYPT_IMSI_HASH = "IMSI HASHから動的iv、keyの取得に失敗しました。";

  // コンテンツ情報XMLのパース後の構造体
  public static class ContentsInfo
  {
    public final static String TAG_CONTENTS_INFO = "CONTENTS_INFO";
    public final static String TAG_IMSI_IV = "IMSI_IV";
    public final static String TAG_IMSI_KEY = "IMSI_KEY";
    public final static String TAG_CONTENTS_URL = "CONTENTS_URL";
    public final static String TAG_CONTENTS_SIZE = "CONTENTS_SIZE";
    public final static String TAG_MAKER_URL = "MAKER_URL";
    public final static String TAG_MAKER_NAME = "MAKER_NAME";
    public final static String TAG_BUY = "BUY";
    public final static String TAG_IMAGE_URL = "IMAGE_URL";
    public final static String TAG_CONTENTS_NAME = "CONTENTS_NAME";

    protected final static String NULL = "NULL";

    String m_imsi_iv;
    String m_imsi_key;
    String m_contents_url;
    String m_contents_size;
    String m_maker_url;
    String m_maker_name;
    String m_buy;
    String m_image_url;
    String m_contents_name;

    public ContentsInfo ()
    {
      m_imsi_iv = NULL;
      m_imsi_key = NULL;
      m_contents_url = NULL;
      m_contents_size = NULL;
      m_maker_url = NULL;
      m_maker_name = NULL;
      m_buy = NULL;
      m_image_url = NULL;
      m_contents_name = NULL;
    }
  }

  // コンテンツ情報XMLのパースを受け持つよ
  private static class ContentsInfoXmlParser
  {
    private InputStream is;
    private XmlPullParser xpp;
    private int eventType;

    public ContentsInfoXmlParser (InputStream is)
    {
      this.is = is;
    }

    public ContentsInfo parse (String enc)
    {
      if (is == null)
        return null;

      ContentsInfo info = null;
      String tag = null;
      String end_tag = null;

      try
      {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance ();
        factory.setNamespaceAware (true);
        this.xpp = factory.newPullParser ();
        xpp.setInput (is, enc);

        this.eventType = xpp.getEventType ();
        while (eventType != XmlPullParser.END_DOCUMENT)
        {
          if (eventType == XmlPullParser.START_TAG)
          {
            tag = xpp.getName ();

            if (tag.compareTo (ContentsInfo.TAG_CONTENTS_INFO) == 0)
              info = new ContentsInfo ();
            if (tag.compareTo (ContentsInfo.TAG_IMSI_IV) == 0)
              info.m_imsi_iv = getText ();
            else if (tag.compareTo (ContentsInfo.TAG_IMSI_KEY) == 0)
              info.m_imsi_key = getText ();
            else if (tag.compareTo (ContentsInfo.TAG_CONTENTS_URL) == 0)
              info.m_contents_url = getText ();
            else if (tag.compareTo (ContentsInfo.TAG_CONTENTS_SIZE) == 0)
              info.m_contents_size = getText ();
            else if (tag.compareTo (ContentsInfo.TAG_MAKER_URL) == 0)
              info.m_maker_url = getText ();
            else if (tag.compareTo (ContentsInfo.TAG_MAKER_NAME) == 0)
              info.m_maker_name = getText ();
            else if (tag.compareTo (ContentsInfo.TAG_BUY) == 0)
              info.m_buy = getText ();
            else if (tag.compareTo (ContentsInfo.TAG_IMAGE_URL) == 0)
              info.m_image_url = getText ();
            else if (tag.compareTo (ContentsInfo.TAG_CONTENTS_NAME) == 0)
              info.m_contents_name = getText ();
          }
          else if (eventType == XmlPullParser.END_TAG)
          {
            end_tag = xpp.getName ();
            if (end_tag.compareTo (ContentsInfo.TAG_CONTENTS_INFO) == 0)
            {
              // 特にやる事なし
            }
          }
          eventType = xpp.next ();
        }
      }
      catch (Exception e)
      {
        e.printStackTrace ();
      }

      return info;
    }

    private String getText () throws XmlPullParserException, IOException
    {
      if (eventType != XmlPullParser.START_TAG)
      {
        eventType = xpp.next ();
        return "UnKnown";
      }

      while (eventType != XmlPullParser.TEXT)
        eventType = xpp.next ();

      return xpp.getText ();
    }
  }

  // ファイルのdecrypt開始。メイン関数
  public String do_decrypt () throws CamDrmException
  {
    if (m_file_path == null)
      throw new CamDrmException (ERR_MSG_NO_FILE);

    // ① enc-contet-infoとenc-contentを切り離す
    separate_file ();

    // コンテンツ情報構造体
    ContentsInfo info = null;
    try
    {
      // ② content-infoを固定鍵、固定ivにてAES復号化
      // ③ XMLをパースしてコンテンツ情報とenc-ivとenc-keyを取り出す
      info = decrypt_contents_info ();

      if (info == null)
        throw new CamDrmException (ERR_MSG_FAIL_DECRYPT_CONTENTS_INFO);
    }
    catch (Exception e)
    {
      String msg = ERR_MSG_FAIL_DECRYPT_CONTENTS_INFO + "(Reason : " + e.getMessage () + ")";
      throw new CamDrmException (msg);
    }

    try
    {
      // ④ IMSIをSHA1にてハッシュ化し、前方後方それぞれ16byteずつを抽出(IMSI HASH)
      // ⑤ dynamic_iv、dynamic_keyをIMSI HASHの前方16byteをimsi_iv、後方16byteをimsi_keyとしてAES複合化
      decrypt_iv_key_with_imsi_hash (info);
    }
    catch (Exception e)
    {
      String msg = ERR_MSG_FAIL_DECRYPT_IMSI_HASH + "(Reason : " + e.getMessage () + ")";
      throw new CamDrmException (msg);
    }

    String ret_file_path = null; // 最終的なコンテンツファイルへのパス

    try
    {
      // ⑥ dynamic_iv、dynamic_keyにてコンテンツをAES複合化
      ret_file_path = decrypt_contents (info);
    }
    catch (Exception e)
    {
      String msg = ERR_MSG_FAIL_DECRYPT_CONTENTS_INFO + "(Reason : " + e.getMessage () + ")";
      throw new CamDrmException (msg);
    }

    return ret_file_path;
  }

  private void separate_file () throws CamDrmException
  {
    // --------------------------------------------------------------
    // 復号時は、先頭から4byteを取得してそれをencrypt_contents_info
    // のサイズとして扱い、encrypt_contents_infoとencrypt_contentsを
    // 分解してから復号処理を実行する。

    // まずは元のファイルをinfoと実データにセパレート
    BufferedInputStream fis = null;

    try
    {
      File file = new File (m_file_path);
      fis = new BufferedInputStream (new FileInputStream (file));

      // 先頭4byteにコンテンツ情報のサイズが入っているので、それを取得
      byte[] size_info = new byte[4];
      fis.read (size_info, 0, 4);
      int contents_info_size = byte2int (size_info);

      // コンテンツ情報の取得
      m_contents_info = new byte[contents_info_size];
      fis.read (m_contents_info, 0, contents_info_size);

      // 実コンテンツファイルの読み込み
      m_contents = new byte[fis.available ()];
      fis.read (m_contents, 0, fis.available ());
    }
    catch (IllegalArgumentException e)
    {
      throw new CamDrmException (e.getMessage ());
    }
    catch (IllegalStateException e)
    {
      throw new CamDrmException (e.getMessage ());
    }
    catch (IndexOutOfBoundsException e)
    {
      throw new CamDrmException (e.getMessage ());
    }
    catch (IOException e)
    {
      throw new CamDrmException (e.getMessage ());
    }
    catch (Exception e)
    {
      throw new CamDrmException (e.getMessage ());
    }
    finally
    {
      try
      {
        fis.close ();
      }
      catch (Exception e)
      {
        throw new CamDrmException (e.getMessage ());
      }
    }
  }

  private static int byte2int (byte[] b)
  {
    return (b[0]<<24) + (b[1]<<16) + (b[2]<<8) + b[3];
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

  // enc-content-infoを固定鍵、固定ivにてAES復号化
  private ContentsInfo decrypt_contents_info () throws Exception
  {
    byte[] decrypt_contents_info = decrypt (STATIC_KEY.getBytes (), STATIC_IV.getBytes (), m_contents_info);

    Log.d ("CamDrm", new String (decrypt_contents_info, "UTF-8"));

    // XMLをパースしておいてContentsInfo(特にimsi_ivとimsi_key)を取得
    ContentsInfoXmlParser parser = new ContentsInfoXmlParser (new ByteArrayInputStream (decrypt_contents_info));
    return parser.parse ("UTF-8");
  }

  // IMSI HASHの前方16byteをiv、後方16byteを鍵としてAES復号化し、iv、keyを取り出す
  private void decrypt_iv_key_with_imsi_hash (ContentsInfo info) throws Exception
  {
    MessageDigest md = null;
    md = MessageDigest.getInstance ("SHA1");

    md.reset ();
    md.update (m_subscriberid.getBytes ());
    byte[] hash= md.digest ();

    // ハッシュを16進数文字列に変換
    StringBuffer sb = new StringBuffer ();
    int cnt= hash.length;
    for (int i= 0; i< cnt; i++)
    {
      sb.append (Integer.toHexString ((hash[i]>> 4) & 0x0F));
      sb.append (Integer.toHexString (hash[i] & 0x0F));
    }

    String imsi_iv = sb.toString ().substring (0, 16);
    String imsi_key = sb.toString ().substring (sb.toString ().length () - 16, sb.toString ().length ());

    /* perl側でencryptする時、plain textのencryptなので  $cipher->encrypt_hex () を使用しているが
      これはcpan(http://search.cpan.org/~lds/Crypt-CBC-2.12/CBC.pm)によると
      ---
      These are convenience functions that operate on ciphertext in a hexadecimal representation.
      encrypt_hex($plaintext) is exactly equivalent to unpack('H*',encrypt($plaintext)).
      These functions can be useful if, for example, you wish to place the encrypted
      と言っているので、Java側でpackを実装してあげて、それをdecryptする

      じゃないと正しい値が帰ってこないよ
     */

    // こいつがコンテンツのAESを解くivとkeyだよ
    m_dynamic_iv = decrypt (imsi_key.getBytes (), imsi_iv.getBytes (), pack (info.m_imsi_iv));
    m_dynamic_key = decrypt (imsi_key.getBytes (), imsi_iv.getBytes (), pack (info.m_imsi_key));
  }

  private static byte[] pack (String hex)
  {
    // 文字列長の1/2の長さのバイト配列を生成。
    byte[] bytes = new byte[hex.length () / 2];

    // バイト配列の要素数分、処理を繰り返す。
    for (int index = 0; index < bytes.length; index++)
    {
      // 16進数文字列をバイトに変換して配列に格納。
      bytes[index] = (byte)Integer.parseInt (hex.substring (index * 2, (index + 1) * 2), 16);
    }

    // バイト配列を返す。
    return bytes;
  }

  // dynamic_iv、dynamic_keyにてコンテンツをAES複合化
  private String decrypt_contents (ContentsInfo info) throws Exception
  {
    String key = new String (m_dynamic_key, "UTF-8");
    String iv = new String (m_dynamic_iv, "UTF-8");

    // JNIにより処理（配列の戻り値に変更したければ、これでreturnに変えて下さい
    byte[] decrypt_object = decrypt_native (key, iv, m_contents);

    /* とりあえず SAVE_FOLDER_PATH に保存する */
    File dir = new File (SAVE_FOLDER_PATH);

    //フォルダが存在しなかった場合にフォルダを作成します。
    if (!dir.exists ())
      dir.mkdir ();

    String file = SAVE_FOLDER_PATH + info.m_contents_name + ".mp3";
    Log.d ("CamDrm", file);

    FileOutputStream os = null;
    try
    {
      os = new FileOutputStream (file);
      os.write (decrypt_object, 0, decrypt_object.length);
    }
    finally
    {
      try { os.close (); } catch (Exception e){}
    }

    // これが実コンテンツファイル！
    return file;
  }
}
