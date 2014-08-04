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
	public static final String KEY_HIDE_INACTIVE = "hide_inactive";
	public static final String KEY_FORCE_UNIT = "force_unit";
	public static final String KEY_NETWORK_TYPE = "network_type";
	public static final String KEY_FONT_SIZE = "font_size";
	public static final String KEY_POSITION = "position";
	public static final String KEY_SUFFIX = "suffix";
	public static final String KEY_DISPLAY = "display";
	public static final String KEY_UPDATE_INTERVAL = "update_interval";
	public static final String KEY_COLOR_MODE = "color_mode";
	public static final String KEY_COLOR = "color";
	public static final String KEY_FONT_STYLE = "font_style";
	
	public static final String DEF_NETWORK_TYPE = "both";
	public static final boolean DEF_SHOW_UPLOAD_SPEED = true;
	public static final boolean DEF_SHOW_DOWNLOAD_SPEED = true;
	public static final boolean DEF_HIDE_UNIT = false;
	public static final boolean DEF_HIDE_INACTIVE = false;
	public static final int DEF_FORCE_UNIT = 0;
	public static final int DEF_FONT_SIZE = 10;
	public static final int DEF_POSITION = 0;
	public static final int DEF_SUFFIX = 1;
	public static final int DEF_DISPLAY = 0;
	public static final int DEF_UPDATE_INTERVALE = 1000;
	public static final int DEF_COLOR_MODE = 0;
	public static final int DEF_COLOR = android.graphics.Color.LTGRAY;
	public static final HashSet<String> DEF_FONT_STYLE = new HashSet<String>();

	public static int getPrefInt(SharedPreferences pref, String key, int def_value) {
		try {
			String value = pref.getString(key, String.valueOf(def_value));
			return Integer.parseInt(value);
		} catch (Exception e) {
			// Do nothing
		}
		return def_value;
	}

}
