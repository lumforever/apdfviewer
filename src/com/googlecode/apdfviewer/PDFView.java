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
package com.googlecode.apdfviewer;

import java.io.FileNotFoundException;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PointF;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

/**
 * View class for render PDF document. 
 * @author Li Wenhao
 */
public class PDFView extends View {
	final static String TAG = "PDFView";
	
	/**
	 * Interface for listener the status of the PDF view.
	 */
	public interface StatusListener {
		/**
		 * Called when PDFView start to load a document.
		 */
		public void onLoadingStart(PDFView v);
		
		/**
		 * Called when load is finished.
		 */
		public void onLoadingEnd(PDFView v);
		
		/**
		 * Called when PDFView start to render a page.
		 */
		public void onRenderingStart(PDFView v);
		
		/**
		 * Called when rendering is finished. 
		 */
		public void onRenderingEnd(PDFView v);
		
		/**
		 * page changed.
		 */
		public void onPageChanged(PDFView v, int page);
		
		/**
		 * error.
		 */
		public void onError(PDFView v, String msg);
	}

	StatusListener m_listener;
	
	/**
	 * offset of current slice.
	 */
	Point m_offset = new Point();
	
	/**
     * The PDFDocument object
     */
    private PDFDocument m_doc = null;
    
    /**
     * file descriptor of the PDF document.
     */
    private AssetFileDescriptor m_descriptor = null;
    
    /**
     * current page number, default is 1.
     */
    private int m_current_page = 1;
    
    /**
     * zoom factor.
     */
    private float m_zoom_factor = 1.0F;
    
    /**
     * system DPI.
     */
    private PointF m_sys_dpi = new PointF();
    
    /**
     * bitmap as cache.
     */
    private Bitmap m_cache_bitmap = null;
    
    /**
     * canvas to draw the cache.
     */
    private Canvas m_cache_canvas = new Canvas();
    
    /**
     * bitmap configure
     */
    Bitmap.Config m_bitmap_config = Bitmap.Config.ARGB_8888;
    

	/**
	 * @see android.view.View#View(android.content.Context)
	 */
	public PDFView(Context context) {
		super(context);
		initView();
	}

	/**
	 * @see android.view.View#View(android.content.Context)
	 */
	public PDFView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		initView();
	}

	/**
	 * @see android.view.View#View(android.content.Context)
	 */
	public PDFView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		initView();
	}
	
	/**
	 * @return the status listener
	 */
	public StatusListener getStatusListener() {
		return m_listener;
	}

	/**
	 * @param mListener the m_listener to set
	 */
	public void setStatusListener(StatusListener l) {
		m_listener = l;
	}

	
	private void initConfig() {
		// get system DPI.
		WindowManager manager = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
		if (manager == null)
			return;
		DisplayMetrics metrics = new DisplayMetrics();
		manager.getDefaultDisplay().getMetrics(metrics);
		m_sys_dpi.set(metrics.xdpi, metrics.ydpi);

		// bitmap configure
		switch (manager.getDefaultDisplay().getPixelFormat()) {
		case PixelFormat.A_8:
			m_bitmap_config = Bitmap.Config.ALPHA_8;
			break;
		case PixelFormat.RGB_565:
			m_bitmap_config = Bitmap.Config.RGB_565;
			break;
		case PixelFormat.RGBA_4444:
			m_bitmap_config = Bitmap.Config.ARGB_4444;
			break;
		case PixelFormat.RGBA_8888:
			m_bitmap_config = Bitmap.Config.ARGB_8888;
			break;
		}
	}
	
	private void initView() {
		setClickable(true);
		
		// initialize configure
		initConfig();
				
		// touch scroll handler
		OnTouchListener l = new View.OnTouchListener(){
			private GestureDetector g = new GestureDetector(getContext(), 
					new GestureDetector.SimpleOnGestureListener(){
				public boolean onScroll(MotionEvent e1, MotionEvent e2, 
						float distanceX, float distanceY) {
					
					if (m_doc == null)
						return false;
					
					m_offset.offset((int)distanceX, (int)distanceY);

					postInvalidate();

					return true;
				}
			});
			
			public boolean onTouch(View v, MotionEvent event) {
				if (g != null) {
					return g.onTouchEvent(event);
				}
				
				return false;
			}
		};
		
		setOnTouchListener(l);
	}

	private void ensureOffset() {
		float zoom = getRealZoomFactor();

		int w = (int)(m_doc.getPageMediaWidth(m_current_page)*m_sys_dpi.x*zoom/72.0F);
		int h = (int)(m_doc.getPageMediaHeight(m_current_page)*m_sys_dpi.y*zoom/72.0F);

		if (m_offset.x > w - getWidth())
			m_offset.x = w - getWidth();

		if (m_offset.x < 0)
			m_offset.x = 0;

		if (m_offset.y > h - getHeight())
			m_offset.y = h - getHeight();

		if (m_offset.y < 0)
			m_offset.y = 0;
	}

	/**
	 * Open PDF contents from the URI.
	 * @param uri
	 */
	public void openUri(Uri uri) {
		// reset
		if (m_doc != null) {
			// TODO: clean up?
			m_doc = null;
		}
		m_current_page = 1;
		
		// open uri
		try {
			m_descriptor = getContext().getContentResolver().openAssetFileDescriptor(uri, "r");
			if (m_descriptor == null) {
				Log.e(TAG, "File desciptor is null.");
			}
		} catch (FileNotFoundException e) {
			Log.e(TAG, "Open file failed.");
			return;
		}
		
		// open document
		m_doc = new PDFDocument(m_descriptor.getFileDescriptor(), "", "");
		if (!m_doc.isOk()) {
			// TODO: report error.
			return;
		}
		
		pageChanged();
	}
	
	private void pageChanged() {
		if (m_listener != null) {
			m_listener.onPageChanged(this, m_current_page);
		}
		
		m_offset.set(0, 0);
		dirty();
	}
	
	/**
	 * Show next page if exist.
	 */
	public void nextPage() {
		if (m_doc != null && m_current_page != m_doc.getNumPages()) {
			m_current_page++;

			pageChanged();
		}
		
	}
	
	/**
	 * Show previews page if exist.
	 */
	public void prevPage() {
		if (m_current_page != 1) {
			m_current_page--;
			pageChanged();
		}
	}
	
	/**
	 * Set zoom factor.
	 * @param z the zoom factor, < 0 means fit width.
	 */
	public void setZoomFactor(float z) {
		if (Float.compare(m_zoom_factor, z) != 0) {
			m_zoom_factor = z;
			// reset offset?
			dirty();
		}
	}
	
	/**
	 * Get real zoom factor.
	 */
	private float getRealZoomFactor() {
		if (m_zoom_factor <= +0.0F) {
			if (m_doc == null)
				return 1.0F;
			return (float) (getWidth()*72.0F/m_doc.getPageMediaWidth(m_current_page)/m_sys_dpi.x);
		}
		
		return m_zoom_factor;
	}
	
	/**
	 * Get current zoom factor.
	 */
	public float getZoomFactor() {
		return m_zoom_factor;
	}
	
	/**
	 * Goto the given page.
	 * @param page the page number.
	 */
	public void gotoPage(int page) {
		if (page != m_current_page && page > 0 && m_doc != null && page <= m_doc.getNumPages()) {
			m_current_page = page;
			pageChanged();
		}
	}
	
	private void dirty() {
		if (m_cache_bitmap != null) {
			m_cache_bitmap.recycle();
			m_cache_bitmap = null;
		}
		
		invalidate();
	}
	
	private boolean isCached() {
		return (m_cache_bitmap != null);
	}
	
	private boolean ensureCache() {
		float zoom = getRealZoomFactor();

		int w = (int)(m_doc.getPageMediaWidth(m_current_page)*m_sys_dpi.x*zoom/72.0F);
		int h = (int)(m_doc.getPageMediaHeight(m_current_page)*m_sys_dpi.y*zoom/72.0F);

		if (m_cache_bitmap == null || 
				m_cache_bitmap.getWidth() != w || m_cache_bitmap.getHeight() != h) {

			if (m_cache_bitmap != null)
				m_cache_bitmap.recycle();

			try {
				m_cache_bitmap = Bitmap.createBitmap(w, h, m_bitmap_config);
			} catch (Throwable a) {
				return false;
			}
			m_cache_canvas.setBitmap(m_cache_bitmap);
		}
		
		return true;
	}
	
	private void drawCache(boolean refresh) {
		m_doc.drawPage(m_cache_canvas, m_current_page);
		
		if (refresh) {
			postInvalidate();
		}
	}

	/**
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		// do nothing if no document loaded.
		if (m_doc == null)
			return;
		
		// set document DPI.
		float zoom = getRealZoomFactor();
		m_doc.setXdpi(m_sys_dpi.x*zoom);
		m_doc.setYdpi(m_sys_dpi.y*zoom);
		
		// ensure offset is right.
		ensureOffset();
		
		// draw
		if (isCached()) {
			canvas.drawBitmap(m_cache_bitmap, -m_offset.x, -m_offset.y, null);
		} else {
			if (ensureCache()) {
				drawCache(true);
			} else {
				// use drawPageSlice.
				m_doc.drawPageSlice(canvas, m_current_page, 
						m_offset.x, m_offset.y, getWidth(), getHeight());
			}
		}		
	}

	public int getPagesCount() {
		if (m_doc != null)
			return m_doc.getNumPages();
		else
			return 1;
	}

	public int getCurrentPage() {
		return m_current_page;
	}
	
}
