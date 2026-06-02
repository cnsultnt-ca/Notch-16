package com.lorenzomoscati.np.Service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ImageView;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.lorenzomoscati.np.Activity.TabbedActivity;
import com.lorenzomoscati.np.Interface.OnBatteryLevelChanged;
import com.lorenzomoscati.np.Interface.OnRotate;
import com.lorenzomoscati.np.Modal.ColorLevel;
import com.lorenzomoscati.np.R;
import com.lorenzomoscati.np.Receiver.BatteryBroadcastReceiver;
import com.lorenzomoscati.np.Receiver.OrientationBroadcastReceiver;
import com.lorenzomoscati.np.Utils.BatteryConfigManager;
import com.lorenzomoscati.np.Utils.SettingsManager;
import com.lorenzomoscati.np.Utils.NotchManager;

import java.util.Objects;

// The Service class to show and handle the overlay
public class OverlayAccessibilityService extends AccessibilityService implements SharedPreferences.OnSharedPreferenceChangeListener {
	
	private SharedPreferences preferences;
	private SharedPreferences notchPreferences;
	private SharedPreferences batteryConfigPreferences;
	private SharedPreferences settingsPreferences;
	
	private WindowManager windowManager;
	private View overlayView;

	private NotchManager notchManager;
	private SettingsManager settingsManager;
	private BatteryConfigManager batteryManager;
	
	private int batteryLevel;
	private int currentRotation = Surface.ROTATION_0;
	
	private final Handler handler = new Handler();
	
	private Context mContext;
	
	private NotificationManagerCompat notificationManager;
	private static final int notificationID = 1;
	
	private OrientationBroadcastReceiver receiverOrientation;
	private BatteryBroadcastReceiver receiverBattery;
	private SharedPreferences.OnSharedPreferenceChangeListener listenerNotchPreferences;
	private SharedPreferences.OnSharedPreferenceChangeListener listenerBatteryConfigPreferences;
	private SharedPreferences.OnSharedPreferenceChangeListener listenerSettingsPreferences;
	private SharedPreferences.OnSharedPreferenceChangeListener listenerPreferences;
	private boolean receiversRegistered = false;
	
	
	// Executed when service started
	@Override
	protected void onServiceConnected() {


		//Configure these here for compatibility with API 13 and below.
		AccessibilityServiceInfo config = new AccessibilityServiceInfo();
		config.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
		config.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
		config.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;

		setServiceInfo(config);

		mContext = this;
		init();
		Log.d("NP_Debug", "onServiceConnected: service_status=" + preferences.getBoolean("service_status", false));

		if (!receiversRegistered) {
			startReceivers();
			receiversRegistered = true;
		}

		makeNotification();

		if (preferences.getBoolean("service_status", false)) {
			updateOverlay(this);
		}

		final SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean("service_started", true);
		editor.apply();
	}



	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		mContext = this;
		init();
		Log.d("NP_Debug", "onStartCommand called");
		if (!receiversRegistered) {
			startReceivers();
			receiversRegistered = true;
		}

		makeNotification();

		if (preferences.getBoolean("service_status", false)) {
			updateOverlay(this);
		}

		final SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean("service_started", true);
		editor.apply();

		return START_STICKY;
	}
	
	private void makeNotification() {
		
		Intent notifyIntent = new Intent(this, TabbedActivity.class);
		notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notifyIntent, PendingIntent.FLAG_IMMUTABLE);
		
		String CHANNEL_ID = "notchPie_ID";
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			CharSequence name = "notchPie_ID";
			String description = "Standard Notch Pie Channel to post notification";
			int importance = NotificationManager.IMPORTANCE_LOW;
			NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
			channel.setDescription(description);
			NotificationManager notificationManager = getSystemService(NotificationManager.class);
			Objects.requireNonNull(notificationManager).createNotificationChannel(channel);
		}
		
		NotificationCompat.Builder notify = new NotificationCompat.Builder(this, CHANNEL_ID)
				.setSmallIcon(R.drawable.ic_launcher_foreground)
				.setContentTitle("Notch Pie")
				.setContentText("Notch Pie is showing an overlay")
				.setPriority(NotificationCompat.PRIORITY_LOW)
				.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
				.setContentIntent(pendingIntent)
				.setOngoing(true);
		
		notificationManager = NotificationManagerCompat.from(this);
		notificationManager.notify(notificationID, notify.build());
		
	}
	
	@Override
	public void onDestroy() {
		
		super.onDestroy();
	
	}
	
	@Override
	public void onTaskRemoved(Intent rootIntent) {
		
		super.onTaskRemoved(rootIntent);
	
	}
	
	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
	
	}

	// When the service gets disabled, the receiver are also disabled
	@Override
	public void onInterrupt() {
		
		stopReceivers();

	}


	// -- Handlers --
	@SuppressLint("InflateParams")
	private void init() {
		
		// Getting the window manager
		if (windowManager == null) {
			windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
		}
		
		// The parent image view in which the bitmap is set
		if (overlayView == null) {
			overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_float, null);
			// Properly rotates the overlay
			overlayView.setRotationY(180);
		}
		
		// Sets the managers to read notch, color and settings
		if (notchManager == null) notchManager = new NotchManager(getApplicationContext());
		if (settingsManager == null) settingsManager = new SettingsManager(getApplicationContext());
		if (batteryManager == null) batteryManager = new BatteryConfigManager(getApplicationContext());
		
		initPref(mContext != null ? mContext : this);

		if (batteryLevel <= 0) {
			IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
			Intent batteryStatus = registerReceiver(null, ifilter);
			if (batteryStatus != null) {
				int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
				int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
				batteryLevel = (int) (level * 100 / (float) scale);
			}
		}
	}
	
	@SuppressLint("InflateParams")
	private void initPref(Context context) {
		
		notchPreferences = context.getSharedPreferences("notch_preferences", 0);
		batteryConfigPreferences = context.getSharedPreferences("battery_config_preferences", 0);
		settingsPreferences = context.getSharedPreferences("settings_preferences", 0);
		preferences = context.getSharedPreferences("preferences", 0);
		
	}

	// -- Utils --
	private int getStatusBarHeight() {
		
		Resources resources = this.getResources();
		
		int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
		
		if (resourceId > 0) {
			
			return resources.getDimensionPixelSize(resourceId);
			
		}
		
		return 0;
		
	}
	
	
	
	// This method starts and initialises every receiver to be uses in service
	private void startReceivers() {
		
		// Orientation receiver
		receiverOrientation = new OrientationBroadcastReceiver(new OnRotate() {
			
			@Override
			public void onPortrait() {
				
				// When in portrait mode, the rotation is set and the notch is redrawn
				currentRotation = Surface.ROTATION_0;
				//updateOverlay(getApplicationContext());
				updateOverlay(getApplicationContext());
				
			}

			@Override
			public void onLandscape() {
				
				// When in landscape mode, the rotation is set and the notch is redrawn
				currentRotation = windowManager.getDefaultDisplay().getRotation();
				//updateOverlay(getApplicationContext());
				updateOverlay(getApplicationContext());
				
			}
			
		});
		
		IntentFilter intentFilterOrientation = new IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED);
		
		
		
		listenerNotchPreferences = new SharedPreferences.OnSharedPreferenceChangeListener() {
			
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
				Log.d("NP_Debug", "notchPreferences changed: " + key);
				updateOverlay(getApplicationContext());
				
			}
		};
		
		
		
		listenerBatteryConfigPreferences = new SharedPreferences.OnSharedPreferenceChangeListener() {
			
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
				
				updateOverlay(getApplicationContext());
				
			}
			
		};
		
		
		
		listenerSettingsPreferences = new SharedPreferences.OnSharedPreferenceChangeListener() {
			
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
				
				updateOverlay(getApplicationContext());
				
			}
			
		};
		
		
		
		listenerPreferences = new SharedPreferences.OnSharedPreferenceChangeListener() {
			
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
				
				Log.d("Detected", "Preference change detected, filtering...");
				
				if (Objects.equals(key, "service_status")) {
					
					Log.d("Filtered", "The change interests service_status...");
					
					if (!preferences.getBoolean(key, false)) {
						
						Log.d("Filtered", "service_status is false, removing...");
						
						removeOverlay();
						
					}
					
					else if (preferences.getBoolean(key, false)) {
						
						Log.d("Filtered", "service_status is true, filtering again");
						
						if (preferences.getBoolean("service_started", false)) {
							
							Log.d("Filtered", "service_started is true, updating...");
							
							makeNotification();
							
							init();
							
							updateOverlay(getApplicationContext());
							
						}
						
					}
					
				}
				
			}
			
		};
		
		
		// Battery receiver
		receiverBattery = new BatteryBroadcastReceiver(new OnBatteryLevelChanged() {
			
			@Override
			public void onChanged(int battery) {
				
				// When the battery level changes, the notch is redrawn according to the new percentage
				batteryLevel = battery;
				//updateOverlay(getApplicationContext());
				updateOverlay(getApplicationContext());
				
			}

			@Override
			public void onChargingConnected() {

			}

			@Override
			public void oncChargingDisconnected(int battery) {
				
				// When the battery is not charging anymore, the charging animation stops
				isAnimationActive = false;
				batteryLevel = battery;
				//updateOverlay(getApplicationContext());
				updateOverlay(getApplicationContext());
				
			}
			
		});
		
		IntentFilter intentFilterBattery = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);

		// Receiver registered
		if (!receiversRegistered) {
			registerReceiver(receiverOrientation, intentFilterOrientation);
			notchPreferences.registerOnSharedPreferenceChangeListener(listenerNotchPreferences);
			batteryConfigPreferences.registerOnSharedPreferenceChangeListener(listenerBatteryConfigPreferences);
			settingsPreferences.registerOnSharedPreferenceChangeListener(listenerSettingsPreferences);
			preferences.registerOnSharedPreferenceChangeListener(listenerPreferences);
			registerReceiver(receiverBattery, intentFilterBattery);
			receiversRegistered = true;
		}
		
	}

	private void updateOverlay(Context context) {
		Log.d("NP_Debug", "updateOverlay called, service_status=" + preferences.getBoolean("service_status", false));
		Log.d("NP_Debug", "notch w=" + notchManager.getWidth() + " h=" + notchManager.getHeight());

		if (preferences.getBoolean("service_status", false) && batteryLevel > 0) {
			makeOverlay(batteryLevel);
		}
	}


	private void stopReceivers() {
		
		// When this is called, the receiver are unregistered
		if (receiverOrientation != null) {
			unregisterReceiver(receiverOrientation);
		}
		
		if (receiverBattery != null) {
			unregisterReceiver(receiverBattery);
		}
		
		if (preferences != null) preferences.unregisterOnSharedPreferenceChangeListener(listenerPreferences);
		if (notchPreferences != null) notchPreferences.unregisterOnSharedPreferenceChangeListener(listenerNotchPreferences);
		if (settingsPreferences != null) settingsPreferences.unregisterOnSharedPreferenceChangeListener(listenerSettingsPreferences);
		if (batteryConfigPreferences != null) batteryConfigPreferences.unregisterOnSharedPreferenceChangeListener(listenerBatteryConfigPreferences);

		receiversRegistered = false;
	}

	// -- Overlay Manager --
	private void makeOverlay(int battery) {
		
		Log.d("Called", "makeOverlay has been called");
		
		// Check if there is support for landscape, and according to this, the notch is drawn
		if (settingsManager.isLandscapeSupport()) {

			if (currentRotation == Surface.ROTATION_0) {

				makeOverlayPortrait(battery);

			}

			else if (currentRotation == Surface.ROTATION_90) {

				makeOverlayLandscape(battery);

			}

			else if (currentRotation == Surface.ROTATION_270) {

				makeOverlayLandscapeReverse(battery);

			}

			else {
				
				Log.d("Called", "else has been called");
				
				removeOverlay();

			}

		}

		else {

			if (currentRotation == Surface.ROTATION_0) {

				makeOverlayPortrait(battery);

			}

			else if (currentRotation == Surface.ROTATION_90) {

				removeOverlay();

			}

			else if (currentRotation == Surface.ROTATION_270) {

				removeOverlay();

			}

			else {
				
				Log.d("Called", "else has been called");
				
				removeOverlay();

			}

		}

	}

	// Makes the notch for a portrait view
	private void makeOverlayPortrait(int battery) {
		Log.d("NP_Debug", "makeOverlayPortrait called, battery=" + battery);

		Bitmap bitmap = drawNotch(battery);
		Log.d("NP_Debug", "bitmap w=" + bitmap.getWidth() + " h=" + bitmap.getHeight());

		ImageView img = overlayView.findViewById(R.id.imageView);
		img.setImageBitmap(bitmap);
		img.setRotation(0);

		overlayView.setRotation(0);
		overlayView.setRotationY(180);
		overlayView.setRotationX(0);
		overlayView.setVisibility(View.VISIBLE);

		try {
			windowManager.updateViewLayout(overlayView, generateParamsPortrait(bitmap.getHeight(), bitmap.getWidth()));
		} catch (Exception e) {
			try {
				windowManager.addView(overlayView, generateParamsPortrait(bitmap.getHeight(), bitmap.getWidth()));
				Log.d("NP_Debug", "addView SUCCESS");
			} catch (Exception e2) {
				Log.d("NP_Debug", "addView failed: " + e2.getMessage());
			}
		}
	}

	// Makes the notch for a landscape view
	private void makeOverlayLandscape(int battery) {
		Bitmap bitmap = drawNotch(battery);
		bitmap = rotateBitmap(bitmap, 90f);

		ImageView img = overlayView.findViewById(R.id.imageView);
		img.setImageBitmap(bitmap);
		img.setRotation(0);

		overlayView.setRotation(0);
		overlayView.setRotationY(180);
		overlayView.setRotationX(0);
		overlayView.setVisibility(View.VISIBLE);

		try {
			windowManager.updateViewLayout(overlayView, generateParamsLandscape(bitmap.getHeight(), bitmap.getWidth()));
		} catch (Exception e) {
			try {
				windowManager.addView(overlayView, generateParamsLandscape(bitmap.getHeight(), bitmap.getWidth()));
			} catch (Exception e2) {
				Log.d("NP_Debug", "addView landscape failed: " + e2.getMessage());
			}
		}
	}

	// Makes the notch for a landscape reverse view
	private void makeOverlayLandscapeReverse(int battery) {
		Bitmap bitmap = drawNotch(battery);
		bitmap = rotateBitmap(bitmap, -90f);

		ImageView img = overlayView.findViewById(R.id.imageView);
		img.setImageBitmap(bitmap);
		img.setRotation(0);

		overlayView.setRotation(0);
		overlayView.setRotationY(180);
		overlayView.setRotationX(0);
		overlayView.setVisibility(View.VISIBLE);

		try {
			windowManager.updateViewLayout(overlayView, generateParamsPortraitLandscapeReverse(bitmap.getHeight(), bitmap.getWidth()));
		} catch (Exception e) {
			try {
				windowManager.addView(overlayView, generateParamsPortraitLandscapeReverse(bitmap.getHeight(), bitmap.getWidth()));
			} catch (Exception e2) {
				Log.d("NP_Debug", "addView landscape reverse failed: " + e2.getMessage());
			}
		}
	}
	// This method removes the overlay from the view
	private void removeOverlay() {
		
		Log.d("Called", "removeOverlay has been called");
		
		if (windowManager == null || overlayView == null) {
			return;
		}
		
		try {
			windowManager.removeView(overlayView);
		} catch (Exception e) {
			Log.e("NP_Debug", "removeView failed: " + e.getMessage());
		}
		
		if (notificationManager != null) {
			notificationManager.cancel(notificationID);
		}

	}

	// -- Layout Parameter Generator --
	private WindowManager.LayoutParams generateParamsPortrait(int h, int w) {

		int layoutParameters = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
				WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
				WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
				WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;

		WindowManager.LayoutParams p = new WindowManager.LayoutParams(
				w,
				h,
				WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
				layoutParameters,
				PixelFormat.TRANSLUCENT);

		p.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;

		//setting boundary

		p.x = notchManager.getxPositionPortrait();
		p.y = 0;

		Log.d("NP_Debug", "x=" + p.x + " y=" + p.y + " w=" + w + " h=" + h + " gravity=" + p.gravity);
		return p;

	}

	private WindowManager.LayoutParams generateParamsLandscape(int h, int w) {

		int layoutParameters = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
				WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
				WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
				WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

		WindowManager.LayoutParams p = new WindowManager.LayoutParams(
				w,
				h,
				WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
				layoutParameters,
				PixelFormat.TRANSLUCENT);

		p.gravity = Gravity.START | Gravity.CENTER;

		//setting boundary
		p.x = notchManager.getxPositionLandscape();
		p.y = notchManager.getyPositionLandscape();



		return p;

	}

	private WindowManager.LayoutParams generateParamsPortraitLandscapeReverse(int h, int w) {

		int layoutParameters = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
				WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
				WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
				WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

		WindowManager.LayoutParams p = new WindowManager.LayoutParams(
				w,
				h,
				WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
				layoutParameters,
				PixelFormat.TRANSLUCENT);

		p.gravity = Gravity.END | Gravity.CENTER;

		//setting boundary
		p.x = notchManager.getxPositionLandscape();
		p.y = notchManager.getyPositionLandscape();



		return p;

	}


	// -- Notch Bitmap Generator --
	// this method draws the notch shape
	// please don't ask me how it works now
	// i kinda forgot myself
	// it just works so i am not commenting out anything in it.
	private Bitmap drawNotch(int battery) {
		int w = notchManager.getWidth();
		int h = notchManager.getHeight();
		int ns = notchManager.getNotchSize();
		int tr = notchManager.getTopRadius();
		int br = notchManager.getBottomRadius();

		float p1 = 0;
		float p2 = (float) (w * 2);
		float p3 = (float) (((w + ns) * 2) + 2);
		float p4 = p3 - (p1 + p1);

		Path path = new Path();
		path.moveTo(p3 - 1.0f, -1.0f + p1);
		float p5 = -p1;
		path.rMoveTo(p5, p5);
		float p8 = ((tr / 100.0f) * (float) ns);// top radius
		float p9 = 0.0f;
		float p10 = ((br / 100.0f) * (float) ns);
		p4 = -((p4 - (((float) ns * 2.0f) + p2)) / 2.0f);
		path.rMoveTo(p4, 0.0F);
		float p11 = -p9;
		p3 = - (float) ns;
		path.rCubicTo(-p8, p9, p10 - (float) ns, p11 + (float) h, p3, (float) h);
		path.rLineTo(-p2, 0.0f);
		path.rCubicTo(-p10, p11, p8 - (float) ns, p9 - (float) h, p3, - (float) h);

		path.close();

		RectF rectF = new RectF();
		path.computeBounds(rectF, true);
		rectF.height();

		Bitmap bitmap = Bitmap.createBitmap(
				//screenWidth,
				(int) Math.abs(rectF.width()),
				(int) Math.abs(rectF.height()) + 10,// , // Height
				Bitmap.Config.ARGB_8888 // Config
		);

		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(Color.RED);
		paint.setAntiAlias(true);
		paint.setDither(true);
		paint.setStyle(Paint.Style.FILL);

		float[] positions = getPositionArray();
		int[] colors;

		if (batteryManager.isLinear()) {
			colors = getColorArrayLinear(battery, settingsManager.isFullStatus());
		} else {
			colors = getColorArrayDefined(battery, settingsManager.isFullStatus());
		}

		SweepGradient sweepGradient = new SweepGradient((int) (Math.abs(rectF.width()) / 2) /*center*/, 0, colors, positions);
		paint.setShader(sweepGradient);

		Canvas canvas = new Canvas(bitmap);
		canvas.drawPath(path, paint);


		return bitmap;
	}

	// -- Array Generators --
	// Array Generators //
	// this method generates the color palette depending upon the config
	private int[] getColorArrayDefined(int battery, boolean isFullStatus) {
		int[] c = new int[181];
		if (isFullStatus) { // fill the overlay with one color
			for (ColorLevel item : batteryManager.getColorLevels()) {
				if (battery >= item.getStartLevel() && battery <= item.getEndLevel()) {
					for (int i = 0; i < 181; i++) {
						c[i] = Color.parseColor(item.getColor());
					}
					break;
				}
			}
		} else { // need partially filler overlay
			for (int i = 0; i < 181; i++)
				c[i] = (settingsManager.isShowBackground()) ? Color.parseColor(settingsManager.getBackgroundColor()) : Color.TRANSPARENT;
			for (ColorLevel item : batteryManager.getColorLevels()) {
				if (battery >= item.getStartLevel() && battery <= item.getEndLevel()) {
					int val = (int) ((battery / 100.0) * 180.0);
					for (int i = 0; i < val; i++) {
						c[i] = Color.parseColor(item.getColor());
					}
					break;
				}
			}
		}
		return c;
	}

	private int[] getColorArrayLinear(int battery, boolean isFullStatus) {
		
		int[] c = new int[181];
		
		String color1 = batteryManager.getLinearStart();
		String color2 = batteryManager.getLinearEnd();
		
		float red1 = (float) Integer.parseInt(color1.substring(1, 3), 16) / 255;
		float green1 = (float) Integer.parseInt(color1.substring(3, 5), 16) / 255;
		float blue1 = (float) Integer.parseInt(color1.substring(5), 16) / 255;
		
		float MAX1 = Math.max(Math.max(red1, green1), blue1);
		float MIN1 = Math.min(Math.min(red1, green1), blue1);
		
		float H1;
		float S1;
		float V1;
		
		if (MAX1 == MIN1) {
		
			H1 = 0;
		
		}
		
		else if (MAX1 == red1) {
			
			H1 = 60 * ((green1 - blue1) / (MAX1 - MIN1));
			
		}
		
		else if (MAX1 == green1) {
		
			H1 = 60 * (2 + ((blue1 - red1) / (MAX1 - MIN1)));
		
		}
		
		else {
			
			H1 = 60 * (2 + ((red1 - green1) / (MAX1 - MIN1)));
			
		}
		
		if (H1 < 0) {
			
			H1 = H1 + 360;
			
		}
		
		
		if (MAX1 == 0) {
			
			S1 = 0;
			
		}
		
		else {
		
			S1 = (MAX1 - MIN1) / MAX1;
		
		}
		
		V1 = MAX1;
		
		
		
		float red2 = (float) Integer.parseInt(color2.substring(1, 3), 16) / 255;
		float green2 = (float) Integer.parseInt(color2.substring(3, 5), 16) / 255;
		float blue2 = (float )Integer.parseInt(color2.substring(5), 16) / 255;
		
		float MAX2 = Math.max(Math.max(red2, green2), blue2);
		float MIN2 = Math.min(Math.min(red2, green2), blue2);
		
		float H2;
		float S2;
		float V2;
		
		if (MAX2 == MIN2) {
			
			H2 = 0;
			
		}
		
		else if (MAX2 == red2) {
			
			H2 = 60 * ((green2 - blue2) / (MAX2 - MIN2));
			
		}
		
		else if (MAX2 == green2) {
			
			H2 = 60 * (2 + ((blue2 - red2) / (MAX2 - MIN2)));
			
		}
		
		else {
			
			H2 = 60 * (2 + ((red2 - green2) / (MAX2 - MIN2)));
			
		}
		
		if (H2 < 0) {
			
			H2 = H2 + 360;
			
		}
		
		
		if (MAX2 == 0) {
			
			S2 = 0;
			
		}
		
		else {
			
			S2 = (MAX2 - MIN2) / MAX2;
			
		}
		
		V2 = MAX2;
		
		
		float hue = H1 * ((float) battery / 100) + H2 * (1 - ((float) battery / 100));
		float saturation = S1 * ((float) battery / 100) + S2 * (1 - ((float) battery / 100));
		float value = V1 * ((float) battery / 100) + V2 * (1 - ((float) battery / 100));
		
		
		
		float red = 255;
		float green = 255;
		float blue = 255;
		
		float chroma = saturation * value;
		float x = chroma * (1 - Math.abs((hue / 60) % 2 - 1));
		float m = value - chroma;
		
		if (hue >= 0 && hue < 60) {
		
			red = chroma;
			green = x;
			blue = 0;
		
		}
		
		else if (hue >= 60 && hue < 120) {
		
			red = x;
			green = chroma;
			blue = 0;
		
		}
		
		else if (hue >= 120 && hue < 180) {
		
			red = 0;
			green = chroma;
			blue = x;
		
		}
		
		else if (hue >= 180 && hue < 240) {
		
			red = 0;
			green = x;
			blue = chroma;
		
		}
		
		else if (hue >= 240 && hue < 300) {
		
			red = x;
			green = 0;
			blue = chroma;
		
		}
		
		else if (hue >= 300 && hue < 360) {
		
			red = chroma;
			green = 0;
			blue = x;
		
		}
		
		red = (red + m) * 255;
		green = (green + m) * 255;
		blue = (blue + m) * 255;
		
		

		Log.e("RED", red + " " + green + blue);

		String r = Integer.toString(Math.round(red), 16);
		if (r.length() == 1)
			r = "0" + r;
		
		String g = Integer.toString(Math.round(green), 16);
		if (g.length() == 1)
			g = "0" + g;
		
		String b = Integer.toString(Math.round(blue), 16);
		if (b.length() == 1)
			b = "0" + b;
		
		String color = "#" + r + "" + g + "" + b;
		
		if (isFullStatus) {
			
			// Fill the overlay with one color
			for (int i = 0; i < 181; i++) {
				
				Log.e("color", color);
				c[i] = Color.parseColor(color);
				
			}

		} else {
			
			// First of all the array is filled with transparent color or background color
			for (int i = 0; i < 181; i++) {
				
				c[i] = (settingsManager.isShowBackground()) ? Color.parseColor(settingsManager.getBackgroundColor()) : Color.TRANSPARENT;
				
			}
			
			// Then it's colored again to show the right battery level
			for (ColorLevel item : batteryManager.getColorLevels()) {
				
				if (battery >= item.getStartLevel() && battery <= item.getEndLevel()) {
					
					int val = (int) ((battery / 100.0) * 180.0);
					
					for (int i = 0; i < val; i++) {
						
						Log.e("color", color);
						c[i] = Color.parseColor(color);
						
					}
					
					break;
					
				}
				
			}
			
		}
		
		return c;
		
	}

	// this method generates an evenly spread position point array
	private float[] getPositionArray() {
		float a = 0.5F / 180;
		float c = 0;
		float[] p = new float[181];
		for (int i = 0; i < 180; i++) {
			p[i] = c;
			c += a;
		}
		p[180] = 0.5F;
		return p;
	}

	private static Bitmap rotateBitmap(Bitmap source, float angle) {
		Matrix matrix = new Matrix();
		matrix.postRotate(angle);
		return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
	}


	// Animation methods
	private boolean isAnimationActive = false;
	private int tempBatteryLevel = batteryLevel;

	private void animation() {
	
		handler.postDelayed(new Runnable() {

			@Override
			public void run() {

				if (tempBatteryLevel == 100) {

					tempBatteryLevel = batteryLevel;

				}

				else {

					tempBatteryLevel++;

				}

				makeOverlay(tempBatteryLevel);

				if (isAnimationActive) {

					animation();

				}

			}

		}, 100);
		
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
	
	
	
	}
	
}
