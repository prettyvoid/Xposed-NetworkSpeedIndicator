package tw.fatminmin.xposed.networkspeedindicator.widget;

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class LegacyPositionCallbackImpl implements PositionCallback {

	private LinearLayout mStatusIcons;
	private LinearLayout mIcons;
	private View view;
	private LinearLayout mNotificationIconArea;

	@Override
	public void setup(MethodHookParam param, View v) {
		 view = v;
		 mStatusIcons = (LinearLayout)XposedHelpers.getObjectField(param.thisObject, "mStatusIcons");
		 mIcons = (LinearLayout)XposedHelpers.getObjectField(param.thisObject, "mIcons");
		 mNotificationIconArea = (LinearLayout)mIcons.getChildAt(0);
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
	public LinearLayout getClockParent() {
		return mIcons;
	}
}
