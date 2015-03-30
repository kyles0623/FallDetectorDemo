package com.example.falldetectordemo;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * The Fall Detection Service is a background service waiting for
 * a fall to happen. The user of the class can define what happens
 * when a fall is detected.
 * @author kyle
 *
 */
public class FallDetectionService extends Service {

	private FallDetector fallDetector;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		fallDetector = new FallDetector(this);
		fallDetector.setCallback(new FallDetectorCallback(){

			@Override
			public void getValues(float a, float b, float c, float acceleration) {
				Log.d("FallDetectionService","Values: "+a+" , "+b+" , "+c+"  = "+acceleration);
				
			}

			@Override
			public void onFallDetected() {
				Log.d("FallDetectionService","FALL DETECTED");
				fallDetector.pause();
				//TODO: DO SOMETHING ELSE
			}
		});
		
	}
	
	@Override
	public int onStartCommand(Intent intent,int flags, int startId)
	{
		fallDetector.runDetector();
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		fallDetector.pause();
	}
	
	

	

}
