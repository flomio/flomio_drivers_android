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

public class M1ValueActivity extends Activity implements OnClickListener {
	Button BtnInitial;
	Button BtnReadValue;
	Button BtnStoreValue;
	Button BtnIncrement;
	Button BtnDecrement;

	EditText EditValue;
	EditText EditSectionNo;
	EditText EditBlockNo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_m1_value);
		BtnInitial = (Button) findViewById(R.id.InitialBlock);
		BtnReadValue = (Button) findViewById(R.id.readvalue);
		BtnStoreValue = (Button) findViewById(R.id.storevalue);
		BtnIncrement = (Button) findViewById(R.id.increment);
		BtnDecrement = (Button) findViewById(R.id.decrement);

		EditValue = (EditText) findViewById(R.id.EditValue);
		EditSectionNo = (EditText) findViewById(R.id.EditSectionNo);
		EditBlockNo = (EditText) findViewById(R.id.EditBlockNo);

		BtnInitial.setOnClickListener(this);
		BtnReadValue.setOnClickListener(this);
		BtnStoreValue.setOnClickListener(this);
		BtnIncrement.setOnClickListener(this);
		BtnDecrement.setOnClickListener(this);
	}

	public void onClick(View view) {
		if (view.getId() == R.id.InitialBlock) {
			try{
				final int section = Integer.parseInt(EditSectionNo
						.getEditableText().toString());
				final int block = Integer.parseInt(EditBlockNo.getEditableText()
						.toString());
				int rc =  MainActivity.myCard.ClassicBlockInitial(section * 4 + block);
				if (rc == 0) {
					ShowToast("success");
				} else {
					ShowToast(String.format("errno:%x", rc));
				}
			}
			catch(Exception expt)
			{
				ShowToast(String.format("value or block info cannot be set to null"));
			}
		}
		if (view.getId() == R.id.readvalue) {
			try{
				int section = Integer.parseInt(EditSectionNo.getEditableText()
						.toString());
				int block = Integer.parseInt(EditBlockNo.getEditableText()
						.toString());
				int[] valueAmount = new int[1];
				int rc =  MainActivity.myCard.ClassicReadValue(section * 4 + block, valueAmount);
				if (rc == 0) {
					ShowToast("success");
				} else {
					ShowToast(String.format("errno:%x", rc));
				}
				EditValue.setText(Integer.toString(valueAmount[0]));
			}
			catch(Exception expt)
			{
				ShowToast(String.format("value or block info cannot be set to null"));
			}
		}
		if (view.getId() == R.id.storevalue) {
			try{
				int value = Integer
						.parseInt(EditValue.getEditableText().toString());
				int section = Integer.parseInt(EditSectionNo.getEditableText()
						.toString());
				int block = Integer.parseInt(EditBlockNo.getEditableText()
						.toString());
				int rc =  MainActivity.myCard.ClassicStoreBlock(section * 4 + block, value);
				if (rc == 0) {
					ShowToast("success");
				} else {
					ShowToast(String.format("errno:%x", rc));
				}
			}
			catch(Exception expt)
			{
				ShowToast(String.format("value or block info cannot be set to null"));
			}
		}
		if (view.getId() == R.id.increment) {
			try
			{
				int value = Integer
						.parseInt(EditValue.getEditableText().toString());
				int section = Integer.parseInt(EditSectionNo.getEditableText()
						.toString());
				int block = Integer.parseInt(EditBlockNo.getEditableText()
						.toString());
				int rc =  MainActivity.myCard.ClassicIncrement(section * 4 + block, value);
				if (rc == 0) {
					ShowToast("success");
				} else {
					ShowToast(String.format("errno:%x", rc));
				}
			}
			catch(Exception expt)
			{
				ShowToast(String.format("value or block info cannot be set to null"));
			}
		}
		if (view.getId() == R.id.decrement) {
			try{
				int value = Integer
						.parseInt(EditValue.getEditableText().toString());
				int section = Integer.parseInt(EditSectionNo.getEditableText()
						.toString());
				int block = Integer.parseInt(EditBlockNo.getEditableText()
						.toString());
				int rc =  MainActivity.myCard.ClassicDecrement(section * 4 + block, value);
				if (rc == 0) {
					ShowToast("success");
				} else {
					ShowToast(String.format("errno:%x", rc));
				}
			}
			catch(Exception expt)
			{
				ShowToast(String.format("value or block info cannot be set to null"));
			}
		}
	}

	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				Toast.makeText(M1ValueActivity.this, msg.obj.toString(),
						Toast.LENGTH_LONG).show();
			}

		}

	};

	private void ShowToast(String str) {
		Message msg = new Message();
		msg.obj = str;
		msg.what = 0;
		msg.setTarget(handler);
		msg.sendToTarget();
	}
}
