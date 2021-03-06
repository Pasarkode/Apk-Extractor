package com.example.applist;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ExpandableListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.Toast;

@SuppressLint("NewApi")
public class MainActivity extends ExpandableListActivity implements
		OnChildClickListener, OnGroupExpandListener, OnGroupCollapseListener,
		OnGroupClickListener, MultiChoiceModeListener {
	static PackageManager packagemanager;
	final String knox = "com.sec.knox.containeragent.USE_CONTAINERAGENT";
	final String knoxapp = "com.sec.knox.containeragent.USE_KNOX_UI";
	ExpandableListView apkList;
	List<PackageInfo> packageList;
	List<PackageInfo> packageList1;
	static List<PackageInfo> selected;
	List<Boolean> selection;
	String path = Environment.getExternalStorageDirectory().toString()
			+ "/MyApps";
	ApkAdapter a;
	private static int expand = -1;
	ActionMode action;
	static boolean exit;
	boolean actionModeEnabled = false;
	GroupPackage pack;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		exit = false;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ActionBar bar = getActionBar();
		selection = new ArrayList<Boolean>();
		bar.setSplitBackgroundDrawable(new ColorDrawable(Color.rgb(0, 0, 0)));
		packagemanager = getPackageManager();
		packageList = packagemanager
				.getInstalledPackages(PackageManager.GET_PERMISSIONS);
		packageList1 = new ArrayList<PackageInfo>();
		for (PackageInfo pi : packageList) {
			boolean b = isSystemPackage(pi);

			@SuppressWarnings("unused")
			String[] permission = (pi.requestedPermissions);
			if (!b) {
				try {
					packageList1.add(pi);
					selection.add(false);
				} catch (Exception e) {
				}
			}
		}
		sort(packageList1);
		a = new ApkAdapter(this, packageList1, packagemanager, selection);
		apkList = (ExpandableListView) findViewById(android.R.id.list);

		apkList.setAdapter(a);
		apkList.setChoiceMode(ExpandableListView.CHOICE_MODE_MULTIPLE_MODAL);
		apkList.setOnChildClickListener(this);
		apkList.setOnGroupExpandListener(this);
		apkList.setOnGroupCollapseListener(this);
		apkList.setOnGroupClickListener(this);
		apkList.setMultiChoiceModeListener(this);
	}

	private boolean isSystemPackage(PackageInfo pi) {
		// TODO Auto-generated method stub
		return ((pi.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) ? true
				: false;

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.apk, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.genapk:
			new Gen().execute(packageList1);
			break;
		case R.id.action_settings:
			Toast.makeText(getBaseContext(), path, Toast.LENGTH_LONG).show();
			break;
		case R.id.genfile:
			WriteData wd = new WriteData();
			Log.v("onmenu reached", "true");
			wd.makeFile(packageList1);
			Toast.makeText(getBaseContext(), "File stored in external memory ",
					Toast.LENGTH_LONG).show();
			break;

		}

		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public void onGroupExpand(int groupPosition) {
		// TODO Auto-generated method stub
		if (expand != -1) {
			// if(apkList.isGroupExpanded(groupPosition))
			apkList.collapseGroup(expand);
		}
		expand = groupPosition;
		super.onGroupExpand(groupPosition);
	}

	@Override
	public void onGroupCollapse(int groupPosition) {
		// TODO Auto-generated method stub
		if (expand == groupPosition)
			expand = -1;
		super.onGroupCollapse(groupPosition);

	}

	@SuppressWarnings("rawtypes")
	public class Gen extends AsyncTask<List, Integer, String> {

		ProgressDialog dialog;

		protected void onPreExecute() {
			// example of setting up something
			dialog = new ProgressDialog(MainActivity.this);
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.setMax(100);
			dialog.show();

		}

		@Override
		protected String doInBackground(List... arg) {
			// TODO Auto-generated method stub

			// TODO Auto-generated method stub
			final Intent main = new Intent(Intent.ACTION_MAIN, null);
			main.addCategory(Intent.CATEGORY_LAUNCHER);

			final List packagelist = getPackageManager().queryIntentActivities(
					main, 0);
			// final List packagelist = arg[0];
			int z = packagelist.size();
			double b = 100 / z;
			for (final Object object : packagelist) {
				publishProgress((int) b);
				Thread app = new Thread(new Runnable() {
					public void run() {
						ResolveInfo rs = (ResolveInfo) object;
						if ((rs.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
							File f1 = new File(
									rs.activityInfo.applicationInfo.publicSourceDir);
							Log.v("file--",
									" " + f1.getName().toString() + "----"
											+ rs.loadLabel(getPackageManager()));
							try {
								String filename = rs.loadLabel(
										getPackageManager()).toString();
								Log.d("file_name--", "" + filename);
								File f2;
								String info = Environment
										.getExternalStorageState();
								if (info.equals(Environment.MEDIA_MOUNTED)) {
									f2 = new File(Environment
											.getExternalStorageDirectory()
											.toString()
											+ "/My All Apps");
								} else {
									f2 = getCacheDir();
								}
								if (!f2.exists())
									f2.mkdirs();
								f2 = new File(f2.getPath() + "/" + filename
										+ ".apk");
								path = f2.getPath();
								f2.createNewFile();
								InputStream in = new FileInputStream(f1);
								OutputStream out = new FileOutputStream(f2);
								byte[] bf = new byte[1024];
								int len;
								while ((len = in.read(bf)) > 0) {
									out.write(bf, 0, len);
								}
								in.close();
								out.close();
								System.out
										.println("BackUp of all the Apk is made");
								dialog.dismiss();
							} catch (FileNotFoundException ex) {
								System.out.println(ex.getMessage()
										+ " in the specified directory.");
							} catch (IOException e) {
								System.out.println(e.getMessage());
							}
						}

					};
				});
				app.start();
			}
			return null;
		}

		protected void onProgressUpdate(Integer... progress) {
			dialog.incrementProgressBy(progress[0]);
		}

		protected void onPostExecute(String result) {
			Toast.makeText(getApplicationContext(),
					"Back up of all application is made", Toast.LENGTH_LONG)
					.show();
		}

	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		// TODO Auto-generated method stub

		String menu = ApkAdapter.a.get(childPosition);
		if (menu.equals("Extract")) {
			PackageInfo p = packageList1.get(groupPosition);
			extractapk(p);
			Toast.makeText(getBaseContext(), "Apk generated",
					Toast.LENGTH_SHORT).show();
		}
		if (menu.equals("AppInfo")) {
			PackageInfo p = packageList1.get(groupPosition);
			AppData appdata = (AppData) getApplicationContext();
			appdata.setPackageInfo(p);

			Intent appInfo = new Intent(getApplicationContext(), ApkInfo.class);
			startActivity(appInfo);
		}
		if (menu.equals("Open")) {
			try {
				PackageInfo p = packageList1.get(groupPosition);
				Intent i = packagemanager
						.getLaunchIntentForPackage(p.packageName);
				if (i == null)
					throw new PackageManager.NameNotFoundException();
				i.addCategory(Intent.CATEGORY_LAUNCHER);
				startActivity(i);
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				Toast.makeText(getBaseContext(),
						"Its a secured app couldnot be opened",
						Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}

		}
		return false;
	}

	private void sort(List<PackageInfo> list) {
		if (list.size() > 0) {
			Collections.sort(list, new Comparator<PackageInfo>() {
				@Override
				public int compare(final PackageInfo object1,
						final PackageInfo object2) {
					return packagemanager
							.getApplicationLabel(object1.applicationInfo)
							.toString()
							.compareTo(
									packagemanager.getApplicationLabel(
											object2.applicationInfo).toString());
				}
			});
		}
	}

	@SuppressWarnings("unused")
	private void showInstalledAppDetails(ResolveInfo paramResolveInfo) {
		String str1 = paramResolveInfo.activityInfo.packageName;
		Intent localIntent = new Intent();
		int i = Build.VERSION.SDK_INT;
		if (i >= 9) {
			localIntent
					.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
			localIntent.setData(Uri.fromParts("package", str1, null));
			startActivity(localIntent);
			return;
		}
		if (i == 8) {
		}
		for (String str2 = "pkg";; str2 = "com.android.settings.ApplicationPkgName") {
			localIntent.setAction("android.intent.action.VIEW");
			localIntent.setClassName("com.android.settings",
					"com.android.settings.InstalledAppDetails");
			localIntent.putExtra(str2, str1);
			break;
		}
	}

	public void extractapk(PackageInfo i) {
		File f = new File(i.applicationInfo.publicSourceDir);
		try {
			String filename = i.packageName.toString();
			Log.d("file_name--", "" + filename);
			File f2;
			String info = Environment.getExternalStorageState();
			if (info.equals(Environment.MEDIA_MOUNTED)) {
				f2 = new File(Environment.getExternalStorageDirectory()
						.toString() + "/My App");
			} else {
				f2 = getCacheDir();
			}
			if (!f2.exists())
				f2.mkdirs();
			f2 = new File(f2.getPath() + "/" + filename + ".apk");
			f2.createNewFile();
			InputStream in = new FileInputStream(f);
			OutputStream out = new FileOutputStream(f2);
			byte[] bf = new byte[1024];
			int len;
			while ((len = in.read(bf)) > 0) {
				out.write(bf, 0, len);
			}
			in.close();
			out.close();
			System.out.println("File Copied");

		} catch (FileNotFoundException ex) {
			System.out
					.println(ex.getMessage() + " in the specified directory.");
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		final MainActivity acti = this;
		AlertDialog.Builder a = new AlertDialog.Builder(MainActivity.this);
		a.setTitle("Exit");
		a.setMessage("Want to Exit?");
		a.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				exit = true;
				acti.finish();
				dialog.cancel();
			}
		});
		a.setNegativeButton("No", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				exit = false;
				dialog.cancel();
			}

		});
		a.show();
	}

	@Override
	public boolean onCreateActionMode(ActionMode actionMode,
			android.view.Menu menu) {
		actionModeEnabled = true;
		actionMode.setTitle("Select Items");
		MenuInflater inflater = actionMode.getMenuInflater();
		inflater.inflate(R.menu.actionmode, menu);
		return true;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode actionMode,
			android.view.Menu menu) {

		return true;
	}

	@Override
	public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {

		switch (menuItem.getItemId()) {
		case R.id.extractmulti: {
			selected = new ArrayList<PackageInfo>();
			getList(selected);
			if (actionMode.getTitle().equals("Select Items")) {
				Thread extract = new Thread(new Runnable() {
					public void run() {
						for (PackageInfo info : selected) {
							extractapk(info);
						}
					};
				});
				extract.start();
				Toast.makeText(getBaseContext(), "Apk generated",
						Toast.LENGTH_SHORT).show();

			}
			break;
		}
		}
		actionMode.finish();
		return true;
	}

	private void getList(List<PackageInfo> selec) {
		// TODO Auto-generated method stub
		for (int i = 0; i < packageList1.size(); i++) {
			if (ApkAdapter.mCheckStates.valueAt(i)) {
				selec.add(packageList1.get(i));
			}
		}
		return;
	}

	@Override
	public void onDestroyActionMode(ActionMode actionMode) {
		actionModeEnabled = false;
		setVisibility(false);
		actionMode.invalidate();
	}

	public void setVisibility(Boolean visible) {
		List<CheckBox> checkboxs = a.getCheckbox();
		if (visible) {
			for (CheckBox a : checkboxs) {
				a.setVisibility(View.VISIBLE);
			}
		} else {
			for (CheckBox a : checkboxs) {
				a.setVisibility(View.INVISIBLE);
			}
		}
	}

	public int checkedcount() {
		int checked = 0;
		for (int i = 0; i < packageList1.size(); i++) {
			if (ApkAdapter.mCheckStates.valueAt(i)) {
				checked++;
			}
		}
		return checked;
	}

	@Override
	public void onItemCheckedStateChanged(ActionMode actionmode, int position,
			long id, boolean checked) {
		// TODO Auto-generated method stub
		pack = (GroupPackage) a.getGroupPackage();
		if (apkList.getCheckedItemCount() == 1) {
			actionmode.setSubtitle("0 item selected");
			int index = apkList.getFlatListPosition(ExpandableListView
					.getPackedPositionForGroup(position));
			@SuppressWarnings("unused")
			int location = pack.packagelist.indexOf((PackageInfo) apkList
					.getItemAtPosition(index));
			setVisibility(true);
			action = actionmode;
		} else {
			actionmode.setSubtitle(checkedcount() + " items selected");
		}

	}

	@Override
	public boolean onGroupClick(ExpandableListView parent, View v,
			int groupPosition, long id) {
		// TODO Auto-generated method stub
		if (actionModeEnabled) {

		}
		return actionModeEnabled;
	}
}