LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := user

LOCAL_SRC_FILES := $(call all-subdir-java-files)

LOCAL_PACKAGE_NAME := PDFViewer

LOCAL_JNI_SHARED_LIBRARIES := libpoppler_jni

include $(BUILD_PACKAGE)

include $(call all-makefiles-under,$(LOCAL_PATH))
