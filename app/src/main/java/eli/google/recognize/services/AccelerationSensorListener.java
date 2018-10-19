package eli.google.recognize.services;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.util.Log;

import java.text.NumberFormat;

import eli.google.recognize.model.AccelerateData;
import eli.google.recognize.model.Stack;

/**
 * Created by zhanbo.zhang on 2018/9/14.
 */

public class AccelerationSensorListener implements SensorEventListener {

    private static final String TAG = "elifli";
    private static final String TAG_SENSOR_GRAVITY = TAG + "_gravity";
    private static final String TAG_SENSOR_ACCELERATION = TAG + "_acceleration";
    private static final String TAG_SENSOR_LINE_ACCELE = TAG + "_line_acceleration";

    private static AccelerationSensorListener instances;
    private Context mContext;
    private SensorManager mSensorManager;
    private Sensor mGravitySensor;
    private Sensor mLinearAcceleration;
    private Sensor mPickupSensor;
    private Stack mAccelerationDataStack;

    private boolean isPhoneLieDown = false;
    private boolean isPhoneUplifted = false;
    private boolean isPhoneStable = true;
    private long lieDownTimeStamp;
    private long upliftedTimeStamp;

    private static final double mPhoneStableAcce = 0.13;
    private static final double mPhoneUnStableAcce = 4.0;

    private OnPhoneMotionChangedListener mMotionListener;

    NumberFormat nf = NumberFormat.getNumberInstance();

    public static AccelerationSensorListener getInstances(Context context) {
        return getInstances(context, null);
    }

    public static AccelerationSensorListener getInstances(Context context, OnPhoneMotionChangedListener gravityChangedListener) {
        if (instances == null) {
            instances = new AccelerationSensorListener(context, gravityChangedListener);
        }
        return instances;
    }

    private AccelerationSensorListener(Context context, OnPhoneMotionChangedListener gravityChangedListener) {
        mContext = context;
        mMotionListener = gravityChangedListener;
        nf.setMaximumFractionDigits(4);
        if (mContext != null) {
            mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
            mGravitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
            mLinearAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            mPickupSensor = mSensorManager.getDefaultSensor(25);
        }
        mAccelerationDataStack = new Stack<>(100);
        new AnalyzeAcceleration().start();
    }

    public void setMotionListener(OnPhoneMotionChangedListener listener) {
        this.mMotionListener = listener;
    }

    public void startMotionListener() {
        if (mSensorManager != null) {
            //mSensorManager.registerListener(this, mGravitySensor, SensorManager.SENSOR_DELAY_UI);
            //mSensorManager.registerListener(this, mLinearAcceleration, SensorManager.SENSOR_DELAY_UI);
            mSensorManager.registerListener(this, mPickupSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    public void stopMotionListener() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this, mLinearAcceleration);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            Log.i(TAG_SENSOR_GRAVITY, "X: " + nf.format(x) + "\tY: " + nf.format(y) + "\tZ: " + nf.format(z));
            /*if (mMotionListener != null) {
                mMotionListener.onAccelerationChanged(Sensor.TYPE_GRAVITY, x, y, z);
            }*/

            if (x < 1) {
                if (y < 1 && z > 9.6) {
                    isPhoneLieDown = true;
                    lieDownTimeStamp = SystemClock.elapsedRealtime();
                    if (isPhoneUplifted && SystemClock.elapsedRealtime() - upliftedTimeStamp < 2000) {
                        isPhoneUplifted = false;
                        Log.i(TAG, "Phone from uplifted to lie down");
                        if (mMotionListener != null) {
                            mMotionListener.onPhoneLieDown();
                        }
                    }
                } else if (y > 6 && z < 7.6) {
                    isPhoneUplifted = true;
                    upliftedTimeStamp = SystemClock.elapsedRealtime();
                    if (isPhoneLieDown && SystemClock.elapsedRealtime() - lieDownTimeStamp < 2000) {
                        isPhoneLieDown = false;
                        Log.i(TAG, "Phone from lie down to uplifted");
                        if (mMotionListener != null) {
                            mMotionListener.onPhoneUplifted();
                        }
                    }
                }
            }
        } else if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            Log.i(TAG_SENSOR_LINE_ACCELE, "X: " + nf.format(x) + "\tY: " + nf.format(y) + "\tZ: " + nf.format(z));

            float sum = Math.abs(x) + Math.abs(y) + Math.abs(z);

            if (isPhoneStable && sum > mPhoneUnStableAcce && mMotionListener != null) {
                isPhoneStable = false;
                mMotionListener.onPhoneUnstable();
            }

            mAccelerationDataStack.addElement(new AccelerateData(x, y, z));

            if (mMotionListener != null) {
                mMotionListener.onAccelerationChanged(Sensor.TYPE_LINEAR_ACCELERATION, x, y, z);
                //mMotionListener.onOtherInfo("SUM: " + sum);
            }
        } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            Log.i(TAG_SENSOR_ACCELERATION, "X: " + nf.format(x) + "\tY: " + nf.format(y) + "\tZ: " + nf.format(z));
        } else if (event.sensor.getType() == 25) {
            Log.i(TAG, "sensor: " + event.values.toString());
        }
    }

    public interface OnPhoneMotionChangedListener {
        void onAccelerationChanged(int type, float x, float y, float z);
        void onPhoneUplifted();
        void onPhoneLieDown();
        void onPhoneUnstable();
        void onPhoneStable();
        void onOtherInfo(String text);
    }

    class AnalyzeAcceleration extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(200);

                    if (mAccelerationDataStack != null) {
                        int size = mAccelerationDataStack.currentSize();
                        double[] xDates = new double[size];
                        double[] yDates = new double[size];
                        double[] zDates = new double[size];

                        for (int i = 0; i < size; i ++) {
                            AccelerateData data = (AccelerateData) mAccelerationDataStack.getData(i);
                            xDates[i] = data.AX;
                            yDates[i] = data.AY;
                            zDates[i] = data.AZ;
                        }

                        double aveX = average(true, xDates);
                        double aveY = average(true, yDates);
                        double aveZ = average(true, zDates);

                        double aveSUM = aveX + aveY + aveZ;
                        if (mMotionListener != null) {
                            mMotionListener.onOtherInfo("average sum: " + nf.format(aveSUM));
                        }
                        if (!isPhoneStable && aveSUM < mPhoneStableAcce && mMotionListener != null) {
                            mMotionListener.onPhoneStable();
                            isPhoneStable = true;
                        }
                    }

                } catch (Exception e) {
                }
            }
        }
    }

    private double disperse(double [] dates) {
        double average = average(false, dates);

        double total1 = 0;
        for (int i = 0; i < dates.length; i ++) {
            double diff = Math.abs(average - dates[i]);
            total1 += diff * diff;
        }
        return Math.sqrt(total1/dates.length);
    }

    private double average(boolean isABS, double [] dates) {
        double average;
        double total = 0;

        for (int i = 0; i < dates.length; i++) {
            total += isABS ? Math.abs(dates[i]) : dates[i];
        }
        average = total/dates.length;

        return average;
    }
}
