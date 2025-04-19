package com.rfid.hf;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class RW_Activity extends Activity implements OnClickListener, OnItemSelectedListener{
	
	Spinner spuid;
	Spinner spant;
	Spinner sptype;
	private ArrayAdapter<String> spada_uid; 
	private ArrayAdapter<String> spada_ant; 
	private ArrayAdapter<String> spada_type; 
	TextView tvResult;
	EditText tvstartblock; 
	EditText tvblocknum; 
	EditText tvwdata; 
	EditText tvrdata; 

	Button btReadSingle;
	Button btReadMul;
	Button btWriteSingle;

	EditText tvPwd;
	EditText tvAFI;

	Button btSetPwd;
	Button btWritePwd;
	Button btProtectAFI;
	Button btProtectEAS;
	Button btWriteAFI;
	Button btReadAFI;
	Button btSetEAS;
	Button btReSetEAS;
	Button btDetectEAS;

	private static final int MSG_SHOW_RESULT = 0;
	private static final int MSG_SHOW_DATA = 1;
	private static final int MSG_SHOW_AFI = 2;
	private static final int MSG_SHOW_DSFID = 3;
	private Handler mHandler;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rw_data);
		initView();
		mHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				switch (msg.what) {
					case MSG_SHOW_RESULT:
						writelog((String)msg.obj, tvResult);
						break;
					case MSG_SHOW_DATA:
						tvrdata.setText((String)msg.obj);
						break;
					case MSG_SHOW_AFI:
						tvAFI.setText((String)msg.obj);
						break;

				default:
					break;
				}
			}
		};  
	}
	
	
	private void UpdateErrorToast(String errormsg)
	{
		if((errormsg==null)||(errormsg.length()==0))return;
		Message message = mHandler.obtainMessage();
		message.what = MSG_SHOW_RESULT;
		message.obj = errormsg;
		mHandler.sendMessage(message);
	}
	
	
	private void UpdateBLKData(String datastr)
	{
		if((datastr==null)||(datastr.length()==0))return;
		Message message = mHandler.obtainMessage();
		message.what = MSG_SHOW_DATA;
		message.obj = datastr;
		mHandler.sendMessage(message);
	}
	private void UpdateAFI(String datastr)
	{
		if((datastr==null)||(datastr.length()==0))return;
		Message message = mHandler.obtainMessage();
		message.what = MSG_SHOW_AFI;
		message.obj = datastr;
		mHandler.sendMessage(message);
	}
//
	private void initView(){
		spuid = (Spinner)findViewById(R.id.uid_spinner);  
		tvstartblock = (EditText)findViewById(R.id.et_wordptr);	
		tvblocknum = (EditText)findViewById(R.id.et_num);	
		tvwdata = (EditText)findViewById(R.id.et_content);	
		tvrdata = (EditText)findViewById(R.id.et_read);	
		tvResult = (TextView)findViewById(R.id.rw_result);	

		
		btReadSingle = (Button)findViewById(R.id.button_read);	
		btReadMul = (Button)findViewById(R.id.button_readmul);	
		btWriteSingle = (Button)findViewById(R.id.button_write);

		
		btReadSingle.setOnClickListener(this);
		btReadMul.setOnClickListener(this);
		btWriteSingle.setOnClickListener(this);

		

		
		sptype = (Spinner)findViewById(R.id.mtype_spinner);
		ArrayAdapter<CharSequence> adapter1 =  ArrayAdapter.createFromResource(this, R.array.type_spinner, android.R.layout.simple_spinner_item);
		adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sptype.setAdapter(adapter1); 
		sptype.setSelection(1, true);
		sptype.setOnItemSelectedListener(this);


		tvPwd = (EditText)findViewById(R.id.et_pwd);
		tvAFI = (EditText)findViewById(R.id.et_AFI);

		btSetPwd = (Button)findViewById(R.id.button_setpwd);
		btWritePwd = (Button)findViewById(R.id.button_writepwd);
		btProtectAFI = (Button)findViewById(R.id.button_protect_AFI);
		btProtectEAS = (Button)findViewById(R.id.button_protect_EAS);
		btWriteAFI = (Button)findViewById(R.id.button_writeafi);
		btReadAFI = (Button)findViewById(R.id.button_readafi);
		btSetEAS = (Button)findViewById(R.id.button_seteas);
		btReSetEAS = (Button)findViewById(R.id.button_reseteas);
		btDetectEAS = (Button)findViewById(R.id.button_detecteas);
		btSetPwd.setOnClickListener(this);
		btWritePwd.setOnClickListener(this);
		btProtectAFI.setOnClickListener(this);
		btProtectEAS.setOnClickListener(this);
		btWriteAFI.setOnClickListener(this);
		btReadAFI.setOnClickListener(this);
		btSetEAS.setOnClickListener(this);
		btReSetEAS.setOnClickListener(this);
		btDetectEAS.setOnClickListener(this);
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		if(ScanMode.mCurIvtClist!=null)
		{
			int nCount = ScanMode.mCurIvtClist.size();
			if(nCount>0)
			{
				String[]uiddata = new String[nCount];
				for (int i = 0; i < nCount; i++) {
			            HashMap<String, String> temp = new HashMap<String, String>();
			            temp = ScanMode.mCurIvtClist.get(i);
			            String uidStr = temp.get("tagUid");
			            uiddata[i] = uidStr;
			        }
				if(nCount>0)
				{
					spada_uid = new ArrayAdapter<String>(RW_Activity.this,  
			                android.R.layout.simple_spinner_item, uiddata);  
					spada_uid.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);  
				   	spuid.setAdapter(spada_uid);  
				   	spuid.setSelection(0,false);
				}
			}
			else
			{
				spuid.setAdapter(null); 
			}
		}
		else
		{
			spuid.setAdapter(null); 
		}
		
		super.onResume();
	}
	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		if(arg0 == btReadSingle){
			if(!checkContent(0)) return;
			try{
				
				if(spuid.getSelectedItem()==null)
				{
					writelog(getString(R.string.selecttag), tvResult);
					return;
				}
				String str = spuid.getSelectedItem().toString();
				str = str.substring(0,16);
				final byte state=0;
				final byte[]UID =  hexStringToBytes(str);
				final byte blockNumber=(byte)(int)Integer.valueOf(tvstartblock.getText().toString());
				final byte[]SecurityWord=new byte[1];
				final byte[]Data=new byte[4];
				final byte[]ErrorCode =new byte[1];
				tvrdata.setText("");
				
				new Thread()
		        {
		            public void run() 
		            {
		            	int result =HfData.reader.ReadSingleBlock(state, UID, blockNumber, SecurityWord, Data, ErrorCode);
						if(result==0)
						{
							String strdata = bytesToHexString(Data,0,Data.length);
							UpdateBLKData(strdata);
							UpdateErrorToast(getString(R.string.success));
						}
						else
						{
							UpdateBLKData("");
							UpdateErrorToast(getString(R.string.failed));
						}
		            };
		        }.start();
		        
			}catch(Exception ex)
			{
				UpdateBLKData("");
				UpdateErrorToast(getString(R.string.failed));
			}
		}
		else if(arg0 == btReadMul)
		{
			if(!checkContent(1)) return;
			try{

                if(spuid.getSelectedItem()==null)
                {
                    writelog(getString(R.string.selecttag), tvResult);
                    return;
                }
				String str = spuid.getSelectedItem().toString();
				str = str.substring(0,16);
				final byte state=0;
				final byte[]UID = hexStringToBytes(str);
				final byte startblockNumber=(byte)(int)Integer.valueOf(tvstartblock.getText().toString());
				final byte numberOfBlock=(byte)(int)Integer.valueOf(tvblocknum.getText().toString());
				final byte[]SecurityWord=new byte[numberOfBlock];
				final byte[]Data=new byte[numberOfBlock*4];
				final byte[]ErrorCode =new byte[1];
				tvrdata.setText("");
				
				new Thread()
		        {
		            public void run() 
		            {
		            	int result =HfData.reader.ReadMultipleBlock(state, UID, startblockNumber, numberOfBlock, SecurityWord, Data, ErrorCode);
						if(result==0)
						{
							String strdata = bytesToHexString(Data,0,Data.length);
							UpdateBLKData(strdata);
							UpdateErrorToast(getString(R.string.success));
						}
						else
						{
							UpdateBLKData("");
							UpdateErrorToast(getString(R.string.failed));
						}
		            };
		        }.start();
				
			}catch(Exception ex)
			{
				UpdateBLKData("");
				UpdateErrorToast(getString(R.string.failed));
			}
		}
		else if(arg0 == btWriteSingle)
		{
			if(!checkContent(2)) return;
			try{
				String strData="";
				
				strData = tvwdata.getText().toString();
				if((strData==null)||(strData.length()!=8))
				{
					writelog(getString(R.string.length_content_warning), tvResult);
					return;
				}
				final byte mType = (byte)sptype.getSelectedItemPosition();
                if(spuid.getSelectedItem()==null)
                {
                    writelog(getString(R.string.selecttag), tvResult);
                    return;
                }
				String str = spuid.getSelectedItem().toString();
				str = str.substring(0,16);
				final byte[]UID =  hexStringToBytes(str);
				final byte blockNumber=(byte)(int)Integer.valueOf(tvstartblock.getText().toString());
				final byte[]Data= hexStringToBytes(strData);
				final byte[]ErrorCode=new byte[1];
				new Thread()
		        {
		            public void run() 
		            {
		            	byte state=0;
						if(mType==0)
							state=0;
						else
							state=8;
		            	int result =HfData.reader.WriteSingleBlock(state, UID, blockNumber, Data, ErrorCode);
						if(result==0)
						{
                            UpdateErrorToast(getString(R.string.success));
						}
						else
						{
                            UpdateErrorToast(getString(R.string.failed));
						}
		            };
		        }.start();
		        
			}catch(Exception ex)
			{
                UpdateErrorToast(getString(R.string.failed));
			}
		}
		else if(arg0 == btSetPwd)
		{
			if(!checkContent(3)) return;
			try{
                if(spuid.getSelectedItem()==null)
                {
                    writelog(getString(R.string.selecttag), tvResult);
                    return;
                }
				String strData="";
				strData = tvPwd.getText().toString();
				if((strData==null)||(strData.length()!=8))
				{
					writelog(getString(R.string.length_content_warning), tvResult);
					return;
				}
				String str = spuid.getSelectedItem().toString();
				str = str.substring(0,16);
				final byte[]UID =  hexStringToBytes(str);
				final byte[]Password= hexStringToBytes(strData);
				new Thread()
				{
					public void run()
					{
						int result =HfData.reader.SetPassWord(UID,Password);
						if(result==0)
						{
							UpdateErrorToast(getString(R.string.success));
						}
						else
						{
							UpdateErrorToast(getString(R.string.failed));
						}
					};
				}.start();

			}catch(Exception ex)
			{
                UpdateErrorToast(getString(R.string.failed));
			}
		}
		else if(arg0 == btWritePwd)
		{
			if(!checkContent(3)) return;
			try{
                if(spuid.getSelectedItem()==null)
                {
                    writelog(getString(R.string.selecttag), tvResult);
                    return;
                }
				String strData="";
				strData = tvPwd.getText().toString();
				if((strData==null)||(strData.length()!=8))
				{
                    writelog(getString(R.string.length_content_warning), tvResult);
					return;
				}
				String str = spuid.getSelectedItem().toString();
				str = str.substring(0,16);
				final byte[]UID =  hexStringToBytes(str);
				final byte[]Password= hexStringToBytes(strData);
				new Thread()
				{
					public void run()
					{
						int result =HfData.reader.WritePassWord(UID,Password);
						if(result==0)
						{
                            UpdateErrorToast(getString(R.string.success));
						}
						else
						{
                            UpdateErrorToast(getString(R.string.failed));
						}
					};
				}.start();

			}catch(Exception ex)
			{
                UpdateErrorToast(getString(R.string.failed));
			}
		}
		else if(arg0 == btProtectAFI)
		{
			if(!checkContent(3)) return;
			try{
                if(spuid.getSelectedItem()==null)
                {
                    writelog(getString(R.string.selecttag), tvResult);
                    return;
                }
				String str = spuid.getSelectedItem().toString();
				str = str.substring(0,16);
				final byte[]UID =  hexStringToBytes(str);
				new Thread()
				{
					public void run()
					{
						int result =HfData.reader.PassWordProtect(UID,(byte)0);
						if(result==0)
						{
							UpdateErrorToast(getString(R.string.success));
						}
						else
						{
                            UpdateErrorToast(getString(R.string.failed));
						}
					};
				}.start();

			}catch(Exception ex)
			{
                UpdateErrorToast(getString(R.string.failed));
			}
		}
		else if(arg0 == btProtectEAS)
		{
			if(!checkContent(3)) return;
			try{
                if(spuid.getSelectedItem()==null)
                {
                    writelog(getString(R.string.selecttag), tvResult);
                    return;
                }
				String str = spuid.getSelectedItem().toString();
				str = str.substring(0,16);
				final byte[]UID =  hexStringToBytes(str);
				new Thread()
				{
					public void run()
					{
						int result =HfData.reader.PassWordProtect(UID,(byte)1);
						if(result==0)
						{
							UpdateErrorToast(getString(R.string.success));
						}
						else
						{
                            UpdateErrorToast(getString(R.string.failed));
						}
					};
				}.start();

			}catch(Exception ex)
			{
                UpdateErrorToast(getString(R.string.failed));
			}
		}

		else if(arg0 == btWriteAFI)
		{
			if(!checkContent(4)) return;
			try{
                if(spuid.getSelectedItem()==null)
                {
                    writelog(getString(R.string.selecttag), tvResult);
                    return;
                }
				String strData="";
				strData = tvAFI.getText().toString();
				if((strData==null)||(strData.length()!=2))
				{
					writelog(getString(R.string.length_content_warning), tvResult);
					return;
				}
				final byte mType = (byte)sptype.getSelectedItemPosition();
				String str = spuid.getSelectedItem().toString();
				str = str.substring(0,16);
				final byte[]UID =  hexStringToBytes(str);
				final byte AFI= (byte)(int)(Integer.valueOf(strData,16));
				final byte[]ErrorCode =new byte[1];
				new Thread()
				{
					public void run()
					{
						byte state=0;
						if(mType==0)
							state=0;
						else
							state=8;
						int result =HfData.reader.WriteAFI(state,UID,AFI,ErrorCode);
						if(result==0)
						{
							UpdateErrorToast(getString(R.string.success));
						}
						else
						{
                            UpdateErrorToast(getString(R.string.failed));
						}
					};
				}.start();

			}catch(Exception ex)
			{
                UpdateErrorToast(getString(R.string.failed));
			}
		}
		else if(arg0 == btReadAFI)
		{
			try{
                if(spuid.getSelectedItem()==null)
                {
                    writelog(getString(R.string.selecttag), tvResult);
                    return;
                }
				String strData="";
				String str = spuid.getSelectedItem().toString();
				str = str.substring(0,16);
				final byte[]UID =  hexStringToBytes(str);
				new Thread()
				{
					public void run()
					{
						byte[]InformationFlag=new byte[1];
						byte[]DSFID=new byte[1];
						byte[]AFI=new byte[1];
						byte[]MemorySize=new byte[2];
						byte[] ErrorCode = new byte[1];
						int result =HfData.reader.GetSystemInformation((byte)0,UID,InformationFlag,DSFID,AFI,MemorySize,ErrorCode);
						if(result==0)
						{
						    String str = Integer.toHexString(AFI[0]&255);
						    if(str.length()==1)str="0"+str;
							UpdateAFI(str);
                            UpdateErrorToast(getString(R.string.success));
						}
						else
						{
                            UpdateErrorToast(getString(R.string.failed));
						}
					};
				}.start();

			}catch(Exception ex)
			{
                UpdateErrorToast(getString(R.string.failed));
			}
		}

		else if(arg0 == btSetEAS)
		{
			try{
                if(spuid.getSelectedItem()==null)
                {
                    writelog(getString(R.string.selecttag), tvResult);
                    return;
                }
				String str = spuid.getSelectedItem().toString();
				str = str.substring(0,16);
				final byte[]UID =  hexStringToBytes(str);
				new Thread()
				{
					public void run()
					{
						int result =HfData.reader.SetEAS(UID);
						if(result==0)
						{
                            UpdateErrorToast(getString(R.string.success));
						}
						else
						{
                            UpdateErrorToast(getString(R.string.failed));
						}
					};
				}.start();

			}catch(Exception ex)
			{
                UpdateErrorToast(getString(R.string.failed));
			}
		}
		else if(arg0 == btReSetEAS)
		{
			try{
                if(spuid.getSelectedItem()==null)
                {
                    writelog(getString(R.string.selecttag), tvResult);
                    return;
                }
				String str = spuid.getSelectedItem().toString();
				str = str.substring(0,16);
				final byte[]UID =  hexStringToBytes(str);
				new Thread()
				{
					public void run()
					{
						int result =HfData.reader.ResetEAS(UID);
						if(result==0)
						{
                            UpdateErrorToast(getString(R.string.success));
						}
						else
						{
                            UpdateErrorToast(getString(R.string.failed));
						}
					};
				}.start();

			}catch(Exception ex)
			{
                UpdateErrorToast(getString(R.string.failed));
			}
		}
		else if(arg0 == btDetectEAS)
        {
            try{
                if(spuid.getSelectedItem()==null)
                {
                    writelog(getString(R.string.selecttag), tvResult);
                    return;
                }
                String str = spuid.getSelectedItem().toString();
                str = str.substring(0,16);
                final byte[]UID =  hexStringToBytes(str);
                new Thread()
                {
                    public void run()
                    {
                        int result =HfData.reader.EASAlarm(UID);
                        if(result==0)
                        {
                            UpdateErrorToast(getString(R.string.strEasAlarm));
                        }
                        else
                        {
                            UpdateErrorToast(getString(R.string.strNoalarm));
                        }
                    };
                }.start();

            }catch(Exception ex)
            {
                UpdateErrorToast(getString(R.string.failed));
            }
        }

	}
	

	//05 现在是默认就读卡了，接上高电平反而不读
	public void writelog(String log,TextView tvResult)
	{
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");// HH:mm:ss
		Date date = new Date(System.currentTimeMillis());
		String textlog = simpleDateFormat.format(date)+" "+log;
		tvResult.setText(textlog);
	}

	private boolean checkContent(int check){
		switch (check) {
			case 0:
				if(Util.isEtEmpty(tvstartblock)) return Util.showWarning(this,R.string.content_empty_warning);
				break;
			case 1:
				if(Util.isEtEmpty(tvstartblock)) return Util.showWarning(this,R.string.content_empty_warning);
				if(Util.isEtEmpty(tvblocknum)) return Util.showWarning(this,R.string.content_empty_warning);
				break;
			case 2:
				if(Util.isEtEmpty(tvstartblock)) return Util.showWarning(this,R.string.content_empty_warning);
				if(Util.isEtEmpty(tvblocknum)) return Util.showWarning(this,R.string.content_empty_warning);
				if(Util.isEtEmpty(tvwdata)) return Util.showWarning(this,R.string.content_empty_warning);
				if(!(Util.isLenLegal(tvwdata)))
					return Util.showWarning(this,R.string.str_lenght_odd_warning);
				break;
			case 3:
				if(Util.isEtEmpty(tvPwd)) return Util.showWarning(this,R.string.pwd_empty_warning);
				break;
			case 4:
				if(Util.isEtEmpty(tvAFI)) return Util.showWarning(this,R.string.content_empty_warning);
				break;
			default:
			break;
		}
		return true;
	}
	
	public String bytesToHexString(byte[] src, int offset, int length) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = offset; i < length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() == 1) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString().toUpperCase();
    }

	public byte[] hexStringToBytes(String hexString) {  
        if (hexString == null || hexString.equals("")) {  
            return null;  
        }  
        hexString = hexString.toUpperCase();  
        int length = hexString.length() / 2;  
        char[] hexChars = hexString.toCharArray();  
        byte[] d = new byte[length];  
        for (int i = 0; i < length; i++) {  
            int pos = i * 2;  
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));  
        }  
        return d;  
    }   
    private byte charToByte(char c) {  
        return (byte) "0123456789ABCDEF".indexOf(c);  
    }
    
}
