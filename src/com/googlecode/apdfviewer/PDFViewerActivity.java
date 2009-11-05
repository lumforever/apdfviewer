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

import com.googlecode.apdfviewer.PDFView.StatusListener;
import com.googlecode.apdfviewer.PagePickerDialog.OnPageSetListener;

import com.googlecode.apdfviewer.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;


/**
 * @author Li Wenhao
 */
public class PDFViewerActivity extends Activity {
	//private final static String TAG = "PDFViewerActivity";
	
    private static final String PAGE = "page";
    private static final String ZOOM = "zoom";
    
    private static final int ABOUT = 1;
    
	PDFView m_pdf_view;
	PagePickerDialog m_go_dialog;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
		
		// Check the intent for the content to view
		Intent intent = getIntent();
		if (intent.getData() == null)
			return;

		Uri uri = intent.getData();		
		m_pdf_view = (PDFView)findViewById(R.id.view);
		
		initZoomSpinner();
		initButtons();
		initListener();

		m_pdf_view.openUri(uri);
		
		initGoDialog();
	}
	
	private void initGoDialog() {
		m_go_dialog = new PagePickerDialog(this);
		m_go_dialog.setMax(m_pdf_view.getPagesCount());
		m_go_dialog.setOnPageSetListener(new OnPageSetListener() {
			public void onPageSet(DialogInterface picker, int page) {
				m_pdf_view.gotoPage(page);				
			}
		});
	}
	
	private void initListener() {
		m_pdf_view.setStatusListener(new StatusListener() {

			public void onError(PDFView v, String msg) {
				// TODO Auto-generated method stub
				
			}

			public void onLoadingEnd(PDFView v) {
				// TODO Auto-generated method stub
				
			}

			public void onLoadingStart(PDFView v) {
				// TODO Auto-generated method stub
				
			}

			public void onPageChanged(PDFView v, int page) {
				// TODO Auto-generated method stub
				ImageButton prev = (ImageButton)findViewById(R.id.prev_page);
				ImageButton next = (ImageButton)findViewById(R.id.next_page);
				prev.setEnabled(false);
				next.setEnabled(false);
				
				if (page != 1)
					prev.setEnabled(true);
				
				if (page != v.getPagesCount())
					next.setEnabled(true);
				
				TextView tv = (TextView)findViewById(R.id.page_number_view);
				tv.setText(page+ "/" + v.getPagesCount());
				
			}

			public void onRenderingEnd(PDFView v) {
				// TODO Auto-generated method stub
				
			}

			public void onRenderingStart(PDFView v) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	private void initButtons() {
		ImageButton btn = (ImageButton)findViewById(R.id.prev_page);
		btn.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
				m_pdf_view.prevPage();
			}
		});

		btn = (ImageButton)findViewById(R.id.next_page);
		btn.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
				m_pdf_view.nextPage();
			}
		});
		
		btn = (ImageButton)findViewById(R.id.go_page);
		btn.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
				m_go_dialog.setCurrent(m_pdf_view.getCurrentPage());
				m_go_dialog.show();
			}
		});
	}
	
	private void initZoomSpinner() {
		Spinner s = (Spinner)findViewById(R.id.zoom);
		
		s.setOnItemSelectedListener(new OnItemSelectedListener(){
			public void onNothingSelected(AdapterView<?> view) {
			}

			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				float factor = 1.0F;
				switch (pos) {
				case 1:
					factor = 0.25F;
					break;
				case 2:
					factor = 0.50F;
					break;
				case 3:
					factor = 0.75F;
					break;
				case 4:
					factor = 1.00F;
					break;
				case 5:
					factor = 1.25F;
					break;
				default:
					factor = -1.0F;
					break;
				}
				m_pdf_view.setZoomFactor(factor);
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt(PAGE, m_pdf_view.getCurrentPage());
		outState.putFloat(ZOOM, m_pdf_view.getZoomFactor());
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	/**
	 * @see android.app.Activity#onRestoreInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		
		m_pdf_view.setZoomFactor(savedInstanceState.getFloat(ZOOM));
		m_pdf_view.gotoPage(savedInstanceState.getInt(PAGE));
	}

	/**
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem item = menu.add(0, 0, 0, "About");
		item.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				showDialog(ABOUT);
				return true;
			}	
	    });
	    return true;
	}

	/**
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		if (id != ABOUT)
			return super.onCreateDialog(id);

		LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.about_dialog, null);

		AlertDialog.Builder b = new AlertDialog.Builder(this)
			.setView(v)
			.setTitle(R.string.app_name);
		AlertDialog d = b.create();
		d.setButton(getText(android.R.string.ok), (OnClickListener)null);
		
		return d;
	}
}
