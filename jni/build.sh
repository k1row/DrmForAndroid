#!/bin/sh

rm -fr ../libs

/cygdrive/c/ndk/ndk-build -B NDK_DEBUG=1

mkdir -p ../libs/armeabi
cp -a libs/armeabi/libDecryptNative.so ../libs/armeabi
diff libs/armeabi/libDecryptNative.so ../libs/armeabi/libDecryptNative.so
