package net.virifi.android.optionalkeyapplauncher;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class OptionalKeyAppLauncherActivity extends Activity implements LoaderCallbacks<List<ResolveInfo>>{
    public static final String PREF_NAME = "OptionalKeySetting";
    public static final String PACKAGE_NAME_KEY = "packageName";
    
    
	private PackageManager mPackageManager = null;
	private ListView mListView = null;
	private AlertDialog mAlertDialog = null;
	private List<ResolveInfo> mResolveInfoList;
	private ProgressDialog mProgressDialog = null;
	private SharedPreferences mPref;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mPackageManager = getPackageManager();
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setMessage("Loading application list");
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		
		mPref = getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
		loadSetting();

        Button getAppListButton = (Button) findViewById(R.id.get_app_list_button);
        getAppListButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mProgressDialog.show();
				getLoaderManager().restartLoader(0, null, OptionalKeyAppLauncherActivity.this);
				//getLoaderManager().initLoader(0, null, OptionalKeyAppLauncherActivity.this);
			}
		});
        
        Button testButton = (Button) findViewById(R.id.test_button);
        testButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent("com.android.systemui.statusbar.OPTIONAL_BUTTON_CLICKED");
				sendBroadcast(intent);
			}
		});
    }

	@Override
	public Loader<List<ResolveInfo>> onCreateLoader(int id, Bundle args) {
		return new AppListLoader(getApplicationContext());
	}

	@Override
	public void onLoadFinished(Loader<List<ResolveInfo>> loader,
			List<ResolveInfo> resolveInfoList) {
		mResolveInfoList = resolveInfoList;
		AppListAdapter adapter = new AppListAdapter(this, R.layout.app_list_item, mResolveInfoList);
        ListView listView = new ListView(this);
        listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> items, View view, int position, long id) {
				mAlertDialog.dismiss();
				ResolveInfo info = mResolveInfoList.get(position);
				setNewApp(info.loadIcon(mPackageManager), info.loadLabel(mPackageManager), info.activityInfo.packageName);
			}
		});
		listView.setAdapter(adapter);
		mAlertDialog = new AlertDialog.Builder(this).setTitle("Select App to launch").setPositiveButton("Cancel", null).setView(listView).create();
		mProgressDialog.dismiss();
		mAlertDialog.show();
	}

	@Override
	public void onLoaderReset(Loader<List<ResolveInfo>> arg0) {
		// TODO 自動生成されたメソッド・スタブ
		
	}
	
	private void loadSetting() {
		String packageName = mPref.getString(PACKAGE_NAME_KEY, null);
		if (packageName == null) {
			setCurrentSettingText(false);
			return;
		}
		
		try {
			ApplicationInfo appInfo = mPackageManager.getApplicationInfo(packageName, 0);
			setNewApp(appInfo.loadIcon(mPackageManager), appInfo.loadLabel(mPackageManager), appInfo.packageName);
		} catch (NameNotFoundException e) {
			setCurrentSettingText(false);
		}
	}
	
	private void setNewApp(Drawable icon, CharSequence appName, String packageName) {
		ImageView appIconView = (ImageView) findViewById(R.id.app_icon);
		TextView appNameView = (TextView) findViewById(R.id.app_name);
		TextView packageNameView = (TextView) findViewById(R.id.package_name);
		LinearLayout layout = (LinearLayout) findViewById(R.id.current_app_container);
		
		appIconView.setImageDrawable(icon);
		appNameView.setText(appName);
		packageNameView.setText(packageName);
		
		Editor editor = mPref.edit();
		editor.putString(PACKAGE_NAME_KEY, packageName);
		editor.commit();
		
		setCurrentSettingText(true);
		layout.setVisibility(View.VISIBLE);
	}
	
	private void setCurrentSettingText(boolean isSet) {
		String str = "";
		TextView textView = (TextView) findViewById(R.id.current_setting_text);
		if (isSet) {
			str = "Current Setting";
		} else {
			str = "Current Setting : not set";
		}
		textView.setText(str);
	}

}