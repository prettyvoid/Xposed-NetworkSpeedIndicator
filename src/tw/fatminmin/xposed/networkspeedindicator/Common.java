package tw.fatminmin.xposed.networkspeedindicator;

import java.util.HashSet;

import android.content.SharedPreferences;

public class Common {

	public static final String PKG_NAME = "tw.fatminmin.xposed.networkspeedindicator";
	public static final String PREFERENCE_FILE = PKG_NAME + "_preferences";
	public static final String ACTION_SETTINGS_CHANGED = PKG_NAME + ".changed";
	
	public static final String KEY_SHOW_UPLOAD_SPEED = "show_upload_speed";
	public static final String KEY_SHOW_DOWNLOAD_SPEED = "show_download_speed";
	public static final String KEY_HIDE_UNIT = "hide_unit";
	public static final String KEY_NO_SPACE = "no_space";
	public static final String KEY_HIDE_B = "hide_b";
	public static final String KEY_HIDE_BELOW = "hide_below";
	public static final String KEY_SHOW_SUFFIX = "show_suffix";
	public static final String KEY_UNIT_MODE = "unit_mode";
	public static final String KEY_FORCE_UNIT = "force_unit";
	public static final String KEY_NETWORK_TYPE = "network_type";
	public static final String KEY_FONT_SIZE = "font_size";
	public static final String KEY_POSITION = "position";
	public static final String KEY_SUFFIX = "suffix";
	public static final String KEY_SMALL_TRIANGLE = "small_triangle";
	public static final String KEY_DISPLAY = "display";
	public static final String KEY_UPDATE_INTERVAL = "update_interval";
	public static final String KEY_FONT_COLOR = "font_color";
	public static final String KEY_COLOR = "color";
	public static final String KEY_FONT_STYLE = "font_style";
	
	public static final HashSet<String> DEF_NETWORK_TYPE = new HashSet<String>();
	public static final boolean DEF_SHOW_UPLOAD_SPEED = true;
	public static final boolean DEF_SHOW_DOWNLOAD_SPEED = true;
	public static final boolean DEF_HIDE_UNIT = false;
	public static final boolean DEF_NO_SPACE = false;
	public static final boolean DEF_HIDE_B = false;
	public static final int DEF_HIDE_BELOW = 0;
	public static final boolean DEF_SHOW_SUFFIX = false;
	public static final int DEF_UNIT_MODE = 3; //Decimal bytes
	public static final int DEF_FORCE_UNIT = 0;
	public static final float DEF_FONT_SIZE = 10;
	public static final int DEF_POSITION = 0;
	public static final int DEF_SUFFIX = 1;
	public static final boolean DEF_SMALL_TRIANGLE = false;
	public static final int DEF_DISPLAY = 0;
	public static final int DEF_UPDATE_INTERVAL = 1000;
	public static final boolean DEF_FONT_COLOR = false;
	public static final int DEF_COLOR = android.graphics.Color.LTGRAY;
	public static final HashSet<String> DEF_FONT_STYLE = new HashSet<String>();
	
	public static final String BIG_UP_TRIANGLE = " \u25B2 ";
	public static final String BIG_DOWN_TRIANGLE = " \u25BC ";
	public static final String SMALL_UP_TRIANGLE = "\u25B4";
	public static final String SMALL_DOWN_TRIANGLE = " \u25BE ";
	
	public static final String BIG_UP_HOLLOW_TRIANGLE = " \u25B3 ";
	public static final String BIG_DOWN_HOLLOW_TRIANGLE = " \u25BD ";
	public static final String SMALL_UP_HOLLOW_TRIANGLE = "\u25B5";
	public static final String SMALL_DOWN_HOLLOW_TRIANGLE = " \u25BF ";
	
	static {
		DEF_NETWORK_TYPE.add("0");
		DEF_NETWORK_TYPE.add("1");
	}

	public static int getPrefInt(SharedPreferences pref, String key, int def_value) {
		try {
			String value = pref.getString(key, String.valueOf(def_value));
			return Integer.parseInt(value);
		} catch (Exception e) {
			// Do nothing
		}
		return def_value;
	}
	
	public static float getPrefFloat(SharedPreferences pref, String key, float def_value) {
		try {
			String value = pref.getString(key, String.valueOf(def_value));
			return Float.parseFloat(value);
		} catch (Exception e) {
			// Do nothing
		}
		return def_value;
	}

}
