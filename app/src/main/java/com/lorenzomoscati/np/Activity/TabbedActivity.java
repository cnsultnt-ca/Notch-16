package com.lorenzomoscati.np.Activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.lorenzomoscati.np.Adapter.SectionsPageAdapter;
import com.lorenzomoscati.np.R;
import com.lorenzomoscati.np.Service.OverlayAccessibilityService;

import java.util.List;
import java.util.Objects;

public class TabbedActivity extends AppCompatActivity {
	
	private ViewPager viewPager;
	private Context mContext;
	private SharedPreferences.OnSharedPreferenceChangeListener listener;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_tabbed);
		
		mContext = this;
		
		final SharedPreferences preferences = getApplicationContext().getSharedPreferences("preferences", 0);
		
		init();

		makeServiceSnackBar();
		
		FloatingActionButton fab = findViewById(R.id.floatingActionButton);

		updateFabColor(fab, preferences.getBoolean("service_status", false));

		listener = (sharedPreferences, key) -> {
			if (Objects.equals(key, "service_status")) {
				updateFabColor(fab, sharedPreferences.getBoolean(key, false));
			}
		};
		preferences.registerOnSharedPreferenceChangeListener(listener);
		
		fab.setOnClickListener(v -> {
			boolean currentStatus = preferences.getBoolean("service_status", false);
			Log.d("NP_Debug", "FAB clicked, checkServiceOn=" + checkServiceOn() + " currentStatus=" + currentStatus);

			if (checkServiceOn()) {
				SharedPreferences.Editor editor = preferences.edit();
				editor.putBoolean("service_status", !currentStatus);
				editor.commit();

				if (!currentStatus) { // We are turning it ON
					Intent serviceIntent = new Intent(getApplicationContext(), OverlayAccessibilityService.class);
					startService(serviceIntent);
				}
			}
			else {
				// Creates the intent to the settings page
				Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
				startActivity(intent);
				
				// Makes a toast to notify the user about what setting to turn on
				Toast.makeText(getApplicationContext(), getString(R.string.popup_accessibility_toast), Toast.LENGTH_LONG).show();
			}
		});

	}

	private void init() {
		
		final String[] titles = {getString(R.string.title_notch), getString(R.string.title_config), getString(R.string.title_battery), getString(R.string.title_about)};
		
		TabLayout tabLayout = findViewById(R.id.tabs);
		viewPager = findViewById(R.id.view_pager);
		viewPager.setAdapter(new SectionsPageAdapter(getSupportFragmentManager(), titles));
		tabLayout.setupWithViewPager(viewPager);

	}
	
	// Checks if the accessibility service is turned on
	private void makeServiceSnackBar() {
		
		// If Doze is on for the app a snackBar is shown
		if (checkDozeOn()) {
			
			// Sets the title and length (infinite)
			Snackbar bar = Snackbar.make(viewPager, getString(R.string.battery_optimization), Snackbar.LENGTH_INDEFINITE);
			
			// Sets the button to disable it
			bar.setAction(getString(R.string.disable), v -> {
				
				// Creates the intent to the settings page
				Intent intent = new Intent();
				intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
				intent.setData(Uri.parse("package:" + getPackageName()));
				startActivity(intent);
				
			});
			
			// Shows the bar
			bar.show();
			
		}
		
		// If the app is running on MIUI a snackBar is shown
		else if (checkMIUI(getApplicationContext())) {
			
			SharedPreferences preferences = getApplicationContext().getSharedPreferences("preferences", 0);
			
			final SharedPreferences.Editor editor = preferences.edit();
			
			if (!preferences.getBoolean("already_appeared_miui", false)) {
				
				// Sets the title and length (infinite)
				Snackbar bar = Snackbar.make(viewPager, getString(R.string.miui_note), Snackbar.LENGTH_INDEFINITE);
				
				bar.setAction(getString(R.string.more_info), v -> {
					
					new AlertDialog.Builder(mContext)
							.setTitle(getString(R.string.overlay_disappearing))
							.setMessage(getString(R.string.overlay_disappearing_desc))
							.setPositiveButton(getString(R.string.understood), (dialog, which) -> {
								
								editor.putBoolean("already_appeared_miui", true);
								editor.apply();
								
							})
							.show();
					
				});
				
				// Shows the bar
				bar.show();
				
			}
		
		}

	}

	@Override
	protected void onResume() {

		super.onResume();

		// Checks again if the accessibility service is enabled
		makeServiceSnackBar();

	}

	// Method that returns true if the accessibility service is turned on
	private boolean checkServiceOn() {

		return isAccessibilityServiceEnabled(getApplicationContext());

	}
	
	private boolean checkDozeOn() {
		
		PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
		return !Objects.requireNonNull(pm).isIgnoringBatteryOptimizations(getPackageName());
	
	}
	
	private boolean isIntentResolved(Context ctx, Intent intent ){
		
		return (intent!=null && ctx.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null);
		
	}
	
	private boolean checkMIUI(Context ctx) {
		
		return isIntentResolved(ctx, new Intent("miui.intent.action.OP_AUTO_START").addCategory(Intent.CATEGORY_DEFAULT))
				|| isIntentResolved(ctx, new Intent().setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")))
				|| isIntentResolved(ctx, new Intent("miui.intent.action.POWER_HIDE_MODE_APP_LIST").addCategory(Intent.CATEGORY_DEFAULT))
				|| isIntentResolved(ctx, new Intent().setComponent(new ComponentName("com.miui.securitycenter", "com.miui.powercenter.PowerSettings")));
		
	}

	// Method that checks the status of the accessibility service
	private static boolean isAccessibilityServiceEnabled(Context context) {

		// Defines an array in which are contained the services turned on
		AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
		List<AccessibilityServiceInfo> enabledServices = Objects.requireNonNull(am).getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);

		// Loops that cycles through the array to check if our service is turned on
		for (AccessibilityServiceInfo enabledService : enabledServices) {

			// Targets the service
			ServiceInfo enabledServiceInfo = enabledService.getResolveInfo().serviceInfo;

			// Checks if the service is our service
			if (Objects.equals(enabledServiceInfo.packageName, context.getPackageName()) && Objects.equals(enabledServiceInfo.name, OverlayAccessibilityService.class.getName())) {

				return true;

			}

		}

		return false;

	}

	private void updateFabColor(FloatingActionButton fab, boolean isOn) {
		int color = isOn ? Color.parseColor("#90EE90") : Color.parseColor("#FFCCCB");
		fab.setBackgroundTintList(ColorStateList.valueOf(color));
	}

}
