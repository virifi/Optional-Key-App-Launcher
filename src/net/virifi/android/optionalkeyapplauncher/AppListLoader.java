package net.virifi.android.optionalkeyapplauncher;

import java.util.Collections;
import java.util.List;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

public class AppListLoader extends AsyncTaskLoader<List<ResolveInfo>> {
	Context mContext;
	List<ResolveInfo> result;
	PackageManager mPackageManager;

	public AppListLoader(Context context) {
		super(context);
		mContext = context;
		mPackageManager = context.getPackageManager();
	}

	@Override
	public List<ResolveInfo> loadInBackground() {
		Intent intent = new Intent(Intent.ACTION_MAIN, null);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		List<ResolveInfo> resolveInfoList = mPackageManager.queryIntentActivities(intent, 0);
		Collections.sort(resolveInfoList, new ResolveInfo.DisplayNameComparator(mPackageManager));
		
		return resolveInfoList;
	}

    @Override
    public void deliverResult(List<ResolveInfo> data) {
        if (isReset()) {
            if (this.result != null) {
                this.result = null;
            }
            return;
        }

        this.result = data;

        if (isStarted()) {
            super.deliverResult(data);
        }
    }

    @Override
    protected void onStartLoading() {
        if (this.result != null) {
            deliverResult(this.result);
        }
        if (takeContentChanged() || this.result == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        super.onStopLoading();
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();
        onStopLoading();
    }
}
