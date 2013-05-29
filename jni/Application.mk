#APP_PROJECT_PATH := $(call my-dir)
APP_PROJECT_PATH := /cygdrive/d/Develop/Eclipse/CamDrm

APP_MODULES      := DecryptNative
APP_STL          := stlport_static
#APP_STL          := gnustl_static

STLPORT_FORCE_REBUILD := true

#APP_BUILD_SCRIPT := $(APP_PROJECT_PATH)/Android.mk
APP_BUILD_SCRIPT := $(APP_PROJECT_PATH)/jni/Android.mk

APP_OPTIM:=debug
#APP_OPTIM:=release
