package net.virifi.android.optionalkeyapplauncher;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.ProgressDialog;
import android.content.Context;
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
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

public class OptionalKeyAppLauncherActivity extends Activity implements
		LoaderCallbacks<List<ResolveInfo>> {
	private Context mContext = null;
	
	public static final String PREF_NAME = "OptionalKeySetting";
	public static final String PACKAGE_NAME_KEY1 = "packageName1";
	public static final String PACKAGE_NAME_KEY2 = "packageName2";
	public static final String MODE_STR1 = "modeStr1";
	public static final String MODE_STR2 = "modeStr2";
	
	public static final String MODE_PREVIOUS = "previous";
	public static final String MODE_SELECT = "select";
	
	private static final int NORMAL_CLICKED = 0;
	private static final int LONG_CLICKED = 1;
	

	private PackageManager mPackageManager = null;
	private ListView mListView = null;
	private AlertDialog mAlertDialog = null;
	private List<ResolveInfo> mResolveInfoList;
	private ProgressDialog mProgressDialog = null;
	private SharedPreferences mPref;

	private View mContainer1 = null;
	private View mContainer2 = null;
	private Switch mSwitch1 = null;
	private Switch mSwitch2 = null;
	private TextView mUnsetTextView1 = null;
	private TextView mUnsetTextView2 = null;
	
	private int mCurrentMode = -1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mContext = getApplicationContext();

		mPackageManager = getPackageManager();
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setMessage("Loading application list");
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

		mPref = getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);

		mContainer1 = findViewById(R.id.current_app_container);
		mContainer2 = findViewById(R.id.current_app_container2);
		mSwitch1 = (Switch) findViewById(R.id.switch1);
		mSwitch2 = (Switch) findViewById(R.id.switch2);
		mUnsetTextView1 = (TextView) findViewById(R.id.unset_text1);
		mUnsetTextView2 = (TextView) findViewById(R.id.unset_text2);
		
		loadSetting();
		
		mSwitch1.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Editor editor = mPref.edit();
				if (isChecked) {
					editor.putString(MODE_STR1, MODE_SELECT);
				} else {
					editor.putString(MODE_STR1, MODE_PREVIOUS);
				}
				editor.commit();
				loadSetting();
			}
		});
		mSwitch2.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Editor editor = mPref.edit();
				if (isChecked) {
					editor.putString(MODE_STR2, MODE_SELECT);
				} else {
					editor.putString(MODE_STR2, MODE_PREVIOUS);
				}
				editor.commit();
				loadSetting();
			}
		});
		

		OnClickListener appSelectListener1 = new OnClickListener() {
			@Override
			public void onClick(View v) {
				mProgressDialog.show();
				mCurrentMode = NORMAL_CLICKED;
				getLoaderManager().restartLoader(0, null, OptionalKeyAppLauncherActivity.this);
			}
		};
		mContainer1.setOnClickListener(appSelectListener1);
		mUnsetTextView1.setOnClickListener(appSelectListener1);

		OnClickListener appSelectListener2 = new OnClickListener() {
			@Override
			public void onClick(View v) {
				mProgressDialog.show();
				mCurrentMode = LONG_CLICKED;
				getLoaderManager().restartLoader(0, null, OptionalKeyAppLauncherActivity.this);
			}
		};
		mContainer2.setOnClickListener(appSelectListener2);
		mUnsetTextView2.setOnClickListener(appSelectListener2);
		
	}

	@Override
	public Loader<List<ResolveInfo>> onCreateLoader(int id, Bundle args) {
		return new AppListLoader(getApplicationContext());
	}

	@Override
	public void onLoadFinished(Loader<List<ResolveInfo>> loader,
			List<ResolveInfo> resolveInfoList) {
		mResolveInfoList = resolveInfoList;
		AppListAdapter adapter = new AppListAdapter(this,
				R.layout.app_list_item, mResolveInfoList);
		ListView listView = new ListView(this);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> items, View view,
					int position, long id) {
				mAlertDialog.dismiss();
				ResolveInfo info = mResolveInfoList.get(position);
				setNewApp(mCurrentMode, info.loadIcon(mPackageManager), info.loadLabel(mPackageManager), info.activityInfo.packageName);
				loadSetting();
			}
		});
		listView.setAdapter(adapter);
		mAlertDialog = new AlertDialog.Builder(this)
				.setTitle("Select App to launch")
				.setPositiveButton("Cancel", null).setView(listView).create();
		mProgressDialog.dismiss();
		mAlertDialog.show();
	}

	@Override
	public void onLoaderReset(Loader<List<ResolveInfo>> arg0) {
		// TODO 自動生成されたメソッド・スタブ

	}

	private void loadSetting() {
		String packageName1 = mPref.getString(PACKAGE_NAME_KEY1, null);
		String modeStr1 = mPref.getString(MODE_STR1, null);
		String packageName2 = mPref.getString(PACKAGE_NAME_KEY2, null);
		String modeStr2 = mPref.getString(MODE_STR2, null);
		
		
		setSetting(NORMAL_CLICKED, packageName1, modeStr1);
		setSetting(LONG_CLICKED, packageName2, modeStr2);
	}
	
	private void setSetting(int target, String packageName, String modeStr) {
		if (modeStr == null) {
			Editor editor = mPref.edit();
			String modeStrKey;
			if (target == NORMAL_CLICKED) {
				modeStrKey = MODE_STR1;
				modeStr = MODE_SELECT;
			} else {
				modeStrKey = MODE_STR2;
				modeStr = MODE_PREVIOUS;
			}
			editor.putString(modeStrKey, modeStr);
			editor.commit();
		}
		Switch modeSwitch;
		if (target == NORMAL_CLICKED) {
			modeSwitch = mSwitch1;
		} else {
			modeSwitch = mSwitch2;
		}
		if (modeStr.equals(MODE_PREVIOUS)) {
			setAppInfoVisivility(target, false, false);
			modeSwitch.setChecked(false);
		} else if (packageName == null) {
			setAppInfoVisivility(target, true, false);
			modeSwitch.setChecked(true);
		} else {
			modeSwitch.setChecked(true);
			try {
				ApplicationInfo appInfo = mPackageManager.getApplicationInfo(packageName, 0);
				setNewApp(target, appInfo.loadIcon(mPackageManager), appInfo.loadLabel(mPackageManager), appInfo.packageName);
				setAppInfoVisivility(target, true, true);
			} catch (NameNotFoundException e) {
				setAppInfoVisivility(target, true, false);
			}
		}
	}

	private void setNewApp(int target, Drawable icon, CharSequence appName, String packageName) {
		ImageView appIconView;
		TextView appNameView;
		TextView packageNameView;
		LinearLayout layout;
		String packageNameKey;
		if (target == NORMAL_CLICKED) {
			appIconView = (ImageView) findViewById(R.id.app_icon);
			appNameView = (TextView) findViewById(R.id.app_name);
			packageNameView = (TextView) findViewById(R.id.package_name);
			layout = (LinearLayout) findViewById(R.id.current_app_container);
			packageNameKey = PACKAGE_NAME_KEY1;
		} else {
			appIconView = (ImageView) findViewById(R.id.app_icon2);
			appNameView = (TextView) findViewById(R.id.app_name2);
			packageNameView = (TextView) findViewById(R.id.package_name2);
			layout = (LinearLayout) findViewById(R.id.current_app_container2);		
			packageNameKey = PACKAGE_NAME_KEY2;
		}

		appIconView.setImageDrawable(icon);
		appNameView.setText(appName);
		packageNameView.setText(packageName);

		Editor editor = mPref.edit();
		editor.putString(packageNameKey, packageName);
		editor.commit();

		setCurrentSettingText(true);
	}

	private void setAppInfoVisivility(int target, boolean visibility, boolean set) {
		TextView textView;
		View container;
		
		if (target == NORMAL_CLICKED) {
			textView = mUnsetTextView1;
			container = mContainer1;
		} else {
			textView = mUnsetTextView2;
			container = mContainer2;
		}
		
		if (visibility) {
			if (set) {
				textView.setVisibility(View.GONE);
				container.setVisibility(View.VISIBLE);
			} else {
				textView.setVisibility(View.VISIBLE);
				container.setVisibility(View.GONE);
			}
		} else {
			textView.setVisibility(View.GONE);
			container.setVisibility(View.GONE);
		}
	}

	private void setCurrentSettingText(boolean isSet) {
	}

}