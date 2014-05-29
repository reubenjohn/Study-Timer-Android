package com.reubenjohn.studytimer;

import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.TimePicker;

import com.reubenjohn.studytimer.StudyTimer.MODES;
import com.reubenjohn.studytimer.timming.Time;
import com.reubenjohn.studytimer.timming.frametimer.FrameTimer;
import com.reubenjohn.studytimer.timming.frametimer.FrameTimerListener;

public class TimerElementsFragment extends Fragment implements
		android.view.View.OnClickListener, FrameTimerListener {

	private int mode;
	private TextView tv_elapse, tv_total_elapse, tv_average;
	private TimerView elapse, totalElapse;
	int cached_lapCount;
	boolean realTimeAverageEnabled = true, running;
	int average;
	private long targetTime;

	private static class layout {
		public static View total_elapse;
	}

	static long defaultTargetTime = Time.getTimeInMilliseconds(0, 0, 1, 0, 0);

	protected static class keys {
		public static final String elapse = "ELAPSE";
		public static String totalElapse = "TOTAL_ELAPSE";
		public static String running = "RUNNING";
		public static String stopTime = "STOP_TIME_TIME";
		public static String targetTime = "TARGET_TIME";
	}

	public interface TimerElementsListener {
		public void onTotalElapseSetManually(long elapse);
	};

	public TimerElementsListener timerElementsListener;

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(keys.running, running);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.timer_elements_fragment, container,
				false);
		bridgeXML(v);
		initializeFeilds();
		if (savedInstanceState != null) {

			if (savedInstanceState.getBoolean(keys.running, false)) {
				start();
			}
		}
		return v;
	}

	@Override
	public void onResume() {
		super.onResume();
		SharedPreferences prefs = getActivity().getPreferences(
				Context.MODE_PRIVATE);
		Log.d("StudyTimer", "Timer Elements resume state: running=" + running);
		elapse.setElapse(prefs.getLong(keys.elapse, 0));
		totalElapse.setElapse(prefs.getLong(keys.totalElapse, 0));
		targetTime = prefs.getLong(keys.targetTime, defaultTargetTime);
		if (running) {
			elapse.setStartTime(prefs.getLong(keys.stopTime, 0));
			totalElapse.setStartTime(prefs.getLong(keys.stopTime, 0));
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		saveTimes();
	}

	@Override
	public void onStop() {
		saveTimes();
		super.onStop();
	}

	@Override
	public void onDestroy() {
		saveTimes();
		super.onDestroy();
	}

	public void start() {
		elapse.start();
		totalElapse.start();
		running = true;
	}

	public void stop() {
		elapse.stop();
		totalElapse.stop();
		running = false;
	}

	public void toggle() {
		if (running)
			running = false;
		else
			running = true;
		elapse.toggle();
		totalElapse.toggle();
	}

	public void lap(int lapCount) {
		elapse.reset();
		if (running)
			elapse.start();
		this.cached_lapCount = lapCount;
	}

	public void reset() {
		Log.d("StudyTimer", "TimerElements reset");
		elapse.reset();
		totalElapse.reset();
		cached_lapCount = 0;
		average = 0;
		targetTime = getActivity().getPreferences(Context.MODE_PRIVATE)
				.getLong(keys.targetTime, defaultTargetTime);
		resetSavedData();
	}

	public void setTotalElapse(long elapse) {
		Log.d("StudyTimer", "Total elapse set: " + elapse);
		totalElapse.setElapse(elapse);
		timerElementsListener.onTotalElapseSetManually(elapse);
	}

	public boolean isRunning() {
		return running;
	}

	protected void resetSavedData() {
		SharedPreferences prefs = getActivity().getPreferences(
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.remove(keys.elapse);
		editor.remove(keys.totalElapse);
		editor.remove(keys.stopTime);
		editor.commit();
	}

	public void initializeLapCount(int lapCount) {
		this.cached_lapCount = lapCount;
	}

	public String getFormatedElapse() {
		return elapse.timer.getFormattedTime();
	}

	public void setAverage(int average) {
		this.average = average;
		if (!realTimeAverageEnabled)
			tv_average.setText(Time.getFormattedTime("%MM:%SS.%sss", average));
	}

	public void setTargetTIme(long targetTime) {
		this.targetTime = targetTime;
		SharedPreferences.Editor editor = getActivity().getPreferences(
				Context.MODE_PRIVATE).edit();
		editor.putLong(keys.targetTime, targetTime);
		editor.commit();
	}

	public void addFrameTimerListenersTo(FrameTimer framer) {
		framer.addFrameTimerListener(elapse);
		framer.addFrameTimerListener(totalElapse);
		framer.addFrameTimerListener(this);
	}

	public void removeFrameTimerListenersFrom(FrameTimer framer) {
		framer.removeFrameTimerListener(elapse);
		framer.removeFrameTimerListener(totalElapse);
	}

	protected void bridgeXML(View v) {
		tv_elapse = (TextView) v.findViewById(R.id.tv_elapse);
		tv_total_elapse = (TextView) v.findViewById(R.id.tv_total_elapse);
		tv_average = (TextView) v.findViewById(R.id.tv_average);
		layout.total_elapse = (View) v.findViewById(R.id.total_elapse);
	}

	protected void initializeFeilds() {
		TimerViewFactory factory = new TimerViewFactory();
		factory.setDefaultFormat("%MM:%SS.%s");

		elapse = factory.produceTimerView(tv_elapse);
		totalElapse = factory.produceTimerView(tv_total_elapse);

		layout.total_elapse.setOnClickListener(this);

	}

	@Override
	public void onNewFrame() {
		if (realTimeAverageEnabled) {
			int realTimeAverage = (int) (average * cached_lapCount + elapse
					.getElapse()) / (cached_lapCount + 1);
			tv_average.setText(Time.getFormattedTime("%MM:%SS.%sss",
					realTimeAverage));
		}
	}

	@Override
	public void onEndFrame() {

	}

	@Override
	public void onReset() {
	}

	public long getElapse() {
		return elapse.getElapse();
	}

	public float getLapProgress() {
		if (targetTime == 0) {
			return -1;
		} else {
			return (100.f * elapse.getElapse()) / targetTime;
		}
	}

	public void saveTimes() {
		SharedPreferences prefs = getActivity().getPreferences(
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putLong(keys.elapse, elapse.getElapse());
		editor.putLong(keys.totalElapse, totalElapse.getElapse());
		editor.putLong(keys.stopTime, System.currentTimeMillis());
		editor.commit();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.total_elapse:
			if (mode == MODES.SESSION_EDIT) {
				Log.d("StudyTimer", "Showing total elapse dialog");
				showTotalElapseDialog();
			}
			break;
		}
	}

	private void showTotalElapseDialog() {
		TimePickerDialog picker = new TimePickerDialog(getActivity(),
				new OnTimeSetListener() {
					int callCount = 0;

					@Override
					public void onTimeSet(TimePicker picker, int minute,
							int second) {
						if (callCount == 1) {
							setTotalElapse(Time.getTimeInMilliseconds(0, 0,
									minute, second, 0));
						}
						callCount++;
					}
				}, 1, 0, true);
		picker.setTitle(R.string.session_edit_total_elapse_title);
		picker.setMessage(getResources().getString(
				R.string.session_edit_total_elapse_message));
		picker.show();
	}

	public void setMode(int MODE) {
		switch (MODE) {
		case MODES.NORMAL:
		case MODES.SESSION_EDIT:
			mode = MODE;
			Log.d("StudyTimer", "TimerElement mode set: " + mode);
			break;
		default:
			Log.d("StudyTimer", "Unknown TimerElement mode request received: " + MODE);
		}
	}

	public void setTimerElementsListener(TimerElementsListener listener) {
		timerElementsListener = listener;
	}

}
