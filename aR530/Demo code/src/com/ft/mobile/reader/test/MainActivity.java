package com.ft.mobile.reader.test;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.ft.mobile.reader.Card;
import com.ft.mobile.reader.Convert;

import ft.mobile.bank.utils.Convection;

public class MainActivity extends Activity implements OnClickListener,
		OnItemSelectedListener {

	private CheckBox cbACard;
	private CheckBox cbBCard;
	private CheckBox cbFelicaCard;
	private CheckBox cbTopazCard;

	private TextView tvLibVersion;
	private TextView tvDeviceID;
	private TextView tvDevUID;
	private TextView tvFirmwareVersion;
	private TextView tvConnectState;

	private Button btnGetLibVersion;
	private Button btnGetDeviceID;
	private Button btnGetDevUID;
	private Button btnGetFirmwareVersion;
	private Button btnTurnOffAutoBuzzer;
	private Button btnBuzzerBeep;
	private Button btnConnect;
	private Button btnDisconnect;
	private Button btnSendApdu;
	private Spinner spiApdu;
	private EditText etInputApdu;
	private Button customSendApdu;
	private Spinner customspiApdu;
	private EditText customInputApdu;

	private ArrayList<String> mApduList;
	private ArrayAdapter<String> arrAdpter;

	private ProgressDialog pdlg;
	private AlertDialog adlg;
/*
	private String[] mApdus = { "0084000008", "00A40000023f00",
			"00A4040005112233445566", "8010010000",
			"00A404000BA000000291A00000019102" };
*/
//	private String[] mApdus = { "0084000008", "00a404000a01020304050607080900",
//			"801003e3000000", "802000000003e3",
//			"801000f100",
//			"80200000f1"};
//	private String[] mApdus = { "00A4040008D156000001010301", "00A40000023F00",
//			"00e4010002a001", "00e00000186216820238008302a00185020000860800000000000000ff",
//			"00A4000002A001", "00E00000186216820201018302B001850200ff86080000000000000000",
//			 "00A4040008D156000001010301", "00A40000023F00",
//			"00D60000FF000102030405060708091011121314151617181920212223242526272829303132333435363738394041424344454647484950515253545556575859606162636465666768697071727374757677787980818283848586878889909192939495969798990001020304050607080910111213141516171819202122232425262728293031323334353637383940414243444546474849505152535455565758596061626364656667686970717273747576777879808182838485868788899091929394959697989900010203040506070809101112131415161718192021222324252627282930313233343536373839404142434445464748495051525354"};

//	private String[] mApdus = { "00a404000a01020304050607080900", 
//			"80200000EE", "80200000EF",
//			"80200000F0", "80200000FF",
//			"80300000FA123400000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000FF"};
	private String[] mApdus = { 
	"008400007F",
	"00A4040008D156000001010301",
	"00A40000023F00",
	"00e4010002a001",
	"00e00000186216820238008302a00185020000860800000000000000ff",
	"00A4000002A001",
	"00E00000186216820201018302B001850200ff86080000000000000000",
	"00D60000FA00010203040506070809101112131415161718192021222324252627282930313233343536373839404142434445464748495051525354555657585960616263646566676869707172737475767778798081828384858687888990919293949596979899000102030405060708091011121314151617181920212223242526272829303132333435363738394041424344454647484950515253545556575859606162636465666768697071727374757677787980818283848586878889909192939495969798990001020304050607080910111213141516171819202122232425262728293031323334353637383940414243444546474849",
	"00D60000FB0001020304050607080910111213141516171819202122232425262728293031323334353637383940414243444546474849505152535455565758596061626364656667686970717273747576777879808182838485868788899091929394959697989900010203040506070809101112131415161718192021222324252627282930313233343536373839404142434445464748495051525354555657585960616263646566676869707172737475767778798081828384858687888990919293949596979899000102030405060708091011121314151617181920212223242526272829303132333435363738394041424344454647484950",

	"00b00000FF",
	"00A40000023F00",
	"00A4000002A001",
	"00e4020002B001",
	"00A40000023F00",
	"00e4010002a001"
	};
//	private String[] mApdus = { "00a404000a01020304050607080900", "8010010200" };
	private static String[] mFelicaApdus1 = { "06010901018000" };
	private static String[] mFelicaApdus2 = { "06+Felica_ID+010901018000" };

	public static Card myCard;

	private static boolean isInit = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		myCard = new Card();
		myCard.Initial(this);

		initUI();

		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_HEADSET_PLUG);
		registerReceiver(receiver, filter);
	}

	private void initUI() {
		cbACard = (CheckBox) findViewById(R.id.cb_a_card);
		cbBCard = (CheckBox) findViewById(R.id.cb_b_card);
		cbFelicaCard = (CheckBox) findViewById(R.id.cb_felica_card);
		cbTopazCard = (CheckBox) findViewById(R.id.cb_topaz_card);
		tvLibVersion = (TextView) findViewById(R.id.tv_lib_version);
		tvDeviceID = (TextView) findViewById(R.id.tv_device_id);
		tvDevUID = (TextView) findViewById(R.id.tv_UID);
		tvFirmwareVersion = (TextView) findViewById(R.id.tv_firmware_version);
		tvConnectState = (TextView) findViewById(R.id.tv_connect_card);
		btnGetLibVersion = (Button) findViewById(R.id.btn_get_lib_version);
		btnGetDeviceID = (Button) findViewById(R.id.btn_get_device_id);
		btnGetDevUID = (Button) findViewById(R.id.btn_get_UID);
		btnGetFirmwareVersion = (Button) findViewById(R.id.btn_get_firmware_version);
		btnTurnOffAutoBuzzer = (Button) findViewById(R.id.btn_trun_off_autobuzzer);
		btnBuzzerBeep = (Button) findViewById(R.id.btn_buzzer_beep);
		btnConnect = (Button) findViewById(R.id.btn_nfc_connect);
		btnDisconnect = (Button) findViewById(R.id.btn_nfc_disconnect);
		btnSendApdu = (Button) findViewById(R.id.btn_send);
		etInputApdu = (EditText) findViewById(R.id.et_send_apdu);
		customSendApdu = (Button) findViewById(R.id.custom_send);
		customInputApdu = (EditText) findViewById(R.id.custom_send_apdu);
		tvConnectState.setText("not connected.");

		mApduList = new ArrayList<String>();
		arrAdpter = new ArrayAdapter<String>(MainActivity.this,
				android.R.layout.simple_spinner_item, mApduList);
		arrAdpter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// setSpinner();

		spiApdu = (Spinner) findViewById(R.id.spi_apdu);
		spiApdu.setAdapter(arrAdpter);
		spiApdu.setOnItemSelectedListener(this);

		
		customspiApdu = (Spinner) findViewById(R.id.custom_spi_apdu);
		customspiApdu.setAdapter(arrAdpter);
		customspiApdu.setOnItemSelectedListener(this);
		
		pdlg = new ProgressDialog(this);
		pdlg.setMessage("Waiting");
		pdlg.setCancelable(false);
		adlg = new AlertDialog.Builder(this).setPositiveButton("OK", null)
				.create();

		btnGetLibVersion.setOnClickListener(this);
		btnGetDeviceID.setOnClickListener(this);
		btnGetDevUID.setOnClickListener(this);
		btnGetFirmwareVersion.setOnClickListener(this);
		btnTurnOffAutoBuzzer.setOnClickListener(this);
		btnBuzzerBeep.setOnClickListener(this);
		btnConnect.setOnClickListener(this);
		btnDisconnect.setOnClickListener(this);
		btnSendApdu.setOnClickListener(this);
		customSendApdu.setOnClickListener(this);
	}

	public void onClick(View view) {

		if (view == btnGetLibVersion) {
			tvLibVersion.setText(myCard.GetLibVersion());
		} else if (view == btnGetDeviceID) {
			new GetDeviceIDTask().execute();
		} else if (view == btnGetDevUID) {
			new GetDevUIDTask().execute();
		} else if (view == btnGetFirmwareVersion) {
			new GetFirmwareVersionTask().execute();
		} else if (view == btnTurnOffAutoBuzzer) {
			new TurnOffAutoBuzzer().execute();
		} else if (view == btnBuzzerBeep) {
			new BuzzerBeep().execute();
		} else if (view == btnConnect) {
			new ConnectTask().execute();
		} else if (view == btnDisconnect) {
			new DisconnectTask().execute();
		} else if (view == btnSendApdu) {
			String str = etInputApdu.getText().toString().trim();
			Log.i("PKCS", "APDU:" + str);
			byte[] sendData = Convert.hexStringTo16(str);
			new TransmitTask().execute(sendData);
		}else if (view == customSendApdu) {
			 String str = customInputApdu.getText().toString().trim();
			 Log.i("PKCS", "customAPDU:" + str);
			 byte[] sendData = Convert.hexStringTo16(str);
			 new TransmitTaskcustom().execute(sendData);
		}

	}

	public void setSpinner() {
		mApduList.clear();
		for (String s : mApdus) {
			mApduList.add(s);
		}
		arrAdpter.notifyDataSetChanged();
		try {
			if (isInit) {
				etInputApdu.setText(spiApdu.getSelectedItem().toString());
			} else {
				isInit = true;
			}
		} catch (Exception ex) {

		}
	}
	
	public void setcustomSpinner() {
		mApduList.clear();
		for (String s : mApdus) {
			mApduList.add(s);
		}
		arrAdpter.notifyDataSetChanged();
		try {
			if (isInit) {
				customInputApdu.setText(customspiApdu.getSelectedItem().toString());
			} else {
				isInit = true;
			}
		} catch (Exception ex) {

		}
	}


	public void setFelicaSpinner() {
		mApduList.clear();
		for (String s : mFelicaApdus1) {
			mApduList.add(s);
		}
		arrAdpter.notifyDataSetChanged();
		try {
			if (isInit) {
				etInputApdu.setText(spiApdu.getSelectedItem().toString());
			} else {
				isInit = true;
			}
		} catch (Exception ex) {

		}
	}
	
	public void setcustomFelicaSpinner() {
		mApduList.clear();
		for (String s : mFelicaApdus2) {
			mApduList.add(s);
		}
		arrAdpter.notifyDataSetChanged();
		try {
			if (isInit) {
				customInputApdu.setText(customspiApdu.getSelectedItem().toString());
			} else {
				isInit = true;
			}
		} catch (Exception ex) {

		}
	}

	public void clearSpinner() {
		mApduList.clear();
		arrAdpter.notifyDataSetChanged();
		etInputApdu.setText("");
	}
	
	public String errContent(int errCode) {
		switch (errCode) {
		case Card.CODE_FAIL:
			return "Fail";

		case Card.CODE_DEVICE_NOT_AVAILABLE:
			return "device is not available";

		case Card.CODE_CARD_NOT_CONNECTED:
			return "card is not connected";

		case Card.CODE_DEVICE_COMM_ERROR:
			return "communication error";

		case Card.CODE_PARAM_ERROR:
			return "illegal parameters";

		case Card.CODE_TIMEOUT:
			return "timeout";

		default:
			return "unkown error " + errCode;
		}

	}

	/**
	 * Handshake
	 */
	class HandshakeTask extends AsyncTask<Void, Void, String> {

		@Override
		protected void onPreExecute() {
			pdlg.show();
		}

		@Override
		protected String doInBackground(Void... params) {
			try {
				Thread.sleep(1500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			for (int i = 1; i < 2; i++) {
				int rc = myCard.HandShake();
				if (rc == 0)
					return "Handshake success.";
			}

			return "Handshake fail.";
		}

		@Override
		protected void onPostExecute(String result) {
			pdlg.hide();
			adlg.setMessage(result);
			adlg.show();
		}

	}

	/**
	 * 获取设备ID
	 */
	class GetDeviceIDTask extends AsyncTask<Void, Void, String> {

		protected void onPreExecute() {
			pdlg.show();
		}

		protected String doInBackground(Void... params) {
			return myCard.GetDeviceID();
		}

		@Override
		protected void onPostExecute(String result) {
			pdlg.hide();
			tvDeviceID.setText(result);
		}

	}
	
	/**
	 * 获取设备UID
	 */
	class GetDevUIDTask extends AsyncTask<Void, Void, String> {

		protected void onPreExecute() {
			pdlg.show();
		}

		protected String doInBackground(Void... params) {
			return myCard.GetDevUID();
		}

		@Override
		protected void onPostExecute(String result) {
			pdlg.hide();
			tvDevUID.setText(result);
		}

	}

	/**
	 * 获取固件版本号
	 */
	class GetFirmwareVersionTask extends AsyncTask<Void, Void, String> {

		protected void onPreExecute() {
			pdlg.show();
		}

		protected String doInBackground(Void... params) {
			return myCard.GetFirmwareVersion();
		}

		@Override
		protected void onPostExecute(String result) {
			pdlg.hide();
			tvFirmwareVersion.setText(result);
		}

	}
	
	/**
	 * 关闭蜂鸣器自动响应
	 */
	class TurnOffAutoBuzzer extends AsyncTask<Void, Void, String> {

		protected void onPreExecute() {
			pdlg.show();
		}

		protected String doInBackground(Void... params) {
			if ( myCard.TrunOffAutoBuzzer() == 0)
			{
				return "Trun off automatic buzzer successed !";
			}
			return "Trun off automatic buzzer failed !";
		}

		@Override
		protected void onPostExecute(String result) {
			pdlg.hide();
			adlg.setMessage(result);
			adlg.show();
		}

	}
	
	/**
	 * 控制蜂鸣器发声
	 */
	class BuzzerBeep extends AsyncTask<Void, Void, String> {

		protected void onPreExecute() {
			pdlg.show();
		}

		protected String doInBackground(Void... params) {
			if ( myCard.BuzzerBeep() == 0)
			{
				return "buzzer beep successed !";
			}
			return "buzzer beep failed !";
		}

		@Override
		protected void onPostExecute(String result) {
			pdlg.hide();
			adlg.setMessage(result);
			adlg.show();
		}

	}

	/**
	 * 寻卡
	 */
	class ConnectTask extends AsyncTask<Void, Void, Integer> {

		@Override
		protected void onPreExecute() {
			pdlg.show();
		}

		@Override
		protected Integer doInBackground(Void... params) {
			ArrayList<Card.CARD_TYPE> list = new ArrayList<Card.CARD_TYPE>();

			if (cbACard.isChecked())
				list.add(Card.CARD_TYPE.A_CARD);
			if (cbBCard.isChecked())
				list.add(Card.CARD_TYPE.B_CARD);
			if (cbFelicaCard.isChecked())
				list.add(Card.CARD_TYPE.Felica_CARD);
			if (cbTopazCard.isChecked())
				list.add(Card.CARD_TYPE.Topaz_CARD);

			Card.CARD_TYPE[] types = new Card.CARD_TYPE[list.size()];
			list.toArray(types);

			int rc = myCard.FTNFC_connect(types, 5);

			return rc;
		}

		@Override
		protected void onPostExecute(Integer result) {
			pdlg.hide();
			String str = "";
			String info = "";

			if (result == 0) {

				int type = myCard.FTNFC_cardType();
				Log.d("xww",
						"connected card type : " + String.format("0x%x", type));

				byte[] cardInfoData = myCard.GetCardInfoData();
				if (cardInfoData != null) {
					info = Convert.Bytes2HexString(cardInfoData);
				}
				
				str = "Card Type : ";

				switch (type) {
				case Card.CARD_NXP_DESFIRE_EV1:
					str += "A card";
					setSpinner();
					str += "\nCard Info :";
					str += "\nSAK : " + info.substring(2, 4);
					str += "\nUID : " + info.substring(6, info.length());
					break;

				case Card.CARD_NXP_TYPE_B:
					str += "B card";
					setSpinner();
					str += "\nCard Info :";
					str += "\nProto_Type : " + info.substring(2, 4);
					str += "\nPUPI : " + info.substring(6, info.length());
					break;

				case Card.CARD_NXP_FELICA:
					str += "Felica card";
					//setFelicaSpinner();
					//setcustomFelicaSpinner();
					etInputApdu.setText("06010901018000");
					String apdu = "00000f06" + info.substring(6, 22) + "010901018000";
					customInputApdu.setText(apdu);
					str += "\nCard Info :";
					str += "\nFelica_ID : " + info.substring(6, 22);
					str += "\nPad_ID : " + info.substring(22, 38);
					break;

				case Card.CARD_INNOVISION_TOPAZ:
					str += "Topaz card";
					setSpinner();
					str += "\nCard Info : " ;
					str += "\natqa : " + info.substring(2, 6);
					str += "\nID : " + info.substring(6, info.length());
					break;

				case Card.CARD_NXP_MIFARE_1K:
					str += "Mifare 1K";
					setSpinner();
					str += "\nCard Info :";
					str += "\nSAK : " + info.substring(2, 4);
					str += "\nUID : " + info.substring(6, info.length());
					startActivity(new Intent(MainActivity.this,
							M1Activity.class));
					break;

				case Card.CARD_NXP_MIFARE_4K:
					str += "Mifare 4K";
					setSpinner();
					str += "\nCard Info :";
					str += "\nSAK : " + info.substring(2, 4);
					str += "\nUID : " + info.substring(6, info.length());
					startActivity(new Intent(MainActivity.this,
							M1Activity.class));
					break;

				case Card.CARD_NXP_MIFARE_UL:
					str += "Mifare UL";
					setSpinner();
					str += "\nCard Info :";
					str += "\nSAK : " + info.substring(2, 4);
					str += "\nUID : " + info.substring(6, info.length());
					startActivity(new Intent(MainActivity.this,
							M1Activity.class));
					break;

				case Card.CARD_NXP_MIFARE_UL_C:
					str += "Mifare UL C";
					setSpinner();
					str += "\nCard Info :";
					str += "\nSAK : " + info.substring(2, 4);
					str += "\nUID : " + info.substring(6, info.length());
					startActivity(new Intent(MainActivity.this,
							M1Activity.class));
					break;

				default:
					str += "unkown card type : " + String.format("%2x", type);
					break;
				}

			} else {
				str = errContent(result);
			}

			tvConnectState.setText(str);
		}

	}

	/**
	 * 断开连接
	 */
	class DisconnectTask extends AsyncTask<Void, Void, String> {

		@Override
		protected void onPreExecute() {
			pdlg.show();
		}

		@Override
		protected String doInBackground(Void... params) {
			int rc = myCard.FTNFC_disconnect();
			if (rc == 0) {
				return "disconnect ok";
			} else {
				return errContent(rc);
			}
		}

		@Override
		protected void onPostExecute(String result) {
			pdlg.hide();
			tvConnectState.setText("not connected.");
			adlg.setMessage(result);
			adlg.show();
		}
	}

	/**
	 * Send APDU
	 */
	class TransmitTask extends AsyncTask<byte[], Void, String> {

		@Override
		protected void onPreExecute() {
			pdlg.show();
		}

		@Override
		protected String doInBackground(byte[]... sendData) {
			byte respData[] = new byte[1024];
			int recvLen = myCard.FTNFC_transmitCmd(sendData[0],  respData);
			return "Response:\n" + Convection.Bytes2HexString(respData, recvLen);
//			return myCard.FTNFC_transmitCmd(sendData[0]);

		}

		@Override
		protected void onPostExecute(String result) {
			pdlg.hide();
			adlg.setMessage(result);
			adlg.show();
		}
	}

	
	class TransmitTaskcustom extends AsyncTask<byte[], Void, String> {

		@Override
		protected void onPreExecute() {
			pdlg.show();
		}

		@Override
		protected String doInBackground(byte[]... sendData) {
			byte respData[] = new byte[1024];
			int recvLen = myCard.FTNFC_transmitcustomCmd(sendData[0],  respData);
			return "Response:\n" + Convection.Bytes2HexString(respData, recvLen);
			
		}

		@Override
		protected void onPostExecute(String result) {
			pdlg.hide();
			adlg.setMessage(result);
			adlg.show();
		}
	}
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			if (intent.hasExtra("state")) {
				if (intent.getIntExtra("state", 0) == 0) {
					tvConnectState.setText("not connected.");
					clearSpinner();
					adlg.setMessage("Device is not connected");
					adlg.show();
				} else if (intent.getIntExtra("state", 0) == 1) {
					adlg.hide();
					new HandshakeTask().execute();
				}
			}
		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (myCard != null) {
			myCard.Release();
			myCard = null;
		}
		unregisterReceiver(receiver);
	}

	public void onItemSelected(AdapterView<?> adapterview, View view, int i,
			long l) {
		if (adapterview == spiApdu) {
			etInputApdu.setText(spiApdu.getSelectedItem().toString());
		}else if (adapterview == customspiApdu) {
			customInputApdu.setText(customspiApdu.getSelectedItem().toString());
		}
	}

	public void onNothingSelected(AdapterView<?> adapterview) {
	}

}
