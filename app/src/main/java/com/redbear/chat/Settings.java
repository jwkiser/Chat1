package com.redbear.chat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

public class Settings extends Activity {
    private final static String TAG = Settings.class.getSimpleName();

    public static final String EXTRAS_DEVICE = "EXTRAS_DEVICE";
    private TextView tv = null;
    private EditText et = null;
    private Button btn = null;
    private Button settingsbtn = null;
    private String mDeviceName;
    public static String mDeviceAddress;
    private RBLService mBluetoothLeService;
    private Map<UUID, BluetoothGattCharacteristic> map = new HashMap<UUID, BluetoothGattCharacteristic>();

    private static final String PREFERENCE_FILENAME = "redbear";
    private static final String PREFERENCE_REMINDER_PERIOD_MINUTES = "ReminderPeriod";


    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName,
                                       IBinder service) {
            mBluetoothLeService = ((RBLService.LocalBinder) service)
                    .getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up
            // initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (RBLService.ACTION_GATT_DISCONNECTED.equals(action)) {
            } else if (RBLService.ACTION_GATT_SERVICES_DISCOVERED
                    .equals(action)) {
                getGattService(mBluetoothLeService.getSupportedGattService());
            } else if (RBLService.ACTION_DATA_AVAILABLE.equals(action)) {
                //displayData(intent.getByteArrayExtra(RBLService.EXTRA_DATA));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        btn = (Button) findViewById(R.id.btn_save);
        btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                BluetoothGattCharacteristic characteristic = Chat.map.get(RBLService.UUID_BLE_SHIELD_TX);

                String str = "i1";
                String str2 = "a19:36";


                characteristic.setValue(str);
                mBluetoothLeService.writeCharacteristic(characteristic);

                try {
                    Thread.sleep(1000);                 //1000 milliseconds is one second.
                } catch(InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }

                characteristic.setValue(str2);
                mBluetoothLeService.writeCharacteristic(characteristic);

                SharedPreferences preferences = Settings.this.getSharedPreferences(PREFERENCE_FILENAME, ContextWrapper.MODE_PRIVATE);
                NumberPicker np = (NumberPicker) findViewById(R.id.numberPicker2);

                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt(PREFERENCE_REMINDER_PERIOD_MINUTES, np.getValue());
                boolean success = editor.commit();

                if(!success) {
                  Log.e(TAG, "Error writing preferences to disk");
                }
            }

        });



        Intent intent = getIntent();

        mDeviceAddress = intent.getStringExtra(Device.EXTRA_DEVICE_ADDRESS);
        mDeviceName = intent.getStringExtra(Device.EXTRA_DEVICE_NAME);

        getActionBar().setTitle(mDeviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        Intent gattServiceIntent = new Intent(this, RBLService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }


    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            mBluetoothLeService.disconnect();
            mBluetoothLeService.close();

            System.exit(0);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
      SharedPreferences preferences = this.getSharedPreferences(PREFERENCE_FILENAME, ContextWrapper.MODE_PRIVATE);

      // Default to 5 minutes
      int reminderPeriod = preferences.getInt(PREFERENCE_REMINDER_PERIOD_MINUTES, 5);
      NumberPicker np = (NumberPicker) findViewById(R.id.numberPicker2);
      np.setValue(reminderPeriod);
    }

    @Override
    protected void onStop() {
        super.onStop();

        unregisterReceiver(mGattUpdateReceiver);
    }



    private void getGattService(BluetoothGattService gattService) {
        if (gattService == null)
            return;

        BluetoothGattCharacteristic characteristic = gattService
                .getCharacteristic(RBLService.UUID_BLE_SHIELD_TX);
        map.put(characteristic.getUuid(), characteristic);

        BluetoothGattCharacteristic characteristicRx = gattService
                .getCharacteristic(RBLService.UUID_BLE_SHIELD_RX);
        mBluetoothLeService.setCharacteristicNotification(characteristicRx,
                true);
        mBluetoothLeService.readCharacteristic(characteristicRx);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(RBLService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(RBLService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(RBLService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(RBLService.ACTION_DATA_AVAILABLE);

        return intentFilter;
    }
}







//Settings JAVA, DO NOT DELETE
/*package com.redbear.chat;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;




public class Settings extends Activity {

    private Map<UUID, BluetoothGattCharacteristic> map2 = new HashMap<UUID, BluetoothGattCharacteristic>();
    private Button savebtn = null;
    private NumberPicker np = null;
    private RBLService mBluetoothLeService = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        String[] values = new String[10];
        for (int i=0;i<values.length;i++){
            values[i]=Integer.toString((i+1)*5);
        }

        final NumberPicker np = (NumberPicker) findViewById(R.id.numberPicker2);
        np.setMaxValue(values.length-1);
        np.setMinValue(1);
        np.setDisplayedValues(values);
        np.setWrapSelectorWheel(false);

        mBluetoothLeService.connect(Chat.mDeviceAddress);
        savebtn = (Button) findViewById(R.id.btn_save);

        savebtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                BluetoothGattCharacteristic characteristic2 = map2
                        .get(RBLService.UUID_BLE_SHIELD_TX);

                final String s = Integer.toString(np.getValue());
                TextView tv = (TextView) findViewById(R.id.textView2);
                tv.setText(s);

               /* characteristic2.setValue(s);
                mBluetoothLeService.writeCharacteristic(characteristic2);*/

/*

            }
        });
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
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

}*/
