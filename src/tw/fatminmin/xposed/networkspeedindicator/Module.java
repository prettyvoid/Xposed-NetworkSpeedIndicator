package tw.fatminmin.xposed.networkspeedindicator;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import tw.fatminmin.xposed.networkspeedindicator.widget.GingerBreadPositionCallbackImpl;
import tw.fatminmin.xposed.networkspeedindicator.widget.JellyBeanPositionCallbackImpl;
import tw.fatminmin.xposed.networkspeedindicator.widget.PositionCallbackImpl;
import tw.fatminmin.xposed.networkspeedindicator.widget.TrafficView;
import android.annotation.SuppressLint;
import android.content.res.XResources;
import android.graphics.Color;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.TextView;
import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class Module implements IXposedHookLoadPackage,
        IXposedHookInitPackageResources {

    
	public TextView getClock() {
		if(trafficView == null || trafficView.mPositionCallback == null) { 
			return null;
		}

		if(trafficView.mPositionCallback.getClockParent().findViewById(clock.getId()) != null) {
			return clock;
		}

		return null;
	}
	
    
	@Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
        if(!lpparam.packageName.equals(PKG_NAME_SYSTEM_UI)) {
            return;
        }
        try {
        	// we hook this method to follow alpha changes in kitkat
        	Class<?> cClock = XposedHelpers.findClass("com.android.systemui.statusbar.policy.Clock", lpparam.classLoader);
        	
    		Method setAlpha = XposedHelpers.findMethodBestMatch(cClock, "setAlpha", Float.class);
    		XposedBridge.hookMethod(setAlpha, new XC_MethodHook() {
				@SuppressLint("NewApi")
				@Override
    			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
    				
					if(param.thisObject != getClock())
						return;
					
    				if(trafficView != null && clock != null) {
    					if(android.os.Build.VERSION.SDK_INT >= 11) {
    						trafficView.setAlpha(clock.getAlpha());
    					}
    				}
    			}
    		});
    		XposedBridge.hookAllMethods(cClock, "setTextColor", new XC_MethodHook() {
				@Override
    			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
    				
					if(param.thisObject != getClock())
						return;
					
    				if(trafficView != null && clock != null) {
    					trafficView.setTextColor(clock.getCurrentTextColor());
    				}
    			}
    		});
        }
        catch(Exception e){
            
        }
    }
    
    TrafficView trafficView;
    TextView clock;

    public static final String PKG_NAME_SYSTEM_UI = "com.android.systemui";

    private static Map<String, String> mLayouts;
    static {
        Map<String, String> tmpMap = new HashMap<String, String>();
        tmpMap.put("tw_super_status_bar", "statusIcons");
        tmpMap.put("status_bar", "statusIcons");

        mLayouts = Collections.unmodifiableMap(tmpMap);
    }
    
    @Override
    public void handleInitPackageResources(InitPackageResourcesParam resparam)
            throws Throwable {
        if (!resparam.packageName.equals(PKG_NAME_SYSTEM_UI)) {
            return;
        }
        XResources res = resparam.res;

        final Entry<String, String> layoutInfo = findLayoutInfo(res);
        if (layoutInfo == null)
            return;

        res.hookLayout(PKG_NAME_SYSTEM_UI, "layout", layoutInfo.getKey(),
                new XC_LayoutInflated() {

                    @Override
                    public void handleLayoutInflated(LayoutInflatedParam liparam)
                            throws Throwable {
                        FrameLayout root = (FrameLayout) liparam.view;

                        clock = (TextView) root.findViewById(liparam.res.getIdentifier("clock", "id", PKG_NAME_SYSTEM_UI));
                        if (trafficView == null) {
                            trafficView = new TrafficView(root.getContext());
                            trafficView.clock = clock;
                        }
                        if (clock != null) {
                            trafficView.setLayoutParams(clock.getLayoutParams());
                            trafficView.setTextColor(clock.getCurrentTextColor());
                        } else {
                            // gingerbread
                            trafficView.setLayoutParams(new LayoutParams(
                                    LayoutParams.WRAP_CONTENT,
                                    LayoutParams.MATCH_PARENT));
                            trafficView.setTextColor(Color
                                    .parseColor("#33b5e5"));
                        }
                        trafficView.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);

                        if (liparam.res.getIdentifier("status_bar_contents", "id", PKG_NAME_SYSTEM_UI) != 0) {
                            // kitkat
                            trafficView.mPositionCallback = new PositionCallbackImpl();
                        } else if (liparam.res.getIdentifier("notification_icon_area", "id",PKG_NAME_SYSTEM_UI) != 0) {
                            // jellybean
                            trafficView.mPositionCallback = new JellyBeanPositionCallbackImpl();
                        } else if (liparam.res.getIdentifier("notificationIcons", "id", PKG_NAME_SYSTEM_UI) != 0) {
                            // gingerbread
                            trafficView.mPositionCallback = new GingerBreadPositionCallbackImpl();
                        }

                        trafficView.mPositionCallback.setup(liparam, trafficView);
                        trafficView.refreshPosition();
                    }
                });
    }

    public static Entry<String, String> findLayoutInfo(XResources res) {
        Iterator<Entry<String, String>> iterator = mLayouts.entrySet()
                .iterator();

        while (iterator.hasNext()) {
            Entry<String, String> entry = iterator.next();
            if (res.getIdentifier(entry.getKey(), "layout", PKG_NAME_SYSTEM_UI) != 0
                    && res.getIdentifier(entry.getValue(), "id",
                            PKG_NAME_SYSTEM_UI) != 0)
                return entry;
        }

        return null;
    }

    
}
