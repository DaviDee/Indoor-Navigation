package inavi.prototyp.gui;

import inavi.prototyp.main.R;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class RoutePrefsActivity extends PreferenceActivity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.route_prefs);
	}

}
