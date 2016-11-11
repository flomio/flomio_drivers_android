package com.ft.mobile.reader.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ft.mobile.reader.CardReader;

import ft.mobile.bank.utils.Convection;

public class M1Activity extends Activity implements View.OnClickListener{
	Button AutherBtn;
	Button ValueBtn;
	Button BinaryBtn;
	EditText SectionNo;
	EditText Key;
	EditText KeyType;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_m1);
		AutherBtn = (Button)findViewById(R.id.btn_auther);
		ValueBtn = (Button)findViewById(R.id.btn_value);
		BinaryBtn = (Button) findViewById(R.id.btn_binary);
		SectionNo = (EditText) findViewById(R.id.EditSection);
		Key = (EditText) findViewById(R.id.EditKey);
		KeyType = (EditText) findViewById(R.id.EditKeyType);
		
		AutherBtn.setOnClickListener(this);
		ValueBtn.setOnClickListener(this);
		BinaryBtn.setOnClickListener(this);
	}
	
	public void onClick(View view) {
		if (view.getId() == R.id.btn_auther) {
			new Thread(){

				@Override
				public void run() {
					try{
					int sectionNo = 4*Integer.parseInt(SectionNo.getEditableText().toString());
					int keytype = KeyType.getEditableText().toString().charAt(0) == 'A'?0x60:0x61;
					byte key[] = Convection.hexString2Bytes(Key.getEditableText().toString());
					int rc = MainActivity.myCard.GeneralAuthenticate(sectionNo, keytype, key);
					if (rc != 0)
						ShowToast(String.format("errno:%x", rc));
					else 
						ShowToast("Sucess");
					}
					catch (Exception expt){
						ShowToast("argument error");
					}
				}
				
			}.start();
			
		}
		if (view.getId() == R.id.btn_value) {
			startActivity(new Intent(M1Activity.this, M1ValueActivity.class));
		}
		if (view.getId() == R.id.btn_binary) {
			startActivity(new Intent(M1Activity.this, M1BinaryActivity.class));
		}
	}
	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			Toast.makeText(M1Activity.this, msg.obj.toString(), Toast.LENGTH_LONG).show();
		}
		
	};
	private void ShowToast(String str) {
		Message msg = new Message();
		msg.obj = str;
		msg.setTarget(handler);
		msg.sendToTarget();
	} 
}
