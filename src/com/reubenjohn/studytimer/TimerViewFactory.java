package com.reubenjohn.studytimer;

import android.widget.TextView;


public class TimerViewFactory {
	private String defaultFormat="%MM:%SS.%s";
	public String getDefaultFormat(){
		return defaultFormat;
	}
	public void setDefaultFormat(String format){
		if(format!=null)
			defaultFormat=format;
	}
	public TimerView produceTimerView(){
		TimerView timerView=new TimerView();
		timerView.setFormat(defaultFormat);
		return timerView;
	}
	public TimerView produceTimerView(TextView textView){
		TimerView timerView=new TimerView();
		timerView.setFormat(defaultFormat);
		timerView.setTextView(textView);
		return timerView;
	}
	
}
