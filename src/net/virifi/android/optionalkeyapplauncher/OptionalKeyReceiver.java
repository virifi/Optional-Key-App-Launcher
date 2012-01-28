package net.virifi.android.optionalkeyapplauncher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

public class OptionalKeyReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences pref = context.getSharedPreferences(OptionalKeyAppLauncherActivity.PREF_NAME, Context.MODE_PRIVATE);
		String packageName = pref.getString(OptionalKeyAppLauncherActivity.PACKAGE_NAME_KEY, null);
		if (packageName == null) return;
	
		PackageManager pkgMgr = context.getPackageManager();
		Intent launchIntent = pkgMgr.getLaunchIntentForPackage(packageName);
		if (launchIntent == null) return;
		launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		context.startActivity(launchIntent);
	}

}
