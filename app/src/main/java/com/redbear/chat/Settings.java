package com.redbear.chat;

import java.sql.Time;
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
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import org.w3c.dom.Text;

public class Settings extends Activity {
    private final static String TAG = Settings.class.getSimpleName();

    public static final String EXTRAS_DEVICE = "EXTRAS_DEVICE";
    private TextView tv = null;
    private EditText et = null;
    private Button btn = null;
    private Button settingsbtn = null;
    private TimePicker med1;
    private TimePicker med2;
    private Switch medswitch1;
    private Switch medswitch2;
    private String alarm1;
    private String alarm2;
    private String mDeviceName;
    private RadioGroup yellowGroup;
    private RadioGroup blueGroup;
    private String[] yellowItems;
    private String[] blueItems;
    public static String stop = "Symptom Stop";
    public static String start = "Symptom Start";
    public static String blue = "Medication";
    public static String yellow = "Event";
    public static final String MY_PREFS_NAME = "PDSettings";

    public static String mDeviceAddress;
    private RBLService mBluetoothLeService;
    private Map<UUID, BluetoothGattCharacteristic> map = new HashMap<UUID, BluetoothGattCharacteristic>();

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

        //Read files from preference file
        SharedPreferences prefs = getSharedPreferences("button_settings",
                MODE_PRIVATE);


        //Initialize Symptom Spinner
        final Spinner symptoms = (Spinner)findViewById(R.id.startDropdown);
        String[] symptomItems = new String[]{"Bradykinesia", "Freezing", "Tremor", "Balance-Walking"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, symptomItems);
        symptoms.setAdapter(adapter);

        //Initialize Yellow Spinner
        yellowGroup = (RadioGroup) findViewById(R.id.yellowGroup);
        final Spinner yellowDrop = (Spinner)findViewById(R.id.yellowDropdown);
        yellowGroup.clearCheck();

        yellowGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rYellowMeds) {
                    yellowItems = new String[]{"Azilect (Rasagline) 1.0mg", "Azilect (Rasagline) 0.5 mg", "Eldepryl(Selegiline) 5 mg",
                                "Mirapex (Pramipexole) 1.0 mg", "Mirapex (Pramipexole) 1.5 mg", "Neupro (Rotigotine) 2.0 mg", "Requip (Ropinirole) 1.0 mg",
                                "Requip (Ropinirole) 2.0 mg", "Sinemet 12.5 mg/50 mg", "Symmetrel (Amantadine) 100 mg", "Sinemet 25 mg/100 mg"};
                }
                else if (checkedId == R.id.rYellowEvent) {
                    yellowItems = new String[]{"Dyskinesia", "Fatigue", "Depression", "Impulsive Behavior", "Memory Difficult"};
                }
                ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(Settings.this, android.R.layout.simple_spinner_item, yellowItems);
                yellowDrop.setAdapter(adapter2);
            }
        });

        final RadioButton yellowMeds = (RadioButton) findViewById(R.id.rYellowMeds);
        yellowMeds.setChecked(true);
        final RadioButton yellowEvent = (RadioButton) findViewById(R.id.rYellowEvent);
        yellowMeds.setChecked(true);

        //Initialize Blue Spinner
        blueGroup = (RadioGroup) findViewById(R.id.blueGroup);
        final Spinner blueDrop = (Spinner)findViewById(R.id.blueDropdown);
        blueGroup.clearCheck();

        blueGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rBlueMeds) {
                    blueItems = new String[]{"Azilect (Rasagline) 1.0mg", "Azilect (Rasagline) 0.5 mg", "Eldepryl(Selegiline) 5 mg",
                            "Mirapex (Pramipexole) 1.0 mg", "Mirapex (Pramipexole) 1.5 mg", "Neupro (Rotigotine) 2.0 mg", "Requip (Ropinirole) 1.0 mg",
                            "Requip (Ropinirole) 2.0 mg", "Sinemet 12.5 mg/50 mg", "Symmetrel (Amantadine) 100 mg", "Sinemet 25 mg/100 mg"};
                }
                else if (checkedId == R.id.rBlueEvent) {
                    blueItems = new String[]{"Dyskinesia", "Fatigue", "Depression", "Impulsive Behavior", "Memory Difficult"};
                }
                ArrayAdapter<String> adapter3 = new ArrayAdapter<String>(Settings.this, android.R.layout.simple_spinner_item, blueItems);
                blueDrop.setAdapter(adapter3);
            }
        });

        final RadioButton blueMeds = (RadioButton) findViewById(R.id.rBlueMeds);
        blueMeds.setChecked(true);
        final RadioButton blueEvent = (RadioButton) findViewById(R.id.rBlueEvent);
        blueEvent.setChecked(true);

        //Initialize Number Picker
        String[] values = new String[10];
        for (int i=0;i<values.length;i++){
            values[i]=Integer.toString((i+1)*5);
        }

        final NumberPicker np = (NumberPicker) findViewById(R.id.numberPicker2);
        np.setMaxValue(values.length-1);
        np.setMinValue(1);
        np.setDisplayedValues(values);
        np.setWrapSelectorWheel(false);
        np.setValue(prefs.getInt("numberpicker", 5));   //Restore previous setting value



        //Initialize Time Pickers
        Switch switch1 = (Switch) findViewById(R.id.medSwitch1);
        Switch switch2 = (Switch) findViewById(R.id.medSwitch2);
        switch1.setChecked(prefs.getBoolean("switch1", false));
        switch2.setChecked(prefs.getBoolean("switch2", false));

        med1 = (TimePicker) findViewById(R.id.medTime1);
        med1.setCurrentHour(prefs.getInt("hour1", 0));
        med1.setCurrentMinute(prefs.getInt("minute1", 0));

        med2 = (TimePicker) findViewById(R.id.medTime2);
        med2.setCurrentHour(prefs.getInt("hour2", 0));
        med2.setCurrentMinute(prefs.getInt("minute2", 0));


        medswitch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    med1.setVisibility(View.VISIBLE);
                } else {
                    med1.setVisibility(View.INVISIBLE);
                }
            }
        });

        medswitch2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    med2.setVisibility(View.VISIBLE);
                } else {
                    med2.setVisibility(View.INVISIBLE);
                }
            }
        });


        //When save button pressed, send data to RBL
        btn = (Button) findViewById(R.id.btn_save);
        btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                BluetoothGattCharacteristic characteristic = Chat.map.get(RBLService.UUID_BLE_SHIELD_TX);

                //Interval Setting
                final String interval = "i" + Integer.toString(np.getValue());

                //Alarm 1
                final String hour1 = Integer.toString(med1.getCurrentHour());
                final String minute1 = Integer.toString(med1.getCurrentMinute());

                if (medswitch1.isChecked()) {
                    alarm1 = "a" + hour1 + ":" + minute1;
                }
                else {
                    alarm1 = "aXX:XX";
                }

                //Alarm 2
                final String hour2 = Integer.toString(med2.getCurrentHour());
                final String minute2 = Integer.toString(med2.getCurrentMinute());

                if (medswitch2.isChecked()) {
                    alarm2 = "b" + hour2 + ":" + minute2;
                }
                else {
                    alarm2 = "bXX:XX";
                }

                //Send Interval setting
                characteristic.setValue(interval);
                mBluetoothLeService.writeCharacteristic(characteristic);

                try {
                    Thread.sleep(500);                 //Wait 1/2 second
                } catch(InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }

                //Send alarm 1 only if checked
                 characteristic.setValue(alarm1);
                 mBluetoothLeService.writeCharacteristic(characteristic);


                try {
                    Thread.sleep(500);                 //Wait 1/2 second
                } catch(InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }

                //Send alarm 2 only if checked
                 characteristic.setValue(alarm2);
                 mBluetoothLeService.writeCharacteristic(characteristic);

                try {
                    Thread.sleep(500);                 //Wait 1/2 second
                } catch(InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }


                //Button Customization Strings
                start = symptoms.getSelectedItem().toString();
                stop = symptoms.getSelectedItem().toString();
                yellow = yellowDrop.getSelectedItem().toString();
                blue = blueDrop.getSelectedItem().toString();

                //Save all Settings
                SharedPreferences prefs = getSharedPreferences("button_settings", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("redpref", start);
                editor.putString("greenpref", stop);
                editor.putString("yellowpref", yellow);
                editor.putString("bluepref", blue);
                editor.putInt("numberpicker", np.getValue());
                editor.putInt("hour1", med1.getCurrentHour());
                editor.putInt("minute1", med1.getCurrentMinute());
                editor.putInt("hour2", med2.getCurrentHour());
                editor.putInt("minute2", med2.getCurrentMinute());
                editor.putBoolean("switch1", medswitch1.isChecked());
                editor.putBoolean("switch2", medswitch2.isChecked());


                editor.commit();

                //Let user know that settings were saved
                Toast.makeText(getApplicationContext(), "Settings Saved", Toast.LENGTH_SHORT).show();




            }

        });





        Intent intent = getIntent();
        mDeviceAddress = intent.getStringExtra(Device.EXTRA_DEVICE_ADDRESS);
        mDeviceName = intent.getStringExtra(Device.EXTRA_DEVICE_NAME);


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
