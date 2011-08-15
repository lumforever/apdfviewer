/*
 * Copyright (C) 2009 Li Wenhao <liwenhao.g@gmail.com>
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

import com.googlecode.apdfviewer.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

/**
 * Dialog for pickup page number.
 * 
 */
public class PagePickerDialog extends AlertDialog {
	
	private TextView m_message_view;
	private SeekBar m_seek_view;
	
	 /**
     * The callback interface.
     */
    public interface OnPageSetListener {

    	/**
    	 * 
    	 * @param picker
    	 * @param page
    	 */
        void onPageSet(DialogInterface picker, int page);
    }
    
    OnPageSetListener m_listener;
    
    public void setOnPageSetListener(OnPageSetListener l) {
    	m_listener = l;
    }

	/**
	 * @param context
	 */
	public PagePickerDialog(Context context) {
		super(context);

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.page_picker_dialog, null);
		setView(view);

		setIcon(R.drawable.go);
		setTitle(R.string.page_picker_title);
		
		m_message_view = (TextView)view.findViewById(R.id.page_picker_message);
		m_seek_view = (SeekBar)view.findViewById(R.id.page_picker_seeker);
		m_seek_view.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				updateMessage();				
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			
		});
		
		ImageButton btn = (ImageButton)view.findViewById(R.id.page_picker_minus);
		btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				m_seek_view.incrementProgressBy(-1);
			}
		});
		
		btn = (ImageButton)view.findViewById(R.id.page_picker_plus);
		btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				m_seek_view.incrementProgressBy(1);
			}
		});

		setButton(context.getText(android.R.string.ok), new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if (m_listener != null)
					m_listener.onPageSet(dialog, m_seek_view.getProgress());
			}
			
		});
		
		setButton2(context.getText(android.R.string.cancel), (OnClickListener) null);
	}
	
	public void setMax(int max) {
		if (max < 1) {
			throw new IllegalArgumentException();
		}
		
		m_seek_view.setMax(max);
	}
	
	public void setCurrent(int cur) {
		if (cur < 1 || cur > m_seek_view.getMax()) {
			throw new IllegalArgumentException();
		}
		m_seek_view.setProgress(cur);
	}
	
	private void updateMessage() {
		String msg = m_seek_view.getProgress() + "/" + m_seek_view.getMax();
		m_message_view.setText(msg);
	}

}
