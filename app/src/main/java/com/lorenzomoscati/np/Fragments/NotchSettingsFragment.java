package com.lorenzomoscati.np.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.lorenzomoscati.np.Adapter.NotchLayoutAdapter;
import com.lorenzomoscati.np.Interface.NotchStyleTouchListener;
import com.lorenzomoscati.np.Modal.NotchItem;
import com.lorenzomoscati.np.R;
import com.lorenzomoscati.np.Utils.NotchManager;

import java.util.ArrayList;
import java.util.Objects;

public class NotchSettingsFragment extends Fragment {

	// Notch manager object
	private NotchManager manager;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		View main = inflater.inflate(R.layout.fragment_notch_settings, container, false);

		init();

		return attach(main);

	}

	// Initializes the method to call the notch manager's methods
	private void init() {

		manager = new NotchManager(Objects.requireNonNull(getContext()));

	}

	// Takes the parent view and attaches the listeners to it
	private View attach(View main) {

		// Text views
		final TextView txHeight = main.findViewById(R.id.txHeight);
		final TextView txWidth = main.findViewById(R.id.txWidth);
		final TextView txNotchSize = main.findViewById(R.id.txNotchSize);
		final TextView txTopRadius = main.findViewById(R.id.txTopRadius);
		final TextView txBottomRadius = main.findViewById(R.id.txBottomRadius);
		final TextView txXPositionP = main.findViewById(R.id.txXPositionPortrait);
		final TextView txYPositionP = main.findViewById(R.id.txYPositionPortrait);
		final TextView txXPositionL = main.findViewById(R.id.txXPositionLandscape);
		final TextView txYPositionL = main.findViewById(R.id.txYPositionLandscape);

		// Seek bars limits
		int[] heightLimit = {50, 500};
		int[] widthLimit = {1, 500};
		int[] notchSizeLimit = {1, 500};
		int[] notchRadiusTopLimit = {0, 100};
		int[] notchRadiusBottomLimit = {0, 100};
		int[] xPositionLimit = {-200, 200};
		int[] yPositionLimit = {-200, 200};

		// Actual SeekBars
		final SeekBar
				height,
				width,
				notchSize,
				topRadius,
				bottomRadius,
				xPositionP,
				yPositionP,
				xPositionL,
				yPositionL;




		// Height SeekBar
		height = main.findViewById(R.id.sbHeight);

		// Settings limits
		height.setMin(heightLimit[0]);
		height.setMax(heightLimit[1]);

		// Applying listener
		height.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

				// Reads the progress
				txHeight.setText(String.valueOf(progress));
				// Sets the corresponding value
				manager.setHeight(progress, getContext());

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}

		});



		// Width SeekBar
		width = main.findViewById(R.id.sbWidth);

		// Settings limits
		width.setMin(widthLimit[0]);
		width.setMax(widthLimit[1]);

		// Applying listener
		width.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

				// Reads the progress
				txWidth.setText(String.valueOf(progress));
				// Sets the corresponding value
				manager.setWidth(progress, getContext());

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}
		});



		// NotchSize SeekBar
		notchSize = main.findViewById(R.id.sbNotchSize);

		// Setting the limits
		notchSize.setMin(notchSizeLimit[0]);
		notchSize.setMax(notchSizeLimit[1]);

		// Applying the listener
		notchSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

				// Reads the progress
				txNotchSize.setText(String.valueOf(progress));
				// Sets the corresponding value
				manager.setNotchSize(progress, getContext());

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}

		});



		// TopRadius SeekBar
		topRadius = main.findViewById(R.id.sbTopRadius);

		// Setting the limits
		topRadius.setMin(notchRadiusTopLimit[0]);
		topRadius.setMax(notchRadiusTopLimit[1]);

		// Applying the listener
		topRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

				// Reads the progress
				txTopRadius.setText(String.valueOf(progress));
				// Sets the corresponding value
				manager.setTopRadius(progress, getContext());

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}

		});



		// BottomRadius SeekBar
		bottomRadius = main.findViewById(R.id.sbBottomRadius);

		// Setting the limits
		bottomRadius.setMin(notchRadiusBottomLimit[0]);
		bottomRadius.setMax(notchRadiusBottomLimit[1]);

		// Applying the listener
		bottomRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

				// Reads the progress
				txBottomRadius.setText(String.valueOf(progress));
				// Sets the corresponding value
				manager.setBottomRadius(progress, getContext());

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}

		});



		// xPosition on portrait mode SeekBar
		xPositionP = main.findViewById(R.id.sbXPositionPortrait);

		// Setting the limits
		xPositionP.setMin(xPositionLimit[0]);
		xPositionP.setMax(xPositionLimit[1]);

		// Applying the listener
		xPositionP.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

				// Reads the progress
				txXPositionP.setText(String.valueOf(progress));
				// Sets the corresponding value
				manager.setxPositionPortrait(progress, getContext());

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}

		});



		// yPosition in portrait mode SeekBar
		yPositionP = main.findViewById(R.id.sbYPositionPortrait);

		// Setting the limits
		yPositionP.setMin(yPositionLimit[0]);
		yPositionP.setMax(yPositionLimit[1]);

		// Applying the listener
		yPositionP.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

				// Reads the progress
				txYPositionP.setText(String.valueOf(progress));
				// Sets the corresponding value
				manager.setyPositionPortrait(progress, getContext());

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}

		});



		// xPosition in landscape mode SeekBar
		xPositionL = main.findViewById(R.id.sbXPositionLandscape);

		// Setting the limits
		xPositionL.setMin(xPositionLimit[0]);
		xPositionL.setMax(xPositionLimit[1]);

		// Applying the listener
		xPositionL.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

				// Reads the progress
				txXPositionL.setText(String.valueOf(progress));
				// Sets the corresponding value
				manager.setxPositionLandscape(progress, getContext());

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}

		});



		// yPosition in landscape mode SeekBar
		yPositionL = main.findViewById(R.id.sbYPositionLandscape);

		// Setting the limits
		yPositionL.setMin(yPositionLimit[0]);
		yPositionL.setMax(yPositionLimit[1]);

		// Applying the listener
		yPositionL.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

				// Reads the progress
				txYPositionL.setText(String.valueOf(progress));
				// Sets the corresponding value
				manager.setyPositionLandscape(progress, getContext());

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}

		});





		// Floating Action Buttons

		// Increases height by one
		FloatingActionButton heightIncrease = main.findViewById(R.id.fabHeightIncrease);
		heightIncrease.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				height.setProgress(height.getProgress() + 1);

			}

		});

		// Decreases height by one
		FloatingActionButton heightDecrease = main.findViewById(R.id.fabHeightDecrease);
		heightDecrease.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				height.setProgress(height.getProgress() - 1);

			}

		});

		// Increases width by one
		FloatingActionButton widthIncrease = main.findViewById(R.id.fabWidthIncrease);
		widthIncrease.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				width.setProgress(width.getProgress() + 1);

			}

		});

		// Decreases width by one
		FloatingActionButton widthDecrease= main.findViewById(R.id.fabWidthDecrease);
		widthDecrease.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				width.setProgress(width.getProgress() - 1);

			}

		});

		// Increases notchSize by one
		FloatingActionButton notchSizeIncrease = main.findViewById(R.id.fabNotchSizeIncrease);
		notchSizeIncrease.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				notchSize.setProgress(notchSize.getProgress() + 1);

			}

		});

		// Decreases notchSize by one
		FloatingActionButton notchSizeDecrease = main.findViewById(R.id.fabNotchSizeDecrease);
		notchSizeDecrease.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				notchSize.setProgress(notchSize.getProgress() - 1);

			}

		});

		// Increases topRadius by one
		FloatingActionButton topRadiusIncrease = main.findViewById(R.id.fabTopRadiusIncrease);
		topRadiusIncrease.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				topRadius.setProgress(topRadius.getProgress() + 1);

			}

		});

		// Decreases topRadius by one
		FloatingActionButton topRadiusDecrease = main.findViewById(R.id.fabTopRadiusDecrease);
		topRadiusDecrease.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				topRadius.setProgress(topRadius.getProgress() - 1);

			}

		});

		// Increases bottomRadius by one
		FloatingActionButton bottomRadiusIncrease = main.findViewById(R.id.fabBottomRadiusIncrease);
		bottomRadiusIncrease.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				bottomRadius.setProgress(bottomRadius.getProgress() + 1);

			}

		});

		// Decreases bottomRadius by one
		FloatingActionButton bottomRadiusDecrease = main.findViewById(R.id.fabBottomRadiusDecrease);
		bottomRadiusDecrease.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				bottomRadius.setProgress(bottomRadius.getProgress() - 1);

			}

		});

		// Increases xPosition in portrait mode by one
		FloatingActionButton xPositionIncreaseP = main.findViewById(R.id.fabXPositionPortraitIncrease);
		xPositionIncreaseP.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				xPositionP.setProgress(xPositionP.getProgress() + 1);

			}

		});

		// Decreases xPosition in portrait mode by one
		FloatingActionButton xPositionDecreaseP = main.findViewById(R.id.fabXPositionPortraitDecrease);
		xPositionDecreaseP.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				xPositionP.setProgress(xPositionP.getProgress() - 1);

			}

		});

		// Increases yPosition in portrait mode by one
		FloatingActionButton yPositionIncreaseP = main.findViewById(R.id.fabYPositionPortraitIncrease);
		yPositionIncreaseP.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				yPositionP.setProgress(yPositionP.getProgress() + 1);

			}

		});

		// Decreases yPosition in portrait mode by one
		FloatingActionButton yPositionDecreaseP = main.findViewById(R.id.fabYPositionPortraitDecrease);
		yPositionDecreaseP.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				yPositionP.setProgress(yPositionP.getProgress() - 1);

			}

		});

		// Increases xPosition in landscape mode by one
		FloatingActionButton xPositionIncreaseL = main.findViewById(R.id.fabXPositionLandscapeIncrease);
		xPositionIncreaseL.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				xPositionL.setProgress(xPositionL.getProgress() + 1);

			}

		});

		// Decreases xPosition in landscape mode by one
		FloatingActionButton xPositionDecreaseL = main.findViewById(R.id.fabXPositionLandscapeDecrease);
		xPositionDecreaseL.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				xPositionL.setProgress(xPositionL.getProgress() - 1);

			}

		});

		// Increases yPosition in landscape mode by one
		FloatingActionButton yPositionIncreaseL = main.findViewById(R.id.fabYPositionLandscapeIncrease);
		yPositionIncreaseL.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				yPositionL.setProgress(yPositionL.getProgress() + 1);

			}

		});

		// Decreases yPosition in landscape mode by one
		FloatingActionButton yPositionDecreaseL = main.findViewById(R.id.fabYPositionLandscapeDecrease);
		yPositionDecreaseL.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				yPositionL.setProgress(yPositionL.getProgress() - 1);

			}

		});



		// Notch presets array
		ArrayList<NotchItem> list = new ArrayList<>();

		// Defines the notch presets array's elements
		NotchItem rounded = new NotchItem(90, 40, 80, 0, 100, getString(R.string.notchTitle_rounded));
		NotchItem teardrop = new NotchItem(86, 30, 127, 71, 60, getString(R.string.notchTitle_teardrop));
		NotchItem mid = new NotchItem(92, 53, 125, 52, 100, getString(R.string.notchTitle_Medium));
		NotchItem wide = new NotchItem(92, 212, 125, 52, 100, getString(R.string.notchTitle_large));

		// Adds these elements
		list.add(rounded);
		list.add(teardrop);
		list.add(mid);
		list.add(wide);

		// Sets the recycler to contain the notch presets
		RecyclerView notchStyles= main.findViewById(R.id.rvNotchStyles);

		// Sets it's size to fixed
		notchStyles.setHasFixedSize(true);

		// Creates the horizontal layout manager
		LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

		// Applies the layout manager
		notchStyles.setLayoutManager(layoutManager);

		// Sets the listener to change the configuration according to the preset pressed
		NotchLayoutAdapter adapter = new NotchLayoutAdapter(getContext(), list, new NotchStyleTouchListener() {

			@Override
			public void onTouch(NotchItem item) {

				height.setProgress(item.getHeight());
				width.setProgress(item.getWidth());
				topRadius.setProgress(item.getTopRadius());
				bottomRadius.setProgress(item.getBottomRadius());
				notchSize.setProgress(item.getSize());

			}

		});

		// Applies the listener
		notchStyles.setAdapter(adapter);
		
		height.setProgress(manager.getHeight());
		width.setProgress(manager.getWidth());
		topRadius.setProgress(manager.getTopRadius());
		bottomRadius.setProgress(manager.getBottomRadius());
		notchSize.setProgress(manager.getNotchSize());
		xPositionP.setProgress(manager.getxPositionPortrait());
		yPositionP.setProgress(manager.getyPositionPortrait());
		xPositionL.setProgress(manager.getxPositionLandscape());
		yPositionL.setProgress(manager.getyPositionLandscape());

		return main;

	}

}
