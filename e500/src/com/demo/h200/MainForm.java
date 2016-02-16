package com.demo.h200;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainForm extends Activity {
    /** Called when the activity is first created. */
	private static final int REQUEST_ENABLE_BT = 1;
	private static final int REQUEST_CONNECT_DEVICE = 2;
	
	private static final int MESSAGE_CONNECTION_SUCCESS = 1;
	private static final int MESSAGE_CONNECTION_FAILED = 2;
	private static final int MESSAGE_TAGID = 3;
	
	private static final String MESSAGE_KEY_TEXT = "Text";

	private static BluetoothAdapter m_BluetoothAdapter;
	private static ConnectThread m_connectThread;
    
	private static Vibrator m_vibrator;
    private static TextView m_tvStatus;
    private static TextView m_tvContinuous, m_tvSummary;
    private static Button m_cmdConnect;
    
    private static boolean m_fConnected = false;
    private static Hashtable<String, Incrementor> m_hashTagID = new Hashtable<String, Incrementor>();
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        m_cmdConnect 	= (Button) findViewById(R.id.connect);
        m_tvStatus 		= (TextView)findViewById(R.id.label);
        m_tvContinuous	= (TextView)findViewById(R.id.body1);
        m_tvSummary		= (TextView)findViewById(R.id.body2);
        
        m_tvContinuous	= (TextView)findViewById(R.id.body1);
        m_tvSummary		= (TextView)findViewById(R.id.body2);
        
        m_BluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
      
        m_vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        
    }
    public void onConnectClick(View view) 
    {	
    	if (!m_BluetoothAdapter.isEnabled()) {
    		Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
	
    		
    	} else {
			if (!m_fConnected)
	    	{
				Intent i = new Intent(this, BluetoothDevicesForm.class);
				startActivityForResult(i, REQUEST_CONNECT_DEVICE);
		   	}
	    	else
	    	{
	    		disconnectDevice();
	    	}
		}	
     }
    
    public void onQuitClick(View view)
    {	
    	finish();
    }
    
    public void onClearClick(View view)
    {	
    	m_hashTagID.clear();
    	m_tvContinuous.setText("");
    	m_tvSummary.setText("");
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
    	
    	if (!m_BluetoothAdapter.isEnabled()) {
    		m_cmdConnect.setText("Enable Bluetooth");
	
    	} else {
			if (!m_fConnected)
	    	{
	    		m_cmdConnect.setText("Connect");
	    	}
	    	else
	    	{
	    		m_cmdConnect.setText("Disconnect");
	    	}
		}	
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
    	
    	if (m_fConnected)
    	{
    		disconnectDevice();
    	}
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
      super.onConfigurationChanged(newConfig);
      setContentView(R.layout.main);
    
      m_cmdConnect 	= (Button) findViewById(R.id.connect);
      m_tvStatus 		= (TextView)findViewById(R.id.label);
      m_tvContinuous	= (TextView)findViewById(R.id.body1);
      m_tvSummary		= (TextView)findViewById(R.id.body2);
      
      m_tvContinuous	= (TextView)findViewById(R.id.body1);
      m_tvSummary		= (TextView)findViewById(R.id.body2);
   
      if (!m_BluetoothAdapter.isEnabled()) {
  		m_cmdConnect.setText("Enable Bluetooth");
	
	  	} else {
				if (!m_fConnected)
		    	{
		    		m_cmdConnect.setText("Connect");
		    	}
		    	else
		    	{
		    		m_cmdConnect.setText("Disconnect");
		    	}
		}	
       
      UpdateSummary();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        
    	switch (requestCode) {
        case REQUEST_ENABLE_BT:
            // When DeviceListActivity returns with a device to connect
        	if (resultCode == Activity.RESULT_OK) {
        		m_cmdConnect.setText("Connect");
            }
            break;
        	
        case REQUEST_CONNECT_DEVICE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
            	connectDevice(data);
            }
            break;
      
        }
	
    }
    
    private void connectDevice(Intent data)
	{
        String address = data.getExtras().getString(BluetoothDevicesForm.EXTRA_DEVICE_ADDRESS);
        BluetoothDevice device = m_BluetoothAdapter.getRemoteDevice(address);
        
        m_connectThread = new ConnectThread(device);
        m_connectThread.start();
    }

	private void disconnectDevice()
	{
		m_connectThread.cancel();
		m_connectThread = null;
		
		m_fConnected = false;
		m_cmdConnect.setText("Connect");  
		m_tvStatus.setText("Disconnected from Reader");
	}

	private final Handler MessageHandler = new Handler() 
	{
	    @Override
	    public void handleMessage(Message msg)
	    {
	    	String [] astrTagID;
	    	StringBuffer bufAppend;
		    Incrementor inc;
			    
	    	int x;
        	
	      	 switch (msg.what) 
        	 {
	        	 case MESSAGE_CONNECTION_SUCCESS:
	             {
	            	 m_tvStatus.setText("Connected to Reader!"); 	
	            	 m_cmdConnect.setText("Disconnect");  
	            	 m_fConnected = true;
	             }
	             break;
	             
	        	 case MESSAGE_CONNECTION_FAILED:
	             {
	            	 m_tvStatus.setText("Error : " + msg.getData().getString(MESSAGE_KEY_TEXT)); 	
	             }
	             break;
	             
	        	 case MESSAGE_TAGID:
	             {
	            	 m_vibrator.vibrate(50);
	            	 
	            	 astrTagID = msg.getData().getStringArray(MESSAGE_KEY_TEXT);
	            	 bufAppend = new StringBuffer();
	            	 
	            	 for(x=0; x<astrTagID.length; x++)
	            	 {
	            		 bufAppend.append(astrTagID[x]);
	            		 bufAppend.append("\r\n");
	            		 
	            		 inc = (Incrementor) m_hashTagID.get(astrTagID[x]);
	            		 if (inc == null)
	            		 {
	            			 inc = new Incrementor();
	            			 m_hashTagID.put(new String(astrTagID[x]), inc);
	            		 }
	            		
	            		 inc.increment();
	            	 }
	            	 
	            	 m_vibrator.vibrate(50);
	            	 
	            	 m_tvContinuous.append(bufAppend.toString());
	            	  
	            	 UpdateSummary();
	            	
	             }
	             break;
        	 }
	    }
	};
	
	private void UpdateSummary()
	{
		 Enumeration en;
		 String strTagID;
		 StringBuffer bufSummary;
		 Incrementor inc;
	    		
		 bufSummary = new StringBuffer();
    	 
		 en = m_hashTagID.keys();
		 
		 while (en.hasMoreElements()) 
		 {
			 strTagID = (String) en.nextElement();
			 inc = (Incrementor) m_hashTagID.get(strTagID);
			 bufSummary.append(String.format("%-30s", strTagID));
			 bufSummary.append(String.format("%5d", inc.getValue()));
			 bufSummary.append("\r\n");
		 }
		 
		 m_tvSummary.setText(bufSummary.toString());
	}
	private class ConnectThread extends Thread 
	{
	    private BluetoothSocket m_Socket = null;
	    private BluetoothDevice m_Device;
	    private ConnectedThread m_thread;
	    private Timer m_timer;
	
	    public ConnectThread(BluetoothDevice device)
	    {
	    	m_Device = device;
	
	        try {
	            Method m = device.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
	            m_Socket = (BluetoothSocket) m.invoke(device, 1);
	            
	        } catch (Exception e) {}
	   
	    }
	
	    @Override
	    public void run() {
	        try {
	        	m_Socket.connect();
	            
	            Message msg = MessageHandler.obtainMessage(MESSAGE_CONNECTION_SUCCESS);
	            Bundle bundle = new Bundle();
	            msg.setData(bundle);
	            MessageHandler.sendMessage(msg);
	          
	            m_thread = new ConnectedThread(m_Socket);
                m_thread.start();
                
                PollTimerTask task = new PollTimerTask(m_thread);
                m_timer = new Timer();
                m_timer.schedule(task, 100, 100);
                
	        } catch (IOException connectException) {
	        
	        	
	        	Message msg = MessageHandler.obtainMessage(MESSAGE_CONNECTION_FAILED);
	            Bundle bundle = new Bundle();
	            bundle.putString(MESSAGE_KEY_TEXT, connectException.getMessage());
	            msg.setData(bundle);
	            MessageHandler.sendMessage(msg);
	        	
	        	try {
	        		m_Socket.close();
	            } catch (IOException closeException) {}
	          
	            return;
	        }
	
	    }
	
	    public void cancel() 
	    {
	        try {
	        	m_Socket.close();
	        } catch (IOException e) { }
	    }
	}
	
	 private class PollTimerTask extends TimerTask
	 {	
		 private ConnectedThread m_thread;
    	
		 public PollTimerTask(ConnectedThread thread)
		 {
    		m_thread = thread;
		 }
    	
		 public void run() 
		 {
    		int [] aby = new int[9];

			aby[0] = 0x40;    // header
			aby[1] = 7;     // length
			aby[2] = 0xEE;   // list tag ID
			aby[3] = 0x2;  // EPC
			aby[4] = 0x0;     // Address
			aby[5] = 0x0;   // Address
			aby[6] = 0x0;   // Length
			aby[7] = 0x0;   // Length
			aby[8] = CalculateChecksum(aby, 0, 8);
    		
			m_thread.ResetBuffer();
    		m_thread.write(aby);
		 }
    	
    	 public int CalculateChecksum(int[] aby, int intOffset, int intLength)
         {
               int intSum = 0, Y, X; 
               int bySum;

               bySum = 0;
               for (X = intOffset; X <= intOffset + intLength - 1; X++)
               {
                    intSum += (int)aby[X];
               }
             
               if (intSum < 256)
                   bySum = intSum;
               else
                   bySum = intSum & 255;
            

               //invert the sum of all bytes (as a byte) and add one
               bySum = ~bySum & 0xFF; 
               
               return bySum + 1;

          }
     	
    }
	
	private class ConnectedThread extends Thread 
	{
		private final byte BOOTCODE_SUCCESS = -16;
		
    	private final BluetoothSocket 	m_Socket;
        private InputStream 			m_InputStream = null;
        private OutputStream			m_OutputStream = null;
        private boolean 				m_fEndLoop = false;
        
        private byte []					m_abyBuffer = new byte[10240];
        private int 					m_iOffset = 0;
        
        
        public ConnectedThread(BluetoothSocket socket) 
        {
        	m_Socket = socket;
            
            try {
            	m_InputStream = socket.getInputStream();
            	m_OutputStream = socket.getOutputStream();
            } catch (IOException e) { }
        }
        
        public void run() 
        {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes, iLen;

            while (!m_fEndLoop) {
                try {
                    bytes = m_InputStream.read(buffer);
                  
                    if (bytes >0)
                    {
                    	System.arraycopy(buffer, 0, m_abyBuffer, m_iOffset, bytes);
                    	m_iOffset += bytes;
                    	
                    	if (m_iOffset >= 2)
                    	{
                    		iLen = m_abyBuffer[1];
                    		if (iLen < 0) iLen += 256;
                    		
                    		if (m_iOffset >= iLen + 2)
                    		{
                    			ProcessIncoming();
                    		}
                    	}
                    }
                } catch (IOException e) {
                  
                }
            }
        }
        
        
        public void ResetBuffer()
        {
        	m_iOffset = 0;
        }
        
        private synchronized void ProcessIncoming()
        {
        	byte byTagIndex, byPos;
            int intLen;
            byte[] aby;
            String [] astrTagID;
            ArrayList<String> arrayTagID = new ArrayList<String>();
            String strTagID;
             
          	Message msg;
          	Bundle bundle;
          	
        	if (m_abyBuffer[0] == BOOTCODE_SUCCESS)
        	{
        		 byPos = 4;
        		 
        		 for (byTagIndex = 0; byTagIndex <= m_abyBuffer[3] - 1; byTagIndex++)
        		 {
        			 intLen = m_abyBuffer[byPos] * 2;
        			 aby = new byte[intLen];
                     
        			 if (intLen > 0)
                     {
                         System.arraycopy(m_abyBuffer, byPos + 1, aby, 0, intLen);
                         byPos += (byte)(intLen + 1);
                         
                         strTagID = toHex(aby);
                         arrayTagID.add(strTagID);

                     }
                     else
                     {
                         byPos += (byte)(intLen + 1);
                     }
        		 }      
          	}
        	
        	astrTagID = new String[arrayTagID.size()];
        	arrayTagID.toArray(astrTagID);
        	
        	if (arrayTagID.size() > 0)
        	{
	           	msg = MessageHandler.obtainMessage(MESSAGE_TAGID);
	            bundle = new Bundle();
	            bundle.putStringArray(MESSAGE_KEY_TEXT, astrTagID);
	            msg.setData(bundle);
	            MessageHandler.sendMessage(msg);
        	}
        	m_iOffset = 0;
        }

        public String toHex(byte[] bytes) {
            BigInteger bi = new BigInteger(1, bytes);
            return String.format("%0" + (bytes.length << 1) + "X", bi);
        }

        
        public void write(int[] bytes)
        {
        	byte aby[] = new byte[bytes.length];
        	
        	int x;
        	
        	for(x=0; x<bytes.length; x++)
        		if (bytes[x] < 128)
        		{
        			aby[x] = (byte)bytes[x];
        		}
        		else
        		{
        			aby[x] = (byte)(bytes[x] - 256);
        		}
        	
            try 
            {
            	m_OutputStream.write(aby);
            } catch (IOException e) 
            {
            	x=1;
            }
        }

        public void cancel() 
        {
        	m_fEndLoop = true;
        }
        
	}
	
    private class Incrementor
    {
    	private int m_iValue = 0;
    	
    	public Incrementor() 
    	{
    	
    	}
    	
    	public void increment()
    	{
    		m_iValue++;
    	}
    	
    	public int getValue()
    	{
    		return m_iValue;
    	}
    }
}