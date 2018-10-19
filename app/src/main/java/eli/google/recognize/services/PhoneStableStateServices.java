package eli.google.recognize.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by zhanbo.zhang on 2018/9/14.
 */

public class PhoneStableStateServices extends Service {

    private static final String TAG = "elifli_Service";

    private AccelerationSensorListener mAccelerationSensorListener;

    boolean isScreenLock = false;

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        super.onCreate();
        mAccelerationSensorListener = AccelerationSensorListener.getInstances(this);
        mAccelerationSensorListener.setMotionListener(listener);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(receiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new ServicesBinder();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    public class ServicesBinder extends Binder {
        public PhoneStableStateServices getServices() {
            return PhoneStableStateServices.this;
        }
    }

    public void setMotionListener(AccelerationSensorListener.OnPhoneMotionChangedListener listener) {
        if (mAccelerationSensorListener != null) {
            mAccelerationSensorListener.setMotionListener(listener);
            mAccelerationSensorListener.startMotionListener();
        }
    }

    AccelerationSensorListener.OnPhoneMotionChangedListener listener = new AccelerationSensorListener.OnPhoneMotionChangedListener() {
        @Override
        public void onAccelerationChanged(int type, float x, float y, float z) {

        }

        @Override
        public void onPhoneUplifted() {

        }

        @Override
        public void onPhoneLieDown() {

        }

        @Override
        public void onPhoneUnstable() {
            Log.i(TAG, "The phone is unstable now.");
        }

        @Override
        public void onPhoneStable() {
            Log.i(TAG, "The phone is stable now.");
        }

        @Override
        public void onOtherInfo(String text) {

        }
    };

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == Intent.ACTION_SCREEN_OFF) {
                Log.i(TAG, "SCREEN_OFF");
                isScreenLock = true;
                mAccelerationSensorListener.startMotionListener();
            } else if (action == Intent.ACTION_SCREEN_ON) {
                Log.i(TAG, "SCREEN_ON");
                isScreenLock = false;
                mAccelerationSensorListener.stopMotionListener();
            }
        }
    };
}
