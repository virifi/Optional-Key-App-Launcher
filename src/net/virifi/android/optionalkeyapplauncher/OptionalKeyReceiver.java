package net.virifi.android.optionalkeyapplauncher;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

public class OptionalKeyReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences pref = context.getSharedPreferences(
				OptionalKeyAppLauncherActivity.PREF_NAME, Context.MODE_PRIVATE);
		String packageNameKey;
		String modeStrKey;

		if ("com.android.systemui.statusbar.OPTIONAL_BUTTON_CLICKED".equals(intent.getAction())) {
			packageNameKey = OptionalKeyAppLauncherActivity.PACKAGE_NAME_KEY1;
			modeStrKey = OptionalKeyAppLauncherActivity.MODE_STR1;
		} else {
			packageNameKey = OptionalKeyAppLauncherActivity.PACKAGE_NAME_KEY2;
			modeStrKey = OptionalKeyAppLauncherActivity.MODE_STR2;
		}

		String modeStr = pref.getString(modeStrKey, null);
		if (modeStr == null)
			return;

		if (modeStr.equals(OptionalKeyAppLauncherActivity.MODE_PREVIOUS)) {
			ActivityManager am = (ActivityManager) context
					.getSystemService(Context.ACTIVITY_SERVICE);
			List<ActivityManager.RecentTaskInfo> recentTaskList = am
					.getRecentTasks(8, ActivityManager.RECENT_WITH_EXCLUDED);
			PackageManager pm = context.getPackageManager();
			Intent queryIntent = new Intent();
			queryIntent.setAction(Intent.ACTION_MAIN);
			queryIntent.addCategory(Intent.CATEGORY_HOME);
			final List<ResolveInfo> homeAppList = pm.queryIntentActivities(
					queryIntent, 0);

			for (int j = 1; j < recentTaskList.size(); j++) {
				RecentTaskInfo info = recentTaskList.get(j);
				Intent i = info.baseIntent;
				String packageName = i.getComponent().getPackageName();
				
				// exclude home apps
				boolean matched = false;
				for (ResolveInfo homeInfo : homeAppList) {
					if (packageName.equals(homeInfo.activityInfo.packageName)) {
						matched = true;
						break;
					}
				}
				if (matched) {
					continue;
				}

				try {
					i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(i);
					break;
				} catch (ActivityNotFoundException e) {
					;
				} catch (SecurityException e) {
					// for SuperUser
					;
				}

			}
		} else {
			String packageName = pref.getString(packageNameKey, null);
			if (packageName == null)
				return;

			PackageManager pkgMgr = context.getPackageManager();
			Intent launchIntent = pkgMgr.getLaunchIntentForPackage(packageName);
			if (launchIntent == null)
				return;
			launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			context.startActivity(launchIntent);
		}
	}

}
