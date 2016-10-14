package com.example.tweekend;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public class SampleColorView extends View{



	public SampleColorView(Context context) {
		super(context);
	}

	public SampleColorView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void changeColor(int c) {
		this.setBackgroundColor(c);
	}
}