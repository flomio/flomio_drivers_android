package com.ft.mobile.reader;

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
import com.ft.mobile.reader.JKeyInt;
import com.ft.mobile.reader.R;
import com.ft.mobile.reader.M1Activity;
import com.ft.mobile.reader.MainActivity;

import ft.mobile.bank.utils.Convection;

public class MainActivity extends Activity implements OnClickListener,
		OnItemSelectedListener {

	private CheckBox cbACard;
	private CheckBox cbBCard;
	private CheckBox cbFelicaCard;
	private CheckBox cbTopazCard;

	private TextView tvLibVersion;
	private TextView tvDeviceID;
	private TextView tvFirmwareVersion;
	private TextView tvConnectState;

	private Button btnGetLibVersion;
	private Button btnGetDeviceID;
	private Button btnGetFirmwareVersion;
	private Button btnConnect;
	private Button btnDisconnect;
	private Button btnSendApdu;
	private Spinner spiApdu;
	private EditText etInputApdu;

	private ArrayList<String> mApduList;
	private ArrayAdapter<String> arrAdpter;

	private ProgressDialog pdlg;
	private AlertDialog adlg;

	private String[] mApdus = { "0084000008", "00A40000023f00",
			"00A4040005112233445566", "8010010000",
			"00A404000BA000000291A00000019102", "00A4040006112233445501", "00010000000100016e0967bfaead620f0c246db4a52a20e8777f2d4d24a78fd9929d1aa7e556501f774008537b375bbf2e66834b5138897e41dbb73ab9f171d825f8304f7788ba0a7f3d45fc005d8d702077afcb8ff72cb98893ca6e51ddb01ba84036bb135083c508530bb1b1d85bccfb228e5486a6aec305e966dc535e594cb68abb7e8725f9f0f49c38fef69c79ff92943d9f2a231c15762993f186ac19361141b3a60fabdb59b6ef8492efef60edcc63adca215771e96b9fcc9d777fbf588af57f74eafcf3f69b3824dbb54629ac6bc5cc3f27ca197dcfdfaaa31946cbb5a09028791521071d558f5168edce7d13ec54c5c5995a5d253fe3ea8ce37c4d6d5e388332495395", "00020000", "0001000080f22b672d5c76a1653d7ed0478fdcf7542334f77a7b0c108b74dea5ee276b3f951253d52e73e34b9ef52e38ab8e5fecf8a84db2377f50529aca54da8a9d2b9e9c8e287ec117e967bd3b741dda6c8637ddad276b39f4820b83d2f4d0265563d7582ed1e94c0f408521da0025d613a006bf3b33946c465b89677c74edb81635f5c3", "00020000" };

	private static String[] mFelicaApdus = { "06010901018000" };

	public static Card myCard;

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
		tvFirmwareVersion = (TextView) findViewById(R.id.tv_firmware_version);
		tvConnectState = (TextView) findViewById(R.id.tv_connect_card);
		btnGetLibVersion = (Button) findViewById(R.id.btn_get_lib_version);
		btnGetDeviceID = (Button) findViewById(R.id.btn_get_device_id);
		btnGetFirmwareVersion = (Button) findViewById(R.id.btn_get_firmware_version);
		btnConnect = (Button) findViewById(R.id.btn_nfc_connect);
		btnDisconnect = (Button) findViewById(R.id.btn_nfc_disconnect);
		btnSendApdu = (Button) findViewById(R.id.btn_send);
		etInputApdu = (EditText) findViewById(R.id.et_send_apdu);
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

		pdlg = new ProgressDialog(this);
		pdlg.setMessage("Waiting");
		pdlg.setCancelable(false);
		adlg = new AlertDialog.Builder(this).setPositiveButton("OK", null)
				.create();

		btnGetLibVersion.setOnClickListener(this);
		btnGetDeviceID.setOnClickListener(this);
		btnGetFirmwareVersion.setOnClickListener(this);
		btnConnect.setOnClickListener(this);
		btnDisconnect.setOnClickListener(this);
		btnSendApdu.setOnClickListener(this);
	}

	public void onClick(View view) {

		if (view == btnGetLibVersion) {
			tvLibVersion.setText(myCard.GetLibVersion());
		} else if (view == btnGetDeviceID) {
			new GetDeviceIDTask().execute();
		} else if (view == btnGetFirmwareVersion) {
			new GetFirmwareVersionTask().execute();
		} else if (view == btnConnect) {
			new ConnectTask().execute();
		} else if (view == btnDisconnect) {
			new DisconnectTask().execute();
		} else if (view == btnSendApdu) {
			String str = etInputApdu.getText().toString().trim();
			byte[] sendData = Convert.hexStringTo16(str);
			new TransmitTask().execute(sendData);
		}

	}

	public void setSpinner() {
		mApduList.clear();
		for (String s : mApdus) {
			mApduList.add(s);
		}
		arrAdpter.notifyDataSetChanged();
	}

	public void setFelicaSpinner() {
		mApduList.clear();
		for (String s : mFelicaApdus) {
			mApduList.add(s);
		}
		arrAdpter.notifyDataSetChanged();
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

			for (int i = 1; i < 3; i++) {
				int rc = myCard.HandShake(MainActivity.this);
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
			byte[] recv = new byte[1024];
			JKeyInt len = new JKeyInt();
			int rc = myCard.GetDeviceID(recv, len);

			if (rc == 0) {
				return Convection.Bytes2HexString(recv, len.value);
			} else {
				return errContent(rc);
			}
		}

		@Override
		protected void onPostExecute(String result) {
			pdlg.hide();
			tvDeviceID.setText(result);
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
			byte[] recv = new byte[1024];
			JKeyInt len = new JKeyInt();
			int rc = myCard.GetFirmwareVersion(recv, len);

			if (rc == 0) {
				return Convection.Bytes2HexString(recv, len.value);
			} else {
				return errContent(rc);
			}
		}

		@Override
		protected void onPostExecute(String result) {
			pdlg.hide();
			tvFirmwareVersion.setText(result);
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

			if (result == 0) {

				int type = myCard.FTNFC_cardType();
				Log.d("xww",
						"connected card type : " + String.format("0x%x", type));

				str = "Card Type : ";

				switch (type) {
				case Card.CARD_NXP_DESFIRE_EV1:
					str += "A card";
					setSpinner();
					break;

				case Card.CARD_NXP_TYPE_B:
					str += "B card";
					setSpinner();
					break;

				case Card.CARD_NXP_FELICA:
					str += "Felica card";
					setFelicaSpinner();
					break;

				case Card.CARD_INNOVISION_TOPAZ:
					str += "Topaz card";
					setSpinner();
					break;

				case Card.CARD_NXP_MIFARE_1K:
					str += "Mifare 1K";
					setSpinner();
					startActivity(new Intent(MainActivity.this,
							M1Activity.class));
					break;

				case Card.CARD_NXP_MIFARE_4K:
					str += "Mifare 4K";
					setSpinner();
					startActivity(new Intent(MainActivity.this,
							M1Activity.class));
					break;

				case Card.CARD_NXP_MIFARE_UL:
					str += "Mifare UL";
					setSpinner();
					startActivity(new Intent(MainActivity.this,
							M1Activity.class));
					break;

				case Card.CARD_NXP_MIFARE_UL_C:
					str += "Mifare UL C";
					setSpinner();
					startActivity(new Intent(MainActivity.this,
							M1Activity.class));
					break;

				default:
					str += "unkown card type : " + String.format("%2x", type);
					break;
				}

				byte[] cardInfoData = myCard.GetCardInfoData();
				if (cardInfoData != null)
					str = str + "\nCard Info : "
							+ Convert.Bytes2HexString(cardInfoData);

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
			JKeyInt respDataLen = new JKeyInt();
			int rc = myCard.FTNFC_transmitCmd(sendData[0], respData,
					respDataLen);
			if (rc == 0) {
				return "Response:\n"
						+ Convection.Bytes2HexString(respData,
								respDataLen.value);
			} else {
				return errContent(rc);
			}
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
					myCard.OnPullHeadSet();
				} else if (intent.getIntExtra("state", 0) == 1) {
					myCard.OnInsertHeadSet();
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
		}
	}

	public void onNothingSelected(AdapterView<?> adapterview) {
	}

}
