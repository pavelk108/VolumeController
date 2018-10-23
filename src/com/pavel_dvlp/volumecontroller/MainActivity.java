package com.pavel_dvlp.volumecontroller;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class MainActivity extends Activity implements OnClickListener, LocationListener, OnSeekBarChangeListener{
	public static final String APP_PREFERENCES = "mysettings"; 
	public static final String APP_PREFERENCES_PBAR = "pbar_";
	
	/*private double speedMaxVolume = 5.0;
	private int minVolume;*/
	private int maxVolume;
	private double minSpeed = -1;
	private double maxSpeed = 0;
	private AudioManager audioManager;
	private LocationManager locationManager;
	
	private SharedPreferences mSettings;
	private SeekBar seekBars[] = new SeekBar[10];
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		findViewById(R.id.button1).setOnClickListener(this);
		
		locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
		
		audioManager =(AudioManager) getSystemService(Context.AUDIO_SERVICE);
		maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		
		
		LinearLayout ll = (LinearLayout) findViewById(R.id.LinearLayout1);
		for (int i = 0; i < seekBars.length; i++){
			seekBars[i] = new SeekBar(this);
			seekBars[i].setMax(maxVolume);
			//seekBars[i].setProgress(i);
			seekBars[i].setOnSeekBarChangeListener(this);
			ll.addView(seekBars[i]);	
		};
		
		loadMySettings();
	}
	
	@Override
	public void onClick(View arg0) {
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 
				audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
	}


	@Override
	public void onLocationChanged(Location location) {
		double speed = location.getSpeed();
		if (speed < maxSpeed && speed > minSpeed)  
			return;
		int vol;
		int pbar_id = (int)(speed/5.0);
		if (pbar_id >= seekBars.length - 1) {
			vol = seekBars[seekBars.length-1].getProgress();
			maxSpeed = 100; 
			minSpeed = seekBars.length * 5 - 5 - 2;
		} 
		else{
			int vol_below = seekBars[pbar_id].getProgress();
			int vol_above = seekBars[pbar_id+1].getProgress();
			double delta = speed/5.0 - Math.floor(speed/5.0);
			int diff = vol_above - vol_below;
			vol = (int)(delta*diff + vol_below); 
			minSpeed = (int)(delta*diff) * 5.0 / diff + pbar_id * 5 - 2;
			maxSpeed = (int)(delta*diff + 1) * 5.0 / diff + pbar_id * 5 + 2;
		}
	
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, 0);
		((Button)findViewById(R.id.button1)).setText("s:"+location.getSpeed());
	}

	private void saveMySettings(){
		SharedPreferences.Editor editor = mSettings.edit();
		for (int i = 0; i < seekBars.length; i++){
			editor.putInt(APP_PREFERENCES_PBAR+i, seekBars[i].getProgress());	
		}
		editor.apply();
	}
	
	private void loadMySettings(){
		for (int i = 0; i < seekBars.length; i++){ 
			
			seekBars[i].setProgress(mSettings.getInt(APP_PREFERENCES_PBAR+i, i));
		}
	}
	

	@Override
	public void onProviderDisabled(String arg0) {	}
	@Override
	public void onProviderEnabled(String arg0) {	}
	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {	}


	@Override
	public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {	}
	@Override
	public void onStartTrackingTouch(SeekBar arg0) {}
	@Override
	public void onStopTrackingTouch(SeekBar arg0) {	
		saveMySettings();
	}
}
