LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

#LOCAL_PREBUILT_LIBS := $(LOCAL_PATH)/libs/libcrypto
#include $(BUILD_MULTI_PREBUILT)

include $(CLEAR_VARS)

LOCAL_MODULE := DecryptNative
LOCAL_C_INCLUDES += $(LOCAL_PATH)/openssl \
                    $(LOCAL_PATH)/openssl/include \
                    $(LOCAL_PATH)/openssl/include/openssl \
                    $(LOCAL_PATH)/openssl/crypto \
                    $(LOCAL_PATH)/openssl/crypto/aes
LOCAL_CFLAGS += $(LOCAL_C_INCLUDES:%=-I%)
#LOCAL_CPPFLAGS += -fexceptions -frtti

#LOCAL_LDLIBS    := -ldl -llog -l$(LOCAL_PATH)/libs
LOCAL_LDLIBS := -ldl -llog $(call host-path, $(LOCAL_PATH)/libs/libcrypto.a)

LOCAL_SRC_FILES := com_camobile_camdrm_DecryptCamDrm.cpp

#LOCAL_STATIC_LIBRARIES := $(call host-path, $(LOCAL_PATH)/libs/libcrypto.a)

#LOCAL_ARM_MODE=arm

include $(BUILD_SHARED_LIBRARY)
#include $(BUILD_STATIC_LIBRARY)
#include $(PREBUILT_SHARED_LIBRARY)
