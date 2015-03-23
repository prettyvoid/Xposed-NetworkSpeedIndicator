//package tw.fatminmin.xposed.networkspeedindicator;
//
//import java.lang.reflect.Method;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.Map;
//import java.util.Map.Entry;
//
//import tw.fatminmin.xposed.networkspeedindicator.logger.Log;
//import tw.fatminmin.xposed.networkspeedindicator.widget.LegacyPositionCallbackImpl;
//import tw.fatminmin.xposed.networkspeedindicator.widget.PositionCallbackImpl;
//import tw.fatminmin.xposed.networkspeedindicator.widget.TrafficView;
//import android.annotation.SuppressLint;
//import android.content.Context;
//import android.content.res.XResources;
//import android.graphics.Color;
//import android.os.Build;
//import android.view.Gravity;
//import android.view.View;
//import android.view.ViewGroup.LayoutParams;
//import android.widget.FrameLayout;
//import android.widget.TextView;
//import de.robv.android.xposed.IXposedHookInitPackageResources;
//import de.robv.android.xposed.IXposedHookLoadPackage;
//import de.robv.android.xposed.IXposedHookZygoteInit;
//import de.robv.android.xposed.XC_MethodHook;
//import de.robv.android.xposed.XposedBridge;
//import de.robv.android.xposed.XposedHelpers;
//import de.robv.android.xposed.XposedHelpers.ClassNotFoundError;
//import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
//import de.robv.android.xposed.callbacks.XC_LayoutInflated;
//import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
//
//public final class Module implements IXposedHookLoadPackage, IXposedHookInitPackageResources {
//
//	private static final String TAG = Module.class.getSimpleName();
//
//	private final View getClock() {
//		if (trafficView == null || trafficView.mPositionCallback == null) {
//
//			if (trafficView == null) {
//				Log.e(TAG, "getClock() inside if - Traffic view is null");
//			} else {
//				if (trafficView.mPositionCallback == null) {
//					Log.e(TAG, "getClock() - Position CallBack is null");
//				}
//			}
//			return null;
//		}
//
//		if (trafficView.mPositionCallback.getClockParent().findViewById(
//				clock.getId()) != null) {
//			Log.e(TAG, "Returning clock");
//			return clock;
//		}
//
//		Log.e(TAG, "Returning null");
//		return null;
//	}
//
////	@Override
////	public final void handleLoadPackage(final LoadPackageParam lpparam)
////			throws Throwable {
////		try {
////			if (!lpparam.packageName.equals(PKG_NAME_SYSTEM_UI)) {
////				return;
////			}
////		} catch (Exception e) {
////			Log.e(TAG, "handleLoadPackage failed: ", e);
////			throw e;
////		}
////		try {
////			Log.e(TAG, "handleLoadPackage - We're Inside SystemUI");
////			Class<?> cClock = XposedHelpers.findClass("com.android.systemui.statusbar.policy.Clock", lpparam.classLoader);
////			Log.e(TAG, "got Class<?> cClock - simple name: " + cClock.getSimpleName());
////			 XposedBridge.hookAllMethods(cClock, "setTextColor", new XC_MethodHook() {
////				 
////				@Override
////				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
////					Log.e(TAG,"Before Hooked - Handle Load Package");
////				}
////				 
////				@Override
////				protected final void afterHookedMethod(final MethodHookParam param) throws Throwable {
////					try {
////						Log.e(TAG, "Calling getClock() from handleLoadPackage-SetTextColor-AfterHookedMethod");
////						if (param.thisObject != getClock()) {
////							Log.e(TAG, "Exiting from handleLoadPackage-AfterHookedMethod - if(param.thisObject != getClock()) ");
////							return;
////						}
////						Log.e(TAG, "HEHE4");
////						if (trafficView != null && clock != null) {
////							Log.e(TAG, "HEHE5");
////							if (clock instanceof TextView) {
////								trafficView.setTextColor(((TextView) clock).getCurrentTextColor());
////							} else {
////								// probably LinearLayout in VN ROM v14.1 (need
////								// to search child elements to find correct text
////								// color)
////								Log.e(TAG, "HEHEXXXX");
////								Log.w(TAG, "clock is not a TextView, it is ", clock.getClass().getSimpleName());
////								trafficView.setTextColor(Common.ANDROID_SKY_BLUE);
////							}
////						}
////					} catch (Exception e) {
////						Log.e(TAG, "afterHookedMethod (setTextColor) failed: ", e);
////						throw e;
////					}
////				}
////			});
////
//////			// we hook this method to follow alpha changes in kitkat
//////			Method setAlpha = XposedHelpers.findMethodBestMatch(cClock,"setAlpha", Float.class);
//////			XposedBridge.hookMethod(setAlpha, new XC_MethodHook() {
//////				@SuppressLint("NewApi")
//////				@Override
//////				protected final void afterHookedMethod(final MethodHookParam param) throws Throwable {
//////					try {
//////						Log.e(TAG,"Calling getClock from handleLoadPackage-SetAlpha-AfterHookedMethod");
//////						if (param.thisObject != getClock())
//////							return;
//////
//////						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//////							if (trafficView != null) {
//////								if (statusIcons != null) {
//////									trafficView.setAlpha(statusIcons.getAlpha());
//////								} else if (clock != null) {
//////									trafficView.setAlpha(clock.getAlpha());
//////								}
//////							}
//////						}
//////					} catch (Exception e) {
//////						Log.e(TAG, "afterHookedMethod (setAlpha) failed: ", e);
//////						throw e;
//////					}
//////				}
//////			});
////		} catch (Exception e) {
////			Log.e(TAG, "handleLoadPackage failure ignored: ", e);
////		} catch (ClassNotFoundError e) {
////			// Clock class not found, ignore
////			Log.w(TAG, "handleLoadPackage failure ignored: ", e);
////		} catch (NoSuchMethodError e) {
////			// setAlpha method not found, ignore
////			Log.w(TAG, "handleLoadPackage failure ignored: ", e);
////		}
////	}
//
//    @Override
//    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
//        if (!lpparam.packageName.equals("com.android.systemui"))
//            return;
//        XposedHelpers.findAndHookMethod(
//                "com.android.systemui.statusbar.phone.PhoneStatusBar",
//                lpparam.classLoader, "makeStatusBarView", new XC_MethodHook() {
//                    
//                    @Override
//                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                        try {
//                        	Log.e(TAG,"After Hooked Method - Handle Load Package");
//                            Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
//                            if(trafficView == null) {
//                                trafficView = new TrafficView(mContext);
//                            }
//                            
//                            boolean legacy = false;
//                            try {
//                                XposedHelpers.getObjectField(param.thisObject, "mSystemIconArea");
//                            }
//                            catch(NoSuchFieldError e) {
//                                legacy = true;
//                            }
//                            
//                            if(legacy) {
//                                trafficView.mPositionCallback = new LegacyPositionCallbackImpl();
//                            }
//                            else {
//                                trafficView.mPositionCallback = new PositionCallbackImpl();
//                            }
//                            
//                            trafficView.mPositionCallback.setup(param, trafficView);
//                            trafficView.refreshPosition();
//                        }
//                        catch(Exception e) {
//                        }
//                    }
//                    
//                });
//    }
//	
//	private TrafficView trafficView;
//	private View clock;
//	private View statusIcons;
//
//	private static final String PKG_NAME_SYSTEM_UI = "com.android.systemui";
//
//	private static final Map<String, String> mLayouts;
//	static {
//		Map<String, String> tmpMap = new HashMap<String, String>();
//		tmpMap.put("tw_super_status_bar", "statusIcons");
//		tmpMap.put("status_bar", "statusIcons");
//
//		mLayouts = Collections.unmodifiableMap(tmpMap);
//	}
//	
//	@Override
//	public void handleInitPackageResources(InitPackageResourcesParam resparam) throws Throwable {
//		if (!resparam.packageName.equals(PKG_NAME_SYSTEM_UI))
//			return;
//		XResources res = resparam.res;
//
//		final Entry<String, String> layoutInfo = findLayoutInfo(res);
//		if (layoutInfo == null)
//			return;
//
//		res.hookLayout(PKG_NAME_SYSTEM_UI, "layout", layoutInfo.getKey(), new XC_LayoutInflated() {
//
//			@Override
//			public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
//				Log.e(TAG,"handleLayoutInflated - handleInitPackageResources");
//				FrameLayout root = (FrameLayout) liparam.view;
//				
//				TextView clock = (TextView) root.findViewById(liparam.res.getIdentifier("clock", "id",
//						PKG_NAME_SYSTEM_UI));
//				if(trafficView == null) {
//				    trafficView = new TrafficView(root.getContext());
//				}
//				trafficView.setLayoutParams(clock.getLayoutParams());
//				trafficView.setTextColor(clock.getCurrentTextColor());
//				trafficView.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
//			}
//		});
//	}
//
////	@Override
////	public final void handleInitPackageResources(final InitPackageResourcesParam resparam) throws Throwable {
////		
////		try {
////			if (!resparam.packageName.equals(PKG_NAME_SYSTEM_UI)) {
////				return;
////			}
////
////			Log.e(TAG, "handleInitPackageResources - We're Inside SystemUI");
////			XResources res = resparam.res;
////
////			final Entry<String, String> layoutInfo = findLayoutInfo(res);
////			
////			if (layoutInfo == null) {
////				Log.e(TAG, "exiting handleInitPackageResources - if (layoutInfo == null) {");
////				return;
////			}
////			
////			Log.e(TAG, "handleInitPackageResources - hooking layout");
////
////			Log.e(TAG, "LayoutInfo.getKey() = " + layoutInfo.getKey());
////			
////			res.hookLayout("com.android.systemui", "layout", "status_bar", new XC_LayoutInflated() {
////						@Override
////						public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
////							try {
////								Log.e(TAG, "handleLayoutInflated");
////								FrameLayout root = (FrameLayout) liparam.view;
////
////								clock = root.findViewById(liparam.res.getIdentifier("clock", "id",PKG_NAME_SYSTEM_UI));
////								statusIcons = root.findViewById(liparam.res.getIdentifier("statusIcons", "id", PKG_NAME_SYSTEM_UI));
////
////								if (trafficView == null) {
////									trafficView = new TrafficView(root.getContext());
////									trafficView.clock = clock;
////								}
////								
////								if (clock != null) {
////									Log.e(TAG, "I'm here 5");
////									trafficView.setLayoutParams(clock.getLayoutParams());
////									if (clock instanceof TextView) {
////										trafficView.setTextColor(((TextView) clock).getCurrentTextColor());
////									} else {
////										Log.e(TAG, "I'm here 6");
////										// probably LinearLayout in VN ROM v14.1
////										// (need to search child elements to
////										// find correct text color)
////										Log.w(TAG,"clock is not a TextView, it is ",clock.getClass().getSimpleName());
////										trafficView.setTextColor(Common.ANDROID_SKY_BLUE);
////									}
////								} else {
////									Log.e(TAG, "I'm here 7");
////									Log.i(TAG, "Clock: Gingerbread");
////									trafficView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.MATCH_PARENT));
////									trafficView.setTextColor(Common.ANDROID_SKY_BLUE);
////								}
////								
////								Log.e(TAG, "I'm he");
////								trafficView.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
////
////								if (liparam.res.getIdentifier("status_bar_contents", "id",
////										PKG_NAME_SYSTEM_UI) != 0) {
////									Log.e(TAG, "I'm here 8");
////									Log.i(TAG, "PositionCallback: KitKat");
////									trafficView.mPositionCallback = new PositionCallbackImpl();
////
////								} else if (liparam.res.getIdentifier("notification_icon_area", "id", PKG_NAME_SYSTEM_UI) != 0) {
////									Log.e(TAG, "I'm here 9");
////									Log.i(TAG, "PositionCallback: Jelly Bean");
////									trafficView.mPositionCallback = new JellyBeanPositionCallbackImpl();
////								} else if (liparam.res.getIdentifier("notificationIcons", "id", PKG_NAME_SYSTEM_UI) != 0) {
////									Log.e(TAG, "I'm here 10");
////									Log.i(TAG, "PositionCallback: Gingerbread");
////									trafficView.mPositionCallback = new GingerBreadPositionCallbackImpl();
////								}
////								Log.e(TAG, "I'm here 11");
////								trafficView.mPositionCallback.setup(liparam, trafficView);
////								trafficView.refreshPosition();
////							} catch (Exception e) {
////								Log.e(TAG, "I'm here 15");
////								Log.e(TAG, "handleLayoutInflated failed: ", e);
////								throw e;
////							}
////						}
////					});
////		} catch (Exception e) {
////			Log.e(TAG, "handleInitPackageResources failed: ", e);
////			throw e;
////		}
////	}
//
//	private static final Entry<String, String> findLayoutInfo(final XResources res) {
//		Iterator<Entry<String, String>> iterator = mLayouts.entrySet().iterator();
//
//		while (iterator.hasNext()) {
//			Entry<String, String> entry = iterator.next();
//			if (res.getIdentifier(entry.getKey(), "layout", PKG_NAME_SYSTEM_UI) != 0 && res.getIdentifier(entry.getValue(), "id", PKG_NAME_SYSTEM_UI) != 0)
//				return entry;
//		}
//
//		return null;
//	}
//}

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