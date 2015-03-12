package com.example.falldetectordemo;

public interface FallDetectorCallback {

	void getValues(float a, float b, float c, float acceleration);
	
	void onFallDetected();
}
