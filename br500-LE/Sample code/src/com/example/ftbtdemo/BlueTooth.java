package com.example.ftbtdemo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.feitian.readerdk.Tool.DK;

/**
 * This is the main Activity that displays the current chat session.
 */
public class BlueTooth extends Activity implements OnClickListener {
	// 宏消息定义
	private static final boolean Debug = true;
	private static final String TAG = "Bluetooth";
	// Key names received from the BluetoothService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";
	public boolean mScanning;
	// 控件、
	private Button mSend;
	private Button mList;
	private Button mConnect;
	private Button mDisConnect;
	private Button mPowerOn;
	private Button mPowerOff;
	
	private Button mExit;
	private Button mclearReceiveData;

	private Button mGetStatus;
	private EditText mEditSend;// 发送数据

	private EditText mEditReceive;
	private Spinner deviceListSpinner;

	private ft_reader mReader;


	private ArrayAdapter<String> mAdapter;
	ArrayList<String> list;
	ArrayList<BluetoothDevice> arrayForBlueToothDevice;
	//
	private List<String> list3 = new ArrayList<String>();
	private ArrayAdapter<String> adapter;

;
	// for test
	BluetoothDevice mBlueToothDevice;
	BluetoothAdapter mBlueToothAdapter = null;
	BluetoothSocket mBlueToothSocket;
	InputStream mInput;
	OutputStream mOutput;
	
	private BluetoothAdapter mBluetoothAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		if (Debug)
			Log.e(TAG, "===onCreate==");
		super.onCreate(savedInstanceState);

		if (isTabletDevice()) {
			setContentView(R.layout.title_activity_ft_bt_demo);
//			setContentView(R.layout.activity_phone);
		} else {
			setContentView(R.layout.activity_phone);
		}
		
		
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "error_bluetooth_not_supported", Toast.LENGTH_SHORT).show();
            finish();
        }
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        
        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "error_bluetooth_not_supported", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    //寮�惎钃濈墮
    mBluetoothAdapter.enable();
    
    
		mSend = (Button) findViewById(R.id.BSendData);
		mSend.setOnClickListener(this);
		mList = (Button) findViewById(R.id.BList);
		mList.setOnClickListener(this);
		mConnect = (Button) findViewById(R.id.Bconnect);
		mConnect.setOnClickListener(this);
		mDisConnect = (Button) findViewById(R.id.BdisConnect);
		mDisConnect.setOnClickListener(this);
		mExit = (Button) findViewById(R.id.BExit);
		mExit.setOnClickListener(this);
		mclearReceiveData = (Button) findViewById(R.id.BclearReceiveData);
		mclearReceiveData.setOnClickListener(this);
		mPowerOn = (Button) findViewById(R.id.BPowerOn);
		mPowerOn.setOnClickListener(this);
		mPowerOff = (Button) findViewById(R.id.BPowerOff);
		mPowerOff.setOnClickListener(this);

		mGetStatus = (Button) findViewById(R.id.BGetStatus);
		mGetStatus.setOnClickListener(this);

		mEditSend = (EditText) findViewById(R.id.ESendData);
		mEditSend.setText("0084000008");
		
		mEditReceive = (EditText) findViewById(R.id.Ereceive);

		deviceListSpinner = (Spinner) findViewById(R.id.spinner1);
		
		mBluetoothAdapter.enable();
		/* 获取 */
		arrayForBlueToothDevice = new ArrayList<BluetoothDevice>();

		list = new ArrayList<String>();
		mAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, list);
		mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		deviceListSpinner.setAdapter(mAdapter);
		deviceListSpinner
				.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						// TODO Auto-generated method stub
						if (Debug)
							Log.e(TAG, "select device " + list.get(arg2));
						mBlueToothDevice = arrayForBlueToothDevice.get(arg2);
						Log.e(TAG, "" + mBlueToothDevice.getAddress() + "  "
								+ mBlueToothDevice.getName());
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
						// TODO Auto-generated method stub
					}
				});

			
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, list3);

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		stat_disconnect();
	}

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case DK.CARD_STATUS:
				switch (msg.arg1) {
				case DK.CARD_ABSENT:
					displayData("IFD", "card absent");
					stat_poweroff();
					break;
				case DK.CARD_PRESENT:
					displayData("IFD", "card persent");
					break;
				case DK.CARD_UNKNOWN:
					displayData("IFD", "card unknown");
					break;
				case DK.IFD_COMMUNICATION_ERROR:
					displayData("IFD", "IFD error");
					break;
				}
			default:
				break;
			}
		}
	};

	@SuppressLint("NewApi")
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == mList) {
			
			mAdapter.clear();
			list.clear();
			arrayForBlueToothDevice.clear();
			scanLeDevice(true);
			
		} else if (v == mConnect) {
			if (mBlueToothDevice == null) {
				displayData("System", "select device first");
				return;
			}
			try {	
				scanLeDevice(false);
				mReader = new ft_reader(mBlueToothDevice.getAddress(), this);
				
				displayData("System", "mConnect ok");

				mReader.registerCardStatusMonitoring(mHandler);
				//
				stat_connect();
			} catch (FtBlueReadException e) {
				stat_disconnect();
				displayData("IFD", e.toString());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				stat_disconnect();
				e.printStackTrace();
			}

		} else if (v == mDisConnect) {
			//mReader.PowerOff();
			mReader.readerClose();
			stat_disconnect();
		} else if (v == mSend) {
			String str = mEditSend.getText().toString();
			if (!isLegal(str)) {
				new AlertDialog.Builder(BlueTooth.this)
						.setTitle("prompt")
						.setMessage("please input data as '0~9' 'a~f' 'A~F'")
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										mEditSend.setText("");
									}
								}).show();
			} else {
				byte[] tmp = Tool.hexStringToBytes(str);
				displayData("Send", str);
				byte[] rev = new byte[1024];
				int[] length = new int[2];
				int ret = DK.RETURN_SUCCESS;
				try {
					ret = mReader.transApdu(tmp.length, tmp, length, rev);
					displayData("Receive", Tool.byte2HexStr(rev, length[0]));
				} catch (FtBlueReadException e) {
					// TODO Auto-generated catch block
					displayData("Receive", e.toString());
				}
			}

		} else if (v == mPowerOn) {

			try {
				 mReader.PowerOn();
				
				displayData("PowerON", "success");
				stat_poweron();
			} catch (FtBlueReadException e) {
				displayData("PowerON", "faild");
			}
		} else if (v == mPowerOff) {
			try {
				mReader.PowerOff();
				displayData("mPowerOff", "success");
				stat_poweroff();
			} catch (FtBlueReadException e) {
				displayData("mPowerOff", e.toString());
			}

		} else if (v == mGetStatus) {
			int ret = 0;
			try {
				ret = mReader.getCardStatus();
			} catch (FtBlueReadException e) {
				displayData("mGetStatus", e.toString());
			}
			if (ret == DK.CARD_ABSENT) {
				displayData("GetStatus", "card absent");
			} else if (ret == DK.CARD_PRESENT) {
				displayData("GetStatus", "card present");
			} else {
				displayData("GetStatus", "card unknow");
			}
		} else  if (v == mclearReceiveData) {
			mEditReceive.setText("");
		} else if (v == mExit) {
			new AlertDialog.Builder(BlueTooth.this)
					.setTitle("prompt")
					.setMessage("Do you want to leave?")
					.setPositiveButton("YES",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									finish();
								}
							})
					.setNegativeButton("NO",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// do nothing
								}
							}).show();
		}
	}

	private void stat_poweroff() {
		mPowerOn.setEnabled(true);
		mPowerOff.setEnabled(false);
		mSend.setEnabled(false);
	}

	private void stat_disconnect() {
		mPowerOn.setEnabled(false);
		mPowerOff.setEnabled(false);
		mGetStatus.setEnabled(false);
		mSend.setEnabled(false);
		mConnect.setEnabled(true);
		mDisConnect.setEnabled(false);
	}

	private void stat_poweron() {
		mPowerOn.setEnabled(false);
		mPowerOff.setEnabled(true);
		mSend.setEnabled(true);

	}

	private void stat_connect() {
		mPowerOn.setEnabled(true);
		mPowerOff.setEnabled(false);
		mGetStatus.setEnabled(true);
		mSend.setEnabled(false);
		mConnect.setEnabled(false);
		mDisConnect.setEnabled(true);

	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
	}

	@Override
	public synchronized void onPause() {
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroy() {
		Log.d("system", "OnDestroy()");
		super.onDestroy();
		try {
			if (null != mReader) {
				mReader.readerClose();
				mReader = null;
			}
			if (null != mBlueToothSocket) {
				mBlueToothSocket.close();
				mBlueToothSocket = null;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void displayData(String Tag, String text) {
		SimpleDateFormat formatter = new SimpleDateFormat("  HH:mm:ss");
		Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
		String str = formatter.format(curDate);
		if (text.length() > 0) {
			mEditReceive.setText(mEditReceive.getText() + "From:" + Tag + str
					+ "\n==>" + text + "\n");
		}
	}

	private boolean isLegal(String dataSendStr) {
		// TODO Auto-generated method stub
		if (dataSendStr.length() == 0)
			return false;
		for (int i = 0; i < dataSendStr.length(); i++) {
			if (!(((dataSendStr.charAt(i) >= '0') && (dataSendStr.charAt(i) <= '9'))
					|| ((dataSendStr.charAt(i) >= 'a') && (dataSendStr
							.charAt(i) <= 'f')) || ((dataSendStr.charAt(i) >= 'A') && (dataSendStr
					.charAt(i) <= 'F')))) {
				return false;
			}
		}
		return true;
	}

	private boolean isTabletDevice() {
		if (android.os.Build.VERSION.SDK_INT >= 11) { // honeycomb
			// test screen size, use reflection because isLayoutSizeAtLeast is
			// only available since 11
			Configuration con = getResources().getConfiguration();
			try {
				Method mIsLayoutSizeAtLeast = con.getClass().getMethod(
						"isLayoutSizeAtLeast", int.class);
				Boolean r = (Boolean) mIsLayoutSizeAtLeast.invoke(con,
						0x00000004); // Configuration.SCREENLAYOUT_SIZE_XLARGE
				return r;
			} catch (Exception x) {
				x.printStackTrace();
				return false;
			}
		}
		return false;
	}
	
	  private void scanLeDevice(final boolean enable) {
	        if (enable) {
	           //  Stops scanning after a pre-defined scan period.
	            mHandler.postDelayed(new Runnable() {
	                @Override
	                public void run() {
	                    mScanning = false;
	                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
	                }
	            }, 10000);

	            mScanning = true;
	           mBluetoothAdapter.startLeScan(mLeScanCallback);
	        } else {
	            mScanning = false;
	            mBluetoothAdapter.stopLeScan(mLeScanCallback);
	        }
	    }
	    
	    private BluetoothAdapter.LeScanCallback mLeScanCallback =
	            new BluetoothAdapter.LeScanCallback() {

	        @Override
	        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
	            runOnUiThread(new Runnable() {
	                @Override
	                public void run() {
	                	String str = device.getName();
	                	if(str == null) str = "UnknownDevice";
	                    if(!arrayForBlueToothDevice.contains(device) && (null != str && (-1 != str.indexOf("FT")))) {
	                	mAdapter.add(str);
	                	//displayData("mLeScanCallback" , "device.name = " + str);
	                	arrayForBlueToothDevice.add(device);
	                    }
	                }
	            });
	        }
	    };
	
}