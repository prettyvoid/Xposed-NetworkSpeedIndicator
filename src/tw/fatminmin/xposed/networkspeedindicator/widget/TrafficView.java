package tw.fatminmin.xposed.networkspeedindicator.widget;

import java.text.DecimalFormat;
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
	
	private boolean mAttached;
	// TrafficStats mTrafficStats;

	long uploadSpeed, downloadSpeed;
	long totalTxBytes, totalRxBytes;
	long lastUpdateTime;
	boolean justLaunched = true;
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
	boolean prefFontColor;
	int prefColor;
	int prefHideBelow;
	boolean prefShowSuffix;
	boolean prefSmallTriangle;
	Set<String> prefUnitFormat = Common.DEF_UNIT_FORMAT;
	Set<String> prefNetworkType = Common.DEF_NETWORK_TYPE;
	Set<String> prefNetworkSpeed = Common.DEF_NETWORK_SPEED;
	Set<String> prefFontStyle = Common.DEF_FONT_STYLE;

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
		if (prefFontColor) {
			setTextColor(prefColor);
		}
		else {
			if(clock != null) {
				setTextColor(clock.getCurrentTextColor());
			}
			else {
				// gingerbread;
				setTextColor(Color.parseColor("#33b5e5"));
			}
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
			}
			else if (action.equals(Common.ACTION_SETTINGS_CHANGED)) {
				Log.i(TAG, "SettingsChanged");
				
				if (intent.hasExtra(Common.KEY_FORCE_UNIT)) {
					prefForceUnit = intent.getIntExtra(Common.KEY_FORCE_UNIT, Common.DEF_FORCE_UNIT);
				}
				if (intent.hasExtra(Common.KEY_UNIT_MODE)) {
					prefUnitMode = intent.getIntExtra(Common.KEY_UNIT_MODE, Common.DEF_UNIT_MODE);
				}
				if (intent.hasExtra(Common.KEY_UNIT_FORMAT)) {
				    prefUnitFormat = (Set<String>) intent.getSerializableExtra(Common.KEY_UNIT_FORMAT);
				}
				if (intent.hasExtra(Common.KEY_HIDE_BELOW)) {
					prefHideBelow = intent.getIntExtra(Common.KEY_HIDE_BELOW, Common.DEF_HIDE_BELOW);
				}
				if (intent.hasExtra(Common.KEY_SHOW_SUFFIX)) {
				    prefShowSuffix = intent.getBooleanExtra(Common.KEY_SHOW_SUFFIX, Common.DEF_SHOW_SUFFIX);
				}
				if (intent.hasExtra(Common.KEY_FONT_SIZE)) {
				    prefFontSize = intent.getFloatExtra(Common.KEY_FONT_SIZE, Common.DEF_FONT_SIZE);
				}
				if (intent.hasExtra(Common.KEY_POSITION)) {
				    prefPosition = intent.getIntExtra(Common.KEY_POSITION, Common.DEF_POSITION);
				    refreshPosition();
				}
				if (intent.hasExtra(Common.KEY_SUFFIX)) {
				    prefSuffix = intent.getIntExtra(Common.KEY_SUFFIX, Common.DEF_SUFFIX);
				}
				if (intent.hasExtra(Common.KEY_SMALL_TRIANGLE)) {
				    prefSmallTriangle = intent.getBooleanExtra(Common.KEY_SMALL_TRIANGLE, Common.DEF_SMALL_TRIANGLE);
				}
				if (intent.hasExtra(Common.KEY_NETWORK_TYPE)) {
				    prefNetworkType = (Set<String>) intent.getSerializableExtra(Common.KEY_NETWORK_TYPE);
				}
				if (intent.hasExtra(Common.KEY_NETWORK_SPEED)) {
				    prefNetworkSpeed = (Set<String>) intent.getSerializableExtra(Common.KEY_NETWORK_SPEED);
				}
				if (intent.hasExtra(Common.KEY_DISPLAY)) {
                    prefDisplay = intent.getIntExtra(Common.KEY_DISPLAY, Common.DEF_DISPLAY);
                }
				if (intent.hasExtra(Common.KEY_UPDATE_INTERVAL)) {
                    prefUpdateInterval = intent.getIntExtra(Common.KEY_UPDATE_INTERVAL, Common.DEF_UPDATE_INTERVAL);
                }
				if (intent.hasExtra(Common.KEY_FONT_COLOR)) {
					prefFontColor = intent.getBooleanExtra(Common.KEY_FONT_COLOR, Common.DEF_FONT_COLOR);
				}
				if (intent.hasExtra(Common.KEY_COLOR)) {
					prefColor = intent.getIntExtra(Common.KEY_COLOR, Common.DEF_COLOR);
				}
				if (intent.hasExtra(Common.KEY_FONT_STYLE)) {
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
			
			// changing values must be fetched together and only once
			long lastUpdateTimeNew = SystemClock.elapsedRealtime();
			long totalTxBytesNew = TrafficStats.getTotalTxBytes();
			long totalRxBytesNew = TrafficStats.getTotalRxBytes();
			
			long elapsedTime = lastUpdateTimeNew - lastUpdateTime;
			
			if (elapsedTime == 0) {
				uploadSpeed = 0;
				downloadSpeed = 0;
			} else {
				uploadSpeed = ((totalTxBytesNew - totalTxBytes) * 1000) / elapsedTime;
				downloadSpeed = ((totalRxBytesNew - totalRxBytes) * 1000) / elapsedTime;
			}
			
			totalTxBytes = totalTxBytesNew;
			totalRxBytes = totalRxBytesNew;
			lastUpdateTime = lastUpdateTimeNew;
			
			SpannableString spanString = new SpannableString(createText());
			
			if(prefFontStyle.contains("B")) {
				spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, spanString.length(), 0);
			}
			if(prefFontStyle.contains("I")) {
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
			networkType = String.valueOf(networkInfo.getType());
			Log.i(TAG, "networkType = " + networkType);
		}
		else {
			networkState = false;
		}
	}

	public void updateTraffic() {
		
		if (justLaunched) {
			//get the values for the first time
			lastUpdateTime = SystemClock.elapsedRealtime();
			totalTxBytes = TrafficStats.getTotalTxBytes();
			totalRxBytes = TrafficStats.getTotalRxBytes();
			
			//don't get the values again
			justLaunched = false;
		}
		
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
		
		boolean showUploadSpeed = prefNetworkSpeed.contains("U");
		boolean showDownloadSpeed = prefNetworkSpeed.contains("D");
		boolean showInExactPosition = (showUploadSpeed && showDownloadSpeed);
		
		if(showUploadSpeed) {
		    strUploadValue = formatSpeed(uploadSpeed, uploadSuffix);
		}
		else {
			strUploadValue = "";
		}
		
		if(showDownloadSpeed) {
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
	
	private String formatSpeed(final long transferSpeedBytes, final String transferSuffix) {
		
		float unitFactor;
		long transferSpeed = transferSpeedBytes;
		
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
			transferDecimalFormat = formatWithDecimal;
			break;
		case 2:
			transferValue = kiloTransferSpeed;
			transferDecimalFormat = formatWithoutDecimal;
			break;
		default:
		case 1:
			transferValue = transferSpeed;
			transferDecimalFormat = formatWithoutDecimal;
			break;
		}
		
		String strTransferValue;
		
		if (transferSpeedBytes < prefHideBelow) {
            strTransferValue = "";
        }
        else {
            strTransferValue = transferDecimalFormat.format(transferValue);
        }
		
		if(strTransferValue.length() > 0) {
		    strTransferValue += Common.formatUnit(prefUnitMode, tempPrefUnit, prefUnitFormat);
		    strTransferValue += transferSuffix;
		}
		else if (prefShowSuffix) {
			strTransferValue += transferSuffix;
		}
		
		return strTransferValue;
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
	
	private void updateViewVisibility() {
		if (networkState && prefNetworkType.contains(networkType)) {
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
		prefUnitFormat = mPref.getStringSet(Common.KEY_UNIT_FORMAT, Common.DEF_UNIT_FORMAT);
		prefHideBelow = Common.getPrefInt(mPref, Common.KEY_HIDE_BELOW, Common.DEF_HIDE_BELOW);
		prefShowSuffix = mPref.getBoolean(Common.KEY_SHOW_SUFFIX, Common.DEF_SHOW_SUFFIX);
		prefFontSize = Common.getPrefFloat(mPref, Common.KEY_FONT_SIZE, Common.DEF_FONT_SIZE);
		prefPosition = Common.getPrefInt(mPref, Common.KEY_POSITION, Common.DEF_POSITION);
		prefSuffix = Common.getPrefInt(mPref, Common.KEY_SUFFIX, Common.DEF_SUFFIX);
		prefSmallTriangle = mPref.getBoolean(Common.KEY_SMALL_TRIANGLE, Common.DEF_SMALL_TRIANGLE);
		prefNetworkType = mPref.getStringSet(Common.KEY_NETWORK_TYPE, Common.DEF_NETWORK_TYPE);
		prefNetworkSpeed = mPref.getStringSet(Common.KEY_NETWORK_SPEED, Common.DEF_NETWORK_SPEED);
		prefDisplay = Common.getPrefInt(mPref, Common.KEY_DISPLAY, Common.DEF_DISPLAY);
		prefUpdateInterval = Common.getPrefInt(mPref, Common.KEY_UPDATE_INTERVAL, Common.DEF_UPDATE_INTERVAL);
		prefFontColor = mPref.getBoolean(Common.KEY_FONT_COLOR, Common.DEF_FONT_COLOR);
		prefColor = mPref.getInt(Common.KEY_COLOR, Common.DEF_COLOR);
		prefFontStyle = mPref.getStringSet(Common.KEY_FONT_STYLE, Common.DEF_FONT_STYLE);
	}
}
