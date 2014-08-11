package tw.fatminmin.xposed.networkspeedindicator.widget;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import tw.fatminmin.xposed.networkspeedindicator.Common;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import de.robv.android.xposed.XSharedPreferences;

@SuppressLint("HandlerLeak")
public class TrafficView extends TextView {

    public PositionCallback mPositionCallback = null;
    public TextView clock = null;
    
	private static final String TAG = TrafficView.class.getSimpleName();
	private static final DecimalFormat formatWithDecimal    = new DecimalFormat(" ##0.0");
	private static final DecimalFormat formatWithoutDecimal = new DecimalFormat(" ##0");
	
	private static final String KILO = "K";
	private static final String MEGA = "M";
	private static final String BYTES = "B";
	private static final String BITS = "b";
	
	private boolean mAttached;
	// TrafficStats mTrafficStats;

	long uploadSpeed, downloadSpeed;
	long totalTxBytes, totalRxBytes;
	long lastUpdateTime;
	String networkType;
	boolean networkState;

	XSharedPreferences mPref;
	int prefPosition;
	int prefForceUnit;
	int prefUnitMode;
	float prefFontSize;
	int prefSuffix;
	int prefDisplay;
	int prefUpdateInterval;
	int prefColorMode;
	int prefColor;
	boolean prefShowUploadSpeed;
	boolean prefShowDownloadSpeed;
	boolean prefHideUnit;
	boolean prefNoSpace;
	boolean prefHideB;
	boolean prefHideInactive;
	boolean prefShowSuffix;
	boolean prefSmallTriangle;
	String prefNetworkType;
	Set<String> prefFontStyle = new HashSet<String>();

	public TrafficView(Context context) {
		this(context, null);
		mAttached = false;
	}

	public TrafficView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public TrafficView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		loadPreferences();
		updateConnectionInfo();
		updateViewVisibility();
	}
	
	public void refreshPosition() {
	    switch(prefPosition) {
        case 0:
            mPositionCallback.setLeft();
            break;
        case 1:
            mPositionCallback.setRight();
            break;
        case 2:
            mPositionCallback.setAbsoluteLeft();
            break;
        }
	}
	
	@SuppressLint("NewApi")
	public void refreshColor() {
		switch(prefColorMode) {
		case 0:
			if(clock != null) {
				setTextColor(clock.getCurrentTextColor());
			}
			else {
				// gingerbread;
				setTextColor(Color.parseColor("#33b5e5"));
			}
			break;
		case 1:
			setTextColor(prefColor);
			break;
		}
	}
	
	private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {

		@SuppressWarnings("unchecked")
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
				updateConnectionInfo();
				updateViewVisibility();
			} else if (action.equals(Common.ACTION_SETTINGS_CHANGED)) {
				Log.i(TAG, "SettingsChanged");
				if (intent.hasExtra(Common.KEY_FORCE_UNIT)) {
					prefForceUnit = intent.getIntExtra(Common.KEY_FORCE_UNIT, Common.DEF_FORCE_UNIT);
				}
				if (intent.hasExtra(Common.KEY_UNIT_MODE)) {
					prefUnitMode = intent.getIntExtra(Common.KEY_UNIT_MODE, Common.DEF_UNIT_MODE);
				}
				if(intent.hasExtra(Common.KEY_SHOW_UPLOAD_SPEED)) {
				    prefShowUploadSpeed = intent.getBooleanExtra(Common.KEY_SHOW_UPLOAD_SPEED, Common.DEF_SHOW_UPLOAD_SPEED);
				}
				if(intent.hasExtra(Common.KEY_SHOW_DOWNLOAD_SPEED)) {
				    prefShowDownloadSpeed = intent.getBooleanExtra(Common.KEY_SHOW_DOWNLOAD_SPEED, Common.DEF_SHOW_DOWNLOAD_SPEED);
				}
				if (intent.hasExtra(Common.KEY_HIDE_UNIT)) {
				    prefHideUnit = intent.getBooleanExtra(Common.KEY_HIDE_UNIT, Common.DEF_HIDE_UNIT);
				}
				if (intent.hasExtra(Common.KEY_NO_SPACE)) {
				    prefNoSpace = intent.getBooleanExtra(Common.KEY_NO_SPACE, Common.DEF_NO_SPACE);
				}
				if (intent.hasExtra(Common.KEY_HIDE_B)) {
				    prefHideB = intent.getBooleanExtra(Common.KEY_HIDE_B, Common.DEF_HIDE_B);
				}
				if (intent.hasExtra(Common.KEY_HIDE_INACTIVE)) {
					prefHideInactive = intent.getBooleanExtra(Common.KEY_HIDE_INACTIVE, Common.DEF_HIDE_INACTIVE);
				}
				if (intent.hasExtra(Common.KEY_SHOW_SUFFIX)) {
				    prefShowSuffix = intent.getBooleanExtra(Common.KEY_SHOW_SUFFIX, Common.DEF_SHOW_SUFFIX);
				}
				if(intent.hasExtra(Common.KEY_FONT_SIZE)) {
				    prefFontSize = intent.getFloatExtra(Common.KEY_FONT_SIZE, Common.DEF_FONT_SIZE);
				}
				if(intent.hasExtra(Common.KEY_POSITION)) {
				    prefPosition = intent.getIntExtra(Common.KEY_POSITION, Common.DEF_POSITION);
				    refreshPosition();
				}
				if(intent.hasExtra(Common.KEY_SUFFIX)) {
				    prefSuffix = intent.getIntExtra(Common.KEY_SUFFIX, Common.DEF_SUFFIX);
				}
				if (intent.hasExtra(Common.KEY_SMALL_TRIANGLE)) {
				    prefSmallTriangle = intent.getBooleanExtra(Common.KEY_SMALL_TRIANGLE, Common.DEF_SMALL_TRIANGLE);
				}
				if(intent.hasExtra(Common.KEY_NETWORK_TYPE)) {
				    prefNetworkType = intent.getStringExtra(Common.KEY_NETWORK_TYPE);
				}
				if(intent.hasExtra(Common.KEY_DISPLAY)) {
                    prefDisplay = intent.getIntExtra(Common.KEY_DISPLAY, Common.DEF_DISPLAY);
                }
				
				if(intent.hasExtra(Common.KEY_UPDATE_INTERVAL)) {
                    prefUpdateInterval = intent.getIntExtra(Common.KEY_UPDATE_INTERVAL, Common.DEF_UPDATE_INTERVAL);
                }
				if(intent.hasExtra(Common.KEY_COLOR_MODE)) {
					prefColorMode = intent.getIntExtra(Common.KEY_COLOR_MODE, Common.DEF_COLOR_MODE);
				}
				if(intent.hasExtra(Common.KEY_COLOR)) {
					prefColor = intent.getIntExtra(Common.KEY_COLOR, Common.DEF_COLOR);
				}
				if(intent.hasExtra(Common.KEY_FONT_STYLE)) {
					prefFontStyle = (Set<String>) intent.getSerializableExtra(Common.KEY_FONT_STYLE);
				}
				updateViewVisibility();
			}
		}
	};

	@SuppressLint("HandlerLeak")
    Handler mTrafficHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			long td = SystemClock.elapsedRealtime() - lastUpdateTime;
			if (td == 0) {
				return;
			}
			
			
			uploadSpeed = (TrafficStats.getTotalTxBytes() - totalTxBytes) * 1000 / td;
			downloadSpeed = (TrafficStats.getTotalRxBytes() - totalRxBytes) * 1000 / td;
			totalTxBytes = TrafficStats.getTotalTxBytes();
			totalRxBytes = TrafficStats.getTotalRxBytes();
			lastUpdateTime = SystemClock.elapsedRealtime();
			
			SpannableString spanString = new SpannableString(createText());
			if(prefFontStyle.contains("Bold")) {
				spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, spanString.length(), 0);
			}
			if(prefFontStyle.contains("Italic")) {
				spanString.setSpan(new StyleSpan(Typeface.ITALIC), 0, spanString.length(), 0);
			}
			
			setText(spanString);
			setTextSize(TypedValue.COMPLEX_UNIT_SP, prefFontSize);
			refreshColor();
			
			update();
			
			super.handleMessage(msg);
		}
	};

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();

		if (!mAttached) {
			mAttached = true;
			IntentFilter filter = new IntentFilter();
			filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
			filter.addAction(Common.ACTION_SETTINGS_CHANGED);
			getContext().registerReceiver(mIntentReceiver, filter, null, getHandler());
		}
		updateViewVisibility();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (mAttached) {
			getContext().unregisterReceiver(mIntentReceiver);
			mAttached = false;
		}
	}

	private void updateConnectionInfo() {
		Log.i(TAG, "updateConnectionInfo");
		ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(
				Context.CONNECTIVITY_SERVICE);

		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		
		if (networkInfo != null) {
			networkState = networkInfo.isAvailable();
			networkType = networkInfo.getTypeName().toUpperCase(Locale.ENGLISH);
			Log.i(TAG, "networkType = " + networkType);
		}
		else {
			networkState = false;
		}
	}

	public void updateTraffic() {
		lastUpdateTime = SystemClock.elapsedRealtime();
		totalRxBytes = TrafficStats.getTotalRxBytes();
		mTrafficHandler.sendEmptyMessage(0);
	}

	private String createText() {
		String uploadSuffix, downloadSuffix;
		String strUploadValue, strDownloadValue;
		
		switch(prefSuffix) {
		default:
		case 0:
		    uploadSuffix = downloadSuffix = " ";
		    break;
		case 1:
			if (prefSmallTriangle) {
				uploadSuffix = Common.SMALL_UP_TRIANGLE;
				downloadSuffix = Common.SMALL_DOWN_TRIANGLE;
			} else {
				uploadSuffix = Common.BIG_UP_TRIANGLE;
				downloadSuffix = Common.BIG_DOWN_TRIANGLE;
			}
		    break;
		case 2:
			if (prefSmallTriangle) {
				uploadSuffix = Common.SMALL_UP_HOLLOW_TRIANGLE;
				downloadSuffix = Common.SMALL_DOWN_HOLLOW_TRIANGLE;
			} else {
				uploadSuffix = Common.BIG_UP_HOLLOW_TRIANGLE;
				downloadSuffix = Common.BIG_DOWN_HOLLOW_TRIANGLE;
			}
		    break;
		}
		
		boolean showInExactPosition = (prefShowUploadSpeed && prefShowDownloadSpeed);
		
		if(prefShowUploadSpeed) {
		    strUploadValue = formatSpeed(uploadSpeed, uploadSuffix);
		}
		else {
			strUploadValue = "";
		}
		
		if(prefShowDownloadSpeed) {
		    strDownloadValue = formatSpeed(downloadSpeed, downloadSuffix);
		}
		else {
			strDownloadValue = "";
		}
		
		String delimeter = "";
		if(prefDisplay == 0) {
		    delimeter = "\n";
		}
		else {
		    delimeter = " ";
		    showInExactPosition = false; //irrelevant in one-line mode
		}
		
		String ret = "";
		if((showInExactPosition) || (strUploadValue.length() > 0 && strDownloadValue.length() > 0)) {
		    ret = strUploadValue + delimeter + strDownloadValue; 
		}
		else {
		    ret = strUploadValue + strDownloadValue;
		}
		return ret;
	}
	
	private String formatSpeed(long transferSpeed, String transferSuffix) {
		
		float unitFactor;
		
		switch (prefUnitMode) {
		case 0: // Binary bits
			transferSpeed *= 8;
			//no break
		case 1: // Binary bytes
			unitFactor = 1024f;
			break;
		case 2: // Decimal bits
			transferSpeed *= 8;
			//no break
		default:
		case 3: // Decimal bytes
			unitFactor = 1000f;
			break;
		}
		
		int tempPrefUnit = prefForceUnit;
		float megaTransferSpeed = ((float) transferSpeed) / (unitFactor * unitFactor);
		float kiloTransferSpeed = ((float) transferSpeed) / unitFactor;
		
		String transferUnit;
		float transferValue;
		DecimalFormat transferDecimalFormat;
		
		if (prefForceUnit == 0) { // Auto mode
			
			if (megaTransferSpeed >= 1) {
				tempPrefUnit = 3;
				
			} else if (kiloTransferSpeed >= 1) {
				tempPrefUnit = 2;
				
			} else {
				tempPrefUnit = 1;
			}
		}
		
		switch (tempPrefUnit) {
		case 3:
			transferValue = megaTransferSpeed;
			transferUnit = MEGA;
			transferDecimalFormat = formatWithDecimal;
			break;
		case 2:
			transferValue = kiloTransferSpeed;
			transferUnit = KILO;
			transferDecimalFormat = formatWithoutDecimal;
			break;
		default:
		case 1:
			transferValue = transferSpeed;
			transferUnit = "";
			transferDecimalFormat = formatWithoutDecimal;
			break;
		}
		
		String strTransferValue;
		
		if (prefHideInactive && transferValue <= 0) {
            strTransferValue = "";
        }
        else {
            strTransferValue = transferDecimalFormat.format(transferValue);
        }
		
		if(strTransferValue.length() > 0) {
		    if(!prefHideUnit) {
		        strTransferValue += formatUnit(transferUnit);
		    }
		    strTransferValue += transferSuffix;
		}
		else if (prefShowSuffix) {
			strTransferValue += transferSuffix;
		}
		
		return strTransferValue;
	}
	
	private String formatUnit(String unit) {
		if (!prefHideB) {
			if (prefUnitMode == 0 || prefUnitMode == 2)
				unit += BITS;
			else
				unit += BYTES;
		}
		if ((!prefNoSpace) && (unit.length() > 0)) {
			unit = " " + unit;
		}
		return unit;
	}

	public void update() {
		mTrafficHandler.removeCallbacks(mRunnable);
		mTrafficHandler.postDelayed(mRunnable, prefUpdateInterval);
	}

	Runnable mRunnable = new Runnable() {
		@Override
		public void run() {
			mTrafficHandler.sendEmptyMessage(0);
		}
	};
	
	private boolean isCorrectNetworkType() {
	    if(prefNetworkType == null || prefNetworkType.equals("both")) {
	        return true;
	    }
	    else {
	        return prefNetworkType.contains(networkType);
	    }
	}
	
	private void updateViewVisibility() {
		if (networkState && isCorrectNetworkType()) {
			if (mAttached) {
				updateTraffic();
			}
			setVisibility(View.VISIBLE);
		} else {
			setVisibility(View.GONE);
		}
		
	}

	private void loadPreferences() {
		mPref = new XSharedPreferences(Common.PKG_NAME);
		prefForceUnit = Common.getPrefInt(mPref, Common.KEY_FORCE_UNIT, Common.DEF_FORCE_UNIT);
		prefUnitMode = Common.getPrefInt(mPref, Common.KEY_UNIT_MODE, Common.DEF_UNIT_MODE);
		prefShowUploadSpeed = mPref.getBoolean(Common.KEY_SHOW_UPLOAD_SPEED, Common.DEF_SHOW_UPLOAD_SPEED);
		prefShowDownloadSpeed = mPref.getBoolean(Common.KEY_SHOW_DOWNLOAD_SPEED, Common.DEF_SHOW_DOWNLOAD_SPEED);
		prefHideUnit = mPref.getBoolean(Common.KEY_HIDE_UNIT, Common.DEF_HIDE_UNIT);
		prefNoSpace = mPref.getBoolean(Common.KEY_NO_SPACE, Common.DEF_NO_SPACE);
		prefHideB = mPref.getBoolean(Common.KEY_HIDE_B, Common.DEF_HIDE_B);
		prefHideInactive = mPref.getBoolean(Common.KEY_HIDE_INACTIVE, Common.DEF_HIDE_INACTIVE);
		prefShowSuffix = mPref.getBoolean(Common.KEY_SHOW_SUFFIX, Common.DEF_SHOW_SUFFIX);
		prefFontSize = Common.getPrefFloat(mPref, Common.KEY_FONT_SIZE, Common.DEF_FONT_SIZE);
		prefPosition = Common.getPrefInt(mPref, Common.KEY_POSITION, Common.DEF_POSITION);
		prefSuffix = Common.getPrefInt(mPref, Common.KEY_SUFFIX, Common.DEF_SUFFIX);
		prefSmallTriangle = mPref.getBoolean(Common.KEY_SMALL_TRIANGLE, Common.DEF_SMALL_TRIANGLE);
		prefNetworkType = mPref.getString(Common.KEY_NETWORK_TYPE, Common.DEF_NETWORK_TYPE);
		prefDisplay = Common.getPrefInt(mPref, Common.KEY_DISPLAY, Common.DEF_DISPLAY);
		prefUpdateInterval = Common.getPrefInt(mPref, Common.KEY_UPDATE_INTERVAL, Common.DEF_UPDATE_INTERVAL);
		prefColorMode = Common.getPrefInt(mPref, Common.KEY_COLOR_MODE	, Common.DEF_COLOR_MODE);
		prefColor = mPref.getInt(Common.KEY_COLOR, Common.DEF_COLOR);
		prefFontStyle = mPref.getStringSet(Common.KEY_FONT_STYLE, Common.DEF_FONT_STYLE);
	}
}
