LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := libpoppler_jni

LOCAL_CPP_EXTENSION := .cc

LOCAL_SRC_FILES :=      \
	PDFDocument.cc

LOCAL_C_INCLUDES :=         	\
	$(LOCAL_PATH)/../poppler	\
	$(LOCAL_PATH)/../poppler/poppler	\
	external/skia/include/core	\
	$(JNI_H_INCLUDE)

LOCAL_CFLAGS += -DPLATFORM_ANDROID


LOCAL_SHARED_LIBRARIES +=   \
	libz				\
	libskia				\
	liblog

LOCAL_STATIC_LIBRARIES +=   \
	libpoppler			

LOCAL_PRELINK_MODULE := false

include $(BUILD_SHARED_LIBRARY)

