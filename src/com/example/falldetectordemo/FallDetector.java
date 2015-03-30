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
	
	/**
	 * Minimum threshold for detecting fall. This threshold
	 * is used to start detecting the free fall component
	 */
	private static final double minThreshold = 3.75;
	
	/**
	 * Max threshold is used to detect the hard 'fall'
	 * after the freefall has been detected
	 */
	private static final double maxThreshold = 25.0;
	
	/**
	 * The time it should take to get frmo min threshold to max threshold
	 */
	private static final int milliSecondsUntilMax = 30000;
	
	private long timeOfMin = -1;
	
	/**
	 * Indicates whether the minimum fall detection has been met.
	 */
	private volatile boolean minMet = false;
	
	/**
	 * Inidicates whether the maximum fall detection has been met.
	 */
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
	
	/**
	 * Lock object for synchronization purposes
	 */
	private final Object lockOject = new Object();
	
	/**
	 * running boolean
	 */
	private final AtomicBoolean running;
	
	/**
	 * Timer task to repetitively check for a fall 
	 */
	private Timer timer;
	
	/**
	 * Callback to decide what happens when a fall is detected
	 */
	private FallDetectorCallback callback = null;
	
	
	/**
	 * Initialize the Fall Detector.
	 * @param context
	 */
	public FallDetector(final Context context)
	{
		this.context = context;
		
		this.sensorHelper = new SensorHelper(context);
		
		running = new AtomicBoolean(false);
		
		sensorHelper.addListener(this);
		
	}
	
	/**
	 * Pause the fall detection system
	 */
	public void pause()
	{
		running.set(false);
		timer.cancel();
		sensorHelper.pause();
	}
	
	/**
	 * Set the callback for notifying a fall detection
	 * @param cb the FallDetectorCallback instance
	 */
	public void setCallback(FallDetectorCallback cb)
	{
		this.callback = cb;
	}
	
	/**
	 * starts detector.<br/>
	 * <b>Note:</b> Should add fall detection callback before calling.
	 */
	public void runDetector()
	{
		sensorHelper.startSensors();
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
	 * Calculates total acceleration given the x,y,z acceleration values
	 * @param AValues X,Y,Z Acceleration Values
	 * @return
	 */
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
				timeOfMin = System.currentTimeMillis();
			}
			
			if(minMet)
			{
				//If the time threshold has been passed, restart the check system.
				if(System.currentTimeMillis() - timeOfMin > milliSecondsUntilMax)
				{
					minMet = false;
				}
				
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
			}
			else
			{
				callback.getValues(AValues[0], AValues[1], AValues[2], (float)At);
			}
		}
		
	}
	
}
