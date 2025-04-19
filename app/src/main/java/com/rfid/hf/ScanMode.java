package com.rfid.hf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import android.app.Activity;
import android.app.ActivityGroup;
import android.content.Context;
import android.content.Intent;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class ScanMode extends Activity implements OnClickListener, OnItemClickListener{
	
	private String mode;
	private Map<String,Integer> data;
	
	Button btStart;
	Button btStop;
	Button btClear;
	ListView listView;
	TextView txNum;
	TextView txTime;
	TextView txCount;
	TextView txinfo;
	private Timer timer;
	private Handler mHandler;
	private boolean isCanceled = true;
	
	private static final int SCAN_INTERVAL = 10;
	private static final int MSG_UPDATE_LISTVIEW = 0;
	private static final int MSG_UPDATE_BUTTON = 2;
	private static final int MSG_UPDATE_INFO=1;
	private static final int MSG_UPDATE_CLEAR=3;
	private static final int MSG_UPDATE_ADD=4;
	private static boolean Scanflag=false;
	
	public static ArrayList<HashMap<String, String>> mCurIvtClist;
	public static ArrayList<HashMap<String, String>> mnewIvtClist;
	SimpleAdapter adapterTaglist;
	
	Thread mThread;
	volatile boolean mWorking=false;
	long Count=0;
	long Number=0;
	long ScanTime =0;
	private static SoundPool soundPool=null;
	private static  Integer soundid=null;
	public static void setsoundid(int id, SoundPool soundPoola)
	{
		soundid =id;
		soundPool = soundPoola;
	}
	public  void playSound() {
		if((soundid==null)||(soundPool==null))return;
		try {
			soundPool.play(soundid, 1, // 左声道音量
					1, // 右声道音量
					1, // 优先级，0为最低
					0, // 循环次数，0无不循环，-1无永远循环
					1  // 回放速度 ，该值在0.5-2.0之间，1为正常速度
			);
			//SystemClock.sleep(50);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		try
		{
			setContentView(R.layout.query);	
			mCurIvtClist = new ArrayList<HashMap<String, String>>();
			mnewIvtClist = new ArrayList<HashMap<String, String>>();
			adapterTaglist = new SimpleAdapter(ScanMode.this, mCurIvtClist, R.layout.listtag_items,
					new String[] { "tagid","tagUid"},
					new int[] { R.id.tv_id,R.id.tv_Uid});

			
			txNum = (TextView)findViewById(R.id.textNumber);
			txTime = (TextView)findViewById(R.id.textTime);
			txCount = (TextView)findViewById(R.id.textCount);

			
			btStart = (Button)findViewById(R.id.btn_startInventory);
			btStart.setOnClickListener(this);
			
			btStop = (Button)findViewById(R.id.btn_stopInventory);
			btStop.setOnClickListener(this);
			
			btClear = (Button)findViewById(R.id.btn_clearList);
			btClear.setOnClickListener(this);
			

			
			listView = (ListView)findViewById(R.id.list_inventory_record);
			//listView.setOnItemClickListener(this);
			
			
			mHandler = new Handler(){

				@Override
				public void handleMessage(Message msg) {
					// TODO Auto-generated method stub
					//if(isCanceled) return;
					switch (msg.what) {
						case MSG_UPDATE_LISTVIEW:
							listView.setAdapter(adapterTaglist);
							adapterTaglist.notifyDataSetChanged();
							txCount.setText(String.valueOf(Count));
							txNum.setText(String.valueOf(Number));
							txTime.setText(String.valueOf(ScanTime));
						break;
						case MSG_UPDATE_BUTTON:
							btStart.setEnabled(true);
							btStop.setEnabled(false);
							btClear.setEnabled(true);
							break;
						case MSG_UPDATE_CLEAR:
							mCurIvtClist.clear();;
							break;
						case MSG_UPDATE_ADD:
							String temp = msg.obj+"";
							String[]uidtemp = temp.split(",");
							HashMap<String, String> temps = new HashMap<String, String>();
							temps.put("tagid", uidtemp[0]);
							temps.put("tagUid", uidtemp[1]);
							mCurIvtClist.add(temps);
							Number = mCurIvtClist.size();
							break;
					default:
						break;
					}
					super.handleMessage(msg);
				}
				
			};
		}
		catch(Exception e)
		{
			
		}
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		btStart.setEnabled(true);
		btStop.setEnabled(false);

	}
	
	@Override
	public void onClick(View arg0) {
		if(arg0==btStart)
		{
			if(mThread==null)
			{
				btStart.setEnabled(false);
				btStop.setEnabled(true);
				btClear.setEnabled(false);
				mWorking=true;
				Count=0;


				mThread = new Thread(new Runnable() 
				{  
		            @Override  
		            public void run() 
		            {  
		            	try{
		            		while(mWorking)
		            		{
		            			long beginTime = System.currentTimeMillis();
								mnewIvtClist.clear();
								InventoryTag();
								//mCurIvtClist.clear();
								mHandler.removeMessages(MSG_UPDATE_CLEAR);
								mHandler.sendEmptyMessage(MSG_UPDATE_CLEAR);
								SystemClock.sleep(10);
		            			Count++;
		            			if(mnewIvtClist.size()>0)
		            			{
                                    playSound();
		            				 for (int i = 0; i < mnewIvtClist.size(); i++) {
		            			            HashMap<String, String> temp = new HashMap<String, String>();
		            			            temp = mnewIvtClist.get(i);
		            			            String data = temp.get("tagid")+","+temp.get("tagUid");
										 AddUID(data);

		            			        }
		            			}
		            			ScanTime = System.currentTimeMillis() - beginTime;
		            			mHandler.removeMessages(MSG_UPDATE_LISTVIEW);
		            			mHandler.sendEmptyMessage(MSG_UPDATE_LISTVIEW);
		            		}
		            		mThread=null;
							mHandler.removeMessages(MSG_UPDATE_BUTTON);
							mHandler.sendEmptyMessage(MSG_UPDATE_BUTTON);
		            	}catch(Exception ex)
		            	{mThread=null;}
		            }  
		        });
				mThread.start();
			}
		}
		else if(arg0==btStop)
		{

			mWorking =false;
		}
		else if(arg0==btClear)
		{
			Count=0;
			Number=0;
			ScanTime=0;
			txNum.setText("0");
			txTime.setText("0");
			txCount.setText("0");
			mCurIvtClist.clear();
			mHandler.removeMessages(MSG_UPDATE_LISTVIEW);
			mHandler.sendEmptyMessage(MSG_UPDATE_LISTVIEW);
			mHandler.removeMessages(MSG_UPDATE_INFO);
			mHandler.sendEmptyMessage(MSG_UPDATE_INFO);
		}
	}

	private void AddUID(String UIDINfo)
	{
		if((UIDINfo==null)||(UIDINfo.length()==0))return;
		Message message = mHandler.obtainMessage();
		message.what = MSG_UPDATE_ADD;
		message.obj = UIDINfo;
		mHandler.sendMessage(message);
	}
	
	private void InventoryTag()
	{
		byte state = (byte)0x06;
		int fCmdRet=0;
		do{
			byte[]UID= new byte[25600];
			int[]CardNum = new int[1];
			CardNum[0]=0;
			fCmdRet = HfData.reader.Inventory(state, UID, CardNum);
			if(CardNum[0]>0)
			{
				for(int m=0;m<CardNum[0];m++)
				{
					byte[]daw = new byte[9];
					System.arraycopy(UID, m*9, daw, 0, 9);
					String uidStr = Util.bytesToHexString(daw, 1, 9);

					HashMap<String, String> temp = new HashMap<String, String>();
					temp.put("tagid", mnewIvtClist.size()+1+"");
					temp.put("tagUid", uidStr);


					int index = checkIsExist(uidStr,mnewIvtClist);
					if(index==-1)//不存在
					{
						mnewIvtClist.add(temp);
					}
				}
			}
			state = (byte)0x02;
		}while(fCmdRet!=0x0E);
	}
	
	public boolean isEmpty(String strEPC)
	{
		return strEPC==null || strEPC.length()==0;
	}
	public int checkIsExist(String strUID,ArrayList<HashMap<String, String>> mList){
        int existFlag = -1;
        if (isEmpty(strUID)) {
            return existFlag;
        }
        if(mList==null)
        {
        	return existFlag;
        }
        String tempStr = "";
        for (int i = 0; i < mList.size(); i++) {
            HashMap<String, String> temp = new HashMap<String, String>();
            temp = mList.get(i);
            tempStr = temp.get("tagUid");
            if (strUID.equals(tempStr)) {
                existFlag = i;
                break;
            }
        }
        return existFlag;
    }
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		// TODO Auto-generated method stub

	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mWorking =false;
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	

}
