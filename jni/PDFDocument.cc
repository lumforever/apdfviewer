/*
 * Copyright (C) 2009 Li Wenhao <liwenhao.g@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <stdio.h>
#include <assert.h>
#include <unistd.h>
#include <fcntl.h>

#include <jni.h>

#include <poppler/PDFDoc.h>
#include <poppler/GlobalParams.h>
#include <poppler/AndroidOutputDev.h>

#include <SkCanvas.h>

#define LOG_NDEBUG 0
#define LOG_TAG "PDFDocument"
#include <cutils/log.h>

struct fields_t {
	jfieldID nativePDF;
	jfieldID useMediaBox;
	jfieldID crop;
	jfieldID hDPI;
	jfieldID vDPI;
	jfieldID rotate;
	jfieldID nativeCanvas;
	jfieldID fd;
};
static fields_t fields;

static double gethDPI(JNIEnv* env, jobject clazz) {
	return env->GetDoubleField(clazz, fields.hDPI);
}

static double getvDPI(JNIEnv* env, jobject clazz) {
	return env->GetDoubleField(clazz, fields.vDPI);
}

static int getRotate(JNIEnv* env, jobject clazz) {
	return env->GetIntField(clazz, fields.rotate);
}

static jboolean getUseMediaBox(JNIEnv* env, jobject clazz) {
	return env->GetBooleanField(clazz, fields.useMediaBox);
}

static jboolean getCrop(JNIEnv* env, jobject clazz) {
	return env->GetBooleanField(clazz, fields.crop);
}

//
// helper function to throw an exception
//
static void throwException(JNIEnv *env, const char* ex, const char* fmt,
		int data) {
	if (jclass cls = env->FindClass(ex)) {
		char msg[1000];
		sprintf(msg, fmt, data);
		env->ThrowNew(cls, msg);
		env->DeleteLocalRef(cls);
	}
}

static PDFDoc* getDoc(JNIEnv* env, jobject clazz) {
	return (PDFDoc*) env->GetIntField(clazz, fields.nativePDF);
}

static SkCanvas* getCanvas(JNIEnv* env, jobject jcanvas) {
	return (SkCanvas*) env->GetIntField(jcanvas, fields.nativeCanvas);
}

/*
 * Method:    getPageMediaWidth
 * Signature: (I)D
 */
static jdouble getPageMediaWidth(JNIEnv *env, jobject clazz, jint page) {
	PDFDoc *doc = getDoc(env, clazz);
	return doc->getPageMediaWidth(page);
}

/*
 * Method:    getPageMediaHeight
 * Signature: (I)D
 */
static jdouble getPageMediaHeight(JNIEnv *env, jobject clazz, jint page) {
	PDFDoc *doc = getDoc(env, clazz);
	return doc->getPageMediaHeight(page);
}

/*
 * Method:    getPageCropWidth
 * Signature: (I)D
 */
static jdouble getPageCropWidth(JNIEnv *env, jobject clazz, jint page) {
	PDFDoc *doc = getDoc(env, clazz);
	return doc->getPageCropWidth(page);
}

/*
 * Method:    getPageCropHeight
 * Signature: (I)D
 */
static jdouble getPageCropHeight(JNIEnv *env, jobject clazz, jint page) {
	PDFDoc *doc = getDoc(env, clazz);
	return doc->getPageCropHeight(page);
}

/*
 * Method:    getPageRotate
 * Signature: (I)I
 */
static jint getPageRotate(JNIEnv *env, jobject clazz, jint page) {
	PDFDoc *doc = getDoc(env, clazz);
	return doc->getPageRotate(page);
}

/*
 * Method:    isOk
 * Signature: ()Z
 */
static jboolean isOk(JNIEnv *env, jobject clazz) {
	PDFDoc *doc = getDoc(env, clazz);
	return doc->isOk();
}

/*
 * Method:    getNumPages
 * Signature: ()I
 */
static jint getNumPages(JNIEnv *env, jobject clazz) {
	PDFDoc *doc = getDoc(env, clazz);
	return doc->getNumPages();
}

/*
 * Method:    drawPage
 * Signature: (Landroid/graphics/Canvas;I)V
 */
static void drawPage(JNIEnv *env, jobject clazz, jobject jcanvas, jint page) {
	PDFDoc *doc = getDoc(env, clazz);

	if (!doc || !doc->isOk())
		return;

	SkCanvas *canvas = getCanvas(env, jcanvas);

	AndroidOutputDev out(canvas, doc->getXRef());
	double hDPI = gethDPI(env, clazz);
	double vDPI = getvDPI(env, clazz);
	int rotate = getRotate(env, clazz);
	int useMediaBox = getUseMediaBox(env, clazz);
	int crop = getCrop(env, clazz);

	LOGV("page: %d", page);

	doc->displayPage(&out, (int)page, hDPI, vDPI, rotate, useMediaBox, crop,
			0, NULL, NULL, NULL, NULL);
}

/*
 * Method:    drawPages
 * Signature: (Landroid/graphics/Canvas;II)V
 */
static void drawPages(JNIEnv *env, jobject clazz, jobject jcanvas,
		jint firstPage, jint lastPage) {
	PDFDoc *doc = getDoc(env, clazz);

	if (!doc || !doc->isOk())
		return;

	SkCanvas *canvas = getCanvas(env, jcanvas);

	AndroidOutputDev out(canvas, doc->getXRef());
	double hDPI = gethDPI(env, clazz);
	double vDPI = getvDPI(env, clazz);
	int rotate = getRotate(env, clazz);
	int useMediaBox = getUseMediaBox(env, clazz);
	int crop = getCrop(env, clazz);

	doc->displayPages(&out, (int)firstPage, (int)lastPage, hDPI, vDPI, rotate,
			useMediaBox, crop, 0, NULL, NULL, NULL, NULL);
}

/*
 * Method:    drawPageSlice
 * Signature: (Landroid/graphics/Canvas;IIIII)V
 */
static void drawPageSlice(JNIEnv *env, jobject clazz, jobject jcanvas,
		jint page, jint sliceX, jint sliceY, jint sliceW, jint sliceH) {
	PDFDoc *doc = getDoc(env, clazz);

	if (!doc || !doc->isOk())
		return;

	SkCanvas *canvas = getCanvas(env, jcanvas);

	AndroidOutputDev out(canvas, doc->getXRef());
	double hDPI = gethDPI(env, clazz);
	double vDPI = getvDPI(env, clazz);
	int rotate = getRotate(env, clazz);
	int useMediaBox = getUseMediaBox(env, clazz);
	int crop = getCrop(env, clazz);

	doc->displayPageSlice(&out, (int) page, hDPI, vDPI, rotate, useMediaBox, crop, 0,
			sliceX, sliceY, sliceW, sliceH, NULL, NULL, NULL, NULL);
}

/*
 * Method:    init_native
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)I
 */
static jint native_init(JNIEnv *env, jobject clazz, jobject descriptor,
		jstring ownerPassword, jstring userPassword) {
	int fd = env->GetIntField(descriptor, fields.fd);
	FILE *file = fdopen(fd, "r");
	if (!file) {
		LOGV("Open fd failed: %d", fd);
	}

	Object obj;
	obj.initNull();
	FileStream *stream = new FileStream(file, 0, gFalse, 0, &obj);
	if (!stream) {
		LOGV("Create stream failed: %d", fd);
	}

	PDFDoc *doc = new PDFDoc(stream);
	if (!doc->isOk()) {
		LOGV("Open failed: %d", doc->getErrorCode());
	}

	return (jint) doc;
}

static void native_class_init(JNIEnv* env, jclass clazz) {
	fields.nativePDF = env->GetFieldID(clazz, "mNativePDF", "I");
	fields.useMediaBox = env->GetFieldID(clazz, "mUseMediaBox", "Z");
	fields.crop = env->GetFieldID(clazz, "mCrop", "Z");
	fields.hDPI = env->GetFieldID(clazz, "mH_DPI", "D");
	fields.vDPI = env->GetFieldID(clazz, "mV_DPI", "D");
	fields.rotate = env->GetFieldID(clazz, "mRotate", "I");

	jclass canvas = env->FindClass("android/graphics/Canvas");
	fields.nativeCanvas = env->GetFieldID(canvas, "mNativeCanvas", "I");

	jclass fd = env->FindClass("java/io/FileDescriptor");
	fields.fd = env->GetFieldID(fd, "descriptor", "I");

	globalParams = new GlobalParams();

}

// ----------------------------------------------------------------------------

static JNINativeMethod gMethods[] = {
		{ "native_class_init", "()V", (void*) native_class_init },
		{ "getPageMediaWidth", "(I)D", (void*) getPageMediaWidth },
		{ "getPageMediaHeight", "(I)D",	(void*) getPageMediaHeight },
		{ "getPageCropWidth", "(I)D", (void*) getPageCropWidth },
		{ "getPageCropHeight", "(I)D", (void*) getPageCropHeight },
		{ "getPageRotate", "(I)I", (void*) getPageRotate },
		{ "isOk", "()Z", (void*) isOk },
		{"getNumPages", "()I", (void*) getNumPages },
		{ "drawPage", "(Landroid/graphics/Canvas;I)V", (void*) drawPage },
		{ "drawPages", "(Landroid/graphics/Canvas;II)V", (void*) drawPages },
		{"drawPageSlice", "(Landroid/graphics/Canvas;IIIII)V", (void*) drawPageSlice },
		{ "native_init", "(Ljava/io/FileDescriptor;Ljava/lang/String;Ljava/lang/String;)I", (void*) native_init }
};

static int registerNativeMethods(JNIEnv* env, const char* className,
		JNINativeMethod* gMethods, int numMethods) {
	jclass clazz;

	clazz = env->FindClass(className);
	if (clazz == NULL) {
		fprintf(stderr, "Native registration unable to find class '%s'\n",
				className);
		return JNI_FALSE;
	}
	if (env->RegisterNatives(clazz, gMethods, numMethods) < 0) {
		fprintf(stderr, "RegisterNatives failed for '%s'\n", className);
		return JNI_FALSE;
	}

	return JNI_TRUE;
}

static int registerNatives(JNIEnv *env) {
	const char* const kClassPathName = "com/googlecode/apdfviewer/PDFDocument";

	return registerNativeMethods(env, kClassPathName, gMethods,
			sizeof(gMethods) / sizeof(gMethods[0]));
}

/*
 * Returns the JNI version on success, -1 on failure.
 */
JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved) {
	JNIEnv* env = NULL;
	jint result = -1;

	if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
		fprintf(stderr, "ERROR: GetEnv failed\n");
		goto bail;
	}
	assert(env != NULL);

	if (!registerNatives(env)) {
		fprintf(stderr, "ERROR: BinaryDictionary native registration failed\n");
		goto bail;
	}

	/* success -- return valid version number */
	result = JNI_VERSION_1_4;

	bail: return result;
}
