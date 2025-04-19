package com.rfid.hf;


import android.R.integer;
import android.content.Context;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.rfid.trans.ReaderBase;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;


public class hflib {
	private byte addr=(byte)255;
	private int uhf_speed;
	private byte uhf_addr;
	private Context mContext;
	private String Serial;
	private int logswitch;
	private int mType=0;
	private int FrmHandle=-1;
	public int AntennaCount=0;
	public int ReaderType=0;
	ReaderBase reader = new ReaderBase();
	public void hflib()
	{	
		
	}
	private void set53CGPIOEnabled(boolean enable){
		FileOutputStream f = null;
		FileOutputStream f1 = null;
		try{
			f = new FileOutputStream("/sys/devices/soc/soc:sectrl/ugp_ctrl/gp_pogo_5v_ctrl/enable");
			f.write(enable?"1".getBytes():"0".getBytes());
			f1 = new FileOutputStream("/sys/devices/soc/soc:sectrl/ugp_ctrl/gp_otg_en_ctrl/enable");
			f1.write(enable?"1".getBytes():"0".getBytes());
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			if(f != null){
				try {
					f.close();
					f1.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	private void set53QGPIOEnabled(boolean enable){
		FileOutputStream f = null;
		FileOutputStream f1 = null;
		try{
			f = new FileOutputStream("/sys/devices/soc/c170000.serial/pogo_uart");
			f.write(enable?"1".getBytes():"0".getBytes());
			f1 = new FileOutputStream("/sys/devices/virtual/Usb_switch/usbswitch/function_otg_en");
			f1.write(enable?"2".getBytes():"0".getBytes());
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			if(f != null){
				try {
					f.close();
					f1.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	private  String GetReaderOsType()
	{
		String readerosType=null;
		try {
			Class<?> c =Class.forName("android.os.SystemProperties");
			Method get =c.getMethod("get", String.class);
			readerosType = (String)get.invoke(c, "pwv.project");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return readerosType;
	}

	public void PowerControll(Context mContext,boolean enable) {
		String Product= GetReaderOsType();
		if(Product.equals("SQ53C")){
			set53CGPIOEnabled(enable);
		}else if(Product.equals("SQ53Q")){
			set53QGPIOEnabled(enable);
		}
		else{
			set53CGPIOEnabled(enable);
		}
	}
	public int OpenReader(int tty_speed,String serial, int mcType,int log_swith,Context mCt)
	{
		try
		{	
			this.uhf_speed = tty_speed;
			this.uhf_addr = addr;
			this.mContext = mCt;
			this.Serial = serial;
			this.logswitch = log_swith;
			this.mType = mcType;
			
			int reply1=0x30;
			int[]fd = new int[1];
			fd[0]=-1;
			reply1 = reader.ConnectReader(Serial,uhf_speed,logswitch);
			PowerControll(null,true);
			SystemClock.sleep(1500);

			if(reply1==0)
			{
				FrmHandle = fd[0];
				byte RFU[]=new byte[2];
				byte TVersionInfo[]=new byte[2];
				byte ReadType[]=new byte[1];
				byte TrType[]=new byte[2];
				byte ScanTime[]=new byte[1];
				reply1 = reader.GetReaderInfo(TVersionInfo, RFU,ReadType, TrType, ScanTime);
				if(reply1==0)
				{
					AntennaCount = (RFU[0] & 0x1F)+1;
					ReaderType = ReadType[0]&255;
				}
			}
			return reply1;
		}
		catch(Exception e)
		{
			return -1;
		}
	 }
	
	public int CloseReader()
	{
		reader.DisconnectReader();
		PowerControll(null,false);
		return 0;
	}
	public int GetReaderInformationInfo(byte VersionInfo[],
			byte RFU[],
			byte ReaderType[],
			byte TrType[],
			byte InventoryScanTime[])
	{
		try
		{
			return reader.GetReaderInfo(VersionInfo, RFU,ReaderType, TrType, InventoryScanTime);
		}
		catch(Exception e)
		{
			return -1;
		}
	}

   public int CloseRf()
   {
	   return reader.CloseRf();
   }
   
   public int OpenRf()
   {
	   return reader.OpenRf();
   }
  
   private void ClearArray(byte[] source)
   {
	   for(int i=0;i<source.length;i++) {
		   source[i]=0;
	   }
   }
   public int GetPower()
   {
		byte[]Power = new byte[1];
		int result =  reader.GetPower(Power);
		if(result==0)
		{
			return Power[0];
		}
		else
		{
			return -1;
		}
   }

   public int SetPower(int Power)
   {
	   return reader.SetPower((byte)Power);
   }

   
   public int Inventory(byte state,byte[]UID,int[] CardNum)
   {
	   if(!MyService.GetRfStatus()) reader.OpenRf();
	   return reader.Inventory(state,(byte)0,UID,CardNum);
   }
   
   public int ReadSingleBlock(byte state,byte UID[],byte blockNumber,
			byte SecurityWord[],byte Data[],byte ErrorCode[])
   {
	   if(!MyService.GetRfStatus()) reader.OpenRf();
	   return reader.ReadSingleBlock(state,UID,blockNumber,SecurityWord,Data,ErrorCode);
   }
   
   public int WriteSingleBlock(byte state,byte UID[],byte blockNumber,
			byte Data[],byte ErrorCode[])
   {
	   if(!MyService.GetRfStatus()) reader.OpenRf();
   	return reader.WriteSingleBlock(state,UID,blockNumber,Data,ErrorCode);
   }
   
   public int ReadMultipleBlock(byte state,byte UID[],byte startblockNumber,
	        byte numberOfBlock,byte SecurityWord[],byte Data[],byte ErrorCode[])
   {
	   if(!MyService.GetRfStatus()) reader.OpenRf();
	   return reader.ReadMultipleBlock(state,UID,startblockNumber,numberOfBlock,SecurityWord,Data,ErrorCode);
   }

	public int LockBlock(byte state,byte[] UID,byte blockNumber,byte ErrorCode[])
	{
		if(!MyService.GetRfStatus()) reader.OpenRf();
		return reader.LockBlock(state,UID,blockNumber,ErrorCode);
	}

	public int WriteAFI(byte state,byte[] UID,byte AFI,byte ErrorCode[])
	{
		if(!MyService.GetRfStatus()) reader.OpenRf();
		return reader.WriteAFI(state,UID,AFI,ErrorCode);
	}
	public int LockAFI(byte state,byte[] UID,byte ErrorCode[])
	{
		if(!MyService.GetRfStatus()) reader.OpenRf();
		return reader.LockAFI(state,UID,ErrorCode);
	}

	public int WriteDSFID(byte state,byte[] UID,byte DSFID,byte ErrorCode[])
	{
		if(!MyService.GetRfStatus()) reader.OpenRf();
		return reader.WriteDSFID(state,UID,DSFID,ErrorCode);
	}
	public int LockDSFID(byte state,byte[] UID,byte ErrorCode[])
	{
		if(!MyService.GetRfStatus()) reader.OpenRf();
		return reader.LockDSFID(state,UID,ErrorCode);
	}

	public int GetSystemInformation(byte state,byte[] UID,byte[]InformationFlag,byte[]DSFID,byte[]AFI,byte[]MemorySize,byte ErrorCode[])
	{
		if(!MyService.GetRfStatus()) reader.OpenRf();
		return reader.GetSystemInformation(state,UID,InformationFlag,DSFID,AFI,MemorySize,ErrorCode);
	}

	public int TransparentRead(int RspLength,int CustomDatalength,byte[] CustomData,int[] FeedbackDataLength,byte[] FeedbackData,byte[]ErrorCode)
	{
		if(!MyService.GetRfStatus()) reader.OpenRf();
		return reader.TransparentRead(RspLength,CustomDatalength,CustomData,FeedbackDataLength,FeedbackData,ErrorCode);
	}

	public int TransparentWrite(byte[]option,int RspLength,int CustomDatalength,byte[] CustomData,int[] FeedbackDataLength,byte[] FeedbackData,byte[]ErrorCode)
	{
		if(!MyService.GetRfStatus()) reader.OpenRf();
		return reader.TransparentWrite(option,RspLength,CustomDatalength,CustomData,FeedbackDataLength,FeedbackData,ErrorCode);
	}

	public int SetEAS(byte[]UID)
	{
		if(!MyService.GetRfStatus()) reader.OpenRf();
		return reader.SetEAS((byte)1,UID);
	}

	public int ResetEAS(byte[]UID)
	{
		if(!MyService.GetRfStatus()) reader.OpenRf();
		return reader.ResetEAS((byte)1,UID);
	}

	public int LockEAS(byte[]UID)
	{
		if(!MyService.GetRfStatus()) reader.OpenRf();
		return reader.LockEAS((byte)1,UID);
	}
	public int EASAlarm(byte[]UID)
	{
		if(!MyService.GetRfStatus()) reader.OpenRf();
		return reader.EASAlarm((byte)1,UID);
	}

	public int SetPassWord(byte[]UID,byte[]PassWord)
	{
		if(!MyService.GetRfStatus()) reader.OpenRf();
		return reader.SetPassWord(UID,PassWord);
	}

	public int WritePassWord(byte[]UID,byte[]PassWord)
	{
		if(!MyService.GetRfStatus()) reader.OpenRf();
		return reader.WritePassWord(UID,PassWord);
	}

	public int LockPassWord(byte[]UID,byte[]PassWord)
	{
		if(!MyService.GetRfStatus()) reader.OpenRf();
		return reader.LockPassWord(UID);
	}

	public int PassWordProtect(byte[]UID,byte optionFlag)
	{
		if(!MyService.GetRfStatus()) reader.OpenRf();
		return reader.PassWordProtect(UID,optionFlag);
	}

	
}





