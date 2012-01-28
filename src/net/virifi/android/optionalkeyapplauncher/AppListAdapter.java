package net.virifi.android.optionalkeyapplauncher;

import java.util.List;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AppListAdapter extends ArrayAdapter<ResolveInfo> {
	private LayoutInflater mInflater;
	private int mLayoutId;
	private PackageManager mPackageManager;
	
	static class ViewHolder {
		ImageView mIconImageView;
		TextView mAppNameTextView;
		TextView mPackageNameTextView;
	}

	public AppListAdapter(Context context, int textViewResourceId, List<ResolveInfo> objects) {
		super(context, 0, objects);
		
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mLayoutId = textViewResourceId;
		mPackageManager = context.getPackageManager();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		
		if (convertView == null) {
			convertView = mInflater.inflate(mLayoutId, parent, false);
			holder = new ViewHolder();
			holder.mIconImageView = (ImageView) convertView.findViewById(R.id.app_icon);
			holder.mAppNameTextView = (TextView) convertView.findViewById(R.id.app_name);
			holder.mPackageNameTextView = (TextView) convertView.findViewById(R.id.package_name);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		ResolveInfo appInfo = getItem(position);
		holder.mIconImageView.setImageDrawable(appInfo.loadIcon(mPackageManager));
		holder.mAppNameTextView.setText(appInfo.loadLabel(mPackageManager));
		holder.mPackageNameTextView.setText(appInfo.activityInfo.packageName);
		
		return convertView;
	}
	
	

}
