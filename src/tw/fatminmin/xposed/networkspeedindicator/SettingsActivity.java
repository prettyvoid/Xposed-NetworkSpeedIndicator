package tw.fatminmin.xposed.networkspeedindicator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.util.Log;

import com.h6ah4i.android.compat.preference.MultiSelectListPreferenceCompat;

public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	private static final String TAG = SettingsActivity.class.getSimpleName();
	private SharedPreferences mPrefs;
	private final Set<String> networkTypeEntries = new LinkedHashSet<String>();
	private final Set<String> networkTypeValues = new LinkedHashSet<String>();

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getPreferenceManager().setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
		addPreferencesFromResource(R.xml.settings);
		mPrefs = getPreferenceScreen().getSharedPreferences();
		
	}

	@Override
	public void onResume() {
		super.onResume();
		
		@SuppressWarnings("deprecation")
		PreferenceGroup settings = (PreferenceGroup) findPreference("settings");
		refreshNetworkTypes();
		refreshPreferences(mPrefs, null);
		setAllSummary(settings);
		
		mPrefs.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onPause() {
		mPrefs.unregisterOnSharedPreferenceChangeListener(this);
		super.onPause();
	}

	void setAllSummary(PreferenceGroup group) {
		for(int i = 0; i < group.getPreferenceCount(); i++) {
			if(group.getPreference(i) instanceof PreferenceGroup) {
				setAllSummary((PreferenceGroup) group.getPreference(i));
			}
			else {
				setSummary(group.getPreference(i));
			}
		}
	}

	private void refreshNetworkTypes() {
		// Get the network types supported by device
		ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo[] allNetInfo = cm.getAllNetworkInfo();
		
		networkTypeEntries.clear();
		networkTypeValues.clear();
		
		if (allNetInfo == null) {
			Log.e(TAG, "Array containing all network info is null!");
		}
		else {
			List<String> resNetworkTypeEntries  = Arrays.asList(getResources().getStringArray(R.array.networktype_entries));
			List<String> resNetworkTypeValues   = Arrays.asList(getResources().getStringArray(R.array.networktype_values));
			
			for (NetworkInfo netInfo : allNetInfo) {
				if (netInfo == null) {
					Log.w(TAG, "Network info object is null.");
				} else {
					String netInfoType = String.valueOf(netInfo.getType());
					int index = resNetworkTypeValues.indexOf(netInfoType);
					if (index >= 0 && index < resNetworkTypeEntries.size()) {
						networkTypeEntries.add(resNetworkTypeEntries.get(index));
						networkTypeValues .add(resNetworkTypeValues .get(index));
					}
				}
			}
		}
	}
	
    void setSummary(Preference preference) {
	    if (preference instanceof ListPreference) {
            ListPreference listPref = (ListPreference) preference;
            preference.setSummary(createListPrefSummary(listPref));
        }
        else if(preference instanceof EditTextPreference) {
            EditTextPreference editPref = (EditTextPreference) preference;
            preference.setSummary(createEditTextSummary(editPref));
        }
        else if(preference instanceof MultiSelectListPreferenceCompat) {
        	MultiSelectListPreferenceCompat mulPref = (MultiSelectListPreferenceCompat) preference;
        	preference.setSummary(createMultiSelectSummary(mulPref));
        }
	}
    
    private String createListPrefSummary(ListPreference listPref) {
    	String summaryText = listPref.getEntry().toString();
    	
    	if (Common.KEY_UNIT_MODE.equals(listPref.getKey())) {
    		try {
    			int listValue = Integer.parseInt(listPref.getValue());
    			String[] summaryTexts = getResources().getStringArray(R.array.unit_mode_summary);
    			summaryText += String.format(" (%s)", summaryTexts[listValue]);
    		} catch (Exception e) {
    			//reset
    			summaryText = listPref.getEntry().toString();
    		}
    	}
    	
    	return summaryText;
    }
    
    private String createEditTextSummary(EditTextPreference editPref) {
    	String summaryText = editPref.getText();
    	
    	if (Common.KEY_UPDATE_INTERVAL.equals(editPref.getKey())) {
    		try {
    			summaryText = String.valueOf(Integer.parseInt(summaryText));
    		} catch (Exception e) {
    			summaryText = String.valueOf(Common.DEF_UPDATE_INTERVAL);
    		}
    		summaryText = formatWithUnit(summaryText, getString(R.string.unit_update_interval));
    	}
    	else if (Common.KEY_HIDE_BELOW.equals(editPref.getKey())) {
    		int value;
    		try {
    			value = Integer.parseInt(summaryText);
    		} catch (Exception e) {
    			value = Common.DEF_HIDE_BELOW;
    		}
    		if (value <= 0) {
    			summaryText = getString(R.string.sum0_hide_below);
    		} else if (value == 1) {
    			summaryText = getString(R.string.sum1_hide_below);
    		} else {
    			summaryText = formatWithUnit(String.valueOf(value), getString(R.string.unit_hide_below));
    		}
    	}
    	else if (Common.KEY_FONT_SIZE.equals(editPref.getKey())) {
    		try {
    			summaryText = String.valueOf(Float.parseFloat(summaryText));
    		} catch (Exception e) {
    			summaryText = String.valueOf(Common.DEF_FONT_SIZE);
    		}
    		summaryText = formatWithUnit(summaryText, getString(R.string.unit_font_size));
    	}
    	
    	return summaryText;
    }
    
    private String formatWithUnit(String value, String unit) {
		if (unit.contains("%s")) {
			return String.format(unit, value);
		} else {
			return value + " " + unit;
		}
	}
    
    private String createMultiSelectSummary(MultiSelectListPreferenceCompat mulPref) {
    	if (mulPref.getValues().size() == 0) {
    		return getString(R.string.summary_none);
    	}
    	
    	String summary = "";
    	for(String str : mulPref.getValues()) {
    		if(summary.length() > 0) {
    			summary += ", ";
    		}
    		summary += str;
    	}
    	return summary;
    }
	
    @SuppressWarnings("deprecation")
	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		Intent intent = new Intent();
		Log.i(TAG, "onSharedPreferenceChanged "+key);
		
		refreshPreferences(prefs, key);
		setSummary(findPreference(key));
		
		intent.setAction(Common.ACTION_SETTINGS_CHANGED);
		
		if (key.equals(Common.KEY_SHOW_UPLOAD_SPEED)) {
		    intent.putExtra(key, prefs.getBoolean(key, Common.DEF_SHOW_UPLOAD_SPEED));
		}
		else if (key.equals(Common.KEY_SHOW_DOWNLOAD_SPEED)) {
		    intent.putExtra(key, prefs.getBoolean(key, Common.DEF_SHOW_DOWNLOAD_SPEED));
		}
		else if (key.equals(Common.KEY_FORCE_UNIT)) {
			intent.putExtra(key, Common.getPrefInt(prefs, key, Common.DEF_FORCE_UNIT));
		}
		else if (key.equals(Common.KEY_UNIT_MODE)) {
			intent.putExtra(key, Common.getPrefInt(prefs, key, Common.DEF_UNIT_MODE));
		}
		else if (key.equals(Common.KEY_HIDE_UNIT)) {
			intent.putExtra(key, prefs.getBoolean(key, Common.DEF_HIDE_UNIT));
		}
		else if (key.equals(Common.KEY_NO_SPACE)) {
			intent.putExtra(key, prefs.getBoolean(key, Common.DEF_NO_SPACE));
		}
		else if (key.equals(Common.KEY_HIDE_B)) {
			intent.putExtra(key, prefs.getBoolean(key, Common.DEF_HIDE_B));
		}
		else if (key.equals(Common.KEY_HIDE_BELOW)) {
			intent.putExtra(key, Common.getPrefInt(prefs, key, Common.DEF_HIDE_BELOW));
		}
		else if (key.equals(Common.KEY_SHOW_SUFFIX)) {
			intent.putExtra(key, prefs.getBoolean(key, Common.DEF_SHOW_SUFFIX));
		}
		else if (key.equals(Common.KEY_FONT_SIZE)) {
		    intent.putExtra(key, Common.getPrefFloat(prefs, key, Common.DEF_FONT_SIZE));
		}
		else if (key.equals(Common.KEY_POSITION)) {
		    intent.putExtra(key, Common.getPrefInt(prefs, key, Common.DEF_POSITION));
		}
		else if (key.equals(Common.KEY_SUFFIX)) {
            intent.putExtra(key, Common.getPrefInt(prefs, key, Common.DEF_SUFFIX));
        }
		else if (key.equals(Common.KEY_SMALL_TRIANGLE)) {
			intent.putExtra(key, prefs.getBoolean(key, Common.DEF_SMALL_TRIANGLE));
		}
		else if (key.equals(Common.KEY_NETWORK_TYPE)) {
			MultiSelectListPreferenceCompat mulPref = (MultiSelectListPreferenceCompat) findPreference(key);
			HashSet<String> value = (HashSet<String>) mulPref.getValues();
			intent.putExtra(key, value);
		}
		else if (key.equals(Common.KEY_DISPLAY)) {
            intent.putExtra(key, Common.getPrefInt(prefs, key, Common.DEF_DISPLAY));
        }
		else if (key.equals(Common.KEY_UPDATE_INTERVAL)) {
            intent.putExtra(key, Common.getPrefInt(prefs, key, Common.DEF_UPDATE_INTERVAL));
        }
		else if (key.equals(Common.KEY_FONT_COLOR)) {
			intent.putExtra(key, prefs.getBoolean(key, Common.DEF_FONT_COLOR));
		}
		else if (key.equals(Common.KEY_COLOR)) {
			intent.putExtra(key, prefs.getInt(key, Common.DEF_COLOR));
		}
		else if (key.equals(Common.KEY_FONT_STYLE)) {
			MultiSelectListPreferenceCompat mulPref = (MultiSelectListPreferenceCompat) findPreference(key);
			HashSet<String> value = (HashSet<String>) mulPref.getValues(); 
			intent.putExtra(key, value);
		}
		else {
			intent.setAction(null);
		}
		
		if (intent.getAction() != null) {
			sendBroadcast(intent);
			Log.i(TAG, "sendBroadcast");
		}
	}
	
	@SuppressWarnings("deprecation")
	private void refreshPreferences(SharedPreferences prefs, String key) {
		// When key is null, refresh everything.
		// When a key is provided, refresh only for that key.
		
		if (key==null) { //only first time
			MultiSelectListPreferenceCompat mulPref = (MultiSelectListPreferenceCompat) findPreference(Common.KEY_NETWORK_TYPE);
			mulPref.setEntries(networkTypeEntries.toArray(new String[]{}));
			mulPref.setEntryValues(networkTypeValues.toArray(new String[]{}));
		}
		
		if (key==null || key.equals(Common.KEY_HIDE_UNIT)) {
	    	//enable only when hide unit is disabled
	    	boolean prefHideUnit = prefs.getBoolean(Common.KEY_HIDE_UNIT, Common.DEF_HIDE_UNIT);
	    	findPreference(Common.KEY_NO_SPACE).setEnabled(!prefHideUnit);
	    	findPreference(Common.KEY_HIDE_B).setEnabled(!prefHideUnit);
		}
    	
		if (key==null
				|| key.equals(Common.KEY_HIDE_BELOW)
				|| key.equals(Common.KEY_SUFFIX)) {
	    	int prefHideBelow = Common.getPrefInt(prefs, Common.KEY_HIDE_BELOW, Common.DEF_HIDE_BELOW);
	    	int prefSuffix = Common.getPrefInt(prefs, Common.KEY_SUFFIX, Common.DEF_SUFFIX);
	    	findPreference(Common.KEY_SHOW_SUFFIX).setEnabled(prefHideBelow > 0 && prefSuffix != 0);
	    	findPreference(Common.KEY_SMALL_TRIANGLE).setEnabled(prefSuffix != 0);
		}
    	
		if (key==null || key.equals(Common.KEY_UNIT_MODE)) {
			// Dynamically change the entry texts of "Unit" preference
			int prefInt = Common.getPrefInt(prefs, Common.KEY_UNIT_MODE, Common.DEF_UNIT_MODE);
			int resId;
			
			switch (prefInt) {
			case 0:
				resId = R.array.unit_entries_binary_bits;
				break;
			case 1:
				resId = R.array.unit_entries_binary_bytes;
				break;
			default: case 2:
				resId = R.array.unit_entries_decimal_bits;
				break;
			case 3:
				resId = R.array.unit_entries_decimal_bytes;
				break;
			}
			
			String[] unitEntries = getResources().getStringArray(resId);
			Preference prefUnit = findPreference(Common.KEY_FORCE_UNIT);
			((ListPreference) prefUnit).setEntries(unitEntries);
			
			if (key != null) { // key-specific refresh
				setSummary(prefUnit);
			}
		}
	}
	
}
