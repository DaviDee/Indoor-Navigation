package inavi.prototyp.manager.display.impl;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class DirectionSensor {

	public final static float INVALID_VALUE = Float.NaN;

	private SensorManager sensorManager;
	private Sensor sensor;
	private SensorEventListener sensorEventListener;
	private float pivot;
	private float[] values;
	private boolean running;

	public static enum Direction {
		NORTH, NORTH_EAST, EAST, SOUTH_EAST, SOUTH, SOUTH_WEST, WEST, NORTH_WEST
	};

	public DirectionSensor(Context context) {
		this.pivot = -1;
		sensorManager = (SensorManager) context
				.getSystemService(Context.SENSOR_SERVICE);
		sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		sensorEventListener = new SensorEventListener() {

			@Override
			public void onSensorChanged(SensorEvent event) {
				values = event.values;
			}

			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
			}
		};
	}

	public void start() {
		if (running)
			stop();
		running = sensorManager.registerListener(sensorEventListener, sensor,
				SensorManager.SENSOR_DELAY_FASTEST);
	}

	public void stop() {
		sensorManager.unregisterListener(sensorEventListener);
	}

	public void setPivot(float pivot) {
		this.pivot = pivot;
	}

	public Direction getDirection() {
		if (values == null || pivot == INVALID_VALUE)
			return null;
		float x = values[0];
		x = Math.abs(pivot - x);
		if (x <= 22.5)
			return Direction.NORTH;
		else if (x <= 67.5)
			return Direction.NORTH_EAST;
		else if (x <= 112.5)
			return Direction.EAST;
		else if (x <= 157.5)
			return Direction.SOUTH_EAST;
		else if (x <= 202.5)
			return Direction.SOUTH;
		else if (x <= 247.5)
			return Direction.SOUTH_WEST;
		else if (x <= 292.5)
			return Direction.WEST;
		else if (x <= 337.5)
			return Direction.NORTH_WEST;
		else
			return Direction.NORTH;
	}

	public float getOrientation() {
		if (values == null || pivot == -1)
			return INVALID_VALUE;
		return (pivot - values[0]) % 360;
	}
}
