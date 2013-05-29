#include "com_camobile_camdrm_DecryptCamDrm.h"


#define LOG_TAG "DecryptNative"


jbyteArray
Java_com_camobile_camdrm_DecryptCamDrm_decrypt_1native (JNIEnv* env, jobject thiz,
                                                      jstring _key, jstring _iv, jbyteArray _data)
{
  const char* key = env->GetStringUTFChars (_key, NULL);
  __android_log_print (ANDROID_LOG_DEBUG, LOG_TAG, "key = %s", key);

  const char* iv = env->GetStringUTFChars (_iv, NULL);
  __android_log_print (ANDROID_LOG_DEBUG, LOG_TAG, "iv = %s", iv);

  // Decryptするデータの正確なサイズを確認
  int length = env->GetArrayLength (_data);
  __android_log_print (ANDROID_LOG_DEBUG, LOG_TAG, "length = %d", length);
  if (length == 0)
  {
    // InputDataのサイズが"0"とか意味不明
    __android_log_write (ANDROID_LOG_ERROR, LOG_TAG, "The object data is empty.");
    return NULL;
  }

  // InputeData配列をjbyte*のバッファにコピー
  jbyte* input_data = NULL;
  input_data = (jbyte*)malloc (length);
  env->GetByteArrayRegion (_data, 0, length, input_data);

  jbyte* out_data = NULL;
  out_data = (jbyte*)malloc (length);

  // opensslの関数を使ってDecrypt開始
  AES_KEY dec_key;
  ::memset (&dec_key, 0, sizeof (AES_KEY));
  AES_set_decrypt_key ((unsigned char*)key, 128, &dec_key);
  AES_cbc_encrypt ((unsigned char*)input_data, (unsigned char*)out_data, length, &dec_key, (unsigned char*)iv, AES_DECRYPT);

  jbyteArray result = env->NewByteArray (length);
  if (result == NULL)
  {
    // 戻り値の配列がアロケートできないなんて。。。
    __android_log_write (ANDROID_LOG_ERROR, LOG_TAG, "Couldn't allocate array 'result' for parameter data.");
    return NULL;
  }

  void* decrypted_data = env->GetPrimitiveArrayCritical (result, NULL);
  ::memcpy (decrypted_data, out_data, (length * sizeof (unsigned char)));
  env->ReleasePrimitiveArrayCritical (result, decrypted_data, 0);

  return result;
}
