package tw.fatminmin.xposed.networkspeedindicator.widget;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LayoutInflated.LayoutInflatedParam;

public class JellyBeanPositionCallbackImpl implements PositionCallback {
    
    public static final String PKG_NAME_SYSTEM_UI = "com.android.systemui";
    
	private ViewGroup mStatusIcons;
	private ViewGroup mIcons;
	private View view;
	private ViewGroup mNotificationIconArea;

	@Override
	public void setup(MethodHookParam param, View v) {
		 view = v;
		 mStatusIcons = (ViewGroup)XposedHelpers.getObjectField(param.thisObject, "mStatusIcons");
		 mIcons = (ViewGroup)XposedHelpers.getObjectField(param.thisObject, "mIcons");
		 mNotificationIconArea = (ViewGroup)mIcons.getChildAt(0);
	}
	
	@Override
	public void setup(LayoutInflatedParam liparam, View v) {
	    view = v;
	    
	    FrameLayout root = (FrameLayout) liparam.view;
	    mStatusIcons = (ViewGroup) root.findViewById(liparam.res.getIdentifier("statusIcons", "id",
                PKG_NAME_SYSTEM_UI));
	    mIcons = (ViewGroup) root.findViewById(liparam.res.getIdentifier("icons", "id",
                PKG_NAME_SYSTEM_UI));
	    mNotificationIconArea = (ViewGroup) root.findViewById(liparam.res.getIdentifier("notification_icon_area", "id",
                PKG_NAME_SYSTEM_UI));
	    
	}
	
	@Override
	public void setAbsoluteLeft() {
		removeFromParent();
		mNotificationIconArea.addView(view, 0);
	}

	@Override
	public void setLeft() {
		removeFromParent();
		mIcons.addView(view, mIcons.indexOfChild(mStatusIcons));
	}

	@Override
	public void setRight() {
		removeFromParent();
		mIcons.addView(view);
	}
	
	private void removeFromParent() {
		if(view.getParent()!=null)
			((ViewGroup)view.getParent()).removeView(view);
	}
	@Override
	public ViewGroup getClockParent() {
		return mIcons;
	}
}
