package com.example.falldetectordemo;

import java.util.ArrayList;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * SensorHelper is used to fulfill all of the needs of the Accelerometer Sensor.
 * @author kyle
 *
 */
public class SensorHelper {

	//Used for Logging
	private final static String TAG = "SensorHelper";
	
	/**
	 * Contains actions for all sensor types
	 */
	private final SensorManager manager;
	
	/**
	 * Sensor that detects Accelerometer values
	 */
	private final Sensor accelerometerSensor;
	
	/**
	 * Sensor that detectors gyroscope values
	 */
	private final Sensor gyroscopeSensor;
	
	/**
	 * Context of the current SensorHelper
	 */
	private final Context context;
	
	/**
	 * List of listeners listening to sensors
	 */
	private final ArrayList<SensorEventListener> listeners;
	
	/**
	 * Initialize the SensorHelper with the given context.
	 * Note: call run() afterwards to begin listening for sensor activity.
	 * @param context Context this is running on.
	 */
	public SensorHelper(final Context context)
	{
		this.context = context;
		
		//
		manager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
		
		accelerometerSensor = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		gyroscopeSensor = manager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		
		listeners = new ArrayList<>();
	}
	
	/**
	 * Pauses all activity of the sensors.
	 * @precondition: none
	 * @postcondition: The sensors will no longer be listening.
	 */
	public void pause()
	{
		for(SensorEventListener listener : listeners)
		{
			manager.unregisterListener(listener);
		}
	}
	
	/**
	 * Starts the sensors listening service.
	 * @precondition: none
	 * @postcondition: the sensors will be listening 
	 */
	public void startSensors()
	{
		for(SensorEventListener listener : listeners)
		{
			manager.registerListener(listener, accelerometerSensor,SensorManager.SENSOR_DELAY_NORMAL);
			manager.registerListener(listener, gyroscopeSensor,SensorManager.SENSOR_DELAY_NORMAL);
		}
	}
	
	/**
	 * Adds a SensorEventListener to the list of listeners to be notified
	 * when new information has been sensed.
	 * @param listener The SensorEventListener to be added
	 * @precondition: none
	 * @postcondition: The listener added will be notified when changes occur
	 */
	public void addListener(final SensorEventListener listener)
	{
		listeners.add(listener);
	}
	
	/**
	 * Removes a SensorEventListener from the list of listeners.
	 * @param listener The listener to be removed
	 * @return True if successfully removed, or false if it was never in the list
	 * @precondition: The listener is in the list of listeners
	 * @postcondition: The listener is not in the list of listeners
	 */
	public boolean removeListener(final SensorEventListener listener)
	{
		return listeners.remove(listener);
	}
	
	
	
	
}
