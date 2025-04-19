package com.rfid.hf;
import android.os.Bundle;
import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TabHost;

public class MainHFActivity extends TabActivity {
	
	private String[] tableMenu = {"Scan","ISO15693"};
	private Intent[] tableIntents;
	
	private TabHost myTabHost;
	
	
	public static final String EXTRA_MODE = "mode";


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		myTabHost = getTabHost();
		Intent intent0 = new Intent(this,ScanView.class);
		Intent intent1 = new Intent(this,ScanModeGroup.class);
		intent1.putExtra(EXTRA_MODE, getString(R.string.tab_scan));
		Intent intent2 = new Intent(this,RW_Activity.class);
		
		TabHost.TabSpec tabSpec0 = myTabHost.newTabSpec(getString(R.string.tab_set)).setIndicator(getString(R.string.tab_set)).setContent(intent0);
		TabHost.TabSpec tabSpec1 = myTabHost.newTabSpec(getString(R.string.tab_scan)).setIndicator(getString(R.string.tab_scan)).setContent(intent1);
		TabHost.TabSpec tabSpec2 = myTabHost.newTabSpec(getString(R.string.tab_operation)).setIndicator(getString(R.string.tab_operation)).setContent(intent2);
		
		myTabHost.addTab(tabSpec0);
		myTabHost.addTab(tabSpec1);
		myTabHost.addTab(tabSpec2);
		myTabHost.setCurrentTab(0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		HfData.reader.CloseReader();
	}
}
