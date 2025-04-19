package com.rfid.hf;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import com.rfid.hf.hflib;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class ScanView extends Activity implements OnClickListener,OnItemSelectedListener{
	
	private TextView bGet;

	private EditText tvVersion;
	private TextView bSetPwr;
	private TextView bGetPwr;

	private Handler mHandler;
	

	
	private static final String TAG = "SacnView";
	
	private static final int MSG_SHOW_PROPERTIES = 0;
	private static final int MSG_UPDATE_Toast = 1;
	private static final int MSG_SHOW_SWR = 2;
	private static final int MSG_SHOW_POWER = 3;
    private byte CurrentAnt=0;
    byte[]VersionInfo=new byte[2];
    byte[]RFU =new byte[2];
    byte[]ReaderType=new byte[1];
    byte[]TrType=new byte[2];
    byte[]InventoryScanTime=new byte[1];
	Spinner spPower;

    //Check
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub  properties
		super.onCreate(savedInstanceState);	
		setContentView(R.layout.scan_view);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		initView();
		
		mHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				switch (msg.what) {
				case MSG_SHOW_PROPERTIES:
					showResult((String)msg.obj);
					break;
				case MSG_UPDATE_Toast:
					Toast.makeText(getApplicationContext(),(String)msg.obj,Toast.LENGTH_SHORT).show();
					break;
				case MSG_SHOW_POWER:
					int power = (Integer)msg.obj;
					if(power>5)power=5;
					spPower.setSelection(power, true);
					break;
				default:
					break;
				}
			}
		};  
	}
	
	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		//CurrentAnt = (byte)position;
	}
	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
//		uhf.close_reader();
	}
	
	private void initView(){

		tvVersion = (EditText)findViewById(R.id.version_text);

		bGet = (TextView)findViewById(R.id.getversion);	
		bGet.setOnClickListener(this);

		String[]PowerArr = new String[6];
		PowerArr[0]="0.25W";
		PowerArr[1]="0.50W";
		PowerArr[2]="0.75W";
		PowerArr[3]="1.00W";
		PowerArr[4]="1.25W";
		PowerArr[5]="1.50W";
		spPower = (Spinner)findViewById(R.id.band_power);
		ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,PowerArr);
		adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spPower.setAdapter(adapter2);
		spPower.setSelection(0, true);

		bSetPwr = (TextView)findViewById(R.id.setpower);
		bSetPwr.setOnClickListener(this);

		bGetPwr = (TextView)findViewById(R.id.getpower);
		bGetPwr.setOnClickListener(this);

	}
	private void UpdateErrorToast(String errormsg)
	{
		if((errormsg==null)||(errormsg.length()==0))return;
		Message message = mHandler.obtainMessage();
		message.what = MSG_UPDATE_Toast;
		message.obj = errormsg;
		mHandler.sendMessage(message);
	}
	
	@SuppressLint("ResourceAsColor")
	@Override
	public void onClick(View view) {
		if(view ==bGet)
		{
			new Thread()
	        {
	            public void run() 
	            {
	            	int result=HfData.reader.GetReaderInformationInfo(VersionInfo, RFU, ReaderType, TrType, InventoryScanTime);
	    			if(result==0){
	    				Message message = mHandler.obtainMessage();
	    				message.what = MSG_SHOW_PROPERTIES;
	    				String str_version = String.valueOf(VersionInfo[1]);
	    				if(str_version.length()==1)str_version="0"+str_version;
	    				str_version=String.valueOf(VersionInfo[0])+"."+str_version;
	    				message.obj = str_version;
	    				mHandler.sendMessage(message);
	    			}
	    			else
	    			{
	    				UpdateErrorToast(getString(R.string.failed));
	    			}
	            };
	        }.start();
			
		}
		else if(view ==bGetPwr)
		{
			new Thread()
			{
				public void run()
				{
					try {
						int result=HfData.reader.GetPower();
						if(result!=-1){
							Message message = mHandler.obtainMessage();
							message.what = MSG_SHOW_POWER;
							message.obj =result;
							mHandler.sendMessage(message);
							UpdateErrorToast(getString(R.string.success));
						}
						else
						{
							UpdateErrorToast(getString(R.string.failed));
						}
					}catch (Exception e)
					{
						UpdateErrorToast(getString(R.string.failed));
					}
				};
			}.start();
		}
		else if(view ==bSetPwr)
		{
			final byte Power = (byte)spPower.getSelectedItemPosition();
			new Thread()
			{
				public void run()
				{
					try {
						int result=HfData.reader.SetPower(Power);
						if(result==0){
							UpdateErrorToast(getString(R.string.success));
						}
						else
						{
							UpdateErrorToast(getString(R.string.failed));
						}
					}catch (Exception e)
					{
						UpdateErrorToast(getString(R.string.failed));
					}
				};
			}.start();
		}
	}

	

	

	private void showResult(String version){
		tvVersion.setText(version.toUpperCase());
	}
	
	private String getVersion(byte[] b){
		if(b!= null && b.length == 2) return b[0]+"."+b[1];
		return "";
	}
	
	private String getStr(byte[] b){
		StringBuffer sb = new StringBuffer("");
		for(int i =0; i < b.length; i++){
			sb.append(b[i]);
		}
		return sb.toString();
	}
	
}
