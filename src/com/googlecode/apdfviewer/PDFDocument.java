/*
 * Copyright (C) 2009 Li Wenhao  <liwenhao.g@gmail.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package com.googlecode.apdfviewer;

import java.io.FileDescriptor;

import android.graphics.Canvas;

/**
 * @author Li Wenhao
 *
 */
public class PDFDocument {
	@SuppressWarnings("unused")
	private int mNativePDF = 0;
	
	private boolean mUseMediaBox = false;
	private boolean mCrop = false;
	private double mH_DPI = 72.0;
	private double mV_DPI = 72.0;
	private int mRotate;
	
	//public PDFDocument(String fileName, String ownerPassword, String userPassword) {
	//	mNativePDF = native_init(fileName, ownerPassword, userPassword);
	//}
	
	public PDFDocument(FileDescriptor fd, String ownerPassword, String userPassword) {
		mNativePDF = native_init(fd, ownerPassword, userPassword);
	}

	public boolean ismUseMediaBox() {
		return mUseMediaBox;
	}

	public void setmUseMediaBox(boolean mUseMediaBox) {
		this.mUseMediaBox = mUseMediaBox;
	}

	public boolean ismCrop() {
		return mCrop;
	}

	public void setmCrop(boolean mCrop) {
		this.mCrop = mCrop;
	}

	public double xdpi() {
		return mH_DPI;
	}

	public void setXdpi(double xdpi) {
		mH_DPI = xdpi;
	}

	public double ydpi() {
		return mV_DPI;
	}

	public void setYdpi(double ydpi) {
		mV_DPI = ydpi;
	}

	public int getmRotate() {
		return mRotate;
	}

	public void setmRotate(int mRotate) {
		this.mRotate = mRotate;
	}
	
	public native double getPageMediaWidth(int page);
	public native double getPageMediaHeight(int page);
	public native double getPageCropWidth(int page);
	public native double getPageCropHeight(int page);
	public native int getPageRotate(int page);	
	public native boolean isOk();
	public native int getNumPages();
	
	public native void drawPage(Canvas canvas, int page);
	
	public native void drawPages(Canvas canvas, int firstPage, int lastPage);

	public native void drawPageSlice(Canvas canvas, int page,
			int sliceX, int sliceY, int sliceW, int sliceH);
	
	private native int native_init(FileDescriptor fd, String ownerPassword, String userPassword);
    
	private static native void native_class_init();
	
    static {
    	try {
    		System.loadLibrary("poppler_jni");
    		native_class_init();
    	} catch (Throwable e) {
    		e.printStackTrace();
    	}
    }
}
