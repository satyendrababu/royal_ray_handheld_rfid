package com.rfid.hf;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
public class Connect232 extends Activity {

	private static final String TAG = "COONECTRS232";
	private static final boolean DEBUG = true;
	private TextView mConectButton;

    private String strIP;
    private String strPort;
    
    Spinner spType;
	Spinner spCom;
	Spinner spBaud;

	String[]devname = new String[1];
	String[]BaudRate = new String[1];
	String devicePath="/dev/ttyHSL0";
	int speed =19200;
	int mType=0;
	Intent intent;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        initSound();

		intent = new Intent(this,MyService.class);
		startService(intent);
		setContentView(R.layout.activity_connect232);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		
		spType = (Spinner)findViewById(R.id.comtype_spinner);
		ArrayAdapter<CharSequence> adapter =  ArrayAdapter.createFromResource(this, R.array.comtype_spinner, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spType.setAdapter(adapter); 
		spType.setSelection(0, true);
		spType.setOnItemSelectedListener(new ComOnItemSelectedListener());
		
		devname[0]="/dev/ttyHSL0";
		spCom = (Spinner)findViewById(R.id.rs232_spinner);
		ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,devname);
		adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spCom.setAdapter(adapter1); 
		spCom.setSelection(0, true);
		spCom.setOnItemSelectedListener(new ComOnItemSelectedListener());
		
		
		BaudRate[0]="19200";
		spBaud = (Spinner)findViewById(R.id.baud_spinner);
		ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,BaudRate);
		adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spBaud.setAdapter(adapter2); 
		spBaud.setSelection(0, true);
		spBaud.setOnItemSelectedListener(new ComOnItemSelectedListener());
		
		mConectButton = (TextView) findViewById(R.id.textview_connect);
		mConectButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					int result=0x30;
					result = HfData.reader.OpenReader(speed, devicePath, mType, 1, null);
					if(result==0){
						Intent intent;
						intent = new Intent().setClass(Connect232.this, MainHFActivity.class);
						startActivity(intent);
					}
					else
					{
						Toast.makeText(
								getApplicationContext(),
								"串口连接失败",
								Toast.LENGTH_SHORT).show();
					}
				}catch (Exception e) 
				{
					Toast.makeText(
							getApplicationContext(),
							"串口连接失败",
							Toast.LENGTH_SHORT).show();
				}
			}
		});
		}
		

		public class ComOnItemSelectedListener implements OnItemSelectedListener{

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				int id = arg0.getId();

				if (id == R.id.comtype_spinner) {
					mType = arg2;
				} else if (id == R.id.rs232_spinner) {
					devicePath = devname[arg2];
				} else if (id == R.id.baud_spinner) {
					if (arg2 == 0) speed = 19200;
					else if (arg2 == 1) speed = 38400;
					else if (arg2 == 2) speed = 57600;
					else if (arg2 == 3) speed = 115200;
				}

				/*switch(arg0.getId())
				{
				case R.id.comtype_spinner:
					mType = arg2;

					break;
				case R.id.rs232_spinner:
					devicePath = devname[arg2];
					break;
				case R.id.baud_spinner:
					if(arg2==0)
						speed = 19200;
					else if(arg2==1)
						speed = 38400;
					else if(arg2==2)
						speed = 57600;
					else if(arg2==3)
						speed = 115200;
					break;
				}*/
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		}
		
		
		
		public boolean onKeyDown(int keyCode, KeyEvent event) {
			if (keyCode == KeyEvent.KEYCODE_BACK) {
				
				finish();

				return true;
			}
			return super.onKeyDown(keyCode, event);
		}
		
		@Override
		protected void onDestroy() {
			// TODO Auto-generated method stub
			super.onDestroy();
			stopService(intent);
		}

    static HashMap<Integer, Integer> soundMap = new HashMap<Integer, Integer>();
    private static SoundPool soundPool;
    private static float volumnRatio;
    private static AudioManager am;
    private  void initSound(){
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 5);
        soundMap.put(1, soundPool.load(this, R.raw.scan_new, 1));
        am = (AudioManager) this.getSystemService(AUDIO_SERVICE);// 实例化AudioManager对象
        ScanMode.setsoundid(soundMap.get(1),soundPool);
    }

}
