package tw.fatminmin.xposed.networkspeedindicator;

import java.util.HashSet;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
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
		
		if(key.equals(Common.KEY_SHOW_UPLOAD_SPEED)) {
		    
		    intent.setAction(Common.ACTION_SETTINGS_CHANGED);
		    intent.putExtra(Common.KEY_SHOW_UPLOAD_SPEED, 
		            prefs.getBoolean(Common.KEY_SHOW_UPLOAD_SPEED, Common.DEF_SHOW_UPLOAD_SPEED));
		    
		}
		else if(key.equals(Common.KEY_SHOW_DOWNLOAD_SPEED)) {
		    
		    intent.setAction(Common.ACTION_SETTINGS_CHANGED);
		    intent.putExtra(Common.KEY_SHOW_DOWNLOAD_SPEED, 
                    prefs.getBoolean(Common.KEY_SHOW_DOWNLOAD_SPEED, Common.DEF_SHOW_DOWNLOAD_SPEED));
		    
		}
		else if (key.equals(Common.KEY_FORCE_UNIT)) {
		    
			intent.setAction(Common.ACTION_SETTINGS_CHANGED);
			intent.putExtra(Common.KEY_FORCE_UNIT,
					Common.getPrefInt(prefs, Common.KEY_FORCE_UNIT, Common.DEF_FORCE_UNIT));
		}
		else if (key.equals(Common.KEY_UNIT_MODE)) {
		    
			intent.setAction(Common.ACTION_SETTINGS_CHANGED);
			intent.putExtra(Common.KEY_UNIT_MODE,
					Common.getPrefInt(prefs, Common.KEY_UNIT_MODE, Common.DEF_UNIT_MODE));
		}
		else if (key.equals(Common.KEY_HIDE_UNIT)) {
		    
			intent.setAction(Common.ACTION_SETTINGS_CHANGED);
			intent.putExtra(Common.KEY_HIDE_UNIT, prefs.getBoolean(Common.KEY_HIDE_UNIT, Common.DEF_HIDE_UNIT));
			
		}
		else if (key.equals(Common.KEY_NO_SPACE)) {
		    
			intent.setAction(Common.ACTION_SETTINGS_CHANGED);
			intent.putExtra(Common.KEY_NO_SPACE, prefs.getBoolean(Common.KEY_NO_SPACE, Common.DEF_NO_SPACE));
			
		}
		else if (key.equals(Common.KEY_HIDE_B)) {
		    
			intent.setAction(Common.ACTION_SETTINGS_CHANGED);
			intent.putExtra(Common.KEY_HIDE_B, prefs.getBoolean(Common.KEY_HIDE_B, Common.DEF_HIDE_B));
			
		}
		else if (key.equals(Common.KEY_HIDE_INACTIVE)) {
			
		    intent.setAction(Common.ACTION_SETTINGS_CHANGED);
			intent.putExtra(Common.KEY_HIDE_INACTIVE,
					prefs.getBoolean(Common.KEY_HIDE_INACTIVE, Common.DEF_HIDE_INACTIVE));
		}
		else if (key.equals(Common.KEY_SHOW_SUFFIX)) {
		    
			intent.setAction(Common.ACTION_SETTINGS_CHANGED);
			intent.putExtra(Common.KEY_SHOW_SUFFIX, prefs.getBoolean(Common.KEY_SHOW_SUFFIX, Common.DEF_SHOW_SUFFIX));
			
		}
		else if(key.equals(Common.KEY_FONT_SIZE)) {
		    intent.setAction(Common.ACTION_SETTINGS_CHANGED);
		    intent.putExtra(Common.KEY_FONT_SIZE,
		            Common.getPrefFloat(prefs, Common.KEY_FONT_SIZE, Common.DEF_FONT_SIZE));
		}
		else if(key.equals(Common.KEY_POSITION)) {
		    intent.setAction(Common.ACTION_SETTINGS_CHANGED);
		    intent.putExtra(Common.KEY_POSITION, 
		            Common.getPrefInt(prefs, Common.KEY_POSITION, Common.DEF_POSITION));
		}
		else if(key.equals(Common.KEY_SUFFIX)) {
            intent.setAction(Common.ACTION_SETTINGS_CHANGED);
            intent.putExtra(Common.KEY_SUFFIX, 
                    Common.getPrefInt(prefs, Common.KEY_SUFFIX, Common.DEF_SUFFIX));
        }
		else if (key.equals(Common.KEY_SMALL_TRIANGLE)) {
		    
			intent.setAction(Common.ACTION_SETTINGS_CHANGED);
			intent.putExtra(Common.KEY_SMALL_TRIANGLE, prefs.getBoolean(Common.KEY_SMALL_TRIANGLE, Common.DEF_SMALL_TRIANGLE));
			
		}
		else if(key.equals(Common.KEY_NETWORK_TYPE)) {
		    intent.setAction(Common.ACTION_SETTINGS_CHANGED);
		    intent.putExtra(Common.KEY_NETWORK_TYPE, prefs.getString(Common.KEY_NETWORK_TYPE, Common.DEF_NETWORK_TYPE));
		}
		else if(key.equals(Common.KEY_DISPLAY)) {
            intent.setAction(Common.ACTION_SETTINGS_CHANGED);
            intent.putExtra(Common.KEY_DISPLAY, 
                    Common.getPrefInt(prefs, Common.KEY_DISPLAY, Common.DEF_DISPLAY));
        }
		else if(key.equals(Common.KEY_UPDATE_INTERVAL)) {
            intent.setAction(Common.ACTION_SETTINGS_CHANGED);
            intent.putExtra(Common.KEY_UPDATE_INTERVAL, 
                    Common.getPrefInt(prefs, Common.KEY_UPDATE_INTERVAL, Common.DEF_UPDATE_INTERVAL));
        }
		else if(key.equals(Common.KEY_COLOR_MODE)) {
			intent.setAction(Common.ACTION_SETTINGS_CHANGED);
			intent.putExtra(Common.KEY_COLOR_MODE, 
                    Common.getPrefInt(prefs, Common.KEY_COLOR_MODE, Common.DEF_COLOR_MODE));
		}
		else if(key.equals(Common.KEY_COLOR)) {
			intent.setAction(Common.ACTION_SETTINGS_CHANGED);
			intent.putExtra(Common.KEY_COLOR, prefs.getInt(Common.KEY_COLOR, Common.DEF_COLOR));
		}
		else if(key.equals(Common.KEY_FONT_STYLE)) {
			intent.setAction(Common.ACTION_SETTINGS_CHANGED);
			
			MultiSelectListPreferenceCompat mulPref = (MultiSelectListPreferenceCompat) findPreference(key);
			HashSet<String> value = (HashSet<String>) mulPref.getValues(); 
			intent.putExtra(Common.KEY_FONT_STYLE, value);
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
		
		if (key==null || key.equals(Common.KEY_COLOR_MODE)) {
			int prefColorMode = Common.getPrefInt(prefs, Common.KEY_COLOR_MODE, Common.DEF_COLOR_MODE);
			findPreference(Common.KEY_COLOR).setEnabled(prefColorMode == 1);
		}
		
		if (key==null || key.equals(Common.KEY_HIDE_UNIT)) {
	    	//enable only when hide unit is disabled
	    	boolean prefHideUnit = prefs.getBoolean(Common.KEY_HIDE_UNIT, Common.DEF_HIDE_UNIT);
	    	findPreference(Common.KEY_NO_SPACE).setEnabled(!prefHideUnit);
	    	findPreference(Common.KEY_HIDE_B).setEnabled(!prefHideUnit);
		}
    	
		if (key==null
				|| key.equals(Common.KEY_HIDE_INACTIVE)
				|| key.equals(Common.KEY_SUFFIX)) {
	    	boolean prefHideInactive = prefs.getBoolean(Common.KEY_HIDE_INACTIVE, Common.DEF_HIDE_INACTIVE);
	    	int prefSuffix = Common.getPrefInt(prefs, Common.KEY_SUFFIX, Common.DEF_SUFFIX);
	    	findPreference(Common.KEY_SHOW_SUFFIX).setEnabled(prefHideInactive && prefSuffix != 0);
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
