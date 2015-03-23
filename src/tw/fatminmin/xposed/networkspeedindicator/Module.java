package tw.fatminmin.xposed.networkspeedindicator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import tw.fatminmin.xposed.networkspeedindicator.widget.LegacyPositionCallbackImpl;
import tw.fatminmin.xposed.networkspeedindicator.widget.PositionCallbackImpl;
import tw.fatminmin.xposed.networkspeedindicator.widget.TrafficView;
import android.content.Context;
import android.content.res.XResources;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;
import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class Module implements IXposedHookInitPackageResources, 
                                IXposedHookLoadPackage {
    
    TrafficView trafficView;
    
    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.android.systemui"))
            return;
        XposedHelpers.findAndHookMethod(
                "com.android.systemui.statusbar.phone.PhoneStatusBar",
                lpparam.classLoader, "makeStatusBarView", new XC_MethodHook() {
                    
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        try {
                            Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                            if(trafficView == null) {
                                trafficView = new TrafficView(mContext);
                            }
                            
                            boolean legacy = false;
                            try {
                                XposedHelpers.getObjectField(param.thisObject, "mSystemIconArea");
                            }
                            catch(NoSuchFieldError e) {
                                legacy = true;
                            }
                            
                            if(legacy) {
                                trafficView.mPositionCallback = new LegacyPositionCallbackImpl();
                            }
                            else {
                                trafficView.mPositionCallback = new PositionCallbackImpl();
                            }
                            
                            trafficView.mPositionCallback.setup(param, trafficView);
                            trafficView.refreshPosition();
                        }
                        catch(Exception e) {
                        }
                    }
                    
                });
    }
    
    
    
	public static final String PKG_NAME_SYSTEM_UI = "com.android.systemui";

	private static Map<String, String> mLayouts;
	static {
		Map<String, String> tmpMap = new HashMap<String, String>();
		tmpMap.put("tw_super_status_bar", "statusIcons");
		tmpMap.put("status_bar", "statusIcons");

		mLayouts = Collections.unmodifiableMap(tmpMap);
	}
	
	@Override
	public void handleInitPackageResources(InitPackageResourcesParam resparam) throws Throwable {
		if (!resparam.packageName.equals(PKG_NAME_SYSTEM_UI))
			return;
		XResources res = resparam.res;

		final Entry<String, String> layoutInfo = findLayoutInfo(res);
		if (layoutInfo == null)
			return;

		res.hookLayout(PKG_NAME_SYSTEM_UI, "layout", layoutInfo.getKey(), new XC_LayoutInflated() {

			@Override
			public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
				FrameLayout root = (FrameLayout) liparam.view;
				
				TextView clock = (TextView) root.findViewById(liparam.res.getIdentifier("clock", "id",
						PKG_NAME_SYSTEM_UI));
				if(trafficView == null) {
				    trafficView = new TrafficView(root.getContext());
				}
				trafficView.setLayoutParams(clock.getLayoutParams());
				trafficView.setTextColor(clock.getCurrentTextColor());
				trafficView.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
			}
		});
	}

	public static Entry<String, String> findLayoutInfo(XResources res) {
		Iterator<Entry<String, String>> iterator = mLayouts.entrySet().iterator();

		while (iterator.hasNext()) {
			Entry<String, String> entry = iterator.next();
			if (res.getIdentifier(entry.getKey(), "layout", PKG_NAME_SYSTEM_UI) != 0
					&& res.getIdentifier(entry.getValue(), "id", PKG_NAME_SYSTEM_UI) != 0)
				return entry;
		}

		return null;
	}
}