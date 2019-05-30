package eli.google.recognize.view;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import eli.google.recognize.R;
import eli.google.recognize.services.VoiceRecorder;
import eli.google.recognize.thread.AccessTokenTask;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "elifli_MainActivity";

    private TextView mSensorTypeText;
    private VoiceRecorder mVoiceRecorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSensorTypeText = (TextView) findViewById(R.id.text_sensor_type);

        AccessTokenTask tokenTask = new AccessTokenTask(this);

        tokenTask.registerListener((text, isFinal) -> {
            runOnUiThread(() -> {
                        mSensorTypeText.setText(mSensorTypeText.getText() + ", " + text);
                    }
            );
        });

        tokenTask.execute();
        checkAndRequestPermission();

        Log.i(TAG, "onCreate");
        mVoiceRecorder = new VoiceRecorder(voiceCallback);
        mVoiceRecorder.start();
    }

    VoiceRecorder.Callback voiceCallback = new VoiceRecorder.Callback() {
        @Override
        public void onVoiceStart() {
            Log.i(TAG, "onVoiceStart");
        }

        @Override
        public void onVoice(byte[] data, int size) {
            Log.i(TAG, "onVoice\tdate size: " + size);
        }

        @Override
        public void onVoiceEnd() {
            Log.i(TAG, "onVoiceEnd");
        }
    };

    private void checkAndRequestPermission() {
        List<String> permissions = new ArrayList<>();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                && !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
            permissions.add(Manifest.permission.RECORD_AUDIO);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED
                && !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CALL_LOG)) {
            permissions.add(Manifest.permission.READ_CALL_LOG);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED
                && !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_CONTACTS)) {
            permissions.add(Manifest.permission.WRITE_CONTACTS);
        }

        if (permissions.size() > 0) {
            String[] pers = new String[permissions.size()];
            for (int i = 0; i < permissions.size(); i ++) {
                pers[i] = permissions.get(i);
            }
            ActivityCompat.requestPermissions(this, pers, 234);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mVoiceRecorder.stop();
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
