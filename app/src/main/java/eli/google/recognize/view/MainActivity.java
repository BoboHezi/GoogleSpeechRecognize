package eli.google.recognize.view;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.Sensor;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.CallLog;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import eli.google.recognize.services.AccelerationSensorListener;
import eli.google.recognize.services.PhoneStableStateServices;
import eli.google.recognize.R;
import eli.google.recognize.thread.AccessTokenTask;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "elifli_MainActivity";

    private PhoneStableStateServices gravityServices;
    private TextView mSensorTypeText;

    DecimalFormat df = new DecimalFormat();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        df.setMaximumFractionDigits(4);
        df.setNegativePrefix("- ");
        df.setPositivePrefix("+");

        mSensorTypeText = (TextView) findViewById(R.id.text_sensor_type);

        AccessTokenTask tokenTask = new AccessTokenTask(this);

        tokenTask.registerListener((text, isFinal) -> {
            runOnUiThread(() -> {
                        mSensorTypeText.setText(mSensorTypeText.getText() + ", " + text);
                    }
            );
        });

        tokenTask.execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //unbindService(mServiceConnection);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (menu != null && menu.getClass().getSimpleName().equalsIgnoreCase("MenuBuilder")) {
            try {
                Method method = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                method.setAccessible(true);
                method.invoke(menu, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return super.onMenuOpened(featureId, menu);
    }
}
