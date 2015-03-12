package com.example.falldetectordemo;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

/**
 * FallDetector using the PerFallD algorithm to detect falls.
 * 
 * 
 * PerFallD  defined in this paper:
 * 
 * Comparison and Characterization of Android-Based Fall Detection Systems
 * http://www.mdpi.com/1424-8220/14/10/18543
 * @author kyle
 *
 */
public class FallDetector implements SensorEventListener {

	private static final String TAG = "FallDetector";
	
	private static final double minThreshold = 3.75;
	
	private static final double maxThreshold = 25.0;
	
	private volatile boolean minMet = false;
	
	private volatile boolean maxMet = false;
	
	/**
	 * Helper to retrieve the values
	 */
	private final SensorHelper sensorHelper;
	
	/**
	 * Current context running in
	 */
	private final Context context;

	/**
	 * Latest Accelerometer values
	 */
	private float[] AValues = {0,0,0};
	
	/**
	 * Latest Gyroscope Values
	 */
	private float[] GValues = {0,0,0};
	
	private final Object lockOject = new Object();
	
	private final AtomicBoolean running;
	
	private final int windowtt = 4000, 
	thresholdtt = 120, 
	windowct = 4000, 
	thresholdct = 50;
	
	private final int windowtv = 4000, 
	thresholdtv = 6, 
	windowcv = 4000, 
	thresholdcv = 2;
	
	private double lastAv, lastAt;
	
	private Timer timer;
	
	private FallDetectorCallback callback = null;
	
	
	public FallDetector(final Context context)
	{
		this.context = context;
		
		this.sensorHelper = new SensorHelper(context);
		
		running = new AtomicBoolean(false);
		
		sensorHelper.addListener(this);
		
	}
	
	public void pause()
	{
		running.set(false);
		timer.cancel();
		sensorHelper.pause();
	}
	
	public void setCallback(FallDetectorCallback cb)
	{
		this.callback = cb;
	}
	
	/**
	 * starts detector.<br/>
	 * <b>Note:</b> Should add fall detection callback.
	 */
	public void runDetector()
	{
		sensorHelper.startSensors();
		lastAt = calculateTotalAcceleration(AValues);
		lastAv  = calculateVerticalAcceleration(AValues,GValues);

		timer = new Timer();
		
		timer.schedule(new FallTimerTask(), 500,1);
		
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		Sensor sensor = event.sensor;
		
		if(sensor.getType() == Sensor.TYPE_ACCELEROMETER)
		{
			synchronized(lockOject)
			{
				AValues = event.values.clone();
			}
			
		}
	}
	
	/**
	 * 
	 * | Av | = | Ax sin() + Ay sin()y - Az cos()y cos ()z |
	 */
	private synchronized static double calculateVerticalAcceleration(float[] AValues, float[] GValues)
	{
		double Av = Math.abs(
				AValues[0]*Math.sin(GValues[2]*180/Math.PI) 
				+ AValues[1]*Math.sin(GValues[1]*180/Math.PI)
				- AValues[2]*Math.cos(GValues[1]*180/Math.PI)*Math.cos(GValues[2]*180/Math.PI)
				);
		
		return Av;
	}
	
	private synchronized static double calculateTotalAcceleration(float[] AValues)
	{
		return Math.abs(
				Math.sqrt(
						Math.abs(AValues[0]*AValues[0])
						+Math.abs(AValues[1]*AValues[1])
						+Math.abs(AValues[2]*AValues[2]))
				);
	}
	
	private class FallTimerTask extends TimerTask
	{

		@Override
		public void run() {
			double At = calculateTotalAcceleration(AValues);
			
			
			if(At <= minThreshold)
			{
				minMet = true;
			}
			
			if(minMet)
			{
				if(At >= maxThreshold)
				{
					maxMet = true;
				}
			}
			
			if(minMet && maxMet)
			{
				callback.onFallDetected();
				minMet = false;
				maxMet = false;
				this.cancel();
			}
			else
			{
				callback.getValues(AValues[0], AValues[1], AValues[2], (float)At);
			}
		}
		
	}
	
}
