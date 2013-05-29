LOCAL_PATH := $(call my-dir)

subdirs := $(addprefix /cygdrive/c/bin/android-ndk-r6/sources/openssl/,$(addsuffix /Android.mk, \
		crypto \
		ssl \
		apps \
	))

include $(subdirs)

include $(CLEAR_VARS)
LOCAL_SRC_FILES:=
LOCAL_C_INCLUDES:=
LOCAL_WHOLE_STATIC_LIBRARIES += libcrypto-static libssl-static
LOCAL_MODULE:= libopenssl-static
include $(BUILD_STATIC_LIBRARY)
