LOCAL_PATH := $(call my-dir)

subdirs := $(addprefix $(NDK_PROJECT_PATH)/,$(addsuffix /Android.mk, \
		crypto \
		ssl \
		apps \
	))

include $(subdirs)

include $(CLEAR_VARS)


LOCAL_MODULE:= libopenssl-static
#LOCAL_SRC_FILES :=
LOCAL_WHOLE_STATIC_LIBRARIES += libcrypto-static libssl-static
LOCAL_LDLIBS    := -llog
#LOCAL_C_INCLUDES := $(LOCAL_PATH) $(LOCAL_PATH)/include

#LOCAL_C_INCLUDES += $(local_c_includes)
#LOCAL_CFLAGS += $(LOCAL_C_INCLUDES:%=-I%)

#include $(BUILD_SHARED_LIBRARY)
include $(BUILD_STATIC_LIBRARY)
