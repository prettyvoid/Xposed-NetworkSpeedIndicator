package tw.fatminmin.xposed.networkspeedindicator.widget;

import java.text.DecimalFormat;
import java.util.Locale;

import tw.fatminmin.xposed.networkspeedindicator.Common;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import de.robv.android.xposed.XSharedPreferences;

@SuppressLint("HandlerLeak")
public class TrafficView extends TextView {

    public PositionCallback mPositionCallback = null;
    
	private static final String TAG = TrafficView.class.getSimpleName();
	private DecimalFormat uploadDecimalFormat, downloadDecimalFormat;
	
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
	int prefFontSize;
	int prefSuffix;
	int prefDisplay;
	int prefUpdateInterval;
	boolean prefShowUploadSpeed;
	boolean prefShowDownloadSpeed;
	boolean prefHideUnit;
	boolean prefHideInactive;
	String prefNetworkType;
	
	String uploadSuffix = "";
	String downloadSuffix = "";

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

	
	private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {

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
				if(intent.hasExtra(Common.KEY_SHOW_UPLOAD_SPEED)) {
				    prefShowUploadSpeed = intent.getBooleanExtra(Common.KEY_SHOW_UPLOAD_SPEED, Common.DEF_SHOW_UPLOAD_SPEED);
				}
				if(intent.hasExtra(Common.KEY_SHOW_DOWNLOAD_SPEED)) {
				    prefShowDownloadSpeed = intent.getBooleanExtra(Common.KEY_SHOW_DOWNLOAD_SPEED, Common.DEF_SHOW_DOWNLOAD_SPEED);
				}
				if (intent.hasExtra(Common.KEY_HIDE_UNIT)) {
				    prefHideUnit = intent.getBooleanExtra(Common.KEY_HIDE_UNIT, Common.DEF_HIDE_UNIT);
				}
				if (intent.hasExtra(Common.KEY_HIDE_INACTIVE)) {
					prefHideInactive = intent.getBooleanExtra(Common.KEY_HIDE_INACTIVE, Common.DEF_HIDE_INACTIVE);
				}
				if (intent.hasExtra(Common.KEY_HIDE_INACTIVE)) {
					prefHideInactive = intent.getBooleanExtra(Common.KEY_HIDE_INACTIVE, Common.DEF_HIDE_INACTIVE);
				}
				if(intent.hasExtra(Common.KEY_FONT_SIZE)) {
				    prefFontSize = intent.getIntExtra(Common.KEY_FONT_SIZE, Common.DEF_FONT_SIZE);
				}
				if(intent.hasExtra(Common.KEY_POSITION)) {
				    prefPosition = intent.getIntExtra(Common.KEY_POSITION, Common.DEF_POSITION);
				    refreshPosition();
				}
				if(intent.hasExtra(Common.KEY_SUFFIX)) {
				    prefSuffix = intent.getIntExtra(Common.KEY_SUFFIX, Common.DEF_SUFFIX);
				}
				if(intent.hasExtra(Common.KEY_NETWORK_TYPE)) {
				    prefNetworkType = intent.getStringExtra(Common.KEY_NETWORK_TYPE);
				}
				if(intent.hasExtra(Common.KEY_DISPLAY)) {
                    prefDisplay = intent.getIntExtra(Common.KEY_DISPLAY, Common.DEF_DISPLAY);
                }
				
				if(intent.hasExtra(Common.KEY_UPDATE_INTERVAL)) {
                    prefUpdateInterval = intent.getIntExtra(Common.KEY_UPDATE_INTERVAL, Common.DEF_UPDATE_INTERVALE);
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
			
			setText(createText());
			setTextSize(TypedValue.COMPLEX_UNIT_SP, prefFontSize);
			
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
			networkState = networkInfo.isConnected();
			networkType = networkInfo.getTypeName().toUpperCase(Locale.ENGLISH);
			Log.i(TAG, "networkType = " + networkType);
		}
	}

	public void updateTraffic() {
		lastUpdateTime = SystemClock.elapsedRealtime();
		totalRxBytes = TrafficStats.getTotalRxBytes();
		mTrafficHandler.sendEmptyMessage(0);
	}

	private String createText() {
		String uploadUnit, downloadUnit;
		float uploadValue, downloadValue;
		
		
		
		switch (prefForceUnit) {
		default:
		case 0:
		    
		    if (((float) uploadSpeed) / 1048576 >= 1) { // 1024 * 1024 113
                uploadValue = ((float) uploadSpeed) / 1048576f;
                uploadUnit = "MB";
                uploadDecimalFormat = new DecimalFormat(" ##0.0");
            } else if (((float) uploadSpeed) / 1024f >= 1) {
                uploadValue = ((float) uploadSpeed) / 1024f;
                uploadUnit = "KB";
                uploadDecimalFormat = new DecimalFormat(" ##0");
            } else {
                uploadValue = uploadSpeed;
                uploadUnit = "B";
                uploadDecimalFormat = new DecimalFormat(" ##0");
            }
		    
			if (((float) downloadSpeed) / 1048576 >= 1) { // 1024 * 1024 113
				downloadValue = ((float) downloadSpeed) / 1048576f;
				downloadUnit = "MB";
				downloadDecimalFormat = new DecimalFormat(" ##0.0");
			} else if (((float) downloadSpeed) / 1024f >= 1) {
				downloadValue = ((float) downloadSpeed) / 1024f;
				downloadUnit = "KB";
				downloadDecimalFormat = new DecimalFormat(" ##0");
			} else {
				downloadValue = downloadSpeed;
				downloadUnit = "B";
				downloadDecimalFormat = new DecimalFormat(" ##0");
			}
			break;
		case 1:
			downloadValue = downloadSpeed;
			uploadValue = uploadSpeed;
			uploadUnit = downloadUnit = "B";
			uploadDecimalFormat = downloadDecimalFormat = new DecimalFormat(" ##0");
			break;
		case 2:
			downloadValue = ((float) downloadSpeed) / 1024f;
			uploadValue = ((float) uploadSpeed) / 1024f;
			uploadUnit = downloadUnit = "KB";
			uploadDecimalFormat = downloadDecimalFormat = new DecimalFormat(" ##0");
			break;
		case 3:
			downloadValue = ((float) downloadSpeed) / 1048576f;
			uploadValue = ((float) uploadSpeed) / 1048576f;
			uploadUnit = downloadUnit = "MB";
			uploadDecimalFormat = downloadDecimalFormat = new DecimalFormat(" ##0.0");
			break;
		}
		
		
		String strUploadValue, strDownloadValue;
		
		if (prefHideInactive && uploadValue <= 0) {
            strUploadValue = "";
        }
        else {
            strUploadValue = uploadDecimalFormat.format(uploadValue);
        }
		if (prefHideInactive && downloadSpeed <= 0) {
		    strDownloadValue = "";
        }
		else {
		    strDownloadValue = downloadDecimalFormat.format(downloadValue);
		}
		
		switch(prefSuffix) {
		case 0:
		    uploadSuffix = downloadSuffix = " ";
		    break;
		case 1:
		    uploadSuffix = " \u25B2 ";
		    downloadSuffix = " \u25BC ";
		    break;
		case 2:
		    uploadSuffix = " \u25B3 ";
		    downloadSuffix = " \u25BD ";
		    break;
		}
		
		if(strUploadValue.length() > 0) {
		    if(!prefHideUnit) {
		        strUploadValue += " " + uploadUnit;
		    }
		    strUploadValue += uploadSuffix;
		}
		if(strDownloadValue.length() > 0) {
		    if(!prefHideUnit) {
		        strDownloadValue += " " + downloadUnit;
		    }
		    strDownloadValue += downloadSuffix;
		}
		
		if(!prefShowUploadSpeed) {
		    strUploadValue = "";
		}
		if(!prefShowDownloadSpeed) {
		    strDownloadValue = "";
		}
		
		String delimeter = "";
		if(prefDisplay == 0) {
		    delimeter = "\n";
		}
		else {
		    delimeter = " ";
		}
		
		String ret = "";
		if(strUploadValue.length() > 0 && strDownloadValue.length() > 0) {
		    ret = strUploadValue + delimeter + strDownloadValue; 
		}
		else {
		    ret = strUploadValue + strDownloadValue;
		}
		return ret;
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
		prefShowUploadSpeed = mPref.getBoolean(Common.KEY_SHOW_UPLOAD_SPEED, Common.DEF_SHOW_UPLOAD_SPEED);
		prefShowDownloadSpeed = mPref.getBoolean(Common.KEY_SHOW_DOWNLOAD_SPEED, Common.DEF_SHOW_DOWNLOAD_SPEED);
		prefHideUnit = mPref.getBoolean(Common.KEY_HIDE_UNIT, Common.DEF_HIDE_UNIT);
		prefHideInactive = mPref.getBoolean(Common.KEY_HIDE_INACTIVE, Common.DEF_HIDE_INACTIVE);
		prefFontSize = Common.getPrefInt(mPref, Common.KEY_FONT_SIZE, Common.DEF_FONT_SIZE);
		prefPosition = Common.getPrefInt(mPref, Common.KEY_POSITION, Common.DEF_POSITION);
		prefSuffix = Common.getPrefInt(mPref, Common.KEY_SUFFIX, Common.DEF_SUFFIX);
		prefNetworkType = mPref.getString(Common.KEY_NETWORK_TYPE, Common.DEF_NETWORK_TYPE);
		prefDisplay = Common.getPrefInt(mPref, Common.KEY_DISPLAY, Common.DEF_DISPLAY);
		prefUpdateInterval = Common.getPrefInt(mPref, Common.KEY_UPDATE_INTERVAL, Common.DEF_UPDATE_INTERVALE);
	}
}
