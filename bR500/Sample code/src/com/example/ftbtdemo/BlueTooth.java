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
	
	//Becuase of some mobile phone's bluetooth chip maximum supported packet length is 272.
	//Greater than Max_Packet_length , plsease subcontracting .
	private final int Max_Packet_length = 272;
	
	private final int Max_Flash_length = 272;
	// 控件、
	private Button mSend;
	private Button mList;
	private Button mConnect;
	private Button mDisConnect;
	private Button mPowerOn;
	private Button mPowerOff;
	private Button BgetUserID;
	private Button BgenUserID;
	private Button BeraseUserID;
	private EditText seedData;// 发送数据
	private Button BgetHardID;
	private Button mExit;
	private Button mclearReceiveData;

	private Button mGetStatus;
	private Button BGetVersion;
	private EditText mEditSend;// 发送数据

	private Button BReadFlash;
	private Button BWriteFlash;
	private EditText EFlash;
	private EditText Eoffset;
	private EditText Elength;

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

	private ReadThread mreadThread;

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
		BgetHardID = (Button) findViewById(R.id.BGetSerial);
		BgetHardID.setOnClickListener(this);
		
		BgetUserID = (Button) findViewById(R.id.BgetUserID);
		BgetUserID.setOnClickListener(this);
		BgenUserID = (Button) findViewById(R.id.BgenUserID);
		BgenUserID.setOnClickListener(this);
		BeraseUserID = (Button) findViewById(R.id.BeraseUserID);
		BeraseUserID.setOnClickListener(this);
		seedData = (EditText) findViewById(R.id.ESeedData);
		seedData.setText("FFFFFFFF");
		
		BReadFlash = (Button) findViewById(R.id.BReadFlash);
		BReadFlash.setOnClickListener(this);
		BWriteFlash = (Button) findViewById(R.id.BWriteFlash);
		BWriteFlash.setOnClickListener(this);
		EFlash = (EditText) findViewById(R.id.EWriteFlash);
		EFlash.setText("00010203040506070809");
		Eoffset = (EditText) findViewById(R.id.Eoffset);
		Eoffset.setText("00");
		Elength = (EditText) findViewById(R.id.Elength);
		Elength.setText("00");
		
		mGetStatus = (Button) findViewById(R.id.BGetStatus);
		mGetStatus.setOnClickListener(this);
		BGetVersion = (Button) findViewById(R.id.BGetVersion);
		BGetVersion.setOnClickListener(this);
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
		
		//To monitor reader connection status
		//#1, register card status monitoring
		BlueToothReceiver.registerCardStatusMonitoring(mHandler);
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
					mPowerOn.setEnabled(true);
					displayData("IFD", "card present");
					break;
				case DK.CARD_UNKNOWN:
					displayData("IFD", "card unknown");
					break;
				case DK.IFD_COMMUNICATION_ERROR:
					displayData("IFD", "IFD error");
					break;
				}
				//To monitor reader bluetooth connection status
				//#2, get the reader bluetooth status, add put your handle code here
				case BlueToothReceiver.BLETOOTH_STATUS:
					switch (msg.arg1) {
					
						//Once reader bluetooth connected, then do your operation here
						case BlueToothReceiver.BLETOOTH_CONNECT:

						break;
					case BlueToothReceiver.BLETOOTH_DISCONNECT:
						//Once bluetooth disconnection, change UI
						Log.d("---->" ,"------<disconnetced from broadcast");
						mReader.readerClose();
						stat_disconnect();
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
			this.mList.setEnabled(false);

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
			// mReader.PowerOff();
			mReader.readerClose();
			stat_disconnect();
		} else if (v == mSend) {
			// Best to do in the thread
//			mreadThread = new ReadThread();
//			mreadThread.start();
			
			
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
				if (tmp.length > 272)
				{
					displayData("Eroor", "Send data is greater than Max_Packet_length!");
					return;
				}
				displayData("Send", str);
				byte[] rev = new byte[Max_Packet_length]; 
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
		} else if (v == BgetHardID) {
			int ret = 0;
			byte[] recvBuf = new byte[64];
			int[] recvBufLen = new int[1];
			try {
				ret = mReader.getHardID(recvBuf, recvBufLen);
			} catch (FtBlueReadException e) {
				displayData("getHardID(SerialNum)", e.toString());
			}
			if (ret == DK.RETURN_SUCCESS) {
				displayData("getHardID(SerialNum)",
						Tool.byte2HexStr(recvBuf, recvBufLen[0]));
			} else {
				displayData("getHardID(SerialNum)",
						"Faile Error " + Integer.toHexString(ret));
			}
		} else if (v == BgenUserID) {
			String str = seedData.getText().toString();
			if (!isLegal(str)) {
				new AlertDialog.Builder(BlueTooth.this)
						.setTitle("prompt")
						.setMessage("please input data as '0~9' 'a~f' 'A~F'")
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										seedData.setText("");
									}
								}).show();
			} else {
				byte[] tmp = Tool.hexStringToBytes(str);
				displayData("GenUID SeedData", str);
				int ret = DK.RETURN_SUCCESS;
				try {
					ret = mReader.genUserID(tmp, tmp.length);
				} catch (FtBlueReadException e) {
					displayData("genUserID", e.toString());
				}
				if (ret == DK.RETURN_SUCCESS) {
					displayData("genUserID", "Success");
				} else {
					displayData("genUserID",
							"Faile Error " + Integer.toHexString(ret));
				}
			}
		} else if (v == BgetUserID) {
			int ret = 0;
			byte[] recvBuf = new byte[64];
			int[] recvBufLen = new int[1];
			try {
				ret = mReader.getUserID(recvBuf, recvBufLen);
			} catch (FtBlueReadException e) {
				displayData("getUserID", e.toString());
			}
			if (ret == DK.RETURN_SUCCESS) {
				displayData("getUserID",
						Tool.byte2HexStr(recvBuf, recvBufLen[0]));
			} else {
				displayData("getUserID",
						"Faile Error " + Integer.toHexString(ret));
			}
		} else if (v == BeraseUserID) {
			String str = seedData.getText().toString();
			if (!isLegal(str)) {
				new AlertDialog.Builder(BlueTooth.this)
						.setTitle("prompt")
						.setMessage("please input data as '0~9' 'a~f' 'A~F'")
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										seedData.setText("");
									}
								}).show();
			} else {
				byte[] tmp = Tool.hexStringToBytes(str);
				displayData("EraseUID SeedData", str);
				int ret = DK.RETURN_SUCCESS;
				try {
					ret = mReader.earseUserID(tmp, tmp.length);
				} catch (FtBlueReadException e) {
					displayData("earseUserID", e.toString());
				}
				if (ret == DK.RETURN_SUCCESS) {
					displayData("erasUserID", "Success");
				} else {
					displayData("erasUserID",
							"Faile Error " + Integer.toHexString(ret));
				}
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
				mPowerOn.setEnabled(true);
				displayData("GetStatus", "card present");
			} else {
				displayData("GetStatus", "card unknow");
			}
		} else if (v == BGetVersion) {
			int ret = 0;
			byte[] recvBuf = new byte[64];
			int[] recvBufLen = new int[1];
			try {
				ret = mReader.getVersion(recvBuf, recvBufLen);
			} catch (FtBlueReadException e) {
				displayData("getVersion", e.toString());
			}
			if (ret == DK.RETURN_SUCCESS) {
				displayData("getVersion", "V" + recvBuf[0] + "." + recvBuf[1]);
			}
		} else if (v == mclearReceiveData) {
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
		} else if (v == BReadFlash) {
			int length = 0;
			int offset = 0;
			byte[] recvBuf = new byte[Max_Flash_length];
			int ret = DK.RETURN_SUCCESS;
			String str = Eoffset.getText().toString();
			if (!isLegal(str)) {
				new AlertDialog.Builder(BlueTooth.this)
						.setTitle("prompt")
						.setMessage("please input data as '0~9' 'a~f' 'A~F'")
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										seedData.setText("");
									}
								}).show();
				return;
			} else {
				byte[] bytebuf = Tool.hexStringToBytes(str);
				for (int i = 0; i < bytebuf.length; i++)
					offset += bytebuf[i] & 0xff;
			}
			str = Elength.getText().toString();
			if (!isLegal(str)) {
				new AlertDialog.Builder(BlueTooth.this)
						.setTitle("prompt")
						.setMessage("please input data as '0~9' 'a~f' 'A~F'")
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										seedData.setText("");
									}
								}).show();
				return;
			} else {
				byte[] bytebuf = Tool.hexStringToBytes(str);
				for (int i = 0; i < bytebuf.length; i++)
					length += bytebuf[i] & 0xff;
			}
			if (offset + length > 255) {
				new AlertDialog.Builder(BlueTooth.this)
						.setTitle("prompt")
						.setMessage(
								"Illegal address (offset add length should be less than 255)")
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										seedData.setText("");
									}
								}).show();
			}
			EFlash.setText("");
			try {
				ret = mReader.readFlash(recvBuf, offset, length);
			} catch (FtBlueReadException e) {
				displayData("readFlash", e.toString());
			}
			if (ret == DK.RETURN_SUCCESS) {
				displayData("readFlash", "Success read length " + length);
				EFlash.setText(Tool.byte2HexStr(recvBuf, length));
			} else {
				displayData("readFlash",
						"Faile Error " + Integer.toHexString(ret));
			}
		} else if (v == BWriteFlash) {
			int length = 0;
			int offset = 0;
			byte[] recvBuf = new byte[Max_Flash_length];
			int ret = DK.RETURN_SUCCESS;
			String str = Eoffset.getText().toString();
			if (!isLegal(str)) {
				new AlertDialog.Builder(BlueTooth.this)
						.setTitle("prompt")
						.setMessage("please input data as '0~9' 'a~f' 'A~F'")
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										seedData.setText("");
									}
								}).show();
				return;
			} else {
				byte[] bytebuf = Tool.hexStringToBytes(str);
				for (int i = 0; i < bytebuf.length; i++)
					offset += bytebuf[i] & 0xff;
			}
			str = EFlash.getText().toString();
			if (!isLegal(str)) {
				new AlertDialog.Builder(BlueTooth.this)
						.setTitle("prompt")
						.setMessage("please input data as '0~9' 'a~f' 'A~F'")
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										seedData.setText("");
									}
								}).show();
			} else {

				byte[] tmp = Tool.hexStringToBytes(str);
				if (offset + tmp.length > 255) {
					new AlertDialog.Builder(BlueTooth.this)
							.setTitle("prompt")
							.setMessage(
									"Illegal address (offset add flash data length should be less than 255)")
							.setPositiveButton("OK",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											seedData.setText("");
										}
									}).show();
				}
				try {
					ret = mReader.writeFlash(tmp, offset, tmp.length);
				} catch (FtBlueReadException e) {
					displayData("writeFlash", e.toString());
				}
				if (ret == DK.RETURN_SUCCESS) {
					displayData("writeFlash", "Success");
				} else {
					displayData("writeFlash",
							"Faile Error " + Integer.toHexString(ret));
				}
			}
		}
	}

	private void stat_poweroff() {
		mPowerOn.setEnabled(false);
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
		BGetVersion.setEnabled(false);
		BWriteFlash.setEnabled(false);
		BReadFlash.setEnabled(false);
		BgetHardID.setEnabled(false);
		BgetUserID.setEnabled(false);
		BeraseUserID.setEnabled(false);
		BgenUserID.setEnabled(false);
	}

	private void stat_poweron() {
		mPowerOn.setEnabled(false);
		mPowerOff.setEnabled(true);
		mSend.setEnabled(true);

	}

	private void stat_connect() {
		mPowerOn.setEnabled(false);
		mPowerOff.setEnabled(false);
		mGetStatus.setEnabled(true);
		mSend.setEnabled(false);
		mConnect.setEnabled(false);
		mDisConnect.setEnabled(true);
		BGetVersion.setEnabled(true);
		BWriteFlash.setEnabled(true);
		BReadFlash.setEnabled(true);
		BgetHardID.setEnabled(true);
		BgetUserID.setEnabled(true);
		BeraseUserID.setEnabled(true);
		BgenUserID.setEnabled(true);
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
		if (dataSendStr.length() == 0 || dataSendStr.length() % 2 == 1)
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
			// Stops scanning after a pre-defined scan period.
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					mScanning = false;
					mList.setEnabled(true);
					mBluetoothAdapter.stopLeScan(mLeScanCallback);
				}
			}, 10000);

			mScanning = true;
			mList.setEnabled(true);
			mBluetoothAdapter.startLeScan(mLeScanCallback);
		} else {
			mScanning = false;
			mList.setEnabled(true);
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
		}
	}

	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice device, int rssi,
				byte[] scanRecord) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					String str = device.getName();
					if (str == null)
						str = "UnknownDevice";
					if (!arrayForBlueToothDevice.contains(device)
							&& (null != str && (-1 != str.indexOf("FT")))) {
						mAdapter.add(str);
						// displayData("mLeScanCallback" , "device.name = " +
						// str);
						mList.setEnabled(true);
						arrayForBlueToothDevice.add(device);
					}
				}
			});
		}
	};

	class ReadThread extends Thread {

		public void run() {

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

				if (tmp.length > 272)
				{
					return;
				}
				byte[] rev = new byte[1024];
				int[] length = new int[2];
				int ret = DK.RETURN_SUCCESS;
				try {
					ret = mReader.transApdu(tmp.length, tmp, length, rev);

				} catch (FtBlueReadException e) {
					// TODO Auto-generated catch block

				}
			}
		}

		public void cancel() {
		}
	}

}