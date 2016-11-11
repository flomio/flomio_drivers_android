package com.ft.mobile.reader.test;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ft.mobile.reader.CardReader;

import ft.mobile.bank.utils.Convection;

public class M1BinaryActivity extends Activity implements OnClickListener {
	Button BtnReadBlock;
	Button BtnUpdateBlock;
	
	EditText EditSectionNo;
	EditText EditBlockNo;
	EditText EditLength;
	EditText EditData;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_m1_binary);
		
		BtnReadBlock = (Button) findViewById(R.id.BtnReadBlock);
		BtnUpdateBlock = (Button) findViewById(R.id.BtnUpdateBlock);
		BtnReadBlock.setOnClickListener(this);
		BtnUpdateBlock.setOnClickListener(this);
		
		EditSectionNo = (EditText) findViewById(R.id.EditBinarySectionOn);
		EditBlockNo = (EditText) findViewById(R.id.EditBinaryBlockNo);
		EditLength = (EditText) findViewById(R.id.EditBinaryLength);
		EditData = (EditText) findViewById(R.id.EditBinaryData);
	}
	
	public void onClick(View view) {
		if(EditSectionNo.getEditableText()== null || EditBlockNo.getEditableText()== null
		|| "".equals(EditSectionNo.getText().toString().trim()) || "".equals(EditBlockNo.getText().toString().trim()) )
		{
			ShowToast("the block info connot be set into null");
			return;
		}
		if(EditLength.getEditableText()== null || "".equals(EditLength.getText().toString().trim()) )
		{
			ShowToast("the length connot be set into null");
			return;
		}
		try{
			int section = Integer.parseInt(EditSectionNo.getEditableText().toString());
			int block = Integer.parseInt(EditBlockNo.getEditableText().toString());
			int length = Integer.parseInt(EditLength.getEditableText().toString());
			
			if (view.getId() == R.id.BtnReadBlock) {
				byte []data = new byte[length];
				int rc = MainActivity.myCard.ReadBinary(section*4+block, data, length);
				if (rc == 0) {
					ShowToast("success");
				} else {
					ShowToast(String.format("errno:%x", rc));
				}
				EditData.setText(Convection.Bytes2HexString(data));
			}
			if (R.id.BtnUpdateBlock == view.getId()) {
				if(	EditData.getEditableText()== null
						|| "".equals(EditData.getText().toString().trim()))
				{
					ShowToast("the data cannot be set into null!");
					return;
				}
				if (length*2!=EditData.getEditableText().toString().length()) {
					Toast.makeText(this, "the data length is not correcet", Toast.LENGTH_LONG).show();
					return;
				}
				byte[] data = Convection.hexString2Bytes(EditData.getEditableText().toString());
				int rc =  MainActivity.myCard.UpdateBinary(section*4+block, data, length);
				if (rc == 0) {
					ShowToast("success");
				} else {
					ShowToast(String.format("errno:%x", rc));
				}
			}
		}catch(Exception expt)
		{
			ShowToast("please check your edit");
		}
	}
	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			Toast.makeText(M1BinaryActivity.this, msg.obj.toString(), Toast.LENGTH_LONG).show();
		}
		
	};
	private void ShowToast(String str) {
		Message msg = new Message();
		msg.obj = str;
		msg.setTarget(handler);
		msg.sendToTarget();
	} 
}
