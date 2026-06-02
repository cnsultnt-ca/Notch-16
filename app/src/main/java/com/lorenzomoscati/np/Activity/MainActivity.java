package com.lorenzomoscati.np.Activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import com.lorenzomoscati.np.R;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		checkPermissions();

	}

	// This method checks for all permissions
	private void checkPermissions() {
		
		if (!canDrawOverlay()) {

			notifyOverlayPermission();

		} else {

			// Starts the tabbed activity, the main one in the application
			startActivity(new Intent(this, TabbedActivity.class));
			
			// Terminates the activity
			finish();

		}

	}
	
	
	
	// Draw over the app permission things

	// This method returns true if the overlay permission is granted
	private boolean canDrawOverlay() {

		return Settings.canDrawOverlays(this);

	}

	// This method shows a dialog notifying the user about overlay permission
	private void notifyOverlayPermission() {
		
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setPositiveButton(getString(R.string.popup_positive), (dialog, which) -> requestOverlayPermission());

		builder.setNegativeButton(getString(R.string.popup_negative), (dialog, which) -> finish());

		builder.setCancelable(false);

		builder.setTitle(getString(R.string.alert_overlay_title));

		builder.setMessage(getString(R.string.alert_overlay_description));

		builder.show();

	}

	// This method requests overlay permission
	private void requestOverlayPermission() {

		Toast.makeText(getApplicationContext(), getString(R.string.alert_overlay_request), Toast.LENGTH_LONG).show();

		Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
		startActivity(intent);

	}




	// This method is called when the app comes to foreground
	@Override
	protected void onResume() {

		super.onResume();

		checkPermissions();

	}
	
}
