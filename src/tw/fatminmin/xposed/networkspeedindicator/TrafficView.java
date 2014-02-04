package tw.fatminmin.xposed.networkspeedindicator;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

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

	private static final String TAG = TrafficView.class.getSimpleName();
	private final DecimalFormat decimalFormat = new DecimalFormat("##0.0");

	private boolean mAttached;
	// TrafficStats mTrafficStats;

	long uploadSpeed, downloadSpeed;
	long totalTxBytes, totalRxBytes;
	long lastUpdateTime;
	String networkType;
	boolean networkState;

	XSharedPreferences mPref;
	int prefForceUnit;
	int prefFontSize;
	boolean prefHideUnit;
	boolean prefHideInactive;
	Set<String> prefHideNetworkState = new HashSet<String>();

	public TrafficView(Context context) {
		this(context, null);
	}

	public TrafficView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public TrafficView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		updateConnectionInfo();
		updateViewVisibility();
		loadPreferences();
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
				if (intent.hasExtra(Common.KEY_HIDE_UNIT)) {
				    prefHideUnit = intent.getBooleanExtra(Common.KEY_HIDE_UNIT, Common.DEF_HIDE_UNIT);
				}
				if (intent.hasExtra(Common.KEY_HIDE_INACTIVE)) {
					prefHideInactive = intent.getBooleanExtra(Common.KEY_HIDE_INACTIVE, Common.DEF_HIDE_INACTIVE);
				}
				if (intent.hasExtra(Common.KEY_HIDE_INACTIVE)) {
					prefHideInactive = intent.getBooleanExtra(Common.KEY_HIDE_INACTIVE, Common.DEF_HIDE_INACTIVE);
				}
				if (intent.hasExtra(Common.KEY_HIDE_NETWORK_TYPE)) {
					Object extra = intent.getSerializableExtra(Common.KEY_HIDE_NETWORK_TYPE);
					if (extra != null)
						prefHideNetworkState = (Set<String>) extra;
				}
				if(intent.hasExtra(Common.KEY_FONT_SIZE)) {
				    prefFontSize = intent.getIntExtra(Common.KEY_FONT_SIZE, Common.DEF_FONT_SIZE);
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
			if (((float) downloadSpeed) / 1048576 >= 1) { // 1024 * 1024 113
				downloadValue = ((float) downloadSpeed) / 1048576f;
				downloadUnit = "MB/s";
			} else if (((float) downloadSpeed) / 1024f >= 1) {
				downloadValue = ((float) downloadSpeed) / 1024f;
				downloadUnit = "KB/s";
			} else {
				downloadValue = downloadSpeed;
				downloadUnit = "B/s";
			}
			
			if (((float) uploadSpeed) / 1048576 >= 1) { // 1024 * 1024 113
			    uploadValue = ((float) uploadSpeed) / 1048576f;
			    uploadUnit = "MB/s";
            } else if (((float) uploadSpeed) / 1024f >= 1) {
                uploadValue = ((float) uploadSpeed) / 1024f;
                uploadUnit = "KB/s";
            } else {
                uploadValue = uploadSpeed;
                uploadUnit = "B/s";
            }
			
			break;
		case 1:
			downloadValue = downloadSpeed;
			uploadValue = uploadSpeed;
			uploadUnit = downloadUnit = "B/s";
			break;
		case 2:
			downloadValue = ((float) downloadSpeed) / 1024f;
			uploadValue = ((float) uploadSpeed) / 1024f;
			uploadUnit = downloadUnit = "KB/s";
			break;
		case 3:
			downloadValue = ((float) downloadSpeed) / 1048576f;
			uploadValue = ((float) uploadSpeed) / 1048576f;
			uploadUnit = downloadUnit = "MB/s";
			break;
		}
		
		
		String strUploadValue, strDownloadValue;
		
		if (prefHideInactive && downloadSpeed <= 0) {
		    strDownloadValue = "";
        }
		else {
		    strDownloadValue = decimalFormat.format(downloadValue);
		}
		if (prefHideInactive && uploadValue <= 0) {
		    strUploadValue = "";
        }
		else {
		    strUploadValue = decimalFormat.format(uploadValue);
		}
		
		if (!prefHideUnit) {
		    if(strUploadValue.length() > 0) {
		        strUploadValue = "U:" + strUploadValue + uploadUnit;
		    }
		    if(strDownloadValue.length() > 0) {
		        strDownloadValue = "D:" + strDownloadValue + downloadUnit;
		    }
		}
		
		String ret = "";
		if(strUploadValue.length() > 0 && strDownloadValue.length() > 0) {
		    ret = strUploadValue + " " + strDownloadValue; 
		}
		else {
		    ret = strUploadValue + strDownloadValue;
		}
		return ret;
	}

	public void update() {
		mTrafficHandler.removeCallbacks(mRunnable);
		mTrafficHandler.postDelayed(mRunnable, 1000);
	}

	Runnable mRunnable = new Runnable() {
		@Override
		public void run() {
			mTrafficHandler.sendEmptyMessage(0);
		}
	};

	private void updateViewVisibility() {
		if (networkState && !prefHideNetworkState.contains(networkType)) {
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
		prefHideUnit = mPref.getBoolean(Common.KEY_HIDE_UNIT, Common.DEF_HIDE_UNIT);
		prefHideInactive = mPref.getBoolean(Common.KEY_HIDE_INACTIVE, Common.DEF_HIDE_INACTIVE);
		prefHideNetworkState = mPref.getStringSet(Common.KEY_HIDE_NETWORK_TYPE, Common.DEF_HIDE_NETWORK_STATE);
		prefFontSize = mPref.getInt(Common.KEY_FONT_SIZE, Common.DEF_FONT_SIZE);
	}
}
