package com.demo.h200;

import java.util.Set;

import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class BluetoothDevicesForm extends ListActivity 
{
	public static String EXTRA_DEVICE_ADDRESS = "device_address";
	private static BluetoothAdapter m_BluetoothAdapter;

    private ArrayAdapter<String> m_DevicesArrayAdapter;
    private ListView m_lv;
    
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
    	super.onCreate(savedInstanceState);
    	
    	requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    	
    	setContentView(R.layout.devices);
    	setTitle("Scanning for Bluetooth devices (please hang on...)");
    	
    	m_lv = getListView();
	}
	
	@Override
    public void onStart() 
    {
    	super.onStart();
    	     	
    	
    }
    
    @Override
    public void onRestart() 
    {
    	super.onRestart();
   
    }	
    
    @Override
    public void onResume() 
    {
    	super.onResume();
    	
    	m_DevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.devices_row);
        m_BluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
   	  	   	   	
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);
        
        setListAdapter(m_DevicesArrayAdapter);
        m_lv.setOnItemClickListener(mDeviceClickListener);
        
   	   	Set<BluetoothDevice> pairedDevices = m_BluetoothAdapter.getBondedDevices();
	   	for (BluetoothDevice device : pairedDevices) 
   	   	{
	   		m_DevicesArrayAdapter.add("PAIRED - " + device.getName() + " [" + device.getAddress() + "]");
   	   	}
	   	
	    doDiscovery();
    }
    
    @Override
    public void onPause()
    {
    	super.onPause();
    }
    	
    @Override
    public void onStop()
    {
    	super.onStop();
    }
    
    @Override
    public void onDestroy()
    {
    	super.onDestroy();
    	
        if (m_BluetoothAdapter != null) {
        	m_BluetoothAdapter.cancelDiscovery();
        }

    }
    
    private void doDiscovery() {
        setProgressBarIndeterminateVisibility(true);
      
	
	    if (m_BluetoothAdapter.isDiscovering()) {
	    	m_BluetoothAdapter.cancelDiscovery();
	    }
	
	    m_BluetoothAdapter.startDiscovery();
    }	
    
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
        	m_BluetoothAdapter.cancelDiscovery();

            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 18, info.length()-1);

            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };
	    
	  
	 private final BroadcastReceiver mReceiver = new BroadcastReceiver() 
	 {
        @Override
        public void onReceive(Context context, Intent intent) 
        {
            String action = intent.getAction();
	
	        if (BluetoothDevice.ACTION_FOUND.equals(action)) 
	        {
	            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
	
	            if (device.getBondState() != BluetoothDevice.BOND_BONDED) 
	            {
	                m_DevicesArrayAdapter.add(device.getName() + " [" + device.getAddress() + "]");
	            }
		
	        } else 
	        	if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) 
	        	{
	        		setProgressBarIndeterminateVisibility(false);
	        		setTitle("Scanning of Bluetooth devices completed.");
		           
		            if (m_DevicesArrayAdapter.getCount() == 0) 
		            {
		             
		            }
		        }
        }
	};
    
}
