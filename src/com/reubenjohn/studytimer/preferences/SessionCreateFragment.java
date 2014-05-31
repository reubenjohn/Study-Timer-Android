package com.reubenjohn.studytimer.preferences;

import android.annotation.TargetApi;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.util.Log;

import com.reubenjohn.studytimer.R;

@TargetApi(11)
public class SessionCreateFragment extends PreferenceFragment {

	private static int layout = R.xml.new_session;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("StudyTimer", "SessionCreateFragment");
		addPreferencesFromResource(SessionCreateFragment.layout); // outer class
		// private members seem to be visible for inner class, and
		// making it static made things so much easier
	}

	public void setPrefListener(OnPreferenceChangeListener listener) {
		Preference p = findPreference("checkbox_key");
		Log.d("StudyTimer", "PreferenceListener set to fragment: "
				+ (p != null));
		if (p != null) {
			p.setOnPreferenceChangeListener(listener);
		}
	}
}