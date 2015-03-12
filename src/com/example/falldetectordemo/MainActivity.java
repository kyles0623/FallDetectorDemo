package com.example.falldetectordemo;

import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {

	private FallDetector fallDetector;
	
	private TextView textView;
	
	private double maxAcc = 0.0;
	
	private double minAcc = 150.0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		fallDetector = new FallDetector(this);
	
		textView = (TextView)findViewById(R.id.text_demo);
		
		fallDetector.setCallback(new FallDetectorCallback(){

			@Override
			public void getValues(final float a, final float b, final float c, final float totalAcceleration) {
				
				 (new Handler(getMainLooper())).post(new Runnable(){

					@Override
					public void run() {
						
						if(totalAcceleration > maxAcc)
							maxAcc = totalAcceleration;
						else if(totalAcceleration < minAcc)
							minAcc = totalAcceleration;
						
						textView.setText("Max: "+maxAcc+", Min: "+minAcc);
					}
				 });
			}

			@Override
			public void onFallDetected() {

				 (new Handler(getMainLooper())).post(new Runnable(){

						@Override
						public void run() {
							
							textView.setText("FALL DETECTED!");
							Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
							MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), notification);
							mediaPlayer.start();
						}
					 });
				
			}
		});
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		fallDetector.runDetector();
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		fallDetector.pause();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
