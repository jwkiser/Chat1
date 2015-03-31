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
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import android.widget.TextView;
import android.widget.Toast;

public class Chat extends Activity {
	private final static String TAG = Chat.class.getSimpleName();

	public static final String EXTRAS_DEVICE = "EXTRAS_DEVICE";
  private static final String KEY_TV_STATE = "KEY_TV_STATE";

	private TextView tv = null;
	//private EditText et = null;
	//private Button btn = null;
    private Button settingsbtn = null;
    private Button timebtn = null;
	private String mDeviceName;
	public static String mDeviceAddress;
	private RBLService mBluetoothLeService;
	public static Map<UUID, BluetoothGattCharacteristic> map = new HashMap<UUID, BluetoothGattCharacteristic>();
    private String redString;
    private String greenString;
    private String yellowString;
    private String blueString;



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
				displayData(intent.getByteArrayExtra(RBLService.EXTRA_DATA));

			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.second);

       	tv = (TextView) findViewById(R.id.textView);
		tv.setMovementMethod(ScrollingMovementMethod.getInstance());
        settingsbtn = (Button) findViewById(R.id.btn_settings);
        timebtn = (Button) findViewById(R.id.btn_time);

        //Send time to device
        BluetoothGattCharacteristic characteristic = map
                .get(RBLService.UUID_BLE_SHIELD_TX);

		/*btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {


                /*String str = et.getText().toString();
				byte b = 0x00;
				byte[] tmp = str.getBytes();
				byte[] tx = new byte[tmp.length + 1];
				tx[0] = b;
				for (int i = 1; i < tmp.length + 1; i++) {
					tx[i] = tmp[i - 1];
				}*/

              /* et.setText("");

            }

		});*/

        //Sync the time
        timebtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                BluetoothGattCharacteristic characteristic = map
                        .get(RBLService.UUID_BLE_SHIELD_TX);

                Calendar c = Calendar.getInstance();
                SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm");
                String sDate = formatter.format(c.getTime());

                String str = "d" + sDate;

                characteristic.setValue(str);
                mBluetoothLeService.writeCharacteristic(characteristic);
                Toast.makeText(getApplicationContext(), "Time synced @ " + sDate, Toast.LENGTH_SHORT).show();
            }
        });


        settingsbtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Settings.class));
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

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);

    // Save the current logs into the bundle
    CharSequence currentLogs = tv.getText();
    Log.d(TAG, "Saving " + currentLogs.length() + " characters from TextView");
    outState.putCharSequence(KEY_TV_STATE, currentLogs);
  }

  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    if(savedInstanceState == null) {
      return;
    }

    // Restore the logs from the bundle
    CharSequence previousLogs = savedInstanceState.getCharSequence(KEY_TV_STATE);
    if(previousLogs == null) {
      return;
    }

    Log.d(TAG, "Restoring " + previousLogs.length() + " characters from Bundle");
    tv.setText(previousLogs);
  }

	private void displayData(byte[] byteArray) {


		if (byteArray != null) {
			String data = new String(byteArray);

            SharedPreferences prefs = getSharedPreferences("button_settings",
                    MODE_PRIVATE);

            redString = prefs.getString("redpref", "Symptom Stop");
            greenString = prefs.getString("greenpref", "Symptom Start");
            blueString = prefs.getString("bluepref", "Medication");
            yellowString = prefs.getString("yellowpref", "Event");

            //Change strings via customization in "settings" page
            if (data.endsWith("1")) {                               //Red Button
                data = data.substring(0, data.length() - 1) + redString + " Stop";
                tv.setTextColor(Color.RED);
            }
            else if (data.endsWith("2")) {                          //Yellow Button
                data = data.substring(0, data.length() - 1) + yellowString;
                tv.setTextColor(Color.YELLOW);
            }
            else if (data.endsWith("3")) {                          //Green Button
                data = data.substring(0, data.length() - 1) + greenString + " Start";
                tv.setTextColor(Color.GREEN);
            }
            else if (data.endsWith("4")) {                          //Blue Button
                data = data.substring(0, data.length() - 1) + blueString;
                tv.setTextColor(Color.BLUE);
            }

			tv.append(data + System.getProperty("line.separator"));
			// find the amount we need to scroll. This works by
			// asking the TextView's internal layout for the position
			// of the final line and then subtracting the TextView's height
			final int scrollAmount = tv.getLayout().getLineTop(
					tv.getLineCount())
					- tv.getHeight();
			// if there is no need to scroll, scrollAmount will be <=0
			if (scrollAmount > 0)
				tv.scrollTo(0, scrollAmount);
			else
				tv.scrollTo(0, 0);
		}
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